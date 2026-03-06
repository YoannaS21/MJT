package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.FoodItem;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.Nutrient;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.SearchResult;
import bg.sofia.uni.fmi.mjt.foodanalyzer.exception.ExternalApiException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.exception.FoodAnalyzerException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.exception.FoodNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final HttpService httpService;
    private final CacheService cacheService;
    private final List<ClientHandler> activeClients;
    private PrintWriter writer;

    public ClientHandler(Socket socket, HttpService httpService, CacheService cacheService,
                         List<ClientHandler> activeClients) {
        this.socket = socket;
        this.httpService = httpService;
        this.cacheService = cacheService;
        this.activeClients = activeClients;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            this.writer = out;
            String inputLine;
            while (!socket.isClosed() && (inputLine = reader.readLine()) != null) {
                String response = processCommand(inputLine);
                writer.println(response);
                writer.println("<END>");
            }
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("Connection reset")) {
                System.out.println("Client disconnected abruptly.");
            } else if (socket.isClosed()) {
                System.out.println("Socket closed by server shutdown.");
            } else {
                Logger.logError("Communication error with client", e);
            }
        } finally {
            cleanup();
        }
    }

    public void stopGracefully() {
        if (writer != null) {
            writer.println("Server is shutting down. Your connection will be closed.");
            writer.println("<END>");
        }
        cleanup();
    }

    private void cleanup() {
        activeClients.remove(this);
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            Logger.logError("Error closing client socket", e);
        }
    }

    private String processCommand(String commandLine) {
        String[] parts = commandLine.trim().split("\\s+", 2);
        String command = parts[0];
        String arg = parts.length > 1 ? parts[1] : "";

        try {
            return switch (command) {
                case "get-food" -> handleGetFood(arg);
                case "get-food-report" -> handleGetFoodReport(arg);
                case "get-food-by-barcode" -> handleGetFoodByBarcode(arg);
                case "disconnect" -> "Goodbye!";
                default -> "Unknown command";
            };
        } catch (FoodNotFoundException e) {
            return "Error: " + e.getMessage();
        } catch (ExternalApiException e) {
            Logger.logError("External API failure for command: " + commandLine, e);
            return "Error: External service unavailable. Please try again later.";

        } catch (FoodAnalyzerException e) {
            Logger.logError("Business logic error", e);
            return "Error: " + e.getMessage();

        } catch (Exception e) {
            Logger.logError("Unexpected error processing command: " + commandLine, e);
            return "Server Error: Something went wrong.";
        }
    }

    private String handleGetFood(String query) throws FoodAnalyzerException {
        StringBuilder sb = new StringBuilder();

        // 1. Checks the Cache (search/query.txt -> products/id.json)
        List<FoodItem> cachedResults = cacheService.getSearchResult(query);

        if (cachedResults != null && !cachedResults.isEmpty()) {
            sb.append("[CACHE] Results for '").append(query).append("':\n");
            for (FoodItem item : cachedResults) {
                sb.append(item.toString()).append("\n");
            }
            return sb.toString();
        }

        // 2. We get the from the API
        SearchResult result = httpService.searchFood(query);

        // 3. We cache the result
        cacheService.cacheSearchResult(query, result.getFoods());

        sb.append("[API] Results for '").append(query).append("':\n");
        for (FoodItem item : result.getFoods()) {
            sb.append(item.toString()).append("\n");
        }
        return sb.toString();
    }

    private String handleGetFoodReport(String fdcIdStr) throws FoodAnalyzerException {
        int fdcId;
        try {
            fdcId = Integer.parseInt(fdcIdStr);
        } catch (NumberFormatException e) {
            return "Invalid ID format. Please provide a number.";
        }

        if (cacheService.hasProduct(fdcId)) {
            return formatReport(cacheService.getProduct(fdcId));
        }

        FoodItem item = httpService.getFoodDetails(fdcId);

        cacheService.cacheProduct(item);

        return formatReport(item);
    }

    private String handleGetFoodByBarcode(String barcodeArg) {
        String gtin = barcodeArg.trim();

        if (gtin.isEmpty()) {
            return "Error: No barcode provided.";
        }

        FoodItem cachedItem = cacheService.getProductByBarcode(gtin);

        if (cachedItem != null) {
            return formatReport(cachedItem);
        }

        return "Product with barcode " + gtin + " not found.";
    }

    private String formatReport(FoodItem item) {
        StringBuilder sb = new StringBuilder();

        String name = item.getDescription() != null ? item.getDescription() : "N/A";
        String brand = item.getBrandOwner() != null ? item.getBrandOwner() : "N/A";
        String upc = item.getGtinUpc() != null ? item.getGtinUpc() : "N/A";
        String ingredients = item.getIngredients() != null ? item.getIngredients() : "Not provided";

        sb.append("Name: ").append(name).append("\n");
        sb.append("Brand: ").append(brand).append("\n");
        sb.append("GTIN: ").append(upc).append("\n");
        sb.append("Ingredients: ").append(ingredients).append("\n");
        sb.append("Nutrients:\n");

        if (item.getNutrients() != null && !item.getNutrients().isEmpty()) {
            for (Nutrient n : item.getNutrients()) {
                String nutrientName = n.getNutrientName();
                if (nutrientName == null) nutrientName = "Unknown Nutrient";

                sb.append(" - ").append(nutrientName)
                    .append(": ").append(String.format("%.2f", n.getValue()))
                    .append(" ").append(n.getUnitName() != null ? n.getUnitName() : "")
                    .append("\n");
            }
        } else {
            sb.append(" - No nutrient information available.\n");
        }

        return sb.toString();
    }
}