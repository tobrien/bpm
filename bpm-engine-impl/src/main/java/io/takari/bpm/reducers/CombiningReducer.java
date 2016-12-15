package io.takari.bpm.reducers;

import io.takari.bpm.actions.Action;
import io.takari.bpm.api.ExecutionException;
import io.takari.bpm.state.ProcessInstance;

import java.util.HashMap;
import java.util.Map;

public class CombiningReducer implements Reducer {

    private final Map<Class<? extends Action>, Reducer> reducers;

    public CombiningReducer(Reducer... rs) {
        this.reducers = new HashMap<>();
        for (Reducer r : rs) {
            for (Class<? extends Action> c : r.getAcceptedActions()) {
                this.reducers.put(c, r);
            }
        }
    }

    @Override
    public Class<? extends Action>[] getAcceptedActions() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public ProcessInstance reduce(ProcessInstance state, Action action) throws ExecutionException {
        Reducer r = reducers.get(action.getClass());
        if (r == null) {
            throw new IllegalArgumentException("Unsupported action type: " + action.getClass());
        }

        return r.reduce(state, action);
    }
}
