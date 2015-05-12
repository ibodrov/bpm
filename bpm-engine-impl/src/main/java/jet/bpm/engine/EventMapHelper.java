package jet.bpm.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.commands.ExecutionCommand;

public class EventMapHelper {

    private static final String EVENT_MAP_KEY = "__bpmn_event_map";

    public static void put(DefaultExecution e, String eventName, ExecutionCommand... commands) throws ExecutionException {
        ExecutionContext ctx = e.getContext();
        Map<String, List<ExecutionCommand>> m = (Map<String, List<ExecutionCommand>>) ctx.getVariable(EVENT_MAP_KEY);
        if (m == null) {
            m = new HashMap<>();
        }
        
        if (m.containsKey(eventName)) {
            throw new ExecutionException("Duplicate event mapping key '%s'", eventName);
        }
        
        List<ExecutionCommand> l = new ArrayList<>();
        l.addAll(Arrays.asList(commands));
        
        m.put(eventName, l);
        ctx.setVariable(EVENT_MAP_KEY, m);
    }
    
    public static boolean isEmpty(DefaultExecution e) {
        ExecutionContext ctx = e.getContext();
        Map<?, ?> m = (Map<?, ?>) ctx.getVariable(EVENT_MAP_KEY);
        return m == null || m.isEmpty();
    }
    
    public static void pushCommands(DefaultExecution e, String eventName) {
        ExecutionContext ctx = e.getContext();
        Map<String, List<ExecutionCommand>> m = (Map<String, List<ExecutionCommand>>) ctx.getVariable(EVENT_MAP_KEY);
        if (m == null) {
            return;
        }
        
        List<ExecutionCommand> l = m.get(eventName);
        if (l == null) {
            return;
        }
        
        for (ExecutionCommand c : l) {
            e.push(c);
        }
    }
}
