package bg.sofia.uni.fmi.mjt.eventbus.events;

public class SimplePayload implements Payload<String> {

    private final String data;

    public SimplePayload(String data) {
        if (data == null || data.isBlank()) {
            throw new IllegalArgumentException("Payload data cannot be null or blank");
        }
        this.data = data;
    }

    @Override
    public int getSize() {
        return data.length();
    }

    @Override
    public String getPayload() {
        return data;
    }

    @Override
    public String toString() {
        return data;
    }
}
