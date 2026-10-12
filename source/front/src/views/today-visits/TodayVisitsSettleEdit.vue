<template>
  <!-- 日终结转（给历史遗留的号收尾） -->
  <el-dialog v-model="visible" title="日终结转（给历史遗留的号收尾）" width="520px">
    <div class="space-y-2 text-sm text-slate-600">
      <p>把还停在中间态的挂号与队列收成终态：</p>
      <ul class="ml-4 list-disc space-y-1 text-xs text-slate-500">
        <li>挂了号没到院（无队列行）→ <b>爽约</b></li>
        <li>到院签到了但没被接诊 → <b>未就诊</b>，队列行 → <b>已失效</b></li>
        <li>挂号已退号/已过号但队列还挂着候诊的 → 队列跟随挂号对齐</li>
      </ul>
      <p class="text-xs text-slate-400">
        不释放号源（过去的号不会变回可卖）；可重复执行，第二次影响 0 条。
      </p>
      <el-form class="pt-1" label-width="90px">
        <el-form-item label="就诊日">
          <el-date-picker v-model="settleDate" class="!w-64" clearable
                          placeholder="留空 = 补跑最早遗留日到昨天" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button :loading="settleLoading" @click="runSettle(true)">先试算</el-button>
      <el-button :loading="settleLoading" type="primary" @click="runSettle(false)">执行结转</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 日终结转执行弹框（原内联于 TodayVisitsView，按作业规范拆出；接口与入参逐字未动）
import {computed, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {runDayEndSettle} from '@/api/appoint';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const settleLoading = ref(false);
const settleDate = ref(null);
const runSettle = async (dryRun) => {
  settleLoading.value = true;
  try {
    const res = await runDayEndSettle({settleDate: settleDate.value || undefined, dryRun});
    ElMessage.success(res.message || '结转完成');
    if (!dryRun) {
      visible.value = false;
      emit('saved');
    }
  } catch (err) {
    ElMessage.error(err.message || '日终结转失败');
  } finally {
    settleLoading.value = false;
  }
};
</script>
