package com.pmdceventos

import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.pmdceventos.databinding.ActivityEstoqueBinding

class PosicaoEstoque : AppCompatActivity() {
    private lateinit var binding: ActivityEstoqueBinding
    private lateinit var recicleViewE: RecyclerView
    private lateinit var recicleViewD: RecyclerView
    private var mListaEstqListE = ArrayList<Produto>  ()
    private var mListaEstqListD = ArrayList<Produto>  ()
    private lateinit var adaptadorE: EstoqueList
    private lateinit var adaptadorD: EstoqueList
    private val db = FirebaseFirestore.getInstance()
    var i : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEstoqueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recicleViewD = findViewById(R.id.recicleVEstoqueDir)
        recicleViewE = findViewById(R.id.recicleVEstoqueEsq)

        recicleViewD.setHasFixedSize(true)
        recicleViewD.layoutManager = LinearLayoutManager(this)
        recicleViewE.setHasFixedSize(true)
        recicleViewE.layoutManager = LinearLayoutManager(this)

        adaptadorD = EstoqueList(mListaEstqListD)
        recicleViewD.adapter = adaptadorD

        adaptadorE = EstoqueList(mListaEstqListE)
        recicleViewE.adapter = adaptadorE

        binding.fecharPosicaoEstoque.setOnClickListener { finish() }

        getEstoque()
        hideSystemBars()
    }

    private fun getEstoque(){
        db.collection("Produtos").orderBy("secProd")
            .addSnapshotListener(object : EventListener<QuerySnapshot>{
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null){
                        Log.e("Firestore error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            var produto = dc.document.getString("nome")
                            var estoque = dc.document.getString("Estoque")
                            var ctrlEstoque = dc.document.getBoolean("ctrlEstoque")

                            if (i % 2 == 0) {
                                mListaEstqListE.add(dc.document.toObject(Produto::class.java))
                            } else {
                                mListaEstqListD.add(dc.document.toObject(Produto::class.java))
                            }
                            i++
                        }
                    }
                    adaptadorE.notifyDataSetChanged()
                    adaptadorD.notifyDataSetChanged()
                }
            })
    }

    private fun hideSystemBars() {
        // Para Android 11 (API 30) ou superior
        val controller = window.insetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.systemBars()) // Oculta barra de status e navegação
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
