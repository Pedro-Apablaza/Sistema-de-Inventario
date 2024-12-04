package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.models.Cliente;
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

public class modificarClienteController {

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

    public void cargarDatosCliente(Cliente cliente) {
        txtRutCliente.setText(cliente.getRut());
        txtNombre.setText(cliente.getNombre());
        txtApellido.setText(cliente.getApellido());
        txtCorreo.setText(cliente.getCorreo());
    }

    @FXML
    private void modificarCliente() {
        String rutCliente = txtRutCliente.getText().trim();
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String correo = txtCorreo.getText().trim();

        // Validaciones
        if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty() || rutCliente.isEmpty()) {
            Mensaje.mostrarMensaje("advertencia", "Campos vacíos", "Todos los campos son obligatorios.");
            return;
        }

        // Validar el formato del RUT
        if (!ValidarRut.validarRut(rutCliente)) {
            Mensaje.mostrarMensaje("advertencia", "RUT inválido", "El RUT ingresado no es válido.");
            return;
        }

        // Validar el formato del correo
        if (!correo.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            Mensaje.mostrarMensaje("advertencia", "Correo inválido", "El correo electrónico no es válido.");
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

        // Validar si el RUT ya existe en la base de datos, pero ignorando el propio RUT del cliente que está modificando
        if (existeRutModificado(rutCliente)) {
            Mensaje.mostrarMensaje("error", "RUT duplicado", "Ya existe un cliente con ese RUT.");
            return;
        }

        // Validar si el correo ya existe en la base de datos, pero ignorando el propio correo del cliente que está modificando
        if (existeCorreoModificado(correo)) {
            Mensaje.mostrarMensaje("error", "Correo duplicado", "Ya existe un cliente con ese correo.");
            return;
        }

        // Actualizar los datos en la base de datos
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE cliente SET nombre = ?, apellido = ?, correo = ? WHERE rut_cliente = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, apellido);
            stmt.setString(3, correo);
            stmt.setString(4, rutCliente);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                Mensaje.mostrarMensaje("informacion", "Éxito", "Cliente modificado correctamente.");

                // Cerrar la ventana de modificación
                Stage stage = (Stage) btnModificar.getScene().getWindow();
                stage.close();
            } else {
                Mensaje.mostrarMensaje("error", "Error", "No se pudo modificar el cliente.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Error en la base de datos: " + e.getMessage());
        }
    }

    // Método para verificar si el RUT ya existe en la base de datos, pero ignorando el cliente actual
    private boolean existeRutModificado(String rut) {
        String query = "SELECT COUNT(*) FROM cliente WHERE rut_cliente = ? AND rut_cliente != ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, rut);
            stmt.setString(2, txtRutCliente.getText().trim());  // Ignoramos el propio RUT
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true; // El RUT ya existe
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // El RUT no existe
    }

    // Método para verificar si el correo ya existe en la base de datos, pero ignorando el correo del cliente actual
    private boolean existeCorreoModificado(String correo) {
        String query = "SELECT COUNT(*) FROM cliente WHERE correo = ? AND rut_cliente != ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, correo);
            stmt.setString(2, txtRutCliente.getText().trim());  // Ignoramos el propio correo
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
        // Cerrar la ventana al presionar "Cancelar"
        Stage stage = (Stage) txtRutCliente.getScene().getWindow();
        stage.close();
    }
}

