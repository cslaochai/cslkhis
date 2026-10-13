<template>
  <el-dialog v-model="visible" :title="form.id ? '编辑耗材' : '新增耗材'" destroy-on-close
             width="520px">
    <el-form label-width="90px">
      <el-form-item label="耗材编码" required>
        <el-input v-model="form.consumableCode" placeholder="如 HC-HC-006"/>
      </el-form-item>
      <el-form-item label="耗材名称" required>
        <el-input v-model="form.consumableName"/>
      </el-form-item>
      <el-form-item label="类别">
        <el-select v-model="form.category" :fit-input-width="false" style="width: 100%">
          <el-option v-for="(label, key) in categoryMap" :key="key" :label="label" :value="Number(key)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="规格">
        <el-input v-model="form.specification"/>
      </el-form-item>
      <el-form-item label="单位">
        <el-input v-model="form.unit" placeholder="包/支/盒/个"/>
      </el-form-item>
      <el-form-item label="生产厂家">
        <el-input v-model="form.manufacturer"/>
      </el-form-item>
      <el-form-item label="零售价">
        <el-input-number v-model="form.retailPrice" :min="0" :precision="2" style="width: 100%"/>
      </el-form-item>
      <el-form-item label="高值耗材">
        <el-switch v-model="form.isHighValue" :active-value="1" :inactive-value="0" active-text="扫码溯源登记"
                   inactive-text="普通领用"/>
      </el-form-item>
      <template v-if="form.isHighValue === 1">
        <el-form-item label="UDI-DI">
          <el-input v-model="form.udiDi" placeholder="GS1 数据载体 (01) 段 14 位，扫码按此命中字典"/>
        </el-form-item>
        <el-form-item label="注册证号">
          <el-input v-model="form.regCertNo" placeholder="如 国械注准20173660001"/>
        </el-form-item>
      </template>
      <el-form-item label="状态">
        <el-select v-model="form.status" :fit-input-width="false" style="width: 100%">
          <el-option :value="1" label="启用"/>
          <el-option :value="0" label="停用"/>
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="['asset:supplies:add','asset:supplies:edit']" type="primary" @click="handleDictSubmit">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {consumableUpsert} from '@/api/supplies';
import {categoryMap} from '@/lib/supplies';

// row 为空 = 新增，有值 = 修改（后端是同一个 consumableUpsert，两侧字段与校验同一份）
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const blankForm = () => ({
  id: null,
  consumableCode: '',
  consumableName: '',
  category: 5,
  specification: '',
  unit: '',
  manufacturer: '',
  retailPrice: null,
  status: 1,
  isHighValue: 0,
  udiDi: '',
  regCertNo: ''
});
const form = ref(blankForm());

watch(visible, (open) => {
  if (!open) return;
  const row = props.row;
  form.value = row
      ? {
        id: row.id,
        consumableCode: row.consumableCode,
        consumableName: row.consumableName,
        category: row.category ?? 5,
        specification: row.specification || '',
        unit: row.unit || '',
        manufacturer: row.manufacturer || '',
        retailPrice: row.retailPrice ?? null,
        status: row.status ?? 1,
        isHighValue: row.isHighValue ?? 0,
        udiDi: row.udiDi || '',
        regCertNo: row.regCertNo || ''
      }
      : blankForm();
});

const handleDictSubmit = async () => {
  const f = form.value;
  if (!f.consumableCode.trim())
    return ElMessage.warning('耗材编码不能为空');
  if (!f.consumableName.trim())
    return ElMessage.warning('耗材名称不能为空');
  try {
    await consumableUpsert({...f, consumableCode: f.consumableCode.trim(), consumableName: f.consumableName.trim()});
    ElMessage.success(f.id ? '字典修改成功' : '字典新增成功');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  }
};
</script>
