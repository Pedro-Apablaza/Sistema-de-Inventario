package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.utils.ValidarRut;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class agregarClienteController {

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnModificar;

    @FXML
    private TextField txtRutCliente;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtCorreo;

    @FXML
    private void agregarCliente() {
        // Obtener valores de los campos
        String rutCliente = txtRutCliente.getText().trim();
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String correo = txtCorreo.getText().trim();

        // Validar que los campos no estén vacíos
        if (rutCliente.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty()) {
            Mensaje.mostrarMensaje("Advertencia", "Todos los campos son obligatorios.", "Rellene todos los campos.");
            return;
        }

        // Validar el formato del correo electrónico
        if (!correo.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            Mensaje.mostrarMensaje("Advertencia", "El correo electrónico no es válido.", "Error en el formato del E-mail");
            return;
        }

        // Validar el formato del RUT
        if (!ValidarRut.validarRut(rutCliente)) {
            Mensaje.mostrarMensaje("advertencia", "RUT inválido", "El RUT ingresado no es válido.");
            return;
        }

        // Validar que el nombre solo contenga caracteres de la A-Z
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$")) {
            Mensaje.mostrarMensaje("advertencia", "Nombre inválido", "El nombre solo puede contener letras.");
            return;
        }

        // Validar que el apellido solo contenga caracteres de la A-Z
        if (!apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$")) {
            Mensaje.mostrarMensaje("advertencia", "Apellido inválido", "El apellido solo puede contener letras.");
            return;
        }

        // Validar si el RUT ya existe en la base de datos
        if (existeRut(rutCliente)) {
            Mensaje.mostrarMensaje("error", "RUT duplicado", "Ya existe un cliente con ese RUT.");
            return;
        }

        // Validar si el correo ya existe en la base de datos
        if (existeCorreo(correo)) {
            Mensaje.mostrarMensaje("error", "Correo duplicado", "Ya existe un cliente con ese correo.");
            return;
        }

        // Insertar datos en la base de datos
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO cliente (rut_cliente, nombre, apellido, correo) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rutCliente);
            stmt.setString(2, nombre);
            stmt.setString(3, apellido);
            stmt.setString(4, correo);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                Mensaje.mostrarMensaje("informacion", "Cliente Agregado", "El cliente se ha agregado correctamente.");
                // Limpiar los campos después de agregar
                txtRutCliente.clear();
                txtNombre.clear();
                txtApellido.clear();
                txtCorreo.clear();

                Stage stage = (Stage) txtRutCliente.getScene().getWindow();
                stage.close();
            } else {
                Mensaje.mostrarMensaje("error", "Error", "Hubo un error al agregar el cliente.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al conectar con la base de datos.");
        }
    }

    // Método para verificar si el RUT ya existe en la base de datos
    private boolean existeRut(String rut) {
        String query = "SELECT COUNT(*) FROM cliente WHERE rut_cliente = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, rut);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // El RUT ya existe
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // El RUT no existe
    }

    // Método para verificar si el correo ya existe en la base de datos
    private boolean existeCorreo(String correo) {
        String query = "SELECT COUNT(*) FROM cliente WHERE correo = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, correo);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // El correo ya existe
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // El correo no existe
    }


    @FXML
    private void cancelar() {
        Stage stage = (Stage) txtRutCliente.getScene().getWindow();
        stage.close();
    }
}
