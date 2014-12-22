package jet.scdp.bpm.api;

import jet.scdp.bpm.engine.Execution;

public interface ActivationListener {

    void onActivation(Execution e, String processDefinitionId, String elementId);
}
