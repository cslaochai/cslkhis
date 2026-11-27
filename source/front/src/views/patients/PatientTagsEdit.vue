<template>
  <el-dialog v-model="visible" destroy-on-close title="患者标签" width="640px">
    <div class="mb-4 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3">
      <div class="flex flex-wrap items-center gap-2">
        <span class="font-medium text-slate-800">{{ patient?.patientName || '' }}</span>
        <span class="font-mono text-sm text-slate-400">{{ patient?.patientNo }}</span>
      <span class="text-sm text-slate-500">
        {{ patientGenderText(patient?.gender) }} · {{ patientAgeText(patient?.age) }}
      </span>
      </div>
    </div>

    <h4 class="mb-2 flex items-center gap-1.5 text-sm font-semibold text-slate-700">
      <span class="h-3.5 w-1 rounded bg-blue-500"></span>已挂标签
    </h4>
    <div v-loading="loading" class="min-h-16 rounded-lg border border-slate-200 p-3">
      <div v-if="currentTags.length" class="flex flex-wrap gap-2">
        <span
            v-for="tag in currentTags"
            :key="tag.tagId"
            :style="{ backgroundColor: tag.tagColor || '#409EFF' }"
            class="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-white"
        >
          {{ tag.tagName }}
          <el-icon v-if="canDelete" class="cursor-pointer" title="移除该标签" @click="handleRemove(tag)">
            <Close/>
          </el-icon>
        </span>
      </div>
      <div v-else class="py-4 text-center text-sm text-slate-400">
        该患者暂无标签
      </div>
    </div>

    <h4 class="mt-4 mb-2 flex items-center gap-1.5 text-sm font-semibold text-slate-700">
      <span class="h-3.5 w-1 rounded bg-blue-500"></span>添加标签
    </h4>
    <div class="flex items-start gap-2">
      <el-select v-model="selectedTagId" :fit-input-width="false" class="flex-1" clearable filterable
                 placeholder="选择要添加的标签">
        <el-option v-for="tag in addableTags" :key="tag.tagId" :label="tag.tagName" :value="tag.tagId"/>
      </el-select>
      <el-button v-if="canAdd" :disabled="!selectedTagId" :loading="submitting" type="primary"
                 @click="handleAdd">添加
      </el-button>
    </div>
    <p v-if="addableTags.length === 0" class="mt-2 text-xs text-slate-400">
      没有可添加的标签（全部已挂上，或标签字典为空）
    </p>

    <template #footer>
      <el-button type="primary" @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Close} from '@element-plus/icons-vue';
import {addPatientTag, getPatientTags, removePatientTag} from '@/api/patient';
import {getPatientTagList} from '@/api/system';
import {hasPermission, loadPermissions} from '@/lib/permission';
import {patientAgeText, patientGenderText} from '@/lib/patientGender';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 当前患者行（至少要有 id / patientName）
  patient: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'changed']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const submitting = ref(false);
const currentTags = ref([]);
const allTags = ref([]);
const selectedTagId = ref(null);
// 有写入才回传 changed，父页面据此决定要不要重拉列表
const dirty = ref(false);
// 按钮级权限：没有对应码就不渲染入口（后端 @PreAuthorize 同码兜底）
const canAdd = computed(() => hasPermission('patient:tag:add'));
const canDelete = computed(() => hasPermission('patient:tag:delete'));

const addableTags = computed(() => {
  const mounted = new Set(currentTags.value.map(t => String(t.tagId)));
  return allTags.value.filter(t => !mounted.has(String(t.tagId)));
});

const load = async () => {
  if (!props.patient?.id) return;
  loading.value = true;
  dirty.value = false;
  selectedTagId.value = null;
  await loadPermissions();
  try {
    const [tagsRes, allRes] = await Promise.all([
      getPatientTags({patientId: props.patient.id}).catch(() => null),
      getPatientTagList({pageNum: 1, pageSize: 200}).catch(() => null),
    ]);
    currentTags.value = tagsRes?.data || [];
    allTags.value = allRes?.data?.records || allRes?.data || [];
  } finally {
    loading.value = false;
  }
};

watch(
    () => props.modelValue,
    (v) => {
      if (v) load();
      else if (dirty.value) emit('changed');
    }
);

const handleAdd = async () => {
  if (!selectedTagId.value) return;
  submitting.value = true;
  try {
    // sourceType=1 手动打标（2 保留给系统按年龄自动打标）
    await addPatientTag({patientId: props.patient.id, tagId: selectedTagId.value, sourceType: 1});
    ElMessage.success('标签已添加');
    dirty.value = true;
    selectedTagId.value = null;
    const res = await getPatientTags({patientId: props.patient.id});
    currentTags.value = res?.data || [];
  } catch (error)  /* 错误已由全局拦截器统一提示 */ {

  } finally {
    submitting.value = false;
  }
};

const handleRemove = async (tag) => {
  try {
    await removePatientTag({patientId: props.patient.id, tagId: tag.tagId});
    ElMessage.success('标签已移除');
    dirty.value = true;
    currentTags.value = currentTags.value.filter(t => String(t.tagId) !== String(tag.tagId));
  } catch (error)  /* 错误已由全局拦截器统一提示 */ {

  }
};
</script>
