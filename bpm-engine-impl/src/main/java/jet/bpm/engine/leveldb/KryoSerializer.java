package jet.bpm.engine.leveldb;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.pool.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.ExecutionContextImpl;
import jet.bpm.engine.commands.A;
import jet.bpm.engine.commands.ExecutionCommand;
import jet.bpm.engine.commands.HandleRaisedErrorCommand;
import jet.bpm.engine.commands.MergeExecutionContextCommand;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.commands.SuspendExecutionCommand;
import jet.bpm.engine.leveldb.index.ExpiredEventIndex.IndexValue;

public class KryoSerializer implements Serializer {

    private final KryoPool kryoPool;

    public KryoSerializer() {

        KryoFactory factory = new KryoFactory() {
            @Override
            public Kryo create() {
                Kryo kryo = new Kryo();
                kryo.setReferences(true);
                kryo.register(IndexValue.class);
                kryo.register(PersistentEvent.class);
                kryo.register(HashSet.class);
                kryo.register(DefaultExecution.class);
                kryo.register(ExecutionContextImpl.class);

                kryo.register(ProcessElementCommand.class);
                kryo.register(ExecutionCommand.class);
                kryo.register(HandleRaisedErrorCommand.class);
                kryo.register(MergeExecutionContextCommand.class);
                kryo.register(SuspendExecutionCommand.class);
                kryo.register(A.class);

                kryo.setClassLoader(Thread.currentThread().getContextClassLoader());

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
