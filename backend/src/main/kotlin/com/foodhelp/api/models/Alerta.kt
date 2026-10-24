package com.foodhelp.api.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "alertas")
class Alerta(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(nullable = false)
    val articuloId: UUID,
    @Column(nullable = false)
    val nombreProducto: String,
    @Column(nullable = false)
    val diasRestantes: Int,
    @Column(nullable = false)
    val hogarId: UUID,
    @Column(nullable = false)
    var leida: Boolean = false,
    @Column(nullable = false)
    val fechaCreacion: LocalDate = LocalDate.now(),
) {
    override fun toString(): String =
        "Alerta(id=$id, articuloId=$articuloId, nombreProducto='$nombreProducto', diasRestantes=$diasRestantes, hogarId=$hogarId, leida=$leida, fechaCreacion=$fechaCreacion)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Alerta) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
