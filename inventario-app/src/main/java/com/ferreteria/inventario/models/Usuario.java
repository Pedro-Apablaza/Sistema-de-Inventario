package com.ferreteria.inventario.models;

public class Usuario {
    private String rutUsuario;
    private String nombre;
    private String apellido;
    private String nomUsuario;
    private String contraseña;
    private String correo;
    private int idRol;
    private boolean softDelete;

    // Constructor, getters y setters
    public Usuario(String rutUsuario, String nombre, String apellido, String nomUsuario, String contraseña, String correo, int idRol, boolean softDelete) {
        this.rutUsuario = rutUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.nomUsuario = nomUsuario;  
        this.contraseña = contraseña;
        this.correo = correo;
        this.idRol = idRol;
        this.softDelete = softDelete;
    }

    public String getRutUsuario() {
        return rutUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNomUsuario() {
        return nomUsuario;  
    }

    public String getContraseña() {
        return contraseña;
    }

    public String getCorreo() {
        return correo;
    }

    public int getIdRol() {
        return idRol;
    }

    public boolean isSoftDelete() {
        return softDelete;
    }
}

