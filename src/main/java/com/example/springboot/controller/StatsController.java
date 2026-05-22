package com.example.springboot.controller;

import com.example.springboot.aspect.DetailedPerformanceAspect;
import com.example.springboot.pojo.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsController {

    @Autowired
    private DetailedPerformanceAspect performanceAspect;

    /**
     * 查看接口性能统计
     * 访问：http://localhost:8088/stats/performance
     */
    @GetMapping("/performance")
    public ResponseMessage<Map<String, Map<String, Object>>> getPerformanceStats() {
        Map<String, Map<String, Object>> report = performanceAspect.getPerformanceReport();
        return ResponseMessage.success(report);
    }

    /**
     * 查看系统状态
     * 访问：http://localhost:8088/stats/health
     */
    @GetMapping("/health")
    public ResponseMessage<Map<String, Object>> getHealth() {
        Map<String, Object> healthInfo = Map.of(
                "status", "UP",
                "timestamp", System.currentTimeMillis(),
                "service", "用户管理系统",
                "version", "1.0.0"
        );
        return ResponseMessage.success(healthInfo);
    }
}