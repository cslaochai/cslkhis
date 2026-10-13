<template>
  <el-dialog v-model="visible" :title="title" data-testid="ta-edit-dialog" width="680px">
    <el-form :model="form" label-width="96px">
      <el-form-item label="被授权人" required>
        <el-select v-model="form.employeeId" :disabled="!!form.id" :fit-input-width="false" class="!w-full"
                   filterable placeholder="搜索并选择院内员工">
          <el-option v-for="e in employeeOptions" :key="e.id" :label="e.label" :value="e.id"/>
        </el-select>
      </el-form-item>
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="授权类别" required>
          <el-select v-model="form.authCategory" :fit-input-width="false" class="!w-full">
            <el-option v-for="d in props.categoryOptions" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="级别上限" required>
          <el-select v-model="form.techLevel" class="!w-full">
            <el-option v-for="d in props.levelOptions" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="授权方式">
          <el-select v-model="form.authType" :fit-input-width="false" class="!w-full">
            <el-option v-for="d in props.authTypeOptions" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="限定术式">
          <el-input v-model="form.itemScope" placeholder="术式编码，逗号分隔；留空=不限"/>
        </el-form-item>
        <el-form-item label="生效日期" required>
          <el-date-picker v-model="form.validFrom" :clearable="false" class="!w-full" placeholder="yyyy-MM-dd"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="有效期至">
          <el-date-picker v-model="form.validUntil" :disabled="indefinite" class="!w-full" placeholder="yyyy-MM-dd"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
      </div>
      <el-checkbox v-model="indefinite" class="mb-3">长期有效（不过期）</el-checkbox>
      <el-form-item label="授权依据">
        <el-input v-model="form.authBasis" :rows="2" placeholder="技术准入评价结论 / 培训考核 / 累计操作例数等，审批时填即可"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="saving" type="primary" @click="handleSave">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
/**
 * 登记 / 修改技术授权（后端同一个 techAuthUpsert，row 为空走登记）。
 * 授权只能授给院内在职员工，所以名册随本表单打开时才拉一次。
 */
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {techAuthUpsert} from '@/api/employeeTechAuth';
import {getEmployeeList} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示登记
  row: {type: Object, default: null},
  // 授权类别字典：列表筛选也要用，由页面加载一次下传
  categoryOptions: {type: Array, default: () => []},
  // 级别上限（1~4 级手术）：列表筛选也要用，由页面下传
  levelOptions: {type: Array, default: () => []},
  // 授权方式字典：只有本表单在下拉里选，仍与类别/状态同一次字典请求带回，由页面下传
  authTypeOptions: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const title = computed(() => (props.row ? `修改授权 - ${props.row.employeeName}` : '登记技术授权'));

// ========== 医生（员工）下拉：授权只能授给院内员工 ==========
const employees = ref([]);
const employeeOptions = computed(() => employees.value.filter((e) => e.status === 1).map((e) => ({
  id: String(e.id),
  label: `${e.empName}　${e.deptName || ''}　${e.title || ''}`,
})));
let employeesLoaded = false;
const loadEmployees = async () => {
  if (employeesLoaded) return;
  employeesLoaded = true;
  try {
    const res = await getEmployeeList({status: 1});
    employees.value = res.data?.records || res.data || [];
  } catch (e) {
    employeesLoaded = false;
    console.error('加载员工列表失败:', e);
  }
};

const saving = ref(false);
const emptyForm = () => ({
  id: null,
  employeeId: '',
  authCategory: 1,
  techLevel: 2,
  authType: 1,
  itemScope: '',
  authBasis: '',
  validFrom: '',
  validUntil: '',
  remark: '',
});
const form = ref(emptyForm());
const indefinite = ref(false);

watch(visible, async (open) => {
  if (!open) return;
  form.value = emptyForm();
  indefinite.value = false;
  await loadEmployees();
  const row = props.row;
  if (!row) return;
  form.value = {
    id: row.id,
    employeeId: String(row.employeeId),
    authCategory: row.authCategory,
    techLevel: row.techLevel,
    authType: row.authType || 1,
    itemScope: row.itemScope || '',
    authBasis: row.authBasis || '',
    validFrom: row.validFrom || '',
    validUntil: row.validUntil || '',
    remark: row.remark || '',
  };
  indefinite.value = !row.validUntil;
});

const handleSave = async () => {
  if (!form.value.employeeId)
    return ElMessage.warning('请选择被授权员工');
  if (!form.value.validFrom)
    return ElMessage.warning('生效日期不能为空');
  saving.value = true;
  try {
    await techAuthUpsert({
      ...form.value,
      validUntil: indefinite.value ? null : (form.value.validUntil || null),
    });
    ElMessage.success('保存成功');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    saving.value = false;
  }
};
</script>
