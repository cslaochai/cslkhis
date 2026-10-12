<template>
  <div class="ai-admin">
    <el-tabs v-model="activeTab">
      <!-- ============ 调用审计 ============ -->
      <el-tab-pane label="调用审计" name="audit">
        <el-card class="filter-card" shadow="never">
          <div class="filter-row">
            <el-select v-model="auditQuery.capabilityKey" clearable placeholder="能力" style="width: 180px">
              <el-option v-for="c in capabilityOptions" :key="c.value" :label="c.label" :value="c.value"/>
            </el-select>
            <el-select v-model="auditQuery.status" clearable placeholder="状态" style="width: 120px">
              <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value"/>
            </el-select>
            <el-input
                v-model="auditQuery.operator"
                clearable
                placeholder="操作人账号"
                style="width: 160px"
                @keyup.enter="searchAudit"
            />
            <el-date-picker
                v-model="auditQuery.range"
                end-placeholder="结束日期"
                range-separator="至"
                start-placeholder="开始日期"
                style="width: 100px"
                type="daterange"
                value-format="YYYY-MM-DD"
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
                <p class="text-xs text-slate-400">摘要口径：标注字段明文，其余为字段指纹（SHA-256 前 12
                  位，可比对不可逆）</p>
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
          <el-table-column align="center" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusTagOf(row.status)" effect="plain" size="small">{{ statusTextOf(row) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="模型" min-width="150" prop="model" show-overflow-tooltip/>
          <el-table-column align="right" label="耗时" width="90">
            <template #default="{ row }">{{ row.latencyMs != null ? row.latencyMs + ' ms' : '—' }}</template>
          </el-table-column>
          <el-table-column align="right" label="token（入/出）" width="130">
            <template #default="{ row }">
              {{ row.promptTokens ?? '—' }} / {{ row.completionTokens ?? '—' }}
            </template>
          </el-table-column>
          <el-table-column label="操作人" prop="operator" show-overflow-tooltip width="120"/>
          <el-table-column label="业务" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              <template v-if="row.bizType">{{ row.bizType }}
                <template v-if="row.bizId"> #{{ row.bizId }}</template>
              </template>
              <span v-else>—</span>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            v-model:current-page="auditQuery.pageNum"
            v-model:page-size="auditQuery.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="auditTotal"
            class="pager"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="onAuditSizeChange"
            @current-change="loadAudit"
        />
      </el-tab-pane>

      <!-- ============ 草稿留痕 ============ -->
      <el-tab-pane label="草稿留痕" name="diff">
        <el-card class="filter-card" shadow="never">
          <div class="filter-row">
            <el-input
                v-model="diffQuery.patientName"
                clearable
                placeholder="患者姓名"
                style="width: 160px"
                @keyup.enter="searchDraftDiffs"
            />
            <el-input
                v-model="diffQuery.doctorName"
                clearable
                placeholder="医生姓名"
                style="width: 160px"
                @keyup.enter="searchDraftDiffs"
            />
            <el-select v-model="diffQuery.changed" clearable placeholder="是否修改" style="width: 130px">
              <el-option :value="1" label="有修改"/>
              <el-option :value="0" label="未修改"/>
            </el-select>
            <el-button type="primary" @click="searchDraftDiffs">查询</el-button>
          </div>
        </el-card>

        <el-table v-loading="diffLoading" :data="diffRows" border stripe>
          <el-table-column label="发生时间" width="170">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 19) }}</template>
          </el-table-column>
          <el-table-column label="患者" prop="patientName" show-overflow-tooltip width="110"/>
          <el-table-column label="科室" min-width="130" prop="deptName" show-overflow-tooltip/>
          <el-table-column label="医生" prop="doctorName" show-overflow-tooltip width="110"/>
          <el-table-column align="center" label="是否修改" width="100">
            <template #default="{ row }">
              <el-tag :type="row.changed ? 'warning' : 'info'" effect="plain" size="small">
                {{ row.changed ? '有修改' : '未修改' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="AI 草稿" min-width="220" prop="draftText" show-overflow-tooltip/>
          <el-table-column align="center" label="操作" width="110">
            <template #default="{ row }">
              <el-button link type="primary" @click="showDiff(row)">查看留痕</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            v-model:current-page="diffQuery.pageNum"
            v-model:page-size="diffQuery.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="diffTotal"
            class="pager"
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
                clearable
                maxlength="200"
                placeholder="问院内制度/就诊须知/检查注意事项，例如：门诊发票怎么补打"
                @keyup.enter="askK()"
            />
            <el-button :loading="kLoading" type="primary" @click="askK()">提问</el-button>
          </div>

          <template v-if="kResult">
            <el-alert
                v-if="kResult.degraded"
                :closable="false"
                :description="kResult.degradeReason"
                class="qa-alert"
                show-icon
                title="本次未经过大模型，以下为检索到的知识库原文片段"
                type="warning"
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
        <el-card class="filter-card" shadow="never">
          <div class="filter-row">
            <el-input
                v-model="docQuery.title"
                clearable
                placeholder="按标题模糊搜索"
                style="width: 220px"
                @keyup.enter="searchDocs"
            />
            <el-button type="primary" @click="searchDocs">查询</el-button>
            <div class="flex-1"></div>
            <el-button plain type="primary" @click="openIngest">录入文档</el-button>
            <el-button plain @click="rebuildIndex">重建向量索引</el-button>
            <el-button plain @click="seedCorpus">灌入示例语料</el-button>
          </div>
        </el-card>

        <el-table v-loading="docLoading" :data="docRows" border stripe>
          <el-table-column label="标题" min-width="220" prop="title" show-overflow-tooltip/>
          <el-table-column label="分类" prop="category" show-overflow-tooltip width="140">
            <template #default="{ row }">{{ row.category || '—' }}</template>
          </el-table-column>
          <el-table-column label="内容来源" prop="sourceName" show-overflow-tooltip width="130">
            <template #default="{ row }">{{ row.sourceName || '—' }}</template>
          </el-table-column>
          <el-table-column align="center" label="版本" prop="version" width="90">
            <template #default="{ row }">{{ row.version || '—' }}</template>
          </el-table-column>
          <el-table-column align="center" label="来源" width="100">
            <template #default="{ row }">{{ sourceTypeText(row.sourceType) }}</template>
          </el-table-column>
          <el-table-column align="right" label="切块数" prop="chunkCount" width="90"/>
          <el-table-column align="center" label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="row.status === 0 ? 'success' : 'info'" effect="plain" size="small">
                {{ row.status === 0 ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="170">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 19) }}</template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="140">
            <template #default="{ row }">
              <el-button link size="small" type="primary" @click="showDetail(row)">详情</el-button>
              <el-button link size="small" type="danger" @click="removeDoc(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            v-model:current-page="docQuery.pageNum"
            v-model:page-size="docQuery.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="docTotal"
            class="pager"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="onDocSizeChange"
            @current-change="loadDocs"
        />
      </el-tab-pane>

      <!-- ============ 问数配置 ============ -->
      <el-tab-pane v-if="canConfig" label="问数配置" name="qaConfig">
        <el-alert
            :closable="false"
            class="qa-config-hint"
            show-icon
            title="运营问数按需加载：先按语义配置命中相关表，命中不足时模型兜底选表，生成时只把相关表结构与命中口径喂给模型。此页维护表/列/语义映射，保存后即时生效。"
            type="info"
        />

        <!-- 数据表配置（白名单） -->
        <el-card class="filter-card" shadow="never">
          <div class="filter-row">
            <el-input
                v-model="schemaQuery.tableName"
                clearable
                placeholder="表名模糊搜索"
                style="width: 220px"
                @keyup.enter="searchSchemas"
            />
            <el-select v-model="schemaQuery.isEnabled" clearable placeholder="是否启用" style="width: 130px">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="停用"/>
            </el-select>
            <el-button type="primary" @click="searchSchemas">查询</el-button>
            <div class="flex-1"></div>
            <el-button plain type="primary" @click="openSchemaEdit()">新增数据表</el-button>
            <el-button plain type="warning" @click="rewriteSchema">重写刷新</el-button>
          </div>
        </el-card>

        <el-table v-loading="schemaLoading" :data="schemaRows" border stripe>
          <el-table-column label="表名" prop="tableName" width="190"/>
          <el-table-column label="用途说明" prop="tableDesc" min-width="240" show-overflow-tooltip/>
          <el-table-column align="center" label="启用列" prop="enabledColumnCount" width="80"/>
          <el-table-column align="center" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.isEnabled ? 'success' : 'info'" effect="plain" size="small">
                {{ row.isEnabled ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="排序" prop="sortOrder" width="70"/>
          <el-table-column label="创建时间" width="170">
            <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 19) }}</template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="200">
            <template #default="{ row }">
              <el-button link size="small" type="primary" @click.stop="openColumnManage(row)">管理列</el-button>
              <el-button link size="small" type="primary" @click.stop="openSchemaEdit(row)">编辑</el-button>
              <el-button link size="small" type="danger" @click.stop="removeSchema(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            v-model:current-page="schemaQuery.pageNum"
            v-model:page-size="schemaQuery.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="schemaTotal"
            class="pager"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="onSchemaSizeChange"
            @current-change="loadSchemas"
        />

        <!-- 语义映射配置（业务口径） -->
        <el-card class="filter-card qa-semantic-card" shadow="never">
          <div class="filter-row">
            <el-input
                v-model="semanticQuery.term"
                clearable
                placeholder="语义词模糊搜索"
                style="width: 200px"
                @keyup.enter="searchSemantics"
            />
            <el-button type="primary" @click="searchSemantics">查询</el-button>
            <div class="flex-1"></div>
            <el-button plain type="primary" @click="openSemanticEdit()">新增语义映射</el-button>
          </div>
        </el-card>

        <el-table v-loading="semanticLoading" :data="semanticRows" border stripe>
          <el-table-column type="expand">
            <template #default="{ row }">
              <div class="semantic-expand">
                <p><span class="expand-label">口径说明</span>{{ row.scopeDesc }}</p>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="语义词" prop="term" width="140"/>
          <el-table-column label="命中表" prop="tableName" width="200"/>
          <el-table-column label="目标列" width="120">
            <template #default="{ row }">{{ row.columnName || '（表级）' }}</template>
          </el-table-column>
          <el-table-column align="center" label="匹配方式" width="100">
            <template #default="{ row }">{{ row.matchMode === 2 ? '精确' : '包含' }}</template>
          </el-table-column>
          <el-table-column align="center" label="优先级" prop="priority" width="80"/>
          <el-table-column label="口径说明" min-width="220" prop="scopeDesc" show-overflow-tooltip/>
          <el-table-column align="center" label="启用" width="80">
            <template #default="{ row }">
              <el-tag :type="row.isEnabled ? 'success' : 'warning'" effect="plain" size="small">
                {{ row.isEnabled ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="操作" width="130">
            <template #default="{ row }">
              <el-button link size="small" type="primary" @click.stop="openSemanticEdit(row)">编辑</el-button>
              <el-button link size="small" type="danger" @click.stop="removeSemantic(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            v-model:current-page="semanticQuery.pageNum"
            v-model:page-size="semanticQuery.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="semanticTotal"
            class="pager"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="onSemanticSizeChange"
            @current-change="loadSemantics"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 录入文档 -->
    <AiAdminIngestAdd v-model="ingestVisible" @saved="onIngestSaved"/>

    <!-- 文档详情（只读） -->
    <AiAdminDocDetail v-model="detailVisible" :id="detailId"/>

    <!-- 草稿留痕详情（只读，红删绿增渲染用模板插值，禁止 v-html） -->
    <AiAdminDiffDetail v-model="diffVisible" :row="diffRow"/>

    <!-- 问数配置弹框（schema/列/语义，新增与修改均走独立组件） -->
    <AiAdminQaSchemaEdit v-model="schemaEditVisible" :row="schemaEditRow" @saved="onSchemaSaved"/>
    <AiAdminQaColumnManage v-model="columnManageVisible" :table-name="columnManageTable"/>
    <AiAdminQaSemanticEdit v-model="semanticEditVisible" :row="semanticEditRow" @saved="onSemanticSaved"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  askKnowledge,
  deleteKnowledgeDocById,
  deleteOperationQaSchema,
  deleteOperationQaSemantic,
  getAiAuditLogPage,
  getKnowledgeDocPage,
  getOperationQaSchemaPage,
  getOperationQaSemanticPage,
  listDraftDiffPage,
  rebuildKnowledgeIndex,
  rewriteOperationQaSchema,
  seedKnowledgeCorpus,
} from '@/api/ai';
import {sourceTypeText} from '@/lib/ai';
import {hasPerm} from '@/lib/perm';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import AiAdminIngestAdd from './AiAdminIngestAdd.vue';
import AiAdminDocDetail from './AiAdminDocDetail.vue';
import AiAdminDiffDetail from './AiAdminDiffDetail.vue';
import AiAdminQaSchemaEdit from './AiAdminQaSchemaEdit.vue';
import AiAdminQaColumnManage from './AiAdminQaColumnManage.vue';
import AiAdminQaSemanticEdit from './AiAdminQaSemanticEdit.vue';

const activeTab = ref('audit');
const capabilityOptions = [
  {value: 'icd10', label: 'ICD-10 编码'},
  {value: 'drug_audit', label: '处方审核'},
  {value: 'emr_qc', label: '病历质控'},
  {value: 'lab_interpret', label: '检验解读'},
  {value: 'emergency_triage', label: '急诊分诊'},
  {value: 'emr_extract', label: '病历抽取'},
  {value: 'emr_draft', label: '病历草拟'},
  {value: 'patient_report_explain', label: '患者报告解读'},
  {value: 'patient_triage_normalize', label: '导诊口语归一'},
  {value: 'knowledge_qa', label: '知识库问答'},
  {value: 'operation_qa', label: '运营问数'},
  {value: 'previsit_summary', label: '预问诊摘要'},
  {value: 'followup_compose', label: '随访话术'},
  {value: 'insurance_evidence', label: '医保证据判定'},
  {value: 'health_check', label: '连通性自检'},
];
const statusOptions = [
  {value: 1, label: '成功', tag: 'success'},
  {value: 2, label: '失败', tag: 'danger'},
  {value: 3, label: '超时', tag: 'warning'},
  {value: 4, label: '降级', tag: 'warning'},
  {value: 5, label: '熔断', tag: 'danger'},
];
const statusTagOf = (status) => statusOptions.find(s => s.value === status)?.tag || 'info';
const statusTextOf = (row) => row.statusText || statusOptions.find(s => s.value === row.status)?.label || row.status;
const auditQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  capabilityKey: '',
  status: undefined,
  operator: '',
  range: [],
});
const auditLoading = ref(false);
const auditRows = ref([]);
const auditTotal = ref(0);
const loadAudit = async () => {
  auditLoading.value = true;
  try {
    const res = await getAiAuditLogPage({
      pageNum: auditQuery.pageNum,
      pageSize: auditQuery.pageSize,
      capabilityKey: auditQuery.capabilityKey || undefined,
      status: auditQuery.status,
      operator: auditQuery.operator || undefined,
      startDate: auditQuery.range?.[0] || undefined,
      endDate: auditQuery.range?.[1] || undefined,
    });
    auditRows.value = res?.data?.records || [];
    auditTotal.value = res?.data?.total || 0;
  } catch (e) {
    console.error('加载 AI 调用审计失败', e);
  } finally {
    auditLoading.value = false;
  }
};
const searchAudit = () => {
  auditQuery.pageNum = 1;
  loadAudit();
};
const onAuditSizeChange = () => {
  auditQuery.pageNum = 1;
  loadAudit();
};
// ---------------- 知识库问答 ----------------
const kQuestion = ref('');
const kLoading = ref(false);
const kResult = ref(null);
const askK = async () => {
  const text = kQuestion.value.trim();
  if (!text) {
    ElMessage.warning('请输入问题');
    return;
  }
  kLoading.value = true;
  kResult.value = null;
  try {
    const res = await askKnowledge({question: text});
    kResult.value = res?.data || null;
  } catch (e) {
    console.error('知识库问答失败', e);
  } finally {
    kLoading.value = false;
  }
};
// ---------------- 知识库维护 ----------------
const canManage = hasPerm('ai:knowledge:manage');
const docQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  title: '',
});
const docLoading = ref(false);
const docRows = ref([]);
const docTotal = ref(0);
const loadDocs = async () => {
  if (!canManage)
    return;
  docLoading.value = true;
  try {
    const res = await getKnowledgeDocPage({
      pageNum: docQuery.pageNum,
      pageSize: docQuery.pageSize,
      title: docQuery.title || undefined,
    });
    docRows.value = res?.data?.records || [];
    docTotal.value = res?.data?.total || 0;
  } catch (e) {
    console.error('加载知识文档失败', e);
  } finally {
    docLoading.value = false;
  }
};
const searchDocs = () => {
  docQuery.pageNum = 1;
  loadDocs();
};
const onDocSizeChange = () => {
  docQuery.pageNum = 1;
  loadDocs();
};
// 录入弹框（表单与提交已拆至 AiAdminIngestAdd.vue，这里只管开关与刷新）
const ingestVisible = ref(false);
const openIngest = () => {
  ingestVisible.value = true;
};
const onIngestSaved = () => {
  docQuery.pageNum = 1;
  loadDocs();
};
// 文档详情弹框（已拆至 AiAdminDocDetail.vue，组件按 id 自取详情）
const detailVisible = ref(false);
const detailId = ref(null);
const showDetail = (row) => {
  detailId.value = row.id;
  detailVisible.value = true;
};
const removeDoc = async (row) => {
  await ElMessageBox.confirm(`确认删除知识文档「${row.title}」？其切块与索引将一并删除。`, '删除确认', {
    type: 'warning',
  });
  try {
    await deleteKnowledgeDocById({id: row.id});
    ElMessage.success('已删除');
    loadDocs();
  } catch (e) {
    console.error('删除知识文档失败', e);
  }
};
const rebuildIndex = async () => {
  try {
    await rebuildKnowledgeIndex();
    ElMessage.success('向量索引已重建');
  } catch (e) {
    console.error('重建索引失败', e);
  }
};
const seedCorpus = async () => {
  try {
    const res = await seedKnowledgeCorpus();
    ElMessage.success(`已灌入 ${res?.data ?? 0} 篇示例语料`);
    loadDocs();
  } catch (e) {
    console.error('灌入语料失败', e);
  }
};
// ---------------- 草稿留痕（G-10） ----------------
const diffQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  patientName: '',
  doctorName: '',
  changed: undefined,
});
const diffLoading = ref(false);
const diffRows = ref([]);
const diffTotal = ref(0);
const loadDraftDiffs = async () => {
  diffLoading.value = true;
  try {
    const res = await listDraftDiffPage({
      pageNum: diffQuery.pageNum,
      pageSize: diffQuery.pageSize,
      patientName: diffQuery.patientName || undefined,
      doctorName: diffQuery.doctorName || undefined,
      changed: diffQuery.changed,
    });
    diffRows.value = res?.data?.records || [];
    diffTotal.value = res?.data?.total || 0;
  } catch (e) {
    console.error('加载草稿留痕失败', e);
  } finally {
    diffLoading.value = false;
  }
};
const searchDraftDiffs = () => {
  diffQuery.pageNum = 1;
  loadDraftDiffs();
};
const onDiffSizeChange = () => {
  diffQuery.pageNum = 1;
  loadDraftDiffs();
};
// 草稿留痕弹框（已拆至 AiAdminDiffDetail.vue，组件按行快照解析 diffJson）
const diffVisible = ref(false);
const diffRow = ref(null);
const showDiff = (row) => {
  diffRow.value = row;
  diffVisible.value = true;
};
// ---------------- 运营问数配置（ai:operationQa:config） ----------------
const canConfig = hasPerm('ai:operationQa:config');
// 数据表配置
const schemaQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  tableName: '',
  isEnabled: undefined,
});
const schemaLoading = ref(false);
const schemaRows = ref([]);
const schemaTotal = ref(0);
const loadSchemas = async () => {
  if (!canConfig)
    return;
  schemaLoading.value = true;
  try {
    const res = await getOperationQaSchemaPage({
      pageNum: schemaQuery.pageNum,
      pageSize: schemaQuery.pageSize,
      tableName: schemaQuery.tableName || undefined,
      isEnabled: schemaQuery.isEnabled,
    });
    schemaRows.value = res?.data?.records || [];
    schemaTotal.value = res?.data?.total || 0;
  } catch (e) {
    console.error('加载问数数据表配置失败', e);
  } finally {
    schemaLoading.value = false;
  }
};
const searchSchemas = () => {
  schemaQuery.pageNum = 1;
  loadSchemas();
};
const onSchemaSizeChange = () => {
  schemaQuery.pageNum = 1;
  loadSchemas();
};
// 数据表新增/编辑弹框（逻辑在 AiAdminQaSchemaEdit.vue）
const schemaEditVisible = ref(false);
const schemaEditRow = ref(null);
const openSchemaEdit = (row) => {
  schemaEditRow.value = row || null;
  schemaEditVisible.value = true;
};
const onSchemaSaved = () => {
  schemaQuery.pageNum = 1;
  loadSchemas();
};
// 列管理弹框（AiAdminQaColumnManage.vue 容器，内嵌列列表 + 列编辑）
const columnManageVisible = ref(false);
const columnManageTable = ref('');
const openColumnManage = (row) => {
  columnManageTable.value = row.tableName;
  columnManageVisible.value = true;
};
const removeSchema = async (row) => {
  await ElMessageBox.confirm(`确认删除数据表「${row.tableName}」？其列配置将一并删除，模型将不再允许查询该表。`, '删除确认', {
    type: 'warning',
  });
  try {
    await deleteOperationQaSchema(row.id);
    ElMessage.success('已删除');
    loadSchemas();
  } catch (e) {
    console.error('删除数据表配置失败', e);
  }
};
// 重写刷新：扫描当前库全表结构，一次性覆盖表/列配置（表结构同步唯一入口）
const rewriteSchema = async () => {
  await ElMessageBox.confirm(
    '重写刷新将扫描当前库全部表结构并覆盖现有表/列配置：新表默认停用（不进白名单）、新列默认启用、注释与类型对齐真实 DDL、库中已删除的表/列会从配置移除。保留既有启用状态与用途说明。确定执行？',
    '重写刷新确认', { type: 'warning' });
  try {
    const res = await rewriteOperationQaSchema();
    ElMessage.success(res?.data || '重写刷新完成');
    loadSchemas();
  } catch (e) {
    console.error('重写刷新失败', e);
  }
};
// 语义映射配置
const semanticQuery = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  term: '',
});
const semanticLoading = ref(false);
const semanticRows = ref([]);
const semanticTotal = ref(0);
const loadSemantics = async () => {
  if (!canConfig)
    return;
  semanticLoading.value = true;
  try {
    const res = await getOperationQaSemanticPage({
      pageNum: semanticQuery.pageNum,
      pageSize: semanticQuery.pageSize,
      term: semanticQuery.term || undefined,
    });
    semanticRows.value = res?.data?.records || [];
    semanticTotal.value = res?.data?.total || 0;
  } catch (e) {
    console.error('加载问数语义映射失败', e);
  } finally {
    semanticLoading.value = false;
  }
};
const searchSemantics = () => {
  semanticQuery.pageNum = 1;
  loadSemantics();
};
const onSemanticSizeChange = () => {
  semanticQuery.pageNum = 1;
  loadSemantics();
};
// 语义新增/编辑弹框（逻辑在 AiAdminQaSemanticEdit.vue）
const semanticEditVisible = ref(false);
const semanticEditRow = ref(null);
const openSemanticEdit = (row) => {
  semanticEditRow.value = row || null;
  semanticEditVisible.value = true;
};
const onSemanticSaved = () => {
  semanticQuery.pageNum = 1;
  loadSemantics();
};
const removeSemantic = async (row) => {
  await ElMessageBox.confirm(`确认删除语义映射「${row.term}」？`, '删除确认', {type: 'warning'});
  try {
    await deleteOperationQaSemantic(row.id);
    ElMessage.success('已删除');
    loadSemantics();
  } catch (e) {
    console.error('删除语义映射失败', e);
  }
};
onMounted(() => {
  loadAudit();
  loadDocs();
  loadDraftDiffs();
  loadSchemas();
  loadSemantics();
});
</script>

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

.qa-config-hint {
  margin-bottom: 12px;
}

.qa-semantic-card {
  margin-top: 24px;
}

.semantic-expand {
  padding: 4px 12px;
  font-size: 13px;
  color: #374151;
}

.semantic-expand p {
  margin: 4px 0;
}
</style>
