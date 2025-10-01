package com.example.glucontrol

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class VerRecordatorio : AppCompatActivity() {

    private lateinit var listaRecordatorios: LinearLayout
    private lateinit var bottomNav: BottomNavigationView
    private var recordatorios: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_recordatorio)

        listaRecordatorios = findViewById(R.id.listaRecordatorios)
        bottomNav = findViewById(R.id.bottomNavigation)
        val btnBack: ImageButton = findViewById(R.id.btnBack)

        // Cargar recordatorios desde SharedPreferences
        val sharedPref = getSharedPreferences("recordatorios", MODE_PRIVATE)
        val recordatoriosSet =
            sharedPref.getStringSet("lista_recordatorios", mutableSetOf()) ?: mutableSetOf()
        recordatorios = recordatoriosSet.toMutableList()

        showRecordatorios()

        btnBack.setOnClickListener {
            finish()
        }
        configurarBottomNav()
    }
        private fun configurarBottomNav() {
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


    private fun showRecordatorios() {
        listaRecordatorios.removeAllViews()

        for (recordatorio in recordatorios) {
            val cardView = layoutInflater.inflate(R.layout.item_recordatorio, listaRecordatorios, false)

            val tvRecordatorio: TextView = cardView.findViewById(R.id.tvRecordatorio)
            val btnEliminarRecordatorio: ImageButton = cardView.findViewById(R.id.btnEliminarRecordatorio)

            tvRecordatorio.text = recordatorio

            // Toggle expansión al hacer clic
            tvRecordatorio.setOnClickListener {
                val isExpanded = tvRecordatorio.maxLines == Integer.MAX_VALUE
                if (isExpanded) {
                    tvRecordatorio.maxLines = 2
                    tvRecordatorio.ellipsize = TextUtils.TruncateAt.END
                } else {
                    tvRecordatorio.maxLines = Integer.MAX_VALUE
                    tvRecordatorio.ellipsize = null
                }
            }

            btnEliminarRecordatorio.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Eliminar")
                    .setMessage("¿Seguro que deseas eliminar este recordatorio?")
                    .setPositiveButton("Sí") { _, _ ->
                        recordatorios.remove(recordatorio)
                        val editor = getSharedPreferences("recordatorios", MODE_PRIVATE).edit()
                        editor.putStringSet("lista_recordatorios", recordatorios.toSet())
                        editor.apply()
                        showRecordatorios()
                        Toast.makeText(this, "Recordatorio eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No", null)
                    .show()
            }

            listaRecordatorios.addView(cardView)
        }
    }
}




