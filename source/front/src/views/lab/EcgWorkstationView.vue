<template>
  <div v-loading="loading" class="space-y-4">
    <!-- 筛选 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex flex-wrap items-center gap-3">
        <el-input v-model="keyword" :prefix-icon="Search" class="!w-72"
                  clearable placeholder="患者姓名 / 患者号 / 记录号 / 项目" @keyup.enter="refresh"/>
        <el-date-picker v-model="dateRange" class="!w-64" end-placeholder="截止日期"
                        start-placeholder="开始日期" type="daterange" value-format="YYYY-MM-DD" @change="refresh"/>
        <el-button type="primary" @click="refresh">查询</el-button>
        <el-button @click="keyword = ''; dateRange = []; refresh()">重置</el-button>
      </div>
    </div>

    <!-- 分栏 -->
    <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <el-tabs v-model="activeTab" class="px-4 pt-2" @tab-change="handleTabChange">
        <el-tab-pane v-for="t in TABS" :key="t.key" :name="t.key">
          <template #label>
            <span>{{ t.label }}</span>
            <span v-if="t.key !== 'all'" class="ml-1 text-xs text-slate-400">({{ tabCounts[t.key] ?? 0 }})</span>
          </template>
        </el-tab-pane>
      </el-tabs>

      <el-table :data="rows" size="small" style="width: 100%">
        <el-table-column label="记录号" prop="recordNo" width="170"/>
        <el-table-column label="患者" width="140">
          <template #default="{ row }">
            <span>{{ row.patientName }}</span>
            <span class="ml-1 text-xs text-slate-400">{{ patientGenderText(row.gender) }} {{ row.age }}岁</span>
          </template>
        </el-table-column>
        <el-table-column label="检查项目" min-width="150" prop="itemName"/>
        <el-table-column label="记录状态" width="100">
          <template #default="{ row }">
            <el-tag :type="recordTagType(row.recordStatus)" effect="plain" size="small">
              {{ row.recordStatusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="波形" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.waveId" effect="plain" size="small" type="success">
              {{ row.ecgTypeText || '常规' }}
            </el-tag>
            <span v-else class="text-xs text-slate-300">未采集</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="测量/Holter" width="100">
          <template #default="{ row }">
            <span class="text-xs">
              {{ [row.measureId ? '测量' : '', row.holterId ? 'Holter' : ''].filter(Boolean).join('/') || '—' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="报告状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.reportStatus)" effect="plain" size="small">
              {{ row.reportStatusText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报告医师" width="100">
          <template #default="{ row }">
            <span class="text-xs">{{ row.writeBy || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="申请科室" width="120">
          <template #default="{ row }">
            <span class="text-xs">{{ row.applyDeptName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="260">
          <template #default="{ row }">
            <el-button v-if="row.recordStatus === 1" v-perm="'medtech:ecg:collect'" link size="small" type="warning"
                       @click="openCollect(row)">
              <el-icon class="mr-0.5">
                <VideoCamera/>
              </el-icon>
              签到
            </el-button>
            <el-button v-else-if="row.recordStatus === 2 || row.recordStatus === 3" v-perm="'medtech:ecg:collect'"
                       link size="small" type="warning" @click="openCollect(row)">
              <el-icon class="mr-0.5">
                <VideoCamera/>
              </el-icon>
              采集
            </el-button>
            <el-button v-if="row.recordStatus >= 4" v-perm="'medtech:ecg:write'" link size="small" type="primary"
                       @click="openWrite(row)">
              <el-icon class="mr-0.5">
                <EditPen/>
              </el-icon>
              {{ row.reportId ? '查看/修改' : '写报告' }}
            </el-button>
            <el-button v-if="row.reportStatus === 1" v-perm="'medtech:ecg:audit'" link size="small" type="warning"
                       @click="openAudit(row)">
              <el-icon class="mr-0.5">
                <Check/>
              </el-icon>
              审核
            </el-button>
            <el-button v-if="row.reportStatus === 3" v-perm="'medtech:ecg:publish'" link size="small" type="success"
                       @click="doPublish(row)">
              <el-icon class="mr-0.5">
                <Promotion/>
              </el-icon>
              发布
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="mt-3 flex justify-end border-t border-slate-100 px-4 py-3">
        <el-pagination v-model:current-page="pagination.pageNum" v-model:page-size="pagination.pageSize"
                       :page-sizes="PAGE_SIZES" :total="pagination.total"
                       layout="total, sizes, prev, pager, next, jumper"
                       @size-change="handleSizeChange" @current-change="handleCurrentChange"/>
      </div>
    </div>

    <!-- 签到 / 波形采集 -->
    <EcgWorkstationCollect v-model="showCollect" :row="collectRow" @saved="handleWorkstationSaved"/>

    <!-- 报告书写 -->
    <EcgWorkstationWrite v-model="showWrite" :row="writeRow" @saved="handleWorkstationSaved"/>

    <!-- 审核 -->
    <el-dialog v-model="showAudit" destroy-on-close title="心电报告审核" top="4vh" width="1100px">
      <template v-if="auditDetail">
        <div class="grid grid-cols-5 gap-4">
          <div class="col-span-2 space-y-3">
            <EcgWavePanel :title="auditDetail.ecgTypeText || '12 导联心电图'" :wave-data="auditDetail.waveData"/>
            <div v-if="auditDetail.hr || auditDetail.measureId" class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
              <span class="text-slate-400">测量：</span>
              心率 {{ auditDetail.hr ?? '—' }}bpm · PR {{ auditDetail.prMs ?? '—' }}ms ·
              QRS {{ auditDetail.qrsMs ?? '—' }}ms · QTc {{ auditDetail.qtcMs ?? '—' }}ms
              <span v-if="auditDetail.rhythmText"> · {{ auditDetail.rhythmText }}</span>
            </div>
            <div v-if="auditDetail.holterId" class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
              <span class="text-slate-400">Holter：</span>
              总心搏 {{ auditDetail.totalBeats ?? '—' }} · 平均 {{ auditDetail.avgHr ?? '—' }}bpm ·
              最快 {{ auditDetail.maxHr ?? '—' }} · 最慢 {{ auditDetail.minHr ?? '—' }} ·
              室早 {{ auditDetail.pvcCount ?? 0 }} · 室上早 {{ auditDetail.svcCount ?? 0 }} ·
              房颤 {{ auditDetail.afibFlag === 1 ? '检出' : '未检出' }} ·
              最长停搏 {{ auditDetail.longestPauseMs ?? 0 }}ms
            </div>
          </div>
          <div class="col-span-3 space-y-3">
            <div class="rounded-lg bg-slate-50 p-3 text-xs leading-6">
              <span class="text-slate-400">患者：</span>{{ auditDetail.patientName }}
              <span class="mx-2 text-slate-300">|</span>
              <span class="text-slate-400">项目：</span>{{ auditDetail.itemName }}
              <span class="mx-2 text-slate-300">|</span>
              <span class="text-slate-400">报告医师：</span>{{ auditDetail.writeBy || '—' }}
            </div>
            <div class="rounded-lg border border-slate-200 p-3">
              <p class="mb-1 text-sm font-medium text-slate-700">心电图所见</p>
              <p class="whitespace-pre-wrap text-sm text-slate-600">{{ auditDetail.reportContent || '—' }}</p>
            </div>
            <div class="rounded-lg border border-slate-200 p-3">
              <p class="mb-1 text-sm font-medium text-slate-700">心电图诊断</p>
              <p class="whitespace-pre-wrap text-sm text-slate-600">{{ auditDetail.conclusion || '—' }}</p>
            </div>
            <div v-if="auditDetail.suggestions" class="rounded-lg border border-slate-200 p-3">
              <p class="mb-1 text-sm font-medium text-slate-700">建议</p>
              <p class="whitespace-pre-wrap text-sm text-slate-600">{{ auditDetail.suggestions }}</p>
            </div>
            <div>
              <label class="mb-1 block text-sm font-medium text-slate-700">审核意见</label>
              <el-input v-model="auditOpinion" :rows="3" placeholder="审核意见（选填）；点「退回」时这里是必填的退回原因"
                        type="textarea"/>
            </div>
          </div>
        </div>
      </template>
      <template #footer>
        <el-button @click="showAudit = false">取消</el-button>
        <el-button v-perm="'medtech:ecg:audit'" :loading="submitting" type="danger" @click="doReject">
          <el-icon class="mr-0.5">
            <Close/>
          </el-icon>
          退回重写
        </el-button>
        <el-button v-perm="'medtech:ecg:audit'" :loading="submitting" type="primary" @click="doAudit">
          <el-icon class="mr-0.5">
            <Check/>
          </el-icon>
          审核通过
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 心电工作站（菜单 417，sql/173）
 *
 * 超声/内镜/放射/病理都有独立工作站，本页补齐心电：
 *   签到 → 波形采集（12 导联，技师岗）→ 测量参数 / Holter 分析 → 报告书写（医师岗）→ 审核 → 发布。
 * 波形是报告的前置闸门（没有波形提交不了报告），Holter 还要再多一道分析闸门，
 * 这些闸门都在服务端 EcgServiceImpl 里，前端只负责把流程走通、把波形画出来。
 */
import {computed, onMounted, ref} from 'vue';
import {Check, Close, EditPen, Promotion, Search, VideoCamera} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  ecgAudit,
  ecgPublish,
  ecgReject,
  getEcgDetailByRecordId,
  getEcgListPage
} from '@/api/medicaltech';
import {patientGenderText} from '@/lib/patientGender';
import {hasPerm} from '@/lib/perm';
import EcgWavePanel from '@/components/his/EcgWavePanel.vue';
import EcgWorkstationCollect from './EcgWorkstationCollect.vue';
import EcgWorkstationWrite from './EcgWorkstationWrite.vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
// 分栏：collectPending=记录状态 1~3（签到/采集是本站职责）；onlyUnwritten=已出结果但还没报告
const TABS = [
  {key: 'collect', label: '待采集', collectPending: true, reportStatus: null, onlyUnwritten: null},
  {key: 'unwritten', label: '待书写', collectPending: null, reportStatus: null, onlyUnwritten: true},
  {key: 'draft', label: '草稿', collectPending: null, reportStatus: 0, onlyUnwritten: null},
  {key: 'pending', label: '待审核', collectPending: null, reportStatus: 1, onlyUnwritten: null},
  {key: 'reviewed', label: '已审核', collectPending: null, reportStatus: 3, onlyUnwritten: null},
  {key: 'published', label: '已发布', collectPending: null, reportStatus: 4, onlyUnwritten: null},
  {key: 'all', label: '全部', collectPending: null, reportStatus: null, onlyUnwritten: null},
];
const activeTab = ref('collect');
const keyword = ref('');
const dateRange = ref([]);
const loading = ref(false);
const rows = ref([]);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
/** 各栏真实条数（探针请求拿 total，不用前端数出来的假数字） */
const tabCounts = ref({});
const canCollect = computed(() => hasPerm('medtech:ecg:collect'));
const canWrite = computed(() => hasPerm('medtech:ecg:write'));
const canAudit = computed(() => hasPerm('medtech:ecg:audit'));
const canPublish = computed(() => hasPerm('medtech:ecg:publish'));
// ========== 列表 ==========
const currentTab = computed(() => TABS.find(t => t.key === activeTab.value) || TABS[6]);
const buildQuery = (extra = {}) => ({
  pageNum: pagination.value.pageNum,
  pageSize: pagination.value.pageSize,
  keyword: keyword.value || undefined,
  collectPending: currentTab.value.collectPending ?? undefined,
  reportStatus: currentTab.value.reportStatus ?? undefined,
  onlyUnwritten: currentTab.value.onlyUnwritten ?? undefined,
  startDate: dateRange.value?.[0] || undefined,
  endDate: dateRange.value?.[1] || undefined,
  ...extra,
});
const loadData = async () => {
  loading.value = true;
  try {
    const res = await getEcgListPage(buildQuery());
    rows.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (e) {
    ElMessage.error(e?.message || '加载失败');
  } finally {
    loading.value = false;
  }
};
/** 各栏条数：只要 total，pageSize 传 1（不受用户翻页控制） */
const loadTabCounts = async () => {
  const jobs = TABS.filter(t => t.key !== 'all').map(async (t) => {
    try {
      const res = await getEcgListPage({
        pageNum: 1, pageSize: 1,
        collectPending: t.collectPending ?? undefined,
        reportStatus: t.reportStatus ?? undefined,
        onlyUnwritten: t.onlyUnwritten ?? undefined,
        keyword: keyword.value || undefined,
        startDate: dateRange.value?.[0] || undefined,
        endDate: dateRange.value?.[1] || undefined,
      });
      tabCounts.value[t.key] = res.data?.total || 0;
    } catch {
      tabCounts.value[t.key] = 0;
    }
  });
  await Promise.all(jobs);
};
const refresh = async () => {
  pagination.value.pageNum = 1;
  await Promise.all([loadData(), loadTabCounts()]);
};
const handleTabChange = async () => {
  pagination.value.pageNum = 1;
  await loadData();
};
const handleSizeChange = (v) => {
  pagination.value.pageSize = v;
  pagination.value.pageNum = 1;
  loadData();
};
const handleCurrentChange = (v) => {
  pagination.value.pageNum = v;
  loadData();
};
// ========== 签到 / 波形采集（技师岗） ==========
const showCollect = ref(false);
const collectRow = ref(null);
const openCollect = (row) => {
  collectRow.value = row;
  showCollect.value = true;
};
// ========== 报告书写（医师岗） ==========
const showWrite = ref(false);
const writeRow = ref(null);
const submitting = ref(false);
const openWrite = (row) => {
  writeRow.value = row;
  showWrite.value = true;
};
const handleWorkstationSaved = async () => {
  await Promise.all([loadData(), loadTabCounts()]);
};
// ========== 审核 / 退回 ==========
const showAudit = ref(false);
const auditRow = ref(null);
const auditOpinion = ref('');
const auditDetail = ref(null);
const openAudit = async (row) => {
  if (!row.reportId)
    return ElMessage.warning('该检查还没有报告');
  auditRow.value = row;
  auditOpinion.value = '';
  try {
    const res = await getEcgDetailByRecordId(row.recordId);
    auditDetail.value = res.data;
  } catch (e) {
    ElMessage.error(e?.message || '加载详情失败');
  }
  showAudit.value = true;
};
const doAudit = async () => {
  submitting.value = true;
  try {
    await ecgAudit({reportId: auditRow.value?.reportId, reason: auditOpinion.value});
    ElMessage.success('审核通过（审核医师签名已完成）');
    showAudit.value = false;
    await Promise.all([loadData(), loadTabCounts()]);
  } catch (e) {
    // 服务端会拒绝「自己审自己」与「跳过审核」，这两条必须原样弹出来，不能吞掉
    ElMessage.error(e?.message || '审核失败');
  } finally {
    submitting.value = false;
  }
};
const doReject = async () => {
  if (!auditOpinion.value?.trim())
    return ElMessage.warning('退回必须写明原因');
  submitting.value = true;
  try {
    await ecgReject({reportId: auditRow.value?.reportId, reason: auditOpinion.value});
    ElMessage.success('已退回，报告回到草稿');
    showAudit.value = false;
    await Promise.all([loadData(), loadTabCounts()]);
  } catch (e) {
    ElMessage.error(e?.message || '退回失败');
  } finally {
    submitting.value = false;
  }
};
const doPublish = async (row) => {
  if (!row.reportId)
    return;
  try {
    await ElMessageBox.confirm('发布后临床医生与患者端可见该报告，确认发布？', '发布报告', {
      confirmButtonText: '发布', cancelButtonText: '取消', type: 'warning',
    });
  } catch {
    return;
  }
  try {
    await ecgPublish(row.reportId);
    ElMessage.success('报告已发布');
    await Promise.all([loadData(), loadTabCounts()]);
  } catch (e) {
    ElMessage.error(e?.message || '发布失败');
  }
};
const statusTagType = (st) => {
  if (st == null)
    return 'info';
  if (st === 4)
    return 'success';
  if (st === 3)
    return 'success';
  if (st === 1)
    return 'warning';
  if (st === 5)
    return 'danger';
  return 'info';
};
const recordTagType = (st) => {
  if (st == null)
    return 'info';
  if (st >= 4)
    return 'success';
  if (st >= 2)
    return 'warning';
  return 'info';
};
onMounted(refresh);
</script>
