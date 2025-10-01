package com.example.glucontrol

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class HomeActivity : AppCompatActivity() {

    private lateinit var txtGreeting: TextView
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var btnMedir: Button
    private lateinit var btnMedir2: Button
    private lateinit var btnRecordatorio: ImageButton
    private lateinit var btnChatIA: ImageButton
    private lateinit var btnRecomendacion: ImageButton
    private lateinit var fabMic: FloatingActionButton
    private lateinit var fabChat: FloatingActionButton 
    private lateinit var btnalimento: ImageButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Obtener el nombre del usuario desde el intent
        val userName = intent.getStringExtra("USERNAME") ?: "Usuario"

        // Inicializar vistas
        txtGreeting = findViewById(R.id.txtGreeting)
        bottomNav = findViewById(R.id.bottomNavigation)
        btnMedir = findViewById(R.id.btnMedir)
        btnMedir2 = findViewById(R.id.btnMedir2)
        btnRecordatorio = findViewById(R.id.btnRecordatorio)
        btnRecomendacion = findViewById(R.id.btnRecomendacion)
        fabMic = findViewById(R.id.fabMic)
        fabChat = findViewById(R.id.fabChat)
        btnalimento = findViewById(R.id.btnalimento)

        // Mostrar saludo
        txtGreeting.text = "Hola!\n$userName"

        // Botones de medir glucosa
        btnMedir.setOnClickListener {
            startActivity(Intent(this, MedirGlucosaActivity::class.java))
        }

        btnMedir2.setOnClickListener {
            startActivity(Intent(this, MedirCamara::class.java))
        }

        // Botón de recordatorio
        btnRecordatorio.setOnClickListener {
            startActivity(Intent(this, GenerarRecordatorio::class.java))
        }

        // FloatingActionButtons
        fabMic.setOnClickListener {
            startActivity(Intent(this, chatvoz::class.java))
        }

        fabChat.setOnClickListener {
            startActivity(Intent(this, ChatBoot::class.java))
        }

        // Botón de recomendación
        btnRecomendacion.setOnClickListener {
            Toast.makeText(this, "Recomendación seleccionada", Toast.LENGTH_SHORT).show()
        }

        btnalimento.setOnClickListener {
            startActivity(Intent(this, EstiloVida::class.java))
        }

    }
}
