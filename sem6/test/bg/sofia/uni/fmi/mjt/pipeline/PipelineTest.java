package bg.sofia.uni.fmi.mjt.pipeline;

import bg.sofia.uni.fmi.mjt.pipeline.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PipelineTest {

    private Stage<String, String> stage1;
    private Stage<String, String> stage2;

    @BeforeEach
    void setUp() {
        stage1 = mock(Stage.class);
        stage2 = mock(Stage.class);
    }

    @Test
    void testStartPipelineWithNullStageThrows() {
        assertThrows(IllegalArgumentException.class, () -> Pipeline.start(null));
    }

    @Test
    void testAddStageReturnsSamePipeline() {
        Pipeline<String, String> pipeline = Pipeline.start(stage1);
        Pipeline<String, String> result = pipeline.addStage(stage2);

        assertSame(pipeline, result);
    }

    @Test
    void testExecuteRunsAllStagesInOrder() {
        when(stage1.execute("input")).thenReturn("middle");
        when(stage2.execute("middle")).thenReturn("output");

        Pipeline<String, String> pipeline = Pipeline.start(stage1)
                .addStage(stage2);

        String result = pipeline.execute("input");

        assertEquals("output", result);

        InOrder inOrder = inOrder(stage1, stage2);
        inOrder.verify(stage1).execute("input");
        inOrder.verify(stage2).execute("middle");
    }

    @Test
    void testExecuteCachesResult() {
        when(stage1.execute("A")).thenReturn("X");

        Pipeline<String, String> pipeline = Pipeline.start(stage1);

        String first = pipeline.execute("A");
        String second = pipeline.execute("A");

        assertEquals("X", first);
        assertEquals("X", second);

        verify(stage1, times(1)).execute("A");
    }

    @Test
    void testExecuteDifferentInputsCachedSeparately() {
        when(stage1.execute("A")).thenReturn("X");
        when(stage1.execute("B")).thenReturn("Y");

        Pipeline<String, String> pipeline = Pipeline.start(stage1);

        assertEquals("X", pipeline.execute("A"));
        assertEquals("Y", pipeline.execute("B"));

        verify(stage1, times(1)).execute("A");
        verify(stage1, times(1)).execute("B");
    }

    @Test
    void testExecuteWithNullInput() {
        when(stage1.execute(null)).thenReturn(null);
        Pipeline<String, String> pipeline = Pipeline.start(stage1);

        assertEquals(null, pipeline.execute(null));
    }
}
