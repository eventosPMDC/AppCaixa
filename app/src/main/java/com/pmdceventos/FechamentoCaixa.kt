package com.pmdceventos

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.pmdceventos.databinding.ActivityFechamentoCaixaBinding
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
    private val db = FirebaseFirestore.getInstance()
    var alidMovCx = ArrayList<String>()
    var alCobranca = ArrayList<String>()
    var alCobraOri = ArrayList<String>()
    var alDathra = ArrayList<String>()
    var alVlrTot = ArrayList<Double>()
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
            cxaberto = "false"
            setResult(RESULT_OK)
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
                            alidMovCx.add(dc.document.id)
                            var cobranca = dc.document.getString("cobranca")
                            var data = dc.document.getString("dia")
                            val hora = dc.document.getString("hora")
                            data = "Data/Hora: $data - $hora"
                            alDathra.add(data)

                            if (dc.document.getString("cobranca") != "ABERTURA DE CAIXA") {
                                val vlrTotal = dc.document.getDouble("vlrTotal")
                                alCobraOri.add(cobranca!!)
                                cobranca = "Total: R$ ${formatCurrency(vlrTotal)} - $cobranca"
                                alCobranca.add(cobranca)
                                dc.document.getDouble("vlrTotal")?.let { alVlrTot.add(it) }
                            } else {
                                alCobranca.add(cobranca!!)
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

        if (alidMovCx.count() > 0) {
            for (j in 0 until alidMovCx.count()) {
                if (alCobranca[j] != "ABERTURA DE CAIXA") {
                    dbLI.collection(idcaixa!!).document(dtCx!!).collection("MovCxItem")
                        .whereEqualTo("codMovCx", alidMovCx[j])
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
                                var i = 0
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
                                    ListaVendasData(alDathra[j],alCobranca[j],lProduto!!,lQtde!!,lVlrUnit!!,lVlrTot!!))
                                adaptador.notifyDataSetChanged()
                                clearParams()
                                if (j == alidMovCx.count()-1)
                                    setVlrCobranca()
                            }
                        })
                } else {
                    mListaVendasAdapter.add(
                        ListaVendasData(alDathra[j],alCobranca[j],"Não há movimento",lQtde!!,lVlrUnit!!,lVlrTot!!))
                }
            }
            adaptador.notifyDataSetChanged()
        }
    }

    private fun setVlrCobranca(){
        var lCobrancas = ""
        var cTexto = ""
        val vazio = "                         "
        var vlrTotCob = 0.00
        for (i in 1..alCobraOri.count()){
            if (!lCobrancas.contains(alCobraOri[i-1].trimIndent())){
                for (j in 1..alCobraOri.count()) {
                    if (alCobraOri[j - 1] == alCobraOri[i - 1]){
                        vlrTotCob += alVlrTot[j - 1]
                    }
                }
                if (cTexto.count() > 0 ) {
                    cTexto += "\n"
                }
                lCobrancas += alCobraOri[i-1].trimIndent()
                val texto = (alCobraOri[i-1].trimIndent() + vazio).substring(0,15)
                val valora = vazio + formatCurrency(vlrTotCob)
                vlrTotCob = 0.00
                cTexto +=
                    texto + valora.substring(valora.length - 12)

            }
            if (i == alCobraOri.count()){
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