<template>
  <el-dialog
      v-model="visible"
      destroy-on-close
      title="医生详情"
      width="700px"
  >
    <div v-loading="detailLoading">
      <template v-if="detailData">
        <!-- 基本信息 -->
        <div class="mb-4 rounded-lg border border-slate-200 bg-slate-50 p-4">
          <div class="flex items-center gap-4">
            <div
                class="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-blue-600 text-xl font-bold text-white">
              {{ detailData.empName?.charAt(0) }}
            </div>
            <div class="flex-1">
              <div class="flex items-center gap-3">
                <h3 class="text-lg font-semibold text-slate-900">{{ detailData.empName }}</h3>
                <el-tag :type="detailData.status === 1 ? 'success' : 'danger'" size="small">
                  {{ detailData.status === 1 ? '在岗' : '停用' }}
                </el-tag>
                <el-tag v-if="detailData.isExpert === 1" size="small" type="warning">专家</el-tag>
              </div>
              <p class="mt-1 text-sm text-slate-500">
                {{ getDictLabelByValue(titleOptions, detailData.title) }} · {{ detailData.deptName }}
              </p>
            </div>
          </div>
        </div>

        <!-- 详细信息 -->
        <div class="grid grid-cols-2 gap-4 py-2 text-sm">
          <div><span class="text-slate-400">工号：</span><span class="font-medium text-slate-700">{{
              detailData.empCode
            }}</span></div>
          <div><span class="text-slate-400">性别：</span><span
              class="font-medium text-slate-700">{{ patientGenderText(detailData.gender) }}</span></div>
          <div><span class="text-slate-400">联系电话：</span><span
              class="font-medium text-slate-700">{{ detailData.phone }}</span></div>
          <div><span class="text-slate-400">邮箱：</span><span class="font-medium text-slate-700">{{
              detailData.email
            }}</span></div>
          <div><span class="text-slate-400">学历：</span><span class="font-medium text-slate-700">{{
              getDictLabelByValue(educationOptions, detailData.education)
            }}</span></div>
          <div><span class="text-slate-400">入职日期：</span><span
              class="font-medium text-slate-700">{{ detailData.hireDate }}</span></div>
          <div><span class="text-slate-400">职位：</span><span class="font-medium text-slate-700">{{
              getDictLabelByValue(positionOptions, detailData.position)
            }}</span></div>
          <div><span class="text-slate-400">专家号费用：</span><span
              class="font-medium text-slate-700">¥{{ detailData.expertPrice || 0 }}</span></div>
          <div class="col-span-2"><span class="text-slate-400">执业科室：</span><span
              class="font-medium text-slate-700">{{
              (detailData.deptNames && detailData.deptNames.length > 0) ? detailData.deptNames.join('、') : detailData.deptName
            }}</span></div>
          <div class="col-span-2"><span class="text-slate-400">擅长：</span><span
              class="font-medium text-slate-700">{{ detailData.specialty }}</span></div>
        </div>
      </template>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {getEmployeeDetail} from '@/api/system'
import {getDictLabelByValue} from '@/lib/doctors'
import {patientGenderText} from '@/lib/patientGender'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  id: {type: [String, Number], default: ''},
  // 字典由页面一次性加载（列表列也在用），这里只读取，不重复请求
  titleOptions: {type: Array, default: () => []},
  educationOptions: {type: Array, default: () => []},
  positionOptions: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const detailLoading = ref(false)
const detailData = ref(null)

// 打开时自己按 id 取详情，父页面只管开关
watch(visible, async (open) => {
  if (!open) return
  detailLoading.value = true
  detailData.value = null
  try {
    const res = await getEmployeeDetail(props.id)
    if (res.code === 200 && res.data) {
      detailData.value = res.data
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    detailLoading.value = false
  }
})
</script>
