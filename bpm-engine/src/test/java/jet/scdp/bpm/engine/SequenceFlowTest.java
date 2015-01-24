package jet.scdp.bpm.engine;

import jet.scdp.bpm.api.ExecutionListener;
import jet.scdp.bpm.api.ExecutionContext;
import java.util.Arrays;
import java.util.UUID;
import jet.scdp.bpm.model.AbstractElement;
import jet.scdp.bpm.model.EndEvent;
import jet.scdp.bpm.model.ExpressionType;
import jet.scdp.bpm.model.ProcessDefinition;
import jet.scdp.bpm.model.SequenceFlow;
import jet.scdp.bpm.model.StartEvent;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class SequenceFlowTest extends AbstractEngineTest {
    
    /**
     * start --delegate--> end
     */
    @Test
    public void testDelegateListener() throws Exception {
        ExecutionListener l = mock(ExecutionListener.class);
        getEngine().getServiceTaskRegistry().register("hello", l);
        
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "end", new SequenceFlow.ExecutionListener("taken", ExpressionType.DELEGATE, "${hello}")),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().run(key, processId, null);

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
        getEngine().getServiceTaskRegistry().register("hello", l);
        
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "end", new SequenceFlow.ExecutionListener("taken", ExpressionType.SIMPLE, "${hello.doIt()}")),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().run(key, processId, null);

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
