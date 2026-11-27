<template>
  <div v-loading="loading">
    <!-- 查询条件 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="状态">
          <el-select v-model="query.status" clearable placeholder="状态" style="width: 150px"
                     @change="handleSearch">
            <el-option v-for="o in CORRECTION_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="更正类型">
          <el-select v-model="query.correctionType" clearable placeholder="类型" style="width: 150px"
                     @change="handleSearch">
            <el-option v-for="o in CORRECTION_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="关键字">
          <el-input
              v-model="query.keyword"
              :prefix-icon="Search"
              class="!w-64"
              clearable
              data-testid="correction-keyword"
              placeholder="申请单号 / 病历号 / 患者姓名"
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 台账 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="list" :max-height="tableMaxHeight" empty-text="暂无更正申请记录" stripe style="width: 100%">
        <el-table-column label="申请单号" width="170">
          <template #default="{ row }">
            <span class="font-mono text-xs font-medium text-slate-700">{{ row.requestNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="病历 / 患者" min-width="170">
          <template #default="{ row }">
            <div class="text-xs">{{ row.recordNo || '' }}</div>
            <div class="text-xs text-slate-400">{{ row.patientName || '' }} <span
                v-if="row.deptName">· {{ row.deptName }}</span></div>
          </template>
        </el-table-column>
        <el-table-column label="原经治" width="100" prop="originalDoctorName"/>
        <el-table-column label="申请人" width="100" prop="applicantName"/>
        <el-table-column label="更正类型" width="110">
          <template #default="{ row }">
            <span class="text-xs">{{ row.correctionTypeText || correctionTypeText(row.correctionType) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="correctionStatusTag(row.status)" effect="plain" size="small">
              {{ row.statusText || correctionStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="160">
          <template #default="{ row }">
            <span class="text-xs text-slate-500">{{ row.createTime || '' }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="{ row }">
            <el-button link size="small" type="primary" @click.stop="openDetail(row)">详情</el-button>
            <el-button
                v-if="Number(row.status) === 1"
                v-perm="'emr:correction:deptApprove'"
                link size="small" type="warning"
                @click.stop="openOpinion(row, 'dept')"
            >
              科室审核
            </el-button>
            <el-button
                v-if="Number(row.status) === 2"
                v-perm="'emr:correction:medicalApprove'"
                link size="small" type="warning"
                @click.stop="openOpinion(row, 'medical')"
            >
              医务审批
            </el-button>
            <el-button
                v-if="[1, 2].includes(Number(row.status))"
                v-perm="'emr:correction:apply'"
                link size="small" type="danger"
                @click.stop="revoke(row)"
            >
              撤回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 申请单详情抽屉 -->
    <el-drawer v-model="detailVisible" direction="rtl" size="56%">
      <template #header>
        <div class="flex items-center gap-3">
          <span class="text-base font-semibold text-slate-900">病历更正申请单</span>
          <span class="text-sm text-slate-400">{{ detail?.requestNo }}</span>
        </div>
      </template>
      <div v-loading="detailLoading">
        <div v-if="detail" class="space-y-5">
          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">一、病历信息</h3>
            <div class="grid grid-cols-3 gap-3 rounded border border-slate-200 p-3 text-sm">
              <div><span class="text-slate-400">病历号：</span>{{ detail.recordNo || '' }}</div>
              <div><span class="text-slate-400">患者：</span>{{ detail.patientName || '' }}</div>
              <div><span class="text-slate-400">科室：</span>{{ detail.deptName || '' }}</div>
              <div><span class="text-slate-400">原经治：</span>{{ detail.originalDoctorName || '' }}</div>
              <div><span class="text-slate-400">申请人：</span>{{ detail.applicantName || '' }}</div>
              <div>
                <span class="text-slate-400">类型：</span>{{ detail.correctionTypeText || correctionTypeText(detail.correctionType) }}
              </div>
            </div>
          </div>

          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">二、申请内容</h3>
            <div class="space-y-2 rounded border border-slate-200 p-3 text-sm">
              <div><span class="font-medium text-slate-500">更正原因：</span>{{ detail.reason || '' }}</div>
              <div><span class="font-medium text-slate-500">拟更正内容：</span>{{ detail.plannedContent || '' }}</div>
              <div v-if="detail.remark"><span class="font-medium text-slate-500">备注：</span>{{ detail.remark }}</div>
            </div>
          </div>

          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">三、审批留痕</h3>
            <div class="space-y-3">
              <div class="rounded border border-slate-200 p-3 text-sm">
                <div class="flex items-center justify-between">
                  <span class="font-medium text-slate-700">科室初审</span>
                  <el-tag v-if="detail.deptApproverName" effect="plain" size="small" type="success">
                    {{ detail.deptApproverName }}
                  </el-tag>
                </div>
                <div class="mt-1 text-xs text-slate-500">
                  时间：{{ detail.deptApproveTime || '' }}
                </div>
                <div class="mt-1 text-xs text-slate-600">意见：{{ detail.deptComment || '' }}</div>
              </div>
              <div class="rounded border border-slate-200 p-3 text-sm">
                <div class="flex items-center justify-between">
                  <span class="font-medium text-slate-700">医务终审</span>
                  <el-tag v-if="detail.medicalApproverName" effect="plain" size="small" type="success">
                    {{ detail.medicalApproverName }}
                  </el-tag>
                </div>
                <div class="mt-1 text-xs text-slate-500">
                  时间：{{ detail.medicalApproveTime || '' }}
                </div>
                <div class="mt-1 text-xs text-slate-600">意见：{{ detail.medicalComment || '' }}</div>
              </div>
            </div>
          </div>

          <div>
            <h3 class="mb-2 border-l-4 border-blue-500 pl-2 text-sm font-bold text-slate-700">四、回写留痕</h3>
            <div class="rounded border border-slate-200 p-3 text-sm">
              <template v-if="detail.modifierName">
                <div><span class="text-slate-400">实际修改人：</span>{{ detail.modifierName }}</div>
                <div class="mt-1 text-xs text-slate-500">回写完成时间：{{ detail.modifyTime || '' }}</div>
              </template>
              <span v-else class="text-xs text-slate-400">尚未回写。</span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <!-- 审批意见弹框 -->
    <EmrCorrectionOpinionDialog
        v-model="opinionVisible"
        :row="opinionRow"
        :stage="opinionStage"
        @saved="loadData"
    />
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {
  CORRECTION_STATUS_OPTIONS,
  CORRECTION_TYPE_OPTIONS,
  correctionStatusTag,
  correctionStatusText,
  correctionTypeText,
} from '@/lib/emrCorrection';
import {
  getCorrectionDetail,
  getCorrectionPage,
  revokeCorrection,
} from '@/api/emrCorrection';
import EmrCorrectionOpinionDialog from './EmrCorrectionOpinionDialog.vue';

const loading = ref(false);
const list = ref([]);
const total = ref(0);
const query = reactive({
  status: undefined,
  correctionType: undefined,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});

const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();

const buildQuery = () => {
  const params = {pageNum: query.pageNum, pageSize: query.pageSize};
  if (query.status !== undefined && query.status !== null)
    params.status = query.status;
  if (query.correctionType !== undefined && query.correctionType !== null)
    params.correctionType = query.correctionType;
  if (query.keyword)
    params.keyword = query.keyword.trim();
  return params;
};

const loadData = async () => {
  loading.value = true;
  try {
    const res = await getCorrectionPage(buildQuery());
    list.value = res.data?.records || [];
    total.value = Number(res.data?.total ?? 0);
  } catch (error) {
    console.error('加载更正申请台账失败:', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  query.pageNum = 1;
  loadData();
};
const handleReset = () => {
  query.status = undefined;
  query.correctionType = undefined;
  query.keyword = '';
  handleSearch();
};
const handleSizeChange = (val) => {
  query.pageSize = val;
  query.pageNum = 1;
  loadData();
};
const handleCurrentChange = (val) => {
  query.pageNum = val;
  loadData();
};

// ---------------- 详情抽屉 ----------------
const detailVisible = ref(false);
const detailLoading = ref(false);
const detail = ref(null);

const openDetail = async (row) => {
  detailVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    const res = await getCorrectionDetail(row.id);
    detail.value = res.data || null;
  } catch (error) {
    console.error('加载更正申请单详情失败:', error);
  } finally {
    detailLoading.value = false;
  }
};

// ---------------- 审批 ----------------
const opinionVisible = ref(false);
const opinionRow = ref(null);
const opinionStage = ref('dept');

const openOpinion = (row, stage) => {
  opinionRow.value = row;
  opinionStage.value = stage;
  opinionVisible.value = true;
};

const revoke = async (row) => {
  try {
    await ElMessageBox.confirm(`确认撤回申请单 ${row.requestNo}？撤回后在途流程终止。`, '撤回更正申请', {
      type: 'warning',
      confirmButtonText: '撤回',
      cancelButtonText: '取消',
    });
  } catch {
    return;
  }
  try {
    await revokeCorrection(row.id);
    ElMessage.success('已撤回');
    await loadData();
  } catch (error) {
    console.error('撤回更正申请失败:', error);
  }
};

onMounted(() => {
  loadData();
});
</script>
