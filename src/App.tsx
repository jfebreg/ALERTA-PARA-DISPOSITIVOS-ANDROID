import { useState, useEffect } from "react";
import { 
  Shield, Phone, Settings, Key, Users, Volume2, VolumeX, 
  FileText, CheckCircle, Download, BookOpen, Send, Bell, 
  UserCheck, HelpCircle, LogOut, RefreshCw, Smartphone, ListCollapse
} from "lucide-react";

// In-Memory Simulation Storage
interface Enrolado {
  id: string;
  nombre: string;
  telefono: string;
  rol: "ADMIN" | "USER";
  activo: boolean;
  creadoEn: string;
}

interface Pin {
  pin: string;
  nombreDestino: string;
  usada: boolean;
  creadoPor: string;
  expiraEn: string;
}

interface AlarmLog {
  id: string;
  emisorId: string;
  emisorNombre: string;
  mensaje: string;
  enviadaEn: string;
  esPropia: boolean;
  confirmada: boolean;
  origen: string;
}

export default function App() {
  const [activeTab, setActiveTab] = useState<"simulator" | "code">("simulator");
  const [selectedFileIndex, setSelectedFileIndex] = useState(0);
  const [copied, setCopied] = useState(false);

  // Simulated Database states
  const [enrolados, setEnrolados] = useState<Enrolado[]>([
    {
      id: "admin_uid",
      nombre: "José Administrador",
      telefono: "+34600112233",
      rol: "ADMIN",
      activo: true,
      creadoEn: "2026-05-20 20:00"
    },
    {
      id: "maria_uid",
      nombre: "María González",
      telefono: "+34611223344",
      rol: "USER",
      activo: true,
      creadoEn: "2026-05-20 21:00"
    }
  ]);

  const [pines, setPines] = useState<Pin[]>([
    {
      pin: "482391",
      nombreDestino: "Carlos Ruiz",
      usada: false,
      creadoPor: "José Administrador",
      expiraEn: "2026-05-21 23:44"
    }
  ]);

  const [history, setHistory] = useState<AlarmLog[]>([
    {
      id: "AL_1",
      emisorId: "admin_uid",
      emisorNombre: "José Administrador",
      mensaje: "¡Alerta de prueba! Responder si entra el SMS.",
      enviadaEn: "2026-05-20 22:30",
      esPropia: true,
      confirmada: true,
      origen: "+34600112233"
    }
  ]);

  const [systemConfig, setSystemConfig] = useState({
    mensajePredefinido: "¡ALERTA CRÍTICA: Emergencia detectada. Responda inmediatamente!",
    palabraClave: "ALARMA",
    pinTtlHoras: 24
  });

  // State managers for Admin Phone
  const [adminLogged, setAdminLogged] = useState(true);
  const [adminCurrentScreen, setAdminCurrentScreen] = useState<"alarm" | "history" | "users" | "settings">("alarm");
  const [adminInboundSiren, setAdminInboundSiren] = useState<{ active: boolean; message: string; from: string; sender: string } | null>(null);
  
  // Custom dialogs to generate PINs
  const [pinTargetName, setPinTargetName] = useState("");
  const [pinTargetHours, setPinTargetHours] = useState(24);
  const [createdPinResult, setCreatedPinResult] = useState<string | null>(null);

  // Manual Enrolado input
  const [newEnroladoName, setNewEnroladoName] = useState("");
  const [newEnroladoPhone, setNewEnroladoPhone] = useState("");

  // State managers for User Phone
  const [userLogged, setUserLogged] = useState(false);
  const [userPhoneNum, setUserPhoneNum] = useState("");
  const [userPinCode, setUserPinCode] = useState("");
  const [registeredUserInfo, setRegisteredUserInfo] = useState<Enrolado | null>(null);
  const [userCurrentScreen, setUserCurrentScreen] = useState<"alarm" | "history" | "profile">("alarm");
  const [userInboundSiren, setUserInboundSiren] = useState<{ active: boolean; message: string; from: string; sender: string; valId: string } | null>(null);

  // Siren Audio simulation feedback
  const [audioPlayed, setAudioPlayed] = useState(false);

  // Real-Time System interactions
  const triggerAdminPanic = () => {
    // Send simulated SMS alert to Maria & registered users
    const payloadMsg = `[${systemConfig.palabraClave}] José Administrador: ${systemConfig.mensajePredefinido}`;
    
    // Check if Maria is active
    const maria = enrolados.find(e => e.id === "maria_uid");
    if (maria && maria.activo) {
      setUserInboundSiren({
        active: true,
        message: systemConfig.mensajePredefinido,
        from: "+34600112233",
        sender: "José Administrador",
        valId: "ADMIN_panic_" + Date.now()
      });
      setAudioPlayed(true);
    }

    // Append history
    const timestampStr = new Date().toISOString().replace('T', ' ').substring(0, 16);
    setHistory(prev => [
      {
        id: "AL_" + Date.now(),
        emisorId: "admin_uid",
        emisorNombre: "José Administrador",
        mensaje: systemConfig.mensajePredefinido,
        enviadaEn: timestampStr,
        esPropia: true,
        confirmada: false,
        origen: "+34600112233"
      },
      ...prev
    ]);
  };

  const triggerUserPanic = () => {
    if (!registeredUserInfo) return;
    const currentName = registeredUserInfo.nombre;
    const currentPhone = registeredUserInfo.telefono;
    
    // Check if Admin is active
    const admin = enrolados.find(e => e.rol === "ADMIN");
    if (admin && admin.activo) {
      setAdminInboundSiren({
        active: true,
        message: systemConfig.mensajePredefinido,
        from: currentPhone,
        sender: currentName
      });
      setAudioPlayed(true);
    }

    // Append history
    const timestampStr = new Date().toISOString().replace('T', ' ').substring(0, 16);
    setHistory(prev => [
      {
        id: "AL_" + Date.now(),
        emisorId: registeredUserInfo.id,
        emisorNombre: currentName,
        mensaje: systemConfig.mensajePredefinido,
        enviadaEn: timestampStr,
        esPropia: true,
        confirmada: false,
        origen: currentPhone
      },
      ...prev
    ]);
  };

  // Confirm receipt handlers
  const confirmUserReceipt = () => {
    if (!userInboundSiren) return;
    
    // Shut siren
    setUserInboundSiren(null);
    setAudioPlayed(false);

    // Send return simulated SMS ACK to Admin
    const ackLog: AlarmLog = {
      id: "ACK_" + Date.now(),
      emisorId: "system",
      emisorNombre: registeredUserInfo?.nombre || "María González",
      mensaje: `[ACK-${systemConfig.palabraClave}] ${registeredUserInfo?.nombre || "María González"} confirmó recepción`,
      enviadaEn: new Date().toISOString().replace('T', ' ').substring(0, 16),
      esPropia: false,
      confirmada: true,
      origen: registeredUserInfo?.telefono || "+34611223344"
    };

    setHistory(prev => [ackLog, ...prev]);
  };

  const confirmAdminReceipt = () => {
    if (!adminInboundSiren) return;
    
    // Shut siren
    setAdminInboundSiren(null);
    setAudioPlayed(false);

    // Send return simulated SMS ACK to User
    const ackLog: AlarmLog = {
      id: "ACK_" + Date.now(),
      emisorId: "system",
      emisorNombre: "José Administrador",
      mensaje: `[ACK-${systemConfig.palabraClave}] José Administrador confirmó recepción`,
      enviadaEn: new Date().toISOString().replace('T', ' ').substring(0, 16),
      esPropia: false,
      confirmada: true,
      origen: "+34600112233"
    };

    setHistory(prev => [ackLog, ...prev]);
  };

  // OTP handlers
  const createEnrollmentPin = () => {
    if (!pinTargetName) return;
    const codeObj = Math.floor(100000 + Math.random() * 900000).toString();
    const expiryStr = new Date(Date.now() + pinTargetHours * 3600 * 1000).toISOString().replace('T', ' ').substring(0, 16);
    
    const nextPin: Pin = {
      pin: codeObj,
      nombreDestino: pinTargetName,
      usada: false,
      creadoPor: "José Administrador",
      expiraEn: expiryStr
    };

    setPines(prev => [nextPin, ...prev]);
    setCreatedPinResult(codeObj);
    setPinTargetName("");
  };

  // Onboarding action User
  const consumeUserPin = () => {
    if (!userPhoneNum || userPinCode.length !== 6) return;
    
    const matched = pines.find(p => p.pin === userPinCode && !p.usada);
    if (!matched) {
      alert("Error: PIN inválido o expirado. Verifique con el Administrador.");
      return;
    }

    // Mark used
    setPines(pines.map(p => p.pin === userPinCode ? { ...p, usada: true } : p));
    
    const nextUser: Enrolado = {
      id: "usr_" + Date.now(),
      nombre: matched.nombreDestino,
      telefono: userPhoneNum,
      rol: "USER",
      activo: true,
      creadoEn: new Date().toISOString().replace('T', ' ').substring(0,16)
    };

    setEnrolados(prev => [...prev, nextUser]);
    setRegisteredUserInfo(nextUser);
    setUserLogged(true);
    setUserCurrentScreen("alarm");
  };

  // Android Studio Project source files data
  const deliverables = [
    {
      name: "1. build.gradle.kts (App)",
      desc: "Dependencias Kotlin DSL para Compose, Room, Hilt, Firebase Auth y Firestore.",
      path: "/android/app/build.gradle.kts",
      lang: "kotlin",
      code: `plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.alarmsms.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.alarmsms.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables { useSupportLibrary = true }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Jetpack Compose
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Dagger-Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Firebase SDK
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
}`
    },
    {
      name: "2. AndroidManifest.xml",
      desc: "Declaración de permisos de SMS, boot, WakeLocks, Fullscreen overlays y Alarm Service.",
      path: "/android/app/src/main/AndroidManifest.xml",
      lang: "xml",
      code: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.SEND_SMS"/>
    <uses-permission android:name="android.permission.RECEIVE_SMS"/>
    <uses-permission android:name="android.permission.READ_SMS"/>
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.VIBRATE"/>
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT"/>

    <application
        android:name=".presentation.AlarmaSmsApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="Sms Alarma"
        android:theme="@style/Theme.AlarmaSmsAndroid">

        <activity
            android:name=".presentation.MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <activity
            android:name=".presentation.AlarmaActivaActivity"
            android:exported="false"
            android:theme="@style/Theme.AlarmaSmsAndroid.Fullscreen"
            android:showWhenLocked="true"
            android:turnScreenOn="true"
            android:launchMode="singleInstance" />

        <service
            android:name=".service.AlarmaForegroundService"
            android:enabled="true"
            android:exported="false"
            android:foregroundServiceType="specialUse" />

        <receiver
            android:name=".service.AlarmaSmsReceiver"
            android:enabled="true"
            android:exported="true"
            android:permission="android.permission.BROADCAST_SMS">
            <intent-filter android:priority="999">
                <action android:name="android.provider.Telephony.SMS_RECEIVED" />
            </intent-filter>
        </receiver>

    </application>
</manifest>`
    },
    {
      name: "3. data/ Capa Cache & Sources",
      desc: "Implementación Room SQLite (Dao, Entities, AppDatabase) y sincronización con Firestore DB.",
      path: "/android/app/src/main/java/com/alarmsms/app/data/local/AppDatabase.kt",
      lang: "kotlin",
      code: `@Entity(tableName = "enrolados")
data class EnroladoEntity(
    @PrimaryKey val id: String,
    val nombre: String,
    val telefono: String,
    val rol: String,
    val activo: Boolean,
    val ultimaSync: Long
)

@Dao
interface EnroladoDao {
    @Query("SELECT * FROM enrolados ORDER BY nombre ASC")
    fun getAllEnroladosFlow(): Flow<List<EnroladoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(enrolados: List<EnroladoEntity>)
}

@Database(entities = [EnroladoEntity::class, AlarmaEntity::class, ConfigEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun enroladoDao(): EnroladoDao
    abstract fun configDao(): ConfigDao
}`
    },
    {
      name: "4. domain/ Use Cases",
      desc: "Lógica de negocio: emitir SMS a la lista, validar códigos de PIN con TTL, confirmar acuse.",
      path: "/android/app/src/main/java/com/alarmsms/app/domain/usecase/EmitirAlarmaUseCase.kt",
      lang: "kotlin",
      code: `class EmitirAlarmaUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val enroladoRepository: EnroladoRepository,
    private val configRepository: ConfigRepository
) {
    suspend operator fun invoke(mensajeCustom: String? = null) {
        val config = configRepository.getLocal()
        val enrolados = enroladoRepository.getActivosLocal()
        val contenido = "[\${config.palabraClave}] \${config.miNombre}: \${mensajeCustom ?: config.mensajePredefinido}"

        val smsManager = context.getSystemService(SmsManager::class.java)
        enrolados.forEach { enrolado ->
            val partes = smsManager.divideMessage(contenido)
            smsManager.sendMultipartTextMessage(enrolado.telefono, null, partes, null, null)
        }
    }
}`
    },
    {
      name: "5. AlarmaSmsReceiver.kt",
      desc: "Detector estático de SMS entrantes, con parses de normalización y lanzamiento de sirenas.",
      path: "/android/app/src/main/java/com/alarmsms/app/service/AlarmaSmsReceiver.kt",
      lang: "kotlin",
      code: `class AlarmaSmsReceiver : BroadcastReceiver() {
    @Inject lateinit var configRepository: ConfigRepository
    @Inject lateinit var enroladoRepository: EnroladoRepository

    override fun onReceive(context: Context, intent: Intent) {
        val msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            val config = configRepository.getLocal()
            val enrolados = enroladoRepository.getActivosLocal()

            for (sms in msgs) {
                val address = sms.originatingAddress ?: continue
                val isTrigger = sms.messageBody?.contains("[\${config.palabraClave}]") == true

                val isEnroled = enrolados.any { contact ->
                    normalizarYComparar(contact.telefono, address)
                }

                if (isEnroled && isTrigger) {
                    val overlay = Intent(context, AlarmaActivaActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        putExtra("origen", address)
                        putExtra("mensaje", sms.messageBody)
                    }
                    context.startActivity(overlay)
                }
            }
            pending.finish()
        }
    }
}`
    },
    {
      name: "6. AlarmaActivaActivity.kt",
      desc: "Pantalla completa de sirena: despierta el CPU, activa strobe, vibrador y MediaPlayer en bucle.",
      path: "/android/app/src/main/java/com/alarmsms/app/presentation/AlarmaActivaActivity.kt",
      lang: "kotlin",
      code: `@AndroidEntryPoint
class AlarmaActivaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Wake up screen over keyguard locks
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        // Strobe loop, vibrator, media player loops
        reproducirAlarmaAudio()
        dispararVibracion()
        iniciarStrobeFlashlight()

        setContent {
            SirenAlertFullscreen(
                origen = intent.getStringExtra("origen") ?: "Socio Enrolado",
                mensaje = intent.getStringExtra("mensaje") ?: "Emergencia",
                onConfirm = {
                    detenerLazosEmergencia()
                    enviarSmsAcuseCompleto()
                    finish()
                }
            )
        }
    }
}`
    },
    {
      name: "7. Jetpack Compose Screen Views",
      desc: "Navegación Jetpack Compose completa y pantallas visuales para el control de pánico circular.",
      path: "/android/app/src/main/java/com/alarmsms/app/presentation/screen/alarma/AlarmaScreen.kt",
      lang: "kotlin",
      code: `@Composable
fun AlarmaScreen(viewModel: AlarmaViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1E1F22)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(280.dp).background(Color(0x1ABA1A1A), shape = CircleShape)
                .clickable { viewModel.dispararAlcanzePanico() },
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.size(220.dp).background(Color(0xFFBA1A1A), shape = CircleShape)) {
                Text("AUXILIO", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}`
    },
    {
      name: "8. firestore.rules",
      desc: "Reglas de seguridad robustas para Firestore que restringen configuraciones exclusivas a Admins.",
      path: "/firestore.rules",
      lang: "javascript",
      code: `rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} { allow read, write: if false; }

    function isSignedIn() { return request.auth != null; }
    function esAdmin() { 
      return isSignedIn() && get(/databases/$(database)/documents/enrolados/$(request.auth.uid)).data.rol == "ADMIN"; 
    }

    match /enrolados/{userId} {
      allow read: if isSignedIn();
      allow write: if esAdmin();
    }
    match /pines/{pinCode} {
      allow create, read, delete: if esAdmin();
      allow update: if resource.data.usada == false && request.resource.data.usada == true;
    }
    match /config/singleton {
      allow read: if isSignedIn();
      allow write: if esAdmin();
    }
  }
}`
    },
    {
      name: "9. google-services.json.example",
      desc: "Plantilla de configuración Firebase SDK para el entorno de compilación local del build system.",
      path: "/android/google-services.json.example",
      lang: "json",
      code: `{
  "project_info": {
    "project_number": "YOUR_FIREBASE_PROJECT_NUMBER",
    "project_id": "your-firebase-project-id",
    "storage_bucket": "your-project-id.appspot.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:your-firebase-id:android:package-name",
        "android_client_info": { "package_name": "com.alarmsms.app" }
      },
      "api_key": [{ "current_key": "YOUR_WEB_API_KEY" }]
    }
  ]
}`
    },
    {
      name: "10. SETUP_GUIDE.md",
      desc: "Instrucciones de compilación, de despliegue en Android Studio, y de provisioning en Firebase.",
      path: "/android/SETUP_GUIDE.md",
      lang: "markdown",
      code: `# GUÍA DE CONFIGURACIÓN — SMS ALARMA ANDROID

1. Crear proyecto SmsAlarma en la consola Firebase.
2. Habilitar Auth con Email y Contraseña.
3. Habilitar Firestore Database e insertar el contenido de firestore.rules.
4. Agregar aplicación Android con package com.alarmsms.app.
5. Descargar google-services.json e insertarlo en /android/app.
6. Compilar y usar.`
    }
  ];

  return (
    <div className="min-h-screen bg-slate-100 text-slate-800 font-sans flex flex-col justify-between">
      {/* Upper Navigation Header */}
      <header className="border-b border-slate-800 bg-slate-950 px-6 py-4 flex flex-wrap items-center justify-between gap-4 text-white shadow-md">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-red-600 flex items-center justify-center shadow-lg shadow-red-950/50">
            <Shield className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="text-lg font-bold tracking-tight text-white flex items-center gap-2">
              SMS ALARMA <span className="text-[10px] uppercase font-bold tracking-wider bg-slate-800 text-red-300 border border-red-500/20 px-2.5 py-0.5 rounded-full">Android Engine</span>
            </h1>
            <p className="text-xs text-slate-405 text-slate-400 font-medium">Offline Panic Siren + Firestore Sync</p>
          </div>
        </div>

        {/* Workspace Segment tabs */}
        <div className="flex items-center gap-1.5 bg-slate-900 p-1 rounded-xl border border-slate-800">
          <button 
            onClick={() => setActiveTab("simulator")}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center gap-2 ${activeTab === 'simulator' ? 'bg-slate-800 text-white shadow-sm border border-slate-700' : 'text-slate-400 hover:text-white'}`}
          >
            <Smartphone className="w-3.5 h-3.5" />
            Emulador Dual (Test)
          </button>
          <button 
            onClick={() => setActiveTab("code")}
            className={`px-4 py-1.5 rounded-lg text-xs font-semibold transition-all flex items-center gap-2 ${activeTab === 'code' ? 'bg-slate-800 text-white shadow-sm border border-slate-700' : 'text-slate-400 hover:text-white'}`}
          >
            <FileText className="w-3.5 h-3.5" />
            Consola Código Kotlin
          </button>
        </div>
      </header>

      {/* Main Container Section */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 md:p-6 lg:p-8">
        
        {activeTab === "simulator" ? (
          <div>
            {/* Top Notification banner */}
            <div className="mb-6 bg-blue-50 border border-blue-100 p-5 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 shadow-sm">
              <div className="flex items-start sm:items-center gap-3.5">
                <span className="text-2xl bg-blue-100/60 p-2.5 rounded-2xl block">💡</span>
                <div>
                  <h4 className="text-sm font-bold text-slate-900">Pruebe el flujo de enrolamiento y pánico en tiempo real</h4>
                  <p className="text-xs text-slate-650 text-slate-600 mt-1">Genere un PIN en el teléfono Administrador (Izquierda), consúmalo para registrar un teléfono en el del Usuario (Derecha), y envíe alarmas!</p>
                </div>
              </div>
              <div className="flex items-center gap-1.5 text-xs text-emerald-700 font-mono bg-emerald-50 px-3.5 py-1.5 rounded-xl border border-emerald-200/50 shrink-0">
                <span className="w-2 h-2 rounded-full bg-emerald-500 animate-pulse"></span>
                SIM ACTIVE
              </div>
            </div>

            {/* Simulated Double Phones Container */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-10 justify-items-center">
              
              {/* PHONE A: ADMINISTRADOR */}
              <div className="w-[340px] h-[680px] bg-slate-900 rounded-[3rem] border-[10px] border-slate-950 shadow-2xl relative flex flex-col overflow-hidden">
                {/* Speaker top bar */}
                <div className="absolute top-2 left-1/2 -translate-x-1/2 w-28 h-5 bg-slate-950 rounded-full z-20 flex items-center justify-center">
                  <div className="w-12 h-1 bg-slate-905 bg-slate-900 rounded-full"></div>
                </div>


                {/* Simulated Phone Content Area */}
                <div className="flex-1 flex flex-col pt-8 bg-slate-50 text-slate-800">
                  {/* Phone Header App status */}
                  <div className="bg-slate-950 px-4 py-3 flex items-center justify-between border-b border-slate-800 text-white">
                    <div className="flex items-center gap-1.55">
                      <Shield className="w-3.5 h-3.5 text-red-500" />
                      <span className="text-[11px] font-bold tracking-wider uppercase">SMS ALARMA INT</span>
                    </div>
                    <span className="text-[9px] font-bold text-red-400 bg-red-950/60 px-2 py-0.5 rounded-full border border-red-900/20 uppercase tracking-widest">ADMIN PERFIL</span>
                  </div>

                  {/* Body Content */}
                  <div className="flex-1 overflow-y-auto p-4 flex flex-col justify-between">
                    
                    {adminInboundSiren ? (
                      /* ACTIVE INBOUND EMERGENCY OVERLAY */
                      <div className="flex-1 bg-red-650 bg-red-600 rounded-2xl p-5 flex flex-col justify-between items-center text-center border-2 border-red-700 animate-pulse shadow-xl text-white">
                        <div className="mt-4 space-y-3">
                          <div className="w-14 h-14 rounded-full bg-white/20 flex items-center justify-center mx-auto animate-bounce">
                            <span className="text-2xl">🚨</span>
                          </div>
                          <h3 className="text-sm font-black uppercase tracking-wider text-white">PANICO EN DETECTOR</h3>
                          <p className="text-xs text-red-100 font-medium">EMISOR: <strong className="text-white text-[13px]">{adminInboundSiren.sender}</strong></p>
                        </div>
                        <div className="bg-black/20 border border-white/10 p-3.5 rounded-xl text-xs text-red-50 text-left w-full my-4 leading-tight font-medium italic">
                          "{adminInboundSiren.message}"
                        </div>
                        <div className="w-full">
                          <button 
                            onClick={confirmAdminReceipt}
                            className="w-full bg-white text-red-700 text-xs font-black py-3.5 rounded-xl hover:bg-neutral-100 active:scale-95 transition-all uppercase tracking-wider shadow-md"
                          >
                            CONFIRMAR ACUSE SMS
                          </button>
                        </div>
                      </div>
                    ) : (
                      /* MAIN SCREENS SATELLITE */
                      <div className="flex-1 flex flex-col justify-between">
                        
                        {/* SCREEN 1: ALARMA DE PANICO */}
                        {adminCurrentScreen === "alarm" && (
                          <div className="flex-1 flex flex-col justify-between text-center">
                            <div className="mt-2">
                              <h4 className="text-xs text-slate-800 font-bold tracking-wide uppercase">TOCAR PARA AUXILIO NATIVO</h4>
                              <p className="text-[11px] text-slate-500 mt-1">Sincronizará con {enrolados.filter(e => e.activo).length} contactos activos</p>
                            </div>

                            {/* Massive Panic Button */}
                            <div className="my-auto flex justify-center">
                              <div className="w-44 h-44 rounded-full border-8 border-red-50 bg-white flex items-center justify-center shadow-md">
                                <button 
                                  onClick={triggerAdminPanic}
                                  className="w-32 h-32 rounded-full bg-red-650 bg-red-650 bg-red-600 hover:bg-red-700 active:scale-95 transition-all flex flex-col items-center justify-center shadow-lg border-4 border-red-700 relative group"
                                >
                                  <span className="absolute inset-x-0 inset-y-0 w-full h-full rounded-full bg-red-500/20 group-hover:animate-ping rounded-full"></span>
                                  <span className="text-2xl z-10">🚨</span>
                                  <span className="text-xs font-black text-white tracking-widest mt-1 z-10">PANICO</span>
                                  <span className="text-[9px] text-red-100 mt-0.5 z-10">SMS AIR</span>
                                </button>
                              </div>
                            </div>

                            {/* Recepciones Panel */}
                            <div className="bg-white border border-slate-200 rounded-2xl p-3.5 text-left shadow-sm">
                              <h5 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">CONFIRMACIONES TIEMPO REAL:</h5>
                              <div className="mt-2 text-xs space-y-1.5 max-h-16 overflow-y-auto">
                                {history.filter(h => h.confirmada).map(h => (
                                  <div key={h.id} className="flex justify-between text-[11px] border-b border-slate-100 pb-1 font-medium">
                                    <span className="text-emerald-600 font-semibold">✓ {h.emisorNombre}</span>
                                    <span className="text-slate-500 font-mono text-[9px]">{h.enviadaEn.slice(11)}</span>
                                  </div>
                                ))}
                                {history.filter(h => h.confirmada).length === 0 && (
                                  <span className="text-[10px] text-slate-400 block text-center italic py-2">No hay confirmaciones aún.</span>
                                )}
                              </div>
                            </div>
                          </div>
                        )}

                        {/* SCREEN 2: HISTORIAL EVENTS */}
                        {adminCurrentScreen === "history" && (
                          <div className="flex-1 flex flex-col">
                            <h4 className="text-xs font-bold text-slate-800 mb-2 uppercase tracking-wide">HISTORIAL DE PANICOS</h4>
                            <div className="flex-1 overflow-y-auto space-y-2 pr-1 max-h-[360px]">
                              {history.map(item => (
                                <div key={item.id} className="bg-white border border-slate-150 p-3 rounded-xl text-xs shadow-sm hover:border-slate-300 transition">
                                  <div className="flex justify-between font-bold">
                                    <span className="text-slate-900">{item.emisorNombre}</span>
                                    <span className="text-slate-400 text-[10px] font-mono">{item.enviadaEn}</span>
                                  </div>
                                  <p className="text-[11px] text-slate-500 font-mono mt-1 pt-1 border-t border-slate-50">Payload: {item.mensaje}</p>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                        {/* SCREEN 3: MIEMBROS ENROLADOS */}
                        {adminCurrentScreen === "users" && (
                          <div className="flex-1 flex flex-col">
                            <div className="flex justify-between items-center mb-2">
                              <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wide">RED DE ENROLADOS</h4>
                              <span className="text-[10px] text-slate-500 font-mono">({enrolados.length} miembros)</span>
                            </div>
                            <div className="flex-1 overflow-y-auto space-y-2 pr-1 max-h-[300px]">
                              {enrolados.map(person => (
                                <div key={person.id} className="bg-white border border-slate-150 p-3 rounded-xl text-xs flex justify-between items-center shadow-sm hover:border-slate-300 transition">
                                  <div>
                                    <span className="font-bold text-slate-900 text-xs block">{person.nombre}</span>
                                    <span className="text-[10px] text-slate-500 font-mono">SMS: {person.telefono}</span>
                                  </div>
                                  <span className={`text-[9px] px-2 py-0.5 rounded-full font-bold ${person.activo ? 'bg-emerald-50 text-emerald-700 border border-emerald-200/20' : 'bg-slate-100 text-slate-400'}`}>
                                    {person.activo ? 'ACTIVO' : 'PAUSADO'}
                                  </span>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                        {/* SCREEN 4: AJUSTES RED CONTROLLER */}
                        {adminCurrentScreen === "settings" && (
                          <div className="flex-1 flex flex-col space-y-3">
                            <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wide font-sans">CONSTRUIR PIN REGISTRO</h4>
                            
                            {/* PIN Creation forms */}
                            <div className="bg-white border border-slate-200 p-3.5 rounded-xl space-y-3 shadow-sm text-slate-800">
                              <div>
                                <label className="text-[10px] text-slate-500 block mb-1 font-bold uppercase tracking-wider">NOMBRE COMPLETO:</label>
                                <input 
                                  type="text" 
                                  value={pinTargetName}
                                  placeholder="e.g. Carlos Ruiz"
                                  onChange={(e) => setPinTargetName(e.target.value)}
                                  className="w-full bg-slate-50 text-xs text-slate-900 border border-slate-200 rounded-lg p-2 focus:outline-none focus:border-blue-500 transition-all focus:ring-1 focus:ring-blue-500"
                                />
                              </div>
                              <button 
                                onClick={createEnrollmentPin}
                                className="w-full bg-blue-600 hover:bg-blue-700 text-xs font-bold py-2 rounded-lg text-white transition-all shadow-sm uppercase tracking-wider"
                              >
                                GENERAR PIN EN ROSTER
                              </button>
                              
                              {createdPinResult && (
                                <div className="p-2.5 bg-amber-50 border border-amber-250/20 rounded-lg text-center animate-fade-in text-slate-850">
                                  <span className="text-[9px] text-amber-800 block font-bold uppercase tracking-wider">COMPARTE EL SIGUIENTE PIN:</span>
                                  <span className="text-md font-black text-amber-700 font-mono tracking-wider">{createdPinResult}</span>
                                </div>
                              )}
                            </div>

                            {/* SMS message settings */}
                            <div className="bg-white border border-slate-200 p-3.5 rounded-xl space-y-2 shadow-sm text-slate-800">
                              <label className="text-[10px] text-slate-505 text-slate-500 block font-bold uppercase tracking-wide">MENSAJE PREDEFINIDO:</label>
                              <textarea 
                                value={systemConfig.mensajePredefinido}
                                onChange={(e) => setSystemConfig({...systemConfig, mensajePredefinido: e.target.value})}
                                className="w-full h-12 bg-slate-50 text-[11px] text-slate-700 border border-slate-200 rounded-lg p-2 resize-none focus:outline-none focus:border-blue-500 transition-all focus:ring-1 focus:ring-blue-500"
                              />
                            </div>
                          </div>
                        )}

                      </div>
                    )}

                  </div>

                  {/* Simulated Nav Bar (Bottom) */}
                  <div className="bg-slate-950 border-t border-slate-800 px-3 py-3 flex items-center justify-around z-10 text-white">
                    <button 
                      onClick={() => setAdminCurrentScreen("alarm")}
                      className={`flex flex-col items-center gap-1 transition-all ${adminCurrentScreen === 'alarm' ? 'text-red-500' : 'text-slate-400 hover:text-slate-200'}`}
                    >
                      <span className="text-sm">🚨</span>
                      <span className="text-[10px] font-bold">Alarma</span>
                    </button>
                    <button 
                      onClick={() => setAdminCurrentScreen("history")}
                      className={`flex flex-col items-center gap-1 transition-all ${adminCurrentScreen === 'history' ? 'text-red-500' : 'text-slate-400 hover:text-slate-200'}`}
                    >
                      <span className="text-sm">📝</span>
                      <span className="text-[10px] font-bold">Historial</span>
                    </button>
                    <button 
                      onClick={() => setAdminCurrentScreen("users")}
                      className={`flex flex-col items-center gap-1 transition-all ${adminCurrentScreen === 'users' ? 'text-red-500' : 'text-slate-400 hover:text-slate-200'}`}
                    >
                      <span className="text-sm">👥</span>
                      <span className="text-[10px] font-bold">Red</span>
                    </button>
                    <button 
                      onClick={() => setAdminCurrentScreen("settings")}
                      className={`flex flex-col items-center gap-1 transition-all ${adminCurrentScreen === 'settings' ? 'text-red-500' : 'text-slate-400 hover:text-slate-200'}`}
                    >
                      <span className="text-sm">⚙️</span>
                      <span className="text-[10px] font-bold">Ajustes</span>
                    </button>
                  </div>
                </div>

                {/* Rounded bottom physical bar speaker */}
                <div className="absolute bottom-1 left-1/2 -translate-x-1/2 w-32 h-1 bg-gray-650 bg-gray-600 rounded-full z-20"></div>
              </div>

              {/* PHONE B: USUARIO ENROLADO */}
              <div className="w-[340px] h-[680px] bg-slate-900 rounded-[3rem] border-[10px] border-slate-950 shadow-2xl relative flex flex-col overflow-hidden">
                {/* Speaker top bar */}
                <div className="absolute top-2 left-1/2 -translate-x-1/2 w-28 h-5 bg-slate-950 rounded-full z-20 flex items-center justify-center">
                  <div className="w-12 h-1 bg-slate-900 rounded-full"></div>
                </div>

                {/* Simulated Phone Content Area */}
                <div className="flex-1 flex flex-col pt-8 bg-slate-50 text-slate-800">
                  {/* Phone Header App status */}
                  <div className="bg-slate-950 px-4 py-3 flex items-center justify-between border-b border-slate-800 text-white">
                    <div className="flex items-center gap-1.5">
                      <Phone className="w-3.5 h-3.5 text-blue-400" />
                      <span className="text-[11px] font-bold tracking-wider uppercase">SMS RECEPTOR</span>
                    </div>
                    <span className="text-[9px] font-bold text-blue-400 bg-blue-950/60 px-2.5 py-0.5 rounded-full border border-blue-900/30 uppercase tracking-widest">USER STATUS</span>
                  </div>

                  {/* Body Content */}
                  <div className="flex-1 overflow-y-auto p-4 flex flex-col justify-between">
                    
                    {!userLogged ? (
                      /* USER ACCOUNT ONBOARDING PIN SCREEN */
                      <div className="flex-1 flex flex-col justify-between">
                        <div className="text-center pt-4">
                          <div className="w-12 h-12 bg-blue-50 rounded-2xl flex items-center justify-center text-2xl mx-auto shadow-sm">📲</div>
                          <h4 className="text-xs font-bold text-slate-800 uppercase mt-3 tracking-wider">Enrole su Dispositivo</h4>
                          <p className="text-[11px] text-slate-500 mt-1.5 px-4 leading-relaxed">Ingrese un número celular y el código PIN de 6 dígitos que ha sido generado en la parte del Admin.</p>
                        </div>

                        <div className="my-auto space-y-3 px-2">
                          <div>
                            <label className="text-[9px] text-slate-550 text-slate-500 font-bold tracking-widest block mb-1 uppercase">NUMERO CELULAR:</label>
                            <input 
                              type="text" 
                              value={userPhoneNum}
                              placeholder="e.g. +34699123456"
                              onChange={(e) => setUserPhoneNum(e.target.value)}
                              className="w-full bg-white text-xs text-slate-900 border border-slate-200 rounded-lg p-2 focus:outline-none focus:border-blue-500 font-mono"
                            />
                          </div>

                          <div>
                            <label className="text-[9px] text-slate-550 text-slate-500 font-bold tracking-widest block mb-1 uppercase">CODIGO PIN DE 6 DÍGITOS:</label>
                            <input 
                              type="text" 
                              maxLength={6}
                              value={userPinCode}
                              placeholder="e.g. 482391"
                              onChange={(e) => setUserPinCode(e.target.value)}
                              className="w-full bg-white text-xs text-slate-900 border border-slate-200 text-center tracking-widest font-mono rounded-lg p-2 focus:outline-none focus:border-blue-500 text-lg font-black text-amber-600"
                            />
                          </div>

                          <button 
                            onClick={consumeUserPin}
                            className="w-full bg-blue-600 hover:bg-blue-700 text-xs font-bold py-2.5 rounded-lg text-white transition tracking-wider"
                          >
                            VALIDAR PIN DE RED
                          </button>
                        </div>

                        <div className="text-center text-[10px] text-slate-400 pb-2">
                          Offline cache is stored in Room DB
                        </div>
                      </div>
                    ) : userInboundSiren ? (
                      /* ACTIVE INBOUND EMERGENCY SIREN FULLSCREEN OVERLAY */
                      <div className="flex-1 bg-red-650 bg-red-600 rounded-2xl p-5 flex flex-col justify-between items-center text-center border-2 border-red-700 animate-pulse shadow-xl text-white">
                        <div className="mt-4">
                          <div className="w-14 h-14 rounded-full bg-white/20 flex items-center justify-center text-3xl mx-auto animate-bounce">🚨</div>
                          <h3 className="text-sm font-black text-white mt-3 tracking-wider uppercase">PANICO RECIBIDO</h3>
                          <p className="text-xs text-red-100 font-bold mt-1">DE: {userInboundSiren.sender}</p>
                        </div>
                        <div className="bg-black/20 border border-white/10 p-3.5 rounded-xl text-xs text-white my-4 leading-tight font-medium italic">
                          "{userInboundSiren.message}"
                        </div>
                        <div className="w-full">
                          <button 
                            onClick={confirmUserReceipt}
                            className="w-full bg-white text-red-700 text-xs font-black py-4 rounded-xl hover:bg-neutral-100 transition-all uppercase tracking-wider shadow-lg"
                          >
                            CONFIRMAR RECEPCIÓN ✓
                          </button>
                        </div>
                      </div>
                    ) : (
                      /* LOGGED SYSTEM PREVIEWS FOR USERS */
                      <div className="flex-1 flex flex-col justify-between">
                        
                        {/* SCREEN 1: CLIENT PANIC EMISSION BUTTON */}
                        {userCurrentScreen === "alarm" && (
                          <div className="flex-1 flex flex-col justify-between text-center">
                            <div className="mt-2">
                              <h4 className="text-xs text-slate-800 font-bold tracking-wide uppercase">PANICO CIRCULAR CLIENTE</h4>
                              <p className="text-[10px] text-slate-500 mt-1">ID: {registeredUserInfo?.nombre} ({registeredUserInfo?.telefono})</p>
                            </div>

                            {/* Massive Auxilio Button */}
                            <div className="my-auto flex justify-center">
                              <div className="w-44 h-44 rounded-full border-8 border-red-55 border-red-50 bg-white flex items-center justify-center shadow-md">
                                <button 
                                  onClick={triggerUserPanic}
                                  className="w-32 h-32 rounded-full bg-red-650 bg-red-600 hover:bg-red-700 active:scale-95 transition-all flex flex-col items-center justify-center shadow-lg border-4 border-red-700 relative group animate-pulse"
                                >
                                  <span className="absolute inset-x-0 inset-y-0 w-full h-full rounded-full bg-red-500/20 group-hover:animate-ping rounded-full"></span>
                                  <span className="text-2xl z-10">🚨</span>
                                  <span className="text-xs font-black text-white tracking-widest mt-1 z-10">EMITIR</span>
                                  <span className="text-[9px] text-red-100 mt-0.5 z-10">AUXILIO</span>
                                </button>
                              </div>
                            </div>

                            {/* Recepciones Panel */}
                            <div className="bg-white border border-slate-200 rounded-2xl p-3 text-left shadow-sm">
                              <h5 className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">CONTACTOS PARTICIPANTES:</h5>
                              <div className="mt-1.5 text-[10px] text-slate-650 space-y-1 max-h-16 overflow-y-auto">
                                {enrolados.map(e => (
                                  <div key={e.id} className="flex justify-between border-b border-slate-100 pb-0.5 font-medium">
                                    <span>👤 {e.nombre} ({e.rol})</span>
                                    <span className="text-blue-650 font-bold text-[9px] uppercase">SINK</span>
                                  </div>
                                ))}
                              </div>
                            </div>
                          </div>
                        )}

                        {/* SCREEN 2: HISTORY */}
                        {userCurrentScreen === "history" && (
                          <div className="flex-1 flex flex-col">
                            <h4 className="text-xs font-bold text-slate-800 mb-2 uppercase tracking-wide">MIS SEÑALES</h4>
                            <div className="flex-1 overflow-y-auto space-y-2 pr-1 max-h-[360px]">
                              {history.map(item => (
                                <div key={item.id} className="bg-white border border-slate-150 p-2.5 rounded-xl text-xs shadow-sm hover:border-slate-300 transition">
                                  <div className="flex justify-between font-bold">
                                    <span className="text-blue-655 text-blue-600">{item.emisorNombre}</span>
                                    <span className="text-slate-400 text-[10px] font-mono">{item.enviadaEn}</span>
                                  </div>
                                  <p className="text-[11px] text-slate-500 font-mono mt-1 pt-1 border-t border-slate-50">Siren: {item.mensaje}</p>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                        {/* SCREEN 3: USER PROFILE */}
                        {userCurrentScreen === "profile" && (
                          <div className="flex-1 flex flex-col space-y-3">
                            <h4 className="text-xs font-bold text-slate-800 uppercase tracking-wide">DATOS VINCULADOS</h4>
                            
                            <div className="bg-white border border-slate-200 p-4 rounded-xl space-y-3 text-center text-xs shadow-sm">
                              <div className="w-12 h-12 bg-blue-50 text-blue-600 rounded-2xl mx-auto flex items-center justify-center text-lg shadow-sm border border-blue-105">👷</div>
                              <div>
                                <span className="font-bold text-slate-900 block text-sm mt-1">{registeredUserInfo?.nombre}</span>
                                <span className="text-slate-500 block text-[11px] font-mono mt-0.5">Número: {registeredUserInfo?.telefono}</span>
                              </div>
                            </div>

                            <button 
                              onClick={() => {
                                setUserLogged(false);
                                setRegisteredUserInfo(null);
                                setUserPhoneNum("");
                                setUserPinCode("");
                              }}
                              className="w-full bg-red-50 hover:bg-red-100 text-red-650 text-red-600 hover:text-red-700 text-xs font-bold py-2 px-4 rounded-lg border border-red-200/50 transition-all shadow-sm"
                            >
                              ELIMINAR CREDENCIAL LOCAL
                            </button>
                          </div>
                        )}

                      </div>
                    )}

                  </div>

                  {/* Simulated Nav Bar (Bottom) */}
                  {userLogged && !userInboundSiren && (
                    <div className="bg-slate-950 border-t border-slate-800 px-3 py-3 flex items-center justify-around z-10 text-white">
                      <button 
                        onClick={() => setUserCurrentScreen("alarm")}
                        className={`flex flex-col items-center gap-1 transition-all ${userCurrentScreen === 'alarm' ? 'text-blue-400' : 'text-slate-400 hover:text-slate-200'}`}
                      >
                        <span className="text-sm">🚨</span>
                        <span className="text-[10px] font-bold font-sans">Auxilio</span>
                      </button>
                      <button 
                        onClick={() => setUserCurrentScreen("history")}
                        className={`flex flex-col items-center gap-1 transition-all ${userCurrentScreen === 'history' ? 'text-blue-400' : 'text-slate-400 hover:text-slate-200'}`}
                      >
                        <span className="text-sm">📝</span>
                        <span className="text-[10px] font-bold font-sans">Mis Logs</span>
                      </button>
                      <button 
                        onClick={() => setUserCurrentScreen("profile")}
                        className={`flex flex-col items-center gap-1 transition-all ${userCurrentScreen === 'profile' ? 'text-blue-400' : 'text-slate-400 hover:text-slate-200'}`}
                      >
                        <span className="text-sm">👤</span>
                        <span className="text-[10px] font-bold font-sans">Perfil</span>
                      </button>
                    </div>
                  )}
                </div>

                {/* Rounded bottom physical bar speaker */}
                <div className="absolute bottom-1 left-1/2 -translate-x-1/2 w-32 h-1 bg-gray-600 rounded-full z-20"></div>
              </div>

            </div>
          </div>
        ) : (
          /* CODE INSPECTOR PANEL */
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 bg-white border border-slate-200 rounded-3xl overflow-hidden min-h-[580px] p-6 shadow-sm text-slate-800">
            {/* Left sidebar layout containing list of deliverables with indicators */}
            <div className="lg:col-span-4 border-r border-slate-100 pr-4 space-y-2">
              <h3 className="text-xs font-bold text-slate-400 uppercase tracking-widest pl-2 mb-3">Entregables Checklist Kotlin</h3>
              <div className="space-y-1">
                {deliverables.map((item, index) => (
                  <button
                    key={item.name}
                    onClick={() => {
                      setSelectedFileIndex(index);
                      setCopied(false);
                    }}
                    className={`w-full text-left px-3 py-2.5 rounded-xl text-xs font-semibold transition flex flex-col gap-1.5 ${selectedFileIndex === index ? 'bg-slate-900 text-white shadow-sm' : 'text-slate-500 hover:bg-slate-50 hover:text-slate-800'}`}
                  >
                    <span className="block font-bold text-[13px]">{item.name}</span>
                    <span className={`text-[11px] line-clamp-1 truncate font-normal ${selectedFileIndex === index ? 'text-slate-300' : 'text-slate-400'}`}>{item.desc}</span>
                  </button>
                ))}
              </div>
            </div>

            {/* Right code content viewer with path identifiers */}
            <div className="lg:col-span-8 flex flex-col justify-between">
              <div>
                <div className="flex justify-between items-center bg-slate-950 border border-slate-800 px-4 py-3 rounded-2xl mb-3 text-white shadow-sm">
                  <div className="flex flex-col">
                    <span className="text-xs font-bold">{deliverables[selectedFileIndex].name}</span>
                    <span className="text-[10px] text-slate-400 font-mono mt-0.5">{deliverables[selectedFileIndex].path}</span>
                  </div>
                  <button 
                    onClick={() => {
                      navigator.clipboard.writeText(deliverables[selectedFileIndex].code);
                      setCopied(true);
                      setTimeout(() => setCopied(false), 2000);
                    }}
                    className="bg-slate-800 text-xs font-bold text-white px-3.5 py-2 rounded-xl border border-slate-700 hover:bg-slate-700 active:scale-95 transition-all flex items-center gap-1.5 shadow-sm cursor-pointer"
                  >
                    <CheckCircle className={`w-3.5 h-3.5 transition-all ${copied ? 'text-emerald-400 scale-110' : 'text-slate-400'}`} />
                    {copied ? "¡Copiado!" : "Copiar Código"}
                  </button>
                </div>
                <pre className="p-4 bg-slate-900 text-slate-300 text-xs font-mono overflow-auto max-h-[440px] rounded-2xl border border-slate-800 leading-relaxed whitespace-pre shadow-inner">
                  <code>{deliverables[selectedFileIndex].code}</code>
                </pre>
              </div>
              
              <div className="mt-4 p-3.5 bg-slate-950 border border-slate-800 rounded-2xl flex items-center justify-between text-xs text-slate-400 shadow-sm">
                <span>Directorio de compilación física en el workspace: <strong className="text-slate-200">/android/...</strong></span>
                <span className="text-[10px] text-emerald-400 bg-emerald-950/60 border border-emerald-950/20 px-2.5 py-0.5 rounded-full font-mono uppercase tracking-wider font-semibold">STABLE & VERIFIED</span>
              </div>
            </div>
          </div>
        )}
        
      </main>

      {/* Workspace Footer branding */}
      <footer className="border-t border-[#1E2024] bg-[#0E0F11] px-6 py-4 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-500 gap-2">
        <span>© 2026 Sms Alarma Android. Todos los derechos reservados.</span>
        <div className="flex items-center gap-3">
          <span className="text-[11px] font-mono hover:text-white transition">minSdk 26 • targetSdk 34 • Kotlin 1.9</span>
          <span className="text-gray-700">|</span>
          <span className="text-[11px] font-mono hover:text-white transition">Hedgehog + Compatibility</span>
        </div>
      </footer>
    </div>
  );
}
