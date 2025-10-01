package com.example.glucontrol

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.appbar.MaterialToolbar
import org.json.JSONArray
import java.text.Normalizer

class ChatBoot : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var inputEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatAdapter: ChatAdapter
    private val mensajes = mutableListOf<Mensaje>()

    private var respuestas: List<Pair<List<String>, String>> = listOf()
    private var respuestaDefault: String = "No entendí lo que dijiste."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatboot)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Optidia Chat IA"
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        recyclerView = findViewById(R.id.chatRecyclerView)
        inputEditText = findViewById(R.id.messageEditText)
        sendButton = findViewById(R.id.sendButton)

        loadResponses()

        chatAdapter = ChatAdapter(mensajes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        sendButton.setOnClickListener {
            val input = inputEditText.text.toString().trim()
            if (!TextUtils.isEmpty(input)) {
                addMessage(input, true)

                val reply = getBotReply(input)
                addMessage(reply, false)

                inputEditText.text.clear()

                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)

                // Toast.makeText(this, "Mensaje enviado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Escribe algo...", Toast.LENGTH_SHORT).show()
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
        return sinPuntuacion.lowercase().trim()
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
            val jsonArray = JSONArray(json)

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