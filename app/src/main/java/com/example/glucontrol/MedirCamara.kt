package com.example.glucontrol

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MedirCamara : AppCompatActivity() {

    private lateinit var webViewAR: WebView
    private val CAMERA_PERMISSION_CODE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medir_camara)

        webViewAR = findViewById(R.id.webViewAR)
        setupWebView()

        // Pedir permiso de cámara
        checkCameraPermission()
    }

    private fun setupWebView() {
        val webSettings: WebSettings = webViewAR.settings
        webSettings.javaScriptEnabled = true
        webSettings.allowFileAccess = true
        webSettings.domStorageEnabled = true

        webViewAR.webViewClient = WebViewClient()
        webViewAR.webChromeClient = WebChromeClient()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            loadARScene()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    private fun loadARScene() {
        // Cargar tu HTML desde assets
        webViewAR.loadUrl("file:///android_asset/ar.html")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadARScene()
            } else {
                // Permiso denegado, puedes mostrar un mensaje
            }
        }
    }
}
