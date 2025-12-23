package bg.sofia.uni.fmi.mjt.jobmatch.matching;

import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.Skill;

import java.util.HashSet;
import java.util.Set;

public class JaccardSimilarity implements SimilarityStrategy {
    public JaccardSimilarity() {
    }

    /**
     * Calculates similarity score between two skill sets.
     *
     * @param candidateSkills The skills possessed by a candidate
     * @param jobSkills       The skills required by a job
     * @return Similarity score in range [0, 1], where 1 means perfect match and 0 means no match
     * @throws IllegalArgumentException if either parameter is null
     */
    @Override
    public double calculateSimilarity(Set<Skill> candidateSkills, Set<Skill> jobSkills) {
        if (candidateSkills == null || jobSkills == null) {
            throw new IllegalArgumentException("Candidate or job skills set is null.");
        }
        if (candidateSkills.isEmpty() || jobSkills.isEmpty()) {
            return 0;
        }

        Set<Skill> union = new HashSet<>(candidateSkills);
        union.addAll(jobSkills);

        Set<Skill> intersection = new HashSet<>(candidateSkills);
        intersection.retainAll(jobSkills);

        int elementsInIntersection = intersection.size();
        int elementsInUnion = union.size();
        return (double) elementsInIntersection / elementsInUnion;
    }
}
