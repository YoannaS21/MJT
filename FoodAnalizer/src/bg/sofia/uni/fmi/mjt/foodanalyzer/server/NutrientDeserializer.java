package bg.sofia.uni.fmi.mjt.foodanalyzer.server;

import bg.sofia.uni.fmi.mjt.foodanalyzer.dto.Nutrient;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class NutrientDeserializer implements JsonDeserializer<Nutrient> {

    @Override
    public Nutrient deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String name = "Unknown Nutrient";
        double value = 0.0;
        String unit = "";

        if (jsonObject.has("nutrientName")) {
            name = jsonObject.get("nutrientName").getAsString();
        } else if (jsonObject.has("nutrient")) {
            JsonObject nested = jsonObject.getAsJsonObject("nutrient");
            if (nested.has("name")) {
                name = nested.get("name").getAsString();
            }
            if (nested.has("unitName")) {
                unit = nested.get("unitName").getAsString();
            }
        } else if (jsonObject.has("name")) {
            name = jsonObject.get("name").getAsString();
        }
        if (jsonObject.has("value")) {
            value = jsonObject.get("value").getAsDouble();
        } else if (jsonObject.has("amount")) {
            value = jsonObject.get("amount").getAsDouble();
        }
        if (unit.isEmpty()) {
            if (jsonObject.has("unitName")) {
                unit = jsonObject.get("unitName").getAsString();
            } else if (jsonObject.has("unit")) {
                unit = jsonObject.get("unit").getAsString();
            }
        }
        return new Nutrient(name, value, unit);
    }
}