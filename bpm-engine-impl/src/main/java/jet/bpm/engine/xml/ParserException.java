package jet.bpm.engine.xml;

public class ParserException extends Exception {

    public ParserException(String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
