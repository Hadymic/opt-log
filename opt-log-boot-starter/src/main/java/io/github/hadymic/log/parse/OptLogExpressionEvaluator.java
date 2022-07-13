package io.github.hadymic.log.parse;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.CachedExpressionEvaluator;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;

/**
 * @author Hadymic
 */
public class OptLogExpressionEvaluator extends CachedExpressionEvaluator {

    public StandardEvaluationContext createEvaluationContext(Method method, Object[] args,
                                                             @Nullable BeanFactory beanFactory) {
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(
                new Object(), method, args, getParameterNameDiscoverer());
        if (beanFactory != null) {
            evaluationContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
        }
        return evaluationContext;
    }
}
