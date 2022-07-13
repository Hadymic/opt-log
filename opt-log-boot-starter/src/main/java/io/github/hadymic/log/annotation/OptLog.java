package io.github.hadymic.log.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * @author Hadymic
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface OptLog {
    /**
     * 成功模板
     */
    String success();

    /**
     * 失败模板
     */
    String fail() default "";

    /**
     * 操作人
     */
    String operator() default "";

    /**
     * 业务Id
     */
    String bizId() default "";

    /**
     * 租户
     */
    String tenant() default "";

    /**
     * 分类
     */
    String category() default "";

    /**
     * 操作类型
     */
    String operate() default "";

    /**
     * 额外信息
     */
    String extra() default "";

    /**
     * 记录条件
     */
    String condition() default "true";

    /**
     * 是否在方法执行前记录日志, 默认为false
     */
    boolean before() default false;
}
