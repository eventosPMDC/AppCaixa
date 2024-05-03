package com.pmdceventos
data class ListaVendasData(
    val dataHora    : String,
    val totVenda    : String,
    val produto     : String,
    val qtde        : String,
    val vlrUnit     : String,
    val vlrTot      : String,
    var expandir    : Boolean = false
)