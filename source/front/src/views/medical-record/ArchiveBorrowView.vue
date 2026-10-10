<template>
  <div>
    <!-- 统计卡 -->
    <div class="mb-3 grid grid-cols-4 gap-4">
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">待审核</div>
        <div class="text-2xl font-semibold text-[#B45309] mt-1">{{ stats.pending }} <span
            class="text-sm font-normal text-gray-400">单</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已借出未还</div>
        <div class="text-2xl font-semibold text-[#1269B5] mt-1">{{ stats.lentOut }} <span
            class="text-sm font-normal text-gray-400">单</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">超期未还</div>
        <div class="text-2xl font-semibold text-[#B91C1C] mt-1">{{ stats.overdue }} <span
            class="text-sm font-normal text-gray-400">单</span></div>
      </div>
      <div class="bg-white rounded-lg border border-gray-100 p-4 shadow-sm">
        <div class="text-sm text-gray-500">已归还（累计）</div>
        <div class="text-2xl font-semibold text-[#0E9488] mt-1">{{ stats.returned }} <span
            class="text-sm font-normal text-gray-400">单</span></div>
      </div>
    </div>

    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="单号">
            <el-input
                v-model="query.borrowNo"
                clearable
                data-testid="ab-no-filter"
                placeholder="单号"
                style="width: 180px"
                @keyup.enter="query.pageNum = 1; loadList()"
            />
          </el-form-item>
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                clearable
                placeholder="病历号/患者姓名/用途"
                style="width: 200px"
                @keyup.enter="query.pageNum = 1; loadList()"
            />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="query.borrowType" :fit-input-width="false" clearable placeholder="类型"
                       style="width: 120px">
              <el-option v-for="d in typeDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" :fit-input-width="false" clearable data-testid="ab-status-filter" placeholder="状态"
                       style="width: 130px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" data-testid="ab-search-btn" type="primary" @click="query.pageNum = 1; loadList()">
              查询
            </el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button :icon="Bell" data-testid="ab-notify-btn" @click="runOverdueNotify">补跑超期提醒</el-button>
          <el-button v-perm="'emr:archiveBorrow:add'" :icon="Plus" data-testid="ab-apply-btn" type="primary"
                     @click="openApply">借阅/复印申请
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="ab-table" stripe>
        <el-table-column label="单号" prop="borrowNo" width="170"/>
        <el-table-column align="center" label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.borrowType)">{{ typeText(row.borrowType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="病历号" prop="recordNo" show-overflow-tooltip width="170"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="120"/>
        <el-table-column label="用途" min-width="160" prop="purpose" show-overflow-tooltip/>
        <el-table-column align="center" label="应还日期" width="110">
          <template #default="{ row }">{{ row.expectReturnDate || '—' }}</template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
            <el-tag v-if="isOverdue(row)" class="ml-1" size="small" type="danger">超期</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请人" prop="applicantName" width="90"/>
        <el-table-column label="申请时间" width="165">
          <template #default="{ row }">{{ (row.createTime || '').replace('T', ' ').slice(0, 19) }}</template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button v-if="Number(row.status) === 1" v-perm="'emr:archiveBorrow:edit'" link type="warning"
                       @click="openAudit(row)">审核
            </el-button>
            <el-button v-if="Number(row.status) === 2" v-perm="'emr:archiveBorrow:edit'" link type="success"
                       @click="giveBack(row)">归还
            </el-button>
            <el-button v-if="canDelete(row)" v-perm="'emr:archiveBorrow:delete'" link type="danger"
                       @click="remove(row)">删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty :description="loading ? '加载中…' : '暂无借阅/复印记录'"/>
        </template>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            layout="total, prev, pager, next, jumper"
            @current-change="loadList"
        />
      </div>
    </el-card>

    <!-- 申请弹窗 -->
    <el-dialog v-model="applyVisible" data-testid="ab-apply-dialog" title="借阅/复印申请" width="560px">
      <el-form label-width="100px">
        <el-form-item label="选择病案" required>
          <el-select
              v-model="form.archiveId"
              :fit-input-width="false"
              :loading="archiveSearching"
              :remote-method="searchArchives"
              data-testid="ab-form-archive"
              filterable
              placeholder="输入病历号/患者姓名搜索（已归档/已封存）"
              remote
              style="width: 100%"
              @change="onArchiveChange"
          >
            <el-option
                v-for="a in archiveOptions"
                :key="a.id"
                :label="`${a.recordNo} / ${a.patientName}（${a.deptName || '—'}）`"
                :value="String(a.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="类型" required>
          <el-radio-group v-model="form.borrowType" data-testid="ab-form-type">
            <el-radio :value="1">借阅（原件，需应还日期）</el-radio>
            <el-radio :value="2">复印</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.borrowType === 1" label="应归还日期" required>
          <el-date-picker
              v-model="form.expectReturnDate"
              :disabled-date="(d: Date) => d.getTime() < Date.now() - 86400000"
              :fit-input-width="false"
              placeholder="选择应归还日期"
              style="width: 100%"
              type="date"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="用途" required>
          <el-input v-model="form.purpose" :rows="3" data-testid="ab-form-purpose" maxlength="500"
                    placeholder="病历讨论 / 医保核查 / 司法取证 / 科研等" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button v-perm="'emr:archiveBorrow:add'" :loading="saving" data-testid="ab-apply-save" type="primary"
                   @click="apply">提交申请
        </el-button>
      </template>
    </el-dialog>

    <!-- 审核弹窗 -->
    <el-dialog v-model="auditVisible" :title="`审核 - ${auditRow?.borrowNo || ''}`" data-testid="ab-audit-dialog"
               width="520px">
      <div v-if="auditRow" class="text-sm text-gray-600 mb-3">
        <div><span class="text-gray-400">病案：</span>{{ auditRow.recordNo }} / {{
            auditRow.patientName
          }}（{{ auditRow.deptName || '—' }}）
        </div>
        <div><span class="text-gray-400">用途：</span>{{ auditRow.purpose }}</div>
        <div v-if="auditRow.expectReturnDate"><span class="text-gray-400">应还日期：</span>{{
            auditRow.expectReturnDate
          }}
        </div>
      </div>
      <el-radio-group v-model="auditApprove" class="mb-3" data-testid="ab-audit-approve">
        <el-radio :value="true">通过</el-radio>
        <el-radio :value="false">拒绝</el-radio>
      </el-radio-group>
      <el-input
          v-model="auditRemark"
          :placeholder="auditApprove ? '审核意见（可空）' : '拒绝理由（必填）'"
          :rows="3"
          data-testid="ab-audit-remark"
          maxlength="500"
          type="textarea"
      />
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button v-perm="'emr:archiveBorrow:edit'" :loading="auditLoading" data-testid="ab-audit-submit"
                   type="primary" @click="submitAudit">确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="`借阅/复印单 ${detail?.borrowNo || ''}`" data-testid="ab-detail-dialog"
               width="640px">
      <template v-if="detail">
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="类型">
            <el-tag :type="typeTagType(detail.borrowType)">{{ typeText(detail.borrowType) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(detail.status)">{{ statusText(detail.status) }}</el-tag>
            <el-tag v-if="isOverdue(detail)" class="ml-1" size="small" type="danger">超期</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="病历号">{{ detail.recordNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ detail.deptName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="应还日期">{{ detail.expectReturnDate || '—' }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ detail.applicantName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{
              (detail.createTime || '').replace('T', ' ').slice(0, 19)
            }}
          </el-descriptions-item>
          <el-descriptions-item :span="2" label="用途">{{ detail.purpose }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="审核">
            <template v-if="detail.auditTime">
              {{ detail.auditByName }} · {{
                (detail.auditTime || '').replace('T', ' ').slice(0, 19)
              }}<br/>{{ detail.auditRemark || '（无意见）' }}
            </template>
            <template v-else>—</template>
          </el-descriptions-item>
          <el-descriptions-item label="借出时间">
            {{ detail.lendTime ? (detail.lendTime || '').replace('T', ' ').slice(0, 19) : '—' }}
          </el-descriptions-item>
          <el-descriptions-item label="归还时间">
            {{ detail.returnTime ? (detail.returnTime || '').replace('T', ' ').slice(0, 19) : '—' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Bell, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  applyBorrow,
  auditBorrow,
  deleteBorrow,
  getBorrowDetail,
  getBorrowList,
  getBorrowStats,
  notifyBorrowOverdue,
  returnBorrow,
} from '@/api/archiveBorrow';
import {getArchiveList} from '@/api/archive';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {isOverdue, statusTagType, typeTagType} from '@/lib/archiveBorrow';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';

const loading = ref(true);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  borrowNo: '',
  borrowType: null,
  status: null,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const stats = reactive({pending: 0, lentOut: 0, overdue: 0, returned: 0});
const typeDict = ref([]);
const statusDict = ref([]);
const typeText = (v) => dictLabelText(typeDict.value, v);
const statusText = (v) => dictLabelText(statusDict.value, v);
// 当前登录人（删除按钮显隐；真边界在服务端）
const me = reactive({employeeId: null});
try {
  const cached = localStorage.getItem('user-info') || localStorage.getItem('userInfo');
  if (cached) {
    const u = JSON.parse(cached);
    me.employeeId = String(u.employeeId ?? u.employee_id ?? '');
  }
} catch { /* 忽略缓存解析失败，按钮显隐退化为服务端校验 */
}
const canDelete = (row) => Number(row.status) === 1
    && !!me.employeeId && String(row.applicantId) === me.employeeId;
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.ARCHIVE_BORROW_TYPE},${DICT_TYPE.ARCHIVE_BORROW_STATUS}`);
    typeDict.value = res?.data?.[DICT_TYPE.ARCHIVE_BORROW_TYPE] || [];
    statusDict.value = res?.data?.[DICT_TYPE.ARCHIVE_BORROW_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const loadStats = async () => {
  try {
    const res = await getBorrowStats();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error('加载统计失败', e);
  }
};
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getBorrowList({
      borrowNo: query.borrowNo.trim() || undefined,
      borrowType: query.borrowType ?? undefined,
      status: query.status ?? undefined,
      keyword: query.keyword.trim() || undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '查询失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.borrowNo = '';
  query.borrowType = null;
  query.status = null;
  query.keyword = '';
  query.pageNum = 1;
  loadList();
};
const runOverdueNotify = async () => {
  try {
    const res = await notifyBorrowOverdue();
    if (res.code === 200)
      ElMessage.success(res.message || '已补跑超期提醒');
    else
      ElMessage.error(res.message || '补跑失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('补跑失败');
  }
};
// ------------------------------------------------------------------
// 申请弹窗
// ------------------------------------------------------------------
const applyVisible = ref(false);
const saving = ref(false);
const form = reactive({
  archiveId: null,
  archiveLabel: '',
  archiveStatus: null,
  borrowType: 1,
  purpose: '',
  expectReturnDate: '',
});
const archiveOptions = ref([]);
const archiveSearching = ref(false);
let archiveSearchTimer = null;
/** 远程搜病案（已归档/已封存；待归档借不了也复不了印） */
const searchArchives = (kw) => {
  if (archiveSearchTimer)
    clearTimeout(archiveSearchTimer);
  archiveSearchTimer = setTimeout(async () => {
    archiveSearching.value = true;
    try {
      const res = await getArchiveList({archiveStatus: null, keyword: kw || undefined, pageNum: 1, pageSize: 20});
      if (res.code === 200) {
        // 只放行可流通的：2 已归档 / 3 已封存
        archiveOptions.value = (res.data?.records || []).filter((a) => Number(a.archiveStatus) === 2 || Number(a.archiveStatus) === 3);
      }
    } catch (e) {
      console.error('搜索病案失败', e);
    } finally {
      archiveSearching.value = false;
    }
  }, 250);
};
const onArchiveChange = (id) => {
  const hit = archiveOptions.value.find(a => String(a.id) === String(id));
  form.archiveStatus = hit ? Number(hit.archiveStatus) : null;
};
const openApply = () => {
  form.archiveId = null;
  form.archiveLabel = '';
  form.archiveStatus = null;
  form.borrowType = 1;
  form.purpose = '';
  form.expectReturnDate = '';
  archiveOptions.value = [];
  applyVisible.value = true;
  searchArchives('');
};
const apply = async () => {
  if (!form.archiveId) {
    ElMessage.warning('请选择病案');
    return;
  }
  if (!form.purpose.trim()) {
    ElMessage.warning('请填写用途');
    return;
  }
  if (form.borrowType === 1 && !form.expectReturnDate) {
    ElMessage.warning('借阅必须选择应归还日期');
    return;
  }
  saving.value = true;
  try {
    const res = await applyBorrow({
      archiveId: form.archiveId,
      borrowType: form.borrowType,
      purpose: form.purpose.trim(),
      expectReturnDate: form.borrowType === 1 ? form.expectReturnDate : undefined,
    });
    if (res.code === 200) {
      ElMessage.success('申请成功，等待病案室审核');
      applyVisible.value = false;
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '申请失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('申请失败');
  } finally {
    saving.value = false;
  }
};
// ------------------------------------------------------------------
// 审核弹窗（通过 / 拒绝共用）
// ------------------------------------------------------------------
const auditVisible = ref(false);
const auditRow = ref(null);
const auditApprove = ref(true);
const auditRemark = ref('');
const auditLoading = ref(false);
const openAudit = (row) => {
  auditRow.value = row;
  auditApprove.value = true;
  auditRemark.value = '';
  auditVisible.value = true;
};
const submitAudit = async () => {
  if (!auditApprove.value && !auditRemark.value.trim()) {
    ElMessage.warning('拒绝必须填写审核意见');
    return;
  }
  auditLoading.value = true;
  try {
    const res = await auditBorrow(auditRow.value.id, auditApprove.value, auditRemark.value.trim() || undefined);
    if (res.code === 200) {
      ElMessage.success('审核完成');
      auditVisible.value = false;
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '审核失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('审核失败');
  } finally {
    auditLoading.value = false;
  }
};
const giveBack = async (row) => {
  try {
    await ElMessageBox.confirm(`确认收到归还：${row.borrowNo}（病历 ${row.recordNo}）？`, '归还确认', {type: 'info'});
  } catch {
    return;
  }
  try {
    const res = await returnBorrow(row.id);
    if (res.code === 200) {
      ElMessage.success('归还登记成功');
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '归还失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('归还失败');
  }
};
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除申请单 ${row.borrowNo}？`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deleteBorrow(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '删除失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
// ------------------------------------------------------------------
// 详情弹窗
// ------------------------------------------------------------------
const detailVisible = ref(false);
const detail = ref(null);
const openDetail = async (row) => {
  try {
    const res = await getBorrowDetail(row.id);
    if (res.code === 200) {
      detail.value = res.data;
      detailVisible.value = true;
    } else {
      ElMessage.error(res.message || '查询详情失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('查询详情失败');
  }
};
onMounted(() => {
  loadDicts();
  loadStats();
  loadList();
});
</script>
