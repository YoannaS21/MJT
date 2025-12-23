package bg.sofia.uni.fmi.mjt.imagekit.algorithm.grayscale;

import java.awt.image.BufferedImage;

public class LuminosityGrayscale implements GrayscaleAlgorithm {

    private static final double RED_COEFFICIENT = 0.21;
    private static final double GREEN_COEFFICIENT = 0.72;
    private static final double BLUE_COEFFICIENT = 0.07;
    private static final int MAX_COLOR_VALUE = 255;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int COLOR_MASK = 0xFF;

    public LuminosityGrayscale() {
    }

    @Override
    public BufferedImage process(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image cannot be null");
        }

        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> RED_SHIFT) & COLOR_MASK;
                int g = (rgb >> GREEN_SHIFT) & COLOR_MASK;
                int b = rgb & COLOR_MASK;

                double lum = RED_COEFFICIENT * r + GREEN_COEFFICIENT * g + BLUE_COEFFICIENT * b;
                int gray = clamp((int) Math.round(lum), 0, MAX_COLOR_VALUE);
                int grayRgb = (gray << RED_SHIFT) | (gray << GREEN_SHIFT) | gray;
                out.setRGB(x, y, grayRgb);
            }
        }

        return out;
    }

    private static int clamp(int v, int min, int max) {
        if (v < min) return min;
        if (v > max) return max;
        return v;
    }
}
