package com.tlias.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlias.common.BaseContext;
import com.tlias.entity.LogItem;
import com.tlias.mapper.LogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * 操作日志切面：记录被 {@link com.tlias.annotation.Log} 标注方法的操作人、参数、返回值与耗时
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private static final int MAX_TEXT_LENGTH = 2000;

    private final LogMapper logMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(com.tlias.annotation.Log)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long cost = System.currentTimeMillis() - start;
            try {
                saveLog(joinPoint, result, cost);
            } catch (Exception e) {
                log.warn("记录操作日志失败: {}", e.getMessage());
            }
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, Object result, long cost) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        LogItem item = new LogItem();
        item.setOperateEmpId(BaseContext.getCurrentId());
        item.setOperateTime(LocalDateTime.now());
        item.setClassName(joinPoint.getTarget().getClass().getName());
        item.setMethodName(signature.getName());
        item.setMethodParams(toJson(joinPoint.getArgs()));
        item.setReturnValue(result == null ? null : toJson(result));
        item.setCostTime(cost);
        logMapper.insert(item);
    }

    private String toJson(Object value) {
        try {
            Object target = value;
            if (value instanceof Object[] args) {
                target = Arrays.stream(args)
                        .filter(arg -> !(arg instanceof MultipartFile))
                        .toArray();
            }
            String json = objectMapper.writeValueAsString(target);
            return json.length() > MAX_TEXT_LENGTH ? json.substring(0, MAX_TEXT_LENGTH) : json;
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }
}
