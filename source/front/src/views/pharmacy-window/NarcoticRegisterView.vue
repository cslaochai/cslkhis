<template>
  <div>
    <!-- 页头 -->
    <div class="mb-3 flex items-center justify-between">
      <div>
        <h2 class="text-lg font-semibold text-slate-800">麻精药品专册</h2>
        <p class="mt-1 text-sm text-slate-500">
          麻醉药品 / 精神药品 / 毒性药品逐笔登记台账；发药时自动登记，含 FEFO 实际批号与双人复核签名
        </p>
      </div>
      <el-button :icon="Refresh" @click="reloadAll">刷新</el-button>
    </div>

    <!-- 计数：走后端聚合 -->
    <div class="mb-3 grid grid-cols-3 gap-3">
      <div class="rounded-lg border border-slate-200 bg-white p-3" data-testid="narco-count-total">
        <div class="text-xs text-slate-500">专册登记总数</div>
        <div class="mt-1 text-xl font-semibold text-[#1269B5]">{{ countsLoaded ? counts.total : '—' }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3" data-testid="narco-count-pending">
        <div class="text-xs text-slate-500">待回收空安瓿</div>
        <div class="mt-1 text-xl font-semibold text-rose-600">{{ countsLoaded ? counts.pendingAmpoule : '—' }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-3" data-testid="narco-count-returned">
        <div class="text-xs text-slate-500">已回收空安瓿</div>
        <div class="mt-1 text-xl font-semibold text-emerald-600">{{ countsLoaded ? counts.returnedAmpoule : '—' }}</div>
      </div>
    </div>

    <!-- 查询卡：条件全部下推后端 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <el-form :model="query" inline @submit.prevent>
        <el-form-item label="关键字">
          <el-input
              v-model="query.keyword"
              :prefix-icon="Search"
              class="!w-96"
              clearable
              data-testid="narco-search"
              placeholder="患者姓名 / 患者号 / 处方号 / 登记号 / 药品名 / 批号"
              @clear="onSearch"
              @keyup.enter="onSearch"
          />
        </el-form-item>
        <el-form-item label="管制分类">
          <el-select v-model="query.specialFlag" class="!w-44" clearable data-testid="narco-flag-filter"
                     placeholder="全部" @change="onSearch">
            <el-option v-for="o in specialOptions" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="空安瓿状态">
          <el-select v-model="query.ampouleStatus" class="!w-36" clearable data-testid="narco-ampoule-filter"
                     placeholder="全部" @change="onSearch">
            <el-option v-for="o in AMPOULE_STATUS_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="发药日期">
          <el-date-picker
              v-model="query.dispenseRange"
              class="!w-64"
              data-testid="narco-date-range"
              end-placeholder="发药止"
              start-placeholder="发药起"
              type="daterange"
              value-format="YYYY-MM-DD"
              @change="onSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button :icon="Search" type="primary" @click="onSearch">查询</el-button>
          <el-button @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格卡：专册列表 -->
    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" data-testid="narco-table" stripe>
        <el-table-column fixed label="登记号" prop="registerNo" show-overflow-tooltip width="160"/>
        <el-table-column label="患者" prop="patientName" show-overflow-tooltip width="110">
          <template #default="{ row }">
            {{ row.patientName || '—' }}
            <span class="text-slate-400">{{ patientGenderText(row.gender) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="药品" min-width="180" prop="drugName" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag :type="flagTag(row.specialFlag)" class="mr-1" effect="dark" size="small">
              {{ flagText(row.specialFlag) }}
            </el-tag>
            <span>{{ row.drugName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="数量" prop="quantity" width="90">
          <template #default="{ row }">{{ row.quantity ?? '—' }} {{ row.unit || '' }}</template>
        </el-table-column>
        <el-table-column label="批号（FEFO 实扣）" min-width="150" prop="batchNo" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="row.batchNo ? '' : 'text-rose-500'">{{ row.batchNo || '无批号' }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="限量/实际" prop="limitDays" width="110">
          <template #default="{ row }">
            <span :class="overLimit(row) ? 'text-rose-600 font-medium' : ''">
              {{ row.limitDays ?? '—' }} / {{ row.duration ?? '—' }} 日
            </span>
          </template>
        </el-table-column>
        <el-table-column label="开方医师" prop="doctorName" show-overflow-tooltip width="100"/>
        <el-table-column label="发药人" prop="dispenseBy" show-overflow-tooltip width="100"/>
        <el-table-column label="复核人" prop="checkerName" show-overflow-tooltip width="100">
          <template #default="{ row }">
            <span :class="row.checkerName ? '' : 'text-slate-400'">{{ row.checkerName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发药时间" prop="dispenseTime" show-overflow-tooltip width="160"/>
        <el-table-column align="center" label="空安瓿" prop="ampouleStatus" width="100">
          <template #default="{ row }">
            <el-tag :type="ampouleStatusTagType(row.ampouleStatus)" size="small">{{
                ampouleStatusText(row.ampouleStatus)
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="150">
          <template #default="{ row }">
            <el-button :icon="View" data-testid="narco-detail-btn" link type="primary" @click="openDetail(row)">详情
            </el-button>
            <el-button
                v-if="canReturnAmpoule(row)"
                v-perm="'pharmacy:narcotic:edit'"
                :icon="Box"
                data-testid="narco-ampoule-btn"
                link
                type="danger"
                @click="openAmpoule(row)"
            >回收
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            :current-page="query.pageNum"
            :page-size="query.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情：专册全字段留痕 -->
    <el-drawer v-model="detailVisible" data-testid="narco-detail" size="46%" title="麻精专册登记详情">
      <div v-if="detail" class="space-y-3 text-sm">
        <div class="rounded border border-slate-200 p-3">
          <div class="mb-2 font-medium text-slate-700">登记信息</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="登记号">{{ detail.registerNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="发药时间">{{ detail.dispenseTime || '—' }}</el-descriptions-item>
            <el-descriptions-item label="处方号">{{ detail.prescriptionNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="发药单号">{{ detail.dispensingNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="开方科室">{{ detail.deptName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="临床诊断">{{ detail.diagnosis || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="rounded border border-slate-200 p-3">
          <div class="mb-2 font-medium text-slate-700">患者（麻精需实名）</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="姓名">{{ detail.patientName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="患者号">{{ detail.patientNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ patientGenderText(detail.gender) }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ detail.age ?? '—' }}</el-descriptions-item>
            <el-descriptions-item :span="2" label="身份证号">{{ detail.idCard || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="rounded border border-slate-200 p-3">
          <div class="mb-2 font-medium text-slate-700">药品与限量</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="管制分类">
              <el-tag :type="flagTag(detail.specialFlag)" effect="dark" size="small">{{
                  flagText(detail.specialFlag)
                }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="剂型">{{ detail.dosageForm || '—' }}</el-descriptions-item>
            <el-descriptions-item :span="2" label="药品">{{ detail.drugName || '—' }} {{
                detail.specification || ''
              }}
            </el-descriptions-item>
            <el-descriptions-item label="数量">{{ detail.quantity ?? '—' }} {{
                detail.unit || ''
              }}
            </el-descriptions-item>
            <el-descriptions-item label="批号">{{ detail.batchNo || '—' }}</el-descriptions-item>
            <el-descriptions-item label="法定限量">{{
                detail.limitDays ? detail.limitDays + ' 日常用量' : '不限'
              }}
            </el-descriptions-item>
            <el-descriptions-item label="处方天数">{{ detail.duration ?? '—' }}</el-descriptions-item>
            <el-descriptions-item :span="2" label="核定日用量">{{ detail.dailyDosage ?? '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="rounded border border-slate-200 p-3">
          <div class="mb-2 font-medium text-slate-700">签名（双人复核）</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="开方医师">{{ detail.doctorName || '—' }}</el-descriptions-item>
            <el-descriptions-item label="审方药师">{{ detail.auditBy || '—' }}</el-descriptions-item>
            <el-descriptions-item label="发药人">{{ detail.dispenseBy || '—' }}</el-descriptions-item>
            <el-descriptions-item label="复核人">{{ detail.checkerName || '—' }}</el-descriptions-item>
            <el-descriptions-item :span="2" label="复核时间">{{ detail.checkTime || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="rounded border border-slate-200 p-3">
          <div class="mb-2 font-medium text-slate-700">空安瓿回收 / 剩余液销毁</div>
          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="回收状态">
              <el-tag :type="ampouleStatusTagType(detail.ampouleStatus)" size="small">
                {{ ampouleStatusText(detail.ampouleStatus) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="发出安瓿数">{{ detail.ampouleIssued ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="回收空安瓿数">{{ detail.ampouleReturned ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="剩余液销毁量">{{ detail.ampouleDestroyed ?? '—' }}</el-descriptions-item>
            <el-descriptions-item label="登记人">{{ detail.returnBy || '—' }}</el-descriptions-item>
            <el-descriptions-item label="登记时间">{{ detail.returnTime || '—' }}</el-descriptions-item>
            <el-descriptions-item :span="2" label="说明">{{ detail.returnRemark || '—' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="detail.remark" class="rounded border border-amber-200 bg-amber-50 p-3 text-amber-700">
          {{ detail.remark }}
        </div>
      </div>
    </el-drawer>

    <!-- 空安瓿回收登记：只补回收字段 -->
    <el-dialog v-model="ampouleVisible" title="空安瓿回收 / 剩余液销毁登记" width="480px">
      <div v-if="ampouleRow" class="space-y-3 text-sm">
        <div class="rounded bg-slate-50 p-2 text-slate-600">
          <div>登记号：{{ ampouleRow.registerNo }}</div>
          <div>药品：{{ ampouleRow.drugName }}（{{ flagText(ampouleRow.specialFlag) }}）</div>
          <div>发出安瓿数：<span class="font-medium text-slate-800">{{ ampouleRow.ampouleIssued ?? '—' }}</span></div>
        </div>
        <el-form label-width="120px">
          <el-form-item label="回收空安瓿数" required>
            <el-input-number
                v-model="ampouleForm.ampouleReturned"
                :min="0"
                :precision="0"
                class="!w-full"
                controls-position="right"
                data-testid="narco-ampoule-input"
            />
          </el-form-item>
          <el-form-item label="剩余液销毁量">
            <el-input-number
                v-model="ampouleForm.ampouleDestroyed"
                :min="0"
                :precision="2"
                class="!w-full"
                controls-position="right"
            />
          </el-form-item>
          <el-form-item label="说明">
            <el-input
                v-model="ampouleForm.returnRemark"
                :rows="2"
                placeholder="双人销毁须写明见证人姓名"
                type="textarea"
            />
          </el-form-item>
        </el-form>
        <div class="text-xs text-slate-400">
          登记后不可撤销、不可修改 —— 专册是证据。回收数不得大于发出数，登记人取当前登录账号。
        </div>
      </div>
      <template #footer>
        <el-button @click="ampouleVisible = false">取消</el-button>
        <el-button v-perm="'pharmacy:narcotic:edit'" :loading="ampouleSubmitting" data-testid="narco-ampoule-submit"
                   type="danger" @click="submitAmpoule">确认登记
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 麻精药品专册（G10，等级评审一票否决项）
 *
 * 为什么必须有这一页：`sys_drug` 原先没有管制分类列、全库没有专册表、没有双人复核、
 * 没有空安瓿回收 —— 系统里「麻醉药品」和「维生素C」走的是完全一样的链路。
 * 《麻醉药品和精神药品管理条例》要求专册逐笔登记、批号可追溯、空安瓿回收，
 * 纸质专册不能替代系统留痕。
 *
 * 三条必须守住的口径：
 * 1. **专册是证据，不是可编辑的业务数据**：本页刻意不给"编辑/删除"按钮，
 *    唯一写入口是「空安瓿回收登记」，且只能补回收字段，不能改数量/批号/双人姓名。
 *    如果这里出现"修改登记"，那专册就从证据变成了台账录入表。
 * 2. **分类文案从字典取**（`his_drug_special_flag`），命中不了渲染「未知(n)」——
 *    不回落成"普通药品"，因为普通药品不受任何麻精管制，回落会把"分类没维护"洗成"不受管"。
 * 3. **计数走后端聚合**，不用本页 list 数（翻页就变）；筛选条件全部下推后端，
 *    不在前端对当前页做 filter —— 专册的价值就是"能查出某一笔"。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Box, Refresh, Search, View} from '@element-plus/icons-vue';
import {ampouleReturn, getNarcoticRegisterList, getNarcoticStatusCount} from '@/api/narcotic';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import {
  AMPOULE_STATUS_OPTIONS,
  ampouleStatusTagType,
  ampouleStatusText,
  specialFlagTagType,
  specialFlagText,
} from '@/lib/drugSpecialFlag';
import {patientGenderText} from '@/lib/patientGender';

const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '',
  specialFlag: null,
  ampouleStatus: null,
  dispenseRange: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
/** 字典：药品特殊管理分类（麻精毒放） */
const specialDict = ref([]);
const specialOptions = computed(() => specialDict.value.map((d) => ({label: d.dictLabel, value: Number(d.dictValue)})));
const loadDicts = async () => {
  try {
    // ⚠ 取值必须是 `res.data[dictType]`：request 拦截器已把响应剥成 `{code,message,data}`，
    //    而 getDictDataMapList 又把它包了一层 `{...res, data: {dictType: [...]}}`。
    //    少写这层 `.data` 不会报错 —— 只是 `specialDict` 恒为空数组，分类列全渲染「未知(n)」，
    //    看着像"字典没维护"，实际是前端没取到。全仓其余页面都写的是 res.data[...]。
    const res = await getDictDataMapList(DICT_TYPE.DRUG_SPECIAL_FLAG);
    specialDict.value = res?.data?.[DICT_TYPE.DRUG_SPECIAL_FLAG] || [];
  } catch (e) {
    // 拿不到字典 → 分类渲染「未知(n)」，不回落成"普通药品"；只记录不阻塞
    console.error('加载药品特殊管理分类字典失败', e);
  }
};
/** 计数：后端聚合，拿不到显示「—」而不是 0 —— 0 是"确实没有"，加载失败不是 */
const counts = ref({
  total: 0,
  pendingAmpoule: 0,
  returnedAmpoule: 0,
});
const countsLoaded = ref(false);
const loadCounts = async () => {
  try {
    const res = await getNarcoticStatusCount();
    if (res.code === 200 && res.data) {
      counts.value = {
        total: Number(res.data.total ?? 0),
        pendingAmpoule: Number(res.data.pendingAmpoule ?? 0),
        returnedAmpoule: Number(res.data.returnedAmpoule ?? 0),
      };
      countsLoaded.value = true;
    }
  } catch (e) {
    console.error('加载专册计数失败', e);
  }
};
const buildQuery = () => ({
  keyword: query.keyword?.trim() || undefined,
  specialFlag: query.specialFlag ?? undefined,
  ampouleStatus: query.ampouleStatus ?? undefined,
  dispenseDateStart: query.dispenseRange?.[0] || undefined,
  dispenseDateEnd: query.dispenseRange?.[1] || undefined,
  pageNum: query.pageNum,
  pageSize: query.pageSize,
});
const loadList = async () => {
  loading.value = true;
  try {
    const res = await getNarcoticRegisterList(buildQuery());
    if (res.code === 200 && res.data) {
      rows.value = res.data.records || [];
      total.value = Number(res.data.total || 0);
    }
  } catch (e) {
    ElMessage.error('加载麻精专册失败');
    console.error(e);
  } finally {
    loading.value = false;
  }
};
const reloadAll = async () => {
  await Promise.all([loadList(), loadCounts()]);
};
const onSearch = () => {
  query.pageNum = 1;
  loadList();
};
const onReset = () => {
  query.keyword = '';
  query.specialFlag = null;
  query.ampouleStatus = null;
  query.dispenseRange = null;
  query.pageNum = 1;
  loadCounts();
  loadList();
};
const handleSizeChange = (size) => {
  query.pageSize = size;
  query.pageNum = 1;
  loadList();
};
const handleCurrentChange = (page) => {
  query.pageNum = page;
  loadList();
};
// ---------------- 详情 ----------------
const detailVisible = ref(false);
const detail = ref(null);
const openDetail = (row) => {
  detail.value = row;
  detailVisible.value = true;
};
// ---------------- 空安瓿回收登记 ----------------
const ampouleVisible = ref(false);
const ampouleRow = ref(null);
const ampouleForm = reactive({ampouleReturned: null, ampouleDestroyed: null, returnRemark: ''});
const ampouleSubmitting = ref(false);
const openAmpoule = (row) => {
  ampouleRow.value = row;
  ampouleForm.ampouleReturned = null;
  ampouleForm.ampouleDestroyed = null;
  ampouleForm.returnRemark = '';
  ampouleVisible.value = true;
};
const submitAmpoule = async () => {
  if (!ampouleRow.value)
    return;
  const returned = ampouleForm.ampouleReturned;
  if (returned === null || returned === undefined || Number(returned) < 0) {
    ElMessage.warning('请填写回收空安瓿数（非负数）');
    return;
  }
  ampouleSubmitting.value = true;
  try {
    const res = await ampouleReturn({
      id: ampouleRow.value.id,
      ampouleReturned: Number(returned),
      ampouleDestroyed: ampouleForm.ampouleDestroyed === null ? undefined : Number(ampouleForm.ampouleDestroyed),
      returnRemark: ampouleForm.returnRemark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('空安瓿回收已登记');
      ampouleVisible.value = false;
      await reloadAll();
    } else {
      ElMessage.error(res.message || '登记失败');
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '登记失败');
  } finally {
    ampouleSubmitting.value = false;
  }
};
/** 只有"待回收"才给登记按钮：已回收再点会被后端拒（不是遗漏） */
const canReturnAmpoule = (row) => Number(row.ampouleStatus) === 1;
const flagText = (v) => specialFlagText(v, specialDict.value);
const flagTag = (v) => specialFlagTagType(v) || 'info';
/**
 * 是否超限量。判定用与后端同一口径的字段（limitDays / duration），
 * 不用库里没落的 actualDays —— 表格取不到的值不要假装能取到。
 */
const overLimit = (row) => {
  const limit = Number(row.limitDays);
  const actual = Number(row.duration);
  if (!Number.isFinite(limit) || limit <= 0 || !Number.isFinite(actual))
    return false;
  return actual > limit;
};
onMounted(async () => {
  await loadDicts();
  await reloadAll();
});
</script>
