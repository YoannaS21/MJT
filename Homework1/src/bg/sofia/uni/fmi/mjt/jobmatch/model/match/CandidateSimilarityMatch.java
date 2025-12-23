package bg.sofia.uni.fmi.mjt.jobmatch.model.match;

import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.Candidate;

public class CandidateSimilarityMatch {
    private Candidate targetCandidate;
    private Candidate similarCandidate;
    private double similarityScore;

    public CandidateSimilarityMatch(Candidate targetCandidate,
                                    Candidate similarCandidate, double similarityScore) {
        if (similarityScore < 0 || similarityScore > 1) {
            throw new IllegalArgumentException("Similarity score should in interval [0,1]");
        }
        this.targetCandidate = targetCandidate;
        this.similarCandidate = similarCandidate;
        this.similarityScore = similarityScore;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public Candidate getTargetCandidate() {
        return targetCandidate;
    }

    public Candidate getSimilarCandidate() {
        return similarCandidate;
    }

    @Override
    public String toString() {
        return "CandidateSimilarityMatch{" +
                "targetCandidate=" + targetCandidate +
                ", similarCandidate=" + similarCandidate +
                ", similarityScore=" + similarityScore +
                '}';
    }
}
