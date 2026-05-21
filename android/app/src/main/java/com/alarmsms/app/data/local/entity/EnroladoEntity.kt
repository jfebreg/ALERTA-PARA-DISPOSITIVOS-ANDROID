package com.alarmsms.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alarmsms.app.domain.model.Enrolado

@Entity(tableName = "enrolados")
data class EnroladoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val telefono: String,
    val rol: String,
    val activo: Boolean,
    val ultimaSync: Long
) {
    fun toDomain() = Enrolado(
        id = id,
        nombre = nombre,
        telefono = telefono,
        rol = rol,
        activo = activo,
        creadoEn = 0L, // local cache is light context-only
        ultimaSync = ultimaSync
    )

    companion object {
        fun fromDomain(enrolado: Enrolado) = EnroladoEntity(
            id = enrolado.id,
            nombre = enrolado.nombre,
            telefono = enrolado.telefono,
            rol = enrolado.rol,
            activo = enrolado.activo,
            ultimaSync = enrolado.ultimaSync
        )
    }
}
