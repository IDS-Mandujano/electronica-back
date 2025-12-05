# 🔧 Electrónica Doméstica - API REST

Sistema integral de gestión para talleres de electrónica doméstica. API RESTful desarrollada con **Java 17**, **Javalin**, y **MySQL** siguiendo principios de **MVC (Model-View-Controller)** con autenticación JWT.

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Javalin](https://img.shields.io/badge/Javalin-5.6.3-blue?style=for-the-badge)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![JWT](https://img.shields.io/badge/JWT-Auth-green?style=for-the-badge)

---

## 🌟 Características

### 🔐 Autenticación JWT
- ✅ Registro de usuarios (Admin, Técnico, Recepcionista)
- ✅ Login con JWT (JSON Web Tokens)
- ✅ Middleware de autenticación automático
- ✅ Recuperación de contraseña por email
- ✅ Encriptación con BCrypt

### 🔧 Gestión de Reparaciones
- ✅ CRUD completo de tarjetas de reparación
- ✅ Asignación a técnicos
- ✅ Seguimiento de estados
- ✅ Historial completo

### 📦 Inventario de Productos
- ✅ CRUD completo de productos
- ✅ Categorización
- ✅ Control de stock
- ✅ Alertas de stock bajo
- ✅ Búsqueda por categoría

### ✅ Registro de Finalizados
- ✅ CRUD completo de trabajos completados
- ✅ Costo de reparación
- ✅ Consultas por técnico
- ✅ Reportes

---

## 🏗️ Arquitectura

Implementa **MVC (Model-View-Controller)** simplificado:

```
📁 Módulo
├── 📄 models/          # Entidades de dominio
├── 📄 repositories/    # Acceso a datos (SQL)
├── 📄 services/        # Lógica de negocio
└── 📄 routes/          # Endpoints HTTP + JWT auth
```

### Ventajas de esta arquitectura:

- ✅ **Simple y clara**: Solo 4 carpetas por módulo
- ✅ **Fácil mantenimiento**: Código organizado y encontrable
- ✅ **Testeable**: Services aislados y testeables
- ✅ **Segura**: Autenticación JWT integrada

---

## 💻 Tecnologías

| Tecnología | Versión | Propósito |
|-----------|---------|-----------|
| **Java** | 17 | Lenguaje principal |
| **Javalin** | 5.6.3 | Framework web ligero |
| **MySQL** | 8.0+ | Base de datos |
| **HikariCP** | 5.1.0 | Connection pooling |
| **BCrypt** | 0.4 | Hash de contraseñas |
| **JWT** | 4.4.0 | Autenticación segura |
| **JavaMail** | 1.6.2 | Envío de emails |
| **Dotenv** | 3.0.0 | Variables de entorno |
| **Jackson** | 2.15.3 | Serialización JSON |

---

## 📦 Requisitos Previos

- ☑️ **Java JDK 17** o superior
- ☑️ **MySQL 8.0+**
- ☑️ **Gradle 8.0+**
- ☑️ Cuenta de **Gmail** con contraseña de aplicación

```bash
# Verificar instalaciones
java -version    # Java 17+
mysql --version  # MySQL 8.0+
gradle --version # Gradle 8.0+
```

---

## 🚀 Instalación

### 1️⃣ Clonar repositorio

```bash
git clone https://github.com/tu-usuario/electronica-domestica-api.git
cd electronica-domestica-api
```

### 2️⃣ Crear base de datos

```bash
mysql -u root -p < database.sql
```

O ejecuta manualmente:

```sql
CREATE DATABASE electronica_domestica;
USE electronica_domestica;
-- Las tablas se crean automáticamente al iniciar la API
```

### 3️⃣ Configurar variables de entorno

Crea `.env` en la raíz:

```properties
# ---- SERVIDOR ----
SERVER_PORT=7000

# ---- BASE DE DATOS ----
DB_URL=jdbc:mysql://localhost:3306/electronica_domestica?useSSL=false&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=tu_contraseña_mysql

# ---- JWT (GENERAR UNA CLAVE SECRETA LARGA) ----
JWT_SECRET=mi-super-clave-secreta-de-minimo-32-caracteres-aleatorios-xyz789
JWT_EXPIRATION=86400000

# ---- EMAIL (GMAIL) ----
EMAIL_FROM=tu-email@gmail.com
EMAIL_PASSWORD=tu-contraseña-de-aplicacion-google
EMAIL_SMTP_HOST=smtp.gmail.com
EMAIL_SMTP_PORT=587

# ---- APLICACIÓN ----
APP_NAME=Electronica Domestica API
APP_FRONTEND_URL=http://localhost:3000
```

**⚠️ IMPORTANTE:** Para el JWT_SECRET, genera una clave aleatoria de al menos 32 caracteres.

### 4️⃣ Instalar dependencias

```bash
gradle clean build
```

### 5️⃣ Iniciar servidor

```bash
gradle run
```

La API estará disponible en: `http://localhost:7000`

---

## 📡 API Endpoints

### Base URL: `http://localhost:7000/api`

### 🔓 Autenticación (Sin token requerido)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/auth/register` | Registrar usuario |
| `POST` | `/auth/login` | Iniciar sesión (obtener token) |
| `POST` | `/auth/request-reset` | Solicitar recuperación de contraseña |
| `POST` | `/auth/reset-password` | Restablecer contraseña |

### 🔒 Tarjetas (Token requerido)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/tarjetas` | Crear tarjeta |
| `GET` | `/tarjetas` | Obtener todas |
| `GET` | `/tarjetas/{id}` | Obtener por ID |
| `PUT` | `/tarjetas/{id}` | Actualizar |
| `DELETE` | `/tarjetas/{id}` | Eliminar |
| `GET` | `/tarjetas/tecnico/{tecnicoId}` | Por técnico |

### 🔒 Productos (Token requerido)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/productos` | Crear producto |
| `GET` | `/productos` | Obtener todos |
| `GET` | `/productos/{id}` | Obtener por ID |
| `PUT` | `/productos/{id}` | Actualizar |
| `DELETE` | `/productos/{id}` | Eliminar |
| `GET` | `/productos/categoria/{categoria}` | Por categoría |
| `GET` | `/productos/stock-bajo?threshold=10` | Stock bajo |

### 🔒 Finalizados (Token requerido)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/finalizados` | Crear registro |
| `GET` | `/finalizados` | Obtener todos |
| `GET` | `/finalizados/{id}` | Obtener por ID |
| `PUT` | `/finalizados/{id}` | Actualizar |
| `DELETE` | `/finalizados/{id}` | Eliminar |
| `GET` | `/finalizados/tecnico/{tecnicoId}` | Por técnico |

---

## 🧪 Ejemplos de Uso

### 1. Registro

```bash
curl -X POST http://localhost:7000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombreCompleto": "Juan Pérez",
    "correoElectronico": "juan@example.com",
    "contrasena": "password123",
    "tipo": "TECNICO"
  }'
```

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": "abc-123",
    "email": "juan@example.com"
  }
}
```

### 2. Login

```bash
curl -X POST http://localhost:7000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "correoElectronico": "juan@example.com",
    "contrasena": "password123"
  }'
```

### 3. Crear Tarjeta (CON TOKEN)

```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X POST http://localhost:7000/api/tarjetas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "nombreCliente": "Carlos López",
    "numeroCelular": "9611234567",
    "marca": "Samsung",
    "modelo": "UN55TU8000",
    "problemaDescrito": "No enciende",
    "tecnicoId": "abc-123",
    "tecnicoNombre": "Juan Pérez"
  }'
```

### 4. Obtener Tarjetas

```bash
curl http://localhost:7000/api/tarjetas \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📁 Estructura del Proyecto

```
electronica-domestica-api/
│
├── src/main/java/com/electronica/
│   ├── Main.java                          # Punto de entrada
│   │
│   ├── config/                            # Configuraciones globales
│   │   ├── DatabaseConfig.java
│   │   ├── EnvConfig.java
│   │   ├── CorsConfig.java
│   │   └── JwtConfig.java                 # Middleware JWT
│   │
│   ├── auth/                              # Módulo de Autenticación
│   │   ├── models/User.java
│   │   ├── repositories/UserRepository.java
│   │   ├── services/
│   │   │   ├── AuthService.java
│   │   │   ├── JwtService.java
│   │   │   └── EmailService.java
│   │   └── routes/AuthRoutes.java
│   │
│   ├── tarjeta/                           # Módulo de Tarjetas
│   │   ├── models/RegistroTarjeta.java
│   │   ├── repositories/TarjetaRepository.java
│   │   ├── services/TarjetaService.java
│   │   └── routes/TarjetaRoutes.java
│   │
│   ├── producto/                          # Módulo de Productos
│   │   ├── models/Producto.java
│   │   ├── repositories/ProductoRepository.java
│   │   ├── services/ProductoService.java
│   │   └── routes/ProductoRoutes.java
│   │
│   └── finalizado/                        # Módulo de Finalizados
│       ├── models/RegistroFinalizado.java
│       ├── repositories/FinalizadoRepository.java
│       ├── services/FinalizadoService.java
│       └── routes/FinalizadoRoutes.java
│
├── .env                                   # Variables de entorno
├── .gitignore
├── build.gradle
├── database.sql
├── README.md
└── settings.gradle
```

---

## 🔐 Seguridad

### Autenticación JWT

Todas las rutas (excepto `/auth/*`) requieren un token JWT válido:

```bash
# Formato correcto del header
Authorization: Bearer <tu_token_jwt>
```

### Generación de Token

El token se genera automáticamente en:
- Registro exitoso (`/auth/register`)
- Login exitoso (`/auth/login`)

### Validación de Token

El middleware `JwtConfig.authenticate` valida automáticamente:
- ✅ Presencia del token
- ✅ Formato correcto (`Bearer <token>`)
- ✅ Firma del token
- ✅ Expiración del token

---

## 🐛 Solución de Problemas

### Error: "Token no proporcionado"

```bash
# ❌ INCORRECTO
curl -H "Authorization: tu_token" ...

# ✅ CORRECTO
curl -H "Authorization: Bearer tu_token" ...
```

### Error: "Cannot connect to database"

1. Verifica que MySQL esté corriendo
2. Verifica credenciales en `.env`
3. Crea la base de datos manualmente

### Error: "JWT_SECRET must be at least 32 characters"

Genera una clave más larga en `.env`:
```properties
JWT_SECRET=mi-clave-super-secreta-con-minimo-32-caracteres-aleatorios-xyz789
```

---

## 🧪 Testing

```bash
# Ejecutar tests
gradle test

# Ver reporte de cobertura
gradle jacocoTestReport
```

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT.

---

## 👨‍💻 Autor

**Tu Nombre**
- GitHub: [@tu-usuario](https://github.com/tu-usuario)
- Email: tu-email@ejemplo.com

---

## 🙏 Agradecimientos

- [Javalin](https://javalin.io/)
- [Auth0 JWT](https://github.com/auth0/java-jwt)
- [HikariCP](https://github.com/brettwooldridge/HikariCP)

---

## 🔜 Roadmap

- [ ] Implementar roles y permisos
- [ ] Agregar paginación
- [ ] Documentación con Swagger
- [ ] Tests unitarios completos
- [ ] Dashboard de estadísticas
- [ ] WebSockets para notificaciones
- [ ] Soporte para imágenes

---

<div align="center">

**⭐ Si este proyecto te fue útil, considera darle una estrella ⭐**

Hecho con ❤️ y ☕

</div>