package jet.bpm.engine;

import java.util.Arrays;
import java.util.UUID;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.CallActivity;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.EventBasedGateway;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.StartEvent;
import org.junit.Test;

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
                    new IntermediateCatchEvent(ev1),
                    new SequenceFlow("f3", ev1, "end"),

                    new SequenceFlow("f4", "gw", ev2),
                    new IntermediateCatchEvent(ev2),
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
}
