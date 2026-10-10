<template>
  <div v-loading="loading">
    <!-- 查询条件 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="searchForm" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input
              v-model="searchForm.keyword"
              :prefix-icon="Search"
              class="!w-64"
              clearable
              data-testid="mr-search"
              placeholder="患者姓名/病历号/挂号单号"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="病历状态">
          <el-select v-model="searchForm.recordStatus" class="!w-36" clearable data-testid="mr-status-filter"
                     placeholder="病历状态" @change="handleSearch">
            <el-option v-for="item in RECORD_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value"/>
          </el-select>
        </el-form-item>
        <!-- data-testid 必须挂在**外层 div** 上：el-date-picker **不透传**未声明的 attr 到根元素
             （实测直接写在组件上时 DOM 里根本不出现；而 el-input / el-select 会透传）。
             给验证脚本一个稳定锚点，别去依赖 .el-date-editor--daterange 这类内部类名。 -->
        <el-form-item label="就诊日期">
          <div data-testid="mr-date-range">
            <el-date-picker
                v-model="searchForm.visitDateRange"
                class="!w-72"
                clearable
                end-placeholder="就诊日期止"
                range-separator="~"
                start-placeholder="就诊日期起"
                type="daterange"
                value-format="YYYY-MM-DD"
                @change="handleSearch"
            />
          </div>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分页列表 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="records" :max-height="tableMaxHeight" empty-text="暂无病历记录" stripe style="width: 100%">
        <el-table-column label="病历号" prop="recordNo" width="180"/>
        <el-table-column label="患者" prop="patientName" width="90"/>
        <el-table-column align="center" label="性别/年龄" width="110">
          <template #default="{ row }">
            <span class="text-xs text-slate-600">{{ patientGenderText(row.gender) }} {{ row.age ?? '-' }}岁</span>
          </template>
        </el-table-column>
        <el-table-column label="科室" prop="deptName" width="120"/>
        <el-table-column label="医生" prop="doctorName" width="90"/>
        <el-table-column label="就诊日期" prop="visitDate" width="120"/>
        <el-table-column label="诊断" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.diagnosisName || row.diagnosis || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="病历状态" width="100">
          <template #default="{ row }">
            <el-tag :type="recordStatusTagType(row.recordStatus)" effect="plain" size="small">
              {{ recordStatusText(row.recordStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="reviewStatusTagType(row.reviewStatus)" effect="plain" size="small">
              {{ reviewStatusText(row.reviewStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="160">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click="openRecordPage(row)">
              <el-icon class="mr-0.5">
                <Document/>
              </el-icon>
              门诊病案首页
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页：在流内紧跟表格底 -->
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

    <!-- 门诊病案首页抽屉 -->
    <el-drawer v-model="drawerVisible" direction="rtl" size="60%">
      <template #header>
        <div class="flex items-center gap-3">
          <span class="text-base font-semibold text-slate-900">门（急）诊病历首页</span>
          <span class="text-sm text-slate-400">{{ selectedRecord?.recordNo }}</span>
        </div>
      </template>

      <div v-loading="detailLoading">
        <div v-if="detailData?.record" class="space-y-5">
          <!-- 一、患者基本信息 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">一、患者基本信息</h3>
            <div class="grid grid-cols-4 gap-3 rounded border border-slate-200 p-3 text-sm">
              <div><span class="text-slate-400">姓名：</span><span class="font-medium">{{
                  detailData.record.patientName
                }}</span></div>
              <div><span class="text-slate-400">性别：</span><span
                  class="font-medium">{{ patientGenderText(detailData.record.gender) }}</span></div>
              <div><span class="text-slate-400">年龄：</span><span class="font-medium">{{
                  detailData.record.age
                }}岁</span></div>
              <div><span class="text-slate-400">患者号：</span><span
                  class="font-mono font-medium">{{ detailData.record.patientNo }}</span></div>
            </div>
          </div>

          <!-- 二、就诊信息 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">二、就诊信息</h3>
            <div class="grid grid-cols-4 gap-3 rounded border border-slate-200 p-3 text-sm">
              <div><span class="text-slate-400">就诊日期：</span><span class="font-medium">{{
                  detailData.record.visitDate
                }}</span></div>
              <div><span class="text-slate-400">科室：</span><span class="font-medium">{{
                  detailData.record.deptName
                }}</span></div>
              <div><span class="text-slate-400">医生：</span><span class="font-medium">{{
                  detailData.record.doctorName
                }}</span></div>
              <div><span class="text-slate-400">挂号单号：</span><span
                  class="font-mono font-medium">{{ detailData.record.registNo || '-' }}</span></div>
            </div>
          </div>

          <!-- 三、病史 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">三、病史</h3>
            <div class="space-y-2 rounded border border-slate-200 p-3 text-sm">
              <div><span class="font-medium text-slate-500">主诉：</span><span
                  class="text-slate-700">{{ detailData.record.chiefComplaint || '-' }}</span></div>
              <div><span class="font-medium text-slate-500">现病史：</span><span
                  class="text-slate-700">{{ detailData.record.presentIllness || '-' }}</span></div>
              <div class="rounded border border-red-200 bg-red-50 p-2"><span
                  class="font-medium text-red-600">过敏史：</span><span
                  class="text-red-700">{{ detailData.record.allergyHistory || '无' }}</span></div>
              <div v-if="detailData.record.pastHistory"><span class="font-medium text-slate-500">既往史：</span><span
                  class="text-slate-700">{{ detailData.record.pastHistory }}</span></div>
            </div>
          </div>

          <!-- 四、体格检查 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">四、体格检查</h3>
            <div class="rounded border border-slate-200 p-3 text-sm">
              <div class="grid grid-cols-5 gap-3">
                <div><span class="text-slate-400">体温：</span><span
                    class="font-medium">{{ detailData.record.temperature || '-' }}℃</span></div>
                <div><span class="text-slate-400">脉搏：</span><span
                    class="font-medium">{{ detailData.record.pulse || '-' }}次/分</span></div>
                <div><span class="text-slate-400">呼吸：</span><span
                    class="font-medium">{{ detailData.record.respiration || '-' }}次/分</span></div>
                <div><span class="text-slate-400">血压：</span><span class="font-medium">{{
                    detailData.record.systolicPressure || '-'
                  }}/{{ detailData.record.diastolicPressure || '-' }}mmHg</span></div>
                <div></div>
              </div>
              <div v-if="detailData.record.generalCondition" class="mt-2"><span class="text-slate-400">一般情况：</span>{{
                  detailData.record.generalCondition
                }}
              </div>
              <div v-if="detailData.record.specialistExam" class="mt-2"><span
                  class="text-slate-400">专科检查：</span>{{ detailData.record.specialistExam }}
              </div>
            </div>
          </div>

          <!-- 五、诊断 -->
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">五、诊断</h3>
            <div class="rounded border border-slate-200 p-3 text-sm">
              <div class="flex items-center gap-2">
                <span class="font-medium text-slate-900">{{
                    detailData.record.diagnosisName || detailData.record.diagnosis || '-'
                  }}</span>
                <span v-if="detailData.record.diagnosisCode"
                      class="font-mono text-xs text-slate-400">（{{ detailData.record.diagnosisCode }}）</span>
              </div>
            </div>
          </div>

          <!-- 六、处方 -->
          <div v-if="detailData.prescriptions?.length">
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">六、处方</h3>
            <div v-for="p in detailData.prescriptions" :key="p.id"
                 class="mb-2 rounded border border-slate-200 p-3 text-sm">
              <div class="flex items-center justify-between">
                <span class="font-mono font-medium text-slate-900">{{ p.prescriptionNo }}</span>
                <span class="text-xs text-slate-400">{{
                    p.createTime ? String(p.createTime).substring(0, 10) : '-'
                  }}</span>
              </div>
              <div class="mt-1 text-xs text-slate-500">{{ p.diagnosis || '-' }} | {{ p.drugCount || 0 }}种药 |
                ¥{{ p.totalAmount || 0 }}
              </div>
            </div>
          </div>

          <!-- 七、检查/检验 -->
          <!-- 七、辅助检查
               字段名必须是 inspectionApplies / laboratoryApplies ——
               EmrRecordDetailVO 的顶层键就是这两个（见 his-emr/vo/EmrRecordDetailVO.java）。
               原先写的是 inspections / laboratories，两个键后端都不返回 → 本段**恒不渲染且不报错**，
               表现为"这个患者没做检查"，看不出是坏了（2026-09-23 修）。 -->
          <div v-if="detailData.inspectionApplies?.length || detailData.laboratoryApplies?.length">
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">七、辅助检查</h3>
            <div class="grid grid-cols-2 gap-3">
              <div v-for="ins in detailData.inspectionApplies" :key="ins.id"
                   class="rounded border border-slate-200 p-2 text-xs">
                <span class="font-medium text-slate-900">[检查]</span> {{ ins.inspectionItemName }}
                <span v-if="ins.bodyPart" class="text-slate-400">（{{ ins.bodyPart }}）</span>
              </div>
              <div v-for="lab in detailData.laboratoryApplies" :key="lab.id"
                   class="rounded border border-slate-200 p-2 text-xs">
                <span class="font-medium text-slate-900">[检验]</span> {{ lab.laboratoryItemName }}
                <span class="text-slate-400">（{{ lab.specimenType || '-' }}）</span>
              </div>
            </div>
          </div>

          <!-- 八、治疗方案 -->
          <div v-if="detailData.record.treatmentPlan">
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">八、治疗方案</h3>
            <div class="rounded border border-slate-200 p-3 text-sm text-slate-700">{{
                detailData.record.treatmentPlan
              }}
            </div>
          </div>

          <!-- 病历状态 -->
          <div class="mt-4 flex items-center justify-between border-t border-slate-200 pt-4 text-xs text-slate-400">
            <span>创建时间：{{ detailData.record.createTime || '-' }}</span>
            <span>提交时间：{{ detailData.record.submitTime || '-' }}</span>
            <span class="rounded bg-slate-100 px-2 py-0.5 font-medium text-slate-600">
              {{ recordStatusText(detailData.record.recordStatus) }}
            </span>
            <span class="rounded bg-slate-100 px-2 py-0.5 font-medium text-slate-600">
              {{ reviewStatusText(detailData.record.reviewStatus) }}
            </span>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {Document, Refresh, Search} from '@element-plus/icons-vue';
import {getRecordDetail, getRecordListPage} from '@/api/emr';
import {patientGenderText} from '@/lib/patientGender';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {
  RECORD_STATUS_OPTIONS,
  recordStatusTagType,
  recordStatusText,
  reviewStatusTagType,
  reviewStatusText,
} from '@/lib/recordStatus';

const loading = ref(false);
const records = ref([]);
const selectedRecord = ref(null);
const detailLoading = ref(false);
const detailData = ref(null);
const drawerVisible = ref(false);
const searchForm = ref({
  keyword: '',
  recordStatus: null,
  /**
   * 就诊日期区间（原为单个 `visitDate`）。2026-09-23 随 601 下线一并把口径统一成区间：
   * 单日只是区间的退化情形，而 `visitDateStart/End` 能覆盖「跨几天找一份病历」的真实用法。
   * 两者后端 DTO 都支持，二选一即可（见 MedicalRecordQueryPageDTO）。
   */
  visitDateRange: [],
});
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
// 状态口径统一走 lib/recordStatus（原先本页自带一份 statusMap/reviewMap，
// 与 601 RecordsView 的三元链重复且措辞不一致：未提交/待提交、已通过/审核通过）
/** 组装后端 MedicalRecordQueryDTO 入参 */
const buildQuery = () => {
  const query = {
    pageNum: pagination.value.pageNum,
    pageSize: pagination.value.pageSize,
  };
  if (searchForm.value.keyword)
    query.keyword = searchForm.value.keyword.trim();
  if (searchForm.value.recordStatus != null)
    query.recordStatus = searchForm.value.recordStatus;
  const [start, end] = searchForm.value.visitDateRange || [];
  if (start)
    query.visitDateStart = start;
  if (end)
    query.visitDateEnd = end;
  return query;
};
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getRecordListPage(buildQuery());
    records.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载病历列表失败:', error);
  } finally {
    loading.value = false;
  }
};
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  searchForm.value = {keyword: '', recordStatus: null, visitDateRange: []};
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
/** 打开门诊病案首页 */
const openRecordPage = async (row) => {
  selectedRecord.value = row;
  detailData.value = null;
  drawerVisible.value = true;
  detailLoading.value = true;
  try {
    const res = await getRecordDetail(row.id);
    detailData.value = res.data || {};
  } catch (error) {
    console.error('加载病案首页失败:', error);
  } finally {
    detailLoading.value = false;
  }
};
onMounted(() => {
  loadData();
});
</script>
