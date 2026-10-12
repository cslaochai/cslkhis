<template>
  <el-drawer v-model="visible" data-testid="cert-detail-drawer" size="620px" title="死亡证明详情（只读）">
    <el-form :disabled="true" data-testid="cert-detail-body" label-width="120px">
      <el-form-item label="证明编号"><span data-testid="cd-cert-no">{{ dv.certNo }}</span></el-form-item>
      <el-form-item label="状态">{{ certStatusText(dv.certStatus) }} · 上报 {{
          reportStatusText(dv.reportStatus)
        }}
      </el-form-item>
      <el-form-item label="住院号">{{ dv.admissionNo }}（{{ dv.patientName }} / {{ patientGenderText(dv.gender) }} /
        {{ dv.age ?? '—' }}岁）
      </el-form-item>
      <el-form-item label="一般项目">{{ dv.nation || '—' }} · {{ dv.occupation || '—' }} · 出生
        {{ fmtDay(dv.birthDate) }} · 身份证 {{ dv.idCard || '—' }}
      </el-form-item>
      <el-form-item label="死亡时间">{{ fmt(dv.deathTime) }}</el-form-item>
      <el-form-item label="死亡地点">{{ placeText(dv.deathPlace) }} {{ dv.deathDeptName || '' }}
        {{ dv.deathWardName || '' }} 床号 {{ dv.deathBedNo || '—' }}
      </el-form-item>
      <el-form-item label="死亡诊断">{{ dv.clinicalDiagnosis }}</el-form-item>
      <el-form-item label="根本死因">{{ dv.underlyingIcdCode }} {{ dv.underlyingIcdName }}</el-form-item>
      <el-form-item label="Ⅰ部分 死因链">
        <div class="w-full text-sm">
          <div v-for="x in dvChain" :key="x.id" data-testid="cd-chain-row">
            （{{ String.fromCharCode(65 + Number(x.seqNo) - 1) }}）{{ x.icdName }} {{ x.icdCode }} ·
            {{ x.intervalText || '间隔未填' }}
          </div>
          <div v-if="!dvChain.length" class="text-slate-400">未填写</div>
        </div>
      </el-form-item>
      <el-form-item label="Ⅱ部分 其他疾病">
        <div class="w-full text-sm">
          <div v-for="x in dvOther" :key="x.id">{{ x.icdName }} {{ x.icdCode }}</div>
          <div v-if="!dvOther.length" class="text-slate-400">无</div>
        </div>
      </el-form-item>
      <el-form-item label="近亲属">{{ dv.relativeName || '—' }} {{ dv.relativeRelation || '' }}
        {{ dv.relativePhone || '' }}
      </el-form-item>
      <el-form-item label="填表/审核">{{ dv.physicianName }} {{ fmt(dv.fillTime) }} ／ {{ dv.reviewerName || '—' }}
        {{ fmt(dv.reviewTime) }}
      </el-form-item>
      <el-form-item v-if="dv.reviewOpinion" label="审核意见">{{ dv.reviewOpinion }}</el-form-item>
      <el-form-item label="签发/打印">{{ fmt(dv.issueTime) }} ／ {{ dv.printCount ?? 0 }} 次（{{
          dv.printerName || '—'
        }}）
      </el-form-item>
      <el-form-item label="上报时限">{{ fmt(dv.reportDeadline) }} · 上报于 {{ fmt(dv.reportTime) }}</el-form-item>
      <el-form-item v-if="dv.reportNo" label="回执号">{{ dv.reportNo }}</el-form-item>
      <el-form-item v-if="dv.voidReason" label="作废原因">{{ dv.voidReason }}（{{ dv.voidBy }} {{
          fmt(dv.voidTime)
        }}）
      </el-form-item>
      <el-form-item v-if="dv.reissueCertNo" label="重开为">{{ dv.reissueCertNo }}</el-form-item>
      <el-form-item label="死亡出院">{{ dv.deathDischarged ? '已办理' : '未办理（未办理不能签发）' }}</el-form-item>
      <el-form-item v-if="dv.registerNo" label="关联登记">{{ dv.registerNo }}（{{
          registerStatusText(dv.registerStatus)
        }}）
      </el-form-item>
      <el-form-item v-if="dv.reportPayload" label="上报报文">
        <el-input :model-value="dv.reportPayload" :rows="12" data-testid="cd-payload" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="cd-close" @click="visible = false">关闭</el-button>
    </template>
  </el-drawer>
</template>

<script setup>
/**
 * 死亡证明详情（只读抽屉）—— 从 DeathCertificateView.vue 拆出。
 * 父页面只传 modelValue + 证明 id + 字典（列表列同源的 dict 对象），
 * 详情数据按 id 自取（gate 兜住业务失败，打开即见、加载失败弹原文）。
 */
import {computed, ref, watch} from 'vue'
import {dictLabelText} from '@/lib/utils'
import {patientGenderText} from '@/lib/patientGender'
import {fmt, fmtDay, gate} from '@/lib/death'
import {getCertDetail} from '@/api/death'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [Number, String], default: null},
  /** 页面统一加载的 his_death_* 字典（place/certStatus/reportStatus/registerStatus 等） */
  dict: {type: Object, required: true},
})
const emit = defineEmits(['update:modelValue'])
const visible = computed({get: () => props.modelValue, set: (v) => emit('update:modelValue', v)})

const certStatusText = (v) => dictLabelText(props.dict.certStatus, v)
const reportStatusText = (v) => dictLabelText(props.dict.reportStatus, v)
const placeText = (v) => dictLabelText(props.dict.place, v)
const registerStatusText = (v) => dictLabelText(props.dict.registerStatus, v)

const dv = ref({})
watch(() => props.modelValue, async (v) => {
  if (!v) return
  const res = await gate(getCertDetail(props.id), '加载详情失败')
  if (res) dv.value = res.data || {}
})
const dvChain = computed(() => (dv.value.causes || []).filter((x) => Number(x.part) !== 2))
const dvOther = computed(() => (dv.value.causes || []).filter((x) => Number(x.part) === 2))
</script>
