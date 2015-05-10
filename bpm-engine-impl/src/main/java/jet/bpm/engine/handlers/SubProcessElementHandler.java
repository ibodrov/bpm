package jet.bpm.engine.handlers;

import java.util.Set;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.ExecutionContextHelper;
import jet.bpm.engine.ExecutionContextImpl;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.commands.HandleRaisedErrorCommand;
import jet.bpm.engine.commands.MergeExecutionContextCommand;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.CallActivity;
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

        // add error handling command to stack
        s.push(new HandleRaisedErrorCommand(c));

        // create new child context (variables of the called process)
        ExecutionContext parent = c.getContext();

        // get the ID of the called process. Depends on call type ('sub-process'
        // or 'call activity') it can be:
        // - ID of process, which contains the element of calling process;
        // - ID of external process from separate process definition
        String id = getCalledProcessId(c, sub);

        // first command is put to the called process' stack
        AbstractElement start = ProcessDefinitionUtils.findStartEvent(sub);
        s.push(new ProcessElementCommand(id, start.getId(), parent));
    }

    @Override
    protected ProcessDefinition findCalledProcess(ProcessElementCommand c) throws ExecutionException {
        ProcessDefinition pd = getProcessDefinition(c);
        return ProcessDefinitionUtils.findSubProces(pd, c.getElementId());
    }

    @Override
    protected String getCalledProcessId(ProcessElementCommand c, ProcessDefinition sub) throws ExecutionException {
        return c.getProcessDefinitionId();
    }

    @Override
    protected ExecutionContext createNewContext(ExecutionContext parent) {
        return new ExecutionContextImpl(parent);
    }

    @Override
    protected MergeExecutionContextCommand makeMergeCommand(ExecutionContext parent, ExecutionContext child, Set<VariableMapping> outVariables) {
        return new MergeExecutionContextCommand(parent, child);
    }
}
