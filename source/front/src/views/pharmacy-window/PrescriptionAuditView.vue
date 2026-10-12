<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="{ keyword, unauditedOnly }" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input
              v-model="keyword"
              :prefix-icon="Search"
              class="!w-80"
              clearable
              placeholder="搜索处方号 / 患者姓名 / 患者号..."
              @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="unauditedOnly" @change="handleSearch">只看未审方</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <p class="mt-2 text-xs text-slate-400">
        审方签名由当前登录药师签署，签名后处方内容即锁定；要改处方须先在「电子签名与时间戳」作废该签名。
      </p>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="list" :max-height="tableMaxHeight" stripe style="width: 100%">
        <el-table-column class-name="font-mono" label="处方号" prop="prescriptionNo" width="170"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="科室" prop="deptName" width="110"/>
        <el-table-column label="开方医生" prop="doctorName" width="100"/>
        <el-table-column align="center" label="药品数" width="80">
          <template #default="{ row }">
            <span class="font-medium">{{ row.drugCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="金额" width="100">
          <template #default="{ row }">
            <span class="font-medium text-red-600">¥{{ row.totalAmount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="处方状态" width="110">
          <template #default="{ row }">
            <el-tooltip
                v-if="row.prescriptionStatus === 7 && row.returnReason"
                :content="`退回原因：${row.returnReason}`"
                placement="top"
            >
              <span :class="['inline-block rounded px-2 py-0.5 font-medium', statusColor(row.prescriptionStatus)]"
                    data-testid="l7-rx-status">
                {{ statusText(row.prescriptionStatus) }}<template v-if="row.returnCount > 1">·{{
                  row.returnCount
                }}次</template>
              </span>
            </el-tooltip>
            <span v-else :class="['inline-block rounded px-2 py-0.5 font-medium', statusColor(row.prescriptionStatus)]"
                  data-testid="l7-rx-status">
              {{ statusText(row.prescriptionStatus) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="开方签名" width="110">
          <template #default="{ row }">
            <span v-if="row.doctorSignId" class="inline-flex items-center gap-1 text-emerald-600">
              <Stamp class="h-3.5 w-3.5"/>已签
            </span>
            <span v-else class="text-slate-400">未签</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="审方签名" width="110">
          <template #default="{ row }">
            <span v-if="row.auditSignId" class="inline-flex items-center gap-1 text-emerald-600">
              <Stamp class="h-3.5 w-3.5"/>已签
            </span>
            <span v-else class="text-amber-600">未签</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="合理用药" width="140">
          <template #default="{ row }">
            <el-tooltip v-if="blockedOf(row)" :content="rationalOf(row).blockMessage || '存在禁忌配伍'" placement="top">
              <span class="inline-flex items-center gap-1 rounded bg-red-100 px-2 py-0.5 font-medium text-red-700"
                    data-testid="kb-rx-block">
                <WarningFilled class="h-3.5 w-3.5"/>禁忌·不可签发
              </span>
            </el-tooltip>
            <el-tooltip v-else-if="hitsOf(row).length" placement="top">
              <template #content>
                <p v-for="h in hitsOf(row)" :key="h.knowledgeId" class="text-xs leading-5">
                  {{ hitTypeText[h.hitType] || h.hitType }}：{{ h.message }}
                </p>
              </template>
              <span class="inline-flex items-center gap-1 rounded bg-amber-100 px-2 py-0.5 font-medium text-amber-700"
                    data-testid="kb-rx-hit">
                提示{{ hitsOf(row).length }}项
              </span>
            </el-tooltip>
            <span v-else class="text-slate-400" data-testid="kb-rx-clean">通过</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
                v-if="!row.auditSignId && row.prescriptionStatus !== 7"
                v-perm="'pharmacy:prescriptionAudit:edit'"
                :icon="Check"
                data-testid="l7-rx-audit"
                link
                type="warning"
                @click="handleAudit(row)"
            >
              审方
            </el-button>
            <el-button
                v-if="!row.auditSignId && row.prescriptionStatus !== 7"
                v-perm="'pharmacy:prescriptionAudit:edit'"
                :icon="CircleClose"
                data-testid="l7-rx-return"
                link
                type="danger"
                @click="handleReturn(row)"
            >
              退回
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="list.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无待审处方
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

    <PrescriptionAuditDetail v-model="detailVisible" :row="detailRow" :blocked-of="blockedOf"
                             :hits-of="hitsOf" :hit-type-text="hitTypeText" :rational-of="rationalOf"
                             :status-color="statusColor" :status-text="statusText"/>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {Check, CircleClose, Refresh, Search, Stamp, WarningFilled} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {auditPrescription, getPrescriptionListPage, rationalCheckPrescriptions} from '@/api/doctor';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import PrescriptionAuditDetail from './PrescriptionAuditDetail.vue';
const loading = ref(false);
const list = ref([]);
const keyword = ref('');
const unauditedOnly = ref(true);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
/** 合理用药核查结果，键为处方 id（字符串，雪花 id 不能转数字） */
const rationalMap = ref({});
const detailVisible = ref(false);
const detailRow = ref(null);
const rxStatusMap = {
  1: {label: '草稿', color: 'bg-slate-100 text-slate-600'},
  2: {label: '已提交', color: 'bg-amber-100 text-amber-700'},
  3: {label: '已审核', color: 'bg-emerald-100 text-emerald-700'},
  4: {label: '已发药', color: 'bg-blue-100 text-blue-700'},
  5: {label: '已取消', color: 'bg-red-100 text-red-500'},
  6: {label: '已退药', color: 'bg-red-100 text-red-500'},
  7: {label: '审方退回', color: 'bg-orange-100 text-orange-700'},
};
const statusText = (v) => rxStatusMap[v]?.label ?? (v == null ? '—' : `未知(${v})`);
const statusColor = (v) => rxStatusMap[v]?.color ?? 'bg-slate-100 text-slate-500';
const fmtTime = (v) => (v ? String(v).replace('T', ' ').slice(0, 19) : '—');
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    };
    if (keyword.value)
      params.keyword = keyword.value;
    if (unauditedOnly.value)
      params.unauditedOnly = true;
    const res = await getPrescriptionListPage(params);
    list.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
    await loadRational(list.value.map((r) => String(r.id)));
  } catch (error) {
    console.error('加载处方列表失败:', error);
  } finally {
    loading.value = false;
  }
};
/**
 * 批量拉取本页处方的合理用药核查结果。
 * 结论全部由后端算（知识表比对 + 剂量上限），前端只负责展示，
 * 不复制任何判断 —— 闸在 `/prescription/audit` 里，这里只是提前把话说清楚。
 */
const loadRational = async (ids) => {
  if (!ids.length) {
    rationalMap.value = {};
    return;
  }
  try {
    const res = await rationalCheckPrescriptions(ids);
    const map = {};
    for (const item of res.data || [])
      map[String(item.prescriptionId)] = item;
    rationalMap.value = map;
  } catch (error) {
    console.error('合理用药核查失败:', error);
    rationalMap.value = {};
  }
};
const rationalOf = (row) => rationalMap.value[String(row?.id)] || null;
const hitsOf = (row) => rationalOf(row)?.hits || [];
const blockedOf = (row) => rationalOf(row)?.blocked === true;
const hitTypeText = {
  INTERACTION: '配伍',
  DOSE_SINGLE: '单次剂量',
  DOSE_DAILY: '日剂量',
};
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  keyword.value = '';
  unauditedOnly.value = true;
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
const openDetail = (row) => {
  detailRow.value = row;
  detailVisible.value = true;
};
/** 审方通过：确认后由后端签署药师签名并把处方置为「已审核」 */
const handleAudit = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`确认为处方 ${row.prescriptionNo} 签署审方意见？签名后内容即锁定，改处方需先作废该签名。`, '处方审核（通过）', {
      confirmButtonText: '确认审方并签名',
      cancelButtonText: '取消',
      inputPlaceholder: '审方意见（可留空）',
      inputType: 'textarea',
      type: 'warning',
    });
    const res = await auditPrescription({prescriptionId: row.id, auditResult: 1, auditOpinion: value || ''});
    ElMessage.success(`审方完成，已签署药师签名（${res.data?.auditBy || '当前药师'}）`);
    loadData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '审方失败');
    }
  }
};
/** 审方退回（L7）：原因必填；不产生药师签名，处方置「审方退回」等医生改方重提 */
const handleReturn = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`退回处方 ${row.prescriptionNo}（${row.patientName}，医生 ${row.doctorName}）。退回后医生将收到站内信，改方重新提交后才能再次审方。`, '审方退回', {
      confirmButtonText: '确认退回',
      cancelButtonText: '取消',
      inputPlaceholder: '退回原因（必填，如：用法用量与诊断不符）',
      inputType: 'textarea',
      inputValidator: (v) => (v && v.trim() ? true : '退回原因不能为空'),
      type: 'warning',
    });
    await auditPrescription({prescriptionId: row.id, auditResult: 2, auditOpinion: value.trim()});
    ElMessage.success('已退回，医生将收到站内信通知');
    loadData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '退回失败');
    }
  }
};
onMounted(() => {
  loadData();
});
</script>
