<template>
  <el-dialog v-model="visible" title="批量生成盲样台账" width="680px">
    <el-form label-width="120px">
      <el-form-item label="盲样编号前缀" required>
        <el-input v-model="form.sampleNoPrefix" placeholder="实际编号 = 前缀 + 序号，如 2026-A 生成 2026-A-1"/>
      </el-form-item>
      <el-form-item label="样品序号" required>
        <el-select v-model="form.sampleSeqs" multiple placeholder="一般一次下发 5 个浓度" style="width: 100%">
          <el-option v-for="s in seqOptions" :key="s" :label="'第 ' + s + ' 号'" :value="s"/>
        </el-select>
      </el-form-item>
      <el-form-item label="检验项目" required>
        <el-select v-model="form.items" allow-create default-first-option filterable multiple
                   placeholder="输入 项目编码|项目名称 后回车，如 GLU|葡萄糖" style="width: 100%">
        </el-select>
      </el-form-item>
      <el-form-item label="检测仪器">
        <el-select v-model="form.instruments" allow-create default-first-option filterable multiple
                   placeholder="填两台以上才会产生「室间差」比对" style="width: 100%">
        </el-select>
      </el-form-item>
      <el-form-item label="方法学">
        <el-input v-model="form.methodName" placeholder="如 己糖激酶法"/>
      </el-form-item>
      <el-form-item label="接收日期">
        <el-date-picker v-model="form.receiveDate" style="width: 100%" type="date" value-format="YYYY-MM-DD"/>
      </el-form-item>
    </el-form>
    <div class="text-xs text-gray-400 px-6">已存在的（序号 + 项目 + 仪器）组合会跳过，不会覆盖已录的检测值。</div>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'medtech:lisEqa:add'" :loading="saving" type="primary" @click="save">生成
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {eqaSampleGenerate} from '@/api/medicaltech';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  planId: {type: [String, Number], default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const saving = ref(false);
const seqOptions = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
const form = reactive({
  sampleNoPrefix: '', sampleSeqs: [], items: [],
  instruments: [], receiveDate: '', methodName: '',
});
watch(visible, (open) => {
  if (!open) return;
  Object.assign(form, {
    sampleNoPrefix: '', sampleSeqs: [1, 2, 3, 4, 5], items: [],
    instruments: [], receiveDate: '', methodName: '',
  });
});
const save = async () => {
  if (!form.sampleNoPrefix.trim()) {
    ElMessage.warning('盲样编号前缀不能为空');
    return;
  }
  if (form.sampleSeqs.length === 0) {
    ElMessage.warning('请勾选样品序号');
    return;
  }
  if (form.items.length === 0) {
    ElMessage.warning('请填写检验项目');
    return;
  }
  const items = form.items.map((raw) => {
    const p = String(raw).split('|');
    const code = (p[0] || '').trim();
    const name = (p[1] || p[0] || '').trim();
    return {itemCode: code, itemName: name};
  });
  if (items.some((i) => !i.itemCode || !i.itemName)) {
    ElMessage.warning('项目格式为「编码|名称」，缺项时会用编码兜底名称');
  }
  saving.value = true;
  try {
    const res = await eqaSampleGenerate({
      planId: props.planId,
      sampleNoPrefix: form.sampleNoPrefix.trim(),
      sampleSeqs: form.sampleSeqs,
      items,
      instruments: form.instruments.length ? form.instruments : undefined,
      receiveDate: form.receiveDate || undefined,
      methodName: form.methodName || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(res.message || '已生成');
      visible.value = false;
      emit('saved');
    } else
      ElMessage.error(res.message || '生成失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('生成失败');
  } finally {
    saving.value = false;
  }
};
</script>
