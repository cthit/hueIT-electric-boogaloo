import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.stringType
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
import kotlin.collections.set

val client = HttpClient()
val debug = true

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
    val hueIDMap = Key("hueidmap", stringType)

    val mapString: String = config[hueIDMap].toString()
    val idMap: List<Int?> = mapString.split(", ").map { it.toIntOrNull() }

    println(idMap)

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
                    handleRequestBody(reqBod, baseURL, idMap)
                }
            }

            post("/list") {
                val reqBods = call.receive<RequestBodyList>()
                val responses = ArrayList<String>()

                reqBods.requestBodyList.forEach {
                    responses.add(handleRequestBody(it, baseURL, idMap))
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

suspend fun handleRequestBody(reqBod: RequestBody, baseURL: String, idMap: List<Int?>): String {
    val id: Int? = if (reqBod.isGroup) reqBod.id else idMap[reqBod.id]
    val groupString: String = if (reqBod.isGroup) "groups" else "lights"

    if (reqBod.props == null) {
        return client.get {
            url(
                baseURL +
                        groupString +
                        (if (id != null && id >= 0) "/$id" else "")
            )
        }
    } else {
        if (id == null) {
            return "{\"error\": \"Invalid ID.\"}"
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

        val updateResponse = statusUpdate(baseURL, body, groupString, id)

        println(updateResponse)

        return updateResponse
    }
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
