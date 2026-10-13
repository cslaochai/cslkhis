<template>
  <el-dialog v-model="visible" title="编辑知识文档" width="78%" top="6vh" append-to-body>
    <div v-loading="loading" class="doc-edit-root">
      <!-- 顶部元数据条 -->
      <div class="doc-meta-bar">
        <el-form :model="form" inline>
          <el-form-item label="标题" required>
            <el-input v-model="form.title" maxlength="100" placeholder="文档标题（必填）" style="width: 220px"/>
          </el-form-item>
          <el-form-item label="分类" required>
            <el-select
                v-model="form.category"
                :fit-input-width="false"
                allow-create
                clearable
                default-first-option
                filterable
                placeholder="分类（必填）"
                style="width: 140px"
            >
              <el-option label="就诊须知" value="就诊须知"/>
              <el-option label="科室介绍" value="科室介绍"/>
              <el-option label="检查注意事项" value="检查注意事项"/>
              <el-option label="药品说明书" value="药品说明书"/>
            </el-select>
          </el-form-item>
          <el-form-item label="版本" required>
            <el-input v-model="form.version" maxlength="50" placeholder="如 v1.0" style="width: 120px"/>
          </el-form-item>
          <el-form-item label="内容来源" required>
            <el-select
                v-model="form.sourceName"
                :fit-input-width="false"
                allow-create
                clearable
                default-first-option
                filterable
                placeholder="来源（必填）"
                style="width: 150px"
            >
              <el-option label="诊疗指南" value="诊疗指南"/>
              <el-option label="论坛" value="论坛"/>
              <el-option label="院内制度" value="院内制度"/>
              <el-option label="药品说明书" value="药品说明书"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-tag size="small" :type="form.status === 0 ? 'success' : 'info'">{{ form.status === 0 ? '正常' : '停用' }}</el-tag>
          </el-form-item>
          <el-form-item label="索引">
            <el-tag v-if="form.chunkCount > 0" size="small" type="success">{{ form.chunkCount }} 块</el-tag>
            <el-tag v-else size="small" type="info">未索引</el-tag>
          </el-form-item>
          <el-form-item v-if="form.createTime" label="创建时间">
            <span class="meta-text">{{ form.createTime }}</span>
          </el-form-item>
        </el-form>
      </div>

      <el-alert
          v-if="form.chunkCount === 0"
          :closable="false"
          class="index-hint"
          show-icon
          title="当前文档未建立索引（检索不会命中）。保存内容后旧索引会失效，请在列表点击「重建索引」生效。"
          type="info"
      />

      <!-- 正文 -->
      <div class="doc-content-panel">
        <div class="content-head">
          <span class="content-title">正文</span>
          <span class="content-count">{{ charCount }} 字</span>
          <div class="content-ops">
            <el-button size="small" :loading="previewing" @click="previewChunks">预览切块</el-button>
            <el-button size="small" :loading="historyLoading" @click="loadHistory">{{ historyVisible ? '收起历史' : `历史版本（${historyRows.length}）` }}</el-button>
          </div>
        </div>
        <el-input
            v-model="form.content"
            :rows="18"
            maxlength="20000"
            placeholder="文档全文；保存后需在列表页点击「重建索引」建立检索"
            show-word-limit
            type="textarea"
        />
      </div>

      <!-- 历史版本留痕 -->
      <div v-if="historyVisible" class="history-panel">
        <div v-if="!historyRows.length" class="history-empty">暂无历史版本（保存过新建/编辑/回退后会出现）</div>
        <div v-for="h in historyRows" :key="h.id" class="history-item">
          <div class="history-head">
            <el-tag size="small" :type="changeTypeTag(h.changeType)">{{ changeTypeText(h.changeType) }}</el-tag>
            <span class="history-time">{{ h.createTime }}</span>
            <span class="history-operator">{{ h.operatorName || '—' }}</span>
            <div class="history-ops">
              <el-button size="small" link type="primary" @click="toggleDiff(h)">{{ diffRow?.id === h.id ? '收起对比' : '对比当前' }}</el-button>
              <el-button size="small" link type="primary" @click="viewHistory(h)">查看</el-button>
              <el-button size="small" link type="warning" @click="revertHistory(h)">回退到此版本</el-button>
            </div>
          </div>
          <div v-if="diffRow?.id === h.id" class="history-diff">
            <div class="diff-tip">绿色 = 当前新增，红色 = 当前删除（相对该历史版本）</div>
            <div class="diff-body">
              <div v-for="(l, i) in diffLines(h)" :key="i" :class="['diff-line', l.type]">{{ l.type === 'del' ? '-' : l.type === 'add' ? '+' : ' ' }} {{ l.text }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 切块预览 -->
    <el-dialog v-model="previewVisible" title="切块预览" width="720px" append-to-body>
      <div v-loading="previewing">
        <el-alert
            :closable="false"
            class="mb-3"
            :description="`将按当前切块参数（单块 ${preview.chunkSize ?? '—'} 字符、重叠 ${preview.chunkOverlap ?? '—'} 字符）切成 ${preview.chunkCount ?? 0} 块，共 ${preview.totalChars ?? 0} 字符`"
            show-icon
            type="info"
        />
        <el-scrollbar height="420px">
          <div v-for="(c, i) in preview.chunks" :key="i" class="chunk-item">
            <div class="chunk-head">第 {{ i + 1 }} 块</div>
            <div class="chunk-text">{{ c }}</div>
          </div>
        </el-scrollbar>
      </div>
    </el-dialog>

    <!-- 历史版本查看 -->
    <el-dialog v-model="viewVisible" :title="`历史版本 · ${viewRow?.createTime || ''}`" width="640px" append-to-body>
      <div class="view-meta">
        <el-tag size="small" :type="changeTypeTag(viewRow?.changeType)">{{ changeTypeText(viewRow?.changeType) }}</el-tag>
        <span>标题：{{ viewRow?.title }}</span>
        <span>分类：{{ viewRow?.category || '—' }}</span>
        <span>版本：{{ viewRow?.version || '—' }}</span>
        <span>来源：{{ viewRow?.sourceName || '—' }}</span>
        <span>操作人：{{ viewRow?.operatorName || '—' }}</span>
      </div>
      <el-input :model-value="viewRow?.content || ''" :rows="14" readonly type="textarea"/>
    </el-dialog>

    <template #footer>
      <span class="footer-tip">保存只更新文本，不重建索引</span>
      <el-button :loading="saving" @click="save">保存</el-button>
      <el-button :loading="savingAndRebuilding" type="primary" @click="saveAndRebuild">保存&重索引</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 知识文档「编辑」弹框（轻量纠错版，自 AiAdminDocEdit 重构）
 *
 * 定位：纠错式编辑，不做创作型富文本。顶部元数据（标题/分类/版本/内容来源）可改，
 * 正文用大 textarea；保存走 ingest 带 id 更新（只落库并清旧向量，不建索引，由列表「重建索引」触发）。
 * 附带三个维护能力：
 * - 预览切块：按后端当前切块参数只算不存，看这篇文档会被切成几块、每块开头内容；
 * - 历史版本：每次保存（新建/编辑/回退）留一条快照，可查看、与当前做行级对比、回退；
 * - 停用/启用与删除在列表操作列，不在这里。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  getKnowledgeDocById,
  ingestKnowledgeDoc,
  listKnowledgeDocHistory,
  previewKnowledgeChunks,
  revertKnowledgeDocHistory,
  submitRebuildKnowledgeDoc,
} from '@/api/ai';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 文档ID（雪花ID，务必传字符串，避免精度丢失）
  id: {type: [String, Number], default: null},
});

const emit = defineEmits(['update:modelValue', 'saved', 'rebuilt']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const saving = ref(false);

const form = reactive({
  title: '',
  category: '',
  version: '',
  sourceName: '',
  content: '',
  sourceType: 2,
  status: 0,
  chunkCount: 0,
  createTime: '',
});

const charCount = computed(() => form.content.length);

// ---------- 打开时按 id 自取详情 + 历史列表 ----------
watch(() => props.modelValue, async (v) => {
  if (!v) {
    previewVisible.value = false;
    viewVisible.value = false;
    return;
  }
  loading.value = true;
  historyRows.value = [];
  historyVisible.value = false;
  diffRow.value = null;
  try {
    const res = await getKnowledgeDocById({id: props.id});
    const d = res?.data;
    if (!d) {
      ElMessage.warning('文档不存在');
      visible.value = false;
      return;
    }
    form.title = d.title || '';
    form.category = d.category || '';
    form.version = d.version || '';
    form.sourceName = d.sourceName || '';
    form.content = d.content || '';
    form.sourceType = d.sourceType ?? 2;
    form.status = d.status ?? 0;
    form.chunkCount = d.chunkCount ?? 0;
    form.createTime = d.createTime || '';
  } catch (e) {
    console.error('加载知识文档详情失败', e);
    ElMessage.error('加载文档失败，请重试');
    visible.value = false;
  } finally {
    loading.value = false;
  }
});

// ---------- 保存（只落库不建索引） ----------
const validate = () => {
  if (!form.title.trim()) {
    ElMessage.warning('标题不能为空');
    return false;
  }
  if (!form.category) {
    ElMessage.warning('分类不能为空');
    return false;
  }
  if (!form.version.trim()) {
    ElMessage.warning('版本不能为空');
    return false;
  }
  if (!form.sourceName) {
    ElMessage.warning('内容来源不能为空');
    return false;
  }
  if (!form.content.trim()) {
    ElMessage.warning('内容不能为空');
    return false;
  }
  return true;
};
const buildPayload = () => ({
  id: props.id,
  title: form.title.trim(),
  category: form.category,
  version: form.version,
  sourceName: form.sourceName,
  content: form.content,
  sourceType: form.sourceType,
});
const save = async () => {
  if (!validate()) return;
  saving.value = true;
  try {
    await ingestKnowledgeDoc(buildPayload());
    ElMessage.success('已保存，索引已失效，请在列表点击「重建索引」');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('保存知识文档失败', e);
  } finally {
    saving.value = false;
  }
};

// ---------- 保存并重建索引（保存后自动提交重建任务，进度由列表页轮询） ----------
const savingAndRebuilding = ref(false);
const saveAndRebuild = async () => {
  if (!validate()) return;
  savingAndRebuilding.value = true;
  try {
    await ingestKnowledgeDoc(buildPayload());
    const res = await submitRebuildKnowledgeDoc({id: props.id});
    const taskId = res?.data;
    if (!taskId) {
      ElMessage.warning('已保存，但重建任务提交失败，请到列表点击「重建索引」');
    } else {
      ElMessage.success('已保存并提交重建索引');
      // 让列表页接管该文档的进度轮询（行内显示进度条，完成后回写切块数）
      emit('rebuilt', {docId: String(props.id), taskId});
    }
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('保存并重建索引失败', e);
  } finally {
    savingAndRebuilding.value = false;
  }
};

// ---------- 切块预览 ----------
const previewVisible = ref(false);
const previewing = ref(false);
const preview = reactive({chunkCount: 0, totalChars: 0, chunks: [], chunkSize: null, chunkOverlap: null});
const previewChunks = async () => {
  if (!form.content.trim()) {
    ElMessage.warning('正文为空，无法预览');
    return;
  }
  previewing.value = true;
  try {
    const res = await previewKnowledgeChunks({content: form.content});
    const d = res?.data || {};
    preview.chunkCount = d.chunkCount ?? 0;
    preview.totalChars = d.totalChars ?? 0;
    preview.chunkSize = d.chunkSize ?? null;
    preview.chunkOverlap = d.chunkOverlap ?? null;
    preview.chunks = d.chunks || [];
    previewVisible.value = true;
  } catch (e) {
    console.error('切块预览失败', e);
    ElMessage.error(e?.message || '切块预览失败');
  } finally {
    previewing.value = false;
  }
};

// ---------- 历史版本 ----------
const historyVisible = ref(false);
const historyLoading = ref(false);
const historyRows = ref([]);
const loadHistory = async () => {
  if (historyVisible.value) {
    historyVisible.value = false;
    return;
  }
  historyVisible.value = true;
  historyLoading.value = true;
  try {
    const res = await listKnowledgeDocHistory({id: props.id});
    historyRows.value = res?.data || [];
  } catch (e) {
    console.error('加载历史版本失败', e);
    ElMessage.error('加载历史版本失败');
  } finally {
    historyLoading.value = false;
  }
};
const changeTypeText = (t) => (t === 1 ? '新建' : t === 3 ? '回退' : '编辑');
const changeTypeTag = (t) => (t === 1 ? 'info' : t === 3 ? 'warning' : 'primary');

// 查看历史内容
const viewVisible = ref(false);
const viewRow = ref(null);
const viewHistory = (h) => {
  viewRow.value = h;
  viewVisible.value = true;
};

// 与当前内容做行级对比（基于行集合的增删近似，够用于留痕核对）
const diffRow = ref(null);
const toggleDiff = (h) => {
  diffRow.value = diffRow.value?.id === h.id ? null : h;
};
const diffLines = (h) => {
  const current = form.content.split('\n').map((l) => l.trim()).filter(Boolean);
  const old = (h.content || '').split('\n').map((l) => l.trim()).filter(Boolean);
  const oldSet = new Set(old);
  const curSet = new Set(current);
  const lines = [];
  for (const l of old) {
    lines.push({type: curSet.has(l) ? 'same' : 'del', text: l});
  }
  for (const l of current) {
    if (!oldSet.has(l)) {
      lines.push({type: 'add', text: l});
    }
  }
  return lines;
};

// 回退到历史版本（快照写回当前文档并清旧向量，需重建索引）
const revertHistory = async (h) => {
  await ElMessageBox.confirm(
      `确定回退到 ${h.createTime} 的历史版本吗？当前内容将被覆盖，回退后索引失效，需在列表点击「重建索引」。`,
      '回退确认',
      {type: 'warning'},
  );
  try {
    const res = await revertKnowledgeDocHistory({id: h.id});
    ElMessage.success('已回退，索引已失效，请在列表点击「重建索引」');
    emit('saved');
    // 刷新当前表单与历史
    const detail = await getKnowledgeDocById({id: res?.data});
    const d = detail?.data;
    if (d) {
      form.title = d.title || '';
      form.category = d.category || '';
      form.version = d.version || '';
      form.sourceName = d.sourceName || '';
      form.content = d.content || '';
      form.sourceType = d.sourceType ?? 2;
      form.status = d.status ?? 0;
      form.chunkCount = d.chunkCount ?? 0;
      form.createTime = d.createTime || '';
    }
    const hist = await listKnowledgeDocHistory({id: res?.data});
    historyRows.value = hist?.data || [];
  } catch (e) {
    console.error('回退历史版本失败', e);
  }
};
</script>

<style scoped>
.doc-edit-root {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.doc-meta-bar {
  background: #fafafa;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 4px 12px;
}

.doc-meta-bar :deep(.el-form-item) {
  margin-bottom: 6px;
  margin-top: 6px;
}

.meta-text {
  font-size: 13px;
  color: #606266;
}

.index-hint {
  --el-alert-padding: 6px 12px;
}

.doc-content-panel {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.content-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.content-title {
  font-weight: 600;
  font-size: 14px;
}

.content-count {
  font-size: 12px;
  color: #909399;
}

.content-ops {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.history-panel {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 10px 12px;
  max-height: 300px;
  overflow-y: auto;
}

.history-empty {
  font-size: 13px;
  color: #909399;
  text-align: center;
  padding: 8px 0;
}

.history-item {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 8px 10px;
  margin-bottom: 8px;
}

.history-item:last-child {
  margin-bottom: 0;
}

.history-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.history-time {
  font-size: 13px;
  color: #333;
}

.history-operator {
  font-size: 12px;
  color: #909399;
}

.history-ops {
  margin-left: auto;
  display: flex;
  gap: 4px;
}

.history-diff {
  margin-top: 8px;
}

.diff-tip {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.diff-body {
  max-height: 180px;
  overflow-y: auto;
  background: #fafafa;
  border-radius: 4px;
  padding: 6px 8px;
  font-family: monospace;
  font-size: 12px;
}

.diff-line {
  white-space: pre-wrap;
  word-break: break-all;
  padding: 1px 4px;
  border-radius: 2px;
}

.diff-line.add {
  background: #e8f5e9;
  color: #2e7d32;
}

.diff-line.del {
  background: #ffebee;
  color: #c62828;
}

.chunk-item {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 4px;
  padding: 8px 10px;
  margin-bottom: 8px;
}

.chunk-head {
  font-size: 12px;
  font-weight: 600;
  color: #1269b5;
  margin-bottom: 4px;
}

.chunk-text {
  font-size: 13px;
  color: #333;
  white-space: pre-wrap;
  word-break: break-all;
}

.view-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 13px;
  color: #606266;
  margin-bottom: 10px;
}

.footer-tip {
  float: left;
  font-size: 12px;
  color: #909399;
}
</style>
