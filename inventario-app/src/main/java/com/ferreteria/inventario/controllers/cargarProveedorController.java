package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.models.Proveedor;
import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.utils.Mensaje;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class cargarProveedorController {
    @FXML
    private TableView<Proveedor> tblProveedores;

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

    @FXML
    private Button btnSeleccionar;

    private ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
        cargarProveedores();
        tblProveedores.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                rellenarCampos(newValue);
            }
        });
    }

    private void configurarTabla() {
        colRut.setCellValueFactory(new PropertyValueFactory<>("rutProveedor"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));
        tblProveedores.setItems(listaProveedores);
    }

    private void cargarProveedores() {
        listaProveedores.clear();
        String query = "SELECT rut_proveedor, nombre, direccion, correo FROM proveedor WHERE soft_delete = 0";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                listaProveedores.add(new Proveedor(
                        resultSet.getString("rut_proveedor"),
                        resultSet.getString("nombre"),
                        resultSet.getString("direccion"),
                        resultSet.getString("correo"),
                        false // Soft delete no se utiliza aquí, pero se inicializa.
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al cargar los proveedores.");
        }
    }

    private void rellenarCampos(Proveedor proveedor) {
        txtRut.setText(proveedor.getRutProveedor());
        txtNombre.setText(proveedor.getNombre());
        txtDireccion.setText(proveedor.getDireccion());
        txtCorreo.setText(proveedor.getCorreo());
    }

    private comprasController1 controladorProveedor;

    // Este método se usa para establecer el controlador de modificar proveedor
    public void setControladorProveedor(comprasController1 controlador) {
        this.controladorProveedor = controlador;
    }

    // Método para seleccionar un proveedor de la tabla
    public void seleccionarProveedor() {
        if (controladorProveedor != null) {
            String rutProveedor = txtRut.getText();
            String nombreProveedor = txtNombre.getText();
            String direccionProveedor = txtDireccion.getText();
            String correoProveedor = txtCorreo.getText();

            controladorProveedor.recibirDatosProveedor(rutProveedor, nombreProveedor, direccionProveedor, correoProveedor);

            Mensaje.mostrarMensaje("informacion", "ÉXITO", "El proveedor se seleccionó con éxito.");

            Stage stage = (Stage) btnSeleccionar.getScene().getWindow();
            stage.close();
        } else {
            Mensaje.mostrarMensaje("error", "ERROR", "No se pudo pasar la información del proveedor.");
        }
    }
}
