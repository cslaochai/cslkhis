<template>
  <div class="space-y-6">
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <el-button :icon="Refresh" @click="refreshAll">刷新</el-button>
    </div>

    <div class="grid grid-cols-2 gap-4 lg:grid-cols-5">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">已完成但缺合格访视</p>
        <p :class="stats.pendingVisit > 0 ? 'text-red-600' : 'text-slate-900'" class="text-lg font-bold"
           data-testid="g15-pending-visit">
          {{ stats.pendingVisit }}
        </p>
        <p class="text-[11px] text-slate-400">含急诊超前麻醉的待补账</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">PACU 在室</p>
        <p class="text-lg font-bold text-slate-900" data-testid="g15-pacu-inroom">{{ stats.inRoom }}</p>
        <p class="text-[11px] text-slate-400">人</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">麻醉记录未计费</p>
        <p :class="stats.uncharged > 0 ? 'text-amber-600' : 'text-slate-900'" class="text-lg font-bold"
           data-testid="g15-uncharged">
          {{ stats.uncharged }}
        </p>
        <p class="text-[11px] text-slate-400">条</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">随访欠账</p>
        <p :class="stats.followupOverdue > 0 ? 'text-red-600' : 'text-slate-900'" class="text-lg font-bold"
           data-testid="p134-fu-overdue">
          {{ stats.followupOverdue }}
        </p>
        <p class="text-[11px] text-slate-400">麻醉结束超 24h 无已完成随访</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">出室标准</p>
        <p class="text-lg font-bold text-slate-900">Aldrete ≥ {{ A.ALDRETE_DISCHARGE_MIN }}</p>
        <p class="text-[11px] text-slate-400">总分由服务端逐项相加</p>
      </div>
    </div>

    <el-tabs v-model="tab" data-testid="g15-tabs">
      <!-- ========== 麻醉记录单 ========== -->
      <el-tab-pane label="麻醉记录单" name="record">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="mb-4 flex flex-wrap items-center gap-3">
            <el-select v-model="filters.recordStatus" class="!w-36" clearable placeholder="记录状态" @change="loadList">
              <el-option v-for="o in A.recordStatusOptions" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-input
                v-model="filters.keyword"
                :prefix-icon="Search"
                class="!w-72"
                clearable
                placeholder="麻醉单号 / 申请单号 / 患者 / 术式"
                @clear="loadList"
                @keyup.enter="loadList"
            />
            <el-button :icon="Search" type="primary" @click="loadList">查询</el-button>
            <el-button v-perm="'ipd:anesthesia:add'" :icon="Plus" data-testid="g15-record-create" type="primary"
                       @click="openCreate">开立麻醉记录单
            </el-button>
          </div>

          <el-table v-loading="loading" :data="rows" data-testid="g15-record-table" style="width: 100%">
            <el-table-column label="麻醉单号" prop="recordNo" width="150"/>
            <el-table-column label="患者 / 术式" min-width="200">
              <template #default="{ row }">
                <div class="text-slate-900">{{ text(row.patientName) }}</div>
                <div class="text-[11px] text-slate-400">
                  {{ text(row.plannedOperationName) }}<span
                    v-if="row.actualOperationName"> / 实际 {{ row.actualOperationName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="麻醉方式 / ASA" width="150">
              <template #default="{ row }">
                <div class="text-slate-700">{{ text(row.anesthesiaTypeText) }}</div>
                <div class="text-[11px] text-slate-400">{{ text(row.asaText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="麻醉医师" width="110">
              <template #default="{ row }"><span class="text-slate-700">{{ text(row.anesthetistName) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="时长" width="140">
              <template #default="{ row }">
                <div class="text-slate-700">{{ text(row.anesthesiaDurationText) }}</div>
                <div class="text-[11px] text-slate-400">手术 {{ text(row.operationDurationText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.recordStatus)" effect="plain" size="small">
                  {{ text(row.recordStatusText) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="计费" min-width="140">
              <template #default="{ row }">
                <el-tag :type="chargeTagType(row.chargeStatus)" effect="plain" size="small">
                  {{ text(row.chargeStatusText) }}
                </el-tag>
                <div v-if="row.chargedAmount" class="text-[11px] text-slate-400">{{ row.chargedAmount }} 元</div>
              </template>
            </el-table-column>
            <el-table-column label="提示" min-width="180">
              <template #default="{ row }">
                <div v-if="row.warningText" class="text-[11px] font-medium text-red-600">
                  <el-icon class="mr-1 align-middle">
                    <Warning/>
                  </el-icon>
                  {{ row.warningText }}
                </div>
                <div v-else-if="row.visitPending" class="text-[11px] text-amber-600">急诊超前麻醉：待补访视</div>
                <span v-else class="text-[11px] text-slate-400">—</span>
              </template>
            </el-table-column>
            <el-table-column align="center" fixed="right" label="操作" width="290">
              <template #default="{ row }">
                <el-button v-if="row.canEditVitals" v-perm="'ipd:anesthesia:edit'" data-testid="g15-record-vital" link
                           type="primary" @click="openVital(row)">体征
                </el-button>
                <el-button v-if="row.canEditVitals" v-perm="'ipd:anesthesia:edit'" data-testid="g15-record-med" link
                           type="primary" @click="openMed(row)">用药
                </el-button>
                <el-button v-if="row.canEditVitals" v-perm="'ipd:anesthesia:edit'" data-testid="g15-record-update" link
                           type="primary" @click="openUpdate(row)">编辑
                </el-button>
                <el-button v-if="row.canSubmit" v-perm="'ipd:anesthesia:edit'" data-testid="g15-record-submit" link
                           type="success" @click="handleSubmit(row)">提交
                </el-button>
                <el-button v-if="row.canAudit" v-perm="'ipd:anesthesia:edit'" data-testid="g15-record-audit" link
                           type="warning" @click="handleAudit(row)">审核
                </el-button>
                <el-button v-if="row.canCharge && row.chargeStatus !== 1" v-perm="'ipd:anesthesia:edit'" data-testid="g15-record-charge"
                           link type="primary" @click="handleCharge(row)">计费
                </el-button>
                <el-button
                    v-if="row.recordStatus === 1 || row.recordStatus === 2"
                    v-perm="'ipd:anesthesia:add'"
                    data-testid="p134-record-followup"
                    link
                    type="success"
                    @click="openFollowup(undefined, { id: String(row.id), recordNo: row.recordNo, patientName: row.patientName, anesthesiaEndTime: row.anesthesiaEndTime })"
                >
                  随访
                </el-button>
                <el-button data-testid="g15-record-detail" link type="info" @click="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400" data-testid="g15-record-empty">
                暂无麻醉记录单（先做术前访视，再点「开立麻醉记录单」）
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
            />
          </div>
        </div>
      </el-tab-pane>

      <!-- ========== 术前访视 ========== -->
      <el-tab-pane label="术前访视" name="visit">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="mb-4 flex flex-wrap items-center gap-3">
            <el-select v-model="visitFilters.conclusion" class="!w-40" clearable placeholder="访视结论"
                       @change="loadVisits">
              <el-option v-for="o in A.visitConclusionOptions" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-input v-model="visitFilters.keyword" :prefix-icon="Search" class="!w-72" clearable
                      placeholder="访视单号 / 申请单号 / 患者" @clear="loadVisits" @keyup.enter="loadVisits"/>
            <el-button :icon="Search" type="primary" @click="loadVisits">查询</el-button>
            <el-button v-perm="'ipd:anesthesia:add'" :icon="Plus" data-testid="g15-visit-create" type="primary"
                       @click="openVisit">新建术前访视
            </el-button>
          </div>

          <el-table v-loading="visitLoading" :data="visitRows" data-testid="g15-visit-table" style="width: 100%">
            <el-table-column label="访视单号" prop="visitNo" width="150"/>
            <el-table-column label="患者 / 术式" min-width="200">
              <template #default="{ row }">
                <div class="text-slate-900">{{ text(row.patientName) }}</div>
                <div class="text-[11px] text-slate-400">{{ text(row.plannedOperationName) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="ASA / 气道" min-width="180">
              <template #default="{ row }">
                <div class="text-slate-700">{{ text(row.asaFullText) }}</div>
                <div class="text-[11px] text-slate-400">{{ text(row.mallampatiText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="困难气道 / 禁食" min-width="170">
              <template #default="{ row }">
                <div :class="row.difficultAirway === 1 ? 'text-red-600' : 'text-slate-700'">
                  困难气道 {{ text(row.difficultAirwayText) }}
                </div>
                <div class="text-[11px] text-slate-400">{{ text(row.npoText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="结论 / 状态" min-width="180">
              <template #default="{ row }">
                <el-tag :type="row.conclusion === 1 ? 'success' : 'warning'" effect="plain" size="small">
                  {{ text(row.conclusionText) }}
                </el-tag>
                <div class="text-[11px] text-slate-400">{{ text(row.visitStatusText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="访视医师" width="110">
              <template #default="{ row }"><span class="text-slate-700">{{ text(row.visitDoctorName) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="提示" min-width="180">
              <template #default="{ row }">
                <div v-if="row.warningText" class="text-[11px] font-medium text-red-600">{{ row.warningText }}</div>
                <span v-else class="text-[11px] text-slate-400">—</span>
              </template>
            </el-table-column>
            <el-table-column align="center" fixed="right" label="操作" width="150">
              <template #default="{ row }">
                <el-button v-perm="'ipd:anesthesia:edit'" data-testid="g15-visit-finish" link type="primary"
                           @click="handleVisitFinish(row)">完成访视
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400" data-testid="g15-visit-empty">暂无术前访视记录</div>
            </template>
          </el-table>

          <div class="mt-4 flex justify-end">
            <el-pagination v-model:current-page="visitPageNum" :total="visitTotal" layout="total, prev, pager, next"
                           @current-change="loadVisits"/>
          </div>
        </div>
      </el-tab-pane>

      <!-- ========== PACU ========== -->
      <el-tab-pane label="PACU 复苏" name="pacu">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="mb-4 flex flex-wrap items-center gap-3">
            <el-select v-model="pacuFilters.status" class="!w-36" clearable placeholder="在室状态" @change="loadPacus">
              <el-option v-for="o in A.pacuStatusOptions" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-input v-model="pacuFilters.keyword" :prefix-icon="Search" class="!w-72" clearable
                      placeholder="复苏单号 / 麻醉单号 / 患者" @clear="loadPacus" @keyup.enter="loadPacus"/>
            <el-button :icon="Search" type="primary" @click="loadPacus">查询</el-button>
            <el-button v-perm="'ipd:anesthesia:add'" :icon="Plus" data-testid="g15-pacu-enter" type="primary"
                       @click="openPacuEnter">入 PACU 登记
            </el-button>
          </div>

          <el-table v-loading="pacuLoading" :data="pacuRows" data-testid="g15-pacu-table" style="width: 100%">
            <el-table-column label="复苏单号" prop="pacuNo" width="150"/>
            <el-table-column label="患者 / 麻醉方式" min-width="190">
              <template #default="{ row }">
                <div class="text-slate-900">{{ text(row.patientName) }}</div>
                <div class="text-[11px] text-slate-400">{{ text(row.anesthesiaTypeText) }} · {{
                    text(row.recordNo)
                  }}
                </div>
              </template>
            </el-table-column>
            <el-table-column label="入室" min-width="150">
              <template #default="{ row }">
                <div class="text-slate-700">{{ fmt(row.enterTime) }}</div>
                <div class="text-[11px] text-slate-400">已停留 {{ text(row.stayDurationText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="Aldrete" width="150">
              <template #default="{ row }">
                <span :class="(row.aldreteTotal ?? 0) >= A.ALDRETE_DISCHARGE_MIN ? 'text-green-600' : 'text-amber-600'">
                  {{ row.aldreteTotal === null || row.aldreteTotal === undefined ? '—' : row.aldreteTotal + ' 分' }}
                </span>
                <div class="text-[11px] text-slate-400">{{ text(row.awarenessText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="状态 / 去向" min-width="150">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'warning' : 'success'" effect="plain" size="small">
                  {{ text(row.statusText) }}
                </el-tag>
                <div class="text-[11px] text-slate-400">{{ text(row.dispositionText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="计费" width="130">
              <template #default="{ row }">
                <el-tag :type="chargeTagType(row.chargeStatus)" effect="plain" size="small">
                  {{ text(row.chargeStatusText) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提示" min-width="180">
              <template #default="{ row }">
                <div v-if="row.warningText" class="text-[11px] font-medium text-red-600">{{ row.warningText }}</div>
                <span v-else class="text-[11px] text-slate-400">—</span>
              </template>
            </el-table-column>
            <el-table-column align="center" fixed="right" label="操作" width="180">
              <template #default="{ row }">
                <el-button v-if="row.canScore" v-perm="'ipd:anesthesia:edit'" data-testid="g15-pacu-score" link
                           type="primary" @click="openScore(row)">Aldrete 评分
                </el-button>
                <el-button v-if="row.canLeave" v-perm="'ipd:anesthesia:edit'" data-testid="g15-pacu-leave" link
                           type="success" @click="handleLeave(row)">出室
                </el-button>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400" data-testid="g15-pacu-empty">暂无 PACU
                复苏记录（麻醉记录提交后可登记入室）
              </div>
            </template>
          </el-table>

          <div class="mt-4 flex justify-end">
            <el-pagination v-model:current-page="pacuPageNum" :total="pacuTotal" layout="total, prev, pager, next"
                           @current-change="loadPacus"/>
          </div>
        </div>
      </el-tab-pane>

      <!-- ========== 麻醉随访（P134.3） ========== -->
      <el-tab-pane label="麻醉随访" name="followup">
        <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
          <div class="mb-4 flex flex-wrap items-center gap-3">
            <el-select v-model="followupFilters.followupStatus" class="!w-32" clearable data-testid="p134-fu-filter-status"
                       placeholder="状态" @change="loadFollowups">
              <el-option :value="0" label="草稿"/>
              <el-option :value="1" label="已完成"/>
            </el-select>
            <el-input
                v-model="followupFilters.keyword"
                :prefix-icon="Search"
                class="!w-72"
                clearable
                placeholder="随访单号 / 麻醉单号 / 患者"
                @clear="loadFollowups"
                @keyup.enter="loadFollowups"
            />
            <el-button :icon="Search" type="primary" @click="loadFollowups">查询</el-button>
            <el-button v-perm="'ipd:anesthesia:add'" :icon="Plus" data-testid="p134-fu-create" type="primary"
                       @click="openFollowup()">
              新建随访
            </el-button>
          </div>

          <el-table v-loading="followupLoading" :data="followupRows" data-testid="p134-fu-table" style="width: 100%">
            <el-table-column label="随访单号 / 轮次" width="170">
              <template #default="{ row }">
                <div class="text-slate-900">{{ text(row.followupNo) }}</div>
                <div class="text-[11px] text-slate-400">{{ text(row.roundText) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="患者 / 麻醉单" min-width="170">
              <template #default="{ row }">
                <div class="text-slate-900">{{ text(row.patientName) }}</div>
                <div class="text-[11px] text-slate-400">{{ text(row.recordNo) }}</div>
              </template>
            </el-table-column>
            <el-table-column label="随访时间" width="150">
              <template #default="{ row }"><span class="text-[12px] text-slate-700">{{ fmt(row.followupTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column align="center" label="疼痛 NRS" width="90">
              <template #default="{ row }">
                <span v-if="row.painScore === null || row.painScore === undefined" class="text-slate-400">未评</span>
                <span v-else :class="row.painScore >= 4 ? 'text-red-600' : 'text-slate-800'"
                      class="font-medium">{{ row.painScore }}</span>
              </template>
            </el-table-column>
            <el-table-column label="恢复情况" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.recovery" :type="row.recovery === 1 ? 'success' : row.recovery === 2 ? 'warning' : 'danger'"
                        effect="plain"
                        size="small">
                  {{ text(row.recoveryText) }}
                </el-tag>
                <span v-else class="text-slate-400">未评</span>
              </template>
            </el-table-column>
            <el-table-column label="并发症" min-width="160">
              <template #default="{ row }">
                <div v-if="!row.adverseItems" class="text-[11px] text-slate-400">无</div>
                <div v-else class="text-[11px] leading-5 text-slate-600">
                  {{ text(row.adverseItemsText) }}
                  <div v-if="row.adverseNote" class="text-slate-400">经过：{{ row.adverseNote }}</div>
                  <div v-if="row.handling" class="text-slate-400">处理：{{ row.handling }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="状态 / 随访人" min-width="140">
              <template #default="{ row }">
                <el-tag :type="row.followupStatus === 1 ? 'success' : 'info'" effect="plain" size="small">
                  {{ text(row.followupStatusText) }}
                </el-tag>
                <div class="mt-1 text-[11px] text-slate-400">{{ text(row.followupDoctorName) }}</div>
                <div v-if="row.finishTime" class="text-[11px] text-slate-400">{{ fmt(row.finishTime) }}</div>
              </template>
            </el-table-column>
            <el-table-column align="center" fixed="right" label="操作" width="190">
              <template #default="{ row }">
                <el-button v-if="row.canEdit" v-perm="'ipd:anesthesia:edit'" :data-testid="`p134-fu-edit-${row.id}`" link
                           type="primary" @click="openFollowup(row)">编辑
                </el-button>
                <el-button v-if="row.canFinish" v-perm="'ipd:anesthesia:edit'" :data-testid="`p134-fu-finish-${row.id}`" link
                           type="success" @click="handleFollowupFinish(row)">完成
                </el-button>
                <el-button v-if="row.canDelete" v-perm="'ipd:anesthesia:edit'" :data-testid="`p134-fu-del-${row.id}`" link
                           type="danger" @click="handleFollowupDelete(row)">删除
                </el-button>
                <span v-if="row.followupStatus === 1" class="text-[11px] text-slate-400">已锁定</span>
              </template>
            </el-table-column>
            <template #empty>
              <div class="py-6 text-sm text-slate-400" data-testid="p134-fu-empty">
                暂无随访（麻醉记录提交后，在记录行点「随访」或点「新建随访」）
              </div>
            </template>
          </el-table>

          <div class="mt-4 flex justify-end">
            <el-pagination
                v-model:current-page="followupPageNum"
                v-model:page-size="followupPageSize"
                :page-sizes="PAGE_SIZES"
                :total="followupTotal"
                layout="total, sizes, prev, pager, next"
                @current-change="loadFollowups"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 开立麻醉记录单 -->
    <el-dialog v-model="createVisible" data-testid="g15-create-dialog" title="开立麻醉记录单" width="680px">
      <el-form label-width="120px">
        <el-form-item label="手术申请单" required>
          <el-select v-model="createForm.applyId" class="!w-full" data-testid="g15-create-apply" filterable
                     placeholder="选择手术申请单">
            <el-option v-for="a in applies" :key="a.id" :label="applyLabel(a)" :value="String(a.id)"/>
          </el-select>
        </el-form-item>
        <div class="mb-3 rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] leading-5 text-amber-700">
          <el-icon class="mr-1 align-middle">
            <Warning/>
          </el-icon>
          必须先有结论为「可施行麻醉」的<b>术前访视</b>才能开立；急诊手术允许先麻醉，
          但本单会一直标「待补访视」。访视结论明确为「暂缓 / 需会诊」的，连急诊也不能越过。
        </div>
        <el-form-item label="麻醉医师">
          <el-select v-model="createForm.anesthetistId" class="!w-full" clearable filterable
                     placeholder="默认取当前登录人">
            <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="麻醉方式">
          <el-select v-model="createForm.anesthesiaType" class="!w-full" clearable placeholder="不传则取申请单登记值">
            <el-option :value="1" label="全身麻醉"/>
            <el-option :value="2" label="椎管内麻醉"/>
            <el-option :value="3" label="神经阻滞麻醉"/>
            <el-option :value="4" label="局部麻醉"/>
            <el-option :value="5" label="其他"/>
          </el-select>
        </el-form-item>
        <el-form-item label="麻醉助手">
          <el-input v-model="createForm.assistantAnesthetistName" placeholder="多人用逗号分隔，可空"/>
        </el-form-item>
        <el-form-item label="入室时间">
          <el-date-picker v-model="createForm.enterRoomTime" class="!w-full" placeholder="不填则取当前时间"
                          type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" :rows="2" placeholder="可空" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button v-perm="'ipd:anesthesia:add'" :loading="createSubmitting" data-testid="g15-create-submit"
                   type="primary" @click="submitCreate">开立
        </el-button>
      </template>
    </el-dialog>

    <!-- 更新麻醉记录 -->
    <el-dialog v-model="updateVisible" data-testid="g15-update-dialog" title="更新麻醉记录（时间轴 / 出入量）"
               width="780px">
      <div v-if="updateTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
          <span class="font-medium text-slate-900">{{ text(updateTarget.recordNo) }}</span>
          <span class="ml-3 text-slate-600">{{
              text(updateTarget.patientName)
            }} · {{ text(updateTarget.plannedOperationName) }}</span>
        </div>
        <el-form label-width="110px">
          <el-form-item label="麻醉方式">
            <el-select v-model="updateForm.anesthesiaType" class="!w-full" clearable>
              <el-option :value="1" label="全身麻醉"/>
              <el-option :value="2" label="椎管内麻醉"/>
              <el-option :value="3" label="神经阻滞麻醉"/>
              <el-option :value="4" label="局部麻醉"/>
              <el-option :value="5" label="其他"/>
            </el-select>
          </el-form-item>
          <el-form-item label="麻醉方法描述">
            <el-input v-model="updateForm.anesthesiaMethodDetail" placeholder="如：静吸复合全麻 + 气管插管"/>
          </el-form-item>
          <el-form-item label="气道管理">
            <el-select v-model="updateForm.airwayDevice" class="!w-52" clearable placeholder="选择">
              <el-option v-for="o in A.airwayDeviceOptions" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-input v-model="updateForm.airwayDeviceSpec" class="!ml-2 !w-56" placeholder="规格（如 7.5# 加强型）"/>
          </el-form-item>
          <el-form-item label="通气方式">
            <el-select v-model="updateForm.ventilationMode" class="!w-52" clearable placeholder="选择">
              <el-option :value="1" label="自主呼吸"/>
              <el-option :value="2" label="辅助通气"/>
              <el-option :value="3" label="控制通气"/>
            </el-select>
          </el-form-item>
          <!-- ⚠ el-date-picker 上的 data-testid **不会落到 DOM**（与 el-input 落 <input>、el-select 落根不同），
               验证脚本定位不到 → 把锚点打在外层 el-form-item 上，用 [data-testid] input 取内部输入框 -->
          <el-form-item data-testid="g15-update-anestart" label="麻醉开始" required>
            <el-date-picker v-model="updateForm.anesthesiaStartTime" class="!w-full" placeholder="诱导开始"
                            type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
          </el-form-item>
          <el-form-item data-testid="g15-update-aneend" label="麻醉结束" required>
            <el-date-picker v-model="updateForm.anesthesiaEndTime" class="!w-full" data-testid="g15-update-aneend"
                            placeholder="停药" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
          </el-form-item>
          <el-form-item label="切皮 / 关腹">
            <el-date-picker v-model="updateForm.operationStartTime" class="!w-56" placeholder="切皮"
                            type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
            <el-date-picker v-model="updateForm.operationEndTime" class="!ml-2 !w-56" placeholder="关腹/关胸"
                            type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
          </el-form-item>
          <el-form-item label="出入量（ml）">
            <el-input v-model.number="updateForm.crystalloid" class="!w-32" placeholder="晶体液"/>
            <el-input v-model.number="updateForm.colloid" class="!ml-2 !w-32" placeholder="胶体液"/>
            <el-input v-model.number="updateForm.bloodTransfusion" class="!ml-2 !w-32" placeholder="输血"/>
            <el-input v-model.number="updateForm.urineOutput" class="!ml-2 !w-32" placeholder="尿量"/>
            <el-input v-model.number="updateForm.bloodLoss" class="!ml-2 !w-32" placeholder="出血"/>
          </el-form-item>
          <el-form-item label="不良事件">
            <el-radio-group v-model="updateForm.adverseEventFlag">
              <el-radio :value="0">无</el-radio>
              <el-radio :value="1">有</el-radio>
            </el-radio-group>
            <el-input v-if="updateForm.adverseEventFlag === 1" v-model="updateForm.adverseEventNote"
                      class="!ml-3 !w-96" placeholder="经过与处理（必填）"/>
          </el-form-item>
          <el-form-item label="麻醉效果">
            <el-select v-model="updateForm.anesthesiaEffect" class="!w-full" clearable>
              <el-option v-for="o in A.effectOptions" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="术后去向">
            <el-select v-model="updateForm.postopDisposition" class="!w-full" clearable>
              <el-option v-for="o in A.dispositionOptions" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="updateVisible = false">取消</el-button>
        <el-button v-perm="'ipd:anesthesia:edit'" :loading="updateSubmitting" data-testid="g15-update-submit"
                   type="primary" @click="submitUpdate">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 追加生命体征 -->
    <el-dialog v-model="vitalVisible" data-testid="g15-vital-dialog" title="追加生命体征" width="640px">
      <div v-if="vitalTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
          {{ text(vitalTarget.recordNo) }} · {{ text(vitalTarget.patientName) }}
          <div class="text-[11px] text-slate-500">采样时刻在同一张麻醉单里唯一：同一时刻两组值属于数据错误</div>
        </div>
        <el-form label-width="110px">
          <el-form-item data-testid="g15-vital-time" label="采样时刻" required>
            <el-date-picker v-model="vitalForm.sampleTime" class="!w-full" data-testid="g15-vital-time"
                            type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
          </el-form-item>
          <el-form-item label="血压 mmHg">
            <el-input v-model.number="vitalForm.systolic" class="!w-32" data-testid="g15-vital-sys"
                      placeholder="收缩压"/>
            <span class="mx-2 text-slate-400">/</span>
            <el-input v-model.number="vitalForm.diastolic" class="!w-32" placeholder="舒张压"/>
          </el-form-item>
          <el-form-item label="心率 / 呼吸">
            <el-input v-model.number="vitalForm.heartRate" class="!w-32" placeholder="次/分"/>
            <el-input v-model.number="vitalForm.respiration" class="!ml-2 !w-32" placeholder="次/分"/>
          </el-form-item>
          <el-form-item label="SpO2 / EtCO2">
            <el-input v-model.number="vitalForm.spo2" class="!w-32" placeholder="%"/>
            <el-input v-model.number="vitalForm.etco2" class="!ml-2 !w-32" placeholder="mmHg"/>
          </el-form-item>
          <el-form-item label="体温 ℃">
            <el-input v-model.number="vitalForm.temperature" class="!w-32" placeholder="如 36.5"/>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="vitalForm.remark" placeholder="如：诱导后、气腹30min"/>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="vitalVisible = false">取消</el-button>
        <el-button v-perm="'ipd:anesthesia:edit'" :loading="vitalSubmitting" data-testid="g15-vital-submit"
                   type="primary" @click="submitVital">记录
        </el-button>
      </template>
    </el-dialog>

    <!-- 追加用药 -->
    <el-dialog v-model="medVisible" data-testid="g15-med-dialog" title="追加麻醉用药" width="600px">
      <div v-if="medTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
          {{ text(medTarget.recordNo) }} · {{ text(medTarget.patientName) }}
        </div>
        <el-form label-width="110px">
          <el-form-item data-testid="g15-med-time" label="给药时刻" required>
            <el-date-picker v-model="medForm.medTime" class="!w-full" data-testid="g15-med-time" type="datetime"
                            value-format="YYYY-MM-DD HH:mm:ss"/>
          </el-form-item>
          <el-form-item label="阶段">
            <el-select v-model="medForm.medPhase" class="!w-48">
              <el-option v-for="o in A.medPhaseOptions" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="药品名称" required>
            <el-input v-model="medForm.drugName" data-testid="g15-med-name" placeholder="如：丙泊酚 / 瑞芬太尼"/>
          </el-form-item>
          <el-form-item label="剂量">
            <el-input v-model.number="medForm.dose" class="!w-32" data-testid="g15-med-dose"/>
            <el-input v-model="medForm.unit" class="!ml-2 !w-24"/>
          </el-form-item>
          <el-form-item label="给药途径">
            <el-select v-model="medForm.route" class="!w-full">
              <el-option v-for="o in A.medRouteOptions" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="medVisible = false">取消</el-button>
        <el-button v-perm="'ipd:anesthesia:edit'" :loading="medSubmitting" data-testid="g15-med-submit" type="primary"
                   @click="submitMed">记录
        </el-button>
      </template>
    </el-dialog>

    <!-- 新建术前访视 -->
    <el-dialog v-model="visitVisible" data-testid="g15-visit-dialog" title="麻醉术前访视" width="780px">
      <el-form label-width="120px">
        <el-form-item label="手术申请单" required>
          <el-select v-model="visitForm.applyId" class="!w-full" data-testid="g15-visit-apply" filterable
                     placeholder="选择手术申请单">
            <el-option v-for="a in applies" :key="a.id" :label="applyLabel(a)" :value="String(a.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="ASA 分级">
          <el-select v-model="visitForm.asaGrade" class="!w-40" clearable data-testid="g15-visit-asa">
            <el-option v-for="o in A.asaOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
          <el-checkbox v-model="visitForm.asaEmergency" :false-value="0" :true-value="1" class="!ml-4">急诊（E）
          </el-checkbox>
        </el-form-item>
        <el-form-item label="气道评估">
          <el-select v-model="visitForm.mallampati" class="!w-56" clearable placeholder="Mallampati">
            <el-option v-for="o in A.mallampatiOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
          <el-select v-model="visitForm.neckMobility" class="!ml-2 !w-36" clearable placeholder="颈部活动度">
            <el-option v-for="o in A.neckMobilityOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
          <el-input v-model.number="visitForm.mouthOpenCm" class="!ml-2 !w-32" placeholder="张口度 cm"/>
        </el-form-item>
        <el-form-item label="困难气道">
          <el-radio-group v-model="visitForm.difficultAirway" data-testid="g15-visit-airway">
            <el-radio :value="0">否</el-radio>
            <el-radio :value="1">是</el-radio>
          </el-radio-group>
          <span class="ml-3 text-[12px] text-amber-600">标「是」必须填写备选方案</span>
        </el-form-item>
        <el-form-item label="既往麻醉史">
          <el-input v-model="visitForm.pastAnesthesiaHistory" :rows="2" placeholder="既往麻醉方式与不良反应"
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="过敏史">
          <el-input v-model="visitForm.allergyHistory" placeholder="药物/食物/消毒剂过敏"/>
        </el-form-item>
        <el-form-item label="长期用药史">
          <el-input v-model="visitForm.medicationHistory" placeholder="抗凝药/降压药/激素等"/>
        </el-form-item>
        <el-form-item label="禁食禁饮">
          <el-select v-model="visitForm.npoStatus" class="!w-56">
            <el-option v-for="o in A.npoOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="身高 / 体重">
          <el-input v-model.number="visitForm.heightCm" class="!w-32" placeholder="cm"/>
          <el-input v-model.number="visitForm.weightKg" class="!ml-2 !w-32" placeholder="kg"/>
        </el-form-item>
        <el-form-item label="麻醉计划">
          <el-input v-model="visitForm.anesthesiaPlan" :rows="2" placeholder="方法 + 主要用药 + 体位" type="textarea"/>
        </el-form-item>
        <el-form-item label="风险评估">
          <el-input v-model="visitForm.riskAssessment" :rows="2" placeholder="ASA ≥ Ⅳ 级必填" type="textarea"/>
        </el-form-item>
        <el-form-item label="备选方案">
          <el-input v-model="visitForm.backupPlan" :rows="2" placeholder="困难气道时的备用方案（标困难气道必填）"
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="访视结论">
          <el-select v-model="visitForm.conclusion" class="!w-56" clearable>
            <el-option v-for="o in A.visitConclusionOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visitVisible = false">取消</el-button>
        <el-button v-perm="'ipd:anesthesia:add'" :loading="visitSubmitting" data-testid="g15-visit-submit"
                   type="primary" @click="submitVisit">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 入 PACU -->
    <el-dialog v-model="enterVisible" data-testid="g15-pacu-enter-dialog" title="入 PACU 登记" width="620px">
      <el-form label-width="120px">
        <el-form-item label="麻醉记录单" required>
          <el-select v-model="enterForm.recordId" class="!w-full" data-testid="g15-pacu-record" filterable
                     placeholder="选择已提交的麻醉记录单">
            <el-option
                v-for="r in submittedRecords"
                :key="r.id"
                :label="`${r.recordNo} · ${r.patientName} · ${r.anesthesiaTypeText}`"
                :value="String(r.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="复苏护士">
          <el-select v-model="enterForm.nurseId" class="!w-full" clearable filterable placeholder="可空">
            <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="enterForm.remark" :rows="2" placeholder="可空" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="enterVisible = false">取消</el-button>
        <el-button v-perm="'ipd:anesthesia:add'" :loading="enterSubmitting" data-testid="g15-pacu-enter-submit"
                   type="primary" @click="submitPacuEnter">确认入室
        </el-button>
      </template>
    </el-dialog>

    <!-- Aldrete 评分 -->
    <el-dialog v-model="scoreVisible" data-testid="g15-pacu-score-dialog" title="Aldrete 评分" width="720px">
      <div v-if="scoreTarget" class="space-y-3">
        <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
          {{ text(scoreTarget.pacuNo) }} · {{ text(scoreTarget.patientName) }} · {{
            text(scoreTarget.anesthesiaTypeText)
          }}
        </div>
        <div class="text-[12px] text-slate-500">
          五项各 0~2 分，<b>总分由服务端逐项相加</b>（前端不传总分）；出室标准 ≥ {{ A.ALDRETE_DISCHARGE_MIN }} 分。
        </div>
        <el-form label-width="110px">
          <el-form-item v-for="(it, idx) in A.ALDRETE_ITEMS" :key="it.key" :label="it.label" required>
            <el-radio-group v-model="scoreValues[idx]" :data-testid="`g15-score-${it.key}`">
              <el-radio v-for="o in it.options" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="清醒程度">
            <el-select v-model="scoreForm.awareness" class="!w-48">
              <el-option v-for="o in A.awarenessOptions" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="氧疗">
            <el-input v-model="scoreForm.oxygenTherapy" placeholder="如 鼻导管 3L/min"/>
          </el-form-item>
          <el-form-item label="镇痛">
            <el-input v-model="scoreForm.analgesia" placeholder="如 PCIA"/>
          </el-form-item>
          <el-form-item label="并发症">
            <el-radio-group v-model="scoreForm.complicationFlag">
              <el-radio :value="0">无</el-radio>
              <el-radio :value="1">有</el-radio>
            </el-radio-group>
            <el-input v-if="scoreForm.complicationFlag === 1" v-model="scoreForm.complicationNote"
                      class="!ml-3 !w-96" placeholder="经过与处理（必填）"/>
          </el-form-item>
        </el-form>
        <div
            :class="scoreLocalTotal >= A.ALDRETE_DISCHARGE_MIN ? 'border-green-200 bg-green-50 text-green-700' : 'border-amber-200 bg-amber-50 text-amber-700'"
            class="rounded border px-3 py-2 text-[13px]"
        >
          本地合计 {{ scoreLocalTotal }} 分（以服务端计算为准）·
          {{
            scoreLocalTotal >= A.ALDRETE_DISCHARGE_MIN ? '已达出室标准' : `未达出室标准（≥${A.ALDRETE_DISCHARGE_MIN}）`
          }}
        </div>
      </div>
      <template #footer>
        <el-button @click="scoreVisible = false">取消</el-button>
        <el-button v-perm="'ipd:anesthesia:edit'" :loading="scoreSubmitting" data-testid="g15-pacu-score-submit"
                   type="primary" @click="submitScore">记录评分
        </el-button>
      </template>
    </el-dialog>

    <!-- 麻醉记录详情 -->
    <el-drawer v-model="detailVisible" data-testid="g15-detail-drawer" size="60%" title="麻醉记录详情">
      <div v-if="detail" class="space-y-4">
        <div class="flex items-center gap-3">
          <el-icon class="text-slate-400">
            <Aim/>
          </el-icon>
          <span class="text-base font-semibold text-slate-900">{{ text(detail.recordNo) }}</span>
          <el-tag :type="statusTagType(detail.recordStatus)" size="small">{{ text(detail.recordStatusText) }}</el-tag>
          <el-tag :type="chargeTagType(detail.chargeStatus)" effect="plain" size="small">
            {{ text(detail.chargeStatusText) }}
          </el-tag>
        </div>

        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="患者">{{ text(detail.patientName) }}</el-descriptions-item>
          <el-descriptions-item label="入院号">{{ text(detail.admissionNo) }}</el-descriptions-item>
          <el-descriptions-item label="术式">{{ text(detail.plannedOperationName) }}</el-descriptions-item>
          <el-descriptions-item label="实际术式">{{ text(detail.actualOperationName) }}</el-descriptions-item>
          <el-descriptions-item label="麻醉方式">{{ text(detail.anesthesiaTypeText) }}</el-descriptions-item>
          <el-descriptions-item label="ASA">{{ text(detail.asaText) }}</el-descriptions-item>
          <el-descriptions-item label="麻醉医师">{{ text(detail.anesthetistName) }}</el-descriptions-item>
          <el-descriptions-item label="手术间">{{ text(detail.operationRoom) }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="麻醉时段">{{ fmt(detail.anesthesiaStartTime) }} ~
            {{ fmt(detail.anesthesiaEndTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="麻醉时长">{{ text(detail.anesthesiaDurationText) }}（计费
            {{ text(detail.billHours) }} 小时）
          </el-descriptions-item>
          <el-descriptions-item label="计费">{{ text(detail.chargeStatusText) }}<span
              v-if="detail.chargedAmount"> · {{ detail.chargedAmount }} 元</span></el-descriptions-item>
          <el-descriptions-item label="术前访视">{{
              detail.visitId ? '已关联' : '待补（急诊超前麻醉）'
            }}
          </el-descriptions-item>
          <el-descriptions-item label="手术时长">{{ text(detail.operationDurationText) }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="detail.warningText"
             class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] text-amber-700">
          {{ detail.warningText }}
        </div>

        <div>
          <div class="mb-2 text-sm font-medium text-slate-800">生命体征（{{ detail.vitalCount ?? 0 }} 条）</div>
          <el-table :data="detail.vitals || []" size="small" style="width: 100%">
            <el-table-column label="时刻" min-width="140">
              <template #default="{ row }">{{ fmt(row.sampleTime) }}</template>
            </el-table-column>
            <el-table-column label="血压" width="110">
              <template #default="{ row }">{{ text(row.systolic) }}/{{ text(row.diastolic) }}</template>
            </el-table-column>
            <el-table-column label="心率" prop="heartRate" width="80"/>
            <el-table-column label="呼吸" prop="respiration" width="80"/>
            <el-table-column label="SpO2" prop="spo2" width="80"/>
            <el-table-column label="EtCO2" prop="etco2" width="80"/>
            <el-table-column label="体温" prop="temperature" width="80"/>
            <el-table-column label="提示" min-width="120">
              <template #default="{ row }">
                <span v-if="row.abnormalText" class="text-[11px] text-red-600">{{ row.abnormalText }}</span>
                <span v-else class="text-[11px] text-slate-400">—</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div>
          <div class="mb-2 text-sm font-medium text-slate-800">麻醉用药（{{ detail.medCount ?? 0 }} 条）</div>
          <el-table :data="detail.meds || []" size="small" style="width: 100%">
            <el-table-column label="时刻" min-width="140">
              <template #default="{ row }">{{ fmt(row.medTime) }}</template>
            </el-table-column>
            <el-table-column label="阶段" width="80">
              <template #default="{ row }">{{ text(row.medPhaseText) }}</template>
            </el-table-column>
            <el-table-column label="药品" min-width="160" prop="drugName"/>
            <el-table-column label="剂量" width="120">
              <template #default="{ row }">{{ text(row.doseText) }}</template>
            </el-table-column>
            <el-table-column label="途径" width="110">
              <template #default="{ row }">{{ text(row.routeText) }}</template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-drawer>

    <!-- 麻醉术后随访（P134.3）：草稿可改，完成即锁死 -->
    <el-dialog
        v-model="followupVisible"
        :title="followupForm.id ? '修改随访草稿' : '新建麻醉随访'"
        data-testid="p134-fu-dialog"
        width="720px"
    >
      <el-form label-width="120px">
        <el-form-item label="麻醉记录" required>
          <el-select
              v-model="followupForm.recordId"
              :disabled="!!followupForm.id"
              class="!w-full"
              data-testid="p134-fu-record"
              filterable
              placeholder="选择已提交/已审核的麻醉记录"
          >
            <el-option
                v-for="t in followupTargets"
                :key="t.id"
                :label="`${t.recordNo || t.id} · ${t.patientName || ''}`"
                :value="t.id"
            />
          </el-select>
          <div v-if="!followupForm.id" class="mt-1 text-[11px] text-slate-400">
            轮次由服务端按该麻醉记录现有轮次 +1（第1轮术后即刻 / 第2轮24h / 第3轮48h…），前端不填
          </div>
        </el-form-item>
        <el-form-item label="随访时间" required>
          <el-date-picker
              v-model="followupForm.followupTime"
              class="!w-full"
              data-testid="p134-fu-time"
              placeholder="不得早于麻醉结束时间"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
          />
          <div v-if="followupTarget?.anesthesiaEndTime" class="mt-1 text-[11px] text-slate-400">
            该次麻醉结束：{{ fmt(followupTarget.anesthesiaEndTime) }}
          </div>
        </el-form-item>
        <el-form-item label="疼痛评分 NRS">
          <el-input-number v-model="followupForm.painScore" :max="10" :min="0" data-testid="p134-fu-pain"/>
          <span class="ml-2 text-[11px] text-slate-400">0~10（完成随访时必填；≥4 建议写明处理）</span>
        </el-form-item>
        <el-form-item label="恢复情况">
          <el-radio-group v-model="followupForm.recovery" data-testid="p134-fu-recovery">
            <el-radio v-for="o in A.followupRecoveryOptions" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="并发症">
          <el-checkbox-group v-model="followupForm.adverse" data-testid="p134-fu-adverse">
            <el-checkbox v-for="o in A.followupAdverseOptions" :key="o.value" :value="o.value">{{
                o.label
              }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <template v-if="hasAdverse">
          <el-form-item label="并发症经过" required>
            <el-input
                v-model="followupForm.adverseNote"
                :rows="2"
                data-testid="p134-fu-note"
                placeholder="发生了什么（勾了却写不出经过，比不勾更糟）"
                type="textarea"
            />
          </el-form-item>
          <el-form-item label="处理与转归" required>
            <el-input
                v-model="followupForm.handling"
                :rows="2"
                data-testid="p134-fu-handling"
                placeholder="怎么处理的、现在怎么样了"
                type="textarea"
            />
          </el-form-item>
        </template>
        <el-form-item label="备注">
          <el-input v-model="followupForm.remark" :rows="2" placeholder="可空" type="textarea"/>
        </el-form-item>
      </el-form>
      <div class="rounded border border-amber-200 bg-amber-50 px-3 py-2 text-[12px] leading-5 text-amber-700">
        <el-icon class="mr-1 align-middle">
          <Warning/>
        </el-icon>
        保存的是<b>草稿</b>（可改可删）；「完成随访」后记录锁死。疼痛与恢复情况在完成时为必填，
        勾选任何并发症则经过与处理必须写全 —— 闸门在服务端。
      </div>
      <template #footer>
        <el-button @click="followupVisible = false">取消</el-button>
        <el-button v-perm="['ipd:anesthesia:add', 'ipd:anesthesia:edit']" :loading="followupSubmitting" data-testid="p134-fu-submit"
                   type="primary" @click="submitFollowup">
          保存草稿
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Aim, Plus, Refresh, Search, Warning} from '@element-plus/icons-vue';
import {
  addAnesthesiaMed,
  addAnesthesiaVital,
  auditAnesthesiaRecord,
  chargeAnesthesiaRecord,
  createAnesthesiaRecord,
  deleteFollowup,
  enterPacu,
  finishAnesthesiaVisit,
  finishFollowup,
  getAnesthesiaRecordDetail,
  getAnesthesiaRecordListPage,
  getAnesthesiaUnchargedCount,
  getAnesthesiaVisitListPage,
  getFinishedWithoutVisitCount,
  getFollowupListPage,
  getFollowupOverdueCount,
  getPacuInRoomCount,
  getPacuListPage,
  leavePacu,
  saveAnesthesiaVisit,
  saveFollowup,
  scorePacu,
  submitAnesthesiaRecord,
  updateAnesthesiaRecord,
} from '@/api/inpatientAnesthesia';
import {getOperationApplyListPage} from '@/api/inpatientOperation';
import {getEmployeeList} from '@/api/system';
import * as A from '@/lib/anesthesia';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));
const applies = ref([]);
const employees = ref([]);
const applyLabel = (a) => `${a.applyNo || '—'} · ${a.patientName || '—'} · ${a.plannedOperationName || '—'}${a.isEmergency === 1 ? '（急诊）' : ''}`;
const loadBaseData = async () => {
  try {
    const res = await getOperationApplyListPage({pageNum: 1, pageSize: 200});
    applies.value = (res.data?.records || []);
  } catch (e) {
    console.error('加载手术申请失败:', e);
  }
  try {
    const res = await getEmployeeList({});
    employees.value = (res.data || []);
  } catch (e) {
    console.error('加载员工失败:', e);
  }
};
// ---------------- 统计 ----------------
const stats = reactive({pendingVisit: 0, inRoom: 0, uncharged: 0, followupOverdue: 0});
const loadStats = async () => {
  try {
    stats.pendingVisit = Number((await getFinishedWithoutVisitCount()).data || 0);
  } catch (e) { /* 角标失败不影响主流程 */
  }
  try {
    stats.inRoom = Number((await getPacuInRoomCount()).data || 0);
  } catch (e) { /* 同上 */
  }
  try {
    stats.uncharged = Number((await getAnesthesiaUnchargedCount()).data || 0);
  } catch (e) { /* 同上 */
  }
  try {
    stats.followupOverdue = Number((await getFollowupOverdueCount()).data || 0);
  } catch (e) { /* 同上 */
  }
};
const tab = ref('record');
const rows = ref([]);
const total = ref(0);
const pageNum = ref(1);
const pageSize = ref(DEFAULT_PAGE_SIZE);
const loading = ref(false);
const filters = reactive({recordStatus: '', keyword: ''});
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getAnesthesiaRecordListPage({
      recordStatus: filters.recordStatus === '' ? undefined : filters.recordStatus,
      keyword: filters.keyword || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    });
    rows.value = (res.data?.records || []);
    total.value = Number(res.data?.total || 0);
  } catch (e) {
    ElMessage.error(e.message || '加载麻醉记录失败');
  } finally {
    loading.value = false;
  }
};
const statusTagType = (s) => (s === 0 ? 'info' : s === 1 ? 'warning' : s === 2 ? 'success' : 'info');
const chargeTagType = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'info');
// ---- 开立 ----
const createVisible = ref(false);
const createSubmitting = ref(false);
const createForm = reactive({
  applyId: '',
  anesthetistId: '',
  anesthesiaType: undefined,
  assistantAnesthetistName: '',
  enterRoomTime: '',
  remark: '',
});
const openCreate = () => {
  createForm.applyId = '';
  createForm.anesthetistId = '';
  createForm.anesthesiaType = undefined;
  createForm.assistantAnesthetistName = '';
  createForm.enterRoomTime = '';
  createForm.remark = '';
  createVisible.value = true;
};
const submitCreate = async () => {
  if (!createForm.applyId) {
    ElMessage.warning('请选择手术申请单');
    return;
  }
  createSubmitting.value = true;
  try {
    const res = await createAnesthesiaRecord({
      applyId: createForm.applyId,
      anesthetistId: createForm.anesthetistId || undefined,
      anesthesiaType: createForm.anesthesiaType,
      assistantAnesthetistName: createForm.assistantAnesthetistName.trim() || undefined,
      enterRoomTime: createForm.enterRoomTime || undefined,
      remark: createForm.remark.trim() || undefined,
    });
    ElMessage.success(`麻醉记录单已开立：${res.data || ''}（记录中）`);
    createVisible.value = false;
    await Promise.all([loadList(), loadStats()]);
  } catch (e) {
    ElMessage.error(e.message || '开立麻醉记录单失败');
  } finally {
    createSubmitting.value = false;
  }
};
// ---- 更新（时间轴 / 出入量） ----
const updateVisible = ref(false);
const updateSubmitting = ref(false);
const updateTarget = ref(null);
const updateForm = reactive({
  anesthesiaType: undefined,
  anesthesiaMethodDetail: '',
  airwayDevice: undefined,
  airwayDeviceSpec: '',
  ventilationMode: undefined,
  enterRoomTime: '',
  anesthesiaStartTime: '',
  operationStartTime: '',
  operationEndTime: '',
  anesthesiaEndTime: '',
  leaveRoomTime: '',
  crystalloid: undefined,
  colloid: undefined,
  bloodTransfusion: undefined,
  urineOutput: undefined,
  bloodLoss: undefined,
  adverseEventFlag: 0,
  adverseEventNote: '',
  anesthesiaEffect: undefined,
  postopDisposition: undefined,
});
const openUpdate = async (row) => {
  let d = row;
  try {
    const res = await getAnesthesiaRecordDetail(row.id);
    d = (res.data || row);
  } catch (e) {
    /* 详情拉取失败就用列表行 */
  }
  updateTarget.value = d;
  updateForm.anesthesiaType = d.anesthesiaType;
  updateForm.anesthesiaStartTime = d.anesthesiaStartTime || '';
  updateForm.anesthesiaEndTime = d.anesthesiaEndTime || '';
  updateForm.enterRoomTime = '';
  updateForm.operationStartTime = '';
  updateForm.operationEndTime = '';
  updateForm.leaveRoomTime = '';
  updateForm.crystalloid = undefined;
  updateForm.colloid = undefined;
  updateForm.bloodTransfusion = undefined;
  updateForm.urineOutput = undefined;
  updateForm.bloodLoss = undefined;
  updateForm.adverseEventFlag = 0;
  updateForm.adverseEventNote = '';
  updateForm.anesthesiaEffect = undefined;
  updateForm.postopDisposition = undefined;
  updateForm.anesthesiaMethodDetail = '';
  updateForm.airwayDevice = undefined;
  updateForm.airwayDeviceSpec = '';
  updateForm.ventilationMode = undefined;
  updateVisible.value = true;
};
const submitUpdate = async () => {
  if (!updateTarget.value)
    return;
  if (updateForm.adverseEventFlag === 1 && !updateForm.adverseEventNote.trim()) {
    ElMessage.warning('已标记不良事件，必须填写经过与处理');
    return;
  }
  updateSubmitting.value = true;
  try {
    await updateAnesthesiaRecord({
      recordId: updateTarget.value.id,
      anesthesiaType: updateForm.anesthesiaType,
      anesthesiaMethodDetail: updateForm.anesthesiaMethodDetail.trim() || undefined,
      airwayDevice: updateForm.airwayDevice,
      airwayDeviceSpec: updateForm.airwayDeviceSpec.trim() || undefined,
      ventilationMode: updateForm.ventilationMode,
      enterRoomTime: updateForm.enterRoomTime || undefined,
      anesthesiaStartTime: updateForm.anesthesiaStartTime || undefined,
      anesthesiaEndTime: updateForm.anesthesiaEndTime || undefined,
      operationStartTime: updateForm.operationStartTime || undefined,
      operationEndTime: updateForm.operationEndTime || undefined,
      leaveRoomTime: updateForm.leaveRoomTime || undefined,
      crystalloid: updateForm.crystalloid,
      colloid: updateForm.colloid,
      bloodTransfusion: updateForm.bloodTransfusion,
      urineOutput: updateForm.urineOutput,
      bloodLoss: updateForm.bloodLoss,
      adverseEventFlag: updateForm.adverseEventFlag,
      adverseEventNote: updateForm.adverseEventNote.trim() || undefined,
      anesthesiaEffect: updateForm.anesthesiaEffect,
      postopDisposition: updateForm.postopDisposition,
    });
    ElMessage.success('麻醉记录已更新');
    updateVisible.value = false;
    await loadList();
  } catch (e) {
    ElMessage.error(e.message || '更新失败');
  } finally {
    updateSubmitting.value = false;
  }
};
// ---- 生命体征 ----
const vitalVisible = ref(false);
const vitalSubmitting = ref(false);
const vitalTarget = ref(null);
const vitalForm = reactive({
  sampleTime: '',
  systolic: undefined,
  diastolic: undefined,
  heartRate: undefined,
  respiration: undefined,
  temperature: undefined,
  spo2: undefined,
  etco2: undefined,
  remark: '',
});
const openVital = (row) => {
  vitalTarget.value = row;
  vitalForm.sampleTime = '';
  vitalForm.systolic = undefined;
  vitalForm.diastolic = undefined;
  vitalForm.heartRate = undefined;
  vitalForm.respiration = undefined;
  vitalForm.temperature = undefined;
  vitalForm.spo2 = undefined;
  vitalForm.etco2 = undefined;
  vitalForm.remark = '';
  vitalVisible.value = true;
};
const submitVital = async () => {
  if (!vitalTarget.value)
    return;
  if (!vitalForm.sampleTime) {
    ElMessage.warning('请选择采样时刻');
    return;
  }
  vitalSubmitting.value = true;
  try {
    await addAnesthesiaVital({
      recordId: vitalTarget.value.id,
      sampleTime: vitalForm.sampleTime,
      systolic: vitalForm.systolic,
      diastolic: vitalForm.diastolic,
      heartRate: vitalForm.heartRate,
      respiration: vitalForm.respiration,
      temperature: vitalForm.temperature,
      spo2: vitalForm.spo2,
      etco2: vitalForm.etco2,
      remark: vitalForm.remark.trim() || undefined,
    });
    ElMessage.success('生命体征已记录');
    vitalVisible.value = false;
    await loadList();
  } catch (e) {
    ElMessage.error(e.message || '记录生命体征失败');
  } finally {
    vitalSubmitting.value = false;
  }
};
// ---- 用药 ----
const medVisible = ref(false);
const medSubmitting = ref(false);
const medTarget = ref(null);
const medForm = reactive({
  medTime: '',
  medPhase: 2,
  drugName: '',
  dose: undefined,
  unit: 'mg',
  route: 1,
});
const openMed = (row) => {
  medTarget.value = row;
  medForm.medTime = '';
  medForm.medPhase = 2;
  medForm.drugName = '';
  medForm.dose = undefined;
  medForm.unit = 'mg';
  medForm.route = 1;
  medVisible.value = true;
};
const submitMed = async () => {
  if (!medTarget.value)
    return;
  if (!medForm.medTime) {
    ElMessage.warning('请选择给药时刻');
    return;
  }
  if (!medForm.drugName.trim()) {
    ElMessage.warning('药品名称不能为空（不知道给的什么药是不能接受的）');
    return;
  }
  medSubmitting.value = true;
  try {
    await addAnesthesiaMed({
      recordId: medTarget.value.id,
      medTime: medForm.medTime,
      medPhase: medForm.medPhase,
      drugName: medForm.drugName.trim(),
      dose: medForm.dose,
      unit: medForm.unit,
      route: medForm.route,
    });
    ElMessage.success('麻醉用药已记录');
    medVisible.value = false;
    await loadList();
  } catch (e) {
    ElMessage.error(e.message || '记录用药失败');
  } finally {
    medSubmitting.value = false;
  }
};
// ---- 提交 / 审核 / 计费 ----
const handleSubmit = async (row) => {
  try {
    await ElMessageBox.confirm('提交后这张麻醉记录**不能再追加生命体征与用药**（术后补一条术中记载属于伪造）。确认提交？', '提交麻醉记录', {
      confirmButtonText: '确认提交',
      cancelButtonText: '再等等',
      type: 'warning'
    });
  } catch {
    return;
  }
  try {
    const res = await submitAnesthesiaRecord({id: row.id});
    const s = res.data || {};
    if (s.failedItems > 0) {
      ElMessage.warning(`已提交，但 ${s.failedItems}/${s.totalItems} 项计费失败：${(s.messages || []).join('；')}`);
    } else {
      ElMessage.success(`麻醉记录已提交，计费 ${s.successItems} 项共 ${s.amount} 元 → ${s.chargeNo || ''}`);
    }
    await Promise.all([loadList(), loadStats()]);
  } catch (e) {
    ElMessage.error(e.message || '提交失败');
  }
};
const handleAudit = async (row) => {
  try {
    await auditAnesthesiaRecord({id: row.id});
    ElMessage.success('麻醉记录已审核');
    await loadList();
  } catch (e) {
    ElMessage.error(e.message || '审核失败');
  }
};
const handleCharge = async (row) => {
  try {
    const res = await chargeAnesthesiaRecord({id: row.id});
    const s = res.data || {};
    ElMessage({
      type: s.failedItems > 0 ? 'warning' : 'success',
      message: `成功 ${s.successItems} 项 / 失败 ${s.failedItems} 项，金额 ${s.amount} 元`,
    });
    await Promise.all([loadList(), loadStats()]);
  } catch (e) {
    ElMessage.error(e.message || '计费失败');
  }
};
// ---- 详情 ----
const detailVisible = ref(false);
const detail = ref(null);
const openDetail = async (row) => {
  try {
    const res = await getAnesthesiaRecordDetail(row.id);
    detail.value = (res.data || row);
    detailVisible.value = true;
  } catch (e) {
    ElMessage.error(e.message || '加载麻醉记录详情失败');
  }
};
const visitRows = ref([]);
const visitTotal = ref(0);
const visitLoading = ref(false);
const visitPageNum = ref(1);
const visitFilters = reactive({conclusion: '', keyword: ''});
const loadVisits = async () => {
  visitLoading.value = true;
  try {
    const res = await getAnesthesiaVisitListPage({
      conclusion: visitFilters.conclusion === '' ? undefined : visitFilters.conclusion,
      keyword: visitFilters.keyword || undefined,
      pageNum: visitPageNum.value,
      pageSize: DEFAULT_PAGE_SIZE,
    });
    visitRows.value = (res.data?.records || []);
    visitTotal.value = Number(res.data?.total || 0);
  } catch (e) {
    ElMessage.error(e.message || '加载术前访视失败');
  } finally {
    visitLoading.value = false;
  }
};
const visitVisible = ref(false);
const visitSubmitting = ref(false);
const visitTargetId = ref('');
const visitForm = reactive({
  applyId: '',
  asaGrade: undefined,
  asaEmergency: 0,
  mallampati: undefined,
  neckMobility: undefined,
  mouthOpenCm: undefined,
  difficultAirway: 0,
  pastAnesthesiaHistory: '',
  allergyHistory: '',
  medicationHistory: '',
  npoStatus: 1,
  heightCm: undefined,
  weightKg: undefined,
  anesthesiaPlan: '',
  riskAssessment: '',
  backupPlan: '',
  conclusion: undefined,
  conclusionNote: '',
});
const openVisit = () => {
  visitTargetId.value = '';
  visitForm.applyId = '';
  visitForm.asaGrade = undefined;
  visitForm.asaEmergency = 0;
  visitForm.mallampati = undefined;
  visitForm.neckMobility = undefined;
  visitForm.mouthOpenCm = undefined;
  visitForm.difficultAirway = 0;
  visitForm.pastAnesthesiaHistory = '';
  visitForm.allergyHistory = '';
  visitForm.medicationHistory = '';
  visitForm.npoStatus = 1;
  visitForm.heightCm = undefined;
  visitForm.weightKg = undefined;
  visitForm.anesthesiaPlan = '';
  visitForm.riskAssessment = '';
  visitForm.backupPlan = '';
  visitForm.conclusion = undefined;
  visitForm.conclusionNote = '';
  visitVisible.value = true;
};
const submitVisit = async () => {
  if (!visitForm.applyId) {
    ElMessage.warning('请选择手术申请单');
    return;
  }
  visitSubmitting.value = true;
  try {
    await saveAnesthesiaVisit({
      id: visitTargetId.value || undefined,
      applyId: visitForm.applyId,
      asaGrade: visitForm.asaGrade,
      asaEmergency: visitForm.asaEmergency,
      mallampati: visitForm.mallampati,
      neckMobility: visitForm.neckMobility,
      mouthOpenCm: visitForm.mouthOpenCm,
      difficultAirway: visitForm.difficultAirway,
      pastAnesthesiaHistory: visitForm.pastAnesthesiaHistory.trim() || undefined,
      allergyHistory: visitForm.allergyHistory.trim() || undefined,
      medicationHistory: visitForm.medicationHistory.trim() || undefined,
      npoStatus: visitForm.npoStatus,
      heightCm: visitForm.heightCm,
      weightKg: visitForm.weightKg,
      anesthesiaPlan: visitForm.anesthesiaPlan.trim() || undefined,
      riskAssessment: visitForm.riskAssessment.trim() || undefined,
      backupPlan: visitForm.backupPlan.trim() || undefined,
      conclusion: visitForm.conclusion,
      conclusionNote: visitForm.conclusionNote.trim() || undefined,
    });
    ElMessage.success('术前访视已保存（尚未给出结论，不能作为麻醉依据）');
    visitVisible.value = false;
    await loadVisits();
  } catch (e) {
    ElMessage.error(e.message || '保存术前访视失败');
  } finally {
    visitSubmitting.value = false;
  }
};
const handleVisitFinish = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt('完成访视意味着麻醉科的评估结论正式出账。结论不是「可施行麻醉」时必须填写说明。', `完成术前访视 ${row.visitNo || ''}`, {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPlaceholder: '结论：1-可施行麻醉 2-暂缓手术 3-需会诊/进一步评估（填数字）',
      inputValue: '1',
      inputValidator: (v) => (['1', '2', '3'].includes(String(v).trim()) ? true : '请填 1 / 2 / 3'),
    });
    const conclusion = Number(String(value).trim());
    if (conclusion !== 1) {
      const r = await ElMessageBox.prompt('结论非「可施行麻醉」，必须说明原因', '结论说明', {
        inputPlaceholder: '如：血压未控制，建议内科会诊后再评估',
        inputValidator: (v) => (v && String(v).trim() ? true : '结论说明不能为空'),
      });
      await finishAnesthesiaVisit({visitId: row.id, conclusion, conclusionNote: String(r.value).trim()});
    } else {
      await finishAnesthesiaVisit({visitId: row.id, conclusion});
    }
    ElMessage.success('术前访视已完成');
    await Promise.all([loadVisits(), loadStats()]);
  } catch (e) {
    if (e === 'cancel' || e === 'close')
      return;
    ElMessage.error(e.message || '完成访视失败');
  }
};
const pacuRows = ref([]);
const pacuTotal = ref(0);
const pacuLoading = ref(false);
const pacuPageNum = ref(1);
const pacuFilters = reactive({status: '', keyword: ''});
const loadPacus = async () => {
  pacuLoading.value = true;
  try {
    const res = await getPacuListPage({
      status: pacuFilters.status === '' ? undefined : pacuFilters.status,
      keyword: pacuFilters.keyword || undefined,
      pageNum: pacuPageNum.value,
      pageSize: DEFAULT_PAGE_SIZE,
    });
    pacuRows.value = (res.data?.records || []);
    pacuTotal.value = Number(res.data?.total || 0);
  } catch (e) {
    ElMessage.error(e.message || '加载 PACU 记录失败');
  } finally {
    pacuLoading.value = false;
  }
};
const enterVisible = ref(false);
const enterSubmitting = ref(false);
const enterForm = reactive({recordId: '', nurseId: '', remark: ''});
const submittedRecords = ref([]);
const openPacuEnter = async () => {
  submittedRecords.value = [];
  try {
    const r1 = await getAnesthesiaRecordListPage({recordStatus: 1, pageNum: 1, pageSize: 200});
    const r2 = await getAnesthesiaRecordListPage({recordStatus: 2, pageNum: 1, pageSize: 200});
    submittedRecords.value = (r1.data?.records || []).concat((r2.data?.records || []));
  } catch (e) {
    console.error('加载已提交麻醉记录失败:', e);
  }
  enterForm.recordId = '';
  enterForm.nurseId = '';
  enterForm.remark = '';
  enterVisible.value = true;
};
const submitPacuEnter = async () => {
  if (!enterForm.recordId) {
    ElMessage.warning('请选择已提交的麻醉记录单');
    return;
  }
  enterSubmitting.value = true;
  try {
    const res = await enterPacu({
      recordId: enterForm.recordId,
      nurseId: enterForm.nurseId || undefined,
      remark: enterForm.remark.trim() || undefined,
    });
    ElMessage.success(`已登记入 PACU：${res.data || ''}`);
    enterVisible.value = false;
    await Promise.all([loadPacus(), loadStats()]);
  } catch (e) {
    ElMessage.error(e.message || '入 PACU 登记失败');
  } finally {
    enterSubmitting.value = false;
  }
};
const scoreVisible = ref(false);
const scoreSubmitting = ref(false);
const scoreTarget = ref(null);
/** Aldrete 五项取值（顺序与 lib/anesthesia.ALDRETE_ITEMS 一致） */
const scoreValues = reactive([2, 2, 2, 2, 2]);
const scoreForm = reactive({
  awareness: 1,
  oxygenTherapy: '',
  analgesia: '',
  complicationFlag: 0,
  complicationNote: '',
});
const openScore = (row) => {
  scoreTarget.value = row;
  scoreValues[0] = 2;
  scoreValues[1] = 2;
  scoreValues[2] = 2;
  scoreValues[3] = 2;
  scoreValues[4] = 2;
  scoreForm.awareness = 1;
  scoreForm.oxygenTherapy = '';
  scoreForm.analgesia = '';
  scoreForm.complicationFlag = 0;
  scoreForm.complicationNote = '';
  scoreVisible.value = true;
};
const scoreLocalTotal = computed(() => scoreValues.reduce((a, b) => a + b, 0));
const submitScore = async () => {
  if (!scoreTarget.value)
    return;
  if (scoreForm.complicationFlag === 1 && !scoreForm.complicationNote.trim()) {
    ElMessage.warning('已标记并发症，必须填写经过与处理');
    return;
  }
  scoreSubmitting.value = true;
  try {
    await scorePacu({
      pacuId: scoreTarget.value.id,
      scoreActivity: scoreValues[0],
      scoreRespiration: scoreValues[1],
      scoreCirculation: scoreValues[2],
      scoreConsciousness: scoreValues[3],
      scoreSpo2: scoreValues[4],
      awareness: scoreForm.awareness,
      oxygenTherapy: scoreForm.oxygenTherapy.trim() || undefined,
      analgesia: scoreForm.analgesia.trim() || undefined,
      complicationFlag: scoreForm.complicationFlag,
      complicationNote: scoreForm.complicationNote.trim() || undefined,
    });
    ElMessage.success(`Aldrete 评分已记录（服务端计算总分 ${scoreLocalTotal.value} 分）`);
    scoreVisible.value = false;
    await loadPacus();
  } catch (e) {
    ElMessage.error(e.message || '评分失败');
  } finally {
    scoreSubmitting.value = false;
  }
};
const handleLeave = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`当前 Aldrete ${text(row.aldreteTotal)} 分（出室标准 ≥ ${A.ALDRETE_DISCHARGE_MIN}）。去向填数字：1-回病房 2-转ICU 3-继续留观`, `出 PACU ${row.pacuNo || ''}`, {
      inputPlaceholder: '1 / 2 / 3',
      inputValue: row.criteriaMet ? '1' : '2',
      inputValidator: (v) => (['1', '2', '3'].includes(String(v).trim()) ? true : '请填 1 / 2 / 3'),
    });
    const disposition = Number(String(value).trim());
    let note = '';
    if (!row.criteriaMet) {
      const r = await ElMessageBox.prompt('未达出室标准，出室必须写明原因（且去向不能是回病房）', '出室说明', {
        inputPlaceholder: '如：SpO2 偏低，转 ICU 继续监护',
        inputValidator: (v) => (v && String(v).trim() ? true : '出室说明不能为空'),
      });
      note = String(r.value).trim();
    }
    const res = await leavePacu({pacuId: row.id, disposition, note: note || undefined});
    const s = res.data || {};
    ElMessage({
      type: s.failedItems > 0 ? 'warning' : 'success',
      message: s.failedItems > 0
          ? `已出室，但计费失败：${(s.messages || []).join('；')}`
          : `已出室，计费 ${s.successItems} 项共 ${s.amount} 元 → ${s.chargeNo || ''}`,
    });
    await Promise.all([loadPacus(), loadStats()]);
  } catch (e) {
    if (e === 'cancel' || e === 'close')
      return;
    ElMessage.error(e.message || '出室失败');
  }
};
const followupRows = ref([]);
const followupTotal = ref(0);
const followupPageNum = ref(1);
const followupPageSize = ref(DEFAULT_PAGE_SIZE);
const followupLoading = ref(false);
const followupFilters = reactive({followupStatus: '', keyword: ''});
const loadFollowups = async () => {
  followupLoading.value = true;
  try {
    const res = await getFollowupListPage({
      followupStatus: followupFilters.followupStatus === '' ? undefined : followupFilters.followupStatus,
      keyword: followupFilters.keyword || undefined,
      pageNum: followupPageNum.value,
      pageSize: followupPageSize.value,
    });
    followupRows.value = (res.data?.records || []);
    followupTotal.value = Number(res.data?.total || 0);
  } catch (e) {
    ElMessage.error(e.message || '加载麻醉随访失败');
  } finally {
    followupLoading.value = false;
  }
};
const reloadFollowupAll = async () => {
  await Promise.all([loadFollowups(), loadStats()]);
};
const nowStr = () => {
  const d = new Date();
  const p = (n) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`;
};
const followupVisible = ref(false);
const followupSubmitting = ref(false);
const followupTargets = ref([]);
const followupForm = reactive({
  id: '',
  recordId: '',
  followupTime: '',
  painScore: undefined,
  recovery: undefined,
  adverse: [],
  adverseNote: '',
  handling: '',
  remark: '',
});
const loadFollowupTargets = async () => {
  try {
    // 可选随访的锚点 = 已提交/已审核的麻醉记录（未定稿的过程没有"术后"可言）
    const res = await getAnesthesiaRecordListPage({pageNum: 1, pageSize: 200});
    followupTargets.value = (res.data?.records || [])
        .filter((r) => r.recordStatus === 1 || r.recordStatus === 2)
        .map((r) => ({
          id: String(r.id),
          recordNo: r.recordNo,
          patientName: r.patientName,
          anesthesiaEndTime: r.anesthesiaEndTime,
        }));
  } catch (e) {
    ElMessage.error(e.message || '加载可随访的麻醉记录失败');
  }
};
const openFollowup = async (row, fromRecord) => {
  if (row) {
    followupForm.id = row.id;
    followupForm.recordId = row.recordId || '';
    followupForm.followupTime = fmt(row.followupTime);
    followupForm.painScore = row.painScore ?? undefined;
    followupForm.recovery = row.recovery ?? undefined;
    followupForm.adverse = (row.adverseItems || '').split(',').filter(Boolean).map(Number);
    followupForm.adverseNote = row.adverseNote || '';
    followupForm.handling = row.handling || '';
    followupForm.remark = row.remark || '';
  } else {
    followupForm.id = '';
    followupForm.recordId = fromRecord?.id || '';
    followupForm.followupTime = nowStr();
    followupForm.painScore = undefined;
    followupForm.recovery = undefined;
    followupForm.adverse = [];
    followupForm.adverseNote = '';
    followupForm.handling = '';
    followupForm.remark = '';
  }
  followupVisible.value = true;
  if (!followupTargets.value.length)
    await loadFollowupTargets();
};
const followupTarget = computed(() => followupTargets.value.find((t) => String(t.id) === String(followupForm.recordId)));
const hasAdverse = computed(() => followupForm.adverse.length > 0);
const submitFollowup = async () => {
  if (!followupForm.recordId) {
    ElMessage.warning('请选择要随访的麻醉记录');
    return;
  }
  if (!followupForm.followupTime) {
    ElMessage.warning('随访时间不能为空（后端要求不早于麻醉结束时间）');
    return;
  }
  followupSubmitting.value = true;
  try {
    const res = await saveFollowup({
      id: followupForm.id || undefined,
      recordId: followupForm.recordId,
      followupTime: followupForm.followupTime,
      painScore: followupForm.painScore,
      recovery: followupForm.recovery,
      adverseItems: followupForm.adverse.join(',') || undefined,
      adverseNote: followupForm.adverseNote.trim() || undefined,
      handling: followupForm.handling.trim() || undefined,
      remark: followupForm.remark.trim() || undefined,
    });
    ElMessage.success(`${followupForm.id ? '随访草稿已更新' : '随访草稿已建'}：${res.data || ''}（轮次由服务端定）`);
    followupVisible.value = false;
    await reloadFollowupAll();
  } catch (e) {
    ElMessage.error(e.message || '保存随访失败');
  } finally {
    followupSubmitting.value = false;
  }
};
const handleFollowupFinish = async (row) => {
  try {
    await ElMessageBox.confirm(`确认完成随访 ${row.followupNo || ''}（${row.patientName || ''} ${row.roundText || ''}）？`
        + '完成后记录锁死：不可再修改、不可删除 —— 想清楚再点。', '完成随访', {
      confirmButtonText: '确认完成',
      cancelButtonText: '再检查下',
      type: 'warning'
    });
  } catch {
    return;
  }
  try {
    await finishFollowup(row.id);
    ElMessage.success('随访已完成（已锁定）');
    await reloadFollowupAll();
  } catch (e) {
    ElMessage.error(e.message || '完成随访失败');
  }
};
const handleFollowupDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`删除随访草稿 ${row.followupNo || ''}？草稿删掉不影响已签的轮次。`, '删除随访草稿', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }
  try {
    await deleteFollowup(row.id);
    ElMessage.success('随访草稿已删除');
    await reloadFollowupAll();
  } catch (e) {
    ElMessage.error(e.message || '删除失败');
  }
};
const refreshAll = async () => {
  await Promise.all([loadList(), loadVisits(), loadPacus(), loadFollowups(), loadStats()]);
};
onMounted(async () => {
  await loadBaseData();
  await refreshAll();
});
</script>
