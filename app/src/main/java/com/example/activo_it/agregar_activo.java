package com.example.activo_it;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

// Formulario único para CREAR y EDITAR un activo. El modo se determina
// según si llegó un Activo existente por Intent (ver esEdicion).
public class agregar_activo extends AppCompatActivity {

    // Guarda la dirección (Uri) de la foto elegida o tomada.
    private Uri fotoUri = null;
    private ImageView ivFoto;
    private View vistaRaiz;

    // Indica si estamos editando un activo existente o creando uno nuevo
    private boolean esEdicion = false;
    // Posición del activo dentro de la lista original (solo válida si esEdicion = true)
    private int posicionRecibida = -1;
    // Activo original recibido (para conservar su id al editar)
    private Activo activoRecibido;

    private TextInputLayout tilEtiqueta, tilTipo, tilMarca, tilModelo, tilSerie;

    // Selector de imagen de la galería (respeta el sistema de permisos moderno)
    private final ActivityResultLauncher<String[]> seleccionarFoto =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        // Pide que el permiso de lectura sobre esta Uri se mantenga
                        // aunque la app se cierre y se vuelva a abrir más tarde.
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        // Algunos proveedores no soportan permisos persistentes; se ignora.
                    }
                    fotoUri = uri;
                    ivFoto.setImageURI(uri);
                }
            });

    // Toma una foto nueva y la guarda en la Uri preparada por abrirCamara()
    private final ActivityResultLauncher<Uri> tomarFoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), exito -> {
                if (exito) {
                    ivFoto.setImageURI(fotoUri);
                }
            });

    // Pide el permiso de cámara en tiempo de ejecución
    private final ActivityResultLauncher<String> pedirPermisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    abrirCamara();
                } else {
                    Snackbar.make(vistaRaiz, "Permiso de cámara denegado", Snackbar.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_activo);

        vistaRaiz = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(vistaRaiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvTituloFormulario = findViewById(R.id.tvTituloFormulario);

        // Los TextInputLayout completos (para poder marcar errores con setError)
        tilEtiqueta = findViewById(R.id.tilEtiqueta);
        tilTipo = findViewById(R.id.tilTipo);
        tilMarca = findViewById(R.id.tilMarca);
        tilModelo = findViewById(R.id.tilModelo);
        tilSerie = findViewById(R.id.tilSerie);

        TextInputEditText etEtiqueta = findViewById(R.id.etEtiqueta);
        TextInputEditText etTipo = findViewById(R.id.etTipo);
        TextInputEditText etMarca = findViewById(R.id.etMarca);
        TextInputEditText etModelo = findViewById(R.id.etModelo);
        TextInputEditText etSerie = findViewById(R.id.etSerie);
        MaterialSwitch switchEstado = findViewById(R.id.switchEstado);
        TextView tvEstadoLabel = findViewById(R.id.tvEstadoLabel);

        TextInputEditText etAsignado = findViewById(R.id.etAsignado);
        TextInputEditText etDepartamento = findViewById(R.id.etDepartamento);
        TextInputEditText etUbicacion = findViewById(R.id.etUbicacion);

        TextInputEditText etProcesador = findViewById(R.id.etProcesador);
        TextInputEditText etRam = findViewById(R.id.etRam);
        TextInputEditText etAlmacenamiento = findViewById(R.id.etAlmacenamiento);
        TextInputEditText etSistemaOperativo = findViewById(R.id.etSistemaOperativo);

        TextInputEditText etFechaCompra = findViewById(R.id.etFechaCompra);
        TextInputEditText etFechaGarantia = findViewById(R.id.etFechaGarantia);
        TextInputEditText etProveedor = findViewById(R.id.etProveedor);
        TextInputEditText etValor = findViewById(R.id.etValor);
        TextInputEditText etObservaciones = findViewById(R.id.etObservaciones);

        MaterialCheckBox cbTerminos = findViewById(R.id.cbTerminos);
        MaterialButton btnFoto = findViewById(R.id.btnFoto);
        MaterialButton btnCamara = findViewById(R.id.btnCamara);
        MaterialButton btnGuardar = findViewById(R.id.btnGuardar);
        MaterialButton btnCancelar = findViewById(R.id.btnCancelar);
        ivFoto = findViewById(R.id.ivFoto);

        // Al tocar el campo de fecha, se abre un calendario nativo (no se escribe a mano)
        configurarSelectorFecha(etFechaCompra);
        configurarSelectorFecha(etFechaGarantia);

        // Ícono de cámara dentro de Etiqueta/Serie: abre el escáner de QR/código de barras
        tilEtiqueta.setEndIconOnClickListener(v -> escanearCodigo(etEtiqueta));
        tilSerie.setEndIconOnClickListener(v -> escanearCodigo(etSerie));

        // ¿Nos mandaron un Activo existente? Si sí, esto es una EDICIÓN, no una creación.
        activoRecibido = (Activo) getIntent().getSerializableExtra("EXTRA_ACTIVO");
        posicionRecibida = getIntent().getIntExtra("EXTRA_POSICION", -1);
        esEdicion = (posicionRecibida != -1 && activoRecibido != null);

        if (esEdicion) {
            // Precarga el formulario con los datos actuales del activo (requisito de Update)
            tvTituloFormulario.setText("Editar activo");
            btnGuardar.setText("Guardar cambios");

            etEtiqueta.setText(activoRecibido.getEtiqueta());
            etTipo.setText(activoRecibido.getTipo());
            etMarca.setText(activoRecibido.getMarca());
            etModelo.setText(activoRecibido.getModelo());
            etSerie.setText(activoRecibido.getSerie());

            boolean estaActivo = Activo.ESTADO_ACTIVO.equals(activoRecibido.getEstado());
            switchEstado.setChecked(estaActivo);
            tvEstadoLabel.setText("Estado: " + activoRecibido.getEstado());

            etAsignado.setText(activoRecibido.getAsignado());
            etDepartamento.setText(activoRecibido.getDepartamento());
            etUbicacion.setText(activoRecibido.getUbicacion());

            etProcesador.setText(activoRecibido.getProcesador());
            etRam.setText(activoRecibido.getRam());
            etAlmacenamiento.setText(activoRecibido.getAlmacenamiento());
            etSistemaOperativo.setText(activoRecibido.getSistemaOperativo());

            etFechaCompra.setText(activoRecibido.getFechaCompra());
            etFechaGarantia.setText(activoRecibido.getFechaVencimientoGarantia());
            etProveedor.setText(activoRecibido.getProveedor());
            if (activoRecibido.getValor() != 0) {
                etValor.setText(String.valueOf(activoRecibido.getValor()));
            }
            etObservaciones.setText(activoRecibido.getObservaciones());

            // Ya había aceptado los términos la primera vez que se creó, así que lo dejamos marcado
            cbTerminos.setChecked(true);

            // Si ya tenía foto guardada, la mostramos y guardamos su Uri para no perderla.
            String fotoExistente = activoRecibido.getFoto();
            if (fotoExistente != null && !fotoExistente.isEmpty()) {
                try {
                    fotoUri = Uri.parse(fotoExistente);
                    ivFoto.setImageURI(fotoUri);
                } catch (SecurityException e) {
                    ivFoto.setImageResource(android.R.drawable.ic_menu_camera);
                }
            }
        }

        // Cada vez que se mueve el switch, actualiza el texto de arriba (Activo/Baja)
        switchEstado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String estado = isChecked ? Activo.ESTADO_ACTIVO : Activo.ESTADO_BAJA;
            tvEstadoLabel.setText("Estado: " + estado);
        });

        // Conecta los campos obligatorios para que el error en rojo desaparezca
        // apenas el usuario empieza a escribir algo
        limpiarErrorAlEscribir(etEtiqueta, tilEtiqueta);
        limpiarErrorAlEscribir(etTipo, tilTipo);
        limpiarErrorAlEscribir(etMarca, tilMarca);
        limpiarErrorAlEscribir(etModelo, tilModelo);
        limpiarErrorAlEscribir(etSerie, tilSerie);

        // Botón Galería: abre el selector de documentos del sistema, filtrado a imágenes
        btnFoto.setOnClickListener(v -> seleccionarFoto.launch(new String[]{"image/*"}));

        // Botón Cámara: revisa permiso antes de abrir la cámara
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
            String etiqueta = obtenerTexto(etEtiqueta);
            String tipo = obtenerTexto(etTipo);
            String marca = obtenerTexto(etMarca);
            String modelo = obtenerTexto(etModelo);
            String serie = obtenerTexto(etSerie);

            // validarCampos() marca CADA campo vacío con su propio mensaje de error.
            // Si algo falla, no seguimos.
            if (!validarCampos(etiqueta, tipo, marca, modelo, serie)) {
                return;
            }

            // Los términos no son un campo de texto, así que aquí usamos Snackbar en vez de setError
            if (!cbTerminos.isChecked()) {
                Snackbar.make(vistaRaiz, "Debes aceptar los términos", Snackbar.LENGTH_SHORT).show();
                return;
            }

            String estado = switchEstado.isChecked() ? Activo.ESTADO_ACTIVO : Activo.ESTADO_BAJA;

            // Si no seleccionó/tomó foto, guardamos cadena vacía en vez de null
            String foto = fotoUri != null ? fotoUri.toString() : "";

            String valorStr = obtenerTexto(etValor);
            double valor = 0;
            if (!TextUtils.isEmpty(valorStr)) {
                try {
                    valor = Double.parseDouble(valorStr);
                } catch (NumberFormatException e) {
                    etValor.setError("Valor inválido");
                    return;
                }
            }

            Activo activoFinal = new Activo(
                    etiqueta, tipo, marca, modelo, serie, estado,
                    obtenerTexto(etAsignado), obtenerTexto(etDepartamento), obtenerTexto(etUbicacion),
                    obtenerTexto(etProcesador), obtenerTexto(etRam), obtenerTexto(etAlmacenamiento), obtenerTexto(etSistemaOperativo),
                    obtenerTexto(etFechaCompra), obtenerTexto(etFechaGarantia), obtenerTexto(etProveedor),
                    valor, obtenerTexto(etObservaciones), foto
            );

            // Si es edición, conserva el id original para que el UPDATE en SQLite funcione
            if (esEdicion) {
                activoFinal.setId(activoRecibido.getId());
            }

            // Empaqueta el Activo dentro del resultado, para que quien nos abrió
            // (MainActivity o detalle_activo) lo reciba en su callback correspondiente
            Intent resultado = new Intent();
            resultado.putExtra("EXTRA_ACTIVO", activoFinal);

            // Solo agregamos la posición si es edición; en modo Crear no aplica
            if (esEdicion) {
                resultado.putExtra("EXTRA_POSICION", posicionRecibida);
            }

            setResult(Activity.RESULT_OK, resultado);
            finish();
        });

        // Cancelar: le avisa a quien nos abrió que no hay nada que hacer
        btnCancelar.setOnClickListener(v -> {
            setResult(Activity.RESULT_CANCELED);
            finish();
        });
    }

    // Lee un TextInputEditText de forma segura (evita NullPointerException)
    private String obtenerTexto(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    // Revisa los 5 campos obligatorios. Si alguno está vacío, le pone su propio
    // mensaje de error en rojo (setError) bajo ESE campo específico.
    // Devuelve true solo si todos pasaron la validación.
    private boolean validarCampos(String etiqueta, String tipo, String marca, String modelo, String serie) {
        boolean esValido = true;

        if (etiqueta.isEmpty()) {
            tilEtiqueta.setError("La etiqueta es obligatoria");
            esValido = false;
        } else {
            tilEtiqueta.setError(null);
        }

        if (tipo.isEmpty()) {
            tilTipo.setError("El tipo es obligatorio");
            esValido = false;
        } else {
            tilTipo.setError(null);
        }

        if (marca.isEmpty()) {
            tilMarca.setError("La marca es obligatoria");
            esValido = false;
        } else {
            tilMarca.setError(null);
        }

        if (modelo.isEmpty()) {
            tilModelo.setError("El modelo es obligatorio");
            esValido = false;
        } else {
            tilModelo.setError(null);
        }

        if (serie.isEmpty()) {
            tilSerie.setError("La serie es obligatoria");
            esValido = false;
        } else {
            tilSerie.setError(null);
        }

        return esValido;
    }

    // Agrega un "vigilante" de texto (TextWatcher) a un campo: cada vez que el
    // usuario escribe algo, revisa si ya no está vacío y borra el error en rojo
    // sin esperar a que vuelva a tocar "Guardar".
    private void limpiarErrorAlEscribir(TextInputEditText editText, TextInputLayout layout) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() > 0) {
                    layout.setError(null);
                }
            }
        });
    }

    // Abre un calendario nativo al tocar el campo; el usuario no escribe la fecha a mano
    private void configurarSelectorFecha(TextInputEditText editText) {
        editText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int anio = calendar.get(Calendar.YEAR);
            int mes = calendar.get(Calendar.MONTH);
            int dia = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this, (view, y, m, d) -> {
                String fecha = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
                editText.setText(fecha);
            }, anio, mes, dia);
            datePicker.show();
        });
    }

    // Abre el escáner de Google (QR y códigos de barra); el resultado se escribe en "destino"
    private void escanearCodigo(TextInputEditText destino) {
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();

        GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(this, options);

        scanner.startScan()
                .addOnSuccessListener(barcode -> {
                    String valor = barcode.getRawValue();
                    if (valor != null && !valor.isEmpty()) {
                        destino.setText(valor);
                    }
                })
                .addOnFailureListener(e ->
                        Snackbar.make(vistaRaiz, "No se pudo escanear: " + e.getMessage(), Snackbar.LENGTH_SHORT).show());
    }

    // Prepara un archivo vacío y su Uri "segura" (vía FileProvider) para que la
    // cámara escriba ahí la foto, y lanza la app de cámara del sistema.
    private void abrirCamara() {
        try {
            // Nombre único por timestamp, para no sobreescribir fotos anteriores
            File archivo = new File(getExternalFilesDir("Pictures"), "foto_" + System.currentTimeMillis() + ".jpg");

            // FileProvider genera una Uri "content://" segura, en vez de exponer la ruta real del archivo
            fotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivo);

            tomarFoto.launch(fotoUri);
        } catch (Exception e) {
            Snackbar.make(vistaRaiz, "Error al abrir la cámara", Snackbar.LENGTH_SHORT).show();
        }
    }
}