package bg.sofia.uni.fmi.mjt.foodanalyzer.dto;

public class Nutrient {
    private String nutrientName;
    private double value;
    private String unitName;

    public Nutrient() {
    }

    public Nutrient(String nutrientName, double value, String unitName) {
        this.nutrientName = nutrientName;
        this.value = value;
        this.unitName = unitName;
    }

    public String getNutrientName() {
        return nutrientName;
    }

    public double getValue() {
        return value;
    }

    public String getUnitName() {
        return unitName;
    }

    @Override
    public String toString() {
        return String.format("%s: %.2f %s", nutrientName, value, unitName);
    }
}