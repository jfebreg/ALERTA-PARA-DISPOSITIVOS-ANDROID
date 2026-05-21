package com.alarmsms.app.presentation.screen.historial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.alarmsms.app.domain.model.Alarma
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistorialScreen(
    viewModel: HistorialViewModel = hiltViewModel()
) {
    val alarmas by viewModel.alarmasHistorial.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Emitidas", "Recibidas")

    val filteredList = remember(alarmas, selectedTab) {
        if (selectedTab == 0) {
            alarmas.filter { it.esPropia }
        } else {
            alarmas.filter { !it.esPropia }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22))
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF2A2B2F),
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFFFFB4AB)
                )
            }
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                )
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📭",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Historial Vacío",
                        color = Color(0xFF8E9099),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList) { alarma ->
                    ItemAlarmaCard(alarma = alarma)
                }
            }
        }
    }
}

@Composable
fun ItemAlarmaCard(alarma: Alarma) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(alarma.enviadaEn))

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2B2F)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (alarma.esPropia) "Pánico Emitido" else "Sirena Recibida",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (alarma.esPropia) Color(0xFFFFB4AB) else Color(0xFFFF5449)
                )
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = Color(0xFF8E9099)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Mensaje: ${alarma.mensaje}",
                fontSize = 13.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (alarma.esPropia) {
                    "Origen: Yo "
                } else {
                    "De: ${alarma.emisorNombre} - IP SMS"
                },
                fontSize = 12.sp,
                color = Color(0xFF8E9099)
            )
        }
    }
}
