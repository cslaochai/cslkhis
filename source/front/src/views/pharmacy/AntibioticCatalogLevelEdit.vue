<template>
  <el-dialog v-model="visible" title="维护抗菌药物分级" width="520px">
    <el-form :model="levelForm" label-width="140px">
      <el-form-item label="药品">
        <span>{{ levelForm.drugName }}<span class="muted">（{{ levelForm.specification || '—' }} / 发药单位：{{ levelForm.unit || '—' }}）</span></span>
      </el-form-item>
      <el-form-item label="抗菌药物分级" required>
        <div data-testid="level-select" style="width: 100%">
          <el-select v-model="levelForm.antibioticLevel" style="width: 100%">
            <el-option v-for="(text, code) in ANTIBIOTIC_LEVEL" :key="code" :label="text" :value="Number(code)" />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item v-if="levelForm.antibioticLevel > 0" label="WHO DDD（g/日）" required>
        <el-input v-model="levelForm.dddValue" data-testid="level-ddd-value" placeholder="如 1.0" />
      </el-form-item>
      <el-form-item v-if="levelForm.antibioticLevel > 0" label="每单位含药量（g）" required>
        <el-input v-model="levelForm.dddUnitGram" data-testid="level-ddd-unit" placeholder="按包装折算，如 0.25g×24粒 = 6.0" />
      </el-form-item>
      <div v-if="levelForm.antibioticLevel > 0" class="form-tip">
        DDDs = 发药数量 × 每单位含药量 ÷ DDD 值；使用强度 AUD = DDDs × 100 ÷ 收治患者人天数
      </div>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="saving" data-testid="btn-save-level" @click="saveLevel">保存</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 维护抗菌药物分级（自 AntibioticCatalogView.vue 原样搬出：字段、校验、接口均未变）
 */
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ANTIBIOTIC_LEVEL } from '@/lib/antibiotic'
import { upsertAntibioticCatalogLevel } from '@/api/antibiotic'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({ get: () => props.modelValue, set: (v) => emit('update:modelValue', v) })

const saving = ref(false)
const levelForm = reactive({ id: null, drugName: '', specification: '', unit: '', antibioticLevel: 0, dddValue: '', dddUnitGram: '' })

watch(visible, (open) => {
  if (!open || !props.row) return
  const row = props.row
  Object.assign(levelForm, {
    id: row.id,
    drugName: row.drugName,
    specification: row.specification,
    unit: row.unit,
    antibioticLevel: row.antibioticLevel || 0,
    dddValue: row.dddValue == null ? '' : String(row.dddValue),
    dddUnitGram: row.dddUnitGram == null ? '' : String(row.dddUnitGram)
  })
})

async function saveLevel() {
  saving.value = true
  try {
    await upsertAntibioticCatalogLevel({
      id: levelForm.id,
      antibioticLevel: levelForm.antibioticLevel,
      dddValue: levelForm.antibioticLevel > 0 ? Number(levelForm.dddValue) : null,
      dddUnitGram: levelForm.antibioticLevel > 0 ? Number(levelForm.dddUnitGram) : null
    })
    ElMessage.success('已保存')
    visible.value = false
    emit('saved')
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.muted {
  color: #909399;
}
.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
  margin-bottom: 10px;
}
</style>
