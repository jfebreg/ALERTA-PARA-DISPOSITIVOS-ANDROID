package com.alarmsms.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Pin(
    val pin: String,
    val nombreDestino: String,
    val usada: Boolean,
    val creadoPor: String,
    val creadoEn: Long,
    val expiraEn: Long,
    val usadaPor: String? = null
)
