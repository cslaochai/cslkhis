<template>
  <el-dialog v-model="visible" title="空安瓿回收 / 剩余液销毁登记" width="480px">
    <div v-if="row" class="space-y-3 text-sm">
      <div class="rounded bg-slate-50 p-2 text-slate-600">
        <div>登记号：{{ row.registerNo }}</div>
        <div>药品：{{ row.drugName }}（{{ flagText(row.specialFlag) }}）</div>
        <div>发出安瓿数：<span class="font-medium text-slate-800">{{ row.ampouleIssued ?? '—' }}</span></div>
      </div>
      <el-form label-width="120px">
        <el-form-item label="回收空安瓿数" required>
          <el-input-number
              v-model="form.ampouleReturned"
              :min="0"
              :precision="0"
              class="!w-full"
              controls-position="right"
              data-testid="narco-ampoule-input"
          />
        </el-form-item>
        <el-form-item label="剩余液销毁量">
          <el-input-number
              v-model="form.ampouleDestroyed"
              :min="0"
              :precision="2"
              class="!w-full"
              controls-position="right"
          />
        </el-form-item>
        <el-form-item label="说明">
          <el-input
              v-model="form.returnRemark"
              :rows="2"
              placeholder="双人销毁须写明见证人姓名"
              type="textarea"
          />
        </el-form-item>
      </el-form>
      <div class="text-xs text-slate-400">
        登记后不可撤销、不可修改 —— 专册是证据。回收数不得大于发出数，登记人取当前登录账号。
      </div>
    </div>
    <template #footer>
      <el-button v-perm="'pharmacy:narcotic:edit'" :loading="submitting" data-testid="narco-ampoule-submit"
                 type="danger" @click="submit">确认登记
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 空安瓿回收登记（只补回收字段）—— 从 views/pharmacy-window/NarcoticRegisterView.vue 拆出
 *
 * 专册唯一的写入口，且只能补 回收空安瓿数 / 剩余液销毁量 / 说明 三个字段：
 * 数量、批号、双人签名一律改不动 —— 专册是证据，不是台账录入表。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {ampouleReturn} from '@/api/narcotic';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 待回收的专册登记行（只有「待回收」才给入口） */
  row: {type: Object, default: null},
  /** 管制分类文案（列表列同源，由页面按字典加载后传下） */
  flagText: {type: Function, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitting = ref(false);
const form = reactive({ampouleReturned: null, ampouleDestroyed: null, returnRemark: ''});

const submit = async () => {
  if (!props.row)
    return;
  const returned = form.ampouleReturned;
  if (returned === null || returned === undefined || Number(returned) < 0) {
    ElMessage.warning('请填写回收空安瓿数（非负数）');
    return;
  }
  submitting.value = true;
  try {
    const res = await ampouleReturn({
      id: props.row.id,
      ampouleReturned: Number(returned),
      ampouleDestroyed: form.ampouleDestroyed === null ? undefined : Number(form.ampouleDestroyed),
      returnRemark: form.returnRemark || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('空安瓿回收已登记');
      visible.value = false;
      emit('saved');
    } 
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitting.value = false;
  }
};

watch(() => props.modelValue, (v) => {
  if (v) {
    form.ampouleReturned = null;
    form.ampouleDestroyed = null;
    form.returnRemark = '';
  }
});
</script>
