<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, Edit, Plus, Refresh, Search} from '@element-plus/icons-vue'
import {getRoleList} from '@/api/system'
import {
    deleteWidgetById,
    getRoleWorkbenchConfig,
    roleWorkbenchConfigUpsert,
    widgetListPage,
    widgetUpsert,
} from '@/api/workbench'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'

/**
 * 系统管理 → 工作台配置
 *
 * 两个页签对应两套数据：
 *  · 角色工作台 = sys_workbench_role（谁能看到哪几张卡、什么顺序、登录后落在哪）；
 *  · 卡片注册表 = sys_workbench_widget（有哪些卡可配，permission 用哪个页面码）。
 *
 * 这里**不写角色码分支，也不在前端复制权限判断**：某角色最终能不能取到某张卡的数据，
 * 由后端「配置勾选 ∩ 该角色在 sys_role_menu 里是否真的持有卡片权限码」算，
 * 所以「勾了卡但角色没页面权限」这种配置在预览里看得见、在首页上不会出现（配错也不炸）。
 */

const activeTab = ref('role')

/** 卡片分区，与 sys_workbench_widget.area 一字不差 */
const AREA_OPTIONS = [
    {value: 'todo', label: '待办'},
    {value: 'notice', label: '通知'},
    {value: 'entry', label: '快捷入口'},
    {value: 'kpi', label: '数字概览'},
    {value: 'domain', label: '业务领域'},
]
const areaLabel = (a: string) => AREA_OPTIONS.find(x => x.value === a)?.label || a

/** 落点策略，与后端 landing_scope 同口径 */
const LANDING_OPTIONS = [
    {value: 0, label: '默认（有患者工作站的岗位直达工作站）'},
    {value: 1, label: '一律进工作台'},
    {value: 2, label: '一律进患者工作站'},
]

/* ==================== 页签一：角色工作台 ==================== */
const roleOptions = ref<any[]>([])
const currentRoleId = ref<string>('')
const landingScope = ref(0)
const roleRows = ref<any[]>([])
const roleLoading = ref(false)
const savingRole = ref(false)

async function loadRoles() {
    try {
        const res = await getRoleList({})
        roleOptions.value = res.data || []
        if (!currentRoleId.value && roleOptions.value.length) {
            currentRoleId.value = String(roleOptions.value[0].roleId ?? roleOptions.value[0].id ?? '')
            await loadRoleConfig()
        }
    } catch (e: any) {
        ElMessage.error(e?.message || '角色列表加载失败')
    }
}

async function loadRoleConfig() {
    if (!currentRoleId.value) return
    roleLoading.value = true
    try {
        const res = await getRoleWorkbenchConfig(currentRoleId.value)
        const data = res.data || {}
        landingScope.value = Number(data.landingScope ?? 0)
        // 后端返回注册表全量左连该角色配置：未勾选的也在里面（visible=0），所以能当预览用
        roleRows.value = (data.widgets || []).map((w: any) => ({
            ...w,
            visible: Number(w.visible ?? 0),
            sortOrder: Number(w.sortOrder ?? 0),
        }))
    } catch (e: any) {
        ElMessage.error(e?.message || '工作台配置加载失败')
    } finally {
        roleLoading.value = false
    }
}

const checkedCount = () => roleRows.value.filter(r => r.visible === 1).length

async function saveRoleConfig() {
    if (!currentRoleId.value) return ElMessage.warning('请先选择角色')
    const widgets = roleRows.value
        .filter(r => r.visible === 1)
        .map(r => ({widgetId: r.id, sortOrder: r.sortOrder, visible: 1}))
    if (widgets.length === 0 && landingScope.value !== 0) {
        return ElMessage.warning('落点策略需随卡片一起保存：请先勾选至少一张卡片')
    }
    savingRole.value = true
    try {
        await roleWorkbenchConfigUpsert({
            roleId: currentRoleId.value,
            landingScope: landingScope.value,
            widgets,
        })
        ElMessage.success('已保存，该角色下次登录或切换角色时生效')
        await loadRoleConfig()
    } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
    } finally {
        savingRole.value = false
    }
}

/* ==================== 页签二：卡片注册表 ==================== */
const widgetLoading = ref(false)
const widgetRows = ref<any[]>([])
const widgetQuery = reactive({keyword: '', area: '', status: null as number | null})
const widgetPage = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0})

const dialogVisible = ref(false)
const dialogTitle = ref('新增卡片')
const submitting = ref(false)
const emptyForm = () => ({
    id: null as string | null,
    widgetCode: '',
    widgetName: '',
    area: 'domain',
    apiKey: '',
    permission: '',
    defaultSpan: 6,
    sortOrder: 100,
    status: 0,
    remark: '',
})
const form = ref(emptyForm())

async function loadWidgets() {
    widgetLoading.value = true
    try {
        const res = await widgetListPage({
            keyword: widgetQuery.keyword || undefined,
            area: widgetQuery.area || undefined,
            status: widgetQuery.status,
            pageNum: widgetPage.pageNum,
            pageSize: widgetPage.pageSize,
        })
        widgetRows.value = res.data?.records || []
        widgetPage.total = Number(res.data?.total || 0)
    } catch (e: any) {
        ElMessage.error(e?.message || '卡片列表加载失败')
    } finally {
        widgetLoading.value = false
    }
}

function handleWidgetSearch() {
    widgetPage.pageNum = 1
    loadWidgets()
}

function handleWidgetReset() {
    widgetQuery.keyword = ''
    widgetQuery.area = ''
    widgetQuery.status = null
    handleWidgetSearch()
}

function openAdd() {
    form.value = emptyForm()
    dialogTitle.value = '新增卡片'
    dialogVisible.value = true
}

/** 编辑回显：VO 是 span、DTO 是 defaultSpan，这里一次性对齐 */
function openEdit(row: any) {
    form.value = {
        id: row.id,
        widgetCode: row.widgetCode,
        widgetName: row.widgetName,
        area: row.area,
        apiKey: row.apiKey,
        permission: row.permission || '',
        defaultSpan: Number(row.span ?? 6),
        sortOrder: Number(row.sortOrder ?? 100),
        status: Number(row.status ?? 0),
        remark: row.remark || '',
    }
    dialogTitle.value = `修改卡片 ${row.widgetCode}`
    dialogVisible.value = true
}

async function submitWidget() {
    submitting.value = true
    try {
        await widgetUpsert(form.value)
        ElMessage.success('保存成功')
        dialogVisible.value = false
        await loadWidgets()
    } catch (e: any) {
        ElMessage.error(e?.message || '保存失败')
    } finally {
        submitting.value = false
    }
}

async function removeWidget(row: any) {
    try {
        await ElMessageBox.confirm(
            `删除卡片「${row.widgetName}」会同时删除全部角色对它的配置，确定继续？`,
            '删除确认',
            {type: 'warning'},
        )
    } catch {
        return
    }
    try {
        await deleteWidgetById(row.id)
        ElMessage.success('删除成功')
        loadWidgets()
    } catch (e: any) {
        ElMessage.error(e?.message || '删除失败')
    }
}

onMounted(() => {
    loadRoles()
    loadWidgets()
})
</script>

<template>
  <div class="space-y-4">
    <div>
      <h1 class="text-xl font-bold text-slate-900">工作台配置</h1>
      <p class="mt-1 text-sm text-slate-500">
        每个角色看哪几张卡片、登录后落在工作台还是患者工作站，都在这里维护，改完无需改代码
      </p>
    </div>

    <el-tabs v-model="activeTab" class="bg-white rounded-lg border border-slate-200 px-4 pt-2 shadow-sm">
      <el-tab-pane label="角色工作台" name="role">
        <div class="flex flex-wrap items-center gap-3 pb-3">
          <span class="text-[15px] text-slate-600">角色</span>
          <el-select
              v-model="currentRoleId"
              filterable
              placeholder="请选择角色"
              style="width: 240px"
              :fit-input-width="false"
              @change="loadRoleConfig"
          >
            <el-option
                v-for="r in roleOptions"
                :key="r.roleId ?? r.id"
                :label="`${r.roleName}（${r.roleCode}）`"
                :value="String(r.roleId ?? r.id)"
            />
          </el-select>

          <span class="ml-3 text-[15px] text-slate-600">登录落点</span>
          <el-radio-group v-model="landingScope">
            <el-radio v-for="o in LANDING_OPTIONS" :key="o.value" :value="o.value" :label="o.value">
              {{ o.label }}
            </el-radio>
          </el-radio-group>

          <div class="ml-auto flex items-center gap-3">
            <span class="text-[15px] text-slate-500">已勾选 {{ checkedCount() }} 张</span>
            <el-button :icon="Refresh" :loading="roleLoading" @click="loadRoleConfig">重新加载</el-button>
            <el-button type="primary" :loading="savingRole" @click="saveRoleConfig">保存</el-button>
          </div>
        </div>

        <el-table v-loading="roleLoading" :data="roleRows" border row-key="id">
          <el-table-column label="显示" width="70" align="center">
            <template #default="{ row }">
              <!-- 未上线的卡（provider 还没写）勾了也不会渲染，直接禁止勾选，避免配了看不见 -->
              <el-checkbox v-model="row.visible" :true-value="1" :false-value="0" :disabled="row.status !== 1"/>
            </template>
          </el-table-column>
          <el-table-column label="卡片" min-width="200">
            <template #default="{ row }">
              <p class="text-[15px] font-medium text-slate-800">{{ row.widgetName }}</p>
              <p class="text-[13px] text-slate-400">{{ row.widgetCode }}</p>
            </template>
          </el-table-column>
          <el-table-column label="分区" width="110">
            <template #default="{ row }">
              <span class="text-[15px] text-slate-600">{{ areaLabel(row.area) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="所需权限码" min-width="220">
            <template #default="{ row }">
              <span v-if="row.permission" class="text-[14px] text-slate-600">{{ row.permission }}</span>
              <span v-else class="text-[14px] text-slate-400">登录即可见</span>
            </template>
          </el-table-column>
          <el-table-column label="宽度" width="90" align="center">
            <template #default="{ row }">
              <span class="text-[15px] text-slate-600">{{ row.span }}/24</span>
            </template>
          </el-table-column>
          <el-table-column label="顺序" width="110" align="center">
            <template #default="{ row }">
              <el-input-number v-model="row.sortOrder" :min="0" :max="9999" :step="10" size="small" controls-position="right"/>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status === 1" type="success" size="small">已上线</el-tag>
              <el-tag v-else type="info" size="small">未上线</el-tag>
            </template>
          </el-table-column>
        </el-table>

        <p class="mt-3 text-[14px] leading-relaxed text-slate-500">
          只有「已上线」且该角色在<span class="font-medium text-slate-700">角色管理 → 菜单权限</span>里确实持有对应权限码的卡片才会出现在首页。
          取消勾选不会删卡片，重新勾上即恢复。
        </p>
      </el-tab-pane>

      <el-tab-pane label="卡片注册表" name="widget">
        <div class="flex flex-wrap items-center gap-3 pb-3">
          <el-input
              v-model="widgetQuery.keyword"
              placeholder="卡片编码 / 名称"
              clearable
              style="width: 220px"
              @keyup.enter="handleWidgetSearch"
          />
          <el-select v-model="widgetQuery.area" placeholder="分区" clearable style="width: 150px" :fit-input-width="false">
            <el-option v-for="a in AREA_OPTIONS" :key="a.value" :label="a.label" :value="a.value"/>
          </el-select>
          <el-select v-model="widgetQuery.status" placeholder="状态" clearable style="width: 130px" :fit-input-width="false">
            <el-option label="已上线" :value="1"/>
            <el-option label="未上线" :value="0"/>
          </el-select>
          <el-button type="primary" :icon="Search" @click="handleWidgetSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleWidgetReset">重置</el-button>
          <el-button class="ml-auto" type="primary" :icon="Plus" @click="openAdd">新增卡片</el-button>
        </div>

        <el-table v-loading="widgetLoading" :data="widgetRows" border row-key="id">
          <el-table-column label="编码" prop="widgetCode" min-width="160"/>
          <el-table-column label="名称" prop="widgetName" min-width="140"/>
          <el-table-column label="分区" width="110">
            <template #default="{ row }">{{ areaLabel(row.area) }}</template>
          </el-table-column>
          <el-table-column label="取数来源" prop="apiKey" min-width="180" show-overflow-tooltip/>
          <el-table-column label="权限码" min-width="200" show-overflow-tooltip>
            <template #default="{ row }">
              <span v-if="row.permission">{{ row.permission }}</span>
              <span v-else class="text-slate-400">登录即可见</span>
            </template>
          </el-table-column>
          <el-table-column label="宽度" width="80" align="center">
            <template #default="{ row }">{{ row.span }}</template>
          </el-table-column>
          <el-table-column label="顺序" prop="sortOrder" width="80" align="center"/>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.status === 1" type="success" size="small">已上线</el-tag>
              <el-tag v-else type="info" size="small">未上线</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" :icon="Delete" @click="removeWidget(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="widgetPage.pageNum"
              v-model:page-size="widgetPage.pageSize"
              :total="widgetPage.total"
              :page-sizes="PAGE_SIZES"
              layout="total, sizes, prev, pager, next, jumper"
              background
              @size-change="handleWidgetSearch"
              @current-change="loadWidgets"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="卡片编码" required>
          <el-input v-model="form.widgetCode" :disabled="!!form.id" placeholder="与后端 provider 的 widgetCode 一字不差，如 myTodo"/>
        </el-form-item>
        <el-form-item label="卡片名称" required>
          <el-input v-model="form.widgetName" placeholder="首页卡片标题，如 我的待办"/>
        </el-form-item>
        <el-form-item label="分区" required>
          <el-select v-model="form.area" style="width: 100%" :fit-input-width="false">
            <el-option v-for="a in AREA_OPTIONS" :key="a.value" :label="a.label" :value="a.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="取数来源" required>
          <el-input v-model="form.apiKey" placeholder="<模块>.<summaryKey>，如 report.hospitalToday（仅对账排查用）"/>
        </el-form-item>
        <el-form-item label="权限码">
          <el-input v-model="form.permission" placeholder="留空=登录即可见；否则必须是 sys_menu 里已有的码，如 report:stats:list"/>
        </el-form-item>
        <el-form-item label="默认宽度">
          <el-input-number v-model="form.defaultSpan" :min="1" :max="24" :step="6"/>
          <span class="ml-2 text-[14px] text-slate-400">24 列栅格，6=四分之一宽、24=整行</span>
        </el-form-item>
        <el-form-item label="顺序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" :step="10"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1" :label="1">已上线</el-radio>
            <el-radio :value="0" :label="0">未上线</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="这张卡的数据口径、归属模块、后续要拆到哪"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitWidget">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
