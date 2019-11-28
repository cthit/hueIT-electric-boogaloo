import com.fasterxml.jackson.databind.JsonNode

fun handleResponseJSON(responseJSON: String, idMap: List<Int?>, groupIdMap: List<Int?>): ResponseList {
    return handleResponseJSON(listOf(responseJSON), idMap, groupIdMap)
}

fun handleResponseJSON(responseJSONs: List<String>, idMap: List<Int?>, groupIdMap: List<Int?>): ResponseList {
    val jsonList = mutableListOf<JsonNode>()

    // Extract response JSONs from the JSON-arrays in the list and store them in another list.
    responseJSONs.forEach {
        val responseObj = mapper.readTree(it)
        println("Full JSON: $responseObj")
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
    val jsonLampSucc = mutableListOf<MutableList<String>>()
    for (i in 0..idMap.size) {
        jsonLampSucc.add(mutableListOf())
    }
    val jsonGroupSucc = mutableListOf<MutableList<String>>()
    for (i in 0..groupIdMap.size) {
        jsonGroupSucc.add(mutableListOf())
    }

    val errors = mutableListOf<ErrorBody>()

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

    val responseBodies = mutableListOf<ResponseBody>()

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
    return ResponseList(responseBodies, if (errors.isNotEmpty()) errors else null)
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
            propMap["hue"]?.toDouble()?.div(HUE),
            propMap["sat"]?.toDouble()?.div(SATURATION),
            propMap["bri"]?.toDouble()?.div(BRIGHTNESS),
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
