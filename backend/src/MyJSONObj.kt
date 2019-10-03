class MyJSONObj(inStr: String) {
    val plains = HashMap<String, String>()
    var arrs = HashMap<String, ArrayList<String>>()
    var jsons = HashMap<String, MyJSONObj>()

    init {
        var jsonStr = inStr
        if (jsonStr.first() == '[') { // TODO: Handle JSONArrays properly
            jsonStr = jsonStr.drop(1).dropLast(1)
        }
        jsonStr = jsonStr.drop(1).dropLast(1)

        while (jsonStr.isNotEmpty()) {
            jsonStr = jsonStr.drop(1)
            val endOfKeyInd = jsonStr.indexOf('"')
            val tempKeyStorage = jsonStr.substring(0, endOfKeyInd)
            jsonStr = jsonStr.drop(endOfKeyInd + 2)

            while (jsonStr.first() == ' ') {
                jsonStr = jsonStr.drop(1)
            }

            when (jsonStr.first()) {
                '{' -> {
                    val endOfJSONInd = endOfNextJSONInd(jsonStr)

                    val JSONChildStr = jsonStr.substring(0, endOfJSONInd + 1)
                    println(JSONChildStr)
                    val tempValStorage = MyJSONObj(JSONChildStr)
                    jsons[tempKeyStorage] = tempValStorage

                    jsonStr = jsonStr.drop(endOfJSONInd + 1)
                }
                '[' -> {
                    val endOfArrInd = jsonStr.indexOf(']')
                    val arrContents = jsonStr.substring(1, endOfArrInd)

                    val tempValStorage = ArrayList<String>(arrContents.split(", ", ","))
                    arrs[tempKeyStorage] = tempValStorage

                    jsonStr = jsonStr.drop(endOfArrInd + 1)
                }
                else -> {
                    val endOfValInd = if (jsonStr.contains(',')) jsonStr.indexOf(',') else jsonStr.lastIndex + 1
                    val tempValStorage = jsonStr.substring(0, endOfValInd)
                    plains[tempKeyStorage] = tempValStorage

                    jsonStr = jsonStr.drop(endOfValInd + 1)
                }
            }

            if (jsonStr.contains('"')) {
                jsonStr = jsonStr.drop(jsonStr.indexOf('"'))
                println()
            } else {
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