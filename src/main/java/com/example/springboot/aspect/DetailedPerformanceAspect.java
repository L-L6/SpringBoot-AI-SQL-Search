package com.example.springboot.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 增强版性能统计切面
 * 功能：统计每个接口的调用次数、总耗时、平均耗时
 */
@Aspect
@Component
public class DetailedPerformanceAspect {

    // 存储统计信息：key=接口名, value=[调用次数, 总耗时]
    private final Map<String, long[]> stats = new ConcurrentHashMap<>();

    @Around("execution(* com.example.springboot.controller.*.*(..))")
    public Object collectStats(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取接口标识
        String methodSignature = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            // 更新统计信息
            updateStats(methodSignature, executionTime);

            return result;
        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            updateStats(methodSignature + "[异常]", executionTime);
            throw throwable;
        }
    }

    /**
     * 更新统计信息
     */
    private synchronized void updateStats(String methodName, long executionTime) {
        long[] stat = stats.getOrDefault(methodName, new long[3]);
        stat[0]++; // 调用次数
        stat[1] += executionTime; // 总耗时
        if (executionTime > stat[2]) { // 最大耗时
            stat[2] = executionTime;
        }
        stats.put(methodName, stat);
    }

    /**
     * 获取统计报告（可以在Controller中调用显示）
     */
    public Map<String, Map<String, Object>> getPerformanceReport() {
        Map<String, Map<String, Object>> report = new HashMap<>();

        for (Map.Entry<String, long[]> entry : stats.entrySet()) {
            String method = entry.getKey();
            long[] data = entry.getValue();
            long count = data[0];
            long totalTime = data[1];
            long maxTime = data[2];

            Map<String, Object> methodStats = new HashMap<>();
            methodStats.put("调用次数", count);
            methodStats.put("总耗时(ms)", totalTime);
            methodStats.put("平均耗时(ms)", count > 0 ? totalTime / count : 0);
            methodStats.put("最大耗时(ms)", maxTime);

            report.put(method, methodStats);
        }

        return report;
    }
}
