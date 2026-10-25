<template>
  <el-dialog v-model="visible" :title="dialogTitle" width="600px">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <el-form-item label="工位编码" prop="stationCode">
        <el-input v-model="formData.stationCode" placeholder="如 OP-GUIDE（全局唯一）"/>
      </el-form-item>
      <el-form-item label="工位名称" prop="stationName">
        <el-input v-model="formData.stationName" placeholder="如 门诊总导诊台"/>
      </el-form-item>
      <el-form-item label="工位类别" prop="stationKind">
        <el-select v-model="formData.stationKind" style="width: 100%">
          <el-option v-for="o in STATION_KIND_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="应到岗位">
        <el-select v-model="formData.requiredStaffType" clearable placeholder="默认护理" style="width: 100%">
          <el-option v-for="o in STAFF_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
      </el-form-item>
      <el-form-item label="在岗人数">
        <el-input-number v-model="formData.capacity" :min="1" :max="20"/>
      </el-form-item>
      <el-form-item label="工位电话">
        <el-input v-model="formData.phone" placeholder="工位联系电话"/>
      </el-form-item>
      <el-form-item label="排序号">
        <el-input-number v-model="formData.sortNo" :min="0" :max="999"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="formData.status">
          <el-radio :value="1">启用</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" :rows="2" type="textarea" placeholder="工位用途说明"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button :loading="submitLoading" type="primary" @click="handleSubmit">确定</el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
// 公共工位字典新增/修改（row 为空走新增）
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {opdStationUpsert} from '@/api/opdStation'
import {STATION_KIND_OPTIONS} from '@/lib/opdStation'
import {STAFF_TYPE_OPTIONS} from '@/lib/scheduleShift'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  row: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const submitLoading = ref(false)
const dialogTitle = ref('新增工位')
const formRef = ref(null)
const formData = reactive({
  id: null, stationCode: '', stationName: '', stationKind: 1, requiredStaffType: 2,
  capacity: 1, phone: '', sortNo: 0, status: 1, remark: '',
})
const rules = {
  stationCode: [{required: true, message: '请填写工位编码', trigger: 'blur'}],
  stationName: [{required: true, message: '请填写工位名称', trigger: 'blur'}],
  stationKind: [{required: true, message: '请选择工位类别', trigger: 'change'}],
}

const resetForm = () => {
  Object.assign(formData, {
    id: null, stationCode: '', stationName: '', stationKind: 1, requiredStaffType: 2,
    capacity: 1, phone: '', sortNo: 0, status: 1, remark: '',
  })
}

watch(visible, (open) => {
  if (!open) return
  resetForm()
  const row = props.row
  if (row) {
    dialogTitle.value = '修改工位'
    Object.assign(formData, {
      id: row.id,
      stationCode: row.stationCode,
      stationName: row.stationName,
      stationKind: row.stationKind,
      requiredStaffType: row.requiredStaffType ?? 2,
      capacity: row.capacity ?? 1,
      phone: row.phone || '',
      sortNo: row.sortNo ?? 0,
      status: row.status ?? 1,
      remark: row.remark || '',
    })
  } else {
    dialogTitle.value = '新增工位'
  }
})

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    const res = await opdStationUpsert({...formData})
    if (res.code === 200) {
      ElMessage.success(formData.id ? '修改成功' : '新增成功')
      visible.value = false
      emit('saved')
    }
  } catch (e) /* 错误已由全局拦截器统一提示 */ {
  } finally {
    submitLoading.value = false
  }
}
</script>
