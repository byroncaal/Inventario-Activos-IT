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


public class ActivoAdapter extends ArrayAdapter<Activo> {

    public ActivoAdapter(@NonNull Context context, @NonNull List<Activo> activos) {

        super(context, 0, activos);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View fila = convertView;
        if (fila == null) {

            fila = LayoutInflater.from(getContext()).inflate(R.layout.item_activo, parent, false);
        }

        // El activo que le toca mostrar a ESTA fila específica
        Activo activo = getItem(position);
        if (activo == null) return fila;

        ImageView ivFoto = fila.findViewById(R.id.ivFotoItem);
        TextView tvEtiqueta = fila.findViewById(R.id.tvEtiquetaItem);
        TextView tvModeloSerie = fila.findViewById(R.id.tvModeloSerieItem);
        TextView tvEstado = fila.findViewById(R.id.tvEstadoItem);

        // Rellena los textos con los datos del activo
        tvEtiqueta.setText(activo.getEtiqueta());
        tvModeloSerie.setText(activo.getModelo() + " · " + activo.getSerie());
        tvEstado.setText(activo.getEstado());


        boolean estaActivo = Activo.ESTADO_ACTIVO.equals(activo.getEstado());
        tvEstado.setTextColor(estaActivo ? Color.parseColor("#1B873F") : Color.parseColor("#D32F2F"));

        // Solo intenta cargar la foto si el activo tiene una Uri guardada..
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