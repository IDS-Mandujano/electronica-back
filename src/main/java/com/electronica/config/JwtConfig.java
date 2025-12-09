package com.electronica.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.Context;

import java.util.Date;
import java.util.Map;

public class JwtConfig {

    public static String generateToken(String userId, String email) {
        Algorithm algorithm = Algorithm.HMAC256(EnvConfig.getJwtSecret());

        return JWT.create()
                .withSubject(userId)
                .withClaim("email", email)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EnvConfig.getJwtExpiration()))
                .sign(algorithm);
    }

    public static DecodedJWT verifyToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(EnvConfig.getJwtSecret());
            return JWT.require(algorithm).build().verify(token);
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Token inválido o expirado");
        }
    }

    public static void validateToken(Context ctx) {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.status(401).json(Map.of(
                    "success", false,
                    "message", "Token no proporcionado"
            ));
            return;
        }

        String token = authHeader.substring(7);

        try {
            DecodedJWT jwt = verifyToken(token);
            ctx.attribute("userId", jwt.getSubject());
            ctx.attribute("email", jwt.getClaim("email").asString());
        } catch (IllegalArgumentException e) {
            ctx.status(401).json(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}