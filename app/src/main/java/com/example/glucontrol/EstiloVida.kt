package com.example.glucontrol

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.glucontrol.databinding.ActivityEstiloVidaBinding

class EstiloVida : AppCompatActivity() {

    private lateinit var binding: ActivityEstiloVidaBinding
    private lateinit var adapter: ComidaAdapter
    private val listaComidas = mutableListOf<Comida>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding
        binding = ActivityEstiloVidaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configuración RecyclerView
        adapter = ComidaAdapter(listaComidas)
        binding.recyclerComidas.layoutManager = LinearLayoutManager(this)
        binding.recyclerComidas.adapter = adapter

        // Guardar comida
        binding.btnGuardar.setOnClickListener {
            val nombre = binding.editTextComida.text.toString()
            val cantidad = binding.editTextCantidad.text.toString()
            val hora = binding.editTextHora.text.toString()

            if (nombre.isNotEmpty() && cantidad.isNotEmpty() && hora.isNotEmpty()) {
                val comida = Comida(nombre, cantidad, hora)
                listaComidas.add(comida)
                adapter.notifyItemInserted(listaComidas.size - 1)
                limpiarCampos()
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Exportar el pdf
        binding.btnExportarComidas.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de exportar pendiente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun limpiarCampos() {
        binding.editTextComida.text.clear()
        binding.editTextCantidad.text.clear()
        binding.editTextHora.text.clear()
    }
}
