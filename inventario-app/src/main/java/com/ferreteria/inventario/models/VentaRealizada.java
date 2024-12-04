package com.ferreteria.inventario.models;

public class VentaRealizada {
    private long idVenta;
    private String fecha;
    private double totalVenta;
    private String cliente;
    private String rutCliente;
    private String vendedor;

    public VentaRealizada(long idVenta, String fecha, double totalVenta, String cliente, String rutCliente, String vendedor) {
        this.idVenta = idVenta;
        this.fecha = fecha;
        this.totalVenta = totalVenta;
        this.cliente = cliente;
        this.rutCliente = rutCliente;
        this.vendedor = vendedor;
    }

    // Getters y setters
    public long getIdVenta() {
        return idVenta;
    }

    public void setIdVenta(long idVenta) {
        this.idVenta = idVenta;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public double getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(double totalVenta) {
        this.totalVenta = totalVenta;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getRutCliente() {
        return rutCliente;
    }

    public void setRutCliente(String rutCliente) {
        this.rutCliente = rutCliente;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }
}
