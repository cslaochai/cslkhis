<template>
  <!-- ==================== 备案详情（只读） ==================== -->
  <el-dialog v-model="visible" :title="`慢特病备案 ${regDetail?.regNo || ''}`" data-testid="chronic-detail-dialog"
             width="820px">
    <el-form v-loading="loading" :model="regDetail" disabled label-width="110px">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="状态">
            <el-tag :type="statusTagType(regDetail?.displayStatus)" size="small">{{ statusText(regDetail) }}</el-tag>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="患者">{{ regDetail?.patientName }}（{{ regDetail?.patientNo }}）</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="医保卡号">{{ regDetail?.medicalInsuranceNoMasked || '—' }}</el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="病种">{{ regDetail?.diseaseCode }} {{ regDetail?.diseaseName }}</el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类别">{{
              dictText(DICT_TYPE.CHRONIC_DISEASE_TYPE, regDetail?.diseaseType)
            }}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="诊断科室">{{ regDetail?.certifyDeptName || '—' }}</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="诊断医师">{{ regDetail?.certifyDoctorName || '—' }}</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="诊断日期">{{ regDetail?.certifyDate }}</el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="诊断依据">{{ regDetail?.certifyBasis }}</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="备案机构">{{ regDetail?.registerDeptName || '—' }}</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="备案经办人">{{ regDetail?.registerEmpName }}</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="备案日期">{{ regDetail?.registerDate }}</el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="待遇有效期">{{ regDetail ? validPeriod(regDetail) : '' }}</el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="剩余天数">
            {{
              regDetail?.longTerm ? '长期有效' : (regDetail?.remainDays != null ? `${regDetail.remainDays} 天` : '—')
            }}
          </el-form-item>
        </el-col>
        <el-col v-if="regDetail?.cancelReason" :span="24">
          <el-form-item label="注销原因">{{ regDetail.cancelReason }}</el-form-item>
        </el-col>
        <el-col v-if="regDetail?.cancelBy" :span="24">
          <el-form-item label="注销经办">{{ regDetail.cancelBy }} / {{ regDetail.cancelTime }}</el-form-item>
        </el-col>
        <el-col v-if="regDetail?.rejectReason" :span="24">
          <el-form-item label="驳回原因">{{ regDetail.rejectReason }}</el-form-item>
        </el-col>
        <el-col v-if="regDetail?.rejectBy" :span="24">
          <el-form-item label="驳回经办">{{ regDetail.rejectBy }} / {{ regDetail.rejectTime }}</el-form-item>
        </el-col>
        <el-col v-if="regDetail?.remark" :span="24">
          <el-form-item label="备注">{{ regDetail.remark }}</el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 慢特病备案详情弹窗（从 InsuranceChronicView 拆出，只读）。
 * 打开后按 id 拉详情；状态配色/文案/有效期口径与台账一致，由页面传入函数复用。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {getChronicRegDetail} from '@/api/insuranceChronic';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 备案 id：打开后按它拉详情 */
  id: {type: [String, Number], default: ''},
  /** 页面统一加载的字典（台账也在用） */
  dicts: {type: Object, default: () => ({})},
  statusTagType: {type: Function, required: true},
  statusText: {type: Function, required: true},
  validPeriod: {type: Function, required: true},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictText = (type, value) => dictLabelText(props.dicts[type], value);

const loading = ref(false);
const regDetail = ref(null);

watch(visible, async (open) => {
  if (!open) return;
  if (!props.id) return;
  loading.value = true;
  try {
    const res = await getChronicRegDetail(props.id);
    regDetail.value = res.data || null;
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false;
  }
});
</script>
