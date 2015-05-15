package jet.bpm.engine.model;

public class ServiceTask extends AbstractElement {
    
    private String name;
    private final String expression;
    private final ExpressionType type;

    public ServiceTask(String id) {
        this(id, ExpressionType.NONE, null);
    }

    public ServiceTask(String id, ExpressionType type, String expression) {
        super(id);
        this.type = type;
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public ExpressionType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }            
}
