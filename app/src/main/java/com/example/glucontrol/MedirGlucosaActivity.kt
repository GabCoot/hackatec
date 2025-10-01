package com.example.glucontrol

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor
import kotlin.random.Random

class MedirGlucosaActivity : AppCompatActivity() {

    private lateinit var backButton: ImageButton
    private lateinit var helpButton: ImageButton
    private lateinit var fingerprintIcon: ImageView
    private lateinit var progressText: TextView
    private lateinit var resultadoText: TextView
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medir_glucosa)

        backButton = findViewById(R.id.backButton)
        helpButton = findViewById(R.id.helpButton)
        fingerprintIcon = findViewById(R.id.bloodDropView)
        progressText = findViewById(R.id.progressText)
        resultadoText = findViewById(R.id.resultadoText)

        // Botón para volver atrás
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Botón de ayuda con diálogo informativo
        helpButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Ayuda")
                .setMessage(
                    "Para medir tu glucosa, coloca tu dedo sobre el sensor y presiona el icono. " +
                            "Espera a que la medición termine sin mover el dedo. " +
                            "El resultado aparecerá en pantalla."
                )
                .setPositiveButton("Cerrar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startMeasurement()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    progressText.text = "Error de autenticación"
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    progressText.text = "Huella no reconocida"
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Medición de Glucosa")
            .setSubtitle("Coloca tu dedo sobre el sensor")
            .setNegativeButtonText("Cancelar")
            .build()

        fingerprintIcon.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun startMeasurement() {
        progressText.text = "Midiendo...\nNo mueva el dedo"
        resultadoText.text = ""

        Toast.makeText(this, "No mueva el dedo durante la medición", Toast.LENGTH_SHORT).show()

        fingerprintIcon.postDelayed({
            val resultado = generarGlucosaSimuladaNormal()
            val interpretacion = interpretarResultado(resultado)

            progressText.text = "Medición completada"
            resultadoText.text = "Resultado: $resultado mg/dL\n$interpretacion"
        }, 3000)
    }

    private fun generarGlucosaSimuladaNormal(): Int {
        return Random.nextInt(70, 100)
    }

    private fun interpretarResultado(resultado: Int): String {
        return "Nivel de glucosa NORMAL"
    }
}
