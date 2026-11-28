<template>
  <!-- 临床科室发起器械包回收申请（0-待回收，等供应室回收确认） -->
  <el-dialog v-model="visible" title="申请回收器械包" width="520px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="器械包名称" prop="packName">
        <el-select v-model="form.packName" :fit-input-width="false" allow-create clearable default-first-option
                   filterable placeholder="选常用器械包，或直接输入名称" style="width: 260px"
                   @change="onPackNameChange">
          <el-option v-for="t in templateOptions" :key="t.id" :label="t.packName" :value="t.packName"/>
        </el-select>
      </el-form-item>
      <el-form-item label="灭菌方式" prop="sterilizeMethod">
        <el-select v-model="form.sterilizeMethod" :fit-input-width="false" placeholder="请选择" style="width: 200px">
          <el-option v-for="d in methodDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="申领科室" prop="deptId">
        <DeptTreeSelect v-model="form.deptId" mode="LEAF" placeholder="默认当前科室" width="260px"
                        @change="onDeptChange"/>
      </el-form-item>      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" maxlength="200" placeholder="如手术台次、污染程度" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submit">提交申请</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
// 器械包回收申请：科室端唯一入口。提交后包落 0-待回收，供应室在「器械包追溯」页做回收确认。
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {cssdApply, cssdTemplateSelectList} from '@/api/cssd';
import DeptTreeSelect from '@/components/his/DeptTreeSelect.vue';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  methodDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const formRef = ref(null);
const templateOptions = ref([]);
const form = reactive({packName: '', sterilizeMethod: null, deptId: null, deptName: '', remark: ''});
const rules = {
  packName: [{required: true, message: '请选择或输入器械包名称', trigger: 'change'}],
  sterilizeMethod: [{required: true, message: '请选择灭菌方式', trigger: 'change'}],
};

const loadTemplates = async () => {
  try {
    const res = await cssdTemplateSelectList();
    templateOptions.value = res?.data || [];
  } catch (e) {
    console.error('加载器械包模板失败', e);
  }
};
// 选模板包名后带出模板默认灭菌方式
const onPackNameChange = (name) => {
  const t = templateOptions.value.find((x) => x.packName === name);
  if (t?.sterilizeMethod) form.sterilizeMethod = Number(t.sterilizeMethod);
};
// DeptTreeSelect 自己加载科室树（选项 id 是字符串），这里只用本地缓存补科室名称快照
const onDeptChange = () => {
  form.deptName = localStorage.getItem('deptName') || '';
};

// 打开时回填当前岗位科室：必须传字符串，与科室下拉的 option value 同型才能显示名称而非裸 id
watch(visible, (open) => {
  if (!open) return;
  const deptId = localStorage.getItem('deptId');
  Object.assign(form, {
    packName: '', sterilizeMethod: null,
    deptId: deptId || null, deptName: localStorage.getItem('deptName') || '', remark: '',
  });
  formRef.value?.clearValidate();
  if (!templateOptions.value.length) loadTemplates();
});

const submit = async () => {
  await formRef.value?.validate();
  try {
    const res = await cssdApply({
      packName: form.packName.trim(),
      sterilizeMethod: form.sterilizeMethod,
      deptId: form.deptId || undefined,
      deptName: form.deptName.trim() || undefined,
      remark: form.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('申请已提交，等待供应室回收');
      visible.value = false;
      emit('saved');
    }
  } catch (e) {
    console.error('提交回收申请失败', e);
  }
};
</script>
