package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.FoodItem;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.Nutrient;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.SearchResult;
import bg.sofia.uni.fmi.mjt.foodanalyzer.exception.ExternalApiException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.exception.FoodNotFoundException;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientHandlerTest {

    @Mock
    private Socket mockSocket;
    @Mock
    private HttpService mockHttpService;
    @Mock
    private CacheService mockCacheService;

    private List<ClientHandler> activeClients;
    private ByteArrayOutputStream outputStream;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() throws IOException {
        outputStream = new ByteArrayOutputStream();
        lenient().when(mockSocket.getOutputStream()).thenReturn(outputStream);
        activeClients = new ArrayList<>();
    }

    private void setInput(String command) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(command.getBytes());
        when(mockSocket.getInputStream()).thenReturn(inputStream);
    }

    @Test
    void testGetFoodFoundInCache() throws Exception {
        setInput("get-food apple" + System.lineSeparator());

        FoodItem cachedItem = gson.fromJson("{ \"fdcId\": 1, \"description\": \"Cached Apple\" }", FoodItem.class);
        when(mockCacheService.getSearchResult("apple")).thenReturn(List.of(cachedItem));

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        String output = outputStream.toString();
        assertTrue(output.contains("[CACHE]"), "Should indicate result is from cache");
        assertTrue(output.contains("Cached Apple"));
        verify(mockHttpService, never()).searchFood(any());
    }

    @Test
    void testGetFoodFoundInApi() throws Exception {
        setInput("get-food banana" + System.lineSeparator());

        when(mockCacheService.getSearchResult("banana")).thenReturn(null);

        String apiJson = "{ \"totalHits\": 1, \"foods\": [ { \"fdcId\": 2, \"description\": \"API Banana\" } ] }";
        SearchResult searchResult = gson.fromJson(apiJson, SearchResult.class);
        when(mockHttpService.searchFood("banana")).thenReturn(searchResult);

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        String output = outputStream.toString();
        assertTrue(output.contains("[API]"), "Should indicate result is from API");
        assertTrue(output.contains("API Banana"));

        verify(mockCacheService).cacheSearchResult(eq("banana"), any());
    }

    @Test
    void testGetFoodReportInvalidId() throws IOException {
        setInput("get-food-report banana" + System.lineSeparator());

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Invalid ID format"), "Should handle NumberFormatException");
    }

    @Test
    void testGetFoodReportFromCache() throws Exception {
        setInput("get-food-report 100" + System.lineSeparator());

        when(mockCacheService.hasProduct(100)).thenReturn(true);

        String json = """
            {
                "fdcId": 100,
                "description": "Tasty Cake",
                "ingredients": "Sugar, Flour",
                "foodNutrients": [
                    { "nutrientName": "Energy", "value": 500.0, "unitName": "KCAL" }
                ]
            }
            """;
        FoodItem item = gson.fromJson(json, FoodItem.class);
        when(mockCacheService.getProduct(100)).thenReturn(item);

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        String output = outputStream.toString();

        assertTrue(output.contains("Name: Tasty Cake"));
        assertTrue(output.contains("Ingredients: Sugar, Flour"));
        assertTrue(output.contains("Energy: 500.00 KCAL"));

        verify(mockHttpService, never()).getFoodDetails(anyInt());
    }

    @Test
    void testGetFoodReportFromApi() throws Exception {
        setInput("get-food-report 200" + System.lineSeparator());

        when(mockCacheService.hasProduct(200)).thenReturn(false);

        FoodItem item = gson.fromJson("{ \"fdcId\": 200, \"description\": \"API Cake\" }", FoodItem.class);
        when(mockHttpService.getFoodDetails(200)).thenReturn(item);

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Name: API Cake"));

        verify(mockCacheService).cacheProduct(item);
    }

    @Test
    void testFormatReportHandlesMissingData() throws Exception {
        setInput("get-food-report 300" + System.lineSeparator());
        when(mockCacheService.hasProduct(300)).thenReturn(false);

        FoodItem item = new FoodItem();
        when(mockHttpService.getFoodDetails(300)).thenReturn(item);

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        String output = outputStream.toString();

        assertTrue(output.contains("Name: N/A"));
        assertTrue(output.contains("Brand: N/A"));
        assertTrue(output.contains("No nutrient information available"));
    }


    @Test
    void testGetFoodByBarcodeFoundInCache() throws Exception {
        setInput("get-food-by-barcode 123456" + System.lineSeparator());

        FoodItem item = gson.fromJson("{ \"fdcId\": 55, \"description\": \"Barcode Milk\" }", FoodItem.class);
        when(mockCacheService.getProductByBarcode("123456")).thenReturn(item);

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Name: Barcode Milk"));
    }

    @Test
    void testGetFoodByBarcodeNotFoundInCache() throws Exception {
        setInput("get-food-by-barcode 999999" + System.lineSeparator());
        when(mockCacheService.getProductByBarcode("999999")).thenReturn(null);

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        String output = outputStream.toString();
        assertTrue(output.contains("Product with barcode 999999 not found"));

        verify(mockHttpService, never()).searchFood(any());
    }

    @Test
    void testGetFoodByBarcodeMissingArg() throws Exception {
        setInput("get-food-by-barcode   " + System.lineSeparator()); // само интервали

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        assertTrue(outputStream.toString().contains("Error: No barcode provided"));
    }

    @Test
    void testStopGracefullyCleanupOnly() throws IOException {
        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        activeClients.add(handler);

        handler.stopGracefully();

        assertFalse(activeClients.contains(handler), "Should remove itself from list");
        verify(mockSocket, atLeastOnce()).close();
    }

    @Test
    void testExternalApiException() throws Exception {
        setInput("get-food banana" + System.lineSeparator());
        when(mockCacheService.getSearchResult("banana")).thenReturn(null);
        when(mockHttpService.searchFood("banana")).thenThrow(new ExternalApiException("API Error"));

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        String output = outputStream.toString();
        assertTrue(output.contains("External service unavailable"));
    }

    @Test
    void testFoodNotFoundException() throws Exception {
        setInput("get-food unicorn" + System.lineSeparator());
        when(mockCacheService.getSearchResult("unicorn")).thenReturn(null);
        when(mockHttpService.searchFood("unicorn")).thenThrow(new FoodNotFoundException("Nothing found"));

        ClientHandler handler = new ClientHandler(mockSocket, mockHttpService, mockCacheService, activeClients);
        handler.run();

        assertTrue(outputStream.toString().contains("Error: Nothing found"));
    }
}