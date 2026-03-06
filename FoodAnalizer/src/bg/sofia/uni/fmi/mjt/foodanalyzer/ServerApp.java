package bg.sofia.uni.fmi.mjt.foodanalyzer;

public class ServerApp {
    static final int PORT = 18081;
    public static void main(String[] args) {
        // Here we create the FoodAnalizerServer and we start it
        new FoodAnalyzerServer(PORT).start();
    }
}