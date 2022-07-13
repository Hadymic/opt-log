package io.github.hadymic.log.configuration;

import io.github.hadymic.log.annotation.EnableOptLog;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.lang.Nullable;

/**
 * @author Hadymic
 */
public class OptLogImportSelector extends AdviceModeImportSelector<EnableOptLog> {

    private static final String OPT_LOG_CONFIGURATION_CLASS_NAME =
            "io.github.hadymic.log.configuration.OptLogAutoConfiguration";

    @Override
    @Nullable
    protected String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return new String[]{AutoProxyRegistrar.class.getName(), OptLogAutoConfiguration.class.getName()};
            case ASPECTJ:
                return new String[]{OPT_LOG_CONFIGURATION_CLASS_NAME};
            default:
                return null;
        }
    }
}
