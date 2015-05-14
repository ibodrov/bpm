package jet.bpm.engine;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.commands.ExecutionCommand;
import jet.bpm.engine.event.Event;

public class EventMapHelper {

    private static final String EVENT_MAP_KEY = "__bpmn_event_map";

    @SuppressWarnings("unchecked")
    public static void put(DefaultExecution e, Event ev, ExecutionCommand... commands) throws ExecutionException {
        ExecutionContext ctx = e.getContext();
        Map<UUID, EventRecord> m = (Map<UUID, EventRecord>) ctx.getVariable(EVENT_MAP_KEY);
        if (m == null) {
            m = new HashMap<>();
        }
        
        if (m.containsKey(ev.getId())) {
            throw new ExecutionException("Duplicate event mapping key '%s'", e.getId());
        }
        
        List<ExecutionCommand> l = new ArrayList<>();
        l.addAll(Arrays.asList(commands));
        
        m.put(ev.getId(), new EventRecord(ev.getGroupId(), l));
        ctx.setVariable(EVENT_MAP_KEY, m);
    }
    
    public static boolean isEmpty(DefaultExecution e) {
        ExecutionContext ctx = e.getContext();
        Map<?, ?> m = (Map<?, ?>) ctx.getVariable(EVENT_MAP_KEY);
        return m == null || m.isEmpty();
    }
    
    @SuppressWarnings("unchecked")
    public static void pushCommands(DefaultExecution e, UUID eventId) {
        ExecutionContext ctx = e.getContext();
        Map<UUID, EventRecord> m = (Map<UUID, EventRecord>) ctx.getVariable(EVENT_MAP_KEY);
        if (m == null) {
            return;
        }

        EventRecord r = m.get(eventId);
        if (r == null) {
            return;
        }

        List<ExecutionCommand> l = r.getCommands();
        if (l == null) {
            return;
        }
        
        for (ExecutionCommand c : l) {
            e.push(c);
        }
    }

    @SuppressWarnings("unchecked")
    public static void clearGroup(DefaultExecution s, UUID groupId) {
        if (groupId == null) {
            return;
        }

        ExecutionContext ctx = s.getContext();
        Map<UUID, EventRecord> m = (Map<UUID, EventRecord>) ctx.getVariable(EVENT_MAP_KEY);
        if (m == null) {
            return;
        }

        List<UUID> toDelete = new ArrayList<>();
        for (Map.Entry<UUID, EventRecord> e : m.entrySet()) {
            EventRecord r = e.getValue();
            if (groupId.equals(r.getGroupId())) {
                toDelete.add(e.getKey());
            }
        }

        for (UUID id : toDelete) {
            m.remove(id);
        }

        if (m.isEmpty()) {
            ctx.removeVariable(EVENT_MAP_KEY);
        } else {
            ctx.setVariable(EVENT_MAP_KEY, m);
        }
    }

    public static final class EventRecord implements Serializable {

        private final UUID groupId;
        private final List<ExecutionCommand> commands;

        public EventRecord(UUID groupId, List<ExecutionCommand> commands) {
            this.groupId = groupId;
            this.commands = commands;
        }

        public UUID getGroupId() {
            return groupId;
        }

        public List<ExecutionCommand> getCommands() {
            return commands;
        }
    }
}
