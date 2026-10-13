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
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="lv-table" stripe>
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
        <el-table-column fixed="right" label="操作" width="300">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click.stop="showDetail(row)">详情
            </el-button>
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
    <InpatientLeaveEdit v-model="editVisible" :base="editBase" :dict-relation="dict.relation" :dict-type="dict.type"
                        :inpatients="inpatients" :row="editRow" @saved="reloadAll"/>

    <!-- 审批弹框（批准即医师电子签名 / 拒绝必填理由） -->
    <InpatientLeaveApprove v-model="approveVisible" :row="approveRow" @saved="reloadAll"/>

    <!-- 登记离院（患方承诺三要素 + 手写签名板） -->
    <InpatientLeaveDepart v-model="leaveVisible" :dict-relation="dict.relation" :row="leaveRow" @saved="reloadAll"/>

    <!-- 销假弹框 -->
    <InpatientLeaveReturn v-model="returnVisible" :row="returnRow" @saved="reloadAll"/>

    <!-- 取消弹框 -->
    <el-dialog v-model="cxVisible" data-testid="lv-cancel-dialog" title="取消请假单" width="480px">
      <p class="mb-2 text-sm text-slate-500">
        仅「待审批/已批准」可取消；已离院的单不能取消（人已经出去了，事实不能蒸发），请等患者返回后销假。</p>
      <el-input v-model="cx.cancelReason" :rows="3" data-testid="lv-cancel-reason" maxlength="500" placeholder="取消原因（必填：写清为什么取消）"
                show-word-limit type="textarea"/>
      <template #footer>
        <el-button :loading="cxSaving" data-testid="lv-cancel-submit" type="danger" @click="doCancel">确认取消
        </el-button>
        <el-button data-testid="lv-cancel-cancel" @click="cxVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 超期处置弹框 -->
    <InpatientLeaveContact v-model="contactVisible" :dict-contact="dict.contact" :dict-report="dict.report"
                           :row="contactRow" @saved="reloadAll"/>

    <!-- 详情（只读，行点击打开） -->
    <InpatientLeaveDetail v-model="detailVisible" :dict-contact="dict.contact" :dict-relation="dict.relation"
                          :dict-report="dict.report" :dict-status="dict.status" :dict-type="dict.type" :id="detailId"/>
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
import InpatientLeaveApprove from './InpatientLeaveApprove.vue'
import InpatientLeaveContact from './InpatientLeaveContact.vue'
import InpatientLeaveDepart from './InpatientLeaveDepart.vue'
import InpatientLeaveDetail from './InpatientLeaveDetail.vue'
import InpatientLeaveEdit from './InpatientLeaveEdit.vue'
import InpatientLeaveReturn from './InpatientLeaveReturn.vue'
import {
  getLeaveBase,
  getLeaveById,
  getLeaveInpatients,
  getLeaveListPage,
  getLeaveStats,
  leaveCancel,
  leavePrint,
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
const statusTag = (s) => (Number(s) === NS.PENDING ? 'warning' : Number(s) === NS.APPROVED ? 'primary' : Number(s) === NS.LEFT ? 'danger' : Number(s) === NS.RETURNED ? 'success' : 'info')

const gate = async (p, msg) => {
  try {
    const res = await p
    if (res.code === 200) return res
    ElMessage.error(res.message || msg)
    return null
  } catch (e) {
    
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
const editVisible = ref(false)
const editRow = ref(null)
const editBase = ref(null)
const openForm = (row, base) => {
  editRow.value = row || null
  editBase.value = base || null
  editVisible.value = true
}

// ---------------- 审批（批准即电子签名 / 拒绝必填理由） ----------------
const approveVisible = ref(false)
const approveRow = ref(null)
const openApprove = (row) => {
  approveRow.value = row
  approveVisible.value = true
}

// ---------------- 登记离院（患方承诺三要素 + 手写签名板） ----------------
const leaveVisible = ref(false)
const leaveRow = ref(null)
const openLeave = (row) => {
  leaveRow.value = row
  leaveVisible.value = true
}

// ---------------- 返回销假 ----------------
const returnVisible = ref(false)
const returnRow = ref(null)
const openBack = (row) => {
  returnRow.value = row
  returnVisible.value = true
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
const contactVisible = ref(false)
const contactRow = ref(null)
const openContact = (row) => {
  contactRow.value = row
  contactVisible.value = true
}

// ---------------- 详情（只读） ----------------
const detailVisible = ref(false)
const detailId = ref(null)
const showDetail = (row) => {
  detailId.value = row.id
  detailVisible.value = true
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
