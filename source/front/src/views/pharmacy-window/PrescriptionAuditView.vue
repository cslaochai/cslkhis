<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Search, Refresh, Check, Stamp, CircleClose, WarningFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPrescriptionListPage, auditPrescription, rationalCheckPrescriptions } from '@/api/doctor'
import { verifySignatureByBiz } from '@/api/signature'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

// 与后端 SignBizType.PRESCRIPTION 同值
const PRESCRIPTION_BIZ_TYPE = 4

const loading = ref(false)
const list = ref<any[]>([])
const keyword = ref('')
const unauditedOnly = ref(true)
const pagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

/** 合理用药核查结果，键为处方 id（字符串，雪花 id 不能转数字） */
const rationalMap = ref<Record<string, any>>({})

const detailVisible = ref(false)
const detailRow = ref<any>(null)
const verifying = ref(false)
const verifyResults = ref<any[]>([])

const rxStatusMap: Record<number, { label: string; color: string }> = {
  1: { label: '草稿', color: 'bg-slate-100 text-slate-600' },
  2: { label: '已提交', color: 'bg-amber-100 text-amber-700' },
  3: { label: '已审核', color: 'bg-emerald-100 text-emerald-700' },
  4: { label: '已发药', color: 'bg-blue-100 text-blue-700' },
  5: { label: '已取消', color: 'bg-red-100 text-red-500' },
  6: { label: '已退药', color: 'bg-red-100 text-red-500' },
  7: { label: '审方退回', color: 'bg-orange-100 text-orange-700' },
}

const statusText = (v: number) => rxStatusMap[v]?.label ?? (v == null ? '—' : `未知(${v})`)
const statusColor = (v: number) => rxStatusMap[v]?.color ?? 'bg-slate-100 text-slate-500'

const fmtTime = (v: string) => (v ? String(v).replace('T', ' ').slice(0, 19) : '—')

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    }
    if (keyword.value) params.keyword = keyword.value
    if (unauditedOnly.value) params.unauditedOnly = true
    const res = await getPrescriptionListPage(params)
    list.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
    await loadRational(list.value.map((r: any) => String(r.id)))
  } catch (error) {
    console.error('加载处方列表失败:', error)
  } finally {
    loading.value = false
  }
}

/**
 * 批量拉取本页处方的合理用药核查结果。
 * 结论全部由后端算（知识表比对 + 剂量上限），前端只负责展示，
 * 不复制任何判断 —— 闸在 `/prescription/audit` 里，这里只是提前把话说清楚。
 */
const loadRational = async (ids: string[]) => {
  if (!ids.length) {
    rationalMap.value = {}
    return
  }
  try {
    const res = await rationalCheckPrescriptions(ids)
    const map: Record<string, any> = {}
    for (const item of res.data || []) map[String(item.prescriptionId)] = item
    rationalMap.value = map
  } catch (error) {
    console.error('合理用药核查失败:', error)
    rationalMap.value = {}
  }
}

const rationalOf = (row: any) => rationalMap.value[String(row?.id)] || null
const hitsOf = (row: any) => rationalOf(row)?.hits || []
const blockedOf = (row: any) => rationalOf(row)?.blocked === true

const hitTypeText: Record<string, string> = {
  INTERACTION: '配伍',
  DOSE_SINGLE: '单次剂量',
  DOSE_DAILY: '日剂量',
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  keyword.value = ''
  unauditedOnly.value = true
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  loadData()
}

const handleCurrentChange = (val: number) => {
  pagination.value.pageNum = val
  loadData()
}

const openDetail = async (row: any) => {
  detailRow.value = row
  verifyResults.value = []
  detailVisible.value = true
}

/** 审方通过：确认后由后端签署药师签名并把处方置为「已审核」 */
const handleAudit = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `确认为处方 ${row.prescriptionNo} 签署审方意见？签名后内容即锁定，改处方需先作废该签名。`,
      '处方审核（通过）',
      {
        confirmButtonText: '确认审方并签名',
        cancelButtonText: '取消',
        inputPlaceholder: '审方意见（可留空）',
        inputType: 'textarea',
        type: 'warning',
      },
    )
    const res = await auditPrescription({ prescriptionId: row.id, auditResult: 1, auditOpinion: value || '' })
    ElMessage.success(`审方完成，已签署药师签名（${res.data?.auditBy || '当前药师'}）`)
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '审方失败')
    }
  }
}

/** 审方退回（L7）：原因必填；不产生药师签名，处方置「审方退回」等医生改方重提 */
const handleReturn = async (row: any) => {
  try {
    const { value } = await ElMessageBox.prompt(
      `退回处方 ${row.prescriptionNo}（${row.patientName}，医生 ${row.doctorName}）。退回后医生将收到站内信，改方重新提交后才能再次审方。`,
      '审方退回',
      {
        confirmButtonText: '确认退回',
        cancelButtonText: '取消',
        inputPlaceholder: '退回原因（必填，如：用法用量与诊断不符）',
        inputType: 'textarea',
        inputValidator: (v: string) => (v && v.trim() ? true : '退回原因不能为空'),
        type: 'warning',
      },
    )
    await auditPrescription({ prescriptionId: row.id, auditResult: 2, auditOpinion: value.trim() })
    ElMessage.success('已退回，医生将收到站内信通知')
    loadData()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退回失败')
    }
  }
}

/** 验证该处方全部签名的有效性（含已作废） */
const handleVerify = async () => {
  if (!detailRow.value) return
  verifying.value = true
  try {
    const res = await verifySignatureByBiz(PRESCRIPTION_BIZ_TYPE, detailRow.value.id)
    verifyResults.value = res.data || []
    if (!verifyResults.value.length) {
      ElMessage.warning('该处方没有任何签名记录')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '验签失败')
  } finally {
    verifying.value = false
  }
}

const verifyLevelClass = (level: number) => {
  if (level === 1) return 'text-emerald-600'
  if (level === 2) return 'text-amber-600'
  return 'text-red-600'
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="{ keyword, unauditedOnly }" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input
            v-model="keyword"
            placeholder="搜索处方号 / 患者姓名 / 患者号..."
            :prefix-icon="Search"
            class="!w-80"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="unauditedOnly" @change="handleSearch">只看未审方</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <p class="mt-2 text-xs text-slate-400">
        审方签名由当前登录药师签署，签名后处方内容即锁定；要改处方须先在「电子签名与时间戳」作废该签名。
      </p>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="list" v-loading="loading" stripe :max-height="tableMaxHeight" style="width: 100%">
        <el-table-column prop="prescriptionNo" label="处方号" width="170" class-name="font-mono" />
        <el-table-column prop="patientName" label="患者" width="100" />
        <el-table-column prop="deptName" label="科室" width="110" />
        <el-table-column prop="doctorName" label="开方医生" width="100" />
        <el-table-column label="药品数" width="80" align="center">
          <template #default="{ row }">
            <span class="font-medium">{{ row.drugCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100" align="right">
          <template #default="{ row }">
            <span class="font-medium text-red-600">¥{{ row.totalAmount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="处方状态" width="110" align="center">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.prescriptionStatus === 7 && row.returnReason"
              :content="`退回原因：${row.returnReason}`"
              placement="top"
            >
              <span :class="['inline-block rounded px-2 py-0.5 font-medium', statusColor(row.prescriptionStatus)]"
                    data-testid="l7-rx-status">
                {{ statusText(row.prescriptionStatus) }}<template v-if="row.returnCount > 1">·{{ row.returnCount }}次</template>
              </span>
            </el-tooltip>
            <span v-else :class="['inline-block rounded px-2 py-0.5 font-medium', statusColor(row.prescriptionStatus)]"
                  data-testid="l7-rx-status">
              {{ statusText(row.prescriptionStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="开方签名" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.doctorSignId" class="inline-flex items-center gap-1 text-emerald-600">
              <Stamp class="h-3.5 w-3.5" />已签
            </span>
            <span v-else class="text-slate-400">未签</span>
          </template>
        </el-table-column>
        <el-table-column label="审方签名" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.auditSignId" class="inline-flex items-center gap-1 text-emerald-600">
              <Stamp class="h-3.5 w-3.5" />已签
            </span>
            <span v-else class="text-amber-600">未签</span>
          </template>
        </el-table-column>
        <el-table-column label="合理用药" width="140" align="center">
          <template #default="{ row }">
            <el-tooltip v-if="blockedOf(row)" placement="top" :content="rationalOf(row).blockMessage || '存在禁忌配伍'">
              <span class="inline-flex items-center gap-1 rounded bg-red-100 px-2 py-0.5 font-medium text-red-700"
                    data-testid="kb-rx-block">
                <WarningFilled class="h-3.5 w-3.5" />禁忌·不可签发
              </span>
            </el-tooltip>
            <el-tooltip v-else-if="hitsOf(row).length" placement="top">
              <template #content>
                <p v-for="h in hitsOf(row)" :key="h.knowledgeId" class="text-xs leading-5">
                  {{ hitTypeText[h.hitType] || h.hitType }}：{{ h.message }}
                </p>
              </template>
              <span class="inline-flex items-center gap-1 rounded bg-amber-100 px-2 py-0.5 font-medium text-amber-700"
                    data-testid="kb-rx-hit">
                提示{{ hitsOf(row).length }}项
              </span>
            </el-tooltip>
            <span v-else class="text-slate-400" data-testid="kb-rx-clean">通过</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="!row.auditSignId && row.prescriptionStatus !== 7"
              v-perm="'pharmacy:prescriptionAudit:edit'"
              type="warning"
              link
              :icon="Check"
              data-testid="l7-rx-audit"
              @click="handleAudit(row)"
            >
              审方
            </el-button>
            <el-button
              v-if="!row.auditSignId && row.prescriptionStatus !== 7"
              v-perm="'pharmacy:prescriptionAudit:edit'"
              type="danger"
              link
              :icon="CircleClose"
              data-testid="l7-rx-return"
              @click="handleReturn(row)"
            >
              退回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="list.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无待审处方
      </div>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="PAGE_SIZES"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="处方详情" size="720px" destroy-on-close>
      <template v-if="detailRow">
        <div class="space-y-4">
          <div class="grid grid-cols-2 gap-4 rounded-lg border border-slate-200 p-4">
            <div>
              <p class="text-xs text-slate-400">患者</p>
              <p class="text-sm font-medium text-slate-700">{{ detailRow.patientName }}（{{ detailRow.patientNo }}）</p>
            </div>
            <div>
              <p class="text-xs text-slate-400">处方号</p>
              <p class="font-mono text-sm font-medium text-slate-700">{{ detailRow.prescriptionNo }}</p>
            </div>
            <div>
              <p class="text-xs text-slate-400">科室 / 开方医生</p>
              <p class="text-sm font-medium text-slate-700">{{ detailRow.deptName }} / {{ detailRow.doctorName }}</p>
            </div>
            <div>
              <p class="text-xs text-slate-400">处方状态</p>
              <span :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', statusColor(detailRow.prescriptionStatus)]">
                {{ statusText(detailRow.prescriptionStatus) }}
              </span>
            </div>
            <div>
              <p class="text-xs text-slate-400">临床诊断</p>
              <p class="text-sm font-medium text-slate-700">{{ detailRow.diagnosis || '—' }}</p>
            </div>
            <div>
              <p class="text-xs text-slate-400">金额</p>
              <p class="text-sm font-bold text-red-600">¥{{ detailRow.totalAmount || 0 }}</p>
            </div>
            <div v-if="detailRow.prescriptionStatus === 7" class="col-span-2 rounded border border-orange-200 bg-orange-50 p-2">
              <p class="text-xs text-orange-500">退回原因（已退回 {{ detailRow.returnCount || 1 }} 次）</p>
              <p class="text-sm text-orange-700">{{ detailRow.returnReason || '—' }}</p>
            </div>
          </div>

          <div class="rounded-lg border border-slate-200 p-4">
            <h4 class="mb-3 text-sm font-medium text-slate-700">签名</h4>
            <div class="grid grid-cols-2 gap-3 text-sm">
              <div>
                <p class="text-xs text-slate-400">开方医师签名</p>
                <p v-if="detailRow.doctorSignId" class="text-slate-700">已签 · {{ fmtTime(detailRow.doctorSignedTime) }}</p>
                <p v-else class="text-slate-400">未签</p>
              </div>
              <div>
                <p class="text-xs text-slate-400">审方药师签名</p>
                <p v-if="detailRow.auditSignId" class="text-slate-700">
                  已签 · {{ detailRow.auditBy || '' }} · {{ fmtTime(detailRow.auditSignedTime) }}
                </p>
                <p v-else class="text-amber-600">未签</p>
              </div>
            </div>
            <div class="mt-3">
              <el-button size="small" :loading="verifying" @click="handleVerify">验证签名有效性</el-button>
            </div>
            <div v-if="verifyResults.length" class="mt-3 space-y-2">
              <div v-for="v in verifyResults" :key="v.signId" class="rounded border border-slate-200 p-2 text-xs">
                <div class="flex items-center gap-2">
                  <span class="font-medium text-slate-700">{{ v.signSceneText }}</span>
                  <span class="text-slate-400">第 {{ v.chainNo }} 环</span>
                  <span class="text-slate-500">{{ v.signerName }}</span>
                  <span :class="verifyLevelClass(v.conclusionLevel)" class="font-medium">{{ v.conclusion }}</span>
                </div>
                <div class="mt-1 font-mono text-[11px] text-slate-400">
                  签名时摘要 {{ v.digestAtSign }} / 当前摘要 {{ v.digestNow || '（对象已不存在）' }}
                </div>
              </div>
            </div>
          </div>

          <div v-if="hitsOf(detailRow).length || blockedOf(detailRow)" class="rounded-lg border border-slate-200 p-4">
            <h4 class="mb-3 text-sm font-medium text-slate-700">合理用药核查</h4>
            <p v-if="blockedOf(detailRow)" class="mb-2 rounded border border-red-200 bg-red-50 p-2 text-xs text-red-700"
               data-testid="kb-drawer-block">
              {{ rationalOf(detailRow).blockMessage }}
            </p>
            <div v-for="h in hitsOf(detailRow)" :key="h.knowledgeId"
                 class="mb-2 rounded border border-slate-200 p-2 text-xs last:mb-0">
              <div class="flex items-center gap-2">
                <span :class="['rounded px-1.5 py-0.5 font-medium',
                      h.severity === 1 ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700']">
                  {{ h.severity === 1 ? '禁忌' : '慎用' }}
                </span>
                <span class="text-slate-500">{{ hitTypeText[h.hitType] || h.hitType }}</span>
                <span class="font-mono text-[11px] text-slate-400">条目 {{ h.knowledgeId }}</span>
              </div>
              <p class="mt-1 text-slate-700">{{ h.message }}</p>
              <p v-if="h.suggestion" class="mt-1 text-slate-500">建议：{{ h.suggestion }}</p>
            </div>
          </div>

          <div v-if="detailRow.details?.length">
            <h4 class="mb-2 text-sm font-medium text-slate-700">药品明细</h4>
            <el-table :data="detailRow.details" size="small" border>
              <el-table-column prop="drugName" label="药品名称" min-width="140" />
              <el-table-column prop="specification" label="规格" width="100" />
              <el-table-column prop="quantity" label="数量" width="70" align="center" />
              <el-table-column prop="unit" label="单位" width="60" align="center" />
              <el-table-column prop="frequency" label="频次" width="80" />
              <el-table-column prop="route" label="途径" width="80" />
              <el-table-column label="金额" width="90" align="right">
                <template #default="{ row }">¥{{ row.amount || 0 }}</template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </template>
    </el-drawer>
  </div>
</template>
