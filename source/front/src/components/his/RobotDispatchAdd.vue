<template>
  <!-- 新建配送任务单：调度台（RobotDispatchView）与全局叫车浮窗（RobotCallFloat）共用，
       表单/校验/提交都在这里，调用方只控显隐 -->
  <el-dialog v-model="visible" title="新建配送任务单" width="600px" @closed="resetForm">
    <el-form ref="formRef" v-loading="optionsLoading" :model="form" :rules="rules" label-width="96px">
      <el-form-item label="发起站点" prop="applyStationId">
        <el-select v-model="form.applyStationId" :fit-input-width="false" class="w-full" filterable
                   placeholder="谁叫的车" @change="onApplyChange">
          <el-option v-for="s in stations" :key="s.id" :label="stationLabel(s)" :value="s.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="物品品类" prop="goodsType">
        <el-select v-model="form.goodsType" :fit-input-width="false" class="w-full" @change="onGoodsChange">
          <el-option v-for="g in goodsTypes" :key="g.code" :value="g.code"
                     :label="`${g.label}（${g.unitWeightKg}kg/箱，单趟最多 ${g.maxBoxCount} 箱）`"/>
        </el-select>
      </el-form-item>
      <el-form-item label="物品明细" prop="goodsName">
        <el-input v-model="form.goodsName" placeholder="如 0.9%氯化钠注射液 250ml×24 瓶" maxlength="200"/>
      </el-form-item>
      <el-form-item label="取货点" prop="fromStationId">
        <el-select v-model="form.fromStationId" :fit-input-width="false" class="w-full" filterable
                   placeholder="机器人去哪装货">
          <el-option v-for="s in pickStations" :key="s.id" :label="stationLabel(s)" :value="s.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="收货点" prop="toStationId">
        <el-select v-model="form.toStationId" :fit-input-width="false" class="w-full" filterable
                   placeholder="送到哪个站点">
          <el-option v-for="s in receiveStations" :key="s.id" :label="stationLabel(s)" :value="s.id"/>
        </el-select>
      </el-form-item>
      <el-form-item label="目标楼层">
        <div class="flex items-center gap-2 text-[13px] text-gray-600">
          <el-tag v-if="toStation" size="small" effect="plain">{{ positionText(toStation) }}</el-tag>
          <span v-else class="text-gray-400">选收货点后自动带出，不再手填</span>
        </div>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-radio-group v-model="form.priority">
          <el-radio v-for="p in priorities" :key="p.value" :value="p.value">{{ p.label }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="箱数" prop="boxCount">
        <div class="flex items-center gap-3">
          <el-input-number v-model="form.boxCount" :min="1" :max="maxBox" style="width: 130px"/>
          <span class="text-[13px] text-gray-500">预计总重 <b class="text-gray-800">{{ estWeight }}</b> kg</span>
          <span class="text-[12px] text-gray-400">按品类单箱重量 × 箱数</span>
        </div>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="submit">提交并派车</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import {computed, reactive, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {robotDispatchCreate} from '@/api/robotDispatch'
import {loadRobotOptions} from '@/lib/robotDispatchOptions'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 当前岗位科室名：打开时自动把发起站点/取货点预填成本科室站点 */
  presetDeptName: {type: String, default: ''},
})
const emit = defineEmits(['update:modelValue', 'saved'])
const visible = computed({get: () => props.modelValue, set: (v) => emit('update:modelValue', v)})

const optionsLoading = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const stations = ref([])
const goodsTypes = ref([])
const priorities = ref([])

const form = reactive({
  applyStationId: null,
  goodsType: '药品',
  goodsName: '',
  fromStationId: null,
  toStationId: null,
  priority: 1,
  boxCount: 1,
  remark: '',
})

const rules = {
  applyStationId: [{required: true, message: '请选择发起站点', trigger: 'change'}],
  goodsType: [{required: true, message: '请选择物品品类', trigger: 'change'}],
  goodsName: [{required: true, message: '请填写物品明细', trigger: 'blur'}],
  fromStationId: [{required: true, message: '请选择取货点', trigger: 'change'}],
  toStationId: [{required: true, message: '请选择收货点', trigger: 'change'}],
  priority: [{required: true, message: '请选择优先级', trigger: 'change'}],
  boxCount: [{required: true, message: '请填写箱数', trigger: 'change'}],
}

const pickStations = computed(() => stations.value.filter((s) => s.pickupEnabled === 1))
const receiveStations = computed(() => stations.value.filter((s) => s.receiveEnabled === 1))
const toStation = computed(() => stations.value.find((s) => String(s.id) === String(form.toStationId)))
const selectedGoods = computed(() => goodsTypes.value.find((g) => g.code === form.goodsType))
const maxBox = computed(() => selectedGoods.value?.maxBoxCount || 1)
const estWeight = computed(() => {
  const unit = selectedGoods.value?.unitWeightKg || 0
  return Math.round(unit * (form.boxCount || 0) * 10) / 10
})

/** 楼层文本：-1 → B1，12 → 12F */
const positionText = (s) => {
  if (!s) return ''
  const floor = s.floorNo == null ? '' : s.floorNo < 0 ? `B${Math.abs(s.floorNo)}` : `${s.floorNo}F`
  return [s.building, floor].filter(Boolean).join(' ')
}
const stationLabel = (s) => `${s.stationName}${positionText(s) ? ' · ' + positionText(s) : ''}`

const loadOptions = async () => {
  optionsLoading.value = true
  try {
    const data = await loadRobotOptions()
    stations.value = data.stations || []
    goodsTypes.value = data.goodsTypes || []
    priorities.value = data.priorities || []
    if (!goodsTypes.value.find((g) => g.code === form.goodsType) && goodsTypes.value.length) {
      form.goodsType = goodsTypes.value[0].code
    }
  } catch (e) {
    console.error('加载叫车候选项失败', e)
    ElMessage.error('站点主数据加载失败，请稍后重试')
  } finally {
    optionsLoading.value = false
  }
}

/** 选发起站点后，取货点没填就跟着带过去（多数场景发起人就在取货点） */
const onApplyChange = (id) => {
  const hit = stations.value.find((s) => String(s.id) === String(id))
  if (hit && !form.fromStationId && hit.pickupEnabled === 1) {
    form.fromStationId = hit.id
  }
}

const onGoodsChange = () => {
  if (form.boxCount > maxBox.value) form.boxCount = maxBox.value
}

/**
 * 按岗位科室名匹配默认站点：先按站点归属科室名精确匹配，再退到站点名包含科室名。
 * 匹配不上就留空让用户选，不塞假默认。
 */
const presetStations = () => {
  const dept = (props.presetDeptName || '').trim()
  if (!dept || !stations.value.length) return
  const hit = stations.value.find((s) => (s.deptName || '').trim() === dept)
      || stations.value.find((s) => (s.stationName || '').includes(dept))
  if (!hit) return
  if (hit.pickupEnabled === 1) {
    form.applyStationId = hit.id
    form.fromStationId = hit.id
  }
}

const resetForm = () => {
  Object.assign(form, {
    applyStationId: null, goodsType: '药品', goodsName: '',
    fromStationId: null, toStationId: null, priority: 1, boxCount: 1, remark: '',
  })
  formRef.value?.clearValidate()
}

watch(() => props.modelValue, async (v) => {
  if (!v) return
  resetForm()
  await loadOptions()
  presetStations()
})

const submit = async () => {
  const ok = await formRef.value.validate().catch(() => false)
  if (!ok) return
  submitting.value = true
  try {
    // autoAssign=true：提交即由后端派单算法选车，不需要人工先派
    await robotDispatchCreate({...form, autoAssign: true})
    ElMessage.success('任务单已提交并派车')
    visible.value = false
    emit('saved')
  } catch (e) {
    console.error('新建配送任务单失败', e)
    ElMessage.error(e?.response?.data?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}
</script>
