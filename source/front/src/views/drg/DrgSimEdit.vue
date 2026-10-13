<template>
  <el-dialog v-model="visible" :title="`DRG 模拟 - ${simForm.patientName}`" width="560px">
    <el-form label-width="110px">
      <el-form-item label="首页主诊断">
        <span>{{ simForm.mainDiagCode || '（缺失）' }} {{ simForm.mainDiagName }}</span>
      </el-form-item>
      <el-form-item v-if="!simForm.mainDiagCode" label="补录 ICD 编码" required>
        <el-input v-model="simForm.icdCode" data-testid="drg-icd-input" placeholder="如 J18.9"/>
      </el-form-item>
      <el-form-item v-if="!simForm.mainDiagCode" label="补录诊断名称">
        <el-input v-model="simForm.icdName" placeholder="如 肺炎"/>
      </el-form-item>
      <div v-if="simResult" class="mb-3 p-3 rounded bg-gray-50 text-sm" data-testid="drg-sim-result">
        <p>入组：<b>{{ simResult.drgCode }}</b> {{ simResult.drgName || '' }}
          <el-tag :type="simTag(simResult.simStatus)" class="ml-1" size="small">{{
              simText(simResult.simStatus)
            }}
          </el-tag>
        </p>
        <p v-if="simResult.simStatus === 1" class="mt-1">
          权重 {{ simResult.weight ?? '—' }}｜
          支付标准 {{ simResult.payStandard == null ? '未下发' : '¥' + simResult.payStandard }}｜实际费用
          ¥{{ simResult.actualAmount }}
          ｜<span v-if="simResult.profitAmount != null" :class="profitColor(simResult.profitAmount)">
            {{ Number(simResult.profitAmount) >= 0 ? '结余 +' : '超支 ' }}{{ simResult.profitAmount }}</span>
          <span v-else class="text-gray-400">盈亏待统筹区下发支付标准</span></p>
        <p class="mt-1 text-gray-500">{{ simResult.ruleNote }}</p>
      </div>
    </el-form>
    <template #footer>
      <el-button data-testid="drg-sim-run" type="primary" @click="runSim">运行模拟</el-button>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {drgSimulate} from '@/api/drg';
import {dictLabelText} from '@/lib/utils';
import {profitColor, simTag} from '@/lib/drg';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 病案首页行（summaryId / patientName / mainDiag* 从这里递进来）
  row: {type: Object, default: null},
  // 分组方案版本由页面工具栏选择，模拟按它跑
  version: {type: String, default: ''},
  // 入组状态字典由页面一次性加载（查询下拉也在用），这里只读取
  simDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const simText = (v) => dictLabelText(props.simDict, v);

const simForm = reactive({
  summaryId: null,
  patientName: '',
  mainDiagCode: '',
  mainDiagName: '',
  icdCode: '',
  icdName: ''
});
const simResult = ref(null);

watch(visible, (open) => {
  if (!open) return;
  const row = props.row || {};
  simForm.summaryId = row.summaryId ?? null;
  simForm.patientName = row.patientName || '';
  simForm.mainDiagCode = row.mainDiagCode || '';
  simForm.mainDiagName = row.mainDiagName || '';
  simForm.icdCode = '';
  simForm.icdName = '';
  simResult.value = null;
});

const runSim = async () => {
  const code = (simForm.mainDiagCode || simForm.icdCode || '').trim();
  if (!code)
    return ElMessage.warning('请补录主诊断 ICD 编码后再模拟');
  try {
    const res = await drgSimulate({
      summaryId: simForm.summaryId,
      icdCode: code,
      icdName: (simForm.mainDiagName || simForm.icdName || '').trim() || undefined,
      version: props.version,
    });
    const d = res?.data;
    simResult.value = d;
    ElMessage.success(d?.simStatus === 1 ? `已入组 ${d.drgCode}` : '未入组（QY）');
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
</script>
