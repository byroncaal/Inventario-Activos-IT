package com.example.activo_it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Lista en memoria: guarda todos los activos mientras la app esté abierta
    private final ArrayList<Activo> activos = new ArrayList<>();

    // Adapter personalizado (dibuja MaterialCardView), pero sigue siendo un
    // ArrayAdapter por dentro, así que conserva getFilter() para el buscador
    private ActivoAdapter adapter;

    private TextView tvContador;

    // Lanzador para CREAR: abre agregar_activo vacío y espera un Activo nuevo de vuelta
    private final ActivityResultLauncher<Intent> lanzadorFormulario =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    Activo nuevo = (Activo) resultado.getData().getSerializableExtra("EXTRA_ACTIVO");
                    if (nuevo != null) {
                        activos.add(nuevo);
                        adapter.notifyDataSetChanged(); // redibuja el ListView con el nuevo elemento
                        actualizarContador();
                    }
                }
            });

    // Lanzador para el DETALLE: espera de vuelta una acción (ACTUALIZAR o ELIMINAR)
    // junto con la posición REAL del elemento afectado dentro de "activos".
    private final ActivityResultLauncher<Intent> lanzadorDetalle =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    Intent data = resultado.getData();
                    String accion = data.getStringExtra("EXTRA_ACCION");
                    int posicion = data.getIntExtra("EXTRA_POSICION", -1);

                    // Por seguridad: si la posición no es válida, no tocamos nada
                    if (posicion < 0 || posicion >= activos.size()) return;

                    if ("ACTUALIZAR".equals(accion)) {
                        Activo actualizado = (Activo) data.getSerializableExtra("EXTRA_ACTIVO");
                        if (actualizado != null) {
                            activos.set(posicion, actualizado); // reemplaza el activo viejo por el editado
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

        // Ajusta el padding para que el contenido no quede debajo de la barra de estado/navegación
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (View v, WindowInsetsCompat insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Registra la MaterialToolbar como la ActionBar de esta pantalla
        MaterialToolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        MaterialButton btnNuevo = findViewById(R.id.btnNuevo);
        ListView lvActivos = findViewById(R.id.lvActivos);
        TextInputEditText etBuscar = findViewById(R.id.etBuscar);
        tvContador = findViewById(R.id.tvContador);

        // Se crea con la lista "activos": cualquier cambio en la lista se refleja
        // llamando adapter.notifyDataSetChanged()
        adapter = new ActivoAdapter(this, activos);
        lvActivos.setAdapter(adapter);

        actualizarContador(); // texto inicial "Activos (0):"

        // Abrir el formulario para agregar un activo nuevo
        btnNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(this, agregar_activo.class);
            lanzadorFormulario.launch(intent);
        });

        // Filtro en vivo: ArrayAdapter (y por herencia, ActivoAdapter) ya trae un
        // Filter incorporado que compara contra el toString() de cada Activo.
        // Solo hace falta conectar el texto que se escribe con adapter.getFilter().
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override public void afterTextChanged(Editable s) {}
        });

        // Al tocar un elemento: abre la pantalla de detalle, no borra directo
        lvActivos.setOnItemClickListener((parent, view, position, id) -> {
            // IMPORTANTE: cuando hay un filtro activo, "position" es la posición dentro
            // de los resultados FILTRADOS, no el índice real en "activos". Por eso
            // pedimos el objeto ya filtrado (adapter.getItem) y buscamos su índice
            // verdadero en la lista completa (activos.indexOf) antes de abrir el detalle.
            Activo seleccionado = adapter.getItem(position);
            int indiceReal = activos.indexOf(seleccionado);

            Intent intent = new Intent(this, detalle_activo.class);
            intent.putExtra("EXTRA_ACTIVO", seleccionado);
            intent.putExtra("EXTRA_POSICION", indiceReal);
            lanzadorDetalle.launch(intent);
        });
    }

    // Actualiza el texto "Activos (N):" según cuántos elementos hay en la lista
    private void actualizarContador() {
        tvContador.setText("Activos (" + activos.size() + "):");
    }
}