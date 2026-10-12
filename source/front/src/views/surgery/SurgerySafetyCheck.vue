<template>
  <!-- 三方安全核查（P134.2） -->
  <el-dialog v-model="visible" data-testid="p134-safety-dialog" title="手术安全核查（三方 · 三时段）"
             width="820px">
    <div v-if="safetyTarget" v-loading="safetyLoading" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <div class="text-slate-900">
          {{ text(safetyTarget.patientName) }}
          <el-tag v-if="safetyTarget.isEmergency === 1" class="ml-1" size="small" type="danger">急诊</el-tag>
        </div>
        <div class="text-[12px] text-slate-600">
          {{ text(safetyTarget.plannedOperationName) }} · {{ text(safetyTarget.operationRoom) }} ·
          {{ text(safetyTarget.plannedTimeText) }} · 主刀 {{ text(safetyTarget.surgeonName) }}
        </div>
      </div>
      <div class="text-[12px] leading-5 text-slate-500">
        法定三个时段<b>按顺序</b>签（麻醉实施前 → 手术开始前 → 患者离开手术室前），每轮都要
        <b>手术医师、麻醉医师、手术室护士三方不同人</b>签名；签过的时段<b>不可改不可删</b>。
        签过一轮的手术，三轮不签满不能登记手术完成。
      </div>
      <div class="grid grid-cols-3 gap-3">
        <div
            v-for="card in safetyCards"
            :key="card.phase"
            :class="card.signed
            ? 'border-emerald-200 bg-emerald-50'
            : (safetyForm.phase === card.phase ? 'border-blue-400 bg-white' : 'border-slate-200 bg-white')"
            :data-testid="`p134-safety-card-${card.phase}`"
            class="rounded-lg border p-3"
        >
          <div class="flex items-center justify-between">
            <span class="text-[13px] font-semibold text-slate-900">第{{ card.phase }}轮 · {{ card.phaseText }}</span>
            <el-tag v-if="card.signed" size="small" type="success">已签</el-tag>
          </div>
          <template v-if="card.signed">
            <div class="mt-1 text-[11px] leading-5 text-slate-600">{{ text(card.signed.itemsText) }}</div>
            <div class="mt-1 text-[11px] text-slate-600">
              手术 {{ card.signed.surgeonName || '—' }} · 麻醉 {{ card.signed.anesthetistName || '—' }} ·
              护士 {{ card.signed.nurseName || '—' }}
            </div>
            <div class="text-[11px] text-slate-400">{{ card.signed.checkNo }} · {{ fmt(card.signed.checkTime) }}</div>
            <div v-if="card.signed.note" class="mt-1 text-[11px] text-amber-700">异常：{{ card.signed.note }}</div>
          </template>
          <template v-else>
            <div v-if="!card.canSign" :data-testid="`p134-safety-reason-${card.phase}`"
                 class="mt-2 text-[11px] leading-5 text-slate-400">
              {{ card.cannotSignReason || '当前不可签' }}
            </div>
            <el-button
                v-else
                :data-testid="`p134-safety-pick-${card.phase}`"
                :type="safetyForm.phase === card.phase ? 'primary' : 'default'"
                class="mt-2"
                size="small"
                @click="pickSafetyPhase(card)"
            >
              {{ safetyForm.phase === card.phase ? '正在填写本轮' : '签这一轮' }}
            </el-button>
          </template>
        </div>
      </div>

      <div v-if="currentSafetyCard" class="rounded-lg border border-slate-200 p-3" data-testid="p134-safety-form">
        <div class="mb-2 text-[12px] text-slate-500">
          {{ currentSafetyCard.phaseText }} 核查要点，标 <span class="text-red-500">*</span> 为必核项
        </div>
        <el-checkbox-group v-model="safetyForm.items" class="flex flex-col gap-1">
          <el-checkbox
              v-for="i in currentSafetyCard.items"
              :key="i.code"
              :data-testid="`p134-safety-item-${i.code}`"
              :value="i.code"
          >
            <span class="text-[12px] text-slate-700">{{ i.label }}</span>
            <span v-if="i.required" class="ml-1 text-red-500">*</span>
          </el-checkbox>
        </el-checkbox-group>
        <el-form class="mt-3" label-width="110px">
          <el-form-item label="手术医师签名" required>
            <el-select v-model="safetyForm.surgeonId" class="!w-full" data-testid="p134-safety-surgeon" filterable
                       placeholder="本场手术医师">
              <el-option v-for="e in employees" :key="e.id"
                         :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                         :value="String(e.id)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="麻醉医师签名" required>
            <el-select v-model="safetyForm.anesthetistId" class="!w-full" data-testid="p134-safety-anesthetist" filterable
                       placeholder="在场麻醉医师">
              <el-option v-for="e in employees" :key="e.id"
                         :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                         :value="String(e.id)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="手术室护士签名" required>
            <el-select v-model="safetyForm.nurseId" class="!w-full" data-testid="p134-safety-nurse" filterable
                       placeholder="巡回/器械护士">
              <el-option v-for="e in employees" :key="e.id"
                         :label="`${e.empName || e.id}${e.deptName ? '（' + e.deptName + '）' : ''}`"
                         :value="String(e.id)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="异常说明">
            <el-input v-model="safetyForm.note" :rows="2" data-testid="p134-safety-note"
                      placeholder="发现风险/偏差必须写（如：电刀自检异常，已更换）" type="textarea"/>
          </el-form-item>
        </el-form>
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button
          v-if="safetyForm.phase"
          v-perm="['ipd:surgery:edit', 'ipd:anesthesia:edit']"
          :loading="safetySubmitting"
          data-testid="p134-safety-submit"
          type="primary"
          @click="submitSafety"
      >
        确认三方已在场并签核
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getSafetyCheckCards, signSafetyCheck} from '@/api/inpatientSafetyCheck';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前要签核的那一条手术申请
  row: {type: Object, default: null},
  employees: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const fmt = (v) => (v ? String(v).replace('T', ' ') : '—');
const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const safetyLoading = ref(false);
const safetySubmitting = ref(false);
const safetyTarget = computed(() => props.row);
const safetyCards = ref([]);
const safetyForm = reactive({
  phase: 0,
  items: [],
  surgeonId: '',
  anesthetistId: '',
  nurseId: '',
  note: '',
});
const loadSafetyCards = async (applyId) => {
  safetyLoading.value = true;
  try {
    const res = await getSafetyCheckCards(applyId);
    safetyCards.value = (res.data || []);
  } catch (error) {
    ElMessage.error(error.message || '加载安全核查单失败');
  } finally {
    safetyLoading.value = false;
  }
};

watch(visible, async (open) => {
  if (!open || !props.row) return;
  safetyForm.phase = 0;
  safetyForm.items = [];
  safetyForm.surgeonId = props.row.surgeonId || '';
  safetyForm.anesthetistId = '';
  safetyForm.nurseId = '';
  safetyForm.note = '';
  await loadSafetyCards(props.row.id);
});

const pickSafetyPhase = (card) => {
  safetyForm.phase = card.phase;
  safetyForm.items = [];
};
const currentSafetyCard = computed(() => safetyCards.value.find((c) => c.phase === safetyForm.phase));

const submitSafety = async () => {
  if (!safetyTarget.value || !safetyForm.phase) {
    ElMessage.warning('请先选择要签核的时段');
    return;
  }
  const card = currentSafetyCard.value;
  if (card) {
    const missing = card.items.filter((i) => i.required && !safetyForm.items.includes(i.code)).map((i) => i.label);
    if (missing.length > 0) {
      ElMessage.warning(`该时段必核项未完成：${missing.join('；')}`);
      return;
    }
  }
  if (!safetyForm.surgeonId || !safetyForm.anesthetistId || !safetyForm.nurseId) {
    ElMessage.warning('手术医师、麻醉医师、手术室护士三方都必须签名（核查的意义就是三方在场）');
    return;
  }
  const ids = [safetyForm.surgeonId, safetyForm.anesthetistId, safetyForm.nurseId];
  if (new Set(ids).size < 3) {
    ElMessage.warning('三方必须是三个不同的人（同一个人签三方是走形式，后端也会拒）');
    return;
  }
  safetySubmitting.value = true;
  try {
    await signSafetyCheck({
      applyId: safetyTarget.value.id,
      phase: safetyForm.phase,
      items: safetyForm.items.join(','),
      surgeonId: safetyForm.surgeonId,
      anesthetistId: safetyForm.anesthetistId,
      nurseId: safetyForm.nurseId,
      note: safetyForm.note.trim() || undefined,
    });
    ElMessage.success('该时段核查已签核（签过即不可改）');
    safetyForm.phase = 0;
    safetyForm.items = [];
    emit('saved');
    await loadSafetyCards(safetyTarget.value.id);
  } catch (error) {
    ElMessage.error(error.message || '核查签核失败');
  } finally {
    safetySubmitting.value = false;
  }
};
</script>
