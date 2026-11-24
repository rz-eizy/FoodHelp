package com.foodhelp.api.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "hogar_expulsados",
    uniqueConstraints = [UniqueConstraint(name = "uk_hogar_expulsado", columnNames = ["hogar_id", "usuario_id"])],
)
class HogarExpulsado(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,
    @Column(name = "hogar_id", nullable = false)
    val hogarId: UUID,
    @Column(name = "usuario_id", nullable = false)
    val usuarioId: UUID,
    @Column(name = "expulsado_en", nullable = false)
    val expulsadoEn: Instant,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HogarExpulsado) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
