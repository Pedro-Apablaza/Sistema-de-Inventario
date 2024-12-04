package com.ferreteria.inventario.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.utils.ValidarRut;

public class agregarProveedorController {

    @FXML
    private TextField txtRutProveedor;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtDireccion;

    @FXML
    private TextField txtCorreo;

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnAgregar;

@FXML
private void agregarProveedor() {

    // Obtener valores de los campos
    String rutProveedor = txtRutProveedor.getText().trim();
    String nombre = txtNombre.getText().trim();
    String direccion = txtDireccion.getText().trim();
    String correo = txtCorreo.getText().trim();

    // Validar los datos ingresados
    if (rutProveedor.isEmpty() || nombre.isEmpty() || direccion.isEmpty() || correo.isEmpty()) {
        Mensaje.mostrarMensaje("advertencia", "Todos los campos son obligatorios.", "Rellene todos los campos.");
        return;
    }

    // Validar que el nombre solo tenga caracteres A-Z, incluyendo tildes y ñ
    if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$")) {
        Mensaje.mostrarMensaje("advertencia", "Nombre inválido", "El nombre solo puede contener letras de la A-Z, incluyendo tildes y la letra ñ.");
        return;
    }

    // Validar el formato del correo
    if (!correo.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
        Mensaje.mostrarMensaje("advertencia", "El correo electrónico no es válido.", "Error en el formato del E-mail");
        return;
    }

    // Validar que el RUT sea válido
    if (!ValidarRut.validarRut(rutProveedor)) {
        Mensaje.mostrarMensaje("advertencia", "RUT inválido", "El RUT ingresado no es válido.");
        return;
    }

    // Validar que el RUT no esté duplicado
    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = "SELECT COUNT(*) FROM proveedor WHERE rut_proveedor = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, rutProveedor);
        ResultSet rs = stmt.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            Mensaje.mostrarMensaje("advertencia", "RUT Duplicado", "Ya existe un proveedor con ese RUT.");
            return;
        }
    } catch (Exception e) {
        e.printStackTrace();
        Mensaje.mostrarMensaje("error", "Error", "Hubo un error al verificar el RUT en la base de datos.");
        return;
    }

    // Validar que el correo no esté duplicado
    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = "SELECT COUNT(*) FROM proveedor WHERE correo = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, correo);
        ResultSet rs = stmt.executeQuery();

        if (rs.next() && rs.getInt(1) > 0) {
            Mensaje.mostrarMensaje("advertencia", "Correo Duplicado", "Ya existe un proveedor con ese correo electrónico.");
            return;
        }
    } catch (Exception e) {
        e.printStackTrace();
        Mensaje.mostrarMensaje("error", "Error", "Hubo un error al verificar el correo en la base de datos.");
        return;
    }

    // Insertar datos en la base de datos
    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = "INSERT INTO proveedor (rut_proveedor, nombre, direccion, correo) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, rutProveedor);
        stmt.setString(2, nombre);
        stmt.setString(3, direccion);
        stmt.setString(4, correo);

        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected > 0) {
            Mensaje.mostrarMensaje("informacion", "Proveedor Agregado", "El proveedor se ha agregado correctamente.");
            // Limpiar los campos después de agregar
            txtRutProveedor.clear();
            txtNombre.clear();
            txtDireccion.clear();
            txtCorreo.clear();

            Stage stage = (Stage) txtRutProveedor.getScene().getWindow();
            stage.close();
        } else {
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al agregar el proveedor.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        Mensaje.mostrarMensaje("error", "Error", "Hubo un error al conectar con la base de datos.");
    }
}

    
    @FXML
    private void cancelar(){
        Stage stage = (Stage) txtRutProveedor.getScene().getWindow();
        stage.close();
    }
    
}
