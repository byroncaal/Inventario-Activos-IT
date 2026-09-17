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

public class MainActivity extends AppCompatActivity implements ActivoAdapter.OnActivoClickListener {

    private final ArrayList<Activo> activos = new ArrayList<>();

    private ActivoAdapter adapter;
    private ActivoDao activoDao;

    private TextView tvContador;

    private final ActivityResultLauncher<Intent> lanzadorFormulario =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    Activo nuevo = (Activo) resultado.getData().getSerializableExtra("EXTRA_ACTIVO");
                    if (nuevo != null) {
                        long id = activoDao.insertar(nuevo);
                        nuevo.setId(id);
                        activos.add(0, nuevo);
                        adapter.actualizarLista(activos);
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
                            activoDao.actualizar(actualizado);
                            activos.set(posicion, actualizado);
                        }
                    } else if ("ELIMINAR".equals(accion)) {
                        Activo eliminado = activos.get(posicion);
                        activoDao.eliminar(eliminado.getId());
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

        activoDao = new ActivoDao(this);
        activos.addAll(activoDao.obtenerTodos());

        adapter = new ActivoAdapter(activos, this);
        rvActivos.setLayoutManager(new LinearLayoutManager(this));
        rvActivos.setAdapter(adapter);

        actualizarContador();

        btnNuevo.setOnClickListener(v -> {
            Intent intent = new Intent(this, agregar_activo.class);
            lanzadorFormulario.launch(intent);
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.filtrar(s.toString());
            }

            @Override public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onActivoClick(Activo activo) {
        int indiceReal = activos.indexOf(activo);

        Intent intent = new Intent(this, detalle_activo.class);
        intent.putExtra("EXTRA_ACTIVO", activo);
        intent.putExtra("EXTRA_POSICION", indiceReal);
        lanzadorDetalle.launch(intent);
    }

    private void actualizarContador() {
        tvContador.setText("Activos (" + activos.size() + "):");
    }
}