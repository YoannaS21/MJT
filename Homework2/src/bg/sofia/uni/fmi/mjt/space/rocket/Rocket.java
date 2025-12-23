package bg.sofia.uni.fmi.mjt.space.rocket;

import java.util.Optional;

public record Rocket(String id, String name, Optional<String> wiki, Optional<Double> height) {
    public Rocket {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (wiki == null) {
            throw new IllegalArgumentException("wiki cannot be null");
        }
        if (height == null) {
            throw new IllegalArgumentException("height cannot be null");
        }
    }
}

