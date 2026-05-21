package com.alarmsms.app.presentation.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.alarmsms.app.domain.model.Enrolado
import com.alarmsms.app.domain.model.Pin
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: AdminViewModel = hiltViewModel()
) {
    var adminTab by remember { mutableStateOf(0) }
    val adminTabTitles = listOf("Enrolados", "PINs de Registro", "Ajustes de Red")

    var showAddEnroladoDialog by remember { mutableStateOf(false) }
    var showGeneratePinDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (adminTab == 0) {
                FloatingActionButton(
                    onClick = { showAddEnroladoDialog = true },
                    containerColor = Color(0xFFBA1A1A),
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Manual setup")
                }
            } else if (adminTab == 1) {
                FloatingActionButton(
                    onClick = { showGeneratePinDialog = true },
                    containerColor = Color(0xFFBA1A1A),
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Generate PIN")
                }
            }
        },
        containerColor = Color(0xFF1E1F22)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = adminTab,
                containerColor = Color(0xFF2A2B2F),
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[adminTab]),
                        color = Color(0xFFFFB4AB)
                    )
                }
            ) {
                adminTabTitles.forEachIndexed { idx, title ->
                    Tab(
                        selected = adminTab == idx,
                        onClick = { adminTab = idx },
                        text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                    )
                }
            }

            when (adminTab) {
                0 -> EnroladosTabContent(viewModel)
                1 -> PinesTabContent(viewModel)
                2 -> ConfigTabContent(viewModel)
            }
        }

        // Add manual enrolado dialog
        if (showAddEnroladoDialog) {
            var tempName by remember { mutableStateOf("") }
            var tempPhone by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showAddEnroladoDialog = false },
                containerColor = Color(0xFF2A2B2F),
                title = { Text("Registrar Enrolado", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ingrese un contacto autorizado directamente.", color = Color(0xFF8E9099), fontSize = 13.sp)
                        OutlinedTextField(
                            value = tempName,
                            onValueChange = { tempName = it },
                            label = { Text("Nombre") }
                        )
                        OutlinedTextField(
                            value = tempPhone,
                            onValueChange = { tempPhone = it },
                            label = { Text("Número Celular") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                        onClick = {
                            if (tempName.isNotBlank() && tempPhone.isNotBlank()) {
                                viewModel.addManualEnrolado(tempName, tempPhone)
                                showAddEnroladoDialog = false
                            }
                        }
                    ) { Text("Confirmar") }
                },
                dismissButton = {
                    TextButton(onClick = { showAddEnroladoDialog = false }) {
                        Text("Cancelar", color = Color(0xFFFFB4AB))
                    }
                }
            )
        }

        // Generate OTP PIN Dialog
        if (showGeneratePinDialog) {
            var tempDestiny by remember { mutableStateOf("") }
            var ttlHrs by remember { mutableStateOf(24f) }

            AlertDialog(
                onDismissRequest = { showGeneratePinDialog = false },
                containerColor = Color(0xFF2A2B2F),
                title = { Text("Generar PIN de Enrolamiento", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Genera un PIN de 6 dígitos que expira.", color = Color(0xFF8E9099), fontSize = 13.sp)
                        OutlinedTextField(
                            value = tempDestiny,
                            onValueChange = { tempDestiny = it },
                            label = { Text("Nombre del Destinado") }
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Vencimiento del PIN: ${ttlHrs.toInt()} horas", color = Color.White, fontSize = 13.sp)
                        Slider(
                            value = ttlHrs,
                            onValueChange = { ttlHrs = it },
                            valueRange = 1f..72f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFB4AB),
                                activeTrackColor = Color(0xFFBA1A1A)
                            )
                        )
                        
                        viewModel.generatedOtpString?.let { code ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF1E1F22))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "PIN CREADO: $code",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFCC00)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                        onClick = {
                            if (viewModel.generatedOtpString != null) {
                                viewModel.generatedOtpString = null
                                showGeneratePinDialog = false
                            } else if (tempDestiny.isNotBlank()) {
                                viewModel.triggerPinGeneration(tempDestiny, ttlHrs.toInt())
                            }
                        }
                    ) {
                        Text(if (viewModel.generatedOtpString != null) "Listo" else "Generar PIN")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        viewModel.generatedOtpString = null
                        showGeneratePinDialog = false
                    }) {
                        Text("Cerrar", color = Color(0xFFFFB4AB))
                    }
                }
            )
        }
    }
}

@Composable
fun EnroladosTabContent(viewModel: AdminViewModel) {
    val itemsList by viewModel.enrolados.collectAsState()

    if (itemsList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay contactos enrolados.", color = Color(0xFF8E9099))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(itemsList) { person ->
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2B2F))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(person.nombre, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Tel: ${person.telefono}", fontSize = 13.sp, color = Color(0xFF8E9099))
                            Text("Rol: ${person.rol}", fontSize = 11.sp, color = Color(0xFFFFB4AB))
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (person.activo) "ACTIVO" else "INACTIVO",
                                fontSize = 11.sp,
                                color = if (person.activo) Color.Green else Color.Gray,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Switch(
                                checked = person.activo,
                                onCheckedChange = { viewModel.toggleEnroladoStatus(person.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFFBA1A1A)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinesTabContent(viewModel: AdminViewModel) {
    val pines = viewModel.pinesList

    LaunchedEffect(Unit) {
        viewModel.cargarPinesFirebase()
    }

    if (pines.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay PINs de invitación activos.", color = Color(0xFF8E9099))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pines) { pinVal ->
                val df = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
                val limit = df.format(Date(pinVal.expiraEn))

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2B2F))) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("PIN: ${pinVal.pin}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFCC00))
                            Text("Asignado a: ${pinVal.nombreDestino}", fontSize = 13.sp, color = Color.White)
                            Text("Vence: $limit", fontSize = 11.sp, color = Color(0xFF8E9099))
                        }
                        
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF400000)),
                            onClick = { viewModel.revokePin(pinVal.pin) }
                        ) {
                            Text("Revocar", fontSize = 11.sp, color = Color.Red)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConfigTabContent(viewModel: AdminViewModel) {
    val cfg = viewModel.currentConfig
    var tempMsg by remember(cfg) { mutableStateOf(cfg.mensajePredefinido) }
    var tempKw by remember(cfg) { mutableStateOf(cfg.palabraClave) }
    var tempTtl by remember(cfg) { mutableStateOf(24f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Configuración de Eventos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)

        OutlinedTextField(
            value = tempMsg,
            onValueChange = { tempMsg = it },
            label = { Text("Mensaje Predefinido de Alerta") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = tempKw,
            onValueChange = { tempKw = it },
            label = { Text("Palabra Clave SMS (Detonador)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Horas de Valides del PIN: ${tempTtl.toInt()}h", color = Color.White)
        }
        
        Slider(
            value = tempTtl,
            onValueChange = { tempTtl = it },
            valueRange = 1f..72f,
            colors = SliderDefaults.colors(
                thumbColor = Color(0xFFFFB4AB),
                activeTrackColor = Color(0xFFBA1A1A)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
            onClick = { viewModel.updateSystemConfiguration(tempMsg, tempKw, tempTtl.toInt()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("GUARDAR AJUSTES DE RED", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}
