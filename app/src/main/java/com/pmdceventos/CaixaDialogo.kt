package com.pmdceventos

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
class CaixaDialogo (context: Context) : Dialog(context) {
    private lateinit var message: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val view = LayoutInflater.from(context).inflate(R.layout.caixa_dialogo, null)
        setContentView(view)

        val textViewMessage = view.findViewById<TextView>(R.id.text_message)
        textViewMessage.text = message

        val buttonOk = view.findViewById<Button>(R.id.button_ok)
        buttonOk.setOnClickListener {
            dismiss()
        }
    }

    fun setMessage(message: String): CaixaDialogo {
        this.message = message
        return this
    }
}