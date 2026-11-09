package com.hua.ai;

import com.hua.ai.service.DialogueService;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.service.AiServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 对话服务工厂类
 *
 * 这个类的主要作用：作为一个"工厂"，专门生产 DialogueService 对话服务实例
 * 使用 Spring 框架的配置功能，让 DialogueService 能够方便地在整个应用中使用
 */
@Configuration  // 告诉Spring：这是一个配置类，里面定义了各种"零件"的制造方法
public class DialogueServiceFactory {

    /**
     * AI聊天模型
     * 这个chatModel就是实际执行AI对话的核心组件，比如是阿里千问、GPT等大模型
     */
    @Autowired
    private ChatModel chatModel;

    /**
     * 内容检索器
     * 负责从知识库、文档库等外部数据源中检索相关信息
     * 为AI对话提供上下文支持和事实依据，增强回答的准确性和专业性
     */
    @Autowired
    private ContentRetriever contentRetriever;

    /**
     * 流式聊天模型
     * 支持流式输出的AI聊天模型，能够实现逐字逐句的实时响应
     * 相比普通聊天模型，提供更好的用户体验，用户无需等待完整响应生成
     */
    @Autowired
    private StreamingChatModel streamingChatModel;

    /**
     * 创建对话服务Bean的方法
     *
     * @Bean 注解告诉Spring：这个方法会返回一个重要的"组件"
     * Spring会调用这个方法，然后把返回的对象管理起来
     * 其他类需要对话服务时，Spring就会把这个对象提供给它们
     *
     * @return 配置好的对话服务实例，可以直接用来进行AI对话
     */
    @Bean
    @Primary
    public DialogueService dialogueService() {
        // 创建基于消息窗口的聊天记忆，最多保留10条对话消息
        // 这种记忆机制可以保持对话的连贯性，同时防止内存无限增长
//        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 使用AiServices构建器创建对话服务实例
        DialogueService codeService = AiServices.builder(DialogueService.class)
                .chatModel(chatModel)      // 设置AI聊天模型，提供自然语言处理能力
                .streamingChatModel(streamingChatModel) //实现流式输出
                // 设置聊天记忆提供者，为每个对话会话创建独立的记忆窗口
                .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
//                .chatMemory(chatMemory)    // 设置聊天记忆，维护对话上下文
                .contentRetriever(contentRetriever)  // 设置内容检索器，启用RAG能力，从知识库检索相关信息来增强AI回答
                .build();                  // 构建完整的对话服务实例

        return codeService;
    }

}
