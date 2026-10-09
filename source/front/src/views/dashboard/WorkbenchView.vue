<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {Refresh} from '@element-plus/icons-vue'
import {getWorkbenchData} from '@/api/workbench'
import {loadWorkbenchConfig, workbenchConfigEpoch} from '@/lib/workbench-config'
import {WIDGET_REGISTRY} from '@/lib/workbench-widgets'
import {hasPerm} from '@/lib/perm'

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
/** 本页最近一次取数时刻，用于顶栏的「何时更新」；不做自动轮询，所以必须由人确认 */
const loadedAt = ref('')

/** 已上线且前端注册过组件的卡（未登记的码在取数那一步点名，不画空白卡） */
const cards = computed(() =>
    widgets.value.filter(w => WIDGET_REGISTRY[w.widgetCode] &&
        // 后端已按权限筛过，这里是第二道：/auth/info 的权限集合若不含该码，卡片的数据接口必然 403
        hasPerm(w.permission))
)

/** 顶栏左侧文案：任何状态下都有事实可读，避免动作按钮孤零零挂在页面顶部 */
const metaText = computed(() => {
    if (loading.value) return '正在载入…'
    if (loadError.value) return '配置载入失败'
    if (!loadedAt.value) return `共 ${cards.value.length} 张卡片`
    return `共 ${cards.value.length} 张卡片 · ${loadedAt.value} 更新`
})

function gridColumn(span: number | null) {
    const n = Math.min(Math.max(Number(span) || GRID_COLS, 1), GRID_COLS)
    return `span ${n}`
}

async function loadAll(force = false) {
    loading.value = true
    loadError.value = ''
    loadedAt.value = ''
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
        loadedAt.value = new Date().toLocaleTimeString('zh-CN', {hour: '2-digit', minute: '2-digit'})
    } catch (e: any) {
        // 取数整体失败不弹错误页：卡片各自显示「—」，配置仍然可用
        console.error('工作台取数失败：', e)
        dataMap.value = {}
        loadedAt.value = ''
    }
}

onMounted(() => loadAll())
// 切角色/科室会清掉配置缓存并推进代次，常驻实例要自己重跑一遍。
// 这里绝不能再调 clearWorkbenchConfigCache()：代次是自增的，清一次就再触发一次本回调，无限循环。
// 不带 force：Header 切角色时已经重拉过一份并写入缓存，再强制一次就是首屏第二次相同请求。
watch(workbenchConfigEpoch, () => loadAll())
</script>

<template>
  <div class="space-y-4">
    <div class="wb-toolbar">
      <p class="wb-toolbar-meta">{{ metaText }}</p>
      <button
          type="button"
          class="wb-toolbar-btn"
          :disabled="loading"
          @click="loadAll(true)"
      >
        <Refresh class="h-4 w-4"/>
        刷新
      </button>
    </div>

    <div v-if="loading" class="wb-panel">
      加载中…
    </div>

    <div v-else-if="loadError" class="wb-panel is-error">
      <p class="text-[15px] text-red-600">{{ loadError }}</p>
      <button
          type="button"
          class="mt-3 rounded-md border border-red-300 px-3 py-1 text-[15px] text-red-600 hover:bg-red-100"
          @click="loadAll(true)"
      >重试</button>
    </div>

    <div v-else-if="cards.length === 0" class="wb-panel">
      当前角色还没有可展示的卡片，请联系管理员在「系统管理 → 工作台配置」中维护
    </div>

    <div v-else class="wb-grid">
      <section
          v-for="w in cards"
          :key="w.widgetCode"
          class="wb-card"
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
/* 顶栏：左侧一行事实（卡片数 / 更新时刻 / 状态），右侧动作。
   动作按钮必须有东西与之对齐，否则视觉上像被随手丢在页面最上面。 */
.wb-toolbar {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
}

.wb-toolbar-meta {
    min-width: 0;
    font-size: 13px;
    line-height: 20px;
    color: #64748B;
}

.wb-toolbar-btn {
    display: flex;
    flex-shrink: 0;
    align-items: center;
    gap: 6px;
    height: 30px;
    padding: 0 12px;
    font-size: 13px;
    color: #475569;
    background: #fff;
    border: 1px solid var(--his-card-border);
    border-radius: 6px;
    transition: color .2s ease, border-color .2s ease;
}

.wb-toolbar-btn:hover:not(:disabled) {
    color: var(--his-primary);
    border-color: var(--his-primary);
}

.wb-toolbar-btn:disabled {
    opacity: .55;
    cursor: not-allowed;
}

/* 空/载/错三态与卡片共用一套皮肤，颜色走令牌而不是 Tailwind 的 slate 灰阶 */
.wb-panel,
.wb-card {
    background: #fff;
    border: 1px solid var(--his-card-border);
    border-radius: var(--his-radius);
    box-shadow: var(--his-shadow-sm);
}

.wb-panel {
    padding: 40px 20px;
    text-align: center;
    font-size: 15px;
    color: #94A3B8;
}

.wb-panel.is-error {
    background: #FEF2F2;
    border-color: #FECACA;
}

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
    padding: 1.25rem;
}

@media (max-width: 1279px) {
    .wb-grid > .wb-card {
        grid-column: 1 / -1 !important;
    }
}
</style>
