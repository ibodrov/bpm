package jet.bpm.engine;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.Callable;
import jet.bpm.engine.api.ExecutionException;
import jet.bpm.engine.model.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndexedProcessDefinitionProvider {

    private static final Logger log = LoggerFactory.getLogger(IndexedProcessDefinitionProvider.class);

    private final ProcessDefinitionProvider delegate;
    private final Cache<String, IndexedProcessDefinition> cache;

    public IndexedProcessDefinitionProvider(final ProcessDefinitionProvider delegate) {
        this.delegate = delegate;
        this.cache = CacheBuilder.newBuilder().build();
    }

    public IndexedProcessDefinition getById(final String key) throws ExecutionException {
        try {
            return cache.get(key, new Callable<IndexedProcessDefinition>() {

                @Override
                public IndexedProcessDefinition call() throws Exception {
                    ProcessDefinition pd = delegate.getById(key);
                    IndexedProcessDefinition ipd = new IndexedProcessDefinition(pd);
                    log.info("getById ['{}'] -> indexing done", key);
                    return ipd;
                }
            });
        } catch (java.util.concurrent.ExecutionException e) {
            throw new ExecutionException("Error getting an process definition: '" + key + "'", e);
        }
    }
}
