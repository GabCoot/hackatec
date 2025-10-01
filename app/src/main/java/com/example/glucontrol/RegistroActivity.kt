package com.example.glucontrol

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegistroActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro)

        nameEditText = findViewById(R.id.editTextName)
        lastNameEditText = findViewById(R.id.editTextLastName)
        emailEditText = findViewById(R.id.editTextEmail)
        passwordEditText = findViewById(R.id.editTextPassword)
        registerButton = findViewById(R.id.buttonRegister)

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (name.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                // Guardar usuario localmente usando SharedPreferences
                val sharedPref = getSharedPreferences("Usuarios", MODE_PRIVATE)
                val editor = sharedPref.edit()

                // Puedes usar el correo como clave para que no se repitan usuarios
                val userKey = email
                val userData =
                    "$name|$lastName|$password" // Guarda nombre, apellido y contraseña separados por "|"

                editor.putString(userKey, userData)
                editor.apply()

                Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()

                // Ir a HomeActivity
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("USERNAME", name)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }
}