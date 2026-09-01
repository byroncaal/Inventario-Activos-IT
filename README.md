# Inventario de Activos IT

Android app for managing an organization's IT asset inventory — laptops, monitors, printers, and other equipment — with local authentication and persistent storage via SQLite.

Built as part of an academic activity (RA01) to practice: user login, RecyclerView, SQLite persistence, and full CRUD operations from Java.

## Features

- **Login** — username/password authentication backed by SQLite (SHA-256 hashed passwords, never stored in plain text).
- **Asset inventory (CRUD)**
  - **Create** — add a new asset through an 18-field form (label, type, brand, model, serial, status, assignment, location, tech specs, purchase info, notes, photo).
  - **Read** — browse all assets in a `RecyclerView`, with a live search bar.
  - **Update** — edit any existing asset; changes save back to SQLite.
  - **Delete** — remove an asset with a confirmation dialog.
- **QR / barcode scanning** — scan an asset tag or serial number directly into the form using Google's on-device code scanner (no camera permission dialog needed for the scanner itself).
- **Camera & gallery** — attach a photo to each asset, taken with the device camera or picked from the gallery.
- **Persistent storage** — all data is written to a real SQLite database file on disk. It survives closing the app, killing it from recents, and rebooting the device. Data is only lost if the app is uninstalled or its storage is manually cleared.

## Tech stack

- **Language:** Java
- **UI:** Android Views + Material Components (Material 3), `RecyclerView`, `TextInputLayout`
- **Storage:** SQLite (`SQLiteOpenHelper`), no external database
- **Scanning:** Google Play Services Code Scanner (`play-services-code-scanner`)
- **Min SDK:** 26 (Android 8.0) · **Target SDK:** 36

## Project structure

```
app/src/main/java/com/example/activo_it/
├── LoginActivity.java         # Entry point — validates credentials against SQLite
├── MainActivity.java          # Asset list (RecyclerView) + search
├── agregar_activo.java        # Create/edit form, date pickers, QR scanner, camera
├── detalle_activo.java        # Read-only detail view + edit/delete actions
├── Activo.java                # Asset model (Serializable, passed via Intent)
├── ActivoAdapter.java         # RecyclerView adapter with manual filtering
├── ActivoDbHelper.java        # SQLiteOpenHelper — schema + full CRUD
└── PasswordUtils.java         # SHA-256 password hashing
```

## Getting started

1. Clone the repo and open it in Android Studio.
2. Add the following line to your local `local.properties` (this file is git-ignored and never committed — choose your own value, it is not published anywhere):
   ```properties
   ADMIN_DEFAULT_PASSWORD=your_chosen_password
   ```
   This value is injected at build time into `BuildConfig.ADMIN_DEFAULT_PASSWORD` and used to seed the default admin account — no credentials are hardcoded in the source, and none are published in this README either.
3. Sync Gradle and run the app on a device or emulator with Google Play Services (required for the barcode scanner).

### Default login

| Field | Value |
|---|---|
| Username | `admin` |
| Password | Whatever you set for `ADMIN_DEFAULT_PASSWORD` in your local `local.properties` (see step 2 above) |

## Security notes

- Passwords are hashed (SHA-256) before being stored — never saved in plain text.
- All SQLite queries use parameterized statements (`?` placeholders), not string concatenation, to prevent SQL injection.
- The default admin password lives in `local.properties` (git-ignored) rather than in source code.
- Internal activities (`MainActivity`, `agregar_activo`, `detalle_activo`) are `exported="false"`; only `LoginActivity` is exported as the app's entry point.

## License

Academic project — RA01, Actividad para desarrollar habilidades y destrezas 03.
