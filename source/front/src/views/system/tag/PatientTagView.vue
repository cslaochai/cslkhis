<template>
  <div>
    <!-- 两卡式列表页：查询卡与表格卡分隔（口径参照 views/system/user/UserView.vue） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="标签名称">
            <el-input v-model="searchForm.tagName" class="!w-48" clearable placeholder="请输入标签名称"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <!-- 主操作区：新增/批量等动作统一靠右，与查询条件视觉分离 -->
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'patient:tag:add'" :icon="Plus" type="primary" @click="handleAdd">新增标签</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" stripe style="width: 100%">
        <el-table-column label="标签名称" min-width="150" prop="tagName">
          <template #default="{ row }">
            <span
                :style="{ backgroundColor: row.tagColor || '#409EFF' }"
                class="inline-block rounded px-2 py-0.5 text-xs font-medium text-white"
            >
              {{ row.tagName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="缩写" prop="shortName" width="150">
          <template #default="{ row }">
            <span
                v-if="row.shortName"
                :style="{ backgroundColor: row.tagColor || '#409EFF' }"
                class="inline-block rounded px-2 py-0.5 text-xs font-medium text-white"
            >
              {{ row.shortName }}
            </span>
            <span v-else class="text-slate-400">-</span>
          </template>
        </el-table-column>
        <el-table-column label="颜色" prop="tagColor" width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <div
                  :style="{ backgroundColor: row.tagColor || '#409EFF' }"
                  class="h-5 w-5 rounded"
              ></div>
              <span class="text-xs text-slate-500">{{ row.tagColor }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" width="200"/>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button :icon="Edit" link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button v-perm="'patient:tag:delete'" :icon="Delete" link type="danger" @click="handleDelete(row)">删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="tableData.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无标签数据
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
  </div>

  <!-- 新增/编辑对话框 -->
  <el-dialog
      v-model="dialogVisible"
      :close-on-click-modal="false"
      :title="dialogTitle"
      width="500px"
  >
    <el-form label-width="80px">
      <el-form-item label="标签名称" required>
        <el-input v-model="formData.tagName" placeholder="请输入标签名称"/>
      </el-form-item>
      <el-form-item label="缩写">
        <el-input v-model="formData.shortName" placeholder="用于空间有限的显示场景"/>
      </el-form-item>
      <el-form-item label="标签颜色" required>
        <div class="flex flex-col gap-3">
          <div class="flex items-center gap-2">
            <el-color-picker v-model="formData.tagColor"/>
            <span class="text-sm text-slate-500">自定义颜色</span>
          </div>
          <div class="flex flex-wrap gap-2">
            <div
                v-for="color in presetColors"
                :key="color"
                :class="formData.tagColor === color ? 'border-slate-800' : 'border-transparent'"
                :style="{ backgroundColor: color }"
                class="h-6 w-6 cursor-pointer rounded border-2 transition-all hover:scale-110"
                @click="formData.tagColor = color"
            ></div>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="预览">
        <span
            :style="{ backgroundColor: formData.tagColor || '#409EFF' }"
            class="inline-block rounded px-3 py-1 text-sm font-medium text-white"
        >
          {{ formData.tagName || '标签预览' }}
        </span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button v-perm="'patient:tag:add'" :loading="submitLoading" type="primary" @click="handleSubmit">确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {createPatientTag, deletePatientTag, getPatientTagList, updatePatientTag} from '@/api/system';

const loading = ref(false);
const searchForm = ref({
  tagName: '',
});
const tableData = ref([]);
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
// 新增/编辑对话框
const dialogVisible = ref(false);
const dialogTitle = ref('新增标签');
const submitLoading = ref(false);
const formData = ref({
  tagId: null,
  tagName: '',
  shortName: '',
  tagColor: '#409EFF',
});
// 预设颜色
const presetColors = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
  '#FF5722', '#9C27B0', '#00BCD4', '#795548', '#607D8B',
];
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  searchForm.value.tagName = '';
  handleSearch();
};
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      tagName: searchForm.value.tagName || undefined,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    };
    const res = await getPatientTagList(params);
    tableData.value = res.data?.records || res.data || [];
    pagination.value.total = res.data?.total || tableData.value.length;
  } catch (error) {
    console.error('加载患者标签失败:', error);
  } finally {
    loading.value = false;
  }
};
const handleAdd = () => {
  formData.value = {
    tagId: null,
    tagName: '',
    shortName: '',
    tagColor: '#409EFF',
  };
  dialogTitle.value = '新增标签';
  dialogVisible.value = true;
};
const handleEdit = (row) => {
  formData.value = {...row};
  dialogTitle.value = '修改标签';
  dialogVisible.value = true;
};
const handleSubmit = async () => {
  if (!formData.value.tagName) {
    ElMessage.warning('请输入标签名称');
    return;
  }
  submitLoading.value = true;
  try {
    if (formData.value.tagId) {
      await updatePatientTag(formData.value);
      ElMessage.success('修改成功');
    } else {
      await createPatientTag(formData.value);
      ElMessage.success('新增成功');
    }
    dialogVisible.value = false;
    loadData();
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  } finally {
    submitLoading.value = false;
  }
};
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除标签 "${row.tagName}" 吗？`, '删除确认', {
      type: 'warning',
    });
    await deletePatientTag(row.tagId);
    ElMessage.success('删除成功');
    loadData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败');
    }
  }
};
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  loadData();
};
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val;
  loadData();
};
onMounted(() => {
  loadData();
});
</script>
