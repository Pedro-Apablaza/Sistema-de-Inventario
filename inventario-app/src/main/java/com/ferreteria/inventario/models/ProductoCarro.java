package com.ferreteria.inventario.models;

import javafx.beans.property.*;

public class ProductoCarro {
    private final LongProperty idProducto;
    private final StringProperty nomProducto;
    private final IntegerProperty cantidad;
    private final StringProperty nomCategoria;
    private final IntegerProperty precio;

    // Constructor corregido para aceptar Long en idProducto
    public ProductoCarro(Long idProducto, String nomProducto, int cantidad, String nomCategoria, Integer precio) {
        this.idProducto = new SimpleLongProperty(idProducto);
        this.nomProducto = new SimpleStringProperty(nomProducto);
        this.cantidad = new SimpleIntegerProperty(cantidad);
        this.nomCategoria = new SimpleStringProperty(nomCategoria);
        this.precio = new SimpleIntegerProperty(precio);
    }

    // Métodos Property para cada campo
    public LongProperty idProductoProperty() {
        return idProducto;
    }

    public StringProperty nomProductoProperty() {
        return nomProducto;
    }

    public IntegerProperty cantidadProperty() {
        return cantidad;
    }

    public StringProperty nomCategoriaProperty() {
        return nomCategoria;
    }

    public IntegerProperty precioProperty() {
        return precio;
    }

    // Métodos Getter para obtener el valor de cada propiedad
    public Long getIdProducto() {
        return idProducto.get();
    }

    public String getNomProducto() {
        return nomProducto.get();
    }

    public int getCantidad() {
        return cantidad.get();
    }

    public String getNomCategoria() {
        return nomCategoria.get();
    }

    public Integer getPrecio() {
        return precio.get();
    }
}

