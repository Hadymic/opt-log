package io.github.hadymic.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志diff函数字段名注解
 *
 * @author Hadymic
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptLogField {

    /**
     * 字段名
     */
    String value() default "";
}
