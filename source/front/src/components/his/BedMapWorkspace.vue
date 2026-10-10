<template>
  <div
      ref="rootRef"
      :style="workspaceH ? { height: `${workspaceH}px` } : undefined"
      class="flex flex-col gap-3"
      data-testid="bed-map"
  >
    <!-- 工具条 -->
    <div class="flex flex-wrap items-center gap-3 shrink-0">
      <!-- 不受限账号的下拉是「所有有床位的科室」，几十条起步，不能靠翻页找 -->
      <el-select v-model="deptId" class="!w-52" data-testid="bed-map-dept" filterable placeholder="科室">
        <el-option
            v-for="d in deptOptions"
            :key="d.deptId"
            :label="`${d.deptName}（${d.occupied}/${d.total}）`"
            :value="String(d.deptId)"
        />
      </el-select>
      <el-select v-model="wardId" class="!w-52" data-testid="bed-map-ward" placeholder="病区">
        <el-option label="全部病区" value=""/>
        <el-option
            v-for="w in wardOptions"
            :key="w.wardId"
            :label="`${w.wardName}（${w.occupied}/${w.total}）`"
            :value="String(w.wardId)"
        />
      </el-select>
      <el-button :loading="loading" data-testid="bed-map-refresh" @click="load">
        <el-icon class="mr-1">
          <Refresh/>
        </el-icon>
        刷新
      </el-button>
      <div class="ml-auto flex flex-wrap items-center gap-3 text-xs text-slate-600">
        <span v-for="lv in [[1, '特级'], [2, '一级'], [3, '二级'], [4, '三级']]" :key="lv[0]"
              class="flex items-center gap-1">
          <i :style="{ background: NURSING_COLORS[lv[0] as number] }"
             class="inline-block w-2.5 h-4 rounded-sm"></i>{{ lv[1] }}护理
        </span>
        <span class="flex items-center gap-1">
          <i :style="{ background: UNKNOWN_NURSING_COLOR }" class="inline-block w-2.5 h-4 rounded-sm"></i>未评估
        </span>
      </div>
    </div>

    <!-- 统计条 -->
    <div class="grid shrink-0 grid-cols-4 md:grid-cols-8 gap-2" data-testid="bed-map-stats">
      <div class="rounded-lg border border-slate-200 bg-white px-3 py-2">
        <div class="text-xs text-slate-500">床位总数</div>
        <div class="text-xl font-semibold text-slate-800">{{ summary.totalBeds ?? 0 }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white px-3 py-2">
        <div class="text-xs text-slate-500">占用</div>
        <div class="text-xl font-semibold text-[#1269B5]">{{ summary.occupied ?? 0 }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white px-3 py-2">
        <div class="text-xs text-slate-500">空闲</div>
        <div class="text-xl font-semibold text-slate-800">{{ summary.free ?? 0 }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white px-3 py-2">
        <div class="text-xs text-slate-500">维修 / 锁定</div>
        <div class="text-xl font-semibold text-slate-800">{{ summary.repair ?? 0 }} / {{ summary.locked ?? 0 }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white px-3 py-2">
        <div class="text-xs text-slate-500">使用率</div>
        <div class="text-xl font-semibold text-[#0E9488]">{{ summary.usageRate ?? 0 }}%</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white px-3 py-2">
        <div class="text-xs text-slate-500">今日新入</div>
        <div class="text-xl font-semibold text-slate-800">{{ summary.newToday ?? 0 }}</div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white px-3 py-2">
        <div class="text-xs text-slate-500">术后 / 危重</div>
        <div class="text-xl font-semibold text-slate-800">{{ summary.postOpCount ?? 0 }} / {{
            summary.criticalCount ?? 0
          }}
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white px-3 py-2">
        <div class="text-xs text-slate-500">过敏 / 未评估</div>
        <div class="text-xl font-semibold text-slate-800">{{ summary.allergyCount ?? 0 }} / {{
            summary.levelUnknown ?? 0
          }}
        </div>
      </div>
    </div>

    <!-- 卡片区：整页不滚，病区与床位在面板内滚 -->
    <div class="min-h-0 flex-1 overflow-y-auto rounded-xl border border-slate-200 bg-slate-50/60 p-3">
      <div v-if="!beds.length && !loading" class="py-16 text-center text-sm text-slate-500">
        该科室暂无床位数据
      </div>
      <section v-for="g in groups" :key="g.wardKey" class="mb-4 last:mb-0">
        <h3 class="mb-2 flex items-center gap-2 text-sm font-semibold text-slate-700">
          {{ g.wardName }}
          <span class="text-xs font-normal text-slate-500">占 {{ g.occupied }} / 共 {{ g.total }} 床</span>
        </h3>
        <div class="grid gap-2" style="grid-template-columns: repeat(auto-fill, minmax(168px, 1fr))">
          <div
              v-for="bed in g.beds"
              :key="bed.bedId"
              :class="bedClass(bed)"
              :style="{ borderLeft: `4px solid ${nursingColor(bed)}` }"
              :title="bed.bedStatus === 2 ? bed.allergyHistory || '' : bed.bedStatusText"
              class="bed-card rounded-lg border p-2 transition-colors"
              data-testid="bed-card"
              @click="openPatient(bed)"
          >
            <div class="flex items-baseline justify-between">
              <span class="text-lg font-semibold text-slate-800">{{ bed.bedNo }}</span>
              <span :class="bed.bedType === 'normal' ? 'text-slate-500' : 'bg-[#1269B5]/10 text-[#1269B5]'"
                    class="rounded px-1 text-[11px]">
                {{ bedTypeText(bed.bedType) }}
              </span>
            </div>

            <template v-if="bed.bedStatus === 2">
              <div class="mt-1 truncate text-[15px] font-semibold text-slate-900" data-testid="bed-card-name">
                {{ bed.patientName || '未登记姓名' }}
              </div>
              <div class="text-xs text-slate-600">
                {{ patientGenderText(bed.gender) }} · {{ patientAgeText(bed.age) }} · 入院{{ bed.admitDays ?? 0 }}天
              </div>
              <div class="truncate text-xs text-slate-600">主治 {{ bed.doctorName || '未指定' }}</div>
              <div class="mt-1 flex flex-wrap gap-1 text-[11px] leading-4">
                <span class="rounded bg-slate-100 px-1 text-slate-700">{{ bed.nursingLevelText }}</span>
                <span
                    v-if="bed.allergyHistory"
                    class="flex items-center gap-0.5 rounded bg-red-50 px-1 text-red-700"
                    data-testid="bed-card-allergy"
                >
                  <el-icon class="text-[11px]"><WarningFilled/></el-icon>过敏
                </span>
                <span v-if="bed.postOpDays !== null && bed.postOpDays !== undefined"
                      class="rounded bg-[#0E9488]/10 px-1 text-[#0B6B63]">
                  术后{{ bed.postOpDays }}天
                </span>
                <span v-if="bed.critical" class="rounded bg-red-600 px-1 text-white">危重</span>
                <span
                    v-if="alertOf(bed)"
                    :class="alertOf(bed).alertLevel === 2 ? 'bg-red-600 text-white' : 'bg-amber-100 text-amber-800'"
                    class="cursor-pointer rounded px-1 font-medium"
                    data-testid="bed-card-mews"
                    title="点击查看危重预警详情"
                    @click.stop="openAlert(alertOf(bed))"
                >MEWS {{ alertOf(bed).totalScore }}</span>
                <span v-if="bed.newToday" class="rounded bg-[#1269B5] px-1 text-white">新入</span>
                <span v-if="bed.activeOrderCount"
                      class="rounded bg-slate-100 px-1 text-slate-700">医嘱{{ bed.activeOrderCount }}</span>
              </div>
            </template>
            <div v-else class="mt-1 text-sm text-slate-500">{{ bed.bedStatusText }}</div>
          </div>
        </div>
      </section>
    </div>

    <PatientDetailDialog v-model="detailVisible" :patient-id="detailPatientId"/>

    <!-- 危重预警详情：只读信息弹框，点遮罩可关 -->
    <el-dialog v-model="alertVisible" data-testid="mews-dialog" title="危重预警详情" width="560px">
      <div v-loading="alertLoading">
        <template v-if="alertDetail">
          <div class="flex flex-wrap items-baseline justify-between gap-2">
            <div class="text-base font-semibold text-slate-900">
              {{ alertDetail.bedNo }}床 {{ alertDetail.patientName }}
              <span class="ml-1 text-sm font-normal text-slate-500">{{ alertDetail.wardName }}</span>
            </div>
            <span
                :class="alertDetail.alertLevel === 2 ? 'bg-red-600 text-white' : 'bg-amber-100 text-amber-800'"
                class="rounded px-2 py-0.5 text-sm font-semibold"
                data-testid="mews-level"
            >{{ alertDetail.alertText }}（{{ alertDetail.totalScore }} 分）</span>
          </div>
          <div class="mt-1 text-xs text-slate-500">体征时点：{{ alertDetail.measureTime }}</div>

          <el-alert
              v-if="alertDetail.degraded"
              :closable="false"
              :title="`模型观察建议本次不可用：${alertDetail.degradeReason || '模型未返回'}。评分与体征事实照常，建议为空。`"
              class="mt-3"
              data-testid="mews-degraded"
              show-icon
              type="warning"
          />

          <h4 class="mt-4 mb-1 text-sm font-semibold text-slate-700">评分明细（MEWS+SpO2）</h4>
          <table class="w-full text-sm" data-testid="mews-items">
            <thead>
            <tr class="border-b border-slate-200 text-xs text-slate-500">
              <th class="py-1 text-left font-normal">项目</th>
              <th class="py-1 text-center font-normal">测量值</th>
              <th class="py-1 text-center font-normal">得分</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="it in alertDetail.items || []" :key="it.name" class="border-b border-slate-100">
              <td class="py-1">{{ it.name }}</td>
              <td class="py-1 text-center">{{ it.valueText }}</td>
              <td :class="it.score > 0 ? 'text-red-600' : 'text-slate-500'" class="py-1 text-center font-semibold">
                {{ it.score }}
              </td>
            </tr>
            </tbody>
          </table>

          <template v-if="(alertDetail.triggeredFacts || []).length">
            <h4 class="mt-4 mb-1 text-sm font-semibold text-slate-700">触发依据</h4>
            <ul class="list-disc pl-5 text-sm text-slate-700">
              <li v-for="(f, i) in alertDetail.triggeredFacts" :key="i">{{ f }}</li>
            </ul>
          </template>

          <h4 class="mt-4 mb-1 text-sm font-semibold text-slate-700">观察建议</h4>
          <div v-if="alertDetail.advice" class="rounded-lg bg-slate-50 p-3 text-sm text-slate-700"
               data-testid="mews-advice">
            {{ alertDetail.advice }}
            <div class="mt-1 text-xs text-slate-400">观察建议由模型生成，仅供参考，不构成医嘱；处置由医护决定。</div>
          </div>
          <div v-else class="text-sm text-slate-500" data-testid="mews-no-advice">
            {{ alertDetail.degraded ? '本次没有可用的模型建议。' : '总分未达观察阈值，暂无模型建议。' }}
          </div>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, nextTick, onBeforeUnmount, onMounted, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Refresh, WarningFilled} from '@element-plus/icons-vue';
import {getBedMap} from '@/api/inpatient';
import {explainDeterioration, scanWardDeterioration} from '@/api/ai';
import {patientAgeText, patientGenderText} from '@/lib/patientGender';
import PatientDetailDialog from '@/components/his/PatientDetailDialog.vue';

const loading = ref(false);
const rootRef = ref(null);
const workspaceH = ref(0);
const deptId = ref('');
const wardId = ref('');
const data = ref({summary: {}, beds: [], deptOptions: [], wardOptions: []});
const measureWorkspace = () => {
  const el = rootRef.value;
  if (!el)
    return;
  // 24 = main 的下内边距（p-6）；480 兜底：视口太矮时宁可整页滚，也不把卡片压成一条缝
  workspaceH.value = Math.max(480, window.innerHeight - el.getBoundingClientRect().top - 24);
};
const load = async () => {
  loading.value = true;
  try {
    const res = await getBedMap({
      deptId: deptId.value || undefined,
      wardId: wardId.value || undefined,
    });
    if (res.code !== 200) {
      ElMessage.error(res.message || '床位图加载失败');
      return;
    }
    data.value = res.data || data.value;
    // 首屏后端会把自己落在主岗位科室，回填到下拉，否则切走就切不回来
    if (!deptId.value && res.data?.deptId)
      deptId.value = String(res.data.deptId);
    await loadAlerts();
  } catch (e) {
    ElMessage.error(e?.message || '床位图加载失败');
  } finally {
    loading.value = false;
  }
};
/** G-12 危重预警角标：按床位图里出现的病区逐个扫描（纯代码评分，无模型调用）。
 * 扫描失败不影响床位图本身 —— 角标缺席不等于「无预警」，床位图必须先能用。 */
const alertMap = ref({});
const loadAlerts = async () => {
  alertMap.value = {};
  const wardIds = new Set(beds.value.filter((b) => b.bedStatus === 2 && b.wardId != null).map((b) => String(b.wardId)));
  if (!wardIds.size)
    return;
  const results = await Promise.allSettled([...wardIds].map((id) => scanWardDeterioration({wardId: id})));
  const map = {};
  for (const r of results) {
    if (r.status !== 'fulfilled' || r.value?.code !== 200)
      continue;
    for (const row of r.value.data || []) {
      if (row?.admissionId != null && row.alertLevel >= 1)
        map[String(row.admissionId)] = row;
    }
  }
  alertMap.value = map;
};
const alertOf = (bed) => (bed.bedStatus === 2 && bed.admissionId != null ? alertMap.value[String(bed.admissionId)] : null);
watch(deptId, () => {
  wardId.value = '';
  load();
});
watch(wardId, () => load());
const beds = computed(() => data.value?.beds || []);
const summary = computed(() => data.value?.summary || {});
const deptOptions = computed(() => data.value?.deptOptions || []);
const wardOptions = computed(() => data.value?.wardOptions || []);
/** 按病区分组：同科多病区时混排看不出「哪张床在哪个病区」 */
const groups = computed(() => {
  const map = new Map();
  for (const bed of beds.value) {
    const key = String(bed.wardId ?? '0');
    if (!map.has(key))
      map.set(key, []);
    map.get(key).push(bed);
  }
  return [...map.entries()].map(([wardKey, list]) => ({
    wardKey,
    wardName: list[0]?.wardName || '未分配病区',
    occupied: list.filter((b) => b.bedStatus === 2).length,
    total: list.length,
    beds: list,
  }));
});
const NURSING_COLORS = {
  1: '#DC2626',
  2: '#EA580C',
  3: '#0E9488',
  4: '#1269B5',
};
const UNKNOWN_NURSING_COLOR = '#94A3B8';
const nursingColor = (bed) => bed.bedStatus !== 2 ? UNKNOWN_NURSING_COLOR : NURSING_COLORS[bed.nursingLevel] || UNKNOWN_NURSING_COLOR;
const bedClass = (bed) => {
  if (bed.bedStatus === 2)
    return 'bg-white border-slate-200 hover:border-[#1269B5] cursor-pointer';
  if (bed.bedStatus === 0)
    return 'bg-amber-50 border-amber-200';
  if (bed.bedStatus === 3)
    return 'bg-slate-100 border-slate-300';
  return 'bg-slate-50/70 border-dashed border-slate-300';
};
const bedTypeText = (type) => {
  if (type === 'ICU')
    return '重症';
  if (type === 'VIP')
    return '特需';
  if (type === 'normal')
    return '普通';
  return type || '普通';
};
const detailVisible = ref(false);
const detailPatientId = ref('');
const openPatient = (bed) => {
  if (bed.bedStatus !== 2 || !bed.patientId)
    return;
  detailPatientId.value = String(bed.patientId);
  detailVisible.value = true;
};
/** 预警详情：评分是代码事实先展示，advice 由 /explain 现拉（仅预警级≥1 才有模型参与）。 */
const alertVisible = ref(false);
const alertDetail = ref(null);
const alertLoading = ref(false);
const openAlert = async (row) => {
  alertVisible.value = true;
  alertDetail.value = row;
  alertLoading.value = true;
  try {
    const res = await explainDeterioration({admissionId: row.admissionId});
    if (res.code !== 200) {
      ElMessage.error(res.message || '预警详情加载失败');
      alertVisible.value = false;
      return;
    }
    alertDetail.value = res.data;
  } catch (e) {
    ElMessage.error(e?.message || '预警详情加载失败');
    alertVisible.value = false;
  } finally {
    alertLoading.value = false;
  }
};
onMounted(async () => {
  await nextTick();
  measureWorkspace();
  window.addEventListener('resize', measureWorkspace);
  await load();
  // 下拉首屏为空时高度还没最终稳定，测量一次即可
  await nextTick();
  measureWorkspace();
});
onBeforeUnmount(() => window.removeEventListener('resize', measureWorkspace));
</script>

<style scoped>
.bed-card {
  min-height: 104px;
}
</style>
