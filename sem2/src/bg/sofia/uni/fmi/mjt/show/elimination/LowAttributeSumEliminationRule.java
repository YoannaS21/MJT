package bg.sofia.uni.fmi.mjt.show.elimination;

import bg.sofia.uni.fmi.mjt.show.ergenka.Ergenka;
import bg.sofia.uni.fmi.mjt.show.ergenka.HumorousErgenka;
import bg.sofia.uni.fmi.mjt.show.ergenka.RomanticErgenka;

public class LowAttributeSumEliminationRule implements EliminationRule {

    private int threshold;

    public LowAttributeSumEliminationRule(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public Ergenka[] eliminateErgenkas(Ergenka[] ergenkas) {
        if(ergenkas == null || ergenkas.length == 0){
            return new Ergenka[0];
        }
        int badErgenkas = 0;
        for (int i = 0; i < ergenkas.length; i++) {
            if (ergenkas[i] == null) {
                continue;
            }
            if (ergenkas[i].getHumorLevel() + ergenkas[i].getRomanceLevel() < threshold) {
                badErgenkas++;
            }
        }

        Ergenka[] stillInTheGame = new Ergenka[ergenkas.length - badErgenkas];
        int index = 0;
        for (int i = 0; i < ergenkas.length; i++) {
            if (ergenkas[i] == null) {
                continue;
            }
            if (!(ergenkas[i].getHumorLevel() + ergenkas[i].getRomanceLevel() < threshold)) {
                stillInTheGame[index++] = ergenkas[i];
            }
        }
        return stillInTheGame;
    }

    public static void main(String[] args) {
        RomanticErgenka a = new RomanticErgenka("Anna", (short) 27, 1, 5, 10, "Dubai");
        RomanticErgenka b = new RomanticErgenka("Deniz", (short) 30, 2, 8, 12, "Maldivi");
        HumorousErgenka c = new HumorousErgenka("Gabi", (short) 25, 0, 11, 10);

        Ergenka[] ergenkas = {a, b, c};
        LowAttributeSumEliminationRule obj = new LowAttributeSumEliminationRule(11);
        Ergenka[] newErg = obj.eliminateErgenkas(ergenkas);
        System.out.println("Остават:");
        for (Ergenka e : newErg) {
            System.out.println(e.getName());
        }
    }
}


