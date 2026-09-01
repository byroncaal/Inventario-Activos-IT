package com.example.activo_it;

import java.io.Serializable;

// Serializable permite que este objeto viaje empaquetado dentro de un Intent
// (necesario para enviarlo entre MainActivity, agregar_activo y detalle_activo).
public class Activo implements Serializable {

    // Constantes de estado: evitan escribir el texto "a mano" y equivocarse
    public static final String ESTADO_ACTIVO = "Activo";
    public static final String ESTADO_BAJA = "Baja";

    // Id de la fila en SQLite. -1 significa "todavía no guardado en la base de datos".
    private long id = -1;

    private String etiqueta;
    private String tipo;
    private String marca;
    private String modelo;
    private String serie;
    private String estado; // guarda "Activo" o "Baja"
    private String asignado;
    private String departamento;
    private String ubicacion;
    private String procesador;
    private String ram;
    private String almacenamiento;
    private String sistemaOperativo;
    private String fechaCompra;
    private String fechaVencimientoGarantia;
    private String proveedor;
    private double valor;
    private String observaciones;
    private String foto; // guarda la Uri de la foto como texto, no el Bitmap

    public Activo() {
    }

    public Activo(String etiqueta, String tipo, String marca, String modelo, String serie,
                  String estado, String asignado, String departamento, String ubicacion,
                  String procesador, String ram, String almacenamiento, String sistemaOperativo,
                  String fechaCompra, String fechaVencimientoGarantia, String proveedor,
                  double valor, String observaciones, String foto) {
        this.etiqueta = etiqueta;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.serie = serie;
        this.estado = estado;
        this.asignado = asignado;
        this.departamento = departamento;
        this.ubicacion = ubicacion;
        this.procesador = procesador;
        this.ram = ram;
        this.almacenamiento = almacenamiento;
        this.sistemaOperativo = sistemaOperativo;
        this.fechaCompra = fechaCompra;
        this.fechaVencimientoGarantia = fechaVencimientoGarantia;
        this.proveedor = proveedor;
        this.valor = valor;
        this.observaciones = observaciones;
        this.foto = foto;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getSerie() { return serie; }
    public void setSerie(String serie) { this.serie = serie; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getAsignado() { return asignado; }
    public void setAsignado(String asignado) { this.asignado = asignado; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getProcesador() { return procesador; }
    public void setProcesador(String procesador) { this.procesador = procesador; }

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }

    public String getAlmacenamiento() { return almacenamiento; }
    public void setAlmacenamiento(String almacenamiento) { this.almacenamiento = almacenamiento; }

    public String getSistemaOperativo() { return sistemaOperativo; }
    public void setSistemaOperativo(String sistemaOperativo) { this.sistemaOperativo = sistemaOperativo; }

    public String getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(String fechaCompra) { this.fechaCompra = fechaCompra; }

    public String getFechaVencimientoGarantia() { return fechaVencimientoGarantia; }
    public void setFechaVencimientoGarantia(String fechaVencimientoGarantia) { this.fechaVencimientoGarantia = fechaVencimientoGarantia; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    // Usado por el buscador (ActivoAdapter.filtrar) para comparar contra el texto escrito
    @Override
    public String toString() {
        return etiqueta + " - " + marca + " " + modelo + " - " + serie + " (" + estado + ")";
    }
}