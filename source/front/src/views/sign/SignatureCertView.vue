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
          <el-table-column label="CA 签发" width="150">
            <template #default="{ row }">
              <span v-if="row.caIssuer" class="text-xs text-slate-600">{{ row.caIssuer }}</span>
              <span v-else class="text-xs text-slate-400">待线下送 CA</span>
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
          <el-table-column label="有效期" width="190">
            <template #default="{ row }">
              <div class="text-xs">{{ row.validFrom || '—' }}</div>
              <div class="text-xs text-slate-400">至 {{ row.validTo || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="使用情况" width="120">
            <template #default="{ row }">
              <div class="text-xs">签名 {{ num(row.signCount) }} 次</div>
              <div class="text-xs text-slate-400">{{ row.lastUsedTime || '未使用' }}</div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="130">
            <template #default="{ row }">
              <el-button link size="small" type="primary" @click="openCertDetail(row)">详情</el-button>
              <el-button
                  v-if="row.canRevoke"
                  v-perm="'sign:cert:revoke'"
                  :data-testid="`p5-sign-cert-revoke-${row.certNo}`"
                  link
                  size="small"
                  type="danger"
                  @click="openRevoke(row)"
              >吊销
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
    <SignatureCenterCertDetail v-model="certDetailVisible" :id="certDetailId"/>

    <!-- ---------------- 办理证书（子组件 SignatureCenterCertIssue） ---------------- -->
    <SignatureCenterCertIssue v-model="issueVisible" @saved="onIssueSaved"/>

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
import {onMounted, reactive, ref} from 'vue';
import {Search} from '@element-plus/icons-vue';
import {ElMessage} from 'element-plus';
import {
  getSignatureQueryOptions,
  getSignCertList,
  revokeSignCert,
} from '@/api/signature';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {num} from '@/lib/sign';
import SignatureCenterCertDetail from './SignatureCenterCertDetail.vue';
import SignatureCenterCertIssue from './SignatureCenterCertIssue.vue';
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
// 吊销证书
const revokeVisible = ref(false);
const revoking = ref(false);
const revokeForm = reactive({certId: '', certNo: '', reason: ''});
// ---------------- 加载 ----------------
const loadOptions = async () => {
  try {
    const res = await getSignatureQueryOptions();
    options.value = res.data || {};
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
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
  issueVisible.value = true;
};
const onIssueSaved = async () => {
  await loadCertList();
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
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    revoking.value = false;
  }
};
onMounted(async () => {
  await loadOptions();
  await loadCertList();
});
</script>
