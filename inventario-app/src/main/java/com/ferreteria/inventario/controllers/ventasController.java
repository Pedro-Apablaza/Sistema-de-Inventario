package com.ferreteria.inventario.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.ferreteria.inventario.models.Categoria;
import com.ferreteria.inventario.models.Producto;
import com.ferreteria.inventario.utils.Mensaje;
import com.ferreteria.inventario.utils.SessionManager;
import com.ferreteria.inventario.utils.stageManager;
import com.ferreteria.inventario.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ventasController {

    @FXML private TextField txtRutCliente;
    @FXML private TextField txtNombreCliente;
    @FXML private TextField txtApellidoCliente;
    @FXML private TextField txtCorreoCliente;
    @FXML private Button btnCliente;

    @FXML private TableView<Producto> tblCarro;

    @FXML private TableColumn<Producto, Long> ColCarroId;
    @FXML private TableColumn<Producto, String> ColCarroNombre;
    @FXML private TableColumn<Producto, Integer> ColCarroCantidad;
    @FXML private TableColumn<Producto, String> ColCarroCategoria;
    @FXML private TableColumn<Producto, Integer> ColCarroPrecio;

    @FXML private TableView<Producto> tblVentas;
    @FXML private TableColumn<Producto, Long> ColId;
    @FXML private TableColumn<Producto, String> ColNombre;
    @FXML private TableColumn<Producto, Integer> colCantidad;

    @FXML private Button btnQuitarCarro;
    @FXML private Button btnLimpiarCarro;

    @FXML private TextField txtID;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private Label lblSubtotal;
    @FXML private Label lblImpuesto;
    @FXML private Label lblTotal;
    
    @FXML
    private RadioButton rdtBoleta;
    @FXML
    private RadioButton rdtFactura;

    private ToggleGroup tipoPago;

    // Lista observable para la tabla tblCarro
    private ObservableList<Producto> carroProductos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cargarCategorias();
        cargarProductos();

        tipoPago = new ToggleGroup();
        rdtBoleta.setToggleGroup(tipoPago);
        rdtFactura.setToggleGroup(tipoPago);
    
        // Seleccionar "Boleta" por defecto
        rdtBoleta.setSelected(true);
    
        // Configurar los RadioButton para que siempre una opción esté seleccionada
        tipoPago.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                // Si no hay nada seleccionado, vuelve a seleccionar "Boleta"
                rdtBoleta.setSelected(true);
            }
        });
    
        // Configurar columnas de tblVentas
        ColId.setCellValueFactory(cellData -> cellData.getValue().idProductoProperty().asObject());
        ColNombre.setCellValueFactory(cellData -> cellData.getValue().nomProductoProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
    
        // Configurar columnas de tblCarro
        ColCarroId.setCellValueFactory(cellData -> cellData.getValue().idProductoProperty().asObject());
        ColCarroNombre.setCellValueFactory(cellData -> cellData.getValue().nomProductoProperty());
        ColCarroCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        ColCarroCategoria.setCellValueFactory(cellData -> cellData.getValue().nomCategoriaProperty());
        ColCarroPrecio.setCellValueFactory(cellData -> cellData.getValue().precioProperty().asObject());

        ColCarroPrecio.setCellValueFactory(cellData -> {
            // Precio total = precio unitario * cantidad
            Producto producto = cellData.getValue();
            int precioTotal = producto.getPrecio() * producto.getCantidad();  // Multiplicamos el precio unitario por la cantidad
            return new javafx.beans.property.SimpleIntegerProperty(precioTotal).asObject(); // Devuelve el precio total
        });

    
        // Agregar un listener para seleccionar un producto en tblVentas
        tblVentas.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mostrarDatosProducto(newValue);  // Llamamos al método para llenar los TextField
            }
        });

        tblCarro.setItems(carroProductos);
    }


    private void mostrarDatosProducto(Producto producto) {
        // Llenar los TextField con la información del producto seleccionado
        txtID.setText(String.valueOf(producto.getIdProducto()));
        txtNombre.setText(producto.getNomProducto());
        txtPrecio.setText(String.valueOf(producto.getPrecio()));
    }

    @FXML
    private FlowPane fpnCategoria;

    private void cargarCategorias() {
        ObservableList<Categoria> categorias = obtenerCategoriasDeBaseDeDatos();
    
        // Crear un botón para cada categoría obtenida de la base de datos
        for (Categoria categoria : categorias) {
            Button btnCategoria = new Button(categoria.getNomCategoria());
            btnCategoria.setPrefWidth(126);
            btnCategoria.setPrefHeight(122);
            btnCategoria.setOnAction(event -> filtrarProductosPorCategoria(categoria.getNomCategoria()));
            fpnCategoria.getChildren().add(btnCategoria);
        }
    }
    
    

    private ObservableList<Categoria> obtenerCategoriasDeBaseDeDatos() {
        ObservableList<Categoria> categorias = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT id_categoria, nom_categoria FROM categoria WHERE soft_delete = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int idCategoria = rs.getInt("id_categoria");
                String nomCategoria = rs.getString("nom_categoria");
                categorias.add(new Categoria(idCategoria, nomCategoria, false));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categorias;
    }

    private void filtrarProductosPorCategoria(String nomCategoria) {
        // Recargar todos los productos desde la base de datos
        ObservableList<Producto> productos = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.id_producto, p.nom_producto, p.cantidad, p.precio, c.nom_categoria FROM producto p JOIN categoria c ON p.id_categoria = c.id_categoria WHERE p.soft_delete = 0 AND c.soft_delete = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Producto producto = new Producto(
                    rs.getLong("id_producto"),
                    rs.getString("nom_producto"),
                    0,  // El precio no se necesita para este filtro
                    rs.getInt("cantidad"),
                    0,  // El stock crítico no es necesario en este filtro
                    rs.getString("nom_categoria")
                );
                productos.add(producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        // Filtrar los productos según la categoría seleccionada
        ObservableList<Producto> productosFiltrados = FXCollections.observableArrayList();
        for (Producto producto : productos) {
            if (nomCategoria.equals("Sin categorías") || producto.getNomCategoria().equals(nomCategoria)) {
                productosFiltrados.add(producto);
            }
        }
    
        // Reemplazar los productos en la tabla con la lista filtrada
        tblVentas.setItems(productosFiltrados);
    }
    
    
    

    @FXML
    private void agregarCarro(ActionEvent event) {
        Producto productoSeleccionado = tblVentas.getSelectionModel().getSelectedItem();
    
        if (productoSeleccionado != null) {
            mostrarVentanaEmergente(productoSeleccionado);
        } else {
            Mensaje.mostrarMensaje("Advertencia", "Debe seleccionar un producto primero", "advertencia");
        }
    }

    private void mostrarVentanaEmergente(Producto producto) {
        // Crear la ventana emergente
        Stage ventanaEmergente = new Stage();
        ventanaEmergente.initModality(Modality.APPLICATION_MODAL);
        ventanaEmergente.setTitle("Agregar al Carro");
    
        // Establecer el color de fondo de la ventana
        ventanaEmergente.setScene(new Scene(new StackPane(), Color.LIGHTYELLOW));
    
        // Crear los elementos en la ventana emergente
        Label lblCantidad = new Label("Cantidad a vender:");
        TextField txtCantidad = new TextField();
        txtCantidad.setPromptText("Ingrese la cantidad"); // Indicador de lo que debe ingresar el usuario
    
        Button btnAceptar = new Button("AGREGAR");
        Button btnCancelar = new Button("CANCELAR");
    
        // Establecer los eventos para los botones
        btnAceptar.setOnAction(e -> {
            String cantidadStr = txtCantidad.getText();
            try {
                // Intentar parsear el valor ingresado como número
                int cantidad = Integer.parseInt(cantidadStr);
    
                // Validar que la cantidad sea mayor a 0
                if (cantidad > 0 && cantidad <= producto.getCantidad()) {
                    // Llamada a agregarProductoACarro con ambos parámetros
                    agregarProductoACarro(producto, cantidad);
                    ventanaEmergente.close();
                } else if (cantidad <= 0) {
                    Mensaje.mostrarMensaje("Error", "Error", "La cantidad debe ser mayor a 0.");
                } else {
                    Mensaje.mostrarMensaje("Error",  "Error", "No hay suficiente stock disponible.");
                }
            } catch (NumberFormatException ex) {
                // En caso de que el valor ingresado no sea un número
                Mensaje.mostrarMensaje("Error", "Error" , "Debe ingresar un número válido.");
            }
        });
    
        btnCancelar.setOnAction(e -> ventanaEmergente.close()); // Cerrar la ventana al cancelar
    
        // Crear un layout para organizar los elementos
        VBox vbox = new VBox(10); // Espaciado de 10 entre los elementos
        vbox.setAlignment(Pos.CENTER); // Centrar los elementos
        vbox.setStyle("-fx-padding: 20;"); // Añadir algo de relleno dentro de la ventana
    
        // Agregar los elementos al layout
        vbox.getChildren().addAll(lblCantidad, txtCantidad, btnAceptar, btnCancelar);
    
        // Establecer la escena con el layout de VBox
        Scene scene = new Scene(vbox, 300, 200);
        ventanaEmergente.setScene(scene);
        ventanaEmergente.show();
    }
    



    private void agregarProductoACarro(Producto producto, int cantidad) {
        // Crear un nuevo objeto Producto con la cantidad seleccionada
        Producto productoCarro = new Producto(
                producto.getIdProducto(),
                producto.getNomProducto(),
                producto.getPrecio(),
                cantidad,
                producto.getStockCritico(),
                producto.getNomCategoria()
        );

        // Verificar si el producto ya está en el carro
        boolean productoExistente = false;
        for (Producto p : carroProductos) {
            if (p.getIdProducto() == productoCarro.getIdProducto()) {
                // Si el producto ya existe, actualizamos la cantidad
                p.setCantidad(p.getCantidad() + cantidad);
                productoExistente = true;
                break;
            }
        }

        if (!productoExistente) {
            carroProductos.add(productoCarro); // Agregar el producto al carro si no existe
        }

        // Ahora, eliminar el producto de tblVentas
        ObservableList<Producto> productosVentas = tblVentas.getItems();
        productosVentas.removeIf(p -> p.getIdProducto() == producto.getIdProducto());

        tblVentas.refresh();  // Refrescar la tabla de ventas
        tblCarro.refresh();   // Refrescar la tabla del carro

        // Actualizar el total del carro
        actualizarTotalCarro();
    }
    


    private void cargarProductos() {
        ObservableList<Producto> productos = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT p.id_producto, p.nom_producto, p.cantidad, p.precio, c.nom_categoria " +
                         "FROM producto p " +
                         "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                         "WHERE p.soft_delete = 0";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Producto producto = new Producto(
                    rs.getLong("id_producto"),
                    rs.getString("nom_producto"),
                    rs.getInt("precio"),
                    rs.getInt("cantidad"),
                    0,  // stockCritico (puedes ajustar según sea necesario)
                    rs.getString("nom_categoria") // Ahora asignamos el nombre de la categoría
                );
                productos.add(producto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Asignar la lista de productos a la tabla tblVentas
        tblVentas.setItems(productos);
    }

    @FXML
    public void quitarCarro(ActionEvent event) {
        Producto productoSeleccionado = tblCarro.getSelectionModel().getSelectedItem();
        
        if (productoSeleccionado != null) {
            tblCarro.getItems().remove(productoSeleccionado);
            
            tblVentas.getItems().add(productoSeleccionado);
            
            tblCarro.refresh();
            tblVentas.refresh();
    
            // Actualizar el total después de quitar el producto
            actualizarTotalCarro();
        } else {
            Mensaje.mostrarMensaje("Advertencia", "Por favor, selecciona un producto para quitar del carro.", "advertencia");
        }
    }
    
    

    @FXML
    public void limpiarCarro(ActionEvent event) {
        // Mostrar mensaje de confirmación utilizando tu clase Mensaje
        boolean confirmar = Mensaje.mostrarConfirmacion("Confirmar acción", "¿Estás seguro de que deseas limpiar el carro? Esta acción eliminará todos los productos del carro.");
        
        if (confirmar) {
            ObservableList<Producto> productosCarro = tblCarro.getItems();
        
            tblVentas.getItems().addAll(productosCarro);
        
            tblCarro.getItems().clear();
            
            tblVentas.refresh();
            tblCarro.refresh();
    
            // Actualizar el total después de limpiar el carro
            actualizarTotalCarro();
        }
    }
    
    
    @FXML
    private void quitarFiltro(ActionEvent event) {
        cargarProductos();
        filtrarProductosEnCarro();
    }
    
    private void filtrarProductosEnCarro() {
        // Obtener los productos que ya están en el carro
        ObservableList<Producto> productosCarro = tblCarro.getItems();
    
        // Filtrar productos en tblVentas para no mostrar los que ya están en el carro
        ObservableList<Producto> productosFiltrados = FXCollections.observableArrayList();
        for (Producto producto : tblVentas.getItems()) {
            boolean existeEnCarro = false;
            for (Producto p : productosCarro) {
                if (producto.getIdProducto() == p.getIdProducto()) {
                    existeEnCarro = true;
                    break;
                }
            }
            if (!existeEnCarro) {
                productosFiltrados.add(producto);
            }
        }
        
        // Actualizar la tabla con los productos filtrados
        tblVentas.setItems(productosFiltrados);
    }

    private void actualizarTotalCarro() {
        double subtotal = 0.0;
    
        // Calcular el subtotal (suma de precio * cantidad de cada producto en el carro)
        for (Producto producto : carroProductos) {
            subtotal += producto.getPrecio() * producto.getCantidad();
        }
    
        // Calcular el impuesto (19%)
        double impuesto = subtotal * 0.19;
    
        // Calcular el total
        double total = subtotal + impuesto;
    
        // Actualizar los Labels con los valores calculados
        lblSubtotal.setText(String.format("%.2f $", subtotal)); // Formato a dos decimales
        lblImpuesto.setText(String.format("%.2f $", impuesto));
        lblTotal.setText(String.format("%.2f $", total));
    }
       

    //"/com/ferreteria/inventario/views/Venta/cargarClienteView.fxml"
    @FXML
    private void abrirVentanaCliente(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Venta/cargarClienteView.fxml"));
            Parent root = loader.load();
    
            // Obtener el controlador de la nueva vista
            cargarClienteController controlador = loader.getController();
    
            // Establecer el controladorVenta en el controlador de cargarCliente
            controlador.setControladorVenta(this);
    
            // Crear el nuevo Stage
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Seleccionar Cliente");
    
            // Registrar la ventana en el stageManager
            stageManager.addStage(stage);
    
            // Mostrar la ventana
            stage.show();
    
            // Evento al cerrar para remover del stageManager
            stage.setOnCloseRequest(e -> stageManager.removeStage(stage));
    
        } catch (IOException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("error", "Error", "No se pudo abrir la ventana de selección de cliente.");
        }
    }
    

    public void recibirDatosCliente(String rutCliente, String nombreCliente, String apellidoCliente, String correoCliente) {
        // Aquí puedes actualizar los campos en la vista principal con los datos recibidos
        txtRutCliente.setText(rutCliente);
        txtNombreCliente.setText(nombreCliente);
        txtApellidoCliente.setText(apellidoCliente);
        txtCorreoCliente.setText(correoCliente);
    }


    
@FXML
private void Vender() {
    try {
        // Paso 0: Mostrar mensaje de confirmación antes de proceder
        boolean confirmacion = Mensaje.mostrarConfirmacion(
        "Confirmar venta",
        "¿Está seguro de que desea procesar esta venta?"
        );
        if (!confirmacion) {
            return;
        }
        // Paso 1: Validar que haya productos en el carro de ventas
        if (tblCarro.getItems().isEmpty()) {
            Mensaje.mostrarMensaje("Advertencia", "No hay productos", "El carro de ventas está vacío.");
            return;
        }

        // Paso 2: Recuperar el cliente y el usuario
        String rutCliente = txtRutCliente.getText(); // Asumiendo que ya tienes el rut del cliente en este campo
        String rutUsuario = SessionManager.getInstance().getRutUsuario(); // Usuario actual en sesión

        // Recuperar tipo de pago desde los RadioButtons
        String tipoPago = "";
        if (rdtBoleta.isSelected()) {
            tipoPago = "Boleta";
        } else if (rdtFactura.isSelected()) {
            tipoPago = "Factura";
        }

        // Validar que el cliente y el tipo de pago estén seleccionados
        if (rutCliente == null || rutCliente.isEmpty() || tipoPago.isEmpty()) {
            Mensaje.mostrarMensaje("Error", "Datos faltantes", "Por favor, seleccione un cliente para la venta.");
            return;
        }

        // Paso 3: Obtener conexión a la base de datos
        Connection connection = DatabaseConnection.getConnection();

        // Paso 4: Registrar la venta en la tabla `venta`
        // Formatear la fecha con el formato dd/MM/yyyy HH:mm:ss
        LocalDateTime fechaVenta = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fechaVentaFormatted = fechaVenta.format(formatter);
        String insertVentaQuery = "INSERT INTO venta (fecha, rut_cliente, rut_usuario, tipo_pago, soft_delete) " +
        "VALUES (?, ?, ?, ?, 0)";
        PreparedStatement pstVenta = connection.prepareStatement(insertVentaQuery, Statement.RETURN_GENERATED_KEYS);
        pstVenta.setString(1, fechaVentaFormatted);  // Utiliza la fecha formateada
        pstVenta.setString(2, rutCliente);
        pstVenta.setString(3, rutUsuario);
        pstVenta.setString(4, tipoPago);
        pstVenta.executeUpdate();

        // Paso 5: Obtener el ID de la venta recién insertada
        ResultSet rs = pstVenta.getGeneratedKeys();
        rs.next();
        int idVenta = rs.getInt(1);

        // Paso 6: Insertar en la tabla `detalle_venta` para cada producto en el carro
        for (Producto productoCarro : tblCarro.getItems()) {
            Long idProducto = productoCarro.getIdProducto();  // Obtener el ID del producto
            int cantidad = productoCarro.getCantidad();       // Obtener la cantidad de productos vendidos
            double precioUnitario = productoCarro.getPrecio(); // Obtener el precio unitario

            // Insertar el detalle de la venta para este producto
            String insertDetalleVentaQuery = "INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_u) " +
                                            "VALUES (?, ?, ?, ?)";
            PreparedStatement pstDetalleVenta = connection.prepareStatement(insertDetalleVentaQuery);
            pstDetalleVenta.setInt(1, idVenta);           // ID de la venta
            pstDetalleVenta.setLong(2, idProducto);       // ID del producto
            pstDetalleVenta.setInt(3, cantidad);          // Cantidad de productos vendidos
            pstDetalleVenta.setDouble(4, precioUnitario); // Precio unitario del producto
            pstDetalleVenta.executeUpdate();
        }

        // Paso 7: Actualizar la cantidad en la tabla `producto`
        for (Producto productoCarro : tblCarro.getItems()) {
            int cantidad = productoCarro.getCantidad();
            Long idProducto = productoCarro.getIdProducto();

            // Actualizar la cantidad de producto en la tabla `producto`
            String updateProductoQuery = "UPDATE producto SET cantidad = cantidad - ? WHERE id_producto = ?";
            PreparedStatement pstProducto = connection.prepareStatement(updateProductoQuery);
            pstProducto.setInt(1, cantidad);
            pstProducto.setLong(2, idProducto);  // Usamos Long para id_producto
            pstProducto.executeUpdate();
        }

        // Paso 8: Mostrar mensaje de confirmación de venta
        Mensaje.mostrarMensaje("informacion", "Venta registrada", "La venta se registró con éxito.");

        // Paso 9: Limpiar la tabla del carro de ventas
        tblCarro.getItems().clear();

        // Paso 10: Mostrar la factura
        mostrarFactura(idVenta, rutCliente, tipoPago);
        cargarProductos();
        lblImpuesto.setText("");    
        lblSubtotal.setText("");
        lblTotal.setText("");
        

    } catch (SQLException e) {
        e.printStackTrace();
        Mensaje.mostrarMensaje("Error", "Error al registrar venta", "Ocurrió un error al procesar la venta.");
    }
}

    
    private void mostrarFactura(int idVenta, String rutCliente, String tipoPago) {
        try {
            // Cargar la vista de la factura
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/facturaView.fxml"));
            Parent root = loader.load();
    
            // Obtener el controlador de la factura
            facturaController facturaController = loader.getController();
    
            // Cargar los detalles de la factura con el idVenta, rutCliente y tipoPago
            facturaController.cargarFactura(idVenta, rutCliente, tipoPago);
    
            // Mostrar la ventana de la factura
            Stage stage = new Stage();
            stage.setTitle("Factura de Venta");
            stage.setScene(new Scene(root));
            stage.show();
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    


    

    @FXML
    public void detalleVentaView() {
        try {
            // Cargar el archivo FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Venta/detalleVentaView.fxml"));
            Parent root = loader.load();
    
            // Crear una nueva escena con el contenido de la vista cargada
            Scene scene = new Scene(root);
    
            // Crear un nuevo Stage (ventana) y establecer la escena
            Stage stage = new Stage();
            stage.setTitle("Detalle de Venta");
            stage.setScene(scene);
    
            // Añadir el Stage al stageManager para su control
            stageManager.addStage(stage);
    
            // Mostrar la ventana
            stage.show();
    
            // Establecer un evento para eliminar la ventana de la lista cuando se cierre
            stage.setOnCloseRequest(event -> stageManager.removeStage(stage));
        } catch (IOException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "Error al cargar la vista", "Hubo un problema al intentar cargar la vista de detalle de venta.");
        }
    }
    
    
}
