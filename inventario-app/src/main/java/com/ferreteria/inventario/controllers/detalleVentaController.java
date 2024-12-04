package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.DatabaseConnection;
import com.ferreteria.inventario.models.DetalleVenta;
import com.ferreteria.inventario.models.VentaRealizada;
import com.ferreteria.inventario.utils.Mensaje;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;  // Solo esta importación de Label de JavaFX

import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class detalleVentaController {

    @FXML private TableView<VentaRealizada> tblVentasRealizadas;
    @FXML private TableColumn<VentaRealizada, String> ColVentaFecha;
    @FXML private TableColumn<VentaRealizada, Double> ColVentaTotal;
    @FXML private TableColumn<VentaRealizada, String> ColVentaCliente;
    @FXML private TableColumn<VentaRealizada, String> ColVentaRut;
    @FXML private TableColumn<VentaRealizada, String> ColVentaVendedor;

    @FXML private TableView<DetalleVenta> tblDetalleVenta;
    @FXML private TableColumn<DetalleVenta, Long> ColDetalleID;
    @FXML private TableColumn<DetalleVenta, String> ColDetalleProducto;
    @FXML private TableColumn<DetalleVenta, Integer> ColDetallePrecio;
    @FXML private TableColumn<DetalleVenta, Integer> ColDetalleCantidad;
    @FXML private TableColumn<DetalleVenta, Integer> ColDetalleTotal;

    @FXML private TextField txtCliente;
    @FXML private TextField txtVendedor;
    @FXML private TextField txtPrecioTotal;
    @FXML private TextField txtFecha;

    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;

    @FXML private Label precio_total;

    @FXML private Button btnFiltrar;
    @FXML private Button btnQuitarFiltro;

    // Lista observable para cargar los datos
    private Map<VentaRealizada, Integer> ventaIdMap = new HashMap<>();
    private ObservableList<VentaRealizada> ventasList;

    @FXML
    public void initialize() {
        // Inicializar las columnas de la tabla principal (ventas realizadas)
        ColVentaFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        ColVentaTotal.setCellValueFactory(new PropertyValueFactory<>("totalVenta"));
        ColVentaCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        ColVentaRut.setCellValueFactory(new PropertyValueFactory<>("rutCliente"));
        ColVentaVendedor.setCellValueFactory(new PropertyValueFactory<>("vendedor"));
        
        // Inicializar las columnas de la tabla de detalles de venta
        ColDetalleID.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        ColDetalleProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        ColDetallePrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        ColDetalleCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        ColDetalleTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    
        // Cargar datos de ventas realizadas en la tabla principal
        cargarVentasRealizadas();
        configurarFechasPredeterminadas();
        actualizarTotal();
    
        // Configurar listener para detectar selección en la tabla tblVentasRealizadas
        tblVentasRealizadas.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // Obtener el ID de la venta desde el objeto seleccionado
                cargarVentaSeleccionada(newValue);
                Long idVenta = newValue.getIdVenta(); // Asegúrate de que el método getIdVenta() existe en la clase VentaRealizada
                
                // Cargar los detalles de la venta seleccionada en la tabla inferior
                cargarDetalleVenta(idVenta);
            }
        });
    }
    
    private void cargarVentaSeleccionada(VentaRealizada venta) {
        // Cargar los datos en los TextField
        txtCliente.setText(venta.getCliente()); // Nombre del cliente
        txtVendedor.setText(venta.getVendedor()); // Nombre del vendedor
        txtPrecioTotal.setText(String.valueOf(venta.getTotalVenta())); // Total de la venta
        //precio_total.setText(String.format("CLP $%,.2f", total));
        txtFecha.setText(venta.getFecha()); // Fecha de la venta
    }

    public void cargarVentasRealizadas() {
        StringBuilder queryBuilder = new StringBuilder("SELECT v.id_venta, v.fecha, SUM(d.cantidad * d.precio_u * 1.19) AS total_venta, " +
                           "cl.nombre AS cliente_nombre, v.rut_cliente, CONCAT(u.nombre, ' ', u.apellido) AS vendedor_nombre " +
                           "FROM venta v " +
                           "JOIN detalle_venta d ON v.id_venta = d.id_venta " +
                           "JOIN cliente cl ON v.rut_cliente = cl.rut_cliente " +
                           "JOIN usuario u ON v.rut_usuario = u.rut_usuario " +
                           "WHERE v.soft_delete = 0 ");
        
        // Filtros por fecha
        LocalDate fechaInicio = dpFechaInicio.getValue();
        LocalDate fechaFin = dpFechaFin.getValue();
    
        if (fechaInicio != null && fechaFin != null) {
            queryBuilder.append("AND v.fecha BETWEEN ? AND DATE_ADD(?, INTERVAL 1 DAY) ");
        }
    
        queryBuilder.append("GROUP BY v.id_venta, v.fecha, cl.nombre, v.rut_cliente, u.nombre, u.apellido");
    
        String query = queryBuilder.toString();
        ventasList = FXCollections.observableArrayList();
    
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
    
            int paramIndex = 1;
    
            if (fechaInicio != null && fechaFin != null) {
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(fechaInicio));
                stmt.setDate(paramIndex++, java.sql.Date.valueOf(fechaFin));
            }
    
            ResultSet rs = stmt.executeQuery();
    
            while (rs.next()) {
                int idVenta = rs.getInt("id_venta");
                String fecha = rs.getString("fecha");
                double totalVenta = rs.getDouble("total_venta");
                String cliente = rs.getString("cliente_nombre");
                String rutCliente = rs.getString("rut_cliente");
                String vendedor = rs.getString("vendedor_nombre");
    
                VentaRealizada venta = new VentaRealizada(idVenta, fecha, totalVenta, cliente, rutCliente, vendedor);
                ventasList.add(venta);
                ventaIdMap.put(venta, idVenta);
            }
    
            tblVentasRealizadas.setItems(ventasList);
            actualizarTotal();
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    

    public void cargarDetalleVenta(Long idVenta) {
        String sql = "SELECT dv.id_producto, p.nom_producto, p.precio, dv.cantidad, (dv.cantidad * p.precio) AS precio_total " +
                     "FROM detalle_venta dv " +
                     "JOIN producto p ON dv.id_producto = p.id_producto " +
                     "WHERE dv.id_venta = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
        
            stmt.setLong(1, idVenta); // Establecemos el id de la venta seleccionada
            ResultSet rs = stmt.executeQuery();
        
            // Lista observable para almacenar las filas
            ObservableList<DetalleVenta> listaDetalle = FXCollections.observableArrayList();
        
            while (rs.next()) {
                // Crear un objeto DetalleVenta y agregarlo a la lista
                DetalleVenta detalle = new DetalleVenta(
                    rs.getLong("id_producto"),      // ID Producto
                    rs.getString("nom_producto"),  // Nombre Producto
                    rs.getInt("precio"),           // Precio Unitario
                    rs.getInt("cantidad"),         // Cantidad
                    rs.getInt("precio_total")      // Precio Total
                );
                listaDetalle.add(detalle);
            }
        
            // Asignar la lista a la tabla
            tblDetalleVenta.setItems(listaDetalle);
        
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void configurarFechasPredeterminadas() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Obtener la fecha más antigua de las ventas
            String sql = "SELECT MIN(fecha) AS fecha_minima FROM venta WHERE soft_delete = 0";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    // Establecer la fecha de inicio al primer registro de venta
                    LocalDate fechaMinima = rs.getDate("fecha_minima").toLocalDate();
                    dpFechaInicio.setValue(fechaMinima);
                }
            }

            // Establecer la fecha de fin como la fecha actual (hoy)
            dpFechaFin.setValue(LocalDate.now());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void actualizarTotal() {
        double total = 0.0;
    
        // Verificar si la lista de ventas no está vacía
        if (ventasList != null && !ventasList.isEmpty()) {
            // Recorremos la lista de ventas y sumamos el total de cada venta
            for (VentaRealizada venta : ventasList) {
                // Verificamos el total de cada venta, para asegurarnos de que no se esté acumulando de forma incorrecta
                double totalVenta = venta.getTotalVenta();
                if (totalVenta > 0) {  // Solo sumamos ventas con un total positivo
                    total += totalVenta;
                }
            }
        }
    
        // Mostrar el total con el formato CLP
        precio_total.setText(String.format("CLP $%,.2f", total));
    }

    @FXML
    private void filtrarPorFechas() {
        // Obtener las fechas seleccionadas
        LocalDate fechaInicio = dpFechaInicio.getValue();
        LocalDate fechaFin = dpFechaFin.getValue();

        // Validar que ambas fechas estén seleccionadas
        if (fechaInicio == null || fechaFin == null) {
            Mensaje.mostrarMensaje("advertencia", "Fecha no seleccionada", "Por favor, seleccione ambas fechas: inicio y fin.");
            return;
        }

        // Validar que la fecha de inicio no sea mayor a la fecha de fin
        if (fechaInicio.isAfter(fechaFin)) {
            Mensaje.mostrarMensaje("error", "Fecha de inicio inválida", "La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }

        // Validar que la fecha de fin no sea menor a la fecha de inicio
        if (fechaFin.isBefore(fechaInicio)) {
            Mensaje.mostrarMensaje("error", "Fecha de fin inválida", "La fecha de fin no puede ser anterior a la fecha de inicio.");
            return;
        }

        // Aplicar el filtro con las fechas seleccionadas
        cargarVentasRealizadas();
    }


    @FXML
    private void quitarFiltro() {
        // Restablecer las fechas a los valores predeterminados
        configurarFechasPredeterminadas();
        
        // Recargar la tabla con todos los datos (sin filtros)
        cargarVentasRealizadas();
    }
}

