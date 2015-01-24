package jet.bpm.engine.api;

public interface ActivationListener {

    void onActivation(Execution e, String processDefinitionId, String elementId);
}
