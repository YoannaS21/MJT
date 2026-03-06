package bg.sofia.uni.fmi.mjt.foodanalyzer;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.CacheService;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.ClientHandler;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.HttpService;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FoodAnalyzerServer {
    private final int port;
    private final ExecutorService executor;
    private final HttpService httpService;
    private final CacheService cacheService;
    private final List<ClientHandler> activeClients;
    private ServerSocket serverSocket;
    private volatile boolean isRunning;

    public FoodAnalyzerServer(int port) {
        this.port = port;
        this.executor = Executors.newCachedThreadPool();
        this.httpService = new HttpService();
        this.cacheService = new CacheService();
        this.activeClients = new CopyOnWriteArrayList<>();
    }

    FoodAnalyzerServer(int port, HttpService httpService, CacheService cacheService) {
        this.port = port;
        this.executor = Executors.newCachedThreadPool();
        this.httpService = httpService;
        this.cacheService = cacheService;
        this.activeClients = new CopyOnWriteArrayList<>();
    }

    public void start() {
        try {
            this.serverSocket = new ServerSocket(port);
            this.isRunning = true;
            System.out.println("Food Analyzer Server started on port " + port);
            System.out.println("Type 'stop' in the server console to shut down.");

            // Thread for stop command
            startConsoleListener();

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    
                    ClientHandler handler = new ClientHandler(clientSocket, httpService, cacheService, activeClients);
                    activeClients.add(handler);

                    executor.submit(handler);
                } catch (SocketException e) {
                    if (isRunning) Logger.logError("Server socket closed", e);
                } catch (IOException e) {
                    if (isRunning) Logger.logError("Error accepting connection", e);
                }
            }
        } catch (IOException e) {
            Logger.logError("Critical: Could not bind to port " + port, e);
        } finally {
            stop();
        }
    }

    private void startConsoleListener() {
        Thread consoleThread = new Thread(() -> {
            try (Scanner scanner = new Scanner(System.in)) {
                while (isRunning) {
                    if (scanner.hasNextLine()) {
                        String cmd = scanner.nextLine().trim();
                        if ("stop".equalsIgnoreCase(cmd)) {
                            stop();
                            break;
                        }
                    }
                }
            }
        });
        consoleThread.setDaemon(true);
        consoleThread.start();
    }

    public void stop() {
        if (!isRunning) return;
        isRunning = false;

        System.out.println("Shutting down server...");

        for (ClientHandler handler : activeClients) {
            handler.stopGracefully();
        }

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            Logger.logError("Error closing server socket", e);
        }

        executor.shutdownNow();
        System.out.println("Server stopped.");
    }
}