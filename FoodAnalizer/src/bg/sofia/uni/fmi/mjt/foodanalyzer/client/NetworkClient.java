package bg.sofia.uni.fmi.mjt.foodanalyzer.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class NetworkClient implements AutoCloseable {
    private final Socket socket;
    private final BufferedReader reader;
    private final PrintWriter writer;

    public NetworkClient(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    NetworkClient(Socket socket) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.writer = new PrintWriter(socket.getOutputStream(), true);
    }

    public String sendRequest(String message) {
        try {
            if (socket.isClosed()) {
                return "Disconnected: The server is no longer reachable.";
            }
            // We send the command
            writer.println(message);
            // We read the response
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                if ("<END>".equals(line)) {
                    break;
                }
                response.append(line).append(System.lineSeparator());
            }
            // If readLine returns null without <END>, means the server stopped the connection
            if (response.isEmpty() && !socket.isClosed()) {
                return "The server closed the connection unexpectedly.";
            }

            return response.toString().trim();

        } catch (SocketException e) {
            return "Connection lost: The server was shut down or the network is unstable.";
        } catch (IOException e) {
            return "Communication error: " + e.getMessage();
        }
    }

    @Override
    public void close() throws IOException {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}