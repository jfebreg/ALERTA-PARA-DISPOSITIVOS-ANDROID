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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmsms.app.domain.model.Enrolado
import com.alarmsms.app.domain.repository.ConfigRepository
import com.alarmsms.app.domain.repository.EnroladoRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LoginAdminViewModel @Inject constructor(
    private val configRepository: ConfigRepository,
    private val enroladoRepository: EnroladoRepository
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var adminName by mutableStateOf("")
    var adminPhone by mutableStateOf("")
    var errorMsg by mutableStateOf<String?>(null)
    var success by mutableStateOf(false)
    var isProgress by mutableStateOf(false)

    fun loginAdmin() {
        if (email.isBlank() || password.isBlank() || adminName.isBlank() || adminPhone.isBlank()) {
            errorMsg = "Por favor, complete todos los campos requeridos."
            return
        }

        viewModelScope.launch {
            isProgress = true
            errorMsg = null
            try {
                // Perform sign-in or registration
                val authResult = try {
                    auth.signInWithEmailAndPassword(email, password).await()
                } catch (e: Exception) {
                    auth.createUserWithEmailAndPassword(email, password).await()
                }

                val uid = authResult.user?.uid ?: throw Exception("Error al procesar Auth de Firebase")

                // Pre-configure the singleton config if first run on Firestore
                configRepository.uploadConfigToFirestore(
                    msg = "¡ALERTA CRÍTICA: Emergencia detectada. Responda inmediatamente!",
                    kw = "ALARMA",
                    ttl = 24
                )

                val defaultAdminObj = Enrolado(
                    id = uid,
                    nombre = adminName,
                    telefono = adminPhone,
                    rol = "ADMIN",
                    activo = true,
                    creadoEn = System.currentTimeMillis(),
                    ultimaSync = System.currentTimeMillis()
                )
                enroladoRepository.registrarNuevoEnrolado(defaultAdminObj)

                // Save local preferences
                val updatedConfig = configRepository.getLocal().copy(
                    miNombre = adminName,
                    miTelefono = adminPhone,
                    miRol = "ADMIN"
                )
                configRepository.guardarLocal(updatedConfig)
                
                success = true
            } catch (e: Exception) {
                errorMsg = "Fallo de ingreso: ${e.localizedMessage ?: "Causa desconocida"}"
            } finally {
                isProgress = false
            }
        }
    }
}

@Composable
fun LoginAdminScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: (rol: String) -> Unit,
    viewModel: LoginAdminViewModel = hiltViewModel()
) {
    LaunchedEffect(viewModel.success) {
        if (viewModel.success) {
            onNavigateToHome("ADMIN")
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Registro Administrador", color = Color.White) },
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
                text = "🔑",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Administrar Red",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Crea o ingresa con tu cuenta de email para administrar la red de enrolados.",
                fontSize = 13.sp,
                color = Color(0xFF8E9099),
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = viewModel.adminName,
                onValueChange = { viewModel.adminName = it },
                label = { Text("Tu Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFB4AB),
                    unfocusedBorderColor = Color(0xFF8E9099)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.adminPhone,
                onValueChange = { viewModel.adminPhone = it },
                label = { Text("Tu Número Celular (e.g. +34600112233)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFB4AB),
                    unfocusedBorderColor = Color(0xFF8E9099)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = { Text("Correo Electrónico") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFB4AB),
                    unfocusedBorderColor = Color(0xFF8E9099)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("Contraseña (Min 6 caracteres)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFFB4AB),
                    unfocusedBorderColor = Color(0xFF8E9099)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    onClick = { viewModel.loginAdmin() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("AUTENTICAR ADMINISTRADOR", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onNavigateBack) {
                    Text("Volver a Selección", color = Color(0xFFFFB4AB))
                }
            }
        }
    }
}
