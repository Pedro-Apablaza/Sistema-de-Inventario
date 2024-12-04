package com.ferreteria.inventario.models;

public class AjusteStock {
    private Long idAjuste;
    private String rutUsuario;
    private Long idProducto;
    private Integer cantidad;
    private String fecha;
    private String tipo;
    private String motivo;

    // Constructor
    public AjusteStock(Long idAjuste, String rutUsuario, Long idProducto, Integer cantidad, String fecha, String tipo, String motivo) {
        this.idAjuste = idAjuste;
        this.rutUsuario = rutUsuario;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.fecha = fecha;
        this.tipo = tipo;
        this.motivo = motivo;
    }

    // Getters y Setters
    public Long getIdAjuste() { return idAjuste; }
    public void setIdAjuste(Long idAjuste) { this.idAjuste = idAjuste; }

    public String getRutUsuario() { return rutUsuario; }
    public void setRutUsuario(String rutUsuario) { this.rutUsuario = rutUsuario; }

    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }

    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
}

