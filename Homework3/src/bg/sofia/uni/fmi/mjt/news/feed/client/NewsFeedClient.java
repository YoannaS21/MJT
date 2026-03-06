package bg.sofia.uni.fmi.mjt.news.feed.client;

import bg.sofia.uni.fmi.mjt.news.feed.exception.BadRequestException;
import bg.sofia.uni.fmi.mjt.news.feed.exception.ExceededRateLimitException;
import bg.sofia.uni.fmi.mjt.news.feed.exception.InvalidApiKeyException;
import bg.sofia.uni.fmi.mjt.news.feed.exception.NewsFeedClientException;
import bg.sofia.uni.fmi.mjt.news.feed.models.ApiErrorResponse;
import bg.sofia.uni.fmi.mjt.news.feed.models.Article;
import bg.sofia.uni.fmi.mjt.news.feed.models.ResponseNewsFeed;
import bg.sofia.uni.fmi.mjt.news.feed.request.NewsFeedRequest;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class NewsFeedClient {
    private static final String API_ENDPOINT = "https://newsapi.org/v2/top-headlines";
    private static final int VALID_STATUS_CODE = 200;
    private static final int BAD_REQUEST = 400;
    private static final int UNAUTORIZED = 401;
    private static final int TOO_MANY_REQUESTS = 429;

    private final HttpClient httpClient;
    private final String apiKey;
    private final Gson gson;

    public NewsFeedClient(HttpClient httpClient, String apiKey) {
        this.httpClient = httpClient;
        this.apiKey = apiKey;
        this.gson = new Gson();
    }

    public List<Article> search(NewsFeedRequest request) throws NewsFeedClientException {
        String uri = request.buildUri(API_ENDPOINT);

        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .header("X-Api-Key", apiKey) // Security: Ключът се предава в хедъра
            .GET()
            .build();

        try {
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != VALID_STATUS_CODE) {
                handleErrorResponse(response);
            }

            ResponseNewsFeed newsResponse = gson.fromJson(response.body(), ResponseNewsFeed.class);
            return newsResponse.articles();

        } catch (IOException | InterruptedException e) {
            throw new NewsFeedClientException("Unexpected error occured", e);
        }
    }

    private void handleErrorResponse(HttpResponse<String> response) throws NewsFeedClientException {
        ApiErrorResponse error = gson.fromJson(response.body(), ApiErrorResponse.class);
        String message = (error != null) ? error.message() : "No message";

        switch (response.statusCode()) {
            case BAD_REQUEST -> throw new BadRequestException("Bad request: " + message);
            case UNAUTORIZED -> throw new InvalidApiKeyException("Invalid API Key: " + message);
            case TOO_MANY_REQUESTS -> throw new ExceededRateLimitException("Rate limit reached: " + message);
            default -> throw new NewsFeedClientException("API Error " + response.statusCode() + " " + message);
        }
    }
}
