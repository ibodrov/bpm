package jet.bpm.engine.handlers;

import java.util.Set;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.ExecutionContextHelper;
import jet.bpm.engine.commands.HandleRaisedErrorCommand;
import jet.bpm.engine.commands.MergeExecutionContextCommand;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.AbstractElement;
import jet.bpm.engine.model.CallActivity;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.VariableMapping;

/**
 * Common logic of (sub)process calling.
 */
public abstract class AbstractCallHandler extends AbstractElementHandler {

    public AbstractCallHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    public void handle(DefaultExecution s, ProcessElementCommand c) throws ExecutionException {
        s.pop();
        
        ProcessDefinition sub = findCalledProcess(c);

        // add the error handling command to stack
        s.push(new HandleRaisedErrorCommand(c));

        // TODO refactor out
        Set<VariableMapping> inVariables = null;
        Set<VariableMapping> outVariables = null;
        
        ProcessDefinition pd = getProcessDefinition(c);
        AbstractElement e = ProcessDefinitionUtils.findElement(pd, c.getElementId());
        if (e instanceof CallActivity) {
            inVariables = ((CallActivity)e).getIn();
            outVariables = ((CallActivity)e).getOut();
        }

        // create a new child context (variables of the called process)
        ExecutionContext parent = s.getContext();
        ExecutionContext child = makeChildContext(s);

        // set IN-parameters of the called process
        ExecutionContextHelper.copyVariables(getEngine().getExpressionManager(), parent, child, inVariables);
        
        // make the child context our current, this will be reverted in merge
        // command below
        s.setContext(child);

        // add the context merging command to the current stack. It will perform
        // OUT-parametes handling later
        s.push(makeMergeCommand(parent, child, outVariables));

        // get the ID of the called process. Depending on the call type
        // ('sub-process' or 'call activity') it can be:
        // - ID of process, which contains the element of calling process;
        // - ID of external process from a separate process definition
        String id = getCalledProcessId(c, sub);

        // push the first command to the called process' stack
        AbstractElement start = ProcessDefinitionUtils.findStartEvent(sub);
        s.push(new ProcessElementCommand(id, start.getId()));
    }
    
    protected abstract MergeExecutionContextCommand makeMergeCommand(ExecutionContext parent, ExecutionContext child, Set<VariableMapping> outVariables);

    protected abstract ProcessDefinition findCalledProcess(ProcessElementCommand c) throws ExecutionException;

    protected abstract String getCalledProcessId(ProcessElementCommand c, ProcessDefinition sub) throws ExecutionException;
    
    protected abstract ExecutionContext makeChildContext(DefaultExecution s);
}
