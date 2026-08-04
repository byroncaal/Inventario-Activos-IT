package com.example.activo_it;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

// Pantalla de detalle: recibe un Activo + su posición en la lista,
// y devuelve a MainActivity qué acción se hizo (ACTUALIZAR o ELIMINAR).
public class detalle_activo extends AppCompatActivity {

    private Activo activoActual;
    private int posicion;

    // Lanza agregar_activo en modo EDICIÓN. Si vuelve con RESULT_OK,
    // reenviamos ese resultado hacia MainActivity y cerramos esta pantalla también.
    private final ActivityResultLauncher<Intent> lanzadorEditar =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    Activo actualizado = (Activo) resultado.getData().getSerializableExtra("EXTRA_ACTIVO");
                    if (actualizado != null) {
                        Intent resultadoFinal = new Intent();
                        resultadoFinal.putExtra("EXTRA_ACCION", "ACTUALIZAR");
                        resultadoFinal.putExtra("EXTRA_ACTIVO", actualizado);
                        resultadoFinal.putExtra("EXTRA_POSICION", posicion);
                        setResult(Activity.RESULT_OK, resultadoFinal);
                        finish(); // vuelve directo a MainActivity ya actualizado
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalle_activo);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Recupera lo que mandó MainActivity al abrir esta pantalla
        activoActual = (Activo) getIntent().getSerializableExtra("EXTRA_ACTIVO");
        posicion = getIntent().getIntExtra("EXTRA_POSICION", -1);
        if (activoActual == null) {
            Toast.makeText(this, "DEBUG: activoActual llegó null", Toast.LENGTH_LONG).show();
        }

        ImageView ivFotoDetalle = findViewById(R.id.ivFotoDetalle);
        TextView tvEtiquetaDetalle = findViewById(R.id.tvEtiquetaDetalle);
        TextView tvModeloDetalle = findViewById(R.id.tvModeloDetalle);
        TextView tvSerieDetalle = findViewById(R.id.tvSerieDetalle);
        TextView tvEstadoDetalle = findViewById(R.id.tvEstadoDetalle);
        MaterialButton btnEditar = findViewById(R.id.btnEditar);
        MaterialButton btnEliminar = findViewById(R.id.btnEliminar);

        if (activoActual != null) {
            tvEtiquetaDetalle.setText(activoActual.getEtiqueta());
            tvModeloDetalle.setText("Modelo: " + activoActual.getModelo());
            tvSerieDetalle.setText("Serie: " + activoActual.getSerie());
            tvEstadoDetalle.setText("Estado: " + activoActual.getEstado());

            // Solo intenta cargar la foto si el activo tiene una guardada
            String foto = activoActual.getFoto();
            if (foto != null && !foto.isEmpty()) {
                ivFotoDetalle.setImageURI(Uri.parse(foto));
            }
        }

        // Editar: abre agregar_activo precargado con los datos actuales + su posición
        btnEditar.setOnClickListener(v -> {
            Intent intent = new Intent(this, agregar_activo.class);
            intent.putExtra("EXTRA_ACTIVO", activoActual);
            intent.putExtra("EXTRA_POSICION", posicion);
            lanzadorEditar.launch(intent);
        });

        // Eliminar: pide confirmación antes de borrar (requisito obligatorio)
        btnEliminar.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar activo")
                    .setMessage("¿Seguro que deseas eliminar \"" + activoActual.getEtiqueta() + "\"?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        Intent resultado = new Intent();
                        resultado.putExtra("EXTRA_ACCION", "ELIMINAR");
                        resultado.putExtra("EXTRA_POSICION", posicion);
                        setResult(Activity.RESULT_OK, resultado);
                        finish();
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });
    }
}