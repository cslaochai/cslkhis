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
      <!-- 待复评：候诊超时应接诊时限且又超一个时限没复评——分诊台的催命榜。
           与「超时未接诊」的分工：那个催医生接诊，这个催护士重新分级。 -->
      <div class="cursor-pointer rounded-lg border-2 border-orange-200 bg-white p-4 shadow-sm"
           data-testid="emg-stat-reeval"
           @click="pickReevalDue">
        <div class="flex items-center gap-3">
          <div class="rounded-lg bg-orange-50 p-2.5">
            <RefreshRight class="h-5 w-5 text-orange-600"/>
          </div>
          <div>
            <p class="text-lg font-bold text-orange-600">{{ stats.reevalDue || 0 }}</p>
            <p class="text-xs text-slate-500">待复评</p>
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
        <!-- 待复评是「超时且又超一个时限没复评」的子集：和留观榜互斥（一个是催接诊、一个是催分级，
             都不该被「占床最久」的排序盖掉），和「只看超时」并行不冲突 -->
        <el-checkbox v-model="searchForm.reevalDueOnly" data-testid="emg-filter-reeval" @change="onReevalDueChange">
          只看待复评 {{ stats.reevalDue || 0 }}
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
        <el-button v-perm="'opd:emergency:add'" :icon="CopyDocument" type="warning" data-testid="emg-btn-batch-add"
                   @click="openBatch">批量登记
        </el-button>
      </div>
    </div>

    <!-- 视图切换：看板（默认，真实 HIS 急诊分诊台主界面形态：分区卡片）/ 列表（明细台账） -->
    <div class="flex items-center gap-3">
      <el-radio-group v-model="viewMode" data-testid="emg-view-switch">
        <el-radio-button value="board" data-testid="emg-view-board">看板</el-radio-button>
        <el-radio-button value="list" data-testid="emg-view-list">列表</el-radio-button>
      </el-radio-group>
      <span class="text-xs text-slate-400">看板按三区（红/黄/绿）分区、I 级置顶；列表为全部明细台账</span>
    </div>

    <!-- 三区看板：分区卡片（红区抢救 / 黄区急症 / 绿区非急症），区内按级别升序、同级按候诊时长降序 -->
    <div v-if="viewMode === 'board'" class="space-y-4" data-testid="emg-board">
      <div v-for="zone in boardZones" :key="zone.key"
           class="rounded-lg border-2 bg-white shadow-sm" :class="zone.border">
        <div class="flex items-center justify-between border-b border-slate-100 px-4 py-2.5">
          <div class="flex items-center gap-2">
            <span class="inline-block h-3 w-3 rounded-full" :class="zone.dot"></span>
            <span class="text-sm font-bold" :class="zone.text">{{ zone.label }}</span>
            <span class="rounded bg-slate-100 px-1.5 py-0.5 text-xs text-slate-500">{{ zone.items.length }} 人</span>
          </div>
          <span class="text-xs text-slate-400">{{ zone.hint }}</span>
        </div>
        <div v-if="zone.items.length === 0" class="py-8 text-center text-sm text-slate-400">本区暂无患者</div>
        <div v-else class="grid grid-cols-1 gap-3 p-3 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          <div v-for="row in zone.items" :key="row.id"
               :data-testid="'emg-card-' + row.id"
               class="rounded-lg border bg-white p-3 shadow-sm transition hover:shadow-md" :class="cardBorder(row)">
            <!-- 头部：患者 + 急诊号 + 状态 -->
            <div class="flex items-start justify-between gap-2">
              <div class="flex items-center gap-2">
                <span
                    :class="['inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-bold text-white', GENDER_DOT[patientAvatarTone(row.gender)]]">
                  {{ patientGenderSymbol(row.gender) }}
                </span>
                <div>
                  <div class="text-sm font-bold text-slate-900">{{ row.patientName }}
                    <span class="text-xs font-normal text-slate-400">{{ row.age }}岁</span></div>
                  <div class="font-mono text-xs text-slate-400">{{ row.emergencyNo }}</div>
                </div>
              </div>
              <span
                  :class="['inline-block rounded px-1.5 py-0.5 text-xs font-bold', statusMap[row.emergencyStatus]?.color || '']">
                {{ statusMap[row.emergencyStatus]?.label || '未知' }}
              </span>
            </div>
            <!-- 分级 + MEWS + 待复评 + 绿通 -->
            <div class="mt-2 flex flex-wrap items-center gap-1">
              <span
                  :class="['inline-block rounded border px-1.5 py-0.5 text-xs font-medium', triageLevelMap[row.triageLevel]?.color || '']">
                {{ triageLevelMap[row.triageLevel]?.label }}
              </span>
              <span v-if="row.mewsScore != null" data-testid="emg-mews-badge"
                    :class="['inline-block rounded px-1.5 py-0.5 text-xs font-bold',
                             row.mewsScore >= 9 ? 'bg-red-100 text-red-700' : row.mewsScore >= 5 ? 'bg-amber-100 text-amber-700' : 'bg-slate-100 text-slate-500']">
                MEWS {{ row.mewsScore }}
              </span>
              <span v-if="row.reevalDue" data-testid="emg-reeval-badge"
                    class="inline-block rounded border border-orange-300 bg-orange-50 px-1.5 py-0.5 text-xs font-bold text-orange-600">
                待复评
              </span>
              <span v-if="row.greenChannel"
                    class="rounded border border-emerald-200 bg-emerald-50 px-1.5 py-0.5 text-xs font-medium text-emerald-600">
                {{ row.greenChannel }}
              </span>
            </div>
            <!-- 主诉 -->
            <div class="mt-2 truncate text-xs text-slate-600" :title="row.chiefComplaint">{{
                row.chiefComplaint || '—'
              }}
            </div>
            <!-- 底部：等候 + 医生 + 操作 -->
            <div class="mt-2 flex items-center justify-between gap-2 border-t border-slate-100 pt-2">
              <div class="text-xs">
                <span
                    :class="['font-medium', row.overdueLevel === 2 ? 'text-red-600' : row.overdueLevel === 1 ? 'text-red-500' : 'text-slate-500']">
                  {{ waitText(row) }}
                </span>
                <span v-if="row.doctorName" class="ml-1 text-slate-400">{{ row.doctorName }}</span>
                <span v-else class="ml-1 text-amber-600">待派单</span>
              </div>
              <div class="flex flex-wrap justify-end gap-1">
                <el-button v-if="row.emergencyStatus === 1" v-perm="'opd:emergency:reeval'" link size="small"
                           type="warning"
                           data-testid="emg-btn-reeval" @click="openReeval(row)">复评
                </el-button>
                <el-button v-if="row.emergencyStatus === 1" v-perm="'opd:emergency:edit'" link size="small"
                           type="primary"
                           @click="handleStatusChange(row, 2)">接诊
                </el-button>
                <el-button v-if="row.emergencyStatus === 2" v-perm="'opd:emergency:edit'" link size="small"
                           type="warning"
                           @click="openObs(row)">留观
                </el-button>
                <el-button v-if="row.emergencyStatus === 2 || row.emergencyStatus === 3" v-perm="'opd:emergency:edit'"
                           link size="small" type="success"
                           @click="openAdmit(row)">转住院
                </el-button>
                <el-button v-if="row.emergencyStatus <= 3" v-perm="'opd:emergency:edit'" link size="small" type="info"
                           @click="handleStatusChange(row, 5)">离院
                </el-button>
                <el-button v-if="row.emergencyStatus <= 3" v-perm="'opd:emergency:edit'" link size="small" type="danger"
                           @click="handleStatusChange(row, 6)">死亡
                </el-button>
                <el-button link size="small" type="default" data-testid="emg-btn-detail"
                           @click="toggleExpand(row)">{{ expandedId === row.id ? '收起' : '详情' }}
                </el-button>
                <el-button v-perm="'opd:emergency:edit'" link size="small" type="primary" data-testid="emg-btn-transfer"
                           @click="openTransfer(row)">转归
                </el-button>
                <el-button v-perm="'opd:emergency:edit'" link size="small" type="default"
                           data-testid="emg-btn-wristband"
                           @click="openWristband(row)">腕带
                </el-button>
              </div>
              <!-- 展开区：就诊时间轴 + MEWS 趋势（真实 HIS 急诊患者详情面板标配） -->
              <div v-if="expandedId === row.id" class="mt-2 space-y-2 border-t border-slate-100 pt-2">
                <EmergencyTimeline :row="row"/>
                <EmergencyMewsTrend :points="mewsPoints(row)"/>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 急诊列表（明细台账，可切换查看） -->
    <div v-if="viewMode === 'list'" class="rounded-lg border border-slate-200 bg-white shadow-sm"
         data-testid="emg-table">
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
        <el-table-column align="center" label="分诊" width="150">
          <template #default="{ row }">
            <span
                :class="['inline-block rounded border px-2 py-0.5 text-xs font-medium', triageLevelMap[row.triageLevel]?.color || '']">
              {{ triageLevelMap[row.triageLevel]?.label }}
            </span>
            <!-- MEWS 与「分数不对但级别不高」的兜底：分高但级别轻的人要能被看见 -->
            <span v-if="row.mewsScore != null"
                  :class="['ml-1 inline-block rounded px-1.5 py-0.5 text-xs font-bold',
                           row.mewsScore >= 9 ? 'bg-red-100 text-red-700'
                           : row.mewsScore >= 5 ? 'bg-amber-100 text-amber-700' : 'bg-slate-100 text-slate-500']"
                  data-testid="emg-mews-badge">
              MEWS {{ row.mewsScore }}
            </span>
            <!-- 待复评徽章：分诊规范要求候诊超时须再次评估分级 -->
            <el-tooltip v-if="row.reevalDue" content="候诊已超应接诊时限且又超一个时限未复评，请重新评估分级"
                        placement="top">
              <span
                  class="ml-1 inline-block rounded border border-orange-300 bg-orange-50 px-1.5 py-0.5 text-xs font-bold text-orange-600"
                  data-testid="emg-reeval-badge">
                待复评
              </span>
            </el-tooltip>
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
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="{ row }">
            <el-button v-if="row.emergencyStatus === 1" v-perm="'opd:emergency:reeval'" link size="small" type="warning"
                       data-testid="emg-btn-reeval" @click="openReeval(row)">
              复评
            </el-button>
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
            <el-button v-perm="'opd:emergency:edit'" link size="small" type="default"
                       data-testid="emg-btn-wristband-list"
                       @click="openWristband(row)">腕带
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
    <EmergencyAdd
        v-model="addVisible"
        :gender-dot="GENDER_DOT"
        :triage-level-map="triageLevelMap"
        @saved="reloadAfterSave"
    />

    <!-- 分诊腕带打印（纯前端，零后端改动） -->
    <EmergencyWristband
        v-model="wristbandVisible"
        :row="wristbandRow"
        :triage-level-map="triageLevelMap"
    />

    <!-- 群伤 / 批量登记（循环调现有 register，零后端新接口） -->
    <EmergencyBatchAdd
        v-model="batchVisible"
        :triage-level-map="triageLevelMap"
        @saved="reloadAfterSave"
    />

    <!-- 转入留观 -->
    <EmergencyObsEdit
        v-model="obsVisible"
        :row="obsRow"
        :wards="wards"
        @saved="reloadAfterSave"
    />

    <!-- 分诊复评：候诊超时再次评估分级（只升不降，留痕） -->
    <EmergencyReevalEdit
        v-model="reevalVisible"
        :row="reevalRow"
        :triage-level-map="triageLevelMap"
        @saved="reloadAfterSave"
    />

    <!-- 急诊转住院（入院登记） -->
    <EmergencyAdmitEdit
        v-model="admitVisible"
        :row="admitRow"
        :wards="wards"
        @saved="reloadAfterSave"
    />
    <!-- 急诊交班清零 -->
    <EmergencyHandoverEdit v-model="hoVisible" @saved="reloadAfterSave"/>

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

    <!-- 交班单明细 -->
    <EmergencyHandoverDetail v-model="hoDetailVisible" :id="hoDetailId"/>

    <!-- 一键转归面板 -->
    <EmergencyTransferEdit v-model="transferVisible" :row="transferRow" :wards="wards" @saved="reloadAfterSave"/>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {
  AlarmClock,
  Bell,
  Check,
  Clock,
  CopyDocument,
  Plus,
  Refresh,
  RefreshRight,
  Search,
  Switch,
  User,
  Warning
} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  getEmergencyHandoverListPage,
  getEmergencyList,
  getEmergencyReevalList,
  getEmergencyStats,
  getEmergencyWardSelectList,
  updateEmergencyStatus
} from '@/api/emergency';
// 今日总值班：本科室阶梯走完后，超时催办落到他身上（sql/169）
import DutyOfficerBar from '@/components/his/DutyOfficerBar.vue';
import {patientAvatarTone, patientGenderSymbol} from '@/lib/patientGender';
import EmergencyAdd from './EmergencyAdd.vue';
import EmergencyAdmitEdit from './EmergencyAdmitEdit.vue';
import EmergencyHandoverDetail from './EmergencyHandoverDetail.vue';
import EmergencyHandoverEdit from './EmergencyHandoverEdit.vue';
import EmergencyObsEdit from './EmergencyObsEdit.vue';
import EmergencyReevalEdit from './EmergencyReevalEdit.vue';
import EmergencyTimeline from './EmergencyTimeline.vue';
import EmergencyMewsTrend from './EmergencyMewsTrend.vue';
import EmergencyTransferEdit from './EmergencyTransferEdit.vue';
import EmergencyWristband from './EmergencyWristband.vue';
import EmergencyBatchAdd from './EmergencyBatchAdd.vue';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';

const GENDER_DOT = {male: 'bg-blue-500', female: 'bg-pink-500', unknown: 'bg-slate-400'};

const loading = ref(false);
const emergencyList = ref([]);
const stats = ref({});
// 视图模式：看板（默认，真实 HIS 急诊分诊台主界面）/ 列表
const viewMode = ref('board');
/** 三区看板分组：按 zone 分块，区内 I 级置顶、同级按候诊时长降序（等最久优先） */
const boardZones = computed(() => {
  const groups = {'红区': [], '黄区': [], '绿区': []};
  for (const r of (emergencyList.value || [])) {
    const z = r.zone === '红区' ? '红区' : r.zone === '黄区' ? '黄区' : '绿区';
    groups[z].push(r);
  }
  const defs = [
    {
      key: '红区',
      label: '红区 · 抢救',
      border: 'border-red-200',
      dot: 'bg-red-500',
      text: 'text-red-700',
      hint: 'I/II 级，立即处置'
    },
    {
      key: '黄区',
      label: '黄区 · 急症',
      border: 'border-amber-200',
      dot: 'bg-amber-500',
      text: 'text-amber-700',
      hint: 'III 级，优先候诊'
    },
    {
      key: '绿区',
      label: '绿区 · 非急症',
      border: 'border-emerald-200',
      dot: 'bg-emerald-500',
      text: 'text-emerald-700',
      hint: 'IV 级，常规候诊'
    },
  ];
  return defs.map(d => {
    const items = (groups[d.key] || []).slice().sort((a, b) => {
      const la = a.triageLevel || 9, lb = b.triageLevel || 9;
      if (la !== lb) return la - lb;
      return (b.waitMinutes || 0) - (a.waitMinutes || 0);
    });
    return {...d, items};
  });
});
/** 卡片描边与分区同色 */
const cardBorder = (row) => row.zone === '红区' ? 'border-red-200'
    : row.zone === '黄区' ? 'border-amber-200' : 'border-emerald-200';
const searchForm = ref({
  keyword: '',
  triageLevel: null,
  emergencyStatus: null,
  // 三个「只看」是兜底看板的入口：待派单池 = 没人负责的，超时 = 该被追问的，待复评 = 该重新分级的
  unassignedOnly: false,
  overdueOnly: false,
  reevalDueOnly: false,
  // 留观榜：null=不看，0=全部在观，其余为统计卡带出的阈值（小时数由后端配置决定，前端不写死）
  obsBoardHours: null,
});
const pagination = ref({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  total: 0,
});
// 新增急诊弹窗
const addVisible = ref(false);
const triageLevelMap = {
  1: {label: 'I级 濒危', color: 'bg-red-100 text-red-700 border-red-300', zone: '红区'},
  2: {label: 'II级 危重', color: 'bg-orange-100 text-orange-700 border-orange-300', zone: '红区'},
  3: {label: 'III级 急症', color: 'bg-amber-100 text-amber-700 border-amber-300', zone: '黄区'},
  4: {label: 'IV级 非急症', color: 'bg-emerald-100 text-emerald-700 border-emerald-300', zone: '绿区'},
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
    if (searchForm.value.reevalDueOnly)
      params.reevalDueOnly = true;
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
const handleSearch = () => {
  pagination.value.pageNum = 1;
  loadData();
};
const handleReset = () => {
  searchForm.value = {
    keyword: '', triageLevel: null, emergencyStatus: null,
    unassignedOnly: false, overdueOnly: false, reevalDueOnly: false, obsBoardHours: null,
  };
  handleSearch();
};
/** 超时看的是「还在等的」，留观榜看的是「已经躺着的」，两个口径互斥（后端也会直接拒绝），所以开一个就关另一个 */
const onOverdueOnlyChange = () => {
  if (searchForm.value.overdueOnly)
    searchForm.value.obsBoardHours = null;
  handleSearch();
};
/** 待复评同样与留观榜互斥：它是候诊队列的催命榜，不能被「占床最久」的排序盖掉 */
const onReevalDueChange = () => {
  if (searchForm.value.reevalDueOnly)
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
  if (searchForm.value.obsBoardHours != null) {
    searchForm.value.overdueOnly = false;
    searchForm.value.reevalDueOnly = false;
  }
  handleSearch();
};
/** 点「待复评」卡片 = 勾上「只看待复评」（再点一次取消） */
const pickReevalDue = () => {
  searchForm.value.reevalDueOnly = !searchForm.value.reevalDueOnly;
  onReevalDueChange();
};
// 分诊复评弹框
const reevalVisible = ref(false);
const reevalRow = ref(null);
const openReeval = (row) => {
  reevalRow.value = row;
  reevalVisible.value = true;
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
  addVisible.value = true;
};

// 群伤 / 批量登记
const batchVisible = ref(false);
const openBatch = () => {
  batchVisible.value = true;
};

// 分诊腕带打印
const wristbandVisible = ref(false);
const wristbandRow = ref({});
const openWristband = (row) => {
  wristbandRow.value = row || {};
  wristbandVisible.value = true;
};
/** 弹框保存后统一刷新：列表与统计卡必须同一口径，少刷一个就是「数字和表格不一致」 */
const reloadAfterSave = async () => {
  await Promise.all([loadData(), loadStats()]);
};
// 卡片展开：时间轴 + MEWS 趋势（详情面板）。复评流水拉一次缓存，避免反复请求
const expandedId = ref(null);
const reevalMap = ref({});
const toggleExpand = async (row) => {
  if (expandedId.value === row.id) {
    expandedId.value = null;
    return;
  }
  expandedId.value = row.id;
  if (!reevalMap.value[row.id]) {
    try {
      const res = await getEmergencyReevalList(row.id);
      reevalMap.value = {...reevalMap.value, [row.id]: res.data || []};
    } catch {
      reevalMap.value = {...reevalMap.value, [row.id]: []};
    }
  }
};
/** MEWS 趋势点：复评历史 + 当前分（单点=无复评，仅有当前值） */
const mewsPoints = (row) => {
  const list = reevalMap.value[row.id] || [];
  const pts = list.map(r => ({t: (r.createTime || '').slice(5, 16), score: r.mewsScore}));
  if (row.mewsScore != null)
    pts.push({t: '当前', score: row.mewsScore});
  return pts;
};
// 一键转归面板
const transferVisible = ref(false);
const transferRow = ref(null);
const openTransfer = (row) => {
  transferRow.value = row;
  transferVisible.value = true;
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
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
// 病区名册由页面加载一次，传给留观与转住院两个弹框
const wards = ref([]);
const loadWards = async () => {
  try {
    const res = await getEmergencyWardSelectList();
    wards.value = res.data || [];
  } catch {
    wards.value = [];
  }
};
const obsVisible = ref(false);
const obsRow = ref(null);
const openObs = async (row) => {
  obsRow.value = row;
  if (wards.value.length === 0)
    await loadWards();
  obsVisible.value = true;
};
const admitVisible = ref(false);
const admitRow = ref(null);
const openAdmit = async (row) => {
  admitRow.value = row;
  if (wards.value.length === 0)
    await loadWards();
  admitVisible.value = true;
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
// 交班清零（sql/153）：清单出题、逐条点名、提交全在 EmergencyHandoverEdit 里，页面只管开合与刷新
const hoVisible = ref(false);
const openHandover = () => {
  hoVisible.value = true;
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
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

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
const hoDetailId = ref('');
const openHoDetail = (row) => {
  hoDetailId.value = row.id;
  hoDetailVisible.value = true;
};
onMounted(() => {
  loadData();
  loadStats();
});
</script>
