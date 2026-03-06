package bg.sofia.uni.fmi.mjt.foodanalyzer.dto;

import java.util.List;

public class FoodItem {
    private int fdcId;
    private String description;
    private String gtinUpc;
    private String brandOwner;
    private String ingredients;

    private List<Nutrient> foodNutrients;

    public int getFdcId() {
        return fdcId;
    }

    public String getDescription() {
        return description;
    }

    public String getGtinUpc() {
        return gtinUpc;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getBrandOwner() {
        return brandOwner;
    }

    public List<Nutrient> getNutrients() {
        return foodNutrients;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (Brand: %s)", fdcId, description, brandOwner);
    }

}
