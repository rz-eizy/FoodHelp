package com.example.foodhelp.data

data class Receta(
    val id: Long,
    val nombre: String,
    val descripcion: String,
    val tiempoPreparacion: Int,
    val imagenUrl: String,
    val instrucciones: String,
    val ingredientes: List<Ingrediente>,
    val categoria: Categoria
)
