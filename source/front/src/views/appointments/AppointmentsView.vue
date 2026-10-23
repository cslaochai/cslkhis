<template>
  <div class="space-y-6">
    <div class="grid grid-cols-3 gap-4 sm:grid-cols-6" data-testid="status-cards">
      <div v-for="item in [
        { label: '总挂号', value: statusCounts.total, color: 'text-slate-700' },
        { label: '待就诊', value: statusCounts.waiting, color: 'text-blue-600' },
        { label: '已签到', value: statusCounts.checkedIn, color: 'text-cyan-600' },
        { label: '已完成', value: statusCounts.completed, color: 'text-emerald-600' },
        { label: '已退号', value: statusCounts.cancelled, color: 'text-red-500' },
        { label: '已过号', value: statusCounts.overdue, color: 'text-orange-500' },
      ]" :key="item.label" :data-testid="'status-card-' + item.label"
           class="rounded-lg border border-slate-200 bg-white px-3 py-2 text-center shadow-sm">
        <p :class="['text-xl font-bold leading-6', item.color]">{{ item.value }}</p>
        <p class="text-xs text-slate-500">{{ item.label }}</p>
      </div>
    </div>
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="预约看板" lazy name="desk">
        <div class="desk-board rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="flex flex-wrap items-center gap-3">
            <!-- 日 / 周 视图切换：真实 HIS 挂号工作台的做法（同一张台子切视图），不是两个菜单 -->
            <el-radio-group v-model="deskViewMode" @change="handleDeskViewChange">
              <el-radio-button value="day">日视图</el-radio-button>
              <el-radio-button value="week">周视图</el-radio-button>
            </el-radio-group>
            <span class="text-sm font-medium text-slate-700">科室</span>
            <el-select v-model="deskDeptId" class="!w-64" clearable data-testid="desk-dept-select" filterable
                       placeholder="全部科室" @change="handleDeskDeptChange">
              <el-option v-for="d in departments" :key="d.id"
                         :label="d.parentDeptName ? `${d.parentDeptName} / ${d.deptName}` : d.deptName"
                         :value="d.id ?? d.deptName"/>
            </el-select>
            <span class="text-sm font-medium text-slate-700">医生</span>
            <el-select v-model="deskDoctorId" class="!w-52" clearable data-testid="desk-doctor-select"
                       filterable
                       placeholder="全部医生" @change="handleDeskDoctorChange">
              <el-option v-for="doc in deskDoctorOptions" :key="doc.id" :label="doc.doctorName" :value="doc.id">
                <span>{{ doc.doctorName }}</span>
                <span v-if="isExpertDoctor(doc.id)"
                      class="ml-1 rounded bg-amber-50 px-1 text-[10px] font-medium text-amber-600 ring-1 ring-amber-200">专家</span>
              </el-option>
            </el-select>
            <div v-if="deskViewMode === 'week'" class="flex items-center gap-2">
              <el-button :icon="ArrowLeft" title="上一周" @click="handleDeskStep(-1)"/>
              <span class="min-w-[210px] text-center text-base font-medium text-slate-600"
                    data-testid="desk-range">{{ deskRangeLabel }}</span>
              <el-button :icon="ArrowRight" title="下一周" @click="handleDeskStep(1)"/>
              <el-button @click="handleDeskToday">回到本周</el-button>
            </div>
            <div class="ml-auto flex items-center gap-2">
              <el-button :icon="Refresh" :loading="deskLoading" data-testid="desk-refresh"
                         @click="handleDeskRefresh">刷新
              </el-button>
              <el-button v-perm="'opd:appointments:add'" :icon="Plus" data-testid="desk-add-appoint" type="primary"
                         @click="handleDeskAdd">新增预约
              </el-button>
            </div>
          </div>
          <div class="mt-3 flex items-stretch gap-4">
            <aside v-if="deskViewMode === 'day'" :style="{maxHeight: deskMaxH + 'px'}"
                   class="flex w-[220px] shrink-0 flex-col overflow-y-auto rounded-lg border border-slate-200 bg-slate-50/50 p-2.5"
                   data-testid="desk-cal">
              <div class="mb-1.5 flex items-center justify-between">
                <button
                    class="rounded border border-slate-200 bg-white px-1.5 text-base leading-6 text-slate-600 hover:bg-slate-100"
                    data-testid="cal-prev"
                    title="上一个月" @click="handleCalShift(-1)">&lsaquo;
                </button>
                <span class="text-sm font-semibold text-slate-700" data-testid="cal-month">{{ calMonthLabel }}</span>
                <button
                    class="rounded border border-slate-200 bg-white px-1.5 text-base leading-6 text-slate-600 hover:bg-slate-100"
                    data-testid="cal-next"
                    title="下一个月" @click="handleCalShift(1)">&rsaquo;
                </button>
              </div>
              <p class="mb-1.5 rounded bg-blue-50 px-2 py-1 text-center text-xs font-medium text-blue-700"
                 data-testid="cal-picked">
                {{ deskDay }} {{ weekdayMap[new Date(deskDay + 'T00:00:00').getDay()] }}
                <span v-if="deskDay === fmtDate(new Date())" class="ml-1 text-[10px] text-blue-400">今天</span>
              </p>
              <div class="grid grid-cols-7 gap-1 text-center">
                <span v-for="w in ['一','二','三','四','五','六','日']" :key="'w' + w"
                      class="py-0.5 text-[11px] font-medium text-slate-400">{{ w }}</span>
              </div>
              <div v-loading="calLoading" class="mt-0.5 grid grid-cols-7 gap-1">
                <button v-for="c in calCells" :key="c.date"
                        :class="c.date === deskDay
                          ? 'bg-blue-600 font-semibold text-white shadow-sm hover:bg-blue-700'
                          : (!c.inMonth
                            ? 'bg-slate-200/70 text-slate-400 hover:bg-slate-300/70'
                            : (c.isToday
                              ? 'bg-white font-semibold text-blue-600 ring-1 ring-inset ring-blue-300 hover:bg-blue-50'
                              : 'bg-white text-slate-700 hover:bg-slate-100'))" :data-date="c.date"
                        :data-has-service="c.hasService ? '1' : '0'"
                        :data-in-month="c.inMonth ? '1' : '0'"
                        :title="c.hasService ? `${c.date} 有排班` : c.date"
                        class="relative flex h-8 w-full items-center justify-center rounded text-sm transition-colors"
                        data-testid="cal-cell"
                        @click="handleCalPick(c.date)">
                  {{ c.day }}
                  <span v-if="c.isToday"
                        :class="c.date === deskDay ? 'text-blue-100' : 'text-blue-500'"
                        class="absolute right-0.5 top-0 text-[9px] font-normal leading-none">今</span>
                  <!-- 有排班的日子加圆点（今天也加：底色不冲突了，两个信号各说一件事） -->
                  <span v-if="c.hasService"
                        :class="c.date === deskDay ? 'bg-white' : 'bg-emerald-500'"
                        class="absolute bottom-0.5 left-1/2 h-1 w-1 -translate-x-1/2 rounded-full"
                        data-testid="cal-dot"></span>
                </button>
              </div>
              <div class="mt-1 flex items-center justify-between text-[11px] text-slate-400">
                <span class="flex items-center gap-1">
                  <span class="h-1 w-1 rounded-full bg-emerald-500"></span> 有排班
                </span>
                <button class="text-blue-600 hover:underline" data-testid="cal-today"
                        @click="handleCalToday(); handleCalPick(fmtDate(new Date()))">回今天
                </button>
              </div>

              <div class="my-2 border-t border-slate-200"></div>
              <p class="mb-1.5 text-[11px] text-slate-400">{{ deskDay }} 概况</p>
              <div class="grid grid-cols-2 gap-1.5">
                <div class="flex items-baseline justify-between rounded bg-white px-2 py-1">
                  <span class="text-[11px] text-slate-500">医生</span>
                  <span class="text-sm font-semibold text-slate-700" data-testid="cal-stat-doctors">
                    {{ deskDayStat.doctors }}<span class="text-xs font-normal text-slate-400"> 位</span>
                  </span>
                </div>
                <div class="flex items-baseline justify-between rounded bg-white px-2 py-1">
                  <span class="text-[11px] text-slate-500">总号源</span>
                  <span class="text-sm font-semibold text-slate-700" data-testid="cal-stat-total">
                    {{ deskDayStat.total }}<span class="text-xs font-normal text-slate-400"> 个</span>
                  </span>
                </div>
                <div class="flex items-baseline justify-between rounded bg-white px-2 py-1">
                  <span class="text-[11px] text-slate-500">已挂号</span>
                  <span class="text-sm font-semibold text-blue-600" data-testid="cal-stat-regs">
                    {{ deskDayStat.regs }}<span class="text-xs font-normal text-slate-400"> 人</span>
                  </span>
                </div>
                <div class="flex items-baseline justify-between rounded bg-white px-2 py-1">
                  <span class="text-[11px] text-slate-500">剩余</span>
                  <span :class="deskDayStat.avail > 0 ? 'text-emerald-600' : 'text-slate-400'"
                        class="text-sm font-semibold"
                        data-testid="cal-stat-avail">
                    {{ deskDayStat.avail }}<span class="text-xs font-normal text-slate-400"> 个</span>
                  </span>
                </div>
              </div>
              <!-- 剩余号源的占比条：一句话说不清的"快满了"用视觉表达 -->
              <div class="mt-1.5 h-1.5 w-full overflow-hidden rounded-full bg-slate-200">
                <div
                    :class="deskDayStat.usedPct >= 90 ? 'bg-rose-400' : (deskDayStat.usedPct >= 70 ? 'bg-amber-400' : 'bg-emerald-500')"
                    :style="{width: deskDayStat.usedPct + '%'}"
                    class="h-full rounded-full transition-all"></div>
              </div>
              <p class="text-center text-[11px] text-slate-400">已用 {{ deskDayStat.usedPct }}%</p>
              <div class="mt-2 border-t border-slate-200 pt-2">
                <el-checkbox v-model="deskHideFull" data-testid="desk-hide-full">隐藏已满/停诊</el-checkbox>
                <p v-if="deskHideFull && deskHiddenTotal > 0" class="mt-1 text-[11px] leading-4 text-amber-600"
                   data-testid="desk-hidden-tip">
                  已隐藏 {{ deskHiddenTotal }} 个已满/停诊班次（关掉开关即可看到）
                </p>
                <div class="mt-2 border-t border-slate-100 pt-2" data-testid="desk-status-filter">
                  <el-checkbox-group v-model="deskStatusFilter" class="flex flex-wrap gap-x-1 gap-y-1">
                    <el-checkbox v-for="s in DESK_STATUS_OPTIONS" :key="s.value" :value="s.value" class="!mr-0">
                      {{ s.label }}
                    </el-checkbox>
                  </el-checkbox-group>
                </div>
              </div>
            </aside>

            <!-- 右：班次 × 医生 / 班次 × 日期 表 -->
            <div class="min-w-0 flex-1">
              <!-- 高度按视口算（撑满窗口）：日视图 18 行起步，写死高度等于强迫人一直滚 -->
              <div ref="deskScrollEl" v-loading="deskLoading" :style="{height: deskMaxH + 'px'}"
                   class="overflow-auto rounded border border-slate-200"
                   data-testid="desk-scroll">
                <!-- 空态要说清「是没排班还是筛掉了」：默认落在我的科室，门诊部/管理员选「全部科室」即可看全院 -->
                <div v-if="!deskLoading && !deskColumns.length"
                     class="flex flex-col items-center justify-center gap-1 py-12 text-center"
                     data-testid="desk-empty">
                  <p class="text-base text-slate-500">
                    {{
                      isAllDept(deskDeptId) ? '当前时间窗口内没有任何排班（已含全部科室）' : '本科室当前时间窗口内没有排班'
                    }}
                  </p>
                  <p class="text-sm text-slate-400">
                    看板默认只显示「我的科室」，把上方「科室」选成「全部科室」即可查看全院；也可以把日期翻到有排班的那天。
                  </p>
                </div>
                <table v-else :class="deskViewMode === 'day' ? 'w-max min-w-[600px]' : 'w-full min-w-[960px]'"
                       class="border-separate border-spacing-0 text-sm">
                  <thead class="sticky top-0 z-20">
                  <tr>
                    <th :class="[deskViewMode === 'day' ? 'min-w-[96px] border-slate-300' : 'min-w-[150px] border-slate-100']"
                        class="sticky left-0 z-30 border-r border-b bg-slate-50 p-2 text-center font-medium text-slate-500">
                      {{ deskViewMode === 'day' ? '时间段' : '医生' }}
                    </th>
                    <th v-for="col in deskColumns" :key="col.key"
                        :class="deskViewMode === 'day' ? 'w-[300px] border-slate-300' : 'border-slate-100'"
                        class="border-r border-b bg-slate-50 p-2 text-center align-top">
                      <!-- 周视图：日期列头可点，下钻到当天日视图。可点性必须在静态态可见，不能只靠 hover tooltip -->
                      <template v-if="deskViewMode === 'week'">
                        <p :class="col.date === fmtDate(new Date()) ? 'text-blue-600' : 'text-slate-700 hover:text-blue-600'"
                           :data-date="col.date"
                           class="group inline-flex cursor-pointer items-center gap-1 rounded px-1.5 py-0.5 text-sm font-semibold
                                transition-colors hover:bg-blue-50"
                           data-testid="desk-date-head"
                           title="点击查看当天的日视图"
                           @click="drillToDay(col.date)">
                          <span class="underline decoration-dotted decoration-1 underline-offset-2">{{
                              col.title
                            }}</span>
                          <svg
                              aria-hidden="true"
                              class="h-3 w-3 shrink-0 text-blue-500 opacity-60 transition-opacity group-hover:opacity-100"
                              viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                            <path
                                d="M512 64a448 448 0 1 1 0 896 448 448 0 0 1 0-896m0 64a384 384 0 1 0 0 768 384 384 0 0 0 0-768m32 128v224h192v64H480V256z"
                                fill="currentColor"/>
                          </svg>
                        </p>
                        <p class="text-xs text-slate-400">{{ col.sub }}</p>
                      </template>
                      <!-- 日视图：医生列头 + 当天余号（0=约满 / <5=少量余号） -->
                      <template v-else>
                        <p :data-doctor-id="col.doctorId"
                           class="flex items-center justify-center gap-1 text-sm font-semibold text-slate-700"
                           data-testid="desk-doctor-head">
                          <span>{{ col.title }}</span>
                          <span v-if="isExpertDoctor(col.doctorId)"
                                class="shrink-0 rounded bg-amber-50 px-1 text-[10px] font-medium leading-4 text-amber-600 ring-1 ring-amber-200"
                                data-testid="desk-expert-badge"
                                title="专家号">专家</span>
                        </p>
                        <p class="text-xs text-slate-400">{{ col.sub || '—' }}</p>
                        <p :class="deskAvailClass(dayDoctorSchedules(col.doctorId))"
                           class="text-xs font-medium" data-testid="desk-doctor-avail">
                          总 {{ deskTotal(dayDoctorSchedules(col.doctorId)) }} ·
                          {{ deskAvailText(dayDoctorSchedules(col.doctorId)) }}
                        </p>
                      </template>
                    </th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="(row, ri) in deskRows" :key="'row-' + row.key" :data-row-key="row.key" class="desk-row">
                    <template v-if="deskViewMode === 'week'">
                      <td class="desk-row-head sticky left-0 z-10 border-r border-b border-slate-100 bg-slate-50 p-2 align-top">
                        <p :data-doctor-id="row.doctorId"
                           class="flex items-center gap-1 text-sm font-semibold text-slate-700"
                           data-testid="desk-row-doctor">
                          <span>{{ row.label }}</span>
                          <span v-if="isExpertDoctor(row.doctorId)"
                                class="shrink-0 rounded bg-amber-50 px-1 text-[10px] font-medium leading-4 text-amber-600 ring-1 ring-amber-200"
                                data-testid="desk-expert-badge"
                                title="专家号">专家</span>
                        </p>
                        <p class="text-[11px] text-slate-400">{{ row.deptName || '—' }}</p>
                        <p class="mt-0.5 text-[11px] leading-4" data-testid="desk-row-stat">
                          <span class="text-blue-600">已挂 {{ weekRowStat(row.doctorId).regs }}</span>
                          <span class="text-slate-300"> / </span>
                          <span
                              :class="deskAvailClass(deskSchedules.filter(s => String(s.doctorId) === String(row.doctorId)))">
                          {{ weekRowStat(row.doctorId).avail > 0 ? '余 ' + weekRowStat(row.doctorId).avail : '约满' }}
                        </span>
                        </p>
                      </td>
                    </template>
                    <td v-else
                        :class="['border-slate-300', ri > 0 && deskRows[ri - 1].sep ? 'border-b-2 !border-b-slate-500' : '']"
                        :data-group="row.group"
                        class="desk-row-head sticky left-0 z-10 border-r border-b bg-slate-50 p-2 text-center font-medium text-slate-600"
                        data-testid="desk-row-slot">
                      <span class="font-mono text-xs">{{ row.label }}</span>
                    </td>
                    <td v-for="col in deskColumns" :key="col.key + '-' + row.key"
                        :class="['align-top border-r border-b',
                               deskViewMode === 'day' ? 'w-[300px] p-1 border-slate-300' : 'min-w-[168px] p-1.5 border-slate-100',
                               ri > 0 && deskRows[ri - 1].sep ? 'border-b-2 !border-b-slate-500' : '',
                               isCellUnavailable(col, row.key) ? 'desk-cell-na' : '']" :data-col="col.key"
                        :data-date="col.date"
                        :data-na="isCellUnavailable(col, row.key) ? '1' : '0'" :data-row="row.key"
                        :data-shift="row.key"
                        data-testid="desk-cell">
                      <template v-if="cellHiddenCountFor(col, row.key) > 0
                                    && !cellSchedulesFor(col, row.key).length">
                        <!-- 日视图行细到半小时后空行很多，空态压到 h-8：18 行 × 44px 光空行就一屏半 -->
                        <p :class="['flex items-center justify-center text-center text-xs text-amber-600', deskViewMode === 'day' ? 'h-8' : 'h-11']"
                           data-testid="desk-cell-allhidden">
                          {{ cellAllFor(col, row.key).length }} 个班次已满/停诊（已隐藏）
                        </p>
                      </template>
                      <p v-else-if="isCellUnavailable(col, row.key)"
                         :class="['flex items-center justify-center text-xs text-slate-400', deskViewMode === 'day' ? 'h-8' : 'h-11']"
                         data-testid="desk-cell-na">
                        -</p>
                      <template v-else>
                        <div class="mb-1 flex flex-wrap items-center justify-between gap-x-1 gap-y-0.5 text-xs">
                        <span class="text-slate-500">
                          已挂 {{
                            deskRegCount(deskCell(col.key, row.key).regs)
                          }} / {{ deskTotal(deskCell(col.key, row.key).schedules) }}
                        </span>
                          <span class="flex shrink-0 items-center gap-1">
                          <span :class="deskAvailClass(deskCell(col.key, row.key).schedules)" class="font-medium">
                            {{ deskAvailText(deskCell(col.key, row.key).schedules) }}
                          </span>
                          <button v-if="deskViewMode === 'week'"
                                  class="shrink-0 cursor-pointer rounded border border-slate-200 bg-white px-1 text-xs leading-4 text-slate-600 hover:bg-slate-50"
                                  data-testid="desk-cell-avail"
                                  title="查看这位医生当天的每个班次：时段 / 余号 / 门诊诊查费"
                                  @click.stop="openCellDialog(col.date, row.key)">余号</button>
                          <button
                              v-if="!isCellTimePassed(col, row.key) && deskAvail(deskCell(col.key, row.key).schedules) > 0"
                              class="shrink-0 cursor-pointer rounded border border-blue-200 bg-white px-1 text-xs font-bold leading-4 text-blue-600 hover:bg-blue-50"
                              data-testid="desk-cell-add"
                              title="新增预约：带入该时段的排班、医生、诊室、科室"
                              @click.stop="addFromCell(col, row.key)">预约</button>
                        </span>
                        </div>
                        <!-- 日视图：当天该医生该班次的患者全铺（单班次最多一个号源数，铺得下） -->
                        <template v-if="deskViewMode === 'day'">
                          <div v-for="r in deskCell(col.key, row.key).regs.filter(deskRegVisible)" :key="r.id"
                               :class="canReschedule(r)
                                ? 'cursor-pointer border-slate-200 bg-white hover:border-blue-300'
                                : 'cursor-default border-slate-100 bg-slate-50 hover:border-slate-300'"
                               :data-regist-id="r.id"
                               :data-regist-status="r.registStatus"
                               :title="deskCardTitle(r)"
                               class="group relative mb-2 overflow-hidden rounded-lg border px-2 py-1 pl-4 text-sm leading-6"
                               data-testid="desk-card"
                               @click="handleDeskCardClick(r)">
                            <!-- 左侧状态色条：4px 宽，贯穿整个卡片高度 -->
                            <span :class="deskStatusChip[Number(r.registStatus)]?.bar || 'bg-slate-300'"
                                  class="absolute left-0 top-0 h-full w-1.5"></span>
                            <div class="flex items-start gap-1.5">
                            <span :class="['mt-1.5 h-2 w-2 shrink-0 rounded-full',
                              deskStatusChip[Number(r.registStatus)]?.dot || regStatusDot[Number(r.registStatus)] || 'bg-slate-300']"></span>
                              <span :class="Number(r.registStatus) >= 4 ? 'text-slate-400' : 'text-slate-700'"
                                    class="min-w-0 flex-1 break-all font-bold leading-5">
                              {{ r.patientName }}
                            </span>
                              <span class="ml-auto flex shrink-0 items-center gap-1">
                                <span v-if="deskStatusChip[Number(r.registStatus)]"
                                      :class="deskStatusChip[Number(r.registStatus)].cls"
                                      class="rounded px-1 py-0.5 text-[9px] font-medium leading-3 whitespace-nowrap">
                                  {{ deskStatusChip[Number(r.registStatus)].text }}
                                </span>
                                <!-- 已收费 = 钱已在账上：改约只放同医生、同费用的号源（后端同样拦），与状态同排一次读完 -->
                                <span v-if="isRegistPaid(r)"
                                      class="rounded bg-emerald-50 px-1 py-0.5 text-[9px] font-medium leading-3 text-emerald-700 whitespace-nowrap">
                                  已收费
                                </span>
                              </span>
                            </div>
                            <p v-if="deskPatientMeta(r) || deskCardVisitNo(r)" class="text-xs text-slate-500">
                              {{ [deskPatientMeta(r), deskCardVisitNo(r)].filter(Boolean).join(' · ') }}
                            </p>
                            <p class="truncate text-xs text-slate-500">
                              {{ deskCardDoctorLine(r) }}
                            </p>
                            <p v-if="deskCardMetaLine(r)" class="truncate text-xs text-slate-500">
                              {{ deskCardMetaLine(r) }}
                            </p>
                            <!-- 收款入口只认账单欠款事实（1-待支付 2-部分支付）：免收/已收齐/账单作废都不是「没收款」。
                                 放退号前面：先办钱再谈退号，收银动线如此。
                                 一个「收费」口进去选档（现金/扫码收款），做成两个按钮等于让收银员先猜客户掏什么 -->
                            <div v-if="canCollectFee(r) || canCancelRegist(r)" class="mt-1 flex justify-end gap-1">
                              <button v-if="canCollectFee(r)"
                                      class="cursor-pointer rounded border border-emerald-200 bg-white px-2 py-0.5 text-xs font-medium text-emerald-600 hover:bg-emerald-50"
                                      data-testid="desk-collect"
                                      @click.stop="handleShowPayment(r)">收费
                              </button>
                              <button v-if="canCancelRegist(r)"
                                  class="cursor-pointer rounded border border-red-200 bg-white px-2 py-0.5 text-xs font-medium text-red-500 hover:bg-red-50"
                                  data-testid="desk-cancel"
                                  @click.stop="handleDeskCancel(r)">退号
                              </button>
                            </div>
                          </div>
                        </template>
                        <!-- 周视图：每格只铺前几张（一天 4 医生 × 20 号 = 80 张，全铺会把 DOM 拖死），其余下钻日视图 -->
                        <template v-else>
                          <div
                              v-for="r in deskCell(col.key, row.key).regs.filter(deskRegVisible).slice(0, DESK_WEEK_CELL_MAX)"
                              :key="r.id"
                              :data-regist-id="r.id"
                              :title="`${r.patientName}｜${deskCardDoctorLine(r)}${sourceLockReason(r) ? '｜' + sourceLockReason(r) : ''}`"
                              class="flex items-center gap-1 leading-6"
                              data-testid="desk-week-card">
                          <span :class="['h-1.5 w-1.5 shrink-0 rounded-full',
                            deskStatusChip[Number(r.registStatus)]?.dot || regStatusDot[Number(r.registStatus)] || 'bg-slate-300']"></span>
                            <span :class="Number(r.registStatus) >= 4 ? 'text-slate-400' : 'text-slate-700'"
                                  class="truncate text-xs font-bold">
                            {{ r.patientName }}
                          </span>
                            <span v-if="deskStatusChip[Number(r.registStatus)]"
                                  :class="deskStatusChip[Number(r.registStatus)].cls.split(' ').filter((c: string) => c.startsWith('text-')).join(' ')"
                                  class="ml-auto shrink-0 text-xs">
                            {{ deskStatusChip[Number(r.registStatus)].text }}
                          </span>
                          </div>
                          <p v-if="deskCell(col.key, row.key).regs.filter(deskRegVisible).length > DESK_WEEK_CELL_MAX"
                             class="cursor-pointer text-xs text-blue-600 hover:underline"
                             data-testid="desk-cell-more"
                             @click="drillToDay(col.date)">
                            还有 {{
                              deskCell(col.key, row.key).regs.filter(deskRegVisible).length - DESK_WEEK_CELL_MAX
                            }} 人 → 看当天
                          </p>
                        </template>
                      </template>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="挂号记录" name="records">
        <div>
          <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <el-form inline>
              <el-form-item label="患者">
                <PatientSelect
                    v-model="searchForm.patientId"
                    :page-size="20"
                    placeholder="搜索患者"
                    size="small"
                    width="192px"
                />
              </el-form-item>
              <el-form-item label="科室">
                <el-select v-model="searchForm.deptId" class="!w-56" clearable filterable placeholder="全部科室">
                  <el-option v-for="d in departments" :key="d.id"
                             :label="d.parentDeptName ? `${d.parentDeptName} / ${d.deptName}` : d.deptName"
                             :value="d.id ?? d.deptName"/>
                </el-select>
              </el-form-item>
              <el-form-item label="就诊状态">
                <el-select v-model="searchForm.registStatus" class="!w-32" clearable placeholder="全部">
                  <el-option v-for="(label, val) in statusMap" :key="val" :label="label" :value="Number(val)"/>
                </el-select>
              </el-form-item>
              <el-form-item label="就诊类型">
                <el-select v-model="searchForm.visitType" class="!w-32" clearable placeholder="全部">
                  <el-option v-for="item in VISIT_TYPE" :key="item.value" :label="item.label" :value="item.value"/>
                </el-select>
              </el-form-item>
              <el-form-item label="就诊日期">
                <el-date-picker
                    v-model="searchForm.visitDate"
                    class="!w-40"
                    clearable
                    placeholder="全部日期"
                    type="date"
                    value-format="YYYY-MM-DD"
                    @change="handleSearch"
                />
              </el-form-item>
              <el-form-item label="挂号时间">
                <el-date-picker
                    v-model="searchForm.registDateRange"
                    :shortcuts="dateShortcuts"
                    class="!w-72"
                    end-placeholder="结束日期"
                    range-separator="至"
                    start-placeholder="开始日期"
                    type="daterange"
                    value-format="YYYY-MM-DD"
                />
              </el-form-item>
              <el-form-item class="ml-auto">
                <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
                <el-button :icon="Refresh" @click="handleReset">重置</el-button>
                <el-button v-perm="'opd:appointments:add'" :icon="Plus" class="ml-2" type="primary"
                           @click="addFromPanel()">新增挂号
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="mt-4 rounded-lg border border-slate-200 bg-white shadow-sm">
            <el-table v-loading="loading" :data="appointments" style="width: 100%">
              <el-table-column label="就诊号" width="200">
                <template #default="{ row }">
                  <span class="font-mono text-sm text-slate-600">{{ row.registNo || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="患者姓名" prop="patientName" width="120"/>
              <el-table-column label="患者编号" prop="patientNo" width="200"/>
              <el-table-column label="就诊日期" width="110">
                <template #default="{ row }">
                  <span class="font-mono text-xs text-slate-600">{{ row.visitDate || '-' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="科室" prop="deptName" width="120"/>
              <el-table-column label="医生" prop="doctorName" width="120"/>
              <el-table-column label="类型" width="150">
                <template #default="{ row }">
                  <!-- 号别口径同 lib/statusColor：未知/未标注显「未标注」，不回落成初诊/复诊 -->
                  <el-tag :class="revisitTypeOf(row.visitType).color + ' border'" effect="plain"
                          size="small">{{ revisitTypeOf(row.visitType).label }}
                  </el-tag>
                  <!-- 复诊来源决定这张号收没收费，账对不上时第一眼要看的就是它 -->
                  <span v-if="row.revisitSource" class="ml-1 text-xs text-slate-500">
                {{ revisitSourceText(row.revisitSource) }}
              </span>
                </template>
              </el-table-column>
              <el-table-column label="结算方式" width="120">
                <template #default="{ row }">
              <span
                  :class="['text-xs font-medium', settlementTypes.find(t => t.value === row.settlementType)?.color || 'text-slate-600']">
                {{ settlementLabel(row.settlementType) }}
              </span>
                </template>
              </el-table-column>
              <el-table-column label="医保卡号" width="200">
                <template #default="{ row }">
              <span v-if="row.medicalInsuranceNo" class="font-mono text-xs text-slate-600">{{
                  row.medicalInsuranceNo
                }}</span>
                  <span v-else class="text-xs text-slate-400">-</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :class="statusConfig[statusMap[row.registStatus]]?.color || null" class="border gap-1"
                          effect="plain"
                          size="small">
                    {{ statusMap[row.registStatus] }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="300">
                <template #default="{ row }">
                  <el-button v-if="row.registStatus === 1" link size="small" type="success" @click="handleCheckIn(row)">
                    签到
                  </el-button>
                  <el-button v-if="canReschedule(row)" v-perm="'opd:appointments:edit'"
                             :icon="Edit" link size="small"
                             type="primary" @click="handleEdit(row)">
                    编辑
                  </el-button>
                  <el-button v-if="canCollectFee(row)" link size="small" type="success"
                             @click="handleShowPayment(row)">
                    收费
                  </el-button>
                  <!-- 只有「已挂号(1)」「已签到(2)」才有退号入口，其余状态一律不出现（含灰色禁用态）：
                       已接诊/已就诊是诊疗已经发生，已过号/爽约/未就诊的号源属过去日期、退了也还不了池
                       （见 DayEndSettleMapper），已退号更没有重复退的道理。
                       状态本身在左侧「状态」列已写明，不需要再摆一个点不动的按钮。 -->
                  <el-button v-if="canCancelRegist(row)" v-perm="'opd:appointments:delete'" link size="small"
                             type="danger" @click="handleCancel(row)">
                    退号
                  </el-button>
                  <!-- 就诊日一过，退号入口消失，但「人昨天没来、今天要退钱」的诉求还在。
                       这条出口走退费申请（挂号状态不动，钱由收费处审核执行），见脚本区同名注释。 -->
                  <el-button v-if="canApplyRefund(row)" v-perm="'finance:refund:add'" data-testid="apply-refund" link
                             size="small"
                             type="warning" @click="handleApplyRefund(row)">
                    申请退费
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <div v-if="appointments.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
              暂无挂号记录
            </div>
            <div class="mt-4 flex justify-end">
              <el-pagination
                  v-model:current-page="pagination.pageNum"
                  v-model:page-size="pagination.pageSize"
                  :page-sizes="PAGE_SIZES"
                  :total="pagination.total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 看板改约：换号源弹窗（已拆到同目录组件；原子换号由后端做，成功后只刷看板） -->
    <AppointmentsRescheduleEdit v-model="showRescheduleDialog" :row="rescheduleRow" :schedule="rescheduleSchedule"
                                @saved="loadDesk"/>

    <!-- 申请退费弹窗：可退金额已由 handleApplyRefund 从账单里算好，组件只管表单与提交 -->
    <AppointmentsRefundEdit v-model="showRefundDialog" :initial="refundInitial"/>

    <!-- 周视图格子的「医生号源明细」弹框：格子数据是看板事实，算好传入；「去挂号」回抛给本页开单 -->
    <AppointmentsCellDetail v-model="showCellDialog" :cell="selectedCell"
                            :schedules="selectedCell ? cellSchedules(selectedCell.date, selectedCell.doctorId) : []"
                            :hidden-count="selectedCell ? cellHiddenCount(selectedCell.date, selectedCell.doctorId) : 0"
                            @register="goRegister"/>

    <!-- 新增挂号弹窗：seed 为看板开单的预填（工具栏入口为空）；需要缴费时账单事实回抛给收费窗。
         只刷看板不刷挂号记录表：新号要先在格子上看见（已挂 N/余 M 跟着变），记录表是翻页查询，等下一次查询自然带上 -->
    <AppointmentsAdd v-model="showAddDialog" :departments="departments" :revisit-source-dict="revisitSourceDict"
                     :seed="addSeed" @saved="loadDesk" @pay="onAddPay"/>

    <!-- 编辑挂号弹窗（schedule 传原号源本体：已收费的号要比「同费用」才让选候选，弹窗自己算不出原价） -->
    <AppointmentsEdit v-model="showEditDialog" :departments="departments" :row="editRow"
                      :schedule="editRow ? (deskScheduleMap[editRow.scheduleId] || null) : null"
                      @saved="reloadListAndStats"/>

    <!-- 门诊诊查费收费：默认现金档（窗口当场点钞记流水），切扫码收款即出一张聚合码（一码多平台） -->
    <QrPayDialog v-model="showPaymentDialog" allow-cash :amount="paymentInfo.amount" :bill-id="paymentInfo.billId"
                 :bill-no="paymentInfo.billNo" :patient-name="paymentInfo.patientName" @paid="onPaid"/>
  </div>
</template>

<script setup>
import {computed, nextTick, onMounted, onUnmounted, ref, watch} from 'vue';
import {
  ArrowLeft,
  ArrowRight,
  CircleCheck,
  CircleClose,
  Clock,
  Edit,
  Plus,
  Refresh,
  Search,
  Timer
} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  cancelRegistration,
  checkInByRegist,
  getAppointStatusCount,
  getBoardRegistList,
  getRegistrationList,
  getScheduleList,
  getScheduleSlotsBatch
} from '@/api/appoint';
import {getBillDetailById} from '@/api/settlementBill';
import {patientAgeText, patientGenderText} from '@/lib/patientGender';
import PatientSelect from '@/components/his/PatientSelect.vue';
import QrPayDialog from '@/components/his/QrPayDialog.vue';
import {getDepartmentSelectList, getDictDataList, getEmployeeList, getUserInfo} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {VISIT_TYPE} from '@/lib/dict';
import {REGIST_TYPE, revisitTypeOf, statusOf} from '@/lib/statusColor';
import {isAllDept, isRegistPaid, settlementLabel, settlementTypes} from '@/lib/appointments';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import AppointmentsAdd from './AppointmentsAdd.vue';
import AppointmentsEdit from './AppointmentsEdit.vue';
import AppointmentsRescheduleEdit from './AppointmentsRescheduleEdit.vue';
import AppointmentsRefundEdit from './AppointmentsRefundEdit.vue';
import AppointmentsCellDetail from './AppointmentsCellDetail.vue';

const getThisWeekRange = () => {
  const now = new Date();
  const day = now.getDay() || 7;
  const monday = new Date(now);
  monday.setDate(now.getDate() - day + 1);
  monday.setHours(0, 0, 0, 0);
  const sunday = new Date(monday);
  sunday.setDate(monday.getDate() + 6);
  sunday.setHours(23, 59, 59, 999);
  const fmt = (d) => {
    const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), dd = String(d.getDate()).padStart(2, '0');
    return `${y}-${m}-${dd}`;
  };
  return [fmt(monday), fmt(sunday)];
};
const fmtDate = (d) => {
  const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), dd = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${dd}`;
};
const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const today = new Date();
      return [fmtDate(today), fmtDate(today)];
    },
  },
  {
    text: '本周',
    value: () => getThisWeekRange(),
  },
  {
    text: '本月',
    value: () => {
      const now = new Date();
      const first = new Date(now.getFullYear(), now.getMonth(), 1);
      const last = new Date(now.getFullYear(), now.getMonth() + 1, 0);
      return [fmtDate(first), fmtDate(last)];
    },
  },
];
const searchForm = ref({
  patientId: null,
  patientName: '',
  deptId: null,
  doctorId: null,
  registStatus: null,
  visitType: null,
  // 挂号员 95% 操作在当日：默认只看今天的就诊
  visitDate: fmtDate(new Date()),
  registDateRange: getThisWeekRange(),
});
const showAddDialog = ref(false);
const showEditDialog = ref(false);
// 拆出的弹框子组件的一次性入参：打开前由本页写好，组件在打开时消费
const addSeed = ref(null);
const editRow = ref(null);
const showPaymentDialog = ref(false);
const paymentInfo = ref({
  registNo: '',
  patientName: '',
  amount: 0,
  registId: null,
  billId: null,
  billNo: '',
});
const loading = ref(false);
const appointments = ref([]);
const departments = ref([]);
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
});
const statusConfig = {
  '已挂号': {
    color: 'bg-emerald-100 text-emerald-700 border-emerald-200',
    icon: CircleCheck
  },
  '已签到': {
    color: 'bg-blue-100 text-blue-700 border-blue-200',
    icon: CircleCheck // 签到使用专门的打卡/用户图标更合适
  },
  '已接诊': {
    color: 'bg-purple-100 text-purple-700 border-purple-200',
    icon: Timer
  },
  '已就诊': {
    color: 'bg-slate-100 text-slate-600 border-slate-200',
    icon: CircleCheck
  },
  '已退号': {
    color: 'bg-red-100 text-red-600 border-red-200',
    icon: CircleClose
  },
  '已过号': {
    color: 'bg-orange-100 text-orange-700 border-orange-200',
    icon: Clock
  },
  '爽约': {
    color: 'bg-rose-100 text-rose-700 border-rose-200',
    icon: Clock
  },
  '未就诊': {
    color: 'bg-slate-100 text-slate-500 border-slate-200',
    icon: Clock
  },
};
const statusMap = {
  '1': '已挂号',
  '2': '已签到',
  '3': '已接诊',
  '4': '已就诊',
  '5': '已退号',
  '6': '已过号',
  '7': '爽约',
  '8': '未就诊',
};
const statusCounts = ref({
  total: 0,
  waiting: 0, // 已挂号(1)，未签到
  checkedIn: 0, // 已签到(2)
  completed: 0, // 已就诊(4)
  cancelled: 0, // 已退号(5)
  overdue: 0, // 已过号(6)
});
const revisitSourceDict = ref([]);
const loadRevisitSourceDict = async () => {
  try {
    const res = await getDictDataList(DICT_TYPE.REVISIT_SOURCE);
    revisitSourceDict.value = res.data || [];
  } catch (error) {
    console.error('加载复诊来源字典失败:', error);
  }
};
/**
 * 复诊来源文案（列表里那一小截灰字）。
 * 命中不到不回落成「复诊」——号别那里已经写了复诊，重复一遍没有信息，
 * 而「字典里查不到的码」恰恰是需要一眼看见的异常（说明有人直接写了库）。
 */
const revisitSourceText = (code) => {
  const hit = revisitSourceDict.value.find((o) => Number(o.dictValue) === Number(code));
  return hit ? hit.dictLabel : `来源${code}`;
};
const handleCheckIn = async (row) => {
  try {
    await checkInByRegist(row.id);
    ElMessage.success('签到成功，患者已进入候诊队列');
    reloadListAndStats();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
const handleCancel = async (row) => {
  // 与后端 cancelRegist 同一口径：只有「已挂号(1)」「已签到(2)」能退。
  // 已接诊(3) 之后诊疗已经发生，退号等于篡改诊疗事实（后端也会拦，这里先给一句人话）。
  if (!canCancelRegist(row)) {
    ElMessage.warning(sourceLockReason(row) || '当前状态不允许退号');
    return;
  }
  try {
    await ElMessageBox.confirm(Number(row.registStatus) === 2
        ? `该患者已签到入队，确定退号？退号后其候诊队列行会一并置为已退号。`
        : '确定要取消该预约吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await cancelRegistration(row.id, '患者主动取消');
    ElMessage.success('取消成功');
    reloadListAndStats();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
const handleEdit = (row) => {
  editRow.value = row;
  showEditDialog.value = true;
};
// ========== 显示支付弹框 ==========
/** 收款取数：金额一律来自账单事实（尚需缴），不由挂号行自带的应收推 */
const buildCollectInfo = async (row) => {
  if (!row.billId) {
    ElMessage.error('未找到关联的账单');
    return null;
  }
  // 查询账单详情获取金额
  let amount = 0;
  try {
    const billRes = await getBillDetailById(row.billId);
    amount = billRes.data?.unpaidAmount ?? billRes.data?.payableAmount ?? 0;
  } catch (e) {
    console.error('获取账单详情失败:', e);
  }
  return {
    registNo: row.registNo || '',
    patientName: row.patientName,
    amount: amount,
    registId: row.id || null,
    billId: row.billId || null,
    billNo: row.billNo || '',
  };
};
const handleShowPayment = async (row) => {
  const info = await buildCollectInfo(row);
  if (!info)
    return;
  paymentInfo.value = info;
  showPaymentDialog.value = true;
};
/** 收款落账（现金与扫码同一路：一笔流水 → 账单状态由后端现算）：入口与角标都跟着账单事实走，看板必须一起刷 */
const onPaid = () => {
  ElMessage.success('门诊诊查费已入账');
  reloadListAndStats();
  loadDesk();
};
/** 新增挂号成功且需缴费：账单事实由子组件回抛，收款弹窗（共享组件）留在本页 */
const onAddPay = (info) => {
  paymentInfo.value = info;
  showPaymentDialog.value = true;
};

const showRefundDialog = ref(false);
// 打开退费弹窗前由本页查好的账单事实（可退金额等），组件在打开时消费
const refundInitial = ref({});
const handleApplyRefund = async (row) => {
  if (!canApplyRefund(row)) {
    ElMessage.warning('该挂号不满足申请退费条件（需就诊日已过、已收费且人未到诊）');
    return;
  }
  let amount = 0;
  try {
    const billRes = await getBillDetailById(row.billId);
    const bill = billRes.data || {};
    const paid = Number(bill.paidAmount ?? 0);
    // 上限是「还能退多少」而不是「已收」：退过一部分的账单按净额给上限，提交时会被服务端打回，
    // 窗口只会觉得系统抽风（真正拦它的那句「不能超过可退金额」看着像前后矛盾）。
    const refunded = Number(bill.refundAmount ?? 0);
    amount = paid - refunded;
  } catch (error) {
    console.error('获取收费单详情失败:', error);
  }
  // 金额拿不到就不开弹框：一个「实付 ¥0、上限 0」的退费表单只会让人以为系统坏了
  if (!(amount > 0)) {
    ElMessage.error('未能取到该挂号的可退金额（可能已经退过），请稍后重试或直接到医院收费处办理');
    return;
  }
  refundInitial.value = {
    patientName: row.patientName || '',
    registNo: row.registNo || '',
    patientId: row.patientId,
    patientNo: row.patientNo || '',
    billId: row.billId,
    billNo: row.billNo || '',
    refundableAmount: amount,
  };
  showRefundDialog.value = true;
};
const handleSearch = () => {
  pagination.value.pageNum = 1;
  // 统计范围跟着筛选条件走 → 必须一起刷，否则「筛选后列表 3 条、卡片还是全院总数」
  reloadListAndStats();
};
const handleReset = () => {
  searchForm.value = {
    patientId: null,
    patientName: '',
    deptId: null,
    doctorId: null,
    registStatus: null,
    visitType: null,
    visitDate: fmtDate(new Date()),
    registDateRange: getThisWeekRange(),
  };
  handleSearch();
};
// 翻页/改每页大小只影响列表本身，统计范围（筛选条件）没变 → 不必重算统计
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  pagination.value.pageNum = 1;
  loadData();
};
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val;
  loadData();
};
const buildQueryParams = (withPaging = true) => {
  const params = {};
  // 本页是门诊预约管理，只管普通号/专家号；急诊直录单不经本页签到/退号，号别口径由后端按参数过滤
  params.registTypes = '1,2';
  if (withPaging) {
    params.pageNum = pagination.value.pageNum;
    params.pageSize = pagination.value.pageSize;
  }
  if (searchForm.value.patientId)
    params.patientId = searchForm.value.patientId;
  if (searchForm.value.deptId)
    params.deptId = searchForm.value.deptId;
  if (searchForm.value.registStatus != null)
    params.registStatus = searchForm.value.registStatus;
  if (searchForm.value.visitDate)
    params.visitDate = searchForm.value.visitDate;
  if (searchForm.value.registDateRange?.[0])
    params.beginTime = searchForm.value.registDateRange[0] + ' 00:00:00';
  if (searchForm.value.registDateRange?.[1])
    params.endTime = searchForm.value.registDateRange[1] + ' 23:59:59';
  return params;
};
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getRegistrationList(buildQueryParams());
    let records = res.data?.records || [];
    // visitType 不在数据库字段中，前端过滤
    if (searchForm.value.visitType) {
      records = records.filter((r) => r.visitType === searchForm.value.visitType);
    }
    appointments.value = records;
    pagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载预约列表失败:', error);
  } finally {
    loading.value = false;
  }
};
const loadStatusCounts = async () => {
  try {
    const statParams = buildQueryParams(false);
    delete statParams.beginTime;
    delete statParams.endTime;
    const res = await getAppointStatusCount(statParams);
    const d = res.data || {};
    statusCounts.value = {
      total: d.total || 0,
      waiting: d.waiting || 0,
      checkedIn: d.checkedIn || 0,
      completed: d.completed || 0,
      cancelled: d.refunded || 0,
      overdue: d.overdue || 0,
    };
    await nextTick();
    calcDeskHeight();
  } catch (error) {
    console.error('加载统计失败:', error);
  }
};
/** 列表与统计一起刷新 —— 凡是会改变统计范围的操作都走这里，避免只刷一半 */
const reloadListAndStats = () => {
  loadData();
  loadStatusCounts();
};
const loadDepartments = async () => {
  try {
    // 不传 scope → 默认按当前人过滤：挂号员只看到被授权的挂号科室。
    // 门诊部 / 管理员这类不限权用户天然拿到全部科室，不需要在这里做角色判断。
    const res = await getDepartmentSelectList({deptType: 1});
    departments.value = res.data || [];
  } catch (error) {
    console.error('加载科室列表失败:', error);
  }
};
const myDeptId = ref(null);
const applyDefaultDept = (deptId) => {
  const hit = departments.value.find(d => String(d.id) === String(deptId));
  if (!hit)
    return;
  myDeptId.value = hit.id;
  if (deskDeptId.value == null)
    deskDeptId.value = hit.id;
};
const loadDefaultDept = async () => {
  try {
    const res = await getUserInfo();
    applyDefaultDept(res.data?.deptId);
  } catch (error) {
    // 取不到就保持「全部科室」，不阻断看板（默认值不该成为故障点）
    console.error('加载当前用户科室失败，看板默认「全部科室」:', error);
  }
};
const activeTab = ref('desk');
/** 弹框里的「格子」：周视图下 = 某医生 × 某天（2026-09-22 起周视图的行就是医生，不再是班次） */
const selectedCell = ref(null);
/** 周视图格子的「医生号源明细」弹框（原来是一张内联表，压在表格下面把看板挤出屏幕） */
const showCellDialog = ref(false);
// 弹框跟着「选中的格子」活：切视图 / 翻周 / 下钻都会清掉 selectedCell，
// 那时弹框必须一起关 —— 否则屏上留着一个指向已不存在的格子的窗（切去日视图还在）。
watch(selectedCell, v => {
  if (!v)
    showCellDialog.value = false;
});
/**
 * 周视图「隐藏已满/停诊」开关。
 *
 * 原「号源面板」tab 的全部内容 = 打开这个开关的周视图：两者是同一张表
 * （`biz_schedule`）、同 24 个字段、同「班次 × 日期」维度，只差 `available_source > 0`
 * 这一个过滤条件（实测本周 66 个班次里只有 3 个已满，差异率 4.5%）。
 * 4.5% 的差异不配拥有一个 tab —— 它是个 filter。
 *
 * 默认**关**：挂号员更关心「这天整体什么样」，已满也是信息（满了才知道要改推荐别的天）。
 * 打开后才是「只看能挂的号」——这就是原来切到号源面板的人才需要的东西。
 */
const deskHideFull = ref(false);
// ========== 左侧 rail：迷你月历（纯日期导航） ==========
/**
 * 月历当前显示的月份（`YYYY-MM-01`）。与 `deskDay` **解耦** ——
 * 用户可以在月历上翻到 10 月看有没有排班，而右侧仍停在 9-21；点某天才会把两侧对齐。
 * 若把两者绑成同一个值，"翻月看未来"这个动作就会把右侧也翻走，等于不能纯浏览。
 */
const calMonth = ref(fmtDate(new Date()).slice(0, 8) + '01');
/**
 * 月历格子上的「有排班」圆点数据源。
 *
 * ⚠️ 为什么用**整月一次区间查询**，而不是按天查 31 次：
 * 排班表 `biz_schedule` 全库仅 74 条 / 覆盖 14 天（实测），整月一次拿回来比周视图（66 条）还小；
 * 而按天 31 次明细查询正是本页刚修掉的「首屏 7 请求」的 4.4 倍放大版。
 * 一次 `startDate/endDate` 区间查询就能覆盖圆点所需的全部信息，没有第二个选择。
 *
 * ⚠️ 只存「哪天有排班」这个事实，**不存每天几条、更不显示「N 个号」**：
 * 显示数量会诱导后续实现去算"某天还剩多少号"，那就必须逐日聚合（见上一条）。
 * 圆点回答的是"这天开不开诊"，不是"这天好不好挂"——后者请点进去看日视图。
 */
const calServiceDays = ref(new Set());
const calLoading = ref(false);
const calMonthLabel = computed(() => {
  const [y, m] = calMonth.value.split('-');
  return `${y} 年 ${Number(m)} 月`;
});
/** 月历 42 格（6 周 × 7 天，周一起）—— 固定 6 行，翻月时高度不跳 */
const calCells = computed(() => {
  const first = new Date(calMonth.value + 'T00:00:00');
  const firstDow = first.getDay() || 7; // 周一=1 … 周日=7
  const start = new Date(first);
  start.setDate(first.getDate() - (firstDow - 1));
  const mon = calMonth.value.slice(0, 7);
  const cells = [];
  for (let i = 0; i < 42; i++) {
    const d = new Date(start);
    d.setDate(start.getDate() + i);
    const ds = fmtDate(d);
    cells.push({
      date: ds,
      day: d.getDate(),
      inMonth: ds.slice(0, 7) === mon,
      hasService: calServiceDays.value.has(ds),
      isToday: ds === fmtDate(new Date()),
    });
  }
  return cells;
});
/**
 * 月历取数 = 当前月份的全部排班（一次区间查询）。
 *
 * 科室筛选**跟随主区**：主区看呼吸内科时，月历圆点也只标呼吸内科开诊的日子，
 * 否则圆点点出来一堆、点进去却是空的（"有排班"与"我能看的排班"必须同一个口径）。
 */
const loadCalServiceDays = async () => {
  calLoading.value = true;
  try {
    const start = calMonth.value;
    const end = fmtDate(new Date(new Date(calMonth.value + 'T00:00:00').getFullYear(), new Date(calMonth.value + 'T00:00:00').getMonth() + 1, 0));
    const params = {startDate: start, endDate: end};
    if (!isAllDept(deskDeptId.value))
      params.deptId = deskDeptId.value;
    if (deskDoctorId.value)
      params.doctorId = deskDoctorId.value;
    const res = await getScheduleList(params);
    const scheds = (res.data?.records || res.data || []);
    calServiceDays.value = new Set(scheds.map((s) => String(s.scheduleDate || '').slice(0, 10)));
  } catch (error) {
    // 圆点取不到就退化成"没有圆点"，不阻断月历导航这个主功能
    console.error('加载月历排班标记失败:', error);
  } finally {
    calLoading.value = false;
  }
};
/**
 * 月历翻月。
 *
 * ⚠️ 只改月历自己的月份，**不动右侧 deskDay** —— 见 `calMonth` 的注释。
 * 展开月历浏览未来排班时不该把工作区带着一起跳。
 */
const handleCalShift = (delta) => {
  const d = new Date(calMonth.value + 'T00:00:00');
  d.setMonth(d.getMonth() + delta);
  calMonth.value = fmtDate(new Date(d.getFullYear(), d.getMonth(), 1));
  loadCalServiceDays();
};
/** 月历「今天」：把月历翻回本月（右侧是否跟过去由调用处决定） */
const handleCalToday = () => {
  calMonth.value = fmtDate(new Date()).slice(0, 8) + '01';
  loadCalServiceDays();
};
/**
 * 点月历某天 → 右侧切到该天（日视图）。
 *
 * 若该天不在当前月历显示的月份，顺手把月历也翻过去（否则跨月的日期没有对应的月历格子高亮）。
 */
const handleCalPick = (date) => {
  if (date.slice(0, 7) !== calMonth.value.slice(0, 7)) {
    calMonth.value = date.slice(0, 8) + '01';
    loadCalServiceDays();
  }
  deskDay.value = date;
  deskViewMode.value = 'day';
  deskWeekOffset.value = 0;
  selectedCell.value = null;
  loadDesk();
};
// 2026-09-22：左栏「我的科室」快捷块按需求移除，切科室统一走上方「科室」下拉。
const weekdayMap = {0: '周日', 1: '周一', 2: '周二', 3: '周三', 4: '周四', 5: '周五', 6: '周六'};
const mondayOf = (offset) => {
  const now = new Date();
  const day = now.getDay() || 7;
  const monday = new Date(now);
  monday.setDate(now.getDate() - day + 1 + offset * 7);
  return monday;
};
const deskViewMode = ref('day');
const deskDay = ref(fmtDate(new Date()));
const deskWeekOffset = ref(0);
const deskDeptId = ref(null);
const deskDoctorId = ref(null);
const deskLoading = ref(false);
const deskDays = ref([]); // 当前窗口的日期：日视图 1 天 / 周视图 7 天
const deskRegs = ref({}); // 就诊日 -> 挂号
const deskSchedules = ref([]); // 当前窗口排班（带 totalSource / availableSource，余号直接用它）
const deskScheduledDoctors = ref([]); // 当前窗口「已排班医生」集合 —— 后端 /system/employee/selectList?onlyScheduled=true 过滤结果，前端不再自算
const deskScheduleMap = ref({});
/**
 * 当前窗口的**时间片段**（biz_schedule_slot，半小时一档）。
 *
 * 日视图的 Y 轴已经细到半小时，「某医生某半小时还剩几个号」这个数字的**唯一真值在段上**
 * （段之间号源并不等分，实测 20 个号切 8 段 = 3,3,3,3,2,2,2,2 —— 用主表 available 除以段数去摊
 * 出来的每一行数字都是假的）。所以日视图必须拉段，不能用排班主表凑。
 *
 * 只服务日视图：周视图的行仍是「班次」，用主表合计即可，不拉（省一次请求）。
 */
const deskSlots = ref([]);
/**
 * 周视图每格最多铺几张卡。
 *
 * 一个科室 4 位医生 × 每人 20 个号 = 单日 80 张卡，一周 560 张 —— 全铺出来不只是"不好看"，
 * DOM 会把浏览器拖死。真实 HIS 也是每格铺一屏内能看完的条数 + 「还有 N 人 → 看当天」。
 *
 * 阈值定 10：一格一天里 上午/下午/全天 三个班次合计 30 条是能看下来的（老王实测板幅够大），
 * 所以按「单个班次 10 条」折叠 —— 从第 11 个人才开始收，不是第 4 个。
 * 日视图单个医生单班次最多 20 张，仍然全铺。
 */
const DESK_WEEK_CELL_MAX = 10;
const deskWeekLabel = computed(() => deskDays.value.length ? `${deskDays.value[0]} ~ ${deskDays.value[6]}` : '');
/** 工具栏中间的日期文案：日视图是「2026-09-21 周一」，周视图是「上周一 ~ 本周日」 */
const deskRangeLabel = computed(() => deskViewMode.value === 'day'
    ? `${deskDay.value} ${weekdayMap[new Date(deskDay.value + 'T00:00:00').getDay()]}`
    : deskWeekLabel.value);
/**
 * 医生排序：**按 doctor_id 升序**。
 *
 * 2026-09-22：试过拼音序（`Intl.Collator('zh-Hans-CN')`），最后改回 id ——
 * 这份库里的医生名是"医生甲 / 压测医生丙 / 权限验证探针医生"这类**没法按姓氏查**的名字，
 * 拼音序排出来人是找不到的；而 id 顺序**永远稳定**（同一个 id 永远在同一列/同一行），
 * 挂号员熟了之后记的是位置而不是名字 —— 位置漂移比"顺序不好看"难受得多。
 *
 * 前后端同口径：后端 `/schedule/list` 与 `/system/employee/selectList` 也都按 doctor_id / id 排，
 * 所以接口喂过来的顺序和这里算出来的一致，不存在"两个地方各排一次、结果不一样"。
 *
 * ⚠️ 必须转**数值**再比：doctor_id 是 BIGINT，按字符串比会出 "10" < "9" 的字典序倒挂；
 *    超过 2^53 的雪花 id 用 Number 也保不住精度，所以走 BigInt。
 */
const byDoctorId = (a, b) => {
  const x = BigInt(String(a?.id ?? a?.doctorId ?? 0));
  const y = BigInt(String(b?.id ?? b?.doctorId ?? 0));
  return x < y ? -1 : x > y ? 1 : 0;
};
/**
 * 医生下拉选项 = 当前窗口「已排班」的医生，**直接取后端过滤结果**（`selectList?onlyScheduled=true`）。
 *
 * 身份判定收口在后端：不传任何联动参数，下拉与看板行天然同口径（同一份集合），
 * 不会出现「下拉里有人、看板上没他」的错位。按拼音序排：下拉里十几个人混着排的时候，找到目标全靠肉眼过滤。
 */
const deskDoctorOptions = computed(() => {
  return deskScheduledDoctors.value.map(e => ({
    id: e.id,
    doctorName: e.empName || '未定医生',
    deptName: e.deptName,
  })).sort(byDoctorId);
});
/** 当前窗口的日期列表：日视图 1 天，周视图 7 天（周一起） */
const deskDateList = () => {
  if (deskViewMode.value === 'day')
    return [deskDay.value];
  const monday = mondayOf(deskWeekOffset.value);
  const days = [];
  for (let i = 0; i < 7; i++) {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    days.push(fmtDate(d));
  }
  return days;
};
/**
 * 看板取数 —— **整段一次查全**，不按天拆、不翻页。
 *
 * 排班与挂号走同一个日期区间：排班本来一次就查整段（`/schedule/list` 带 startDate/endDate），
 * 挂号以前却按天拆成 7 路、每路各自翻页（`pageSize=200`，单日超 200 条就再来一页），
 * 周视图一次开 7 个请求、数据多时是 7×页数 —— 而结果和一次区间查询完全一样。
 *
 * 为什么挂号必须整段全量：每格要显示「已挂 N / 总号源」和「还有 M 人 → 看当天」，
 * 拿到某一页根本没法算。后端 `/appoint/boardList` 因此刻意不分页。
 *
 * 也不再把 7 天的结果分别 fill 回去：一次拿回整段、一个循环归位，
 * 不存在「7 个请求里有一个失败 → 那一天静默变空」这种半死不活的中间态。
 */
const loadDesk = async () => {
  deskLoading.value = true;
  try {
    const days = deskDateList();
    deskDays.value = days;
    const params = {startDate: days[0], endDate: days[days.length - 1], registTypes: '1,2'};
    if (!isAllDept(deskDeptId.value))
      params.deptId = deskDeptId.value;
    if (deskDoctorId.value)
      params.doctorId = deskDoctorId.value;
    // 排班 + 挂号 + 已排班医生集合并行：集合也走后端 onlyScheduled 过滤（身份判定收口在后端），共 3 个请求
    const [schedRes, regRes, doctorRes] = await Promise.all([
      getScheduleList(params),
      getBoardRegistList(params),
      getEmployeeList({onlyScheduled: true, startDate: params.startDate, endDate: params.endDate, deptId: params.deptId}),
    ]);
    const scheds = schedRes.data?.records || schedRes.data || [];
    deskSchedules.value = scheds;
    deskScheduledDoctors.value = doctorRes.data || [];
    // 行/下拉都只认「当前窗口有排班的医生」（后端集合判定）：翻周/换天后选中的医生若该窗口无排班，行会消失，
    // 必须自动清掉，否则周视图空板、下拉顶着一个不存在的选中值
    if (deskDoctorId.value && !deskScheduledDoctors.value.some(e => String(e.id) === String(deskDoctorId.value))) {
      deskDoctorId.value = null;
    }
    const map = {};
    scheds.forEach((s) => {
      map[s.id] = s;
    });
    deskScheduleMap.value = map;
    // 按就诊日归位：口径是 visit_date（就诊日），与「今日队列」一致，不是到院日
    const regs = {};
    days.forEach(d => {
      regs[d] = [];
    });
    for (const r of (regRes.data || [])) {
      const d = String(r?.visitDate || '').slice(0, 10);
      if (regs[d])
        regs[d].push(r);
    }
    deskRegs.value = regs;
    await loadDeskSlots(scheds);
    // 整轴 48 行：渲染完立刻把视口挪到当天第一档，别让人从 0 点往下翻
    await nextTick();
    scrollToFirstServiceRow();
  } catch (error) {
    console.error('加载预约看板失败:', error);
  } finally {
    deskLoading.value = false;
  }
};
/**
 * 拉当前窗口排班的**时间片段**（批量一次，不逐条打请求）。
 *
 * 失败不抛、只清空：段只影响「格子里那几个数字」的精度，
 * 排班/挂号本身已经画出来了，不能因为一次段查询失败把整屏弄没。
 * 空数组会让日视图那几格显示「无段数据」，是可见的降级，不是静默丢数。
 */
const loadDeskSlots = async (scheds) => {
  if (deskViewMode.value !== 'day') {
    deskSlots.value = [];
    return;
  }
  const ids = (scheds || []).map((s) => s.id).filter(Boolean);
  if (!ids.length) {
    deskSlots.value = [];
    return;
  }
  try {
    const res = await getScheduleSlotsBatch(ids);
    deskSlots.value = res.data || [];
  } catch (error) {
    console.error('加载时间片段失败:', error);
    deskSlots.value = [];
  }
};
// ========== 看板高度：撑满当前窗口 ==========
//
// Y 轴是全天 48 档，写死 620px 等于让人一路滚。所以按视口算高度：容器有多高就是多高，
// 超出的部分在**容器内部**滚（表头吸顶），主区那条页面级滚动条由下面的二次校正压掉。
//
// ⚠️ 上限必须夹住：页面向下滚之后容器 rect.top 会变负，直接拿它算会得到比窗口更高的表
//    （越往下滚表越高，永远滚不到底），所以上界锁 innerHeight - 120。
const deskScrollEl = ref(null);
const deskMaxH = ref(620);
/**
 * 往上找真正滚动的那个祖先（DashboardLayout 里是 flex-1 overflow-auto 的主区）。
 *
 * 注意从 **parentElement** 开始，跳过 `deskScrollEl` 自己 —— 它也是 overflow-auto，
 * 但它那条滚动条是看板内容滚动（48 行时间轴的正常行为），不是页面溢出。
 */
const findScrollParent = (el) => {
  let p = el?.parentElement || null;
  while (p) {
    const oy = getComputedStyle(p).overflowY;
    if ((oy === 'auto' || oy === 'scroll') && p.scrollHeight > p.clientHeight + 1)
      return p;
    p = p.parentElement;
  }
  return null;
};
const calcDeskHeight = () => {
  const el = deskScrollEl.value;
  if (!el)
    return;
  const top = el.getBoundingClientRect().top;
  const h = window.innerHeight - top - 16;
  deskMaxH.value = Math.max(320, Math.min(h, window.innerHeight - 120));
  // 二次校正：容器的上下都还有东西是 `innerHeight - top` 算不到的 ——
  //   上：工具栏内容随视图/数据折行时容器会下移；
  //   下：main 的 padding-bottom(24) + 卡片 p-4(16) + 卡片外层的 space-y-6(24)。
  // 这几项加起来几十 px，硬编码等于换个页面就错 —— 所以按主区的**真实溢出**逐帧收敛。
  // 最多收 3 次：够。（左侧日历列已用同一个 deskMaxH 封顶，每次收缩都会真的减少行高，
  // 不会出现「越收溢出还在」的假收敛 —— 那正是只封桌子的旧版本踩的坑）
  const settle = (times) => requestAnimationFrame(() => {
    const sc = findScrollParent(el);
    if (!sc || times <= 0)
      return;
    const over = sc.scrollHeight - sc.clientHeight;
    if (over <= 0)
      return;
    deskMaxH.value = Math.max(320, deskMaxH.value - over);
    settle(times - 1);
  });
  settle(3);
};
/**
 * 数据到位后把视口落到当天**第一个有排班的档**。
 *
 * 刻度压缩之后，第一行本来就是早班第一档（scrollTop ≈ 0）；这里留着是因为压缩只按
 * 「有没有排班」判，筛掉号源已满的档之后首行仍可能不是第一档 —— 逻辑不变，只是通常不滚。
 */
const scrollToFirstServiceRow = () => {
  const el = deskScrollEl.value;
  if (!el || deskViewMode.value !== 'day')
    return;
  let lo = Infinity;
  for (const s of deskSchedules.value) {
    if (s.scheduleDate !== deskDay.value)
      continue;
    const a = toMin(s.startTime);
    if (a != null)
      lo = Math.min(lo, a);
  }
  if (!Number.isFinite(lo)) {
    el.scrollTop = 0;
    return;
  }
  const key = fmtMin(Math.floor(lo / 30) * 30);
  const td = el.querySelector(`tbody td[data-row="${key}"]`);
  const tr = td?.parentElement;
  if (!tr) {
    el.scrollTop = 0;
    return;
  }
  const thead = el.querySelector('thead');
  const headH = thead ? thead.getBoundingClientRect().height : 0;
  el.scrollTop = Math.max(0, tr.getBoundingClientRect().top - el.getBoundingClientRect().top + el.scrollTop - headH - 8);
};
const onWinResize = () => calcDeskHeight();
// ---- 格子取数：两个视图共用同一组函数，只有"按天+班次"还是"按医生+班次"的差别 ----
/** 余号合计（余号口径 = bis_schedule.available_source，与号源面板一致） */
const deskAvail = (schedules) => schedules.reduce((sum, s) => sum + (Number(s.availableSource) || 0), 0);
/** 总号源合计 */
const deskTotal = (schedules) => schedules.reduce((sum, s) => sum + (Number(s.totalSource) || 0), 0);
/** 某组排班下的在板挂号：**含已退号**（2026-10-08 起退号是否上板由左侧状态勾选框决定，
 *  数据层不再硬滤；「已挂 N」这类计数口径仍排除退号，见 deskRegCount） */
const deskRegsOfSchedules = (date, schedules) => {
  if (!schedules.length)
    return [];
  const ids = new Set(schedules.map(s => s.id));
  return (deskRegs.value[date] || []).filter(r => ids.has(r.scheduleId));
};
/** 计数口径：已挂 N 不算退号（号已经还回号池，把退号数进"已挂"会和余号对不上账） */
const deskRegCount = (regs) => regs.filter((r) => Number(r.registStatus) !== 5).length;
/**
 * 周视图格子 = **某医生 × 某天**（跨当天全部班次合计）。
 *
 * 原来这一格是「某天 × 某班次 × 全院医生」：一行医生当格子内容铺不下，
 * 医生维度的问句（"张三这周还剩多少号"）根本问不出来 —— 现在它就是行，直接读一行即可。
 */
const weekCellSchedules = (date, doctorId) => deskSchedules.value.filter(s => s.scheduleDate === date && String(s.doctorId) === String(doctorId));
const weekCellRegs = (date, doctorId) => deskRegsOfSchedules(date, weekCellSchedules(date, doctorId));
/** 日视图某医生当天的排班（按段定位用，不限于某班次） */
const dayDoctorSchedsOf = (doctorId) => deskSchedules.value.filter(s => s.scheduleDate === deskDay.value && s.doctorId === doctorId);
/** 日视图格子：某医生 × 某半小时段 —— 覆盖这一段的排班（开单时要的是排班，不是段） */
const daySlotCellSchedules = (doctorId, slotStart) => {
  const m = toMin(slotStart);
  if (m == null)
    return [];
  return dayDoctorSchedsOf(doctorId).filter(s => {
    const a = toMin(s.startTime);
    const b = toMin(s.endTime);
    return a != null && b != null && a <= m && m < b;
  });
};
/**
 * 日视图格子的「号源数字来源」——**段投影**。
 *
 * 有段 → 用真段（段之间号源不等分，只有段上的数字是真的）。
 * 无段（历史排班，号源只在主表）→ 整条排班的号源记在它窗口的**第一行**，其余行不记：
 * 否则同一条排班的 20 个号会被它覆盖的 8 行各算一遍，格子加总 160 对不上列头的 20。
 */
const daySlotSegments = (scheds, slotStart) => {
  const ids = new Set(scheds.map((s) => String(s.id)));
  const segs = deskSlots.value.filter((s) => ids.has(String(s.scheduleId)) && String(s.startTime).slice(0, 5) === slotStart);
  if (segs.length)
    return segs;
  const m = toMin(slotStart);
  return scheds.filter((s) => {
    const a = toMin(s.startTime);
    return firstSlotStartOf(s.id) == null && a != null && Math.floor(a / 30) * 30 === m;
  });
};
/**
 * 日视图格子里的人：某医生 × 某半小时段的挂号。
 *
 * 挂号自带段快照（`slotStart`/`slotEnd`），直接按它归位 —— 不靠排班窗口去猜。
 * 没有段快照的挂号（改段功能上线前的历史数据，或任何没传 slotId 的入口）：
 * 统一收到所属排班的第一段展示 —— 数据不丢；不再标「未选段」（2026-10-08 老王：看不懂这标签，
 * 它是写给开发看的内部口径，不是给挂号员看的信息）。
 */
const daySlotCellRegs = (doctorId, slotStart) => {
  const date = deskDay.value;
  const ids = new Set(dayDoctorSchedsOf(doctorId).map((s) => s.id));
  const all = (deskRegs.value[date] || []).filter((r) => ids.has(r.scheduleId));
  const hit = [];
  const fallback = [];
  for (const r of all) {
    const head = String(r.slotStart || '').slice(0, 5);
    if (head === slotStart) {
      hit.push(r);
      // 无快照的挂号：段表的第一段优先，段表查不到再退回排班窗口首档（见 fallbackSlotStartOf）。
      // 两档都兜不住才真的丢 —— 那种情况下宁可也让它在首档出现，也不要静默不见。
    } else if (!head && (fallbackSlotStartOf(r.scheduleId) ?? null) === slotStart) {
      fallback.push(r);
    }
  }
  return [...hit, ...fallback];
};
/** 日视图格子：某医生 × 某班次（当天）—— 周视图的「隐藏已满」总账仍按班次口径算，保留 */
const dayCellSchedules = (doctorId, type) => deskSchedules.value.filter(s => s.doctorId === doctorId && Number(s.scheduleType) === type);
const dayCellRegs = (doctorId, type) => deskRegsOfSchedules(deskDay.value, dayCellSchedules(doctorId, type));
/**
 * 医生身份 = 后端 `selectList?onlyScheduled=true` 的过滤结果（`deskScheduledDoctors`），
 * 前端只做字段装配（映射 + 排序），不做"谁算已排班"的判定。
 *
 * 日视图用它按**当天**（day 模式窗口 = 当天）出医生列，周视图按**整周窗口**出医生行 ——
 * 同一份后端集合，换周/换天不会出现"这列多了个人"。
 * `roomName`（诊室）是排班侧属性，员工接口没有：从已加载排班补装配，身份判定不依赖它。
 */
const toDoctorView = (emp) => ({
  id: emp.id,
  doctorId: emp.id,
  doctorName: emp.empName || '未定医生',
  deptId: emp.deptId,
  deptName: emp.deptName,
  roomName: deskSchedules.value.find(s => String(s.doctorId) === String(emp.id))?.roomName,
});
/** 日视图的医生列：当天有排班的医生（后端过滤，day 窗口 = 当天），同一医生多诊室多班次合成一列，按拼音序排 */
const dayDoctors = computed(() => deskScheduledDoctors.value.map(toDoctorView).sort(byDoctorId));
/**
 * 周视图的行 = **医生**（2026-09-22：原为班次，行是半天粒度、格里塞满患者卡片，
 * 回答不了"某医生这周排没排、还剩多少"）。
 *
 * 行 = 当前窗口「已排班的医生」，**由后端 `selectList?onlyScheduled=true` 过滤**（2026-10-14 起不再前端投影、不再并名册）：
 *   挂号看板只回答"现在能挂谁的号"，整周无排班的医生一行七格「-」是纯噪音；
 *   "谁这周还没排班"是排班管理页的职责（那页有应排/实排统计，仍按全量名册出行）。
 *   外院/多点执业医生天然在排班数据里，不需要名册兜底。
 * 选中了具体医生时只留他一行：周视图此时就是"这一位医生的一周"，别人的行是噪声。
 * 同日视图一样按拼音序排 —— 两边的行/列顺序打架，翻一次视图就得重新找人。
 */
const weekDoctorRows = computed(() => {
  if (deskViewMode.value !== 'week')
    return [];
  let list = deskScheduledDoctors.value.map(e => ({
    key: String(e.id),
    doctorId: e.id,
    label: e.empName || '未定医生',
    deptName: e.deptName,
  }));
  if (deskDoctorId.value) {
    list = list.filter(d => String(d.doctorId) === String(deskDoctorId.value));
  }
  // 行内的统计（已挂 / 剩余）在下面 weekRowStat 里按 `deskSchedules` 现算，不在这里缓存：
  // 挂号/退号之后 deskRegs 变了，行要有反应 —— 缓存一份就等于让行停在上一刻。
  // 行序与日视图的列序用同一个比较器（byDoctorId），翻视图时位置不漂移。
  return list.sort(byDoctorId);
});
/**
 * 周视图行（某医生）在**当前这一周**的合计。
 *
 * 「这个医生有了多少个挂号患者、还剩余多少」在周视图里要有两个尺度：
 * 格子（某天）由 `deskCell` 给，整周合计由这里给 —— 后者直接写在行首，扫一行就知道这人这周忙不忙。
 *
 * 口径与格子严格同源：`deskTotal` / `deskAvail` / `deskRegsOfSchedules`（已退号不计数），
 * 不另算一套 —— 否则行首说 20、七格加起来是 18，同一屏两个数字打架。
 */
const weekRowStat = (doctorId) => {
  const scheds = deskSchedules.value.filter(s => String(s.doctorId) === String(doctorId));
  let regs = 0;
  if (scheds.length) {
    for (const d of deskDays.value) {
      // 计数排除退号：deskRegsOfSchedules 现在返回全量（退号上不上板由勾选框管），
      // 但"已挂 N"是容量数字，必须与余号对账
      regs += deskRegCount(deskRegsOfSchedules(d, scheds.filter(s => s.scheduleDate === d)));
    }
  }
  return {total: deskTotal(scheds), avail: deskAvail(scheds), regs};
};
// ========== 日视图 Y 轴：全天时间刻度（00:00 ~ 24:00，半小时一档）+ 刻度压缩 ==========
//
// 为什么是**固定刻度**（0~24 点整轴）而不是"当天有排班的时间窗并集"：
// 挂号员在板上找的是"9 点那档还有没有号"，时间轴本身必须是稳定参照系 ——
// 轴跟着排班伸缩的话，同一行换一天就是另一个时刻，每天都要重新目视对齐
// （这正是上一个版本的问题：轴长随排班变，行首还错着位）。
// 真实 HIS / 日历类排班控件都是 0~24 点整轴 + 内部滚动，这里照做。
//
// 为什么行是半小时而不是「上午 / 下午」：号源的事实落在半小时段上（biz_schedule_slot），
// 而且段之间**并不等分**（实测 20 个号切 8 段 = 3,3,3,3,2,2,2,2）。
// 用「上午」一行来显示，等于把 8 个不同余号的段压成一个数 —— 回答不了"9 点那档还有没有号"，
// 而这正是分时段预约挂号要回答的问题。
//
// 整轴 48 行 ≠ 48 行都画：没人排班的档**压缩掉**（见 daySlotRows），
// 压缩后一天通常只剩十几行，一屏看得全，轴内部的滚动不再是常态。
/**
 * "HH:mm" / "HH:mm:ss" → 当天分钟数。解析不了返回 null —— 别让 NaN 混进大小比较（NaN 比较恒 false，会静默吞行）
 *
 * ⚠️ 秒必须可选：实测后端既回 `08:00` 也回 `08:00:00`（看板压测夹具就是带秒的）。
 *    写成严格 `$` 结尾时，`08:00:00` 解析成 null → 这一条排班被 `continue` 掉，
 *    后果是**这一档整行消失、这位医生在日视图连列都没有**，且不报任何错。
 */
const toMin = (hhmm) => {
  const m = /^(\d{1,2}):(\d{2})(?::(\d{2}))?$/.exec(String(hhmm ?? '').trim());
  return m ? Number(m[1]) * 60 + Number(m[2]) : null;
};
const fmtMin = (m) => `${String(Math.floor(m / 60)).padStart(2, '0')}:${String(m % 60).padStart(2, '0')}`;
/**
 * 时段分组（只用于行首的分组标签，不参与任何取数）。
 * 四档与 `biz_schedule.schedule_type` 对齐：4凌晨 / 1上午 / 2下午 / 晚上。
 */
const slotGroupOf = (m) => m < 8 * 60 ? '凌晨' : m < 12 * 60 ? '上午' : m < 18 * 60 ? '下午' : '晚上';
/** 某排班的段起点（无段返回 null）。**只用于判断"这条排班有没有段数据"**，别拿它给人归位 */
const firstSlotStartOf = (scheduleId) => {
  const hit = deskSlots.value.find((s) => String(s.scheduleId) === String(scheduleId));
  return hit ? String(hit.startTime).slice(0, 5) : null;
};
/**
 * 没有段快照的挂号该落在哪一档。
 *
 * ⚠️ 不能复用 `firstSlotStartOf`：那个函数对"没段数据的排班"返回 null，
 *    于是**历史排班（号源只在主表，biz_schedule_slot 里根本没有它的段）上的挂号
 *    会一档都落不上 —— 整批人在日视图里凭空消失**。
 *    实测压测科室 94 条挂号全挂在无段的排班上，改半小时档之后页面卡片数一度是 0，
 *    而同一屏的号源数字还写着"已挂 0 / 40 余 22"（used=18）—— 数字说用了 18 个、
 *    名单一张都没有，这种"静默丢人"是挂号窗口最不能接受的一类错误。
 *
 * 所以这里分两级兜底：段表的第一段 → 排班窗口的第一档（`start_time` 向下对齐到半点）。
 */
const fallbackSlotStartOf = (scheduleId) => {
  const seg = firstSlotStartOf(scheduleId);
  if (seg)
    return seg;
  const s = deskSchedules.value.find((x) => String(x.id) === String(scheduleId));
  const a = toMin(s?.startTime);
  return a == null ? null : fmtMin(Math.floor(a / 30) * 30);
};
/**
 * 日视图 Y 轴：全天 48 档 `00:00~00:30` … `23:30~24:00`，**但只渲染有排班的档**（刻度压缩）。
 *
 * 压缩规则：这一档所有医生都没排班 → 这一行不出现。48 行里绝大多数是空档（凌晨、半夜、午休），
 * 全画出来等于让人滚三屏找早班第一档 —— 而"这档没人排班"这个事实，用**不存在**表达反而更清楚
 * （空行和"排了但没号"长得一样，是上一版被吐槽的原因）。
 *
 * 与"取当天排班时间窗并集"的差别：并集保留的是**连续区间**，区间里的空档照样画；
 * 压缩是按档独立的 —— 上午满、中午空、下午满，画出来就是上午和下午两段，中间不补空行。
 *
 * ⚠️ 压缩掉的一段必须看得出来：否则 11:30 下面直接接 14:00，读的人分不清"中间没人排班"
 *    还是"被筛选/隐藏开关筛掉了"。所以断档处打一条**加粗分隔线**（`sep`），
 *    与「凌晨/上午/下午/晚上」的分组线走同一个视觉通道 —— 段落靠线分，不再靠文字标签。
 */
const DAY_SLOT_STEP = 30;
const daySlotRows = computed(() => {
  const rows = [];
  if (deskViewMode.value !== 'day')
    return rows;
  const doctors = dayDoctors.value;
  // 压缩判据只看「有没有排班覆盖这一档」，不看号源是否已满：
  // 满的档要留在板上（它上面还挂着"已挂/余号"和挂号卡片），藏掉等于把已发生的号一起藏了。
  const busy = (start) => doctors.some(d => daySlotCellSchedules(d.id, start).length > 0);
  let prev = -DAY_SLOT_STEP; // 上一保留档的分钟数：初值保证第一行不会被误判成断档
  for (let m = 0; m < 24 * 60; m += DAY_SLOT_STEP) {
    const start = fmtMin(m);
    if (!busy(start))
      continue;
    const gap = m !== prev + DAY_SLOT_STEP;
    rows.push({
      key: start,
      label: `${start}~${fmtMin(m + DAY_SLOT_STEP)}`,
      start,
      group: slotGroupOf(m),
      // 第一行不画线（它上面没有东西需要分隔）；断档或跨组 → 加粗线
      sep: rows.length > 0 && (gap || slotGroupOf(m) !== slotGroupOf(prev)),
    });
    prev = m;
  }
  return rows;
});
/**
 * 当前视图的行 —— 日视图 = 半小时段、周视图 = **医生**。
 *
 * 2026-09-22：周视图原是「班次」行，一格 = 一天 × 一个班次 × 全院医生 →
 * 格里只能塞"患者名单"，"某医生还剩几个号"要点开才知道；改成医生行之后格子天然回答这事。
 *
 * 两个视图共用同一张表，只有行的定义不同（与 `deskColumns` 列的定义同理），
 * 这样格子渲染 / 卡片 / 开单都只有一份实现。
 */
const deskRows = computed(() => deskViewMode.value === 'day'
    ? daySlotRows.value
    : weekDoctorRows.value);
/** 余号文案：0 → 约满，1~4 → 少量余号（真实 HIS 排班大屏的提示口径） */
const deskAvailText = (schedules) => {
  const avail = deskAvail(schedules);
  if (avail <= 0)
    return '约满';
  return avail < 5 ? `余 ${avail}（少量）` : `余 ${avail}`;
};
const deskAvailClass = (schedules) => {
  const avail = deskAvail(schedules);
  if (avail <= 0)
    return 'text-red-500';
  return avail < 5 ? 'text-amber-600' : 'text-emerald-600';
};
const dayDoctorSchedules = (doctorId) => deskSchedules.value.filter(s => s.scheduleDate === deskDay.value && s.doctorId === doctorId);
const isExpertDoctor = (doctorId) => {
  const scope = deskViewMode.value === 'day'
      ? deskSchedules.value.filter((s) => s.scheduleDate === deskDay.value)
      : deskSchedules.value;
  return scope.some((s) => String(s.doctorId) === String(doctorId) && Number(s.isExpert) === 1);
};
const deskColumns = computed(() => {
  if (deskViewMode.value === 'day') {
    return dayDoctors.value.map(d => ({
      key: 'doc-' + d.id,
      title: d.doctorName,
      sub: [d.deptName, d.roomName].filter(Boolean).join(' · '),
      doctorId: d.id,
      // 格子开单（addFromCell）的科室兜底：工具栏可能是「全部科室」，此时只能从排班本身取真实科室
      deptId: d.deptId,
      date: deskDay.value,
    }));
  }
  return deskDays.value.map(d => ({
    key: 'day-' + d,
    title: d,
    sub: weekdayMap[new Date(d + 'T00:00:00').getDay()],
    doctorId: null,
    date: d,
  }));
});
/**
 * 格子数据一次性算好，别在模板里反复 find/filter。
 *
 * 周视图 21 格、日视图最多「医生数 × 3」格，每格都在模板里现算会把每次渲染变成 O(格子数 × 挂号数)。
 */
const deskCellMap = computed(() => {
  const map = {};
  for (const col of deskColumns.value) {
    for (const row of deskRows.value) {
      if (deskViewMode.value === 'day') {
        const srcSchedules = daySlotCellSchedules(col.doctorId, row.start);
        map[`${col.key}|${row.key}`] = {
          // schedules = 段投影：余号/总数一律取段上的真值（下面的 deskTotal / deskAvailText 直接用它）
          schedules: daySlotSegments(srcSchedules, row.start),
          srcSchedules,
          regs: daySlotCellRegs(col.doctorId, row.start),
        };
      } else {
        const schedules = weekCellSchedules(col.date, row.key);
        map[`${col.key}|${row.key}`] = {
          schedules,
          srcSchedules: schedules,
          regs: deskRegsOfSchedules(col.date, schedules)
        };
      }
    }
  }
  return map;
});
/**
 * 取一格。行键在日视图是 `"08:30"`、周视图是班次数字(1/2/3/4)，同一视图内唯一、跨视图不混用。
 */
const deskCell = (colKey, rowKey) => deskCellMap.value[`${colKey}|${rowKey}`] || {
  schedules: [],
  regs: [],
  srcSchedules: []
};
/**
 * 分时段挂号截止提前量（分钟）：时段开始前 10 分钟之后不再放号（用户口径 2026-10-14）。
 * 与后端 BizAppointServiceImpl.REGIST_CUTOFF_MINUTES 同口径，改动必须两边同步。
 */
const REGIST_CUTOFF_MINUTES = 10;
/**
 * 这一格「没号可挂」= 压根**没有排班**（2026-09-22）。
 *
 * 判据用 `srcSchedules`（覆盖这一档的排班本体），不用段投影：段没拉到 ≠ 没排班。
 * 判定收口成一个函数是因为它现在同时决定两件事：
 *   · 文案走「未排班」分支
 *   · 格子走 `desk-cell-na`（淡灰底 + `cursor: not-allowed`）
 * 两处各写一遍 `!…srcSchedules.length` 迟早会出现"字写着未排班、格子看着能点"。
 *
 * 2026-10-14 扩展：**已过挂号截止时间不算"格子不可用"**（用户口径：能看不能挂）——
 * 已过时段的格子照常显示排班/余号/预约按钮，超时拦截收在挂号动作里（addFromCell
 * 与弹窗候选过滤、提交校验），不置灰 —— 否则"上午班过了、下午班还在"会把整格误伤成灰。
 * 这里只保留「无排班」判据。
 */
const isCellUnavailable = (col, rowKey) => !deskCell(col.key, rowKey).srcSchedules.length;
/**
 * 该格子是否已过挂号截止（开诊前 {@link #REGIST_CUTOFF_MINUTES} 分钟，用户口径 2026-10-14）：
 * 只用于**挂号动作时拦截**（addFromCell 入口 + 弹窗候选过滤 + 提交校验），不参与格子置灰。
 *   · 就诊日 < 今天 → 整格不可挂（历史日期）
 *   · 就诊日 = 今天：
 *       日视图格子（rowKey 是 "HH:mm" 半小时档）→ 档开始-10min ≤ 现在即不可挂；
 *       周视图格子（rowKey 是医生）→ 该格**所有**班次开始-10min 都已过才整格拦截，
 *         只要还有一个未截止班次，放行进弹窗 —— 弹窗里（AppointmentsAdd）再滤掉已过班次。
 */
const isCellTimePassed = (col, rowKey) => {
  const date = String(col.date || '');
  const today = fmtDate(new Date());
  if (!date || date > today) return false;
  if (date < today) return true;
  const nowMin = new Date().getHours() * 60 + new Date().getMinutes();
  if (deskViewMode.value === 'day') {
    const m = toMin(rowKey);
    return m !== null && m - REGIST_CUTOFF_MINUTES <= nowMin;
  }
  const scheds = deskCell(col.key, rowKey).srcSchedules;
  if (!scheds.length) return false; // 无排班由 isCellUnavailable 原有分支管
  return scheds.every((s) => {
    const m = toMin(s.startTime);
    return m !== null && m - REGIST_CUTOFF_MINUTES <= nowMin;
  });
};
const showRescheduleDialog = ref(false);
// 打开改约弹窗前由本页写好：被改约的卡片 + 它当前挂的排班（组件用它回显「当前号源」）
const rescheduleRow = ref(null);
const rescheduleSchedule = ref(null);
/** 挂号状态点颜色（1挂号 2签到 3接诊 4就诊 5退号 6过号 7爽约 8未就诊） */
const regStatusDot = {
  1: 'bg-blue-500',
  2: 'bg-cyan-500',
  3: 'bg-green-500',
  4: 'bg-slate-400',
  5: 'bg-red-400',
  6: 'bg-orange-500',
  7: 'bg-rose-600',
  8: 'bg-slate-400',
};
/**
 * 卡片状态标签：每个状态都有一句话说明，颜色与色点一致。
 *
 * 口径 = `AppointStatusEnum`（1已挂号 2已签到 3已接诊 4已就诊 5已退号 6已过号 7爽约 8未就诊）。
 * 「已退号(5)」2026-10-08 起定义 —— 退号是否上板由左侧状态勾选框决定（默认勾选=展示），
 *   原口径「退号不上板」随勾选框下线；未勾选时它不出现在板上，但数据仍整段全量加载。
 * 别把「已签到」写成「候诊中」：那是 `QueueStatusEnum` 的词，两套枚举不能互套。
 *
 * 2026-10-08 老王定色：就诊中=亮绿（还在诊室里，最该抢眼）、已就诊=灰（看完走人，退到背景），
 * 已挂号保持蓝不变。名字颜色跟着走（>=4 灰名）。
 */
const deskStatusChip = {
  1: {text: '已挂号', cls: 'bg-blue-50 text-blue-600', dot: 'bg-blue-500', bar: 'bg-blue-500'},
  2: {text: '已签到', cls: 'bg-cyan-50 text-cyan-700', dot: 'bg-cyan-500', bar: 'bg-cyan-500'},
  3: {text: '就诊中', cls: 'bg-green-100 text-green-700', dot: 'bg-green-500', bar: 'bg-green-500'},
  4: {text: '已就诊', cls: 'bg-slate-100 text-slate-500', dot: 'bg-slate-400', bar: 'bg-slate-400'},
  5: {text: '已退号', cls: 'bg-red-50 text-red-500', dot: 'bg-red-400', bar: 'bg-red-400'},
  6: {text: '已过号', cls: 'bg-orange-100 text-orange-700', dot: 'bg-orange-500', bar: 'bg-orange-500'},
  7: {text: '爽约', cls: 'bg-rose-100 text-rose-700', dot: 'bg-rose-600', bar: 'bg-rose-600'},
  8: {text: '未就诊', cls: 'bg-slate-100 text-slate-500', dot: 'bg-slate-400', bar: 'bg-slate-400'},
};
const DESK_STATUS_OPTIONS = [
  {value: 1, label: '已挂号'},
  {value: 2, label: '已签到'},
  {value: 3, label: '就诊中'},
  {value: 4, label: '已就诊'},
  {value: 5, label: '已退号'},
  {value: 6, label: '已过号'},
  {value: 7, label: '爽约'},
  {value: 8, label: '未就诊'},
];
const deskStatusFilter = ref(DESK_STATUS_OPTIONS.map(s => s.value));
const deskRegVisible = (r) => deskStatusFilter.value.includes(Number(r.registStatus));
const handleDeskDeptChange = () => {
  // 换科室后原来选中的医生可能压根不在这个科室，必须清掉，否则看板会空得莫名其妙
  deskDoctorId.value = null;
  // 月历圆点跟随科室：主区看呼吸内科，圆点也只标呼吸内科开诊的日子，否则点进去是空的
  loadCalServiceDays();
  loadDesk();
};
const handleDeskDoctorChange = () => {
  loadCalServiceDays();
  loadDesk();
};
/**
 * 看板工具栏的「新增预约」：不带具体号源（看板是全局视角，格子才是具体某一档），
 * 但必须带科室 + 日期 —— 这两项在工具栏上是可见的事实，让用户重选一遍等于白给。
 * 「全部科室」没有具体科室可带，`AppointmentsAdd` 里的 `openAddFromPanel` 会置空不猜（见那里的第 ③ 条）。
 */
const handleDeskAdd = () => {
  addFromPanel({
    deptId: deskDeptId.value,
    visitDate: deskViewMode.value === 'day' ? deskDay.value : (deskDays.value[0] || ''),
  });
};
const handleDeskViewChange = async () => {
  selectedCell.value = null;
  if (deskViewMode.value === 'week')
    deskHideFull.value = false;
  await loadDesk();
  await nextTick();
  calcDeskHeight();
};
const handleDeskStep = (delta) => {
  selectedCell.value = null;
  if (deskViewMode.value === 'day') {
    const d = new Date(deskDay.value + 'T00:00:00');
    d.setDate(d.getDate() + delta);
    deskDay.value = fmtDate(d);
  } else {
    deskWeekOffset.value += delta;
  }
  loadDesk();
};
const handleDeskRefresh = async () => {
  selectedCell.value = null;
  await loadDesk();
  loadCalServiceDays(); // 不 await：圆点是导航装饰，晚一拍到不影响任何操作
};
const handleDeskToday = () => {
  deskDay.value = fmtDate(new Date());
  deskWeekOffset.value = 0;
  selectedCell.value = null;
  loadDesk();
};
/**
 * 周视图点日期（列头或「还有 N 人」）→ 下钻到当天日视图。
 *
 * 这就是真实 HIS 的「格子里只铺两块，点开切到日模式看全部」：
 * 周视图回答"哪天忙"，日视图回答"具体是谁"，两者共用同一份已加载数据，切过去不再打接口以外的东西。
 */
const drillToDay = (date) => {
  deskDay.value = date;
  deskViewMode.value = 'day';
  selectedCell.value = null;
  loadDesk();
};
/** 复诊号：费用按复诊收费策略按间隔天数现算，改期会让免收口径失效 → 已收费一律不许改约 */
const isRevisitRegist = (r) => Number(r?.revisitSource) > 0;
/**
 * 能不能改约：只有「已挂号」且就诊日未过。
 * 已收费的号不再一刀切禁掉 —— 后端闸放的是「同医生 + 同费用」的号源（改约弹窗按这个筛候选），
 * 复诊号除外（见 isRevisitRegist）。
 */
const canReschedule = (r) => Number(r?.registStatus) === 1 && !isVisitDatePast(r)
    && (!isRegistPaid(r) || !isRevisitRegist(r));
const isVisitDatePast = (r) => {
  const d = String(r?.visitDate || '').slice(0, 10);
  return !!d && d < fmtDate(new Date());
};
const canCancelRegist = (r) => [1, 2].includes(Number(r?.registStatus)) && !isVisitDatePast(r);
/** 能打开收款码：有账单且还欠钱（1-待支付 2-部分支付）；免收/已收齐/已作废/已退费都没有可收的钱 */
const canCollectFee = (r) => !!r?.billId && [1, 2].includes(Number(r?.billStatus));
const canApplyRefund = (r) => !!r?.billId
    && isVisitDatePast(r)
    && isRegistPaid(r)
    && [1, 2, 6, 7, 8].includes(Number(r?.registStatus));
/** 锁死原因文案：挂到卡片 title 上，别让用户点了没反应还不知道为什么 */
const sourceLockReason = (r) => {
  if (isVisitDatePast(r)) {
    return `就诊日 ${String(r?.visitDate || '').slice(0, 10)} 已过，不能退号或改约，患者未到诊需退费的，请到「挂号记录」里发起退费申请`;
  }
  if (Number(r?.registStatus) === 1 && isRegistPaid(r)) {
    return isRevisitRegist(r)
        ? '该挂号已收费且是复诊号：费用按复诊收费策略现算，改期会使免收口径失效；请先退号退费（按原支付渠道退回）后重新挂号'
        : '该挂号已收费：改约只能选同医生、同费用的号源（弹窗里已按此过滤）；不同价请先退号退费（按原支付渠道退回）后重新挂号';
  }
  switch (Number(r?.registStatus)) {
    case 3:
      return '已接诊：诊疗已开始，不再允许调整或变更号源';
    case 4:
      return '已就诊：诊疗已结束，不再允许调整或变更号源';
    case 5:
      return '已退号：诊疗已结束，不再允许调整或变更号源';
    case 6:
      return '已过号：诊疗已结束，不再允许调整或变更号源';
    case 7:
      return '爽约：诊疗已结束，不再允许调整或变更号源';
    case 8:
      return '未就诊：诊疗已结束，不再允许调整或变更号源';
    default:
      return '';
  }
};
// ========== 看板卡片上的患者信息（一行一件事，格子只有 ~110px 宽） ==========
/** 性别年龄：「男 52岁」；没采集到的那一项就不显示，不编造 */
const deskPatientMeta = (r) => {
  const parts = [];
  const g = patientGenderText(r?.gender);
  if (g !== '—')
    parts.push(g);
  const a = patientAgeText(r?.age);
  if (a !== '—')
    parts.push(a);
  return parts.join(' ');
};
/** 就诊号（排队号）：签到才建队列行，所以未签到时为空，不写占位 */
const deskCardVisitNo = (r) => (r?.queueNo ? `${r.queueNo} 号` : '');
/** 第二行：医生 · 号别。急诊号要一眼能挑出来（走另一条就诊流程） */
const deskCardDoctorLine = (r) => {
  const parts = [];
  parts.push(r?.doctorName || '未定医生');
  parts.push(statusOf(REGIST_TYPE, r?.registType).label);
  return parts.join(' · ');
};
/**
 * 第三行：结算方式。自费是默认值不写（每张卡都刷一行「自费」等于没信息）。
 * 就诊号在第一行年龄后面，所以本行只剩非自费的结算信息。
 */
const deskCardMetaLine = (r) => {
  if (Number(r?.settlementType) !== 1)
    return settlementLabel(r?.settlementType);
  return '';
};
/**
 * 卡片悬停提示：动作说明 + 完整患者信息（卡片上被截断的在这里兜底）+ 电话 + 挂号单号。
 * 电话放这里而不是占卡片一行 —— 改约/退号前要联系患者时才需要，平时不用看。
 */
const deskCardTitle = (r) => {
  const action = canReschedule(r) ? '点击改约（换号源）' : (sourceLockReason(r) || '当前状态不可改约');
  const meta = deskPatientMeta(r);
  const doctor = deskCardDoctorLine(r);
  const visitNo = deskCardVisitNo(r);
  const settlement = deskCardMetaLine(r);
  const info = [meta, visitNo, doctor, settlement].filter(Boolean).join(' · ');
  const extra = [info, r?.phone ? `电话 ${r.phone}` : '', r?.registNo ? `就诊号 ${r.registNo}` : '']
      .filter(Boolean)
      .join('　');
  return extra ? `${action}\n${extra}` : action;
};
/**
 * 点卡片：能改约的进改约弹窗；已锁死的状态给一句人话。
 * 卡片统一 cursor-pointer —— 点了有反馈才配得上手型（点了没反应最让人困惑）。
 */
const handleDeskCardClick = (r) => {
  if (canReschedule(r)) {
    handleReschedule(r);
    return;
  }
  ElMessage.warning(sourceLockReason(r) || '当前状态不允许变更号源');
};
/** 点患者卡：改约（换号源弹窗拆到同目录组件，后端原子释放旧号/扣新号并同步就诊日期与医生） */
const handleReschedule = (r) => {
  // 双保险：即使某处漏改了模板的 @click，也不让已就诊/就诊中的卡进到改约弹窗
  if (!canReschedule(r)) {
    ElMessage.warning(sourceLockReason(r) || '当前状态不允许变更号源');
    return;
  }
  rescheduleRow.value = r;
  // 当前号源 = 挂号挂的那条排班（deskScheduleMap 里是排班本体），传给改约组件回显
  rescheduleSchedule.value = deskScheduleMap.value[r.scheduleId] || null;
  showRescheduleDialog.value = true;
};
const handleDeskCancel = async (r) => {
  // 双保险：即使某处漏改了模板的 v-if，也不让不可退的卡走到接口
  if (!canCancelRegist(r)) {
    ElMessage.warning(sourceLockReason(r) || '当前状态不允许退号');
    return;
  }
  try {
    await ElMessageBox.confirm(`确认为 ${r.patientName} 办理退号？`, '退号确认', {
      confirmButtonText: '退号', cancelButtonText: '取消', type: 'warning',
    });
  } catch {
    return;
  }
  try {
    await cancelRegistration(r.id, '看板退号');
    ElMessage.success('退号成功');
    loadDesk();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
const handleTabChange = (tab) => {
  // 首次切到预约看板时加载本周数据（号源面板已并入周视图，不再单独取数）
  if (tab === 'desk' && !deskDays.value.length) {
    loadDesk();
    loadCalServiceDays();
  }
};
/**
 * 周视图格子里的排班 —— 原「号源面板」的取数就长在这上面。
 *
 * `all` = 该格全部排班；`visible` = 应用「隐藏已满/停诊」后的可见排班。
 * 两者刻意分开：开关只影响**显示**，不能让人误以为「已满的班次不存在」
 * （给患者改推荐时间时，知道"上午满了"和"上午没班"是两回事）。
 */
const cellAll = (date, doctorId) => weekCellSchedules(date, doctorId);
const cellSchedules = (date, doctorId) => {
  const all = cellAll(date, doctorId);
  if (!deskHideFull.value)
    return all;
  return all.filter(s => Number(s.availableSource) > 0);
};
/** 该格被开关藏掉了几个班次（用于「已满/停诊已隐藏 N」提示，避免静默消失） */
const cellHiddenCount = (date, doctorId) => cellAll(date, doctorId).length - cellSchedules(date, doctorId).length;
/**
 * 当前视图被藏掉的班次总数：开关打开时给一句总账，别让人以为号源凭空少了。
 *
 * 日视图也要有这个总账（原来只算了周视图）—— 否则在日视图打开开关，
 * 某个医生整个半天凭空消失，没有任何提示。
 */
const deskHiddenTotal = computed(() => (deskColumns.value || []).reduce((sum, col) => sum + deskRows.value.reduce((s, row) => s + cellHiddenCountFor(col, row.key), 0), 0));
/**
 * 周视图格子的明细**只由「余号」按钮触发**（2026-09-22）。
 *
 * 原实现的单击=弹明细有个致命副作用：周视图一行一位医生，看板是"横着扫"的，
 * 任何一次误点（甚至只是想选中/瞄一眼）都会弹出一层模态 —— 而在挂号窗口这种
 * 手速场景下，弹框要比"多按一下"贵得多。可点的事情明确放在按钮上，误触成本就消失了。
 *
 * 原实现还带「双击格子开单」，2026-10-08 按需求摘除：格子上已有「预约」按钮，
 * 开单入口只留按钮，误触成本归零。
 */
const openCellDialog = (date, doctorId) => {
  if (!cellSchedules(date, doctorId).length)
    return;
  const rowHit = weekDoctorRows.value.find(r => String(r.doctorId) === String(doctorId));
  selectedCell.value = {date, doctorId, doctorName: rowHit?.label || ''};
  showCellDialog.value = true;
};
/**
 * 按**列**取格子排班 —— 日视图列 = 医生、周视图列 = 日期，两种格子的定位维度不同。
 *
 * 上面那几个 `(date, doctorId)` 版函数是周视图专用（周视图列就是日期、行是医生）；
 * 「预约」按钮在两个视图都要开单，所以这里再包一层按 `col` 定位的版本，
 * 让调用方只关心"这一格"，不关心当前是哪个视图。
 *
 * `all` = 全部排班，`visible` = 应用「隐藏已满/停诊」后的可见排班（与周视图口径一致）。
 */
const cellAllFor = (col, rowKey) => deskViewMode.value === 'day'
    ? daySlotCellSchedules(col.doctorId, String(rowKey))
    : cellAll(col.date, rowKey);
const cellSchedulesFor = (col, rowKey) => {
  const all = cellAllFor(col, rowKey);
  if (!deskHideFull.value)
    return all;
  // 日视图的「满」是**段级**的：这一档没号就算满 —— 同一位医生 10 点那档还有号，
  // 不代表 8 点这档还有号，用主表 available>0 判断会把满的档也放出来。
  if (deskViewMode.value === 'day') {
    return all.filter((s) => deskAvail(daySlotSegments([s], String(rowKey))) > 0);
  }
  return all.filter(s => Number(s.availableSource) > 0);
};
const cellHiddenCountFor = (col, rowKey) => cellAllFor(col, rowKey).length - cellSchedulesFor(col, rowKey).length;
/**
 * 日历下方的「当天概况」：医生数 / 总号源 / 已挂号 / 剩余号源。
 *
 * 数据全部来自**已加载的** `deskSchedules` + `deskRegs`，不新增任何接口 ——
 * 这些数字本来就画在每个医生的格子里（"已挂 2 / 总 50 · 余 47"），
 * 只是要点一遍每格才能加总。日历旁边给一份合计，回答的是"这天忙不忙、还能挂几个"。
 *
 * ⚠️ 口径与格子严格一致：
 *   · 只用当前窗口那天（日视图 = `deskDay`），不跨天
 *   · 「医生数」按 `doctorId` 去重（同一医生上午下午两个班次算 1 位）
 *   · 「已挂号」沿用 `deskRegsOfSchedules`（已退号不计数），与格子里的"已挂 N"同源
 *   · 「剩余号源」= `availableSource` 合计，与格子里的"余 N"同源
 *   不在这块里另算一套口径 —— 否则日历说 50、格子加起来是 47，就是"同一屏两个数字打架"。
 *
 * ⚠️ 这块**不显示"隐藏已满"开关的影响**：开关是"看什么"，概况是"实际有多少"。
 * 开关开着时格子少了几个班次，但这天的容量事实没变（已满也是这天的号源），
 * 所以概况始终按 `deskSchedules` 全量算 —— 与 `deskHiddenTotal` 那句"已隐藏 N 个"配套看。
 */
const deskDayStat = computed(() => {
  const date = deskDay.value;
  const dayScheds = deskSchedules.value.filter(s => s.scheduleDate === date);
  const doctors = new Set(dayScheds.map(s => s.doctorId).filter(Boolean)).size;
  const total = dayScheds.reduce((sum, s) => sum + (Number(s.totalSource) || 0), 0);
  const avail = dayScheds.reduce((sum, s) => sum + (Number(s.availableSource) || 0), 0);
  const regs = deskRegCount(deskRegsOfSchedules(date, dayScheds));
  // 已用占比 = 已挂号 / 总号源（不是 1 - 余号/总数：两者在有"停用号源"时会不等，
  // 挂号员关心的是"发出去了多少号"，用实际挂号数更直白）
  const usedPct = total > 0 ? Math.min(100, Math.round((regs / total) * 100)) : 0;
  return {doctors, total, avail, regs, usedPct};
});
/**
 * 打开新增挂号弹窗。
 *
 * seed = 看板带进来的预填（科室/日期/号源/段/医生），由子组件在打开时消费；
 * 工具栏「新增挂号」不带 seed，开一张干净的空表。
 */
const addFromPanel = (seed = null) => {
  addSeed.value = seed;
  showAddDialog.value = true;
};
const goRegister = (s) => {
  addFromPanel({
    deptId: s.deptId,
    visitDate: s.scheduleDate,
    scheduleId: s.id,
    // 号源面板已并入周视图，两者现在共用 deskSchedules 一份数据
    snapshot: {scheduleDate: s.scheduleDate, list: deskSchedules.value.filter(x => x.scheduleDate === s.scheduleDate)},
  });
};
const addFromCell = (col, rowKey) => {
  // 兜底：模板里不可用格已置灰无按钮，但直连/键盘触发时给一句人话，别静默进弹窗选不到时段
  if (isCellTimePassed(col, rowKey)) {
    ElMessage.info(`该时段已过挂号截止时间（开诊前 ${REGIST_CUTOFF_MINUTES} 分钟截止），无法挂号`);
    return;
  }
  const cellScheds = cellSchedulesFor(col, rowKey);
  if (!cellScheds.length) {
    // 分清两种"没号源"：本来就没排班 vs 被「隐藏已满/停诊」开关藏掉了
    const hidden = cellHiddenCountFor(col, rowKey);
    ElMessage.info(hidden > 0
        ? `该时段 ${hidden} 个班次已满/停诊（当前被「隐藏已满/停诊」开关隐藏），关掉开关可查看并开单`
        : '该时段没有排班，无法新增预约');
    return;
  }
  // 日视图这一格 = 某医生的某半小时档 → 把这一档直接带上，挂号单不必再选一遍时间段
  let slotId;
  if (deskViewMode.value === 'day') {
    const ids = new Set(cellScheds.map((s) => String(s.id)));
    const segs = deskSlots.value.filter((s) => ids.has(String(s.scheduleId)) && String(s.startTime).slice(0, 5) === String(rowKey));
    if (segs.length === 1)
      slotId = segs[0].id;
  }
  // 多个号源（同医生多诊室 / 同格多医生）→ 不猜第一条，把 scheduleId 留空让人在下拉里选
  const deptId = cellScheds[0]?.deptId ?? col.deptId ?? deskDeptId.value;
  const snapshotDate = deskViewMode.value === 'day' ? deskDay.value : col.date;
  const onlyOne = cellScheds.length === 1;
  addFromPanel({
    deptId,
    visitDate: snapshotDate,
    scheduleId: onlyOne ? cellScheds[0].id : undefined,
    // 周视图的行就是这位医生、日视图的列也是这位医生 —— 两种视图都能把医生带上，
    // 号源下拉直接落到他身上，不用在下拉里再找一遍
    doctorId: deskViewMode.value === 'day' ? col.doctorId : rowKey,
    slotId,
    snapshot: {scheduleDate: snapshotDate, list: cellScheds},
  });
};
onMounted(async () => {
  // 列表 + 统计：2 次请求（原来是 listPage 7 次：1 次列表 + 6 次「读 total」）
  reloadListAndStats();
  loadRevisitSourceDict();
  // 顺序有讲究：科室列表先到（默认科室要命中下拉里真实存在的 option），再取当前用户科室，最后才拉看板
  await loadDepartments();
  await loadDefaultDept();
  // 默认 tab 是「预约看板」，但 @tab-change 只在用户点击时触发 → 首屏必须自己拉一次
  if (activeTab.value === 'desk') {
    loadDesk();
    // 月历圆点：与主区并行拉，不 await（它是导航装饰，晚 200ms 到不影响任何操作）
    loadCalServiceDays();
  }
  // 表格撑满窗口：首屏 DOM 出来之后才量得到位置，所以放在 nextTick 里
  await nextTick();
  calcDeskHeight();
  window.addEventListener('resize', onWinResize);
});
onUnmounted(() => {
  window.removeEventListener('resize', onWinResize);
});
</script>

<style scoped>
.patient-select :deep(.el-select-dropdown__item) {
  height: auto;
  min-height: 72px;
  padding: 8px 20px;
  line-height: 1.5;
}

tr.desk-row:hover > td {
  background-color: #e8f1fc;
}

tr.desk-row:hover > td.desk-row-head {
  background-color: #d3e4f8;
}

td.desk-cell-na {
  cursor: not-allowed;
}
</style>

<style>
.desk-board .text-xs {
  font-size: 14px;
}

.desk-board .text-\[11px\] {
  font-size: 13px;
}

.desk-board .text-\[10px\] {
  font-size: 12px;
}

.desk-board .text-\[9px\] {
  font-size: 11px;
}

.desk-board table .text-sm {
  font-size: 16px;
}

.desk-board table td,
.desk-board table th {
  line-height: 1.55;
}

.desk-board .text-slate-300 {
  color: #94a3b8;
}

.desk-board .text-slate-400 {
  color: #64748b;
}

.desk-board .text-slate-500 {
  color: #475569;
}

.desk-board aside .grid [class*="text-[11px]"] {
  font-size: 12px;
}
</style>

<style>
.patient-select-dropdown {
  width: 500px !important;
}
</style>
