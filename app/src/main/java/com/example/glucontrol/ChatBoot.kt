package com.example.glucontrol

import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import android.speech.tts.TextToSpeech
import java.util.Locale

class ChatBoot : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private val mensajes = mutableListOf<Mensaje>()

    private lateinit var tts: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatboot)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Optidia Chat IA"
        toolbar.setNavigationOnClickListener { onBackPressed() }

        recyclerView = findViewById(R.id.chatRecyclerView)
        inputEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        chatAdapter = ChatAdapter(mensajes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        // Inicializar TTS
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                tts.language = Locale("es", "MX") // Español México
            }
        }

        sendButton.setOnClickListener {
            val input = inputEditText.text.toString().trim()
            if (!TextUtils.isEmpty(input)) {
                addMessage(input, true)
                inputEditText.text.clear()

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                // Llamada a la API
                ChatGPTApi.sendMessage(input) { response ->
                    runOnUiThread {
                        addMessage(response, false)
                        recyclerView.scrollToPosition(mensajes.size - 1)

                        // Leer la respuesta en voz alta
                        tts.speak(response, TextToSpeech.QUEUE_FLUSH, null, null)
                    }
                }
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        chatAdapter.addMessage(Mensaje(text, isUser))
        recyclerView.post { recyclerView.scrollToPosition(mensajes.size - 1) }
    }

    override fun onDestroy() {

        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}

