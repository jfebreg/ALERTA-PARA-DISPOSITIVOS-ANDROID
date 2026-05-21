package com.alarmsms.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Enrolado(
    val id: String,
    val nombre: String,
    val telefono: String,
    val rol: String, // "ADMIN" or "USER"
    val activo: Boolean,
    val creadoEn: Long,
    val ultimaSync: Long
)
