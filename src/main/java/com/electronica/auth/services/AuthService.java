package com.electronica.auth.services;

import com.electronica.config.EnvConfig;
import com.electronica.config.JwtConfig;
import com.electronica.auth.models.User;
import com.electronica.auth.repositories.UserRepository;
import io.javalin.http.Context;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class AuthService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * REGISTRO DE USUARIO CON ENVÍO DE EMAIL
     */
    public void register(Context ctx) {
        try {
            var body = ctx.bodyAsClass(Map.class);

            // Validar campos requeridos
            String nombreCompleto = (String) body.get("nombreCompleto");
            String email = (String) body.get("correoElectronico");
            String contrasena = (String) body.get("contrasena");
            String tipo = (String) body.get("tipo");

            if (nombreCompleto == null || nombreCompleto.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "El nombre completo es requerido"
                ));
                return;
            }

            if (email == null || email.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "El correo electrónico es requerido"
                ));
                return;
            }

            if (contrasena == null || contrasena.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "La contraseña es requerida"
                ));
                return;
            }

            if (tipo == null || tipo.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "El tipo de usuario es requerido"
                ));
                return;
            }

            // Verificar si el email ya existe
            if (userRepository.existsByEmail(email)) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "El correo ya está registrado"
                ));
                return;
            }

            // Guardar la contraseña en texto plano ANTES de encriptarla
            String contrasenaTextoPlano = contrasena;

            // Crear usuario
            User user = new User();
            user.setId(UUID.randomUUID().toString());
            user.setNombreCompleto(nombreCompleto);
            user.setCorreoElectronico(email);
            user.setTipo(tipo);
            user.setContrasena(BCrypt.hashpw(contrasena, BCrypt.gensalt()));

            // Guardar en BD
            User saved = userRepository.save(user);

            // 🔥 ENVIAR EMAIL DE BIENVENIDA CON CREDENCIALES
            try {
                emailService.sendWelcomeEmail(
                        saved.getCorreoElectronico(),
                        saved.getNombreCompleto(),
                        contrasenaTextoPlano,  // Enviar contraseña en texto plano
                        saved.getTipo()
                );
                System.out.println("✅ Email de bienvenida enviado a: " + saved.getCorreoElectronico());
            } catch (Exception emailError) {
                System.err.println("⚠️ Error al enviar email (pero usuario creado): " + emailError.getMessage());
                // No fallar el registro si falla el email
            }

            // Generar token JWT
            String token = JwtConfig.generateToken(saved.getId(), saved.getCorreoElectronico());

            ctx.status(201).json(Map.of(
                    "success", true,
                    "message", "Usuario registrado exitosamente. Se ha enviado un email con las credenciales.",
                    "data", Map.of(
                            "token", token,
                            "userId", saved.getId(),
                            "email", saved.getCorreoElectronico(),
                            "nombreCompleto", saved.getNombreCompleto(),
                            "tipo", saved.getTipo()
                    )
            ));

        } catch (Exception e) {
            System.err.println("❌ Error en registro: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    /**
     * LOGIN DE USUARIO
     */
    public void login(Context ctx) {
        try {
            var body = ctx.bodyAsClass(Map.class);

            String email = (String) body.get("correoElectronico");
            String password = (String) body.get("contrasena");

            if (email == null || email.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "El correo electrónico es requerido"
                ));
                return;
            }

            if (password == null || password.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "La contraseña es requerida"
                ));
                return;
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

            if (!BCrypt.checkpw(password, user.getContrasena())) {
                throw new IllegalArgumentException("Credenciales inválidas");
            }

            String token = JwtConfig.generateToken(user.getId(), user.getCorreoElectronico());

            ctx.json(Map.of(
                    "success", true,
                    "message", "Inicio de sesión exitoso",
                    "data", Map.of(
                            "token", token,
                            "userId", user.getId(),
                            "email", user.getCorreoElectronico(),
                            "nombreCompleto", user.getNombreCompleto(),
                            "tipo", user.getTipo()
                    )
            ));

        } catch (IllegalArgumentException e) {
            ctx.status(401).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("❌ Error en login: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * SOLICITAR RECUPERACIÓN DE CONTRASEÑA
     */
    public void requestPasswordReset(Context ctx) {
        try {
            var body = ctx.bodyAsClass(Map.class);
            String email = (String) body.get("correoElectronico");

            if (email == null || email.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "El correo electrónico es requerido"
                ));
                return;
            }

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

            String resetToken = UUID.randomUUID().toString();
            LocalDateTime expiry = LocalDateTime.now().plusHours(1);

            userRepository.updateResetToken(user.getId(), resetToken, expiry);

            String resetLink = EnvConfig.getAppFrontendUrl() + "/reset-password?token=" + resetToken;
            emailService.sendPasswordResetEmail(user.getCorreoElectronico(), resetLink);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Email de recuperación enviado"
            ));

        } catch (IllegalArgumentException e) {
            // Por seguridad, siempre devolvemos éxito aunque el email no exista
            ctx.json(Map.of(
                    "success", true,
                    "message", "Si el correo existe, recibirás un email de recuperación"
            ));
        } catch (Exception e) {
            System.err.println("❌ Error al solicitar reset: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Error al enviar email"
            ));
        }
    }

    /**
     * RESTABLECER CONTRASEÑA
     */
    public void resetPassword(Context ctx) {
        try {
            var body = ctx.bodyAsClass(Map.class);
            String token = (String) body.get("token");
            String newPassword = (String) body.get("nuevaContrasena");

            if (token == null || token.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Token es requerido"
                ));
                return;
            }

            if (newPassword == null || newPassword.isEmpty()) {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", "Nueva contraseña es requerida"
                ));
                return;
            }

            User user = userRepository.findByResetToken(token)
                    .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

            if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("Token expirado");
            }

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
            userRepository.updatePassword(user.getId(), hashedPassword);
            userRepository.updateResetToken(user.getId(), null, null);

            ctx.json(Map.of(
                    "success", true,
                    "message", "Contraseña actualizada exitosamente"
            ));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            System.err.println("❌ Error al resetear contraseña: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Error interno del servidor"
            ));
        }
    }

    // ... (al final de tu clase AuthService, después de resetPassword)

    public void getTecnicos(Context ctx) {
        try {
            // ¡Esta clase SÍ tiene 'userRepository'!
            List<User> tecnicos = userRepository.findByTipo("tecnico");

            List<Map<String, String>> tecnicoData = tecnicos.stream()
                    .map(user -> Map.of(
                            "id", user.getId(),
                            "nombre", user.getNombreCompleto()
                    ))
                    .collect(Collectors.toList());

            ctx.json(Map.of("success", true, "data", tecnicoData));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Error al obtener lista de técnicos"
            ));
        }
    }
}
