<template>
  <div data-testid="duty-post-view">
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="责任范围">
            <el-select v-model="searchForm.dutyScope" clearable placeholder="全部" style="width: 130px">
              <el-option v-for="o in DUTY_SCOPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="单元类型">
            <el-select v-model="searchForm.orgType" clearable placeholder="全部" style="width: 110px">
              <el-option v-for="o in ORG_UNIT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 100px">
              <el-option :value="1" label="启用"/>
              <el-option :value="0" label="停用"/>
            </el-select>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" clearable placeholder="编码/名称" style="width: 140px"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start">
          <el-button v-perm="'org:duty:edit'" :icon="Plus" data-testid="duty-post-add" type="primary"
                     @click="handleAdd">
            新增点位
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <div data-testid="duty-post-table">
        <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
          <el-table-column label="点位编码" prop="postCode" width="150"/>
          <el-table-column label="点位名称" min-width="160" prop="postName"/>
          <el-table-column label="责任范围" prop="dutyScopeText" width="110"/>
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
              <el-tag :type="row.roleType === 1 ? 'primary' : 'info'" size="small">{{ row.roleTypeText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="应到岗位" prop="requiredStaffTypeName" width="110">
            <template #default="{ row }">{{ row.requiredStaffTypeName || '不限' }}</template>
          </el-table-column>
          <el-table-column label="值班电话" prop="phone" width="130"/>
          <el-table-column label="排序" prop="sortNo" width="80"/>
          <el-table-column label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                {{ row.status === 1 ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="140" prop="remark" show-overflow-tooltip/>
          <el-table-column fixed="right" label="操作" width="160">
            <template #default="{ row }">
              <el-button v-perm="'org:duty:edit'" :icon="Edit" link type="primary" @click="handleEdit(row)">修改
              </el-button>
              <el-button v-perm="'org:duty:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">删除
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

    <DutyPostEdit v-model="dialogVisible" :row="editRow" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
/**
 * 值守点位（sql/200，菜单 2936）。
 *
 * 把「位」从「人」里剥出来：点位先定义存在（这个医院今天就该有这个班），排班只是把人写进位里。
 * 以前 biz_duty_roster 的唯一键是 (日期, 班次, 角色)，既表达「一个位一天一位」又当「一个人的班」用，
 * 结果是换人看不出是换了人还是换了时间。现在位是位、人是人，位可以空着等排。
 */
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {deleteDutyPost, getDutyPostListPage,} from '@/api/dutyPost'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import {DUTY_SCOPE_OPTIONS, ORG_UNIT_TYPE_OPTIONS} from '@/lib/staffSchedule'
import DutyPostEdit from './DutyPostEdit.vue'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const searchForm = ref({dutyScope: null, orgType: null, status: null, keyword: ''})

const dialogVisible = ref(false)
const editRow = ref(null)

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

const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData()
}
const handleReset = () => {
  searchForm.value = {dutyScope: null, orgType: null, status: null, keyword: ''}
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

const handleAdd = () => {
  editRow.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  editRow.value = row
  dialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定删除点位「${row.postName}」吗？（已被排班占用的删不掉）`, '删除点位', {type: 'warning'})
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

onMounted(() => {
  loadData()
})
</script>
