package bg.sofia.uni.fmi.mjt.news.feed.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class NewsFeedRequestBuilderTest {

    @Test
    void testBuilderSetsAllFieldsCorrectly() {
        NewsFeedRequest request = NewsFeedRequest.newBuilder("java")
            .setCountry("bg")
            .setCategory("technology")
            .setPage(2)
            .build();

        assertEquals("java", request.getKeywords(), "Keywords should match");
        assertEquals("bg", request.getCountry(), "Country should match");
        assertEquals("technology", request.getCategory(), "Category should match");
        assertEquals(2, request.getPage(), "Page number should match");
    }

    @Test
    void testBuilderThrowsExceptionWhenKeywordsAreNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            NewsFeedRequest.newBuilder(null).build();
        }, "Should throw IllegalArgumentException if keywords are null");
    }

    @Test
    void testBuilderThrowsExceptionWhenKeywordsAreEmpty() {
        assertThrows(IllegalArgumentException.class, () -> {
            NewsFeedRequest.newBuilder("").build();
        }, "Should throw IllegalArgumentException if keywords are empty");
    }

    @Test
    void testBuilderThrowsExceptionForInvalidPage() {
        assertThrows(IllegalArgumentException.class, () -> {
            NewsFeedRequest.newBuilder("java")
                .setPage(0) // Страниците обикновено започват от 1
                .build();
        }, "Should throw exception for page number less than 1");
    }

}