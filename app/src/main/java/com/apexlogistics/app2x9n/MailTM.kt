package com.apexlogistics.app2x9n

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object MailTm {
    private const val BASE = "https://api.mail.tm"

    data class Inbox(val address: String, val password: String, val token: String)

    private fun post(url: String, body: String, bearer: String? = null): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.requestMethod = "POST"
        c.setRequestProperty("Content-Type", "application/json")
        if (bearer != null) c.setRequestProperty("Authorization", "Bearer $bearer")
        c.doOutput = true
        c.connectTimeout = 20000
        c.readTimeout = 20000
        c.outputStream.use { it.write(body.toByteArray()) }
        val code = c.responseCode
        val stream = if (code in 200..299) c.inputStream else c.errorStream
        val text = stream?.bufferedReader()?.readText() ?: ""
        if (code !in 200..299) throw RuntimeException("HTTP $code: $text")
        return text
    }

    private fun get(url: String, bearer: String? = null): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.requestMethod = "GET"
        if (bearer != null) c.setRequestProperty("Authorization", "Bearer $bearer")
        c.connectTimeout = 20000
        c.readTimeout = 20000
        val code = c.responseCode
        val stream = if (code in 200..299) c.inputStream else c.errorStream
        val text = stream?.bufferedReader()?.readText() ?: ""
        if (code !in 200..299) throw RuntimeException("HTTP $code: $text")
        return text
    }

    fun createInbox(): Inbox {
        val domainsJson = get("$BASE/domains")
        val domains = JSONObject(domainsJson).getJSONArray("hydra:member")
        val domain = JSONObject(domains.getString(0)).getString("domain")
        val local = (1..14).map { ('a'..'z').random() }.joinToString("")
        val address = "$local@$domain"
        val pw = (1..18).map {
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".random()
        }.joinToString("")

        post("$BASE/accounts",
            """{"address":"$address","password":"$pw"}""")

        val tokenJson = post("$BASE/token",
            """{"address":"$address","password":"$pw"}""")
        val token = JSONObject(tokenJson).getString("token")
        return Inbox(address, pw, token)
    }

    fun waitForVerifyLink(inbox: Inbox, timeoutMs: Long = 240_000): String? {
        val re = Regex("""https://click\.discord\.com/ls/click\?upn=[A-Za-z0-9._%\-]+""")
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            try {
                val listJson = get("$BASE/messages", inbox.token)
                val msgs = JSONObject(listJson).getJSONArray("hydra:member")
                for (i in 0 until msgs.length()) {
                    val id = JSONObject(msgs.getString(i)).getString("id")
                    val full = get("$BASE/messages/$id", inbox.token)
                    val body = JSONObject(full).toString()
                    val hit = re.find(body)
                    if (hit != null) return hit.value
                }
            } catch (_: Exception) {}
            Thread.sleep(5000)
        }
        return null
    }
}
