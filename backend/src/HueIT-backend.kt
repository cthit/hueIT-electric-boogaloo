import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.set

val client = HttpClient()
val mapper = ObjectMapper()
const val hueConst: Double = 65535.0 / 360
const val satConst: Double = 254.0 / 100
const val briConst: Double = 254.0 / 100

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ResponseList(
    val responses: List<ResponseBody>,
    val errors: List<ErrorBody>?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
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

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestBody(
    val isGroup: Boolean,
    val id: Int,
    val props: RequestBodyProperty?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RequestBodyProperty(
    val pwr: Boolean?,
    val hue: Double?,
    val sat: Double?,
    val bri: Double?,
    val rst: Boolean?
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class HueRequestBody(
    val on: Boolean?,
    val hue: Int?,
    val sat: Int?,
    val bri: Int?
)

fun main(args: Array<String>): Unit = startServer(args[0])

fun startServer(hueKey: String) {
    val config = ConfigurationProperties.fromResource("config.properties")

    // Fetch config values
    val selfPort = Key("server.port", intType)
    val hueHost = Key("huebridge.host", stringType)
    val hueIDMap = Key("hueidmap", stringType)
    val hueGroupIDMap = Key("huegroupidmap", stringType)

    // Fetch and parse list that converts frontend ids to hue ids for lamps.
    val mapString: String = config[hueIDMap].toString()
    val idMap: List<Int?> = mapString.split(", ").map { it.toIntOrNull() }
    val groupMapString = config[hueGroupIDMap].toString()
    val groupIdMap: List<Int?> = groupMapString.split(", ").map { it.toIntOrNull() }

    println("Id map: $idMap")
    println("Group ID map: $groupIdMap")

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

        install(CallLogging) {
            level = Level.INFO
        }

        routing {
            // Handles single requests.
            post("/") {
                val reqBod = call.receive<RequestBody>()

                println(reqBod.toString())

                try {
                    val responseJSON = handleRequestBody(reqBod, baseURL, idMap, groupIdMap)
                    val parsedJSON = handleResponseJSON(responseJSON, idMap, groupIdMap)

                    println(mapper.writeValueAsString(parsedJSON))

                    call.respondText {
                        mapper.writeValueAsString(parsedJSON)
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

                try {
                    // Handle all requests and put the responses in a list.
                    reqBods.requestBodyList.forEach {
                        responses.add(handleRequestBody(it, baseURL, idMap, groupIdMap))
                    }

                    val parsedJSON = handleResponseJSON(responses, idMap, groupIdMap)

                    println(parsedJSON.toString())

                    call.respondText {
                        mapper.writeValueAsString(parsedJSON)
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

fun handleResponseJSON(responseJSON: String, idMap: List<Int?>, groupIdMap: List<Int?>): ResponseList {
    val passList = LinkedList<String>()
    passList.add(responseJSON)
    return handleResponseJSON(passList, idMap, groupIdMap)
}

fun handleResponseJSON(responseJSONs: List<String>, idMap: List<Int?>, groupIdMap: List<Int?>): ResponseList {
    val jsonList = LinkedList<JsonNode>()

    // Extract response JSONs from the JSON-arrays in the list and store them in another list.
    responseJSONs.forEach {
        val responseObj = mapper.readTree(it)
        println("Full JSON: ${responseObj}")
        if (responseObj.isArray) {
            responseObj.forEachIndexed { index, jsonNode ->
                println("Index $index: $jsonNode")
                jsonList.add(jsonNode)
            }
        } else {
            jsonList.add(responseObj)
        }
    }

    // Translate the parsed JSON-objects into something more readable and then return them.
    return translateResponseJSON(jsonList, idMap, groupIdMap)
}

fun translateResponseJSON(hueJSONS: List<JsonNode>, idMap: List<Int?>, groupIdMap: List<Int?>): ResponseList {
    // Create one list for each lamp that is defined by idMap.
    // These lists will hold the successfully executed requests for their respective lamp.
    val jsonLampSucc = ArrayList<LinkedList<String>>(idMap.size)
    for (i in 0..idMap.size) {
        jsonLampSucc.add(LinkedList())
    }
    val jsonGroupSucc = ArrayList<LinkedList<String>>()
    for (i in 0..groupIdMap.size) {
        jsonGroupSucc.add(LinkedList())
    }

    val errors = LinkedList<ErrorBody>()

    println()
    hueJSONS.forEach {
        // Extract successful requests and store in list for later merging into data class.
        val succ = it.get("success")
        succ?.fields()?.forEach { entry ->
            val key = entry.key
            val value = entry.value.asText()

            println("Key: $key")

            val splitKey = key.split('/')
            val isGroup = splitKey[1] == "groups"
            val id = if (isGroup) groupIdMap.indexOf(splitKey[2].toInt()) else idMap.indexOf(splitKey[2].toInt())

            val reqData = "${splitKey[4]}:$value"

            if (isGroup) {
                if (id >= 0 && id < idMap.size) {
                    jsonGroupSucc[id].add(reqData)
                } else {
                    println("Attempting to add data '$reqData' for group with id ${splitKey[2]} but found none in the map.")
                }
            } else {
                if (id >= 0 && id < idMap.size) {
                    jsonLampSucc[id].add(reqData)
                } else {
                    println("Attempting to add data '$reqData' for lamp with id ${splitKey[2]} but found none in the map.")
                }
            }
        }

        // Convert error message into something more readable.
        val error = it.get("error")
        if (error != null) {
            assembleErrorJSON(
                error.get("type").asInt(),
                error.get("address").asText(),
                error.get("description").asText()
            )
        }
    }
    println()

    val responseBodies = LinkedList<ResponseBody>()

    // Assemble and store ResponseBodies.
    jsonLampSucc.forEachIndexed { i, it ->
        if (it.isNotEmpty()) {
            responseBodies.add(assembleResponseJSON(it, i, false))
        }
    }
    jsonGroupSucc.forEachIndexed { i, it ->
        if (it.isNotEmpty()) {
            responseBodies.add(assembleResponseJSON(it, i, true))
        }
    }

    // Assemble and return a ResponseList.
    return ResponseList(responseBodies, if (!errors.isEmpty()) errors else null)
}

fun assembleResponseJSON(attributes: List<String>, id: Int, isGroup: Boolean): ResponseBody {
    // Store all gathered key-value pairs in a HashMap for easy and simultaneous retrieval.
    val propMap = HashMap<String, String>()
    attributes.forEach {
        val pair = it.split(':')
        propMap[pair[0]] = pair[1]
    }

    // Assemble RequestBodyProperty from the previously created HashMap.
    // Any keys with no values in the map set their respective property in the data class to null.
    val props = if (propMap.isNotEmpty())
        RequestBodyProperty(
            propMap["on"]?.toBoolean(),
            propMap["hue"]?.toDouble(),
            propMap["sat"]?.toDouble(),
            propMap["bri"]?.toDouble(),
            null
        )
    else
        null

    // Assembles and returns the ResponseBody.
    return ResponseBody(isGroup, id, props)
}

fun assembleErrorJSON(type: Int, address: String, description: String): ErrorBody {
    val addressParts = address.split('/')
    val isGroup: Boolean = addressParts[1] == "groups"
    val id: Int = addressParts[2].toInt()
    return ErrorBody(type, isGroup, id, description)
}

suspend fun handleRequestBody(reqBod: RequestBody, baseURL: String, idMap: List<Int?>, groupIdMap: List<Int?>): String {
    val id: Int? = if (reqBod.isGroup) groupIdMap[reqBod.id] else idMap[reqBod.id]
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
            return "{\"error\": \"That lamp does not exist.\"}"
        }

        val props: RequestBodyProperty = reqBod.props

        val body: HueRequestBody = if (props.rst != null && props.rst) {
            HueRequestBody(
                props.pwr,
                8418,
                140,
                254
            )
        } else {
            HueRequestBody(
                props.pwr,
                (props.hue?.times(hueConst))?.toInt(),
                (props.sat?.times(satConst))?.toInt(),
                (props.bri?.times(briConst))?.toInt()
            )
        }

        return statusUpdate(baseURL, body, groupString, id)
    }
}

suspend fun statusUpdate(baseURL: String, bodyObject: HueRequestBody, type: String, id: Int): String {
    val stateName = if (type == "groups") "action" else "state"
    val fullURL = "$baseURL$type/$id/$stateName"

    println(mapper.writeValueAsString(bodyObject))

    // Send request to Philips Hue-bridge and return response.
    return client.put {
        url(fullURL)
        body = mapper.writeValueAsString(bodyObject)
    }
}
