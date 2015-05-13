package jet.bpm.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.JavaDelegate;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.BoundaryEvent;
import jet.bpm.engine.model.CallActivity;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.EventBasedGateway;
import jet.bpm.engine.model.ExpressionType;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.ServiceTask;
import jet.bpm.engine.model.StartEvent;
import jet.bpm.engine.model.VariableMapping;
import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

public class CallActivityTest extends AbstractEngineTest {

    /**
     * start --> call               end
     *               \             /
     *                start --> end
     */
    @Test
    public void testSimple() throws Exception {
        String aId = "testA";
        String bId = "testB";

        deploy(new ProcessDefinition(aId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "call"),
                new CallActivity("call", bId),
                new SequenceFlow("f2", "call", "end"),
                new EndEvent("end")
        )));

        deploy(new ProcessDefinition(bId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, aId, null);

        // ---

        assertActivations(key, aId,
                "start",
                "f1",
                "call");

        assertActivations(key, bId,
                "start",
                "f1",
                "end");

        assertActivations(key, aId,
                "f2",
                "end");

        assertNoMoreActivations();
    }

    /**
     * start --> call                              end
     *               \                            /
     *                start --> gw --> ev1 --> end
     *                            \          /
     *                             --> ev2 --
     */
    @Test
    public void testEventGateway() throws Exception {
        String aId = "testA";
        String bId = "testB";
        String ev1 = "ev1";
        String ev2 = "ev2";

        deploy(new ProcessDefinition(aId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "call"),
                new CallActivity("call", bId),
                new SequenceFlow("f2", "call", "end"),
                new EndEvent("end")
        )));

        deploy(new ProcessDefinition(bId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "gw"),
                new EventBasedGateway("gw"),
                    new SequenceFlow("f2", "gw", ev1),
                    new IntermediateCatchEvent(ev1, ev1),
                    new SequenceFlow("f3", ev1, "end"),

                    new SequenceFlow("f4", "gw", ev2),
                    new IntermediateCatchEvent(ev2, ev2),
                    new SequenceFlow("f5", ev2, "end"),
                new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, aId, null);

        assertActivations(key, aId,
                "start",
                "f1",
                "call");

        assertActivations(key, bId,
                "start",
                "f1",
                "gw",
                "f2",
                ev1,
                "f4",
                ev2);

        assertNoMoreActivations();

        // ---

        getEngine().resume(key, ev1, null);

        assertActivations(key, bId,
                "f3",
                "end");
        assertActivations(key, aId,
                "f2",
                "end");

        assertNoMoreActivations();
    }
    
    /**
     * start --> call               t1 --> end
     *               \             /
     *                start --> end
     */
    @Test
    public void testOutVariables() throws Exception {
        String aId = "testA";
        String bId = "testB";
        final String beforeK = "beforeK" + System.currentTimeMillis();
        final String insideK = "insideK" + System.currentTimeMillis();
        final String outsidek = "outsideK" + System.currentTimeMillis();
        final Object v = "v" + System.currentTimeMillis();
        
        Set<VariableMapping> ins = new HashSet<>();
        ins.add(new VariableMapping(null, "${" + beforeK + "}", insideK));
        
        Set<VariableMapping> outs = new HashSet<>();
        outs.add(new VariableMapping(null, "${" + insideK + "}", outsidek));

        deploy(new ProcessDefinition(aId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "call"),
                new CallActivity("call", bId, ins, outs),
                new SequenceFlow("f2", "call", "t1"),
                new ServiceTask("t1", ExpressionType.DELEGATE, "${t1}"),
                new SequenceFlow("f3", "t1", "end"),
                new EndEvent("end")
        )));

        deploy(new ProcessDefinition(bId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "end"),
                new EndEvent("end")
        )));
        
        JavaDelegate t1Task = spy(new JavaDelegate() {

            @Override
            public void execute(ExecutionContext ctx) throws Exception {
                Object o = ctx.getVariable(outsidek);
                assertEquals(v, o);
            }
        });
        getEngine().getServiceTaskRegistry().register("t1", t1Task);

        // ---

        String key = UUID.randomUUID().toString();
        Map<String, Object> input = new HashMap<>();
        input.put(beforeK, v);
        getEngine().start(key, aId, input);

        // ---

        assertActivations(key, aId,
                "start",
                "f1",
                "call");

        assertActivations(key, bId,
                "start",
                "f1",
                "end");

        assertActivations(key, aId,
                "f2",
                "t1",
                "f3",
                "end");

        assertNoMoreActivations();
        
        // ---
        
        verify(t1Task, times(1)).execute(any(ExecutionContext.class));
    }
    
    /**
     * start --> call                       end
     *               \                     /
     *                start --> ev1 --> end
     */
    @Test
    public void testError() throws Exception {
        String aId = "testA";
        String bId = "testB";
        String errorRef = "e" + System.currentTimeMillis();
        String messageRef = "m" + System.currentTimeMillis();

        deploy(new ProcessDefinition(aId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "call"),
                new CallActivity("call", bId),
                new BoundaryEvent("be", "call", errorRef),
                new SequenceFlow("f2", "call", "end"),
                new SequenceFlow("f3", "be", "end"),
                new EndEvent("end")
        )));

        deploy(new ProcessDefinition(bId, Arrays.<AbstractElement>asList(
                new StartEvent("bstart"),
                new SequenceFlow("bf1", "bstart", "bev1"),
                new IntermediateCatchEvent("bev1", messageRef),
                new SequenceFlow("bf2", "bev1", "bend"),
                new EndEvent("bend", errorRef)
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, aId, null);

        // ---

        assertActivations(key, aId,
                "start",
                "f1",
                "call");

        assertActivations(key, bId,
                "bstart",
                "bf1",
                "bev1");
        
        getEngine().resume(key, messageRef, null);

        assertActivations(key, bId,
                "bf2",
                "bend");
        
        assertActivations(key, aId,
                "f3",
                "end");

        assertNoMoreActivations();
    }
}
