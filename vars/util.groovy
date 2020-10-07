import groovy.json.JsonSlurperClassic

def parseJson(jsonString) {
    def slurper = new JsonSlurperClassic()
    def result = slurper.parseText(jsonString)
    slurper = null
    return result
}

/**
 * Property/method that retrieves the current folder name.
 * NOTE: Must have a node context to work
 * @return The name of the current folder as a string.
 */
def getFolderName() {
    def path = pwd()
    if (path.contains('@')) {
        path = path.substring(0, path.lastIndexOf('@'))
    }
    def array = path.replace('\\', '/').split("/")
    return array[array.length - 1];
}


/**
 * Internal NonCPS method for setting a node in an offline status.
 * @param  nodeName The node name (case sensitive)
 * @param  message  The offline message
 */
@NonCPS
def markNodeOfflineInternal(String nodeName, String message) {
    def node = Jenkins.instance.getNode(nodeName)
    if (null == node) { return false }

    computer = node.toComputer()
    if (null == computer) { return false }

    computer.setTemporarilyOffline(true)
    computer.doChangeOfflineCause(message)
    computer = null
    node = null
    return true
}

/**
 * Public CPS method to be called for setting a node offline. Utilizes
 * system/NonCPS method for setting the status.
 * @param  nodeName The node name (case sensitive)
 * @param  message  The offline message
 */
def markNodeOffline(String nodeName, String message) {
    def result = markNodeOfflineInternal(nodeName, message)
    if (!result) {
        throw new Exception("Could not mark $nodeName OFFLINE")
    }
    sh script: "echo Marked $nodeName OFFLINE", label: "$nodeName OFFLINE"
}

/**
 * Creates combinations from the key/value pairs of the provided map.
 * Example: [axis1: ['one', 'two'], axis2: ['a']] =>
            [[axis1: 'one', axis2: 'a'], [axis1: 'two', axis2: 'a']]
 * @param  map A map of key/value pairs.
 * @return     A list of maps. Each map is a specific combination of
 *             the provided map.
 */
List createComboList(Map map) {
    def combinations = []
    def vals = map.values().combinations()
    vals.each { c ->
        def tempMap = [:]
        def keys = map.keySet()
        for (int i = 0; i < keys.size(); ++i) {
            tempMap[keys[i]] = c[i]
        }
        combinations.add(tempMap)
    }
    return combinations
}

/**
 * Converts a map of key/value pairs into a list of
 * strings used for the 'withEnv' step
 * Example:         [MYVAR: 'potato', HAM: 'cheese', ...] =>
 *                  ["MYVAR=potato", "HAM=cheese", ...]
 * @param  map Map of key/value pairs.
 * @return     A list of strings under the format 'Key=Value' for 'withEnv' step.
 */
List mapToEnv(map) {
    def envList = []
    map.each { e ->
        envList.add("${e.getKey()}=${e.getValue()}")
    }
    return envList
}

/**
 * Converts a map of key value pairs into a formatted
 * string of the map's values to be used as a label.
 * @param  map        Map of key/value pairs.
 * @param  ignoreKeys A list of key-strings to exclude from the label.
 * @return A formatted label string.
 */
String mapToLabel(Map map, def ignoreKeys = null) {
    def list = []
    map.each { kv ->
        if (ignoreKeys?.contains(kv.key)) {
            continue
        }
        list.add("${kv.value}")
    }
    return list.join('-')
}

/**
 * Converts a list of env var assignment strings to a
 * map of key value pairs
 * Example:     ["MYVAR=potato", "HAM=cheese" ...] =>
                [MYVAR: 'potato', HAM: 'cheese', ...]
 * @param  environ      A list of env var assignment strings
 * @return A map of env var key value pairs
 */
Map envToMap(List environ) {
    def envMap = [:]
    environ.each { e ->
        def keyVal = e.split('=')
        envMap.put(keyVal[0], keyVal[1])
    }
    return envMap
}

/**
 * Iterates on the first map and token replaces each string value with the token/values
 * specified in teh second map.
 * Example:     map:    [YO: '${PHRASE}'],
 *              envMap: [PHRASE: 'dawg!'] =>
 *              result: [YO: 'dawg!']
 * @param  map      The map to modify/token-replace on
 * @param  envMap   The key/values token/strings to use fore replacement
 * @return A new map with tokens replaced in the string values
 */
Map envTokenReplaceMapStrings(Map map, Map envMap) {
    def modifiedMap = [:]
    map.keySet().each { k ->
        def value = map[k]
        if (!(value instanceof String)) { return }
        envMap.each { envVar ->
            value = value.replace("\$${envVar.key}", envVar.value)
        }
        modifiedMap[k] = value
    }
    return modifiedMap
}

/**
 * Pulic method/property for retrieving the username of the user
 * who triggered a build.
 * @return User ID string. E.G. tsizzle, auto, etc.
 */
def getBuilduser() {
  return getBuildUserInternal()
}

/**
 * Internal method to retrieve the user information.
 * If there is no user-information, we assume
 * it's an automated job and return 'auto'.
 *
 * NOTE: We don't put this in the 'call' body as it is
 * NonCPS.
 * @return User ID string. E.G. tabeyti
 */
@NonCPS
def getBuildUserInternal() {
  def userCause = currentBuild.rawBuild.getCause(Cause.UserIdCause)
  if (null != userCause) {
    return userCause.getUserId()
  }
  return 'auto'
}