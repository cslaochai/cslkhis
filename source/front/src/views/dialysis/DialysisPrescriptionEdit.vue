<template>
  <!-- ============ 处方弹框 ============ -->
  <el-dialog v-model="visible" :title="`透析处方 · ${archive?.patientName || ''}`" data-testid="hd-prescription-dialog"
             width="900px">
    <div class="space-y-3">
      <el-table v-loading="preLoading" :data="preRows" border data-testid="hd-prescription-table" max-height="240"
                size="small">
        <el-table-column label="生效日" prop="startDate" width="110"/>
        <el-table-column label="停用日" prop="endDate" width="110">
          <template #default="{ row }">{{ row.endDate || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="prescriptionStatusTag(row.status)" size="small">
              {{ row.status === 1 ? '有效' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="right" label="干体重" prop="dryWeight" width="90"/>
        <el-table-column align="right" label="时长(min)" prop="durationMin" width="90"/>
        <el-table-column align="right" label="血流(ml/min)" prop="bloodFlow" width="110"/>
        <el-table-column label="透析器" width="130">
          <template #default="{ row }">{{ dictText('dialyzer', row.dialyzer) }}</template>
        </el-table-column>
        <el-table-column label="抗凝" width="110">
          <template #default="{ row }">{{ dictText('anticoag', row.anticoagulant) }}</template>
        </el-table-column>
        <el-table-column label="剂量" min-width="140" prop="anticoagDose">
          <template #default="{ row }">{{ row.anticoagDose || '—' }}</template>
        </el-table-column>
        <el-table-column label="开立人" prop="doctorName" width="90"/>
        <el-table-column fixed="right" label="操作" width="130">
          <template #default="{ row }">
            <el-button v-if="row.status === 1" v-perm="'medtech:dialysis:add'" data-testid="hd-pre-edit-btn" link size="small"
                       type="primary" @click="openPreEdit(row)">编辑
            </el-button>
            <el-button v-if="canStopPrescription(row.status)" v-perm="'medtech:dialysis:add'" data-testid="hd-pre-stop-btn" link
                       size="small"
                       type="danger" @click="stopPrescription(row)">停用
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-4 text-sm text-slate-400">还没有处方，下面填一张（没有处方不能排班）</div>
        </template>
      </el-table>

      <div v-if="canWritePrescription(archive?.status)" class="border border-slate-200 rounded p-3 space-y-2">
        <div class="text-sm font-medium text-slate-600">{{ preForm.id ? '修改处方' : '新开处方' }}</div>
        <div class="flex items-center gap-3 flex-wrap">
          <el-input-number v-model="preForm.dryWeight" :controls="false" :max="999.99" :min="0" :precision="2"
                           class="w-28" data-testid="hd-pre-dry" placeholder="干体重kg"/>
          <el-input-number v-model="preForm.durationMin" :controls="false" :max="720" :min="30"
                           class="w-24" data-testid="hd-pre-duration" placeholder="时长min"/>
          <el-input-number v-model="preForm.bloodFlow" :controls="false" :max="400" :min="50"
                           class="w-28" data-testid="hd-pre-flow" placeholder="血流ml/min"/>
          <el-select v-model="preForm.dialyzer" :fit-input-width="false" class="w-40" data-testid="hd-pre-dialyzer"
                     placeholder="透析器">
            <el-option v-for="d in dicts.dialyzer" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
          <el-select v-model="preForm.anticoagulant" :fit-input-width="false" class="w-36" data-testid="hd-pre-anticoag"
                     placeholder="抗凝">
            <el-option v-for="d in dicts.anticoag" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
          <el-input v-model="preForm.anticoagDose" class="w-52" data-testid="hd-pre-dose" placeholder="抗凝剂量描述"/>
          <el-input-number v-model="preForm.targetUltraMl" :controls="false" :max="99999" :min="0" :precision="1"
                           class="w-32" data-testid="hd-pre-ultra" placeholder="目标超滤ml"/>
          <el-date-picker v-model="preForm.startDate" :clearable="false" class="w-36" data-testid="hd-pre-start"
                          placeholder="生效日期" type="date" value-format="YYYY-MM-DD"/>
          <el-input v-model="preForm.remark" class="w-40" data-testid="hd-pre-remark" placeholder="备注"/>
          <el-button v-perm="'medtech:dialysis:add'" :icon="preForm.id ? Edit : Plus" data-testid="hd-pre-save-btn"
                     type="primary" @click="savePrescription">{{ preForm.id ? '保存修改' : '开处方' }}
          </el-button>
          <el-button v-if="preForm.id" :icon="Refresh" @click="resetPreForm">清空重填</el-button>
        </div>
        <div class="text-xs text-slate-500">新开处方会把该档案原来的有效处方自动停用；已停用的处方不可编辑。</div>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 处方弹框：列表 + 表单同在本弹框内，preForm.id 为空走新开、有值走修改（后端同一个 upsert）
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Edit, Plus, Refresh} from '@element-plus/icons-vue';
import {listDialysisPrescriptions, stopDialysisPrescription, upsertDialysisPrescription} from '@/api/dialysis';
import {dictLabelText, localDateStr} from '@/lib/utils';
import {canStopPrescription, canWritePrescription, prescriptionStatusTag} from '@/lib/dialysis';

const props = defineProps({
  modelValue: Boolean,
  // 当前档案行（列表也要用的状态判断都基于它）
  archive: {type: Object, default: null},
  // 页面级参照：透析器/抗凝字典由主页面加载一次后传入
  dicts: {type: Object, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictText = (key, value) => dictLabelText(props.dicts[key], value);

const preRows = ref([]);
const preLoading = ref(false);
const preForm = reactive({
  id: null, dryWeight: null, durationMin: 240, bloodFlow: 220, dialyzer: 3,
  anticoagulant: 1, anticoagDose: '', targetUltraMl: null, startDate: localDateStr(), remark: '',
});
const loadPrescriptions = async () => {
  if (!props.archive?.id)
    return;
  preLoading.value = true;
  try {
    const res = await listDialysisPrescriptions(props.archive.id);
    preRows.value = res?.data || [];
  } catch (e) {
    console.error(e);
  } finally {
    preLoading.value = false;
  }
};
const resetPreForm = () => {
  Object.assign(preForm, {
    id: null, dryWeight: null, durationMin: 240, bloodFlow: 220, dialyzer: 3,
    anticoagulant: 1, anticoagDose: '', targetUltraMl: null, startDate: localDateStr(), remark: '',
  });
};
const openPreEdit = (row) => {
  if (!row) {
    resetPreForm();
    return;
  }
  Object.assign(preForm, {
    id: row.id, dryWeight: row.dryWeight, durationMin: row.durationMin, bloodFlow: row.bloodFlow,
    dialyzer: row.dialyzer, anticoagulant: row.anticoagulant, anticoagDose: row.anticoagDose || '',
    targetUltraMl: row.targetUltraMl, startDate: row.startDate, remark: row.remark || '',
  });
};
const savePrescription = async () => {
  if (preForm.dryWeight == null) {
    ElMessage.warning('干体重不能为空');
    return;
  }
  if (!preForm.startDate) {
    ElMessage.warning('处方生效日期不能为空');
    return;
  }
  try {
    const res = await upsertDialysisPrescription({
      id: preForm.id ?? undefined,
      archiveId: props.archive.id,
      dryWeight: preForm.dryWeight,
      durationMin: preForm.durationMin ?? undefined,
      bloodFlow: preForm.bloodFlow ?? undefined,
      dialyzer: preForm.dialyzer ?? undefined,
      anticoagulant: preForm.anticoagulant ?? undefined,
      anticoagDose: preForm.anticoagDose || undefined,
      targetUltraMl: preForm.targetUltraMl ?? undefined,
      startDate: preForm.startDate,
      remark: preForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '处方已保存');
      resetPreForm();
      await loadPrescriptions();
      emit('saved');
    } else {
      ElMessage.error(res.message || '处方保存失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '处方保存失败');
  }
};
const stopPrescription = async (row) => {
  try {
    const res = await stopDialysisPrescription({id: row.id, reason: '临床调整，停用该处方'});
    if (res.code === 200) {
      ElMessage.success(res.message || '处方已停用');
      await loadPrescriptions();
      emit('saved');
    } else {
      ElMessage.error(res.message || '处方停用失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '处方停用失败');
  }
};

watch(visible, (v) => {
  if (!v) return;
  resetPreForm();
  loadPrescriptions();
});
</script>
