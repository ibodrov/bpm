package jet.bpm.engine;

import java.util.Arrays;
import java.util.UUID;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.JavaDelegate;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.ExpressionType;
import jet.bpm.engine.model.InclusiveGateway;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.ServiceTask;
import jet.bpm.engine.model.StartEvent;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class InclusiveGatewayTest extends AbstractEngineTest {

    /**
     * start --> gw1 --> ev --> gw2 --> end
     */
    @Test
    public void testSingleEvent() throws Exception {
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
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
    @Test
    public void testDuoEvent() throws Exception {
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
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
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
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
}
