<script setup lang="ts">
/**
 * 设备管理（G22，菜单 902）
 *
 * 台账 = sys_equipment（49 号铺底 90 行）；本页聚焦维保/计量：
 * - 维保登记成功回写档案「最近维保日期」，下次维保日期 = 最近维保 + 维保周期（前端只展示，计算在后端 VO）
 * - 计量（强检/校准）有效期至过期即台账亮红
 * - 维保/计量记录录错可删（后端会按剩余记录重算最近维保日期）
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  equipmentListPage, getEquipmentDetail, maintainListPage, maintainCreate, maintainDelete,
  meteringListPage, meteringCreate, meteringDelete,
} from '@/api/equipment'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

// ---------------- 字典 ----------------
const statusDict = ref<any[]>([])
const categoryDict = ref<any[]>([])
const maintainTypeDict = ref<any[]>([])
const meteringTypeDict = ref<any[]>([])
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const categoryText = (v: any) => dictLabelText(categoryDict.value, v)
const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(
      `${DICT_TYPE.EQUIP_MAINTAIN_TYPE},${DICT_TYPE.EQUIP_METERING_TYPE},${DICT_TYPE.EQUIP_METERING_RESULT}`)
    maintainTypeDict.value = res?.data?.[DICT_TYPE.EQUIP_MAINTAIN_TYPE] || []
    meteringTypeDict.value = res?.data?.[DICT_TYPE.EQUIP_METERING_TYPE] || []
    // 设备类别/状态是 49 号铺底的老字典，不在 G22 段，单独取
    const res2: any = await getDictDataMapList('his_equipment_category,his_equipment_status')
    categoryDict.value = res2?.data?.['his_equipment_category'] || []
    statusDict.value = res2?.data?.['his_equipment_status'] || []
  } catch (e) { console.error('加载字典失败', e) }
}

// ---------------- 台账列表 ----------------
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({
  keyword: '', category: null as number | null, status: null as number | null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const loadList = async () => {
  loading.value = true
  try {
    const res: any = await equipmentListPage({
      keyword: query.keyword.trim() || undefined,
      category: query.category ?? undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    })
    if (res.code === 200) {
      rows.value = res.data?.records || []
      total.value = Number(res.data?.total || 0)
    } else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') } finally { loading.value = false }
}
const reset = () => {
  Object.assign(query, { keyword: '', category: null, status: null, pageNum: 1 })
  loadList()
}
const statusTag = (v: number) => ({ 1: 'success', 2: 'info', 3: 'warning', 4: 'danger' } as any)[v] || 'info'

// ---------------- 维保 / 计量登记 ----------------
const maintainVisible = ref(false)
const maintainForm = reactive({
  equipmentId: null as number | null, equipmentName: '', maintainType: 1,
  maintainDate: '', nextMaintainDate: '', cost: '', faultDesc: '', handleResult: '',
  maintainResult: 1, handlerName: '',
})
const openMaintain = (row: any) => {
  Object.assign(maintainForm, {
    equipmentId: Number(row.id), equipmentName: row.equipmentName, maintainType: 1,
    maintainDate: '', nextMaintainDate: '', cost: '', faultDesc: '', handleResult: '',
    maintainResult: 1, handlerName: '',
  })
  maintainVisible.value = true
}
const submitMaintain = async () => {
  if (!maintainForm.maintainDate) { ElMessage.warning('请选择维保日期'); return }
  try {
    const res: any = await maintainCreate({
      equipmentId: maintainForm.equipmentId,
      maintainType: maintainForm.maintainType,
      maintainDate: maintainForm.maintainDate,
      nextMaintainDate: maintainForm.nextMaintainDate || undefined,
      cost: maintainForm.cost || undefined,
      faultDesc: maintainForm.faultDesc || undefined,
      handleResult: maintainForm.handleResult || undefined,
      maintainResult: maintainForm.maintainResult,
      handlerName: maintainForm.handlerName || undefined,
    })
    if (res.code === 200) { ElMessage.success('维保登记成功'); maintainVisible.value = false; loadList() }
    else ElMessage.error(res.message || '登记失败')
  } catch (e) { console.error(e); ElMessage.error('登记失败') }
}

const meteringVisible = ref(false)
const meteringForm = reactive({
  equipmentId: null as number | null, equipmentName: '', meteringType: 1,
  meteringDate: '', validUntil: '', meteringResult: 1, certNo: '', agency: '',
})
const openMetering = (row: any) => {
  Object.assign(meteringForm, {
    equipmentId: Number(row.id), equipmentName: row.equipmentName, meteringType: 1,
    meteringDate: '', validUntil: '', meteringResult: 1, certNo: '', agency: '',
  })
  meteringVisible.value = true
}
const submitMetering = async () => {
  if (!meteringForm.meteringDate || !meteringForm.validUntil) {
    ElMessage.warning('请选择计量日期与有效期至')
    return
  }
  try {
    const res: any = await meteringCreate({
      equipmentId: meteringForm.equipmentId,
      meteringType: meteringForm.meteringType,
      meteringDate: meteringForm.meteringDate,
      validUntil: meteringForm.validUntil,
      meteringResult: meteringForm.meteringResult,
      certNo: meteringForm.certNo || undefined,
      agency: meteringForm.agency || undefined,
    })
    if (res.code === 200) { ElMessage.success('计量登记成功'); meteringVisible.value = false; loadList() }
    else ElMessage.error(res.message || '登记失败')
  } catch (e) { console.error(e); ElMessage.error('登记失败') }
}

// ---------------- 详情（最近维保/计量） ----------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  try {
    const res: any = await getEquipmentDetail(row.id)
    if (res.code === 200) { detail.value = res.data; detailVisible.value = true }
    else ElMessage.error(res.message || '查询失败')
  } catch (e) { console.error(e); ElMessage.error('查询失败') }
}

const fmtDate = (v: string) => (v ? String(v).slice(0, 10) : '—')
const meteringExpired = (row: any) => row.meteringExpired
// 本地时区当日（勿用 toISOString —— UTC+8 零点前会取到昨天）
const today = (() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})()
const dictVal = (dict: any[], v: any) => dictLabelText(dict, v)
const maintainTypeText = (row: any) => dictVal(maintainTypeDict.value, row.maintainType)
const meteringTypeText = (row: any) => dictVal(meteringTypeDict.value, row.meteringType)

const delMaintain = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该维保记录？删除后档案最近维保日期按剩余记录重算。', '删除确认', { type: 'warning' })
  } catch { return }
  try {
    const res: any = await maintainDelete(row.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      const detailRes: any = await getEquipmentDetail(detail.value.id)
      if (detailRes.code === 200) detail.value = detailRes.data
      loadList()
    } else ElMessage.error(res.message || '删除失败')
  } catch (e) { console.error(e); ElMessage.error('删除失败') }
}
const delMetering = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该计量记录？', '删除确认', { type: 'warning' })
  } catch { return }
  try {
    const res: any = await meteringDelete(row.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      const detailRes: any = await getEquipmentDetail(detail.value.id)
      if (detailRes.code === 200) detail.value = detailRes.data
      loadList()
    } else ElMessage.error(res.message || '删除失败')
  } catch (e) { console.error(e); ElMessage.error('删除失败') }
}

onMounted(() => { loadDicts(); loadList() })
</script>

<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" placeholder="设备编码/名称/型号/科室" clearable style="width: 220px"
                    @keyup.enter="query.pageNum = 1; loadList()" />
        </el-form-item>
        <el-form-item label="设备类别">
          <el-select v-model="query.category" placeholder="设备类别" clearable style="width: 150px"
                     :fit-input-width="false">
            <el-option v-for="d in categoryDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 110px" :fit-input-width="false">
            <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="equipment-table">
        <el-table-column prop="equipmentCode" label="资产编号" width="140" />
        <el-table-column prop="equipmentName" label="设备名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="model" label="型号" width="120" show-overflow-tooltip />
        <el-table-column prop="deptName" label="使用科室" width="110" />
        <el-table-column prop="category" label="类别" width="120">
          <template #default="{ row }">{{ categoryText(row.category) }}</template>
        </el-table-column>
        <el-table-column prop="purchasePrice" label="原值(元)" width="110" align="right">
          <template #default="{ row }">{{ row.purchasePrice != null ? Number(row.purchasePrice).toLocaleString() : '—' }}</template>
        </el-table-column>
        <el-table-column label="最近维保" width="110">
          <template #default="{ row }">{{ fmtDate(row.lastMaintainDate) }}</template>
        </el-table-column>
        <el-table-column label="下次维保" width="110">
          <template #default="{ row }">
            <span :class="{ 'text-red-500 font-bold': row.nextMaintainDate && row.nextMaintainDate < today }">
              {{ fmtDate(row.nextMaintainDate) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="计量有效期至" width="120">
          <template #default="{ row }">
            <span v-if="row.meteringValidUntil" :class="{ 'text-red-500 font-bold': meteringExpired(row) }">
              {{ fmtDate(row.meteringValidUntil) }}{{ meteringExpired(row) ? '（过期）' : '' }}
            </span>
            <span v-else class="text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-perm="'asset:equipment:add'" link type="primary" size="small" @click="openMaintain(row)">维保登记</el-button>
            <el-button v-perm="'asset:equipment:add'" link type="success" size="small" @click="openMetering(row)">计量登记</el-button>
            <el-button link size="small" @click="openDetail(row)">记录</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 维保登记 -->
    <el-dialog v-model="maintainVisible" :title="`维保登记 — ${maintainForm.equipmentName}`" width="540px"
               :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="维保类型" required>
          <el-radio-group v-model="maintainForm.maintainType">
            <el-radio v-for="d in maintainTypeDict" :key="d.dictValue" :value="Number(d.dictValue)">
              {{ d.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="维保日期" required>
          <el-date-picker v-model="maintainForm.maintainDate" type="date" value-format="YYYY-MM-DD"
                          style="width: 200px" />
        </el-form-item>
        <el-form-item label="下次维保日期">
          <el-date-picker v-model="maintainForm.nextMaintainDate" type="date" value-format="YYYY-MM-DD"
                          style="width: 200px" />
        </el-form-item>
        <el-form-item label="费用(元)">
          <el-input-number v-model="maintainForm.cost as any" :min="0" :precision="2" style="width: 200px" />
        </el-form-item>
        <el-form-item label="故障描述">
          <el-input v-model="maintainForm.faultDesc" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input v-model="maintainForm.handleResult" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="维保结果" required>
          <el-radio-group v-model="maintainForm.maintainResult">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="2">异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="维保人">
          <el-input v-model="maintainForm.handlerName" placeholder="不填默认当前登录人" style="width: 200px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="maintainVisible = false">取消</el-button>
        <el-button type="primary" v-perm="'asset:equipment:add'" @click="submitMaintain">登记</el-button>
      </template>
    </el-dialog>

    <!-- 计量登记 -->
    <el-dialog v-model="meteringVisible" :title="`计量登记 — ${meteringForm.equipmentName}`" width="520px"
               :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="计量类型" required>
          <el-radio-group v-model="meteringForm.meteringType">
            <el-radio v-for="d in meteringTypeDict" :key="d.dictValue" :value="Number(d.dictValue)">
              {{ d.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计量日期" required>
          <el-date-picker v-model="meteringForm.meteringDate" type="date" value-format="YYYY-MM-DD"
                          style="width: 200px" />
        </el-form-item>
        <el-form-item label="有效期至" required>
          <el-date-picker v-model="meteringForm.validUntil" type="date" value-format="YYYY-MM-DD"
                          style="width: 200px" />
        </el-form-item>
        <el-form-item label="计量结果" required>
          <el-radio-group v-model="meteringForm.meteringResult">
            <el-radio :value="1">合格</el-radio>
            <el-radio :value="2">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="证书编号">
          <el-input v-model="meteringForm.certNo" style="width: 220px" />
        </el-form-item>
        <el-form-item label="检定机构">
          <el-input v-model="meteringForm.agency" style="width: 220px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="meteringVisible = false">取消</el-button>
        <el-button type="primary" v-perm="'asset:equipment:add'" @click="submitMetering">登记</el-button>
      </template>
    </el-dialog>

    <!-- 记录详情 -->
    <el-dialog v-model="detailVisible" :title="`设备记录 — ${detail?.equipmentName || ''}`" width="820px">
      <template v-if="detail">
        <div class="text-sm text-gray-500 mb-2">最近维保记录（{{ detail.recentMaintains?.length || 0 }} 条）</div>
        <el-table :data="detail.recentMaintains || []" border size="small" data-testid="maintain-records">
          <el-table-column label="类型" width="70">
            <template #default="{ row }">{{ maintainTypeText(row) }}</template>
          </el-table-column>
          <el-table-column label="维保日期" width="110">
            <template #default="{ row }">{{ fmtDate(row.maintainDate) }}</template>
          </el-table-column>
          <el-table-column prop="faultDesc" label="故障描述" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.faultDesc || '—' }}</template>
          </el-table-column>
          <el-table-column prop="handleResult" label="处理结果" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.handleResult || '—' }}</template>
          </el-table-column>
          <el-table-column prop="maintainResultText" label="结果" width="70" />
          <el-table-column prop="handlerName" label="维保人" width="90" />
          <el-table-column label="操作" width="70">
            <template #default="{ row }">
              <el-button v-perm="'asset:equipment:delete'" link type="danger" size="small"
                         @click="delMaintain(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="text-sm text-gray-500 mt-4 mb-2">最近计量记录（{{ detail.recentMeterings?.length || 0 }} 条）</div>
        <el-table :data="detail.recentMeterings || []" border size="small" data-testid="metering-records">
          <el-table-column label="类型" width="70">
            <template #default="{ row }">{{ meteringTypeText(row) }}</template>
          </el-table-column>
          <el-table-column label="计量日期" width="110">
            <template #default="{ row }">{{ fmtDate(row.meteringDate) }}</template>
          </el-table-column>
          <el-table-column label="有效期至" width="110">
            <template #default="{ row }">{{ fmtDate(row.validUntil) }}</template>
          </el-table-column>
          <el-table-column prop="meteringResultText" label="结果" width="70" />
          <el-table-column prop="certNo" label="证书编号" width="130">
            <template #default="{ row }">{{ row.certNo || '—' }}</template>
          </el-table-column>
          <el-table-column prop="agency" label="检定机构" min-width="130" show-overflow-tooltip>
            <template #default="{ row }">{{ row.agency || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="70">
            <template #default="{ row }">
              <el-button v-perm="'asset:equipment:delete'" link type="danger" size="small"
                         @click="delMetering(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>
