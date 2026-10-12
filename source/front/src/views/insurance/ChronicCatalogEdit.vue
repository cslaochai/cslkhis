<template>
  <!-- ==================== 病种目录 新建/编辑 ==================== -->
  <el-dialog v-model="visible" :title="catForm.id ? `修改病种 ${catForm.diseaseCode}` : '新增慢特病病种'"
             data-testid="catalog-form-dialog" width="640px">
    <el-form :model="catForm" label-width="110px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="病种编码" required>
            <el-input v-model="catForm.diseaseCode" data-testid="catalog-form-code" placeholder="如 MZ012"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="类别" required>
            <el-select v-model="catForm.diseaseType" data-testid="catalog-form-type" style="width: 100%">
              <el-option v-for="d in dictOptions(DICT_TYPE.CHRONIC_DISEASE_TYPE)" :key="d.dictValue"
                         :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="病种名称" required>
            <el-input v-model="catForm.diseaseName" data-testid="catalog-form-name"
                      placeholder="与国家医保慢特病目录同名"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="ICD-10 主码">
            <el-input v-model="catForm.icdCode" data-testid="catalog-form-icd" placeholder="如 I10"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="默认有效期">
            <el-input-number v-model="catForm.defaultValidMonths" :controls="false" :min="1" data-testid="catalog-form-months"
                             placeholder="留空＝长期" style="width: 100%"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="启用状态">
            <el-switch v-model="catForm.status" :active-value="1" :inactive-value="0"
                       data-testid="catalog-form-status"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input v-model="catForm.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button :loading="catSubmitting" data-testid="catalog-form-save" type="primary" @click="saveCat">保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {DICT_TYPE} from '@/lib/dict-cache';
import {upsertChronicCatalog} from '@/api/insuranceChronic';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 编辑时传入的行数据；为空表示新增（原 openCatCreate/openCatEdit 的分支语义） */
  row: {type: Object, default: null},
  /** 字典由页面统一加载（列表也在用），子组件只读 */
  dicts: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictOptions = (type) => props.dicts[type] || [];

const catSubmitting = ref(false);
const emptyCat = () => ({
  id: null, diseaseCode: '', diseaseName: '', diseaseType: 1,
  icdCode: '', defaultValidMonths: undefined, status: 1, remark: ''
});
const catForm = ref(emptyCat());

watch(visible, (open) => {
  if (!open) return;
  // 打开时按父页传入的行复位表单：row 为空＝新增，有值＝编辑（口径与原页内逻辑一致）
  catForm.value = props.row
      ? {...emptyCat(), ...props.row, id: props.row.id, defaultValidMonths: props.row.defaultValidMonths ?? undefined}
      : emptyCat();
});

const saveCat = async () => {
  const f = catForm.value;
  if (!f.diseaseCode || !f.diseaseName || !f.diseaseType) {
    ElMessage.warning('病种编码、名称、类别都要填');
    return;
  }
  catSubmitting.value = true;
  try {
    await upsertChronicCatalog({
      id: f.id || undefined, diseaseCode: f.diseaseCode.trim(), diseaseName: f.diseaseName.trim(),
      diseaseType: f.diseaseType, icdCode: f.icdCode || undefined,
      defaultValidMonths: f.defaultValidMonths ?? undefined, status: f.status, remark: f.remark || undefined
    });
    ElMessage.success(f.id ? '病种已更新' : '病种已新增');
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error?.message || '病种保存失败');
  } finally {
    catSubmitting.value = false;
  }
};
</script>
