<template>
  <div>
    <!-- 搜索区域 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="科室名称">
            <el-input
                v-model="searchForm.deptName"
                clearable
                placeholder="请输入科室名称"
                @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item label="科室类型">
            <el-select v-model="searchForm.deptType" clearable placeholder="请选择科室类型">
              <el-option v-for="d in deptTypeOptions" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'org:dept:add'" :icon="Plus" type="primary" @click="handleAdd">新增科室</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" :max-height="tableMaxHeight" highlight-current-row
                stripe style="cursor: pointer" @row-click="handleRowClick">
        <el-table-column label="科室编码" prop="deptCode" width="120"/>
        <el-table-column label="科室名称" prop="deptName" width="150"/>
        <el-table-column label="科室类型" prop="deptType" width="120">
          <template #default="{ row }">
            {{ deptTypeLabel(deptTypeOptions, row.deptType) }}
          </template>
        </el-table-column>
        <el-table-column label="位置" prop="location" width="150">
          <template #default="{ row }">
            {{ row.location }}
          </template>
        </el-table-column>
        <el-table-column label="联系电话" prop="contactPhone" width="140">
          <template #default="{ row }">
            {{ row.contactPhone }}
          </template>
        </el-table-column>
        <el-table-column label="科室简介" min-width="200" prop="deptDesc">
          <template #default="{ row }">
            <span class="line-clamp-1">{{ row.deptDesc }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="排序" prop="sortOrder" width="80"/>
        <el-table-column label="状态" prop="status" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button v-perm="'org:dept:add'" :icon="Edit" link type="primary" @click.stop="handleEdit(row)">编辑
            </el-button>
            <el-button v-perm="'org:dept:delete'" :icon="Delete" link type="danger" @click.stop="handleDelete(row)">
              删除
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

    <DepartmentsEdit
        v-model="editVisible"
        :dept-tree="deptTree"
        :dept-type-options="deptTypeOptions"
        :row="editingRow"
        @saved="loadData"
    />

    <!-- 科室详情（只读，无保存按钮） -->
    <DepartmentsDetail
        v-model="detailVisible"
        :dept-tree="deptTree"
        :dept-type-options="deptTypeOptions"
        :id="detailId"
    />
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {deleteDepartment, getDepartmentList, getDepartmentTree, getDictDataMapList} from '@/api/system';
import {deptTypeLabel} from '@/lib/deptType';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import DepartmentsDetail from './DepartmentsDetail.vue';
import DepartmentsEdit from './DepartmentsEdit.vue';

const loading = ref(false);
const searchForm = reactive({
  deptName: '',
  deptType: null, // 搜索仍用单选，传单个值给后端
});
const tableData = ref([]);
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const editVisible = ref(false);
const editingRow = ref(null);
const detailVisible = ref(false);
const detailId = ref('');
// 科室树
const deptTree = ref([]);
const loadDeptTree = async () => {
  try {
    const res = await getDepartmentTree();
    if (res.code === 200) {
      deptTree.value = res.data || [];
    }
  } catch (error) {
    console.error('加载科室树失败:', error);
  }
};
/**
 * 科室类型选项 —— **唯一口径是字典 `his_dept_type`**，禁止在本页硬编码。
 *
 * 这里踩过一个静默 bug（2026-09-21）：原先本页写死 4 项
 * `[门诊科室1, 医技科室2, 药房3, 其他4]` —— 缺了 `4-住院科室`，
 * 而真实数据里 dept_type=4 是**住院科室**（医院信息系统/内科系统/外科系统…9 个），
 * 于是这 9 个科室在页面上全渲染成「其他」，与 dept_type=5 的 11 个真·其他科室
 * （院办/医务科/护理部…）混成一类 —— **不报错、也看不出错**。
 *
 * 口径三方对齐（实测 2026-09-21）：
 *   · 列注释：1-门诊科室 2-医技科室 3-药房 4-住院科室 5-其他
 *   · 字典 his_dept_type：同上（一致）
 *   · 数据分布：1=58 / 2=12 / 3=5 / 4=9 / 5=11（一致）
 * 所以字典就是权威，本页只消费不定义。
 */
const deptTypeOptions = ref([]);
const loadDeptTypeDict = async () => {
  try {
    const res = await getDictDataMapList('his_dept_type');
    deptTypeOptions.value = res.data?.['his_dept_type'] || [];
  } catch (error) {
    console.error('加载科室类型字典失败:', error);
  }
};
onMounted(() => {
  loadData();
  loadDeptTree();
  loadDeptTypeDict();
});
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      ...searchForm,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    };
    // 搜索时把单个值转成字符串传给后端（FIND_IN_SET 需要字符串）
    if (searchForm.deptType !== null) {
      params.deptType = String(searchForm.deptType);
    }
    const res = await getDepartmentList(params);
    if (res.code === 200) {
      tableData.value = res.data?.records || res.data || [];
      pagination.value.total = res.data?.total || tableData.value.length;
    }
  } catch (error) {
    console.error('加载科室列表失败:', error);
  } finally {
    loading.value = false;
  }
};
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  searchForm.deptName = '';
  searchForm.deptType = null;
  pagination.value.pageNum = 1;
  loadData();
};
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  loadData();
};
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val;
  loadData();
};
const handleAdd = () => {
  editingRow.value = null;
  editVisible.value = true;
};
const handleEdit = (row) => {
  editingRow.value = row;
  editVisible.value = true;
};
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除科室 "${row.deptName}" 吗？`, '删除确认', {
      type: 'warning',
    });
    const res = await deleteDepartment(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadData();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (error) {
    if (error !== 'cancel' && error.message) {
      ElMessage.error(error.message);
    }
  }
};
// 单击行查看详情
const handleRowClick = (row) => {
  detailId.value = String(row.id);
  detailVisible.value = true;
};
</script>
