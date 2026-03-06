package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.FoodItem;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.SearchResult;
import bg.sofia.uni.fmi.mjt.foodanalyzer.exception.ExternalApiException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.exception.FoodNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private HttpService httpService;

    @BeforeEach
    void setUp() {
        httpService = new HttpService(mockHttpClient);
    }

    @Test
    void testSearchFoodSuccess() throws Exception {
        String jsonResponse = """
            {
                "totalHits": 1,
                "foods": [ { "fdcId": 101, "description": "Apple" } ]
            }
            """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);

        when(mockHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn((HttpResponse) mockResponse);

        SearchResult result = httpService.searchFood("apple");

        assertNotNull(result);
        assertEquals(1, result.getFoods().size());
        assertEquals("Apple", result.getFoods().get(0).getDescription());
    }

    @Test
    void testSearchFoodNotFoundAnywhere() throws Exception {
        String jsonResponse = "{ \"foods\": [] }";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);

        when(mockHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn((HttpResponse) mockResponse);

        assertThrows(FoodNotFoundException.class, () -> httpService.searchFood("unicorn"));
    }

    @Test
    void testSearchFoodApiError() throws Exception {
        when(mockResponse.statusCode()).thenReturn(500);

        when(mockHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn((HttpResponse) mockResponse);

        assertThrows(ExternalApiException.class, () -> httpService.searchFood("apple"));
    }

    @Test
    void testSearchFoodNetworkFailure() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any()))
            .thenThrow(new IOException("No internet connection"));

        assertThrows(ExternalApiException.class, () -> httpService.searchFood("apple"));
    }

    @Test
    void testGetFoodDetailsSuccess() throws Exception {
        String jsonResponse = "{ \"fdcId\": 555, \"description\": \"Steak\" }";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);

        when(mockHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn((HttpResponse) mockResponse);

        FoodItem item = httpService.getFoodDetails(555);

        assertNotNull(item);
        assertEquals(555, item.getFdcId());
        assertEquals("Steak", item.getDescription());
    }

    @Test
    void testGetFoodDetailsNotFound() throws Exception {
        when(mockResponse.statusCode()).thenReturn(404);

        when(mockHttpClient.send(any(HttpRequest.class), any()))
            .thenReturn((HttpResponse) mockResponse);

        assertThrows(FoodNotFoundException.class, () -> httpService.getFoodDetails(999999));
    }
}