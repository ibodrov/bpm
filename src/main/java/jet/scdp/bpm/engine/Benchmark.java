package jet.scdp.bpm.engine;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import jet.scdp.bpm.api.Engine;
import jet.scdp.bpm.model.AbstractElement;
import jet.scdp.bpm.model.EndEvent;
import jet.scdp.bpm.model.EventBasedGateway;
import jet.scdp.bpm.model.IntermediateCatchEvent;
import jet.scdp.bpm.model.ProcessDefinition;
import jet.scdp.bpm.model.SequenceFlow;
import jet.scdp.bpm.model.StartEvent;

public class Benchmark {
    
    private static final String PROCESS_NAME = "bench";
    
    public static void main(String[] args) throws Exception {
        AbstractEngine e = new DefaultEngine();
        ProcessDefinitionProviderImpl pdp = (ProcessDefinitionProviderImpl)e.getProcessDefinitionProvider();
        pdp.add(new ProcessDefinition(PROCESS_NAME, Arrays.<AbstractElement>asList(
                new StartEvent("start"),
                new SequenceFlow("f1", "start", "gw"),
                new EventBasedGateway("gw"),
                    new SequenceFlow("f2", "gw", "ev"),
                    new IntermediateCatchEvent("ev"),
                    new SequenceFlow("f3", "ev", "end"),
                    new EndEvent("end"))));
        
        AtomicLong counter = new AtomicLong();
        
        int threads = 10;
        long iterations = 20000;
        CyclicBarrier barrier = new CyclicBarrier(threads + 1);
        
        new Monitor(counter, barrier).start();
        
        for (int t = 0; t < threads; t++) {
            new Worker(e, counter, iterations, barrier).start();
        }
        
        barrier.await();
    }
    
    public static final class Monitor extends Thread {
        
        private final AtomicLong counter;
        private final CyclicBarrier barrier;

        public Monitor(AtomicLong counter, CyclicBarrier barrier) {
            this.counter = counter;
            this.barrier = barrier;
            setDaemon(true);
        }

        @Override
        public void run() {
            while (!barrier.isBroken()) {
                long was = counter.get();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                long now = counter.get();
                System.out.println(now - was);
            }
        }
    }
    
    public static final class Worker extends Thread {
        
        private final Engine engine;
        private final AtomicLong counter;
        private final long iterations;
        private final CyclicBarrier barrier;

        public Worker(Engine engine, AtomicLong counter, long iterations, CyclicBarrier barrier) {
            this.engine = engine;
            this.counter = counter;
            this.iterations = iterations;
            this.barrier = barrier;
        }
        
        @Override
        public void run() {
            try {
                for (long i = 0; i < iterations; i++) {
                    String key = UUID.randomUUID().toString();
                    
                    engine.run(key, PROCESS_NAME, null);
                    engine.resume(key, "ev", null);
                    
                    counter.incrementAndGet();
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } finally {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException ex) {
                    // ignore
                }
            }
        }
    }
}
