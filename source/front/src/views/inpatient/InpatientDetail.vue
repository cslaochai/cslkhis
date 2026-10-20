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
            <span class="font-semibold text-slate-800">管床团队（主管医生）</span>
            <el-button v-if="detail.admission?.admitStatus === 1" size="small" type="primary" plain
                       @click="openBind">指派 / 转交主管医生</el-button>
          </div>
          <el-table :data="attendingList" border size="small">
            <el-table-column label="角色" width="100">
              <template #default="{ row }">{{ row.relationTypeText }}</template>
            </el-table-column>
            <el-table-column label="医生" prop="employeeName" min-width="100"/>
            <el-table-column label="科室" prop="deptName" min-width="120"/>
            <el-table-column label="生效时间" prop="effectiveTime" width="160"/>
            <el-table-column label="失效时间" width="160">
              <template #default="{ row }">{{ row.expireTime || '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
                  {{ row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130" align="center">
              <template #default="{ row }">
                <el-button v-if="row.status === 1" link type="warning" size="small"
                           @click="onEndAttending(row)">结束</el-button>
                <el-button link type="danger" size="small" @click="onDeleteAttending(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <p v-if="!attendingList.length" class="py-4 text-center text-xs text-slate-400">
            尚未指派主管医生
          </p>
        </div>

        <div class="rounded-lg border border-slate-200 p-4">
          <div class="mb-3 flex items-center gap-2">
            <span class="font-semibold text-slate-800">责任护士</span>
            <el-button v-if="detail.admission?.admitStatus === 1" size="small" type="primary" plain
                       @click="openNurseBind">指派 / 转交责任护士</el-button>
          </div>
          <el-table :data="nurseList" border size="small">
            <el-table-column label="护士" prop="nurseName" min-width="100"/>
            <el-table-column label="科室" prop="deptName" min-width="120"/>
            <el-table-column label="生效时间" prop="effectiveTime" width="160"/>
            <el-table-column label="失效时间" width="160">
              <template #default="{ row }">{{ row.expireTime || '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="plain" size="small">
                  {{ row.statusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="130" align="center">
              <template #default="{ row }">
                <el-button v-if="row.status === 1" link type="warning" size="small"
                           @click="onEndNurse(row)">结束</el-button>
                <el-button link type="danger" size="small" @click="onDeleteNurse(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <p v-if="!nurseList.length" class="py-4 text-center text-xs text-slate-400">
            尚未指派责任护士
          </p>
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

    <el-dialog v-model="bindVisible" title="指派 / 转交管床医生" width="520px" append-to-body>
      <el-form :model="bindForm" label-width="90px">
        <el-form-item label="角色">
          <el-radio-group v-model="bindForm.relationType">
            <el-radio :value="1">主管</el-radio>
            <el-radio :value="2">主诊组长</el-radio>
            <el-radio :value="3">协作</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="医生">
          <el-select v-model="bindForm.employeeId" filterable placeholder="搜索姓名 / 工号选人"
                     style="width: 100%">
            <el-option v-for="e in doctorOptions" :key="e.id"
                       :label="`${e.empName}（${e.empCode || '无工号'}）${e.deptName ? '·' + e.deptName : ''}`"
                       :value="e.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="bindForm.effectiveTime" type="datetime"
                          value-format="YYYY-MM-DD HH:mm:ss" placeholder="留空＝从入院时间起算"
                          style="width: 100%"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="bindForm.remark" type="textarea" :rows="2" placeholder="交接说明（可空）"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="bindLoading" @click="submitBind">确定</el-button>
        <el-button @click="bindVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="nurseBindVisible" title="指派 / 转交责任护士" width="520px" append-to-body>
      <el-form :model="nurseBindForm" label-width="90px">
        <el-form-item label="护士">
          <el-select v-model="nurseBindForm.nurseId" filterable placeholder="搜索姓名 / 工号选人"
                     style="width: 100%">
            <el-option v-for="n in nurseOptions" :key="n.id"
                       :label="`${n.empName}（${n.empCode || '无工号'}）${n.deptName ? '·' + n.deptName : ''}`"
                       :value="n.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="生效时间">
          <el-date-picker v-model="nurseBindForm.effectiveTime" type="datetime"
                          value-format="YYYY-MM-DD HH:mm:ss" placeholder="留空＝从当前/入院时刻起算"
                          style="width: 100%"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="nurseBindForm.remark" type="textarea" :rows="2" placeholder="交接说明（可空）"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" :loading="nurseBindLoading" @click="submitNurseBind">确定</el-button>
        <el-button @click="nurseBindVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </el-drawer>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  bindAttending,
  bindChargeNurse,
  deleteAttending,
  deleteChargeNurse,
  endAttending,
  endChargeNurse,
  getAttendingList,
  getChargeNurseList,
  getInpatientDetail
} from '@/api/inpatient';
import {getEmployeeList} from '@/api/system';
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

// ===== 主管医生管床关系 =====
const attendingList = ref([]);
const doctorOptions = ref([]);
const bindVisible = ref(false);
const bindLoading = ref(false);
const bindForm = reactive({
  relationType: 1,
  employeeId: null,
  effectiveTime: '',
  remark: '',
});

const loadAttending = async () => {
  if (!props.id) return;
  try {
    const res = await getAttendingList({admissionId: props.id});
    attendingList.value = res.data || [];
  } catch (e) {
    /* 全局拦截器已提示 */
  }
};

const loadDoctors = async () => {
  if (doctorOptions.value.length) return;
  try {
    const res = await getEmployeeList({empType: 1});
    doctorOptions.value = res.data || [];
  } catch (e) {
    /* 全局拦截器已提示 */
  }
};

const openBind = () => {
  Object.assign(bindForm, {relationType: 1, employeeId: null, effectiveTime: '', remark: ''});
  bindVisible.value = true;
  loadDoctors();
};

const submitBind = async () => {
  if (!bindForm.employeeId) {
    ElMessage.warning('请选择医生');
    return;
  }
  bindLoading.value = true;
  try {
    await bindAttending({
      admissionId: props.id,
      employeeId: bindForm.employeeId,
      relationType: bindForm.relationType,
      effectiveTime: bindForm.effectiveTime || null,
      remark: bindForm.remark || '',
    });
    ElMessage.success('管床关系已保存');
    bindVisible.value = false;
    await loadAttending();
  } catch (e) {
    /* 全局拦截器已提示 */
  } finally {
    bindLoading.value = false;
  }
};

const onEndAttending = async (row) => {
  try {
    await ElMessageBox.confirm(`结束 ${row.employeeName}（${row.relationTypeText}）的管床关系？结束后留痕不删除。`, '确认结束', {type: 'warning'});
  } catch {
    return;
  }
  await endAttending(row.id);
  ElMessage.success('已结束');
  await loadAttending();
};

const onDeleteAttending = async (row) => {
  try {
    await ElMessageBox.confirm('删除误建的管床关系（物理删除，不可恢复）？', '确认删除', {type: 'error'});
  } catch {
    return;
  }
  await deleteAttending(row.id);
  ElMessage.success('已删除');
  await loadAttending();
};

// ===== 责任护士关系 =====
const nurseList = ref([]);
const nurseOptions = ref([]);
const nurseBindVisible = ref(false);
const nurseBindLoading = ref(false);
const nurseBindForm = reactive({
  nurseId: null,
  effectiveTime: '',
  remark: '',
});

const loadNurse = async () => {
  if (!props.id) return;
  try {
    const res = await getChargeNurseList({admissionId: props.id});
    nurseList.value = res.data || [];
  } catch (e) {
    /* 全局拦截器已提示 */
  }
};

const loadNurses = async () => {
  if (nurseOptions.value.length) return;
  try {
    const res = await getEmployeeList({empType: 2});
    nurseOptions.value = res.data || [];
  } catch (e) {
    /* 全局拦截器已提示 */
  }
};

const openNurseBind = () => {
  Object.assign(nurseBindForm, {nurseId: null, effectiveTime: '', remark: ''});
  nurseBindVisible.value = true;
  loadNurses();
};

const submitNurseBind = async () => {
  if (!nurseBindForm.nurseId) {
    ElMessage.warning('请选择责任护士');
    return;
  }
  nurseBindLoading.value = true;
  try {
    await bindChargeNurse({
      admissionId: props.id,
      nurseId: nurseBindForm.nurseId,
      effectiveTime: nurseBindForm.effectiveTime || null,
      remark: nurseBindForm.remark || '',
    });
    ElMessage.success('责任护士已保存');
    nurseBindVisible.value = false;
    await loadNurse();
  } catch (e) {
    /* 全局拦截器已提示 */
  } finally {
    nurseBindLoading.value = false;
  }
};

const onEndNurse = async (row) => {
  try {
    await ElMessageBox.confirm(`结束 ${row.nurseName} 的责任护士关系？结束后留痕。`, '确认结束', {type: 'warning'});
  } catch {
    return;
  }
  await endChargeNurse(row.id);
  ElMessage.success('已结束');
  await loadNurse();
};

const onDeleteNurse = async (row) => {
  try {
    await ElMessageBox.confirm('删除误建的责任护士关系（物理删除，不可恢复）？', '确认删除', {type: 'error'});
  } catch {
    return;
  }
  await deleteChargeNurse(row.id);
  ElMessage.success('已删除');
  await loadNurse();
};

watch(visible, async (open) => {
  if (!open)
    return;
  detailLoading.value = true;
  detail.value = null;
  attendingList.value = [];
  nurseList.value = [];
  try {
    const res = await getInpatientDetail(props.id);
    detail.value = res.data;
    await Promise.all([loadAttending(), loadNurse()]);
  } catch (e) {
    
    visible.value = false;
  } finally {
    detailLoading.value = false;
  }
});
</script>
