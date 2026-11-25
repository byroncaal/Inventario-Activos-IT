# Activo IT

Aplicación Android nativa (Java) para gestionar el inventario de activos tecnológicos de una organización: laptops, monitores, impresoras y más. Permite saber qué equipos existen, quién los tiene, dónde están y cuándo vence su garantía.

## Características

- **Login y registro** de usuarios. Las contraseñas se guardan como hash SHA-256, nunca en texto plano.
- **CRUD completo de activos:** crear, listar, ver detalle, editar y eliminar (con confirmación).
- **Buscador** por etiqueta, marca, modelo, serie o estado.
- **Dashboard** con total de activos, activos vigentes, gráfica de barras por tipo y dona de estado (vistas personalizadas con `Canvas`).
- **Base de datos local SQLite:** funciona sin internet.
- **Ficha técnica con la API de Icecat:** consulta por Marca y Modelo y autocompleta Tipo, Procesador, RAM, Almacenamiento y Sistema operativo.
- **Escáner de QR y códigos de barras** (ML Kit) para Etiqueta y Serie.
- **Foto del equipo** desde galería o cámara.
- Selector de fecha nativo y validación campo por campo.

## Arquitectura

Arquitectura por capas con separación de responsabilidades parecida a MVC, apoyada en el patrón **DAO**.

| Capa | Clases |
|---|---|
| Presentación | `LoginActivity`, `RegistroActivity`, `MainActivity`, `agregar_activo`, `detalle_activo`, `ActivoAdapter`, `BarChartView`, `DonutView` |
| Datos | `ActivoDbHelper` (esquema), `ActivoDao`, `UsuarioDao` |
| Servicios externos | `IcecatRetrofitClient`, `IcecatApiService`, `IcecatConfig`, ML Kit Code Scanner, `FileProvider` |
| Utilidades | `PasswordUtils` |

### Modelos

- `Activo`: modelo principal con 19 atributos. Implementa `Serializable` para viajar entre pantallas por `Intent`.
- `usuarios`: tabla manejada por `UsuarioDao` (`username` único y `password_hash`).
- `FichaTecnica`: modelo interno y temporal con los datos extraídos del JSON de Icecat.

### Base de datos

Archivo `activo_it.db` con dos tablas:

- `activos`: id, etiqueta, tipo, marca, modelo, serie, estado, asignado, departamento, ubicación, procesador, RAM, almacenamiento, sistema operativo, fecha de compra, fecha de garantía, proveedor, valor, observaciones y foto (Uri como texto).
- `usuarios`: id, username y password_hash.

## Tecnologías

- Java, Android Studio, Material Design 3
- SQLite (`SQLiteOpenHelper`)
- Retrofit + Gson
- Google ML Kit Code Scanner
- RecyclerView, FileProvider, Activity Result API

## Configuración

1. Clona el repositorio y ábrelo en Android Studio.
2. Define las credenciales de Icecat como campos de `BuildConfig`: `ICECAT_API_TOKEN` e `ICECAT_CONTENT_TOKEN`. No las subas al repositorio.
3. Si tienes cuenta propia de Icecat, cambia `SHOPNAME` en `IcecatConfig`. Por defecto usa la cuenta demo pública `openIcecat-live`.
4. Sincroniza Gradle y ejecuta en un dispositivo o emulador Android.

## Uso rápido

- Usuario inicial: `admin` / contraseña `admin123`. Cámbiala o crea tu propia cuenta desde **Regístrate**.
- Para la guía completa paso a paso, consulta el manual de usuario en la carpeta `Documentos`.

## Permisos

- `CAMERA`: tomar fotos del equipo.
- `INTERNET`: consultar la ficha técnica en Icecat.

## Documentación

En la carpeta `Documentos`:

- `Activo_IT_Manual_de_Usuario.pdf`
- `Activo_IT_Speech.pdf`

## Posibles mejoras

- Migrar a Room con ViewModel y LiveData.
- Agregar *salt* al hash de contraseñas (bcrypt o PBKDF2).
- Guardar las fotos en almacenamiento interno.
- Recuperación de contraseña.
