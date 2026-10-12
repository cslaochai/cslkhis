<template>
  <!-- 就诊详情抽屉：六段 -->
  <el-drawer v-model="visible" destroy-on-close size="62%" title="就诊详情">
    <div v-loading="detailLoading" class="space-y-4">
      <template v-if="drawerRow">
        <!-- ① 挂号信息 -->
        <section class="rounded-lg border border-slate-200 p-3">
          <h4 class="mb-2 flex items-center gap-1.5 text-sm font-semibold text-slate-800">
            <el-icon>
              <Document/>
            </el-icon>
            挂号信息
          </h4>
          <div class="grid grid-cols-4 gap-x-4 gap-y-2 text-xs">
            <div><p class="text-slate-400">就诊号</p>
              <p class="font-mono font-medium text-slate-800">{{ drawerRow.registNo }}</p></div>
            <div><p class="text-slate-400">患者</p>
              <p class="font-medium text-slate-800">{{ drawerRow.patientName }}（{{ drawerRow.patientNo }}）</p></div>
            <div><p class="text-slate-400">性别 / 年龄</p>
              <p class="text-slate-700">{{ patientGenderText(drawerRow.gender) }} / {{ drawerRow.age ?? '-' }} 岁</p>
            </div>
            <div><p class="text-slate-400">号别</p>
              <p class="text-slate-700">
                {{ drawerRow.registType != null ? statusOf(REGIST_TYPE, drawerRow.registType).label : '-' }}</p></div>
            <div><p class="text-slate-400">就诊日期</p>
              <p class="text-slate-700">{{ drawerRow.visitDate || '-' }}</p></div>
            <div><p class="text-slate-400">挂号时间</p>
              <p class="text-slate-700">{{ fmt(drawerRow.registTime) }}</p></div>
            <div><p class="text-slate-400">科室 / 医生</p>
              <p class="text-slate-700">{{ drawerRow.deptName }} / {{ drawerRow.doctorName }}</p></div>
            <div><p class="text-slate-400">来源 / 结算</p>
              <p class="text-slate-700">
                {{ drawerRow.registSource != null ? statusOf(REGIST_SOURCE, drawerRow.registSource).label : '-' }} /
                {{
                  drawerRow.settlementType != null ? statusOf(SETTLEMENT_TYPE, drawerRow.settlementType).label : '-'
                }}
              </p></div>
            <div><p class="text-slate-400">就诊类型</p>
              <p class="text-slate-700">{{
                  drawerRow.revisitType != null ? statusOf(REVISIT_TYPE, drawerRow.revisitType).label : '未标注'
                }}</p></div>
            <div><p class="text-slate-400">当前状态</p>
              <el-tag :type="logStatusStyle(drawerRow).tagType" effect="plain" size="small">
                {{ drawerRow.logStatusLabel || logStatusStyle(drawerRow).label }}
              </el-tag>
            </div>
          </div>
        </section>

        <!-- ② 队列过程 -->
        <section class="rounded-lg border border-slate-200 p-3">
          <h4 class="mb-2 text-sm font-semibold text-slate-800">队列过程</h4>
          <div v-if="drawerRow.queueId" class="grid grid-cols-4 gap-x-4 gap-y-2 text-xs">
            <div><p class="text-slate-400">排队号 / 序号</p>
              <p class="font-mono text-slate-800">{{ shortQueueNo(drawerRow.queueNo) }} /
                {{ drawerRow.sequenceNo ?? '-' }}</p></div>
            <div><p class="text-slate-400">签到到达</p>
              <p class="text-slate-700">{{ fmt(drawerRow.arriveTime) }}</p></div>
            <div><p class="text-slate-400">叫号</p>
              <p class="text-slate-700">{{ fmt(drawerRow.callTime) }}（{{ drawerRow.callCount ?? 0 }} 次）</p></div>
            <div><p class="text-slate-400">开始就诊</p>
              <p class="text-slate-700">{{ fmt(drawerRow.startTime) }}</p></div>
            <div><p class="text-slate-400">结束</p>
              <p class="text-slate-700">{{ fmt(drawerRow.endTime) }}</p></div>
            <div><p class="text-slate-400">候诊时长</p>
              <p class="text-slate-700">{{ duration(drawerRow.waitMinutes) }}</p></div>
            <div><p class="text-slate-400">就诊时长</p>
              <p class="text-slate-700">{{ duration(drawerRow.visitMinutes) }}</p></div>
            <div><p class="text-slate-400">过号</p>
              <p class="text-slate-700">
                {{
                  drawerRow.isOverdue === 1 ? `已过号 · ${fmt(drawerRow.overdueTime)}${drawerRow.overdueReason ? ' · ' + drawerRow.overdueReason : ''}` : '否'
                }}
              </p></div>
          </div>
          <div v-else class="py-3 text-xs text-slate-400">该挂号还未签到入队（无队列记录）</div>
        </section>

        <!-- ③ 病历摘要 -->
        <section class="rounded-lg border border-slate-200 p-3">
          <h4 class="mb-2 text-sm font-semibold text-slate-800">病历摘要</h4>
          <div v-if="record()" class="space-y-2 text-xs">
            <div class="grid grid-cols-3 gap-x-4 gap-y-2">
              <div><p class="text-slate-400">主诉</p>
                <p class="whitespace-pre-wrap text-slate-700">{{ record().chiefComplaint || '-' }}</p></div>
              <div><p class="text-slate-400">现病史</p>
                <p class="whitespace-pre-wrap text-slate-700">{{ record().presentIllness || '-' }}</p></div>
              <div><p class="text-slate-400">既往史</p>
                <p class="whitespace-pre-wrap text-slate-700">{{ record().pastHistory || '-' }}</p></div>
              <div><p class="text-slate-400">体格检查</p>
                <p class="whitespace-pre-wrap text-slate-700">{{ record().physicalExamination || '-' }}</p></div>
              <div><p class="text-slate-400">初步诊断</p>
                <p class="whitespace-pre-wrap text-slate-700">{{ record().diagnosis || '-' }}</p></div>
              <div><p class="text-slate-400">处理意见</p>
                <p class="whitespace-pre-wrap text-slate-700">{{
                    record().treatmentPlan || record().advice || '-'
                  }}</p></div>
            </div>
            <div class="flex flex-wrap gap-x-4 gap-y-1 border-t border-slate-100 pt-2 text-slate-500">
              <span>病历号 <b class="font-mono text-slate-700">{{ record().recordNo || '-' }}</b></span>
              <span>状态 <b class="text-slate-700">{{ record().recordStatus ?? '-' }}</b></span>
              <span>书写 <b class="text-slate-700">{{ fmt(record().createTime) }}</b></span>
              <span>提交 <b class="text-slate-700">{{ fmt(record().submitTime) }}</b></span>
            </div>
          </div>
          <div v-else class="py-3 text-xs text-slate-400">本次就诊还没有病历记录</div>
        </section>

        <!-- ④ 处方 -->
        <section class="rounded-lg border border-slate-200 p-3">
          <h4 class="mb-2 text-sm font-semibold text-slate-800">处方（{{ prescriptions().length }}）</h4>
          <div v-if="prescriptions().length" class="space-y-3">
            <div v-for="p in prescriptions()" :key="p.id" class="rounded border border-slate-100 bg-slate-50/60 p-2">
              <div class="mb-1 flex flex-wrap items-center gap-x-3 text-xs text-slate-600">
                <span class="font-mono font-medium text-slate-800">{{ p.prescriptionNo }}</span>
                <span>{{ p.diagnosis || '无诊断' }}</span>
                <span>金额 <b class="text-slate-800">¥{{ money(p.totalAmount) }}</b></span>
                <el-tag :type="statusOf(APPLY_STATUS, p.prescriptionStatus).tagType" effect="plain" size="small">
                  {{ statusOf(APPLY_STATUS, p.prescriptionStatus).label }}
                </el-tag>
              </div>
              <el-table :data="p.details || []" border size="small">
                <el-table-column label="药品" min-width="140" prop="drugName"/>
                <el-table-column label="规格" prop="specification" width="110"/>
                <el-table-column align="center" label="数量" prop="quantity" width="70"/>
                <el-table-column align="center" label="单位" prop="unit" width="60"/>
                <el-table-column align="center" label="单次剂量" prop="singleDosage" width="90"/>
                <el-table-column align="center" label="频次" prop="frequency" width="80"/>
                <el-table-column align="center" label="用法" prop="route" width="80"/>
                <el-table-column align="right" label="金额" width="80">
                  <template #default="{ row: d }">¥{{ money(d.amount) }}</template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div v-else class="py-3 text-xs text-slate-400">本次就诊没有处方</div>
        </section>

        <!-- ⑤ 检查检验 -->
        <section class="rounded-lg border border-slate-200 p-3">
          <h4 class="mb-2 text-sm font-semibold text-slate-800">检查 / 检验申请</h4>
          <div class="space-y-3">
            <div>
              <p class="mb-1 text-xs font-medium text-slate-500">检查（{{ inspectionApplies().length }}）</p>
              <el-table v-if="inspectionApplies().length" :data="inspectionApplies()" border size="small">
                <el-table-column label="申请单号" prop="applyNo" width="150"/>
                <el-table-column label="检查项目" min-width="140" prop="inspectionItemName"/>
                <el-table-column label="检查部位" prop="bodyPart" width="100"/>
                <el-table-column label="临床诊断" min-width="120" prop="clinicalDiagnosis" show-overflow-tooltip/>
                <el-table-column align="center" label="状态" width="90">
                  <template #default="{ row: a }">
                    <el-tag :type="statusOf(APPLY_STATUS, a.applyStatus).tagType" effect="plain" size="small">
                      {{ statusOf(APPLY_STATUS, a.applyStatus).label }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <p v-else class="py-2 text-xs text-slate-400">无检查申请</p>
            </div>
            <div>
              <p class="mb-1 text-xs font-medium text-slate-500">检验（{{ laboratoryApplies().length }}）</p>
              <el-table v-if="laboratoryApplies().length" :data="laboratoryApplies()" border size="small">
                <el-table-column label="申请单号" prop="applyNo" width="150"/>
                <el-table-column label="检验项目" min-width="140" prop="laboratoryItemName"/>
                <el-table-column label="标本" prop="specimenType" width="90"/>
                <el-table-column label="临床诊断" min-width="120" prop="clinicalDiagnosis" show-overflow-tooltip/>
                <el-table-column align="center" label="状态" width="90">
                  <template #default="{ row: a }">
                    <el-tag :type="statusOf(APPLY_STATUS, a.applyStatus).tagType" effect="plain" size="small">
                      {{ statusOf(APPLY_STATUS, a.applyStatus).label }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <p v-else class="py-2 text-xs text-slate-400">无检验申请</p>
            </div>
          </div>
        </section>

        <!-- ⑥ 收费 -->
        <section class="rounded-lg border border-slate-200 p-3">
          <h4 class="mb-2 text-sm font-semibold text-slate-800">收费记录（{{ chargeList.length }}）</h4>
          <div v-if="chargeList.length" class="space-y-3">
            <div v-for="c in chargeList" :key="c.id" class="rounded border border-slate-100 p-2">
              <div class="mb-1 flex flex-wrap items-center gap-x-3 text-xs text-slate-600">
                <span class="font-mono font-medium text-slate-800">{{ c.billNo }}</span>
                <span>总额 <b class="text-slate-800">¥{{ money(c.totalAmount) }}</b></span>
              </div>
              <el-table :data="c.items || []" border size="small">
                <el-table-column label="项目" min-width="140" prop="itemName"/>
                <el-table-column label="编码" prop="itemCode" width="120"/>
                <el-table-column label="规格" prop="specification" width="110"/>
                <el-table-column align="center" label="数量" prop="quantity" width="70"/>
                <el-table-column align="right" label="金额" width="90">
                  <template #default="{ row: d }">¥{{ money(d.amount) }}</template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div v-else class="py-3 text-xs text-slate-400">本次就诊没有收费记录</div>
        </section>
      </template>
    </div>
  </el-drawer>
</template>

<script setup>
// 就诊详情抽屉（六段）：①② 段读父页面传来的当前行（打开瞬间不闪白），
// ③④⑤⑥ 段自己按行上的 registId / patientId 取数（原内联于 TodayVisitsView，接口逐字未动）
import {computed, ref, watch} from 'vue';
import {Document} from '@element-plus/icons-vue';
import {getByRegistId, getRecordDetail} from '@/api/emr';
import {listItemsByPatient} from '@/api/settlementBill';
import {
  APPLY_STATUS,
  REGIST_SOURCE,
  REGIST_TYPE,
  REVISIT_TYPE,
  SETTLEMENT_TYPE,
  statusOf,
} from '@/lib/statusColor';
import {patientGenderText} from '@/lib/patientGender';
import {shortQueueNo} from '@/lib/utils';
import {duration, fmt, logStatusStyle} from '@/lib/todayVisits';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前就诊行（列表行对象，含挂号 + 队列全过程时间戳）
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const drawerRow = computed(() => props.row);
const detailLoading = ref(false);
/** ③④⑤ 来自 /emr/getRecordDetailById */
const emrDetail = ref(null);
/** ⑥ 收费（按患者取，再按 registId 归属本次就诊） */
const chargeList = ref([]);

const loadDetail = async (row) => {
  detailLoading.value = true;
  try {
    // ③ ④ ⑤ 段：先按 registId 取病历，再按 recordId 取「病历 + 处方 + 检查 + 检验」
    const recRes = await getByRegistId({registId: row.registId});
    const rec = Array.isArray(recRes.data) ? recRes.data[0] : recRes.data;
    if (rec?.id) {
      const detailRes = await getRecordDetail(String(rec.id));
      emrDetail.value = detailRes.data || null;
    }
    // ⑥ 收费段：四层按患者取账单行快照，门诊一次就诊 = encounterId 即挂号ID，过滤后按账单归组成卡片
    if (row.patientId) {
      const chargeRes = await listItemsByPatient(row.patientId);
      const all = chargeRes.data || [];
      const items = all.filter((c) => String(c.encounterId) === String(row.registId));
      chargeList.value = buildChargeCards(items);
    }
  } catch (err) {
    console.error('加载就诊详情失败:', err);
  } finally {
    detailLoading.value = false;
  }
};

watch(visible, (open) => {
  if (!open) return;
  emrDetail.value = null;
  chargeList.value = [];
  if (props.row) loadDetail(props.row);
});

const record = () => emrDetail.value?.record || null;
const prescriptions = () => emrDetail.value?.prescriptions || [];
const inspectionApplies = () => emrDetail.value?.inspectionApplies || [];
const laboratoryApplies = () => emrDetail.value?.laboratoryApplies || [];
const money = (v) => (v == null ? '0.00' : Number(v).toFixed(2));
// 四层账单行扁平列表 → 按账单归组成卡片（一次就诊通常一张账单，仍兼容多账单）
const buildChargeCards = (items) => {
  const map = new Map();
  for (const it of items || []) {
    const key = String(it.billId);
    if (!map.has(key)) {
      map.set(key, {id: it.billId, billNo: it.billNo, items: [], totalAmount: 0});
    }
    const card = map.get(key);
    card.items.push(it);
    card.totalAmount = (card.totalAmount || 0) + (it.amount || 0);
  }
  return Array.from(map.values());
};
</script>
