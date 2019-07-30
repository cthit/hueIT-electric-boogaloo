import io.ktor.application.ApplicationCall
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
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.json.simple.JSONObject
import java.io.File

val client = HttpClient()
val debug = true

data class RequestBody(
    val isGroup: Boolean,
    val id: Int,
    val props: List<RequestBodyProperty>
)
data class RequestBodyProperty(
    val key: String,
    val value: Int
)

fun main(args: Array<String>) {
    val file = File(args[0])
    val fileLines = file.readLines()

    // fileLines[0] is the IP for the hue bridge
    // fileLines[1] is the user that is registered at the bridge
    val baseURL = "http://" + fileLines[0] + "/api/" + fileLines[1] + "/"

    val server = embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation){
            jackson {
            }
        }

        routing {
            post("/") {
                val reqBod = call.receive<RequestBody>()

                if (reqBod.props.isEmpty()){
                    call.respondText {
                        client.get{
                            url(baseURL +
                                    (if (reqBod.isGroup) "groups" else "lights") +
                                    (if (reqBod.id >= 0) "/" + reqBod.id else ""))
                        }
                    }
                } else {
                    val body = JSONObject()

                    reqBod.props.forEach{
                        when (it.key){
                            "power" -> body["on"] = it.value != 0
                            "hue" -> body["hue"] = it.value * 65535 / 360
                            "sat", "bri" -> body[it.key] = it.value * 254 / 100
                            "rst" -> {
                                body["hue"] = 8418
                                body["bri"] = 254
                                body["sat"] = 140
                            }
                        }
                    }

                    call.respondText {
                        statusUpdate(baseURL, body, if (reqBod.isGroup) "groups" else "lights", reqBod.id)
                    }
                }
            }
        }
    }

    server.start(wait = true)
}

suspend fun statusUpdate(baseURL: String, bodyObject: JSONObject, type: String, id: Int): String {
    var stateName = "state"

    if (type == "groups") {
        stateName = "action"
    }

    val fullURL = "$baseURL$type/$id/$stateName"

    if (debug) {
        println(bodyObject.toString())
    }

    return client.put {
        url(fullURL)
        body = bodyObject.toString()
    }
}
