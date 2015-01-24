package jet.bpm.engine.task;

import jet.bpm.engine.DefaultExecution;

public interface ProcessListener {

    void onExecutionStart(DefaultExecution e);

    void onExecutionEnd(DefaultExecution e);

    void onElementActivationStart(DefaultExecution e, String processDefinitionId, String elementId);

    void onElementActivationEnd(DefaultExecution e, String processDefinitionId, String elementId);
}
