<template>
  <el-dialog v-model="visible" title="手工补记账（错漏费用的唯一录入口）" width="560px">
    <el-form label-width="110px">
      <el-form-item label="患者" required>
        <PatientSelect v-model="form.patientId" data-testid="fee-book-patient" @select="onPatientSelect"/>
      </el-form-item>
      <el-form-item label="就诊类型" required>
        <el-radio-group v-model="form.encounterType">
          <el-radio :value="1">门诊</el-radio>
          <el-radio :value="2">住院</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="就诊标识ID" required>
        <el-input v-model="form.encounterId" data-testid="fee-book-encounter" placeholder="挂号ID / 住院记录ID"/>
      </el-form-item>
      <el-form-item label="项目类型" required>
        <el-select v-model="form.itemType" data-testid="fee-book-item-type" placeholder="选择类型" style="width: 100%">
          <el-option v-for="o in dictOptions(DICT_TYPE.CHARGE_ITEM_TYPE)" :key="o.dictValue" :label="o.dictLabel"
                     :value="Number(o.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="项目名称" required>
        <el-input v-model="form.itemName" data-testid="fee-book-item-name"/>
      </el-form-item>
      <el-form-item label="单价 / 数量" required>
        <div class="flex items-center gap-2">
          <el-input-number v-model="form.price" :min="0" :precision="4" data-testid="fee-book-price"/>
          <el-input-number v-model="form.quantity" :min="0.01" :precision="2" data-testid="fee-book-quantity"/>
          <span class="text-sm text-slate-500">合计 ¥{{ formatMoney(amount) }}</span>
        </div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" maxlength="200" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="saving" data-testid="fee-book-submit" type="primary" @click="submit">记账</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * L1 手工补记账（错漏费用的唯一录入口）—— 从 views/fee-record/FeeRecordView.vue 拆出
 *
 * 只能新增、没有修改语义（记账行不可改，修正走红冲）；金额一律后端现算（单价×数量）。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {bookFee} from '@/api/feeRecord';
import {DICT_TYPE} from '@/lib/dict-cache';
import {formatMoney} from '@/lib/utils';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 项目类型等字典候选（页面已加载，禁止子组件重复拉取） */
  dictOptions: {type: Function, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  patientId: null,
  patientNo: '',
  patientName: '',
  encounterType: 1,
  encounterId: null,
  encounterNo: '',
  itemType: null,
  itemName: '',
  price: null,
  quantity: 1,
  unit: '',
  sourceType: 11,
  remark: '',
});
const amount = computed(() => {
  const p = Number(form.price || 0);
  const q = Number(form.quantity || 0);
  return Math.round(p * q * 100) / 100;
});

function reset() {
  form.patientId = null;
  form.patientNo = '';
  form.patientName = '';
  form.encounterType = 1;
  form.encounterId = null;
  form.encounterNo = '';
  form.itemType = null;
  form.itemName = '';
  form.price = null;
  form.quantity = 1;
  form.unit = '';
  form.remark = '';
}

function onPatientSelect(p) {
  form.patientId = p?.id ?? null;
  form.patientNo = p?.patientNo || '';
  form.patientName = p?.name || p?.patientName || '';
}

async function submit() {
  if (!form.patientId) {
    ElMessage.warning('请选择患者');
    return;
  }
  if (!form.encounterId) {
    ElMessage.warning('请填写就诊标识（挂号/住院记录ID）');
    return;
  }
  if (!form.itemType) {
    ElMessage.warning('请选择项目类型');
    return;
  }
  if (!form.itemName) {
    ElMessage.warning('请填写项目名称');
    return;
  }
  if (!(Number(form.price) > 0) || !(Number(form.quantity) > 0)) {
    ElMessage.warning('单价与数量必须大于 0');
    return;
  }
  saving.value = true;
  try {
    const res = await bookFee({...form});
    ElMessage.success(`已记账 ${res.data?.feeNo || ''}，金额 ¥${formatMoney(res.data?.amount)}`);
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e.message || '补记账失败');
  } finally {
    saving.value = false;
  }
}

watch(() => props.modelValue, (v) => {
  if (v) reset();
});
</script>
