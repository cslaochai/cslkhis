<template>
  <!-- 交班清零：清单由后端按「本科室未闭环 ∧（无人认领 ∨ 挂我名下）」出题，
       逐条点名写去向才能提交 —— 漏一条就等于把这个人留在上一个班的名单里没人管。 -->
  <el-dialog v-model="visible" destroy-on-close title="急诊交班清零" width="1080px">
    <div class="mb-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs leading-relaxed text-amber-700">
      交班 = 把「挂在我名下」和「还没有人负责」的未闭环患者<strong>逐条</strong>移交并写下去向交代。
      只要有一条没点名，本次交班不能提交。移交后责任医生立即变更（叫号队列与挂号单一并改到接班人名下），
      并给接续医生发一条接收待办；台账只增不改，做错了只能重新交一次。
    </div>

    <div class="mb-3 grid grid-cols-2 gap-4">
      <el-form :model="hoForm" label-position="top">
        <el-form-item label="接班人（总接班，逐条可再指定别的接续医生）" required>
          <el-select v-model="hoForm.takeEmpId" class="!w-full" data-testid="emg-ho-taker"
                     filterable placeholder="选择接班医生">
            <el-option v-for="t in takers" :key="t.empId" :label="t.empName" :value="t.empId">
              <span>{{ t.empName }}</span>
              <span class="float-right text-xs text-slate-400">{{ t.deptName }} · {{ t.sourceText }}</span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <el-form :model="hoForm" label-position="top">
        <el-form-item label="整班备注（可选）">
          <el-input v-model="hoForm.remark" placeholder="如：夜班 1 例在抢救、设备科已通知"/>
        </el-form-item>
      </el-form>
    </div>

    <el-table v-loading="hoLoading" :data="hoPending" data-testid="emg-ho-table" max-height="300"
              style="width: 100%">
      <el-table-column label="患者" width="120">
        <template #default="{ row }">
          <div class="text-sm font-medium text-slate-900">{{ row.patientName }}</div>
          <div class="font-mono text-xs text-slate-400">{{ row.emergencyNo }}</div>
        </template>
      </el-table-column>
      <el-table-column label="分诊/状态" width="110">
        <template #default="{ row }">
          <div class="text-xs text-slate-700">{{ row.triageLevelText }}</div>
          <div class="text-xs text-slate-400">{{ row.emergencyStatusText }}</div>
        </template>
      </el-table-column>
      <el-table-column label="现负责" width="90">
        <template #default="{ row }">
          <span v-if="row.doctorName" class="text-xs text-slate-700">{{ row.doctorName }}</span>
          <el-tooltip v-else :content="row.unassignedReason || '尚未派单'" placement="top">
            <span
                class="inline-block rounded border border-amber-300 bg-amber-50 px-1.5 py-0.5 text-xs font-medium text-amber-700">
              待派单
            </span>
          </el-tooltip>
        </template>
      </el-table-column>
      <el-table-column label="计时" width="90">
        <template #default="{ row }">
          <div
              :class="['text-xs', row.overdueLevel > 0 || row.obsLevel > 0 ? 'font-bold text-red-600' : 'text-slate-600']">
            {{ hoTimeText(row) }}
          </div>
          <div v-if="row.overdueText || row.obsLevelText" class="text-xs font-medium text-red-500">
            {{ row.obsLevelText || row.overdueText }}
          </div>
        </template>
      </el-table-column>
      <el-table-column label="主诉/诊断" min-width="150" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="text-xs text-slate-700">{{ row.chiefComplaint || '—' }}</div>
          <div class="text-xs text-slate-400">{{ row.diagnosis || '' }}</div>
        </template>
      </el-table-column>
      <el-table-column label="接续医生" width="130">
        <template #default="{ row }">
          <el-select v-model="hoRows[String(row.emergencyId)].takeDoctorId" class="!w-full"
                     clearable filterable placeholder="默认接班人">
            <el-option v-for="t in takers" :key="t.empId" :label="t.empName" :value="t.empId"/>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="去向交代" min-width="180">
        <template #default="{ row }">
          <el-select v-model="hoRows[String(row.emergencyId)].disposition" allow-create
                     class="!w-full" default-first-option filterable placeholder="必填：下一步做什么">
            <el-option v-for="o in dispositionOptions" :key="o" :label="o" :value="o"/>
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="补充交代" min-width="150">
        <template #default="{ row }">
          <el-input v-model="hoRows[String(row.emergencyId)].handoverNote" class="!w-full"
                    placeholder="过敏史/家属在等…"/>
        </template>
      </el-table-column>
    </el-table>

    <template #footer>
      <span class="mr-4 text-xs text-slate-500" data-testid="emg-ho-count">
        共 {{ hoPending.length }} 条（其中待派单 {{ hoPoolCount }} 条）<template v-if="hoMissing > 0">
          · <span class="font-medium text-red-600">未点名 {{ hoMissing }} 条</span></template>
      </span>
      <el-button :disabled="hoPending.length === 0 || hoMissing > 0" :loading="hoSubmitting" data-testid="emg-ho-submit"
                 type="warning" @click="submitHandover">
        确认交班
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 交班的定义是「我名下不再有未闭环的患者」，所以清单必须逐条点名：
// 后端按「本科室未闭环 ∧（无人指派 ∨ 挂我名下）」出题，少一条就整体拒绝。
// 前端不自己数"还剩几条"，清单永远来自接口，避免两边各算一套。
import {computed, ref, watch} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  getEmergencyHandoverPendingList,
  getEmergencyHandoverTakeList,
  saveEmergencyHandover
} from '@/api/emergency';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const hoLoading = ref(false);
const hoSubmitting = ref(false);
const hoPending = ref([]);
const takers = ref([]);
// key = emergencyId（雪花 ID 一律当字符串用，Number() 会丢精度）
const hoRows = ref({});
const hoForm = ref({takeEmpId: null, remark: ''});
const dispositionOptions = ['继续留观，等待结果', '已收诊治，需接续观察', '联系入院/转专科', '评估后可离院'];

/** 清单里"计时"一列两口径：留观看已占床小时数，候诊/诊治看等候分钟（后端 NOW() 现算） */
const hoTimeText = (row) => {
  if (row.emergencyStatus === 3)
    return row.obsHours == null ? '-' : `${row.obsHours}小时`;
  if (row.waitMinutes == null)
    return '-';
  const m = Number(row.waitMinutes);
  if (m < 60)
    return `${m}分钟`;
  return `${Math.floor(m / 60)}小时${m % 60}分钟`;
};

const loadHandoverPending = async () => {
  hoLoading.value = true;
  try {
    const [pendingRes, takerRes] = await Promise.all([
      getEmergencyHandoverPendingList(),
      getEmergencyHandoverTakeList(),
    ]);
    hoPending.value = pendingRes.data || [];
    takers.value = (takerRes.data || []).map((t) => ({...t, empId: String(t.empId)}));
    // 每次重开都重新逐行建账：残留上一班的交代等于把没交接的人算成交接了
    hoRows.value = {};
    hoPending.value.forEach((row) => {
      hoRows.value[String(row.emergencyId)] = {takeDoctorId: null, disposition: '', handoverNote: ''};
    });
  } catch (error) {
    ElMessage.error(error.message || '待交班清单加载失败');
  } finally {
    hoLoading.value = false;
  }
};

watch(visible, async (open) => {
  if (!open)
    return;
  hoForm.value = {takeEmpId: null, remark: ''};
  await loadHandoverPending();
});

/** 未点名条数 > 0 时禁用提交按钮：让"漏交"在点下去之前就显形，而不是等后端拒绝 */
const hoMissing = computed(() => hoPending.value.filter((row) => !(hoRows.value[String(row.emergencyId)]?.disposition || '').trim()).length);
const hoPoolCount = computed(() => hoPending.value.filter((row) => row.poolFlag).length);

const submitHandover = async () => {
  if (!hoForm.value.takeEmpId)
    return ElMessage.warning('请选择接班人');
  if (hoMissing.value > 0)
    return ElMessage.warning(`还有 ${hoMissing.value} 条未填写去向交代`);
  const take = takers.value.find((t) => t.empId === hoForm.value.takeEmpId);
  try {
    await ElMessageBox.confirm(`确认把 ${hoPending.value.length} 名未闭环患者移交给 ${take?.empName || '接班人'}？移交后这些患者的责任医生即刻变更，台账不可修改。`, '交班确认', {
      confirmButtonText: '确认交班',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }
  hoSubmitting.value = true;
  try {
    const res = await saveEmergencyHandover({
      takeEmpId: hoForm.value.takeEmpId,
      remark: hoForm.value.remark || undefined,
      items: hoPending.value.map((row) => {
        const r = hoRows.value[String(row.emergencyId)] || {takeDoctorId: null, disposition: '', handoverNote: ''};
        return {
          emergencyId: String(row.emergencyId),
          takeDoctorId: r.takeDoctorId || undefined,
          disposition: r.disposition.trim(),
          handoverNote: r.handoverNote || undefined,
        };
      }),
    });
    ElMessage.success(`交班完成（${res.data}），未闭环清单已清零`);
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '交班失败');
    // 清单可能已被别人改动（新登记/已接诊），失败后重新拉一次再让用户点名
    await loadHandoverPending();
  } finally {
    hoSubmitting.value = false;
  }
};
</script>
