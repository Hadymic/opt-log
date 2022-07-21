package io.github.hadymic.log.annotation;

import io.github.hadymic.log.configuration.OptLogImportSelector;
import io.github.hadymic.log.enums.OptLogSpEL;
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

    /**
     * 是否全局启用{@link OptLog @OptLog}参数的SpEL解析功能
     * 默认全部启用
     */
    OptLogSpEL[] enableSpEL() default {
            OptLogSpEL.SUCCESS, OptLogSpEL.FAIL,
            OptLogSpEL.OPERATOR, OptLogSpEL.BIZ_ID,
            OptLogSpEL.TENANT, OptLogSpEL.CATEGORY,
            OptLogSpEL.OPERATE, OptLogSpEL.EXTRA,
            OptLogSpEL.CONDITION
    };
}
