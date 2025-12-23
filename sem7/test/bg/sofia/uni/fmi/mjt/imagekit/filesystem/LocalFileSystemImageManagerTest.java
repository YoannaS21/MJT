package bg.sofia.uni.fmi.mjt.imagekit.filesystem;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class LocalFileSystemImageManagerTest {

    @Test
    void testSaveAndLoadRoundtripPNG() throws IOException {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();

        // create simple 2x2 image
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        img.setRGB(0, 0, Color.RED.getRGB());
        img.setRGB(1, 0, Color.GREEN.getRGB());
        img.setRGB(0, 1, Color.BLUE.getRGB());
        img.setRGB(1, 1, Color.WHITE.getRGB());

        File tempDir = Files.createTempDirectory("imgkit-").toFile();
        tempDir.deleteOnExit();
        File outFile = new File(tempDir, "test-image.png");

        mgr.saveImage(img, outFile);

        assertTrue(outFile.exists());

        BufferedImage loaded = mgr.loadImage(outFile);
        assertNotNull(loaded);
        assertEquals(2, loaded.getWidth());
        assertEquals(2, loaded.getHeight());
    }

    @Test
    void testLoadImageNullFileThrows() {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        assertThrows(IllegalArgumentException.class, () -> mgr.loadImage(null));
    }

    @Test
    void testSaveImageNullArgsThrow() {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        assertThrows(IllegalArgumentException.class, () -> mgr.saveImage(null, new File("x.png")));
        assertThrows(IllegalArgumentException.class, () -> mgr.saveImage(new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB), null));
    }

    @Test
    void testLoadImageFromNonExistentFileThrows() {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        File nonExistent = new File("non-existent-file-xyz123.png");
        assertThrows(IOException.class, () -> mgr.loadImage(nonExistent));
    }

    @Test
    void testLoadImageUnsupportedFormatThrows() throws IOException {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        File tempFile = Files.createTempFile("test", ".txt").toFile();
        tempFile.deleteOnExit();
        Files.writeString(tempFile.toPath(), "not an image");

        assertThrows(IOException.class, () -> mgr.loadImage(tempFile));
    }

    @Test
    void testSaveImageToExistingFileThrows() throws IOException {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);

        File tempFile = Files.createTempFile("test", ".png").toFile();
        tempFile.deleteOnExit();

        assertThrows(IOException.class, () -> mgr.saveImage(img, tempFile));
    }

    @Test
    void testSaveImageParentDirNotExistThrows() {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        File fileWithNonExistentParent = new File("/non/existent/path/image.png");

        assertThrows(IOException.class, () -> mgr.saveImage(img, fileWithNonExistentParent));
    }

    @Test
    void testLoadImagesFromDirectoryNullThrows() {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        assertThrows(IllegalArgumentException.class, () -> mgr.loadImagesFromDirectory(null));
    }

    @Test
    void testLoadImagesFromNonExistentDirectoryThrows() {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        File nonExistent = new File("non-existent-dir-xyz123");
        assertThrows(IOException.class, () -> mgr.loadImagesFromDirectory(nonExistent));
    }

    @Test
    void testLoadImagesFromDirectoryWithUnsupportedFilesThrows() throws IOException {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();

        File tempDir = Files.createTempDirectory("imgkit-test-").toFile();
        tempDir.deleteOnExit();

        File textFile = new File(tempDir, "test.txt");
        Files.writeString(textFile.toPath(), "not an image");
        textFile.deleteOnExit();

        assertThrows(IOException.class, () -> mgr.loadImagesFromDirectory(tempDir));
    }

    @Test
    void testLoadImagesFromEmptyDirectory() throws IOException {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();

        File tempDir = Files.createTempDirectory("imgkit-empty-").toFile();
        tempDir.deleteOnExit();

        var images = mgr.loadImagesFromDirectory(tempDir);
        assertTrue(images.isEmpty());
    }

    @Test
    void testLoadImagesFromDirectoryMultipleImages() throws IOException {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();

        File tempDir = Files.createTempDirectory("imgkit-multi-").toFile();
        tempDir.deleteOnExit();

        BufferedImage img1 = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        BufferedImage img2 = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);

        File file1 = new File(tempDir, "img1.png");
        File file2 = new File(tempDir, "img2.jpg");

        mgr.saveImage(img1, file1);
        mgr.saveImage(img2, file2);

        var images = mgr.loadImagesFromDirectory(tempDir);
        assertEquals(2, images.size());
    }

    @Test
    void testSaveAndLoadJPEGFormat() throws IOException {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        BufferedImage img = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);

        File tempDir = Files.createTempDirectory("imgkit-jpg-").toFile();
        tempDir.deleteOnExit();
        File jpgFile = new File(tempDir, "test.jpg");

        mgr.saveImage(img, jpgFile);
        BufferedImage loaded = mgr.loadImage(jpgFile);

        assertEquals(5, loaded.getWidth());
        assertEquals(5, loaded.getHeight());
    }

    @Test
    void testSaveAndLoadBMPFormat() throws IOException {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        BufferedImage img = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);

        File tempDir = Files.createTempDirectory("imgkit-bmp-").toFile();
        tempDir.deleteOnExit();
        File bmpFile = new File(tempDir, "test.bmp");

        mgr.saveImage(img, bmpFile);
        BufferedImage loaded = mgr.loadImage(bmpFile);

        assertEquals(4, loaded.getWidth());
        assertEquals(4, loaded.getHeight());
    }

    @Test
    void testLoadImageFromDirectory() throws IOException {
        LocalFileSystemImageManager mgr = new LocalFileSystemImageManager();
        File fileInsteadOfDir = Files.createTempFile("test", ".png").toFile();
        fileInsteadOfDir.deleteOnExit();

        assertThrows(IOException.class, () -> mgr.loadImagesFromDirectory(fileInsteadOfDir));
    }
}
