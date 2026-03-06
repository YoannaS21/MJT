package bg.sofia.uni.fmi.mjt.foodanalyzer.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NetworkClientTest {

    @Mock
    private Socket mockSocket;

    private ByteArrayOutputStream clientOutput;

    @BeforeEach
    void setUp() throws IOException {
        clientOutput = new ByteArrayOutputStream();
        when(mockSocket.getOutputStream()).thenReturn(clientOutput);
    }

    private void setServerResponse(String response) throws IOException {
        ByteArrayInputStream serverInput = new ByteArrayInputStream(response.getBytes());
        when(mockSocket.getInputStream()).thenReturn(serverInput);
    }

    @Test
    void testSendRequestSuccessfully() throws Exception {
        setServerResponse("Mocked Apple Data\n<END>\n");
        when(mockSocket.isClosed()).thenReturn(false);

        try (NetworkClient client = new NetworkClient(mockSocket)) {
            String result = client.sendRequest("get-food apple");

            assertEquals("Mocked Apple Data", result);
            assertEquals("get-food apple\n", clientOutput.toString().replace("\r", ""));
        }
    }

    @Test
    void testServerClosesConnectionUnexpectedly() throws Exception {
        setServerResponse("");
        when(mockSocket.isClosed()).thenReturn(false);

        try (NetworkClient client = new NetworkClient(mockSocket)) {
            String result = client.sendRequest("get-food");

            assertEquals("The server closed the connection unexpectedly.", result);
        }
    }

    @Test
    void testSendRequestWhenSocketIsClosed() throws Exception {
        setServerResponse("");
        when(mockSocket.isClosed()).thenReturn(true);

        try (NetworkClient client = new NetworkClient(mockSocket)) {
            String result = client.sendRequest("get-food");

            assertEquals("Disconnected: The server is no longer reachable.", result);
        }
    }
}