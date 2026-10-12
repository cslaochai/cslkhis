<template>
  <!-- 套餐编辑（明细整单替换） -->
  <el-dialog v-model="visible"
             :title="form.id ? `编辑套餐 — ${form.packageName}` : '新增体检套餐'" width="880px">
    <el-form label-width="100px">
      <div class="flex flex-wrap gap-x-6">
        <el-form-item label="套餐名称" required>
          <el-input v-model="form.packageName" style="width: 220px"/>
        </el-form-item>
        <el-form-item label="适用性别">
          <el-select v-model="form.genderLimit" style="width: 110px">
            <el-option :value="0" label="不限"/>
            <el-option :value="1" label="男"/>
            <el-option :value="2" label="女"/>
          </el-select>
        </el-form-item>
        <el-form-item label="价格(元)">
          <el-input-number v-model="form.price" :controls="false" :min="0" :precision="2" style="width: 130px"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>
      <el-form-item label="说明">
        <el-input v-model="form.description" style="width: 520px"/>
      </el-form-item>
      <el-form-item label="项目明细" required>
        <div class="w-full">
          <el-table :data="form.items" border max-height="320" size="small">
            <el-table-column label="项目名称" min-width="220">
              <template #default="{ row }">
                <el-input v-model="row.itemName"/>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                <el-select v-model="row.itemType" style="width: 100%">
                  <el-option v-for="t in itemTypeOptions" :key="t" :label="itemTypeMap[t]" :value="t"/>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="参考范围" width="180">
              <template #default="{ row }">
                <el-input v-model="row.refStandard"/>
              </template>
            </el-table-column>
            <el-table-column label="单项金额(元)" width="140">
              <template #default="{ row }">
                <el-input-number v-model="row.amount" :controls="false" :min="0" :precision="2"
                                 style="width: 120px"/>
              </template>
            </el-table-column>
            <el-table-column align="center" label="" width="60">
              <template #default="{ $index }">
                <el-button :disabled="form.items.length <= 1" :icon="Delete" link size="small" type="danger"
                           @click="removeItem($index)"/>
              </template>
            </el-table-column>
          </el-table>
          <div class="flex items-center gap-3 mt-1">
            <el-button :icon="Plus" link type="primary" @click="addItem">添加明细行</el-button>
            <span class="text-xs text-gray-500">保存时按整单替换明细；体检登记时按这些项目预生成结果空行</span>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="saving" type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 体检套餐新增/修改（后端同一个 checkupPackageSave，row 为空走新增、有值走修改）
 * 套餐保存是明细整单替换（Save DTO items 整删重插），编辑前必须回填全量明细，
 * 故打开时按 id 走 getCheckupPackage 取详情而不是直接用列表行。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {Delete, Plus} from '@element-plus/icons-vue';
import {checkupPackageSave, getCheckupPackage} from '@/api/checkup';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 为空走新增，有值走修改
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const itemTypeMap = {1: '检验', 2: '检查', 3: '一般'};
const itemTypeOptions = [1, 2, 3];
const saving = ref(false);
const form = reactive({
  id: null, packageName: '', genderLimit: 0, price: null, description: '', status: 1, remark: '',
  items: [],
});
const blankItem = () => ({itemName: '', itemType: 3, refStandard: '', amount: null});

watch(
  () => props.modelValue,
  async (v) => {
    if (!v) return;
    if (!props.row) {
      Object.assign(form, {
        id: null, packageName: '', genderLimit: 0, price: null, description: '', status: 1, remark: '',
        items: [blankItem()],
      });
      return;
    }
    try {
      const res = await getCheckupPackage(props.row.id);
      if (res.code !== 200) {
        ElMessage.error(res.message || '查询失败');
        visible.value = false;
        return;
      }
      const d = res.data;
      Object.assign(form, {
        id: d.id, packageName: d.packageName, genderLimit: Number(d.genderLimit ?? 0),
        price: d.price ?? null, description: d.description || '', status: Number(d.status ?? 1),
        remark: d.remark || '',
        items: (d.items || []).map((i) => ({
          itemName: i.itemName, itemType: Number(i.itemType ?? 3), refStandard: i.refStandard || '',
          amount: i.amount ?? null,
        })),
      });
      if (!form.items.length)
        form.items = [blankItem()];
    } catch (e) {
      console.error(e);
      ElMessage.error('查询失败');
      visible.value = false;
    }
  }
);

const addItem = () => form.items.push(blankItem());
const removeItem = (idx) => form.items.splice(idx, 1);
const save = async () => {
  if (!form.packageName.trim()) {
    ElMessage.warning('请填写套餐名称');
    return;
  }
  const items = form.items.filter((i) => i.itemName.trim());
  if (!items.length) {
    ElMessage.warning('项目明细至少一条');
    return;
  }
  saving.value = true;
  try {
    const res = await checkupPackageSave({
      id: form.id ?? undefined,
      packageName: form.packageName.trim(),
      genderLimit: form.genderLimit,
      price: form.price ?? undefined,
      description: form.description.trim() || undefined,
      status: form.status,
      remark: form.remark.trim() || undefined,
      items: items.map((i) => ({
        itemName: i.itemName.trim(),
        itemType: i.itemType,
        refStandard: i.refStandard?.trim() || undefined,
        amount: i.amount ?? undefined,
      })),
    });
    if (res.code === 200) {
      ElMessage.success(form.id ? '套餐已保存' : '套餐已创建');
      visible.value = false;
      emit('saved');
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    saving.value = false;
  }
};
</script>
