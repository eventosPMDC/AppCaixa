package com.pmdceventos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class ListaVendasAdapter(private var mListaListaVddas : List<ListaVendasData>):
      RecyclerView.Adapter<ListaVendasAdapter.ListaVendasViewHolder>(){
    inner class ListaVendasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imgExpand: ImageView = itemView.findViewById(R.id.imgExpand)
        val tvDataHora: TextView = itemView.findViewById(R.id.tvDataHora)
        val tvTtlCobra: TextView = itemView.findViewById(R.id.tvTtlCobra)
        val tvProduto: TextView = itemView.findViewById(R.id.tvProduto)
        val tvQtd : TextView = itemView.findViewById(R.id.tvQtd)
        val tvVlrUnt: TextView = itemView.findViewById(R.id.tvVlrUnt)
        val tvVlrtot: TextView = itemView.findViewById(R.id.tvVlrTot)
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
        holder.tvProduto.text = listaVendasData.produto
        holder.tvQtd.text = listaVendasData.qtde
        holder.tvVlrUnt.text = listaVendasData.vlrUnit
        holder.tvVlrtot.text = listaVendasData.vlrTot

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
        }else{
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}