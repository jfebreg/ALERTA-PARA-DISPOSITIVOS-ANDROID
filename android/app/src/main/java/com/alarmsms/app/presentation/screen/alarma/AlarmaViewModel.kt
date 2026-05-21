package com.alarmsms.app.presentation.screen.alarma

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmsms.app.domain.model.Alarma
import com.alarmsms.app.domain.repository.EnroladoRepository
import com.alarmsms.app.domain.usecase.EmitirAlarmaUseCase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmaViewModel @Inject constructor(
    private val emitirAlarmaUseCase: EmitirAlarmaUseCase,
    private val enroladoRepo: EnroladoRepository
) : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    var totalEnroladosActivos by mutableIntStateOf(0)
    var isSending by mutableStateOf(false)
    var activeAlarmId by mutableStateOf<String?>(null)
    
    // Live confirmation registry maps user ID -> acknowledgment timestamp
    private val _confirmaciones = MutableStateFlow<Map<String, Long>>(emptyMap())
    val confirmaciones: StateFlow<Map<String, Long>> = _confirmaciones

    init {
        fetchTotalEnrolados()
    }

    private fun fetchTotalEnrolados() {
        viewModelScope.launch {
            val list = enroladoRepo.getActivosLocal()
            totalEnroladosActivos = list.size
        }
    }

    fun dispararAlcanzePanico(mensajeCustom: String? = null) {
        viewModelScope.launch {
            isSending = true
            try {
                val alarmaId = emitirAlarmaUseCase(mensajeCustom)
                activeAlarmId = alarmaId
                suscribirAConsultaDeConfirmaciones(alarmaId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSending = false
            }
        }
    }

    private fun suscribirAConsultaDeConfirmaciones(alarmaId: String) {
        firestore.collection("alarmas").document(alarmaId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    error.printStackTrace()
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val mappings = snapshot.get("confirmaciones") as? Map<String, Long> ?: emptyMap()
                    _confirmaciones.value = mappings
                }
            }
    }
}
