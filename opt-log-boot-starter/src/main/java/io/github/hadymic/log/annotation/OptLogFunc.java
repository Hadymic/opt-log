package io.github.hadymic.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志自定义函数注解
 *
 * @author Hadymic
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptLogFunc {

    /**
     * 自定义函数名称, 默认为方法名
     */
    String value() default "";
}
