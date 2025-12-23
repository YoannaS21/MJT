package bg.sofia.uni.fmi.mjt.jobmatch;

import bg.sofia.uni.fmi.mjt.jobmatch.model.match.CandidateJobMatch;

import java.util.Comparator;

public class DescendingSimilarityNameComparator implements Comparator<CandidateJobMatch> {
    @Override
    public int compare(CandidateJobMatch o1, CandidateJobMatch o2) {
        int compareValue = Double.compare(o2.getSimilarityScore(), o1.getSimilarityScore());
        if (compareValue != 0) {
            return compareValue;
        }
        return o1.getCandidate().getName().compareTo(o2.getCandidate().getName());
    }
}
