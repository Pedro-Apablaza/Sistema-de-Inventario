package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.utils.stageManager;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class administradorController {
    
    @FXML
    public void productoView() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/administrador/Opciones/producto/productoView.fxml"));
            VBox root = loader.load();
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Productos");
            stage.show();
            // Registrar la ventana abierta
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void informacionEmpresaView() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/administrador/Opciones/informacionEmpresaView.fxml"));
            VBox root = loader.load();            
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Información de Empresa");
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void usuarioView() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/administrador/Opciones/usuario/usuarioView.fxml"));
            VBox root = loader.load();
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Usuarios");
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void categoriaView() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/administrador/Opciones/categoria/categoriaView.fxml"));
            VBox root = loader.load();
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Categorías");
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void proveedorView() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/administrador/Opciones/proveedor/proveedorView.fxml"));
            VBox root = loader.load();
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Proveedores");
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void clienteView() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/administrador/Opciones/cliente/clienteView.fxml"));
            VBox root = loader.load();
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Clientes");
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
