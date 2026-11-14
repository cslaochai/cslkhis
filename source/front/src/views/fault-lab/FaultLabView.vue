<template>
  <div class="p-6">
    <!-- 无权限：菜单已经按角色过滤，这里再兜一层，防止菜单接口 fail-open 时误入 -->
    <div v-if="!permChecked" v-loading="true" class="min-h-[200px] rounded-xl bg-white"></div>

    <el-result
        v-else-if="!permitted"
        icon="warning"
        title="仅系统管理员可见"
        sub-title="当前角色没有 fault:lab:view 权限，请切换到「系统管理员」岗位后再打开本页。"
    />

    <div v-else>
      <!-- ==================== 顶部 ==================== -->
      <div class="mb-4 flex flex-wrap items-start justify-between gap-3">
        <div>
          <h1 class="text-xl font-semibold text-slate-800">故障演练控制台</h1>
        </div>
        <div class="flex items-center gap-2">
          <el-checkbox v-model="autoRefresh">每 5 秒刷新水位</el-checkbox>
          <el-button :icon="Refresh" :loading="statusLoading" @click="refreshStatus(true)">刷新状态</el-button>
          <el-button type="warning" plain :icon="CircleClose" @click="releaseAll">一键收尾</el-button>
        </div>
      </div>

      <el-alert
          v-if="statusError"
          class="mb-4"
          type="error"
          :closable="false"
          show-icon
          title="演练进程未启动或不可达"
          :description="statusError"
      />

      <!-- ==================== 实时水位 ==================== -->
      <el-card shadow="never" class="mb-4">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-medium text-slate-700">演练进程实时水位</span>
            <span class="text-xs text-slate-400">
              {{ status ? `采样于 ${statusTime}` : '未取到状态' }}
            </span>
          </div>
        </template>
        <div class="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6">
          <div class="rounded-lg bg-slate-50 px-3 py-3">
            <div class="text-xs text-slate-500">进程状态</div>
            <div class="mt-1 flex items-center gap-1 text-lg font-semibold" :class="status ? 'text-emerald-600' : 'text-rose-600'">
              <span class="inline-block h-2 w-2 rounded-full" :class="status ? 'bg-emerald-500' : 'bg-rose-500'"></span>
              {{ status ? '在线' : '离线' }}
            </div>
          </div>
          <div class="rounded-lg bg-slate-50 px-3 py-3">
            <div class="text-xs text-slate-500">堆内存</div>
            <div class="mt-1 text-lg font-semibold text-slate-800">
              {{ status ? `${status.heapUsedMb} / ${status.heapMaxMb} MB` : '—' }}
            </div>
            <el-progress
                v-if="status"
                class="mt-1"
                :percentage="heapPercent"
                :stroke-width="4"
                :show-text="false"
                :color="heapPercent > 85 ? '#f56c6c' : '#1269B5'"
            />
          </div>
          <div class="rounded-lg bg-slate-50 px-3 py-3">
            <div class="text-xs text-slate-500">演练保留堆</div>
            <div class="mt-1 text-lg font-semibold text-slate-800">{{ status ? `${status.retainedHeapMb} MB` : '—' }}</div>
          </div>
          <div class="rounded-lg bg-slate-50 px-3 py-3">
            <div class="text-xs text-slate-500">堆外保留</div>
            <div class="mt-1 text-lg font-semibold text-slate-800">{{ status ? `${status.retainedDirectMb} MB` : '—' }}</div>
          </div>
          <div class="rounded-lg bg-slate-50 px-3 py-3">
            <div class="text-xs text-slate-500">故障线程</div>
            <div class="mt-1 text-lg font-semibold" :class="status && status.activeFaultThreads > 0 ? 'text-amber-600' : 'text-slate-800'">
              {{ status ? status.activeFaultThreads : '—' }}
            </div>
          </div>
          <div class="rounded-lg bg-slate-50 px-3 py-3">
            <div class="text-xs text-slate-500">JVM 线程 / CPU 核</div>
            <div class="mt-1 text-lg font-semibold text-slate-800">
              {{ status ? `${status.liveThreads} / ${status.availableProcessors}` : '—' }}
            </div>
          </div>
        </div>
      </el-card>

      <!-- ==================== 场景卡片 ==================== -->
      <div v-for="group in scenarioGroups" :key="group.name" class="mb-6">
        <div class="mb-2 flex items-center gap-2">
          <el-icon class="text-[#1269B5]"><component :is="groupIcon(group.name)"/></el-icon>
          <h2 class="text-base font-semibold text-slate-800">{{ group.name }}</h2>
          <span class="text-xs text-slate-400">{{ group.items.length }} 个场景</span>
        </div>

        <div class="grid grid-cols-1 gap-3 xl:grid-cols-2">
          <el-card v-for="sc in group.items" :key="sc.code" shadow="hover" class="fault-card">
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <h3 class="truncate text-sm font-semibold text-slate-800">{{ sc.name }}</h3>
                <p class="mt-0.5 text-xs text-slate-400">{{ sc.code }}</p>
              </div>
              <el-tag :type="riskType(sc.risk)" effect="light" size="small" class="shrink-0">{{ sc.risk }}风险</el-tag>
            </div>

            <div v-if="sc.params" class="mt-3 flex flex-wrap items-end gap-3">
              <div v-for="p in sc.params" :key="p.key" class="flex flex-col gap-1">
                <span class="text-xs text-slate-500">{{ p.label }}</span>
                <el-input-number
                    v-if="p.type !== 'switch'"
                    v-model="params[sc.code][p.key]"
                    :min="p.min"
                    :max="p.max"
                    :step="p.step || 1"
                    size="small"
                    controls-position="right"
                    class="w-[132px]"
                />
                <el-switch v-else v-model="params[sc.code][p.key]" size="small"/>
              </div>
            </div>

            <div class="mt-4 flex flex-wrap items-center gap-2">
              <el-button
                  type="danger"
                  plain
                  size="small"
                  :icon="VideoPlay"
                  :loading="loading[sc.code]"
                  @click="runAction(sc)"
              >
                触发故障
              </el-button>
            </div>

            <div
                v-if="results[sc.code]"
                class="mt-3 rounded-lg border px-3 py-2 text-xs leading-5"
                :class="resultClass(results[sc.code])"
            >
              <div class="flex items-center gap-2">
                <el-tag :type="statusTagType(results[sc.code].status)" size="small" effect="dark">
                  {{ results[sc.code].status }}
                </el-tag>
                <span class="font-medium">{{ results[sc.code].message }}</span>
              </div>
              <div class="mt-1 text-slate-600">
                堆 {{ results[sc.code].heapUsedMb }} / {{ results[sc.code].heapMaxMb }} MB ·
                保留堆 {{ results[sc.code].retainedHeapMb }} MB ·
                堆外 {{ results[sc.code].retainedDirectMb }} MB ·
                故障线程 {{ results[sc.code].activeFaultThreads }}
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- ==================== 控制与对照动作 ==================== -->
      <el-card shadow="never" class="mb-4">
        <template #header>
          <span class="font-medium text-slate-700">控制与对照动作</span>
        </template>

        <div class="grid grid-cols-1 gap-2 lg:grid-cols-2">
          <div
              v-for="ac in extraActions"
              :key="ac.code"
              class="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-slate-200 px-3 py-2"
          >
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-slate-800">{{ ac.name }}</span>
                <el-tag size="small" :type="riskType(ac.risk)" effect="plain">{{ ac.risk }}</el-tag>
              </div>
              <div v-if="results[ac.code]" class="mt-1 text-xs" :class="resultTextClass(results[ac.code])">
                {{ results[ac.code].status }} · {{ results[ac.code].message }}
              </div>
            </div>
            <div class="flex items-center gap-2">
              <template v-for="p in ac.params" :key="p.key">
                <el-input
                    v-if="p.type === 'text'"
                    v-model="params[ac.code][p.key]"
                    size="small"
                    class="w-[120px]"
                />
                <el-input-number
                    v-else
                    v-model="params[ac.code][p.key]"
                    :min="p.min"
                    :max="p.max"
                    :step="p.step || 1"
                    size="small"
                    controls-position="right"
                    class="w-[120px]"
                />
              </template>
              <el-button size="small" :loading="loading[ac.code]" @click="runAction(ac)">执行</el-button>
            </div>
          </div>
        </div>
      </el-card>

      <!-- ==================== 最近操作 ==================== -->
      <el-card shadow="never" class="mb-4">
        <template #header>
          <div class="flex items-center justify-between">
            <span class="font-medium text-slate-700">操作记录</span>
            <el-button text size="small" :disabled="logs.length === 0" @click="logs = []">清空</el-button>
          </div>
        </template>
        <el-table :data="logs" size="small" border max-height="320" empty-text="暂无操作记录">
          <el-table-column label="时间" prop="time" width="92"/>
          <el-table-column label="动作" prop="name" min-width="180"/>
          <el-table-column label="结果" width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="回执" prop="message" min-width="260" show-overflow-tooltip/>
          <el-table-column label="堆水位" width="130">
            <template #default="{ row }">{{ row.heapUsedMb }} / {{ row.heapMaxMb }} MB</template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  Aim,
  CircleClose,
  Cpu,
  DataLine,
  Files,
  Monitor,
  Refresh,
  VideoPlay,
  Warning
} from '@element-plus/icons-vue'
import {getFaultCatalog, getFaultStatus, triggerFault} from '@/api/faultLab'
import {ensurePerm} from '@/lib/perm'

/**
 * 场景卡片元数据：catalog 只给中文说明，接口路径与参数范围必须前端登记。
 * 参数默认值 / 上下限与 his-web-fault 的 @RequestParam 默认值和 FaultLabProperties 上限对齐，
 * 真正夹取仍在服务端做。
 */
const SCENARIOS = {
  'cpu-regex': {
    group: 'CPU',
    risk: '中',
    endpoint: '/fault/cpu/regex',
    params: [
      {key: 'seconds', label: '持续秒数', min: 1, max: 60, default: 20},
      {key: 'chars', label: '回溯字符数', min: 15, max: 40, default: 25}
    ]
  },
  'cpu-spin': {
    group: 'CPU',
    risk: '高',
    endpoint: '/fault/cpu/spin',
    params: [
      {key: 'threads', label: '自旋线程', min: 1, max: 8, default: 4},
      {key: 'seconds', label: '持续秒数', min: 1, max: 60, default: 20}
    ]
  },
  'heap-leak': {
    group: '内存',
    risk: '中',
    endpoint: '/fault/heap/leak',
    params: [{key: 'mb', label: '泄漏大小(MB)', min: 8, max: 512, step: 8, default: 128}]
  },
  'heap-oom': {
    group: '内存',
    risk: '高',
    endpoint: '/fault/heap/oom',
    params: [
      {key: 'mb', label: '填堆大小(MB)', min: 64, max: 512, step: 64, default: 256},
      {key: 'releaseAfter', label: 'OOM 后自动释放', type: 'switch', default: true}
    ]
  },
  offheap: {
    group: '内存',
    risk: '高',
    endpoint: '/fault/offheap/leak',
    params: [{key: 'mb', label: '堆外大小(MB)', min: 16, max: 512, step: 16, default: 256}]
  },
  'thread-exhaust': {
    group: '线程',
    risk: '高',
    endpoint: '/fault/thread/load',
    params: [
      {key: 'requests', label: '压入请求数', min: 10, max: 400, step: 10, default: 300},
      {key: 'delayMs', label: '每请求耗时(ms)', min: 100, max: 15000, step: 100, default: 3000}
    ]
  },
  'db-pool': {
    group: '数据库',
    risk: '高',
    endpoint: '/fault/db/hold',
    params: [
      {key: 'requests', label: '占池请求数', min: 2, max: 30, default: 20},
      {key: 'holdMs', label: '占用时长(ms)', min: 500, max: 120000, step: 500, default: 4000}
    ]
  },
  deadlock: {
    group: '并发与缓存',
    risk: '中',
    endpoint: '/fault/deadlock',
    params: []
  },
  'cache-stampede': {
    group: '并发与缓存',
    risk: '中',
    endpoint: '/fault/cache/stampede',
    params: [{key: 'concurrency', label: '并发数', min: 5, max: 400, step: 5, default: 40}]
  },
  'gc-pressure': {
    group: 'GC',
    risk: '低',
    endpoint: '/fault/gc/pressure',
    params: [
      {key: 'seconds', label: '持续秒数', min: 5, max: 60, default: 20},
      {key: 'mbPerSecond', label: '每秒分配(MB)', min: 4, max: 128, step: 4, default: 32}
    ]
  }
}

/** 收尾 / 对照 / 只读动作：与场景卡分开，避免误当成「又一个故障」 */
const EXTRA_ACTIONS = [
  {
    code: 'cpu-stop',
    name: '停止 CPU 故障线程',
    risk: '低',
    endpoint: '/fault/cpu/stop',
    params: []
  },
  {
    code: 'heap-release',
    name: '释放堆内保留对象',
    risk: '低',
    endpoint: '/fault/heap/release',
    params: []
  },
  {
    code: 'offheap-release',
    name: '释放堆外内存',
    risk: '低',
    endpoint: '/fault/offheap/release',
    params: []
  },
  {
    code: 'thread-slow',
    name: '单请求变慢（对照）',
    risk: '低',
    endpoint: '/fault/thread/slow',
    params: [{key: 'delayMs', label: '耗时(ms)', min: 100, max: 15000, step: 100, default: 3000}]
  },
  {
    code: 'thread-downstream',
    name: '下游不返回（对照）',
    risk: '中',
    endpoint: '/fault/thread/downstream',
    params: [{key: 'delayMs', label: '耗时(ms)', min: 100, max: 15000, step: 100, default: 3000}]
  },
  {
    code: 'db-init',
    name: '初始化演练数据',
    risk: '低',
    endpoint: '/fault/db/init',
    params: []
  },
  {
    code: 'db-slow-query',
    name: '无索引 LIKE 慢查询',
    risk: '中',
    endpoint: '/fault/db/slowQuery',
    params: [{key: 'keyword', label: '关键字', type: 'text', default: '张'}]
  },
  {
    code: 'db-pool-status',
    name: '查看连接池水位',
    risk: '低',
    endpoint: '/fault/db/poolStatus',
    params: []
  },
  {
    code: 'cache-singleflight',
    name: '缓存单飞（对照）',
    risk: '低',
    endpoint: '/fault/cache/singleflight',
    params: [{key: 'concurrency', label: '并发数', min: 5, max: 400, step: 5, default: 40}]
  }
]

const GROUP_ORDER = ['CPU', '内存', '线程', '数据库', '并发与缓存', 'GC']
const GROUP_ICONS = {
  CPU: Cpu,
  内存: DataLine,
  线程: Monitor,
  数据库: Files,
  并发与缓存: Aim,
  GC: Warning
}

const permChecked = ref(false)
const permitted = ref(false)
const catalog = ref([])
const status = ref(null)
const statusError = ref('')
const statusLoading = ref(false)
const statusTime = ref('')
const autoRefresh = ref(true)
const loading = reactive({})
const params = reactive({})
const results = reactive({})
const logs = ref([])
let timer = null

const scenarioCards = computed(() =>
    catalog.value
        .map(item => ({...item, ...(SCENARIOS[item.code] || {})}))
        .filter(item => item.endpoint)
)

const scenarioGroups = computed(() =>
    GROUP_ORDER
        .map(name => ({name, items: scenarioCards.value.filter(item => item.group === name)}))
        .filter(group => group.items.length > 0)
)

const heapPercent = computed(() => {
  if (!status.value || !status.value.heapMaxMb) return 0
  return Math.min(100, Math.round((status.value.heapUsedMb * 100) / status.value.heapMaxMb))
})

const extraActions = EXTRA_ACTIONS

function initExtraParams() {
  EXTRA_ACTIONS.forEach(action => {
    params[action.code] = {}
    action.params.forEach(p => {
      params[action.code][p.key] = p.default
    })
  })
}

function initParams(list) {
  list.forEach(item => {
    const meta = SCENARIOS[item.code]
    if (meta && meta.params) {
      params[item.code] = {}
      meta.params.forEach(p => {
        params[item.code][p.key] = p.default
      })
    }
  })
  EXTRA_ACTIONS.forEach(action => {
    params[action.code] = {}
    action.params.forEach(p => {
      params[action.code][p.key] = p.default
    })
  })
}

function groupIcon(name) {
  return GROUP_ICONS[name] || Aim
}

function riskType(risk) {
  if (risk === '高') return 'danger'
  if (risk === '中') return 'warning'
  return 'success'
}

function statusTagType(value) {
  const text = String(value || '').toUpperCase()
  if (/REJECT|FAIL|ERROR|DENY|FORBID/.test(text)) return 'danger'
  if (/RUN|OK|SUCCESS|START/.test(text)) return 'success'
  return 'info'
}

function resultClass(result) {
  return statusTagType(result.status) === 'danger'
      ? 'border-rose-200 bg-rose-50'
      : 'border-emerald-200 bg-emerald-50'
}

function resultTextClass(result) {
  return statusTagType(result.status) === 'danger' ? 'text-rose-600' : 'text-emerald-700'
}

function nowText() {
  const d = new Date()
  const pad = n => String(n).padStart(2, '0')
  return `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

function pushLog(action, result) {
  logs.value.unshift({
    time: nowText(),
    name: action.name,
    status: result.status,
    message: result.message,
    heapUsedMb: result.heapUsedMb,
    heapMaxMb: result.heapMaxMb
  })
  if (logs.value.length > 30) logs.value = logs.value.slice(0, 30)
}

async function refreshStatus(notify = false) {
  if (statusLoading.value) return
  statusLoading.value = true
  try {
    status.value = await getFaultStatus()
    statusError.value = ''
    statusTime.value = nowText()
    if (notify) ElMessage.success('演练进程状态已刷新')
  } catch (e) {
    status.value = null
    statusError.value = e.message
    if (notify) ElMessage.error(e.message)
  } finally {
    statusLoading.value = false
  }
}

async function runAction(action) {
  if (action.risk === '高') {
    const extra = action.code === 'heap-oom'
        ? 'OOM 会真实触发并可能写出数百 MB 的 hprof 到 run/dumps，确认磁盘够用。'
        : '该动作会真实消耗 CPU / 内存 / 线程 / 数据库连接池。'
    try {
      await ElMessageBox.confirm(
          `${action.name}：${extra} 只影响演练进程 18090，确认执行？`,
          '高风险演练确认',
          {type: 'warning', confirmButtonText: '确认触发', cancelButtonText: '取消'}
      )
    } catch (e) {
      return
    }
  }

  loading[action.code] = true
  try {
    const result = await triggerFault(action.endpoint, params[action.code] || {})
    results[action.code] = result
    pushLog(action, result)
    const tag = statusTagType(result.status)
    if (tag === 'danger') {
      ElMessage.warning(result.message)
    } else {
      ElMessage.success(result.message)
    }
    await refreshStatus()
  } catch (e) {
    const failed = {status: 'ERROR', message: e.message, heapUsedMb: 0, heapMaxMb: 0}
    results[action.code] = failed
    pushLog(action, failed)
    ElMessage.error(e.message)
  } finally {
    loading[action.code] = false
  }
}

async function releaseAll() {
  try {
    await ElMessageBox.confirm(
        '将依次停止 CPU 故障线程、释放堆内保留对象、释放堆外内存。用于演示结束收尾，确认执行？',
        '一键收尾',
        {type: 'warning', confirmButtonText: '开始收尾', cancelButtonText: '取消'}
    )
  } catch (e) {
    return
  }
  const steps = [
    {code: 'cpu-stop', name: '停止 CPU 故障线程', endpoint: '/fault/cpu/stop'},
    {code: 'heap-release', name: '释放堆内保留对象', endpoint: '/fault/heap/release'},
    {code: 'offheap-release', name: '释放堆外内存', endpoint: '/fault/offheap/release'}
  ]
  for (const step of steps) {
    try {
      const result = await triggerFault(step.endpoint, {})
      results[step.code] = result
      pushLog(step, result)
    } catch (e) {
      console.error('收尾动作失败：', step.name, e)
      ElMessage.error(`${step.name}失败：${e.message}`)
    }
  }
  await refreshStatus()
  ElMessage.success('收尾动作已执行，留意堆 / 堆外 / 故障线程是否回落')
}

async function loadAll() {
  // 先初始化「控制与对照动作」的参数：演练进程没起来时 catalog 拉不到，
  // 但这块区域照样要能渲染（否则 v-model 读 undefined 会抛错）。
  initExtraParams()
  try {
    const list = await getFaultCatalog()
    initParams(list)
    catalog.value = list
  } catch (e) {
    statusError.value = e.message
    ElMessage.error(e.message)
  }
  await refreshStatus()
}

function startTimer() {
  stopTimer()
  timer = setInterval(() => {
    if (autoRefresh.value && !document.hidden) refreshStatus()
  }, 5000)
}

function stopTimer() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

onMounted(async () => {
  permitted.value = await ensurePerm('fault:lab:view')
  permChecked.value = true
  if (!permitted.value) return
  await loadAll()
  startTimer()
})

onBeforeUnmount(stopTimer)
</script>

<style scoped>
.fault-card :deep(.el-card__body) {
  padding: 16px;
}

code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}
</style>
