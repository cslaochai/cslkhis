<template>
  <!-- 工作台首页效果预览：纯静态示例，不调任何接口。
       卡片的显隐/顺序/宽度跟随当前勾选，卡片内容全部写死，仅用于模拟展示。 -->
  <div class="wb-preview">
    <!-- 模拟首页工具栏 -->
    <div class="wb-preview-toolbar">
      <p class="text-[13px] text-slate-500">
        共 {{ visibleCards.length }} 张卡片 · 效果预览（示例数据，非真实业务数据）
      </p>
      <span class="text-[13px] text-slate-400">刷新</span>
    </div>

    <div v-if="visibleCards.length === 0" class="wb-preview-empty">
      当前角色还没有可展示的卡片
    </div>

    <div v-else class="wb-preview-scroll">
      <div class="wb-preview-grid">
        <section
            v-for="w in visibleCards"
            :key="w.widgetCode"
            :style="{ gridColumn: `span ${clampSpan(w.span)}` }"
            class="wb-preview-card"
        >
          <!-- 我的待办 -->
          <template v-if="w.widgetCode === 'myTodo'">
            <h3 class="mb-3.5 flex items-center justify-between text-[17px] font-semibold text-slate-800">
              <span>我的待办</span>
              <a class="text-[15px] font-normal text-[#1269B5]">查看全部</a>
            </h3>
            <p class="mb-2 text-[15px] text-slate-600">
              共 <span class="text-[18px] font-bold text-slate-900">4</span> 条
              <span class="ml-2 text-red-600">危急值 1 条</span>
            </p>
            <ul class="divide-y divide-slate-100">
              <li class="flex items-center gap-3 py-2.5">
                <span class="shrink-0 rounded bg-red-50 px-2 py-0.5 text-[13px] font-medium text-red-600">处方审核</span>
                <p class="min-w-0 flex-1 truncate text-[15px] font-medium text-slate-800">王秀兰 抗菌药物处方待审核</p>
                <span class="shrink-0 text-[13px] text-slate-400">09:32</span>
                <button class="shrink-0 rounded-md border border-[#1269B5] px-3 py-1 text-[14px] text-[#1269B5]" type="button">处理</button>
              </li>
              <li class="flex items-center gap-3 py-2.5">
                <span class="shrink-0 rounded bg-red-50 px-2 py-0.5 text-[13px] font-medium text-red-600">危急值</span>
                <p class="min-w-0 flex-1 truncate text-[15px] font-medium text-slate-800">12床 李国强 血钾 6.8 mmol/L</p>
                <span class="shrink-0 text-[13px] text-slate-400">09:18</span>
                <button class="shrink-0 rounded-md border border-[#1269B5] px-3 py-1 text-[14px] text-[#1269B5]" type="button">处理</button>
              </li>
              <li class="flex items-center gap-3 py-2.5">
                <span class="shrink-0 rounded bg-blue-50 px-2 py-0.5 text-[13px] font-medium text-[#1269B5]">会诊</span>
                <p class="min-w-0 flex-1 truncate text-[15px] font-medium text-slate-800">心内科会诊申请：陈建华</p>
                <span class="shrink-0 text-[13px] text-slate-400">08:55</span>
                <button class="shrink-0 rounded-md border border-[#1269B5] px-3 py-1 text-[14px] text-[#1269B5]" type="button">处理</button>
              </li>
              <li class="flex items-center gap-3 py-2.5">
                <span class="shrink-0 rounded bg-blue-50 px-2 py-0.5 text-[13px] font-medium text-[#1269B5]">出院审核</span>
                <p class="min-w-0 flex-1 truncate text-[15px] font-medium text-slate-800">28床 赵敏 出院结算待审核</p>
                <span class="shrink-0 text-[13px] text-slate-400">08:40</span>
                <button class="shrink-0 rounded-md border border-[#1269B5] px-3 py-1 text-[14px] text-[#1269B5]" type="button">处理</button>
              </li>
            </ul>
          </template>

          <!-- 最新消息 -->
          <template v-else-if="w.widgetCode === 'myNotice'">
            <h3 class="mb-3.5 flex items-center justify-between text-[17px] font-semibold text-slate-800">
              <span>最新消息</span>
              <a class="text-[15px] font-normal text-[#1269B5]">查看全部</a>
            </h3>
            <p class="mb-2 text-[15px] text-slate-600">
              共 <span class="text-[18px] font-bold text-slate-900">4</span> 条
            </p>
            <ul class="divide-y divide-slate-100">
              <li class="flex items-center gap-3 py-2.5">
                <span class="shrink-0 rounded bg-slate-100 px-2 py-0.5 text-[13px] font-medium text-slate-600">系统通知</span>
                <p class="min-w-0 flex-1 truncate text-[15px] font-medium text-slate-800">本周六凌晨 00:00-02:00 系统维护</p>
                <span class="shrink-0 text-[13px] text-slate-400">10-12</span>
                <button class="shrink-0 rounded-md border border-slate-300 px-3 py-1 text-[14px] text-slate-600" type="button">已读</button>
              </li>
              <li class="flex items-center gap-3 py-2.5">
                <span class="shrink-0 rounded bg-slate-100 px-2 py-0.5 text-[13px] font-medium text-slate-600">排班通知</span>
                <p class="min-w-0 flex-1 truncate text-[15px] font-medium text-slate-800">下周门诊排班表已发布，请及时查看</p>
                <span class="shrink-0 text-[13px] text-slate-400">10-11</span>
                <button class="shrink-0 rounded-md border border-slate-300 px-3 py-1 text-[14px] text-slate-600" type="button">已读</button>
              </li>
              <li class="flex items-center gap-3 py-2.5">
                <span class="shrink-0 rounded bg-slate-100 px-2 py-0.5 text-[13px] font-medium text-slate-600">会议通知</span>
                <p class="min-w-0 flex-1 truncate text-[15px] font-medium text-slate-800">今日 16:00 三季度医疗质量例会</p>
                <span class="shrink-0 text-[13px] text-slate-400">10-11</span>
                <button class="shrink-0 rounded-md border border-slate-300 px-3 py-1 text-[14px] text-slate-600" type="button">已读</button>
              </li>
              <li class="flex items-center gap-3 py-2.5">
                <span class="shrink-0 rounded bg-slate-100 px-2 py-0.5 text-[13px] font-medium text-slate-600">院内公告</span>
                <p class="min-w-0 flex-1 truncate text-[15px] font-medium text-slate-800">国庆节放假安排及值班要求</p>
                <span class="shrink-0 text-[13px] text-slate-400">10-10</span>
                <button class="shrink-0 rounded-md border border-slate-300 px-3 py-1 text-[14px] text-slate-600" type="button">已读</button>
              </li>
            </ul>
          </template>

          <!-- 常用入口 -->
          <template v-else-if="w.widgetCode === 'quickEntry'">
            <h3 class="mb-3.5 text-[17px] font-semibold text-slate-800">常用入口</h3>
            <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 xl:grid-cols-4">
              <div v-for="it in quickEntries" :key="it.label"
                   class="flex items-center gap-2.5 rounded-lg border border-slate-200 bg-white px-3.5 py-3">
                <span class="flex h-5 w-5 shrink-0 items-center justify-center rounded bg-[#1269B5]/10 text-[#1269B5]">
                  <el-icon><Menu/></el-icon>
                </span>
                <span class="min-w-0">
                  <span class="block truncate text-[15px] font-medium text-slate-800">{{ it.label }}</span>
                  <span class="block truncate text-[13px] text-slate-400">{{ it.group }}</span>
                </span>
              </div>
            </div>
          </template>

          <!-- 今日全院概况 -->
          <template v-else-if="w.widgetCode === 'hospitalToday'">
            <h3 class="mb-3.5 text-[17px] font-semibold text-slate-800">今日全院概况</h3>
            <p class="mb-2 text-sm font-medium text-slate-500">今日运行</p>
            <div class="mb-4 grid grid-cols-2 gap-3 xl:grid-cols-4">
              <div class="metric-cell"><p class="metric-label">今日挂号</p><p class="metric-value">1,286</p></div>
              <div class="metric-cell"><p class="metric-label">今日收入</p><p class="metric-value">¥486,230.50</p></div>
              <div class="metric-cell"><p class="metric-label">在院患者</p><p class="metric-value">742</p></div>
              <div class="metric-cell"><p class="metric-label">占用床位</p><p class="metric-value">612 / 800</p></div>
              <div class="metric-cell"><p class="metric-label">床位使用率</p><p class="metric-value">77%</p></div>
            </div>
            <p class="mb-2 text-sm font-medium text-slate-500">异常告警</p>
            <div class="grid grid-cols-2 gap-3 xl:grid-cols-4">
              <div class="metric-cell"><p class="metric-label">待处理危急值</p><p class="metric-value metric-danger">3</p></div>
              <div class="metric-cell"><p class="metric-label">待审核处方</p><p class="metric-value metric-danger">12</p></div>
              <div class="metric-cell"><p class="metric-label">质控不通过病历</p><p class="metric-value metric-danger">5</p></div>
              <div class="metric-cell"><p class="metric-label">欠费住院患者</p><p class="metric-value metric-danger">2</p></div>
            </div>
          </template>

          <!-- 我的今日诊疗 -->
          <template v-else-if="w.widgetCode === 'myClinicalToday'">
            <h3 class="mb-3.5 text-[17px] font-semibold text-slate-800">我的今日诊疗</h3>
            <div class="grid grid-cols-2 gap-3 xl:grid-cols-4">
              <div class="metric-cell"><p class="metric-label">今日排班</p><p class="metric-value">1</p></div>
              <div class="metric-cell"><p class="metric-label">候诊人数</p><p class="metric-value">23</p></div>
              <div class="metric-cell"><p class="metric-label">我的住院患者</p><p class="metric-value">18</p></div>
              <div class="metric-cell"><p class="metric-label">待校对医嘱</p><p class="metric-value metric-danger">7</p></div>
              <div class="metric-cell"><p class="metric-label">待我会诊</p><p class="metric-value metric-danger">1</p></div>
              <div class="metric-cell"><p class="metric-label">待处理危急值</p><p class="metric-value">0</p></div>
              <div class="metric-cell"><p class="metric-label">待归档病历</p><p class="metric-value metric-danger">4</p></div>
            </div>
          </template>

          <!-- 病区今日概况 -->
          <template v-else-if="w.widgetCode === 'wardNursingToday'">
            <h3 class="mb-3.5 text-[17px] font-semibold text-slate-800">病区今日概况</h3>
            <div class="grid grid-cols-2 gap-3 xl:grid-cols-4">
              <div class="metric-cell"><p class="metric-label">病区在院</p><p class="metric-value">46</p></div>
              <div class="metric-cell"><p class="metric-label">占用床位</p><p class="metric-value">42 / 50</p></div>
              <div class="metric-cell"><p class="metric-label">今日入院</p><p class="metric-value">6</p></div>
              <div class="metric-cell"><p class="metric-label">今日出院</p><p class="metric-value">4</p></div>
              <div class="metric-cell"><p class="metric-label">待执行医嘱</p><p class="metric-value metric-danger">28</p></div>
            </div>
          </template>

          <!-- 科室就诊排行 -->
          <template v-else-if="w.widgetCode === 'deptVisitRank'">
            <h3 class="mb-3.5 text-[17px] font-semibold text-slate-800">科室就诊排行</h3>
            <p class="mb-3 text-[15px] text-slate-600">
              今日共 <span class="text-[18px] font-bold text-slate-900">870</span> 人次
            </p>
            <div class="space-y-2.5">
              <div class="rank-row"><span class="rank-no">1</span><span class="rank-name">内科门诊</span>
                <div class="rank-track"><div class="rank-bar" style="width:100%"></div></div><span class="rank-cnt">286</span></div>
              <div class="rank-row"><span class="rank-no">2</span><span class="rank-name">儿科</span>
                <div class="rank-track"><div class="rank-bar" style="width:69%"></div></div><span class="rank-cnt">198</span></div>
              <div class="rank-row"><span class="rank-no">3</span><span class="rank-name">外科门诊</span>
                <div class="rank-track"><div class="rank-bar" style="width:55%"></div></div><span class="rank-cnt">156</span></div>
              <div class="rank-row"><span class="rank-no">4</span><span class="rank-name">妇产科</span>
                <div class="rank-track"><div class="rank-bar" style="width:46%"></div></div><span class="rank-cnt">132</span></div>
              <div class="rank-row"><span class="rank-no">5</span><span class="rank-name">骨科</span>
                <div class="rank-track"><div class="rank-bar" style="width:34%"></div></div><span class="rank-cnt">98</span></div>
            </div>
          </template>

          <!-- 近 7 日就诊趋势 -->
          <template v-else-if="w.widgetCode === 'weekVisitTrend'">
            <h3 class="mb-3.5 text-[17px] font-semibold text-slate-800">近 7 日就诊趋势</h3>
            <p class="mb-3 text-[15px] text-slate-600">
              近 7 日共 <span class="text-[18px] font-bold text-slate-900">7,191</span> 人次
            </p>
            <div class="flex items-end gap-2" style="height:190px">
              <div class="trend-col"><span class="trend-cnt">1120</span>
                <div class="trend-track"><div class="trend-bar" style="height:86%"></div></div><span class="trend-date">10-07</span></div>
              <div class="trend-col"><span class="trend-cnt">986</span>
                <div class="trend-track"><div class="trend-bar" style="height:75%"></div></div><span class="trend-date">10-08</span></div>
              <div class="trend-col"><span class="trend-cnt">1245</span>
                <div class="trend-track"><div class="trend-bar" style="height:95%"></div></div><span class="trend-date">10-09</span></div>
              <div class="trend-col"><span class="trend-cnt">1308</span>
                <div class="trend-track"><div class="trend-bar" style="height:100%"></div></div><span class="trend-date">10-10</span></div>
              <div class="trend-col"><span class="trend-cnt">1186</span>
                <div class="trend-track"><div class="trend-bar" style="height:91%"></div></div><span class="trend-date">10-11</span></div>
              <div class="trend-col"><span class="trend-cnt">734</span>
                <div class="trend-track"><div class="trend-bar" style="height:56%"></div></div><span class="trend-date">10-12</span></div>
              <div class="trend-col"><span class="trend-cnt">612</span>
                <div class="trend-track"><div class="trend-bar" style="height:47%"></div></div><span class="trend-date">10-13</span></div>
            </div>
          </template>

          <!-- 其他未覆盖的卡片：简单兜底 -->
          <template v-else>
            <h3 class="mb-3.5 text-[17px] font-semibold text-slate-800">{{ w.widgetName }}</h3>
            <div class="space-y-2">
              <div class="h-3.5 rounded bg-slate-200/70" style="width:70%"></div>
              <div class="h-3.5 rounded bg-slate-200/70" style="width:58%"></div>
              <div class="h-3.5 rounded bg-slate-200/70" style="width:46%"></div>
            </div>
          </template>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed} from 'vue'
import {Menu} from '@element-plus/icons-vue'
import {WIDGET_REGISTRY} from '@/lib/workbench-widgets'

const GRID_COLS = 24

const props = defineProps({
  // 当前勾选的卡片行（roleRows 中 visible=1 的子集），只用于决定显隐/顺序/宽度
  widgets: {type: Array, default: () => []},
})

/** 首页只渲染在 WIDGET_REGISTRY 登记过组件的卡，预览同口径；按 sortOrder 升序 */
const visibleCards = computed(() =>
    props.widgets
        .filter(w => WIDGET_REGISTRY[w.widgetCode])
        .slice()
        .sort((a, b) => Number(a.sortOrder || 0) - Number(b.sortOrder || 0)),
)

/** 常用入口写死在这里 */
const quickEntries = [
  {label: '门诊挂号', group: '门诊'},
  {label: '门诊收费', group: '门诊'},
  {label: '住院登记', group: '住院'},
  {label: '出院结算', group: '住院'},
  {label: '药房发药', group: '药房'},
  {label: '医嘱校对', group: '住院'},
  {label: '病历书写', group: '病历'},
  {label: '检查申请', group: '医技'},
  {label: '检验申请', group: '医技'},
  {label: '排班管理', group: '行政'},
  {label: '患者查询', group: '门诊'},
  {label: '报表统计', group: '行政'},
]

function clampSpan(span) {
  return Math.min(Math.max(Number(span) || GRID_COLS, 1), GRID_COLS)
}
</script>

<style scoped>
/* 模拟工作台页面：浅灰底 + 白卡，与 WorkbenchView 同皮肤 */
.wb-preview {
  background: #F4F6F9;
  border: 1px solid var(--his-card-border);
  border-radius: var(--his-radius);
  padding: 14px;
}

.wb-preview-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.wb-preview-empty {
  padding: 48px 20px;
  text-align: center;
  font-size: 15px;
  color: #94A3B8;
  background: #fff;
  border: 1px solid var(--his-card-border);
  border-radius: var(--his-radius);
}

/* 全勾选时卡片很多，预览区内部滚动而不是把弹框撑出视口 */
.wb-preview-scroll {
  max-height: 52vh;
  overflow-y: auto;
}

/* 24 列栅格：与 WorkbenchView 同口径。预览固定按宽屏排布（不套 1280px 媒体查询），
   否则在 980px 弹框里全部整行，看不出多列布局 */
.wb-preview-grid {
  display: grid;
  grid-template-columns: repeat(24, minmax(0, 1fr));
  gap: 1.25rem;
}

.wb-preview-card {
  min-width: 0;
  padding: 1.25rem;
  background: #fff;
  border: 1px solid var(--his-card-border);
  border-radius: var(--his-radius);
  box-shadow: var(--his-shadow-sm);
}

/* 数字概览卡的格子（与真实 MetricWidget 一致） */
.metric-cell {
  padding: 0.75rem 1rem;
  background: rgb(248 250 252 / 0.7);
  border: 1px solid #E2E8F0;
  border-radius: 0.5rem;
}
.metric-label {
  font-size: 15px;
  color: #475569;
}
.metric-value {
  margin-top: 0.25rem;
  font-size: 22px;
  font-weight: 700;
  line-height: 1.25;
  color: #0F172A;
}
.metric-danger {
  color: #DC2626;
}

/* 科室排行 */
.rank-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.rank-no {
  width: 1.25rem;
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 500;
  color: #94A3B8;
  text-align: right;
}
.rank-name {
  width: 7rem;
  flex-shrink: 0;
  overflow: hidden;
  font-size: 15px;
  color: #334155;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.rank-track {
  position: relative;
  height: 1.5rem;
  flex: 1;
  overflow: hidden;
  background: #F1F5F9;
  border-radius: 0.25rem;
}
.rank-bar {
  height: 100%;
  background: #1269B5;
  opacity: 0.8;
  border-radius: 0.25rem;
}
.rank-cnt {
  width: 3rem;
  flex-shrink: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0F172A;
  text-align: right;
}

/* 7 日趋势柱 */
.trend-col {
  display: flex;
  height: 100%;
  flex: 1;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  gap: 0.375rem;
}
.trend-cnt {
  font-size: 15px;
  font-weight: 700;
  color: #0F172A;
}
.trend-track {
  position: relative;
  width: 100%;
  max-width: 46px;
  flex: 1;
  background: #F1F5F9;
  border-radius: 0.25rem 0.25rem 0 0;
}
.trend-bar {
  position: absolute;
  bottom: 0;
  width: 100%;
  max-width: 46px;
  background: #0E9488;
  opacity: 0.8;
  border-radius: 0.25rem 0.25rem 0 0;
}
.trend-date {
  font-size: 14px;
  color: #64748B;
}
</style>
