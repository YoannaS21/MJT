package bg.sofia.uni.fmi.mjt.foodanalyzer;

public class ClientApp {
    static final int PORT = 18081;
    public static void main(String[] args) {
        // Here we start the Client console, that creates a new network client
        new FoodAnalyzerConsole("localhost", PORT).start();
    }
}
