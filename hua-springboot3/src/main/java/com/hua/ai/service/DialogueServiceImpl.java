package com.hua.ai.service;

import com.hua.ai.domain.Report;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.Result;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;

/**
 * 对话服务实现类
 * 这个类专门负责处理用户与AI之间的对话交互
 * 就像是一个智能客服的"大脑"，接收用户问题，返回AI回答
 */
/**
 * 旧的对话服务实现（保留用于参考/对比），当前项目实际对话能力由
 * {@link com.hua.ai.DialogueServiceFactory} 基于 LangChain4j 的 AiServices 统一构建提供。
 */
@Deprecated
public class DialogueServiceImpl implements DialogueService{

    // 自动注入AI聊天模型，Spring会自动找到合适的实现类并赋值
    // 这就像给我们的服务配备了一个"AI大脑"
    @Autowired
    private ChatModel chatModel;

    //定义提示词
    private static final String prompt = "你是一个专业的编程对话小助手，帮助用户解答各类编程学习相关的问题，并给出学习建议。" +
            "1.解答 Python、Java、C/C++ 等主流编程语言的基础知识问题\n" +
            "2.提供各编程语言的系统学习路线指导（入门 → 进阶 → 实战）\n" +
            "3.解答算法与数据结构、数据库、操作系统等计算机基础知识\n" +
            "4.提供项目实战建议，帮助用户将理论知识转化为实践能力\n" +
            "5.根据用户的学习阶段，推荐合适的学习资源与学习方法\n" +
            "请用简洁易懂的语言回答，鼓励用户坚持学习，循序渐进地掌握编程技能。";

    /**
     * 与AI进行对话的主要方法
     *
     * @param message 用户输入的消息内容
     * @return AI生成的回答文本
     * <p>
     * 工作流程：
     * 1. 把用户文字包装成AI能理解的消息格式
     * 2. 发送给AI模型进行处理
     * 3. 从AI回复中提取文字内容返回给用户
     */
    @Override
    public String chat(int memoryId, String message) {
        SystemMessage systemMessage = SystemMessage.from(prompt);
        UserMessage userMessage = UserMessage.from(message);

        // 第一步：把用户消息发送给AI模型，等待AI思考并生成回复
        // 这就像是把问题递给AI专家，让它来解答
        ChatResponse res = chatModel.chat(systemMessage, userMessage);

        // 第二步：从AI的完整回复中，专门提取出文字内容部分
        // 就像从专家的长篇大论中摘出最核心的答案
        AiMessage aiMessage = res.aiMessage();

        // 第三步：返回纯文本的AI回答给调用者
        // 把AI的"专业术语"转换回普通人能看懂的文字
        return aiMessage.text();
    }

    /**
     * 获取对话报告
     *
     * @param message 用户输入的消息内容
     * @return 格式化后的报告内容
     */
    @Override
    public Report getReport(int memoryId, String message) {
        // 此处应实现获取报告的逻辑，暂时返回null
        return null;
    }

    /**
     * 使用RAG（检索增强生成）技术与AI进行对话
     *
     * @param message 用户输入的消息内容
     * @return 包含RAG处理结果的封装对象
     */
    @Override
    public Result<String> getChatRag(int memoryId, String message) {
        // 此处应实现RAG对话逻辑，暂时返回null
        return null;
    }

    /**
     * 基于SSE（Server-Sent Events）的流式对话接口
     *
     * @param memoryId 对话记忆ID，用于标识和维持对话的上下文记忆
     * @param message 用户输入的消息内容
     * @return 返回一个Flux流，包含AI实时生成的回复片段
     */
    @Override
    public Flux<String> sseChat(int memoryId, String message) {
        return Flux.empty();
    }
}
