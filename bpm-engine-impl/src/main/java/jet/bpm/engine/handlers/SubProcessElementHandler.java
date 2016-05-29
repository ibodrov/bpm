package jet.bpm.engine.handlers;

import java.util.Set;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.ExecutionContextImpl;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.commands.HandleRaisedErrorCommand;
import jet.bpm.engine.commands.MergeExecutionContextCommand;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.VariableMapping;

public class SubProcessElementHandler extends AbstractCallHandler {

    public SubProcessElementHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();

        ProcessDefinition sub = findCalledProcess(c);

        // add an error handling command to the stack
        s.push(new HandleRaisedErrorCommand(c));

        // get the ID of the called process. Depending on the call type
        // ('sub-process' or 'call activity') it can be:
        // - ID of process, which contains the element of calling process;
        // - ID of external process from a separate process definition
        String id = getCalledProcessId(c, sub);

        // push the first command to the called process' stack
        AbstractElement start = ProcessDefinitionUtils.findStartEvent(sub);
        s.push(new ProcessElementCommand(id, start.getId()));
    }

    @Override
    protected ProcessDefinition findCalledProcess(ProcessElementCommand c) throws ExecutionException {
        ProcessDefinition pd = getProcessDefinition(c);
        return ProcessDefinitionUtils.findSubProcess(pd, c.getElementId());
    }

    @Override
    protected String getCalledProcessId(ProcessElementCommand c, ProcessDefinition sub) throws ExecutionException {
        return c.getProcessDefinitionId();
    }

    @Override
    protected MergeExecutionContextCommand makeMergeCommand(ExecutionContext parent, ExecutionContext child, Set<VariableMapping> outVariables) {
        return new MergeExecutionContextCommand(parent);
    }

    @Override
    protected ExecutionContext makeChildContext(DefaultExecution s) {
        return new ExecutionContextImpl(s.getContext());
    }
}
