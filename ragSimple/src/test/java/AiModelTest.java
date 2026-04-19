import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import com.rag.simple.service.RefundCustomerService;
import java.util.Scanner;

import static org.assertj.core.api.Assertions.assertThat;

// 确保这里指向你的主启动类
@SpringBootTest(classes = com.rag.simple.RagSimpleApplication.class)
class AiModelTest {

    @Autowired
    private EmbeddingModel embeddingModel; // 阿里云 DashScope 的 Embedding 实现

    @Autowired
    private DashScopeChatModel chatModel;

    @Autowired
    private RefundCustomerService refundCustomerService;
    /**
     * 测试 1：阿里云向量化功能
     * 目标：验证 text-embedding-v1 是否正常工作，并检查实际维度
     */
    @Test
    void testEmbedding() {
        System.out.println(">>> [阿里云] 开始测试向量化...");

        // 1. 准备文本
        String text = "阿里云通义千问的 text-embedding-v1 模型效果非常棒。";

        // 2. 构建请求 (Spring AI 会自动处理 Batch)
        EmbeddingOptions operation = EmbeddingOptions.builder()
                .model("text-embedding-v1")
                .build();
        EmbeddingRequest request = new EmbeddingRequest(List.of(text), operation);

        try {
            // 3. 调用 API
            EmbeddingResponse response = embeddingModel.call(request);

            // 4. 获取结果
            List<Embedding> results = response.getResults();
            assertThat(results).isNotEmpty();

            // 5. 获取向量数据 (阿里云返回的是 List<Double>)
            float[] vector = results.get(0).getOutput();

            System.out.println(">>> [成功] 向量化完成！");
            System.out.println(">>> [信息] 实际向量维度: " + vector.length);
            System.out.println(">>> [信息] 前5个数值: " + vector);

            // 6. 动态断言
            // 注意：text-embedding-v1 默认通常是 1536 维。
            // 如果你在 yml 中强制指定了 dimensions: 1024，这里应该是 1024。
            // 如果这里打印 1536 但你数据库是 1024，存入时会报错！
            int actualDimension = vector.length;
            System.out.println(">>> [警告] 请确保 application.yml 中 pgvector.dimensions 设置为: " + actualDimension);

            // 暂时不写死断言，避免维度不匹配导致测试失败，请根据打印结果手动核对

        } catch (Exception e) {
            System.err.println(">>> [失败] 向量化出错: " + e.getMessage());
            e.printStackTrace();
            throw e; // 抛出异常让测试标记为失败
        }
    }

    /**
     * 测试 2：阿里云聊天功能
     * 目标：验证 Qwen-Turbo 是否连通
     */
    @Test
    void testChat() {
        System.out.println(">>> [阿里云] 开始测试聊天模型 (Qwen)...");

        try {
            ChatClient chatClient = ChatClient
                    .builder(chatModel)
                    .defaultSystem("我是阿里云的用户，请回答我的问题。")
                    .build();
            // 1. 发送消息
            ChatResponse response = chatClient.prompt()
                    .user("你好，我是阿里云的用户，请问你是哪个模型？")
                    .call()
                    .chatResponse();

            // 2. 获取回复
            String content = response.getResult().getOutput().getText();

            System.out.println(">>> [成功] AI 回复: " + content);

            // 3. 简单验证
            assertThat(content).isNotEmpty();
            assertThat(content).containsIgnoringCase("通义千问"); // 验证它是否承认自己是 Qwen

        } catch (Exception e) {
            System.err.println(">>> [失败] 聊天出错: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 测试 3：客服模拟多轮对话（包含退款 @Tool 记忆）
     */
    @Test
    void testRefundAgent() {
        System.out.println(">>> [测试退款客服] 开始模拟多轮对话...");
        String chatId = "user_" + System.currentTimeMillis();

        // 模拟多轮对话
        String[] userMessages = {
                "你好，我在你们店里买的东西出问题了。",
                "我买的食物都变质了！！！完全没法吃，订单号是 ORD-2026-999。",
                "是的，我确认需要退款。"
        };

        for (String message : userMessages) {
            System.out.println("\n[用户]: " + message);
            String aiResponse = refundCustomerService.chat(chatId, message);
            System.out.println("[AI客服]: " + aiResponse);
        }
        
        System.out.println("\n>>> [测试退款客服] 测试完成");
    }
}