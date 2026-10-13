<template>
  <el-drawer v-model="visible" destroy-on-close size="880px" title="住院详情">
    <div v-loading="detailLoading" class="space-y-4">
      <template v-if="detail">
        <div class="rounded-lg border border-slate-200 p-4">
          <div class="mb-3 flex items-center gap-2">
            <span class="font-semibold text-slate-800">{{ detail.admission?.patientName }}</span>
            <el-tag :type="detail.admission?.admitStatus === 1 ? 'primary' : 'info'" effect="plain" size="small">
              {{ detail.admission?.admitStatus === 1 ? '在院' : '已出院' }}
            </el-tag>
            <el-tag effect="plain" size="small" type="warning">住院号 {{ detail.admission?.admissionNo }}</el-tag>
          </div>
          <div class="grid grid-cols-3 gap-x-4 gap-y-2 text-sm">
            <div><span class="text-slate-400">性别 / 年龄</span>：{{ genderText(detail.admission?.gender) }} ·
              {{ detail.admission?.age ?? '—' }}岁
            </div>
            <div><span class="text-slate-400">科室</span>：{{ detail.admission?.deptName || '—' }}</div>
            <div><span class="text-slate-400">病区 / 床位</span>：{{ detail.admission?.wardName || '—' }} ·
              {{ detail.admission?.bedNo || '—' }}
            </div>
            <div><span class="text-slate-400">入院途径</span>：{{ textOf(admitWayMap, detail.admission?.admitWay) }}
            </div>
            <div><span class="text-slate-400">入院时间</span>：{{ detail.admission?.admitTime || '—' }}</div>
            <div><span class="text-slate-400">出院时间</span>：{{ detail.admission?.dischargeTime || '—' }}</div>
            <div><span class="text-slate-400">住院天数</span>：{{ detail.admission?.inpatientDays ?? 0 }} 天</div>
            <div><span class="text-slate-400">主管医生</span>：{{ detail.admission?.doctorName || '—' }}</div>
            <div><span class="text-slate-400">医保卡号</span>：{{ detail.admission?.medicalInsuranceNo || '—' }}</div>
            <div class="col-span-3"><span class="text-slate-400">入院诊断</span>：{{
                detail.admission?.diagnosis || '—'
              }}
            </div>
          </div>
        </div>

        <div class="rounded-lg border border-slate-200 p-4">
          <div class="mb-3 flex items-center gap-2">
            <span class="font-semibold text-slate-800">病案首页</span>
            <el-tag :type="detail.summaryStatusText === '已归档' ? 'success' : 'info'" effect="plain" size="small">
              {{ detail.summaryStatusText }}
            </el-tag>
            <el-tag v-if="detail.summary?.readmit31d === 1" effect="plain" size="small" type="danger">31 日内再入院
            </el-tag>
            <el-tag v-if="detail.summary?.isSurgery === 1" effect="plain" size="small" type="warning">有手术</el-tag>
          </div>
          <div class="grid grid-cols-3 gap-x-4 gap-y-2 text-sm">
            <div><span class="text-slate-400">主要诊断</span>：{{
                detail.summary?.mainDiagnosisName || '—'
              }}（{{ detail.summary?.mainDiagnosisCode || '未编码' }}）
            </div>
            <div><span class="text-slate-400">离院方式</span>：{{
                textOf(dischargeWayMap, detail.summary?.dischargeWay)
              }}
            </div>
            <div><span class="text-slate-400">是否危重</span>：{{
                detail.summary?.isCritical === 1 ? '是' : detail.summary?.isCritical === 0 ? '否' : '—'
              }}
            </div>
            <div><span class="text-slate-400">住院总费用</span>：<span
                class="font-semibold text-blue-600">{{ money(detail.summary?.totalAmount) }}</span></div>
            <div><span class="text-slate-400">西药费</span>：{{ money(detail.summary?.westernDrugAmount) }}</div>
            <div><span class="text-slate-400">检查费</span>：{{ money(detail.summary?.examAmount) }}</div>
            <div><span class="text-slate-400">检验费</span>：{{ money(detail.summary?.labAmount) }}</div>
            <div><span class="text-slate-400">手术费</span>：{{ money(detail.summary?.operationAmount) }}</div>
            <div><span class="text-slate-400">床位费</span>：{{ money(detail.summary?.bedAmount) }}</div>
            <div><span class="text-slate-400">护理费</span>：{{ money(detail.summary?.nursingAmount) }}</div>
            <div><span class="text-slate-400">耗材费</span>：{{ money(detail.summary?.materialAmount) }}</div>
            <div><span class="text-slate-400">治疗费</span>：{{ money(detail.summary?.treatmentAmount) }}</div>
          </div>
        </div>

        <div class="rounded-lg border border-slate-200 p-4">
          <p class="mb-2 font-semibold text-slate-800">诊断明细</p>
          <el-table :data="detail.diagnoses || []" border size="small">
            <el-table-column align="center" label="序号" prop="seqNo" width="60"/>
            <el-table-column label="类型" width="90">
              <template #default="{ row }">
                <el-tag :type="row.diagType === 1 ? 'danger' : 'info'" effect="plain" size="small">
                  {{ row.diagType === 1 ? '主要诊断' : '其他诊断' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="ICD-10" prop="icdCode" width="100"/>
            <el-table-column label="诊断名称" min-width="140" prop="icdName"/>
            <el-table-column label="入院病情" width="100">
              <template #default="{ row }">{{ textOf(admitConditionMap, row.admitCondition) }}</template>
            </el-table-column>
            <el-table-column label="CC/MCC" prop="ccLevel" width="90"/>
            <el-table-column label="诊断依据" min-width="180" prop="diagnosisBasis" show-overflow-tooltip/>
          </el-table>
          <p v-if="!(detail.diagnoses || []).length" class="py-4 text-center text-xs text-slate-400">
            未录入诊断明细 —— 没有主要诊断，DRG 无法分组，医保结算清单也过不了
          </p>
        </div>

        <div class="rounded-lg border border-slate-200 p-4">
          <p class="mb-2 font-semibold text-slate-800">手术操作明细</p>
          <el-table :data="detail.operations || []" border size="small">
            <el-table-column align="center" label="序号" prop="seqNo" width="60"/>
            <el-table-column align="center" label="主要" width="70">
              <template #default="{ row }">
                <el-tag v-if="row.isMain === 1" effect="plain" size="small" type="danger">主</el-tag>
                <span v-else class="text-xs text-slate-400">—</span>
              </template>
            </el-table-column>
            <el-table-column label="ICD-9-CM-3" prop="operationCode" width="120"/>
            <el-table-column label="手术操作名称" min-width="140" prop="operationName"/>
            <el-table-column label="手术日期" prop="operationDate" width="150"/>
            <el-table-column label="级别" width="80">
              <template #default="{ row }">{{ textOf(operationLevelMap, row.operationLevel) }}</template>
            </el-table-column>
            <el-table-column label="切口" width="80">
              <template #default="{ row }">{{ textOf(incisionLevelMap, row.incisionLevel) }}</template>
            </el-table-column>
            <el-table-column label="麻醉" width="90">
              <template #default="{ row }">{{ textOf(anesthesiaMap, row.anesthesiaType) }}</template>
            </el-table-column>
            <el-table-column label="主刀" prop="surgeonName" width="90"/>
            <el-table-column label="手术依据" min-width="160" prop="operationBasis" show-overflow-tooltip/>
          </el-table>
          <p v-if="!(detail.operations || []).length" class="py-4 text-center text-xs text-slate-400">
            无手术操作记录
          </p>
        </div>
      </template>
    </div>
  </el-drawer>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getInpatientDetail} from '@/api/inpatient';
import {patientGenderText} from '@/lib/patientGender';
import {
  admitConditionMap,
  admitWayMap,
  anesthesiaMap,
  dischargeWayMap,
  incisionLevelMap,
  money,
  operationLevelMap,
  textOf
} from '@/lib/inpatient';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 入院记录主键：详情组件自己按 id 取数
  id: {type: [String, Number], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const detailLoading = ref(false);
const detail = ref(null);
// 性别文案统一走 lib/patientGender：0 是历史脏码值，渲染成「未知(0)」而不是"女"
const genderText = (g) => patientGenderText(g);

watch(visible, async (open) => {
  if (!open)
    return;
  detailLoading.value = true;
  detail.value = null;
  try {
    const res = await getInpatientDetail(props.id);
    detail.value = res.data;
  } catch (e) {
    
    visible.value = false;
  } finally {
    detailLoading.value = false;
  }
});
</script>
