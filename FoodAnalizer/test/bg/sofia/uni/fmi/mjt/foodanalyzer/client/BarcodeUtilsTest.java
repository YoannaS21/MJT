package bg.sofia.uni.fmi.mjt.foodanalyzer.client;

import com.google.zxing.NotFoundException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

class BarcodeUtilsTest {

    private File getFileFromResources(String fileName) {
        File file = new File("test/resources/" + fileName);

        if (!file.exists()) {
            file = new File("resources/" + fileName);
        }

        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + file.getAbsolutePath());
        }
        return file;
    }

    @Test
    void testDecodeValidBarcodeFromResources() throws Exception {
        File barcodeFile = getFileFromResources("barcode.gif");

        String result = BarcodeUtils.decodeBarcode(barcodeFile);

        assertNotNull(result);
        assertEquals("009800146130", result.trim(), "The decoded text should match the barcode in the image");
    }

    @Test
    void testDecodeImageWithoutBarcodeThrowsNotFoundException() {
        File noBarcodeFile = getFileFromResources("no_barcode.png");

        assertThrows(NotFoundException.class, () -> {
            BarcodeUtils.decodeBarcode(noBarcodeFile);
        }, "Should throw NotFoundException when no barcode is found in the image");
    }

    @Test
    void testDecodeInvalidFileThrowsIOException() {
        File notImageFile = getFileFromResources("not_image.txt");

        assertThrows(IOException.class, () -> {
            BarcodeUtils.decodeBarcode(notImageFile);
        }, "Should throw IOException for files that are not valid images");
    }

    @Test
    void testDecodeNonExistentFile() {
        File missingFile = new File("non_existent_file.png");

        assertThrows(IOException.class, () -> {
            BarcodeUtils.decodeBarcode(missingFile);
        }, "Should throw IOException when file does not exist");
    }
}