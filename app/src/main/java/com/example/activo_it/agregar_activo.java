package com.example.activo_it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

public class agregar_activo extends AppCompatActivity {

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

        TextInputEditText etEtiqueta = findViewById(R.id.etEtiqueta);
        TextInputEditText etModelo = findViewById(R.id.etModelo);
        TextInputEditText etSerie = findViewById(R.id.etSerie);
        MaterialSwitch switchEstado = findViewById(R.id.switchEstado);
        TextView tvEstadoLabel = findViewById(R.id.tvEstadoLabel);
        MaterialButton btnGuardar = findViewById(R.id.btnGuardar);
        MaterialButton btnCancelar = findViewById(R.id.btnCancelar);

        // Cada vez que se mueve el switch, actualiza el texto de arriba (Activo/Baja)
        switchEstado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String estado = isChecked ? Activo.ESTADO_ACTIVO : Activo.ESTADO_BAJA;
            tvEstadoLabel.setText("Estado: " + estado);
        });

        btnGuardar.setOnClickListener(v -> {
            String etiqueta = etEtiqueta.getText() != null ? etEtiqueta.getText().toString().trim() : "";
            String modelo = etModelo.getText() != null ? etModelo.getText().toString().trim() : "";
            String serie = etSerie.getText() != null ? etSerie.getText().toString().trim() : "";

            // Validación simple: ningún campo puede quedar vacío
            if (etiqueta.isEmpty() || modelo.isEmpty() || serie.isEmpty()) {
                Toast.makeText(this, "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            String estado = switchEstado.isChecked() ? Activo.ESTADO_ACTIVO : Activo.ESTADO_BAJA;
            Activo nuevo = new Activo(etiqueta, modelo, serie, estado);

            // Empaqueta el nuevo Activo dentro del resultado, para que
            // MainActivity lo reciba en el callback de lanzadorFormulario
            Intent resultado = new Intent();
            resultado.putExtra("EXTRA_ACTIVO", nuevo);
            setResult(Activity.RESULT_OK, resultado);

            Toast.makeText(this, "Activo agregado", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Cancelar: le avisa a MainActivity que no hay nada que agregar
        btnCancelar.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }
}