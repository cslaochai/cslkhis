<script setup>
/**
 * 手卫生依从性面板（biz_hand_hygiene_obs，菜单 617 /hand-hygiene）
 *
 * 抽出成组件的原因：同一块工作区出现第二个入口 ——
 * ①「院感监测」（菜单 614）的第三个页签（感控日常工作在这）；
 * ②「手卫生依从性」（菜单 617）独立页（月度下科室暗访时只看这块）。
 * 按 AGENTS.md「同一表单/工作区出现第 2 次就抽成共用组件」，两处共用本组件，
 * 避免「改了这一处、那一处口径又漂移」。
 *
 * 口径：
 *  - 观察记录**只增不改**：登记错了不许改数，只能重新登记一条（患者安全第一理由）。
 *  - 依从率 = SUM(执行数)/SUM(时机数)，一律**后端聚合**，前端不做任何占比自算
 *    （先聚总再相除 vs 先算每行占比再平均，是两个数，后者是错的）。
 *  - 60% 是国家等级评审的手卫生依从率达标线，低于它标黄；这个阈值服务端不判，只做展示。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import request from '@/api/request'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { getHandObsListPage, handObsAdd, getHandObsStats } from '@/api/infectionMonitor'

const pct = (v) => (v === null || v === undefined ? '—' : Number(v).toFixed(1))
const fmtTime = (t) => (t ? String(t).replace('T', ' ').slice(0, 16) : '—')
const fmtDate = (d) => (d ? String(d).slice(0, 10) : '—')

// ---------------- 字典（筛选用；表格里的对象文案取后端 obsObjectText） ----------------
const obsObjectDict = ref([])
const loadDicts = async () => {
  try {
    const r = await getDictDataMapList(DICT_TYPE.HAND_OBS_OBJECT)
    obsObjectDict.value = r?.data?.[DICT_TYPE.HAND_OBS_OBJECT] || []
  } catch (e) { console.error('加载手卫生字典失败', e) }
}

// ---------------- 统计（后端聚合） ----------------
const stats = ref({})
const loadStats = async () => {
  try {
    const res = await getHandObsStats({})
    if (res.code === 200) stats.value = res.data || {}
  } catch (e) { console.error(e) }
}

// ---------------- 观察记录列表 ----------------
const query = reactive({ pageNo: 1, pageSize: DEFAULT_PAGE_SIZE, deptId: null, obsObject: null, obsDateStart: '', obsDateEnd: '' })
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const loadRows = async () => {
  loading.value = true
  try {
    const res = await getHandObsListPage({
      ...query,
      deptId: query.deptId || undefined,
      obsObject: query.obsObject || undefined,
      obsDateStart: query.obsDateStart || undefined,
      obsDateEnd: query.obsDateEnd || undefined,
    })
    if (res.code === 200) { rows.value = res.data?.records || []; total.value = res.data?.total || 0 }
    else ElMessage.error(res.message || '加载观察记录失败')
  } catch (e) { console.error(e); ElMessage.error('加载观察记录失败') } finally { loading.value = false }
}
const resetQuery = () => {
  Object.assign(query, { deptId: null, obsObject: null, obsDateStart: '', obsDateEnd: '', pageNo: 1 })
  loadRows()
}

// ---------------- 登记（只增） ----------------
const openVisible = ref(false)
const form = reactive({ obsDate: '', deptId: null, deptName: '', obsObject: null, opportunityCount: null, complyCount: null, remark: '' })
const deptOptions = ref([])
const loadDepts = async () => {
  if (deptOptions.value.length) return
  try {
    const res = await request.get('/system/department/selectList')
    if (res.code === 200) deptOptions.value = res.data || []
  } catch (e) { console.error(e) }
}
const onCreate = async () => {
  Object.assign(form, { obsDate: '', deptId: null, deptName: '', obsObject: null, opportunityCount: null, complyCount: null, remark: '' })
  await loadDepts()
  openVisible.value = true
}
const onDeptChange = (id) => {
  const d = deptOptions.value.find((x) => String(x.id) === String(id))
  form.deptName = d?.deptName || ''
}
const onSave = async () => {
  if (!form.obsDate) return ElMessage.warning('请选择观察日期')
  if (!form.deptId) return ElMessage.warning('请选择科室')
  if (!form.obsObject) return ElMessage.warning('请选择观察对象')
  if (!form.opportunityCount || form.opportunityCount < 1) return ElMessage.warning('时机数至少为 1')
  if (form.complyCount === null || form.complyCount === undefined) return ElMessage.warning('请填写执行数')
  if (Number(form.complyCount) > Number(form.opportunityCount)) return ElMessage.warning('执行数不能大于时机数')
  try {
    const res = await handObsAdd({ ...form, remark: form.remark || undefined })
    if (res.code === 200) {
      ElMessage.success(res.message || '观察记录已保存')
      openVisible.value = false
      await Promise.all([loadRows(), loadStats()])
    } else ElMessage.error(res.message || '保存失败')
  } catch (e) { console.error(e) }
}

onMounted(async () => {
  await loadDicts()
  await Promise.all([loadStats(), loadRows()])
})

defineExpose({ reload: () => Promise.all([loadStats(), loadRows()]) })
</script>

<template>
  <div class="hand-hygiene-panel">
    <div class="stat-row" data-testid="hand-stats">
      <div class="stat-item"><div class="stat-num">{{ stats.recordCount || 0 }}</div><div class="stat-label">观察记录（近30天）</div></div>
      <div class="stat-item"><div class="stat-num">{{ stats.totalOpportunity || 0 }}</div><div class="stat-label">手卫生时机</div></div>
      <div class="stat-item"><div class="stat-num">{{ stats.totalComply || 0 }}</div><div class="stat-label">实际执行</div></div>
      <div class="stat-item" :class="Number(stats.complyRate) >= 60 ? 'ok' : 'warn'">
        <div class="stat-num" data-testid="hand-comply-rate">{{ pct(stats.complyRate) }}%</div>
        <div class="stat-label">依从率（达标线 60%）</div>
      </div>
    </div>

    <div v-if="(stats.byObject || []).length" class="group-row">
      <div v-for="g in stats.byObject" :key="g.obsObject" class="stat-item mini">
        <b>{{ g.obsObjectText }}</b>：{{ g.comply }}/{{ g.opportunity }}（{{ pct(g.complyRate) }}%）
      </div>
    </div>

    <div class="toolbar">
      <el-select v-model="query.obsObject" placeholder="观察对象" clearable style="width:130px" data-testid="hand-filter-object"
                 @change="query.pageNo = 1; loadRows()">
        <el-option v-for="d in obsObjectDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
      </el-select>
      <el-select v-model="query.deptId" placeholder="被观察科室" clearable filterable style="width:170px" data-testid="hand-filter-dept"
                 @change="query.pageNo = 1; loadRows()">
        <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id" />
      </el-select>
      <el-date-picker v-model="query.obsDateStart" type="date" placeholder="观察日期从" value-format="YYYY-MM-DD" style="width:150px" />
      <el-date-picker v-model="query.obsDateEnd" type="date" placeholder="至" value-format="YYYY-MM-DD" style="width:130px" />
      <el-button :icon="Search" type="primary" data-testid="hand-search" @click="query.pageNo = 1; loadRows()">查询</el-button>
      <el-button :icon="Refresh" data-testid="hand-reset" @click="resetQuery">重置</el-button>
      <div class="spacer" />
      <el-button v-perm="'emr:infectionMonitor:add'" type="primary" data-testid="hand-create" @click="onCreate">观察登记</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe size="small" data-testid="hand-table">
      <el-table-column label="观察日期" width="110"><template #default="{ row }">{{ fmtDate(row.obsDate) }}</template></el-table-column>
      <el-table-column prop="deptName" label="科室" min-width="120" show-overflow-tooltip />
      <el-table-column label="对象" width="90"><template #default="{ row }">{{ row.obsObjectText }}</template></el-table-column>
      <el-table-column prop="opportunityCount" label="时机数" width="80" />
      <el-table-column prop="complyCount" label="执行数" width="80" />
      <el-table-column label="依从率" width="90"><template #default="{ row }">{{ pct(row.complyRate) }}%</template></el-table-column>
      <el-table-column prop="observerName" label="观察人" width="90" />
      <el-table-column label="登记时间" width="150"><template #default="{ row }">{{ fmtTime(row.obsTime) }}</template></el-table-column>
      <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
      <template #empty><span class="muted">近 30 天还没有观察记录 —— 点右上角「观察登记」下科室记录第一次</span></template>
    </el-table>
    <el-pagination v-model:current-page="query.pageNo" :page-size="query.pageSize" :total="total"
                   layout="total, prev, pager, next" style="margin-top:8px" @current-change="loadRows" />

    <el-dialog v-model="openVisible" title="手卫生观察登记" width="520px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="观察日期">
          <!-- el-date-picker 不透传原生 attr（data-testid 会丢），必须外包一层 div 承载 testid -->
          <div data-testid="hand-obs-date">
            <el-date-picker v-model="form.obsDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
          </div>
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="form.deptId" filterable style="width:100%" data-testid="hand-dept" @change="onDeptChange">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="观察对象">
          <el-select v-model="form.obsObject" filterable style="width:100%" data-testid="hand-object">
            <el-option v-for="d in obsObjectDict" :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue" />
          </el-select>
        </el-form-item>
        <el-form-item label="时机数"><el-input-number v-model="form.opportunityCount" :min="1" style="width:160px" data-testid="hand-opportunity" /></el-form-item>
        <el-form-item label="执行数"><el-input-number v-model="form.complyCount" :min="0" style="width:160px" data-testid="hand-comply" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="openVisible = false">取消</el-button>
        <el-button v-perm="'emr:infectionMonitor:add'" type="primary" data-testid="hand-save" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.stat-row { display: flex; gap: 8px; margin-bottom: 10px; }
.stat-row .stat-item { flex: 1; }
.group-row { display: flex; gap: 8px; margin-bottom: 10px; flex-wrap: wrap; }
.group-row .stat-item { flex: 1; min-width: 180px; }
.stat-item { background: var(--el-fill-color-light); border-radius: 6px; padding: 10px 14px; text-align: center; }
.stat-item.warn { background: var(--el-color-warning-light-9); }
.stat-item.ok { background: var(--el-color-success-light-9); }
.stat-item.mini { padding: 6px 10px; font-size: 13px; }
.stat-num { font-size: 22px; font-weight: 700; }
.stat-label { font-size: 12px; color: var(--el-text-color-secondary); }
.toolbar { display: flex; gap: 8px; align-items: center; margin-bottom: 10px; flex-wrap: wrap; }
.toolbar .spacer { flex: 1; }
.muted { color: var(--el-text-color-secondary); font-size: 12px; }
</style>
