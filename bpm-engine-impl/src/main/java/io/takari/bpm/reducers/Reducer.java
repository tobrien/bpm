package io.takari.bpm.reducers;

import io.takari.bpm.actions.Action;
import io.takari.bpm.api.ExecutionException;
import io.takari.bpm.state.ProcessInstance;

public interface Reducer {

    Class<? extends Action>[] getAcceptedActions();

    ProcessInstance reduce(ProcessInstance state, Action action) throws ExecutionException;
}
