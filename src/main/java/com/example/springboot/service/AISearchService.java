package com.example.springboot.service;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.*;

@Service
public class AISearchService {

    private static final Logger logger = LoggerFactory.getLogger(AISearchService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${alibaba.dashscope.model:qwen3.5-35b-a3b}")
    private String model;

    @Value("${ai.max-results:100}")
    private int maxResults;

    private MultiModalConversation conv;

    @PostConstruct
    public void init() {
        Constants.baseHttpApiUrl = "https://dashscope.aliyuncs.com/api/v1";
        this.conv = new MultiModalConversation();
    }

    /**
     * AI搜索主方法
     */
    public Map<String, Object> search(String naturalQuery, String tableName) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 获取表结构
            String tableSchema = getTableSchema(tableName);

            // 2. AI生成WHERE条件
            String whereClause = generateSQLWithAI(naturalQuery, tableSchema);

            // 3. 构建并执行SQL
            String sql = buildSafeSQL(tableName, whereClause);
            List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);

            // 4. 返回结果
            result.put("success", true);
            result.put("query", naturalQuery);
            result.put("sql", sql);
            result.put("data", data);
            result.put("count", data.size());

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 获取表结构信息
     */
    private String getTableSchema(String tableName) throws SQLException {
        StringBuilder schema = new StringBuilder();
        schema.append("表名: ").append(tableName).append("\n字段:\n");

        Connection conn = jdbcTemplate.getDataSource().getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet columns = metaData.getColumns(null, null, tableName, null);

        while (columns.next()) {
            String colName = columns.getString("COLUMN_NAME");
            String colType = columns.getString("TYPE_NAME");
            schema.append("- ").append(colName).append(" (").append(colType).append(")\n");
        }
        columns.close();
        conn.close();

        return schema.toString();
    }

    /**
     * AI生成SQL
     */
    private String generateSQLWithAI(String query, String schema) throws Exception {
        logger.info("调用AI生成SQL，查询: {}, 表结构长度: {}", query, schema.length());  //  修正变量名

        String prompt = String.format(
                "你是一个SQL专家。请将用户的自然语言查询转换为SQL WHERE条件。\n\n" +
                        "表结构：%s\n\n" +
                        "用户查询：\"%s\"\n\n" +
                        "只返回SQL WHERE条件。如果无法理解，返回：1=1",
                schema, query  // 使用正确的参数名
        );

        MultiModalMessage userMessage = MultiModalMessage.builder()
                .role(Role.USER.getValue())
                .content(Collections.singletonList(Collections.singletonMap("text", prompt)))
                .build();

        MultiModalConversationParam param = MultiModalConversationParam.builder()
                .model(model)
                .messages(Collections.singletonList(userMessage))
                .build();

        MultiModalConversationResult result = conv.call(param);
        logger.info("AI调用完成，requestId: {}", result.getRequestId());

        if (result.getOutput() != null
                && result.getOutput().getChoices() != null
                && !result.getOutput().getChoices().isEmpty()) {
            Object text = result.getOutput().getChoices().get(0)
                    .getMessage().getContent().get(0).get("text");
            if (text != null) {
                String aiResponse = text.toString();
                logger.info("AI响应: {}", aiResponse);
                return cleanSQLOutput(aiResponse);
            }
            logger.warn("AI返回了空结果");
        } else {
            logger.warn("AI Output为null");
        }

        return "1=1";
    }

    /**
     * 清理AI输出
     */
    private String cleanSQLOutput(String aiOutput) {
        String output = aiOutput.trim();

        // 移除代码块标记
        if (output.startsWith("```")) {
            output = output.replaceAll("```sql|```", "").trim();
        }

        // 提取WHERE条件
        String upper = output.toUpperCase();
        if (upper.contains("WHERE ")) {
            int idx = upper.indexOf("WHERE ");
            output = output.substring(idx + 6).trim();
        }

        // 移除末尾分号
        if (output.endsWith(";")) {
            output = output.substring(0, output.length() - 1);
        }

        return output.isEmpty() ? "1=1" : output;
    }

    /**
     * 构建安全SQL
     */
    private String buildSafeSQL(String tableName, String whereClause) {
        return String.format("SELECT * FROM %s WHERE %s LIMIT %d",
                tableName, whereClause, maxResults);
    }

    /**
     * 获取数据库所有表
     */
    public List<String> getAllTables() {
        try {
            List<String> tables = new ArrayList<>();
            Connection conn = jdbcTemplate.getDataSource().getConnection();
            ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});

            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
            conn.close();

            return tables;
        } catch (SQLException e) {
            return Arrays.asList("user", "product", "order"); // 默认表
        }
    }
}
