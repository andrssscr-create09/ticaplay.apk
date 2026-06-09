# TicaPlay APK — Guía de compilación

## Qué hace esta app
- Pantalla de login con **usuario y contraseña** (el cliente nunca ve la URL del servidor)
- Autenticación contra Xtream Codes con **fallback automático** entre dos servidores
- Dashboard con acceso a TV en vivo, películas y series
- Botón de soporte directo a WhatsApp
- Colores: negro cosmos #080810 + morado materia oscura #3B0764

---

## OPCIÓN A — GitHub Actions (sin instalar nada, 100% online)

### Pasos:
1. Crear cuenta en github.com (gratis)
2. Crear repositorio nuevo → "New repository" → nombre: `ticaplay-apk` → Public
3. Subir todos estos archivos al repositorio
4. Ir a la pestaña **Actions** del repositorio
5. El workflow se ejecuta automáticamente al subir los archivos
6. Cuando termine (≈3 minutos), clic en el build → **Artifacts** → descargar **TicaPlay-APK.zip**
7. Descomprimir → tenés el archivo `app-debug.apk`

---

## OPCIÓN B — Android Studio (en tu PC)

### Requisitos:
- Descargar Android Studio: https://developer.android.com/studio
- Instalación: ~10 minutos

### Pasos:
1. Abrir Android Studio
2. **File → Open** → seleccionar la carpeta `ticaplay-apk`
3. Esperar que descargue dependencias (primera vez ~5 min)
4. **Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. Clic en "locate" en la notificación → tenés el APK

---

## Cambiar el número de WhatsApp de soporte

En el archivo:
`app/src/main/java/com/ticaplay/app/DashboardActivity.java`

Buscar la línea:
```
Uri.parse("https://wa.me/50600000000?text=Hola+TicaPlay...")
```

Reemplazar `50600000000` con tu número real (código de CR: 506 + número sin guiones).

---

## Cambiar servidores

En `MainActivity.java`:
```java
private static final String SERVER_PRIMARY   = "http://tvpluscr.com:8080";
private static final String SERVER_SECONDARY = "https://reborned.cc:8443";
```

---

## Instalar el APK en Android
1. Pasar el APK al celular (WhatsApp, Drive, cable)
2. Abrir el archivo en el celular
3. Si pide permiso: **Configuración → Instalar apps desconocidas → Permitir**
4. Instalar

## Código Downloader (Fire TV / Android TV)
1. Subir el APK a Google Drive → obtener link de descarga directa
2. Registrar el link en: https://aftv.news/downloader-codes
3. Te dan un código de 6 dígitos → ese es el código que le das al cliente
