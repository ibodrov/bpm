package jet.scdp.bpm.api;

public class ExecutionException extends Exception {

    public ExecutionException(String message) {
        super(message);
    }
    
    public ExecutionException(String format, Object ... args) {
        super(String.format(format, args));
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
