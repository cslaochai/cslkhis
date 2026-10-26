<template>
  <div class="space-y-6">
    <!-- 标题 + 在院患者过滤 -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div class="flex items-center gap-3">
        <el-select
            v-model="filters.admissionId"
            class="!w-72"
            clearable
            data-testid="p4-op-admission"
            filterable
            placeholder="按在院患者过滤"
            @change="handleSearch"
        >
          <el-option
              v-for="a in admissions"
              :key="a.admissionId"
              :label="admissionLabel(a)"
              :value="String(a.admissionId)"
          />
        </el-select>
        <el-button v-perm="'ipd:surgery:add'" :icon="Plus" data-testid="p4-op-apply" type="primary"
                   @click="openApply()">
          发起手术申请
        </el-button>
        <el-button :icon="Setting" data-testid="p134-op-rooms" @click="openRooms">手术间管理</el-button>
        <el-button :icon="Refresh" @click="handleSearch">刷新</el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" data-testid="p134-op-tabs" @tab-change="onTabChangeAll">
      <el-tab-pane label="手术申请" name="apply">
        <!-- 统计卡片 -->
        <div class="grid grid-cols-2 gap-4 lg:grid-cols-3">
          <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <p class="text-xs text-slate-500">未完成手术</p>
            <p class="text-lg font-bold text-slate-900" data-testid="p4-op-unfinished">{{ unfinishedCount }}</p>
            <p class="text-[11px] text-slate-400">待排期 + 已排期 + 术前核对完成</p>
          </div>
          <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <p class="text-xs text-slate-500">当前筛选结果</p>
            <p class="text-lg font-bold text-slate-900" data-testid="p4-op-total">{{ total }}</p>
            <p class="text-[11px] text-slate-400">共 {{ total }} 条手术申请</p>
          </div>
          <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
            <p class="text-xs text-slate-500">本页回写链不完整</p>
            <p
                :class="rows.filter(chainBroken).length > 0 ? 'text-red-600' : 'text-slate-900'"
                class="text-lg font-bold"
                data-testid="p4-op-broken"
            >
              {{ rows.filter(chainBroken).length }}
            </p>
            <p class="text-[11px] text-slate-400">已完成但缺首页明细或病历锚点</p>
          </div>
        </div>

        <!-- 筛选 + 表格 -->
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="mb-4 flex flex-wrap items-center gap-3">
            <el-select
                v-model="filters.operationStatus"
                class="!w-36"
                clearable
                data-testid="p4-op-filter-status"
                placeholder="状态"
                @change="handleSearch"
            >
              <el-option :value="0" label="待排期"/>
              <el-option :value="1" label="已排期"/>
              <el-option :value="2" label="术前核对完成"/>
              <el-option :value="3" label="已完成"/>
              <el-option :value="4" label="已取消"/>
            </el-select>
            <el-select
                v-model="filters.surgeonId"
                class="!w-44"
                clearable
                data-testid="p4-op-filter-surgeon"
                filterable
                placeholder="主刀医师"
                @change="handleSearch"
            >
              <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
            </el-select>
            <el-select
                v-model="filters.anesthetistId"
                class="!w-44"
                clearable
                data-testid="p4-op-filter-anesthetist"
                filterable
                placeholder="麻醉医师"
                @change="handleSearch"
            >
              <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>            </el-select>
            <el-select
                v-model="filters.operationRoom"
                class="!w-36"
                clearable
                data-testid="p4-op-filter-room"
                filterable
                placeholder="手术间"
                @change="handleSearch"
            >
              <el-option v-for="r in rooms" :key="r" :label="r" :value="r"/>
            </el-select>
            <el-input
                v-model="filters.keyword"
                :prefix-icon="Search"
                class="!w-72"
                clearable
                placeholder="手术单号 / 入院号 / 患者 / 术式 / 主刀"
                @clear="handleSearch"
                @keyup.enter="handleSearch"
            />
            <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetFilters">重置</el-button>
          </div>

          <el-table v-loading="loading" :data="rows" data-testid="p4-op-table" style="width: 100%">
            <el-table-column label="手术单号" prop="applyNo" width="150"/>
            <el-table-column label="患者" min-width="140">
              <template #default="{ row }">
                <div class="text-slate-900">{{ text(row.patientName) }}</div>
                <div class="text-[11px] text-slate-400">{{ text(row.admissionNo) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="拟施 / 实际术式" min-width="200">
              <template #default="{ row }">
                <div class="font-medium text-slate-900">{{ text(row.plannedOperationName) }}</div>
                <div v-if="row.actualOperationName" class="text-[11px] text-slate-500">
                  实际：{{ row.actualOperationName }}
                </div>
                <div class="text-[11px] text-slate-400">
                  {{ text(row.operationLevelText) }}手术 · {{ text(row.incisionLevelText) }}切口 ·
                  {{ text(row.anesthesiaTypeText) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="手术间 / 时段" min-width="180">
              <template #default="{ row }">
                <div class="text-slate-700">{{ text(row.operationRoom) }}</div>
                <div class="text-[11px] text-slate-400">{{ text(row.plannedTimeText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="主刀 / 跟台" min-width="150">
              <template #default="{ row }">
                <span class="text-slate-700">{{ text(row.surgeonName) }}</span>
                <div v-if="row.isMain === 1" class="text-[11px] text-slate-400">主要手术</div>
                <div v-if="row.circulatingNurseName || row.scrubNurseName" class="mt-1 text-[11px] text-slate-500">
                  巡 {{ text(row.circulatingNurseName) }} · 洗 {{ text(row.scrubNurseName) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="麻醉" min-width="130">
              <template #default="{ row }">
                <span class="text-slate-700">{{ text(row.anesthetistName) }}</span>
                <div class="text-[11px] text-slate-400">{{ text(row.anesthesiaTypeText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="状态 / 进度" min-width="190">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.operationStatus)" effect="plain" size="small">
                  {{ text(row.operationStatusText) }}
                </el-tag>
                <div class="mt-1 text-[11px] text-slate-500">{{ stageText(row) }}</div>
                <div v-if="row.waitText && row.operationStatus !== 3" class="text-[11px] text-slate-400">
                  {{ row.waitText }}
                </div>
                <div v-if="row.stalled" class="mt-1 text-[11px] font-medium text-red-600">
                  <el-icon class="mr-1 align-middle">
                    <Warning/>
                  </el-icon>
                  {{ text(row.stalledText) }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="术前核对" min-width="170">
              <template #default="{ row }">
                <div v-if="!row.preopCheckItems" class="text-[11px] text-slate-400">—（尚未核对）</div>
                <div v-else class="text-[11px] leading-5 text-slate-600">
                  {{ text(row.preopCheckItemsText) }}
                  <div v-if="row.preopCheckDoctorName" class="text-slate-400">
                    由 {{ row.preopCheckDoctorName }} 于 {{ fmt(row.preopCheckTime) }} 核对
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="回写（首页明细 / 病历）" min-width="180">
              <template #default="{ row }">
                <div v-if="!row.recordNo" class="text-[11px] text-slate-400">未回写（手术未完成）</div>
                <template v-else>
                  <div class="text-[11px] text-slate-700">病历号 {{ row.recordNo }}</div>
                  <div class="text-[11px] text-slate-400">
                    首页手术明细 {{ row.operationId ? '已写入' : '缺失' }}
                  </div>
                </template>
              </template>
            </el-table-column>
            <el-table-column align="center" fixed="right" label="操作" width="290">
              <template #default="{ row }">
                <el-button
                    v-if="row.canSchedule"
                    v-perm="'ipd:surgery:edit'"
                    data-testid="p4-op-schedule"
                    link
                    type="primary"
                    @click="openSchedule(row)"
                >
                  {{ row.operationStatus === 0 ? '排台' : '改期' }}
                </el-button>
                <el-button
                    v-if="row.canPreopCheck"
                    v-perm="'ipd:surgery:edit'"
                    data-testid="p4-op-preopcheck"
                    link
                    type="warning"
                    @click="openPreopCheck(row)"
                >
                  术前核对
                </el-button>
                <el-button
                    v-if="row.canFinish"
                    v-perm="'ipd:surgery:edit'"
                    data-testid="p4-op-finish"
                    link
                    type="success"
                    @click="openFinish(row)"
                >
                  登记完成
                </el-button>
                <el-button
                    v-if="row.canEdit"
                    v-perm="'ipd:surgery:edit'"
                    data-testid="p4-op-edit"
                    link
                    type="primary"
                    @click="openApply(row)"
                >
                  修改
                </el-button>
                <el-button
                    v-if="row.canCancel"
                    v-perm="'ipd:surgery:delete'"
                    data-testid="p4-op-cancel"
                    link
                    type="danger"
                    @click="handleCancel(row)"
                >
                  取消
                </el-button>
                <el-button
                    v-if="row.operationStatus === 1 || row.operationStatus === 2"
                    v-perm="['ipd:surgery:edit', 'ipd:anesthesia:edit']"
                    data-testid="p134-op-safety"
                    link
                    type="primary"
                    @click="openSafety(row)"
                >
                  三方核查
                </el-button>
                <el-button data-testid="p4-op-detail" link type="info" @click="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400" data-testid="p4-op-empty">
                暂无手术申请（点击右上角「发起手术申请」开始）
              </div>
            </template>
          </el-table>

          <div class="mt-4 flex justify-end">
            <el-pagination
                v-model:current-page="pageNum"
                v-model:page-size="pageSize"
                :page-sizes="PAGE_SIZES"
                :total="total"
                layout="total, sizes, prev, pager, next"
                @current-change="loadList"
                @size-change="handleSearch"
            />
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="排台总表" name="matrix">
        <div class="mb-4 flex flex-wrap items-center gap-3">
          <el-date-picker
              v-model="matrixDate"
              :clearable="false"
              class="!w-40"
              data-testid="p134-mx-date"
              placeholder="总表日期"
              type="date"
              value-format="YYYY-MM-DD"
              @change="loadMatrix"
          />
          <el-button :icon="Refresh" data-testid="p134-mx-refresh" @click="loadMatrix">刷新</el-button>
          <el-button
              v-perm="'ipd:surgery:edit'"
              type="primary"
              plain
              :icon="Setting"
              data-testid="p134-mx-ai"
              @click="openAi"
          >
            AI 排台
          </el-button>
          <span v-if="matrix" class="text-[12px] text-slate-500" data-testid="p134-mx-summary">
            当天已排 {{ matrix.scheduledCount ?? 0 }} 台 · 急诊 {{ matrix.emergencyCount ?? 0 }} 台 ·
            待排期 {{ (matrix.unscheduled || []).length }} 条 · 未登记手术间 {{ (matrix.others || []).length }} 组
          </span>
        </div>
        <div class="mb-2 rounded border border-blue-100 bg-blue-50 px-3 py-2 text-[12px] leading-5 text-blue-700">
          列 = 启用中的手术间（列序可在「手术间管理」调整）；卡片上的「核查 n/3」是三方安全核查进度 ——
          签过一轮的手术，三轮不签满不能登记完成。排到了未登记手术间的手术会落在最后的橙色列里，不丢。
        </div>
        <div v-loading="matrixLoading" class="overflow-x-auto pb-2">
          <div class="flex min-w-max items-start gap-3">
            <div
                v-for="col in matrixColumns"
                :key="`col-${col.roomName}`"
                :class="col.unregistered ? 'border-amber-300' : 'border-slate-200'"
                :data-testid="`p134-mx-col-${col.unregistered ? 'other' : col.roomCode}`"
                class="w-60 shrink-0 rounded-lg border bg-white shadow-sm"
            >
              <div class="border-b border-slate-100 px-3 py-2">
                <div class="text-sm font-semibold text-slate-900">
                  {{ col.roomName }}
                  <span v-if="col.unregistered" class="text-[11px] font-normal text-amber-600">（未登记手术间）</span>
                </div>
                <div class="text-[11px] text-slate-400">
                  {{
                    col.unregistered ? '请到「手术间管理」补录主数据' : `${col.roomCode} · ${col.location || '未填位置'}`
                  }} ·
                  {{ (col.ops || []).length }} 台
                </div>
              </div>
              <div class="space-y-2 p-2">
                <div
                    v-for="(op, opIdx) in col.ops"
                    :key="op.id"
                    :class="op.isEmergency === 1 ? 'border-red-200 bg-red-50' : 'border-slate-200 bg-slate-50'"
                    :data-testid="`p134-mx-op-${op.id}`"
                    class="rounded-md border p-2"
                >
                  <div class="text-[12px] font-semibold text-slate-900">
                    <span class="mr-1 rounded bg-slate-200 px-1 text-[11px] font-semibold text-slate-700"
                          :data-testid="`p134-mx-seq-${op.id}`">第{{ opIdx + 1 }}台</span>
                    {{ hm(op.plannedStartTime) }}~{{ hm(op.plannedEndTime) }}
                    <el-tag v-if="op.isEmergency === 1" effect="plain" size="small" type="danger">急诊</el-tag>
                  </div>
                  <div class="text-[12px] text-slate-700">{{ text(op.patientName) }} · {{
                      text(op.plannedOperationName)
                    }}
                  </div>
                  <div class="mt-1 text-[11px] text-slate-600">
                    术者：{{ text(op.surgeonName) }} · 麻醉：{{ text(op.anesthetistName) }}
                  </div>
                  <div class="mt-1 flex items-center justify-between">
                    <el-tag :type="statusTagType(op.operationStatus)" size="small">{{
                        text(op.operationStatusText)
                      }}
                    </el-tag>
                    <span :class="phaseClass(op)" :data-testid="`p134-mx-phase-${op.id}`"
                          class="text-[11px] font-medium">
                      核查 {{ phaseOf(op) }}/3
                    </span>
                  </div>
                  <div v-if="op.circulatingNurseName || op.scrubNurseName" class="mt-1 text-[11px] text-slate-500">
                    巡回：{{ text(op.circulatingNurseName) }} · 器械/洗手：{{ text(op.scrubNurseName) }}
                  </div>
                  <div class="mt-1 flex flex-wrap gap-x-2">
                    <el-button
                        v-if="op.operationStatus === 1 || op.operationStatus === 2"
                        v-perm="['ipd:surgery:edit', 'ipd:anesthesia:edit']"
                        data-testid="p134-mx-safety"
                        link
                        size="small"
                        type="primary"
                        @click="openSafety(op)"
                    >
                      安全核查
                    </el-button>
                    <el-button
                        v-if="op.canSchedule"
                        v-perm="'ipd:surgery:edit'"
                        link
                        size="small"
                        type="warning"
                        @click="openSchedule(op)"
                    >
                      改期
                    </el-button>
                    <el-button
                        v-if="op.canPreopCheck"
                        v-perm="'ipd:surgery:edit'"
                        link
                        size="small"
                        type="warning"
                        @click="openPreopCheck(op)"
                    >
                      术前核对
                    </el-button>
                  </div>
                </div>
                <div v-if="!(col.ops || []).length" class="py-4 text-center text-[11px] text-slate-300">今日无排台</div>
              </div>
            </div>

            <div class="w-64 shrink-0 rounded-lg border border-slate-200 bg-white shadow-sm"
                 data-testid="p134-mx-col-pending">
              <div class="border-b border-slate-100 px-3 py-2">
                <div class="text-sm font-semibold text-slate-900">待排期（{{ (matrix?.unscheduled || []).length }}）</div>
                <div class="text-[11px] text-slate-400">点「排台」把手术放进上面的手术间列</div>
              </div>
              <div class="space-y-2 p-2">
                <div
                    v-for="op in matrix?.unscheduled || []"
                    :key="op.id"
                    :data-testid="`p134-mx-pending-${op.id}`"
                    class="rounded-md border border-dashed border-slate-300 bg-white p-2"
                >
                  <div class="text-[12px] text-slate-700">
                    {{ text(op.patientName) }} · {{ text(op.plannedOperationName) }}
                    <el-tag v-if="op.isEmergency === 1" effect="plain" size="small" type="danger">急诊</el-tag>
                  </div>
                  <div class="text-[11px] text-slate-400">{{ text(op.applyDeptName) }} · 申请 {{
                      fmt(op.applyTime)
                    }}
                  </div>
                  <el-button
                      v-if="op.canSchedule"
                      v-perm="'ipd:surgery:edit'"
                      class="mt-1"
                      data-testid="p134-mx-schedule"
                      link
                      size="small"
                      type="primary"
                      @click="openSchedule(op)"
                  >
                    排台
                  </el-button>
                  <div v-else class="mt-1 text-[11px] text-slate-300">无排台权限/已被锁定</div>
                </div>
                <div v-if="!(matrix?.unscheduled || []).length" class="py-4 text-center text-[11px] text-slate-300">
                  没有在等待排台的手术
                </div>
              </div>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="利用率" name="utilization">
        <div class="query-grid-wrap" style="margin-bottom: 12px;">
          <div class="query-grid">
            <el-form-item label="统计区间">
              <el-date-picker
                  v-model="utilRange"
                  type="daterange"
                  value-format="YYYY-MM-DD"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 300px;"
              />
            </el-form-item>
          </div>
          <div class="query-grid-ops">
            <el-button type="primary" :loading="utilLoading" data-testid="p134-util-query" @click="loadUtilization">查询</el-button>
          </div>
        </div>

        <el-row :gutter="12" class="mb-3">
          <el-col :span="6">
            <div class="util-stat-card">
              <div class="util-stat-value">{{ utilTotal?.scheduledCount ?? 0 }}</div>
              <div class="util-stat-label">区间手术台数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="util-stat-card">
              <div class="util-stat-value">{{ utilTotal?.emergencyCount ?? 0 }}</div>
              <div class="util-stat-label">其中急诊台数</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="util-stat-card">
              <div class="util-stat-value">{{ fmtMinutes(utilTotal?.occupiedMinutes) }}</div>
              <div class="util-stat-label">占用时长</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="util-stat-card">
              <div class="util-stat-value">{{ utilTotal?.utilizationRate ?? 0 }}%</div>
              <div class="util-stat-label">全院利用率</div>
            </div>
          </el-col>
        </el-row>

        <el-table :data="utilRows" border stripe size="small" data-testid="p134-util-table" style="width: 100%">
          <el-table-column prop="roomName" label="手术间" min-width="130" />
          <el-table-column prop="roomCode" label="编码" width="90" />
          <el-table-column prop="location" label="位置" min-width="120" show-overflow-tooltip />
          <el-table-column prop="scheduledCount" label="台数" width="70" align="center" />
          <el-table-column prop="emergencyCount" label="急诊台" width="80" align="center" />
          <el-table-column label="占用时长" width="120" align="right">
            <template #default="{ row }">{{ fmtMinutes(row.occupiedMinutes) }}</template>
          </el-table-column>
          <el-table-column label="可用时长" width="120" align="right">
            <template #default="{ row }">{{ fmtMinutes(row.capacityMinutes) }}</template>
          </el-table-column>
          <el-table-column label="利用率" min-width="170">
            <template #default="{ row }">
              <div class="util-cell">
                <el-progress
                    :percentage="Number(row.utilizationRate ?? 0)"
                    :color="utilColor(row.utilizationRate)"
                    :stroke-width="8"
                    style="flex: 1;"
                />
                <span class="util-pct">{{ row.utilizationRate ?? 0 }}%</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <SurgeryApplyEdit
        v-model="applyVisible"
        :admissions="admissions"
        :preset-admission-id="filters.admissionId"
        :row="applyRow"
        @saved="refreshAfterChange"
    />

    <SurgeryRoomAssign
        v-model="scheduleVisible"
        :employees="employees"
        :rooms="rooms"
        :row="scheduleRow"
        @saved="refreshAfterChange"
    />

    <SurgeryAiSuggest
        v-model="aiVisible"
        :initial-date="matrixDate"
        @saved="refreshAfterChange"
    />

    <SurgeryPreopCheck
        v-model="checkVisible"
        :row="checkRow"
        @saved="refreshAfterChange"
    />

    <SurgeryFinish
        v-model="finishVisible"
        :row="finishRow"
        @saved="refreshAfterChange"
    />

    <SurgeryDetail
        v-model="detailVisible"
        :id="detailId"
        :row="detailRow"
    />

    <SurgerySafetyCheck
        v-model="safetyVisible"
        :employees="employees"
        :row="safetyRow"
        @saved="refreshAfterSafety"
    />

    <SurgeryRoomManage
        v-model="roomVisible"
        @changed="loadMatrixIfOpen"
        @saved="onRoomSaved"
    />
  </div>
</template>

<script setup>
/**
 * 住院手术闭环（P4.3：申请 → 排台 → 术前核对 → 完成 → 回写病案首页手术明细）
 *
 * 这个页面替代了原来的假页面：它拉的是 `chargeType=5` 的**收费记录**，
 * 然后把术式写成「手术治疗」、主刀写成「-」、时间写成 08:00-10:00、类型写成「择期」——
 * 整页数据没有一个字来自真实手术。按项目规范（AGENTS.md §2），这种页面必须重写为真实接口驱动。
 *
 * 八条口径：
 * 1. 状态机：0-待排期 → 1-已排期 → 2-术前核对完成 → 3-已完成；0/1 → 4-已取消。
 * 2. **按钮可用性由后端给**（canSchedule / canPreopCheck / canFinish / canCancel / canEdit），
 *    不按 operationStatus 码值 switch，也不在本地拦截（本地拦截会掩盖后端规则的失效）。
 * 3. **发起 ≠ 排台 ≠ 上台**：申请只登记"要做什么手术、为什么"；手术间/时段/主刀是手术室的动作。
 * 4. 排台会被后端校验「同手术间时段重叠」，拒绝对文案里会点明和哪一台撞了 —— 直接展示。
 * 5. **术前核对 4 项必核**（身份与部位 / 术式与知情同意 / 麻醉与麻醉同意 / 过敏史与术前用药），
 *    缺一项后端直接拒。所以这里用勾选框而不是一句话备注。
 * 6. **完成才回写**：一次事务写 ①病案首页手术明细 ②record_type=5 手术记录病历。
 *    列表的「首页明细ID / 病历号」就是这条链的证据，缺任何一个是链断了（后端会标红提示）。
 * 7. 首页记的是**实际做的**手术（与拟施不一致时以实际为准）——这是防"只做探查却编切除术"的关键。
 * 8. 所有 ID 都是字符串（雪花ID），不要 Number()。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search, Setting, Warning} from '@element-plus/icons-vue';
import {
  cancelOperation,
  getOperationApplyListPage,
  getOperationRoomList,
  getOperationRoomUtilization,
  getOperationScheduleMatrix,
  getOperationUnfinishedCount,
} from '@/api/inpatientOperation';
import {getInpatientListPage} from '@/api/inpatient';
import {getEmployeeList} from '@/api/system';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import SurgeryApplyEdit from './SurgeryApplyEdit.vue';
import SurgeryAiSuggest from './SurgeryAiSuggest.vue';
import SurgeryDetail from './SurgeryDetail.vue';
import SurgeryFinish from './SurgeryFinish.vue';
import SurgeryPreopCheck from './SurgeryPreopCheck.vue';
import SurgeryRoomAssign from './SurgeryRoomAssign.vue';
import SurgeryRoomManage from './SurgeryRoomManage.vue';
import SurgerySafetyCheck from './SurgerySafetyCheck.vue';

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));
// ---------------- 基础数据 ----------------
const admissions = ref([]);
const employees = ref([]);
const rooms = ref([]);
const admissionLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.deptName || a.wardName || '—'}）`;
const loadBaseData = async () => {
  try {
    const res = await getInpatientListPage({admitStatus: 1, pageNum: 1, pageSize: 200});
    admissions.value = (res.data?.records || []);
  } catch (error) {
    console.error('加载在院患者失败:', error);
  }
  try {
    const res = await getEmployeeList({});
    employees.value = (res.data || []);
  } catch (error) {
    console.error('加载员工失败:', error);
  }
  try {
    // 手术间主数据（sql/134）+ 历史自由文本合并后由后端 roomList 给出，前端不再写候选数组
    const res = await getOperationRoomList();
    rooms.value = (res.data || []);
  } catch (error) {
    console.error('加载手术间候选失败:', error);
    rooms.value = [];
  }
};
// ---------------- 列表 ----------------
const rows = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(DEFAULT_PAGE_SIZE);
const loading = ref(false);
const unfinishedCount = ref(0);
const filters = reactive({
  admissionId: '',
  operationStatus: '',
  surgeonId: '',
  anesthetistId: '',
  operationRoom: '',
  keyword: '',
});
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getOperationApplyListPage({
      admissionId: filters.admissionId || undefined,
      operationStatus: filters.operationStatus === '' ? undefined : filters.operationStatus,
      surgeonId: filters.surgeonId || undefined,
      anesthetistId: filters.anesthetistId || undefined,
      operationRoom: filters.operationRoom || undefined,
      keyword: filters.keyword || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    });
    rows.value = (res.data?.records || []);
    total.value = Number(res.data?.total || 0);
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false;
  }
};
const loadUnfinishedCount = async () => {
  try {
    const res = await getOperationUnfinishedCount({});
    unfinishedCount.value = Number(res.data || 0);
  } catch (error) {
    console.error('加载未完成手术数失败:', error);
  }
};
/** 每台手术的进行位置（列表里一眼看出卡在哪一步）——纯后端文案拼装，前端不加业务判断 */
const stageText = (row) => {
  if (row.operationStatus === 0)
    return '① 等待手术室排台';
  if (row.operationStatus === 1)
    return '② 已排台，等待术前核对';
  if (row.operationStatus === 2)
    return '③ 已核对，等待上台并登记完成';
  if (row.operationStatus === 3)
    return '④ 已完成并回写';
  return '已取消';
};
const handleSearch = () => {
  pageNum.value = 1;
  loadList();
};
const resetFilters = () => {
  filters.admissionId = '';
  filters.operationStatus = '';
  filters.surgeonId = '';
  filters.anesthetistId = '';
  filters.operationRoom = '';
  filters.keyword = '';
  pageNum.value = 1;
  loadList();
};
// ---------------- 一、发起 / 修改手术申请 ----------------
const applyVisible = ref(false);
const applyRow = ref(null);
const openApply = (row) => {
  applyRow.value = row || null;
  applyVisible.value = true;
};
// ---------------- 二、排台 ----------------
const scheduleVisible = ref(false);
const scheduleRow = ref(null);
const openSchedule = (row) => {
  scheduleRow.value = row;
  scheduleVisible.value = true;
};
// ---------------- 三、术前核对 ----------------
const checkVisible = ref(false);
const checkRow = ref(null);
const openPreopCheck = (row) => {
  checkRow.value = row;
  checkVisible.value = true;
};
// ---------------- 四、完成（回写首页明细 + 手术记录病历） ----------------
const finishVisible = ref(false);
const finishRow = ref(null);
const openFinish = (row) => {
  finishRow.value = row;
  finishVisible.value = true;
};
/** 弹框里任何一次写库之后，列表 + 未完成数 + 打开着的总表都要跟着刷 */
const refreshAfterChange = async () => {
  await Promise.all([loadList(), loadUnfinishedCount(), loadMatrixIfOpen()]);
};
/** 核查签核不改变状态机（未完成数、申请列表的进度都不动），所以只刷列表和总表 */
const refreshAfterSafety = async () => {
  await Promise.all([loadList(), loadMatrixIfOpen()]);
};
// ---------------- 取消 / 详情 ----------------
const handleCancel = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`确认取消手术申请 ${row.applyNo || ''}（${row.patientName || ''} ${row.plannedOperationName || ''}）？`
        + '已排台的手术取消后会释放手术间时段；术前核对完成后不能再取消。', '取消手术申请', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      inputPlaceholder: '取消原因（必填，如：患者体温升高，暂停手术）',
      inputValidator: (v) => (v && v.trim() ? true : '取消原因不能为空'),
    });
    await cancelOperation({applyId: row.id, cancelReason: value.trim()});
    ElMessage.success('手术申请已取消');
    await Promise.all([loadList(), loadUnfinishedCount(), loadMatrixIfOpen()]);
  } catch (error) {
    if (error === 'cancel' || error === 'close')
      return;
    
  }
};
const detailVisible = ref(false);
const detailId = ref('');
const detailRow = ref(null);
const openDetail = (row) => {
  detailId.value = row.id;
  detailRow.value = row;
  detailVisible.value = true;
};
/** 已完成但缺首页明细/病历锚点 = 链断了。后端已标 stalled，这里显式渲染出来，不静默。 */
const chainBroken = (row) => row.operationStatus === 3 && (!row.operationId || !row.recordId);
const statusTagType = (status) => {
  if (status === 0)
    return 'info';
  if (status === 1)
    return 'warning';
  if (status === 2)
    return 'primary';
  if (status === 3)
    return 'success';
  return 'info';
};
// ---------------- 页签与排台总表（P134.1） ----------------
const activeTab = ref('apply');
const matrix = ref(null);
const matrixLoading = ref(false);
const today = () => {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
};
const matrixDate = ref(today());
const loadMatrix = async () => {
  matrixLoading.value = true;
  try {
    const res = await getOperationScheduleMatrix(matrixDate.value);
    matrix.value = (res.data || {date: matrixDate.value, rooms: [], others: [], unscheduled: []});
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    matrixLoading.value = false;
  }
};
const onTabChange = (name) => {
  if (name === 'matrix' && !matrix.value)
    loadMatrix();
};
const onTabChangeAll = (name) => {
  onTabChange(name);
  onTabChangeUtil(name);
};
const hm = (t) => (t ? String(t).replace('T', ' ').slice(11, 16) : '—');
const phaseOf = (op) => Number(op.safetyCheckPhases ?? 0);
const phaseClass = (op) => phaseOf(op) >= 3 ? 'text-emerald-600' : phaseOf(op) > 0 ? 'text-amber-600' : 'text-slate-400';
/** 手术间列 + 未登记手术间兜底列拼成一排渲染（others 恒在正规列之后） */
const matrixColumns = computed(() => {
  if (!matrix.value)
    return [];
  const rooms = (matrix.value.rooms || []).map((c) => ({...c, unregistered: false}));
  const others = (matrix.value.others || []).map((c) => ({...c, unregistered: true}));
  return [...rooms, ...others];
});
const roomVisible = ref(false);
const openRooms = () => {
  roomVisible.value = true;
};
const aiVisible = ref(false);
const openAi = () => {
  aiVisible.value = true;
};
const onRoomSaved = async () => {
  await Promise.all([loadList(), loadMatrixIfOpen()]);
};
const loadMatrixIfOpen = async () => {
  if (activeTab.value === 'matrix')
    await loadMatrix();
};
const safetyVisible = ref(false);
const safetyRow = ref(null);
const openSafety = (row) => {
  safetyRow.value = row;
  safetyVisible.value = true;
};
// ---------------- 手术间利用率（P134.1.3） ----------------
const utilRange = ref([today(), today()]);
const utilRows = ref([]);
const utilLoading = ref(false);
const utilTotal = computed(() => utilRows.value.find((r) => r.roomName === '全院合计') || null);
const fmtMinutes = (m) => {
  const n = Number(m || 0);
  const h = Math.floor(n / 60);
  return h > 0 ? `${h}h${n % 60 ? n % 60 + 'min' : ''}` : `${n}min`;
};
const utilColor = (rate) => {
  const r = Number(rate || 0);
  if (r >= 70) return '#e64340';
  if (r >= 40) return '#e6a23c';
  return '#0E9488';
};
const loadUtilization = async () => {
  utilLoading.value = true;
  try {
    const [from, to] = utilRange.value || [today(), today()];
    const res = await getOperationRoomUtilization(from, to);
    utilRows.value = (res.data || []).filter((r) => r.roomName !== '未登记手术间（手工名）');
  } catch (error)  /* 错误已由全局拦截器统一提示 */{
    
  } finally {
    utilLoading.value = false;
  }
};
const onTabChangeUtil = (name) => {
  if (name === 'utilization' && !utilRows.value.length)
    loadUtilization();
};
onMounted(async () => {
  await loadBaseData();
  await Promise.all([loadList(), loadUnfinishedCount()]);
});
</script>

<style scoped>
.util-stat-card {
  background: #f7f9fc;
  border: 1px solid #e5e9f0;
  border-radius: 8px;
  padding: 12px 16px;
}
.util-stat-value {
  font-size: 22px;
  font-weight: 600;
  color: #1269B5;
}
.util-stat-label {
  font-size: 12px;
  color: #8a94a6;
  margin-top: 2px;
}
.util-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}
.util-pct {
  font-size: 12px;
  color: #475569;
  white-space: nowrap;
}
</style>
