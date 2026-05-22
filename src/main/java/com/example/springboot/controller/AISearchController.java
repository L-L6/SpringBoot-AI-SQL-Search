package com.example.springboot.controller;

import com.example.springboot.service.AISearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.GenerationOutput;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import java.util.*;
import java.sql.*;
import java.util.Date;

@RestController
@RequestMapping("/api/ai")
public class AISearchController {


    @Autowired
    private AISearchService aiSearchService;

    @Autowired
    private JdbcTemplate jdbcTemplate;  // ✅ 添加JdbcTemplate注入


    /**
     * AI搜索接口
     * POST /api/ai/search
     */
    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody Map<String, String> request) {
        String query = request.get("query");
        String tableName = request.getOrDefault("tableName", "user"); // 默认查询user表

        if (query == null || query.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "查询内容不能为空");
            return error;
        }

        return aiSearchService.search(query, tableName);
    }

    /**
     * 获取所有表名
     * GET /api/ai/tables
     */
    @GetMapping("/tables")
    public Map<String, Object> getTables() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("tables", aiSearchService.getAllTables());
        return result;
    }

    /**
     * 测试AI连接
     * GET /api/ai/test
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> result = new HashMap<>();

        try {
            Map<String, Object> testResult = aiSearchService.search("测试连接", "user");
            result.put("success", true);
            result.put("message", "AI服务连接正常");
            result.put("testResult", testResult);
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "AI服务连接失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 分析SQL语句
     */
    private Map<String, Object> analyzeSQL(String sql) {
        Map<String, Object> analysis = new HashMap<>();

        try {
            String upperSql = sql.toUpperCase();

            analysis.put("sql_length", sql.length());
            analysis.put("contains_select", upperSql.contains("SELECT"));
            analysis.put("contains_from", upperSql.contains("FROM"));
            analysis.put("contains_where", upperSql.contains("WHERE"));
            analysis.put("contains_limit", upperSql.contains("LIMIT"));

            // 检查WHERE子句
            int whereIndex = upperSql.indexOf("WHERE");
            if (whereIndex != -1) {
                String afterWhere = sql.substring(whereIndex);
                analysis.put("where_clause_in_sql", afterWhere);

                // 检查WHERE条件是否为空
                int whereEnd = afterWhere.toUpperCase().indexOf("LIMIT");
                if (whereEnd == -1) whereEnd = afterWhere.length();
                String whereCondition = afterWhere.substring(5, whereEnd).trim();
                analysis.put("where_condition_in_sql", whereCondition);
                analysis.put("where_condition_empty", whereCondition.isEmpty());
            }

            // 检查表名
            int fromIndex = upperSql.indexOf("FROM");
            if (fromIndex != -1) {
                int whereOrLimit = upperSql.indexOf("WHERE");
                if (whereOrLimit == -1) whereOrLimit = upperSql.indexOf("LIMIT");
                if (whereOrLimit == -1) whereOrLimit = sql.length();

                String tablePart = sql.substring(fromIndex + 4, whereOrLimit).trim();
                analysis.put("table_part_in_sql", tablePart);
            }

        } catch (Exception e) {
            analysis.put("analysis_error", e.getMessage());
        }

        return analysis;
    }

    /**
     * 从SQL中提取表名
     */
    private String extractTableName(String sql) {
        try {
            String upperSql = sql.toUpperCase();
            int fromIndex = upperSql.indexOf("FROM");
            if (fromIndex == -1) return null;

            int whereIndex = upperSql.indexOf("WHERE", fromIndex);
            int limitIndex = upperSql.indexOf("LIMIT", fromIndex);
            int orderIndex = upperSql.indexOf("ORDER BY", fromIndex);
            int groupIndex = upperSql.indexOf("GROUP BY", fromIndex);

            int endIndex = sql.length();
            if (whereIndex != -1) endIndex = Math.min(endIndex, whereIndex);
            if (limitIndex != -1) endIndex = Math.min(endIndex, limitIndex);
            if (orderIndex != -1) endIndex = Math.min(endIndex, orderIndex);
            if (groupIndex != -1) endIndex = Math.min(endIndex, groupIndex);

            String tablePart = sql.substring(fromIndex + 4, endIndex).trim();

            // 移除可能的别名
            if (tablePart.contains(" ")) {
                tablePart = tablePart.split(" ")[0];
            }

            // 移除可能的括号
            if (tablePart.contains("(")) {
                tablePart = tablePart.split("\\(")[0];
            }

            return tablePart.trim();

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 尝试执行简化查询
     */
    private void trySimpleQueries(String tableName, Map<String, Object> result) {
        Map<String, Object> simpleTests = new HashMap<>();

        if (tableName != null) {
            // 测试1：查询表是否存在
            try {
                String checkTableSql = "SELECT COUNT(*) as count FROM " + tableName + " WHERE 1=0";
                jdbcTemplate.queryForMap(checkTableSql);
                simpleTests.put("table_check", "表存在");
            } catch (Exception e) {
                simpleTests.put("table_check", "错误: " + e.getMessage());
            }

            // 测试2：查询简单计数
            try {
                String countSql = "SELECT COUNT(*) as cnt FROM " + tableName;
                Map<String, Object> countResult = jdbcTemplate.queryForMap(countSql);
                simpleTests.put("simple_count", countResult);
            } catch (Exception e) {
                simpleTests.put("simple_count_error", e.getMessage());
            }

            // 测试3：查询简单数据
            try {
                String simpleSql = "SELECT * FROM " + tableName + " LIMIT 1";
                List<Map<String, Object>> data = jdbcTemplate.queryForList(simpleSql);
                simpleTests.put("simple_query", data.size() + " 行数据");
            } catch (Exception e) {
                simpleTests.put("simple_query_error", e.getMessage());
            }
        }

        result.put("simple_tests", simpleTests);
    }


    /**
     * 测试SQL执行
     */
    private Map<String, Object> testSQL(String sql, String methodName) {
        Map<String, Object> testResult = new HashMap<>();

        testResult.put("sql", sql);
        testResult.put("method", methodName);

        try {
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
            testResult.put("success", true);
            testResult.put("data_count", data.size());
            testResult.put("sample_data", data.size() > 0 ? data.subList(0, Math.min(3, data.size())) : new ArrayList<>());
        } catch (Exception e) {
            testResult.put("success", false);
            testResult.put("error", e.getMessage());
        }

        return testResult;
    }


    /**
     * 获取SDK信息
     */
    private Map<String, String> getSDKInfo() {
        Map<String, String> info = new HashMap<>();

        try {
            // 获取SDK版本
            info.put("sdk_version", "2.9.2"); // 根据你的pom.xml

            // 获取API Key信息
            String apiKey = com.alibaba.dashscope.utils.Constants.apiKey;
            info.put("api_key_set", apiKey != null ? "是" : "否");
            if (apiKey != null) {
                info.put("api_key_length", String.valueOf(apiKey.length()));
            }

        } catch (Exception e) {
            info.put("error", e.getMessage());
        }

        return info;
    }
}
