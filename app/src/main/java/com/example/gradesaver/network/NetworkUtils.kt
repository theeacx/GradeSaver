package com.example.gradesaver.network

import android.util.Log
import com.example.gradesaver.dataClasses.OpenAIResponse
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.Thread.sleep
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.pow
import kotlin.random.Random

object NetworkUtils {

    private const val API_URL = "https://api.openai.com/v1/chat/completions"
    private const val API_KEY = "sk-proj-WAxdzXHMk4cfb8rZWFI3T3BlbkFJTUl5CIpikXsaazW4hztc"

    fun callOpenAiApi(prompt: String, callback: (String?) -> Unit) {
        Thread {
            var attempts = 0
            val maxAttempts = 5
            var success = false

            while (attempts < maxAttempts && !success) {
                try {
                    val url = URL(API_URL)
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("Authorization", "Bearer $API_KEY")
                    connection.doOutput = true

                    val messages = listOf(
                        mapOf("role" to "system", "content" to "You are a helpful assistant. Only respond to questions related to programming, cybernetics, math, statistics, and economics. If the question is not related to these topics, respond with 'Sorry, my purpose is to respond to questions related to programming, cybernetics, math, statistics, and economics.'"),
                        mapOf("role" to "user", "content" to prompt)
                    )
                    val request = mapOf("model" to "gpt-3.5-turbo", "messages" to messages)
                    val jsonRequest = Gson().toJson(request)

                    val outputStream: OutputStream = connection.outputStream
                    outputStream.write(jsonRequest.toByteArray())
                    outputStream.flush()
                    outputStream.close()

                    val responseCode = connection.responseCode
                    Log.d("NetworkUtils", "Response Code: $responseCode")

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = bufferedReader.use { it.readText() }
                        bufferedReader.close()

                        Log.d("NetworkUtils", "Response: $response")

                        val openAIResponse = Gson().fromJson(response, OpenAIResponse::class.java)
                        val responseText = openAIResponse.choices.firstOrNull()?.message?.content?.trim()
                        callback(responseText)
                        success = true
                    } else if (responseCode == 429) {
                        val errorStream = connection.errorStream
                        val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                        Log.e("NetworkUtils", "Rate Limit Error Response: $errorResponse")
                        attempts++
                        val backoffTime = 2.0.pow(attempts).toLong() * 1000 + Random.nextLong(1000)
                        sleep(backoffTime)
                    } else {
                        val errorStream = connection.errorStream
                        val errorResponse = errorStream?.bufferedReader()?.use { it.readText() }
                        Log.e("NetworkUtils", "Error Response: $errorResponse")
                        callback(null)
                        break
                    }
                    connection.disconnect()
                } catch (e: Exception) {
                    Log.e("NetworkUtils", "Exception: ${e.message}", e)
                    callback(null)
                    break
                }
            }
            if (!success) {
                callback("Exceeded maximum retry attempts")
            }
        }.start()
    }
}
