package bg.sofia.uni.fmi.mjt.space.mission;

public record Detail(String rocketName, String payload) {
    public Detail {
        if (rocketName == null) {
            throw new IllegalArgumentException("rocketName can't be null");
        }
        if (payload == null) {
            throw new IllegalArgumentException("payload can't be null");
        }
    }
}
