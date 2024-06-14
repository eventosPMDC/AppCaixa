package com.pmdceventos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class ListaVendasAdapter(private var mListaListaVddas : List<ListaVendasData>):
      RecyclerView.Adapter<ListaVendasAdapter.ListaVendasViewHolder>(){
    inner class ListaVendasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imgExpand: ImageView = itemView.findViewById(R.id.imgExpand)
        val tvDataHora: TextView = itemView.findViewById(R.id.tvDataHora)
        val tvTtlCobra: TextView = itemView.findViewById(R.id.tvTtlCobra)
        val tvProduto: TextView = itemView.findViewById(R.id.tvProduto)
        val constraintLayoutLV : ConstraintLayout = itemView.findViewById(R.id.contraintLayoutLV)
        val clListaItens : ConstraintLayout = itemView.findViewById(R.id.clListaItens)
        fun collapseExpandedView(){
            clListaItens.visibility = View.GONE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListaVendasViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.venda_lista , parent, false)
        return ListaVendasViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mListaListaVddas.size
    }

    override fun onBindViewHolder(holder: ListaVendasViewHolder, position: Int) {
        val listaVendasData = mListaListaVddas[position]
        holder.tvDataHora.text = listaVendasData.dataHora
        holder.tvTtlCobra.text = listaVendasData.totVenda
        holder.tvProduto.text = generateLineProd(listaVendasData)

        val estaEspandido : Boolean = listaVendasData.expandir
        holder.clListaItens.visibility = if (estaEspandido) View.VISIBLE else View.GONE
        if (estaEspandido) {
            holder.imgExpand.setImageResource(R.drawable.baseline_expand_less_24)
        } else holder.imgExpand.setImageResource(R.drawable.baseline_expand_more_24)

        holder.constraintLayoutLV.setOnClickListener {
            oItemEstaExtendido(position)
            listaVendasData.expandir = !listaVendasData.expandir
            notifyItemChanged(position,Unit)
        }

    }

    private fun oItemEstaExtendido(position: Int){
        val temp = mListaListaVddas.indexOfFirst {
            it.expandir
        }
        if (temp >= 0 && temp != position){
            mListaListaVddas[temp].expandir = false
            notifyItemChanged(temp, 0)
        }
    }

    override fun onBindViewHolder(
        holder: ListaVendasViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ){
        if(payloads.isNotEmpty() && payloads[0] == 0){
            holder.collapseExpandedView()
            holder.imgExpand.setImageResource(R.drawable.baseline_expand_more_24)
        }else{
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    private fun generateLineProd(listaVendasData : ListaVendasData): String {
        var countL = listaVendasData.produto.lines().count()
        var linha : String = ""
        val linhaVazia = "                                                             "
        for (i in 1..countL) {
            if (i > 1) {
                linha += "\n"
            }
            var prod = listaVendasData.produto.lines()[i-1]
            prod += linhaVazia
            prod = prod.substring(0,30)

            var qtd  = listaVendasData.qtde.lines()[i-1]
            if (qtd != "") {
                val qtdInt = qtd.toDouble()
                qtd = "$linhaVazia${qtdInt.toInt()} X"
                qtd = qtd.substring(qtd.length - 6)
            }

            var vlrun = listaVendasData.vlrUnit.lines()[i-1]
            if (vlrun != "") {
                vlrun = linhaVazia + formatCurrency(vlrun.toDouble(), false) + " ="
                vlrun = vlrun.substring(vlrun.length - 10)
            }

            var totPro = listaVendasData.vlrTot.lines()[i-1]
            if (totPro != "") {
                totPro = linhaVazia + formatCurrency(totPro.toDouble())
                totPro = totPro.substring(totPro.length - 11)
            }

            linha += prod + qtd + vlrun + totPro
        }
        return linha
    }

    private fun formatCurrency(vlrtotal: Double?, vlrNum: Boolean = true): String? {
        val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        if (vlrNum) {
            return formatoMoeda.format(vlrtotal)
        } else return formatoMoeda.format(vlrtotal).replace("R$","")
    }
}