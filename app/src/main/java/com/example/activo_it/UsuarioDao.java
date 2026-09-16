package com.example.activo_it;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

// DAO: concentra TODO el acceso a datos de la tabla "usuarios" (login y registro).
public class UsuarioDao {

    private final ActivoDbHelper dbHelper;

    public UsuarioDao(Context context) {
        dbHelper = new ActivoDbHelper(context);
    }

    // Compara contra el HASH guardado, nunca contra texto plano.
    public boolean validarCredenciales(String username, String password) {
        String passwordHash = PasswordUtils.hashPassword(password);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                ActivoDbHelper.TABLE_USUARIOS,
                new String[]{ActivoDbHelper.COL_USER_ID},
                ActivoDbHelper.COL_USERNAME + " = ? AND " + ActivoDbHelper.COL_PASSWORD_HASH + " = ?",
                new String[]{username, passwordHash},
                null, null, null
        );

        boolean valido = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return valido;
    }

    // Registra un nuevo usuario. Devuelve true si se creó correctamente,
    // false si el username ya existe (UNIQUE hace que insert() devuelva -1).
    public boolean registrar(String username, String password) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ActivoDbHelper.COL_USERNAME, username);
        values.put(ActivoDbHelper.COL_PASSWORD_HASH, PasswordUtils.hashPassword(password));

        long id = db.insert(ActivoDbHelper.TABLE_USUARIOS, null, values);
        db.close();
        return id != -1;
    }
}