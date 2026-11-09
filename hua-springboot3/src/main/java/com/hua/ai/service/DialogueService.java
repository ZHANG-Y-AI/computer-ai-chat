package com.hua.ai.service;

import com.hua.ai.domain.Report;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * 对话服务接口
 * 这个服务提供了与AI进行对话的能力，可以理解为是一个智能聊天机器人的核心接口
 */
public interface DialogueService {

    /**
     * 与AI进行对话
     * 这个方法使用了系统提示词来定义AI的角色和行为模式，让AI按照特定的方式与用户交流
     *
     * @param message 用户输入的消息内容，比如："你好，我想了解一下人工智能"
     * @return AI根据用户消息生成的回复内容，比如："你好！我很乐意为你介绍人工智能的相关知识..."
     *
     * @SystemMessage 注解说明：
     * - fromResource = "prompt.txt" 表示从项目资源目录下的prompt.txt文件中读取系统提示词
     * - 系统提示词就像是给AI的"工作说明书"，告诉它应该扮演什么角色、用什么语气回答、有哪些限制等
     * - 例如prompt.txt里可能写着："你是一个友好的客服助手，请用热情礼貌的语气回答用户问题..."
     */
    @SystemMessage(fromResource = "prompt.txt")
    String chat(@MemoryId int memoryId, @UserMessage String message);

    /**
     * 获取对话报告
     * 基于用户输入的消息生成结构化的报告或总结，适用于需要格式化输出的场景
     *
     * @param message 用户输入的消息内容，通常包含生成报告的具体要求或主题
     * @return 格式化后的报告内容，可能包含分析结果、统计数据或结构化信息
     *
     * @SystemMessage 注解说明：
     * - 同样使用prompt.txt中的系统提示词来定义AI的角色和行为
     * - 与chat方法不同的是，这个方法更侧重于生成结构化的报告输出
     */
    @SystemMessage(fromResource = "prompt.txt")
    Report getReport(@MemoryId int memoryId, @UserMessage String message);

    /**
     * 使用RAG（检索增强生成）技术与AI进行对话
     * 该方法结合了外部知识检索和语言模型生成能力，提供更准确、信息更丰富的回答
     *
     * @param message 用户输入的消息内容，比如："请介绍一下机器学习的最新发展趋势"
     * @return 包含RAG处理结果的封装对象，其中包含：
     *         - AI生成的回答内容
     *         - 相关的检索来源信息
     *         - 置信度评分等元数据
     *         - 可能的错误信息或处理状态
     *
     * @SystemMessage 注解说明：
     * - 使用prompt.txt中的系统提示词来定义AI的基础行为
     * - RAG技术会在生成回答前先从知识库中检索相关信息，确保回答的准确性和时效性
     * - 适用于需要基于特定知识库或最新信息进行回答的场景
     */
    @SystemMessage(fromResource = "prompt.txt")
    Result<String> getChatRag(@MemoryId int memoryId, @UserMessage String message);

    /**
     * 基于SSE（Server-Sent Events）的流式对话接口
     * 该方法支持实时流式输出，适用于需要逐步显示AI回复内容的场景，如聊天界面
     *
     * @param memoryId 对话记忆ID，用于标识和维持对话的上下文记忆
     *                 - 通过@MemoryId注解标记，系统会根据此ID维护独立的对话历史
     *                 - 相同的memoryId可以保持多轮对话的连贯性
     *                 - 不同的memoryId会创建独立的对话会话
     * @param message 用户输入的消息内容，比如："请详细解释一下深度学习"
     * @return 返回一个Flux流，包含AI实时生成的回复片段
     *         - 回复内容会分成多个片段逐步推送
     *         - 客户端可以通过订阅这个流来实现打字机效果
     *         - 每个字符串片段都是完整回复的一部分
     *
     * 使用示例：
     * Flux<String> response = dialogueService.sseChat(123, "你好");
     * response.subscribe(chunk -> System.out.print(chunk));
     *
     * @SystemMessage 注解说明：
     * - 使用prompt.txt中的系统提示词定义AI角色和行为
     * - 结合@MemoryId确保流式对话的上下文连贯性
     * - @UserMessage明确标记用户输入内容
     *
     * 适用场景：
     * - 实时聊天应用
     * - 需要打字机效果的界面
     * - 长文本生成时的渐进式显示
     */
    @SystemMessage(fromResource = "prompt.txt")
    Flux<String> sseChat(@MemoryId int memoryId, @UserMessage String message);
}
