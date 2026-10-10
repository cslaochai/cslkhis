<template>
  <div class="drug-trace-view" data-testid="drug-trace-page">
    <el-row :gutter="12" class="stat-row">
      <el-col v-for="card in statCards" :key="card.key" :span="6">
        <div :data-testid="`stat-${card.key}`" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ card.value }}</div>
        </div>
      </el-col>
    </el-row>

    <el-tabs v-model="activeTab">
      <!-- ========== ① 台账 ========== -->
      <el-tab-pane label="追溯码台账" name="ledger">
        <el-form :inline="true" class="query-bar">
          <el-form-item label="关键字">
            <el-input
                v-model="query.keyword"
                clearable
                data-testid="query-keyword"
                placeholder="追溯码/药品/批号/患者"
                style="width: 220px"
            />
          </el-form-item>
          <el-form-item label="码状态">
            <el-select v-model="query.status" clearable data-testid="query-status" placeholder="全部"
                       style="width: 140px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="上传状态">
            <el-select v-model="query.uploadStatus" clearable data-testid="query-upload" placeholder="全部"
                       style="width: 140px">
              <el-option v-for="d in uploadDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="码制">
            <el-select v-model="query.codeType" clearable placeholder="全部" style="width: 180px">
              <el-option v-for="d in codeTypeDict" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="来源">
            <el-select v-model="query.sourceType" clearable placeholder="全部" style="width: 140px">
              <el-option v-for="d in sourceDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button data-testid="btn-query" type="primary" @click="loadList">查询</el-button>
            <el-button data-testid="btn-reset" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table
            v-loading="loading"
            :data="rows"
            border
            data-testid="trace-table"
            stripe
            @row-click="openDetail"
            @selection-change="onSelectionChange"
        >
          <el-table-column type="selection" width="46"/>
          <el-table-column label="追溯码" min-width="200" prop="traceCode" show-overflow-tooltip/>
          <el-table-column label="药品" min-width="150" prop="drugName" show-overflow-tooltip/>
          <el-table-column label="规格" prop="specification" show-overflow-tooltip width="120"/>
          <el-table-column label="批号" prop="stockBatchNo" width="120"/>
          <el-table-column label="码制" width="140">
            <template #default="{ row }">{{ codeTypeText(row.codeType) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="traceStatusTagType(row.status)" data-testid="cell-status">{{
                  statusText(row.status)
                }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="上传" width="110">
            <template #default="{ row }">
              <el-tag :type="uploadStatusTagType(row.uploadStatus)">{{ uploadText(row.uploadStatus) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="患者" prop="patientName" width="100"/>
          <el-table-column label="采集时间" prop="scanTime" width="160"/>
          <el-table-column fixed="right" label="操作" width="150">
            <template #default="{ row }">
              <el-button data-testid="btn-detail" link type="primary" @click.stop="openDetail(row)">详情</el-button>
              <el-button
                  v-if="canVoidTrace(row)"
                  v-perm="'pharmacy:drugTrace:edit'"
                  data-testid="btn-void"
                  link
                  type="warning"
                  @click.stop="openVoid(row)"
              >作废
              </el-button>
              <el-button
                  v-if="canDeleteTrace(row)"
                  v-perm="'pharmacy:drugTrace:delete'"
                  data-testid="btn-delete"
                  link
                  type="danger"
                  @click.stop="doDelete(row)"
              >删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            class="pager"
            layout="total, sizes, prev, pager, next, jumper"
            @current-change="loadList"
            @size-change="loadList"
        />
      </el-tab-pane>

      <!-- ========== ② 采集与核对 ========== -->
      <el-tab-pane label="采集与核对" name="collect">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-card shadow="never">
              <template #header><b>入库采集 / 存量补采</b></template>
              <el-form label-width="96px">
                <el-form-item label="追溯码">
                  <el-input
                      v-model="collectForm.traceCode"
                      clearable
                      data-testid="input-collect-code"
                      placeholder="扫码枪输入或手工录入，回车解析"
                      @keyup.enter="doScanCollect"
                  />
                </el-form-item>
                <el-form-item label="采集来源">
                  <el-radio-group v-model="collectForm.sourceType" data-testid="radio-source-type">
                    <el-radio :value="1">入库采集</el-radio>
                    <el-radio :value="2">存量补采</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="指定药品">
                  <el-select
                      v-model="collectForm.drugId"
                      clearable
                      data-testid="select-collect-drug"
                      filterable
                      placeholder="码识别不出药品时人工指定"
                      style="width: 100%"
                  >
                    <el-option v-for="d in drugs" :key="d.id" :label="`${d.drugName}（${d.specification || ''}）`"
                               :value="String(d.id)"/>
                  </el-select>
                </el-form-item>
                <el-form-item label="挂靠批次">
                  <el-select v-model="collectForm.stockId" data-testid="select-collect-stock"
                             placeholder="先解析再选批次" style="width: 100%">
                    <el-option
                        v-for="b in (collectScan?.batches || [])"
                        :key="b.stockId"
                        :label="`${b.batchNo}｜效期 ${b.expiryDate || '-'}｜余 ${b.quantity ?? 0}`"
                        :value="String(b.stockId)"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="备注">
                  <el-input v-model="collectForm.remark" placeholder="选填"/>
                </el-form-item>
                <el-form-item>
                  <el-button data-testid="btn-collect-scan" @click="doScanCollect">解析校验</el-button>
                  <el-button
                      :disabled="!collectScan?.canCollect"
                      data-testid="btn-collect-submit"
                      type="primary"
                      @click="submitCollect"
                  >确认采集
                  </el-button>
                </el-form-item>
              </el-form>

              <div v-if="collectScan" class="scan-result" data-testid="collect-scan-result">
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="码制">{{ codeTypeText(collectScan.codeType) }}</el-descriptions-item>
                  <el-descriptions-item label="产品标识">{{ collectScan.drugDi || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="序列号">{{ collectScan.serialNo || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="码内批号">{{ collectScan.batchNo || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="命中药品">{{ collectScan.drugName || '未命中' }}</el-descriptions-item>
                  <el-descriptions-item label="批准文号">{{ collectScan.approvalNumber || '-' }}</el-descriptions-item>
                </el-descriptions>
                <el-alert
                    v-if="collectScan.tip"
                    :closable="false"
                    :title="collectScan.tip"
                    :type="collectScan.canCollect ? 'success' : 'warning'"
                    class="scan-tip"
                />
              </div>
            </el-card>
          </el-col>

          <el-col :span="12">
            <el-card shadow="never">
              <template #header><b>发药核销（扫码核对）</b></template>
              <el-form label-width="96px">
                <el-form-item label="发药记录">
                  <el-select
                      v-model="dispensingId"
                      :remote-method="onDispensingSearch"
                      clearable
                      data-testid="select-dispensing"
                      filterable
                      placeholder="按患者姓名搜索已发药记录"
                      remote
                      style="width: 100%"
                  >
                    <el-option
                        v-for="d in dispensingOptions"
                        :key="d.id"
                        :label="`${d.dispensingNo}｜${d.patientName}｜${d.drugName}`"
                        :value="String(d.id)"
                    />
                  </el-select>
                </el-form-item>
                <el-form-item label="追溯码">
                  <el-input
                      v-model="dispenseCode"
                      clearable
                      data-testid="input-dispense-code"
                      placeholder="发药窗口扫码，回车校验"
                      @keyup.enter="doScanDispense"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button data-testid="btn-dispense-scan" @click="doScanDispense">核对</el-button>
                  <el-button
                      :disabled="!dispenseScan?.canDispense"
                      data-testid="btn-dispense-submit"
                      type="primary"
                      @click="submitDispense"
                  >确认核销
                  </el-button>
                </el-form-item>
              </el-form>

              <div v-if="dispenseScan" class="scan-result" data-testid="dispense-scan-result">
                <el-descriptions :column="2" border size="small">
                  <el-descriptions-item label="发药单">{{ dispenseScan.dispensingNo || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="患者">{{
                      dispenseScan.dispensingPatientName || '-'
                    }}
                  </el-descriptions-item>
                  <el-descriptions-item label="发药品种">{{
                      dispenseScan.dispensingDrugName || '-'
                    }}
                  </el-descriptions-item>
                  <el-descriptions-item label="发药数量">{{
                      dispenseScan.dispensingQuantity ?? '-'
                    }}
                  </el-descriptions-item>
                  <el-descriptions-item label="追溯码归属">{{ dispenseScan.drugName || '-' }}</el-descriptions-item>
                  <el-descriptions-item label="码状态">
                    {{ dispenseScan.exists ? statusText(dispenseScan.existStatus) : '未采集' }}
                  </el-descriptions-item>
                </el-descriptions>
                <el-alert
                    v-if="dispenseScan.tip"
                    :closable="false"
                    :title="dispenseScan.tip"
                    :type="dispenseScan.canDispense ? 'success' : 'error'"
                    class="scan-tip"
                />
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- ========== ③ 上传 ========== -->
      <el-tab-pane label="上传与对账" name="upload">
        <el-card shadow="never">
          <template #header>
            <div class="upload-header">
              <b>上传医保局追溯码平台</b>
              <span class="upload-hint">追溯码以 FAIL 开头的行会被平台校验拒绝（用于验证失败重传链路）</span>
            </div>
          </template>
          <div class="upload-actions">
            <el-button
                v-perm="'pharmacy:drugTrace:export'"
                :loading="uploading"
                data-testid="btn-upload-all"
                type="primary"
                @click="doUpload(undefined)"
            >上传全部待上传（{{ stats.pendingUpload ?? 0 }}）
            </el-button>
            <el-button
                v-perm="'pharmacy:drugTrace:export'"
                :disabled="!selection.length"
                :loading="uploading"
                data-testid="btn-upload-selected"
                @click="doUpload(selection.filter(isUploadPending).map((r) => r.id))"
            >上传选中（{{ selection.filter(isUploadPending).length }}）
            </el-button>
            <el-button data-testid="btn-refresh-stats" @click="loadStats">刷新统计</el-button>
          </div>

          <div v-if="uploadResult" class="upload-result" data-testid="upload-result">
            <el-descriptions :column="4" border size="small">
              <el-descriptions-item label="上传批次">{{ uploadResult.uploadBatchNo }}</el-descriptions-item>
              <el-descriptions-item label="总数">{{ uploadResult.total }}</el-descriptions-item>
              <el-descriptions-item label="成功">{{ uploadResult.success }}</el-descriptions-item>
              <el-descriptions-item label="失败">{{ uploadResult.failed }}</el-descriptions-item>
            </el-descriptions>
            <el-table v-if="uploadResult.failures?.length" :data="uploadResult.failures" border class="fail-table"
                      data-testid="upload-failures" size="small">
              <el-table-column label="追溯码" min-width="200" prop="traceCode" show-overflow-tooltip/>
              <el-table-column label="药品" prop="drugName" width="160"/>
              <el-table-column label="失败原因" min-width="240" prop="reason" show-overflow-tooltip/>
            </el-table>
          </div>

          <el-alert
              :closable="false"
              :title="`近 30 天已发药 ${stats.recentDispenseTotal ?? 0} 行，其中未核销追溯码 ${stats.unTracedDispense ?? 0} 行（必须采集品种 ${stats.requiredUnTracedDispense ?? 0} 行）——未核销是医保稽核的直接扣分项`"
              class="upload-hint-alert"
              type="info"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 详情弹窗（只读） -->
    <el-dialog v-model="detailVisible" data-testid="trace-detail-dialog" title="追溯码详情" width="720px">
      <el-descriptions v-if="detail" :column="2" border>
        <el-descriptions-item label="追溯码">{{ detail.traceCode }}</el-descriptions-item>
        <el-descriptions-item label="院内流水号">{{ detail.traceNo }}</el-descriptions-item>
        <el-descriptions-item label="药品">{{
            detail.drugName
          }}{{ detail.specification ? `（${detail.specification}）` : '' }}
        </el-descriptions-item>
        <el-descriptions-item label="批准文号">{{ detail.approvalNumber || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产厂家">{{ detail.manufacturer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="批号">{{ detail.stockBatchNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="码制">{{ codeTypeText(detail.codeType) }}</el-descriptions-item>
        <el-descriptions-item label="产品标识">{{ detail.drugDi || '-' }}</el-descriptions-item>
        <el-descriptions-item label="序列号">{{ detail.serialNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采集来源">{{ sourceText(detail.sourceType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusText(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="上传状态">{{ uploadText(detail.uploadStatus) }}</el-descriptions-item>
        <el-descriptions-item label="采集人">{{ detail.operatorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采集时间">{{ detail.scanTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发药单">{{ detail.dispensingNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="患者">{{ detail.patientName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="核销时间">{{ detail.dispenseTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="核销人">{{ detail.dispenseOperator || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上传批次">{{ detail.uploadBatchNo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="上传时间">{{ detail.uploadTime || '-' }}</el-descriptions-item>
        <el-descriptions-item :span="2" label="上传失败原因">{{ detail.uploadFailReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="作废类型">{{
            detail.voidType ? voidText(detail.voidType) : '-'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="作废原因">{{ detail.voidReason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 作废弹窗 -->
    <el-dialog v-model="voidVisible" data-testid="void-dialog" title="追溯码作废" width="480px">
      <el-form label-width="90px">
        <el-form-item label="作废类型">
          <el-select v-model="voidForm.voidType" data-testid="select-void-type" style="width: 100%">
            <el-option v-for="d in voidDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="原因">
          <el-input v-model="voidForm.reason" :rows="3" data-testid="input-void-reason" placeholder="必填，退药/报损/召回原因"
                    type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="voidVisible = false">取消</el-button>
        <el-button data-testid="btn-void-submit" type="primary" @click="submitVoid">确认作废</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 药品追溯码采集与核对（sql/156）
 *
 * 医保局口径：入库扫码采集、发药扫码核销，两个动作都要上传。三个页签对应三个岗位动作：
 *   ① 台账   —— 一码一行，正查（按码查去向）反查（按患者查用到哪些码）
 *   ② 采集与核对 —— 入库验收扫码采集 / 发药窗口扫码核销，两条闸门都在服务端
 *   ③ 上传   —— 批量上传医保局 + 失败重传 + 对账统计
 *
 * 闸门结论一律用服务端 /drugTrace/scan 返回的 canCollect / canDispense + tip，
 * 前端不自己判「能不能采」—— 未采集就发药、一码核销两次（回流药）、串码这三类判断
 * 一旦前端各写一份，迟早与服务端不一致。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  collectDrugTrace,
  deleteDrugTrace,
  getDrugTraceReconcile,
  listDrugTracePage,
  scanDrugTrace,
  uploadDrugTrace,
  verifyDispenseDrugTrace,
  voidDrugTrace,
} from '@/api/drugTrace';
import {getDispensingList} from '@/api/dispensing';
import {getDictDataMapList, getDrugSelectList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {
  canDeleteTrace,
  canVoidTrace,
  codeTypeText,
  isUploadPending,
  TRACE_SOURCE_TYPE,
  traceStatusTagType,
  uploadStatusTagType,
} from '@/lib/drugTrace';

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const activeTab = ref('ledger');
const query = reactive({
  keyword: '',
  status: null,
  uploadStatus: null,
  codeType: null,
  sourceType: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// ============ 字典 ============
const statusDict = ref([]);
const uploadDict = ref([]);
const codeTypeDict = ref([]);
const sourceDict = ref([]);
const voidDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const uploadText = (v) => dictLabelText(uploadDict.value, v);
const codeTypeDictText = (v) => dictLabelText(codeTypeDict.value, v);
const sourceText = (v) => dictLabelText(sourceDict.value, v);
const voidText = (v) => dictLabelText(voidDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.DRUG_TRACE_STATUS},${DICT_TYPE.DRUG_TRACE_UPLOAD_STATUS},${DICT_TYPE.DRUG_TRACE_CODE_TYPE},${DICT_TYPE.DRUG_TRACE_SOURCE_TYPE},${DICT_TYPE.DRUG_TRACE_VOID_TYPE}`);
    const d = res?.data || {};
    statusDict.value = d[DICT_TYPE.DRUG_TRACE_STATUS] || [];
    uploadDict.value = d[DICT_TYPE.DRUG_TRACE_UPLOAD_STATUS] || [];
    codeTypeDict.value = d[DICT_TYPE.DRUG_TRACE_CODE_TYPE] || [];
    sourceDict.value = d[DICT_TYPE.DRUG_TRACE_SOURCE_TYPE] || [];
    voidDict.value = d[DICT_TYPE.DRUG_TRACE_VOID_TYPE] || [];
  } catch (e) {
    console.error('加载追溯码字典失败', e);
  }
};
// ============ 对账统计 ============
const stats = ref({});
const statCards = computed(() => [
  {key: 'total', label: '采集总量', value: stats.value.total ?? 0},
  {key: 'inStock', label: '在库', value: stats.value.inStock ?? 0},
  {key: 'dispensed', label: '已核销', value: stats.value.dispensed ?? 0},
  {key: 'voided', label: '已作废', value: stats.value.voided ?? 0},
  {key: 'pendingUpload', label: '待上传', value: stats.value.pendingUpload ?? 0},
  {key: 'uploaded', label: '已上传', value: stats.value.uploaded ?? 0},
  {key: 'uploadFailed', label: '上传失败', value: stats.value.uploadFailed ?? 0},
  {key: 'unTracedDispense', label: '近30天发药未核销', value: stats.value.unTracedDispense ?? 0},
]);
const loadStats = async () => {
  try {
    const res = await getDrugTraceReconcile();
    if (res.code === 200)
      stats.value = res.data || {};
  } catch (e) {
    console.error('加载追溯码对账统计失败', e);
  }
};
// ============ 台账 ============
const loadList = async () => {
  loading.value = true;
  try {
    const res = await listDrugTracePage({
      keyword: query.keyword?.trim() || undefined,
      status: query.status ?? undefined,
      uploadStatus: query.uploadStatus ?? undefined,
      codeType: query.codeType ?? undefined,
      sourceType: query.sourceType ?? undefined,
      pageNum: query.pageNum,
      pageSize: query.pageSize,
    });
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || [];
      total.value = Number(res.data.total || 0);
    }
  } catch (e) {
    ElMessage.error(e?.message || '加载追溯码台账失败');
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.keyword = '';
  query.status = null;
  query.uploadStatus = null;
  query.codeType = null;
  query.sourceType = null;
  query.pageNum = 1;
  loadList();
};
// ============ 详情 ============
const detailVisible = ref(false);
const detail = ref(null);
const openDetail = (row) => {
  detail.value = row;
  detailVisible.value = true;
};
// ============ 采集 ============
const collectForm = reactive({
  traceCode: '',
  sourceType: TRACE_SOURCE_TYPE.INBOUND,
  drugId: null,
  stockId: null,
  remark: '',
});
const collectScan = ref(null);
const drugs = ref([]);
const loadDrugs = async () => {
  try {
    const res = await getDrugSelectList({});
    drugs.value = res?.data || [];
  } catch (e) {
    console.error('加载药品下拉失败', e);
  }
};
const doScanCollect = async () => {
  if (!collectForm.traceCode?.trim()) {
    ElMessage.warning('请先扫描或输入追溯码');
    return;
  }
  try {
    const res = await scanDrugTrace({
      traceCode: collectForm.traceCode.trim(),
      scene: 1,
      drugId: collectForm.drugId || undefined,
    });
    collectScan.value = res?.data || null;
    const d = res?.data;
    if (d && d.batches && d.batches.length === 1)
      collectForm.stockId = String(d.batches[0].stockId);
    if (d && !d.canCollect && d.tip)
      ElMessage.warning(d.tip);
  } catch (e) {
    ElMessage.error(e?.message || '扫码解析失败');
  }
};
const submitCollect = async () => {
  if (!collectScan.value?.canCollect) {
    ElMessage.warning(collectScan.value?.tip || '当前追溯码不满足采集条件');
    return;
  }
  if (!collectForm.stockId) {
    ElMessage.warning('请选择挂靠批次');
    return;
  }
  try {
    const res = await collectDrugTrace({
      traceCode: collectForm.traceCode.trim(),
      drugId: collectForm.drugId || undefined,
      stockId: collectForm.stockId,
      sourceType: collectForm.sourceType,
      remark: collectForm.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(`采集成功：${res.data?.traceNo || ''}`);
      collectForm.traceCode = '';
      collectForm.stockId = null;
      collectForm.remark = '';
      collectScan.value = null;
      await Promise.all([loadList(), loadStats()]);
    }
  } catch (e) {
    ElMessage.error(e?.message || '采集失败');
  }
};
// ============ 发药核销 ============
const dispenseCode = ref('');
const dispensingId = ref(null);
const dispensingOptions = ref([]);
const dispenseScan = ref(null);
const loadDispensing = async (keyword) => {
  try {
    const res = await getDispensingList({
      patientName: keyword?.trim() || undefined,
      dispensingStatus: 2,
      pageNum: 1,
      pageSize: 50,
    });
    dispensingOptions.value = res?.data?.records || [];
  } catch (e) {
    console.error('加载发药记录失败', e);
  }
};
const onDispensingSearch = (kw) => loadDispensing(kw);
const doScanDispense = async () => {
  if (!dispensingId.value) {
    ElMessage.warning('请先选择发药记录');
    return;
  }
  if (!dispenseCode.value?.trim()) {
    ElMessage.warning('请先扫描或输入追溯码');
    return;
  }
  try {
    const res = await scanDrugTrace({
      traceCode: dispenseCode.value.trim(),
      scene: 2,
      dispensingId: dispensingId.value,
    });
    dispenseScan.value = res?.data || null;
    const d = res?.data;
    if (d && !d.canDispense && d.tip)
      ElMessage.warning(d.tip);
  } catch (e) {
    ElMessage.error(e?.message || '扫码校验失败');
  }
};
const submitDispense = async () => {
  if (!dispenseScan.value?.canDispense) {
    ElMessage.warning(dispenseScan.value?.tip || '当前追溯码不满足核销条件');
    return;
  }
  try {
    const res = await verifyDispenseDrugTrace({
      traceCode: dispenseCode.value.trim(),
      dispensingId: dispensingId.value,
    });
    if (res.code === 200) {
      ElMessage.success('核销成功，已置为待上传');
      dispenseCode.value = '';
      dispenseScan.value = null;
      await Promise.all([loadList(), loadStats()]);
    }
  } catch (e) {
    ElMessage.error(e?.message || '核销失败');
  }
};
// ============ 作废 ============
const voidVisible = ref(false);
const voidForm = reactive({traceId: null, voidType: 1, reason: ''});
const openVoid = (row) => {
  voidForm.traceId = row.id;
  voidForm.voidType = Number(row.status) === 2 ? 1 : 2;
  voidForm.reason = '';
  voidVisible.value = true;
};
const submitVoid = async () => {
  if (!voidForm.reason?.trim()) {
    ElMessage.warning('请填写作废原因');
    return;
  }
  try {
    const res = await voidDrugTrace({
      traceId: voidForm.traceId,
      voidType: voidForm.voidType,
      reason: voidForm.reason.trim(),
    });
    if (res.code === 200) {
      ElMessage.success('已作废，状态变更将重新上传');
      voidVisible.value = false;
      await Promise.all([loadList(), loadStats()]);
    }
  } catch (e) {
    ElMessage.error(e?.message || '作废失败');
  }
};
// ============ 删除 ============
const doDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除追溯码「${row.traceCode}」？仅在库且未上传的误采记录可删，已核销/已上传的码属于医保数据不允许删除。`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deleteDrugTrace(row.id);
    if (res.code === 200) {
      ElMessage.success('已删除');
      await Promise.all([loadList(), loadStats()]);
    }
  } catch (e) {
    ElMessage.error(e?.message || '删除失败');
  }
};
// ============ 上传 ============
const uploading = ref(false);
const uploadResult = ref(null);
const selection = ref([]);
const onSelectionChange = (val) => {
  selection.value = val;
};
const doUpload = async (ids) => {
  uploading.value = true;
  try {
    const res = await uploadDrugTrace(ids && ids.length ? {ids} : {limit: 200});
    if (res.code === 200) {
      uploadResult.value = res.data;
      const d = res.data || {};
      if ((d.failed || 0) > 0) {
        ElMessage.warning(`上传完成：成功 ${d.success || 0} 条，失败 ${d.failed || 0} 条（见失败明细）`);
      } else {
        ElMessage.success(`上传完成：成功 ${d.success || 0} 条`);
      }
      await Promise.all([loadList(), loadStats()]);
    }
  } catch (e) {
    ElMessage.error(e?.message || '上传失败');
  } finally {
    uploading.value = false;
  }
};
onMounted(async () => {
  await loadDicts();
  await Promise.all([loadList(), loadStats(), loadDrugs(), loadDispensing()]);
});
</script>

<style scoped>
.drug-trace-view {
  padding: 16px;
}

.stat-row {
  margin-bottom: 12px;
}

.stat-card {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 10px;
  background: #fff;
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.query-bar {
  margin-bottom: 8px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}

.scan-result {
  margin-top: 8px;
}

.scan-tip {
  margin-top: 8px;
}

.upload-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.upload-hint {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.upload-actions {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.fail-table {
  margin-top: 10px;
}

.upload-hint-alert {
  margin-top: 12px;
}
</style>
