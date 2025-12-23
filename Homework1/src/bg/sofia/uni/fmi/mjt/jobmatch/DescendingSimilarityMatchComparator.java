package bg.sofia.uni.fmi.mjt.jobmatch;

import bg.sofia.uni.fmi.mjt.jobmatch.model.match.CandidateSimilarityMatch;

import java.util.Comparator;

public class DescendingSimilarityMatchComparator implements Comparator<CandidateSimilarityMatch> {
    @Override
    public int compare(CandidateSimilarityMatch o1, CandidateSimilarityMatch o2) {
        int compareValue = Double.compare(o2.getSimilarityScore(), o1.getSimilarityScore());
        if (compareValue != 0) {
            return compareValue;
        }
        return o1.getSimilarCandidate().getName().compareTo(o2.getSimilarCandidate().getName());
    }
}
