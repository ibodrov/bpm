package jet.bpm.examples.async;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import jet.bpm.engine.Engines;
import jet.bpm.engine.ExecutionContextHelper;
import jet.bpm.engine.ProcessDefinitionProvider;
import jet.bpm.engine.api.Engine;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.api.JavaDelegate;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.ExpressionType;
import jet.bpm.engine.model.InclusiveGateway;
import jet.bpm.engine.model.IntermediateCatchEvent;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.ServiceTask;
import jet.bpm.engine.model.StartEvent;
import jet.bpm.engine.task.ServiceTaskRegistry;

public class Main {
    
    private static final String DEFINITION_ID = "myProcess";

    public static void main(String[] args) throws Exception {
        TaskExecutor executor = new TaskExecutorImpl();
        ServiceTaskRegistryImpl taskRegistry = new ServiceTaskRegistryImpl(executor);
        Engine engine = Engines.inMemory(new ProcessDefinitionProviderImpl(), taskRegistry);
        taskRegistry.setEngine(engine);
        
        // gentlemen, start your engines
        
        String key = "abc";
        engine.start(key, DEFINITION_ID, null);
        
        Thread.sleep(10000);
        
        // we don't care about stopping correctly
        System.exit(0);
    }
    
    /**
     * Provider for out process definition. Here you could use an external
     * storage, convert from other formats, etc.
     * 
     * We will define the process like this:
     *
     *  start --> gw1 --> t1 --> ev1 --> gw2 --> end
     *               \                  /
     *                --> t2 --> ev2 -->
     *
     * In the real-world usage there should be an event based gateway on
     * each taskName to handle timeouts and other errors.
     */
    public static final class ProcessDefinitionProviderImpl implements ProcessDefinitionProvider {

        @Override
        public ProcessDefinition getById(String id) throws ExecutionException {
            
            return new ProcessDefinition(DEFINITION_ID, Arrays.asList(
                    new StartEvent("start"),
                    new SequenceFlow("f1", "start", "gw1"),
                    new InclusiveGateway("gw1"),

                        new SequenceFlow("f2", "gw1", "t1"),
                        new ServiceTask("t1", ExpressionType.DELEGATE, "${taskA}"),
                        new SequenceFlow("f3", "t1", "ev1"),
                        new IntermediateCatchEvent("ev1", "taskA"),
                        new SequenceFlow("f4", "ev1", "gw2"),

                        new SequenceFlow("f5", "gw1", "t2"),
                        new ServiceTask("t2", ExpressionType.DELEGATE, "${taskB}"),
                        new SequenceFlow("f6", "t2", "ev2"),
                        new IntermediateCatchEvent("ev2", "taskB"),
                        new SequenceFlow("f7", "ev2", "gw2"),

                new InclusiveGateway("gw2"),
                new SequenceFlow("f8", "gw2", "end"),
                new EndEvent("end")
            ));
        }
    }
    
    /**
     * Our custom service tasks registry. Instead of executing "real" tasks,
     * it will return proxies which send a taskName to the task executor.
     */
    public static final class ServiceTaskRegistryImpl implements ServiceTaskRegistry {

        private final TaskExecutor executor;
        private Engine engine;

        public ServiceTaskRegistryImpl(TaskExecutor executor) {
            this.executor = executor;
        }

        public void setEngine(Engine engine) {
            this.engine = engine;
        }
        
        @Override
        public Object getByKey(final String key) {
            return new JavaDelegate() {
                @Override
                public void execute(final ExecutionContext ctx) throws Exception {
                    final String processKey = (String) ctx.getVariable(ExecutionContextHelper.PROCESS_BUSINESS_KEY);
                    final String eventName = key;
                    
                    executor.submit(key, () -> {
                        try {
                            // TODO: optimistic locking. There could be a case
                            // when we get an event notification before
                            // finishing the main execution
                            engine.resume(processKey, eventName, null);
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    });
                }
            };
        }
    }
    
    /**
     * Our custom task executor. It could be a distributed task executor or
     * a local fork/join pool or whatever you like.
     * 
     * Receives taskNames to start a task and sends notification on a task
     * completion.
     * 
     * A simple callback is used to notify for the task completion. In real
     * world, it could be replaced with selectors, promises, etc.
     */
    public interface TaskExecutor {
        
        void submit(String taskName, TaskCallback callback);
    }
    
    public static final class TaskExecutorImpl implements TaskExecutor {
        
        private final Executor executor = Executors.newCachedThreadPool();
        private final Map<String, Runnable> tasks = new HashMap<>();
        
        public TaskExecutorImpl() {
            tasks.put("taskA", new LongTask("TaskA", 5000));
            tasks.put("taskB", new LongTask("TaskB", 3000));
        }
        
        @Override
        public void submit(String taskName, final TaskCallback callback) {
            final Runnable r = tasks.get(taskName);
            
            executor.execute(() -> {
                r.run();
                callback.onCompletion();
            });
        }
    }
    
    public interface TaskCallback {
        
        void onCompletion();
    }
    
    /**
     * Example of a long-running task.
     */
    public static final class LongTask implements Runnable {
        
        private final String name;
        private final long workAmount;

        public LongTask(String name, long workAmount) {
            this.name = name;
            this.workAmount = workAmount;
        }

        @Override
        public void run() {
            try {
                System.out.println("Started: " + name);
                Thread.sleep(workAmount);
                System.out.println("Finished: " + name);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
