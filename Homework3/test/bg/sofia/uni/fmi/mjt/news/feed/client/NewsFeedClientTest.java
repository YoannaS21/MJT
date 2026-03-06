package bg.sofia.uni.fmi.mjt.news.feed.client;

import bg.sofia.uni.fmi.mjt.news.feed.exception.BadRequestException;
import bg.sofia.uni.fmi.mjt.news.feed.exception.ExceededRateLimitException;
import bg.sofia.uni.fmi.mjt.news.feed.exception.InvalidApiKeyException;
import bg.sofia.uni.fmi.mjt.news.feed.exception.NewsFeedClientException;
import bg.sofia.uni.fmi.mjt.news.feed.request.NewsFeedRequest;
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
public class NewsFeedClientTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private NewsFeedClient client;
    private static final String API_KEY = "test-key";

    @BeforeEach
    void setUp() {
        client = new NewsFeedClient(mockHttpClient, API_KEY);
    }

    @Test
    void testSearchReturnsArticlesOnSuccess() throws IOException, InterruptedException, NewsFeedClientException {
        String jsonResponse = "{ \"status\": \"ok\", \"articles\": [ { \"title\": \"Gymnastics\" } ] }";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        NewsFeedRequest request = NewsFeedRequest.newBuilder("java").build();

        var result = client.search(request);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Gymnastics", result.get(0).title());
    }

    @Test
    void testSearchThrowsInvalidApiKeyExceptionOn401() throws IOException, InterruptedException {
        String errorJson = "{ \"code\": \"apiKeyInvalid\", \"message\": \"Your API key is invalid.\" }";

        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn(errorJson);
        when(mockHttpClient.send(any(), any())).thenReturn((HttpResponse) mockResponse);

        assertThrows(InvalidApiKeyException.class, () -> client.search(NewsFeedRequest.newBuilder("test").build()));
    }

    @Test
    void testSearchThrowsExceededRateLimitExceptionOn429() throws IOException, InterruptedException {
        String errorJson = "{ \"code\": \"rateLimited\", \"message\": \"Too many requests.\" }";

        when(mockResponse.statusCode()).thenReturn(429);
        when(mockResponse.body()).thenReturn(errorJson);
        when(mockHttpClient.send(any(), any())).thenReturn((HttpResponse) mockResponse);

        assertThrows(ExceededRateLimitException.class, () -> client.search(NewsFeedRequest.newBuilder("test").build()));
    }

    @Test
    void testSearchThrowsBadRequestExceptionOn400() throws IOException, InterruptedException {
        String errorJson = "{ \"code\": \"parameterInvalid\", \"message\": \"Missing query parameter.\" }";

        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn(errorJson);
        when(mockHttpClient.send(any(), any())).thenReturn((HttpResponse) mockResponse);

        assertThrows(BadRequestException.class, () -> client.search(NewsFeedRequest.newBuilder("test").build()));
    }

    @Test
    void testSearchThrowsNewsFeedClientExceptionOnNetworkError() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(), any())).thenThrow(new IOException("Connection reset"));

        assertThrows(NewsFeedClientException.class, () -> client.search(NewsFeedRequest.newBuilder("test").build()));
    }
}