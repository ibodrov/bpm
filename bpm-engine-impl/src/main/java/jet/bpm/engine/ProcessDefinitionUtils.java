package jet.bpm.engine;

import java.util.ArrayList;
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
     * Находит описание процесс по его идентификатору.
     * @param provider
     * @param id
     * @return
     * @throws ExecutionException если искомое описание процесса не найдено.
     */
    public static ProcessDefinition findProcess(ProcessDefinitionProvider provider, String id) throws ExecutionException {
        ProcessDefinition pd = provider.getById(id);
        if (pd == null) {
            throw new ExecutionException("Unknown process definition '%s'", id);
        }
        return pd;
    }

    /**
     * Находит описание (под)процесса по идентификатору элемента процесса.
     * @param pd описание (корневого) процесса.
     * @param id идентификатор элемент искомого процесса.
     * @return если искомый элемент был найден в корневом процессе, то будет
     * возвращено описание корневого процесса. Если искомый элемент был найден
     * в подпроцессе, то будет возвращено описание подпроцесса.
     * @throws ExecutionException если искомый элемент не найден ни в
     * корневом процессе, ни одном из подпроцессов.
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
     * Находит элемент (под)процесса по его идентификатору.
     * @param pd
     * @param id
     * @return элемент процесса.
     * @throws ExecutionException если искомый элемент не найден.
     */
    public static AbstractElement findElement(ProcessDefinition pd, String id) throws ExecutionException {
        ProcessDefinition sub = findElementProcess(pd, id);
        return sub.getChild(id);
    }

    /**
     * Находит подпроцесс по его идентификатору.
     * @param pd
     * @param id
     * @return найденный элемент подпроцесса.
     * @throws ExecutionException если искомый подпроцесс не найден или
     * найденный элемент не является подпроцессом.
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
     * Для указанного элемента находит все исходящие sequence flow.
     * @param pd
     * @param from
     * @return набор найденных flow.
     * @throws ExecutionException если в процессе не задано ни одного flow
     * исходящего из указанного элемента.
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
     * Для указанного элемента находит все входящие sequence flow.
     * @param pd
     * @param to
     * @return набор найденных flow.
     * @throws ExecutionException если в процессе не задано ни одного flow
     * входящие в указанный элемент.
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
     * Находит начальное событие в описании процесса. Поиск в подпроцессах не
     * осуществляется.
     * @param pd
     * @return начальное событие
     * @throws ExecutionException если в процессе не определено ни одного
     * начального события.
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
