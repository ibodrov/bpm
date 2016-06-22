package jet.bpm.engine;

import java.util.Arrays;
import java.util.UUID;
import jet.bpm.engine.api.BpmnError;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.api.JavaDelegate;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.BoundaryEvent;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.ExpressionType;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.ServiceTask;
import jet.bpm.engine.model.StartEvent;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class TimerBoundaryEvent extends AbstractEngineTest {
    
    /**
     * start --> t1 --------> end1
     *            \
     *             timer1 --> end2
     */
    @Test(timeout = 10000)
    public void testAsUsual() throws Exception {
        JavaDelegate longTask = spy(new JavaDelegate() {

            @Override
            public void execute(ExecutionContext ctx) throws ExecutionException {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        getServiceTaskRegistry().register("longTask", longTask);
        
        // ---
        
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "t1"),
                new ServiceTask("t1", ExpressionType.DELEGATE, "${longTask}"),
                new BoundaryEvent("timer1", "t1", null, "PT7S"),
                new SequenceFlow("f2", "t1", "end1"),
                new SequenceFlow("f3", "timer1", "end2"),
                new EndEvent("end1"),
                new EndEvent("end2")
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
                "end1");
        assertNoMoreActivations();

        // ---

        verify(longTask, times(1)).execute(any(ExecutionContext.class));
    }
    
    /**
     * start --> t1 --------> end1
     *            \
     *             timer1 --> end2
     */
    @Test(timeout = 10000)
    public void testTimeout() throws Exception {
        JavaDelegate longTask = spy(new JavaDelegate() {

            @Override
            public void execute(ExecutionContext ctx) throws ExecutionException {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        
        getServiceTaskRegistry().register("longTask", longTask);
        
        // ---
        
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "t1"),
                new ServiceTask("t1", ExpressionType.DELEGATE, "${longTask}"),
                new BoundaryEvent("timer1", "t1", null, "PT3S"),
                new SequenceFlow("f2", "t1", "end1"),
                new SequenceFlow("f3", "timer1", "end2"),
                new EndEvent("end1"),
                new EndEvent("end2")
        )));

        // ---
        
        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "t1",
                "f3",
                "end2");
        assertNoMoreActivations();

        // ---

        verify(longTask, times(1)).execute(any(ExecutionContext.class));
    }
    
    /**
     * start --> t1 ---------> end1
     *            |\
     *            | timer1 --> end2
     *            \
     *             error1 ----> end3
     */
    @Test
    public void testMixedEvents() throws Exception {
        JavaDelegate failingTask = spy(new JavaDelegate() {

            @Override
            public void execute(ExecutionContext ctx) throws ExecutionException {
                throw new BpmnError("error1");
            }
        });
        
        getServiceTaskRegistry().register("failingTask", failingTask);
        
        // ---
        
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "t1"),
                new ServiceTask("t1", ExpressionType.DELEGATE, "${failingTask}"),
                new BoundaryEvent("timer1", "t1", null, "PT3S"),
                new BoundaryEvent("error1", "t1", "error1"),
                new SequenceFlow("f2", "t1", "end1"),
                new SequenceFlow("f3", "timer1", "end2"),
                new SequenceFlow("f4", "error1", "end3"),
                new EndEvent("end1"),
                new EndEvent("end2"),
                new EndEvent("end3")
        )));

        // ---
        
        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "t1",
                "f4",
                "end3");
        assertNoMoreActivations();

        // ---

        verify(failingTask, times(1)).execute(any(ExecutionContext.class));
    }
}
