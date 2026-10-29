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
    <AnesthesiaRecordAdd v-model="createVisible" :applies="applies" :employees="employees" @saved="reloadRecordAll"/>

    <!-- 更新麻醉记录 -->
    <AnesthesiaRecordEdit v-model="updateVisible" :row="updateTarget" @saved="loadList"/>

    <!-- 追加生命体征 -->
    <AnesthesiaVitalSignAdd v-model="vitalVisible" :row="vitalTarget" @saved="loadList"/>

    <!-- 追加用药 -->
    <AnesthesiaMedAdd v-model="medVisible" :row="medTarget" @saved="loadList"/>

    <!-- 新建术前访视 -->
    <AnesthesiaVisitEdit v-model="visitVisible" :applies="applies" @saved="loadVisits"/>

    <!-- 入 PACU -->
    <AnesthesiaPacuEnter v-model="enterVisible" :employees="employees" @saved="reloadPacuAll"/>

    <!-- Aldrete 评分 -->
    <AnesthesiaPacuScore v-model="scoreVisible" :row="scoreTarget" @saved="loadPacus"/>

    <!-- 麻醉记录详情 -->
    <AnesthesiaRecordDetail v-model="detailVisible" :row="detailRow"/>

    <!-- 麻醉术后随访（P134.3）：草稿可改，完成即锁死 -->
    <AnesthesiaFollowupEdit v-model="followupVisible" :row="followupRow" :from-record="followupFromRecord" @saved="reloadFollowupAll"/>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Search, Warning} from '@element-plus/icons-vue';
import {
  auditAnesthesiaRecord,
  chargeAnesthesiaRecord,
  deleteFollowup,
  finishAnesthesiaVisit,
  finishFollowup,
  getAnesthesiaRecordListPage,
  getAnesthesiaUnchargedCount,
  getAnesthesiaVisitListPage,
  getFinishedWithoutVisitCount,
  getFollowupListPage,
  getFollowupOverdueCount,
  getPacuInRoomCount,
  getPacuListPage,
  leavePacu,
  submitAnesthesiaRecord,
} from '@/api/inpatientAnesthesia';
import {getOperationApplyListPage} from '@/api/inpatientOperation';
import {getEmployeeList} from '@/api/system';
import AnesthesiaRecordAdd from './AnesthesiaRecordAdd.vue';
import AnesthesiaRecordEdit from './AnesthesiaRecordEdit.vue';
import AnesthesiaVitalSignAdd from './AnesthesiaVitalSignAdd.vue';
import AnesthesiaMedAdd from './AnesthesiaMedAdd.vue';
import AnesthesiaVisitEdit from './AnesthesiaVisitEdit.vue';
import AnesthesiaPacuEnter from './AnesthesiaPacuEnter.vue';
import AnesthesiaPacuScore from './AnesthesiaPacuScore.vue';
import AnesthesiaRecordDetail from './AnesthesiaRecordDetail.vue';
import AnesthesiaFollowupEdit from './AnesthesiaFollowupEdit.vue';
import * as A from '@/lib/anesthesia';
import {chargeTagType, fmt, statusTagType, text} from '@/lib/anesthesia';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {OPERATION_ANESTHETIST_ROLE_CODE} from '@/lib/operationSchedule';

const applies = ref([]);
const employees = ref([]);
const loadBaseData = async () => {
  try {
    const res = await getOperationApplyListPage({pageNum: 1, pageSize: 200});
    applies.value = (res.data?.records || []);
  } catch (e) {
    console.error('加载手术申请失败:', e);
  }
  try {
    // 麻醉记录单/PACU 上签的是麻醉医师，候选按角色 10041 收口（与排台同一口径）。
    // 原来拉的是「本部门员工」：手术医师登录时麻醉医师下拉里只有自己科室的人。
    const res = await getEmployeeList({roleCode: OPERATION_ANESTHETIST_ROLE_CODE});
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false;
  }
};
const reloadRecordAll = async () => {
  await Promise.all([loadList(), loadStats()]);
};
// ---- 开立 ----
const createVisible = ref(false);
const openCreate = () => {
  createVisible.value = true;
};
// ---- 更新（时间轴 / 出入量） ----
const updateVisible = ref(false);
const updateTarget = ref(null);
const openUpdate = (row) => {
  updateTarget.value = row;
  updateVisible.value = true;
};
// ---- 生命体征 ----
const vitalVisible = ref(false);
const vitalTarget = ref(null);
const openVital = (row) => {
  vitalTarget.value = row;
  vitalVisible.value = true;
};
// ---- 用药 ----
const medVisible = ref(false);
const medTarget = ref(null);
const openMed = (row) => {
  medTarget.value = row;
  medVisible.value = true;
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
const handleAudit = async (row) => {
  try {
    await auditAnesthesiaRecord({id: row.id});
    ElMessage.success('麻醉记录已审核');
    await loadList();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  }
};
// ---- 详情 ----
const detailVisible = ref(false);
const detailRow = ref(null);
const openDetail = (row) => {
  detailRow.value = row;
  detailVisible.value = true;
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    visitLoading.value = false;
  }
};
const visitVisible = ref(false);
const openVisit = () => {
  visitVisible.value = true;
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    pacuLoading.value = false;
  }
};
const reloadPacuAll = async () => {
  await Promise.all([loadPacus(), loadStats()]);
};
const enterVisible = ref(false);
const openPacuEnter = () => {
  enterVisible.value = true;
};
const scoreVisible = ref(false);
const scoreTarget = ref(null);
const openScore = (row) => {
  scoreTarget.value = row;
  scoreVisible.value = true;
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    followupLoading.value = false;
  }
};
const reloadFollowupAll = async () => {
  await Promise.all([loadFollowups(), loadStats()]);
};
const followupVisible = ref(false);
const followupRow = ref(null);
const followupFromRecord = ref(null);
const openFollowup = (row, fromRecord) => {
  followupRow.value = row || null;
  followupFromRecord.value = fromRecord || null;
  followupVisible.value = true;
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
