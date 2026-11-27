<template>
  <div>
    <!-- 查询卡：条件已超过 4 个，按 .query-grid 口径排（每条件 240px、日期区间 312px） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="query-grid-wrap">
        <el-form class="query-grid patient-query-grid" label-width="70px" @submit.prevent>
          <el-form-item label="患者姓名">
            <el-input v-model="searchForm.patientName" clearable placeholder="请输入患者姓名"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="患者号">
            <el-input v-model="searchForm.patientNo" clearable placeholder="请输入患者号"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="身份证号">
            <el-input v-model="searchForm.idCard" clearable placeholder="支持末 4 位模糊查询"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="searchForm.phone" clearable placeholder="请输入手机号"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="searchForm.gender" clearable placeholder="全部">
              <el-option v-for="g in PATIENT_GENDER_OPTIONS" :key="g.value" :label="g.label" :value="g.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="婚姻状况">
            <el-select v-model="searchForm.maritalStatus" clearable placeholder="全部">
              <el-option v-for="m in MARITAL_OPTIONS" :key="m.value" :label="m.label" :value="m.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="费别">
            <el-select v-model="searchForm.medicalInsuranceType" :fit-input-width="false" clearable
                       placeholder="全部">
              <el-option v-for="item in feeTypeOptions" :key="item.dictValue" :label="item.dictLabel"
                         :value="item.dictValue"/>
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="searchForm.tagId" :fit-input-width="false" clearable placeholder="全部">
              <el-option v-for="tag in tagList" :key="tag.tagId" :label="tag.tagName" :value="tag.tagId"/>
            </el-select>
          </el-form-item>
          <el-form-item class="is-daterange" label="建档日期">
            <el-date-picker v-model="searchForm.createDateRange" end-placeholder="结束" range-separator="至"
                            start-placeholder="开始" type="daterange" value-format="YYYY-MM-DD"/>
          </el-form-item>
        </el-form>
        <div class="query-grid-ops flex shrink-0 items-start gap-3">
          <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          <el-button v-perm="'patient:add'" :icon="Plus" type="primary" @click="openAdd">新增患者</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡：数据列 min-width 摊满卡片宽度（固定 width 会在宽屏右侧留一条空白带） -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="patients" :max-height="tableMaxHeight" stripe style="width: 100%">
        <!-- 患者号列同时承载标签：简写 chip + 色底，标签多时向下换行（行高增加可接受） -->
        <el-table-column label="患者号/标签" min-width="220">
          <template #default="{ row }">
            <div class="font-mono text-slate-700">{{ row.patientNo }}</div>
            <div v-if="(row.tags || []).length" class="mt-1 flex flex-wrap gap-1">
              <span v-for="tag in visibleTags(row)" :key="tag.tagId"
                    :style="{ backgroundColor: tag.tagColor || '#409EFF' }"
                    :title="tagChipTitle(tag)"
                    class="inline-block cursor-default rounded px-1 py-0.5 text-xs font-medium leading-4 text-white">
                {{ tagChipText(tag) }}
              </span>
              <span
                  v-if="!expandedTagRows.has(row.id) && (row.tags || []).length > TAG_LIST_VISIBLE_LIMIT"
                  class="cursor-pointer rounded px-1 py-0.5 text-xs leading-4 text-slate-500 hover:text-slate-700"
                  title="展开剩余标签"
                  @click.stop="toggleTagExpand(row.id)">
                +{{ (row.tags || []).length - TAG_LIST_VISIBLE_LIMIT }}
              </span>
              <span v-else-if="expandedTagRows.has(row.id)"
                    class="cursor-pointer rounded px-1 py-0.5 text-xs leading-4 text-slate-400 hover:text-slate-600"
                    @click.stop="toggleTagExpand(row.id)">收起</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="姓名" min-width="110">
          <template #default="{ row }">
            <span class="font-medium text-slate-800">{{ row.patientName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="性别/年龄" min-width="120">
          <template #default="{ row }">
            <div>{{ patientGenderText(row.gender) }} / {{ patientAgeText(row.age) }}</div>
            <div class="text-xs text-slate-400">{{ maritalStatusText(row.maritalStatus) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="身份证号" min-width="150">
          <template #default="{ row }">
            <span class="font-mono">{{ row.idCardMasked || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="联系电话/住址" min-width="180">
          <template #default="{ row }">
            <div>{{ row.phoneMasked || '' }}</div>
            <div class="text-xs text-slate-400" :title="row.addressMasked">{{ row.addressMasked || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="费别/医保" min-width="150">
          <template #default="{ row }">
            <div>{{ row.medicalInsuranceType || '' }}</div>
            <div v-if="row.medicalInsuranceNoMasked" class="font-mono text-xs text-slate-400">
              {{ row.medicalInsuranceNoMasked }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="就诊次数" min-width="100">
          <template #default="{ row }">
            <div>挂号 {{ row.appointCount ?? 0 }}</div>
            <div class="text-xs text-slate-400">就诊 {{ row.visitCount ?? 0 }}</div>
          </template>
        </el-table-column>
        <el-table-column label="最近就诊" min-width="190">
          <template #default="{ row }">
            <div>{{ fmtDateTime(row.lastVisitTime) || '' }}</div>
            <div class="text-xs text-slate-400">
              {{ visitLine(row.lastVisitDeptName, row.lastVisitDoctorName) }}
            </div>
            <div v-if="row.firstVisitTime" class="text-xs text-slate-400">
              首次 {{ fmtDateTime(row.firstVisitTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="建档时间/状态" min-width="180">
          <template #default="{ row }">
            <div>{{ fmtDateTime(row.createTime) }}</div>
            <div class="mt-0.5">
              <span :class="row.status === 1 ? 'bg-blue-100 text-blue-700' : 'bg-slate-100 text-slate-600'"
                    class="inline-block rounded px-1.5 py-0.5 text-xs">
                {{ patientStatusText(row.status) }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column align="right" fixed="right" label="操作" width="190">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click.stop="handleViewDetail(row)">详情</el-button>
            <!-- 患者标签是窗口/护士的运营标记（VIP、欠费、注意事项）：超级管理员的工作对象是配置不是患者，入口不给 -->
            <el-button v-if="!isSystemAdmin" link size="small" type="primary" @click.stop="openTagDialog(row)">标签</el-button>
            <el-button v-perm="'patient:edit'" link size="small" type="primary" @click.stop="openEdit(row)">编辑</el-button>
            <el-button v-perm="'patient:edit'" link size="small" type="danger" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="patients.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无匹配的患者记录
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

    <PatientDetailDialog
        v-model="showDetailDialog"
        :patient="selectedPatient"
        :patient-id="selectedPatientId"
    />

    <!-- 患者标签维护：挂在患者身上（不是标签字典本身，字典在「患者中心 → 患者标签」） -->
    <PatientTagsEdit v-model="showTagDialog" :patient="tagPatient" @changed="loadData"/>

    <!-- 新增/修改共用一个弹框组件：row 为空走新增、有值走修改 -->
    <PatientsEdit v-model="showEditDialog" :row="editRow" @saved="loadData"/>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {deletePatient, getPatientList} from '@/api/patient';
import {getDictDataList, getPatientTagList} from '@/api/system';
import {PATIENT_GENDER_OPTIONS, patientAgeText, patientGenderText} from '@/lib/patientGender';
import {maritalStatusText, patientStatusText} from '@/lib/patientField';
import {DICT_TYPE} from '@/lib/dict-cache';
import {TAG_LIST_VISIBLE_LIMIT, tagChipText, tagChipTitle} from '@/lib/patientTag';
// 患者详情弹框统一走通用组件（原页面内联实现已删除：它读的 medicalRecords / prescriptions /
// inspections / laboratories 字段并不在返回体里，4 个 tab 恒为空）
import PatientDetailDialog from '@/components/his/PatientDetailDialog.vue';
import PatientTagsEdit from './PatientTagsEdit.vue';
import PatientsEdit from './PatientsEdit.vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {isSystemAdminRole} from '@/lib/role-workspace';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

// 婚姻状况码值与 PatientsEdit 的 el-option 一一对应（0 是非 nil 值，不能换成 null）
const MARITAL_OPTIONS = [
  {label: '未婚', value: 0},
  {label: '已婚', value: 1},
  {label: '离异', value: 2},
  {label: '丧偶', value: 3},
];

const searchForm = ref({
  patientName: '',
  patientNo: '',
  idCard: '',
  phone: '',
  gender: null,
  maritalStatus: null,
  medicalInsuranceType: null,
  tagId: null,
  createDateRange: [],
});
// 费别下拉（字典 medical_insurance_type，启用 5 项含自费；值即中文费别名）
const feeTypeOptions = ref([]);
// 详情弹框：selectedPatient 只用于「打开瞬间先用列表行数据渲染头部」避免闪白，
// 完整数据由 PatientDetailDialog 按 patientId 自己取（主档详情 + CDR 全景时间轴）
const selectedPatient = ref(null);
const selectedPatientId = ref('');
const showDetailDialog = ref(false);
const showEditDialog = ref(false);
// 传给弹框的当前行：null = 新增
const editRow = ref(null);
// 标签维护弹框：当前患者行
const showTagDialog = ref(false);
const tagPatient = ref(null);
// 超级管理员不显示「标签」入口（判定口径见 lib/role-workspace 的 isSystemAdminRole；
// 切角色会整页刷新，构建时读一次 localStorage 即可）
const isSystemAdmin = isSystemAdminRole(localStorage.getItem('currentRole'));
const loading = ref(false);
const patients = ref([]);
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();

const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  searchForm.value = {
    patientName: '',
    patientNo: '',
    idCard: '',
    phone: '',
    gender: null,
    maritalStatus: null,
    medicalInsuranceType: null,
    tagId: null,
    createDateRange: [],
  };
  handleSearch();
};
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  pagination.value.pageNum = 1;
  loadData();
};
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val;
  loadData();
};
const loadData = async () => {
  loading.value = true;
  try {
    const f = searchForm.value;
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    };
    if (f.patientName) params.patientName = f.patientName;
    if (f.patientNo) params.patientNo = f.patientNo;
    if (f.idCard) params.idCard = f.idCard;
    if (f.phone) params.phone = f.phone;
    if (f.gender !== null && f.gender !== undefined) params.gender = f.gender;
    if (f.maritalStatus !== null && f.maritalStatus !== undefined) params.maritalStatus = f.maritalStatus;
    if (f.medicalInsuranceType) params.medicalInsuranceType = f.medicalInsuranceType;
    if (f.tagId) params.tagId = f.tagId;
    if (Array.isArray(f.createDateRange) && f.createDateRange.length === 2) {
      params.createDateStart = f.createDateRange[0];
      params.createDateEnd = f.createDateRange[1];
    }
    const res = await getPatientList(params);
    // 标签由 listPage 批量回填（row.tags），不再逐行发请求
    patients.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载患者列表失败:', error);
  } finally {
    loading.value = false;
  }
};
const handleViewDetail = (row) => {
  if (!row?.id)
    return;
  selectedPatient.value = row;
  selectedPatientId.value = String(row.id);
  showDetailDialog.value = true;
};
const openAdd = () => {
  editRow.value = null;
  showEditDialog.value = true;
};
const openEdit = (row) => {
  editRow.value = row;
  showEditDialog.value = true;
};
const openTagDialog = (row) => {
  tagPatient.value = row;
  showTagDialog.value = true;
};
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该患者吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await deletePatient(row.id);
    ElMessage.success('删除成功');
    loadData();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{

  }
};
const fmtDateTime = (v) => (v ? String(v).slice(0, 10) : '');
const visitLine = (deptName, doctorName) =>
    [deptName, doctorName].filter(Boolean).join(' · ') || '';
// 患者标签列表（筛选下拉用；患者身上已挂的标签由 listPage 的 row.tags 带回）
const tagList = ref([]);
// 标签折叠状态（按行记忆）：超过 TAG_LIST_VISIBLE_LIMIT 折成「+N」，点击展开/收起
const expandedTagRows = ref(new Set());
const toggleTagExpand = (id) => {
  const next = new Set(expandedTagRows.value);
  if (next.has(id)) {
    next.delete(id);
  } else {
    next.add(id);
  }
  expandedTagRows.value = next;
};
const visibleTags = (row) => {
  const tags = row.tags || [];
  return expandedTagRows.value.has(row.id) ? tags : tags.slice(0, TAG_LIST_VISIBLE_LIMIT);
};
const loadTagList = async () => {
  try {
    const res = await getPatientTagList({});
    tagList.value = res.data?.records || res.data || [];
  } catch (error) {
    console.error('加载标签列表失败:', error);
  }
};
onMounted(() => {
  loadData();
  loadTagList();
  getDictDataList(DICT_TYPE.MEDICAL_INSURANCE_TYPE)
      .then(res => { feeTypeOptions.value = res.data || []; })
      .catch(() => { feeTypeOptions.value = []; });
});
</script>

<style scoped>
/* 查询条：固定 5 列（每格 240px），条件按格子左右对齐、不随窗口拉伸；
   建档日期区间占 2 格（312px 控件放得下，右侧留白不影响列对齐）。
   窄屏降到 4 列，避免右侧条件被裁掉。 */
.patient-query-grid {
  display: grid;
  grid-template-columns: repeat(5, 240px);
  gap: 12px 16px;
  align-items: start;
  flex: none;
}

.patient-query-grid .el-form-item {
  margin: 0;
  width: 240px;
}

.patient-query-grid .el-form-item.is-daterange {
  width: 312px;
  grid-column: span 2;
}

@media (max-width: 1500px) {
  .patient-query-grid {
    grid-template-columns: repeat(4, 240px);
  }
}
</style>
