package com.example.activo_it;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegistroActivity extends AppCompatActivity {

    private UsuarioDao usuarioDao;
    private TextInputLayout tilNuevoUsuario, tilNuevaPassword, tilConfirmarPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        View vistaRaiz = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(vistaRaiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        usuarioDao = new UsuarioDao(this);

        tilNuevoUsuario = findViewById(R.id.tilNuevoUsuario);
        tilNuevaPassword = findViewById(R.id.tilNuevaPassword);
        tilConfirmarPassword = findViewById(R.id.tilConfirmarPassword);

        TextInputEditText etNuevoUsuario = findViewById(R.id.etNuevoUsuario);
        TextInputEditText etNuevaPassword = findViewById(R.id.etNuevaPassword);
        TextInputEditText etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
        MaterialButton btnRegistrar = findViewById(R.id.btnRegistrar);
        MaterialButton btnVolverLogin = findViewById(R.id.btnVolverLogin);

        btnRegistrar.setOnClickListener(v -> {
            String usuario = etNuevoUsuario.getText() != null ? etNuevoUsuario.getText().toString().trim() : "";
            String password = etNuevaPassword.getText() != null ? etNuevaPassword.getText().toString().trim() : "";
            String confirmarPassword = etConfirmarPassword.getText() != null ? etConfirmarPassword.getText().toString().trim() : "";

            if (!validarCampos(usuario, password, confirmarPassword)) {
                return;
            }

            boolean exito = usuarioDao.registrar(usuario, password);

            if (exito) {
                Toast.makeText(this, "Cuenta creada. Ahora inicia sesión.", Toast.LENGTH_LONG).show();
                finish();
            } else {
                tilNuevoUsuario.setError("Ese usuario ya existe, elige otro");
            }
        });

        btnVolverLogin.setOnClickListener(v -> finish());
    }

    private boolean validarCampos(String usuario, String password, String confirmarPassword) {
        boolean esValido = true;

        if (TextUtils.isEmpty(usuario)) {
            tilNuevoUsuario.setError("Ingresa un usuario");
            esValido = false;
        } else {
            tilNuevoUsuario.setError(null);
        }

        if (TextUtils.isEmpty(password)) {
            tilNuevaPassword.setError("Ingresa una contraseña");
            esValido = false;
        } else if (password.length() < 6) {
            tilNuevaPassword.setError("Debe tener al menos 6 caracteres");
            esValido = false;
        } else {
            tilNuevaPassword.setError(null);
        }

        if (TextUtils.isEmpty(confirmarPassword)) {
            tilConfirmarPassword.setError("Confirma tu contraseña");
            esValido = false;
        } else if (!confirmarPassword.equals(password)) {
            tilConfirmarPassword.setError("Las contraseñas no coinciden");
            esValido = false;
        } else {
            tilConfirmarPassword.setError(null);
        }

        return esValido;
    }
}