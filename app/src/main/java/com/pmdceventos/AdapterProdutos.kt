package com.pmdceventos

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class AdapterProdutos (private val produto: ArrayList<Produto>, val setItemOnList: (Int) -> Unit):
    RecyclerView.Adapter<AdapterProdutos.AdapterViewHolder>() {

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterViewHolder {
        val produtoView = LayoutInflater.from(parent.context).inflate(R.layout.listar_produtos,
            parent, false)
         return AdapterViewHolder(produtoView)
    }

    override fun getItemCount(): Int =produto.size

    override fun onBindViewHolder(holder: AdapterViewHolder, position: Int) {
        Log.e("depois do var produto","onBindViewHolder")
        val currentProduto = produto[position]
        val nome = currentProduto.nome
        val valor = currentProduto.valor
        holder.btbproduto.text = buildString {
            append(nome)
            append("\n")
            append(formatCurrency(valor))
        }
        holder.btbproduto.setCompoundDrawablesWithIntrinsicBounds(getState(currentProduto.nome),0,0,0)
        holder.btbproduto.setOnClickListener { setItemOnList(position) }
    }

    private fun getState(nome: String): Int {
        when (nome) {
            "REFRIGERANTE" -> {
                return R.drawable.latasrefri
            }
            "AGUA" -> {
                return R.drawable.garrafa_agua
            }
            "CERVEJA" -> {
                return R.drawable.latascerveja
            }
            "CHOPP" -> {
                return R.drawable.chopp
            }
            else -> {
                return R.drawable.baseline_catching_pokemon
            }
        }
    }

    private fun formatCurrency(vlrtotal: Double?): CharSequence? {
        val formatoMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        return formatoMoeda.format(vlrtotal)
    }

    class AdapterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val btbproduto : AppCompatButton = itemView.findViewById(R.id.btn_produto)
    }
}