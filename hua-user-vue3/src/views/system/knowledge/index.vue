<template>
  <div class="app-container">
    <!-- 四模块入口 -->
    <template v-if="!currentModule">
      <el-row :gutter="20">
        <el-col v-for="m in modules" :key="m.code" :xs="24" :sm="12" :md="12" :lg="6">
          <el-card shadow="hover" class="kb-card" @click="enterModule(m.code)">
            <div class="kb-card-title">{{ m.label }}</div>
            <div class="kb-card-desc">{{ m.desc }}</div>
            <el-button type="primary" link class="kb-card-action">进入管理</el-button>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 模块内：文档列表与上传 -->
    <template v-else>
      <el-page-header @back="goHub" :title="moduleTitle + ' — 文档管理'" />

      <el-row :gutter="12" class="mt16 mb16">
        <el-col :xs="12" :sm="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-val">{{ statCount('0') }}</div>
            <div class="stat-label">待索引</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-val stat-ok">{{ statCount('1') }}</div>
            <div class="stat-label">已索引</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-val stat-fail">{{ statCount('2') }}</div>
            <div class="stat-label">索引失败</div>
          </el-card>
        </el-col>
        <el-col :xs="12" :sm="6">
          <el-card shadow="never" class="stat-card">
            <div class="stat-val">{{ statsSummary.total }}</div>
            <div class="stat-label">本模块合计</div>
          </el-card>
        </el-col>
      </el-row>

      <el-form :model="queryParams" ref="queryRef" :inline="true" class="mt8" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="queryParams.title" placeholder="标题" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="索引状态" prop="indexStatus">
          <el-select v-model="queryParams.indexStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="待索引" value="0" />
            <el-option label="已索引" value="1" />
            <el-option label="失败" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="文档状态" prop="status">
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

      <el-row :gutter="10" class="mb8">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="openUpload" v-hasPermi="['kb:doc:add']">上传文档</el-button>
        </el-col>
      </el-row>

      <el-table v-loading="loading" :data="docList" :row-class-name="kbRowClassName">
        <el-table-column label="ID" prop="docId" width="70" align="center" />
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
        <el-table-column label="创建时间" prop="createTime" width="160" align="center">
          <template #default="scope">
            <span>{{ parseTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="更新时间" prop="updateTime" width="160" align="center">
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

      <el-dialog title="上传知识库文档" v-model="uploadOpen" width="520px" append-to-body destroy-on-close>
        <el-form ref="uploadRef" :model="uploadForm" :rules="uploadRules" label-width="80px">
          <el-form-item label="模块">
            <el-input :model-value="moduleTitle" disabled />
          </el-form-item>
          <el-form-item label="标题" prop="title">
            <el-input v-model="uploadForm.title" placeholder="显示用标题" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="文件" prop="file">
            <el-upload
              ref="uploadCtrl"
              :limit="1"
              :auto-upload="false"
              :on-change="onFileChange"
              accept=".txt,.md"
            >
              <el-button type="primary">选择文件</el-button>
              <template #tip>
                <div class="el-upload__tip">仅支持 .txt / .md，UTF-8 编码</div>
              </template>
            </el-upload>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="uploadOpen = false">取 消</el-button>
          <el-button type="primary" :loading="uploading" @click="submitUpload">确 定</el-button>
        </template>
      </el-dialog>

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
    </template>
  </div>
</template>

<script setup name="Knowledge">
import { ref, reactive, computed, watch, getCurrentInstance } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  listKbDocument,
  uploadKbDocument,
  delKbDocument,
  reindexKbDocument,
  getKbDocument,
  previewKbDocument,
  statsKbDocument,
  updateKbDocumentStatus
} from '@/api/kb/document'
import auth from '@/plugins/auth'

const { proxy } = getCurrentInstance()
const route = useRoute()
const router = useRouter()

const modules = [
  { code: 'LEARNING_PATH', label: '学习路线', desc: '路径规划、阶段目标与推荐顺序' },
  { code: 'PROJECT_LAB', label: '项目实战', desc: '综合项目与动手实验资料' },
  { code: 'QNA', label: '知识点答疑', desc: '常见问题与概念讲解' },
  { code: 'DEV_TOOLCHAIN', label: '开发者工具链', desc: '环境、工具与命令参考' }
]

const currentModule = computed(() => route.query.module || '')

const moduleTitle = computed(() => {
  const m = modules.find(x => x.code === currentModule.value)
  return m ? m.label : ''
})

function moduleLabel(code) {
  const m = modules.find(x => x.code === code)
  return m ? m.label : (code || '—')
}

function enterModule(code) {
  router.replace({ path: route.path, query: { module: code } })
}

function goHub() {
  router.replace({ path: route.path, query: {} })
}

const loading = ref(false)
const docList = ref([])
const total = ref(0)
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  module: '',
  title: undefined,
  indexStatus: undefined,
  status: undefined
})

const statsSummary = ref({ total: 0, byIndexStatus: {} })

function statCount(key) {
  const m = statsSummary.value.byIndexStatus || {}
  const v = m[key]
  return v != null ? v : 0
}

function loadModuleStats() {
  if (!queryParams.module) return
  statsKbDocument({ module: queryParams.module }).then(res => {
    statsSummary.value = {
      total: res.data.total ?? 0,
      byIndexStatus: res.data.byIndexStatus || {}
    }
  }).catch(() => {})
}

watch(
  () => route.query.module,
  (m) => {
    queryParams.module = m || ''
    if (m) {
      queryParams.pageNum = 1
      getList()
      loadModuleStats()
    } else {
      statsSummary.value = { total: 0, byIndexStatus: {} }
    }
  },
  { immediate: true }
)

function getList() {
  if (!queryParams.module) return
  loading.value = true
  listKbDocument(queryParams).then(res => {
    docList.value = res.rows
    total.value = res.total
    loading.value = false
  }).catch(() => { loading.value = false })
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryParams.title = undefined
  queryParams.indexStatus = undefined
  queryParams.status = undefined
  handleQuery()
}

function kbRowClassName({ row }) {
  return row.indexStatus === '2' ? 'kb-row-fail' : ''
}

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

const uploadOpen = ref(false)
const uploading = ref(false)
const uploadForm = reactive({ title: '', file: null })
const uploadRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }]
}
const uploadRef = ref(null)

function openUpload() {
  uploadForm.title = ''
  uploadForm.file = null
  uploadOpen.value = true
}

function onFileChange(file) {
  uploadForm.file = file.raw
}

function submitUpload() {
  uploadRef.value.validate(valid => {
    if (!valid) return
    if (!uploadForm.file) {
      proxy.$modal.msgWarning('请选择文件')
      return
    }
    const fd = new FormData()
    fd.append('module', currentModule.value)
    fd.append('title', uploadForm.title)
    fd.append('file', uploadForm.file)
    uploading.value = true
    uploadKbDocument(fd).then(() => {
      proxy.$modal.msgSuccess('上传成功')
      uploadOpen.value = false
      getList()
      loadModuleStats()
    }).finally(() => { uploading.value = false })
  })
}

function handleDelete(row) {
  proxy.$modal.confirm('确认删除「' + row.title + '」？').then(() => {
    return delKbDocument(row.docId)
  }).then(() => {
    proxy.$modal.msgSuccess('删除成功')
    getList()
    loadModuleStats()
  }).catch(() => {})
}

function handleReindex(row) {
  proxy.$modal.confirm('确认为「' + row.title + '」重建向量索引？').then(() => {
    return reindexKbDocument(row.docId)
  }).then(() => {
    proxy.$modal.msgSuccess('已触发重建')
    getList()
    loadModuleStats()
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
    loadModuleStats()
  }).catch(() => {})
}

function parseTime(val) {
  return proxy.parseTime(val)
}
</script>

<style scoped>
.kb-card {
  cursor: pointer;
  margin-bottom: 16px;
  min-height: 140px;
}
.kb-card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}
.kb-card-desc {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  min-height: 44px;
}
.kb-card-action {
  margin-top: 8px;
}
.mt16 {
  margin-top: 16px;
}
.mt8 {
  margin-top: 8px;
}
.mb16 {
  margin-bottom: 16px;
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
.stat-label {
  margin-top: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
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
