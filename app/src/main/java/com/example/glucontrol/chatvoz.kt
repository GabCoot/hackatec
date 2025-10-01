package com.example.glucontrol

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.Normalizer
import java.util.Locale

class chatvoz : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var micButton: FloatingActionButton
    private lateinit var chatAdapter: ChatAdapter
    private val mensajes = mutableListOf<Mensaje>()

    private var respuestas: List<Pair<List<String>, String>> = listOf()
    private var respuestaDefault: String = "No entendí lo que dijiste."

    companion object {
        private const val REQUEST_CODE_SPEECH = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatvoz)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Chat IA por Voz"
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        recyclerView = findViewById(R.id.chatRecyclerView)
        micButton = findViewById(R.id.micButton)

        chatAdapter = ChatAdapter(mensajes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        loadResponses()

        micButton.setOnClickListener {
            startVoiceRecognition()
        }
    }

    private fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla ahora...")
        }

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "El dispositivo no soporta reconocimiento de voz", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SPEECH && resultCode == Activity.RESULT_OK && data != null) {
            val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = result?.get(0) ?: ""

            if (!TextUtils.isEmpty(spokenText)) {
                addMessage(spokenText, isUser = true)

                val reply = getBotReply(spokenText)
                addMessage(reply, isUser = false)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        mensajes.add(Mensaje(text, isUser))
        chatAdapter.notifyItemInserted(mensajes.size - 1)
        recyclerView.post {
            recyclerView.smoothScrollToPosition(mensajes.size - 1)
        }
    }

    private fun normalizarTexto(texto: String): String {
        val temp = Normalizer.normalize(texto, Normalizer.Form.NFD)
        val sinAcentos = temp.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        val sinPuntuacion = sinAcentos.replace(Regex("[^\\p{L}\\p{Nd}\\s]+"), "")
        return sinPuntuacion.lowercase(Locale.getDefault()).trim()
    }

    private fun getBotReply(input: String): String {
        val mensajeNormalizado = normalizarTexto(input)
        for ((keywords, response) in respuestas) {
            if (keywords.any { keyword -> mensajeNormalizado.contains(normalizarTexto(keyword)) }) {
                return response
            }
        }
        return respuestaDefault
    }

    private fun loadResponses() {
        try {
            val inputStream = assets.open("respuestas.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()

            val json = String(buffer, Charsets.UTF_8)
            val jsonArray = org.json.JSONArray(json)

            val listaRespuestas = mutableListOf<Pair<List<String>, String>>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.getJSONObject(i)

                val keys = if (item.has("keywords")) item.getJSONArray("keywords")
                else item.getJSONArray("mensaje") // Soporte para campo alternativo

                val palabrasClave = mutableListOf<String>()
                for (j in 0 until keys.length()) {
                    palabrasClave.add(normalizarTexto(keys.getString(j)))
                }

                val respuesta = item.getString("response")

                if (palabrasClave.contains("default")) {
                    respuestaDefault = respuesta
                } else {
                    listaRespuestas.add(Pair(palabrasClave, respuesta))
                }
            }

            respuestas = listaRespuestas

        } catch (e: Exception) {
            e.printStackTrace()
            respuestaDefault = "Error al cargar respuestas."
            Log.e("RESPONSES", "Error al cargar JSON: ${e.message}")
        }
    }
}
