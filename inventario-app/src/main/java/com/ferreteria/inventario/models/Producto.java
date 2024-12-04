package com.ferreteria.inventario.models;

import javafx.beans.property.*;

public class Producto {

    private LongProperty idProducto;
    private StringProperty nomProducto;
    private IntegerProperty precio;
    private IntegerProperty cantidad;
    private IntegerProperty stockCritico;
    private StringProperty nomCategoria;

    // Constructor
    public Producto(Long idProducto, String nomProducto, int precio, int cantidad, int stockCritico, String nomCategoria) {
        this.idProducto = new SimpleLongProperty(idProducto);
        this.nomProducto = new SimpleStringProperty(nomProducto);
        this.precio = new SimpleIntegerProperty(precio);
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.stockCritico = new SimpleIntegerProperty(stockCritico);
        this.nomCategoria = new SimpleStringProperty(nomCategoria);
    }

    // Getters y Setters
    public Long getIdProducto() {
        return idProducto.get();
    }

    public void setIdProducto(Long idProducto) {
        this.idProducto.set(idProducto);
    }

    public String getNomProducto() {
        return nomProducto.get();
    }

    public void setNomProducto(String nomProducto) {
        this.nomProducto.set(nomProducto);
    }

    public int getPrecio() {
        return precio.get();
    }

    public void setPrecio(int precio) {
        this.precio.set(precio);
    }

    public int getCantidad() {
        return cantidad.get();
    }

    public void setCantidad(int cantidad) {
        this.cantidad.set(cantidad);
    }

    public int getStockCritico() {
        return stockCritico.get();
    }

    public void setStockCritico(int stockCritico) {
        this.stockCritico.set(stockCritico);
    }

    public String getNomCategoria() {
        return nomCategoria.get();
    }

    public void setNomCategoria(String nomCategoria) {
        this.nomCategoria.set(nomCategoria);
    }

    // Propiedades de JavaFX para vinculación
    public LongProperty idProductoProperty() {
        return idProducto;
    }

    public StringProperty nomProductoProperty() {
        return nomProducto;
    }

    public IntegerProperty precioProperty() {
        return precio;
    }

    public IntegerProperty cantidadProperty() {
        return cantidad;
    }

    public IntegerProperty stockCriticoProperty() {
        return stockCritico;
    }

    public StringProperty nomCategoriaProperty() {
        return nomCategoria;
    }
}

