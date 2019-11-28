package test


import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import hueModule
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals


class ApplicationTest {
    companion object {
        val wireMockServer = WireMockServer(wireMockConfig().port(8089))
    }

    @Before
    fun setupWireMock() {
        if (!wireMockServer.isRunning) {
            println("Running false")
            wireMockServer.start()
        }

        wireMockServer.resetAll()
    }

    @Test
    fun testHello() {
        withTestApplication({ hueModule() }) {
            handleRequest(HttpMethod.Get, "/hello").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("HelloWorld", response.content)
            }
        }
    }

    @Test
    fun testGetStatusCommand() {
        wireMockServer.stubFor(
            get(urlEqualTo("/api/TEST_KEY/lights/13"))
                .willReturn(
                    okJson(
                        "{\n" +
                            "\t\"state\": {\n" +
                            "\t\t\"hue\": 50000,\n" +
                            "\t\t\"on\": true,\n" +
                            "\t\t\"effect\": \"none\",\n" +
                            "\t\t\"alert\": \"none\",\n" +
                            "\t\t\"bri\": 200,\n" +
                            "\t\t\"sat\": 200,\n" +
                            "\t\t\"ct\": 500,\n" +
                            "\t\t\"xy\": [0.5, 0.5],\n" +
                            "\t\t\"reachable\": true,\n" +
                            "\t\t\"colormode\": \"hs\"\n" +
                            "\t},\n" +
                            "\t\"type\": \"Living Colors\",\n" +
                            "\t\"name\": \"LC 1\",\n" +
                            "\t\"modelid\": \"LC0015\",\n" +
                            "\t\"swversion\": \"1.0.3\"\n" +
                            "}"
                    )
                )
        )

        withTestApplication({ hueModule() }) {
            handleRequest(HttpMethod.Post, "/") {
                addHeader("Content-Type", "application/json")
                setBody(
                    "{" +
                        " \"isGroup\": false,\n" +
                        "    \"id\": 1\n" +
                        "}"
                )
            }.apply {
                println(response.status())
                println(response.content)
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }


    @Test
    fun testSingleCommand() {
        wireMockServer.stubFor(
            put(urlEqualTo("/api/TEST_KEY/lights/13/state"))
                .willReturn(
                    okJson(
                        "[" +
                            "{\"success\":{\"/lights/13/state/on\":true}}" +
                            "]"
                    )
                )
        )

        withTestApplication({ hueModule() }) {
            handleRequest(HttpMethod.Post, "/") {
                addHeader("Content-Type", "application/json")
                setBody(
                    "{" +
                        " \"isGroup\": false," +
                        "    \"id\": 1," +
                        " \"props\": { \"pwr\": true }" +
                        "}"
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals(
                    "{\"responses\":[{\"id\":1,\"updatedProps\":{\"pwr\":true},\"group\":false}]}",
                    response.content
                )
            }
        }
    }


}
