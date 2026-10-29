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
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="cn-table" stripe>
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
        <el-table-column fixed="right" label="操作" width="280">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click.stop="showDetail(row)">详情</el-button>
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

    <!-- 表单弹框（草稿）：填写/修改合并，表单在 CriticalNoticeEdit -->
    <CriticalNoticeEdit v-model="fVisible" :base="fBase" :dict="dict" :inpatients="inpatients" :row="fRow"
                        @saved="reloadAll"/>

    <!-- 签收弹框（家属手写签名板）：表单在 CriticalNoticeAck -->
    <CriticalNoticeAck v-model="ackVisible" :dict="dict" :row="ackRow" @saved="reloadAll"/>

    <!-- 作废弹框 -->
    <el-dialog v-model="vVisible" data-testid="cn-void-dialog" title="作废通知单" width="480px">
      <p class="mb-2 text-sm text-slate-500">{{
          vForm.noticeNo
        }}：作废不改内容、不删行，仅状态留痕；已签名锁定的须先在签名中心作废签名。</p>
      <el-input v-model="vForm.voidReason" :rows="3" data-testid="cn-void-reason" maxlength="500" placeholder="作废原因（必填：写清错在哪）"
                show-word-limit type="textarea"/>
      <template #footer>
        <el-button data-testid="cn-void-submit" type="danger" @click="doVoid">确认作废</el-button>
        <el-button data-testid="cn-void-cancel" @click="vVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 详情（只读，操作列「详情」打开）：按 id 自拉数据，见 CriticalNoticeDetail -->
    <CriticalNoticeDetail v-model="dvVisible" :dict="dict" :id="dvId"/>

    <!-- 软签二次确认：签发要落医师电子签名，由签发医师本人输入 PIN -->
    <SignPinDialog ref="pinDialogRef" v-model="pinDialogVisible"/>
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
import {NS, fmt, statusTag, signTagText, gate} from '@/lib/criticalNotice'
import SignPinDialog from '@/components/his/SignPinDialog.vue'
import CriticalNoticeEdit from './CriticalNoticeEdit.vue'
import CriticalNoticeAck from './CriticalNoticeAck.vue'
import CriticalNoticeDetail from './CriticalNoticeDetail.vue'
import {
  getNoticeBase,
  getNoticeById,
  getNoticeInpatients,
  getNoticeListPage,
  getNoticeStats,
  noticeIssue,
  noticePrint,
  noticeVoid,
} from '@/api/criticalNotice'

const COPIES = ['病历联（随病历存档）', '患方联（交家属留存）']

const esc = (v) => String(v ?? '').replace(/[&<>"]/g, (c) => ({
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;'
}[c]))

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

// ---------------- 表单（草稿） ----------------
const fVisible = ref(false)
const fRow = ref(null)
const fBase = ref(null)
// 软签二次确认：PIN 由签发医师本人输入，服务端校验通过才解密私钥
const pinDialogRef = ref(null)
const pinDialogVisible = ref(false)
const openForm = (row, base) => {
  fRow.value = row || null
  fBase.value = base || null
  fVisible.value = true
}

// ---------------- 签发 ----------------
const onIssue = async (row) => {
  // 签发要落医师电子签名：先取 PIN，取消则不签发
  const pin = await pinDialogRef.value.askPin()
  if (!pin) {
    ElMessage.info('已取消签名，通知单未签发')
    return
  }
  const res = await gate(noticeIssue({id: row.id, pin}), '签发失败')
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
const dvId = ref(null)
// 详情组件 CriticalNoticeDetail 打开时按 id 自拉数据，这里只传 id 并打开
const showDetail = (row) => {
  dvId.value = row.id
  dvVisible.value = true
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
  // 医生候选在 CriticalNoticeEdit 子组件内自加载；主页只展示后端返回的 doctorName，无需 loadDoctors
  await loadDicts()
  await reloadAll()
})
</script>
