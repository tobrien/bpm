package io.takari.bpm;

import io.takari.bpm.api.ExecutionContext;
import io.takari.bpm.api.JavaDelegate;
import io.takari.bpm.model.EndEvent;
import io.takari.bpm.model.ExpressionType;
import io.takari.bpm.model.InclusiveGateway;
import io.takari.bpm.model.IntermediateCatchEvent;
import io.takari.bpm.model.ProcessDefinition;
import io.takari.bpm.model.SequenceFlow;
import io.takari.bpm.model.ServiceTask;
import io.takari.bpm.model.StartEvent;
import java.util.Arrays;
import java.util.UUID;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class InclusiveGatewayTest extends AbstractEngineTest {

    /**
     * start --> gw1 --> ev --> gw2 --> end
     */
    @Test
    public void testSingleEvent() throws Exception {
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "gw1"),
                new InclusiveGateway("gw1"),
                    new SequenceFlow("f2", "gw1", "ev"),
                    new IntermediateCatchEvent("ev", "ev"),
                    new SequenceFlow("f3", "ev", "gw2"),
                new InclusiveGateway("gw2"),
                new SequenceFlow("f4", "gw2", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        getEngine().resume(key, "ev", null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "gw1",
                "f2",
                "ev",
                "f3",
                "gw2",
                "f4",
                "end");
        assertNoMoreActivations();
    }

    /**
     * start --> gw1 --> ev1 --> gw2 --> end
     *              \           /
     *               --> ev2 -->
     */
    private ProcessDefinition makeDuoEventProcess(String processId) {
       return new ProcessDefinition(processId, Arrays.asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "gw1"),
                new InclusiveGateway("gw1"),

                    new SequenceFlow("f2", "gw1", "ev1"),
                    new IntermediateCatchEvent("ev1", "ev1"),
                    new SequenceFlow("f3", "ev1", "gw2"),

                    new SequenceFlow("f4", "gw1", "ev2"),
                    new IntermediateCatchEvent("ev2", "ev2"),
                    new SequenceFlow("f5", "ev2", "gw2"),

                new InclusiveGateway("gw2"),
                new SequenceFlow("f6", "gw2", "end"),
                new EndEvent("end")
        ));
    }


    @Test
    public void testDuoEvent() throws Exception {
        String processId = "test";
        deploy(makeDuoEventProcess(processId));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "gw1",
                "f2",
                "ev1",
                "f4",
                "ev2");
        assertNoMoreActivations();

        // ---

        getEngine().resume(key, "ev1", null);

        // ---

        assertActivations(key, processId,
                "f3",
                "gw2");
        assertNoMoreActivations();

        // ---

        getEngine().resume(key, "ev2", null);

        // ---

        assertActivations(key, processId,
                "f5",
                "gw2",
                "f6",
                "end");
        assertNoMoreActivations();
    }

    @Test
    public void testDuoEventReverseOrder() throws Exception {
        String processId = "test";
        deploy(makeDuoEventProcess(processId));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);
        getEngine().resume(key, "ev2", null);
        getEngine().resume(key, "ev1", null);

        assertActivations(key, processId,
                "start",
                "f1",
                "gw1",
                "f2",
                "ev1",
                "f4",
                "ev2",
                "f5",
                "gw2",
                "f3",
                "gw2",
                "f6",
                "end");
        assertNoMoreActivations();
    }

    /**
     * start --> gw1 --> t1 --> ev1 --> gw2 --> t3 --> end
     *              \                  /
     *               --> t2 --> ev2 -->
     */
    @Test
    public void testDuoEventComplex() throws Exception {
        JavaDelegate taskA = mock(JavaDelegate.class);
        getServiceTaskRegistry().register("taskA", taskA);
        
        JavaDelegate taskB = mock(JavaDelegate.class);
        getServiceTaskRegistry().register("taskB", taskB);
        
        JavaDelegate taskC = mock(JavaDelegate.class);
        getServiceTaskRegistry().register("taskC", taskC);
        
        // ---
        
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "gw1"),
                new InclusiveGateway("gw1"),

                    new SequenceFlow("f2", "gw1", "t1"),
                    new ServiceTask("t1", ExpressionType.DELEGATE, "${taskA}"),
                    new SequenceFlow("f3", "t1", "ev1"),
                    new IntermediateCatchEvent("ev1", "ev1"),
                    new SequenceFlow("f4", "ev1", "gw2"),

                    new SequenceFlow("f5", "gw1", "t2"),
                    new ServiceTask("t2", ExpressionType.DELEGATE, "${taskB}"),
                    new SequenceFlow("f6", "t2", "ev2"),
                    new IntermediateCatchEvent("ev2", "ev2"),
                    new SequenceFlow("f7", "ev2", "gw2"),

                new InclusiveGateway("gw2"),
                new SequenceFlow("f8", "gw2", "t3"),
                new ServiceTask("t3", ExpressionType.DELEGATE, "${taskC}"),
                new SequenceFlow("f9", "t3", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);
        getEngine().resume(key, "ev1", null);
        getEngine().resume(key, "ev2", null);
        
        // ---
        
        verify(taskA, times(1)).execute(any(ExecutionContext.class));
        verify(taskB, times(1)).execute(any(ExecutionContext.class));
        verify(taskC, times(1)).execute(any(ExecutionContext.class));
    }
    
    /**
     * start --> gw1 --> t1 ------> gw2 --> end
     *              \             /
     *               \--> t2 ---->
     *                \          /
     *                 --> t3 -->
     */
    @Test
    public void testPartiallyInactive() throws Exception {
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "gw1"),
                new InclusiveGateway("gw1"),

                    new SequenceFlow("f2", "gw1", "t1"),
                    new ServiceTask("t1"),
                    new SequenceFlow("f3", "t1", "gw2"),

                    new SequenceFlow("f4", "gw1", "t2", "${false}"),
                    new ServiceTask("t2"),
                    new SequenceFlow("f5", "t2", "gw2"),

                    new SequenceFlow("f6", "gw1", "t3", "${false}"),
                    new ServiceTask("t3"),
                    new SequenceFlow("f7", "t3", "gw2"),

                new InclusiveGateway("gw2"),
                new SequenceFlow("f8", "gw2", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "gw1",
                "f2",
                "t1",
                "f3",
                "gw2",
                "f8",
                "end");
        assertNoMoreActivations();
    }
}
