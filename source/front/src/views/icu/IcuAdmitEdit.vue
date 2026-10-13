<template>
  <!-- ============ 入科弹框 ============ -->
  <el-dialog v-model="visible" :title="admitTitle" data-testid="icu-admit-dialog" width="620px">
    <el-form :model="admitForm" label-width="100px">
      <el-form-item label="住院患者" required>
        <el-select v-model="admitForm.admissionId" :disabled="!!admitForm.id" :fit-input-width="false" :loading="admissionLoading"
                   :remote-method="searchAdmission" class="w-full" data-testid="icu-admit-patient"
                   filterable
                   placeholder="输入姓名/住院号搜索在院患者" remote>
          <el-option v-for="a in admissionOptions" :key="a.admissionId"
                     :label="`${a.patientName}（${a.patientNo}）${a.deptName || ''} ${a.bedNo || ''}`"
                     :value="a.admissionId"/>
        </el-select>
      </el-form-item>
      <el-form-item label="ICU 床位" required>
        <el-select v-model="admitForm.bedId" :disabled="!!admitForm.id" :fit-input-width="false" class="w-52"
                   data-testid="icu-admit-bed" placeholder="选择空闲 ICU 床">
          <el-option v-for="b in freeBeds" :key="b.bedId" :label="b.bedNo" :value="b.bedId"/>
          <el-option v-if="admitForm.id && admitForm.bedId" :key="`cur-${admitForm.bedId}`"
                     :label="`${admitForm.bedNo}（在科中不可换床，需换床请先出科）`" :value="admitForm.bedId"/>
        </el-select>
        <span class="text-xs text-slate-500 ml-2">一次住院同时仅一条在科记录，一床同时只允许一名患者</span>
      </el-form-item>
      <el-form-item label="监护等级" required>
        <el-select v-model="admitForm.careLevel" :fit-input-width="false" class="w-40" data-testid="icu-admit-care">
          <el-option v-for="d in dicts.careLevel" :key="d.dictValue" :label="d.dictLabel"
                     :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="入科时间" required>
        <el-date-picker v-model="admitForm.inTime" :clearable="false" class="w-52"
                        data-testid="icu-admit-time" placeholder="不得晚于当前、不早于入院" type="datetime"
                        value-format="YYYY-MM-DD HH:mm:ss"/>
      </el-form-item>
      <el-form-item label="入科 GCS">
        <el-input v-model="admitForm.inGcs" class="w-32" data-testid="icu-admit-gcs" placeholder="3~15，可空"/>
      </el-form-item>
      <el-form-item label="入科诊断">
        <el-input v-model="admitForm.inDiag" data-testid="icu-admit-diag" placeholder="如 重症肺炎、感染性休克"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="admitForm.remark" data-testid="icu-admit-remark"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="admitSaving" data-testid="icu-admit-ok" type="primary" @click="saveAdmit">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {listIcuAdmissions, upsertIcuStay} from '@/api/icu';
import {nowText} from '@/lib/icu';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 修改时传入的台账行；为空走新增（入科登记）
  row: {type: Object, default: null},
  // 从空床卡片预选的床位（仅新增时生效）
  bed: {type: Object, default: null},
  // 列表也在用的字典由页面加载一次传下来
  dicts: {type: Object, required: true},
  freeBeds: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const admitSaving = ref(false);
const admitForm = reactive({
  id: null,
  admissionId: null,
  bedId: null,
  bedNo: '',
  careLevel: 1,
  inTime: '',
  inGcs: null,
  inDiag: '',
  remark: '',
});
const admitTitle = computed(() => (admitForm.id ? '修改入科信息' : 'ICU 入科登记'));
const admissionOptions = ref([]);
const admissionLoading = ref(false);
const searchAdmission = async (keyword) => {
  admissionLoading.value = true;
  try {
    const res = await listIcuAdmissions({keyword: keyword?.trim() || undefined, limit: 50});
    admissionOptions.value = res?.data || [];
  } catch (e) {
    console.error(e);
  } finally {
    admissionLoading.value = false;
  }
};
watch(() => props.modelValue, async (v) => {
  if (!v) return;
  const row = props.row;
  const bed = props.bed;
  admitForm.id = row?.id ?? null;
  admitForm.inTime = row?.inTime || nowText();
  admitForm.careLevel = row?.careLevel ?? 1;
  admitForm.inGcs = row?.inGcs ?? null;
  admitForm.inDiag = row?.inDiag ?? '';
  admitForm.remark = row?.remark ?? '';
  admitForm.bedId = row?.bedId ?? bed?.bedId ?? null;
  admitForm.bedNo = row?.bedNo ?? bed?.bedNo ?? '';
  admitForm.admissionId = row?.admissionId ?? null;
  // 修改时患者不可换：候选下拉里放上这条记录的患者，否则 el-select 只显示 ID
  admissionOptions.value = row
      ? [{
        admissionId: row.admissionId,
        patientName: row.patientName,
        patientNo: row.patientNo,
        deptName: row.fromDeptName,
        bedNo: row.bedNo,
        diagnosis: row.inDiag
      }]
      : [];
  if (!row)
    await searchAdmission('');
});
const saveAdmit = async () => {
  if (!admitForm.admissionId)
    return ElMessage.warning('请选择住院患者');
  if (!admitForm.bedId)
    return ElMessage.warning('请选择 ICU 床位');
  if (!admitForm.inTime)
    return ElMessage.warning('请选择入科时间');
  admitSaving.value = true;
  try {
    const res = await upsertIcuStay({
      id: admitForm.id || undefined,
      admissionId: admitForm.admissionId,
      bedId: admitForm.bedId,
      careLevel: admitForm.careLevel,
      inTime: admitForm.inTime,
      inGcs: admitForm.inGcs === '' ? null : admitForm.inGcs,
      inDiag: admitForm.inDiag || undefined,
      remark: admitForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '入科已登记');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '入科登记失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '入科登记失败');
  } finally {
    admitSaving.value = false;
  }
};
</script>
