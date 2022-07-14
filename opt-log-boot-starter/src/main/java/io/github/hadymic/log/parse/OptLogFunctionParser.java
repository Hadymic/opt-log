package io.github.hadymic.log.parse;

import io.github.hadymic.log.cache.OptLogFunctionCache;
import io.github.hadymic.log.configuration.OptLogProperties;
import io.github.hadymic.log.context.OptLogContext;
import io.github.hadymic.log.enums.OptLogStatus;
import io.github.hadymic.log.model.MethodExecuteResult;
import io.github.hadymic.log.model.OptLogFunction;
import io.github.hadymic.log.model.OptLogOps;
import io.github.hadymic.log.model.OptLogRecord;
import io.github.hadymic.log.service.IOperatorService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Hadymic
 */
public class OptLogFunctionParser implements BeanFactoryAware {

    private static final Pattern FUNCTION_PATTERN = Pattern.compile("#(.*?)\\(");
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("#\\{(.*?)}");

    private OptLogFunctionCache functionCache;

    private OptLogProperties properties;

    private IOperatorService operatorService;

    private BeanFactory beanFactory;

    private final OptLogExpressionEvaluator expressionEvaluator = new OptLogExpressionEvaluator();
    private final SpelExpressionParser expressionParser = new SpelExpressionParser();
    private final TemplateParserContext templateParserContext = new TemplateParserContext();

    public void initContext(Method method, Object[] args) {
        StandardEvaluationContext context = expressionEvaluator
                .createEvaluationContext(method, args, beanFactory);
        OptLogContext.setContext(context);
    }

    public List<OptLogRecord> resolveBefore(List<OptLogOps> opsList) {
        if (CollectionUtils.isEmpty(opsList)) {
            return new ArrayList<>();
        }
        return opsList.stream()
                .map(ops -> resolve(ops, null, null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<OptLogRecord> resolveAfter(List<OptLogOps> opsList, Object result, MethodExecuteResult executeResult) {
        if (CollectionUtils.isEmpty(opsList)) {
            return new ArrayList<>();
        }
        OptLogContext.putVariable(properties.getVariable().getResult(), result);
        OptLogContext.putVariable(properties.getVariable().getErrorMsg(), executeResult.getErrorMsg());

        return opsList.stream()
                .map(ops -> resolve(ops, result, executeResult))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private OptLogRecord resolve(OptLogOps ops, Object result, MethodExecuteResult executeResult) {
        Boolean condition = resolveTemplate(ops.getCondition(), Boolean.class);
        if (condition == null || !condition) {
            return null;
        }

        Object bizId = resolveTemplate(ops.getBizId());
        Object tenant = resolveTemplate(ops.getTenant());
        Object category = resolveTemplate(ops.getCategory());
        Object operate = resolveTemplate(ops.getOperate());
        Object extra = resolveTemplate(ops.getExtra());

        Object content;
        OptLogStatus status = OptLogStatus.BEFORE;
        Long operateTime = null;
        Long executeTime = null;
        String errorMsg = null;
        if (executeResult == null) {
            content = resolveTemplate(ops.getSuccess());
        } else {
            if (executeResult.isSuccess()) {
                content = resolveTemplate(ops.getSuccess());
                status = OptLogStatus.SUCCESS;
            } else {
                content = resolveTemplate(ops.getFail());
                status = OptLogStatus.FAIL;
            }
            operateTime = executeResult.getOperateTime();
            executeTime = executeResult.getExecuteTime();
            errorMsg = executeResult.getErrorMsg();
        }

        Object operator = resolveTemplate(ops.getOperator());
        if (operator == null) {
            operator = operatorService.getOperator();
        }

        OptLogRecord logRecord = new OptLogRecord();
        logRecord.setOperator(toString(operator));
        logRecord.setBizId(toString(bizId));
        logRecord.setTenant(toString(tenant));
        logRecord.setCategory(toString(category));
        logRecord.setOperate(toString(operate));
        logRecord.setExtra(toString(extra));
        logRecord.setContent(toString(content));
        logRecord.setStatus(status);
        logRecord.setOperateTime(operateTime);
        logRecord.setExecuteTime(executeTime);
        logRecord.setResult(result);
        logRecord.setErrorMsg(errorMsg);
        return logRecord;
    }

    private String toString(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    private Object resolveTemplate(String template) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        String parsed = parseTemplate(template);
        StandardEvaluationContext context = OptLogContext.getContext();
        ParserContext parserContext = getParserContext(template);
        Expression expression = expressionParser.parseExpression(parsed, parserContext);
        return expression.getValue(context);
    }

    private <T> T resolveTemplate(String template, Class<T> clazz) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        String parsed = parseTemplate(template);
        StandardEvaluationContext context = OptLogContext.getContext();
        ParserContext parserContext = getParserContext(template);
        Expression expression = expressionParser.parseExpression(parsed, parserContext);
        return expression.getValue(context, clazz);
    }

    private ParserContext getParserContext(String template) {
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        if (matcher.find()) {
            return templateParserContext;
        } else {
            return null;
        }
    }

    private String parseTemplate(String template) {
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        if (!matcher.find()) {
            return matchFunction(template);
        }
        StringBuffer sb = new StringBuffer();
        do {
            String match = matcher.group(1);
            String func = matchFunction(match);
            matcher.appendReplacement(sb, "#{" + func + "}");
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String matchFunction(String template) {
        Matcher matcher = FUNCTION_PATTERN.matcher(template);
        if (!matcher.find()) {
            return template;
        }
        StringBuffer sb = new StringBuffer();
        do {
            String functionName = matcher.group(1);
            OptLogFunction function = functionCache.getFunction(functionName);
            if (function == null) {
                continue;
            }
            if (function.isStatic()) {
                StandardEvaluationContext context = OptLogContext.getContext();
                context.registerFunction(functionName, function.getMethod());
            } else {
                String replace = function.getBeanMethod();
                matcher.appendReplacement(sb, replace);
            }
        } while (matcher.find());
        matcher.appendTail(sb);
        return sb.toString();
    }

    public void setFunctionCache(OptLogFunctionCache functionCache) {
        this.functionCache = functionCache;
    }

    public void setProperties(OptLogProperties properties) {
        this.properties = properties;
    }

    public void setOperatorService(IOperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
