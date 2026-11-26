<template>
  <div>
    <el-tabs v-model="activeTab">
      <!-- ==================== 一、科室 → 科目 对应 ==================== -->
      <el-tab-pane label="科室科目对应" name="map">
        <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
          <div class="flex items-start justify-between gap-4">
            <el-form :model="query" inline @submit.prevent>
              <el-form-item label="科室">
                <el-tree-select
                    v-model="query.deptId"
                    :data="visibleDeptTree"
                    :props="treeProps"
                    check-strictly
                    clearable
                    data-testid="dept-select"
                    default-expand-all
                    filterable
                    node-key="id"
                    placeholder="请选择科室"
                    style="width: 240px"
                    @change="handleDeptChange"
                >
                  <template #default="{ data }">
                    <span class="flex w-full items-center gap-1 pr-2">
                      <span class="truncate">{{ data.deptName }}</span>
                      <span v-if="unmappedIds.has(String(data.id))" class="unmapped-dot"
                            title="未配诊疗科目"/>
                    </span>
                  </template>
                </el-tree-select>
              </el-form-item>
              <el-form-item>
                <el-checkbox v-model="query.onlyUnmapped" @change="handleOnlyUnmappedChange">
                  只看未配（{{ unmappedDepts.length }}）
                </el-checkbox>
              </el-form-item>
              <el-form-item>
                <el-button :icon="Search" type="primary" @click="handleQuery">查询</el-button>
                <el-button :icon="Refresh" @click="handleReset">重置</el-button>
              </el-form-item>
            </el-form>
            <div class="flex shrink-0 items-start gap-3">
              <el-button
                  v-perm="'org:medSubject:add'"
                  :disabled="!currentDept"
                  :icon="Plus"
                  data-testid="dept-add-subject"
                  type="primary"
                  @click="openAddDialog"
              >
                新增映射
              </el-button>
            </div>
          </div>
        </el-card>

        <el-card class="table-card" shadow="never">
          <el-table
              v-if="currentDept"
              v-loading="mapLoading"
              :data="deptSubjects"
              :max-height="tableMaxHeight"
              data-testid="dept-map-table"
              stripe
          >
            <el-table-column label="科目代码" prop="subjectCode" width="120"/>
            <el-table-column label="科目名称" min-width="200">
              <template #default="{ row }">
                <span :data-testid="`subj-name-${row.subjectCode}`">{{ row.subjectName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="上级科目" min-width="150">
              <template #default="{ row }">
                <span class="text-slate-600">{{ row.parentName || '—' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="主科目" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isPrimary === 1" size="small" type="success">主科目</el-tag>
                <span v-else class="text-slate-400">—</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="180">
              <template #default="{ row }">
                <el-button
                    v-if="row.isPrimary !== 1"
                    v-perm="'org:medSubject:add'"
                    link
                    type="primary"
                    @click="handleSetPrimary(row)"
                >
                  设为主科目
                </el-button>
                <el-button
                    v-perm="'org:medSubject:add'"
                    link
                    type="danger"
                    @click="handleRemoveSubject(row)"
                >
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div v-else class="px-6 py-16">
            <el-empty description="请选择科室，查看或维护它的诊疗科目对应"/>
          </div>
          <div ref="footerRef" class="list-footer flex items-center justify-between gap-4">
            <div class="flex flex-wrap items-center gap-2 text-[13px] text-slate-600">
              <template v-if="currentDept">
                <span class="font-medium">{{ currentDept.deptName }}</span>
                <el-tag v-if="currentDept.deptCode" size="small" type="info">{{ currentDept.deptCode }}</el-tag>
                <span v-if="currentPrimary" class="text-slate-500">
                  主科目 {{ currentPrimary.subjectCode }} {{ currentPrimary.subjectName }}
                </span>
                <span v-else class="text-red-500">未设主科目</span>
                <span class="text-slate-400">一个科室可有多个科目，上报只取主科目</span>
              </template>
            </div>
            <span class="shrink-0 text-[12px] text-slate-500">共 {{ deptSubjects.length }} 条</span>
          </div>
        </el-card>
      </el-tab-pane>

      <!-- ==================== 二、诊疗科目名录 ==================== -->
      <el-tab-pane label="诊疗科目名录" name="subject">
        <el-card class="query-card mb-3" shadow="never">
          <div class="flex items-start justify-between gap-4">
            <div class="max-w-3xl pt-1.5 text-[13px] leading-6 text-slate-600">
              代码与名称取自国家卫健委《医疗机构诊疗科目名录》；二级科目请按本院《医疗机构执业许可证》副本范围裁剪。
            </div>
            <div class="flex shrink-0 items-start">
              <el-button
                  v-perm="'org:medSubject:add'"
                  :icon="Plus"
                  data-testid="subject-add-root"
                  type="primary"
                  @click="handleAddRoot"
              >
                新增一级科目
              </el-button>
            </div>
          </div>
        </el-card>

        <el-card class="table-card" shadow="never">
          <el-table
              v-loading="subjectLoading"
              :data="subjectTree"
              :max-height="tableMaxHeight"
              :tree-props="{children: 'children'}"
              data-testid="subject-tree-table"
              default-expand-all
              row-key="subjectCode"
          >
            <el-table-column label="科目代码" prop="subjectCode" width="120"/>
            <el-table-column label="科目名称" min-width="200" prop="subjectName"/>
            <el-table-column label="层级" width="90">
              <template #default="{ row }">
                <el-tag :type="row.subjectLevel === 1 ? 'primary' : 'info'" size="small">
                  {{ row.subjectLevel === 1 ? '一级' : '二级' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="临床" width="80">
              <template #default="{ row }">
                <span :class="row.isClinical === 1 ? 'text-teal-600' : 'text-slate-400'">
                  {{ row.isClinical === 1 ? '是' : '否' }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="已映射科室" width="110">
              <template #default="{ row }">
                <span :data-testid="`subj-count-${row.subjectCode}`">{{ row.mappedDeptCount || 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="230">
              <template #default="{ row }">
                <el-button
                    v-if="row.subjectLevel === 1"
                    v-perm="'org:medSubject:add'"
                    link
                    type="primary"
                    @click="handleAddChild(row)"
                >
                  新增下级
                </el-button>
                <el-button v-perm="'org:medSubject:add'" link type="primary" @click="handleEditSubject(row)">
                  编辑
                </el-button>
                <el-button v-perm="'org:medSubject:delete'" link type="danger" @click="handleDeleteSubject(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="list-footer text-[12px] text-slate-500">
            共 {{ subjectTree.length }} 个一级科目
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <MedSubjectAdd
        v-model="addDialog"
        :dept="currentDept"
        :groups="subjectGroups"
        :subjects="deptSubjects"
        @saved="handleMapSaved"
    />

    <SubjectEdit
        v-model="subjectDialog"
        :parent-subject="subjectParent"
        :row="subjectRow"
        @saved="loadSubjectTree"
    />
  </div>
</template>

<script lang="js" setup>
import {computed, onMounted, reactive, ref } from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Refresh, Search} from '@element-plus/icons-vue'
import {
  deleteMedSubject,
  getDepartmentTree,
  getDeptSubjectMap,
  getMedSubjectTree,
  getUnmappedDepts,
  saveDeptSubjectMap,
} from '@/api/system'
import {useTableMaxHeight} from '@/lib/useTableMaxHeight'
import MedSubjectAdd from '@/views/system/medSubject/MedSubjectAdd.vue'
import SubjectEdit from '@/views/system/medSubject/SubjectEdit.vue'

const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight()

const activeTab = ref('map')

// ---------------- 科室与查询 ----------------
const deptTree = ref([])
const currentDept = ref(null)
const deptSubjects = ref([])
const mapLoading = ref(false)

const query = reactive({deptId: '', onlyUnmapped: false})

// 未配科目的临床科室（后端已排除“内科系统”这类有下级的分组目录节点）
const unmappedDepts = ref([])
const unmappedIds = computed(() => new Set(unmappedDepts.value.map(d => String(d.deptId))))

const currentPrimary = computed(() => deptSubjects.value.find(s => s.isPrimary === 1) || null)

// 分组目录节点本身不参与上报，选中没有意义；在树里置灰，只让选叶子科室
const treeProps = {
  label: 'deptName',
  children: 'children',
  disabled: (data) => (data.children || []).length > 0,
}

const visibleDeptTree = computed(() => {
  if (!query.onlyUnmapped) return deptTree.value
  const filter = (nodes) => (nodes || []).map(n => {
    const children = filter(n.children)
    const selfHit = unmappedIds.value.has(String(n.id))
    if (!selfHit && !children.length) return null
    return {...n, children}
  }).filter(Boolean)
  return filter(deptTree.value)
})

// ---------------- 科目 ----------------
const subjectTree = ref([])
const subjectLoading = ref(false)

// 下拉按一级科目分组：科室通常直接挂二级科目（03.01），一级科目兜底（如 02 全科医疗科）
const subjectGroups = computed(() => subjectTree.value
    .filter(s => s.status === 1)
    .map(s => ({
      code: s.subjectCode,
      name: s.subjectName,
      children: (s.children || []).filter(c => c.status === 1),
    })))

// ---------------- 弹框 ----------------
const addDialog = ref(false)
const subjectDialog = ref(false)
const subjectRow = ref(null)
const subjectParent = ref(null)

onMounted(async () => {
  await Promise.all([loadDeptTree(), loadSubjectTree(), loadUnmapped()])
  // 进页面直接落到第一个未配科室，避免空白页；没有未配的就落到第一个叶子科室
  const first = unmappedDepts.value[0]?.deptId || firstLeafDeptId(deptTree.value)
  if (first) await selectDept(String(first))
})

const loadDeptTree = async () => {
  try {
    const res = await getDepartmentTree()
    if (res.code === 200) deptTree.value = res.data || []
  } catch (error) /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */ {
  }
}

const loadSubjectTree = async () => {
  subjectLoading.value = true
  try {
    const res = await getMedSubjectTree()
    if (res.code === 200) subjectTree.value = res.data || []
  } catch (error) /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */ {
  } finally {
    subjectLoading.value = false
  }
}

const loadUnmapped = async () => {
  try {
    const res = await getUnmappedDepts()
    if (res.code === 200) unmappedDepts.value = res.data || []
  } catch (error) /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */ {
  }
}

const firstLeafDeptId = (nodes) => {
  for (const n of nodes || []) {
    if (!(n.children || []).length) return n.id
    const hit = firstLeafDeptId(n.children)
    if (hit) return hit
  }
  return null
}

const findDeptNode = (nodes, id) => {
  for (const n of nodes || []) {
    if (String(n.id) === String(id)) return n
    const hit = findDeptNode(n.children, id)
    if (hit) return hit
  }
  return null
}

const selectDept = async (id) => {
  const node = findDeptNode(deptTree.value, id)
  if (!node) return
  query.deptId = node.id
  await handleDeptChange(node.id)
}

const handleDeptChange = async (id) => {
  if (!id) {
    currentDept.value = null
    deptSubjects.value = []
    return
  }
  const node = findDeptNode(deptTree.value, id)
  currentDept.value = node ? {...node} : {id}
  await loadDeptMap(id)
}

const loadDeptMap = async (deptId) => {
  mapLoading.value = true
  try {
    const res = await getDeptSubjectMap(deptId)
    if (res.code === 200) {
      // 后端返回的 deptName / parentName 比树节点上的更全（含上级科室名），覆盖过去
      currentDept.value = {...currentDept.value, ...res.data}
      deptSubjects.value = res.data?.subjects || []
    }
  } catch (error) /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */ {
  } finally {
    mapLoading.value = false
  }
}

const handleOnlyUnmappedChange = async (checked) => {
  if (!checked) return
  const first = unmappedDepts.value[0]?.deptId
  if (first) await selectDept(String(first))
  else if (currentDept.value && !unmappedIds.value.has(String(currentDept.value.id))) {
    query.deptId = ''
    currentDept.value = null
    deptSubjects.value = []
  }
}

const handleQuery = async () => {
  await Promise.all([loadUnmapped(), currentDept.value ? loadDeptMap(currentDept.value.id) : Promise.resolve()])
}

const handleReset = async () => {
  query.onlyUnmapped = false
  const first = unmappedDepts.value[0]?.deptId || firstLeafDeptId(deptTree.value)
  if (first) await selectDept(String(first))
  else {
    query.deptId = ''
    currentDept.value = null
    deptSubjects.value = []
  }
}

// ---------------- 映射增删改（整单提交） ----------------
const toItems = (list) => list.map(s => ({subjectCode: s.subjectCode, isPrimary: s.isPrimary}))

const saveMap = async (items) => {
  if (!currentDept.value) return
  try {
    const res = await saveDeptSubjectMap(currentDept.value.id ?? currentDept.value.deptId, items)
    if (res.code === 200) {
      ElMessage.success(res.msg || '保存成功')
      await handleMapSaved()
    }
  } catch (error) /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */ {
  }
}

const handleMapSaved = async () => {
  await Promise.all([
    currentDept.value ? loadDeptMap(currentDept.value.id ?? currentDept.value.deptId) : Promise.resolve(),
    loadUnmapped(),
    loadSubjectTree(),
  ])
}

const openAddDialog = () => {
  if (!currentDept.value) {
    ElMessage.warning('请先选择科室')
    return
  }
  addDialog.value = true
}

const handleSetPrimary = async (row) => {
  try {
    await ElMessageBox.confirm(
        `把「${row.subjectName}」设为 ${currentDept.value.deptName} 的上报主科目？病案首页将取这个科目。`,
        '设置主科目',
        {type: 'warning'},
    )
    await saveMap(deptSubjects.value.map(s => ({
      subjectCode: s.subjectCode,
      isPrimary: s.subjectCode === row.subjectCode ? 1 : 0,
    })))
  } catch (error) /* 取消：无需提示 */ {
  }
}

const handleRemoveSubject = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定移除「${row.subjectName}」吗？移除后该科室不再对应这个诊疗科目。`,
        '移除确认',
        {type: 'warning'},
    )
    await saveMap(toItems(deptSubjects.value.filter(s => s.subjectCode !== row.subjectCode)))
  } catch (error) /* 取消：无需提示 */ {
  }
}

// ---------------- 科目维护 ----------------
const handleAddRoot = () => {
  subjectRow.value = null
  subjectParent.value = null
  subjectDialog.value = true
}

const handleAddChild = (row) => {
  subjectRow.value = null
  subjectParent.value = row
  subjectDialog.value = true
}

const handleEditSubject = (row) => {
  subjectRow.value = row
  subjectParent.value = null
  subjectDialog.value = true
}

const handleDeleteSubject = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定删除「${row.subjectCode} ${row.subjectName}」吗？已被科室引用或还有二级科目时不能删。`,
        '删除确认',
        {type: 'warning'},
    )
    const res = await deleteMedSubject(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      await Promise.all([loadSubjectTree(), loadUnmapped()])
    }
  } catch (error) /* 取消或后端拦截：错误已由全局拦截器提示 */ {
  }
}
</script>

<style scoped>
.unmapped-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #ef4444;
  flex-shrink: 0;
}
</style>
