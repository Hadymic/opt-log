package io.github.hadymic.log.configuration;

import io.github.hadymic.log.annotation.EnableOptLog;
import io.github.hadymic.log.aop.OptLogAspect;
import io.github.hadymic.log.cache.OptLogFunctionCache;
import io.github.hadymic.log.function.IDiffFunction;
import io.github.hadymic.log.function.impl.DefaultDiffFunctionImpl;
import io.github.hadymic.log.parse.OptLogFunctionParser;
import io.github.hadymic.log.service.IOperatorService;
import io.github.hadymic.log.service.IOptLogService;
import io.github.hadymic.log.service.impl.DefaultOperatorServiceImpl;
import io.github.hadymic.log.service.impl.DefaultOptLogServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;

/**
 * @author Hadymic
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({OptLogProperties.class})
public class OptLogAutoConfiguration implements ImportAware {

    @Nullable
    protected AnnotationAttributes enableOptLog;

    @Bean
    public OptLogFunctionCache optLogFunctionCache() {
        return new OptLogFunctionCache();
    }

    @Bean
    public OptLogFunctionParser optLogFunctionParser(OptLogFunctionCache optLogFunctionCache,
                                                     IOperatorService operatorService,
                                                     OptLogProperties optLogProperties) {
        OptLogFunctionParser parser = new OptLogFunctionParser();
        parser.setFunctionCache(optLogFunctionCache);
        parser.setOperatorService(operatorService);
        parser.setProperties(optLogProperties);
        return parser;
    }

    @Bean
    public OptLogAspect optLogAspect(OptLogFunctionParser optLogFunctionParser,
                                     IOptLogService optLogService) {
        OptLogAspect aspect = new OptLogAspect();
        aspect.setOptLogService(optLogService);
        aspect.setFunctionParser(optLogFunctionParser);
        return aspect;
    }

    @Bean
    @ConditionalOnMissingBean(IOperatorService.class)
    public IOperatorService operatorService() {
        return new DefaultOperatorServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(IOptLogService.class)
    public IOptLogService optLogService() {
        return new DefaultOptLogServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(IDiffFunction.class)
    public IDiffFunction diffFunction(OptLogProperties optLogProperties) {
        DefaultDiffFunctionImpl diffFunction = new DefaultDiffFunctionImpl();
        diffFunction.setProperties(optLogProperties);
        return diffFunction;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableOptLog = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableOptLog.class.getName(), false));
        if (this.enableOptLog == null) {
            throw new IllegalArgumentException(
                    "@EnableOptLog is not present on importing class " + importMetadata.getClassName());
        }
    }
}
