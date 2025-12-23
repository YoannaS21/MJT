package bg.sofia.uni.fmi.mjt.jobmatch.model.entity;

public record Skill(String name, int level) {
    public Skill {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name was either blank ot null.");
        }
        if (level < 1 || level > 5) {
            throw new IllegalArgumentException("Level should be between 1 and 5.");
        }
    }
}
