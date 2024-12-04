package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.Producto;
import com.ferreteria.inventario.utils.stageManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;



public class bodegaController {
    
    @FXML
    private TableView<Producto> tblBodega;

    @FXML
    private TableColumn<Producto, Long> ColID;
    @FXML
    private TableColumn<Producto, String> ColNombre;
    @FXML
    private TableColumn<Producto, Integer> ColCantidad;
    @FXML
    private TableColumn<Producto, Integer> ColStockCritico;

    private ObservableList<Producto> productosList = FXCollections.observableArrayList();


    public void initialize() {
        // Inicializamos las columnas de la tabla
        ColID.setCellValueFactory(cellData -> cellData.getValue().idProductoProperty().asObject());
        ColNombre.setCellValueFactory(cellData -> cellData.getValue().nomProductoProperty());
        ColCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        ColStockCritico.setCellValueFactory(cellData -> cellData.getValue().stockCriticoProperty().asObject());

        // Cargamos los productos cuando se inicie la vista
        cargarProductosBajoStock();
    }

    public void cargarProductosBajoStock() {
        // Consulta mejorada para obtener el nombre de la categoría también
        String query = "SELECT p.id_producto, p.nom_producto, p.cantidad, p.stock_critico, c.nom_categoria " +
                       "FROM producto p " +
                       "JOIN categoria c ON p.id_categoria = c.id_categoria " +
                       "WHERE p.cantidad <= p.stock_critico AND p.soft_delete = 0";
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
    
            productosList.clear(); // Limpiar la lista antes de llenarla
    
            while (rs.next()) {
                long idProducto = rs.getLong("id_producto");
                String nombreProducto = rs.getString("nom_producto");
                int cantidad = rs.getInt("cantidad");
                int stockCritico = rs.getInt("stock_critico");
                String nombreCategoria = rs.getString("nom_categoria");  // Obtenemos el nombre de la categoría
    
                // Agregamos el producto a la lista
                productosList.add(new Producto(idProducto, nombreProducto, 0, cantidad, stockCritico, nombreCategoria)); // Asignamos precio en 0 por ahora
            }
    
            // Asignar la lista a la tabla
            tblBodega.setItems(productosList);
    
        } catch (SQLException e) {
            e.printStackTrace(); // Manejo de errores
        }
    }
    

    @FXML
    public void entradaView() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Bodega/Opciones/registroEntradasView.fxml"));
            VBox root = loader.load();
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Entradas");
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void salidaView() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Bodega/Opciones/registroSalidasView.fxml"));
            VBox root = loader.load();
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Salidas");
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void stockView() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Bodega/Opciones/stock/stockView.fxml"));
            VBox root = loader.load();
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Administrar Stock");
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void compraView() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Bodega/Opciones/compra/comprasView.fxml"));
            VBox root = loader.load();
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Compra");
            stage.show();
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
