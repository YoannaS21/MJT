package bg.sofia.uni.fmi.mjt.jobmatch.model.entity;

public enum Education {
    HIGH_SCHOOL(1),
    BACHELORS(2),
    MASTERS(3),
    PHD(4);

    private final int level;

    Education(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}