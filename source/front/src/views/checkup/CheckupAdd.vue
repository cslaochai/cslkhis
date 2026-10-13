<template>
  <!-- 登记弹窗 -->
  <el-dialog v-model="visible" title="体检登记" width="480px">
    <el-form label-width="90px">
      <el-form-item label="体检人" required>
        <PatientSelect v-model="createForm.patientId"/>
      </el-form-item>
      <el-form-item label="对象类型">
        <el-radio-group v-model="createForm.personType">
          <el-radio :value="1">个人</el-radio>
          <el-radio :value="2">团体</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="套餐" required>
        <el-select v-model="createForm.packageId" :fit-input-width="false" data-testid="checkup-package-select"
                   placeholder="选择套餐" style="width: 100%">
          <el-option v-for="p in packages" :key="p.id" :label="`${p.packageName}（¥${p.price}）`"
                     :value="String(p.id)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="体检日期" required>
        <el-date-picker v-model="createForm.checkupDate" style="width: 100%" type="date"
                        value-format="YYYY-MM-DD"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="createForm.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'checkup:manage:add'" data-testid="checkup-create-submit" type="primary"
                 @click="submitCreate">登记
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 体检登记弹窗（仅新增：1 已登记为初始态，无修改语义，故为 xxxAdd 形态）
 * 套餐下拉只有本表单使用，弹框打开时懒加载。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {checkupPackageListPage, checkupRecordCreate} from '@/api/checkup';
import PatientSelect from '@/components/his/PatientSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const createForm = reactive({patientId: null, personType: 1, packageId: null, checkupDate: '', remark: ''});
const packages = ref([]);
const loadPackages = async () => {
  try {
    const res = await checkupPackageListPage({status: 1, pageNum: 1, pageSize: 100});
    packages.value = res?.data?.records || [];
  } catch (e) {
    console.error('加载套餐失败', e);
  }
};

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return;
    createForm.patientId = null;
    createForm.personType = 1;
    createForm.packageId = null;
    createForm.checkupDate = '';
    createForm.remark = '';
    loadPackages();
  }
);

const submitCreate = async () => {
  if (!createForm.patientId)
    return ElMessage.warning('请选择体检人');
  if (!createForm.packageId)
    return ElMessage.warning('请选择套餐');
  if (!createForm.checkupDate)
    return ElMessage.warning('请选择体检日期');
  try {
    await checkupRecordCreate({
      patientId: createForm.patientId, personType: createForm.personType,
      packageId: createForm.packageId, checkupDate: createForm.checkupDate, remark: createForm.remark,
    });
    ElMessage.success('登记成功');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '登记失败');
  }
};
</script>
