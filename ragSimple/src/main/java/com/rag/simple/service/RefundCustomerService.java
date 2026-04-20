package com.rag.simple.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RefundCustomerService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RefundCustomerService(ChatModel chatModel, ChatMemory chatMemory, List<ToolCallback> tools) {
        this.chatMemory = chatMemory;

        ToolCallingChatOptions toolCallingChatOptions = ToolCallingChatOptions.builder()
                .toolCallbacks(tools)
                .internalToolExecutionEnabled(false)
                .build();
        // 步骤：设置提示词，扮演客服角色，增加记忆，注入工具
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("你是一名具有多年电商经验的客服员工。在服务过程中，请认真分析当前货物是否满足退款条件（例如：完全不能使用、食物变质等）。如果用户给出的信息不足以判断，你应该委婉地安抚客户情绪，并请求他们提供更多具体信息，以便核实。当你确定符合退款条件后，直接调用退款工具为用户办理退款。")
                // 使用 MessageChatMemoryAdvisor 增加多轮对话记忆，添加拦截日志
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), new SimpleLoggerAdvisor())
                // 直接注册带有 @Tool 注解的对象作为工具
                .defaultTools(this)
                // 禁用内部自动工具执行，改为手动拦截处理
                .defaultOptions(toolCallingChatOptions)
                .build();
    }

    /**
     * 和客服对话的方法
     */
    public String chat(String chatId, String userMessage) {
        ChatClient.ChatClientRequestSpec promptSpec = this.chatClient.prompt();
        if (userMessage != null && !userMessage.isEmpty()) {
            promptSpec.user(userMessage);
        }
        ChatResponse response = promptSpec
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();

        // 循环处理工具调用，直到 AI 给出最终文本回复
        while (response != null && response.hasToolCalls()) {
            AssistantMessage assistantMessage = response.getResult().getOutput();
            List<Message> toolResponses = executeToolCalls(assistantMessage);
            
            // 将工具执行结果存入记忆（注意：MessageChatMemoryAdvisor 会自动处理后续读取，但我们需要确保当前上下文包含这些结果）
            this.chatMemory.add(chatId, assistantMessage);
            this.chatMemory.add(chatId, toolResponses);

            // 再次调用 AI 处理工具结果
            response = this.chatClient.prompt()
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                    .call()
                    .chatResponse();
        }

        return response != null ? response.getResult().getOutput().getText() : "服务繁忙，请稍后再试。";
    }


    /**
     * 和客服对话的方法流式调用
     */
    public Flux<String> streamChat(String chatId, String userMessage) {
        return handleStream(chatId, userMessage);
    }

    private Flux<String> handleStream(String chatId, String userMessage) {
        ChatClient.ChatClientRequestSpec promptSpec = this.chatClient.prompt();
        if (userMessage != null && !userMessage.isEmpty()) {
            promptSpec.user(userMessage);
        }
        return promptSpec.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .chatResponse()
                .collectList()
                .flatMapMany(responses -> {
                    // 合并流式响应中的所有片段（主要是为了提取完整的工具调用）
                    ChatResponse integratedResponse = responses.get(responses.size() - 1);
                    
                    if (integratedResponse.hasToolCalls()) {
                        AssistantMessage assistantMessage = integratedResponse.getResult().getOutput();
                        List<Message> toolResponses = executeToolCalls(assistantMessage);

                        // 手动维护记忆：添加回复（含工具调用请求）和工具执行结果
                        this.chatMemory.add(chatId, assistantMessage);
                        this.chatMemory.add(chatId, toolResponses);

                        // 递归调用：由于已经更新了记忆，下次调用不带 userMessage 即可
                        return handleStream(chatId, null);
                    } else {
                        // 如果没有工具调用，则输出文本内容
                        return Flux.fromIterable(responses)
                                .map(r -> r.getResult().getOutput().getText() != null ? r.getResult().getOutput().getText() : "");
                    }
                });
    }

    /**
     * 手动执行工具调用并返回结果消息列表
     */
    private List<Message> executeToolCalls(AssistantMessage assistantMessage) {
        return assistantMessage.getToolCalls().stream().map(toolCall -> {
            String result = "未知工具调用";
            if ("processRefund".equals(toolCall.name())) {
                try {
                    Map<String, Object> args = objectMapper.readValue(toolCall.arguments(), Map.class);
                    result = processRefund((String) args.get("orderId"), (String) args.get("reason"));
                } catch (JsonProcessingException e) {
                    result = "解析工具参数失败: " + e.getMessage();
                }
            }
            return ToolResponseMessage.builder()
                    .responses(List.of(new ToolResponseMessage.ToolResponse(toolCall.id(), toolCall.name(), result)))
                    .build();
        }).collect(Collectors.toList());
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
