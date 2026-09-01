package com.example.activo_it;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

// Pantalla principal: lista de activos con RecyclerView, buscador y botón "Nuevo activo".
// Implementa la CREATE (vía lanzadorFormulario) y delega UPDATE/DELETE a detalle_activo.
public class MainActivity extends AppCompatActivity implements ActivoAdapter.OnActivoClickListener {

    // Lista en memoria: se carga desde SQLite al abrir la app y se mantiene
    // sincronizada con la base de datos en cada operación
    private final ArrayList<Activo> activos = new ArrayList<>();

    private ActivoAdapter adapter;
    private ActivoDbHelper dbHelper;

    private TextView tvContador;

    // Lanzador para CREAR: abre agregar_activo vacío, guarda en SQLite y espera un Activo nuevo de vuelta
    private final ActivityResultLauncher<Intent> lanzadorFormulario =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    Activo nuevo = (Activo) resultado.getData().getSerializableExtra("EXTRA_ACTIVO");
                    if (nuevo != null) {
                        long id = dbHelper.insertActivo(nuevo);
                        nuevo.setId(id);
                        activos.add(0, nuevo); // arriba, coincide con el orden de la BD (más reciente primero)
                        adapter.actualizarLista(activos);
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
                            dbHelper.actualizarActivo(actualizado);
                            activos.set(posicion, actualizado);
                        }
                    } else if ("ELIMINAR".equals(accion)) {
                        Activo eliminado = activos.get(posicion);
                        dbHelper.eliminarActivo(eliminado.getId());
                        activos.remove(posicion);
                    }

                    adapter.actualizarLista(activos);
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

        MaterialToolbar toolbar = findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbar);

        MaterialButton btnNuevo = findViewById(R.id.btnNuevo);
        RecyclerView rvActivos = findViewById(R.id.rvActivos);
        TextInputEditText etBuscar = findViewById(R.id.etBuscar);
        tvContador = findViewById(R.id.tvContador);

        // READ: carga todo lo guardado en SQLite de sesiones anteriores
        dbHelper = new ActivoDbHelper(this);
        activos.addAll(dbHelper.obtenerTodos());

        adapter = new ActivoAdapter(activos, this);
        rvActivos.setLayoutManager(new LinearLayoutManager(this));
        rvActivos.setAdapter(adapter);

        actualizarContador(); // texto inicial "Activos (N):"

        // Abrir el formulario para agregar un activo nuevo
        btnNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(this, agregar_activo.class);
            lanzadorFormulario.launch(intent);
        });

        // Filtro en vivo: cada tecla escrita se reenvía al adapter
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // Se llama cuando se toca cualquier fila del RecyclerView (ver ActivoAdapter).
    // Busca el índice real dentro de "activos" (no el filtrado) antes de abrir el detalle,
    // para que UPDATE/DELETE apunten a la posición correcta.
    @Override
    public void onActivoClick(Activo activo) {
        int indiceReal = activos.indexOf(activo);

        Intent intent = new Intent(this, detalle_activo.class);
        intent.putExtra("EXTRA_ACTIVO", activo);
        intent.putExtra("EXTRA_POSICION", indiceReal);
        lanzadorDetalle.launch(intent);
    }

    // Actualiza el texto "Activos (N):" según cuántos elementos hay en la lista
    private void actualizarContador() {
        tvContador.setText("Activos (" + activos.size() + "):");
    }
}