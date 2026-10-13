<template>
  <el-drawer v-model="visible" size="480px" title="报卡详情">
    <div class="space-y-2 text-sm" data-testid="detail-body">
      <div class="flex"><span class="w-24 text-gray-400">报卡编号</span><span
          data-testid="detail-no">{{ detailCard().reportNo }}</span></div>
      <div class="flex"><span class="w-24 text-gray-400">患者</span><span>{{
          detailCard().patientName
        }} / {{ detailCard().patientNo }}</span></div>
      <div class="flex"><span class="w-24 text-gray-400">病种</span><span>{{
          detailCard().infectiousClassText
        }} · {{ detailCard().diseaseName }}（{{ detailCard().icd10 }}）</span></div>
      <div class="flex"><span class="w-24 text-gray-400">状态</span><span>{{
          detailCard().reportStatusText
        }}（第 {{ detailCard().reportCount }} 次报卡）</span></div>
      <div class="flex"><span class="w-24 text-gray-400">时限</span><span>{{
          fmtTime(detailCard().reportDeadline)
        }}</span></div>
      <div class="flex"><span class="w-24 text-gray-400">填卡</span><span>{{
          detailCard().reportByName
        }} · {{ fmtTime(detailCard().reportTime) }}</span></div>
      <div v-if="detailCard().auditByName" class="flex"><span class="w-24 text-gray-400">审核</span><span>{{
          detailCard().auditByName
        }} · {{ fmtTime(detailCard().auditTime) }} {{ detailCard().auditOpinion }}</span></div>
      <div v-if="detailCard().returnReason" class="flex"><span class="w-24 text-gray-400">退报原因</span><span
          class="text-red-500">{{ detailCard().returnReason }}</span></div>
      <div v-if="detailCard().clinicalDesc">
        <div class="text-gray-400">临床摘要</div>
        <div>{{ detailCard().clinicalDesc }}</div>
      </div>
      <div v-if="detailCard().directPayload">
        <div class="text-gray-400 mt-2">直报报文（{{ fmtTime(detailCard().directTime) }}）</div>
        <el-input :model-value="detail.directPayloadPreview" :rows="12" data-testid="detail-payload" readonly
                  type="textarea"/>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
/**
 * 传染病报卡详情抽屉（自 InfectiousReportView 拆出）
 *
 * 按 id 自取 getReportDetail（出参含 card 与直报报文预览 directPayloadPreview），
 * 打开即拉、状态文案取后端 *Text，前端不另写映射。
 */
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getReportDetail} from '@/api/infectiousReport'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 报卡ID
  id: {type: [String, Number], default: null},
})

const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—')

const detail = ref({})
const detailCard = () => detail.value.card || {}

// 打开时按 id 自取详情（与拆分前 showDetail 的取数口径一致）
watch(() => props.modelValue, async (v) => {
  if (!v) return
  detail.value = {}
  if (!props.id) return
  try {
    const res = await getReportDetail(props.id)
    if (res.code === 200) {
      detail.value = res.data || {}
    } 
  } catch (e) {
    console.error(e)
    
  }
})
</script>
