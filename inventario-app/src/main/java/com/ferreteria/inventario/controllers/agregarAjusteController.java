package com.ferreteria.inventario.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.utils.SessionManager;
import com.ferreteria.inventario.utils.stageManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class agregarAjusteController {
    @FXML
    private TextField txtProducto;
    @FXML
    private TextField txtCantidad;
    @FXML
    private TextField txtTipo;
    @FXML
    private TextArea txtMotivo;

    private Long productoIDSeleccionado;  // ID del producto seleccionado

    // Este método se invoca para cargar la ventana de selección de producto
    @FXML
    public void cargarProducto() {
        try {
            // Cargar la vista
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Bodega/Opciones/stock/cargarProducto.fxml"));
            VBox root = loader.load();
            
            // Obtener el controlador de la vista cargada
            cargarProductoController controladorProducto = loader.getController();
            
            // Crear una nueva ventana (Stage)
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Seleccione un producto");
    
            // Pasar el controlador de la vista principal (agregarAjusteController) al controlador de la nueva vista
            controladorProducto.setControladorPadre(this);  // Aquí pasas el controlador actual (agregarAjusteController)
            
            // Mostrar la ventana
            stage.show();
            
            // Registrar la ventana abierta
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para establecer el ID del producto seleccionado desde otro controlador
    public void setProductoIDSeleccionado(Long productoID) {
        this.productoIDSeleccionado = productoID;  // Establecer el ID del producto
    }
    
    // Método para establecer el nombre del producto en el campo de texto
    public void setProductoSeleccionado(String nombreProducto) {
        txtProducto.setText(nombreProducto);
    }

public void agregarAjuste() {
    // Validar que se haya seleccionado un producto
    if (productoIDSeleccionado == null) {
        Mensaje.mostrarMensaje("error", "Producto no seleccionado", "Debe seleccionar un producto antes de realizar el ajuste.");
        return;
    }

    // Obtener los valores ingresados
    String cantidadText = txtCantidad.getText();
    String tipo = txtTipo.getText();
    String motivo = txtMotivo.getText();
    
    // Validar que todos los campos estén rellenados
    if (cantidadText.isEmpty() || tipo.isEmpty() || motivo.isEmpty()) {
        Mensaje.mostrarMensaje("error", "Campos vacíos", "Todos los campos deben ser rellenados.");
        return;
    }

    // Validar que la cantidad sea un número válido (positivo o negativo)
    if (!isNumeric(cantidadText)) {
        Mensaje.mostrarMensaje("error", "Cantidad no válida", "La cantidad debe ser un valor numérico (positivo o negativo).");
        return;
    }
    int cantidad = Integer.parseInt(cantidadText);

    // Validar que el tipo solo contenga letras (con acentos y ñ)
    if (!isValidTipo(tipo)) {
        Mensaje.mostrarMensaje("error", "Tipo no válido", "El tipo solo debe contener caracteres de la A-Z (incluyendo tildes y ñ).");
        return;
    }

    // Validar que la cantidad ajustada no sea mayor al stock disponible si es un ajuste negativo
    int cantidadActual = obtenerCantidadProducto(productoIDSeleccionado);
    if (tipo.equalsIgnoreCase("salida") && cantidad > cantidadActual) {
        Mensaje.mostrarMensaje("error", "Cantidad de ajuste inválida", "No puede quitar más productos de los que hay en el stock.");
        return;
    }

    // Obtener el rut del usuario y la fecha actual
    String rutUsuario = SessionManager.getInstance().getRutUsuario();
    LocalDateTime fecha = LocalDateTime.now();

    try (Connection conn = DatabaseConnection.getConnection()) {
        // Registrar el ajuste en la tabla ajuste_stock
        String sql = "INSERT INTO ajuste_stock (id_producto, rut_usuario, cantidad, motivo, fecha, tipo) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setLong(1, productoIDSeleccionado);
        stmt.setString(2, rutUsuario);
        stmt.setInt(3, cantidad);
        stmt.setString(4, motivo);
        stmt.setTimestamp(5, Timestamp.valueOf(fecha));
        stmt.setString(6, tipo);
        stmt.executeUpdate();

        // Actualizar la cantidad del producto ajustado en la tabla producto
        actualizarCantidadProducto(productoIDSeleccionado, cantidad);

        Mensaje.mostrarMensaje("informacion", "Ajuste agregado", "Se registró con éxito.");

        // Llamar al método del controlador padre para actualizar la tabla
        if (controladorPadre != null) {
            controladorPadre.cargarDatos();
        }

        // Cerrar la ventana de ajuste
        Stage stage = (Stage) txtProducto.getScene().getWindow();
        stage.close();

    } catch (SQLException e) {
        e.printStackTrace();
        Mensaje.mostrarMensaje("error", "Error al agregar el ajuste", "No se pudo registrar el ajuste.");
    }
}

// Método para verificar si la cadena es un número (positivo o negativo)
private boolean isNumeric(String str) {
    try {
        Integer.parseInt(str); // Intentar convertir la cadena a un número
        return true; // Si no lanza excepción, es un número válido
    } catch (NumberFormatException e) {
        return false; // No es un número válido
    }
}

// Método para validar el tipo (solo caracteres alfabéticos, incluidas tildes y ñ)
private boolean isValidTipo(String tipo) {
    // Expresión regular que permite letras a-z, tildes y ñ
    String regex = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+$";
    return tipo.matches(regex);
}

    
    // Método para obtener la cantidad actual de un producto
    private int obtenerCantidadProducto(long idProducto) {
        int cantidad = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT cantidad FROM producto WHERE id_producto = ? AND soft_delete = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, idProducto);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                cantidad = rs.getInt("cantidad");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cantidad;
    }
    
    // Método para actualizar la cantidad del producto
    private void actualizarCantidadProducto(long idProducto, int cantidad) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE producto SET cantidad = cantidad + ? WHERE id_producto = ? AND soft_delete = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, cantidad);
            stmt.setLong(2, idProducto);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ajusteController controladorPadre;

    public void setControladorPadre(ajusteController controladorPadre) {
        this.controladorPadre = controladorPadre;
    }
}
