package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.FoodItem;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.Nutrient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CacheService {
    private final String cacheDir;
    private final String productsDir;
    private final String searchDir;
    private final String barcodesDir;

    private final Gson gson;

    public CacheService() {
        this("cache");
    }

    public CacheService(String rootPath) {
        this.cacheDir = rootPath;
        this.productsDir = cacheDir + File.separator + "products";
        this.searchDir = cacheDir + File.separator + "search";
        this.barcodesDir = cacheDir + File.separator + "barcodes";

        this.gson = new GsonBuilder()
            .registerTypeAdapter(Nutrient.class, new NutrientDeserializer())
            .setPrettyPrinting()
            .create();

        initCache();
    }

    private void initCache() {
        new File(productsDir).mkdirs();
        new File(searchDir).mkdirs();
        new File(barcodesDir).mkdirs();
    }

    public void cacheProduct(FoodItem item) {
        if (item == null) return;

        String filename = productsDir + File.separator + item.getFdcId() + ".json";
        try (Writer writer = new BufferedWriter(new FileWriter(filename))) {
            gson.toJson(item, writer);
            writer.flush();
        } catch (IOException e) {
            Logger.logError("Failed to write product cache: " + item.getFdcId(), e);
        }

        if (item.getGtinUpc() != null && !item.getGtinUpc().isEmpty()) {
            cacheBarcodePointer(item.getGtinUpc(), item.getFdcId());
        }
    }

    public FoodItem getProduct(int fdcId) {
        File file = new File(productsDir + File.separator + fdcId + ".json");
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                return gson.fromJson(reader, FoodItem.class);
            } catch (IOException e) {
                Logger.logError("Failed to read cache file for ID: " + fdcId, e);
            }
        }
        return null;
    }

    public boolean hasProduct(int fdcId) {
        return new File(productsDir + File.separator + fdcId + ".json").exists();
    }

    private void cacheBarcodePointer(String gtin, int fdcId) {
        String filename = barcodesDir + File.separator + gtin + ".txt";
        try (Writer writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(String.valueOf(fdcId));
            writer.flush();
        } catch (IOException e) {
            Logger.logError("Error caching barcode index: " + gtin, e);
        }
    }

    public FoodItem getProductByBarcode(String gtin) {
        File pointerFile = new File(barcodesDir + File.separator + gtin + ".txt");
        if (pointerFile.exists()) {
            try {
                String content = Files.readString(pointerFile.toPath()).trim();
                int fdcId = Integer.parseInt(content);

                return getProduct(fdcId);
            } catch (Exception e) {
                Logger.logError("Error resolving barcode pointer: " + gtin, e);
            }
        }
        return null;
    }

    public void cacheSearchResult(String query, List<FoodItem> items) {
        String safeQuery = query.trim().replaceAll("[^a-zA-Z0-9]", "_");
        String filename = searchDir + File.separator + safeQuery + ".txt";

        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            for (FoodItem item : items) {
                writer.println(item.getFdcId());
                cacheProduct(item);
            }
            writer.flush();
        } catch (IOException e) {
            Logger.logError("Error caching search result for: " + query, e);
        }
    }

    public List<FoodItem> getSearchResult(String query) {
        String safeQuery = query.trim().replaceAll("[^a-zA-Z0-9]", "_");
        File indexFile = new File(searchDir + File.separator + safeQuery + ".txt");

        if (indexFile.exists()) {
            List<FoodItem> results = new ArrayList<>();
            try (Scanner scanner = new Scanner(indexFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (!line.isEmpty()) {
                        try {
                            int fdcId = Integer.parseInt(line);
                            FoodItem item = getProduct(fdcId);
                            if (item != null) {
                                results.add(item);
                            }
                        } catch (NumberFormatException nfe) {
                            // Ignore invalid lines
                        }
                    }
                }
                return results;
            } catch (Exception e) {
                Logger.logError("Error reading search cache for: " + query, e);
            }
        }
        return null;
    }
}