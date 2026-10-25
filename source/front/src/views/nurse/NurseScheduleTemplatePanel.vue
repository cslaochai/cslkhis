<template>
  <div class="nurse-template-panel">
    <!-- 统计卡片：我的模板 / 已用模板 / 已删除模板 / 权限组模板 -->
    <div class="mb-3 grid grid-cols-2 gap-3 sm:grid-cols-4">
      <div class="rounded-md border border-slate-200 bg-white px-4 py-2 text-center transition hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md">
        <p class="text-xl font-semibold text-slate-800">{{ stats.myCount }}</p>
        <p class="text-xs text-slate-400">我的模板</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-2 text-center transition hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md">
        <p class="text-xl font-semibold text-emerald-600">{{ stats.usedCount }}</p>
        <p class="text-xs text-slate-400">已用模板（套用过的）</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-2 text-center transition hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md">
        <p class="text-xl font-semibold text-slate-800">{{ stats.deletedCount }}</p>
        <p class="text-xs text-slate-400">已删除模板</p>
      </div>
      <div class="rounded-md border border-slate-200 bg-white px-4 py-2 text-center transition hover:-translate-y-0.5 hover:border-blue-300 hover:shadow-md">
        <p class="text-xl font-semibold text-blue-600">{{ stats.sharedCount }}</p>
        <p class="text-xs text-slate-400">权限组模板（共享）</p>
      </div>
    </div>

    <el-card shadow="never">
      <el-tabs v-model="activeInnerTab">
        <!-- ============== 排班模板规则 ============== -->
        <el-tab-pane label="排班模板规则" name="rules">
          <div class="mb-3 flex items-center gap-2">
            <span class="text-sm text-slate-400">班次实用排班模式（APN / 8-8 / 小组排班）脚手架，新建模板时可参考，模板仍可自由增删明细。</span>
            <el-button v-perm="PERM_EDIT" class="ml-auto" :icon="Plus" type="primary" @click="openRuleEdit()">新增规则</el-button>
          </div>
          <el-table v-loading="rulesLoading" :data="rules" border size="small" style="width: 100%">
            <el-table-column label="模式名称" prop="ruleName" min-width="140"/>
            <el-table-column label="编码" prop="ruleCode" width="110"/>
            <el-table-column label="模式明细" prop="patternDetail" min-width="260" show-overflow-tooltip/>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="备注" prop="remark" min-width="160" show-overflow-tooltip/>
            <el-table-column label="操作" width="150" align="center" fixed="right">
              <template #default="{ row }">
                <el-button v-perm="PERM_EDIT" link type="primary" size="small" @click="openRuleEdit(row)">编辑</el-button>
                <el-button v-perm="PERM_EDIT" link type="danger" size="small" @click="deleteRule(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============== 排班模板 ============== -->
        <el-tab-pane label="排班模板" name="templates">
          <div class="mb-3 flex flex-wrap items-center gap-2">
            <el-input v-model="templateQuery.keyword" clearable placeholder="模板名称" style="width: 160px"
                      @keyup.enter="reloadTemplates"/>
            <el-select v-model="templateQuery.unitType" clearable placeholder="单元类型" style="width: 120px">
              <el-option v-for="u in unitTypeOptions" :key="u.value" :label="u.label" :value="u.value"/>
            </el-select>
            <el-select v-model="templateQuery.wardId" clearable filterable placeholder="排班单元" style="width: 200px">
              <el-option v-for="u in units" :key="`${u.unitType}:${u.wardId}`"
                         :label="`${u.unitType === 2 ? '门诊' : '病区'} · ${u.wardName}`" :value="u.wardId"/>
            </el-select>
            <el-select v-model="templateQuery.status" clearable placeholder="状态" style="width: 100px">
              <el-option label="启用" :value="1"/>
              <el-option label="停用" :value="0"/>
            </el-select>
            <el-select v-model="templateQuery.scope" clearable placeholder="范围" style="width: 120px">
              <el-option label="个人" :value="0"/>
              <el-option label="权限组共享" :value="1"/>
            </el-select>
            <el-button type="primary" :icon="Search" @click="reloadTemplates">查询</el-button>
            <el-button v-perm="PERM_ADD" class="ml-auto" :icon="MagicStick" type="primary" plain @click="openApply()">套用模板到周</el-button>
            <el-button v-perm="PERM_ADD" :icon="Plus" type="primary" @click="openTemplateEdit()">新增模板</el-button>
          </div>
          <el-table v-loading="templatesLoading" :data="templates" border size="small" style="width: 100%">
            <el-table-column label="模板名称" prop="name" min-width="160"/>
            <el-table-column label="单元" min-width="180">
              <template #default="{ row }">
                {{ row.unitType === 2 ? '门诊' : '病区' }} · {{ row.wardName }}
              </template>
            </el-table-column>
            <el-table-column label="单双周" width="90" align="center">
              <template #default="{ row }">{{ parityText(row.weekParity) }}</template>
            </el-table-column>
            <el-table-column label="生效期" min-width="180">
              <template #default="{ row }">
                {{ row.validFrom || '不限' }} ~ {{ row.validUntil || '不限' }}
              </template>
            </el-table-column>
            <el-table-column label="范围" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.scope === 1 ? 'warning' : 'info'" size="small">{{ row.scope === 1 ? '共享' : '个人' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="明细" prop="itemCount" width="70" align="center"/>
            <el-table-column label="已套用" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="row.applied ? 'success' : 'info'" size="small">{{ row.applied ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建人" prop="creatorName" width="100"/>
            <el-table-column label="操作" width="220" align="center" fixed="right">
              <template #default="{ row }">
                <el-button v-perm="PERM_ADD" link type="primary" size="small" @click="openApply(row)">套用</el-button>
                <el-button link type="primary" size="small" @click="viewTemplate(row)">明细</el-button>
                <el-button v-perm="PERM_EDIT" link type="primary" size="small" @click="openTemplateEdit(row)">编辑</el-button>
                <el-button v-perm="PERM_EDIT" link type="warning" size="small" @click="toggleStatus(row)">
                  {{ row.status === 1 ? '停用' : '启用' }}
                </el-button>
                <el-button v-perm="PERM_DELETE" link type="danger" size="small" @click="removeTemplate(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-2 flex justify-end">
            <el-pagination :current-page="templateQuery.pageNum" :page-size="templateQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="templatesTotal" background layout="total, sizes, prev, pager, next"
                           @current-change="(v) => { templateQuery.pageNum = v; loadTemplates() }"
                           @size-change="(v) => { templateQuery.pageSize = v; templateQuery.pageNum = 1; loadTemplates() }"/>
          </div>
        </el-tab-pane>

        <!-- ============== 适用记录 ============== -->
        <el-tab-pane label="适用记录" name="applies">
          <div class="mb-3 flex flex-wrap items-center gap-2">
            <el-select v-model="applyQuery.unitType" clearable placeholder="单元类型" style="width: 120px">
              <el-option v-for="u in unitTypeOptions" :key="u.value" :label="u.label" :value="u.value"/>
            </el-select>
            <el-select v-model="applyQuery.wardId" clearable filterable placeholder="排班单元" style="width: 200px">
              <el-option v-for="u in units" :key="`${u.unitType}:${u.wardId}`"
                         :label="`${u.unitType === 2 ? '门诊' : '病区'} · ${u.wardName}`" :value="u.wardId"/>
            </el-select>
            <el-button type="primary" :icon="Search" @click="reloadApplies">查询</el-button>
          </div>
          <el-table v-loading="appliesLoading" :data="applies" border size="small" style="width: 100%">
            <el-table-column label="模板" prop="templateName" min-width="150"/>
            <el-table-column label="单元" min-width="170">
              <template #default="{ row }">
                {{ row.unitType === 2 ? '门诊' : '病区' }} · {{ row.wardName }}
              </template>
            </el-table-column>
            <el-table-column label="套用周" min-width="200">
              <template #default="{ row }">{{ row.applyWeekStart }} ~ {{ row.applyWeekEnd }}</template>
            </el-table-column>
            <el-table-column label="操作人" prop="appliedByName" width="100"/>
            <el-table-column label="套用时间" prop="appliedAt" width="160"/>
            <el-table-column label="生成/跳过" width="110" align="center">
              <template #default="{ row }">
                <span class="text-emerald-600">{{ row.generatedCount }}</span>
                <span class="text-slate-400"> / </span>
                <span class="text-amber-600">{{ row.skippedCount }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 0 ? 'success' : 'warning'" size="small">{{ row.status === 0 ? '成功' : '部分跳过' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="说明" prop="remark" min-width="220" show-overflow-tooltip/>
          </el-table>
          <div class="mt-2 flex justify-end">
            <el-pagination :current-page="applyQuery.pageNum" :page-size="applyQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="appliesTotal" background layout="total, sizes, prev, pager, next"
                           @current-change="(v) => { applyQuery.pageNum = v; loadApplies() }"
                           @size-change="(v) => { applyQuery.pageSize = v; applyQuery.pageNum = 1; loadApplies() }"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- ============== 模板新增/编辑弹窗 ============== -->
    <el-dialog v-model="tplEditVisible" :title="tplForm.id ? '编辑模板' : '新增模板'" width="960px" destroy-on-close
               @closed="tplEditVisible = false">
      <el-form :model="tplForm" label-width="92px">
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="模板名称" required>
              <el-input v-model="tplForm.name" placeholder="如「内科一组周模板」" maxlength="100"/>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="单元类型" required>
              <el-select v-model="tplForm.unitType" :disabled="!!tplForm.id" @change="onUnitTypeChange">
                <el-option v-for="u in unitTypeOptions" :key="u.value" :label="u.label" :value="u.value"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="排班单元" required>
              <el-select v-model="tplForm.wardId" :disabled="!!tplForm.id" filterable placeholder="选择单元"
                         :loading="unitLoading" @change="loadNurses">
                <el-option v-for="u in units" :key="`${u.unitType}:${u.wardId}`"
                           :label="`${u.unitType === 2 ? '门诊' : '病区'} · ${u.wardName}`" :value="u.wardId"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="6">
            <el-form-item label="单双周">
              <el-select v-model="tplForm.weekParity">
                <el-option v-for="p in parityOptions" :key="p.value" :label="p.label" :value="p.value"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="9">
            <el-form-item label="生效期">
              <el-date-picker v-model="validRange" type="daterange" range-separator="至"
                              start-placeholder="起始" end-placeholder="截止" value-format="YYYY-MM-DD" style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="4">
            <el-form-item label="范围">
              <el-select v-model="tplForm.scope">
                <el-option label="个人" :value="0"/>
                <el-option label="共享" :value="1"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="状态">
              <el-select v-model="tplForm.status">
                <el-option label="启用" :value="1"/>
                <el-option label="停用" :value="0"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="tplForm.remark" type="textarea" :rows="2" maxlength="500" placeholder="可选"/>
        </el-form-item>

        <el-divider content-position="left">明细行（护士 × 星期 × 出勤/班次）</el-divider>
        <div class="mb-2 flex items-center gap-2">
          <el-button :icon="Plus" size="small" type="primary" plain @click="addItemRow">新增一行</el-button>
          <span class="text-xs text-slate-400">出勤=上班时需选班次；其余状态班次自动置空。至少一行。</span>
        </div>
        <el-table :data="tplForm.items" border size="small" style="width: 100%">
          <el-table-column label="#" width="48" align="center">
            <template #default="{ $index }">{{ $index + 1 }}</template>
          </el-table-column>
          <el-table-column label="护士" min-width="160">
            <template #default="{ row }">
              <el-select v-model="row.employeeId" filterable placeholder="选择护士" :loading="nurseLoading"
                         @change="(v) => onItemNurseChange(row, v)">
                <el-option v-for="n in nurses" :key="n.employeeId" :label="`${n.nurseName}（${n.empCode || ''}）`" :value="n.employeeId"/>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="星期" width="110">
            <template #default="{ row }">
              <el-select v-model="row.weekDay">
                <el-option v-for="d in weekDayOptions" :key="d.value" :label="d.label" :value="d.value"/>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="出勤状态" width="110">
            <template #default="{ row }">
              <el-select v-model="row.scheduleStatus" @change="() => onItemStatusChange(row)">
                <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value"/>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="班次" width="160">
            <template #default="{ row }">
              <el-select v-model="row.shiftId" :disabled="row.scheduleStatus !== 1" clearable
                         placeholder="上班需选班次" :loading="shiftLoading">
                <el-option v-for="s in shifts" :key="s.shiftId" :label="s.shiftName" :value="s.shiftId"/>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.remark" maxlength="500" placeholder="可选"/>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="70" align="center">
            <template #default="{ $index }">
              <el-button link type="danger" size="small" @click="tplForm.items.splice($index, 1)">删</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="tplEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>

    <!-- ============== 套用模板弹窗 ============== -->
    <el-dialog v-model="applyVisible" title="套用模板到周" width="560px" destroy-on-close @closed="applyVisible = false">
      <el-form :model="applyForm" label-width="92px">
        <el-form-item label="选择模板" required>
          <el-select v-model="applyForm.templateId" filterable placeholder="选择模板" :loading="tplOptsLoading"
                     @change="onApplyTemplateChange">
            <el-option v-for="t in applyTplOptions" :key="t.id" :label="t.name" :value="t.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="单元类型" required>
          <el-select v-model="applyForm.unitType" :disabled="!!applyForm.templateId" @change="onApplyUnitTypeChange">
            <el-option v-for="u in unitTypeOptions" :key="u.value" :label="u.label" :value="u.value"/>
          </el-select>
        </el-form-item>
        <el-form-item label="排班单元" required>
          <el-select v-model="applyForm.wardId" :disabled="!!applyForm.templateId" filterable placeholder="选择单元"
                     :loading="unitLoading">
            <el-option v-for="u in units" :key="`${u.unitType}:${u.wardId}`"
                       :label="`${u.unitType === 2 ? '门诊' : '病区'} · ${u.wardName}`" :value="u.wardId"/>
          </el-select>
        </el-form-item>
        <el-form-item label="目标周">
          <el-date-picker v-model="applyForm.targetWeekStart" type="date" placeholder="默认下周（周一）"
                          value-format="YYYY-MM-DD" :disabled-date="disabledPastDate" style="width: 100%"/>
          <span class="ml-2 text-xs text-slate-400">留空=下周，已排的格子不覆盖。</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" :loading="applying" @click="doApply">套用</el-button>
      </template>
    </el-dialog>

    <!-- ============== 模板明细查看弹窗 ============== -->
    <el-dialog v-model="detailVisible" :title="`模板明细 · ${detailData.name || ''}`" width="720px" destroy-on-close>
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="单元">{{ detailData.unitType === 2 ? '门诊' : '病区' }} · {{ detailData.wardName }}</el-descriptions-item>
        <el-descriptions-item label="单双周">{{ parityText(detailData.weekParity) }}</el-descriptions-item>
        <el-descriptions-item label="生效期">{{ detailData.validFrom || '不限' }} ~ {{ detailData.validUntil || '不限' }}</el-descriptions-item>
        <el-descriptions-item label="范围/状态">
          {{ detailData.scope === 1 ? '共享' : '个人' }} / {{ detailData.status === 1 ? '启用' : '停用' }}
        </el-descriptions-item>
      </el-descriptions>
      <el-table :data="detailData.items || []" border size="small" class="mt-3" style="width: 100%">
        <el-table-column label="护士" prop="employeeName" width="120"/>
        <el-table-column label="星期" width="90">
          <template #default="{ row }">{{ weekDayText(row.weekDay) }}</template>
        </el-table-column>
        <el-table-column label="出勤状态" width="100">
          <template #default="{ row }">{{ statusText(row.scheduleStatus) }}</template>
        </el-table-column>
        <el-table-column label="班次" prop="shiftName" min-width="140"/>
        <el-table-column label="备注" prop="remark" min-width="160" show-overflow-tooltip/>
      </el-table>
    </el-dialog>

    <!-- ============== 规则编辑弹窗 ============== -->
    <el-dialog v-model="ruleEditVisible" :title="ruleForm.id ? '编辑规则' : '新增规则'" width="560px" destroy-on-close>
      <el-form :model="ruleForm" label-width="92px">
        <el-form-item label="模式名称" required>
          <el-input v-model="ruleForm.ruleName" placeholder="如「APN 三班倒」" maxlength="100"/>
        </el-form-item>
        <el-form-item label="编码" required>
          <el-input v-model="ruleForm.ruleCode" placeholder="APN / 8-8 / GROUP" maxlength="50"/>
        </el-form-item>
        <el-form-item label="模式明细">
          <el-input v-model="ruleForm.patternDetail" type="textarea" :rows="4"
                    placeholder='JSON，如 {"shifts":["早班","中班","夜班"],"desc":"三班倒覆盖24h"}'/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="ruleForm.status">
            <el-option label="启用" :value="1"/>
            <el-option label="停用" :value="0"/>
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="ruleForm.remark" type="textarea" :rows="2" maxlength="500" placeholder="可选"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSaving" @click="saveRule">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, reactive, computed, onMounted, nextTick} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Plus, Search, MagicStick} from '@element-plus/icons-vue'
import {PAGE_SIZES, DEFAULT_PAGE_SIZE} from '@/lib/pagination'
import {PERM_ADD, PERM_EDIT, PERM_DELETE} from '@/lib/nurseSchedule'
import {
  getTemplateStats, pageTemplates, getTemplateDetail, upsertTemplate, deleteTemplate,
  updateTemplateStatus, listTemplateRules, upsertTemplateRule, deleteTemplateRule,
  applyTemplateToWeek, pageApplies, getNurseShiftOptions
} from '@/api/nurseScheduleTemplate'
import {getNurseUnitSelectList, getNurseSelectList} from '@/api/nurseSchedule'

const activeInnerTab = ref('rules')

// 字典/选项
const statusOptions = [
  {label: '上班', value: 1}, {label: '休息', value: 2}, {label: '请假', value: 3},
  {label: '培训', value: 4}, {label: '停班', value: 5}
]
const weekDayOptions = [
  {label: '周一', value: 1}, {label: '周二', value: 2}, {label: '周三', value: 3}, {label: '周四', value: 4},
  {label: '周五', value: 5}, {label: '周六', value: 6}, {label: '周日', value: 7}
]
const unitTypeOptions = [{label: '病区', value: 1}, {label: '门诊科室', value: 2}]
const parityOptions = [{label: '每周', value: 0}, {label: '单周', value: 1}, {label: '双周', value: 2}]
const statusText = (v) => (statusOptions.find(s => s.value === v) || {}).label || '—'
const weekDayText = (v) => (weekDayOptions.find(d => d.value === v) || {}).label || '—'
const parityText = (v) => (parityOptions.find(p => p.value === v) || {}).label || '每周'

// 下拉数据
const units = ref([])
const nurses = ref([])
const shifts = ref([])
const unitLoading = ref(false)
const nurseLoading = ref(false)
const shiftLoading = ref(false)

// 统计
const stats = ref({myCount: 0, usedCount: 0, deletedCount: 0, sharedCount: 0})

// 规则
const rules = ref([])
const rulesLoading = ref(false)
const ruleEditVisible = ref(false)
const ruleSaving = ref(false)
const ruleForm = reactive({id: null, ruleName: '', ruleCode: '', patternDetail: '', status: 1, remark: ''})

// 模板
const templates = ref([])
const templatesTotal = ref(0)
const templatesLoading = ref(false)
const templateQuery = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, keyword: '', unitType: null, wardId: null, status: null, scope: null})

// 套用记录
const applies = ref([])
const appliesTotal = ref(0)
const appliesLoading = ref(false)
const applyQuery = reactive({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, unitType: null, wardId: null})

// 模板编辑
const tplEditVisible = ref(false)
const saving = ref(false)
const tplForm = reactive({
  id: null, name: '', unitType: 1, wardId: null, weekParity: 0,
  validFrom: null, validUntil: null, scope: 0, status: 1, remark: '', items: []
})
const validRange = ref([])

// 套用
const applyVisible = ref(false)
const applying = ref(false)
const tplOptsLoading = ref(false)
const applyTplOptions = ref([])
const applyForm = reactive({templateId: null, unitType: 1, wardId: null, targetWeekStart: ''})

// 明细查看
const detailVisible = ref(false)
const detailData = reactive({name: '', unitType: 1, wardName: '', weekParity: 0, validFrom: '', validUntil: '', scope: 0, status: 1, items: []})

const nextMonday = () => {
  const d = new Date()
  const day = d.getDay() || 7
  d.setDate(d.getDate() + (8 - day))
  const y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), dd = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${dd}`
}
const disabledPastDate = (date) => date && date < new Date(new Date().toDateString())

onMounted(async () => {
  await loadUnits()
  await loadShifts()
  await loadStats()
  await loadRules()
  await loadTemplates()
  await loadApplies()
})

async function loadUnits() {
  unitLoading.value = true
  try {
    const res = await getNurseUnitSelectList({})
    units.value = res.data || []
  } finally {
    unitLoading.value = false
  }
}

async function loadShifts() {
  shiftLoading.value = true
  try {
    const res = await getNurseShiftOptions()
    shifts.value = res.data || []
  } finally {
    shiftLoading.value = false
  }
}

async function loadStats() {
  try {
    const res = await getTemplateStats()
    stats.value = res.data || stats.value
  } catch (e) { /* 全局拦截 */ }
}

async function loadRules() {
  rulesLoading.value = true
  try {
    const res = await listTemplateRules()
    rules.value = res.data || []
  } finally {
    rulesLoading.value = false
  }
}

async function loadTemplates() {
  templatesLoading.value = true
  try {
    const res = await pageTemplates({
      pageNum: templateQuery.pageNum, pageSize: templateQuery.pageSize,
      keyword: templateQuery.keyword || undefined,
      unitType: templateQuery.unitType, wardId: templateQuery.wardId,
      status: templateQuery.status, scope: templateQuery.scope
    })
    templates.value = res.data?.records || []
    templatesTotal.value = Number(res.data?.total || 0)
  } finally {
    templatesLoading.value = false
  }
}

function reloadTemplates() {
  templateQuery.pageNum = 1
  loadTemplates()
}

async function loadApplies() {
  appliesLoading.value = true
  try {
    const res = await pageApplies({
      pageNum: applyQuery.pageNum, pageSize: applyQuery.pageSize,
      unitType: applyQuery.unitType, wardId: applyQuery.wardId
    })
    applies.value = res.data?.records || []
    appliesTotal.value = Number(res.data?.total || 0)
  } finally {
    appliesLoading.value = false
  }
}

function reloadApplies() {
  applyQuery.pageNum = 1
  loadApplies()
}

// ---------- 规则 ----------
function openRuleEdit(rule) {
  Object.assign(ruleForm, {
    id: rule?.id ?? null, ruleName: rule?.ruleName ?? '', ruleCode: rule?.ruleCode ?? '',
    patternDetail: rule?.patternDetail ?? '', status: rule?.status ?? 1, remark: rule?.remark ?? ''
  })
  ruleEditVisible.value = true
}

async function saveRule() {
  if (!ruleForm.ruleName || !ruleForm.ruleCode) {
    ElMessage.warning('请填写模式名称与编码')
    return
  }
  ruleSaving.value = true
  try {
    await upsertTemplateRule({...ruleForm})
    ElMessage.success(ruleForm.id ? '规则已更新' : '规则已创建')
    ruleEditVisible.value = false
    await loadRules()
  } catch (e) { /* 拦截 */ } finally {
    ruleSaving.value = false
  }
}

async function deleteRule(rule) {
  try {
    await ElMessageBox.confirm(`确认删除规则「${rule.ruleName}」？`, '删除规则', {type: 'warning'})
  } catch {
    return
  }
  try {
    await deleteTemplateRule(rule.id)
    ElMessage.success('已删除')
    await loadRules()
  } catch (e) { /* 拦截 */ }
}

// ---------- 模板 ----------
async function loadNurses(unitType, wardId) {
  if (!wardId) {
    nurses.value = []
    return
  }
  nurseLoading.value = true
  try {
    const res = await getNurseSelectList(unitType, wardId, '')
    nurses.value = res.data || []
  } finally {
    nurseLoading.value = false
  }
}

function onUnitTypeChange() {
  tplForm.wardId = null
  nurses.value = []
}

function addItemRow() {
  tplForm.items.push({employeeId: null, employeeName: '', weekDay: 1, scheduleStatus: 1, shiftId: null, remark: ''})
}

function onItemNurseChange(row, v) {
  const n = nurses.value.find(x => x.employeeId === v)
  row.employeeName = n ? n.nurseName : ''
}

function onItemStatusChange(row) {
  if (row.scheduleStatus !== 1) row.shiftId = null
}

async function openTemplateEdit(tpl) {
  if (tpl?.id) {
    const res = await getTemplateDetail(tpl.id)
    const d = res.data
    Object.assign(tplForm, {
      id: d.id, name: d.name, unitType: d.unitType, wardId: d.wardId, weekParity: d.weekParity || 0,
      validFrom: d.validFrom, validUntil: d.validUntil, scope: d.scope ?? 0, status: d.status ?? 1,
      remark: d.remark || '', items: (d.items || []).map(i => ({
        employeeId: i.employeeId, employeeName: i.employeeName, weekDay: i.weekDay,
        scheduleStatus: i.scheduleStatus, shiftId: i.shiftId, remark: i.remark || ''
      }))
    })
    validRange.value = d.validFrom && d.validUntil ? [d.validFrom, d.validUntil] : []
    await loadNurses(d.unitType, d.wardId)
  } else {
    Object.assign(tplForm, {
      id: null, name: '', unitType: 1, wardId: null, weekParity: 0,
      validFrom: null, validUntil: null, scope: 0, status: 1, remark: '', items: []
    })
    validRange.value = []
    nurses.value = []
  }
  tplEditVisible.value = true
}

async function saveTemplate() {
  if (!tplForm.name || !tplForm.wardId) {
    ElMessage.warning('请填写模板名称并选择排班单元')
    return
  }
  if (!tplForm.items.length) {
    ElMessage.warning('模板至少要有一行明细')
    return
  }
  for (const it of tplForm.items) {
    if (!it.employeeId) {
      ElMessage.warning('存在未选择护士的明细行')
      return
    }
    if (it.scheduleStatus === 1 && !it.shiftId) {
      ElMessage.warning('出勤=上班的明细行必须选择班次')
      return
    }
  }
  const payload = {
    id: tplForm.id, name: tplForm.name, unitType: tplForm.unitType, wardId: tplForm.wardId,
    weekParity: tplForm.weekParity, validFrom: validRange.value?.[0] || null,
    validUntil: validRange.value?.[1] || null, scope: tplForm.scope, status: tplForm.status,
    remark: tplForm.remark, items: tplForm.items
  }
  saving.value = true
  try {
    await upsertTemplate(payload)
    ElMessage.success(payload.id ? '模板已更新' : '模板已创建')
    tplEditVisible.value = false
    await Promise.all([loadStats(), loadTemplates()])
  } catch (e) { /* 拦截 */ } finally {
    saving.value = false
  }
}

async function removeTemplate(tpl) {
  try {
    await ElMessageBox.confirm(`确认删除模板「${tpl.name}」？删除后联动明细一并失效。`, '删除模板', {type: 'warning'})
  } catch {
    return
  }
  try {
    await deleteTemplate(tpl.id)
    ElMessage.success('已删除')
    await Promise.all([loadStats(), loadTemplates()])
  } catch (e) { /* 拦截 */ }
}

async function toggleStatus(tpl) {
  const next = tpl.status === 1 ? 0 : 1
  try {
    await updateTemplateStatus(tpl.id, next)
    ElMessage.success(next === 1 ? '已启用' : '已停用')
    await Promise.all([loadStats(), loadTemplates()])
  } catch (e) { /* 拦截 */ }
}

async function viewTemplate(tpl) {
  const res = await getTemplateDetail(tpl.id)
  Object.assign(detailData, res.data)
  detailVisible.value = true
}

// ---------- 套用 ----------
async function openApply(tpl) {
  applyForm.templateId = tpl?.id ?? null
  applyForm.unitType = tpl?.unitType ?? 1
  applyForm.wardId = tpl?.wardId ?? null
  applyForm.targetWeekStart = ''
  if (!applyTplOptions.value.length) {
    tplOptsLoading.value = true
    try {
      const res = await pageTemplates({pageNum: 1, pageSize: 300, status: 1})
      applyTplOptions.value = res.data?.records || []
    } finally {
      tplOptsLoading.value = false
    }
  }
  if (tpl?.id) {
    applyForm.unitType = tpl.unitType
    applyForm.wardId = tpl.wardId
  }
  applyVisible.value = true
}

function onApplyTemplateChange(v) {
  const t = applyTplOptions.value.find(x => x.id === v)
  if (t) {
    applyForm.unitType = t.unitType
    applyForm.wardId = t.wardId
  }
}

function onApplyUnitTypeChange() {
  applyForm.wardId = null
}

async function doApply() {
  if (!applyForm.templateId || !applyForm.wardId) {
    ElMessage.warning('请选择模板与排班单元')
    return
  }
  applying.value = true
  try {
    const res = await applyTemplateToWeek({
      templateId: applyForm.templateId, unitType: applyForm.unitType,
      wardId: applyForm.wardId, targetWeekStart: applyForm.targetWeekStart || undefined
    })
    ElMessage.success(res.message || '套用完成')
    applyVisible.value = false
    await Promise.all([loadStats(), loadApplies(), loadTemplates()])
  } catch (e) { /* 拦截 */ } finally {
    applying.value = false
  }
}

defineExpose({openApply})
</script>
