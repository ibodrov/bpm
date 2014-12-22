package jet.scdp.bpm.engine.task;

import jet.scdp.bpm.engine.Execution;

public interface ProcessListener {

    void onExecutionStart(Execution e);

    void onExecutionEnd(Execution e);

    void onElementActivationStart(Execution e, String processDefinitionId, String elementId);

    void onElementActivationEnd(Execution e, String processDefinitionId, String elementId);
}
