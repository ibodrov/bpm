package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.BpmnError;
import jet.bpm.engine.api.JavaDelegate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
import org.junit.Test;
import static org.mockito.Mockito.*;

public class ServiceTaskTest extends AbstractEngineTest {

    /**
     * start --> t1 --> end
     */
    @Test
    public void testSimple() throws Exception {
        JavaDelegate helloTask = spy(new JavaDelegate() {

            @Override
            public void execute(ExecutionContext ctx) throws ExecutionException {
                System.out.println("Hello, " + ctx.getVariable("name") + "!");
            }
        });
        getEngine().getServiceTaskRegistry().register("hello", helloTask);

        // ---

        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "t1"),
                new ServiceTask("t1", ExpressionType.DELEGATE, "${hello}"),
                new SequenceFlow("f2", "t1", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "world");
        getEngine().start(key, processId, vars);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "t1",
                "f2",
                "end");
        assertNoMoreActivations();

        // ---

        verify(helloTask, times(1)).execute(any(ExecutionContext.class));
    }

    /**
     * start --> t1 ----------> end
     *             \        /
     *              error --
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

        // ---

        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "t1"),
                new ServiceTask("t1", ExpressionType.DELEGATE, "${t1}"),
                new BoundaryEvent("be1", "t1", errorRef),
                new SequenceFlow("f2", "be1", "end"),
                new SequenceFlow("f3", "t1", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "t1",
                "f2",
                "end");
        assertNoMoreActivations();

        // ---

        verify(t1, times(1)).execute(any(ExecutionContext.class));
    }
    
    /**
     * start --> t1 --> end
     */
    @Test
    public void testSimpleTaskExpression() throws Exception {
        SampleTask t = mock(SampleTask.class);
        getEngine().getServiceTaskRegistry().register("hello", t);

        // ---

        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "t1"),
                new ServiceTask("t1", ExpressionType.SIMPLE, "${hello.doIt(123)}"),
                new SequenceFlow("f2", "t1", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        Map<String, Object> vars = new HashMap<>();
        vars.put("name", "world");
        getEngine().start(key, processId, vars);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "t1",
                "f2",
                "end");
        assertNoMoreActivations();

        // ---

        verify(t, times(1)).doIt(eq(123L));
    }
    
    public interface SampleTask {
        
        void doIt(long i);
    }
}
