package com.pmdceventos

data class Produto(
    val nome: String,
    var valor: Double?
){
    constructor() : this("",null)
}
