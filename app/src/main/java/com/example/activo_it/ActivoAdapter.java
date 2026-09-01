package com.example.activo_it;

import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// Adapter del RecyclerView que muestra la lista de activos en MainActivity.
public class ActivoAdapter extends RecyclerView.Adapter<ActivoAdapter.ActivoViewHolder> {

    // Se notifica a MainActivity cuál Activo fue tocado, en vez de un
    // listener genérico de posición (evita problemas al filtrar la lista).
    public interface OnActivoClickListener {
        void onActivoClick(Activo activo);
    }

    // Lista completa (fuente de verdad, la misma referencia que tiene MainActivity)
    // vs. lista visible (puede estar filtrada por el buscador)
    private final List<Activo> activosCompletos;
    private List<Activo> activosFiltrados;
    private final OnActivoClickListener listener;

    public ActivoAdapter(List<Activo> activos, OnActivoClickListener listener) {
        this.activosCompletos = activos;
        this.activosFiltrados = new ArrayList<>(activos);
        this.listener = listener;
    }

    // Llamar después de agregar/editar/eliminar en la lista completa, para refrescar la vista.
    // activosCompletos YA es la misma referencia que la lista de MainActivity,
    // por eso aquí solo se reconstruye la vista filtrada, sin copiar datos.
    public void actualizarLista(List<Activo> nuevaLista) {
        activosFiltrados = new ArrayList<>(activosCompletos);
        notifyDataSetChanged();
    }

    // Filtro manual por texto (RecyclerView no trae Filter incorporado como ArrayAdapter)
    public void filtrar(String texto) {
        activosFiltrados = new ArrayList<>();
        if (texto == null || texto.trim().isEmpty()) {
            activosFiltrados.addAll(activosCompletos);
        } else {
            String busqueda = texto.toLowerCase().trim();
            for (Activo activo : activosCompletos) {
                if (activo.toString().toLowerCase().contains(busqueda)) {
                    activosFiltrados.add(activo);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ActivoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activo, parent, false);
        return new ActivoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivoViewHolder holder, int position) {
        Activo activo = activosFiltrados.get(position);

        holder.tvEtiqueta.setText(activo.getEtiqueta());
        holder.tvModeloSerie.setText(activo.getMarca() + " " + activo.getModelo() + " · " + activo.getSerie());
        holder.tvEstado.setText(activo.getEstado());

        // Verde para "Activo", rojo para "Baja"
        boolean estaActivo = Activo.ESTADO_ACTIVO.equals(activo.getEstado());
        holder.tvEstado.setTextColor(estaActivo ? Color.parseColor("#1B873F") : Color.parseColor("#D32F2F"));

        // Solo intenta cargar la foto si el activo tiene una Uri guardada
        String foto = activo.getFoto();
        if (foto != null && !foto.isEmpty()) {
            try {
                holder.ivFoto.setImageURI(Uri.parse(foto));
            } catch (Exception e) {
                holder.ivFoto.setImageResource(android.R.drawable.ic_menu_camera); // ícono de respaldo
            }
        } else {
            holder.ivFoto.setImageResource(android.R.drawable.ic_menu_camera);
        }

        holder.itemView.setOnClickListener(v -> listener.onActivoClick(activo));
    }

    @Override
    public int getItemCount() {
        return activosFiltrados.size();
    }

    // ViewHolder: guarda las referencias a las vistas de cada fila,
    // evitando llamar findViewById repetidamente al reciclar vistas.
    static class ActivoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoto;
        TextView tvEtiqueta, tvModeloSerie, tvEstado;

        ActivoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFotoItem);
            tvEtiqueta = itemView.findViewById(R.id.tvEtiquetaItem);
            tvModeloSerie = itemView.findViewById(R.id.tvModeloSerieItem);
            tvEstado = itemView.findViewById(R.id.tvEstadoItem);
        }
    }
}