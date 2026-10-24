package com.foodhelp.api.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "articulos_despensa")
class ArticuloDespensa(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(nullable = false)
    var nombre: String,
    @Column(nullable = false)
    var fechaVencimiento: LocalDate? = null,
    @Column(nullable = false)
    var cantidad: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var categoria: CategoriaEnum = CategoriaEnum.OTROS,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hogar_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    val hogar: Hogar,
) {
    override fun toString(): String =
        "ArticuloDespensa(id=$id, nombre='$nombre', fechaVencimiento=$fechaVencimiento, cantidad=$cantidad, categoria=$categoria)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArticuloDespensa) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id?.hashCode() ?: 0
}
