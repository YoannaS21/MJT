package bg.sofia.uni.fmi.mjt.news.feed.request;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class NewsFeedRequest {
    private final String keywords;
    private final String category;
    private final String country;
    private final int page;

    NewsFeedRequest(String keywords, String category, String country, int page) {
        this.keywords = keywords;
        this.category = category;
        this.country = country;
        this.page = page;
    }

    public String buildUri(String baseUrl) {
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append("?q=").append(URLEncoder.encode(keywords, StandardCharsets.UTF_8));

        if (category != null) sb.append("&category=").append(category);
        if (country != null) sb.append("&country=").append(country);
        sb.append("&page=").append(page);

        return sb.toString();
    }

    public static NewsFeedRequestBuilder newBuilder(String keywords) {
        return new NewsFeedRequestBuilder(keywords);
    }

    public int getPage() {
        return page;
    }

    public String getCountry() {
        return country;
    }

    public String getCategory() {
        return category;
    }

    public String getKeywords() {
        return keywords;
    }
}