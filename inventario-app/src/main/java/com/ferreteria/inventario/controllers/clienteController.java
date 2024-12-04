package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.Cliente;
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

public class clienteController {

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, String> colRut;

    @FXML
    private TableColumn<Cliente, String> colNombre;

    @FXML
    private TableColumn<Cliente, String> colApellido;

    @FXML
    private TableColumn<Cliente, String> colCorreo;

    @FXML
    private TextField txtRut;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtCorreo;

    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
        cargarClientes();
        tablaClientes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                rellenarCampos(newValue);
            }
        });
    }

    private void configurarTabla() {
        colRut.setCellValueFactory(new PropertyValueFactory<>("rut"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        tablaClientes.setItems(listaClientes);
    }

    private void cargarClientes() {
        listaClientes.clear();
        String query = "SELECT rut_cliente, nombre, apellido, correo FROM cliente WHERE soft_delete = 0";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                listaClientes.add(new Cliente(
                        resultSet.getString("rut_cliente"),
                        resultSet.getString("nombre"),
                        resultSet.getString("apellido"),
                        resultSet.getString("correo")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al cargar los clientes.");
        }
    }

    private void rellenarCampos(Cliente cliente) {
        txtRut.setText(cliente.getRut());
        txtNombre.setText(cliente.getNombre());
        txtApellido.setText(cliente.getApellido());
        txtCorreo.setText(cliente.getCorreo());
    }

    @FXML
    private void agregarClienteView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Administrador/Opciones/cliente/agregarClienteView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Agregar Cliente");
            stage.setScene(new Scene(root));
            stage.show();
            stageManager.addStage(stage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void modificarClienteView(ActionEvent event) {
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado == null) {
            Mensaje.mostrarMensaje("advertencia", "Por favor, selecciona un cliente para modificar.", "Debe seleccionar un cliente para modificar.");
            return;
        }

        try {
            // Cargar el archivo FXML de la vista de modificación de cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Administrador/Opciones/cliente/modificarClienteView.fxml"));
            Parent root = loader.load();

            // Pasar los datos del cliente seleccionado al controlador de la vista
            modificarClienteController controller = loader.getController();
            controller.cargarDatosCliente(clienteSeleccionado);

            // Crear una nueva ventana para la vista de modificación
            Stage modificarStage = new Stage();
            modificarStage.setScene(new Scene(root));
            modificarStage.setTitle("Modificar Cliente");
            modificarStage.initModality(Modality.WINDOW_MODAL);
            modificarStage.initOwner(tablaClientes.getScene().getWindow());
            modificarStage.showAndWait(); // Espera hasta que la ventana se cierre antes de continuar

        } catch (IOException e) {
            Mensaje.mostrarMensaje("error", "Error al cargar la vista", "No se pudo cargar la vista de modificación de cliente.");
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarCliente() {
        // Obtener el cliente seleccionado
        Cliente clienteSeleccionado = tablaClientes.getSelectionModel().getSelectedItem();
    
        if (clienteSeleccionado == null) {
            Mensaje.mostrarMensaje("advertencia", "Selecciona un cliente", "Debes seleccionar un cliente para eliminar.");
            return;
        }
    
        // Confirmar la eliminación
        boolean confirmar = Mensaje.mostrarConfirmacion("Eliminar Cliente", "¿Estás seguro de que deseas eliminar al cliente?");
        if (!confirmar) {
            return;
        }
    
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE cliente SET soft_delete = 1 WHERE rut_cliente = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, clienteSeleccionado.getRut());
    
            int rows = stmt.executeUpdate();
            
            // Si la actualización fue exitosa
            if (rows > 0) {
                Mensaje.mostrarMensaje("informacion", "Éxito", "Cliente eliminado correctamente.");
                cargarClientes();
            } else {
                // Si no se afectaron filas
                Mensaje.mostrarMensaje("error", "Error", "No se pudo eliminar el cliente.");
            }
    
        } catch (SQLException e) {
            // Mostrar solo los errores que realmente ocurran en la base de datos
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Error en la base de datos: " + e.getMessage());
        }
    }
    


    @FXML
    public void Actualizar(){
        cargarClientes();
    }


}
