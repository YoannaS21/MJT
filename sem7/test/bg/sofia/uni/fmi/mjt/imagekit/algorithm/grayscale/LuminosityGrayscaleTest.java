package bg.sofia.uni.fmi.mjt.imagekit.algorithm.grayscale;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

public class LuminosityGrayscaleTest {

    @Test
    void testProcessNullThrows() {
        LuminosityGrayscale algo = new LuminosityGrayscale();
        assertThrows(IllegalArgumentException.class, () -> algo.process(null));
    }

    @Test
    void testLuminosityFormulaRoundingAndClamping() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        int r = 100, g = 150, b = 200;
        int rgb = (r << 16) | (g << 8) | b;
        img.setRGB(0, 0, rgb);

        LuminosityGrayscale algo = new LuminosityGrayscale();
        BufferedImage out = algo.process(img);

        int outRgb = out.getRGB(0, 0);
        int gray = (outRgb >> 16) & 0xFF;
        // expected round(0.21*100 + 0.72*150 + 0.07*200) = round(21 + 108 + 14) = 143
        assertEquals(143, gray);

        // Check all channels are equal (grayscale) - mask out alpha channel
        int grayG = (outRgb >> 8) & 0xFF;
        int grayB = outRgb & 0xFF;
        assertEquals(gray, grayG, "G channel should equal R channel");
        assertEquals(gray, grayB, "B channel should equal R channel");
    }

    @Test
    void testProcessPureWhite() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0xFFFFFF);

        LuminosityGrayscale algo = new LuminosityGrayscale();
        BufferedImage out = algo.process(img);

        int gray = (out.getRGB(0, 0) >> 16) & 0xFF;
        assertEquals(255, gray);
    }

    @Test
    void testProcessPureBlack() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0x000000);

        LuminosityGrayscale algo = new LuminosityGrayscale();
        BufferedImage out = algo.process(img);

        int gray = (out.getRGB(0, 0) >> 16) & 0xFF;
        assertEquals(0, gray);
    }

    @Test
    void testProcessMultiplePixels() {
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                img.setRGB(x, y, ((100 + x * 50) << 16) | ((150 + y * 20) << 8) | 200);
            }
        }

        LuminosityGrayscale algo = new LuminosityGrayscale();
        BufferedImage out = algo.process(img);

        assertEquals(3, out.getWidth());
        assertEquals(3, out.getHeight());
        assertEquals(BufferedImage.TYPE_INT_RGB, out.getType());
    }

    @Test
    void testProcessReturnsNewImage() {
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        LuminosityGrayscale algo = new LuminosityGrayscale();
        BufferedImage out = algo.process(img);

        assertNotSame(img, out, "Should return a new image");
    }

    @Test
    void testClampingUpperBound() {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, 0xFFFFFF); // 255, 255, 255

        LuminosityGrayscale algo = new LuminosityGrayscale();
        BufferedImage out = algo.process(img);

        int gray = (out.getRGB(0, 0) >> 16) & 0xFF;
        assertTrue(gray <= 255, "Gray value should be clamped to 255");
    }
}
