package bg.sofia.uni.fmi.mjt.foodanalyzer;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.BarcodeUtils;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.NetworkClient;

import java.io.File;
import java.util.Scanner;

public class FoodAnalyzerConsole {
    private final String host;
    private final int port;

    public FoodAnalyzerConsole(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try (Scanner scanner = new Scanner(System.in);
             NetworkClient client = new NetworkClient(host, port)) {

            runConsole(scanner, client);

        } catch (Exception e) {
            System.err.println("Client Error: " + e.getMessage());
        }
        System.out.println("Client application closed.");
    }

    void runConsole(Scanner scanner, NetworkClient client) {
        System.out.println("Connected to server (" + host + ":" + port + ")");

        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) break;

            String input = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input) || "disconnect".equalsIgnoreCase(input)) {
                client.sendRequest("disconnect");
                break;
            }

            String response;
            if (input.startsWith("get-food-by-barcode")) {
                if (!handleBarcodeCommand(input, client)) {
                    break;
                }
            } else {
                response = client.sendRequest(input);
                System.out.println(response);

                if (isConnectionLost(response)) break;
            }
        }
    }

    private boolean handleBarcodeCommand(String input, NetworkClient client) {
        String code = null;
        String imgPath = null;

        String[] parts = input.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String part : parts) {
            part = part.trim().replace("\"", "");
            if (part.startsWith("--code=")) code = part.substring("--code=".length());
            else if (part.startsWith("--img=")) imgPath = part.substring("--img=".length());
        }

        try {
            if (code != null && !code.isEmpty()) {
                String response = client.sendRequest("get-food-by-barcode " + code);
                System.out.println(response);

                if (isConnectionLost(response)) return false;

                boolean notFound = response.toLowerCase().contains("not found") || response.toLowerCase().contains("error");

                if (!notFound) return true;

                if (imgPath != null && !imgPath.isEmpty()) {
                    System.out.println(">>> Attempting fallback with image...");
                } else {
                    return true;
                }
            }

            if (imgPath != null && !imgPath.isEmpty()) {
                File imageFile = new File(imgPath);
                if (!imageFile.exists()) {
                    System.err.println("Error: Image file not found: " + imgPath);
                    return true;
                }

                System.out.println("[Client] Decoding barcode from image...");
                String decodedCode = BarcodeUtils.decodeBarcode(imageFile);
                System.out.println("[Client] Decoded code: " + decodedCode);

                String response = client.sendRequest("get-food-by-barcode " + decodedCode);
                System.out.println(response);

                return !isConnectionLost(response);
            }

        } catch (Exception e) {
            System.err.println("Error processing barcode command: " + e.getMessage());
        }
        return true;
    }

    // Helps discovering an error from NetworkClient
    private boolean isConnectionLost(String response) {
        return response.startsWith("Connection lost") ||
            response.startsWith("Disconnected") ||
            response.contains("shutting down");
    }
}