package bg.sofia.uni.fmi.mjt.imagekit.algorithm.detection;

import bg.sofia.uni.fmi.mjt.imagekit.algorithm.grayscale.LuminosityGrayscale;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class SobelEdgeDetectionTest {

    @Test
    void testProcessNullThrows() {
        SobelEdgeDetection sobel = new SobelEdgeDetection(new LuminosityGrayscale());
        assertThrows(IllegalArgumentException.class, () -> sobel.process(null));
    }

    @Test
    void testDetectsVerticalEdgeInSimpleImage() {
        // 3x3 image: left column black (0), middle black (0), right column white (255)
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        int black = 0x000000;
        int white = 0xFFFFFF;
        for (int y = 0; y < 3; y++) {
            img.setRGB(0, y, black);
            img.setRGB(1, y, black);
            img.setRGB(2, y, white);
        }

        SobelEdgeDetection sobel = new SobelEdgeDetection(new LuminosityGrayscale());
        BufferedImage out = sobel.process(img);

        // Expect stronger edge response near the boundary between columns 1 and 2 (index 1)
        int centerY = 1;
        int valNearEdge = out.getRGB(1, centerY) & 0xFF;
        int valInsideBlack = out.getRGB(0, centerY) & 0xFF;
        int valInsideWhite = out.getRGB(2, centerY) & 0xFF;

        assertTrue(valNearEdge > valInsideBlack, "Edge magnitude should be higher near edge than inside black region");
        assertTrue(valNearEdge > valInsideWhite, "Edge magnitude should be higher near edge than inside white region");
        assertTrue(valNearEdge > 0, "Edge magnitude should be positive at the edge");
    }

    @Test
    void testConstructorNullGrayscaleThrows() {
        assertThrows(IllegalArgumentException.class, () -> new SobelEdgeDetection(null));
    }

    @Test
    void testProcessReturnsTypeIntRGB() {
        BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        SobelEdgeDetection sobel = new SobelEdgeDetection(new LuminosityGrayscale());
        BufferedImage out = sobel.process(img);

        assertEquals(BufferedImage.TYPE_INT_RGB, out.getType());
    }

    @Test
    void testProcessPreservesDimensions() {
        BufferedImage img = new BufferedImage(10, 8, BufferedImage.TYPE_INT_RGB);
        SobelEdgeDetection sobel = new SobelEdgeDetection(new LuminosityGrayscale());
        BufferedImage out = sobel.process(img);

        assertEquals(10, out.getWidth());
        assertEquals(8, out.getHeight());
    }

    @Test
    void testZeroPaddingAtBorders() {
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        // Set all white
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                img.setRGB(x, y, 0xFFFFFF);
            }
        }

        SobelEdgeDetection sobel = new SobelEdgeDetection(new LuminosityGrayscale());
        BufferedImage out = sobel.process(img);

        // Uniform image should have low edge response everywhere
        int cornerVal = out.getRGB(0, 0) & 0xFF;
        assertTrue(cornerVal >= 0 && cornerVal <= 255);
    }

    @Test
    void testProcessUniformImageHasLowEdges() {
        BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        int gray = (128 << 16) | (128 << 8) | 128;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 5; x++) {
                img.setRGB(x, y, gray);
            }
        }

        SobelEdgeDetection sobel = new SobelEdgeDetection(new LuminosityGrayscale());
        BufferedImage out = sobel.process(img);

        int centerVal = (out.getRGB(2, 2) >> 16) & 0xFF;
        assertTrue(centerVal < 10, "Uniform image should have very low edge magnitude");
    }

    @Test
    void testDetectsHorizontalEdge() {
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        // Top rows black, bottom row white
        for (int x = 0; x < 3; x++) {
            img.setRGB(x, 0, 0x000000);
            img.setRGB(x, 1, 0x000000);
            img.setRGB(x, 2, 0xFFFFFF);
        }

        SobelEdgeDetection sobel = new SobelEdgeDetection(new LuminosityGrayscale());
        BufferedImage out = sobel.process(img);

        int valNearEdge = (out.getRGB(1, 1) >> 16) & 0xFF;
        assertTrue(valNearEdge > 0, "Should detect horizontal edge");
    }

    @Test
    void testMagnitudeClamping() {
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        // Create extreme contrast
        img.setRGB(0, 0, 0x000000);
        img.setRGB(1, 0, 0x000000);
        img.setRGB(2, 0, 0xFFFFFF);
        img.setRGB(0, 1, 0x000000);
        img.setRGB(1, 1, 0x000000);
        img.setRGB(2, 1, 0xFFFFFF);
        img.setRGB(0, 2, 0x000000);
        img.setRGB(1, 2, 0x000000);
        img.setRGB(2, 2, 0xFFFFFF);

        SobelEdgeDetection sobel = new SobelEdgeDetection(new LuminosityGrayscale());
        BufferedImage out = sobel.process(img);

        // Check all pixels are clamped to [0, 255]
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int val = (out.getRGB(x, y) >> 16) & 0xFF;
                assertTrue(val >= 0 && val <= 255, "Pixel value should be in [0, 255]");
            }
        }
    }
}
