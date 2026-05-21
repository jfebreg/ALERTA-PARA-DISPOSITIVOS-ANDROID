package com.alarmsms.app.presentation.screen.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmsms.app.domain.repository.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    sealed class SplashState {
        object Idle : SplashState()
        object GoToOnboarding : SplashState()
        data class GoToHome(val rol: String) : SplashState()
    }

    private val _state = mutableStateOf<SplashState>(SplashState.Idle)
    val state: State<SplashState> = _state

    init {
        verificarSesion()
    }

    private fun verificarSesion() {
        viewModelScope.launch {
            delay(1500) // Beautiful cinematic entry pause
            val config = configRepository.getLocal()
            if (config.miRol.isNotEmpty() && config.miNombre.isNotEmpty()) {
                _state.value = SplashState.GoToHome(config.miRol)
            } else {
                _state.value = SplashState.GoToOnboarding
            }
        }
    }
}

@Composable
fun SplashScreen(
    onNavigateToHome: (rol: String) -> Unit,
    onNavigateToOnboarding: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state

    LaunchedEffect(state) {
        when (val curr = state) {
            is SplashViewModel.SplashState.GoToHome -> onNavigateToHome(curr.rol)
            is SplashViewModel.SplashState.GoToOnboarding -> onNavigateToOnboarding()
            SplashViewModel.SplashState.Idle -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "🚨",
                fontSize = 80.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sms Alarma",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sistema de Sirenas de Pánico",
                fontSize = 14.sp,
                color = Color(0xFF8E9099)
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(color = Color(0xFFFFB4AB))
        }
    }
}
