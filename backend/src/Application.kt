import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.url
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.json.simple.JSONObject
import java.io.File

val client = HttpClient()
val debug = true

fun main(args: Array<String>) {

    val file = File(args[0])
    val fileLines = file.readLines()

    // fileLines[0] is the IP for the hue bridge
    // fileLines[1] is the user that is registered at the bridge
    val baseURL = "http://" + fileLines[0] + "/api/" + fileLines[1] + "/"

    val server = embeddedServer(Netty, port = 8080) {
        routing {

            route("/{type}") {
                // Returns the status of all lights/groups
                get("/all") {
                    val type = call.parameters["type"] + "s"

                    if (isValidType(type)) {
                        call.respondText {
                            client.get {
                                url(baseURL + type)
                            }
                        }
                    }
                }

                // Returns the stetus of the selected light/group
                route("/{number}") {
                    get {
                        val type = call.parameters["type"] + "s"
                        val number = call.parameters["number"]?.toInt()

                        if (isValidType(type)) {
                            call.respondText {
                                client.get {
                                    url(baseURL + type + "/" + number)
                                }
                            }
                        }
                    }


                    get("/power/{power}") {
                        val body = JSONObject()

                        val power = call.parameters["power"]

                        if (power.equals("on")) {
                            body.put("on", true)
                        } else if (power.equals("off")) {
                            body.put("on", false)
                        }

                        call.respondText {
                            attemptStatusUpdate(baseURL, body, call)
                        }
                    }

                    get("/color/{hue}/{sat}/{bri}"){
                        val body = JSONObject()

                        setColor(body, call)

                        call.respondText {
                            attemptStatusUpdate(baseURL, body, call)
                        }
                    }

                    get("/reset"){
                        val body = JSONObject()

                        body.set("hue", 8418)
                        body.set("bri", 254)
                        body.set("sat", 140)

                        call.respondText {
                            attemptStatusUpdate(baseURL, body, call)
                        }
                    }
                }
            }
        }
    }

    server.start(wait = true)
}

fun isValidType(type: String): Boolean {
    return type.equals("groups") || type.equals("lights")
}

fun setColor(body: JSONObject, call: ApplicationCall) {
    val hue = call.parameters["hue"]?.toDoubleOrNull()
    if (hue != null && hue >= 0 && hue <= 360){
        body.put("hue", (hue * 65535 / 360).toInt())
    } else {
        return
    }

    val sat = call.parameters["sat"]?.toDoubleOrNull()
    if (sat != null && sat >= 0 && sat <= 100){
        body.put("sat", (sat * 254 / 100).toInt())
    } else {
        body.remove("hue")
        return
    }

    val bri = call.parameters["bri"]?.toDoubleOrNull()
    if (bri != null && bri >= 0 && bri <= 100){
        body.put("bri", (bri * 254 / 100).toInt())
    } else {
        body.remove("hue")
        body.remove("sat")
    }
}

suspend fun attemptStatusUpdate(baseURL: String, bodyObject: JSONObject, call: ApplicationCall): String {
    val type = call.parameters["type"] + "s"
    val number = call.parameters["number"]

    if (isValidType(type)) {
        if (!number.isNullOrEmpty()) {
            val id = number.toInt()

            if (bodyObject.isNotEmpty()) {
                return statusUpdate(baseURL, bodyObject, type, id)
            }
            return "Invalid update!"
        }
        return "Invalid ID!"
    }
    return "Invalid type!"
}

suspend fun statusUpdate(baseURL: String, bodyObject: JSONObject, type: String, id: Int): String {
    var stateName = "state"

    if (type.equals("groups")) {
        stateName = "action"
    }

    val fullURL = baseURL + type + "/" + id + "/" + stateName

    if (debug) {
        println(bodyObject.toString())
    }

    return client.put {
        url(fullURL)
        body = bodyObject.toString()
    }
}
