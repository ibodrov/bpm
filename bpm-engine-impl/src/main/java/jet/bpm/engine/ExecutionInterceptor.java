package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionException;

public interface ExecutionInterceptor {
    
    void onStart(String processBusinessKey) throws ExecutionException;
    
    void onSuspend() throws ExecutionException;
    
    void onResume() throws ExecutionException;
    
    void onFinish(String processBusinessKey) throws ExecutionException;
    
    void onCommand() throws ExecutionException;
}
