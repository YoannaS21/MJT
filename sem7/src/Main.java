import bg.sofia.uni.fmi.mjt.imagekit.algorithm.ImageAlgorithm;
import bg.sofia.uni.fmi.mjt.imagekit.algorithm.detection.SobelEdgeDetection;
import bg.sofia.uni.fmi.mjt.imagekit.algorithm.grayscale.LuminosityGrayscale;
import bg.sofia.uni.fmi.mjt.imagekit.filesystem.FileSystemImageManager;
import bg.sofia.uni.fmi.mjt.imagekit.filesystem.LocalFileSystemImageManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String inputPath;
        String outputDirPath;

        if (args.length >= 2) {
            inputPath = args[0];
            outputDirPath = args[1];
        } else {
            inputPath = "test-images/test.png";
            outputDirPath = "out-images";
            System.out.println("No arguments provided. Using defaults:");
            System.out.println("Input: " + inputPath);
            System.out.println("Output: " + outputDirPath);
        }

        File input = new File(inputPath);
        File outputDir = new File(outputDirPath);

        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("Failed to create output directory: " + outputDir.getAbsolutePath());
                return;
            }
        }

        FileSystemImageManager fs = new LocalFileSystemImageManager();
        ImageAlgorithm grayscale = new LuminosityGrayscale();
        ImageAlgorithm sobel = new SobelEdgeDetection(grayscale);

        try {
            BufferedImage img = fs.loadImage(input);

            BufferedImage gray = grayscale.process(img);
            BufferedImage edges = sobel.process(img);

            String baseName = stripExtension(input.getName());
            File grayOut = new File(outputDir, baseName + "-grayscale.png");
            File edgesOut = new File(outputDir, baseName + "-edges.png");

            fs.saveImage(gray, grayOut);
            fs.saveImage(edges, edgesOut);

            System.out.println("Saved grayscale to: " + grayOut.getAbsolutePath());
            System.out.println("Saved edges to:    " + edgesOut.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static String stripExtension(String name) {
        int idx = name.lastIndexOf('.')
                ;
        if (idx <= 0) return name;
        return name.substring(0, idx);
    }
}
