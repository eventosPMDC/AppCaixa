package com.pmdceventos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class EstoqueList(private var mListaEstoqueList: List<Produto>):
    RecyclerView.Adapter<EstoqueList.EstoqueListHolder>() {

    inner class EstoqueListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvProdutoEst: TextView = itemView.findViewById(R.id.tvProdutoEst)
        val tvQtdEstoque: TextView = itemView.findViewById(R.id.tvQtdEstoque)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EstoqueListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.estoque_lista , parent, false)
        return EstoqueListHolder(view)
    }

    override fun onBindViewHolder(holder: EstoqueListHolder, position: Int) {
        var listaEstoqueList = mListaEstoqueList[position]
        if (listaEstoqueList.ctrlEstoque == true) {
            holder.tvQtdEstoque.text = "Estoque Atual = ${listaEstoqueList.estoque?.toInt()}"
        } else {
            holder.tvQtdEstoque.text = "Qtd. Vendida = ${listaEstoqueList.estoque?.toInt()}"
        }
        holder.tvProdutoEst.text = listaEstoqueList.nome

    }

    override fun getItemCount(): Int {
        return mListaEstoqueList.size
    }
}