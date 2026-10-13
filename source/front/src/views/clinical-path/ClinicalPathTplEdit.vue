<template>
  <!-- 模板编辑弹窗（草稿） -->
  <el-dialog v-model="visible" :title="tplForm.id ? '编辑路径模板（草稿）' : '新增路径模板'"
             data-testid="cp-tpl-edit-dialog" width="1000px">
    <div class="space-y-4">
      <el-form class="grid grid-cols-2 gap-x-4" label-width="90px">
        <el-form-item label="路径编码" required>
          <el-input v-model="tplForm.pathwayCode" :disabled="!!tplForm.id" data-testid="cp-tpl-code"
                    placeholder="如 LP-GG-001"/>
        </el-form-item>
        <el-form-item label="路径名称" required>
          <el-input v-model="tplForm.pathwayName" data-testid="cp-tpl-name" placeholder="如 人工全髋关节置换术路径"/>
        </el-form-item>
        <el-form-item label="适用科室">
          <el-select v-model="tplForm.deptId" :fit-input-width="false" class="w-full" clearable filterable
                     placeholder="科室（可不选）">
            <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="Number(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="tplForm.version" placeholder="V1"/>
        </el-form-item>
        <el-form-item class="col-span-2" label="适用病种">
          <el-input v-model="tplForm.diagnosis" placeholder="诊断口径描述，入径时医生判断"/>
        </el-form-item>
      </el-form>

      <div class="flex items-center justify-between">
        <h4 class="font-semibold text-slate-800">路径步骤（路径日 × 项目，文书不生成医嘱）</h4>
        <el-button :icon="Plus" data-testid="cp-step-add-btn" size="small" @click="addStep">加步骤</el-button>
      </div>
      <el-table :data="tplForm.steps" border data-testid="cp-step-table" max-height="360" size="small">
        <el-table-column label="路径日" width="120">
          <template #default="{ row }">
            <el-input-number v-model="row.dayNo" :max="60" :min="1" class="w-24" controls-position="right"
                             size="small"/>
          </template>
        </el-table-column>
        <el-table-column label="项目类型" width="150">
          <template #default="{ row }">
            <el-select v-model="row.itemType" :fit-input-width="false" class="w-full" size="small">
              <el-option v-for="d in itemTypeDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="项目名称" width="240">
          <template #default="{ row }">
            <el-select v-model="row.itemName" :fit-input-width="false" :loading="itemNameLoading" :remote-method="searchItemNames" allow-create class="w-full"
                       data-testid="cp-step-name-select"
                       default-first-option filterable placeholder="搜索字典或直接输入"
                       remote
                       size="small" @change="(v: string) => onItemNameChange(row, v)">
              <el-option v-for="o in itemNameOptions" :key="o.itemName" :label="o.itemName" :value="o.itemName"/>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="路径要求" min-width="220">
          <template #default="{ row }">
            <el-input v-model="row.content" placeholder="该路径日对本项目的要求" size="small"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="操作" width="70">
          <template #default="{ $index }">
            <el-button :icon="Delete" link size="small" type="danger" @click="delStep($index)"/>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-3 text-sm text-slate-400">点「加步骤」维护路径日项目</div>
        </template>
      </el-table>
    </div>
    <template #footer>
      <el-button :loading="tplSaving" data-testid="cp-tpl-save-btn" type="primary" @click="saveTpl">保存草稿
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 路径模板编辑（草稿）：新增与编辑共用同一个 pathwayUpsert（row 为空 = 新增，带 id = 编辑），
 * 步骤清单、项目编码带出、科室候选都在这张表单里，所以科室列表由组件首次打开时自取。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Delete, Plus} from '@element-plus/icons-vue';
import {getPathwayDetail, pathwayUpsert} from '@/api/pathway';
import {getDepartmentSelectList} from '@/api/system';
import {getTreatmentItemSelectList} from '@/api/treatment';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  // 项目类型字典与详情弹框同一份，由页面加载一次传下来
  itemTypeDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const tplSaving = ref(false);
const tplForm = reactive({
  id: null,
  pathwayCode: '',
  pathwayName: '',
  deptId: null,
  diagnosis: '',
  version: 'V1',
  remark: '',
  steps: [],
});

const depts = ref([]);
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({pageSize: 200});
    depts.value = res?.data?.records || res?.data || [];
  } catch (e) {
    console.error('加载科室失败', e);
  }
};

const resetForm = () => {
  tplForm.id = null;
  tplForm.pathwayCode = '';
  tplForm.pathwayName = '';
  tplForm.deptId = null;
  tplForm.diagnosis = '';
  tplForm.version = 'V1';
  tplForm.remark = '';
  tplForm.steps = [{dayNo: 1, itemType: 1, itemName: '', content: '', sortNo: 1}];
};

const loadDetail = async () => {
  const res = await getPathwayDetail(props.row.id);
  if (res.code === 200 && res.data) {
    const d = res.data;
    tplForm.id = d.id;
    tplForm.pathwayCode = d.pathwayCode;
    tplForm.pathwayName = d.pathwayName;
    tplForm.deptId = d.deptId ? Number(d.deptId) : null;
    tplForm.diagnosis = d.diagnosis || '';
    tplForm.version = d.version || 'V1';
    tplForm.remark = d.remark || '';
    tplForm.steps = (d.steps || []).map((s) => ({...s}));
  } else {
    ElMessage.error(res.message || '加载模板失败');
  }
};

watch(visible, async (open) => {
  if (!open) return;
  resetForm();
  if (!depts.value.length)
    await loadDepts();
  if (!props.row?.id)
    return;
  try {
    await loadDetail();
  } catch (e) {
    console.error(e);
    ElMessage.error('加载模板失败');
  }
});

const addStep = () => {
  const last = tplForm.steps[tplForm.steps.length - 1];
  tplForm.steps.push({
    dayNo: last?.dayNo || 1,
    itemType: last?.itemType || 1,
    itemName: '',
    content: '',
    sortNo: tplForm.steps.length + 1,
  });
};
const delStep = (idx) => {
  tplForm.steps.splice(idx, 1);
};
// 项目名称软带出：治疗项目字典远程搜索，允许自由文本（filterable + allow-create）
// 选中字典项时同步 row.itemCode（医生站偏离比对只认带编码的步骤）；自由文本编码置空
const itemNameOptions = ref([]);
const itemNameLoading = ref(false);
const searchItemNames = async (kw) => {
  itemNameLoading.value = true;
  try {
    const res = await getTreatmentItemSelectList({keyword: kw || undefined, limit: 20});
    itemNameOptions.value = (res?.data || []).map((x) => ({itemName: x.itemName, itemCode: x.itemCode}));
  } catch (e) {
    console.error(e);
    itemNameOptions.value = [];
  } finally {
    itemNameLoading.value = false;
  }
};
const onItemNameChange = (row, val) => {
  row.itemCode = itemNameOptions.value.find(o => o.itemName === val)?.itemCode ?? null;
};
const saveTpl = async () => {
  if (!tplForm.pathwayCode.trim() || !tplForm.pathwayName.trim()) {
    ElMessage.warning('路径编码与名称必填');
    return;
  }
  for (const s of tplForm.steps) {
    if (!s.dayNo || !s.itemType || !String(s.itemName || '').trim()) {
      ElMessage.warning('步骤的路径日/项目类型/项目名称必填');
      return;
    }
  }
  tplSaving.value = true;
  try {
    const res = await pathwayUpsert({
      id: tplForm.id || undefined,
      pathwayCode: tplForm.pathwayCode.trim(),
      pathwayName: tplForm.pathwayName.trim(),
      deptId: tplForm.deptId ?? undefined,
      diagnosis: tplForm.diagnosis.trim() || undefined,
      version: tplForm.version.trim() || undefined,
      remark: tplForm.remark.trim() || undefined,
      steps: tplForm.steps.map((s, i) => ({
        dayNo: Number(s.dayNo),
        itemType: Number(s.itemType),
        itemName: String(s.itemName).trim(),
        itemCode: String(s.itemCode || '').trim() || undefined,
        content: String(s.content || '').trim() || undefined,
        sortNo: i + 1,
      })),
    });
    if (res.code === 200) {
      ElMessage.success('模板已保存（草稿）');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    tplSaving.value = false;
  }
};
</script>

<style scoped>
.w-24 {
  width: 96px;
}
</style>
