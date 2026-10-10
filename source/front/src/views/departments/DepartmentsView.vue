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
            {{ getDeptTypeLabel(row.deptType) }}
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :close-on-click-modal="false"
        :title="dialogTitle"
        width="700px"
    >
      <el-form
          ref="formRef"
          :model="formData"
          :rules="rules"
          label-width="100px"
      >
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="科室名称" prop="deptName">
            <el-input v-model="formData.deptName" placeholder="请输入科室名称"/>
          </el-form-item>
          <el-form-item label="上级科室">
            <el-tree-select
                v-model="formData.parentId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'id', children: 'children' }"
                :render-after-expand="false"
                check-strictly
                class="w-full"
                clearable
                placeholder="请选择上级科室（不选则为顶级）"
            />
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="科室类型" prop="deptType">
            <el-select v-model="formData.deptType" :fit-input-width="false" class="w-full" multiple
                       placeholder="请选择科室类型">
              <el-option v-for="d in deptTypeOptions" :key="d.dictValue"
                         :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="排序号">
            <el-input-number v-model="formData.sortOrder" :min="0" class="w-full"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="位置">
            <el-input v-model="formData.location" placeholder="如：门诊楼A座3层"/>
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="formData.contactPhone" placeholder="请输入联系电话"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="科室负责人">
            <el-select v-model="formData.deptLeaderId" :fit-input-width="false" class="w-full" clearable
                       filterable
                       placeholder="请选择科室负责人">
              <el-option v-for="e in employeeList" :key="e.id"
                         :label="`${e.empName}（${e.title || '无职称'}）`" :value="e.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="formData.status">
              <el-radio :value="1">正常</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <el-form-item label="科室简介">
          <el-input v-model="formData.deptDesc" :rows="3" placeholder="请输入科室简介" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-perm="'org:dept:add'" :loading="submitLoading" type="primary" @click="handleSubmit">确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 科室详情弹框（只读，无保存按钮） -->
    <el-dialog v-model="detailVisible" destroy-on-close title="科室详情" width="700px">
      <el-form disabled label-width="100px">
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="科室名称">
            <el-input v-model="detailData.deptName"/>
          </el-form-item>
          <el-form-item label="上级科室">
            <el-input :value="getParentDeptName(detailData.parentId)"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="科室类型">
            <el-input :value="getDeptTypeLabel(detailData.deptType)"/>
          </el-form-item>
          <el-form-item label="排序号">
            <el-input-number v-model="detailData.sortOrder" :min="0" class="w-full"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="位置">
            <el-input v-model="detailData.location"/>
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="detailData.contactPhone"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="科室负责人">
            <el-input :value="detailData.deptLeaderName || '-'"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="detailData.status">
              <el-radio :value="1">正常</el-radio>
              <el-radio :value="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
        </div>
        <el-form-item label="科室简介">
          <el-input v-model="detailData.deptDesc" :rows="3" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  createDepartment,
  deleteDepartment,
  getDepartmentDetail,
  getDepartmentList,
  getDepartmentTree,
  getDictDataMapList,
  getEmployeeList,
  updateDepartment
} from '@/api/system';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

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
// 新增/编辑对话框
const dialogVisible = ref(false);
const dialogTitle = ref('新增科室');
const submitLoading = ref(false);
const formRef = ref(null);
const isEdit = ref(false);
// 详情对话框
const detailVisible = ref(false);
const detailData = reactive({
  id: null,
  deptCode: '',
  deptName: '',
  deptType: '',
  parentId: 0,
  sortOrder: 0,
  location: '',
  contactPhone: '',
  deptLeaderId: null,
  deptLeaderName: '',
  deptDesc: '',
  status: 1,
});
const formData = reactive({
  id: null,
  deptCode: '',
  deptName: '',
  deptType: [], // 多选，提交时 join(',')
  parentId: 0,
  sortOrder: 0,
  location: '',
  contactPhone: '',
  deptLeaderId: null,
  deptDesc: '',
  status: 1,
});
const rules = {
  deptName: [{required: true, message: '请输入科室名称', trigger: 'blur'}],
  deptType: [{required: true, message: '请选择科室类型', trigger: 'change'}],
};
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
// 员工列表（用于科室负责人下拉）
const employeeList = ref([]);
const loadEmployeeList = async () => {
  try {
    const res = await getEmployeeList({});
    employeeList.value = res.data || [];
  } catch (error) {
    console.error('加载员工列表失败:', error);
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
  loadEmployeeList();
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
const resetForm = () => {
  formData.id = null;
  formData.deptCode = '';
  formData.deptName = '';
  formData.deptType = [];
  formData.parentId = 0;
  formData.sortOrder = 0;
  formData.location = '';
  formData.contactPhone = '';
  formData.deptLeaderId = null;
  formData.deptDesc = '';
  formData.status = 1;
};
const handleAdd = () => {
  resetForm();
  isEdit.value = false;
  dialogTitle.value = '新增科室';
  dialogVisible.value = true;
};
const handleEdit = async (row) => {
  resetForm();
  isEdit.value = true;
  dialogTitle.value = '编辑科室';
  try {
    const res = await getDepartmentDetail(row.id);
    if (res.code === 200) {
      // 兼容后端返回的逗号分隔字符串，转成数组给 checkbox-group
      const deptTypeStr = res.data.deptType;
      formData.deptType = typeof deptTypeStr === 'string'
          ? deptTypeStr.split(',').map(Number).filter(n => !isNaN(n))
          : [deptTypeStr];
      Object.assign(formData, {...res.data, deptType: formData.deptType});
    }
  } catch (error) {
    ElMessage.error(error.message || '获取科室信息失败');
  }
  dialogVisible.value = true;
};
const handleSubmit = async () => {
  if (!formRef.value)
    return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  submitLoading.value = true;
  try {
    // 提交前把数组转成逗号分隔字符串
    const payload = {...formData, deptType: formData.deptType.join(',')};
    let res;
    if (isEdit.value) {
      res = await updateDepartment(payload);
    } else {
      res = await createDepartment(payload);
    }
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功');
      dialogVisible.value = false;
      loadData();
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  } finally {
    submitLoading.value = false;
  }
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
// 命中不到字典就出「未知(n)」—— 不回落成「其他」，否则脏值看起来像合法值（口径铁律）
const getDeptTypeLabel = (type) => {
  // 兼容逗号分隔的多值
  if (typeof type === 'string' && type.includes(',')) {
    return type.split(',').map(t => dictLabelText(deptTypeOptions.value, Number(t))).join('、');
  }
  return dictLabelText(deptTypeOptions.value, typeof type === 'string' ? Number(type) : type);
};
// 获取父级科室名称
const getParentDeptName = (parentId) => {
  const findParent = (nodes, id) => {
    for (const node of nodes) {
      if (String(node.id) === String(id))
        return node.deptName;
      if (node.children?.length) {
        const found = findParent(node.children, id);
        if (found)
          return found;
      }
    }
    return '';
  };
  if (!parentId || parentId === 0)
    return '顶级科室';
  return findParent(deptTree.value, parentId) || '未知';
};
// 单击行查看详情
const handleRowClick = async (row) => {
  try {
    const res = await getDepartmentDetail(row.id);
    if (res.code === 200) {
      Object.assign(detailData, res.data);
      detailVisible.value = true;
    }
  } catch (error) {
    ElMessage.error(error.message || '获取科室详情失败');
  }
};
</script>
