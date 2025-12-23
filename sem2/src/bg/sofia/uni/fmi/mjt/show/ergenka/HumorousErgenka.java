package bg.sofia.uni.fmi.mjt.show.ergenka;

import bg.sofia.uni.fmi.mjt.show.date.DateEvent;

import java.util.Objects;

public class HumorousErgenka implements Ergenka {
    private String name;
    private short age;
    private int romanceLevel;
    private int humorLevel;
    private int rating;

    public HumorousErgenka(String name, short age, int romanceLevel,
                           int humorLevel, int rating) {
        this.name = name;
        this.age = age;
        this.romanceLevel = romanceLevel;
        this.humorLevel = humorLevel;
        this.rating = rating;
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
        int duration = dateEvent.getDuration();
        if (duration >= 30 && duration <= 90) {
            bonus += 4;
        } else if (duration < 30) {
            bonus -= 2;
        } else if (duration > 90) {
            bonus -= 3;
        }

        rating = (humorLevel * 5) / dateEvent.getTensionLevel() + Math.floorDiv(romanceLevel, 3) + bonus;
    }
}
