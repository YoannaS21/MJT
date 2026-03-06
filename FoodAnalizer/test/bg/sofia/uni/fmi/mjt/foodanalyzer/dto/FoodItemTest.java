package bg.sofia.uni.fmi.mjt.foodanalyzer.dto;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.NutrientDeserializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodItemTest {

    private Gson gson;

    @BeforeEach
    void setUp() {
        gson = new GsonBuilder()
            .registerTypeAdapter(Nutrient.class, new NutrientDeserializer())
            .create();
    }

    @Test
    void testDeserializationWithFullData() {
        String json = """
            {
                "fdcId": 12345,
                "description": "Premium Cheese",
                "gtinUpc": "00112233",
                "brandOwner": "Milka",
                "ingredients": "Milk, Salt, Love",
                "foodNutrients": [
                    {
                        "nutrientName": "Protein",
                        "unitName": "G",
                        "value": 20.0
                    }
                ]
            }
            """;

        FoodItem item = gson.fromJson(json, FoodItem.class);

        assertNotNull(item, "Object should not be null");
        assertEquals(12345, item.getFdcId());
        assertEquals("Premium Cheese", item.getDescription());
        assertEquals("00112233", item.getGtinUpc());
        assertEquals("Milka", item.getBrandOwner());

        assertNotNull(item.getNutrients());
        assertEquals(1, item.getNutrients().size());
        assertEquals("Protein", item.getNutrients().get(0).getNutrientName());
    }

    @Test
    void testDeserializationWithMissingOptionalFields() {
        String json = """
            {
                "fdcId": 999,
                "description": "Raw Apple"
            }
            """;

        FoodItem item = gson.fromJson(json, FoodItem.class);

        assertEquals(999, item.getFdcId());
        assertEquals("Raw Apple", item.getDescription());

        assertNull(item.getGtinUpc());
        assertNull(item.getBrandOwner());
        assertNull(item.getIngredients());
    }

    @Test
    void testToStringContainsEssentialReportInfo() {
        String json = "{ \"fdcId\": 10, \"description\": \"Banana\", \"brandOwner\": \"Chiquita\" }";
        FoodItem item = gson.fromJson(json, FoodItem.class);

        String report = item.toString();

        assertTrue(report.contains("Banana"), "Report must contain product name");
        assertTrue(report.contains("Chiquita"), "Report must contain brand");
        assertTrue(report.contains("10"), "Report must contain ID");
    }

    @Test
    void testDeserializationWithNestedNutrientStructure() {
        String json = """
            {
                "fdcId": 555,
                "description": "Complex Steak",
                "foodNutrients": [
                    {
                        "type": "FoodNutrient",
                        "id": 1001,
                        "nutrient": {
                            "id": 1008,
                            "number": "208",
                            "name": "Energy",       
                            "unitName": "KCAL"    
                        },
                        "amount": 250.5           
                    }
                ]
            }
            """;

        FoodItem item = gson.fromJson(json, FoodItem.class);
        assertNotNull(item);
        assertEquals(555, item.getFdcId());

        assertNotNull(item.getNutrients());
        assertEquals(1, item.getNutrients().size());

        Nutrient n = item.getNutrients().get(0);

        assertEquals("Energy", n.getNutrientName());
        assertEquals("KCAL", n.getUnitName());
        assertEquals(250.5, n.getValue());
    }
}