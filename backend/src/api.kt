import com.fasterxml.jackson.annotation.JsonInclude


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