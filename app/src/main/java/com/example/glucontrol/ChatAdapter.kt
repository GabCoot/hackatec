package com.example.glucontrol

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val mensajes: MutableList<Mensaje>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val USER = 0
        const val BOT = 1
    }

    override fun getItemViewType(position: Int) = if (mensajes[position].isUser) USER else BOT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == USER) R.layout.usuario else R.layout.boot
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return if (viewType == USER) UserViewHolder(view) else BotViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = mensajes[position]
        if (holder is UserViewHolder) holder.text.text = msg.text
        else if (holder is BotViewHolder) holder.text.text = msg.text
    }

    override fun getItemCount() = mensajes.size

    fun addMessage(mensaje: Mensaje) {
        mensajes.add(mensaje)
        notifyItemInserted(mensajes.size - 1)
    }

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.userText)
    }

    class BotViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.botText)
    }
}
