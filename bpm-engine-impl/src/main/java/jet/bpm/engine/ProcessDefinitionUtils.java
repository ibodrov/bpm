package jet.bpm.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.BoundaryEvent;
import jet.bpm.engine.model.EndEvent;
import jet.bpm.engine.model.EventBasedGateway;
import jet.bpm.engine.model.ExclusiveGateway;
import jet.bpm.engine.model.InclusiveGateway;
import jet.bpm.engine.model.ParallelGateway;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.SequenceFlow;
import jet.bpm.engine.model.StartEvent;
import jet.bpm.engine.model.SubProcess;

public final class ProcessDefinitionUtils {

    /**
     * Finds process definition by its ID.
     * @param provider
     * @param id
     * @return
     * @throws ExecutionException if the process definition is not found.
     */
    public static ProcessDefinition findProcess(ProcessDefinitionProvider provider, String id) throws ExecutionException {
        ProcessDefinition pd = provider.getById(id);
        if (pd == null) {
            throw new ExecutionException("Unknown process definition '%s'", id);
        }
        return pd;
    }

    /**
     * Finds (sub)process definition by its element ID.
     * @param pd parent process definition.
     * @param id the (sub)process element ID.
     * @return process definition, which contains element with specified ID.
     * @throws ExecutionException if the element is not found in parent or any
     * subprocesses.
     */
    public static ProcessDefinition findElementProcess(ProcessDefinition pd, String id) throws ExecutionException {
        ProcessDefinition sub = findElementProcess0(pd, id);
        if (sub == null) {
            throw new ExecutionException("Invalid process definition '%s': unknown element '%s'", pd.getId(), id);
        }
        return sub;
    }

    private static ProcessDefinition findElementProcess0(ProcessDefinition pd, String id) {
        if (pd.hasChild(id)) {
            return pd;
        }

        for (AbstractElement e : pd.getChildren()) {
            if (e instanceof ProcessDefinition) {
                ProcessDefinition sub = findElementProcess0((ProcessDefinition) e, id);
                if (sub != null) {
                    return sub;
                }
            }
        }

        return null;
    }

    /**
     * Finds element of (sub)process by ID;
     * @param pd
     * @param id
     * @throws ExecutionException if the element is not found.
     */
    public static AbstractElement findElement(ProcessDefinition pd, String id) throws ExecutionException {
        ProcessDefinition sub = findElementProcess(pd, id);
        return sub.getChild(id);
    }

    /**
     * Finds subprocess by ID.
     * @param pd
     * @param id
     * @throws ExecutionException if the subprocesss is not found.
     */
    public static SubProcess findSubProces(ProcessDefinition pd, String id) throws ExecutionException {
        AbstractElement e = findElement(pd, id);
        if (e instanceof SubProcess) {
            return (SubProcess) e;
        } else {
            throw new ExecutionException("Invalid process definition '%s': element '%s' is not a subprocess element", pd.getId(), id);
        }
    }

    /**
     * Finds all outgoind flows for the specified element.
     * @param pd
     * @param from
     * @throws ExecutionException if the element has no outgoing flows..
     */
    public static List<SequenceFlow> findOutgoingFlows(ProcessDefinition pd, String from) throws ExecutionException {
        List<SequenceFlow> result = new ArrayList<>();

        ProcessDefinition sub = findElementProcess(pd, from);
        for (AbstractElement e : sub.getChildren()) {
            if (e instanceof SequenceFlow) {
                SequenceFlow f = (SequenceFlow) e;
                if (from.equals(f.getFrom())) {
                    result.add(f);
                }
            }
        }

        if (result.isEmpty()) {
            throw new ExecutionException("Invalid process definition '%s': no flows from '%s'", pd.getId(), from);
        }

        return result;
    }
    
    public static SequenceFlow findOutgoingFlow(ProcessDefinition pd, String from) throws ExecutionException {
        List<SequenceFlow> l = findOutgoingFlows(pd, from);
        if (l.size() != 1) {
            throw new ExecutionException("Invalid process definition '%s': expected single flow from '%s'", pd.getId(), from);
        }
        return l.get(0);
    }

    /**
     * Finds all incoming flows for the specified element.
     * @param pd
     * @param to
     * @throws ExecutionException if the element has no incoming flows..
     */
    public static List<SequenceFlow> findIncomingFlows(ProcessDefinition pd, String to) throws ExecutionException {
        List<SequenceFlow> result = new ArrayList<>();

        ProcessDefinition sub = findElementProcess(pd, to);
        for (AbstractElement e : sub.getChildren()) {
            if (e instanceof SequenceFlow) {
                SequenceFlow f = (SequenceFlow) e;
                if (to.equals(f.getTo())) {
                    result.add(f);
                }
            }
        }

        if (result.isEmpty()) {
            throw new ExecutionException("Invalid process definition '%s': no flows to '%s'", pd.getId(), to);
        }

        return result;
    }

    /**
     * Finds (first) start event of the process.
     * @param pd
     * @throws ExecutionException if process has no start events.
     */
    public static StartEvent findStartEvent(ProcessDefinition pd) throws ExecutionException {
        for (AbstractElement e : pd.getChildren()) {
            if (e instanceof StartEvent) {
                return (StartEvent) e;
            }
        }

        throw new ExecutionException("Invalid process definition '%s': no start event defined", pd.getId());
    }
    
    public static List<BoundaryEvent> findBoundaryEvents(ProcessDefinition pd, String attachedToRef) throws ExecutionException {
        List<BoundaryEvent> l = new ArrayList<>();
        
        ProcessDefinition sub = findElementProcess(pd, attachedToRef);
        for (AbstractElement e : sub.getChildren()) {
            if (e instanceof BoundaryEvent) {
                BoundaryEvent ev = (BoundaryEvent) e;
                if (attachedToRef.equals(ev.getAttachedToRef())) {
                    l.add(ev);
                }
            }
        }
        
        return l;
    }

    public static BoundaryEvent findBoundaryEvent(ProcessDefinition pd, String attachedToRef, String errorRef) throws ExecutionException {
        List<BoundaryEvent> l = findBoundaryEvents(pd, attachedToRef);
        for (BoundaryEvent ev : l) {
            if (attachedToRef.equals(ev.getAttachedToRef())) {
                if (errorRef != null) {
                    if (errorRef.equals(ev.getErrorRef())) {
                        return ev;
                    }
                } else if (ev.getErrorRef() == null) {
                    return ev;
                }
            }
        }
        return null;
    }
    
    public static Collection<SequenceFlow> filterOutgoingFlows(ProcessDefinition pd, String from, String ... filtered) throws ExecutionException {
        List<SequenceFlow> l = findOutgoingFlows(pd, from);
        
        if (filtered != null && filtered.length > 0) {
            for (Iterator<SequenceFlow> i = l.iterator(); i.hasNext();) {
                SequenceFlow f = i.next();
                for (String id : filtered) {
                    if (id.equals(f.getId())) {
                        i.remove();
                    }
                }
            }
        }
        
        return l;
    }
    
    public static String findNextGatewayId(ProcessDefinition pd, String from) throws ExecutionException {
        AbstractElement e = findElement(pd, from);
        if (!(e instanceof SequenceFlow)) {
            e = findOutgoingFlow(pd, from);
        }
        
        while (e != null) {
            if (e instanceof SequenceFlow) {
                SequenceFlow f = (SequenceFlow) e;
                e = findElement(pd, f.getTo());
            } else if (e instanceof ParallelGateway || e instanceof EventBasedGateway || e instanceof InclusiveGateway || e instanceof ExclusiveGateway) {
                return e.getId();
            } else if (e instanceof EndEvent) {
                return null;
            } else {
                e = findOutgoingFlow(pd, e.getId());
            }
        }
        
        throw new ExecutionException("Invalid process definition '%s': can't find next parallel gateway after '%s'", pd.getId(), from);
    }

    private ProcessDefinitionUtils() {
    }
}
