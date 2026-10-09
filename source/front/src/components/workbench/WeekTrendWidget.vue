<script setup lang="ts">
import {computed} from 'vue'

const props = defineProps<{
    code: string
    data: Record<string, any> | null
    error?: string | null
}>()

const ready = computed(() => !!props.data && !props.error)
const items = computed<any[]>(() => props.data?.items || [])
const maxCnt = computed(() => Math.max(1, ...items.value.map(x => Number(x.cnt || 0))))
const totalCount = computed(() => items.value.reduce((s, x) => s + Number(x.cnt || 0), 0))

function dayLabel(date: string) {
    return (date || '').slice(5) || '—'
}
</script>

<template>
  <div>
    <div v-if="!ready" class="py-10 text-center text-[15px] text-slate-400">—</div>
    <div v-else>
      <p class="mb-3 text-[15px] text-slate-600">
        近 7 日共 <span class="text-[18px] font-bold text-slate-900">{{ totalCount }}</span> 人次
      </p>
      <div class="flex items-end gap-2" style="height: 190px">
        <div v-for="(item, i) in items" :key="i" class="flex h-full flex-1 flex-col items-center justify-end gap-1.5">
          <span class="text-[15px] font-bold text-slate-800">{{ item.cnt }}</span>
          <div class="relative w-full max-w-[46px] flex-1 rounded-t bg-slate-100">
            <div
                class="absolute bottom-0 w-full max-w-[46px] rounded-t bg-[#0E9488] opacity-80 transition-all duration-500"
                :style="{height: `${(Number(item.cnt || 0) / maxCnt) * 100}%`}"
            />
          </div>
          <span class="text-[14px] text-slate-500">{{ dayLabel(item.date) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>
