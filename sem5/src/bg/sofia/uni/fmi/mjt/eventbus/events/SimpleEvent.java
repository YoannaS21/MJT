package bg.sofia.uni.fmi.mjt.eventbus.events;

import java.time.Instant;

public class SimpleEvent implements Event<SimplePayload> {

    private final Instant timestamp;
    private final int priority;
    private final String source;
    private final SimplePayload payload;

    public SimpleEvent(String source, int priority, SimplePayload payload) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Source cannot be null or blank");
        }
        if (priority < 0) {
            throw new IllegalArgumentException("Priority must be >= 0");
        }
        if (payload == null) {
            throw new IllegalArgumentException("Payload cannot be null");
        }

        this.timestamp = Instant.now();
        this.priority = priority;
        this.source = source;
        this.payload = payload;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public SimplePayload getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "SimpleEvent{" +
                "timestamp=" + timestamp +
                ", priority=" + priority +
                ", source='" + source + '\'' +
                ", payload=" + payload +
                '}';
    }
}
