package com.example.activo_it;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

public class agregar_activo extends AppCompatActivity {

    private Uri fotoUri = null;
    private ImageView ivFoto;

    // Guardamos si venimos en modo edición y, de ser así, en qué posición
    // de la lista original está el activo (-1 significa "es uno nuevo")
    private boolean esEdicion = false;
    private int posicionRecibida = -1;

    private final ActivityResultLauncher<String> seleccionarFoto =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoUri = uri;
                    ivFoto.setImageURI(uri);
                }
            });

    private final ActivityResultLauncher<Uri> tomarFoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), exito -> {
                if (exito) {
                    ivFoto.setImageURI(fotoUri);
                }
            });

    private final ActivityResultLauncher<String> pedirPermisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    abrirCamara();
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_activo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvTituloFormulario = findViewById(R.id.tvTituloFormulario);
        TextInputEditText etEtiqueta = findViewById(R.id.etEtiqueta);
        TextInputEditText etModelo = findViewById(R.id.etModelo);
        TextInputEditText etSerie = findViewById(R.id.etSerie);
        MaterialSwitch switchEstado = findViewById(R.id.switchEstado);
        TextView tvEstadoLabel = findViewById(R.id.tvEstadoLabel);
        MaterialCheckBox cbTerminos = findViewById(R.id.cbTerminos);
        MaterialButton btnFoto = findViewById(R.id.btnFoto);
        MaterialButton btnCamara = findViewById(R.id.btnCamara);
        MaterialButton btnGuardar = findViewById(R.id.btnGuardar);
        MaterialButton btnCancelar = findViewById(R.id.btnCancelar);
        ivFoto = findViewById(R.id.ivFoto);

        // ¿Nos mandaron un Activo existente? Si sí, esto es una EDICIÓN
        Activo activoRecibido = (Activo) getIntent().getSerializableExtra("EXTRA_ACTIVO");
        posicionRecibida = getIntent().getIntExtra("EXTRA_POSICION", -1);
        esEdicion = (posicionRecibida != -1 && activoRecibido != null);

        if (esEdicion) {
            // Precarga el formulario con los datos actuales del activo
            tvTituloFormulario.setText("Editar activo");
            btnGuardar.setText("Guardar cambios");

            etEtiqueta.setText(activoRecibido.getEtiqueta());
            etModelo.setText(activoRecibido.getModelo());
            etSerie.setText(activoRecibido.getSerie());

            boolean estaActivo = Activo.ESTADO_ACTIVO.equals(activoRecibido.getEstado());
            switchEstado.setChecked(estaActivo);
            tvEstadoLabel.setText("Estado: " + activoRecibido.getEstado());

            // Ya había aceptado los términos la primera vez
            cbTerminos.setChecked(true);

            // Si ya tenía foto guardada, la mostramos
            String fotoExistente = activoRecibido.getFoto();
            if (fotoExistente != null && !fotoExistente.isEmpty()) {
                fotoUri = Uri.parse(fotoExistente);
                ivFoto.setImageURI(fotoUri);
            }
        }

        switchEstado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String estado = isChecked ? Activo.ESTADO_ACTIVO : Activo.ESTADO_BAJA;
            tvEstadoLabel.setText("Estado: " + estado);
        });

        btnFoto.setOnClickListener(v -> seleccionarFoto.launch("image/*"));

        btnCamara.setOnClickListener(v -> {
            boolean tienePermiso = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED;
            if (tienePermiso) {
                abrirCamara();
            } else {
                pedirPermisoCamara.launch(android.Manifest.permission.CAMERA);
            }
        });

        btnGuardar.setOnClickListener(v -> {
            String etiqueta = etEtiqueta.getText() != null ? etEtiqueta.getText().toString().trim() : "";
            String modelo = etModelo.getText() != null ? etModelo.getText().toString().trim() : "";
            String serie = etSerie.getText() != null ? etSerie.getText().toString().trim() : "";

            if (etiqueta.isEmpty() || modelo.isEmpty() || serie.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!cbTerminos.isChecked()) {
                Toast.makeText(this, "Debes aceptar los términos", Toast.LENGTH_SHORT).show();
                return;
            }

            String estado = switchEstado.isChecked() ? Activo.ESTADO_ACTIVO : Activo.ESTADO_BAJA;
            String foto = fotoUri != null ? fotoUri.toString() : "";
            Activo activoFinal = new Activo(etiqueta, modelo, serie, estado, foto);

            Intent resultado = new Intent();
            resultado.putExtra("EXTRA_ACTIVO", activoFinal);

            // Solo agregamos la posición si es edición; MainActivity (modo Crear)
            // no la necesita porque simplemente hace .add() al final de la lista
            if (esEdicion) {
                resultado.putExtra("EXTRA_POSICION", posicionRecibida);
            }

            setResult(Activity.RESULT_OK, resultado);
            Toast.makeText(this, esEdicion ? "Activo actualizado" : "Activo agregado", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnCancelar.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    /*private void abrirCamara() {
        try {
            File archivo = new File(getExternalFilesDir("Pictures"), "foto_" + System.currentTimeMillis() + ".jpg");
            fotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivo);
            tomarFoto.launch(fotoUri);
        } catch (Exception e) {
            Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }*/
    private void abrirCamara() {
        try {
            File archivo = new File(getExternalFilesDir("Pictures"), "foto_" + System.currentTimeMillis() + ".jpg");
            fotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivo);
            tomarFoto.launch(fotoUri);
        } catch (Exception e) {
            e.printStackTrace(); // <-- agrega esto para ver el error real en Logcat
            Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

}