package com.ferreteria.inventario.models;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Categoria {
    private final IntegerProperty idCategoria;
    private final StringProperty nomCategoria;
    private boolean softDelete;

    public Categoria(int idCategoria, String nomCategoria, boolean softDelete) {
        this.idCategoria = new SimpleIntegerProperty(idCategoria);
        this.nomCategoria = new SimpleStringProperty(nomCategoria);
        this.softDelete = softDelete;
    }

    @Override
    public String toString() {
        return getNomCategoria(); // Solo devuelve el nombre de la categoría
    }

    public int getIdCategoria() {
        return idCategoria.get();
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria.set(idCategoria);
    }

    public IntegerProperty idCategoriaProperty() {
        return idCategoria;
    }

    public String getNomCategoria() {
        return nomCategoria.get();
    }

    public void setNomCategoria(String nomCategoria) {
        this.nomCategoria.set(nomCategoria);
    }

    public StringProperty nomCategoriaProperty() {
        return nomCategoria;
    }

    public boolean isSoftDelete() {
        return softDelete;
    }

    public void setSoftDelete(boolean softDelete) {
        this.softDelete = softDelete;
    }
}
