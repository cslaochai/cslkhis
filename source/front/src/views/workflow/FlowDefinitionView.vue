<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="流程">
            <el-input
                v-model="searchForm.keyword"
                clearable
                placeholder="流程编码或名称"
                style="width: 220px"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" clearable placeholder="全部" style="width: 140px">
              <el-option label="草稿" :value="0"/>
              <el-option label="已发布" :value="1"/>
              <el-option label="已停用" :value="2"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'workflow:definition:config'" :icon="Plus" type="primary" @click="handleAdd">
            新增流程
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe>
        <el-table-column label="流程编码" prop="flowCode" width="200"/>
        <el-table-column label="流程名称" prop="flowName" min-width="150"/>
        <el-table-column label="说明" prop="flowDesc" min-width="260" show-overflow-tooltip/>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="版本" prop="version" width="80"/>
        <el-table-column label="更新时间" prop="updateTime" width="170"/>
        <el-table-column fixed="right" label="操作" width="230">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleDetail(row)">详情</el-button>
            <el-button v-perm="'workflow:definition:config'" :icon="Edit" link type="primary"
                       @click="handleEdit(row)">编辑
            </el-button>
            <el-button
                v-if="row.status !== 1"
                v-perm="'workflow:definition:config'"
                link
                type="success"
                @click="handlePublish(row)"
            >发布
            </el-button>
            <el-button v-perm="'workflow:definition:config'" :icon="Delete" link type="danger"
                       @click="handleDelete(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

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

    <FlowDefinitionDetail v-model="detailVisible" :id="detailId"/>
  </div>
</template>

<script lang="js" setup>
import {onMounted, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {
  flowDefinitionDelete,
  flowDefinitionListPage,
  flowDefinitionPublish,
} from '@/api/workflow'
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import FlowDefinitionDetail from '@/views/workflow/FlowDefinitionDetail.vue'

const router = useRouter()
const loading = ref(false)
const searchForm = ref({keyword: '', status: null})
const tableData = ref([])
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const detailVisible = ref(false)
const detailId = ref(null)
const statusTagType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await flowDefinitionListPage({
      ...searchForm.value,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    })
    if (res.code === 200) {
      tableData.value = res.data.records || []
      pagination.value.total = res.data.total || 0
    }
  } catch (e) {
    console.error('加载流程定义失败', e)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {keyword: '', status: null}
  pagination.value.pageNum = 1
  loadData()
}

const handleAdd = () => {
  router.push('/workflow/definition/designer')
}

const handleEdit = (row) => {
  router.push(`/workflow/definition/designer/${row.id}`)
}

const handleDetail = (row) => {
  detailId.value = row.id
  detailVisible.value = true
}

const handlePublish = async (row) => {
  try {
    await ElMessageBox.confirm(
        `发布后「${row.flowName}」即可被启动/被意图识别命中，确定发布吗？`, '发布确认', {type: 'warning'})
    const res = await flowDefinitionPublish(row.id)
    if (res.code === 200) {
      ElMessage.success('已发布')
      loadData()
    }
  } catch (e) {
    if (e !== 'cancel') console.error('发布流程失败', e)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定删除流程「${row.flowName}」吗？仅草稿可删，节点与流转线一并物理删除。`, '删除确认', {type: 'warning'})
    const res = await flowDefinitionDelete(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    }
  } catch (e) {
    if (e !== 'cancel') console.error('删除流程失败', e)
  }
}

const handleSizeChange = (val) => {
  pagination.value.pageSize = val
  loadData()
}

const handleCurrentChange = (val) => {
  pagination.value.pageNum = val
  loadData()
}

onMounted(loadData)
</script>
