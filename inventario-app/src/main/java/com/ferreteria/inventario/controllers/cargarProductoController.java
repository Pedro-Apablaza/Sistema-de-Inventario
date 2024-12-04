package com.ferreteria.inventario.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.Categoria;
import com.ferreteria.inventario.models.Producto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class cargarProductoController {

    @FXML
    private TableView<Producto> tblAjuste;
    @FXML
    private TableColumn<Producto, Long> ColID;
    @FXML
    private TableColumn<Producto, String> ColNombre;
    @FXML
    private TableColumn<Producto, Integer> ColCantidad;
    @FXML
    private TableColumn<Producto, Integer> ColPrecio;
    @FXML
    private TableColumn<Producto, String> ColCategoria;

    @FXML
    private FlowPane fpnCategoria;



    @FXML
    private void initialize() {
        cargarCategorias();
        cargarProductos();
        
        // Vinculamos las columnas con las propiedades de los productos
        ColID.setCellValueFactory(cellData -> cellData.getValue().idProductoProperty().asObject());
        ColNombre.setCellValueFactory(cellData -> cellData.getValue().nomProductoProperty());
        ColCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        ColPrecio.setCellValueFactory(cellData -> cellData.getValue().precioProperty().asObject());
        ColCategoria.setCellValueFactory(cellData -> cellData.getValue().nomCategoriaProperty());
    }
    
    


    private void cargarCategorias() {
        ObservableList<Categoria> categorias = obtenerCategoriasDeBaseDeDatos();
        
        // Crear un botón para cada categoría obtenida de la base de datos
        for (Categoria categoria : categorias) {
            Button btnCategoria = new Button(categoria.getNomCategoria());
            btnCategoria.setPrefWidth(126);
            btnCategoria.setPrefHeight(122);
            btnCategoria.setOnAction(event -> filtrarProductosPorCategoria(categoria.getNomCategoria()));
            fpnCategoria.getChildren().add(btnCategoria);
        }
    }
    
    
    private ObservableList<Categoria> obtenerCategoriasDeBaseDeDatos() {
        ObservableList<Categoria> categorias = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_categoria, nom_categoria FROM categoria WHERE soft_delete = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int idCategoria = rs.getInt("id_categoria");
                String nomCategoria = rs.getString("nom_categoria");
                categorias.add(new Categoria(idCategoria, nomCategoria, false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categorias;
    }
    
    private void filtrarProductosPorCategoria(String nomCategoria) {
        // Recargar productos desde la base de datos solo si es necesario
        ObservableList<Producto> productos = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.id_producto, p.nom_producto, p.cantidad, p.precio, c.nom_categoria " +
                         "FROM producto p " +
                         "JOIN categoria c ON p.id_categoria = c.id_categoria " +
                         "WHERE p.soft_delete = 0 AND c.soft_delete = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Producto producto = new Producto(
                    rs.getLong("id_producto"),
                    rs.getString("nom_producto"),
                    rs.getInt("precio"),
                    rs.getInt("cantidad"),
                    0,  // stockCritico, no lo estamos utilizando en esta vista
                    rs.getString("nom_categoria")
                );
                productos.add(producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        // Filtrar los productos según la categoría seleccionada
        ObservableList<Producto> productosFiltrados = FXCollections.observableArrayList();
        for (Producto producto : productos) {
            if (nomCategoria.equals("Sin categorías") || producto.getNomCategoria().equals(nomCategoria)) {
                productosFiltrados.add(producto);
            }
        }
    
        // Reemplazar los productos en la tabla con la lista filtrada
        tblAjuste.setItems(productosFiltrados);
    }
    
    

    private void cargarProductos() {
        ObservableList<Producto> productos = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.id_producto, p.nom_producto, p.cantidad, p.precio, c.nom_categoria " +
                         "FROM producto p " +
                         "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                         "WHERE p.soft_delete = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Producto producto = new Producto(
                    rs.getLong("id_producto"),
                    rs.getString("nom_producto"),
                    rs.getInt("precio"),
                    rs.getInt("cantidad"),
                    0,  // stockCritico (puedes ajustarlo según sea necesario)
                    rs.getString("nom_categoria") // Asignamos el nombre de la categoría
                );
                productos.add(producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Asignar la lista de productos a la tabla tblAjuste
        tblAjuste.setItems(productos);
        
    }

    @FXML
    private void SeleccionarProducto() {
        // Obtener el producto seleccionado de la tabla
        Producto productoSeleccionado = tblAjuste.getSelectionModel().getSelectedItem();
        
        if (productoSeleccionado != null && controladorPadre != null) {
            // Llamar al método en agregarAjusteController para actualizar el nombre y el ID del producto
            controladorPadre.setProductoSeleccionado(productoSeleccionado.getNomProducto());
            controladorPadre.setProductoIDSeleccionado(productoSeleccionado.getIdProducto()); // Pasar el ID del producto
            
            // Cerrar la ventana de selección
            Stage stage = (Stage) tblAjuste.getScene().getWindow();
            stage.close();
        }
    }
    
    
    
    
    private agregarAjusteController controladorPadre;

    public void setControladorPadre(agregarAjusteController controladorPadre) {
        this.controladorPadre = controladorPadre;
    }
    
    
    

    @FXML
    private void quitarFiltro(ActionEvent event) {
        cargarProductos();
    }
}
