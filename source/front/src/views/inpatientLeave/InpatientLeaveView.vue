<template>
  <div data-testid="inpatient-leave-page">
    <!-- 统计卡 -->
    <div class="mb-3 grid grid-cols-12 gap-2">
      <el-card v-for="c in statCards" :key="c.k" class="!rounded-lg !py-1" shadow="never">
        <div class="text-center">
          <div :class="['text-xl font-semibold', c.cls]" :data-testid="'stat-' + c.k">{{ stats[c.k] ?? 0 }}</div>
          <div class="mt-0.5 text-[11px] text-slate-400">{{ c.label }}</div>
        </div>
      </el-card>
    </div>

    <!-- 在院患者横幅：选中即在院患者开请假单 -->
    <div class="mb-3 flex flex-wrap items-center gap-2">
      <el-select v-model="bannerAdmission" :fit-input-width="false" class="!w-80" clearable data-testid="lv-banner-admission"
                 filterable placeholder="选择在院患者直接开请假单" @change="onBannerPick">
        <el-option v-for="p in inpatients" :key="p.admissionId"
                   :label="`${p.bedNo || '—'}床 ${p.patientName}（${p.wardName || p.deptName || '—'}，在途请假 ${p.activeLeaveCount ?? 0} 张）`"
                   :value="p.admissionId"/>
      </el-select>
      <el-button v-perm="'ipd:leave:add'" :icon="Plus" data-testid="lv-new" type="primary"
                 @click="openForm(null, null)">新建请假单
      </el-button>
    </div>

    <!-- 过滤行（两卡式：查询卡 + 表格卡，口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="q" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="q.keyword" clearable data-testid="lv-keyword" placeholder="单号/患者/住院号/去向"
                    style="width:220px" @keyup.enter="q.pageNum = 1; loadList()"/>
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="q.leaveType" clearable data-testid="lv-filter-type" placeholder="类别"
                     style="width:150px">
            <el-option v-for="d in dict.type" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="q.leaveStatus" clearable data-testid="lv-filter-status" placeholder="状态"
                     style="width:120px">
            <el-option v-for="d in dict.status" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="q.overdueOnly" data-testid="lv-filter-overdue">只看超期未归</el-checkbox>
        </el-form-item>
        <el-form-item label="申请日期">
          <el-date-picker v-model="q.startDate" data-testid="lv-start" placeholder="申请起" style="width:140px"
                          type="date" value-format="YYYY-MM-DD"/>
          <el-date-picker v-model="q.endDate" data-testid="lv-end" placeholder="申请止" style="width:140px"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" data-testid="lv-search" type="primary" @click="q.pageNum = 1; loadList()">查询
          </el-button>
          <el-button :icon="Refresh" data-testid="lv-reset" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 台账 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="lv-table" stripe
                @row-click="showDetail">
        <el-table-column label="单号" min-width="140" prop="leaveNo"/>
        <el-table-column label="患者" min-width="100">
          <template #default="{ row }">{{ row.patientName }}（{{ row.admissionNo || '—' }}）</template>
        </el-table-column>
        <el-table-column label="科室/病区/床位" min-width="160">
          <template #default="{ row }">{{ row.deptName || '—' }} / {{ row.wardName || '—' }} / {{
              row.bedNo || '—'
            }}
          </template>
        </el-table-column>
        <el-table-column label="类别" min-width="120">
          <template #default="{ row }">{{ typeText(row.leaveType) }}</template>
        </el-table-column>
        <el-table-column label="去向" min-width="150" prop="destination" show-overflow-tooltip/>
        <el-table-column label="随行/联系人" min-width="110">
          <template #default="{ row }">{{
              row.companionName
            }}{{ row.companionName ? `（${relationText(row.companionRelation)}）` : '' }}
          </template>
        </el-table-column>
        <el-table-column label="预计离院 → 返回" min-width="200">
          <template #default="{ row }">{{ fmt(row.expectedLeaveTime).slice(5, 16) }} →
            {{ fmt(row.expectedReturnTime).slice(5, 16) }}
          </template>
        </el-table-column>
        <el-table-column label="审批医师" prop="doctorName" width="90"/>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :data-testid="'lv-status-' + row.leaveNo" :type="statusTag(Number(row.leaveStatus))" size="small">
              {{ statusText(row.leaveStatus) }}
            </el-tag>
            <div v-if="row.overdue" class="text-[10px] font-semibold text-red-600">超期 {{ row.overdueHours }} 小时
            </div>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="260">
          <template #default="{ row }">
            <el-button v-if="Number(row.leaveStatus) === NS.PENDING" v-perm="'ipd:leave:add'" data-testid="lv-btn-edit"
                       size="small" @click.stop="openForm(row)">编辑
            </el-button>
            <el-button v-if="Number(row.leaveStatus) === NS.PENDING" v-perm="'ipd:leave:edit'" :data-testid="'lv-approve-' + row.leaveNo"
                       size="small" type="primary" @click.stop="openApprove(row)">审批
            </el-button>
            <el-button v-if="Number(row.leaveStatus) === NS.APPROVED" v-perm="'ipd:leave:edit'" :data-testid="'lv-leave-' + row.leaveNo"
                       size="small" type="success" @click.stop="openLeave(row)">登记离院
            </el-button>
            <el-button v-if="Number(row.leaveStatus) === NS.LEFT" v-perm="'ipd:leave:edit'" :data-testid="'lv-back-' + row.leaveNo" size="small"
                       type="success" @click.stop="openBack(row)">销假
            </el-button>
            <el-button v-if="Number(row.leaveStatus) === NS.LEFT && row.overdue" v-perm="'ipd:leave:edit'" :data-testid="'lv-contact-' + row.leaveNo"
                       size="small" type="danger" @click.stop="openContact(row)">超期处置
            </el-button>
            <el-button v-if="[NS.PENDING, NS.APPROVED].includes(Number(row.leaveStatus))" v-perm="'ipd:leave:edit'"
                       :data-testid="'lv-cancel-' + row.leaveNo" plain size="small" type="danger"
                       @click.stop="openCancel(row)">取消
            </el-button>
            <el-button v-if="[NS.LEFT, NS.RETURNED].includes(Number(row.leaveStatus))" v-perm="'ipd:leave:print'"
                       :data-testid="'lv-print-' + row.leaveNo" :icon="Printer" size="small" @click.stop="onPrint(row)">
              承诺书
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" :page-sizes="PAGE_SIZES"
                       :total="total" data-testid="lv-pagination" layout="total, sizes, prev, pager, next"
                       @current-change="loadList" @size-change="q.pageNum = 1; loadList()"/>
      </div>
    </el-card>

    <!-- 申请表单弹框 -->
    <el-dialog v-model="fVisible" :title="form.id ? '修改请假单（待审批）' : '填写请假单（申请）'" data-testid="lv-form-dialog"
               width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="在院患者" prop="admissionId">
          <el-select v-model="form.admissionId" :disabled="!!form.id" :fit-input-width="false" class="!w-full"
                     clearable
                     data-testid="lv-form-admission" filterable placeholder="搜索姓名/住院号选择在院患者"
                     @change="onAdmissionPick">
            <el-option v-for="p in inpatients" :key="p.admissionId"
                       :label="`${p.bedNo || '—'}床 ${p.patientName}（${p.deptName || '—'}）`" :value="p.admissionId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="患者快照">
          <span class="text-sm text-slate-600" data-testid="lv-form-snapshot">
            {{ form.patientName || '—' }} / {{ form.admissionNo || '—' }} / {{
              form.wardName || '—'
            }} {{ form.bedNo ? form.bedNo + '床' : '' }}
          </span>
        </el-form-item>
        <el-form-item label="请假类别" prop="leaveType">
          <el-radio-group v-model="form.leaveType" data-testid="lv-form-type">
            <el-radio v-for="d in dict.type" :key="d.dictValue" :value="Number(d.dictValue)">{{
                d.dictLabel
              }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="form.reason" :rows="2" data-testid="lv-form-reason" maxlength="500" show-word-limit
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="去向" prop="destination">
          <el-input v-model="form.destination" data-testid="lv-form-destination" maxlength="200"
                    placeholder="写清去哪、能联系到人的地方（责任界定的关键）"/>
        </el-form-item>
        <el-form-item label="随行/联系人" required>
          <div class="flex w-full gap-2">
            <el-input v-model="form.companionName" data-testid="lv-form-companion" maxlength="50" placeholder="姓名"
                      style="flex:1"/>
            <el-select v-model="form.companionRelation" :fit-input-width="false" data-testid="lv-form-relation"
                       placeholder="与患者关系" style="width:150px">
              <el-option v-for="d in dict.relation" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-input v-model="form.companionPhone" :placeholder="form.id && form.companionPhoneMasked ? `已留存 ${form.companionPhoneMasked}，留空表示不修改` : '联系电话'"
                      data-testid="lv-form-phone"
                      maxlength="20" style="width:160px"/>
          </div>
        </el-form-item>
        <el-form-item label="预计离院时间" prop="expectedLeaveTime">
          <el-date-picker v-model="form.expectedLeaveTime" data-testid="lv-form-leave-time" placeholder="预计离开病区"
                          style="width:220px" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item label="预计返回时间" prop="expectedReturnTime">
          <el-date-picker v-model="form.expectedReturnTime" data-testid="lv-form-return-time" placeholder="超过即超期未归"
                          style="width:220px" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
          <span class="ml-2 text-xs text-slate-400">单次请假上限见系统参数（默认 72 小时），超上限后端直接拒绝</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" data-testid="lv-form-remark" maxlength="500"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="lv-form-cancel" @click="fVisible = false">取消</el-button>
        <el-button :loading="fSaving" data-testid="lv-form-save" type="primary" @click="saveForm">保存申请</el-button>
      </template>
    </el-dialog>

    <!-- 审批弹框（批准即医师电子签名 / 拒绝必填理由） -->
    <el-dialog v-model="apVisible" data-testid="lv-approve-dialog" title="审批请假单" width="560px">
      <el-form label-width="110px">
        <el-form-item label="请假单">{{ ap.leaveNo }} / {{ ap.patientName }}</el-form-item>
        <el-form-item label="审批结论" required>
          <el-radio-group v-model="ap.allow" data-testid="lv-approve-allow">
            <el-radio :value="true">批准（医师电子签名锁定）</el-radio>
            <el-radio :value="false">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="ap.allow" label="医师意见" required>
          <el-input v-model="ap.doctorAdvice" :rows="4" data-testid="lv-approve-advice" maxlength="1000" placeholder="病情评估：是否允许外出、外出期间注意事项（随签名一并锁定）"
                    show-word-limit
                    type="textarea"/>
        </el-form-item>
        <el-form-item v-else label="拒绝理由" required>
          <el-input v-model="ap.rejectReason" :rows="3" data-testid="lv-approve-reject" maxlength="500" placeholder="写清病情为什么不允许外出"
                    show-word-limit type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="lv-approve-cancel" @click="apVisible = false">取消</el-button>
        <el-button :loading="apSaving" data-testid="lv-approve-submit" type="primary" @click="doApprove">
          {{ ap.allow ? '确认批准' : '确认拒绝' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 登记离院（患方承诺三要素 + 手写签名板） -->
    <el-dialog v-model="lvVisible" data-testid="lv-leave-dialog" title="登记离院（患方签署风险告知与责任承诺书）"
               width="620px">
      <el-alert :closable="false" class="mb-3" title="未批准不放人；患方三要素（姓名/关系/手写签名）缺一不可 —— 这是「离院期间出事责任界定」的院内凭证。"
                type="warning"/>
      <el-form label-width="120px">
        <el-form-item label="请假单">{{ lv.leaveNo }} / {{ lv.patientName }}</el-form-item>
        <el-form-item label="确认人姓名" required>
          <el-input v-model="lv.confirmName" data-testid="lv-leave-name" maxlength="50"/>
        </el-form-item>
        <el-form-item label="与患者关系" required>
          <el-select v-model="lv.confirmRelation" :fit-input-width="false" data-testid="lv-leave-relation"
                     placeholder="责任界定必填" style="width:220px">
            <el-option v-for="d in dict.relation" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" required>
          <el-input v-model="lv.confirmPhone" data-testid="lv-leave-phone" maxlength="20"/>
        </el-form-item>
        <el-form-item label="实际离院时间">
          <el-date-picker v-model="lv.actualLeaveTime" data-testid="lv-leave-time" placeholder="默认当前时间"
                          style="width:220px" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item label="手写签名" required>
          <SignaturePad ref="signPad"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="lv-leave-cancel" @click="lvVisible = false">取消</el-button>
        <el-button :loading="lvSaving" data-testid="lv-leave-submit" type="primary" @click="doLeave">签署并登记离院
        </el-button>
      </template>
    </el-dialog>

    <!-- 销假弹框 -->
    <el-dialog v-model="bkVisible" data-testid="lv-back-dialog" title="返回销假" width="480px">
      <el-form label-width="110px">
        <el-form-item label="请假单">{{ bk.leaveNo }} / {{ bk.patientName }}</el-form-item>
        <el-form-item label="返回情况">
          <el-input v-model="bk.returnNote" :rows="3" data-testid="lv-back-note" maxlength="500" placeholder="可空：返回时患者状态等"
                    show-word-limit type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="lv-back-cancel" @click="bkVisible = false">取消</el-button>
        <el-button :loading="bkSaving" data-testid="lv-back-submit" type="primary" @click="doBack">确认销假</el-button>
      </template>
    </el-dialog>

    <!-- 取消弹框 -->
    <el-dialog v-model="cxVisible" data-testid="lv-cancel-dialog" title="取消请假单" width="480px">
      <p class="mb-2 text-sm text-slate-500">
        仅「待审批/已批准」可取消；已离院的单不能取消（人已经出去了，事实不能蒸发），请等患者返回后销假。</p>
      <el-input v-model="cx.cancelReason" :rows="3" data-testid="lv-cancel-reason" maxlength="500" placeholder="取消原因（必填：写清为什么取消）"
                show-word-limit type="textarea"/>
      <template #footer>
        <el-button data-testid="lv-cancel-cancel" @click="cxVisible = false">取消</el-button>
        <el-button :loading="cxSaving" data-testid="lv-cancel-submit" type="danger" @click="doCancel">确认取消
        </el-button>
      </template>
    </el-dialog>

    <!-- 超期处置弹框 -->
    <el-dialog v-model="ctVisible" data-testid="lv-contact-dialog" title="超期未归处置" width="520px">
      <el-alert :closable="false" :title="`已超期 ${ct.overdueHours} 小时未返回病区。联系不上必须升级上报（主管医师 → 护士长 → 医务科/总值班）。`" class="mb-3"
                type="error"/>
      <el-form label-width="110px">
        <el-form-item label="请假单">{{ ct.leaveNo }} / {{ ct.patientName }}</el-form-item>
        <el-form-item label="联系结果" required>
          <el-select v-model="ct.contactResult" :fit-input-width="false" data-testid="lv-contact-result" placeholder="请选择"
                     style="width:100%">
            <el-option v-for="d in dict.contact" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="上报对象" required>
          <el-select v-model="ct.reportTo" :fit-input-width="false" data-testid="lv-contact-report" placeholder="请选择"
                     style="width:100%">
            <el-option v-for="d in dict.report" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="处置备注">
          <el-input v-model="ct.contactNote" :rows="3" data-testid="lv-contact-note" maxlength="500" placeholder="联系经过、约定内容等"
                    show-word-limit type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="lv-contact-cancel" @click="ctVisible = false">取消</el-button>
        <el-button :loading="ctSaving" data-testid="lv-contact-submit" type="primary" @click="doContact">记录处置
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情（只读，行点击打开） -->
    <el-dialog v-model="dvVisible" data-testid="lv-detail-dialog" title="请假单详情（只读）" width="760px">
      <el-form :disabled="true" label-width="130px">
        <el-form-item label="单号"><span data-testid="lv-detail-no">{{ dv.leaveNo }}</span></el-form-item>
        <el-form-item label="患者">{{ dv.patientName }} / {{ dv.admissionNo }} / {{ dv.deptName }} {{ dv.wardName }}
          {{ dv.bedNo }}
        </el-form-item>
        <el-form-item label="类别/状态">{{ typeText(dv.leaveType) }} / {{ statusText(dv.leaveStatus) }}
          <el-tag v-if="dv.overdue" class="ml-2" size="small" type="danger">超期 {{ dv.overdueHours }} 小时</el-tag>
        </el-form-item>
        <el-form-item label="请假事由"><span data-testid="lv-detail-reason">{{ dv.reason }}</span></el-form-item>
        <el-form-item label="去向">{{ dv.destination }}</el-form-item>
        <el-form-item label="随行/联系人">{{ dv.companionName }}（{{
            relationText(dv.companionRelation)
          }}）{{ dv.companionPhoneMasked || '' }}
        </el-form-item>
        <el-form-item label="预计离院/返回">{{ fmt(dv.expectedLeaveTime) }} → {{
            fmt(dv.expectedReturnTime)
          }}
        </el-form-item>
        <el-form-item label="申请人">{{ dv.applyBy || '—' }} · {{ fmt(dv.applyTime) }}</el-form-item>
        <el-form-item v-if="dv.doctorAdvice" label="医师意见"><span data-testid="lv-detail-advice">{{
            dv.doctorAdvice
          }}</span></el-form-item>
        <el-form-item v-if="dv.rejectReason" label="拒绝理由">{{ dv.rejectReason }}</el-form-item>
        <el-form-item v-if="dv.actualLeaveTime" label="实际离院">{{ fmt(dv.actualLeaveTime) }}</el-form-item>
        <el-form-item v-if="dv.actualReturnTime" label="实际返回">{{ fmt(dv.actualReturnTime) }}
          {{ dv.returnBy ? `（销假：${dv.returnBy}）` : '' }} {{ dv.returnNote || '' }}
        </el-form-item>
        <el-form-item v-if="dv.confirmName" label="患方签署">
          <div data-testid="lv-detail-confirm">
            <img v-if="dv.confirmSignature" :src="dv.confirmSignature" alt="患方手写签名"
                 class="h-11 border border-slate-200"/>
            <div class="text-xs text-slate-500">{{ dv.confirmName }}（{{
                relationText(dv.confirmRelation)
              }}）　{{ dv.confirmPhoneMasked || '' }}　{{ fmt(dv.confirmTime) }}
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="dv.overdueContactResult" label="超期处置">
          <div data-testid="lv-detail-contact">{{ contactText(dv.overdueContactResult) }} ·
            上报：{{ reportText(dv.reportTo) }} · {{ dv.overdueContactBy || '—' }} {{ fmt(dv.overdueContactTime) }}
            <div class="text-xs text-slate-500">{{ dv.overdueContactNote || '' }}</div>
          </div>
        </el-form-item>
        <el-form-item v-if="dv.cancelReason" label="取消">{{ dv.cancelBy }} · {{ fmt(dv.cancelTime) }} ·
          {{ dv.cancelReason }}
        </el-form-item>
        <el-form-item v-if="dv.signNo" label="电子签名">
          <span class="text-xs text-slate-500" data-testid="lv-detail-sign">签名流水 {{
              dv.signNo
            }}；摘要 {{
              String(dv.contentDigest || '').slice(0, 16)
            }}…；验签 {{
              Number(dv.verifyStatus) === 1 ? '通过' : Number(dv.verifyStatus) === 2 ? '失败' : '未校验'
            }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="lv-detail-close" @click="dvVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 住院患者请假/离院登记（菜单 320 / 路由 /inpatient-leave，后端 /patient/inpatient/leave）
 *
 * 一张单走四步：申请（护士/医生代填草稿）→ 审批（当前登录医师批准即电子签名锁定，biz_type=10；
 * 或拒绝必填理由）→ 登记离院（患方签署「离院风险告知与责任承诺书」三要素：确认人 + 关系 +
 * 手写签名，签字那一刻登记实际离院时间）→ 返回销假。
 * 超期未归是查询时算的展示态（表格标红 + overdueHours），超期处置（联系结果 + 上报）单独落记录。
 * 口径全部在后端：在途唯一、时长上限、未批准不放人、已离院不可取消；动作可用性读后端 can* 字段。
 */
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Plus, Printer, Refresh, Search} from '@element-plus/icons-vue'
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache'
import {dictLabelText} from '@/lib/utils'
import {patientGenderText} from '@/lib/patientGender'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import SignaturePad from '@/components/his/SignaturePad.vue'
import {
  getLeaveBase,
  getLeaveById,
  getLeaveInpatients,
  getLeaveListPage,
  getLeaveStats,
  leaveApprove,
  leaveBack,
  leaveCancel,
  leaveConfirm,
  leaveContact,
  leavePrint,
  leaveUpsert,
} from '@/api/inpatientLeave'

const NS = {PENDING: 1, APPROVED: 2, LEFT: 3, RETURNED: 4, REJECTED: 5, CANCELLED: 6}

const esc = (v) => String(v ?? '').replace(/[&<>"]/g, (c) => ({
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;'
}[c]))
const fmt = (t) => (t ? String(t).slice(0, 19) : '—')

// ---------------- 字典 ----------------
const dict = reactive({type: [], status: [], relation: [], contact: [], report: []})
const loadDicts = async () => {
  try {
    // 一次最多 5 个 type（超了整个返回空）
    const first = await loadDictDataMap([DICT_TYPE.LEAVE_TYPE, DICT_TYPE.LEAVE_STATUS, DICT_TYPE.NOTICE_RELATION].join(','))
    dict.type = first[DICT_TYPE.LEAVE_TYPE] || []
    dict.status = first[DICT_TYPE.LEAVE_STATUS] || []
    dict.relation = first[DICT_TYPE.NOTICE_RELATION] || []
    const second = await loadDictDataMap([DICT_TYPE.LEAVE_CONTACT, DICT_TYPE.LEAVE_REPORT].join(','))
    dict.contact = second[DICT_TYPE.LEAVE_CONTACT] || []
    dict.report = second[DICT_TYPE.LEAVE_REPORT] || []
  } catch (e) {
    console.error('加载请假离院字典失败', e)
  }
}
const typeText = (v) => dictLabelText(dict.type, v)
const statusText = (v) => dictLabelText(dict.status, v)
const relationText = (v) => dictLabelText(dict.relation, v)
const contactText = (v) => dictLabelText(dict.contact, v)
const reportText = (v) => dictLabelText(dict.report, v)
const statusTag = (s) => (Number(s) === NS.PENDING ? 'warning' : Number(s) === NS.APPROVED ? 'primary' : Number(s) === NS.LEFT ? 'danger' : Number(s) === NS.RETURNED ? 'success' : 'info')

const gate = async (p, msg) => {
  try {
    const res = await p
    if (res.code === 200) return res
    ElMessage.error(res.message || msg)
    return null
  } catch (e) {
    ElMessage.error(String((e && e.message) || msg))
    return null
  }
}

// ---------------- 统计卡 ----------------
const stats = ref({})
const loadStats = async () => {
  const res = await gate(getLeaveStats(), '加载统计失败')
  if (res) stats.value = res.data || {}
}
const statCards = computed(() => [
  {k: 'pendingCount', label: '待审批', cls: 'text-amber-500'},
  {k: 'approvedCount', label: '已批准待离院', cls: 'text-blue-500'},
  {k: 'leftCount', label: '在院外', cls: 'text-orange-500'},
  {k: 'overdueCount', label: '超期未归', cls: 'text-red-600'},
  {k: 'returnedTodayCount', label: '今日已返回', cls: 'text-green-600'},
])

// ---------------- 在院患者横幅 ----------------
const inpatients = ref([])
const bannerAdmission = ref(null)
const loadInpatients = async () => {
  const res = await gate(getLeaveInpatients({limit: 200}), '加载在院患者失败')
  if (res) inpatients.value = res.data || []
}
const onBannerPick = async (admissionId) => {
  if (!admissionId) return
  const res = await gate(getLeaveBase(admissionId), '带出患者信息失败')
  if (!res) return
  if (Number(res.data?.activeLeaveCount) > 0) {
    ElMessage.warning('该患者已有一张进行中的请假单（待审批/已批准/已离院），销假或取消后才能再申请')
    return
  }
  openForm({admissionId: String(admissionId)}, res.data)
}

// ---------------- 台账 ----------------
const q = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  keyword: '',
  leaveType: null,
  leaveStatus: null,
  overdueOnly: false,
  startDate: null,
  endDate: null
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const loadList = async () => {
  loading.value = true
  const res = await gate(getLeaveListPage({...q}), '加载请假台账失败')
  if (res) {
    rows.value = res.data?.records || [];
    total.value = res.data?.total || 0
  }
  loading.value = false
}
const resetQuery = () => {
  Object.assign(q, {
    keyword: '',
    leaveType: null,
    leaveStatus: null,
    overdueOnly: false,
    startDate: null,
    endDate: null,
    pageNum: 1
  })
  loadList()
}
const reloadAll = () => Promise.all([loadStats(), loadList(), loadInpatients()])

// ---------------- 申请表单（草稿） ----------------
const fVisible = ref(false)
const fSaving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, admissionId: null, patientName: '', wardName: '', bedNo: '', admissionNo: '',
  leaveType: 1, reason: '', destination: '', companionName: '', companionRelation: null, companionPhone: '',
  expectedLeaveTime: '', expectedReturnTime: '', remark: '',
  // 编辑草稿时电话拿不到明文（列表不出联系方式、详情只给脱敏值），这一格默认留空＝沿用原值
  companionPhoneMasked: '',
})
// 随行人电话的必填是**动态**的：列表 VO 不出联系方式、详情只给脱敏值，编辑草稿时这一格
// 必然是空的（后端也同样放宽了 Service 强校验）。若这里写死 required:true，用户点「编辑」
// 什么都不改直接保存就会被自己的规则弹回「电话不能为空」——现象像修改功能坏了。
// 有留存值（脱敏串）时允许留空＝沿用原值；新建（没有留存值）才必填。
const rules = computed(() => ({
  admissionId: [{required: true, message: '必须挂在一次住院上', trigger: 'change'}],
  leaveType: [{required: true, message: '请选择请假类别', trigger: 'change'}],
  reason: [{required: true, message: '请假事由不能为空', trigger: 'blur'}],
  destination: [{required: true, message: '去向不能为空（写清去哪，责任界定的关键）', trigger: 'blur'}],
  companionName: [{required: true, message: '随行/联系人不能为空', trigger: 'blur'}],
  companionPhone: [{required: !form.companionPhoneMasked, message: '随行人联系电话不能为空', trigger: 'blur'}],
  expectedLeaveTime: [{required: true, message: '预计离院时间不能为空', trigger: 'change'}],
  expectedReturnTime: [{required: true, message: '预计返回时间不能为空', trigger: 'change'}],
}))
const nowText = (plusHours = 0) => {
  const d = new Date(Date.now() + plusHours * 3600 * 1000)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:00`
}
const openForm = (row, base) => {
  Object.assign(form, {
    id: row?.id || null,
    admissionId: row?.admissionId || base?.admissionId || null,
    patientName: base?.patientName || row?.patientName || '',
    wardName: base?.wardName || '',
    bedNo: base?.bedNo || '',
    admissionNo: base?.admissionNo || row?.admissionNo || '',
    leaveType: row?.leaveType || 1,
    reason: row?.reason || '',
    destination: row?.destination || '',
    companionName: row?.companionName || '',
    companionRelation: row?.companionRelation ?? null,
    companionPhone: row?.companionPhone || '',
    expectedLeaveTime: row?.expectedLeaveTime ? String(row.expectedLeaveTime).slice(0, 19) : nowText(1),
    expectedReturnTime: row?.expectedReturnTime ? String(row.expectedReturnTime).slice(0, 19) : nowText(5),
    remark: row?.remark || '',
    companionPhoneMasked: '',
  })
  if (row?.id) fillEditableContent(row.id)
  fVisible.value = true
}
/** 编辑草稿：补全详情字段（台账行有全部申请字段，详情只是兜底） */
const fillEditableContent = async (id) => {
  const res = await gate(getLeaveById(id), '加载请假单失败')
  if (!res) return
  const d = res.data || {}
  Object.assign(form, {
    reason: d.reason || form.reason,
    destination: d.destination || form.destination,
    companionName: d.companionName || form.companionName,
    companionRelation: d.companionRelation ?? form.companionRelation,
    remark: d.remark || form.remark,
    patientName: d.patientName,
    wardName: d.wardName,
    bedNo: d.bedNo,
    admissionNo: d.admissionNo,
    companionPhoneMasked: d.companionPhoneMasked || '',
  })
}
const onAdmissionPick = async (admissionId) => {
  if (!admissionId || form.id) return
  const res = await gate(getLeaveBase(admissionId), '带出患者信息失败')
  if (!res) return
  const b = res.data || {}
  Object.assign(form, {
    patientName: b.patientName, wardName: b.wardName, bedNo: b.bedNo, admissionNo: b.admissionNo,
    reason: form.reason || (b.diagnosis ? '' : ''),
  })
}
const saveForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.expectedReturnTime <= form.expectedLeaveTime) {
    return ElMessage.warning('预计返回时间必须晚于预计离院时间')
  }
  // 电话：编辑态留空＝沿用原值（列表不出明文、详情只给脱敏值，前端无法回显）；新建必须填
  if (!form.companionPhone && !form.companionPhoneMasked) {
    return ElMessage.warning('随行人联系电话不能为空（超期要打电话找人）')
  }
  fSaving.value = true
  const res = await gate(leaveUpsert({
    id: form.id, admissionId: form.admissionId, leaveType: form.leaveType,
    reason: form.reason, destination: form.destination,
    companionName: form.companionName, companionRelation: form.companionRelation,
    // 留空＝沿用原值（后端口径：列表/详情都不出明文电话，编辑时无法回显）
    companionPhone: form.companionPhone || undefined,
    expectedLeaveTime: form.expectedLeaveTime, expectedReturnTime: form.expectedReturnTime, remark: form.remark,
  }), '保存失败')
  fSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '请假单已保存')
  fVisible.value = false
  reloadAll()
}

// ---------------- 审批（批准即电子签名 / 拒绝必填理由） ----------------
const apVisible = ref(false)
const apSaving = ref(false)
const ap = reactive({id: null, leaveNo: '', patientName: '', allow: true, doctorAdvice: '', rejectReason: ''})
const openApprove = (row) => {
  Object.assign(ap, {
    id: row.id,
    leaveNo: row.leaveNo,
    patientName: row.patientName,
    allow: true,
    doctorAdvice: '',
    rejectReason: ''
  })
  apVisible.value = true
}
const doApprove = async () => {
  if (ap.allow && !ap.doctorAdvice) return ElMessage.warning('批准必须填写医师意见（病情评估：是否允许外出、外出期间注意事项）')
  if (!ap.allow && !ap.rejectReason) return ElMessage.warning('拒绝必须填写理由（写清病情为什么不允许外出）')
  apSaving.value = true
  const res = await gate(leaveApprove({
    id: ap.id, allow: ap.allow,
    doctorAdvice: ap.allow ? ap.doctorAdvice : (ap.doctorAdvice || undefined),
    rejectReason: ap.allow ? undefined : ap.rejectReason,
  }), '审批失败')
  apSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '已审批')
  apVisible.value = false
  reloadAll()
}

// ---------------- 登记离院（患方承诺三要素 + 手写签名板） ----------------
const lvVisible = ref(false)
const lvSaving = ref(false)
const signPad = ref(null)
const lv = reactive({
  id: null,
  leaveNo: '',
  patientName: '',
  confirmName: '',
  confirmRelation: null,
  confirmPhone: '',
  actualLeaveTime: ''
})
const openLeave = (row) => {
  Object.assign(lv, {
    id: row.id,
    leaveNo: row.leaveNo,
    patientName: row.patientName,
    confirmName: '',
    confirmRelation: null,
    confirmPhone: '',
    actualLeaveTime: nowText(0)
  })
  lvVisible.value = true
}
const doLeave = async () => {
  if (!lv.confirmName) return ElMessage.warning('患方确认人姓名不能为空')
  if (!lv.confirmRelation) return ElMessage.warning('确认人与患者的关系是责任界定必填项')
  if (!lv.confirmPhone) return ElMessage.warning('确认人联系电话不能为空')
  if (!signPad.value || signPad.value.isEmpty()) return ElMessage.warning('请患方在签名板上手写签名（承诺书必须亲笔签署）')
  lvSaving.value = true
  const res = await gate(leaveConfirm({
    id: lv.id, confirmName: lv.confirmName, confirmRelation: lv.confirmRelation, confirmPhone: lv.confirmPhone,
    confirmSignature: signPad.value.confirm(), actualLeaveTime: lv.actualLeaveTime || undefined,
  }), '离院登记失败')
  lvSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '患方已签署承诺书并登记离院')
  lvVisible.value = false
  reloadAll()
}

// ---------------- 返回销假 ----------------
const bkVisible = ref(false)
const bkSaving = ref(false)
const bk = reactive({id: null, leaveNo: '', patientName: '', returnNote: ''})
const openBack = (row) => {
  Object.assign(bk, {id: row.id, leaveNo: row.leaveNo, patientName: row.patientName, returnNote: ''})
  bkVisible.value = true
}
const doBack = async () => {
  bkSaving.value = true
  const res = await gate(leaveBack({id: bk.id, returnNote: bk.returnNote || undefined}), '销假失败')
  bkSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '已销假')
  bkVisible.value = false
  reloadAll()
}

// ---------------- 取消 ----------------
const cxVisible = ref(false)
const cxSaving = ref(false)
const cx = reactive({id: null, leaveNo: '', cancelReason: ''})
const openCancel = (row) => {
  Object.assign(cx, {id: row.id, leaveNo: row.leaveNo, cancelReason: ''})
  cxVisible.value = true
}
const doCancel = async () => {
  if (!cx.cancelReason) return ElMessage.warning('取消原因不能为空（写清为什么取消）')
  cxSaving.value = true
  const res = await gate(leaveCancel({id: cx.id, cancelReason: cx.cancelReason}), '取消失败')
  cxSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '已取消')
  cxVisible.value = false
  reloadAll()
}

// ---------------- 超期处置 ----------------
const ctVisible = ref(false)
const ctSaving = ref(false)
const ct = reactive({
  id: null,
  leaveNo: '',
  patientName: '',
  overdueHours: 0,
  contactResult: null,
  contactNote: '',
  reportTo: null
})
const openContact = (row) => {
  Object.assign(ct, {
    id: row.id,
    leaveNo: row.leaveNo,
    patientName: row.patientName,
    overdueHours: row.overdueHours || 0,
    contactResult: null,
    contactNote: '',
    reportTo: null
  })
  ctVisible.value = true
}
const doContact = async () => {
  if (!ct.contactResult) return ElMessage.warning('请选择联系结果')
  if (!ct.reportTo) return ElMessage.warning('请选择上报对象（联系不上必须升级上报）')
  ctSaving.value = true
  const res = await gate(leaveContact({
    id: ct.id,
    contactResult: ct.contactResult,
    contactNote: ct.contactNote || undefined,
    reportTo: ct.reportTo
  }), '超期处置失败')
  ctSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '超期处置已记录')
  ctVisible.value = false
  reloadAll()
}

// ---------------- 详情（只读） ----------------
const dvVisible = ref(false)
const dv = ref({})
const showDetail = async (row) => {
  const res = await gate(getLeaveById(row.id), '加载详情失败')
  if (res) {
    dv.value = res.data || {};
    dvVisible.value = true
  }
}

// ---------------- 承诺书打印（患方联 + 病历联） ----------------
const COPIES = ['病历联（随病历存档）', '患方联（交患者/家属留存）']
const receiptHtml = (d) => {
  const sigImg = d.confirmSignature && String(d.confirmSignature).startsWith('data:image/png;base64,')
      ? `<img src="${esc(d.confirmSignature)}" style="height:44px" alt="患方签名" />`
      : '<span style="color:#999">（无）</span>'
  const verify = Number(d.verifyStatus) === 1 ? '通过' : Number(d.verifyStatus) === 2 ? '失败' : '未校验'
  return `
    <div class="row"><span>姓名：${esc(d.patientName)}</span><span>性别：${esc(patientGenderText(d.gender))}</span>
      <span>年龄：${esc(d.age)}岁</span><span>住院号：${esc(d.admissionNo)}</span></div>
    <div class="row"><span>科室：${esc(d.deptName || '—')}</span><span>病区/床号：${esc(d.wardName || '—')} / ${esc(d.bedNo || '—')}</span>
      <span>请假类别：<b>${esc(typeText(d.leaveType))}</b></span></div>
    <h3>一、请假事由与去向</h3><div class="para">事由：${esc(d.reason)}<br/>去向：${esc(d.destination)}　随行/联系人：${esc(d.companionName)}（${esc(relationText(d.companionRelation))}）　电话：${esc(d.companionPhoneMasked || '—')}</div>
    <div class="row"><span>预计离院：${esc(fmt(d.expectedLeaveTime))}</span><span>预计返回：${esc(fmt(d.expectedReturnTime))}</span></div>
    <h3>二、医师意见（病情评估）</h3><div class="para">${esc(d.doctorAdvice || '—')}</div>
    <h3>三、离院风险告知</h3><div class="para">住院期间擅自离开病区可能延误病情观察与救治，外出期间发生的病情变化、意外伤害及其他不良后果，
      由患者/家属自行承担相应责任；请保持联系电话畅通，按约定时间返回病区，如有特殊情况提前告知医护人员。</div>
    <h3>四、患方承诺</h3><div class="para">本人/家属已充分知晓上述风险与注意事项，自愿申请离院并承诺按期返回。</div>
    <div class="row"><span>实际离院时间：${esc(fmt(d.actualLeaveTime))}</span><span>实际返回时间：${esc(fmt(d.actualReturnTime))}</span></div>
    <div class="sign">
      <span>审批医师（电子签名已锚定）：${esc(d.doctorName || '—')}</span>
      <span>审批时间：${esc(fmt(d.approveTime))}</span>
    </div>
    <div class="sign">
      <span>患方签名（亲笔）：${sigImg}</span>
      <span>姓名：${esc(d.confirmName || '—')}　与患者关系：${esc(relationText(d.confirmRelation))}</span>
    </div>
    <div class="row"><span>联系电话：${esc(d.confirmPhoneMasked || '—')}</span><span>签署时间：${esc(fmt(d.confirmTime))}</span></div>
    <div class="foot">医师电子签名流水：${esc(d.signNo || '—')}　内容摘要(SHA-256)：${esc(d.contentDigest ? String(d.contentDigest).slice(0, 16) + '…' : '—')}
      ；验签：${esc(verify)}　第 ${esc(d.printCount || 1)} 次打印 · ${esc(d.printerName || '')} · 单号 ${esc(d.leaveNo)}</div>`
}
const onPrint = async (row) => {
  const got = await gate(getLeaveById(row.id), '加载详情失败')
  if (!got) return
  const d = got.data || {}
  const ackRes = await gate(leavePrint({id: row.id}), '打印计数失败')
  if (!ackRes) return
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>${esc(d.leaveNo)}</title><style>
      @page { size: A4; margin: 12mm; }
      body { font-family: "Microsoft YaHei", sans-serif; color: #111; margin: 0; }
      .copy { page-break-after: always; padding: 8px 4px; }
      .copy:last-child { page-break-after: auto; }
      h1 { font-size: 17px; text-align: center; margin: 0 0 2px; }
      .sub { text-align: center; font-size: 12px; color: #444; margin-bottom: 6px; }
      .lian { text-align: center; font-size: 12px; margin-bottom: 8px; }
      h3 { font-size: 12px; margin: 10px 0 4px; }
      .para { font-size: 12px; border: 1px solid #000; padding: 6px 8px; min-height: 34px; }
      .row { font-size: 12px; margin: 4px 0; display: flex; gap: 18px; flex-wrap: wrap; }
      .sign { margin-top: 16px; display: flex; justify-content: space-between; align-items: center; font-size: 12px; }
      .foot { margin-top: 10px; font-size: 10px; color: #555; border-top: 1px dashed #999; padding-top: 4px; }
    </style></head><body>
    ${COPIES.map((c, i) => `<div class="copy"><h1>住院患者请假/离院风险告知与责任承诺书</h1>
      <div class="sub">单号：${esc(d.leaveNo)}</div>
      <div class="lian">${esc(c)} · 第 ${i + 1} 联</div>${receiptHtml(d)}</div>`).join('')}
    </body></html>`
  const win = window.open('', '_blank')
  if (!win) return ElMessage.error('浏览器拦截了新窗口，请允许弹出后重试')
  win.document.write(html)
  win.document.close()
  win.onload = () => win.print()
  await reloadAll()
}

onMounted(async () => {
  await loadDicts()
  await reloadAll()
})
</script>
