<template>
  <div class="ai-admin">
    <el-tabs v-model="activeTab">
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

      <!-- ============ 提示词管理 ============ -->
      <el-tab-pane label="提示词管理" name="prompt">
        <el-card class="query-card mb-3" shadow="never">
          <el-form inline>
            <el-form-item label="关键词">
              <el-input v-model="promptQuery.keyword" clearable placeholder="模板名" style="width:200px" @keyup.enter="searchPromptList"/>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchPromptList">查询</el-button>
              <el-button @click="resetPromptSearch">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card class="table-card" shadow="never">
          <el-table v-loading="promptLoading" :data="promptRows" stripe>
            <el-table-column label="模板编码" prop="templateCode" min-width="180"/>
            <el-table-column label="模板名称" prop="templateName" min-width="180"/>
            <el-table-column label="当前生效版本" width="200" align="center">
              <template #default="{ row }">
                <el-tag size="small">v{{ row.version }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="修改人备注" prop="remark" min-width="200" show-overflow-tooltip/>
            <el-table-column label="更新时间" width="200">
              <template #default="{ row }">{{ formatT(row.updateTime) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="140" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openPromptDetail(row)">详细</el-button>
                <el-button link type="primary" @click.stop="openPromptEdit(row)">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="list-footer flex items-center justify-end mt-3">
            <el-pagination v-model:current-page="promptQuery.pageNum" v-model:page-size="promptQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="promptTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @current-change="loadPromptList" @size-change="searchPromptList"/>
          </div>
        </el-card>

        <!-- 详情弹框：左侧历史版本，右侧内容 -->
        <el-dialog v-model="promptDialogVisible" :title="currentPromptName" width="60%" top="8vh">
          <template #header>
            <span style="font-weight:600;">{{ currentPromptName }}
              <el-tag v-if="currentVersionRow?.isActive === 1" type="success" size="small" style="margin-left:8px;">当前生效</el-tag>
            </span>
          </template>
          <div class="prompt-layout">
            <div style="width:240px; flex-shrink:0; border-right:1px solid var(--el-border-color-lighter); padding-right:12px;">
              <div style="font-weight:600; margin-bottom:8px;">历史版本</div>
              <el-scrollbar height="450px">
                <div v-for="v in promptHistory" :key="v.id"
                     class="prompt-item" :class="{active: v.id === currentVersionRow?.id}"
                     @click="switchVersion(v)">
                  <div style="font-weight:600;">v{{ v.version }}
                    <el-tag v-if="v.isActive === 1" size="small" type="success" style="margin-left:4px;">生效</el-tag>
                  </div>
                  <div style="font-size:12px; color:#9ca3af; margin-top:2px;">{{ formatT(v.updateTime) }}</div>
                  <div v-if="v.remark" style="font-size:12px; color:#6b7280; margin-top:2px;">{{ v.remark }}</div>
                  <el-button v-if="v.isActive !== 1" size="small" type="warning" plain style="margin-top:4px;"
                             @click.stop="activateVersion(v)">切回此版本</el-button>
                </div>
              </el-scrollbar>
            </div>
            <div style="flex:1; padding-left:16px;">
              <div style="margin-bottom:10px;">v{{ currentVersionRow?.version }} 内容</div>
              <el-input v-model="currentPromptContent" type="textarea" :rows="18"
                        :readonly="!editingPrompt"
                        style="font-family: monospace; font-size:13px;"/>
              <template v-if="editingPrompt">
                <div style="margin-top:10px;">
                  <div style="font-size:13px; margin-bottom:4px;">备注（本次改了什么）</div>
                  <el-input v-model="promptRemark" type="textarea" :rows="3" placeholder="备注"/>
                </div>
                <div style="margin-top:12px; text-align:right;">
                  <el-button type="primary" :loading="promptSaving" @click="savePrompt">保存新版本</el-button>
                  <el-button @click="promptDialogVisible = false">取消</el-button>
                </div>
              </template>
            </div>
          </div>
        </el-dialog>
      </el-tab-pane>

      <!-- ============ 知识库管理 ============ -->
      <el-tab-pane v-if="canManage" label="知识库管理" name="manage">
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
            <el-table-column label="索引" min-width="110" align="center">
              <template #default="{ row }">
                <el-progress v-if="rebuildMap[row.id]?.running" :percentage="rebuildMap[row.id].progress" :stroke-width="6"
                             style="width: 84px; margin: 0 auto;"/>
                <el-tag v-else-if="!row.chunkCount" size="small" type="info">未索引</el-tag>
                <span v-else>{{ row.chunkCount }} 块</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" min-width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'info'" size="small">{{ row.status === 0 ? '正常' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="createTime" min-width="160"/>
            <el-table-column fixed="right" label="操作" width="250" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openDocEdit(row)">编辑</el-button>
                <el-button v-if="rebuildMap[row.id]?.running" disabled link type="primary">重建中 {{ rebuildMap[row.id].progress }}%</el-button>
                <el-button v-else link type="primary" @click.stop="startRebuild(row)">重建索引</el-button>
                <el-button link :type="row.status === 0 ? 'warning' : 'success'" @click.stop="toggleStatus(row)">
                  {{ row.status === 0 ? '停用' : '启用' }}
                </el-button>
                <el-button link type="danger" @click="removeDoc(row)">删除</el-button>
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
    <AiAdminDocEdit v-model="editVisible" :id="editId" @saved="onDocEditSaved" @rebuilt="onDocEditRebuilt"/>
  </div>
</template>

<script setup>
import {onBeforeUnmount, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  askKnowledge,
  deleteKnowledgeDocById,
  getKnowledgeDocPage,
  getKnowledgeRebuildProgress,
  seedKnowledgeCorpus,
  submitRebuildKnowledgeDoc,
  updateKnowledgeDocStatus,
} from '@/api/ai';
import { listPromptTemplates, listPromptHistory, upsertPromptTemplate, activatePromptTemplate } from '@/api/ai';
import {sourceTypeText} from '@/lib/ai';
import {hasPerm} from '@/lib/perm';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import AiAdminIngestAdd from './AiAdminIngestAdd.vue';
import AiAdminDocEdit from './AiAdminDocEdit.vue';

const activeTab = ref('qa');

// ---------------- 提示词管理 ----------------
const promptQuery = reactive({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, keyword: '' })
const promptRows = ref([])
const promptTotal = ref(0)
const promptLoading = ref(false)
const promptDialogVisible = ref(false)
const promptHistory = ref([])
const currentPromptName = ref('')
const currentPromptCode = ref('')
const currentVersionRow = ref(null)
const currentPromptContent = ref('')
const editingPrompt = ref(false)
const promptRemark = ref('')
const promptSaving = ref(false)
const formatT = (t) => t ? String(t).replace('T', ' ').slice(0, 19) : '—'
onMounted(async () => {
  await loadPromptList()
})
const loadPromptList = async () => {
  promptLoading.value = true
  try {
    const res = await listPromptTemplates(promptQuery)
    promptRows.value = res?.data?.records || []
    promptTotal.value = Number(res?.data?.total || 0)
  } finally {
    promptLoading.value = false
  }
}
const searchPromptList = () => {
  promptQuery.pageNum = 1
  loadPromptList()
}
const resetPromptSearch = () => {
  promptQuery.keyword = ''
  promptQuery.pageNum = 1
  loadPromptList()
}
const openPromptDetail = async (row) => {
  currentPromptName.value = row.templateName
  currentPromptCode.value = row.templateCode
  promptDialogVisible.value = true
  editingPrompt.value = false
  const res = await listPromptHistory(row.templateCode)
  promptHistory.value = res?.data || []
  // is_active=1 的已经在后端排最前
  const active = promptHistory.value.find(v => v.isActive === 1) || promptHistory.value[0]
  switchVersion(active)
}
const openPromptEdit = async (row) => {
  await openPromptDetail(row)
  editingPrompt.value = true
}
const activateVersion = async (v) => {
  await ElMessageBox.confirm(`确定切回 v${v.version} 吗？`, '版本回退', { type: 'warning' })
  await activatePromptTemplate(v.id)
  ElMessage.success('已切回 v' + v.version)
  // 刷新历史列表
  const res = await listPromptHistory(currentPromptCode.value)
  promptHistory.value = res?.data || []
  switchVersion(promptHistory.value.find(x => x.isActive === 1))
  await loadPromptList()
}
const switchVersion = (v) => {
  currentVersionRow.value = v
  currentPromptContent.value = v?.content || ''
}
const savePrompt = async () => {
  promptSaving.value = true
  try {
    const res = await upsertPromptTemplate({
      templateCode: currentPromptCode.value,      content: currentPromptContent.value,
      remark: promptRemark.value,
    })
    ElMessage.success('已保存，版本 ' + res?.data)
    promptRemark.value = ''
    editingPrompt.value = false
    promptDialogVisible.value = false
    await loadPromptList()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    promptSaving.value = false
  }
}
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
// 文档编辑弹框（已拆至 AiAdminDocEdit.vue，组件按 id 自取详情，保存后回写列表）
const editVisible = ref(false);
const editId = ref(null);
const openDocEdit = (row) => {
  editId.value = row.id;
  editVisible.value = true;
};
const onDocEditSaved = () => {
  docQuery.pageNum = 1;
  loadDocs();
};
// 编辑弹框「保存&重索引」：接管该文档的重建任务，行内显示进度并轮询
const onDocEditRebuilt = ({docId, taskId}) => {
  if (rebuildMap[docId]?.running) {
    return;
  }
  rebuildMap[docId] = {running: true, progress: 0, taskId};
  pollRebuild(docId, taskId);
};
const removeDoc = async (row) => {
  await ElMessageBox.confirm(`确认删除知识文档「${row.title}」？其切块与索引将一并删除，历史版本保留。`, '删除确认', {
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
// 单文档重建索引（异步任务 + 进度轮询；任务在服务端内存态，重启后失效需重试）
const rebuildMap = reactive({});
const rebuildPollTimers = new Map();
const REBUILD_POLL_INTERVAL = 1500;
const startRebuild = async (row) => {
  if (rebuildMap[row.id]?.running) {
    return;
  }
  try {
    const res = await submitRebuildKnowledgeDoc({id: row.id});
    const taskId = res?.data;
    if (!taskId) {
      ElMessage.warning('提交重建任务失败，请重试');
      return;
    }
    rebuildMap[row.id] = {running: true, progress: 0, taskId};
    pollRebuild(row.id, taskId);
  } catch (e) {
    console.error('提交重建索引失败', e);
    ElMessage.error(e?.message || '提交失败，请重试');
  }
};
const pollRebuild = (rowId, taskId) => {
  const timer = setTimeout(async () => {
    rebuildPollTimers.delete(rowId);
    try {
      const res = await getKnowledgeRebuildProgress({taskId});
      const t = res?.data;
      if (!t) {
        ElMessage.warning('重建任务已失效（服务可能重启），请重新点击重建索引');
        delete rebuildMap[rowId];
        return;
      }
      if (t.status === 1) {
        rebuildMap[rowId] = {running: true, progress: t.progress ?? 0, taskId};
        pollRebuild(rowId, taskId);
      } else if (t.status === 2) {
        ElMessage.success(`索引重建完成，共 ${t.chunkCount ?? 0} 块`);
        delete rebuildMap[rowId];
        loadDocs();
      } else {
        ElMessage.error(t.errorMsg || '重建索引失败');
        delete rebuildMap[rowId];
      }
    } catch (e) {
      console.error('查询重建进度失败', e);
      delete rebuildMap[rowId];
      ElMessage.error('查询重建进度失败，请重新点击重建索引');
    }
  }, REBUILD_POLL_INTERVAL);
  rebuildPollTimers.set(rowId, timer);
};
const clearRebuildPollers = () => {
  rebuildPollTimers.forEach((t) => clearTimeout(t));
  rebuildPollTimers.clear();
};
const toggleStatus = async (row) => {
  const toStop = row.status === 0;
  if (toStop) {
    await ElMessageBox.confirm(`确认停用知识文档「${row.title}」？停用后问答检索将不再命中该文档。`, '停用确认', {
      type: 'warning',
    });
  }
  try {
    await updateKnowledgeDocStatus({id: row.id, status: toStop ? 1 : 0});
    ElMessage.success(toStop ? '已停用' : '已启用');
    loadDocs();
  } catch (e) {
    console.error('更新文档状态失败', e);
  }
};
const seedCorpus = async () => {
  try {
    const res = await seedKnowledgeCorpus();
    ElMessage.success(`已灌入 ${res?.data ?? 0} 篇示例语料（已建索引）`);
    loadDocs();
  } catch (e) {
    console.error('灌入语料失败', e);
  }
};

onMounted(() => {
  loadDocs();
});
onBeforeUnmount(clearRebuildPollers);
</script>

<style scoped>
.prompt-layout { display: flex; gap: 12px; }
.prompt-item { padding: 8px 10px; cursor: pointer; border-radius: 6px; font-size: 13px; border: 1px solid var(--el-border-color-lighter); margin-bottom: 6px; }
.prompt-item:hover { background: var(--el-fill-color-light); border-color: var(--el-border-color); }
.prompt-item.active { background: var(--el-color-primary-light-8); color: var(--el-color-primary); border-color: var(--el-color-primary); }
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
