package jet.bpm.engine.handlers;

import java.util.Set;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.ExecutionContextHelper;
import jet.bpm.engine.ExecutionContextImpl;
import jet.bpm.engine.commands.HandleRaisedErrorCommand;
import jet.bpm.engine.commands.MergeExecutionContextCommand;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.CallActivity;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.VariableMapping;

/**
 * Общая логика обработки элементов вызова (под)процессов.
 */
public abstract class AbstractCallHandler extends AbstractElementHandler {

    public AbstractCallHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        // находим вызываемый процесс BPM
        ProcessDefinition sub = findCalledProcess(c);

        // помещаем на стек команду обработки ошибок BPM
        s.push(new HandleRaisedErrorCommand(c));

        // TODO refactor out
        Set<VariableMapping> inVariables = null;
        Set<VariableMapping> outVariables = null;

        ProcessDefinition pd = getProcessDefinition(c);
        AbstractElement e = pd.getChild(c.getElementId());
        if (e instanceof CallActivity) {
            inVariables = ((CallActivity)e).getIn();
            outVariables = ((CallActivity)e).getOut();
        }

        // создаем новый побочный контекст (фактически, набор параметров
        // вызываемого процесса)
        ExecutionContext parent = c.getContext();
        ExecutionContext child = new ExecutionContextImpl();

        // IN-параметры
        ExecutionContextHelper.copyVariables(getEngine().getExpressionManager(), parent, child, inVariables);

        // помещаем на стек команду объединения контекстов вызываемого процесса
        // и вызывающего процесса. Тем самым обеспечивается передача
        // OUT-параметров
        s.push(new MergeExecutionContextCommand(parent, child, outVariables));

        // определяем ID вызываемого процесса. В зависимости от типа вызова
        // ('sub-process' или 'call activity') это может быть:
        // - ID процесса, содержащего в себе элемент вызываемого процесса;
        // - ID независимого процесса, отдельно установленного в реестре
        String id = getCalledProcessId(c, sub);

        // помещаем на стек начальный элемент процесса
        AbstractElement start = ProcessDefinitionUtils.findStartEvent(sub);
        s.push(new ProcessElementCommand(id, start.getId(), child));
    }

    protected abstract ProcessDefinition findCalledProcess(ProcessElementCommand c) throws ExecutionException;

    protected abstract String getCalledProcessId(ProcessElementCommand c, ProcessDefinition sub) throws ExecutionException;
}
