package com.ferreteria.inventario.models;

public class ProductoEntrada {
    private Long id;
    private String nombreProducto;
    private Integer cantidad;
    private String nombreUsuario;
    private String fecha;
    private String motivo;

    public ProductoEntrada(Long id, String nombreProducto, Integer cantidad, String nombreUsuario, String fecha, String motivo) {
        this.id = id;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.nombreUsuario = nombreUsuario;
        this.fecha = fecha;
        this.motivo = motivo;
    }

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}
