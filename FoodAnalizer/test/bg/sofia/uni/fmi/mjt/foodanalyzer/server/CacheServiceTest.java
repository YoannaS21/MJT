package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.FoodItem;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.Nutrient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CacheServiceTest {
    @TempDir
    Path tempDir;

    private CacheService cacheService;
    private Gson gson;

    @BeforeEach
    void setUp() {
        cacheService = new CacheService(tempDir.toString());

        gson = new GsonBuilder()
            .registerTypeAdapter(Nutrient.class, new NutrientDeserializer())
            .create();
    }

    @Test
    void testInitCacheCreatesDirectories() {
        File productsDir = tempDir.resolve("products").toFile();
        File searchDir = tempDir.resolve("search").toFile();
        File barcodesDir = tempDir.resolve("barcodes").toFile();

        assertTrue(productsDir.exists(), "Products directory should exist");
        assertTrue(productsDir.isDirectory());

        assertTrue(searchDir.exists(), "Search directory should exist");
        assertTrue(barcodesDir.exists(), "Barcodes directory should exist");
    }

    @Test
    void testCacheAndRetrieveProduct() {
        String json = """
            {
                "fdcId": 123,
                "description": "Test Apple",
                "gtinUpc": "999888"
            }
            """;
        FoodItem item = gson.fromJson(json, FoodItem.class);

        cacheService.cacheProduct(item);

        File file = tempDir.resolve("products").resolve("123.json").toFile();
        assertTrue(file.exists(), "File 123.json should be created");

        FoodItem retrieved = cacheService.getProduct(123);

        assertNotNull(retrieved);
        assertEquals("Test Apple", retrieved.getDescription());
        assertEquals(123, retrieved.getFdcId());
    }

    @Test
    void testGetNonExistentProduct() {
        FoodItem item = cacheService.getProduct(99999);
        assertNull(item, "Should return null if file does not exist");
    }

    @Test
    void testBarcodeCachingAndRetrieval() {
        String json = """
            { "fdcId": 555, "description": "Milk", "gtinUpc": "112233" }
            """;
        FoodItem item = gson.fromJson(json, FoodItem.class);

        cacheService.cacheProduct(item);

        File barcodeFile = tempDir.resolve("barcodes").resolve("112233.txt").toFile();
        assertTrue(barcodeFile.exists(), "Barcode pointer file should exist");

        FoodItem retrieved = cacheService.getProductByBarcode("112233");

        assertNotNull(retrieved, "Should resolve product by barcode");
        assertEquals(555, retrieved.getFdcId());
        assertEquals("Milk", retrieved.getDescription());
    }

    @Test
    void testGetProductByNonExistentBarcode() {
        FoodItem item = cacheService.getProductByBarcode("000000");
        assertNull(item);
    }

    @Test
    void testCacheAndRetrieveSearchResult() {
        String json1 = "{ \"fdcId\": 10, \"description\": \"Green Apple\" }";
        String json2 = "{ \"fdcId\": 20, \"description\": \"Red Apple\" }";

        FoodItem item1 = gson.fromJson(json1, FoodItem.class);
        FoodItem item2 = gson.fromJson(json2, FoodItem.class);

        List<FoodItem> searchResults = List.of(item1, item2);

        cacheService.cacheSearchResult("apple", searchResults);

        File searchFile = tempDir.resolve("search").resolve("apple.txt").toFile();
        assertTrue(searchFile.exists());

        List<FoodItem> retrievedList = cacheService.getSearchResult("apple");

        assertNotNull(retrievedList);
        assertEquals(2, retrievedList.size());

        assertEquals("Green Apple", retrievedList.get(0).getDescription());
        assertEquals("Red Apple", retrievedList.get(1).getDescription());
    }

    @Test
    void testCacheSearchResultWithSpecialCharacters() {
        String query = "ben & jerry's / ice cream";

        cacheService.cacheSearchResult(query, List.of());
        String expectedName = "ben___jerry_s___ice_cream.txt";

        File file = tempDir.resolve("search").resolve(expectedName).toFile();
        assertTrue(file.exists(), "Should sanitize filename");
    }
}