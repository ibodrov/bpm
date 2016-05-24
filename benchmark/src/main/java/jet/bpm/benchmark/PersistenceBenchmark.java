package jet.bpm.benchmark;

import java.util.Arrays;
import java.util.UUID;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.EventBasedGateway;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.StartEvent;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Simple benchmark of the process suspending/resuming.
 */
public class PersistenceBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractBenchmarkState {

        public BenchmarkState() {
            super(false, new ProcessDefinition("test", Arrays.asList(
                    new StartEvent("start"),
                    new SequenceFlow("f1", "start", "gw"),
                    new EventBasedGateway("gw"),
                    new SequenceFlow("f2", "gw", "ev1"),
                    new IntermediateCatchEvent("ev1", "msg"),
                    new SequenceFlow("f3", "ev1", "end"),
                    new EndEvent("end"))));
        }
    }

    @Benchmark
    public void test(BenchmarkState state) {
        try {
            String k = UUID.randomUUID().toString();
            state.getEngine().start(k, "test", null);
            state.getEngine().resume(k, "msg", null);
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
