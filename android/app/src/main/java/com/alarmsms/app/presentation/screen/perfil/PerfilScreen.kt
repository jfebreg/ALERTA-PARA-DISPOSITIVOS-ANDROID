package com.alarmsms.app.presentation.screen.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.alarmsms.app.domain.model.Config
import com.alarmsms.app.domain.repository.ConfigRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val configRepository: ConfigRepository
) : ViewModel() {

    var currentProfile by mutableStateOf(Config())
        private set

    var tempName by mutableStateOf("")
    var loggedOut by mutableStateOf(false)

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        viewModelScope.launch {
            val local = configRepository.getLocal()
            currentProfile = local
            tempName = local.miNombre
        }
    }

    fun guardarNuevoNombre() {
        viewModelScope.launch {
            val updated = currentProfile.copy(miNombre = tempName)
            configRepository.guardarLocal(updated)
            currentProfile = updated
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            // Clear local states
            configRepository.guardarLocal(Config())
            loggedOut = true
        }
    }
}

@Composable
fun PerfilScreen(
    onNavigateToSplash: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val prof = viewModel.currentProfile

    LaunchedEffect(viewModel.loggedOut) {
        if (viewModel.loggedOut) {
            onNavigateToSplash()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(Color(0xFFFFB4AB), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (prof.miRol == "ADMIN") "👨‍✈️" else "👷",
                fontSize = 44.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "TU CONFIGURACIÓN",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = "Rol asignado: ${prof.miRol}",
            fontSize = 12.sp,
            color = Color(0xFF8E9099)
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = viewModel.tempName,
            onValueChange = { viewModel.tempName = it },
            label = { Text("Tu Identidad Visual") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A3B3F)),
            onClick = { viewModel.guardarNuevoNombre() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("ACTUALIZAR APODO LOCAL", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2B2F)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("INFORMACIÓN DE REGISTRO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB4AB))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Número: ${prof.miTelefono}", color = Color.White, fontSize = 13.sp)
                Text("Identificador: ${prof.id.take(12)}...", color = Color(0xFF8E9099), fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5A1A1A)),
            onClick = { viewModel.cerrarSesion() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("CERRAR SESION SEGURO", fontWeight = FontWeight.Bold, color = Color.White)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}
