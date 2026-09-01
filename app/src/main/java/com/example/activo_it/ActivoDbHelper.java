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
    private static final int DATABASE_VERSION = 3;

    public static final String TABLE_ACTIVOS = "activos";
    public static final String COL_ID = "id";
    public static final String COL_ETIQUETA = "etiqueta";
    public static final String COL_TIPO = "tipo";
    public static final String COL_MARCA = "marca";
    public static final String COL_MODELO = "modelo";
    public static final String COL_SERIE = "serie";
    public static final String COL_ESTADO = "estado";
    public static final String COL_ASIGNADO = "asignado";
    public static final String COL_DEPARTAMENTO = "departamento";
    public static final String COL_UBICACION = "ubicacion";
    public static final String COL_PROCESADOR = "procesador";
    public static final String COL_RAM = "ram";
    public static final String COL_ALMACENAMIENTO = "almacenamiento";
    public static final String COL_SISTEMA_OPERATIVO = "sistema_operativo";
    public static final String COL_FECHA_COMPRA = "fecha_compra";
    public static final String COL_FECHA_GARANTIA = "fecha_garantia";
    public static final String COL_PROVEEDOR = "proveedor";
    public static final String COL_VALOR = "valor";
    public static final String COL_OBSERVACIONES = "observaciones";
    public static final String COL_FOTO = "foto";

    public static final String TABLE_USUARIOS = "usuarios";
    public static final String COL_USER_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD_HASH = "password_hash";

    private static final String SQL_CREATE_TABLE_ACTIVOS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_ACTIVOS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_ETIQUETA + " TEXT NOT NULL, " +
                    COL_TIPO + " TEXT, " +
                    COL_MARCA + " TEXT, " +
                    COL_MODELO + " TEXT, " +
                    COL_SERIE + " TEXT, " +
                    COL_ESTADO + " TEXT, " +
                    COL_ASIGNADO + " TEXT, " +
                    COL_DEPARTAMENTO + " TEXT, " +
                    COL_UBICACION + " TEXT, " +
                    COL_PROCESADOR + " TEXT, " +
                    COL_RAM + " TEXT, " +
                    COL_ALMACENAMIENTO + " TEXT, " +
                    COL_SISTEMA_OPERATIVO + " TEXT, " +
                    COL_FECHA_COMPRA + " TEXT, " +
                    COL_FECHA_GARANTIA + " TEXT, " +
                    COL_PROVEEDOR + " TEXT, " +
                    COL_VALOR + " REAL, " +
                    COL_OBSERVACIONES + " TEXT, " +
                    COL_FOTO + " TEXT)";

    private static final String SQL_CREATE_TABLE_USUARIOS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USUARIOS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USERNAME + " TEXT NOT NULL UNIQUE, " +
                    COL_PASSWORD_HASH + " TEXT NOT NULL)";

    private static final String SQL_DELETE_TABLE_ACTIVOS =
            "DROP TABLE IF EXISTS " + TABLE_ACTIVOS;

    public ActivoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_ACTIVOS);
        db.execSQL(SQL_CREATE_TABLE_USUARIOS);
        crearUsuarioPorDefecto(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_TABLE_ACTIVOS);
        onCreate(db);
    }

    // Crea admin/admin123 la primera vez que se crea la base de datos.
    // La contraseña por defecto vive en local.properties (BuildConfig.ADMIN_DEFAULT_PASSWORD),
    // no escrita literal aquí, para no exponer credenciales en el código fuente versionado.
    private void crearUsuarioPorDefecto(SQLiteDatabase db) {
        String passwordHash = PasswordUtils.hashPassword(BuildConfig.ADMIN_DEFAULT_PASSWORD);
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USUARIOS +
                        " (" + COL_USERNAME + ", " + COL_PASSWORD_HASH + ") VALUES (?, ?)",
                new Object[]{"admin", passwordHash});
    }

    public boolean validarCredenciales(String username, String password) {
        String passwordHash = PasswordUtils.hashPassword(password);

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USUARIOS,
                new String[]{COL_USER_ID},
                COL_USERNAME + " = ? AND " + COL_PASSWORD_HASH + " = ?",
                new String[]{username, passwordHash},
                null, null, null
        );

        boolean valido = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return valido;
    }

    public long insertActivo(Activo activo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = construirValues(activo);
        long id = db.insert(TABLE_ACTIVOS, null, values);
        db.close();
        return id;
    }

    public List<Activo> obtenerTodos() {
        List<Activo> lista = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_ACTIVOS + " ORDER BY " + COL_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Activo activo = new Activo(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ETIQUETA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TIPO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MARCA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_MODELO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SERIE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ESTADO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ASIGNADO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DEPARTAMENTO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_UBICACION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PROCESADOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_RAM)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_ALMACENAMIENTO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_SISTEMA_OPERATIVO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_COMPRA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA_GARANTIA)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_PROVEEDOR)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(COL_VALOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_OBSERVACIONES)),
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

    public int actualizarActivo(Activo activo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = construirValues(activo);
        int filas = db.update(TABLE_ACTIVOS, values, COL_ID + "=?",
                new String[]{String.valueOf(activo.getId())});
        db.close();
        return filas;
    }

    public void eliminarActivo(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ACTIVOS, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    private ContentValues construirValues(Activo activo) {
        ContentValues values = new ContentValues();
        values.put(COL_ETIQUETA, activo.getEtiqueta());
        values.put(COL_TIPO, activo.getTipo());
        values.put(COL_MARCA, activo.getMarca());
        values.put(COL_MODELO, activo.getModelo());
        values.put(COL_SERIE, activo.getSerie());
        values.put(COL_ESTADO, activo.getEstado());
        values.put(COL_ASIGNADO, activo.getAsignado());
        values.put(COL_DEPARTAMENTO, activo.getDepartamento());
        values.put(COL_UBICACION, activo.getUbicacion());
        values.put(COL_PROCESADOR, activo.getProcesador());
        values.put(COL_RAM, activo.getRam());
        values.put(COL_ALMACENAMIENTO, activo.getAlmacenamiento());
        values.put(COL_SISTEMA_OPERATIVO, activo.getSistemaOperativo());
        values.put(COL_FECHA_COMPRA, activo.getFechaCompra());
        values.put(COL_FECHA_GARANTIA, activo.getFechaVencimientoGarantia());
        values.put(COL_PROVEEDOR, activo.getProveedor());
        values.put(COL_VALOR, activo.getValor());
        values.put(COL_OBSERVACIONES, activo.getObservaciones());
        values.put(COL_FOTO, activo.getFoto());
        return values;
    }
}