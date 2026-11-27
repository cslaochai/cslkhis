<template>
  <div v-loading="loading" class="p-5 space-y-4">
    <!-- KPI 卡（今日快照；流量/财务指标带较昨日环比，存量在院人数不挂环比） -->
    <div class="grid grid-cols-7 gap-3">
      <div v-for="k in kpis" :key="k.label" :data-testid="`bi-kpi-${k.color}`"
           :class="['bg-white rounded-lg border border-gray-100 p-4 shadow-sm', k.drill && 'cursor-pointer hover:border-blue-200 hover:shadow-md']"
           @click="goReport(k.drill)">
        <p class="text-xs text-gray-500">{{ k.label }}</p>
        <p :class="{ blue: 'text-blue-600', indigo: 'text-indigo-600', green: 'text-green-600', amber: 'text-amber-600', purple: 'text-purple-600', red: 'text-red-600', teal: 'text-teal-600' }[k.color]"
           class="text-2xl font-semibold mt-1">
          {{ k.value }}</p>
        <p v-if="k.delta" class="mt-1 text-[11px]" :class="deltaClass(k.delta)">
          <span v-if="!k.delta.flat">{{ k.delta.up ? '↑' : '↓' }}</span>{{ k.delta.text }}
          <span class="text-gray-400">较昨日</span>
        </p>
      </div>
    </div>

    <!-- 运营效率卡（近 30 日） -->
    <div class="grid grid-cols-3 gap-3" data-testid="bi-national">
      <div v-for="k in nationalKpis" :key="k.label" :data-testid="k.testid"
           :class="['bg-white rounded-lg border border-gray-100 p-4 shadow-sm', k.drill && 'cursor-pointer hover:border-blue-200 hover:shadow-md']"
           @click="goReport(k.drill)">
        <div class="flex items-baseline justify-between">
          <p class="text-xs text-gray-500">{{ k.label }}</p>
          <p class="text-[10px] text-gray-400">近 30 日</p>
        </div>
        <p :class="{ blue: 'text-blue-600', amber: 'text-amber-600', purple: 'text-purple-600', teal: 'text-teal-600' }[k.color]"
           class="text-2xl font-semibold mt-1">
          {{ k.value }}</p>
        <p :title="k.sub" class="text-[11px] text-gray-400 mt-1 truncate">{{ k.sub }}</p>
      </div>
    </div>

    <!-- 质量与安全卡（近 30 日） -->
    <div class="grid grid-cols-3 gap-3" data-testid="bi-quality">
      <div v-for="k in qualityKpis" :key="k.label" :data-testid="k.testid"
           class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="flex items-baseline justify-between">
          <p class="text-xs text-gray-500">{{ k.label }}</p>
          <p class="text-[10px] text-gray-400">近 30 日</p>
        </div>
        <p :class="{ blue: 'text-blue-600', amber: 'text-amber-600', purple: 'text-purple-600', green: 'text-green-600', red: 'text-red-600' }[k.color]"
           class="text-2xl font-semibold mt-1">
          {{ k.value }}</p>
        <p :title="k.sub" class="text-[11px] text-gray-400 mt-1 truncate">{{ k.sub }}</p>
      </div>
    </div>

    <!-- 趋势图 -->
    <div class="grid grid-cols-2 gap-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm cursor-pointer hover:border-blue-200 hover:shadow-md"
           data-testid="bi-appt-trend" @click="goReport('outpatient')">
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
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm cursor-pointer hover:border-blue-200 hover:shadow-md"
           data-testid="bi-rev-trend" @click="goReport('revenue')">
        <div class="flex items-center justify-between mb-3">
          <p class="text-sm font-medium">近 7 日收入趋势（元）</p>
          <div class="flex items-center gap-3 text-[11px] text-gray-500">
            <span class="flex items-center gap-1"><i class="inline-block w-2.5 h-2.5 rounded-sm bg-blue-500"></i>门诊</span>
            <span class="flex items-center gap-1"><i class="inline-block w-2.5 h-2.5 rounded-sm bg-green-500"></i>住院</span>
          </div>
        </div>
        <svg class="w-full" viewBox="0 0 480 190">
          <line stroke="#e5e7eb" x1="30" x2="470" y1="160" y2="160"/>
          <template v-for="(b, i) in revBars" :key="i">
            <rect :height="b.outH" :x="40 + i * 60" :y="160 - b.outH" fill="#3b82f6" rx="3" width="16">
              <title>{{ b.date }} 门诊：¥{{ b.out.toLocaleString() }}</title>
            </rect>
            <rect :height="b.inH" :x="58 + i * 60" :y="160 - b.inH" fill="#10b981" rx="3" width="16">
              <title>{{ b.date }} 住院：¥{{ b.in.toLocaleString() }}</title>
            </rect>
            <text :x="57 + i * 60" :y="152 - b.h" fill="#374151" font-size="10" text-anchor="middle">
              {{ b.amount >= 10000 ? (b.amount / 10000).toFixed(1) + '万' : b.amount }}
            </text>
            <text :x="57 + i * 60" fill="#6b7280" font-size="11" text-anchor="middle" y="178">{{ b.date }}</text>
          </template>
          <text v-if="!revBars.length" fill="#9ca3af" font-size="13" text-anchor="middle" x="240" y="90">暂无数据</text>
        </svg>
      </div>
    </div>

    <!-- 科室 TOP5 + 收入结构 -->
    <div class="grid grid-cols-2 gap-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="bi-dept-top">
        <p class="text-sm font-medium mb-3">近 30 日收入 TOP5 科室</p>
        <div class="space-y-2">
          <div v-for="d in deptBars" :key="d.name" class="flex items-center gap-2 cursor-pointer hover:bg-slate-50 rounded px-1 -mx-1"
               @click="goReport('revenue', d.deptId)">
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
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="bi-structure">
        <p class="text-sm font-medium mb-3">近 30 日收入结构</p>
        <div class="space-y-2">
          <div v-for="s in structureBars" :key="s.code" class="flex items-center gap-2">
            <span class="text-xs text-gray-600 w-20 truncate">{{ s.name }}</span>
            <div class="flex-1 h-5 bg-gray-50 rounded overflow-hidden">
              <div class="h-5 rounded" :style="{ width: s.w + 'px', background: STRUCTURE_COLORS[s.code] || '#9ca3af' }"></div>
            </div>
            <span class="text-xs text-gray-500 w-14 text-right">{{ (s.ratio * 100).toFixed(1) }}%</span>
            <span class="text-xs text-gray-700 w-24 text-right">{{ money(s.amount) }}</span>
          </div>
          <p v-if="!structureBars.length" class="text-xs text-gray-400 text-center py-6">暂无数据</p>
        </div>
      </div>
    </div>

    <!-- 工作量 TOP5 + 床位资源 -->
    <div class="grid grid-cols-2 gap-3">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="bi-workload-top">
        <p class="text-sm font-medium mb-3">近 30 日出院人次 TOP5 科室</p>
        <div class="space-y-2">
          <div v-for="w in workloadBars" :key="w.name" class="flex items-center gap-2 cursor-pointer hover:bg-slate-50 rounded px-1 -mx-1"
               @click="goReport('inpatient', w.deptId)">
            <span class="text-xs text-gray-600 w-24 truncate">{{ w.name }}</span>
            <div class="flex-1 h-5 bg-gray-50 rounded overflow-hidden">
              <div :style="{ width: w.w + 'px' }" class="h-5 rounded"
                   style="background: linear-gradient(90deg,#0e9488,#14b8a6)"></div>
            </div>
            <span class="text-xs text-gray-700 w-24 text-right">{{ w.count }} 人</span>
          </div>
          <p v-if="!workloadBars.length" class="text-xs text-gray-400 text-center py-6">暂无数据</p>
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm" data-testid="bi-bed">
      <p class="text-sm font-medium mb-3">床位占用</p>
      <div class="flex items-center gap-10">
        <svg width="120" height="120" viewBox="0 0 120 120" class="shrink-0">
          <circle cx="60" cy="60" fill="none" r="44" stroke="#e5e7eb" stroke-width="12"/>
          <circle :stroke-dasharray="`${Number(data?.bedOccupancy || 0) * 276.5} 276.5`" cx="60" cy="60" fill="none" r="44"
                  stroke="#f59e0b" stroke-linecap="round" stroke-width="12" transform="rotate(-90 60 60)"/>
          <text fill="#111827" font-size="18" font-weight="600" text-anchor="middle" x="60" y="58">
            {{ pct(data?.bedOccupancy) }}
          </text>
          <text fill="#6b7280" font-size="10" text-anchor="middle" x="60" y="76">
            占用 {{ data?.bedOccupied ?? '' }} / 总 {{ data?.bedTotal ?? '' }} 张
          </text>
        </svg>
        <div class="flex-1 grid grid-cols-2 gap-x-10 gap-y-3 text-sm">
          <div class="flex items-center justify-between">
            <span class="text-gray-500">今日入院</span>
            <span class="text-gray-800">{{ data?.todayAdmissions ?? '' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-gray-500">今日出院</span>
            <span class="text-gray-800">{{ data?.todayDischargeCount ?? '' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-gray-500">待入院排队</span>
            <span class="text-gray-800">{{ data?.pendingAdmissionCount ?? '' }}</span>
          </div>
          <div class="flex items-center justify-between">
            <span class="text-gray-500">空床</span>
            <span class="text-gray-800">{{ data?.emptyBeds ?? '' }} 张</span>
          </div>
        </div>
      </div>
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
import {useRouter} from 'vue-router';

const router = useRouter();
// 下钻：驾驶舱卡片/图表跳运营统计报表对应页签；科室 TOP 条带 deptId 钻取该科室明细
const goReport = (tab, deptId) => {
  if (!tab) return;
  const query = {tab, from: 'bi'};
  if (deptId) query.deptId = deptId;
  router.push({path: '/reports', query});
};

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

/**
 * 环比口径：count/money 算增长率；ratio 算百分点差值（药占比越低越好，下降记 good）；
 * stock 为时点存量，不挂环比。昨日基数为 0 时不显示箭头，避免除零误导。
 */
const buildDelta = (today, yesterday, kind) => {
  if (kind === 'stock') return null;
  const t = Number(today || 0);
  const y = Number(yesterday || 0);
  if (kind === 'ratio') {
    const diff = t - y;
    if (Math.abs(diff) < 1e-9) return {up: false, flat: true, text: '0.00pct', good: true};
    return {up: diff > 0, flat: false, text: Math.abs(diff * 100).toFixed(2) + 'pct', good: diff < 0};
  }
  if (y <= 0) return {up: false, flat: true, text: '—', good: true};
  const rate = (t - y) / y;
  if (Math.abs(rate) < 1e-9) return {up: false, flat: true, text: '0.0%', good: true};
  return {up: rate > 0, flat: false, text: Math.abs(rate * 100).toFixed(1) + '%', good: rate > 0};
};
const deltaClass = (delta) => (delta.flat ? 'text-gray-400' : delta.good ? 'text-green-600' : 'text-red-500');

const kpis = computed(() => {
  const d = data.value;
  return [
    {label: '今日挂号人次', value: String(d?.todayAppointments ?? ''), color: 'blue', drill: 'outpatient',
      delta: buildDelta(d?.todayAppointments, d?.yesterdayAppointments, 'count')},
    {label: '今日入院', value: String(d?.todayAdmissions ?? ''), color: 'indigo', drill: 'inpatient',
      delta: buildDelta(d?.todayAdmissions, d?.yesterdayAdmissions, 'count')},
    {label: '在院人数', value: String(d?.inHospitalCount ?? ''), color: 'green', drill: 'inpatient', delta: null},
    {label: '今日出院', value: String(d?.todayDischargeCount ?? ''), color: 'amber', drill: 'inpatient',
      delta: buildDelta(d?.todayDischargeCount, d?.yesterdayDischargeCount, 'count')},
    {label: '今日手术台次', value: String(d?.todayOperations ?? ''), color: 'teal', drill: 'inpatient',
      delta: buildDelta(d?.todayOperations, d?.yesterdayOperations, 'count')},
    {label: '今日收入', value: d ? money(d.todayRevenue) : '', color: 'purple', drill: 'revenue',
      delta: buildDelta(d?.todayRevenue, d?.yesterdayRevenue, 'money')},
    {label: '药占比', value: d ? pct(d.drugRatio) : '', color: 'red', drill: 'pharmacy',
      delta: buildDelta(d?.drugRatio, d?.yesterdayDrugRatio, 'ratio')},
  ];
});
// 国考四指标卡（M5，近 30 日窗口，分母写进副标题能对上数）
const nationalKpis = computed(() => [
  {
    label: '平均住院日', value: national.value ? Number(national.value.avgLengthOfStay).toFixed(2) + ' 天' : '',
    sub: national.value ? `出院 ${national.value.dischargeCount} 人 / 床日 ${national.value.totalBedDays}` : '',
    color: 'blue', testid: 'bi-nat-los', drill: 'inpatient',
  },
  {
    label: '床位周转次数', value: national.value ? Number(national.value.bedTurnover).toFixed(2) : '',
    sub: national.value ? `出院 ${national.value.dischargeCount} 人 / 可用床位 ${national.value.usableBeds} 张` : '',
    color: 'teal', testid: 'bi-nat-turnover', drill: 'inpatient',
  },
  {
    label: '耗占比', value: national.value ? pct(national.value.materialRatio) : '',
    sub: national.value ? `耗材 ${money(national.value.materialRevenue)} / 收入 ${money(national.value.revenue)}` : '',
    color: 'amber', testid: 'bi-nat-material', drill: 'revenue',
  },
  {
    label: 'CMI（模拟）', value: national.value ? (national.value.cmi == null ? '—' : Number(national.value.cmi).toFixed(4)) : '',
    sub: national.value ? `入组 ${national.value.cmiGroupedCount} / 样本 ${national.value.cmiSampleCount} 例` : '',
    color: 'purple', testid: 'bi-nat-cmi', drill: 'inpatient',
  },
  {
    label: '门诊次均费用', value: national.value ? money(national.value.outpatientAvgCost) : '',
    sub: national.value ? `门诊收入 ${money(national.value.outpatientRevenue)} / ${national.value.outpatientVisits} 人次` : '',
    color: 'blue', testid: 'bi-nat-out-avg', drill: 'outpatient',
  },
  {
    label: '住院次均费用', value: national.value ? money(national.value.inpatientAvgCost) : '',
    sub: national.value ? `住院收入 ${money(national.value.inpatientRevenue)} / 出院 ${national.value.dischargeCount} 人` : '',
    color: 'teal', testid: 'bi-nat-in-avg', drill: 'inpatient',
  },
]);
// 质量与安全卡（近 30 日）：手术结构 / 医保 / 满意度 / 不良事件 / 院内感染
const qualityKpis = computed(() => {
  const n = national.value;
  return [
    {
      label: '三四级手术占比', value: n ? pct(n.level34Ratio) : '',
      sub: n ? `三/四级 ${n.operationLevel34} 台 / 主手术 ${n.operationTotal} 台` : '',
      color: 'amber', testid: 'bi-nat-op34',
    },
    {
      label: '医保收入占比', value: n ? pct(n.insuranceRatio) : '',
      sub: n ? `医保 ${money(n.insuranceRevenue)} / 费用 ${money(n.settleRevenue)}` : '',
      color: 'blue', testid: 'bi-nat-insurance',
    },
    {
      label: '满意率', value: n ? (n.satisfiedRatio == null ? '—' : pct(n.satisfiedRatio)) : '',
      sub: n ? `均分 ${n.avgScore == null ? '—' : Number(n.avgScore).toFixed(2)} / 回收 ${n.surveyCount} 份` : '',
      color: 'green', testid: 'bi-nat-satisfy',
    },
    {
      label: '不良事件上报', value: n ? String(n.adverseEventCount) : '',
      sub: n ? '近 30 日上报起数' : '',
      color: 'red', testid: 'bi-nat-adverse',
    },
    {
      label: '院内感染', value: n ? String(n.infectionCount) : '',
      sub: n ? '近 30 日已确认例数' : '',
      color: 'purple', testid: 'bi-nat-infection',
    },
  ];
});
// 近 7 日门诊量柱图
const apptBars = computed(() => {
  const rows = data.value?.appointmentTrend || [];
  const max = Math.max(1, ...rows.map(r => Number(r.count || 0)));
  return rows.map(r => ({date: r.date, count: Number(r.count || 0), h: (Number(r.count || 0) / max) * 130}));
});
// 近 7 日收入柱图（门诊/住院双系列）
const revBars = computed(() => {
  const rows = data.value?.revenueTrend || [];
  const max = Math.max(1, ...rows.map(r => Math.max(Number(r.amount || 0), Number(r.outpatientAmount || 0), Number(r.inpatientAmount || 0))));
  return rows.map(r => ({
    date: r.date, amount: Number(r.amount || 0),
    out: Number(r.outpatientAmount || 0), in: Number(r.inpatientAmount || 0),
    h: (Number(r.amount || 0) / max) * 130,
    outH: (Number(r.outpatientAmount || 0) / max) * 130,
    inH: (Number(r.inpatientAmount || 0) / max) * 130,
  }));
});
// 近 30 日出院人次 TOP5 科室
const workloadBars = computed(() => {
  const rows = data.value?.deptWorkloadTop || [];
  const max = Math.max(1, ...rows.map(r => Number(r.count || 0)));
  return rows.map(r => ({
    deptId: r.deptId, name: r.deptName, count: Number(r.count || 0),
    w: Math.max(4, (Number(r.count || 0) / max) * 420),
  }));
});
// 科室 TOP5 条形
const deptBars = computed(() => {
  const rows = data.value?.deptTop || [];
  const max = Math.max(1, ...rows.map(r => Number(r.amount || 0)));
  return rows.map(r => ({
    deptId: r.deptId, name: r.deptName, amount: Number(r.amount || 0),
    w: Math.max(4, (Number(r.amount || 0) / max) * 420),
  }));
});
// 收入结构六类条形（药品为西药/中成药/饮片归并）
const STRUCTURE_COLORS = {
  registration: '#94a3b8', drug: '#6366f1', exam: '#3b82f6',
  lab: '#10b981', treatment: '#f59e0b', consumable: '#ec4899', other: '#9ca3af',
};
const structureBars = computed(() => {
  const rows = data.value?.revenueStructure || [];
  const total = rows.reduce((s, r) => s + Number(r.amount || 0), 0);
  const max = Math.max(1, ...rows.map(r => Number(r.amount || 0)));
  return rows.map(r => ({
    code: r.code, name: r.name, amount: Number(r.amount || 0),
    ratio: total > 0 ? Number(r.amount || 0) / total : 0,
    w: Math.max(4, (Number(r.amount || 0) / max) * 420),
  }));
});
onMounted(load);
</script>
