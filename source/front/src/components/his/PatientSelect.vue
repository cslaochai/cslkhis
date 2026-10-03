<script setup lang="js">
/**
 * PatientSelect — 全局患者搜索下拉组件
 *
 * 封装：keyword 四字段 OR 搜索、loading、富信息 option（标签/电话/预约次数等）、
 * 高亮匹配、空态文案。消费方只需 v-model + @select。
 *
 * Props:
 *   modelValue  - 绑定患者 ID（String）
 *   placeholder - 输入提示（默认"搜索患者：姓名、患者号、手机号、身份证号"）
 *   size        - el-select 尺寸 'large' | 'default' | 'small'
 *   disabled    - 是否禁用
 *   clearable   - 是否可清空（默认 true）
 *   pageSize    - 搜索返回条数（默认 10）
 *   width       - 组件宽度（CSS 值，默认 100%）
 *   searchIcon  - 是否在输入框左侧显示放大镜（默认 false；顶部全局搜索打开）
 *
 * Events:
 *   update:modelValue - v-model 双绑
 *   select            - 选中后回调完整患者对象
 *   clear             - 清空时回调
 */
import {ref, computed, watch} from 'vue'
import {Search} from '@element-plus/icons-vue'
import {getPatientDetail, getPatientList} from '@/api/patient'
import {patientGenderSymbol} from '@/lib/patientGender'
import {patientTypeText} from '@/lib/patientType'
import {tagChipText} from '@/lib/patientTag'

const props = defineProps({
  modelValue: {type: [String, Number], default: null},
  placeholder: {type: String, default: '搜索患者：姓名、患者号、手机号、身份证号'},
  size: {type: String, default: 'default'},
  disabled: {type: Boolean, default: false},
  clearable: {type: Boolean, default: true},
  pageSize: {type: Number, default: 10},
  width: {type: String, default: '100%'},
  searchIcon: {type: Boolean, default: false},
})

const emit = defineEmits(['update:modelValue', 'select', 'clear'])

const selectedId = ref(props.modelValue)
const results = ref([])
const loading = ref(false)
const keyword = ref('')

/**
 * 外部直接赋值时的回显：下拉的选项来自「搜索关键字」的结果集，如果调用方只给了 ID
 * （典型场景：从患者详情弹框跳 `/cdr?patientId=xxx`，或上游页面回填），
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
 * 选中并回调。
 *
 * 为什么不能只靠 el-select 的 @change：
 *   el-select 在「值没有变化」时不触发 change —— 当前患者已经就是这个人时，
 *   再搜一次并点选它，界面毫无反应（顶部搜索最容易撞上：搜到自己正在看的患者，
 *   人明明在别的页面，期望是切回工作台，结果点了没动静）。
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

/**
 * 丢弃已缓存的结果集（供外部在「身份变了」之后调用）。
 *
 * 什么时候必须调：切换角色 / 切换科室。分组与置顶是**后端按当前角色**算出来的
 * （只有门诊岗位才带 todayVisit* 标注），旧结果里那一组「今日就诊」是按上一个岗位的
 * 口径算的；留着它，用户切完岗位再打开下拉看到的还是旧分组 —— 就是「切了啥都没变」。
 * 不在这里重新请求：此时关键字往往已清空，白打一次接口；下次输入自然会按新身份取数。
 */
const reset = () => {
  results.value = []
  keyword.value = ''
  loading.value = false
}

defineExpose({reset})

/* ---------- 展示辅助 ---------- */
const calcAge = (birthDate) => {
  if (!birthDate) return '-'
  const b = new Date(birthDate), n = new Date()
  let age = n.getFullYear() - b.getFullYear()
  const m = n.getMonth() - b.getMonth()
  if (m < 0 || (m === 0 && n.getDate() < b.getDate())) age--
  return age >= 0 ? age : '-'
}
const patientAge = (p) => (p.age != null && p.age !== '' ? p.age : calcAge(p.birthDate))

const insuranceShortLabel = (type) => {
  const t = String(type || '')
  if (!t) return ''
  if (t.includes('职工')) return '职工医保'
  if (t.includes('居民')) return '居民医保'
  if (t.includes('农村') || t.includes('新农合')) return '新农合'
  if (t.includes('公费')) return '公费医疗'
  if (t.includes('商业')) return '商业保险'
  return t
}
const insuranceLabel = (p) => insuranceShortLabel(p.medicalInsuranceType) || (p.medicalInsuranceNo ? '医保' : '')

// 患者类型文案统一走 @/lib/patientType（曾在这里写 {1:'门诊',2:'住院',3:'急诊'}，
// 把「城镇职工医保」渲染成「住院」——那套是就诊类型的口径，不是参保性质）

const tagChipStyle = (tag) => {
  const color = tag.tagColor || '#0E9488'
  return {backgroundColor: `${color}14`, color, borderColor: `${color}40`}
}

const formatDate = (v) => (v ? String(v).replace('T', ' ').slice(0, 10) : '')

/* ---------- 今日就诊分组 ---------- */
/*
 * 后端已把今日就诊患者排在分页结果最前（排序发生在 SQL 的 LIMIT 之前），
 * 这里只负责「分组 + 标注」。
 * 【不能改成前端排序】下拉是分页的（默认 10 条），前端只能重排当前这一页 ——
 * 今天就诊的患者若按建档时间落在第 3 页，前端再怎么排也提不上来。
 *
 * 【要不要分组由后端按当前角色定，前端不判岗位】只有门诊岗位（医生/急诊医生/
 * 分诊护士/前台导诊）的返回里才有 todayVisit*；非门诊岗位后端根本不查今日队列，
 * 于是这里两组塌成一组、标题为空，自然退化成纯「全院档案」列表。
 * 所以**别在这里加角色判断**：那是第二处口径，一旦与后端白名单不一致，
 * 就会出现「顶上几个人没有标题也没标注，看着像随机排序」。
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

<template>
  <el-select
      v-model="selectedId"
      filterable
      remote
      reserve-keyword
      :fit-input-width="false"
      :no-data-text="keyword ? '未找到匹配的患者' : '输入姓名 / 患者号 / 手机号 / 身份证号搜索'"
      :placeholder="placeholder"
      :remote-method="handleSearch"
      :loading="loading"
      :size="size"
      :disabled="disabled"
      :clearable="clearable"
      class="patient-select"
      popper-class="patient-search-popper"
      :style="{width}"
      @change="handleSelect"
      @clear="handleClear"
  >
    <!-- 放大镜放在输入框内部左侧（EP 2.14 的 prefix 插槽；此前只能绝对定位在框外右侧，
         遮挡 clear 按钮且不随尺寸自适应） -->
    <template v-if="searchIcon" #prefix>
      <el-icon class="ps-prefix-icon">
        <Search/>
      </el-icon>
    </template>

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
            p.gender === 1 ? 'ps-avatar-male'
                : p.gender === 2 ? 'ps-avatar-female' : 'ps-avatar-unknown',
            p.status === 0 ? 'ps-avatar-disabled' : '',
          ]"
          >
            {{ p.patientName?.charAt(0) || '患' }}
          </div>

          <div class="ps-main">
            <!-- 今日就诊标注：只在今天确实有门诊就诊时出现。
                 没有今日就诊的患者**不写「无就诊」**——未判定 ≠ 正常，
                 这类患者直接归到下方「全院档案」组里，不伪造一个看似合法的状态。 -->
            <div
                v-if="p.todayVisitStatus != null"
                :class="['ps-today', p.todayVisitMine ? 'ps-today-mine' : '']"
            >
              <span class="ps-today-dot"></span>
              <span class="ps-today-text">{{ todayVisitLabel(p) }}</span>
            </div>

            <!-- 第一行：姓名 + 性别 + 年龄 + 患者号 + 医保 -->
            <div class="ps-line ps-line-title">
              <span class="ps-name" v-html="highlight(p.patientName)"></span>
              <span :class="['ps-gender', p.gender === 1 ? 'male' : p.gender === 2 ? 'female' : 'unknown']">
              {{ patientGenderSymbol(p.gender) }}
            </span>
              <span class="ps-age">{{ patientAge(p) }}岁</span>
              <span class="ps-chip ps-chip-code" v-html="highlight(p.patientNo)"></span>
              <span v-if="insuranceLabel(p)" class="ps-chip ps-chip-insurance">
              {{ insuranceLabel(p) }}
            </span>
              <span v-if="p.patientType" class="ps-chip ps-chip-plain">
              {{ patientTypeText(p.patientType) }}
            </span>
              <span v-if="p.status === 0" class="ps-chip ps-chip-off">已停用</span>
            </div>

            <!-- 第二行：标签 + 过敏 -->
            <div class="ps-line ps-tags">
            <span
                v-for="tag in (p.tags || [])"
                :key="tag.tagId"
                class="ps-tag"
                :style="tagChipStyle(tag)"
            >{{ tagChipText(tag) }}</span>
              <span v-if="p.allergyHistory && p.allergyHistory !== '无'" class="ps-tag ps-tag-danger">
              过敏史
            </span>
              <span v-if="!(p.tags || []).length" class="ps-empty-tip">无标签</span>
            </div>

            <!-- 第三行：联系方式 -->
            <div class="ps-line ps-meta">
            <span class="ps-meta-item">
              <span class="ps-meta-key">手机</span>{{ p.phoneMasked || '-' }}
            </span>
              <span class="ps-meta-sep">|</span>
              <span class="ps-meta-item">
              <span class="ps-meta-key">证件</span>{{ p.idCardMasked || '-' }}
            </span>
              <span class="ps-meta-sep">|</span>
              <span class="ps-meta-item">
              <span class="ps-meta-key">联系人</span>{{
                  p.contactName || '-'
                }}{{ p.contactPhone ? ' ' + p.contactPhone : '' }}
            </span>
            </div>

            <!-- 第四行：地址 + 就诊统计 -->
            <div class="ps-line ps-meta">
            <span class="ps-meta-item ps-address">
              <span class="ps-meta-key">住址</span>{{ p.address || '-' }}
            </span>
              <span class="ps-stats">
              <span class="ps-stat"><b>{{ p.appointCount ?? 0 }}</b>次预约</span>
              <span class="ps-stat"><b>{{ p.visitCount ?? 0 }}</b>次就诊</span>
              <span class="ps-stat" v-if="p.lastVisitTime">最近 {{ formatDate(p.lastVisitTime) }}</span>
            </span>
            </div>
          </div>
        </div>
      </el-option>
    </el-option-group>
  </el-select>
</template>
