<template>
  <el-dialog v-model="visible" title="新增科室 → 上报科室代码" width="640px">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="160px">
      <el-form-item label="① 院内科室（源）" prop="deptId">
        <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="treeProps"
            check-strictly
            clearable
            data-testid="map-dept-select"
            default-expand-all
            filterable
            node-key="id"
            placeholder="选择要挂科目的科室"
            style="width: 100%"
            @change="handleDeptChange"
        />
      </el-form-item>
      <el-form-item label="② 上报科室代码" prop="subjectCode">
        <el-select
            v-model="form.subjectCode"
            data-testid="dept-subject-select"
            filterable
            placeholder="选择 CT08.00.002 科室代码"
            style="width: 100%"
        >
          <el-option-group
              v-for="group in groups"
              :key="group.code"
              :label="`${reportCode(group.code)} ${group.name}`"
          >
            <el-option
                v-for="s in group.children"
                :key="s.subjectCode"
                :label="`${reportCode(s.subjectCode)} ${s.subjectName}`"
                :value="s.subjectCode"
            />
            <el-option
                v-if="!group.children.length"
                :key="group.code"
                :label="`${reportCode(group.code)} ${group.name}（一级科目）`"
                :value="group.code"
            />
          </el-option-group>
        </el-select>
      </el-form-item>

      <!-- 把“谁映射谁”直接写成一句话，避免看成“给科目配科室” -->
      <div class="mb-4 rounded-md border border-slate-200 bg-slate-50 px-4 py-3 text-[13px]" data-testid="map-preview">
        <span class="text-slate-500">映射关系：</span>
        <el-tag v-if="selectedDept" class="mr-1" effect="plain" size="default">
          {{ selectedDept.deptName }}<template v-if="selectedDept.deptCode">（{{ selectedDept.deptCode }}）</template>
        </el-tag>
        <span v-else class="mr-1 text-slate-400">未选科室</span>
        <span class="mx-1 text-slate-400">→</span>
        <el-tag v-if="selectedSubject" effect="plain" size="default" type="success">
          {{ reportCode(selectedSubject.code) }} {{ selectedSubject.name }}
        </el-tag>
        <span v-else class="text-slate-400">未选代码</span>
        <div class="mt-1.5 text-[12px] text-slate-500">
          左边是院内科室，右边是上报口径的科室代码（CT08.00.002，A 前缀）；医保结算清单和卫统上报取这个码。
        </div>
      </div>

      <el-form-item label="主科目">
        <el-checkbox v-model="form.isPrimary">设为主科目（病案首页取这个）</el-checkbox>
        <div class="text-[12px] text-slate-500">
          勾选后会把该科室原来的主科目替换掉；一个科室只能有一个主科目。
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getDeptSubjectMap, saveDeptSubjectMap} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 当前页面选中的科室（仅作为默认值，弹框里可以改）
  dept: {type: Object, default: null},
  // 科室树
  deptTree: {type: Array, default: () => []},
  // 科目树按一级分组后的下拉数据
  groups: {type: Array, default: () => []},
  // 已配过上报科目的科室ID：管理片区里的精神心理科/感染疾病科/健康管理中心靠它识别为可维护
  mappedDeptIds: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const formRef = ref(null)
const submitLoading = ref(false)
const form = reactive({deptId: '', subjectCode: '', isPrimary: false})
// 目标科室已有的映射：改科室时要重新拉，整单提交靠它
const deptSubjects = ref([])
const deptLoading = ref(false)

const rules = {
  deptId: [{required: true, message: '请选择科室', trigger: 'change'}],
  subjectCode: [{required: true, message: '请选择诊疗科目', trigger: 'change'}],
}

// 可维护节点口径与主页面一致：临床(2)/其他(7)，或本身就配过上报科目的管理片区(1)
const isSubjectDept = (data) => {
  if (!data) return false
  const nodeType = String(data.nodeType ?? '')
  if (nodeType === '2' || nodeType === '7') return true
  if (!nodeType) {
    const types = String(data.deptType ?? '').split(',').map(s => s.trim())
    if (types.some(t => ['1', '4', '6'].includes(t))) return true
  }
  return props.mappedDeptIds.map(id => String(id)).includes(String(data.id))
}

const treeProps = {
  label: 'deptName',
  children: 'children',
  disabled: (data) => !isSubjectDept(data),
}

const findDept = (nodes, id) => {
  for (const n of nodes || []) {
    if (String(n.id) === String(id)) return n
    const hit = findDept(n.children, id)
    if (hit) return hit
  }
  return null
}

const selectedDept = computed(() => findDept(props.deptTree, form.deptId) || null)
// 上报口径 CT08.00.002 用 A 前缀，库里存的是不带前缀的科目码
const reportCode = (code) => (code ? `A${code}` : '')
const selectedSubject = computed(() => {
  for (const g of props.groups) {
    if (g.code === form.subjectCode) return {code: g.code, name: g.name}
    const hit = (g.children || []).find(c => c.subjectCode === form.subjectCode)
    if (hit) return {code: hit.subjectCode, name: hit.subjectName}
  }
  return null
})

const loadDeptSubjects = async (deptId) => {
  deptLoading.value = true
  try {
    const res = await getDeptSubjectMap(deptId)
    deptSubjects.value = res.code === 200 ? (res.data?.subjects || []) : []
    return deptSubjects.value
  } catch (error) /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */ {
    deptSubjects.value = []
    return []
  } finally {
    deptLoading.value = false
  }
}

watch(() => props.modelValue, async (open) => {
  if (!open) return
  form.subjectCode = ''
  form.deptId = props.dept?.id ?? props.dept?.deptId ?? ''
  formRef.value?.clearValidate()
  const list = form.deptId ? await loadDeptSubjects(form.deptId) : []
  form.isPrimary = !list.some(s => s.isPrimary === 1)
})

const handleDeptChange = async (deptId) => {
  form.subjectCode = ''
  const list = deptId ? await loadDeptSubjects(deptId) : []
  form.isPrimary = !list.some(s => s.isPrimary === 1)
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (deptLoading.value) return
  const deptId = form.deptId
  if (!deptId) return
  const code = form.subjectCode
  if (deptSubjects.value.some(s => s.subjectCode === code)) {
    ElMessage.warning('该科目已在这个科室的清单里')
    return
  }
  const items = deptSubjects.value.map(s => ({
    subjectCode: s.subjectCode,
    isPrimary: form.isPrimary ? 0 : s.isPrimary,
  }))
  items.push({subjectCode: code, isPrimary: form.isPrimary ? 1 : 0})

  submitLoading.value = true
  try {
    const res = await saveDeptSubjectMap(deptId, items)
    if (res.code === 200) {
      ElMessage.success(res.msg || '保存成功')
      visible.value = false
      emit('saved', deptId)
    }
  } catch (error) /* 后端业务异常已由全局拦截器提示，这里不重复弹窗 */ {
  } finally {
    submitLoading.value = false
  }
}
</script>
