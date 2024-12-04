package com.ferreteria.inventario.models;


public class Registro {
    private int id;
    private String nombre;
    private int cantidad;
    private String usuario;
    private java.util.Date fecha;  // Cambiado a java.util.Date
    private String motivo;
    private String tipo;

    public Registro(int id, String nombre, int cantidad, String usuario, java.util.Date fecha, String motivo, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.usuario = usuario;
        this.fecha = fecha;
        this.motivo = motivo;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
    public String getUsuario() { return usuario; }
    public java.util.Date getFecha() { return fecha; }  // Retornar java.util.Date
    public String getMotivo() { return motivo; }
    public String getTipo() {return tipo;}
}

