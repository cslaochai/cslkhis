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

    <!-- 合规审核详情抽屉（拆到 ComplianceAuditDetail） -->
    <ComplianceAuditDetail v-model="detailVisible" :id="detailId" :row="detailRow"
                           :type-text="typeText" :risk-tag-type="riskTagType" :fmt-time="fmtTime"/>
  </div>
</template>

<script setup>
/**
 * 医保合规审核台账（菜单 1012 / 路由 /compliance-audit，菜单化见 sql/165）
 *
 * 「医保结算清单」页里已经能挂单执行自查，但跑完的审核记录**没有地方回看**：
 * biz_yb_compliance_audit 4 条主表、biz_yb_compliance_audit_item 72 行规则明细，全在库里躺着。
 * 台账是「医保局来查时我能拿出什么东西」的答案，缺了这个入口等于自查自嗨。
 *
 * 三条不妥协的展示纪律：
 *  1. 结论/等级/数量一律取后端VO 的 **Text 与 count**（riskScore、hitCount、costRatio 都是服务端算的），
 *     前端不重算、不补零、不把 null 显示成 0。
 *  2. 规则明细的 result 是**三态**（1命中/2通过/3不适用），必须渲染后端 resultText ——
 *     把「不适用（缺依据未评估）」显示成「通过」是最危险的错：它让结论看起来比实际可信。
 *  3. 本页只读。执行审核（跑规则）在「医保结算清单」里按键触发，这里只回看结果。
 */
import {onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Refresh, Search, View} from '@element-plus/icons-vue'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {getDictDataMapList} from '@/api/system'
import {DICT_TYPE} from '@/lib/dict-cache'
import {dictLabelText} from '@/lib/utils'
import {getComplianceAuditList} from '@/api/compliance'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import ComplianceAuditDetail from './ComplianceAuditDetail.vue'

const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—')

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
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    loading.value = false
  }
}
const resetQuery = () => {
  Object.assign(query, {riskLevel: null, auditType: null, pageNum: 1});
  loadRows()
}

// ---------------- 详情（主表 + 规则明细 + AI 证据判定已拆到 ComplianceAuditDetail） ----------------
const detailVisible = ref(false)
const detailId = ref('')
const detailRow = ref(null)
const openDetail = (row) => {
  detailId.value = String(row.id)
  detailRow.value = row
  detailVisible.value = true
}

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
