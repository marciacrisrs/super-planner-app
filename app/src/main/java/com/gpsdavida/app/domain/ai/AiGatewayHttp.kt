package com.superplanner.app.domain.ai

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

internal data class AiGatewayHttpResponse(val status: Int, val body: String)

internal fun postAiGateway(path: String, endpoint: String, requestId: String, timeoutMs: Int, body: String, schemaVersion: String? = null): AiGatewayHttpResponse {
    val connection = (URI("${endpoint.trim().trimEnd('/')}$path").toURL().openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 8_000
        readTimeout = timeoutMs
        doOutput = true
        setRequestProperty("Content-Type", "application/json; charset=utf-8")
        setRequestProperty("Accept", "application/json")
        setRequestProperty("Cache-Control", "no-store")
        setRequestProperty("X-Request-Id", requestId)
        schemaVersion?.let { setRequestProperty("X-AI-Schema-Version", it) }
    }
    return try {
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val responseBody = stream?.let { BufferedReader(InputStreamReader(it, Charsets.UTF_8)).use { reader -> reader.readText() } }.orEmpty()
        AiGatewayHttpResponse(status, responseBody)
    } finally {
        connection.disconnect()
    }
}
