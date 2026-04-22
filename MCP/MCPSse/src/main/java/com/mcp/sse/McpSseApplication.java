package com.mcp.sse;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * @author Sleck
 */
@SpringBootApplication
public class McpSseApplication {
    public static void main(String[] args) {
        SpringApplication.run(McpSseApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider weatherTools () {
        // 自动扫描 WeatherService 中带有 @Tool 注解的方法
        return MethodToolCallbackProvider.builder().toolObjects(new WeatherService()).build();
    }
}
