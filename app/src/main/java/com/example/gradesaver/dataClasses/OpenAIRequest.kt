package com.example.gradesaver.dataClasses

data class OpenAIRequest(
    val prompt: String,
    val max_tokens: Int = 150,
    val temperature: Double = 0.7
)
