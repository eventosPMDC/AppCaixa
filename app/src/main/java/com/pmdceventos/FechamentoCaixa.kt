package com.pmdceventos

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.pmdceventos.databinding.ActivityFechamentoCaixaBinding
import java.lang.reflect.Array
import java.text.NumberFormat
import java.util.Locale

class FechamentoCaixa : AppCompatActivity() {

    private lateinit var binding: ActivityFechamentoCaixaBinding
    private lateinit var recyclerView: RecyclerView
    private var mListaVendasAdapter = ArrayList<ListaVendasData> ()
    private lateinit var adaptador : ListaVendasAdapter
    private var idcaixa : String? = ""
    private var dtCx : String? = ""
    private var lProduto : String? = ""
    private var lQtde : String? = ""
    private var lVlrUnit : String? = ""
    private var lVlrTot : String? = ""
    val db = FirebaseFirestore.getInstance()
    var al_idMovCx = ArrayList<String>()
    var al_cobranca = ArrayList<String>()
    var al_cobraOri = ArrayList<String>()
    var al_dathra = ArrayList<String>()
    var al_vlrTot = ArrayList<Double>()
    var i : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFechamentoCaixaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.recicleViewVendas)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adaptador = ListaVendasAdapter(mListaVendasAdapter)
        recyclerView.adapter = adaptador

        idcaixa = intent.getStringExtra("caixa")
        dtCx    = intent.getStringExtra("dataCX")

        binding.cancelFechamento.setOnClickListener { finish() }

        binding.confirmFechamento.setOnClickListener {
            val cxAberto = hashMapOf(
                "cxaberto" to "false"
            )
            db.collection("Config").document(serialNnbr!!).update(cxAberto as Map<String, Any?>)
            finish()
        }

        getVendas()

    }

    private fun getVendas(){
        db.collection(idcaixa!!)
            .document(dtCx!!)
            .collection("MovCaixa")
            .orderBy("seqmov")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        Log.e("Firestore error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            al_idMovCx.add(dc.document.id.toString())
                            var cobranca = dc.document.getString("cobranca")
                            var data = dc.document.getString("dia")
                            val hora = dc.document.getString("hora")
                            data = "Data/Hora: $data - $hora"
                            al_dathra.add(data)

                            if (dc.document.getString("cobranca") != "ABERTURA DE CAIXA") {
                                val vlrTotal = dc.document.getDouble("vlrTotal")
                                al_cobraOri.add(cobranca!!)
                                cobranca = "Total: R$ ${formatCurrency(vlrTotal)} - $cobranca"
                                al_cobranca.add(cobranca)
                                dc.document.getDouble("vlrTotal")?.let { al_vlrTot.add(it) }
                            } else {
                                al_cobranca.add(cobranca!!)
                            }
                            i++
                        }
                    }
                    getListaItens()
                }
            })

    }

    private fun clearParams(){
        lProduto = ""
        lQtde = ""
        lVlrTot = ""
        lVlrUnit = ""
    }

    private fun getListaItens() {
        val dbLI = FirebaseFirestore.getInstance()

        if (al_idMovCx.count() > 0) {
            for (j in 0 until al_idMovCx.count()) {
                if (al_cobranca[j] != "ABERTURA DE CAIXA") {
                    dbLI.collection(idcaixa!!).document(dtCx!!).collection("MovCxItem")
                        .whereEqualTo("codMovCx", al_idMovCx[j])
                        .orderBy("secItem")
                        .addSnapshotListener(object : EventListener<QuerySnapshot> {
                            override fun onEvent(
                                value: QuerySnapshot?,
                                error: FirebaseFirestoreException?
                            ) {
                                if (error != null) {
                                    Log.e("Firestore error", error.message.toString())
                                    return
                                }
                                var i: Int = 0
                                for (dc: DocumentChange in value?.documentChanges!!) {
                                    if (dc.type == DocumentChange.Type.ADDED) {
                                        if (i > 0) {
                                            lProduto += "\n"
                                            lQtde += "\n"
                                            lVlrTot += "\n"
                                            lVlrUnit += "\n"
                                        }
                                        var produto = dc.document.getString("Produto")
                                        var qtde = dc.document.getDouble("Qtde")
                                        var vlrUnit = dc.document.getDouble("VlrUnit")

                                        lProduto += produto
                                        lQtde += "$qtde"
                                        lVlrUnit += vlrUnit
                                        lVlrTot += (qtde!! * vlrUnit!!)

                                        i++
                                    }
                                }
                                mListaVendasAdapter.add(
                                    ListaVendasData(al_dathra[j],al_cobranca[j],lProduto!!,lQtde!!,lVlrUnit!!,lVlrTot!!))
                                adaptador.notifyDataSetChanged()
                                clearParams()
                                if (j == al_idMovCx.count()-1)
                                    setVlrCobranca()
                            }
                        })
                } else {
                    mListaVendasAdapter.add(
                        ListaVendasData(al_dathra[j],al_cobranca[j],"Não há movimento",lQtde!!,lVlrUnit!!,lVlrTot!!))
                }
            }
            adaptador.notifyDataSetChanged()
        }
    }

    private fun setVlrCobranca(){
        var lCobrancas : String = ""
        var cTexto : String = ""
        val vazio = "                         "
        var vlrTotCob : Double = 0.00
        for (i in 1..al_cobraOri.count()){
            if (!lCobrancas.contains(al_cobraOri[i-1].trimIndent())){
                for (j in 1..al_cobraOri.count()) {
                    if (al_cobraOri[j - 1] == al_cobraOri[i - 1]){
                        vlrTotCob += al_vlrTot[j - 1]
                    }
                }
                if (cTexto.count() > 0 ) {
                    cTexto += "\n"
                }
                lCobrancas += al_cobraOri[i-1].toString().trimIndent()
                val texto = (al_cobraOri[i-1].trimIndent() + vazio).substring(0,15)
                val valora = vazio + formatCurrency(vlrTotCob)
                vlrTotCob = 0.00
                cTexto +=
                    texto + valora.substring(valora.length - 12)

            }
            if (i == al_cobraOri.count()){
                binding.listViewCobrancas.text = cTexto
                binding.listViewCobrancas.visibility = View.VISIBLE
            }
        }
    }

    private fun formatCurrency(vlrtotal: Double?): CharSequence? {
        val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatoMoeda.format(vlrtotal)
    }
}