<template>
  <!-- 站点新增/修改：表单、校验、提交都在这里，父页面只控显隐 + @saved 重载 -->
  <el-dialog v-model="visible" :title="isEdit ? '编辑站点' : '新增站点'" width="640px">
    <el-form ref="formRef" v-loading="detailLoading" :model="formData" :rules="rules" label-width="96px">
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="站点编码" prop="stationCode">
          <el-input v-model="formData.stationCode" placeholder="ST-1F-LAB" maxlength="32"/>
        </el-form-item>
        <el-form-item label="站点名称" prop="stationName">
          <el-input v-model="formData.stationName" placeholder="1F 检验科标本接收窗" maxlength="64"/>
        </el-form-item>
        <el-form-item label="站点类型" prop="stationType">
          <el-select v-model="formData.stationType" :fit-input-width="false" class="w-full"
                     placeholder="请选择站点类型">
            <el-option v-for="t in stationTypes" :key="t.value" :label="t.label" :value="t.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="归属科室">
          <el-select v-model="formData.deptId" :fit-input-width="false" class="w-full" clearable filterable
                     placeholder="公共站点可留空" @change="onDeptChange">
            <el-option v-for="d in deptList" :key="d.id" :label="d.fullPath || d.deptName" :value="d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="楼栋">
          <el-input v-model="formData.building" placeholder="住院部" maxlength="32"/>
        </el-form-item>
        <el-form-item label="楼层" prop="floorNo">
          <el-input-number v-model="formData.floorNo" :min="-3" :max="30" class="w-full"/>
          <div class="text-[12px] text-gray-400">地下层填负数，B1 为 -1</div>
        </el-form-item>
        <el-form-item label="取货/收货">
          <div class="flex items-center gap-4">
            <el-checkbox v-model="formData.pickupEnabled" :true-value="1" :false-value="0">可作取货点</el-checkbox>
            <el-checkbox v-model="formData.receiveEnabled" :true-value="1" :false-value="0">可作收货点</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item label="排序号">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" class="w-full"/>
        </el-form-item>
        <el-form-item label="停靠点位码">
          <el-input v-model="formData.dockCode" placeholder="厂商地图 point 编码，未对码可留空" maxlength="64"/>
        </el-form-item>
        <el-form-item label="服务时间窗">
          <el-input v-model="formData.serviceWindow" placeholder="08:00-17:00，留空为全天" maxlength="32"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="formData.enabled">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </div>
      <el-form-item label="备注">
        <el-input v-model="formData.remark" type="textarea" :rows="2" maxlength="500" show-word-limit/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button v-perm="'iot:robotStation:add'" :loading="submitLoading" type="primary" @click="handleSubmit">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {robotStationDetail, robotStationUpsert} from '@/api/robotStation'
import {flattenDeptTree, loadDeptTreeOptions} from '@/lib/deptOptions'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** row 为空 = 新增，有值 = 修改（父页面传当前行即可，详情由本组件按 id 自己取） */
  row: {type: Object, default: null},
  /** 站点类型候选，由页面从 /options 拿一次下传，避免两个组件各拉一遍 */
  stationTypes: {type: Array, default: () => []},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({get: () => props.modelValue, set: (v) => emit('update:modelValue', v)})
const isEdit = computed(() => !!props.row?.id)

const formRef = ref(null)
const detailLoading = ref(false)
const submitLoading = ref(false)
const deptList = ref([])

const emptyForm = () => ({
  id: null,
  stationCode: '',
  stationName: '',
  stationType: 1,
  building: '',
  floorNo: 1,
  deptId: null,
  deptName: '',
  pickupEnabled: 1,
  receiveEnabled: 1,
  dockCode: '',
  serviceWindow: '',
  mapX: null,
  mapY: null,
  enabled: 1,
  sortOrder: 0,
  remark: '',
})
const formData = reactive(emptyForm())

const rules = {
  stationCode: [{required: true, message: '请填写站点编码', trigger: 'blur'}],
  stationName: [{required: true, message: '请填写站点名称', trigger: 'blur'}],
  stationType: [{required: true, message: '请选择站点类型', trigger: 'change'}],
  floorNo: [{required: true, message: '请填写楼层', trigger: 'change'}],
}

const loadDepts = async () => {
  if (deptList.value.length) return
  try {
    // 站点归属可能是全院任一科室，设备科维护要看到全量
    const tree = await loadDeptTreeOptions({scope: 'ALL', mode: 'LEAF'})
    deptList.value = flattenDeptTree(tree)
  } catch (e) {
    console.error('加载科室下拉失败', e)
  }
}

const onDeptChange = (id) => {
  const hit = deptList.value.find((d) => String(d.id) === String(id))
  formData.deptName = hit ? hit.deptName : ''
}

watch(visible, async (open) => {
  if (!open) return
  Object.assign(formData, emptyForm())
  formRef.value?.clearValidate()
  await loadDepts()
  if (!props.row?.id) return
  detailLoading.value = true
  try {
    const res = await robotStationDetail(props.row.id)
    if (res.code === 200 && res.data) {
      // 只回填 DTO 字段：详情 VO 里的 xxxLabel/createTime 原样塞回来会让 upsert 撞 unknown field
      const d = res.data
      Object.assign(formData, {
        id: d.id,
        stationCode: d.stationCode,
        stationName: d.stationName,
        stationType: d.stationType,
        building: d.building,
        floorNo: d.floorNo,
        deptId: d.deptId ?? null,
        deptName: d.deptName ?? '',
        pickupEnabled: d.pickupEnabled ?? 1,
        receiveEnabled: d.receiveEnabled ?? 1,
        dockCode: d.dockCode ?? '',
        serviceWindow: d.serviceWindow ?? '',
        mapX: d.mapX ?? null,
        mapY: d.mapY ?? null,
        enabled: d.enabled ?? 1,
        sortOrder: d.sortOrder ?? 0,
        remark: d.remark ?? '',
      })
    }
  } catch (e) {
    console.error('加载站点详情失败', e)
  } finally {
    detailLoading.value = false
  }
})

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    await robotStationUpsert({...formData})
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    visible.value = false
    emit('saved')
  } catch (e) {
    console.error('保存站点失败', e)
    ElMessage.error(e?.response?.data?.message || '保存失败')
  } finally {
    submitLoading.value = false
  }
}
</script>
