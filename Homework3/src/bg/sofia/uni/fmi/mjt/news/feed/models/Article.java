package bg.sofia.uni.fmi.mjt.news.feed.models;

public record Article(String title, String author, String publishedAt,
                      String description, String content, String url) {
}

