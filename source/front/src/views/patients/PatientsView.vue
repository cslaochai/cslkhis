<template>
  <div>
    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="患者姓名">
            <el-input v-model="searchForm.patientName" class="!w-40" clearable placeholder="请输入患者姓名"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="患者号">
            <el-input v-model="searchForm.patientNo" class="!w-36" clearable placeholder="请输入患者号"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="searchForm.phone" class="!w-36" clearable placeholder="请输入手机号"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="searchForm.gender" class="!w-24" clearable placeholder="全部">
              <el-option :value="1" label="男"/>
              <el-option :value="2" label="女"/>
            </el-select>
          </el-form-item>
          <el-form-item label="费别">
            <el-select v-model="searchForm.medicalInsuranceType" class="!w-36" clearable placeholder="全部">
              <el-option v-for="item in feeTypeOptions" :key="item.dictValue" :label="item.dictLabel"
                         :value="item.dictValue"/>
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="searchForm.tagId" class="!w-36" clearable placeholder="全部">
              <el-option v-for="tag in tagList" :key="tag.tagId" :label="tag.tagName" :value="tag.tagId"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'patient:add'" :icon="Plus" type="primary" @click="openAdd">新增患者
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="patients" :max-height="tableMaxHeight" stripe style="width: 100%">
        <el-table-column class-name="font-mono" label="患者号" prop="patientNo" width="150"/>
        <el-table-column label="姓名" min-width="170">
          <template #default="{ row }">
            <div class="font-medium text-slate-800">{{ row.patientName }}</div>
            <div v-if="(patientTagsMap[row.id] || []).length" class="mt-1 flex flex-wrap gap-1">
              <span v-for="tag in visibleTags(row)" :key="tag.tagId"
                    :style="{ backgroundColor: tag.tagColor || '#409EFF' }"
                    :title="tagChipTitle(tag)"
                    class="inline-block cursor-default rounded px-1 py-0.5 font-medium leading-4 text-white">
                {{ tagChipText(tag) }}
              </span>
              <span
                  v-if="!expandedTagRows.has(row.id) && (patientTagsMap[row.id] || []).length > TAG_LIST_VISIBLE_LIMIT"
                  class="cursor-pointer rounded px-1 py-0.5 leading-4 text-slate-500 hover:text-slate-700"
                  title="展开剩余标签"
                  @click.stop="toggleTagExpand(row.id)">
                +{{ (patientTagsMap[row.id] || []).length - TAG_LIST_VISIBLE_LIMIT }}
              </span>
              <span v-else-if="expandedTagRows.has(row.id)"
                    class="cursor-pointer rounded px-1 py-0.5 leading-4 text-slate-400 hover:text-slate-600"
                    @click.stop="toggleTagExpand(row.id)">收起</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="性别/年龄" width="150">
          <template #default="{ row }">{{ patientGenderText(row.gender) }} / {{ patientAgeText(row.age) }}</template>
        </el-table-column>
        <el-table-column label="联系电话" width="150">
          <template #default="{ row }">{{ row.phoneMasked }}</template>
        </el-table-column>
        <el-table-column label="建档时间" width="150">
          <template #default="{ row }">{{ fmtDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="最近就诊" width="300">
          <template #default="{ row }">
            <div>{{ fmtDateTime(row.lastVisitTime) }}</div>
            <div v-if="row.lastVisitDeptName || row.lastVisitDoctorName"
                 class="text-slate-400">
              {{ [row.lastVisitDeptName, row.lastVisitDoctorName].filter(Boolean).join(' · ') }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="医保信息" width="170">
          <template #default="{ row }">
            <div>{{ row.medicalInsuranceType }}</div>
            <div v-if="row.medicalInsuranceNoMasked" class="font-mono text-slate-400">
              {{ row.medicalInsuranceNoMasked }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="首次就诊" width="300">
          <template #default="{ row }">
            <div>{{ fmtDateTime(row.firstVisitTime) }}</div>
            <div v-if="row.firstVisitDeptName || row.firstVisitDoctorName"
                 class="text-slate-400">
              {{ [row.firstVisitDeptName, row.firstVisitDoctorName].filter(Boolean).join(' · ') }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="血型" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.bloodType" effect="plain" size="small">{{ row.bloodType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="过敏史" prop="allergyHistory" show-overflow-tooltip width="200"/>
        <el-table-column align="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click.stop="handleViewDetail(row)">详情
            </el-button>
            <el-button v-perm="'patient:edit'" link size="small" type="primary" @click.stop="openEdit(row)">编辑
            </el-button>
            <el-button link size="small" type="danger" @click.stop="handleDelete(row)">删除</el-button>
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

    <!-- 新增/修改共用一个弹框组件：row 为空走新增、有值走修改 -->
    <PatientsEdit v-model="showEditDialog" :row="editRow" @saved="loadData"/>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {Plus, Refresh, Search} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {deletePatient, getPatientList, getPatientTags} from '@/api/patient';
import {getDictDataList, getPatientTagList} from '@/api/system';
import {patientAgeText, patientGenderText} from '@/lib/patientGender';
import {DICT_TYPE} from '@/lib/dict-cache';
import {TAG_LIST_VISIBLE_LIMIT, tagChipText, tagChipTitle} from '@/lib/patientTag';
// 患者详情弹框统一走通用组件（原页面内联实现已删除：它读的 medicalRecords / prescriptions /
// inspections / laboratories 字段并不在返回体里，4 个 tab 恒为空）
import PatientDetailDialog from '@/components/his/PatientDetailDialog.vue';
import PatientsEdit from './PatientsEdit.vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const searchForm = ref({
  patientName: '',
  patientNo: '',
  phone: '',
  gender: null,
  medicalInsuranceType: null,
  tagId: null,
});
// 费别下拉（字典 medical_insurance_type，标准 5 项含自费；值即中文费别名）
const feeTypeOptions = ref([]);
// 详情弹框：selectedPatient 只用于「打开瞬间先用列表行数据渲染头部」避免闪白，
// 完整数据由 PatientDetailDialog 按 patientId 自己取（主档详情 + CDR 全景时间轴）
const selectedPatient = ref(null);
const selectedPatientId = ref('');
const showDetailDialog = ref(false);
const showEditDialog = ref(false);
// 传给弹框的当前行：null = 新增
const editRow = ref(null);
const loading = ref(false);
const patients = ref([]);
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const statusColors = {
  0: 'bg-slate-100 text-slate-600 border-slate-200',
  1: 'bg-blue-100 text-blue-700 border-blue-200',
};
const statusMap = {
  0: '停用',
  1: '正常',
};
// 性别文案统一走 lib/patientGender（sys_gender 口径：1男 2女 9未知，0 等异常码值渲染成「未知(0)」而不是"女"）
// 婚姻状况文案统一走 @/lib/patientField（0未婚/1已婚/2离异/3丧偶，异常码值渲染「未知(n)」）。
// 原地的 maritalStatusMap 从未被引用，已删除 —— 口径只留 lib 一份。
// 费别（参保类型）统一走字典 medical_insurance_type，患者主档不再有独立的 patient_type 字段
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  searchForm.value = {
    patientName: '',
    patientNo: '',
    phone: '',
    gender: null,
    medicalInsuranceType: null,
    tagId: null,
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
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    };
    if (searchForm.value.patientName)
      params.patientName = searchForm.value.patientName;
    if (searchForm.value.patientNo)
      params.patientNo = searchForm.value.patientNo;
    if (searchForm.value.phone)
      params.phone = searchForm.value.phone;
    if (searchForm.value.gender !== null)
      params.gender = searchForm.value.gender;
    if (searchForm.value.medicalInsuranceType)
      params.medicalInsuranceType = searchForm.value.medicalInsuranceType;
    if (searchForm.value.tagId)
      params.tagId = searchForm.value.tagId;
    const res = await getPatientList(params);
    patients.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
    // 批量加载患者标签
    if (patients.value.length > 0) {
      const patientIds = patients.value.map(p => p.id);
      await loadPatientTagsBatch(patientIds);
    }
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
const openEdit = (row) => {
  editRow.value = row;
  showEditDialog.value = true;
};
const handleCopyName = (name) => {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(name).then(() => {
      ElMessage.success('已复制');
    }).catch(() =>  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
      
    });
  } else {
    // 降级方案
    const textarea = document.createElement('textarea');
    textarea.value = name;
    document.body.appendChild(textarea);
    textarea.select();
    document.execCommand('copy');
    document.body.removeChild(textarea);
    ElMessage.success('已复制');
  }
};
const fmtDateTime = (v) => (v ? v.slice(0, 10) : '');
// 患者标签列表
const tagList = ref([]);
const patientTagsMap = ref({});
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
  const tags = patientTagsMap.value[row.id] || [];
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
// 加载患者标签（批量）
const loadPatientTagsBatch = async (patientIds) => {
  try {
    const promises = patientIds.map(id => getPatientTags({patientId: id}));
    const results = await Promise.all(promises);
    patientIds.forEach((id, index) => {
      patientTagsMap.value[id] = results[index]?.data || [];
    });
  } catch (error) {
    console.error('加载患者标签失败:', error);
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
