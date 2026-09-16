package com.example.activo_it;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements ActivoAdapter.OnActivoClickListener {

    // TEMPORAL: username de Icecat para las pruebas.
    // Reemplázalo por tu propio username (lo ves en icecat.biz/en/myIcecat).
    // Mientras tanto, "openIcecat-live" es la cuenta demo pública y sirve
    // para confirmar que la llamada funciona.
    private static final String ICECAT_SHOPNAME = "openIcecat-live";

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

        //probarIcecat(); // TEMPORAL: solo para ver la estructura real del JSON, luego se quita
    }

    // TEMPORAL: llamada de prueba a Icecat con un producto real conocido (monitor iiyama),
    // solo para confirmar el formato exacto de la respuesta antes de integrarlo al formulario.
    /*
    private void probarIcecat() {
        IcecatApiService apiService = IcecatRetrofitClient.getInstance().create(IcecatApiService.class);
        Call<JsonObject> call = apiService.getProductByGtin(
                BuildConfig.ICECAT_API_TOKEN,
                BuildConfig.ICECAT_CONTENT_TOKEN,
                ICECAT_SHOPNAME,
                "4948570114344",
                "en",
                ""
        );

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().toString();
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Respuesta cruda de Icecat")
                            .setMessage(json.length() > 3000 ? json.substring(0, 3000) + "\n\n...(cortado)" : json)
                            .setPositiveButton("Cerrar", null)
                            .show();
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorBody = "(no se pudo leer el cuerpo del error)";
                    }
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Error " + response.code())
                            .setMessage(errorBody.isEmpty() ? "Sin detalle adicional" : errorBody)
                            .setPositiveButton("Cerrar", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable throwable) {
                Toast.makeText(MainActivity.this, "Sin conexión: " + throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
*/
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