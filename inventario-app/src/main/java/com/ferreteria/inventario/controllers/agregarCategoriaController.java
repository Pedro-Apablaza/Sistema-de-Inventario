package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.utils.Mensaje;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;

public class agregarCategoriaController {

    @FXML
    private Button btnCancelar;

    @FXML
    private TextField txtCategoria;

    // Función para verificar si ya existe una categoría con el mismo nombre
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

    @FXML
    private void agregarCategoria(ActionEvent event) {
        String nombreCategoria = txtCategoria.getText().trim();
    
        // Validación: Verificar si el campo está vacío
        if (nombreCategoria.isEmpty()) {
            Mensaje.mostrarMensaje("error", "Error", "El nombre de la categoría no puede estar vacío.");
            return;
        }
    
        // Validación: Verificar que el nombre solo contenga caracteres de la A-Z
        if (!nombreCategoria.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$")) {
            Mensaje.mostrarMensaje("error", "Error", "El nombre de la categoría solo puede contener letras.");
            return;
        }
    
        // Validación: Verificar si el nombre de la categoría ya existe
        if (buscarDuplicado(nombreCategoria)) {
            Mensaje.mostrarMensaje("error", "Error", "Ya existe una categoría con ese nombre.");
            return;
        }
    
        // Si no hay duplicados, insertar la nueva categoría en la base de datos
        String query = "INSERT INTO categoria (nom_categoria) VALUES (?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nombreCategoria);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                Mensaje.mostrarMensaje("informacion", "Categoría Agregada", "La categoría se ha agregado correctamente.");
                txtCategoria.clear(); // Limpiar el campo después de agregar
                Stage stage = (Stage) btnCancelar.getScene().getWindow();
                stage.close();
            } else {
                Mensaje.mostrarMensaje("error", "Error", "Hubo un error al agregar la categoría.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al conectar con la base de datos.");
        }
    }
    

    // Método para cerrar la ventana
    @FXML
    private void cancelarCategoria() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }


}
