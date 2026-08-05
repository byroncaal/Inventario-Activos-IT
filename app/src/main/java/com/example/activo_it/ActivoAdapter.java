package com.example.activo_it;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

// Adapter personalizado para dibujar cada fila del ListView como una
// MaterialCardView (con foto, texto y color de estado), en vez del layout
// de texto plano que usa ArrayAdapter por defecto.
//
// Extiende ArrayAdapter (no BaseAdapter) a propósito: así conservamos gratis
// el método getFilter(), que es lo que usa el buscador para filtrar en vivo.
public class ActivoAdapter extends ArrayAdapter<Activo> {

    public ActivoAdapter(@NonNull Context context, @NonNull List<Activo> activos) {
        // El "0" es el id de layout que ArrayAdapter usaría por defecto;
        // no importa acá porque getView() está completamente sobreescrito abajo,
        // así que ese layout por defecto nunca se llega a usar.
        super(context, 0, activos);
    }

    // Se llama automáticamente por el ListView, una vez por cada fila visible.
    // "convertView" es una fila reciclada (para no crear vistas de más al hacer scroll);
    // si viene null, es la primera vez que se dibuja esa posición y hay que inflarla.
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View fila = convertView;
        if (fila == null) {
            // "false" en inflate: no adjuntar la vista al parent todavía,
            // el ListView se encarga de eso internamente
            fila = LayoutInflater.from(getContext()).inflate(R.layout.item_activo, parent, false);
        }

        // El activo que le toca mostrar a ESTA fila específica
        Activo activo = getItem(position);
        if (activo == null) return fila; // seguridad: nunca debería pasar, pero evita un crash

        ImageView ivFoto = fila.findViewById(R.id.ivFotoItem);
        TextView tvEtiqueta = fila.findViewById(R.id.tvEtiquetaItem);
        TextView tvModeloSerie = fila.findViewById(R.id.tvModeloSerieItem);
        TextView tvEstado = fila.findViewById(R.id.tvEstadoItem);

        // Rellena los textos con los datos del activo
        tvEtiqueta.setText(activo.getEtiqueta());
        tvModeloSerie.setText(activo.getModelo() + " · " + activo.getSerie());
        tvEstado.setText(activo.getEstado());

        // Color del texto de estado: verde si está Activo, rojo si está de Baja.
        // Esto le da una señal visual rápida sin tener que leer el texto completo.
        boolean estaActivo = Activo.ESTADO_ACTIVO.equals(activo.getEstado());
        tvEstado.setTextColor(estaActivo ? Color.parseColor("#1B873F") : Color.parseColor("#D32F2F"));

        // Solo intenta cargar la foto si el activo tiene una Uri guardada.
        // El try/catch evita un crash si la Uri quedó "vieja" o inválida
        // (por ejemplo, si la foto se borró del dispositivo después de guardarla).
        String foto = activo.getFoto();
        if (foto != null && !foto.isEmpty()) {
            try {
                ivFoto.setImageURI(Uri.parse(foto));
            } catch (Exception e) {
                ivFoto.setImageResource(android.R.drawable.ic_menu_camera); // ícono de respaldo
            }
        } else {
            ivFoto.setImageResource(android.R.drawable.ic_menu_camera);
        }

        return fila;
    }
}