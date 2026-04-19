package com.rag.simple.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class RefundCustomerService {

    private final ChatClient chatClient;
    public RefundCustomerService(ChatModel chatModel, ChatMemory chatMemory) {
        
        // 步骤：设置提示词，扮演客服角色，增加记忆，注入工具
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一名具有多年电商经验的客服员工。在服务过程中，请认真分析当前货物是否满足退款条件（例如：完全不能使用、食物变质等）。如果用户给出的信息不足以判断，你应该委婉地安抚客户情绪，并请求他们提供更多具体信息，以便核实。当你确定符合退款条件后，直接调用退款工具为用户办理退款。")
                // 使用 MessageChatMemoryAdvisor 增加多轮对话记忆，添加拦截日志
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), new SimpleLoggerAdvisor())
                // 直接注册带有 @Tool 注解的对象作为工具
                .defaultTools(this)
                .build();
    }

    /**
     * 和客服对话的方法
     */
    public String chat(String chatId, String userMessage) {
        return this.chatClient.prompt()
                .user(userMessage)
                // 绑定 conversationId 以区分不同用户的多轮对话记忆
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();
    }


    /**
     * 和客服对话的方法流式调用
     */
    public Flux<String> streamChat(String chatId, String userMessage) {
        return this.chatClient.prompt()
                .user(userMessage)
                // 绑定 conversationId 以区分不同用户的多轮对话记忆
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    /**
     * 编写退款方法，使用 @Tool 注解
     * 大语言模型在判断符合退款条件后会自动提取工具并调用
     */
    @Tool(description = "根据订单号和退款原因为用户办理退款操作")
    public String processRefund(String orderId, String reason) {
        // 模拟退款逻辑
        System.out.println(">>> [系统日志] 正在为订单 " + orderId + " 执行退款，原因：" + reason);
        return "退款成功！(订单号: " + orderId + ")";
    }
}
