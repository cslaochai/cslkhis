<template>
  <div class="space-y-6">
    <!-- 查询条 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex flex-wrap items-end gap-3">
        <div class="w-80" data-testid="p5-cdr-patient-select">
          <p class="mb-1 text-xs text-slate-500">患者</p>
          <PatientSelect
              v-model="query.patientId"
              placeholder="搜索患者：姓名、患者号、手机号、身份证号"
              @select="onSelectPatient"
          />
        </div>
        <div>
          <p class="mb-1 text-xs text-slate-500">事件类型</p>
          <el-select
              v-model="query.eventType"
              class="!w-52"
              clearable
              data-testid="p5-cdr-filter-type"
              placeholder="全部类型"
          >
            <el-option v-for="d in eventDict" :key="d.code" :label="d.text" :value="d.code"/>
          </el-select>
        </div>
        <div>
          <p class="mb-1 text-xs text-slate-500">日期区间</p>
          <el-date-picker
              v-model="query.dateRange"
              class="!w-64"
              end-placeholder="结束日期"
              range-separator="至"
              start-placeholder="开始日期"
              type="daterange"
              value-format="YYYY-MM-DD"
          />
        </div>
        <el-button :icon="Search" data-testid="p5-cdr-search" type="primary" @click="load">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </div>
    </div>

    <!-- 未选患者 -->
    <div
        v-if="!patient && !loading"
        class="rounded-lg border border-dashed border-slate-300 bg-white px-6 py-16 text-center"
        data-testid="p5-cdr-empty"
    >
      <el-icon class="mb-3 text-3xl text-slate-300">
        <Document/>
      </el-icon>
      <p class="text-sm text-slate-500">先在上方选择一位患者，这里会显示他/她的完整就诊脉络</p>
    </div>

    <template v-if="patient">
      <!-- 提示 -->
      <div v-if="(data.warnings || []).length" class="space-y-2" data-testid="p5-cdr-warnings">
        <el-alert
            v-for="(w, i) in data.warnings"
            :key="i"
            :closable="false"
            :title="w"
            class="!items-start"
            show-icon
            type="warning"
        />
      </div>

      <!-- 临床摘要（画像条）：接诊前 10 秒看"这是个什么病人" -->
      <div
          v-if="clinical"
          class="rounded-lg border border-slate-200 bg-white shadow-sm overflow-hidden"
          data-testid="p6-cdr-clinical-bar"
      >
        <div class="flex flex-wrap items-center justify-between gap-2 border-b border-slate-200 bg-slate-50 px-4 py-2">
          <p class="text-sm font-medium text-slate-700">临床摘要（画像）</p>
          <span class="text-xs text-slate-400">
            异常检验窗口 90 天 · 就诊/重复检查窗口 30 天 · 与用药安全（P6）共用口径
          </span>
        </div>

        <div class="grid grid-cols-1 gap-4 p-4 lg:grid-cols-4">
          <!-- 过敏 -->
          <div data-testid="p6-cdr-allergy">
            <p class="mb-1 text-xs font-medium text-slate-500">过敏</p>
            <template v-if="clinical.allergyPositive">
              <div v-for="(a, ai) in allergyList" :key="ai" class="mb-1 text-sm leading-5">
                <el-tag class="mr-1" effect="dark" size="small" type="danger">过敏</el-tag>
                <span class="font-medium text-slate-800">{{ a.allergenName }}</span>
                <span v-if="a.allergySeverity" class="ml-1 text-red-600">{{ a.allergySeverity }}</span>
                <span v-if="a.allergySymptoms" class="text-xs text-slate-500"> · {{ a.allergySymptoms }}</span>
                <span class="ml-1 text-[11px] text-slate-400">（{{ allergySourceText(a.source) }}）</span>
              </div>
            </template>
            <p v-else class="text-sm text-slate-400">无过敏记录（文本与结构化档案均空）</p>
          </div>

          <!-- 慢病/重大病史 -->
          <div data-testid="p6-cdr-chronic">
            <p class="mb-1 text-xs font-medium text-slate-500">慢病 / 重大病史</p>
            <template v-if="chronicTags.length">
              <el-tag
                  v-for="t in chronicTags"
                  :key="t"
                  class="mr-1 mb-1"
                  effect="plain"
                  size="small"
                  type="warning"
              >
                {{ t }}
              </el-tag>
            </template>
            <p v-else class="text-sm text-slate-400">未命中慢病关键词</p>
          </div>

          <!-- 危急值 / 异常检验 -->
          <div data-testid="p6-cdr-risk">
            <p class="mb-1 text-xs font-medium text-slate-500">危急值 / 异常检验</p>
            <p class="text-sm">
              危急值
              <span
                  :class="(criticalPart.openCount || 0) > 0 ? 'text-red-600' : 'text-slate-800'"
                  class="font-bold"
                  data-testid="p6-cdr-critical-open"
              >
                {{ criticalPart.totalCount ?? 0 }}
              </span>
              条
              <span v-if="(criticalPart.openCount || 0) > 0" class="text-red-600">
                （{{ criticalPart.openCount }} 条未处置）
              </span>
            </p>
            <p class="text-sm" data-testid="p6-cdr-abnormal">
              近 90 天异常检验
              <span :class="(abnormalPart.abnormalCount || 0) > 0 ? 'text-red-600' : 'text-slate-800'"
                    class="font-bold">
                {{ abnormalPart.abnormalCount ?? 0 }}
              </span>
              项
              <span v-if="(abnormalPart.unjudgedCount || 0) > 0" class="text-amber-600">
                （{{ abnormalPart.unjudgedCount }} 项未判定≠正常）
              </span>
            </p>
            <p v-if="(abnormalPart.items || []).length" class="mt-1 text-xs text-slate-500">
              最近：{{ abnormalPart.items[0].itemName }} {{
                abnormalPart.items[0].resultValue
              }}{{ abnormalPart.items[0].resultUnit }}（{{ abnormalPart.items[0].flagText }}）
            </p>
            <p v-else-if="(criticalPart.items || []).length" class="mt-1 text-xs text-slate-500">
              最近：{{ criticalPart.items[0].itemName }} {{
                criticalPart.items[0].resultValue
              }}{{ criticalPart.items[0].resultUnit }}（{{ criticalPart.items[0].statusText }}）
            </p>
          </div>

          <!-- 30 天就诊 + 重复检查 -->
          <div data-testid="p6-cdr-visits30d">
            <p class="mb-1 text-xs font-medium text-slate-500">近 30 天就诊</p>
            <p class="text-sm text-slate-800">
              门诊 <span class="font-bold">{{ visits30d.outpatientCount ?? 0 }}</span> /
              急诊 <span class="font-bold">{{ visits30d.emergencyCount ?? 0 }}</span> /
              住院 <span class="font-bold">{{ visits30d.inpatientCount ?? 0 }}</span>
            </p>
            <template v-if="repeatExams.length">
              <p class="mt-1 text-xs font-medium text-amber-700" data-testid="p6-cdr-repeat">
                重复检查提醒：
              </p>
              <p v-for="r in repeatExams" :key="r.itemName" class="text-xs text-amber-700">
                「{{ r.itemName }}」{{ r.count }} 次
              </p>
            </template>
          </div>
        </div>

        <!-- 风险结论 -->
        <div
            :class="clinicalWarnings.length ? 'border-amber-100 bg-amber-50 text-amber-800' : 'border-slate-100 bg-slate-50 text-slate-400'"
            class="flex flex-wrap items-start gap-x-4 gap-y-1 border-t px-4 py-2 text-xs"
            data-testid="p6-cdr-clinical-warnings"
        >
          <template v-if="clinicalWarnings.length">
            <span v-for="(w, wi) in clinicalWarnings" :key="wi" class="flex items-center gap-1">
              <el-icon><WarningFilled/></el-icon>{{ w }}
            </span>
          </template>
          <span v-else>暂无风险标记（不代表无风险：未判定结果与未填写的档案不在提醒之列）</span>
        </div>
      </div>

      <!-- 身份卡 -->
      <div
          class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
          data-testid="p5-cdr-patient-card"
      >
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p class="text-base font-semibold text-slate-900">
              {{ patient.patientNo }} {{ patient.patientName }}
              <span class="ml-2 text-sm font-normal text-slate-500">
                {{ patient.genderText }} / {{ patient.age ?? '—' }}岁
              </span>
              <el-tag
                  v-if="Number(patient.mergeStatus) === 1"
                  class="ml-2"
                  size="small"
                  type="warning"
              >
                已并入主档
              </el-tag>
            </p>
            <div class="mt-2 grid grid-cols-1 gap-x-8 gap-y-1 text-sm text-slate-600 sm:grid-cols-2">
              <span>身份证号：{{ patient.idCard || '（空）' }}</span>
              <span>手机号：{{ patient.phone || '（空）' }}</span>
              <span>出生日期：{{ patient.birthDate || '（空）' }}</span>
              <span>血型：{{ patient.bloodType || '（空）' }}</span>
              <span>医保类型：{{ patient.medicalInsuranceType || '（空）' }}</span>
              <span>住址：{{ patient.address || '（空）' }}</span>
            </div>
          </div>
          <div class="min-w-[220px]">
            <p class="text-xs text-slate-500">档案完整度</p>
            <p
                :class="patient.completeRate >= 80 ? 'text-emerald-600' : 'text-amber-600'"
                class="text-lg font-bold"
                data-testid="p5-cdr-complete-rate"
            >
              {{ patient.completeRate }}%
            </p>
            <div class="mt-1 flex flex-wrap gap-1">
              <el-tag
                  v-for="f in patient.missingFields || []"
                  :key="f"
                  effect="plain"
                  size="small"
                  type="warning"
              >
                缺 {{ f }}
              </el-tag>
              <span v-if="!(patient.missingFields || []).length" class="text-xs text-emerald-600">
                关键字段已填全
              </span>
            </div>
            <p
                v-if="(patient.shadowArchives || []).length"
                class="mt-2 text-xs text-amber-700"
                data-testid="p5-cdr-shadow"
            >
              含 {{ patient.shadowArchives.length }} 份被并档案的数据：
              {{ patient.shadowArchives.map((x: any) => x.patientNo).join('、') }}
            </p>
          </div>
        </div>
      </div>

      <!-- 概览 -->
      <div class="grid grid-cols-2 gap-4 lg:grid-cols-6" data-testid="p5-cdr-summary">
        <div class="rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <p class="text-xs text-slate-500">就诊次</p>
          <p class="text-lg font-bold text-slate-900" data-testid="p5-cdr-summary-visits">
            {{ summary.visitCount }}
          </p>
          <p class="text-[11px] text-slate-400">
            门诊 {{ summary.outpatientCount }} / 住院 {{ summary.inpatientCount }} / 急诊
            {{ summary.emergencyCount }}
          </p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <p class="text-xs text-slate-500">在院</p>
          <p :class="summary.activeInpatientCount > 0 ? 'text-red-600' : 'text-slate-900'" class="text-lg font-bold">
            {{ summary.activeInpatientCount }}
          </p>
          <p class="text-[11px] text-slate-400">未出院的住院次数</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <p class="text-xs text-slate-500">事件总数</p>
          <p class="text-lg font-bold text-slate-900" data-testid="p5-cdr-summary-events">
            {{ summary.eventCount }}
          </p>
          <p class="text-[11px] text-slate-400">已归入就诊次</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <p class="text-xs text-slate-500">未归位</p>
          <p
              :class="summary.unresolvedEventCount > 0 ? 'text-amber-600' : 'text-slate-900'"
              class="text-lg font-bold"
              data-testid="p5-cdr-summary-unresolved"
          >
            {{ summary.unresolvedEventCount }}
          </p>
          <p class="text-[11px] text-slate-400">归属不到就诊次的记录</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <p class="text-xs text-slate-500">累计费用</p>
          <p class="text-lg font-bold text-slate-900">{{ money(summary.totalAmount) }}</p>
          <p class="text-[11px] text-slate-400">按就诊次合计</p>
        </div>
        <div class="rounded-lg border border-slate-200 bg-white p-3 shadow-sm">
          <p class="text-xs text-slate-500">就诊时间跨度</p>
          <p class="text-sm font-semibold text-slate-900">
            {{ (summary.firstVisitTime || '—').substring(0, 10) }}
          </p>
          <p class="text-[11px] text-slate-400">至 {{ (summary.lastVisitTime || '—').substring(0, 10) }}</p>
        </div>
      </div>

      <!-- 健康档案 -->
      <div
          v-if="profile.length"
          class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
          data-testid="p5-cdr-profile"
      >
        <p class="mb-3 text-sm font-medium text-slate-700">健康档案（不属于某一次就诊）</p>
        <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">
          <div v-for="g in profile" :key="g.key">
            <p class="mb-1 text-xs font-medium text-slate-500">{{ g.label }}（{{ g.count }}）</p>
            <ul class="space-y-1">
              <li v-for="it in g.items" :key="it.id" class="text-sm text-slate-700">
                <span class="font-medium">{{ it.title }}</span>
                <span v-if="it.summary" class="text-slate-500"> · {{ it.summary }}</span>
                <span v-if="it.time" class="text-xs text-slate-400"> （{{ it.time.substring(0, 10) }}）</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      <!-- 时间轴 -->
      <div
          v-loading="loading"
          class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm"
          data-testid="p5-cdr-timeline"
      >
        <div class="mb-4 flex items-center justify-between">
          <p class="text-sm font-medium text-slate-700">就诊脉络（按时间倒序）</p>
          <span class="text-xs text-slate-400">共 {{ visits.length }} 个节点</span>
        </div>

        <el-empty
            v-if="!visits.length && !unresolved.length"
            data-testid="p5-cdr-timeline-empty"
            description="该患者没有任何就诊记录"
        />

        <el-timeline v-if="visits.length">
          <el-timeline-item
              v-for="(v, vi) in visits"
              :key="v.nodeKey"
              :color="nodeColor(v.nodeType)"
              :hollow="v.nodeType === 'PATIENT'"
              placement="top"
              size="large"
          >
            <div
                :data-testid="`p5-cdr-node-${vi}`"
                class="rounded-lg border border-slate-200 bg-slate-50/60"
            >
              <!-- 节点头 -->
              <div class="flex flex-wrap items-center gap-2 border-b border-slate-200 px-4 py-2">
                <el-tag :type="nodeTypeTag(v.nodeType)" effect="dark" size="small">
                  {{ v.nodeTypeText }}
                </el-tag>
                <span :data-testid="`p5-cdr-node-title-${vi}`" class="text-sm font-semibold text-slate-800">
                  {{ v.title }}
                </span>
                <span v-if="v.anchorNo" class="text-xs text-slate-500">{{ v.anchorNo }}</span>
                <span v-if="v.subtitle" class="text-xs text-slate-400">{{ v.subtitle }}</span>
                <el-tag v-if="v.fromShadow" effect="plain" size="small" type="warning">
                  数据来自被并档案
                </el-tag>
                <span class="ml-auto flex items-center gap-3 text-xs text-slate-500">
                  <span v-if="v.statusText">{{ v.statusText }}</span>
                  <span v-if="v.durationDays">{{ v.durationDays }} 天</span>
                  <span v-if="v.totalAmount !== null && Number(v.totalAmount) !== 0">{{ money(v.totalAmount) }}</span>
                  <span :data-testid="`p5-cdr-node-count-${vi}`">{{ v.eventCount }} 条记录</span>
                </span>
              </div>

              <!-- 时间与结局 -->
              <div class="flex flex-wrap items-center gap-x-4 gap-y-1 px-4 py-2 text-xs text-slate-500">
                <span class="flex items-center gap-1">
                  <el-icon><Clock/></el-icon>
                  {{ v.startTime || '—' }}
                  <template v-if="v.endTime"> ~ {{ v.endTime }}</template>
                </span>
                <span v-if="v.deptName">科室：{{ v.deptName }}</span>
                <span v-if="v.operatorName">医生：{{ v.operatorName }}</span>
                <span v-if="v.outcome">诊断/结局：{{ v.outcome }}</span>
              </div>

              <!-- 缺口 -->
              <div
                  v-if="(v.gaps || []).length"
                  :data-testid="`p5-cdr-node-gaps-${vi}`"
                  class="flex items-start gap-2 border-t border-amber-100 bg-amber-50 px-4 py-2 text-xs text-amber-700"
              >
                <el-icon class="mt-0.5 shrink-0">
                  <WarningFilled/>
                </el-icon>
                <span>
                  病历完整性缺口：
                  <el-tag
                      v-for="g in v.gaps"
                      :key="g"
                      class="ml-1"
                      effect="plain"
                      size="small"
                      type="warning"
                  >
                    {{ g }}
                  </el-tag>
                </span>
              </div>

              <!-- 事件列表 -->
              <div v-if="v.events.length" class="divide-y divide-slate-100">
                <div
                    v-for="(e, ei) in v.events"
                    :key="e.eventType + '#' + e.sourceId"
                    :data-testid="`p5-cdr-event-${vi}-${ei}`"
                    class="flex flex-wrap items-start gap-x-3 gap-y-1 px-4 py-2 hover:bg-white"
                >
                  <span class="w-24 shrink-0 font-mono text-xs text-slate-400">
                    {{ eventTime(e.eventTime).substring(5) }}
                  </span>
                  <el-tag :type="eventTag(e.eventType)" class="shrink-0" effect="plain" size="small">
                    {{ e.eventTypeText }}
                  </el-tag>
                  <span class="min-w-[220px] flex-1">
                    <span class="text-sm text-slate-800">{{ e.title }}</span>
                    <span v-if="e.summary" class="ml-2 text-xs text-slate-500">{{ e.summary }}</span>
                  </span>
                  <span class="flex items-center gap-2 text-xs text-slate-500">
                    <span v-if="e.secondaryText">{{ e.secondaryLabel }}：{{ e.secondaryText }}</span>
                    <span v-if="e.deptName">{{ e.deptName }}</span>
                    <span v-if="e.operatorName">{{ e.operatorName }}</span>
                    <el-tag v-if="e.statusText" effect="plain" size="small">{{ e.statusText }}</el-tag>
                    <span v-if="e.amount !== null && e.amount !== undefined" class="text-slate-700">
                      {{ e.amountLabel ? e.amountLabel + ' ' : '' }}{{ money(e.amount) }}
                    </span>
                    <span
                        v-if="e.ownerArchiveNo"
                        :title="`该记录挂在被并档案 ${e.ownerArchiveNo} 下`"
                        class="text-amber-600"
                    >
                      来源档案 {{ e.ownerArchiveNo }}
                    </span>
                  </span>
                </div>
              </div>
              <!-- 没有事件：过滤时要说明"是筛掉的"，没过滤时就是"这个就诊次确实没数据" -->
              <div v-else class="px-4 py-2 text-xs text-slate-400">
                {{ query.eventType ? '该就诊次下没有符合筛选条件的记录' : '该就诊次下没有任何记录' }}
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 未归位事件 -->
      <div
          v-if="unresolved.length"
          class="rounded-lg border border-amber-200 bg-white p-4 shadow-sm"
          data-testid="p5-cdr-unresolved"
      >
        <p class="mb-1 text-sm font-medium text-amber-700">
          归属不到就诊次的记录（{{ unresolved.length }} 条）
        </p>
        <p class="mb-3 text-xs text-slate-500">
          这些记录指向的挂号 / 入院记录不存在，或不属于该患者 —— 单列出来便于核实，
          不会因为它们挂不上而消失。
        </p>
        <div class="divide-y divide-slate-100">
          <div
              v-for="(e, i) in unresolved"
              :key="'u' + i"
              :data-testid="`p5-cdr-unresolved-${i}`"
              class="flex flex-wrap items-center gap-x-3 gap-y-1 py-2"
          >
            <span class="w-24 shrink-0 font-mono text-xs text-slate-400">
              {{ eventDay(e.eventTime) }}
            </span>
            <el-tag :type="eventTag(e.eventType)" effect="plain" size="small">{{ e.eventTypeText }}</el-tag>
            <span class="text-sm text-slate-800">{{ e.title }}</span>
            <span v-if="e.anchorId" class="text-xs text-slate-400">
              锚点 {{ e.anchorType }}#{{ e.anchorId }}
            </span>
            <span class="ml-auto text-xs text-slate-400">来源 {{ e.sourceTable }}</span>
          </div>
        </div>
      </div>

      <!-- 事件分类统计（与源表对账用） -->
      <div
          v-if="(summary.eventCounts || []).length"
          class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
          data-testid="p5-cdr-event-counts"
      >
        <p class="mb-3 text-sm font-medium text-slate-700">各类记录条数（可与源系统对账）</p>
        <div class="flex flex-wrap gap-2">
          <el-tag v-for="c in summary.eventCounts" :key="c.key" effect="plain" size="small">
            {{ c.label }} {{ c.count }}
          </el-tag>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
/**
 * 患者全景时间轴（CDR / P5.2）
 *
 * 这个页面对应「临床数据整合」的核心功能角色：把散在门诊、住院、医技、收费、病案各处的记录，
 * 按"这个患者来过医院几次"重新讲一遍。医生看一眼就知道：这个人之前来过没有、每次做了什么、
 * 有没有该有的文书缺失、有没有单据悬空。
 *
 * 三条必须写在页面上的口径（否则使用者会误读）：
 *   - 时间轴的骨架是**就诊次**，不是某张表；
 *   - 缺口（gaps）是"病历该有的东西没有"，是**提示**不是报错；
 *   - 归属不到就诊次的记录会单列出来，不会被悄悄丢掉。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {useRoute} from 'vue-router';
import {Clock, Document, Search, WarningFilled} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {getCdrEventDict, getClinicalSummary, getPatientCdr} from '@/api/cdr';
import PatientSelect from '@/components/his/PatientSelect.vue';

const loading = ref(false);
const data = ref(null);
const eventDict = ref([]);
const clinical = ref(null);
const route = useRoute();
const query = reactive({
  patientId: '',
  eventType: '',
  dateRange: [],
});
const loadDict = async () => {
  try {
    const res = await getCdrEventDict();
    eventDict.value = res.data || [];
  } catch {
    eventDict.value = [];
  }
};
const load = async () => {
  if (!query.patientId) {
    ElMessage.warning('请先选择患者');
    return;
  }
  loading.value = true;
  try {
    const params = {patientId: String(query.patientId)};
    if (query.eventType)
      params.eventType = query.eventType;
    if (query.dateRange && query.dateRange.length === 2) {
      params.startDate = query.dateRange[0];
      params.endDate = query.dateRange[1];
    }
    // 摘要条与时间轴并行取数：摘要失败不挡时间轴，但要在控制台可见（不允许静默吞）
    const [res, cs] = await Promise.allSettled([getPatientCdr(params), getClinicalSummary({patientId: String(query.patientId)})]);
    if (res.status === 'fulfilled') {
      data.value = res.value.data;
    } else {
      throw res.reason;
    }
    if (cs.status === 'fulfilled') {
      clinical.value = cs.value.data;
    } else {
      clinical.value = null;
      console.error('临床摘要加载失败', cs.reason);
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载患者全景失败');
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  query.eventType = '';
  query.dateRange = [];
  if (query.patientId)
    load();
};
const onSelectPatient = (p) => {
  query.patientId = p?.id ? String(p.id) : '';
  if (query.patientId)
    load();
};
/* ---------- 展示辅助 ---------- */
const nodeTypeTag = (t) => t === 'INPATIENT' ? 'danger' : t === 'EMERGENCY' ? 'warning' : t === 'OUTPATIENT' ? 'primary' : 'info';
const nodeColor = (t) => t === 'INPATIENT' ? '#dc2626' : t === 'EMERGENCY' ? '#d97706' : t === 'OUTPATIENT' ? '#1269B5' : '#64748b';
const eventTag = (t) => {
  if (['criticalValue'].includes(t))
    return 'danger';
  if (['qualityControl', 'referral'].includes(t))
    return 'warning';
  if (['charge', 'prepay', 'inpatientSettlement', 'insuranceSettlement'].includes(t))
    return 'success';
  return 'info';
};
const money = (v) => (v === null || v === undefined ? '—' : `¥${Number(v).toFixed(2)}`);
const visits = computed(() => data.value?.visits || []);
const patient = computed(() => data.value?.patient || null);
const summary = computed(() => data.value?.summary || null);
const unresolved = computed(() => data.value?.unresolvedEvents || []);
const profile = computed(() => (data.value?.profile || []).filter((g) => g.count > 0));
/* ---------- 临床摘要（画像条） ---------- */
const allergyList = computed(() => clinical.value?.allergies || []);
const chronicTags = computed(() => clinical.value?.chronicTags || []);
const criticalPart = computed(() => clinical.value?.criticalValues || {});
const abnormalPart = computed(() => clinical.value?.abnormalLabs || {});
const visits30d = computed(() => clinical.value?.visits30d || {});
const repeatExams = computed(() => clinical.value?.repeatExams || []);
const clinicalWarnings = computed(() => clinical.value?.warnings || []);
const allergySourceText = (s) => s === 'STRUCTURED' ? '结构化档案' : s === 'TEXT' ? '档案自述' : '未知(' + s + ')';
const eventTime = (t) => (t ? t.substring(0, 16) : '—');
const eventDay = (t) => (t ? t.substring(0, 10) : '—');
onMounted(async () => {
  await loadDict();
  // 支持 /cdr?patientId=xxx 直接带入患者（患者详情弹框的「打开完整时间轴」入口走这里）
  const pid = route.query.patientId;
  if (pid) {
    query.patientId = String(pid);
    load();
  }
});
</script>
