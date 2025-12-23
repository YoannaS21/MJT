package bg.sofia.uni.fmi.mjt.show;

import bg.sofia.uni.fmi.mjt.show.date.DateEvent;
import bg.sofia.uni.fmi.mjt.show.elimination.EliminationRule;
import bg.sofia.uni.fmi.mjt.show.elimination.LowestRatingEliminationRule;
import bg.sofia.uni.fmi.mjt.show.ergenka.Ergenka;

public class ShowAPIImpl implements ShowAPI {

    private Ergenka[] ergenkas = null;
    private EliminationRule[] defaultEliminationRules = null;

    public ShowAPIImpl(Ergenka[] ergenkas, EliminationRule[] defaultEliminationRules) {
        this.defaultEliminationRules = defaultEliminationRules;
        this.ergenkas = ergenkas;
    }

    @Override
    public Ergenka[] getErgenkas() {
        return ergenkas;
    }

    @Override
    public void playRound(DateEvent dateEvent) {
        if(dateEvent == null || ergenkas == null){
            return;
        }
        for (Ergenka ergenka : ergenkas) {
            ergenka.reactToDate(dateEvent);
        }
    }

    @Override
    public void eliminateErgenkas(EliminationRule[] eliminationRules) {
        if (eliminationRules == null || eliminationRules.length == 0) {
            if (defaultEliminationRules != null && defaultEliminationRules.length > 0) {
                for (EliminationRule rule : defaultEliminationRules) {
                    ergenkas = rule.eliminateErgenkas(ergenkas);
                }
            }
            else {
                LowestRatingEliminationRule rule = new LowestRatingEliminationRule();
                ergenkas = rule.eliminateErgenkas(ergenkas);
            }
        } else {
            for (EliminationRule rule : eliminationRules) {
                ergenkas = rule.eliminateErgenkas(ergenkas);
            }
        }
    }

    @Override
    public void organizeDate(Ergenka ergenka, DateEvent dateEvent) {
        for (Ergenka currentErgenka : ergenkas) {
            if (currentErgenka.equals(ergenka)) {
                currentErgenka.reactToDate(dateEvent);
            }
        }
    }
}
