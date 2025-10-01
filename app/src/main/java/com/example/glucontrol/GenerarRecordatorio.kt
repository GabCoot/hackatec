package com.example.glucontrol

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TimePicker
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class GenerarRecordatorio : AppCompatActivity() {

    private lateinit var timePicker: TimePicker
    private lateinit var etNota: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnBack: ImageButton
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var dayToggles: List<ToggleButton>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generarrecordatorio)

        // Inicialización de vistas
        timePicker = findViewById(R.id.timePicker)
        etNota = findViewById(R.id.etNota)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnBack = findViewById(R.id.btnBack)
        bottomNav = findViewById(R.id.bottomNavigation)

        // Lista de ToggleButtons para los días
        dayToggles = listOf(
            findViewById(R.id.daySun),
            findViewById(R.id.dayMon),
            findViewById(R.id.dayTue),
            findViewById(R.id.dayWed),
            findViewById(R.id.dayThu),
            findViewById(R.id.dayFri),
            findViewById(R.id.daySat)
        )

        // Acción botón guardar
        btnGuardar.setOnClickListener {
            val hour: Int
            val minute: Int
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                hour = timePicker.hour
                minute = timePicker.minute
            } else {
                hour = timePicker.currentHour
                minute = timePicker.currentMinute
            }

            val nota = etNota.text.toString()
            val diasSeleccionados = dayToggles
                .filter { it.isChecked }
                .map { it.text.toString() }

            val recordatorioNuevo = "$hour:$minute - $nota - Días: ${diasSeleccionados.joinToString(", ")}"

            // Guardar en SharedPreferences
            val sharedPref = getSharedPreferences("recordatorios", MODE_PRIVATE)
            val editor = sharedPref.edit()

            // Obtener lista existente
            val recordatoriosExistentes = sharedPref.getStringSet("lista_recordatorios", mutableSetOf()) ?: mutableSetOf()

            // Agregar nuevo recordatorio
            val nuevosRecordatorios = recordatoriosExistentes.toMutableSet()
            nuevosRecordatorios.add(recordatorioNuevo)

            // Guardar lista actualizada
            editor.putStringSet("lista_recordatorios", nuevosRecordatorios)
            editor.apply()

            // Confirmación al usuario
            Toast.makeText(this, "Recordatorio guardado", Toast.LENGTH_SHORT).show()

            // Ir a la pantalla de visualización
            val intent = Intent(this, VerRecordatorio::class.java)
            startActivity(intent)
        }

        // Acción botón volver
        btnBack.setOnClickListener {
            finish()
        }

        // Bottom Navigation
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_inicio -> {
                    startActivity(
                        Intent(this, HomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                    true
                }

                R.id.menu_reportes -> {
                    startActivity(Intent(this, Reportes::class.java))
                    true
                }

                R.id.menu_notificaciones -> {
                    Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }
}

