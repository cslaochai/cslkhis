<template>
  <el-dialog v-model="visible" data-testid="p4-tf-detail-dialog" title="用血单详情" width="920px">
    <div v-if="detail" v-loading="loading" class="space-y-4">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="用血单号">{{ text(detail.applyNo) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          {{ text(detail.transfusionStatusText) }} / 配血：{{ text(detail.crossmatchStatusText) }}
        </el-descriptions-item>
        <el-descriptions-item label="患者">
          {{ text(detail.patientName) }}（{{ text(detail.admissionNo) }}）
        </el-descriptions-item>
        <el-descriptions-item label="床号 / 科室">
          {{ text(detail.applyBedNo) }}床 / {{ text(detail.applyDeptName) }}
        </el-descriptions-item>
        <el-descriptions-item label="受血者血型">
          {{ text(detail.bloodTypeText) }}
          <span v-if="detail.aboMatchesArchive === false" class="ml-2 text-[11px] text-amber-600">
            （与档案不一致，以本次为准）
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="血液品种">
          {{ text(detail.bloodComponentText) }}
          <span v-if="detail.componentSpec" class="text-slate-400">（{{ detail.componentSpec }}）</span>
        </el-descriptions-item>
        <el-descriptions-item label="申请袋数 / 总量">
          {{ text(detail.bagCount) }} 袋 /
          {{ detail.plannedAmount ? detail.plannedAmount + (detail.amountUnit || '') : '—' }}
          <span class="ml-2 text-slate-400">{{ text(detail.bagProgressText) }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="紧急用血">{{ text(detail.isEmergencyText) }}</el-descriptions-item>
        <el-descriptions-item label="申请医生 / 时间">
          {{ text(detail.applyDoctorName) }} / {{ fmt(detail.applyTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="输血目的">{{ text(detail.transfusionPurpose) }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="输血指征">{{ text(detail.indication) }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="输血前指标">
          Hb {{ text(detail.preHb) }} g/L · HCT {{ text(detail.preHct) }}% · PLT {{ text(detail.prePlt) }}×10⁹/L
        </el-descriptions-item>
        <el-descriptions-item label="既往输血史">{{ text(detail.transfusionHistory) }}</el-descriptions-item>
        <el-descriptions-item label="既往反应史">{{ text(detail.reactionHistory) }}</el-descriptions-item>
        <el-descriptions-item label="妊娠史">{{ text(detail.pregnancyHistory) }}</el-descriptions-item>
        <el-descriptions-item label="配血人 / 时间">
          {{ text(detail.crossmatchDoctorName) }} / {{ fmt(detail.crossmatchTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="发血人 / 时间">
          {{ text(detail.issueDoctorName) }} / {{ fmt(detail.issueTime) }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="双人核对">
          <span v-if="!detail.checkNurseName" class="text-slate-400">尚未核对</span>
          <span v-else>
            {{ detail.checkNurseName }} / {{ text(detail.checkNurse2Name) }}
            <span class="text-slate-400">于 {{ fmt(detail.checkTime) }}</span>
            <div class="text-[11px] text-slate-500">{{ text(detail.checkItemsText) }}</div>
          </span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.checkNote" :span="2" label="核对异常说明">
          {{ detail.checkNote }}
        </el-descriptions-item>
        <el-descriptions-item label="输注时间 / 时长">
          {{ fmt(detail.infusionStartTime) }} ~ {{ fmt(detail.infusionEndTime) }}
          <span v-if="detail.durationText" class="text-slate-400">（{{ detail.durationText }}）</span>
        </el-descriptions-item>
        <el-descriptions-item label="实际输注量">
          {{ text(detail.actualAmount) }}{{ detail.amountUnit || '' }}
          <span v-if="detail.infusionSpeed" class="text-slate-400">· {{ detail.infusionSpeed }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.observation" :span="2" label="输注观察">
          {{ detail.observation }}
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.efficacyEval" :span="2" label="疗效评估">
          {{ detail.efficacyEval }}
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="输血后复查">
          Hb {{ text(detail.postHb) }} g/L · HCT {{ text(detail.postHct) }}% · PLT {{ text(detail.postPlt) }}×10⁹/L
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="输血反应">
          <span v-if="detail.hasReaction === 1" class="font-medium text-red-600">
            {{ text(detail.reactionType) }}：{{ text(detail.reactionDesc) }}；处理：{{ text(detail.reactionHandle) }}
            <div class="text-[11px] text-slate-500">
              由 {{ text(detail.reactionReporterName) }} 于 {{ fmt(detail.reactionTime) }} 上报
            </div>
          </span>
          <span v-else class="text-slate-400">{{ text(detail.hasReactionText) }}</span>
        </el-descriptions-item>
        <el-descriptions-item :span="2" label="回写病历号">
          <span v-if="detail.recordNo" class="text-slate-700">{{ detail.recordNo }}（输血记录）</span>
          <span v-else class="text-slate-400">尚未回写（输血未完成）</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.cancelReason" :span="2" label="取消原因">
          {{ detail.cancelReason }}（{{ text(detail.cancelDoctorName) }} {{ fmt(detail.cancelTime) }}）
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.remark" :span="2" label="备注">{{ detail.remark }}</el-descriptions-item>
      </el-descriptions>

      <div>
        <div class="mb-2 text-[13px] font-medium text-slate-700">
          血袋明细（{{ (detail.bags || []).length }} 袋）
        </div>
        <el-table :data="detail.bags || []" border size="small" style="width: 100%">
          <el-table-column label="血袋号" min-width="140" prop="bagNo"/>
          <el-table-column label="献血编号" min-width="120" prop="donorNo"/>
          <el-table-column label="血型" width="110">
            <template #default="{ row }">{{ text(row.bloodTypeText) }}</template>
          </el-table-column>
          <el-table-column label="规格 / 血量" min-width="130">
            <template #default="{ row }">
              {{ text(row.spec) }}
              <span v-if="row.amount" class="text-slate-400">{{ row.amount }}{{ row.amountUnit || '' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="来源血站" min-width="120">
            <template #default="{ row }">{{ text(row.sourceBank) }}</template>
          </el-table-column>
          <el-table-column label="有效期" width="140">
            <template #default="{ row }">
              <span :class="row.expired ? 'text-red-600' : 'text-slate-700'">
                {{ text(row.expireDate) }}{{ row.expired ? '（已过期）' : '' }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="配血结果" min-width="130">
            <template #default="{ row }">
              <el-tag :type="row.crossmatchResult === 1 ? 'success' : 'danger'" effect="plain" size="small">
                {{ text(row.crossmatchResultText) }}
              </el-tag>
              <div class="text-[11px] text-slate-400">
                主 {{ text(row.crossmatchMain) }} / 次 {{ text(row.crossmatchSide) }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ text(row.bagStatusText) }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// ---------------- 详情 ----------------
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getTransfusionApplyDetail} from '@/api/inpatientTransfusion';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
});

const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const loading = ref(false);
const detail = ref(null);

const loadDetail = async () => {
  loading.value = true;
  try {
    const res = await getTransfusionApplyDetail(props.id);
    detail.value = res.data;
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false;
  }
};

watch(visible, (open) => {
  if (open && props.id) loadDetail();
});
</script>
