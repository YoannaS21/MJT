package bg.sofia.uni.fmi.mjt.jobmatch.matching;

import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.Skill;

import java.util.Set;

/**
 * Strategy interface for calculating similarity between skill sets.
 * <p>
 * Different implementations use different algorithms to measure how well
 * a candidate's skills match job requirements. The strategy is stateless
 * and can be passed as a parameter to methods that need it.
 * <p>
 * This follows the Strategy Pattern - see:
 * https://refactoring.guru/design-patterns/strategy
 */
public interface SimilarityStrategy {

    /**
     * Calculates similarity score between two skill sets.
     *
     * @param candidateSkills The skills possessed by a candidate
     * @param jobSkills       The skills required by a job
     * @return Similarity score in range [0, 1], where 1 means perfect match and 0 means no match
     * @throws IllegalArgumentException if either parameter is null
     */
    double calculateSimilarity(Set<Skill> candidateSkills, Set<Skill> jobSkills);

}