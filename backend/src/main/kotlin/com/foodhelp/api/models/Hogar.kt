package com.foodhelp.api.models

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "hogares")
class Hogar(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(nullable = false)
    var nombre: String,
    @Column(nullable = false, unique = true)
    val codigoHogar: String,
    @OneToMany(mappedBy = "hogar")
    var usuarios: MutableList<Usuario> = mutableListOf(),
    @OneToMany(mappedBy = "hogar", cascade = [CascadeType.ALL], orphanRemoval = true)
    var articulos: MutableList<ArticuloDespensa> = mutableListOf(),
    @Column(name = "propietario_id")
    var propietarioId: UUID? = null,
) {
    override fun toString(): String = "Hogar(id=$id, nombre='$nombre', codigoHogar='$codigoHogar')"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Hogar) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
