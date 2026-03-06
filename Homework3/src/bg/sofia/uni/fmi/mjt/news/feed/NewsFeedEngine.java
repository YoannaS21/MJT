package bg.sofia.uni.fmi.mjt.news.feed;

import bg.sofia.uni.fmi.mjt.news.feed.client.NewsFeedClient;
import bg.sofia.uni.fmi.mjt.news.feed.exception.NewsFeedClientException;
import bg.sofia.uni.fmi.mjt.news.feed.models.Article;
import bg.sofia.uni.fmi.mjt.news.feed.request.NewsFeedRequest;

import java.net.http.HttpClient;
import java.util.List;

public class NewsFeedEngine {
    private final NewsFeedClient client;

    public NewsFeedEngine(NewsFeedClient client) {
        this.client = client;
    }

    public NewsFeedEngine() {
        String apiKey = System.getenv("NEWS_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Environment variable NEWS_API_KEY is not set.");
        }
        this.client = new NewsFeedClient(HttpClient.newHttpClient(), apiKey);
    }

    public List<Article> getNews(String keywords) throws NewsFeedClientException {
        return getNews(keywords, null, null, 1);
    }

    public List<Article> getNews(String keywords, int page) throws NewsFeedClientException {
        return getNews(keywords, null, null, page);
    }

    public List<Article> getNewsByCategory(String keywords, String category) throws NewsFeedClientException {
        return getNews(keywords, category, null, 1);
    }

    public List<Article> getNewsByCategory(String keywords, String category, int page) throws NewsFeedClientException {
        return getNews(keywords, category, null, page);
    }

    public List<Article> getNewsByCountry(String keywords, String country) throws NewsFeedClientException {
        return getNews(keywords, null, country, 1);
    }

    public List<Article> getNewsByCountry(String keywords, String country, int page) throws NewsFeedClientException {
        return getNews(keywords, null, country, page);
    }

    public List<Article> getNewsByCategoryAndCountry(String keywords, String category, String country)
        throws NewsFeedClientException {
        return getNews(keywords, category, country, 1);
    }

    public List<Article> getNews(String keywords, String category, String country, int page)
        throws NewsFeedClientException {

        NewsFeedRequest request = NewsFeedRequest.newBuilder(keywords)
            .setCategory(category)
            .setCountry(country)
            .setPage(page)
            .build();

        return client.search(request);
    }
}