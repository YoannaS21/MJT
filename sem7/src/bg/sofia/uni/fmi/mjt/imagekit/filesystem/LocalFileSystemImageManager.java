package bg.sofia.uni.fmi.mjt.imagekit.filesystem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LocalFileSystemImageManager implements FileSystemImageManager {

    private static final Set<String> SUPPORTED_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "bmp"));
    private static final int MAGIC_BYTES_SIZE = 12;

    public LocalFileSystemImageManager() {
    }

    @Override
    public BufferedImage loadImage(File imageFile) throws IOException {
        validateImageFile(imageFile);

        BufferedImage img = ImageIO.read(imageFile);
        if (img == null) {
            throw createDetailedIOException(imageFile);
        }
        return img;
    }

    private void validateImageFile(File imageFile) throws IOException {
        if (imageFile == null) {
            throw new IllegalArgumentException("imageFile cannot be null");
        }
        if (!imageFile.exists() || !imageFile.isFile()) {
            throw new IOException("Image file does not exist or is not a regular file: " + imageFile);
        }
        String ext = getExtension(imageFile);
        if (!isSupported(ext)) {
            throw new IOException("Unsupported image format: " + ext);
        }
    }

    private IOException createDetailedIOException(File imageFile) {
        String ext = getExtension(imageFile);
        String readersInfo = getReadersInfo(imageFile);
        String magicHex = getMagicBytes(imageFile);

        return new IOException(
            "Failed to read image (possibly corrupted or unsupported content): " + imageFile
                + " | ext=" + ext
                + " | header=" + magicHex
                + " | readers=" + readersInfo
        );
    }

    private String getReadersInfo(File imageFile) {
        try (var iis = ImageIO.createImageInputStream(imageFile)) {
            if (iis != null) {
                var readers = ImageIO.getImageReaders(iis);
                StringBuilder sb = new StringBuilder();
                boolean any = false;
                while (readers.hasNext()) {
                    any = true;
                    var r = readers.next();
                    sb.append(r.getOriginatingProvider() != null
                            ? r.getOriginatingProvider().getFormatNames()[0]
                            : r.getFormatName());
                    if (readers.hasNext()) sb.append(',');
                }
                return any ? sb.toString() : "<no readers available for stream>";
            }
            return "<unable to open image input stream>";
        } catch (Exception ignored) {
            return "<error while probing readers>";
        }
    }

    private String getMagicBytes(File imageFile) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(imageFile)) {
            byte[] buf = new byte[MAGIC_BYTES_SIZE];
            int n = fis.read(buf);
            if (n > 0) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < n; i++) {
                    sb.append(String.format("%02X", buf[i]));
                    if (i < n - 1) sb.append(' ');
                }
                return sb.toString();
            }
            return "<empty file>";
        } catch (Exception ignored) {
            return "<error reading file header>";
        }
    }

    @Override
    public List<BufferedImage> loadImagesFromDirectory(File imagesDirectory) throws IOException {
        validateDirectory(imagesDirectory);

        File[] files = imagesDirectory.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }

        validateAllFilesSupported(files);
        return loadAllImages(files);
    }

    private void validateDirectory(File imagesDirectory) throws IOException {
        if (imagesDirectory == null) {
            throw new IllegalArgumentException("imagesDirectory cannot be null");
        }
        if (!imagesDirectory.exists() || !imagesDirectory.isDirectory()) {
            throw new IOException("Path does not exist or is not a directory: " + imagesDirectory);
        }
    }

    private void validateAllFilesSupported(File[] files) throws IOException {
        for (File f : files) {
            if (f.isFile()) {
                String ext = getExtension(f);
                if (!isSupported(ext)) {
                    throw new IOException("Directory contains unsupported file format: " + f.getName());
                }
            }
        }
    }

    private List<BufferedImage> loadAllImages(File[] files) throws IOException {
        List<BufferedImage> result = new ArrayList<>();
        for (File f : files) {
            if (f.isFile() && isSupported(getExtension(f))) {
                result.add(loadImage(f));
            }
        }
        return result;
    }

    @Override
    public void saveImage(BufferedImage image, File imageFile) throws IOException {
        validateSaveArguments(image, imageFile);

        BufferedImage toSave = convertToRgbIfNeeded(image);
        String ext = getExtension(imageFile);

        boolean ok = ImageIO.write(toSave, ext.toLowerCase(Locale.ROOT), imageFile);
        if (!ok) {
            throw new IOException("Failed to write image using format: " + ext);
        }
    }

    private void validateSaveArguments(BufferedImage image, File imageFile) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("image cannot be null");
        }
        if (imageFile == null) {
            throw new IllegalArgumentException("imageFile cannot be null");
        }
        if (imageFile.exists()) {
            throw new IOException("Target file already exists: " + imageFile);
        }
        File parent = imageFile.getParentFile();
        if (parent != null && !parent.exists()) {
            throw new IOException("Parent directory does not exist: " + parent);
        }
        String ext = getExtension(imageFile);
        if (!isSupported(ext)) {
            throw new IOException("Unsupported image format for saving: " + ext);
        }
    }

    private BufferedImage convertToRgbIfNeeded(BufferedImage image) {
        if (image.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage rgbImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB
            );
            rgbImage.getGraphics().drawImage(image, 0, 0, null);
            rgbImage.getGraphics().dispose();
            return rgbImage;
        }
        return image;
    }

    private static boolean isSupported(String ext) {
        return ext != null && SUPPORTED_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT));
    }

    private static String getExtension(File f) {
        String name = f.getName();
        int idx = name.lastIndexOf('.');
        if (idx == -1 || idx == name.length() - 1) {
            return null;
        }
        return name.substring(idx + 1);
    }
}
