package jet.scdp.bpm.model;

public class SequenceFlow extends AbstractElement {

    private final String from;
    private final String to;
    private final String expression;
    private final ExecutionListener[] listeners;

    public SequenceFlow(String id, String from, String to) {
        this(id, from, to, null, null);
    }
    
    public SequenceFlow(String id, String from, String to, String expression) {
        this(id, from, to, expression, null);
    }
    
    public SequenceFlow(String id, String from, String to, ExecutionListener ... listeners) {
        this(id, from, to, null, listeners);
    }

    public SequenceFlow(String id, String from, String to, String expression, ExecutionListener ... listeners) {
        super(id);
        this.from = from;
        this.to = to;
        this.expression = expression;
        this.listeners = listeners;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getExpression() {
        return expression;
    }

    public ExecutionListener[] getListeners() {
        return listeners;
    }
    
    public static final class ExecutionListener {
        
        private final String event;
        private final ExpressionType type;
        private final String expression;

        public ExecutionListener(String event, ExpressionType type, String expression) {
            this.event = event;
            this.type = type;
            this.expression = expression;
        }

        public String getEvent() {
            return event;
        }

        public ExpressionType getType() {
            return type;
        }

        public String getExpression() {
            return expression;
        }
    }
}
