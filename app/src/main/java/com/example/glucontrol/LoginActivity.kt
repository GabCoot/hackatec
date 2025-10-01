package com.example.glucontrol

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class LoginActivity : AppCompatActivity() {

    private lateinit var userEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var ingresarButton: Button
    private lateinit var registrarseText: TextView
    private lateinit var scrollView: ScrollView
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Vinculación de vistas
        userEditText = findViewById(R.id.editTextUser)
        passwordEditText = findViewById(R.id.editTextPassword)
        ingresarButton = findViewById(R.id.buttonLogin)
        registrarseText = findViewById(R.id.textRegister)
        scrollView = findViewById(R.id.scrollView)

        // Forzar scroll cuando se hace foco en los campos
        val focusScrollListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                scrollView.postDelayed({
                    scrollView.smoothScrollTo(0, v.top)
                }, 300)
            }
        }

        userEditText.onFocusChangeListener = focusScrollListener
        passwordEditText.onFocusChangeListener = focusScrollListener

        // Lógica de ingreso
        ingresarButton.setOnClickListener {
            val email = userEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val sharedPref = getSharedPreferences("Usuarios", MODE_PRIVATE)
                val userData = sharedPref.getString(email, null)

                if (userData != null) {
                    val parts = userData.split("|")
                    val storedPassword = parts[2]

                    if (password == storedPassword) {
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("USERNAME", parts[0])
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Usuario no registrado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Ingrese usuario y contraseña", Toast.LENGTH_SHORT).show()
            }
        }

        // Redirección a registro
        registrarseText.setOnClickListener {
            val intent = Intent(this, RegistroActivity::class.java)
            startActivity(intent)
        }
    }

    // Ocultar el teclado si se toca fuera de un EditText
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            currentFocus!!.clearFocus()
        }
        return super.dispatchTouchEvent(ev)
    }
}
