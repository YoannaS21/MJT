package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.FoodItem;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.Nutrient;
import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.SearchResult;
import bg.sofia.uni.fmi.mjt.foodanalyzer.exception.ExternalApiException;
import bg.sofia.uni.fmi.mjt.foodanalyzer.exception.FoodNotFoundException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class HttpService {
    private static final String API_KEY = System.getenv("USDA_API_KEY");
    private static final String API_BASE_URL = "https://api.nal.usda.gov/fdc/v1/";

    private static final int HTTP_OK = 200;
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final int HTTP_FORBIDDEN = 403;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_SERVER_ERROR = 500;

    private static final int DEFAULT_PAGE_SIZE = 25;

    private final HttpClient client;
    private final Gson gson;

    // For Testing
    public HttpService(HttpClient client) {
        this.client = client;
        this.gson = new GsonBuilder()
            .registerTypeAdapter(Nutrient.class, new NutrientDeserializer())
            .create();
    }

    public HttpService() {
        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IllegalStateException("Environment variable 'USDA_API_KEY' is missing!");
        }

        this.client = HttpClient.newHttpClient();

        this.gson = new GsonBuilder()
            .registerTypeAdapter(Nutrient.class, new NutrientDeserializer())
            .create();
    }

    public SearchResult searchFood(String query) throws ExternalApiException, FoodNotFoundException {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            String url = String.format("%sfoods/search?query=%s&requireAllWords=true&pageSize=%d&api_key=%s",
                API_BASE_URL, encodedQuery, DEFAULT_PAGE_SIZE, API_KEY);

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            handleErrorStatus(response.statusCode(), url);

            SearchResult result = gson.fromJson(response.body(), SearchResult.class);

            if (result.getFoods() == null || result.getFoods().isEmpty()) {
                throw new FoodNotFoundException("No food found for query: " + query);
            }

            return result;

        } catch (IOException | InterruptedException e) {
            Logger.logError("Network error while searching for: " + query, e);
            throw new ExternalApiException("Connection failed while searching", e);
        }
    }

    public FoodItem getFoodDetails(int fdcId) throws ExternalApiException, FoodNotFoundException {
        String url = String.format("%sfood/%d?api_key=%s", API_BASE_URL, fdcId, API_KEY);
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == HTTP_NOT_FOUND) {
                throw new FoodNotFoundException("ID " + fdcId + " not found in USDA database.");
            }

            handleErrorStatus(response.statusCode(), url);

            return gson.fromJson(response.body(), FoodItem.class);

        } catch (IOException | InterruptedException e) {
            Logger.logError("Network error while fetching details for ID: " + fdcId, e);
            throw new ExternalApiException("Connection failed while fetching details", e);
        }
    }

    private void handleErrorStatus(int statusCode, String url) throws ExternalApiException {
        if (statusCode == HTTP_OK) {
            return;
        }

        String errorMsg;
        if (statusCode == HTTP_UNAUTHORIZED || statusCode == HTTP_FORBIDDEN) {
            errorMsg = "Invalid API Key or Access Denied";
        } else if (statusCode >= HTTP_SERVER_ERROR) {
            errorMsg = "USDA Server is down (Code " + statusCode + ")";
        } else {
            errorMsg = "Unexpected error code: " + statusCode;
        }

        ExternalApiException exception = new ExternalApiException(errorMsg);

        Logger.logError("API Request failed. URL: " + url + " | Status: " + statusCode, exception);

        throw exception;
    }
}