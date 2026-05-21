package com.alarmsms.app.presentation.screen.admin

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmsms.app.data.remote.FirestorePinSource
import com.alarmsms.app.domain.model.Config
import com.alarmsms.app.domain.model.Enrolado
import com.alarmsms.app.domain.model.Pin
import com.alarmsms.app.domain.repository.ConfigRepository
import com.alarmsms.app.domain.repository.EnroladoRepository
import com.alarmsms.app.domain.usecase.GenerarPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val configRepo: ConfigRepository,
    private val enroladoRepo: EnroladoRepository,
    private val generarPinUseCase: GenerarPinUseCase,
    private val pinSource: FirestorePinSource
) : ViewModel() {

    // Enrolados state
    val enrolados: StateFlow<List<Enrolado>> = enroladoRepo.getEnroladosLocalFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Pines state
    var pinesList by mutableStateOf<List<Pin>>(emptyList())
        private set

    // Config singleton state
    var currentConfig by mutableStateOf(Config())
        private set

    // New PIN dialog parameters
    var targetNewName by mutableStateOf("")
    var generatedOtpString by mutableStateOf<String?>(null)

    // Manual enrolado creation parameters
    var manualName by mutableStateOf("")
    var manualPhone by mutableStateOf("")

    init {
        recuperarConfigLocal()
        cargarPinesFirebase()
    }

    private fun recuperarConfigLocal() {
        viewModelScope.launch {
            currentConfig = configRepo.getLocal()
        }
    }

    fun cargarPinesFirebase() {
        viewModelScope.launch {
            try {
                pinesList = pinSource.getPinesActivos()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Config workflows
    fun updateSystemConfiguration(msg: String, kw: String, ttl: Int) {
        viewModelScope.launch {
            configRepo.uploadConfigToFirestore(msg, kw, ttl)
            currentConfig = configRepo.getLocal()
        }
    }

    // Enrolados workflows
    fun toggleEnroladoStatus(userId: String, activeState: Boolean) {
        viewModelScope.launch {
            enroladoRepo.logicToggleActivo(userId, activeState)
        }
    }

    fun deleteEnrolado(userId: String) {
        viewModelScope.launch {
            enroladoRepo.logicEliminar(userId)
        }
    }

    fun addManualEnrolado(name: String, phone: String) {
        viewModelScope.launch {
            val virtualUid = "MANUAL_ID_${System.currentTimeMillis()}"
            val model = Enrolado(
                id = virtualUid,
                nombre = name,
                telefono = phone,
                rol = "USER",
                activo = true,
                creadoEn = System.currentTimeMillis(),
                ultimaSync = System.currentTimeMillis()
            )
            enroladoRepo.registrarNuevoEnrolado(model)
        }
    }

    // OTP workflows
    fun triggerPinGeneration(targetName: String, ttlHours: Int) {
        viewModelScope.launch {
            val code = generarPinUseCase(targetName, ttlHours)
            generatedOtpString = code
            cargarPinesFirebase() // Refresh
        }
    }

    fun revokePin(pinCode: String) {
        viewModelScope.launch {
            pinSource.invalidarPin(pinCode)
            cargarPinesFirebase() // Refresh
        }
    }
}
