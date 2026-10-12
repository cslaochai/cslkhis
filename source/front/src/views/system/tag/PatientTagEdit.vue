<template>
  <!-- 新增与编辑共用一张表单（后端同一个 upsert，row 为空走新增） -->
  <el-dialog
      v-model="visible"
      :title="formData.tagId ? '修改标签' : '新增标签'"
      width="500px"
  >
    <el-form label-width="80px">
      <el-form-item label="标签名称" required>
        <el-input v-model="formData.tagName" placeholder="请输入标签名称"/>
      </el-form-item>
      <el-form-item label="缩写">
        <el-input v-model="formData.shortName" placeholder="用于空间有限的显示场景"/>
      </el-form-item>
      <el-form-item label="标签颜色" required>
        <div class="flex flex-col gap-3">
          <div class="flex items-center gap-2">
            <el-color-picker v-model="formData.tagColor"/>
            <span class="text-sm text-slate-500">自定义颜色</span>
          </div>
          <div class="flex flex-wrap gap-2">
            <div
                v-for="color in presetColors"
                :key="color"
                :class="formData.tagColor === color ? 'border-slate-800' : 'border-transparent'"
                :style="{ backgroundColor: color }"
                class="h-6 w-6 cursor-pointer rounded border-2 transition-all hover:scale-110"
                @click="formData.tagColor = color"
            ></div>
          </div>
        </div>
      </el-form-item>
      <el-form-item label="预览">
        <span
            :style="{ backgroundColor: formData.tagColor || '#409EFF' }"
            class="inline-block rounded px-3 py-1 text-sm font-medium text-white"
        >
          {{ formData.tagName || '标签预览' }}
        </span>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'patient:tag:add'" :loading="submitLoading" type="primary" @click="handleSubmit">确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createPatientTag, updatePatientTag} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示新增
  row: {type: Object, default: null},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const submitLoading = ref(false);
const formData = ref({
  tagId: null,
  tagName: '',
  shortName: '',
  tagColor: '#409EFF',
});
// 预设颜色
const presetColors = [
  '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
  '#FF5722', '#9C27B0', '#00BCD4', '#795548', '#607D8B',
];

watch(visible, (open) => {
  if (!open) return;
  formData.value = props.row ? {...props.row} : {
    tagId: null,
    tagName: '',
    shortName: '',
    tagColor: '#409EFF',
  };
});

const handleSubmit = async () => {
  if (!formData.value.tagName) {
    ElMessage.warning('请输入标签名称');
    return;
  }
  submitLoading.value = true;
  try {
    if (formData.value.tagId) {
      await updatePatientTag(formData.value);
      ElMessage.success('修改成功');
    } else {
      await createPatientTag(formData.value);
      ElMessage.success('新增成功');
    }
    visible.value = false;
    emit('saved');
  } catch (error) {
    ElMessage.error(error.message || '操作失败');
  } finally {
    submitLoading.value = false;
  }
};
</script>
