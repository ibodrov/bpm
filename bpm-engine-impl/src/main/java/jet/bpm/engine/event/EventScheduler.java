package jet.bpm.engine.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import static java.util.concurrent.TimeUnit.SECONDS;
import jet.bpm.engine.api.Engine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventScheduler.class);

    private final EventManager eventManager;

    private final Engine engine;

    private final BlockingQueue<Event> acquiredEventQueue;

    private Thread eventAcquisitionThread;

    private final List<Thread> eventExecutorThreads = new ArrayList<>();

    private volatile boolean stopped = true;

    private int eventExecutorsCount = 10;

    private int maxEventsPerAcquisition = 10;
    private long acquisitionDelay = SECONDS.toMillis(5);
    private long acquisitionErrorDelay = SECONDS.toMillis(5);
    private long executionErrorDelay = SECONDS.toMillis(5);

    public EventScheduler(Engine engine, EventManager eventManager, int maxAcquiredEventQueueSize) {
        this.engine = engine;
        this.eventManager = eventManager;
        this.acquiredEventQueue = new LinkedBlockingQueue(maxAcquiredEventQueueSize);
    }

    public void setEventExecutorsCount(int eventExecutorsCount) {
        this.eventExecutorsCount = eventExecutorsCount;
    }

    public void setMaxEventsPerAcquisition(int maxEventsPerAcquisition) {
        this.maxEventsPerAcquisition = maxEventsPerAcquisition;
    }

    public void setAcquisitionDelay(long acquisitionDelay) {
        this.acquisitionDelay = acquisitionDelay;
    }

    public void setAcquisitionErrorDelay(long acquisitionErrorDelay) {
        this.acquisitionErrorDelay = acquisitionErrorDelay;
    }

    public void setExecutionErrorDelay(long executionErrorDelay) {
        this.executionErrorDelay = executionErrorDelay;
    }

    public synchronized void start() {
        if (!stopped) {
            return;
        }

        stopped = false;

        for (int i = 0; i < eventExecutorsCount; i++) {
            Thread t = new Thread("eventExecutionThread") {

                @Override
                public void run() {
                    eventExecutionLoop();
                }
            };
            t.start();
            eventExecutorThreads.add(t);
        }

        eventAcquisitionThread = new Thread("eventAcquisitionThread") {

            @Override
            public void run() {
                eventAcquisitionLoop();
            }
        };
        eventAcquisitionThread.start();

        log.info("start -> done");
    }

    public synchronized void stop() {
        if (stopped) {
            return;
        }

        stopped = true;
        eventAcquisitionThread.interrupt();

        for (Thread t : eventExecutorThreads) {
            t.interrupt();
        }
        eventExecutorThreads.clear();

        log.info("stop -> done");
    }

    private void eventAcquisitionLoop() {
        while (!Thread.currentThread().isInterrupted() && !stopped) {
            try {
                List<Event> acquiredEvents = eventManager.findNextEventsToExecute(maxEventsPerAcquisition);
                if (acquiredEvents.isEmpty()) {
                    sleep(acquisitionDelay);
                    continue;
                }

                for (Event e : acquiredEvents) {
                    acquiredEventQueue.put(e);
                }
            } catch (InterruptedException e) {
                log.info("eventAcquisitionLoop -> interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("eventAcquisitionLoop -> error, retry in {} ms", acquisitionErrorDelay, e);
                sleep(acquisitionErrorDelay);
            }
        }

        log.info("eventAcquisitionLoop -> done");
    }

    private void eventExecutionLoop() {
        while (!Thread.currentThread().isInterrupted() && !stopped) {
            try {
                Event event = acquiredEventQueue.take();
                String processBusinessKey = event.getProcessBusinessKey();
                String eventId = event.getId();

                engine.resume(processBusinessKey, eventId, null);
            } catch (InterruptedException e) {
                log.info("eventExecutionLoop -> interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("eventExecutionLoop -> error, retry in {} ms", executionErrorDelay, e);
                sleep(executionErrorDelay);
            }
        }

        log.info("eventAcquisitionLoop -> done");
    }

    private void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
