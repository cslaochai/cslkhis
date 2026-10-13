<template>
  <el-dialog v-model="visible" :title="`执行配血：${row?.matchNo || ''}`" width="520px">
    <el-form label-width="90px">
      <el-form-item label="配血方法">
        <el-select v-model="form.method" style="width: 100%">
          <el-option v-for="d in cmMethodDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item label="配血结果">
        <el-radio-group v-model="form.result">
          <el-radio v-for="d in cmResultDict" :key="d.dictValue" :value="Number(d.dictValue)">{{
              d.dictLabel
            }}
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="补充结论">
        <el-input v-model="form.conclusion" :rows="2" placeholder="ABO/Rh 核对说明由服务端生成，这里补充观察记录"
                  type="textarea"/>
      </el-form-item>
    </el-form>
    <div class="text-xs text-gray-400 px-6">相合的配血单经第二人复核后，血袋自动置「已预留」；发血时凭预留状态 +
      申请单号执行。
    </div>
    <template #footer>
      <el-button v-perm="'medtech:bloodBank:edit'" type="primary" @click="saveCmExecute">提交</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 执行配血弹框（自 BloodBankView 拆出，对已有配血单执行配血）
 *
 * 配血方法 / 结果字典由页面加载后传入（交叉配血列表列也用同一份，见拆分规范第 6 条）；
 * 行快照 row 用于标题与提交时的 matchId，执行成功后 emit('saved')，由页面刷新配血列表与统计卡。
 */
import {computed, reactive, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {crossmatchExecute} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前配血单行快照（标题与提交的 matchId 都用它）
  row: {type: Object, default: null},
  // 配血方法字典（页面已加载）
  cmMethodDict: {type: Array, default: () => []},
  // 配血结果字典（页面已加载）
  cmResultDict: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const form = reactive({result: 1, method: 2, conclusion: ''});

watch(visible, (open) => {
  if (!open) return;
  Object.assign(form, {result: 1, method: Number(props.row?.method) || 2, conclusion: ''});
});

const saveCmExecute = () => {
  crossmatchExecute({
    matchId: props.row.id, result: form.result,
    method: form.method, conclusion: form.conclusion.trim() || undefined,
  }).then((res) => {
    if (res.code === 200) {
      ElMessage.success('配血已执行，待第二人复核');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '执行失败');
  }).catch((e) => {
    console.error(e);
    ElMessage.error('执行失败');
  });
};
</script>
