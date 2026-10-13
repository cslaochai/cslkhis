<template>
  <!-- ==================== 批次 新建/编辑 ==================== -->
  <el-dialog v-model="visible"
             :title="inspectForm.id ? `修改批次 ${inspectForm.inspectNo}` : '新建飞检/审核批次'" data-testid="inspect-form-dialog"
             width="700px">
    <el-form :model="inspectForm" label-width="120px">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="检查类型" required>
            <el-select v-model="inspectForm.inspectType" data-testid="inspect-form-type" style="width: 100%">
              <el-option v-for="d in dictOptions(DICT_TYPE.YB_INSPECT_TYPE)" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="统筹区/医保局" required>
            <el-input v-model="inspectForm.fundOrg" data-testid="inspect-form-fund-org"
                      placeholder="如 长沙市医疗保障局"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item data-testid="inspect-form-start" label="审核期间起" required>
            <el-date-picker v-model="inspectForm.inspectStartDate" style="width: 100%" type="date"
                            value-format="YYYY-MM-DD"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item data-testid="inspect-form-end" label="审核期间止" required>
            <el-date-picker v-model="inspectForm.inspectEndDate" style="width: 100%" type="date"
                            value-format="YYYY-MM-DD"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item data-testid="inspect-form-date" label="进驻/通知日期" required>
            <el-date-picker v-model="inspectForm.inspectDate" style="width: 100%" type="date"
                            value-format="YYYY-MM-DD"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="检查组">
            <el-input v-model="inspectForm.inspectTeam" placeholder="如 国家医保局第12飞检组"/>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="院内接待人">
            <el-input v-model="inspectForm.ourReceiver" placeholder="谁在现场对接检查组"/>
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="备注">
        <el-input v-model="inspectForm.remark" :rows="2" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="inspectSubmitting" data-testid="inspect-form-save" type="primary" @click="saveInspect">
        保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {DICT_TYPE} from '@/lib/dict-cache';
import {upsertYbInspection} from '@/api/insuranceDeduct';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  dicts: {type: Object, default: () => ({})},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const dictOptions = (type) => props.dicts[type] || [];

const inspectSubmitting = ref(false);
const emptyInspect = () => ({
  id: null, inspectNo: '', inspectType: 1, fundOrg: '', inspectStartDate: '', inspectEndDate: '',
  inspectDate: '', inspectTeam: '', ourReceiver: '', remark: ''
});
const inspectForm = ref(emptyInspect());

watch(visible, (open) => {
  if (!open) return;
  inspectForm.value = props.row ? {...emptyInspect(), ...props.row, id: props.row.id} : emptyInspect();
});

const saveInspect = async () => {
  const f = inspectForm.value;
  if (!f.fundOrg || !f.inspectStartDate || !f.inspectEndDate || !f.inspectDate) {
    ElMessage.warning('统筹区、审核期间、进驻日期都要填');
    return;
  }
  inspectSubmitting.value = true;
  try {
    await upsertYbInspection({
      id: f.id || undefined, inspectType: f.inspectType, fundOrg: f.fundOrg,
      inspectStartDate: f.inspectStartDate, inspectEndDate: f.inspectEndDate, inspectDate: f.inspectDate,
      inspectTeam: f.inspectTeam || undefined, ourReceiver: f.ourReceiver || undefined, remark: f.remark || undefined
    });
    ElMessage.success(f.id ? '批次已更新' : '批次已新建');
    visible.value = false;
    emit('saved');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    inspectSubmitting.value = false;
  }
};
</script>
