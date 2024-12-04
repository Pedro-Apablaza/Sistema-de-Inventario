package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.utils.ValidarRut;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

public class agregarUsuarioController {

    @FXML
    private TextField txtRut;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtApellido;

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContraseña;

    @FXML
    private TextField txtCorreo;

    @FXML
    private ComboBox<String> cmbRol;

    @FXML
    private Button btnAgregar;

    @FXML
    private Button btnCancelar;

    private ObservableList<String> listaRoles = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cargarRoles();
        btnAgregar.setOnAction(event -> agregarUsuario());
        btnCancelar.setOnAction(event -> limpiarCampos());
    }

    private void cargarRoles() {
        String query = "SELECT nombre FROM rol WHERE id_rol != 1";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet resultSet = stmt.executeQuery()) {

            while (resultSet.next()) {
                listaRoles.add(resultSet.getString("nombre"));
            }
            cmbRol.setItems(listaRoles);
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al cargar los roles.");
        }
    }

    @FXML
    private void agregarUsuario() {
        // Obtener los valores de los campos
        String rut = txtRut.getText().trim();
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String usuario = txtUsuario.getText().trim();
        String contraseña = txtContraseña.getText();
        String correo = txtCorreo.getText().trim();
        String rolSeleccionado = cmbRol.getValue();
    
        // Validar que ninguno de los campos esté vacío
        if (rut.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || usuario.isEmpty() ||
            contraseña.isEmpty() || correo.isEmpty() || rolSeleccionado == null) {
            Mensaje.mostrarMensaje("advertencia", "Campos vacíos", "Por favor, completa todos los campos.");
            return;
        }
    
        // Validar que el nombre y apellido solo contengan caracteres de la A-Z
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$")) {
            Mensaje.mostrarMensaje("advertencia", "Nombre no válido", "El nombre solo puede contener letras.");
            return;
        }
    
        if (!apellido.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$")) {
            Mensaje.mostrarMensaje("advertencia", "Apellido no válido", "El apellido solo puede contener letras.");
            return;
        }
    
        // Validar que el usuario solo contenga caracteres alfanuméricos
        if (!usuario.matches("[a-zA-Z0-9]+")) {
            Mensaje.mostrarMensaje("advertencia", "Usuario no válido", "El nombre de usuario solo puede contener caracteres alfanuméricos.");
            return;
        }
    
        // Validar el formato del correo electrónico
        if (!correoValido(correo)) {
            Mensaje.mostrarMensaje("advertencia", "Correo no válido", "El correo electrónico no tiene un formato válido.");
            return;
        }
    
        // Validar que el RUT sea válido
        if (!ValidarRut.validarRut(rut)) {
            Mensaje.mostrarMensaje("advertencia", "RUT inválido", "El RUT ingresado no es válido.");
            return;
        }
    
        // Verificar si el RUT ya existe
        String queryRut = "SELECT COUNT(*) AS conteo FROM usuario WHERE rut_usuario = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtRut = connection.prepareStatement(queryRut)) {
    
            stmtRut.setString(1, rut);
            ResultSet rsRut = stmtRut.executeQuery();
            if (rsRut.next() && rsRut.getInt("conteo") > 0) {
                Mensaje.mostrarMensaje("advertencia", "RUT duplicado", "El RUT ingresado ya está registrado.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al verificar la unicidad del RUT.");
            return;
        }
    
        // Verificar si el correo ya existe
        String queryCorreo = "SELECT COUNT(*) AS conteo FROM usuario WHERE correo = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtCorreo = connection.prepareStatement(queryCorreo)) {
    
            stmtCorreo.setString(1, correo);
            ResultSet rsCorreo = stmtCorreo.executeQuery();
            if (rsCorreo.next() && rsCorreo.getInt("conteo") > 0) {
                Mensaje.mostrarMensaje("advertencia", "Correo duplicado", "El correo ingresado ya está registrado.");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al verificar la unicidad del correo.");
            return;
        }
    
        // Hashear la contraseña usando BCrypt
        String contraseñaHasheada = BCrypt.hashpw(contraseña, BCrypt.gensalt());
    
        // Consultar el ID del rol seleccionado
        String queryRol = "SELECT id_rol FROM rol WHERE nombre = ?";
        String queryInsert = "INSERT INTO usuario (rut_usuario, nombre, apellido, nom_usuario, contraseña, correo, id_rol, soft_delete) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, 0)";
    
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtRol = connection.prepareStatement(queryRol);
             PreparedStatement stmtInsert = connection.prepareStatement(queryInsert)) {
    
            // Obtener el id del rol seleccionado
            stmtRol.setString(1, rolSeleccionado);
            ResultSet resultSet = stmtRol.executeQuery();
            if (!resultSet.next()) {
                Mensaje.mostrarMensaje("error", "Error", "El rol seleccionado no es válido.");
                return;
            }
            int idRol = resultSet.getInt("id_rol");
    
            // Insertar el nuevo usuario
            stmtInsert.setString(1, rut);
            stmtInsert.setString(2, nombre);
            stmtInsert.setString(3, apellido);
            stmtInsert.setString(4, usuario);
            stmtInsert.setString(5, contraseñaHasheada); // Guardar la contraseña hasheada
            stmtInsert.setString(6, correo);
            stmtInsert.setInt(7, idRol);
    
            int filasAfectadas = stmtInsert.executeUpdate();
            if (filasAfectadas > 0) {
                Mensaje.mostrarMensaje("informacion", "Éxito", "Usuario agregado exitosamente.");
                limpiarCampos();
                Stage stage = (Stage) txtNombre.getScene().getWindow(); // Usa un nodo existente
                stage.close();
            } else {
                Mensaje.mostrarMensaje("error", "Error", "No se pudo agregar el usuario.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al agregar el usuario.");
        }
    }
    
    // Método para validar el formato de correo electrónico
    private boolean correoValido(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
    

    private void limpiarCampos() {
        txtRut.clear();
        txtNombre.clear();
        txtApellido.clear();
        txtUsuario.clear();
        txtContraseña.clear();
        txtCorreo.clear();
        cmbRol.getSelectionModel().clearSelection();
    }
}
