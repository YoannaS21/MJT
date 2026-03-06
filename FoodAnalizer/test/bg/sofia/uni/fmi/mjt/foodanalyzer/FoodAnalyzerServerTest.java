package bg.sofia.uni.fmi.mjt.foodanalyzer;

import bg.sofia.uni.fmi.mjt.foodanalyzer.server.CacheService;
import bg.sofia.uni.fmi.mjt.foodanalyzer.server.HttpService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class FoodAnalyzerServerTest {

    private InputStream originalIn;

    @Mock
    private HttpService mockHttpService;

    @Mock
    private CacheService mockCacheService;

    @BeforeEach
    void setUp() {
        originalIn = System.in;
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
    }

    @Test
    void testServerStopsViaConsoleCommand() {
        ByteArrayInputStream simulatedInput = new ByteArrayInputStream("stop\n".getBytes());
        System.setIn(simulatedInput);

        FoodAnalyzerServer server = new FoodAnalyzerServer(18080, mockHttpService, mockCacheService);

        assertDoesNotThrow(server::start, "Server should start and then stop gracefully when 'stop' is entered.");
    }

    @Test
    void testServerAcceptsClientConnections() throws InterruptedException {
        FoodAnalyzerServer server = new FoodAnalyzerServer(18081, mockHttpService, mockCacheService);

        Thread serverThread = new Thread(() -> server.start());
        serverThread.start();

        Thread.sleep(500); // Wait for the server to start

        assertDoesNotThrow(() -> {
            try (Socket client = new Socket("localhost", 18081)) {
                assertTrue(client.isConnected());
            }
        });

        server.stop();
        serverThread.join(2000);
    }

    @Test
    void testStopCanBeCalledMultipleTimes() {
        FoodAnalyzerServer server = new FoodAnalyzerServer(18082, mockHttpService, mockCacheService);

        assertDoesNotThrow(() -> {
            server.stop();
            server.stop();
        }, "Calling stop multiple times should not throw an exception");
    }
}