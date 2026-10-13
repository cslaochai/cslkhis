<template>
  <el-dialog v-model="visible" :title="`心电采集 - ${collectDetail?.patientName || ''} ${collectDetail?.itemName || ''}`" destroy-on-close
             width="860px">
    <template v-if="collectDetail">
      <div class="space-y-3">
        <div class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
          <span class="text-slate-400">患者：</span>{{ collectDetail.patientName }}
          {{ patientGenderText(collectDetail.gender) }} {{ collectDetail.age }}岁
          <span class="mx-2 text-slate-300">|</span>
          <span class="text-slate-400">记录号：</span>{{ collectDetail.recordNo }}
          <span class="mx-2 text-slate-300">|</span>
          <span class="text-slate-400">状态：</span>{{ collectDetail.recordStatusText }}
          <span class="mx-2 text-slate-300">|</span>
          <span class="text-slate-400">临床诊断：</span>{{ collectDetail.clinicalDiagnosis || '—' }}
        </div>

        <div v-if="collectDetail.recordStatus === 1" class="rounded-lg border border-amber-200 bg-amber-50 p-3">
          <span class="text-sm text-amber-700">该检查还是「已登记」，需要先签到再采集。</span>
          <el-button v-perm="'medtech:ecg:collect'" :loading="collectSubmitting" class="ml-3" size="small"
                     type="warning" @click="doCheckIn">签到
          </el-button>
        </div>

        <div v-else class="flex flex-wrap items-center gap-3">
          <span class="text-sm text-slate-600">演示/联调采集（真实设备走 POST /medicaltech/ecg/collectWave）</span>
          <el-select v-model="rhythmCode" :disabled="collectDetail.recordStatus >= 5" class="!w-40">
            <el-option v-for="o in RHYTHM_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
          <el-button v-perm="'medtech:ecg:collect'" :disabled="collectDetail.recordStatus >= 5" :loading="collectSubmitting"
                     type="primary" @click="doSimulate">
            {{ collectDetail.waveId ? '重新采集（覆盖）' : '模拟采集 12 导联' }}
          </el-button>
          <span v-if="collectDetail.recordStatus >= 5" class="text-xs text-slate-400">
            报告已进入审核/发布流程，波形锁定不能重采
          </span>
        </div>

        <EcgWavePanel :wave-data="collectDetail.waveData" title="12 导联心电图"/>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {ecgCheckIn, ecgSimulateWave, getEcgDetailByRecordId} from '@/api/medicaltech';
import {patientGenderText} from '@/lib/patientGender';
import EcgWavePanel from '@/components/his/EcgWavePanel.vue';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 列表行数据（按 row.recordId 自取详情）
  row: {type: Object, default: null},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

// ========== 签到 / 波形采集（技师岗） ==========
const RHYTHM_OPTIONS = [
  {value: 1, label: '窦性心律'},
  {value: 2, label: '窦性心动过速'},
  {value: 3, label: '窦性心动过缓'},
  {value: 4, label: '心房颤动'},
  {value: 5, label: '室性早搏'},
];
const collectRow = ref(null);
const collectDetail = ref(null);
const collectSubmitting = ref(false);
const rhythmCode = ref(1);

watch(visible, (v) => {
  if (v) openCollect();
});

const openCollect = async () => {
  collectRow.value = props.row;
  rhythmCode.value = 1;
  collectDetail.value = null;
  try {
    const res = await getEcgDetailByRecordId(props.row?.recordId);
    collectDetail.value = res.data;
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
const doCheckIn = async () => {
  collectSubmitting.value = true;
  try {
    const res = await ecgCheckIn(collectRow.value?.recordId);
    collectDetail.value = res.data;
    collectRow.value = {...collectRow.value, recordStatus: 2, recordStatusText: '已签到'};
    ElMessage.success('已签到');
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    collectSubmitting.value = false;
  }
};
const doSimulate = async () => {
  collectSubmitting.value = true;
  try {
    const res = await ecgSimulateWave({recordId: collectRow.value?.recordId, rhythmCode: rhythmCode.value});
    collectDetail.value = res.data;
    ElMessage.success('波形已入库（该检查已出结果）');
    emit('saved');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    collectSubmitting.value = false;
  }
};
</script>
