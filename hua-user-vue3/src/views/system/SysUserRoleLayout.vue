<template>
  <div class="ur-shell">
    <router-view />
  </div>
</template>

<script setup name="SysUserRoleLayout">
</script>

<style scoped>
.ur-shell {
  min-height: calc(100vh - 120px);
  background: var(--el-bg-color-page);
  padding: 0;
}
</style>
