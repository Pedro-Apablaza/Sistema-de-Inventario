package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.utils.SessionManager;
import com.ferreteria.inventario.utils.stageManager;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {

    @FXML
    private TextField txtUsuario; // Campo para el nombre de usuario
    @FXML
    private PasswordField txtContrasena; // Campo para la contraseña

    /**
     * Método para manejar el evento de login al presionar el botón "Iniciar Sesión".
     */
    @FXML
    private void Login() {
        String nomUsuario = txtUsuario.getText().trim();
        String contrasena = txtContrasena.getText().trim();

        // Validar campos vacíos
        if (nomUsuario.isEmpty() || contrasena.isEmpty()) {
            Mensaje.mostrarMensaje("Error", "ADVERTENCIA", "Por favor, complete todos los campos.");
            return;
        }

        // Validar credenciales en la base de datos
        if (validarCredenciales(nomUsuario, contrasena)) {
            abrirMenuPrincipal();
        } else {
            Mensaje.mostrarMensaje("Error", "ERROR", "Usuario o contraseña incorrectos.");
        }
    }


    private boolean validarCredenciales(String nomUsuario, String contrasena) {
        String query = "SELECT * FROM usuario WHERE nom_usuario = ? AND contraseña = ? AND soft_delete = 0";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nomUsuario);
            stmt.setString(2, contrasena);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // Guardar los datos del usuario en la sesión
                    SessionManager session = SessionManager.getInstance();
                    session.setRutUsuario(rs.getString("rut_usuario"));
                    session.setNombre(rs.getString("nombre"));
                    session.setApellido(rs.getString("apellido"));
                    session.setNomUsuario(rs.getString("nom_usuario"));
                    session.setCorreo(rs.getString("correo"));
                    session.setIdRol(rs.getInt("id_rol"));
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "ERROR", "Ocurrió un error al validar las credenciales.");
        }
        return false;
    }

    @FXML
    private void Salir() {
        // Limpiar los datos de la sesión
        SessionManager session = SessionManager.getInstance();
        session.clear(); // Limpiar la sesión del usuario

        // Cerrar la ventana actual
        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        stage.close();

        // Detener completamente la aplicación
        Platform.exit();
    }

    private void abrirMenuPrincipal() {
        try {
            // Cargar el archivo FXML para la vista del menú principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/MenuView.fxml"));
            
            // Cargar el escenario (Stage)
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Menú Principal");
            
            // Mostrar la ventana
            stage.show();
            stageManager.addStage(stage);

            // Cerrar la ventana de login actual
            Stage currentStage = (Stage) txtUsuario.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "Ocurrió un error al abrir el menú principal.", "ERROR");
        }
    }

}
