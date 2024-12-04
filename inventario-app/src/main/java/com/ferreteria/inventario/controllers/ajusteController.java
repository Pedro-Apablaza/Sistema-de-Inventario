package com.ferreteria.inventario.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.AjusteStock;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.utils.stageManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class ajusteController {

    @FXML
    private TableView<AjusteStock> tblAjuste;
    @FXML
    private TableColumn<AjusteStock, Long> colId;
    @FXML
    private TableColumn<AjusteStock, String> colRutUsuario;
    @FXML
    private TableColumn<AjusteStock, Long> colIdProducto;
    @FXML
    private TableColumn<AjusteStock, Integer> colCantidad;
    @FXML
    private TableColumn<AjusteStock, String> colFecha;
    @FXML
    private TableColumn<AjusteStock, String> colTipo;
    @FXML
    private TableColumn<AjusteStock, String> colMotivo;

    @FXML
    private TextField txtID;
    @FXML
    private TextField txtRut;
    @FXML
    private TextField txtProducto;
    @FXML
    private TextField txtUsuario;
    @FXML
    private TextField txtTipo;
    @FXML
    private TextArea txtMotivo;
    @FXML
    private DatePicker dtpFecha;

    public void initialize() {
        // Asignar las propiedades de las columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("idAjuste"));
        colRutUsuario.setCellValueFactory(new PropertyValueFactory<>("rutUsuario"));
        colIdProducto.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));

        // Cargar los datos en la tabla
        cargarDatos();
        
        tblAjuste.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                cargarDetalle(newValue);
            }
        });
    }

    public void cargarDatos() {
        ObservableList<AjusteStock> listaAjustes = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_ajuste, rut_usuario, id_producto, cantidad, DATE_FORMAT(fecha, '%Y-%m-%d %H:%i:%s') AS fecha, tipo, motivo " +
                        "FROM ajuste_stock WHERE 1=1"; // Agrega filtros si es necesario
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                listaAjustes.add(new AjusteStock(
                    rs.getLong("id_ajuste"),
                    rs.getString("rut_usuario"),
                    rs.getLong("id_producto"),
                    rs.getInt("cantidad"),
                    rs.getString("fecha"),
                    rs.getString("tipo"),
                    rs.getString("motivo")
                ));
            }

            // Asignar los datos a la tabla
            tblAjuste.setItems(listaAjustes);

        } catch (SQLException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error al cargar datos", "Error", "No se pudo cargar la tabla de ajustes.");
        }
    }



    @FXML
    public void agregarAjusteView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Bodega/Opciones/stock/AgregarAjusteView.fxml"));
            Parent root = loader.load();
            
            // Obtener el controlador de la vista cargada
            agregarAjusteController controlador = loader.getController();
            
            // Pasar la referencia del controlador actual
            controlador.setControladorPadre(this);
            
            // Crear la ventana
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Agregar Ajuste");
            stageManager.addStage(stage); // Registrar la ventana abierta
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


    private void cargarDetalle(AjusteStock ajuste) {
        txtID.setText(String.valueOf(ajuste.getIdAjuste()));
        txtRut.setText(ajuste.getRutUsuario());
        txtProducto.setText(String.valueOf(ajuste.getIdProducto()));
        txtUsuario.setText(ajuste.getRutUsuario()); // Cambia si tienes un nombre de usuario asociado
        txtTipo.setText(ajuste.getTipo());
        txtMotivo.setText(ajuste.getMotivo());

        // Parsear la fecha a LocalDate si es compatible con el DatePicker
        try {
            LocalDate fecha = LocalDate.parse(ajuste.getFecha(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            dtpFecha.setValue(fecha);
        } catch (DateTimeParseException e) {
            dtpFecha.setValue(null); // Si no es válida, limpiar el campo
        }
    }
}


