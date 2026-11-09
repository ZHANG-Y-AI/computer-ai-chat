<template>
  <div class="app-container kb-ops-root">
    <div class="kb-ops-head">
      <h2 class="kb-ops-head-title">知识库运维</h2>
      <p class="kb-ops-head-desc">索引与状态运维、全库筛选与统计</p>
    </div>

    <el-row :gutter="12" class="mt16">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-val">{{ statsSummary.total }}</div>
          <div class="stat-label">文档总数</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-val stat-warn">{{ statIdx('0') }}</div>
          <div class="stat-label">待索引</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-val stat-ok">{{ statIdx('1') }}</div>
          <div class="stat-label">已索引</div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-val stat-fail">{{ statIdx('2') }}</div>
          <div class="stat-label">索引失败</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt16">
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" header="索引状态分布">
          <div ref="chartIndexRef" class="chart-box" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card shadow="never" header="各模块文档数">
          <div ref="chartModuleRef" class="chart-box" />
        </el-card>
      </el-col>
    </el-row>

    <el-form :model="queryParams" :inline="true" class="mt16" label-width="80px">
      <el-form-item label="模块">
        <el-select v-model="queryParams.module" placeholder="全部模块" clearable style="width: 180px" @change="handleQuery">
          <el-option v-for="m in modules" :key="m.code" :label="m.label" :value="m.code" />
        </el-select>
      </el-form-item>
      <el-form-item label="标题">
        <el-input v-model="queryParams.title" placeholder="标题" clearable style="width: 200px" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="索引状态">
        <el-select v-model="queryParams.indexStatus" placeholder="全部" clearable style="width: 120px">
          <el-option label="待索引" value="0" />
          <el-option label="已索引" value="1" />
          <el-option label="失败" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="文档状态">
        <el-select v-model="queryParams.status" placeholder="全部" clearable style="width: 120px">
          <el-option label="正常" value="0" />
          <el-option label="停用" value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="docList" :row-class-name="kbRowClassName">
      <el-table-column label="ID" prop="docId" width="72" align="center" />
      <el-table-column label="模块" width="120" align="center">
        <template #default="scope">{{ moduleLabel(scope.row.module) }}</template>
      </el-table-column>
      <el-table-column label="标题" prop="title" min-width="140" show-overflow-tooltip />
      <el-table-column label="文件名" prop="fileName" min-width="120" show-overflow-tooltip />
      <el-table-column label="索引" prop="indexStatus" width="96" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.indexStatus === '1'" type="success">已索引</el-tag>
          <el-tag v-else-if="scope.row.indexStatus === '2'" type="danger">失败</el-tag>
          <el-tag v-else type="info">待索引</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="文档状态" prop="status" width="120" align="center">
        <template #default="scope">
          <template v-if="hasKbStatusPerm">
            <el-switch
              :model-value="scope.row.status === '0'"
              inline-prompt
              active-text="启"
              inactive-text="停"
              style="--el-switch-on-color: var(--el-color-success); --el-switch-off-color: var(--el-color-warning)"
              @change="(on) => handleStatusToggle(scope.row, on)"
            />
          </template>
          <template v-else>
            <el-tag v-if="scope.row.status === '1'" type="warning">停用</el-tag>
            <el-tag v-else type="success">正常</el-tag>
          </template>
        </template>
      </el-table-column>
      <el-table-column label="创建人" prop="createBy" width="100" show-overflow-tooltip />
      <el-table-column label="更新时间" prop="updateTime" width="165" align="center">
        <template #default="scope">
          <span>{{ parseTime(scope.row.updateTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="240" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
          <el-button link type="primary" @click="handleReindex(scope.row)" v-hasPermi="['kb:doc:index']" :disabled="scope.row.status === '1'">重建索引</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row)" v-hasPermi="['kb:doc:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0"
      :total="total"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />

    <el-drawer v-model="detailOpen" title="文档详情" size="640px" destroy-on-close @closed="onDetailClosed">
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detailDoc">
          <el-descriptions :column="1" border size="small" class="mb16">
            <el-descriptions-item label="文档 ID">{{ detailDoc.docId }}</el-descriptions-item>
            <el-descriptions-item label="模块">{{ moduleLabel(detailDoc.module) }}</el-descriptions-item>
            <el-descriptions-item label="标题">{{ detailDoc.title }}</el-descriptions-item>
            <el-descriptions-item label="文件名">{{ detailDoc.fileName }}</el-descriptions-item>
            <el-descriptions-item label="索引状态">
              <el-tag v-if="detailDoc.indexStatus === '1'" type="success">已索引</el-tag>
              <el-tag v-else-if="detailDoc.indexStatus === '2'" type="danger">失败</el-tag>
              <el-tag v-else type="info">待索引</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="文档状态">
              <el-tag v-if="detailDoc.status === '1'" type="warning">停用</el-tag>
              <el-tag v-else type="success">正常</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建人">{{ detailDoc.createBy || '—' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ parseTime(detailDoc.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ parseTime(detailDoc.updateTime) }}</el-descriptions-item>
            <el-descriptions-item label="备注" v-if="detailDoc.remark">{{ detailDoc.remark }}</el-descriptions-item>
          </el-descriptions>
          <div class="preview-title">正文预览</div>
          <div v-if="!canPreview" class="preview-muted">无「知识库查询」权限时仅展示元数据。</div>
          <div v-else v-loading="previewLoading" class="preview-box">
            <pre v-if="previewText !== null" class="preview-pre">{{ previewText }}</pre>
            <div v-else-if="!previewLoading" class="preview-muted">暂无预览内容</div>
          </div>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup name="KbOps">
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, getCurrentInstance } from 'vue'
import * as echarts from 'echarts'
import {
  listKbDocument,
  delKbDocument,
  reindexKbDocument,
  getKbDocument,
  previewKbDocument,
  statsKbDocument,
  updateKbDocumentStatus
} from '@/api/kb/document'
import auth from '@/plugins/auth'

const { proxy } = getCurrentInstance()
const modules = [
  { code: 'LEARNING_PATH', label: '学习路线' },
  { code: 'PROJECT_LAB', label: '项目实战' },
  { code: 'QNA', label: '知识点答疑' },
  { code: 'DEV_TOOLCHAIN', label: '开发者工具链' }
]

function moduleLabel(code) {
  const m = modules.find(x => x.code === code)
  return m ? m.label : (code || '—')
}

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  module: undefined,
  title: undefined,
  indexStatus: undefined,
  status: undefined
})

const loading = ref(false)
const docList = ref([])
const total = ref(0)

const statsSummary = ref({ total: 0, byIndexStatus: {}, byModule: {} })

function statIdx(k) {
  const m = statsSummary.value.byIndexStatus || {}
  const v = m[k]
  return v != null ? v : 0
}

function buildListParams() {
  const p = { ...queryParams }
  if (!p.module) delete p.module
  if (p.title === '' || p.title == null) delete p.title
  if (p.indexStatus === '' || p.indexStatus == null) delete p.indexStatus
  if (p.status === '' || p.status == null) delete p.status
  return p
}

function loadStats() {
  const mod = queryParams.module || undefined
  statsKbDocument(mod ? { module: mod } : {}).then(res => {
    statsSummary.value = {
      total: res.data.total ?? 0,
      byIndexStatus: res.data.byIndexStatus || {},
      byModule: res.data.byModule || {}
    }
    nextTick(() => updateCharts())
  }).catch(() => {})
}

function getList() {
  loading.value = true
  listKbDocument(buildListParams()).then(res => {
    docList.value = res.rows
    total.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
  loadStats()
}

function resetQuery() {
  queryParams.module = undefined
  queryParams.title = undefined
  queryParams.indexStatus = undefined
  queryParams.status = undefined
  handleQuery()
}

function kbRowClassName({ row }) {
  return row.indexStatus === '2' ? 'kb-row-fail' : ''
}

const chartIndexRef = ref(null)
const chartModuleRef = ref(null)
let chartIndexInst
let chartModuleInst

const indexPieData = computed(() => {
  const m = statsSummary.value.byIndexStatus || {}
  return [
    { name: '待索引', value: m['0'] || 0 },
    { name: '已索引', value: m['1'] || 0 },
    { name: '失败', value: m['2'] || 0 }
  ]
})

const moduleBarOption = computed(() => {
  const raw = statsSummary.value.byModule || {}
  const names = []
  const vals = []
  Object.keys(raw).sort().forEach(k => {
    names.push(moduleLabel(k))
    vals.push(raw[k])
  })
  return { names, vals }
})

function updateCharts() {
  if (chartIndexRef.value) {
    if (!chartIndexInst) chartIndexInst = echarts.init(chartIndexRef.value)
    chartIndexInst.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['36%', '68%'],
        data: indexPieData.value,
        label: { formatter: '{b}: {c}' }
      }]
    }, true)
  }
  if (chartModuleRef.value) {
    if (!chartModuleInst) chartModuleInst = echarts.init(chartModuleRef.value)
    const { names, vals } = moduleBarOption.value
    chartModuleInst.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 48, right: 16, top: 24, bottom: 48 },
      xAxis: { type: 'category', data: names.length ? names : ['—'], axisLabel: { rotate: names.length > 3 ? 20 : 0 } },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ type: 'bar', data: vals.length ? vals : [0], itemStyle: { color: '#409EFF' } }]
    }, true)
  }
}

function onResize() {
  chartIndexInst?.resize()
  chartModuleInst?.resize()
}

onMounted(() => {
  getList()
  loadStats()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  chartIndexInst?.dispose()
  chartModuleInst?.dispose()
  chartIndexInst = null
  chartModuleInst = null
})

const detailOpen = ref(false)
const detailLoading = ref(false)
const detailDoc = ref(null)
const previewLoading = ref(false)
const previewText = ref(null)
const canPreview = computed(() => auth.hasPermi('kb:doc:query'))
const hasKbStatusPerm = computed(() => auth.hasPermi('kb:knowledge:list'))

function onDetailClosed() {
  detailDoc.value = null
  previewText.value = null
}

function openDetail(row) {
  detailOpen.value = true
  detailDoc.value = null
  previewText.value = null
  detailLoading.value = true
  getKbDocument(row.docId).then(res => {
    detailDoc.value = res.data
  }).catch(() => {
    proxy.$modal.msgError('加载详情失败')
  }).finally(() => { detailLoading.value = false })
  if (canPreview.value) {
    previewLoading.value = true
    previewKbDocument(row.docId).then(res => {
      previewText.value = res.data.content ?? ''
    }).catch(() => {
      previewText.value = null
      proxy.$modal.msgWarning('正文预览加载失败')
    }).finally(() => { previewLoading.value = false })
  }
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除「' + row.title + '」？').then(() => {
    return delKbDocument(row.docId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
    loadStats()
  }).catch(() => {})
}

function handleReindex(row) {
  proxy.$modal.confirm('确认为「' + row.title + '」重建向量索引？').then(() => {
    return reindexKbDocument(row.docId)
  }).then(() => {
    proxy.$modal.msgSuccess('已触发重建')
    getList()
    loadStats()
  }).catch(() => {})
}

function handleStatusToggle(row, enabled) {
  const next = enabled ? '0' : '1'
  const tip = enabled
    ? '确认启用「' + row.title + '」？将重新写入向量索引。'
    : '确认停用「' + row.title + '」？将从向量库移除，对话中不再引用。'
  proxy.$modal.confirm(tip).then(() => {
    return updateKbDocumentStatus(row.docId, next)
  }).then((res) => {
    Object.assign(row, res.data)
    proxy.$modal.msgSuccess('状态已更新')
    loadStats()
  }).catch(() => {})
}

function parseTime(val) {
  return proxy.parseTime(val)
}
</script>

<style scoped>
.kb-ops-root {
  padding-top: 0;
}
.kb-ops-head {
  margin-bottom: 4px;
}
.kb-ops-head-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  letter-spacing: 0.5px;
}
.kb-ops-head-desc {
  margin: 6px 0 0;
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.mt16 {
  margin-top: 16px;
}
.stat-card {
  text-align: center;
}
.stat-val {
  font-size: 22px;
  font-weight: 600;
  line-height: 1.2;
}
.stat-val.stat-ok {
  color: var(--el-color-success);
}
.stat-val.stat-fail {
  color: var(--el-color-danger);
}
.stat-val.stat-warn {
  color: var(--el-color-warning);
}
.stat-label {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
.chart-box {
  width: 100%;
  height: 280px;
}
.mb16 {
  margin-bottom: 16px;
}
.preview-title {
  font-weight: 600;
  margin-bottom: 8px;
}
.preview-box {
  min-height: 120px;
}
.preview-pre {
  margin: 0;
  padding: 12px;
  background: var(--el-fill-color-light);
  border-radius: 6px;
  max-height: 420px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  line-height: 1.5;
}
.preview-muted {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}
.detail-body {
  min-height: 160px;
}
</style>

<style>
.el-table .kb-row-fail > td {
  background-color: var(--el-color-danger-light-9) !important;
}
</style>
