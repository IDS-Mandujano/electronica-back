# 🔄 Compatibilidad Windows ↔ macOS

Este documento explica la compatibilidad de la API entre Windows y macOS, y los cambios necesarios para ejecutarla en cada plataforma.

---

## ✅ **Compatibilidad General**

La API es **100% compatible** entre Windows y macOS porque:

- ✅ **Java es multiplataforma**: El código Java funciona igual en ambos sistemas operativos
- ✅ **Javalin es multiplataforma**: El framework web funciona en cualquier sistema operativo
- ✅ **Gradle es multiplataforma**: El sistema de build funciona en Windows, macOS y Linux
- ✅ **MySQL es multiplataforma**: La base de datos funciona igual en ambos sistemas
- ✅ **Sin rutas hardcodeadas**: El código usa rutas relativas y variables de entorno

---

## 🔧 **Cambios Realizados para macOS**

### 1. **build.gradle** ✅
```gradle
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)  // Cambiado de 24 a 21
    }
}
```

**Razón**: Java 24 aún no existe. Se ajustó a Java 21 que es la versión instalada en tu Mac.

### 2. **gradle.properties** ✅
```properties
# ANTES (Windows):
org.gradle.java.home=C:/Java/java-1.8.0-openjdk-1.8.0.392-1.b08.redhat.windows.x86_64

# AHORA (macOS):
# Gradle detectará automáticamente Java 21 instalado
# Si necesitas especificar manualmente:
# org.gradle.java.home=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
```

**Razón**: Las rutas de Windows no funcionan en macOS. Gradle detecta automáticamente Java en macOS.

### 3. **Permisos de ejecución** ✅
```bash
chmod +x gradlew  # Necesario en macOS/Linux, no en Windows
```

**Razón**: macOS/Linux requieren permisos de ejecución explícitos para scripts.

---

## 🚀 **Cómo Ejecutar en Cada Plataforma**

### **Windows**
```bash
# Usar gradlew.bat
gradlew.bat clean build
gradlew.bat run
```

### **macOS/Linux**
```bash
# Usar gradlew (script shell)
./gradlew clean build
./gradlew run
```

---

## 📋 **Verificación de Compatibilidad**

### ✅ **Verificado en macOS:**
- [x] Compilación exitosa con `./gradlew clean build`
- [x] Gradle detecta Java 21 automáticamente
- [x] Todas las dependencias se descargan correctamente
- [x] El código Java no tiene rutas específicas de Windows
- [x] El archivo `.env` funciona igual en ambos sistemas

### ⚠️ **Diferencias Menores:**

| Aspecto | Windows | macOS |
|---------|---------|-------|
| **Script Gradle** | `gradlew.bat` | `gradlew` |
| **Separador de rutas** | `\` o `/` | `/` |
| **Variables de entorno** | `%VAR%` | `$VAR` |
| **Permisos de archivos** | No necesarios | `chmod +x` requerido |

---

## 🔍 **Verificación de Requisitos**

### **En macOS:**
```bash
# Verificar Java
java -version
# Debe mostrar: java version "21.0.5" o superior

# Verificar Gradle
./gradlew --version
# Debe mostrar: Gradle 8.14

# Verificar MySQL
mysql --version
# Debe mostrar: mysql Ver 8.0.x o superior
```

---

## 📝 **Archivo .env**

El archivo `.env` funciona **exactamente igual** en ambos sistemas:

```properties
# Funciona igual en Windows y macOS
SERVER_PORT=7000
DB_URL=jdbc:mysql://localhost:3306/electronica_domestica?useSSL=false&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=tu_contraseña
```

**No requiere cambios** entre plataformas.

---

## 🐛 **Solución de Problemas**

### **Error: "permission denied: ./gradlew"**
```bash
chmod +x gradlew
```

### **Error: "Java version mismatch"**
- Verifica que tengas Java 21 instalado
- Ajusta `build.gradle` si es necesario

### **Error: "Cannot find Java"**
- En macOS: Gradle detecta automáticamente Java
- Si no funciona, descomenta y ajusta `org.gradle.java.home` en `gradle.properties`

---

## ✅ **Conclusión**

La API es **completamente compatible** entre Windows y macOS. Los únicos cambios necesarios son:

1. ✅ Versión de Java ajustada (24 → 21)
2. ✅ Ruta de Java en `gradle.properties` comentada (Gradle detecta automáticamente)
3. ✅ Permisos de ejecución en `gradlew`

**Todo lo demás funciona igual en ambas plataformas.**

---

## 🎯 **Próximos Pasos**

1. ✅ Verificar que MySQL esté corriendo
2. ✅ Verificar que el archivo `.env` tenga las credenciales correctas
3. ✅ Ejecutar: `./gradlew run`
4. ✅ Probar: `curl http://localhost:7000/api/health`

---

**Última actualización**: Diciembre 2024
**Versión Java**: 21.0.5
**Versión Gradle**: 8.14
**Sistema Operativo**: macOS (Darwin 26.1 aarch64)

