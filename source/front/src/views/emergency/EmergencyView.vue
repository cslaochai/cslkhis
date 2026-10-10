<template>
  <div class="space-y-6">
    <!-- 今日总值班：候诊/留观超时升级阶梯的末级收口人（医生 → 当班 → 科主任 → 总值班） -->
    <DutyOfficerBar/>
    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 gap-4 sm:grid-cols-10">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-blue-50 p-2.5">
            <Clock class="h-5 w-5 text-blue-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-blue-600">{{ stats.waiting || 0 }}</p>
            <p class="text-xs text-slate-500">候诊</p>
          </div>
        </div>
      </div>
      <!-- 兜底看板的两个数：超时必须有人被追问，池子里的每一条都还没有负责人 -->
      <div class="rounded-lg border-2 border-red-200 bg-white p-4 shadow-sm" data-testid="emg-stat-overdue">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-red-50 p-2.5">
            <AlarmClock class="h-5 w-5 text-red-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-red-600">{{ stats.overdueWaiting || 0 }}</p>
            <p class="text-xs text-slate-500">超时未接诊</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-amber-200 bg-white p-4 shadow-sm" data-testid="emg-stat-pool">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-amber-50 p-2.5">
            <Bell class="h-5 w-5 text-amber-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-amber-600">{{ stats.unassignedWaiting || 0 }}</p>
            <p class="text-xs text-slate-500">待派单池</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-purple-50 p-2.5">
            <User class="h-5 w-5 text-purple-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-purple-600">{{ stats.treating || 0 }}</p>
            <p class="text-xs text-slate-500">诊治中</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-amber-50 p-2.5">
            <Warning class="h-5 w-5 text-amber-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-amber-600">{{ stats.observation || 0 }}</p>
            <p class="text-xs text-slate-500">留观</p>
          </div>
        </div>
      </div>
      <!-- 留观两档：预警档是"该开始张罗去向"，上限档是"必须今天定下来"。
           分两档是因为只给一个"超时限"时，刚过预警线的人以为不着急，床位一直占着。
           小时数由 /emergency/stats 带回（sys_config 可改），卡片与看板同一口径。 -->
      <div class="cursor-pointer rounded-lg border border-amber-200 bg-white p-4 shadow-sm"
           data-testid="emg-stat-obs-warn"
           @click="pickObsBoard(stats.obsWarnHours)">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-amber-50 p-2.5">
            <Clock class="h-5 w-5 text-amber-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-amber-600">{{ stats.obsOverWarn || 0 }}</p>
            <p class="text-xs text-slate-500">留观超预警{{
                stats.obsWarnHours != null ? ` ≥${stats.obsWarnHours}h` : ''
              }}</p>
          </div>
        </div>
      </div>
      <div class="cursor-pointer rounded-lg border-2 border-red-200 bg-white p-4 shadow-sm"
           data-testid="emg-stat-obs-max"
           @click="pickObsBoard(stats.obsMaxHours)">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-red-50 p-2.5">
            <AlarmClock class="h-5 w-5 text-red-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-red-600">{{ stats.obsOverMax || 0 }}</p>
            <p class="text-xs text-slate-500">留观超上限{{
                stats.obsMaxHours != null ? ` ≥${stats.obsMaxHours}h` : ''
              }}</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border-2 border-red-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-red-50 p-2.5">
            <Warning class="h-5 w-5 text-red-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-red-600">{{ stats.redZone || 0 }}</p>
            <p class="text-xs text-slate-500">红区</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-emerald-50 p-2.5">
            <Check class="h-5 w-5 text-emerald-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-emerald-600">{{ stats.greenChannel || 0 }}</p>
            <p class="text-xs text-slate-500">绿色通道</p>
          </div>
        </div>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-slate-50 p-2.5">
            <Clock class="h-5 w-5 text-slate-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-slate-600">{{ stats.todayTotal || 0 }}</p>
            <p class="text-xs text-slate-500">今日急诊</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 查询条件 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex items-center gap-3">
        <el-input v-model="searchForm.keyword" :prefix-icon="Search" class="!w-60"
                  clearable placeholder="搜索患者、急诊号..." @keyup.enter="handleSearch"/>
        <el-select v-model="searchForm.triageLevel" class="!w-36" clearable placeholder="分诊级别"
                   @change="handleSearch">
          <el-option v-for="(v, k) in triageLevelMap" :key="k" :label="v.label" :value="Number(k)"/>
        </el-select>
        <el-select v-model="searchForm.emergencyStatus" class="!w-28" clearable placeholder="状态"
                   @change="handleSearch">
          <el-option v-for="(v, k) in statusMap" :key="k" :label="v.label" :value="Number(k)"/>
        </el-select>
        <!-- 两个兜底入口：池子=还没人负责的，超时=该被追问的。计数来自 /emergency/stats，页面不自造数字 -->
        <el-checkbox v-model="searchForm.unassignedOnly" data-testid="emg-filter-unassigned" @change="handleSearch">
          只看待派单 {{ stats.unassignedWaiting || 0 }}
        </el-checkbox>
        <el-checkbox v-model="searchForm.overdueOnly" data-testid="emg-filter-overdue" @change="onOverdueOnlyChange">
          只看超时 {{ stats.overdueWaiting || 0 }}
        </el-checkbox>
        <!-- 留观榜：换的是"看谁"，不参与任何写动作；空=不启用 -->
        <el-select v-model="searchForm.obsBoardHours" class="!w-44" clearable
                   data-testid="emg-filter-obsboard" placeholder="留观榜" @change="onObsBoardChange">
          <el-option v-for="o in obsBoardOptions" :key="o.value" :label="o.label" :value="o.value"/>
        </el-select>
        <el-button :icon="Search" type="primary" @click="handleSearch">查询</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        <div class="flex-1"></div>
        <el-button :icon="Refresh" data-testid="emg-btn-ledger" @click="openHoLedger">交班台账</el-button>
        <el-button v-perm="'opd:emergency:handover'" :icon="Switch" data-testid="emg-btn-handover" type="warning"
                   @click="openHandover">交班清零
        </el-button>
        <el-button v-perm="'opd:emergency:add'" :icon="Plus" type="danger" @click="handleAdd">急诊登记</el-button>
      </div>
    </div>

    <!-- 急诊列表 -->
    <div class="rounded-lg border border-slate-200 bg-white shadow-sm" data-testid="emg-table">
      <el-table v-loading="loading" :data="emergencyList" style="width: 100%">
        <el-table-column class-name="font-mono text-sm" label="急诊号" width="170">
          <template #default="{ row }">
            <span class="font-mono text-sm font-medium text-slate-700">{{ row.emergencyNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="患者" min-width="120">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span
                  :class="['inline-flex h-7 w-7 shrink-0 items-center justify-center rounded-full text-xs font-bold text-white',
                           GENDER_DOT[patientAvatarTone(row.gender)]]">
                {{ patientGenderSymbol(row.gender) }}
              </span>
              <div>
                <div class="text-sm font-medium text-slate-900">{{ row.patientName }}</div>
                <div class="text-xs text-slate-400">{{ row.age }}岁</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="分诊" width="110">
          <template #default="{ row }">
            <span
                :class="['inline-block rounded border px-2 py-0.5 text-xs font-medium', triageLevelMap[row.triageLevel]?.color || '']">
              {{ triageLevelMap[row.triageLevel]?.label }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="主诉" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-sm text-slate-700">{{ row.chiefComplaint }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="区域" width="80">
          <template #default="{ row }">
            <span :class="[
              'inline-block rounded px-2 py-0.5 text-xs font-bold',
              row.zone === '红区' ? 'bg-red-100 text-red-700' :
              row.zone === '黄区' ? 'bg-amber-100 text-amber-700' :
              'bg-emerald-100 text-emerald-700'
            ]">{{ row.zone }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="绿色通道" width="100">
          <template #default="{ row }">
            <span v-if="row.greenChannel"
                  class="rounded bg-emerald-50 px-1.5 py-0.5 text-xs font-medium text-emerald-600 border border-emerald-200">
              {{ row.greenChannel }}
            </span>
            <span v-else class="text-xs text-slate-400">—</span>
          </template>
        </el-table-column>
        <el-table-column label="科室/医生" width="150">
          <template #default="{ row }">
            <div class="text-xs text-slate-500">
              <div>{{ row.deptName }}</div>
              <div v-if="row.doctorName" class="text-slate-700">
                {{ row.doctorName }}
                <span v-if="row.assignType === 2 || row.assignType === 4 || row.assignType === 5"
                      class="text-slate-400">
                  （{{ row.assignTypeText }}）
                </span>
              </div>
              <!-- 没有医生 = 还在待派单池里，这件事必须显眼：它决定要不要有人现在去认领 -->
              <el-tooltip v-else :content="row.unassignedReason || '候诊中且尚无接诊医生'" placement="top">
                <span
                    class="inline-block rounded border border-amber-300 bg-amber-50 px-1.5 py-0.5 text-xs font-medium text-amber-700">
                  待派单
                </span>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="入急诊时间" width="150">
          <template #default="{ row }">
            <span class="text-xs text-slate-600">{{ row.admissionTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="留观" width="110">
          <template #default="{ row }">
            <div v-if="row.observationStartTime">
              <div class="text-xs font-medium text-slate-700">{{
                  row.observationBed ? row.observationBed + ' 床' : '未占床'
                }}
              </div>
              <div class="text-xs text-slate-400">{{ getObsTime(row) }}</div>
              <!-- 档位由后端判定（服务端 NOW() 才是事实），到上限档才红底 -->
              <span v-if="row.obsLevelText" :class="['inline-block rounded px-1.5 py-0.5 text-xs font-bold',
                             row.obsLevel === 2 ? 'bg-red-100 text-red-700' : 'bg-amber-100 text-amber-700']"
                    data-testid="emg-obs-level">
                {{ row.obsLevelText }}
              </span>
            </div>
            <span v-else class="text-xs text-slate-400">—</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="等候" width="110">
          <template #default="{ row }">
            <span
                :class="['text-sm', row.overdueLevel === 2 ? 'font-bold text-red-600'
                          : row.overdueLevel === 1 ? 'font-medium text-red-500' : 'text-slate-600']">
              {{ waitText(row) }}
            </span>
            <div v-if="row.overdueText" class="text-xs font-medium text-red-500">{{ row.overdueText }}</div>
          </template>
        </el-table-column>
        <el-table-column align="center" label="状态" width="90">
          <template #default="{ row }">
            <span
                :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', statusMap[row.emergencyStatus]?.color || '']">
              {{ statusMap[row.emergencyStatus]?.label || '未知' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="170">
          <template #default="{ row }">
            <el-button v-if="row.emergencyStatus === 1" v-perm="'opd:emergency:edit'" link size="small" type="primary"
                       @click="handleStatusChange(row, 2)">
              接诊
            </el-button>
            <el-button v-if="row.emergencyStatus === 2" v-perm="'opd:emergency:edit'" link size="small" type="warning"
                       @click="openObs(row)">
              留观
            </el-button>
            <el-button v-if="row.emergencyStatus === 2 || row.emergencyStatus === 3" v-perm="'opd:emergency:edit'"
                       link size="small" type="success"
                       @click="openAdmit(row)">
              转住院
            </el-button>
            <el-button v-if="row.emergencyStatus <= 3" v-perm="'opd:emergency:edit'" link size="small" type="info"
                       @click="handleStatusChange(row, 5)">
              离院
            </el-button>
            <el-button v-if="row.emergencyStatus <= 3" v-perm="'opd:emergency:edit'" link size="small" type="danger"
                       @click="handleStatusChange(row, 6)">
              死亡
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="emergencyList.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
        暂无急诊记录
      </div>
      <div class="mt-4 flex justify-end">
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
    </div>

    <!-- 新增急诊弹窗 -->
    <el-dialog v-model="showAddDialog" destroy-on-close title="急诊登记" width="760px">
      <!-- 患者来源切换 -->
      <div class="mb-4 flex rounded-lg border border-slate-200 bg-slate-50 p-1">
        <button
            :class="addMode === 'existing' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
            class="flex-1 rounded-md py-2 text-sm font-medium transition-colors"
            @click="addMode = 'existing'"
        >选择已有患者
        </button>
        <button
            :class="addMode === 'new' ? 'bg-white text-blue-600 shadow-sm' : 'text-slate-500 hover:text-slate-700'"
            class="flex-1 rounded-md py-2 text-sm font-medium transition-colors"
            @click="addMode = 'new'"
        >录入新患者
        </button>
      </div>

      <el-form :model="addForm" label-position="top">
        <!-- 已有患者模式 -->
        <template v-if="addMode === 'existing'">
          <el-form-item label="选择患者" required>
            <PatientSelect
                v-model="addForm.patientId"
                placeholder="输入姓名 / 患者号 / 手机号 / 身份证号搜索"
                @select="handlePatientSelect"
            />
          </el-form-item>
          <!-- 已选患者信息回显 -->
          <div v-if="addForm.patientId" class="mb-4 rounded-lg border border-blue-200 bg-blue-50 p-3">
            <div class="flex items-center gap-3 text-sm">
              <span :class="['inline-flex h-8 w-8 items-center justify-center rounded-full text-sm font-bold text-white',
                GENDER_DOT[patientAvatarTone(addForm.gender)]]">
                {{ patientGenderSymbol(addForm.gender) }}
              </span>
              <div>
                <span class="font-medium text-slate-900">{{ addForm.patientName }}</span>
                <span class="ml-2 text-xs text-slate-500">{{ addForm.patientNo }} · {{
                    addForm.age
                  }}岁 · {{ addForm.phone }}</span>
              </div>
            </div>
          </div>
        </template>

        <!-- 新患者模式 -->
        <template v-else>
          <div class="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
            急诊直录患者，就诊后可到患者管理中补全信息
          </div>
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="患者姓名" required>
              <el-input v-model="addForm.patientName" placeholder="请输入姓名"/>
            </el-form-item>
            <el-form-item label="性别" required>
              <el-radio-group v-model="addForm.gender">
                <el-radio v-for="g in PATIENT_GENDER_OPTIONS" :key="g.value" :value="g.value">{{ g.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="年龄" required>
              <el-input v-model.number="addForm.age" placeholder="请输入年龄" type="number"/>
            </el-form-item>
            <el-form-item label="联系电话">
              <el-input v-model="addForm.phone" placeholder="选填（填了须为 11 位手机号）"/>
            </el-form-item>
          </div>
          <el-form-item label="身份证号">
            <el-input v-model="addForm.idCard" placeholder="选填，用于患者身份核验"/>
          </el-form-item>
        </template>

        <!-- 共享字段 -->
        <el-form-item label="主诉" required>
          <!-- 主诉一改就作废旧建议：否则可能采纳一条针对别的主诉算出来的级别 -->
          <el-input v-model="addForm.chiefComplaint" :rows="2" placeholder="请输入主诉" type="textarea"
                    @input="triageSuggestion = null"/>
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="分诊级别" required>
            <el-select v-model="addForm.triageLevel" class="w-full">
              <el-option v-for="(v, k) in triageLevelMap" :key="k" :label="v.label" :value="Number(k)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="接诊科室">
            <el-select v-model="addForm.deptId" class="w-full" clearable filterable placeholder="选择科室"
                       @change="handleDeptChange">
              <el-option v-for="d in departments" :key="d.id" :label="d.deptName" :value="d.id"/>
            </el-select>
          </el-form-item>
        </div>

        <!-- AI 分诊建议 -->
        <div class="mb-4 rounded-lg border border-slate-200 bg-slate-50/60 p-3">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-slate-700">AI 分诊建议</p>
              <p class="mt-0.5 text-xs text-slate-400">
                填写生命体征后由系统判定红旗征象；<b>只做建议，不会自动改分诊级别</b>
              </p>
            </div>
            <el-button
                :loading="triageSuggesting"
                plain
                size="small"
                type="primary"
                @click="runTriageSuggest"
            >
              获取建议
            </el-button>
          </div>

          <div class="mt-3 grid grid-cols-3 gap-2">
            <el-input-number v-model="vitalForm.temperature" :max="45" :min="30" :precision="1" :step="0.1"
                             class="!w-full" controls-position="right" placeholder="体温℃" size="small"/>
            <el-input-number v-model="vitalForm.pulse" :max="300" :min="0" class="!w-full" controls-position="right"
                             placeholder="脉搏" size="small"/>
            <el-input-number v-model="vitalForm.respiratory" :max="100" :min="0" class="!w-full"
                             controls-position="right" placeholder="呼吸" size="small"/>
            <el-input-number v-model="vitalForm.systolic" :max="300" :min="0" class="!w-full" controls-position="right"
                             placeholder="收缩压" size="small"/>
            <el-input-number v-model="vitalForm.diastolic" :max="200" :min="0" class="!w-full"
                             controls-position="right" placeholder="舒张压" size="small"/>
            <el-input-number v-model="vitalForm.spo2" :max="100" :min="0" class="!w-full" controls-position="right"
                             placeholder="血氧%" size="small"/>
          </div>
          <p class="mt-1 text-[11px] text-slate-400">
            体温 / 脉搏 / 呼吸 / 收缩压 / 舒张压 / 血氧饱和度。不填也能获取建议，但命中红旗征象的可能性会下降。
          </p>

          <div v-if="triageSuggestion" class="mt-3 space-y-2">
            <el-alert
                v-if="triageSuggestion.degraded"
                :closable="false"
                show-icon
                title="本次未经过大模型，建议来自确定性规则"
                type="warning"
            >
              <template #default>
                <span class="text-xs">{{ triageSuggestion.degradeReason }}</span>
              </template>
            </el-alert>

            <div v-if="triageSuggestion.redFlags?.length" class="rounded border border-red-200 bg-red-50 p-2">
              <p class="mb-1 text-xs font-medium text-red-700">命中红旗征象</p>
              <ul class="list-disc pl-4 text-xs text-red-700">
                <li v-for="(f, i) in triageSuggestion.redFlags" :key="i">{{ f }}</li>
              </ul>
            </div>

            <div class="flex flex-wrap items-center gap-2 text-xs">
              <el-tag :type="levelBasisTag(triageSuggestion.levelBasis)" effect="plain" size="small">
                依据：{{ levelBasisText(triageSuggestion.levelBasis) }}
              </el-tag>
              <span class="text-slate-500">系统建议</span>
              <span
                  :class="['rounded border px-2 py-0.5 font-medium',
                         triageLevelMap[triageSuggestion.suggestedLevel]?.color || '']">
                {{ triageSuggestion.suggestedLevelText }}
              </span>
              <span v-if="triageSuggestion.suggestedZone" class="text-slate-500">
                {{ triageSuggestion.suggestedZone }}
              </span>
              <el-tag v-if="triageSuggestion.suggestedGreenChannel" size="small" type="success">
                {{ triageSuggestion.suggestedGreenChannel }}
              </el-tag>
              <span class="text-slate-500">
                当前已选 {{ triageLevelMap[addForm.triageLevel]?.label }}
              </span>
            </div>

            <div v-if="triageSuggestion.recommendActions?.length" class="rounded bg-white p-2">
              <p class="mb-1 text-xs font-medium text-slate-600">建议措施</p>
              <ul class="list-disc pl-4 text-xs text-slate-600">
                <li v-for="(a, i) in triageSuggestion.recommendActions" :key="i">{{ a }}</li>
              </ul>
            </div>

            <p v-if="triageSuggestion.reasoning" class="text-xs text-slate-500">
              {{ triageSuggestion.reasoning }}
            </p>

            <div class="flex items-center gap-2">
              <el-button v-if="canAdoptSuggestion" size="small" type="danger" @click="adoptSuggestion">
                采纳建议，提升为 {{ triageSuggestion.suggestedLevelText }}
              </el-button>
              <span v-else class="text-xs text-slate-500">
                系统建议不会降低人工分级，维持当前所选级别即可
              </span>
            </div>
          </div>
        </div>
        <el-form-item label="接诊医生">
          <el-select v-model="addForm.doctorId" :disabled="!addForm.deptId" class="w-full"
                     clearable filterable
                     no-data-text="该科室此刻无在岗值班医生" placeholder="选择此刻在岗的值班医生（留空＝交给系统按排班派单）">
            <el-option v-for="doc in employeeList" :key="doc.id" :label="doc.empName"
                       :value="doc.id">
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium">{{ doc.empName }}</span>
                <span v-if="doc.startTime" class="text-xs text-slate-400">{{ doc.startTime }}-{{ doc.endTime }}</span>
              </div>
            </el-option>
          </el-select>
          <div v-if="!addForm.deptId" class="mt-1 text-xs text-slate-400">
            先选接诊科室；不指定医生时，登记会按该科室<b>当前在岗排班</b>自动派单
          </div>
          <div v-else-if="employeeList.length === 0" class="mt-1 text-xs text-amber-600">
            该科室此刻无在岗值班医生，本例会进入<b>待派单池</b>（可被超时催办），请说明原因
          </div>
        </el-form-item>
        <el-form-item v-if="addForm.deptId && !addForm.doctorId && employeeList.length === 0"
                      data-testid="emg-unassigned-reason" label="未派单原因">
          <el-input v-model="addForm.unassignedReason" maxlength="200" placeholder="如：值班医生出诊在外 / 夜班仅一名医生在抢救，留空则系统记「当日该科室无在岗排班医生」"
                    show-word-limit/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button v-perm="'opd:emergency:add'" type="danger" @click="handleAddSubmit">确认登记</el-button>
      </template>
    </el-dialog>

    <!-- 转入留观：必须选一张急诊病区的空闲床（占 sys_bed，住院分床不会再发这张床） -->
    <el-dialog v-model="obsVisible" destroy-on-close title="转入留观" width="460px">
      <el-form :model="obsForm" label-position="top">
        <el-form-item label="患者">
          <el-input :model-value="obsForm.patientName" disabled/>
        </el-form-item>
        <el-form-item label="留观病区" required>
          <el-select v-model="obsForm.wardId" class="!w-full" placeholder="选择急诊病区" @change="onObsWardChange">
            <el-option v-for="w in emergencyWards" :key="w.wardId" :label="w.wardName" :value="String(w.wardId)">
              <span>{{ w.wardName }}</span>
              <span :class="w.freeBeds > 0 ? 'text-emerald-500' : 'text-rose-500'" class="float-right text-xs">
                空闲 {{ w.freeBeds }}
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="留观床位" required>
          <el-select v-model="obsForm.bedId" :disabled="!obsForm.wardId" class="!w-full" placeholder="先选病区">
            <el-option v-for="b in obsBeds" :key="b.bedId" :label="`${b.bedNo}（${b.bedType}）`"
                       :value="String(b.bedId)"/>
          </el-select>
          <p v-if="obsForm.wardId && !obsBeds.length" class="mt-1 text-xs text-rose-500">该病区暂无空闲床位</p>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="obsVisible = false">取消</el-button>
        <el-button :loading="obsSubmitting" type="warning" @click="submitObs">确认占床并转留观</el-button>
      </template>
    </el-dialog>

    <!-- 转住院：一次动作完成入院登记（途径=急诊）+ 急诊终态，不再"只翻状态不落入院" -->
    <el-dialog v-model="admitVisible" destroy-on-close title="急诊转住院（入院登记）" width="560px">
      <el-form :model="admitForm" label-position="top">
        <div class="grid grid-cols-2 gap-x-4">
          <el-form-item label="患者">
            <el-input :model-value="admitForm.patientName" disabled/>
          </el-form-item>
          <el-form-item label="入院途径">
            <el-input disabled model-value="急诊"/>
          </el-form-item>
          <el-form-item label="入院病区" required>
            <el-select v-model="admitForm.wardId" class="!w-full" filterable placeholder="选择病区"
                       @change="onAdmitWardChange">
              <el-option v-for="w in wards" :key="w.wardId" :label="w.wardName" :value="String(w.wardId)">
                <span>{{ w.wardName }}</span>
                <span :class="w.freeBeds > 0 ? 'text-emerald-500' : 'text-rose-500'" class="float-right text-xs">
                  空闲 {{ w.freeBeds }}
                </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="入院床位" required>
            <el-select v-model="admitForm.bedId" :disabled="!admitForm.wardId" class="!w-full" placeholder="先选病区">
              <el-option v-for="b in admitBeds" :key="b.bedId" :label="b.bedNo" :value="String(b.bedId)"/>
            </el-select>
            <p v-if="admitForm.wardId && !admitBeds.length" class="mt-1 text-xs text-rose-500">该病区暂无空闲床位</p>
          </el-form-item>
          <el-form-item label="入院医生" required>
            <el-select v-model="admitForm.admitDoctorId" class="!w-full" filterable placeholder="选择医生">
              <el-option v-for="e in employees" :key="e.id" :label="e.empName" :value="e.id"/>
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="入院诊断">
          <el-input v-model="admitForm.diagnosis" placeholder="默认取急诊初步诊断，可修改"/>
        </el-form-item>
        <p class="text-xs text-slate-400">
          确认后同步办理：生成入院记录（占住院床位）、释放留观床（若在观）、急诊号挂号单与叫号队列置为已就诊。
        </p>
      </el-form>
      <template #footer>
        <el-button @click="admitVisible = false">取消</el-button>
        <el-button :loading="admitSubmitting" type="success" @click="submitAdmit">确认入院登记</el-button>
      </template>
    </el-dialog>
    <!-- 交班清零：清单由后端按「本科室未闭环 ∧（无人认领 ∨ 挂我名下）」出题，
         逐条点名写去向才能提交 —— 漏一条就等于把这个人留在上一个班的名单里没人管。 -->
    <el-dialog v-model="hoVisible" destroy-on-close title="急诊交班清零" width="1080px">
      <div class="mb-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs leading-relaxed text-amber-700">
        交班 = 把「挂在我名下」和「还没有人负责」的未闭环患者<strong>逐条</strong>移交并写下去向交代。
        只要有一条没点名，本次交班不能提交。移交后责任医生立即变更（叫号队列与挂号单一并改到接班人名下），
        并给接续医生发一条接收待办；台账只增不改，做错了只能重新交一次。
      </div>

      <div class="mb-3 grid grid-cols-2 gap-4">
        <el-form :model="hoForm" label-position="top">
          <el-form-item label="接班人（总接班，逐条可再指定别的接续医生）" required>
            <el-select v-model="hoForm.takeEmpId" class="!w-full" data-testid="emg-ho-taker"
                       filterable placeholder="选择接班医生">
              <el-option v-for="t in takers" :key="t.empId" :label="t.empName" :value="t.empId">
                <span>{{ t.empName }}</span>
                <span class="float-right text-xs text-slate-400">{{ t.deptName }} · {{ t.sourceText }}</span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <el-form :model="hoForm" label-position="top">
          <el-form-item label="整班备注（可选）">
            <el-input v-model="hoForm.remark" placeholder="如：夜班 1 例在抢救、设备科已通知"/>
          </el-form-item>
        </el-form>
      </div>

      <el-table v-loading="hoLoading" :data="hoPending" data-testid="emg-ho-table" max-height="300"
                style="width: 100%">
        <el-table-column label="患者" width="120">
          <template #default="{ row }">
            <div class="text-sm font-medium text-slate-900">{{ row.patientName }}</div>
            <div class="font-mono text-xs text-slate-400">{{ row.emergencyNo }}</div>
          </template>
        </el-table-column>
        <el-table-column label="分诊/状态" width="110">
          <template #default="{ row }">
            <div class="text-xs text-slate-700">{{ row.triageLevelText }}</div>
            <div class="text-xs text-slate-400">{{ row.emergencyStatusText }}</div>
          </template>
        </el-table-column>
        <el-table-column label="现负责" width="90">
          <template #default="{ row }">
            <span v-if="row.doctorName" class="text-xs text-slate-700">{{ row.doctorName }}</span>
            <el-tooltip v-else :content="row.unassignedReason || '尚未派单'" placement="top">
              <span
                  class="inline-block rounded border border-amber-300 bg-amber-50 px-1.5 py-0.5 text-xs font-medium text-amber-700">
                待派单
              </span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column label="计时" width="90">
          <template #default="{ row }">
            <div
                :class="['text-xs', row.overdueLevel > 0 || row.obsLevel > 0 ? 'font-bold text-red-600' : 'text-slate-600']">
              {{ hoTimeText(row) }}
            </div>
            <div v-if="row.overdueText || row.obsLevelText" class="text-xs font-medium text-red-500">
              {{ row.obsLevelText || row.overdueText }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="主诉/诊断" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="text-xs text-slate-700">{{ row.chiefComplaint || '—' }}</div>
            <div class="text-xs text-slate-400">{{ row.diagnosis || '' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="接续医生" width="130">
          <template #default="{ row }">
            <el-select v-model="hoRows[String(row.emergencyId)].takeDoctorId" class="!w-full"
                       clearable filterable placeholder="默认接班人">
              <el-option v-for="t in takers" :key="t.empId" :label="t.empName" :value="t.empId"/>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="去向交代" min-width="180">
          <template #default="{ row }">
            <el-select v-model="hoRows[String(row.emergencyId)].disposition" allow-create
                       class="!w-full" default-first-option filterable placeholder="必填：下一步做什么">
              <el-option v-for="o in dispositionOptions" :key="o" :label="o" :value="o"/>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="补充交代" min-width="150">
          <template #default="{ row }">
            <el-input v-model="hoRows[String(row.emergencyId)].handoverNote" class="!w-full"
                      placeholder="过敏史/家属在等…"/>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <span class="mr-4 text-xs text-slate-500" data-testid="emg-ho-count">
          共 {{ hoPending.length }} 条（其中待派单 {{ hoPoolCount }} 条）<template v-if="hoMissing > 0">
            · <span class="font-medium text-red-600">未点名 {{ hoMissing }} 条</span></template>
        </span>
        <el-button @click="hoVisible = false">取消</el-button>
        <el-button :disabled="hoPending.length === 0 || hoMissing > 0" :loading="hoSubmitting" data-testid="emg-ho-submit"
                   type="warning" @click="submitHandover">
          确认交班
        </el-button>
      </template>
    </el-dialog>

    <!-- 交班台账：每班一单的定格事实，只读凭证 -->
    <el-dialog v-model="hoLedgerVisible" destroy-on-close title="交班台账" width="1080px">
      <div class="mb-3 flex items-center gap-3">
        <el-input v-model="hoLedgerKeyword" class="!w-64" clearable placeholder="交班单号 / 交班人 / 接班人"
                  @keyup.enter="onHoLedgerSearch"/>
        <el-button :icon="Search" type="primary" @click="onHoLedgerSearch">查询</el-button>
        <div class="flex-1"></div>
        <span class="text-xs text-slate-400">点一行看该班逐条移交凭证</span>
      </div>
      <el-table v-loading="hoLedgerLoading" :data="hoLedgerList" data-testid="emg-ho-ledger" max-height="380"
                style="width: 100%" @row-click="openHoDetail">
        <el-table-column class-name="font-mono text-sm" label="交班单号" width="150">
          <template #default="{ row }">
            <span class="font-mono text-xs font-medium text-slate-700">{{ row.handoverNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="科室" width="110">
          <template #default="{ row }">
            <div class="text-xs text-slate-700">{{ row.deptName }}</div>
            <div v-if="row.shiftName" class="text-xs text-slate-400">{{ row.shiftName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="交班 → 接班" width="140">
          <template #default="{ row }">
            <div class="text-xs text-slate-700">{{ row.fromEmpName }} → {{ row.takeEmpName }}</div>
          </template>
        </el-table-column>
        <el-table-column label="本班时段" width="170">
          <template #default="{ row }">
            <span class="text-xs text-slate-600">{{ row.periodBegin }} ~ {{ row.periodEnd }}</span>
          </template>
        </el-table-column>
        <el-table-column align="center" label="移交" width="90">
          <template #default="{ row }">
            <span class="text-sm font-bold text-slate-800">{{ row.pendingCount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="其中" min-width="200">
          <template #default="{ row }">
            <span class="text-xs text-slate-500">
              待派单 {{ row.poolCount }} · 超时 {{ row.overdueCount }} · 在观 {{ row.observationCount }}
            </span>
            <span v-if="row.obsOverLimitCount > 0" class="ml-1 text-xs font-bold text-red-600">
              超上限 {{ row.obsOverLimitCount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="备注" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="text-xs text-slate-500">{{ row.remark || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column fixed="right" label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="openHoDetail(row)">明细</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="mt-4 flex justify-end">
        <el-pagination
            v-model:current-page="hoLedgerPage.pageNum"
            v-model:page-size="hoLedgerPage.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="hoLedgerPage.total"
            layout="total, sizes, prev, pager, next"
            @size-change="loadHoLedger"
            @current-change="loadHoLedger"
        />
      </div>
    </el-dialog>

    <!-- 交班单明细：只读凭证，没有确定键 -->
    <el-dialog v-model="hoDetailVisible" destroy-on-close title="交班单明细" width="1000px">
      <div v-loading="hoDetailLoading" class="space-y-4">
        <template v-if="hoDetail?.handover">
          <div class="grid grid-cols-4 gap-4 rounded-lg border border-slate-200 bg-slate-50 p-3 text-xs">
            <div>
              <p class="text-slate-400">交班单号</p>
              <p class="font-mono font-medium text-slate-800">{{ hoDetail.handover.handoverNo }}</p>
            </div>
            <div>
              <p class="text-slate-400">交班 → 接班</p>
              <p class="font-medium text-slate-800">{{ hoDetail.handover.fromEmpName }} →
                {{ hoDetail.handover.takeEmpName }}</p>
            </div>
            <div>
              <p class="text-slate-400">科室 / 班次</p>
              <p class="font-medium text-slate-800">{{ hoDetail.handover.deptName }}
                {{ hoDetail.handover.shiftName || '—' }}</p>
            </div>
            <div>
              <p class="text-slate-400">本班时段</p>
              <p class="font-medium text-slate-800">{{ hoDetail.handover.periodBegin }} ~ {{
                  hoDetail.handover.periodEnd
                }}</p>
            </div>
            <div v-if="hoDetail.handover.remark" class="col-span-4">
              <p class="text-slate-400">整班备注</p>
              <p class="text-slate-700">{{ hoDetail.handover.remark }}</p>
            </div>
          </div>
          <el-table :data="hoDetail.items" max-height="360" style="width: 100%">
            <el-table-column label="患者" width="120">
              <template #default="{ row }">
                <div class="text-sm font-medium text-slate-900">{{ row.patientName }}</div>
                <div class="font-mono text-xs text-slate-400">{{ row.emergencyNo }}</div>
              </template>
            </el-table-column>
            <el-table-column label="分诊/状态" width="110">
              <template #default="{ row }">
                <div class="text-xs text-slate-700">{{ row.triageLevelText }}</div>
                <div class="text-xs text-slate-400">{{ row.emergencyStatusText }}</div>
              </template>
            </el-table-column>
            <el-table-column label="原负责 → 接续" width="150">
              <template #default="{ row }">
                <span class="text-xs text-slate-600">{{ row.fromDoctorName || '无人负责' }}</span>
                <span class="mx-1 text-xs text-slate-400">→</span>
                <span class="text-xs font-medium text-slate-800">{{ row.takeDoctorName }}</span>
              </template>
            </el-table-column>
            <el-table-column label="计时" width="90">
              <template #default="{ row }">
                <span class="text-xs text-slate-600">{{ row.waitMinutes != null ? row.waitMinutes + '分' : '' }}</span>
                <span class="text-xs text-slate-600">{{ row.obsHours != null ? row.obsHours + '小时' : '' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="去向交代" min-width="180">
              <template #default="{ row }">
                <span class="text-xs text-slate-700">{{ row.disposition }}</span>
              </template>
            </el-table-column>
            <el-table-column label="补充交代" min-width="150">
              <template #default="{ row }">
                <span class="text-xs text-slate-500">{{ row.handoverNote || '—' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {AlarmClock, Bell, Check, Clock, Plus, Refresh, Search, Switch, User, Warning} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  admitEmergency,
  getEmergencyBedSelectList,
  getEmergencyDutySelectList,
  getEmergencyHandoverDetail,
  getEmergencyHandoverListPage,
  getEmergencyHandoverPendingList,
  getEmergencyHandoverTakeList,
  getEmergencyList,
  getEmergencyStats,
  getEmergencyWardSelectList,
  registerEmergency,
  saveEmergencyHandover,
  updateEmergencyStatus
} from '@/api/emergency';
import {getDepartmentSelectList, getEmployeeList} from '@/api/system';
import {suggestEmergencyTriage} from '@/api/ai';
import PatientSelect from '@/components/his/PatientSelect.vue';
// 今日总值班：本科室阶梯走完后，超时催办落到他身上（sql/169）
import DutyOfficerBar from '@/components/his/DutyOfficerBar.vue';
import {
  isIdCardFormatLegal,
  isPatientGenderCollected,
  isPhoneLegal,
  PATIENT_GENDER_OPTIONS,
  patientAvatarTone,
  patientGenderSymbol
} from '@/lib/patientGender';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';

const GENDER_DOT = {male: 'bg-blue-500', female: 'bg-pink-500', unknown: 'bg-slate-400'};

const loading = ref(false);
const emergencyList = ref([]);
const stats = ref({});
const departments = ref([]);
const employeeList = ref([]);
const searchForm = ref({
  keyword: '',
  triageLevel: null,
  emergencyStatus: null,
  // 两个「只看」是兜底看板的入口：待派单池 = 没人负责的，超时 = 该被追问的
  unassignedOnly: false,
  overdueOnly: false,
  // 留观榜：null=不看，0=全部在观，其余为统计卡带出的阈值（小时数由后端配置决定，前端不写死）
  obsBoardHours: null,
});
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
});
// 新增急诊弹窗
const showAddDialog = ref(false);
const addMode = ref('existing'); // 选择已有患者 / 录入新患者
const addForm = ref({
  patientId: null,
  patientName: '',
  patientNo: '',
  // 不预设性别：默认成"男"等于静默编造性别（急诊也有性别专属判断）
  gender: null,
  age: 0,
  phone: '',
  idCard: '',
  chiefComplaint: '',
  triageLevel: 3,
  deptId: null,
  doctorId: null,
  // 只在「当前无在岗医生」时出现输入框；留空时后端兜底成「当日该科室无在岗排班医生」
  unassignedReason: '',
});
const triageLevelMap = {
  1: {label: 'I级 濒危', color: 'bg-red-100 text-red-700 border-red-300', zone: '红区'},
  2: {label: 'II级 危重', color: 'bg-orange-100 text-orange-700 border-orange-300', zone: '红区'},
  3: {label: 'III级 急症', color: 'bg-amber-100 text-amber-700 border-amber-300', zone: '黄区'},
  4: {label: 'IV级 非急症', color: 'bg-emerald-100 text-emerald-700 border-emerald-300', zone: '绿区'},
};
// ==================== AI 分诊建议 ====================
//
// 分工：SpO2<90、收缩压<90、GCS≤8 这类红旗征象由后端确定性规则判定（HARD_RULE），
// 模型只在没有红旗征象时补充严重程度的判断。
//
// 「只升不降」在这一步必须由前端守住：登记时患者还没建档，
// 后端不知道该分诊台当前选的是几级，所以采纳建议时只在
// 「建议级别比当前选的更严重」时才允许改（数字更小 = 更严重）。
const vitalForm = ref({
  temperature: null,
  pulse: null,
  respiratory: null,
  systolic: null,
  diastolic: null,
  spo2: null,
});
const triageSuggestion = ref(null);
const triageSuggesting = ref(false);
const resetVitalForm = () => {
  vitalForm.value = {
    temperature: null, pulse: null, respiratory: null,
    systolic: null, diastolic: null, spo2: null,
  };
  triageSuggestion.value = null;
};
/** 只把填了值的项放进 JSON，空值不能当成 0（0 是一个结论） */
const buildVitalSigns = () => {
  const src = vitalForm.value;
  const out = {};
  const keys = ['temperature', 'pulse', 'respiratory', 'systolic', 'diastolic', 'spo2'];
  for (const k of keys) {
    const v = src[k];
    if (v !== null && v !== undefined && !Number.isNaN(Number(v))) {
      out[k] = Number(v);
    }
  }
  return Object.keys(out).length > 0 ? JSON.stringify(out) : undefined;
};
const runTriageSuggest = async () => {
  if (!addForm.value.chiefComplaint?.trim()) {
    ElMessage.warning('请先填写主诉');
    return;
  }
  triageSuggesting.value = true;
  try {
    const res = await suggestEmergencyTriage({
      chiefComplaint: addForm.value.chiefComplaint,
      vitalSigns: buildVitalSigns(),
      gender: addForm.value.gender || undefined,
      age: addForm.value.age || undefined,
    });
    triageSuggestion.value = res.data;
  } catch (error) {
    ElMessage.error(error.message || '分诊建议获取失败');
  } finally {
    triageSuggesting.value = false;
  }
};
/** 建议比当前选的更严重才给「采纳」入口，绝不提供降级操作 */
const canAdoptSuggestion = computed(() => {
  const s = triageSuggestion.value;
  if (!s?.suggestedLevel)
    return false;
  return s.suggestedLevel < addForm.value.triageLevel;
});
const adoptSuggestion = () => {
  const s = triageSuggestion.value;
  if (!s?.suggestedLevel)
    return;
  if (s.suggestedLevel >= addForm.value.triageLevel) {
    ElMessage.warning('系统建议不会降低人工分级，请维持当前级别');
    return;
  }
  addForm.value.triageLevel = s.suggestedLevel;
  ElMessage.success(`已采纳建议：${s.suggestedLevelText}`);
};
const levelBasisText = (basis) => {
  if (basis === 'HARD_RULE')
    return '确定性红旗征象（硬规则）';
  if (basis === 'MANUAL')
    return '维持人工分级';
  if (basis === 'MODEL')
    return '大模型判断';
  return basis || '—';
};
const levelBasisTag = (basis) => {
  if (basis === 'HARD_RULE')
    return 'danger';
  if (basis === 'MODEL')
    return 'warning';
  return 'info';
};
const statusMap = {
  1: {label: '候诊', color: 'bg-blue-100 text-blue-700'},
  2: {label: '诊治中', color: 'bg-purple-100 text-purple-700'},
  3: {label: '留观', color: 'bg-amber-100 text-amber-700'},
  4: {label: '转住院', color: 'bg-indigo-100 text-indigo-700'},
  5: {label: '离院', color: 'bg-slate-100 text-slate-600'},
  6: {label: '死亡', color: 'bg-red-100 text-red-700'},
};
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    };
    if (searchForm.value.keyword)
      params.keyword = searchForm.value.keyword;
    if (searchForm.value.triageLevel != null)
      params.triageLevel = searchForm.value.triageLevel;
    if (searchForm.value.emergencyStatus != null)
      params.emergencyStatus = searchForm.value.emergencyStatus;
    // 「只看」类布尔：只传 true，false/未选一律不出现 key（同 keyword 的写法）
    if (searchForm.value.unassignedOnly)
      params.unassignedOnly = true;
    if (searchForm.value.overdueOnly)
      params.overdueOnly = true;
    // 0 是有效值（=全部在观），所以只判空，不能用真值判断
    if (searchForm.value.obsBoardHours != null)
      params.observationMinHours = searchForm.value.obsBoardHours;
    const res = await getEmergencyList(params);
    emergencyList.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载急诊数据失败:', error);
  } finally {
    loading.value = false;
  }
};
const loadStats = async () => {
  try {
    const res = await getEmergencyStats();
    stats.value = res.data || {};
  } catch (error) {
    console.error('加载统计失败:', error);
  }
};
const loadDepartments = async () => {
  try {
    // 不传 scope → 默认按当前人过滤
    const res = await getDepartmentSelectList({deptType: 1});
    departments.value = res.data || [];
  } catch (error) {
    console.error('加载科室失败:', error);
  }
};
const loadDoctors = async (deptId) => {
  try {
    // 用「此刻在岗的值班医生」而不是门诊可挂号排班：急诊值班表号源恒为 0（不该被前台挂上门诊号），
    // 走 availableList 永远是空 → 页面写死"无排班医生"，而后端派单却查得到人，两边各说一套。
    const res = await getEmergencyDutySelectList(deptId);
    employeeList.value = (res.data || []).map((d) => ({
      id: d.doctorId,
      empName: d.doctorName,
      startTime: d.startTime,
      endTime: d.endTime,
    }));
  } catch (error) {
    employeeList.value = [];
  }
};
const handleDeptChange = (deptId) => {
  addForm.value.doctorId = null;
  if (deptId)
    loadDoctors(deptId);
  else
    employeeList.value = [];
};
const handlePatientSelect = (patient) => {
  if (!patient)
    return;
  addForm.value.patientId = patient.id;
  addForm.value.patientName = patient.patientName;
  addForm.value.patientNo = patient.patientNo;
  addForm.value.gender = patient.gender;
  addForm.value.age = patient.age || 0;
  addForm.value.phone = patient.phone || '';
};
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  searchForm.value = {
    keyword: '', triageLevel: null, emergencyStatus: null,
    unassignedOnly: false, overdueOnly: false, obsBoardHours: null,
  };
  handleSearch();
};
/** 超时看的是「还在等的」，留观榜看的是「已经躺着的」，两个口径互斥（后端也会直接拒绝），所以开一个就关另一个 */
const onOverdueOnlyChange = () => {
  if (searchForm.value.overdueOnly)
    searchForm.value.obsBoardHours = null;
  handleSearch();
};
const onObsBoardChange = () => {
  // el-select clearable 清空后给的是 ''（不是 null），不归一就会把 observationMinHours='' 传给后端
  const v = searchForm.value.obsBoardHours;
  searchForm.value.obsBoardHours = (v === '' || v === undefined ? null : v);
  if (searchForm.value.obsBoardHours != null)
    searchForm.value.overdueOnly = false;
  handleSearch();
};
/** 点统计卡直接上看板：卡片数字与列表必须同一口径，所以小时数取自接口返回的阈值而不是前端写死 */
const pickObsBoard = (hours) => {
  if (hours == null)
    return;
  searchForm.value.obsBoardHours = searchForm.value.obsBoardHours === hours ? null : hours;
  if (searchForm.value.obsBoardHours != null)
    searchForm.value.overdueOnly = false;
  handleSearch();
};
/** 档位标签把小时数写出来：只说「超时限」会让人猜是哪一档 */
const obsBoardOptions = computed(() => {
  const warn = stats.value.obsWarnHours;
  const max = stats.value.obsMaxHours;
  const out = [{value: 0, label: '全部在观'}];
  if (warn != null)
    out.push({value: warn, label: `留观 ≥ ${warn} 小时`});
  if (max != null && max !== warn)
    out.push({value: max, label: `留观 ≥ ${max} 小时（上限）`});
  return out;
});
/** 清单里"计时"一列两口径：留观看已占床小时数，候诊/诊治看等候分钟（后端 NOW() 现算） */
const hoTimeText = (row) => {
  if (row.emergencyStatus === 3)
    return row.obsHours == null ? '-' : `${row.obsHours}小时`;
  if (row.waitMinutes == null)
    return '-';
  const m = Number(row.waitMinutes);
  if (m < 60)
    return `${m}分钟`;
  return `${Math.floor(m / 60)}小时${m % 60}分钟`;
};
const handleSizeChange = (val) => {
  pagination.value.pageSize = val;
  pagination.value.pageNum = 1;
  loadData();
};
const handleCurrentChange = (val) => {
  pagination.value.pageNum = val;
  loadData();
};
// 新增急诊
const handleAdd = () => {
  addForm.value = {
    patientId: null, patientName: '', patientNo: '', gender: null, age: 0,
    phone: '', idCard: '', chiefComplaint: '', triageLevel: 3, deptId: null, doctorId: null,
    unassignedReason: '',
  };
  addMode.value = 'existing';
  employeeList.value = [];
  resetVitalForm();
  showAddDialog.value = true;
};
const handleAddSubmit = async () => {
  // 校验患者信息
  if (addMode.value === 'existing') {
    if (!addForm.value.patientId) {
      ElMessage.warning('请选择患者');
      return;
    }
  } else {
    if (!addForm.value.patientName) {
      ElMessage.warning('请填写患者姓名');
      return;
    }
    // 性别必选（含「未知」）：急诊同样有性别专属判断（妊娠、前列腺等）
    if (!isPatientGenderCollected(addForm.value.gender)) {
      ElMessage.warning('请选择性别（确实没问到请选「未知」）');
      return;
    }
    // 三无患者允许没有手机号/身份证；但填了就必须合法
    if (addForm.value.phone && !isPhoneLegal(addForm.value.phone)) {
      ElMessage.warning('手机号格式不正确：应为 11 位手机号');
      return;
    }
    if (addForm.value.idCard && !isIdCardFormatLegal(addForm.value.idCard)) {
      ElMessage.warning('身份证号格式不正确：应为 18 位（末位可为 X）');
      return;
    }
  }
  if (!addForm.value.chiefComplaint) {
    ElMessage.warning('请填写主诉');
    return;
  }
  try {
    const dept = departments.value.find((d) => d.id === addForm.value.deptId);
    const doc = employeeList.value.find((e) => e.id === addForm.value.doctorId);
    const data = {...addForm.value};
    if (!data)
      return;
    // 新患者模式下清空patientId，让后端知道这是急诊直录
    if (addMode.value === 'new') {
      data.patientId = null;
      data.patientNo = '';
    }
    if (dept)
      data.deptName = dept.deptName;
    if (doc)
      data.doctorName = doc.empName;
    await registerEmergency(data);
    // 派单结果要说清楚：登记成功不等于有人负责，用户必须知道自己刚才是不是把患者扔进了池子
    ElMessage.success(addForm.value.doctorId
        ? '急诊登记成功，已按所选医生派单'
        : (employeeList.value.length
            ? '急诊登记成功，已按当前在岗排班自动派单'
            : '急诊登记成功，该科室此刻无在岗医生，已进待派单池'));
    showAddDialog.value = false;
    loadData();
    loadStats();
  } catch (error) {
    ElMessage.error(error.message || '登记失败');
  }
};
// 状态变更（接诊/离院/死亡这类无需补料的流转）
const handleStatusChange = async (row, newStatus) => {
  const label = statusMap[newStatus]?.label || '未知';
  try {
    await ElMessageBox.confirm(`确认将 ${row.patientName} 状态变更为「${label}」？`, '状态变更', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    });
    await updateEmergencyStatus(row.id, newStatus);
    ElMessage.success('状态更新成功');
    loadData();
    loadStats();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '操作失败');
    }
  }
};
// ==================== 留观占床 ====================
// 留观必须落到一张真实的 sys_bed 床位上：不占床的话「这张床有没有人躺着」
// 就只存在于急诊表一处，住院分床会把同一张床再发出去。
const obsVisible = ref(false);
const obsSubmitting = ref(false);
const obsBeds = ref([]);
const obsForm = ref({
  id: '',
  patientName: '',
  wardId: null,
  bedId: null,
});
/** 留观只在急诊自己的病区（急诊内科/外科/儿科病区），不是全院住院病区 */
const emergencyWards = computed(() => wards.value.filter((w) => (w.wardName || '').startsWith('急诊')));
const openObs = async (row) => {
  obsForm.value = {id: row.id, patientName: row.patientName, wardId: null, bedId: null};
  obsBeds.value = [];
  if (wards.value.length === 0)
    await loadWards();
  obsVisible.value = true;
};
const onObsWardChange = async (wardId) => {
  obsForm.value.bedId = null;
  obsBeds.value = [];
  if (!wardId)
    return;
  try {
    const res = await getEmergencyBedSelectList({wardId, bedStatus: 1});
    obsBeds.value = res.data || [];
  } catch {
    obsBeds.value = [];
  }
};
const submitObs = async () => {
  if (!obsForm.value.wardId)
    return ElMessage.warning('请选择留观病区');
  if (!obsForm.value.bedId)
    return ElMessage.warning('请选择留观床位');
  obsSubmitting.value = true;
  try {
    await updateEmergencyStatus(obsForm.value.id, 3, {
      observationWardId: obsForm.value.wardId,
      observationBedId: obsForm.value.bedId,
    });
    ElMessage.success('已转入留观并占床');
    obsVisible.value = false;
    await Promise.all([loadData(), loadStats()]);
  } catch (error) {
    ElMessage.error(error.message || '转留观失败');
  } finally {
    obsSubmitting.value = false;
  }
};
// ==================== 转住院（真实入院登记，途径=急诊） ====================
const admitVisible = ref(false);
const admitSubmitting = ref(false);
const wards = ref([]);
const admitBeds = ref([]);
const employees = ref([]);
const admitForm = ref({
  id: '',
  patientName: '',
  wardId: null,
  bedId: null,
  admitDoctorId: null,
  diagnosis: '',
});
const loadWards = async () => {
  try {
    const res = await getEmergencyWardSelectList();
    wards.value = res.data || [];
  } catch {
    wards.value = [];
  }
};
const loadEmployeesForAdmit = async () => {
  try {
    const res = await getEmployeeList({});
    employees.value = (res.data || []).map((e) => ({id: String(e.id), empName: e.empName}));
  } catch {
    employees.value = [];
  }
};
const openAdmit = async (row) => {
  admitForm.value = {
    id: row.id, patientName: row.patientName, wardId: null, bedId: null,
    admitDoctorId: row.doctorId || null, diagnosis: row.diagnosis || '',
  };
  admitBeds.value = [];
  if (wards.value.length === 0)
    await loadWards();
  if (employees.value.length === 0)
    await loadEmployeesForAdmit();
  admitVisible.value = true;
};
const onAdmitWardChange = async (wardId) => {
  admitForm.value.bedId = null;
  admitBeds.value = [];
  if (!wardId)
    return;
  try {
    const res = await getEmergencyBedSelectList({wardId, bedStatus: 1});
    admitBeds.value = res.data || [];
  } catch {
    admitBeds.value = [];
  }
};
const submitAdmit = async () => {
  if (!admitForm.value.wardId)
    return ElMessage.warning('请选择入院病区');
  if (!admitForm.value.bedId)
    return ElMessage.warning('请选择入院床位');
  if (!admitForm.value.admitDoctorId)
    return ElMessage.warning('请选择入院医生');
  admitSubmitting.value = true;
  try {
    const res = await admitEmergency({...admitForm.value});
    ElMessage.success(`转住院成功，入院登记已办理（入院ID ${res.data}）`);
    admitVisible.value = false;
    await Promise.all([loadData(), loadStats()]);
  } catch (error) {
    ElMessage.error(error.message || '转住院失败');
  } finally {
    admitSubmitting.value = false;
  }
};
const waitText = (row) => {
  // 时长与超时档位由后端算（服务端 NOW() 才是事实，浏览器时钟不算）
  if (row.waitMinutes == null)
    return '-';
  const minutes = Number(row.waitMinutes);
  if (minutes < 60)
    return `${minutes}分钟`;
  return `${Math.floor(minutes / 60)}小时${minutes % 60}分钟`;
};
/** 留观时长：从占床那一刻算起，离观后停表 */
const getObsTime = (row) => {
  if (!row.observationStartTime)
    return '-';
  const start = new Date(row.observationStartTime);
  const end = row.observationEndTime ? new Date(row.observationEndTime) : new Date();
  const minutes = Math.floor((end.getTime() - start.getTime()) / 60000);
  if (minutes < 60)
    return `${minutes}分钟`;
  return `${Math.floor(minutes / 60)}小时${minutes % 60}分钟`;
};
// ==================== 交班清零（sql/153） ====================
//
// 交班的定义是「我名下不再有未闭环的患者」，所以清单必须逐条点名：
// 后端按「本科室未闭环 ∧（无人指派 ∨ 挂我名下）」出题，少一条就整体拒绝。
// 前端不自己数"还剩几条"，清单永远来自接口，避免两边各算一套。
const hoVisible = ref(false);
const hoLoading = ref(false);
const hoSubmitting = ref(false);
const hoPending = ref([]);
const takers = ref([]);
// key = emergencyId（雪花 ID 一律当字符串用，Number() 会丢精度）
const hoRows = ref({});
const hoForm = ref({takeEmpId: null, remark: ''});
const dispositionOptions = ['继续留观，等待结果', '已收诊治，需接续观察', '联系入院/转专科', '评估后可离院'];
const loadHandoverPending = async () => {
  hoLoading.value = true;
  try {
    const [pendingRes, takerRes] = await Promise.all([
      getEmergencyHandoverPendingList(),
      getEmergencyHandoverTakeList(),
    ]);
    hoPending.value = pendingRes.data || [];
    takers.value = (takerRes.data || []).map((t) => ({...t, empId: String(t.empId)}));
    // 每次重开都重新逐行建账：残留上一班的交代等于把没交接的人算成交接了
    hoRows.value = {};
    hoPending.value.forEach((row) => {
      hoRows.value[String(row.emergencyId)] = {takeDoctorId: null, disposition: '', handoverNote: ''};
    });
  } catch (error) {
    ElMessage.error(error.message || '待交班清单加载失败');
  } finally {
    hoLoading.value = false;
  }
};
const openHandover = async () => {
  hoForm.value = {takeEmpId: null, remark: ''};
  hoVisible.value = true;
  await loadHandoverPending();
};
/** 未点名条数 > 0 时禁用提交按钮：让"漏交"在点下去之前就显形，而不是等后端拒绝 */
const hoMissing = computed(() => hoPending.value.filter((row) => !(hoRows.value[String(row.emergencyId)]?.disposition || '').trim()).length);
const hoPoolCount = computed(() => hoPending.value.filter((row) => row.poolFlag).length);
const submitHandover = async () => {
  if (!hoForm.value.takeEmpId)
    return ElMessage.warning('请选择接班人');
  if (hoMissing.value > 0)
    return ElMessage.warning(`还有 ${hoMissing.value} 条未填写去向交代`);
  const take = takers.value.find((t) => t.empId === hoForm.value.takeEmpId);
  try {
    await ElMessageBox.confirm(`确认把 ${hoPending.value.length} 名未闭环患者移交给 ${take?.empName || '接班人'}？移交后这些患者的责任医生即刻变更，台账不可修改。`, '交班确认', {
      confirmButtonText: '确认交班',
      cancelButtonText: '取消',
      type: 'warning'
    });
  } catch {
    return;
  }
  hoSubmitting.value = true;
  try {
    const res = await saveEmergencyHandover({
      takeEmpId: hoForm.value.takeEmpId,
      remark: hoForm.value.remark || undefined,
      items: hoPending.value.map((row) => {
        const r = hoRows.value[String(row.emergencyId)] || {takeDoctorId: null, disposition: '', handoverNote: ''};
        return {
          emergencyId: String(row.emergencyId),
          takeDoctorId: r.takeDoctorId || undefined,
          disposition: r.disposition.trim(),
          handoverNote: r.handoverNote || undefined,
        };
      }),
    });
    ElMessage.success(`交班完成（${res.data}），未闭环清单已清零`);
    hoVisible.value = false;
    await Promise.all([loadData(), loadStats()]);
  } catch (error) {
    ElMessage.error(error.message || '交班失败');
    // 清单可能已被别人改动（新登记/已接诊），失败后重新拉一次再让用户点名
    await loadHandoverPending();
  } finally {
    hoSubmitting.value = false;
  }
};
// 交班台账：只读凭证，翻页看历史
const hoLedgerVisible = ref(false);
const hoLedgerLoading = ref(false);
const hoLedgerList = ref([]);
const hoLedgerKeyword = ref('');
const hoLedgerPage = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const loadHoLedger = async () => {
  hoLedgerLoading.value = true;
  try {
    const params = {pageNum: hoLedgerPage.value.pageNum, pageSize: hoLedgerPage.value.pageSize};
    if (hoLedgerKeyword.value)
      params.keyword = hoLedgerKeyword.value;
    const res = await getEmergencyHandoverListPage(params);
    hoLedgerList.value = res.data?.records || [];
    hoLedgerPage.value.total = res.data?.total || 0;
  } catch (error) {
    ElMessage.error(error.message || '交班台账加载失败');
  } finally {
    hoLedgerLoading.value = false;
  }
};
const openHoLedger = () => {
  hoLedgerKeyword.value = '';
  hoLedgerPage.value.pageNum = 1;
  hoLedgerVisible.value = true;
  loadHoLedger();
};
const onHoLedgerSearch = () => {
  hoLedgerPage.value.pageNum = 1;
  loadHoLedger();
};
const hoDetailVisible = ref(false);
const hoDetailLoading = ref(false);
const hoDetail = ref(null);
const openHoDetail = async (row) => {
  hoDetail.value = null;
  hoDetailVisible.value = true;
  hoDetailLoading.value = true;
  try {
    const res = await getEmergencyHandoverDetail(String(row.id));
    hoDetail.value = res.data;
  } catch (error) {
    ElMessage.error(error.message || '交班单加载失败');
  } finally {
    hoDetailLoading.value = false;
  }
};
onMounted(() => {
  loadData();
  loadStats();
  loadDepartments();
});
</script>
