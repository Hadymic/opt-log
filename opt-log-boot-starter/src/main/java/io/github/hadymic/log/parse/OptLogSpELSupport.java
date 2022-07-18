package io.github.hadymic.log.parse;

import io.github.hadymic.log.cache.OptLogFunctionCache;
import io.github.hadymic.log.context.OptLogContext;
import io.github.hadymic.log.model.OptLogFunction;
import org.springframework.expression.Expression;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Hadymic
 */
public abstract class OptLogSpELSupport {

    private static final Pattern FUNCTION_PATTERN = Pattern.compile("@(.*?)\\(");
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("#\\{(.*?)}");

    private OptLogFunctionCache functionCache;

    private final SpelExpressionParser expressionParser = new SpelExpressionParser();
    private final TemplateParserContext templateParserContext = new TemplateParserContext();

    protected String toString(@Nullable Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    public Object resolveTemplate(String template) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        String parsed = parseTemplate(template);
        StandardEvaluationContext context = OptLogContext.getContext();
        ParserContext parserContext = getParserContext(template);
        Expression expression = expressionParser.parseExpression(parsed, parserContext);
        return expression.getValue(context);
    }

    public <T> T resolveTemplate(String template, Class<T> clazz) {
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
                if (!functionName.contains(".")) {
                    matcher.appendReplacement(sb, "#" + functionName + "(");
                }
                continue;
            }
            if (function.isStatic()) {
                StandardEvaluationContext context = OptLogContext.getContext();
                context.registerFunction(functionName, function.getMethod());
                matcher.appendReplacement(sb, "#" + functionName + "(");
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
}
