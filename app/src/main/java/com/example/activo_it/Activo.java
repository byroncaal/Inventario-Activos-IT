package com.example.activo_it;

import java.io.Serializable;

// Serializable permite que este objeto viaje empaquetado dentro de un Intent
// (necesario para enviarlo de vuelta desde agregar_activo hacia MainActivity)
public class Activo implements Serializable {

    // Constantes de estado: evitan escribir el texto "a mano" y equivocarse
    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_BAJA = "Baja";

    private String etiqueta;
    private String modelo;
    private String serie;
    private String estado; // guarda "Activo" o "Baja"
    private String foto;   // guarda la Uri de la foto como texto (no el Bitmap)

    public Activo(String etiqueta, String modelo, String serie, String estado, String foto) {
        this.etiqueta = etiqueta;
        this.modelo = modelo;
        this.serie = serie;
        this.estado = estado;
        this.foto = foto;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    // Se guarda como String (la Uri en formato texto), no como Bitmap:
    // guardar el Bitmap completo sería pesado y no sobrevive bien un Intent.
    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    // El ListView usa este método automáticamente para saber qué texto
    // mostrar en cada fila, ya que el ArrayAdapter es de tipo Activo
    @Override
    public String toString() {
        return etiqueta + " - " + modelo + " - " + serie + " (" + estado + ")";
    }
}