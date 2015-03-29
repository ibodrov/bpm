package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.BpmnError;
import jet.bpm.engine.api.JavaDelegate;
import java.util.Arrays;
import java.util.UUID;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.BoundaryEvent;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.ServiceTask;
import jet.bpm.engine.model.ExpressionType;
import jet.bpm.engine.model.StartEvent;
import jet.bpm.engine.model.SubProcess;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class SubProcessTest extends AbstractEngineTest {

    /**
     * start --> sub                            t2 --> end
     *              \                          /
     *               substart --> t1 --> subend
     */
    @Test
    public void testSimple() throws Exception {
        JavaDelegate t1 = mock(JavaDelegate.class);
        getEngine().getServiceTaskRegistry().register("t1", t1);

        JavaDelegate t2 = mock(JavaDelegate.class);
        getEngine().getServiceTaskRegistry().register("t2", t2);

        // --

        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "sub"),
                new SubProcess("sub", Arrays.<AbstractElement>asList(
                        new StartEvent("substart"),
                        new SequenceFlow("f2", "substart", "t1"),
                        new ServiceTask("t1", ExpressionType.DELEGATE, "${t1}"),
                        new SequenceFlow("f3", "t1", "subend"),
                        new EndEvent("subend")
                )),
                new SequenceFlow("f4", "sub", "t2"),
                new ServiceTask("t2", ExpressionType.DELEGATE, "${t2}"),
                new SequenceFlow("f5", "t2", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "sub",
                "substart",
                "f2",
                "t1",
                "f3",
                "subend",
                "f4",
                "t2",
                "f5",
                "end");
        assertNoMoreActivations();

        // ---

        verify(t1, times(1)).execute(any(ExecutionContext.class));
        verify(t2, times(1)).execute(any(ExecutionContext.class));
    }

    /**
     * start --> sub                            t2 ------------------> end
     *              \                          /                   /
     *               substart --> t1 --> subend --> error --> t3 --
     */
    @Test
    public void testBoundaryError() throws Exception {
        final String errorRef = "test#" + System.currentTimeMillis();

        JavaDelegate t1 = spy(new JavaDelegate() {

            @Override
            public void execute(ExecutionContext ctx) throws ExecutionException {
                throw new BpmnError(errorRef);
            }
        });
        getEngine().getServiceTaskRegistry().register("t1", t1);

        JavaDelegate t2 = mock(JavaDelegate.class);
        getEngine().getServiceTaskRegistry().register("t2", t2);

        JavaDelegate t3 = mock(JavaDelegate.class);
        getEngine().getServiceTaskRegistry().register("t3", t3);

        // ---

        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "sub"),
                new SubProcess("sub", Arrays.<AbstractElement>asList(
                        new StartEvent("substart"),
                        new SequenceFlow("f2", "substart", "t1"),
                        new ServiceTask("t1", ExpressionType.DELEGATE, "${t1}"),
                        new SequenceFlow("f3", "t1", "subend"),
                        new EndEvent("subend")
                )),
                new BoundaryEvent("be1", "sub", errorRef),
                new SequenceFlow("f4", "be1", "t3"),
                new ServiceTask("t3", ExpressionType.DELEGATE, "${t3}"),
                new SequenceFlow("f5", "t3", "end"),

                new SequenceFlow("f6", "sub", "t2"),
                new ServiceTask("t2", ExpressionType.DELEGATE, "${t2}"),
                new SequenceFlow("f6", "t2", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---
        
        assertActivations(key, processId,
                "start",
                "f1",
                "sub",
                "substart",
                "f2",
                "t1",
                "f4",
                "t3",
                "f5",
                "end");
        assertNoMoreActivations();

        // ---

        verify(t1, times(1)).execute(any(ExecutionContext.class));
        verifyZeroInteractions(t2);
        verify(t3, times(1)).execute(any(ExecutionContext.class));
    }
    
    /**
     * start --> sub                                -----------> end
     *              \                              /            /
     *               substart --> t1 --> suberrend --> error -->
     */
    @Test
    public void testErrorEnd() throws Exception {
        final String errorRef = "test#" + System.currentTimeMillis();
        
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "sub"),
                new SubProcess("sub", Arrays.<AbstractElement>asList(
                        new StartEvent("substart"),
                        new SequenceFlow("f2", "substart", "subend"),
                        new EndEvent("subend", errorRef)
                )),
                new BoundaryEvent("be1", "sub", errorRef),
                new SequenceFlow("f4", "be1", "end"),

                new SequenceFlow("f6", "sub", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---
        
        assertActivations(key, processId,
                "start",
                "f1",
                "sub",
                "substart",
                "f2",
                "subend",
                "f4",
                "end");
        assertNoMoreActivations();
    }
}
