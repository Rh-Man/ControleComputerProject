// ClientHandler.java
package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ServerController serverController;
    private PrintWriter out;
    private BufferedReader in;
    private String clientAddress;
    private boolean isRunning = true;

    public ClientHandler(Socket socket, ServerController controller) {
        this.clientSocket = socket;
        this.serverController = controller;
        this.clientAddress = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            // Initialiser les flux d'entrée/sortie
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            // Lire les commandes du client
            while (isRunning && (inputLine = in.readLine()) != null) {
                serverController.addLog("Commande reçue de " + clientAddress + ": " + inputLine);
                
                // Exécuter la commande
                String result = executeCommand(inputLine);
                
                // Envoyer le résultat au client
                out.println(result);
            }
        } catch (IOException e) {
            if (isRunning) {
                e.printStackTrace();
            }
        } finally {
            close();
        }
    }

    private String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        
        try {
            Process process;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                process = Runtime.getRuntime().exec("cmd.exe /c " + command);
            } else {
                process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});
            }
            
            // Lire la sortie standard
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String s;
            while ((s = stdInput.readLine()) != null) {
                output.append(s).append("\n");
            }
            
            // Lire la sortie d'erreur
            BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((s = stdError.readLine()) != null) {
                output.append(s).append("\n");
            }
            
            // Attendre que le processus se termine
            process.waitFor();
            
        } catch (IOException | InterruptedException e) {
            output.append("Erreur lors de l'exécution de la commande: ").append(e.getMessage());
        }
        
        return output.toString();
    }

    public void close() {
        isRunning = false;
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            
            serverController.removeClient(clientAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}