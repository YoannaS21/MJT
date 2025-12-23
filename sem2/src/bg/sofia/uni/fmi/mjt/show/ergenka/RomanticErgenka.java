package bg.sofia.uni.fmi.mjt.show.ergenka;

import bg.sofia.uni.fmi.mjt.show.date.DateEvent;

import java.util.Objects;

public class RomanticErgenka implements Ergenka {

    private String name;
    private short age;
    private int romanceLevel;
    private int humorLevel;
    private int rating;
    private String favouriteDateLocation;

    public RomanticErgenka(String name, short age,
                           int romanceLevel,
                           int humorLevel, int rating,
                           String favouriteDateLocation) {
        this.name = name;
        this.age = age;
        this.romanceLevel = romanceLevel;
        this.humorLevel = humorLevel;
        this.rating = rating;
        this.favouriteDateLocation = favouriteDateLocation;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public short getAge() {
        return age;
    }

    @Override
    public int getHumorLevel() {
        return humorLevel;
    }

    @Override
    public int getRomanceLevel() {
        return romanceLevel;
    }

    @Override
    public int getRating() {
        return rating;
    }

    @Override
    public void reactToDate(DateEvent dateEvent) {
        if (dateEvent == null) {
            return;
        }
        int bonus = 0;
        if (favouriteDateLocation != null && dateEvent.getLocation() != null) {
            if (favouriteDateLocation.equalsIgnoreCase(dateEvent.getLocation())) {
                bonus += 5;
            }
        }
        if (dateEvent.getDuration() < 30) {
            bonus -= 3;
        } else if (dateEvent.getDuration() > 120) {
            bonus -= 2;
        }
        rating = (romanceLevel * 7) / dateEvent.getTensionLevel() + Math.floorDiv(humorLevel, 3) + bonus;
    }
}
