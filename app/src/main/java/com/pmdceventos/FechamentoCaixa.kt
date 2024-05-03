package com.pmdceventos

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFechamentoCaixaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recyclerView = findViewById(R.id.recicleViewVendas)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

        idcaixa = intent.getStringExtra("caixa")
        dtCx    = intent.getStringExtra("dataCX")

        getVendas()
    }

    private fun getVendas(){
        var db = FirebaseFirestore.getInstance()
        val collectionMovCx = db.collection(idcaixa!!).document(dtCx!!).collection("MovCaixa")

        collectionMovCx.get().addOnSuccessListener {
            for (docMovCx in it){
                val idMovCx = docMovCx.id.toString()
                var cobranca = docMovCx.getString("cobranca")
                var data = docMovCx.getString("dia")
                val hora = docMovCx.getString("hora")
                if (cobranca == "ABERTURA DE CAIXA"){
                    data += "Data/Hora: $data - $hora"
                    lProduto = "Não há moviemnto"
                    lQtde = ""
                    lVlrTot = ""
                    lVlrUnit = ""
                } else {
                    val vlrTotal = docMovCx.getDouble("vlrTotal")
                    cobranca = "Total: R$ ${formatCurrency(vlrTotal)} - $cobranca"
                }

                mListaVendasAdapter.add(
                    ListaVendasData(data!!,cobranca,lProduto!!,lQtde!!,lVlrUnit!!,lVlrTot!!))

                clearParams()
            }
        }.addOnFailureListener { exception ->
            // Lidar com falhas ao obter os documentos
            Toast.makeText(this,"Erro ao obter documentos: ${exception.message}",Toast.LENGTH_LONG)
        }
        adaptador = ListaVendasAdapter(mListaVendasAdapter)
        recyclerView.adapter = adaptador
    }

    private fun clearParams(){
        lProduto = ""
        lQtde = ""
        lVlrTot = ""
        lVlrUnit = ""
    }

    private fun getListaItens(idMov: String) {
        val dbLI = FirebaseFirestore.getInstance()
        val collectionListaItens = dbLI.collection(idcaixa!!).document(dtCx!!).collection("MovCxItem")
        val qryListaItens = collectionListaItens.whereArrayContains("codMovCx", arrayOf(idMov))
            .orderBy("secItem", Query.Direction.ASCENDING)

        qryListaItens.get().addOnSuccessListener {
            var i : Int = 0
            for (docListaItens in it){
                if (i > 0) {
                    lProduto += "\n"
                    lQtde  += "\n"
                    lVlrTot += "\n"
                    lVlrUnit += "\n"
                }
                var produto = docListaItens.getString("Produto")
                var qtde    = docListaItens.getDouble("Qtde")
                var vlrUnit = docListaItens.getDouble("VlrUnit")

                lProduto += produto
                lQtde    += "$qtde x R$"
                lVlrUnit += " $vlrUnit ="
                lVlrTot  += formatCurrency(qtde!! * vlrUnit!!)

                i++
            }
        }
    }

    private fun setVlrCobranca(cobra:String, valor: Double){

    }

    private fun formatCurrency(vlrtotal: Double?): CharSequence? {
        val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatoMoeda.format(vlrtotal)
    }
}