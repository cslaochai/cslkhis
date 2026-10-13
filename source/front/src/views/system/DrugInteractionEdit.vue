<template>
  <el-dialog v-model="visible" :title="title" data-testid="kb-int-dialog" width="720px">
    <el-form :model="formData" label-width="120px">
      <el-form-item label="成分关键字A" required>
        <el-input v-model="formData.componentA" data-testid="kb-form-componentA"
                  placeholder="取成分主干，如「华法林」（命中华法林钠片/华法林抗凝注射液）"/>
      </el-form-item>
      <el-form-item label="成分关键字B" required>
        <el-input v-model="formData.componentB" data-testid="kb-form-componentB"
                  placeholder="如「胺碘酮」；两个成分书写顺序不影响唯一性"/>
      </el-form-item>
      <el-form-item label="严重度" required>
        <el-radio-group v-model="formData.severity">
          <el-radio v-for="d in props.severityOptions" :key="d.dictValue" :value="Number(d.dictValue)">
            {{ d.dictLabel }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="相互作用后果" required>
        <el-input
            v-model="formData.interactionDesc"
            :rows="3"
            data-testid="kb-form-desc"
            placeholder="审方被拦时医生看到的正文，逐字引用，请写成可执行的结论"
            type="textarea"
        />
      </el-form-item>
      <el-form-item label="处理建议">
        <el-input v-model="formData.suggestion" :rows="2" placeholder="换什么药 / 减多少量 / 监测什么指标"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" placeholder="依据来源（说明书版本 / 指南条目），便于以后复核"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'system:drugKnowledge:add'" :loading="submitting" data-testid="kb-int-save" type="primary"
                 @click="handleSubmit">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
/**
 * 相互作用条目：新增与修改共用（后端同一个 interactionUpsert，row 为空走新增）。
 * 审方药师被拒时屏幕上那句理由就是这里的 interactionDesc 原文（后端逐字引用），
 * 所以这里写的每个字都是发给医生的正式结论，校验只拦「空正文」不放行。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {interactionUpsert} from '@/api/drugKnowledge';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示新增
  row: {type: Object, default: null},
  // 严重度字典 his_drug_interaction_severity：列表筛选与列文案也要用，由页面加载一次下传
  severityOptions: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const title = computed(() => (props.row ? `修改相互作用：${props.row.componentA} × ${props.row.componentB}` : '新增相互作用'));

const submitting = ref(false);
const emptyForm = () => ({
  id: null,
  componentA: '',
  componentB: '',
  severity: 2,
  interactionDesc: '',
  suggestion: '',
  status: 1,
  remark: '',
});
const formData = reactive(emptyForm());

watch(visible, (open) => {
  if (!open) return;
  Object.assign(formData, emptyForm());
  const row = props.row;
  if (!row) return;
  Object.assign(formData, {
    id: row.id,
    componentA: row.componentA,
    componentB: row.componentB,
    severity: Number(row.severity),
    interactionDesc: row.interactionDesc || '',
    suggestion: row.suggestion || '',
    status: Number(row.status ?? 1),
    remark: row.remark || '',
  });
});

const handleSubmit = async () => {
  if (!formData.componentA.trim() || !formData.componentB.trim()) {
    return ElMessage.warning('两个成分关键字都要填');
  }
  if (!formData.interactionDesc.trim()) {
    return ElMessage.warning('相互作用后果不能为空，审方被拦时医生看到的就是这句话');
  }
  submitting.value = true;
  try {
    await interactionUpsert({...formData});
    ElMessage.success('已保存，下一次审方立即生效');
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e?.message || '保存失败');
  } finally {
    submitting.value = false;
  }
};
</script>
