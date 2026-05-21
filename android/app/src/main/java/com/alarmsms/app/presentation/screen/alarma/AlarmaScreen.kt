package com.alarmsms.app.presentation.screen.alarma

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlarmaScreen(
    viewModel: AlarmaViewModel = hiltViewModel()
) {
    val liveACKs by viewModel.confirmaciones.collectAsState()

    // Soft alert pulse loop state
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRatio by infiniteTransition.animateFloat(
        initialValue = if (viewModel.isSending) 0.95f else 0.98f,
        targetValue = if (viewModel.isSending) 1.05f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alarmPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(
                text = "BOTÓN PANICO SMS",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Señalizará a ${viewModel.totalEnroladosActivos} contactos enrolados",
                fontSize = 13.sp,
                color = Color(0xFF8E9099),
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Circular panic button occupying ~60% height
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .scale(pulseRatio)
                    .background(Color(0x1ABA1A1A), shape = CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(bounded = false, radius = 140.dp),
                        onClick = { viewModel.dispararAlcanzePanico() }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(Color(0xFFBA1A1A), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "PRESIONAR",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AUXILIO",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xAAFFFFFF)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "🚨",
                            fontSize = 32.sp
                        )
                    }
                }
            }
        }

        // Live confirmation section footer
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2B2F)),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(bottom = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "RECEPCIONES CONFIRMADAS EN TIEMPO REAL:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFB4AB)
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                if (liveACKs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay confirmaciones aún para el pánico activo.",
                            color = Color(0xFF8E9099),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                    ) {
                        liveACKs.forEach { (userId, timestamp) ->
                            val readableTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "✅", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Usuario ($userId)",
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Text(
                                    text = readableTime,
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E9099)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
