import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.url
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.json.simple.JSONObject
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlin.collections.set
import com.natpryce.konfig.*

val client = HttpClient()
val debug = true
val weekend = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

data class RequestBodyList(
    val requestBodyList: List<RequestBody>
)

data class RequestBody(
    val isGroup: Boolean,
    val id: Int,
    val props: RequestBodyProperty?
)

data class RequestBodyProperty(
    val pwr: Boolean?,
    val hue: Double?,
    val sat: Double?,
    val bri: Double?,
    val rst: Boolean?
)

fun main(args: Array<String>) {
    val config = ConfigurationProperties.fromResource("config.properties")

    val selfPort = Key("server.port", intType)
    val hueHost = Key("huebridge.host", stringType)
    val hueKey = Key("huebridge.key", stringType)

    val baseURL = "http://" + config[hueHost] + "/api/" + config[hueKey] + "/"

    val server = embeddedServer(Netty, port = config[selfPort]) {
        install(ContentNegotiation) {
            jackson {
            }
        }

        routing {
            post("/") {
                val reqBod = call.receive<RequestBody>()

                call.respondText {
                    handleRequestBody(reqBod, baseURL)
                }
            }

            post("/list") {
                val reqBods = call.receive<RequestBodyList>()
                val responses = ArrayList<String>()

                reqBods.requestBodyList.forEach {
                    responses.add(handleRequestBody(it, baseURL))
                }

                val responseBody = JSONObject()
                responseBody["responses"] = responses

                call.respondText {
                    responseBody.toString()
                }
            }
        }
    }

    server.start(wait = true)
}

suspend fun handleRequestBody(reqBod: RequestBody, baseURL: String): String {
    if (reqBod.props == null) {
        return client.get {
            url(
                baseURL +
                        (if (reqBod.isGroup) "groups" else "lights") +
                        (if (reqBod.id >= 0) "/" + reqBod.id else "")
            )
        }
    } else {
        if (!isAllowedTime()) {
            return "{\"error\": \"Not allowed to change lights at this time and day.\"}"
        }

        val body = JSONObject()

        if (reqBod.props.pwr != null) {
            body["on"] = reqBod.props.pwr
        }
        if (reqBod.props.hue != null) {
            body["hue"] = (reqBod.props.hue * 65535 / 360).toInt()
        }
        if (reqBod.props.sat != null) {
            body["sat"] = (reqBod.props.sat * 254 / 100).toInt()
        }
        if (reqBod.props.bri != null) {
            body["bri"] = (reqBod.props.bri * 254 / 100).toInt()
        }
        if (reqBod.props.rst != null && reqBod.props.rst) {
            body["hue"] = 8418
            body["bri"] = 254
            body["sat"] = 140
        }

        val updateResponse = statusUpdate(baseURL, body, if (reqBod.isGroup) "groups" else "lights", reqBod.id)

        println(updateResponse)

        return updateResponse
    }
}

fun isAllowedTime(): Boolean {
    if (debug) {
        return true
    }

    val dt = LocalDateTime.now()

    if (dt.hour < 8 || dt.hour >= 17) {
        return true
    }

    if (dt.dayOfWeek in weekend) {
        return true
    }

    return false
}

suspend fun statusUpdate(baseURL: String, bodyObject: JSONObject, type: String, id: Int): String {
    val stateName = if (type == "groups") "action" else "state"

    val fullURL = "$baseURL$type/$id/$stateName"

    if (debug) {
        println(bodyObject.toString())
    }

    return client.put {
        url(fullURL)
        body = bodyObject.toString()
    }
}
