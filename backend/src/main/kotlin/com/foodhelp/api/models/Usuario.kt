package com.foodhelp.api.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(nullable = false)
    val nombre: String,
    @Column(nullable = false)
    val contrasena: String,
    @Column(nullable = true, unique = true)
    val correo: String? = null,
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "hogar_id", nullable = true)
    val hogar: Hogar? = null,
) {
    override fun toString(): String = "Usuario(id=$id, nombre='$nombre', correo='$correo')"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Usuario) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
