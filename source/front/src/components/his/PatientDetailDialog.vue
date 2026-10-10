<template>
  <el-dialog
      v-model="visible"
      class="patient-detail-dialog"
      destroy-on-close
      title="患者详情"
      top="6vh"
      width="960px"
  >
    <!-- patient-detail-shell / patient-detail-fixed 两个类配合全局 style.css 把高度链接通：
         shell 吃掉 body 剩余高度，fixed 的部分（身份卡/概览/警告）固定不压缩，
         剩下的高度留给内容区自己滚（否则会出现「弹框一条 + 内容区一条」两个滚动条） -->
    <div class="patient-detail-shell">
      <!-- 身份卡 -->
      <div class="patient-detail-fixed mb-4 rounded-lg border border-slate-200 bg-slate-50 p-4">
        <div class="flex items-start gap-4">
          <!-- 头像用姓名首字（性别符号换成首字：性别下方文字已给出，重复占位不如放姓名缩写）；
               底色与形状走全局 style.css 的 .patient-avatar-*，与搜索下拉项同一组色值、同一种圆角 -->
          <span
              :class="'patient-avatar-' + patientAvatarTone(headPatient.gender)"
              class="patient-avatar flex h-14 w-14 shrink-0 items-center justify-center text-xl font-bold text-white">
            {{ headAvatarText }}
          </span>
          <div class="min-w-0 flex-1">
            <div class="flex flex-wrap items-center gap-2">
              <h3 class="text-lg font-semibold text-slate-900">{{ headName }}</h3>
              <button
                  v-if="headPatient.patientName"
                  class="copy-btn"
                  title="复制患者姓名"
                  @click="handleCopy(headName, '姓名')"
              >
                <CopyDocument class="h-3.5 w-3.5"/>
              </button>
              <span v-if="headPatient.patientNo" class="flex items-center gap-1">
                <span class="font-mono text-sm text-slate-400">{{ freeText(headPatient.patientNo) }}</span>
                <button
                    class="copy-btn"
                    title="复制患者号"
                    @click="handleCopy(headPatient.patientNo, '患者号')"
                >
                  <CopyDocument class="h-3.5 w-3.5"/>
                </button>
              </span>
              <span class="text-sm text-slate-500">
                {{ patientGenderText(headPatient.gender) }} · {{ patientAgeText(headPatient.age) }}
              </span>
              <span v-if="allergyAlert" class="rounded bg-red-100 px-2 py-0.5 text-xs font-medium text-red-600">
                ⚠ 过敏：{{ allergyAlert }}
              </span>
              <span v-if="isMergedArchive" class="rounded bg-amber-100 px-2 py-0.5 text-xs font-medium text-amber-700">
                已并入主档 {{ freeText(cdrPatient.masterNo) }}
              </span>
            </div>
            <!-- 手机号 / 身份证由**后端**出参时就打码（getDetailById 走 maskDetailSensitiveFields，
                 列表 VO 走 maskListSensitiveFields），这里只渲染 xxxMasked 字段。
                 前端遮等于没遮：明文仍在响应体里，抓包/日志/第二个调用方都会漏 -->
            <div class="mt-1.5 flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-slate-500">
              <span>电话：{{ freeText(headPatient.phoneMasked) }}</span>
              <span>血型：{{ freeText(headPatient.bloodType) }}</span>
              <span>患者类型：{{ patientTypeText(headPatient.patientType) }}</span>
              <span>身份证：{{ freeText(headPatient.idCardMasked) }}</span>
            </div>
            <!-- 患者标签：运营标记（VIP / 高血压 / 建档提醒…），与列表页、搜索下拉同一套色值口径。
                 标签一多就会把这一行撑成三行、把下面的概览卡片挤下去，所以超出 8 个折叠成「+N」，
                 悬停才把剩下的铺出来 —— 展开态放在 tooltip 而不是就地换行，是为了让头部高度稳定
                 （头部高度一变，弹窗里那几个自适应区会跟着跳）。 -->
            <div v-if="detail" class="mt-1 flex flex-wrap items-center gap-1">
              <span class="text-xs text-slate-400">标签：</span>
              <template v-if="patientTags.length">
                <span
                    v-for="t in visibleTags"
                    :key="t.tagId"
                    :style="{ backgroundColor: t.tagColor || '#409EFF' }"
                    :title="t.tagName"
                    class="inline-flex items-center rounded px-1.5 py-0.5 text-xs font-medium text-white"
                >
                  {{ tagChipText(t) }}
                </span>
                <el-tooltip v-if="hiddenTags.length" :show-after="120" placement="top">
                  <template #content>
                    <div class="space-y-0.5">
                      <div v-for="t in hiddenTags" :key="t.tagId">{{ t.tagName }}</div>
                    </div>
                  </template>
                  <span
                      class="inline-flex cursor-help items-center rounded bg-slate-200 px-1.5 py-0.5 text-xs font-medium text-slate-600 hover:bg-slate-300"
                  >
                    +{{ hiddenTags.length }}
                  </span>
                </el-tooltip>
              </template>
              <span v-else class="text-xs text-slate-400">无</span>
            </div>
            <div class="mt-1 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-400">
              <span v-if="cdrPatient">
                档案完整度 {{ cdrPatient.completeRate ?? '—' }}%
                <template v-if="(cdrPatient.missingFields || []).length">
                  （缺 {{ cdrPatient.missingFields.join('、') }}）
                </template>
              </span>
              <span v-if="(cdrPatient?.shadowArchives || []).length" class="text-amber-600">
                含 {{ cdrPatient.shadowArchives.length }} 份被并档案的数据
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 概览：只在时间轴取到数据时显示，避免用 0 冒充「没有就诊」 -->
      <div v-if="summary" class="patient-detail-fixed mb-4 grid grid-cols-3 gap-3 lg:grid-cols-6">
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="text-xs text-slate-400">就诊次</p>
          <p class="text-lg font-semibold text-slate-900">{{ summary.visitCount ?? '—' }}</p>
          <p class="text-[11px] text-slate-400">
            门诊 {{ summary.outpatientCount ?? 0 }} / 住院 {{ summary.inpatientCount ?? 0 }} / 急诊
            {{ summary.emergencyCount ?? 0 }}
          </p>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="text-xs text-slate-400">在院</p>
          <p :class="Number(summary.activeInpatientCount) > 0 ? 'text-red-600' : 'text-slate-900'"
             class="text-lg font-semibold">
            {{ summary.activeInpatientCount ?? 0 }}
          </p>
          <p class="text-[11px] text-slate-400">未出院</p>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="text-xs text-slate-400">事件总数</p>
          <p class="text-lg font-semibold text-slate-900">{{ summary.eventCount ?? 0 }}</p>
          <p class="text-[11px] text-slate-400">已归入就诊次</p>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="text-xs text-slate-400">未归位</p>
          <p :class="Number(summary.unresolvedEventCount) > 0 ? 'text-amber-600' : 'text-slate-900'"
             class="text-lg font-semibold">
            {{ summary.unresolvedEventCount ?? 0 }}
          </p>
          <p class="text-[11px] text-slate-400">归属不到就诊次</p>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="text-xs text-slate-400">累计费用</p>
          <p class="text-lg font-semibold text-slate-900">{{ moneyText(summary.totalAmount) }}</p>
          <p class="text-[11px] text-slate-400">按就诊次合计</p>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="text-xs text-slate-400">时间跨度</p>
          <p class="text-sm font-semibold text-slate-900">{{ timeToDate(summary.firstVisitTime) }}</p>
          <p class="text-[11px] text-slate-400">至 {{ timeToDate(summary.lastVisitTime) }}</p>
        </div>
      </div>

      <el-alert
          v-if="(cdr?.warnings || []).length"
          :closable="false"
          class="patient-detail-fixed mb-3"
          type="warning"
      >
        <p v-for="(w, i) in cdr.warnings" :key="i" class="text-xs">{{ w }}</p>
      </el-alert>

      <el-tabs v-model="activeTab">
        <!-- ============ 基本信息 ============ -->
        <el-tab-pane label="基本信息" name="basic">
          <!-- 内容区自己滚（高度由 flex 分配、overflow-y:auto）：只出这一条滚动条 -->
          <div v-loading="detailLoading" class="dialog-scroll space-y-4 py-2 pr-1">
            <template v-if="detail">
              <section v-for="group in basicGroups" :key="group.title">
                <h4 class="mb-2 flex items-center gap-1.5 text-sm font-semibold text-slate-700">
                  <span class="h-3.5 w-1 rounded bg-blue-500"></span>{{ group.title }}
                </h4>
                <div
                    class="grid grid-cols-2 gap-x-4 gap-y-2 rounded-lg border border-slate-200 p-3 text-sm lg:grid-cols-4">
                  <div v-for="item in group.items" :key="item.label" class="flex items-center gap-1">
                    <span class="shrink-0 text-slate-400">{{ item.label }}：</span>
                    <span class="break-all font-medium text-slate-700">{{ item.value }}</span>
                    <button
                        v-if="item.copy"
                        :title="'复制' + item.label"
                        class="copy-btn"
                        @click="handleCopy(item.copy, item.label)"
                    >
                      <CopyDocument class="h-3 w-3"/>
                    </button>
                  </div>
                </div>
              </section>
            </template>
            <!-- 无临床权限的岗位（收费/药房/前台/医技）：把「为什么少两个 tab」说清楚，
                 否则用户只会觉得页面坏了 —— 收敛权限而不解释，等于制造一次误报障 -->
            <el-alert
                v-if="detail && !canViewClinical"
                :closable="false"
                class="mt-1"
                show-icon
                type="info"
            >
              <template #title>当前岗位只能查看患者身份与费用信息</template>
              <div class="text-xs text-slate-500">
                诊断、病历、检验检查结果属于临床内容，需要医生 / 护士 / 病案等岗位权限；如需查看请切换到相应岗位。
              </div>
            </el-alert>
            <div v-else-if="detailFailed && !detailLoading" class="py-14 text-center text-sm text-slate-400">
              基本信息加载失败
              <el-button class="ml-2" link type="primary" @click="reloadAll">重新加载</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- ============ 健康档案（临床内容，按岗位收敛） ============ -->
        <el-tab-pane v-if="canViewClinical" label="健康档案" name="profile">
          <!-- 这一 tab 只读。改档案去 /health-record（六组需要完整界面，塞进弹框必然长成第二套实现） -->
          <div class="mb-2 flex items-center gap-2 rounded border border-slate-200 bg-slate-50 px-3 py-2">
            <span class="text-xs text-slate-500">此处只读。增删改请到健康档案页，六组共用同一份明细。</span>
            <el-button :icon="FirstAidKit" link size="small" type="primary" @click="openHealthRecord">
              去维护
            </el-button>
          </div>
          <!-- 六组档案（过敏/既往/手术/家族/用药/联系人）加起来可能很长，容器内滚动，不撑高弹窗 -->
          <div v-loading="cdrLoading" class="dialog-scroll py-2 pr-1">
            <template v-if="cdr">
              <div v-if="profileHasAny" class="grid grid-cols-1 gap-4 lg:grid-cols-3">
                <div
                    v-for="g in profileGroups"
                    :key="g.key"
                    :class="g.key === 'allergy' && Number(g.count) > 0 ? 'border-red-200 bg-red-50' : 'border-slate-200'"
                    class="rounded-lg border p-3"
                >
                  <p :class="g.key === 'allergy' && Number(g.count) > 0 ? 'text-red-700' : 'text-slate-500'"
                     class="mb-1.5 text-xs font-medium">
                    {{ g.label }}（{{ g.count }}）
                  </p>
                  <ul v-if="Number(g.count) > 0" class="space-y-1">
                    <li v-for="it in g.items" :key="it.id" class="text-sm text-slate-700">
                      <span class="font-medium">{{ freeText(it.title) }}</span>
                      <span v-if="it.summary" class="text-slate-500"> · {{ it.summary }}</span>
                      <span v-if="it.time" class="text-xs text-slate-400">（{{ timeToDate(it.time) }}）</span>
                    </li>
                  </ul>
                  <p v-else class="text-sm text-slate-400">暂无</p>
                </div>
              </div>
              <div v-else class="py-14 text-center text-sm text-slate-400">
                该患者没有健康档案记录（过敏史 / 既往史 / 手术史 / 家族史 / 用药史 / 联系人）
              </div>
            </template>
            <div v-else-if="cdrFailed && !cdrLoading" class="py-14 text-center text-sm text-slate-400">
              健康档案加载失败
              <el-button class="ml-2" link type="primary" @click="reloadAll">重新加载</el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- ============ 就诊脉络（临床内容，按岗位收敛） ============ -->
        <el-tab-pane v-if="canViewClinical" name="visits">
          <template #label>
            就诊脉络
            <span v-if="cdr" class="text-slate-400">（{{ visits.length }}）</span>
          </template>
          <!-- 就诊脉络一条就诊次就是一张卡，多次就诊会很长，容器内滚动 -->
          <div v-loading="cdrLoading" class="dialog-scroll py-2 pr-1">
            <template v-if="cdr">
              <div v-if="eventTypeOptions.length" class="mb-3 flex flex-wrap items-center gap-2">
                <span class="text-xs text-slate-400">按类型筛选：</span>
                <el-tag
                    :effect="eventFilter === '' ? 'dark' : 'plain'"
                    :type="eventFilter === '' ? 'primary' : 'info'"
                    class="cursor-pointer"
                    @click="eventFilter = ''"
                >
                  全部
                </el-tag>
                <el-tag
                    v-for="opt in eventTypeOptions"
                    :key="opt.value"
                    :effect="eventFilter === opt.value ? 'dark' : 'plain'"
                    :type="eventFilter === opt.value ? 'primary' : 'info'"
                    class="cursor-pointer"
                    @click="eventFilter = opt.value"
                >
                  {{ opt.label }}
                </el-tag>
                <span class="ml-auto text-xs text-slate-400">当前显示 {{ totalShownEvents }} 条</span>
              </div>

              <el-empty
                  v-if="!visits.length && !unresolvedEvents.length"
                  description="该患者没有任何就诊记录"
              />
              <el-empty
                  v-else-if="totalShownEvents === 0"
                  :description="`该患者没有「${eventTypeOptions.find(o => o.value === eventFilter)?.label || eventFilter}」类型的记录`"
              />

              <el-timeline v-else>
                <el-timeline-item
                    v-for="v in visits"
                    :key="v.nodeKey"
                    :color="nodeColor(v.nodeType)"
                    placement="top"
                >
                  <div
                      v-if="filterEvents(v.events).length || !eventFilter"
                      class="rounded-lg border border-slate-200 bg-slate-50/60"
                  >
                    <div class="flex flex-wrap items-center gap-2 border-b border-slate-200 px-4 py-2">
                      <el-tag :type="nodeTypeTag(v.nodeType)" effect="dark" size="small">{{ v.nodeTypeText }}</el-tag>
                      <span class="text-sm font-semibold text-slate-800">{{ v.title }}</span>
                      <span v-if="v.anchorNo" class="font-mono text-xs text-slate-500">{{ v.anchorNo }}</span>
                      <span v-if="v.fromShadow" class="rounded bg-amber-100 px-1.5 py-0.5 text-[10px] text-amber-700">
                        数据来自被并档案
                      </span>
                      <span class="ml-auto flex items-center gap-3 text-xs text-slate-500">
                        <span v-if="v.statusText">{{ v.statusText }}</span>
                        <span v-if="v.durationDays">{{ v.durationDays }} 天</span>
                        <span v-if="Number(v.totalAmount)">{{ moneyText(v.totalAmount) }}</span>
                        <span>{{ filterEvents(v.events).length }} 条</span>
                      </span>
                    </div>
                    <div class="flex flex-wrap items-center gap-x-4 gap-y-1 px-4 py-1.5 text-xs text-slate-500">
                      <span class="flex items-center gap-1">
                        <el-icon><Clock/></el-icon>{{ timeToMinute(v.startTime) }}
                        <template v-if="v.endTime"> ~ {{ timeToMinute(v.endTime) }}</template>
                      </span>
                      <span v-if="v.deptName">科室：{{ v.deptName }}</span>
                      <span v-if="v.operatorName">医生：{{ v.operatorName }}</span>
                      <span v-if="v.outcome">诊断：{{ v.outcome }}</span>
                    </div>
                    <div
                        v-if="(v.gaps || []).length"
                        class="flex flex-wrap items-center gap-1 border-t border-amber-100 bg-amber-50 px-4 py-1.5 text-xs text-amber-700"
                    >
                      <el-icon>
                        <WarningFilled/>
                      </el-icon>
                      <span>病历完整性缺口：</span>
                      <span v-for="g in v.gaps" :key="g" class="rounded bg-amber-100 px-1.5 py-0.5">{{ g }}</span>
                    </div>
                    <div v-if="filterEvents(v.events).length" class="divide-y divide-slate-100">
                      <div
                          v-for="e in filterEvents(v.events)"
                          :key="e.eventType + '#' + e.sourceId"
                          class="flex flex-wrap items-start gap-x-3 gap-y-1 px-4 py-2"
                      >
                        <span class="w-24 shrink-0 font-mono text-xs text-slate-400">
                          {{ timeToMinute(e.eventTime).substring(5) }}
                        </span>
                        <el-tag :type="eventTag(e.eventType)" class="shrink-0" effect="plain" size="small">
                          {{ e.eventTypeText || e.eventType }}
                        </el-tag>
                        <span class="text-sm font-medium text-slate-700">{{ freeText(e.title) }}</span>
                        <span v-if="e.summary" class="text-xs text-slate-500">{{ e.summary }}</span>
                        <span v-if="e.secondaryText" class="text-xs text-slate-400">
                          {{ e.secondaryLabel || '' }}{{ e.secondaryText }}
                        </span>
                        <span v-if="Number(e.amount)" class="ml-auto text-xs text-slate-600">
                          {{ e.amountLabel || '金额' }} {{ moneyText(e.amount) }}
                        </span>
                      </div>
                    </div>
                  </div>
                </el-timeline-item>
              </el-timeline>

              <!-- 归属不到就诊次的记录：必须显示，藏起来等于数据丢了 -->
              <div v-if="filterEvents(unresolvedEvents).length" class="mt-4">
                <p class="mb-2 flex items-center gap-1.5 text-sm font-semibold text-amber-700">
                  <el-icon>
                    <WarningFilled/>
                  </el-icon>
                  未归入就诊次的记录（{{ filterEvents(unresolvedEvents).length }}）—— 单据悬空，不是丢了
                </p>
                <div class="divide-y divide-slate-100 rounded-lg border border-amber-200 bg-amber-50/50">
                  <div
                      v-for="e in filterEvents(unresolvedEvents)"
                      :key="'u' + e.eventType + '#' + e.sourceId"
                      class="flex flex-wrap items-center gap-x-3 gap-y-1 px-4 py-2"
                  >
                    <span class="font-mono text-xs text-slate-400">{{ timeToDate(e.eventTime) }}</span>
                    <el-tag :type="eventTag(e.eventType)" effect="plain" size="small">{{
                        e.eventTypeText || e.eventType
                      }}
                    </el-tag>
                    <span class="text-sm font-medium text-slate-700">{{ freeText(e.title) }}</span>
                    <span v-if="e.summary" class="text-xs text-slate-500">{{ e.summary }}</span>
                  </div>
                </div>
              </div>
            </template>
            <div v-else-if="cdrFailed && !cdrLoading" class="py-14 text-center text-sm text-slate-400">
              就诊脉络加载失败
              <el-button class="ml-2" link type="primary" @click="reloadAll">重新加载</el-button>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <template #footer>
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-2 text-xs text-slate-400">
          <template v-if="cdr">
            <el-icon>
              <Link/>
            </el-icon>
            数据按 EMPI 口径归并，含被并档案
          </template>
        </div>
        <div class="flex items-center gap-2">
          <el-button v-if="canViewClinical" :icon="FirstAidKit" @click="openHealthRecord">维护健康档案</el-button>
          <el-button v-if="showTimelineEntry && canViewClinical" :icon="Document" @click="openFullTimeline">
            打开完整时间轴
          </el-button>
          <el-button type="primary" @click="visible = false">关闭</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script lang="js" setup>
import {computed, ref, watch} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {Clock, CopyDocument, Document, FirstAidKit, Link, WarningFilled} from '@element-plus/icons-vue'
import {getPatientFullDetail} from '@/api/patient'
import {getPatientCdr} from '@/api/cdr'
import {patientAgeText, patientAvatarTone, patientGenderText} from '@/lib/patientGender'
import {patientTypeText} from '@/lib/patientType'
import {copyText} from '@/lib/clipboard'
import {hasPermission, loadPermissions} from '@/lib/permission'
import {TAG_VISIBLE_LIMIT, tagChipText} from '@/lib/patientTag'
import {
  cardTypeText,
  freeText,
  maritalStatusText,
  moneyText,
  patientStatusText,
  timeToDate,
  timeToMinute,
} from '@/lib/patientField'

const props = defineProps({
  // v-model 控制显隐
  modelValue: {type: Boolean, default: false},
  // 患者ID（雪花ID，务必传字符串，避免精度丢失）
  patientId: {type: [String, Number], default: ''},
  // 可选：列表行已有数据，先渲染头部避免弹框刚打开时一片空白
  patient: {type: Object, default: null},
  // 是否显示「打开完整时间轴」入口
  showTimelineEntry: {type: Boolean, default: true},
})

const emit = defineEmits(['update:modelValue'])

const router = useRouter()

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const activeTab = ref('basic')

/**
 * 能不能看**临床内容**（健康档案 / 就诊脉络）。
 *
 * 权限码与后端 CdrController 上的 @PreAuthorize 同源（`patient:cdr:list`）——
 * 前端隐藏的入口和后端拦的接口必须同一个口径，否则会出现两种错配：
 * 「看得见、点进去 403」或者「藏起来了、接口却敞着」。
 *
 * 收费员/药剂师/检验技师这类岗位的菜单里没有『患者全景』，所以拿不到这个码：
 * 他们看得到患者身份与费用（窗口收款、发药核对要用），看不到诊断与病历原文
 * （最小必要原则 —— 这是真实 HIS 稽核会查的项）。
 */
const canViewClinical = computed(() => hasPermission('patient:cdr:list'))

/* ---------- 主档详情 ---------- */
const detail = ref(null)
const detailLoading = ref(false)
const detailFailed = ref(false)

/* ---------- CDR 全景时间轴 ---------- */
const cdr = ref(null)
const cdrLoading = ref(false)
const cdrFailed = ref(false)

/* ---------- 就诊脉络里的事件类型筛选 ---------- */
const eventFilter = ref('')

const reset = () => {
  activeTab.value = 'basic'
  eventFilter.value = ''
  detail.value = null
  cdr.value = null
  detailFailed.value = false
  cdrFailed.value = false
}

const loadDetail = async (id) => {
  detailLoading.value = true
  detailFailed.value = false
  try {
    const res = await getPatientFullDetail(id)
    if (res?.code === 200 && res.data) {
      detail.value = res.data
    } else {
      detailFailed.value = true
    }
  } catch (e) {
    console.error('加载患者主档详情失败', e)
    detailFailed.value = true
  } finally {
    detailLoading.value = false
  }
}

const loadCdr = async (id) => {
  cdrLoading.value = true
  cdrFailed.value = false
  try {
    const res = await getPatientCdr({patientId: String(id)})
    if (res?.code === 200 && res.data) {
      cdr.value = res.data
    } else {
      cdrFailed.value = true
    }
  } catch (e) {
    console.error('加载患者全景时间轴失败', e)
    cdrFailed.value = true
  } finally {
    cdrLoading.value = false
  }
}

const load = async () => {
  const id = props.patientId
  if (!id) return
  reset()
  // 先判定权限再取数：否则会给没权限的岗位发一个注定 403 的 CDR 请求，
  // 控制台报错 + tab 里显示「加载失败」，用户以为系统坏了（其实是他这个岗位不该看）。
  await loadPermissions()
  loadDetail(id)
  if (canViewClinical.value) {
    loadCdr(id)
  }
}

watch(
    () => props.modelValue,
    (v) => {
      if (v) load()
    }
)

const reloadAll = () => load()

/* ---------- 头部身份卡：优先用主档详情，回落到列表行数据 ---------- */
const headPatient = computed(() => detail.value || props.patient || {})

const headName = computed(() => headPatient.value.patientName || '—')

// 头像：显示姓名首字（此前放的是性别符号 ♂/♀，那是「身份属性」不是「头像」，
// 而且旁边一行已经写了「男 · 38岁」，性别重复出现两次还占掉了唯一能放姓名缩写的位置）
const headAvatarText = computed(() => {
  const n = String(headPatient.value.patientName || '').trim()
  return n ? n.slice(0, 1) : '?'
})

/* ---------- 患者标签 ---------- */
/**
 * 标签来自主档详情（`PatientDetailVO.tags`），不单独发请求。
 *
 * ⚠️ 只有在 `detail` 加载成功后才渲染这一行：加载中或加载失败时若渲染成「无」，
 * 就成了把「没查到」伪装成「该患者没有标签」—— 标签是 VIP / 欠费这类要认人的标记，
 * 说错比不说代价大。
 *
 * chip 文本与折叠阈值都走 `lib/patientTag`：列表页 / 选患者下拉 / 这里三处必须同一口径
 * （曾经这里渲染 shortName「糖」、那两处渲染「糖尿病」，同一个患者两个说法）。
 */
const patientTags = computed(() => detail.value?.tags || [])
const visibleTags = computed(() => patientTags.value.slice(0, TAG_VISIBLE_LIMIT))
const hiddenTags = computed(() => patientTags.value.slice(TAG_VISIBLE_LIMIT))

/**
 * 复制姓名 / 患者号
 *
 * 为什么值得给这两个字段单独做按钮：它们是**跨系统传递患者身份的唯二短标识**，
 * 而真实科室里的动作几乎都要把这两样东西搬到别处去 ——
 *   ① 报给检验科/影像科核对标本、排队叫号（口头念 + 抄号，抄错一位就是另一个人的报告）；
 *   ② 填纸质单据、传染病卡、转诊单、外院会诊申请；
 *   ③ 微信/站内信里告诉同事「这个人你接手一下」；
 *   ④ 录进体检、PACS、医保等外部系统（那些系统只认患者号/姓名，没有复制就只能手打）。
 * 患者号（如 ZX20260918000123）和雪花 ID 都是十几二十位，手抄必错、错了还不报错 ——
 * 复制不是为了省事，是为了**少一次录错人**。
 */
const handleCopy = async (text, label) => {
  const ok = await copyText(text)
  if (ok) {
    ElMessage.success(`已复制${label}：${text}`)
  } else {
    ElMessage.error(`复制${label}失败，请手动选择文本`)
  }
}

// CDR 身份卡能提供 EMPI 归并信息与档案完整度，主档 VO 没有
const cdrPatient = computed(() => cdr.value?.patient || null)

const allergyAlert = computed(() => {
  const text = headPatient.value.allergyHistory
  if (text && String(text).trim() !== '' && String(text).trim() !== '无') return String(text)
  // 结构化过敏档案也算，避免「文字栏空着但档案里有过敏」时漏旗
  const group = (cdr.value?.profile || []).find((g) => g.key === 'allergy')
  if (group && Number(group.count) > 0) {
    return group.items.map((i) => i.title).filter(Boolean).join('、')
  }
  return ''
})

const isMergedArchive = computed(() => Number(cdrPatient.value?.mergeStatus) === 1)

/* ---------- 概览 ---------- */
const summary = computed(() => cdr.value?.summary || null)

/* ---------- 健康档案（六组：过敏/既往/手术/家族/用药/联系人） ---------- */
const profileGroups = computed(() => cdr.value?.profile || [])
const profileHasAny = computed(() => profileGroups.value.some((g) => Number(g.count) > 0))

/* ---------- 就诊脉络 ---------- */
const visits = computed(() => cdr.value?.visits || [])
const unresolvedEvents = computed(() => cdr.value?.unresolvedEvents || [])

// 事件类型筛选选项：从真实数据里聚合，不依赖额外接口，也不会出现「筛了必然为空」的假选项
const eventTypeOptions = computed(() => {
  const map = new Map()
  const walk = (list) => {
    list.forEach((e) => {
      if (!e?.eventType) return
      if (!map.has(e.eventType)) map.set(e.eventType, e.eventTypeText || e.eventType)
    })
  }
  visits.value.forEach((v) => walk(v.events || []))
  walk(unresolvedEvents.value)
  return Array.from(map, ([value, label]) => ({value, label}))
})

const filterEvents = (events) => {
  const list = events || []
  if (!eventFilter.value) return list
  return list.filter((e) => e.eventType === eventFilter.value)
}

const totalShownEvents = computed(() =>
    visits.value.reduce((sum, v) => sum + filterEvents(v.events).length, 0) +
    filterEvents(unresolvedEvents.value).length
)

/* ---------- 展示辅助（跟 CDR 页面同一套口径） ---------- */
const nodeColor = (t) =>
    t === 'INPATIENT' ? '#dc2626' : t === 'EMERGENCY' ? '#d97706' : t === 'OUTPATIENT' ? '#1269B5' : '#64748b'

const nodeTypeTag = (t) =>
    t === 'INPATIENT' ? 'danger' : t === 'EMERGENCY' ? 'warning' : t === 'OUTPATIENT' ? 'primary' : 'info'

const eventTag = (t) => {
  if (t === 'criticalValue') return 'danger'
  if (t === 'qualityControl' || t === 'referral') return 'warning'
  if (t === 'charge' || t === 'prepay' || t === 'inpatientSettlement' || t === 'insuranceSettlement') return 'success'
  return 'info'
}

/* ---------- 基本信息分组（口径统一走 lib/） ----------
 * item 形状：{ label, value, copy? } —— copy 有值时右侧渲染复制按钮
 * （只有「患者号 / 姓名」这两个跨系统传人用的短标识才给复制，其余字段给了也没人用，
 *  反而让每一行都长出图标、真正的字段值被挤得看不见）
 * 手机号 / 证件号 / 身份证 / 医保卡号一律走 lib/patientField 的打码口径，禁止裸渲染 */
const basicGroups = computed(() => {
  const d = detail.value
  if (!d) return []
  return [
    {
      title: '身份识别',
      items: [
        {label: '患者号', value: freeText(d.patientNo), copy: d.patientNo ? String(d.patientNo) : ''},
        {label: '姓名', value: freeText(d.patientName), copy: d.patientName ? String(d.patientName) : ''},
        {label: '性别', value: patientGenderText(d.gender)},
        {label: '年龄', value: patientAgeText(d.age)},
        {label: '出生日期', value: freeText(d.birthDate)},
        {label: '证件类型', value: cardTypeText(d.cardType)},
        {label: '证件号码', value: freeText(d.cardNoMasked)},
        {label: '身份证号', value: freeText(d.idCardMasked)},
      ],
    },
    {
      title: '联系方式',
      items: [
        {label: '联系电话', value: freeText(d.phoneMasked)},
        {label: '联系人', value: freeText(d.contactName)},
        {label: '联系人电话', value: freeText(d.contactPhoneMasked)},
        {label: '与患者关系', value: freeText(d.contactRelation)},
        {label: '家庭住址', value: freeText(d.address)},
        {label: '民族', value: freeText(d.nation)},
        {label: '职业', value: freeText(d.occupation)},
        {label: '婚姻状况', value: maritalStatusText(d.maritalStatus)},
      ],
    },
    {
      title: '参保与账户',
      items: [
        {label: '患者类型', value: patientTypeText(d.patientType)},
        {label: '医保类型', value: freeText(d.medicalInsuranceType)},
        {label: '医保卡号', value: freeText(d.medicalInsuranceNoMasked)},
        {label: '账户余额', value: moneyText(d.balance)},
        {label: '累计消费', value: moneyText(d.totalExpense)},
        {label: '就诊次数', value: d.visitCount === null || d.visitCount === undefined ? '—' : `${d.visitCount} 次`},
        {label: '最近就诊', value: freeText(d.lastVisitTime)},
        {label: '档案状态', value: patientStatusText(d.status)},
      ],
    },
    {
      title: '其他',
      items: [
        {label: '血型', value: freeText(d.bloodType)},
        // 「自述既往史/过敏史」属临床内容：没有 patient:cdr:list 的岗位（收费/药房/前台），
        // 服务端根本不会返回这两个字段，照常渲染就会显示成「—」——
        // 那等于把"无权看"伪装成"该患者没有过敏史"，比不显示更危险。所以整行不渲染。
        ...(canViewClinical.value
            ? [
              {label: '既往病史（自述）', value: freeText(d.medicalHistory)},
              {label: '过敏史（自述）', value: freeText(d.allergyHistory)},
            ]
            : []),
      ],
    },
  ]
})

const openFullTimeline = () => {
  visible.value = false
  router.push({path: '/cdr', query: {patientId: String(props.patientId)}})
}

/**
 * 跳到健康档案页维护这一患者。
 *
 * 这里刻意**不做就地编辑**：六组档案是纵向数据，需要「按条目增删改 + 看主档摘要投影」的完整界面，
 * 塞进这个弹框会变成第二套实现（字段、校验、字典翻译各写一遍），两套必然会漂。
 * 弹框只负责「看」，改去 `/health-record`，并把 patientId 带过去省掉再搜一次。
 */
const openHealthRecord = () => {
  visible.value = false
  router.push({path: '/health-record', query: {patientId: String(props.patientId)}})
}

defineExpose({reload: reloadAll})
</script>

<style scoped>
/* ---------- 内容区独立滚动 ----------
   高度不在这里算：由全局 style.css 的 `.patient-detail-dialog` 系列规则用 flex 一路分配下来
   （dialog → body → shell → el-tabs → el-tabs__content → el-tab-pane → 这里放 100%）。
   早先在这里写 `max-height: calc(100vh - 560px)` 是估出来的值，和 body 实际剩余空间对不上，
   结果弹框和内容区各出一条滚动条 —— 所以高度只由 flex 决定，这里只负责滚。
   overscroll-behavior: contain：滚到底不要把滚动链传给 body（否则整页跟着动/出滚动条）。
   滚动条样式沿用 style.css 的全局医院蓝细条，不在这里各写一套。 */
.dialog-scroll {
  /* 同样不能用 height:100%（父级 pane 的高度是 flex 分配的 used value，百分比会退化成 auto），
     用 flex 吃掉 pane 剩余高度。 */
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
}

/* ---------- 复制按钮 ----------
   默认淡灰，hover 才变蓝 —— 常显的图标会和字段值抢注意力，反而看不清信息。 */
.copy-btn {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  color: #94a3b8;
  background: transparent;
  cursor: pointer;
  transition: color 0.15s ease, background 0.15s ease;
}

.copy-btn:hover {
  color: #1269B5;
  background: #E8F1FA;
}
</style>
