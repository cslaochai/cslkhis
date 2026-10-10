<template>
  <div class="p-4 space-y-4" data-testid="yb-deduct-page">
    <!-- 台账汇总：数字全部来自后端 SQL 聚合 -->
    <el-row :gutter="12">
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-pending" shadow="never">
          <div class="text-xs text-slate-500">待确认</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.pendingConfirmCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-appealing" shadow="never">
          <div class="text-xs text-slate-500">申诉中</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.appealingCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-waitpay" shadow="never">
          <div class="text-xs text-slate-500">维持扣款待缴</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.waitPayCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-paid" shadow="never">
          <div class="text-xs text-slate-500">已缴回</div>
          <div class="text-2xl font-semibold mt-1">{{ summary.paidCount ?? 0 }}</div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-overdue" shadow="never">
          <div class="text-xs text-slate-500">超期未结</div>
          <div :class="(summary.overdueCount ?? 0) > 0 ? 'text-red-600' : ''" class="text-2xl font-semibold mt-1">
            {{ summary.overdueCount ?? 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card class="!rounded-lg" data-testid="deduct-sum-amount" shadow="never">
          <div class="text-xs text-slate-500">未结案金额</div>
          <div class="text-2xl font-semibold mt-1 text-[#1269B5]">{{ money(summary.openAmountSum) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="!rounded-lg" shadow="never">
      <el-tabs v-model="activeTab" data-testid="deduct-tabs">
        <!-- ==================== 扣款通知 ==================== -->
        <el-tab-pane label="扣款通知台账" name="notice">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="noticeQuery.deductStatus" clearable data-testid="deduct-filter-status" placeholder="全部状态"
                       style="width: 150px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_DEDUCT_STATUS)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="noticeQuery.sourceType" clearable placeholder="全部来源" style="width: 160px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_DEDUCT_SOURCE)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="noticeQuery.violationType" clearable placeholder="全部违规类型" style="width: 170px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_VIOLATION_TYPE)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="noticeQuery.inspectionId" clearable data-testid="deduct-filter-inspection" placeholder="不限飞检批次"
                       style="width: 190px">
              <el-option v-for="i in inspectionOptions" :key="i.id" :label="`${i.inspectNo} ${i.fundOrg}`"
                         :value="String(i.id)"/>
            </el-select>
            <el-checkbox v-model="noticeQuery.onlyOverdue" data-testid="deduct-only-overdue">只看超期</el-checkbox>
            <el-input v-model="noticeQuery.keyword" clearable data-testid="deduct-keyword" placeholder="单号/患者/科室/违规描述"
                      style="width: 220px"
                      @keyup.enter="noticePage.pageNum = 1; loadNotices()"/>
            <el-button :icon="Search" data-testid="deduct-search-btn" type="primary"
                       @click="noticePage.pageNum = 1; loadNotices()">查询
            </el-button>
            <el-button :icon="Refresh" @click="resetNoticeQuery">重置</el-button>
            <div class="flex-1"></div>
            <el-button v-perm="'finance:insuranceDeduct:add'" :icon="Plus" data-testid="deduct-create-btn"
                       type="primary" @click="openNoticeCreate">录入扣款通知
            </el-button>
          </div>

          <el-table v-loading="noticeLoading" :data="noticeRows" data-testid="deduct-table" size="default"
                    style="width: 100%" @row-click="openNoticeDetail">
            <el-table-column class-name="font-mono text-xs" label="扣款单号" prop="deductNo" width="150"/>
            <el-table-column label="来源" show-overflow-tooltip width="140">
              <template #default="{row}">{{ dictText(DICT_TYPE.YB_DEDUCT_SOURCE, row.sourceType) }}</template>
            </el-table-column>
            <el-table-column label="患者" prop="patientName" width="90">
              <template #default="{row}">{{ row.patientName || '—' }}</template>
            </el-table-column>
            <el-table-column label="被审科室" prop="deptName" show-overflow-tooltip width="110">
              <template #default="{row}">{{ row.deptName || '—' }}</template>
            </el-table-column>
            <el-table-column label="违规类型" show-overflow-tooltip width="140">
              <template #default="{row}">{{ dictText(DICT_TYPE.YB_VIOLATION_TYPE, row.violationType) }}</template>
            </el-table-column>
            <el-table-column label="违规事实" min-width="220" prop="violationDesc" show-overflow-tooltip/>
            <el-table-column align="right" label="扣款金额" width="110">
              <template #default="{row}">{{ money(row.deductAmount) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{row}">
                <el-tag :data-testid="`deduct-status-${row.deductNo}`" :type="statusTagType(row.deductStatus)"
                        size="small">
                  {{ dictText(DICT_TYPE.YB_DEDUCT_STATUS, row.deductStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="处理期限" width="150">
              <template #default="{row}">
                <div class="text-xs">{{ row.handleDeadline }}</div>
                <el-tag v-if="row.overdue" data-testid="deduct-overdue-tag" size="small" type="danger">超期
                  {{ Math.abs(row.deadlineDays) }} 天
                </el-tag>
                <span v-else-if="row.deadlineDays != null" class="text-xs text-slate-400">剩 {{
                    row.deadlineDays
                  }} 天</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="250">
              <template #default="{row}">
                <el-button v-if="canAppeal(row)" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`deduct-appeal-${row.deductNo}`" plain
                           size="small"
                           type="primary" @click.stop="openAppeal(row)">申诉
                </el-button>
                <el-button v-if="canAppealResult(row)" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`deduct-result-${row.deductNo}`" plain
                           size="small"
                           type="primary" @click.stop="openAppealResult(row)">录结果
                </el-button>
                <el-button v-if="canConfirm(row)" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`deduct-confirm-${row.deductNo}`" plain
                           size="small"
                           type="warning" @click.stop="openConfirm(row)">确认追责
                </el-button>
                <el-button v-if="canPayback(row)" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`deduct-payback-${row.deductNo}`" :icon="Money" plain
                           size="small"
                           type="success" @click.stop="openPayback(row)">缴回
                </el-button>
                <el-button v-if="canEditNotice(row)" v-perm="'finance:insuranceDeduct:add'" :data-testid="`deduct-edit-${row.deductNo}`" :icon="Edit"
                           plain
                           size="small" @click.stop="openNoticeEdit(row)">编辑
                </el-button>
                <el-button v-if="canCancelNotice(row)" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`deduct-cancel-${row.deductNo}`" :icon="Delete" plain
                           size="small"
                           type="danger" @click.stop="cancelDeductRow(row)">作废
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="flex justify-end mt-3">
            <el-pagination v-model:current-page="noticePage.pageNum" v-model:page-size="noticePage.pageSize"
                           :page-sizes="PAGE_SIZES" :total="noticePage.total"
                           data-testid="deduct-pagination"
                           layout="total, sizes, prev, pager, next, jumper" @current-change="loadNotices"
                           @size-change="noticePage.pageNum = 1; loadNotices()"/>
          </div>
        </el-tab-pane>

        <!-- ==================== 飞检批次 ==================== -->
        <el-tab-pane label="飞检/专项审核批次" name="inspection">
          <div class="flex flex-wrap items-center gap-2 mb-3">
            <el-select v-model="inspectQuery.inspectType" clearable placeholder="全部检查类型" style="width: 170px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_INSPECT_TYPE)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="inspectQuery.status" clearable placeholder="全部状态" style="width: 140px">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_INSPECT_STATUS)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-input v-model="inspectQuery.keyword" clearable data-testid="inspect-keyword" placeholder="批次号/医保局/检查组"
                      style="width: 220px"
                      @keyup.enter="inspectPage.pageNum = 1; loadInspections()"/>
            <el-button :icon="Search" data-testid="inspect-search-btn" type="primary"
                       @click="inspectPage.pageNum = 1; loadInspections()">查询
            </el-button>
            <el-button :icon="Refresh" @click="resetInspectQuery">重置</el-button>
            <div class="flex-1"></div>
            <el-button v-perm="'finance:insuranceDeduct:add'" :icon="Plus" data-testid="inspect-create-btn"
                       type="primary" @click="openInspectCreate">新建批次
            </el-button>
          </div>

          <el-table v-loading="inspectLoading" :data="inspectRows" data-testid="inspect-table" size="default"
                    style="width: 100%">
            <el-table-column class-name="font-mono text-xs" label="批次号" prop="inspectNo" width="150"/>
            <el-table-column label="检查类型" width="130">
              <template #default="{row}">{{ dictText(DICT_TYPE.YB_INSPECT_TYPE, row.inspectType) }}</template>
            </el-table-column>
            <el-table-column label="统筹区/医保局" min-width="150" prop="fundOrg" show-overflow-tooltip/>
            <el-table-column label="审核期间" width="200">
              <template #default="{row}">{{ row.inspectStartDate }} ~ {{ row.inspectEndDate }}</template>
            </el-table-column>
            <el-table-column label="进驻/通知日期" prop="inspectDate" width="120"/>
            <el-table-column label="检查组" min-width="150" prop="inspectTeam" show-overflow-tooltip>
              <template #default="{row}">{{ row.inspectTeam || '—' }}</template>
            </el-table-column>
            <el-table-column label="院内接待" prop="ourReceiver" width="100">
              <template #default="{row}">{{ row.ourReceiver || '—' }}</template>
            </el-table-column>
            <el-table-column align="right" label="名下扣款" width="150">
              <template #default="{row}">
                <span class="text-xs">{{ row.deductCount }} 张 / </span>
                <span class="font-medium">{{ money(row.deductAmountSum) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{row}">
                <el-tag :type="inspectTagType(row.status)" size="small">
                  {{ dictText(DICT_TYPE.YB_INSPECT_STATUS, row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="结论" min-width="200" show-overflow-tooltip>
              <template #default="{row}">{{ row.conclusion || '—' }}</template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="280">
              <template #default="{row}">
                <el-button :data-testid="`inspect-drill-${row.inspectNo}`" :icon="View" plain size="small"
                           @click="drillNotices(row)">看扣款单
                </el-button>
                <el-button v-if="row.status === INSPECT_RUNNING" v-perm="'finance:insuranceDeduct:add'" :data-testid="`inspect-edit-${row.inspectNo}`" plain
                           size="small"
                           type="primary" @click="openInspectEdit(row)">编辑
                </el-button>
                <el-button v-if="row.status === INSPECT_RUNNING" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`inspect-conclude-${row.inspectNo}`" plain
                           size="small"
                           type="success" @click="openConclude(row)">结项
                </el-button>
                <el-button v-if="row.status === INSPECT_RUNNING" v-perm="'finance:insuranceDeduct:edit'" :data-testid="`inspect-cancel-${row.inspectNo}`" :icon="Delete" plain
                           size="small"
                           type="danger" @click="cancelInspectRow(row)">作废
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="flex justify-end mt-3">
            <el-pagination v-model:current-page="inspectPage.pageNum" v-model:page-size="inspectPage.pageSize"
                           :page-sizes="PAGE_SIZES" :total="inspectPage.total"
                           data-testid="inspect-pagination"
                           layout="total, sizes, prev, pager, next, jumper" @current-change="loadInspections"
                           @size-change="inspectPage.pageNum = 1; loadInspections()"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ==================== 通知单 新建/编辑 ==================== -->
    <el-dialog v-model="noticeFormVisible"
               :title="noticeForm.id ? `修改扣款通知 ${noticeForm.deductNo}` : '录入扣款通知'" data-testid="deduct-form-dialog"
               width="820px">
      <el-form :model="noticeForm" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="扣款来源" required>
              <el-select v-model="noticeForm.sourceType" data-testid="deduct-form-source" style="width: 100%">
                <el-option v-for="d in dictOptions(DICT_TYPE.YB_DEDUCT_SOURCE)" :key="d.dictValue" :label="d.dictLabel"
                           :value="Number(d.dictValue)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="飞检批次">
              <el-select v-model="noticeForm.inspectionId" :placeholder="noticeForm.sourceType === 1 ? '来源为飞检现场时必须关联' : '可不关联'" clearable data-testid="deduct-form-inspection"
                         filterable
                         style="width: 100%">
                <el-option v-for="i in inspectionOptions" :key="i.id" :label="`${i.inspectNo} ${i.fundOrg}`"
                           :value="String(i.id)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="患者">
              <PatientSelect v-model="noticeForm.patientId" placeholder="按扣款通知上的患者搜（批次性问题可留空）"
                             @select="onNoticePatient"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="就诊类型">
              <el-select v-model="noticeForm.encounterType" clearable style="width: 100%">
                <el-option :value="1" label="门诊"/>
                <el-option :value="2" label="住院"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="被审科室">
              <el-select v-model="noticeForm.deptId" clearable data-testid="deduct-form-dept" filterable style="width: 100%"
                         @change="onNoticeDept">
                <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="责任医师">
              <el-input v-model="noticeForm.doctorName" placeholder="通知上写明的医师姓名"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="违规类型" required>
              <el-select v-model="noticeForm.violationType" data-testid="deduct-form-violation" style="width: 100%">
                <el-option v-for="d in dictOptions(DICT_TYPE.YB_VIOLATION_TYPE)" :key="d.dictValue" :label="d.dictLabel"
                           :value="Number(d.dictValue)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="扣款金额" required>
              <el-input-number v-model="noticeForm.deductAmount" :controls="false" :min="0.01" :precision="2"
                               data-testid="deduct-form-amount" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item data-testid="deduct-form-notice-date" label="通知日期" required>
              <el-date-picker v-model="noticeForm.noticeDate" style="width: 100%" type="date"
                              value-format="YYYY-MM-DD"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item data-testid="deduct-form-deadline" label="处理期限" required>
              <el-date-picker v-model="noticeForm.handleDeadline" style="width: 100%" type="date"
                              value-format="YYYY-MM-DD"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="违规事实描述" required>
          <el-input v-model="noticeForm.violationDesc" :rows="3" data-testid="deduct-form-desc"
                    placeholder="飞检问的就是这一句：哪天、哪个项目、怎么个违规法，照通知单原文写"
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="noticeForm.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="noticeFormVisible = false">取消</el-button>
        <el-button :loading="noticeSubmitting" data-testid="deduct-form-save" type="primary" @click="saveNotice">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 通知单详情（只读 + 留痕） ==================== -->
    <el-dialog v-model="noticeDetailVisible" :title="`扣款通知 ${noticeDetail?.deductNo || ''}`" data-testid="deduct-detail-dialog"
               width="880px">
      <el-form :model="noticeDetail" disabled label-width="120px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="状态">
              <el-tag :type="statusTagType(noticeDetail?.deductStatus)" size="small">
                {{ dictText(DICT_TYPE.YB_DEDUCT_STATUS, noticeDetail?.deductStatus) }}
              </el-tag>
              <el-tag v-if="noticeDetail?.overdue" class="ml-2" size="small" type="danger">已超期</el-tag>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="来源">{{
                dictText(DICT_TYPE.YB_DEDUCT_SOURCE, noticeDetail?.sourceType)
              }}
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="飞检批次">{{ noticeDetail?.inspectionNo || '—' }}</el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="患者">{{ noticeDetail?.patientName || '—' }}</el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="被审科室">{{ noticeDetail?.deptName || '—' }}</el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="责任医师">{{ noticeDetail?.doctorName || '—' }}</el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="违规类型">{{
                dictText(DICT_TYPE.YB_VIOLATION_TYPE, noticeDetail?.violationType)
              }}
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="扣款金额">{{ money(noticeDetail?.deductAmount) }}</el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="处理期限">{{ noticeDetail?.handleDeadline }}</el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="违规事实">{{ noticeDetail?.violationDesc }}</el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.appealReason" :span="24">
            <el-form-item label="申诉理由">{{ noticeDetail.appealReason }}</el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.appealMaterial" :span="24">
            <el-form-item label="申诉材料">{{ noticeDetail.appealMaterial }}</el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.appealResult" :span="12">
            <el-form-item label="申诉结果">{{
                dictText(DICT_TYPE.YB_APPEAL_RESULT, noticeDetail.appealResult)
              }}
            </el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.liableDeptName" :span="12">
            <el-form-item label="责任科室">{{ noticeDetail.liableDeptName }}</el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.liableEmpName" :span="12">
            <el-form-item label="责任人">{{ noticeDetail.liableEmpName }}</el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.lossBearType" :span="12">
            <el-form-item label="承担方式">{{
                dictText(DICT_TYPE.YB_LOSS_BEAR, noticeDetail.lossBearType)
              }}
            </el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.bearDeptAmount != null" :span="8">
            <el-form-item label="科室承担">{{ money(noticeDetail.bearDeptAmount) }}</el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.bearEmpAmount != null" :span="8">
            <el-form-item label="个人承担">{{ money(noticeDetail.bearEmpAmount) }}</el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.paybackVoucher" :span="8">
            <el-form-item label="缴回凭证">{{ noticeDetail.paybackVoucher }}</el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.paybackDate" :span="8">
            <el-form-item label="缴回日期">{{ noticeDetail.paybackDate }}</el-form-item>
          </el-col>
          <el-col v-if="noticeDetail?.cancelReason" :span="24">
            <el-form-item label="作废原因">{{ noticeDetail.cancelReason }}</el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <div class="font-medium mb-2 text-sm">处理留痕</div>
      <el-timeline data-testid="deduct-log-timeline">
        <el-timeline-item v-for="log in noticeDetail?.logs || []" :key="log.id" :timestamp="log.operateTime"
                          placement="top">
          <div class="text-sm">
            <el-tag size="small" type="info">{{ dictText(DICT_TYPE.YB_DEDUCT_ACTION, log.action) }}</el-tag>
            <span class="ml-2">{{ log.detail }}</span>
            <span v-if="log.amount != null" class="ml-2 text-slate-500">{{ money(log.amount) }}</span>
            <span class="ml-2 text-xs text-slate-400">{{ log.operator }}</span>
          </div>
        </el-timeline-item>
      </el-timeline>
      <template #footer>
        <el-button @click="noticeDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 发起申诉 ==================== -->
    <el-dialog v-model="appealVisible" :title="`发起申诉 ${appealForm.deductNo}`" data-testid="deduct-appeal-dialog"
               width="640px">
      <el-form :model="appealForm" label-width="100px">
        <el-form-item label="申诉理由" required>
          <el-input v-model="appealForm.appealReason" :rows="3" data-testid="deduct-appeal-reason"
                    placeholder="为什么认为这笔扣款不该扣，写事实不写情绪" type="textarea"/>
        </el-form-item>
        <el-form-item label="申诉材料">
          <el-input v-model="appealForm.appealMaterial" :rows="2" placeholder="附了哪些单据/截图/签字件"
                    type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appealVisible = false">取消</el-button>
        <el-button :loading="appealSubmitting" data-testid="deduct-appeal-submit" type="primary" @click="submitAppeal">
          提交申诉
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 录入申诉结果 ==================== -->
    <el-dialog v-model="resultVisible" :title="`录入申诉结果 ${resultForm.deductNo}`" data-testid="deduct-result-dialog"
               width="600px">
      <el-form :model="resultForm" label-width="100px">
        <el-form-item label="申诉结果" required>
          <el-radio-group v-model="resultForm.appealResult" data-testid="deduct-result-radio">
            <el-radio :value="1">申诉成功（扣款撤销）</el-radio>
            <el-radio :value="2">申诉驳回（维持扣款待缴）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="结果说明">
          <el-input v-model="resultForm.appealResultRemark" :rows="3" placeholder="照医保局回复原文写" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultVisible = false">取消</el-button>
        <el-button :loading="resultSubmitting" data-testid="deduct-result-submit" type="primary"
                   @click="submitAppealResult">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 确认扣款并追责 ==================== -->
    <el-dialog v-model="confirmVisible" :title="`确认扣款并追责 ${confirmForm.deductNo}`" data-testid="deduct-confirm-dialog"
               width="680px">
      <el-alert :closable="false" :title="`扣款金额 ${money(confirmForm.deductAmount)}；共担时科室+个人分摊之和不得超过它，差额视为院方承担`" class="mb-3"
                type="info"/>
      <el-form :model="confirmForm" label-width="110px">
        <el-form-item label="责任科室" required>
          <el-select v-model="confirmForm.liableDeptId" data-testid="deduct-confirm-dept" filterable style="width: 100%"
                     @change="onConfirmDept">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="String(d.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="责任人">
          <el-input v-model="confirmForm.liableEmpName" placeholder="个人承担或共担时要写到人"/>
        </el-form-item>
        <el-form-item label="损失承担方式" required>
          <el-select v-model="confirmForm.lossBearType" data-testid="deduct-confirm-bear" style="width: 100%">
            <el-option v-for="d in dictOptions(DICT_TYPE.YB_LOSS_BEAR)" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科室承担">
              <el-input-number v-model="confirmForm.bearDeptAmount" :controls="false" :min="0" :precision="2"
                               data-testid="deduct-confirm-dept-amount" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="个人承担">
              <el-input-number v-model="confirmForm.bearEmpAmount" :controls="false" :min="0" :precision="2"
                               data-testid="deduct-confirm-emp-amount" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <div v-if="confirmForm.lossBearType === 4" class="text-xs text-slate-500 ml-[110px]">
          已分摊 {{ money(bearSum) }} / {{ money(confirmForm.deductAmount) }}
        </div>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button :loading="confirmSubmitting" data-testid="deduct-confirm-submit" type="primary"
                   @click="submitConfirm">确认扣款
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 录入缴回 ==================== -->
    <el-dialog v-model="paybackVisible" :title="`录入缴回 ${paybackForm.deductNo}`" data-testid="deduct-payback-dialog"
               width="600px">
      <el-alert :closable="false" :title="`缴回金额须等于扣款金额 ${money(paybackForm.deductAmount)}，差额走院内财务承担流程`" class="mb-3"
                type="warning"/>
      <el-form :model="paybackForm" label-width="110px">
        <el-form-item label="缴回金额" required>
          <el-input-number v-model="paybackForm.paidAmount" :controls="false" :min="0.01" :precision="2"
                           data-testid="deduct-payback-amount" style="width: 100%"/>
        </el-form-item>
        <el-form-item data-testid="deduct-payback-date" label="缴回日期" required>
          <el-date-picker v-model="paybackForm.paybackDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="凭证号/流水" required>
          <el-input v-model="paybackForm.paybackVoucher" data-testid="deduct-payback-voucher"
                    placeholder="如 YB-PAY-20260918-0037"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="paybackVisible = false">取消</el-button>
        <el-button :loading="paybackSubmitting" data-testid="deduct-payback-submit" type="primary"
                   @click="submitPayback">确认缴回
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 批次 新建/编辑 ==================== -->
    <el-dialog v-model="inspectFormVisible"
               :title="inspectForm.id ? `修改批次 ${inspectForm.inspectNo}` : '新建飞检/审核批次'" data-testid="inspect-form-dialog"
               width="700px">
      <el-form :model="inspectForm" label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="检查类型" required>
              <el-select v-model="inspectForm.inspectType" data-testid="inspect-form-type" style="width: 100%">
                <el-option v-for="d in dictOptions(DICT_TYPE.YB_INSPECT_TYPE)" :key="d.dictValue" :label="d.dictLabel"
                           :value="Number(d.dictValue)"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="统筹区/医保局" required>
              <el-input v-model="inspectForm.fundOrg" data-testid="inspect-form-fund-org"
                        placeholder="如 长沙市医疗保障局"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item data-testid="inspect-form-start" label="审核期间起" required>
              <el-date-picker v-model="inspectForm.inspectStartDate" style="width: 100%" type="date"
                              value-format="YYYY-MM-DD"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item data-testid="inspect-form-end" label="审核期间止" required>
              <el-date-picker v-model="inspectForm.inspectEndDate" style="width: 100%" type="date"
                              value-format="YYYY-MM-DD"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item data-testid="inspect-form-date" label="进驻/通知日期" required>
              <el-date-picker v-model="inspectForm.inspectDate" style="width: 100%" type="date"
                              value-format="YYYY-MM-DD"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="检查组">
              <el-input v-model="inspectForm.inspectTeam" placeholder="如 国家医保局第12飞检组"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="院内接待人">
              <el-input v-model="inspectForm.ourReceiver" placeholder="谁在现场对接检查组"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="inspectForm.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inspectFormVisible = false">取消</el-button>
        <el-button :loading="inspectSubmitting" data-testid="inspect-form-save" type="primary" @click="saveInspect">
          保存
        </el-button>
      </template>
    </el-dialog>

    <!-- ==================== 批次结项 ==================== -->
    <el-dialog v-model="concludeVisible" :title="`批次结项 ${concludeForm.inspectNo}`" data-testid="inspect-conclude-dialog"
               width="640px">
      <el-alert :closable="false" class="mb-3" title="结项后批次内容不可再改，结论会永久留档" type="info"/>
      <el-form :model="concludeForm" label-width="90px">
        <el-form-item label="结项结论" required>
          <el-input v-model="concludeForm.conclusion" :rows="4" data-testid="inspect-conclude-text"
                    placeholder="抽查多少份、发现几类问题、合计扣款多少、整改要求是什么"
                    type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="concludeVisible = false">取消</el-button>
        <el-button :loading="concludeSubmitting" data-testid="inspect-conclude-submit" type="primary"
                   @click="submitConclude">确认结项
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Edit, Money, Plus, Refresh, Search, View} from '@element-plus/icons-vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {getDepartmentSelectList} from '@/api/system';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {
  appealDeduct,
  appealResultDeduct,
  cancelDeduct,
  cancelYbInspection,
  concludeYbInspection,
  confirmDeduct,
  getDeductDetail,
  getDeductList,
  getDeductSummary,
  getYbInspectionList,
  getYbInspectionSelectList,
  paybackDeduct,
  upsertDeduct,
  upsertYbInspection
} from '@/api/insuranceDeduct';
// 状态码只用来判定按钮显隐与标签配色，文字一律从字典翻译（字典权威在 sql/163）
const ST_PENDING = 1, ST_APPEALING = 2, ST_WAIT_PAY = 4, ST_PAID = 5;
const INSPECT_RUNNING = 1;
const DEDUCT_DICT_TYPES = [
  DICT_TYPE.YB_INSPECT_TYPE, DICT_TYPE.YB_INSPECT_STATUS, DICT_TYPE.YB_DEDUCT_SOURCE,
  DICT_TYPE.YB_VIOLATION_TYPE, DICT_TYPE.YB_DEDUCT_STATUS, DICT_TYPE.YB_APPEAL_RESULT,
  DICT_TYPE.YB_LOSS_BEAR, DICT_TYPE.YB_DEDUCT_ACTION
];
const dicts = ref({});
const dictOptions = (type) => dicts.value[type] || [];
const dictText = (type, value) => dictLabelText(dicts.value[type], value);
const activeTab = ref('notice');
// ==================== 科室下拉（跨科室选责任科室，要全院范围） ====================
const deptOptions = ref([]);
const deptNameOf = (id) => deptOptions.value.find((d) => String(d.id) === String(id))?.deptName || '';
// ==================== 汇总（后端 SQL 聚合，禁止前端自算） ====================
const summary = ref({});
const loadSummary = async () => {
  try {
    const res = await getDeductSummary();
    summary.value = res.data || {};
  } catch (error) {
    ElMessage.error(error?.message || '扣款汇总加载失败');
  }
};
// ==================== 扣款通知列表 ====================
const noticeLoading = ref(false);
const noticeRows = ref([]);
const noticeQuery = reactive({
  deductStatus: undefined, sourceType: undefined,
  violationType: undefined, inspectionId: undefined,
  onlyOverdue: false, keyword: ''
});
const noticePage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const loadNotices = async () => {
  noticeLoading.value = true;
  try {
    const res = await getDeductList({
      ...noticeQuery, onlyOverdue: noticeQuery.onlyOverdue || undefined,
      pageNum: noticePage.pageNum, pageSize: noticePage.pageSize
    });
    noticeRows.value = res.data?.records || [];
    noticePage.total = res.data?.total || 0;
  } catch (error) {
    ElMessage.error(error?.message || '扣款通知列表加载失败');
  } finally {
    noticeLoading.value = false;
  }
};
const resetNoticeQuery = () => {
  noticeQuery.deductStatus = undefined;
  noticeQuery.sourceType = undefined;
  noticeQuery.violationType = undefined;
  noticeQuery.inspectionId = undefined;
  noticeQuery.onlyOverdue = false;
  noticeQuery.keyword = '';
  noticePage.pageNum = 1;
  loadNotices();
};
const money = (v) => `¥${Number(v ?? 0).toFixed(2)}`;
const statusTagType = (s) => s === ST_PENDING ? 'warning' : s === ST_APPEALING ? 'primary' : s === 3 ? 'success'
    : s === ST_WAIT_PAY ? 'danger' : s === ST_PAID ? 'success' : 'info';
// 动作显隐与后端状态机逐条对齐，后端仍是唯一裁判
const canEditNotice = (row) => row.deductStatus === ST_PENDING;
const canAppeal = (row) => row.deductStatus === ST_PENDING;
const canAppealResult = (row) => row.deductStatus === ST_APPEALING;
const canConfirm = (row) => row.deductStatus === ST_PENDING || row.deductStatus === ST_WAIT_PAY;
// 状态 4 有两个来路：申诉驳回（还没定责）和已确认追责。后端要求先定责才能缴回，
// 所以只有带 confirmBy 的待缴单才露「缴回」，否则点进去必然被拒。
const canPayback = (row) => row.deductStatus === ST_WAIT_PAY && !!row.confirmBy;
const canCancelNotice = (row) => row.deductStatus === ST_PENDING;
// ==================== 飞检批次候选 ====================
const inspectionOptions = ref([]);
const loadInspectionOptions = async () => {
  try {
    const res = await getYbInspectionSelectList();
    inspectionOptions.value = res.data || [];
  } catch (error) {
    ElMessage.error(error?.message || '飞检批次候选加载失败');
  }
};
// ==================== 通知单 新建/编辑 ====================
const noticeFormVisible = ref(false);
const noticeSubmitting = ref(false);
const emptyNotice = () => ({
  id: null, deductNo: '', sourceType: 1, inspectionId: undefined, patientId: undefined,
  patientName: '', patientNo: '', encounterType: undefined,
  deptId: undefined, deptName: '', doctorName: '', violationType: undefined,
  violationDesc: '', deductAmount: undefined, noticeDate: '', handleDeadline: '', remark: ''
});
const noticeForm = ref(emptyNotice());
const openNoticeCreate = async () => {
  noticeForm.value = emptyNotice();
  await loadInspectionOptions();
  noticeFormVisible.value = true;
};
const openNoticeEdit = async (row) => {
  try {
    const res = await getDeductDetail(row.id);
    // 日期字段后端出参就是 yyyy-MM-dd 文本，直接喂给 el-date-picker 的 value-format
    noticeForm.value = {
      ...emptyNotice(), ...(res.data || {}), id: row.id,
      inspectionId: res.data?.inspectionId ? String(res.data.inspectionId) : undefined,
      deptId: res.data?.deptId ? String(res.data.deptId) : undefined,
      patientId: res.data?.patientId ? String(res.data.patientId) : undefined
    };
    await loadInspectionOptions();
    noticeFormVisible.value = true;
  } catch (error) {
    ElMessage.error(error?.message || '扣款单详情加载失败');
  }
};
const onNoticePatient = (p) => {
  if (!p)
    return;
  noticeForm.value.patientName = p.patientName;
  noticeForm.value.patientNo = p.patientNo;
};
const onNoticeDept = (id) => {
  noticeForm.value.deptName = id ? deptNameOf(id) : '';
};
const saveNotice = async () => {
  const f = noticeForm.value;
  if (!f.sourceType || !f.violationType || !f.deductAmount || !f.noticeDate || !f.handleDeadline || !f.violationDesc) {
    ElMessage.warning('来源、违规类型、扣款金额、通知日期、处理期限、违规描述都要填');
    return;
  }
  noticeSubmitting.value = true;
  try {
    await upsertDeduct({
      id: f.id || undefined, sourceType: f.sourceType, inspectionId: f.inspectionId || undefined,
      patientId: f.patientId || undefined, patientName: f.patientName || undefined, patientNo: f.patientNo || undefined,
      encounterType: f.encounterType || undefined, deptId: f.deptId || undefined, deptName: f.deptName || undefined,
      doctorName: f.doctorName || undefined, violationType: f.violationType, violationDesc: f.violationDesc,
      deductAmount: f.deductAmount, noticeDate: f.noticeDate, handleDeadline: f.handleDeadline,
      remark: f.remark || undefined
    });
    ElMessage.success(f.id ? '扣款通知已更新' : '扣款通知已录入');
    noticeFormVisible.value = false;
    await Promise.all([loadNotices(), loadSummary()]);
  } catch (error) {
    ElMessage.error(error?.message || '保存失败');
  } finally {
    noticeSubmitting.value = false;
  }
};
// ==================== 详情（只读 + 留痕时间线） ====================
const noticeDetailVisible = ref(false);
const noticeDetail = ref(null);
const openNoticeDetail = async (row) => {
  try {
    const res = await getDeductDetail(row.id);
    noticeDetail.value = res.data || null;
    noticeDetailVisible.value = true;
  } catch (error) {
    ElMessage.error(error?.message || '扣款单详情加载失败');
  }
};
// ==================== 申诉 ====================
const appealVisible = ref(false);
const appealSubmitting = ref(false);
const appealForm = reactive({id: null, deductNo: '', appealReason: '', appealMaterial: ''});
const openAppeal = (row) => {
  Object.assign(appealForm, {id: row.id, deductNo: row.deductNo, appealReason: '', appealMaterial: ''});
  appealVisible.value = true;
};
const submitAppeal = async () => {
  if (!appealForm.appealReason) {
    ElMessage.warning('申诉理由必填，医保局看的就是这一句');
    return;
  }
  appealSubmitting.value = true;
  try {
    await appealDeduct({
      id: appealForm.id,
      appealReason: appealForm.appealReason,
      appealMaterial: appealForm.appealMaterial || undefined
    });
    ElMessage.success('已提交申诉，状态转为申诉中');
    appealVisible.value = false;
    await Promise.all([loadNotices(), loadSummary()]);
  } catch (error) {
    ElMessage.error(error?.message || '申诉提交失败');
  } finally {
    appealSubmitting.value = false;
  }
};
// ==================== 申诉结果 ====================
const resultVisible = ref(false);
const resultSubmitting = ref(false);
const resultForm = reactive({id: null, deductNo: '', appealResult: 1, appealResultRemark: ''});
const openAppealResult = (row) => {
  Object.assign(resultForm, {id: row.id, deductNo: row.deductNo, appealResult: 1, appealResultRemark: ''});
  resultVisible.value = true;
};
const submitAppealResult = async () => {
  resultSubmitting.value = true;
  try {
    await appealResultDeduct({
      id: resultForm.id,
      appealResult: resultForm.appealResult,
      appealResultRemark: resultForm.appealResultRemark || undefined
    });
    ElMessage.success(resultForm.appealResult === 1 ? '已记申诉成功，扣款撤销' : '已记申诉驳回，转入待缴');
    resultVisible.value = false;
    await Promise.all([loadNotices(), loadSummary()]);
  } catch (error) {
    ElMessage.error(error?.message || '申诉结果录入失败');
  } finally {
    resultSubmitting.value = false;
  }
};
// ==================== 确认扣款并追责 ====================
const confirmVisible = ref(false);
const confirmSubmitting = ref(false);
const confirmForm = reactive({
  id: null, deductNo: '', deductAmount: 0, liableDeptId: undefined,
  liableDeptName: '', liableEmpName: '', lossBearType: 2, bearDeptAmount: undefined, bearEmpAmount: undefined
});
const openConfirm = (row) => {
  Object.assign(confirmForm, {
    id: row.id, deductNo: row.deductNo, deductAmount: Number(row.deductAmount || 0),
    liableDeptId: row.deptId ? String(row.deptId) : undefined, liableDeptName: row.deptName || '',
    liableEmpName: row.doctorName || '', lossBearType: row.lossBearType || 2,
    bearDeptAmount: row.bearDeptAmount ?? undefined, bearEmpAmount: row.bearEmpAmount ?? undefined
  });
  if (!confirmForm.liableDeptId) {
    confirmForm.liableDeptName = '';
  }
  confirmVisible.value = true;
};
const onConfirmDept = (id) => {
  confirmForm.liableDeptName = id ? deptNameOf(id) : '';
};
const bearSum = computed(() => Number(confirmForm.bearDeptAmount || 0) + Number(confirmForm.bearEmpAmount || 0));
const submitConfirm = async () => {
  if (!confirmForm.liableDeptId) {
    ElMessage.warning('责任科室必选，追责追不到科室等于没追');
    return;
  }
  if (confirmForm.lossBearType === 4 && bearSum.value > confirmForm.deductAmount) {
    ElMessage.warning(`分摊合计 ${money(bearSum.value)} 已超过扣款 ${money(confirmForm.deductAmount)}，分摊不能超出实扣`);
    return;
  }
  confirmSubmitting.value = true;
  try {
    await confirmDeduct({
      id: confirmForm.id,
      liableDeptId: confirmForm.liableDeptId,
      liableDeptName: confirmForm.liableDeptName || undefined,
      liableEmpName: confirmForm.liableEmpName || undefined,
      lossBearType: confirmForm.lossBearType,
      bearDeptAmount: confirmForm.bearDeptAmount ?? undefined,
      bearEmpAmount: confirmForm.bearEmpAmount ?? undefined
    });
    ElMessage.success('已确认扣款并追责，转入待缴');
    confirmVisible.value = false;
    await Promise.all([loadNotices(), loadSummary()]);
  } catch (error) {
    ElMessage.error(error?.message || '确认追责失败');
  } finally {
    confirmSubmitting.value = false;
  }
};
// ==================== 缴回 ====================
const paybackVisible = ref(false);
const paybackSubmitting = ref(false);
const paybackForm = reactive({
  id: null,
  deductNo: '',
  deductAmount: 0,
  paidAmount: undefined,
  paybackDate: '',
  paybackVoucher: ''
});
const openPayback = (row) => {
  Object.assign(paybackForm, {
    id: row.id, deductNo: row.deductNo, deductAmount: Number(row.deductAmount || 0),
    paidAmount: Number(row.deductAmount || 0), paybackDate: '', paybackVoucher: ''
  });
  paybackVisible.value = true;
};
const submitPayback = async () => {
  if (!paybackForm.paidAmount || !paybackForm.paybackDate || !paybackForm.paybackVoucher) {
    ElMessage.warning('缴回金额、缴回日期、凭证号都要填');
    return;
  }
  paybackSubmitting.value = true;
  try {
    await paybackDeduct({
      id: paybackForm.id, paidAmount: paybackForm.paidAmount,
      paybackDate: paybackForm.paybackDate, paybackVoucher: paybackForm.paybackVoucher
    });
    ElMessage.success('已录入缴回，这张扣款单闭环');
    paybackVisible.value = false;
    await Promise.all([loadNotices(), loadSummary()]);
  } catch (error) {
    // 缴回金额与实扣不符、未确认追责先缴回这类情况由后端判定，拒的原因必须原样回给用户，
    // 否则点「确定」后弹窗静止、用户以为页面坏了
    ElMessage.error(error?.message || '缴回登记失败');
  } finally {
    paybackSubmitting.value = false;
  }
};
const cancelDeductRow = (row) => {
  ElMessageBox.prompt(`作废扣款单「${row.deductNo}」？仅误录/重复录入才作废，作废原因会写进留痕。`, '作废确认', {
    inputPlaceholder: '作废原因（必填）',
    inputValidator: (v) => (v && v.trim() ? true : '作废原因不能为空'),
    type: 'warning'
  }).then(async ({value}) => {
    try {
      await cancelDeduct({id: row.id, reason: value.trim()});
      ElMessage.success('已作废');
      await Promise.all([loadNotices(), loadSummary()]);
    } catch (error) {
      ElMessage.error(error?.message || '作废失败');
    }
  }).catch(() => {
  });
};
// ==================== 飞检批次 ====================
const inspectLoading = ref(false);
const inspectRows = ref([]);
const inspectQuery = reactive({inspectType: undefined, status: undefined, keyword: ''});
const inspectPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const loadInspections = async () => {
  inspectLoading.value = true;
  try {
    const res = await getYbInspectionList({
      ...inspectQuery,
      pageNum: inspectPage.pageNum,
      pageSize: inspectPage.pageSize
    });
    inspectRows.value = res.data?.records || [];
    inspectPage.total = res.data?.total || 0;
  } catch (error) {
    ElMessage.error(error?.message || '飞检批次列表加载失败');
  } finally {
    inspectLoading.value = false;
  }
};
const resetInspectQuery = () => {
  inspectQuery.inspectType = undefined;
  inspectQuery.status = undefined;
  inspectQuery.keyword = '';
  inspectPage.pageNum = 1;
  loadInspections();
};
const inspectFormVisible = ref(false);
const inspectSubmitting = ref(false);
const emptyInspect = () => ({
  id: null, inspectNo: '', inspectType: 1, fundOrg: '', inspectStartDate: '', inspectEndDate: '',
  inspectDate: '', inspectTeam: '', ourReceiver: '', remark: ''
});
const inspectForm = ref(emptyInspect());
const openInspectCreate = () => {
  inspectForm.value = emptyInspect();
  inspectFormVisible.value = true;
};
const openInspectEdit = (row) => {
  inspectForm.value = {...emptyInspect(), ...row, id: row.id};
  inspectFormVisible.value = true;
};
const saveInspect = async () => {
  const f = inspectForm.value;
  if (!f.fundOrg || !f.inspectStartDate || !f.inspectEndDate || !f.inspectDate) {
    ElMessage.warning('统筹区、审核期间、进驻日期都要填');
    return;
  }
  inspectSubmitting.value = true;
  try {
    await upsertYbInspection({
      id: f.id || undefined, inspectType: f.inspectType, fundOrg: f.fundOrg,
      inspectStartDate: f.inspectStartDate, inspectEndDate: f.inspectEndDate, inspectDate: f.inspectDate,
      inspectTeam: f.inspectTeam || undefined, ourReceiver: f.ourReceiver || undefined, remark: f.remark || undefined
    });
    ElMessage.success(f.id ? '批次已更新' : '批次已新建');
    inspectFormVisible.value = false;
    await Promise.all([loadInspections(), loadInspectionOptions()]);
  } catch (error) {
    ElMessage.error(error?.message || '保存失败');
  } finally {
    inspectSubmitting.value = false;
  }
};
const concludeVisible = ref(false);
const concludeSubmitting = ref(false);
const concludeForm = reactive({id: null, inspectNo: '', conclusion: ''});
const openConclude = (row) => {
  Object.assign(concludeForm, {id: row.id, inspectNo: row.inspectNo, conclusion: ''});
  concludeVisible.value = true;
};
const submitConclude = async () => {
  if (!concludeForm.conclusion) {
    ElMessage.warning('结项结论必填，飞检收尾要留下结论');
    return;
  }
  concludeSubmitting.value = true;
  try {
    await concludeYbInspection({id: concludeForm.id, conclusion: concludeForm.conclusion});
    ElMessage.success('批次已结项');
    concludeVisible.value = false;
    await Promise.all([loadInspections(), loadInspectionOptions()]);
  } catch (error) {
    ElMessage.error(error?.message || '结项失败');
  } finally {
    concludeSubmitting.value = false;
  }
};
const cancelInspectRow = (row) => {
  ElMessageBox.prompt(`作废批次「${row.inspectNo}」？名下有扣款通知时不能作废。`, '作废确认', {
    inputPlaceholder: '作废原因（必填）',
    inputValidator: (v) => (v && v.trim() ? true : '作废原因不能为空'),
    type: 'warning'
  }).then(async ({value}) => {
    try {
      await cancelYbInspection({id: row.id, reason: value.trim()});
      ElMessage.success('已作废');
      await Promise.all([loadInspections(), loadInspectionOptions()]);
    } catch (error) {
      ElMessage.error(error?.message || '批次作废失败');
    }
  }).catch(() => {
  });
};
// 从批次下钻到它名下的扣款单
const drillNotices = (row) => {
  noticeQuery.inspectionId = String(row.id);
  noticeQuery.onlyOverdue = false;
  noticePage.pageNum = 1;
  activeTab.value = 'notice';
  loadNotices();
};
const inspectTagType = (s) => (s === 1 ? 'primary' : s === 2 ? 'success' : 'info');
onMounted(async () => {
  dicts.value = await loadDictDataMap(DEDUCT_DICT_TYPES.join(','));
  await Promise.all([loadDepts(), loadNotices(), loadSummary(), loadInspections(), loadInspectionOptions()]);
});
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({scope: 'ALL'});
    deptOptions.value = res.data || [];
  } catch (error) {
    ElMessage.error(error?.message || '科室候选加载失败');
  }
};
</script>
