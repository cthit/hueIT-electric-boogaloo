class MyJSONObj(var JSONstr: String) {
    val plains = HashMap<String, String>()
    var arrs = HashMap<String, ArrayList<String>>()
    var jsons = HashMap<String, MyJSONObj>()

    init {
        JSONstr = JSONstr.drop(1).dropLast(1)
        while (JSONstr.length > 0) {
            JSONstr = JSONstr.drop(1)
            val endOfKeyInd = JSONstr.indexOf('"')
            val tempKeyStorage = JSONstr.substring(0, endOfKeyInd)
            JSONstr = JSONstr.drop(endOfKeyInd + 3)
            println("Key: $tempKeyStorage")

            val firstValChar = JSONstr.first()
            when (firstValChar) {
                '{' -> {
                    var endOfJSONInd = JSONstr.indexOf('}')
                    var startOfJSONInd = 0

                    while (JSONstr.substring(startOfJSONInd + 1).contains('{')) {
                        startOfJSONInd = JSONstr.indexOf('{', startOfJSONInd + 1)
                        if (startOfJSONInd > endOfJSONInd) {
                            break
                        }
                        endOfJSONInd = JSONstr.indexOf('}', endOfJSONInd + 1)
                    }

                    val tempValStorage = MyJSONObj(JSONstr.substring(0, endOfJSONInd + 1))
                    jsons[tempKeyStorage] = tempValStorage

                    JSONstr.drop(endOfJSONInd + 1)

                    println("Payload: $tempValStorage")
                }
                '[' -> {
                    val endOfArrInd = JSONstr.indexOf(']')
                    val arrContents = JSONstr.substring(1, endOfArrInd)

                    val tempValStorage = ArrayList<String>(arrContents.split(", ", ","))
                    arrs[tempKeyStorage] = tempValStorage

                    JSONstr = JSONstr.drop(endOfArrInd + 1)

                    println("Payload: $tempValStorage")
                }
                else -> {
                    val endOfValInd = if (JSONstr.contains(',')) JSONstr.indexOf(',') else JSONstr.lastIndex
                    val tempValStorage = JSONstr.substring(0, endOfValInd)
                    plains[tempKeyStorage] = tempValStorage

                    JSONstr = JSONstr.drop(endOfValInd + 1)

                    println("Payload: $tempValStorage")
                }
            }

            if (JSONstr.contains('"')) {
                JSONstr = JSONstr.drop(JSONstr.indexOf('"'))
                println()
            } else {
                JSONstr = ""
            }
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