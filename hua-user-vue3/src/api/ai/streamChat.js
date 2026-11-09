/**
 * 流式对话接口 - 通过Server-Sent Events实现实时对话流
 * 用于与聊天API建立连接并接收流式响应
 *
 * @param {int} memoryId - 记忆/会话ID，用于保持对话上下文
 * @param {string} message - 用户发送的消息内容
 * @returns {Promise<EventSource>} 返回一个Promise，解析为EventSource对象用于接收服务器推送的数据
 */
export function streamDialogue(memoryId, message) {
    return new Promise((resolve) => {
        // 获取基础API地址，从环境变量中读取
        const baseUrl = import.meta.env.VITE_APP_BASE_API;

        // 创建EventSource连接，用于接收服务器推送的流式数据
        // 对message进行URL编码，确保特殊字符正确处理
        const eventSource = new EventSource(
            `${baseUrl}/chat/dialogue?memoryId=${memoryId}&message=${encodeURIComponent(message)}`
        );

        // 立即解析Promise，返回EventSource对象
        // 这样调用方可以立即设置事件监听器
        resolve(eventSource);
    });
}
