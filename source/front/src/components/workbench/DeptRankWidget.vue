<script setup lang="ts">
import {computed} from 'vue'

/**
 * 科室就诊量排行卡：横向条形排行，纯 div 画（不引图表库）。
 * 行数据 {deptName, cnt} 来自后端 SQL 别名，值以大字号深色呈现（表格可读性口径）。
 */
const props = defineProps<{
    code: string
    data: Record<string, any> | null
    error?: string | null
}>()

const ready = computed(() => !!props.data && !props.error)
const items = computed<any[]>(() => props.data?.items || [])
/** 条长按本次最大值归一；至少取 1，避免全 0 时除零 */
const maxCnt = computed(() => Math.max(1, ...items.value.map(x => Number(x.cnt || 0))))
const totalCount = computed(() => items.value.reduce((s, x) => s + Number(x.cnt || 0), 0))
</script>

<template>
  <div>
    <div v-if="!ready" class="py-10 text-center text-[15px] text-slate-400">—</div>
    <div v-else-if="items.length === 0" class="py-10 text-center text-[15px] text-slate-400">
      今日暂无就诊数据
    </div>

    <div v-else>
      <p class="mb-3 text-[15px] text-slate-600">
        今日共 <span class="text-[18px] font-bold text-slate-900">{{ totalCount }}</span> 人次
      </p>
      <div class="space-y-2.5">
        <div v-for="(item, i) in items" :key="i" class="flex items-center gap-3">
          <span class="w-5 shrink-0 text-right text-[14px] font-medium text-slate-400">{{ i + 1 }}</span>
          <span class="w-28 shrink-0 truncate text-[15px] text-slate-700" :title="item.deptName">{{ item.deptName }}</span>
          <div class="relative h-6 flex-1 overflow-hidden rounded bg-slate-100">
            <div
                class="h-full rounded bg-[#1269B5] opacity-80 transition-all duration-500"
                :style="{width: `${(Number(item.cnt || 0) / maxCnt) * 100}%`}"
            />
          </div>
          <span class="w-12 shrink-0 text-right text-[16px] font-bold text-slate-900">{{ item.cnt }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
