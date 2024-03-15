package com.pmdceventos

data class ItensLista(
    val descricao : String,
    val qtdevlrun : String,
    val vlrtotal  : Double? = 0.00,
    val vlrUnit   : Double? = 0.00,
    val qtde      : Int? = 0,
    val idProd    : String
)
