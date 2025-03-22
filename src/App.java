// App.java

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        
        // Démarrer le serveur
        FXMLLoader serverLoader = new FXMLLoader(getClass().getResource("/views/ServerInterface.fxml"));
        Parent serverRoot = serverLoader.load();
        Stage serverStage = new Stage();
        serverStage.setTitle("Serveur de Contrôle à Distance");
        serverStage.setScene(new Scene(serverRoot));
        serverStage.show();
        
        // Démarrer le client
        FXMLLoader clientLoader = new FXMLLoader(getClass().getResource("/views/ClientInterface.fxml"));
        Parent clientRoot = clientLoader.load();
        Stage clientStage = new Stage();
        clientStage.setTitle("Client de Contrôle à Distance");
        clientStage.setScene(new Scene(clientRoot));
        clientStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
