package bg.sofia.uni.fmi.mjt.file.step;

import bg.sofia.uni.fmi.mjt.file.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UpperCaseFileTest {
    private UpperCaseFile step;

    @BeforeEach
    void setUp() {
        step = new UpperCaseFile();
    }

    @Test
    void testProcessWithNullFile() {
        assertThrows(IllegalArgumentException.class,
                () -> step.process(null),
                "Passing null file should throw IllegalArgumentException");
    }

    @Test
    void testProcessWithNullContent() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> step.process(mockFile),
                "File with null content should throw IllegalArgumentException");
    }

    @Test
    void testProcessWithEmptyContent() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn("");
        File result = step.process(mockFile);

        assertNotNull(result, "Result should not be null");
        assertEquals("", result.getContent(), "Empty content should remain empty");
    }

    @Test
    void testProcessWithLowercaseContent() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn("hello world");
        File result = step.process(mockFile);

        assertNotNull(result);
        assertEquals("HELLO WORLD", result.getContent(), "Content should be uppercased");
        assertNotSame(mockFile, result, "A new File object should be returned");
    }

    @Test
    void testProcessWithMixedCaseContent() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn("HeLLo WoRLD");
        File result = step.process(mockFile);

        assertEquals("HELLO WORLD", result.getContent());
    }

    @Test
    void testProcessWithNumbersAndSymbols() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn("123 abc!@#");
        File result = step.process(mockFile);

        assertEquals("123 ABC!@#", result.getContent());
    }
}
