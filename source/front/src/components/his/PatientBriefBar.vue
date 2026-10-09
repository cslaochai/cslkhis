<script setup lang="ts">
import {computed} from 'vue'
import {Coin, Setting, Tickets} from '@element-plus/icons-vue'
import {patientGenderText} from '@/lib/patientGender'
import {queueVisitBadgeOf} from '@/lib/statusColor'
import {shortQueueNo} from '@/lib/utils'

const props = defineProps<{
  patient: any
  detail?: any
  record?: any
  /** 到达/签到文案，由页面算好传入（例如 "08:12 到达"） */
  arriveText?: string
}>()

const emit = defineEmits<{
  (e: 'open-charge'): void
  (e: 'open-tags'): void
  /** 打开患者详情弹窗：主档 + 健康档案 + 历次就诊（跨门诊/住院/医技，按就诊次组织） */
  (e: 'open-detail'): void
}>()

const hasAllergy = computed(() => {
  const a = props.record?.allergyHistory
  return !!a && a !== '无' && a !== '否认'
})

/**
 * 号别徽章：口径见 lib/statusColor.queueVisitBadgeOf —— 急诊号优先显「急诊号」，
 * 其余按 visit_type 1 初诊 / 2 复诊；队列行没挂到挂号（visitType 为 null）时显「未标注」，
 * 不许回落到「初诊」。
 */
const visitStyle = computed(() => queueVisitBadgeOf(props.patient))

const insuranceLabel = computed(() => props.detail?.medicalInsuranceType || '')
</script>

<template>
  <div class="flex h-[var(--his-briefbar-h)] shrink-0 items-center gap-3 rounded-lg border border-slate-200 bg-white px-4 shadow-sm">
    <span class="text-base font-bold text-slate-900">{{ patient?.patientName }}</span>
    <span class="text-sm text-slate-500">
      {{ patientGenderText(patient?.gender) }} · {{ patient?.age }}岁
    </span>
    <el-tag size="small" :type="visitStyle.tagType" effect="plain">
      {{ visitStyle.label }}
    </el-tag>
    <span class="rounded bg-slate-100 px-1.5 py-0.5 text-xs text-slate-600">{{ shortQueueNo(patient?.queueNo) || '-' }}</span>
    <span v-if="hasAllergy"
          class="rounded bg-red-50 px-1.5 py-0.5 text-xs font-medium text-red-600">
      ⚠ 过敏：{{ record.allergyHistory }}
    </span>
    <span v-if="insuranceLabel" class="rounded bg-blue-50 px-1.5 py-0.5 text-xs text-blue-600">
      医保：{{ insuranceLabel }}
    </span>
    <span class="text-xs text-slate-400">就诊号 {{ patient?.patientNo }}</span>
    <span v-if="arriveText" class="text-xs text-emerald-600">{{ arriveText }}</span>

    <div class="ml-auto flex items-center gap-2">
      <el-tooltip content="患者标签设置" placement="bottom">
        <el-button :icon="Setting" size="small" circle @click="emit('open-tags')"/>
      </el-tooltip>
      <el-tooltip content="患者主档 / 健康档案 / 历次就诊（跨门诊·住院·医技）" placement="bottom">
        <el-button size="small" :icon="Tickets" @click="emit('open-detail')">患者详情</el-button>
      </el-tooltip>
      <el-button size="small" :icon="Coin" @click="emit('open-charge')">费用</el-button>
    </div>
  </div>
</template>
