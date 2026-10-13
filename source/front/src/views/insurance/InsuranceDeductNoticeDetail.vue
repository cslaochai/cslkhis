<template>
  <!-- ==================== 通知单详情（只读 + 留痕） ==================== -->
  <el-dialog v-model="visible" :title="`扣款通知 ${noticeDetail?.deductNo || ''}`" data-testid="deduct-detail-dialog"
             width="880px">
    <el-form v-loading="loading" :model="noticeDetail" disabled label-width="120px">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-form-item label="状态">
            <el-tag :type="statusTagType(noticeDetail?.deductStatus)" size="small">
              {{ dictText(DICT_TYPE.YB_DEDUCT_STATUS, noticeDetail?.deductStatus) }}
            </el-tag>
            <el-tag v-if="noticeDetail?.overdue" class="ml-2" size="small" type="danger">已超期</el-tag>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="来源">{{
              dictText(DICT_TYPE.YB_DEDUCT_SOURCE, noticeDetail?.sourceType)
            }}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="飞检批次">{{ noticeDetail?.inspectionNo || '—' }}</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="患者">{{ noticeDetail?.patientName || '—' }}</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="被审科室">{{ noticeDetail?.deptName || '—' }}</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="责任医师">{{ noticeDetail?.doctorName || '—' }}</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="违规类型">{{
              dictText(DICT_TYPE.YB_VIOLATION_TYPE, noticeDetail?.violationType)
            }}
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="扣款金额">{{ money(noticeDetail?.deductAmount) }}</el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="处理期限">{{ noticeDetail?.handleDeadline }}</el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="违规事实">{{ noticeDetail?.violationDesc }}</el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.appealReason" :span="24">
          <el-form-item label="申诉理由">{{ noticeDetail.appealReason }}</el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.appealMaterial" :span="24">
          <el-form-item label="申诉材料">{{ noticeDetail.appealMaterial }}</el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.appealResult" :span="12">
          <el-form-item label="申诉结果">{{
              dictText(DICT_TYPE.YB_APPEAL_RESULT, noticeDetail.appealResult)
            }}
          </el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.liableDeptName" :span="12">
          <el-form-item label="责任科室">{{ noticeDetail.liableDeptName }}</el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.liableEmpName" :span="12">
          <el-form-item label="责任人">{{ noticeDetail.liableEmpName }}</el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.lossBearType" :span="12">
          <el-form-item label="承担方式">{{
              dictText(DICT_TYPE.YB_LOSS_BEAR, noticeDetail.lossBearType)
            }}
          </el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.bearDeptAmount != null" :span="8">
          <el-form-item label="科室承担">{{ money(noticeDetail.bearDeptAmount) }}</el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.bearEmpAmount != null" :span="8">
          <el-form-item label="个人承担">{{ money(noticeDetail.bearEmpAmount) }}</el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.paybackVoucher" :span="8">
          <el-form-item label="缴回凭证">{{ noticeDetail.paybackVoucher }}</el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.paybackDate" :span="8">
          <el-form-item label="缴回日期">{{ noticeDetail.paybackDate }}</el-form-item>
        </el-col>
        <el-col v-if="noticeDetail?.cancelReason" :span="24">
          <el-form-item label="作废原因">{{ noticeDetail.cancelReason }}</el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div class="font-medium mb-2 text-sm">处理留痕</div>
    <el-timeline data-testid="deduct-log-timeline">
      <el-timeline-item v-for="log in noticeDetail?.logs || []" :key="log.id" :timestamp="log.operateTime"
                        placement="top">
        <div class="text-sm">
          <el-tag size="small" type="info">{{ dictText(DICT_TYPE.YB_DEDUCT_ACTION, log.action) }}</el-tag>
          <span class="ml-2">{{ log.detail }}</span>
          <span v-if="log.amount != null" class="ml-2 text-slate-500">{{ money(log.amount) }}</span>
          <span class="ml-2 text-xs text-slate-400">{{ log.operator }}</span>
        </div>
      </el-timeline-item>
    </el-timeline>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {getDeductDetail} from '@/api/insuranceDeduct';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  dicts: {type: Object, default: () => ({})},
  money: {type: Function, required: true},
  statusTagType: {type: Function, required: true},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictText = (type, value) => dictLabelText(props.dicts[type], value);

const loading = ref(false);
const noticeDetail = ref(null);

watch(visible, async (open) => {
  if (!open || !props.id) return;
  loading.value = true;
  try {
    const res = await getDeductDetail(props.id);
    noticeDetail.value = res.data || null;
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false;
  }
});
</script>
