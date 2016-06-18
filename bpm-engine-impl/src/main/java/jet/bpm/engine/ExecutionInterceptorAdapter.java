package jet.bpm.engine;

import jet.bpm.engine.api.ExecutionException;

public abstract class ExecutionInterceptorAdapter implements ExecutionInterceptor {

    @Override
    public void onStart(String processBusinessKey) throws ExecutionException {
    }

    @Override
    public void onSuspend() throws ExecutionException {
    }

    @Override
    public void onResume() throws ExecutionException {
    }

    @Override
    public void onFinish(String processBusinessKey) throws ExecutionException {
    }

    @Override
    public void onCommand() throws ExecutionException {
    }
}
