package io.github.hadymic.log.parse;

import io.github.hadymic.log.annotation.EnableOptLog;
import io.github.hadymic.log.configuration.OptLogProperties;
import io.github.hadymic.log.context.OptLogContext;
import io.github.hadymic.log.enums.OptLogSpEL;
import io.github.hadymic.log.enums.OptLogStatus;
import io.github.hadymic.log.model.MethodExecuteResult;
import io.github.hadymic.log.model.OptLogOps;
import io.github.hadymic.log.model.OptLogRecord;
import io.github.hadymic.log.model.OptLogSpELStatus;
import io.github.hadymic.log.service.IOperatorService;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Hadymic
 */
public class OptLogFunctionParser extends OptLogSpELSupport implements ApplicationContextAware {

    private OptLogProperties properties;

    private IOperatorService operatorService;

    private ApplicationContext applicationContext;

    private final OptLogExpressionEvaluator expressionEvaluator = new OptLogExpressionEvaluator();

    private OptLogSpELStatus spELStatus;

    @PostConstruct
    public void init() {
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(EnableOptLog.class);
        OptLogSpEL[] array = OptLogSpEL.ALL;
        if (!CollectionUtils.isEmpty(beanMap)) {
            for (Object value : beanMap.values()) {
                Class<?> targetClass = AopUtils.getTargetClass(value);
                EnableOptLog annotation = AnnotationUtils.findAnnotation(targetClass, EnableOptLog.class);
                if (annotation != null) {
                    array = annotation.enableSpEL();
                    break;
                }
            }
        }
        spELStatus = OptLogSpELStatus.of(array);
    }

    public void initContext(Method method, Object[] args) {
        StandardEvaluationContext context = expressionEvaluator
                .createEvaluationContext(method, args, applicationContext);
        OptLogContext.setContext(context);
    }

    public List<OptLogRecord> resolveBefore(List<OptLogOps> beforeRecordOps, List<OptLogOps> beforeParseOps) {
        List<OptLogRecord> records = beforeRecordOps.stream()
                .map(ops -> resolve(ops, null, null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (OptLogOps ops : beforeParseOps) {
            parseBefore(ops);
        }
        return records;
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

    private void parseBefore(OptLogOps ops) {
        OptLogSpELStatus status = ops.getStatus();
        if (spELStatus.isCondition() && status.isCondition()) {
            Boolean condition = resolveTemplate(ops.getCondition(), Boolean.class);
            if (condition == null || !condition) {
                ops.setCondition("false");
                return;
            } else {
                ops.setCondition("true");
            }
        }
        if (spELStatus.isSuccess() && status.isSuccess()) {
            ops.setSuccess(parseBefore(ops.getSuccess()));
        }
        if (spELStatus.isFail() && status.isFail()) {
            ops.setFail(parseBefore(ops.getFail()));
        }
        if (spELStatus.isOperator() && status.isOperator()) {
            ops.setOperator(parseBefore(ops.getOperator()));
        }
        if (spELStatus.isBizId() && status.isBizId()) {
            ops.setBizId(parseBefore(ops.getBizId()));
        }
        if (spELStatus.isTenant() && status.isTenant()) {
            ops.setTenant(parseBefore(ops.getTenant()));
        }
        if (spELStatus.isCategory() && status.isCategory()) {
            ops.setCategory(parseBefore(ops.getCategory()));
        }
        if (spELStatus.isOperate() && status.isOperate()) {
            ops.setOperate(parseBefore(ops.getOperate()));
        }
        if (spELStatus.isExtra() && status.isExtra()) {
            ops.setExtra(parseBefore(ops.getExtra()));
        }
    }

    private String parseBefore(String template) {
        Object resolve = resolveTemplate(template);
        if (resolve == null) {
            return null;
        } else {
            return "'" + toString(resolve) + "'";
        }
    }

    private OptLogRecord resolve(OptLogOps ops, Object result, MethodExecuteResult executeResult) {
        Boolean condition;
        if (spELStatus.isCondition()) {
            condition = resolveTemplate(ops.getCondition(), Boolean.class);
        } else {
            condition = "true".equals(ops.getCondition());
        }
        if (condition == null || !condition) {
            return null;
        }

        Object bizId = ops.getBizId();
        if (spELStatus.isBizId()) {
            bizId = resolveTemplate(ops.getBizId());
        }
        Object tenant = ops.getTenant();
        if (spELStatus.isTenant()) {
            tenant = resolveTemplate(ops.getTenant());
        }
        Object category = ops.getCategory();
        if (spELStatus.isCategory()) {
            category = resolveTemplate(ops.getCategory());
        }
        Object operate = ops.getOperate();
        if (spELStatus.isOperate()) {
            operate = resolveTemplate(ops.getOperate());
        }
        Object extra = ops.getExtra();
        if (spELStatus.isExtra()) {
            extra = resolveTemplate(ops.getExtra());
        }

        Object content;
        OptLogStatus status = OptLogStatus.BEFORE;
        Long operateTime = null;
        Long executeTime = null;
        String errorMsg = null;
        if (executeResult == null) {
            if (spELStatus.isSuccess()) {
                content = resolveTemplate(ops.getSuccess());
            } else {
                content = ops.getSuccess();
            }
        } else {
            if (executeResult.isSuccess()) {
                if (spELStatus.isSuccess()) {
                    content = resolveTemplate(ops.getSuccess());
                } else {
                    content = ops.getSuccess();
                }
                status = OptLogStatus.SUCCESS;
            } else {
                if (spELStatus.isFail()) {
                    content = resolveTemplate(ops.getFail());
                } else {
                    content = ops.getSuccess();
                }
                status = OptLogStatus.FAIL;
            }
            operateTime = executeResult.getOperateTime();
            executeTime = executeResult.getExecuteTime();
            errorMsg = executeResult.getErrorMsg();
        }

        Object operator;
        if (spELStatus.isOperator()) {
            operator = resolveTemplate(ops.getOperator());
        } else {
            operator = ops.getOperator();
        }
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

    public void setProperties(OptLogProperties properties) {
        this.properties = properties;
    }

    public void setOperatorService(IOperatorService operatorService) {
        this.operatorService = operatorService;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
