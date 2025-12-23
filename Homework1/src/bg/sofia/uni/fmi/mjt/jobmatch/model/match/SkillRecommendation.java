package bg.sofia.uni.fmi.mjt.jobmatch.model.match;

public record SkillRecommendation(String skillName, double improvementScore) {
    public SkillRecommendation {
        if (skillName == null || skillName.isBlank()) {
            throw new IllegalArgumentException("Skill name can't be null or blank.");
        }
        if (improvementScore < 0) {
            throw new IllegalArgumentException("Improvement score can't be negative.");
        }
    }
}
