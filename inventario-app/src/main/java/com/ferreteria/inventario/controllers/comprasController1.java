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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
import com.ferreteria.inventario.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class comprasController1 {

    @FXML
    private TextField txtRutProveedor;
    @FXML
    private TextField txtNombreProveedor;
    @FXML
    private TextField txtDireccionProveedor;
    @FXML
    private TextField txtCorreoProveedor;
    @FXML
    private Button btnProveedor;

    @FXML
    private TableView<Producto> tblCarro;

    @FXML
    private TableColumn<Producto, Long> ColCarroId;
    @FXML
    private TableColumn<Producto, String> ColCarroNombre;
    @FXML
    private TableColumn<Producto, Integer> ColCarroCantidad;
    @FXML
    private TableColumn<Producto, String> ColCarroCategoria;
    @FXML
    private TableColumn<Producto, Integer> ColCarroPrecio;

    @FXML
    private TableView<Producto> tblCompras;
    @FXML
    private TableColumn<Producto, Long> ColId;
    @FXML
    private TableColumn<Producto, String> ColNombre;
    @FXML
    private TableColumn<Producto, Integer> colCantidad;

    @FXML
    private Button btnQuitarCarro;
    @FXML
    private Button btnLimpiarCarro;

    @FXML
    private TextField txtID;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtPrecio;
    @FXML
    private Label lblTotal;
    

    // Lista observable para la tabla tblCarro
    private ObservableList<Producto> carroProductos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cargarCategorias();
        cargarProductos();
    
        // Configurar columnas de tblCompras
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

    
        // Agregar un listener para seleccionar un producto en tblCompras
        tblCompras.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
        tblCompras.setItems(productosFiltrados);
    }
    
    
    

    @FXML
    private void agregarCarro(ActionEvent event) {
        Producto productoSeleccionado = tblCompras.getSelectionModel().getSelectedItem();
    
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

        // Ahora, eliminar el producto de tblCompras
        ObservableList<Producto> productosVentas = tblCompras.getItems();
        productosVentas.removeIf(p -> p.getIdProducto() == producto.getIdProducto());

        tblCompras.refresh();  // Refrescar la tabla de ventas
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
        
        // Asignar la lista de productos a la tabla tblCompras
        tblCompras.setItems(productos);
    }

    @FXML
    public void quitarCarro(ActionEvent event) {
        Producto productoSeleccionado = tblCarro.getSelectionModel().getSelectedItem();
        
        if (productoSeleccionado != null) {
            tblCarro.getItems().remove(productoSeleccionado);
            
            tblCompras.getItems().add(productoSeleccionado);
            
            tblCarro.refresh();
            tblCompras.refresh();
    
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
        
            tblCompras.getItems().addAll(productosCarro);
        
            tblCarro.getItems().clear();
            
            tblCompras.refresh();
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
    
        // Filtrar productos en tblCompras para no mostrar los que ya están en el carro
        ObservableList<Producto> productosFiltrados = FXCollections.observableArrayList();
        for (Producto producto : tblCompras.getItems()) {
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
        tblCompras.setItems(productosFiltrados);
    }

    private void actualizarTotalCarro() {
        int total = 0;
        
        // Sumar el precio de cada producto en el carro
        for (Producto producto : carroProductos) {
            total += producto.getPrecio() * producto.getCantidad();
        }
        
        // Actualizar el Label con el total calculado
        lblTotal.setText(total + " $");
    }    

    //"/com/ferreteria/inventario/views/Venta/cargarProveedorView.fxml"
    @FXML
    private void abrirVentanaProveedor(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/Bodega/Opciones/compra/cargarProveedorView.fxml"));
            Parent root = loader.load();

            cargarProveedorController controlador = loader.getController();
            controlador.setControladorProveedor(this);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Seleccionar Proveedor");

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "No se pudo abrir la ventana de proveedores", "Hubo un problema al abrir la vista de proveedores.");
        }
    }
    

    public void recibirDatosProveedor(String rutProveedor, String nombreProveedor, String direccionProveedor, String correoProveedor) {
        // Aquí puedes actualizar los campos en la vista principal con los datos recibidos
        txtRutProveedor.setText(rutProveedor);
        txtNombreProveedor.setText(nombreProveedor);
        txtDireccionProveedor.setText(direccionProveedor);
        txtCorreoProveedor.setText(correoProveedor);
    }
    
    @FXML
    private void Comprar() {
        try {
            boolean confirmacion = Mensaje.mostrarConfirmacion(
                "Confirmar venta",
                "¿Está seguro de que desea procesar esta venta?"
                );
                if (!confirmacion) {
                    return;
                }

            // Paso 2: Validar que haya un proveedor seleccionado
            String rutProveedor = txtRutProveedor.getText(); // Obtener el rut del proveedor
            if (rutProveedor == null || rutProveedor.isEmpty()) {
                Mensaje.mostrarMensaje("Advertencia", "Proveedor no seleccionado", "Debe seleccionar un proveedor antes de realizar la compra.");
                return;
            }

            // Paso 3: Validar que haya productos en el carro de compras
            if (tblCarro.getItems().isEmpty()) {
                Mensaje.mostrarMensaje("Advertencia", "No hay productos", "El carro de compras está vacío.");
                return;
            }

            // Paso 4: Obtener el usuario actual (si es necesario)
            String rutUsuario = SessionManager.getInstance().getRutUsuario(); // Usuario actual en sesión

            // Paso 5: Obtener conexión a la base de datos
            Connection connection = DatabaseConnection.getConnection();

            // Paso 6: Registrar la compra en la tabla `compra`
            LocalDateTime fechaCompra = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String fechaCompraFormatted = fechaCompra.format(formatter);
            String insertCompraQuery = "INSERT INTO compra (fecha, rut_proveedor, rut_usuario, soft_delete) " +
                                    "VALUES (?, ?, ?, 0)";
            PreparedStatement pstCompra = connection.prepareStatement(insertCompraQuery, Statement.RETURN_GENERATED_KEYS);
            pstCompra.setString(1, fechaCompraFormatted);
            pstCompra.setString(2, rutProveedor);
            pstCompra.setString(3, rutUsuario);
            pstCompra.executeUpdate();

            // Paso 7: Obtener el ID de la compra recién insertada
            ResultSet rs = pstCompra.getGeneratedKeys();
            rs.next();
            int idCompra = rs.getInt(1);

            // Paso 8: Insertar en la tabla `detalle_compra` para cada producto en el carro
            for (Producto productoCarro : tblCarro.getItems()) {
                Long idProducto = productoCarro.getIdProducto();  // Obtener el ID del producto
                int cantidad = productoCarro.getCantidad();       // Obtener la cantidad de productos comprados
                double precioUnitario = productoCarro.getPrecio(); // Obtener el precio unitario
        
                // Insertar el detalle de la compra para este producto
                String insertDetalleCompraQuery = "INSERT INTO detalle_compra (id_compra, id_producto, cantidad, precio_u) " +
                                                "VALUES (?, ?, ?, ?)";
                PreparedStatement pstDetalleCompra = connection.prepareStatement(insertDetalleCompraQuery);
                pstDetalleCompra.setInt(1, idCompra);           // ID de la compra
                pstDetalleCompra.setLong(2, idProducto);        // ID del producto
                pstDetalleCompra.setInt(3, cantidad);           // Cantidad de productos comprados
                pstDetalleCompra.setDouble(4, precioUnitario);  // Precio unitario del producto
                pstDetalleCompra.executeUpdate();
            }

            // Paso 9: Actualizar la cantidad en la tabla `producto`
            for (Producto productoCarro : tblCarro.getItems()) {
                int cantidad = productoCarro.getCantidad();
                Long idProducto = productoCarro.getIdProducto();

                // Actualizar la cantidad de producto en la tabla `producto`
                String updateProductoQuery = "UPDATE producto SET cantidad = cantidad + ? WHERE id_producto = ?";
                PreparedStatement pstProducto = connection.prepareStatement(updateProductoQuery);
                pstProducto.setInt(1, cantidad); // Aumentamos la cantidad con la compra
                pstProducto.setLong(2, idProducto);  // Usamos Long para id_producto
                pstProducto.executeUpdate();
            }

            // Paso 10: Mostrar mensaje de confirmación de compra
            Mensaje.mostrarMensaje("informacion", "Compra registrada", "La compra se registró con éxito.");
            cargarProductos();

            // Paso 11: Limpiar la tabla del carro de compras
            tblCarro.getItems().clear();

        } catch (SQLException e) {
            e.printStackTrace();
            Mensaje.mostrarMensaje("Error", "Error al registrar compra", "Ocurrió un error al procesar la compra.");
        }
    }

    
}
