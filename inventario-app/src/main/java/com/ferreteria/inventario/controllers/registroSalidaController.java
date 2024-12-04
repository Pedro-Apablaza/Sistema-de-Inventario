package com.ferreteria.inventario.controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.Registro;
import com.ferreteria.inventario.utils.Mensaje;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class registroSalidaController {

    @FXML
    private TableView<Registro> tblSalida;

    @FXML
    private TableColumn<Registro, Integer> colID;
    @FXML
    private TableColumn<Registro, String> colNombre;
    @FXML
    private TableColumn<Registro, Integer> colCantidad;
    @FXML
    private TableColumn<Registro, String> colUsuario;
    @FXML
    private TableColumn<Registro, String> colFecha;
    @FXML
    private TableColumn<Registro, String> colMotivo;
    @FXML
    private TableColumn<Registro, String> colTipo;

    @FXML
    private void cargarEntradas() {
        ObservableList<Registro> listaEntradas = FXCollections.observableArrayList();

        try (Connection connection = DatabaseConnection.getConnection()) {
            String query =
            "SELECT " +
            "    dv.id_venta AS id, " +
            "    p.nom_producto AS nombre, " +
            "    dv.cantidad, " +
            "    v.rut_usuario AS usuario, " +
            "    v.fecha, " +
            "    'Venta registrada' AS motivo, " +
            "    'Venta' AS tipo " +
            "FROM " +
            "    detalle_venta dv " +
            "JOIN " +
            "    producto p ON dv.id_producto = p.id_producto " +
            "JOIN " +
            "    venta v ON dv.id_venta = v.id_venta " +
            "WHERE " +
            "    v.soft_delete = 0 " +
            "UNION ALL " +
            "SELECT " +
            "    a.id_ajuste AS id, " +
            "    p.nom_producto AS nombre, " +
            "    a.cantidad, " +
            "    a.rut_usuario AS usuario, " +
            "    a.fecha, " +
            "    a.motivo, " +
            "    'Ajuste' AS tipo " +
            "FROM " +
            "    ajuste_stock a " +
            "JOIN " +
            "    producto p ON a.id_producto = p.id_producto " +
            "WHERE " +
            "    a.cantidad < 0 " +
            "ORDER BY fecha DESC;";

            PreparedStatement pst = connection.prepareStatement(query);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                int cantidad = rs.getInt("cantidad");
                String usuario = rs.getString("usuario");
                java.sql.Date sqlDate = rs.getDate("fecha");
                java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
                String motivo = rs.getString("motivo");
                String tipo = rs.getString("tipo");

                listaEntradas.add(new Registro(id, nombre, cantidad, usuario, utilDate, motivo, tipo));
            }

            tblSalida.setItems(listaEntradas);

            // Depuración: Verificar los datos cargados
            System.out.println("Entradas cargadas: " + listaEntradas.size());
        } catch (SQLException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "Error al cargar entradas", "Ocurrió un error al cargar las entradas.");
        }
    }

    @FXML
    private void initialize() {
        // Configurar las columnas de la tabla
        colID.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));

        // Cambiar la columna de fecha para que trabaje con String
        colFecha.setCellValueFactory(cellData -> {
            // Formatear la fecha en un String
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return new SimpleStringProperty(sdf.format(cellData.getValue().getFecha()));
        });

        colMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        // Llamar a cargarEntradas() para llenar la tabla con los datos
        cargarEntradas();
    }
}
