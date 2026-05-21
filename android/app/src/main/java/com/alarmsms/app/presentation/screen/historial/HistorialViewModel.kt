package com.alarmsms.app.presentation.screen.historial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmsms.app.domain.model.Alarma
import com.alarmsms.app.domain.repository.AlarmaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistorialViewModel @Inject constructor(
    private val repository: AlarmaRepository
) : ViewModel() {

    val alarmasHistorial: StateFlow<List<Alarma>> = repository.getAlarmasFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
