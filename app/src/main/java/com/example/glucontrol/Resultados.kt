package com.example.glucontrol

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.glucontrol.databinding.ActivityResultadosBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class Resultados : AppCompatActivity() {

    private lateinit var binding: ActivityResultadosBinding
    private val gson = Gson()
    private val prefsGrafica = "historial_grafica"
    private val prefsReportes = "reportes"
    private val maxMuestras = 5 // máx barras visibles

    // Estructura para la gráfica
    data class Muestra(val fechaEtiqueta: String, val valor: Float)

    // Estructura para los reportes
    data class Registro(
        val nivel: Int,
        val estado: String,
        val momento: String,
        val fechaHora: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultadosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- DATOS DE ESTA MEDICIÓN ---
        val nivelGlucosa = (70..99).random()
        val momentoMedicion = intent.getStringExtra("momentoMedicion") ?: "Sin dato"
        val fechaHoraCompleta = SimpleDateFormat("MM/dd/yyyy hh:mm a", Locale.getDefault()).format(Date())
        val etiquetaEjeX = SimpleDateFormat("MM/dd\nHH:mm", Locale.getDefault()).format(Date())

        // --- MOSTRAR EN CAJA AZUL ---
        binding.txtNivel.text = "$nivelGlucosa"
        binding.txtMomento.text = momentoMedicion
        binding.txtFechaHora.text = fechaHoraCompleta

        val estado = when {
            nivelGlucosa < 70 -> {
                binding.indicador.setGuidelinePercent(0.1f)
                "Bajo"
            }
            nivelGlucosa in 70..99 -> {
                binding.indicador.setGuidelinePercent(0.5f)
                "Normal"
            }
            else -> {
                binding.indicador.setGuidelinePercent(0.9f)
                "Alto"
            }
        }
        binding.txtEstado.text = estado

        // --- GUARDAR EN HISTÓRICO (GRÁFICA) ---
        val prefsGraf = getSharedPreferences(prefsGrafica, MODE_PRIVATE)
        val listaGuardada = gson.fromJson(
            prefsGraf.getString("lista", "[]"),
            Array<Muestra>::class.java
        )?.toMutableList() ?: mutableListOf()

        listaGuardada.add(Muestra(etiquetaEjeX, nivelGlucosa.toFloat()))
        while (listaGuardada.size > maxMuestras) {
            if (listaGuardada.isNotEmpty()) listaGuardada.removeAt(0)
        }
        prefsGraf.edit().putString("lista", gson.toJson(listaGuardada)).apply()

        // --- GUARDAR TAMBIÉN EN REPORTES ---
        val prefsReport = getSharedPreferences(prefsReportes, MODE_PRIVATE)
        val listaReportes = gson.fromJson(
            prefsReport.getString("historial", "[]"),
            Array<Registro>::class.java
        )?.toMutableList() ?: mutableListOf()

        listaReportes.add(Registro(nivelGlucosa, estado, momentoMedicion, fechaHoraCompleta))
        prefsReport.edit().putString("historial", gson.toJson(listaReportes)).apply()

        // --- MOSTRAR GRÁFICA ---
        mostrarGrafico(listaGuardada)

        // --- BOTONES ---
        binding.btnNuevoRegistro.setOnClickListener {
            startActivity(Intent(this, MedirCamara::class.java))
            finish()
        }

        binding.btnBack.setOnClickListener { finish() }

      
    }

    private fun mostrarGrafico(lista: List<Muestra>) {
        val entries = lista.mapIndexed { i, m -> BarEntry(i.toFloat(), m.valor) }
        val labels = lista.map { it.fechaEtiqueta }

        val dataSet = BarDataSet(entries, "Mediciones")
        dataSet.setDrawValues(false)

        val data = BarData(dataSet)
        data.barWidth = 0.6f

        binding.barChart.apply {
            clear()
            this.data = data
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textSize = 10f
            }
            axisLeft.axisMinimum = 60f
            axisLeft.axisMaximum = 130f
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            invalidate()
        }
    }
}
