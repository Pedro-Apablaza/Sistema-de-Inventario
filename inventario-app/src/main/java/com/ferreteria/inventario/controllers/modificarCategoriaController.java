package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.models.Categoria;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;

public class modificarCategoriaController {

    @FXML
    private TextField txtCategoria;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnModificar;

    private Categoria categoriaSeleccionada;

    // Método para inicializar la vista con la categoría seleccionada
    public void initData(Categoria categoria) {
        this.categoriaSeleccionada = categoria;
        // Rellenar el campo de texto con el nombre de la categoría seleccionada
        txtCategoria.setText(categoria.getNomCategoria());
    }

    @FXML
    public void modificarCategoria() {
        String nuevoNombre = txtCategoria.getText().trim();

        // Validar si el nombre está vacío
        if (nuevoNombre.isEmpty()) {
            Mensaje.mostrarMensaje("error", "Error", "El nombre de la categoría no puede estar vacío.");
            return;
        }

        // Validación: Verificar que el nombre solo contenga caracteres de la A-Z
        if (!nuevoNombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$")) {
            Mensaje.mostrarMensaje("error", "Error", "El nombre de la categoría solo puede contener letras");
            return;
        }

        // Verificar si el nombre ya existe (solo si es diferente al nombre actual)
        if (!nuevoNombre.equals(categoriaSeleccionada.getNomCategoria()) && buscarDuplicado(nuevoNombre)) {
            Mensaje.mostrarMensaje("error", "Error", "Ya existe una categoría con ese nombre.");
            return;
        }

        // Actualizar la categoría en la base de datos
        if (actualizarCategoria(nuevoNombre)) {
            Mensaje.mostrarMensaje("informacion", "Categoría Modificada", "La categoría se ha modificado correctamente.");
            cancelarCategoria();
        } else {
            Mensaje.mostrarMensaje("error", "Error", "Hubo un problema al modificar la categoría.");
        }
    }

    // Método para verificar si ya existe una categoría con el mismo nombre
    private boolean buscarDuplicado(String nombreCategoria) {
        String query = "SELECT COUNT(*) FROM categoria WHERE nom_categoria = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nombreCategoria);
            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0; // Si el resultado es mayor a 0, existe un duplicado
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método para actualizar la categoría en la base de datos
    private boolean actualizarCategoria(String nuevoNombre) {
        String query = "UPDATE categoria SET nom_categoria = ? WHERE id_categoria = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nuevoNombre);
            stmt.setInt(2, categoriaSeleccionada.getIdCategoria());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método para cerrar la ventana
    @FXML
    private void cancelarCategoria() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

}
