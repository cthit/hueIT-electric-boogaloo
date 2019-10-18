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
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.json.simple.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

val client = HttpClient()
val debug = false

data class ResponseList(
    val responses: List<ResponseBody>,
    val errors: List<ErrorBody>?
)

data class ResponseBody(
    val isGroup: Boolean,
    val id: Int,
    val updatedProps: RequestBodyProperty?
)

data class ErrorBody(
    val type: Int,
    val isGroup: Boolean,
    val id: Int,
    val description: String
)

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

fun startServer(hueKey: String) {
//    val myjson = MyJSONObj("{\"test_val\":1,\"test_arr1\":[1,2,3],\"test_arr2\":[1, 2, 3],\"success\":{\"/groups/2/action/on\":false}}")

    val config = ConfigurationProperties.fromResource("config.properties")

    // Fetch config values
    val selfPort = Key("server.port", intType)
    val hueHost = Key("huebridge.host", stringType)
    val hueIDMap = Key("hueidmap", stringType)

    // Fetch and parse list that converts frontend ids to hue ids for lamps.
    val mapString: String = config[hueIDMap].toString()
    val idMap: List<Int?> = mapString.split(", ").map { it.toIntOrNull() }

    println(idMap)

    val baseURL = "http://" + config[hueHost] + "/api/" + hueKey + "/"

    val server = embeddedServer(Netty, port = config[selfPort]) {
        // Used for translating JSON-objects from frontend to data classes that Kotlin can understand.
        install(ContentNegotiation) {
            jackson {
            }
        }

        // Allows any host to use the backend with POST-requests.
        install(CORS) {
            method(HttpMethod.Post)
            anyHost()
        }

        routing {
            // Handles single requests.
            post("/") {
                val reqBod = call.receive<RequestBody>()

                println(reqBod.toString())

                try {
                    val responseJSON = handleRequestBody(reqBod, baseURL, idMap)
                    val parsedJSON = handleResponseJSON(responseJSON, idMap)

                    println(parsedJSON.toString())

                    call.respondText {
                        parsedJSON.toString()
                    }
                } catch (e: Exception) {
                    println(e.toString())

                    call.respondText {
                        e.toString()
                    }
                }
            }

            // Handles lists of requests.
            post("/list") {
                val reqBods = call.receive<RequestBodyList>()
                val responses = ArrayList<String>()

                println(reqBods.toString())

                var responseJSON = ""

                try {
                    // Handle all requests and put the responses in a list.
                    reqBods.requestBodyList.forEach {
                        responses.add(handleRequestBody(it, baseURL, idMap))
                    }

                    // Merge all response JSON-objects into one.
                    responses.forEach {
                        responseJSON = if (responses.isEmpty()) it else "${responseJSON.dropLast(1)},${it.drop(1)}"
                    }

                    val parsedJSON = handleResponseJSON(responseJSON, idMap)

                    println(parsedJSON.toString())

                    call.respondText {
                        parsedJSON.toString()
                    }
                } catch (e: Exception) {
                    println(e.toString())

                    call.respondText {
                        e.toString()
                    }
                }

            }
        }
    }

    server.start(wait = true)
}

fun handleResponseJSON(responseJSON: String, idMap: List<Int?>): ResponseList {
    // Remove wrapping square brackets from JSON-array-string.
    var jsonStr = responseJSON.drop(1).dropLast(1)

    val jsonList = LinkedList<MyJSONObj>()

    // Sends all found JSON-objects to the parser and then appends the returned object to a list.
    while (jsonStr.isNotEmpty()) {
        val endOfJSONInd = MyJSONObj.endOfNextJSONInd(jsonStr)
        jsonList.add(MyJSONObj(jsonStr.substring(0, endOfJSONInd + 1)))
        jsonStr = jsonStr.drop(endOfJSONInd + 1)

        jsonStr = jsonStr.dropWhile { it != '{' }
    }

    // Translate the parsed JSON-objects into something more readable and then return them.
    return translateResponseJSON(jsonList, idMap)
}

fun translateResponseJSON(hueJSONS: List<MyJSONObj>, idMap: List<Int?>): ResponseList {
    // Create one list for each lamp that is defined by idMap.
    // These lists will hold the successfully executed requests for their respective lamp.
    val jsonSucc = ArrayList<LinkedList<String>>(idMap.size)
    for (i in 0..idMap.size) {
        jsonSucc.add(LinkedList())
    }

    val errors = LinkedList<ErrorBody>()

    hueJSONS.forEach {
        val succ: MyJSONObj? = it.jsons["success"]
        // Extract and store the keys and values for each successfully executed request and store them in their
        // corresponding lists.
        succ?.plains?.forEach { k, v ->
            val keyVals = k.split('/')
            if (keyVals[1] == "lights") {
                val id = idMap.indexOf(keyVals[2].toInt())
                if (keyVals[3] == "state") {
                    val key = keyVals[4]
                    jsonSucc[id].add("$key:$v")
                }
            }
        }

        val error: MyJSONObj? = it.jsons["error"]
        // Create ErrorBodies for each error and store them.
        if (error != null) {
            val plains = error.plains
            errors.add(
                assembleErrorJSON(
                    plains["type"]!!.toInt(),
                    plains["address"]!!,
                    plains["description"]!!
                )
            )
        }
    }

    val responseBodies = LinkedList<ResponseBody>()

    // Assemble and store ResponseBodies.
    for (i in 0..idMap.size) {
        if (jsonSucc[i].isNotEmpty()) {
            responseBodies.add(assembleResponseJSON(jsonSucc[i], i))
        }
    }

    // Assemble and return a ResponseList.
    return ResponseList(responseBodies, if (!errors.isEmpty()) errors else null)
}

fun assembleResponseJSON(attributes: List<String>, id: Int): ResponseBody {
    // Store all gathered key-value pairs in a HashMap for easy and simultaneous retrieval.
    val propMap = HashMap<String, String>()
    attributes.forEach {
        val pair = it.split(':')
        propMap[pair[0]] = pair[1]
    }

    val hasProps = propMap.isNotEmpty()

    // Assemble RequestBodyProperty from the previously created HashMap.
    // Any keys with no values in the map set their respective property in the data class to null.
    val props = RequestBodyProperty(
        propMap["on"]?.toBoolean(),
        propMap["hue"]?.toDouble(),
        propMap["sat"]?.toDouble(),
        propMap["bri"]?.toDouble(),
        null
    )

    // Assembles and returns the ResponseBody. If the HashMap is empty the RequestBodyProperty is replaced with null.
    return ResponseBody(false, id, if (hasProps) props else null)
}

fun assembleErrorJSON(type: Int, address: String, description: String): ErrorBody {
    val addressParts = address.split('/')
    val isGroup: Boolean = addressParts[1] == "groups"
    val id: Int = addressParts[2].toInt()
    return ErrorBody(type, isGroup, id, description)
}

suspend fun handleRequestBody(reqBod: RequestBody, baseURL: String, idMap: List<Int?>): String {
    val id: Int? = if (reqBod.isGroup) reqBod.id else idMap[reqBod.id]
    val groupString: String = if (reqBod.isGroup) "groups" else "lights"

    if (reqBod.props == null) {
        // Get the status of the given group(s) or lamp(s) if the RequestBody does not have any properties.
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

        // Assemble a JSON-object with all requested field and correctly scaled values.
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

        return statusUpdate(baseURL, body, groupString, id)
    }
}

suspend fun statusUpdate(baseURL: String, bodyObject: JSONObject, type: String, id: Int): String {
    val stateName = if (type == "groups") "action" else "state"
    val fullURL = "$baseURL$type/$id/$stateName"

    if (debug) {
        println(bodyObject.toString())
    }

    // Send request to Philips Hue-bridge and return response.
    return client.put {
        url(fullURL)
        body = bodyObject.toString()
    }
}
