<template>
  <div class="kb-shell">
    <aside class="kb-shell-aside">
      <div class="kb-shell-aside-title">知识库</div>
      <p class="kb-shell-aside-desc">文档与索引运维</p>
      <nav class="kb-shell-nav">
        <router-link
          class="kb-shell-nav-btn"
          active-class="is-active"
          to="/index/knowledge/doc"
        >
          <span class="kb-shell-nav-label">文档管理</span>
          <span class="kb-shell-nav-hint">四模块上传与列表</span>
        </router-link>
        <router-link
          class="kb-shell-nav-btn"
          active-class="is-active"
          to="/index/knowledge/ops"
        >
          <span class="kb-shell-nav-label">知识库运维</span>
          <span class="kb-shell-nav-hint">统计、筛选与索引</span>
        </router-link>
      </nav>
    </aside>
    <main class="kb-shell-main">
      <router-view />
    </main>
  </div>
</template>

<script setup name="KbShell">
</script>

<style scoped>
.kb-shell {
  display: flex;
  min-height: calc(100vh - 120px);
  background: var(--el-bg-color-page);
}
.kb-shell-aside {
  width: 220px;
  flex-shrink: 0;
  padding: 20px 16px;
  background: linear-gradient(180deg, var(--el-fill-color-blank) 0%, var(--el-fill-color-light) 100%);
  border-right: 1px solid var(--el-border-color-lighter);
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.04);
}
.kb-shell-aside-title {
  font-size: 17px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  letter-spacing: 0.5px;
}
.kb-shell-aside-desc {
  margin: 6px 0 18px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}
.kb-shell-nav {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.kb-shell-nav-btn {
  display: block;
  padding: 12px 14px;
  border-radius: 10px;
  text-decoration: none;
  color: var(--el-text-color-regular);
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  transition: border-color 0.2s, box-shadow 0.2s, color 0.2s;
}
.kb-shell-nav-btn:hover {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-5);
}
.kb-shell-nav-btn.is-active {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-3);
  background: var(--el-color-primary-light-9);
  box-shadow: 0 0 0 1px var(--el-color-primary-light-7);
}
.kb-shell-nav-label {
  display: block;
  font-size: 14px;
  font-weight: 600;
}
.kb-shell-nav-hint {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.85;
  font-weight: 400;
}
.kb-shell-main {
  flex: 1;
  min-width: 0;
  padding: 16px 20px 24px;
  overflow: auto;
}
</style>
