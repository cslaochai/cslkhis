<template>
  <!-- ==================== 备案 新建/编辑 ==================== -->
  <el-dialog v-model="visible" :title="regForm.id ? `修改备案 ${regForm.regNo}` : '登记慢特病备案'"
             data-testid="chronic-form-dialog" width="820px">
    <el-alert v-if="!regForm.id" :closable="false" class="mb-3" title="经办人留空＝记为当前登录人（谁点保存就是谁办的备案）；填别人姓名时备注必须写明原因"
              type="info"/>
    <el-form v-loading="detailLoading" :model="regForm" label-width="110px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="患者" required>
            <PatientSelect v-model="regForm.patientId" data-testid="chronic-form-patient" @select="onRegPatient"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="病种" required>
            <el-select v-model="regForm.catalogId" data-testid="chronic-form-catalog" filterable style="width: 100%"
                       @change="onRegCatalog">
              <el-option v-for="c in catalogOptions" :key="c.id"
                         :label="`${c.diseaseCode} ${c.diseaseName}`" :value="String(c.id)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="诊断科室">
            <el-select v-model="regForm.certifyDeptId" clearable data-testid="chronic-form-dept" filterable style="width: 100%"
                       @change="onRegDept">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="诊断医师">
            <el-input v-model="regForm.certifyDoctorName" placeholder="开诊断的那位医师"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item data-testid="chronic-form-certify-date" label="诊断日期" required>
            <el-date-picker v-model="regForm.certifyDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item data-testid="chronic-form-register-date" label="备案日期" required>
            <el-date-picker v-model="regForm.registerDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item data-testid="chronic-form-valid-start" label="待遇生效日" required>
            <el-date-picker v-model="regForm.validStart" style="width: 100%" type="date" value-format="YYYY-MM-DD"
                            @change="onRegValidStart"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item data-testid="chronic-form-valid-end" label="待遇终止日">
            <div class="flex items-center gap-2 w-full">
              <el-date-picker v-model="regForm.validEnd" :disabled="regForm.longTerm" placeholder="留空＝长期" style="flex: 1"
                              type="date" value-format="YYYY-MM-DD"/>
              <el-checkbox v-model="regForm.longTerm" data-testid="chronic-form-long-term" @change="onRegTermChange">
                长期
              </el-checkbox>
            </div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="备案机构">
            <el-input v-model="regForm.registerDeptName" data-testid="chronic-form-register-dept"
                      placeholder="如 长沙市医疗保障局 / 本院医保科"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="备案经办人">
            <el-input v-model="regForm.registerEmpName" data-testid="chronic-form-register-emp"
                      placeholder="留空＝当前登录人"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="诊断依据" required>
        <el-input v-model="regForm.certifyBasis" :rows="3" data-testid="chronic-form-basis"
                  placeholder="病历摘要 / 检验检查结果 / 出院小结——医保稽核时要拿这句对材料"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="regForm.remark" :rows="2" data-testid="chronic-form-remark"
                  placeholder="外部机构代办时在这里写明，如「长沙市医保中心窗口张XX代办」"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="regSubmitting" data-testid="chronic-form-save" type="primary" @click="submitReg">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 慢特病备案 登记/修改弹窗（从 InsuranceChronicView 拆出，row 为空＝登记，有值＝修改）。
 * 病种候选与科室候选只在表单里用，弹窗打开时自取（原页内 onMounted 预载的逻辑随迁）。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {getDepartmentSelectList} from '@/api/system';
import {getChronicCatalogSelectList, getChronicRegDetail, upsertChronicReg} from '@/api/insuranceChronic';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 编辑时传入的行数据（取其 id 拉详情）；为空表示登记新备案 */
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

// ==================== 科室下拉（诊断科室要全院范围） ====================
const deptOptions = ref([]);
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({scope: 'ALL'});
    deptOptions.value = res.data || [];
  } catch (error) {
    ElMessage.error(error?.message || '科室候选加载失败');
  }
};
const deptNameOf = (id) => deptOptions.value.find((d) => String(d.id) === String(id))?.deptName || '';
// ==================== 病种目录候选（备案表单下拉） ====================
const catalogOptions = ref([]);
const loadCatalogOptions = async () => {
  try {
    const res = await getChronicCatalogSelectList();
    catalogOptions.value = res.data || [];
  } catch (error) {
    ElMessage.error(error?.message || '病种候选加载失败');
  }
};

const regSubmitting = ref(false);
const detailLoading = ref(false);
const emptyReg = () => ({
  id: null, regNo: '', patientId: undefined, patientName: '',
  catalogId: undefined, certifyDeptId: undefined, certifyDeptName: '',
  certifyDoctorName: '', certifyDate: '', certifyBasis: '',
  registerDeptName: '', registerEmpName: '', registerDate: '',
  validStart: '', validEnd: '', longTerm: false, remark: ''
});
const regForm = ref(emptyReg());
// 本地日期字符串：toISOString 走 UTC，早上 8 点前会把「今天」写成昨天
const fmtDate = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
const today = () => fmtDate(new Date());

watch(visible, async (open) => {
  if (!open) return;
  // 原页内 openRegCreate/openRegEdit 是「先取数再开弹窗」，拆出后统一为「开弹窗再取数」
  regForm.value = emptyReg();
  detailLoading.value = true;
  try {
    await Promise.all([loadCatalogOptions(), loadDeptsOnce()]);
    if (props.row?.id) {
      const res = await getChronicRegDetail(props.row.id);
      const d = res.data || {};
      regForm.value = {
        ...emptyReg(), ...d,
        id: props.row.id,
        patientId: d.patientId ? String(d.patientId) : undefined,
        catalogId: d.catalogId ? String(d.catalogId) : undefined,
        certifyDeptId: d.certifyDeptId ? String(d.certifyDeptId) : undefined,
        // 长期单出参 validEnd 为 null，el-date-picker 要的是空串而不是 null
        validEnd: d.validEnd || '',
        longTerm: !d.validEnd
      };
    } else {
      regForm.value.registerDate = today();
      regForm.value.certifyDate = today();
    }
  } catch (error) {
    ElMessage.error(error?.message || '备案详情加载失败');
    visible.value = false;
  } finally {
    detailLoading.value = false;
  }
});

// 科室候选整页只回一次（原来也是页面级预载），失败后允许下次打开重试
let deptsLoaded = false;
const loadDeptsOnce = async () => {
  if (deptsLoaded) return;
  await loadDepts();
  deptsLoaded = true;
};

const onRegPatient = (p) => {
  if (p)
    regForm.value.patientName = p.patientName;
};
const onRegDept = (id) => {
  regForm.value.certifyDeptName = id ? deptNameOf(id) : '';
};
/** 选病种时把目录的默认有效期带出来，长期病种就留空 */
const onRegCatalog = (id) => {
  const c = catalogOptions.value.find((x) => String(x.id) === String(id));
  if (!c)
    return;
  if (c.defaultValidMonths) {
    regForm.value.longTerm = false;
    if (regForm.value.validStart)
      fillValidEndByMonths(c.defaultValidMonths);
  } else {
    regForm.value.longTerm = true;
    regForm.value.validEnd = '';
  }
};
/** 先生效日后选病种还是先选病种都要能带出默认终止日，所以两边都挂一次 */
const onRegValidStart = () => {
  const c = catalogOptions.value.find((x) => String(x.id) === String(regForm.value.catalogId));
  if (c?.defaultValidMonths && regForm.value.validStart && !regForm.value.longTerm && !regForm.value.validEnd) {
    fillValidEndByMonths(c.defaultValidMonths);
  }
};
const fillValidEndByMonths = (months) => {
  const start = new Date(`${regForm.value.validStart}T00:00:00`);
  start.setMonth(start.getMonth() + Number(months));
  regForm.value.validEnd = fmtDate(start);
};
const onRegTermChange = (longTerm) => {
  if (longTerm)
    regForm.value.validEnd = '';
};
const submitReg = async () => {
  const f = regForm.value;
  if (!f.patientId || !f.catalogId || !f.certifyDate || !f.certifyBasis || !f.registerDate || !f.validStart) {
    ElMessage.warning('患者、病种、诊断日期、诊断依据、备案日期、待遇生效日都要填');
    return;
  }
  // 后端只在「新建且姓名不是当前登录人」时强制备注，这里提前拦一遍省一次 400
  if (!f.id && f.registerEmpName && !f.remark) {
    ElMessage.warning('改成本院之外的经办人时，备注要写明原因（如「长沙市医保中心窗口张XX代办」）');
    return;
  }
  regSubmitting.value = true;
  try {
    await upsertChronicReg({
      id: f.id || undefined,
      patientId: f.patientId,
      catalogId: f.catalogId,
      certifyDeptId: f.certifyDeptId || undefined,
      certifyDeptName: f.certifyDeptName || undefined,
      certifyDoctorName: f.certifyDoctorName || undefined,
      certifyDate: f.certifyDate,
      certifyBasis: f.certifyBasis,
      registerDeptName: f.registerDeptName || undefined,
      registerEmpName: f.registerEmpName || undefined,
      registerDate: f.registerDate,
      validStart: f.validStart,
      validEnd: f.longTerm ? undefined : (f.validEnd || undefined),
      remark: f.remark || undefined
    });
    ElMessage.success(f.id ? '备案已更新' : '备案已登记');
    visible.value = false;
    emit('saved');
  } catch (error) {
    // 「同一患者同一病种已有有效备案」这类判定只在后端有据（要查历史单号与待遇期），
    // 拒绝理由必须原样弹出来，否则用户只看到弹窗不动
    ElMessage.error(error?.message || '备案保存失败');
  } finally {
    regSubmitting.value = false;
  }
};
</script>
