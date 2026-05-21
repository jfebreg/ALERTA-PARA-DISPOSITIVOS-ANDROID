package com.alarmsms.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alarmsms.app.domain.model.Config

@Entity(tableName = "configs")
data class ConfigEntity(
    @PrimaryKey val id: String = "singleton",
    val mensajePredefinido: String,
    val palabraClave: String,
    val miNombre: String,
    val miTelefono: String,
    val miRol: String
) {
    fun toDomain() = Config(
        id = id,
        mensajePredefinido = mensajePredefinido,
        palabraClave = palabraClave,
        miNombre = miNombre,
        miTelefono = miTelefono,
        miRol = miRol
    )

    companion object {
        fun fromDomain(config: Config) = ConfigEntity(
            id = config.id,
            mensajePredefinido = config.mensajePredefinido,
            palabraClave = config.palabraClave,
            miNombre = config.miNombre,
            miTelefono = config.miTelefono,
            miRol = config.miRol
        )
    }
}
