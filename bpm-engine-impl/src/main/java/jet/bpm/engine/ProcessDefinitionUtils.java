package jet.bpm.engine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.BoundaryEvent;
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

    public static BoundaryEvent findBoundaryEvent(ProcessDefinition pd, String attachedToRef, String errorRef) throws ExecutionException {
        ProcessDefinition sub = findElementProcess(pd, attachedToRef);

        for (AbstractElement e : sub.getChildren()) {
            if (e instanceof BoundaryEvent) {
                BoundaryEvent ev = (BoundaryEvent) e;
                if (attachedToRef.equals(ev.getAttachedToRef())) {
                    if(errorRef != null) {
                        if (errorRef.equals(ev.getErrorRef())) {
                            return ev;
                        }
                    } else if (ev.getErrorRef() == null) {
                        return ev;
                    }
                }
            }
        }

        return null;
    }

    private ProcessDefinitionUtils() {
    }
}
