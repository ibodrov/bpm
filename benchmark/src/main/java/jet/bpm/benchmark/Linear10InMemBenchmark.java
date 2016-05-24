package jet.bpm.benchmark;

import java.util.Arrays;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.ExpressionType;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.ServiceTask;
import jet.bpm.engine.model.StartEvent;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

/**
 * Linear process with 10 JUEL tasks. It is not really meaningful because
 * it is basically a JUEL benchmark.
 */
public class Linear10InMemBenchmark {

    @State(Scope.Benchmark)
    public static class BenchmarkState extends AbstractBenchmarkState {

        public BenchmarkState() {
            super(new ProcessDefinition("test", Arrays.asList(
                    new StartEvent("start"),
                    new SequenceFlow("f1", "start", "t1"),
                    new ServiceTask("t1", ExpressionType.SIMPLE, "${1 + 1}"),
                    new SequenceFlow("f2", "t1", "t2"),
                    new ServiceTask("t2", ExpressionType.SIMPLE, "${2 + 1}"),
                    new SequenceFlow("f3", "t2", "t3"),
                    new ServiceTask("t3", ExpressionType.SIMPLE, "${3 + 1}"),
                    new SequenceFlow("f4", "t3", "t4"),
                    new ServiceTask("t4", ExpressionType.SIMPLE, "${4 + 1}"),
                    new SequenceFlow("f5", "t4", "t5"),
                    new ServiceTask("t5", ExpressionType.SIMPLE, "${5 + 1}"),
                    new SequenceFlow("f6", "t5", "t6"),
                    new ServiceTask("t6", ExpressionType.SIMPLE, "${6 + 1}"),
                    new SequenceFlow("f7", "t6", "t7"),
                    new ServiceTask("t7", ExpressionType.SIMPLE, "${7 + 1}"),
                    new SequenceFlow("f8", "t7", "t8"),
                    new ServiceTask("t8", ExpressionType.SIMPLE, "${8 + 1}"),
                    new SequenceFlow("f9", "t8", "t9"),
                    new ServiceTask("t9", ExpressionType.SIMPLE, "${9 + 1}"),
                    new SequenceFlow("f10", "t9", "t10"),
                    new ServiceTask("t10", ExpressionType.SIMPLE, "${10 + 1}"),
                    new SequenceFlow("f11", "t10", "end"),
                    new EndEvent("end"))));
        }
    }

    @Benchmark
    public void test(BenchmarkState state) {
        try {
            state.getEngine().start("a", "test", null);
        } catch (ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
