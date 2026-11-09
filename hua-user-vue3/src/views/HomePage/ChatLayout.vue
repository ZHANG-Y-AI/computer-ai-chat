<template>
  <div class="chat-layout">
    <aside class="session-sidebar">
      <div class="sidebar-brand">编程助手</div>
      <nav class="mode-nav">
        <router-link
          v-for="m in modeNav"
          :key="m.path"
          :to="m.path"
          class="mode-link"
          active-class="mode-link-active"
        >
          {{ m.label }}
        </router-link>
      </nav>
      <el-button type="primary" plain class="new-session-btn" @click="onNewSession">
        新建会话
      </el-button>
      <div class="session-list-label">会话记录</div>
      <el-scrollbar class="session-scroll">
        <ul class="session-list">
          <li
            v-for="s in sessions"
            :key="s.localId"
            :class="['session-item', { active: s.localId === activeLocalId }]"
            @click="onSelect(s.localId)"
          >
            <div class="session-item-main">
              <span class="session-title">{{ s.title || '新会话' }}</span>
              <span class="session-time">{{ formatTime(s.updatedAt) }}</span>
            </div>
            <el-button
              type="danger"
              link
              class="session-delete"
              @click.stop="onDelete(s.localId)"
            >
              删除
            </el-button>
          </li>
          <li v-if="!sessions.length" class="session-empty">暂无会话</li>
        </ul>
      </el-scrollbar>
    </aside>
    <main class="chat-main">
      <router-view :key="route.path" />
    </main>
  </div>
</template>

<script setup>
import { computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import useUserStore from '@/store/modules/user'
import { useChatSessionStore } from '@/store/modules/chatSession'

const route = useRoute()
const userStore = useUserStore()
const chatStore = useChatSessionStore()

const modeNav = [
  { path: '/index/chat/normal', label: '普通对话' },
  { path: '/index/chat/rag', label: 'RAG对话' },
  { path: '/index/chat/report', label: '报告生成' },
  { path: '/index/chat/stream', label: '流式对话' }
]

const mode = computed(() => route.meta.chatMode || 'stream')

const sessions = computed(() => chatStore.listSessions(mode.value))
const activeLocalId = computed(() => chatStore.activeId(mode.value))

onMounted(() => {
  chatStore.setUserKey(userStore.id)
})

watch(
  () => userStore.id,
  (id) => {
    chatStore.setUserKey(id)
  }
)

watch(
  () => mode.value,
  (m) => {
    chatStore.ensureActiveSession(m)
  },
  { immediate: true }
)

function onNewSession() {
  chatStore.createSession(mode.value)
}

function onSelect(localId) {
  chatStore.selectSession(mode.value, localId)
}

function onDelete(localId) {
  chatStore.deleteSession(mode.value, localId)
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.toLocaleDateString()} ${d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`
}
</script>

<style scoped>
.chat-layout {
  display: flex;
  height: calc(100vh - 72px);
  min-height: 480px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8f0 100%);
}

.session-sidebar {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  padding: 16px 12px;
  background: rgba(255, 255, 255, 0.85);
  border-right: 1px solid #e8ecf1;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.04);
}

.sidebar-brand {
  font-weight: 700;
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 12px;
  padding-left: 4px;
}

.mode-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 12px;
}

.mode-link {
  display: block;
  padding: 10px 12px;
  border-radius: 10px;
  color: #495057;
  text-decoration: none;
  font-size: 14px;
  transition: background 0.15s ease, color 0.15s ease;
}

.mode-link:hover {
  background: #eef2f7;
  color: #2980b9;
}

.mode-link-active {
  background: linear-gradient(135deg, #3498db22, #9b59b622);
  color: #2980b9;
  font-weight: 600;
}

.new-session-btn {
  width: 100%;
  margin-bottom: 16px;
}

.session-list-label {
  font-size: 12px;
  color: #adb5bd;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  margin-bottom: 8px;
  padding-left: 4px;
}

.session-scroll {
  flex: 1;
  min-height: 0;
}

.session-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 8px;
  border-radius: 10px;
  cursor: pointer;
  margin-bottom: 4px;
  border: 1px solid transparent;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.session-item:hover {
  background: #f1f3f5;
}

.session-item.active {
  background: #e7f1ff;
  border-color: #b6d4fe;
}

.session-item-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.session-title {
  font-size: 13px;
  color: #343a40;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-time {
  font-size: 11px;
  color: #adb5bd;
}

.session-delete {
  flex-shrink: 0;
  font-size: 12px;
}

.session-empty {
  padding: 16px 8px;
  font-size: 13px;
  color: #adb5bd;
  text-align: center;
}

.chat-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}
</style>
