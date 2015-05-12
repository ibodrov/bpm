package jet.bpm.engine.api;

public class NoEventFoundException extends ExecutionException {

    public NoEventFoundException(String format, Object... args) {
        super(format, args);
    }
}
