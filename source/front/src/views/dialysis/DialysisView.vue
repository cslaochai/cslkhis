<template>
  <div class="p-5">
    <el-tabs v-model="activeTab" data-testid="hd-tabs" @tab-change="onTabChange">
      <!-- ============ 日看板 ============ -->
      <el-tab-pane label="机位日看板" name="board">
        <div v-loading="boardLoading" class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
          <div class="flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800">透析机位排班看板</h3>
            <el-date-picker v-model="boardDate" :clearable="false" class="w-40" data-testid="hd-board-date"
                            placeholder="日期" type="date" value-format="YYYY-MM-DD" @change="loadBoard"/>
            <el-button :icon="Refresh" @click="loadBoard">刷新</el-button>
            <span class="text-sm text-slate-500">
              当日已排 {{ board.sessionCount }} 例，完成 {{ board.doneCount }} 例；点空格子排班，点已占格子看治疗单
            </span>
          </div>

          <div v-for="group in slotTables" :key="group.slot" class="space-y-2">
            <div class="text-sm font-medium text-slate-600">{{ dictText('slot', group.slot) }}</div>
            <el-table :data="group.rows" :data-testid="`hd-board-slot${group.slot}`" border size="small">
              <el-table-column label="机位" prop="machineNo" width="90"/>
              <el-table-column label="分区" prop="roomName" width="130">
                <template #default="{ row }">{{ row.roomName || '—' }}</template>
              </el-table-column>
              <el-table-column label="机位状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="machineStatusTag(row.machineStatus)" size="small">
                    {{ dictText('machineStatus', row.machineStatus) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="患者" min-width="150">
                <template #default="{ row }">
                  <span v-if="row.sessionId">{{ row.patientName }}（{{ row.patientNo }}）</span>
                  <span v-else class="text-slate-400">空闲</span>
                </template>
              </el-table-column>
              <el-table-column label="治疗单" width="150">
                <template #default="{ row }">
                  <el-tag v-if="row.sessionId" :type="sessionStatusTag(row.sessionStatus)" size="small">
                    {{ row.sessionNo }} · {{ dictText('sessionStatus', row.sessionStatus) }}
                  </el-tag>
                  <span v-else class="text-slate-400">—</span>
                </template>
              </el-table-column>
              <el-table-column align="right" label="透前/透后 (kg)" width="140">
                <template #default="{ row }">{{ row.beforeWeight ?? '—' }} / {{ row.afterWeight ?? '—' }}</template>
              </el-table-column>
              <el-table-column align="right" label="超滤 (ml)" width="110">
                <template #default="{ row }">{{ row.ultraMl ?? '—' }}</template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="110">
                <template #default="{ row }">
                  <el-button :data-testid="`hd-board-btn-${group.slot}-${row.machineNo}`" link size="small"
                             type="primary"
                             @click="onCellClick({ ...row, slot: group.slot })">
                    {{ row.sessionId ? '查看' : '排班' }}
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-4 text-sm text-slate-400">还没有机位，请先到「血液净化机位管理」菜单维护</div>
              </template>
            </el-table>
          </div>
        </div>
      </el-tab-pane>

      <!-- ============ 透析档案 ============ -->
      <el-tab-pane label="透析档案" name="archive">
        <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
          <div class="flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800 mr-2">透析患者档案（一人一档）</h3>
            <el-input v-model="arQuery.dialysisNo" class="w-40" clearable data-testid="hd-ar-no" placeholder="透析号"/>
            <el-input v-model="arQuery.patientName" class="w-40" clearable data-testid="hd-ar-name"
                      placeholder="患者姓名"/>
            <el-select v-model="arQuery.accessType" :fit-input-width="false" class="w-40" clearable
                       placeholder="血管通路">
              <el-option v-for="d in dicts.access" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="arQuery.status" :fit-input-width="false" class="w-32" clearable placeholder="档案状态">
              <el-option v-for="d in dicts.archiveStatus" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-button :icon="Search" @click="() => { arQuery.pageNum = 1; loadArchive() }">查询</el-button>
            <el-button :icon="Refresh" @click="resetArchiveQuery">重置</el-button>
            <el-button v-perm="'medtech:dialysis:add'" :icon="Plus" data-testid="hd-ar-add-btn" type="primary"
                       @click="openArchiveEdit()">新建档案
            </el-button>
          </div>

          <el-table v-loading="arLoading" :data="arRows" border data-testid="hd-archive-table">
            <el-table-column label="透析号" prop="dialysisNo" width="140"/>
            <el-table-column label="患者" prop="patientName" width="100"/>
            <el-table-column label="患者编号" prop="patientNo" width="130"/>
            <el-table-column label="联系电话" width="120">
              <template #default="{ row }">{{ row.phoneMasked || '—' }}</template>
            </el-table-column>
            <el-table-column label="首透日期" prop="firstDialysisDate" width="110"/>
            <el-table-column label="血管通路" width="120">
              <template #default="{ row }">{{ dictText('access', row.accessType) }}</template>
            </el-table-column>
            <el-table-column label="通路部位" min-width="140" prop="accessSite">
              <template #default="{ row }">{{ row.accessSite || '—' }}</template>
            </el-table-column>
            <el-table-column label="频次" width="110">
              <template #default="{ row }">{{ dictText('freq', row.dialysisFreq) }}</template>
            </el-table-column>
            <el-table-column label="有效处方" min-width="180">
              <template #default="{ row }">
                <span v-if="row.activePrescriptionId">
                  干体重 {{ row.dryWeight }}kg · {{ row.durationMin }}min · {{ dictText('dialyzer', row.dialyzer) }}
                </span>
                <el-tag v-else size="small" type="warning">未开处方</el-tag>
              </template>
            </el-table-column>
            <el-table-column align="right" label="例次(完成/总)" width="120">
              <template #default="{ row }">{{ row.sessionDone || 0 }}/{{ row.sessionTotal || 0 }}</template>
            </el-table-column>
            <el-table-column label="最近透析" prop="lastSessionDate" width="110">
              <template #default="{ row }">{{ row.lastSessionDate || '—' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="archiveStatusTag(row.status)" size="small">{{
                    dictText('archiveStatus', row.status)
                  }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="220">
              <template #default="{ row }">
                <el-button v-perm="'medtech:dialysis:add'" data-testid="hd-ar-edit-btn" link size="small"
                           type="primary" @click="openArchiveEdit(row)">编辑
                </el-button>
                <el-button data-testid="hd-ar-pre-btn" link size="small" type="primary"
                           @click="openPrescription(row)">处方
                </el-button>
                <el-button v-if="canChangeArchive(row.status)" v-perm="'medtech:dialysis:add'" data-testid="hd-ar-status-btn" link
                           size="small"
                           type="warning" @click="openStatusChange(row)">状态
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400">还没有透析档案，点「新建档案」从患者主档选人生成</div>
            </template>
          </el-table>

          <el-pagination v-model:current-page="arQuery.pageNum" v-model:page-size="arQuery.pageSize"
                         :page-sizes="PAGE_SIZES" :total="arTotal" layout="total, sizes, prev, pager, next"
                         @current-change="loadArchive" @size-change="() => { arQuery.pageNum = 1; loadArchive() }"/>
        </div>
      </el-tab-pane>

      <!-- ============ 透析单台账 ============ -->
      <el-tab-pane label="透析单台账" name="session">
        <div class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
          <div class="flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800 mr-2">透析单（排班→上机→下机）</h3>
            <el-input v-model="seQuery.sessionNo" class="w-40" clearable data-testid="hd-se-no" placeholder="透析单号"/>
            <el-input v-model="seQuery.patientName" class="w-40" clearable data-testid="hd-se-name"
                      placeholder="患者姓名"/>
            <el-date-picker v-model="seQuery.startDate" class="w-36" placeholder="开始日期" type="date"
                            value-format="YYYY-MM-DD"/>
            <el-date-picker v-model="seQuery.endDate" class="w-36" placeholder="结束日期" type="date"
                            value-format="YYYY-MM-DD"/>
            <el-select v-model="seQuery.timeSlot" :fit-input-width="false" class="w-28" clearable placeholder="时段">
              <el-option v-for="d in dicts.slot" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="seQuery.status" :fit-input-width="false" class="w-28" clearable placeholder="状态">
              <el-option v-for="d in dicts.sessionStatus" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-button :icon="Search" @click="() => { seQuery.pageNum = 1; loadSession() }">查询</el-button>
            <el-button :icon="Refresh" @click="resetSessionQuery">重置</el-button>
          </div>

          <el-table v-loading="seLoading" :data="seRows" border data-testid="hd-session-table">
            <el-table-column label="透析单号" prop="sessionNo" width="150"/>
            <el-table-column label="日期" prop="dialysisDate" width="110"/>
            <el-table-column label="时段" width="80">
              <template #default="{ row }">{{ dictText('slot', row.timeSlot) }}</template>
            </el-table-column>
            <el-table-column label="机位" prop="machineNo" width="90"/>
            <el-table-column label="患者" prop="patientName" width="100"/>
            <el-table-column label="处方快照" width="160">
              <template #default="{ row }">{{ row.dryWeight }}kg / {{ row.durationMin }}min / {{
                  row.bloodFlow
                }}ml·min
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="sessionStatusTag(row.status)" size="small">{{
                    dictText('sessionStatus', row.status)
                  }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column align="right" label="透前/透后" width="120">
              <template #default="{ row }">{{ row.beforeWeight ?? '—' }} / {{ row.afterWeight ?? '—' }}</template>
            </el-table-column>
            <el-table-column align="right" label="超滤(ml)" prop="ultraMl" width="100">
              <template #default="{ row }">{{ row.ultraMl ?? '—' }}</template>
            </el-table-column>
            <el-table-column align="right" label="实际时长" prop="actualDurationMin" width="90">
              <template #default="{ row }">{{ row.actualDurationMin ?? '—' }}</template>
            </el-table-column>
            <el-table-column label="不良反应" width="120">
              <template #default="{ row }">
                <el-tag v-if="row.adverseType" size="small" type="danger">{{
                    dictText('adverse', row.adverseType)
                  }}
                </el-tag>
                <span v-else class="text-slate-400">—</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="290">
              <template #default="{ row }">
                <el-button data-testid="hd-se-view-btn" link size="small" type="primary"
                           @click="openSessionView(row.id)">详情
                </el-button>
                <el-button v-if="canStart(row.status)" v-perm="'medtech:dialysis:edit'" data-testid="hd-se-start-btn" link size="small"
                           type="success" @click="openAction('start', row)">上机
                </el-button>
                <el-button v-if="canFinish(row.status)" v-perm="'medtech:dialysis:edit'" data-testid="hd-se-finish-btn" link
                           size="small"
                           type="success" @click="openAction('finish', row)">下机
                </el-button>
                <el-button v-if="canRecordAdverse(row.status)" v-perm="'medtech:dialysis:edit'" data-testid="hd-se-adverse-btn" link
                           size="small"
                           type="warning" @click="openAction('adverse', row)">反应
                </el-button>
                <el-button v-if="canReschedule(row.status)" v-perm="'medtech:dialysis:edit'" data-testid="hd-se-resch-btn" link
                           size="small"
                           type="primary" @click="openAction('reschedule', row)">改期
                </el-button>
                <el-button v-if="canCancel(row.status)" v-perm="'medtech:dialysis:edit'" data-testid="hd-se-cancel-btn" link size="small"
                           type="danger" @click="openAction('cancel', row)">取消
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400">还没有透析单，请到「机位日看板」点空格子排班</div>
            </template>
          </el-table>

          <el-pagination v-model:current-page="seQuery.pageNum" v-model:page-size="seQuery.pageSize"
                         :page-sizes="PAGE_SIZES" :total="seTotal" layout="total, sizes, prev, pager, next"
                         @current-change="loadSession" @size-change="() => { seQuery.pageNum = 1; loadSession() }"/>
        </div>
      </el-tab-pane>

      <!-- 机位管理页签已拆为独立菜单 2931（sql/186） -->

      <!-- ============ 工作量统计 ============ -->
      <el-tab-pane label="工作量统计" name="stats">
        <div v-loading="stLoading" class="bg-white rounded-lg border border-slate-200 p-4 space-y-3">
          <div class="flex items-center gap-3 flex-wrap">
            <h3 class="font-semibold text-slate-800">透析工作量与不良反应</h3>
            <el-date-picker v-model="stRange" class="w-64" data-testid="hd-stats-range" end-placeholder="结束日期"
                            range-separator="至" start-placeholder="开始日期" type="daterange"
                            value-format="YYYY-MM-DD"/>
            <el-button :icon="Search" @click="loadStats">统计</el-button>
          </div>

          <div v-if="stats" class="grid grid-cols-2 md:grid-cols-4 gap-3" data-testid="hd-stats-cards">
            <div class="border border-slate-200 rounded p-3">
              <div class="text-xs text-slate-500">在透患者</div>
              <div class="text-2xl font-semibold text-slate-800">{{ stats.inDialysisPatients ?? 0 }}</div>
            </div>
            <div class="border border-slate-200 rounded p-3">
              <div class="text-xs text-slate-500">区间例次（完成/取消）</div>
              <div class="text-2xl font-semibold text-slate-800">
                {{ stats.sessionTotal ?? 0 }}
                <span class="text-sm text-slate-500">（{{ stats.doneCount ?? 0 }}/{{ stats.cancelledCount ?? 0 }}）</span>
              </div>
            </div>
            <div class="border border-slate-200 rounded p-3">
              <div class="text-xs text-slate-500">人均例次</div>
              <div class="text-2xl font-semibold text-slate-800">{{ stats.sessionsPerPatient ?? '—' }}</div>
            </div>
            <div class="border border-slate-200 rounded p-3">
              <div class="text-xs text-slate-500">平均超滤 (ml) / 平均时长 (min)</div>
              <div class="text-2xl font-semibold text-slate-800">
                {{ stats.avgUltraMl ?? '—' }} / {{ stats.avgActualDurationMin ?? '—' }}
              </div>
            </div>
          </div>

          <div v-if="stats" class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <div class="text-sm font-medium text-slate-600 mb-2">不良反应分布</div>
              <el-table :data="stats.adverseTypes || []" border data-testid="hd-stats-adverse" max-height="240"
                        size="small">
                <el-table-column label="类型" min-width="160">
                  <template #default="{ row }">{{ dictText('adverse', row.type) }}</template>
                </el-table-column>
                <el-table-column align="right" label="例次" prop="count" width="100"/>
                <template #empty>
                  <div class="py-4 text-sm text-slate-400">区间内没有不良反应登记</div>
                </template>
              </el-table>
            </div>
            <div>
              <div class="text-sm font-medium text-slate-600 mb-2">机位负荷</div>
              <el-table :data="stats.machineLoads || []" border data-testid="hd-stats-machine" max-height="240"
                        size="small">
                <el-table-column label="机位" min-width="140" prop="machineNo"/>
                <el-table-column align="right" label="例次" prop="count" width="100"/>
                <template #empty>
                  <div class="py-4 text-sm text-slate-400">区间内没有排班</div>
                </template>
              </el-table>
            </div>
          </div>
          <div v-if="stats && (stats.onMachineCount || stats.scheduledCount)" class="text-sm text-slate-500">
            区间内透析中 {{ stats.onMachineCount }} 例、待上机 {{ stats.scheduledCount }} 例
          </div>
          <el-empty v-if="!stats" description="选择区间后点「统计」"/>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ============ 排班弹框 ============ -->
    <el-dialog v-model="scheduleDialog" data-testid="hd-schedule-dialog" title="透析排班" width="560px">
      <el-form label-width="90px">
        <el-form-item label="患者">
          <el-select v-model="scheduleForm.archiveId" :fit-input-width="false" class="w-full" data-testid="hd-schedule-archive"
                     filterable placeholder="选择在透患者">
            <el-option v-for="a in archiveOptions" :key="a.id" :label="`${a.patientName}（${a.patientNo}）${a.dialysisNo}`"
                       :value="a.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="日期">
          <el-date-picker v-model="scheduleForm.dialysisDate" :clearable="false" class="w-40"
                          data-testid="hd-schedule-date" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="scheduleForm.timeSlot" :fit-input-width="false" class="w-32"
                     data-testid="hd-schedule-slot">
            <el-option v-for="d in dicts.slot" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="机位">
          <el-select v-model="scheduleForm.machineId" :fit-input-width="false" class="w-40" data-testid="hd-schedule-machine"
                     filterable>
            <el-option v-for="m in machineOptions" :key="m.id" :label="`${m.machineNo} ${m.roomName || ''}`"
                       :value="m.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="scheduleForm.remark" data-testid="hd-schedule-remark" placeholder="可空"/>
        </el-form-item>
        <div class="text-xs text-slate-500 pl-[90px]">
          排班会把该患者当前有效处方（干体重/时长/血流速/透析器/抗凝）整套快照进透析单；同一机位同一时段只能排一人。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialog = false">取消</el-button>
        <el-button :loading="scheduling" data-testid="hd-schedule-ok" type="primary" @click="doSchedule">确认排班
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 档案弹框 ============ -->
    <el-dialog v-model="archiveDialog" :title="archiveForm.id ? '编辑透析档案' : '新建透析档案'" data-testid="hd-archive-dialog"
               width="620px">
      <el-form label-width="100px">
        <el-form-item label="患者" required>
          <PatientSelect v-if="!archiveForm.id" v-model="archiveForm.patientId" data-testid="hd-archive-patient"
                         @select="onPatientPicked"/>
          <span v-else>{{ archiveForm.patientName }}（{{ archiveForm.dialysisNo }}）</span>
        </el-form-item>
        <el-form-item label="首透日期" required>
          <el-date-picker v-model="archiveForm.firstDialysisDate" :clearable="false" class="w-40"
                          data-testid="hd-archive-first-date" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="血管通路" required>
          <el-select v-model="archiveForm.accessType" :fit-input-width="false" class="w-44" data-testid="hd-archive-access"
                     placeholder="选择通路">
            <el-option v-for="d in dicts.access" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="通路部位">
          <el-input v-model="archiveForm.accessSite" data-testid="hd-archive-site"
                    placeholder="如 左前臂桡动脉-头静脉"/>
        </el-form-item>
        <el-form-item label="透析频次">
          <el-select v-model="archiveForm.dialysisFreq" :fit-input-width="false" class="w-40"
                     data-testid="hd-archive-freq">
            <el-option v-for="d in dicts.freq" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="原发病">
          <el-input v-model="archiveForm.cause" :rows="2" data-testid="hd-archive-cause" placeholder="如 慢性肾小球肾炎致慢性肾脏病5期"
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="archiveForm.remark" data-testid="hd-archive-remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="archiveDialog = false">取消</el-button>
        <el-button :loading="archiveSaving" data-testid="hd-archive-ok" type="primary" @click="saveArchive">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 档案状态弹框 ============ -->
    <el-dialog v-model="statusDialog" data-testid="hd-status-dialog" title="档案状态变更" width="520px">
      <el-form label-width="90px">
        <el-form-item label="患者">{{ statusForm.patientName }}（{{ statusForm.dialysisNo }}）</el-form-item>
        <el-form-item label="当前状态">{{ dictText('archiveStatus', statusForm.currentStatus) }}</el-form-item>
        <el-form-item label="目标状态" required>
          <el-select v-model="statusForm.status" :fit-input-width="false" class="w-40" data-testid="hd-status-target"
                     placeholder="选择状态">
            <el-option v-for="d in dicts.archiveStatus" :key="d.dictValue" :disabled="Number(d.dictValue) === Number(statusForm.currentStatus)"
                       :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="statusForm.reason" :rows="2" data-testid="hd-status-reason"
                    placeholder="暂停/退出必填（转腹透、肾移植、死亡、失访等）" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialog = false">取消</el-button>
        <el-button :loading="statusSaving" data-testid="hd-status-ok" type="primary" @click="saveStatus">确认变更
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 处方弹框 ============ -->
    <el-dialog v-model="preDialog" :title="`透析处方 · ${preArchive?.patientName || ''}`" data-testid="hd-prescription-dialog"
               width="900px">
      <div class="space-y-3">
        <el-table v-loading="preLoading" :data="preRows" border data-testid="hd-prescription-table" max-height="240"
                  size="small">
          <el-table-column label="生效日" prop="startDate" width="110"/>
          <el-table-column label="停用日" prop="endDate" width="110">
            <template #default="{ row }">{{ row.endDate || '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="prescriptionStatusTag(row.status)" size="small">
                {{ row.status === 1 ? '有效' : '已停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column align="right" label="干体重" prop="dryWeight" width="90"/>
          <el-table-column align="right" label="时长(min)" prop="durationMin" width="90"/>
          <el-table-column align="right" label="血流(ml/min)" prop="bloodFlow" width="110"/>
          <el-table-column label="透析器" width="130">
            <template #default="{ row }">{{ dictText('dialyzer', row.dialyzer) }}</template>
          </el-table-column>
          <el-table-column label="抗凝" width="110">
            <template #default="{ row }">{{ dictText('anticoag', row.anticoagulant) }}</template>
          </el-table-column>
          <el-table-column label="剂量" min-width="140" prop="anticoagDose">
            <template #default="{ row }">{{ row.anticoagDose || '—' }}</template>
          </el-table-column>
          <el-table-column label="开立人" prop="doctorName" width="90"/>
          <el-table-column fixed="right" label="操作" width="130">
            <template #default="{ row }">
              <el-button v-if="row.status === 1" v-perm="'medtech:dialysis:add'" data-testid="hd-pre-edit-btn" link size="small"
                         type="primary" @click="openPreEdit(row)">编辑
              </el-button>
              <el-button v-if="canStopPrescription(row.status)" v-perm="'medtech:dialysis:add'" data-testid="hd-pre-stop-btn" link
                         size="small"
                         type="danger" @click="stopPrescription(row)">停用
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-4 text-sm text-slate-400">还没有处方，下面填一张（没有处方不能排班）</div>
          </template>
        </el-table>

        <div v-if="canWritePrescription(preArchive?.status)" class="border border-slate-200 rounded p-3 space-y-2">
          <div class="text-sm font-medium text-slate-600">{{ preForm.id ? '修改处方' : '新开处方' }}</div>
          <div class="flex items-center gap-3 flex-wrap">
            <el-input-number v-model="preForm.dryWeight" :controls="false" :max="999.99" :min="0" :precision="2"
                             class="w-28" data-testid="hd-pre-dry" placeholder="干体重kg"/>
            <el-input-number v-model="preForm.durationMin" :controls="false" :max="720" :min="30"
                             class="w-24" data-testid="hd-pre-duration" placeholder="时长min"/>
            <el-input-number v-model="preForm.bloodFlow" :controls="false" :max="400" :min="50"
                             class="w-28" data-testid="hd-pre-flow" placeholder="血流ml/min"/>
            <el-select v-model="preForm.dialyzer" :fit-input-width="false" class="w-40" data-testid="hd-pre-dialyzer"
                       placeholder="透析器">
              <el-option v-for="d in dicts.dialyzer" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="preForm.anticoagulant" :fit-input-width="false" class="w-36" data-testid="hd-pre-anticoag"
                       placeholder="抗凝">
              <el-option v-for="d in dicts.anticoag" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-input v-model="preForm.anticoagDose" class="w-52" data-testid="hd-pre-dose" placeholder="抗凝剂量描述"/>
            <el-input-number v-model="preForm.targetUltraMl" :controls="false" :max="99999" :min="0" :precision="1"
                             class="w-32" data-testid="hd-pre-ultra" placeholder="目标超滤ml"/>
            <el-date-picker v-model="preForm.startDate" :clearable="false" class="w-36" data-testid="hd-pre-start"
                            placeholder="生效日期" type="date" value-format="YYYY-MM-DD"/>
            <el-input v-model="preForm.remark" class="w-40" data-testid="hd-pre-remark" placeholder="备注"/>
            <el-button v-perm="'medtech:dialysis:add'" :icon="preForm.id ? Edit : Plus" data-testid="hd-pre-save-btn"
                       type="primary" @click="savePrescription">{{ preForm.id ? '保存修改' : '开处方' }}
            </el-button>
            <el-button v-if="preForm.id" :icon="Refresh" @click="resetPreForm">清空重填</el-button>
          </div>
          <div class="text-xs text-slate-500">新开处方会把该档案原来的有效处方自动停用；已停用的处方不可编辑。</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="preDialog = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ============ 动作弹框（上机/下机/反应/取消/改期）============
         modelValue 声明为 Boolean，Vue 会把 '' 强转成 true，所以不能直接 v-model 绑字符串 kind，
         否则进页面就自带弹开一个空弹框（遮罩挡住整页）。 -->
    <el-dialog :model-value="!!actionDialog"
               :title="actionTitle"
               data-testid="hd-action-dialog" width="560px" @update:model-value="(v) => { if (!v) actionDialog = '' }">
      <div v-if="actionRow" class="text-sm text-slate-500 mb-3">
        {{ actionRow.patientName }} · {{ actionRow.dialysisDate }} {{ dictText('slot', actionRow.timeSlot) }} ·
        机位 {{ actionRow.machineNo }} · {{ actionRow.sessionNo }}
      </div>
      <el-form label-width="110px">
        <template v-if="actionDialog === 'start'">
          <el-form-item label="透前体重(kg)" required>
            <el-input-number v-model="actionForm.beforeWeight" :controls="false" :max="999.99" :min="0" :precision="2"
                             class="w-32" data-testid="hd-action-before-weight"/>
          </el-form-item>
          <el-form-item label="通路评估" required>
            <el-input v-model="actionForm.accessCheck" :rows="2" data-testid="hd-action-access" placeholder="如 内瘘血流量良好、无渗血"
                      type="textarea"/>
          </el-form-item>
          <el-form-item label="上机时间">
            <el-date-picker v-model="actionForm.onTime" class="w-52" data-testid="hd-action-on-time"
                            placeholder="默认当前时间" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
          </el-form-item>
        </template>
        <template v-else-if="actionDialog === 'finish'">
          <el-form-item label="透后体重(kg)" required>
            <el-input-number v-model="actionForm.afterWeight" :controls="false" :max="999.99" :min="0" :precision="2"
                             class="w-32" data-testid="hd-action-after-weight"/>
          </el-form-item>
          <el-form-item label="下机时间">
            <el-date-picker v-model="actionForm.offTime" class="w-52" data-testid="hd-action-off-time"
                            placeholder="默认当前时间" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
          </el-form-item>
          <div class="text-xs text-slate-500 pl-[110px]">超滤量
            =（透前-透后）×1000，实际时长按下机-上机回算，均由服务端计算。
          </div>
        </template>
        <template v-else-if="actionDialog === 'adverse'">
          <el-form-item label="不良反应" required>
            <el-select v-model="actionForm.adverseType" :fit-input-width="false" class="w-48" data-testid="hd-action-adverse-type"
                       placeholder="选择类型">
              <el-option v-for="d in dicts.adverse" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="处置描述">
            <el-input v-model="actionForm.adverseDesc" :rows="3" data-testid="hd-action-adverse-desc"
                      placeholder="如 透中血压下降至 90/55，降低血流速并补生理盐水 200ml"
                      type="textarea"/>
          </el-form-item>
        </template>
        <template v-else-if="actionDialog === 'cancel'">
          <el-form-item label="取消原因" required>
            <el-input v-model="actionForm.reason" :rows="2" data-testid="hd-action-reason" placeholder="如 患者临时发热，本次取消"
                      type="textarea"/>
          </el-form-item>
        </template>
        <template v-else-if="actionDialog === 'reschedule'">
          <el-form-item label="日期">
            <el-date-picker v-model="actionForm.dialysisDate" :clearable="false" class="w-40" data-testid="hd-action-date"
                            type="date" value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item label="时段">
            <el-select v-model="actionForm.timeSlot" :fit-input-width="false" class="w-32" data-testid="hd-action-slot">
              <el-option v-for="d in dicts.slot" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="机位">
            <el-select v-model="actionForm.machineId" :fit-input-width="false" class="w-40" data-testid="hd-action-machine"
                       filterable>
              <el-option v-for="m in machineOptions" :key="m.id" :label="`${m.machineNo} ${m.roomName || ''}`"
                         :value="m.id"/>
            </el-select>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="actionDialog = ''">取消</el-button>
        <el-button :loading="actionSaving" data-testid="hd-action-ok" type="primary" @click="doAction">确认</el-button>
      </template>
    </el-dialog>

    <!-- 机位弹框已随 2931 拆分移除（sql/186） -->

    <!-- ============ 透析单详情（只读）============ -->
    <el-dialog v-model="sessionViewDialog" data-testid="hd-session-dialog" title="透析单详情" width="820px">
      <el-form v-if="sessionView" disabled label-width="110px">
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="透析单号">{{ sessionView.sessionNo }}</el-form-item>
          <el-form-item label="状态">
            <el-tag :type="sessionStatusTag(sessionView.status)" size="small">
              {{ dictText('sessionStatus', sessionView.status) }}
            </el-tag>
          </el-form-item>
          <el-form-item label="日期/时段">{{ sessionView.dialysisDate }} {{
              dictText('slot', sessionView.timeSlot)
            }}
          </el-form-item>
          <el-form-item label="机位">{{ sessionView.machineNo }} {{ sessionView.roomName || '' }}</el-form-item>
          <el-form-item label="患者">{{ sessionView.patientName }}（{{ sessionView.patientNo }}）</el-form-item>
          <el-form-item label="处方快照">
            {{ sessionView.dryWeight }}kg / {{ sessionView.durationMin }}min / {{ sessionView.bloodFlow }}ml·min ·
            {{ dictText('dialyzer', sessionView.dialyzer) }} · {{ dictText('anticoag', sessionView.anticoagulant) }}
          </el-form-item>
          <el-form-item label="透前体重">{{ sessionView.beforeWeight ?? '—' }} kg</el-form-item>
          <el-form-item label="透后体重">{{ sessionView.afterWeight ?? '—' }} kg</el-form-item>
          <el-form-item label="上机">{{ sessionView.onTime || '—' }} {{ sessionView.onBy || '' }}</el-form-item>
          <el-form-item label="下机">{{ sessionView.offTime || '—' }} {{ sessionView.offBy || '' }}</el-form-item>
          <el-form-item label="实际时长">{{ sessionView.actualDurationMin ?? '—' }} min</el-form-item>
          <el-form-item label="超滤量">{{ sessionView.ultraMl ?? '—' }} ml</el-form-item>
        </div>
        <el-form-item label="通路评估">{{ sessionView.accessCheck || '—' }}</el-form-item>
        <el-form-item label="不良反应">
          <span v-if="sessionView.adverseType">
            {{ dictText('adverse', sessionView.adverseType) }}：{{ sessionView.adverseDesc || '未填处置' }}
          </span>
          <span v-else>无</span>
        </el-form-item>
        <el-form-item v-if="sessionView.cancelReason" label="取消原因">{{ sessionView.cancelReason }}</el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="sessionViewDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 血液净化（透析）中心工作台（sql/108，菜单 413）
 *
 * 四个页签（机位管理已拆为独立菜单 2931「血液净化机位管理」，sql/186）：
 *  1) 日看板：日期 × 三时段 × 全机位，空格子点一下即排班，已占格子看治疗单。
 *  2) 透析档案：一人一档（患者快照服务端重查），在透/暂停/退出，暂停与退出必须写原因。
 *  3) 透析单台账：上机（透前体重+通路评估）→ 下机（透后体重，超滤量与实际时长服务端回算）
 *     → 不良反应补登 → 改期/取消。
 *  4) 工作量统计：例次/人均/超滤均值/不良反应分布/机位负荷。
 * 规则（服务端收口，前端只做显隐）：本域不出收费单、不扣耗材；
 * 状态文案全部走字典 his_dialysis_*，tag 色与动作显隐单点 lib/dialysis.js。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  cancelDialysisSession,
  changeDialysisArchiveStatus,
  finishDialysisSession,
  getDialysisArchive,
  getDialysisBoard,
  getDialysisSession,
  getDialysisStats,
  listDialysisArchivePage,
  listDialysisMachineSelect,
  listDialysisPrescriptions,
  listDialysisSessionPage,
  recordDialysisAdverse,
  rescheduleDialysisSession,
  scheduleDialysisSession,
  startDialysisSession,
  stopDialysisPrescription,
  upsertDialysisArchive,
  upsertDialysisPrescription,
} from '@/api/dialysis';
import {getDictDataMapList} from '@/api/system';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText, localDateStr} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {
  ARCHIVE_STATUS,
  archiveStatusTag,
  canCancel,
  canChangeArchive,
  canFinish,
  canRecordAdverse,
  canReschedule,
  canScheduleCell,
  canStart,
  canStopPrescription,
  canWritePrescription,
  machineStatusTag,
  prescriptionStatusTag,
  sessionStatusTag,
} from '@/lib/dialysis';

const activeTab = ref('board');
// ---------------- 字典 ----------------
const dicts = reactive({
  archiveStatus: [], access: [], freq: [], dialyzer: [], anticoag: [],
  slot: [], sessionStatus: [], adverse: [], machineStatus: [],
});
const dictText = (key, value) => dictLabelText(dicts[key], value);
const loadDicts = async () => {
  const types = [
    DICT_TYPE.DIALYSIS_STATUS, DICT_TYPE.DIALYSIS_ACCESS, DICT_TYPE.DIALYSIS_FREQ,
    DICT_TYPE.DIALYSIS_DIALYZER, DICT_TYPE.DIALYSIS_ANTICOAG, DICT_TYPE.DIALYSIS_SLOT,
    DICT_TYPE.DIALYSIS_SESSION_STATUS, DICT_TYPE.DIALYSIS_ADVERSE, DICT_TYPE.DIALYSIS_MACHINE_STATUS,
  ];
  try {
    // selectGroup 单次最多 5 个类型（超了抛「数据字典每次最多只能查询5个」），分页取再合并
    const map = {};
    for (let i = 0; i < types.length; i += 5) {
      const res = await getDictDataMapList(types.slice(i, i + 5).join(','));
      Object.assign(map, res?.data || {});
    }
    dicts.archiveStatus = map[DICT_TYPE.DIALYSIS_STATUS] || [];
    dicts.access = map[DICT_TYPE.DIALYSIS_ACCESS] || [];
    dicts.freq = map[DICT_TYPE.DIALYSIS_FREQ] || [];
    dicts.dialyzer = map[DICT_TYPE.DIALYSIS_DIALYZER] || [];
    dicts.anticoag = map[DICT_TYPE.DIALYSIS_ANTICOAG] || [];
    dicts.slot = map[DICT_TYPE.DIALYSIS_SLOT] || [];
    dicts.sessionStatus = map[DICT_TYPE.DIALYSIS_SESSION_STATUS] || [];
    dicts.adverse = map[DICT_TYPE.DIALYSIS_ADVERSE] || [];
    dicts.machineStatus = map[DICT_TYPE.DIALYSIS_MACHINE_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 页签一：日看板 ----------------
const boardDate = ref(localDateStr());
const boardLoading = ref(false);
const board = reactive({
  slot1: [], slot2: [], slot3: [], sessionCount: 0, doneCount: 0,
});
const loadBoard = async () => {
  boardLoading.value = true;
  try {
    const res = await getDialysisBoard(boardDate.value);
    if (res.code === 200) {
      board.slot1 = res.data?.slot1 || [];
      board.slot2 = res.data?.slot2 || [];
      board.slot3 = res.data?.slot3 || [];
      board.sessionCount = res.data?.sessionCount || 0;
      board.doneCount = res.data?.doneCount || 0;
    } else {
      ElMessage.error(res.message || '加载看板失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载看板失败');
  } finally {
    boardLoading.value = false;
  }
};
const slotTables = computed(() => [
  {slot: 1, rows: board.slot1},
  {slot: 2, rows: board.slot2},
  {slot: 3, rows: board.slot3},
]);
const onCellClick = async (cell) => {
  if (cell.sessionId) {
    await openSessionView(cell.sessionId);
    return;
  }
  if (!canScheduleCell(cell)) {
    ElMessage.warning('该机位当前为维修/停用，不能排班');
    return;
  }
  await loadArchiveOptions();
  scheduleForm.dialysisDate = boardDate.value;
  scheduleForm.timeSlot = cell.slot;
  scheduleForm.machineId = cell.machineId;
  scheduleForm.archiveId = null;
  scheduleForm.remark = '';
  scheduleDialog.value = true;
};
// ---------------- 排班弹框 ----------------
const scheduleDialog = ref(false);
const scheduling = ref(false);
const archiveOptions = ref([]);
const scheduleForm = reactive({
  dialysisDate: '', timeSlot: null, machineId: null, archiveId: null, remark: '',
});
const loadArchiveOptions = async () => {
  if (archiveOptions.value.length)
    return;
  try {
    const res = await listDialysisArchivePage({status: ARCHIVE_STATUS.ON, pageNum: 1, pageSize: 100});
    archiveOptions.value = res?.data?.records || [];
  } catch (e) {
    console.error(e);
  }
};
const machineOptions = ref([]);
const loadMachineOptions = async () => {
  try {
    const res = await listDialysisMachineSelect();
    machineOptions.value = res?.data || [];
  } catch (e) {
    console.error(e);
  }
};
const doSchedule = async () => {
  if (!scheduleForm.archiveId || !scheduleForm.machineId || !scheduleForm.timeSlot || !scheduleForm.dialysisDate) {
    ElMessage.warning('请选择患者、日期、时段与机位');
    return;
  }
  scheduling.value = true;
  try {
    const res = await scheduleDialysisSession({
      archiveId: scheduleForm.archiveId,
      dialysisDate: scheduleForm.dialysisDate,
      timeSlot: scheduleForm.timeSlot,
      machineId: scheduleForm.machineId,
      remark: scheduleForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已排班');
      scheduleDialog.value = false;
      await loadBoard();
      await loadSession();
    } else {
      ElMessage.error(res.message || '排班失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '排班失败');
  } finally {
    scheduling.value = false;
  }
};
// ---------------- 页签二：透析档案 ----------------
const arLoading = ref(false);
const arRows = ref([]);
const arTotal = ref(0);
const arQuery = reactive({
  dialysisNo: '',
  patientName: '',
  accessType: null,
  status: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE
});
const loadArchive = async () => {
  arLoading.value = true;
  try {
    const res = await listDialysisArchivePage({
      dialysisNo: arQuery.dialysisNo.trim() || undefined,
      patientName: arQuery.patientName.trim() || undefined,
      accessType: arQuery.accessType ?? undefined,
      status: arQuery.status ?? undefined,
      pageNum: arQuery.pageNum,
      pageSize: arQuery.pageSize,
    });
    if (res.code === 200) {
      arRows.value = res.data?.records || [];
      arTotal.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '查询档案失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '查询档案失败');
  } finally {
    arLoading.value = false;
  }
};
const resetArchiveQuery = () => {
  arQuery.dialysisNo = '';
  arQuery.patientName = '';
  arQuery.accessType = null;
  arQuery.status = null;
  arQuery.pageNum = 1;
  loadArchive();
};
const archiveDialog = ref(false);
const archiveSaving = ref(false);
const archiveForm = reactive({
  id: null, patientId: null, patientName: '', dialysisNo: '',
  firstDialysisDate: localDateStr(), cause: '', accessType: null, accessSite: '',
  dialysisFreq: 3, remark: '',
});
const openArchiveEdit = async (row) => {
  archiveForm.id = row?.id ?? null;
  archiveForm.patientId = row?.patientId ?? null;
  archiveForm.patientName = row?.patientName ?? '';
  archiveForm.dialysisNo = row?.dialysisNo ?? '';
  archiveForm.remark = row?.remark ?? '';
  if (row?.id) {
    // 编辑回显走 getById（明文电话），列表是脱敏出参
    try {
      const res = await getDialysisArchive(row.id);
      if (res.code === 200 && res.data) {
        Object.assign(archiveForm, {
          firstDialysisDate: res.data.firstDialysisDate,
          cause: res.data.cause || '',
          accessType: res.data.accessType,
          accessSite: res.data.accessSite || '',
          dialysisFreq: res.data.dialysisFreq,
          remark: res.data.remark || '',
        });
      }
    } catch (e) {
      console.error(e);
    }
  } else {
    Object.assign(archiveForm, {
      firstDialysisDate: localDateStr(), cause: '', accessType: null, accessSite: '', dialysisFreq: 3, remark: '',
    });
  }
  archiveDialog.value = true;
};
const onPatientPicked = (p) => {
  archiveForm.patientId = p?.id ?? null;
  archiveForm.patientName = p?.patientName || p?.name || '';
};
const saveArchive = async () => {
  if (!archiveForm.patientId || !archiveForm.firstDialysisDate || !archiveForm.accessType) {
    ElMessage.warning('请选择患者、首次透析日期与血管通路');
    return;
  }
  archiveSaving.value = true;
  try {
    const res = await upsertDialysisArchive({
      id: archiveForm.id ?? undefined,
      patientId: archiveForm.patientId,
      firstDialysisDate: archiveForm.firstDialysisDate,
      cause: archiveForm.cause || undefined,
      accessType: archiveForm.accessType,
      accessSite: archiveForm.accessSite || undefined,
      dialysisFreq: archiveForm.dialysisFreq ?? undefined,
      remark: archiveForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '保存成功');
      archiveDialog.value = false;
      archiveOptions.value = [];
      await loadArchive();
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    archiveSaving.value = false;
  }
};
// 档案状态变更（暂停/退出原因必填）
const statusDialog = ref(false);
const statusSaving = ref(false);
const statusForm = reactive({id: null, dialysisNo: '', patientName: '', currentStatus: null, status: null, reason: ''});
const openStatusChange = (row) => {
  Object.assign(statusForm, {
    id: row.id, dialysisNo: row.dialysisNo, patientName: row.patientName,
    currentStatus: row.status, status: null, reason: '',
  });
  statusDialog.value = true;
};
const saveStatus = async () => {
  if (!statusForm.status) {
    ElMessage.warning('请选择目标状态');
    return;
  }
  if (statusForm.status !== ARCHIVE_STATUS.ON && !statusForm.reason.trim()) {
    ElMessage.warning('暂停/退出必须填写原因');
    return;
  }
  statusSaving.value = true;
  try {
    const res = await changeDialysisArchiveStatus({
      id: statusForm.id, status: statusForm.status, reason: statusForm.reason || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '状态已更新');
      statusDialog.value = false;
      await loadArchive();
    } else {
      ElMessage.error(res.message || '状态更新失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '状态更新失败');
  } finally {
    statusSaving.value = false;
  }
};
// ---------------- 处方（档案行内弹框）----------------
const preDialog = ref(false);
const preArchive = ref(null);
const preRows = ref([]);
const preLoading = ref(false);
const preForm = reactive({
  id: null, dryWeight: null, durationMin: 240, bloodFlow: 220, dialyzer: 3,
  anticoagulant: 1, anticoagDose: '', targetUltraMl: null, startDate: localDateStr(), remark: '',
});
const loadPrescriptions = async () => {
  if (!preArchive.value?.id)
    return;
  preLoading.value = true;
  try {
    const res = await listDialysisPrescriptions(preArchive.value.id);
    preRows.value = res?.data || [];
  } catch (e) {
    console.error(e);
  } finally {
    preLoading.value = false;
  }
};
const openPrescription = async (row) => {
  preArchive.value = row;
  resetPreForm();
  preDialog.value = true;
  await loadPrescriptions();
};
const resetPreForm = () => {
  Object.assign(preForm, {
    id: null, dryWeight: null, durationMin: 240, bloodFlow: 220, dialyzer: 3,
    anticoagulant: 1, anticoagDose: '', targetUltraMl: null, startDate: localDateStr(), remark: '',
  });
};
const openPreEdit = (row) => {
  if (!row) {
    resetPreForm();
    return;
  }
  Object.assign(preForm, {
    id: row.id, dryWeight: row.dryWeight, durationMin: row.durationMin, bloodFlow: row.bloodFlow,
    dialyzer: row.dialyzer, anticoagulant: row.anticoagulant, anticoagDose: row.anticoagDose || '',
    targetUltraMl: row.targetUltraMl, startDate: row.startDate, remark: row.remark || '',
  });
};
const savePrescription = async () => {
  if (preForm.dryWeight == null) {
    ElMessage.warning('干体重不能为空');
    return;
  }
  if (!preForm.startDate) {
    ElMessage.warning('处方生效日期不能为空');
    return;
  }
  try {
    const res = await upsertDialysisPrescription({
      id: preForm.id ?? undefined,
      archiveId: preArchive.value.id,
      dryWeight: preForm.dryWeight,
      durationMin: preForm.durationMin ?? undefined,
      bloodFlow: preForm.bloodFlow ?? undefined,
      dialyzer: preForm.dialyzer ?? undefined,
      anticoagulant: preForm.anticoagulant ?? undefined,
      anticoagDose: preForm.anticoagDose || undefined,
      targetUltraMl: preForm.targetUltraMl ?? undefined,
      startDate: preForm.startDate,
      remark: preForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '处方已保存');
      resetPreForm();
      archiveOptions.value = [];
      await loadPrescriptions();
      await loadArchive();
    } else {
      ElMessage.error(res.message || '处方保存失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '处方保存失败');
  }
};
const stopPrescription = async (row) => {
  try {
    const res = await stopDialysisPrescription({id: row.id, reason: '临床调整，停用该处方'});
    if (res.code === 200) {
      ElMessage.success(res.message || '处方已停用');
      await loadPrescriptions();
      await loadArchive();
    } else {
      ElMessage.error(res.message || '处方停用失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '处方停用失败');
  }
};
// ---------------- 页签三：透析单台账 ----------------
const seLoading = ref(false);
const seRows = ref([]);
const seTotal = ref(0);
const seQuery = reactive({
  sessionNo: '', patientName: '', startDate: '', endDate: '',
  timeSlot: null, status: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const loadSession = async () => {
  seLoading.value = true;
  try {
    const res = await listDialysisSessionPage({
      sessionNo: seQuery.sessionNo.trim() || undefined,
      patientName: seQuery.patientName.trim() || undefined,
      startDate: seQuery.startDate || undefined,
      endDate: seQuery.endDate || undefined,
      timeSlot: seQuery.timeSlot ?? undefined,
      status: seQuery.status ?? undefined,
      pageNum: seQuery.pageNum,
      pageSize: seQuery.pageSize,
    });
    if (res.code === 200) {
      seRows.value = res.data?.records || [];
      seTotal.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '查询透析单失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '查询透析单失败');
  } finally {
    seLoading.value = false;
  }
};
const resetSessionQuery = () => {
  Object.assign(seQuery, {
    sessionNo: '', patientName: '', startDate: '', endDate: '',
    timeSlot: null, status: null, pageNum: 1,
  });
  loadSession();
};
const sessionViewDialog = ref(false);
const sessionView = ref(null);
const openSessionView = async (id) => {
  try {
    const res = await getDialysisSession(id);
    if (res.code === 200) {
      sessionView.value = res.data;
      sessionViewDialog.value = true;
    } else {
      ElMessage.error(res.message || '查询透析单失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '查询透析单失败');
  }
};
// 上机 / 下机 / 不良反应 / 取消 / 改期
const actionDialog = ref('');
const actionSaving = ref(false);
const actionRow = ref(null);
const actionForm = reactive({
  beforeWeight: null, accessCheck: '', onTime: '', afterWeight: null, offTime: '',
  adverseType: null, adverseDesc: '', reason: '', dialysisDate: '', timeSlot: null, machineId: null,
});
const openAction = (kind, row) => {
  actionRow.value = row;
  actionDialog.value = kind;
  Object.assign(actionForm, {
    beforeWeight: null, accessCheck: '', onTime: '', afterWeight: null, offTime: '',
    adverseType: null, adverseDesc: '', reason: '',
    dialysisDate: row.dialysisDate, timeSlot: row.timeSlot, machineId: row.machineId,
  });
};
const doAction = async () => {
  const row = actionRow.value;
  if (!row)
    return;
  const kind = actionDialog.value;
  let payload = {id: row.id};
  let call = null;
  if (kind === 'start') {
    if (actionForm.beforeWeight == null || !actionForm.accessCheck.trim()) {
      ElMessage.warning('透前体重与通路评估都必填');
      return;
    }
    payload = {
      ...payload,
      beforeWeight: actionForm.beforeWeight,
      accessCheck: actionForm.accessCheck,
      onTime: actionForm.onTime || undefined
    };
    call = startDialysisSession;
  } else if (kind === 'finish') {
    if (actionForm.afterWeight == null) {
      ElMessage.warning('透后体重必填');
      return;
    }
    payload = {...payload, afterWeight: actionForm.afterWeight, offTime: actionForm.offTime || undefined};
    call = finishDialysisSession;
  } else if (kind === 'adverse') {
    if (!actionForm.adverseType) {
      ElMessage.warning('请选择不良反应类型');
      return;
    }
    payload = {...payload, adverseType: actionForm.adverseType, adverseDesc: actionForm.adverseDesc || undefined};
    call = recordDialysisAdverse;
  } else if (kind === 'cancel') {
    if (!actionForm.reason.trim()) {
      ElMessage.warning('取消原因必填');
      return;
    }
    payload = {...payload, reason: actionForm.reason};
    call = cancelDialysisSession;
  } else if (kind === 'reschedule') {
    if (!actionForm.dialysisDate || !actionForm.machineId || !actionForm.timeSlot) {
      ElMessage.warning('日期、时段、机位都必选');
      return;
    }
    payload = {
      ...payload,
      dialysisDate: actionForm.dialysisDate,
      timeSlot: actionForm.timeSlot,
      machineId: actionForm.machineId
    };
    call = rescheduleDialysisSession;
  }
  actionSaving.value = true;
  try {
    const res = await call(payload);
    if (res.code === 200) {
      ElMessage.success(res.message || '操作成功');
      actionDialog.value = '';
      await loadSession();
      await loadBoard();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '操作失败');
  } finally {
    actionSaving.value = false;
  }
};
const actionTitle = computed(() => ({
  start: '上机登记', finish: '下机登记', adverse: '登记不良反应', cancel: '取消治疗单', reschedule: '改期/改机位',
}[actionDialog.value] || ''));
// 机位管理页签已拆为独立菜单 2931（sql/186）；loadMachineOptions 保留供排班下拉使用。
// ---------------- 页签四：工作量统计 ----------------
const stLoading = ref(false);
const stRange = ref([localDateStr(), localDateStr()]);
const stats = ref(null);
const loadStats = async () => {
  if (!stRange.value || !stRange.value[0] || !stRange.value[1]) {
    ElMessage.warning('请选择统计区间');
    return;
  }
  stLoading.value = true;
  try {
    const res = await getDialysisStats({startDate: stRange.value[0], endDate: stRange.value[1]});
    if (res.code === 200) {
      stats.value = res.data;
    } else {
      ElMessage.error(res.message || '统计失败');
    }
  } catch (e) {
    ElMessage.error(e?.message || '统计失败');
  } finally {
    stLoading.value = false;
  }
};
const onTabChange = (name) => {
  if (name === 'board')
    loadBoard();
  if (name === 'archive')
    loadArchive();
  if (name === 'session')
    loadSession();
  if (name === 'stats')
    loadStats();
};
onMounted(() => {
  loadDicts();
  loadMachineOptions();
  loadBoard();
});
</script>

<style scoped>
:deep(.el-table .cell) {
  font-size: 13px;
  color: #1e293b;
}
</style>
