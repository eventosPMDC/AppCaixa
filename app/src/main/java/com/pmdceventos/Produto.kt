package com.pmdceventos

data class Produto(
    val nome: String,
    var valor: Double?,
    val idProd: String,
    val imagem: String,
    val valorC: Double?,
    var estoque: Double?,
    var combo: Boolean?,
    var idProC: String?,
    val ctrlEstoque: Boolean?
){
    constructor() : this("",null,"","",0.00,0.00,false,"",false)
}
