package bg.sofia.uni.fmi.mjt.space.mission;

import bg.sofia.uni.fmi.mjt.space.rocket.RocketStatus;

import java.time.LocalDate;
import java.util.Optional;

public record Mission(String id, String company, String location,
                      LocalDate date, Detail detail, RocketStatus rocketStatus,
                      Optional<Double> cost, MissionStatus missionStatus) {
    public Mission {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }
        if (company == null) {
            throw new IllegalArgumentException("company cannot be null");
        }
        if (location == null) {
            throw new IllegalArgumentException("location cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("date cannot be null");
        }
        if (detail == null) {
            throw new IllegalArgumentException("detail cannot be null");
        }
        if (rocketStatus == null) {
            throw new IllegalArgumentException("rocketStatus cannot be null");
        }
        if (cost == null) {
            throw new IllegalArgumentException("cost cannot be null");
        }
        if (missionStatus == null) {
            throw new IllegalArgumentException("missionStatus cannot be null");
        }
    }
}
