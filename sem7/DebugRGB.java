public class DebugRGB {
    public static void main(String[] args) {
        int gray = 143;
        int expected = (gray << 16) | (gray << 8) | gray;
        int actual = 9408399;
        
        System.out.println("Expected: " + expected);
        System.out.println("Actual: " + actual);
        System.out.println("Expected hex: " + Integer.toHexString(expected));
        System.out.println("Actual hex: " + Integer.toHexString(actual));
        
        // Extract RGB from actual
        int r = (actual >> 16) & 0xFF;
        int g = (actual >> 8) & 0xFF;
        int b = actual & 0xFF;
        System.out.println("Actual R=" + r + " G=" + g + " B=" + b);
    }
}
