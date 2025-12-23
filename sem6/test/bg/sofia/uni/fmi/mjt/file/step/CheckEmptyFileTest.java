package bg.sofia.uni.fmi.mjt.file.step;

import bg.sofia.uni.fmi.mjt.file.File;
import bg.sofia.uni.fmi.mjt.file.exception.EmptyFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CheckEmptyFileTest {
    private CheckEmptyFile step;

    @BeforeEach
    void setUp() {
        step = new CheckEmptyFile();
    }

    @Test
    void testProcessWhenFileIsNull() {
        assertThrows(EmptyFileException.class, () -> step.process(null),
                "When input file is null EmptyFileException should be thrown.");
    }

    @Test
    void testProcessWhenFileContentIsNull() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn(null);
        assertThrows(EmptyFileException.class, () -> step.process(mockFile),
                "When input file is null EmptyFileException should be thrown.");
        //when(mockFile.getContent()).thenReturn("Hey");
    }

    @Test
    void testProcessWhenFileContentIsEmpty() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn("");
        assertThrows(EmptyFileException.class, () -> step.process(mockFile),
                "When input file is empty EmptyFileException should be thrown.");
    }

    @Test
    void testProcessWhenFileIsCorrect() {
        File mockFile = mock(File.class);
        when(mockFile.getContent()).thenReturn("hey");

        File result = step.process(mockFile);
        assertEquals(mockFile, result, "Valid file should be returned as it is");
    }
}
