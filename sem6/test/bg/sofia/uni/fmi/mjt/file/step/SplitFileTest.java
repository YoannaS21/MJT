package bg.sofia.uni.fmi.mjt.file.step;

import bg.sofia.uni.fmi.mjt.file.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SplitFileTest {
    private SplitFile step;

    @BeforeEach
    void setUp() {
        step = new SplitFile();
    }

    @Test
    void testProcessWithNullFile() {
        assertThrows(IllegalArgumentException.class,
                () -> step.process(null),
                "Null file should throw IllegalArgumentException");
    }

    @Test
    void testProcessWithNullContent() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> step.process(mockFile),
                "Null file should throw IllegalArgumentException");
    }

    @Test
    void testProcessWithEmptyContent() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn("");
        Set<File> result = step.process(mockFile);
        assertTrue(result.isEmpty(), "Empty content should result in empty set");
    }

    @Test
    void testProcessWithSingleWord() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn("Hello");
        Set<File> result = step.process(mockFile);
        assertEquals(1, result.size(), "Single word should produce set of size 1");
        assertEquals("Hello", result.iterator().next().getContent());
    }

    @Test
    void testProcessWithMultipleWords() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn("Hello world this is test");
        Set<File> result = step.process(mockFile);
        assertEquals(5, result.size(), "Should split content into 5 unique words");
        assertTrue(result.stream().anyMatch(f -> f.getContent().equals("Hello")));
        assertTrue(result.stream().anyMatch(f -> f.getContent().equals("world")));
        assertTrue(result.stream().anyMatch(f -> f.getContent().equals("this")));
        assertTrue(result.stream().anyMatch(f -> f.getContent().equals("is")));
        assertTrue(result.stream().anyMatch(f -> f.getContent().equals("test")));
    }

    @Test
    void testProcessWithDuplicateWords() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn("Hello world Hello world");
        Set<File> result = step.process(mockFile);
        assertEquals(2, result.size(), "Duplicate words should not be added twice");
        assertTrue(result.stream().anyMatch(f -> f.getContent().equals("Hello")));
        assertTrue(result.stream().anyMatch(f -> f.getContent().equals("world")));
    }
}
