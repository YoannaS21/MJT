package bg.sofia.uni.fmi.mjt.imagekit.algorithm.detection;

import bg.sofia.uni.fmi.mjt.imagekit.algorithm.ImageAlgorithm;

import java.awt.image.BufferedImage;

public class SobelEdgeDetection implements EdgeDetectionAlgorithm {

    private static final int KERNEL_SIZE = 3;
    private static final int MAX_COLOR_VALUE = 255;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int COLOR_MASK = 0xFF;
    private static final int KERNEL_CENTER_OFFSET = 1;
    private static final int SOBEL_WEIGHT_TWO = 2;
    private static final int SOBEL_WEIGHT_MINUS_TWO = -2;

    private final ImageAlgorithm grayscaleAlgorithm;

    public SobelEdgeDetection(ImageAlgorithm grayscaleAlgorithm) {
        if (grayscaleAlgorithm == null) {
            throw new IllegalArgumentException("grayscaleAlgorithm cannot be null");
        }
        this.grayscaleAlgorithm = grayscaleAlgorithm;
    }

    @Override
    public BufferedImage process(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("image cannot be null");
        }

        BufferedImage gray = grayscaleAlgorithm.process(image);
        int width = gray.getWidth();
        int height = gray.getHeight();

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[][] gxKernel = createGxKernel();
        int[][] gyKernel = createGyKernel();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int[] gradients = computeGradients(gray, x, y, width, height, gxKernel, gyKernel);
                int pixelValue = computeMagnitude(gradients[0], gradients[1]);
                int rgb = createRgbValue(pixelValue);
                out.setRGB(x, y, rgb);
            }
        }

        return out;
    }

    private int[][] createGxKernel() {
        return new int[][] {
            {-1, 0, 1},
            {SOBEL_WEIGHT_MINUS_TWO, 0, SOBEL_WEIGHT_TWO},
            {-1, 0, 1}
        };
    }

    private int[][] createGyKernel() {
        return new int[][] {
            {-1, SOBEL_WEIGHT_MINUS_TWO, -1},
            {0, 0, 0},
            {1, SOBEL_WEIGHT_TWO, 1}
        };
    }

    private int[] computeGradients(BufferedImage gray, int x, int y, int width, int height,
                                   int[][] gxKernel, int[][] gyKernel) {
        int gx = 0;
        int gy = 0;

        for (int ky = 0; ky < KERNEL_SIZE; ky++) {
            for (int kx = 0; kx < KERNEL_SIZE; kx++) {
                int nx = x + kx - KERNEL_CENTER_OFFSET;
                int ny = y + ky - KERNEL_CENTER_OFFSET;

                int pixelVal = getPixelValue(gray, nx, ny, width, height);
                gx += pixelVal * gxKernel[ky][kx];
                gy += pixelVal * gyKernel[ky][kx];
            }
        }

        return new int[] {gx, gy};
    }

    private int getPixelValue(BufferedImage gray, int x, int y, int width, int height) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return 0; // zero padding
        }
        int rgb = gray.getRGB(x, y);
        return (rgb >> RED_SHIFT) & COLOR_MASK;
    }

    private int computeMagnitude(int gx, int gy) {
        double g = Math.sqrt((double) gx * gx + (double) gy * gy);
        return Math.min(MAX_COLOR_VALUE, (int) Math.round(g));
    }

    private int createRgbValue(int pixelValue) {
        return (pixelValue << RED_SHIFT) | (pixelValue << GREEN_SHIFT) | pixelValue;
    }
}
