package com.ferreteria.inventario.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.Producto;
import com.ferreteria.inventario.utils.stageManager;
import com.ferreteria.inventario.utils.Mensaje;

public class productoController {

    @FXML
    private TextField txt_nombre_producto;
    @FXML
    private TextField txt_precio_producto;
    @FXML
    private TextField txt_categoria_producto;
    @FXML
    private TextField txt_id_producto;
    @FXML
    private TextField txt_cantidad_producto;

    @FXML
    private TableView<Producto> tablaProductos;
    @FXML
    private TableColumn<Producto, Long> columnaIdProducto; 
    @FXML
    private TableColumn<Producto, String> columnaNomProducto;
    @FXML
    private TableColumn<Producto, Integer> columnaPrecio;
    @FXML
    private TableColumn<Producto, Integer> columnaCantidad;
    @FXML
    private TableColumn<Producto, Integer> columnaStockCritico; // Nueva columna
    @FXML
    private TableColumn<Producto, String> columnaNomCategoria; // Mostrar el nombre de la categoría

    private ObservableList<Producto> listaProductos;

    @FXML
    public void initialize() {
        listaProductos = FXCollections.observableArrayList(); // Inicializar la lista observable
        configurarColumnas();
        cargarDatos();
    
        // Agregar Listener a la selección de la tabla
        tablaProductos.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mostrarDatosProducto(newValue);
            }
        });
    }

    private void configurarColumnas() {
        columnaIdProducto.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        columnaNomProducto.setCellValueFactory(new PropertyValueFactory<>("nomProducto"));
        columnaPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        columnaStockCritico.setCellValueFactory(new PropertyValueFactory<>("stockCritico"));
        columnaNomCategoria.setCellValueFactory(new PropertyValueFactory<>("nomCategoria"));
    }

    @FXML
    public void cargarDatos() {
        listaProductos.clear();
        try (Connection connection = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.id_producto, p.nom_producto, p.precio, p.cantidad, p.stock_critico, c.nom_categoria " +
                         "FROM producto p " +
                         "JOIN categoria c ON p.id_categoria = c.id_categoria " +
                         "WHERE p.soft_delete = 0";

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            // Recorrer el ResultSet y agregar cada producto a la lista observable
            while (resultSet.next()) {
                Long idProducto = resultSet.getLong("id_producto");
                String nomProducto = resultSet.getString("nom_producto");
                int precio = resultSet.getInt("precio");
                int cantidad = resultSet.getInt("cantidad");
                int stockCritico = resultSet.getInt("stock_critico");
                String nomCategoria = resultSet.getString("nom_categoria");

                // Crear el objeto Producto
                Producto producto = new Producto(idProducto, nomProducto, precio, cantidad, stockCritico, nomCategoria);
                listaProductos.add(producto);
            }

            tablaProductos.setItems(listaProductos);
        } catch (SQLException e) {
            System.out.println("Error al cargar los datos: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @FXML
    private void agregarProductoView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Administrador/Opciones/producto/agregarProductoView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Agregar Producto");
            stage.setScene(new Scene(root));
            stage.show();
            stageManager.addStage(stage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        

    @FXML
    public void modificarProductoView() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (productoSeleccionado == null) {
            Mensaje.mostrarMensaje("error", "Error", "Debe seleccionar un producto para modificar.");
            return;
        }
    
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/administrador/Opciones/producto/modificarProductoView.fxml"));
            Parent root = loader.load();
    
            // Obtener el controlador de la vista de modificación
            modificarProductoController controller = loader.getController();
    
            // Pasar los datos del producto seleccionado al controlador
            controller.cargarDatosProducto(productoSeleccionado);
    
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modificar Producto");
            stage.show();
    
            // Registrar la ventana
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    @FXML
    private void eliminarProducto() {
        Producto productoSeleccionado = tablaProductos.getSelectionModel().getSelectedItem();

        if (productoSeleccionado == null) {
            Mensaje.mostrarMensaje("error", "Operación fallida", "Por favor seleccione un producto para eliminar.");
            return;
        }

        // Crear la ventana de confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación de Eliminación");
        alert.setHeaderText("Está a punto de eliminar un producto");
        alert.setContentText("¿Está seguro de que desea eliminar el producto: " + productoSeleccionado.getNomProducto() + "?");

        // Mostrar la ventana y esperar respuesta
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Long IdProductoValue = productoSeleccionado.getIdProducto();
                eliminacion(IdProductoValue);
            } else {
                Mensaje.mostrarMensaje("informacion", "Operación exitosa" ,"La eliminación ha sido cancelada.");
            }
        });
    }

    private void eliminacion(Long IdProducto) {
        String sql = "UPDATE producto SET soft_delete = 1 WHERE id_producto = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, IdProducto);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                Mensaje.mostrarMensaje("informacion", "Exito","Producto eliminado exitosamente.");
                cargarDatos();
            } else {
                Mensaje.mostrarMensaje("informacion", "Error","No se pudo eliminar el producto o no se encontró.");
            }
        } catch (SQLException e) {
            Mensaje.mostrarMensaje("error", "Error de SQL", e.getMessage());
        }
    }

    @FXML
    private void actualizarProducto() {
        cargarDatos();
    }



    private void mostrarDatosProducto(Producto producto) {
        txt_nombre_producto.setText(producto.getNomProducto());
        txt_precio_producto.setText(String.valueOf(producto.getPrecio()));
        txt_categoria_producto.setText(producto.getNomCategoria());
        txt_id_producto.setText(String.valueOf(producto.getIdProducto()));
        txt_cantidad_producto.setText(String.valueOf(producto.getCantidad()));
    }

}
