package com.alarmsms.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Alarma(
    val id: String,
    val emisorId: String,
    val emisorNombre: String,
    val mensaje: String,
    val enviadaEn: Long,
    val esPropia: Boolean = false,
    val confirmada: Boolean = false,
    val confirmaciones: Map<String, Long> = emptyMap() // Map of userId -> timestamp
)
