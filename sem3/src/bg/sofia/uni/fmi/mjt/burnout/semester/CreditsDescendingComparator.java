package bg.sofia.uni.fmi.mjt.burnout.semester;

import bg.sofia.uni.fmi.mjt.burnout.subject.UniversitySubject;

import java.util.Comparator;

public class CreditsDescendingComparator implements Comparator<UniversitySubject> {
    @Override
    public int compare(UniversitySubject o1, UniversitySubject o2) {
        return Integer.compare(o2.credits(), o1.credits());
    }
}
