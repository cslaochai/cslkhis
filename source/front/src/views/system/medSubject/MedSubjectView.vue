<template>
  <div>
    <el-tabs v-model="activeTab">
      <!-- ==================== 一、科室 → 科目 对照 ==================== -->
      <el-tab-pane label="科室科目对照" name="map">
        <el-alert
            v-if="unmappedDepts.length"
            :closable="false"
            class="mb-3"
            type="warning"
            show-icon
            title="还有临床科室没配诊疗科目，病案首页上报会取不到标准科别"
        >
          <div class="flex items-center gap-3">
            <span class="text-[13px]">共 {{ unmappedDepts.length }} 个：{{ unmappedNames }}</span>
            <el-button link type="primary" @click="handleJumpUnmapped">定位第一个</el-button>
          </div>
        </el-alert>

        <div class="flex items-start gap-3">
          <el-card class="query-card w-[320px] shrink-0" shadow="never">
            <el-input v-model="deptFilter" class="mb-2" clearable data-testid="dept-search"
                      placeholder="搜索科室名称"/>
            <div class="dept-tree-box">
              <el-tree
                  ref="deptTreeRef"
                  :data="deptTree"
                  :expand-on-click-node="false"
                  :filter-node-method="filterDeptNode"
                  :props="{label: 'deptName', children: 'children'}"
                  data-testid="dept-tree"
                  highlight-current
                  node-key="id"
                  @node-click="handleDeptClick"
              >
                <template #default="{ data }">
                  <span :data-testid="`dept-node-${data.id}`" class="flex w-full items-center gap-1 pr-2">
                    <span class="truncate">{{ data.deptName }}</span>
                    <span v-if="unmappedIds.has(String(data.id))" class="unmapped-dot" title="未配诊疗科目"/>
                  </span>
                </template>
              </el-tree>
            </div>
            <div class="mt-2 text-[12px] text-slate-500">
              红点 = 该科室还没配诊疗科目
            </div>
          </el-card>

          <el-card class="table-card flex-1" shadow="never">
            <template v-if="currentDept">
              <div class="mb-3 flex items-center justify-between">
                <div class="flex items-center gap-2">
                  <span class="text-[15px] font-medium text-slate-800">{{ currentDept.deptName }}</span>
                  <el-tag v-if="currentDept.deptCode" size="small" type="info">{{ currentDept.deptCode }}</el-tag>
                  <el-tag v-if="currentDept.parentName" size="small">{{ currentDept.parentName }}</el-tag>
                  <el-tag v-if="currentPrimary" size="small" type="success">
                    主科目：{{ currentPrimary.subjectCode }} {{ currentPrimary.subjectName }}
                  </el-tag>
                  <el-tag v-else size="small" type="danger">未设主科目</el-tag>
                </div>
                <span class="text-[12px] text-slate-500">改动即时保存</span>
              </div>

              <el-table v-loading="mapLoading" :data="deptSubjects" stripe>
                <el-table-column label="科目代码" prop="subjectCode" width="110"/>
                <el-table-column label="科目名称" min-width="180">
                  <template #default="{ row }">
                    <span :data-testid="`subj-name-${row.subjectCode}`">{{ row.subjectName }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="上级科目" min-width="140">
                  <template #default="{ row }">
                    <span class="text-slate-600">{{ row.parentName || '—' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="主科目" width="90">
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
                    >设为主科目
                    </el-button>
                    <el-button
                        v-perm="'org:medSubject:add'"
                        link
                        type="danger"
                        @click="handleRemoveSubject(row)"
                    >移除
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>

              <div class="mt-3 flex items-center gap-2">
                <el-select
                    v-model="pendingSubject"
                    class="!w-80"
                    clearable
                    data-testid="dept-subject-select"
                    filterable
                    placeholder="选择要添加的诊疗科目"
                >
                  <el-option-group
                      v-for="group in subjectGroups"
                      :key="group.code"
                      :label="`${group.code} ${group.name}`"
                  >
                    <el-option
                        v-for="s in group.children"
                        :key="s.subjectCode"
                        :label="`${s.subjectCode} ${s.subjectName}`"
                        :value="s.subjectCode"
                    />
                    <el-option
                        v-if="!group.children.length"
                        :key="group.code"
                        :label="`${group.code} ${group.name}（一级科目）`"
                        :value="group.code"
                    />
                  </el-option-group>
                </el-select>
                <el-button v-perm="'org:medSubject:add'" :disabled="!pendingSubject" data-testid="dept-add-subject"
                           type="primary" @click="handleAddSubject">
                  添加科目
                </el-button>
                <span class="text-[12px] text-slate-500">
                  一个科室可以有多个科目，但只有一个是上报主科目
                </span>
              </div>
            </template>
            <el-empty v-else description="点击左侧科室查看 / 维护它的诊疗科目"/>
          </el-card>
        </div>
      </el-tab-pane>

      <!-- ==================== 二、科目维护 ==================== -->
      <el-tab-pane label="科目维护" name="subject">
        <el-card class="table-card" shadow="never">
          <div class="mb-3 flex items-center justify-between">
            <div class="text-[13px] text-slate-600">
              代码与名称取自国家卫健委《医疗机构诊疗科目名录》；二级科目请按本院《医疗机构执业许可证》副本范围裁剪。
            </div>
            <el-button v-perm="'org:medSubject:add'" :icon="Plus" data-testid="subject-add-root"
                       type="primary" @click="handleAddRoot">
              新增一级科目
            </el-button>
          </div>
          <el-table
              v-loading="subjectLoading"
              :data="subjectTree"
              :tree-props="{children: 'children'}"
              default-expand-all
              row-key="subjectCode"
          >
            <el-table-column label="科目代码" prop="subjectCode" width="110"/>
            <el-table-column label="科目名称" min-width="180" prop="subjectName"/>
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
            <el-table-column label="状态" width="80">
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
                >新增下级
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
        </el-card>
      </el-tab-pane>
    </el-tabs>

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
import {Plus} from '@element-plus/icons-vue'
import {
  deleteMedSubject,
  getDepartmentTree,
  getDeptSubjectMap,
  getMedSubjectTree,
  getUnmappedDepts,
  saveDeptSubjectMap,
} from '@/api/system'
import SubjectEdit from '@/views/system/medSubject/SubjectEdit.vue'

const activeTab = ref('map')

// ---------------- 科室树 ----------------
const deptTree = ref([])
const deptFilter = ref('')
const deptTreeRef = ref(null)
const currentDept = ref(null)
const deptSubjects = ref([])
const mapLoading = ref(false)

// 未配科目的临床科室（后端已排除"内科系统"这类有下级的分组目录节点）
const unmappedDepts = ref([])
const unmappedIds = computed(() => new Set(unmappedDepts.value.map(d => String(d.deptId))))
const unmappedNames = computed(() => {
  const names = unmappedDepts.value.map(d => d.deptName)
  return names.length > 6 ? names.slice(0, 6).join('、') + ` 等 ${names.length} 个` : names.join('、')
})

const currentPrimary = computed(() => deptSubjects.value.find(s => s.isPrimary === 1) || null)

watch(deptFilter, (v) => deptTreeRef.value?.filter(v))

const filterDeptNode = (value, data) => {
  if (!value) return true
  return (data.deptName || '').includes(value)
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
const subjectDialog = ref(false)
const subjectRow = ref(null)
const subjectParent = ref(null)

onMounted(async () => {
  await Promise.all([loadDeptTree(), loadSubjectTree(), loadUnmapped()])
})

const loadDeptTree = async () => {
  try {
    const res = await getDepartmentTree()
    if (res.code === 200) {
      deptTree.value = res.data || []
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */{
  }
}

const loadSubjectTree = async () => {
  subjectLoading.value = true
  try {
    const res = await getMedSubjectTree()
    if (res.code === 200) {
      subjectTree.value = res.data || []
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */{
  } finally {
    subjectLoading.value = false
  }
}

const loadUnmapped = async () => {
  try {
    const res = await getUnmappedDepts()
    if (res.code === 200) {
      unmappedDepts.value = res.data || []
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */{
  }
}

const handleDeptClick = async (data) => {
  currentDept.value = data
  await loadDeptMap(data.id)
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
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */{
  } finally {
    mapLoading.value = false
  }
}

/**
 * 整单提交当前科室的科目清单。
 * 后端是整单替换语义（传什么就是什么），所以每次增删改都带上完整清单。
 */
const saveMap = async (items) => {
  if (!currentDept.value) return
  try {
    const res = await saveDeptSubjectMap(currentDept.value.id ?? currentDept.value.deptId, items)
    if (res.code === 200) {
      ElMessage.success(res.msg || '保存成功')
      await Promise.all([loadDeptMap(currentDept.value.deptId ?? currentDept.value.id), loadUnmapped(), loadSubjectTree()])
    }
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此重复弹窗 */{
  }
}

// 当前清单转提交结构
const toItems = (list) => list.map(s => ({subjectCode: s.subjectCode, isPrimary: s.isPrimary}))

const pendingSubject = ref('')

const handleAddSubject = async () => {
  const code = pendingSubject.value
  if (!code) return
  if (deptSubjects.value.some(s => s.subjectCode === code)) {
    ElMessage.warning('该科目已在这个科室的清单里')
    return
  }
  const items = toItems(deptSubjects.value)
  items.push({subjectCode: code, isPrimary: items.length === 0 ? 1 : 0})
  await saveMap(items)
  pendingSubject.value = ''
}

const handleSetPrimary = async (row) => {
  const items = deptSubjects.value.map(s => ({
    subjectCode: s.subjectCode,
    isPrimary: s.subjectCode === row.subjectCode ? 1 : 0,
  }))
  await saveMap(items)
}

const handleRemoveSubject = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确定移除「${row.subjectName}」吗？移除后该科室不再对应这个诊疗科目。`,
        '移除确认',
        {type: 'warning'},
    )
    await saveMap(toItems(deptSubjects.value.filter(s => s.subjectCode !== row.subjectCode)))
  } catch (error)  /* 取消：无需提示 */{
  }
}

const handleJumpUnmapped = async () => {
  const first = unmappedDepts.value[0]
  if (!first) return
  activeTab.value = 'map'
  deptFilter.value = first.deptName
  // 树的 filter 是内部状态更新，等一个 tick 再查节点，否则拿到的可能还是过滤前的树
  await nextTick()
  const node = findDeptNode(deptTree.value, String(first.deptId))
  if (node) {
    deptTreeRef.value?.setCurrentKey(node.id)
    await handleDeptClick(node)
  }
}

const findDeptNode = (list, id) => {
  for (const d of list || []) {
    if (String(d.id) === id) return d
    const hit = findDeptNode(d.children, id)
    if (hit) return hit
  }
  return null
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
  } catch (error)  /* 取消或后端拦截：错误已由全局拦截器提示 */{
  }
}
</script>

<style scoped>
.dept-tree-box {
  max-height: calc(100vh - 340px);
  overflow-y: auto;
}

.unmapped-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #ef4444;
  flex-shrink: 0;
}
</style>
