<script setup lang="ts">
/**
 * AI 管理台（菜单 2942，sql/225，挂系统管理）
 *
 * P1 一页收口（施工手册 G-03/G-04）：
 *  1. 调用审计：sys_ai_call_log 分页浏览 —— 模型调用健康度在这里看，不再查库；
 *  2. 知识库问答：RAG 只科普不判定，degraded=true 时答案来自检索原文，警示必显；
 *  3. 知识库维护：录入/删除/重建索引/灌语料，ai:knowledge:manage 才可见。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAiAuditLogPage, askKnowledge,
  ingestKnowledgeDoc, getKnowledgeDocPage, getKnowledgeDocById,
  deleteKnowledgeDocById, rebuildKnowledgeIndex, seedKnowledgeCorpus,
  listDraftDiffPage,
} from '@/api/ai'
import { hasPerm } from '@/lib/perm'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'

const activeTab = ref('audit')

// ---------------- 调用审计 ----------------

const capabilityOptions = [
  { value: 'icd10', label: 'ICD-10 编码' },
  { value: 'drug_audit', label: '处方审核' },
  { value: 'emr_qc', label: '病历质控' },
  { value: 'lab_interpret', label: '检验解读' },
  { value: 'emergency_triage', label: '急诊分诊' },
  { value: 'emr_extract', label: '病历抽取' },
  { value: 'emr_draft', label: '病历草拟' },
  { value: 'patient_report_explain', label: '患者报告解读' },
  { value: 'patient_triage_normalize', label: '导诊口语归一' },
  { value: 'knowledge_qa', label: '知识库问答' },
  { value: 'operation_qa', label: '运营问数' },
  { value: 'previsit_summary', label: '预问诊摘要' },
  { value: 'followup_compose', label: '随访话术' },
  { value: 'insurance_evidence', label: '医保证据判定' },
  { value: 'health_check', label: '连通性自检' },
]

const statusOptions = [
  { value: 1, label: '成功', tag: 'success' },
  { value: 2, label: '失败', tag: 'danger' },
  { value: 3, label: '超时', tag: 'warning' },
  { value: 4, label: '降级', tag: 'warning' },
  { value: 5, label: '熔断', tag: 'danger' },
]

const statusTagOf = (status?: number) =>
  statusOptions.find(s => s.value === status)?.tag || 'info'
const statusTextOf = (row: any) =>
  row.statusText || statusOptions.find(s => s.value === row.status)?.label || row.status

const auditQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  capabilityKey: '',
  status: undefined as number | undefined,
  operator: '',
  range: [] as string[],
})
const auditLoading = ref(false)
const auditRows = ref<any[]>([])
const auditTotal = ref(0)

const loadAudit = async () => {
  auditLoading.value = true
  try {
    const res: any = await getAiAuditLogPage({
      pageNum: auditQuery.pageNum,
      pageSize: auditQuery.pageSize,
      capabilityKey: auditQuery.capabilityKey || undefined,
      status: auditQuery.status,
      operator: auditQuery.operator || undefined,
      startDate: auditQuery.range?.[0] || undefined,
      endDate: auditQuery.range?.[1] || undefined,
    })
    auditRows.value = res?.data?.records || []
    auditTotal.value = res?.data?.total || 0
  } catch (e) {
    console.error('加载 AI 调用审计失败', e)
  } finally {
    auditLoading.value = false
  }
}

const searchAudit = () => {
  auditQuery.pageNum = 1
  loadAudit()
}

const onAuditSizeChange = () => {
  auditQuery.pageNum = 1
  loadAudit()
}

// ---------------- 知识库问答 ----------------

const kQuestion = ref('')
const kLoading = ref(false)
const kResult = ref<any>(null)

const askK = async () => {
  const text = kQuestion.value.trim()
  if (!text) {
    ElMessage.warning('请输入问题')
    return
  }
  kLoading.value = true
  kResult.value = null
  try {
    const res: any = await askKnowledge({ question: text })
    kResult.value = res?.data || null
  } catch (e) {
    console.error('知识库问答失败', e)
  } finally {
    kLoading.value = false
  }
}

// ---------------- 知识库维护 ----------------

const canManage = hasPerm('ai:knowledge:manage')

const docQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  title: '',
})
const docLoading = ref(false)
const docRows = ref<any[]>([])
const docTotal = ref(0)

const sourceTypeText = (t?: number) => (t === 1 ? '内置示例' : t === 3 ? '文件导入' : '手工录入')

const loadDocs = async () => {
  if (!canManage) return
  docLoading.value = true
  try {
    const res: any = await getKnowledgeDocPage({
      pageNum: docQuery.pageNum,
      pageSize: docQuery.pageSize,
      title: docQuery.title || undefined,
    })
    docRows.value = res?.data?.records || []
    docTotal.value = res?.data?.total || 0
  } catch (e) {
    console.error('加载知识文档失败', e)
  } finally {
    docLoading.value = false
  }
}

const searchDocs = () => {
  docQuery.pageNum = 1
  loadDocs()
}

const onDocSizeChange = () => {
  docQuery.pageNum = 1
  loadDocs()
}

const ingestVisible = ref(false)
const ingestForm = reactive({
  title: '',
  category: '',
  content: '',
  sourceType: 2,
})
const ingestSubmitting = ref(false)

const openIngest = () => {
  ingestForm.title = ''
  ingestForm.category = ''
  ingestForm.content = ''
  ingestForm.sourceType = 2
  ingestVisible.value = true
}

const submitIngest = async () => {
  if (!ingestForm.title.trim() || !ingestForm.content.trim()) {
    ElMessage.warning('标题与内容不能为空')
    return
  }
  ingestSubmitting.value = true
  try {
    await ingestKnowledgeDoc({ ...ingestForm })
    ElMessage.success('已录入并建立索引')
    ingestVisible.value = false
    docQuery.pageNum = 1
    loadDocs()
  } catch (e) {
    console.error('录入知识文档失败', e)
  } finally {
    ingestSubmitting.value = false
  }
}

const detailVisible = ref(false)
const detail = ref<any>(null)

const showDetail = async (row: any) => {
  try {
    const res: any = await getKnowledgeDocById({ id: row.id })
    detail.value = res?.data || null
    detailVisible.value = true
  } catch (e) {
    console.error('加载知识文档详情失败', e)
  }
}

const removeDoc = async (row: any) => {
  await ElMessageBox.confirm(`确认删除知识文档「${row.title}」？其切块与索引将一并删除。`, '删除确认', {
    type: 'warning',
  })
  try {
    await deleteKnowledgeDocById({ id: row.id })
    ElMessage.success('已删除')
    loadDocs()
  } catch (e) {
    console.error('删除知识文档失败', e)
  }
}

const rebuildIndex = async () => {
  try {
    await rebuildKnowledgeIndex()
    ElMessage.success('向量索引已重建')
  } catch (e) {
    console.error('重建索引失败', e)
  }
}

const seedCorpus = async () => {
  try {
    const res: any = await seedKnowledgeCorpus()
    ElMessage.success(`已灌入 ${res?.data ?? 0} 篇示例语料`)
    loadDocs()
  } catch (e) {
    console.error('灌入语料失败', e)
  }
}

// ---------------- 草稿留痕（G-10） ----------------

const diffQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  patientName: '',
  doctorName: '',
  changed: undefined as number | undefined,
})
const diffLoading = ref(false)
const diffRows = ref<any[]>([])
const diffTotal = ref(0)

const loadDraftDiffs = async () => {
  diffLoading.value = true
  try {
    const res: any = await listDraftDiffPage({
      pageNum: diffQuery.pageNum,
      pageSize: diffQuery.pageSize,
      patientName: diffQuery.patientName || undefined,
      doctorName: diffQuery.doctorName || undefined,
      changed: diffQuery.changed,
    })
    diffRows.value = res?.data?.records || []
    diffTotal.value = res?.data?.total || 0
  } catch (e) {
    console.error('加载草稿留痕失败', e)
  } finally {
    diffLoading.value = false
  }
}

const searchDraftDiffs = () => {
  diffQuery.pageNum = 1
  loadDraftDiffs()
}

const onDiffSizeChange = () => {
  diffQuery.pageNum = 1
  loadDraftDiffs()
}

const diffVisible = ref(false)
const diffDetail = ref<any>(null)
const diffSegments = ref<{ type: number; text: string }[]>([])

const showDiff = (row: any) => {
  diffDetail.value = row
  diffSegments.value = []
  try {
    // diffJson 是留痕快照，坏数据只退化为展示终稿原文，不许整页报错
    const parsed = JSON.parse(row.diffJson || '[]')
    if (Array.isArray(parsed)) {
      diffSegments.value = parsed
    }
  } catch {
    /* 保持空数组 */
  }
  diffVisible.value = true
}

onMounted(() => {
  loadAudit()
  loadDocs()
  loadDraftDiffs()
})
</script>

<template>
  <div class="ai-admin">
    <div class="admin-header">
      <h2 class="admin-title">AI 管理台</h2>
      <p class="admin-sub">模型调用的审计账本与院内知识库的问答、维护入口。AI 能力白名单与开关见配置中心。</p>
    </div>

    <el-tabs v-model="activeTab">
      <!-- ============ 调用审计 ============ -->
      <el-tab-pane label="调用审计" name="audit">
        <el-card shadow="never" class="filter-card">
          <div class="filter-row">
            <el-select v-model="auditQuery.capabilityKey" placeholder="能力" clearable style="width: 180px">
              <el-option v-for="c in capabilityOptions" :key="c.value" :label="c.label" :value="c.value" />
            </el-select>
            <el-select v-model="auditQuery.status" placeholder="状态" clearable style="width: 120px">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
            <el-input
              v-model="auditQuery.operator"
              placeholder="操作人账号"
              clearable
              style="width: 160px"
              @keyup.enter="searchAudit"
            />
            <el-date-picker
              v-model="auditQuery.range"
              type="daterange"
              value-format="YYYY-MM-DD"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 260px"
            />
            <el-button type="primary" @click="searchAudit">查询</el-button>
          </div>
        </el-card>

        <el-table v-loading="auditLoading" :data="auditRows" border stripe>
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="audit-expand">
                <p><span class="expand-label">提示词版本</span>{{ row.promptVersion || '—' }}</p>
                <p><span class="expand-label">服务提供方</span>{{ row.provider || '—' }}</p>
                <p><span class="expand-label">输入摘要</span>{{ row.inputDigest || '—' }}</p>
                <p><span class="expand-label">输出摘要</span>{{ row.outputDigest || '—' }}</p>
                <p class="text-xs text-slate-400">摘要口径：标注字段明文，其余为字段指纹（SHA-256 前 12 位，可比对不可逆）</p>
                <p v-if="row.errorMsg"><span class="expand-label">错误信息</span>{{ row.errorMsg }}</p>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="发生时间" width="170">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 19) }}</template>
          </el-table-column>
          <el-table-column label="能力" min-width="140">
            <template #default="{ row }">
              {{ capabilityOptions.find(c => c.value === row.capabilityKey)?.label || row.capabilityKey }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagOf(row.status)" size="small" effect="plain">{{ statusTextOf(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="model" label="模型" min-width="150" show-overflow-tooltip />
          <el-table-column label="耗时" width="90" align="right">
            <template #default="{ row }">{{ row.latencyMs != null ? row.latencyMs + ' ms' : '—' }}</template>
          </el-table-column>
          <el-table-column label="token（入/出）" width="130" align="right">
            <template #default="{ row }">
              {{ row.promptTokens ?? '—' }} / {{ row.completionTokens ?? '—' }}
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="120" show-overflow-tooltip />
          <el-table-column label="业务" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <template v-if="row.bizType">{{ row.bizType }}<template v-if="row.bizId"> #{{ row.bizId }}</template></template>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pager"
          v-model:current-page="auditQuery.pageNum"
          v-model:page-size="auditQuery.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="auditTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="onAuditSizeChange"
          @current-change="loadAudit"
        />
      </el-tab-pane>

      <!-- ============ 草稿留痕 ============ -->
      <el-tab-pane label="草稿留痕" name="diff">
        <el-card shadow="never" class="filter-card">
          <div class="filter-row">
            <el-input
              v-model="diffQuery.patientName"
              placeholder="患者姓名"
              clearable
              style="width: 160px"
              @keyup.enter="searchDraftDiffs"
            />
            <el-input
              v-model="diffQuery.doctorName"
              placeholder="医生姓名"
              clearable
              style="width: 160px"
              @keyup.enter="searchDraftDiffs"
            />
            <el-select v-model="diffQuery.changed" placeholder="是否修改" clearable style="width: 130px">
              <el-option label="有修改" :value="1" />
              <el-option label="未修改" :value="0" />
            </el-select>
            <el-button type="primary" @click="searchDraftDiffs">查询</el-button>
          </div>
        </el-card>

        <el-table v-loading="diffLoading" :data="diffRows" border stripe>
          <el-table-column label="发生时间" width="170">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 19) }}</template>
          </el-table-column>
          <el-table-column prop="patientName" label="患者" width="110" show-overflow-tooltip />
          <el-table-column prop="deptName" label="科室" min-width="130" show-overflow-tooltip />
          <el-table-column prop="doctorName" label="医生" width="110" show-overflow-tooltip />
          <el-table-column label="是否修改" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.changed ? 'warning' : 'info'" size="small" effect="plain">
                {{ row.changed ? '有修改' : '未修改' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="draftText" label="AI 草稿" min-width="220" show-overflow-tooltip />
          <el-table-column label="操作" width="110" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click="showDiff(row)">查看留痕</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pager"
          v-model:current-page="diffQuery.pageNum"
          v-model:page-size="diffQuery.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="diffTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="onDiffSizeChange"
          @current-change="loadDraftDiffs"
        />
      </el-tab-pane>

      <!-- ============ 知识库问答 ============ -->
      <el-tab-pane label="知识库问答" name="qa">
        <el-card shadow="never">
          <div class="qa-row">
            <el-input
              v-model="kQuestion"
              placeholder="问院内制度/就诊须知/检查注意事项，例如：门诊发票怎么补打"
              maxlength="200"
              clearable
              @keyup.enter="askK()"
            />
            <el-button type="primary" :loading="kLoading" @click="askK()">提问</el-button>
          </div>

          <template v-if="kResult">
            <el-alert
              v-if="kResult.degraded"
              type="warning"
              :closable="false"
              show-icon
              class="qa-alert"
              title="本次未经过大模型，以下为检索到的知识库原文片段"
              :description="kResult.degradeReason"
            />
            <div class="qa-answer">{{ kResult.answer }}</div>
            <div v-if="kResult.sources?.length" class="qa-sources">
              <p class="qa-sources-title">引用来源（{{ kResult.sources.length }}）</p>
              <div v-for="(s, i) in kResult.sources" :key="i" class="qa-source">
                <span class="qa-source-title">{{ s.docTitle }}</span>
                <span v-if="s.category" class="qa-source-cat">{{ s.category }}</span>
                <p class="qa-source-snippet">{{ s.snippet }}</p>
              </div>
            </div>
            <p class="qa-meta">耗时 {{ kResult.latencyMs }} ms · 知识库问答只做科普解释，不做医疗判定</p>
          </template>
        </el-card>
      </el-tab-pane>

      <!-- ============ 知识库维护 ============ -->
      <el-tab-pane v-if="canManage" label="知识库维护" name="manage">
        <el-card shadow="never" class="filter-card">
          <div class="filter-row">
            <el-input
              v-model="docQuery.title"
              placeholder="按标题模糊搜索"
              clearable
              style="width: 220px"
              @keyup.enter="searchDocs"
            />
            <el-button type="primary" @click="searchDocs">查询</el-button>
            <div class="flex-1"></div>
            <el-button type="primary" plain @click="openIngest">录入文档</el-button>
            <el-button plain @click="rebuildIndex">重建向量索引</el-button>
            <el-button plain @click="seedCorpus">灌入示例语料</el-button>
          </div>
        </el-card>

        <el-table v-loading="docLoading" :data="docRows" border stripe>
          <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.category || '—' }}</template>
          </el-table-column>
          <el-table-column label="来源" width="100" align="center">
            <template #default="{ row }">{{ sourceTypeText(row.sourceType) }}</template>
          </el-table-column>
          <el-table-column prop="chunkCount" label="切块数" width="90" align="right" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small" effect="plain">
                {{ row.status === 0 ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="170">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 19) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center">
            <template #default="{ row }">
              <el-button link type="primary" size="small" @click="showDetail(row)">详情</el-button>
              <el-button link type="danger" size="small" @click="removeDoc(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pager"
          v-model:current-page="docQuery.pageNum"
          v-model:page-size="docQuery.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="docTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="onDocSizeChange"
          @current-change="loadDocs"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 录入文档 -->
    <el-dialog v-model="ingestVisible" title="录入知识文档" width="640px">
      <el-form label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="ingestForm.title" maxlength="100" placeholder="文档标题" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select
            v-model="ingestForm.category"
            placeholder="选择或输入分类"
            clearable
            style="width: 100%"
            :fit-input-width="false"
            filterable
            allow-create
            default-first-option
          >
            <el-option label="就诊须知" value="就诊须知" />
            <el-option label="科室介绍" value="科室介绍" />
            <el-option label="检查注意事项" value="检查注意事项" />
            <el-option label="药品说明书" value="药品说明书" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-radio-group v-model="ingestForm.sourceType">
            <el-radio :value="2">手工录入</el-radio>
            <el-radio :value="3">文件导入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="ingestForm.content"
            type="textarea"
            :rows="12"
            maxlength="20000"
            show-word-limit
            placeholder="文档全文；保存后自动切块并建立向量索引"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ingestVisible = false">取消</el-button>
        <el-button type="primary" :loading="ingestSubmitting" @click="submitIngest">保存</el-button>
      </template>
    </el-dialog>

    <!-- 文档详情（只读） -->
    <el-dialog v-model="detailVisible" :title="detail?.title || '文档详情'" width="640px">
      <el-form label-width="80px" disabled>
        <el-form-item label="分类">{{ detail?.category || '—' }}</el-form-item>
        <el-form-item label="来源">{{ sourceTypeText(detail?.sourceType) }} · 切块 {{ detail?.chunkCount ?? 0 }}</el-form-item>
        <el-form-item label="原文">
          <div class="doc-content">{{ detail?.content }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 草稿留痕详情（只读，红删绿增渲染用模板插值，禁止 v-html） -->
    <el-dialog
      v-model="diffVisible"
      :title="`草稿留痕 · ${diffDetail?.patientName || ''}（${diffDetail?.deptName || ''}）`"
      width="720px"
    >
      <p class="diff-meta">
        医生 {{ diffDetail?.doctorName || '—' }} · {{ (diffDetail?.createTime || '').replace('T', ' ').slice(0, 19) }}
      </p>
      <div class="diff-view">
        <template v-for="(seg, i) in diffSegments" :key="i">
          <span v-if="seg.type === 1" class="diff-del">{{ seg.text }}</span>
          <span v-else-if="seg.type === 2" class="diff-add">{{ seg.text }}</span>
          <span v-else>{{ seg.text }}</span>
        </template>
        <span v-if="!diffSegments.length">{{ diffDetail?.finalText }}</span>
      </div>
      <p class="diff-legend">
        <span class="diff-del">删除</span> / <span class="diff-add">新增</span> ——
        医生对 AI 草稿的改动；文本过长时尾部可能被截断
      </p>
      <template #footer>
        <el-button @click="diffVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.ai-admin {
  padding: 16px;
}
.admin-header {
  margin-bottom: 12px;
}
.admin-title {
  margin: 0;
  font-size: 18px;
}
.admin-sub {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}
.filter-card {
  margin-bottom: 12px;
}
.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
.flex-1 {
  flex: 1;
}
.audit-expand {
  padding: 4px 12px;
  font-size: 13px;
  color: #374151;
}
.audit-expand p {
  margin: 4px 0;
}
.expand-label {
  display: inline-block;
  width: 90px;
  color: #9ca3af;
}
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
.qa-row {
  display: flex;
  gap: 12px;
}
.qa-row .el-input {
  flex: 1;
}
.qa-alert {
  margin-top: 12px;
}
.qa-answer {
  margin-top: 12px;
  padding: 12px;
  background: #f0f7ff;
  border-left: 3px solid #1269B5;
  border-radius: 4px;
  font-size: 14px;
  white-space: pre-wrap;
}
.qa-sources {
  margin-top: 12px;
}
.qa-sources-title {
  margin: 0 0 6px;
  font-size: 13px;
  color: #6b7280;
}
.qa-source {
  padding: 8px 10px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  margin-bottom: 6px;
}
.qa-source-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-right: 8px;
}
.qa-source-cat {
  font-size: 12px;
  color: #9ca3af;
}
.qa-source-snippet {
  margin: 4px 0 0;
  font-size: 12px;
  color: #6b7280;
}
.qa-meta {
  margin: 10px 0 0;
  font-size: 12px;
  color: #9ca3af;
}
.doc-content {
  white-space: pre-wrap;
  font-size: 13px;
  max-height: 320px;
  overflow-y: auto;
}
.diff-meta {
  margin: 0 0 8px;
  font-size: 12px;
  color: #9ca3af;
}
.diff-view {
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 4px;
  font-size: 14px;
  line-height: 1.8;
  white-space: pre-wrap;
  max-height: 360px;
  overflow-y: auto;
}
.diff-del {
  color: #b91c1c;
  background: #fee2e2;
  text-decoration: line-through;
}
.diff-add {
  color: #047857;
  background: #d1fae5;
}
.diff-legend {
  margin: 8px 0 0;
  font-size: 12px;
  color: #9ca3af;
}
</style>
