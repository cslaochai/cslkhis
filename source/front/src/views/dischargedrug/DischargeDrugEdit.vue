<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑带药单' : '开带药单'" width="560px">
    <el-form label-width="90px">
      <el-form-item label="入院ID" required>
        <el-input v-model="form.admissionId" :disabled="!!form.id" placeholder="入院记录ID"/>
      </el-form-item>
      <el-form-item label="药品名称" required>
        <el-input v-model="form.drugName" placeholder="药品名称"/>
      </el-form-item>
      <el-form-item label="规格">
        <el-input v-model="form.spec" placeholder="如 0.25g*24片"/>
      </el-form-item>
      <el-form-item label="用法用量">
        <div class="flex gap-2 w-full">
          <el-input v-model="form.dosage" placeholder="每次剂量"/>
          <el-input v-model="form.usageText" placeholder="如 每日三次 饭后"/>
        </div>
      </el-form-item>
      <el-form-item label="带药数量" required>
        <div class="flex gap-2 w-full">
          <el-input-number v-model="form.quantity" :min="0.01" :precision="2" class="!w-40"/>
          <el-input v-model="form.unit" class="!w-32" placeholder="单位（盒/瓶）"/>
        </div>
      </el-form-item>
      <el-form-item label="用药天数">
        <el-input-number v-model="form.days" :min="1" :precision="0"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="['inpatient:dischargeDrug:add','inpatient:dischargeDrug:edit']" :loading="saving"
                 type="primary" @click="submitEdit">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 出院带药「开单 / 编辑」弹框（自 DischargeDrugView 拆出）
 *
 * row 为空 = 开带药单（入院ID 默认带入列表查询条件）；row 有值 = 编辑待发药单。
 * 表单状态、校验、提交全部在这里，保存成功后只向外 emit('saved') 让列表刷新。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {dischargeDrugUpsert} from '@/api/dischargeDrug';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 编辑行快照（null=开单）
  row: {type: Object, default: null},
  // 开单时默认带入的入院ID（来自列表查询条件）
  admissionId: {type: String, default: ''},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const form = reactive({
  id: null, admissionId: '', drugId: null, drugName: '',
  spec: '', dosage: '', unit: '', quantity: null, usageText: '', days: null, remark: '',
});

// 每次打开按 row 快照初始化：空行=开单（入院ID 带入列表条件），有行=编辑
watch(() => props.modelValue, (v) => {
  if (!v) return;
  const r = props.row;
  if (r) {
    Object.assign(form, {
      id: r.id, admissionId: r.admissionId, drugId: r.drugId, drugName: r.drugName,
      spec: r.spec, dosage: r.dosage, unit: r.unit, quantity: r.quantity,
      usageText: r.usageText, days: r.days, remark: r.remark,
    });
  } else {
    Object.assign(form, {
      id: null, admissionId: props.admissionId || '', drugId: null, drugName: '',
      spec: '', dosage: '', unit: '', quantity: null, usageText: '', days: null, remark: '',
    });
  }
});

const submitEdit = async () => {
  if (!form.admissionId || !form.drugName.trim() || !form.quantity) {
    ElMessage.warning('入院ID、药品名称、带药数量为必填');
    return;
  }
  saving.value = true;
  try {
    const res = await dischargeDrugUpsert({
      id: form.id ?? undefined,
      admissionId: form.admissionId,
      drugId: form.drugId ?? undefined,
      drugName: form.drugName.trim(),
      spec: form.spec || undefined,
      dosage: form.dosage || undefined,
      unit: form.unit || undefined,
      quantity: form.quantity,
      usageText: form.usageText || undefined,
      days: form.days ?? undefined,
      remark: form.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('带药单已保存');
      visible.value = false;
      emit('saved');
    } 
  } catch (e) {
    console.error(e);
    
  } finally {
    saving.value = false;
  }
};
</script>
