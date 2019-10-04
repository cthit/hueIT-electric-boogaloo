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

fun main() {
//    val myjson = MyJSONObj("{\"test_val\":1,\"test_arr1\":[1,2,3],\"test_arr2\":[1, 2, 3],\"success\":{\"/groups/2/action/on\":false}}")

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

                try {
                    val responseJSON = handleRequestBody(reqBod, baseURL, idMap)
                    val parsedJSON = handleResponseJSON(responseJSON, idMap)

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

            post("/list") {
                val reqBods = call.receive<RequestBodyList>()
                val responses = ArrayList<String>()

                var responseJSON = ""

                try {
                    reqBods.requestBodyList.forEach {
                        responses.add(handleRequestBody(it, baseURL, idMap))
                    }

                    responses.forEach {
                        if (responses.isEmpty()) {
                            responseJSON = it
                        } else {
                            responseJSON = "${responseJSON.dropLast(1)},${it.drop(1)}"
                        }
                    }

                    val parsedJSON = handleResponseJSON(responseJSON, idMap)

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
    var jsonStr = responseJSON.drop(1).dropLast(1)
    val jsonList = LinkedList<MyJSONObj>()

    while (jsonStr.isNotEmpty()) {
        val endOfJSONInd = MyJSONObj.endOfNextJSONInd(jsonStr)
        jsonList.add(MyJSONObj(jsonStr.substring(0, endOfJSONInd + 1)))
        jsonStr = jsonStr.drop(endOfJSONInd + 1)

        jsonStr = jsonStr.dropWhile { it != '{' }
    }

    return translateResponseJSON(jsonList, idMap)
}

fun translateResponseJSON(hueJSONS: List<MyJSONObj>, idMap: List<Int?>): ResponseList {
    val jsonSucc = ArrayList<LinkedList<String>>(idMap.size)
    for (i in 0..idMap.size) {
        jsonSucc.add(LinkedList())
    }

    val errors = LinkedList<ErrorBody>()

    hueJSONS.forEach {
        val succ: MyJSONObj? = it.jsons["success"]
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

    for (i in 0..idMap.size) {
        if (jsonSucc[i].isNotEmpty()) {
            responseBodies.add(assembleResponseJSON(jsonSucc[i], i))
        }
    }

    return ResponseList(responseBodies, if (!errors.isEmpty()) errors else null)
}

fun assembleResponseJSON(attributes: List<String>, id: Int): ResponseBody {
    val propMap = HashMap<String, String>()
    attributes.forEach {
        val pair = it.split(':')
        propMap[pair[0]] = pair[1]
    }
    val hasProps = propMap.isNotEmpty()
    val props = RequestBodyProperty(
        propMap["on"]?.toBoolean(),
        propMap["hue"]?.toDouble(),
        propMap["sat"]?.toDouble(),
        propMap["bri"]?.toDouble(),
        null
    )
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
