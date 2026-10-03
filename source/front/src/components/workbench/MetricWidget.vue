<script setup lang="ts">
import { computed } from 'vue'
import { METRIC_SPECS } from '@/lib/workbench-widgets'

/**
 * 通用数字卡：一张卡 = 一组带口径的指标格子。
 *
 * 指标口径（取哪个 key、叫什么名、怎么格式化）登记在 lib/workbench-widgets.js 的
 * METRIC_SPECS，本组件不认卡片编码也不认角色 —— 新增一张数字卡只在注册表里加一段 spec。
 */
const props = defineProps<{
  code: string
  data: Record<string, any> | null
  error?: string | null
}>()

const groups = computed(() => METRIC_SPECS[props.code] || [])
/** 取数失败或还没到：整卡显示「—」而不是 0，0 是"今天确实没有"，— 是"没算出来" */
const ready = computed(() => !!props.data && !props.error)

function value(item: any) {
  if (!ready.value) return '—'
  const v = Number(props.data?.[item.key] ?? 0)
  const pair = Number(props.data?.[item.pairKey ?? ''] ?? 0)
  if (item.format === 'money') return '¥' + v.toLocaleString('zh-CN', {maximumFractionDigits: 2})
  if (item.format === 'pair') return `${v} / ${pair}`
  if (item.format === 'percent') return pair > 0 ? `${Math.round((v / pair) * 100)}%` : '—'
  return v.toLocaleString('zh-CN')
}

function danger(item: any) {
  return !!item.danger && ready.value && Number(props.data?.[item.key] ?? 0) > 0
}
</script>

<template>
  <div class="space-y-4">
    <div v-for="(group, gi) in groups" :key="gi">
      <p v-if="group.title" class="mb-2 text-sm font-medium text-slate-500">{{ group.title }}</p>
      <div class="grid grid-cols-2 gap-3 xl:grid-cols-4">
        <div
            v-for="item in group.items"
            :key="`${item.key}-${item.label}`"
            class="rounded-lg border border-slate-200 bg-slate-50/70 px-4 py-3"
        >
          <p class="text-[15px] text-slate-600">{{ item.label }}</p>
          <p class="mt-1 text-[22px] font-bold leading-tight" :class="danger(item) ? 'text-red-600' : 'text-slate-900'">
            {{ value(item) }}
          </p>
        </div>
      </div>
    </div>
    <p v-if="!groups.length" class="text-sm text-slate-500">
      卡片「{{ code }}」还没有登记指标口径，请在 lib/workbench-widgets.js 的 METRIC_SPECS 补一段。
    </p>
  </div>
</template>
