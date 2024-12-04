package com.ferreteria.inventario.utils;

import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;

public class stageManager {

    // Lista estática para registrar todas las ventanas (Stage) abiertas
    private static final List<Stage> stages = new ArrayList<>();

    // Método para añadir una ventana abierta a la lista
    public static void addStage(Stage stage) {
        stages.add(stage);
    }

    // Método para eliminar una ventana de la lista cuando se cierra
    public static void removeStage(Stage stage) {
        stages.remove(stage);
    }

    // Obtener todas las ventanas abiertas
    public static List<Stage> getStages() {
        return stages;
    }

    // Cerrar todas las ventanas activas
    public static void closeAllStages() {
        for (Stage stage : stages) {
            if (stage != null) {
                stage.close();
            }
        }
        stages.clear();  // Limpiar la lista después de cerrar todas las ventanas
    }
}
