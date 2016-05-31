package jet.bpm.engine.api;

/**
 * A delegated task interface. Any class that implements this interface
 * can be used as a service task called by "delegate expression".
 */
public interface JavaDelegate {

    void execute(ExecutionContext ctx) throws Exception;
}
