import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CreateTestImage {
    public static void main(String[] args) throws Exception {
        BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 200, 200);
        
        g.setColor(Color.RED);
        g.fillRect(20, 20, 60, 60);
        
        g.setColor(Color.GREEN);
        g.fillOval(100, 20, 60, 60);
        
        g.setColor(Color.BLUE);
        g.fillRect(20, 120, 160, 60);
        
        g.dispose();
        
        ImageIO.write(img, "png", new File("test-images/test.png"));
        System.out.println("Created test-images/test.png");
    }
}
