package jet.bpm.engine.api.interceptors;

import jet.bpm.engine.api.ExecutionException;

public interface ExecutionInterceptor {
    
    void onStart(InterceptorStartEvent ev) throws ExecutionException;
    
    void onSuspend() throws ExecutionException;
    
    void onResume() throws ExecutionException;
    
    void onFinish(String processBusinessKey) throws ExecutionException;
    
    void onCommand() throws ExecutionException;
    
    void onError(String processBusinessKey, Throwable cause) throws ExecutionException;
}
