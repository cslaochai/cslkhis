<template>
  <el-dialog
      v-model="visible"
      :title="isEdit ? '编辑科室' : '新增科室'"
      width="700px"
  >
    <el-form v-loading="detailLoading" ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="科室名称" prop="deptName">
          <el-input v-model="formData.deptName" placeholder="请输入科室名称"/>
        </el-form-item>
        <el-form-item label="上级科室">
          <el-tree-select
              v-model="formData.parentId"
              :data="deptTree"
              :props="{ label: 'deptName', value: 'id', children: 'children' }"
              :render-after-expand="false"
              check-strictly
              class="w-full"
              clearable
              placeholder="请选择上级科室（不选则为顶级）"
          />
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="科室类型" prop="deptType">
          <el-select v-model="formData.deptType" :fit-input-width="false" class="w-full" multiple
                     placeholder="请选择科室类型">
            <el-option v-for="d in deptTypeOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="formData.sortOrder" :min="0" class="w-full"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="位置">
          <el-input v-model="formData.location" placeholder="如：门诊楼A座3层"/>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="科室负责人">
          <el-select v-model="formData.deptLeaderId" :fit-input-width="false" class="w-full" clearable
                     filterable
                     placeholder="请选择科室负责人">
            <el-option v-for="e in employeeList" :key="e.id"
                       :label="`${e.empName}（${e.title || '无职称'}）`" :value="e.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>
      <el-form-item label="科室简介">
        <el-input v-model="formData.deptDesc" :rows="3" placeholder="请输入科室简介" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'org:dept:add'" :loading="submitLoading" type="primary" @click="handleSubmit">确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createDepartment, getDepartmentDetail, getEmployeeList, updateDepartment} from '@/api/system';
import {deptTypeToArray} from '@/lib/deptType';

// row 为空 = 新增，有值 = 修改（后端是同一个科室主档的 create/update，两侧字段与校验必须同一份）
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  deptTree: {type: Array, default: () => []},
  // 科室类型的唯一口径是字典 his_dept_type，由页面加载一次传下来（列表列同一份）
  deptTypeOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const formRef = ref(null);
const submitLoading = ref(false);
const detailLoading = ref(false);
const isEdit = computed(() => !!props.row?.id);

const formData = reactive({
  id: null,
  deptCode: '',
  deptName: '',
  deptType: [], // 多选，提交时 join(',')
  parentId: 0,
  sortOrder: 0,
  location: '',
  contactPhone: '',
  deptLeaderId: null,
  deptDesc: '',
  status: 1,
});
const rules = {
  deptName: [{required: true, message: '请输入科室名称', trigger: 'blur'}],
  deptType: [{required: true, message: '请选择科室类型', trigger: 'change'}],
};

const employeeList = ref([]);
const loadEmployeeList = async () => {
  try {
    const res = await getEmployeeList({});
    employeeList.value = res.data || [];
  } catch (error) {
    console.error('加载员工列表失败:', error);
  }
};

const resetForm = () => {
  formData.id = null;
  formData.deptCode = '';
  formData.deptName = '';
  formData.deptType = [];
  formData.parentId = 0;
  formData.sortOrder = 0;
  formData.location = '';
  formData.contactPhone = '';
  formData.deptLeaderId = null;
  formData.deptDesc = '';
  formData.status = 1;
};

watch(visible, async (open) => {
  if (!open) return;
  resetForm();
  formRef.value?.clearValidate();
  if (!employeeList.value.length) await loadEmployeeList();
  if (!props.row?.id) return;
  detailLoading.value = true;
  try {
    const res = await getDepartmentDetail(props.row.id);
    if (res.code === 200) {
      Object.assign(formData, {...res.data, deptType: deptTypeToArray(res.data.deptType)});
    }
  } catch (error) {
    ElMessage.error(error.message || '获取科室信息失败');
  } finally {
    detailLoading.value = false;
  }
});

const handleSubmit = async () => {
  if (!formRef.value)
    return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  submitLoading.value = true;
  try {
    // 提交前把数组转成逗号分隔字符串
    const payload = {...formData, deptType: formData.deptType.join(',')};
    const res = isEdit.value ? await updateDepartment(payload) : await createDepartment(payload);
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '操作失败');
    }
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  } finally {
    submitLoading.value = false;
  }
};
</script>
