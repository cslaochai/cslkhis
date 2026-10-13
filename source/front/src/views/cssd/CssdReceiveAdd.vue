<template>
  <el-dialog v-model="visible" title="器械回收登记" width="680px">
    <el-form label-width="100px">
      <el-form-item label="器械包条码">
        <el-input v-model="form.packNo" placeholder="不填自动生成（CSSD+日期+序号）" style="width: 280px"/>
      </el-form-item>
      <el-form-item label="器械包名称" required>
        <el-select v-model="form.packName" :fit-input-width="false" allow-create default-first-option
                   filterable placeholder="从模板目录选择" style="width: 280px"
                   @change="onPackNameChange">
          <el-option v-for="t in templateOptions" :key="t.id" :label="t.packName" :value="t.packName"/>
        </el-select>
      </el-form-item>
      <el-form-item v-if="packItemsPreview.length" label="组成清单">
        <el-table :data="packItemsPreview" border max-height="260" size="small" style="width: 420px">
          <el-table-column label="名称" min-width="150" prop="itemName"/>
          <el-table-column label="规格" prop="spec" width="120">
            <template #default="{ row }">{{ row.spec || '—' }}</template>
          </el-table-column>
          <el-table-column align="right" label="基数" width="90">
            <template #default="{ row }">{{ row.quantity }}{{ row.unit }}</template>
          </el-table-column>
        </el-table>
      </el-form-item>
      <el-form-item label="申领科室">
        <el-select v-model="form.deptId" :fit-input-width="false" clearable filterable
                   placeholder="请选择科室" style="width: 280px" @change="onReceiveDeptChange">
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="灭菌方式" required>
        <el-select v-model="form.sterilizeMethod" :fit-input-width="false" style="width: 280px">
          <el-option v-for="d in methodDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'asset:cssd:edit'" type="primary" @click="submitReceive">登记</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {cssdReceive, cssdTemplateSelectList, getCssdTemplateDetail} from '@/api/cssd';
import {getDepartmentSelectList} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 灭菌方式字典由父页一次加载后传入（列表列与追溯链详情共用同一份，避免两处各拉一次）
  methodDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = reactive({
  packNo: '', packName: '', deptId: null, deptName: '', sterilizeMethod: 1, remark: '',
});
const packItemsPreview = ref([]);
// 申领科室、器械包模板目录只有本弹窗用，首次打开时才拉取（口径参照原父页 onMounted 预载）
const deptOptions = ref([]);
const loadDepts = async () => {
  if (deptOptions.value.length)
    return;
  try {
    const res = await getDepartmentSelectList({});
    if (res.code === 200)
      deptOptions.value = res.data || [];
  } catch (e) {
    console.error('加载科室失败', e);
  }
};
const templateOptions = ref([]);
const loadTemplateOptions = async () => {
  if (templateOptions.value.length)
    return;
  try {
    const res = await cssdTemplateSelectList();
    if (res.code === 200)
      templateOptions.value = res.data || [];
  } catch (e) {
    console.error('加载器械包模板失败', e);
  }
};

watch(visible, (open) => {
  if (!open) return;
  Object.assign(form, {packNo: '', packName: '', deptId: null, deptName: '', sterilizeMethod: 1, remark: ''});
  packItemsPreview.value = [];
  loadDepts();
  loadTemplateOptions();
});

const onReceiveDeptChange = (id) => {
  const d = deptOptions.value.find((x) => String(x.id) === String(id));
  form.deptName = d?.deptName || '';
};
/** 选包软带出：默认灭菌方式与组成清单（仍可手改包名走自定义） */
const onPackNameChange = async (name) => {
  const tpl = templateOptions.value.find((x) => x.packName === name);
  if (!tpl) {
    packItemsPreview.value = [];
    return;
  }
  form.sterilizeMethod = Number(tpl.sterilizeMethod) || form.sterilizeMethod;
  try {
    const res = await getCssdTemplateDetail(tpl.id);
    if (res.code === 200)
      packItemsPreview.value = res.data?.items || [];
  } catch (e) {
    console.error(e);
    packItemsPreview.value = [];
  }
};
const submitReceive = async () => {
  if (!String(form.packName || '').trim()) {
    ElMessage.warning('请选择器械包');
    return;
  }
  try {
    const res = await cssdReceive({
      packNo: form.packNo.trim() || undefined,
      packName: String(form.packName).trim(),
      deptId: form.deptId ?? undefined,
      deptName: form.deptName.trim() || undefined,
      sterilizeMethod: form.sterilizeMethod,
      remark: form.remark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(`回收登记成功，条码 ${res.data?.packNo}`);
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '登记失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('登记失败');
  }
};
</script>
