import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.url

const val HUE: Double = 65535.0 / 360
const val SATURATION: Double = 254.0 / 100
const val BRIGHTNESS: Double = 254.0 / 100

class HueClient(private val baseUrl: String) {

    suspend fun handleRequestBody(reqBod: RequestBody, idMap: List<Int?>, groupIdMap: List<Int?>): String {
        val id: Int? = if (reqBod.isGroup) groupIdMap[reqBod.id] else idMap[reqBod.id]
        val groupString: String = if (reqBod.isGroup) "groups" else "lights"

        if (reqBod.props == null) {
            // Get the status of the given group(s) or lamp(s) if the RequestBody does not have any properties.
            return client.get {
                url(
                    baseUrl +
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
                    (props.hue?.times(HUE))?.toInt(),
                    (props.sat?.times(SATURATION))?.toInt(),
                    (props.bri?.times(BRIGHTNESS))?.toInt()
                )
            }

            return statusUpdate(body, groupString, id)
        }
    }

    private suspend fun statusUpdate(bodyObject: HueRequestBody, type: String, id: Int): String {
        val stateName = if (type == "groups") "action" else "state"
        val fullURL = "$baseUrl$type/$id/$stateName"

        println(mapper.writeValueAsString(bodyObject))

        // Send request to Philips Hue-bridge and return response.
        return client.put {
            url(fullURL)
            body = mapper.writeValueAsString(bodyObject)
        }
    }
}