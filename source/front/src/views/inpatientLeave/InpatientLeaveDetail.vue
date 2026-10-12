<template>
  <el-dialog v-model="visible" data-testid="lv-detail-dialog" title="请假单详情（只读）" width="760px">
    <el-form v-loading="loading" :disabled="true" label-width="130px">
      <el-form-item label="单号"><span data-testid="lv-detail-no">{{ dv.leaveNo }}</span></el-form-item>
      <el-form-item label="患者">{{ dv.patientName }} / {{ dv.admissionNo }} / {{ dv.deptName }} {{ dv.wardName }}
        {{ dv.bedNo }}
      </el-form-item>
      <el-form-item label="类别/状态">{{ typeText(dv.leaveType) }} / {{ statusText(dv.leaveStatus) }}
        <el-tag v-if="dv.overdue" class="ml-2" size="small" type="danger">超期 {{ dv.overdueHours }} 小时</el-tag>
      </el-form-item>
      <el-form-item label="请假事由"><span data-testid="lv-detail-reason">{{ dv.reason }}</span></el-form-item>
      <el-form-item label="去向">{{ dv.destination }}</el-form-item>
      <el-form-item label="随行/联系人">{{ dv.companionName }}（{{
          relationText(dv.companionRelation)
        }}）{{ dv.companionPhoneMasked || '' }}
      </el-form-item>
      <el-form-item label="预计离院/返回">{{ fmt(dv.expectedLeaveTime) }} → {{
          fmt(dv.expectedReturnTime)
        }}
      </el-form-item>
      <el-form-item label="申请人">{{ dv.applyBy || '—' }} · {{ fmt(dv.applyTime) }}</el-form-item>
      <el-form-item v-if="dv.doctorAdvice" label="医师意见"><span data-testid="lv-detail-advice">{{
          dv.doctorAdvice
        }}</span></el-form-item>
      <el-form-item v-if="dv.rejectReason" label="拒绝理由">{{ dv.rejectReason }}</el-form-item>
      <el-form-item v-if="dv.actualLeaveTime" label="实际离院">{{ fmt(dv.actualLeaveTime) }}</el-form-item>
      <el-form-item v-if="dv.actualReturnTime" label="实际返回">{{ fmt(dv.actualReturnTime) }}
        {{ dv.returnBy ? `（销假：${dv.returnBy}）` : '' }} {{ dv.returnNote || '' }}
      </el-form-item>
      <el-form-item v-if="dv.confirmName" label="患方签署">
        <div data-testid="lv-detail-confirm">
          <img v-if="dv.confirmSignature" :src="dv.confirmSignature" alt="患方手写签名"
               class="h-11 border border-slate-200"/>
          <div class="text-xs text-slate-500">{{ dv.confirmName }}（{{
              relationText(dv.confirmRelation)
            }}）　{{ dv.confirmPhoneMasked || '' }}　{{ fmt(dv.confirmTime) }}
          </div>
        </div>
      </el-form-item>
      <el-form-item v-if="dv.overdueContactResult" label="超期处置">
        <div data-testid="lv-detail-contact">{{ contactText(dv.overdueContactResult) }} ·
          上报：{{ reportText(dv.reportTo) }} · {{ dv.overdueContactBy || '—' }} {{ fmt(dv.overdueContactTime) }}
          <div class="text-xs text-slate-500">{{ dv.overdueContactNote || '' }}</div>
        </div>
      </el-form-item>
      <el-form-item v-if="dv.cancelReason" label="取消">{{ dv.cancelBy }} · {{ fmt(dv.cancelTime) }} ·
        {{ dv.cancelReason }}
      </el-form-item>
      <el-form-item v-if="dv.signNo" label="电子签名">
          <span class="text-xs text-slate-500" data-testid="lv-detail-sign">签名流水 {{
              dv.signNo
            }}；摘要 {{
              String(dv.contentDigest || '').slice(0, 16)
            }}…；验签 {{
              Number(dv.verifyStatus) === 1 ? '通过' : Number(dv.verifyStatus) === 2 ? '失败' : '未校验'
            }}</span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="lv-detail-close" @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {dictLabelText} from '@/lib/utils'
import {getLeaveById} from '@/api/inpatientLeave'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: null},
  dictType: {type: Array, default: () => []},
  dictStatus: {type: Array, default: () => []},
  dictRelation: {type: Array, default: () => []},
  dictContact: {type: Array, default: () => []},
  dictReport: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const fmt = (t) => (t ? String(t).slice(0, 19) : '—')
const typeText = (v) => dictLabelText(props.dictType, v)
const statusText = (v) => dictLabelText(props.dictStatus, v)
const relationText = (v) => dictLabelText(props.dictRelation, v)
const contactText = (v) => dictLabelText(props.dictContact, v)
const reportText = (v) => dictLabelText(props.dictReport, v)

const loading = ref(false)
const dv = ref({})
watch(visible, async (open) => {
  if (!open || !props.id) return
  loading.value = true
  try {
    const res = await getLeaveById(props.id)
    if (res.code === 200) {
      dv.value = res.data || {}
    } else {
      ElMessage.error(res.message || '加载详情失败')
    }
  } catch (e) {
    ElMessage.error(String((e && e.message) || '加载详情失败'))
  } finally {
    loading.value = false
  }
})
</script>
