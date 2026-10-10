<template>
  <div v-loading="loading" class="space-y-6">
    <div class="flex flex-wrap items-end justify-between gap-3">
      <div class="flex items-center gap-2">
        <el-date-picker
            v-model="range"
            :clearable="false"
            :shortcuts="shortcuts"
            end-placeholder="结束日期"
            range-separator="至"
            start-placeholder="开始日期"
            style="width: 280px"
            type="daterange"
            value-format="YYYY-MM-DD"
        />
        <el-button :loading="loading" type="primary" @click="loadData">查询</el-button>
      </div>
    </div>

    <!-- Summary Cards -->
    <div class="grid grid-cols-1 gap-4 sm:grid-cols-4">
      <div v-for="item in [
        { label: '门诊量', value: fmtInt(d.opVisitTotal), sub: `退号 ${fmtInt(d.opRefundCount)} · 初诊 ${fmtInt(d.opFirstVisitCount)} / 复诊 ${fmtInt(d.opRevisitCount)}`, icon: User, color: 'text-blue-600', bg: 'bg-blue-50' },
        { label: '收入净额', value: `${fmtWan(d.revTotal)}`, sub: `退费 ${fmtWan(d.revRefundAmount)} · ${fmtInt(d.revRefundCount)} 单`, icon: Money, color: 'text-emerald-600', bg: 'bg-emerald-50' },
        { label: '入出院人次', value: `${fmtInt(d.ipAdmitCount)} / ${fmtInt(d.ipDischargeCount)}`, sub: `在院 ${fmtInt(d.ipInCur)} 人（当前）`, icon: Suitcase, color: 'text-purple-600', bg: 'bg-purple-50' },
        { label: '床位使用率', value: `${d.bedUseRate ?? 0}%`, sub: `占用 ${fmtInt(d.bedOccupied)} / 编制 ${fmtInt(d.bedTotal)} 张`, icon: DataLine, color: 'text-amber-600', bg: 'bg-amber-50' },
        { label: '药占比', value: `${d.drugRatio ?? 0}%`, sub: `药品收入 ${fmtWan(d.revDrug)}`, icon: FirstAidKit, color: 'text-rose-600', bg: 'bg-rose-50' },
        { label: '医保结算占比', value: `${insShare}%`, sub: '按门诊挂号结算方式', icon: Tickets, color: 'text-cyan-600', bg: 'bg-cyan-50' },
        { label: '出院平均住院日', value: `${d.ipAvgLosDays ?? 0} 天`, sub: `占用 ${fmtInt(d.ipBedDays)} 床日`, icon: Timer, color: 'text-indigo-600', bg: 'bg-indigo-50' },
        { label: '处方数', value: fmtInt(d.phPrescCount), sub: `审方退回 ${fmtInt(d.phAuditReturnCount)} 次`, icon: TrendCharts, color: 'text-teal-600', bg: 'bg-teal-50' },
      ]" :key="item.label" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div :class="['rounded-lg p-2.5 shrink-0', item.bg]">
            <component :is="item.icon" :class="['h-5 w-5', item.color]"/>
          </div>
          <div class="min-w-0">
            <p :class="['text-xl font-bold leading-tight', item.color]">{{ item.value }}</p>
            <p class="text-xs text-slate-500">{{ item.label }}</p>
            <p class="truncate text-xs text-slate-400">{{ item.sub }}</p>
          </div>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="门诊统计" name="outpatient"/>
      <el-tab-pane label="住院统计" name="inpatient"/>
      <el-tab-pane label="收入分析" name="revenue"/>
      <el-tab-pane label="药事统计" name="pharmacy"/>
    </el-tabs>

    <!-- ==================== 门诊 ==================== -->
    <template v-if="activeTab === 'outpatient'">
      <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div class="flex items-center justify-between px-6 pb-2 pt-4">
          <h3 class="text-base font-semibold text-slate-800">门诊量日趋势 <span
              class="text-xs font-normal text-slate-400">{{ d.startDate }} ~ {{
              d.endDate
            }}{{ trendSlicedNote(opTrendSliced) }}</span></h3>
          <span class="text-xs text-slate-400"><span class="inline-block h-2 w-2 rounded-sm bg-blue-500"/> 门诊量 <span
              class="ml-2 inline-block h-2 w-2 rounded-sm bg-rose-400"/> 退号</span>
        </div>
        <div class="px-6 pb-6 pt-2">
          <div v-if="opTrendSliced.length" class="flex items-end gap-0.5" style="height: 208px">
            <div v-for="(row, i) in opTrendSliced" :key="i"
                 class="group relative flex flex-1 flex-col items-center justify-end gap-0" style="height: 100%">
              <div
                  class="pointer-events-none absolute -top-1 left-1/2 z-10 hidden -translate-x-1/2 whitespace-nowrap rounded bg-slate-800 px-2 py-1 text-xs text-white group-hover:block">
                {{ row.d }} 门诊 {{ row.n }} · 退号 {{ row.cancels }}
              </div>
              <div class="flex w-full items-end justify-center gap-px" style="height: 180px">
                <div :style="{ height: `${(row.n / trendMax(opTrendSliced, ['n'])) * 100}%` }"
                     class="w-1/2 max-w-[10px] rounded-t-sm bg-blue-500"/>
                <div :style="{ height: `${(row.cancels / trendMax(opTrendSliced, ['n'])) * 100}%` }"
                     class="w-1/2 max-w-[10px] rounded-t-sm bg-rose-400"/>
              </div>
              <span class="mt-1 text-[10px] text-slate-400">{{ opTrendSliced.length > 20 ? '' : row.d }}</span>
            </div>
          </div>
          <el-empty v-else :image-size="60" description="该区间无门诊数据"/>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <h3 class="mb-4 text-base font-semibold text-slate-800">科室门诊量 TOP10</h3>
          <div v-if="(d.opDeptTop || []).length" class="space-y-3">
            <div v-for="(row, i) in d.opDeptTop" :key="row.name" class="flex items-center gap-3">
              <span :class="i < 3 ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-500'"
                    class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs font-bold">{{ i + 1 }}</span>
              <span class="w-28 shrink-0 truncate text-sm text-slate-700">{{ row.name }}</span>
              <div class="relative h-5 flex-1 overflow-hidden rounded bg-slate-100">
                <div :style="{ width: `${(row.cnt / d.opDeptTop[0].cnt) * 100}%`, opacity: 0.7 + (1 - i / d.opDeptTop.length) * 0.3 }"
                     class="h-full rounded bg-blue-500"/>
              </div>
              <span class="w-12 shrink-0 text-right text-sm font-medium text-slate-700">{{ row.cnt }}</span>
            </div>
          </div>
          <el-empty v-else :image-size="60" description="暂无数据"/>
        </div>

        <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <h3 class="mb-4 text-base font-semibold text-slate-800">门诊结构分析</h3>
          <div class="space-y-5">
            <div v-for="block in [
              { title: '挂号类型', rows: (d.opTypeDist || []).map((r: any) => ({ name: REGIST_TYPE[r.code] ?? `类型${r.code}`, cnt: r.cnt })) },
              { title: '挂号来源', rows: (d.opSourceDist || []).map((r: any) => ({ name: REGIST_SOURCE[r.code] ?? `来源${r.code}`, cnt: r.cnt })) },
              { title: '结算方式', rows: (d.opSettleDist || []).map((r: any) => ({ name: SETTLEMENT[r.code] ?? `方式${r.code}`, cnt: r.cnt })) },
              { title: '年龄分布', rows: ageDist },
            ]" :key="block.title">
              <p class="mb-2 text-sm font-medium text-slate-600">{{ block.title }}</p>
              <div v-if="block.rows.length" class="space-y-1.5">
                <div v-for="(row, i) in block.rows" :key="row.name" class="flex items-center gap-2">
                  <span class="w-24 shrink-0 truncate text-xs text-slate-500">{{ row.name }}</span>
                  <div class="relative h-4 flex-1 overflow-hidden rounded bg-slate-100">
                    <div :class="['h-full rounded', colorOf(i)]"
                         :style="{ width: `${(row.cnt / trendMax(block.rows, ['cnt'])) * 100}%`, opacity: 0.8 }"/>
                  </div>
                  <span class="w-10 shrink-0 text-right text-xs text-slate-500">{{ row.cnt }}</span>
                  <span class="w-12 shrink-0 text-right text-xs text-slate-400">{{
                      pctOf(row.cnt, block.rows.reduce((s: number, r: any) => s + r.cnt, 0))
                    }}%</span>
                </div>
              </div>
              <p v-else class="text-xs text-slate-400">暂无数据</p>
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- ==================== 住院 ==================== -->
    <template v-if="activeTab === 'inpatient'">
      <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <h3 class="mb-4 text-base font-semibold text-slate-800">入院日趋势 <span
              class="text-xs font-normal text-slate-400">{{ d.startDate }} ~ {{ d.endDate }}</span></h3>
          <div v-if="(d.ipTrend || []).length" class="flex items-end gap-0.5" style="height: 160px">
            <div v-for="(row, i) in (d.ipTrend || []).slice(-60)" :key="i"
                 class="group relative flex h-full flex-1 flex-col items-center justify-end">
              <div
                  class="pointer-events-none absolute -top-1 left-1/2 z-10 hidden -translate-x-1/2 whitespace-nowrap rounded bg-slate-800 px-2 py-1 text-xs text-white group-hover:block">
                {{ row.d }} 入 {{ row.admits }} · 出 {{ row.discharges }}
              </div>
              <div :style="{ height: `${(row.admits / trendMax(d.ipTrend, ['admits'])) * 100}%` }"
                   class="w-full max-w-[12px] rounded-t-sm bg-purple-500"/>
            </div>
          </div>
          <el-empty v-else :image-size="60" description="该区间无入院数据"/>
          <p class="mt-2 text-xs text-slate-400">悬停柱子可看当日入/出院人次；入院 {{ fmtInt(d.ipAdmitCount) }} · 出院
            {{ fmtInt(d.ipDischargeCount) }} 人次</p>
        </div>

        <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <h3 class="mb-4 text-base font-semibold text-slate-800">床位与住院效率（当前快照）</h3>
          <div class="space-y-4">
            <div>
              <div class="mb-1 flex items-center justify-between text-sm">
                <span class="text-slate-600">床位使用率（占用 {{ fmtInt(d.bedOccupied) }} / 编制 {{
                    fmtInt(d.bedTotal)
                  }}，不含维修床）</span>
                <span
                    :class="['font-medium', (d.bedUseRate ?? 0) > 85 ? 'text-red-600' : (d.bedUseRate ?? 0) > 70 ? 'text-amber-600' : 'text-emerald-600']">{{
                    d.bedUseRate ?? 0
                  }}%</span>
              </div>
              <el-progress :color="(d.bedUseRate ?? 0) > 85 ? '#dc2626' : (d.bedUseRate ?? 0) > 70 ? '#d97706' : '#059669'" :percentage="Math.min(100, Number(d.bedUseRate) || 0)"
                           :show-text="false"/>
            </div>
            <div class="grid grid-cols-3 gap-3 text-center">
              <div class="rounded-lg bg-slate-50 p-3">
                <p class="text-xl font-bold text-purple-600">{{ fmtInt(d.ipInCur) }}</p>
                <p class="text-xs text-slate-500">在院人数（当前）</p>
              </div>
              <div class="rounded-lg bg-slate-50 p-3">
                <p class="text-xl font-bold text-indigo-600">{{ d.ipAvgLosDays ?? 0 }}</p>
                <p class="text-xs text-slate-500">出院平均住院日（天）</p>
              </div>
              <div class="rounded-lg bg-slate-50 p-3">
                <p class="text-xl font-bold text-cyan-600">{{ fmtInt(d.ipBedDays) }}</p>
                <p class="text-xs text-slate-500">出院占用床日</p>
              </div>
            </div>
            <p class="text-xs text-slate-400">床位使用率与在院人数为当前时点快照，不随查询区间回看历史。</p>
          </div>
        </div>
      </div>

      <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div class="px-6 pb-2 pt-4">
          <h3 class="text-base font-semibold text-slate-800">科室出院队列分布 TOP10</h3>
          <p class="text-xs text-slate-400">出院队列 =
            出院时间落在查询区间内的住院记录；「期间入院」为该出院患者中医嘱入院时间也落在区间内的人数</p>
        </div>
        <div class="px-6 pb-6 pt-2">
          <el-table v-if="(d.ipDeptDist || []).length" :data="d.ipDeptDist" border size="small">
            <el-table-column label="#" type="index" width="48"/>
            <el-table-column label="科室" min-width="140" prop="deptName" show-overflow-tooltip/>
            <el-table-column align="right" label="出院人次" prop="discharges" width="100"/>
            <el-table-column align="right" label="同期入院" prop="admits" width="100"/>
            <el-table-column align="right" label="例均住院日(天)" prop="avgLos" width="130"/>
          </el-table>
          <el-empty v-else :image-size="60" description="该区间无出院数据"/>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <h3 class="mb-4 text-base font-semibold text-slate-800">住院结算汇总（按结算日期）</h3>
          <div v-if="d.ipSettle" class="grid grid-cols-2 gap-3">
            <div v-for="cell in [
              { label: '结算单数', value: `${fmtInt(d.ipSettle.settleCount)} 单` },
              { label: '住院总费用', value: `${fmtWan(d.ipSettle.totalAmount)}` },
              { label: '统筹支付', value: `${fmtWan(d.ipSettle.insuranceAmount)}` },
              { label: '患者支付', value: `${fmtWan(d.ipSettle.patientPayAmount)}` },
              { label: '欠费', value: `${fmtWan(d.ipSettle.arrearsAmount)}` },
              { label: '统筹负担率', value: `${pctOf(d.ipSettle.insuranceAmount, d.ipSettle.totalAmount)}%` },
            ]" :key="cell.label" class="rounded-lg bg-slate-50 p-3">
              <p class="text-base font-bold text-slate-800">{{ cell.value }}</p>
              <p class="text-xs text-slate-500">{{ cell.label }}</p>
            </div>
          </div>
          <el-empty v-else :image-size="60" description="暂无结算数据"/>
        </div>

        <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <h3 class="mb-4 text-base font-semibold text-slate-800">出院结算险种构成</h3>
          <div v-if="(d.ipInsuranceDist || []).length" class="space-y-3">
            <div v-for="(row, i) in d.ipInsuranceDist" :key="row.name" class="flex items-center gap-3">
              <span class="w-28 shrink-0 truncate text-sm text-slate-600">{{ row.name }}</span>
              <div class="relative h-6 flex-1 overflow-hidden rounded-md bg-slate-100">
                <div :class="['flex h-full items-center rounded-md pl-2', colorOf(i)]"
                     :style="{ width: `${(row.cnt / trendMax(d.ipInsuranceDist, ['cnt'])) * 100}%`, opacity: 0.85 }">
                  <span class="text-xs font-medium text-white">{{ row.cnt }}</span>
                </div>
              </div>
              <span class="w-12 shrink-0 text-right text-sm font-medium text-slate-700">{{
                  pctOf(row.cnt, d.ipInsuranceDist.reduce((s: number, r: any) => s + r.cnt, 0))
                }}%</span>
            </div>
          </div>
          <el-empty v-else :image-size="60" description="暂无数据"/>
        </div>
      </div>
    </template>

    <!-- ==================== 收入 ==================== -->
    <template v-if="activeTab === 'revenue'">
      <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div class="flex items-center justify-between px-6 pb-2 pt-4">
          <h3 class="text-base font-semibold text-slate-800">收入净额日趋势 <span
              class="text-xs font-normal text-slate-400">{{ d.startDate }} ~ {{
              d.endDate
            }}{{ trendSlicedNote(revTrendSliced) }}</span></h3>
          <span class="text-xs text-slate-400">退费明细按负数抵扣</span>
        </div>
        <div class="px-6 pb-6 pt-2">
          <div v-if="revTrendSliced.length" class="flex items-end gap-0.5" style="height: 180px">
            <div v-for="(row, i) in revTrendSliced" :key="i"
                 class="group relative flex h-full flex-1 flex-col items-center justify-end">
              <div
                  class="pointer-events-none absolute -top-1 left-1/2 z-10 hidden -translate-x-1/2 whitespace-nowrap rounded bg-slate-800 px-2 py-1 text-xs text-white group-hover:block">
                {{ row.d }} {{ fmtWan(row.amt) }}
              </div>
              <div :style="{ height: `${(Math.max(0, row.amt) / trendMax(revTrendSliced, ['amt'])) * 100}%` }"
                   class="w-full max-w-[12px] rounded-t-sm bg-emerald-500"/>
            </div>
          </div>
          <el-empty v-else :image-size="60" description="该区间无收入数据"/>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <h3 class="mb-4 text-base font-semibold text-slate-800">收入类别构成（按收费明细）</h3>
          <div v-if="(d.revTypeDist || []).length" class="space-y-3">
            <div v-for="(row, i) in d.revTypeDist" :key="row.name" class="flex items-center gap-3">
              <span class="w-20 shrink-0 text-sm text-slate-600">{{ row.name }}</span>
              <div class="relative h-7 flex-1 overflow-hidden rounded-md bg-slate-100">
                <div :class="['flex h-full items-center rounded-md pl-2', colorOf(i)]"
                     :style="{ width: `${(Math.abs(row.amt) / trendMax(d.revTypeDist, ['amt'])) * 100}%`, opacity: 0.85 }">
                  <span class="text-xs font-medium text-white">{{ fmtWan(row.amt) }}</span>
                </div>
              </div>
              <span class="w-14 shrink-0 text-right text-sm font-medium text-slate-700">{{
                  pctOf(row.amt, d.revTotal)
                }}%</span>
            </div>
          </div>
          <el-empty v-else :image-size="60" description="暂无数据"/>
          <div class="mt-4 grid grid-cols-3 gap-3 text-center">
            <div class="rounded-lg bg-slate-50 p-3">
              <p class="text-base font-bold text-emerald-600">{{ fmtWan(d.revOutpatient) }}</p>
              <p class="text-xs text-slate-500">门急诊收入</p>
            </div>
            <div class="rounded-lg bg-slate-50 p-3">
              <p class="text-base font-bold text-purple-600">{{ fmtWan(d.revInpatient) }}</p>
              <p class="text-xs text-slate-500">住院收入</p>
            </div>
            <div class="rounded-lg bg-slate-50 p-3">
              <p class="text-base font-bold text-rose-600">{{ fmtWan(d.revRefundAmount) }}</p>
              <p class="text-xs text-slate-500">退费金额</p>
            </div>
          </div>
        </div>

        <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <h3 class="mb-4 text-base font-semibold text-slate-800">科室收入 TOP10</h3>
          <div v-if="(d.revDeptTop || []).length" class="space-y-3">
            <div v-for="(row, i) in d.revDeptTop" :key="row.name" class="flex items-center gap-3">
              <span class="w-28 shrink-0 truncate text-sm text-slate-600">{{ row.name }}</span>
              <div class="relative h-6 flex-1 overflow-hidden rounded bg-slate-100">
                <div :style="{ width: `${(row.amt / d.revDeptTop[0].amt) * 100}%`, opacity: 0.7 + (1 - i / d.revDeptTop.length) * 0.3 }"
                     class="h-full rounded bg-emerald-500"/>
              </div>
              <span class="w-20 shrink-0 text-right text-sm font-medium text-slate-700">{{ fmtWan(row.amt) }}</span>
            </div>
          </div>
          <el-empty v-else :image-size="60" description="暂无数据"/>
        </div>
      </div>

      <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
        <h3 class="mb-4 text-base font-semibold text-slate-800">支付方式构成（按已收费收费单）</h3>
        <div v-if="(d.revPayDist || []).length" class="grid grid-cols-2 gap-3 sm:grid-cols-5">
          <div v-for="row in d.revPayDist" :key="row.code" class="rounded-lg bg-slate-50 p-3 text-center">
            <p class="text-base font-bold text-slate-800">{{ fmtWan(row.amt) }}</p>
            <p class="text-xs text-slate-500">{{ PAY_METHOD[row.code] ?? `方式${row.code}` }} · {{ row.cnt }} 单</p>
            <p class="text-xs text-slate-400">
              {{ pctOf(row.amt, d.revPayDist.reduce((s: number, r: any) => s + Number(r.amt), 0)) }}%</p>
          </div>
        </div>
        <el-empty v-else :image-size="60" description="暂无数据"/>
      </div>
    </template>

    <!-- ==================== 药事 ==================== -->
    <template v-if="activeTab === 'pharmacy'">
      <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div class="rounded-lg border border-slate-200 bg-white p-6 shadow-sm">
          <h3 class="mb-4 text-base font-semibold text-slate-800">处方概况</h3>
          <div class="grid grid-cols-2 gap-3">
            <div v-for="cell in [
              { label: '处方总数', value: `${fmtInt(d.phPrescCount)} 张` },
              { label: '门/急诊/住院', value: `${fmtInt(d.phPrescOutpatient)} / ${fmtInt(d.phPrescEmergency)} / ${fmtInt(d.phPrescInpatient)}` },
              { label: '药师审核退回', value: `${fmtInt(d.phAuditReturnCount)} 次` },
              { label: '退药', value: `${fmtInt(d.phReturnCount)} 条 ${fmtWan(d.phReturnAmount)}` },
            ]" :key="cell.label" class="rounded-lg bg-slate-50 p-3">
              <p class="text-base font-bold text-slate-800">{{ cell.value }}</p>
              <p class="text-xs text-slate-500">{{ cell.label }}</p>
            </div>
          </div>
          <div v-if="(d.phPrescTypeDist || []).length" class="mt-4 space-y-2">
            <p class="text-sm font-medium text-slate-600">处方类型构成</p>
            <div v-for="(row, i) in d.phPrescTypeDist" :key="row.code" class="flex items-center gap-3">
              <span class="w-20 shrink-0 text-sm text-slate-600">{{ PRESC_TYPE[row.code] ?? `类型${row.code}` }}</span>
              <div class="relative h-6 flex-1 overflow-hidden rounded bg-slate-100">
                <div :class="['h-full rounded', colorOf(i)]"
                     :style="{ width: `${(row.amt / trendMax(d.phPrescTypeDist, ['amt'])) * 100}%`, opacity: 0.85 }"/>
              </div>
              <span class="w-12 shrink-0 text-right text-xs text-slate-500">{{ row.cnt }} 张</span>
              <span class="w-20 shrink-0 text-right text-sm font-medium text-slate-700">{{ fmtWan(row.amt) }}</span>
            </div>
          </div>
        </div>

        <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="px-6 pb-2 pt-4">
            <h3 class="text-base font-semibold text-slate-800">发药金额日趋势 <span
                class="text-xs font-normal text-slate-400">{{ d.startDate }} ~ {{
                d.endDate
              }}{{ trendSlicedNote(dispTrendSliced) }}</span></h3>
          </div>
          <div class="px-6 pb-6 pt-2">
            <div v-if="dispTrendSliced.length" class="flex items-end gap-0.5" style="height: 232px">
              <div v-for="(row, i) in dispTrendSliced" :key="i"
                   class="group relative flex h-full flex-1 flex-col items-center justify-end">
                <div
                    class="pointer-events-none absolute -top-1 left-1/2 z-10 hidden -translate-x-1/2 whitespace-nowrap rounded bg-slate-800 px-2 py-1 text-xs text-white group-hover:block">
                  {{ row.d }} {{ fmtWan(row.amt) }}
                </div>
                <div :style="{ height: `${(row.amt / trendMax(dispTrendSliced, ['amt'])) * 100}%` }"
                     class="w-full max-w-[12px] rounded-t-sm bg-teal-500"/>
              </div>
            </div>
            <el-empty v-else :image-size="60" description="该区间无发药记录"/>
          </div>
        </div>
      </div>

      <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
        <div class="px-6 pb-2 pt-4">
          <h3 class="text-base font-semibold text-slate-800">药品用量金额 TOP10（按处方明细，覆盖门诊与住院）</h3>
        </div>
        <div class="px-6 pb-6 pt-2">
          <el-table v-if="(d.phDrugTop || []).length" :data="d.phDrugTop" border size="small">
            <el-table-column label="#" type="index" width="48"/>
            <el-table-column label="药品名称" min-width="160" prop="drugName" show-overflow-tooltip/>
            <el-table-column label="规格" min-width="120" prop="specification" show-overflow-tooltip/>
            <el-table-column align="right" label="数量" width="110">
              <template #default="{ row }">{{ fmtInt(row.qty) }}{{ row.unit ? ' ' + row.unit : '' }}</template>
            </el-table-column>
            <el-table-column align="right" label="金额" width="120">
              <template #default="{ row }">{{ fmtWan(row.amt) }}</template>
            </el-table-column>
          </el-table>
          <el-empty v-else :image-size="60" description="该区间无处方用药数据"/>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {DataLine, FirstAidKit, Money, Suitcase, Tickets, Timer, TrendCharts, User} from '@element-plus/icons-vue';
import {getStatsOverview} from '@/api/report';

const loading = ref(false);
const activeTab = ref('outpatient');
const data = ref(null);
const fmtDay = (d) => {
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
};
const today = new Date();
const range = ref([fmtDay(new Date(today.getTime() - 29 * 86400000)), fmtDay(today)]);
const shortcuts = [
  {text: '近7天', value: () => [new Date(Date.now() - 6 * 86400000), new Date()]},
  {text: '近30天', value: () => [new Date(Date.now() - 29 * 86400000), new Date()]},
  {text: '近90天', value: () => [new Date(Date.now() - 89 * 86400000), new Date()]},
];
// ==== 字典口径（与 sys_menu 铺底注释/AppointStatusEnum/字典 his_pay_method 一致） ====
const REGIST_TYPE = {1: '普通号', 2: '专家号', 3: '急诊号', 4: '免费号'};
const REGIST_SOURCE = {1: '窗口', 2: '自助机', 3: '网上', 4: '预约挂号'};
const SETTLEMENT = {1: '自费', 2: '城镇职工医保', 3: '城乡居民医保', 4: '公费', 5: '商业保险'};
const PRESC_TYPE = {1: '西药', 2: '中成药', 3: '中药饮片'};
const PAY_METHOD = {1: '现金', 2: '微信', 3: '支付宝', 4: '医保(卡)', 5: '余额'};
const AGE_ORDER = ['婴儿(<1)', '儿童(1-14)', '青年(15-40)', '中年(41-65)', '老年(65+)', '未填'];
const BAR_COLORS = ['bg-blue-500', 'bg-emerald-500', 'bg-amber-500', 'bg-purple-500', 'bg-rose-500', 'bg-cyan-500'];
const colorOf = (i) => BAR_COLORS[i % BAR_COLORS.length];
const d = computed(() => data.value || {});
const trendMax = (rows, keys) => Math.max(1, ...rows.flatMap(r => keys.map(k => Number(r[k]) || 0)));
const fmtInt = (v) => (Number(v) || 0).toLocaleString();
const fmtAmt = (n) => (Math.abs(n) >= 10000 ? `${(n / 10000).toFixed(1)}万` : n.toLocaleString('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
}));
const fmtWan = (v) => `¥${fmtAmt(Number(v) || 0)}`;
const pctOf = (part, total) => {
  const t = Number(total) || 0;
  return t > 0 ? ((Number(part) || 0) / t * 100).toFixed(1) : '0.0';
};
const insShare = computed(() => {
  const list = d.value.opSettleDist || [];
  const total = list.reduce((s, r) => s + (Number(r.cnt) || 0), 0);
  const ins = list.filter((r) => r.code !== 1).reduce((s, r) => s + (Number(r.cnt) || 0), 0);
  return pctOf(ins, total);
});
const ageDist = computed(() => {
  const list = d.value.opAgeDist || [];
  return [...list].sort((a, b) => AGE_ORDER.indexOf(a.name) - AGE_ORDER.indexOf(b.name));
});
const opTrendSliced = computed(() => (d.value.opTrend || []).slice(-60));
const revTrendSliced = computed(() => (d.value.revTrend || []).slice(-60));
const dispTrendSliced = computed(() => (d.value.phDispTrend || []).slice(-60));
const trendSlicedNote = (rows) => ((d.value.opTrend || []).length > 60 && rows.length <= 60 ? `（仅显示最近 ${rows.length} 天）` : '');
const loadData = async () => {
  if (!range.value)
    return;
  loading.value = true;
  try {
    const res = await getStatsOverview({startDate: range.value[0], endDate: range.value[1]});
    data.value = res.data || {};
  } catch (e) {
    ElMessage.error(e?.message || '加载报表数据失败');
  } finally {
    loading.value = false;
  }
};
onMounted(loadData);
</script>
