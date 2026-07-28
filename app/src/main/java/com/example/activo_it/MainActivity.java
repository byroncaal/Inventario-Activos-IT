package com.example.activo_it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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

    // Lista en memoria: guarda todos los activos mientras la app esté abierta
    private final ArrayList<Activo> activos = new ArrayList<>();

    private ArrayAdapter<Activo> adapter;
    private TextView tvContador;

    // Lanzador que abre agregar_activo y ESPERA un resultado (el nuevo Activo).
    // En vez de startActivity() simple, este patrón permite recibir datos de vuelta.
    private final ActivityResultLauncher<Intent> lanzadorFormulario =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    // Recupera el objeto Activo que devolvió agregar_activo
                    Activo nuevo = (Activo) resultado.getData().getSerializableExtra("EXTRA_ACTIVO");
                    if (nuevo != null) {
                        activos.add(nuevo);
                        adapter.notifyDataSetChanged();
                        actualizarContador();
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ajusta el padding para que el contenido no quede debajo de la barra de estado/navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (View v, WindowInsetsCompat insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnNuevo = findViewById(R.id.btnNuevo);
        ListView lvActivos = findViewById(R.id.lvActivos);
        tvContador = findViewById(R.id.tvContador);


        // ArrayAdapter genérico: usa el toString() de Activo para dibujar cada fila
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, activos);
        lvActivos.setAdapter(adapter);

        actualizarContador();

        // abrir el formulario para agregar un activo nuevo
        btnNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(this, agregar_activo.class);
            lanzadorFormulario.launch(intent);
        });

        // borrar: al tocar un activo de la lista, se elimina
        lvActivos.setOnItemClickListener((parent, view, position, id) -> {
            activos.remove(position);
            adapter.notifyDataSetChanged();
            actualizarContador();
        });
    }

    // Actualiza el texto "Activos (N):" según cuántos elementos hay en la lista
    private void actualizarContador() {
        tvContador.setText("Activos (" + activos.size() + "):");
    }
}