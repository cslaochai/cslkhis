<script setup lang="ts">
/**
 * 体检管理（G23，菜单 905，挂患者中心）
 *
 * 体检套餐已拆为独立菜单 2930「体检套餐」（sql/185，含套餐新增/编辑/停用维护）；
 * 本页登记弹窗的套餐下拉照旧走 checkupPackageListPage（仅启用）—— 数据流不断。
 * 登记状态机：1 已登记 → 2 检查中 → 3 已完成 → 4 已出报告（终态禁改禁删）。
 * 登记时按套餐项目预生成结果空行；出报告要求全部明细已录 + 总检结论必填。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import {
  checkupPackageListPage, checkupRecordCreate, checkupRecordListPage, getCheckupRecord,
  checkupRecordStart, checkupResultSave, checkupConclude, checkupRecordDelete,
} from '@/api/checkup'
import { getDictDataMapList } from '@/api/system'
import { DICT_TYPE } from '@/lib/dict-cache'
import { dictLabelText } from '@/lib/utils'
import PatientSelect from '@/components/his/PatientSelect.vue'
import { DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

// ---------------- 字典 ----------------
const statusDict = ref<any[]>([])
const personDict = ref<any[]>([])
const flagDict = ref<any[]>([])
const statusText = (v: any) => dictLabelText(statusDict.value, v)
const statusTag = (v: number) => ({ 1: 'info', 2: 'warning', 3: 'primary', 4: 'success' } as any)[v] || 'info'
const personText = (v: any) => dictLabelText(personDict.value, v)
const flagTag = (v: number) => ({ 0: 'success', 1: 'danger', 2: 'warning' } as any)[v] || 'info'
const flagText = (v: any) => dictLabelText(flagDict.value, v)
const loadDicts = async () => {
  try {
    const res: any = await getDictDataMapList(
      `${DICT_TYPE.CHECKUP_RECORD_STATUS},${DICT_TYPE.CHECKUP_PERSON_TYPE},${DICT_TYPE.CHECKUP_RESULT_FLAG}`)
    statusDict.value = res?.data?.[DICT_TYPE.CHECKUP_RECORD_STATUS] || []
    personDict.value = res?.data?.[DICT_TYPE.CHECKUP_PERSON_TYPE] || []
    flagDict.value = res?.data?.[DICT_TYPE.CHECKUP_RESULT_FLAG] || []
  } catch (e) { console.error('加载字典失败', e) }
}

// ---------------- 登记列表 ----------------
const loading = ref(false)
const rows = ref<any[]>([])
const total = ref(0)
const query = reactive({ keyword: '', recordStatus: null as number | null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()
const loadList = async () => {
  loading.value = true
  try {
    const res: any = await checkupRecordListPage({
      keyword: query.keyword.trim() || undefined,
      recordStatus: query.recordStatus ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    })
    rows.value = res?.data?.records || []
    total.value = Number(res?.data?.total || 0)
  } catch (e) { console.error('加载体检登记失败', e) } finally { loading.value = false }
}
const reset = () => {
  query.keyword = ''; query.recordStatus = null; query.pageNum = 1; loadList()
}

// ---------------- 登记弹窗 ----------------
const createVisible = ref(false)
const createForm = reactive({ patientId: null as any, personType: 1, packageId: null as any, checkupDate: '', remark: '' })
const packages = ref<any[]>([])
const loadPackages = async () => {
  try {
    const res: any = await checkupPackageListPage({ status: 1, pageNum: 1, pageSize: 100 })
    packages.value = res?.data?.records || []
  } catch (e) { console.error('加载套餐失败', e) }
}
const openCreate = () => {
  createForm.patientId = null; createForm.personType = 1; createForm.packageId = null
  createForm.checkupDate = ''; createForm.remark = ''
  createVisible.value = true
}
const submitCreate = async () => {
  if (!createForm.patientId) return ElMessage.warning('请选择体检人')
  if (!createForm.packageId) return ElMessage.warning('请选择套餐')
  if (!createForm.checkupDate) return ElMessage.warning('请选择体检日期')
  try {
    await checkupRecordCreate({
      patientId: createForm.patientId, personType: createForm.personType,
      packageId: createForm.packageId, checkupDate: createForm.checkupDate, remark: createForm.remark,
    })
    ElMessage.success('登记成功')
    createVisible.value = false
    loadList()
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '登记失败') }
}

// ---------------- 详情 / 结果录入 ----------------
const detailVisible = ref(false)
const detail = ref<any>(null)
const openDetail = async (row: any) => {
  try {
    const res: any = await getCheckupRecord(row.id)
    detail.value = res?.data || null
    detailVisible.value = true
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '加载详情失败') }
}
const saveOne = async (row: any) => {
  if (!row.resultValue || !String(row.resultValue).trim()) return ElMessage.warning('请填写结果值')
  try {
    await checkupResultSave({
      resultId: row.id, resultValue: row.resultValue, abnormalFlag: row.abnormalFlag ?? 0,
      summaryText: row.summaryText, checkerName: row.checkerName,
    })
    ElMessage.success('结果已保存')
    const res: any = await getCheckupRecord(detail.value.id)
    detail.value = res?.data
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '保存失败') }
}
const startOne = async (row: any) => {
  try {
    await checkupRecordStart(row.id)
    ElMessage.success('已开始体检')
    loadList()
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '操作失败') }
}
const deleteOne = async (row: any) => {
  try {
    await ElMessageBox.confirm('确认删除该体检登记？仅已登记状态可删。', '删除确认', { type: 'warning' })
  } catch { return }
  try {
    await checkupRecordDelete(row.id)
    ElMessage.success('已删除')
    loadList()
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '删除失败') }
}

// ---------------- 总检 ----------------
const concludeVisible = ref(false)
const concludeForm = reactive({ recordId: null as any, recordNo: '', conclusion: '', doctorName: '' })
const openConclude = async (row: any) => {
  try {
    const res: any = await getCheckupRecord(row.id)
    const d = res?.data
    if (d?.recordStatus !== 3) return ElMessage.warning('存在未录入的结果项，不能总检出报告')
    concludeForm.recordId = d.id; concludeForm.recordNo = d.recordNo
    concludeForm.conclusion = ''; concludeForm.doctorName = ''
    concludeVisible.value = true
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '加载失败') }
}
const submitConclude = async () => {
  if (!concludeForm.conclusion.trim()) return ElMessage.warning('总检结论必填')
  try {
    await checkupConclude({
      recordId: concludeForm.recordId, conclusion: concludeForm.conclusion, doctorName: concludeForm.doctorName,
    })
    ElMessage.success('体检报告已出')
    concludeVisible.value = false
    loadList()
  } catch (e: any) { ElMessage.error(e?.response?.data?.message || '出报告失败') }
}

onMounted(() => { loadDicts(); loadList(); loadPackages() })
</script>

<template>
  <div>
    <!-- 体检登记（「体检套餐」页签已拆为独立菜单 2930，sql/185） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="姓名 / 体检编号" clearable style="width: 200px"
                      @keyup.enter="query.pageNum = 1; loadList()" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.recordStatus" placeholder="状态" clearable style="width: 130px"
                       :fit-input-width="false">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="query.pageNum = 1; loadList()">查询</el-button>
            <el-button :icon="Refresh" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button type="primary" plain v-perm="'checkup:manage:add'" data-testid="checkup-create-btn"
                     @click="openCreate">体检登记</el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="rows" v-loading="loading" stripe :max-height="tableMaxHeight" data-testid="checkup-table">
        <el-table-column prop="recordNo" label="体检编号" width="210" />
        <el-table-column prop="patientName" label="体检人" width="100" />
        <el-table-column prop="gender" label="性别" width="60">
          <template #default="{ row }">{{ ({ 1: '男', 2: '女', 9: '未知' } as any)[row.gender] || '—' }}</template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="60" />
        <el-table-column prop="personType" label="对象" width="70">
          <template #default="{ row }">{{ personText(row.personType) }}</template>
        </el-table-column>
        <el-table-column prop="packageName" label="套餐" min-width="150" show-overflow-tooltip />
        <el-table-column prop="totalAmount" label="金额(元)" width="90" align="right" />
        <el-table-column prop="checkupDate" label="体检日期" width="105" />
        <el-table-column prop="recordStatus" label="状态" width="95">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.recordStatus)">{{ statusText(row.recordStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="185" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" v-perm="'checkup:manage:edit'" @click="openDetail(row)">结果</el-button>
            <el-button v-if="row.recordStatus === 1" v-perm="'checkup:manage:edit'" link type="warning" size="small"
                       @click="startOne(row)">开始体检</el-button>
            <el-button v-if="row.recordStatus === 3" v-perm="'checkup:manage:edit'" link type="success" size="small"
                       @click="openConclude(row)">总检</el-button>
            <el-button v-if="row.recordStatus === 1" v-perm="'checkup:manage:delete'" link type="danger" size="small"
                       @click="deleteOne(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 登记弹窗 -->
    <el-dialog v-model="createVisible" title="体检登记" width="480px">
      <el-form label-width="90px">
        <el-form-item label="体检人" required>
          <PatientSelect v-model="createForm.patientId as any" />
        </el-form-item>
        <el-form-item label="对象类型">
          <el-radio-group v-model="createForm.personType">
            <el-radio :value="1">个人</el-radio>
            <el-radio :value="2">团体</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="套餐" required>
          <el-select v-model="createForm.packageId" placeholder="选择套餐" style="width: 100%"
                     :fit-input-width="false" data-testid="checkup-package-select">
            <el-option v-for="p in packages" :key="p.id" :label="`${p.packageName}（¥${p.price}）`"
                       :value="String(p.id)" />
          </el-select>
        </el-form-item>
        <el-form-item label="体检日期" required>
          <el-date-picker v-model="createForm.checkupDate" type="date" value-format="YYYY-MM-DD"
                          style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" v-perm="'checkup:manage:add'" data-testid="checkup-create-submit" @click="submitCreate">登记</el-button>
      </template>
    </el-dialog>

    <!-- 结果明细弹窗 -->
    <el-dialog v-model="detailVisible" :title="`结果明细 - ${detail?.patientName || ''}（${detail?.recordNo || ''}）`"
               width="860px">
      <div v-if="detail" data-testid="checkup-result-panel">
        <p class="text-sm text-gray-500 mb-2">
          状态：<el-tag :type="statusTag(detail.recordStatus)" size="small">{{ statusText(detail.recordStatus) }}</el-tag>
          <span class="ml-3">套餐：{{ detail.packageName }}（¥{{ detail.totalAmount }}）</span>
        </p>
        <el-table :data="detail.results || []" border size="small">
          <el-table-column prop="itemName" label="项目" width="150" />
          <el-table-column prop="refStandard" label="参考范围" width="160" show-overflow-tooltip />
          <el-table-column label="结果值" min-width="170">
            <template #default="{ row }">
              <el-input v-model="row.resultValue" size="small" :disabled="detail.recordStatus === 4"
                        :data-testid="`checkup-result-input-${row.id}`" />
            </template>
          </el-table-column>
          <el-table-column label="标志" width="100">
            <template #default="{ row }">
              <el-select v-model="row.abnormalFlag" size="small" :disabled="detail.recordStatus === 4"
                         :fit-input-width="false">
                <el-option v-for="d in flagDict" :key="d.dictValue" :label="d.dictLabel"
                           :value="Number(d.dictValue)" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="小结" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.summaryText" size="small" :disabled="detail.recordStatus === 4" />
            </template>
          </el-table-column>
          <el-table-column label="医师" width="90">
            <template #default="{ row }">
              <el-input v-model="row.checkerName" size="small" :disabled="detail.recordStatus === 4" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button v-if="detail.recordStatus !== 4" v-perm="'checkup:manage:edit'" link type="primary" size="small"
                         @click="saveOne(row)">保存</el-button>
              <el-tag v-else :type="flagTag(row.abnormalFlag)" size="small">{{ flagText(row.abnormalFlag) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button v-if="detail?.recordStatus === 3" v-perm="'checkup:manage:edit'" type="primary" data-testid="checkup-conclude-btn"
                   @click="openConclude(detail)">总检出报告</el-button>
      </template>
    </el-dialog>

    <!-- 总检弹窗 -->
    <el-dialog v-model="concludeVisible" :title="`总检出报告 - ${concludeForm.recordNo}`" width="480px">
      <el-form label-width="90px">
        <el-form-item label="总检结论" required>
          <el-input v-model="concludeForm.conclusion" type="textarea" :rows="4"
                    data-testid="checkup-conclusion-input" />
        </el-form-item>
        <el-form-item label="总检医师">
          <el-input v-model="concludeForm.doctorName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="concludeVisible = false">取消</el-button>
        <el-button type="primary" v-perm="'checkup:manage:edit'" data-testid="checkup-conclude-submit" @click="submitConclude">出报告</el-button>
      </template>
    </el-dialog>
  </div>
</template>
