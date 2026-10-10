<template>
  <div class="space-y-4 p-4" data-testid="death-cert-page">
    <!-- 统计卡 -->
    <div class="grid grid-cols-12 gap-2">
      <el-card v-for="c in statCards" :key="c.k" class="!rounded-lg !py-1" shadow="never">
        <div class="text-center">
          <div :class="['text-xl font-semibold', c.cls]" :data-testid="'stat-' + c.k">{{ stats[c.k] ?? 0 }}</div>
          <div class="mt-0.5 text-[11px] text-slate-400">{{ c.label }}</div>
        </div>
      </el-card>
    </div>

    <el-tabs v-model="tab" data-testid="death-tabs">
      <!-- 页签 1：待开证榜 -->
      <el-tab-pane label="待开证榜" name="pending">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <el-input v-model="pq.keyword" clearable data-testid="pending-keyword" placeholder="住院号/姓名/出院单号"
                    style="width:220px" @keyup.enter="pq.pageNum = 1; loadPending()"/>
          <el-date-picker v-model="pq.startDate" data-testid="pending-start" placeholder="出院起" style="width:150px"
                          type="date" value-format="YYYY-MM-DD"/>
          <el-date-picker v-model="pq.endDate" data-testid="pending-end" placeholder="出院止" style="width:150px"
                          type="date" value-format="YYYY-MM-DD"/>
          <el-button :icon="Search" data-testid="pending-search" type="primary" @click="pq.pageNum = 1; loadPending()">
            查询
          </el-button>
          <el-button :icon="Refresh" data-testid="pending-reset" @click="resetPending">重置</el-button>
          <div class="flex-1"/>
          <span class="text-xs text-slate-400">出院办理不拦死亡未开证，但这里挂一天就是欠账一天</span>
        </div>
        <el-table v-loading="pLoading" :data="pRows" border data-testid="pending-table" size="small" stripe>
          <el-table-column label="住院号" min-width="120" prop="admissionNo"/>
          <el-table-column label="死者" min-width="120">
            <template #default="{ row }">{{ row.patientName }} / {{ patientGenderText(row.gender) }}</template>
          </el-table-column>
          <el-table-column label="出院（死亡）时间" min-width="160">
            <template #default="{ row }">{{ fmt(row.dischargeTime) }}</template>
          </el-table-column>
          <el-table-column label="科室/病区/床位" min-width="170">
            <template #default="{ row }">{{ row.deptName || '—' }} / {{ row.wardName || '—' }} / {{
                row.bedNo || '—'
              }}
            </template>
          </el-table-column>
          <el-table-column label="出院诊断" min-width="200" prop="dischargeDiagnosis" show-overflow-tooltip/>
          <el-table-column label="拖延" width="80">
            <template #default="{ row }"><span class="text-red-600">{{ row.pendingDays ?? 0 }} 天</span></template>
          </el-table-column>
          <el-table-column label="死亡登记" min-width="140">
            <template #default="{ row }">{{ row.registerNo || '未登记' }}</template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="160">
            <template #default="{ row }">
              <el-button v-perm="'ipd:deathCertificate:add'" :data-testid="'pending-issue-' + row.admissionNo" size="small"
                         type="primary"
                         @click.stop="openCertFormByAdmission(row.admissionId)">开证
              </el-button>
              <el-button v-perm="'ipd:deathRegister:add'" :data-testid="'pending-register-' + row.admissionNo"
                         size="small"
                         @click.stop="onOpenRegisterCreate(row.admissionId)">登记
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="pq.pageNum" v-model:page-size="pq.pageSize" :page-sizes="PAGE_SIZES"
                       :total="pTotal" class="mt-3" data-testid="pending-pagination"
                       layout="total, sizes, prev, pager, next"
                       @current-change="loadPending" @size-change="pq.pageNum = 1; loadPending()"/>
      </el-tab-pane>

      <!-- 页签 2：证明台账 -->
      <el-tab-pane label="证明台账" name="cert">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <el-input v-model="cq.keyword" clearable data-testid="cert-keyword" placeholder="证明编号/死者/根本死因"
                    style="width:220px" @keyup.enter="cq.pageNum = 1; loadCerts()"/>
          <el-select v-model="cq.certStatus" clearable data-testid="cert-filter-status" placeholder="证明状态"
                     style="width:130px">
            <el-option v-for="d in dict.certStatus" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
          <el-select v-model="cq.deathPlace" clearable data-testid="cert-filter-place" placeholder="死亡地点"
                     style="width:130px">
            <el-option v-for="d in dict.place" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
          <el-select v-model="cq.deathDeptId" :fit-input-width="false" clearable data-testid="cert-filter-dept" filterable
                     placeholder="死亡科室" style="width:170px">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
          <el-date-picker v-model="cq.startDate" data-testid="cert-start" placeholder="死亡起" style="width:140px"
                          type="date" value-format="YYYY-MM-DD"/>
          <el-date-picker v-model="cq.endDate" data-testid="cert-end" placeholder="死亡止" style="width:140px"
                          type="date" value-format="YYYY-MM-DD"/>
          <el-checkbox v-model="cq.overdue" :false-value="0" :true-value="1" data-testid="cert-filter-overdue"
                       @change="onCertOverdue">只看逾期未报
          </el-checkbox>
          <el-button :icon="Search" data-testid="cert-search" type="primary" @click="cq.pageNum = 1; loadCerts()">查询
          </el-button>
          <el-button :icon="Refresh" data-testid="cert-reset" @click="resetCert">重置</el-button>
        </div>
        <el-table v-loading="cLoading" :data="cRows" border data-testid="cert-table" size="small" stripe>
          <el-table-column label="证明编号" min-width="160">
            <template #default="{ row }">
              {{ row.certNo }}
              <el-tag v-if="row.origCertNo" class="ml-1" size="small" type="info">重开自 {{ row.origCertNo }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="死者" min-width="110">
            <template #default="{ row }">{{ row.patientName }} / {{ patientGenderText(row.gender) }} / {{
                row.age ?? '—'
              }}岁
            </template>
          </el-table-column>
          <el-table-column label="死亡时间" min-width="150">
            <template #default="{ row }">{{ fmt(row.deathTime) }}</template>
          </el-table-column>
          <el-table-column label="死亡地点/科室" min-width="150">
            <template #default="{ row }">{{ placeText(row.deathPlace) }} {{ row.deathDeptName || '' }}</template>
          </el-table-column>
          <el-table-column label="根本死因" min-width="170" show-overflow-tooltip>
            <template #default="{ row }">{{ row.underlyingIcdCode || '未编码' }} {{
                row.underlyingIcdName || ''
              }}
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="certTag(Number(row.certStatus))">{{ certStatusText(row.certStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上报" width="100">
            <template #default="{ row }">
              <el-tag :type="reportTag(Number(row.reportStatus))" size="small">{{
                  reportStatusText(row.reportStatus)
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上报时限" min-width="150">
            <template #default="{ row }">
              <span :class="row.overdue ? 'text-red-600' : ''">{{ fmt(row.reportDeadline) }}</span>
              <span v-if="row.overdue" class="ml-1 text-xs text-red-600">已逾期</span>
              <span v-else-if="Number(row.certStatus) === CS.ISSUED && row.remainHours != null"
                    class="ml-1 text-xs text-slate-400">余 {{ row.remainHours }}h</span>
            </template>
          </el-table-column>
          <el-table-column label="打印" width="70">
            <template #default="{ row }">{{ row.printCount ?? 0 }} 次</template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="300">
            <template #default="{ row }">
              <el-button v-if="certEditable(row)" v-perm="'ipd:deathCertificate:add'" :data-testid="'cert-edit-' + row.certNo"
                         size="small" @click.stop="openCertEdit(row.id)">修改
              </el-button>
              <el-button v-if="Number(row.certStatus) === CS.DRAFT" v-perm="'ipd:deathCertificate:edit'" :data-testid="'cert-audit-' + row.certNo"
                         size="small" type="primary" @click.stop="onAudit(row)">审核
              </el-button>
              <el-button v-if="Number(row.certStatus) === CS.AUDITED" v-perm="'ipd:deathCertificate:edit'" :data-testid="'cert-issue-' + row.certNo"
                         size="small" type="success" @click.stop="onIssue(row)">签发
              </el-button>
              <el-button v-if="Number(row.certStatus) === CS.ISSUED" v-perm="'ipd:deathCertificate:print'" :data-testid="'cert-print-' + row.certNo"
                         :icon="Printer" size="small" @click.stop="onPrint(row)">四联
              </el-button>
              <el-button v-if="Number(row.certStatus) === CS.ISSUED && Number(row.reportStatus) !== 2"
                         v-perm="'ipd:deathCertificate:report'" :data-testid="'cert-report-' + row.certNo" :icon="Promotion" size="small"
                         type="warning" @click.stop="onReport(row)">上报
              </el-button>
              <el-button v-if="Number(row.certStatus) !== CS.VOID" v-perm="'ipd:deathCertificate:edit'" :data-testid="'cert-void-' + row.certNo"
                         plain size="small" type="danger" @click.stop="onVoidCert(row)">作废
              </el-button>
              <el-button v-if="Number(row.certStatus) === CS.VOID" v-perm="'ipd:deathCertificate:edit'" :data-testid="'cert-reissue-' + row.certNo"
                         size="small" @click.stop="onReissue(row)">重开
              </el-button>
              <el-button :data-testid="'cert-detail-' + row.certNo" size="small" @click.stop="showCertDetail(row)">
                详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="cq.pageNum" v-model:page-size="cq.pageSize" :page-sizes="PAGE_SIZES"
                       :total="cTotal" class="mt-3" data-testid="cert-pagination"
                       layout="total, sizes, prev, pager, next"
                       @current-change="loadCerts" @size-change="cq.pageNum = 1; loadCerts()"/>
      </el-tab-pane>

      <!-- 页签 3：死亡登记 -->
      <el-tab-pane label="死亡登记" name="register">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <el-input v-model="rq.keyword" clearable data-testid="reg-keyword" placeholder="登记号/死者姓名"
                    style="width:200px" @keyup.enter="rq.pageNum = 1; loadRegisters()"/>
          <el-select v-model="rq.registerStatus" clearable data-testid="reg-filter-status" placeholder="登记状态"
                     style="width:130px">
            <el-option v-for="d in dict.registerStatus" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
          <el-select v-model="rq.deathType" :fit-input-width="false" clearable data-testid="reg-filter-type"
                     placeholder="死亡类型" style="width:190px">
            <el-option v-for="d in dict.deathType" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
          <el-select v-model="rq.policeFlag" clearable data-testid="reg-filter-police" placeholder="是否报公安"
                     style="width:130px">
            <el-option :value="1" label="已报"/>
            <el-option :value="0" label="未报"/>
          </el-select>
          <el-select v-model="rq.disputeFlag" clearable data-testid="reg-filter-dispute" placeholder="有无纠纷"
                     style="width:120px">
            <el-option :value="1" label="有纠纷"/>
            <el-option :value="0" label="无纠纷"/>
          </el-select>
          <el-date-picker v-model="rq.startDate" data-testid="reg-start" placeholder="死亡起" style="width:140px"
                          type="date" value-format="YYYY-MM-DD"/>
          <el-date-picker v-model="rq.endDate" data-testid="reg-end" placeholder="死亡止" style="width:140px"
                          type="date" value-format="YYYY-MM-DD"/>
          <el-button :icon="Search" data-testid="reg-search" type="primary" @click="rq.pageNum = 1; loadRegisters()">
            查询
          </el-button>
          <el-button :icon="Refresh" data-testid="reg-reset" @click="resetRegister">重置</el-button>
          <div class="flex-1"/>
          <el-button v-perm="'ipd:deathRegister:add'" :icon="Plus" data-testid="reg-create" type="primary"
                     @click="onOpenRegisterCreate(null)">新建登记
          </el-button>
        </div>
        <el-table v-loading="rLoading" :data="rRows" border data-testid="reg-table" size="small" stripe>
          <el-table-column label="登记号" min-width="150" prop="registerNo"/>
          <el-table-column label="死者" min-width="100" prop="patientName"/>
          <el-table-column label="死亡时间" min-width="150">
            <template #default="{ row }">{{ fmt(row.deathTime) }}</template>
          </el-table-column>
          <el-table-column label="死亡类型" min-width="170">
            <template #default="{ row }">{{ deathTypeText(row.deathType) }}</template>
          </el-table-column>
          <el-table-column label="公安报案" min-width="160">
            <template #default="{ row }">
              <el-tag v-if="Number(row.policeFlag) === 1" size="small" type="success">已报</el-tag>
              <el-tag v-else size="small" type="danger">未报</el-tag>
              <span v-if="row.policeCaseNo" class="ml-1 text-xs">{{ row.policeCaseNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="尸体处理" min-width="140">
            <template #default="{ row }">{{ disposalText(row.bodyDisposal) }} {{ row.bodyUnit || '' }}</template>
          </el-table-column>
          <el-table-column label="家属领取联次" min-width="180">
            <template #default="{ row }">{{ copiesText(row.receivedCopies) }}</template>
          </el-table-column>
          <el-table-column label="纠纷" width="80">
            <template #default="{ row }"><span :class="Number(row.disputeFlag) === 1 ? 'text-red-600' : ''">{{
                Number(row.disputeFlag) === 1 ? '有' : '无'
              }}</span></template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="registerTag(Number(row.registerStatus))">{{
                  registerStatusText(row.registerStatus)
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="关联证明" min-width="150">
            <template #default="{ row }">{{ row.certNo || '未开证' }}</template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="230">
            <template #default="{ row }">
              <el-button v-if="Number(row.registerStatus) === RS.DRAFT" v-perm="'ipd:deathRegister:add'" :data-testid="'reg-edit-' + row.registerNo"
                         size="small" @click.stop="openRegisterEdit(row)">修改
              </el-button>
              <el-button v-if="Number(row.registerStatus) === RS.DRAFT" v-perm="'ipd:deathRegister:edit'" :data-testid="'reg-confirm-' + row.registerNo"
                         size="small" type="success"
                         @click.stop="onConfirmRegister(row)">确认登记
              </el-button>
              <el-button v-if="Number(row.registerStatus) !== RS.VOID" v-perm="'ipd:deathRegister:edit'" :data-testid="'reg-void-' + row.registerNo"
                         plain size="small" type="danger"
                         @click.stop="onVoidRegister(row)">作废
              </el-button>
              <el-button :data-testid="'reg-detail-' + row.registerNo" size="small"
                         @click.stop="showRegisterDetail(row)">详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="rq.pageNum" v-model:page-size="rq.pageSize" :page-sizes="PAGE_SIZES"
                       :total="rTotal" class="mt-3" data-testid="reg-pagination"
                       layout="total, sizes, prev, pager, next"
                       @current-change="loadRegisters" @size-change="rq.pageNum = 1; loadRegisters()"/>
      </el-tab-pane>

      <!-- 页签 4：上报台账 -->
      <el-tab-pane label="上报台账" name="report">
        <div class="mb-3 flex flex-wrap items-center gap-2">
          <el-input v-model="mq.keyword" clearable data-testid="rep-keyword" placeholder="证明编号/死者/根本死因"
                    style="width:220px" @keyup.enter="mq.pageNum = 1; loadReport()"/>
          <el-select v-model="mq.reportStatus" clearable data-testid="rep-filter-status" placeholder="上报状态"
                     style="width:130px" @change="onReportOverdue">
            <el-option v-for="d in dict.reportStatus" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
          <el-checkbox v-model="mq.overdue" :false-value="0" :true-value="1" data-testid="rep-filter-overdue"
                       @change="onReportOverdue">只看逾期
          </el-checkbox>
          <el-button :icon="Search" data-testid="rep-search" type="primary" @click="mq.pageNum = 1; loadReport()">查询
          </el-button>
          <div class="flex-1"/>
          <el-button v-perm="'ipd:deathCertificate:report'" :icon="Warning" :loading="notifying" data-testid="rep-notify"
                     type="warning" @click="onNotify">
            逾期催报（每日一次，幂等）
          </el-button>
        </div>
        <el-table v-loading="mLoading" :data="mRows" border data-testid="rep-table" size="small" stripe>
          <el-table-column label="证明编号" min-width="160" prop="certNo"/>
          <el-table-column label="死者" min-width="100" prop="patientName"/>
          <el-table-column label="根本死因" min-width="170">
            <template #default="{ row }">{{ row.underlyingIcdCode || '未编码' }} {{
                row.underlyingIcdName || ''
              }}
            </template>
          </el-table-column>
          <el-table-column label="签发时间" min-width="150">
            <template #default="{ row }">{{ fmt(row.issueTime) }}</template>
          </el-table-column>
          <el-table-column label="上报时限" min-width="160">
            <template #default="{ row }">
              <span :class="row.overdue ? 'text-red-600' : ''">{{ fmt(row.reportDeadline) }}</span>
              <span v-if="row.overdue"
                    class="ml-1 text-xs text-red-600">逾期 {{ Math.max(0, -Math.trunc(Number(row.remainHours) / 24)) }} 天</span>
            </template>
          </el-table-column>
          <el-table-column label="上报状态" width="100">
            <template #default="{ row }">
              <el-tag :type="reportTag(Number(row.reportStatus))">{{ reportStatusText(row.reportStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上报时间/回执号" min-width="200">
            <template #default="{ row }">{{ fmt(row.reportTime) }} {{ row.reportNo || '' }}</template>
          </el-table-column>
          <el-table-column label="失败原因" min-width="160" show-overflow-tooltip>
            <template #default="{ row }"><span class="text-red-500">{{ row.reportError || '—' }}</span></template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="180">
            <template #default="{ row }">
              <el-button v-if="Number(row.certStatus) === CS.ISSUED && Number(row.reportStatus) !== 2"
                         v-perm="'ipd:deathCertificate:report'" :data-testid="'rep-report-' + row.certNo" size="small"
                         type="warning" @click.stop="onReport(row)">上报
              </el-button>
              <el-button :data-testid="'rep-detail-' + row.certNo" size="small" @click.stop="showCertDetail(row)">
                报文详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination v-model:current-page="mq.pageNum" v-model:page-size="mq.pageSize" :page-sizes="PAGE_SIZES"
                       :total="mTotal" class="mt-3" data-testid="rep-pagination"
                       layout="total, sizes, prev, pager, next"
                       @current-change="loadReport" @size-change="mq.pageNum = 1; loadReport()"/>
      </el-tab-pane>
    </el-tabs>

    <!-- 证明填写 / 修改 -->
    <el-dialog v-model="cfVisible" :title="cfTitle" data-testid="cert-form-dialog" top="6vh" width="920px">
      <div class="space-y-4">
        <div class="rounded bg-slate-50 p-3 text-sm" data-testid="cert-form-snapshot">
          <span class="text-slate-500">死者（一般项目按住院快照，不由前端提交）：</span>
          <b>{{ cfBase.patientName || '—' }}</b>
          <span class="ml-2">{{ patientGenderText(cfBase.gender) }} · {{
              cfBase.age ?? '—'
            }}岁 · 民族 {{ cfBase.nation || '—' }} · {{ cfBase.occupation || '—' }}</span>
          <span class="ml-2">住院号 {{ cfBase.admissionNo || '—' }}</span>
          <span class="ml-2">身份证 {{ cfBase.idCard || '—' }}</span>
          <div v-if="cfBase.dischargeTime" class="mt-1 text-xs text-slate-500">
            死亡离院时间：{{ fmt(cfBase.dischargeTime) }}（签发时证明的死亡时间必须与它同一时点）
          </div>
          <div v-else-if="cfBase.deathDischarged !== undefined" :class="cfBase.deathDischarged ? 'text-slate-500' : 'text-amber-600'"
               class="mt-1 text-xs" data-testid="cf-discharge-hint">
            {{
              cfBase.deathDischarged ? '该住院已办死亡离院，可签发' : '该住院尚未办「死亡」离院 —— 填表可以先做，但签发前必须先去出院办理'
            }}
          </div>
        </div>

        <div class="grid grid-cols-3 gap-3">
          <div>
            <div class="mb-1 text-xs text-slate-500">死亡时间 *</div>
            <el-date-picker v-model="cf.deathTime" data-testid="cf-death-time" placeholder="精确到分"
                            style="width:100%" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">死亡地点 *</div>
            <el-select v-model="cf.deathPlace" :fit-input-width="false" data-testid="cf-death-place" style="width:100%">
              <el-option v-for="d in dict.place" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </div>
          <div v-if="cf.deathPlace === PLACE_IN_HOSPITAL">
            <div class="mb-1 text-xs text-slate-500">死亡科室 *</div>
            <el-select v-model="cf.deathDeptId" :fit-input-width="false" data-testid="cf-death-dept" filterable
                       placeholder="死亡所在科室" style="width:100%">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">是否尸检</div>
            <el-select v-model="cf.autopsyFlag" data-testid="cf-autopsy-flag" style="width:100%">
              <el-option :value="0" label="否"/>
              <el-option :value="1" label="是"/>
            </el-select>
          </div>
          <div class="col-span-2">
            <div class="mb-1 text-xs text-slate-500">尸检结果（已做尸检时填写）</div>
            <el-input v-model="cf.autopsyResult" data-testid="cf-autopsy-result"/>
          </div>
          <div class="col-span-3">
            <div class="mb-1 text-xs text-slate-500">死亡诊断（致死的主要疾病诊断）*</div>
            <el-input v-model="cf.clinicalDiagnosis" :rows="2" data-testid="cf-clinical-diagnosis" type="textarea"/>
          </div>
          <div class="col-span-3">
            <div class="mb-1 text-xs text-slate-500">生前主要疾病最高诊断单位 / 既往史</div>
            <el-input v-model="cf.pastHistory" data-testid="cf-past-history"/>
          </div>
        </div>

        <div>
          <div class="mb-1 flex items-center justify-between">
            <span
                class="text-sm font-medium text-slate-600">Ⅰ部分 死因链（直接死因 → 中间原因 → 根本死因，链尾即根本死因）</span>
            <span class="text-xs text-slate-400">签发前每行 ICD-10 必须补全</span>
          </div>
          <el-table :data="cf.chain" border data-testid="cf-chain-table" size="small">
            <el-table-column label="顺序" width="70">
              <template #default="{ row }">（{{ row.slot.toUpperCase() }}）</template>
            </el-table-column>
            <el-table-column label="导致死亡的疾病或情况">
              <template #default="{ row }">
                <el-input v-model="row.icdName" data-testid="cf-chain-name" placeholder="疾病名称"/>
              </template>
            </el-table-column>
            <el-table-column label="ICD-10 编码" width="160">
              <template #default="{ row }">
                <el-input v-model="row.icdCode" data-testid="cf-chain-code" placeholder="如 I21.0"/>
              </template>
            </el-table-column>
            <el-table-column label="发病至死亡间隔" width="170">
              <template #default="{ row }">
                <el-input v-model="row.intervalText" placeholder="如 30分钟 / 10年"/>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-1 text-xs text-slate-500">根本死因（服务端按链尾回写并校验）：<b
              data-testid="cf-underlying">{{ underlyingView }}</b></div>
        </div>

        <div>
          <div class="mb-1 flex items-center justify-between">
            <span class="text-sm font-medium text-slate-600">Ⅱ部分 其他疾病（与死亡因果链无关但需登记）</span>
            <el-button :icon="Plus" data-testid="cf-add-other" size="small" @click="addOtherCause">加一行</el-button>
          </div>
          <el-table v-if="cf.other.length" :data="cf.other" border data-testid="cf-other-table" size="small">
            <el-table-column label="疾病名称">
              <template #default="{ row }">
                <el-input v-model="row.icdName"/>
              </template>
            </el-table-column>
            <el-table-column label="ICD-10" width="160">
              <template #default="{ row }">
                <el-input v-model="row.icdCode"/>
              </template>
            </el-table-column>
            <el-table-column label="间隔" width="160">
              <template #default="{ row }">
                <el-input v-model="row.intervalText"/>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80">
              <template #default="{ $index }">
                <el-button :data-testid="'cf-del-other-' + $index" :icon="Delete" plain size="small" type="danger"
                           @click="delOtherCause($index)"/>
              </template>
            </el-table-column>
          </el-table>
          <div v-else class="text-xs text-slate-400">无</div>
        </div>

        <div class="grid grid-cols-4 gap-3">
          <div>
            <div class="mb-1 text-xs text-slate-500">近亲属姓名</div>
            <el-input v-model="cf.relativeName" data-testid="cf-relative-name"/>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">与死者关系</div>
            <el-input v-model="cf.relativeRelation"/>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">联系电话</div>
            <el-input v-model="cf.relativePhone" data-testid="cf-relative-phone"/>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">填表医师（法定签名位）</div>
            <el-input v-model="cf.physicianName" data-testid="cf-physician"/>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">填表日期</div>
            <el-date-picker v-model="cf.fillTime" style="width:100%" type="datetime"
                            value-format="YYYY-MM-DD HH:mm:ss"/>
          </div>
          <div class="col-span-3">
            <div class="mb-1 text-xs text-slate-500">备注</div>
            <el-input v-model="cf.remark"/>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button data-testid="cf-cancel" @click="cfVisible = false">取消</el-button>
        <el-button v-perm="'ipd:deathCertificate:add'" :loading="cfSaving" data-testid="cf-save" type="primary"
                   @click="saveCert">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 登记填写 / 修改 -->
    <el-dialog v-model="rfVisible" :title="rfTitle" data-testid="reg-form-dialog" top="6vh" width="880px">
      <div class="space-y-4">
        <div class="grid grid-cols-3 gap-3">
          <div class="col-span-2">
            <div class="mb-1 text-xs text-slate-500">
              死亡出院的住院（只列已办「死亡」离院者，姓名/死亡时间/科室由服务端按住院重查）*
            </div>
            <el-select v-model="rf.admissionId" :disabled="!!rf.id" :fit-input-width="false" :loading="candLoading"
                       :remote-method="searchCandidates"
                       data-testid="rf-admission" filterable placeholder="搜索住院号或死者姓名"
                       remote style="width:100%">
              <el-option v-for="c in candidates" :key="c.admissionId" :label="`${c.admissionNo} ${c.patientName} ${fmt(c.deathTime)}`"
                         :value="c.admissionId"/>
            </el-select>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">关联死亡证明</div>
            <div class="pt-1 text-sm">{{ rfBase.certNo || '尚未开证（可先登记后补证）' }}</div>
          </div>
        </div>
        <div v-if="rfBase.patientName" class="rounded bg-slate-50 p-3 text-xs text-slate-600"
             data-testid="reg-form-snapshot">
          {{ rfBase.patientName }} · 死亡时间 {{ fmt(rfBase.deathTime) }} · {{ rfBase.deathDeptName || '—' }} 床号
          {{ rfBase.deathBedNo || '—' }}
        </div>

        <div class="grid grid-cols-3 gap-3">
          <div>
            <div class="mb-1 text-xs text-slate-500">死亡类型 *（非疾病/不明必须先报公安）</div>
            <el-select v-model="rf.deathType" :fit-input-width="false" data-testid="rf-death-type" style="width:100%">
              <el-option v-for="d in dict.deathType" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">是否已报公安</div>
            <el-select v-model="rf.policeFlag" data-testid="rf-police-flag" style="width:100%">
              <el-option :value="1" label="已报"/>
              <el-option :value="0" label="未报"/>
            </el-select>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">是否经法医出具/核实</div>
            <el-select v-model="rf.forensicFlag" data-testid="rf-forensic-flag" style="width:100%">
              <el-option :value="1" label="是"/>
              <el-option :value="0" label="否"/>
            </el-select>
          </div>
          <template v-if="rf.policeFlag === 1">
            <div>
              <div class="mb-1 text-xs text-slate-500">公安机关</div>
              <el-input v-model="rf.policeOrg" data-testid="rf-police-org"/>
            </div>
            <div>
              <div class="mb-1 text-xs text-slate-500">报案/案件编号</div>
              <el-input v-model="rf.policeCaseNo" data-testid="rf-police-case"/>
            </div>
            <div>
              <div class="mb-1 text-xs text-slate-500">报案时间 *</div>
              <el-date-picker v-model="rf.policeReportTime" data-testid="rf-police-time" style="width:100%"
                              type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
            </div>
          </template>
        </div>

        <div class="grid grid-cols-3 gap-3">
          <div>
            <div class="mb-1 text-xs text-slate-500">尸体处理方式（确认登记前必填）</div>
            <el-select v-model="rf.bodyDisposal" :fit-input-width="false" clearable data-testid="rf-body-disposal"
                       placeholder="遗体交给谁" style="width:100%">
              <el-option v-for="d in dict.disposal" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">接运/处理单位</div>
            <el-input v-model="rf.bodyUnit" data-testid="rf-body-unit"/>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">遗体移出时间</div>
            <el-date-picker v-model="rf.bodyTransportTime" style="width:100%" type="datetime"
                            value-format="YYYY-MM-DD HH:mm:ss"/>
          </div>
        </div>

        <div class="grid grid-cols-4 gap-3">
          <div>
            <div class="mb-1 text-xs text-slate-500">家属姓名</div>
            <el-input v-model="rf.relativeName"/>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">与死者关系</div>
            <el-input v-model="rf.relativeRelation"/>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">家属电话</div>
            <el-input v-model="rf.relativePhone" data-testid="rf-relative-phone"/>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">家属已领取联次</div>
            <el-select v-model="rf.receivedCopies" :fit-input-width="false" data-testid="rf-copies" multiple
                       style="width:100%">
              <el-option v-for="d in dict.copies" :key="d.dictValue" :label="d.dictLabel" :value="String(d.dictValue)"/>
            </el-select>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">领取时间</div>
            <el-date-picker v-model="rf.receiveTime" style="width:100%" type="datetime"
                            value-format="YYYY-MM-DD HH:mm:ss"/>
          </div>
          <div>
            <div class="mb-1 text-xs text-slate-500">是否存在纠纷</div>
            <el-select v-model="rf.disputeFlag" data-testid="rf-dispute-flag" style="width:100%">
              <el-option :value="0" label="无"/>
              <el-option :value="1" label="有"/>
            </el-select>
          </div>
          <div class="col-span-2">
            <div class="mb-1 text-xs text-slate-500">纠纷情况</div>
            <el-input v-model="rf.disputeDesc" data-testid="rf-dispute-desc"/>
          </div>
          <div class="col-span-4">
            <div class="mb-1 text-xs text-slate-500">备注</div>
            <el-input v-model="rf.remark"/>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button data-testid="rf-cancel" @click="rfVisible = false">取消</el-button>
        <el-button v-perm="'ipd:deathRegister:add'" :loading="rfSaving" data-testid="rf-save" type="primary"
                   @click="saveRegister">保存
        </el-button>
      </template>
    </el-dialog>

    <!-- 证明详情（只读） -->
    <el-drawer v-model="dvVisible" data-testid="cert-detail-drawer" size="620px" title="死亡证明详情（只读）">
      <el-form :disabled="true" data-testid="cert-detail-body" label-width="120px">
        <el-form-item label="证明编号"><span data-testid="cd-cert-no">{{ dv.certNo }}</span></el-form-item>
        <el-form-item label="状态">{{ certStatusText(dv.certStatus) }} · 上报 {{
            reportStatusText(dv.reportStatus)
          }}
        </el-form-item>
        <el-form-item label="住院号">{{ dv.admissionNo }}（{{ dv.patientName }} / {{ patientGenderText(dv.gender) }} /
          {{ dv.age ?? '—' }}岁）
        </el-form-item>
        <el-form-item label="一般项目">{{ dv.nation || '—' }} · {{ dv.occupation || '—' }} · 出生
          {{ fmtDay(dv.birthDate) }} · 身份证 {{ dv.idCard || '—' }}
        </el-form-item>
        <el-form-item label="死亡时间">{{ fmt(dv.deathTime) }}</el-form-item>
        <el-form-item label="死亡地点">{{ placeText(dv.deathPlace) }} {{ dv.deathDeptName || '' }}
          {{ dv.deathWardName || '' }} 床号 {{ dv.deathBedNo || '—' }}
        </el-form-item>
        <el-form-item label="死亡诊断">{{ dv.clinicalDiagnosis }}</el-form-item>
        <el-form-item label="根本死因">{{ dv.underlyingIcdCode }} {{ dv.underlyingIcdName }}</el-form-item>
        <el-form-item label="Ⅰ部分 死因链">
          <div class="w-full text-sm">
            <div v-for="x in dvChain" :key="x.id" data-testid="cd-chain-row">
              （{{ String.fromCharCode(65 + Number(x.seqNo) - 1) }}）{{ x.icdName }} {{ x.icdCode }} ·
              {{ x.intervalText || '间隔未填' }}
            </div>
            <div v-if="!dvChain.length" class="text-slate-400">未填写</div>
          </div>
        </el-form-item>
        <el-form-item label="Ⅱ部分 其他疾病">
          <div class="w-full text-sm">
            <div v-for="x in dvOther" :key="x.id">{{ x.icdName }} {{ x.icdCode }}</div>
            <div v-if="!dvOther.length" class="text-slate-400">无</div>
          </div>
        </el-form-item>
        <el-form-item label="近亲属">{{ dv.relativeName || '—' }} {{ dv.relativeRelation || '' }}
          {{ dv.relativePhone || '' }}
        </el-form-item>
        <el-form-item label="填表/审核">{{ dv.physicianName }} {{ fmt(dv.fillTime) }} ／ {{ dv.reviewerName || '—' }}
          {{ fmt(dv.reviewTime) }}
        </el-form-item>
        <el-form-item v-if="dv.reviewOpinion" label="审核意见">{{ dv.reviewOpinion }}</el-form-item>
        <el-form-item label="签发/打印">{{ fmt(dv.issueTime) }} ／ {{ dv.printCount ?? 0 }} 次（{{
            dv.printerName || '—'
          }}）
        </el-form-item>
        <el-form-item label="上报时限">{{ fmt(dv.reportDeadline) }} · 上报于 {{ fmt(dv.reportTime) }}</el-form-item>
        <el-form-item v-if="dv.reportNo" label="回执号">{{ dv.reportNo }}</el-form-item>
        <el-form-item v-if="dv.voidReason" label="作废原因">{{ dv.voidReason }}（{{ dv.voidBy }} {{
            fmt(dv.voidTime)
          }}）
        </el-form-item>
        <el-form-item v-if="dv.reissueCertNo" label="重开为">{{ dv.reissueCertNo }}</el-form-item>
        <el-form-item label="死亡出院">{{ dv.deathDischarged ? '已办理' : '未办理（未办理不能签发）' }}</el-form-item>
        <el-form-item v-if="dv.registerNo" label="关联登记">{{ dv.registerNo }}（{{
            registerStatusText(dv.registerStatus)
          }}）
        </el-form-item>
        <el-form-item v-if="dv.reportPayload" label="上报报文">
          <el-input :model-value="dv.reportPayload" :rows="12" data-testid="cd-payload" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="cd-close" @click="dvVisible = false">关闭</el-button>
      </template>
    </el-drawer>

    <!-- 登记详情（只读） -->
    <el-drawer v-model="rdVisible" data-testid="reg-detail-drawer" size="560px" title="死亡登记详情（只读）">
      <el-form :disabled="true" data-testid="reg-detail-body" label-width="120px">
        <el-form-item label="登记号">{{ rd.registerNo }}</el-form-item>
        <el-form-item label="状态">{{ registerStatusText(rd.registerStatus) }}</el-form-item>
        <el-form-item label="死者">{{ rd.patientName }} · 住院号 {{ rd.admissionNo }} · 死亡 {{
            fmt(rd.deathTime)
          }}
        </el-form-item>
        <el-form-item label="死亡科室/床位">{{ rd.deathDeptName || '—' }} / {{ rd.deathBedNo || '—' }}</el-form-item>
        <el-form-item label="关联证明">{{ rd.certNo || '未开证' }}（{{ certStatusText(rd.certStatus) }}）</el-form-item>
        <el-form-item label="死亡类型">{{ deathTypeText(rd.deathType) }}</el-form-item>
        <el-form-item label="公安报案">{{
            Number(rd.policeFlag) === 1 ? `已报：${rd.policeOrg || ''} ${rd.policeCaseNo || ''} ${fmt(rd.policeReportTime)}` : '未报'
          }}
        </el-form-item>
        <el-form-item label="法医出具">{{ Number(rd.forensicFlag) === 1 ? '是' : '否' }}</el-form-item>
        <el-form-item label="尸体处理">{{ disposalText(rd.bodyDisposal) }} · {{ rd.bodyUnit || '—' }} · 移出
          {{ fmt(rd.bodyTransportTime) }}
        </el-form-item>
        <el-form-item label="家属">{{ rd.relativeName || '—' }} {{ rd.relativeRelation || '' }}
          {{ rd.relativePhone || '' }}
        </el-form-item>
        <el-form-item label="领取联次">{{ copiesText(rd.receivedCopies) }} · {{ fmt(rd.receiveTime) }}</el-form-item>
        <el-form-item label="纠纷">{{
            Number(rd.disputeFlag) === 1 ? `有：${rd.disputeDesc || ''}` : '无'
          }}
        </el-form-item>
        <el-form-item label="登记人/时间">{{ rd.registrarName || '—' }} · {{ fmt(rd.registerTime) }}</el-form-item>
        <el-form-item v-if="rd.voidReason" label="作废原因">{{ rd.voidReason }}（{{ fmt(rd.voidTime) }}）</el-form-item>
        <el-form-item label="备注">{{ rd.remark || '—' }}</el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="rd-close" @click="rdVisible = false">关闭</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
/**
 * 死亡证明与死亡登记（菜单 318 / 路由 /death-certificate，后端 his-patient /patient/death）
 *
 * 四个页签对应四件事，全部数据驱动、无一处写死：
 *   待开证榜 = 欠账榜（已办死亡离院却没有有效证明，不拦出院但一直挂着）；
 *   证明台账 = 填写→审核→签发→打印→作废重开（法定文书，开具后禁改）；
 *   死亡登记 = 院内处置闭环（非疾病/死因不明必须先报公安才让确认）；
 *   上报台账 = 死因监测报文组装留痕 + 逾期催报。
 * 口径：时限/逾期/剩余小时全由后端算（overdue/remainHours），页面只渲染不自己比时间；
 * 状态文案走字典（his_death_* 八张，loadDictDataMap 内部已按 5 个一批切开）。
 */
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Plus, Printer, Promotion, Refresh, Search, Warning} from '@element-plus/icons-vue'
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache'
import {dictLabelText} from '@/lib/utils'
import {patientGenderText} from '@/lib/patientGender'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {getDepartmentSelectList} from '@/api/system'
import {
  certAudit,
  certIssue,
  certNotifyOverdue,
  certPrint,
  certReissue,
  certReport,
  certUpsert,
  certVoid,
  getCertAdmissionBase,
  getCertDetail,
  getCertListPage,
  getCertPendingListPage,
  getDeathStats,
  getRegisterAdmissions,
  getRegisterBase,
  getRegisterDetail,
  getRegisterListPage,
  registerConfirm,
  registerUpsert,
  registerVoid,
} from '@/api/death'

const CS = {DRAFT: 1, AUDITED: 2, ISSUED: 3, VOID: 4}
const RS = {DRAFT: 1, DONE: 2, VOID: 3}
const PLACE_IN_HOSPITAL = 1

const esc = (v) => String(v ?? '').replace(/[&<>"]/g, (c) => ({
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;'
}[c]))
const fmt = (t) => (t ? String(t).slice(0, 19) : '—')
const fmtDay = (t) => (t ? String(t).slice(0, 10) : '—')

// ---------------- 字典 ----------------
const dict = reactive({
  place: [], certStatus: [], reportStatus: [], causePart: [],
  deathType: [], disposal: [], copies: [], registerStatus: [],
})
const loadDicts = async () => {
  try {
    const map = await loadDictDataMap([
      DICT_TYPE.DEATH_PLACE, DICT_TYPE.DEATH_CERT_STATUS, DICT_TYPE.DEATH_REPORT_STATUS,
      DICT_TYPE.DEATH_CAUSE_PART, DICT_TYPE.DEATH_TYPE, DICT_TYPE.DEATH_BODY_DISPOSAL,
      DICT_TYPE.DEATH_CERT_COPY, DICT_TYPE.DEATH_REGISTER_STATUS,
    ].join(','))
    dict.place = map[DICT_TYPE.DEATH_PLACE] || []
    dict.certStatus = map[DICT_TYPE.DEATH_CERT_STATUS] || []
    dict.reportStatus = map[DICT_TYPE.DEATH_REPORT_STATUS] || []
    dict.causePart = map[DICT_TYPE.DEATH_CAUSE_PART] || []
    dict.deathType = map[DICT_TYPE.DEATH_TYPE] || []
    dict.disposal = map[DICT_TYPE.DEATH_BODY_DISPOSAL] || []
    dict.copies = map[DICT_TYPE.DEATH_CERT_COPY] || []
    dict.registerStatus = map[DICT_TYPE.DEATH_REGISTER_STATUS] || []
  } catch (e) {
    console.error('加载死亡相关字典失败', e)
  }
}
const certStatusText = (v) => dictLabelText(dict.certStatus, v)
const reportStatusText = (v) => dictLabelText(dict.reportStatus, v)
const placeText = (v) => dictLabelText(dict.place, v)
const deathTypeText = (v) => dictLabelText(dict.deathType, v)
const disposalText = (v) => dictLabelText(dict.disposal, v)
const registerStatusText = (v) => dictLabelText(dict.registerStatus, v)
const copiesText = (v) => (v ? String(v).split(',').map((x) => dictLabelText(dict.copies, x)).join('、') : '—')
const certTag = (s) => (s === CS.DRAFT ? 'warning' : s === CS.AUDITED ? 'primary' : s === CS.ISSUED ? 'success' : 'info')
const reportTag = (s) => (s === 2 ? 'success' : s === 3 ? 'danger' : 'info')
const registerTag = (s) => (s === RS.DRAFT ? 'warning' : s === RS.DONE ? 'success' : 'info')

// ---------------- 科室下拉（参照数据，不挂权限码） ----------------
const deptOptions = ref([])
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList()
    if (res.code === 200) deptOptions.value = res.data || []
  } catch (e) {
    console.error('加载科室下拉失败', e)
  }
}

// ---------------- 统计卡 ----------------
const stats = ref({})
const loadStats = async () => {
  try {
    const res = await getDeathStats()
    if (res.code === 200) stats.value = res.data || {}
  } catch (e) {
    console.error(e);
    ElMessage.error('加载统计失败')
  }
}
const statCards = computed(() => [
  {k: 'deathDischargeTotal', label: '死亡出院', cls: ''},
  {k: 'noCertCount', label: '待开证', cls: 'text-red-600'},
  {k: 'draftCount', label: '草稿', cls: 'text-amber-500'},
  {k: 'auditedCount', label: '已审核', cls: 'text-blue-500'},
  {k: 'issuedCount', label: '已开具', cls: 'text-green-600'},
  {k: 'voidCount', label: '已作废', cls: 'text-slate-400'},
  {k: 'unreportedCount', label: '待上报', cls: 'text-amber-600'},
  {k: 'reportedCount', label: '已上报', cls: 'text-green-600'},
  {k: 'reportFailedCount', label: '上报失败', cls: 'text-red-500'},
  {k: 'overdueCount', label: '逾期未报', cls: 'text-red-600'},
  {k: 'noRegisterCount', label: '待登记', cls: 'text-amber-600'},
  {k: 'nonDiseaseUnpolicedCount', label: '未报案已登记', cls: 'text-red-700'},
])

const tab = ref('pending')
const reloadAll = () => Promise.all([loadStats(), loadPending(), loadCerts(), loadRegisters(), loadReport()])

// ---------------- 页签 1：待开证榜 ----------------
const pq = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, keyword: '', startDate: null, endDate: null})
const pRows = ref([])
const pTotal = ref(0)
const pLoading = ref(false)
const loadPending = async () => {
  pLoading.value = true
  try {
    const res = await getCertPendingListPage({...pq})
    if (res.code === 200) {
      pRows.value = res.data?.records || [];
      pTotal.value = res.data?.total || 0
    } else ElMessage.error(res.message || '加载待开证榜失败')
  } catch (e) {
    console.error(e);
    ElMessage.error('加载待开证榜失败')
  } finally {
    pLoading.value = false
  }
}
const resetPending = () => {
  pq.keyword = '';
  pq.startDate = null;
  pq.endDate = null;
  pq.pageNum = 1;
  loadPending()
}

// ---------------- 页签 2：证明台账 ----------------
const cq = reactive({
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, keyword: '', certStatus: null, deathPlace: null,
  deathDeptId: null, startDate: null, endDate: null, overdue: 0,
})
const cRows = ref([])
const cTotal = ref(0)
const cLoading = ref(false)
const loadCerts = async () => {
  cLoading.value = true
  try {
    const res = await getCertListPage({...cq})
    if (res.code === 200) {
      cRows.value = res.data?.records || [];
      cTotal.value = res.data?.total || 0
    } else ElMessage.error(res.message || '加载证明台账失败')
  } catch (e) {
    console.error(e);
    ElMessage.error('加载证明台账失败')
  } finally {
    cLoading.value = false
  }
}
const resetCert = () => {
  Object.assign(cq, {
    keyword: '',
    certStatus: null,
    deathPlace: null,
    deathDeptId: null,
    startDate: null,
    endDate: null,
    overdue: 0,
    pageNum: 1
  })
  loadCerts()
}
const onCertOverdue = () => {
  cq.pageNum = 1;
  loadCerts()
}

// ---------------- 页签 3：死亡登记 ----------------
const rq = reactive({
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, keyword: '', registerStatus: null, deathType: null,
  policeFlag: null, disputeFlag: null, startDate: null, endDate: null,
})
const rRows = ref([])
const rTotal = ref(0)
const rLoading = ref(false)
const loadRegisters = async () => {
  rLoading.value = true
  try {
    const res = await getRegisterListPage({...rq})
    if (res.code === 200) {
      rRows.value = res.data?.records || [];
      rTotal.value = res.data?.total || 0
    } else ElMessage.error(res.message || '加载死亡登记失败')
  } catch (e) {
    console.error(e);
    ElMessage.error('加载死亡登记失败')
  } finally {
    rLoading.value = false
  }
}
const resetRegister = () => {
  Object.assign(rq, {
    keyword: '',
    registerStatus: null,
    deathType: null,
    policeFlag: null,
    disputeFlag: null,
    startDate: null,
    endDate: null,
    pageNum: 1
  })
  loadRegisters()
}

// ---------------- 页签 4：上报台账（只看已开具的证，口径与台账分开两套查询） ----------------
const mq = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  keyword: '',
  reportStatus: null,
  certStatus: CS.ISSUED,
  overdue: 0
})
const mRows = ref([])
const mTotal = ref(0)
const mLoading = ref(false)
const loadReport = async () => {
  mLoading.value = true
  try {
    const res = await getCertListPage({...mq})
    if (res.code === 200) {
      mRows.value = res.data?.records || [];
      mTotal.value = res.data?.total || 0
    } else ElMessage.error(res.message || '加载上报台账失败')
  } catch (e) {
    console.error(e);
    ElMessage.error('加载上报台账失败')
  } finally {
    mLoading.value = false
  }
}
const onReportOverdue = () => {
  mq.pageNum = 1;
  loadReport()
}

// ---------------- 证明表单 ----------------
const CHAIN_SLOTS = ['a', 'b', 'c', 'd']
const blankChain = () => CHAIN_SLOTS.map((t) => ({slot: t, icdCode: '', icdName: '', intervalText: ''}))
const cfVisible = ref(false)
const cfTitle = ref('填写死亡证明')
const cfSaving = ref(false)
const cfBase = ref({})
const cf = reactive({
  id: null, admissionId: null, certNo: '', deathTime: '', deathPlace: PLACE_IN_HOSPITAL,
  deathDeptId: null, clinicalDiagnosis: '', pastHistory: '', autopsyFlag: 0, autopsyResult: '',
  relativeName: '', relativeRelation: '', relativePhone: '', physicianName: '', fillTime: '',
  underlyingIcdCode: '', underlyingIcdName: '', remark: '',
  chain: blankChain(), other: [],
})
const resetCertForm = () => {
  Object.assign(cf, {
    id: null, admissionId: null, certNo: '', deathTime: '', deathPlace: PLACE_IN_HOSPITAL,
    deathDeptId: null, clinicalDiagnosis: '', pastHistory: '', autopsyFlag: 0, autopsyResult: '',
    relativeName: '', relativeRelation: '', relativePhone: '', physicianName: '', fillTime: '',
    underlyingIcdCode: '', underlyingIcdName: '', remark: '',
    chain: blankChain(), other: [],
  })
}
const filledChain = computed(() => cf.chain.filter((r) => (r.icdName || '').trim() || (r.icdCode || '').trim()))
/** 根本死因＝链尾（服务端同口径校验并回写，界面只显式不给了改的地方） */
const chainTail = computed(() => filledChain.value[filledChain.value.length - 1] || null)
const underlyingView = computed(() => {
  if (chainTail.value) return `${chainTail.value.icdCode || '待补编码'} ${chainTail.value.icdName || ''}`.trim()
  return cf.underlyingIcdCode ? `${cf.underlyingIcdCode} ${cf.underlyingIcdName || ''}`.trim() : '—'
})

const openCertFormByAdmission = async (admissionId) => {
  try {
    const res = await getCertAdmissionBase(admissionId)
    if (res.code !== 200) return ElMessage.error(res.message || '加载开证底稿失败')
    const snap = res.data || {}
    if (snap.hasActiveCert) {
      const hit = await getCertListPage({pageNum: 1, pageSize: 50, keyword: snap.admissionNo})
      const row = (hit.data?.records || []).find((r) => String(r.admissionId) === String(snap.admissionId) && Number(r.certStatus) !== CS.VOID)
      if (row) return openCertEdit(row.id)
    }
    resetCertForm()
    cf.admissionId = snap.admissionId
    // 死亡时间默认带出死亡离院时间：签发时两者必须同一时点，早一步填好省一次返工
    cf.deathTime = snap.dischargeTime || ''
    cf.deathDeptId = snap.deptId || null
    cf.clinicalDiagnosis = snap.diagnosis || ''
    cfBase.value = snap
    cfTitle.value = '填写死亡证明'
    cfVisible.value = true
  } catch (e) {
    ElMessage.error(String((e && e.message) || '加载开证底稿失败'))
  }
}
const openCertEdit = async (id) => {
  const res = await gate(getCertDetail(id), '加载证明失败')
  if (!res) return
  const d = res.data || {}
  resetCertForm()
  Object.assign(cf, {
    id: d.id,
    admissionId: d.admissionId,
    certNo: d.certNo,
    deathTime: d.deathTime || '',
    deathPlace: d.deathPlace ?? PLACE_IN_HOSPITAL,
    deathDeptId: d.deathDeptId || null,
    clinicalDiagnosis: d.clinicalDiagnosis || '',
    pastHistory: d.pastHistory || '',
    autopsyFlag: Number(d.autopsyFlag) === 1 ? 1 : 0,
    autopsyResult: d.autopsyResult || '',
    relativeName: d.relativeName || '',
    relativeRelation: d.relativeRelation || '',
    relativePhone: d.relativePhone || '',
    physicianName: d.physicianName || '',
    fillTime: d.fillTime || '',
    underlyingIcdCode: d.underlyingIcdCode || '',
    underlyingIcdName: d.underlyingIcdName || '',
    remark: d.remark || '',
  })
  const chain = (d.causes || []).filter((x) => Number(x.part) !== 2)
  const other = (d.causes || []).filter((x) => Number(x.part) === 2)
  cf.chain = blankChain().map((row, i) => ({
    ...row,
    icdCode: chain[i]?.icdCode || '',
    icdName: chain[i]?.icdName || '',
    intervalText: chain[i]?.intervalText || '',
  }))
  cf.other = other.map((x) => ({
    icdCode: x.icdCode || '',
    icdName: x.icdName || '',
    intervalText: x.intervalText || ''
  }))
  cfBase.value = d
  cfTitle.value = '修改死亡证明'
  cfVisible.value = true
}
const addOtherCause = () => cf.other.push({icdCode: '', icdName: '', intervalText: ''})
const delOtherCause = (i) => cf.other.splice(i, 1)

const buildCauses = () => {
  const isBlank = (r) => !(r.icdName || '').trim() && !(r.icdCode || '').trim()
  const pick = (rows, part) => rows.filter((r) => !isBlank(r))
      .map((r, i) => ({part, seqNo: i + 1, icdCode: r.icdCode, icdName: r.icdName, intervalText: r.intervalText}))
  return [...pick(cf.chain, 1), ...pick(cf.other, 2)]
}
const saveCert = async () => {
  if (!cf.admissionId) return ElMessage.warning('缺少住院记录，请从待开证榜或台账进入')
  if (!cf.deathTime) return ElMessage.warning('死亡时间不能为空')
  if (!cf.deathPlace) return ElMessage.warning('死亡地点不能为空')
  if (cf.deathPlace === PLACE_IN_HOSPITAL && !cf.deathDeptId) return ElMessage.warning('医院内死亡必须选择死亡科室')
  if (!(cf.clinicalDiagnosis || '').trim()) return ElMessage.warning('死亡诊断不能为空')
  if (!filledChain.value.length) return ElMessage.warning('死因链Ⅰ至少填写一行（直接死因→根本死因）')
  // 链不能空跳：a→b→c→d 是因果顺序，中间空一行会被后端按「行序不连续」拒掉
  const firstBlank = cf.chain.findIndex((r) => !(r.icdName || '').trim() && !(r.icdCode || '').trim())
  if (firstBlank >= 0 && cf.chain.slice(firstBlank + 1).some((r) => (r.icdName || '').trim() || (r.icdCode || '').trim())) {
    return ElMessage.warning(`死因链Ⅰ第 ${CHAIN_SLOTS[firstBlank].toUpperCase()} 行为空，链上不能空跳，请顺次填写`)
  }
  const tail = chainTail.value
  cfSaving.value = true
  try {
    const res = await certUpsert({
      id: cf.id, admissionId: cf.admissionId, deathTime: cf.deathTime, deathPlace: cf.deathPlace,
      deathDeptId: cf.deathPlace === PLACE_IN_HOSPITAL ? cf.deathDeptId : null,
      clinicalDiagnosis: cf.clinicalDiagnosis, pastHistory: cf.pastHistory,
      autopsyFlag: cf.autopsyFlag, autopsyResult: cf.autopsyResult,
      relativeName: cf.relativeName, relativeRelation: cf.relativeRelation, relativePhone: cf.relativePhone,
      physicianName: cf.physicianName || undefined, fillTime: cf.fillTime || undefined,
      underlyingIcdCode: tail?.icdCode || cf.underlyingIcdCode || undefined,
      underlyingIcdName: tail?.icdName || cf.underlyingIcdName || undefined,
      remark: cf.remark, causes: buildCauses(),
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '证明已保存')
      cfVisible.value = false
      await reloadAll()
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) {
    ElMessage.error(String((e && e.message) || '保存失败'))
  } finally {
    cfSaving.value = false
  }
}

// ---------------- 状态动作 ----------------
/**
 * request.js 对业务失败（code!==200）走的是 reject，而不是把 res 返回回来，
 * 所以 `if (res.code === 200) … else ElMessage.error(…)` 的 else 分支永远跑不到 ——
 * 现象是「点了按钮没反应」。这一页的卖点全是闸门（未办死亡离院不许签发、非疾病死亡未报公安不许确认、
 * 已上报冻结不许重报），被拦时必须把后端原文弹给用户，故所有调用一律走这里兜住。
 */
const gate = async (promise, fallback) => {
  try {
    const res = await promise
    if (res && res.code === 200) return res
    ElMessage.error(String((res && res.message) || fallback))
    return null
  } catch (e) {
    ElMessage.error(String((e && e.message) || fallback))
    return null
  }
}
const onAudit = async (row) => {
  const {value} = await ElMessageBox.prompt(
      `审核证明 ${row.certNo}（${row.patientName}）`, '审核意见（可选）',
      {
        inputPlaceholder: '审核意见',
        confirmButtonText: '通过',
        cancelButtonText: '取消'
      }).catch(() => ({value: undefined}))
  if (value === undefined) return
  if (await gate(certAudit({id: row.id, opinion: value}), '审核失败')) {
    ElMessage.success('已审核，可签发');
    await reloadAll()
  }
}
const onIssue = async (row) => {
  const ok = await ElMessageBox.confirm(
      `签发即对外出具法定凭证：${row.certNo}（${row.patientName}）。签发后内容不可修改，错证只能作废重开。确认签发？`,
      '签发确认', {confirmButtonText: '签发', cancelButtonText: '取消'}).catch(() => null)
  if (!ok) return
  if (await gate(certIssue({id: row.id}), '签发失败')) {
    ElMessage.success('已签发');
    await reloadAll()
  }
}
const onVoidCert = async (row) => {
  const {value} = await ElMessageBox.prompt(
      `作废证明 ${row.certNo}（${row.patientName}）：内容不再可改，重开时系统按原证带出新草稿`, '作废原因（必填）',
      {
        inputPlaceholder: '错在哪：如根本死因编码填错',
        confirmButtonText: '作废',
        cancelButtonText: '取消'
      }).catch(() => ({value: undefined}))
  if (value === undefined) return
  if (!(value || '').trim()) return ElMessage.warning('作废原因必填')
  if (await gate(certVoid({id: row.id, reason: value}), '作废失败')) {
    ElMessage.success('已作废');
    await reloadAll()
  }
}
const onReissue = async (row) => {
  const ok = await ElMessageBox.confirm(
      `按被作废的原证 ${row.certNo} 复制一张新草稿（orig_cert_id 指回原证，原证内容不动）。继续？`,
      '重开确认', {confirmButtonText: '重开', cancelButtonText: '取消'}).catch(() => null)
  if (!ok) return
  const res = await gate(certReissue(row.id), '重开失败')
  if (res) {
    ElMessage.success('已生成新草稿');
    await reloadAll();
    await openCertEdit(res.data)
  }
}
const onReport = async (row) => {
  const ok = await ElMessageBox.confirm(
      `上报 ${row.certNo}：系统组装死因监测标准报文并留痕（当前不对接区域平台，真实对接时这一步换成外发）。继续？`,
      '上报确认', {confirmButtonText: '上报', cancelButtonText: '取消'}).catch(() => null)
  if (!ok) return
  if (await gate(certReport(row.id), '上报失败')) {
    ElMessage.success('上报完成，报文已留痕');
    await reloadAll()
  }
}
const notifying = ref(false)
const onNotify = async () => {
  notifying.value = true
  try {
    const res = await certNotifyOverdue()
    if (res.code === 200) ElMessage.success(`催报完成，发送 ${res.data || 0} 条站内信`)
    else ElMessage.error(res.message || '催报失败')
    await loadStats()
  } catch (e) {
    ElMessage.error(String((e && e.message) || '催报失败'))
    await loadStats()
  } finally {
    notifying.value = false
  }
}

// ---------------- 四联打印 ----------------
const causeTableHtml = (causes) => {
  const chain = (causes || []).filter((x) => Number(x.part) !== 2)
  const other = (causes || []).filter((x) => Number(x.part) === 2)
  const chainHtml = CHAIN_SLOTS.map((slot, i) => {
    const r = chain[i]
    return `<tr><th>（${slot.toUpperCase()}）</th><td>${esc(r?.icdName || '')}</td><td>${esc(r?.icdCode || '')}</td><td>${esc(r?.intervalText || '')}</td></tr>`
  }).join('')
  const otherHtml = other.length
      ? other.map((r) => `<tr><td>${esc(r.icdName || '')}</td><td>${esc(r.icdCode || '')}</td><td>${esc(r.intervalText || '')}</td></tr>`).join('')
      : '<tr><td colspan="3">无</td></tr>'
  return `<h3>Ⅰ部分 死因链（直接死因→根本死因）</h3>
    <table><tr><th>顺序</th><th>导致死亡的疾病或情况</th><th>ICD-10</th><th>发病至死亡间隔</th></tr>${chainHtml}</table>
    <h3>Ⅱ部分 其他疾病（与死亡因果链无关）</h3>
    <table><tr><th>疾病名称</th><th>ICD-10</th><th>间隔</th></tr>${otherHtml}</table>`
}
const certBodyHtml = (d) => `
    <div class="row"><span>姓名：${esc(d.patientName)}</span><span>性别：${esc(patientGenderText(d.gender))}</span>
      <span>年龄：${esc(d.age)}岁</span><span>民族：${esc(d.nation)}</span></div>
    <div class="row"><span>身份证号：${esc(d.idCard)}</span><span>出生日期：${esc(fmtDay(d.birthDate))}</span></div>
    <div class="row"><span>职业：${esc(d.occupation)}</span><span>死亡时间：${esc(fmt(d.deathTime))}</span>
      <span>死亡地点：${esc(placeText(d.deathPlace))}</span></div>
    <div class="row"><span>死亡科室：${esc(d.deathDeptName || '—')}</span><span>病区/床号：${esc(d.deathWardName || '—')} / ${esc(d.deathBedNo || '—')}</span>
      <span>是否尸检：${Number(d.autopsyFlag) === 1 ? '是' : '否'}</span></div>
    <div class="row"><span>死亡诊断：${esc(d.clinicalDiagnosis)}</span></div>
    <div class="row"><span>既往主要疾病史：${esc(d.pastHistory || '—')}</span></div>
    ${causeTableHtml(d.causes)}
    <div class="row"><span>根本死因：${esc(d.underlyingIcdCode)} ${esc(d.underlyingIcdName)}</span></div>
    <div class="row"><span>近亲属姓名：${esc(d.relativeName || '—')}</span><span>与死者关系：${esc(d.relativeRelation || '—')}</span>
      <span>联系电话：${esc(d.relativePhone || '—')}</span></div>
    <div class="sign">
      <span>填表医师（签名）：${esc(d.physicianName)}</span>
      <span>填表日期：${esc(fmt(d.fillTime))}</span>
      <span>审核：${esc(d.reviewerName || '—')} ${esc(fmt(d.reviewTime))}</span>
    </div>
    <div class="sign"><span>签发：${esc(fmt(d.issueTime))}</span><span>（医疗卫生机构盖章）</span>
      <span>第 ${esc(d.printCount || 1)} 次打印 · ${esc(d.printerName || '')}</span></div>`
const onPrint = async (row) => {
  const got = await gate(getCertDetail(row.id), '加载证明失败')
  if (!got) return
  const d = got.data || {}
  const ack = await gate(certPrint({id: row.id}), '打印计数失败')
  if (!ack) return
  const copies = dict.copies.length ? dict.copies : [{dictValue: '1', dictLabel: '记录联'}]
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>${esc(d.certNo)}</title><style>
      @page { size: A4; margin: 12mm; }
      body { font-family: "Microsoft YaHei", sans-serif; color: #111; margin: 0; }
      .copy { page-break-after: always; padding: 8px 4px; }
      .copy:last-child { page-break-after: auto; }
      h1 { font-size: 17px; text-align: center; margin: 0 0 2px; }
      .sub { text-align: center; font-size: 12px; color: #444; margin-bottom: 6px; }
      .联 { text-align: center; font-size: 12px; margin-bottom: 8px; }
      h3 { font-size: 12px; margin: 10px 0 4px; }
      table { width: 100%; border-collapse: collapse; }
      th, td { border: 1px solid #000; padding: 3px 6px; font-size: 12px; text-align: left; }
      th { background: #f2f2f2; white-space: nowrap; }
      .row { font-size: 12px; margin: 4px 0; display: flex; gap: 18px; flex-wrap: wrap; }
      .sign { margin-top: 18px; display: flex; justify-content: space-between; font-size: 12px; }
    </style></head><body>
    ${copies.map((c) => `<div class="copy"><h1>居民死亡医学证明（推断）书</h1>
      <div class="sub">编号：${esc(d.certNo)}　住院号：${esc(d.admissionNo)}</div>
      <div class="联">${esc(c.dictLabel)}（第 ${esc(c.dictValue)} 联）</div>${certBodyHtml(d)}</div>`).join('')}
    </body></html>`
  const win = window.open('', '_blank')
  if (!win) return ElMessage.error('浏览器拦截了新窗口，请允许弹出后重试')
  win.document.write(html)
  win.document.close()
  win.onload = () => win.print()
  await reloadAll()
}

// ---------------- 证明详情（只读） ----------------
const dvVisible = ref(false)
const dv = ref({})
const showCertDetail = async (row) => {
  const res = await gate(getCertDetail(row.id), '加载详情失败')
  if (res) {
    dv.value = res.data || {};
    dvVisible.value = true
  }
}
const dvChain = computed(() => (dv.value.causes || []).filter((x) => Number(x.part) !== 2))
const dvOther = computed(() => (dv.value.causes || []).filter((x) => Number(x.part) === 2))

// ---------------- 登记表单 ----------------
const rfVisible = ref(false)
const rfTitle = ref('死亡登记')
const rfSaving = ref(false)
const rfBase = ref({})
const candLoading = ref(false)
const candidates = ref([])
const rf = reactive({
  id: null, admissionId: null, certId: null, deathType: 1,
  policeFlag: 0, policeOrg: '', policeCaseNo: '', policeReportTime: '', forensicFlag: 0,
  bodyDisposal: null, bodyUnit: '', bodyTransportTime: '',
  relativeName: '', relativeRelation: '', relativePhone: '',
  receivedCopies: [], receiveTime: '', disputeFlag: 0, disputeDesc: '', remark: '',
})
const resetRegisterForm = () => {
  Object.assign(rf, {
    id: null, admissionId: null, certId: null, deathType: 1,
    policeFlag: 0, policeOrg: '', policeCaseNo: '', policeReportTime: '', forensicFlag: 0,
    bodyDisposal: null, bodyUnit: '', bodyTransportTime: '',
    relativeName: '', relativeRelation: '', relativePhone: '',
    receivedCopies: [], receiveTime: '', disputeFlag: 0, disputeDesc: '', remark: '',
  })
  rfBase.value = {}
  candidates.value = []
}
const searchCandidates = async (keyword) => {
  candLoading.value = true
  try {
    const res = await getRegisterAdmissions(keyword || '', 50)
    if (res.code === 200) candidates.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    candLoading.value = false
  }
}
const onOpenRegisterCreate = async (admissionId) => {
  resetRegisterForm()
  rfTitle.value = '死亡登记（新建）'
  await searchCandidates('')
  if (admissionId) {
    rf.admissionId = admissionId
    await onPickCandidate(admissionId)
  }
  rfVisible.value = true
}
const onPickCandidate = async (admissionId) => {
  if (!admissionId) return
  const res = await gate(getRegisterBase(admissionId), '加载登记底稿失败')
  if (!res) return
  const b = res.data || {}
  rfBase.value = b
  // 证明可由服务端兜底挂上（resolveCertId 认「该住院当前有效证明」），这里只是把默认值显式化
  rf.certId = b.certId || null
}
const openRegisterEdit = async (row) => {
  const res = await gate(getRegisterDetail(row.id), '加载登记失败')
  if (!res) return
  const d = res.data || {}
  resetRegisterForm()
  Object.assign(rf, {
    id: d.id,
    admissionId: d.admissionId,
    certId: d.certId || null,
    deathType: d.deathType ?? 1,
    policeFlag: Number(d.policeFlag) === 1 ? 1 : 0,
    policeOrg: d.policeOrg || '',
    policeCaseNo: d.policeCaseNo || '',
    policeReportTime: d.policeReportTime || '',
    forensicFlag: Number(d.forensicFlag) === 1 ? 1 : 0,
    bodyDisposal: d.bodyDisposal ?? null,
    bodyUnit: d.bodyUnit || '',
    bodyTransportTime: d.bodyTransportTime || '',
    relativeName: d.relativeName || '',
    relativeRelation: d.relativeRelation || '',
    relativePhone: d.relativePhone || '',
    receivedCopies: d.receivedCopies ? String(d.receivedCopies).split(',') : [],
    receiveTime: d.receiveTime || '',
    disputeFlag: Number(d.disputeFlag) === 1 ? 1 : 0,
    disputeDesc: d.disputeDesc || '',
    remark: d.remark || '',
  })
  rfBase.value = {
    admissionNo: d.admissionNo, patientName: d.patientName, deathTime: d.deathTime,
    deathDeptName: d.deathDeptName, deathBedNo: d.deathBedNo, certNo: d.certNo, certStatus: d.certStatus,
  }
  candidates.value = [{admissionId: d.admissionId, admissionNo: d.admissionNo, patientName: d.patientName}]
  rfTitle.value = '死亡登记（修改）'
  rfVisible.value = true
}
const saveRegister = async () => {
  if (!rf.admissionId) return ElMessage.warning('请选择已办死亡离院的住院')
  if (!rf.deathType) return ElMessage.warning('死亡类型不能为空')
  if (rf.policeFlag === 1 && !rf.policeReportTime) return ElMessage.warning('已报公安的必须填写报案时间')
  rfSaving.value = true
  try {
    const res = await registerUpsert({
      id: rf.id, admissionId: rf.admissionId, certId: rf.certId || undefined, deathType: rf.deathType,
      policeFlag: rf.policeFlag, policeOrg: rf.policeOrg, policeCaseNo: rf.policeCaseNo,
      policeReportTime: rf.policeFlag === 1 ? rf.policeReportTime : undefined,
      forensicFlag: rf.forensicFlag, bodyDisposal: rf.bodyDisposal, bodyUnit: rf.bodyUnit,
      bodyTransportTime: rf.bodyTransportTime || undefined,
      relativeName: rf.relativeName, relativeRelation: rf.relativeRelation, relativePhone: rf.relativePhone,
      receivedCopies: rf.receivedCopies.length ? rf.receivedCopies.join(',') : undefined,
      receiveTime: rf.receiveTime || undefined, disputeFlag: rf.disputeFlag,
      disputeDesc: rf.disputeDesc, remark: rf.remark,
    })
    if (res.code === 200) {
      ElMessage.success(res.message || '登记已保存');
      rfVisible.value = false;
      await reloadAll()
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) {
    ElMessage.error(String((e && e.message) || '保存失败'))
  } finally {
    rfSaving.value = false
  }
}
const onConfirmRegister = async (row) => {
  const ok = await ElMessageBox.confirm(
      `确认登记 ${row.registerNo}（${row.patientName}）：确认后内容不再可改，要改只能作废重登。`,
      '确认登记', {confirmButtonText: '确认', cancelButtonText: '取消'}).catch(() => null)
  if (!ok) return
  if (await gate(registerConfirm({id: row.id}), '登记失败')) {
    ElMessage.success('已登记');
    await reloadAll()
  }
}
const onVoidRegister = async (row) => {
  const {value} = await ElMessageBox.prompt(`作废登记 ${row.registerNo}（${row.patientName}）`, '作废原因（必填）',
      {
        inputPlaceholder: '如联次登记错误',
        confirmButtonText: '作废',
        cancelButtonText: '取消'
      }).catch(() => ({value: undefined}))
  if (value === undefined) return
  if (!(value || '').trim()) return ElMessage.warning('作废原因必填')
  if (await gate(registerVoid({id: row.id, reason: value}), '作废失败')) {
    ElMessage.success('已作废');
    await reloadAll()
  }
}
const rdVisible = ref(false)
const rd = ref({})
const showRegisterDetail = async (row) => {
  const res = await gate(getRegisterDetail(row.id), '加载详情失败')
  if (res) {
    rd.value = res.data || {};
    rdVisible.value = true
  }
}

/** 台账行的可编辑判定：草稿/已审核可改，已开具与已作废走状态动作 */
const certEditable = (row) => [CS.DRAFT, CS.AUDITED].includes(Number(row.certStatus))

onMounted(async () => {
  await Promise.all([loadDicts(), loadDepts()])
  await reloadAll()
})
</script>

<style scoped>
:deep(.el-table .cell) {
  font-size: 14px;
  color: #1f2937;
}

:deep(.el-table th .cell) {
  font-weight: 600;
  color: #334155;
}
</style>
