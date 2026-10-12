<template>
  <!-- ============ 入科详情（只读） ============ -->
  <el-dialog v-model="visible" data-testid="icu-stay-dialog" title="入科记录详情" width="720px">
    <el-form v-if="stayDetail" :model="stayDetail" disabled label-width="100px">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="入科单号">
          <el-input :model-value="stayDetail.stayNo"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="stayStatusTag(stayDetail.status)" size="small">{{
              dictText('stayStatus', stayDetail.status)
            }}
          </el-tag>
        </el-form-item>
        <el-form-item label="患者">
          <el-input :model-value="`${stayDetail.patientName}（${stayDetail.patientNo}）`"/>
        </el-form-item>
        <el-form-item label="来源科室">
          <el-input :model-value="stayDetail.fromDeptName || '—'"/>
        </el-form-item>
        <el-form-item label="ICU 床位">
          <el-input :model-value="`${stayDetail.wardName || ''} ${stayDetail.bedNo}`"/>
        </el-form-item>
        <el-form-item label="监护等级">
          <el-input :model-value="dictText('careLevel', stayDetail.careLevel)"/>
        </el-form-item>
        <el-form-item label="入科时间">
          <el-input :model-value="stayDetail.inTime"/>
        </el-form-item>
        <el-form-item label="入科登记人">
          <el-input :model-value="stayDetail.inBy || '—'"/>
        </el-form-item>
        <el-form-item label="入科 GCS">
          <el-input :model-value="String(stayDetail.inGcs ?? '—')"/>
        </el-form-item>
        <el-form-item label="滞留时长">
          <el-input :model-value="`${stayDetail.stayHours ?? 0} 小时`"/>
        </el-form-item>
        <el-form-item class="col-span-2" label="入科诊断">
          <el-input :model-value="stayDetail.inDiag || '—'"/>
        </el-form-item>
        <el-form-item label="出科时间">
          <el-input :model-value="stayDetail.outTime || '—'"/>
        </el-form-item>
        <el-form-item label="转出去向">
          <el-input :model-value="stayDetail.outDest ? dictText('outDest', stayDetail.outDest) : '—'"/>
        </el-form-item>
        <el-form-item label="出科 GCS">
          <el-input :model-value="String(stayDetail.outGcs ?? '—')"/>
        </el-form-item>
        <el-form-item label="出科登记人">
          <el-input :model-value="stayDetail.outBy || '—'"/>
        </el-form-item>
        <el-form-item class="col-span-2" label="转归说明">
          <el-input :model-value="stayDetail.outReason || '—'"/>
        </el-form-item>
        <el-form-item label="监护记录">
          <el-input :model-value="`${stayDetail.monitorCount ?? 0} 条 · 最近 ${stayDetail.lastMonitorTime || '无'}`"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input :model-value="stayDetail.remark || '—'"/>
        </el-form-item>
      </div>
    </el-form>
    <div v-if="stayDetail" class="mb-1 text-sm font-medium text-slate-700" data-testid="icu-stay-monitors">
      监护记录（{{ stayMonitors.length }} 条）
    </div>
    <el-table v-if="stayDetail" :data="stayMonitors" border max-height="240" size="small">
      <el-table-column label="时刻" prop="recordTime" width="160"/>
      <el-table-column label="T" prop="temperature" width="70"/>
      <el-table-column label="P" prop="pulse" width="60"/>
      <el-table-column label="BP" width="95">
        <template #default="{ row }">{{ bpText(row) }}</template>
      </el-table-column>
      <el-table-column label="SpO2" prop="spo2" width="70"/>
      <el-table-column label="GCS" prop="gcsTotal" width="70"/>
      <el-table-column label="平衡" prop="fluidBalance" width="90"/>
      <el-table-column label="处置" min-width="160" prop="handling" show-overflow-tooltip/>
      <el-table-column label="记录人" prop="recorderName" width="100"/>
    </el-table>
    <template #footer>
      <div class="flex justify-end gap-2">
        <el-button type="primary" @click="visible = false">关闭</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getIcuMonitorTrend, getIcuStay} from '@/api/icu';
import {dictLabelText} from '@/lib/utils';
import {bpText, stayStatusTag} from '@/lib/icu';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 入科记录 id，打开时自取数
  id: {type: [String, Number], default: null},
  // 列表也在用的字典由页面加载一次传下来
  dicts: {type: Object, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictText = (key, value) => dictLabelText(props.dicts[key], value);

const stayDetail = ref(null);
const stayMonitors = ref([]);
const load = async () => {
  try {
    const res = await getIcuStay(props.id);
    if (res.code === 200 && res.data) {
      stayDetail.value = res.data;
      const trend = await getIcuMonitorTrend(props.id, undefined);
      stayMonitors.value = trend?.code === 200 ? (trend.data || []) : [];
    } else {
      ElMessage.error(res.message || '查询入科记录失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '查询入科记录失败');
  }
};
watch(() => props.modelValue, (v) => {
  if (v && props.id) load();
});
</script>
