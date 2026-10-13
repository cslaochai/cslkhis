<template>
  <!-- 三阶段清点 -->
  <el-dialog v-model="visible" :title="phaseTitle" data-testid="g15-count-phase-dialog" width="760px">
    <div v-if="row" class="space-y-3">
      <div class="rounded border border-slate-200 bg-slate-50 p-3 text-sm text-slate-700">
        {{ text(row.countNo) }} · {{ text(row.patientName) }} · {{ text(row.plannedOperationName) }}
        <div class="text-[11px] text-slate-500">当前阶段：{{ text(row.phaseText) }}</div>
      </div>
      <el-form label-width="110px">
        <el-form-item label="核对人" required>
          <el-select v-model="form.nurseId" class="!w-full" data-testid="g15-count-nurse" filterable
                     placeholder="选择核对人（清点必须留名）">
            <el-option v-for="e in employees" :key="e.id" :label="e.empName || e.id" :value="String(e.id)"/>
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="row.items || []" style="width: 100%">
        <el-table-column label="名称" min-width="200">
          <template #default="{ row }">
            <span class="text-slate-900">{{ text(row.itemName) }}</span>
            <span class="ml-2 text-[11px] text-slate-400">{{ text(row.itemCategoryText) }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="术前基线" width="110">
          <template #default="{ row }">{{
              row.beforeQty === null || row.beforeQty === undefined ? '—' : row.beforeQty
            }}
          </template>
        </el-table-column>
        <el-table-column align="center" label="本次实点" width="180">
          <template #default="{ row }">
            <el-input-number v-model="form.qtys[String(row.id)]" :data-testid="`g15-count-qty-${row.seqNo}`" :min="0" :step="1"
                             size="small"/>
          </template>
        </el-table-column>
      </el-table>
      <el-form label-width="110px">
        <el-form-item label="差异说明">
          <el-input v-model="form.diffNote" :rows="2" data-testid="g15-count-diffnote"
                    placeholder="任一项与术前不一致时必填：差了什么、怎么处理、结论" type="textarea"/>
        </el-form-item>
      </el-form>
      <div class="text-[12px] text-slate-500">
        判定基准是<b>术前基线</b>，不是"和上一段比" ——
        连续两段都少一块纱布时，"与上段一致"会显示通过。
      </div>
    </div>
    <template #footer>
      <el-button v-perm="'ipd:operationCount:edit'" :loading="submitting" data-testid="g15-count-phase-submit"
                 type="primary" @click="submit">登记清点
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {countOperationPhase} from '@/api/operationCount';
import {countPhaseText} from '@/lib/anesthesia';
import {text} from '@/lib/operationCount';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前清点单（含 items 基线），由父页面加载后传入
  row: {type: Object, default: null},
  employees: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);
const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitting = ref(false);
const form = reactive({phase: 1, nurseId: '', diffNote: '', qtys: {}});
watch(visible, (v) => {
  if (!v)
    return;
  const c = props.row;
  if (!c)
    return;
  form.phase = (c.phase || 0) + 1;
  form.nurseId = '';
  form.diffNote = '';
  form.qtys = {};
  (c.items || []).forEach((it) => {
    form.qtys[String(it.id)] = Number(it.beforeQty ?? 0);
  });
});
const phaseTitle = computed(() => '登记' + countPhaseText(form.phase));
const submit = async () => {
  const c = props.row;
  if (!c)
    return;
  if (!form.nurseId) {
    ElMessage.warning('请选择核对人（清点必须留名）');
    return;
  }
  const quantities = (c.items || []).map((it) => ({
    itemId: String(it.id),
    qty: Number(form.qtys[String(it.id)] ?? 0),
  }));
  submitting.value = true;
  try {
    await countOperationPhase({
      countId: c.id,
      phase: form.phase,
      nurseId: form.nurseId,
      quantities,
      diffNote: form.diffNote.trim() || undefined,
    });
    ElMessage.success(`${countPhaseText(form.phase)}已登记`);
    visible.value = false;
    emit('saved');
  } catch (e) {
    ElMessage.error(e.message || '清点登记失败');
  } finally {
    submitting.value = false;
  }
};
</script>
