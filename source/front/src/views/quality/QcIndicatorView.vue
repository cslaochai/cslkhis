<template>
  <div>
    <div class="mb-3 flex flex-wrap items-center gap-2">
      <el-select v-model="days" style="width: 140px" @change="load">
        <el-option :value="30" label="近 30 天"/>
        <el-option :value="90" label="近 90 天"/>
        <el-option :value="180" label="近 180 天"/>
        <el-option :value="365" label="近 365 天"/>
      </el-select>
      <el-button :icon="Refresh" circle @click="load"/>
      <span class="ml-auto text-xs text-slate-400">数据源：质控单、缺陷明细、病案首页（只读统计）</span>
    </div>

    <div v-loading="loading" class="grid grid-cols-2 gap-3 lg:grid-cols-3">
      <div v-for="card in cards" :key="card.label" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="text-xs text-slate-500">{{ card.label }}</div>
        <div :class="card.tone" class="mt-1 text-2xl font-semibold">{{ card.value }}</div>
        <div class="mt-1 text-xs text-slate-400">{{ card.hint }}</div>
      </div>
    </div>

    <div class="mt-3 rounded-lg border border-slate-200 bg-white shadow-sm">
      <div class="flex items-center justify-between border-b border-slate-100 px-4 py-3">
        <span class="text-sm font-medium">科室排名（按质控单数）</span>
        <span class="text-xs text-slate-400">共 {{ data?.totalQc ?? 0 }} 张质控单，平均 {{
            data?.avgScore ?? '—'
          }} 分</span>
      </div>
      <el-table :data="data?.deptRanks || []" size="default">
        <el-table-column label="科室" min-width="160" prop="deptName"/>
        <el-table-column label="质控单数" prop="totalQc" width="110"/>
        <el-table-column label="通过数" prop="passQc" width="100"/>
        <el-table-column label="通过率" width="110">
          <template #default="{ row }">{{ pct(row.passRate) }}</template>
        </el-table-column>
        <el-table-column label="平均分" prop="avgScore" width="110"/>
        <template #empty>
          <div class="py-6 text-sm text-slate-400">窗口期内没有质控数据</div>
        </template>
      </el-table>
    </div>

    <div class="mt-3 rounded-lg border border-slate-200 bg-slate-50 p-4 text-xs leading-6 text-slate-600">
      <p class="font-medium text-slate-700">口径说明</p>
      <p>通过率 = 无重要及以上问题（最高严重度 &lt; 2）的质控单 / 质控单总数；有否决项（严重度 3）必为丙级且不通过。</p>
      <p>甲级率 = 无重要及以上问题且得分 ≥ 90 的质控单 / 质控单总数；乙级为 75~89 分。</p>
      <p>时限达标率 = 1 − 命中「入院记录 24h / 首次病程 8h / 出院记录 24h」任一时限规则的住院文书数 / 住院质控文书数。</p>
      <p>首页完整率 = 主诊断、主诊断编码、入院途径三项齐全的病案首页 / 首页总数；首页缺项会直接影响 DRG/DIP 入组。</p>
      <p>7 日归档率 = 出院后 7 日内归档的病历 / 已出院病历（按住院登记去重）；未归档一律计入未达标，不从分母剔除。</p>
      <p>签名完整率 = 已完成电子签名的文书 / 已提交及以上的文书；未签名的电子病历不具备法律效力，归档前必须补签。</p>
      <p>比率为「—」表示分母为 0（窗口期内没有数据），不是 0 分。</p>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {Refresh} from '@element-plus/icons-vue';
import {getQcIndicators} from '@/api/qualityControl';

const loading = ref(false);
const days = ref(30);
const data = ref(null);

const pct = (v) => (v === null || v === undefined ? '—' : `${Number(v).toFixed(1)}%`);

const cards = computed(() => {
  const d = data.value || {};
  return [
    {
      label: '通过率',
      value: pct(d.passRate),
      hint: `${d.passQc ?? 0} / ${d.totalQc ?? 0} 张通过（否决项 ${d.vetoQc ?? 0} 张）`,
      tone: 'text-emerald-600',
    },
    {
      label: '甲级率',
      value: pct(d.gradeARate),
      hint: '无重要问题且 ≥90 分',
      tone: 'text-sky-600',
    },
    {
      label: '时限达标率',
      value: pct(d.deadlineRate),
      hint: `${d.deadlineViolation ?? 0} / ${d.inpatientQc ?? 0} 份住院文书超时`,
      tone: 'text-amber-600',
    },
    {
      label: '首页完整率',
      value: pct(d.summaryCompleteRate),
      hint: `${d.summaryComplete ?? 0} / ${d.summaryTotal ?? 0} 份首页三项齐全`,
      tone: 'text-rose-600',
    },
    {
      label: '7 日归档率',
      value: pct(d.archiveRate),
      hint: `${d.archiveOnTime ?? 0} / ${d.archiveTotal ?? 0} 份出院病历 7 日内归档`,
      tone: 'text-violet-600',
    },
    {
      label: '签名完整率',
      value: pct(d.signRate),
      hint: `${d.signDone ?? 0} / ${d.signTotal ?? 0} 份已提交文书完成电子签名`,
      tone: 'text-teal-600',
    },
  ];
});

async function load() {
  loading.value = true;
  try {
    const res = await getQcIndicators(days.value);
    data.value = res.data || null;
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>
