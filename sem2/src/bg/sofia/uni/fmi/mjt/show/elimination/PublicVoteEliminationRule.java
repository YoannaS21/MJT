package bg.sofia.uni.fmi.mjt.show.elimination;

import bg.sofia.uni.fmi.mjt.show.ergenka.Ergenka;
import bg.sofia.uni.fmi.mjt.show.ergenka.HumorousErgenka;
import bg.sofia.uni.fmi.mjt.show.ergenka.RomanticErgenka;

public class PublicVoteEliminationRule implements EliminationRule {
    private String[] votes;

    public PublicVoteEliminationRule(String[] votes) {
        this.votes = votes;
    }

    private String toEliminate() {
        if (votes == null || votes.length == 0) {
            return null;
        }

        String element = null;
        int count = 0;

        for (String vote : votes) {
            if (count == 0) {
                element = vote;
                count++;
            } else if (element != null && element.equals(vote)) {
                count++;
            } else {
                count--;
            }
        }

        int occ = 0;
        for (String vote : votes) {
            if (element != null && element.equals(vote)) {
                occ++;
            }
        }

        if (occ > votes.length / 2) {
            return element;
        } else {
            return null;
        }
    }

    @Override
    public Ergenka[] eliminateErgenkas(Ergenka[] ergenkas) {
        if (ergenkas == null || ergenkas.length == 0) {
            return new Ergenka[0];
        }

        String toEliminate = toEliminate();
        if (toEliminate == null) {
            return ergenkas;
        }

        boolean ergenkaExists = false;
        for (Ergenka e : ergenkas) {
            if (e != null && e.getName().equals(toEliminate)) {
                ergenkaExists = true;
            }
        }

        if (!ergenkaExists) {
            return ergenkas;
        }

        Ergenka[] newErgenkas = new Ergenka[ergenkas.length - 1];
        int index = 0;
        for (int i = 0; i < ergenkas.length; i++) {
            if (ergenkas[i] == null) {
                continue;
            }
            if (!(ergenkas[i].getName().equals(toEliminate))) {
                newErgenkas[index++] = ergenkas[i];
            }
        }
        return newErgenkas;
    }

    public static void main(String[] args) {
        RomanticErgenka a = new RomanticErgenka("Anna", (short) 27, 1, 5, 10, "Dubai");
        RomanticErgenka b = new RomanticErgenka("Deniz", (short) 30, 2, 8, 12, "Maldivi");
        HumorousErgenka c = new HumorousErgenka("Gabi", (short) 25, 0, 11, 10);

        Ergenka[] ergenkas = {a, b, c};
        String[] votes = {"Deniz", "Deniz", "Gabi", "Anna"};
        PublicVoteEliminationRule obj = new PublicVoteEliminationRule(votes);
        Ergenka[] newErg = obj.eliminateErgenkas(ergenkas);
        System.out.println("Остават:");
        for (Ergenka e : newErg) {
            System.out.println(e.getName());
        }
    }
}
