package com.alarmsms.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alarmsms.app.domain.model.Alarma

@Entity(tableName = "alarmas")
data class AlarmaEntity(
    @PrimaryKey val id: String,
    val emisorId: String,
    val emisorNombre: String,
    val mensaje: String,
    val enviadaEn: Long,
    val esPropia: Boolean,
    val confirmada: Boolean
) {
    fun toDomain() = Alarma(
        id = id,
        emisorId = emisorId,
        emisorNombre = emisorNombre,
        mensaje = mensaje,
        enviadaEn = enviadaEn,
        esPropia = esPropia,
        confirmada = confirmada
    )

    companion object {
        fun fromDomain(alarma: Alarma) = AlarmaEntity(
            id = alarma.id,
            emisorId = alarma.emisorId,
            emisorNombre = alarma.emisorNombre,
            mensaje = alarma.mensaje,
            enviadaEn = alarma.enviadaEn,
            esPropia = alarma.esPropia,
            confirmada = alarma.confirmada
        )
    }
}
