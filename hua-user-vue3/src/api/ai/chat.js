import request from '@/utils/request'
import { getToken } from '@/utils/auth'

export function postChat(mode, sessionId, message) {
  return request({
    url: '/chat',
    method: 'post',
    timeout: 60000,
    data: {
      mode,
      sessionId,
      message
    }
  })
}

/**
 * 流式对话（SSE）
 * 注意：EventSource 无法设置自定义 Header，因此通过 query 携带 token。
 */
export function streamChat(sessionId, message) {
  const baseUrl = import.meta.env.VITE_APP_BASE_API
  const token = getToken() || ''
  const url =
    `${baseUrl}/chat/stream?mode=stream` +
    `&sessionId=${encodeURIComponent(sessionId)}` +
    `&message=${encodeURIComponent(message)}` +
    `&token=${encodeURIComponent(token)}`
  return new EventSource(url)
}

