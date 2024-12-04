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
import com.ferreteria.inventario.utils.Mensaje;

public class agregarProductoController {

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


 

    @FXML
    public void agregarProducto() {
        // Obtener los valores de los campos de texto
        String nombre = txtNombre.getText();
        String precioText = txtPrecio.getText();
        String cantidadText = txtCantidad.getText();
        String stockText = txtStock.getText();
        
        // Validar si algún campo está vacío
        if (nombre.isEmpty() || precioText.isEmpty() || cantidadText.isEmpty() || stockText.isEmpty()) {
            Mensaje.mostrarMensaje("error", "Error de validación", "Todos los campos deben ser rellenados.");
            return;
        }

        // Validar si el precio es un número válido
        double precio;
        try {
            precio = Double.parseDouble(precioText);
        } catch (NumberFormatException e) {
            Mensaje.mostrarMensaje("error", "Error de entrada", "El precio debe ser un número válido.");
            return;
        }

        // Validar si la cantidad es un número válido
        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadText);
        } catch (NumberFormatException e) {
            Mensaje.mostrarMensaje("error", "Error de entrada", "La cantidad debe ser un número válido.");
            return;
        }

        // Validar si el stock crítico es un número válido
        int stockCritico;
        try {
            stockCritico = Integer.parseInt(stockText);
        } catch (NumberFormatException e) {
            Mensaje.mostrarMensaje("error", "Error de entrada", "El stock crítico debe ser un número válido.");
            return;
        }

        // Validar que el precio sea mayor que 0
        if (precio <= 0) {
            Mensaje.mostrarMensaje("error", "Error de precio", "El precio debe ser mayor a 0.");
            return;
        }

        // Validar que la cantidad sea mayor que 0
        if (cantidad <= 0) {
            Mensaje.mostrarMensaje("error", "Error de cantidad", "La cantidad debe ser mayor a 0.");
            return;
        }

        // Validar que el stock crítico sea mayor que 0
        if (stockCritico <= 0) {
            Mensaje.mostrarMensaje("error", "Error de stock crítico", "El stock crítico debe ser mayor a 0.");
            return;
        }

        // Obtener la categoría seleccionada
        Categoria categoriaSeleccionada = cmbCategoria.getSelectionModel().getSelectedItem();
        if (categoriaSeleccionada == null) {
            Mensaje.mostrarMensaje("error", "Error de categoría", "Debe seleccionar una categoría.");
            return;
        }

        int idCategoria = categoriaSeleccionada.getIdCategoria();

        // Conectar con la base de datos
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                Mensaje.mostrarMensaje("error", "Error de conexión", "No se pudo establecer conexión con la base de datos.");
                return;
            }

            // Verificar si ya existe un producto con el mismo nombre
            String checkQuery = "SELECT COUNT(*) FROM producto WHERE nom_producto = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, nombre);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    Mensaje.mostrarMensaje("error", "Error de duplicado", "Ya existe un producto con ese nombre.");
                    return;
                }
            }

            // Insertar el producto en la base de datos
            String insertQuery = "INSERT INTO producto (nom_producto, precio, cantidad, stock_critico, id_categoria) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, nombre);
                insertStmt.setDouble(2, precio);
                insertStmt.setInt(3, cantidad);
                insertStmt.setInt(4, stockCritico);
                insertStmt.setInt(5, idCategoria); // Aquí se usa el ID de la categoría seleccionada

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    Mensaje.mostrarMensaje("informacion", "Producto agregado", "El producto se agregó correctamente.");
                    Stage stage = (Stage) txtNombre.getScene().getWindow();
                    stage.close();
                } else {
                    Mensaje.mostrarMensaje("error", "Error al agregar", "No se pudo agregar el producto.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error SQL", "Hubo un problema al insertar el producto.");
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
