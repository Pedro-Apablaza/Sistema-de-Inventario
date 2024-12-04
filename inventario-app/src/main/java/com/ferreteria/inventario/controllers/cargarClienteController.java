package com.ferreteria.inventario.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.Cliente;
import com.ferreteria.inventario.utils.Mensaje;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class cargarClienteController {
    @FXML
    private TableView<Cliente> tblClientes;

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
    @FXML
    private Button btnSeleccionar;

    private ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
        cargarClientes();
        tblClientes.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
        tblClientes.setItems(listaClientes);
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

    private ventasController controladorVenta;

    // Este método se usa para establecer el controlador de ventas
    public void setControladorVenta(ventasController controlador) {
        this.controladorVenta = controlador;
    }

    // Método para seleccionar un cliente de la tabla
    public void seleccionarCliente() {
        // Verificar si el controladorVenta es null antes de llamarlo
        if (controladorVenta != null) {
            // Suponiendo que estos campos contienen los datos del cliente seleccionado
            String rutCliente = txtRut.getText();
            String nombreCliente = txtNombre.getText();
            String apellidoCliente = txtApellido.getText();
            String correoCliente = txtCorreo.getText();

            // Llamar al método recibirDatosCliente del controlador de ventas
            controladorVenta.recibirDatosCliente(rutCliente, nombreCliente, apellidoCliente, correoCliente);

            // Mostrar un mensaje de éxito
            Mensaje.mostrarMensaje("informacion", "ÉXITO", "El cliente se selecciono con éxito.");

            // Cerrar la ventana de selección de cliente
            Stage stage = (Stage) btnSeleccionar.getScene().getWindow();
            stage.close();  // Cierra la ventana de cargarClienteView
        } else {
            // Si controladorVenta es null, mostrar un mensaje de error
            Mensaje.mostrarMensaje("error", "ERROR", "No se pudo pasar la información del cliente.");
        }
    }
}
