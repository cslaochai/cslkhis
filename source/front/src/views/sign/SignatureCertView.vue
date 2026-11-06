<template>
  <div class="space-y-6">
    <!-- ---------------- 签名证书 ---------------- -->
    <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <div class="flex flex-wrap items-center gap-2 border-b border-slate-100 px-4 py-3">
        <el-select v-model="certQuery.certStatus" clearable data-testid="p5-sign-f-certstatus"
                   placeholder="证书状态"
                   style="width: 130px" @change="() => { certQuery.pageNum = 1; loadCertList() }">
          <el-option v-for="o in options.certStatuses || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
        </el-select>
        <el-input v-model="certQuery.keyword" clearable data-testid="p5-sign-f-certkeyword"
                  placeholder="证书号 / 员工姓名 / 指纹"
                  style="width: 230px"
                  @keyup.enter="() => { certQuery.pageNum = 1; loadCertList() }">
          <template #prefix>
            <el-icon>
              <Search/>
            </el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="() => { certQuery.pageNum = 1; loadCertList() }">查询</el-button>
        <el-button v-perm="'sign:cert:edit'" data-testid="p5-sign-issue-open" plain type="primary"
                   @click="openIssue">办理签名证书
        </el-button>
      </div>

      <div v-loading="certLoading" class="p-4">
        <!-- CA 通道前置自检：在线申请未接入 / 接入不可用时先亮出来，
            否则操作员只会看到「重新调用」点了还是失败，不知道卡在配置上 -->
        <el-alert v-if="caNotice" :closable="false" :title="caNotice.title" :type="caNotice.type"
                  class="mb-3" data-testid="p5-sign-ca-status-alert" show-icon>
          <template #default>
            <span class="text-xs leading-relaxed">{{ caNotice.hint }}</span>
          </template>
        </el-alert>
        <el-table :data="certList" border data-testid="p5-sign-cert-table" size="small">
          <el-table-column label="证书编号" width="180">
            <template #default="{ row }">
              <span class="text-xs font-medium text-slate-700">{{ row.certNo }}</span>
            </template>
          </el-table-column>
          <el-table-column label="持有人" width="140">
            <template #default="{ row }">
              <div class="text-xs">{{ row.empName || '—' }}</div>
              <div class="text-xs text-slate-400">{{ row.deptName || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="算法" width="200">
            <template #default="{ row }">
              <span class="text-xs text-slate-600">{{ row.keyAlgo }} / {{ row.digestAlgo }} / {{
                  row.signAlgo
                }}</span>
            </template>
          </el-table-column>
          <el-table-column label="公钥指纹" width="200">
            <template #default="{ row }">
              <span :data-testid="`p5-sign-cert-fp-${row.certNo}`"
                    class="font-mono text-xs text-slate-600">{{ row.keyFingerprintGroups }}</span>
            </template>
          </el-table-column>
          <el-table-column label="CA 签发" width="180">
            <template #default="{ row }">
              <el-tooltip :content="row.caLastMessage || ''" :disabled="!row.caLastMessage" placement="top">
                <span class="inline-block">
                  <el-tag v-if="row.caApplyStatus" :type="caApplyTagType(row.caApplyStatus)" size="small">
                    {{ row.caApplyStatusText || row.caApplyStatus }}
                  </el-tag>
                  <span v-else class="text-xs text-slate-400">待线下送 CA</span>
                </span>
              </el-tooltip>
              <!-- 提交失败 / 被驳回时把结论露在列表里：只给红标签，操作员不知道要不要处理 -->
              <div v-if="isCaBroken(row)" class="mt-0.5 text-xs text-red-600">
                {{ row.caApplyStatus === 'FAILED' ? 'CA 调用失败' : 'CA 未受理' }}
              </div>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.certStatus === 1 ? 'success' : (row.certStatus === 0 ? 'warning' : 'info')"
                      size="small">{{
                  row.certStatusText
                }}
              </el-tag>
              <div v-if="row.expired" :data-testid="`p5-sign-cert-expired-${row.certNo}`"
                   class="mt-0.5 text-xs text-red-600">已过期
              </div>
            </template>
          </el-table-column>
          <el-table-column label="有效期" width="200">
            <template #default="{ row }">
              <template v-if="row.validityEstimated">
                <div class="text-xs text-slate-400">待 CA 签发</div>
                <div class="text-xs text-slate-300">有效期以签发为准</div>
              </template>
              <template v-else>
                <div class="text-xs">{{ row.validFrom || '—' }}</div>
                <div class="text-xs text-slate-400">至 {{ row.validTo || '—' }}</div>
              </template>
            </template>
          </el-table-column>
          <el-table-column label="使用情况" width="120">
            <template #default="{ row }">
              <div class="text-xs">签名 {{ num(row.signCount) }} 次</div>
              <div class="text-xs text-slate-400">{{ row.lastUsedTime || '未使用' }}</div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="330">
            <template #default="{ row }">
              <el-button v-perm="'sign:cert:edit'" link size="small" type="primary"
                         @click="openCertDetail(row)">详情
              </el-button>
              <el-button v-perm="'sign:cert:edit'" link size="small" type="primary"
                         data-testid="p5-sign-progress-open" @click="openProgress(row)">进度
              </el-button>
              <el-button v-if="row.certStatus === 0" v-perm="'sign:cert:edit'" data-testid="p5-sign-bind-open"
                         link size="small" type="primary" @click="openBind(row)">回填
              </el-button>
              <el-tooltip :content="caOfflineTip" :disabled="!caOffline" placement="top">
                  <el-button
                      v-perm="'sign:cert:edit'"
                      :data-testid="`p5-sign-cert-retry-${row.certNo}`"
                      :disabled="caOffline"
                      :loading="retryingId === String(row.id)"
                      link
                      size="small"
                      type="danger"
                      @click="retryCa(row)"
                  >{{ retryLabel(row) }}
                  </el-button>
              </el-tooltip>
              <el-button v-if="row.canRevoke && !row.expired" v-perm="'sign:cert:revoke'"
                         :data-testid="`p5-sign-cert-revoke-${row.certNo}`"
                         link size="small" type="danger" @click="openRevoke(row)">吊销
              </el-button>
              <el-button v-if="canReissue(row)" v-perm="'sign:cert:edit'"
                         :data-testid="`p5-sign-cert-reissue-${row.certNo}`"
                         link size="small" type="primary" @click="openReissue(row)">重新签发
              </el-button>
              <el-button v-perm="'sign:cert:edit'" link size="small" type="primary"
                         data-testid="p5-sign-timeline-open" @click="openEmpTimeline(row)">履历
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">暂无签名证书</div>
          </template>
        </el-table>

        <div class="mt-3 flex items-center justify-between">
          <p class="text-xs text-slate-500" data-testid="p5-sign-cert-total">共 {{ certTotal }} 张证书</p>
          <el-pagination
              v-model:current-page="certQuery.pageNum"
              v-model:page-size="certQuery.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="certTotal"
              layout="sizes, prev, pager, next"
              @current-change="loadCertList"
              @size-change="() => { certQuery.pageNum = 1; loadCertList() }"
          />
        </div>
      </div>
    </div>

    <!-- ---------------- 证书详情（子组件 SignatureCenterCertDetail） ---------------- -->
    <SignatureCenterCertDetail :id="certDetailId" v-model="certDetailVisible"/>

    <!-- ---------------- 办理证书（子组件 SignatureCenterCertIssue） ---------------- -->
    <SignatureCenterCertIssue v-model="issueVisible" :bind-cert-id="issueBindId" :preset-emp="issuePresetEmp"
                              @saved="onIssueSaved"/>

    <!-- ---------------- 申请进度（流程图，子组件 SignatureCertApplyFlow） ---------------- -->
    <SignatureCertApplyFlow :id="progressId" v-model="progressVisible" @saved="onIssueSaved"/>

    <!-- ---------------- 员工证书操作履历（CA申请+吊销时间线，子组件 SignatureCertEmpTimeline） ---------------- -->
    <SignatureCertEmpTimeline v-model="timelineVisible" :emp-id="timelineEmpId" :emp-name="timelineEmpName"/>

    <!-- ---------------- 吊销证书 ---------------- -->
    <el-dialog v-model="revokeVisible" title="吊销签名证书" width="480px">
      <p class="text-sm">证书：<b>{{ revokeForm.certNo }}</b></p>
      <el-input v-model="revokeForm.reason" class="mt-3" data-testid="p5-sign-revoke-reason"
                placeholder="吊销理由（必填）"/>
      <template #footer>
        <el-button :loading="revoking" data-testid="p5-sign-revoke-submit" type="danger" @click="submitRevoke">
          确认吊销
        </el-button>
        <el-button @click="revokeVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue';
import {Search} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {
  getSignCaStatus,
  getSignCertList,
  getSignatureQueryOptions,
  revokeSignCert,
  submitSignCaApply,
} from '@/api/signature';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {caApplyTagType, num} from '@/lib/sign';
import SignatureCenterCertDetail from './SignatureCenterCertDetail.vue';
import SignatureCenterCertIssue from './SignatureCenterCertIssue.vue';
import SignatureCertApplyFlow from './SignatureCertApplyFlow.vue';
import SignatureCertEmpTimeline from './SignatureCertEmpTimeline.vue';
// ---------------- 下拉选项 ----------------
const options = ref({});
// ---------------- 证书 ----------------
const certLoading = ref(false);
const certList = ref([]);
const certTotal = ref(0);
const certQuery = reactive({
  certStatus: undefined,
  issuedMode: undefined,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 证书详情（子组件 SignatureCenterCertDetail）
const certDetailVisible = ref(false);
const certDetailId = ref(null);
// 办理证书（子组件 SignatureCenterCertIssue）
const issueVisible = ref(false);
// 回填模式：传入已存在证书 id（null = 新建密钥对流程）
const issueBindId = ref(null);
// 重新签发模式：带上原持有人，避免再次搜索选错人
const issuePresetEmp = ref(null);
// 申请进度（子组件 SignatureCertApplyFlow，流程图）
const progressVisible = ref(false);
const progressId = ref(null);
// 员工证书操作履历（子组件 SignatureCertEmpTimeline，CA申请+吊销时间线）
const timelineVisible = ref(false);
const timelineEmpId = ref(null);
const timelineEmpName = ref('');
// 吊销证书
const revokeVisible = ref(false);
const revoking = ref(false);
const revokeForm = reactive({certId: '', certNo: '', reason: ''});
// ---------------- CA 通道自检 / 重提 ----------------
// offline=未接入在线申请；online 且 available=false=已启用但凭证或连通性不达标
const caStatus = ref(null);
const caOffline = computed(() => caStatus.value?.caMode === 'offline');
const caOfflineTip = '未接入 CA 在线申请：这张证只能走「回填」上传 CA 签发的证书文件，重试不会送单';
const caNotice = computed(() => {
  const s = caStatus.value;
  if (!s) return null;
  if (s.caMode === 'offline') {
    return {
      type: 'warning',
      title: '未接入 CA 在线申请',
      hint: s.hint || '证书需线下送 CA 签发，再回本页用「回填」上传 CA 导出的证书文件生效。'
    };
  }
  if (s.available === false) {
    return {
      type: 'warning',
      title: 'CA 在线申请已启用，但当前接入不可用',
      hint: s.hint || '请核对 CA 地址与接入密钥；恢复前可先用「回填」走线下签发。'
    };
  }
  return null;
});
// 正在重提 CA 申请的证书 id：逐行 loading，不整表转圈
const retryingId = ref(null);
// 断了就是断了：提交失败 / 被驳回 / 被撤销的在线申请不会再自行推进，只能重提或转线下
const isCaBroken = (row) => ['FAILED', 'REJECTED', 'CANCELLED'].includes(row.caApplyStatus);
const canRetryCa = (row) => row.certStatus === 0 && isCaBroken(row);
// 提交失败叫「重新调用」，CA 驳回/撤销叫「重新申请」：同一个动作，但操作员要知道卡在哪一方
const retryLabel = (row) => (row.caApplyStatus === 'FAILED' ? '重新调用' : '重新申请');
// 已吊销 / 已过期：原证不能再用于新签名，补办一张；有效期内的证书必须先吊销，交后端拦
const canReissue = (row) => row.certStatus === 2 || (row.certStatus === 1 && row.expired === true);
// ---------------- 加载 ----------------
const loadOptions = async () => {
  try {
    const res = await getSignatureQueryOptions();
    options.value = res.data || {};
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const loadCaStatus = async () => {
  try {
    const res = await getSignCaStatus();
    caStatus.value = res.data || null;
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const loadCertList = async () => {
  certLoading.value = true;
  try {
    const params = {pageNum: certQuery.pageNum, pageSize: certQuery.pageSize};
    if (certQuery.certStatus !== undefined && certQuery.certStatus !== null)
      params.certStatus = certQuery.certStatus;
    if (certQuery.issuedMode !== undefined && certQuery.issuedMode !== null)
      params.issuedMode = certQuery.issuedMode;
    if (certQuery.keyword)
      params.keyword = certQuery.keyword.trim();
    const res = await getSignCertList(params);
    certList.value = res.data?.records || [];
    certTotal.value = Number(res.data?.total ?? 0);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  } finally {
    certLoading.value = false;
  }
};
// ---------------- 证书操作 ----------------
const openCertDetail = (row) => {
  certDetailId.value = row.id;
  certDetailVisible.value = true;
};
const openIssue = () => {
  issueBindId.value = null;
  issuePresetEmp.value = null;
  issueVisible.value = true;
};
// 回填已有证书（线下送 CA 签发后，把 CA 返回的证书 PEM 回填到这张「待 CA 签发」的证书上）
const openBind = (row) => {
  issueBindId.value = row.id;
  issuePresetEmp.value = null;
  issueVisible.value = true;
};
// 重新签发（过期/已吊销后补办）：原证保留在台账里供历史签名验签，这里给同一持有人发新证
const openReissue = (row) => {
  issueBindId.value = null;
  issuePresetEmp.value = {
    id: row.empId,
    empName: row.empName,
    deptId: row.deptId,
    deptName: row.deptName,
  };
  issueVisible.value = true;
};
const openProgress = (row) => {
  progressId.value = row.id;
  progressVisible.value = true;
};
// 员工维度全局履历：该员工名下所有证书的 CA 申请与吊销事件一条时间线看全
const openEmpTimeline = (row) => {
  timelineEmpId.value = row.empId;
  timelineEmpName.value = row.empName || '';
  timelineVisible.value = true;
};
const onIssueSaved = async () => {
  await loadCertList();
};
// 重新调用 CA：后端 caSubmit 对「仍在审核中」的单子等价刷新、不重复建单；
// 驳回/撤销/提交失败的单子会换新幂等键重新送单，所以这里可以放心重复点。
const retryCa = async (row) => {
  retryingId.value = String(row.id);
  try {
    const res = await submitSignCaApply(row.id);
    const d = res.data || {};
    if (d.applyStatus === 'FAILED') {
      ElMessage.error(d.applyMessage || 'CA 申请仍提交失败，请先检查上方 CA 通道配置');
    } else if (d.applyStatus === 'PENDING' || d.applyStatus === 'APPROVED') {
      ElMessage.success(`CA 申请单 ${d.applyNo || ''} 正在${d.applyStatusText || '处理中'}，签发结果由 CA 主动推送回写`);
    } else if (d.applyStatus === 'ISSUED') {
      ElMessage.success('CA 已签发，证书已回填生效');
    } else {
      ElMessage.success('已重新提交 CA 申请');
    }
    await loadCertList();
    await loadCaStatus();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  } finally {
    retryingId.value = null;
  }
};
const openRevoke = (row) => {
  revokeForm.certId = row.id;
  revokeForm.certNo = row.certNo;
  revokeForm.reason = '';
  revokeVisible.value = true;
};
const submitRevoke = async () => {
  if (!revokeForm.reason.trim()) {
    ElMessage.warning('吊销必须写理由');
    return;
  }
  revoking.value = true;
  try {
    await revokeSignCert(revokeForm.certId, revokeForm.reason.trim());
    ElMessage.success('已吊销');
    revokeVisible.value = false;
    await loadCertList();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  } finally {
    revoking.value = false;
  }
};
onMounted(async () => {
  await loadOptions();
  await loadCaStatus();
  await loadCertList();
});
</script>
