package com.hua.ai.controller;

import com.hua.ai.web.ChatModeRouter;
import com.hua.ai.web.ChatRequest;
import com.hua.ai.web.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ChatModeRouter router;

    /**
     * 统一非流式入口（普通/RAG/报告）
     */
    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest req) {
        return router.chat(req);
    }

    /**
     * 统一流式入口（SSE）
     */
    @GetMapping("/stream")
    public Flux<ServerSentEvent<String>> stream(String mode, int sessionId, String message) {
        return router.stream(sessionId, message)
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }

    /**
     * 兼容旧接口（/chat/dialogue）
     */
    @GetMapping("/dialogue")
    public Flux<ServerSentEvent<String>> dialogue(int memoryId, String message) {
        return stream("stream", memoryId, message);
    }

}
