<template>
  <el-dialog v-model="visible" data-testid="p4-tf-cross-dialog" title="配血（血型鉴定 + 交叉配血）" width="1080px">
    <div v-if="row" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm">
        <div class="text-slate-900">
          {{ text(row.patientName) }}（{{ text(row.admissionNo) }}）
          <el-tag v-if="row.isEmergency === 1" class="ml-2" size="small" type="danger">紧急</el-tag>
        </div>
        <div class="mt-1 text-slate-700">
          受血者 {{ text(row.bloodTypeText) }} ·
          {{ text(row.bloodComponentText) }}（{{ text(row.componentSpec) }}）·
          申请 {{ text(row.bagCount) }} 袋
        </div>
        <div class="text-[12px] text-slate-500">输血指征：{{ text(row.indication) }}</div>
      </div>
      <el-alert :closable="false" show-icon type="warning">
        红细胞类按红细胞规则判 ABO 相容（A←A/O、B←B/O、AB←AB/A/B/O、O←O），
        血浆类方向相反（A←A/AB、B←B/AB、AB←AB、O←全）；受血者 Rh 阴性时血袋必须 Rh 阴性。
        不相容会被后端<b>直接拒绝</b>（ABO 不相容输注是致死性差错）。
        血袋号全局唯一：一袋血只能给一个人。
      </el-alert>
      <div class="flex items-center justify-between">
        <span class="text-[12px] text-slate-500">
          本次录入 {{ bagRows.length }} 袋；已配 {{ row.matchedBagCount || 0 }}/{{ row.bagCount }} 袋。
          可分多批提交（血站分批到货是常态），累计不得超过申请袋数。
        </span>
        <el-button v-perm="'medtech:transfusion:add'" :icon="Plus" size="small" @click="addBagRow">增加一袋
        </el-button>
      </div>
      <div v-for="(b, idx) in bagRows" :key="idx" class="rounded border border-slate-200 p-3">
        <div class="mb-2 flex items-center justify-between">
          <span class="text-[13px] font-medium text-slate-700">第 {{ idx + 1 }} 袋</span>
          <el-button v-perm="'medtech:transfusion:delete'" :icon="Delete" link type="danger"
                     @click="removeBagRow(idx)">删除
          </el-button>
        </div>
        <div class="grid grid-cols-2 gap-3 lg:grid-cols-4">
          <el-input v-model="b.bagNo" :data-testid="`p4-tf-bag-no-${idx}`" placeholder="血袋号 *"/>
          <el-input v-model="b.donorNo" placeholder="献血编号"/>
          <el-select v-model="b.bagAbo" :data-testid="`p4-tf-bag-abo-${idx}`" placeholder="血袋 ABO *">
            <el-option v-for="t in aboTypes" :key="t" :label="`${t} 型`" :value="t"/>
          </el-select>
          <el-select v-model="b.bagRh" :data-testid="`p4-tf-bag-rh-${idx}`" placeholder="血袋 Rh *">
            <el-option v-for="t in rhTypes" :key="t" :label="`Rh ${t}性`" :value="t"/>
          </el-select>
          <el-input v-model="b.spec" placeholder="规格"/>
          <el-input v-model.number="b.amount" placeholder="血量"/>
          <el-input v-model="b.amountUnit" placeholder="单位（U/ml/治疗量）"/>
          <el-input v-model="b.sourceBank" placeholder="来源血站"/>
          <el-date-picker
              v-model="b.collectDate"
              class="!w-full"
              placeholder="采集日期"
              type="date"
              value-format="YYYY-MM-DD"
          />
          <el-date-picker
              v-model="b.expireDate"
              :data-testid="`p4-tf-bag-expire-${idx}`"
              class="!w-full"
              placeholder="有效期至 *"
              type="date"
              value-format="YYYY-MM-DD"
          />
          <el-select v-model="b.crossmatchMain" placeholder="主侧">
            <el-option label="阴性（相合）" value="阴性"/>
            <el-option label="阳性（不合）" value="阳性"/>
          </el-select>
          <el-select v-model="b.crossmatchSide" placeholder="次侧">
            <el-option label="阴性（相合）" value="阴性"/>
            <el-option label="阳性（不合）" value="阳性"/>
          </el-select>
          <el-select v-model="b.crossmatchResult" :data-testid="`p4-tf-bag-result-${idx}`" placeholder="配血结论 *">
            <el-option :value="1" label="相合"/>
            <el-option :value="2" label="不合"/>
          </el-select>
          <el-input v-model="b.remark" placeholder="备注"/>
        </div>
      </div>
      <el-form label-width="90px">
        <el-form-item label="配血备注">
          <el-input
              v-model="crossNote"
              :rows="2"
              data-testid="p4-tf-cross-note"
              placeholder="可空；有配血不合时必须写明原因与后续处理"
              type="textarea"
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button
          v-perm="'medtech:transfusion:edit'"
          :loading="crossSubmitting"
          data-testid="p4-tf-cross-submit"
          type="primary"
          @click="submitCrossmatch"
      >
        提交配血结果
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// ---------------- 二、配血（逐袋） ----------------
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Delete, Plus} from '@element-plus/icons-vue';
import {crossmatchTransfusion} from '@/api/inpatientTransfusion';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  aboTypes: {type: Array, default: () => []},
  rhTypes: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const text = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));

const crossSubmitting = ref(false);
const crossNote = ref('');
const bagRows = ref([]);
const blankBag = () => ({
  bagNo: '',
  donorNo: '',
  bagAbo: '',
  bagRh: '',
  spec: props.row?.componentSpec || '',
  amount: undefined,
  amountUnit: props.row?.amountUnit || '',
  sourceBank: '',
  collectDate: '',
  expireDate: '',
  crossmatchMain: '阴性',
  crossmatchSide: '阴性',
  crossmatchResult: 1,
  remark: '',
});

watch(visible, (open) => {
  if (!open) return;
  crossNote.value = '';
  // 按申请袋数预生成行：少配一袋后端会拒（"未配齐不可发血"），
  // 但重复配同一袋后端会 update，所以预生成行不影响重配场景
  const need = Math.max(1, props.row?.bagCount || 1);
  bagRows.value = Array.from({length: need}, () => blankBag());
});

const addBagRow = () => {
  bagRows.value.push(blankBag());
};
const removeBagRow = (idx) => {
  if (bagRows.value.length <= 1) {
    ElMessage.warning('至少保留一袋');
    return;
  }
  bagRows.value.splice(idx, 1);
};
const submitCrossmatch = async () => {
  if (!props.row)
    return;
  for (let i = 0; i < bagRows.value.length; i++) {
    const b = bagRows.value[i];
    if (!b.bagNo || !String(b.bagNo).trim()) {
      ElMessage.warning(`第 ${i + 1} 袋：血袋号不能为空`);
      return;
    }
    if (!b.bagAbo) {
      ElMessage.warning(`第 ${i + 1} 袋：请选择血袋 ABO 血型`);
      return;
    }
    if (!b.bagRh) {
      ElMessage.warning(`第 ${i + 1} 袋：请选择血袋 Rh 血型`);
      return;
    }
    if (!b.expireDate) {
      ElMessage.warning(`第 ${i + 1} 袋：请填写有效期（超期血袋不得输注）`);
      return;
    }
    if (!b.crossmatchResult) {
      ElMessage.warning(`第 ${i + 1} 袋：请选择配血结论`);
      return;
    }
  }
  crossSubmitting.value = true;
  try {
    const res = await crossmatchTransfusion({
      applyId: props.row.id,
      crossmatchNote: crossNote.value.trim() || undefined,
      bags: bagRows.value.map((b) => ({
        bagNo: String(b.bagNo).trim(),
        donorNo: b.donorNo?.trim() || undefined,
        bagAbo: b.bagAbo,
        bagRh: b.bagRh,
        spec: b.spec?.trim() || undefined,
        amount: b.amount,
        amountUnit: b.amountUnit?.trim() || undefined,
        sourceBank: b.sourceBank?.trim() || undefined,
        collectDate: b.collectDate || undefined,
        expireDate: b.expireDate || undefined,
        crossmatchMain: b.crossmatchMain || undefined,
        crossmatchSide: b.crossmatchSide || undefined,
        crossmatchResult: b.crossmatchResult,
        remark: b.remark?.trim() || undefined,
      })),
    });
    // 配血结论在**响应 message**里（不是 data）：后端 crossmatch 返回的是"结果说明"
    // —— 全部相合 / N 袋不合（仍是 200）/ 只配了 x/n 袋。读 res.data 会拿到 null，
    // 于是所有配血结果都显示成兜底文案，护士看不出到底配成没有。
    const msg = String(res.message || '配血结果已录入');
    if (msg.includes('不合')) {
      ElMessage.warning(msg);
    } else {
      ElMessage.success(msg);
    }
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '配血失败');
  } finally {
    crossSubmitting.value = false;
  }
};
</script>
