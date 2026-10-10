package com.apexlogistics.app2x9n

object Generator {
    private val ADJ = listOf(
        "swift", "quiet", "crimson", "hollow", "amber", "vivid", "stormy",
        "lucid", "feral", "mellow", "iron", "velvet", "arctic", "ember",
        "static", "glass", "dusk", "raven", "misty", "solar"
    )
    private val NOUN = listOf(
        "harbor", "circuit", "drift", "signal", "vector", "cipher",
        "orbit", "razor", "haze", "relay", "flint", "onyx", "quartz",
        "nomad", "pilot", "beacon", "forge", "ridge", "vault"
    )
    private val CHARS =
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#\$%^&*"

    fun username(): String {
        for (i in 0 until 50) {
            val a = ADJ.random()
            val n = NOUN.random()
            val sep = listOf(".", "_", "").random()
            val d = (100..9999).random().toString()
            val name = "$a$sep$n$d".lowercase().take(32)
            if (valid(name)) return name
        }
        return "user" + (100000..999999).random()
    }

    private fun valid(s: String): Boolean {
        if (s.length < 3 || s.length > 32) return false
        if (!s.matches(Regex("[a-z0-9._]+"))) return false
        if (s.startsWith(".") || s.startsWith("_")) return false
        if (s.endsWith(".") || s.endsWith("_")) return false
        if (s.contains("__") || s.contains("..")) return false
        if (listOf("discord", "everyone", "here", "nitro").any { s.contains(it) }) return false
        return true
    }

    fun password(len: Int = 18): String {
        val sb = StringBuilder()
        repeat(len) { sb.append(CHARS.random()) }
        return sb.toString()
    }

    fun dob(): Triple<Int, Int, Int> =
        Triple((1..12).random(), (1..28).random(), (1988..2001).random())
}
