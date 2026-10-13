<template>
  <div class="ai-admin">
    <el-tabs v-model="activeTab">
      <!-- ============ 草稿留痕 ============ -->
      <el-tab-pane label="草稿留痕" name="diff">
        <el-card class="query-card mb-3" shadow="never">
          <el-form :model="diffQuery" inline>
            <el-form-item label="患者">
              <el-input v-model="diffQuery.patientName" clearable placeholder="患者姓名" style="width: 150px" @keyup.enter="searchDraftDiffs"/>
            </el-form-item>
            <el-form-item label="医生">
              <el-input v-model="diffQuery.doctorName" clearable placeholder="医生姓名" style="width: 150px" @keyup.enter="searchDraftDiffs"/>
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="diffQuery.changed" clearable placeholder="全部" style="width: 120px">
                <el-option :value="1" label="有修改"/>
                <el-option :value="0" label="未修改"/>
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button :icon="Search" type="primary" @click="searchDraftDiffs">搜索</el-button>
              <el-button :icon="Refresh" @click="resetDiff">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card class="table-card" shadow="never">
          <el-table v-loading="diffLoading" :data="diffRows" stripe>
            <el-table-column label="发生时间" prop="createTime" min-width="160"/>
            <el-table-column label="患者" prop="patientName" min-width="100" show-overflow-tooltip/>
            <el-table-column label="科室" prop="deptName" min-width="120" show-overflow-tooltip/>
            <el-table-column label="医生" prop="doctorName" min-width="100" show-overflow-tooltip/>
            <el-table-column label="状态" min-width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.changed ? 'warning' : 'info'" size="small">{{ row.changed ? '有修改' : '未修改' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="AI 草稿" prop="draftText" min-width="200" show-overflow-tooltip/>
            <el-table-column fixed="right" label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click="showDiff(row)">查看留痕</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="list-footer flex items-center justify-end mt-3">
            <el-pagination v-model:current-page="diffQuery.pageNum" v-model:page-size="diffQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="diffTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @size-change="onDiffSizeChange" @current-change="loadDraftDiffs"/>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- ============ 知识库问答 ============ -->
      <el-tab-pane label="知识库问答" name="qa">
        <el-card shadow="never">
          <div class="flex gap-2 mb-4">
            <el-input v-model="kQuestion" clearable maxlength="200"
                      placeholder="问院内制度/就诊须知/检查注意事项" @keyup.enter="askK()"/>
            <el-button :loading="kLoading" type="primary" @click="askK()">提问</el-button>
          </div>
          <template v-if="kResult">
            <el-alert v-if="kResult.degraded" :closable="false" :description="kResult.degradeReason"
                      class="mb-3" show-icon title="未经过大模型，以下为知识库原文片段" type="warning"/>
            <div class="text-base leading-relaxed mb-4">{{ kResult.answer }}</div>
            <div v-if="kResult.sources?.length" class="border-t pt-3">
              <p class="text-sm text-gray-500 mb-2">引用来源（{{ kResult.sources.length }}）</p>
              <div v-for="(s, i) in kResult.sources" :key="i" class="border rounded p-3 mb-2">
                <div class="flex items-center gap-2 mb-1">
                  <span class="font-medium">{{ s.docTitle }}</span>
                  <el-tag v-if="s.category" size="small" effect="plain">{{ s.category }}</el-tag>
                </div>
                <p class="text-sm text-gray-600">{{ s.snippet }}</p>
              </div>
            </div>
            <p class="text-xs text-gray-400 mt-3">耗时 {{ kResult.latencyMs }} ms · 知识库问答只做科普解释，不做医疗判定</p>
          </template>
        </el-card>
      </el-tab-pane>

      <!-- ============ 知识库维护 ============ -->
      <el-tab-pane v-if="canManage" label="知识库维护" name="manage">
        <el-card class="query-card mb-3" shadow="never">
          <div class="flex items-start justify-between gap-4">
            <el-form :model="docQuery" inline>
              <el-form-item label="标题">
                <el-input v-model="docQuery.title" clearable placeholder="按标题搜索" style="width: 200px" @keyup.enter="searchDocs"/>
              </el-form-item>
              <el-form-item>
                <el-button :icon="Search" type="primary" @click="searchDocs">搜索</el-button>
                <el-button :icon="Refresh" @click="resetDoc">重置</el-button>
              </el-form-item>
            </el-form>
            <div class="flex shrink-0 items-start gap-2">
              <el-button :icon="Plus" type="primary" @click="openIngest">录入文档</el-button>
              <el-button :icon="RefreshRight" plain @click="rebuildIndex">重建索引</el-button>
              <el-button plain @click="seedCorpus">灌入示例</el-button>
            </div>
          </div>
        </el-card>

        <el-card class="table-card" shadow="never">
          <el-table v-loading="docLoading" :data="docRows" stripe>
            <el-table-column label="标题" prop="title" min-width="200" show-overflow-tooltip/>
            <el-table-column label="分类" prop="category" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ row.category || '—' }}</template>
            </el-table-column>
            <el-table-column label="内容来源" prop="sourceName" min-width="120" show-overflow-tooltip>
              <template #default="{ row }">{{ row.sourceName || '—' }}</template>
            </el-table-column>
            <el-table-column label="版本" prop="version" min-width="70" align="center"/>
            <el-table-column label="来源" min-width="90" align="center">
              <template #default="{ row }">{{ sourceTypeText(row.sourceType) }}</template>
            </el-table-column>
            <el-table-column label="切块数" prop="chunkCount" min-width="80" align="right"/>
            <el-table-column label="状态" min-width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">{{ row.status === 0 ? '正常' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" min-width="160"/>
            <el-table-column fixed="right" label="操作" width="130" align="center">
              <template #default="{ row }">
                <el-button :icon="View" link type="primary" @click="showDetail(row)">详情</el-button>
                <el-button :icon="Delete" link type="danger" @click="removeDoc(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="list-footer flex items-center justify-end mt-3">
            <el-pagination v-model:current-page="docQuery.pageNum" v-model:page-size="docQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="docTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @size-change="onDocSizeChange" @current-change="loadDocs"/>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <AiAdminIngestAdd v-model="ingestVisible" @saved="onIngestSaved"/>
    <AiAdminDocDetail v-model="detailVisible" :id="detailId"/>
    <AiAdminDiffDetail v-model="diffVisible" :row="diffRow"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  askKnowledge,
  deleteKnowledgeDocById,
  getKnowledgeDocPage,
  listDraftDiffPage,
  rebuildKnowledgeIndex,
  seedKnowledgeCorpus,
} from '@/api/ai';
import {sourceTypeText} from '@/lib/ai';
import {hasPerm} from '@/lib/perm';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import AiAdminIngestAdd from './AiAdminIngestAdd.vue';
import AiAdminDocDetail from './AiAdminDocDetail.vue';
import AiAdminDiffDetail from './AiAdminDiffDetail.vue';

const activeTab = ref('diff');
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

onMounted(() => {
  loadDocs();
  loadDraftDiffs();
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

.qa-relation-card {
  margin-top: 24px;
}

.relation-arrow {
  color: #909399;
  font-weight: 700;
}
</style>
