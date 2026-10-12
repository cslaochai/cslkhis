<template>
  <el-dialog v-model="visible" title="新增医嘱别名" width="520px">
    <el-form :model="aliasForm" label-width="130px">
      <el-form-item label="别名（医嘱写法）" required>
        <el-input v-model="aliasForm.aliasName" data-testid="alias-name" placeholder="如：注射用头孢曲松钠" />
      </el-form-item>
      <el-form-item label="指向药品" required>
        <div data-testid="alias-drug-select" style="width: 100%">
          <el-select v-model="aliasForm.drugId" filterable placeholder="选择抗菌药物目录里的药品" style="width: 100%">
            <el-option
              v-for="d in drugOptions"
              :key="d.id"
              :label="`${d.drugName}（${ANTIBIOTIC_LEVEL[d.antibioticLevel]}）`"
              :value="d.id" />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="说明">
        <el-input v-model="aliasForm.remark" type="textarea" :rows="2" maxlength="500" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" data-testid="btn-save-alias" @click="saveAlias">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 新增医嘱别名（自 AntibioticCatalogView.vue 原样搬出：字段、校验、接口均未变）
 * drugOptions 由父页面加载（列表列 aliasLevel 也用它），此处只作下拉数据源。
 */
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ANTIBIOTIC_LEVEL } from '@/lib/antibiotic'
import { upsertAntibioticAlias } from '@/api/antibiotic'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  drugOptions: { type: Array, default: () => [] }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })

const saving = ref(false)
const aliasForm = reactive({ drugId: null, aliasName: '', remark: '' })

watch(visible, (open) => {
  if (!open) return
  aliasForm.drugId = null
  aliasForm.aliasName = ''
  aliasForm.remark = ''
})

async function saveAlias() {
  saving.value = true
  try {
    await upsertAntibioticAlias({ ...aliasForm })
    ElMessage.success('已保存')
    visible.value = false
    emit('saved')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}
</script>
