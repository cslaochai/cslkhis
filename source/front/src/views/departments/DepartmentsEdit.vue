<template>
  <el-dialog
      v-model="visible"
      :title="isEdit ? '编辑科室' : '新增科室'"
      width="700px"
  >
    <el-form v-loading="detailLoading" ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="科室名称" prop="deptName">
          <el-input v-model="formData.deptName" placeholder="请输入科室名称"/>
        </el-form-item>
        <el-form-item label="上级科室">
          <el-tree-select
              v-model="formData.parentId"
              :data="deptTree"
              :props="{ label: 'deptName', value: 'id', children: 'children' }"
              :render-after-expand="false"
              check-strictly
              class="w-full"
              clearable
              placeholder="请选择上级科室（不选则为顶级）"
          />
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="节点类型" prop="nodeType">
          <el-select v-model="formData.nodeType" :fit-input-width="false" class="w-full"
                     placeholder="请选择节点类型" @change="handleNodeTypeChange">
            <el-option v-for="n in nodeTypeOptions" :key="n.value" :label="n.label" :value="n.value"/>
          </el-select>
        </el-form-item>
        <!-- 服务范围与节点类型正交：片区/机构/行政后勤没有门诊/住院/急诊之分，选它就等于让片区再次混进科室 -->
        <el-form-item label="科室类型" prop="deptType">
          <el-select v-model="formData.deptType" :disabled="!serviceScopeEnabled" :fit-input-width="false"
                     class="w-full" multiple
                     :placeholder="serviceScopeEnabled ? '请选择科室类型' : '该节点类型不设服务范围'">
            <el-option v-for="d in deptTypeOptions" :key="d.dictValue"
                       :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="排序号">
          <el-input-number v-model="formData.sortOrder" :min="0" class="w-full"/>
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="formData.location" placeholder="如：门诊楼A座3层"/>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="联系电话">
          <el-input v-model="formData.contactPhone" placeholder="请输入联系电话"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="科室负责人">
          <el-select v-model="formData.deptLeaderId" :fit-input-width="false" class="w-full" clearable
                     filterable
                     placeholder="请选择科室负责人">
            <el-option v-for="e in employeeList" :key="e.id"
                       :label="`${e.empName}（${e.title || '无职称'}）`" :value="e.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="门诊审核人">
          <el-select v-model="formData.outpatientAuditorId" :fit-input-width="false" class="w-full" clearable
                     filterable
                     placeholder="审本科室门诊排班的指定人">
            <el-option v-for="e in employeeList" :key="e.id"
                       :label="`${e.empName}（${e.title || '无职称'}）`" :value="e.id"/>
          </el-select>
        </el-form-item>
      </div>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item v-if="isNursingAreaNode" label="护理审核人">
          <el-select v-model="formData.nursingAuditorId" :fit-input-width="false" class="w-full" clearable
                     filterable
                     placeholder="审本片区内各科室护理排班的指定人">
            <el-option v-for="e in employeeList" :key="e.id"
                       :label="`${e.empName}（${e.title || '无职称'}）`" :value="e.id"/>
          </el-select>
        </el-form-item>
        <!-- 口径：护理排班三级链的审核权在片区（大科）节点，末级病区/临床科室不再单独指定 -->
        <el-form-item v-else label="护理审核人">
          <p class="my-0 text-xs leading-5 text-slate-500">
            本科室是末级科室，不单独配置审核人，由所属护理片区统一指定审核人。
          </p>
        </el-form-item>
        <el-form-item label="排班审核人">
          <el-select v-model="formData.scheduleAuditorId" :fit-input-width="false" class="w-full" clearable
                     filterable
                     placeholder="审本科室全院岗位排班的指定人">
            <el-option v-for="e in employeeList" :key="e.id"
                       :label="`${e.empName}（${e.title || '无职称'}）`" :value="e.id"/>
          </el-select>
        </el-form-item>
      </div>
      <el-form-item label="科室简介">
        <el-input v-model="formData.deptDesc" :rows="3" placeholder="请输入科室简介" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'org:dept:add'" :loading="submitLoading" type="primary" @click="handleSubmit">确定
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {createDepartment, getDepartmentDetail, getEmployeeList, updateDepartment} from '@/api/system';
import {deptTypeToArray} from '@/lib/deptType';
import {DEPT_NODE_TYPE_OPTIONS, hasServiceScope} from '@/lib/deptNodeType';

// row 为空 = 新增，有值 = 修改（后端是同一个科室主档的 create/update，两侧字段与校验必须同一份）
const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
  deptTree: {type: Array, default: () => []},
  // 科室类型的唯一口径是字典 his_dept_type，由页面加载一次传下来（列表列同一份）
  deptTypeOptions: {type: Array, default: () => []},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const formRef = ref(null);
const submitLoading = ref(false);
const detailLoading = ref(false);
const isEdit = computed(() => !!props.row?.id);

const formData = reactive({
  id: null,
  deptCode: '',
  deptName: '',
  nodeType: '2', // 0-医疗机构 1-管理片区 2-临床科室 3-医技 4-药剂 5-行政后勤 6-医疗辅助 7-其他
  deptType: [], // 多选，提交时 join(',')
  parentId: 0,
  sortOrder: 0,
  location: '',
  contactPhone: '',
  deptLeaderId: null,
  outpatientAuditorId: null,
  nursingAuditorId: null,
  scheduleAuditorId: null,
  deptDesc: '',
  status: 1,
});
// 该节点类型是否要填服务范围：片区/机构/行政后勤 不填（后端同样会拒绝给片区挂服务类型）
const nodeTypeOptions = DEPT_NODE_TYPE_OPTIONS;
const serviceScopeEnabled = computed(() => hasServiceScope(formData.nodeType));
const rules = computed(() => ({
  deptName: [{required: true, message: '请输入科室名称', trigger: 'blur'}],
  nodeType: [{required: true, message: '请选择节点类型', trigger: 'change'}],
  // 必填只在业务节点上要求：切换节点类型会清空数组，rules 跟着节点类型走
  deptType: serviceScopeEnabled.value
      ? [{required: true, message: '请选择科室类型', trigger: 'change'}]
      : [],
}));
const handleNodeTypeChange = () => {
  if (!serviceScopeEnabled.value) formData.deptType = [];
};

const employeeList = ref([]);

/**
 * 科室树里「被别的科室当父节点」的那批 id（护理片区/大科）。
 * 口径来自现有 deptTree，不新增接口：叶子判定 = 本节点在树上有下级，与后端
 * 「护理审核人只配在有下级的片区节点、末级科室往上找」同一事实。
 */
const areaDeptIds = computed(() => {
  const ids = new Set();
  const walk = (nodes) => {
    (nodes || []).forEach((n) => {
      const kids = (n.children || []).filter((c) => c && c.id !== undefined && c.id !== null);
      if (kids.length) ids.add(String(n.id));
      walk(kids);
    });
  };
  walk(props.deptTree);
  return ids;
});
/** 当前编辑的是不是护理片区/大科节点：只有它才配护理排班审核人（新增片区按节点类型直接判定） */
const isNursingAreaNode = computed(() => formData.nodeType === '1'
    || (!!formData.id && areaDeptIds.value.has(String(formData.id))));
const loadEmployeeList = async () => {
  try {
    const res = await getEmployeeList({});
    employeeList.value = res.data || [];
  } catch (error) {
    console.error('加载员工列表失败:', error);
  }
};

const resetForm = () => {
  formData.id = null;
  formData.deptCode = '';
  formData.deptName = '';
  formData.nodeType = '2';
  formData.deptType = [];
  formData.parentId = 0;
  formData.sortOrder = 0;
  formData.location = '';
  formData.contactPhone = '';
  formData.deptLeaderId = null;
  formData.outpatientAuditorId = null;
  formData.nursingAuditorId = null;
  formData.scheduleAuditorId = null;
  formData.deptDesc = '';
  formData.status = 1;
};

watch(visible, async (open) => {
  if (!open) return;
  resetForm();
  formRef.value?.clearValidate();
  if (!employeeList.value.length) await loadEmployeeList();
  if (!props.row?.id) return;
  detailLoading.value = true;
  try {
    const res = await getDepartmentDetail(props.row.id);
    if (res.code === 200) {
      Object.assign(formData, {
        ...res.data,
        // 存量行没有节点类型时按临床科室回显，保存后即被回填（不默认成片区，避免历史行被误判）
        nodeType: res.data.nodeType == null || res.data.nodeType === '' ? '2' : String(res.data.nodeType),
        deptType: deptTypeToArray(res.data.deptType),
      });
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    detailLoading.value = false;
  }
});

const handleSubmit = async () => {
  if (!formRef.value)
    return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  submitLoading.value = true;
  try {
    // 提交前把数组转成逗号分隔字符串；非业务节点（片区/机构/行政）一律不带服务范围
    const payload = {
      ...formData,
      deptType: serviceScopeEnabled.value ? formData.deptType.join(',') : '',
    };
    const res = isEdit.value ? await updateDepartment(payload) : await createDepartment(payload);
    if (res.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '新增成功');
      visible.value = false;
      emit('saved');
    } 
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    submitLoading.value = false;
  }
};
</script>
