package bg.sofia.uni.fmi.mjt.file;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileTest {
    @Test
    void testFileConstructorWhenContentIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new File(null),
                "If content is null the constructor should throw Illegal argument.");
    }
}
