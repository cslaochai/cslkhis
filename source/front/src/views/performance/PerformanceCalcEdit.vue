<template>
  <el-dialog v-model="visible" title="执行绩效核算" width="480px">
    <div v-if="info" class="text-sm space-y-2 mb-3 p-3 rounded bg-gray-50" data-testid="perf-calc-info">
      <p>科室：{{ info.deptName }}（{{ info.costMonth }}）</p>
      <p>当月收入：<b>{{ money(info.revenue) }}</b>｜药品收入 {{ money(info.drugRevenue) }}</p>
      <p>成本：<b>{{ info.costExists ? money(info.totalCost) : '未录入（按 0 计）' }}</b></p>
      <p>结余：<b :class="surplusColor(info.surplus)">{{ money(info.surplus) }}</b></p>
    </div>
    <el-form label-width="100px">
      <el-form-item label="提成系数">
        <el-input-number v-model="bonusRate" :max="1" :min="0" :precision="4" :step="0.01"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="calcLoading" data-testid="perf-calc-submit" type="primary" @click="submitCalc">确认核算
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {perfCalc, perfRevenueInfo} from '@/api/perf';
import {money, surplusColor} from '@/lib/performance';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 科室 / 月份在页面工具栏选好再打开本弹窗，这里只读取并展示收入信息
  deptId: {type: [String, Number], default: null},
  costMonth: {type: String, default: ''},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const bonusRate = ref(0.06);
const info = ref(null);
const calcLoading = ref(false);

// 打开时自取收入信息；取不到就关掉弹窗，不让人对着空信息核算
watch(visible, async (open) => {
  if (!open) return;
  info.value = null;
  try {
    const res = await perfRevenueInfo(props.deptId, props.costMonth);
    info.value = res?.data;
  } catch (e) {
    
    visible.value = false;
  }
});

const submitCalc = async () => {
  calcLoading.value = true;
  try {
    const res = await perfCalc({
      deptId: props.deptId, costMonth: props.costMonth, bonusRate: bonusRate.value,
    });
    ElMessage.success(`核算完成：${res?.data?.deptName} ${res?.data?.costMonth} 绩效 ¥${Number(res?.data?.perfAmount || 0).toLocaleString()}`);
    visible.value = false;
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    calcLoading.value = false;
  }
};
</script>
