package com.example.activo_it;

import java.security.MessageDigest;

public class PasswordUtils {

    // Convierte una contraseña en texto plano a su hash SHA-256 en hexadecimal.
    // Nunca guardamos la contraseña real en la base de datos, solo este hash,
    // así que aunque alguien lea el archivo .db directamente, no puede ver la contraseña.
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes("UTF-8"));

            // Convierte cada byte del hash a su representación hexadecimal (2 caracteres)
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // SHA-256 siempre está disponible en Android, así que este catch
            // es solo una salvaguarda, no debería ocurrir en la práctica.
            throw new RuntimeException("Error al hashear la contraseña", e);
        }
    }
}