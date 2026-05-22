package com.example.springboot.config;

import com.alibaba.dashscope.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class AlibabaAIConfig {

    @Value("${alibaba.dashscope.api-key}")
    private String apiKey;

    @PostConstruct
    public void init(){
        Constants.apiKey = apiKey;
        System.out.println("阿里云AI配置已加载，API Key：" + maskApiKey(apiKey));
    }

    private String maskApiKey(String key) {
        if(key == null || key.length() <= 8) return "***";
        return key.substring(0,4) + "***" +key.substring(key.length()-4);
    }
}
