<template>
  <div class="w-full">
    <!-- 周统计总览：页面顶部白底卡片行，与下方 tab 分离；口径 = 当前科室筛选 × 面板所在周，随上周/下周切换联动 -->
    <div class="mb-3 grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5" data-testid="panel-stats">
      <div
          :title="`实排 ${panelStats.served} / 应排 ${panelStats.expected}：本周有班次的医生占在岗名册的比例，实排少于应排说明有医生漏排`"
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
          data-testid="stat-coverage">
        <p class="text-xl font-semibold text-slate-800">
          {{ panelStats.served }}<span class="text-sm font-normal text-slate-400"> / {{ panelStats.expected }}</span>
        </p>
        <p class="text-xs text-slate-400">应排/实排 · 覆盖 {{ panelStats.coveragePct }}%</p>
      </div>
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
          data-testid="stat-planned-days" title="本周所有医生累计的排班天数总和（医生×日期去重，同一天多个班次算 1 天）">
        <p class="text-xl font-semibold text-blue-600">{{ panelStats.plannedDays }}</p>
        <p class="text-xs text-slate-400">已排班天数</p>
      </div>
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
          data-testid="stat-total-source">
        <p class="text-xl font-semibold text-slate-800">{{ panelStats.total }}</p>
        <p class="text-xs text-slate-400">总排班号源量</p>
      </div>
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
          data-testid="stat-stopped">
        <p :class="panelStats.stopped > 0 ? 'text-red-500' : 'text-slate-800'" class="text-xl font-semibold">
          {{ panelStats.stopped }}
        </p>
        <p class="text-xs text-slate-400">停诊班次数</p>
      </div>
      <div
          class="rounded-md border border-slate-200 bg-white px-4 py-1.5 text-center transition duration-200 hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md cursor-default"
          data-testid="stat-avg-source" title="总号源 ÷ 班次数：横向对比不同医生、不同科室的排班负荷">
        <p class="text-xl font-semibold text-emerald-600">{{ panelStats.avgPerShift }}</p>
        <p class="text-xs text-slate-400">平均单班次排号量</p>
      </div>
    </div>

    <div class="mb-3 rounded-lg border border-slate-200 bg-white px-4 py-3 shadow-sm" data-testid="on-duty-panel">
      <div class="mb-2 flex flex-wrap items-center gap-3">
        <span class="text-base font-medium text-slate-700">今日在岗</span>
        <el-tag effect="plain" size="small" type="info">{{ onDutyMoment || '—' }}</el-tag>
        <span class="text-xs text-slate-400" data-testid="on-duty-count">
          {{ onDutyDeptName || '未选科室' }} ·
          共 {{ onDutyList.length }} 人在岗
        </span>
        <div class="ml-auto flex items-center gap-2">
          <span v-if="onDutyLoading" class="text-xs text-slate-400">加载中…</span>
          <el-button :icon="Refresh" data-testid="on-duty-refresh" size="small" @click="loadOnDuty">刷新</el-button>
        </div>
      </div>

      <div v-if="!panelDeptId" class="py-2 text-sm text-slate-400" data-testid="on-duty-empty">
        请先在下方选择科室
      </div>
      <div v-else class="flex flex-col gap-2" data-testid="on-duty-groups">
        <div v-for="g in onDutyGroups" :key="g.staffType"
             :data-testid="`on-duty-group-${g.staffType}`"
             class="flex flex-wrap items-center gap-2">
          <span class="w-14 shrink-0 text-xs text-slate-500">{{ g.staffTypeName }}</span>
          <el-tag v-for="s in g.list" :key="s.scheduleId"
                  :data-testid="`on-duty-item`"
                  :title="`${s.startTime || ''}-${s.endTime || ''}${s.roomName ? ' · ' + s.roomName : ''}`"
                  class="on-duty-tag"
                  effect="light"
                  size="large"
                  type="success">
            {{ s.staffName }}
            <span class="ml-1 text-xs text-slate-500">
              {{ s.shiftName || `${s.startTime}-${s.endTime}` }}{{ s.roomName ? ' · ' + s.roomName : '' }}
            </span>
          </el-tag>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeView">
      <!-- 周视图排班面板 -->
      <el-tab-pane label="排班面板" name="panel">
        <div ref="panelCard" :style="{ height: panelCardHeight + 'px' }"
             class="flex flex-col rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="filter-row flex flex-wrap items-center gap-3">
            <span class="text-base font-medium text-slate-700">科室</span>
            <el-select v-model="panelDeptId" :fit-input-width="false" class="!w-48" clearable data-testid="panel-dept"
                       filterable placeholder="全部科室"
                       popper-class="schedule-filter-popper"
                       @change="handlePanelDeptChange">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
            <span class="text-base font-medium text-slate-700">人员</span>
            <el-select v-model="panelDoctorKey" :fit-input-width="false" class="!w-52" clearable
                       data-testid="panel-doctor"
                       filterable placeholder="全部人员"
                       popper-class="schedule-filter-popper"
                       @change="handlePanelDoctorChange">
              <el-option v-for="d in panelDoctorOptions" :key="d.key"
                         :label="panelDeptId ? d.doctorName : `${d.doctorName}（${d.deptName}）`"
                         :value="d.key"/>
            </el-select>
            <div class="flex items-center gap-2">
              <el-button @click="handlePanelWeekShift(-1)"><span class="px-0.5 text-base leading-none">‹</span>上一周
              </el-button>
              <span class="min-w-[190px] text-center text-base font-medium text-slate-600">{{ panelWeekLabel }}</span>
              <el-button @click="handlePanelWeekShift(1)">下一周<span class="px-0.5 text-base leading-none">›</span>
              </el-button>
              <el-button v-if="panelWeekOffset !== 0" link type="primary" @click="handlePanelBackToThisWeek">
                回本周
              </el-button>
            </div>
            <span class="ml-auto flex items-center gap-2">
              <el-button v-perm="'outpatient:schedule:add'" :loading="generateLoading"
                         :title="`目标周固定为下周（${nextWeekRangeLabel}），与上面「上一周/下一周」的浏览筛选无关；已过去的日期会自动跳过`"
                         data-testid="generate-by-template"
                         @click="handleGenerateFromTemplate">按模板生成下周排班</el-button>
              <el-button v-perm="'outpatient:schedule:add'" :loading="replicateLoading" type="success"
                         @click="handleReplicateNextWeek">
                复刻到 {{ replicateTargetLabel }}
              </el-button>
              <el-button v-perm="'outpatient:schedule:add'" :loading="weekActionLoading"
                         :disabled="!weekDraftCount" data-testid="week-submit"
                         title="把当前显示周内本科室草稿一次性提交为待审核（已发布/待审核的不动）"
                         @click="handleSubmitWeek">整周提交审核<template v-if="weekDraftCount">（{{ weekDraftCount }}）</template>
              </el-button>
              <el-button v-perm="'outpatient:schedule:audit'" :loading="weekActionLoading" type="success"
                         :disabled="!weekPendingCount" data-testid="week-audit"
                         title="把当前显示周内本科室待审核一次性审核发布并出号（草稿需先提交）"
                         @click="handleAuditWeek">整周审核发布<template v-if="weekPendingCount">（{{ weekPendingCount }}）</template>
              </el-button>
              <el-button v-perm="'outpatient:schedule:add'" :icon="Plus" type="primary"
                         @click="handleAdd">新增排班</el-button>
            </span>
          </div>
          <div ref="panelTableWrap" v-loading="panelLoading" class="mt-3 min-h-0 flex-1 overflow-x-auto">
            <el-table :data="doctorRows" :max-height="panelTableMaxHeight" border min-width="980"
                      style="width: 100%">
              <el-table-column fixed="left" width="170">
                <template #header><span class="text-base font-semibold text-slate-800">{{
                    staffTypeText(panelStaffType)
                  }}人员</span></template>
                <template #default="{ row }">
                  <div :data-testid="`panel-row-${row.doctorName}`">
                    <p class="text-base font-semibold text-slate-800">
                      {{ row.doctorName }}<span v-if="row.title"
                                                :class="getTitleTagClass(titleMap, row.title)"
                                                class="ml-1 inline-block rounded px-1 py-px align-middle text-[10px] font-normal leading-4 ring-1 ring-inset">{{
                        getTitleLabel(titleMap, row.title)
                      }}</span>
                    </p>
                    <p class="text-sm text-slate-500">{{ row.deptName }}</p>
                    <p v-if="!hasShift(row)" class="text-sm text-slate-400">本周未排班</p>
                  </div>
                </template>
              </el-table-column>
              <el-table-column v-for="d in panelDays" :key="d" align="center" min-width="140">
                <template #header>
                  <p :class="['text-base font-semibold', d === fmtISO(new Date()) ? 'text-blue-700' : 'text-slate-800']">
                    {{ d }} {{ dayMap[new Date(d + 'T00:00:00').getDay()] }}
                  </p>
                </template>
                <template #default="{ row }">
                  <div class="group/cell relative min-h-[60px] py-1">
                    <!-- 「加排班」悬停才浮现（opacity 切换 + 绝对定位，格子尺寸零变化）；已过日期整格只读，按钮直接不渲染 -->
                    <button v-if="d >= todayStr" v-perm="'outpatient:schedule:add'"
                            :data-testid="`panel-add-${row.doctorId}-${d}`"
                            class="absolute right-1 top-0.5 z-20 cursor-pointer rounded border border-blue-200 bg-blue-50 px-1.5 py-0.5 text-[10px] font-medium leading-4 text-blue-600 opacity-0 transition-opacity duration-150 group-hover/cell:opacity-100 hover:bg-blue-100"
                            title="为该医生该日补班"
                            @click="handlePanelCellAdd(row, d)">加排班
                    </button>
                    <div :class="['flex flex-col gap-1', d >= todayStr ? 'mt-6' : '']">
                      <div v-for="s in cellsFor(row, d)" :key="s.id"
                           :class="['group relative rounded border px-2.5 py-1.5 text-left leading-5',
                                  s.status === 0
                                    ? 'border-slate-200 bg-slate-100 text-slate-400 line-through'
                                    : 'border-slate-200 bg-white ' + shiftColor(s)]"
                           :data-testid="`panel-cell-${row.doctorId}-${d}`"
                           :title="cellTooltip(s)">
                        <!-- 第一行：班次名 + 发布态标签居左，label「已挂」+ 已挂/总号源 紧靠右（值加粗） -->
                        <div class="flex items-center justify-between gap-2">
                          <span class="flex min-w-0 items-center gap-1.5">
                            <span :class="s.status === 0 ? '' : 'font-medium text-slate-700'"
                                  class="text-sm">{{ shiftText(s) }}</span>
                            <!-- 发布态（G-15）：文案唯一来源 = 后端 publishStatusText，前端不抄中文 -->
                            <el-tag v-if="s.publishStatus != null" :data-testid="`publish-tag-${s.id}`"
                                    :type="publishStatusTagType(s.publishStatus)"
                                    class="!px-1.5 !align-middle shrink-0"
                                    effect="plain"
                                    size="small">{{ s.publishStatusText }}
                            </el-tag>
                          </span>
                          <!-- 只有医生岗有号源：出勤岗这一格显示「在岗」，不显示永远为 0 的 已挂/总 -->
                          <span v-if="s.status !== 0 && staffTypeHasSource(s.staffType)" class="text-xs text-slate-600 shrink-0">已挂 <span
                              class="text-base font-bold text-slate-900">{{ usedOf(s) }}/{{
                              s.totalSource
                            }}</span></span>
                          <span v-else-if="s.status !== 0" class="text-xs font-medium text-emerald-600 shrink-0">在岗</span>
                          <span v-else class="text-sm shrink-0">停用</span>
                        </div>
                        <div class="mt-0.5 text-xs leading-4 text-slate-600">
                          <p class="truncate">
                            <span v-if="s.status === 0">已停用 · 原 {{ s.totalSource }} 个号源</span>
                            <template v-else>
                              <span>{{ s.startTime || '' }}-{{ s.endTime || '' }}</span>
                              <template v-if="staffTypeHasSource(s.staffType)"> · <span
                                  :class="s.roomName ? '' : 'font-medium text-amber-600'">{{
                                  s.roomName || '未排诊室'
                                }}</span><span v-if="s.isExpert === 1"
                                               class="ml-2 inline-block rounded bg-red-50 px-1 py-px text-[10px] font-medium leading-4 text-red-500 ring-1 ring-inset ring-red-200">专家</span>
                              </template>
                            </template>
                          </p>
                        </div>
                        <!-- 操作行：详情/停/加 与 发布流转（提交审核/审核发布/进入调班/撤回）同一行，鼠标移到卡片上才浮现（hover），
                             默认透明不占位跳动（高度由 flex 预留）。日期已过 → 整条不渲染（服务端同口径拒绝） -->
                        <div v-if="s.status === 1 && d >= todayStr"
                             class="mt-2.5 flex flex-wrap items-center justify-end gap-x-2 gap-y-0.5 leading-5 opacity-0 transition-opacity duration-150 group-hover:opacity-100">
                          <button v-perm="'outpatient:schedule:edit'" :data-testid="`panel-edit-${row.doctorId}-${d}`"
                                  class="cursor-pointer text-[13px] text-blue-600"
                                  title="查看排班详情"
                                  @click.stop="handlePanelCellEdit(s)">详情
                          </button>
                          <!-- 调班中(PUBLISH_ADJUSTING)：停、加属变更动作，连同它们紧邻的分隔竖线一起隐藏，只留详情与撤回 -->
                          <template v-if="s.publishStatus !== PUBLISH_ADJUSTING">
                            <span class="text-slate-200">|</span>
                            <button v-perm="'outpatient:schedule:delete'"
                                    :title="staffTypeHasSource(s.staffType) ? '快捷停诊' : '停用该班次'"
                                    class="cursor-pointer text-[13px] text-red-500"
                                    @click.stop="handleToggleStatus(s)">停
                            </button>
                            <!-- 加号只给医生出诊班：出勤岗没有号源池（后端同样拒绝），按钮直接不渲染 -->
                            <template v-if="staffTypeHasSource(s.staffType)">
                              <span class="text-slate-200">|</span>
                              <button v-perm="'outpatient:schedule:edit'" class="cursor-pointer text-[13px] text-blue-600"
                                      title="专家加号"
                                      @click.stop="handleAddSource(s)">加
                              </button>
                            </template>
                          </template>
                          <!-- 发布流转（G-15）：状态互斥，当前态动作 + 撤回；调班中只剩撤回 -->
                          <template v-if="s.publishStatus != null">
                            <span class="text-slate-200">|</span>
                            <button v-if="s.publishStatus === PUBLISH_DRAFT" v-perm="'outpatient:schedule:add'"
                                    :data-testid="`publish-submit-${s.id}`"
                                    class="cursor-pointer text-[13px] font-medium text-blue-600"
                                    title="提交审核（草稿→待审核）"
                                    @click.stop="handlePublishSubmit(s)">提交审核
                            </button>
                            <button v-else-if="s.publishStatus === PUBLISH_PENDING" v-perm="'outpatient:schedule:audit'"
                                    :data-testid="`publish-audit-${s.id}`"
                                    class="cursor-pointer text-[13px] font-medium text-emerald-600"
                                    title="审核发布（待审核→已发布，同时对外出号）"
                                    @click.stop="handleAuditPublish(s)">审核发布
                            </button>
                            <button v-else-if="isPublishEffective(s.publishStatus)" v-perm="'outpatient:schedule:edit'"
                                    :data-testid="`publish-adjust-${s.id}`"
                                    class="cursor-pointer text-[13px] font-medium text-sky-600"
                                    title="进入调班（已发布/已变更→调班中，号源暂冻结）"
                                    @click.stop="handleAdjustStart(s)">进入调班
                            </button>
                            <!-- 调班中动作按钮已不渲染，撤回前这条分隔竖线一并隐藏，避免出现孤立竖线 -->
                            <span v-if="s.publishStatus !== PUBLISH_ADJUSTING" class="text-slate-200">|</span>
                            <button v-perm="'outpatient:schedule:edit'"
                                    :data-testid="`publish-recall-${s.id}`"
                                    class="cursor-pointer text-[13px] text-slate-400 hover:text-red-500"
                                    title="撤回（任意态→草稿，已发布的号源会一并撤下）"
                                    @click.stop="handleRecallPublish(s)">撤回
                            </button>
                          </template>
                        </div>
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="doctorRows.length === 0 && !panelLoading" class="py-12 text-center text-sm text-slate-400"
                 data-testid="panel-empty">
              <!-- 到了这里说明「一个医生都没有」（名册空 + 排班空），不是「没人排班」——
                   后者现在照样出一行，不该再走空态 -->
              <template v-if="panelDeptId != null">
                本科室没有在岗{{ staffTypeText(panelStaffType) }}人员 ——
                <el-button link type="primary" @click="panelDeptId = null; loadPanel()">查看全部科室</el-button>
              </template>
              <template v-else>
                没有可排班的在岗{{ staffTypeText(panelStaffType) }}人员（先在员工管理里给员工配好科室与岗位）
              </template>
            </div>
          </div>

          <!-- 医生维度周汇总：本周谁的号最紧张一目了然。
               它自己封顶 + 内部滚（全院视图几百个医生时会铺成十几行），
               不然汇总越长表格越矮 —— 排班面板的主角是表格，不是这块附注。 -->
          <div v-if="doctorSummary.length"
               class="mt-3 max-h-14 shrink-0 overflow-y-auto border-t border-slate-100 pt-2">
            <p class="mb-2 text-xs font-medium text-slate-500">{{
                staffTypeText(panelStaffType)
              }}本周汇总（按已挂号排序）</p>
            <div class="flex flex-wrap gap-2">
              <div v-for="row in doctorSummary" :key="row.name"
                   class="flex items-center gap-2 rounded-md border border-slate-200 bg-slate-50 px-2.5 py-1.5 text-xs">
                <span class="font-medium text-slate-700">{{ row.name }}</span>
                <span class="text-slate-400">{{ row.shifts }} 班</span>
                <span class="text-slate-500">已挂 <span class="font-semibold text-blue-600">{{
                    row.used
                  }}</span>/{{ row.total }}</span>
                <span v-if="row.stopped > 0" class="font-medium text-red-500">停诊 {{ row.stopped }}</span>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="排班模板" lazy name="template">
        <div class="flex flex-col rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
             style="height: calc(100vh - 236px); min-height: 420px;">
          <!-- 搜索区：模板是长期资产，量会一直涨 → 条件必须下推后端，前端不切片 -->
          <div class="filter-row mb-2 flex flex-wrap items-center gap-3">
            <el-input v-model="tplSearchForm.keyword" :prefix-icon="Search" class="!w-56"
                      clearable data-testid="tpl-keyword" placeholder="搜索医生 / 科室 / 诊室"
                      @clear="handleTplSearch" @keyup.enter="handleTplSearch"/>
            <el-select v-model="tplSearchForm.deptId" class="!w-44" clearable data-testid="tpl-dept" filterable
                       placeholder="全部科室" @change="handleTplSearch">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
            <el-select v-model="tplSearchForm.weekDay" class="!w-36" clearable data-testid="tpl-weekday"
                       placeholder="全部星期" @change="handleTplSearch">
              <el-option v-for="(label, day) in weekDayMap" :key="day" :label="label" :value="Number(day)"/>
            </el-select>
            <el-select v-model="tplSearchForm.status" class="!w-36" clearable data-testid="tpl-status"
                       placeholder="全部状态" @change="handleTplSearch">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="停用"/>
            </el-select>
            <el-button :icon="Search" data-testid="tpl-search-btn" type="primary" @click="handleTplSearch">查询
            </el-button>
            <el-button :icon="Refresh" @click="handleTplReset">重置</el-button>
            <div class="ml-auto flex items-center gap-3">
              <el-button :icon="Refresh" @click="loadTemplates">刷新</el-button>
              <el-button v-perm="'outpatient:schedule:add'" :icon="Plus" type="primary" @click="handleTplAdd">新增模板
              </el-button>
            </div>
          </div>

          <!-- 表格铺满剩余高度，行多时表格内部滚动，分页常驻底部 -->
          <div class="min-h-0 flex-1">
            <el-table v-loading="tplLoading" :data="templates" :height="tplTableHeight" border data-testid="tpl-table"
                      stripe style="width: 100%">
              <el-table-column label="科室" min-width="110" prop="deptName"/>
              <el-table-column label="人员" min-width="100" prop="doctorName"/>
              <el-table-column align="center" label="星期几" min-width="90">
                <template #default="{ row }">
                  <el-tag :type="row.weekDay === 6 || row.weekDay === 7 ? 'warning' : 'info'" size="small">
                    {{ weekDayMap[row.weekDay] || '-' }}
                  </el-tag>
                  <p v-if="(row.weekParity || 0) > 0" class="mt-0.5 text-[10px] text-slate-400">
                    {{ weekParityMap[row.weekParity] }}</p>
                </template>
              </el-table-column>
              <el-table-column label="班次" min-width="90">
                <template #default="{ row }">
                  <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', shiftColor(row)]">
                    {{ shiftText(row) }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column label="时段" min-width="120">
                <template #default="{ row }">
                  <span class="font-mono text-sm text-slate-600">{{ row.startTime }}-{{ row.endTime }}</span>
                </template>
              </el-table-column>
              <el-table-column label="生效期" min-width="180">
                <template #default="{ row }">
                  <span v-if="row.validFrom || row.validUntil" class="text-xs text-slate-500">
                    {{ row.validFrom || '不限' }} ~ {{ row.validUntil || '不限' }}
                  </span>
                  <span v-else class="text-xs text-slate-400">长期有效</span>
                </template>
              </el-table-column>
              <el-table-column align="center" label="号源" min-width="80">
                <template #default="{ row }">
                  <!-- 出勤岗没有号源：显示「—」而不是 0（0 会被读成「号源配错了」） -->
                  <span v-if="staffTypeHasSource(row.staffType)" class="font-medium">{{ row.totalSource }}</span>
                  <span v-else class="text-xs text-slate-300">—</span>
                </template>
              </el-table-column>
              <el-table-column align="center" label="预约池" min-width="90">
                <template #default="{ row }">
                  <span v-if="!staffTypeHasSource(row.staffType)" class="text-xs text-slate-300">—</span>
                  <span v-else-if="(row.appointmentSource || 0) > 0"
                        class="text-xs text-blue-600">划 {{ row.appointmentSource }}</span>
                  <span v-else class="text-xs text-slate-400">不划</span>
                </template>
              </el-table-column>
              <el-table-column label="诊室" min-width="110" prop="roomName"/>
              <el-table-column align="right" label="门诊诊查费" min-width="90">
                <template #default="{ row }">
                  <span v-if="staffTypeHasSource(row.staffType)">¥{{ row.diagnosisFee || 0 }}</span>
                  <span v-else class="text-xs text-slate-300">—</span>
                </template>
              </el-table-column>
              <el-table-column align="center" label="专家" min-width="70">
                <template #default="{ row }">
                  <el-tag v-if="row.isExpert === 1" size="small" type="danger">专家</el-tag>
                  <span v-else class="text-xs text-slate-300">—</span>
                </template>
              </el-table-column>
              <el-table-column align="center" label="状态" min-width="80">
                <template #default="{ row }">
                  <el-tag :type="statusColors[row.status]" size="small">{{ statusMap[row.status] || '-' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column class-name="tpl-op-col" fixed="right" label="操作" width="180">
                <template #default="{ row }">
                  <el-button v-if="row.status === 1" v-perm="'outpatient:schedule:edit'" link size="small"
                             type="warning"
                             @click="handleTplToggle(row)">停用
                  </el-button>
                  <el-button v-else v-perm="'outpatient:schedule:edit'" link size="small" type="success"
                             @click="handleTplToggle(row)">启用
                  </el-button>
                  <el-button v-perm="'outpatient:schedule:edit'" :icon="Edit" link type="primary"
                             @click="handleTplEdit(row)">
                    编辑
                  </el-button>
                  <el-button v-perm="'outpatient:schedule:delete'" :icon="Delete" link type="danger"
                             @click="handleTplDelete(row)">删除
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-10 text-sm text-slate-400">
                  暂无模板。点击「新增模板」配置人员（医生/护理/医技/收费…）的固定班次，之后每周在「排班面板」一键生成
                </div>
              </template>
            </el-table>
          </div>

          <div class="mt-3 flex justify-end">
            <el-pagination
                v-model:current-page="tplPagination.pageNum"
                v-model:page-size="tplPagination.pageSize"
                :page-sizes="PAGE_SIZES"
                :total="tplPagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleTplSizeChange"
                @current-change="handleTplPageChange"
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- 变更记录：门诊排班审计（谁在何时对哪个班次做了什么、为什么），停诊/加号/退号/段编辑全落账 -->
      <el-tab-pane label="变更记录" name="logs">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="filter-row mb-3 flex flex-wrap items-center gap-3">
            <span class="text-base font-medium text-slate-700">科室</span>
            <el-select v-model="logSearchForm.deptId" :fit-input-width="false" class="!w-48" clearable
                       data-testid="log-dept" filterable placeholder="全部科室" popper-class="schedule-filter-popper">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
            <span class="text-base font-medium text-slate-700">排班日期</span>
            <el-date-picker v-model="logDateRange" data-testid="log-date-range" :value-format="'YYYY-MM-DD'"
                            end-placeholder="结束日期" range-separator="~" start-placeholder="开始日期"
                            type="daterange" class="!w-64"/>
            <span class="text-base font-medium text-slate-700">动作</span>
            <el-select v-model="logSearchForm.actionType" :fit-input-width="false" class="!w-32" clearable
                       data-testid="log-action" placeholder="全部动作" popper-class="schedule-filter-popper">
              <el-option v-for="o in LOG_ACTION_TYPES" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-button :icon="Search" data-testid="log-search" type="primary" @click="handleLogSearch">查询</el-button>
            <el-button @click="handleLogReset">重置</el-button>
          </div>
          <el-table v-loading="logLoading" :data="logList" border data-testid="log-table" size="default">
            <el-table-column label="排班日期" min-width="110">
              <template #default="{row}">{{ row.scheduleDate || '—' }}</template>
            </el-table-column>
            <el-table-column label="科室" min-width="130" prop="deptName"/>
            <el-table-column label="医生" min-width="100" prop="doctorName"/>
            <el-table-column label="动作" min-width="90">
              <template #default="{row}">
                <el-tag :type="logActionTagType(row.actionType)" size="small">{{ row.actionTypeText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="数量" min-width="70" align="right">
              <template #default="{row}">{{ row.amount ?? '—' }}</template>
            </el-table-column>
            <el-table-column label="原因" min-width="200" prop="reason" show-overflow-tooltip/>
            <el-table-column label="操作人" min-width="90" prop="createBy"/>
            <el-table-column label="发生时间" min-width="160" prop="occurTime"/>
          </el-table>
          <div class="mt-3 flex items-center justify-between">
            <span class="text-xs text-slate-400">共 {{ logPagination.total }} 条（仅记录门诊排班维护动作，历史数据无留痕属存量事实）</span>
            <el-pagination v-model:current-page="logPagination.pageNum" v-model:page-size="logPagination.pageSize"
                           :page-sizes="PAGE_SIZES" :total="logPagination.total"
                           background layout="total, sizes, prev, pager, next, jumper"
                           @current-change="loadLogs" @size-change="handleLogSizeChange"/>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 新增排班（批量：人员多选 × 日期区间）与修改排班（单条）拆成两个组件：
         以前一个弹窗靠 v-if="isEdit" 分叉，两套排版挤在一起，改一边就碰坏另一边。 -->
    <ScheduleAdd
        v-model="dialogVisible"
        :departments="departments"
        :seed="addSeed"
        :title-map="titleMap"
        @saved="loadPanel"
    />
    <ScheduleEdit
        v-model="editVisible"
        :row="editingSchedule"
        @stop="onDetailStop"
    />

    <!-- 新增/编辑排班模板弹窗 -->
    <ScheduleTplEdit
        v-model="tplDialogVisible"
        :departments="departments"
        :row="editingTpl"
        :title-map="titleMap"
        :week-day-map="weekDayMap"
        @saved="loadTemplates"
    />

    <!-- 停诊影响名单 + 批量退号 -->
    <el-dialog v-model="stopImpactVisible" destroy-on-close title="停诊确认——该班次在挂患者" width="640px">
      <div v-loading="stopImpactLoading">
        <p class="mb-3 text-sm text-slate-600">
          <span class="font-medium">{{ stopImpactSchedule?.doctorName }}</span>
          {{ stopImpactSchedule?.scheduleDate }} {{ shiftText(stopImpactSchedule) }}
          · 共 <span class="font-semibold text-red-500">{{ stopImpactList.length }}</span> 人在挂
        </p>
        <el-table v-if="stopImpactList.length" :data="stopImpactList" border max-height="320" size="small">
          <el-table-column label="患者" min-width="110">
            <template #default="{ row }">{{ row.patientName }}</template>
          </el-table-column>
          <el-table-column label="联系电话" min-width="120">
            <template #default="{ row }">{{ row.phone || '-' }}</template>
          </el-table-column>
          <el-table-column align="center" label="渠道" width="80">
            <template #default="{ row }">
              <el-tag :type="row.registSource === 4 ? 'warning' : 'info'" size="small">
                {{ registSourceMap[row.registSource] || '窗口' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="状态" width="90">
            <template #default="{ row }">{{ registStatusMap[row.registStatus] || row.registStatus }}</template>
          </el-table-column>
          <el-table-column align="center" label="时段" width="70">
            <template #default="{ row }">{{ row.slotTime || '-' }}</template>
          </el-table-column>
        </el-table>
        <p v-else class="py-6 text-center text-sm text-slate-400">该班次暂无在挂患者，可直接停诊</p>
        <p v-if="stopImpactList.length" class="mt-3 text-xs text-slate-400">
          批量退号按原挂号渠道回补号源（预约单还预约池、窗口单还现场池）；已缴费的记录需先到收费处退费，失败记录会在结论中提示。
        </p>
      </div>
      <template #footer>
        <el-button v-if="stopImpactList.length" v-perm="'outpatient:schedule:delete'" :loading="batchCancelLoading"
                   type="warning" @click="handleBatchCancel">
          停诊并批量退号（{{ stopImpactList.length }}）
        </el-button>
        <el-button v-perm="'outpatient:schedule:delete'" type="danger" @click="handleStopOnly">仅停诊（名单留存自行通知）
        </el-button>
        <el-button @click="stopImpactVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 按模板生成——预览确认 -->
    <el-dialog v-model="previewVisible" destroy-on-close title="按模板生成下周排班——预览" width="680px">
      <div v-loading="previewLoading">
        <template v-if="previewData">
          <p class="mb-3 text-sm text-slate-600">
            目标周：<span class="font-medium">{{ previewData.weekStart }} ~ {{ previewData.weekEnd }}</span>
            · 将新增 <span class="font-semibold text-blue-600">{{ (previewData.willCreate || []).length }}</span> 条
          </p>
          <div v-if="(previewData.willCreate || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">将生成</p>
            <p v-for="(d, i) in previewData.willCreate" :key="'c' + i" class="text-xs text-slate-600">· {{ d }}</p>
          </div>
          <div v-if="(previewData.skipExist || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：已存在（{{ previewData.skipExist.length }}）</p>
            <p v-for="(d, i) in previewData.skipExist" :key="'se' + i" class="text-xs text-slate-400">· {{ d }}</p>
          </div>
          <div v-if="(previewData.skipOverlap || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：时间冲突（{{ previewData.skipOverlap.length }}）</p>
            <p v-for="(d, i) in previewData.skipOverlap" :key="'so' + i" class="text-xs text-slate-400">· {{ d }}</p>
          </div>
          <div v-if="(previewData.skipParity || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：单双周不匹配（{{
                previewData.skipParity.length
              }}）</p>
            <p v-for="(d, i) in previewData.skipParity" :key="'sp' + i" class="text-xs text-slate-400">· {{ d }}</p>
          </div>
          <div v-if="(previewData.skipPast || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：日期已过（{{ previewData.skipPast.length }}）</p>
            <p v-for="(d, i) in previewData.skipPast" :key="'sp2' + i" class="text-xs text-slate-400">· {{ d }}</p>
            <p class="mt-1 text-xs text-amber-600">这些日期已就诊完毕，不能再生成班次（号源池不该出现能挂历史号的口子）</p>
          </div>
          <div v-if="(previewData.skipExpired || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">跳过：不在生效日期（{{
                previewData.skipExpired.length
              }}）</p>
            <p v-for="(d, i) in previewData.skipExpired" :key="'sx' + i" class="text-xs text-slate-400">· {{ d }}</p>
          </div>
          <div v-if="(previewData.skipInvalid || []).length" class="mb-3">
            <p class="mb-1 text-xs font-medium text-slate-500">
              跳过：模板没选班次或班次已删除（{{ previewData.skipInvalid.length }}）</p>
            <p v-for="(d, i) in previewData.skipInvalid" :key="'si' + i" class="text-xs text-slate-400">· {{ d }}</p>
            <p class="mt-1 text-xs text-amber-600">时间段由班次带出，这类模板要编辑一次选上班次才能生成</p>
          </div>
          <div v-if="(previewData.doctorNames || []).length"
               class="rounded-md bg-amber-50 px-3 py-2 text-xs text-amber-700">
            涉及医生：{{ (previewData.doctorNames || []).join('、') }}——请确认目标周无停诊/请假人员（系统暂无可靠的医生停用数据，出诊人请人工核对）
          </div>
        </template>
      </div>
      <template #footer>
        <el-button v-perm="'outpatient:schedule:add'" :disabled="!previewData || !(previewData.willCreate || []).length"
                   :loading="generateLoading"
                   type="primary"
                   @click="handleConfirmGenerate">确认生成
        </el-button>
        <el-button @click="previewVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 专家加号：数量与原因合到一个弹框（原先两次 prompt 前后脱节），原因写入排班留痕 -->
    <el-dialog v-model="addSourceVisible" destroy-on-close title="专家加号" width="460px">
      <el-form label-position="left" label-width="84px">
        <el-form-item v-if="addSourceRow" label="班次">
          <span class="text-sm text-slate-700">
            {{ addSourceRow.doctorName }} · {{ addSourceRow.scheduleDate }} · {{ shiftText(addSourceRow) }}
          </span>
        </el-form-item>
        <el-form-item v-if="addSourceRow" label="当前号源">
          <span class="text-sm text-slate-600">
            总 {{ addSourceRow.totalSource }} · 剩余 {{ addSourceRow.availableSource }}
          </span>
        </el-form-item>
        <el-form-item label="加号数量" required>
          <el-input-number v-model="addSourceForm.num" :min="1" :max="50" class="!w-full" controls-position="right"/>
        </el-form-item>
        <el-form-item label="加号原因" required>
          <el-input v-model="addSourceForm.reason" type="textarea" :rows="3" maxlength="100" show-word-limit
                    placeholder="必填，写入排班变更留痕"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :loading="addSourceLoading" type="primary" @click="handleAddSourceSubmit">加号</el-button>
        <el-button @click="addSourceVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, onUnmounted, ref, watch} from 'vue';
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  addScheduleSource,
  batchCancelRegist,
  deleteSchedule,
  deleteScheduleTemplate,
  generateScheduleFromTemplate,
  getClinicSourceLogPage,
  getOnDutyStaff,
  getScheduleList,
  getScheduleTemplateListPage,
  getStopImpact,
  previewScheduleTemplate,
  replicateSchedule,
  scheduleAdjustStart,
  scheduleAuditPublish,
  scheduleAuditWeek,
  scheduleRecall,
  scheduleSubmit,
  scheduleSubmitWeek,
  updateScheduleStatus,
  updateScheduleTemplateStatus
} from '@/api/appoint';
import {getDepartmentSelectList, getEmployeeList, getUserInfo} from '@/api/system';
import {DICT_TYPE, loadDictDataList} from '@/lib/dict-cache';
import {
  SCHEDULE_TYPE_TEXT,
  STAFF_TYPE_DOCTOR,
  staffTypeHasSource,
  staffTypeText
} from '@/lib/scheduleShift';
import {PUBLISH_ADJUSTING, PUBLISH_DRAFT, PUBLISH_PENDING, isPublishEffective, publishStatusTagType} from '@/lib/publishStatus';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {fmtISO} from '@/lib/scheduleDate';
import {getTitleLabel, getTitleTagClass} from '@/lib/scheduleTitle';
import ScheduleAdd from './ScheduleAdd.vue';
import ScheduleEdit from './ScheduleEdit.vue';
import ScheduleTplEdit from './ScheduleTplEdit.vue';

const departments = ref([]);
const titleMap = ref({});
// ========== 排班模板（周模板驱动） ==========
const templates = ref([]);
const tplLoading = ref(false);
const tplDialogVisible = ref(false);
/** 模板新增/编辑共用一个组件（ScheduleTplEdit）：row 为空 = 新增，有值 = 编辑，标题跟着分叉 */
const editingTpl = ref(null);
const generateLoading = ref(false);
const weekDayMap = {1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六', 7: '周日'};
const weekParityMap = {0: '每周', 1: '单周', 2: '双周'};
// 模板页是「长期资产」：量会一直涨，全量拉下来既慢也没法翻查 → 走分页 + 关键词
const tplSearchForm = ref({
  keyword: '',
  deptId: null,
  weekDay: null,
  status: null,
});
const tplPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const loadTemplates = async () => {
  tplLoading.value = true;
  try {
    const res = await getScheduleTemplateListPage({
      pageNum: tplPagination.value.pageNum,
      pageSize: tplPagination.value.pageSize,
      keyword: tplSearchForm.value.keyword || undefined,
      deptId: tplSearchForm.value.deptId || undefined,
      // 模板集固定只取医生岗：本页模板生成的是出诊计划，出勤岗模板不归这里管
      staffType: STAFF_TYPE_DOCTOR,
      weekDay: tplSearchForm.value.weekDay || undefined,
      status: tplSearchForm.value.status ?? undefined,
    });
    templates.value = res.data?.records || [];
    tplPagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载排班模板失败:', error);
  } finally {
    tplLoading.value = false;
  }
};
const handleTplSearch = () => {
  tplPagination.value.pageNum = 1;
  loadTemplates();
};
const handleTplReset = () => {
  tplSearchForm.value = {keyword: '', deptId: null, weekDay: null, status: null};
  tplPagination.value.pageNum = 1;
  loadTemplates();
};
const handleTplPageChange = (val) => {
  tplPagination.value.pageNum = val;
  loadTemplates();
};
const handleTplSizeChange = (val) => {
  tplPagination.value.pageSize = val;
  tplPagination.value.pageNum = 1;
  loadTemplates();
};
const handleTplAdd = () => {
  editingTpl.value = null;
  tplDialogVisible.value = true;
};
const handleTplEdit = (row) => {
  editingTpl.value = row;
  tplDialogVisible.value = true;
};
const handleTplDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`删除 ${row.doctorName} ${weekDayMap[row.weekDay]} ${shiftText(row)} 的模板？`, '提示', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning',
    });
    await deleteScheduleTemplate(row.id);
    ElMessage.success('模板已删除');
    loadTemplates();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
const handleTplToggle = async (row) => {
  const stop = row.status === 1;
  try {
    await updateScheduleTemplateStatus(row.id, stop ? 0 : 1);
    ElMessage.success(stop ? '模板已停用' : '模板已启用');
    loadTemplates();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
// ========== 生成预览（dryRun） ==========
const previewVisible = ref(false);
const previewData = ref(null);
const previewLoading = ref(false);
/**
 * 模板生成的目标周 = **下周**，与面板的「上一周/下一周」筛选**无关**。
 *
 * 周切换只是"看哪一周"的筛选条件，它一旦当写入目标参数用，就会出现
 * 「翻到上周想看历史 → 点生成 → 真往上周写班次」这种事故（实测过这个耦合）。
 * 后端 weekOffset 不传时默认也是 1（下周），这里显式传同一个口径，两边一致。
 * 本周剩下的日子要补班走「手工新增」或「复刻」，模板是长期资产、不承担临时补排。
 */
const GENERATE_TARGET_WEEK_OFFSET = 1;
/** 下周区间（给按钮文案与预览确认用，本地拼串不用 toISOString） */
const nextWeekRangeLabel = computed(() => {
  const monday = mondayOfPanel(GENERATE_TARGET_WEEK_OFFSET);
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  return `${fmtISO(monday).slice(5)} ~ ${fmtISO(sunday).slice(5)}`;
});
/** 按模板生成下周排班：先预览确认（含医生名单人工核对），再执行 */
const handleGenerateFromTemplate = async () => {
  previewLoading.value = true;
  previewVisible.value = true;
  try {
    // 生成的是下周医生出诊计划：模板集与目标周由后端按医生岗收口，科室跟随面板筛选
    const res = await previewScheduleTemplate(GENERATE_TARGET_WEEK_OFFSET, panelDeptId.value, panelStaffType);
    previewData.value = res.data || {};
  } catch (error) {
    
    previewVisible.value = false;
  } finally {
    previewLoading.value = false;
  }
};
const handleConfirmGenerate = async () => {
  generateLoading.value = true;
  try {
    const res = await generateScheduleFromTemplate(GENERATE_TARGET_WEEK_OFFSET, panelDeptId.value, panelStaffType);
    ElMessage.success(res.message || '生成完成');
    previewVisible.value = false;
    loadPanel();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    generateLoading.value = false;
  }
};
// 新增与修改是两个弹窗：新增的医生是「多选」（一批排完一个科室），
// 修改的医生是「单条只读」（换人等于改成别人的班次），两者本来就不是同一个控件。
// 表单状态、班次字典候选与提交校验都在 ScheduleAdd / ScheduleEdit 组件里，页面只控显隐 + 传打开时的数据。
const dialogVisible = ref(false);
const editVisible = ref(false);
/** 新增弹窗的预填：工具栏「新增」为空壳，点面板空格子时才带科室/医生/日期 */
const addSeed = ref(null);
/** 修改弹窗的行数据 */
const editingSchedule = ref(null);
// ---------- 变更记录（门诊排班审计）：停诊/启用/加号/批量退号/段级编辑/新增/修改/删除/复刻全落账 ----------
const LOG_ACTION_TYPES = [
  {value: 1, label: '新增排班'},
  {value: 2, label: '修改排班'},
  {value: 3, label: '删除排班'},
  {value: 4, label: '停诊'},
  {value: 5, label: '启用'},
  {value: 6, label: '加号'},
  {value: 7, label: '批量退号'},
  {value: 8, label: '段级编辑'},
  {value: 9, label: '复刻'},
];
const logActionTagType = (actionType) => {
  if (actionType === 4) return 'danger';    // 停诊
  if (actionType === 3 || actionType === 7) return 'warning'; // 删除/批量退号
  if (actionType === 5 || actionType === 1) return 'success'; // 启用/新增
  return 'info';
};
const logDateRange = ref([]);
const logSearchForm = ref({deptId: null, actionType: null});
const logPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const logList = ref([]);
const logLoading = ref(false);
const loadLogs = async () => {
  logLoading.value = true;
  try {
    const res = await getClinicSourceLogPage({
      pageNum: logPagination.value.pageNum,
      pageSize: logPagination.value.pageSize,
      deptId: logSearchForm.value.deptId || undefined,
      startDate: logDateRange.value?.[0] || undefined,
      endDate: logDateRange.value?.[1] || undefined,
      actionType: logSearchForm.value.actionType ?? undefined,
    });
    logList.value = res.data?.records || [];
    logPagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载门诊排班变更记录失败:', error);
  } finally {
    logLoading.value = false;
  }
};
const handleLogSearch = () => {
  logPagination.value.pageNum = 1;
  loadLogs();
};
const handleLogReset = () => {
  logSearchForm.value = {deptId: null, actionType: null};
  logDateRange.value = [];
  handleLogSearch();
};
const handleLogSizeChange = (val) => {
  logPagination.value.pageSize = val;
  logPagination.value.pageNum = 1;
  loadLogs();
};
// 本地日期格式化在 lib/scheduleDate.js（新增/修改排班组件同一份，不在此重复定义）
const statusMap = {0: '停用', 1: '正常'};
const statusColors = {0: 'danger', 1: 'success'};
const dayMap = {0: '周日', 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六'};
const shiftColors = {
  1: 'bg-blue-100 text-blue-700',
  2: 'bg-emerald-100 text-emerald-700',
  3: 'bg-purple-100 text-purple-700',
  4: 'bg-indigo-100 text-indigo-700',
  5: 'bg-slate-200 text-slate-700',
};
// 班次文案唯一口径在 lib/scheduleShift.js（1上午/2下午/3全天/4凌晨/5夜班）
const shiftLabel = SCHEDULE_TYPE_TEXT;
/**
 * 面板/提示上的班次名：**字典名优先**（「专家门诊」「急诊前夜班」这种才是排班员说的班次），
 * 退回班别（后端按 shift_id 补的派生值），都没了才是「未分类」——不猜。
 */
const shiftText = (row) => row?.shiftName || shiftLabel[row?.scheduleType] || '未分类';
const shiftColor = (row) => shiftColors[row?.scheduleType] || 'bg-slate-50 text-slate-600';
/** 格子 tooltip：紧凑密度下时段/占用/诊室/预约余量的唯一出口（不丢信息） */
const cellTooltip = (s) => {
  const parts = [
    `${shiftText(s)} ${s.startTime || ''}-${s.endTime || ''}`.trim(),
    `已挂 ${usedOf(s)}/${s.totalSource}`,
  ];
  if (s.roomName)
    parts.push(s.roomName);
  if (!s.roomName && s.status !== 0)
    parts.push('未排诊室');
  if (s.isExpert === 1)
    parts.push('专家');
  if ((s.appointmentSource || 0) > 0) {
    parts.push(`约余${Math.max((s.appointmentSource || 0) - (s.usedAppointmentSource || 0), 0)}`);
  }
  if (s.status === 0)
    parts.push('已停诊');
  return parts.join(' · ');
};
// ========== 周视图排班面板 ==========
const activeView = ref('panel');
const panelDeptId = ref(null);
const panelDoctorId = ref(null);
/**
 * 面板固定只看医生岗：本页排的是「出诊计划」（号源、诊室、门诊诊查费都挂在医生班次上），
 * 号源表也只允许医生落班。护理/医技/收费等纯出勤排班在「员工排班」页，那边按单元排。
 * 因此这里不给岗位切换入口——以前能切到护理看，但新增/模板弹窗仍只提交医生，
 * 切过去只能看不能排，是个死路。
 */
const panelStaffType = STAFF_TYPE_DOCTOR;
/**
 * 选中的"科室+医生"组合键（`deptId_doctorId`）。
 * 不能只存 doctorId：同一个 doctor_id 会横跨多个科室（实测 2098255864065458177 同时排在
 * 全科医学科与呼吸内科），只按 id 过滤会把几个科室的班次一起拉出来。
 */
const panelDoctorKey = ref(null);
const panelWeekOffset = ref(0); // 0=本周，1=下周，-1=上周
const panelLoading = ref(false);
const panelSchedules = ref([]);
const panelDays = ref([]);
const replicateLoading = ref(false);
// ========== 今日在岗（排班的下游出口，sql/196）==========
/**
 * 排班表只是「计划」，业务流程真正要问的是「此刻这个科室谁在班」——这里就是那个答案。
 * 全岗位混排（医生/护理/医技/药学/收费/行政），跟 panels 的单一岗位视角互补：
 * 面板回答「这周怎么排」，这里回答「今天此刻谁顶着」。
 * 科室跟随排班面板的科室（不另起一套筛选状态），双份状态迟早打架。
 */
const onDutyList = ref([]);
const onDutyLoading = ref(false);
const onDutyMoment = ref('');
const loadOnDuty = async () => {
  if (!panelDeptId.value) {
    onDutyList.value = [];
    return;
  }
  onDutyLoading.value = true;
  try {
    // onDutyOnly=true：只要此刻真正在班的（跨零点夜班由后端 ShiftCoverUtil 判定）
    const res = await getOnDutyStaff({deptId: panelDeptId.value, onDutyOnly: true});
    onDutyList.value = res.data || [];
    const d = new Date();
    onDutyMoment.value = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  } catch (error) {
    console.error('加载今日在岗失败:', error);
    onDutyList.value = [];
  } finally {
    onDutyLoading.value = false;
  }
};
/** 在岗名单按岗位分组：医生一组、护理一组……按岗位类别码值升序 */
const onDutyGroups = computed(() => {
  const map = new Map();
  for (const s of onDutyList.value) {
    const key = Number(s.staffType);
    if (!map.has(key))
      map.set(key, []);
    map.get(key).push(s);
  }
  return [...map.entries()]
      .sort((a, b) => a[0] - b[0])
      .map(([staffType, list]) => ({staffType, staffTypeName: staffTypeText(staffType), list}));
});
// 切科室 = 换一批人：在岗名单必须跟着变，否则会停留在上一个科室的人身上
watch(() => panelDeptId.value, () => {
  loadOnDuty();
});
/** 当前科室名。id 在后端 ToStringSerializer 出来是字符串，两边都用 String 比，别用 === */
const onDutyDeptName = computed(() => {
  const hit = departments.value.find(d => String(d.id) === String(panelDeptId.value));
  return hit ? hit.deptName : '';
});
/**
 * 人员名册：**在岗人员全量**（`/system/employee/selectList`，按岗位类别 staffType + status=1）。
 *
 * 面板行 = 名册 ∪ 排班里真实出现的医生。为什么必须带名册：
 * 没排班的医生在排班数据里根本不存在，只按排班去重出不来行 —— 排班员看到的就是
 * "本周谁排了"，而看不到"谁还没排"，而后者才是排班这份工作的主语（排班 = 给没排的人排）。
 * 所以本周无班次的医生也要占一行，七格全空，点格子「＋」直接补班。
 *
 * 反向不能省：排班里的医生可能是外院/多点执业的（sys_employee 里查不到），
 * 只按名册出行会**漏掉真实排班**，所以两边取并集。
 */
const panelRoster = ref([]);
/** 号源占用工具：used = total - available（格子里展示「已挂/总」） */
const usedOf = (s) => (s.totalSource || 0) - (s.availableSource || 0);
/**
 * 统计专用的全量周排班（不带医生筛选）。
 * 为什么单独存一份：统计卡片的口径必须稳定在「科室 × 本周」——如果跟着医生筛选走，
 * 选中某位医生后 panelSchedules 只剩他一个人的班次，「应排/实排覆盖率」就没有意义了。
 * 未选医生时 panelSchedules 直接复用这一份（只发一次请求）；选了医生才多发一次。
 */
const panelAllSchedules = ref([]);
/** 整周流转 loading（提交/发布共用，避免连点） */
const weekActionLoading = ref(false);
/** 当前显示周内待提交草稿数 / 待审核数（整周按钮角标；口径 = 当前科室 × 面板所在周全量） */
const weekDraftCount = computed(() => panelAllSchedules.value.filter((s) => s.publishStatus === PUBLISH_DRAFT).length);
const weekPendingCount = computed(() => panelAllSchedules.value.filter((s) => s.publishStatus === PUBLISH_PENDING).length);
/** 本周排班总览：六张置顶卡片（随上周/下周切换联动，口径 = 当前科室筛选 × 面板所在周） */
const panelStats = computed(() => {
  const list = panelAllSchedules.value;
  const total = list.reduce((a, s) => a + (s.totalSource || 0), 0);
  const shifts = list.length;
  const stopped = list.filter((s) => s.status === 0).length;
  // 应排 = 当前科室筛选下的在岗医生名册；实排 = 本周真有班次的医生（按 科室+医生 去重，
  // 与面板行同粒度）。外院/多点执业医生可能让实排 > 应排，覆盖率封顶 100%。
  const expected = panelRoster.value.length;
  const servedKeys = new Set(list.map((s) => `${s.deptId}_${s.doctorId}`));
  const served = servedKeys.size;
  // 已排班天数：医生 × 日期 去重（同一天上午+下午两个班次算 1 天）
  const dayKeys = new Set(list.map((s) => `${s.deptId}_${s.doctorId}_${s.scheduleDate}`));
  // 平均单班次排号量 = 总号源 / 班次数
  return {
    expected,
    served,
    coveragePct: expected ? Math.min(100, Math.round((served / expected) * 100)) : (served ? 100 : 0),
    plannedDays: dayKeys.size,
    total,
    shifts,
    stopped,
    avgPerShift: shifts ? Math.round(total / shifts) : 0,
  };
});
const panelWeekLabel = computed(() => {
  if (!panelDays.value.length)
    return '';
  return `${panelDays.value[0]} ~ ${panelDays.value[6]}`;
});
/** 今天（YYYY-MM-DD）：面板上「已过日期不再排班」的判定基准，ISO 串可直接字典序比较 */
const todayStr = computed(() => fmtISO(new Date()));
/** 医生维度周汇总：每位医生本周班次数与号源占用（医生视图的轻量形态） */
const doctorSummary = computed(() => {
  const map = new Map();
  panelSchedules.value.forEach((s) => {
    const key = `${s.deptId}_${s.doctorId}`;
    if (!map.has(key)) {
      map.set(key, {
        name: `${s.deptName || ''} ${s.doctorName || ''}`.trim(),
        shifts: 0,
        total: 0,
        used: 0,
        stopped: 0
      });
    }
    const row = map.get(key);
    row.shifts++;
    row.total += s.totalSource || 0;
    row.used += usedOf(s);
    if (s.status === 0)
      row.stopped++;
  });
  return Array.from(map.values()).sort((a, b) => b.used - a.used);
});
/** 面板行：名册 ∪ 排班（去重键 = 科室+医生+姓名，与下拉选项同一个键，过滤才能对得上） */
const doctorRows = computed(() => {
  const map = new Map();
  const put = (r) => {
    const key = doctorOptionKey(r);
    if (!map.has(key)) {
      map.set(key, {
        deptId: r.deptId,
        deptName: r.deptName,
        doctorId: r.doctorId,
        doctorName: r.doctorName,
        title: r.title || ''
      });
    }
  };
  panelRoster.value.forEach(put);
  panelSchedules.value.forEach(put);
  let rows = Array.from(map.values());
  if (panelDoctorKey.value != null) {
    rows = rows.filter(r => doctorOptionKey(r) === panelDoctorKey.value);
  }
  return rows.sort((a, b) => (a.deptName || '').localeCompare(b.deptName || '') || (a.doctorName || '').localeCompare(b.doctorName || ''));
});
/**
 * 医生下拉选项 = **在岗名册 ∪ 当前排班里真实出现的医生**（与面板行同源，键也同名）。
 *
 * 为什么不能只取一边：
 *   1) 只用排班数据 → **没排班的医生在下拉里不存在**，而排班员最常做的事就是"筛出某人给他排"，
 *      等于最常用的路径被堵死（本次改造的直接原因）；
 *   2) 只用员工表 → 排班里的「医生」可能是**外院/多点执业**的（实测 `biz_schedule.doctor_id`
 *      有 id 在 `sys_employee` 里查不到），会**漏掉真实排班**；同一医生还可能被排在
 *      **非主科室**（实测任永星主科室=全科医学科，也在呼吸内科出诊），按员工主科室过滤同样会漏。
 *
 * ⚠️ 去重键必须包含 `deptId + doctorId + doctorName` 三者，**少任何一个都会静默吞选项**：
 *   实测同一个 `doctor_id=2098255864065458177` 在**同一科室**下挂了两个不同名字
 *   （`VSCH-任永星P8` 与 `E2E-TAB-挂号医生`，都在呼吸内科，属夹具残留）。
 *   - 只按 doctorId 去重 → 跨科室的三个名字塌成 1 项；
 *   - 只按 deptId+doctorId 去重 → 同科室的两个名字仍然塌成 1 项（**丢掉的那项无法被选中**，
 *     且用户在下拉里根本看不到它，属于"列表少了东西但不报错"）。
 *   键 = 面板行的展示粒度，才能保证「下拉里能看到的，就是表格里能筛出来的」。
 */
const doctorOptionKey = (s) => `${s.deptId}_${s.doctorId}_${s.doctorName}`;
const panelDoctorOptions = computed(() => {
  const map = new Map();
  const put = (s) => {
    const key = doctorOptionKey(s);
    if (!map.has(key)) {
      map.set(key, {key, doctorId: s.doctorId, doctorName: s.doctorName, deptId: s.deptId, deptName: s.deptName});
    }
  };
  panelRoster.value.forEach((e) => put(e));
  panelSchedules.value.forEach((s) => {
    if (panelDeptId.value != null && String(s.deptId) !== String(panelDeptId.value))
      return;
    put(s);
  });
  return Array.from(map.values()).sort((a, b) => (a.deptName || '').localeCompare(b.deptName || '') || (a.doctorName || '').localeCompare(b.doctorName || ''));
});
const mondayOfPanel = (offset) => {
  const now = new Date();
  const day = now.getDay() || 7;
  const monday = new Date(now);
  monday.setDate(now.getDate() - day + 1 + offset * 7);
  return monday;
};
/**
 * 拉在岗医生名册。只跟科室有关（与翻周无关），所以按科室缓存，翻周不重复请求。
 * 离职/停用（status=0）的不进名册 —— 排班面板是给"还能出诊的人"排的。
 * 但**已排了班的人不受影响**：那部分由 panelSchedules 并集兜住，数据不会凭空消失。
 */
let rosterDeptKey = null;
const loadPanelRoster = async () => {
  // 缓存键按科室即可：名册只跟科室有关（岗位恒医生），翻周不重复请求
  const key = String(panelDeptId.value ?? '__ALL__');
  if (rosterDeptKey === key)
    return;
  try {
    const params = {staffType: STAFF_TYPE_DOCTOR};
    if (panelDeptId.value)
      params.deptId = panelDeptId.value;
    const res = await getEmployeeList(params);
    const list = res.data?.records || res.data || [];
    panelRoster.value = list
        .filter((e) => e.status === 1)
        .map((e) => ({
          deptId: e.deptId,
          deptName: e.deptName || departments.value.find((d) => String(d.id) === String(e.deptId))?.deptName || '',
          doctorId: e.id,
          doctorName: e.empName,
          title: e.title || '',
        }));
    rosterDeptKey = key;
  } catch (error) {
    console.error('加载医生名册失败:', error);
  }
};
const loadPanel = async () => {
  panelLoading.value = true;
  try {
    await loadPanelRoster();
    const monday = mondayOfPanel(panelWeekOffset.value);
    const days = [];
    for (let i = 0; i < 7; i++) {
      const d = new Date(monday);
      d.setDate(monday.getDate() + i);
      days.push(fmtISO(d));
    }
    panelDays.value = days;
    const params = {startDate: days[0], endDate: days[6], staffType: panelStaffType};
    if (panelDeptId.value)
      params.deptId = panelDeptId.value;
    // 统计与医生筛选解耦：先拉不带医生条件的全量（统计卡用）；选了医生再拉一份精简集（表格用），
    // 未选医生时两份同源，只发一次请求。
    const res = await getScheduleList(params);
    panelAllSchedules.value = res.data?.records || res.data || [];
    if (panelDoctorId.value != null) {
      const filtered = await getScheduleList({
        ...params,
        staffType: panelStaffType,
        doctorId: panelDoctorId.value
      });
      panelSchedules.value = filtered.data?.records || filtered.data || [];
    } else {
      panelSchedules.value = panelAllSchedules.value;
    }
  } catch (error) {
    console.error('加载排班面板失败:', error);
  } finally {
    panelLoading.value = false;
  }
};
/**
 * 面板默认科室 = 「我的科室」。
 *
 * 排班管理是按科室运维的（科主任/门诊部看自己科），所以默认落在当前用户主科室；
 * 管理员/门诊部要看全院就点下拉右侧的清除，回到「全部科室」。
 *
 * 四个边界（都实测过，别想当然）：
 *   1) `/auth/info` 的 deptId 必须**先确认科室下拉已就绪**再落值，否则 el-select 显示成裸数字
 *      （`applyPanelDefaultDept` 用 `departments` 找 option，找不到就不设 —— 这是对的）；
 *   2) **落值后要自己再拉一次面板**。onMounted 里 `loadPanel()` 是无参首屏拉取，默认科室是
 *      异步拿到的，晚于它 —— 不补拉就会出现「下拉显示产前诊断中心、表格却是全院数据」
 *      这种 UI 与数据各说各话的状态（实测踩到）。
 *   3) 取不到就保持「全部科室」，默认值不该成为故障点；
 *   4) **只在首次生效**（`deptDefaultApplied` 守卫），否则用户手动清空科室后
 *      任何一次刷新都会把它弹回来 —— 那是"默认值"变成了"强制值"。
 */
let deptDefaultApplied = false;
const applyPanelDefaultDept = async (deptId) => {
  if (deptDefaultApplied || panelDeptId.value != null)
    return;
  if (deptId == null || deptId === '')
    return;
  const hit = departments.value.find((d) => String(d.id) === String(deptId));
  if (hit) {
    panelDeptId.value = hit.id;
    // 默认科室落地后重拉（首屏那次不带 deptId，不重拉会 UI 与数据不一致）
    await loadPanel();
    // 我的科室一个医生都没有 → 空面板比"全院"更难用，退回全部科室。
    // 科室下拉会显示成"全部科室"（清除态），UI 状态本身就是说明，不再弹文字提示。
    if (!doctorRows.value.length) {
      panelDeptId.value = null;
      await loadPanel();
    }
  }
  deptDefaultApplied = true;
};
const loadPanelDefaultDept = async () => {
  try {
    const res = await getUserInfo();
    await applyPanelDefaultDept(res.data?.deptId);
  } catch (error) {
    console.error('加载当前用户科室失败，排班默认「全部科室」:', error);
  }
};
const handlePanelDeptChange = () => {
  // 换科室必须清医生：旧医生未必属于新科室，留着会筛出空表且用户不知道为什么
  panelDoctorKey.value = null;
  panelDoctorId.value = null;
  loadPanel();
};
const handlePanelDoctorChange = () => {
  // 下拉绑的是组合键；同步出 doctorId 供后端下推（后端只按 doctorId 过滤，
  // 所以同 id 跨科室时后端会多给几条，由前端 panelDoctorKey 精确到科室。
  // 这是刻意的：不为一个"同 id 多科室"的脏数据形态给查询接口加参）
  const hit = panelDoctorOptions.value.find(o => o.key === panelDoctorKey.value);
  panelDoctorId.value = hit ? hit.doctorId : null;
  loadPanel();
};
const handlePanelWeekShift = (delta) => {
  panelWeekOffset.value += delta;
  loadPanel();
};
const handlePanelBackToThisWeek = () => {
  panelWeekOffset.value = 0;
  loadPanel();
};
/**
 * 某行某天的班次。
 * 必须**同时比科室**：同一 doctor_id 会横跨多个科室（任永星同时排在全科医学科与呼吸内科），
 * 只按医生过滤会把别的科室的班次画到这一行 —— 名册里每人一行后这个错位会直接可见。
 */
const cellsFor = (row, date) => panelSchedules.value.filter(s => String(s.doctorId) === String(row.doctorId)
    && (row.deptId == null || s.deptId == null || String(s.deptId) === String(row.deptId))
    && s.scheduleDate === date);
/** 该行本周是否有任何班次（名册进来的医生可能一整周都没排） */
const hasShift = (row) => panelSchedules.value.some(s => String(s.doctorId) === String(row.doctorId)
    && (row.deptId == null || s.deptId == null || String(s.deptId) === String(row.deptId)));
/** 点空格子：预填科室/医生/日期，直接进新增弹窗 */
const handlePanelCellAdd = (row, date) => {
  addSeed.value = {
    deptId: row.deptId,
    deptName: row.deptName,
    doctorId: row.doctorId,
    doctorName: row.doctorName,
    // 面板空格子进来：医生预填一位（要批量加人再自己在弹窗里多选）
    doctorIds: [row.doctorId],
    scheduleDateRange: [date, date],
  };
  dialogVisible.value = true;
};
/** 点已有格子：进只读排班详情（不可原地改；停诊、加号走独立按钮） */
const handlePanelCellEdit = (row) => {
  handleEdit(row);
};
// ========== 发布态流转（G-15）：提交/审核/调班/撤回 ==========
// 四个动作都是「单条状态迁移」，一次点击一次请求；成功后统一重载面板，
// 让 publishStatusText 与号源数从后端真相回来（前端不自己算状态）。
const publishActionLabel = (s) => `${s?.doctorName || ''} ${s?.scheduleDate || ''} ${shiftText(s)}`.trim();
/** 整周流转入参：当前显示周起止 + 当前科室 + 岗位（与 loadPanel 查询同口径） */
const weekPayload = () => ({
  deptId: panelDeptId.value ?? undefined,
  staffType: panelStaffType,
  startDate: panelDays.value[0],
  endDate: panelDays.value[6],
});
/** 整周提交审核：当前显示周内本科室草稿一次性提交为待审核（已待审核/已发布/调班中行不动） */
const handleSubmitWeek = async () => {
  if (!weekDraftCount.value) {
    ElMessage.warning('当前周没有待提交的草稿排班');
    return;
  }
  try {
    await ElMessageBox.confirm(
        `将把 ${panelDays.value[0]} ~ ${panelDays.value[6]} 的 ${weekDraftCount.value} 条草稿排班一次性提交审核，是否继续？`,
        '整周提交审核', {confirmButtonText: '提交审核', cancelButtonText: '取消', type: 'info'});
  } catch {
    return;
  }
  weekActionLoading.value = true;
  try {
    const res = await scheduleSubmitWeek(weekPayload());
    ElMessage.success(`已提交审核 ${res.data ?? weekDraftCount.value} 条`);
    loadPanel();
  } catch (error) /* 错误已由全局拦截器统一提示 */ {
  } finally {
    weekActionLoading.value = false;
  }
};
/** 整周审核发布：当前显示周内本科室待审核一次性发布并出号（草稿需先提交，已发布的不动） */
const handleAuditWeek = async () => {
  if (!weekPendingCount.value) {
    ElMessage.warning('当前周没有待审核的排班');
    return;
  }
  try {
    await ElMessageBox.confirm(
        `将把 ${panelDays.value[0]} ~ ${panelDays.value[6]} 的 ${weekPendingCount.value} 条待审核排班一次性审核发布，发布后正式对外出号，是否继续？`,
        '整周审核发布', {confirmButtonText: '审核发布', cancelButtonText: '取消', type: 'warning'});
  } catch {
    return;
  }
  weekActionLoading.value = true;
  try {
    const res = await scheduleAuditWeek(weekPayload());
    ElMessage.success(`已审核发布 ${res.data ?? weekPendingCount.value} 条，已正式出号`);
    loadPanel();
  } catch (error) /* 错误已由全局拦截器统一提示 */ {
  } finally {
    weekActionLoading.value = false;
  }
};
/** 提交审核：草稿→待审核，等审核人发布 */
const handlePublishSubmit = async (row) => {
  try {
    await scheduleSubmit(row.id);
    ElMessage.success('已提交审核');
    loadPanel();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
};
/** 审核发布：待审核→已发布，同时对外出号（号源生成在后端） */
const handleAuditPublish = async (row) => {
  try {
    await scheduleAuditPublish(row.id);
    ElMessage.success(`已审核发布：${publishActionLabel(row)}`);
    loadPanel();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
};
/** 进入调班：已发布/已变更→调班中，号源暂冻结；改完保存后端自动转「已变更」 */
const handleAdjustStart = async (row) => {
  try {
    await ElMessageBox.confirm(
        `将 ${publishActionLabel(row)} 进入调班：暂停接受新预约，已挂号患者不受影响；改完保存即自动重新出号（无需二次审核）。是否继续？`,
        '进入调班',
        {confirmButtonText: '进入调班', cancelButtonText: '取消', type: 'info'});
  } catch {
    return;
  }
  try {
    await scheduleAdjustStart(row.id);
    ElMessage.success('已进入调班，号源暂冻结，改完保存即可');
    loadPanel();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
};
/** 撤回：任意态→草稿。已发布的号源会一并撤下，患者侧立刻不可约，必须先确认 */
const handleRecallPublish = async (row) => {
  const eff = isPublishEffective(row.publishStatus);
  try {
    await ElMessageBox.confirm(
        eff
            ? `撤回 ${publishActionLabel(row)} 将退回草稿，该班已发布的号源会一并撤下，患者将无法预约。是否继续？`
            : `确认撤回 ${publishActionLabel(row)}？撤回后该排班退回草稿，需重新提交审核才生效。`,
        '撤回排班',
        {confirmButtonText: '撤回', cancelButtonText: '取消', type: 'warning'});
  } catch {
    return;
  }
  try {
    await scheduleRecall(row.id);
    ElMessage.success('已撤回至草稿');
    loadPanel();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
};
/** 复刻固定后移一周，前后端同一口径 */
const REPLICATE_OFFSET_DAYS = 7;
/**
 * 复刻的目标周 = 当前显示周 + 7 天。
 * 文案必须算出来而不是写死「下周」：面板翻到下周时，"下一周"其实是下下周 ——
 * 写死会让用户以为点错了（按钮说的是本周，干的是下下周）。
 */
const replicateTargetLabel = computed(() => {
  if (panelDays.value.length !== 7)
    return '下周';
  const start = new Date(panelDays.value[0] + 'T00:00:00');
  start.setDate(start.getDate() + REPLICATE_OFFSET_DAYS);
  const end = new Date(panelDays.value[6] + 'T00:00:00');
  end.setDate(end.getDate() + REPLICATE_OFFSET_DAYS);
  return `${fmtISO(start).slice(5)}~${fmtISO(end).slice(5)}`;
});
/** 把当前显示的这一周整体滚动复制到下一周（停用条目与目标日期已过的条目由后端跳过） */
const handleReplicateNextWeek = async () => {
  const source = panelSchedules.value.filter(s => s.status === 1);
  if (!source.length) {
    ElMessage.warning('当前周没有可复刻的排班');
    return;
  }
  try {
    await ElMessageBox.confirm(`将把 ${panelDays.value[0]} ~ ${panelDays.value[6]} 的 ${source.length} 条正常排班复制到 ${replicateTargetLabel.value}，是否继续？`,
        '复刻排班到下一周', {confirmButtonText: '复刻', cancelButtonText: '取消', type: 'info'});
  } catch {
    return;
  }
  replicateLoading.value = true;
  try {
    // 整周复刻只发一次请求：源区间、后移天数、科室筛选交给后端，
    // 跳过（日期已过/时段已被占）与落库在同一事务里由它算。
    const res = await replicateSchedule({
      deptId: panelDeptId.value ?? undefined,
      staffType: STAFF_TYPE_DOCTOR,
      startDate: panelDays.value[0],
      endDate: panelDays.value[6],
      dayOffset: REPLICATE_OFFSET_DAYS,
    });
    const msg = res.data?.message || `复刻完成：成功 ${res.data?.created ?? 0} 条`;
    // 一条都没复刻上不是「完成」：这里原先恒发 success，后端的结论被洗成绿色提示
    if (!res.data?.created) ElMessage.error(msg);
    else ElMessage.success(msg);
    loadPanel();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    replicateLoading.value = false;
  }
};
const loadDepartments = async () => {
  try {
    // 不传 scope → 默认按当前人过滤：排班员只能给自己的授权科室排班。
    // 这一处同时喂面板筛选（1091）与排班/模板表单（1317/1521/1612），
    // 四处共用一份列表 —— 只要能选的科室就是能排的科室，不会出现"能选不能排"。
    const res = await getDepartmentSelectList({deptType: 1});
    departments.value = res.data || [];
  } catch (error) {
    console.error('加载科室列表失败:', error);
  }
};
// ========== 新增/修改排班弹窗的打开入口（表单逻辑在 ScheduleAdd / ScheduleEdit 组件里） ==========
const handleAdd = () => {
  // 工具栏「新增」不带任何预填：弹窗自己按医生岗拉科室与人员候选
  addSeed.value = {};
  dialogVisible.value = true;
};
const handleEdit = (row) => {
  editingSchedule.value = row;
  editVisible.value = true;
};
/** 详情弹窗里发起停诊：关闭详情，复用格子「停」的影响名单→退号闭环 */
const onDetailStop = () => {
  editVisible.value = false;
  if (editingSchedule.value)
    handleToggleStatus(editingSchedule.value);
};
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await deleteSchedule(row.id);
    ElMessage.success('删除成功');
    loadPanel();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
// ========== 停诊闭环（影响名单 + 批量退号） ==========
const stopImpactVisible = ref(false);
const stopImpactLoading = ref(false);
const stopImpactList = ref([]);
const stopImpactSchedule = ref(null);
const batchCancelLoading = ref(false);
const registSourceMap = {1: '窗口', 2: '自助机', 3: '网上', 4: '预约'};
const registStatusMap = {1: '待支付', 2: '已签到', 3: '就诊中', 4: '已就诊'};
/** 停诊确认弹名单：展示该班次在挂患者，可一键批量退号（按渠道还池），再切停诊 */
const handleToggleStatus = async (row) => {
  if (row.status !== 1) {
    // 启用：直接切
    try {
      await updateScheduleStatus(row.id, 1);
      ElMessage.success('已启用');
      loadPanel();
    } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
      
    }
    return;
  }
  if (!staffTypeHasSource(row.staffType)) {
    try {
      await updateScheduleStatus(row.id, 0);
      ElMessage.success('已停用该出勤班次');
      loadPanel();
    } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
      
    }
    return;
  }
  // 停诊：先拉影响名单
  stopImpactSchedule.value = row;
  stopImpactLoading.value = true;
  stopImpactVisible.value = true;
  try {
    const res = await getStopImpact(row.id);
    stopImpactList.value = res.data || [];
  } catch (error) {
    
    stopImpactVisible.value = false;
  } finally {
    stopImpactLoading.value = false;
  }
};
const handleBatchCancel = async () => {
  const ids = stopImpactList.value.map((r) => r.registId);
  if (!ids.length)
    return;
  batchCancelLoading.value = true;
  try {
    const res = await batchCancelRegist(stopImpactSchedule.value?.id, ids, `班次停诊（${stopImpactSchedule.value?.doctorName || ''} ${stopImpactSchedule.value?.scheduleDate || ''}）批量退号`);
    ElMessage.success(res.message || '批量退号完成');
    // 退完直接停诊
    await updateScheduleStatus(stopImpactSchedule.value.id, 0);
    stopImpactVisible.value = false;
    loadPanel();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    batchCancelLoading.value = false;
  }
};
const handleStopOnly = async () => {
  try {
    await updateScheduleStatus(stopImpactSchedule.value.id, 0);
    ElMessage.success('已停诊，请按名单通知患者改约或退号');
    stopImpactVisible.value = false;
    loadPanel();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
// ========== 加号 ==========
// 数量与原因在同一个弹框里填（原先两次 ElMessageBox.prompt 前后脱节）
const addSourceVisible = ref(false);
const addSourceLoading = ref(false);
const addSourceRow = ref(null);
const addSourceForm = ref({num: 1, reason: ''});
/** 点格子上的「加」：打开加号弹框，不在这里直接提交 */
const handleAddSource = (row) => {
  addSourceRow.value = row;
  addSourceForm.value = {num: 1, reason: ''};
  addSourceVisible.value = true;
};
const handleAddSourceSubmit = async () => {
  const num = Math.min(50, Math.max(1, parseInt(addSourceForm.value.num, 10)));
  if (!Number.isFinite(num) || num < 1) {
    ElMessage.warning('请输入 1-50 的加号数量');
    return;
  }
  const reason = (addSourceForm.value.reason || '').trim();
  if (!reason) {
    ElMessage.warning('请填写加号原因（写入排班留痕）');
    return;
  }
  addSourceLoading.value = true;
  try {
    await addScheduleSource(addSourceRow.value.id, num, reason);
    ElMessage.success(`已加号 ${num} 个`);
    addSourceVisible.value = false;
    loadPanel();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  } finally {
    addSourceLoading.value = false;
  }
};
const loadTitleDict = async () => {
  try {
    const list = await loadDictDataList(DICT_TYPE.SYS_HOSPITAL_TITLE);
    const map = {};
    list.forEach((item) => {
      map[item.dictValue] = item.dictLabel;
    });
    titleMap.value = map;
  } catch {
  }
};
const tplTableHeight = ref(420);
const calcTplTableHeight = () => {
  tplTableHeight.value = Math.max(240, window.innerHeight - 352);
};
const panelCard = ref(null);
const panelTableWrap = ref(null);
const panelCardHeight = ref(420);
const panelTableMaxHeight = ref(420);
const calcPanelTableHeight = () => {
  const card = panelCard.value;
  const wrap = panelTableWrap.value;
  if (!card || !wrap)
    return;
  panelCardHeight.value = Math.max(320, window.innerHeight - card.getBoundingClientRect().top - 12);
  // 卡片高度改完要等一帧，flex 分完才知道表格区剩多少（此时量才准）
  requestAnimationFrame(() => {
    panelTableMaxHeight.value = Math.max(240, wrap.clientHeight);
  });
};
// 模板页签 lazy：切过去才挂载，首次进入要补一次加载；resize 时重算表格高度
watch(activeView, (val) => {
  if (val === 'template') {
    calcTplTableHeight();
    loadTemplates();
  }
  if (val === 'logs') {
    loadLogs();
  }
  nextTick(calcPanelTableHeight);
});
// 面板上方（今日在岗，随人数换行）与下方（本周汇总，随人数换行）都会变高变矮 → 变了就重算
watch(() => [doctorRows.value.length, doctorSummary.value.length, onDutyGroups.value.length], () => {
  nextTick(calcPanelTableHeight);
});
const onWindowResize = () => {
  calcTplTableHeight();
  calcPanelTableHeight();
};
onMounted(() => {
  calcTplTableHeight();
  window.addEventListener('resize', onWindowResize);
  // 必须先拿到科室下拉 → 再把「我的科室」落到真实 option 上 → 再拉面板。
  // 顺序错了会出现「下拉显示 A、表格是全院」的自相矛盾状态（实测踩过）。
  loadDepartments().then(loadPanelDefaultDept).then(() => {
    // 科室列表为空（或取用户科室失败）时兜底拉一次全院，保证面板不是空白
    if (!panelSchedules.value.length && !panelLoading.value)
      loadPanel();
  });
  loadTitleDict();
  loadTemplates();
  nextTick(calcPanelTableHeight);
});
onUnmounted(() => {
  window.removeEventListener('resize', onWindowResize);
});
</script>

<style scoped>
/* 表格线条加深一档：默认 --el-table-border-color #ebeef5 在投影/低对比屏上几乎看不见。
   只作用于本页两个表（排班面板/排班模板；班次字典已拆为独立页面 ShiftDictView），slate-300 = #cbd5e1 */
:deep(.el-table) {
  --el-table-border-color: #cbd5e1;
}

/* 页签/表头/筛选控件整体放大一档（用户反馈原 14px 太小）。
   按钮只放大筛选行（.filter-row），表格里 link 操作按钮维持小字号，避免操作列换行 */
:deep(.el-tabs__item) {
  font-size: 16px;
}

:deep(.el-table th.el-table__cell) {
  font-size: 15px;
}

:deep(.filter-row .el-button),
:deep(.filter-row .el-input__inner),
:deep(.filter-row .el-select__placeholder),
:deep(.filter-row .el-select__selected-item) {
  font-size: 15px;
}

/* 模板表操作列：收紧单元格内边距和按钮间距，三个按钮保持单行 */
:deep(.tpl-op-col .cell) {
  padding-left: 4px;
  padding-right: 4px;
}

:deep(.tpl-op-col .el-button + .el-button) {
  margin-left: 6px;
}
</style>
