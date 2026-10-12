<template>
  <!-- 角色工作台配置弹框：勾选卡片 + 登录落点（原「工作台配置 → 角色工作台」页签迁入，交互同 RoleMenuEdit） -->
  <el-dialog
      v-model="visible"
      :title="`工作台配置 - ${props.role?.roleName || ''}`"
      destroy-on-close
      width="980px"
  >
    <div v-loading="roleLoading">
      <!-- 视图切换：卡片配置 / 效果预览（实时反映当前勾选，不保存也可预览） -->
      <div class="mb-3 flex items-center justify-between gap-3">
        <el-radio-group v-model="viewMode">
          <el-radio-button value="config">卡片配置</el-radio-button>
          <el-radio-button value="preview">效果预览</el-radio-button>
        </el-radio-group>
        <span class="text-[14px] text-slate-500">已勾选 {{ checkedCount() }} 张</span>
      </div>

      <div v-if="viewMode === 'config'" class="flex flex-col gap-3">
        <!-- 登录落点 -->
        <div class="flex flex-wrap items-center gap-3">
          <span class="text-[15px] text-slate-600">登录落点</span>
          <el-radio-group v-model="landingScope">
            <el-radio v-for="o in LANDING_OPTIONS" :key="o.value" :value="o.value">
              {{ o.label }}
            </el-radio>
          </el-radio-group>
        </div>

        <!-- 卡片勾选列表：后端已强过滤，只返回已上线卡片（未上线卡勾了也不渲染，不参与配置） -->
        <el-table :data="roleRows" border max-height="46vh" row-key="id">
          <el-table-column align="center" label="显示" width="70">
            <template #default="{ row }">
              <el-checkbox v-model="row.visible" :false-value="0" :true-value="1"/>
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
          <el-table-column align="center" label="宽度" width="90">
            <template #default="{ row }">
              <span class="text-[15px] text-slate-600">{{ row.span }}/24</span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="顺序" width="110">
            <template #default="{ row }">
              <el-input-number v-model="row.sortOrder" :max="9999" :min="0" :step="10" controls-position="right"
                               size="small"/>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 预览视图：按当前勾选实时模拟首页布局（骨架占位，不调业务接口） -->
      <div v-else>
        <RoleWorkbenchPreview :widgets="checkedWidgets"/>
      </div>
    </div>

    <template #footer>
      <div class="flex items-center justify-between">
        <el-button :icon="Refresh" :loading="roleLoading" @click="loadRoleConfig">重新加载</el-button>
        <div class="flex gap-2">
          <el-button @click="visible = false">取消</el-button>
          <el-button :loading="savingRole" type="primary" @click="saveRoleConfig">保存</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, ref, watch} from 'vue'
import {ElMessage} from 'element-plus'
import {Refresh} from '@element-plus/icons-vue'
import {getRoleWorkbenchConfig, roleWorkbenchConfigUpsert} from '@/api/workbench'
import RoleWorkbenchPreview from '@/views/system/role/RoleWorkbenchPreview.vue'

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  // 角色管理列表当前行（id/roleName/roleCode）
  role: {type: Object, default: null},
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

/** 卡片分区，与 sys_workbench_widget.area 一字不差 */
const AREA_OPTIONS = [
  {value: 'todo', label: '待办'},
  {value: 'notice', label: '通知'},
  {value: 'entry', label: '快捷入口'},
  {value: 'kpi', label: '数字概览'},
  {value: 'domain', label: '业务领域'},
]
const areaLabel = (a) => AREA_OPTIONS.find(x => x.value === a)?.label || a
/** 落点策略，与后端 landing_scope 同口径 */
const LANDING_OPTIONS = [
  {value: 0, label: '默认（有患者工作站的岗位直达工作站）'},
  {value: 1, label: '一律进工作台'},
  {value: 2, label: '一律进患者工作站'},
]

const landingScope = ref(0)
const roleRows = ref([])
const roleLoading = ref(false)
const savingRole = ref(false)
// 弹框内两视图：卡片配置（勾选表格）/ 效果预览（首页布局骨架）
const viewMode = ref('config')

watch(visible, (open) => {
  if (!open || !props.role) return
  // 每次打开默认回到配置视图，避免上一次停在预览
  viewMode.value = 'config'
  loadRoleConfig()
})

async function loadRoleConfig() {
  if (!props.role?.id) return
  roleLoading.value = true
  try {
    const res = await getRoleWorkbenchConfig(props.role.id)
    const data = res.data || {}
    landingScope.value = Number(data.landingScope ?? 0)
    roleRows.value = (data.widgets || []).map((w) => ({
      ...w,
      visible: Number(w.visible ?? 0),
      sortOrder: Number(w.sortOrder ?? 0),
    }))
  } catch (e) {
    ElMessage.error(e?.message || '工作台配置加载失败')
  } finally {
    roleLoading.value = false
  }
}

const checkedCount = () => roleRows.value.filter(r => r.visible === 1).length
/** 当前勾选的卡片（预览组件的输入，顺序与宽度在预览内按首页口径归一） */
const checkedWidgets = computed(() => roleRows.value.filter(r => r.visible === 1))

async function saveRoleConfig() {
  if (!props.role?.id) return
  const widgets = roleRows.value
      .filter(r => r.visible === 1)
      .map(r => ({widgetId: r.id, sortOrder: r.sortOrder, visible: 1}))
  if (widgets.length === 0 && landingScope.value !== 0) {
    return ElMessage.warning('落点策略需随卡片一起保存：请先勾选至少一张卡片')
  }
  savingRole.value = true
  try {
    await roleWorkbenchConfigUpsert({
      roleId: props.role.id,
      landingScope: landingScope.value,
      widgets,
    })
    ElMessage.success('已保存，该角色下次登录或切换角色时生效')
    visible.value = false
    emit('saved')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    savingRole.value = false
  }
}
</script>
