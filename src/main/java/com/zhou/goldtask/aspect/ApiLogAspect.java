package com.zhou.goldtask.aspect;

import cn.dev33.satoken.util.SaResult;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
@Slf4j
public class ApiLogAspect {

    @Pointcut("execution(* com.zhou.goldtask.controller..*.*(..)) && !execution(* com.zhou.goldtask.controller.WebSocketServer.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        long startTime = System.currentTimeMillis();
        String uuid = UUID.randomUUID().toString();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();
        String requestUrl = request.getRequestURL().toString();
        String requestMethod = request.getMethod();

        Map<String, Object> params = getParams(joinPoint, signature);
        String paramsJson = truncateLongString(JSONUtil.toJsonStr(params), 100);

        log.info("开始请求:[{}]\n请求URL:{}\n请求方法:{}#{}\nHTTP方法:{}\n请求参数:{}", uuid, requestUrl, className, methodName, requestMethod, paramsJson);

        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            log.error("请求异常:[{}]-{}", uuid, throwable.getMessage(), throwable);
            result = SaResult.error(throwable.getMessage());
            return result;
        } finally {
            String resultJson = truncateLongString(result != null ? JSONUtil.toJsonStr(result) : "null", 101);
            log.info("响应:[{}]\n结果:{}\n请求耗时:{} ms", uuid, resultJson, System.currentTimeMillis() - startTime);
        }
    }

    private Map<String, Object> getParams(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        Map<String, Object> params = new HashMap<>();
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        if (parameterNames != null && args != null) {
            for (int i = 0; i < parameterNames.length; i++) {
                String name = parameterNames[i];
                Object value = args[i];
                if (!(value instanceof HttpServletRequest || value instanceof javax.servlet.http.HttpServletResponse)) {
                    params.put(name, value);
                }
            }
        }
        return params;
    }

    private String truncateLongString(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}