package com.example.activo_it;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

// DAO (Data Access Object): concentra TODO el acceso a datos de la tabla "activos".
// MainActivity, agregar_activo y detalle_activo hablan con esta clase,
// nunca directamente con SQLiteDatabase.
public class ActivoDao {

    private final ActivoDbHelper dbHelper;

    public ActivoDao(Context context) {
        dbHelper = new ActivoDbHelper(context);
    }

    // CREATE - devuelve el id generado por SQLite
    public long insertar(Activo activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = construirValues(activo);
        long id = db.insert(ActivoDbHelper.TABLE_ACTIVOS, null, values);
        db.close();
        return id;
    }

    // READ - todos los activos guardados, el más reciente primero
    public List<Activo> obtenerTodos() {
        List<Activo> lista = new ArrayList<>();
        String query = "SELECT * FROM " + ActivoDbHelper.TABLE_ACTIVOS + " ORDER BY " + ActivoDbHelper.COL_ID + " DESC";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Activo activo = new Activo(
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_ETIQUETA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_TIPO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_MARCA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_MODELO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_SERIE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_ESTADO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_ASIGNADO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_DEPARTAMENTO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_UBICACION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_PROCESADOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_RAM)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_ALMACENAMIENTO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_SISTEMA_OPERATIVO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_FECHA_COMPRA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_FECHA_GARANTIA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_PROVEEDOR)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_VALOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_OBSERVACIONES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_FOTO))
                );
                activo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(ActivoDbHelper.COL_ID)));
                lista.add(activo);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }

    // UPDATE
    public int actualizar(Activo activo) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = construirValues(activo);
        int filas = db.update(ActivoDbHelper.TABLE_ACTIVOS, values, ActivoDbHelper.COL_ID + "=?",
                new String[]{String.valueOf(activo.getId())});
        db.close();
        return filas;
    }

    // DELETE
    public void eliminar(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(ActivoDbHelper.TABLE_ACTIVOS, ActivoDbHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    private ContentValues construirValues(Activo activo) {
        ContentValues values = new ContentValues();
        values.put(ActivoDbHelper.COL_ETIQUETA, activo.getEtiqueta());
        values.put(ActivoDbHelper.COL_TIPO, activo.getTipo());
        values.put(ActivoDbHelper.COL_MARCA, activo.getMarca());
        values.put(ActivoDbHelper.COL_MODELO, activo.getModelo());
        values.put(ActivoDbHelper.COL_SERIE, activo.getSerie());
        values.put(ActivoDbHelper.COL_ESTADO, activo.getEstado());
        values.put(ActivoDbHelper.COL_ASIGNADO, activo.getAsignado());
        values.put(ActivoDbHelper.COL_DEPARTAMENTO, activo.getDepartamento());
        values.put(ActivoDbHelper.COL_UBICACION, activo.getUbicacion());
        values.put(ActivoDbHelper.COL_PROCESADOR, activo.getProcesador());
        values.put(ActivoDbHelper.COL_RAM, activo.getRam());
        values.put(ActivoDbHelper.COL_ALMACENAMIENTO, activo.getAlmacenamiento());
        values.put(ActivoDbHelper.COL_SISTEMA_OPERATIVO, activo.getSistemaOperativo());
        values.put(ActivoDbHelper.COL_FECHA_COMPRA, activo.getFechaCompra());
        values.put(ActivoDbHelper.COL_FECHA_GARANTIA, activo.getFechaVencimientoGarantia());
        values.put(ActivoDbHelper.COL_PROVEEDOR, activo.getProveedor());
        values.put(ActivoDbHelper.COL_VALOR, activo.getValor());
        values.put(ActivoDbHelper.COL_OBSERVACIONES, activo.getObservaciones());
        values.put(ActivoDbHelper.COL_FOTO, activo.getFoto());
        return values;
    }
}