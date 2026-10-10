<template>
  <div v-loading="loading" class="p-5 space-y-4">
    <!-- KPI 卡 -->
    <div class="grid grid-cols-6 gap-3">
      <div v-for="k in kpis" :key="k.label" :data-testid="`bi-kpi-${k.color}`"
           class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <p class="text-xs text-gray-500">{{ k.label }}</p>
        <p :class="{ blue: 'text-blue-600', green: 'text-green-600', amber: 'text-amber-600', purple: 'text-purple-600', red: 'text-red-600', teal: 'text-teal-600' }[k.color as any]"
           class="text-2xl font-semibold mt-1">
          {{ k.value }}</p>
      </div>
    </div>

    <!-- 国考四指标卡（近 30 日） -->
    <div class="grid grid-cols-4 gap-3" data-testid="bi-national">
      <div v-for="k in nationalKpis" :key="k.label" :data-testid="k.testid"
           class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="flex items-baseline justify-between">
          <p class="text-xs text-gray-500">{{ k.label }}</p>
          <p class="text-[10px] text-gray-400">近 30 日</p>
        </div>
        <p :class="{ blue: 'text-blue-600', amber: 'text-amber-600', purple: 'text-purple-600', teal: 'text-teal-600' }[k.color as any]"
           class="text-2xl font-semibold mt-1">
          {{ k.value }}</p>
        <p :title="k.sub" class="text-[11px] text-gray-400 mt-1 truncate">{{ k.sub }}</p>
      </div>
    </div>

    <!-- 趋势图 -->
    <div class="grid grid-cols-2 gap-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="bi-appt-trend">
        <p class="text-sm font-medium mb-3">近 7 日门诊挂号趋势</p>
        <svg class="w-full" viewBox="0 0 480 190">
          <line stroke="#e5e7eb" x1="30" x2="470" y1="160" y2="160"/>
          <template v-for="(b, i) in apptBars" :key="i">
            <rect :height="b.h" :x="40 + i * 60" :y="160 - b.h" fill="#3b82f6" rx="3" width="36">
              <title>{{ b.date }}：{{ b.count }} 人次</title>
            </rect>
            <text :x="58 + i * 60" :y="152 - b.h" fill="#374151" font-size="11" text-anchor="middle">{{
                b.count
              }}
            </text>
            <text :x="58 + i * 60" fill="#6b7280" font-size="11" text-anchor="middle" y="178">{{ b.date }}</text>
          </template>
          <text v-if="!apptBars.length" fill="#9ca3af" font-size="13" text-anchor="middle" x="240" y="90">暂无数据
          </text>
        </svg>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="bi-rev-trend">
        <p class="text-sm font-medium mb-3">近 7 日收入趋势（元）</p>
        <svg class="w-full" viewBox="0 0 480 190">
          <line stroke="#e5e7eb" x1="30" x2="470" y1="160" y2="160"/>
          <template v-for="(b, i) in revBars" :key="i">
            <rect :height="b.h" :x="40 + i * 60" :y="160 - b.h" fill="#10b981" rx="3" width="36">
              <title>{{ b.date }}：¥{{ b.amount.toLocaleString() }}</title>
            </rect>
            <text :x="58 + i * 60" :y="152 - b.h" fill="#374151" font-size="10" text-anchor="middle">
              {{ b.amount >= 10000 ? (b.amount / 10000).toFixed(1) + '万' : b.amount }}
            </text>
            <text :x="58 + i * 60" fill="#6b7280" font-size="11" text-anchor="middle" y="178">{{ b.date }}</text>
          </template>
          <text v-if="!revBars.length" fill="#9ca3af" font-size="13" text-anchor="middle" x="240" y="90">暂无数据</text>
        </svg>
      </div>
    </div>

    <!-- 科室 TOP5 + 床位 -->
    <div class="grid grid-cols-2 gap-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="bi-dept-top">
        <p class="text-sm font-medium mb-3">近 30 日收入 TOP5 科室</p>
        <div class="space-y-2">
          <div v-for="d in deptBars" :key="d.name" class="flex items-center gap-2">
            <span class="text-xs text-gray-600 w-24 truncate">{{ d.name }}</span>
            <div class="flex-1 h-5 bg-gray-50 rounded overflow-hidden">
              <div :style="{ width: d.w + 'px' }" class="h-5 rounded"
                   style="background: linear-gradient(90deg,#6366f1,#8b5cf6)"></div>
            </div>
            <span class="text-xs text-gray-700 w-24 text-right">{{ money(d.amount) }}</span>
          </div>
          <p v-if="!deptBars.length" class="text-xs text-gray-400 text-center py-6">暂无数据</p>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="bi-bed">
        <p class="text-sm font-medium mb-3">床位占用</p>
        <svg class="w-full" viewBox="0 0 400 150">
          <circle cx="110" cy="75" fill="none" r="55" stroke="#e5e7eb" stroke-width="16"/>
          <circle :stroke-dasharray="`${Number(data?.bedOccupancy || 0) * 345.6} 345.6`" cx="110" cy="75" fill="none" r="55" stroke="#f59e0b"
                  stroke-linecap="round"
                  stroke-width="16" transform="rotate(-90 110 75)"/>
          <text fill="#111827" font-size="22" font-weight="600" text-anchor="middle" x="110" y="72">
            {{ pct(data?.bedOccupancy) }}
          </text>
          <text fill="#6b7280" font-size="11" text-anchor="middle" x="110" y="94">
            占用 {{ data?.bedOccupied ?? '—' }} / 总 {{ data?.bedTotal ?? '—' }} 张
          </text>
          <text fill="#374151" font-size="13" x="240" y="60">在院人数：{{ data?.inHospitalCount ?? '—' }}</text>
          <text fill="#374151" font-size="13" x="240" y="85">今日出院：{{ data?.todayDischargeCount ?? '—' }}</text>
          <text fill="#374151" font-size="13" x="240" y="110">
            今日药费：{{ data ? money(data.todayDrugRevenue) : '—' }}
          </text>
        </svg>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * BI 驾驶舱（G23，菜单 906，挂报表统计；目录 1200 于 sql/188 由「报表与审计」更名）
 *
 * 两个接口全量返回（/report/bi/overview + /report/bi/nationalMetrics 国考四指标），
 * 页面不二次拼装。图表用纯 SVG 自绘（趋势柱图 + 科室横向条形），不加 echarts 依赖。
 */
import {computed, onMounted, ref} from 'vue';

const data = ref(null);
const national = ref(null);
const loading = ref(false);
const load = async () => {
  loading.value = true;
  try {
    const {default: request} = await import('@/api/request');
    const [res, nat] = await Promise.all([
      request.get('/report/bi/overview'),
      request.get('/report/bi/nationalMetrics'),
    ]);
    data.value = res?.data || null;
    national.value = nat?.data || null;
  } catch (e) {
    console.error('加载驾驶舱数据失败', e);
  } finally {
    loading.value = false;
  }
};
const money = (v) => '¥' + Number(v || 0).toLocaleString('zh-CN', {minimumFractionDigits: 2, maximumFractionDigits: 2});
const pct = (v) => (Number(v || 0) * 100).toFixed(1) + '%';
const kpis = computed(() => [
  {label: '今日挂号人次', value: String(data.value?.todayAppointments ?? '—'), color: 'blue'},
  {label: '在院人数', value: String(data.value?.inHospitalCount ?? '—'), color: 'green'},
  {label: '今日出院', value: String(data.value?.todayDischargeCount ?? '—'), color: 'amber'},
  {label: '今日收入', value: data.value ? money(data.value.todayRevenue) : '—', color: 'purple'},
  {label: '药占比', value: data.value ? pct(data.value.drugRatio) : '—', color: 'red'},
  {label: '床位占用率', value: data.value ? pct(data.value.bedOccupancy) : '—', color: 'teal'},
]);
// 国考四指标卡（M5，近 30 日窗口，分母写进副标题能对上数）
const nationalKpis = computed(() => [
  {
    label: '平均住院日', value: national.value ? Number(national.value.avgLengthOfStay).toFixed(2) + ' 天' : '—',
    sub: national.value ? `出院 ${national.value.dischargeCount} 人 / 床日 ${national.value.totalBedDays}` : '',
    color: 'blue', testid: 'bi-nat-los',
  },
  {
    label: '床位周转次数', value: national.value ? Number(national.value.bedTurnover).toFixed(2) : '—',
    sub: national.value ? `出院 ${national.value.dischargeCount} 人 / 可用床位 ${national.value.usableBeds} 张` : '',
    color: 'teal', testid: 'bi-nat-turnover',
  },
  {
    label: '耗占比', value: national.value ? pct(national.value.materialRatio) : '—',
    sub: national.value ? `耗材 ${money(national.value.materialRevenue)} / 收入 ${money(national.value.revenue)}` : '',
    color: 'amber', testid: 'bi-nat-material',
  },
  {
    label: 'CMI（模拟）', value: national.value ? Number(national.value.cmi).toFixed(4) : '—',
    sub: national.value ? `入组 ${national.value.cmiGroupedCount} / 样本 ${national.value.cmiSampleCount} 例` : '',
    color: 'purple', testid: 'bi-nat-cmi',
  },
]);
// 近 7 日门诊量柱图
const apptBars = computed(() => {
  const rows = data.value?.appointmentTrend || [];
  const max = Math.max(1, ...rows.map(r => Number(r.count || 0)));
  return rows.map(r => ({date: r.date, count: Number(r.count || 0), h: (Number(r.count || 0) / max) * 130}));
});
// 近 7 日收入柱图
const revBars = computed(() => {
  const rows = data.value?.revenueTrend || [];
  const max = Math.max(1, ...rows.map(r => Number(r.amount || 0)));
  return rows.map(r => ({date: r.date, amount: Number(r.amount || 0), h: (Number(r.amount || 0) / max) * 130}));
});
// 科室 TOP5 条形
const deptBars = computed(() => {
  const rows = data.value?.deptTop || [];
  const max = Math.max(1, ...rows.map(r => Number(r.amount || 0)));
  return rows.map(r => ({
    name: r.deptName, amount: Number(r.amount || 0),
    w: Math.max(4, (Number(r.amount || 0) / max) * 420),
  }));
});
onMounted(load);
</script>
