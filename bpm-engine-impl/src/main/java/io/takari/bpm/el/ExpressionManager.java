package io.takari.bpm.el;

import io.takari.bpm.api.ExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ExpressionManager {

    <T> T eval(ExecutionContext ctx, String expr, Class<T> type);

    Object interpolate(ExecutionContext ctx, Object v);
}
