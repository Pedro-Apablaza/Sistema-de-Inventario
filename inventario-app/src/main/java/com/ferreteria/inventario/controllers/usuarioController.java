package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.Usuario;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.utils.stageManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class usuarioController {

    @FXML
    private TableView<Usuario> tablaUsuarios;

    @FXML
    private TableColumn<Usuario, String> columnaUsuario;

    @FXML
    private TableColumn<Usuario, String> columnaRutUsuario;

    @FXML
    private TableColumn<Usuario, String> columnaNombre;

    @FXML
    private TableColumn<Usuario, String> columnaApellido;

    @FXML
    private TableColumn<Usuario, String> columnaContraseña;

    @FXML
    private TableColumn<Usuario, String> columnaRol;

    @FXML
    private TableColumn<Usuario, String> columnaCorreo;

    @FXML
    private TextField txtUsuario;

    @FXML
    private TextField txtRut;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtRol;

    @FXML
    private ObservableList<Usuario> listaUsuarios = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cargarUsuarios();
        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                llenarCamposConDatos(newValue);
            }
        });
    }

    @FXML
    private void cargarUsuarios() {
        listaUsuarios.clear();  // Limpiar la lista antes de cargar los nuevos datos
        String query = "SELECT rut_usuario, nombre, apellido, nom_usuario, contraseña, correo, id_rol FROM usuario WHERE soft_delete = 0";
        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet resultSet = stmt.executeQuery(query)) {
    
            // Configurar las columnas de la tabla
            columnaRutUsuario.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRutUsuario()));
            columnaUsuario.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomUsuario())); // Configurar la columna Usuario
            columnaNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
            columnaApellido.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getApellido()));
            columnaContraseña.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getContraseña()));
            columnaRol.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getIdRol())));
            columnaCorreo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCorreo()));
    
            // Llenar la lista con los datos de la base de datos
            while (resultSet.next()) {
                String rutUsuario = resultSet.getString("rut_usuario");
                String nombre = resultSet.getString("nombre");
                String apellido = resultSet.getString("apellido");
                String nomUsuario = resultSet.getString("nom_usuario");  // Campo 'usuario'
                String contraseña = resultSet.getString("contraseña");
                String correo = resultSet.getString("correo");
                int idRol = resultSet.getInt("id_rol");
    
                // Crear el objeto Usuario y agregarlo a la lista observable
                Usuario usuario = new Usuario(rutUsuario, nombre, apellido, nomUsuario, contraseña, correo, idRol, false);
                listaUsuarios.add(usuario);
            }
    
            // Asignar la lista observable a la tabla
            tablaUsuarios.setItems(listaUsuarios);
    
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al conectar con la base de datos.");
        }
    }

    private void llenarCamposConDatos(Usuario usuario) {
        txtUsuario.setText(usuario.getNomUsuario());
        txtRut.setText(usuario.getRutUsuario());
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtCorreo.setText(usuario.getCorreo());
        txtRol.setText(String.valueOf(usuario.getIdRol()));
    }

    @FXML
    private void agregarUsuarioView(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Administrador/Opciones/usuario/agregarUsuarioView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Agregar Usuario");
            stage.setScene(new Scene(root));
            stage.show();
            stageManager.addStage(stage);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void modificarUsuarioView() {
        // Obtener el usuario seleccionado de la tabla
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuarioSeleccionado == null) {
            Mensaje.mostrarMensaje("error", "Error", "Debe seleccionar un usuario para modificar.");
            return;
        }
    
        // Verificar si el usuario seleccionado tiene el rol de "Superadmin"
        int rolId = usuarioSeleccionado.getIdRol(); // Obtener el ID del rol del usuario
        String rolNombre = getRolById(rolId); // Obtener el nombre del rol a partir del ID
    
        if ("SuperAdmin".equals(rolNombre)) {
            Mensaje.mostrarMensaje("error", "Error", "No se puede modificar un usuario con el rol de Superadmin.");
            return;  // No permitir la modificación si el usuario tiene el rol de "Superadmin"
        }
    
        try {
            // Cargar la vista de modificación de usuario
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Administrador/Opciones/usuario/modificarUsuarioView.fxml"));
            Parent root = loader.load();
    
            // Obtener el controlador de la vista de modificación de usuario
            modificarUsuarioController controller = loader.getController();
    
            // Pasar el usuario seleccionado al controlador de la vista de modificación
            controller.setUsuarioEditar(usuarioSeleccionado.getRutUsuario());
    
            // Crear una nueva ventana (Stage) para mostrar la vista de modificación
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Modificar Usuario");
            stage.show();
    
            // Registrar la ventana en el stageManager
            stageManager.addStage(stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    


    @FXML
    private void eliminarUsuario() {
        // Obtener el usuario seleccionado en la tabla
        Usuario usuarioSeleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (usuarioSeleccionado == null) {
            Mensaje.mostrarMensaje("advertencia", "Advertencia", "Por favor, selecciona un usuario para eliminar.");
            return;
        }

        String rutUsuario = usuarioSeleccionado.getRutUsuario(); // Asegúrate de que la clase Usuario tiene este método

        // Validar si el usuario tiene el rol "Superadmin"
        String queryRol = "SELECT r.nombre FROM usuario u JOIN rol r ON u.id_rol = r.id_rol WHERE u.rut_usuario = ?";
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement stmtRol = connection.prepareStatement(queryRol)) {

            stmtRol.setString(1, rutUsuario);
            ResultSet rs = stmtRol.executeQuery();

            if (rs.next()) {
                String nombreRol = rs.getString("nombre");

                if ("Superadmin".equalsIgnoreCase(nombreRol)) {
                    Mensaje.mostrarMensaje("advertencia", "Advertencia", "No puedes eliminar al usuario con rol Superadmin.");
                    return;
                }
            } else {
                Mensaje.mostrarMensaje("error", "Error", "No se encontró información del rol para el usuario seleccionado.");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al validar el rol del usuario.");
            return;
        }

        // Confirmación antes de proceder con la eliminación
        if (!Mensaje.mostrarConfirmacion("Confirmación", "¿Estás seguro de que deseas eliminar este usuario?")) {
            return; // El usuario canceló la operación
        }

        // Consulta SQL para eliminación lógica
        String queryEliminar = "UPDATE usuario SET soft_delete = 1 WHERE rut_usuario = ?";

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement stmtEliminar = connection.prepareStatement(queryEliminar)) {

            stmtEliminar.setString(1, rutUsuario);

            int filasAfectadas = stmtEliminar.executeUpdate();
            if (filasAfectadas > 0) {
                Mensaje.mostrarMensaje("informacion", "Éxito", "Usuario eliminado correctamente.");
                cargarUsuarios(); // Recargar la tabla con los datos actualizados
            } else {
                Mensaje.mostrarMensaje("error", "Error", "No se pudo eliminar el usuario.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al eliminar el usuario.");
        }
    }


    private String getRolById(int idRol) {
        String rolNombre = null;
        String query = "SELECT nombre FROM rol WHERE id_rol = ?";
        
        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement stmt = connection.prepareStatement(query)) {
            
            stmt.setInt(1, idRol);
            ResultSet resultSet = stmt.executeQuery();
            
            if (resultSet.next()) {
                rolNombre = resultSet.getString("nombre");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return rolNombre != null ? rolNombre : "Empleado"; 
    }


    @FXML
    private void actualizar(){
        cargarUsuarios();
    }
}
