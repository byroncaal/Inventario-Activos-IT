package com.example.activo_it;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Solo se encarga del ESQUEMA de la base de datos (crear/actualizar tablas).
// El acceso a los datos (CRUD) vive en las clases DAO: ActivoDao y UsuarioDao.
public class ActivoDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "activo_it.db";
    private static final int DATABASE_VERSION = 3;

    // --- Tabla de activos ---
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

    // --- Tabla de usuarios (login) ---
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
        // Solo se recrea la tabla de activos; la de usuarios se conserva
        // entre actualizaciones para no perder las cuentas ya creadas/registradas.
        db.execSQL(SQL_DELETE_TABLE_ACTIVOS);
        onCreate(db);
    }

    // Crea admin/admin123 la primera vez. INSERT OR IGNORE respeta el UNIQUE
    // de username, así nunca se duplica ni sobreescribe en ejecuciones futuras.
    private void crearUsuarioPorDefecto(SQLiteDatabase db) {
        String passwordHash = PasswordUtils.hashPassword("admin123");
        db.execSQL("INSERT OR IGNORE INTO " + TABLE_USUARIOS +
                        " (" + COL_USERNAME + ", " + COL_PASSWORD_HASH + ") VALUES (?, ?)",
                new Object[]{"admin", passwordHash});
    }
}