<template>
  <div data-testid="duty-type-view">
    <el-card class="query-card mb-3" shadow="never">
      <div class="flex items-center justify-between gap-4">
        <div class="text-sm text-slate-500">
          值班线 = 全院总值班的「条」：医疗总值归医务科、护理总值归护理部、行政总值与院领导带班归院办。
          线的归口科室决定谁能排它（切换岗位即切换可排范围），资质要求决定能排什么人。
        </div>
        <el-button v-perm="'org:dutyType:edit'" :icon="Plus" data-testid="duty-type-add" type="primary"
                   @click="handleAdd">
          新增值班类型
        </el-button>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="排序" prop="sortNo" width="70" align="center"/>
        <el-table-column label="值班类型" prop="typeName" min-width="130">
          <template #default="{ row }">
            <span class="font-medium">{{ row.typeName }}</span>
            <el-tag size="small" style="margin-left: 6px" type="info">{{ row.typeCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="归口科室（谁排这条线）" prop="manageDeptName" min-width="150">
          <template #default="{ row }">
            <span>{{ row.manageDeptName || '' }}</span>
            <el-tag v-if="row.manageable" size="small" style="margin-left: 6px" type="success">当前岗位可排</el-tag>
            <el-tag v-else size="small" style="margin-left: 6px" type="info">需切到该科室岗位</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="应到岗位类别" prop="requiredStaffTypeText" width="120">
          <template #default="{ row }">{{ row.requiredStaffTypeText || '不限' }}</template>
        </el-table-column>
        <el-table-column label="资质要求（职务）" prop="requiredPositionsText" min-width="180" show-overflow-tooltip/>
        <el-table-column label="院领导带班层" width="120" align="center">
          <template #default="{ row }">{{ row.leaderLayer === 1 ? '需要' : '不需要' }}</template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备注" prop="remark" min-width="200" show-overflow-tooltip/>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button v-perm="'org:dutyType:edit'" :icon="Edit" link type="primary" @click="handleEdit(row)">
              修改
            </el-button>
            <el-button v-perm="'org:dutyType:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" :image-size="60"
                description="还没有值班类型，先新增一条（如「医疗总值班」，归口医务科）"/>
    </el-card>

    <DutyTypeEdit v-model="dialogVisible" :row="editRow" @saved="loadData"/>
  </div>
</template>

<script lang="js" setup>
/**
 * 值班类型（值班线）维护（菜单 2983）。
 *
 * 这一页存在的意义：厂商的总值班是「配置驱动」而不是「代码写死」——
 * 加一条线（后勤总值/分院区总值）不需要改代码发版，院办自己加。
 */
import {onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus} from '@element-plus/icons-vue'
import {deleteDutyType, getDutyTypeList} from '@/api/dutyType'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import DutyTypeEdit from './DutyTypeEdit.vue'

const loading = ref(false)
const tableData = ref([])
const {tableMaxHeight} = useTableMaxHeight()

const dialogVisible = ref(false)
const editRow = ref(null)

onMounted(() => {
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getDutyTypeList()
    if (res.code === 200) tableData.value = res.data || []
  } catch (e) /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  } finally {
    loading.value = false
  }
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
        `删除值班类型「${row.typeName}」？还挂着点位或已有值班记录的只能停用，不能删。`,
        '删除值班类型', {type: 'warning'})
    const res = await deleteDutyType(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (e) /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
}
</script>
