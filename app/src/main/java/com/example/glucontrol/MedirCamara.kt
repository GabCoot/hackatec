package com.example.glucontrol

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs

class MedirCamara : AppCompatActivity() {

    private lateinit var dropdown: Spinner
    private lateinit var progressText: TextView
    private lateinit var previewView: PreviewView
    private lateinit var cameraControl: CameraControl
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var bottomNav: BottomNavigationView

    private var isMeasuring = false
    private var isFinishingMeasurement = false
    private var cameraStarted = false
    private val handler = Handler(Looper.getMainLooper())
    private var finishRunnable: Runnable? = null

    private val measurementOptions = listOf(
        "Seleccione una opción",
        "Ayuno (por la mañana temprano)",
        "1 hr después del desayuno",
        "2 hrs después del desayuno",
        "Antes de comer",
        "1 hr después del almuerzo",
        "2 hrs después del almuerzo",
        "Antes de la cena",
        "1 hr después de la cena",
        "2 hrs después de la cena"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medir_camara)

        dropdown     = findViewById(R.id.dropdown)
        previewView  = PreviewView(this)
        findViewById<FrameLayout>(R.id.cameraPreviewContainer).addView(previewView)
        progressText = findViewById(R.id.progressStatus)
        bottomNav    = findViewById(R.id.bottomNavigation)

        dropdown.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, measurementOptions
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        findViewById<ImageButton>(R.id.helpButton).setOnClickListener { mostrarAyuda() }
        findViewById<ImageButton>(R.id.backButton).setOnClickListener { finish() }
        configurarBottomNav()

        cameraExecutor = Executors.newSingleThreadExecutor()

        dropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                if (pos != 0 && !cameraStarted) solicitarCamara()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
    }

    private fun solicitarCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10)
        }
    }

    private fun startCamera() {
        cameraStarted = true
        ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener({
                val provider = future.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val analyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { ia ->
                        ia.setAnalyzer(cameraExecutor) { image ->
                            val redSignal = redDifference(image)
                            image.close()

                            if (redSignal > 50 && !isMeasuring && !isFinishingMeasurement) {
                                runOnUiThread { startMeasuring() }
                            } else if (redSignal < 30 && isMeasuring && !isFinishingMeasurement) {
                                runOnUiThread { stopMeasuring() }
                            }
                        }
                    }

                val cam = provider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer
                )
                cameraControl = cam.cameraControl
                cameraControl.enableTorch(true)
            }, ContextCompat.getMainExecutor(this))
        }
    }

    private fun redDifference(image: ImageProxy): Int {
        val uBuf = image.planes[1].buffer
        val vBuf = image.planes[2].buffer
        val uStep = image.planes[1].pixelStride
        val vStep = image.planes[2].pixelStride

        var sumU = 0
        var sumV = 0
        var count = 0
        val sampleEvery = 16

        val limit = vBuf.remaining()
        var i = 0
        while (i < limit) {
            sumU += uBuf[i].toInt() and 0xFF
            sumV += vBuf[i].toInt() and 0xFF
            count++
            i += sampleEvery * vStep
        }
        if (count == 0) return 0
        return abs((sumV / count) - (sumU / count))
    }

    private fun startMeasuring() {
        isMeasuring = true
        progressText.text = "Midiendo...\nNo retire el dedo"

        finishRunnable?.let { handler.removeCallbacks(it) }

        finishRunnable = Runnable {
            progressText.text = "Medición completada"
            isFinishingMeasurement = true
            cameraControl.enableTorch(false)

            handler.postDelayed({
                finalizarMedicion()
            }, 2000)
        }

        handler.postDelayed(finishRunnable!!, 3000)
    }

    private fun stopMeasuring() {
        finishRunnable?.let { handler.removeCallbacks(it) }
        isMeasuring = false
        progressText.text = ""
        cameraControl.enableTorch(true)
    }

    private fun finalizarMedicion() {
        isMeasuring = false
        isFinishingMeasurement = false
        val momento = dropdown.selectedItem.toString()
        val glucosaSimulada = obtenerGlucosaSimulada(momento)

        val intent = Intent(this, Resultados::class.java).apply {
            putExtra("momentoMedicion", momento)
            putExtra("glucosa", glucosaSimulada)
        }
        startActivity(intent)
        finish()
    }

    private fun obtenerGlucosaSimulada(momento: String): Int {
        return when (momento) {
            "Ayuno (por la mañana temprano)"      -> (70..99).random()
            "1 hr después del desayuno"           -> (140..180).random()
            "2 hrs después del desayuno"          -> (120..140).random()
            "Antes de comer"                      -> (70..100).random()
            "1 hr después del almuerzo"           -> (140..180).random()
            "2 hrs después del almuerzo"          -> (120..140).random()
            "Antes de la cena"                    -> (70..100).random()
            "1 hr después de la cena"             -> (140..180).random()
            "2 hrs después de la cena"            -> (120..140).random()
            else                                  -> (80..100).random()
        }
    }

    private fun mostrarAyuda() = AlertDialog.Builder(this)
        .setTitle("Ayuda")
        .setMessage("Coloque el dedo sobre la cámara y mantenga presionado para medir su nivel de glucosa.")
        .setPositiveButton("Entendido") { d, _ -> d.dismiss() }
        .show()

    private fun configurarBottomNav() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_inicio -> {
                    startActivity(Intent(this, HomeActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
                    true
                }
                R.id.menu_reportes -> {
                    startActivity(Intent(this, Reportes::class.java))
                    true
                }
                R.id.menu_notificaciones -> {
                    Toast.makeText(this, "Ver Notificaciones", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    override fun onRequestPermissionsResult(code: Int, perms: Array<out String>, res: IntArray) {
        super.onRequestPermissionsResult(code, perms, res)
        if (code == 10 && res.isNotEmpty() && res[0] == PackageManager.PERMISSION_GRANTED) {
            if (!cameraStarted) startCamera()
        } else {
            Toast.makeText(this, "Se necesita permiso de cámara", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        cameraExecutor.shutdown()
    }
}
