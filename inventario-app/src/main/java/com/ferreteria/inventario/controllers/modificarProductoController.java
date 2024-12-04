package com.ferreteria.inventario.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.Categoria;
import com.ferreteria.inventario.models.Producto;
import com.ferreteria.inventario.utils.Mensaje;

public class modificarProductoController {

    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtStock;
    @FXML
    private ComboBox<Categoria> cmbCategoria;

    /**
     * Método llamado al inicializar el controlador para cargar las categorías
     * en el ComboBox desde la base de datos.
     */
    @FXML
    public void initialize() {
        cargarCategorias();
    }

    private void cargarCategorias() {
        // Limpiar el ComboBox antes de cargar los elementos
        cmbCategoria.getItems().clear();
    
        // Conectar con la base de datos
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                Mensaje.mostrarMensaje("error", "Error de conexión", "No se pudo establecer conexión con la base de datos.");
                return;
            }
    
            // Consulta SQL para obtener las categorías
            String query = "SELECT id_categoria, nom_categoria FROM categoria WHERE nom_categoria != 'Sin categoría'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int idCategoria = rs.getInt("id_categoria");
                    String nombreCategoria = rs.getString("nom_categoria");
    
                    // Crear una nueva instancia de Categoria y agregarla al ComboBox
                    cmbCategoria.getItems().add(new Categoria(idCategoria, nombreCategoria, false));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error al cargar categorías", "Hubo un problema al cargar las categorías desde la base de datos.");
        }
    }
    

    private Long idProductoActual;
    public void cargarDatosProducto(Producto producto) {
        this.idProductoActual = producto.getIdProducto();
        txtNombre.setText(producto.getNomProducto());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));
        txtCantidad.setText(String.valueOf(producto.getCantidad()));
        txtStock.setText(String.valueOf(producto.getStockCritico()));

        // Buscar y seleccionar la categoría correspondiente en el ComboBox
        cmbCategoria.getItems().stream()
            .filter(categoria -> categoria.getNomCategoria().equals(producto.getNomCategoria()))
            .findFirst()
            .ifPresent(cmbCategoria.getSelectionModel()::select);
    }


 
    @FXML
    public void modificarProducto() {
        // Obtener los valores de los campos de texto
        String nombre = txtNombre.getText();
        double precio;
        int cantidad;
        int stockCritico;

        try {
            // Convertir los valores a los tipos adecuados
            precio = Double.parseDouble(txtPrecio.getText());
            cantidad = Integer.parseInt(txtCantidad.getText());
            stockCritico = Integer.parseInt(txtStock.getText());
        } catch (NumberFormatException e) {
            Mensaje.mostrarMensaje("error", "Error de entrada", "Precio, cantidad y stock deben ser números válidos.");
            return;
        }

        // Obtener la categoría seleccionada
        Categoria categoriaSeleccionada = cmbCategoria.getSelectionModel().getSelectedItem();
        if (categoriaSeleccionada == null) {
            Mensaje.mostrarMensaje("error", "Error de categoría", "Debe seleccionar una categoría.");
            return;
        }

        int idCategoria = categoriaSeleccionada.getIdCategoria();

        // Validación de los campos
        if (nombre == null || nombre.isEmpty()) {
            Mensaje.mostrarMensaje("error", "Error de producto", "El nombre del producto no puede estar vacío.");
            return;
        }
        if (precio <= 0) {
            Mensaje.mostrarMensaje("error", "Error de precio", "El precio debe ser mayor a 0.");
            return;
        }
        if (cantidad <= 0) {
            Mensaje.mostrarMensaje("error", "Error de cantidad", "La cantidad debe ser mayor a 0.");
            return;
        }
        if (stockCritico <= 0) {
            Mensaje.mostrarMensaje("error", "Error de stock crítico", "El stock crítico debe ser mayor a 0.");
            return;
        }

        // Conectar con la base de datos
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                Mensaje.mostrarMensaje("error", "Error de conexión", "No se pudo establecer conexión con la base de datos.");
                return;
            }

            // Verificar si ya existe un producto con el mismo nombre y que no sea el producto actual
            String checkQuery = "SELECT COUNT(*) FROM producto WHERE nom_producto = ? AND id_producto != ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, nombre);
                checkStmt.setLong(2, idProductoActual); // Aseguramos que no se compare con el producto actual
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    Mensaje.mostrarMensaje("error", "Error de duplicado", "Ya existe un producto con ese nombre.");
                    return;
                }
            }

            // Actualizar el producto en la base de datos
            String updateQuery = "UPDATE producto SET nom_producto = ?, precio = ?, cantidad = ?, stock_critico = ?, id_categoria = ? WHERE id_producto = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setString(1, nombre);
                updateStmt.setDouble(2, precio);
                updateStmt.setInt(3, cantidad);
                updateStmt.setInt(4, stockCritico);
                updateStmt.setInt(5, idCategoria);
                updateStmt.setLong(6, idProductoActual); // Usamos el ID del producto que estamos modificando

                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected > 0) {
                    Mensaje.mostrarMensaje("informacion", "Producto modificado", "El producto se modificó correctamente.");
                    Stage stage = (Stage) txtNombre.getScene().getWindow();
                    stage.close();
                } else {
                    Mensaje.mostrarMensaje("error", "Error al modificar", "No se pudo modificar el producto.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error SQL", "Hubo un problema al actualizar el producto.");
        }
    }


    

    @FXML
    private void cancelar() {
        // Obtener el Stage (ventana actual) desde cualquier control
        Stage stage = (Stage) txtNombre.getScene().getWindow(); // Puedes usar cualquier control que esté en la ventana
        stage.close();  // Cierra la ventana actual

        // Limpiar los campos de entrada
        txtNombre.clear();
        txtPrecio.clear();
        txtCantidad.clear();
        txtStock.clear();
        cmbCategoria.getSelectionModel().clearSelection();
    }
}
