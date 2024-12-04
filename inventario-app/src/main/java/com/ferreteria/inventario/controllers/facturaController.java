package com.ferreteria.inventario.controllers;

import com.ferreteria.inventario.models.Cliente;
import com.ferreteria.inventario.models.DetalleVenta;
import com.ferreteria.inventario.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class facturaController {

    @FXML
    private Label lblID;

    @FXML
    private Label lblCorreo;

    @FXML
    private Label lblFecha;

    @FXML
    private Label lblNombre;

    @FXML
    private Label lblRut;

    @FXML
    private Label lblSubtotal;

    @FXML
    private Label lblImpuesto;

    @FXML
    private Label lblTotal;
    
    @FXML
    private Label lblTipo;

    @FXML
    private TableView<DetalleVenta> tblFactura;

    @FXML
    private TableColumn<DetalleVenta, String> colDescripcion;

    @FXML
    private TableColumn<DetalleVenta, Integer> colPrecio;

    @FXML
    private TableColumn<DetalleVenta, Integer> colCantidad;

    @FXML
    private TableColumn<DetalleVenta, Integer> colTotal;

    public void initialize() {
        // Inicializamos las columnas de la tabla
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    // Método que carga la factura con los datos correspondientes
    public void cargarFactura(int idVenta, String rutCliente, String tipoPago) {
        // Primero cargamos los detalles del cliente, fecha, subtotal, impuestos, etc.
        Cliente cliente = obtenerCliente(rutCliente);
        cargarDatosFactura(idVenta, cliente);
        cargarDetallesVenta(idVenta);
    
        // Ahora actualizamos el lblTipo según el tipo de pago
        if ("Boleta".equals(tipoPago)) {
            lblTipo.setText("Boleta N° ");
        } else if ("Factura".equals(tipoPago)) {
            lblTipo.setText("Factura N° ");
        }
    }
    

    // Método para obtener los datos del cliente (nombre, correo, etc.)
    private Cliente obtenerCliente(String rutCliente) {
        Cliente cliente = null;
    
        try (Connection conn = DatabaseConnection.getConnection()) {
            // El nombre de la columna debe coincidir con el de la base de datos
            String sql = "SELECT rut_cliente, nombre, apellido, correo FROM cliente WHERE rut_cliente = ? AND soft_delete = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, rutCliente); // El parámetro es 'rutCliente'
            ResultSet rs = stmt.executeQuery();
    
            if (rs.next()) {
                String rut = rs.getString("rut_cliente");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String correo = rs.getString("correo");
    
                cliente = new Cliente(rut, nombre, apellido, correo);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return cliente;
    }
    

    private void cargarDatosFactura(int idVenta, Cliente cliente) {
        if (cliente != null) {
            // Si el cliente se carga correctamente, se asignan los valores a los Label
            System.out.println("Cliente cargado: " + cliente.getNombre());
            lblRut.setText(cliente.getRut());
            lblNombre.setText(cliente.getNombre() + " " + cliente.getApellido());
            lblCorreo.setText(cliente.getCorreo());
        } else {
            // Si no se encuentra al cliente, se muestra un mensaje en la consola
            System.out.println("Cliente no encontrado");
        }

        // Aquí puedes continuar con el código para establecer el ID de la venta y la fecha
        lblID.setText(String.valueOf(idVenta));

        // Formatear la fecha para el formato dd/MM/yyyy HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        lblFecha.setText(LocalDateTime.now().format(formatter));  // Formato actualizado

        // Calcular el subtotal, impuesto y total
        double subtotal = calcularSubtotal(idVenta);  // Obtener el subtotal desde los detalles de la venta

        lblSubtotal.setText(String.format("$%.2f", subtotal));

        // Calcular el impuesto (19%)
        double impuesto = subtotal * 0.19;  // 19% de impuesto
        lblImpuesto.setText(String.format("$%.2f", impuesto));

        // El total sería el subtotal más el impuesto
        double total = subtotal + impuesto;
        lblTotal.setText(String.format("$%.2f", total));
    }

    // Método para calcular el subtotal
    private double calcularSubtotal(int idVenta) {
        double subtotal = 0.0;

        // Suponiendo que tienes un método que obtiene los detalles de la venta, por ejemplo:
        ObservableList<DetalleVenta> detallesVenta = obtenerDetallesVenta(idVenta);
        
        // Calcular el subtotal sumando (precio * cantidad) para cada detalle de la venta
        for (DetalleVenta detalle : detallesVenta) {
            subtotal += detalle.getPrecio() * detalle.getCantidad();
        }

        return subtotal;
    }



    // Método para cargar los detalles de la venta
    private void cargarDetallesVenta(int idVenta) {
        ObservableList<DetalleVenta> detalles = obtenerDetallesVenta(idVenta);
        tblFactura.setItems(detalles);
    }

    private ObservableList<DetalleVenta> obtenerDetallesVenta(int idVenta) {
        ObservableList<DetalleVenta> detalles = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT dv.id_producto, p.nom_producto, dv.precio_u, dv.cantidad " +
                        "FROM detalle_venta dv " +
                        "JOIN producto p ON dv.id_producto = p.id_producto " +
                        "WHERE dv.id_venta = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idVenta);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Long idProducto = rs.getLong("id_producto");
                String nombreProducto = rs.getString("nom_producto");
                Integer precio = rs.getInt("precio_u");
                Integer cantidad = rs.getInt("cantidad");

                DetalleVenta detalle = new DetalleVenta(idProducto, nombreProducto, precio, cantidad, precio * cantidad);
                detalles.add(detalle);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return detalles;
    }
    
}
