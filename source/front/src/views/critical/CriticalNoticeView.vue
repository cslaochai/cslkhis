<template>
  <div data-testid="critical-notice-page">
    <!-- 统计卡 -->
    <div class="grid grid-cols-12 gap-2 mb-3">
      <el-card v-for="c in statCards" :key="c.k" class="!rounded-lg !py-1" shadow="never">
        <div class="text-center">
          <div :class="['text-xl font-semibold', c.cls]" :data-testid="'stat-' + c.k">{{ stats[c.k] ?? 0 }}</div>
          <div class="mt-0.5 text-[11px] text-slate-400">{{ c.label }}</div>
        </div>
      </el-card>
    </div>

    <!-- 在院患者横幅：选中即在院患者开单 -->
    <div class="flex flex-wrap items-center gap-2 mb-3">
      <el-select v-model="bannerAdmission" :fit-input-width="false" class="!w-80" clearable data-testid="cn-banner-admission"
                 filterable placeholder="选择在院患者直接开通知单" @change="onBannerPick">
        <el-option v-for="p in inpatients" :key="p.admissionId"
                   :label="`${p.bedNo || '—'}床 ${p.patientName}（${p.wardName || p.deptName || '—'}，已告知 ${p.noticeCount ?? 0} 次）`"
                   :value="p.admissionId"/>
      </el-select>
      <el-button v-perm="'ipd:criticalNotice:add'" :icon="Plus" data-testid="cn-new" type="primary"
                 @click="openForm(null, null)">新建通知单
      </el-button>
    </div>

    <!-- 过滤行 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="q" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="q.keyword" clearable data-testid="cn-keyword" placeholder="单号/患者/住院号/诊断"
                    style="width:220px" @keyup.enter="q.pageNum = 1; loadList()"/>
        </el-form-item>
        <el-form-item label="类别">
          <el-select v-model="q.noticeType" clearable data-testid="cn-filter-type" placeholder="类别"
                     style="width:110px">
            <el-option v-for="d in dict.type" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="q.noticeStatus" clearable data-testid="cn-filter-status" placeholder="状态"
                     style="width:120px">
            <el-option v-for="d in dict.status" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="告知起">
          <el-date-picker v-model="q.startDate" data-testid="cn-start" placeholder="告知起" style="width:140px"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="告知止">
          <el-date-picker v-model="q.endDate" data-testid="cn-end" placeholder="告知止" style="width:140px"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" data-testid="cn-search" type="primary" @click="q.pageNum = 1; loadList()">查询
          </el-button>
          <el-button :icon="Refresh" data-testid="cn-reset" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 台账 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="cn-table" stripe
                @row-click="showDetail">
        <el-table-column label="单号" min-width="140" prop="noticeNo"/>
        <el-table-column label="患者" min-width="100">
          <template #default="{ row }">{{ row.patientName }}（{{ row.admissionNo || '—' }}）</template>
        </el-table-column>
        <el-table-column label="科室/病区/床位" min-width="160">
          <template #default="{ row }">{{ row.deptName || '—' }} / {{ row.wardName || '—' }} / {{
              row.bedNo || '—'
            }}
          </template>
        </el-table-column>
        <el-table-column label="类别" width="80">
          <template #default="{ row }">
            <el-tag :type="Number(row.noticeType) === 1 ? 'danger' : 'warning'" size="small">{{
                typeText(row.noticeType)
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="目前诊断" min-width="180" prop="clinicalDiagnosis" show-overflow-tooltip/>
        <el-table-column label="告知时间" min-width="150">
          <template #default="{ row }">{{ fmt(row.notifyTime) }}</template>
        </el-table-column>
        <el-table-column label="告知医师" prop="doctorName" width="100"/>
        <el-table-column label="签收人" min-width="120">
          <template #default="{ row }">{{
              row.signerName || '—'
            }}{{ row.signerName ? `（${relationText(row.signerRelation)}）` : '' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(Number(row.noticeStatus))" size="small">{{ statusText(row.noticeStatus) }}</el-tag>
            <div class="text-slate-400">{{ signTagText(row.signStatus) }}</div>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="240">
          <template #default="{ row }">
            <el-button v-if="Number(row.noticeStatus) === NS.DRAFT" v-perm="'ipd:criticalNotice:add'" data-testid="cn-btn-edit"
                       size="small" @click.stop="openForm(row)">编辑
            </el-button>
            <el-button v-if="Number(row.noticeStatus) === NS.DRAFT" v-perm="'ipd:criticalNotice:edit'" :data-testid="'cn-issue-' + row.noticeNo"
                       size="small" type="primary" @click.stop="onIssue(row)">签发
            </el-button>
            <el-button v-if="Number(row.noticeStatus) === NS.ISSUED" v-perm="'ipd:criticalNotice:edit'" :data-testid="'cn-ack-' + row.noticeNo"
                       size="small" type="success" @click.stop="openAck(row)">签收
            </el-button>
            <el-button v-if="Number(row.noticeStatus) === NS.ACKED" v-perm="'ipd:criticalNotice:print'" :data-testid="'cn-print-' + row.noticeNo"
                       :icon="Printer" size="small" @click.stop="onPrint(row)">回执
            </el-button>
            <el-button v-if="[NS.DRAFT, NS.ISSUED].includes(Number(row.noticeStatus))"
                       v-perm="'ipd:criticalNotice:edit'" :data-testid="'cn-void-' + row.noticeNo" plain size="small"
                       type="danger" @click.stop="openVoid(row)">作废
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="q.pageNum" v-model:page-size="q.pageSize" :page-sizes="PAGE_SIZES"
                       :total="total" data-testid="cn-pagination" layout="total, sizes, prev, pager, next"
                       @current-change="loadList" @size-change="q.pageNum = 1; loadList()"/>
      </div>
    </el-card>

    <!-- 表单弹框（草稿） -->
    <el-dialog v-model="fVisible" :title="form.id ? '修改通知单（草稿）' : '填写通知单（草稿）'" data-testid="cn-form-dialog"
               width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="在院患者" prop="admissionId">
          <el-select v-model="form.admissionId" :disabled="!!form.id" :fit-input-width="false" class="!w-full"
                     clearable
                     data-testid="cn-form-admission" filterable placeholder="搜索姓名/住院号选择在院患者"
                     @change="onAdmissionPick">
            <el-option v-for="p in inpatients" :key="p.admissionId"
                       :label="`${p.bedNo || '—'}床 ${p.patientName}（${p.deptName || '—'}）`" :value="p.admissionId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="患者快照">
          <span class="text-sm text-slate-600" data-testid="cn-form-snapshot">
            {{ form.patientName || '—' }} / {{ form.admissionNo || '—' }} / {{
              form.wardName || '—'
            }} {{ form.bedNo ? form.bedNo + '床' : '' }}
          </span>
        </el-form-item>
        <el-form-item label="通知类别" prop="noticeType">
          <el-radio-group v-model="form.noticeType" data-testid="cn-form-type">
            <el-radio v-for="d in dict.type" :key="d.dictValue" :value="Number(d.dictValue)">{{
                d.dictLabel
              }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="患者神志" prop="consciousnessStatus">
          <el-select v-model="form.consciousnessStatus" data-testid="cn-form-cons" style="width:180px">
            <el-option v-for="d in dict.consciousness" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="目前诊断" prop="clinicalDiagnosis">
          <el-input v-model="form.clinicalDiagnosis" data-testid="cn-form-diagnosis" maxlength="500"/>
        </el-form-item>
        <el-form-item label="病情及危险因素" prop="conditionDesc">
          <el-input v-model="form.conditionDesc" :rows="3" data-testid="cn-form-condition" maxlength="1000" show-word-limit
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="预警事项" prop="warningMatters">
          <el-input v-model="form.warningMatters" :rows="3" data-testid="cn-form-warning" maxlength="1000" show-word-limit
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="医方措施">
          <el-input v-model="form.doctorMeasures" :rows="2" data-testid="cn-form-measures" maxlength="1000" show-word-limit
                    type="textarea"/>
        </el-form-item>
        <el-form-item label="告知时间" prop="notifyTime">
          <el-date-picker v-model="form.notifyTime" data-testid="cn-form-notify-time" placeholder="精确到分"
                          style="width:220px" type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
        </el-form-item>
        <el-form-item label="见证医师">
          <el-select v-model="form.witnessDoctorId" :fit-input-width="false" class="!w-full"
                     clearable data-testid="cn-form-witness"
                     filterable placeholder="抢救场景第二医师（可空，不得与告知医师同人）">
            <el-option v-for="d in doctors" :key="d.employeeId" :label="`${d.empName}（${d.deptName || '—'}）`"
                       :value="d.employeeId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" data-testid="cn-form-remark" maxlength="500"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="cn-form-cancel" @click="fVisible = false">取消</el-button>
        <el-button :loading="fSaving" data-testid="cn-form-save" type="primary" @click="saveForm">保存草稿</el-button>
      </template>
    </el-dialog>

    <!-- 签收弹框（家属手写签名板） -->
    <el-dialog v-model="ackVisible" data-testid="cn-ack-dialog" title="家属签收（手写签名 + 法定关系）" width="560px">
      <el-form label-width="110px">
        <el-form-item label="通知单">{{ ack.noticeNo }} / {{ ack.patientName }}</el-form-item>
        <el-form-item label="签收人姓名" required>
          <el-input v-model="ack.signerName" data-testid="cn-ack-name" maxlength="50"/>
        </el-form-item>
        <el-form-item label="与患者关系" required>
          <el-select v-model="ack.signerRelation" :fit-input-width="false" data-testid="cn-ack-relation"
                     placeholder="法定必填" style="width:220px">
            <el-option v-for="d in dict.relation" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="证件号">
          <el-input v-model="ack.signerIdCard" data-testid="cn-ack-idcard" maxlength="20"/>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="ack.signerPhone" data-testid="cn-ack-phone" maxlength="20"/>
        </el-form-item>
        <el-form-item label="手写签名" required>
          <SignaturePad ref="signPad"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="cn-ack-cancel" @click="ackVisible = false">取消</el-button>
        <el-button :loading="ackSaving" data-testid="cn-ack-submit" type="primary" @click="doAck">确认签收</el-button>
      </template>
    </el-dialog>

    <!-- 作废弹框 -->
    <el-dialog v-model="vVisible" data-testid="cn-void-dialog" title="作废通知单" width="480px">
      <p class="mb-2 text-sm text-slate-500">{{
          vForm.noticeNo
        }}：作废不改内容、不删行，仅状态留痕；已签名锁定的须先在签名中心作废签名。</p>
      <el-input v-model="vForm.voidReason" :rows="3" data-testid="cn-void-reason" maxlength="500" placeholder="作废原因（必填：写清错在哪）"
                show-word-limit type="textarea"/>
      <template #footer>
        <el-button data-testid="cn-void-cancel" @click="vVisible = false">取消</el-button>
        <el-button data-testid="cn-void-submit" type="danger" @click="doVoid">确认作废</el-button>
      </template>
    </el-dialog>

    <!-- 详情（只读，行点击打开） -->
    <el-dialog v-model="dvVisible" data-testid="cn-detail-dialog" title="通知单详情（只读）" width="720px">
      <el-form :disabled="true" label-width="110px">
        <el-form-item label="单号"><span data-testid="cn-detail-no">{{ dv.noticeNo }}</span></el-form-item>
        <el-form-item label="患者">{{ dv.patientName }} / {{ dv.admissionNo }} / {{ dv.deptName }} {{ dv.wardName }}
          {{ dv.bedNo }}
        </el-form-item>
        <el-form-item label="类别/神志">{{ typeText(dv.noticeType) }} / {{
            consText(dv.consciousnessStatus)
          }}
        </el-form-item>
        <el-form-item label="目前诊断"><span data-testid="cn-detail-diagnosis">{{ dv.clinicalDiagnosis }}</span>
        </el-form-item>
        <el-form-item label="病情及危险因素">
          <el-input :model-value="dv.conditionDesc" :rows="3" type="textarea"/>
        </el-form-item>
        <el-form-item label="预警事项">
          <el-input :model-value="dv.warningMatters" :rows="3" type="textarea"/>
        </el-form-item>
        <el-form-item label="医方措施">
          <el-input :model-value="dv.doctorMeasures" :rows="2" type="textarea"/>
        </el-form-item>
        <el-form-item label="告知时间">{{ fmt(dv.notifyTime) }}</el-form-item>
        <el-form-item label="告知/见证医师">{{ dv.doctorName }} / {{ dv.witnessDoctorName || '—' }}</el-form-item>
        <el-form-item label="状态">
          <el-tag :type="statusTag(Number(dv.noticeStatus))" size="small">{{ statusText(dv.noticeStatus) }}</el-tag>
          <el-tag class="ml-2" size="small" type="info">{{ signTagText(dv.signStatus) }}</el-tag>
        </el-form-item>
        <el-form-item v-if="Number(dv.noticeStatus) === NS.ACKED" label="签收信息">
          <div data-testid="cn-detail-ack">
            <img v-if="dv.signerSignature" :src="dv.signerSignature" alt="家属手写签名"
                 class="h-11 border border-slate-200"/>
            <div class="text-xs text-slate-500">{{ dv.signerName }}（{{
                relationText(dv.signerRelation)
              }}）　{{ dv.signerIdCardMasked || '' }}　{{ dv.signerPhoneMasked || '' }}　{{ fmt(dv.acknowledgeTime) }}
            </div>
          </div>
        </el-form-item>
        <el-form-item v-if="dv.signNo" label="电子签名">
          <span class="text-xs text-slate-500" data-testid="cn-detail-sign">签名流水 {{
              dv.signNo
            }}；摘要 {{
              String(dv.contentDigest || '').slice(0, 16)
            }}…；验签 {{
              Number(dv.verifyStatus) === 1 ? '通过' : Number(dv.verifyStatus) === 2 ? '失败' : '未校验'
            }}</span>
        </el-form-item>
        <el-form-item v-if="dv.noticeStatus === NS.VOID" label="作废">{{ dv.voidBy }} · {{ fmt(dv.voidTime) }} ·
          {{ dv.voidReason }}
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button data-testid="cn-detail-close" @click="dvVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 病危/病重通知与告知书签收回执（菜单 319 / 路由 /critical-notice，后端 /patient/criticalNotice）
 *
 * 一张单走三步：填写（草稿）→ 签发（当前登录医师电子签名锁定，biz_type=9）→
 * 家属签收（手写签名板 + 法定关系）→ 打印两联回执（病历联+患方联）。
 * 口径全部在后端：签名即锁定、已签收禁作废、患者一般项目服务端重查快照；
 * 页面只渲染后端给的脱敏字段（signerIdCardMasked/signerPhoneMasked），不做任何遮码。
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
  getNoticeBase,
  getNoticeById,
  getNoticeDoctorOptions,
  getNoticeInpatients,
  getNoticeListPage,
  getNoticeStats,
  noticeAcknowledge,
  noticeIssue,
  noticePrint,
  noticeUpsert,
  noticeVoid,
} from '@/api/criticalNotice'

const NS = {DRAFT: 1, ISSUED: 2, ACKED: 3, VOID: 4}
const COPIES = ['病历联（随病历存档）', '患方联（交家属留存）']

const esc = (v) => String(v ?? '').replace(/[&<>"]/g, (c) => ({
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;'
}[c]))
const fmt = (t) => (t ? String(t).slice(0, 19) : '—')

// ---------------- 字典 ----------------
const dict = reactive({type: [], status: [], consciousness: [], relation: []})
const loadDicts = async () => {
  try {
    const map = await loadDictDataMap([
      DICT_TYPE.NOTICE_TYPE, DICT_TYPE.NOTICE_STATUS, DICT_TYPE.NOTICE_CONSCIOUSNESS, DICT_TYPE.NOTICE_RELATION,
    ].join(','))
    dict.type = map[DICT_TYPE.NOTICE_TYPE] || []
    dict.status = map[DICT_TYPE.NOTICE_STATUS] || []
    dict.consciousness = map[DICT_TYPE.NOTICE_CONSCIOUSNESS] || []
    dict.relation = map[DICT_TYPE.NOTICE_RELATION] || []
  } catch (e) {
    console.error('加载病危重通知字典失败', e)
  }
}
const typeText = (v) => dictLabelText(dict.type, v)
const statusText = (v) => dictLabelText(dict.status, v)
const consText = (v) => dictLabelText(dict.consciousness, v)
const relationText = (v) => dictLabelText(dict.relation, v)
const statusTag = (s) => (s === NS.DRAFT ? 'warning' : s === NS.ISSUED ? 'primary' : s === NS.ACKED ? 'success' : 'info')
const signTagText = (s) => (Number(s) === 1 ? '已签名' : Number(s) === 2 ? '签名已作废' : '未签名')

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
  const res = await gate(getNoticeStats(), '加载统计失败')
  if (res) stats.value = res.data || {}
}
const statCards = computed(() => [
  {k: 'draftCount', label: '草稿', cls: 'text-amber-500'},
  {k: 'issuedCount', label: '待签收', cls: 'text-blue-500'},
  {k: 'ackedCount', label: '已签收', cls: 'text-green-600'},
  {k: 'voidCount', label: '已作废', cls: 'text-slate-400'},
])

// ---------------- 在院患者横幅 ----------------
const inpatients = ref([])
const bannerAdmission = ref(null)
const loadInpatients = async () => {
  const res = await gate(getNoticeInpatients({limit: 200}), '加载在院患者失败')
  if (res) inpatients.value = res.data || []
}
const onBannerPick = async (admissionId) => {
  if (!admissionId) return
  const res = await gate(getNoticeBase(admissionId), '带出患者信息失败')
  if (!res) return
  openForm({admissionId: String(admissionId)}, res.data)
}

// ---------------- 台账 ----------------
const q = reactive({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  keyword: '',
  noticeType: null,
  noticeStatus: null,
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
  const res = await gate(getNoticeListPage({...q}), '加载通知台账失败')
  if (res) {
    rows.value = res.data?.records || [];
    total.value = res.data?.total || 0
  }
  loading.value = false
}
const resetQuery = () => {
  Object.assign(q, {keyword: '', noticeType: null, noticeStatus: null, startDate: null, endDate: null, pageNum: 1})
  loadList()
}
const reloadAll = () => Promise.all([loadStats(), loadList(), loadInpatients()])

// ---------------- 医师下拉 ----------------
const doctors = ref([])
const loadDoctors = async () => {
  const res = await gate(getNoticeDoctorOptions(), '加载医师候选失败')
  if (res) doctors.value = res.data || []
}

// ---------------- 表单（草稿） ----------------
const fVisible = ref(false)
const fSaving = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, admissionId: null, patientName: '', wardName: '', bedNo: '', admissionNo: '',
  noticeType: 1, consciousnessStatus: 1, clinicalDiagnosis: '', conditionDesc: '',
  warningMatters: '', doctorMeasures: '', notifyTime: '', witnessDoctorId: null, remark: '',
})
const rules = {
  admissionId: [{required: true, message: '必须挂在一次住院上', trigger: 'change'}],
  noticeType: [{required: true, message: '请选择通知类别', trigger: 'change'}],
  consciousnessStatus: [{required: true, message: '请选择患者神志', trigger: 'change'}],
  clinicalDiagnosis: [{required: true, message: '目前诊断不能为空', trigger: 'blur'}],
  conditionDesc: [{required: true, message: '病情及危险因素不能为空', trigger: 'blur'}],
  warningMatters: [{required: true, message: '预警事项不能为空', trigger: 'blur'}],
  notifyTime: [{required: true, message: '告知时间不能为空', trigger: 'change'}],
}
const nowText = () => {
  const d = new Date()
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:00`
}
const openForm = (row, base) => {
  Object.assign(form, {
    id: row?.id || null,
    admissionId: row?.admissionId || base?.admissionId || null,
    patientName: base?.patientName || row?.patientName || '',
    wardName: base?.wardName || '', bedNo: base?.bedNo || '', admissionNo: base?.admissionNo || row?.admissionNo || '',
    noticeType: row?.noticeType || 1, consciousnessStatus: row?.consciousnessStatus || 1,
    clinicalDiagnosis: row?.clinicalDiagnosis || base?.diagnosis || '',
    conditionDesc: '', warningMatters: '', doctorMeasures: '',
    notifyTime: row?.notifyTime ? String(row.notifyTime).slice(0, 19) : nowText(),
    witnessDoctorId: row?.witnessDoctorId || null, remark: row?.remark || '',
  })
  if (row?.id) fillEditableContent(row.id)
  fVisible.value = true
}
/** 编辑草稿：正文大字段不在台账行里，走详情接口补全（后端未脱敏正文，回显零损失） */
const fillEditableContent = async (id) => {
  const res = await gate(getNoticeById(id), '加载通知单失败')
  if (!res) return
  const d = res.data || {}
  Object.assign(form, {
    conditionDesc: d.conditionDesc || '', warningMatters: d.warningMatters || '',
    doctorMeasures: d.doctorMeasures || '', remark: d.remark || '',
    witnessDoctorId: d.witnessDoctorId || form.witnessDoctorId,
    patientName: d.patientName, wardName: d.wardName, bedNo: d.bedNo, admissionNo: d.admissionNo,
  })
}
const onAdmissionPick = async (admissionId) => {
  if (!admissionId || form.id) return
  const res = await gate(getNoticeBase(admissionId), '带出患者信息失败')
  if (!res) return
  const b = res.data || {}
  Object.assign(form, {
    patientName: b.patientName, wardName: b.wardName, bedNo: b.bedNo,
    admissionNo: b.admissionNo, clinicalDiagnosis: form.clinicalDiagnosis || b.diagnosis || '',
  })
}
const saveForm = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  fSaving.value = true
  const res = await gate(noticeUpsert({
    id: form.id, admissionId: form.admissionId, noticeType: form.noticeType,
    consciousnessStatus: form.consciousnessStatus, clinicalDiagnosis: form.clinicalDiagnosis,
    conditionDesc: form.conditionDesc, warningMatters: form.warningMatters,
    doctorMeasures: form.doctorMeasures, notifyTime: form.notifyTime,
    witnessDoctorId: form.witnessDoctorId, remark: form.remark,
  }), '保存失败')
  fSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '通知单已保存')
  fVisible.value = false
  reloadAll()
}

// ---------------- 签发 ----------------
const onIssue = async (row) => {
  const res = await gate(noticeIssue({id: row.id}), '签发失败')
  if (!res) return
  ElMessage.success(res.message || '已签发并完成医师电子签名')
  reloadAll()
}

// ---------------- 签收（家属手写签名板） ----------------
const ackVisible = ref(false)
const ackSaving = ref(false)
const signPad = ref(null)
const ack = reactive({
  id: null,
  noticeNo: '',
  patientName: '',
  signerName: '',
  signerRelation: null,
  signerIdCard: '',
  signerPhone: ''
})
const openAck = (row) => {
  Object.assign(ack, {
    id: row.id,
    noticeNo: row.noticeNo,
    patientName: row.patientName,
    signerName: '',
    signerRelation: null,
    signerIdCard: '',
    signerPhone: ''
  })
  ackVisible.value = true
}
const doAck = async () => {
  if (!ack.signerName) return ElMessage.warning('签收人姓名不能为空')
  if (!ack.signerRelation) return ElMessage.warning('签收人与患者的关系是法定必填项')
  if (!signPad.value || signPad.value.isEmpty()) return ElMessage.warning('请家属在签名板上手写签名')
  ackSaving.value = true
  const res = await gate(noticeAcknowledge({...ack, signerSignature: signPad.value.confirm()}), '签收失败')
  ackSaving.value = false
  if (!res) return
  ElMessage.success(res.message || '家属已签收')
  ackVisible.value = false
  reloadAll()
}

// ---------------- 作废 ----------------
const vVisible = ref(false)
const vForm = reactive({id: null, noticeNo: '', voidReason: ''})
const openVoid = (row) => {
  Object.assign(vForm, {id: row.id, noticeNo: row.noticeNo, voidReason: ''});
  vVisible.value = true
}
const doVoid = async () => {
  if (!vForm.voidReason) return ElMessage.warning('作废原因不能为空（写清错在哪）')
  const res = await gate(noticeVoid({id: vForm.id, voidReason: vForm.voidReason}), '作废失败')
  if (!res) return
  ElMessage.success(res.message || '已作废')
  vVisible.value = false
  reloadAll()
}

// ---------------- 详情（只读） ----------------
const dvVisible = ref(false)
const dv = ref({})
const showDetail = async (row) => {
  const res = await gate(getNoticeById(row.id), '加载详情失败')
  if (res) {
    dv.value = res.data || {};
    dvVisible.value = true
  }
}

// ---------------- 两联回执打印 ----------------
const receiptHtml = (d) => {
  const sigImg = d.signerSignature && String(d.signerSignature).startsWith('data:image/png;base64,')
      ? `<img src="${esc(d.signerSignature)}" style="height:44px" alt="家属签名" />`
      : '<span style="color:#999">（无）</span>'
  const verify = Number(d.verifyStatus) === 1 ? '通过' : Number(d.verifyStatus) === 2 ? '失败' : '未校验'
  return `
    <div class="row"><span>姓名：${esc(d.patientName)}</span><span>性别：${esc(patientGenderText(d.gender))}</span>
      <span>年龄：${esc(d.age)}岁</span><span>住院号：${esc(d.admissionNo)}</span></div>
    <div class="row"><span>科室：${esc(d.deptName || '—')}</span><span>病区/床号：${esc(d.wardName || '—')} / ${esc(d.bedNo || '—')}</span>
      <span>通知类别：<b>${esc(typeText(d.noticeType))}</b></span><span>患者神志：${esc(consText(d.consciousnessStatus))}</span></div>
    <h3>一、目前诊断</h3><div class="para">${esc(d.clinicalDiagnosis)}</div>
    <h3>二、患者目前情况（病情及危险因素）</h3><div class="para">${esc(d.conditionDesc)}</div>
    <h3>三、病情可能出现的危险情况（预警事项）</h3><div class="para">${esc(d.warningMatters)}</div>
    <h3>四、医方措施与配合要求</h3><div class="para">${esc(d.doctorMeasures || '—')}</div>
    <div class="row"><span>告知时间：${esc(fmt(d.notifyTime))}</span></div>
    <div class="sign">
      <span>告知医师（电子签名已锚定）：${esc(d.doctorName)}</span>
      <span>见证医师：${esc(d.witnessDoctorName || '—')}</span>
      <span>签发时间：${esc(fmt(d.issueTime))}</span>
    </div>
    <div class="sign">
      <span>签收人（患方亲笔）：${sigImg}</span>
      <span>姓名：${esc(d.signerName || '—')}　与患者关系：${esc(relationText(d.signerRelation))}</span>
    </div>
    <div class="row"><span>签收人证件号：${esc(d.signerIdCardMasked || '—')}</span>
      <span>联系电话：${esc(d.signerPhoneMasked || '—')}</span>
      <span>签收时间：${esc(fmt(d.acknowledgeTime))}</span></div>
    <div class="foot">医师电子签名流水：${esc(d.signNo || '—')}　内容摘要(SHA-256)：${esc(d.contentDigest ? String(d.contentDigest).slice(0, 16) + '…' : '—')}
      ；验签：${esc(verify)}　第 ${esc(d.printCount || 1)} 次打印 · ${esc(d.printerName || '')}</div>`
}
const onPrint = async (row) => {
  const got = await gate(getNoticeById(row.id), '加载详情失败')
  if (!got) return
  const d = got.data || {}
  const ackRes = await gate(noticePrint({id: row.id}), '打印计数失败')
  if (!ackRes) return
  const html = `<!doctype html><html><head><meta charset="utf-8"><title>${esc(d.noticeNo)}</title><style>
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
    ${COPIES.map((c, i) => `<div class="copy"><h1>病危（重）通知暨告知书签收回执</h1>
      <div class="sub">单号：${esc(d.noticeNo)}</div>
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
  await Promise.all([loadDicts(), loadDoctors()])
  await reloadAll()
})
</script>
