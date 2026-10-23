<template>
  <div class="flex h-[var(--his-page-h)] flex-col gap-3">
    <!-- ============ 工具栏 ============ -->
    <div class="flex flex-wrap items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 shadow-sm">
      <el-date-picker
          v-model="visitDate"
          :clearable="false"
          style="width: 148px"
          type="date"
          value-format="YYYY-MM-DD"
          @change="pagination.pageNum = 1; reload()"
      />
      <el-select v-model="deptId" filterable placeholder="诊区" style="width: 150px" @change="onDeptChange">
        <el-option v-for="d in depts" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
      </el-select>
      <el-select v-model="doctorId" clearable filterable placeholder="医生" style="width: 140px"
                 @change="pagination.pageNum = 1; loadList()">
        <el-option v-for="e in doctors" :key="e.id" :label="e.empName" :value="String(e.id)"/>
      </el-select>
      <el-input v-model="keyword" clearable placeholder="姓名/患者号/就诊号/排队号" style="width: 220px"
                @keyup.enter="pagination.pageNum = 1; loadList()">
        <template #prefix>
          <el-icon>
            <Search/>
          </el-icon>
        </template>
      </el-input>
      <el-button :icon="Search" type="primary" @click="pagination.pageNum = 1; loadList()">查询</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>

      <div class="ml-auto flex flex-wrap items-center gap-1.5">
        <el-button :type="statusFilter.length === 0 ? 'primary' : 'default'" size="small" @click="toggleAll">
          全部
        </el-button>
        <el-button :type="statusFilter.includes(2) ? 'primary' : 'default'" size="small" @click="toggleStatus(2)">
          候诊 {{ stats.waiting ?? 0 }}
        </el-button>
        <el-button :type="statusFilter.includes(3) ? 'primary' : 'default'" size="small" @click="toggleStatus(3)">
          就诊中 {{ stats.consulting ?? 0 }}
        </el-button>
        <el-button :type="statusFilter.includes(6) ? 'primary' : 'default'" size="small" @click="toggleStatus(6)">
          过号 {{ stats.overdue ?? 0 }}
        </el-button>
        <el-button plain size="small" type="danger" @click="filterTimeout">
          候诊超时 {{ stats.timeout ?? 0 }}
        </el-button>
      </div>
    </div>

    <!-- ============ 三栏主体 ============ -->
    <div class="flex min-h-0 flex-1 gap-3">
      <!-- 左栏：监控（常驻，不随选中行切换；各列表卡固定结构，今日统计沉底） -->
      <div class="flex w-[var(--his-queue-col-w)] flex-shrink-0 flex-col gap-3 overflow-hidden">
        <!-- 待签到（已缴费未签到，固定高度：标题 + 列表内滚动 + 底部全部签到） -->
        <div class="flex h-56 flex-col rounded-lg border shadow-sm"
             :class="hasUnchecked ? 'border-red-300 bg-red-50' : 'border-slate-200 bg-white'">
          <div class="flex items-center gap-2 border-b px-3 py-2"
               :class="hasUnchecked ? 'border-red-200' : 'border-slate-200'">
            <WarningFilled class="h-4 w-4" :class="hasUnchecked ? 'text-red-500' : 'text-slate-400'"/>
            <span class="text-sm font-bold" :class="hasUnchecked ? 'text-red-600' : 'text-slate-500'">
              待签到 <span class="text-xs font-normal text-slate-400">已缴费未签到</span>
            </span>
            <span v-if="hasUnchecked"
                  class="ml-auto rounded-full bg-red-500 px-1.5 py-0.5 text-xs font-bold text-white">
              {{ stats.unchecked ?? 0 }}
            </span>
            <span v-else class="ml-auto text-xs text-slate-400">0 人</span>
          </div>
          <div v-loading="uncheckedLoading" class="min-h-0 flex-1 divide-y divide-slate-100 overflow-y-auto bg-white">
            <div v-for="row in uncheckedList" :key="'ui-' + row.registId" class="flex items-center gap-2 px-3 py-2">
              <div class="min-w-0 flex-1">
                <div class="truncate text-sm font-medium text-slate-800">{{ row.patientName }}</div>
                <div class="truncate text-xs text-slate-400">
                  {{ row.registNo }} · {{ statusOf(REGIST_TYPE, row.registType).label }} · {{
                    row.doctorName || '未指定医生'
                  }}
                </div>
              </div>
              <el-button link size="small" type="primary" @click="doCheckIn(row)">签到</el-button>
            </div>
            <div v-if="!uncheckedLoading && uncheckedList.length === 0" class="py-5 text-center text-xs text-slate-400">
              没有待签到的患者
            </div>
          </div>
          <div v-if="uncheckedList.length > 0" class="border-t border-slate-200 bg-white px-3 py-2">
            <el-button :loading="checkInAllLoading" class="w-full" size="small" type="primary" @click="doCheckInAll">
              全部签到（{{ uncheckedList.length }} 人）
            </el-button>
          </div>
        </div>

        <!-- 当前就诊中（弹性高占满剩余，列表内滚动） -->
        <div class="flex min-h-0 flex-1 flex-col rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="flex items-center gap-2 border-b border-slate-200 px-3 py-2">
            <Clock class="h-4 w-4 text-emerald-600"/>
            <span class="text-sm font-medium text-slate-700">当前就诊中</span>
            <span v-if="consultingCount > 0"
                  class="ml-auto rounded-full bg-emerald-500 px-1.5 py-0.5 text-xs font-bold text-white">
              {{ consultingCount }}
            </span>
          </div>
          <div class="min-h-0 flex-1 divide-y divide-slate-100 overflow-y-auto">
            <template v-for="room in consulting" :key="'c-' + room.doctorId">
              <div v-for="p in (room.consultingPatients || [])" :key="p.id" class="flex items-center gap-2 px-3 py-2">
                <span
                    class="inline-flex h-6 w-6 items-center justify-center rounded-full bg-purple-100 text-xs font-bold text-purple-700">
                  {{ p.patientName?.charAt(0) }}
                </span>
                <div class="min-w-0">
                  <div class="truncate text-sm font-medium text-slate-800">{{ p.patientName }}</div>
                  <div class="truncate text-xs text-slate-400">
                    {{ shortQueueNo(p.queueNo) }} · {{ p.roomName || '未分配诊室' }} · {{ p.doctorName }}
                  </div>
                </div>
              </div>
            </template>
            <div
                v-if="consultingLoading && consulting.every((r: any) => !r.consultingPatients || r.consultingPatients.length === 0)"
                class="py-5 text-center text-xs text-slate-400">
              就诊中患者加载中…
            </div>
            <div v-else-if="consulting.every((r: any) => !r.consultingPatients || r.consultingPatients.length === 0)"
                 class="py-5 text-center text-xs text-slate-400">
              暂无就诊中患者
            </div>
          </div>
        </div>

        <!-- 候诊超时 -->
        <div class="rounded-lg border border-amber-200 bg-amber-50 p-3 shadow-sm">
          <div class="flex items-center gap-2">
            <WarningFilled class="h-4 w-4 text-amber-600"/>
            <span class="text-sm font-medium text-amber-700">候诊超时</span>
            <span class="ml-auto text-xs text-amber-600">等候 &gt; 30 分钟</span>
          </div>
          <div class="mt-2 flex items-center justify-between text-xs text-amber-700">
            <span>当前候诊中已超时</span>
            <span :class="(stats.timeout ?? 0) > 0 ? 'text-red-600' : ''" class="font-semibold text-lg">
              {{ stats.timeout ?? 0 }} 人
            </span>
          </div>
          <div class="mt-2">
            <el-button size="small" type="warning" plain @click="filterTimeout">
              查看候诊队列
            </el-button>
          </div>
        </div>

        <!-- 复诊等候超时（弹性高，列表内滚动） -->
        <div class="flex min-h-0 flex-1 flex-col rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="flex items-center gap-2 border-b border-slate-200 px-3 py-2">
            <WarningFilled class="h-4 w-4 text-red-500"/>
            <span class="text-sm font-medium text-slate-700">复诊等候超时</span>
            <span v-if="revisitTimeout.length > 0"
                  class="ml-auto rounded-full bg-red-500 px-1.5 py-0.5 text-xs font-bold text-white">
              {{ revisitTimeout.length }}
            </span>
          </div>
          <div class="min-h-0 flex-1 divide-y divide-slate-100 overflow-y-auto">
            <div v-for="q in revisitTimeout" :key="'rt-' + q.id" class="flex items-center justify-between px-3 py-2">
              <div class="min-w-0">
                <div class="truncate text-sm font-medium text-slate-800">
                  {{ shortQueueNo(q.queueNo) }} · {{ q.patientName }}
                </div>
                <div class="text-xs text-slate-400">等候 {{ waitText(q) }} · {{ q.deptName }}</div>
              </div>
              <el-button link size="small" type="danger" @click="handleRejoin(q, 1)">置为优先</el-button>
            </div>
            <div v-if="revisitTimeout.length === 0 && revisitLoading" class="py-5 text-center text-xs text-slate-400">
              超时复诊加载中…
            </div>
            <div v-else-if="revisitTimeout.length === 0" class="py-5 text-center text-xs text-slate-400">
              暂无超时复诊患者
            </div>
          </div>
        </div>

        <!-- 今日统计（左下角沉底，数据与顶部统计同源） -->
        <div class="mt-auto rounded-lg border border-slate-200 bg-white px-3 py-2 shadow-sm">
          <div class="mb-1 text-xs font-medium text-slate-500">今日</div>
          <div class="flex flex-wrap gap-x-2.5 gap-y-0.5 text-xs text-slate-500">
            <span>候诊 <b class="text-slate-800">{{ stats.waiting ?? 0 }}</b></span>
            <span>就诊 <b class="text-slate-800">{{ stats.consulting ?? 0 }}</b></span>
            <span>过号 <b class="text-slate-800">{{ stats.overdue ?? 0 }}</b></span>
            <span>完成 <b class="text-slate-800">{{ stats.completed ?? 0 }}</b></span>
          </div>
        </div>
      </div>

      <!-- 中栏：FIFO 队列主表（最宽） -->
      <div class="flex min-w-0 flex-1 flex-col rounded-lg border border-slate-200 bg-white shadow-sm">
        <el-table
            v-loading="loading"
            :data="rows"
            height="100%"
            highlight-current-row
            row-key="id"
            @current-change="onRowChange"
        >
          <el-table-column align="center" label="序" type="index" width="52"/>
          <el-table-column label="排队号" width="92">
            <template #default="{ row }">
              <span class="rounded bg-blue-50 px-2 py-0.5 text-xs font-bold text-blue-700">{{
                  shortQueueNo(row.queueNo)
                }}</span>
            </template>
          </el-table-column>
          <el-table-column label="患者" min-width="150">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-900">{{ row.patientName }}</div>
              <div class="text-xs text-slate-400">
                {{ genderText(row.gender) }}/{{ row.age ?? '-' }} · {{ row.patientNo }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="医生" prop="doctorName" show-overflow-tooltip width="88"/>
          <el-table-column align="center" label="号别" width="80">
            <template #default="{ row }">
              <el-tag :type="statusOf(REGIST_TYPE, row.registType).tagType" effect="plain" size="small">
                {{ statusOf(REGIST_TYPE, row.registType).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column align="center" label="初/复" width="62">
            <template #default="{ row }">
              <span v-if="row.visitType === 2" class="text-xs font-medium text-sky-600">复诊</span>
              <span v-else class="text-xs text-slate-300">初诊</span>
            </template>
          </el-table-column>
          <el-table-column label="诊室" show-overflow-tooltip width="86">
            <template #default="{ row }">
              <span :class="row.roomName ? 'text-slate-700' : 'text-slate-300'">{{ row.roomName || '-' }}</span>
            </template>
          </el-table-column>
          <el-table-column align="right" label="等候" width="86">
            <template #default="{ row }">
              <span
                  :class="isTimeoutRow(row) ? 'text-xs font-medium text-red-500' : 'text-xs text-slate-500'">
                {{ waitText(row) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="状态" width="86">
            <template #default="{ row }">
              <el-tag :type="statusOf(QUEUE_STATUS, row.queueStatus).tagType" effect="plain" size="small">
                {{ statusOf(QUEUE_STATUS, row.queueStatus).label }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="150">
            <template #default="{ row }">
              <el-button v-if="row.queueStatus === 2" link type="warning"
                         @click.stop="handleOverdue(row)">过号
              </el-button>
              <el-button v-if="row.queueStatus === 2" link type="danger"
                         @click.stop="handleRejoin(row, 1)">优先
              </el-button>
              <!-- 过号回队：过号患者人又来了的唯一出口（排到队尾，不插队） -->
              <el-button v-if="row.queueStatus === 6" link type="danger" @click.stop="handleRejoin(row, 2)">重新入队
              </el-button>
              <span v-if="row.queueStatus === 3" class="text-xs text-slate-400">医生接诊中</span>
              <span v-else-if="row.queueStatus !== 2 && row.queueStatus !== 6"
                    class="text-xs text-slate-300">已结束</span>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-8 text-sm text-slate-400">
              当前诊区暂无排队记录（已缴费未签到的患者请到上方「待签到」名单处理）
            </div>
          </template>
        </el-table>
        <div class="flex items-center justify-between border-t border-slate-100 px-4 py-2">
          <span class="text-xs text-slate-400">
            共 {{ pagination.total }} 条 · 点行在右栏处理（调号/优先/改派/分诊）
          </span>
          <el-pagination
              :current-page="pagination.pageNum"
              :page-size="pagination.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="pagination.total"
              background
              layout="sizes, prev, pager, next"
              @current-change="(p: number) => { pagination.pageNum = p; loadList() }"
              @size-change="(s: number) => { pagination.pageSize = s; pagination.pageNum = 1; loadList() }"
          />
        </div>
      </div>

      <!-- 右栏：操作卡（头固定 + 内容滚动；顺序：信息网格 → 分诊登记[含保存] → 队列调度 → 留痕） -->
      <div class="flex w-[var(--his-order-col-w)] flex-shrink-0 flex-col gap-3">
        <template v-if="selected">
          <div class="flex min-h-0 flex-1 flex-col rounded-lg border border-slate-200 bg-white shadow-sm">
            <!-- 头：固定 -->
            <div class="flex items-center gap-2 border-b border-slate-200 px-3 py-2">
              <UserFilled class="h-4 w-4 text-blue-600"/>
              <span class="text-sm font-medium text-slate-700">{{ selected.patientName }}</span>
              <el-tag :type="statusOf(QUEUE_STATUS, selected.queueStatus).tagType" effect="plain" size="small">
                {{ statusOf(QUEUE_STATUS, selected.queueStatus).label }}
              </el-tag>
              <el-button class="ml-auto" link size="small" @click="selected = null">收起</el-button>
            </div>

            <!-- 内容：滚动 -->
            <div class="min-h-0 flex-1 space-y-2.5 overflow-y-auto p-3">
              <!-- 患者信息网格 -->
              <div class="grid grid-cols-2 gap-x-3 gap-y-1.5 rounded bg-slate-50 p-2 text-xs">
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">排队号</span>
                  <span class="rounded bg-blue-50 px-1.5 py-0.5 font-bold text-blue-700">{{
                      shortQueueNo(selected.queueNo)
                    }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">患者</span>
                  <span class="font-medium text-slate-800">
                    {{ genderText(selected.gender) }}/{{ selected.age ?? '-' }}
                  </span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">医生</span>
                  <span class="font-medium text-slate-800">{{ selected.doctorName || '未指定医生' }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">诊室</span>
                  <span :class="selected.roomName ? 'font-medium text-slate-800' : 'font-medium text-amber-600'">
                    {{ selected.roomName || '未分配' }}
                  </span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">号别</span>
                  <span class="font-medium text-slate-800">{{ statusOf(REGIST_TYPE, selected.registType).label }}</span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">初/复</span>
                  <span :class="selected.visitType === 2 ? 'font-medium text-sky-600' : 'text-slate-500'">
                    {{ selected.visitType === 2 ? '复诊' : '初诊' }}
                  </span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">等候</span>
                  <span :class="isTimeoutRow(selected) ? 'font-medium text-red-500' : 'text-slate-600'">
                    {{ waitText(selected) }}
                  </span>
                </div>
                <div class="flex items-center justify-between">
                  <span class="text-slate-400">分诊记录</span>
                  <span :class="triageHistory.length > 0 ? 'font-medium text-slate-700' : 'text-slate-400'">
                    {{ triageHistory.length > 0 ? `${triageHistory.length} 次` : '未登记' }}
                  </span>
                </div>
              </div>

              <!-- 分诊登记（门诊不做四级分级，仅体征） -->
              <div class="rounded border border-slate-200 p-2">
                <div class="mb-1.5 text-xs font-semibold text-slate-500">分诊登记（可选）</div>
                <div class="mb-1 text-xs text-slate-400">生命体征</div>
                <div class="grid grid-cols-2 gap-2">
                  <div>
                    <label class="mb-1 block text-xs text-slate-500">体温(℃)</label>
                    <el-input-number v-model="form.temperature" :max="45" :min="30" :precision="1" :step="0.1"
                                     class="w-full" controls-position="right" size="small"/>
                  </div>
                  <div>
                    <label class="mb-1 block text-xs text-slate-500">脉搏(次/分)</label>
                    <el-input-number v-model="form.pulse" :max="300" :min="0" class="w-full" controls-position="right"
                                     size="small"/>
                  </div>
                  <div>
                    <label class="mb-1 block text-xs text-slate-500">呼吸(次/分)</label>
                    <el-input-number v-model="form.respiration" :max="100" :min="0" class="w-full"
                                     controls-position="right" size="small"/>
                  </div>
                  <div>
                    <label class="mb-1 block text-xs text-slate-500">血氧 SpO₂(%)</label>
                    <el-input-number v-model="form.spo2" :max="100" :min="0" class="w-full" controls-position="right"
                                     size="small"/>
                  </div>
                  <div>
                    <label class="mb-1 block text-xs text-slate-500">收缩压(mmHg)</label>
                    <el-input-number v-model="form.systolicBp" :max="400" :min="0" class="w-full"
                                     controls-position="right"
                                     size="small"/>
                  </div>
                  <div>
                    <label class="mb-1 block text-xs text-slate-500">舒张压(mmHg)</label>
                    <el-input-number v-model="form.diastolicBp" :max="300" :min="0" class="w-full"
                                     controls-position="right"
                                     size="small"/>
                  </div>
                </div>
                <div class="mt-2 flex gap-2 border-t border-slate-100 pt-2">
                  <el-button v-perm="['opd:triage:add', 'opd:triage:edit']" :loading="saving" class="flex-1"
                             type="primary" @click="handleSaveTriage">保存分诊
                  </el-button>
                  <el-button @click="clearForm">清空</el-button>
                </div>
              </div>

              <!-- 队列调度（候诊中/过号） -->
              <div v-if="selected.queueStatus === 2 || selected.queueStatus === 6"
                   class="rounded border border-blue-100 bg-blue-50 p-2">
                <div class="mb-1.5 text-xs font-semibold text-blue-700">队列调度</div>
                <template v-if="selected.queueStatus === 2">
                  <div class="mb-1.5 flex items-center justify-between text-xs text-slate-500">
                    <span>调号（相对当前位置）</span>
                    <span>移动步数</span>
                  </div>
                  <div class="mb-2 flex items-center gap-1.5">
                    <el-button class="flex-1" size="small" @click="handleMove(-moveStep)">前移</el-button>
                    <el-input-number v-model="moveStep" :max="10" :min="1" size="small" style="width: 88px"/>
                    <el-button class="flex-1" size="small" @click="handleMove(moveStep)">后移</el-button>
                  </div>
                  <!-- 置为优先：主操作，占满一行 -->
                  <el-button class="w-full" size="small" type="danger" @click="handleRejoin(selected, 1)">
                    置为优先（插队首）
                  </el-button>
                  <div class="mt-1.5 flex items-center gap-1.5">
                    <el-button class="flex-1" size="small" type="warning" plain @click="handleOverdue(selected)">过号
                    </el-button>
                    <el-button class="flex-1" size="small" type="primary" plain @click="openReassign(selected)">
                      改派医生/诊室
                    </el-button>
                  </div>
                </template>
                <template v-else>
                  <div class="mb-1.5 text-xs text-slate-500">
                    过号患者本人已回来：重新排回候诊队列<b>队尾</b>（不插队，避免挤掉正常排队的人）
                  </div>
                  <el-button class="w-full" size="small" type="danger" @click="handleRejoin(selected, 2)">
                    过号回队（队尾）
                  </el-button>
                </template>
              </div>
              <div v-else-if="selected.queueStatus === 3"
                   class="rounded border border-slate-200 bg-slate-50 p-2 text-xs text-slate-500">
                医生接诊中，队列操作不可用；叫号与接诊在医生工作站。
              </div>

              <!-- 分诊留痕 -->
              <div v-if="triageHistory.length > 0" class="rounded bg-slate-50 p-2">
                <div class="mb-1 text-xs font-medium text-slate-500">分诊记录</div>
                <div v-for="(h, i) in triageHistory" :key="h.id"
                     class="flex items-baseline gap-1.5 text-xs text-slate-500">
                  <span :class="i === 0 ? 'font-medium text-slate-700' : ''">{{ i === 0 ? '当前' : '历史' }}</span>
                  <span>{{ h.triageTime }}</span>
                  <span>{{ h.triageNurseName || '未记录护士' }}</span>
                  <span v-if="h.chiefComplaint" class="truncate text-slate-400">{{ h.chiefComplaint }}</span>
                </div>
              </div>
            </div>

          </div>
        </template>

        <!-- 未选中 → 引导 -->
        <template v-else>
          <div class="flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto">
            <div class="rounded-lg border border-blue-100 bg-blue-50 p-3 shadow-sm">
              <div class="mb-1 text-xs font-medium text-blue-700">操作说明</div>
              <ul class="space-y-1 text-xs text-blue-600">
                <li>· 左栏是<b>常驻监控</b>：待签到 / 就诊中 / 候诊超时 / 复诊等候超时，不随选中切换</li>
                <li>· 点中间队列某行，右栏出现该患者的<b>队列处理 + 分诊卡</b></li>
                <li>· 候诊中可：前移/后移（调号）、置为优先（插队首）、过号、改派医生/诊室</li>
                <li>· 过号患者本人回来：点「重新入队」，<b>排到队尾</b>不插队</li>
                <li>· 门诊<b>不做四级分级</b>：队列按先到先得叫号；急诊分级在急诊分诊台</li>
                <li>· <b>叫号（呼叫/重呼）只在医生工作站</b>：分诊台叫号会把医生正在接诊的患者顶掉</li>
                <li>· 诊室只读展示：由排班决定（排队号前缀就是诊室编号），换诊室走「改派」</li>
                <li>· 签到只受理<b>就诊日 = 今天</b>的号；隔日号请先办理改约</li>
                <li>· 缴费请引导患者到收费处，护士站不受理收费</li>
              </ul>
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- ============ 改派弹窗 ============ -->
    <el-dialog v-model="reassignVisible" :close-on-click-modal="true" title="改派医生 / 诊室" width="420px"
               @closed="reassignForm = {doctorId: '', roomId: ''}">
      <div class="space-y-3">
        <div>
          <label class="mb-1 block text-xs text-slate-500">目标医生 <span class="text-red-500">*</span></label>
          <el-select v-model="reassignForm.doctorId" filterable placeholder="选择医生" style="width: 100%">
            <el-option v-for="e in doctors" :key="e.id" :label="e.empName" :value="String(e.id)"/>
          </el-select>
        </div>
        <div>
          <label class="mb-1 block text-xs text-slate-500">目标诊室（不选则保留原诊室）</label>
          <el-select v-model="reassignForm.roomId" clearable filterable placeholder="选择诊室" style="width: 100%">
            <el-option v-for="r in reassignRooms" :key="r.id" :label="r.name" :value="String(r.id)"/>
          </el-select>
        </div>
        <div class="rounded bg-slate-50 px-2 py-1.5 text-xs text-slate-500">
          改派后：排队号按新诊室前缀重生成、排到目标医生队列<b>队尾</b>；挂号记录医生同步变更；号源不随改派转移（需换号源走改约）
        </div>
      </div>
      <template #footer>
        <el-button @click="reassignVisible = false">取消</el-button>
        <el-button :loading="reassigning" type="primary" @click="doReassign">确认改派</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 门诊分诊工作站（2026-10-22 三栏版）
 *
 * 布局按用户拍板的三栏式：
 * - 左栏【监控】常驻：当前就诊中 / 候诊超时 / 复诊等候超时，不随选中行切换；
 * - 中栏【FIFO 队列】最宽：先到先得，去掉四级分级列；
 * - 右栏【操作卡】：点行出现（队列调度：前移/后移/置为优先/过号/改派 + 分诊体征），未选中给引导。
 *
 * 关键口径（2026-10-22 门诊收口）：
 * - 门诊**不做急诊式四级分级**：签到不再默认写 4 级，队列按 sequence_no 纯 FIFO；
 *   急诊 I~IV 级只在急诊科直录行上出现，急诊分诊台另有入口。
 * - rejoinQueue 拆成两种语义：mode=1 置为优先（候诊中插队首）/ mode=2 过号回队（排到队尾不插队）。
 * - 叫号（呼叫/重呼）只在医生工作站发起：与医生「接诊下一位」是同一套抢占语义
 *   （closeConsultingOf 会把该医生当前就诊中的患者置「已就诊」），分诊台只保留
 *   「不动就诊中」的队列管理权。
 * - 诊室来源是排班（排队号前缀=诊室编号）；分诊台不再有「选诊室」表单，换诊室走改派。
 * - 数据源是 **biz_queue**（/queue/listPage），不是挂号列表。
 */
import {computed, onMounted, onUnmounted, ref} from 'vue';
import {Clock, Refresh, Search, UserFilled, WarningFilled} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  checkInByRegist,
  getConsultingPatients,
  getQueueListPage,
  getQueueStats,
  getRevisitTimeoutPatients,
  getTriageByQueueId,
  getUncheckedList,
  moveQueue,
  overdueQueue,
  reassignQueue,
  rejoinQueue,
  saveTriage,
} from '@/api/appoint';
import {getClinicRoomListAll, getDepartmentSelectList, getEmployeeList, getUserInfo} from '@/api/system';
import {QUEUE_STATUS, REGIST_TYPE, statusOf} from '@/lib/statusColor';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {shortQueueNo} from '@/lib/utils';
import {patientGenderText} from '@/lib/patientGender';

const loading = ref(false);
const rows = ref([]);
const depts = ref([]);
const doctors = ref([]);
// 筛选条件（全部下推后端，前端不做任何 filter）
const visitDate = ref(localDate());
const deptId = ref('');
const doctorId = ref('');
const keyword = ref('');
const statusFilter = ref([]);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const stats = ref({});
/** 是否有已缴费未签到患者 —— 有则横幅红色高亮 + 自动展开，处理完自动降级灰色 */
const hasUnchecked = computed(() => (stats.value.unchecked ?? 0) > 0);
const consulting = ref([]);
const revisitTimeout = ref([]);
const consultingLoading = ref(true);
const revisitLoading = ref(true);
const consultingCount = computed(() => (consulting.value || [])
    .reduce((sum, r) => sum + ((r.consultingPatients || []).length), 0));

function localDate() {
  // 不能用 toISOString().slice(0,10)：UTC+8 上午 8 点前会取到昨天
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

const genderText = (g) => patientGenderText(g);
/**
 * 时间解析（全站唯一入口）。
 * 只在「空格分隔」时换成 `T`（ISO 允许），两种格式都能解析；不做老 Safari 的 / 替换。
 */
const parseTime = (v) => {
  if (!v)
    return null;
  const d = new Date(String(v).trim().replace(' ', 'T'));
  return isNaN(d.getTime()) ? null : d;
};
/** 等候分钟数按时间戳现算（不读 wait_duration，那是结诊时才回填的字段） */
const waitMinutes = (row) => {
  const start = parseTime(row.arriveTime);
  if (!start)
    return 0;
  const end = parseTime(row.endTime) || new Date();
  return Math.max(0, Math.floor((end.getTime() - start.getTime()) / 60000));
};
const waitText = (row) => {
  const minutes = waitMinutes(row);
  if (minutes < 60)
    return `${minutes}分`;
  return `${Math.floor(minutes / 60)}时${minutes % 60}分`;
};
/** 候诊超时判定：候诊中且已等候 > 30 分钟（与 stats.timeout 同口径，不落状态列） */
const isTimeoutRow = (row) => row.queueStatus === 2 && waitMinutes(row) >= 30;
/** 行样式：超时行整行标红（背景淡红 + 字体红），护士扫一眼即知谁等了太久 */
// ========== 列表 ==========
const buildParams = () => {
  const params = {
    pageNum: pagination.value.pageNum,
    pageSize: pagination.value.pageSize,
    visitDate: visitDate.value || undefined,
    deptId: deptId.value || undefined,
    doctorId: doctorId.value || undefined,
    keyword: keyword.value.trim() || undefined,
    // 门诊分诊台只管普通号/专家号；急诊直录行归急诊工作站管理（登记即入队，不经分诊签到）
    registTypes: '1,2',
  };
  if (statusFilter.value.length > 0)
    params.queueStatuses = statusFilter.value.join(',');
  return params;
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getQueueListPage(buildParams());
    rows.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载队列失败:', error);
  } finally {
    loading.value = false;
  }
};
const loadStats = async () => {
  try {
    const res = await getQueueStats({
      deptId: deptId.value || undefined,
      visitDate: visitDate.value || undefined,
      registTypes: '1,2',
    });
    stats.value = res.data || {};
  } catch (error) {
    console.error('加载统计失败:', error);
  }
};
const loadConsulting = async () => {
  consultingLoading.value = true;
  try {
    const res = await getConsultingPatients();
    consulting.value = res.data || [];
  } catch (error) {
    console.error('加载就诊中患者失败:', error);
    consulting.value = [];
  } finally {
    consultingLoading.value = false;
  }
};
const loadRevisitTimeout = async () => {
  revisitLoading.value = true;
  try {
    const res = await getRevisitTimeoutPatients();
    revisitTimeout.value = res.data || [];
  } catch (error) {
    console.error('加载复诊超时失败:', error);
    revisitTimeout.value = [];
  } finally {
    revisitLoading.value = false;
  }
};
const reload = async () => {
  await Promise.all([loadList(), loadStats(), loadConsulting(), loadRevisitTimeout()]);
};
const onDeptChange = async () => {
  pagination.value.pageNum = 1;
  doctorId.value = '';
  selected.value = null;
  await loadDoctors();
  await reload();
  await loadUnchecked();
};
const loadDoctors = async () => {
  try {
    const params = {empType: 1, pageSize: 200};
    if (deptId.value)
      params.deptId = deptId.value;
    const res = await getEmployeeList(params);
    doctors.value = res.data?.records || [];
  } catch (error) {
    console.error('加载医生失败:', error);
  }
};
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({});
    depts.value = res.data || [];
    if (deptId.value)
      return;
    // 默认诊区 = 登录人所属科室（先看 localStorage，再看 /auth/info 的 UserLoginVO.deptId）
    let own = localStorage.getItem('deptId');
    if (!own) {
      try {
        const info = await getUserInfo();
        own = info.data?.deptId ? String(info.data.deptId) : null;
      } catch (error) {
        own = null;
      }
    }
    const hit = own ? depts.value.find((d) => String(d.id) === String(own)) : null;
    deptId.value = hit ? String(hit.id) : '';
  } catch (error) {
    console.error('加载科室失败:', error);
  }
};
const handleReset = async () => {
  pagination.value.pageNum = 1;
  doctorId.value = '';
  keyword.value = '';
  statusFilter.value = [];
  visitDate.value = localDate();
  await reload();
};
const toggleStatus = (code) => {
  const idx = statusFilter.value.indexOf(code);
  if (idx >= 0)
    statusFilter.value.splice(idx, 1);
  else
    statusFilter.value.push(code);
  pagination.value.pageNum = 1;
  loadList();
};
const toggleAll = () => {
  statusFilter.value = [];
  pagination.value.pageNum = 1;
  loadList();
};
/** 候诊超时入口：切到候诊筛选 + 整行标红（stats.timeout 与行内 >30 分钟同口径） */
const filterTimeout = () => {
  statusFilter.value = [];
  if (!statusFilter.value.includes(2))
    statusFilter.value.push(2);
  pagination.value.pageNum = 1;
  loadList();
};
// ========== 行操作 ==========
// 叫号（呼叫/重呼）不在分诊台：与医生「接诊下一位」同语义会收口医生当前就诊，见操作列注释。
const handleOverdue = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`将 ${row.patientName}（${row.queueNo}）标记为过号，请填写原因`, '过号确认', {
      confirmButtonText: '确定过号',
      cancelButtonText: '取消',
      inputPlaceholder: '如：呼叫三次未到'
    });
    if (!value || !value.trim()) {
      ElMessage.warning('过号必须填写原因');
      return;
    }
    await overdueQueue(row.id, value.trim());
    ElMessage.success('已标记过号');
    if (selected.value?.id === row.id)
      selected.value = null;
    await reload();
  } catch (error) {
    // 错误已由全局拦截器统一提示
  }
};
/**
 * 重新入队（2026-10-22 拆分两种语义）：
 * mode=1 置为优先 —— 候诊中患者由护士提上来，插到队首；
 * mode=2 过号回队 —— 过号患者回来了，排到队尾（不插队，避免挤掉正常排队的人）。
 */
const handleRejoin = async (row, mode) => {
  const m = mode ?? (row.queueStatus === 6 ? 2 : 1);
  const overdue = m === 2;
  try {
    await ElMessageBox.confirm(overdue
        ? `将 ${row.patientName}（${row.queueNo}）重新排回候诊队列？会排到当前队尾，不插队`
        : `将 ${row.patientName}（${row.queueNo}）置为优先？插到 ${row.doctorName || '该医生'} 队列队首`, overdue ? '过号回队' : '置为优先', {
      confirmButtonText: overdue ? '确认回队' : '确认优先',
      cancelButtonText: '取消',
      type: 'warning'
    });
    const res = await rejoinQueue(row.id, m);
    // 序号必须原样告诉护士 —— 只说「已优先」没法向患者交代
    const action = overdue ? '已重新入队（队尾）' : '已置为优先';
    ElMessage.success(res.data != null ? `${action}，当前序号 ${res.data}` : (res.message || action));
    if (selected.value?.id === row.id)
      selected.value = null;
    await reload();
  } catch (error) {
    // 错误已由全局拦截器统一提示
  }
};
/** 调号：候诊中患者相对当前位置前移/后移 N 位（step 正=后移，负=前移），边界收口到队首/队尾 */
const moveStep = ref(1);
const handleMove = async (step) => {
  if (!selected.value)
    return;
  try {
    const res = await moveQueue(selected.value.id, step);
    const label = step > 0 ? '后移' : '前移';
    ElMessage.success(res.data != null ? `已${label}，当前序号 ${res.data}` : (res.message || `调号完成（${label}）`));
    selected.value = null;
    await reload();
  } catch (error) {
    // 错误已由全局拦截器统一提示
  }
};
// ========== 改派（目标医生/诊室） ==========
const reassignVisible = ref(false);
const reassignForm = ref({doctorId: '', roomId: ''});
const reassignRooms = ref([]);
const reassignRow = ref(null);
const reassigning = ref(false);
const openReassign = async (row) => {
  reassignRow.value = row;
  reassignForm.value = {
    doctorId: row.doctorId ? String(row.doctorId) : '',
    roomId: row.roomId ? String(row.roomId) : '',
  };
  reassignVisible.value = true;
  try {
    // 诊室下拉：当前诊区的诊室（不分页）
    const res = await getClinicRoomListAll({deptId: deptId.value || undefined, pageSize: 200});
    reassignRooms.value = res.data?.records || res.data || [];
  } catch (error) {
    console.error('加载诊室失败:', error);
    reassignRooms.value = [];
  }
};
const doReassign = async () => {
  if (!reassignRow.value)
    return;
  if (!reassignForm.value.doctorId) {
    ElMessage.warning('请选择目标医生');
    return;
  }
  reassigning.value = true;
  try {
    const res = await reassignQueue(
        reassignRow.value.id,
        Number(reassignForm.value.doctorId),
        reassignForm.value.roomId ? Number(reassignForm.value.roomId) : undefined
    );
    const d = res.data;
    ElMessage.success(d
        ? `已改派到 ${d.doctorName}${d.roomName ? ' / ' + d.roomName : ''}，新排队号 ${d.queueNo}，当前序号 ${d.sequenceNo}`
        : (res.message || '改派完成'));
    reassignVisible.value = false;
    selected.value = null;
    await reload();
  } catch (error) {
    // 错误已由全局拦截器统一提示
  } finally {
    reassigning.value = false;
  }
};
// ========== 分诊卡 ==========
const selected = ref(null);
const triageHistory = ref([]);
const saving = ref(false);
const form = ref({
  temperature: null, pulse: null, respiration: null,
  systolicBp: null, diastolicBp: null, spo2: null,
  remark: '',
});
const onRowChange = async (row) => {
  if (!row)
    return;
  await openTriage(row);
};
const openTriage = async (row) => {
  selected.value = row;
  form.value = {
    temperature: null, pulse: null, respiration: null,
    systolicBp: null, diastolicBp: null, spo2: null,
    remark: '',
  };
  triageHistory.value = [];
  try {
    const res = await getTriageByQueueId(row.id);
    const latest = res.data?.latest;
    triageHistory.value = res.data?.history || [];
    if (latest) {
      // 已分诊过：把上次的值填回来，护士只改变化项
      form.value = {
        temperature: latest.temperature ?? null,
        pulse: latest.pulse ?? null,
        respiration: latest.respiration ?? null,
        systolicBp: latest.systolicBp ?? null,
        diastolicBp: latest.diastolicBp ?? null,
        spo2: latest.spo2 ?? null,
        remark: '',
      };
    }
  } catch (error) {
    console.error('加载分诊记录失败:', error);
  }
};
const clearForm = () => {
  form.value = {
    temperature: null, pulse: null, respiration: null,
    systolicBp: null, diastolicBp: null, spo2: null,
    remark: '',
  };
};
const handleSaveTriage = async () => {
  if (!selected.value) {
    ElMessage.warning('请先在中间队列中选择患者');
    return;
  }
  saving.value = true;
  try {
    // 门诊版不带 triageLevel：后端已支持可空（传了才校验、非空才回写队列）
    const payload = {
      queueId: selected.value.id,
      remark: form.value.remark || undefined,
    };
    for (const k of ['temperature', 'pulse', 'respiration', 'systolicBp', 'diastolicBp', 'spo2']) {
      if (form.value[k] !== null && form.value[k] !== '' && form.value[k] !== undefined) {
        payload[k] = form.value[k];
      }
    }
    const res = await saveTriage(payload);
    ElMessage.success(res.message || '分诊已保存');
    await openTriage(selected.value);
    await Promise.all([loadList(), loadStats()]);
  } catch (error) {
    // 错误已由全局拦截器统一提示
  } finally {
    saving.value = false;
  }
};
// ========== 待签到（已缴费未签到，左栏常驻列表） ==========
const uncheckedList = ref([]);
const uncheckedLoading = ref(false);
const checkInAllLoading = ref(false);
const loadUnchecked = async () => {
  uncheckedLoading.value = true;
  try {
    const res = await getUncheckedList({
      deptId: deptId.value || undefined,
      visitDate: visitDate.value || undefined,
    });
    uncheckedList.value = res.data || [];
  } catch (error) {
    console.error('加载待签到列表失败:', error);
  } finally {
    uncheckedLoading.value = false;
  }
};
const doCheckIn = async (row) => {
  try {
    await checkInByRegist(row.registId);
    ElMessage.success(`${row.patientName} 已签到`);
    await loadUnchecked();
    await reload();
  } catch (error) {
    // 错误已由全局拦截器统一提示
  }
};
const doCheckInAll = async () => {
  if (uncheckedList.value.length === 0) {
    ElMessage.warning('当前没有待签到的患者');
    return;
  }
  checkInAllLoading.value = true;
  let okCount = 0;
  const failed = [];
  for (const row of uncheckedList.value) {
    try {
      await checkInByRegist(row.registId);
      okCount++;
    } catch (error) {
      failed.push(`${row.patientName}：${error.message || '签到失败'}`);
    }
  }
  if (okCount > 0)
    ElMessage.success(`已签到 ${okCount} 人`);
  // 失败原因逐条说出来 —— 只说「部分失败」护士不知道该找谁
  if (failed.length > 0)
    ElMessage.warning(failed.join('；'));
  await loadUnchecked();
  await reload();
  checkInAllLoading.value = false;
};
// ========== 定时刷新 ==========
let timer = null;
onMounted(async () => {
  await loadDepts();
  await loadDoctors();
  await reload();
  // 左栏待签到常驻展示，直接拉名单
  await loadUnchecked();
  // 5 秒刷统计与监控（护士站要挂在墙上），列表不自动刷，避免正在点按钮时行被刷新掉
  timer = setInterval(() => {
    loadStats();
    loadConsulting();
    loadRevisitTimeout();
    loadUnchecked();
  }, 5000);
});
onUnmounted(() => {
  if (timer)
    clearInterval(timer);
});
</script>

<style scoped>
</style>
