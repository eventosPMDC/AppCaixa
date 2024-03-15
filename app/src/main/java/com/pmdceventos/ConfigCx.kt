package com.pmdceventos

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.pmdceventos.databinding.ActivityConfigCxBinding

class ConfigCx : AppCompatActivity() {
    private lateinit var binding : ActivityConfigCxBinding
    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigCxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.edtChaveunica.setText(intent.getStringExtra("serialNmbr"))
        binding.edtCaixa.setText(intent.getStringExtra("caixa"))
        binding.btnOk.setOnClickListener {
            val chvunica = binding.edtChaveunica.text.toString()
            val caixa    = binding.edtCaixa.text.toString()

            if (caixa.isEmpty()){
                Toast.makeText(this, "O caixa não foi informado!", Toast.LENGTH_SHORT).show()
            } else {
                gravarConfig(chvunica,caixa)
            }
        }
    }

    private fun gravarConfig(chaveunica : String, caixan : String){
        val mpConfigcx = hashMapOf(
            "configId" to chaveunica,
            "caixa" to caixan)

        if (intent.getStringExtra("caixa") == "") {
            db.collection("Config").document(chaveunica).set(mpConfigcx)
                .addOnCompleteListener { gravacao ->
                    if (gravacao.isSuccessful) {
                        Toast.makeText(this, "Dados gravados!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.addOnFailureListener {
                Toast.makeText(this, "Houve um erro na gravação", Toast.LENGTH_SHORT).show()
            }
        }else{
            db.collection("Config").document(chaveunica).update(mpConfigcx as Map<String, Any>)
                .addOnCompleteListener { gravacao ->
                    if (gravacao.isSuccessful) {
                        Toast.makeText(this, "Dados gravados!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Houve um erro na gravação", Toast.LENGTH_SHORT).show()
                }
        }
    }
}