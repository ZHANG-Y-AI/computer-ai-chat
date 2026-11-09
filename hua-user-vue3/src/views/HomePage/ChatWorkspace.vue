<template>
  <div class="chat-workspace-root">
    <div class="chat-container">
      <div class="chat-toolbar">
        <div class="mode-title">{{ modeTitle }}</div>
        <div class="toolbar-actions">
          <el-button size="small" @click="newSession" :disabled="isLoading">新建会话</el-button>
          <el-button
            v-if="mode === 'stream' && isLoading"
            size="small"
            type="danger"
            @click="stopStream"
          >
            停止生成
          </el-button>
        </div>
      </div>

      <div class="chat-history" ref="chatHistory">
        <div v-if="messages.length === 0" class="welcome-message">
          <div class="welcome-icon">👋</div>
          <h3>您好！我是编程助手</h3>
        </div>
        <div
          v-for="(message, index) in messages"
          :key="index"
          :class="['message', message.role === 'user' ? 'user' : 'ai']"
        >
          <div class="message-avatar">
            <div v-if="message.role === 'user'" class="avatar user-avatar">👤</div>
            <div v-else class="avatar ai-avatar">🤖</div>
          </div>
          <div class="message-content-wrapper">
            <div class="message-content">
              <div
                v-if="message.role === 'assistant' && message.mode === 'report'"
                class="report-card"
              >
                <div class="report-card-header">
                  <div>
                    <div class="report-card-kicker">报告生成结果</div>
                    <h3 class="report-card-title">{{ parseReport(message.content).title }}</h3>
                  </div>
                  <el-button size="small" plain @click="copyReport(message.content)">复制报告</el-button>
                </div>
                <div class="report-card-body">
                  <p
                    v-for="(paragraph, paragraphIndex) in parseReport(message.content).paragraphs"
                    :key="paragraphIndex"
                    class="report-paragraph"
                    :class="{ 'is-section': isReportSection(paragraph) }"
                  >
                    {{ paragraph }}
                  </p>
                  <div v-if="!message.content" class="report-empty">报告生成中...</div>
                </div>
              </div>
              <div v-else class="message-text">{{ message.content }}</div>
              <div v-if="message.role === 'assistant' && message.mode === 'rag'" class="source-card">
                <div class="source-card-title">参考来源</div>
                <template v-if="message.sources && message.sources.length">
                  <div
                    v-for="(source, sourceIndex) in message.sources"
                    :key="sourceIndex"
                    class="source-item"
                  >
                    <div class="source-item-header">
                      <span class="source-file">{{ source.fileName || '未知来源' }}</span>
                      <span v-if="typeof source.score === 'number'" class="source-score">
                        相似度 {{ formatScore(source.score) }}
                      </span>
                    </div>
                    <div v-if="source.preview" class="source-preview">{{ source.preview }}</div>
                  </div>
                </template>
                <div v-else class="source-empty">未检索到明确来源</div>
              </div>
              <div
                v-if="
                  message.role === 'assistant' &&
                  isLoading &&
                  mode === 'stream' &&
                  index === messages.length - 1
                "
                class="typing-indicator"
              >
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
                <div class="typing-dot"></div>
              </div>
            </div>
            <div class="message-time">{{ message.time }}</div>
          </div>
        </div>
      </div>

      <div class="chat-input-container">
        <div class="input-wrapper">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="2"
            :maxrows="4"
            :placeholder="placeholder"
            :disabled="isLoading"
            @keydown.enter.exact.prevent="sendMessage"
            class="custom-input"
          />
          <div class="input-actions">
            <div class="char-count">{{ inputMessage.length }}/500</div>
            <el-button
              type="primary"
              @click="sendMessage"
              :disabled="isLoading || !inputMessage.trim()"
              class="send-button"
              :class="{ loading: isLoading }"
            >
              <span v-if="!isLoading">发送</span>
              <div v-else class="loading-spinner"></div>
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute } from 'vue-router'
import { postChat, streamChat } from '@/api/ai/chat'
import useUserStore from '@/store/modules/user'
import { useChatSessionStore } from '@/store/modules/chatSession'

const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatSessionStore()

const chatHistory = ref(null)
const inputMessage = ref('')
const messages = ref([])
const isLoading = ref(false)
const streamingText = ref('')
/** @type {import('vue').Ref<EventSource | null>} */
const eventSource = ref(null)

const mode = computed(() => route.meta.chatMode || 'stream')

const modeTitleMap = {
  normal: '普通对话',
  rag: 'RAG对话',
  report: '报告生成',
  stream: '流式对话'
}
const modeTitle = computed(() => modeTitleMap[mode.value] || '对话')

const placeholder = computed(() => {
  if (mode.value === 'report') {
    return '请输入报告主题与要求，例如：\n- 主题：Spring Security + JWT鉴权流程\n- 输出：摘要/要点/步骤/注意事项\n- 字数：800字左右'
  }
  if (mode.value === 'rag') {
    return '请输入需要基于知识库回答的问题，例如：本项目的JWT鉴权流程是什么？'
  }
  if (mode.value === 'stream') {
    return '请输入问题（将逐段生成并实时展示）...'
  }
  return '请输入您的问题...'
})

let skipPersist = false
let persistTimer = null

const stopStream = () => {
  if (eventSource.value) {
    eventSource.value.close()
    eventSource.value = null
  }
  isLoading.value = false
  streamingText.value = ''
}

function loadFromStore() {
  skipPersist = true
  const s = chatStore.getActiveSession(mode.value)
  messages.value = s ? JSON.parse(JSON.stringify(s.messages)) : []
  nextTick(() => {
    setTimeout(() => {
      skipPersist = false
    }, 50)
  })
}

watch(
  () => chatStore.activeId(mode.value),
  () => {
    stopStream()
    inputMessage.value = ''
    loadFromStore()
  }
)

watch(
  messages,
  () => {
    if (skipPersist) return
    if (persistTimer) clearTimeout(persistTimer)
    persistTimer = setTimeout(() => {
      chatStore.persistActiveMessages(mode.value, messages.value)
    }, 400)
  },
  { deep: true }
)

onMounted(() => {
  chatStore.setUserKey(userStore.id)
  chatStore.ensureActiveSession(mode.value)
  loadFromStore()
})

watch(
  () => userStore.id,
  (id) => {
    chatStore.setUserKey(id)
    chatStore.ensureActiveSession(mode.value)
    loadFromStore()
  }
)

const newSession = () => {
  stopStream()
  chatStore.createSession(mode.value)
}

const formatScore = (score) => {
  if (typeof score !== 'number' || Number.isNaN(score)) return ''
  return `${Math.round(score * 100)}%`
}

const parseReport = (content = '') => {
  const lines = content
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
  const title = lines[0] || '报告生成结果'
  return {
    title,
    paragraphs: lines.slice(1)
  }
}

const isReportSection = (paragraph = '') => {
  return /^([一二三四五六七八九十]+、|\d+[.、]|#{1,6}\s+)/.test(paragraph)
}

const copyReport = async (content = '') => {
  if (!content) return
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('报告内容已复制')
  } catch (error) {
    console.error('复制报告失败:', error)
    ElMessage.error('复制失败，请手动选择文本复制')
  }
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = {
    role: 'user',
    content: inputMessage.value,
    time: new Date().toLocaleTimeString(),
    mode: mode.value
  }
  messages.value.push(userMessage)

  const messageToSend = inputMessage.value
  inputMessage.value = ''

  setTimeout(() => {
    scrollToBottom()
  }, 0)

  try {
    isLoading.value = true
    streamingText.value = ''

    const session = chatStore.getActiveSession(mode.value)
    const backendId = session?.backendSessionId
    if (backendId == null) {
      throw new Error('no active session')
    }

    const aiMessage = {
      role: 'assistant',
      content: '',
      sources: [],
      time: new Date().toLocaleTimeString(),
      mode: mode.value
    }
    messages.value.push(aiMessage)

    if (mode.value === 'stream') {
      eventSource.value = streamChat(backendId, messageToSend)

      eventSource.value.onmessage = (event) => {
        const chunk = event.data
        streamingText.value += chunk

        const lastMessage = messages.value[messages.value.length - 1]
        if (lastMessage && lastMessage.role === 'assistant') {
          lastMessage.content = streamingText.value
        }
        setTimeout(() => scrollToBottom(), 0)
      }

      eventSource.value.onerror = () => {
        stopStream()

        const lastMessage = messages.value[messages.value.length - 1]
        if (lastMessage && lastMessage.role === 'assistant') {
          lastMessage.content =
            streamingText.value || '抱歉，回复似乎出现了问题，请稍后重试。'
        }
        forceScrollToBottom()
      }
    } else {
      const res = await postChat(mode.value, backendId, messageToSend)
      const lastMessage = messages.value[messages.value.length - 1]
      if (lastMessage && lastMessage.role === 'assistant') {
        lastMessage.content = res?.answer || ''
        lastMessage.sources = mode.value === 'rag' ? (res?.sources || []) : []
      }
      isLoading.value = false
      setTimeout(() => scrollToBottom(), 0)
    }
  } catch (error) {
    console.error('对话出错:', error)
    const lastMessage = messages.value[messages.value.length - 1]
    if (lastMessage?.role === 'assistant' && !lastMessage.content) {
      lastMessage.content = '抱歉，我在处理您的问题时遇到了错误，请稍后再试。'
      lastMessage.time = new Date().toLocaleTimeString()
    } else {
      messages.value.push({
        role: 'assistant',
        content: '抱歉，我在处理您的问题时遇到了错误，请稍后再试。',
        time: new Date().toLocaleTimeString(),
        mode: mode.value
      })
    }
    isLoading.value = false
    streamingText.value = ''
  }
}

const scrollToBottom = () => {
  nextTick(() => {
    if (chatHistory.value) {
      chatHistory.value.scrollTop = chatHistory.value.scrollHeight
    }
  })
}

const forceScrollToBottom = () => {
  if (chatHistory.value) {
    chatHistory.value.scrollTop = chatHistory.value.scrollHeight
  }
}

onBeforeUnmount(() => {
  stopStream()
  if (persistTimer) clearTimeout(persistTimer)
})
</script>

<style scoped>
.chat-workspace-root {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  font-family:
    'Inter',
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    Roboto,
    sans-serif;
}

.chat-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;
  box-sizing: border-box;
  min-height: 0;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  padding: 10px 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.mode-title {
  font-weight: 600;
  font-size: 16px;
  color: #2c3e50;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chat-history {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  border-radius: 16px;
  background-color: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  scroll-behavior: smooth;
  min-height: 200px;
}

.welcome-message {
  text-align: center;
  padding: 40px 20px;
  color: #495057;
}

.welcome-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.welcome-message h3 {
  margin: 0 0 12px;
  font-size: 22px;
  font-weight: 600;
}

.message {
  display: flex;
  margin-bottom: 24px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message.user {
  flex-direction: row-reverse;
}

.message-avatar {
  margin: 0 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-avatar {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
}

.ai-avatar {
  background: linear-gradient(135deg, #9b59b6, #8e44ad);
  color: white;
}

.message-content-wrapper {
  max-width: 70%;
}

.message-content {
  padding: 12px 18px;
  border-radius: 18px;
  position: relative;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message.user .message-content {
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.ai .message-content {
  background: #f8f9fa;
  color: #495057;
  border-bottom-left-radius: 4px;
  border: 1px solid #eef0f5;
}

.message-text {
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.report-card {
  min-width: min(680px, 100%);
  overflow: hidden;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  border: 1px solid #dbe8ff;
  box-shadow: 0 10px 30px rgba(64, 158, 255, 0.12);
}

.report-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding: 18px 20px;
  background: linear-gradient(135deg, #ecf5ff 0%, #f5f3ff 100%);
  border-bottom: 1px solid #dbe8ff;
}

.report-card-kicker {
  margin-bottom: 6px;
  font-size: 12px;
  font-weight: 700;
  color: #409eff;
  letter-spacing: 0.08em;
}

.report-card-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.4;
  color: #1f2d3d;
}

.report-card-body {
  padding: 18px 20px 20px;
}

.report-paragraph {
  margin: 0 0 12px;
  line-height: 1.8;
  color: #3f4a56;
  white-space: pre-wrap;
}

.report-paragraph.is-section {
  margin-top: 18px;
  margin-bottom: 10px;
  padding-left: 10px;
  border-left: 4px solid #409eff;
  font-weight: 700;
  color: #243447;
  background: linear-gradient(90deg, rgba(64, 158, 255, 0.08), transparent);
}

.report-empty {
  color: #909399;
  font-size: 14px;
}

.source-card {
  margin-top: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #ffffff;
  border: 1px solid #e4e7ed;
}

.source-card-title {
  font-size: 13px;
  font-weight: 700;
  color: #606266;
  margin-bottom: 8px;
}

.source-item {
  padding: 10px;
  border-radius: 10px;
  background: #f6f8fb;
  border: 1px solid #edf0f5;
}

.source-item + .source-item {
  margin-top: 8px;
}

.source-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 6px;
}

.source-file {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  word-break: break-all;
}

.source-score {
  flex-shrink: 0;
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  border-radius: 999px;
  padding: 2px 8px;
}

.source-preview,
.source-empty {
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}

.message-time {
  font-size: 12px;
  color: #adb5bd;
  margin-top: 6px;
  padding: 0 4px;
}

.typing-indicator {
  display: flex;
  align-items: center;
  margin-top: 8px;
}

.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #adb5bd;
  margin: 0 2px;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-dot:nth-child(1) {
  animation-delay: -0.32s;
}

.typing-dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes typing {
  0%,
  80%,
  100% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.chat-input-container {
  background: white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

.input-wrapper {
  position: relative;
}

.custom-input :deep(.el-textarea__inner) {
  border-radius: 12px;
  border: 1px solid #eef0f5;
  padding: 16px;
  font-size: 16px;
  resize: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
}

.custom-input :deep(.el-textarea__inner:focus) {
  border-color: #3498db;
  box-shadow: 0 2px 12px rgba(52, 152, 219, 0.2);
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.char-count {
  font-size: 14px;
  color: #adb5bd;
}

.send-button {
  border-radius: 12px;
  padding: 10px 24px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(52, 152, 219, 0.3);
  border: none;
}

.send-button:hover:not(.is-disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(52, 152, 219, 0.4);
}

.send-button.loading {
  background-color: #bdc3c7;
  pointer-events: none;
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid transparent;
  border-top: 2px solid white;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
</style>
