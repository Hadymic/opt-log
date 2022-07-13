package io.github.hadymic.log.annotation;

import io.github.hadymic.log.configuration.OptLogImportSelector;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

import java.lang.annotation.*;

/**
 * 启用操作日志注解
 *
 * @author Hadymic
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(OptLogImportSelector.class)
public @interface EnableOptLog {

    boolean proxyTargetClass() default false;

    AdviceMode mode() default AdviceMode.PROXY;

    int order() default Ordered.LOWEST_PRECEDENCE;
}
