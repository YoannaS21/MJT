package bg.sofia.uni.fmi.mjt.foodanalyzer;

import bg.sofia.uni.fmi.mjt.foodanalyzer.client.BarcodeUtils;
import bg.sofia.uni.fmi.mjt.foodanalyzer.client.NetworkClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FoodAnalyzerConsoleTest {

    @Mock
    private NetworkClient mockClient;

    private FoodAnalyzerConsole console;

    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;
    private ByteArrayOutputStream outContent;
    private ByteArrayOutputStream errContent;

    @BeforeEach
    void setUp() {
        console = new FoodAnalyzerConsole("localhost", 8080);

        outContent = new ByteArrayOutputStream();
        errContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testExitCommandSendsDisconnectAndBreaksLoop() {
        Scanner scanner = new Scanner("exit\n");

        console.runConsole(scanner, mockClient);

        verify(mockClient).sendRequest("disconnect");
    }

    @Test
    void testNormalCommandSendsRequestToClient() {
        Scanner scanner = new Scanner("get-food apple\nexit\n");
        when(mockClient.sendRequest("get-food apple")).thenReturn("Apple Info");

        console.runConsole(scanner, mockClient);

        verify(mockClient).sendRequest("get-food apple");
        verify(mockClient).sendRequest("disconnect");
    }

    @Test
    void testConnectionLostBreaksLoopAutomatically() {
        Scanner scanner = new Scanner("get-food apple\n");
        when(mockClient.sendRequest("get-food apple")).thenReturn("Connection lost: The server was shut down.");

        console.runConsole(scanner, mockClient);

        verify(mockClient).sendRequest("get-food apple");
        verify(mockClient, never()).sendRequest("disconnect");
    }

    @Test
    void testBarcodeCodeOnlySuccess() {
        Scanner scanner = new Scanner("get-food-by-barcode --code=12345\nexit\n");
        when(mockClient.sendRequest("get-food-by-barcode 12345")).thenReturn("Product Info");

        console.runConsole(scanner, mockClient);

        verify(mockClient).sendRequest("get-food-by-barcode 12345");
    }

    @Test
    void testBarcodeCodeOnlyNotFound() {
        Scanner scanner = new Scanner("get-food-by-barcode --code=000\nexit\n");
        when(mockClient.sendRequest("get-food-by-barcode 000")).thenReturn("Error: Product not found");

        console.runConsole(scanner, mockClient);

        verify(mockClient).sendRequest("get-food-by-barcode 000");
    }

    @Test
    void testBarcodeCodeConnectionLost() {
        Scanner scanner = new Scanner("get-food-by-barcode --code=123\n");
        when(mockClient.sendRequest("get-food-by-barcode 123")).thenReturn("Connection lost");

        console.runConsole(scanner, mockClient);

        verify(mockClient, never()).sendRequest("disconnect");
    }

    @Test
    void testBarcodeCodeAndImageFallbackImageMissing() {
        Scanner scanner = new Scanner("get-food-by-barcode --code=123 --img=missing.png\nexit\n");
        when(mockClient.sendRequest("get-food-by-barcode 123")).thenReturn("Product not found");

        console.runConsole(scanner, mockClient);

        verify(mockClient).sendRequest("get-food-by-barcode 123");
        verify(mockClient, never()).sendRequest(argThat(arg -> arg.startsWith("get-food-by-barcode") && !arg.contains("123")));
    }

    @Test
    void testBarcodeCodeAndImageFallbackSuccess() throws Exception {
        File tempImage = File.createTempFile("test_barcode", ".png");
        tempImage.deleteOnExit();

        Scanner scanner = new Scanner("get-food-by-barcode --code=wrong --img=" + tempImage.getAbsolutePath() + "\nexit\n");

        when(mockClient.sendRequest("get-food-by-barcode wrong")).thenReturn("Product not found");
        when(mockClient.sendRequest("get-food-by-barcode 99999")).thenReturn("Fallback Product Info");

        try (MockedStatic<BarcodeUtils> mockedBarcodeUtils = mockStatic(BarcodeUtils.class)) {
            mockedBarcodeUtils.when(() -> BarcodeUtils.decodeBarcode(any(File.class))).thenReturn("99999");

            console.runConsole(scanner, mockClient);
        }

        verify(mockClient).sendRequest("get-food-by-barcode wrong");
        verify(mockClient).sendRequest("get-food-by-barcode 99999");
    }

    @Test
    void testBarcodeImageOnlySuccess() throws Exception {
        File tempImage = File.createTempFile("test_barcode_only", ".png");
        tempImage.deleteOnExit();

        Scanner scanner = new Scanner("get-food-by-barcode --img=" + tempImage.getAbsolutePath() + "\nexit\n");
        when(mockClient.sendRequest("get-food-by-barcode 777")).thenReturn("Image Product Info");

        try (MockedStatic<BarcodeUtils> mockedBarcodeUtils = mockStatic(BarcodeUtils.class)) {
            mockedBarcodeUtils.when(() -> BarcodeUtils.decodeBarcode(any(File.class))).thenReturn("777");

            console.runConsole(scanner, mockClient);
        }

        verify(mockClient).sendRequest("get-food-by-barcode 777");
        verify(mockClient, never()).sendRequest(argThat(arg -> arg.contains("--code=")));
    }

    @Test
    void testBarcodeImageExceptionCaught() throws Exception {
        File tempImage = File.createTempFile("bad_image", ".png");
        tempImage.deleteOnExit();

        Scanner scanner = new Scanner("get-food-by-barcode --img=" + tempImage.getAbsolutePath() + "\nexit\n");

        try (MockedStatic<BarcodeUtils> mockedBarcodeUtils = mockStatic(BarcodeUtils.class)) {
            mockedBarcodeUtils.when(() -> BarcodeUtils.decodeBarcode(any(File.class)))
                .thenThrow(new RuntimeException("Fake Decoding Error"));

            console.runConsole(scanner, mockClient);
        }

        assertTrue(errContent.toString().contains("Error processing barcode command: Fake Decoding Error"));
    }

    @Test
    void testBarcodeWithQuotesParameters() {
        Scanner scanner = new Scanner("get-food-by-barcode --code=\"123 456\"\nexit\n");
        when(mockClient.sendRequest("get-food-by-barcode 123 456")).thenReturn("Quoted Product Info");

        console.runConsole(scanner, mockClient);

        verify(mockClient).sendRequest("get-food-by-barcode 123 456");
    }
}