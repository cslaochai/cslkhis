<template>
  <el-select
      v-model="selectedId"
      :clearable="clearable"
      :disabled="disabled"
      :fit-input-width="false"
      :loading="loading"
      :no-data-text="keyword ? '未找到匹配的患者' : '输入姓名 / 患者号 / 手机号 / 身份证号搜索'"
      :placeholder="placeholder"
      :remote-method="handleSearch"
      :size="size"
      :style="{width}"
      class="patient-select"
      filterable
      popper-class="appointment-patient-popper"
      remote
      reserve-keyword
      @change="handleSelect"
      @clear="handleClear"
  >
    <!-- 分组渲染：今日就诊排最前（后端已按「我的今日 → 今日其他科室 → 其余」排序）。
         option 内容只写一份，靠 v-for over groups 复用。 -->
    <el-option-group
        v-for="g in groupedResults"
        :key="g.key"
        :label="g.label"
    >
      <el-option
          v-for="p in g.list"
          :key="p.id"
          :label="`${p.patientName} (${p.patientNo})`"
          :value="p.id"
          @click="handleOptionClick(p)"
      >
        <div class="ps-item">
          <!-- 头像 -->
          <div
              :class="[
            'ps-avatar',
            'ps-avatar-' + patientAvatarTone(p.gender),
            p.status === 0 ? 'ps-avatar-disabled' : '',
          ]"
          >
            {{ p.patientName?.charAt(0) || '患' }}
          </div>

          <div class="ps-main">
            <!-- 今日就诊标注：挂号窗口最该先看到的一条 —— 今天已经有号的人再挂，
                 大概率是复诊或当日回诊，选人时就得看见，而不是挂完单才发现 -->
            <div
                v-if="p.todayVisitStatus != null"
                :class="['ps-today', p.todayVisitMine ? 'ps-today-mine' : '']"
            >
              <span class="ps-today-dot"></span>
              <span class="ps-today-text">{{ todayVisitLabel(p) }}</span>
            </div>

            <!-- 第一行：姓名 + 性别 + 年龄 + 患者号 + 费别 -->
            <div class="ps-line ps-line-title">
              <span class="ps-name" v-html="highlight(p.patientName)"></span>
              <span :class="['ps-gender', patientAvatarTone(p.gender)]">
              {{ patientGenderSymbol(p.gender) }}
            </span>
              <span class="ps-age">{{ patientAgeText(patientAge(p)) }}</span>
              <span class="ps-chip ps-chip-code" v-html="highlight(p.patientNo)"></span>
              <span v-if="feeShortLabel(p.medicalInsuranceType)" class="ps-chip ps-chip-insurance">
              {{ feeShortLabel(p.medicalInsuranceType) }}
            </span>
              <span v-if="p.status === 0" class="ps-chip ps-chip-off">已停用</span>
            </div>

            <!-- 第二行：联系方式 + 就诊足迹（挂号员确认身份、判断初复诊就靠这两样） -->
            <div class="ps-line ps-meta">
            <span class="ps-meta-item">
              <span class="ps-meta-key">手机</span>{{ p.phoneMasked || '-' }}
            </span>
              <span class="ps-meta-sep">|</span>
              <span class="ps-meta-item">
              <span class="ps-meta-key">最近就诊</span>{{ lastVisitLabel(p) }}
            </span>
              <span class="ps-meta-sep">|</span>
              <span class="ps-meta-item">
              <span class="ps-meta-key">预约</span>{{ p.appointCount ?? 0 }} 次
            </span>
            </div>
          </div>
        </div>
      </el-option>
    </el-option-group>
  </el-select>
</template>

<script lang="js" setup>
import {computed, ref, watch} from 'vue'
import {getPatientDetail, getPatientList} from '@/api/patient'
import {patientAgeText, patientAvatarTone, patientGenderSymbol} from '@/lib/patientGender'
import {feeShortLabel} from '@/lib/payType'

// 接口契约与 PatientSelect 保持一致（v-model + @select + @clear），
// 换用本组件不需要改调用方一行代码；差异只在展示信息与下拉宽度。
const props = defineProps({
  modelValue: {type: [String, Number], default: null},
  placeholder: {type: String, default: '搜索患者：姓名、患者号、手机号、身份证号'},
  size: {type: String, default: 'default'},
  disabled: {type: Boolean, default: false},
  clearable: {type: Boolean, default: true},
  pageSize: {type: Number, default: 10},
  width: {type: String, default: '100%'},
})

const emit = defineEmits(['update:modelValue', 'select', 'clear'])

const selectedId = ref(props.modelValue)
const results = ref([])
const loading = ref(false)
const keyword = ref('')

/**
 * 外部直接赋值时的回显（与 PatientSelect 同一口径）：
 * 下拉的选项来自搜索结果，调用方只给了 ID 时（看板预填、上游页面回填），
 * 选项里没有这条记录，el-select 就只能显示一串雪花 ID。
 * 这里按 ID 反查一次并塞进选项集，保证显示的是姓名而不是 ID。
 */
const syncLabel = async (id) => {
  if (!id) return
  if (results.value.some(p => String(p.id) === String(id))) return
  try {
    const res = await getPatientDetail(id)
    if (res?.code === 200 && res.data?.id) {
      results.value = [res.data, ...results.value]
    }
  } catch (e) {
    console.error('按ID回显患者失败:', e)
  }
}

// 双向同步
watch(() => props.modelValue, v => {
  selectedId.value = v
  syncLabel(v)
}, {immediate: true})
watch(selectedId, v => {
  emit('update:modelValue', v)
})

/* ---------- 搜索 ---------- */
const handleSearch = async (query) => {
  keyword.value = query || ''
  if (!query) {
    results.value = [];
    return
  }
  loading.value = true
  try {
    const res = await getPatientList({keyword: query, pageNum: 1, pageSize: props.pageSize})
    results.value = res.data?.records || []
  } catch (e) {
    console.error('搜索患者失败:', e)
  } finally {
    loading.value = false
  }
}

/**
 * 选中并回调（与 PatientSelect 同一去重口径）。
 *
 * 为什么不能只靠 el-select 的 @change：
 *   el-select 在「值没有变化」时不触发 change —— 当前患者已经就是这个人时，
 *   再搜一次并点选它，界面毫无反应（挂号窗口换一个人重挂时最容易撞上）。
 * 所以在 option 上直接接一次点击，并做 400ms 去重，避免和 @change 重复回调。
 */
let lastEmitted = {id: null, at: 0}
const emitSelect = (patient) => {
  const now = Date.now()
  if (lastEmitted.id === patient.id && now - lastEmitted.at < 400) return
  lastEmitted = {id: patient.id, at: now}
  emit('select', patient)
}

const handleOptionClick = (p) => {
  if (p?.id != null) emitSelect(p)
}

const handleSelect = (patientId) => {
  if (!patientId) return
  // 用字符串比较：id 出参是雪花 ID 序列化后的字符串，两端口径不一致时会静默找不到人
  const patient = results.value.find(p => String(p.id) === String(patientId))
  if (patient) emitSelect(patient)
}

const handleClear = () => {
  results.value = []
  keyword.value = ''
  emit('clear')
}

/* ---------- 展示辅助 ---------- */
const calcAge = (birthDate) => {
  if (!birthDate) return null
  const b = new Date(birthDate), n = new Date()
  let age = n.getFullYear() - b.getFullYear()
  const m = n.getMonth() - b.getMonth()
  if (m < 0 || (m === 0 && n.getDate() < b.getDate())) age--
  return age >= 0 ? age : null
}
const patientAge = (p) => (p.age != null && p.age !== '' ? p.age : calcAge(p.birthDate))

const formatDate = (v) => (v ? String(v).replace('T', ' ').slice(0, 10) : '')

/** 最近就诊：日期 + 科室；从无就诊记录的患者明说"无"，不要留空让人猜 */
const lastVisitLabel = (p) => {
  if (!p.lastVisitTime) return '无就诊记录'
  const dept = p.lastVisitDeptName ? ` ${p.lastVisitDeptName}` : ''
  return `${formatDate(p.lastVisitTime)}${dept}`
}

/* ---------- 今日就诊分组（与 PatientSelect 同一口径，别在这里改） ---------- */
/*
 * 后端已把今日就诊患者排在分页结果最前（排序发生在 SQL 的 LIMIT 之前），
 * 这里只负责「分组 + 标注」。
 * 【不能改成前端排序】下拉是分页的（默认 10 条），前端只能重排当前这一页 ——
 * 今天就诊的患者若按建档时间落在第 3 页，前端再怎么排也提不上来。
 *
 * 【要不要分组由后端按当前角色定，前端不判岗位】只有门诊岗位的返回里才有
 * todayVisit*；非门诊岗位后端根本不查今日队列，于是这里两组塌成一组、标题为空，
 * 自然退化成纯「全院档案」列表。所以**别在这里加角色判断**。
 */
const todayResults = computed(() => results.value.filter(p => p.todayVisitStatus != null))
const historyResults = computed(() => results.value.filter(p => p.todayVisitStatus == null))

const groupedResults = computed(() => {
  const groups = []
  if (todayResults.value.length) {
    groups.push({
      key: 'today',
      label: `今日就诊（${todayResults.value.length}）`,
      list: todayResults.value,
    })
  }
  if (historyResults.value.length) {
    // 只有一组时不打标题：「全院档案」这四个字比内容还长，纯干扰
    groups.push({
      key: 'history',
      label: todayResults.value.length ? `全院档案（${historyResults.value.length}）` : '',
      list: historyResults.value,
    })
  }
  return groups
})

/** 今日就诊标注：状态 · 科室 · 医生 · 序号（缺项不占位，避免出现一连串「·」） */
const todayVisitLabel = (p) => {
  const parts = [p.todayVisitText, p.todayVisitDept, p.todayVisitDoctor, p.todayVisitQueueNo]
  return parts.filter(Boolean).join(' · ')
}

// 关键字高亮
const escapeHtml = (s) => String(s ?? '').replace(/[&<>"']/g, c => (
    {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[c]
))
const highlight = (text) => {
  const raw = String(text ?? '')
  if (!keyword.value) return escapeHtml(raw)
  const idx = raw.toLowerCase().indexOf(keyword.value.toLowerCase())
  if (idx < 0) return escapeHtml(raw)
  const hit = raw.slice(idx, idx + keyword.value.length)
  return `${escapeHtml(raw.slice(0, idx))}<em class="ps-hit">${escapeHtml(hit)}</em>${escapeHtml(raw.slice(idx + hit.length))}`
}
</script>
