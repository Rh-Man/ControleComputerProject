// ClientController.java
package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ClientController {
    @FXML
    private TextField ipTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private TextField commandTextField;
    @FXML
    private Button sendButton;
    @FXML
    private TextArea outputTextArea;
    @FXML
    private ListView<String> historyListView;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isConnected = false;
    private ObservableList<String> commandHistory = FXCollections.observableArrayList();

    public void initialize() {
        historyListView.setItems(commandHistory);
        disconnectButton.setDisable(true);
        sendButton.setDisable(true);
        
        connectButton.setOnAction(e -> connect());
        disconnectButton.setOnAction(e -> disconnect());
        sendButton.setOnAction(e -> sendCommand());
        
        // Permettre d'envoyer une commande en appuyant sur Entrée
        commandTextField.setOnAction(e -> sendCommand());
        
        // Permettre de cliquer sur une commande dans l'historique pour la réutiliser
        historyListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selectedCommand = historyListView.getSelectionModel().getSelectedItem();
                if (selectedCommand != null) {
                    commandTextField.setText(selectedCommand);
                }
            }
        });
    }

    private void connect() {
        String ip = ipTextField.getText().trim();
        String portStr = portTextField.getText().trim();
        
        if (ip.isEmpty() || portStr.isEmpty()) {
            outputTextArea.appendText("Veuillez renseigner l'adresse IP et le port\n");
            return;
        }
        
        try {
            int port = Integer.parseInt(portStr);
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            isConnected = true;
            
            // Mettre à jour l'interface
            connectButton.setDisable(true);
            disconnectButton.setDisable(false);
            sendButton.setDisable(false);
            ipTextField.setDisable(true);
            portTextField.setDisable(true);
            
            outputTextArea.appendText("Connecté au serveur " + ip + ":" + port + "\n");
            
            // Démarrer un thread pour lire les réponses du serveur
            new Thread(() -> {
                try {
                    String response;
                    while (isConnected && (response = in.readLine()) != null) {
                        String finalResponse = response;
                        Platform.runLater(() -> {
                            outputTextArea.appendText(finalResponse + "\n");
                        });
                    }
                } catch (IOException e) {
                    if (isConnected) {
                        e.printStackTrace();
                        Platform.runLater(() -> {
                            outputTextArea.appendText("Erreur de communication avec le serveur\n");
                            disconnect();
                        });
                    }
                }
            }).start();
            
        } catch (NumberFormatException e) {
            outputTextArea.appendText("Port invalide\n");
        } catch (IOException e) {
            outputTextArea.appendText("Impossible de se connecter au serveur: " + e.getMessage() + "\n");
        }
    }

    private void disconnect() {
        if (!isConnected) return;
        
        isConnected = false;
        
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Mettre à jour l'interface
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
        sendButton.setDisable(true);
        ipTextField.setDisable(false);
        portTextField.setDisable(false);
        
        outputTextArea.appendText("Déconnecté du serveur\n");
    }

    private void sendCommand() {
        if (!isConnected) return;
        
        String command = commandTextField.getText().trim();
        if (command.isEmpty()) return;
        
        // Envoyer la commande au serveur
        out.println(command);
        
        // Ajouter la commande à l'historique
        commandHistory.add(0, command);
        
        // Vider le champ de commande
        commandTextField.clear();
    }
}