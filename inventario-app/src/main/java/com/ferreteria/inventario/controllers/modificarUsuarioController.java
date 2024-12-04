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
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

public class modificarUsuarioController {

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
    private Button btnCancelar;

    private ObservableList<String> listaRoles = FXCollections.observableArrayList();

    // Suponemos que el controlador va a recibir un objeto Usuario o algún identificador del usuario a editar
    private String rutUsuarioEditar;

    @FXML
    public void initialize() {
        cargarRoles();
    }

    public void setUsuarioEditar(String rutUsuario) {
        this.rutUsuarioEditar = rutUsuario;
        cargarUsuario(rutUsuarioEditar);
    }

    private void cargarRoles() {
        String query = "SELECT nombre FROM rol WHERE id_rol != 1";  // Excluyendo el rol 'Superadmin'
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

    private void cargarUsuario(String rutUsuario) {
        String query = "SELECT rut_usuario, nombre, apellido, nom_usuario, contraseña, correo, id_rol FROM usuario WHERE rut_usuario = ?";
    
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {
    
            stmt.setString(1, rutUsuario);
            ResultSet resultSet = stmt.executeQuery();
    
            if (resultSet.next()) {
                txtRut.setText(resultSet.getString("rut_usuario"));
                txtNombre.setText(resultSet.getString("nombre"));
                txtApellido.setText(resultSet.getString("apellido"));
                txtUsuario.setText(resultSet.getString("nom_usuario"));
                txtContraseña.setText(resultSet.getString("contraseña"));
                txtCorreo.setText(resultSet.getString("correo"));
    
                // Obtener el ID del rol del usuario
                int rolId = resultSet.getInt("id_rol");
    
                // Seleccionar el rol en el ComboBox basándote en el id_rol
                for (String rol : listaRoles) {
                    if (rol.equals(getRolById(rolId))) {
                        cmbRol.getSelectionModel().select(rol);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al cargar los datos del usuario.");
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
    private void modificarUsuario() {
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
        if (!usuario.matches("[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ]+$")) {
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
        String queryRut = "SELECT COUNT(*) AS conteo FROM usuario WHERE rut_usuario = ? AND rut_usuario != ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtRut = connection.prepareStatement(queryRut)) {
    
            stmtRut.setString(1, rut);
            stmtRut.setString(2, this.rutUsuarioEditar);
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
        String queryCorreo = "SELECT COUNT(*) AS conteo FROM usuario WHERE correo = ? AND correo != ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmtCorreo = connection.prepareStatement(queryCorreo)) {
    
            stmtCorreo.setString(1, correo);
            stmtCorreo.setString(2, correo);
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

        if (!esHashValido(contraseña)) { 
            // Si no está hasheada, la hasheamos
            contraseña = BCrypt.hashpw(contraseña, BCrypt.gensalt());
        }

        // Consultar el ID del rol seleccionado
        String queryRol = "SELECT id_rol FROM rol WHERE nombre = ?";
        String queryUpdate = "UPDATE usuario SET nombre = ?, apellido = ?, nom_usuario = ?, contraseña = ?, correo = ?, id_rol = ? WHERE rut_usuario = ?";

        try (Connection connection = DatabaseConnection.getConnection();
            PreparedStatement stmtRol = connection.prepareStatement(queryRol);
            PreparedStatement stmtUpdate = connection.prepareStatement(queryUpdate)) {

            // Obtener el id del rol seleccionado
            stmtRol.setString(1, rolSeleccionado);
            ResultSet resultSet = stmtRol.executeQuery();
            if (!resultSet.next()) {
                Mensaje.mostrarMensaje("error", "Error", "El rol seleccionado no es válido.");
                return;
            }
            int idRol = resultSet.getInt("id_rol");

            // Actualizar el usuario
            stmtUpdate.setString(1, nombre);
            stmtUpdate.setString(2, apellido);
            stmtUpdate.setString(3, usuario);
            stmtUpdate.setString(4, contraseña);
            stmtUpdate.setString(5, correo);
            stmtUpdate.setInt(6, idRol);
            stmtUpdate.setString(7, rut);

            int filasAfectadas = stmtUpdate.executeUpdate();
            if (filasAfectadas > 0) {
                Mensaje.mostrarMensaje("informacion", "Éxito", "Usuario actualizado exitosamente.");
                limpiarCampos();
                Stage stage = (Stage) btnCancelar.getScene().getWindow();
                stage.close();
            } else {
                Mensaje.mostrarMensaje("error", "Error", "No se pudo actualizar el usuario.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "Hubo un error al actualizar el usuario.");
        }
    }

    /**
     * Verifica si una contraseña es un hash válido de bcrypt.
     * @param contraseña La contraseña a verificar.
     * @return true si la contraseña parece estar hasheada, false en caso contrario.
     */
    private boolean esHashValido(String contraseña) {
        // Verificar que la longitud sea de 60 caracteres (típico de bcrypt)
        if (contraseña.length() != 60) {
            return false;
        }
        // Verificar el formato típico de bcrypt (prefijo $2b$, $2a$, etc.)
        return contraseña.matches("^\\$2[aby]\\$.{56}$");
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

    @FXML
    private void cancelar(){
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
