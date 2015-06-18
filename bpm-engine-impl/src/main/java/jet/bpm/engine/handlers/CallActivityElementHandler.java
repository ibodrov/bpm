package jet.bpm.engine.handlers;

import java.util.Set;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.ExecutionContextImpl;
import jet.bpm.engine.IndexedProcessDefinitionProvider;
import jet.bpm.engine.ProcessDefinitionUtils;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.commands.MergeExecutionContextCommand;
import jet.bpm.engine.commands.ProcessElementCommand;
import jet.bpm.engine.model.CallActivity;
import jet.bpm.engine.model.ProcessDefinition;
import jet.bpm.engine.model.VariableMapping;

public class CallActivityElementHandler extends AbstractCallHandler {

    public CallActivityElementHandler(AbstractEngine engine) {
        super(engine);
    }

    @Override
    protected ProcessDefinition findCalledProcess(ProcessElementCommand c) throws ExecutionException {
        ProcessDefinition pd = getProcessDefinition(c);
        CallActivity act = (CallActivity) ProcessDefinitionUtils.findElement(pd, c.getElementId());

        IndexedProcessDefinitionProvider provider = getEngine().getProcessDefinitionProvider();
        return provider.getById(act.getCalledElement());
    }

    @Override
    protected String getCalledProcessId(ProcessElementCommand c, ProcessDefinition sub) throws ExecutionException {
        return sub.getId();
    }

    @Override
    protected MergeExecutionContextCommand makeMergeCommand(ExecutionContext parent, ExecutionContext child, Set<VariableMapping> outVariables) {
        return new MergeExecutionContextCommand(parent, outVariables);
    }

    @Override
    protected ExecutionContext makeChildContext(DefaultExecution s) {
        return new ExecutionContextImpl(null);
    }
}
