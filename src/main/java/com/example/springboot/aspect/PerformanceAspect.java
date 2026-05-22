package com.example.springboot.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * 接口性能监控切面
 * 功能：统计每个Controller方法的执行时间
 */

@Aspect
@Component
public class PerformanceAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceAspect.class);

    /**
     * 切入点：所有Controller类的方法
     * 解释：execution(* com.example.springboot.controller.*.*(..))
     * 第一个 *：任何返回类型
     * com.example.springboot.controller：controller包
     * 第二个 *：controller包下的任何类
     * 第三个 *：类中的任何方法
     * (..)：任何参数
     */
    @Pointcut("execution(* com.example.springboot.controller.*.*(..))")
    public void controllerMethods(){}

    /**
     * 环绕通知：统计方法执行时间
     * @param joinPoint 连接点
     * @return 方法执行结果
     * @throws Throwable 可能抛出的异常
     */
    @Around("controllerMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable{
        long startTime = System.currentTimeMillis();

        HttpServletRequest request = null;
        try{
            request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        }catch (Exception e){

        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        String requestInfo = "";
        if (request != null) {
            requestInfo = String.format(" | 请求: %s %s",
                    request.getMethod(), request.getRequestURI());
        }

        logger.info(" 开始执行: {}.{}{}", className, methodName, requestInfo);

        if (logger.isDebugEnabled() && args.length > 0) {
            logger.debug(" 方法参数: {}", Arrays.toString(args));
        }

        try {
            // 执行实际的方法
            Object result = joinPoint.proceed();

            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;

            // 根据执行时间使用不同级别的日志
            String performanceLevel = getPerformanceLevel(executionTime);

            logger.info("✅ 执行完成: {}.{} | 耗时: {}ms {}",
                    className, methodName, executionTime, performanceLevel);

            return result;

        } catch (Throwable throwable) {
            // 异常时的处理
            long executionTime = System.currentTimeMillis() - startTime;

            logger.error("❌ 执行异常: {}.{} | 耗时: {}ms | 异常: {} - {}",
                    className, methodName, executionTime,
                    throwable.getClass().getSimpleName(), throwable.getMessage());

            throw throwable;
        }
    }

    /**
     * 根据执行时间评估性能等级
     */
    private String getPerformanceLevel(long executionTime) {
        if (executionTime < 50) {
            return "[ 极快]";
        } else if (executionTime < 200) {
            return "[ 良好]";
        } else if (executionTime < 1000) {
            return "[ 注意]";
        } else if (executionTime < 5000) {
            return "[ 较慢]";
        } else {
            return "[ 超慢]";

        }

    }

}
