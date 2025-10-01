package com.example.glucontrol

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class Reportes : AppCompatActivity() {

    private lateinit var contenedor: LinearLayout
    private lateinit var bottomNav: BottomNavigationView
    private val gson = Gson()
    private val prefsFile = "reportes"          // mismo archivo que Resultados
    private val keyHist = "historial"

    private var registros = mutableListOf<Registro>()

    data class Registro(val nivel: Int, val estado: String, val momento: String, val fechaHora: String)

    /* ------------------ CICLO DE VIDA ------------------ */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportes)

        contenedor = findViewById(R.id.contenedorRegistros)
        bottomNav  = findViewById(R.id.bottomNavigation)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            startActivity(
                Intent(this, HomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
        findViewById<Button>(R.id.btnExportar).setOnClickListener { exportar() }
        findViewById<ImageButton>(R.id.btnBorrar).setOnClickListener { borrarSeleccionados() }

        configurarBottomNav()
        cargarRegistros()
        dibujarRegistros()
    }

    /* ------------------ BOTTOM NAV ------------------ */
    private fun configurarBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_inicio -> {
                    startActivity(
                        Intent(this, HomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    ); true
                }
                R.id.menu_reportes -> true
                R.id.menu_notificaciones -> {
                    Toast.makeText(this, "Notificaciones", Toast.LENGTH_SHORT).show(); true
                }
                else -> false
            }
        }
        bottomNav.selectedItemId = R.id.menu_reportes
    }

    /* ------------------- CARGAR / SALVAR ------------------- */
    private fun cargarRegistros() {
        val prefs = getSharedPreferences(prefsFile, MODE_PRIVATE)
        registros = gson.fromJson(
            prefs.getString(keyHist, "[]"),
            Array<Registro>::class.java
        )?.toMutableList() ?: mutableListOf()
    }

    private fun guardarRegistros() {
        val prefs = getSharedPreferences(prefsFile, MODE_PRIVATE)
        prefs.edit().putString(keyHist, gson.toJson(registros)).apply()
    }

    /* ------------------- PINTAR UI ------------------- */
    private fun dibujarRegistros() {
        contenedor.removeAllViews()
        registros.forEachIndexed { idx, r ->
            val item = layoutInflater.inflate(R.layout.item_reporte, contenedor, false)
            item.findViewById<TextView>(R.id.txtNivel).text      = "${r.nivel} mg/dL"
            item.findViewById<TextView>(R.id.txtEstado).text     = r.estado
            item.findViewById<TextView>(R.id.txtMomento).text    = r.momento
            item.findViewById<TextView>(R.id.txtFechaHora).text  = r.fechaHora
            item.tag = idx
            contenedor.addView(item)
        }
    }

    /* ------------------- EXPORTAR PDF ------------------- */
    private fun exportar() {
        val lista = registrosSeleccionados().ifEmpty { registros }
        if (lista.isEmpty()) { toast("No hay registros para exportar"); return }

        if (Build.VERSION.SDK_INT < 29 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            solicitarPermisoEscritura(); return
        }

        val pdf   = PdfDocument()
        val paint = Paint().apply { textSize = 12f }
        var page  = pdf.startPage(PdfDocument.PageInfo.Builder(300, 420, 1).create())
        var y     = 25

        lista.forEach { r ->
            if (y > 380) { pdf.finishPage(page); page = pdf.startPage(PdfDocument.PageInfo.Builder(300, 420, 1).create()); y = 25 }
            page.canvas.drawText("Fecha:  ${r.fechaHora}",                         10f, y.toFloat(), paint); y += 15
            page.canvas.drawText("Momento: ${r.momento}",                          10f, y.toFloat(), paint); y += 15
            page.canvas.drawText("Nivel:   ${r.nivel} mg/dL   Estado: ${r.estado}",10f, y.toFloat(), paint); y += 25
            page.canvas.drawLine(10f, y.toFloat(), 290f, y.toFloat(), paint); y += 15
        }
        pdf.finishPage(page)

        val nombre = "Reportes_" + SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date()) + ".pdf"
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, nombre)
        pdf.writeTo(FileOutputStream(file))
        pdf.close()

        toast("PDF guardado en:\n${file.absolutePath}")
    }

    /* ------------------- BORRAR ------------------- */
    private fun borrarSeleccionados() {
        val eliminar = registrosSeleccionados()
        if (eliminar.isEmpty()) { toast("Selecciona al menos un registro"); return }
        registros.removeAll(eliminar)
        guardarRegistros()
        dibujarRegistros()
    }

    /* ------------------- UTILIDADES ------------------- */
    private fun registrosSeleccionados(): List<Registro> {
        val list = mutableListOf<Registro>()
        for (i in 0 until contenedor.childCount) {
            val view = contenedor.getChildAt(i)
            if (view.findViewById<CheckBox>(R.id.checkSeleccion).isChecked) {
                list.add(registros[view.tag as Int])
            }
        }
        return list
    }

    private fun solicitarPermisoEscritura() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 20)
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
