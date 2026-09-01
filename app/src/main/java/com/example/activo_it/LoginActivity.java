package com.example.activo_it;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// Pantalla de entrada de la app. Valida usuario/contraseña contra la tabla
// "usuarios" de SQLite (ver ActivoDbHelper.validarCredenciales).
public class LoginActivity extends AppCompatActivity {

    private ActivoDbHelper dbHelper;
    private TextInputLayout tilUsuario, tilPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Ajusta el padding para que el contenido no quede debajo de la barra de estado/navegación
        View vistaRaiz = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(vistaRaiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dbHelper = new ActivoDbHelper(this);

        tilUsuario = findViewById(R.id.tilUsuario);
        tilPassword = findViewById(R.id.tilPassword);
        TextInputEditText etUsuario = findViewById(R.id.etUsuario);
        TextInputEditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnIngresar = findViewById(R.id.btnIngresar);

        btnIngresar.setOnClickListener(v -> {
            String usuario = etUsuario.getText() != null ? etUsuario.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            boolean esValido = true;

            // Marca cada campo vacío con su propio error (setError), sin usar Toast
            if (TextUtils.isEmpty(usuario)) {
                tilUsuario.setError("Ingresa tu usuario");
                esValido = false;
            } else {
                tilUsuario.setError(null);
            }

            if (TextUtils.isEmpty(password)) {
                tilPassword.setError("Ingresa tu contraseña");
                esValido = false;
            } else {
                tilPassword.setError(null);
            }

            if (!esValido) {
                return;
            }

            if (dbHelper.validarCredenciales(usuario, password)) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish(); // el botón "atrás" no debe regresar al login
            } else {
                tilPassword.setError("Usuario o contraseña incorrectos");
            }
        });
    }
}