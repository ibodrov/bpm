package jet.bpm.engine;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.event.EventDispatcher;
import jet.bpm.engine.event.EventPersistenceManager;
import jet.bpm.engine.event.EventScheduler;
import jet.bpm.engine.event.ExpiredEvent;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class EventSchedulerTest {

    @Test
    public void test() throws Exception {
        ExpiredEvent ev1 = new ExpiredEvent(UUID.randomUUID(), new Date());
        
        EventPersistenceManager events = mock(EventPersistenceManager.class);
        when(events.findNextExpiredEvent(anyInt())).thenReturn(Arrays.asList(ev1));
        when(events.get(any(UUID.class))).thenAnswer(new Answer<Event>() {
            @Override
            public Event answer(InvocationOnMock invocation) throws Throwable {
                UUID id = (UUID)invocation.getArguments()[0];
                return new Event(id, null, null, null, null, true, null);
            }
        });
        
        EventDispatcher dispatcher = mock(EventDispatcher.class);
        
        EventScheduler sched = new EventScheduler(events, 10, dispatcher);
        sched.setAcquisitionDelay(100);
        sched.setEventExecutorsCount(1);
        sched.start();
        
        ArgumentCaptor<Event> args = ArgumentCaptor.forClass(Event.class);
        verify(dispatcher, timeout(5000).times(1)).dispatch(args.capture());
        
        args.getValue().getId().equals(ev1.geId());
        
        sched.stop();
    }
}
