package com.pmdceventos
data class ListaVendasData(
    val imageExpand: Int,
    val dataHora: String,
    val totVenda: String,
    val itensVenda: String,
    var expandir: Boolean = false
)