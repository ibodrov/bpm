package jet.bpm.engine;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.handlers.IntermediateCatchEventHandler;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.EventBasedGateway;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.StartEvent;
import static org.junit.Assert.*;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

public class EventBasedGatewayTest extends AbstractEngineTest {

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
                    new IntermediateCatchEvent("ev"),
                    new SequenceFlow("f3", "ev", "end"),
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
                "gw",
                "f2",
                "ev",
                "f3",
                "end");
        assertNoMoreActivations();
    }

    /**
     * start --> gw --> ev1 --> end1
     *             \
     *              --> ev2 --> end2
     */
    @Test
    public void testDuoEvent() throws Exception {
        String processId = "test";
        String eventGroup = "gw#" + System.currentTimeMillis();

        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", eventGroup),
                new EventBasedGateway(eventGroup),
                    new SequenceFlow("f2", eventGroup, "ev1"),
                    new IntermediateCatchEvent("ev1"),
                    new SequenceFlow("f3", "ev1", "end1"),

                    new EndEvent("end1"),

                    new SequenceFlow("f4", eventGroup, "ev2"),
                    new IntermediateCatchEvent("ev2"),
                    new SequenceFlow("f5", "ev2", "end2"),

                    new EndEvent("end2")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        getEngine().resume(key, "ev2", null);

        // ---

        assertActivations(key, processId,
                "start",
                "f1",
                eventGroup,
                "f2",
                "ev1",
                "f4",
                "ev2",
                "f5",
                "end2");
        assertNoMoreActivations();

        // ---

        verify(eventManager, times(1)).clearGroup(eq(key), eq(eventGroup));
    }

    /**
     * start --> gw1 --> ev1 --> end1
     *             \
     *              --> ev2 --> gw2 --> ev3 --> end2
     *                            \
     *                             --> ev4 --> end3
     */
    @Test
    public void testNested() throws Exception {
        String processId = "test";

        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "gw1"),
                new EventBasedGateway("gw1"),
                    new SequenceFlow("f2", "gw1", "ev1"),
                    new IntermediateCatchEvent("ev1"),
                    new SequenceFlow("f3", "ev1", "end1"),

                    new EndEvent("end1"),

                    new SequenceFlow("f4", "gw1", "ev2"),
                    new IntermediateCatchEvent("ev2"),
                    new SequenceFlow("f5", "ev2", "gw2"),
                    new EventBasedGateway("gw2"),
                        new SequenceFlow("f6", "gw2", "ev3"),
                        new IntermediateCatchEvent("ev3"),
                        new SequenceFlow("f7", "ev3", "end2"),

                        new EndEvent("end2"),

                        new SequenceFlow("f8", "gw2", "ev4"),
                        new IntermediateCatchEvent("ev4"),
                        new SequenceFlow("f9", "ev4", "end3"),

                        new EndEvent("end3")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        getEngine().start(key, processId, null);

        // ---

        getEngine().resume(key, "ev2", null);
        getEngine().resume(key, "ev4", null);

        // ---

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
                "f6",
                "ev3",
                "f8",
                "ev4",
                "f9",
                "end3");
        assertNoMoreActivations();
    }
    
    /**
     * start --> gw --> ev1 --> ev1 --> end
     */
    @Test
    public void testTimerEvent() throws Exception {
        String k1 = "timeVal";
        Object v1 = new Date();
        String dt = "2011-03-11T12:13:14";
        
        String processId = "test";
        deploy(new ProcessDefinition(processId, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "gw"),
                new EventBasedGateway("gw"),
                    new SequenceFlow("f2", "gw", "ev1"),
                    new IntermediateCatchEvent("ev1", null, "${" + k1 + "}", null),
                    new SequenceFlow("f3", "ev1", "ev2"),
                    new IntermediateCatchEvent("ev2", null, dt, null),
                    new SequenceFlow("f4", "ev2", "end"),
                    new EndEvent("end")
        )));

        // ---

        String key = UUID.randomUUID().toString();
        Map<String, Object> m = new HashMap<>();
        m.put(k1, v1);
        getEngine().start(key, processId, m);

        // ---

        ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
        verify(eventManager, times(1)).register(eq(key), arg.capture());
        Event ev = arg.getValue();
        assertNotNull(ev);
        assertEquals(v1, ev.getTimeDate());
        reset(eventManager);
        
        // ---
        
        getEngine().resume(key, "ev1", null);
        
        // ---
        
        arg = ArgumentCaptor.forClass(Event.class);
        verify(eventManager, times(1)).register(eq(key), arg.capture());
        ev = arg.getValue();
        assertNotNull(ev);
        Date actualDt = IntermediateCatchEventHandler.parseIso8601(dt);
        assertEquals(actualDt, ev.getTimeDate());
    }
}
