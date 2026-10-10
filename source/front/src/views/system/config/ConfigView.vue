<template>
  <div class="p-6">
    <el-card v-loading="loading" shadow="never">
      <template #header>
        <span class="font-medium text-slate-700">基础配置</span>
      </template>
      <el-form ref="formRef" :model="basicConfig" :rules="rules" class="max-w-xl" label-width="120px">
        <el-form-item label="医院名称" prop="hospitalName">
          <el-input v-model="basicConfig.hospitalName"/>
        </el-form-item>
        <el-form-item label="医院地址" prop="hospitalAddress">
          <el-input v-model="basicConfig.hospitalAddress"/>
        </el-form-item>
        <el-form-item label="联系电话" prop="hospitalPhone">
          <el-input v-model="basicConfig.hospitalPhone"/>
        </el-form-item>
        <el-form-item label="邮箱" prop="hospitalEmail">
          <el-input v-model="basicConfig.hospitalEmail"/>
        </el-form-item>
        <el-form-item>
          <el-button v-perm="'system:config:add'" :icon="Check" :loading="saving" type="primary"
                     @click="handleSaveBasic">保存配置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script lang="js" setup>
import {onMounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Check} from '@element-plus/icons-vue'
import {getHospitalConfig, saveHospitalConfig} from '../../../api/system'

const loading = ref(false)
const saving = ref(false)
const formRef = ref()

const basicConfig = ref({
  hospitalName: '',
  hospitalAddress: '',
  hospitalPhone: '',
  hospitalEmail: '',
})

const rules = {
  hospitalName: [{required: true, message: '请输入医院名称', trigger: 'blur'}],
  hospitalAddress: [{required: true, message: '请输入医院地址', trigger: 'blur'}],
  hospitalPhone: [{required: true, message: '请输入联系电话', trigger: 'blur'}],
}

const loadConfig = async () => {
  loading.value = true
  try {
    const res = await getHospitalConfig()
    if (res.code === 200 && res.data) {
      basicConfig.value = {...basicConfig.value, ...res.data}
    }
  } finally {
    loading.value = false
  }
}

const handleSaveBasic = async () => {
  await formRef.value.validate()
  saving.value = true
  try {
    const res = await saveHospitalConfig(basicConfig.value)
    if (res.code === 200) {
      ElMessage.success('基础配置保存成功')
    }
  } finally {
    saving.value = false
  }
}

onMounted(loadConfig)
</script>
