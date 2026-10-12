<template>
  <el-dialog
      v-model="visible"
      data-testid="p4-tf-reaction-dialog"
      title="输血反应上报"
      width="620px"
  >
    <div v-if="row" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <div class="text-slate-900">{{ text(row.patientName) }}（{{ text(row.applyNo) }}）</div>
        <div class="mt-1 text-slate-700">
          {{ text(row.bloodTypeText) }} · {{ text(row.bloodComponentText) }} ·
          输注 {{ fmt(row.infusionStartTime) }} ~ {{ fmt(row.infusionEndTime) }}
        </div>
      </div>
      <el-form label-width="100px">
        <el-form-item label="反应类型" required>
          <el-select
              v-model="reactionForm.reactionType"
              class="!w-full"
              data-testid="p4-tf-reaction-type"
              placeholder="从字典选择（不接受自由填写）"
          >
            <el-option v-for="t in reactionTypes" :key="t" :label="t" :value="t"/>
          </el-select>
        </el-form-item>
        <el-form-item label="反应描述" required>
          <el-input
              v-model="reactionForm.reactionDesc"
              :rows="3"
              data-testid="p4-tf-reaction-desc"
              placeholder="什么时候出现什么症状、生命体征怎么变的"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="处理措施" required>
          <el-input
              v-model="reactionForm.reactionHandle"
              :rows="3"
              data-testid="p4-tf-reaction-handle"
              placeholder="停药 / 给氧 / 用药 / 是否上报血库与医务科"
              type="textarea"
          />
        </el-form-item>
      </el-form>
      <el-alert :closable="false" show-icon type="warning">
        上报只置「有输血反应」标记并登记详情，<b>不回改历史状态</b> ——
        把已完成的输血单改回输注中就是在改历史，还会破坏"完成=已回写病历"的一致性。
      </el-alert>
    </div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button
          v-perm="'medtech:transfusion:add'"
          :loading="reactionSubmitting"
          data-testid="p4-tf-reaction-submit"
          type="primary"
          @click="submitReaction"
      >
        确认上报
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// ---------------- 六、输血反应上报 ----------------
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getTransfusionReactionTypes, reportTransfusionReaction} from '@/api/inpatientTransfusion';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const reactionTypes = ref([]);
const loadReactionTypes = async () => {
  try {
    const res = await getTransfusionReactionTypes();
    reactionTypes.value = (res.data || []);
  } catch (error) {
    console.error('加载输血反应类型失败:', error);
  }
};

const reactionSubmitting = ref(false);
const reactionForm = reactive({
  reactionType: '',
  reactionDesc: '',
  reactionHandle: '',
});

watch(visible, async (open) => {
  if (!open) return;
  reactionForm.reactionType = '';
  reactionForm.reactionDesc = '';
  reactionForm.reactionHandle = '';
  if (!reactionTypes.value.length) await loadReactionTypes();
});

const submitReaction = async () => {
  if (!props.row)
    return;
  if (!reactionForm.reactionType) {
    ElMessage.warning('请选择输血反应类型');
    return;
  }
  if (!reactionForm.reactionDesc.trim()) {
    ElMessage.warning('请填写反应描述');
    return;
  }
  if (!reactionForm.reactionHandle.trim()) {
    ElMessage.warning('请填写处理措施');
    return;
  }
  reactionSubmitting.value = true;
  try {
    await reportTransfusionReaction({
      applyId: props.row.id,
      reactionType: reactionForm.reactionType,
      reactionDesc: reactionForm.reactionDesc.trim(),
      reactionHandle: reactionForm.reactionHandle.trim(),
    });
    ElMessage.success('输血反应已上报');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '输血反应上报失败');
  } finally {
    reactionSubmitting.value = false;
  }
};
</script>
