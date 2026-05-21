# GUÍA DE CONFIGURACIÓN — SMS ALARMA ANDROID

---

## REQUISITOS GENERALES
- **IDE:** Android Studio Hedgehog (o superior).
- **Gradle:** Versión 8.2+ con soporte Kotlin DSL.
- **Java JDK:** Versión 17+.
- **Dispositivo de Pruebas:** Teléfonos celulares reales Android (versión mínima Oreo / API 26) equipados con tarjeta SIM de saldo activo para testing de SMS nativo.

---

## PASO 1: CREAR TU PROYECTO EN FIREBASE
1. Dirígete a la consola oficial: [https://console.firebase.google.com/](https://console.firebase.google.com/).
2. Haz clic en **"Agregar Proyecto"** e introduce el nombre: `SmsAlarma`.
3. Activa o desactiva Google Analytics según tu preferencia y haz clic en crear.

---

## PASO 2: HABILITAR FIREBASE AUTHENTICATION (ADMIN)
1. En el panel de control lateral izquierdo, ve a **Construir -> Authentication** y haz clic en **Comenzar**.
2. En la pestaña **Método de inicio de sesión**, selecciona **Correo electrónico y contraseña**.
3. Activa la casilla de habilitación y haz clic en **Guardar**.

*Nota: La primera vez que el Administrador introduzca su email y contraseña en la aplicación, el sistema creará esa cuenta de forma automática en Firebase Auth y la asignará inmediatamente en el nodo /enrolados de la base de datos.*

---

## PASO 3: PROVISIONAR LA BASE DE DATOS CLOUD FIRESTORE
1. En la barra lateral ve a **Construir -> Firestore Database** y haz clic en **Crear base de datos**.
2. Elige la **Ubicación/Región** de tu conveniencia y haz clic en continuar.
3. Elige iniciar en **Modo de prueba** (esto es temporal ya que aplicaremos reglas robustas en el paso siguiente).
4. Luego de que se cree la base de datos, entra en la pestaña **Rules** (Reglas de Seguridad).
5. Copia el archivo `firestore.rules` provisto en este proyecto y pégalo allí para blindar accesos indebidos. Haz clic en **Publicar**.

---

## PASO 4: VINCULAR LA APP DE ANDROID CON FIREBASE
1. En el panel de Inicio de tu proyecto Firebase, haz clic en el ícono de **Android (con forma de robot)** para registrar tu app.
2. Introduce el identificador de paquete exacto: `com.alarmsms.app`.
3. Dale un apodo (por ejemplo: `Sms Alarma App`).
4. Haz clic en **Registrar app**.
5. Descarga el archivo generado **`google-services.json`**.
6. Coloca este archivo directamente dentro de la carpeta `/android/app/` de tu proyecto antes de compilar.

---

## PASO 5: ARRANQUE, COMPILACIÓN Y TESTING EN TU DISPOSITIVO
1. Abre **Android Studio** y selecciona **Open Class/Project**.
2. Apunta a la carpeta `/android` de este directorio de trabajo.
3. Espera a que Gradle descargue las dependencias y sincronice todos los archivos con éxito.
4. Conecta tu teléfono Android mediante depuración USB (Developer Mode activado).
5. Haz clic en **Run app** (flecha verde).
6. **MUY IMPORTANTE:** Al abrir la aplicación por primera vez en el dispositivo, acepta **TODAS** las solicitudes de permisos de tiempo de ejecución (SMS, Cámara, Notificaciones).
7. ¡Disfruta del sistema de alarma interactivo y blindado sobre SMS nativo!
