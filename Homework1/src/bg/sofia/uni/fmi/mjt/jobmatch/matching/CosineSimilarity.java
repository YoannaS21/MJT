package bg.sofia.uni.fmi.mjt.jobmatch.matching;

import bg.sofia.uni.fmi.mjt.jobmatch.model.entity.Skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CosineSimilarity implements SimilarityStrategy {
    public CosineSimilarity() {
    }

    private double getNorm(int[] vector) {
        double squared = 0;
        for (int i = 0; i < vector.length; i++) {
            squared += vector[i] * vector[i];
        }
        return Math.sqrt(squared);
    }

    private int getDotProduct(int[] vector1, int[] vector2) {
        int dotProduct = 0;
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
        }
        return dotProduct;
    }

    private List<String> getSortedUnionSkillNames(Set<Skill> candidateSkills, Set<Skill> jobSkills) {
        Set<String> unionSkills = new HashSet<>();
        for (Skill skill : candidateSkills) {
            unionSkills.add(skill.name());
        }
        for (Skill skill : jobSkills) {
            unionSkills.add(skill.name());
        }
        List<String> sorted = new ArrayList<>(unionSkills);
        Collections.sort(sorted);
        return sorted;
    }

    private void buildVectors(Set<Skill> candidateSkills, Set<Skill> jobSkills,
                              List<String> sortedSkillNames, int[] candidateVector, int[] jobVector) {
        Map<String, Integer> candidateMap = new HashMap<>();
        for (Skill skill : candidateSkills) {
            candidateMap.put(skill.name(), skill.level());
        }

        Map<String, Integer> jobMap = new HashMap<>();
        for (Skill skill : jobSkills) {
            jobMap.put(skill.name(), skill.level());
        }

        for (int i = 0; i < sortedSkillNames.size(); i++) {
            candidateVector[i] = candidateMap.getOrDefault(sortedSkillNames.get(i), 0);
            jobVector[i] = jobMap.getOrDefault(sortedSkillNames.get(i), 0);
        }
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
            throw new IllegalArgumentException("Job ot candidate skills is null.");
        }

        List<String> sorted = getSortedUnionSkillNames(candidateSkills, jobSkills);
        int[] candidateVector = new int[sorted.size()];
        int[] jobVector = new int[sorted.size()];
        buildVectors(candidateSkills, jobSkills, sorted, candidateVector, jobVector);

        int dotProduct = getDotProduct(candidateVector, jobVector);
        double normCandidateVector = getNorm(candidateVector);
        double normJobVector = getNorm(jobVector);

        if (normCandidateVector == 0 || normJobVector == 0) {
            return 0.0;
        }

        double similarity = (double) dotProduct / (normCandidateVector * normJobVector);
        return Math.round(similarity * 1000.0) / 1000.0;
    }

    public static void main() {
        Skill s1 = new Skill("Java", 4);
        Skill s2 = new Skill("Python", 3);
        Skill s3 = new Skill("SQL", 2);
        Skill s4 = new Skill("Java", 5);
        Skill s5 = new Skill("Python", 2);
        Skill s6 = new Skill("JavaScript", 3);
        Set<Skill> skills1 = new HashSet<>();
        skills1.add(s1);
        skills1.add(s2);
        skills1.add(s3);
        Set<Skill> skills2 = new HashSet<>();
        skills2.add(s4);
        skills2.add(s5);
        skills2.add(s6);

        CosineSimilarity cls = new CosineSimilarity();
        double result = cls.calculateSimilarity(skills1, skills2);
        System.out.printf(String.valueOf(result));
    }
}
