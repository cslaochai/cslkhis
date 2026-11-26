<template>
  <div>
    <el-tabs v-model="activeTab">
      <!-- ==================== 一、科室 → 科目 对应 ==================== -->
      <el-tab-pane label="科室 → 上报科室代码" name="map">
        <div class="dept-subject-layout">
          <div class="dept-tree-panel">
            <div class="mb-3 flex items-center justify-between">
              <span class="font-medium">科室列表</span>
              <div class="flex items-center">
                <el-button :icon="Expand" bg size="small" text @click="handleExpandAll">展开</el-button>
                <el-button :icon="Fold" bg size="small" text @click="handleCollapseAll">收缩</el-button>
                <el-button :icon="Refresh" bg size="small" text @click="handleRefresh">刷新</el-button>
              </div>
            </div>

            <div class="mb-3">
              <el-input
                  v-model="deptKeyword"
                  :prefix-icon="Search"
                  clearable
                  placeholder="搜索科室名称 / 科室编码"
                  size="small"
              />
            </div>

            <div class="dept-tree-scroll">
              <el-tree
                  ref="deptTreeRef"
                  :data="deptTree"
                  :expand-on-click-node="false"
                  :filter-node-method="filterDeptNode"
                  :indent="10"
                  :props="treeProps"
                  data-testid="dept-tree"
                  default-expand-all
                  highlight-current
                  node-key="id"
                  @node-click="handleTreeClick"
              >
                <template #default="{ data }">
                  <span class="flex w-full items-center gap-1 pr-2">
                    <span class="truncate">{{ data.deptName }}</span>
                    <span v-if="unmappedIds.has(String(data.id))" class="unmapped-dot"
                          title="未配上报科室代码"/>
                  </span>
                </template>
              </el-tree>
            </div>
          </div>

          <div class="dept-list-panel">
            <div class="dept-list-head">
              <div class="flex min-w-0 items-center gap-2">
                <span class="font-medium">{{ currentDept ? currentDept.deptName : '上报科室代码' }}</span>
                <el-tag v-if="currentDept?.deptCode" size="small" type="info">{{ currentDept.deptCode }}</el-tag>
                <template v-if="deptSubjects.length">
                  <span class="text-[12px] text-slate-500">
                    主科目：<span class="font-medium text-slate-700">{{ currentPrimary ? reportCode(currentPrimary.subjectCode) + ' ' + currentPrimary.subjectName : '未设置' }}</span>
                  </span>
                  <span class="text-[12px] text-slate-400">共 {{ deptSubjects.length }} 条</span>
                </template>
              </div>
              <el-button
                  v-perm="'org:medSubject:add'"
                  :icon="Plus"
                  data-testid="dept-add-subject"
                  size="small"
                  type="primary"
                  @click="openAddDialog"
              >
                新增映射
              </el-button>
            </div>
            <div class="dept-table-body">
              <el-table
                  v-loading="mapLoading"
                  :data="deptSubjects"
                  :empty-text="currentDept ? '暂无数据' : '请选择左侧科室'"
                  data-testid="dept-map-table"
                  height="100%"
                  stripe
              >
                <el-table-column label="上报科室代码（CT08.00.002）" min-width="250">
                  <template #default="{ row }">
                    <span class="font-mono">{{ reportCode(row.subjectCode) }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="科目名称" min-width="240">
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
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ==================== 二、诊疗科目名录 ==================== -->
      <el-tab-pane label="上报科室代码表" name="subject">
        <el-card class="query-card mb-3" shadow="never">
          <div class="flex items-start justify-between gap-4">
            <div class="max-w-4xl pt-1.5 text-[13px] leading-6 text-slate-600">
              临床类代码取自《医疗卫生机构业务科室分类与代码》(CT08.00.002)，页面按 <span class="font-mono">A + 科目码</span>
              展示（如 03.01 → A03.01）。医技、行政后勤类代码待补全量码表；二级科目请按本院《医疗机构执业许可证》副本范围裁剪。
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
            <el-table-column label="上报科室代码" width="150">
              <template #default="{ row }">
                <span class="font-mono">{{ reportCode(row.subjectCode) }}</span>
                <span class="ml-1 text-[11px] text-slate-400">{{ row.subjectCode }}</span>
              </template>
            </el-table-column>
            <el-table-column label="名称" min-width="200" prop="subjectName"/>
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
        :dept-tree="deptTree"
        :groups="subjectGroups"
        @saved="handleAddSaved"
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
import {computed, nextTick, onMounted, ref, watch} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Search, Expand, Fold, Refresh} from '@element-plus/icons-vue'
import {
  deleteMedSubject,
  getDepartmentTree,
  getDeptSubjectMap,
  getMedSubjectTree,
  getUnmappedDepts,
  saveDeptSubjectMap,
} from '@/api/system'
import MedSubjectAdd from '@/views/system/medSubject/MedSubjectAdd.vue'
import SubjectEdit from '@/views/system/medSubject/SubjectEdit.vue'

const activeTab = ref('map')

// ---------------- 科室与查询 ----------------
const deptTree = ref([])
const currentDept = ref(null)
const deptSubjects = ref([])
const mapLoading = ref(false)
const deptTreeRef = ref(null)
const deptKeyword = ref('')

// 未配科目的临床科室（后端已排除“内科系统”这类有下级的分组目录节点）
const unmappedDepts = ref([])
const unmappedIds = computed(() => new Set(unmappedDepts.value.map(d => String(d.deptId))))

const currentPrimary = computed(() => deptSubjects.value.find(s => s.isPrimary === 1) || null)

// 库里存的是诊疗科目码（03.01），上报按 CT08.00.002 用 A 前缀（A03.01）；只有展示层加，写库不改
const reportCode = (code) => (code ? `A${code}` : '')

// 分组目录节点本身不参与上报，选中没有意义；在树里置灰，只让选叶子科室
const treeProps = {
  label: 'deptName',
  children: 'children',
  disabled: (data) => (data.children || []).length > 0,
}

// 左侧树筛选：名称或科室编码命中；只过滤显示，不改树结构
const filterDeptNode = (value, data) => {
  const kw = String(value || '').trim()
  if (!kw) return true
  return String(data.deptName || '').includes(kw) || String(data.deptCode || '').includes(kw)
}

watch(deptKeyword, (v) => deptTreeRef.value?.filter(v))

// 展开 / 收缩 / 刷新：与「菜单管理」左树同一套交互
const getAllDeptKeys = (nodes) => {
  const keys = []
  const walk = (list) => (list || []).forEach(n => {
    keys.push(n.id)
    walk(n.children)
  })
  walk(nodes)
  return keys
}

const setAllExpanded = (expanded) => {
  getAllDeptKeys(deptTree.value).forEach(key => {
    const node = deptTreeRef.value?.getNode(key)
    if (node) node.expanded = expanded
  })
}

const handleExpandAll = () => setAllExpanded(true)
const handleCollapseAll = () => setAllExpanded(false)

const handleRefresh = async () => {
  deptKeyword.value = ''
  await Promise.all([loadDeptTree(), loadUnmapped()])
  await nextTick()
  deptTreeRef.value?.filter('')
  if (currentDept.value) {
    await loadDeptMap(currentDept.value.id ?? currentDept.value.deptId)
    deptTreeRef.value?.setCurrentKey(currentDept.value.id ?? currentDept.value.deptId)
  }
}

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
  await handleDeptChange(node.id)
  await nextTick()
  deptTreeRef.value?.setCurrentKey(node.id)
}

// 分组目录节点（内科系统、外科系统…）本身不参与上报，点了也不出数据
const handleTreeClick = async (data) => {
  if ((data.children || []).length) return
  await handleDeptChange(data.id)
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

// 弹框里可能换了科室：落到新科室再刷新，用户能看到刚存进去的那一行
const handleAddSaved = async (deptId) => {
  if (deptId && (!currentDept.value || String(currentDept.value.id ?? currentDept.value.deptId) !== String(deptId))) {
    await selectDept(String(deptId))
  }
  await handleMapSaved()
}

const openAddDialog = () => {
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
/* 与「系统管理 → 菜单管理」同风格：左侧固定宽树面板 + 右侧列表面板，纯边框白底，不用 el-card */
.dept-subject-layout {
  display: flex;
  gap: 16px;
  height: calc(100vh - 200px);
  min-height: 560px;
}

.dept-tree-panel {
  display: flex;
  flex: none;
  flex-direction: column;
  width: 384px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.dept-tree-scroll {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.dept-list-panel {
  display: flex;
  flex: 1;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
}

.dept-list-head {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid #f1f5f9;
}

.dept-table-body {
  flex: 1;
  min-height: 0;
}

.unmapped-dot {
  display: inline-block;
  flex-shrink: 0;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #ef4444;
}
</style>
