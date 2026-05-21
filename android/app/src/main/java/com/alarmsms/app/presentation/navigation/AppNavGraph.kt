package com.alarmsms.app.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.alarmsms.app.presentation.screen.admin.AdminScreen
import com.alarmsms.app.presentation.screen.alarma.AlarmaScreen
import com.alarmsms.app.presentation.screen.historial.HistorialScreen
import com.alarmsms.app.presentation.screen.onboarding.EnrolamientoUsuarioScreen
import com.alarmsms.app.presentation.screen.onboarding.LoginAdminScreen
import com.alarmsms.app.presentation.screen.perfil.PerfilScreen
import com.alarmsms.app.presentation.screen.splash.SplashScreen

object Route {
    const val SPLASH = "splash"
    const val CHOOSE_PROFILE = "choose_profile"
    const val ADMIN_LOGIN = "admin_login"
    const val USER_ENROLL = "user_enroll"
    const val MAIN = "main/{rol}"
    
    fun mainRoute(rol: String) = "main/$rol"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Route.SPLASH
    ) {
        // 1. Cinematic Splash
        composable(Route.SPLASH) {
            SplashScreen(
                onNavigateToHome = { rol ->
                    navController.navigate(Route.mainRoute(rol)) {
                        popUpTo(Route.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Route.CHOOSE_PROFILE) {
                        popUpTo(Route.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Persona Selector Onboarding
        composable(Route.CHOOSE_PROFILE) {
            ChooseProfileScreen(
                onSelectAdmin = { navController.navigate(Route.ADMIN_LOGIN) },
                onSelectUser = { navController.navigate(Route.USER_ENROLL) }
            )
        }

        // 3. Admin Authentication Layout
        composable(Route.ADMIN_LOGIN) {
            LoginAdminScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { rol ->
                    navController.navigate(Route.mainRoute(rol)) {
                        popUpTo(Route.CHOOSE_PROFILE) { inclusive = true }
                    }
                }
            )
        }

        // 4. User Enrollment PIN input Layout
        composable(Route.USER_ENROLL) {
            EnrolamientoUsuarioScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = { rol ->
                    navController.navigate(Route.mainRoute(rol)) {
                        popUpTo(Route.CHOOSE_PROFILE) { inclusive = true }
                    }
                }
            )
        }

        // 5. Main Screen Hub containing Bottom Navigator tabs
        composable(Route.MAIN) { backStackEntry ->
            val userRole = backStackEntry.arguments?.getString("rol") ?: "USER"
            MainTabScreenContainer(
                role = userRole,
                onLogout = {
                    navController.navigate(Route.SPLASH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun ChooseProfileScreen(
    onSelectAdmin: () -> Unit,
    onSelectUser: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1F22))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🚨", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sms Alarma", fontSize = 32.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Black, color = Color.White)
            Text("Enrolar Dispositivo", fontSize = 14.sp, color = Color(0xFF8E9099))
            
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = onSelectUser,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("VINCULAR COMO USUARIO", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onSelectAdmin,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF8E9099))
            ) {
                Text("ACCEDER COMO ADMINISTRADOR", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun MainTabScreenContainer(
    role: String,
    onLogout: () -> Unit
) {
    var selectedScreenIndex by remember { mutableStateOf(0) }
    
    val tabs = mutableListOf("Alarma", "Historial")
    if (role == "ADMIN") {
        tabs.add("Admin")
    }
    tabs.add("Perfil")

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF2A2B2F),
                contentColor = Color.White
            ) {
                tabs.forEachIndexed { index, name ->
                    NavigationBarItem(
                        selected = selectedScreenIndex == index,
                        onClick = { selectedScreenIndex = index },
                        icon = {
                            Text(
                                text = when (name) {
                                    "Alarma" -> "🚨"
                                    "Historial" -> "📝"
                                    "Admin" -> "⚙️"
                                    else -> "👤"
                                },
                                fontSize = 20.sp
                            )
                        },
                        label = { Text(name, color = Color.White, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color(0xFFFFB4AB)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (tabs[selectedScreenIndex]) {
                "Alarma" -> AlarmaScreen()
                "Historial" -> HistorialScreen()
                "Admin" -> AdminScreen()
                else -> PerfilScreen(onNavigateToSplash = onLogout)
            }
        }
    }
}
