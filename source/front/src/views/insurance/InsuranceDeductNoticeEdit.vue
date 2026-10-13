<template>
  <!-- ==================== 通知单 新建/编辑 ==================== -->
  <el-dialog v-model="visible"
             :title="noticeForm.id ? `修改扣款通知 ${noticeForm.deductNo}` : '录入扣款通知'" data-testid="deduct-form-dialog"
             width="820px">
    <el-form v-loading="detailLoading" :model="noticeForm" label-width="110px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="扣款来源" required>
            <el-select v-model="noticeForm.sourceType" data-testid="deduct-form-source" style="width: 100%">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_DEDUCT_SOURCE)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="飞检批次">
            <el-select v-model="noticeForm.inspectionId" :placeholder="noticeForm.sourceType === 1 ? '来源为飞检现场时必须关联' : '可不关联'" clearable data-testid="deduct-form-inspection"
                       filterable
                       style="width: 100%">
              <el-option v-for="i in inspectionOptions" :key="i.id" :label="`${i.inspectNo} ${i.fundOrg}`"
                         :value="String(i.id)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="患者">
            <PatientSelect v-model="noticeForm.patientId" placeholder="按扣款通知上的患者搜（批次性问题可留空）"
                           @select="onNoticePatient"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="就诊类型">
            <el-select v-model="noticeForm.encounterType" clearable style="width: 100%">
              <el-option :value="1" label="门诊"/>
              <el-option :value="2" label="住院"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="被审科室">
            <el-select v-model="noticeForm.deptId" clearable data-testid="deduct-form-dept" filterable style="width: 100%"
                       @change="onNoticeDept">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="责任医师">
            <el-input v-model="noticeForm.doctorName" placeholder="通知上写明的医师姓名"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="违规类型" required>
            <el-select v-model="noticeForm.violationType" data-testid="deduct-form-violation" style="width: 100%">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_VIOLATION_TYPE)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="扣款金额" required>
            <el-input-number v-model="noticeForm.deductAmount" :controls="false" :min="0.01" :precision="2"
                             data-testid="deduct-form-amount" style="width: 100%"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item data-testid="deduct-form-notice-date" label="通知日期" required>
            <el-date-picker v-model="noticeForm.noticeDate" style="width: 100%" type="date"
                            value-format="YYYY-MM-DD"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item data-testid="deduct-form-deadline" label="处理期限" required>
            <el-date-picker v-model="noticeForm.handleDeadline" style="width: 100%" type="date"
                            value-format="YYYY-MM-DD"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="违规事实描述" required>
        <el-input v-model="noticeForm.violationDesc" :rows="3" data-testid="deduct-form-desc"
                  placeholder="飞检问的就是这一句：哪天、哪个项目、怎么个违规法，照通知单原文写"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="noticeForm.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="noticeSubmitting" data-testid="deduct-form-save" type="primary" @click="saveNotice">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {DICT_TYPE} from '@/lib/dict-cache';
import {getDeductDetail, upsertDeduct} from '@/api/insuranceDeduct';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  dicts: {type: Object, default: () => ({})},
  deptOptions: {type: Array, default: () => []},
  inspectionOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictOptions = (type) => props.dicts[type] || [];
const deptNameOf = (id) => props.deptOptions.find((d) => String(d.id) === String(id))?.deptName || '';

const noticeSubmitting = ref(false);
const detailLoading = ref(false);
const emptyNotice = () => ({
  id: null, deductNo: '', sourceType: 1, inspectionId: undefined, patientId: undefined,
  patientName: '', patientNo: '', encounterType: undefined,
  deptId: undefined, deptName: '', doctorName: '', violationType: undefined,
  violationDesc: '', deductAmount: undefined, noticeDate: '', handleDeadline: '', remark: ''
});
const noticeForm = ref(emptyNotice());

watch(visible, async (open) => {
  if (!open) return;
  noticeForm.value = emptyNotice();
  if (!props.row?.id) return;
  detailLoading.value = true;
  try {
    const res = await getDeductDetail(props.row.id);
    // 日期字段后端出参就是 yyyy-MM-dd 文本，直接喂给 el-date-picker 的 value-format
    noticeForm.value = {
      ...emptyNotice(), ...(res.data || {}), id: props.row.id,
      inspectionId: res.data?.inspectionId ? String(res.data.inspectionId) : undefined,
      deptId: res.data?.deptId ? String(res.data.deptId) : undefined,
      patientId: res.data?.patientId ? String(res.data.patientId) : undefined
    };
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    detailLoading.value = false;
  }
});

const onNoticePatient = (p) => {
  if (!p)
    return;
  noticeForm.value.patientName = p.patientName;
  noticeForm.value.patientNo = p.patientNo;
};
const onNoticeDept = (id) => {
  noticeForm.value.deptName = id ? deptNameOf(id) : '';
};
const saveNotice = async () => {
  const f = noticeForm.value;
  if (!f.sourceType || !f.violationType || !f.deductAmount || !f.noticeDate || !f.handleDeadline || !f.violationDesc) {
    ElMessage.warning('来源、违规类型、扣款金额、通知日期、处理期限、违规描述都要填');
    return;
  }
  noticeSubmitting.value = true;
  try {
    await upsertDeduct({
      id: f.id || undefined, sourceType: f.sourceType, inspectionId: f.inspectionId || undefined,
      patientId: f.patientId || undefined, patientName: f.patientName || undefined, patientNo: f.patientNo || undefined,
      encounterType: f.encounterType || undefined, deptId: f.deptId || undefined, deptName: f.deptName || undefined,
      doctorName: f.doctorName || undefined, violationType: f.violationType, violationDesc: f.violationDesc,
      deductAmount: f.deductAmount, noticeDate: f.noticeDate, handleDeadline: f.handleDeadline,
      remark: f.remark || undefined
    });
    ElMessage.success(f.id ? '扣款通知已更新' : '扣款通知已录入');
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    noticeSubmitting.value = false;
  }
};
</script>
