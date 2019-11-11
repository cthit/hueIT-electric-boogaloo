import com.fasterxml.jackson.databind.ObjectMapper
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.stringType
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.client.HttpClient
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
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.event.Level

val client = HttpClient()
val mapper = ObjectMapper()



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
    val hueClient = HueClient(baseURL)

    val server = embeddedServer(Netty, port = config[selfPort]) {
        // Used for translating JSON-objects from frontend to data classes that Kotlin can understand.
        install(ContentNegotiation) {
            jackson {}
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
                index(hueClient, idMap, groupIdMap)
            }

            // Handles lists of requests.
            post("/list") {
                handleRequestList(hueClient, idMap, groupIdMap)
            }
        }
    }

    server.start(wait = true)
}

private suspend fun PipelineContext<Unit, ApplicationCall>.handleRequestList(
    hueClient: HueClient,
    idMap: List<Int?>,
    groupIdMap: List<Int?>
) {
    val reqBods = call.receive<RequestBodyList>()
    val responses = mutableListOf<String>()

    println(reqBods.toString())

    try {
        // Handle all requests and put the responses in a list.
        reqBods.requestBodyList.forEach {
            responses.add(hueClient.handleRequestBody(it, idMap, groupIdMap))
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

private suspend fun PipelineContext<Unit, ApplicationCall>.index(
    hueClient: HueClient,
    idMap: List<Int?>,
    groupIdMap: List<Int?>
) {
    val reqBod = call.receive<RequestBody>()
    println(reqBod.toString())
    try {
        val responseJSON =
            hueClient.handleRequestBody(reqBod, idMap, groupIdMap)
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
