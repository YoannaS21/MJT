package bg.sofia.uni.fmi.mjt.pipeline.stage;

import bg.sofia.uni.fmi.mjt.pipeline.step.Step;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StageTest {

    @Test
    void testStartWithNullStep() {
        assertThrows(IllegalArgumentException.class, () -> Stage.start(null),
                "Passing null step should throw IllegalArgumentException");
    }

    @Test
    void testExecuteSingleStep() {
        Step<Integer, String> step = mock(Step.class);
        when(step.process(5)).thenReturn("result");

        Stage<Integer, String> stage = Stage.start(step);
        String output = stage.execute(5);

        assertEquals("result", output, "Output should match step.process result");
        verify(step).process(5);
    }

    @Test
    void testAddStepAndExecuteMultipleSteps() {
        Step<Integer, String> step1 = mock(Step.class);
        Step<String, Boolean> step2 = mock(Step.class);

        when(step1.process(10)).thenReturn("ten");
        when(step2.process("ten")).thenReturn(true);

        Stage<Integer, String> stage = Stage.start(step1);
        Stage<Integer, Boolean> extendedStage = stage.addStep(step2);

        Boolean result = extendedStage.execute(10);

        assertTrue(result, "Final output should be true");
        verify(step1).process(10);
        verify(step2).process("ten");

        assertSame(stage, extendedStage, "addStep should return the same stage instance");
    }

    @Test
    void testAddNullStep() {
        Step<Integer, String> step = mock(Step.class);
        Stage<Integer, String> stage = Stage.start(step);

        assertThrows(IllegalArgumentException.class, () -> stage.addStep(null),
                "Adding null step should throw IllegalArgumentException");
    }

    @Test
    void testExecuteWithMultipleStepTypes() {
        Step<Integer, Integer> step1 = mock(Step.class);
        Step<Integer, String> step2 = mock(Step.class);
        Step<String, Boolean> step3 = mock(Step.class);

        when(step1.process(1)).thenReturn(2);
        when(step2.process(2)).thenReturn("done");
        when(step3.process("done")).thenReturn(true);

        Stage<Integer, Integer> stage = Stage.start(step1);
        Stage<Integer, String> stage2 = stage.addStep(step2);
        Stage<Integer, Boolean> stage3 = stage2.addStep(step3);

        Boolean result = stage3.execute(1);

        assertTrue(result, "Final result should be true");
        verify(step1).process(1);
        verify(step2).process(2);
        verify(step3).process("done");
    }

    @Test
    void testExecuteWithoutStepsChangingType() {
        Step<String, String> step = input -> input;
        Stage<String, String> stage = Stage.start(step);

        String result = stage.execute("hello");
        assertEquals("hello", result, "Output should be equal to input");
    }
}
