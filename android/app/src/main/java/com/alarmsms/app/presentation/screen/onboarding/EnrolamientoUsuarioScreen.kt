package com.alarmsms.app.presentation.screen.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmsms.app.domain.usecase.ValidarPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnrolamientoUsuarioViewModel @Inject constructor(
    private val validarPinUseCase: ValidarPinUseCase
) : ViewModel() {

    var telefono by mutableStateOf("")
    var pinCode by mutableStateOf("")
    var errorMsg by mutableStateOf<String?>(null)
    var success by mutableStateOf(false)
    var isProgress by mutableStateOf(false)

    fun validarEnrolamiento() {
        if (telefono.isBlank() || pinCode.length != 6) {
            errorMsg = "Por favor, complete tu teléfono y el PIN de 6 dígitos brindado por el Administrador."
            return
        }

        viewModelScope.launch {
            isProgress = true
            errorMsg = null
            
            val result = validarPinUseCase(telefono, pinCode)
            isProgress = false
            
            when (result) {
                is ValidarPinUseCase.Result.Success -> {
                    success = true
                }
                is ValidarPinUseCase.Result.Error -> {
                    errorMsg = result.message
                }
            }
        }
    }
}

@Composable
fun EnrolamientoUsuarioScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: (rol: String) -> Unit,
    viewModel: EnrolamientoUsuarioViewModel = hiltViewModel()
) {
    LaunchedEffect(viewModel.success) {
        if (viewModel.success) {
            onNavigateToHome("USER")
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Registrar Dispositivo", color = Color.White) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color(0xFF1E1F22))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1E1F22))
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "📲",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Enrolamiento de Usuario",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ingrese su número de teléfono celular actual y el PIN de enrolamiento emitido por el administrador de su red.",
                fontSize = 13.sp,
                color = Color(0xFF8E9099),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = viewModel.telefono,
                onValueChange = { viewModel.telefono = it },
                label = { Text("Número de Teléfono (e.g. +34600112233)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFB4AB),
                    unfocusedBorderColor = Color(0xFF8E9099)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = viewModel.pinCode,
                onValueChange = { if (it.length <= 6) viewModel.pinCode = it },
                label = { Text("PIN de Enrolamiento (6 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFB4AB),
                    unfocusedBorderColor = Color(0xFF8E9099)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.errorMsg != null) {
                Text(
                    text = viewModel.errorMsg ?: "",
                    color = Color(0xFFFF3B30),
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (viewModel.isProgress) {
                CircularProgressIndicator(color = Color(0xFFFFB4AB))
            } else {
                Button(
                    onClick = { viewModel.validarEnrolamiento() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("VALIDAR ENROLAMIENTO", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onNavigateBack) {
                    Text("Volver a Selección", color = Color(0xFFFFB4AB))
                }
            }
        }
    }
}
