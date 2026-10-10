<template>
  <div class="flex h-full flex-col gap-3" data-testid="bed-center">
    <!-- 今日总值班：跨科调配的接收争议、等床超时无人管，都由他拍板（sql/169） -->
    <div class="shrink-0" data-testid="bc-duty">
      <DutyOfficerBar/>
    </div>
    <!-- 顶部概览：数值全部来自后端一次算好的 /queue/stats，前端不做任何加减 -->
    <div class="grid shrink-0 grid-cols-2 gap-2 md:grid-cols-4 xl:grid-cols-8" data-testid="bc-stats">
      <div class="rounded-lg border border-slate-200 bg-white p-3">
        <div class="text-xs text-slate-500">等待中</div>
        <div class="text-xl font-semibold text-slate-800">{{ stats.waitingTotal ?? 0 }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3">
        <div class="text-xs text-slate-500">危重 / 急</div>
        <div class="text-xl font-semibold text-rose-600">
          {{ stats.waitingCritical ?? 0 }} / {{ stats.waitingUrgent ?? 0 }}
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3">
        <div class="text-xs text-slate-500">已安排床位</div>
        <div class="text-xl font-semibold text-[#1269B5]">{{ stats.arrangedCount ?? 0 }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3">
        <div class="text-xs text-slate-500">今日收治 / 取消</div>
        <div class="text-xl font-semibold text-emerald-600">
          {{ stats.admittedToday ?? 0 }} / {{ stats.cancelledToday ?? 0 }}
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3">
        <div class="text-xs text-slate-500">等待超时（&gt;{{ stats.maxWaitDays ?? 7 }}天）</div>
        <div :class="(stats.overdueCount ?? 0) > 0 ? 'text-rose-600' : 'text-slate-800'" class="text-xl font-semibold">
          {{ stats.overdueCount ?? 0 }}
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3">
        <div class="text-xs text-slate-500">平均 / 最长等待</div>
        <div class="text-sm font-semibold text-slate-800">
          {{ stats.avgWaitHours ?? 0 }}h / {{ stats.maxWaitHours ?? 0 }}h
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3">
        <div class="text-xs text-slate-500">全院床位（总）</div>
        <div class="text-xl font-semibold text-slate-800">{{ stats.totalBeds ?? 0 }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3">
        <div class="text-xs text-slate-500">空闲 / 已预留</div>
        <div class="text-xl font-semibold text-teal-600">
          {{ stats.freeBeds ?? 0 }} / {{ stats.lockedBeds ?? 0 }}
        </div>
      </div>
    </div>

    <div class="flex min-h-0 flex-1 flex-col rounded-lg border border-slate-200 bg-white">
      <el-tabs v-model="tab" class="bc-tabs min-h-0 flex-1">
        <!-- ==================== 页签一：等床队列 ==================== -->
        <el-tab-pane name="queue">
          <template #label>
            <span class="inline-flex items-center">
              等床队列
              <span v-if="waitingCount > 0" class="ml-1 rounded bg-rose-500 px-1.5 text-xs text-white">{{
                  waitingCount
                }}</span>
            </span>
          </template>

          <div class="flex h-full min-h-0 flex-col p-3">
            <div class="mb-2 flex flex-wrap items-center gap-2">
              <el-select v-model="query.waitStatus" class="!w-32" clearable data-testid="bc-q-status"
                         placeholder="状态">
                <el-option :value="0" label="等待中"/>
                <el-option :value="1" label="已安排床位"/>
                <el-option :value="2" label="已收治入院"/>
                <el-option :value="3" label="已取消"/>
              </el-select>
              <el-select v-model="query.applyDeptId" :fit-input-width="false" class="!w-44" clearable data-testid="bc-q-dept"
                         filterable placeholder="拟收治科室">
                <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
              </el-select>
              <el-select v-model="query.priority" class="!w-28" clearable data-testid="bc-q-priority"
                         placeholder="优先级">
                <el-option :value="1" label="普通"/>
                <el-option :value="2" label="急"/>
                <el-option :value="3" label="危重"/>
              </el-select>
              <el-select v-model="query.bedType" class="!w-28" clearable data-testid="bc-q-bedtype" placeholder="床型">
                <el-option label="普通" value="normal"/>
                <el-option label="重症 ICU" value="ICU"/>
                <el-option label="特需 VIP" value="VIP"/>
              </el-select>
              <el-input v-model="query.keyword" class="!w-44" clearable data-testid="bc-q-keyword"
                        placeholder="姓名 / 等待号 / 电话" @keyup.enter="loadQueue"/>
              <el-checkbox v-model="query.overdueOnly" data-testid="bc-q-overdue">只看超时</el-checkbox>
              <el-button :loading="loading" data-testid="bc-refresh" @click="loadQueue">查询</el-button>
              <el-button v-perm="'ipd:bedCenter:add'" data-testid="bc-add" type="primary" @click="openCreate">
                登记排队
              </el-button>
            </div>

            <el-table v-loading="loading" :data="rows" border class="flex-1" data-testid="bc-queue-table" height="100%"
                      size="small">
              <el-table-column align="center" label="位次" width="60">
                <template #default="{ row }">
                  <span v-if="row.seq && row.seq > 0" class="font-semibold text-[#1269B5]">{{ row.seq }}</span>
                  <span v-else class="text-xs text-slate-400">—</span>
                </template>
              </el-table-column>
              <el-table-column label="等待号" prop="waitNo" width="130"/>
              <el-table-column label="患者" min-width="140">
                <template #default="{ row }">
                  <div class="font-medium text-slate-800">{{ row.patientName || '—' }}</div>
                  <div class="text-xs text-slate-400" data-testid="bc-row-patient-sub">
                    {{ row.genderText || '—' }} · {{ row.age ?? '—' }}岁 · {{ row.phone || '无电话' }}
                  </div>
                </template>
              </el-table-column>
              <el-table-column align="center" label="优先级" width="80">
                <template #default="{ row }">
                  <el-tag :type="priorityTag(row.priority)" size="small">{{ row.priorityText }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="拟收治科室" min-width="120">
                <template #default="{ row }">
                  <span>{{ row.applyDeptName || '未指定' }}</span>
                  <div class="text-xs text-slate-400">{{ row.diagnosisName || '未填拟诊' }}</div>
                </template>
              </el-table-column>
              <el-table-column label="床型 / 需求" width="130">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.bedTypeText }}</div>
                  <div class="text-xs text-slate-400">
                    {{ row.genderLimitText }}<span v-if="row.isolationFlag === 1"> · 隔离</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="等待时长" width="120">
                <template #default="{ row }">
                  <span :class="row.expired ? 'font-semibold text-rose-600' : 'text-slate-700'">
                    {{ row.waitDurationText }}
                  </span>
                  <el-tag v-if="row.expired" class="ml-1" size="small" type="danger">超时</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="安排床位" min-width="150">
                <template #default="{ row }">
                  <template v-if="row.assignedBedNo">
                    <div class="text-slate-800" data-testid="bc-row-bed">
                      {{ row.assignedDeptName }} · {{ row.assignedWardName }} · {{ row.assignedBedNo }}床
                    </div>
                    <el-tag v-if="row.crossDept" size="small" type="warning">跨科调配</el-tag>
                  </template>
                  <span v-else class="text-xs text-slate-400">未安排</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="waitStatusTag(row.waitStatus)" size="small">{{ row.waitStatusText }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="230">
                <template #default="{ row }">
                  <el-button v-if="row.canAssign" v-perm="'ipd:bedCenter:assign'" data-testid="bc-row-assign" link
                             size="small" type="primary" @click="openAssign(row)">安排床位
                  </el-button>
                  <el-button v-if="row.canRelease" v-perm="'ipd:bedCenter:release'" data-testid="bc-row-release" link
                             size="small" type="warning" @click="openRelease(row)">退回
                  </el-button>
                  <el-button v-if="row.canAdmit" v-perm="'ipd:bedCenter:admit'" data-testid="bc-row-admit" link
                             size="small" type="success" @click="openAdmit(row)">办理入院
                  </el-button>
                  <el-button v-if="row.canCancel" v-perm="'ipd:bedCenter:cancel'" data-testid="bc-row-cancel" link
                             size="small" type="danger" @click="openCancel(row)">取消
                  </el-button>
                  <el-button v-if="row.waitStatus === 0" v-perm="'ipd:bedCenter:add'" data-testid="bc-row-edit" link
                             size="small" type="info" @click="openEdit(row)">改需求
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <span class="text-xs text-slate-400">当前条件下没有排队记录</span>
              </template>
            </el-table>

            <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                           :page-sizes="PAGE_SIZES" :total="total" class="mt-2 justify-end"
                           layout="total, sizes, prev, pager, next" @current-change="loadQueue"
                           @size-change="loadQueue"/>
          </div>
        </el-tab-pane>

        <!-- ==================== 页签二：全院床位池 ==================== -->
        <el-tab-pane label="全院床位" name="pool">
          <div class="flex h-full min-h-0 flex-col p-3">
            <div class="mb-2 grid grid-cols-2 gap-2 md:grid-cols-5" data-testid="bc-overview">
              <div class="rounded border border-slate-200 bg-slate-50 p-2">
                <div class="text-xs text-slate-500">床位总数</div>
                <div class="text-lg font-semibold">{{ overview.summary?.totalBeds ?? 0 }}</div>
              </div>
              <div class="rounded border border-slate-200 bg-emerald-50 p-2">
                <div class="text-xs text-slate-500">空闲</div>
                <div class="text-lg font-semibold text-emerald-600">{{ overview.summary?.freeBeds ?? 0 }}</div>
              </div>
              <div class="rounded border border-slate-200 bg-slate-50 p-2">
                <div class="text-xs text-slate-500">占用 / 使用率</div>
                <div class="text-lg font-semibold text-slate-800">
                  {{ overview.summary?.occupiedBeds ?? 0 }} / {{ overview.summary?.usageRate ?? 0 }}%
                </div>
              </div>
              <div class="rounded border border-slate-200 bg-amber-50 p-2">
                <div class="text-xs text-slate-500">已预留（锁定）</div>
                <div class="text-lg font-semibold text-amber-600">{{ overview.summary?.lockedBeds ?? 0 }}</div>
              </div>
              <div class="rounded border border-slate-200 bg-slate-50 p-2">
                <div class="text-xs text-slate-500">维修停用</div>
                <div class="text-lg font-semibold text-slate-500">{{ overview.summary?.repairBeds ?? 0 }}</div>
              </div>
            </div>

            <div class="mb-2 flex flex-wrap items-center gap-2">
              <el-select v-model="poolQuery.deptId" :fit-input-width="false" class="!w-44" clearable data-testid="bc-pool-dept"
                         filterable placeholder="科室">
                <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
              </el-select>
              <el-select v-model="poolQuery.wardId" :fit-input-width="false" class="!w-44" clearable
                         data-testid="bc-pool-ward"
                         placeholder="病区">
                <el-option v-for="w in wardOptions" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
              </el-select>
              <el-select v-model="poolQuery.bedStatus" class="!w-28" clearable data-testid="bc-pool-status"
                         placeholder="状态">
                <el-option :value="0" label="维修"/>
                <el-option :value="1" label="空闲"/>
                <el-option :value="2" label="占用"/>
                <el-option :value="3" label="锁定"/>
              </el-select>
              <el-input v-model="poolQuery.keyword" class="!w-40" clearable data-testid="bc-pool-keyword"
                        placeholder="床号 / 患者姓名" @keyup.enter="loadPool"/>
              <el-button :loading="poolLoading" data-testid="bc-pool-refresh" @click="loadPool">查询</el-button>
            </div>

            <el-table v-loading="poolLoading" :data="poolRows" border class="flex-1" data-testid="bc-pool-table"
                      height="100%" size="small">
              <el-table-column label="床号" prop="bedNo" width="90"/>
              <el-table-column label="所属科室 / 病区" min-width="180">
                <template #default="{ row }">
                  <span>{{ row.deptName || '—' }}</span>
                  <span class="text-xs text-slate-400"> · {{ row.wardName || '—' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="类型" prop="bedTypeText" width="80"/>
              <el-table-column label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="bedStatusTag(row.bedStatus)" size="small">{{ row.bedStatusText }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="占用患者" min-width="160">
                <template #default="{ row }">
                  <span v-if="row.patientName">{{ row.patientName }}（{{ row.admitTime || '—' }}入院）</span>
                  <span v-else class="text-xs text-slate-400">—</span>
                </template>
              </el-table-column>
              <el-table-column label="预留去向" min-width="180">
                <template #default="{ row }">
                  <span v-if="row.reservedPatientName">
                    {{ row.reservedPatientName }}
                    <span class="text-xs text-slate-400">→ {{ row.useDeptName || '未指定科室' }}（{{
                        row.reservedTime
                      }}）</span>
                  </span>
                  <span v-else class="text-xs text-slate-400">—</span>
                </template>
              </el-table-column>
              <template #empty>
                <span class="text-xs text-slate-400">没有符合条件的床位</span>
              </template>
            </el-table>

            <el-pagination v-model:current-page="poolQuery.pageNum" v-model:page-size="poolQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="poolTotal" class="mt-2 justify-end"
                           layout="total, sizes, prev, pager, next" @current-change="loadPool"
                           @size-change="loadPool"/>
          </div>
        </el-tab-pane>

        <!-- ==================== 页签三：床位一张图（调配视角） ==================== -->
        <el-tab-pane label="床位一张图" name="map">
          <div class="flex h-full min-h-0 flex-col p-3">
            <div class="mb-2 grid grid-cols-2 gap-2 md:grid-cols-5" data-testid="bc-map-stats">
              <div class="rounded border border-slate-200 bg-slate-50 p-2">
                <div class="text-xs text-slate-500">本科室床位</div>
                <div class="text-lg font-semibold">{{ mapData.summary?.totalBeds ?? 0 }}</div>
              </div>
              <div class="rounded border border-emerald-200 bg-emerald-50 p-2">
                <div class="text-xs text-slate-500">空闲（可直接安排）</div>
                <div class="text-lg font-semibold text-emerald-600">{{ mapData.summary?.free ?? 0 }}</div>
              </div>
              <div class="rounded border border-slate-200 bg-slate-50 p-2">
                <div class="text-xs text-slate-500">占用 / 使用率</div>
                <div class="text-lg font-semibold text-slate-800">
                  {{ mapData.summary?.occupied ?? 0 }} / {{ mapData.summary?.usageRate ?? 0 }}%
                </div>
              </div>
              <div class="rounded border border-amber-200 bg-amber-50 p-2">
                <div class="text-xs text-slate-500">已预留</div>
                <div class="text-lg font-semibold text-amber-600">{{ mapData.summary?.locked ?? 0 }}</div>
              </div>
              <div class="rounded border border-slate-200 bg-slate-50 p-2">
                <div class="text-xs text-slate-500">维修停用</div>
                <div class="text-lg font-semibold text-slate-500">{{ mapData.summary?.repair ?? 0 }}</div>
              </div>
            </div>

            <div class="mb-2 flex flex-wrap items-center gap-2">
              <el-select v-model="mapQuery.deptId" :fit-input-width="false" class="!w-56" data-testid="bc-map-dept"
                         filterable placeholder="科室" @change="onMapDeptChange">
                <el-option v-for="d in mapDeptOptions" :key="d.deptId" :label="`${d.deptName}（${d.occupied}/${d.total}）`"
                           :value="d.deptId"/>
              </el-select>
              <el-select v-model="mapQuery.wardId" :fit-input-width="false" class="!w-48" clearable
                         data-testid="bc-map-ward" placeholder="全部病区" @change="loadMap">
                <el-option v-for="w in mapWardOptions" :key="w.wardId" :label="`${w.wardName}（${w.occupied}/${w.total}）`"
                           :value="w.wardId"/>
              </el-select>
              <el-button :loading="mapLoading" data-testid="bc-map-refresh" @click="loadMap">刷新</el-button>
              <div class="ml-1 flex items-center gap-3 text-xs text-slate-500">
                <span><i class="bc-dot bc-dot-free"/>空闲</span>
                <span><i class="bc-dot bc-dot-occupied"/>占用</span>
                <span><i class="bc-dot bc-dot-locked"/>已预留</span>
                <span><i class="bc-dot bc-dot-repair"/>维修</span>
              </div>
            </div>

            <div class="min-h-0 flex-1 overflow-auto" data-testid="bc-map-canvas">
              <div v-for="g in groupedBeds" :key="g.wardId" class="mb-3">
                <div class="mb-1 text-sm font-semibold text-slate-700">
                  {{ g.wardName }}
                  <span class="text-xs font-normal text-slate-400">
                    占用 {{ g.occupied }} / {{ g.beds.length }}
                    <span v-if="g.reserved"> · 预留 {{ g.reserved }}</span>
                  </span>
                </div>
                <div class="grid grid-cols-3 gap-2 md:grid-cols-6 xl:grid-cols-10">
                  <div v-for="bed in g.beds" :key="bed.bedId" :class="bedCardClass(bed)"
                       class="bc-bed cursor-pointer rounded border p-2 text-left transition hover:shadow"
                       data-testid="bc-map-bed" @click="onBedClick(bed)">
                    <div class="flex items-center justify-between">
                      <span class="text-xs font-semibold" data-testid="bc-map-bed-no">{{ bed.bedNo }}</span>
                      <span class="text-[10px] text-slate-400">{{ bed.bedTypeText || bed.bedType }}</span>
                    </div>
                    <div class="mt-0.5 truncate text-[13px] font-semibold" data-testid="bc-map-bed-name">
                      {{ bed.patientName || bed.reservedPatientName || '空床' }}
                    </div>
                    <div class="truncate text-[11px] text-slate-500" data-testid="bc-map-bed-sub">
                      <span v-if="bed.bedStatus === 2">入院 {{ bed.admitDays ?? 0 }} 天</span>
                      <span v-else-if="bed.bedStatus === 3 && bed.reservedPatientName">
                        预留 · {{ bed.reservedPriorityText || '' }}
                      </span>
                      <span v-else>{{ bed.bedStatusText }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-if="!mapLoading && !mapBeds.length" class="py-8 text-center text-xs text-slate-400">
                该科室下没有床位
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- ==================== 登记 / 改需求 ==================== -->
    <el-dialog v-model="formVisible" :title="form.id ? '修改排队需求' : '登记床位排队'" width="620px">
      <el-form label-width="110px" size="small">
        <el-form-item v-if="!form.admissionOrderId" label="患者" required>
          <PatientSelect v-model="form.patientId" :disabled="!!form.id" data-testid="bc-form-patient"
                         @select="onPatientSelect"/>
        </el-form-item>
        <el-form-item v-else label="患者">
          <span class="text-sm text-slate-700">
            {{ form.patientName }}（来源住院证 {{ form.admissionOrderId }}）
          </span>
        </el-form-item>
        <el-form-item label="拟收治科室">
          <el-select v-model="form.applyDeptId" :fit-input-width="false" class="!w-full" clearable data-testid="bc-form-dept"
                     filterable placeholder="选择科室">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="期望病区">
          <el-select v-model="form.expectWardId" :fit-input-width="false" class="!w-full" clearable
                     data-testid="bc-form-ward" placeholder="不指定=服从调配">
            <el-option v-for="w in wardOptions" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="床型 / 优先级">
          <div class="flex gap-2">
            <el-select v-model="form.bedType" class="!w-32" data-testid="bc-form-bedtype">
              <el-option label="普通" value="normal"/>
              <el-option label="重症 ICU" value="ICU"/>
              <el-option label="特需 VIP" value="VIP"/>
            </el-select>
            <el-select v-model="form.priority" class="!w-32" data-testid="bc-form-priority">
              <el-option :value="1" label="普通"/>
              <el-option :value="2" label="急"/>
              <el-option :value="3" label="危重"/>
            </el-select>
            <el-select v-model="form.genderLimit" class="!w-32" data-testid="bc-form-gender">
              <el-option :value="0" label="不限性别"/>
              <el-option :value="1" label="限男床"/>
              <el-option :value="2" label="限女床"/>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="隔离需求">
          <el-checkbox v-model="isolation" data-testid="bc-form-isolation">需要隔离床（不得混住）</el-checkbox>
        </el-form-item>
        <el-form-item label="预计入院日期">
          <el-date-picker v-model="form.expectAdmitDate" data-testid="bc-form-expect" placeholder="选择预计入院日期"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item v-if="form.id" label="联系电话">
          <el-input v-model="form.phone" class="!w-40" data-testid="bc-form-phone"/>
        </el-form-item>
        <el-form-item label="拟诊名称">
          <el-input v-model="form.diagnosisName" data-testid="bc-form-diag" placeholder="拟诊（快照到排队单上）"/>
        </el-form-item>
      </el-form>
      <p class="mb-2 text-xs text-slate-400">
        保存后进入队列并按「优先级 → 登记时间」排序；门急诊所开的住院证会由系统自动入队，不需要在这里重复登记。
      </p>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button :loading="submitting" data-testid="bc-form-submit" type="primary" @click="submitForm">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 安排床位 ==================== -->
    <el-dialog v-model="assignVisible" title="安排床位（含跨科调配）" width="820px">
      <div v-if="current" class="mb-2 text-sm text-slate-600">
        患者 <b>{{ current.patientName }}</b> · {{ current.priorityText }} · 拟收治
        {{ current.applyDeptName || '未指定科室' }} · 需求床型 {{ current.bedTypeText }}
      </div>
      <el-radio-group v-model="assignMode" class="mb-2" data-testid="bc-assign-mode">
        <el-radio-button value="match">推荐匹配</el-radio-button>
        <el-radio-button value="manual">自选床位</el-radio-button>
      </el-radio-group>

      <el-input v-if="assignMode === 'manual'" v-model="manualKeyword" class="mb-2 !w-56" clearable
                data-testid="bc-assign-keyword"
                placeholder="床号 / 科室" @keyup.enter="loadManualBeds"/>

      <el-table :data="assignRows" border data-testid="bc-assign-table" height="320" highlight-current-row
                size="small" @current-change="onPickBed">
        <el-table-column label="选择" width="50">
          <template #default="{ row }">
            <el-radio v-model="pickedBedId" :value="String(row.bedId)"><span/></el-radio>
          </template>
        </el-table-column>
        <el-table-column label="床号" prop="bedNo" width="90"/>
        <el-table-column label="归属科室 / 病区" min-width="180">
          <template #default="{ row }">
            {{ row.deptName || '—' }} · {{ row.wardName || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="床型" prop="bedTypeText" width="80"/>
        <el-table-column v-if="assignMode === 'match'" label="匹配档位" prop="matchLevelText" width="120"/>
        <el-table-column v-if="assignMode === 'match'" label="说明" min-width="200" prop="matchReason"/>
        <template #empty>
          <span class="text-xs text-slate-400">没有符合条件的床位</span>
        </template>
      </el-table>
      <p class="mt-2 text-xs text-slate-400">
        确认后该床会被<b>锁定</b>并挂上此患者（其他排队记录安排不到它），随后在列表里点「办理入院」正式占用。
      </p>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button :disabled="!pickedBedId" :loading="submitting" data-testid="bc-assign-submit" type="primary"
                   @click="submitAssign">确认安排
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 退回 / 取消 ==================== -->
    <el-dialog v-model="reasonVisible" :title="reasonTitle" width="480px">
      <el-input v-model="reason" :placeholder="reasonPlaceholder" :rows="3" data-testid="bc-reason-input"
                type="textarea"/>
      <template #footer>
        <el-button @click="reasonVisible = false">取消</el-button>
        <el-button :loading="submitting" data-testid="bc-reason-submit" type="primary" @click="submitReason">确认
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 办理入院 ==================== -->
    <el-dialog v-model="admitVisible" title="按已安排床位办理入院" width="560px">
      <el-form label-width="110px" size="small">
        <el-form-item label="床位">
          <span class="text-sm text-slate-700" data-testid="bc-admit-bed">
            {{ current?.assignedDeptName }} · {{ current?.assignedWardName }} · {{ current?.assignedBedNo }}床
            <el-tag v-if="current?.crossDept" class="ml-1" size="small" type="warning">跨科调配</el-tag>
          </span>
        </el-form-item>
        <el-form-item label="入院医生" required>
          <el-select v-model="admitForm.admitDoctorId" :fit-input-width="false" class="!w-full" data-testid="bc-admit-doctor"
                     filterable placeholder="选择医生">
            <el-option v-for="e in employees" :key="e.id" :label="e.empName" :value="e.id"/>
          </el-select>
        </el-form-item>
        <el-form-item v-if="!current?.admissionOrderId" label="入院途径" required>
          <el-select v-model="admitForm.admitWay" class="!w-full" data-testid="bc-admit-way" placeholder="病案首页必填">
            <el-option :value="2" label="急诊"/>
            <el-option :value="3" label="转院"/>
            <el-option :value="4" label="其他"/>
          </el-select>
        </el-form-item>
        <el-form-item v-else label="入院途径">
          <span class="text-sm text-slate-500">门诊（按来源住院证自动确定）</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="admitForm.remark" data-testid="bc-admit-remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="admitVisible = false">取消</el-button>
        <el-button :loading="submitting" data-testid="bc-admit-submit" type="primary" @click="submitAdmit">确认入院
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 床位卡详情（图上点床） ==================== -->
    <el-dialog v-model="bedVisible" :title="`床位 ${bedDetail.bedNo || ''} · ${bedDetail.bedStatusText || ''}`"
               width="520px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="所属">{{ bedDetail.deptName || '—' }} · {{
            bedDetail.wardName || '—'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="床型">{{
            bedDetail.bedTypeText || bedDetail.bedType || '—'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="bedStatusTag(bedDetail.bedStatus)" size="small">{{ bedDetail.bedStatusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="占用患者">
          {{ bedDetail.patientName || '—' }}
          <span v-if="bedDetail.admitTime" class="text-xs text-slate-400">（{{ bedDetail.admitTime }} 入院）</span>
        </el-descriptions-item>
        <el-descriptions-item label="预留去向">
          <span v-if="bedDetail.reservedPatientName">
            {{ bedDetail.reservedPatientName }}
            <span class="text-xs text-slate-400">
              · {{ bedDetail.reservedWaitNo || '无等待号' }} · {{ bedDetail.reservedPriorityText || '' }}
            </span>
          </span>
          <span v-else class="text-xs text-slate-400">—</span>
        </el-descriptions-item>
        <el-descriptions-item label="调配方式">
          {{ bedDetail.allocTypeText || '—' }}
          <el-tag v-if="bedDetail.crossDept" class="ml-1" size="small" type="warning">跨科</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="预留时间">{{ bedDetail.reserveTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ bedDetail.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="bedVisible = false">关闭</el-button>
        <el-button v-if="bedDetail.canReserve" v-perm="'ipd:bedCenter:assign'" data-testid="bc-bed-reserve"
                   type="primary" @click="openReserve(bedDetail)">预留给等床患者
        </el-button>
        <el-button v-if="bedDetail.canRelease" v-perm="'ipd:bedCenter:release'" data-testid="bc-bed-release"
                   type="warning" @click="releaseFromMap">释放该床
        </el-button>
        <el-button v-if="bedDetail.canAdmit" v-perm="'ipd:bedCenter:admit'" data-testid="bc-bed-admit"
                   type="primary" @click="admitFromMap">办理入院
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 图上预留：选一位在等的患者 ==================== -->
    <el-dialog v-model="reserveVisible" title="把这张床预留给谁" width="560px">
      <div class="mb-2 text-sm text-slate-600">
        床位：<b>{{ bedDetail.bedNo }}</b>（{{ bedDetail.deptName }} · {{ bedDetail.wardName }}）
      </div>
      <el-select v-model="reserveWaitId" :fit-input-width="false" class="!w-full" data-testid="bc-reserve-patient"
                 filterable placeholder="选择等待中的患者" @change="onReservePick">
        <el-option v-for="w in waitingRows" :key="w.id" :label="`#${w.seq ?? '-'} ${w.patientName} · ${w.priorityText || ''} · ${w.applyDeptName || '未指定科室'}`"
                   :value="String(w.id)"/>
      </el-select>
      <div v-if="reserveHint" class="mt-2 rounded border border-slate-200 bg-slate-50 p-2 text-xs"
           data-testid="bc-reserve-hint">
        {{ reserveHint }}
      </div>
      <div v-else-if="reserveWaitId" class="mt-2 text-xs text-slate-400">正在核对匹配档位……</div>
      <template #footer>
        <el-button @click="reserveVisible = false">取消</el-button>
        <el-button :disabled="!reserveWaitId" :loading="submitting" data-testid="bc-reserve-submit"
                   type="primary" @click="submitReserve">确认预留
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 床位服务中心（菜单 317，sql/144）
 *
 * 三个前置判断，决定了这一页长什么样：
 *  1. **队列不是先到先得**：服务端按「优先级 → 登记时间」排序，前端没有排序入参。
 *     页面上那个「位次」是后端给的全局序数（翻到第 2 页不会从 1 重新数）。
 *  2. **安排床位 = 锁定**：确认后该床立刻被占用不了别人（`bed_status=3` + 挂患者），
 *     所以弹窗里<b>只列出空闲且未被预留的床</b>，并把后端的推荐档位（本科室/跨科 × 同床型/可降级）显示出来 ——
 *     决定由现场的人做，系统负责说清为什么是这张。
 *  3. **所有操作可用性读后端**（canAssign/canRelease/canCancel/canAdmit）：
 *     前端自己拼一遍状态机，就一定会在某个新状态下和后端漂移。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {getDepartmentSelectList, getEmployeeList} from '@/api/system';
import {getInpatientWardList} from '@/api/inpatient';
// 今日总值班：跨科调配与等床超时的兜底协调人（sql/169）
import DutyOfficerBar from '@/components/his/DutyOfficerBar.vue';
import {
  admitBedWait,
  assignBed,
  cancelBedWait,
  countBedWaiting,
  getBedCenterMap,
  getBedOverview,
  getBedPool,
  getBedWaitDetail,
  getBedWaitListPage,
  getBedWaitStats,
  matchBeds,
  releaseBed,
  upsertBedWait,
} from '@/api/bedCenter';

const tab = ref('queue');
const loading = ref(false);
const poolLoading = ref(false);
const submitting = ref(false);
const rows = ref([]);
const poolRows = ref([]);
const total = ref(0);
const poolTotal = ref(0);
const stats = ref({});
const overview = ref({summary: {}, deptRows: []});
const waitingCount = ref(0);
const deptOptions = ref([]);
const wardOptions = ref([]);
const employees = ref([]);
const query = reactive({
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, waitStatus: null, applyDeptId: null,
  priority: null, bedType: null, keyword: '', overdueOnly: false,
});
const poolQuery = reactive({
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, deptId: null, wardId: null, bedStatus: null, keyword: '',
});
/** 病区候选：有选中科室时按科室收窄，没选时给全院病区（条件都下推后端，前端只做下拉收窄） */
const wardRows = ref([]);
const filteredWards = computed(() => {
  const deptId = form.applyDeptId || poolQuery.deptId || query.applyDeptId;
  if (!deptId)
    return wardRows.value;
  return wardRows.value.filter((w) => String(w.deptId) === String(deptId));
});
const wardOptionsForPicker = computed(() => filteredWards.value.length ? filteredWards.value : wardRows.value);
// ---------------- 加载 ----------------
const loadQueue = async () => {
  loading.value = true;
  try {
    const res = await getBedWaitListPage(query);
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '队列加载失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '队列加载失败');
  } finally {
    loading.value = false;
  }
};
const loadPool = async () => {
  poolLoading.value = true;
  try {
    const res = await getBedPool(poolQuery);
    if (res.code === 200) {
      poolRows.value = res.data?.rows || [];
      poolTotal.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '床位池加载失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '床位池加载失败');
  } finally {
    poolLoading.value = false;
  }
};
const loadTop = async () => {
  try {
    const res = await getBedWaitStats();
    if (res.code === 200)
      stats.value = res.data || {};
  } catch { /* 概览失败不影响列表 */
  }
  try {
    const res = await countBedWaiting();
    if (res.code === 200)
      waitingCount.value = Number(res.data || 0);
  } catch { /* 忽略角标 */
  }
  try {
    const res = await getBedOverview();
    if (res.code === 200)
      overview.value = res.data || {summary: {}, deptRows: []};
  } catch { /* 忽略 */
  }
};
const loadOptions = async () => {
  try {
    const res = await getDepartmentSelectList({});
    if (res.code === 200)
      deptOptions.value = (res.data || []).filter((d) => d && d.deptName);
  } catch {
    deptOptions.value = [];
  }
  try {
    const res = await getInpatientWardList();
    if (res.code === 200)
      wardRows.value = res.data || [];
    wardOptions.value = wardRows.value;
  } catch {
    wardRows.value = [];
  }
  try {
    const res = await getEmployeeList({empType: 1});
    if (res.code === 200)
      employees.value = res.data?.records || res.data || [];
  } catch {
    employees.value = [];
  }
};
onMounted(async () => {
  await loadOptions();
  await Promise.all([loadTop(), loadQueue(), loadPool(), loadMap()]);
});
const reloadAll = async () => {
  await Promise.all([loadTop(), loadQueue(), loadPool(), loadMap()]);
};
// ---------------- 登记 / 改需求 ----------------
const formVisible = ref(false);
const isolation = ref(false);
const form = reactive({
  id: null, patientId: null, patientName: '', patientNo: '', gender: null, age: null, phone: '',
  admissionOrderId: null, applyDeptId: null, applyDeptName: '', expectWardId: null, bedType: 'normal',
  priority: 1, genderLimit: 0, isolationFlag: 0, expectAdmitDate: null, diagnosisName: '', remark: '',
});
const resetForm = () => {
  Object.assign(form, {
    id: null, patientId: null, patientName: '', patientNo: '', gender: null, age: null, phone: '',
    admissionOrderId: null, applyDeptId: null, applyDeptName: '', expectWardId: null, bedType: 'normal',
    priority: 1, genderLimit: 0, isolationFlag: 0, expectAdmitDate: null, diagnosisName: '', remark: '',
  });
  isolation.value = false;
};
const openCreate = () => {
  resetForm();
  formVisible.value = true;
};
const openEdit = (row) => {
  resetForm();
  Object.assign(form, {
    id: row.id, patientId: row.patientId, patientName: row.patientName, gender: row.gender, age: row.age,
    phone: row.phone, admissionOrderId: row.admissionOrderId, applyDeptId: row.applyDeptId,
    expectWardId: row.expectWardId, bedType: row.bedType, priority: row.priority,
    genderLimit: row.genderLimit ?? 0, diagnosisName: row.diagnosisName,
  });
  isolation.value = Number(row.isolationFlag) === 1;
  formVisible.value = true;
};
const onPatientSelect = (p) => {
  if (!p)
    return;
  form.patientName = p.patientName || '';
  form.patientNo = p.patientNo || '';
  form.gender = p.gender ?? null;
  form.age = p.age ?? null;
  form.phone = p.phone || '';
};
const submitForm = async () => {
  if (!form.id && !form.patientId)
    return ElMessage.warning('请选择患者');
  if (!form.id && !form.patientName)
    return ElMessage.warning('未取到患者姓名，请重新选择患者');
  submitting.value = true;
  try {
    const payload = {
      ...form,
      patientId: form.patientId ? String(form.patientId) : null,
      isolationFlag: isolation.value ? 1 : 0,
    };
    const res = await upsertBedWait(payload);
    if (res.code === 200) {
      ElMessage.success(res.message || '排队登记成功');
      formVisible.value = false;
      await reloadAll();
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};
// ---------------- 安排床位 ----------------
const assignVisible = ref(false);
const current = ref(null);
const assignMode = ref('match');
const assignRows = ref([]);
const pickedBedId = ref('');
const manualKeyword = ref('');
const openAssign = async (row) => {
  current.value = row;
  pickedBedId.value = '';
  assignMode.value = 'match';
  manualKeyword.value = '';
  assignVisible.value = true;
  await loadMatch();
};
const loadMatch = async () => {
  assignRows.value = [];
  try {
    const res = await matchBeds(current.value.id);
    if (res.code === 200) {
      assignRows.value = res.data || [];
      if (!assignRows.value.length) {
        ElMessage.warning('当前没有符合硬条件的空闲床位（普通需求不会占用 ICU / 特需床）');
      }
    } else {
      ElMessage.error(res.message || '床位匹配失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '床位匹配失败');
  }
};
const loadManualBeds = async () => {
  assignRows.value = [];
  try {
    const res = await getBedPool({
      pageNum: 1, pageSize: 100, availableOnly: true, deptId: current.value?.applyDeptId || null,
      keyword: manualKeyword.value || null,
    });
    if (res.code === 200) {
      assignRows.value = (res.data?.rows || []).map((b) => ({...b, bedTypeText: b.bedTypeText}));
    } else {
      ElMessage.error(res.message || '床位查询失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '床位查询失败');
  }
};
const onPickBed = (row) => {
  if (row)
    pickedBedId.value = String(row.bedId);
};
const submitAssign = async () => {
  if (!pickedBedId.value)
    return ElMessage.warning('请选择一张床位');
  submitting.value = true;
  try {
    const res = await assignBed({waitId: String(current.value.id), bedId: pickedBedId.value});
    if (res.code === 200) {
      ElMessage.success(res.message || '床位已预留');
      assignVisible.value = false;
      await reloadAll();
    } else {
      ElMessage.error(res.message || '安排失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '安排失败');
  } finally {
    submitting.value = false;
  }
};
// ---------------- 退回 / 取消 ----------------
const reasonVisible = ref(false);
const reason = ref('');
const reasonKind = ref('cancel');
const reasonTitle = computed(() => reasonKind.value === 'release' ? '退回队列（释放床位）' : '取消排队');
const reasonPlaceholder = computed(() => reasonKind.value === 'release'
    ? '该床将被释放，患者回到等待队列重新排序'
    : '取消原因必填：转院 / 改门诊治疗 / 患者放弃 ……（留存用于解释床位周转）');
const openRelease = (row) => {
  current.value = row;
  reasonKind.value = 'release';
  reason.value = '';
  reasonVisible.value = true;
};
const openCancel = (row) => {
  current.value = row;
  reasonKind.value = 'cancel';
  reason.value = '';
  reasonVisible.value = true;
};
const submitReason = async () => {
  submitting.value = true;
  try {
    const payload = {waitId: String(current.value.id), reason: reason.value};
    const res = reasonKind.value === 'release' ? await releaseBed(payload) : await cancelBedWait(payload);
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      reasonVisible.value = false;
      await reloadAll();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '操作失败');
  } finally {
    submitting.value = false;
  }
};
// ---------------- 办理入院 ----------------
const admitVisible = ref(false);
const admitForm = reactive({admitDoctorId: null, admitWay: null, remark: ''});
const openAdmit = (row) => {
  current.value = row;
  admitForm.admitDoctorId = null;
  admitForm.admitWay = row?.admissionOrderId ? 1 : null;
  admitForm.remark = '';
  admitVisible.value = true;
};
const submitAdmit = async () => {
  if (!admitForm.admitDoctorId)
    return ElMessage.warning('请选择入院医生');
  if (!current.value?.admissionOrderId && !admitForm.admitWay)
    return ElMessage.warning('请选择入院途径');
  submitting.value = true;
  try {
    const res = await admitBedWait({
      waitId: String(current.value.id),
      admitDoctorId: admitForm.admitDoctorId,
      admitWay: admitForm.admitWay,
      remark: admitForm.remark || null,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '入院登记成功');
      admitVisible.value = false;
      await reloadAll();
    } else {
      ElMessage.error(res.message || '入院失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '入院失败');
  } finally {
    submitting.value = false;
  }
};
// ---------------- 床位一张图（调配视角） ----------------
const mapLoading = ref(false);
const mapData = ref({summary: {}, beds: [], deptOptions: [], wardOptions: []});
const mapQuery = reactive({deptId: null, wardId: null});
const waitingRows = ref([]);
const mapBeds = computed(() => mapData.value.beds || []);
const mapDeptOptions = computed(() => mapData.value.deptOptions || []);
const mapWardOptions = computed(() => mapData.value.wardOptions || []);
/** 按病区分组：不分组的话几十张床平铺一屏，看不出哪一片还空着 */
const groupedBeds = computed(() => {
  const m = new Map();
  for (const b of mapBeds.value) {
    const key = String(b.wardId || 'none');
    if (!m.has(key)) {
      m.set(key, {wardId: key, wardName: b.wardName || '未分病区', beds: [], occupied: 0, reserved: 0});
    }
    const g = m.get(key);
    g.beds.push(b);
    if (b.bedStatus === 2)
      g.occupied += 1;
    if (b.bedStatus === 3)
      g.reserved += 1;
  }
  return Array.from(m.values());
});
const loadMap = async () => {
  mapLoading.value = true;
  try {
    const res = await getBedCenterMap({deptId: mapQuery.deptId || null, wardId: mapQuery.wardId || null});
    if (res.code === 200) {
      mapData.value = res.data || {};
      // 没指定科室时服务端会替我们落一个，回写到下拉 —— 否则会出现
      // 「下拉看着是 A 科室、画出来的是 B 科室」，这种错位最难排查
      if (!mapQuery.deptId && res.data?.deptId)
        mapQuery.deptId = res.data.deptId;
    } else {
      ElMessage.error(res.message || '床位图加载失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '床位图加载失败');
  } finally {
    mapLoading.value = false;
  }
};
const onMapDeptChange = () => {
  mapQuery.wardId = null;
  loadMap();
};
/** 床卡配色与顶部图例同源，别在两处各写一遍颜色 */
const bedCardClass = (bed) => {
  switch (bed.bedStatus) {
    case 1:
      return 'bc-bed-free';
    case 2:
      return 'bc-bed-occupied';
    case 3:
      return 'bc-bed-locked';
    default:
      return 'bc-bed-repair';
  }
};
// ---- 点床看详情 ----
const bedVisible = ref(false);
const bedDetail = ref({});
const onBedClick = (bed) => {
  bedDetail.value = bed;
  bedVisible.value = true;
};
// ---- 图上预留 ----
const reserveVisible = ref(false);
const reserveWaitId = ref('');
const reserveHint = ref('');
const loadWaitingRows = async () => {
  try {
    const res = await getBedWaitListPage({pageNum: 1, pageSize: 100, waitStatus: 0});
    if (res.code === 200)
      waitingRows.value = res.data?.records || [];
  } catch {
    waitingRows.value = [];
  }
};
const openReserve = async () => {
  reserveWaitId.value = '';
  reserveHint.value = '';
  bedVisible.value = false;
  reserveVisible.value = true;
  await loadWaitingRows();
};
/**
 * 选完患者后核对匹配档位：<b>只提示，不拦截</b>。
 * 后端 matchBeds 的口径本来就是「给候选不给最优解」—— 跨科、床型降级这类判断
 * 不该被系统替现场的人做掉，但"这张床不在推荐里"必须说清楚，
 * 否则事后复盘没人知道当时为什么这么安排。
 */
const onReservePick = async () => {
  reserveHint.value = '';
  if (!reserveWaitId.value)
    return;
  try {
    const res = await matchBeds(reserveWaitId.value);
    if (res.code !== 200)
      return;
    const hit = (res.data || []).find((m) => String(m.bedId) === String(bedDetail.value.bedId));
    reserveHint.value = hit
        ? `匹配档位：${hit.matchLevelText || hit.matchLevel}（${hit.matchScore ?? '-'} 分）— ${hit.matchReason || ''}`
        : '该床不在推荐候选内（可能跨科或床型降级），确认后仍会强制安排';
  } catch {
    /* 提示拿不到不阻断安排 */
  }
};
const submitReserve = async () => {
  if (!reserveWaitId.value)
    return ElMessage.warning('请选择要安排的患者');
  submitting.value = true;
  try {
    const res = await assignBed({waitId: reserveWaitId.value, bedId: String(bedDetail.value.bedId)});
    if (res.code === 200) {
      ElMessage.success(res.message || '床位已预留');
      reserveVisible.value = false;
      await reloadAll();
    } else {
      ElMessage.error(res.message || '预留失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '预留失败');
  } finally {
    submitting.value = false;
  }
};
// ---- 图上释放 / 入院 ----
const releaseFromMap = () => {
  current.value = {id: bedDetail.value.reservedWaitId};
  reasonKind.value = 'release';
  reason.value = '';
  bedVisible.value = false;
  reasonVisible.value = true;
};
const admitFromMap = async () => {
  try {
    const res = await getBedWaitDetail(bedDetail.value.reservedWaitId);
    if (res.code === 200 && res.data) {
      bedVisible.value = false;
      openAdmit(res.data);
    } else {
      ElMessage.error(res.message || '未取到排队记录');
    }
  } catch (e) {
    ElMessage.error(e?.message || '未取到排队记录');
  }
};
// ---------------- 展示 ----------------
const priorityTag = (p) => (p === 3 ? 'danger' : p === 2 ? 'warning' : 'info');
const waitStatusTag = (s) => (s === 0 ? 'warning' : s === 1 ? 'primary' : s === 2 ? 'success' : 'info');
const bedStatusTag = (s) => (s === 1 ? 'success' : s === 2 ? 'info' : s === 3 ? 'warning' : 'danger');
</script>

<style scoped>
.bc-tabs :deep(.el-tabs__content) {
  height: calc(100% - 40px);
}

.bc-tabs :deep(.el-tab-pane) {
  height: 100%;
}

/* 床位卡配色：与顶部图例的 .bc-dot-* 一一对应，改色必须两处一起改 */
.bc-bed-free {
  border-color: #a7f3d0;
  background: #ecfdf5;
}

.bc-bed-occupied {
  border-color: #bfdbfe;
  background: #eff6ff;
}

.bc-bed-locked {
  border-color: #fcd34d;
  background: #fffbeb;
}

.bc-bed-repair {
  border-color: #e2e8f0;
  background: #f8fafc;
  opacity: 0.7;
}

.bc-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  margin-right: 4px;
  border-radius: 9999px;
  vertical-align: -1px;
}

.bc-dot-free {
  background: #34d399;
}

.bc-dot-occupied {
  background: #60a5fa;
}

.bc-dot-locked {
  background: #fbbf24;
}

.bc-dot-repair {
  background: #cbd5e1;
}
</style>
