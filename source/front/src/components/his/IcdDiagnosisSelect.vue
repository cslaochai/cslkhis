<template>
  <el-select
      v-model="innerCode"
      filterable
      remote
      reserve-keyword
      :clearable="clearable"
      :disabled="disabled"
      :size="size"
      :loading="loading"
      :placeholder="placeholder"
      :fit-input-width="false"
      :remote-method="remoteSearch"
      :style="{width}"
      popper-class="icd-diagnosis-popper"
      @change="onSelect"
      @clear="onClear"
  >
    <el-option
        v-for="o in options"
        :key="o.icdCode"
        :value="o.icdCode"
        :label="(o.icdCode || '') + ' ' + (o.icdName || '')"
    />
  </el-select>
</template>

<script setup>
import {ref, watch} from 'vue';
import {searchIcd10} from '@/api/system';

/**
 * ICD-10 诊断远程检索选择器（复用 system.js searchIcd10，后端零改动）。
 *
 * 契约：
 *  - v-model:modelValue 绑定诊断编码（icdCode）；
 *  - v-model:name 绑定诊断名称（icdName），选中后联动回填；
 *  - change 回传完整 option；clear 清空编码与名称。
 * 单表单与多行表格行内均可用：每个实例自带检索选项集，互不串扰。
 */
const props = defineProps({
  modelValue: {type: String, default: ''},
  name: {type: String, default: ''},
  placeholder: {type: String, default: '输入诊断名称 / 编码搜索'},
  size: {type: String, default: 'default'},
  disabled: {type: Boolean, default: false},
  clearable: {type: Boolean, default: true},
  width: {type: String, default: '100%'},
});
const emit = defineEmits(['update:modelValue', 'update:name', 'change', 'clear']);

const options = ref([]);
const loading = ref(false);
const innerCode = ref(props.modelValue || '');

/** 外部回显：编码有值但选项集里没有时预置一条，避免下拉只显示裸编码 */
const seedOption = (code, name) => {
  if (!code) return;
  if (!options.value.some(o => o.icdCode === code)) {
    options.value = [{icdCode: code, icdName: name || ''}, ...options.value];
  }
};

watch(() => props.modelValue, (v) => {
  innerCode.value = v || '';
  seedOption(v, props.name);
}, {immediate: true});
watch(() => props.name, (n) => seedOption(props.modelValue, n));

const remoteSearch = async (query) => {
  const kw = (query || '').trim();
  if (!kw) {
    options.value = [];
    return;
  }
  loading.value = true;
  try {
    const res = await searchIcd10(kw);
    options.value = res.data || [];
  } catch (e) {
    options.value = [];
    console.error('检索 ICD 编码失败:', e);
  } finally {
    loading.value = false;
  }
};

const onSelect = (code) => {
  const opt = options.value.find(o => o.icdCode === code);
  const name = opt ? opt.icdName : props.name;
  emit('update:modelValue', code || '');
  emit('update:name', name || '');
  if (opt) emit('change', opt);
};

const onClear = () => {
  emit('update:modelValue', '');
  emit('update:name', '');
  emit('clear');
};
</script>
