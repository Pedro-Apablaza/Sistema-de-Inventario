package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.utils.SessionManager;
import com.ferreteria.inventario.utils.stageManager;
import com.ferreteria.inventario.utils.Mensaje;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

public class menuController {

    @FXML
    private Label lblNombreUsuario;
    @FXML
    private Label lblCorreoUsuario;
    @FXML
    private Label lblRolUsuario;

    @FXML
    private Button btnAdministrador;
    @FXML
    private Button btnVentas;
    @FXML
    private Button btnBodega;

    // Método que se ejecuta al cargar la vista del menú
    @FXML
    public void initialize() {
        cargarDatosUsuario();
        filtrarOpcionesPorRol();
    }

    /**
     * Carga los datos del usuario desde la sesión y los muestra en las etiquetas (Labels).
     */
    private void cargarDatosUsuario() {
        try {
            SessionManager session = SessionManager.getInstance();

            if (session.getRutUsuario() == null) {
                Mensaje.mostrarMensaje("Error", "ERROR", "No se encontró una sesión activa.");
                cerrarAplicacion();
                return;
            }

            // Cargar nombre y apellido del usuario
            lblNombreUsuario.setText(session.getNombre() + " " + session.getApellido());

            // Cargar correo del usuario
            lblCorreoUsuario.setText(session.getCorreo());

            // Determinar y cargar el rol del usuario
            String rol;
            switch (session.getIdRol()) {
                case 1:
                    rol = "Superadmin";
                    break;
                case 2:
                    rol = "Administrador";
                    break;
                case 3:
                    rol = "Bodega";
                    break;
                case 4:
                    rol = "Ventas";
                    break;
                default:
                    rol = "Desconocido";
                    break;
            }
            lblRolUsuario.setText(rol);
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "ERROR", "Ocurrió un error al cargar los datos del usuario.");
        }
    }

    /**
     * Filtra las opciones disponibles según el rol del usuario.
     */
    private void filtrarOpcionesPorRol() {
        SessionManager session = SessionManager.getInstance();

        // Obtener el rol del usuario
        int idRol = session.getIdRol();

        // Deshabilitar las opciones que no correspondan al rol del usuario
        switch (idRol) {
            case 1: // Superadmin
                // Todos los botones están habilitados para Superadmin
                btnAdministrador.setDisable(false);
                btnVentas.setDisable(false);
                btnBodega.setDisable(false);
                break;
            case 2: // Administrador
                // Administrador solo tiene acceso a la vista de administración y bodega
                btnAdministrador.setDisable(false);
                btnVentas.setDisable(false);
                btnBodega.setDisable(false);
                break;
            case 3: // Bodega
                // Bodega solo tiene acceso a la vista de productos
                btnAdministrador.setDisable(true);
                btnVentas.setDisable(true);
                btnBodega.setDisable(false);
                break;
            case 4: // Ventas
                // Ventas solo tiene acceso a la vista de ventas
                btnAdministrador.setDisable(true);
                btnVentas.setDisable(false);
                btnBodega.setDisable(true);
                break;
            default:
                // Si el rol es desconocido, deshabilitar todas las opciones
                btnAdministrador.setDisable(true);
                btnVentas.setDisable(true);
                btnBodega.setDisable(true);
                break;
        }
    }

    /**
     * Cierra la aplicación de manera segura.
     */
    private void cerrarAplicacion() {
        Stage stage = (Stage) lblNombreUsuario.getScene().getWindow();
        stage.close();
    }

    /**
     * Método para manejar el evento de cerrar sesión.
     */
    @FXML
    private void cerrarSesion(ActionEvent event) throws IOException {
        // Limpiar la sesión del usuario
        SessionManager session = SessionManager.getInstance();
        session.clear();  // Limpiar los datos del usuario logueado
    
        // Cerrar todas las ventanas activas
        stageManager.closeAllStages();
    
        // Volver a cargar la vista de Login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/loginView.fxml"));
        Parent root = loader.load();
        Stage loginStage = new Stage();
        loginStage.setScene(new Scene(root));
        loginStage.setTitle("Login");
        loginStage.show();
    }
    

    /**
     * Método para manejar la acción de salir del sistema.
     */
    @FXML
    private void salirDelSistema() {
        cerrarAplicacion();
    }

    /**
     * Método para abrir la vista de administración.
     */
    @FXML
    private void administradorView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Administrador/AdministradorView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Administrador");
            stage.setScene(new Scene(root));
            
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "ERROR", "Ocurrió un error al abrir la vista de administración.");
        }
    }

    /**
     * Método para abrir la vista de ventas.
     */
    @FXML
    private void ventasView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Venta/ventaView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Ventas");
            stage.setScene(new Scene(root));
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "ERROR", "Ocurrió un error al abrir la vista de ventas.");
        }
    }

    /**
     * Método para abrir la vista de productos.
     */
    @FXML
    private void bodegaView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Bodega/bodegaView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Productos");
            stage.setScene(new Scene(root));
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "ERROR", "Ocurrió un error al abrir la vista de bodega.");
        }
    }
}
