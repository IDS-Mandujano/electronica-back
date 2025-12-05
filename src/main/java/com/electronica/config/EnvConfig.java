package com.electronica.config;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvConfig {
    private static final Dotenv dotenv;

    static {
        try {
            // Intentar cargar .env desde la raíz del proyecto
            dotenv = Dotenv.configure()
                    .directory("./")
                    .ignoreIfMissing()
                    .load();
            System.out.println("✅ Archivo .env cargado correctamente");
        } catch (Exception e) {
            throw new RuntimeException("❌ Error al cargar archivo .env: " + e.getMessage(), e);
        }
    }

    // ---- SERVIDOR ----
    public static int getServerPort() {
        return Integer.parseInt(get("SERVER_PORT", "7000"));
    }

    // ---- BASE DE DATOS ----
    public static String getDbUrl() {
        return get("DB_URL", "jdbc:mysql://localhost:3306/electronica_domestica");
    }

    public static String getDbUsername() {
        return get("DB_USERNAME", "root");
    }

    public static String getDbPassword() {
        return get("DB_PASSWORD", "");
    }

    // ---- JWT ----
    public static String getJwtSecret() {
        String secret = get("JWT_SECRET", null);
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET debe tener al menos 32 caracteres. " +
                            "Configúralo en el archivo .env"
            );
        }
        return secret;
    }

    public static long getJwtExpiration() {
        return Long.parseLong(get("JWT_EXPIRATION", "86400000"));
    }

    // ---- EMAIL ----
    public static String getEmailFrom() {
        String email = get("EMAIL_FROM", null);
        if (email == null || email.isEmpty()) {
            throw new IllegalStateException(
                    "EMAIL_FROM no está configurado en el archivo .env"
            );
        }
        return email;
    }

    public static String getEmailPassword() {
        String password = get("EMAIL_PASSWORD", null);
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException(
                    "EMAIL_PASSWORD no está configurado en el archivo .env. " +
                            "Usa una contraseña de aplicación de Google"
            );
        }
        return password;
    }

    public static String getEmailSmtpHost() {
        return get("EMAIL_SMTP_HOST", "smtp.gmail.com");
    }

    public static String getEmailSmtpPort() {
        return get("EMAIL_SMTP_PORT", "587");
    }

    // ---- APLICACIÓN ----
    public static String getAppName() {
        return get("APP_NAME", "Electronica Domestica API");
    }

    public static String getAppFrontendUrl() {
        return get("APP_FRONTEND_URL", "http://localhost:3000");
    }

    // ---- MÉTODO AUXILIAR ----
    private static String get(String key, String defaultValue) {
        String value = dotenv.get(key);
        return value != null ? value : defaultValue;
    }

    // ---- MÉTODO PARA VALIDAR CONFIGURACIÓN ----
    public static void validateConfig() {
        System.out.println("\n🔍 Validando configuración...\n");

        try {
            System.out.println("📡 SERVER_PORT: " + getServerPort());
            System.out.println("💾 DB_URL: " + maskSensitiveData(getDbUrl()));
            System.out.println("👤 DB_USERNAME: " + getDbUsername());
            System.out.println("🔐 DB_PASSWORD: " + (getDbPassword().isEmpty() ? "(vacío)" : "***"));
            System.out.println("🔑 JWT_SECRET: " + maskSecret(getJwtSecret()));
            System.out.println("⏰ JWT_EXPIRATION: " + getJwtExpiration() + "ms");
            System.out.println("📧 EMAIL_FROM: " + getEmailFrom());
            System.out.println("🔐 EMAIL_PASSWORD: " + maskSecret(getEmailPassword()));
            System.out.println("📬 EMAIL_SMTP_HOST: " + getEmailSmtpHost());
            System.out.println("🔌 EMAIL_SMTP_PORT: " + getEmailSmtpPort());
            System.out.println("🏷️  APP_NAME: " + getAppName());
            System.out.println("🌐 APP_FRONTEND_URL: " + getAppFrontendUrl());

            System.out.println("\n✅ Configuración válida\n");
        } catch (Exception e) {
            System.err.println("\n❌ Error en la configuración: " + e.getMessage() + "\n");
            throw e;
        }
    }

    private static String maskSensitiveData(String data) {
        if (data == null || data.length() < 10) return "***";
        return data.substring(0, 20) + "...";
    }

    private static String maskSecret(String secret) {
        if (secret == null || secret.length() < 4) return "***";
        return secret.substring(0, 4) + "***" + secret.substring(secret.length() - 4);
    }
}