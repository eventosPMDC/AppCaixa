package com.pmdceventos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pmdceventos.databinding.ActivityFechamentoCaixaBinding

class FechamentoCaixa : AppCompatActivity() {

    private lateinit var binding: ActivityFechamentoCaixaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFechamentoCaixaBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun getVendas(){
        var db = FirebaseFirestore.getInstance()
        var idcaixa = intent.getStringExtra("caixa")
        var dtCx    = intent.getStringExtra("dataCX")
        val collectionMovCx = db.collection(idcaixa!!).document(dtCx!!).collection("MovCaixa")
        val queryMovCx = collectionMovCx
               .orderBy("dia", Query.Direction.ASCENDING)
               .orderBy("hora", Query.Direction.ASCENDING)

        queryMovCx.get().addOnSuccessListener {
            for (docMovCx in it){
                val idMovCx = docMovCx.id.toString()
                val cobranca = docMovCx.data.get("cobranca").toString()
                val data = docMovCx.getString("dia")
                val hora = docMovCx.getString("hora")
            }
        }


    }


    private fun setVlrCobranca(cobra:String, valor: Double){

    }
}