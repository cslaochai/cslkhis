<template>
  <div>
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input v-model="query.keyword" clearable placeholder="设备编码/名称/型号/科室" style="width: 220px"
                    @keyup.enter="query.pageNum = 1; loadList()"/>
        </el-form-item>
        <el-form-item label="设备类别">
          <el-select v-model="query.category" :fit-input-width="false" clearable placeholder="设备类别"
                     style="width: 150px">
            <el-option v-for="d in categoryDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" :fit-input-width="false" clearable placeholder="状态" style="width: 110px">
            <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="query.pageNum = 1; loadList()">查询</el-button>
          <el-button :icon="Refresh" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="equipment-table" stripe>
        <el-table-column label="资产编号" prop="equipmentCode" width="140"/>
        <el-table-column label="设备名称" min-width="150" prop="equipmentName" show-overflow-tooltip/>
        <el-table-column label="型号" prop="model" show-overflow-tooltip width="120"/>
        <el-table-column label="使用科室" prop="deptName" width="110"/>
        <el-table-column label="类别" prop="category" width="120">
          <template #default="{ row }">{{ categoryText(row.category) }}</template>
        </el-table-column>
        <el-table-column align="right" label="原值(元)" prop="purchasePrice" width="110">
          <template #default="{ row }">{{
              row.purchasePrice != null ? Number(row.purchasePrice).toLocaleString() : '—'
            }}
          </template>
        </el-table-column>
        <el-table-column label="最近维保" width="110">
          <template #default="{ row }">{{ fmtDate(row.lastMaintainDate) }}</template>
        </el-table-column>
        <el-table-column label="下次维保" width="110">
          <template #default="{ row }">
            <span :class="{ 'text-red-500 font-bold': row.nextMaintainDate && row.nextMaintainDate < today }">
              {{ fmtDate(row.nextMaintainDate) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="计量有效期至" width="120">
          <template #default="{ row }">
            <span v-if="row.meteringValidUntil" :class="{ 'text-red-500 font-bold': meteringExpired(row) }">
              {{ fmtDate(row.meteringValidUntil) }}{{ meteringExpired(row) ? '（过期）' : '' }}
            </span>
            <span v-else class="text-gray-400">—</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" prop="status" width="90">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="200">
          <template #default="{ row }">
            <el-button v-perm="'asset:equipment:add'" link size="small" type="primary" @click="openMaintain(row)">
              维保登记
            </el-button>
            <el-button v-perm="'asset:equipment:add'" link size="small" type="success" @click="openMetering(row)">
              计量登记
            </el-button>
            <el-button link size="small" @click="openDetail(row)">记录</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" :page-size="query.pageSize" :total="total"
                       layout="total, prev, pager, next" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 维保登记 -->
    <el-dialog v-model="maintainVisible" :close-on-click-modal="false" :title="`维保登记 — ${maintainForm.equipmentName}`"
               width="540px">
      <el-form label-width="100px">
        <el-form-item label="维保类型" required>
          <el-radio-group v-model="maintainForm.maintainType">
            <el-radio v-for="d in maintainTypeDict" :key="d.dictValue" :value="Number(d.dictValue)">
              {{ d.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="维保日期" required>
          <el-date-picker v-model="maintainForm.maintainDate" style="width: 200px" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="下次维保日期">
          <el-date-picker v-model="maintainForm.nextMaintainDate" style="width: 200px" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="费用(元)">
          <el-input-number v-model="maintainForm.cost" :min="0" :precision="2" style="width: 200px"/>
        </el-form-item>
        <el-form-item label="故障描述">
          <el-input v-model="maintainForm.faultDesc" :rows="2" type="textarea"/>
        </el-form-item>
        <el-form-item label="处理结果">
          <el-input v-model="maintainForm.handleResult" :rows="2" type="textarea"/>
        </el-form-item>
        <el-form-item label="维保结果" required>
          <el-radio-group v-model="maintainForm.maintainResult">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="2">异常</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="维保人">
          <el-input v-model="maintainForm.handlerName" placeholder="不填默认当前登录人" style="width: 200px"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="maintainVisible = false">取消</el-button>
        <el-button v-perm="'asset:equipment:add'" type="primary" @click="submitMaintain">登记</el-button>
      </template>
    </el-dialog>

    <!-- 计量登记 -->
    <el-dialog v-model="meteringVisible" :close-on-click-modal="false" :title="`计量登记 — ${meteringForm.equipmentName}`"
               width="520px">
      <el-form label-width="100px">
        <el-form-item label="计量类型" required>
          <el-radio-group v-model="meteringForm.meteringType">
            <el-radio v-for="d in meteringTypeDict" :key="d.dictValue" :value="Number(d.dictValue)">
              {{ d.dictLabel }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计量日期" required>
          <el-date-picker v-model="meteringForm.meteringDate" style="width: 200px" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="有效期至" required>
          <el-date-picker v-model="meteringForm.validUntil" style="width: 200px" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="计量结果" required>
          <el-radio-group v-model="meteringForm.meteringResult">
            <el-radio :value="1">合格</el-radio>
            <el-radio :value="2">不合格</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="证书编号">
          <el-input v-model="meteringForm.certNo" style="width: 220px"/>
        </el-form-item>
        <el-form-item label="检定机构">
          <el-input v-model="meteringForm.agency" style="width: 220px"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="meteringVisible = false">取消</el-button>
        <el-button v-perm="'asset:equipment:add'" type="primary" @click="submitMetering">登记</el-button>
      </template>
    </el-dialog>

    <!-- 记录详情 -->
    <el-dialog v-model="detailVisible" :title="`设备记录 — ${detail?.equipmentName || ''}`" width="820px">
      <template v-if="detail">
        <div class="text-sm text-gray-500 mb-2">最近维保记录（{{ detail.recentMaintains?.length || 0 }} 条）</div>
        <el-table :data="detail.recentMaintains || []" border data-testid="maintain-records" size="small">
          <el-table-column label="类型" width="70">
            <template #default="{ row }">{{ maintainTypeText(row) }}</template>
          </el-table-column>
          <el-table-column label="维保日期" width="110">
            <template #default="{ row }">{{ fmtDate(row.maintainDate) }}</template>
          </el-table-column>
          <el-table-column label="故障描述" min-width="140" prop="faultDesc" show-overflow-tooltip>
            <template #default="{ row }">{{ row.faultDesc || '—' }}</template>
          </el-table-column>
          <el-table-column label="处理结果" min-width="140" prop="handleResult" show-overflow-tooltip>
            <template #default="{ row }">{{ row.handleResult || '—' }}</template>
          </el-table-column>
          <el-table-column label="结果" prop="maintainResultText" width="70"/>
          <el-table-column label="维保人" prop="handlerName" width="90"/>
          <el-table-column label="操作" width="70">
            <template #default="{ row }">
              <el-button v-perm="'asset:equipment:delete'" link size="small" type="danger"
                         @click="delMaintain(row)">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="text-sm text-gray-500 mt-4 mb-2">最近计量记录（{{ detail.recentMeterings?.length || 0 }} 条）</div>
        <el-table :data="detail.recentMeterings || []" border data-testid="metering-records" size="small">
          <el-table-column label="类型" width="70">
            <template #default="{ row }">{{ meteringTypeText(row) }}</template>
          </el-table-column>
          <el-table-column label="计量日期" width="110">
            <template #default="{ row }">{{ fmtDate(row.meteringDate) }}</template>
          </el-table-column>
          <el-table-column label="有效期至" width="110">
            <template #default="{ row }">{{ fmtDate(row.validUntil) }}</template>
          </el-table-column>
          <el-table-column label="结果" prop="meteringResultText" width="70"/>
          <el-table-column label="证书编号" prop="certNo" width="130">
            <template #default="{ row }">{{ row.certNo || '—' }}</template>
          </el-table-column>
          <el-table-column label="检定机构" min-width="130" prop="agency" show-overflow-tooltip>
            <template #default="{ row }">{{ row.agency || '—' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="70">
            <template #default="{ row }">
              <el-button v-perm="'asset:equipment:delete'" link size="small" type="danger"
                         @click="delMetering(row)">删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 设备管理（G22，菜单 902）
 *
 * 台账 = sys_equipment（49 号铺底 90 行）；本页聚焦维保/计量：
 * - 维保登记成功回写档案「最近维保日期」，下次维保日期 = 最近维保 + 维保周期（前端只展示，计算在后端 VO）
 * - 计量（强检/校准）有效期至过期即台账亮红
 * - 维保/计量记录录错可删（后端会按剩余记录重算最近维保日期）
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Refresh, Search} from '@element-plus/icons-vue';
import {
  equipmentListPage,
  getEquipmentDetail,
  maintainCreate,
  maintainDelete,
  meteringCreate,
  meteringDelete,
} from '@/api/equipment';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
// ---------------- 字典 ----------------
const statusDict = ref([]);
const categoryDict = ref([]);
const maintainTypeDict = ref([]);
const meteringTypeDict = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const categoryText = (v) => dictLabelText(categoryDict.value, v);
const loadDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.EQUIP_MAINTAIN_TYPE},${DICT_TYPE.EQUIP_METERING_TYPE},${DICT_TYPE.EQUIP_METERING_RESULT}`);
    maintainTypeDict.value = res?.data?.[DICT_TYPE.EQUIP_MAINTAIN_TYPE] || [];
    meteringTypeDict.value = res?.data?.[DICT_TYPE.EQUIP_METERING_TYPE] || [];
    // 设备类别/状态是 49 号铺底的老字典，不在 G22 段，单独取
    const res2 = await getDictDataMapList('his_equipment_category,his_equipment_status');
    categoryDict.value = res2?.data?.['his_equipment_category'] || [];
    statusDict.value = res2?.data?.['his_equipment_status'] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
// ---------------- 台账列表 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '', category: null, status: null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await equipmentListPage({
      keyword: query.keyword.trim() || undefined,
      category: query.category ?? undefined,
      status: query.status ?? undefined,
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};
const reset = () => {
  Object.assign(query, {keyword: '', category: null, status: null, pageNum: 1});
  loadList();
};
const statusTag = (v) => ({1: 'success', 2: 'info', 3: 'warning', 4: 'danger'}[v] || 'info');
// ---------------- 维保 / 计量登记 ----------------
const maintainVisible = ref(false);
const maintainForm = reactive({
  equipmentId: null, equipmentName: '', maintainType: 1,
  maintainDate: '', nextMaintainDate: '', cost: '', faultDesc: '', handleResult: '',
  maintainResult: 1, handlerName: '',
});
const openMaintain = (row) => {
  Object.assign(maintainForm, {
    equipmentId: Number(row.id), equipmentName: row.equipmentName, maintainType: 1,
    maintainDate: '', nextMaintainDate: '', cost: '', faultDesc: '', handleResult: '',
    maintainResult: 1, handlerName: '',
  });
  maintainVisible.value = true;
};
const submitMaintain = async () => {
  if (!maintainForm.maintainDate) {
    ElMessage.warning('请选择维保日期');
    return;
  }
  try {
    const res = await maintainCreate({
      equipmentId: maintainForm.equipmentId,
      maintainType: maintainForm.maintainType,
      maintainDate: maintainForm.maintainDate,
      nextMaintainDate: maintainForm.nextMaintainDate || undefined,
      cost: maintainForm.cost || undefined,
      faultDesc: maintainForm.faultDesc || undefined,
      handleResult: maintainForm.handleResult || undefined,
      maintainResult: maintainForm.maintainResult,
      handlerName: maintainForm.handlerName || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('维保登记成功');
      maintainVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '登记失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('登记失败');
  }
};
const meteringVisible = ref(false);
const meteringForm = reactive({
  equipmentId: null, equipmentName: '', meteringType: 1,
  meteringDate: '', validUntil: '', meteringResult: 1, certNo: '', agency: '',
});
const openMetering = (row) => {
  Object.assign(meteringForm, {
    equipmentId: Number(row.id), equipmentName: row.equipmentName, meteringType: 1,
    meteringDate: '', validUntil: '', meteringResult: 1, certNo: '', agency: '',
  });
  meteringVisible.value = true;
};
const submitMetering = async () => {
  if (!meteringForm.meteringDate || !meteringForm.validUntil) {
    ElMessage.warning('请选择计量日期与有效期至');
    return;
  }
  try {
    const res = await meteringCreate({
      equipmentId: meteringForm.equipmentId,
      meteringType: meteringForm.meteringType,
      meteringDate: meteringForm.meteringDate,
      validUntil: meteringForm.validUntil,
      meteringResult: meteringForm.meteringResult,
      certNo: meteringForm.certNo || undefined,
      agency: meteringForm.agency || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('计量登记成功');
      meteringVisible.value = false;
      loadList();
    } else
      ElMessage.error(res.message || '登记失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('登记失败');
  }
};
// ---------------- 详情（最近维保/计量） ----------------
const detailVisible = ref(false);
const detail = ref(null);
const openDetail = async (row) => {
  try {
    const res = await getEquipmentDetail(row.id);
    if (res.code === 200) {
      detail.value = res.data;
      detailVisible.value = true;
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  }
};
const fmtDate = (v) => (v ? String(v).slice(0, 10) : '—');
const meteringExpired = (row) => row.meteringExpired;
// 本地时区当日（勿用 toISOString —— UTC+8 零点前会取到昨天）
const today = (() => {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
})();
const dictVal = (dict, v) => dictLabelText(dict, v);
const maintainTypeText = (row) => dictVal(maintainTypeDict.value, row.maintainType);
const meteringTypeText = (row) => dictVal(meteringTypeDict.value, row.meteringType);
const delMaintain = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该维保记录？删除后档案最近维保日期按剩余记录重算。', '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await maintainDelete(row.id);
    if (res.code === 200) {
      ElMessage.success('已删除');
      const detailRes = await getEquipmentDetail(detail.value.id);
      if (detailRes.code === 200)
        detail.value = detailRes.data;
      loadList();
    } else
      ElMessage.error(res.message || '删除失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
const delMetering = async (row) => {
  try {
    await ElMessageBox.confirm('确认删除该计量记录？', '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await meteringDelete(row.id);
    if (res.code === 200) {
      ElMessage.success('已删除');
      const detailRes = await getEquipmentDetail(detail.value.id);
      if (detailRes.code === 200)
        detail.value = detailRes.data;
      loadList();
    } else
      ElMessage.error(res.message || '删除失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
onMounted(() => {
  loadDicts();
  loadList();
});
</script>
