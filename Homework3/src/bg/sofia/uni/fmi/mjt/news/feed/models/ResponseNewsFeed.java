package bg.sofia.uni.fmi.mjt.news.feed.models;

import java.util.List;

public record ResponseNewsFeed(String status, String totalResults,
                               List<Article> articles, String code, String message) {
}
