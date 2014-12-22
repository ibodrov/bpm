package jet.scdp.bpm.api;

public interface ActivationListener {

    void onActivation(Execution e, String processDefinitionId, String elementId);
}
