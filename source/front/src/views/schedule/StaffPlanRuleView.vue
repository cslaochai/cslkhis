<template>
  <div data-testid="staff-plan-rule-view">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="单元类型">
            <el-select v-model="searchForm.orgType" clearable placeholder="全部" style="width: 110px">
              <el-option v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="岗位类别">
            <el-select v-model="searchForm.staffType" clearable placeholder="全部" style="width: 130px">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 100px">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="停用"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start">
          <el-button v-perm="'org:schedule:add'" :icon="Plus" data-testid="plan-rule-add" type="primary"
                     @click="handleAdd">
            新增标准
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div data-testid="plan-rule-table">
        <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
          <el-table-column label="排班单元" min-width="170">
            <template #default="{ row }">{{ row.orgName || '—' }}（{{ row.orgTypeText }}）</template>
          </el-table-column>
          <el-table-column label="班次" min-width="140">
            <template #default="{ row }">
              <el-tag v-if="!row.shiftId || Number(row.shiftId) === 0" size="small" type="info">全部班次（合计）</el-tag>
              <span v-else>{{ row.shiftName }}</span>
            </template>
          </el-table-column>
          <el-table-column label="岗位类别" prop="staffTypeName" width="110"/>
          <el-table-column label="最低在岗" prop="minStaff" width="100"/>
          <el-table-column label="最高在岗" width="100">
            <template #default="{ row }">{{ row.maxStaff || '不限' }}</template>
          </el-table-column>
          <el-table-column label="周工时上限" width="110">
            <template #default="{ row }">{{ row.maxWeekHours ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="连夜上限" width="100">
            <template #default="{ row }">{{ row.maxConsecutiveNightDays ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="连班上限" width="100">
            <template #default="{ row }">{{ row.maxConsecutiveWorkDays ?? '—' }}</template>
          </el-table-column>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="160" prop="remark" show-overflow-tooltip/>
          <el-table-column fixed="right" label="操作" width="160">
            <template #default="{ row }">
              <el-button v-perm="'org:schedule:add'" :icon="Edit" link type="primary" @click="handleEdit(row)">修改
              </el-button>
              <el-button v-perm="'org:schedule:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="140px">
        <el-form-item label="排班单元类型">
          <el-radio-group v-model="formData.orgType" @change="formData.orgId = null">
            <el-radio v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="orgIdRequired" label="所属科室">
          <el-select v-model="formData.orgId" filterable placeholder="选择科室" style="width: 100%">
            <el-option v-for="o in deptOptions" :key="o.id" :label="o.name" :value="o.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="班次">
          <el-select v-model="formData.shiftId" clearable placeholder="留空 = 该单元全部班次共用" style="width: 100%">
            <el-option v-for="s in shiftOptions" :key="s.id" :label="s.shiftName" :value="s.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="岗位类别" prop="staffType">
          <div data-testid="pr-staff-type">
            <el-select v-model="formData.staffType" style="width: 100%">
              <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="最低在岗人数" prop="minStaff">
          <div data-testid="pr-min-staff">
            <el-input-number v-model="formData.minStaff" :max="999" :min="0"/>
          </div>
        </el-form-item>
        <el-form-item label="最高在岗人数">
          <el-input-number v-model="formData.maxStaff" :max="999" :min="0" placeholder="0 或留空 = 不限"/>
        </el-form-item>
        <el-form-item label="周工时上限">
          <el-input-number v-model="formData.maxWeekHours" :max="200" :min="0" :precision="1" :step="4"/>
        </el-form-item>
        <el-form-item label="连续夜班天数上限">
          <el-input-number v-model="formData.maxConsecutiveNightDays" :max="30" :min="0"/>
        </el-form-item>
        <el-form-item label="连续上班天数上限">
          <el-input-number v-model="formData.maxConsecutiveWorkDays" :max="30" :min="0"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button :loading="submitLoading" data-testid="pr-submit" type="primary" @click="handleSubmit">确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="js" setup>
/**
 * 人力配置标准（sql/200，菜单 2937）。
 *
 * 一个单元 × 一个班次 × 一个岗位类别该配多少人。它是排班保存时的闸门，不是报表：
 * 低于最低在岗会拦住保存（一个班没人值班要当场发现，别等出事），高于上限只提示。
 * 这张表由病区护理那套 biz_nurse_schedule_rule 泛化而来，所以起订的一份数据是从护理规则迁过来的。
 */
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {deleteStaffPlanRule, getStaffPlanRuleListPage, staffPlanRuleUpsert} from '@/api/staffPlanRule'
import {getDepartmentSelectList} from '@/api/system'
import {getShiftSelectList} from '@/api/appoint'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {ORG_UNIT_TYPE_HOSPITAL, ORG_UNIT_TYPE_OPTIONS} from '@/lib/staffSchedule'
import {STAFF_TYPE_OPTIONS} from '@/lib/scheduleShift'

const SHIFT_USE_SCOPE_GENERAL = 4

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const searchForm = ref({orgType: null, staffType: null, status: null})

const deptOptions = ref([])
const shiftOptions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增人力标准')
const formRef = ref(null)
const formData = reactive({
  id: null, orgType: ORG_UNIT_TYPE_HOSPITAL, orgId: null, shiftId: null, staffType: 2,
  minStaff: 1, maxStaff: null, maxWeekHours: null, maxConsecutiveNightDays: null,
  maxConsecutiveWorkDays: null, status: 1, remark: '',
})
const rules = {
  staffType: [{required: true, message: '请选择岗位类别', trigger: 'change'}],
  minStaff: [{required: true, message: '请填写最低在岗人数', trigger: 'blur'}],
}

const orgIdRequired = computed(() => Number(formData.orgType) !== ORG_UNIT_TYPE_HOSPITAL)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getStaffPlanRuleListPage({
      ...searchForm.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载人力标准失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData()
}
const handleReset = () => {
  searchForm.value = {orgType: null, staffType: null, status: null}
  handleSearch()
}
const handleSizeChange = (v) => {
  pagination.value.pageSize = v;
  loadData()
}
const handleCurrentChange = (v) => {
  pagination.value.pageNum = v;
  loadData()
}

const resetForm = () => {
  formData.id = null
  formData.orgType = ORG_UNIT_TYPE_HOSPITAL
  formData.orgId = null
  formData.shiftId = null
  formData.staffType = 2
  formData.minStaff = 1
  formData.maxStaff = null
  formData.maxWeekHours = null
  formData.maxConsecutiveNightDays = null
  formData.maxConsecutiveWorkDays = null
  formData.status = 1
  formData.remark = ''
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增人力标准'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  dialogTitle.value = '修改人力标准'
  Object.assign(formData, {
    id: row.id,
    orgType: row.orgType,
    orgId: row.orgId || null,
    // 0 = 该单元全部班次共用（合计行），下拉里没有这条，单独补
    shiftId: row.shiftId && Number(row.shiftId) !== 0 ? row.shiftId : null,
    staffType: row.staffType,
    minStaff: row.minStaff,
    maxStaff: row.maxStaff,
    maxWeekHours: row.maxWeekHours,
    maxConsecutiveNightDays: row.maxConsecutiveNightDays,
    maxConsecutiveWorkDays: row.maxConsecutiveWorkDays,
    status: row.status ?? 1,
    remark: row.remark || '',
  })
  if (row.shiftId && Number(row.shiftId) !== 0
      && !shiftOptions.value.some((s) => String(s.id) === String(row.shiftId))) {
    shiftOptions.value = [{id: row.shiftId, shiftName: row.shiftName || '已停用班次'}, ...shiftOptions.value]
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const payload = {...formData, orgId: orgIdRequired.value ? formData.orgId : null}
    const res = await staffPlanRuleUpsert(payload)
    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `删掉「${row.orgName} · ${row.shiftName || '全部班次'} · ${row.staffTypeName}」这条标准后，` +
        '该单元这个班不再做人数校验，确定吗？',
        '删除人力标准', {type: 'warning'})
    const res = await deleteStaffPlanRule(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel' && e.message) ElMessage.error(e.message)
  }
}

onMounted(async () => {
  loadData()
  try {
    const [dRes, sRes] = await Promise.all([
      getDepartmentSelectList({}),
      getShiftSelectList({status: 1, useScope: SHIFT_USE_SCOPE_GENERAL}),
    ])
    deptOptions.value = (dRes?.data || []).map((d) => ({id: d.id ?? d.deptId, name: d.deptName ?? d.name}))
    shiftOptions.value = (sRes?.data || []).map((s) => ({id: s.id, shiftName: s.shiftName}))
  } catch (e) {
    ElMessage.error(e.message || '加载下拉数据失败')
  }
})
</script>
