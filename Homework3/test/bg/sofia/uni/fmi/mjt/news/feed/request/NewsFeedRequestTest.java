package bg.sofia.uni.fmi.mjt.news.feed.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class NewsFeedRequestTest {

    private static final String BASE_URL = "https://newsapi.org/v2/top-headlines";

    @Test
    void testBuildUriEncodesKeywordsCorrectly() {
        NewsFeedRequest request = NewsFeedRequest.newBuilder("space x+")
            .build();

        String uri = request.buildUri(BASE_URL);

        // Интервалът става +, а плюсът става %2B (или интервалът става %20)
        // StandardCharsets.UTF_8 обикновено прави интервала на '+'
        assertTrue(uri.contains("q=space+x%2B") || uri.contains("q=space%20x%2B"),
            "Keywords should be URL encoded");
    }

    @Test
    void testBuildUriWithOnlyRequiredFields() {
        NewsFeedRequest request = NewsFeedRequest.newBuilder("java").build();
        String uri = request.buildUri(BASE_URL);

        assertTrue(uri.contains("q=java"));
        assertTrue(uri.contains("page="));
        assertFalse(uri.contains("category="), "Category should not be present if null");
        assertFalse(uri.contains("country="), "Country should not be present if null");
    }

    @Test
    void testBuildUriWithAllFields() {
        NewsFeedRequest request = NewsFeedRequest.newBuilder("health")
            .setCategory("business")
            .setCountry("bg")
            .setPage(3)
            .build();

        String uri = request.buildUri(BASE_URL);

        assertTrue(uri.startsWith(BASE_URL + "?"));
        assertTrue(uri.contains("q=health"));
        assertTrue(uri.contains("&category=business"));
        assertTrue(uri.contains("&country=bg"));
        assertTrue(uri.contains("&page=3"));
    }

    @Test
    void testBuildUriHasCorrectDelimiters() {
        NewsFeedRequest request = NewsFeedRequest.newBuilder("java")
            .setCountry("us")
            .build();

        String uri = request.buildUri(BASE_URL);

        assertTrue(uri.contains("q=java&country=us"), "Parameters should be separated by '&'");
    }

}