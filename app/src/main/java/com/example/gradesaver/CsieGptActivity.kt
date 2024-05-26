package com.example.gradesaver

import android.os.Bundle
import android.view.Gravity
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.gradesaver.network.NetworkUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CsieGptActivity : AppCompatActivity() {

    private lateinit var messageContainer: LinearLayout
    private lateinit var editTextMessage: EditText
    private lateinit var sendButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csie_gpt)

        messageContainer = findViewById(R.id.messageContainer)
        editTextMessage = findViewById(R.id.editTextMessage)
        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener { sendMessage() }

        // Initial welcome message from GPT
        addMessageToContainer("Hi! What can I help you with today?", false)
    }

    private fun sendMessage() {
        val message = editTextMessage.text.toString().trim()
        if (message.isNotEmpty()) {
            addMessageToContainer(message, true)
            editTextMessage.setText("")
            handleUserMessage(message)
        }
    }

    private fun handleUserMessage(message: String) {
        NetworkUtils.callOpenAiApi(message) { responseText ->
            runOnUiThread {
                responseText?.let {
                    addMessageToContainer(it, false)
                } ?: run {
                    addMessageToContainer("Failed to get a response from GPT.", false)
                }
            }
        }
    }

    private fun addMessageToContainer(message: String, isUser: Boolean) {
        val messageView = TextView(this).apply {
            text = message
            textSize = 16f
            setPadding(16, 10, 16, 10)
            background = ContextCompat.getDrawable(
                this@CsieGptActivity, if (isUser) R.drawable.user_bubble_background else R.drawable.gpt_bubble_background)
            setTextColor(ContextCompat.getColor(context, if (isUser) R.color.purple else R.color.white))
        }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            gravity = if (isUser) Gravity.END else Gravity.START
            topMargin = 8
            bottomMargin = 8
        }
        messageView.layoutParams = params
        messageContainer.addView(messageView)
    }
}
