package com.ferreteria.inventario.models;

public class Proveedor {
    private String rutProveedor;
    private String nombre;
    private String direccion;
    private String correo;
    private boolean softDelete;

    public Proveedor(String rutProveedor, String nombre, String direccion, String correo, boolean softDelete) {
        this.rutProveedor = rutProveedor;
        this.nombre = nombre;
        this.direccion = direccion;
        this.correo = correo;
        this.softDelete = softDelete;
    }

    // Getter y setter para rutProveedor
    public String getRutProveedor() {
        return rutProveedor;
    }

    public void setRutProveedor(String rutProveedor) {
        this.rutProveedor = rutProveedor;
    }

    // Getter y setter para otros atributos
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isSoftDelete() {
        return softDelete;
    }

    public void setSoftDelete(boolean softDelete) {
        this.softDelete = softDelete;
    }
}
