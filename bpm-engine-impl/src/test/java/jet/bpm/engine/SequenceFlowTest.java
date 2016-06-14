package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionListener;
import jet.bpm.engine.api.ExecutionContext;
import java.util.Arrays;
import java.util.UUID;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.ExpressionType;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.StartEvent;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class SequenceFlowTest extends AbstractEngineTest {
    
    /**
     * start --delegate--> end
     */
    @Test
    public void testDelegateListener() throws Exception {
        ExecutionListener l = mock(ExecutionListener.class);
        getServiceTaskRegistry().register("hello", l);
        
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "end", new SequenceFlow.ExecutionListener("taken", ExpressionType.DELEGATE, "${hello}")),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "end");
        assertNoMoreActivations();

        // ---

        verify(l, times(1)).notify(any(ExecutionContext.class));
    }
    
    /**
     * start --simple--> end
     */
    @Test
    public void testSimpleListener() throws Exception {
        SampleListener l = mock(SampleListener.class);
        getServiceTaskRegistry().register("hello", l);
        
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "end", new SequenceFlow.ExecutionListener("taken", ExpressionType.SIMPLE, "${hello.doIt()}")),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                "end");
        assertNoMoreActivations();

        // ---

        verify(l, times(1)).doIt();
    }
    
    public interface SampleListener {
        
        void doIt();
    }
}
