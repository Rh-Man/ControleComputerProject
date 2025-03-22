// ServerController.java
package controllers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

public class ServerController {
    @FXML
    private ListView<String> clientListView;
    @FXML
    private TextArea commandsTextArea;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;

    private ServerSocket serverSocket;
    private final int PORT = 5000;
    private boolean isRunning = false;
    private ExecutorService executor;
    private ObservableList<String> connectedClients = FXCollections.observableArrayList();
    private List<ClientHandler> clientHandlers = new ArrayList<>();

    public void initialize() {
        clientListView.setItems(connectedClients);
        stopButton.setDisable(true);
        
        startButton.setOnAction(e -> startServer());
        stopButton.setOnAction(e -> stopServer());
    }

    private void startServer() {
        if (isRunning) return;
        
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            executor = Executors.newCachedThreadPool();
            
            startButton.setDisable(true);
            stopButton.setDisable(false);
            
            addLog("Serveur démarré sur le port " + PORT);
            
            // Démarrer un thread pour accepter les connexions
            new Thread(() -> {
                while (isRunning) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        String clientAddress = clientSocket.getInetAddress().getHostAddress();
                        
                        // Ajouter le client à la liste
                        Platform.runLater(() -> {
                            connectedClients.add(clientAddress);
                            addLog("Nouveau client connecté: " + clientAddress);
                        });
                        
                        // Créer et démarrer un handler pour ce client
                        ClientHandler handler = new ClientHandler(clientSocket, this);
                        clientHandlers.add(handler);
                        executor.execute(handler);
                        
                    } catch (IOException e) {
                        if (isRunning) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            
        } catch (IOException e) {
            e.printStackTrace();
            addLog("Erreur lors du démarrage du serveur: " + e.getMessage());
        }
    }

    private void stopServer() {
        if (!isRunning) return;
        
        isRunning = false;
        
        // Fermer toutes les connexions client
        for (ClientHandler handler : clientHandlers) {
            handler.close();
        }
        clientHandlers.clear();
        
        // Fermer le serveur
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Arrêter l'executor
        if (executor != null) {
            executor.shutdown();
        }
        
        // Mettre à jour l'interface
        Platform.runLater(() -> {
            connectedClients.clear();
            startButton.setDisable(false);
            stopButton.setDisable(true);
            addLog("Serveur arrêté");
        });
    }

    public void addLog(String message) {
        Platform.runLater(() -> {
            commandsTextArea.appendText(message + "\n");
        });
    }

    public void removeClient(String clientAddress) {
        Platform.runLater(() -> {
            connectedClients.remove(clientAddress);
            addLog("Client déconnecté: " + clientAddress);
        });
    }
}