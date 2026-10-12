<template>
  <el-drawer v-model="visible" data-testid="reg-detail-drawer" size="560px" title="死亡登记详情（只读）">
    <el-form :disabled="true" data-testid="reg-detail-body" label-width="120px">
      <el-form-item label="登记号">{{ rd.registerNo }}</el-form-item>
      <el-form-item label="状态">{{ registerStatusText(rd.registerStatus) }}</el-form-item>
      <el-form-item label="死者">{{ rd.patientName }} · 住院号 {{ rd.admissionNo }} · 死亡 {{
          fmt(rd.deathTime)
        }}
      </el-form-item>
      <el-form-item label="死亡科室/床位">{{ rd.deathDeptName || '—' }} / {{ rd.deathBedNo || '—' }}</el-form-item>
      <el-form-item label="关联证明">{{ rd.certNo || '未开证' }}（{{ certStatusText(rd.certStatus) }}）</el-form-item>
      <el-form-item label="死亡类型">{{ deathTypeText(rd.deathType) }}</el-form-item>
      <el-form-item label="公安报案">{{
          Number(rd.policeFlag) === 1 ? `已报：${rd.policeOrg || ''} ${rd.policeCaseNo || ''} ${fmt(rd.policeReportTime)}` : '未报'
        }}
      </el-form-item>
      <el-form-item label="法医出具">{{ Number(rd.forensicFlag) === 1 ? '是' : '否' }}</el-form-item>
      <el-form-item label="尸体处理">{{ disposalText(rd.bodyDisposal) }} · {{ rd.bodyUnit || '—' }} · 移出
        {{ fmt(rd.bodyTransportTime) }}
      </el-form-item>
      <el-form-item label="家属">{{ rd.relativeName || '—' }} {{ rd.relativeRelation || '' }}
        {{ rd.relativePhone || '' }}
      </el-form-item>
      <el-form-item label="领取联次">{{ copiesText(rd.receivedCopies) }} · {{ fmt(rd.receiveTime) }}</el-form-item>
      <el-form-item label="纠纷">{{
          Number(rd.disputeFlag) === 1 ? `有：${rd.disputeDesc || ''}` : '无'
        }}
      </el-form-item>
      <el-form-item label="登记人/时间">{{ rd.registrarName || '—' }} · {{ fmt(rd.registerTime) }}</el-form-item>
      <el-form-item v-if="rd.voidReason" label="作废原因">{{ rd.voidReason }}（{{ fmt(rd.voidTime) }}）</el-form-item>
      <el-form-item label="备注">{{ rd.remark || '—' }}</el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="rd-close" @click="visible = false">关闭</el-button>
    </template>
  </el-drawer>
</template>

<script setup>
/**
 * 死亡登记详情（只读抽屉）—— 从 DeathCertificateView.vue 拆出。
 * 父页面只传 modelValue + 登记 id + 字典（列表列同源的 dict 对象），
 * 详情数据按 id 自取（gate 兜住业务失败，打开即见、加载失败弹原文）。
 */
import {computed, ref, watch} from 'vue'
import {dictLabelText} from '@/lib/utils'
import {fmt, gate} from '@/lib/death'
import {getRegisterDetail} from '@/api/death'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [Number, String], default: null},
  /** 页面统一加载的 his_death_* 字典（registerStatus/certStatus/deathType/disposal/copies 等） */
  dict: {type: Object, required: true},
})
const emit = defineEmits(['update:modelValue'])
const visible = computed({get: () => props.modelValue, set: (v) => emit('update:modelValue', v)})

const registerStatusText = (v) => dictLabelText(props.dict.registerStatus, v)
const certStatusText = (v) => dictLabelText(props.dict.certStatus, v)
const deathTypeText = (v) => dictLabelText(props.dict.deathType, v)
const disposalText = (v) => dictLabelText(props.dict.disposal, v)
const copiesText = (v) => (v ? String(v).split(',').map((x) => dictLabelText(props.dict.copies, x)).join('、') : '—')

const rd = ref({})
watch(() => props.modelValue, async (v) => {
  if (!v) return
  const res = await gate(getRegisterDetail(props.id), '加载详情失败')
  if (res) rd.value = res.data || {}
})
</script>
