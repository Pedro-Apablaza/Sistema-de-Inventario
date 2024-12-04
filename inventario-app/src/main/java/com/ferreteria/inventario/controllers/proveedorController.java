package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.Proveedor;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.utils.stageManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class proveedorController {

    @FXML
    private TableView<Proveedor> tablaProveedor;
    @FXML
    private TableColumn<Proveedor, String> colRut;
    @FXML
    private TableColumn<Proveedor, String> colNombre;
    @FXML
    private TableColumn<Proveedor, String> colDireccion;
    @FXML
    private TableColumn<Proveedor, String> colCorreo;

    @FXML
    private TextField txtRut;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtCorreo;

    private ObservableList<Proveedor> listaProveedores;

    @FXML
    public void initialize() {
        configurarTabla();
        cargarProveedores();
        tablaProveedor.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mostrarDetallesProveedor(newValue);
            }
        });
    }

    private void configurarTabla() {
        colRut.setCellValueFactory(new PropertyValueFactory<>("rutProveedor"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        listaProveedores = FXCollections.observableArrayList();
        tablaProveedor.setItems(listaProveedores);
    }

    private void cargarProveedores() {
        listaProveedores.clear();
        String query = "SELECT rut_proveedor, nombre, direccion, correo FROM proveedor WHERE soft_delete = 0";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            listaProveedores.clear();
            while (resultSet.next()) {
                Proveedor proveedor = new Proveedor(
                        resultSet.getString("rut_proveedor"),
                        resultSet.getString("nombre"),
                        resultSet.getString("direccion"),
                        resultSet.getString("correo"), 
                        false
                );
                listaProveedores.add(proveedor);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void mostrarDetallesProveedor(Proveedor proveedor) {
        txtRut.setText(proveedor.getRutProveedor());
        txtNombre.setText(proveedor.getNombre());
        txtDireccion.setText(proveedor.getDireccion());
        txtCorreo.setText(proveedor.getCorreo());
    }

    @FXML
    private void agregarProveedorView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Administrador/Opciones/proveedor/agregarProveedorView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Agregar Proveedor");
            stage.setScene(new Scene(root));
            stage.show();
            stageManager.addStage(stage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    @FXML
    private void modificarProveedorView() {
        Proveedor proveedorSeleccionado = tablaProveedor.getSelectionModel().getSelectedItem();

        if (proveedorSeleccionado == null) {
            Mensaje.mostrarMensaje("Advertencia", "Por favor, selecciona un proveedor para modificar.", "Debe seleccionar un proveedor para modificar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Administrador/Opciones/proveedor/modificarProveedorView.fxml"));
            Parent root = loader.load();

            modificarProveedorController controller = loader.getController();
            controller.cargarDatosProveedor(proveedorSeleccionado);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modificar Proveedor");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(tablaProveedor.getScene().getWindow());
            stage.showAndWait(); // Espera a que se cierre esta ventana antes de continuar en la principal

        } catch (IOException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "No se pudo cargar la vista de modificación.", "ERROR");
        }
    }


        @FXML
        private void eliminarProveedor() {
            // Obtener el cliente seleccionado
            Proveedor proveedorSeleccionado = tablaProveedor.getSelectionModel().getSelectedItem();
        
            if (proveedorSeleccionado == null) {
                Mensaje.mostrarMensaje("advertencia", "Selecciona un proveedor", "Debes seleccionar un proveedor para eliminar.");
                return;
            }
        
            // Confirmar la eliminación
            boolean confirmar = Mensaje.mostrarConfirmacion("Eliminar Proveedor", "¿Estás seguro de que deseas eliminar al proveedor?");
            if (!confirmar) {
                return;
            }
        
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE proveedor SET soft_delete = 1 WHERE rut_proveedor = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, proveedorSeleccionado.getRutProveedor());
        
                int rows = stmt.executeUpdate();
                
                // Si la actualización fue exitosa
                if (rows > 0) {
                    Mensaje.mostrarMensaje("informacion", "Éxito", "Proveedor eliminado correctamente.");
                    cargarProveedores();
                } else {
                    // Si no se afectaron filas
                    Mensaje.mostrarMensaje("error", "Error", "No se pudo eliminar el proveedor.");
                }
        
            } catch (SQLException e) {
                // Mostrar solo los errores que realmente ocurran en la base de datos
                e.printStackTrace();
                Mensaje.mostrarMensaje("error", "Error", "Error en la base de datos: " + e.getMessage());
            }
        }
    

    
    @FXML
    public void Actualizar(){
        cargarProveedores();
    }
}
