package com.ferreteria.inventario.controllers;

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
import javafx.stage.Stage;
import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.Categoria;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.utils.stageManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class categoriaController {

    @FXML
    private TableView<Categoria> tblCategorias;
    @FXML
    private TableColumn<Categoria, Integer> colIdCategoria;
    @FXML
    private TableColumn<Categoria, String> colCategoria;
    @FXML
    private TextField txtIdCategoria;
    @FXML
    private TextField txtCategoria;

    // Lista observable para la tabla
    private ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
        cargarCategorias();
    }

    // Configuración de la tabla
    private void configurarTabla() {
        // Asignar la celda de la tabla para la columna 'ID Categoría'
        colIdCategoria.setCellValueFactory(cellData -> cellData.getValue().idCategoriaProperty().asObject());
        
        // Asignar la celda de la tabla para la columna 'Categoría'
        colCategoria.setCellValueFactory(cellData -> cellData.getValue().nomCategoriaProperty());
        
        // Detectar la fila seleccionada y rellenar los campos
        tblCategorias.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> rellenarCampos(newValue)
        );
    }


    // Cargar las categorías desde la base de datos
    public void cargarCategorias() {
        // Limpiar la lista
        listaCategorias.clear();
    
        // Conectar con la base de datos
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) {
                Mensaje.mostrarMensaje("error", "Error de conexión", "No se pudo establecer conexión con la base de datos.");
                return;
            }
    
            String query = "SELECT id_categoria, nom_categoria, soft_delete FROM categoria WHERE soft_delete = 0 AND nom_categoria != 'Sin categoria'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int idCategoria = rs.getInt("id_categoria");
                    String nombreCategoria = rs.getString("nom_categoria");
                    boolean softDelete = rs.getBoolean("soft_delete");
    
                    // Crear una nueva instancia de Categoria y agregarla a la lista
                    listaCategorias.add(new Categoria(idCategoria, nombreCategoria, softDelete));
                }
    
                // Asignar la lista a la tabla
                tblCategorias.setItems(listaCategorias);
    
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error al cargar categorías", "Hubo un problema al cargar las categorías desde la base de datos.");
        }
    }

    // Rellenar los campos cuando se selecciona una categoría de la tabla
    private void rellenarCampos(Categoria categoria) {
        if (categoria != null) {
            txtIdCategoria.setText(String.valueOf(categoria.getIdCategoria()));
            txtCategoria.setText(categoria.getNomCategoria());
        }
    }
    @FXML
    private void agregarCategoriaView(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Administrador/Opciones/categoria/agregarCategoriaView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Agregar Categoria");
            stage.setScene(new Scene(root));
            stage.show();
            stageManager.addStage(stage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void modificarCategoriaView() {
        // Obtener la categoría seleccionada de la tabla
        Categoria categoriaSeleccionada = tblCategorias.getSelectionModel().getSelectedItem();
        if (categoriaSeleccionada == null) {
            Mensaje.mostrarMensaje("error", "Error", "Debe seleccionar una categoría para modificar.");
            return;
        }
    
        try {
            // Cargar la vista de modificación de categoría
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Administrador/Opciones/categoria/modificarCategoriaView.fxml"));
            Parent root = loader.load();
    
            // Obtener el controlador de la vista de modificación de categoría
            modificarCategoriaController controller = loader.getController();
    
            // Pasar la categoría seleccionada al controlador de la vista de modificación
            controller.initData(categoriaSeleccionada);
    
            // Crear una nueva ventana (Stage) para mostrar la vista de modificación
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modificar Categoría");
            stage.show();
    
            // Registrar la ventana en el stageManager
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void eliminarCategoria() {
        // Mostrar un cuadro de diálogo de confirmación usando tu método mostrarConfirmacion
        boolean confirmacion = Mensaje.mostrarConfirmacion("Confirmación de eliminación", 
                "¿Estás seguro de que deseas eliminar esta categoría?.");
    
        // Si el usuario selecciona "Aceptar", se procede con la eliminación
        if (confirmacion) {
            int idCategoria;
            try {
                idCategoria = Integer.parseInt(txtIdCategoria.getText());
            } catch (NumberFormatException e) {
                Mensaje.mostrarMensaje("error", "Error de entrada", "El ID de la categoría no es válido.");
                return;
            }
    
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (conn == null) {
                    Mensaje.mostrarMensaje("error", "Error de conexión", "No se pudo establecer conexión con la base de datos.");
                    return;
                }
    
                // Paso 1: Actualizar los productos para que tengan la categoría "Sin categoría" (ID 1)
                String updateProductosQuery = "UPDATE producto SET id_categoria = 1 WHERE id_categoria = ? OR id_categoria IS NULL";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateProductosQuery)) {
                    updateStmt.setInt(1, idCategoria); // ID de la categoría que se está eliminando
                    updateStmt.executeUpdate();
                }
    
                // Paso 2: Realizar el soft delete de la categoría
                String deleteQuery = "UPDATE categoria SET soft_delete = 1 WHERE id_categoria = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                    deleteStmt.setInt(1, idCategoria);
                    int rowsAffected = deleteStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        Mensaje.mostrarMensaje("informacion", "Categoría eliminada", "La categoría se eliminó correctamente.");
                        cargarCategorias(); // Recargar la tabla de categorías
                    } else {
                        Mensaje.mostrarMensaje("error", "Error al eliminar", "No se pudo eliminar la categoría.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Mensaje.mostrarMensaje("error", "Error SQL", "Hubo un problema al procesar la solicitud.");
            }
        } else {
            // Si el usuario cancela, no hacer nada
            Mensaje.mostrarMensaje("informacion", "Eliminación cancelada", "La categoría no fue eliminada.");
        }
    }
    
    

    @FXML
    private void actualizarCategoria(){
        cargarCategorias();
    }

}
