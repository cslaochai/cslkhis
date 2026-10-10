<template>
  <div class="p-5 space-y-4">
    <!-- 统计：全部来自 /emr/treatment/stats，页面不自造数字 -->
    <div class="grid grid-cols-8 gap-3">
      <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
        <div class="text-xs text-gray-500">在办疗程</div>
        <div class="text-xl font-semibold text-[#1269B5] mt-1" data-testid="stat-running">{{
            stats.runningApplies
          }}
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
        <div class="text-xs text-gray-500">今日计划次数</div>
        <div class="text-xl font-semibold text-[#1269B5] mt-1" data-testid="stat-plan">{{ stats.todayPlan }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
        <div class="text-xs text-gray-500">今日已打卡</div>
        <div class="text-xl font-semibold text-[#0E9488] mt-1" data-testid="stat-done">{{ stats.todayDone }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
        <div class="text-xs text-gray-500">今日待打卡</div>
        <div class="text-xl font-semibold text-[#B45309] mt-1" data-testid="stat-pending">{{ stats.todayPending }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
        <div class="text-xs text-gray-500">逾期未打卡</div>
        <div :class="Number(stats.overduePending) ? 'text-red-600' : 'text-gray-400'" class="text-xl font-semibold mt-1"
             data-testid="stat-overdue">{{ stats.overduePending }}
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
        <div class="text-xs text-gray-500">已做未记账</div>
        <div :class="Number(stats.unbilled) ? 'text-red-600' : 'text-gray-400'" class="text-xl font-semibold mt-1"
             data-testid="stat-unbilled">{{ stats.unbilled }}
        </div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
        <div class="text-xs text-gray-500">计费失败</div>
        <div class="text-xl font-semibold text-red-600 mt-1" data-testid="stat-chargefail">{{ stats.chargeFail }}</div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-3 shadow-sm">
        <div class="text-xs text-gray-500">今日治疗费</div>
        <div class="text-xl font-semibold text-[#0E9488] mt-1" data-testid="stat-amount">{{
            money(stats.todayAmount)
          }}
        </div>
      </div>
    </div>

    <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
      <el-tabs v-model="activeTab">
        <!-- ============ A 今日治疗台 ============ -->
        <el-tab-pane label="今日治疗台" name="board">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-button v-for="c in execCounts" :key="`b-${c.status}`" :type="boardQuery.execStatus === c.status ? 'primary' : ''" data-testid="board-chip"
                       size="small" @click="setBoardExec(c.status)">
              {{ c.label }} {{ c.count }}
            </el-button>
            <el-button :type="boardQuery.overdueOnly ? 'danger' : ''" data-testid="board-overdue" size="small"
                       @click="toggleOverdue">只看逾期
            </el-button>
            <div class="flex-1"/>
            <span class="text-xs text-gray-500">排期日期（只影响看哪天，不改变任何一次执行的归属）</span>
            <!-- el-date-picker 会把 data-* 属性吞掉（EP 的 Picker 只转发自己认识的那几个），
                 所以验收锚点挂在外层 display:contents 的 span 上：不产生盒子，布局一点不变 -->
            <span data-testid="board-date" style="display: contents">
              <el-date-picker v-model="boardQuery.planDate" :disabled="boardQuery.overdueOnly" style="width: 140px" type="date"
                              value-format="YYYY-MM-DD" @change="boardQuery.pageNum = 1; loadBoard()"/>
            </span>
            <el-button :icon="Refresh" size="small" @click="loadBoard(); loadStats()">刷新</el-button>
          </div>
          <el-table v-loading="boardLoading" :data="boardRows" data-testid="board-table" size="small">
            <el-table-column label="患者" min-width="120">
              <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 text-xs"> / {{
                  row.patientNo
                }}</span></template>
            </el-table-column>
            <el-table-column label="疗程单号" prop="applyNo" width="180"/>
            <el-table-column label="治疗项目" min-width="140" prop="itemName" show-overflow-tooltip/>
            <el-table-column align="center" label="次数" width="60">
              <template #default="{ row }"><span data-testid="board-seq">第{{ row.execSeq }}次</span></template>
            </el-table-column>
            <el-table-column label="排期" prop="planDate" width="110">
              <template #default="{ row }">
                <span :class="row.overdue ? 'text-red-600 font-medium' : ''">{{ row.planDate || '未排期' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="执行" width="90">
              <template #default="{ row }">
                <el-tag :type="execTag(row.execStatus) as any" data-testid="board-exec-status" size="small">
                  {{ row.execStatusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="计费" width="100">
              <template #default="{ row }">
                <el-tooltip v-if="row.chargeFailReason" :content="row.chargeFailReason" placement="top">
                  <el-tag :type="chargeTag(row.chargeStatus) as any" data-testid="board-charge-status" size="small">
                    {{ row.chargeStatusText }}
                  </el-tag>
                </el-tooltip>
                <el-tag v-else :type="chargeTag(row.chargeStatus) as any" data-testid="board-charge-status"
                        size="small">{{ row.chargeStatusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column align="right" label="金额" width="80">
              <template #default="{ row }">{{ money(row.chargeAmount ?? row.price) }}</template>
            </el-table-column>
            <el-table-column label="执行护士/时间" width="170">
              <template #default="{ row }">{{ row.executorName || '—' }}<span
                  class="text-gray-400 text-xs"> {{ fmtTime(row.executeTime) }}</span></template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="210">
              <template #default="{ row }">
                <el-tooltip :content="row.canExecute ? '按次打卡并计费' : (row.cannotExecuteReason || '')"
                            placement="top">
                  <span>
                    <el-button v-perm="'opd:treatmentStation:edit'" :disabled="!row.canExecute" data-testid="btn-execute" link
                               size="small" type="primary"
                               @click="openExecute(row)">打卡</el-button>
                  </span>
                </el-tooltip>
                <el-button v-if="Number(row.execStatus) === EXEC.PENDING" v-perm="'opd:treatmentStation:edit'" data-testid="btn-reschedule"
                           link size="small"
                           type="warning" @click="openReschedule(row)">改期
                </el-button>
                <el-button v-if="row.canRetryCharge" v-perm="'opd:treatmentStation:edit'" data-testid="btn-retry" link
                           size="small" type="danger"
                           @click="doRetryCharge(row)">补记
                </el-button>
                <span v-if="!row.canExecute && Number(row.execStatus) === EXEC.PENDING"
                      class="text-xs text-red-500 ml-1" data-testid="board-block-reason">{{
                    row.cannotExecuteReason
                  }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="boardQuery.pageNum" v-model:page-size="boardQuery.pageSize" :total="boardTotal"
                           background layout="total, prev, pager, next"
                           @current-change="loadBoard"/>
          </div>
        </el-tab-pane>

        <!-- ============ B 治疗开单 ============ -->
        <el-tab-pane label="治疗开单" name="open">
          <div class="rounded border border-gray-100 bg-gray-50 p-4 mb-4">
            <div class="grid grid-cols-4 gap-3">
              <div>
                <div class="text-xs text-gray-500 mb-1">患者</div>
                <PatientSelect v-model="openForm.patientId" data-testid="open-patient" size="default"
                               @clear="openForm.registOptions = []; openForm.registId = null"
                               @select="onPatientSelect"/>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">本次就诊（挂号单，决定费用归属科室与开单医生）</div>
                <el-select v-model="openForm.registId" :disabled="!openForm.patientId" :fit-input-width="false" data-testid="open-regist"
                           filterable placeholder="选择就诊" style="width: 100%">
                  <el-option v-for="r in openForm.registOptions" :key="r.id" :label="`${r.visitDate} ${r.deptName} ${r.doctorName}（${r.registNo}）`"
                             :value="r.id"/>
                </el-select>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">治疗项目（单价取价表快照）</div>
                <el-select v-model="openForm.treatmentItemId" :fit-input-width="false" :loading="openForm.itemLoading" data-testid="open-item"
                           filterable placeholder="选择项目" style="width: 100%">
                  <el-option v-for="i in openForm.itemOptions" :key="i.itemId" :label="`${i.itemName}（${i.itemCode}，${money(i.price)}/次）`"
                             :value="i.itemId"/>
                </el-select>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">疗程开始日期</div>
                <span data-testid="open-start" style="display: contents">
                  <el-date-picker v-model="openForm.startDate" style="width: 100%" type="date"
                                  value-format="YYYY-MM-DD"/>
                </span>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">总次数</div>
                <el-input-number v-model="openForm.totalTimes" :max="60" :min="1" controls-position="right"
                                 data-testid="open-times" style="width: 100%"/>
              </div>
              <div>
                <div class="text-xs text-gray-500 mb-1">间隔天数（1=每日一次）</div>
                <el-input-number v-model="openForm.intervalDays" :max="30" :min="1" controls-position="right"
                                 style="width: 100%"/>
              </div>
              <div class="col-span-2">
                <div class="text-xs text-gray-500 mb-1">备注</div>
                <el-input v-model="openForm.remark" data-testid="open-remark" maxlength="200"
                          placeholder="如：青霉素皮试阴性后执行"/>
              </div>
            </div>
            <div class="flex items-center gap-3 mt-3">
              <el-button v-perm="'opd:treatmentStation:add'" :loading="openForm.saving" data-testid="open-submit"
                         type="primary" @click="submitApply">开单并排期
              </el-button>
              <span class="text-sm text-gray-600" data-testid="open-plan">
                计划总额 {{ money(openPlanAmount) }}（{{
                  openForm.totalTimes
                }} 次 × {{ money(priceOf(openForm.treatmentItemId)) }}）
              </span>
              <span
                  class="text-xs text-gray-400">开单即按「开始日 + 间隔 × (次数−1)」一次生成全部排期流水，之后逐次打卡</span>
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="applyQuery.keyword" clearable data-testid="apply-keyword" placeholder="疗程单号/患者/项目"
                      style="width: 220px" @keyup.enter="applyQuery.pageNum = 1; loadApplies()"/>
            <el-select v-model="applyQuery.applyStatus" :fit-input-width="false" clearable placeholder="疗程状态"
                       style="width: 140px">
              <el-option v-for="d in applyStatusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-button :icon="Search" type="primary" @click="applyQuery.pageNum = 1; loadApplies()">查询</el-button>
          </div>
          <el-table v-loading="applyLoading" :data="applyRows" data-testid="apply-table" size="small">
            <el-table-column label="疗程单号" prop="applyNo" width="180"/>
            <el-table-column label="患者" min-width="120">
              <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 text-xs"> / {{
                  row.patientNo
                }}</span></template>
            </el-table-column>
            <el-table-column label="项目" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">
                <span data-testid="apply-item">{{ row.itemName }}</span>
                <span class="text-gray-400 text-xs"> {{ row.itemTypeText }}</span>
              </template>
            </el-table-column>
            <el-table-column align="center" label="进度" width="90">
              <template #default="{ row }"><span data-testid="apply-progress">{{ row.progressText }}</span></template>
            </el-table-column>
            <el-table-column label="排期" width="170">
              <template #default="{ row }">{{ row.startDate }} 起 每 {{ row.intervalDays }} 天</template>
            </el-table-column>
            <el-table-column label="开单" min-width="150" show-overflow-tooltip>
              <template #default="{ row }">{{ row.deptName || '未指定' }} {{ row.doctorName || '' }}</template>
            </el-table-column>
            <el-table-column align="right" label="单价/计划总额" width="130">
              <template #default="{ row }">{{ money(row.price) }} / <span
                  data-testid="apply-amount">{{ money(row.planAmount) }}</span></template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="Number(row.applyStatus) === 2 ? 'info' : Number(row.applyStatus) === 1 ? 'success' : 'warning'"
                        data-testid="apply-status"
                        size="small">{{ row.applyStatusText }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="210">
              <template #default="{ row }">
                <el-button data-testid="btn-apply-detail" link size="small" type="primary" @click="openDetail(row)">
                  详情
                </el-button>
                <el-button v-if="!Number(row.doneTimes) && Number(row.applyStatus) !== 2"
                           v-perm="'opd:treatmentStation:edit'" data-testid="btn-apply-reschedule" link size="small"
                           type="warning" @click="openApplyReschedule(row)">重排
                </el-button>
                <el-button v-if="Number(row.applyStatus) !== 2" v-perm="'opd:treatmentStation:delete'" data-testid="btn-apply-cancel"
                           link size="small"
                           type="danger" @click="doApplyCancel(row)">取消
                </el-button>
                <el-button v-if="!Number(row.doneTimes)" v-perm="'opd:treatmentStation:delete'" data-testid="btn-apply-delete" link
                           size="small"
                           type="danger" @click="doApplyDelete(row)">删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="applyQuery.pageNum" v-model:page-size="applyQuery.pageSize" :total="applyTotal"
                           background layout="total, prev, pager, next"
                           @current-change="loadApplies"/>
          </div>
        </el-tab-pane>

        <!-- ============ C 执行与计费台账 ============ -->
        <el-tab-pane label="执行与计费台账" name="ledger">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-button v-for="c in [0, 1, 2, 3]" :key="`c-${c}`" :type="ledQuery.chargeStatus === c ? 'primary' : ''"
                       size="small"
                       @click="ledQuery.chargeStatus = ledQuery.chargeStatus === c ? null : c; ledQuery.unbilledOnly = false; ledQuery.pageNum = 1; loadLedger()">
              {{ dictLabelText(chargeDict, c) }} {{ ledCountOf(`charge-${c}`) }}
            </el-button>
            <el-button data-testid="led-unbilled" plain size="small" type="danger" @click="showUnbilled">只看已做未记账
              {{ stats.unbilled }}
            </el-button>
          </div>
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-input v-model="ledQuery.keyword" clearable data-testid="led-keyword" placeholder="疗程单号/患者/项目"
                      style="width: 220px" @keyup.enter="ledQuery.pageNum = 1; loadLedger()"/>
            <el-select v-model="ledQuery.execStatus" :fit-input-width="false" clearable placeholder="执行状态"
                       style="width: 130px">
              <el-option v-for="d in execDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-date-picker v-model="ledQuery.startDate" placeholder="排期起" style="width: 140px" type="date"
                            value-format="YYYY-MM-DD"/>
            <el-date-picker v-model="ledQuery.endDate" placeholder="排期止" style="width: 140px" type="date"
                            value-format="YYYY-MM-DD"/>
            <el-button :icon="Search" type="primary" @click="ledQuery.pageNum = 1; loadLedger()">查询</el-button>
            <el-button :icon="Refresh" @click="resetLedger">重置</el-button>
          </div>
          <el-table v-loading="ledLoading" :data="ledRows" data-testid="led-table" size="small">
            <el-table-column label="流水号" prop="recordNo" width="200"/>
            <el-table-column label="患者" min-width="110">
              <template #default="{ row }">{{ row.patientName }}<span class="text-gray-400 text-xs"> / {{
                  row.patientNo
                }}</span></template>
            </el-table-column>
            <el-table-column label="项目" min-width="140" prop="itemName" show-overflow-tooltip/>
            <el-table-column align="center" label="次数" width="70">
              <template #default="{ row }">第{{ row.execSeq }}次</template>
            </el-table-column>
            <el-table-column label="排期" prop="planDate" width="105"/>
            <el-table-column label="执行" width="90">
              <template #default="{ row }">
                <el-tag :type="execTag(row.execStatus) as any" size="small">{{ row.execStatusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="计费" width="100">
              <template #default="{ row }">
                <el-tooltip :content="row.chargeFailReason" :disabled="!row.chargeFailReason" placement="top">
                  <el-tag :type="chargeTag(row.chargeStatus) as any" data-testid="led-charge-status" size="small">
                    {{ row.chargeStatusText }}
                  </el-tag>
                </el-tooltip>
              </template>
            </el-table-column>
            <el-table-column align="right" label="金额/收费单" width="180">
              <template #default="{ row }">
                <span data-testid="led-amount">{{ money(row.chargeAmount) }}</span>
                <span class="text-gray-400 text-xs"> {{ row.chargeNo || '' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="执行护士/时间" width="170">
              <template #default="{ row }">{{ row.executorName || '—' }}<span
                  class="text-gray-400 text-xs"> {{ fmtTime(row.executeTime) }}</span></template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="110">
              <template #default="{ row }">
                <el-button v-if="row.canExecute" v-perm="'opd:treatmentStation:edit'" link size="small" type="primary"
                           @click="openExecute(row)">打卡
                </el-button>
                <el-button v-if="row.canRetryCharge" v-perm="'opd:treatmentStation:edit'" link size="small"
                           type="danger" @click="doRetryCharge(row)">补记
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-3 flex justify-end">
            <el-pagination v-model:current-page="ledQuery.pageNum" v-model:page-size="ledQuery.pageSize" :total="ledTotal"
                           background layout="total, prev, pager, next"
                           @current-change="loadLedger"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 打卡确认 -->
    <el-dialog v-model="execDlg.visible"
               :title="`打卡：${execDlg.target?.patientName || ''} · ${execDlg.target?.itemName || ''} 第 ${execDlg.target?.execSeq || ''} 次`"
               width="560px">
      <div class="text-sm text-gray-600 mb-3">
        排期 {{ execDlg.target?.planDate }} · 单价 {{ money(execDlg.target?.price) }}，确认后按次计入本次就诊的治疗费单。
      </div>
      <el-form label-width="88px">
        <el-form-item label="患者反应">
          <el-radio-group v-model="execDlg.form.recordStatus" data-testid="exec-record-status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="治疗记录">
          <el-input v-model="execDlg.form.result" :rows="2" data-testid="exec-result" maxlength="500"
                    placeholder="留空则记「第 N 次治疗完成，过程顺利」" type="textarea"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="execDlg.form.remark" maxlength="200" placeholder="选填"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="execDlg.visible = false">取消</el-button>
        <el-button v-perm="'opd:treatmentStation:edit'" :loading="execDlg.saving" data-testid="exec-submit"
                   type="primary" @click="doExecute">确认打卡
        </el-button>
      </template>
    </el-dialog>

    <!-- 单条改期 -->
    <el-dialog v-model="reschDlg.visible"
               :title="`改期：第 ${reschDlg.target?.execSeq || ''} 次（${reschDlg.target?.itemName || ''}）`"
               width="480px">
      <el-form label-width="88px">
        <el-form-item label="改到">
          <span data-testid="resch-date" style="display: contents">
            <el-date-picker v-model="reschDlg.form.planDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
          </span>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="reschDlg.form.reason" :rows="2" data-testid="resch-reason" maxlength="200"
                    placeholder="如：患者外出，顺延一天" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reschDlg.visible = false">取消</el-button>
        <el-button v-perm="'opd:treatmentStation:edit'" :loading="reschDlg.saving" data-testid="resch-submit"
                   type="primary" @click="doReschedule">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 疗程重排（未打任何一次卡时） -->
    <el-dialog v-model="reschApplyDlg.visible" :title="`重排疗程：${reschApplyDlg.target?.applyNo || ''}`" width="480px">
      <div class="text-xs text-gray-500 mb-3">
        还没有打过任何一次卡，因此可以整体改次数/间隔/开始日；旧排期会被重建。已开始或已产生计费痕迹的疗程只能逐次改期。
      </div>
      <el-form label-width="88px">
        <el-form-item label="总次数">
          <el-input-number v-model="reschApplyDlg.form.totalTimes" :max="60" :min="1" controls-position="right"/>
        </el-form-item>
        <el-form-item label="间隔天数">
          <el-input-number v-model="reschApplyDlg.form.intervalDays" :max="30" :min="1" controls-position="right"/>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="reschApplyDlg.form.startDate" style="width: 100%" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reschApplyDlg.form.remark" maxlength="200"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reschApplyDlg.visible = false">取消</el-button>
        <el-button v-perm="'opd:treatmentStation:edit'" :loading="reschApplyDlg.saving" type="primary"
                   @click="doApplyReschedule">保存并重排
        </el-button>
      </template>
    </el-dialog>

    <!-- 疗程详情 -->
    <el-dialog v-model="detailDlg.visible" title="疗程详情" width="900px">
      <div v-loading="detailDlg.loading">
        <template v-if="detailDlg.data?.apply">
          <div class="grid grid-cols-4 gap-2 text-sm mb-3">
            <div>疗程单号：<b>{{ detailDlg.data.apply.applyNo }}</b></div>
            <div>患者：{{ detailDlg.data.apply.patientName }} / {{ detailDlg.data.apply.patientNo }}</div>
            <div>就诊：{{ detailDlg.data.apply.registNo }}</div>
            <div>状态：{{ detailDlg.data.apply.applyStatusText }}（{{ detailDlg.data.apply.progressText }}）</div>
            <div>项目：{{ detailDlg.data.apply.itemName }}（{{ detailDlg.data.apply.itemTypeText }}）</div>
            <div>开单：{{ detailDlg.data.apply.deptName || '未指定' }} {{ detailDlg.data.apply.doctorName }}</div>
            <div>执行科室：{{ detailDlg.data.apply.execDeptName || '未指定' }}</div>
            <div>单价/计划：{{ money(detailDlg.data.apply.price) }} / {{ money(detailDlg.data.apply.planAmount) }}</div>
          </div>
          <el-table :data="detailDlg.data.execList" data-testid="detail-exec-table" max-height="420" size="small">
            <el-table-column label="流水号" prop="recordNo" width="190"/>
            <el-table-column label="排期" prop="planDate" width="105"/>
            <el-table-column label="执行" width="90">
              <template #default="{ row }">
                <el-tag :type="execTag(row.execStatus) as any" size="small">{{ row.execStatusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="计费" width="100">
              <template #default="{ row }">
                <el-tag :type="chargeTag(row.chargeStatus) as any" size="small">{{ row.chargeStatusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column align="right" label="金额" width="80">
              <template #default="{ row }">{{ money(row.chargeAmount) }}</template>
            </el-table-column>
            <el-table-column label="执行人/时间" width="170">
              <template #default="{ row }">{{ row.executorName || '—' }} {{ fmtTime(row.executeTime) }}</template>
            </el-table-column>
            <el-table-column label="治疗记录" min-width="200" prop="result" show-overflow-tooltip/>
          </el-table>
        </template>
      </div>
      <template #footer>
        <el-button @click="detailDlg.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 门诊治疗站（G19，菜单 206 / 路由 /treatment-station）
 *
 * 三个 tab：
 *  A 今日治疗台 —— 按排期日看"今天该做哪几次"，逐次打卡（打卡即按次计费）；
 *  B 治疗开单    —— 选患者→选这次就诊（挂号单）→选项目→定总次数/间隔/开始日，一次把排期落库；
 *  C 执行台账    —— 全量按次流水 + 计费状态分布，逾期/漏记账在这张表里被捞出来补。
 *
 * ⚠ 页面不判"能不能打卡"、不算进度：canExecute / overdue / progressText / canRetryCharge
 *   全在后端（TreatmentService.blockReason 单点），前端只渲染 + 把原因原话透传给用户。
 * ⚠ 日期筛选只管"看"：打卡写的是被点那一行的 recordId，切换日期不会把执行挪到另一天。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import PatientSelect from '@/components/his/PatientSelect.vue';
import request from '@/api/request';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {
  deleteTreatmentApply,
  getTreatmentApplyDetail,
  getTreatmentApplyListPage,
  getTreatmentExecListPage,
  getTreatmentExecStatusCount,
  getTreatmentItemSelectList,
  getTreatmentStats,
  treatmentApplyCancel,
  treatmentApplyUpsert,
  treatmentExecExecute,
  treatmentExecReschedule,
  treatmentExecRetryCharge,
} from '@/api/treatment';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';

const activeTab = ref('board');
const today = (() => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
})();
const EXEC = {PENDING: 0, DONE: 1, CANCELLED: 2};
const CHARGE = {NONE: 0, DONE: 1, FAIL: 2, NA: 3};
const execTag = (s) => (Number(s) === EXEC.DONE ? 'success' : Number(s) === EXEC.CANCELLED ? 'info' : 'warning');
const chargeTag = (s) => (Number(s) === CHARGE.DONE ? 'success'
    : Number(s) === CHARGE.FAIL ? 'danger' : Number(s) === CHARGE.NA ? 'info' : '');
const money = (v) => (v === null || v === undefined ? '—' : `¥${Number(v).toFixed(2)}`);
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—');
// ---------------- 字典（只给筛选下拉；列表文案取后端 *Text） ----------------
const execDict = ref([]);
const chargeDict = ref([]);
const itemTypeDict = ref([]);
const applyStatusDict = ref([]);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.TREATMENT_EXEC_STATUS},${DICT_TYPE.TREATMENT_CHARGE_STATUS},`
        + `${DICT_TYPE.TREATMENT_ITEM_TYPE},${DICT_TYPE.TREATMENT_APPLY_STATUS}`);
    execDict.value = res?.data?.[DICT_TYPE.TREATMENT_EXEC_STATUS] || [];
    chargeDict.value = res?.data?.[DICT_TYPE.TREATMENT_CHARGE_STATUS] || [];
    itemTypeDict.value = res?.data?.[DICT_TYPE.TREATMENT_ITEM_TYPE] || [];
    applyStatusDict.value = res?.data?.[DICT_TYPE.TREATMENT_APPLY_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 统计 ----------------
const stats = reactive({
  runningApplies: 0, todayPlan: 0, todayDone: 0, todayPending: 0,
  overduePending: 0, unbilled: 0, chargeFail: 0, todayAmount: 0,
});
const loadStats = async () => {
  try {
    const res = await getTreatmentStats();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error(e);
  }
};
// ================= A 今日治疗台 =================
const boardQuery = reactive({
  planDate: today, execStatus: null, overdueOnly: false, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const boardLoading = ref(false);
const boardRows = ref([]);
const boardTotal = ref(0);
const boardCounts = ref([]);
const loadBoard = async () => {
  boardLoading.value = true;
  try {
    const payload = {
      planDate: boardQuery.overdueOnly ? undefined : (boardQuery.planDate || undefined),
      execStatus: boardQuery.execStatus ?? undefined,
      overdueOnly: boardQuery.overdueOnly || undefined,
      pageNum: boardQuery.pageNum, pageSize: boardQuery.pageSize,
    };
    const [pageRes, countRes] = await Promise.all([
      getTreatmentExecListPage(payload), getTreatmentExecStatusCount(payload),
    ]);
    if (pageRes.code === 200) {
      boardRows.value = pageRes.data?.records || [];
      boardTotal.value = Number(pageRes.data?.total || 0);
    } else
      ElMessage.error(pageRes.message || '查询失败');
    if (countRes.code === 200)
      boardCounts.value = countRes.data || [];
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    boardLoading.value = false;
  }
};
const setBoardExec = (s) => {
  boardQuery.execStatus = s;
  boardQuery.pageNum = 1;
  loadBoard();
};
const toggleOverdue = () => {
  boardQuery.overdueOnly = !boardQuery.overdueOnly;
  boardQuery.pageNum = 1;
  loadBoard();
};
const countOf = (key) => {
  const hit = boardCounts.value.find((c) => c.key === key);
  return hit ? Number(hit.count) : 0;
};
const execCounts = computed(() => [0, 1, 2].map((s) => ({
  status: s, label: dictLabelText(execDict.value, s), count: countOf(`exec-${s}`),
})));
// ---------------- 打卡 ----------------
const execDlg = reactive({
  visible: false, target: null, saving: false,
  form: {recordStatus: 1, result: '', remark: ''},
});
const openExecute = (row) => {
  if (!row.canExecute) {
    ElMessage.warning(row.cannotExecuteReason || '这一行现在不能打卡');
    return;
  }
  execDlg.target = row;
  execDlg.form = {recordStatus: 1, result: '', remark: ''};
  execDlg.visible = true;
};
const doExecute = async () => {
  execDlg.saving = true;
  try {
    const res = await treatmentExecExecute({
      recordId: execDlg.target.recordId,
      recordStatus: execDlg.form.recordStatus,
      result: execDlg.form.result.trim() || undefined,
      remark: execDlg.form.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已打卡');
      execDlg.visible = false;
      await refreshAll();
    } else
      ElMessage.error(res.message || '打卡失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('打卡失败');
  } finally {
    execDlg.saving = false;
  }
};
// ---------------- 改期（单条待执行次） ----------------
const reschDlg = reactive({visible: false, target: null, saving: false, form: {planDate: '', reason: ''}});
const openReschedule = (row) => {
  reschDlg.target = row;
  reschDlg.form = {planDate: row.planDate || today, reason: ''};
  reschDlg.visible = true;
};
const doReschedule = async () => {
  if (!reschDlg.form.planDate) {
    ElMessage.warning('请选择改到哪天');
    return;
  }
  if (!reschDlg.form.reason.trim()) {
    ElMessage.warning('改期原因必填');
    return;
  }
  reschDlg.saving = true;
  try {
    const res = await treatmentExecReschedule({
      recordId: reschDlg.target.recordId, planDate: reschDlg.form.planDate, reason: reschDlg.form.reason.trim(),
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已改期');
      reschDlg.visible = false;
      await refreshAll();
    } else
      ElMessage.error(res.message || '改期失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('改期失败');
  } finally {
    reschDlg.saving = false;
  }
};
const doRetryCharge = async (row) => {
  try {
    await ElMessageBox.confirm(`确认补记第 ${row.execSeq} 次的治疗费（${money(row.price)}）？只有「已执行但没记上账」的行会补，不会重复记账。`, '计费补记', {type: 'warning'});
  } catch {
    return;
  }
  const res = await treatmentExecRetryCharge({recordId: row.recordId});
  if (res.code === 200) {
    ElMessage.success(res.message || '已补记');
    await refreshAll();
  } else
    ElMessage.error(res.message || '补记失败');
};
// ================= B 治疗开单 =================
const openForm = reactive({
  patientId: null, patientName: '', registId: null, registOptions: [],
  treatmentItemId: null, itemOptions: [], itemLoading: false,
  totalTimes: 5, intervalDays: 1, startDate: today, remark: '', saving: false,
});
const priceOf = (itemId) => {
  const hit = openForm.itemOptions.find((i) => String(i.itemId) === String(itemId));
  return hit ? Number(hit.price || 0) : 0;
};
const openPlanAmount = computed(() => (priceOf(openForm.treatmentItemId) * Number(openForm.totalTimes || 0)).toFixed(2));
const loadRegists = async (patientId) => {
  openForm.registOptions = [];
  openForm.registId = null;
  if (!patientId)
    return;
  try {
    const res = await request.get('/appoint/listPage', {params: {patientId, pageNum: 1, pageSize: 20}});
    if (res.code === 200) {
      // 已退号（regist_status=5）的就诊不能挂治疗，后端也会拒，这里直接不给选
      openForm.registOptions = (res.data?.records || []).filter((r) => Number(r.registStatus) !== 5);
      if (!openForm.registOptions.length)
        ElMessage.warning('该患者没有可开治疗的就诊记录（已退号的已过滤）');
    } else
      ElMessage.error(res.message || '加载就诊记录失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('加载就诊记录失败');
  }
};
const onPatientSelect = async (p) => {
  openForm.patientName = p?.patientName || '';
  await loadRegists(p?.id);
};
const searchItems = async () => {
  openForm.itemLoading = true;
  try {
    const res = await getTreatmentItemSelectList({keyword: undefined, limit: 200});
    if (res.code === 200)
      openForm.itemOptions = res.data || [];
  } catch (e) {
    console.error(e);
  } finally {
    openForm.itemLoading = false;
  }
};
const submitApply = async () => {
  if (!openForm.registId) {
    ElMessage.warning('请先选择患者与本次就诊');
    return;
  }
  if (!openForm.treatmentItemId) {
    ElMessage.warning('请选择治疗项目');
    return;
  }
  if (!openForm.startDate) {
    ElMessage.warning('请选择疗程开始日期');
    return;
  }
  openForm.saving = true;
  try {
    const res = await treatmentApplyUpsert({
      registId: openForm.registId,
      treatmentItemId: openForm.treatmentItemId,
      totalTimes: Number(openForm.totalTimes),
      intervalDays: Number(openForm.intervalDays),
      startDate: openForm.startDate,
      remark: openForm.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '开单成功');
      openForm.remark = '';
      // 治疗台也要一起刷：开单一次生成全部排期流水，不刷的话今天那次在治疗台上看不见
      await refreshAll();
    } else
      ElMessage.error(res.message || '开单失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('开单失败');
  } finally {
    openForm.saving = false;
  }
};
// ---------------- 疗程台账 ----------------
const applyQuery = reactive({
  keyword: '', applyStatus: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const applyLoading = ref(false);
const applyRows = ref([]);
const applyTotal = ref(0);
const loadApplies = async () => {
  applyLoading.value = true;
  try {
    const res = await getTreatmentApplyListPage({
      keyword: applyQuery.keyword.trim() || undefined,
      applyStatus: applyQuery.applyStatus ?? undefined,
      pageNum: applyQuery.pageNum, pageSize: applyQuery.pageSize,
    });
    if (res.code === 200) {
      applyRows.value = res.data?.records || [];
      applyTotal.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    applyLoading.value = false;
  }
};
// 重排疗程（一次卡都没打过时才允许整体改次数/间隔/开始日）
const reschApplyDlg = reactive({
  visible: false,
  target: null,
  saving: false,
  form: {totalTimes: 1, intervalDays: 1, startDate: '', remark: ''}
});
const openApplyReschedule = (row) => {
  reschApplyDlg.target = row;
  reschApplyDlg.form = {
    totalTimes: Number(row.totalTimes || 1), intervalDays: Number(row.intervalDays || 1),
    startDate: row.startDate || today, remark: row.remark || '',
  };
  reschApplyDlg.visible = true;
};
const doApplyReschedule = async () => {
  reschApplyDlg.saving = true;
  try {
    const res = await treatmentApplyUpsert({
      applyId: reschApplyDlg.target.applyId,
      registId: reschApplyDlg.target.registId,
      treatmentItemId: reschApplyDlg.target.treatmentItemId,
      totalTimes: Number(reschApplyDlg.form.totalTimes),
      intervalDays: Number(reschApplyDlg.form.intervalDays),
      startDate: reschApplyDlg.form.startDate,
      remark: reschApplyDlg.form.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '疗程已重排');
      reschApplyDlg.visible = false;
      await refreshAll();
    } else
      ElMessage.error(res.message || '重排失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('重排失败');
  } finally {
    reschApplyDlg.saving = false;
  }
};
const doApplyCancel = async (row) => {
  let reason = '';
  try {
    const r = await ElMessageBox.prompt(`取消疗程 ${row.applyNo}（${row.itemName}，已做 ${row.doneTimes}/${row.totalTimes} 次）。未执行的次数一并取消；已计费的治疗费不退，需要退费请到收费窗口冲红。`, '取消疗程', {
      inputPlaceholder: '取消原因（必填）',
      inputValidator: (v) => (v && v.trim() ? true : '取消原因必填')
    });
    reason = r?.value || '';
  } catch {
    return;
  }
  const res = await treatmentApplyCancel({applyId: row.applyId, reason: reason.trim()});
  if (res.code === 200) {
    ElMessage.success(res.message || '已取消');
    await refreshAll();
  } else
    ElMessage.error(res.message || '取消失败');
};
const doApplyDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`删除疗程 ${row.applyNo}？只有一次都没执行过的疗程能删，排期流水会一并清掉。`, '删除', {type: 'warning'});
  } catch {
    return;
  }
  const res = await deleteTreatmentApply(row.applyId);
  if (res.code === 200) {
    ElMessage.success(res.message || '已删除');
    await refreshAll();
  } else
    ElMessage.error(res.message || '删除失败');
};
// ---------------- 疗程详情 ----------------
const detailDlg = reactive({visible: false, loading: false, data: null});
const openDetail = async (row) => {
  detailDlg.visible = true;
  detailDlg.loading = true;
  detailDlg.data = null;
  try {
    const res = await getTreatmentApplyDetail(row.applyId);
    if (res.code === 200)
      detailDlg.data = res.data;
    else
      ElMessage.error(res.message || '加载详情失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('加载详情失败');
  } finally {
    detailDlg.loading = false;
  }
};
// ================= C 执行与计费台账 =================
const ledQuery = reactive({
  keyword: '', execStatus: null, chargeStatus: null,
  unbilledOnly: false, startDate: '', endDate: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const ledLoading = ref(false);
const ledRows = ref([]);
const ledTotal = ref(0);
const ledCounts = ref([]);
const loadLedger = async () => {
  ledLoading.value = true;
  try {
    const payload = {
      keyword: ledQuery.keyword.trim() || undefined,
      execStatus: ledQuery.execStatus ?? undefined,
      chargeStatus: ledQuery.chargeStatus ?? undefined,
      unbilledOnly: ledQuery.unbilledOnly || undefined,
      startDate: ledQuery.startDate || undefined,
      endDate: ledQuery.endDate || undefined,
      pageNum: ledQuery.pageNum, pageSize: ledQuery.pageSize,
    };
    const [pageRes, countRes] = await Promise.all([
      getTreatmentExecListPage(payload), getTreatmentExecStatusCount(payload),
    ]);
    if (pageRes.code === 200) {
      ledRows.value = pageRes.data?.records || [];
      ledTotal.value = Number(pageRes.data?.total || 0);
    } else
      ElMessage.error(pageRes.message || '查询失败');
    if (countRes.code === 200)
      ledCounts.value = countRes.data || [];
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    ledLoading.value = false;
  }
};
const ledCountOf = (key) => {
  const hit = ledCounts.value.find((c) => c.key === key);
  return hit ? Number(hit.count) : 0;
};
const resetLedger = () => {
  Object.assign(ledQuery, {
    keyword: '', execStatus: null, chargeStatus: null, unbilledOnly: false, startDate: '', endDate: '', pageNum: 1,
  });
  loadLedger();
};
const showUnbilled = () => {
  ledQuery.unbilledOnly = true;
  ledQuery.execStatus = null;
  ledQuery.chargeStatus = null;
  ledQuery.pageNum = 1;
  loadLedger();
};
/**
 * 任何一个写动作之后统一刷三张表 + 统计。
 *
 * 只刷当前那张是错的：开单一次生成全部排期流水，今日治疗台却停在旧数据上，
 * 用户开完单在治疗台上找不到今天那一次；取消/删除同理。
 */
const refreshAll = () => Promise.all([loadBoard(), loadApplies(), loadLedger(), loadStats()]);
onMounted(() => {
  loadDicts();
  loadStats();
  searchItems();
  loadBoard();
  loadApplies();
  loadLedger();
});
</script>

<style scoped>
.el-tag {
  font-weight: 400;
}
</style>
