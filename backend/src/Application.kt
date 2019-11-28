import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.application.Application
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
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.event.Level

val client = HttpClient()
val mapper = ObjectMapper()

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
fun Application.hueModule() {
    // Fetch config values
    val hueKey = environment.config.property("ktor.application.api_key").getString()
    val hueHost = environment.config.property("ktor.huebridge.host").getString()
    val idMap: List<Int?> = environment.config.property("ktor.huebridge.idmap").getList().map { it.toIntOrNull() }
    val groupIdMap: List<Int?> =
        environment.config.property("ktor.huebridge.groupidmap").getList().map { it.toIntOrNull() }

    println("Id map:       $idMap")
    println("Group ID map: $groupIdMap")

    val baseURL = "http://$hueHost/api/$hueKey/"
    val hueClient = HueClient(baseURL)

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
        get("/hello") {
            call.respondText { "HelloWorld" }
        }

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
