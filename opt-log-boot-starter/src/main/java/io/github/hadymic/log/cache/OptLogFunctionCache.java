package io.github.hadymic.log.cache;

import io.github.hadymic.log.annotation.OptLogFunc;
import io.github.hadymic.log.model.OptLogFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Hadymic
 */
@Slf4j
public class OptLogFunctionCache implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Map<String, OptLogFunction> functionMap;

    @EventListener(ApplicationStartedEvent.class)
    public void init() {
        functionMap = new HashMap<>();
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(OptLogFunc.class);
        if (CollectionUtils.isEmpty(beanMap)) {
            return;
        }
        for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
            Class<?> targetClass = AopUtils.getTargetClass(entry.getValue());
            Method[] methods = targetClass.getMethods();
            for (Method method : methods) {
                OptLogFunc methodFunc = AnnotationUtils.findAnnotation(method, OptLogFunc.class);
                if (methodFunc == null) {
                    continue;
                }
                setCache(entry.getKey(), method, methodFunc);
            }
        }
    }

    private void setCache(String beanName, Method method, OptLogFunc methodFunc) {
        String functionName = methodFunc.value();
        if (!StringUtils.hasText(functionName)) {
            functionName = method.getName();
        }

        OptLogFunction function = new OptLogFunction();
        function.setBeanName(beanName);
        function.setFunctionName(functionName);
        function.setMethodName(method.getName());
        function.setMethod(method);
        function.setStatic(Modifier.isStatic(method.getModifiers()));
        if (functionMap.containsKey(functionName)) {
            log.warn("opt log custom function init warn: function '{}' is duplicated", functionName);
        }
        functionMap.put(functionName, function);
        log.debug("opt log register custom function: {}", functionName);
    }

    public OptLogFunction getFunction(String functionName) {
        return functionMap.get(functionName);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
