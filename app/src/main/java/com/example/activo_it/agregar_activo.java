package com.example.activo_it;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;

// Este Activity sirve para DOS casos: crear un activo nuevo (esEdicion = false)
// y editar uno existente (esEdicion = true). Se decide según si llegó o no
// un Activo + posición en el Intent que abrió esta pantalla.
public class agregar_activo extends AppCompatActivity {

    // Guarda la dirección (Uri) de la foto elegida o tomada.
    // Se guarda como Uri en memoria, pero se pasa como String (foto.toString()) al Activo.
    private Uri fotoUri = null;
    private ImageView ivFoto;

    // Vista raíz del layout. La necesitamos como ancla para mostrar Snackbar,
    // ya que Snackbar.make() requiere un View visible en pantalla, no un Context.
    private View vistaRaiz;

    // Indica si estamos editando un activo existente o creando uno nuevo
    private boolean esEdicion = false;
    // Posición del activo dentro de la lista original (solo válida si esEdicion = true)
    private int posicionRecibida = -1;

    // Referencias a los TextInputLayout (el "contenedor" visual del campo).
    // setError() se llama sobre ESTOS, no sobre el TextInputEditText de adentro,
    // porque es el TextInputLayout quien dibuja el mensaje en rojo bajo el campo.
    private TextInputLayout tilEtiqueta, tilModelo, tilSerie;

    // Lanzador de galería: usamos OpenDocument() en vez de GetContent() porque
    // OpenDocument SÍ permite pedir un permiso de lectura PERSISTENTE sobre la
    // Uri elegida. Con GetContent(), el permiso era solo temporal y se perdía
    // al salir de esta pantalla, causando un crash (SecurityException) cuando
    // detalle_activo intentaba mostrar esa misma foto más tarde.
    private final ActivityResultLauncher<String[]> seleccionarFoto =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        // Pide que el permiso de lectura sobre esta Uri se mantenga
                        // más allá de esta pantalla (persiste incluso tras reiniciar la app)
                        getContentResolver().takePersistableUriPermission(
                                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        // Algunos proveedores de archivos no soportan permiso persistente;
                        // lo ignoramos y seguimos, la foto igual se muestra en esta pantalla
                    }
                    fotoUri = uri;              // guardamos la dirección de la imagen elegida
                    ivFoto.setImageURI(uri);    // la mostramos de inmediato en el ImageView
                }
            });

    // Lanzador de cámara: toma una foto nueva y la guarda en la Uri que ya preparamos (fotoUri)
    private final ActivityResultLauncher<Uri> tomarFoto =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), exito -> {
                if (exito) {
                    // La foto ya quedó guardada físicamente en fotoUri (se la pasamos al lanzar la cámara)
                    ivFoto.setImageURI(fotoUri);
                }
            });

    // Pide el permiso de cámara en tiempo de ejecución (obligatorio desde Android 6+)
    private final ActivityResultLauncher<String> pedirPermisoCamara =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    abrirCamara(); // si el usuario aceptó, ahora sí abrimos la cámara
                } else {
                    Snackbar.make(vistaRaiz, "Permiso de cámara denegado", Snackbar.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_activo);

        // Guardamos la vista raíz para poder usarla luego en los Snackbar
        vistaRaiz = findViewById(R.id.main);

        // Ajusta el padding para que el contenido no quede debajo de la barra de estado/navegación
        ViewCompat.setOnApplyWindowInsetsListener(vistaRaiz, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvTituloFormulario = findViewById(R.id.tvTituloFormulario);

        // Los TextInputLayout completos (para poder marcar errores)
        tilEtiqueta = findViewById(R.id.tilEtiqueta);
        tilModelo = findViewById(R.id.tilModelo);
        tilSerie = findViewById(R.id.tilSerie);

        // Los campos de texto de adentro (para leer/escribir el texto)
        TextInputEditText etEtiqueta = findViewById(R.id.etEtiqueta);
        TextInputEditText etModelo = findViewById(R.id.etModelo);
        TextInputEditText etSerie = findViewById(R.id.etSerie);
        MaterialSwitch switchEstado = findViewById(R.id.switchEstado);
        TextView tvEstadoLabel = findViewById(R.id.tvEstadoLabel);
        MaterialCheckBox cbTerminos = findViewById(R.id.cbTerminos);
        MaterialButton btnFoto = findViewById(R.id.btnFoto);
        MaterialButton btnCamara = findViewById(R.id.btnCamara);
        MaterialButton btnGuardar = findViewById(R.id.btnGuardar);
        MaterialButton btnCancelar = findViewById(R.id.btnCancelar);
        ivFoto = findViewById(R.id.ivFoto);

        // ¿Nos mandaron un Activo existente? Si sí, esto es una EDICIÓN, no una creación.
        Activo activoRecibido = (Activo) getIntent().getSerializableExtra("EXTRA_ACTIVO");
        posicionRecibida = getIntent().getIntExtra("EXTRA_POSICION", -1);
        esEdicion = (posicionRecibida != -1 && activoRecibido != null);

        if (esEdicion) {
            // Precarga el formulario con los datos actuales del activo, como pide el requisito de Update
            tvTituloFormulario.setText("Editar activo");
            btnGuardar.setText("Guardar cambios");

            etEtiqueta.setText(activoRecibido.getEtiqueta());
            etModelo.setText(activoRecibido.getModelo());
            etSerie.setText(activoRecibido.getSerie());

            boolean estaActivo = Activo.ESTADO_ACTIVO.equals(activoRecibido.getEstado());
            switchEstado.setChecked(estaActivo);
            tvEstadoLabel.setText("Estado: " + activoRecibido.getEstado());

            // Ya había aceptado los términos la primera vez que se creó, así que lo dejamos marcado
            cbTerminos.setChecked(true);

            // Si ya tenía foto guardada, la mostramos y guardamos su Uri para no perderla.
            // Protegido con try/catch: si esta Uri viene de una foto guardada ANTES
            // de este fix, es posible que el permiso ya se haya perdido y falle.
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

        // Conecta los 3 campos para que el error en rojo desaparezca apenas
        // el usuario empieza a escribir algo (sin esperar a que vuelva a dar Guardar)
        limpiarErrorAlEscribir(etEtiqueta, tilEtiqueta);
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
                // Todavía no tenemos permiso: lo pedimos, y si lo conceden, pedirPermisoCamara abre la cámara
                pedirPermisoCamara.launch(android.Manifest.permission.CAMERA);
            }
        });

        btnGuardar.setOnClickListener(v -> {
            String etiqueta = etEtiqueta.getText() != null ? etEtiqueta.getText().toString().trim() : "";
            String modelo = etModelo.getText() != null ? etModelo.getText().toString().trim() : "";
            String serie = etSerie.getText() != null ? etSerie.getText().toString().trim() : "";

            // validarCampos() marca CADA campo vacío con su propio mensaje de error
            // (requisito: "Errores con setError, no Toast"). Si algo falla, no seguimos.
            if (!validarCampos(etiqueta, modelo, serie)) {
                return;
            }

            // Los términos no son un campo de texto, así que aquí usamos Snackbar en vez de setError
            if (!cbTerminos.isChecked()) {
                Snackbar.make(vistaRaiz, "Debes aceptar los términos", Snackbar.LENGTH_SHORT).show();
                return;
            }

            String estado = switchEstado.isChecked() ? Activo.ESTADO_ACTIVO : Activo.ESTADO_BAJA;

            // Si no seleccionó/tomó foto, guardamos cadena vacía en vez de null
            // (evita un NullPointerException al hacer fotoUri.toString())
            String foto = fotoUri != null ? fotoUri.toString() : "";

            Activo activoFinal = new Activo(etiqueta, modelo, serie, estado, foto);

            // Empaqueta el Activo dentro del resultado, para que quien nos abrió
            // (MainActivity o detalle_activo) lo reciba en su callback correspondiente
            Intent resultado = new Intent();
            resultado.putExtra("EXTRA_ACTIVO", activoFinal);

            // Solo agregamos la posición si es edición; en modo Crear no aplica,
            // porque el elemento nuevo simplemente se agrega al final de la lista
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

    // Revisa los 3 campos obligatorios. Si alguno está vacío, le pone su propio
    // mensaje de error en rojo (setError) bajo ESE campo específico.
    // Si un campo ya es válido, le quitamos el error (por si venía de un intento anterior).
    // Devuelve true solo si los 3 campos pasaron la validación.
    private boolean validarCampos(String etiqueta, String modelo, String serie) {
        boolean esValido = true;

        if (etiqueta.isEmpty()) {
            tilEtiqueta.setError("La etiqueta es obligatoria");
            esValido = false;
        } else {
            tilEtiqueta.setError(null); // limpia el error si ya está bien
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
    // usuario escribe algo, revisa si ya no está vacío y, si es así, borra el
    // error en rojo de ese campo sin esperar a que vuelva a tocar "Guardar".
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

    // Prepara un archivo vacío y su Uri "segura" (vía FileProvider) para que la
    // cámara escriba ahí la foto, y lanza la app de cámara del sistema.
    private void abrirCamara() {
        try {
            // Nombre único por timestamp, para no sobreescribir fotos anteriores
            File archivo = new File(getExternalFilesDir("Pictures"), "foto_" + System.currentTimeMillis() + ".jpg");

            // FileProvider genera una Uri "content://" segura, en vez de exponer la ruta real del archivo
            fotoUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", archivo);

            tomarFoto.launch(fotoUri); // abre la app de cámara, que guardará la foto en fotoUri
        } catch (Exception e) {
            Snackbar.make(vistaRaiz, "Error al abrir la cámara", Snackbar.LENGTH_SHORT).show();
        }
    }
}