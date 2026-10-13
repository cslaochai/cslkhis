<template>
  <el-dialog v-model="visible" title="录入知识文档" width="880px">
    <el-form label-width="80px">
      <el-form-item label="来源">
        <el-radio-group v-model="ingestForm.sourceType">
          <el-radio :value="2">手工录入</el-radio>
          <el-radio :value="3">文件导入</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- 手工录入：粘贴全文，保存后到列表重建索引 -->
      <template v-if="ingestForm.sourceType === 2">
        <el-form-item label="标题" required>
          <el-input v-model="ingestForm.title" maxlength="100" placeholder="文档标题（必填）"/>
        </el-form-item>
        <el-form-item label="分类" required>
          <el-select
              v-model="ingestForm.category"
              :fit-input-width="false"
              allow-create
              clearable
              default-first-option
              filterable
              placeholder="分类（必填）"
              style="width: 100%"
          >
            <el-option label="就诊须知" value="就诊须知"/>
            <el-option label="科室介绍" value="科室介绍"/>
            <el-option label="检查注意事项" value="检查注意事项"/>
            <el-option label="药品说明书" value="药品说明书"/>
          </el-select>
        </el-form-item>
        <el-form-item label="版本" required>
          <el-input v-model="ingestForm.version" maxlength="50" placeholder="如 v1.0 / 2026.09（必填）"/>
        </el-form-item>
        <el-form-item label="内容来源" required>
          <el-select
              v-model="ingestForm.sourceName"
              :fit-input-width="false"
              allow-create
              clearable
              default-first-option
              filterable
              placeholder="来源（必填），如 诊疗指南 / 论坛"
              style="width: 100%"
          >
            <el-option label="诊疗指南" value="诊疗指南"/>
            <el-option label="论坛" value="论坛"/>
            <el-option label="院内制度" value="院内制度"/>
            <el-option label="药品说明书" value="药品说明书"/>
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
              v-model="ingestForm.content"
              :rows="12"
              maxlength="20000"
              placeholder="文档全文；保存后需在列表点击「重建索引」建立检索"
              show-word-limit
              type="textarea"
          />
        </el-form-item>
      </template>

      <!-- 文件导入：多文件批量 → 异步解析（进度）→ 预览确认 → 保存（不建索引） -->
      <template v-else>
        <el-alert
            :closable="false"
            class="import-step-hint"
            show-icon
            title="上传后先异步解析并预览文本，确认无误后点击「确认入库」——只保存文本，不自动切块建索引，之后到列表逐个点击「重建索引」"
            type="info"
        />
        <el-form-item label="文件" required>
          <div class="import-upload-row">
            <el-upload
                v-model:file-list="fileList"
                :auto-upload="false"
                :limit="20"
                accept=".pdf,.docx,.txt,.md,.markdown"
                drag
                multiple
                :on-exceed="handleFileExceed"
            >
              <div class="upload-hint">
                <div>将文件拖到此处，或<em>点击选择文件</em>（可多选）</div>
                <div class="upload-sub">支持 pdf / docx / txt / md，单个 ≤20MB；上传后先异步解析，确认保存（不建索引），再到列表重建索引</div>
              </div>
            </el-upload>
            <el-button :loading="importSubmitting" class="import-submit-btn" type="primary" @click="submitImport">
              上传并解析
            </el-button>
          </div>
        </el-form-item>

        <div v-if="tasks.length" class="import-tasks">
          <div v-for="task in tasks" :key="task.id" class="import-task">
            <div class="task-head">
              <span class="task-name" :title="task.fileName">{{ task.fileName }}</span>
              <el-tag :type="statusTagType(task.status)" size="small">{{ statusText(task.status) }}</el-tag>
              <span v-if="task.status === 3" class="task-progress">{{ task.progress ?? 0 }}%</span>
            </div>

            <!-- 解析中 -->
            <el-progress v-if="task.status === 1" :duration="2" :indeterminate="true" :percentage="100"/>
            <!-- 待确认：预览解析文本 + 元数据 -->
            <div v-else-if="task.status === 2" class="task-confirm">
              <div class="confirm-hint">✔ 已解析完成，请核对下方文本与元数据，确认无误后点击「确认入库」（只保存文本，不建索引）</div>
              <el-input :model-value="task.contentPreview" :rows="5" readonly type="textarea"/>
              <div class="preview-meta">
                解析文本共 {{ task.contentLength }} 字符（预览前 3000 字符，确认后全文入库）
              </div>
              <div class="task-meta">
                <el-input v-model="task.title" maxlength="100" placeholder="文档标题（必填）"/>
                <el-select
                    v-model="task.category"
                    :fit-input-width="false"
                    allow-create
                    clearable
                    default-first-option
                    filterable
                    placeholder="分类（必填）"
                >
                  <el-option label="就诊须知" value="就诊须知"/>
                  <el-option label="科室介绍" value="科室介绍"/>
                  <el-option label="检查注意事项" value="检查注意事项"/>
                  <el-option label="药品说明书" value="药品说明书"/>
                </el-select>
                <el-input v-model="task.version" maxlength="50" placeholder="版本（必填），如 v1.0"/>
                <el-select
                    v-model="task.sourceName"
                    :fit-input-width="false"
                    allow-create
                    clearable
                    default-first-option
                    filterable
                    placeholder="内容来源（必填）"
                >
                  <el-option label="诊疗指南" value="诊疗指南"/>
                  <el-option label="论坛" value="论坛"/>
                  <el-option label="院内制度" value="院内制度"/>
                  <el-option label="药品说明书" value="药品说明书"/>
                </el-select>
              </div>
              <div class="task-ops">
                <el-button size="small" type="primary" @click="commitTask(task)">确认入库</el-button>
                <el-button size="small" @click="cancelTask(task)">取消</el-button>
              </div>
            </div>
            <!-- 保存中 -->
            <el-progress v-else-if="task.status === 3" :percentage="task.progress ?? 0"/>
            <!-- 完成 -->
            <div v-else-if="task.status === 4" class="task-done">已保存（未建索引，请到列表点「重建索引」）</div>
            <!-- 失败 -->
            <div v-else-if="task.status === 5" class="task-error">
              <span>失败：{{ task.errorMsg }}</span>
              <el-button size="small" @click="removeTask(task)">移除</el-button>
            </div>
            <!-- 已取消 -->
            <div v-else-if="task.status === 6" class="task-cancelled">
              <span>已取消</span>
              <el-button size="small" @click="removeTask(task)">移除</el-button>
            </div>
          </div>
          <div class="import-summary">共 {{ tasks.length }} 个文件 · 已保存 {{ doneCount }} 个</div>
        </div>
      </template>
    </el-form>

    <template #footer>
      <el-button v-if="ingestForm.sourceType === 2" :loading="ingestSubmitting" type="primary" @click="submitIngest">
        保存
      </el-button>
      <el-button v-else-if="hasActiveImport" disabled :loading="true">保存中…</el-button>
      <el-button @click="closeDialog">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 知识文档「录入」弹框（自 AiAdminView 拆出）
 *
 * 只有新增语义（页面无修改入口，修改在 AiAdminDocEdit），故用 Add 而不是 Edit；
 * 表单 state / 校验 / 提交都留在这里。
 * - 手工录入：/ai/knowledge/ingest，只保存不建索引，之后到列表点「重建索引」。
 * - 文件导入：多文件批量 → /ai/knowledge/import/submit 异步解析 → 轮询任务进度 →
 *   解析完成预览文本、填写元数据 → /ai/knowledge/import/commit 异步保存并留历史版本（不建索引）。
 */
import {computed, onBeforeUnmount, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {
  cancelKnowledgeImport,
  commitKnowledgeImport,
  getKnowledgeImportList,
  ingestKnowledgeDoc,
  submitKnowledgeImport,
} from '@/api/ai';

const POLL_INTERVAL = 1500;

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const ingestForm = reactive({
  title: '',
  category: '',
  content: '',
  sourceType: 2,
  version: '',
  sourceName: '',
});
const ingestSubmitting = ref(false);

// 文件导入：选中文件列表（仅选择不自动上传）+ 导入任务列表 + 轮询
const fileList = ref([]);
const importSubmitting = ref(false);
const tasks = ref([]);
let pollTimer = null;
/** 是否有任务确认过入库（轮询结束后据此通知父页面刷新列表） */
let dirty = false;
/** 活跃任务连续轮询查不到的次数（后端重启后任务丢失，连续 3 次标记失败，避免无限轮询） */
const missCount = new Map();

const doneCount = computed(() => tasks.value.filter((t) => t.status === 4).length);
const hasActiveImport = computed(() => tasks.value.some((t) => t.status === 1 || t.status === 3));

const statusText = (s) => ({1: '解析中', 2: '待确认', 3: '入库中', 4: '已完成', 5: '失败', 6: '已取消'}[s] ?? s);
const statusTagType = (s) => ({1: 'info', 2: 'warning', 3: 'primary', 4: 'success', 5: 'danger', 6: 'info'}[s] ?? 'info');

// 每次打开重置表单与任务；关闭时停轮询
watch(() => props.modelValue, (v) => {
  if (!v) {
    stopPolling();
    return;
  }
  ingestForm.title = '';
  ingestForm.category = '';
  ingestForm.content = '';
  ingestForm.sourceType = 2;
  ingestForm.version = '';
  ingestForm.sourceName = '';
  fileList.value = [];
  tasks.value = [];
  dirty = false;
});
// 注意：stopPolling 定义在下方，必须用箭头函数延迟引用，直接传引用会因 const TDZ 在 setup 阶段报错
onBeforeUnmount(() => stopPolling());

const handleFileExceed = () => {
  ElMessage.warning('一次最多选择 20 个文件');
};

// 批量上传：创建导入任务并异步解析
const submitImport = async () => {
  const raws = fileList.value.map((f) => f.raw).filter(Boolean);
  if (!raws.length) {
    ElMessage.warning('请选择要上传的文件');
    return;
  }
  importSubmitting.value = true;
  try {
    const fd = new FormData();
    raws.forEach((f) => fd.append('files', f));
    const res = await submitKnowledgeImport(fd);
    const list = res?.data ?? [];
    if (!list.length) {
      ElMessage.warning('没有可解析的文件');
      return;
    }
    tasks.value = list.map((t) => ({...t}));
    startPolling();
  } catch (e) {
    console.error('批量上传失败', e);
    ElMessage.error('上传失败，请重试');
  } finally {
    importSubmitting.value = false;
  }
};

// 轮询：解析中/入库中的任务持续拉取状态，全部终态后停止
const startPolling = () => {
  stopPolling();
  pollTimer = setInterval(async () => {
    const activeIds = tasks.value
        .filter((t) => t.status === 1 || t.status === 3)
        .map((t) => t.id);
    if (!activeIds.length) {
      stopPolling();
      notifyIfChanged();
      return;
    }
    try {
      const res = await getKnowledgeImportList({ids: activeIds});
      const returned = res?.data ?? [];
      const returnedIds = new Set(returned.map((t) => t.id));
      const map = new Map(returned.map((t) => [t.id, t]));
      let anyActive = false;
      tasks.value = tasks.value.map((t) => {
        if (t.status === 1 || t.status === 3) {
          if (returnedIds.has(t.id)) {
            missCount.delete(t.id);
            anyActive = true;
            return {...t, ...map.get(t.id)};
          }
          // 活跃任务不在返回里：可能服务重启导致任务丢失
          const cnt = (missCount.get(t.id) ?? 0) + 1;
          missCount.set(t.id, cnt);
          if (cnt >= 3) {
            return {...t, status: 5, errorMsg: '任务已失效（服务可能重启），请重新上传'};
          }
          anyActive = true;
          return t;
        }
        return map.get(t.id) ? {...t, ...map.get(t.id)} : t;
      });
      if (!anyActive) {
        stopPolling();
        notifyIfChanged();
      }
    } catch (e) {
      // 轮询失败下轮重试
    }
  }, POLL_INTERVAL);
};
const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer);
    pollTimer = null;
  }
};
// 有任务入库完成时通知父页面刷新列表
const notifyIfChanged = () => {
  if (dirty) {
    dirty = false;
    emit('saved');
  }
};

// 确认入库：预览后补充元数据，异步保存并留历史版本（不建索引）
const commitTask = async (task) => {
  if (!task.title?.trim()) {
    ElMessage.warning('标题不能为空');
    return;
  }
  if (!task.category) {
    ElMessage.warning('分类不能为空');
    return;
  }
  if (!task.version?.trim()) {
    ElMessage.warning('版本不能为空');
    return;
  }
  if (!task.sourceName) {
    ElMessage.warning('内容来源不能为空');
    return;
  }
  try {
    const res = await commitKnowledgeImport({
      id: task.id,
      title: task.title.trim(),
      category: task.category,
      version: task.version,
      sourceName: task.sourceName,
    });
    Object.assign(task, res?.data ?? {});
    dirty = true;
    startPolling(); // 进入入库中，继续轮询进度
  } catch (e) {
    console.error('确认入库失败', e);
  }
};
const cancelTask = async (task) => {
  try {
    await cancelKnowledgeImport({id: task.id});
    task.status = 6;
  } catch (e) {
    console.error('取消导入任务失败', e);
  }
};
const removeTask = (task) => {
  tasks.value = tasks.value.filter((t) => t.id !== task.id);
};

const submitIngest = async () => {
  if (!ingestForm.title.trim()) {
    ElMessage.warning('标题不能为空');
    return;
  }
  if (!ingestForm.category) {
    ElMessage.warning('分类不能为空');
    return;
  }
  if (!ingestForm.version.trim()) {
    ElMessage.warning('版本不能为空');
    return;
  }
  if (!ingestForm.sourceName) {
    ElMessage.warning('内容来源不能为空');
    return;
  }
  if (!ingestForm.content.trim()) {
    ElMessage.warning('内容不能为空');
    return;
  }
  ingestSubmitting.value = true;
  try {
    await ingestKnowledgeDoc({...ingestForm});
    ElMessage.success('已保存（未建索引），请在列表点击「重建索引」');
    visible.value = false;
    emit('saved');
  } catch (e) {
    console.error('录入知识文档失败', e);
  } finally {
    ingestSubmitting.value = false;
  }
};

const closeDialog = () => {
  stopPolling();
  visible.value = false;
};
</script>

<style scoped>
.upload-hint {
  text-align: center;
}

.upload-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.import-step-hint {
  margin-bottom: 12px;
}

.import-upload-row {
  width: 100%;
}

.import-submit-btn {
  margin-top: 10px;
  width: 100%;
}

.import-tasks {
  margin-top: 4px;
  max-height: 420px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.import-task {
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 10px 12px;
}

.task-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.task-name {
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 380px;
}

.task-progress {
  margin-left: auto;
  font-size: 12px;
  color: #1269b5;
}

.confirm-hint {
  margin-bottom: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #e6a23c;
}

.preview-meta {
  margin: 4px 0 8px;
  font-size: 12px;
  color: #909399;
}

.task-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-bottom: 8px;
}

.task-ops {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.task-done {
  color: #67c23a;
  font-size: 13px;
}

.task-error {
  color: #f56c6c;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.task-cancelled {
  color: #909399;
  font-size: 13px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.import-summary {
  text-align: right;
  font-size: 12px;
  color: #909399;
}
</style>
