package bg.sofia.uni.fmi.mjt.file.step;

import bg.sofia.uni.fmi.mjt.file.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CountFilesTest {
    private CountFiles step;

    @BeforeEach
    void setUp() {
        step = new CountFiles();
    }

    @Test
    void testProcessWhenCollectionIsNull() {
        assertThrows(IllegalArgumentException.class, () -> step.process(null),
                "When the collection is null IllegalArgument exception should be thrown.");
    }

    @Test
    void testProcessWhenCollectionIsEmpty() {
        Collection<File> mockList = mock(Collection.class);
        when(mockList.size()).thenReturn(0);
        assertEquals(0, step.process(mockList));
    }

    @Test
    void testProcessWhenCollectionIsValid() {
        Collection<File> mockList = mock(Collection.class);
        when(mockList.size()).thenReturn(3);
        assertEquals(3, step.process(mockList));
    }


}
