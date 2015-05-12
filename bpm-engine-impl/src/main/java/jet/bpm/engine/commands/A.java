package jet.bpm.engine.commands;

import jet.bpm.engine.AbstractEngine;
import jet.bpm.engine.BpmnErrorHelper;
import jet.bpm.engine.DefaultExecution;
import jet.bpm.engine.api.ExecutionContext;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.persistence.PersistenceManager;

public class A implements ExecutionCommand {
    
    private final String parentId;

    public A(String parentId) {
        this.parentId = parentId;
    }
    
    @Override
    public DefaultExecution exec(AbstractEngine engine, DefaultExecution execution) throws ExecutionException {
        execution.pop();
        
        ExecutionContext src = execution.getContext();
        
        PersistenceManager pm = engine.getPersistenceManager();
        DefaultExecution parent = pm.get(parentId);
        ExecutionContext dst = parent.getContext();

        String errorRef = BpmnErrorHelper.getRaisedError(src);
        if (errorRef != null) {
            BpmnErrorHelper.raiseError(dst, errorRef);
        }
        return execution;
    }
}
