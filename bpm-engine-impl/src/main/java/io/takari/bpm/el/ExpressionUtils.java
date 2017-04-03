package io.takari.bpm.el;

import io.takari.bpm.api.ExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ExpressionUtils {

    public static Object interpolate(ExpressionManager em, ExecutionContext ctx, Object v) {
        if (v instanceof String) {
            return em.eval(ctx, (String) v, Object.class);
        } else if (v instanceof Map) {
            Map<Object, Object> m = (Map<Object, Object>) v;
            if (m.isEmpty()) {
                return v;
            }

            for (Map.Entry<Object, Object> e : m.entrySet()) {
                m.put(e.getKey(), interpolate(em, ctx, e.getValue()));
            }

            return m;
        } else if (v instanceof List) {
            List src = (List) v;
            if (src.isEmpty()) {
                return v;
            }

            List dst = new ArrayList(src.size());
            for (Object vv : src) {
                dst.add(interpolate(em, ctx, vv));
            }

            return dst;
        } else if (v instanceof Object[]) {
            Object[] src = (Object[]) v;
            if (src.length == 0) {
                return v;
            }

            for (int i = 0; i < src.length; i++) {
                src[i] = interpolate(em, ctx, src[i]);
            }
        }

        return v;
    }

    private ExpressionUtils() {
    }
}
