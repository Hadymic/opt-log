package io.github.hadymic.log.model;

import lombok.Data;

import java.lang.reflect.Method;

/**
 * @author Hadymic
 */
@Data
public class OptLogFunction {
    private String beanName;
    private String functionName;
    private String methodName;
    private Method method;
    private boolean isStatic;

    public String getBeanMethod(String args) {
        return "@" + beanName + "." + methodName + "(" + args + ")";
    }
}
