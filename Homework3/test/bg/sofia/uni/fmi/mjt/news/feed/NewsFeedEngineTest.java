package bg.sofia.uni.fmi.mjt.news.feed;

import bg.sofia.uni.fmi.mjt.news.feed.client.NewsFeedClient;
import bg.sofia.uni.fmi.mjt.news.feed.exception.NewsFeedClientException;
import bg.sofia.uni.fmi.mjt.news.feed.models.Article;
import bg.sofia.uni.fmi.mjt.news.feed.request.NewsFeedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewsFeedEngineTest {

    @Mock
    private NewsFeedClient mockClient;

    private NewsFeedEngine engine;

    @BeforeEach
    void setUp() {
        engine = new NewsFeedEngine(mockClient);
    }

    @Test
    void testGetNewsReturnsCorrectArticlesFromClient() throws NewsFeedClientException {
        Article a1 = new Article("Title 1", "Author 1", "2026", "Desc", "Content", "url1");
        Article a2 = new Article("Title 2", "Author 2", "2026", "Desc", "Content", "url2");
        List<Article> mockArticles = List.of(a1, a2);

        when(mockClient.search(any(NewsFeedRequest.class))).thenReturn(mockArticles);

        List<Article> result = engine.getNews("java");

        assertNotNull(result);
        assertEquals(2, result.size(), "Should return 2 articles");
        assertEquals("Title 1", result.get(0).title());
        assertSame(a2, result.get(1), "Objects should be the same");
    }

    @Test
    void testGetNewsReturnsEmptyListWhenNoArticlesFound() throws NewsFeedClientException {
        when(mockClient.search(any())).thenReturn(List.of());

        List<Article> result = engine.getNews("non-existent-keyword");

        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be an empty list");
    }

    @Test
    void testGetNewsByCountrySendsCorrectData() throws NewsFeedClientException {
        engine.getNewsByCountry("sofia", "bg");

        ArgumentCaptor<NewsFeedRequest> captor = ArgumentCaptor.forClass(NewsFeedRequest.class);
        verify(mockClient).search(captor.capture());

        String uri = captor.getValue().buildUri("http://api");
        assertTrue(uri.contains("country=bg"), "Engine трябва да подаде държава 'bg'");
        assertTrue(uri.contains("q=sofia"), "Engine трябва да подаде ключова дума 'sofia'");
    }

    @Test
    void testGetNewsByCategoryWithPageSendsCorrectData() throws NewsFeedClientException {
        engine.getNewsByCategory("future", "technology", 3);

        ArgumentCaptor<NewsFeedRequest> captor = ArgumentCaptor.forClass(NewsFeedRequest.class);
        verify(mockClient).search(captor.capture());

        String uri = captor.getValue().buildUri("http://api");
        assertTrue(uri.contains("category=technology"));
        assertTrue(uri.contains("page=3"), "Трябва да е изпратена страница 3");
    }

    @Test
    void testGetNewsByCategoryAndCountry() throws NewsFeedClientException {
        engine.getNewsByCategoryAndCountry("it", "science", "us");

        ArgumentCaptor<NewsFeedRequest> captor = ArgumentCaptor.forClass(NewsFeedRequest.class);
        verify(mockClient).search(captor.capture());

        String uri = captor.getValue().buildUri("http://api");
        assertTrue(uri.contains("category=science"));
        assertTrue(uri.contains("country=us"));
    }

    @Test
    void testGetNewsWithKeywordsAndPage() throws NewsFeedClientException {
        engine.getNews("weather", 10);

        ArgumentCaptor<NewsFeedRequest> captor = ArgumentCaptor.forClass(NewsFeedRequest.class);
        verify(mockClient).search(captor.capture());

        String uri = captor.getValue().buildUri("http://api");
        assertTrue(uri.contains("q=weather"));
        assertTrue(uri.contains("page=10"));
    }

    @Test
    void testGetNewsPropagatesClientException() throws NewsFeedClientException {
        when(mockClient.search(any())).thenThrow(new NewsFeedClientException("Unauthorized"));

        assertThrows(NewsFeedClientException.class, () -> engine.getNews("crypto"),
            "Engine should propagate client exceptions");
    }
}