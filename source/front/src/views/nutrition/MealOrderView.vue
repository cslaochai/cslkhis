<template>
  <div data-testid="meal-order-view">
    <el-card shadow="never" class="stat-card">
      <div class="stat-items">
        <div class="stat-item">
          <div class="stat-value" data-testid="ov-meal-count">{{ overview.todayMealCount ?? '-' }}</div>
          <div class="stat-label">今日订餐总条数</div>
          <div class="stat-sub">含已取消，签收率另算</div>
        </div>
        <div class="stat-item">
          <div class="stat-value" :class="{ 'stat-value-danger': (overview.todayMealPendingCount ?? 0) > 0 }"
               data-testid="ov-meal-pending">
            {{ overview.todayMealPendingCount ?? '-' }}
          </div>
          <div class="stat-label">今日未签收到位</div>
          <div class="stat-sub">已签收 {{ overview.todayMealSignedCount ?? 0 }} 条</div>
        </div>
        <div class="stat-item">
          <div class="stat-value"
               :class="{ 'stat-value-warn': Number(overview.todayMealSignRate) < STATS_TARGETS.mealSignRate }"
               data-testid="ov-meal-rate">
            {{ overview.todayMealSignRate ?? '0.00' }}%
          </div>
          <div class="stat-label">今日签收率（目标 ≥{{ STATS_TARGETS.mealSignRate }}%）</div>
          <div class="stat-sub">按「已签收 ÷ 应送」现算，不落库</div>
        </div>
      </div>
    </el-card>

    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="就餐日期">
            <div data-testid="filter-meal-date">
              <el-date-picker v-model="query.mealDate" type="date" value-format="YYYY-MM-DD" placeholder="就餐日期"
                              :clearable="false" style="width: 150px"/>
            </div>
          </el-form-item>
          <el-form-item label="病区">
            <div data-testid="filter-ward">
              <el-select v-model="query.wardId" placeholder="病区" clearable style="width: 150px">
                <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="餐次">
            <div data-testid="filter-meal-type">
              <el-select v-model="query.mealType" placeholder="餐次" clearable style="width: 110px">
                <el-option v-for="(t, code) in MEAL_TYPE_TEXT" :key="code" :label="t" :value="Number(code)"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="配餐状态">
            <div data-testid="filter-status">
              <el-select v-model="query.deliverStatus" placeholder="配餐状态" clearable style="width: 130px">
                <el-option v-for="(t, code) in MEAL_STATUS_TEXT" :key="code" :label="t" :value="Number(code)"/>
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="姓名 / 患者编号 / 住院号 / 订餐单号" clearable
                      style="width: 240px" data-testid="filter-keyword"/>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" data-testid="btn-query" @click="onQuery">查询</el-button>
            <el-button data-testid="btn-reset" @click="onReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：批量推进/退订与生成餐单统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:meal:status'" :disabled="!selection.length" data-testid="btn-batch-advance"
                     @click="openAdvance(selection, null)">
            批量推进（{{ selection.length }}）
          </el-button>
          <el-button v-perm="'ipd:meal:status'" :disabled="!selection.length" type="danger" plain
                     data-testid="btn-batch-cancel" @click="openCancelBatch">
            批量退订（{{ selection.length }}）
          </el-button>
          <el-button v-perm="'ipd:meal:generate'" type="primary" data-testid="btn-generate" @click="openGenDialog">
            生成餐单
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" stripe :max-height="tableMaxHeight" v-loading="loading" data-testid="meal-table"
                @selection-change="selection = $event" @row-click="onRowClick">
        <el-table-column type="selection" width="42"/>
        <el-table-column label="患者" min-width="130">
          <template #default="{ row }">
            <div>{{ row.patientName || '-' }}</div>
            <div class="muted">{{ row.admissionNo || row.patientNo || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="病区 / 床" min-width="130">
          <template #default="{ row }">
            <div>{{ row.wardName || '-' }}</div>
            <div class="muted">{{ row.bedNo ? row.bedNo + '床' : '' }}{{
                row.deptName ? ' · ' + row.deptName : ''
              }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="饮食" min-width="150">
          <template #default="{ row }">
            <div>{{ row.dietName || '-' }}</div>
            <div class="muted">{{ row.dietCode || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="日期 / 餐次" width="130">
          <template #default="{ row }">
            <div>{{ row.mealDate || '-' }}</div>
            <div data-testid="cell-meal-type">{{ row.mealTypeText || '-' }} × {{ row.quantity ?? 1 }}</div>
          </template>
        </el-table-column>
        <el-table-column label="配餐内容" min-width="170" show-overflow-tooltip>
          <template #default="{ row }">{{ row.dishContent || '—' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="MEAL_STATUS_TAG[row.deliverStatus]" disable-transitions data-testid="cell-meal-status">
              {{ row.deliverStatusText }}
            </el-tag>
            <div class="muted">{{ row.nextStatusText ? '下一步 ' + row.nextStatusText : '终态' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="配送 / 签收" width="165">
          <template #default="{ row }">
            <div class="muted">{{ row.deliverTime || '—' }}</div>
            <div class="muted">{{ row.signTime || '—' }}{{ row.signBy ? ' · ' + row.signBy : '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="来源" width="120">
          <template #default="{ row }">
            <div>{{ row.sourceText || '-' }}</div>
            <div class="muted">{{ row.mealNo || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
                v-if="Number(row.canAdvance) === 1"
                v-perm="'ipd:meal:status'"
                link
                type="primary"
                data-testid="btn-advance"
                @click.stop="openAdvance([row], null)">{{ row.nextStatusText || '推进' }}
            </el-button>
            <el-button
                v-if="Number(row.canCancel) === 1"
                v-perm="'ipd:meal:status'"
                link
                type="danger"
                data-testid="btn-cancel-meal"
                @click.stop="openAdvance([row], 4)">退订
            </el-button>
            <el-button
                v-if="row.deliverStatus < 2"
                v-perm="'ipd:meal:status'"
                link
                type="info"
                data-testid="btn-delete-meal"
                @click.stop="doDelete(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <!-- ⚠ layout 含 sizes 时 page-size 必须 v-model + @size-change，否则 EP 2.14 把整条分页渲染成 null -->
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            layout="total, sizes, prev, pager, next"
            :total="total"
            :page-sizes="PAGE_SIZES"
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            @current-change="loadList"
            @size-change="onSizeChange"/>
      </div>
    </el-card>

    <!-- 生成餐单 -->
    <el-dialog v-model="genVisible" title="按膳食方案生成餐单" width="520px" data-testid="gen-dialog">
      <el-form label-width="110px">
        <el-form-item label="就餐日期" required>
          <div data-testid="gen-meal-date" style="width: 100%">
            <el-date-picker v-model="genForm.mealDate" type="date" value-format="YYYY-MM-DD" :clearable="false"
                            style="width: 100%"/>
          </div>
        </el-form-item>
        <el-form-item label="病区范围">
          <div data-testid="gen-wards" style="width: 100%">
            <el-select v-model="genForm.wardIds" multiple filterable clearable placeholder="留空 = 全部病区"
                       style="width: 100%">
              <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="覆盖重生成">
          <el-switch v-model="genForm.overwrite" data-testid="gen-overwrite"/>
          <div class="muted hint">
            只重生成「未配送」的餐（待配餐/已配餐先物理删再插）。已配送、已签收是既成事实，这些人整天跳过并在结果里报数。
          </div>
        </el-form-item>
        <div class="form-tip">
          生成规则全在服务端：只取「执行中 + 口服 + 本人今天在院」的方案，按方案餐次拆成一日多条。
          同一个人同一餐只有一条（唯一键 人+日期+餐次），重复点不会产生第二份饭。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="genVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" data-testid="btn-do-generate" @click="doGenerate">生成</el-button>
      </template>
    </el-dialog>

    <!-- 状态推进 / 退订 -->
    <el-dialog v-model="advanceVisible" :title="advanceTitle" width="500px" data-testid="advance-dialog">
      <el-form label-width="110px">
        <el-form-item label="处理条数">
          <el-input :value="`${advanceIds.length} 条 ${targetStatusText ? '→ ' + targetStatusText : ''}`" disabled/>
        </el-form-item>
        <el-form-item v-if="advanceTarget === 1" label="当日食谱">
          <el-input v-model="advanceForm.dishContent" type="textarea" :rows="2" maxlength="500"
                    placeholder="如：糖尿病午餐 · 杂粮饭 + 清蒸鱼 + 炒青菜" data-testid="input-dish"/>
        </el-form-item>
        <el-form-item v-if="advanceTarget === 3" label="签收人" required>
          <el-input v-model="advanceForm.signBy" maxlength="50" placeholder="患者 / 家属 / 护士姓名"
                    data-testid="input-sign-by"/>
        </el-form-item>
        <el-form-item v-if="advanceTarget === 4" label="退订原因" required>
          <el-input v-model="advanceForm.cancelReason" type="textarea" :rows="2" maxlength="500"
                    placeholder="如：停餐 / 出院 / 拒餐 / 转科" data-testid="input-cancel-reason"/>
        </el-form-item>
        <div class="form-tip">
          批量是全成功或全不生效：一部分标成「已签收」另一部分报错，食堂就说不清今天到底送了几份。
          跨状态的勾选会被服务端按状态机拒掉，请一次勾选同一状态的餐。
        </div>
      </el-form>
      <template #footer>
        <el-button @click="advanceVisible = false">取消</el-button>
        <el-button :type="advanceTarget === 4 ? 'danger' : 'primary'" :loading="saving" data-testid="btn-do-advance"
                   @click="doAdvance">确认
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情（只读） -->
    <el-dialog v-model="detailVisible" title="订餐明细" width="600px" data-testid="meal-detail-dialog">
      <el-form :disabled="true" label-width="110px">
        <el-form-item label="患者">
          <el-input
              :value="`${detail.patientName || ''} · ${detail.admissionNo || ''}（${detail.wardName || ''}${detail.bedNo ? ' ' + detail.bedNo + '床' : ''}）`"/>
        </el-form-item>
        <el-form-item label="订餐单号">
          <el-input :value="detail.mealNo || '—'"/>
        </el-form-item>
        <el-form-item label="饮食 / 餐次">
          <el-input
              :value="`${detail.dietName || '—'}（${detail.dietCode || ''}）· ${detail.mealDate || ''} ${detail.mealTypeText || ''} × ${detail.quantity ?? 1}`"/>
        </el-form-item>
        <el-form-item label="配餐内容">
          <el-input :value="detail.dishContent || '—'" type="textarea" :rows="2"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-input
              :value="`${detail.deliverStatusText || '—'}${detail.nextStatusText ? ' · 下一步 ' + detail.nextStatusText : ' · 终态'}`"/>
        </el-form-item>
        <el-form-item label="配餐时间">
          <el-input :value="detail.prepareTime || '—'"/>
        </el-form-item>
        <el-form-item label="配送">
          <el-input :value="`${detail.deliverTime || '—'}${detail.deliverByName ? ' · ' + detail.deliverByName : ''}`"/>
        </el-form-item>
        <el-form-item label="签收">
          <el-input :value="`${detail.signTime || '—'}${detail.signBy ? ' · ' + detail.signBy : ''}`"/>
        </el-form-item>
        <el-form-item label="退订">
          <el-input :value="`${detail.cancelTime || '—'}${detail.cancelReason ? ' · ' + detail.cancelReason : ''}`"/>
        </el-form-item>
        <el-form-item label="来源 / 备注">
          <el-input :value="`${detail.sourceText || '—'}${detail.remark ? ' · ' + detail.remark : ''}`" type="textarea"
                    :rows="2"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {useRoute} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {
  deleteMealOrder,
  generateMealOrder,
  getDietWardSelectList,
  listMealOrderPage,
  updateMealStatus,
} from '@/api/diet'
import {getNutritionOverview} from '@/api/nutrition'
import {MEAL_STATUS_TAG, MEAL_STATUS_TEXT, MEAL_TYPE_TEXT, STATS_TARGETS} from '@/lib/nutrition'

const route = useRoute()

const todayStr = () => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const overview = ref({})
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const selection = ref([])

const query = reactive({
  mealDate: todayStr(),
  wardId: null,
  mealType: null,
  deliverStatus: null,
  dietPlanId: null,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
})

// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

async function loadOverview() {
  try {
    const res = await getNutritionOverview()
    overview.value = res?.data || {}
  } catch (e) {
    overview.value = {}
  }
}

async function loadList() {
  loading.value = true
  try {
    const res = await listMealOrderPage({...query})
    rows.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (e) {
    ElMessage.error(e?.message || '查询失败')
  } finally {
    loading.value = false
  }
}

function onQuery() {
  query.pageNum = 1
  loadList()
}

function onReset() {
  query.mealDate = todayStr()
  query.wardId = null
  query.mealType = null
  query.deliverStatus = null
  query.dietPlanId = null
  query.keyword = ''
  query.pageNum = 1
  loadList()
}

function onSizeChange() {
  query.pageNum = 1
  loadList()
}

const wards = ref([])

async function loadWards() {
  try {
    const res = await getDietWardSelectList()
    wards.value = res?.data || []
  } catch (e) {
    wards.value = []
  }
}

// ---------------- 生成餐单 ----------------
const genVisible = ref(false)
const saving = ref(false)
const genForm = reactive({mealDate: tomorrowStr(), wardIds: [], overwrite: false})

function tomorrowStr() {
  const d = new Date(Date.now() + 86400000)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function openGenDialog() {
  genForm.mealDate = tomorrowStr()
  genForm.wardIds = query.wardId ? [query.wardId] : []
  genForm.overwrite = false
  genVisible.value = true
}

async function doGenerate() {
  if (!genForm.mealDate) {
    ElMessage.warning('请选择就餐日期');
    return
  }
  saving.value = true
  try {
    const res = await generateMealOrder({
      mealDate: genForm.mealDate,
      wardIds: genForm.wardIds.length ? genForm.wardIds : null,
      overwrite: genForm.overwrite,
    })
    const vo = res?.data || {}
    ElMessage.success(`${vo.message || ''}（扫到方案 ${vo.planCount ?? 0} 个，生成 ${vo.generatedCount ?? 0} 条，跳过 ${vo.skippedCount ?? 0} 人）`)
    genVisible.value = false
    query.mealDate = genForm.mealDate
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '生成失败')
  } finally {
    saving.value = false
  }
}

// ---------------- 状态机推进 ----------------
const advanceVisible = ref(false)
const advanceIds = ref([])
const advanceTarget = ref(null)
const advanceForm = reactive({dishContent: '', signBy: '', cancelReason: ''})

const targetStatusText = computed(() => MEAL_STATUS_TEXT[advanceTarget.value] || '')
const advanceTitle = computed(() => (advanceTarget.value === 4 ? '退订餐单' : `推进到「${targetStatusText.value}」`))

/** 单条推进取后端算好的下一步；批量要求勾选的餐同一状态，否则服务端按状态机整批拒 */
function openAdvance(list, forcedTarget) {
  if (!list.length) {
    ElMessage.warning('请先勾选订餐明细');
    return
  }
  const statuses = [...new Set(list.map((r) => r.deliverStatus))]
  if (statuses.length > 1 && list.length > 1) {
    ElMessage.warning('批量推进只能勾选同一状态的餐（现在是 ' + statuses.map((s) => MEAL_STATUS_TEXT[s]).join('、') + '）')
    return
  }
  if (forcedTarget === 4) {
    if (Number(list[0].canCancel) !== 1) {
      ElMessage.warning('已签收或已取消的餐不能再退订');
      return
    }
    advanceTarget.value = 4
  } else {
    if (Number(list[0].canAdvance) !== 1) {
      ElMessage.warning('这批餐已经是终态，不能再推进');
      return
    }
    advanceTarget.value = statuses[0] + 1
  }
  advanceIds.value = list.map((r) => r.id)
  advanceForm.dishContent = list[0].dishContent || ''
  advanceForm.signBy = ''
  advanceForm.cancelReason = ''
  advanceVisible.value = true
}

function openCancelBatch() {
  openAdvance(selection.value, 4)
}

async function doAdvance() {
  const payload = {ids: advanceIds.value, deliverStatus: advanceTarget.value}
  if (advanceTarget.value === 1) {
    payload.dishContent = advanceForm.dishContent.trim() || null
  } else if (advanceTarget.value === 3) {
    if (!advanceForm.signBy.trim()) {
      ElMessage.warning('签收人不能为空（谁收到饭要落名）');
      return
    }
    payload.signBy = advanceForm.signBy.trim()
  } else if (advanceTarget.value === 4) {
    if (!advanceForm.cancelReason.trim()) {
      ElMessage.warning('退订原因不能为空');
      return
    }
    payload.cancelReason = advanceForm.cancelReason.trim()
  }
  saving.value = true
  try {
    const res = await updateMealStatus(payload)
    ElMessage.success(`已更新 ${res?.data ?? 0} 条`)
    advanceVisible.value = false
    selection.value = []
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '操作失败')
  } finally {
    saving.value = false
  }
}

async function doDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除订餐 ${row.mealNo || ''}（${row.patientName || ''} ${row.mealTypeText || ''}）？`, '删除确认', {
      type: 'warning',
    })
  } catch (e) {
    return
  }
  try {
    await deleteMealOrder(row.id)
    ElMessage.success('已删除')
    loadList()
    loadOverview()
  } catch (e) {
    ElMessage.error(e?.message || '删除失败')
  }
}

// ---------------- 详情 ----------------
const detailVisible = ref(false)
const detail = ref({})

/** 勾选列的点击也会冒泡成 row-click */
function onRowClick(row, column) {
  if (column && column.type === 'selection') return
  detail.value = row
  detailVisible.value = true
}

onMounted(() => {
  if (route.query.dietPlanId) {
    query.dietPlanId = String(route.query.dietPlanId)
    query.mealDate = null
  }
  loadOverview()
  loadList()
  loadWards()
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 12px;
}

.stat-items {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-item {
  flex: 1;
  min-width: 190px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 10px;
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
}

.stat-value-danger {
  color: #f56c6c;
}

.stat-value-warn {
  color: #e6a23c;
}

.stat-label {
  color: #606266;
  font-size: 12px;
  margin-top: 4px;
}

.stat-sub {
  color: #909399;
  font-size: 12px;
  margin-top: 2px;
}

.muted {
  color: #909399;
  font-size: 12px;
}

.hint {
  margin-left: 8px;
}

.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
</style>
