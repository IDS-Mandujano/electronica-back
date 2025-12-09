package com.electronica.auth.services;

import com.electronica.config.EnvConfig;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailService {
    private final String fromEmail;
    private final String password;
    private final Properties properties;

    public EmailService() {
        this.fromEmail = EnvConfig.getEmailFrom();
        this.password = EnvConfig.getEmailPassword();

        this.properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", EnvConfig.getEmailSmtpHost());
        properties.put("mail.smtp.port", EnvConfig.getEmailSmtpPort());
        properties.put("mail.smtp.ssl.trust", EnvConfig.getEmailSmtpHost());
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        System.out.println("📧 EmailService inicializado con: " + fromEmail);
    }

    /**
     * Envía un email de bienvenida con las credenciales al registrarse
     */
    public void sendWelcomeEmail(String toEmail, String nombreCompleto, String contrasena, String tipo) {
        Session session = createSession();

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🎉 Bienvenido a " + EnvConfig.getAppName());

            String tipoEmoji = switch (tipo.toUpperCase()) {
                case "ADMIN" -> "👑 Administrador";
                case "TECNICO" -> "🔧 Técnico";
                case "RECEPCIONISTA" -> "📋 Recepcionista";
                default -> "👤 Usuario";
            };

            String htmlContent = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                            background-color: #f4f4f4;
                        }
                        .container {
                            background-color: white;
                            border-radius: 10px;
                            padding: 0;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                            overflow: hidden;
                        }
                        .header {
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: white;
                            padding: 40px 30px;
                            text-align: center;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 28px;
                        }
                        .content {
                            padding: 30px;
                        }
                        .welcome-message {
                            font-size: 18px;
                            color: #667eea;
                            font-weight: bold;
                            margin-bottom: 20px;
                        }
                        .credentials-box {
                            background-color: #f8f9fa;
                            border-left: 4px solid #667eea;
                            padding: 20px;
                            margin: 20px 0;
                            border-radius: 5px;
                        }
                        .credentials-box h3 {
                            margin-top: 0;
                            color: #667eea;
                        }
                        .credential-item {
                            margin: 10px 0;
                            padding: 10px;
                            background-color: white;
                            border-radius: 5px;
                        }
                        .credential-label {
                            font-weight: bold;
                            color: #666;
                            font-size: 12px;
                            text-transform: uppercase;
                        }
                        .credential-value {
                            font-size: 16px;
                            color: #333;
                            font-family: 'Courier New', monospace;
                            margin-top: 5px;
                        }
                        .role-badge {
                            display: inline-block;
                            background-color: #667eea;
                            color: white;
                            padding: 8px 15px;
                            border-radius: 20px;
                            font-size: 14px;
                            margin: 10px 0;
                        }
                        .warning-box {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 5px;
                        }
                        .button {
                            display: inline-block;
                            padding: 15px 30px;
                            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                            color: white;
                            text-decoration: none;
                            border-radius: 5px;
                            margin: 20px 0;
                            font-weight: bold;
                            text-align: center;
                        }
                        .footer {
                            margin-top: 30px;
                            padding-top: 20px;
                            border-top: 1px solid #ddd;
                            font-size: 12px;
                            color: #666;
                            text-align: center;
                        }
                        .security-tips {
                            background-color: #e7f3ff;
                            border-left: 4px solid #2196F3;
                            padding: 15px;
                            margin: 20px 0;
                            border-radius: 5px;
                        }
                        .security-tips h4 {
                            margin-top: 0;
                            color: #2196F3;
                        }
                        .security-tips ul {
                            margin: 10px 0;
                            padding-left: 20px;
                        }
                        .security-tips li {
                            margin: 5px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🎉 ¡Bienvenido!</h1>
                            <p style="margin: 10px 0 0 0; font-size: 16px;">Tu cuenta ha sido creada exitosamente</p>
                        </div>
                        
                        <div class="content">
                            <p class="welcome-message">Hola %s,</p>
                            
                            <p>Nos complace darte la bienvenida a <strong>%s</strong>.</p>
                            
                            <p>Tu cuenta ha sido creada con el rol:</p>
                            <div style="text-align: center;">
                                <span class="role-badge">%s</span>
                            </div>
                            
                            <div class="credentials-box">
                                <h3>🔐 Tus Credenciales de Acceso</h3>
                                
                                <div class="credential-item">
                                    <div class="credential-label">📧 Correo Electrónico</div>
                                    <div class="credential-value">%s</div>
                                </div>
                                
                                <div class="credential-item">
                                    <div class="credential-label">🔑 Contraseña</div>
                                    <div class="credential-value">%s</div>
                                </div>
                                
                                <div class="credential-item">
                                    <div class="credential-label">👤 Rol</div>
                                    <div class="credential-value">%s</div>
                                </div>
                            </div>
                            
                            <div class="warning-box">
                                <strong>⚠️ IMPORTANTE:</strong>
                                <ul style="margin: 10px 0; padding-left: 20px;">
                                    <li>Guarda estas credenciales en un lugar seguro</li>
                                    <li>No compartas tu contraseña con nadie</li>
                                    <li>Te recomendamos cambiar tu contraseña después del primer inicio de sesión</li>
                                </ul>
                            </div>
                            
                            <div style="text-align: center;">
                                <a href="%s" class="button">Iniciar Sesión Ahora</a>
                            </div>
                            
                            <div class="security-tips">
                                <h4>🛡️ Consejos de Seguridad</h4>
                                <ul>
                                    <li>Usa una contraseña fuerte y única</li>
                                    <li>No uses la misma contraseña en otros sitios</li>
                                    <li>Cierra sesión cuando termines de usar el sistema</li>
                                    <li>Si sospechas que tu cuenta fue comprometida, cambia tu contraseña inmediatamente</li>
                                </ul>
                            </div>
                            
                            <p>Si tienes alguna pregunta o necesitas ayuda, no dudes en contactarnos.</p>
                            
                            <p>¡Gracias por unirte a nosotros!</p>
                            
                            <div class="footer">
                                <p>Este es un correo automático, por favor no respondas.</p>
                                <p>&copy; 2024 %s. Todos los derechos reservados.</p>
                                <p style="margin-top: 10px; color: #999;">
                                    Si no solicitaste esta cuenta, por favor ignora este correo.
                                </p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
            """.formatted(
                    nombreCompleto,
                    EnvConfig.getAppName(),
                    tipoEmoji,
                    toEmail,
                    contrasena,
                    tipo,
                    EnvConfig.getAppFrontendUrl(),
                    EnvConfig.getAppName()
            );

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("✅ Email de bienvenida enviado a: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email de bienvenida: " + e.getMessage());
            e.printStackTrace();
            // No lanzamos excepción para que no falle el registro si falla el email
        }
    }

    /**
     * Envía email de recuperación de contraseña
     */
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        Session session = createSession();

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("🔐 Recuperación de Contraseña - " + EnvConfig.getAppName());

            String htmlContent = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .container {
                            background-color: #f9f9f9;
                            border-radius: 10px;
                            padding: 30px;
                            box-shadow: 0 2px 5px rgba(0,0,0,0.1);
                        }
                        .header {
                            background-color: #dc3545;
                            color: white;
                            padding: 20px;
                            text-align: center;
                            border-radius: 10px 10px 0 0;
                            margin: -30px -30px 20px -30px;
                        }
                        .button {
                            display: inline-block;
                            padding: 12px 30px;
                            background-color: #dc3545;
                            color: white !important;
                            text-decoration: none;
                            border-radius: 5px;
                            margin: 20px 0;
                            font-weight: bold;
                        }
                        .warning {
                            background-color: #fff3cd;
                            border-left: 4px solid #ffc107;
                            padding: 10px;
                            margin: 20px 0;
                        }
                        .footer {
                            margin-top: 30px;
                            padding-top: 20px;
                            border-top: 1px solid #ddd;
                            font-size: 12px;
                            color: #666;
                            text-align: center;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🔐 Recuperación de Contraseña</h1>
                        </div>
                        
                        <p>Hola,</p>
                        
                        <p>Has solicitado restablecer tu contraseña en <strong>%s</strong>.</p>
                        
                        <p>Haz clic en el siguiente botón para crear una nueva contraseña:</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button">Restablecer Contraseña</a>
                        </div>
                        
                        <p>O copia y pega este enlace en tu navegador:</p>
                        <p style="word-break: break-all; background-color: #f0f0f0; padding: 10px; border-radius: 5px;">
                            %s
                        </p>
                        
                        <div class="warning">
                            <strong>⚠️ Importante:</strong>
                            <ul>
                                <li>Este enlace expirará en <strong>1 hora</strong></li>
                                <li>Solo puedes usar este enlace una vez</li>
                                <li>Si no solicitaste este cambio, ignora este correo</li>
                            </ul>
                        </div>
                        
                        <div class="footer">
                            <p>Este es un correo automático, por favor no respondas.</p>
                            <p>&copy; 2024 %s. Todos los derechos reservados.</p>
                        </div>
                    </div>
                </body>
                </html>
            """.formatted(
                    EnvConfig.getAppName(),
                    resetLink,
                    resetLink,
                    EnvConfig.getAppName()
            );

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("✅ Email de recuperación enviado a: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("❌ Error al enviar email: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo enviar el email de recuperación", e);
        }
    }

    private Session createSession() {
        return Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
    }
}