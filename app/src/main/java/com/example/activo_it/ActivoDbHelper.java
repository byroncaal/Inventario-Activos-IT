package com.example.activo_it;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class ActivoDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "activo_it.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_ACTIVOS = "activos";
    public static final String COL_ID = "id";
    public static final String COL_ETIQUETA = "etiqueta";
    public static final String COL_MODELO = "modelo";
    public static final String COL_SERIE = "serie";
    public static final String COL_ESTADO = "estado";
    public static final String COL_FOTO = "foto";

    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_ACTIVOS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ETIQUETA + " TEXT NOT NULL, " +
                    COL_MODELO + " TEXT, " +
                    COL_SERIE + " TEXT, " +
                    COL_ESTADO + " TEXT, " +
                    COL_FOTO + " TEXT)";

    private static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_ACTIVOS;

    public ActivoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE);
        onCreate(db);
    }

    // CREATE - devuelve el id generado por SQLite
    public long insertActivo(Activo activo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ETIQUETA, activo.getEtiqueta());
        values.put(COL_MODELO, activo.getModelo());
        values.put(COL_SERIE, activo.getSerie());
        values.put(COL_ESTADO, activo.getEstado());
        values.put(COL_FOTO, activo.getFoto());

        long id = db.insert(TABLE_ACTIVOS, null, values);
        db.close();
        return id;
    }

    // READ - todos los activos guardados, el más reciente primero
    public List<Activo> obtenerTodos() {
        List<Activo> lista = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_ACTIVOS + " ORDER BY " + COL_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Activo activo = new Activo(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ETIQUETA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MODELO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SERIE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FOTO))
                );
                activo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
                lista.add(activo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // UPDATE
    public int actualizarActivo(Activo activo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ETIQUETA, activo.getEtiqueta());
        values.put(COL_MODELO, activo.getModelo());
        values.put(COL_SERIE, activo.getSerie());
        values.put(COL_ESTADO, activo.getEstado());
        values.put(COL_FOTO, activo.getFoto());

        int filas = db.update(TABLE_ACTIVOS, values, COL_ID + "=?",
                new String[]{String.valueOf(activo.getId())});
        db.close();
        return filas;
    }

    // DELETE
    public void eliminarActivo(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACTIVOS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
}