package jet.bpm.engine;

import java.util.Arrays;
import java.util.UUID;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.EventBasedGateway;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.StartEvent;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class ExecutionInterceptorTest extends AbstractEngineTest {

    private ExecutionInterceptor interceptor;
    
    @Before
    public void setUp() {
        interceptor = mock(ExecutionInterceptor.class);
        getEngine().addInterceptor(interceptor);
    }
    
    /**
     * start --> gw --> ev --> end
     */
    @Test
    public void testSingleEvent() throws Exception {
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "gw"),
                new EventBasedGateway("gw"),
                    new SequenceFlow("f2", "gw", "ev"),
                    new IntermediateCatchEvent("ev", "ev"),
                    new SequenceFlow("f3", "ev", "end"),
                    new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);
        
        
        verify(interceptor, times(1)).onStart(eq(key));
        verify(interceptor, times(1)).onSuspend();

        // ---

        getEngine().resume(key, "ev", null);

        // ---

        verify(interceptor, times(1)).onResume();
        verify(interceptor, times(1)).onFinish(eq(key));
    }
}
