package com.rag.simple.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    /**
     * 将 ChatMemory 注册为 Spring 全局 Bean，
     * 这样你在项目里的任何地方都可以直接 @Autowired 注入它了！
     */
    @Bean
    public ChatMemory jdbcChatMemory() {
        return MessageWindowChatMemory.builder().maxMessages(10).build();
    }
}
