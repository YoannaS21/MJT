package bg.sofia.uni.fmi.mjt.show.elimination;

import bg.sofia.uni.fmi.mjt.show.ergenka.Ergenka;
import bg.sofia.uni.fmi.mjt.show.ergenka.HumorousErgenka;
import bg.sofia.uni.fmi.mjt.show.ergenka.RomanticErgenka;

import java.util.Arrays;

public class LowestRatingEliminationRule implements EliminationRule {
    @Override
    public Ergenka[] eliminateErgenkas(Ergenka[] ergenkas) {
        if (ergenkas == null || ergenkas.length == 0) {
            return new Ergenka[0];
        }

        Integer minRating = null;
        for (Ergenka ergenka : ergenkas) {
            if (ergenka != null) {
                if (minRating == null || ergenka.getRating() < minRating) {
                    minRating = ergenka.getRating();
                }
            }
        }

        if (minRating == null) {
            return new Ergenka[0];
        }

        int count = 0;
        for (Ergenka ergenka : ergenkas) {
            if (ergenka != null && ergenka.getRating() > minRating) {
                count++;
            }
        }

        Ergenka[] newErgenkas = new Ergenka[count];
        int index = 0;
        for (Ergenka ergenka : ergenkas) {
            if (ergenka != null && ergenka.getRating() > minRating) {
                newErgenkas[index++] = ergenka;
            }
        }

        return newErgenkas;
    }
}
