class MyJSONObj(inStr: String) {
    val plains = HashMap<String, String>()
    var arrs = HashMap<String, ArrayList<String>>()
    var jsons = HashMap<String, MyJSONObj>()

    init {
        var jsonStr = inStr

        // Drop the curly brackets that define the confines of the JSON-object.
        jsonStr = jsonStr.drop(1).dropLast(1)

        while (jsonStr.isNotEmpty()) {
            // Drop the leading ".
            jsonStr = jsonStr.drop(1)

            // Everything before the next " is the name of the key in the JSON-object.
            val endOfKeyInd = jsonStr.indexOf('"')

            val tempKeyStorage = jsonStr.substring(0, endOfKeyInd)
            jsonStr = jsonStr.drop(endOfKeyInd + 2)

            // Drop all spaces before next character.
            while (jsonStr.first() == ' ') {
                jsonStr = jsonStr.drop(1)
            }

            // Handle the value based on what it is.
            // The kind of value is determined by the first character.
            when (jsonStr.first()) {
                // { is JSON-object.
                '{' -> {
                    val endOfJSONInd = endOfNextJSONInd(jsonStr)

                    // Parse JSON-object.
                    val JSONChildStr = jsonStr.substring(0, endOfJSONInd + 1)
                    val tempValStorage = MyJSONObj(JSONChildStr)
                    jsons[tempKeyStorage] = tempValStorage

                    jsonStr = jsonStr.drop(endOfJSONInd + 1)
                }
                // [ is array.
                '[' -> {
                    // Get array as string.
                    val endOfArrInd = jsonStr.indexOf(']')
                    val arrContents = jsonStr.substring(1, endOfArrInd)

                    // Split array-string on , and store as string-array.
                    val tempValStorage = ArrayList<String>(arrContents.split(", ", ","))
                    arrs[tempKeyStorage] = tempValStorage

                    jsonStr = jsonStr.drop(endOfArrInd + 1)
                }
                // " is string.
                '"' -> {
                    // Get and store string value.
                    jsonStr = jsonStr.drop(1)
                    val endOfValInd = jsonStr.indexOf('"')
                    val tempValStorage = jsonStr.substring(0, endOfValInd)
                    plains[tempKeyStorage] = tempValStorage

                    jsonStr = jsonStr.drop(endOfValInd + 2)
                }
                // Otherwise it's some other kind of "plain" value.
                else -> {
                    // Get and store value as string.
                    val endOfValInd = if (jsonStr.contains(',')) jsonStr.indexOf(',') else jsonStr.lastIndex + 1
                    val tempValStorage = jsonStr.substring(0, endOfValInd)
                    plains[tempKeyStorage] = tempValStorage

                    jsonStr = jsonStr.drop(endOfValInd + 1)
                }
            }

            jsonStr = jsonStr.dropWhile { it != '"' }
            if (!jsonStr.contains('"')) {
                jsonStr = ""
            }
        }
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Plain values:")
        for (k in plains.keys) {
            val v = plains[k]
            stringBuilder.append("\n$k: $v")
        }

        stringBuilder.append("\nArrays:")
        for (k in arrs.keys) {
            val v = arrs[k]
            stringBuilder.append("\n$k: $v")
        }

        stringBuilder.append("\nObjects:")
        for (k in jsons.keys) {
            val v = jsons[k].toString()
            stringBuilder.append("\n$k:{\n$v\n}")
        }

        return stringBuilder.toString()
    }

    companion object {
        fun endOfNextJSONInd(jsonStr: String): Int {
            var endOfJSONInd = jsonStr.indexOf('}')
            var startOfJSONInd = 0

            // When we have a { with an index higher than the current } then the index of the } is the end of the
            // JSON-object.
            while (jsonStr.substring(startOfJSONInd + 1).contains('{')) {
                startOfJSONInd = jsonStr.indexOf('{', startOfJSONInd + 1)
                if (startOfJSONInd > endOfJSONInd) {
                    break
                }
                endOfJSONInd = jsonStr.indexOf('}', endOfJSONInd + 1)
            }
            return endOfJSONInd
        }
    }
}

/*
{
    "success": {
        "/groups/2/action/on": false
    }
}
*/