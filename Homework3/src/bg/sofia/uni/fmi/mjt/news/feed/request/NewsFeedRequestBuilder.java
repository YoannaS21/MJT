package bg.sofia.uni.fmi.mjt.news.feed.request;

public class NewsFeedRequestBuilder {
    private final String keywords;
    private String category;
    private String country;
    private int page = 1;

    public NewsFeedRequestBuilder(String keywords) {
        if (keywords == null || keywords.isBlank()) {
            throw new IllegalArgumentException("Keywords are mandatory for search.");
        }
        this.keywords = keywords;
    }

    public NewsFeedRequestBuilder setCategory(String category) {
        this.category = category;
        return this;
    }

    public NewsFeedRequestBuilder setCountry(String country) {
        this.country = country;
        return this;
    }

    public NewsFeedRequestBuilder setPage(int page) {
        if (page <= 0) {
            throw new IllegalArgumentException("Page number must be positive.");
        }
        this.page = page;
        return this;
    }

    public NewsFeedRequest build() {
        return new NewsFeedRequest(keywords, category, country, page);
    }

}
