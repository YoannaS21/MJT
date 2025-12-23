package bg.sofia.uni.fmi.mjt.jobmatch.model.match;

import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.Candidate;
import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.JobPosting;

public class CandidateJobMatch {
    private Candidate candidate;
    private JobPosting jobPosting;
    private double similarityScore;

    public CandidateJobMatch(Candidate candidate, JobPosting jobPosting,
                             double similarityScore) {
        if (similarityScore < 0 || similarityScore > 1) {
            throw new IllegalArgumentException("Similarity score should in interval [0,1]");
        }
        this.candidate = candidate;
        this.jobPosting = jobPosting;
        this.similarityScore = similarityScore;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public JobPosting getJobPosting() {
        return jobPosting;
    }

    @Override
    public String toString() {
        return "CandidateJobMatch{" +
                "candidate=" + candidate +
                ", jobPosting=" + jobPosting +
                ", similarityScore=" + similarityScore +
                '}';
    }
}
