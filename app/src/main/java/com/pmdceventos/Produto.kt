package com.pmdceventos

data class Produto(
    val nome: String,
    var valor: Double?,
    val idProd: String,
    val imagem: String
){
    constructor() : this("",null,"","")
}
