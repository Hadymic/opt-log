package io.github.hadymic.log.context;

import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author Hadymic
 */
public class OptLogContext {

    private static final InheritableThreadLocal<StandardEvaluationContext> CONTEXT_THREAD_LOCAL = new InheritableThreadLocal<>();

    public static void setContext(StandardEvaluationContext context) {
        StandardEvaluationContext get = getContext();
        if (get != null) {
            return;
        }
        CONTEXT_THREAD_LOCAL.set(context);
    }

    public static StandardEvaluationContext getContext() {
        return CONTEXT_THREAD_LOCAL.get();
    }

    public static void putVariable(String key, Object value) {
        StandardEvaluationContext context = getContext();
        context.setVariable(key, value);
        CONTEXT_THREAD_LOCAL.set(context);
    }

    public static Object getVariable(String key) {
        StandardEvaluationContext context = getContext();
        return context.lookupVariable(key);
    }

    public static void clear() {
        CONTEXT_THREAD_LOCAL.remove();
    }
}
