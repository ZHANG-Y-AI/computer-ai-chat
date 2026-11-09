import { defineStore } from 'pinia'

export const CHAT_MODES = ['normal', 'rag', 'report', 'stream']

function emptyBucket() {
  return { sessions: [], activeLocalId: null }
}

function defaultBuckets() {
  return {
    normal: emptyBucket(),
    rag: emptyBucket(),
    report: emptyBucket(),
    stream: emptyBucket()
  }
}

function genLocalId() {
  return crypto.randomUUID()
}

/** 后端 sessionId 为 int，避免与常见魔法数冲突 */
function genBackendSessionId() {
  return Math.floor(100000 + Math.random() * 900000000)
}

function storageKey(userKey) {
  return `hua_chat_sessions_${userKey}_v1`
}

export const useChatSessionStore = defineStore('chatSession', {
  state: () => ({
    userKey: 'guest',
    buckets: defaultBuckets()
  }),
  getters: {
    listSessions: (state) => (mode) => state.buckets[mode]?.sessions ?? [],
    activeId: (state) => (mode) => state.buckets[mode]?.activeLocalId ?? null,
    getActiveSession: (state) => (mode) => {
      const b = state.buckets[mode]
      if (!b?.activeLocalId) return null
      return b.sessions.find((s) => s.localId === b.activeLocalId) ?? null
    }
  },
  actions: {
    setUserKey(key) {
      const k = key ? String(key) : 'guest'
      this.userKey = k
      this.loadFromStorage()
    },
    loadFromStorage() {
      try {
        const raw = localStorage.getItem(storageKey(this.userKey))
        if (!raw) {
          this.buckets = defaultBuckets()
          return
        }
        const parsed = JSON.parse(raw)
        const next = defaultBuckets()
        for (const m of CHAT_MODES) {
          const b = parsed[m]
          next[m] = {
            sessions: Array.isArray(b?.sessions) ? b.sessions : [],
            activeLocalId: b?.activeLocalId ?? null
          }
        }
        this.buckets = next
      } catch {
        this.buckets = defaultBuckets()
      }
    },
    saveToStorage() {
      try {
        localStorage.setItem(storageKey(this.userKey), JSON.stringify(this.buckets))
      } catch (e) {
        console.warn('chat session persist failed', e)
      }
    },
    /** 当前模式下若无会话则创建一个 */
    ensureActiveSession(mode) {
      const b = this.buckets[mode]
      if (!b) return
      if (!b.sessions.length) {
        this._appendSession(mode, {
          localId: genLocalId(),
          backendSessionId: genBackendSessionId(),
          title: '新会话',
          updatedAt: Date.now(),
          messages: []
        })
        this.saveToStorage()
        return
      }
      const active = b.sessions.find((s) => s.localId === b.activeLocalId)
      if (!active) {
        b.activeLocalId = b.sessions[0].localId
        this.saveToStorage()
      }
    },
    _appendSession(mode, session) {
      const b = this.buckets[mode]
      b.sessions.unshift(session)
      b.activeLocalId = session.localId
    },
    /** 新建会话：新增一条侧栏记录并切换 */
    createSession(mode) {
      const b = this.buckets[mode]
      const session = {
        localId: genLocalId(),
        backendSessionId: genBackendSessionId(),
        title: '新会话',
        updatedAt: Date.now(),
        messages: []
      }
      b.sessions.unshift(session)
      b.activeLocalId = session.localId
      this.saveToStorage()
      return session
    },
    selectSession(mode, localId) {
      const b = this.buckets[mode]
      if (!b.sessions.some((s) => s.localId === localId)) return
      b.activeLocalId = localId
      this.saveToStorage()
    },
    deleteSession(mode, localId) {
      const b = this.buckets[mode]
      b.sessions = b.sessions.filter((s) => s.localId !== localId)
      if (b.activeLocalId === localId) {
        b.activeLocalId = b.sessions[0]?.localId ?? null
        if (!b.sessions.length) {
          this.ensureActiveSession(mode)
        }
      }
      this.saveToStorage()
    },
    /** 写回当前会话消息（同一会话多轮只更新该条） */
    persistActiveMessages(mode, messages) {
      const b = this.buckets[mode]
      const session = b.sessions.find((s) => s.localId === b.activeLocalId)
      if (!session) return
      session.messages = JSON.parse(JSON.stringify(messages))
      session.updatedAt = Date.now()
      if (session.title === '新会话') {
        const firstUser = messages.find((m) => m.role === 'user')
        if (firstUser?.content) {
          const t = String(firstUser.content).trim().replace(/\s+/g, ' ')
          session.title = t.length > 40 ? `${t.slice(0, 40)}…` : t
        }
      }
      this.saveToStorage()
    }
  }
})
