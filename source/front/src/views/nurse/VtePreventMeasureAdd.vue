<template>
  <!-- 登记预防措施 -->
  <el-dialog v-model="visible" :title="formTitle" data-testid="measure-dialog" width="520px">
    <el-form label-width="110px">
      <el-form-item label="患者">
        <div>{{ row.patientName || '-' }}（{{ row.admissionNo || '-' }}）</div>
      </el-form-item>
      <el-form-item label="风险等级">
        <el-tag :type="riskLevelTag(row.riskLevel)" disable-transitions>{{
            riskLevelText(row.riskLevel)
          }}
        </el-tag>
        <span class="muted">Caprini {{ row.capriniScore ?? '-' }} 分</span>
      </el-form-item>
      <el-form-item label="措施" required>
        <div data-testid="sel-measure-code" style="width: 100%">
          <el-select v-model="form.measureCode" placeholder="选择措施" style="width: 100%">
            <el-option
                v-for="m in measureOptions"
                :key="m.measureCode"
                :label="m.measureName + (m.recommend ? '（推荐）' : '')"
                :value="m.measureCode"/>
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="落实状态" required>
        <div data-testid="sel-exec-status" style="width: 100%">
          <el-radio-group v-model="form.executeStatus">
            <el-radio :value="1">已落实</el-radio>
            <el-radio :value="0">待落实</el-radio>
            <el-radio :value="2">禁忌未用</el-radio>
            <el-radio :value="3">患者拒绝</el-radio>
          </el-radio-group>
        </div>
      </el-form-item>
      <el-form-item v-if="form.executeStatus === 2 || form.executeStatus === 3" label="原因" required>
        <el-input v-model="form.reason" :rows="2" data-testid="ipt-reason" maxlength="500"
                  placeholder="禁忌/拒绝的原因（必填）" type="textarea"/>
      </el-form-item>
      <el-form-item label="措施说明">
        <el-input v-model="form.measureName" maxlength="200" placeholder="如：梯度压力袜 + 间歇充气加压装置"/>
      </el-form-item>
      <el-form-item label="计划日期">
        <el-date-picker v-model="form.planDate" placeholder="默认今天" style="width: 100%" type="date"
                        value-format="YYYY-MM-DD"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="saving" data-testid="btn-save-measure" type="primary" @click="submitForm">保存
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 登记预防措施（只有新增）：患者快照读父页面传入的 row（中高危名单行），
 * 措施候选按该患者风险等级在本组件打开时自拉；保存成功后 emit('saved') 让父页面重拉名单与看板。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getVteMeasureOptions, upsertVtePrevent} from '@/api/vte';
import {riskLevelTag, riskLevelText} from '@/lib/vte';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 中高危名单行（含 admissionId / patientName / riskLevel / capriniScore 快照）
  row: {type: Object, default: () => ({})},
});

const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const formTitle = computed(() => `登记预防措施 · ${props.row.patientName || ''}`);

const saving = ref(false);
const measureOptions = ref([]);
const form = reactive({measureCode: null, executeStatus: 1, reason: '', measureName: '', planDate: null});

watch(() => props.modelValue, async (v) => {
  if (!v)
    return;
  form.measureCode = null;
  form.executeStatus = 1;
  form.reason = '';
  form.measureName = '';
  form.planDate = null;
  try {
    const res = await getVteMeasureOptions(props.row.riskLevel);
    measureOptions.value = res?.data || [];
  } catch (e) {
    measureOptions.value = [];
  }
});

async function submitForm() {
  if (!form.measureCode) {
    ElMessage.warning('请选择措施');
    return
  }
  if ((form.executeStatus === 2 || form.executeStatus === 3) && !form.reason.trim()) {
    ElMessage.warning('禁忌未用 / 患者拒绝必须填写原因')
    return
  }
  saving.value = true
  try {
    await upsertVtePrevent({
      admissionId: props.row.admissionId,
      measureCode: form.measureCode,
      measureName: form.measureName || null,
      planDate: form.planDate || null,
      executeStatus: form.executeStatus,
      reason: form.reason || null,
    })
    ElMessage.success('措施已保存')
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
  font-size: 12px;
}
</style>
