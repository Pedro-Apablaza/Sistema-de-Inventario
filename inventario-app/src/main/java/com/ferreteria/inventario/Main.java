package com.ferreteria.inventario;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

   @Override
public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/ferreteria/inventario/views/loginView.fxml"));
    VBox root = loader.load(); // Cambia GridPane a VBox
    primaryStage.setScene(new Scene(root));
    primaryStage.setTitle("Login");
    primaryStage.show();
}


    public static void main(String[] args) {
        launch(args);
    }
}

//mvn clean install
//mvn exec:java