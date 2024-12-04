package com.ferreteria.inventario.utils;

public class SessionManager {
    private static SessionManager instance;

    private String rutUsuario;
    private String nombre;
    private String apellido;
    private String nomUsuario;
    private String correo;
    private int idRol;

    // Constructor privado para Singleton
    private SessionManager() {}

    // Método para obtener la instancia única
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    // Métodos getters y setters
    public String getRutUsuario() {
        return rutUsuario;
    }

    public void setRutUsuario(String rutUsuario) {
        this.rutUsuario = rutUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNomUsuario() {
        return nomUsuario;
    }

    public void setNomUsuario(String nomUsuario) {
        this.nomUsuario = nomUsuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    // Método para limpiar la sesión
    public void clear() {
        rutUsuario = null;
        nombre = null;
        apellido = null;
        nomUsuario = null;
        correo = null;
        idRol = 0;
    }
}
