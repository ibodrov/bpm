package jet.bpm.engine;

import jet.bpm.engine.api.Execution;

public interface ActivationListener {

    /**
     * Fires on the process element activation.
     * @param e process execution instance.
     * @param processDefinitionId the ID of process definition.
     * @param elementId the ID of the activated element.
     */
    void onActivation(Execution e, String processDefinitionId, String elementId);
}
