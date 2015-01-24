package jet.scdp.bpm.engine.commands;

import jet.scdp.bpm.api.ExecutionException;
import jet.scdp.bpm.engine.AbstractEngine;
import jet.scdp.bpm.engine.DefaultExecution;
import jet.scdp.bpm.api.ExecutionContext;

public class ProcessElementCommand implements ExecutionCommand {

    private final String processDefinitionId;
    private final String elementId;
    private final String groupId;
    private final boolean exclusive;
    private final ExecutionContext context;

    public ProcessElementCommand(String processDefinitionId, String elementId, ExecutionContext context) {
        this(processDefinitionId, elementId, null, false, context);
    }

    public ProcessElementCommand(String processDefinitionId, String elementId, String groupId, boolean exclusive, ExecutionContext context) {
        this.processDefinitionId = processDefinitionId;
        this.elementId = elementId;
        this.groupId = groupId;
        this.exclusive = exclusive;
        this.context = context;
    }

    public String getProcessDefinitionId() {
        return processDefinitionId;
    }

    public String getElementId() {
        return elementId;
    }

    /**
     * @return идентификатор ветви исполнения. Обычно задается по идентификатору
     * события, породившего ветвь.
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return признак "эклюзивности" выполнения. Используется для определения
     * необходимости выполнения параллельных ветвей процесса.
     */
    public boolean isExclusive() {
        return exclusive;
    }

    @Override
    public ExecutionContext getContext() {
        return context;
    }

    @Override
    public DefaultExecution exec(AbstractEngine engine, DefaultExecution execution) throws ExecutionException {
        // обрабатываем элемент
        engine.getElementHandler().handle(execution, this);

        // нотифицируем о произошедщей активации элемента
        context.onActivation(execution, processDefinitionId, elementId);
        engine.fireOnElementActivation(execution, processDefinitionId, elementId);

        return execution;
    }
}
