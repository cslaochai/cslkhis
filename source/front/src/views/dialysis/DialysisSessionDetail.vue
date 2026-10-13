<template>
  <!-- ============ 透析单详情（只读）============ -->
  <el-dialog v-model="visible" data-testid="hd-session-dialog" title="透析单详情" width="820px">
    <el-form v-if="sessionView" disabled label-width="110px">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="透析单号">{{ sessionView.sessionNo }}</el-form-item>
        <el-form-item label="状态">
          <el-tag :type="sessionStatusTag(sessionView.status)" size="small">
            {{ dictText('sessionStatus', sessionView.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="日期/时段">{{ sessionView.dialysisDate }} {{
            dictText('slot', sessionView.timeSlot)
          }}
        </el-form-item>
        <el-form-item label="机位">{{ sessionView.machineNo }} {{ sessionView.roomName || '' }}</el-form-item>
        <el-form-item label="患者">{{ sessionView.patientName }}（{{ sessionView.patientNo }}）</el-form-item>
        <el-form-item label="处方快照">
          {{ sessionView.dryWeight }}kg / {{ sessionView.durationMin }}min / {{ sessionView.bloodFlow }}ml·min ·
          {{ dictText('dialyzer', sessionView.dialyzer) }} · {{ dictText('anticoag', sessionView.anticoagulant) }}
        </el-form-item>
        <el-form-item label="透前体重">{{ sessionView.beforeWeight ?? '—' }} kg</el-form-item>
        <el-form-item label="透后体重">{{ sessionView.afterWeight ?? '—' }} kg</el-form-item>
        <el-form-item label="上机">{{ sessionView.onTime || '—' }} {{ sessionView.onBy || '' }}</el-form-item>
        <el-form-item label="下机">{{ sessionView.offTime || '—' }} {{ sessionView.offBy || '' }}</el-form-item>
        <el-form-item label="实际时长">{{ sessionView.actualDurationMin ?? '—' }} min</el-form-item>
        <el-form-item label="超滤量">{{ sessionView.ultraMl ?? '—' }} ml</el-form-item>
      </div>
      <el-form-item label="通路评估">{{ sessionView.accessCheck || '—' }}</el-form-item>
      <el-form-item label="不良反应">
        <span v-if="sessionView.adverseType">
          {{ dictText('adverse', sessionView.adverseType) }}：{{ sessionView.adverseDesc || '未填处置' }}
        </span>
        <span v-else>无</span>
      </el-form-item>
      <el-form-item v-if="sessionView.cancelReason" label="取消原因">{{ sessionView.cancelReason }}</el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 透析单详情弹框：只读，自己按 id 取数
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getDialysisSession} from '@/api/dialysis';
import {dictLabelText} from '@/lib/utils';
import {sessionStatusTag} from '@/lib/dialysis';

const props = defineProps({
  modelValue: Boolean,
  // 透析单 id，打开时按它回查明细
  id: {type: [Number, String], default: null},
  // 页面级参照：状态/时段/透析器/抗凝/不良反应字典由主页面加载一次后传入
  dicts: {type: Object, required: true},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictText = (key, value) => dictLabelText(props.dicts[key], value);

const sessionView = ref(null);
const loadSessionView = async () => {
  if (!props.id) return;
  try {
    const res = await getDialysisSession(props.id);
    if (res.code === 200) {
      sessionView.value = res.data;
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};

watch(visible, (v) => {
  if (v) loadSessionView();
});
</script>
