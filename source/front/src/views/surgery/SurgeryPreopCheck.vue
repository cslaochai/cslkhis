<template>
  <!-- 术前核对 -->
  <el-dialog v-model="visible" data-testid="p4-op-check-dialog" title="术前核对（手术安全核查）" width="620px">
    <div v-if="checkTarget" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <div class="text-slate-900">{{ text(checkTarget.patientName) }}</div>
        <div class="text-slate-700">{{ text(checkTarget.plannedOperationName) }}</div>
        <div class="text-[12px] text-slate-500">
          {{ text(checkTarget.operationRoom) }} · {{ text(checkTarget.plannedTimeText) }} ·
          主刀 {{ text(checkTarget.surgeonName) }}
        </div>
      </div>
      <div class="text-[12px] text-slate-500">
        标 <span class="text-red-500">*</span> 的是必核项，缺任何一项都无法提交 ——
        术前核对的价值就在于"在切皮之前逐项确认过"。
      </div>
      <el-checkbox-group v-model="checkForm.items" class="flex flex-col gap-2">
        <el-checkbox
            v-for="i in checkItemOptions"
            :key="i.code"
            :data-testid="`p4-op-check-item-${i.code}`"
            :value="i.code"
        >
          <span class="text-slate-700">{{ i.label }}</span>
          <span v-if="i.required" class="ml-1 text-red-500">*</span>
        </el-checkbox>
      </el-checkbox-group>
      <el-form label-width="100px">
        <el-form-item label="异常说明">
          <el-input
              v-model="checkForm.preopNote"
              :rows="2"
              data-testid="p4-op-check-note"
              placeholder="正常可空；有异常必须写（如：术中需备血 4U、青霉素过敏需换用头孢）"
              type="textarea"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button
          v-perm="'ipd:surgery:edit'"
          :loading="checkSubmitting"
          data-testid="p4-op-check-submit"
          type="primary"
          @click="submitPreopCheck"
      >
        确认已完成核对
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getOperationCheckItems, preopCheckOperation} from '@/api/inpatientOperation';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前要核对的那一条手术申请
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const checkSubmitting = ref(false);
const checkItemOptions = ref([]);
const checkTarget = computed(() => props.row);
const checkForm = reactive({
  items: [],
  preopNote: '',
});
const loadCheckItems = async () => {
  try {
    const res = await getOperationCheckItems();
    checkItemOptions.value = (res.data || []);
  } catch (error) {
    console.error('加载术前核对项失败:', error);
  }
};

watch(visible, async (open) => {
  if (!open) return;
  checkForm.items = [];
  checkForm.preopNote = '';
  if (!checkItemOptions.value.length) await loadCheckItems();
});

const submitPreopCheck = async () => {
  if (!checkTarget.value)
    return;
  const missing = checkItemOptions.value
      .filter((i) => i.required && !checkForm.items.includes(i.code))
      .map((i) => i.label);
  if (missing.length > 0) {
    ElMessage.warning(`术前核对必核项未完成：${missing.join('；')}`);
    return;
  }
  checkSubmitting.value = true;
  try {
    await preopCheckOperation({
      applyId: checkTarget.value.id,
      checkItems: checkForm.items.join(','),
      preopNote: checkForm.preopNote.trim() || undefined,
    });
    ElMessage.success('术前核对已完成，可以上台并登记手术完成');
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    checkSubmitting.value = false;
  }
};
</script>
