<template>
  <!-- 详情（只读，行点击打开） -->
  <el-dialog v-model="visible" data-testid="cn-detail-dialog" title="通知单详情（只读）" width="720px">
    <el-form :disabled="true" label-width="110px">
      <el-form-item label="单号"><span data-testid="cn-detail-no">{{ dv.noticeNo }}</span></el-form-item>
      <el-form-item label="患者">{{ dv.patientName }} / {{ dv.admissionNo }} / {{ dv.deptName }} {{ dv.wardName }}
        {{ dv.bedNo }}
      </el-form-item>
      <el-form-item label="类别/神志">{{ typeText(dv.noticeType) }} / {{
          consText(dv.consciousnessStatus)
        }}
      </el-form-item>
      <el-form-item label="目前诊断"><span data-testid="cn-detail-diagnosis">{{ dv.clinicalDiagnosis }}</span>
      </el-form-item>
      <el-form-item label="病情及危险因素">
        <el-input :model-value="dv.conditionDesc" :rows="3" type="textarea"/>
      </el-form-item>
      <el-form-item label="预警事项">
        <el-input :model-value="dv.warningMatters" :rows="3" type="textarea"/>
      </el-form-item>
      <el-form-item label="医方措施">
        <el-input :model-value="dv.doctorMeasures" :rows="2" type="textarea"/>
      </el-form-item>
      <el-form-item label="告知时间">{{ fmt(dv.notifyTime) }}</el-form-item>
      <el-form-item label="告知/见证医师">{{ dv.doctorName }} / {{ dv.witnessDoctorName || '—' }}</el-form-item>
      <el-form-item label="状态">
        <el-tag :type="statusTag(Number(dv.noticeStatus))" size="small">{{ statusText(dv.noticeStatus) }}</el-tag>
        <el-tag class="ml-2" size="small" type="info">{{ signTagText(dv.signStatus) }}</el-tag>
      </el-form-item>
      <el-form-item v-if="Number(dv.noticeStatus) === NS.ACKED" label="签收信息">
        <div data-testid="cn-detail-ack">
          <img v-if="dv.signerSignature" :src="dv.signerSignature" alt="家属手写签名"
               class="h-11 border border-slate-200"/>
          <div class="text-xs text-slate-500">{{ dv.signerName }}（{{
              relationText(dv.signerRelation)
            }}）　{{ dv.signerIdCardMasked || '' }}　{{ dv.signerPhoneMasked || '' }}　{{ fmt(dv.acknowledgeTime) }}
          </div>
        </div>
      </el-form-item>
      <el-form-item v-if="dv.signNo" label="电子签名">
        <span class="text-xs text-slate-500" data-testid="cn-detail-sign">签名流水 {{
            dv.signNo
          }}；摘要 {{
            String(dv.contentDigest || '').slice(0, 16)
          }}…；验签 {{
            Number(dv.verifyStatus) === 1 ? '通过' : Number(dv.verifyStatus) === 2 ? '失败' : '未校验'
          }}</span>
      </el-form-item>
      <el-form-item v-if="dv.noticeStatus === NS.VOID" label="作废">{{ dv.voidBy }} · {{ fmt(dv.voidTime) }} ·
        {{ dv.voidReason }}
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button data-testid="cn-detail-close" @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 通知单详情（只读）：打开时按 id 自拉 getNoticeById；
 * 字典（类别/状态/神志/关系）列表页也在用，由父页面加载一次传入。
 * 页面只渲染后端给的脱敏字段（signerIdCardMasked/signerPhoneMasked），不做任何遮码。
 */
import {computed, ref, watch} from 'vue'
import {dictLabelText} from '@/lib/utils'
import {getNoticeById} from '@/api/criticalNotice'
import {fmt, gate, NS, signTagText, statusTag} from '@/lib/criticalNotice'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 通知单 id（雪花 id，字符串）
  id: {type: [String, Number], default: null},
  // 字典：列表列与详情共用，父页面加载一次传入
  dict: {type: Object, required: true},
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const typeText = (v) => dictLabelText(props.dict.type, v)
const statusText = (v) => dictLabelText(props.dict.status, v)
const consText = (v) => dictLabelText(props.dict.consciousness, v)
const relationText = (v) => dictLabelText(props.dict.relation, v)

const dv = ref({})
watch(() => props.modelValue, async (v) => {
  if (!v) return
  const res = await gate(getNoticeById(props.id), '加载详情失败')
  if (res) dv.value = res.data || {}
})
</script>
