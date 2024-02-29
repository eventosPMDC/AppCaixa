package com.pmdceventos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class AdapterItensLista(private val newItensLista: ArrayList<ItensLista>, val onClickDelete: (Int) -> Unit) :
    RecyclerView.Adapter<AdapterItensLista.AdapterViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.listar_itens,
            parent,false)
        return AdapterViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return newItensLista.size
    }

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
        val currentItem = newItensLista[position]
        holder.descricao?.text = currentItem.descricao
        holder.qtdevlrun?.text = currentItem.qtdevlrun
        holder.vlrtotal?.text = formatCurrency(currentItem.vlrtotal)
        holder.button?.setOnClickListener { onClickDelete(position) }
    }

    private fun formatCurrency(vlrtotal: Double?): CharSequence? {
        val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatoMoeda.format(vlrtotal)
    }

    class AdapterViewHolder(itemView :View): RecyclerView.ViewHolder(itemView){
        val descricao : TextView? = itemView.findViewById(R.id.tv_item)
        val qtdevlrun : TextView? = itemView.findViewById(R.id.tv_qtdvlrun)
        val vlrtotal : TextView? = itemView.findViewById(R.id.tv_vlrtotal)
        val button : Button? = itemView.findViewById(R.id.btn_del)
    }
}