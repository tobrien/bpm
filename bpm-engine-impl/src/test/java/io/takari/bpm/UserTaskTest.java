package io.takari.bpm;

import io.takari.bpm.actions.CreateEventAction;
import io.takari.bpm.api.ExecutionException;
import io.takari.bpm.model.*;
import io.takari.bpm.state.ProcessInstance;
import io.takari.bpm.state.StateHelper;
import io.takari.bpm.task.UserTaskHandler;
import org.junit.Test;

import java.util.Arrays;
import java.util.UUID;

public class UserTaskTest extends AbstractEngineTest {

    /**
     * start --> t1 --> end
     */
    @Test
    public void testSuspend() throws Exception {
        getUserTaskHandler().set(new UserTaskHandler() {
            @Override
            public ProcessInstance handle(ProcessInstance state, String definitionId, String elementId) throws ExecutionException {
                state = StateHelper.push(state, new CreateEventAction(definitionId, elementId));
                return state;
            }
        });

        // --

        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "t1"),
                new UserTask("t1"),
                new SequenceFlow("f2", "t1", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "t1");
        assertNoMoreActivations();

        // ---

        getEngine().resume(key, "t1", null);

        // ---

        assertActivations(key, processId,
                "f2",
                "end");
        assertNoMoreActivations();
    }
}
