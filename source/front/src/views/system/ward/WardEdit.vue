<template>
  <!-- 新增与编辑共用一张表单（后端同一个 upsert，wardId 为空走新增） -->
  <el-dialog v-model="visible" :title="formData.wardId ? '编辑病区' : '新增病区'" width="760px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <el-form-item label="病区名称" prop="wardName">
        <el-input v-model="formData.wardName" data-testid="ward-name" placeholder="如：呼吸内科病区、13A 病区"/>
      </el-form-item>
      <el-form-item label="病区编码" prop="wardCode">
        <el-input v-model="formData.wardCode" placeholder="留空由系统生成 W001 形式的编码"/>
      </el-form-item>
      <el-form-item label="本位科室" prop="deptId">
        <el-select
            v-model="formData.deptId"
            class="!w-full"
            data-testid="ward-primary-dept"
            filterable
            placeholder="默认收治科室（病区的主责科室）"
            @change="handlePrimaryDeptChange"
        >
          <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
        </el-select>
        <div class="mt-1 text-[12px] text-slate-500">
          本位科室会同步写入病区主表，数据权限、排班、床位统计都按它折算；跨科收治的其它科室填在下方。
        </div>
      </el-form-item>

      <el-form-item label="收治科室">
        <div class="w-full">
          <div
              v-for="(row, idx) in deptRows"
              :key="idx"
              class="mb-2 flex items-center gap-2"
              :data-testid="`ward-dept-row-${idx}`"
          >
            <el-select
                v-model="row.deptId"
                :disabled="row.isPrimary === 1"
                class="!w-64"
                filterable
                placeholder="选择科室"
            >
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
            <el-tag v-if="row.isPrimary === 1" size="small" type="primary">本位</el-tag>
            <el-input-number
                v-model="row.bedQuota"
                :min="0"
                :value-on-clear="null"
                class="!w-32"
                controls-position="right"
                placeholder="优先床数"
            />
            <span class="text-[12px] text-slate-500">优先床数（空=不限额）</span>
            <el-button
                v-if="row.isPrimary !== 1"
                :icon="Delete"
                link
                type="danger"
                @click="removeDeptRow(idx)"
            >删除
            </el-button>
          </div>
          <el-button :icon="Plus" data-testid="ward-add-dept" link type="primary" @click="addDeptRow">
            添加收治科室
          </el-button>
          <div class="mt-1 text-[12px] text-slate-500">
            一个病区可以收多个科室的患者（跨科收治 / 全院一张床）；优先床数用于标记该科室在本病区的专科床配额，剩余床位作机动床。
          </div>
        </div>
      </el-form-item>

      <el-form-item label="总床位数" prop="totalBeds">
        <el-input-number v-model="formData.totalBeds" :min="0" class="!w-40" controls-position="right"/>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="formData.remark" :rows="3" placeholder="如：位于住院楼 13 层 A 区" type="textarea"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button v-perm="'org:ward:add'" :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {Delete, Plus} from '@element-plus/icons-vue'
import {getWardDetail, saveWard} from '@/api/system'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 编辑传入当前行；为空表示新增
  row: {type: Object, default: null},
  // 科室树：页面加载一次下传，这里展平成可搜索的下拉
  deptList: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  wardId: null,
  wardName: '',
  wardCode: '',
  deptId: null,
  totalBeds: 0,
  status: 1,
  remark: '',
})

// 收治科室清单：本位科室固定第一行且不可删，其余可增删
const deptRows = ref([])

const rules = {
  wardName: [{required: true, message: '请输入病区名称', trigger: 'blur'}],
  deptId: [{required: true, message: '请选择本位科室', trigger: 'change'}],
}

// 科室树展平：病区收治科室不该选到「内科系统」这类汇总节点，所以只列真实科室，
// 但要保留全部层级（临床科室可能挂在「内科系统」下）
const deptOptions = computed(() => {
  const out = []
  const walk = (list) => {
    for (const d of list || []) {
      out.push({id: d.id, deptName: d.deptName})
      if (d.children && d.children.length) walk(d.children)
    }
  }
  walk(props.deptList)
  return out
})

const resetForm = () => {
  formData.wardId = null
  formData.wardName = ''
  formData.wardCode = ''
  formData.deptId = null
  formData.totalBeds = 0
  formData.status = 1
  formData.remark = ''
  deptRows.value = []
}

const ensurePrimaryRow = () => {
  if (!formData.deptId) return
  const primary = deptRows.value.find(r => r.isPrimary === 1)
  if (primary) {
    primary.deptId = formData.deptId
    return
  }
  deptRows.value.unshift({deptId: formData.deptId, bedQuota: null, isPrimary: 1})
}

const handlePrimaryDeptChange = () => {
  ensurePrimaryRow()
}

const addDeptRow = () => {
  deptRows.value.push({deptId: null, bedQuota: null, isPrimary: 0})
}

const removeDeptRow = (idx) => {
  deptRows.value.splice(idx, 1)
}

watch(visible, async (open) => {
  if (!open) return
  resetForm()
  if (!props.row) {
    // 新增：先把本位行占好，用户选完本位就能直接填床数
    return
  }
  try {
    const res = await getWardDetail(props.row.wardId)
    if (res.code === 200 && res.data) {
      const d = res.data
      formData.wardId = d.wardId
      formData.wardName = d.wardName
      formData.wardCode = d.wardCode
      formData.deptId = d.deptId
      formData.totalBeds = d.totalBeds ?? 0
      formData.status = d.status
      formData.remark = d.remark || ''
      deptRows.value = (d.depts || []).map(item => ({
        deptId: item.deptId,
        bedQuota: item.bedQuota,
        isPrimary: item.isPrimary === 1 ? 1 : 0,
      }))
      ensurePrimaryRow()
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
  }
})

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  // 收治科室去重、去空；本位科室必须在内（后端也会兜一次）
  const seen = new Set()
  const depts = []
  for (const row of deptRows.value) {
    if (!row.deptId || seen.has(row.deptId)) continue
    seen.add(row.deptId)
    depts.push({deptId: row.deptId, bedQuota: row.bedQuota ?? null})
  }
  if (formData.deptId && !seen.has(formData.deptId)) {
    depts.unshift({deptId: formData.deptId, bedQuota: null})
  }

  const payload = {
    wardId: formData.wardId,
    wardName: formData.wardName,
    wardCode: formData.wardCode,
    deptId: formData.deptId,
    totalBeds: formData.totalBeds,
    status: formData.status,
    remark: formData.remark,
    depts,
  }

  submitLoading.value = true
  try {
    const res = await saveWard(payload)
    if (res.code === 200) {
      ElMessage.success(formData.wardId ? '修改成功' : '新增成功')
      visible.value = false
      emit('saved')
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
  } finally {
    submitLoading.value = false
  }
}
</script>
