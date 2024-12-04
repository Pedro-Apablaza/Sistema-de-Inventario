package com.ferreteria.inventario.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Mensaje {

    public static void mostrarMensaje(String tipo, String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.NONE);

        // Usar un enum para los tipos de alerta puede ser más seguro y legible
        switch (tipo.toLowerCase()) {
            case "informacion":
                alert.setAlertType(AlertType.INFORMATION);
                break;
            case "advertencia":
                alert.setAlertType(AlertType.WARNING);
                break;
            case "error":
                alert.setAlertType(AlertType.ERROR);
                break;
            case "confirmacion":
                alert.setAlertType(AlertType.CONFIRMATION);
                break;
            default:
                alert.setAlertType(AlertType.ERROR); // Por defecto en caso de tipo desconocido
                break;
        }

        alert.setTitle(titulo);
        alert.setHeaderText(null);  // Puedes personalizar este campo si lo necesitas
        alert.setContentText(mensaje); // Aquí se muestra el mensaje

        // Mostrar la alerta y esperar que el usuario la cierre
        alert.showAndWait();
    }

    // Confirmación de mensaje con retorno booleano
    public static boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        // Esperar una respuesta del usuario (OK o CANCEL)
        Optional<ButtonType> resultado = alert.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }
}
