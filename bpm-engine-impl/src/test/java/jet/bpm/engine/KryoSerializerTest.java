package jet.bpm.engine;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.leveldb.KryoSerializer;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

public class KryoSerializerTest {

    @Test
    public void hashSetSerialize() throws Exception {
        KryoSerializer s = new KryoSerializer();

        Set<String> eventNames = new HashSet<>(Arrays.asList("a", "b", "c"));

        byte[] bytes = s.toBytes(eventNames);
        assertNotNull(bytes);

        assertEquals(eventNames, s.fromBytes(bytes));
    }

    @Test
    @Ignore
    public void defaultExecutionSerialize() throws Exception {
        KryoSerializer s = new KryoSerializer();
        
        ExecutionContextImpl c = new ExecutionContextImpl(null);
        c.setVariable("v1", "v2");
        c.setVariable("v2", new Service("sid"));

        DefaultExecution e = new DefaultExecution(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "bus-key", c);

        ProcessElementCommand cmd1 = new ProcessElementCommand("pid", "eid");

        e.push(cmd1);

        byte[] bytes = s.toBytes(e);
        assertNotNull(bytes);

        DefaultExecution ee = (DefaultExecution) s.fromBytes(bytes);
        assertEquals(e.getId(), ee.getId());
        assertEquals(e.getBusinessKey(), ee.getBusinessKey());
        assertEquals(e.getParentId(), ee.getParentId());
        assertEquals(e.getRootId(), ee.getRootId());
        assertEquals(e.size(), ee.size());
    }

    private static class Service {
        private final String sid;

        public Service(String sid) {
            this.sid = sid;
        }

        public String getSid() {
            return sid;
        }
    }
}
