package com.example.glucontrol

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

object ChatGPTApi {

    private const val API_KEY = "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"
    private const val URL = "https://api.openai.com/v1/chat/completions"

    fun sendMessage(message: String, callback: (String) -> Unit) {
        try {
            val client = OkHttpClient()

            val messagesArray = JSONArray()
            messagesArray.put(JSONObject().apply { put("role", "system"); put("content", "Eres un asistente útil y amigable.") })
            messagesArray.put(JSONObject().apply { put("role", "user"); put("content", message) })

            val jsonBody = JSONObject()
            jsonBody.put("model", "gpt-3.5-turbo")
            jsonBody.put("messages", messagesArray)

            val body = RequestBody.create(
                "application/json; charset=utf-8".toMediaType(),
                jsonBody.toString()
            )

            val request = Request.Builder()
                .url(URL)
                .header("Authorization", "Bearer $API_KEY")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback("Error de conexión: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        if (!response.isSuccessful) {
                            callback("Error HTTP: ${response.code} ${response.message}")
                            return
                        }

                        val bodyString = response.body?.string()
                        if (bodyString.isNullOrEmpty()) {
                            callback("Error: respuesta vacía")
                            return
                        }

                        val json = JSONObject(bodyString)
                        val choices = json.optJSONArray("choices")
                        if (choices == null || choices.length() == 0) {
                            val error = json.optJSONObject("error")?.optString("message")
                            callback(error ?: "Error: no hay respuesta")
                            return
                        }

                        val messageObj = choices.getJSONObject(0).optJSONObject("message")
                        val text = messageObj?.optString("content") ?: "Error: contenido vacío"
                        callback(text.trim())

                    } catch (e: Exception) {
                        callback("Error al procesar respuesta: ${e.message}")
                    }
                }
            })

        } catch (e: Exception) {
            callback("Error inesperado: ${e.message}")
        }
    }
}
