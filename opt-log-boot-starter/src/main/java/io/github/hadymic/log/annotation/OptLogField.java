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

    /**
     * 自定义函数, 使用SpEL进行解析
     * 参数占位符为__field, 请注意使用
     * 由于使用的是同一个Context, 所以也可以获取到上下文
     * 例: 自定义一个user函数, 用于将user id转为user name
     * function 可以填写 #user(#__field)
     */
    String function() default "";
}
