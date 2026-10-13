<template>
  <!-- ============ 出科弹框 ============ -->
  <el-dialog v-model="visible" :title="`出科登记 · ${outForm.patientName}`" data-testid="icu-out-dialog"
             width="560px">
    <el-form :model="outForm" label-width="100px">
      <el-form-item label="入科单号">
        <span class="text-sm text-slate-600">{{ outForm.stayNo }}</span>
      </el-form-item>
      <el-form-item label="出科时间" required>
        <el-date-picker v-model="outForm.outTime" :clearable="false" class="w-52" data-testid="icu-out-time"
                        type="datetime" value-format="YYYY-MM-DD HH:mm:ss"/>
      </el-form-item>
      <el-form-item label="转出去向" required>
        <el-select v-model="outForm.outDest" :fit-input-width="false" class="w-44" data-testid="icu-out-dest"
                   placeholder="选择去向">
          <el-option v-for="d in dicts.outDest" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
        </el-select>
      </el-form-item>
      <el-form-item :required="outNeedsReason(outForm.outDest)" label="转归说明">
        <el-input v-model="outForm.outReason" :placeholder="outNeedsReason(outForm.outDest) ? '转院/死亡/自动离院必填' : '可空'" :rows="2"
                  data-testid="icu-out-reason"
                  type="textarea"/>
      </el-form-item>
      <el-form-item label="出科 GCS">
        <el-input v-model="outForm.outGcs" class="w-32" data-testid="icu-out-gcs" placeholder="3~15，可空"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="outSaving" data-testid="icu-out-ok" type="primary" @click="saveOut">确认出科</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {outIcuStay} from '@/api/icu';
import {nowText, outNeedsReason} from '@/lib/icu';

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 要出科的在科记录行（id/stayNo/patientName）
  row: {type: Object, default: null},
  // 列表也在用的字典由页面加载一次传下来
  dicts: {type: Object, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const outSaving = ref(false);
const outForm = reactive({
  id: null,
  stayNo: '',
  patientName: '',
  outTime: '',
  outDest: null,
  outReason: '',
  outGcs: null
});
watch(() => props.modelValue, (v) => {
  if (!v) return;
  const row = props.row;
  outForm.id = row.id;
  outForm.stayNo = row.stayNo;
  outForm.patientName = row.patientName;
  outForm.outTime = nowText();
  outForm.outDest = null;
  outForm.outReason = '';
  outForm.outGcs = null;
});
const saveOut = async () => {
  if (!outForm.outTime)
    return ElMessage.warning('请选择出科时间');
  if (!outForm.outDest)
    return ElMessage.warning('请选择转出去向');
  if (outNeedsReason(outForm.outDest) && !outForm.outReason.trim()) {
    return ElMessage.warning('转院/死亡/自动离院必须填写转归说明');
  }
  outSaving.value = true;
  try {
    const res = await outIcuStay({
      id: outForm.id,
      outTime: outForm.outTime,
      outDest: outForm.outDest,
      outReason: outForm.outReason || undefined,
      outGcs: outForm.outGcs === '' || outForm.outGcs == null ? null : outForm.outGcs,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已出科');
      visible.value = false;
      emit('saved');
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    outSaving.value = false;
  }
};
</script>
