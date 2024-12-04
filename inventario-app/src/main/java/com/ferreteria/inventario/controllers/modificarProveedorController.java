package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.models.Proveedor;
import com.ferreteria.inventario.utils.ValidarRut;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.DatabaseConnection;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class modificarProveedorController {

    @FXML
    private Button btnCancelar;

    @FXML
    private Button btnModificar;

    @FXML
    private TextField txtRutProveedor;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtDireccion;

    @FXML
    private TextField txtCorreo;

    public void cargarDatosProveedor(Proveedor proveedor) {
        // Cargar datos del proveedor en los campos de texto
        txtRutProveedor.setText(proveedor.getRutProveedor());
        txtNombre.setText(proveedor.getNombre());
        txtDireccion.setText(proveedor.getDireccion());
        txtCorreo.setText(proveedor.getCorreo());
    }

    @FXML
    private void modificarProveedor() {
        String rutProveedor = txtRutProveedor.getText().trim();
        String nombre = txtNombre.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String correo = txtCorreo.getText().trim();
    
        // Validaciones
        if (nombre.isEmpty() || direccion.isEmpty() || correo.isEmpty()) {
            Mensaje.mostrarMensaje("advertencia", "Campos vacíos", "Todos los campos son obligatorios.");
            return;
        }
    
        // Validar formato del nombre
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$")) {
            Mensaje.mostrarMensaje("advertencia", "Nombre inválido", "El nombre solo debe contener letras y espacios.");
            return;
        }
    
        // Validar RUT
        if (!ValidarRut.validarRut(rutProveedor)) {
            Mensaje.mostrarMensaje("advertencia", "RUT inválido", "El RUT ingresado no es válido.");
            return;
        }
    
        // Validar correo electrónico
        if (!correo.matches("^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
            Mensaje.mostrarMensaje("advertencia", "Correo inválido", "El correo electrónico no es válido.");
            return;
        }
    
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Verificar si el proveedor existe en la base de datos
            String checkSql = "SELECT rut_proveedor, correo FROM proveedor WHERE rut_proveedor = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, rutProveedor);
            ResultSet checkRs = checkStmt.executeQuery();
            
            // Verificamos si el proveedor existe
            if (!checkRs.next()) {
                Mensaje.mostrarMensaje("error", "Proveedor no encontrado", "El RUT del proveedor no existe en la base de datos.");
                return;
            }
    
            // Verificar si el RUT es único (para el caso de editar, ignoramos el proveedor actual)
            String checkRutSql = "SELECT COUNT(*) FROM proveedor WHERE rut_proveedor = ? AND rut_proveedor != ?";
            PreparedStatement checkRutStmt = conn.prepareStatement(checkRutSql);
            checkRutStmt.setString(1, rutProveedor);  // RUT que estamos intentando insertar
            checkRutStmt.setString(2, rutProveedor);  // Excluimos el propio RUT en la validación
            ResultSet checkRutRs = checkRutStmt.executeQuery();
            if (checkRutRs.next() && checkRutRs.getInt(1) > 0) {
                Mensaje.mostrarMensaje("advertencia", "RUT duplicado", "El RUT ingresado ya está asociado a otro proveedor.");
                return;
            }
    
            // Verificar si el correo es único (para el caso de editar, ignoramos el proveedor actual)
            String checkCorreoSql = "SELECT COUNT(*) FROM proveedor WHERE correo = ? AND rut_proveedor != ?";
            PreparedStatement checkCorreoStmt = conn.prepareStatement(checkCorreoSql);
            checkCorreoStmt.setString(1, correo);  // Correo que estamos intentando insertar
            checkCorreoStmt.setString(2, rutProveedor);  // Excluimos el proveedor actual en la validación
            ResultSet checkCorreoRs = checkCorreoStmt.executeQuery();
            if (checkCorreoRs.next() && checkCorreoRs.getInt(1) > 0) {
                Mensaje.mostrarMensaje("advertencia", "Correo duplicado", "El correo electrónico ya está asociado a otro proveedor.");
                return;
            }
    
            // Si todas las validaciones pasan, realizamos la actualización
            String sql = "UPDATE proveedor SET nombre = ?, direccion = ?, correo = ? WHERE rut_proveedor = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, nombre);
            stmt.setString(2, direccion);
            stmt.setString(3, correo);
            stmt.setString(4, rutProveedor);
    
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                Mensaje.mostrarMensaje("informacion", "Éxito", "Proveedor modificado correctamente.");
                // Cerrar la ventana de modificación
                txtRutProveedor.clear();
                txtNombre.clear();
                txtDireccion.clear();
                txtCorreo.clear();

                Stage stage = (Stage) txtRutProveedor.getScene().getWindow();
                stage.close();
            } else {
                Mensaje.mostrarMensaje("error", "Error", "No se pudo modificar el proveedor.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Error en la base de datos: " + e.getMessage());
        }
    }
    
    


    @FXML
    private void cancelar() {
        // Cerrar la ventana sin hacer nada
        Stage stage = (Stage) txtRutProveedor.getScene().getWindow();
        stage.close();
    }
}
