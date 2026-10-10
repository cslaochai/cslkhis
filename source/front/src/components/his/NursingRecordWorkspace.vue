<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div>
        <h1 class="text-xl font-semibold text-slate-900">护理文书（三测单 / 护理记录）</h1>
        <p class="mt-1 text-sm text-slate-500">
          三测单同一时点只允许一条；体温曲线按测量时间自动绘制，纵轴固定 35~41℃。
        </p>
      </div>
      <div class="flex items-center gap-3">
        <el-select
            v-model="admissionId"
            class="!w-80"
            data-testid="p2-nursing-admission-select"
            filterable
            placeholder="选择在院患者"
            @change="handleAdmissionChange"
        >
          <el-option v-for="a in admissions" :key="a.admissionId" :label="patientLabel(a)"
                     :value="String(a.admissionId)"/>
        </el-select>
        <el-button :icon="Refresh" @click="reloadAll">刷新</el-button>
      </div>
    </div>

    <div class="grid grid-cols-2 gap-4">
      <div
          v-for="s in statCards"
          :key="s.label"
          class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div :class="s.bg" class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg">
          <el-icon :class="s.color" class="h-5 w-5">
            <component :is="s.icon"/>
          </el-icon>
        </div>
        <div class="min-w-0">
          <p class="truncate text-xs text-slate-500">{{ s.label }}</p>
          <p class="text-lg font-bold text-slate-900">{{ s.value }}</p>
          <p class="truncate text-[11px] text-slate-400">{{ s.hint }}</p>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
             @tab-change="handleAssessTabEnter">
      <!-- ============== 三测单 ============== -->
      <el-tab-pane label="三测单" name="sheet">
        <div class="mb-3 flex items-center gap-3">
          <span class="text-sm text-slate-600">
            {{ sheet.patientName || '—' }} · 床 {{ sheet.bedNo || '—' }} · 数据点 {{ sheet.pointCount ?? 0 }}
          </span>
          <el-button v-perm="'ipd:nurse:add'" :icon="DocumentChecked" class="!ml-auto" data-testid="g14-open-batch"
                     @click="openBatch">
            批量录入体温单
          </el-button>
          <el-button v-perm="'ipd:nurse:add'" :icon="Plus" data-testid="p2-open-vital" type="primary"
                     @click="openCreate">
            录入护理记录
          </el-button>
        </div>

        <!-- 体温曲线：固定纵轴 35~41℃ -->
        <div v-loading="sheetLoading" class="rounded border border-slate-200 bg-white p-3">
          <div class="mb-2 text-sm font-medium text-slate-700">体温曲线（℃）</div>
          <svg
              v-if="chartPoints.length"
              :viewBox="`0 0 ${CHART_W} ${CHART_H}`"
              class="w-full"
              data-testid="p2-temp-chart"
              style="height: 240px"
          >
            <line v-for="g in gridLines" :key="g.label"
                  :x1="PAD_L" :x2="CHART_W - PAD_R" :y1="g.y" :y2="g.y"
                  stroke="#e2e8f0" stroke-width="1"/>
            <text v-for="g in gridLines" :key="`t-${g.label}`"
                  :x="PAD_L - 8" :y="g.y + 4" fill="#94a3b8" font-size="11" text-anchor="end">{{ g.label }}
            </text>
            <polyline :points="tempPolyline" fill="none" stroke="#ef4444" stroke-width="2"/>
            <g v-for="(p, i) in chartPoints" :key="p.recordId">
              <circle
                  :cx="xOf(i)"
                  :cy="yOf(Number(p.temperature))" data-testid="p2-temp-point" fill="#ef4444" r="4"
              />
              <text :x="xOf(i)" :y="CHART_H - 8" fill="#94a3b8" font-size="10" text-anchor="middle">
                {{ p.measureClock }}
              </text>
            </g>
          </svg>
          <div v-else class="py-8 text-center text-sm text-slate-400">
            还没有三测单数据，点右上「录入护理记录」录入体温
          </div>
        </div>

        <el-table :data="sheet.points" border class="mt-3" data-testid="p2-sheet-table" style="width: 100%">
          <el-table-column label="日期" width="120">
            <template #default="{ row }">{{ row.measureDate || '—' }}</template>
          </el-table-column>
          <el-table-column label="时点" width="90">
            <template #default="{ row }">{{ row.measureClock || '—' }}</template>
          </el-table-column>
          <el-table-column label="班次" width="90">
            <template #default="{ row }">{{ row.shiftText || '—' }}</template>
          </el-table-column>
          <el-table-column label="体温℃" width="90">
            <template #default="{ row }">
              <span class="font-medium text-rose-600">{{ num(row.temperature) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="脉搏" width="80">
            <template #default="{ row }">{{ num(row.pulse) }}</template>
          </el-table-column>
          <el-table-column label="呼吸" width="80">
            <template #default="{ row }">{{ num(row.respiration) }}</template>
          </el-table-column>
          <el-table-column label="血压" width="110">
            <template #default="{ row }">{{ row.bloodPressureText || '—' }}</template>
          </el-table-column>
          <el-table-column label="大便" width="80">
            <template #default="{ row }">{{ num(row.stoolCount) }}</template>
          </el-table-column>
          <el-table-column label="尿量ml" width="90">
            <template #default="{ row }">{{ num(row.urineVolume) }}</template>
          </el-table-column>
          <el-table-column label="记录护士" min-width="110">
            <template #default="{ row }">{{ row.nurseName || '—' }}</template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">暂无三测单数据</div>
          </template>
        </el-table>
      </el-tab-pane>

      <!-- ============== 专项评估（sql/159：5 类透视卡 + 流水） ============== -->
      <el-tab-pane label="专项评估" name="assess">
        <!-- 透视卡：每类量表最新一次（后端 latestByType），点「去评估」进对应专项表单 -->
        <div v-loading="latestLoading" class="mb-4 grid grid-cols-5 gap-3">
          <div
              v-for="t in SPECIAL_TYPES"
              :key="t.type"
              :class="latestByType[t.type]
              ? (latestByType[t.type].riskLevel >= 3 ? 'border-rose-200 bg-rose-50' : 'border-slate-200 bg-white')
              : 'border-dashed border-slate-300 bg-slate-50'"
              :data-testid="`n-card-${t.type}`"
              class="rounded-lg border p-3 shadow-sm"
          >
            <div class="mb-1 flex items-center justify-between">
              <span class="text-sm font-medium text-slate-700">{{ t.short }}</span>
              <el-button
                  v-perm="'ipd:nurse:add'"
                  :data-testid="`n-assess-go-${t.type}`"
                  link
                  size="small"
                  type="primary"
                  @click="openSpecial(t.type)"
              >去评估
              </el-button>
            </div>
            <template v-if="latestByType[t.type]">
              <div class="flex items-baseline gap-2">
                <span :data-testid="`n-card-score-${t.type}`"
                      class="text-2xl font-bold text-slate-900">{{ latestByType[t.type].totalScore }}</span>
                <el-tag :data-testid="`n-card-risk-${t.type}`" :type="(RISK_LEVEL_TAG[latestByType[t.type].riskLevel] || 'info') as any"
                        size="small">
                  {{ RISK_LEVEL_TEXT[latestByType[t.type].riskLevel] || '未知(0)' }}
                </el-tag>
              </div>
              <p class="mt-1 truncate text-[11px] text-slate-400">
                {{
                  latestByType[t.type].assessTime ? String(latestByType[t.type].assessTime).replace('T', ' ').slice(5, 16) : '—'
                }}
                · {{ latestByType[t.type].assessNurseName || '—' }}
              </p>
            </template>
            <template v-else>
              <p class="py-2 text-sm text-slate-400">未评估</p>
              <p class="text-[11px] text-slate-400">{{ t.desc }}</p>
            </template>
          </div>
        </div>

        <div class="mb-3 flex items-center gap-3">
          <span class="text-sm text-slate-600">
            {{ sheet.patientName || '—' }} 的评估流水 · 风险等级由后端按分数段判定（NRS 3 / VTE Caprini 4 / 管路滑脱 5）
          </span>
        </div>
        <el-table v-loading="assessLoading" :data="assessRows" border data-testid="g14-assess-table"
                  style="width: 100%">
          <el-table-column label="评估单号" prop="assessNo" width="150"/>
          <el-table-column label="类型" width="160">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{
                  row.assessTypeText || ASSESS_TYPE_TEXT[row.assessType] || '未知(0)'
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="总分" width="90">
            <template #default="{ row }"><span class="font-medium">{{ row.totalScore }}</span></template>
          </el-table-column>
          <el-table-column label="风险等级" width="110">
            <template #default="{ row }">
              <el-tag :type="(RISK_LEVEL_TAG[row.riskLevel] || 'info') as any" data-testid="g14-risk-tag" size="small">
                {{ row.riskLevelText || RISK_LEVEL_TEXT[row.riskLevel] || '未知(0)' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="评估时间" width="160">
            <template #default="{ row }">{{
                row.assessTime ? String(row.assessTime).replace('T', ' ').slice(0, 16) : '—'
              }}
            </template>
          </el-table-column>
          <el-table-column label="评估护士" prop="assessNurseName" width="110"/>
          <el-table-column label="备注" min-width="160">
            <template #default="{ row }">{{ row.remark || '—' }}</template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">该患者暂无评估单，点上方卡片「去评估」</div>
          </template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="assessPagination.pageNum"
              v-model:page-size="assessPagination.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="assessTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="loadAssessments"
              @current-change="loadAssessments"
          />
        </div>
      </el-tab-pane>

      <!-- ============== 出入量小结（G14） ============== -->
      <el-tab-pane label="出入量小结" name="io">
        <div class="mb-3 flex items-center gap-3">
          <el-date-picker
              v-model="ioRange"
              class="!w-72"
              data-testid="g14-io-range"
              end-placeholder="结束日期"
              start-placeholder="开始日期"
              type="daterange"
              value-format="YYYY-MM-DD"
          />
          <el-button :icon="PieChart" type="primary" @click="loadIoSummary">复算</el-button>
        </div>
        <div v-loading="ioLoading">
          <template v-if="ioSummary">
            <div class="mb-3 grid grid-cols-4 gap-3">
              <div class="rounded border border-blue-200 bg-blue-50 p-3 text-center">
                <p class="text-xs text-slate-500">总入量 ml</p>
                <p class="text-lg font-bold text-blue-700" data-testid="g14-io-intake">{{
                    ioSummary.totals?.intake ?? 0
                  }}</p>
              </div>
              <div class="rounded border border-rose-200 bg-rose-50 p-3 text-center">
                <p class="text-xs text-slate-500">总出量 ml</p>
                <p class="text-lg font-bold text-rose-700" data-testid="g14-io-output">{{
                    ioSummary.totals?.output ?? 0
                  }}</p>
              </div>
              <div class="rounded border border-teal-200 bg-teal-50 p-3 text-center">
                <p class="text-xs text-slate-500">总尿量 ml</p>
                <p class="text-lg font-bold text-teal-700">{{ ioSummary.totals?.urine ?? 0 }}</p>
              </div>
              <div :class="(ioSummary.totals?.netBalance ?? 0) >= 0 ? 'border-amber-200 bg-amber-50' : 'border-slate-200 bg-slate-50'"
                   class="rounded border p-3 text-center">
                <p class="text-xs text-slate-500">净平衡（入-出）</p>
                <p class="text-lg font-bold text-slate-800" data-testid="g14-io-net">
                  {{ ioSummary.totals?.netBalance ?? 0 }}</p>
              </div>
            </div>
            <el-table :data="ioSummary.days || []" border style="width: 100%">
              <el-table-column label="日期" prop="date" width="130"/>
              <el-table-column label="入量 ml" width="110">
                <template #default="{ row }">{{ row.intake }}</template>
              </el-table-column>
              <el-table-column label="出量 ml" width="110">
                <template #default="{ row }">{{ row.output }}</template>
              </el-table-column>
              <el-table-column label="尿量 ml" width="110">
                <template #default="{ row }">{{ row.urine }}</template>
              </el-table-column>
              <el-table-column label="大便 次" width="100">
                <template #default="{ row }">{{ row.stool }}</template>
              </el-table-column>
              <el-table-column label="当日净平衡" width="120">
                <template #default="{ row }">
                  <span :class="row.netBalance >= 0 ? 'text-amber-600' : 'text-emerald-600'">{{ row.netBalance }}</span>
                </template>
              </el-table-column>
              <el-table-column label="测量点数" min-width="100">
                <template #default="{ row }">{{ row.pointCount }}</template>
              </el-table-column>
              <template #empty>
                <div class="py-6 text-sm text-slate-400">区间内没有带出入量的测量点（出入量在体温单/生命体征行内录入）
                </div>
              </template>
            </el-table>
          </template>
          <div v-else class="py-6 text-center text-sm text-slate-400">选择患者后自动复算（从护理文书原始测量行按日汇总）
          </div>
        </div>
      </el-tab-pane>

      <!-- ============== 护理文书列表 ============== -->
      <el-tab-pane label="护理文书列表" name="list">
        <div class="mb-3 flex items-center gap-3">
          <el-select v-model="query.nursingType" class="!w-40" clearable placeholder="文书类型">
            <el-option v-for="t in typeOptions" :key="t.code" :label="t.label" :value="t.code"/>
          </el-select>
          <el-button type="primary" @click="loadRows">查询</el-button>
        </div>
        <el-table v-loading="loading" :data="rows" border data-testid="p2-nursing-table" style="width: 100%">
          <el-table-column label="文书号" prop="recordNo" width="150"/>
          <el-table-column label="类型" width="120">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ row.nursingTypeText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="测量时间" width="160">
            <template #default="{ row }">
              {{ row.measureTime ? String(row.measureTime).replace('T', ' ').slice(0, 16) : '—' }}
            </template>
          </el-table-column>
          <el-table-column label="体温℃" width="80">
            <template #default="{ row }">{{ num(row.temperature) }}</template>
          </el-table-column>
          <el-table-column label="脉搏/呼吸" width="110">
            <template #default="{ row }">{{ num(row.pulse) }} / {{ num(row.respiration) }}</template>
          </el-table-column>
          <el-table-column label="血压" width="100">
            <template #default="{ row }">{{ row.bloodPressureText || '—' }}</template>
          </el-table-column>
          <el-table-column label="护理级别" width="110">
            <template #default="{ row }">{{ row.nursingLevelText || '—' }}</template>
          </el-table-column>
          <el-table-column label="护理正文" min-width="200">
            <template #default="{ row }">
              <span class="text-xs text-slate-600">{{ row.nursingContent || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="记录护士" prop="nurseName" width="110"/>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ row.recordStatusText }}</el-tag>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">暂无护理文书</div>
          </template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @size-change="loadRows"
              @current-change="loadRows"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ============== 录入 ============== -->
    <el-dialog v-model="dialog" title="录入护理文书" width="60%">
      <el-form label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="文书类型" required>
              <el-select v-model="form.nursingType" class="!w-full" data-testid="p2-nursing-type">
                <el-option v-for="t in typeOptions" :key="t.code" :label="t.label" :value="t.code"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <!-- data-testid 挂 el-form-item：el-date-picker 内部 inheritAttrs=false，挂在组件上不会落到 DOM -->
            <el-form-item data-testid="p2-measure-time" label="测量时间" required>
              <el-date-picker
                  v-model="form.measureTime"
                  class="!w-full"
                  placeholder="三测单按时点唯一"
                  type="datetime"
                  value-format="YYYY-MM-DDTHH:mm:ss"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="体温℃">
              <el-input-number v-model="form.temperature" :max="43" :min="34" :precision="1"
                               :step="0.1" class="!w-full" controls-position="right" data-testid="p2-temperature"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="脉搏">
              <el-input-number v-model="form.pulse" :max="250" :min="20" class="!w-full" controls-position="right"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="呼吸">
              <el-input-number v-model="form.respiration" :max="80" :min="5" class="!w-full" controls-position="right"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="血氧%">
              <el-input-number v-model="form.spo2" :max="100" :min="50" class="!w-full" controls-position="right"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="6">
            <el-form-item label="收缩压">
              <el-input-number v-model="form.systolicPressure" :max="300" :min="40" class="!w-full"
                               controls-position="right"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="舒张压">
              <el-input-number v-model="form.diastolicPressure" :max="200" :min="20" class="!w-full"
                               controls-position="right"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="大便次数">
              <el-input-number v-model="form.stoolCount" :max="30" :min="0" class="!w-full" controls-position="right"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="尿量ml">
              <el-input-number v-model="form.urineVolume" :max="10000" :min="0" class="!w-full"
                               controls-position="right"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="班次">
              <el-select v-model="form.shift" class="!w-full" clearable>
                <el-option :value="1" label="白班"/>
                <el-option :value="2" label="小夜班"/>
                <el-option :value="3" label="大夜班"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="护理级别">
              <el-select v-model="form.nursingLevel" class="!w-full" clearable>
                <el-option :value="1" label="特级护理"/>
                <el-option :value="2" label="一级护理"/>
                <el-option :value="3" label="二级护理"/>
                <el-option :value="4" label="三级护理"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="护理正文">
          <el-input v-model="form.nursingContent" :rows="3" data-testid="p2-nursing-content" placeholder="护理记录单必填；三测单可留空"
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button v-perm="'ipd:nurse:add'" :loading="saving" data-testid="p2-submit-nursing" type="primary"
                   @click="submitNursing">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- ============== 批量录入（G14） ============== -->
    <el-dialog v-model="batchDialog" title="体温单批量录入（一次测量 × 多位在院患者）" top="5vh" width="85%">
      <el-alert :closable="false" class="mb-3" title="后端整体事务：任何一行缺体征值 / 患者已出院 / 时点冲突，整批拒绝（批量要么完整要么不落库）"
                type="info"/>
      <el-form label-width="90px">
        <el-row :gutter="16">
          <el-col :span="10">
            <el-form-item data-testid="g14-batch-time" label="测量时点" required>
              <el-date-picker v-model="batchMeasureTime" class="!w-full" placeholder="本批统一时点（三测单按时点唯一）"
                              type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="班次">
              <el-select v-model="batchShift" class="!w-full" clearable>
                <el-option :value="1" label="白班"/>
                <el-option :value="2" label="小夜班"/>
                <el-option :value="3" label="大夜班"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="勾选患者" required>
              <el-select v-model="batchPicked" :fit-input-width="false" class="!w-full" filterable
                         multiple placeholder="选本次测量到的患者" @change="onBatchPickedChange">
                <el-option v-for="a in admissions" :key="a.admissionId" :label="patientLabel(a)"
                           :value="String(a.admissionId)"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <el-table v-if="batchRows.length" :data="batchRows" border data-testid="g14-batch-table" max-height="380"
                style="width: 100%">
        <el-table-column label="患者" min-width="180" prop="patientLabel"/>
        <el-table-column label="体温℃" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.temperature" :max="43" :min="34" :precision="1" :step="0.1"
                             controls-position="right" style="width: 100%"/>
          </template>
        </el-table-column>
        <el-table-column label="脉搏" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.pulse" :max="250" :min="20" controls-position="right" style="width: 100%"/>
          </template>
        </el-table-column>
        <el-table-column label="呼吸" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.respiration" :max="80" :min="5" controls-position="right"
                             style="width: 100%"/>
          </template>
        </el-table-column>
        <el-table-column label="收缩压" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.systolicPressure" :max="300" :min="40" controls-position="right"
                             style="width: 100%"/>
          </template>
        </el-table-column>
        <el-table-column label="舒张压" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.diastolicPressure" :max="200" :min="20" controls-position="right"
                             style="width: 100%"/>
          </template>
        </el-table-column>
        <el-table-column label="血氧%" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.spo2" :max="100" :min="50" controls-position="right" style="width: 100%"/>
          </template>
        </el-table-column>
        <el-table-column label="大便" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.stoolCount" :max="30" :min="0" controls-position="right" style="width: 100%"/>
          </template>
        </el-table-column>
        <el-table-column label="尿量ml" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.urineVolume" :max="10000" :min="0" controls-position="right"
                             style="width: 100%"/>
          </template>
        </el-table-column>
        <el-table-column label="入量ml" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.intakeVolume" :max="10000" :min="0" controls-position="right"
                             style="width: 100%"/>
          </template>
        </el-table-column>
        <el-table-column label="出量ml" width="110">
          <template #default="{ row }">
            <el-input-number v-model="row.outputVolume" :max="10000" :min="0" controls-position="right"
                             style="width: 100%"/>
          </template>
        </el-table-column>
      </el-table>
      <div v-else class="py-6 text-center text-sm text-slate-400">先在上面的下拉里勾选本次测量到的患者</div>
      <template #footer>
        <el-button @click="batchDialog = false">取消</el-button>
        <el-button v-perm="'ipd:nurse:add'" :loading="batchSaving" data-testid="g14-submit-batch" type="primary"
                   @click="submitBatch">
          提交本批（{{ batchRows.length }} 行）
        </el-button>
      </template>
    </el-dialog>

    <!-- ============== 专项评估弹框（sql/159：按类型分支渲染，一个入口避免五套模板） ============== -->
    <el-dialog v-model="specialDialog" :title="specialScale.name" top="6vh" width="62%">
      <el-form label-width="90px">
        <el-row :gutter="16">
          <el-col :span="10">
            <el-form-item data-testid="n-assess-time" label="评估时间" required>
              <el-date-picker v-model="specialTime" class="!w-full" placeholder="评估时间"
                              type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
            </el-form-item>
          </el-col>
          <el-col :span="14">
            <el-form-item label="合计/风险">
              <span class="text-lg font-bold text-slate-800" data-testid="n-assess-total">{{
                  specialTotal ?? '—'
                }}</span>
              <el-tag v-if="specialRiskPreview" :type="(RISK_LEVEL_TAG[specialRiskPreview] || 'info') as any"
                      class="ml-2" data-testid="n-assess-risk">
                {{ RISK_LEVEL_TEXT[specialRiskPreview] }}
              </el-tag>
            </el-form-item>
          </el-col>
        </el-row>
        <el-alert :closable="false" :title="specialScale.hint" class="mb-3" type="info"/>

        <!-- NRS：0~10 大按钮 + 部位/性质/措施 -->
        <template v-if="specialType === 3">
          <div class="mb-4 rounded border border-slate-200 p-3">
            <p class="mb-2 text-sm font-medium text-slate-700">疼痛强度（请患者自评）</p>
            <div class="flex flex-wrap gap-2" data-testid="n-nrs-score">
              <el-button
                  v-for="n in 11" :key="n - 1"
                  :data-testid="`n-nrs-btn-${n - 1}`"
                  :plain="nrsScore !== n - 1"
                  :type="nrsScore === n - 1 ? 'primary' : 'default'"
                  class="!w-12"
                  @click="nrsScore = n - 1"
              >{{ n - 1 }}
              </el-button>
            </div>
            <p class="mt-2 text-xs text-slate-400">0 无疼痛 · 1~3 轻度 · 4~6 中度 · 7~10 重度</p>
          </div>
          <el-row :gutter="16">
            <!-- el-input 不透传 data-testid，testid 挂 el-form-item（同 p2-measure-time 模式） -->
            <el-col :span="8">
              <el-form-item data-testid="n-nrs-site" label="疼痛部位">
                <el-input v-model="nrsSite" placeholder="如：腹部、右下腹"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="疼痛性质">
                <el-select v-model="nrsNature" class="!w-full" clearable data-testid="n-nrs-nature" placeholder="选填">
                  <el-option v-for="n in (specialScale as any).natures" :key="n" :label="n" :value="n"/>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item data-testid="n-nrs-measure" label="处理措施">
                <el-input v-model="nrsMeasure" placeholder="如：药物镇痛、心理疏导"/>
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- Caprini：危险因素分组勾选累加 -->
        <template v-else-if="specialType === 4">
          <div v-for="g in (specialScale as any).groups" :key="g.label"
               class="mb-3 rounded border border-slate-200 p-3">
            <p class="mb-2 text-sm font-medium text-slate-700">{{ g.label }}</p>
            <el-checkbox-group v-model="checkedFactors">
              <el-checkbox
                  v-for="it in g.items" :key="it.key"
                  :data-testid="`n-caprini-${it.key}`"
                  :value="it.key"
              >{{ it.label }}（+{{ it.score }}）
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </template>

        <!-- 管路滑脱：留置清单勾选（不计分）+ 风险项单选 -->
        <template v-else-if="specialType === 5">
          <div class="mb-3 rounded border border-blue-200 bg-blue-50 p-3">
            <p class="mb-2 text-sm font-medium text-slate-700">当前留置管路（勾选做管路透视，不计分）</p>
            <el-checkbox-group v-model="checkedTubes">
              <el-checkbox
                  v-for="t in (specialScale as any).tubes" :key="t.key"
                  :data-testid="`n-tube-${t.key}`"
                  :value="t.key"
              >{{ t.label }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
          <div v-for="it in (specialScale as any).items" :key="it.key" class="mb-3 rounded border border-slate-200 p-3">
            <p class="mb-2 text-sm font-medium text-slate-700">{{ it.label }}</p>
            <el-radio-group v-model="radioPicks[it.key]">
              <el-radio v-for="opt in it.options" :key="opt.score" :value="opt.score">{{ opt.label }}（{{ opt.score }}
                分）
              </el-radio>
            </el-radio-group>
          </div>
        </template>

        <!-- 压疮 Braden / 跌倒 Morse：通用 radio 量表 -->
        <template v-else>
          <div v-for="it in (specialScale as any).items" :key="it.key" class="mb-3 rounded border border-slate-200 p-3">
            <p class="mb-2 text-sm font-medium text-slate-700">{{ it.label }}</p>
            <el-radio-group v-model="radioPicks[it.key]">
              <el-radio v-for="opt in it.options" :key="opt.score" :value="opt.score">{{ opt.label }}（{{ opt.score }}
                分）
              </el-radio>
            </el-radio-group>
          </div>
        </template>

        <el-form-item label="备注">
          <el-input v-model="specialRemark" placeholder="护理措施等"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="specialDialog = false">取消</el-button>
        <el-button v-perm="'ipd:nurse:add'" :loading="specialSaving" data-testid="n-submit-assess" type="primary"
                   @click="submitSpecial">保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Clock, DataLine, DocumentChecked, PieChart, Plus, Refresh} from '@element-plus/icons-vue';
import {getInpatientListPage} from '@/api/inpatient';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {
  getAssessmentLatestByType,
  getIntakeOutputSummary,
  getNursingAssessmentListPage,
  getNursingRecordListPage,
  getNursingTypeOptions,
  getTempSheet,
  saveNursingAssessment,
  saveNursingRecord,
  saveNursingRecordsBatch,
} from '@/api/inpatientNursing';
import {
  ASSESS_TYPE_TEXT,
  RISK_LEVEL_TAG,
  RISK_LEVEL_TEXT,
  riskLevelOf,
  SCALES,
  SPECIAL_TYPES,
} from '@/lib/nursingAssessment';
// ---------------- 基础数据 ----------------
const admissions = ref([]);
const admissionId = ref('');
const typeOptions = ref([]);
const num = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));
const patientLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`;
const loadAdmissions = async () => {
  try {
    const res = await getInpatientListPage({admitStatus: 1, pageNum: 1, pageSize: 200});
    admissions.value = (res.data?.records || []);
    if (!admissionId.value && admissions.value.length > 0) {
      admissionId.value = String(admissions.value[0].admissionId);
    }
  } catch (error) {
    console.error('加载在院患者失败:', error);
  }
};
// ---------------- 三测单 ----------------
const sheet = ref({points: []});
const sheetLoading = ref(false);
const loadSheet = async () => {
  sheetLoading.value = true;
  try {
    const res = await getTempSheet({admissionId: admissionId.value || undefined});
    sheet.value = (res.data || {points: []});
  } catch (error) {
    ElMessage.error(error.message || '加载三测单失败');
  } finally {
    sheetLoading.value = false;
  }
};
/** 纵轴固定 35~41℃：三测单的刻度是标准的，随数据浮动会让"高烧"看上去跟正常一样陡 */
const AXIS_MIN = 35;
const AXIS_MAX = 41;
const CHART_W = 880;
const CHART_H = 220;
const PAD_L = 46;
const PAD_R = 20;
const PAD_T = 16;
const PAD_B = 28;
const chartPoints = computed(() => (sheet.value.points || []).filter(p => p.temperature !== null && p.temperature !== undefined));
const xOf = (i) => {
  const n = Math.max(chartPoints.value.length, 1);
  return PAD_L + (n === 1 ? (CHART_W - PAD_L - PAD_R) / 2 : ((CHART_W - PAD_L - PAD_R) * i) / (n - 1));
};
const yOf = (t) => {
  const clamped = Math.min(Math.max(t, AXIS_MIN), AXIS_MAX);
  return PAD_T + ((AXIS_MAX - clamped) / (AXIS_MAX - AXIS_MIN)) * (CHART_H - PAD_T - PAD_B);
};
const tempPolyline = computed(() => chartPoints.value.map((p, i) => `${xOf(i)},${yOf(Number(p.temperature))}`).join(' '));
const gridLines = computed(() => {
  const lines = [];
  for (let t = 35; t <= 41; t++) {
    lines.push({y: yOf(t), label: String(t)});
  }
  return lines;
});
// ---------------- 列表 ----------------
const activeTab = ref('sheet');
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const query = ref({nursingType: null});
const loadRows = async () => {
  loading.value = true;
  try {
    const res = await getNursingRecordListPage({
      admissionId: admissionId.value || undefined,
      nursingType: query.value.nursingType ?? undefined,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    });
    rows.value = (res.data?.records || []);
    total.value = Number(res.data?.total ?? 0);
  } catch (error) {
    ElMessage.error(error.message || '加载护理文书失败');
  } finally {
    loading.value = false;
  }
};
const statCards = computed(() => [
  {
    label: '三测单点数',
    value: sheet.value.pointCount ?? 0,
    hint: '按时点唯一，同点不可重复',
    color: 'text-rose-600',
    bg: 'bg-rose-50',
    icon: DataLine
  },
  {
    label: '护理文书',
    value: total.value,
    hint: '含三测单 / 护理记录 / 生命体征',
    color: 'text-blue-600',
    bg: 'bg-blue-50',
    icon: Clock
  },
]);
// ---------------- 录入 ----------------
const dialog = ref(false);
const saving = ref(false);
const emptyForm = () => ({
  nursingType: 1,
  measureTime: '',
  shift: undefined,
  temperature: undefined,
  pulse: undefined,
  respiration: undefined,
  systolicPressure: undefined,
  diastolicPressure: undefined,
  spo2: undefined,
  stoolCount: undefined,
  urineVolume: undefined,
  nursingLevel: undefined,
  nursingContent: '',
  remark: '',
});
const form = ref(emptyForm());
const openCreate = () => {
  if (!admissionId.value) {
    ElMessage.warning('请先选择在院患者');
    return;
  }
  form.value = emptyForm();
  dialog.value = true;
};
const submitNursing = async () => {
  if (!admissionId.value) {
    ElMessage.warning('请先选择在院患者');
    return;
  }
  saving.value = true;
  try {
    await saveNursingRecord({
      admissionId: admissionId.value,
      nursingType: form.value.nursingType,
      measureTime: form.value.measureTime || undefined,
      shift: form.value.shift ?? undefined,
      temperature: form.value.temperature ?? undefined,
      pulse: form.value.pulse ?? undefined,
      respiration: form.value.respiration ?? undefined,
      systolicPressure: form.value.systolicPressure ?? undefined,
      diastolicPressure: form.value.diastolicPressure ?? undefined,
      spo2: form.value.spo2 ?? undefined,
      stoolCount: form.value.stoolCount ?? undefined,
      urineVolume: form.value.urineVolume ?? undefined,
      nursingLevel: form.value.nursingLevel ?? undefined,
      nursingContent: form.value.nursingContent || undefined,
      remark: form.value.remark || undefined,
    });
    ElMessage.success('护理文书已保存');
    dialog.value = false;
    await reloadAll();
  } catch (error) {
    ElMessage.error(error.message || '保存护理文书失败');
  } finally {
    saving.value = false;
  }
};
const reloadAll = async () => {
  await Promise.all([loadSheet(), loadRows()]);
};
const handleAdmissionChange = async () => {
  pagination.value.pageNum = 1;
  await reloadAll();
  await Promise.all([loadAssessments(), loadLatest(), loadIoSummary()]);
};
// ---------------- G14-1 体温单批量录入 ----------------
// 一次测量动作 × 多个在院患者（护士拿体温计挨床测的真实场景）；后端整体事务，
// 任何一行不合法（缺体征值 / 不在院 / 时点冲突）整批拒绝 —— 批量要么完整要么不落。
const batchDialog = ref(false);
const batchSaving = ref(false);
const batchMeasureTime = ref('');
const batchShift = ref(undefined);
const batchPicked = ref([]);
const batchRows = ref([]);
const openBatch = () => {
  batchMeasureTime.value = '';
  batchShift.value = undefined;
  batchPicked.value = [];
  batchRows.value = [];
  batchDialog.value = true;
};
const onBatchPickedChange = (vals) => {
  batchRows.value = vals.map((id) => {
    const existing = batchRows.value.find((r) => r.admissionId === id);
    if (existing)
      return existing;
    const a = admissions.value.find((x) => String(x.admissionId) === String(id));
    return {
      admissionId: String(id),
      patientLabel: a ? patientLabel(a) : String(id),
    };
  });
};
const submitBatch = async () => {
  if (!batchMeasureTime.value) {
    ElMessage.warning('请先选本批测量的统一时点');
    return;
  }
  if (!batchRows.value.length) {
    ElMessage.warning('请先勾选本次测量的患者');
    return;
  }
  batchSaving.value = true;
  try {
    const res = await saveNursingRecordsBatch({
      measureTime: batchMeasureTime.value,
      shift: batchShift.value ?? undefined,
      rows: batchRows.value.map((r) => ({
        admissionId: r.admissionId,
        temperature: r.temperature ?? undefined,
        pulse: r.pulse ?? undefined,
        respiration: r.respiration ?? undefined,
        systolicPressure: r.systolicPressure ?? undefined,
        diastolicPressure: r.diastolicPressure ?? undefined,
        spo2: r.spo2 ?? undefined,
        stoolCount: r.stoolCount ?? undefined,
        urineVolume: r.urineVolume ?? undefined,
        intakeVolume: r.intakeVolume ?? undefined,
        outputVolume: r.outputVolume ?? undefined,
      })),
    });
    ElMessage.success(`批量录入成功：${res.data} 条`);
    batchDialog.value = false;
    await reloadAll();
  } catch (error) {
    ElMessage.error(error.message || '批量录入失败');
  } finally {
    batchSaving.value = false;
  }
};
// ---------------- sql/159 护理专项评估（5 类透视卡 + 专项弹框） ----------------
// 旧「量表下拉 + radio 单选」泛化弹框表达不了：NRS 的部位/性质/措施、VTE 的勾选累加、
// 管路的留置清单 —— 专项各给专属表单，数据仍统一落 biz_nursing_assessment（assess_type 1~5）。
const assessRows = ref([]);
const assessTotal = ref(0);
const assessLoading = ref(false);
const assessPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
/** 透视卡：每类量表最新一条（后端窗口函数取 max(assess_time)，没评过不返回 → 前端渲染「未评估」） */
const latestByType = ref({});
const latestLoading = ref(false);
const loadLatest = async () => {
  if (!admissionId.value)
    return;
  latestLoading.value = true;
  try {
    const res = await getAssessmentLatestByType({admissionId: admissionId.value});
    const map = {};
    for (const row of (res.data || []))
      map[row.assessType] = row;
    latestByType.value = map;
  } catch (error) {
    ElMessage.error(error.message || '加载专项透视失败');
  } finally {
    latestLoading.value = false;
  }
};
const loadAssessments = async () => {
  if (!admissionId.value)
    return;
  assessLoading.value = true;
  try {
    const res = await getNursingAssessmentListPage({
      admissionId: admissionId.value,
      pageNum: assessPagination.value.pageNum,
      pageSize: assessPagination.value.pageSize,
    });
    assessRows.value = (res.data?.records || []);
    assessTotal.value = Number(res.data?.total ?? 0);
  } catch (error) {
    ElMessage.error(error.message || '加载评估单失败');
  } finally {
    assessLoading.value = false;
  }
};
// ---------- 专项弹框（一个入口按类型分支渲染，避免五套 dialog 的模板重复） ----------
const specialDialog = ref(false);
const specialSaving = ref(false);
const specialType = ref(1);
const specialTime = ref('');
const specialRemark = ref('');
/** radio 计分选择：type 1/2 与 type 5 的风险项共用（key → 所选 score） */
const radioPicks = ref({});
// NRS 专项
const nrsScore = ref(null);
const nrsSite = ref('');
const nrsNature = ref('');
const nrsMeasure = ref('');
// Caprini 危险因素勾选（勾中的 item key 数组）
const checkedFactors = ref([]);
// 管路留置清单勾选（不计分，value 项透视）
const checkedTubes = ref([]);
const specialScale = computed(() => SCALES[specialType.value] || SCALES[1]);
/** Caprini 合计 = 勾选项求和 */
const capriniTotal = computed(() => {
  let sum = 0;
  for (const g of (specialScale.value.groups || [])) {
    for (const it of g.items) {
      if (checkedFactors.value.includes(it.key))
        sum += it.score;
    }
  }
  return sum;
});
const specialTotal = computed(() => {
  if (specialType.value === 3)
    return nrsScore.value;
  if (specialType.value === 4)
    return capriniTotal.value;
  return Object.values(radioPicks.value).reduce((s, v) => s + (Number(v) || 0), 0);
});
const specialRiskPreview = computed(() => specialTotal.value === null || specialTotal.value === undefined
    ? null
    : riskLevelOf(specialType.value, specialTotal.value));
const nowStr = () => {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  // 本地取日期：toISOString 会落 UTC，跨时区把"现在"错写前一天
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const openSpecial = (type) => {
  if (!admissionId.value) {
    ElMessage.warning('请先选择在院患者');
    return;
  }
  specialType.value = type;
  radioPicks.value = {};
  nrsScore.value = null;
  nrsSite.value = '';
  nrsNature.value = '';
  nrsMeasure.value = '';
  checkedFactors.value = [];
  checkedTubes.value = [];
  specialRemark.value = '';
  specialTime.value = nowStr();
  specialDialog.value = true;
};
const submitSpecial = async () => {
  const items = [];
  if (specialType.value === 3) {
    if (nrsScore.value === null) {
      ElMessage.warning('请先点选疼痛强度（0~10）');
      return;
    }
    items.push({key: 'pain', label: '疼痛强度', score: nrsScore.value});
    if (nrsSite.value.trim())
      items.push({key: 'painSite', label: '疼痛部位', value: nrsSite.value.trim()});
    if (nrsNature.value)
      items.push({key: 'painNature', label: '疼痛性质', value: nrsNature.value});
    if (nrsMeasure.value.trim())
      items.push({key: 'painMeasure', label: '处理措施', value: nrsMeasure.value.trim()});
  } else if (specialType.value === 4) {
    for (const g of (specialScale.value.groups || [])) {
      for (const it of g.items) {
        if (checkedFactors.value.includes(it.key))
          items.push({key: it.key, label: it.label, score: it.score});
      }
    }
    // 0 分（无危险因素）是合法临床结论：补一条 0 分占位项，明细不为空
    if (!items.length)
      items.push({key: 'noFactor', label: '未勾选任何危险因素（总分 0）', score: 0});
  } else {
    const scaleItems = (specialScale.value.items || []);
    if (scaleItems.some((it) => radioPicks.value[it.key] === undefined)) {
      ElMessage.warning('量表项未选满（总分必须能从明细推导）');
      return;
    }
    for (const it of scaleItems) {
      items.push({key: it.key, label: it.label, score: radioPicks.value[it.key]});
    }
    if (specialType.value === 5) {
      for (const t of (specialScale.value.tubes || [])) {
        if (checkedTubes.value.includes(t.key)) {
          items.push({key: `tube:${t.key}`, label: t.label, value: '留置中'});
        }
      }
    }
  }
  if (!specialTime.value) {
    ElMessage.warning('请选择评估时间');
    return;
  }
  specialSaving.value = true;
  try {
    await saveNursingAssessment({
      admissionId: admissionId.value,
      assessType: specialType.value,
      totalScore: specialTotal.value,
      itemsJson: JSON.stringify(items),
      assessTime: specialTime.value,
      remark: specialRemark.value || undefined,
    });
    ElMessage.success('评估单已保存');
    specialDialog.value = false;
    await Promise.all([loadAssessments(), loadLatest()]);
  } catch (error) {
    ElMessage.error(error.message || '保存评估单失败');
  } finally {
    specialSaving.value = false;
  }
};
// ---------------- G14-3 出入量小结 ----------------
const ioSummary = ref(null);
const ioLoading = ref(false);
const ioRange = ref(null);
const loadIoSummary = async () => {
  if (!admissionId.value)
    return;
  ioLoading.value = true;
  try {
    const res = await getIntakeOutputSummary({
      admissionId: admissionId.value,
      beginDate: ioRange.value?.[0] || undefined,
      endDate: ioRange.value?.[1] || undefined,
    });
    ioSummary.value = res.data;
  } catch (error) {
    ElMessage.error(error.message || '加载出入量小结失败');
  } finally {
    ioLoading.value = false;
  }
};
const handleAssessTabEnter = async (name) => {
  if (name === 'assess')
    await Promise.all([loadAssessments(), loadLatest()]);
  if (name === 'io')
    await loadIoSummary();
};
onMounted(async () => {
  try {
    const res = await getNursingTypeOptions();
    typeOptions.value = (res.data || []);
  } catch (error) {
    console.error('加载护理类型失败:', error);
  }
  await loadAdmissions();
  await reloadAll();
});
</script>
