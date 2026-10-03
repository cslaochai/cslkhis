<script setup lang="ts">
import {onMounted, ref} from 'vue'
import {Plus, Refresh, Search} from '@element-plus/icons-vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  createPatient,
  deletePatient,
  getPatientDetail,
  getPatientList,
  getPatientTags,
  updatePatient
} from '@/api/patient'
import {getDictDataList, getPatientTagList} from '@/api/system'
import {DICT_TYPE} from '@/lib/dict-cache'
import {
  birthDateFromIdCard,
  isIdCardBirthDateLegal,
  isIdCardChecksumLegal,
  isIdCardFormatLegal,
  isPatientGenderCollected,
  isPhoneLegal,
  PATIENT_GENDER_OPTIONS,
  patientAgeText,
  patientGenderText
} from '@/lib/patientGender'
import {PATIENT_TYPE_OPTIONS} from '@/lib/patientType'
import {TAG_LIST_VISIBLE_LIMIT, tagChipText, tagChipTitle} from '@/lib/patientTag'
// 患者详情弹框统一走通用组件（原页面内联实现已删除：它读的 medicalRecords / prescriptions /
// inspections / laboratories 字段并不在返回体里，4 个 tab 恒为空）
import PatientDetailDialog from '@/components/his/PatientDetailDialog.vue'
import { PAGE_SIZES, DEFAULT_PAGE_SIZE } from '@/lib/pagination'
import { useTableMaxHeight } from '@/lib/useTableMaxHeight'

interface Patient {
  id: number
  patientNo: string
  patientName: string
  gender: number
  age: number
  phone: string
  // 列表接口电话回脱敏值、明文恒 null；编辑弹窗走 getById 补全量
  phoneMasked?: string | null
  bloodType: string
  allergyHistory: string
  address: string
  // 列表接口身份证回脱敏值、明文恒 null；编辑弹窗走 getById 补全量
  idCard?: string | null
  idCardMasked?: string | null
  createTime: string
  status: number
  // 列表接口新增的展示字段（后端批量计算 / 脱敏，前端不自造）
  medicalInsuranceType?: string
  // 医保卡号明文在列表接口恒为 null，展示用脱敏值
  medicalInsuranceNoMasked?: string | null
  lastVisitTime?: string
  lastVisitDeptName?: string
  lastVisitDoctorName?: string
  firstVisitTime?: string
  firstVisitDeptName?: string
  firstVisitDoctorName?: string
}

const searchForm = ref({
  patientName: '',
  patientNo: '',
  phone: '',
  gender: null as number | null,
  patientType: null as number | null,
  tagId: null as number | null,
})
// 详情弹框：selectedPatient 只用于「打开瞬间先用列表行数据渲染头部」避免闪白，
// 完整数据由 PatientDetailDialog 按 patientId 自己取（主档详情 + CDR 全景时间轴）
const selectedPatient = ref<Patient | null>(null)
const selectedPatientId = ref<string>('')
const showDetailDialog = ref(false)
const showAddDialog = ref(false)
const showEditDialog = ref(false)
const editingPatient = ref<any>(null)
const loading = ref(false)
const patients = ref<Patient[]>([])

const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
})
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const { queryCardRef, footerRef, tableMaxHeight } = useTableMaxHeight()

const statusColors: Record<number, string> = {
  0: 'bg-slate-100 text-slate-600 border-slate-200',
  1: 'bg-blue-100 text-blue-700 border-blue-200',
}

const statusMap: Record<number, string> = {
  0: '停用',
  1: '正常',
}

// 性别文案统一走 lib/patientGender（sys_gender 口径：1男 2女 9未知，0 等异常码值渲染成「未知(0)」而不是"女"）

// 婚姻状况文案统一走 @/lib/patientField（0未婚/1已婚/2离异/3丧偶，异常码值渲染「未知(n)」）。
// 原地的 maritalStatusMap 从未被引用，已删除 —— 口径只留 lib 一份。

// 患者类型文案走 @/lib/patientType（曾在这里写 {1:'普通患者',2:'医保患者',3:'公费患者'}，
// 2/3 实际是「城镇职工医保 / 城乡居民医保」，不是"医保患者 / 公费患者"）

const newPatient = ref({
  patientName: '',
  // 不预设性别：默认成"男"等于静默编造性别，改为必须显式选择（含「未知」）
  gender: null as number | null,
  birthDate: '',
  phone: '',
  idCard: '',
  bloodType: '',
  allergyHistory: '',
  address: '',
  nation: '',
  occupation: '',
  maritalStatus: null as number | null,
  patientType: 1,
  medicalInsuranceType: '',
  medicalInsuranceNo: '',
  contactName: '',
  contactPhone: '',
  contactRelation: '',
})

const handleSearch = () => {
  pagination.value.pageNum = 1
  loadData()
}

const handleReset = () => {
  searchForm.value = {
    patientName: '',
    patientNo: '',
    phone: '',
    gender: null,
    patientType: null,
    tagId: null,
  }
  handleSearch()
}

const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  pagination.value.pageNum = 1
  loadData()
}

const handleCurrentChange = (val: number) => {
  pagination.value.pageNum = val
  loadData()
}

const loadData = async () => {
  loading.value = true
  try {
    const params: any = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    }
    if (searchForm.value.patientName) params.patientName = searchForm.value.patientName
    if (searchForm.value.patientNo) params.patientNo = searchForm.value.patientNo
    if (searchForm.value.phone) params.phone = searchForm.value.phone
    if (searchForm.value.gender !== null) params.gender = searchForm.value.gender
    if (searchForm.value.patientType !== null) params.patientType = searchForm.value.patientType
    if (searchForm.value.tagId) params.tagId = searchForm.value.tagId
    const res = await getPatientList(params)
    patients.value = res.data?.records || []
    pagination.value.total = res.data?.total || 0
    // 批量加载患者标签
    if (patients.value.length > 0) {
      const patientIds = patients.value.map(p => p.id)
      await loadPatientTagsBatch(patientIds)
    }
  } catch (error) {
    console.error('加载患者列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleViewDetail = (row: Patient) => {
  if (!row?.id) return
  selectedPatient.value = row
  selectedPatientId.value = String(row.id)
  showDetailDialog.value = true
}

/**
 * 身份证填完 → 把出生日期带出来。录入时少填一格，也避免 birth_date 空着：
 * age 是后端拿 birth_date 算的，空着列表就显示「—」；EMPI 的「同名+同性别+同出生日期」
 * 那一档匹配也依赖它。已经手动填过出生日期就不覆盖。
 */
const syncBirthDateFromIdCard = () => {
  if (newPatient.value.birthDate) return
  const d = birthDateFromIdCard(newPatient.value.idCard)
  if (d) newPatient.value.birthDate = d
}

const handleSubmit = async () => {
  if (!newPatient.value.patientName) {
    ElMessage.warning('请输入患者姓名')
    return
  }
  // 性别必须显式选：以前下拉默认选中"男"，不碰它就是男 —— 那不是必填，是静默编造性别
  if (!isPatientGenderCollected(newPatient.value.gender)) {
    ElMessage.warning('请选择性别（确实没问到请选「未知」）')
    return
  }
  if (!newPatient.value.idCard) {
    ElMessage.warning('请输入身份证号')
    return
  }
  if (!isIdCardFormatLegal(newPatient.value.idCard)) {
    ElMessage.warning('身份证号格式不正确：应为 18 位（末位可为 X）')
    return
  }
  // 出生日期单独判：校验位对了不代表日期存在（19990230 这种），提示要指到点子上
  if (!isIdCardBirthDateLegal(newPatient.value.idCard)) {
    ElMessage.warning('身份证号中的出生日期不存在，请核对')
    return
  }
  if (!isIdCardChecksumLegal(newPatient.value.idCard)) {
    ElMessage.warning('身份证号校验位不正确，请核对')
    return
  }
  // 手机号放宽为可空（老年患者、三无患者常见）：但填了就必须是合法号码
  if (newPatient.value.phone && !isPhoneLegal(newPatient.value.phone)) {
    ElMessage.warning('手机号格式不正确：应为 11 位手机号')
    return
  }

  try {
    await createPatient(newPatient.value)
    ElMessage.success('新增成功')
    showAddDialog.value = false
    loadData()
    newPatient.value = {
      patientName: '',
      gender: null,
      birthDate: '',
      phone: '',
      idCard: '',
      bloodType: '',
      allergyHistory: '',
      address: '',
      nation: '',
      occupation: '',
      maritalStatus: null,
      patientType: 1,
      medicalInsuranceType: '',
      medicalInsuranceNo: '',
      contactName: '',
      contactPhone: '',
      contactRelation: '',
    }
  } catch (error) {
    ElMessage.error(error.message || '新增失败')
  }
}

const handleDelete = async (row: Patient) => {
  try {
    await ElMessageBox.confirm('确定要删除该患者吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deletePatient(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

const handleEdit = async (row: Patient) => {
  editingPatient.value = {...row}
  showEditDialog.value = true
  // 列表接口不回传医保卡号明文（服务端已脱敏、明文置 null）：
  // 编辑弹窗单独取主档全量补齐，否则用户看到空的「医保卡号」会误以为患者没填，
  // 且直接回传 null 虽然不会洗掉库里数据（MP 跳过 null 字段），但体验是错的
  try {
    const res = await getPatientDetail(row.id)
    if (res.data) {
      editingPatient.value = {...res.data}
    }
  } catch (error) {
    console.error('加载患者全量信息失败（编辑表单按列表行数据展示）:', error)
  }
}

const handleEditSubmit = async () => {
  if (!editingPatient.value.patientName) {
    ElMessage.warning('请输入患者姓名')
    return
  }
  if (!isPatientGenderCollected(editingPatient.value.gender)) {
    ElMessage.warning('请选择性别（确实没问到请选「未知」；历史脏码值 0 请顺手修正）')
    return
  }
  if (!editingPatient.value.idCard) {
    ElMessage.warning('请输入身份证号')
    return
  }
  // 修改时只验 18 位格式、不验校验位：存量库里有 32 条造数编的身份证，
  // 卡校验位会让这些老档连改电话都保存不了（与后端 PatientProfileValidator 同口径）
  if (!isIdCardFormatLegal(editingPatient.value.idCard)) {
    ElMessage.warning('身份证号格式不正确：应为 18 位（末位可为 X）')
    return
  }
  if (editingPatient.value.phone && !isPhoneLegal(editingPatient.value.phone)) {
    ElMessage.warning('手机号格式不正确：应为 11 位手机号')
    return
  }
  try {
    await updatePatient(editingPatient.value)
    ElMessage.success('修改成功')
    showEditDialog.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.message || '修改失败')
  }
}

const handleCopyName = (name: string) => {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(name).then(() => {
      ElMessage.success('已复制')
    }).catch(() => {
      ElMessage.error('复制失败')
    })
  } else {
    // 降级方案
    const textarea = document.createElement('textarea')
    textarea.value = name
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success('已复制')
  }
}

// 医保类型字典
const medicalInsuranceTypes = ref<any[]>([])
const loadMedicalInsuranceTypes = async () => {
  try {
    const res = await getDictDataList(DICT_TYPE.MEDICAL_INSURANCE_TYPE)
    medicalInsuranceTypes.value = res.data || []
  } catch (error) {
    console.error('加载医保类型字典失败:', error)
  }
}

// 民族字典
const nationalityList = ref<any[]>([])
const loadNationalityList = async () => {
  try {
    const res = await getDictDataList(DICT_TYPE.SYS_NATIONALITY)
    nationalityList.value = res.data || []
  } catch (error) {
    console.error('加载民族字典失败:', error)
  }
}

// 与患者关系字典
const patientRelationList = ref<any[]>([])
const loadPatientRelationList = async () => {
  try {
    const res = await getDictDataList(DICT_TYPE.SYS_PATIENT_RELATION)
    patientRelationList.value = res.data || []
  } catch (error) {
    console.error('加载与患者关系字典失败:', error)
  }
}

const fmtDateTime = (v?: string | null) => (v ? v.slice(0, 10) : '')

// 患者标签列表
const tagList = ref<any[]>([])
const patientTagsMap = ref<Record<number, any[]>>({})
// 标签折叠状态（按行记忆）：超过 TAG_LIST_VISIBLE_LIMIT 折成「+N」，点击展开/收起
const expandedTagRows = ref<Set<number>>(new Set())
const toggleTagExpand = (id: number) => {
  const next = new Set(expandedTagRows.value)
  if (next.has(id)) {
    next.delete(id)
  } else {
    next.add(id)
  }
  expandedTagRows.value = next
}
const visibleTags = (row: Patient) => {
  const tags = patientTagsMap.value[row.id] || []
  return expandedTagRows.value.has(row.id) ? tags : tags.slice(0, TAG_LIST_VISIBLE_LIMIT)
}
const loadTagList = async () => {
  try {
    const res = await getPatientTagList({})
    tagList.value = res.data?.records || res.data || []
  } catch (error) {
    console.error('加载标签列表失败:', error)
  }
}

// 加载患者标签（批量）
const loadPatientTagsBatch = async (patientIds: number[]) => {
  try {
    const promises = patientIds.map(id => getPatientTags({patientId: id}))
    const results = await Promise.all(promises)
    patientIds.forEach((id, index) => {
      patientTagsMap.value[id] = results[index]?.data || []
    })
  } catch (error) {
    console.error('加载患者标签失败:', error)
  }
}

onMounted(() => {
  loadData()
  loadMedicalInsuranceTypes()
  loadNationalityList()
  loadPatientRelationList()
  loadTagList()
})
</script>

<template>
  <div>
    <!-- 查询卡 -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="searchForm" inline>
          <el-form-item label="患者姓名">
            <el-input v-model="searchForm.patientName" placeholder="请输入患者姓名" clearable class="!w-40"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="患者号">
            <el-input v-model="searchForm.patientNo" placeholder="请输入患者号" clearable class="!w-36"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="手机号">
            <el-input v-model="searchForm.phone" placeholder="请输入手机号" clearable class="!w-36"
                      @keyup.enter="handleSearch"/>
          </el-form-item>
          <el-form-item label="性别">
            <el-select v-model="searchForm.gender" placeholder="全部" clearable class="!w-24">
              <el-option label="男" :value="1"/>
              <el-option label="女" :value="2"/>
            </el-select>
          </el-form-item>
          <el-form-item label="患者类型">
            <el-select v-model="searchForm.patientType" placeholder="全部" clearable class="!w-32">
              <el-option v-for="o in PATIENT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="标签">
            <el-select v-model="searchForm.tagId" placeholder="全部" clearable class="!w-36">
              <el-option v-for="tag in tagList" :key="tag.tagId" :label="tag.tagName" :value="tag.tagId"/>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
            <el-button :icon="Refresh" @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'patient:add'" type="primary" :icon="Plus" @click="showAddDialog = true">新增患者</el-button>
        </div>
      </div>
    </el-card>

    <!-- 表格卡 -->
    <el-card class="table-card" shadow="never">
      <el-table :data="patients" v-loading="loading" stripe :max-height="tableMaxHeight" style="width: 100%" @row-click="handleViewDetail">
        <el-table-column prop="patientNo" label="患者号" class-name="font-mono" width="150"/>
        <el-table-column label="姓名" min-width="170">
          <template #default="{ row }">
            <div class="font-medium text-slate-800">{{ row.patientName }}</div>
            <div v-if="(patientTagsMap[row.id] || []).length" class="mt-1 flex flex-wrap gap-1">
              <span v-for="tag in visibleTags(row)" :key="tag.tagId"
                    class="inline-block cursor-default rounded px-1 py-0.5 font-medium leading-4 text-white"
                    :style="{ backgroundColor: tag.tagColor || '#409EFF' }"
                    :title="tagChipTitle(tag)">
                {{ tagChipText(tag) }}
              </span>
              <span
                  v-if="!expandedTagRows.has(row.id) && (patientTagsMap[row.id] || []).length > TAG_LIST_VISIBLE_LIMIT"
                  class="cursor-pointer rounded px-1 py-0.5 leading-4 text-slate-500 hover:text-slate-700"
                  title="展开剩余标签"
                  @click.stop="toggleTagExpand(row.id)">
                +{{ (patientTagsMap[row.id] || []).length - TAG_LIST_VISIBLE_LIMIT }}
              </span>
              <span v-else-if="expandedTagRows.has(row.id)"
                    class="cursor-pointer rounded px-1 py-0.5 leading-4 text-slate-400 hover:text-slate-600"
                    @click.stop="toggleTagExpand(row.id)">收起</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="性别/年龄" width="150">
          <template #default="{ row }">{{ patientGenderText(row.gender) }} / {{ patientAgeText(row.age) }}</template>
        </el-table-column>
        <el-table-column label="联系电话" width="150">
          <template #default="{ row }">{{ row.phoneMasked }}</template>
        </el-table-column>
        <el-table-column label="建档时间" width="150">
          <template #default="{ row }">{{ fmtDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="最近就诊" width="300">
          <template #default="{ row }">
            <div>{{ fmtDateTime(row.lastVisitTime) }}</div>
            <div v-if="row.lastVisitDeptName || row.lastVisitDoctorName"
                 class="text-slate-400">
              {{ [row.lastVisitDeptName, row.lastVisitDoctorName].filter(Boolean).join(' · ') }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="医保信息" width="170">
          <template #default="{ row }">
            <div>{{ row.medicalInsuranceType }}</div>
            <div v-if="row.medicalInsuranceNoMasked" class="font-mono text-slate-400">
              {{ row.medicalInsuranceNoMasked }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="首次就诊" width="300">
          <template #default="{ row }">
            <div>{{ fmtDateTime(row.firstVisitTime) }}</div>
            <div v-if="row.firstVisitDeptName || row.firstVisitDoctorName"
                 class="text-slate-400">
              {{ [row.firstVisitDeptName, row.firstVisitDoctorName].filter(Boolean).join(' · ') }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="血型" width="150">
          <template #default="{ row }">
            <el-tag effect="plain" size="small" v-if="row.bloodType">{{ row.bloodType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="allergyHistory" label="过敏史" show-overflow-tooltip width="200"/>
        <el-table-column label="操作" width="120" align="right">
          <template #default="{ row }">
            <el-button v-perm="'patient:edit'" type="primary" link size="small" @click.stop="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link size="small" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="patients.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无匹配的患者记录
      </div>
      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 患者详情弹框：统一走通用组件（主档详情 + CDR 全景时间轴），与 Header 共用同一实现 -->
    <PatientDetailDialog
        v-model="showDetailDialog"
        :patient-id="selectedPatientId"
        :patient="selectedPatient"
    />

    <el-dialog v-model="showAddDialog" title="新增患者" width="800px" destroy-on-close>
      <el-form label-position="top" :model="newPatient">
        <!-- 基本信息 -->
        <h4 class="mb-2 text-sm font-medium text-slate-700">基本信息</h4>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="姓名" required>
            <el-input v-model="newPatient.patientName" placeholder="请输入患者姓名"/>
          </el-form-item>
          <el-form-item label="性别" required>
            <el-select v-model="newPatient.gender" placeholder="请选择（不确定选未知）" class="w-full">
              <el-option v-for="g in PATIENT_GENDER_OPTIONS" :key="g.value" :label="g.label" :value="g.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="出生日期">
            <el-date-picker v-model="newPatient.birthDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD"
                            class="w-full"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="身份证号" required>
            <el-input v-model="newPatient.idCard" placeholder="请输入身份证号" @blur="syncBirthDateFromIdCard"/>
          </el-form-item>
          <el-form-item label="联系电话">
            <el-input v-model="newPatient.phone" placeholder="选填（填了须为 11 位手机号）"/>
          </el-form-item>
          <el-form-item label="民族">
            <el-select v-model="newPatient.nation" placeholder="请选择民族" filterable allow-create default-first-option
                       class="w-full">
              <el-option v-for="item in nationalityList" :key="item.dictValue" :label="item.dictLabel"
                         :value="item.dictValue"/>
            </el-select>
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="职业">
            <el-input v-model="newPatient.occupation" placeholder="请输入职业"/>
          </el-form-item>
          <el-form-item label="婚姻状况">
            <el-select v-model="newPatient.maritalStatus" placeholder="请选择" class="w-full" clearable>
              <el-option label="未婚" :value="0"/>
              <el-option label="已婚" :value="1"/>
              <el-option label="离异" :value="2"/>
              <el-option label="丧偶" :value="3"/>
            </el-select>
          </el-form-item>
          <el-form-item label="患者类型">
            <el-select v-model="newPatient.patientType" class="w-full">
              <el-option v-for="o in PATIENT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
        </div>

        <!-- 联系人信息 -->
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="联系人姓名">
            <el-input v-model="newPatient.contactName" placeholder="请输入联系人姓名"/>
          </el-form-item>
          <el-form-item label="联系人电话">
            <el-input v-model="newPatient.contactPhone" placeholder="请输入联系人电话"/>
          </el-form-item>
          <el-form-item label="与患者关系">
            <!-- 绑 dictLabel（文案）而不是 dictValue（码值）：biz_patient.contact_relation 这一列
                 存的是文案（列注释「联系人关系（父母、配偶、子女等）」），绑码值会出现
                 「主档里存着 '2'、结构化表里存着 2」的两套写法。保存时服务端会把文案反查成码值
                 写入 biz_patient_contact.relationship，两边从此一致。
                 也**去掉了 allow-create**：关系必须是字典里的值，自由文本（历史上出现过「父子」）
                 在结构化表里映射不到任何码值，只能落到 99-其他。 -->
            <el-select v-model="newPatient.contactRelation" placeholder="请选择关系" filterable
                       default-first-option class="w-full">
              <el-option v-for="item in patientRelationList" :key="item.dictValue" :label="item.dictLabel"
                         :value="item.dictLabel"/>
            </el-select>
          </el-form-item>
        </div>

        <!-- 其他信息 -->
        <h4 class="mb-2 mt-4 text-sm font-medium text-slate-700">其他信息</h4>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="血型">
            <el-select v-model="newPatient.bloodType" placeholder="选择血型" class="w-full" clearable>
              <el-option label="A型" value="A"/>
              <el-option label="B型" value="B"/>
              <el-option label="AB型" value="AB"/>
              <el-option label="O型" value="O"/>
            </el-select>
          </el-form-item>
          <el-form-item label="民族">
            <el-select v-model="newPatient.nation" placeholder="请选择民族" filterable allow-create default-first-option
                       class="w-full">
              <el-option v-for="item in nationalityList" :key="item.dictValue" :label="item.dictLabel"
                         :value="item.dictValue"/>
            </el-select>
          </el-form-item>
          <el-form-item label="职业">
            <el-input v-model="newPatient.occupation" placeholder="请输入职业"/>
          </el-form-item>
        </div>
        <el-form-item label="家庭住址">
          <el-input v-model="newPatient.address" placeholder="请输入家庭住址"/>
        </el-form-item>

        <!-- 医保信息 -->
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="医保类型">
            <el-select v-model="newPatient.medicalInsuranceType" placeholder="请选择医保类型" filterable clearable
                       class="w-full">
              <el-option v-for="item in medicalInsuranceTypes" :key="item.dictValue" :label="item.dictLabel"
                         :value="item.dictValue"/>
            </el-select>
          </el-form-item>
          <el-form-item label="医保卡号">
            <el-input v-model="newPatient.medicalInsuranceNo" placeholder="请输入医保卡号"/>
          </el-form-item>
        </div>

        <el-form-item label="过敏史">
          <el-input type="textarea" v-model="newPatient.allergyHistory" :rows="3"
                    placeholder="快速录入用：这里填一句过敏史，保存后会转成一条结构化过敏记录（严重程度记为「未评估」）。按条目维护请到「患者中心 → 健康档案」"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button v-perm="'patient:add'" type="primary" @click="handleSubmit">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 编辑患者弹窗 -->
    <el-dialog v-model="showEditDialog" title="编辑患者" width="800px" destroy-on-close>
      <el-form label-position="top" :model="editingPatient">
        <!-- 基本信息 -->
        <h4 class="mb-2 text-sm font-medium text-slate-700">基本信息</h4>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="姓名" required>
            <el-input v-model="editingPatient.patientName" placeholder="请输入患者姓名"/>
          </el-form-item>
          <el-form-item label="性别" required>
            <el-select v-model="editingPatient.gender" placeholder="请选择（不确定选未知）" class="w-full">
              <el-option v-for="g in PATIENT_GENDER_OPTIONS" :key="g.value" :label="g.label" :value="g.value"/>
            </el-select>
          </el-form-item>
          <el-form-item label="出生日期">
            <el-date-picker v-model="editingPatient.birthDate" type="date" placeholder="选择日期"
                            value-format="YYYY-MM-DD" class="w-full"/>
          </el-form-item>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="联系电话">
            <el-input v-model="editingPatient.phone" placeholder="选填（填了须为 11 位手机号）"/>
          </el-form-item>
          <el-form-item label="身份证号" required>
            <el-input v-model="editingPatient.idCard" placeholder="请输入身份证号"/>
          </el-form-item>
          <el-form-item label="患者类型">
            <el-select v-model="editingPatient.patientType" class="w-full">
              <el-option v-for="o in PATIENT_TYPE_OPTIONS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
          </el-form-item>
        </div>

        <!-- 其他信息 -->
        <h4 class="mb-2 mt-4 text-sm font-medium text-slate-700">其他信息</h4>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="血型">
            <el-select v-model="editingPatient.bloodType" placeholder="选择血型" class="w-full" clearable>
              <el-option label="A型" value="A"/>
              <el-option label="B型" value="B"/>
              <el-option label="AB型" value="AB"/>
              <el-option label="O型" value="O"/>
            </el-select>
          </el-form-item>
          <el-form-item label="民族">
            <el-select v-model="editingPatient.nation" placeholder="请选择民族" filterable allow-create
                       default-first-option class="w-full">
              <el-option v-for="item in nationalityList" :key="item.dictValue" :label="item.dictLabel"
                         :value="item.dictValue"/>
            </el-select>
          </el-form-item>
          <el-form-item label="职业">
            <el-input v-model="editingPatient.occupation" placeholder="请输入职业"/>
          </el-form-item>
        </div>
        <el-form-item label="家庭住址">
          <el-input v-model="editingPatient.address" placeholder="请输入家庭住址"/>
        </el-form-item>

        <!-- 联系人信息 -->
        <h4 class="mb-2 mt-4 text-sm font-medium text-slate-700">联系人信息</h4>
        <div class="grid grid-cols-3 gap-4">
          <el-form-item label="联系人姓名">
            <el-input v-model="editingPatient.contactName" placeholder="请输入联系人姓名"/>
          </el-form-item>
          <el-form-item label="联系人电话">
            <el-input v-model="editingPatient.contactPhone" placeholder="请输入联系人电话"/>
          </el-form-item>
          <el-form-item label="与患者关系">
            <!-- 同新增对话框：绑 dictLabel，且不给 allow-create（理由见新增处注释） -->
            <el-select v-model="editingPatient.contactRelation" placeholder="请选择关系" filterable
                       default-first-option class="w-full">
              <el-option v-for="item in patientRelationList" :key="item.dictValue" :label="item.dictLabel"
                         :value="item.dictLabel"/>
            </el-select>
          </el-form-item>
        </div>

        <!-- 医保信息 -->
        <h4 class="mb-2 mt-4 text-sm font-medium text-slate-700">医保信息</h4>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="医保类型">
            <el-select v-model="editingPatient.medicalInsuranceType" placeholder="请选择医保类型" filterable clearable
                       class="w-full">
              <el-option v-for="item in medicalInsuranceTypes" :key="item.dictValue" :label="item.dictLabel"
                         :value="item.dictValue"/>
            </el-select>
          </el-form-item>
          <el-form-item label="医保卡号">
            <el-input v-model="editingPatient.medicalInsuranceNo" placeholder="请输入医保卡号"/>
          </el-form-item>
        </div>

        <el-form-item label="过敏史">
          <el-input type="textarea" v-model="editingPatient.allergyHistory" :rows="3"
                    placeholder="该患者已有结构化过敏记录时，此处显示的是明细摘要、保存后会被明细覆盖；按条目维护请到「患者中心 → 健康档案」"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button v-perm="'patient:edit'" type="primary" @click="handleEditSubmit">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>
