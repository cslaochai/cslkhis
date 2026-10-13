<template>
  <!-- 生成餐单 -->
  <el-dialog v-model="visible" title="按膳食方案生成餐单" width="520px" data-testid="gen-dialog">
    <el-form label-width="110px">
      <el-form-item label="就餐日期" required>
        <div data-testid="gen-meal-date" style="width: 100%">
          <el-date-picker v-model="genForm.mealDate" type="date" value-format="YYYY-MM-DD" :clearable="false"
                          style="width: 100%"/>
        </div>
      </el-form-item>
      <el-form-item label="病区范围">
        <div data-testid="gen-wards" style="width: 100%">
          <el-select v-model="genForm.wardIds" multiple filterable clearable placeholder="留空 = 全部病区"
                     style="width: 100%">
            <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="w.wardId"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="覆盖重生成">
        <el-switch v-model="genForm.overwrite" data-testid="gen-overwrite"/>
        <div class="muted hint">
          只重生成「未配送」的餐（待配餐/已配餐先物理删再插）。已配送、已签收是既成事实，这些人整天跳过并在结果里报数。
        </div>
      </el-form-item>
      <div class="form-tip">
        生成规则全在服务端：只取「执行中 + 口服 + 本人今天在院」的方案，按方案餐次拆成一日多条。
        同一个人同一餐只有一条（唯一键 人+日期+餐次），重复点不会产生第二份饭。
      </div>
    </el-form>
    <template #footer>
      <el-button type="primary" :loading="saving" data-testid="btn-do-generate" @click="doGenerate">生成</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { generateMealOrder } from '@/api/diet'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  /** 病区下拉：页面查询卡同款数据，按 prop 传入 */
  wards: { type: Array, default: () => [] },
  /** 打开时默认圈定的病区（取页面当前查询条件） */
  wardId: { type: [Number, String], default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const saving = ref(false)
const genForm = reactive({ mealDate: '', wardIds: [], overwrite: false })

function tomorrowStr() {
  const d = new Date(Date.now() + 86400000)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

watch(
  () => props.modelValue,
  (v) => {
    if (!v) return
    genForm.mealDate = tomorrowStr()
    genForm.wardIds = props.wardId ? [props.wardId] : []
    genForm.overwrite = false
  },
)

async function doGenerate() {
  if (!genForm.mealDate) {
    ElMessage.warning('请选择就餐日期');
    return
  }
  saving.value = true
  try {
    const res = await generateMealOrder({
      mealDate: genForm.mealDate,
      wardIds: genForm.wardIds.length ? genForm.wardIds : null,
      overwrite: genForm.overwrite,
    })
    const vo = res?.data || {}
    ElMessage.success(`${vo.message || ''}（扫到方案 ${vo.planCount ?? 0} 个，生成 ${vo.generatedCount ?? 0} 条，跳过 ${vo.skippedCount ?? 0} 人）`)
    visible.value = false
    emit('saved', genForm.mealDate)
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.muted {
  color: #909399;
  font-size: 12px;
}
.hint {
  margin-left: 8px;
}
.form-tip {
  color: #909399;
  font-size: 12px;
  line-height: 1.6;
}
</style>
