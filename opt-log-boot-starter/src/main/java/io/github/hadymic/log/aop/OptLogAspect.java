package io.github.hadymic.log.aop;

import io.github.hadymic.log.annotation.OptLog;
import io.github.hadymic.log.context.OptLogContext;
import io.github.hadymic.log.model.MethodExecuteResult;
import io.github.hadymic.log.model.OptLogOps;
import io.github.hadymic.log.model.OptLogRecord;
import io.github.hadymic.log.parse.OptLogFunctionParser;
import io.github.hadymic.log.service.IOptLogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 操作日志切面
 *
 * @author Hadymic
 */
@Slf4j
@Aspect
public class OptLogAspect {

    private OptLogFunctionParser functionParser;

    private IOptLogService optLogService;

    @Pointcut("@annotation(io.github.hadymic.log.annotation.OptLog)")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法上的所有注解
        List<OptLogOps> opsList;
        try {
            opsList = getOps(joinPoint);
        } catch (Exception e) {
            log.error("opt log method parse error", e);
            opsList = new ArrayList<>();
        }

        // 分离执行前记录和执行后记录
        List<OptLogOps> beforeOps = new ArrayList<>();
        List<OptLogOps> afterOps = new ArrayList<>();
        for (OptLogOps ops : opsList) {
            if (ops.isBefore()) {
                beforeOps.add(ops);
            } else {
                afterOps.add(ops);
            }
        }

        // 执行前记录
        try {
            List<OptLogRecord> beforeRecords = functionParser.resolveBefore(beforeOps);
            for (OptLogRecord record : beforeRecords) {
                optLogService.record(record);
            }
        } catch (Exception e) {
            log.error("opt log before function resolve error", e);
        }

        // 执行方法
        MethodExecuteResult executeResult = new MethodExecuteResult();
        Object result = null;
        try {
            result = joinPoint.proceed();
            executeResult.success();
        } catch (Throwable e) {
            executeResult.fail(e);
        }

        // 执行后记录
        try {
            List<OptLogRecord> afterRecords = functionParser.resolveAfter(afterOps, result, executeResult);
            for (OptLogRecord record : afterRecords) {
                optLogService.record(record);
            }
        } catch (Exception e) {
            log.error("opt log after function resolve error", e);
        } finally {
            OptLogContext.clear();
        }

        if (executeResult.getThrowable() != null) {
            throw executeResult.getThrowable();
        }
        return result;
    }

    private List<OptLogOps> getOps(JoinPoint joinPoint) {
        Method method = getMethod(joinPoint);
        Object[] args = joinPoint.getArgs();
        functionParser.initContext(method, args);
        return parseAnnotations(method);
    }

    private Method getMethod(JoinPoint joinPoint) {
        Class<?> targetClass = AopUtils.getTargetClass(joinPoint.getTarget());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return AopUtils.getMostSpecificMethod(signature.getMethod(), targetClass);
    }

    private List<OptLogOps> parseAnnotations(AnnotatedElement element) {
        Set<OptLog> annotations = AnnotatedElementUtils.findAllMergedAnnotations(element, OptLog.class);
        List<OptLogOps> ops = new ArrayList<>();
        for (OptLog optLog : annotations) {
            ops.add(parseAnnotation(optLog));
        }
        return ops;
    }

    private OptLogOps parseAnnotation(OptLog optLog) {
        OptLogOps ops = new OptLogOps();
        ops.setSuccess(optLog.success());
        ops.setFail(optLog.fail());
        ops.setOperator(optLog.operator());
        ops.setBizId(optLog.bizId());
        ops.setTenant(optLog.tenant());
        ops.setCategory(optLog.category());
        ops.setOperate(optLog.operate());
        ops.setExtra(optLog.extra());
        ops.setCondition(optLog.condition());
        ops.setBefore(optLog.before());
        return ops;
    }

    public void setFunctionParser(OptLogFunctionParser functionParser) {
        this.functionParser = functionParser;
    }

    public void setOptLogService(IOptLogService optLogService) {
        this.optLogService = optLogService;
    }
}
