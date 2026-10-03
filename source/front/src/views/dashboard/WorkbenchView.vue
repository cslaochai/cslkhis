<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {Refresh} from '@element-plus/icons-vue'
import {getWorkbenchData} from '@/api/workbench'
import {loadWorkbenchConfig, workbenchConfigEpoch} from '@/lib/workbench-config'
import {WIDGET_REGISTRY} from '@/lib/workbench-widgets'
import {hasPerm} from '@/lib/perm'

/**
 * 门户工作台（所有角色共用的唯一首页）
 *
 * 这里没有任何角色分支：卡片清单由 `/workbench/config` 按「角色配置勾选 ∩ 权限码命中」算好，
 * 取数由 `/workbench/data` 一次聚合返回，页面只负责按 widgetCode 查组件注册表渲染。
 * 谁该看到哪几张卡，去「系统管理 → 工作台配置」里改，不改代码。
 */

interface WidgetConf {
    id: string
    widgetCode: string
    widgetName: string
    area: string
    apiKey: string
    permission: string | null
    span: number | null
    sortOrder: number | null
    status: number
    visible: number
}

/** 栅格总列数，与 sys_workbench_widget.default_span 同一口径（24 列便于 1/2、1/3、1/4 拆分） */
const GRID_COLS = 24

const loading = ref(true)
const loadError = ref('')
const widgets = ref<WidgetConf[]>([])
/** widgetCode -> {data, error}，取数失败也占位，让卡片显式画出失败态而不是整卡消失 */
const dataMap = ref<Record<string, any>>({})

/** 已上线且前端注册过组件的卡（未登记的码在取数那一步点名，不画空白卡） */
const cards = computed(() =>
    widgets.value.filter(w => WIDGET_REGISTRY[w.widgetCode] &&
        // 后端已按权限筛过，这里是第二道：/auth/info 的权限集合若不含该码，卡片的数据接口必然 403
        hasPerm(w.permission))
)

function gridColumn(span: number | null) {
    const n = Math.min(Math.max(Number(span) || GRID_COLS, 1), GRID_COLS)
    return `span ${n}`
}

async function loadAll(force = false) {
    loading.value = true
    loadError.value = ''
    try {
        const cfg = await loadWorkbenchConfig(force)
        widgets.value = cfg.widgets as WidgetConf[]
        // 后端注册表里配了卡、前端却没有组件 = 卡片静默消失，必须在控制台点名
        widgets.value.forEach(w => {
            if (!WIDGET_REGISTRY[w.widgetCode]) {
                console.warn(`[workbench] 卡片 ${w.widgetCode} 没有前端组件，请在 lib/workbench-widgets.js 登记`)
            }
        })
        await loadData()
    } catch (e: any) {
        console.error('工作台加载失败：', e)
        loadError.value = e?.message || '工作台配置拉取失败，请检查后端服务'
    } finally {
        loading.value = false
    }
}

async function loadData() {
    try {
        const res = await getWorkbenchData()
        const map: Record<string, any> = {}
        for (const item of res?.data || []) {
            map[item.code] = {data: item.data, error: item.error}
        }
        dataMap.value = map
    } catch (e: any) {
        // 取数整体失败不弹错误页：卡片各自显示「—」，配置仍然可用
        console.error('工作台取数失败：', e)
        dataMap.value = {}
    }
}

onMounted(() => loadAll())
// 切角色/科室会清掉配置缓存并推进代次，常驻实例要自己重跑一遍。
// 这里绝不能再调 clearWorkbenchConfigCache()：代次是自增的，清一次就再触发一次本回调，无限循环。
// 不带 force：Header 切角色时已经重拉过一份并写入缓存，再强制一次就是首屏第二次相同请求。
watch(workbenchConfigEpoch, () => loadAll())
</script>

<template>
  <div class="space-y-5">
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-2xl font-bold text-slate-900">工作台</h1>
        <p class="mt-1 text-sm text-slate-500">按当前角色汇总的待办与今日运行概览</p>
      </div>
      <button
          type="button"
          class="flex shrink-0 items-center gap-1.5 rounded-lg border border-slate-300 px-3 py-1.5 text-[15px] text-slate-600 transition-colors hover:border-[#1269B5] hover:text-[#1269B5]"
          :disabled="loading"
          @click="loadAll(true)"
      >
        <Refresh class="h-4 w-4"/>
        刷新
      </button>
    </div>

    <div v-if="loading" class="rounded-lg border border-slate-200 bg-white p-10 text-center text-[15px] text-slate-400 shadow-sm">
      加载中…
    </div>

    <div v-else-if="loadError" class="rounded-lg border border-red-200 bg-red-50 p-10 text-center shadow-sm">
      <p class="text-[15px] text-red-600">{{ loadError }}</p>
      <button
          type="button"
          class="mt-3 rounded-md border border-red-300 px-3 py-1 text-[15px] text-red-600 hover:bg-red-100"
          @click="loadAll(true)"
      >重试</button>
    </div>

    <div v-else-if="cards.length === 0" class="rounded-lg border border-slate-200 bg-white p-10 text-center text-[15px] text-slate-400 shadow-sm">
      当前角色还没有可展示的卡片，请联系管理员在「系统管理 → 工作台配置」中维护
    </div>

    <div v-else class="wb-grid">
      <section
          v-for="w in cards"
          :key="w.widgetCode"
          class="wb-card rounded-lg border border-slate-200 bg-white p-5 shadow-sm"
          :style="{gridColumn: gridColumn(w.span)}"
      >
        <h3 class="mb-3.5 text-[17px] font-semibold text-slate-800">{{ w.widgetName }}</h3>
        <component
            :is="WIDGET_REGISTRY[w.widgetCode].component"
            :code="w.widgetCode"
            :data="dataMap[w.widgetCode]?.data ?? null"
            :error="dataMap[w.widgetCode]?.error ?? null"
            v-bind="WIDGET_REGISTRY[w.widgetCode].props || {}"
            @refresh="loadData"
        />
      </section>
    </div>
  </div>
</template>

<style scoped>
/*
 * 24 列栅格：卡片宽度由后端 default_span 决定，页面上不许再写死 col-span。
 * 窄屏放不下 24 列，1280px 以下一律整卡独占一行（横向条形/九宫格自己会缩）。
 */
.wb-grid {
    display: grid;
    grid-template-columns: repeat(24, minmax(0, 1fr));
    gap: 1.25rem;
}

.wb-card {
    min-width: 0;
}

@media (max-width: 1279px) {
    .wb-grid > .wb-card {
        grid-column: 1 / -1 !important;
    }
}
</style>
