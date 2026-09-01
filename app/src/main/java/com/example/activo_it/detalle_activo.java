package com.example.activo_it;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

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

        ImageView ivFotoDetalle = findViewById(R.id.ivFotoDetalle);
        TextView tvEtiquetaDetalle = findViewById(R.id.tvEtiquetaDetalle);
        TextView tvEstadoDetalle = findViewById(R.id.tvEstadoDetalle);

        TextView tvTipoDetalle = findViewById(R.id.tvTipoDetalle);
        TextView tvMarcaDetalle = findViewById(R.id.tvMarcaDetalle);
        TextView tvModeloDetalle = findViewById(R.id.tvModeloDetalle);
        TextView tvSerieDetalle = findViewById(R.id.tvSerieDetalle);

        TextView tvAsignadoDetalle = findViewById(R.id.tvAsignadoDetalle);
        TextView tvDepartamentoDetalle = findViewById(R.id.tvDepartamentoDetalle);
        TextView tvUbicacionDetalle = findViewById(R.id.tvUbicacionDetalle);

        TextView tvProcesadorDetalle = findViewById(R.id.tvProcesadorDetalle);
        TextView tvRamDetalle = findViewById(R.id.tvRamDetalle);
        TextView tvAlmacenamientoDetalle = findViewById(R.id.tvAlmacenamientoDetalle);
        TextView tvSistemaOperativoDetalle = findViewById(R.id.tvSistemaOperativoDetalle);

        TextView tvFechaCompraDetalle = findViewById(R.id.tvFechaCompraDetalle);
        TextView tvFechaGarantiaDetalle = findViewById(R.id.tvFechaGarantiaDetalle);
        TextView tvProveedorDetalle = findViewById(R.id.tvProveedorDetalle);
        TextView tvValorDetalle = findViewById(R.id.tvValorDetalle);
        TextView tvObservacionesDetalle = findViewById(R.id.tvObservacionesDetalle);

        MaterialButton btnEditar = findViewById(R.id.btnEditar);
        MaterialButton btnEliminar = findViewById(R.id.btnEliminar);

        if (activoActual != null) {
            tvEtiquetaDetalle.setText(activoActual.getEtiqueta());
            tvEstadoDetalle.setText("Estado: " + valorOGuion(activoActual.getEstado()));

            tvTipoDetalle.setText("Tipo: " + valorOGuion(activoActual.getTipo()));
            tvMarcaDetalle.setText("Marca: " + valorOGuion(activoActual.getMarca()));
            tvModeloDetalle.setText("Modelo: " + valorOGuion(activoActual.getModelo()));
            tvSerieDetalle.setText("Serie: " + valorOGuion(activoActual.getSerie()));

            tvAsignadoDetalle.setText("Asignado a: " + valorOGuion(activoActual.getAsignado()));
            tvDepartamentoDetalle.setText("Departamento: " + valorOGuion(activoActual.getDepartamento()));
            tvUbicacionDetalle.setText("Ubicación: " + valorOGuion(activoActual.getUbicacion()));

            tvProcesadorDetalle.setText("Procesador: " + valorOGuion(activoActual.getProcesador()));
            tvRamDetalle.setText("RAM: " + valorOGuion(activoActual.getRam()));
            tvAlmacenamientoDetalle.setText("Almacenamiento: " + valorOGuion(activoActual.getAlmacenamiento()));
            tvSistemaOperativoDetalle.setText("Sistema operativo: " + valorOGuion(activoActual.getSistemaOperativo()));

            tvFechaCompraDetalle.setText("Fecha de compra: " + valorOGuion(activoActual.getFechaCompra()));
            tvFechaGarantiaDetalle.setText("Vencimiento de garantía: " + valorOGuion(activoActual.getFechaVencimientoGarantia()));
            tvProveedorDetalle.setText("Proveedor: " + valorOGuion(activoActual.getProveedor()));
            tvValorDetalle.setText("Valor: " + (activoActual.getValor() != 0 ? String.valueOf(activoActual.getValor()) : "-"));
            tvObservacionesDetalle.setText(valorOGuion(activoActual.getObservaciones()));

            // Solo intenta cargar la foto si el activo tiene una guardada
            String foto = activoActual.getFoto();
            if (foto != null && !foto.isEmpty()) {
                try {
                    ivFotoDetalle.setImageURI(Uri.parse(foto));
                } catch (SecurityException e) {
                    ivFotoDetalle.setImageResource(android.R.drawable.ic_menu_camera);
                }
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

    // Muestra "-" en vez de dejar vacío un campo opcional que no se llenó
    private String valorOGuion(String valor) {
        return (valor == null || valor.isEmpty()) ? "-" : valor;
    }
}