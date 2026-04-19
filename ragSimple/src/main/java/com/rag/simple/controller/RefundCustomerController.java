package com.rag.simple.controller;

import com.rag.simple.service.RefundCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author Sleck
 */
@Slf4j
@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundCustomerController {

    private final RefundCustomerService refundCustomerService;

    /*
    * 退款接口
    *
    * */
    @RequestMapping("/chat")
    public String chat(String chatId, String userMessage) {
        return refundCustomerService.chat(chatId, userMessage);
    }


    /*
     * 退款接口 流式调用
     *
     * */
    @GetMapping(value = "/streamChat")
    public Flux<String> streamChat(String chatId, String userMessage, jakarta.servlet.http.HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        return refundCustomerService.streamChat(chatId, userMessage);
    }
}
