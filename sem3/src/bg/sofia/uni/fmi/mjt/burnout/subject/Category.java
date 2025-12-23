package bg.sofia.uni.fmi.mjt.burnout.subject;

public enum Category {

    MATH(0.2),
    PROGRAMMING(0.1),
    THEORY(0.15),
    PRACTICAL(0.05);

    private final double coefficient;

    Category(double coefficient) {
        this.coefficient = coefficient;
    }

    public double getCoefficient() {
        return coefficient;
    }

}
