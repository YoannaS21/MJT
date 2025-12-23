package bg.sofia.uni.fmi.mjt.file.step;

import bg.sofia.uni.fmi.mjt.file.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrintFilesTest {
    private PrintFiles step;

    @BeforeEach
    void setUp() {
        step = new PrintFiles();
    }

    @Test
    void testProcessWhenCollectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> step.process(null),
                "When input collection is null, IllegalArgument should be thrown.");
    }

    @Test
    void testProcessWithFiles() {
        File file1 = mock(File.class);
        when(file1.getContent()).thenReturn("Content 1");

        File file2 = mock(File.class);
        when(file2.getContent()).thenReturn("Content 2");

        Collection<File> files = List.of(file1, file2);

        Collection<File> result = step.process(files);

        assertEquals(files, result, "Process should return the same collection");

        verify(file1).getContent();
        verify(file2).getContent();
    }

    @Test
    void testProcessWithNullFiles() {
        File file1 = mock(File.class);
        when(file1.getContent()).thenReturn("Content 1");

        Collection<File> files = new ArrayList<>();
        files.add(file1);
        files.add(null);

        Collection<File> result = step.process(files);

        assertEquals(files, result, "Process should return the same collection");

        verify(file1).getContent();
    }

}
