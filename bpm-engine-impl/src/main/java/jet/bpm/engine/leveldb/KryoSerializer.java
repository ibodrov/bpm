package jet.bpm.engine.leveldb;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Kryo.DefaultInstantiatorStrategy;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.EventMapHelper.EventRecord;
import jet.bpm.engine.ExecutionContextImpl;
import jet.bpm.engine.commands.ExecutionCommand;
import jet.bpm.engine.commands.HandleRaisedErrorCommand;
import jet.bpm.engine.commands.MergeExecutionContextCommand;
import jet.bpm.engine.commands.PersistExecutionCommand;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.commands.ProcessEventMappingCommand;
import jet.bpm.engine.commands.SuspendExecutionCommand;
import jet.bpm.engine.event.Event;
import jet.bpm.engine.event.ExpiredEvent;
import org.objenesis.instantiator.ObjectInstantiator;
import org.objenesis.strategy.StdInstantiatorStrategy;

public class KryoSerializer implements Serializer {

    private final KryoPool kryoPool;

    public KryoSerializer() {

        KryoFactory factory = new KryoFactory() {
            @Override
            public Kryo create() {
                Kryo kryo = new Kryo();
                
                kryo.setReferences(true);
                kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
                
                kryo.register(UUID.class);
                kryo.register(Event.class);
                kryo.register(ExpiredEvent.class);
                kryo.register(HashSet.class);
                kryo.register(HashMap.class);
                kryo.register(ConcurrentLinkedDeque.class);
                kryo.register(DefaultExecution.class);
                kryo.register(ExecutionContextImpl.class);
                kryo.register(EventRecord.class);

                kryo.register(ProcessElementCommand.class);
                kryo.register(ExecutionCommand.class);
                kryo.register(HandleRaisedErrorCommand.class);
                kryo.register(MergeExecutionContextCommand.class);
                kryo.register(SuspendExecutionCommand.class);
                kryo.register(ProcessEventMappingCommand.class);
                kryo.register(PersistExecutionCommand.class);

                kryo.setClassLoader(Thread.currentThread().getContextClassLoader());
                
                // в ExecutionContextImpl используется
                // Collections.synchronizedMap, который возвращает экземпляр
                // класса с видимостью private - это w/a для его корректной
                // десериализации
                Class<?> klass = Collections.synchronizedMap(new HashMap<String, Object>()).getClass();
                kryo.register(klass);
                kryo.getRegistration(klass).setInstantiator(new ObjectInstantiator() {

                    @Override
                    public Object newInstance() {
                        return Collections.synchronizedMap(new HashMap<String, Object>());
                    }
                });

                return kryo;
            }
        };

        this.kryoPool = new KryoPool.Builder(factory).softReferences().build();
    }

    @Override
    public byte[] toBytes(Object n) {
        Kryo kryo = kryoPool.borrow();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (Output output = new Output(bos)) {
            kryo.writeClassAndObject(output, n);
        } finally {
            kryoPool.release(kryo);
        }
        return bos.toByteArray();
    }

    @Override
    public Object fromBytes(byte[] bytes) {
        Kryo kryo = kryoPool.borrow();

        try (Input input = new Input(new ByteArrayInputStream(bytes))) {
            return kryo.readClassAndObject(input);
        } finally {
            kryoPool.release(kryo);
        }
    }
}
