<template>
  <div class="compliance-audit-view" data-testid="compliance-audit-view">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="审核类型">
            <el-select v-model="query.auditType" clearable data-testid="ca-filter-type" placeholder="全部审核类型"
                       style="width:150px">
              <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="风险等级">
            <el-select v-model="query.riskLevel" clearable data-testid="ca-filter-risk" placeholder="全部风险等级"
                       style="width:150px">
              <el-option v-for="d in riskDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="ca-search-btn" type="primary" @click="query.pageNum = 1; loadRows()">
              查询
            </el-button>
            <el-button :icon="Refresh" data-testid="ca-reset-btn" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <span class="muted">本页只回看已跑过的审核结果；要执行审核去「医保结算清单」</span>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="ca-table" stripe>
        <el-table-column label="审核单号" prop="auditNo" width="180"/>
        <el-table-column label="结算清单号" prop="settlementNo" width="180"/>
        <el-table-column label="审核类型" width="120">
          <template #default="{ row }">{{ typeText(row.auditType) }}</template>
        </el-table-column>
        <el-table-column label="风险等级" width="110">
          <template #default="{ row }">
            <el-tag :data-testid="`ca-risk-${row.auditNo}`" :type="riskTagType(row.riskLevel)" size="small">
              {{ row.riskLevelText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险分" prop="riskScore" width="80"/>
        <el-table-column label="规则命中" width="170">
          <template #default="{ row }">
            命中 <b class="hit">{{ row.hitCount ?? '—' }}</b>
            / 通过 <b class="pass">{{ row.passCount ?? '—' }}</b>
            / 未评估 <b class="na">{{ row.naCount ?? '—' }}</b>
          </template>
        </el-table-column>
        <el-table-column label="结论" min-width="200" prop="conclusion" show-overflow-tooltip/>
        <el-table-column label="审核人" prop="auditBy" width="100"/>
        <el-table-column label="审核时间" width="150">
          <template #default="{ row }">{{ fmtTime(row.auditTime) }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="80">
          <template #default="{ row }">
            <el-button :data-testid="`ca-view-${row.auditNo}`" :icon="View" link type="primary"
                       @click.stop="openDetail(row)">详情
            </el-button>
          </template>
        </el-table-column>
        <template #empty><span class="muted">还没有合规审核记录 —— 到「医保结算清单」先维护编码明细，再执行合规审核</span>
        </template>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :page-sizes="PAGE_SIZES"
                       :total="total" data-testid="ca-pagination"
                       layout="total, sizes, prev, pager, next" @current-change="loadRows"
                       @size-change="query.pageNum = 1; loadRows()"/>
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" :title="`合规审核 ${detail.auditNo || ''}`" direction="rtl" size="72%">
      <div v-loading="detailLoading" class="detail-wrap">
        <el-descriptions :column="2" border data-testid="ca-detail-head" size="small">
          <el-descriptions-item label="结算清单号">{{ detail.settlementNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核类型">{{ typeText(detail.auditType) }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="riskTagType(detail.riskLevel)" size="small">{{ detail.riskLevelText }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="风险分">{{ detail.riskScore ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="DRG 分组">{{ detail.drgCode || '未分组' }}</el-descriptions-item>
          <el-descriptions-item label="DRG 权重">{{ detail.drgWeight ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="实际费用（元）">{{ money(detail.actualCost) }}</el-descriptions-item>
          <el-descriptions-item label="支付标准（元）">{{ money(detail.payStandard) }}</el-descriptions-item>
          <el-descriptions-item label="费用倍率">{{ detail.costRatio ?? '—' }}</el-descriptions-item>
          <el-descriptions-item label="审核时间">{{ fmtTime(detail.auditTime) }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="审核结论">{{ detail.conclusion || '—' }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="备注">{{ detail.remark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="sec">规则判定明细（{{ detail.hitItemCount ?? items.length }} 条命中，共 {{ items.length }} 条）</h4>
        <el-table :data="items" border data-testid="ca-detail-items" max-height="420" size="small" stripe>
          <el-table-column label="规则" prop="ruleCode" width="70"/>
          <el-table-column label="规则名称" min-width="180" prop="ruleName" show-overflow-tooltip/>
          <el-table-column label="分组" prop="ruleGroupText" width="90"/>
          <el-table-column label="判定" width="90">
            <template #default="{ row }">
              <el-tag :type="itemResultTagType(row.result)" size="small">{{ row.resultText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="风险" prop="riskLevelText" width="80"/>
          <el-table-column label="命中对象" prop="targetName" show-overflow-tooltip width="140"/>
          <el-table-column label="判定依据" min-width="240" prop="evidence" show-overflow-tooltip/>
          <el-table-column label="整改建议" min-width="200" prop="suggestion" show-overflow-tooltip/>
        </el-table>
        <p class="muted mt">「不适用」= 缺依据没法评估，不是「没 problem」，凑数当通过会让结论虚高。</p>

        <h4 class="sec">AI 证据判定</h4>
        <div v-if="hitItems.length" class="flex items-center gap-3 mb-2">
          <el-button :loading="aiEvidenceLoading" data-testid="ca-ai-evidence-btn" size="small" type="primary"
                     @click="runAiEvidence">
            AI 证据判定
          </el-button>
          <span class="muted">模型逐条阅读病历证据判「支持 / 反驳 / 证据不足」，仅提示人工复核，不改变规则结论</span>
        </div>
        <el-alert
            v-if="aiEvidence?.degraded"
            :closable="false" :title="`AI 证据判定暂不可用：${aiEvidence.degradeReason || '原因未知'}，规则结论以人工核对为准`" class="mb-2" data-testid="ca-ai-evidence-degraded"
            show-icon
            type="warning"/>
        <el-table
            v-if="aiEvidence && !aiEvidence.degraded"
            :data="aiEvidence.judgments" border data-testid="ca-ai-evidence-table" max-height="300" size="small">
          <el-table-column label="规则" prop="ruleCode" width="70"/>
          <el-table-column label="规则名称" min-width="160" prop="ruleName" show-overflow-tooltip/>
          <el-table-column label="证据判定" width="110">
            <template #default="{ row }">
              <el-tag :type="verdictTagType(row.verdict)" size="small">{{ row.verdictText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="理由" min-width="240" prop="reason" show-overflow-tooltip/>
          <el-table-column label="原文引用" min-width="200" prop="quote" show-overflow-tooltip/>
          <template #empty><span class="muted">模型未给出判定</span></template>
        </el-table>
        <p v-if="aiEvidence && !aiEvidence.degraded && aiEvidence.overall" class="muted mt">总评：{{
            aiEvidence.overall
          }}</p>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
/**
 * 医保合规审核台账（菜单 1012 / 路由 /compliance-audit，菜单化见 sql/165）
 *
 * 「医保结算清单」页里已经能挂单执行自查，但跑完的审核记录**没有地方回看**：
 * biz_compliance_audit 4 条主表、biz_compliance_audit_item 72 行规则明细，全在库里躺着。
 * 台账是「医保局来查时我能拿出什么东西」的答案，缺了这个入口等于自查自嗨。
 *
 * 三条不妥协的展示纪律：
 *  1. 结论/等级/数量一律取后端VO 的 **Text 与 count**（riskScore、hitCount、costRatio 都是服务端算的），
 *     前端不重算、不补零、不把 null 显示成 0。
 *  2. 规则明细的 result 是**三态**（1命中/2通过/3不适用），必须渲染后端 resultText ——
 *     把「不适用（缺依据未评估）」显示成「通过」是最危险的错：它让结论看起来比实际可信。
 *  3. 本页只读。执行审核（跑规则）在「医保结算清单」里按键触发，这里只回看结果。
 */
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Refresh, Search, View} from '@element-plus/icons-vue'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {getDictDataMapList} from '@/api/system'
import {DICT_TYPE} from '@/lib/dict-cache'
import {dictLabelText} from '@/lib/utils'
import {getComplianceAuditDetail, getComplianceAuditList} from '@/api/compliance'
import {judgeInsuranceEvidence} from '@/api/ai'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—')
const money = (v) => (v === null || v === undefined || v === '' ? '—' : Number(v).toFixed(2))

// ---------------- 字典 ----------------
const typeDict = ref([])
const riskDict = ref([])
const resultDict = ref([])
const targetDict = ref([])
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList([
      DICT_TYPE.COMPLIANCE_AUDIT_TYPE, DICT_TYPE.COMPLIANCE_RISK_LEVEL,
      DICT_TYPE.COMPLIANCE_ITEM_RESULT, DICT_TYPE.COMPLIANCE_TARGET_TYPE,
    ].join(','))
    const d = res?.data || {}
    typeDict.value = d[DICT_TYPE.COMPLIANCE_AUDIT_TYPE] || []
    riskDict.value = d[DICT_TYPE.COMPLIANCE_RISK_LEVEL] || []
    resultDict.value = d[DICT_TYPE.COMPLIANCE_ITEM_RESULT] || []
    targetDict.value = d[DICT_TYPE.COMPLIANCE_TARGET_TYPE] || []
  } catch (e) {
    console.error('加载合规字典失败', e)
  }
}
const typeText = (v) => dictLabelText(typeDict.value, v)
// 0-未发现 1-提示 2-关注 3-高危：0 不是"错"，是"这次没查出问题"，要显示成正常色
const riskTagType = (v) => ({0: 'success', 1: 'info', 2: 'warning', 3: 'danger'}[v] || 'info')
// 三态：命中红 / 通过绿 / 不适用灰 —— 缺依据的必须一眼看出是灰的
const itemResultTagType = (v) => ({1: 'danger', 2: 'success', 3: 'info'}[v] || 'info')

// ---------------- 列表 ----------------
const loading = ref(false)
const rows = ref([])
const total = ref(0)
const query = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, riskLevel: null, auditType: null})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()
const loadRows = async () => {
  loading.value = true
  try {
    const res = await getComplianceAuditList({
      pageNum: query.pageNum,
      pageSize: query.pageSize,
      riskLevel: query.riskLevel === null || query.riskLevel === '' ? undefined : Number(query.riskLevel),
      auditType: query.auditType === null || query.auditType === '' ? undefined : Number(query.auditType),
    })
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = res.data?.total || 0
    } else ElMessage.error(res.message || '加载合规审核记录失败')
  } catch (e) {
    console.error(e);
    ElMessage.error('加载合规审核记录失败')
  } finally {
    loading.value = false
  }
}
const resetQuery = () => {
  Object.assign(query, {riskLevel: null, auditType: null, pageNum: 1});
  loadRows()
}

// ---------------- 详情（主表 + 规则明细 + 诊断/手术依据核对） ----------------
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({})
const items = ref([])
const openDetail = async (row) => {
  detail.value = row
  items.value = []
  aiEvidence.value = null
  detailVisible.value = true
  detailLoading.value = true
  try {
    const res = await getComplianceAuditDetail(String(row.id))
    if (res.code === 200) {
      detail.value = res.data || row
      items.value = res.data?.items || []
    } else ElMessage.error(res.message || '加载审核详情失败')
  } catch (e) {
    console.error(e);
    ElMessage.error('加载审核详情失败')
  } finally {
    detailLoading.value = false
  }
}

// ---------------- AI 证据判定（G-07） ----------------
// 模型逐条读病历证据，判「支持/反驳/证据不足」；只读计算不写库，不改规则结论。
// degraded=true 时judgments 为空 —— 规则判定依据照常可见，警示必显，不许把「无判定」当「无风险」。
const hitItems = computed(() => items.value.filter(i => i.result === 1))
const aiEvidence = ref(null)
const aiEvidenceLoading = ref(false)
const runAiEvidence = async () => {
  if (!detail.value?.id) return
  aiEvidenceLoading.value = true
  aiEvidence.value = null
  try {
    const res = await judgeInsuranceEvidence({auditId: detail.value.id})
    if (res.code === 200) {
      aiEvidence.value = res.data
      if (res.data?.degraded) ElMessage.warning(`AI 证据判定暂不可用（${res.data.degradeReason || '原因未知'}），请人工核对证据`)
    } else ElMessage.error(res.message || 'AI 证据判定失败')
  } catch (e) {
    console.error(e);
    ElMessage.error('AI 证据判定失败')
  } finally {
    aiEvidenceLoading.value = false
  }
}
const verdictTagType = (v) => (v === 1 ? 'danger' : v === 2 ? 'success' : 'info')

onMounted(async () => {
  await loadDicts()
  await loadRows()
})
</script>

<style scoped>
.muted {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.mt {
  margin-top: 8px;
}

.hit {
  color: var(--el-color-danger);
}

.pass {
  color: var(--el-color-success);
}

.na {
  color: var(--el-text-color-secondary);
}

.sec {
  margin: 14px 0 8px;
  font-size: 14px;
}

.detail-wrap {
  padding-bottom: 20px;
}
</style>
