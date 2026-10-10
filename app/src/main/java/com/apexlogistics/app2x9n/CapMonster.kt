package com.apexlogistics.app2x9n

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object CapMonster {
    private const val CREATE = "https://api.capmonster.cloud/createTask"
    private const val RESULT = "https://api.capmonster.cloud/getTaskResult"

    private fun postJson(url: String, body: String): String {
        val c = URL(url).openConnection() as HttpURLConnection
        c.requestMethod = "POST"
        c.setRequestProperty("Content-Type", "application/json")
        c.doOutput = true
        c.connectTimeout = 30000
        c.readTimeout = 30000
        c.outputStream.use { it.write(body.toByteArray()) }
        return c.inputStream.bufferedReader().readText()
    }

    fun solveHcaptcha(key: String, pageUrl: String, sitekey: String): String? {
        val createBody = """
            {"clientKey":"$key","task":{
                "type":"HCaptchaTaskProxyless",
                "websiteURL":"$pageUrl",
                "websiteKey":"$sitekey"
            }}
        """.trimIndent()

        val createResp = JSONObject(postJson(CREATE, createBody))
        if (createResp.getInt("errorId") != 0) {
            throw RuntimeException("CapMonster: " + createResp.toString())
        }
        val taskId = createResp.getInt("taskId")

        val deadline = System.currentTimeMillis() + 180_000
        while (System.currentTimeMillis() < deadline) {
            Thread.sleep(5000)
            val body = """{"clientKey":"$key","taskId":$taskId}"""
            val r = JSONObject(postJson(RESULT, body))
            if (r.optString("status") == "ready") {
                return r.getJSONObject("solution")
                    .getString("gRecaptchaResponse")
            }
            if (r.optInt("errorId", 0) != 0) {
                throw RuntimeException("CapMonster result: $r")
            }
        }
        return null
    }
}
