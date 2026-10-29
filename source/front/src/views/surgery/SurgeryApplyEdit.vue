<template>
  <!-- 发起 / 修改手术申请 -->
  <el-dialog
      v-model="visible"
      :title="applyForm.id ? '修改手术申请' : '发起手术申请'"
      data-testid="p4-op-apply-dialog"
      width="680px"
  >
    <el-form label-width="110px">
      <el-form-item label="在院患者" required>
        <el-select
            v-model="applyForm.admissionId"
            :disabled="!!applyForm.id"
            class="!w-full"
            data-testid="p4-op-apply-admission"
            filterable
            placeholder="选择在院患者"
        >
          <el-option
              v-for="a in admissions"
              :key="a.admissionId"
              :label="admissionLabel(a)"
              :value="String(a.admissionId)"
          />
        </el-select>
      </el-form-item>
      <el-form-item v-if="currentAdmission" label="申请科室">
        <span class="text-slate-700">
          {{ text(currentAdmission.deptName) }} / {{ text(currentAdmission.wardName) }} /
          {{ text(currentAdmission.bedNo) }}床
        </span>
        <span class="ml-2 text-[11px] text-slate-400">申请科室取患者当前科室，服务端写入，不需要填</span>
      </el-form-item>
      <el-form-item label="拟施手术" required>
        <!-- 术式取自 ICD-9-CM-3 码表（13,000+ 条），选中即带出编码；码表未收录的术式仍可手输，
             但手输行没有编码锚点，病案首页与医保上传只能按名称比对 -->
        <el-select
            v-model="applyForm.plannedOperationName"
            allow-create
            class="!w-full"
            data-testid="p4-op-apply-name"
            default-first-option
            filterable
            :loading="opDictLoading"
            placeholder="输入术式名称或编码搜索，未收录可直接输入"
            remote
            :remote-method="searchOperationDict"
            @change="onOperationPicked"
        >
          <el-option
              v-for="o in opDictOptions"
              :key="o.opCode"
              :label="o.opName"
              :value="o.opName"
          >
            <span>{{ o.opName }}</span>
            <span class="float-right text-[11px] text-slate-400">{{ o.opCode }}</span>
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="手术编码">
        <el-input v-model="applyForm.plannedOperationCode" placeholder="选术式自动带出 ICD-9-CM-3，也可手填"/>
      </el-form-item>
      <el-form-item label="手术级别">
        <el-select v-model="applyForm.operationLevel" class="!w-full" clearable placeholder="选择级别">
          <el-option :value="1" label="一级"/>
          <el-option :value="2" label="二级"/>
          <el-option :value="3" label="三级"/>
          <el-option :value="4" label="四级"/>
        </el-select>
      </el-form-item>
      <el-form-item label="切口等级">
        <el-select v-model="applyForm.incisionLevel" class="!w-full" clearable placeholder="选择切口等级">
          <el-option :value="0" label="0类"/>
          <el-option :value="1" label="Ⅰ类"/>
          <el-option :value="2" label="Ⅱ类"/>
          <el-option :value="3" label="Ⅲ类"/>
        </el-select>
      </el-form-item>
      <el-form-item label="麻醉方式">
        <el-select v-model="applyForm.anesthesiaType" class="!w-full" clearable placeholder="选择麻醉方式">
          <el-option :value="1" label="全身麻醉"/>
          <el-option :value="2" label="椎管内麻醉"/>
          <el-option :value="3" label="神经阻滞麻醉"/>
          <el-option :value="4" label="局部麻醉"/>
          <el-option :value="5" label="其他"/>
        </el-select>
      </el-form-item>
      <el-form-item label="术前诊断" required>
        <el-input
            v-model="applyForm.preopDiagnosis"
            :rows="2"
            data-testid="p4-op-apply-diagnosis"
            placeholder="回写病历的「术前诊断」要素取自这里"
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="手术指征" required>
        <el-input
            v-model="applyForm.operationReason"
            :rows="3"
            data-testid="p4-op-apply-reason"
            placeholder="为什么必须开这一刀（如：胆囊结石反复发作伴胆囊壁增厚）"
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="急诊 / 主要">
        <el-radio-group v-model="applyForm.isEmergency" class="mr-4">
          <el-radio :value="0">择期</el-radio>
          <el-radio :value="1">急诊</el-radio>
        </el-radio-group>
        <el-radio-group v-model="applyForm.isMain">
          <el-radio :value="1">主要手术</el-radio>
          <el-radio :value="0">次要手术</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="applyForm.remark" :rows="2" placeholder="可空" type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] leading-5 text-amber-700">
      <el-icon class="mr-1 align-middle">
        <Warning/>
      </el-icon>
      提交后手术<b>尚未排台</b>：手术间、时段、主刀由手术室在「排台」里指定。
      同一次住院只允许一条「主要手术」申请（首页主要手术只能有 1 条）。
    </div>
    <template #footer>
      <el-button v-perm="['ipd:surgery:add','ipd:surgery:edit']" :loading="applySubmitting" data-testid="p4-op-apply-submit"
                 type="primary" @click="submitApply">
        {{ applyForm.id ? '保存修改' : '提交手术申请' }}
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Warning} from '@element-plus/icons-vue';
import {saveOperationApply} from '@/api/inpatientOperation';
import {searchOperationIcd} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // row 为空 = 发起，有值 = 修改（后端是同一个 saveOperationApply，字段与校验必须同一份）
  row: {type: Object, default: null},
  admissions: {type: Array, default: () => []},
  // 发起时默认带上一页面上正在过滤的在院患者
  presetAdmissionId: {type: [String, Number], default: ''},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));
const admissionLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.deptName || a.wardName || '—'}）`;

const applySubmitting = ref(false);
const applyForm = reactive({
  id: '',
  admissionId: '',
  plannedOperationCode: '',
  plannedOperationName: '',
  operationLevel: undefined,
  incisionLevel: undefined,
  anesthesiaType: undefined,
  preopDiagnosis: '',
  operationReason: '',
  isEmergency: 0,
  isMain: 1,
  remark: '',
});
const currentAdmission = computed(() => props.admissions.find((a) => String(a.admissionId) === String(applyForm.admissionId)));

/** 术式候选：远端检索（码表 13,000+ 条，不一次性拉全量） */
const opDictOptions = ref([]);
const opDictLoading = ref(false);
// 输入很快时多次请求会乱序返回，只认最后一次，避免旧结果盖掉新关键词的候选
let opDictSeq = 0;
const searchOperationDict = async (keyword) => {
  const seq = ++opDictSeq;
  opDictLoading.value = true;
  try {
    const res = await searchOperationIcd(keyword || '', 50);
    if (seq === opDictSeq)
      opDictOptions.value = res.data || [];
  } catch (e) /* 检索失败不阻断手输术式 */{
    console.error('手术操作编码检索失败', e);
  } finally {
    if (seq === opDictSeq)
      opDictLoading.value = false;
  }
};
/** 选中码表项才带出编码；手输术式不动已填的编码（历史申请可能只有手输名称） */
const onOperationPicked = (name) => {
  const hit = opDictOptions.value.find((o) => o.opName === name);
  if (hit && hit.opCode)
    applyForm.plannedOperationCode = hit.opCode;
};

watch(visible, (open) => {
  if (!open) return;
  const row = props.row;
  if (row) {
    // 修改：只允许「待排期」（后端也会拒，前端少让人白填一遍）
    applyForm.id = row.id;
    applyForm.admissionId = row.admissionId || '';
    applyForm.plannedOperationCode = row.plannedOperationCode || '';
    applyForm.plannedOperationName = row.plannedOperationName || '';
    applyForm.operationLevel = row.operationLevel;
    applyForm.incisionLevel = row.incisionLevel;
    applyForm.anesthesiaType = row.anesthesiaType;
    applyForm.preopDiagnosis = row.preopDiagnosis || '';
    applyForm.operationReason = row.operationReason || '';
    applyForm.isEmergency = row.isEmergency ?? 0;
    applyForm.isMain = row.isMain ?? 1;
    applyForm.remark = row.remark || '';
  } else {
    applyForm.id = '';
    applyForm.admissionId = props.presetAdmissionId || '';
    applyForm.plannedOperationCode = '';
    applyForm.plannedOperationName = '';
    applyForm.operationLevel = undefined;
    applyForm.incisionLevel = undefined;
    applyForm.anesthesiaType = undefined;
    applyForm.preopDiagnosis = '';
    applyForm.operationReason = '';
    applyForm.isEmergency = 0;
    applyForm.isMain = 1;
    applyForm.remark = '';
  }
  // 修改时把当前术式塞进候选（allow-create 只保证显示，不回填编码）；新申请先拉一批常用术式
  opDictOptions.value = applyForm.plannedOperationName
      ? [{opCode: applyForm.plannedOperationCode || '', opName: applyForm.plannedOperationName}]
      : [];
  if (!applyForm.plannedOperationName)
    searchOperationDict('');
});

const submitApply = async () => {
  if (!applyForm.admissionId) {
    ElMessage.warning('请选择在院患者');
    return;
  }
  if (!applyForm.plannedOperationName.trim()) {
    ElMessage.warning('请填写拟施手术名称');
    return;
  }
  if (!applyForm.preopDiagnosis.trim()) {
    ElMessage.warning('请填写术前诊断（回写病历的术前诊断要素取自这里）');
    return;
  }
  if (!applyForm.operationReason.trim()) {
    ElMessage.warning('请填写手术指征（开一刀是一个医疗决定，必须写清为什么）');
    return;
  }
  applySubmitting.value = true;
  try {
    const res = await saveOperationApply({
      id: applyForm.id || undefined,
      admissionId: applyForm.admissionId,
      plannedOperationCode: applyForm.plannedOperationCode.trim() || undefined,
      plannedOperationName: applyForm.plannedOperationName.trim(),
      operationLevel: applyForm.operationLevel,
      incisionLevel: applyForm.incisionLevel,
      anesthesiaType: applyForm.anesthesiaType,
      preopDiagnosis: applyForm.preopDiagnosis.trim(),
      operationReason: applyForm.operationReason.trim(),
      isEmergency: applyForm.isEmergency,
      isMain: applyForm.isMain,
      remark: applyForm.remark.trim() || undefined,
    });
    ElMessage.success(`${applyForm.id ? '手术申请已修改' : '手术申请已提交'}：${res.data || ''}（等待手术室排台）`);
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    applySubmitting.value = false;
  }
};
</script>
