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

    <el-tabs v-model="activeTab" data-testid="p134-op-tabs" @tab-change="onTabChange">
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
            <el-table-column label="主刀" width="100">
              <template #default="{ row }">
                <span class="text-slate-700">{{ text(row.surgeonName) }}</span>
                <div v-if="row.isMain === 1" class="text-[11px] text-slate-400">主要手术</div>
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
                    v-for="op in col.ops"
                    :key="op.id"
                    :class="op.isEmergency === 1 ? 'border-red-200 bg-red-50' : 'border-slate-200 bg-slate-50'"
                    :data-testid="`p134-mx-op-${op.id}`"
                    class="rounded-md border p-2"
                >
                  <div class="text-[12px] font-semibold text-slate-900">
                    {{ hm(op.plannedStartTime) }}~{{ hm(op.plannedEndTime) }}
                    <el-tag v-if="op.isEmergency === 1" effect="plain" size="small" type="danger">急诊</el-tag>
                  </div>
                  <div class="text-[12px] text-slate-700">{{ text(op.patientName) }} · {{
                      text(op.plannedOperationName)
                    }}
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
    </el-tabs>

    <!-- 发起 / 修改手术申请 -->
    <el-dialog
        v-model="applyVisible"
        :title="applyForm.id ? '修改手术申请' : '发起手术申请'"
        data-testid="p4-op-apply-dialog"
        width="680px"
    >
      <el-form label-width="110px">
        <el-form-item label="在院患者" required>
          <el-select
              v-model="applyForm.admissionId"
              :disabled="!!applyForm.id"
              class="!w-full"
              data-testid="p4-op-apply-admission"
              filterable
              placeholder="选择在院患者"
          >
            <el-option
                v-for="a in admissions"
                :key="a.admissionId"
                :label="admissionLabel(a)"
                :value="String(a.admissionId)"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="currentAdmission" label="申请科室">
          <span class="text-slate-700">
            {{ text(currentAdmission.deptName) }} / {{ text(currentAdmission.wardName) }} /
            {{ text(currentAdmission.bedNo) }}床
          </span>
          <span class="ml-2 text-[11px] text-slate-400">申请科室取患者当前科室，服务端写入，不需要填</span>
        </el-form-item>
        <el-form-item label="拟施手术" required>
          <el-input
              v-model="applyForm.plannedOperationName"
              data-testid="p4-op-apply-name"
              maxlength="200"
              placeholder="如：腹腔镜胆囊切除术"
          />
        </el-form-item>
        <el-form-item label="手术编码">
          <el-input v-model="applyForm.plannedOperationCode" placeholder="ICD-9-CM-3（可空，但不建议）"/>
        </el-form-item>
        <el-form-item label="手术级别">
          <el-select v-model="applyForm.operationLevel" class="!w-full" clearable placeholder="选择级别">
            <el-option :value="1" label="一级"/>
            <el-option :value="2" label="二级"/>
            <el-option :value="3" label="三级"/>
            <el-option :value="4" label="四级"/>
          </el-select>
        </el-form-item>
        <el-form-item label="切口等级">
          <el-select v-model="applyForm.incisionLevel" class="!w-full" clearable placeholder="选择切口等级">
            <el-option :value="0" label="0类"/>
            <el-option :value="1" label="Ⅰ类"/>
            <el-option :value="2" label="Ⅱ类"/>
            <el-option :value="3" label="Ⅲ类"/>
          </el-select>
        </el-form-item>
        <el-form-item label="麻醉方式">
          <el-select v-model="applyForm.anesthesiaType" class="!w-full" clearable placeholder="选择麻醉方式">
            <el-option :value="1" label="全身麻醉"/>
            <el-option :value="2" label="椎管内麻醉"/>
            <el-option :value="3" label="神经阻滞麻醉"/>
            <el-option :value="4" label="局部麻醉"/>
            <el-option :value="5" label="其他"/>
          </el-select>
        </el-form-item>
        <el-form-item label="术前诊断" required>
          <el-input
              v-model="applyForm.preopDiagnosis"
              :rows="2"
              data-testid="p4-op-apply-diagnosis"
              placeholder="回写病历的「术前诊断」要素取自这里"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="手术指征" required>
          <el-input
              v-model="applyForm.operationReason"
              :rows="3"
              data-testid="p4-op-apply-reason"
              placeholder="为什么必须开这一刀（如：胆囊结石反复发作伴胆囊壁增厚）"
              type="textarea"
          />
        </el-form-item>
        <el-form-item label="急诊 / 主要">
          <el-radio-group v-model="applyForm.isEmergency" class="mr-4">
            <el-radio :value="0">择期</el-radio>
            <el-radio :value="1">急诊</el-radio>
          </el-radio-group>
          <el-radio-group v-model="applyForm.isMain">
            <el-radio :value="1">主要手术</el-radio>
            <el-radio :value="0">次要手术</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.remark" :rows="2" placeholder="可空" type="textarea"/>
        </el-form-item>
      </el-form>
      <div class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] leading-5 text-amber-700">
        <el-icon class="mr-1 align-middle">
          <Warning/>
        </el-icon>
        提交后手术<b>尚未排台</b>：手术间、时段、主刀由手术室在「排台」里指定。
        同一次住院只允许一条「主要手术」申请（首页主要手术只能有 1 条）。
      </div>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button v-perm="['ipd:surgery:add','ipd:surgery:edit']" :loading="applySubmitting" data-testid="p4-op-apply-submit"
                   type="primary" @click="submitApply">
          {{ applyForm.id ? '保存修改' : '提交手术申请' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 排台 -->
    <el-dialog v-model="scheduleVisible" data-testid="p4-op-schedule-dialog" title="手术室排台" width="620px">
      <div v-if="scheduleTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div class="text-slate-900">
            {{ text(scheduleTarget.patientName) }}（{{ text(scheduleTarget.admissionNo) }}）
            <el-tag v-if="scheduleTarget.isEmergency === 1" class="ml-2" size="small" type="danger">急诊</el-tag>
          </div>
          <div class="mt-1 text-slate-700">
            {{ text(scheduleTarget.plannedOperationName) }} ·
            {{ text(scheduleTarget.operationLevelText) }}手术 ·
            {{ text(scheduleTarget.anesthesiaTypeText) }}
          </div>
          <div class="text-[12px] text-slate-500">术前诊断：{{ text(scheduleTarget.preopDiagnosis) }}</div>
        </div>
        <el-form label-width="100px">
          <el-form-item label="手术间" required>
            <el-select
                v-model="scheduleForm.operationRoom"
                allow-create
                class="!w-full"
                data-testid="p4-op-schedule-room"
                default-first-option
                filterable
                placeholder="选择或输入手术间"
            >
              <el-option v-for="r in rooms" :key="r" :label="r" :value="r"/>
            </el-select>
          </el-form-item>
          <el-form-item label="计划开始" required>
            <el-date-picker
                v-model="scheduleForm.plannedStartTime"
                class="!w-full"
                data-testid="p4-op-schedule-start"
                placeholder="选择开始时间"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="计划结束" required>
            <el-date-picker
                v-model="scheduleForm.plannedEndTime"
                class="!w-full"
                data-testid="p4-op-schedule-end"
                placeholder="选择结束时间"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="主刀医师" required>
            <el-select
                v-model="scheduleForm.surgeonId"
                class="!w-full"
                data-testid="p4-op-schedule-surgeon"
                filterable
                placeholder="选择主刀医师"
            >
              <el-option
                  v-for="e in employees"
                  :key="e.id"
                  :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                  :value="String(e.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="助手">
            <el-input v-model="scheduleForm.assistantName" placeholder="多人用逗号分隔，可空"/>
          </el-form-item>
          <el-form-item label="麻醉医师">
            <el-select
                v-model="scheduleForm.anesthetistId"
                class="!w-full"
                clearable
                filterable
                placeholder="可空（未指定）"
            >
              <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="排台备注">
            <el-input v-model="scheduleForm.scheduleRemark" :rows="2" placeholder="可空" type="textarea"/>
          </el-form-item>
        </el-form>
        <el-alert :closable="false" show-icon type="info">
          同一手术间、时间区间重叠的在途手术会被后端拒绝（并发手术台会给不出资源）。
          端点相接不算冲突：上一台 10:00 结束、下一台 10:00 开始是允许的。
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="scheduleVisible = false">取消</el-button>
        <el-button v-perm="'ipd:surgery:edit'" :loading="scheduleSubmitting" data-testid="p4-op-schedule-submit"
                   type="primary" @click="submitSchedule">
          确认排台
        </el-button>
      </template>
    </el-dialog>

    <!-- 术前核对 -->
    <el-dialog v-model="checkVisible" data-testid="p4-op-check-dialog" title="术前核对（手术安全核查）" width="620px">
      <div v-if="checkTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div class="text-slate-900">{{ text(checkTarget.patientName) }}</div>
          <div class="text-slate-700">{{ text(checkTarget.plannedOperationName) }}</div>
          <div class="text-[12px] text-slate-500">
            {{ text(checkTarget.operationRoom) }} · {{ text(checkTarget.plannedTimeText) }} ·
            主刀 {{ text(checkTarget.surgeonName) }}
          </div>
        </div>
        <div class="text-[12px] text-slate-500">
          标 <span class="text-red-500">*</span> 的是必核项，缺任何一项都无法提交 ——
          术前核对的价值就在于"在切皮之前逐项确认过"。
        </div>
        <el-checkbox-group v-model="checkForm.items" class="flex flex-col gap-2">
          <el-checkbox
              v-for="i in checkItemOptions"
              :key="i.code"
              :data-testid="`p4-op-check-item-${i.code}`"
              :value="i.code"
          >
            <span class="text-slate-700">{{ i.label }}</span>
            <span v-if="i.required" class="ml-1 text-red-500">*</span>
          </el-checkbox>
        </el-checkbox-group>
        <el-form label-width="100px">
          <el-form-item label="异常说明">
            <el-input
                v-model="checkForm.preopNote"
                :rows="2"
                data-testid="p4-op-check-note"
                placeholder="正常可空；有异常必须写（如：术中需备血 4U、青霉素过敏需换用头孢）"
                type="textarea"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="checkVisible = false">取消</el-button>
        <el-button
            v-perm="'ipd:surgery:edit'"
            :loading="checkSubmitting"
            data-testid="p4-op-check-submit"
            type="primary"
            @click="submitPreopCheck"
        >
          确认已完成核对
        </el-button>
      </template>
    </el-dialog>

    <!-- 登记手术完成 -->
    <el-dialog
        v-model="finishVisible"
        data-testid="p4-op-finish-dialog"
        title="登记手术完成（回写首页明细 + 手术记录病历）"
        width="760px"
    >
      <div v-if="finishTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div class="text-slate-900">{{ text(finishTarget.patientName) }}</div>
          <div class="text-slate-700">
            拟施：{{ text(finishTarget.plannedOperationName) }} ·
            {{ text(finishTarget.operationRoom) }} ·
            主刀 {{ text(finishTarget.surgeonName) }}
          </div>
          <div class="text-[12px] text-slate-500">
            术前核对：{{ text(finishTarget.preopCheckItemsText) }}
          </div>
        </div>
        <el-form label-width="110px">
          <el-form-item label="实际手术" required>
            <el-input
                v-model="finishForm.actualOperationName"
                data-testid="p4-op-finish-name"
                maxlength="200"
                placeholder="实际做的是什么（与拟施不一致时以这里为准，首页记的是它）"
            />
          </el-form-item>
          <el-form-item label="实际编码">
            <el-input v-model="finishForm.actualOperationCode" placeholder="ICD-9-CM-3（可空）"/>
          </el-form-item>
          <el-form-item label="开始时间" required>
            <el-date-picker
                v-model="finishForm.operationStartTime"
                class="!w-full"
                data-testid="p4-op-finish-start"
                placeholder="切皮时间"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="结束时间" required>
            <el-date-picker
                v-model="finishForm.operationEndTime"
                class="!w-full"
                data-testid="p4-op-finish-end"
                placeholder="关腹/关胸时间"
                type="datetime"
                value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          <el-form-item label="术中出血量">
            <el-input v-model.number="finishForm.bloodLoss" placeholder="ml（0 也是有效观测值）"/>
          </el-form-item>
          <el-form-item label="术中所见" required>
            <el-input
                v-model="finishForm.intraopFindings"
                :rows="3"
                data-testid="p4-op-finish-findings"
                placeholder="四核对里「编码有没有病历支持」追的就是这一段"
                type="textarea"
            />
          </el-form-item>
          <el-form-item label="手术经过" required>
            <el-input
                v-model="finishForm.intraopProcedure"
                :rows="3"
                data-testid="p4-op-finish-procedure"
                placeholder="操作步骤"
                type="textarea"
            />
          </el-form-item>
          <el-form-item label="术后处理" required>
            <el-input
                v-model="finishForm.postopNote"
                :rows="3"
                data-testid="p4-op-finish-postop"
                placeholder="术后处理与注意事项（缺了等于「做完就不管了」）"
                type="textarea"
            />
          </el-form-item>
          <el-form-item label="标本送检">
            <el-input v-model="finishForm.specimenSent" placeholder="无标本请写「无」，不要留空"/>
          </el-form-item>
        </el-form>
        <el-alert :closable="false" show-icon type="warning">
          提交即回写两份正式文书：病案首页手术明细 + record_type=5 手术记录病历（签名为主刀医师）。
          回写失败整笔回滚，不会出现"状态已完成、首页和病历里查不到"。
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="finishVisible = false">取消</el-button>
        <el-button
            v-perm="'ipd:surgery:edit'"
            :loading="finishSubmitting"
            data-testid="p4-op-finish-submit"
            type="primary"
            @click="submitFinish"
        >
          确认完成并回写
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" data-testid="p4-op-detail-dialog" title="手术详情" width="820px">
      <div v-if="detail" class="space-y-4">
        <div class="flex items-center gap-3">
          <el-icon class="text-slate-400">
            <Scissor/>
          </el-icon>
          <span class="text-base font-semibold text-slate-900">{{ text(detail.applyNo) }}</span>
          <el-tag :type="statusTagType(detail.operationStatus)" size="small">
            {{ text(detail.operationStatusText) }}
          </el-tag>
          <span class="text-[12px] text-slate-400">{{ stageText(detail) }}</span>
        </div>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="患者">{{ text(detail.patientName) }}</el-descriptions-item>
          <el-descriptions-item label="入院号">{{ text(detail.admissionNo) }}</el-descriptions-item>
          <el-descriptions-item label="申请科室">{{ text(detail.applyDeptName) }}</el-descriptions-item>
          <el-descriptions-item label="申请时床位">
            {{ text(detail.applyWardName) }} {{ text(detail.applyBedNo) }}床
          </el-descriptions-item>
          <el-descriptions-item label="申请医生">{{ text(detail.applyDoctorName) }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ fmt(detail.applyTime) }}</el-descriptions-item>
          <el-descriptions-item label="拟施手术">{{ text(detail.plannedOperationName) }}</el-descriptions-item>
          <el-descriptions-item label="手术编码">{{ text(detail.plannedOperationCode) }}</el-descriptions-item>
          <el-descriptions-item label="手术级别">{{ text(detail.operationLevelText) }}</el-descriptions-item>
          <el-descriptions-item label="切口等级">{{ text(detail.incisionLevelText) }}</el-descriptions-item>
          <el-descriptions-item label="麻醉方式">{{ text(detail.anesthesiaTypeText) }}</el-descriptions-item>
          <el-descriptions-item label="急诊 / 主次">
            {{ text(detail.isEmergencyText) }} / {{ text(detail.isMainText) }}
          </el-descriptions-item>
          <el-descriptions-item :span="2" label="术前诊断">{{ text(detail.preopDiagnosis) }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="手术指征">{{ text(detail.operationReason) }}</el-descriptions-item>
          <el-descriptions-item label="手术间">{{ text(detail.operationRoom) }}</el-descriptions-item>
          <el-descriptions-item label="计划时段">{{ text(detail.plannedTimeText) }}</el-descriptions-item>
          <el-descriptions-item label="主刀医师">{{ text(detail.surgeonName) }}</el-descriptions-item>
          <el-descriptions-item label="助手">{{ text(detail.assistantName) }}</el-descriptions-item>
          <el-descriptions-item label="麻醉医师">{{ text(detail.anesthetistName) }}</el-descriptions-item>
          <el-descriptions-item label="排台人 / 时间">
            {{ text(detail.scheduleDoctorName) }} / {{ fmt(detail.scheduleTime) }}
          </el-descriptions-item>
          <el-descriptions-item :span="2" label="术前核对">
            <span :class="detail.preopCheckItems ? 'text-slate-700' : 'text-slate-400'">
              {{ detail.preopCheckItems ? detail.preopCheckItemsText : '尚未核对' }}
            </span>
            <div v-if="detail.preopCheckDoctorName" class="text-[11px] text-slate-400">
              由 {{ detail.preopCheckDoctorName }} 于 {{ fmt(detail.preopCheckTime) }} 核对
            </div>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.preopNote" :span="2" label="核对异常说明">
            {{ detail.preopNote }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.actualOperationName" :span="2" label="实际手术">
            {{ detail.actualOperationName }}
            <span class="ml-2 text-[11px] text-slate-400">
              {{ fmt(detail.operationStartTime) }} ~ {{ fmt(detail.operationEndTime) }}
              （{{ text(detail.durationText) }}）
            </span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.bloodLoss !== undefined && detail.bloodLoss !== null" label="术中出血量">
            {{ detail.bloodLoss }} ml
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.specimenSent" label="标本送检">{{
              detail.specimenSent
            }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.intraopFindings" :span="2" label="术中所见">
            {{ detail.intraopFindings }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.intraopProcedure" :span="2" label="手术经过">
            {{ detail.intraopProcedure }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.postopNote" :span="2" label="术后处理">
            {{ detail.postopNote }}
          </el-descriptions-item>
          <el-descriptions-item :span="2" label="回写病历号">
            <span v-if="detail.recordNo" class="text-slate-700">{{ detail.recordNo }}</span>
            <span v-else class="text-slate-400">尚未回写（手术未完成）</span>
            <span v-if="detail.operationId" class="ml-2 text-[11px] text-slate-400">首页手术明细已写入</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.cancelReason" :span="2" label="取消原因">
            {{ detail.cancelReason }}（{{ text(detail.cancelDoctorName) }} {{ fmt(detail.cancelTime) }}）
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.remark" :span="2" label="备注">{{ detail.remark }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 三方安全核查（P134.2） -->
    <el-dialog v-model="safetyVisible" data-testid="p134-safety-dialog" title="手术安全核查（三方 · 三时段）"
               width="820px">
      <div v-if="safetyTarget" v-loading="safetyLoading" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <div class="text-slate-900">
            {{ text(safetyTarget.patientName) }}
            <el-tag v-if="safetyTarget.isEmergency === 1" class="ml-1" size="small" type="danger">急诊</el-tag>
          </div>
          <div class="text-[12px] text-slate-600">
            {{ text(safetyTarget.plannedOperationName) }} · {{ text(safetyTarget.operationRoom) }} ·
            {{ text(safetyTarget.plannedTimeText) }} · 主刀 {{ text(safetyTarget.surgeonName) }}
          </div>
        </div>
        <div class="text-[12px] leading-5 text-slate-500">
          法定三个时段<b>按顺序</b>签（麻醉实施前 → 手术开始前 → 患者离开手术室前），每轮都要
          <b>手术医师、麻醉医师、手术室护士三方不同人</b>签名；签过的时段<b>不可改不可删</b>。
          签过一轮的手术，三轮不签满不能登记手术完成。
        </div>
        <div class="grid grid-cols-3 gap-3">
          <div
              v-for="card in safetyCards"
              :key="card.phase"
              :class="card.signed
              ? 'border-emerald-200 bg-emerald-50'
              : (safetyForm.phase === card.phase ? 'border-blue-400 bg-white' : 'border-slate-200 bg-white')"
              :data-testid="`p134-safety-card-${card.phase}`"
              class="rounded-lg border p-3"
          >
            <div class="flex items-center justify-between">
              <span class="text-[13px] font-semibold text-slate-900">第{{ card.phase }}轮 · {{ card.phaseText }}</span>
              <el-tag v-if="card.signed" size="small" type="success">已签</el-tag>
            </div>
            <template v-if="card.signed">
              <div class="mt-1 text-[11px] leading-5 text-slate-600">{{ text(card.signed.itemsText) }}</div>
              <div class="mt-1 text-[11px] text-slate-600">
                手术 {{ card.signed.surgeonName || '—' }} · 麻醉 {{ card.signed.anesthetistName || '—' }} ·
                护士 {{ card.signed.nurseName || '—' }}
              </div>
              <div class="text-[11px] text-slate-400">{{ card.signed.checkNo }} · {{ fmt(card.signed.checkTime) }}</div>
              <div v-if="card.signed.note" class="mt-1 text-[11px] text-amber-700">异常：{{ card.signed.note }}</div>
            </template>
            <template v-else>
              <div v-if="!card.canSign" :data-testid="`p134-safety-reason-${card.phase}`"
                   class="mt-2 text-[11px] leading-5 text-slate-400">
                {{ card.cannotSignReason || '当前不可签' }}
              </div>
              <el-button
                  v-else
                  :data-testid="`p134-safety-pick-${card.phase}`"
                  :type="safetyForm.phase === card.phase ? 'primary' : 'default'"
                  class="mt-2"
                  size="small"
                  @click="pickSafetyPhase(card)"
              >
                {{ safetyForm.phase === card.phase ? '正在填写本轮' : '签这一轮' }}
              </el-button>
            </template>
          </div>
        </div>

        <div v-if="currentSafetyCard" class="rounded-lg border border-slate-200 p-3" data-testid="p134-safety-form">
          <div class="mb-2 text-[12px] text-slate-500">
            {{ currentSafetyCard.phaseText }} 核查要点，标 <span class="text-red-500">*</span> 为必核项
          </div>
          <el-checkbox-group v-model="safetyForm.items" class="flex flex-col gap-1">
            <el-checkbox
                v-for="i in currentSafetyCard.items"
                :key="i.code"
                :data-testid="`p134-safety-item-${i.code}`"
                :value="i.code"
            >
              <span class="text-[12px] text-slate-700">{{ i.label }}</span>
              <span v-if="i.required" class="ml-1 text-red-500">*</span>
            </el-checkbox>
          </el-checkbox-group>
          <el-form class="mt-3" label-width="110px">
            <el-form-item label="手术医师签名" required>
              <el-select v-model="safetyForm.surgeonId" class="!w-full" data-testid="p134-safety-surgeon" filterable
                         placeholder="本场手术医师">
                <el-option v-for="e in employees" :key="e.id"
                           :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                           :value="String(e.id)"/>
              </el-select>
            </el-form-item>
            <el-form-item label="麻醉医师签名" required>
              <el-select v-model="safetyForm.anesthetistId" class="!w-full" data-testid="p134-safety-anesthetist" filterable
                         placeholder="在场麻醉医师">
                <el-option v-for="e in employees" :key="e.id"
                           :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                           :value="String(e.id)"/>
              </el-select>
            </el-form-item>
            <el-form-item label="手术室护士签名" required>
              <el-select v-model="safetyForm.nurseId" class="!w-full" data-testid="p134-safety-nurse" filterable
                         placeholder="巡回/器械护士">
                <el-option v-for="e in employees" :key="e.id"
                           :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                           :value="String(e.id)"/>
              </el-select>
            </el-form-item>
            <el-form-item label="异常说明">
              <el-input v-model="safetyForm.note" :rows="2" data-testid="p134-safety-note"
                        placeholder="发现风险/偏差必须写（如：电刀自检异常，已更换）" type="textarea"/>
            </el-form-item>
          </el-form>
        </div>
      </div>
      <template #footer>
        <el-button @click="safetyVisible = false">关闭</el-button>
        <el-button
            v-if="safetyForm.phase"
            v-perm="['ipd:surgery:edit', 'ipd:anesthesia:edit']"
            :loading="safetySubmitting"
            data-testid="p134-safety-submit"
            type="primary"
            @click="submitSafety"
        >
          确认三方已在场并签核
        </el-button>
      </template>
    </el-dialog>

    <!-- 手术间主数据管理（P134.1） -->
    <el-dialog v-model="roomVisible" data-testid="p134-room-dialog" title="手术间管理（排台总表的台）" width="760px">
      <div class="mb-3 flex items-center justify-between">
        <div class="text-[12px] leading-5 text-slate-500">
          总表列 = 启用中的手术间，按排序号排列序。<b>删除是物理删</b>（编码会释放）；只是暂用不到请「停用」。
        </div>
        <el-button v-perm="'ipd:surgery:add'" :icon="Plus" data-testid="p134-room-new" size="small" type="primary"
                   @click="newRoom">
          新增手术间
        </el-button>
      </div>
      <el-table v-loading="roomLoading" :data="roomRows" data-testid="p134-room-table" size="small">
        <el-table-column label="编码" prop="roomCode" width="100"/>
        <el-table-column label="名称" min-width="140" prop="roomName"/>
        <el-table-column label="位置" min-width="140" prop="location">
          <template #default="{ row }">{{ text(row.location) }}</template>
        </el-table-column>
        <el-table-column align="center" label="排序" prop="sortOrder" width="70"/>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{
                row.status === 1 ? '启用' : '停用'
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="操作" width="200">
          <template #default="{ row }">
            <el-button v-perm="'ipd:surgery:add'" :data-testid="`p134-room-edit-${row.id}`" link size="small"
                       type="primary" @click="editRoom(row)">编辑
            </el-button>
            <el-button
                v-perm="'ipd:surgery:add'"
                :data-testid="`p134-room-toggle-${row.id}`"
                :type="row.status === 1 ? 'warning' : 'success'"
                link
                size="small"
                @click="toggleRoomStatus(row, row.status === 1 ? 0 : 1)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
            <el-button v-perm="'ipd:surgery:delete'" :data-testid="`p134-room-del-${row.id}`" link size="small"
                       type="danger" @click="removeRoom(row)">删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-4 text-[12px] text-slate-400">暂无手术间主数据（点「新增手术间」建第一间）</div>
        </template>
      </el-table>

      <div v-if="roomFormVisible" class="mt-4 rounded-lg border border-slate-200 p-4" data-testid="p134-room-form">
        <div class="mb-2 text-sm font-semibold text-slate-900">{{ roomForm.id ? '修改手术间' : '新增手术间' }}</div>
        <el-form label-width="90px">
          <el-form-item label="编码" required>
            <el-input v-model="roomForm.roomCode" data-testid="p134-room-form-code" maxlength="32"
                      placeholder="如 OR07（唯一）"/>
          </el-form-item>
          <el-form-item label="名称" required>
            <el-input v-model="roomForm.roomName" data-testid="p134-room-form-name" maxlength="64"
                      placeholder="如 7号手术间（排台快照按名称对齐）"/>
          </el-form-item>
          <el-form-item label="位置">
            <el-input v-model="roomForm.location" maxlength="100" placeholder="楼层/区域，如 外科楼3层东区"/>
          </el-form-item>
          <el-form-item label="排序">
            <el-input v-model.number="roomForm.sortOrder" placeholder="总表列序，小的排前" type="number"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="roomForm.status">
              <el-radio :value="1">启用</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <div class="flex justify-end gap-2">
          <el-button @click="roomFormVisible = false">取消</el-button>
          <el-button v-perm="'ipd:surgery:add'" :loading="roomSubmitting" data-testid="p134-room-submit" type="primary"
                     @click="submitRoom">保存
          </el-button>
        </div>
      </div>
    </el-dialog>
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
import {Plus, Refresh, Scissor, Search, Setting, Warning} from '@element-plus/icons-vue';
import {
  cancelOperation,
  deleteOperationRoom,
  finishOperation,
  getOperationApplyDetail,
  getOperationApplyListPage,
  getOperationCheckItems,
  getOperationRoomAll,
  getOperationRoomList,
  getOperationScheduleMatrix,
  getOperationUnfinishedCount,
  preopCheckOperation,
  saveOperationApply,
  saveOperationRoom,
  scheduleOperation,
} from '@/api/inpatientOperation';
import {getSafetyCheckCards, signSafetyCheck} from '@/api/inpatientSafetyCheck';
import {getInpatientListPage} from '@/api/inpatient';
import {getEmployeeList} from '@/api/system';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));
// ---------------- 基础数据 ----------------
const admissions = ref([]);
const employees = ref([]);
const rooms = ref([]);
const checkItemOptions = ref([]);
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
  try {
    const res = await getOperationCheckItems();
    checkItemOptions.value = (res.data || []);
  } catch (error) {
    console.error('加载术前核对项失败:', error);
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
      operationRoom: filters.operationRoom || undefined,
      keyword: filters.keyword || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    });
    rows.value = (res.data?.records || []);
    total.value = Number(res.data?.total || 0);
  } catch (error) {
    ElMessage.error(error.message || '加载手术申请失败');
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
  filters.operationRoom = '';
  filters.keyword = '';
  pageNum.value = 1;
  loadList();
};
// ---------------- 一、发起 / 修改手术申请 ----------------
const applyVisible = ref(false);
const applySubmitting = ref(false);
const applyForm = reactive({
  id: '',
  admissionId: '',
  plannedOperationCode: '',
  plannedOperationName: '',
  operationLevel: undefined,
  incisionLevel: undefined,
  anesthesiaType: undefined,
  preopDiagnosis: '',
  operationReason: '',
  isEmergency: 0,
  isMain: 1,
  remark: '',
});
const currentAdmission = computed(() => admissions.value.find((a) => String(a.admissionId) === String(applyForm.admissionId)));
const openApply = (row) => {
  if (row) {
    // 修改：只允许「待排期」（后端也会拒，前端少让人白填一遍）
    applyForm.id = row.id;
    applyForm.admissionId = row.admissionId || '';
    applyForm.plannedOperationCode = row.plannedOperationCode || '';
    applyForm.plannedOperationName = row.plannedOperationName || '';
    applyForm.operationLevel = row.operationLevel;
    applyForm.incisionLevel = row.incisionLevel;
    applyForm.anesthesiaType = row.anesthesiaType;
    applyForm.preopDiagnosis = row.preopDiagnosis || '';
    applyForm.operationReason = row.operationReason || '';
    applyForm.isEmergency = row.isEmergency ?? 0;
    applyForm.isMain = row.isMain ?? 1;
    applyForm.remark = row.remark || '';
  } else {
    applyForm.id = '';
    applyForm.admissionId = filters.admissionId || '';
    applyForm.plannedOperationCode = '';
    applyForm.plannedOperationName = '';
    applyForm.operationLevel = undefined;
    applyForm.incisionLevel = undefined;
    applyForm.anesthesiaType = undefined;
    applyForm.preopDiagnosis = '';
    applyForm.operationReason = '';
    applyForm.isEmergency = 0;
    applyForm.isMain = 1;
    applyForm.remark = '';
  }
  applyVisible.value = true;
};
const submitApply = async () => {
  if (!applyForm.admissionId) {
    ElMessage.warning('请选择在院患者');
    return;
  }
  if (!applyForm.plannedOperationName.trim()) {
    ElMessage.warning('请填写拟施手术名称');
    return;
  }
  if (!applyForm.preopDiagnosis.trim()) {
    ElMessage.warning('请填写术前诊断（回写病历的术前诊断要素取自这里）');
    return;
  }
  if (!applyForm.operationReason.trim()) {
    ElMessage.warning('请填写手术指征（开一刀是一个医疗决定，必须写清为什么）');
    return;
  }
  applySubmitting.value = true;
  try {
    const res = await saveOperationApply({
      id: applyForm.id || undefined,
      admissionId: applyForm.admissionId,
      plannedOperationCode: applyForm.plannedOperationCode.trim() || undefined,
      plannedOperationName: applyForm.plannedOperationName.trim(),
      operationLevel: applyForm.operationLevel,
      incisionLevel: applyForm.incisionLevel,
      anesthesiaType: applyForm.anesthesiaType,
      preopDiagnosis: applyForm.preopDiagnosis.trim(),
      operationReason: applyForm.operationReason.trim(),
      isEmergency: applyForm.isEmergency,
      isMain: applyForm.isMain,
      remark: applyForm.remark.trim() || undefined,
    });
    ElMessage.success(`${applyForm.id ? '手术申请已修改' : '手术申请已提交'}：${res.data || ''}（等待手术室排台）`);
    applyVisible.value = false;
    await Promise.all([loadList(), loadUnfinishedCount(), loadMatrixIfOpen()]);
  } catch (error) {
    ElMessage.error(error.message || '手术申请提交失败');
  } finally {
    applySubmitting.value = false;
  }
};
// ---------------- 二、排台 ----------------
const scheduleVisible = ref(false);
const scheduleSubmitting = ref(false);
const scheduleTarget = ref(null);
const scheduleForm = reactive({
  operationRoom: '',
  plannedStartTime: '',
  plannedEndTime: '',
  surgeonId: '',
  assistantName: '',
  anesthetistId: '',
  scheduleRemark: '',
});
const openSchedule = (row) => {
  scheduleTarget.value = row;
  scheduleForm.operationRoom = row.operationRoom || '';
  scheduleForm.plannedStartTime = row.plannedStartTime || '';
  scheduleForm.plannedEndTime = row.plannedEndTime || '';
  scheduleForm.surgeonId = row.surgeonId || '';
  scheduleForm.assistantName = row.assistantName || '';
  scheduleForm.anesthetistId = '';
  scheduleForm.scheduleRemark = '';
  scheduleVisible.value = true;
};
const submitSchedule = async () => {
  if (!scheduleTarget.value)
    return;
  if (!scheduleForm.operationRoom) {
    ElMessage.warning('请选择或输入手术间');
    return;
  }
  if (!scheduleForm.plannedStartTime || !scheduleForm.plannedEndTime) {
    ElMessage.warning('请选择计划开始与结束时间');
    return;
  }
  if (!scheduleForm.surgeonId) {
    ElMessage.warning('请选择主刀医师');
    return;
  }
  scheduleSubmitting.value = true;
  try {
    await scheduleOperation({
      applyId: scheduleTarget.value.id,
      operationRoom: scheduleForm.operationRoom,
      plannedStartTime: scheduleForm.plannedStartTime,
      plannedEndTime: scheduleForm.plannedEndTime,
      surgeonId: scheduleForm.surgeonId,
      assistantName: scheduleForm.assistantName.trim() || undefined,
      anesthetistId: scheduleForm.anesthetistId || undefined,
      scheduleRemark: scheduleForm.scheduleRemark.trim() || undefined,
    });
    ElMessage.success('已排台');
    scheduleVisible.value = false;
    await Promise.all([loadList(), loadUnfinishedCount(), loadMatrixIfOpen()]);
  } catch (error) {
    ElMessage.error(error.message || '排台失败');
  } finally {
    scheduleSubmitting.value = false;
  }
};
// ---------------- 三、术前核对 ----------------
const checkVisible = ref(false);
const checkSubmitting = ref(false);
const checkTarget = ref(null);
const checkForm = reactive({
  items: [],
  preopNote: '',
});
const openPreopCheck = (row) => {
  checkTarget.value = row;
  checkForm.items = [];
  checkForm.preopNote = '';
  checkVisible.value = true;
};
const submitPreopCheck = async () => {
  if (!checkTarget.value)
    return;
  const missing = checkItemOptions.value
      .filter((i) => i.required && !checkForm.items.includes(i.code))
      .map((i) => i.label);
  if (missing.length > 0) {
    ElMessage.warning(`术前核对必核项未完成：${missing.join('；')}`);
    return;
  }
  checkSubmitting.value = true;
  try {
    await preopCheckOperation({
      applyId: checkTarget.value.id,
      checkItems: checkForm.items.join(','),
      preopNote: checkForm.preopNote.trim() || undefined,
    });
    ElMessage.success('术前核对已完成，可以上台并登记手术完成');
    checkVisible.value = false;
    await Promise.all([loadList(), loadUnfinishedCount(), loadMatrixIfOpen()]);
  } catch (error) {
    ElMessage.error(error.message || '术前核对失败');
  } finally {
    checkSubmitting.value = false;
  }
};
// ---------------- 四、完成（回写首页明细 + 手术记录病历） ----------------
const finishVisible = ref(false);
const finishSubmitting = ref(false);
const finishTarget = ref(null);
const finishForm = reactive({
  actualOperationCode: '',
  actualOperationName: '',
  operationStartTime: '',
  operationEndTime: '',
  bloodLoss: undefined,
  intraopFindings: '',
  intraopProcedure: '',
  postopNote: '',
  specimenSent: '',
});
const openFinish = (row) => {
  finishTarget.value = row;
  finishForm.actualOperationCode = row.plannedOperationCode || '';
  // 默认带出拟施术式 —— 大多数情况一致，不一致时医生会改，改了就按实际回写首页
  finishForm.actualOperationName = row.plannedOperationName || '';
  finishForm.operationStartTime = row.plannedStartTime || '';
  finishForm.operationEndTime = row.plannedEndTime || '';
  finishForm.bloodLoss = undefined;
  finishForm.intraopFindings = '';
  finishForm.intraopProcedure = '';
  finishForm.postopNote = '';
  finishForm.specimenSent = '';
  finishVisible.value = true;
};
const submitFinish = async () => {
  if (!finishTarget.value)
    return;
  if (!finishForm.actualOperationName.trim()) {
    ElMessage.warning('请填写实际手术名称');
    return;
  }
  if (!finishForm.operationStartTime || !finishForm.operationEndTime) {
    ElMessage.warning('请填写实际开始与结束时间');
    return;
  }
  if (!finishForm.intraopFindings.trim() || !finishForm.intraopProcedure.trim() || !finishForm.postopNote.trim()) {
    ElMessage.warning('术中所见、手术经过、术后处理都不能为空');
    return;
  }
  finishSubmitting.value = true;
  try {
    await finishOperation({
      applyId: finishTarget.value.id,
      actualOperationCode: finishForm.actualOperationCode.trim() || undefined,
      actualOperationName: finishForm.actualOperationName.trim(),
      operationStartTime: finishForm.operationStartTime,
      operationEndTime: finishForm.operationEndTime,
      bloodLoss: finishForm.bloodLoss,
      intraopFindings: finishForm.intraopFindings.trim(),
      intraopProcedure: finishForm.intraopProcedure.trim(),
      postopNote: finishForm.postopNote.trim(),
      specimenSent: finishForm.specimenSent.trim() || undefined,
    });
    ElMessage.success('手术已完成（已回写病案首页手术明细与手术记录病历）');
    finishVisible.value = false;
    await Promise.all([loadList(), loadUnfinishedCount(), loadMatrixIfOpen()]);
  } catch (error) {
    ElMessage.error(error.message || '登记手术完成失败');
  } finally {
    finishSubmitting.value = false;
  }
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
    ElMessage.error(error.message || '取消失败');
  }
};
const detailVisible = ref(false);
const detail = ref(null);
const openDetail = async (row) => {
  try {
    const res = await getOperationApplyDetail(row.id);
    detail.value = (res.data || row);
    detailVisible.value = true;
  } catch (error) {
    ElMessage.error(error.message || '加载手术详情失败');
  }
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
  } catch (error) {
    ElMessage.error(error.message || '加载排台总表失败');
  } finally {
    matrixLoading.value = false;
  }
};
const onTabChange = (name) => {
  if (name === 'matrix' && !matrix.value)
    loadMatrix();
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
const roomRows = ref([]);
const roomLoading = ref(false);
const roomFormVisible = ref(false);
const roomSubmitting = ref(false);
const roomForm = reactive({
  id: '',
  roomCode: '',
  roomName: '',
  location: '',
  sortOrder: 1,
  status: 1,
});
const loadRooms = async () => {
  roomLoading.value = true;
  try {
    const res = await getOperationRoomAll();
    roomRows.value = (res.data || []);
  } catch (error) {
    ElMessage.error(error.message || '加载手术间失败');
  } finally {
    roomLoading.value = false;
  }
};
const openRooms = () => {
  roomFormVisible.value = false;
  roomVisible.value = true;
  loadRooms();
};
const editRoom = (row) => {
  roomForm.id = row.id;
  roomForm.roomCode = row.roomCode;
  roomForm.roomName = row.roomName;
  roomForm.location = row.location || '';
  roomForm.sortOrder = row.sortOrder ?? 1;
  roomForm.status = row.status ?? 1;
  roomFormVisible.value = true;
};
const newRoom = () => {
  roomForm.id = '';
  roomForm.roomCode = '';
  roomForm.roomName = '';
  roomForm.location = '';
  roomForm.sortOrder = (roomRows.value.length + 1) || 1;
  roomForm.status = 1;
  roomFormVisible.value = true;
};
const submitRoom = async () => {
  if (!roomForm.roomCode.trim() || !roomForm.roomName.trim()) {
    ElMessage.warning('手术间编码与名称都不能为空');
    return;
  }
  roomSubmitting.value = true;
  try {
    await saveOperationRoom({
      id: roomForm.id || undefined,
      roomCode: roomForm.roomCode.trim(),
      roomName: roomForm.roomName.trim(),
      location: roomForm.location.trim() || undefined,
      sortOrder: roomForm.sortOrder,
      status: roomForm.status,
    });
    ElMessage.success('手术间已保存');
    roomFormVisible.value = false;
    await Promise.all([loadRooms(), loadList(), loadMatrixIfOpen()]);
  } catch (error) {
    ElMessage.error(error.message || '保存手术间失败');
  } finally {
    roomSubmitting.value = false;
  }
};
const loadMatrixIfOpen = async () => {
  if (activeTab.value === 'matrix')
    await loadMatrix();
};
const removeRoom = async (row) => {
  try {
    await ElMessageBox.confirm(`手术间「${row.roomName}」将被物理删除（编码 ${row.roomCode} 会释放，可重建同码）。`
        + '只是暂时不用请改「停用」，不要删。历史手术单不受影响。', '删除手术间', {
      confirmButtonText: '确认删除',
      cancelButtonText: '改用停用',
      type: 'warning'
    });
  } catch {
    return;
  }
  try {
    await deleteOperationRoom(row.id);
    ElMessage.success('手术间已删除');
    await Promise.all([loadRooms(), loadMatrixIfOpen()]);
  } catch (error) {
    ElMessage.error(error.message || '删除手术间失败');
  }
};
const toggleRoomStatus = async (row, status) => {
  try {
    await saveOperationRoom({
      id: row.id,
      roomCode: row.roomCode,
      roomName: row.roomName,
      location: row.location,
      sortOrder: row.sortOrder,
      status,
    });
    ElMessage.success(status === 1 ? '手术间已启用' : '手术间已停用（总表不再出列，历史手术不受影响）');
    await Promise.all([loadRooms(), loadMatrixIfOpen()]);
  } catch (error) {
    ElMessage.error(error.message || '状态更新失败');
  }
};
const safetyVisible = ref(false);
const safetyLoading = ref(false);
const safetySubmitting = ref(false);
const safetyTarget = ref(null);
const safetyCards = ref([]);
const safetyForm = reactive({
  phase: 0,
  items: [],
  surgeonId: '',
  anesthetistId: '',
  nurseId: '',
  note: '',
});
const loadSafetyCards = async (applyId) => {
  safetyLoading.value = true;
  try {
    const res = await getSafetyCheckCards(applyId);
    safetyCards.value = (res.data || []);
  } catch (error) {
    ElMessage.error(error.message || '加载安全核查单失败');
  } finally {
    safetyLoading.value = false;
  }
};
const openSafety = async (row) => {
  safetyTarget.value = row;
  safetyForm.phase = 0;
  safetyForm.items = [];
  safetyForm.surgeonId = row.surgeonId || '';
  safetyForm.anesthetistId = '';
  safetyForm.nurseId = '';
  safetyForm.note = '';
  safetyVisible.value = true;
  await loadSafetyCards(row.id);
};
const pickSafetyPhase = (card) => {
  safetyForm.phase = card.phase;
  safetyForm.items = [];
};
const currentSafetyCard = computed(() => safetyCards.value.find((c) => c.phase === safetyForm.phase));
const submitSafety = async () => {
  if (!safetyTarget.value || !safetyForm.phase) {
    ElMessage.warning('请先选择要签核的时段');
    return;
  }
  const card = currentSafetyCard.value;
  if (card) {
    const missing = card.items.filter((i) => i.required && !safetyForm.items.includes(i.code)).map((i) => i.label);
    if (missing.length > 0) {
      ElMessage.warning(`该时段必核项未完成：${missing.join('；')}`);
      return;
    }
  }
  if (!safetyForm.surgeonId || !safetyForm.anesthetistId || !safetyForm.nurseId) {
    ElMessage.warning('手术医师、麻醉医师、手术室护士三方都必须签名（核查的意义就是三方在场）');
    return;
  }
  const ids = [safetyForm.surgeonId, safetyForm.anesthetistId, safetyForm.nurseId];
  if (new Set(ids).size < 3) {
    ElMessage.warning('三方必须是三个不同的人（同一个人签三方是走形式，后端也会拒）');
    return;
  }
  safetySubmitting.value = true;
  try {
    await signSafetyCheck({
      applyId: safetyTarget.value.id,
      phase: safetyForm.phase,
      items: safetyForm.items.join(','),
      surgeonId: safetyForm.surgeonId,
      anesthetistId: safetyForm.anesthetistId,
      nurseId: safetyForm.nurseId,
      note: safetyForm.note.trim() || undefined,
    });
    ElMessage.success('该时段核查已签核（签过即不可改）');
    safetyForm.phase = 0;
    safetyForm.items = [];
    await Promise.all([loadSafetyCards(safetyTarget.value.id), loadList(), loadMatrixIfOpen()]);
  } catch (error) {
    ElMessage.error(error.message || '核查签核失败');
  } finally {
    safetySubmitting.value = false;
  }
};
onMounted(async () => {
  await loadBaseData();
  await Promise.all([loadList(), loadUnfinishedCount()]);
});
</script>
