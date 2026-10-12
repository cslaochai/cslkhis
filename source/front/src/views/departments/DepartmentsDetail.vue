<template>
  <el-dialog
      v-model="visible"
      destroy-on-close
      title="科室详情"
      width="700px"
  >
    <el-form v-loading="loading" disabled label-width="100px">
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="科室名称">
          <el-input v-model="detail.deptName"/>
        </el-form-item>
        <el-form-item label="上级科室">
          <el-input :value="parentDeptName"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="科室类型">
          <el-input :value="deptTypeLabel(deptTypeOptions, detail.deptType)"/>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="detail.sortOrder" :min="0" class="w-full"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="位置">
          <el-input v-model="detail.location"/>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="detail.contactPhone"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="科室负责人">
          <el-input :value="detail.deptLeaderName || '-'"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="detail.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>
      <el-form-item label="科室简介">
        <el-input v-model="detail.deptDesc" :rows="3" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {getDepartmentDetail} from '@/api/system';
import {deptTypeLabel} from '@/lib/deptType';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  deptTree: {type: Array, default: () => []},
  deptTypeOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const detail = reactive({
  id: null,
  deptCode: '',
  deptName: '',
  deptType: '',
  parentId: 0,
  sortOrder: 0,
  location: '',
  contactPhone: '',
  deptLeaderId: null,
  deptLeaderName: '',
  deptDesc: '',
  status: 1,
});

const findParent = (nodes, id) => {
  for (const node of nodes) {
    if (String(node.id) === String(id))
      return node.deptName;
    if (node.children?.length) {
      const found = findParent(node.children, id);
      if (found)
        return found;
    }
  }
  return '';
};
const parentDeptName = computed(() => {
  const parentId = detail.parentId;
  // 主键出参是字符串（防 JS 丢精度），顶级用 '0' 表达，不能写成 parentId === 0
  if (parentId == null || String(parentId) === '0')
    return '顶级科室';
  return findParent(props.deptTree, parentId) || '未知';
});

watch(visible, async (open) => {
  if (!open || !props.id) return;
  loading.value = true;
  try {
    const res = await getDepartmentDetail(props.id);
    if (res.code === 200) {
      Object.assign(detail, res.data);
    }
  } catch (error) {
    ElMessage.error(error.message || '获取科室详情失败');
  } finally {
    loading.value = false;
  }
});
</script>
