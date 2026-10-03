<script setup lang="js">
/**
 * 值守点位（sql/200，菜单 2936）。
 *
 * 把「位」从「人」里剥出来：点位先定义存在（这个医院今天就该有这个班），排班只是把人写进位里。
 * 以前 biz_duty_roster 的唯一键是 (日期, 班次, 角色)，既表达「一个位一天一位」又当「一个人的班」用，
 * 结果是换人看不出是换了人还是换了时间。现在位是位、人是人，位可以空着等排。
 */
import { computed, reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete, Refresh } from '@element-plus/icons-vue'
import {
  getDutyPostListPage, dutyPostUpsert, deleteDutyPost,
} from '@/api/dutyPost'
import { getDepartmentSelectList } from '@/api/system'
import { getShiftSelectList } from '@/api/appoint'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'
import {
  DUTY_SCOPE_OPTIONS, DUTY_ROLE_TYPE_OPTIONS, ORG_UNIT_TYPE_OPTIONS, ORG_UNIT_TYPE_HOSPITAL,
} from '@/lib/staffSchedule'
import { STAFF_TYPE_OPTIONS } from '@/lib/scheduleShift'

const SHIFT_USE_SCOPE_DUTY = 3

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const pagination = ref({ pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0 })
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const searchForm = ref({ dutyScope: null, orgType: null, status: null, keyword: '' })

const deptOptions = ref([])
// 值班班次册（use_scope=3）：班次决定这一位几点到几点，点位不能再自己填一遍时间
const shiftOptions = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增点位')
const formRef = ref(null)
const formData = reactive({
  id: null, postCode: '', postName: '', dutyScope: 1, orgType: ORG_UNIT_TYPE_HOSPITAL, orgId: null,
  roleType: 1, shiftId: null, requiredStaffType: null, phone: '', sortNo: 0, status: 1, remark: '',
})
const rules = {
  postCode: [{ required: true, message: '请填写点位编码', trigger: 'blur' }],
  postName: [{ required: true, message: '请填写点位名称', trigger: 'blur' }],
  dutyScope: [{ required: true, message: '请选择责任范围', trigger: 'change' }],
  roleType: [{ required: true, message: '请选择班内角色', trigger: 'change' }],
  shiftId: [{ required: true, message: '请选择班次', trigger: 'change' }],
}

const orgIdRequired = computed(() => Number(formData.orgType) !== ORG_UNIT_TYPE_HOSPITAL)

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDutyPostListPage({
      ...searchForm.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载点位失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => { pagination.value.pageNum = 1; loadData() }
const handleReset = () => {
  searchForm.value = { dutyScope: null, orgType: null, status: null, keyword: '' }
  handleSearch()
}
const handleSizeChange = (v) => { pagination.value.pageSize = v; loadData() }
const handleCurrentChange = (v) => { pagination.value.pageNum = v; loadData() }

const resetForm = () => {
  formData.id = null
  formData.postCode = ''
  formData.postName = ''
  formData.dutyScope = 1
  formData.orgType = ORG_UNIT_TYPE_HOSPITAL
  formData.orgId = null
  formData.roleType = 1
  formData.shiftId = null
  formData.requiredStaffType = null
  formData.phone = ''
  formData.sortNo = 0
  formData.status = 1
  formData.remark = ''
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增点位'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  dialogTitle.value = '修改点位'
  Object.assign(formData, {
    id: row.id,
    postCode: row.postCode,
    postName: row.postName,
    dutyScope: row.dutyScope,
    orgType: row.orgType,
    orgId: row.orgId || null,
    roleType: row.roleType,
    shiftId: row.shiftId,
    requiredStaffType: row.requiredStaffType || null,
    phone: row.phone || '',
    sortNo: row.sortNo ?? 0,
    status: row.status ?? 1,
    remark: row.remark || '',
  })
  // 挂的班次已被停用时会不在下拉里，补进去保证回显不是空的
  if (row.shiftId && !shiftOptions.value.some((s) => String(s.id) === String(row.shiftId))) {
    shiftOptions.value = [{ id: row.shiftId, shiftName: row.shiftName || '已停用班次' }, ...shiftOptions.value]
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitLoading.value = true
  try {
    const payload = { ...formData, orgId: orgIdRequired.value ? formData.orgId : null }
    const res = await dutyPostUpsert(payload)
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
      `确定删除点位「${row.postName}」吗？（已被排班占用的删不掉）`, '删除点位', { type: 'warning' })
    const res = await deleteDutyPost(row.id)
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
      getShiftSelectList({ status: 1, useScope: SHIFT_USE_SCOPE_DUTY }),
    ])
    deptOptions.value = (dRes?.data || []).map((d) => ({ id: d.id ?? d.deptId, name: d.deptName ?? d.name }))
    shiftOptions.value = (sRes?.data || []).map((s) => ({ id: s.id, shiftName: s.shiftName }))
  } catch (e) {
    ElMessage.error(e.message || '加载下拉数据失败')
  }
})
</script>

<template>
  <div data-testid="duty-post-view">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="责任范围">
            <el-select v-model="searchForm.dutyScope" placeholder="全部" clearable style="width: 130px">
              <el-option v-for="o in DUTY_SCOPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="单元类型">
            <el-select v-model="searchForm.orgType" placeholder="全部" clearable style="width: 110px">
              <el-option v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 100px">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="编码/名称" clearable style="width: 140px" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start">
          <el-button v-perm="'org:duty:edit'" type="primary" :icon="Plus" data-testid="duty-post-add" @click="handleAdd">
            新增点位
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div data-testid="duty-post-table">
        <el-table :data="tableData" v-loading="loading" stripe :max-height="tableMaxHeight">
          <el-table-column prop="postCode" label="点位编码" width="150" />
          <el-table-column prop="postName" label="点位名称" min-width="160" />
          <el-table-column prop="dutyScopeText" label="责任范围" width="110" />
          <el-table-column label="所属单元" min-width="150">
            <template #default="{ row }">{{ row.orgName || '—' }}（{{ row.orgTypeText }}）</template>
          </el-table-column>
          <el-table-column label="班次" min-width="200">
            <template #default="{ row }">
              {{ row.shiftName }} {{ row.startTime }}~{{ row.endTime }}
            </template>
          </el-table-column>
          <el-table-column label="班内角色" width="100">
            <template #default="{ row }">
              <el-tag size="small" :type="row.roleType === 1 ? 'primary' : 'info'">{{ row.roleTypeText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="requiredStaffTypeName" label="应到岗位" width="110">
            <template #default="{ row }">{{ row.requiredStaffTypeName || '不限' }}</template>
          </el-table-column>
          <el-table-column prop="phone" label="值班电话" width="130" />
          <el-table-column prop="sortNo" label="排序" width="80" />
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="160" fixed="right">
            <template #default="{ row }">
              <el-button v-perm="'org:duty:edit'" type="primary" link :icon="Edit" @click="handleEdit(row)">修改</el-button>
              <el-button v-perm="'org:duty:delete'" type="danger" link :icon="Delete" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="120px">
        <el-form-item label="点位编码" prop="postCode">
          <div data-testid="dp-code">
            <el-input v-model="formData.postCode" placeholder="如 DUTY-NIGHT-MAIN（全局唯一）" />
          </div>
        </el-form-item>
        <el-form-item label="点位名称" prop="postName">
          <div data-testid="dp-name">
            <el-input v-model="formData.postName" placeholder="如 夜间总值班（主班）" />
          </div>
        </el-form-item>
        <el-form-item label="责任范围" prop="dutyScope">
          <el-select v-model="formData.dutyScope" style="width: 100%">
            <el-option v-for="o in DUTY_SCOPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属单元类型">
          <el-radio-group v-model="formData.orgType" @change="formData.orgId = null">
            <el-radio v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="orgIdRequired" label="所属科室">
          <el-select v-model="formData.orgId" filterable placeholder="选择科室" style="width: 100%">
            <el-option v-for="o in deptOptions" :key="o.id" :label="o.name" :value="o.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="班次" prop="shiftId">
          <div data-testid="dp-shift">
            <el-select v-model="formData.shiftId" placeholder="这一位几点到几点由班次带出" style="width: 100%">
              <el-option v-for="s in shiftOptions" :key="s.id" :label="s.shiftName" :value="s.id" />
            </el-select>
          </div>
        </el-form-item>
        <el-form-item label="班内角色" prop="roleType">
          <el-radio-group v-model="formData.roleType">
            <el-radio v-for="o in DUTY_ROLE_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="应到岗位类别">
          <el-select v-model="formData.requiredStaffType" clearable placeholder="不限" style="width: 100%">
            <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="值班电话">
          <el-input v-model="formData.phone" placeholder="半夜打得通的那一部" />
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="formData.sortNo" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="如：急诊床位调配的最终兜底人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" data-testid="dp-submit" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
