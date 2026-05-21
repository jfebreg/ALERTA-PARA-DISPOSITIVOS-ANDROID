package com.alarmsms.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val id: String = "singleton",
    val mensajePredefinido: String = "ALERTA DE SISTEMA: ¡Peligro, actúe de inmediato!",
    val palabraClave: String = "ALARMA",
    val miNombre: String = "",
    val miTelefono: String = "",
    val miRol: String = "" // "ADMIN" or "USER" or empty if onboarding
)
