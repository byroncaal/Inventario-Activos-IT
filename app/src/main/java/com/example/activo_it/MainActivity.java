package com.example.activo_it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<Activo> activos = new ArrayList<>();
    private ArrayAdapter<Activo> adapter;
    private TextView tvContador;

    private final ActivityResultLauncher<Intent> lanzadorFormulario =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    Activo nuevo = (Activo) resultado.getData().getSerializableExtra("EXTRA_ACTIVO");
                    if (nuevo != null) {
                        activos.add(nuevo);
                        adapter.notifyDataSetChanged();
                        actualizarContador();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> lanzadorDetalle =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    Intent data = resultado.getData();
                    String accion = data.getStringExtra("EXTRA_ACCION");
                    int posicion = data.getIntExtra("EXTRA_POSICION", -1);

                    if (posicion < 0 || posicion >= activos.size()) return;

                    if ("ACTUALIZAR".equals(accion)) {
                        Activo actualizado = (Activo) data.getSerializableExtra("EXTRA_ACTIVO");
                        if (actualizado != null) {
                            activos.set(posicion, actualizado);
                        }
                    } else if ("ELIMINAR".equals(accion)) {
                        activos.remove(posicion);
                    }

                    adapter.notifyDataSetChanged();
                    actualizarContador();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (View v, WindowInsetsCompat insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnNuevo = findViewById(R.id.btnNuevo);
        ListView lvActivos = findViewById(R.id.lvActivos);
        EditText etBuscar = findViewById(R.id.etBuscar);
        tvContador = findViewById(R.id.tvContador);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, activos);
        lvActivos.setAdapter(adapter);

        actualizarContador();

        btnNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(this, agregar_activo.class);
            lanzadorFormulario.launch(intent);
        });

        // Filtro en vivo: el ArrayAdapter ya sabe filtrar usando el toString() de Activo
        // (etiqueta, modelo, serie, estado), así que basta con conectarlo al EditText.
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        lvActivos.setOnItemClickListener((parent, view, position, id) -> {
            // IMPORTANTE: cuando hay un filtro activo, "position" es la posición dentro
            // de los resultados FILTRADOS, no el índice real en "activos". Por eso pedimos
            // el objeto ya filtrado y buscamos su índice verdadero antes de abrir el detalle.
            Activo seleccionado = adapter.getItem(position);
            int indiceReal = activos.indexOf(seleccionado);

            Intent intent = new Intent(this, detalle_activo.class);
            intent.putExtra("EXTRA_ACTIVO", seleccionado);
            intent.putExtra("EXTRA_POSICION", indiceReal);
            lanzadorDetalle.launch(intent);
        });
    }

    private void actualizarContador() {
        tvContador.setText("Activos (" + activos.size() + "):");
    }
}