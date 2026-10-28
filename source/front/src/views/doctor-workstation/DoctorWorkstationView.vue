<template>
  <div class="flex h-[var(--his-page-h)] gap-3">
    <div class="flex w-[var(--his-queue-col-w)] shrink-0 flex-col gap-3">
      <div class="call-board shrink-0 rounded-lg p-3.5 shadow-sm">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-1.5">
            <span :class="doctorPaused ? 'is-away' : (currentCalledPatient ? 'is-busy' : 'is-idle')"
                  class="call-light"></span>
            <span class="text-xs font-bold tracking-wide text-slate-700">{{
                doctorPaused ? '暂离中' : (currentCalledPatient ? '接诊中' : '空闲')
              }}</span>
          </div>
          <span class="rounded bg-slate-100 px-1.5 py-0.5 text-xs font-medium text-slate-600">候诊 {{
              waitingCount
            }} 人</span>
        </div>

        <!-- 正在就诊：大号号码 + 姓名（窄列必须 truncate，否则名字挤成竖排） -->
        <div v-if="currentCalledPatient" class="mt-2">
          <div class="flex items-end gap-2">
            <div class="call-no leading-none text-[#1269B5]">{{
                shortQueueNo(currentCalledPatient.queueNo) || '—'
              }}
            </div>
            <div class="min-w-0 flex-1 pb-1">
              <div class="truncate text-lg font-bold leading-tight text-slate-900">
                {{ currentCalledPatient.patientName }}
              </div>
              <div class="mt-0.5 flex items-center gap-1 text-xs text-slate-500">
                <span>{{ patientGenderText(currentCalledPatient.gender) }} {{ currentCalledPatient.age }}岁</span>
                <span v-if="currentCalledPatient.registType === 3"
                      class="rounded bg-red-500 px-1 font-bold text-white">急</span>
              </div>
            </div>
          </div>
          <div class="mt-1.5 flex items-center justify-between text-xs text-slate-500">
            <span>已等待 {{ calcWaitMinutes(currentCalledPatient) }} 分钟</span>
            <span class="min-w-0 truncate pl-2 text-slate-400">{{ currentCalledPatient.patientNo }}</span>
          </div>
        </div>
        <div v-else class="mt-2 flex h-[74px] items-center">
          <span class="text-sm leading-6 text-slate-400">{{
              doctorPaused ? '已暂离，叫号将跳过本诊室' : '尚未叫号，点击下方「接诊下一位」开始'
            }}</span>
        </div>

        <!-- 下一位预览：排序口径 = 后端 callNext（危重级别优先 → 序号） -->
        <div class="mt-2 flex items-center gap-2 rounded-md bg-slate-50 px-2 py-1.5 text-xs">
          <span class="shrink-0 font-medium text-slate-500">下一位</span>
          <template v-if="nextWaiting">
            <span class="shrink-0 font-bold text-slate-900">{{ shortQueueNo(nextWaiting.queueNo) }}</span>
            <span class="min-w-0 truncate text-slate-700">{{ nextWaiting.patientName }}</span>
            <span v-if="nextWaiting.triageLevel && nextWaiting.triageLevel <= 2"
                  class="ml-auto shrink-0 rounded bg-red-500 px-1 text-[10px] font-bold text-white">
              {{ nextWaiting.triageLevel }}级
            </span>
          </template>
          <span v-else class="text-slate-400">队列已空</span>
        </div>

        <div class="mt-2.5 flex gap-2">
          <el-button :disabled="doctorPaused || !waitingCount" :icon="VideoPlay" class="!flex-1"
                     type="primary"
                     @click="handleCallNext">接诊下一位
          </el-button>
          <el-button :disabled="!currentCalledPatient || doctorPaused" :icon="RefreshRight" class="!ml-0 !px-4"
                     @click="handleRecallPatient">重呼
          </el-button>
        </div>
        <el-button :icon="VideoPause" :type="doctorPaused ? 'success' : 'warning'" class="mt-2 !w-full" plain
                   @click="handlePause">{{ doctorPaused ? '恢复接诊' : '暂离' }}
        </el-button>
      </div>
      <div class="flex min-h-0 flex-1 flex-col rounded-lg border border-slate-200 bg-white shadow-sm">
        <div class="flex shrink-0 items-center gap-1 border-b border-slate-200 px-2 py-2">
          <span class="pl-1 text-sm font-bold text-slate-700">候诊 {{ waitingCount }}</span>
          <span class="text-xs text-slate-400">已诊 {{ stats.called }}</span>
          <span class="ml-auto">
            <el-button :icon="RefreshRight" link size="small" @click="loadData">刷新
            </el-button>
          </span>
        </div>
        <!-- 搜索框 -->
        <div class=" border-slate-100 px-4 py-2">
          <el-input v-model="queueSearch" :prefix-icon="Search" clearable placeholder="搜索姓名、就诊号..."
                    size="small"/>
        </div>
        <!-- 过滤按钮 -->
        <div class="flex items-center gap-1 border-slate-100 px-4 py-2">
          <el-button
              v-for="f in [{ label: '全部', value: 'all' }, { label: '候诊', value: 'waiting' }, { label: '就诊中', value: 'consulting' }]"
              :key="f.value" :plain="queueFilter !== f.value" :type="queueFilter === f.value ? 'primary' : ''"
              class="!rounded-md"
              size="small"
              @click="queueFilter = f.value"
          >{{ f.label }}
          </el-button>
        </div>
        <!-- 患者列表 -->
        <div v-loading="loading" class="flex-1 overflow-y-auto">
          <div v-for="row in filteredQueue" :key="row.id"
               :class="{ 'bg-blue-50 border-l-2 border-l-blue-500': currentPatient?.id === row.id }"
               class="group cursor-pointer border-y border-slate-100 px-4 py-3 transition-colors hover:bg-slate-50"
               @click="selectPatient(row)"
          >
            <div class="flex items-center justify-between gap-2">
              <!-- 姓名单行截断：新疆等地音译长名（15~18 字）在 280px 窄列里若不 truncate 会换行成 2~3 行、
                   把排队号/状态徽章整块挤掉。名字 flex-1 截断、悬停 title 看全名，徽章 shrink-0 钉住不被挤。 -->
              <div class="flex min-w-0 flex-1 items-center gap-2">
                <span :title="row.patientName"
                      class="min-w-0 flex-1 truncate text-sm font-bold text-slate-900">{{ row.patientName }}</span>
                <span class="shrink-0 rounded bg-blue-50 px-1.5 py-0.5 text-xs font-medium text-blue-600">{{
                    shortQueueNo(row.queueNo)
                  }}</span>
              </div>
              <span
                  :class="['shrink-0 rounded px-1.5 py-0.5 text-xs', queueStatusMap[row.queueStatus]?.color || 'bg-slate-100 text-slate-600']">
                {{ queueStatusMap[row.queueStatus]?.label }}
              </span>
            </div>
            <div class="mt-1.5 flex flex-wrap items-center gap-1.5 text-xs">
              <!-- 分诊等级：只显示 1/2 级（危重/急症），3/4 级是常态不显示，避免噪音。
                   排序（callNext ORDER BY triage_level ASC）+ 禁止跳过危重（callSpecific）已是后端硬逻辑，
                   这里只把「哪个人是危重」在医生端显性化，否则医生看不出为什么下一位跳了号。 -->
              <span v-if="row.triageLevel === 1"
                    class="rounded bg-red-500 px-1.5 py-0.5 font-medium text-white">1级·危重</span>
              <span v-else-if="row.triageLevel === 2"
                    class="rounded bg-orange-500 px-1.5 py-0.5 font-medium text-white">2级·急症</span>
              <span
                  :class="['rounded px-1.5 py-0.5 font-medium',
                           GENDER_TONE_CLASS[patientAvatarTone(row.gender)]]">
                {{ patientGenderText(row.gender) }} {{ row.age }}岁
              </span>
              <span v-if="row.age && row.age >= 60" class="rounded bg-amber-50 px-1.5 py-0.5 text-amber-600">老年</span>
              <span v-else-if="row.age && row.age <= 14"
                    class="rounded bg-purple-50 px-1.5 py-0.5 text-purple-600">儿童</span>
              <!-- 号别：急诊号优先显「急诊号」；其余未知（队列行没挂到挂号 → visitType 为 null）显「未标注」，不许编成初诊/复诊 -->
              <span :class="['rounded px-1.5 py-0.5', queueVisitBadgeOf(row).color]">
                {{ queueVisitBadgeOf(row).label }}
              </span>
              <!-- 批次E/E6：复诊号关联了原病历（挂号表 revisit_record_id），已在既往病历里标出来 -->
              <el-tooltip v-if="row.revisitRecordId" content="本次复诊关联了既往病历（原病历未改动）">
                <span class="rounded bg-amber-50 px-1.5 py-0.5 text-amber-600">关联病历</span>
              </el-tooltip>
              <span v-if="row.settlementType && row.settlementType !== 1"
                    class="rounded bg-blue-50 px-1.5 py-0.5 text-blue-600">医保</span>
              <span v-if="row.hasAllergy" class="rounded bg-red-50 px-1.5 py-0.5 text-red-600">过敏</span>
            </div>
            <div class="mt-1 flex items-center justify-between text-xs text-slate-400">
              <div class="flex items-center gap-2">
                <span v-if="row.arriveTime" class="text-slate-400">
                  <Clock class="inline h-3 w-3"/>
                  {{ formatArriveTime(row.arriveTime) }}到达
                </span>
                <span v-if="row.queueStatus === 3" :class="waitDurationClass(calcWaitMinutes(row))">
                  等待 {{ calcWaitMinutes(row) }}分钟
                </span>
              </div>
              <el-button v-if="row.queueStatus === 2" class="opacity-0 group-hover:opacity-100 transition-opacity" link
                         size="small"
                         type="primary"
                         @click.stop="handleCallSpecific(row)">
                呼叫
              </el-button>
              <!-- 过号：叫了没到、人不在（分诊台同款接口 overdueQueue，仅候诊中可过号，置 6-已过号）。
                   与分诊台互斥由后端守卫：同一患者两边同时操作时 0 行失败并报「状态不允许过号」 -->
              <el-button v-if="row.queueStatus === 2" class="opacity-0 group-hover:opacity-100 transition-opacity"
                         link size="small" type="danger"
                         @click.stop="handleOverdue(row)">
                过号
              </el-button>
              <!-- 回诊：叫下一位时被自动收口（队列 4）但病历并未结诊提交（挂号单不是 4）的行，
                   允许本人重新叫回就诊中 —— 检查结果回来后看第二眼是门诊高频动作 -->
              <el-button v-if="row.queueStatus === 4 && row.registStatus !== 4"
                         class="opacity-0 group-hover:opacity-100 transition-opacity" link size="small"
                         type="warning"
                         @click.stop="handleCallSpecific(row)">
                回诊
              </el-button>
              <!-- 回队：已过号的患者回来了 → 重新排队（后端 mode=2 排到候诊队尾，不插队，
                   避免挤掉正常排队的人；过号标记保留留痕） -->
              <el-button v-if="row.queueStatus === 6" class="opacity-0 group-hover:opacity-100 transition-opacity"
                         link size="small" type="warning"
                         @click.stop="handleRejoin(row)">
                回队
              </el-button>
            </div>
          </div>
          <div v-if="filteredQueue.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">暂无患者
          </div>
        </div>
      </div>
    </div>

    <!-- 主区：患者条 + 病历 + 医嘱面板 + 底部动作条 -->
    <div class="flex min-w-0 flex-1 flex-col gap-3">
      <!-- 无患者状态：说清"是没叫号还是今天根本没人"，并给手动刷新入口（队列 30s 自动刷一次） -->
      <div v-if="!currentPatient" class="flex flex-1 flex-col items-center justify-center text-slate-400">
        <Reading class="mb-4 h-16 w-16 opacity-30"/>
        <p class="text-lg font-medium">未开始叫号</p>
        <p class="mt-2 text-sm">
          {{
            waitingCount > 0
                ? `今日候诊 ${waitingCount} 人，点击「接诊下一位」开始`
                : '今日没有挂到本诊室的患者；新挂号会自动刷进队列，也可手动刷新'
          }}
        </p>
        <div class="mt-4 flex items-center gap-2">
          <el-button size="large" type="primary" @click="handleCallNext">
            <el-icon class="mr-1">
              <VideoPlay/>
            </el-icon>
            接诊下一位
          </el-button>
          <el-button :icon="RefreshRight" size="large" @click="loadData">刷新队列</el-button>
        </div>
      </div>

      <template v-else>
        <!-- 患者条：压成 1 行。身份明细与标签都在「患者详情」弹窗里，条上只留动作按钮 -->
        <PatientBriefBar :arrive-text="arriveText" :detail="patientDetail" :patient="currentPatient"
                         :record="recordForm"
                         @open-charge="showChargeDrawer = true" @open-tags="handleOpenTagDialog"
                         @open-detail="showPatientDetail = true">
        </PatientBriefBar>

        <!-- 医保与收费（紧凑条，替代原 drawer） -->
        <div v-if="currentPatient.queueStatus == 3 && insuranceInfo"
             class="flex shrink-0 items-center gap-4 rounded-md bg-blue-50 px-3 py-1.5 text-xs">
          <span class="text-slate-600">{{ insuranceInfo.medicalInsuranceType || '自费' }}</span>
          <span class="text-slate-600">报销 <b class="text-emerald-600">{{
              insuranceInfo.coverageRatio || 0
            }}%</b></span>
          <span class="text-slate-600">医保支付 ¥{{ (insuranceInfo.insurancePay || 0).toFixed(2) }}</span>
          <span class="ml-auto text-slate-600">自付 <b class="text-red-600">¥{{
              (insuranceInfo.selfPay || 0).toFixed(2)
            }}</b></span>
        </div>

        <div class="relative flex min-h-0 flex-1 gap-3">
          <div class="flex min-h-0 min-w-0 flex-1 flex-col gap-3">
            <div class="flex min-h-0 flex-1 gap-3">
          <div :class="recordColCollapsed ? 'w-9' : 'w-[560px]'"
               class="relative flex min-w-0 shrink-0 flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm transition-all duration-200">
            <button v-if="compactViewport" :class="recordCollapsed ? 'flex-col px-1 py-2' : 'px-3 py-2'"
                    :title="recordCollapsed ? '展开病历' : '收起病历面板'"
                    class="flex w-full shrink-0 cursor-pointer items-center justify-between gap-1 border-b border-slate-200 bg-slate-50 text-left transition-colors hover:bg-slate-100"
                    type="button"
                    @click="recordCollapsed = !recordCollapsed">
              <span :class="recordCollapsed ? '[writing-mode:vertical-rl] text-xs tracking-widest' : ''"
                    class="text-sm font-bold text-slate-700">病历</span>
              <span v-if="!recordCollapsed" class="truncate text-xs text-slate-400">主诉 / 查体 / 诊断</span>
              <el-icon class="shrink-0 text-slate-400">
                <ArrowRight v-if="recordCollapsed"/>
                <ArrowLeft v-else/>
              </el-icon>
            </button>
            <div v-else
                 class="flex w-full shrink-0 items-center justify-between border-b border-slate-200 bg-slate-50 px-3 py-2">
              <span class="text-sm font-bold text-slate-700">病历</span>
              <span class="text-xs text-slate-400">主诉 / 查体 / 诊断</span>
            </div>
            <div v-if="!recordColCollapsed" class="flex min-h-0 flex-1">
              <div class="relative flex w-10 shrink-0 flex-col border-r border-slate-200 bg-slate-50 px-1.5 py-3">
                <span
                    :style="{ transform: `translateY(${activeTabIndex * 68}px)` }"
                    class="pointer-events-none absolute left-1.5 top-3 h-14 w-0.5 bg-[#1269B5] transition-transform duration-300 ease-out"></span>
                <div class="flex flex-col gap-3">
                  <button v-for="(t, i) in recordNavTabs" :key="t.key"
                          :class="activeRecordSection === t.key
                            ? 'z-10 font-bold text-[#1269B5]'
                            : 'z-10 text-slate-500 hover:bg-slate-100 hover:text-[#1269B5]'"
                          :title="t.title"
                          class="relative flex h-14 w-full cursor-pointer items-center justify-center text-xs transition-colors [writing-mode:vertical-rl]" type="button"
                          @click="scrollToRecordSection(t.key)">{{ t.label }}
                  </button>
                </div>
              </div>
              <div ref="recordScrollEl" class="min-h-0 flex-1 overflow-y-auto p-4 custom-scrollbar record-form"
                   @scroll="onRecordScroll">
                <div class="space-y-3">

                  <!-- 第一节：主诉与病史 -->
                  <section ref="chiefSection" class="scroll-mt-3">
                    <div class="mb-2 flex items-center justify-between">
                      <h3 class="text-base font-bold text-slate-800">主诉与病史</h3>
                      <el-button link type="primary" @click="openExtractDialog">
                        智能录入
                      </el-button>
                    </div>
                    <div class="space-y-2">
                      <!-- G-05 预问诊报告卡：患者小程序问卷 + AI 凝摘要，只读参考 -->
                      <div v-if="previsitInfo" class="rounded-md border border-sky-200 bg-sky-50/60 p-3">
                        <div class="mb-2 flex items-center justify-between">
                          <span class="text-sm font-bold text-sky-800">预问诊报告</span>
                          <span class="text-xs text-slate-500">
                          {{ previsitSourceText(previsitInfo.summarySource) }}
                          <template v-if="previsitInfo.createTime"> · {{
                              String(previsitInfo.createTime).slice(0, 16).replace('T', ' ')
                            }}</template>
                        </span>
                        </div>
                        <div class="space-y-1.5 text-sm leading-6 text-slate-800">
                          <p v-if="previsitInfo.summaryAi">
                            <span class="font-medium text-slate-600">AI 摘要：</span>{{ previsitInfo.summaryAi }}
                          </p>
                          <p v-if="previsitInfo.mainSymptom">
                            <span class="font-medium text-slate-600">主症状：</span>{{ previsitInfo.mainSymptom }}
                          </p>
                          <p v-if="previsitInfo.freeText">
                            <span class="font-medium text-slate-600">患者补充：</span>{{ previsitInfo.freeText }}
                          </p>
                        </div>
                      </div>
                      <div>
                        <label class="mb-1 block text-sm font-medium text-slate-600">主诉 <span
                            class="text-red-500">*</span>
                          <span v-if="triageChiefComplaintValue.value"
                                class="ml-1 rounded bg-sky-100 px-1.5 py-0.5 text-xs text-sky-600">分诊主诉</span>
                        </label>
                        <el-input v-model="recordForm.chiefComplaint" :disabled="isRecordSubmitted" :rows="2"
                                  placeholder="请输入主诉" type="textarea"/>
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">现病史 <span
                              class="text-red-500">*</span></label>
                          <div class="flex items-center gap-1">
                            <el-button :loading="draftLoading" link type="primary" @click="runDraft">
                              AI 草拟
                            </el-button>
                            <el-button link type="primary" @click="openVoiceDialog">
                              语音口述
                            </el-button>
                            <el-button v-if="!recordForm.presentIllness" link type="primary"
                                       @click="recordForm.presentIllness = '无'">填写「无」
                            </el-button>
                          </div>
                        </div>
                        <el-input v-model="recordForm.presentIllness" :disabled="isRecordSubmitted" :rows="3"
                                  placeholder="请输入现病史"
                                  type="textarea"/>
                      </div>
                      <div class="rounded-md border border-red-200 bg-red-50/50 p-3">
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-bold text-red-700">⚠ 过敏史 <span
                              class="text-red-500">*</span></label>
                          <div class="flex items-center gap-1">
                            <el-tag v-if="historySources.allergyHistory" size="small" type="info">
                              {{ historySourceLabel('allergyHistory') }}
                            </el-tag>
                            <el-button v-if="historySources.allergyHistory" link type="danger"
                                       @click="clearPrefilled('allergyHistory')">清除
                            </el-button>
                            <el-button v-if="!recordForm.allergyHistory" link type="primary"
                                       @click="recordForm.allergyHistory = '无'">填写「无」
                            </el-button>
                          </div>
                        </div>
                        <el-input v-model="recordForm.allergyHistory" :disabled="isRecordSubmitted" :rows="2"
                                  placeholder="无过敏史请填写 无"
                                  type="textarea"/>
                      </div>
                      <div class="space-y-3">
                        <div>
                          <div class="mb-1 flex items-center justify-between">
                            <label class="text-sm font-medium text-slate-600">既往史</label>
                            <div class="flex items-center gap-1">
                              <el-tag v-if="historySources.pastHistory" size="small" type="info">
                                {{ historySourceLabel('pastHistory') }}
                              </el-tag>
                              <el-button v-if="historySources.pastHistory" link type="danger"
                                         @click="clearPrefilled('pastHistory')">清除
                              </el-button>
                              <el-button v-if="!recordForm.pastHistory" link type="primary"
                                         @click="recordForm.pastHistory = '无'">填写「无」
                              </el-button>
                            </div>
                          </div>
                          <el-input v-model="recordForm.pastHistory" :disabled="isRecordSubmitted" :rows="2"
                                    placeholder="既往病史" type="textarea"/>
                        </div>
                        <div>
                          <div class="mb-1 flex items-center justify-between">
                            <label class="text-sm font-medium text-slate-600">个人史</label>
                            <div class="flex items-center gap-1">
                              <el-tag v-if="historySources.personalHistory" size="small" type="info">
                                {{ historySourceLabel('personalHistory') }}
                              </el-tag>
                              <el-button v-if="historySources.personalHistory" link type="danger"
                                         @click="clearPrefilled('personalHistory')">清除
                              </el-button>
                              <el-button v-if="!recordForm.personalHistory" link type="primary"
                                         @click="recordForm.personalHistory = '无'">填写「无」
                              </el-button>
                            </div>
                          </div>
                          <el-input v-model="recordForm.personalHistory" :rows="2" placeholder="个人史"
                                    type="textarea"/>
                        </div>
                        <div>
                          <div class="mb-1 flex items-center justify-between">
                            <label class="text-sm font-medium text-slate-600">家族史</label>
                            <div class="flex items-center gap-1">
                              <el-tag v-if="historySources.familyHistory" size="small" type="info">
                                {{ historySourceLabel('familyHistory') }}
                              </el-tag>
                              <el-button v-if="historySources.familyHistory" link type="danger"
                                         @click="clearPrefilled('familyHistory')">清除
                              </el-button>
                              <el-button v-if="!recordForm.familyHistory" link type="primary"
                                         @click="recordForm.familyHistory = '无'">填写「无」
                              </el-button>
                            </div>
                          </div>
                          <el-input v-model="recordForm.familyHistory" :rows="2" placeholder="家族史" type="textarea"/>
                        </div>
                        <div>
                          <div class="mb-1 flex items-center justify-between">
                            <label class="text-sm font-medium text-slate-600">婚育史</label>
                            <div class="flex items-center gap-1">
                              <el-tag v-if="historySources.marriageHistory" size="small" type="info">
                                {{ historySourceLabel('marriageHistory') }}
                              </el-tag>
                              <el-button v-if="historySources.marriageHistory" link type="danger"
                                         @click="clearPrefilled('marriageHistory')">清除
                              </el-button>
                              <el-button v-if="!recordForm.marriageHistory" link type="primary"
                                         @click="recordForm.marriageHistory = '无'">填写「无」
                              </el-button>
                            </div>
                          </div>
                          <el-input v-model="recordForm.marriageHistory" :rows="2"
                                    placeholder="婚育史（如：已婚，生育1子1女；无可填「无」）" type="textarea"/>
                        </div>
                      </div>
                    </div>
                  </section>

                  <div class="border-t border-slate-100"></div>

                  <!-- 第二节：体格检查 -->
                  <section ref="examSection" class="scroll-mt-3">
                    <div class="mb-2 text-base font-bold text-slate-800">体格检查</div>
                    <div class="space-y-2">
                      <div class="flex items-center justify-end gap-2">
                        <el-button link size="small" type="primary"
                                   @click="handleExamAllNormal">一键正常
                        </el-button>
                        <el-button :loading="copyExamLoading" link size="small" type="primary"
                                   @click="handleCopyLastExam">复制上次查体
                        </el-button>
                      </div>
                      <div>
                        <!-- 生命体征：label 与输入框分离（单位进 label），label 样式与查体字段一致；
                             分诊带入角标挂在体温旁（分诊带入的第一个必填项） -->
                        <div class="grid grid-cols-2 gap-x-3 gap-y-2">
                          <div>
                            <label class="mb-1 block text-sm font-medium text-slate-600">体温 <span
                                class="text-red-500">*</span>（℃）
                              <span v-if="triagePrefilled"
                                    class="ml-1 rounded bg-sky-100 px-1.5 py-0.5 text-xs text-sky-600">分诊带入</span>
                            </label>
                            <el-input v-model="recordForm.temperature" size="small"/>
                          </div>
                          <div>
                            <label class="mb-1 block text-sm font-medium text-slate-600">脉搏（次/分）</label>
                            <el-input v-model="recordForm.pulse" size="small"/>
                          </div>
                          <div>
                            <label class="mb-1 block text-sm font-medium text-slate-600">呼吸（次/分）</label>
                            <el-input v-model="recordForm.respiration" size="small"/>
                          </div>
                          <div>
                            <label class="mb-1 block text-sm font-medium text-slate-600">收缩压 <span
                                class="text-red-500">*</span>（mmHg）</label>
                            <el-input v-model="recordForm.systolicPressure" size="small"/>
                          </div>
                          <div>
                            <label class="mb-1 block text-sm font-medium text-slate-600">舒张压（mmHg）</label>
                            <el-input v-model="recordForm.diastolicPressure" size="small"/>
                          </div>
                        </div>
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">一般情况</label>
                          <el-button v-if="!recordForm.generalCondition" link size="small" type="primary"
                                     @click="recordForm.generalCondition = '正常'">填写「正常」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.generalCondition" :autosize="{ minRows: 2, maxRows: 6 }"
                                  placeholder="发育、营养、神志等"
                                  type="textarea"
                        />
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">皮肤黏膜</label>
                          <el-button v-if="!recordForm.skinMucosa" link size="small" type="primary"
                                     @click="recordForm.skinMucosa = '正常'">填写「正常」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.skinMucosa" :autosize="{ minRows: 2, maxRows: 6 }"
                                  placeholder="皮肤颜色、湿度等"
                                  type="textarea"
                        />
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">头颈部</label>
                          <el-button v-if="!recordForm.headNeck" link size="small" type="primary"
                                     @click="recordForm.headNeck = '正常'">填写「正常」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.headNeck" :autosize="{ minRows: 2, maxRows: 6 }"
                                  placeholder="头颅、眼、耳等"
                                  type="textarea"
                        />
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">胸肺</label>
                          <el-button v-if="!recordForm.chestLung" link size="small" type="primary"
                                     @click="recordForm.chestLung = '正常'">填写「正常」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.chestLung" :autosize="{ minRows: 2, maxRows: 6 }"
                                  placeholder="胸廓、叩诊、听诊等"
                                  type="textarea"
                        />
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">心脏</label>
                          <el-button v-if="!recordForm.heart" link size="small" type="primary"
                                     @click="recordForm.heart = '正常'">填写「正常」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.heart" :autosize="{ minRows: 2, maxRows: 6 }"
                                  placeholder="心率、心律等"
                                  type="textarea"
                        />
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">腹部</label>
                          <el-button v-if="!recordForm.abdomen" link size="small" type="primary"
                                     @click="recordForm.abdomen = '正常'">填写「正常」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.abdomen" :autosize="{ minRows: 2, maxRows: 6 }"
                                  placeholder="压痛、肝脾等"
                                  type="textarea"
                        />
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">脊柱四肢</label>
                          <el-button v-if="!recordForm.spineLimbs" link size="small" type="primary"
                                     @click="recordForm.spineLimbs = '正常'">填写「正常」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.spineLimbs" :autosize="{ minRows: 2, maxRows: 6 }"
                                  placeholder="脊柱、关节等"
                                  type="textarea"
                        />
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">神经系统</label>
                          <el-button v-if="!recordForm.nervousSystem" link size="small" type="primary"
                                     @click="recordForm.nervousSystem = '正常'">填写「正常」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.nervousSystem" :autosize="{ minRows: 2, maxRows: 6 }"
                                  placeholder="生理反射等"
                                  type="textarea"
                        />
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">专科检查</label>
                          <el-button v-if="!recordForm.specialistExam" link size="small" type="primary"
                                     @click="recordForm.specialistExam = '正常'">填写「正常」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.specialistExam" :autosize="{ minRows: 2, maxRows: 6 }"
                                  placeholder="专科检查所见"
                                  type="textarea"
                        />
                      </div>
                    </div>
                  </section>

                  <div class="border-t border-slate-100"></div>

                  <!-- 第三节：诊断与治疗 -->
                  <section ref="diagSection" class="scroll-mt-3">
                    <div class="mb-2 text-base font-bold text-slate-800">诊断与治疗</div>
                    <div class="space-y-2">
                      <div><label class="mb-1 block text-sm font-medium text-slate-600">诊断 <span
                          class="text-red-500">*</span></label>
                        <el-select v-model="recordForm.diagnosis" :disabled="isRecordSubmitted" :loading="icd10Loading"
                                   :remote-method="handleIcd10Search" class="w-full"
                                   filterable placeholder="输入疾病名称或编码搜索"
                                   remote reserve-keyword
                                   @change="handleIcd10Select">
                          <el-option v-for="item in icd10Results" :key="item.id"
                                     :label="`${item.icdCode} - ${item.icdName}`" :value="item.icdName">
                            <div class="flex items-center justify-between"><span
                                class="font-mono text-sm text-blue-600">{{
                                item.icdCode
                              }}</span><span class="text-sm text-slate-700">{{ item.icdName }}</span></div>
                            <div class="text-xs text-slate-400">{{ item.icdCategory }}</div>
                          </el-option>
                        </el-select>
                        <!-- 常用诊断快捷选择 -->
                        <div v-if="myDiagTemplates.length > 0" class="mt-2 flex flex-wrap gap-1.5">
                          <span class="text-[10px] text-slate-400 leading-5">常用：</span>
                          <span v-for="tpl in myDiagTemplates" :key="tpl.icdCode"
                                class="cursor-pointer rounded bg-blue-50 px-1.5 py-0.5 text-[11px] text-blue-600 transition-colors hover:bg-blue-100"
                                @click="handleQuickSelectDiag(tpl)">
                        {{ tpl.icdName }}
                      </span>
                        </div>
                        <div v-if="recordForm.diagnosisCode"
                             class="mt-1 flex items-center gap-2 text-xs text-slate-400">
                          <span>编码：{{ recordForm.diagnosisCode }}</span>
                          <el-button link size="small" type="primary" @click="handleAddDiagToTemplate">
                            <el-icon class="mr-0.5">
                              <Plus/>
                            </el-icon>
                            添加到常用
                          </el-button>
                        </div>
                        <el-button :loading="icdPredictionLoading" class="mt-1" link size="small" type="primary"
                                   @click="handlePredictIcd">
                          <el-icon class="mr-0.5">
                            <MagicStick/>
                          </el-icon>
                          智能预测
                        </el-button>
                      </div>

                      <!-- ICD-10预测结果面板 -->
                      <div v-if="showPrediction" class="rounded-lg border border-blue-200 bg-blue-50 p-2.5">
                        <div class="mb-1.5 flex items-center justify-between">
                      <span class="text-xs font-bold text-blue-700">
                        <el-icon class="mr-0.5"><MagicStick/></el-icon>预测结果
                      </span>
                          <el-button link size="small" type="primary" @click="showPrediction = false">收起</el-button>
                        </div>
                        <div v-if="icdPredictionLoading" class="py-3 text-center text-xs text-blue-500">分析中...</div>
                        <div v-else-if="icdPredictions.length === 0" class="py-3 text-center text-xs text-slate-400">
                          未找到匹配编码
                        </div>
                        <div v-else class="max-h-48 space-y-1.5 overflow-y-auto">
                          <div v-for="(item, idx) in icdPredictions" :key="item.icdCode"
                               class="cursor-pointer rounded border border-slate-200 bg-white p-2 transition-colors hover:border-blue-300"
                               @click="handleSelectPrediction(item)">
                            <div class="flex items-center justify-between">
                              <div class="flex items-center gap-1.5">
                            <span class="rounded bg-blue-100 px-1 py-0.5 text-[10px] font-bold text-blue-700">#{{
                                idx + 1
                              }}</span>
                                <span class="font-mono text-xs font-medium text-blue-600">{{ item.icdCode }}</span>
                                <span class="text-xs font-medium text-slate-900">{{ item.icdName }}</span>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>

                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">辅助检查</label>
                          <el-button v-if="!recordForm.auxiliaryExam" link size="small" type="primary"
                                     @click="recordForm.auxiliaryExam = '暂无'">填写「暂无」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.auxiliaryExam" :autosize="{ minRows: 2, maxRows: 6 }"
                                  placeholder="实验室检查、影像学检查结果等"
                                  type="textarea"/>
                      </div>
                      <div>
                        <div class="mb-1 flex items-center justify-between">
                          <label class="text-sm font-medium text-slate-600">治疗方案</label>
                          <el-button v-if="!recordForm.treatmentPlan" link size="small" type="primary"
                                     @click="recordForm.treatmentPlan = '遵医嘱'">填写「遵医嘱」
                          </el-button>
                        </div>
                        <el-input v-model="recordForm.treatmentPlan" :autosize="{ minRows: 3, maxRows: 8 }"
                                  placeholder="治疗方案、用药建议等"
                                  type="textarea"/>
                      </div>
                    </div>
                  </section>
                </div>
              </div>
            </div>

            <!-- 结诊提交中全屏遮罩 -->
            <div v-if="submitting" class="fixed inset-0 z-[100] flex items-center justify-center bg-white/70">
              <div class="text-center">
                <div class="text-lg font-bold text-blue-600 mb-2">正在结诊提交...</div>
              </div>
            </div>
          </div>

          <!-- 病历已提交水印：覆盖整个工作区（病历栏 + 处方/检查/检验右栏），absolute 随主容器定位，
               左上→中央→右下斜向三枚。病历栏折叠时仍覆盖右栏，不按 recordColCollapsed 隐藏。 -->
          <div v-if="recordForm.recordStatus === 2"
               class="pointer-events-none absolute inset-0 z-50 overflow-hidden">
            <div
                class="absolute left-[8%] top-[10%] rotate-[-30deg] whitespace-nowrap text-4xl font-black text-red-500/40 select-none">
              病历已提交
            </div>
            <div
                class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 rotate-[-30deg] whitespace-nowrap text-4xl font-black text-red-500/40 select-none">
              病历已提交
            </div>
            <div
                class="absolute bottom-[10%] right-[8%] rotate-[-30deg] whitespace-nowrap text-4xl font-black text-red-500/40 select-none">
              病历已提交
            </div>
          </div>
          <!-- 已审核水印 -->
          <div v-if="recordForm.reviewStatus === 2"
               class="pointer-events-none absolute inset-0 z-50 flex items-center justify-center">
            <div class="rotate-[-30deg] text-4xl font-black text-emerald-500/20 select-none">
              已审核
            </div>
          </div>

          <div class="flex min-h-0 min-w-0 flex-1 flex-col gap-3 pb-3">
            <OrderPanel :count-text="prescriptionList.length + ' 张'" class="flex-[1.3] min-h-0" fill noCollapse
                        title="处方">
              <div class="flex min-h-0 flex-1 flex-col">
                <!-- 三个处方类型 tab -->
                <el-tabs v-model="currentPrescriptionType" class="!mb-0">
                  <el-tab-pane :name="1" label="西药"/>
                  <el-tab-pane :name="2" label="中成药"/>
                  <el-tab-pane :name="3" label="中药饮片"/>
                </el-tabs>
                <div v-if="currentPatient.queueStatus === 3 && currentPrescriptionType === 3"
                     class="mb-2 mt-2 flex items-center justify-end gap-2 rounded-md bg-slate-50 px-3 py-2">
                  <label class="text-xs text-slate-600" data-testid="ws-dose-label">剂数<span
                      class="text-red-500">*</span></label>
                  <el-input-number v-model="prescriptionForm.doseCount" :max="30" :min="1"
                                   controls-position="right" data-testid="ws-dose-count" size="small"
                                   style="width: 100px"/>
                  <label class="text-xs text-slate-600">煎服<span class="text-red-500">*</span></label>
                  <el-radio-group v-model="prescriptionForm.decoctFlag" size="small">
                    <el-radio v-for="d in tcmDecoctFlagDict" :key="d.dictValue"
                              :data-testid="`ws-decoct-${d.dictValue}`" :value="Number(d.dictValue)">
                      {{ d.dictLabel }}
                    </el-radio>
                  </el-radio-group>
                </div>
                <div v-if="currentPatient.queueStatus === 3 && currentPrescriptionType !== 3"
                     class="mb-2 mt-2 flex items-center justify-end gap-2 rounded-md bg-slate-50 px-3 py-2">
                  <label class="text-xs text-slate-600" data-testid="ws-longrx-label">慢病长处方</label>
                  <el-switch v-model="prescriptionForm.isLongPrescription" :disabled="!chronicQualified"
                             data-testid="ws-longrx-switch" size="small"/>
                  <template v-if="prescriptionForm.isLongPrescription">
                    <label class="text-xs text-slate-600">天数<span class="text-red-500">*</span></label>
                    <el-input-number v-model="prescriptionForm.longPrescriptionDays" :max="90" :min="1"
                                     controls-position="right" data-testid="ws-longrx-days" size="small"
                                     style="width: 100px"/>
                    <span class="text-[10px] text-slate-400">1~90 天，限长期用药慢病患者</span>
                  </template>
                  <span v-else-if="chronicQualified" class="text-[10px] text-slate-400">
                      已认定：{{ chronicDiseaseText }}
                    </span>
                  <span v-else class="text-[10px] text-amber-600" data-testid="ws-longrx-nochronic">
                      该患者无已认定的慢病档案，不能开慢病长处方
                    </span>
                </div>
                <div class="flex min-h-0 flex-1 flex-col space-y-2 py-1">
                  <!-- 处方明细 -->
                  <div class="flex min-h-0 flex-1 flex-col space-y-2 overflow-y-auto">
                    <!-- 可编辑列表 (queueStatus=3 结诊中)：标准 HIS 处方明细 = 可编辑表格 -->
                    <template v-if="currentPatient.queueStatus === 3">
                      <!-- 表格顶部工具行：左=常用药品快捷 chips（点击=追加模板首味药为新行），右=新增药品（挨着 table，留一点间隔） -->
                      <div class="mb-1.5 flex items-center justify-between gap-2">
                        <div class="flex flex-wrap items-center gap-1">
                            <span v-if="filteredRxTemplates.length > 0"
                                  class="text-[10px] font-medium text-slate-500">常用药品：</span>
                          <span v-for="tpl in filteredRxTemplates.slice(0, 5)" :key="tpl.id"
                                class="cursor-pointer rounded bg-blue-50 px-1.5 py-0.5 text-[11px] text-blue-600 transition-colors hover:bg-blue-100"
                                @click="handleAppendRxTemplate(tpl)">
                              {{ tpl.templateName }}
                            </span>
                        </div>
                        <el-button v-if="!isRecordSubmitted" v-perm="'opd:doctorWorkstation:add'" :icon="Plus"
                                   size="small" type="primary"
                                   @click="handleAddEmptyDrug">新增药品
                        </el-button>
                      </div>
                      <div class="min-h-0 flex-1">
                        <el-table :data="prescriptionForm.details" border height="100%" size="small">
                          <!-- 药品：行内搜索下拉（三类共用，饮片搜索中药饮片），必填 -->
                          <el-table-column min-width="190">
                            <template #header>
                              <span class="text-slate-700">药品名称<span class="text-red-500">*</span></span>
                            </template>
                            <template #default="{ row }">
                              <el-select v-model="row.drugId" :loading="drugLoading" :placeholder="currentPrescriptionType === 3 ? '搜索中药饮片' : '搜索药品'"
                                         data-testid="ws-herb-drug" filterable
                                         size="small"
                                         style="width: 100%"
                                         @change="(val: any) => handleDrugSelectForRecord(row, val)">
                                <el-option v-for="drug in drugResults" :key="drug.id"
                                           :label="currentPrescriptionType === 3 ? drug.drugName : `${drug.drugName} (${drug.specification})`"
                                           :value="drug.id">
                                  <div class="flex items-center justify-between">
                                    <span class="text-sm font-medium text-slate-900">{{ drug.drugName }}</span>
                                    <span class="text-xs text-slate-400">{{
                                        currentPrescriptionType === 3 ? `¥${drug.retailPrice}/${drug.unit}` : (drug.dosageForm || drug.specification)
                                      }}</span>
                                  </div>
                                  <div class="text-xs text-slate-400">
                                    {{ drug.specification }} | ¥{{ drug.retailPrice }}/{{ drug.unit }}
                                    <template v-if="currentPrescriptionType === 3 && drug.gramPerUnit">
                                      ≈ ¥{{ tcmPerGramPrice(drug).toFixed(3) }}/g
                                    </template>
                                  </div>
                                </el-option>
                              </el-select>
                            </template>
                          </el-table-column>
                          <!-- 单次用量 / 每剂克数（必填） -->
                          <el-table-column width="120">
                            <template #header>
                              <span class="text-slate-700">{{
                                  currentPrescriptionType === 3 ? '每剂克数(g)' : '单次用量'
                                }}<span class="text-red-500">*</span></span>
                            </template>
                            <template #default="{ row }">
                              <el-input v-model="row.singleDosage" :placeholder="currentPrescriptionType === 3 ? '如: 15' : '用量'"
                                        data-testid="ws-herb-grams"
                                        size="small" @change="applyTcmGrams(row)"/>
                            </template>
                          </el-table-column>
                          <!-- 饮片：实发克数 + 金额（只读，每剂克数×剂数自动算） -->
                          <el-table-column v-if="currentPrescriptionType === 3" label="实发" width="110">
                            <template #default="{ row }">
                              <span class="text-xs font-medium text-slate-800"
                                    data-testid="ws-herb-total">{{ row.quantity || 0 }} g</span>
                              <span class="ml-1 text-xs text-slate-400"
                                    data-testid="ws-herb-amount">¥{{ Number(row.amount || 0).toFixed(2) }}</span>
                            </template>
                          </el-table-column>
                          <!-- 频次 / 用法（必填） -->
                          <el-table-column width="110">
                            <template #header>
                              <span class="text-slate-700">{{ currentPrescriptionType === 3 ? '用法' : '频次' }}<span
                                  class="text-red-500">*</span></span>
                            </template>
                            <template #default="{ row }">
                              <el-select v-model="row.frequency"
                                         :placeholder="currentPrescriptionType === 3 ? '用法' : '频次'" size="small"
                                         style="width: 100%">
                                <el-option v-if="currentPrescriptionType === 3" label="每日一剂" value="每日一剂"/>
                                <el-option v-if="currentPrescriptionType === 3" label="每日两剂" value="每日两剂"/>
                                <el-option v-if="currentPrescriptionType === 3" label="隔日一剂" value="隔日一剂"/>
                                <el-option v-if="currentPrescriptionType !== 3" label="一日一次" value="一日一次"/>
                                <el-option v-if="currentPrescriptionType !== 3" label="一日两次" value="一日两次"/>
                                <el-option v-if="currentPrescriptionType !== 3" label="一日三次" value="一日三次"/>
                                <el-option v-if="currentPrescriptionType === 1" label="需要时" value="需要时"/>
                              </el-select>
                            </template>
                          </el-table-column>
                          <!-- 途径 / 煎法（必填） -->
                          <el-table-column width="120">
                            <template #header>
                              <span class="text-slate-700">{{
                                  currentPrescriptionType === 3 ? '煎法' : (currentPrescriptionType === 2 ? '用法' : '途径')
                                }}<span class="text-red-500">*</span></span>
                            </template>
                            <template #default="{ row }">
                              <el-select v-model="row.route" :placeholder="currentPrescriptionType === 3 ? '煎法' : '途径'"
                                         data-testid="ws-herb-route" size="small"
                                         style="width: 100%">
                                <el-option v-for="d in tcmMethodDict" v-if="currentPrescriptionType === 3"
                                           :key="d.dictValue" :label="d.dictLabel" :value="d.dictValue"/>
                                <el-option v-if="currentPrescriptionType !== 3 && currentPrescriptionType !== 2"
                                           label="口服" value="口服"/>
                                <el-option v-if="currentPrescriptionType !== 3 && currentPrescriptionType !== 2"
                                           label="静脉注射" value="静脉注射"/>
                                <el-option v-if="currentPrescriptionType !== 3 && currentPrescriptionType !== 2"
                                           label="肌肉注射" value="肌肉注射"/>
                                <el-option v-if="currentPrescriptionType !== 3 && currentPrescriptionType !== 2"
                                           label="外用" value="外用"/>
                                <el-option v-if="currentPrescriptionType === 2" label="口服" value="口服"/>
                                <el-option v-if="currentPrescriptionType === 2" label="含服" value="含服"/>
                                <el-option v-if="currentPrescriptionType === 2" label="外用" value="外用"/>
                                <el-option v-if="currentPrescriptionType === 2" label="噎服" value="噎服"/>
                              </el-select>
                            </template>
                          </el-table-column>
                          <!-- 总量 + 天数（西药/中成药，必填） -->
                          <el-table-column v-if="currentPrescriptionType !== 3" width="100">
                            <template #header>
                              <span class="text-slate-700">总量<span class="text-red-500">*</span></span>
                            </template>
                            <template #default="{ row }">
                              <el-input-number v-model="row.quantity" :min="1" controls-position="right" size="small"
                                               style="width: 100%"/>
                            </template>
                          </el-table-column>
                          <el-table-column v-if="currentPrescriptionType !== 3" width="90">
                            <template #header>
                              <span class="text-slate-700">天数<span class="text-red-500">*</span></span>
                            </template>
                            <template #default="{ row }">
                              <el-input-number v-model="row.duration" :max="90" :min="1" controls-position="right"
                                               size="small" style="width: 100%"/>
                            </template>
                          </el-table-column>
                          <!-- 饮片脚注（标准 HIS：每味药特殊煎法，先煎/后下/包煎/烍化/冲服/另煎/兑服） -->
                          <el-table-column v-if="currentPrescriptionType === 3" label="脚注" width="110">
                            <template #default="{ row }">
                              <el-select v-model="row.remark" clearable placeholder="无" size="small"
                                         style="width: 100%">
                                <el-option v-for="f in tcmFootnoteOptions" :key="f" :label="f" :value="f"/>
                              </el-select>
                            </template>
                          </el-table-column>
                          <!-- 单价 / 金额（三类处方都显示：西药/中成药按盒，饮片按克（price=元/克），金额=price×实发克数） -->
                          <el-table-column label="单价" width="90">
                            <template #default="{ row }">
                              <span class="text-xs text-slate-500">¥{{
                                  Number(row.price || 0).toFixed(2)
                                }}/{{ row.unit || '盒' }}</span>
                            </template>
                          </el-table-column>
                          <el-table-column align="right" label="金额" width="90">
                            <template #default="{ row }">
                              <span class="text-xs font-medium text-slate-800">¥{{
                                  (Number(row.price || 0) * Number(row.quantity || 0)).toFixed(2)
                                }}</span>
                            </template>
                          </el-table-column>
                          <!-- 删除 -->
                          <el-table-column align="center" label="" width="64">
                            <template #default="{ $index }">
                              <el-button v-perm="'opd:doctorWorkstation:delete'" link size="small" type="danger"
                                         @click="handleRemoveDrug($index)">删除
                              </el-button>
                            </template>
                          </el-table-column>
                        </el-table>
                      </div>
                      <!-- 表尾合计（标准 HIS）：饮片=共N味·总克数·金额；西药/中成药=共N项·金额 -->
                      <div v-if="prescriptionForm.details.length > 0"
                           class="mt-2 flex items-center justify-end gap-5 text-xs text-slate-500">
                        <template v-if="currentPrescriptionType === 3">
                            <span>共 <b class="text-slate-800">{{ prescriptionForm.details.length }}</b> 味 ·
                              总克数 <b class="text-slate-800">{{ totalTcmGrams.toFixed(0) }}g</b>
                              （每剂克数 × {{ prescriptionForm.doseCount }} 剂）</span>
                          <span>金额 <b class="text-[#1269B5]">¥{{ totalTcmAmount.toFixed(2) }}</b></span>
                        </template>
                        <template v-else>
                          <span>共 <b class="text-slate-800">{{ prescriptionForm.details.length }}</b> 项药品</span>
                          <span>金额 <b class="text-[#1269B5]">¥{{
                              rxAmountOfType(currentPrescriptionType).toFixed(2)
                            }}</b></span>
                        </template>
                      </div>
                    </template>
                    <!-- 纯列表 (非结诊中) -->
                    <template v-else>
                      <div v-for="p in prescriptionList.filter(p => p.prescriptionType === currentPrescriptionType)"
                           :key="p.id || p.prescriptionType">
                        <div v-if="p.details && p.details.length > 0" class="mb-3">
                          <div class="mb-2 flex items-center justify-between text-xs text-slate-500">
                              <span>
                                {{ prescriptionTypeLabel(p.prescriptionType) }}处方 · {{ p.details.length }}种药品
                                <template v-if="p.prescriptionType === 3 && p.doseCount"> · {{
                                    p.doseCount
                                  }}剂</template>
                                <template v-if="p.isLongPrescription === 1"> · 长处方{{
                                    p.longPrescriptionDays
                                  }}天</template>
                              </span>
                            <el-tooltip v-if="p.doctorSignId" :content="`开方医师 ${p.doctorName} · ${p.doctorSignedTime ? String(p.doctorSignedTime).slice(0, 16).replace('T', ' ') : ''}，签名即锁定`"
                                        effect="plain">
                              <el-tag effect="plain" size="small" type="success">已电子签名·{{ p.doctorName }}</el-tag>
                            </el-tooltip>
                            <el-tag v-else effect="plain" size="small" type="info">未签名（结诊时签署）</el-tag>
                          </div>
                          <!-- 医嘱栏对调成 flex-1 主工作区后（2026-09-26 方案A），表格按真实 HIS 处方全列铺开：
                               药名/规格/厂家/单价/金额是医生开完方核对与患者费用解释要看的东西，
                               原先 480px 装不下才砍掉。min-width 让列随栏宽弹性摊开，不留大片空白 -->
                          <el-table :data="p.details" border size="small">
                            <el-table-column label="药品名称" min-width="150" prop="drugName" show-overflow-tooltip/>
                            <el-table-column label="规格" min-width="110" prop="specification" show-overflow-tooltip/>
                            <el-table-column label="生产厂家" min-width="150" prop="manufacturer"
                                             show-overflow-tooltip/>
                            <el-table-column align="center" label="数量" prop="quantity" width="70"/>
                            <el-table-column align="center" label="单位" prop="unit" width="56"/>
                            <el-table-column align="right" label="单价" width="80">
                              <template #default="{ row }">{{ Number(row.price || 0).toFixed(2) }}</template>
                            </el-table-column>
                            <el-table-column align="right" label="金额" width="86">
                              <template #default="{ row }">
                                  <span class="font-medium text-slate-800">{{
                                      Number(row.amount || 0).toFixed(2)
                                    }}</span>
                              </template>
                            </el-table-column>
                            <el-table-column label="单次用量" prop="singleDosage" width="86"/>
                            <el-table-column label="频次" prop="frequency" width="86"/>
                            <el-table-column label="途径" prop="route" width="70"/>
                            <el-table-column align="center" label="天数" prop="duration" width="56"/>
                          </el-table>
                        </div>
                      </div>
                      <div
                          v-if="prescriptionList.filter(p => p.prescriptionType === currentPrescriptionType).length === 0 || prescriptionList.filter(p => p.prescriptionType === currentPrescriptionType).every(p => !p.details || p.details.length === 0)"
                          class="py-8 text-center text-sm text-slate-400">
                        {{
                          patientDataLoading ? '处方加载中…' : `暂无${prescriptionTypeLabel(currentPrescriptionType)}处方`
                        }}
                      </div>
                    </template>
                  </div>
                </div>
              </div>
            </OrderPanel>
            <OrderPanel :count-text="inspectionRecords.length + ' 项'" class="flex-[0.85] min-h-0" fill noCollapse
                        title="检查申请">
              <div class="flex min-h-0 flex-1 flex-col">
                <div class="flex min-h-0 flex-1 flex-col space-y-2 py-1">
                  <!-- 检查申请列表：结诊中=可编辑表格（工具行+新增按钮常显，空列表时表格显示空态）；
                       非结诊=只读卡片列表 -->
                  <div class="flex min-h-0 flex-1 flex-col">
                    <div class="flex min-h-0 flex-1 flex-col space-y-2 overflow-y-auto">
                      <!-- 可编辑列表 (queueStatus=3 就诊中)：标准 HIS 检查申请 = 表格列表 -->
                      <template v-if="currentPatient.queueStatus === 3">
                        <!-- 表格顶部工具行：左=常用检查 chips（点击=追加模板为新行），右=新增检查（挨着 table，留一点间隔） -->
                        <div class="mb-1.5 flex items-center justify-between gap-2">
                          <div class="flex flex-wrap items-center gap-1">
                            <span v-if="myInspectionTemplates.length > 0"
                                  class="text-[10px] font-medium text-slate-500">常用检查：</span>
                            <el-button v-for="tpl in myInspectionTemplates" :key="tpl.id" link size="small"
                                       type="primary" @click="handleAppendInspectionTemplate(tpl)">
                              {{ tpl.templateName }}
                            </el-button>
                          </div>
                          <el-button v-if="!isRecordSubmitted" v-perm="'opd:doctorWorkstation:add'" :icon="Plus"
                                     size="small" type="primary"
                                     @click="handleAddEmptyInspection">新增检查
                          </el-button>
                        </div>
                        <div class="min-h-0 flex-1">
                          <el-table :data="inspectionRecords" border empty-text="暂无检查申请，点击上方「新增检查」"
                                    height="100%" size="small">
                            <el-table-column min-width="190">
                              <template #header>
                                <span class="text-slate-700">检查项目<span class="text-red-500">*</span></span>
                              </template>
                              <template #default="{ row }">
                                <el-select v-model="row.inspectionItemId" :disabled="!applyRowEditable(row)"
                                           :loading="inspectionItemLoading" filterable placeholder="搜索检查项目"
                                           size="small" style="width: 100%"
                                           @change="(val: any) => handleInspectionItemSelectForRecord(row, val)">
                                  <el-option v-for="opt in inspectionItemResults" :key="opt.id"
                                             :label="`${opt.itemCode} - ${opt.itemName}`" :value="opt.id">
                                    <div class="flex items-center justify-between">
                                      <span class="text-sm font-medium text-slate-900">{{ opt.itemName }}</span>
                                      <span class="text-xs text-slate-400">¥{{ opt.price }}</span>
                                    </div>
                                    <div class="text-xs text-slate-400">{{ opt.itemCode }} · {{ opt.bodyPart }}</div>
                                  </el-option>
                                </el-select>
                              </template>
                            </el-table-column>
                            <el-table-column label="部位" width="130">
                              <template #default="{ row }">
                                <el-input v-model="row.bodyPart" :disabled="!applyRowEditable(row)"
                                          placeholder="检查部位" size="small"/>
                              </template>
                            </el-table-column>
                            <el-table-column label="注意事项" min-width="150">
                              <template #default="{ row }">
                                <el-input v-model="row.preparation" :disabled="!applyRowEditable(row)"
                                          placeholder="注意事项（可选）" size="small"/>
                              </template>
                            </el-table-column>
                            <el-table-column label="检查目的" min-width="150">
                              <template #default="{ row }">
                                <el-input v-model="row.inspectionPurpose" :disabled="!applyRowEditable(row)"
                                          placeholder="检查目的（可选）" size="small"/>
                              </template>
                            </el-table-column>
                            <el-table-column label="状态" min-width="150">
                              <template #default="{ row }">
                                <el-tag v-if="row.id" :type="applyTagType(row)" size="small">{{
                                    row.execStatusText
                                  }}
                                </el-tag>
                                <el-tag v-else size="small" type="info">保存病历时开单</el-tag>
                                <el-tag v-if="row.critical" effect="dark" size="small" type="danger">危急值</el-tag>
                                <el-tooltip v-if="row.signStatus === 1"
                                            :content="`开单医师 ${row.doctorName} · ${row.signedTime || ''}，签名即锁定`">
                                  <el-tag data-testid="p5-ins-sign-tag" effect="plain" size="small" type="success">
                                    已电子签名
                                  </el-tag>
                                </el-tooltip>
                                <el-tag v-else-if="row.signStatus === 2" effect="plain" size="small" type="warning">
                                  签名已作废
                                </el-tag>
                              </template>
                            </el-table-column>
                            <el-table-column align="center" label="" width="64">
                              <template #default="{ $index }">
                                <el-button v-perm="'opd:doctorWorkstation:delete'" link size="small" type="danger"
                                           @click="handleDeleteInspectionRecord($index)">删除
                                </el-button>
                              </template>
                            </el-table-column>
                          </el-table>
                        </div>
                        <!-- 检查费表尾合计（标准 HIS 表尾） -->
                        <div v-if="inspectionRecords.length > 0"
                             class="mt-2 flex items-center justify-end text-xs text-slate-500">
                          <span>共 <b class="text-slate-800">{{ inspectionRecords.length }}</b> 项 · 检查费
                            <b class="text-[#1269B5]">¥{{ insTotalAmount.toFixed(2) }}</b>
                          </span>
                        </div>
                      </template>
                      <!-- 纯列表 (非就诊中) -->
                      <template v-else>
                        <div v-for="(item, idx) in inspectionRecords"
                             :key="item.id || ('pending-inspection-' + item._pendingIdx)"
                             :class="[
                             'rounded-lg border p-3',
                             item.critical
                                 ? 'border-red-300 bg-red-50'
                                 : (item.execStatusText === '待缴费' ? 'border-amber-200 bg-amber-50' : 'border-slate-200')
                           ]">
                          <div class="flex items-center justify-between">
                            <div class="flex items-center gap-2">
                              <span class="text-sm font-medium text-slate-900">{{ item.inspectionItemName }}</span>
                              <el-tag :type="applyTagType(item)" size="small">{{ item.execStatusText }}</el-tag>
                              <el-tag v-if="item.critical" effect="dark" size="small" type="danger">危急值</el-tag>
                              <el-tag v-if="item.isEmergency === 1" size="small" type="warning">急诊</el-tag>
                              <el-tag v-if="item.signStatus === 1" effect="plain" size="small" type="success">
                                已电子签名·{{ item.doctorName }}
                              </el-tag>
                              <el-tag v-else-if="item.signStatus === 2" effect="plain" size="small" type="warning">
                                签名已作废
                              </el-tag>
                            </div>
                            <div class="flex items-center gap-2">
                              <span class="text-xs text-slate-400">{{ item.createTime }}</span>
                              <el-button v-if="item.execRecordId" link size="small" type="primary"
                                         @click="handleViewInspectionReport(item)">
                                查看报告
                              </el-button>
                              <!-- 批次E/E6：结果已回 → 一键建复诊（关联本次病历，免门诊诊查费） -->
                              <el-button v-if="canCreateRevisit(item)" link size="small" type="warning"
                                         @click="handleCreateRevisit(item)">
                                建复诊
                              </el-button>
                              <el-button v-if="item.canDelete" v-perm="'opd:doctorWorkstation:delete'" link
                                         size="small" type="danger"
                                         @click="handleDeleteInspectionRecord(idx)">
                                删除
                              </el-button>
                              <el-tooltip v-else-if="item.deleteBlockReason" :content="item.deleteBlockReason">
                                <span class="cursor-help text-xs text-slate-300">删除</span>
                              </el-tooltip>
                            </div>
                          </div>
                          <div v-if="item.bodyPart" class="mt-1 text-xs text-slate-500">部位：{{ item.bodyPart }}</div>
                          <div v-if="item.inspectionPurpose" class="mt-1 text-xs text-slate-500">
                            目的：{{ item.inspectionPurpose }}
                          </div>
                          <div v-if="item.specialRequirements" class="mt-1 text-xs text-slate-500">
                            注意事项：{{ item.specialRequirements }}
                          </div>
                          <div v-if="item.critical" class="mt-2 rounded bg-red-100 p-2 text-xs text-red-700">
                            该检查/相关检验出现危急值，请优先处理并及时通知患者。
                          </div>
                        </div>
                        <!-- 批次E/F：只读态下没有申请时给出明确空态，避免「没开」和「没加载出来」混淆 -->
                        <div v-if="inspectionRecords.length === 0"
                             class="py-8 text-center text-sm text-slate-400">
                          {{ patientDataLoading ? '检查申请加载中…' : '本次就诊还没有检查申请' }}
                        </div>
                      </template>
                    </div>
                  </div>
                </div>
              </div>
            </OrderPanel>
            <OrderPanel :count-text="laboratoryRecords.length + ' 项'" class="flex-[0.85] min-h-0" fill noCollapse
                        title="检验申请">
              <div class="flex min-h-0 flex-1 flex-col">
                <div class="flex min-h-0 flex-1 flex-col space-y-2 py-1">
                  <!-- 检验申请列表：结诊中=可编辑表格（工具行+新增按钮常显，空列表时表格显示空态）；
                       非结诊=只读卡片列表 -->
                  <div class="flex min-h-0 flex-1 flex-col">
                    <div class="flex min-h-0 flex-1 flex-col space-y-2 overflow-y-auto">
                      <!-- 可编辑列表 (queueStatus=3 结诊中)：标准 HIS 检验申请 = 表格列表 -->
                      <template v-if="currentPatient.queueStatus === 3">
                        <!-- 表格顶部工具行：左=常用检验 chips（点击=追加模板为新行），右=新增检验（挨着 table，留一点间隔） -->
                        <div class="mb-1.5 flex items-center justify-between gap-2">
                          <div class="flex flex-wrap items-center gap-1">
                            <span v-if="myLaboratoryTemplates.length > 0"
                                  class="text-[10px] font-medium text-slate-500">常用检验：</span>
                            <el-button v-for="tpl in myLaboratoryTemplates" :key="tpl.id" link size="small"
                                       type="primary" @click="handleAppendLaboratoryTemplate(tpl)">
                              {{ tpl.templateName }}
                            </el-button>
                          </div>
                          <el-button v-if="!isRecordSubmitted" v-perm="'opd:doctorWorkstation:add'" :icon="Plus"
                                     size="small" type="primary"
                                     @click="handleAddEmptyLaboratory">新增检验
                          </el-button>
                        </div>
                        <div class="min-h-0 flex-1">
                          <el-table :data="laboratoryRecords" border empty-text="暂无检验申请，点击上方「新增检验」"
                                    height="100%" size="small">
                            <el-table-column min-width="190">
                              <template #header>
                                <span class="text-slate-700">检验项目<span class="text-red-500">*</span></span>
                              </template>
                              <template #default="{ row }">
                                <el-select v-model="row.laboratoryItemId" :disabled="!applyRowEditable(row)"
                                           :loading="laboratoryItemLoading" filterable placeholder="搜索检验项目"
                                           size="small" style="width: 100%"
                                           @change="(val: any) => handleLaboratoryItemSelectForRecord(row, val)">
                                  <el-option v-for="opt in laboratoryItemResults" :key="opt.id"
                                             :label="`${opt.itemCode} - ${opt.itemName}`" :value="opt.id">
                                    <div class="flex items-center justify-between">
                                      <span class="text-sm font-medium text-slate-900">{{ opt.itemName }}</span>
                                      <span class="text-xs text-slate-400">¥{{ opt.price }}</span>
                                    </div>
                                    <div class="text-xs text-slate-400">{{ opt.itemCode }}</div>
                                  </el-option>
                                </el-select>
                              </template>
                            </el-table-column>
                            <el-table-column label="标本" width="110">
                              <template #default="{ row }">
                                <el-select v-model="row.specimenType" :disabled="!applyRowEditable(row)"
                                           placeholder="标本" size="small" style="width: 100%">
                                  <el-option label="血液" value="血液"/>
                                  <el-option label="尿液" value="尿液"/>
                                  <el-option label="粪便" value="粪便"/>
                                  <el-option label="体液" value="体液"/>
                                </el-select>
                              </template>
                            </el-table-column>
                            <el-table-column align="center" label="空腹" width="90">
                              <template #default="{ row }">
                                <el-switch v-model="row.isFasting" :active-value="1" :disabled="!applyRowEditable(row)"
                                           :inactive-value="0" active-text="空腹" size="small"/>
                              </template>
                            </el-table-column>
                            <el-table-column label="检验目的" min-width="160">
                              <template #default="{ row }">
                                <el-input v-model="row.laboratoryPurpose" :disabled="!applyRowEditable(row)"
                                          placeholder="检验目的（可选）" size="small"/>
                              </template>
                            </el-table-column>
                            <el-table-column label="状态" min-width="150">
                              <template #default="{ row }">
                                <el-tag v-if="row.id" :type="applyTagType(row)" size="small">{{
                                    row.execStatusText
                                  }}
                                </el-tag>
                                <el-tag v-else size="small" type="info">保存病历时开单</el-tag>
                                <el-tag v-if="row.critical" effect="dark" size="small" type="danger">危急值</el-tag>
                                <el-tag v-if="row.isEmergency === 1" size="small" type="warning">急诊</el-tag>
                                <el-tag v-if="row.signStatus === 1" effect="plain" size="small" type="success">
                                  已电子签名
                                </el-tag>
                                <el-tag v-else-if="row.signStatus === 2" effect="plain" size="small" type="warning">
                                  签名已作废
                                </el-tag>
                              </template>
                            </el-table-column>
                            <el-table-column align="center" label="" width="64">
                              <template #default="{ $index }">
                                <el-button v-perm="'opd:doctorWorkstation:delete'" link size="small" type="danger"
                                           @click="handleDeleteLaboratoryRecord($index)">删除
                                </el-button>
                              </template>
                            </el-table-column>
                          </el-table>
                        </div>
                        <!-- 检验费表尾合计（标准 HIS 表尾） -->
                        <div v-if="laboratoryRecords.length > 0"
                             class="mt-2 flex items-center justify-end text-xs text-slate-500">
                          <span>共 <b class="text-slate-800">{{ laboratoryRecords.length }}</b> 项 · 检验费
                            <b class="text-[#1269B5]">¥{{ labTotalAmount.toFixed(2) }}</b>
                          </span>
                        </div>
                      </template>
                      <!-- 纯列表 (非就诊中) -->
                      <template v-else>
                        <div v-for="(item, idx) in laboratoryRecords"
                             :key="item.id || ('pending-laboratory-' + item._pendingIdx)"
                             :class="[
                             'rounded-lg border p-3',
                             item.critical
                                 ? 'border-red-300 bg-red-50'
                                 : (item.execStatusText === '待缴费' ? 'border-amber-200 bg-amber-50' : 'border-slate-200')
                           ]">
                          <div class="flex items-center justify-between">
                            <div class="flex items-center gap-2">
                              <span class="text-sm font-medium text-slate-900">{{ item.laboratoryItemName }}</span>
                              <el-tag :type="applyTagType(item)" size="small">{{ item.execStatusText }}</el-tag>
                              <el-tag v-if="item.critical" effect="dark" size="small" type="danger">危急值</el-tag>
                              <el-tag v-if="item.isEmergency === 1" size="small" type="warning">急诊</el-tag>
                              <el-tag v-if="item.isFasting === 1" size="small" type="info">空腹</el-tag>
                              <el-tag v-if="item.signStatus === 1" effect="plain" size="small" type="success">
                                已电子签名·{{ item.doctorName }}
                              </el-tag>
                              <el-tag v-else-if="item.signStatus === 2" effect="plain" size="small" type="warning">
                                签名已作废
                              </el-tag>
                            </div>
                            <div class="flex items-center gap-2">
                              <span class="text-xs text-slate-400">{{ item.createTime }}</span>
                              <el-button v-if="item.execRecordId" link size="small" type="primary"
                                         @click="handleViewLaboratoryReport(item)">
                                查看报告
                              </el-button>
                              <!-- 批次E/E6：结果已回 → 一键建复诊（关联本次病历，免门诊诊查费） -->
                              <el-button v-if="canCreateRevisit(item)" link size="small" type="warning"
                                         @click="handleCreateRevisit(item)">
                                建复诊
                              </el-button>
                              <el-button v-if="item.canDelete" v-perm="'opd:doctorWorkstation:delete'" link
                                         size="small" type="danger"
                                         @click="handleDeleteLaboratoryRecord(idx)">
                                删除
                              </el-button>
                              <el-tooltip v-else-if="item.deleteBlockReason" :content="item.deleteBlockReason">
                                <span class="cursor-help text-xs text-slate-300">删除</span>
                              </el-tooltip>
                            </div>
                          </div>
                          <div v-if="item.specimenType" class="mt-1 text-xs text-slate-500">标本：{{
                              item.specimenType
                            }}
                          </div>
                          <div v-if="item.laboratoryPurpose" class="mt-1 text-xs text-slate-500">
                            目的：{{ item.laboratoryPurpose }}
                          </div>
                          <div v-if="item.critical" class="mt-2 rounded bg-red-100 p-2 text-xs text-red-700">
                            该检验出现危急值，请优先处理并及时通知患者。
                          </div>
                        </div>
                        <!-- 批次E/F：只读态下没有申请时给出明确空态，避免「没开」和「没加载出来」混淆 -->
                        <div v-if="laboratoryRecords.length === 0"
                             class="py-8 text-center text-sm text-slate-400">
                          {{ patientDataLoading ? '检验申请加载中…' : '本次就诊还没有检验申请' }}
                        </div>
                      </template>
                    </div>
                  </div>
                </div>
              </div>
            </OrderPanel>
          </div>
            </div>
          <!-- 底部动作条：占满「病历 + 处方/医嘱」两列总宽（主区块底部），不压第三栏决策支持 -->
          <PageActionBar align="start" class="flex-wrap !h-auto shrink-0">
            <!-- 结诊前：核心操作 -->
            <div v-if="!isVisitCompleted" class="flex items-center gap-3">
              <!-- 住院证记录查看/作废：排在临时保存前（用户要求），普通按钮样式（plain 白底边框） -->
              <el-button v-if="currentPatient.queueStatus == 3" v-perm="'opd:doctorWorkstation:list'" plain
                         @click="myAdmissionListVisible = true">我开的住院证
              </el-button>
              <!-- 模板维护（五类模板统一入口：常用诊断/处方模板/药品套餐/检查模板/检验模板，弹窗左侧分类、右侧列表套用） -->
              <el-button v-if="currentPatient.queueStatus == 3" plain @click="openTemplateMaintain">模板维护
              </el-button>
              <el-button v-if="currentPatient.queueStatus == 3"
                         v-perm="['opd:doctorWorkstation:add', 'opd:doctorWorkstation:edit']" type="primary"
                         @click="handleSaveRecord">
                临时保存
              </el-button>
              <el-button v-if="currentPatient.queueStatus == 3" v-perm="'opd:doctorWorkstation:add'" :icon="Tickets" plain
                         type="success" @click="openOrderDialog">
                开住院证
              </el-button>
              <el-button v-if="currentPatient.queueStatus == 3" v-perm="'opd:doctorWorkstation:add'" plain type="primary"
                         @click="openRevisitAppoint">
                预约复诊
              </el-button>
              <el-button v-if="currentPatient.queueStatus == 3" v-perm="'opd:doctorWorkstation:edit'" :loading="submitting"
                         type="warning" @click="handleComplete">
                结诊
              </el-button>
            </div>
            <!-- 结诊后：打印指引 + 撤回结诊（已提交可撤，已归档后端拒） -->
            <div v-else class="flex items-center gap-3">
              <el-button v-if="isRecordSubmitted" v-perm="'opd:doctorWorkstation:edit'" :loading="revoking" plain
                         type="danger" @click="handleRevokeRecord">
                <el-icon class="mr-1">
                  <RefreshLeft/>
                </el-icon>
                撤回结诊
              </el-button>
              <el-button :loading="printingDocs" type="success" @click="handlePrintVisitDocs">
                <el-icon class="mr-1">
                  <Printer/>
                </el-icon>
                打印就诊文书
              </el-button>
            </div>
          </PageActionBar>
          </div>
          <!--
            第三栏（决策支持栏）：辅助诊疗 + 用药安全审查。标准 HIS 门诊医生站常把 AI/CDSS 辅助
            放在主工作区一侧的窄栏，不挤占医嘱开单宽度；本栏 300px、独立滚动。
          -->
          <div class="flex w-[300px] shrink-0 flex-col gap-3 overflow-y-auto pb-3 custom-scrollbar">
            <!-- 本次费用（标准 HIS 开单区常驻合计 + 明细直接展开：金额不多，医生一眼看到处方/检查/检验各项；
                 收款在收费窗口/自助机，医生只核对开单金额） -->
            <div class="rounded-lg border border-slate-200 bg-white px-3 py-2.5">
              <div class="flex items-center justify-between">
                <span class="text-xs font-medium text-slate-600">本次费用（预估）</span>
                <span class="text-base font-bold text-[#1269B5]">¥{{ orderTotalAmount.toFixed(2) }}</span>
              </div>
              <div
                  class="mt-2 max-h-40 space-y-1 overflow-y-auto border-t border-slate-100 pt-2 text-xs custom-scrollbar">
                <div v-if="rxFeeRows.length === 0 && inspectionRecords.length === 0 && laboratoryRecords.length === 0"
                     class="py-1 text-slate-400">暂无开单项目
                </div>
                <template v-else>
                  <div v-for="r in rxFeeRows" :key="r.label" class="flex items-center justify-between">
                    <span class="text-slate-500">{{ r.label }}</span>
                    <span class="font-medium text-slate-700">¥{{ r.amount.toFixed(2) }}</span>
                  </div>
                  <div v-for="(it, i) in inspectionRecords" :key="'i' + i" class="flex items-center justify-between">
                    <span class="truncate text-slate-500">{{ it.inspectionItemName }}</span>
                    <span class="font-medium text-slate-700">¥{{ Number(it.price || 0).toFixed(2) }}</span>
                  </div>
                  <div v-for="(it, i) in laboratoryRecords" :key="'l' + i" class="flex items-center justify-between">
                    <span class="truncate text-slate-500">{{ it.laboratoryItemName }}</span>
                    <span class="font-medium text-slate-700">¥{{ Number(it.price || 0).toFixed(2) }}</span>
                  </div>
                </template>
              </div>
              <div class="mt-1 text-[10px] text-slate-400">
                结算金额以收费端为准
              </div>
            </div>
            <!-- AI辅助诊疗：内容区限高 + 卡内滚动条；卡背景显式白 -->
            <div class="overflow-hidden rounded-lg border border-slate-200 bg-white">
              <div class="border-b border-slate-200 bg-slate-50 px-3 py-2">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <div
                        class="flex h-5 w-5 items-center justify-center rounded bg-gradient-to-br from-blue-500 to-purple-600">
                      <span class="text-[10px] font-bold text-white">AI</span>
                    </div>
                    <span class="text-sm font-bold text-slate-700">辅助诊疗</span>
                  </div>
                  <el-button :loading="aiDiagnosisLoading" link size="small" type="primary"
                             @click="handleAiDiagnosis">
                    刷新推荐
                  </el-button>
                </div>
              </div>
              <div class="max-h-[480px] overflow-y-auto p-3 custom-scrollbar">
                <!-- AI诊断推荐 -->
                <div v-if="aiDiagnosisResults.length > 0">
                  <!-- 模型未参与只说明降级原因分类，不展示底层报错原文（连接串/端口对医生无意义） -->
                  <div v-if="aiDiagnosisDegraded"
                       class="mb-2 rounded bg-amber-50 px-2 py-1 text-xs text-amber-700">
                    模型未参与，以下为码表规则匹配结果
                  </div>
                  <div class="mb-2 text-xs text-slate-500">根据病历信息，系统推荐以下诊断：</div>
                  <div class="space-y-2">
                    <div v-for="(item, index) in aiDiagnosisResults" :key="index"
                         class="rounded-lg border border-slate-100 p-2.5 transition-colors hover:border-blue-200 hover:bg-blue-50">
                      <div class="flex items-center justify-between">
                        <div class="flex items-center gap-2">
                    <span
                        class="flex h-5 w-5 items-center justify-center rounded-full bg-blue-100 text-[10px] font-bold text-blue-600">
                      {{ index + 1 }}
                    </span>
                          <span class="text-sm font-medium text-slate-900">{{ item.icdName }}</span>
                        </div>
                        <el-tag v-if="item.confidence !== null"
                                :type="item.confidence >= 80 ? 'success' : item.confidence >= 60 ? 'warning' : 'info'"
                                size="small">
                          {{ item.confidence }}%
                        </el-tag>
                        <el-tag v-else effect="plain" size="small" type="info">
                          {{ item.source === 'rule' ? '规则匹配' : '未给分值' }}
                        </el-tag>
                      </div>
                      <div class="mt-1.5 flex items-center gap-2">
                        <span class="text-[10px] text-slate-400">{{ item.icdCode }}</span>
                        <el-button link size="small" type="primary" @click="handleAdoptAiDiagnosis(item)">
                          采纳
                        </el-button>
                        <el-button link size="small" type="info" @click="handleViewAiGuide(item)">
                          查看指南
                        </el-button>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else-if="aiDiagnosisLoading" class="py-6 text-center">
                  <el-icon class="mb-2 h-6 w-6 animate-spin text-blue-500">
                    <Loading/>
                  </el-icon>
                  <div class="text-xs text-slate-500">AI正在分析病历信息...</div>
                </div>
                <div v-else class="py-4 text-center">
                  <div class="text-xs text-slate-400">填写主诉或现病史后</div>
                  <div class="text-xs text-slate-400">系统将自动推荐诊断</div>
                </div>
              </div>
            </div>
            <!-- 用药安全审查：开方实时审方（行变化即比对，命中实时显示）+ 结诊提交后药师审方复核 -->
            <div class="overflow-hidden rounded-lg border border-slate-200 bg-white">
              <div class="border-b border-slate-200 bg-slate-50 px-3 py-2">
                <div class="flex items-center justify-between">
                  <div class="flex items-center gap-2">
                    <el-icon class="text-slate-400">
                      <InfoFilled/>
                    </el-icon>
                    <span class="text-sm font-bold text-slate-700">用药安全审查</span>
                  </div>
                  <el-tag v-if="rxDraftChecking" effect="plain" size="small" type="info">审查中…</el-tag>
                  <el-tag v-else-if="rxDraftHits.length > 0" :type="rxDraftHits.some(h => h.blocked) ? 'danger' : 'warning'"
                          size="small">
                    {{ rxDraftHits.some(h => h.blocked) ? '存在拦截项' : rxDraftHits.length + ' 项提示' }}
                  </el-tag>
                  <el-tag v-else-if="prescriptionForm.details.length > 0" effect="plain" size="small"
                          type="success">未发现问题
                  </el-tag>
                  <el-tag v-else effect="plain" size="small" type="info">待处方</el-tag>
                </div>
              </div>
              <div class="p-3">
                <div v-if="prescriptionForm.details.length === 0" class="py-4 text-center">
                  <div class="text-xs text-slate-400">添加药品后实时审查配伍与剂量；结诊提交后由药师审方复核</div>
                </div>
                <div v-else-if="rxDraftHits.length === 0" class="py-4 text-center">
                  <div class="text-xs text-slate-400">未发现配伍禁忌 / 剂量超限问题</div>
                </div>
                <div v-else class="space-y-1.5">
                  <div v-for="(h, i) in rxDraftHits" :key="i" :class="h.blocked ? 'bg-red-50 text-red-700' : 'bg-amber-50 text-amber-700'"
                       class="rounded-md px-2 py-1.5 text-xs">
                    <div class="font-medium">{{ h.drugNameA }}
                      <template v-if="h.drugNameB"> × {{ h.drugNameB }}</template>
                    </div>
                    <div>{{ h.message }}</div>
                    <div v-if="h.suggestion" class="mt-0.5 text-[10px] opacity-80">建议：{{ h.suggestion }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

      </template>
    </div>

  </div>
  <!-- 历史就诊（患者级）：弹窗不打断接诊，内含 CDR 全景时间轴与「打开完整时间轴」 -->
  <PatientDetailDialog v-model="showPatientDetail" :patient="currentPatient"
                       :patient-id="currentPatient?.patientId"/>

  <!-- 费用与医保抽屉（原来常驻右栏 + 藏在一个 Tab 里，改为患者条「费用」按需展开） -->
  <el-drawer v-model="showChargeDrawer" size="560px" title="医保与收费">
    <div class="space-y-3">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <div class="mb-3 flex items-center gap-2">
          <el-icon class="text-blue-500">
            <Document/>
          </el-icon>
          <span class="text-sm font-bold text-slate-700">医保与结算</span>
        </div>
        <div class="space-y-3">
          <div class="rounded-lg bg-blue-50 p-3">
            <div class="text-xs text-slate-500">医保类型</div>
            <div class="text-sm font-medium text-slate-900">{{ insuranceInfo?.medicalInsuranceType || '-' }}</div>
          </div>
          <div class="grid grid-cols-2 gap-2">
            <div class="rounded-lg bg-emerald-50 p-3">
              <div class="text-xs text-slate-500">统筹比例</div>
              <div class="text-lg font-bold text-emerald-600">{{ insuranceInfo?.coverageRatio || 0 }}%</div>
            </div>
            <div class="rounded-lg bg-amber-50 p-3">
              <div class="text-xs text-slate-500">结算方式</div>
              <div class="text-xs font-medium text-amber-700">{{
                  Number(insuranceInfo?.settlementType) === 1 ? '全自费' : '医保联网'
                }}
              </div>
            </div>
          </div>
          <div class="space-y-2 border-t border-slate-100 pt-3">
            <div class="flex items-center justify-between text-sm">
              <span class="text-slate-500">本单预估</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-xs text-slate-500">药品费用</span>
              <span class="text-sm font-medium text-slate-900">¥{{
                  (insuranceInfo?.drugTotal || 0).toFixed(2)
                }}</span>
            </div>
            <div class="flex items-center justify-between">
              <span class="text-xs text-slate-500">统筹支付</span>
              <span class="text-sm font-medium text-emerald-600">¥{{
                  (insuranceInfo?.insurancePay || 0).toFixed(2)
                }}</span>
            </div>
            <div class="flex items-center justify-between border-t border-slate-100 pt-2">
              <span class="text-xs font-medium text-slate-700">个人自付</span>
              <span class="text-base font-bold text-red-600">¥{{ (insuranceInfo?.selfPay || 0).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>

    </div>
  </el-drawer>

  <!-- AI诊疗指南弹窗 -->
  <el-dialog
      v-model="aiGuideDialogVisible"
      :close-on-click-modal="false"
      title="诊疗指南"
      width="700px"
  >
    <div v-if="aiGuideData" class="space-y-4">
      <div class="rounded-lg bg-gradient-to-r from-blue-50 to-purple-50 p-4">
        <div class="flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-full bg-blue-100">
            <el-icon class="h-5 w-5 text-blue-600">
              <Document/>
            </el-icon>
          </div>
          <div>
            <div class="text-lg font-bold text-slate-900">{{ aiGuideData.icdName }}</div>
            <div class="text-sm text-slate-500">ICD编码: {{ aiGuideData.icdCode }}</div>
          </div>
        </div>
      </div>

      <!-- 治疗原则 -->
      <div class="rounded-lg border border-slate-200 p-4">
        <h4 class="mb-3 flex items-center gap-2 text-sm font-bold text-slate-700">
          <el-icon class="text-blue-500">
            <CircleCheck/>
          </el-icon>
          治疗原则
        </h4>
        <div class="space-y-2 text-sm text-slate-600">
          <div class="flex items-start gap-2">
            <span class="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500"></span>
            <span>明确诊断后针对性治疗</span>
          </div>
          <div class="flex items-start gap-2">
            <span class="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500"></span>
            <span>根据病情严重程度选择门诊或住院治疗</span>
          </div>
          <div class="flex items-start gap-2">
            <span class="mt-1 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500"></span>
            <span>定期复查，评估治疗效果</span>
          </div>
        </div>
      </div>

      <!-- 推荐用药 -->
      <div class="rounded-lg border border-slate-200 p-4">
        <h4 class="mb-3 flex items-center gap-2 text-sm font-bold text-slate-700">
          <el-icon class="text-emerald-500">
            <Coin/>
          </el-icon>
          推荐用药方案
        </h4>
        <div class="space-y-3">
          <div class="rounded-lg bg-slate-50 p-3">
            <div class="mb-1 text-sm font-medium text-slate-900">一线用药</div>
            <div class="text-xs text-slate-600">根据临床指南推荐，首选以下药物</div>
            <div class="mt-2 space-y-1">
              <div class="flex items-center justify-between rounded bg-white p-2">
                <span class="text-sm text-slate-700">阿莫西林克拉维酸钾 0.375g</span>
                <span class="text-xs text-slate-500">口服，一日三次</span>
              </div>
              <div class="flex items-center justify-between rounded bg-white p-2">
                <span class="text-sm text-slate-700">头孢呋辛 0.75g</span>
                <span class="text-xs text-slate-500">静脉注射，每8小时一次</span>
              </div>
            </div>
          </div>
          <div class="rounded-lg bg-slate-50 p-3">
            <div class="mb-1 text-sm font-medium text-slate-900">替代用药</div>
            <div class="text-xs text-slate-600">对一线药物过敏或耐药时考虑</div>
            <div class="mt-2 space-y-1">
              <div class="flex items-center justify-between rounded bg-white p-2">
                <span class="text-sm text-slate-700">阿奇霉素 0.5g</span>
                <span class="text-xs text-slate-500">口服，一日一次</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 注意事项 -->
      <div class="rounded-lg border border-amber-200 bg-amber-50 p-4">
        <h4 class="mb-2 flex items-center gap-2 text-sm font-bold text-amber-700">
          <el-icon>
            <Warning/>
          </el-icon>
          注意事项
        </h4>
        <ul class="space-y-1 text-sm text-amber-800">
          <li>• 用药前询问过敏史</li>
          <li>• 注意肝肾功能调整剂量</li>
          <li>• 疗程结束后评估疗效</li>
          <li>• 如症状未缓解及时复诊</li>
        </ul>
      </div>
    </div>
    <template #footer>
      <el-button type="primary" @click="handleAdoptAiDiagnosis(aiGuideData)">
        采纳该诊断
      </el-button>
      <el-button @click="aiGuideDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 结诊文书弹窗（引导单 / 处方签，均含左上角聚合支付码） -->
  <el-dialog
      v-model="showGuideSheetDialog"
      :close-on-click-modal="false"
      title="结诊文书"
      width="960px"
  >
    <div class="mb-3 flex items-center gap-2">
      <el-button :type="guideDocTab === 'guide' ? 'primary' : ''" size="small"
                 @click="guideDocTab = 'guide'">引导单
      </el-button>
      <el-button :type="guideDocTab === 'rx' ? 'primary' : ''" size="small"
                 @click="guideDocTab = 'rx'">处方签
      </el-button>
    </div>
    <div class="min-h-[800px]">
      <div v-if="loadingGuide" class="flex items-center justify-center py-10 text-sm text-slate-500">加载中...</div>
      <iframe v-else-if="guideDocTab === 'guide' && guidePdfUrl" :src="guidePdfUrl"
              class="h-[800px] w-full border-0"></iframe>
      <iframe v-else-if="guideDocTab === 'rx' && rxPdfUrl" :src="rxPdfUrl"
              class="h-[800px] w-full border-0"></iframe>
      <div v-else class="flex items-center justify-center py-10 text-sm text-slate-400">
        {{ guideDocTab === 'guide' ? '指引单尚未生成，请先提交病历' : '处方签尚未生成，结诊提交后自动生成' }}
      </div>
    </div>
    <template #footer>
      <el-button type="primary" @click="printGuidePdf">打印</el-button>
      <el-button @click="showGuideSheetDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 标签管理弹窗 -->
  <el-dialog
      v-model="showTagDialog"
      :close-on-click-modal="false"
      title="患者标签管理"
      width="500px"
  >
    <div v-if="currentPatient" class="space-y-4">
      <!-- 当前患者标签 -->
      <div>
        <h4 class="mb-2 text-sm font-medium text-slate-700">当前标签</h4>
        <div v-if="patientTags.length > 0" class="flex flex-wrap gap-2">
          <span v-for="tag in patientTags" :key="tag.tagId"
                :style="{ backgroundColor: tag.tagColor || '#409EFF' }"
                class="inline-flex items-center gap-1 rounded px-2 py-1 text-xs font-medium text-white">
            {{ tag.tagName }}
            <el-icon class="cursor-pointer hover:text-red-200" @click="handleRemoveTag(tag.tagId)">
              <Delete class="h-3 w-3"/>
            </el-icon>
          </span>
        </div>
        <div v-else class="text-sm text-slate-400">暂无标签</div>
      </div>

      <!-- 添加标签 -->
      <div>
        <h4 class="mb-2 text-sm font-medium text-slate-700">添加标签</h4>
        <el-input v-model="tagSearchKeyword" :prefix-icon="Search" class="mb-3" clearable placeholder="搜索标签..."/>
        <div class="max-h-60 overflow-y-auto">
          <div class="flex flex-wrap gap-2">
            <span v-for="tag in filteredAllTags" :key="tag.tagId"
                  :class="isTagAdded(tag.tagId) ? 'opacity-50 cursor-not-allowed' : 'text-white cursor-pointer'"
                  :style="{ backgroundColor: isTagAdded(tag.tagId) ? '#ccc' : (tag.tagColor || '#409EFF') }"
                  class="inline-flex cursor-pointer items-center gap-1 rounded px-2 py-1 text-xs font-medium transition-all hover:scale-105"
                  @click="!isTagAdded(tag.tagId) && handleAddTag(tag.tagId)">
              {{ tag.tagName }}
              <el-icon v-if="!isTagAdded(tag.tagId)" class="h-3 w-3"><Plus/></el-icon>
            </span>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>

  <!-- 常用诊断维护 -->
  <el-dialog v-model="showDiagTemplateDialog" destroy-on-close title="常用诊断维护" width="600px"
             @open="loadDiagTemplates">
    <div class="space-y-3">
      <div class="text-xs text-slate-500">管理您常用的ICD-10诊断编码，开方时可快速选择。</div>
      <!-- 搜索添加 -->
      <div>
        <el-input v-model="diagSearchKeyword" :prefix-icon="Search" clearable
                  placeholder="输入诊断名称或编码搜索并添加" @clear="diagSearchResults = []"
                  @input="handleDiagSearchInDialog"/>
        <div v-if="diagSearchResults.length > 0"
             class="mt-2 max-h-40 space-y-1 overflow-y-auto rounded border border-slate-200 p-2">
          <div v-for="item in diagSearchResults" :key="item.icdCode"
               class="flex cursor-pointer items-center justify-between rounded px-2 py-1.5 hover:bg-blue-50"
               @click="handleAddDiagFromDialog(item)">
            <div class="flex items-center gap-2">
              <span class="font-mono text-xs font-medium text-blue-600">{{ item.icdCode }}</span>
              <span class="text-sm text-slate-900">{{ item.icdName }}</span>
            </div>
            <el-icon class="text-blue-500">
              <Plus/>
            </el-icon>
          </div>
        </div>
      </div>
      <!-- 已有列表 -->
      <div v-if="myDiagTemplates.length === 0" class="py-4 text-center text-sm text-slate-400">
        暂无常用诊断
      </div>
      <div v-else class="space-y-2">
        <div v-for="(item, idx) in myDiagTemplates" :key="item.id || idx"
             class="flex items-center justify-between rounded border border-slate-200 p-2.5 hover:bg-slate-50">
          <div class="flex items-center gap-2">
            <span class="font-mono text-xs font-medium text-blue-600">{{ item.icdCode }}</span>
            <span class="text-sm text-slate-900">{{ item.icdName }}</span>
          </div>
          <el-button link size="small" type="danger" @click="handleDeleteDiagTemplate(idx)">删除</el-button>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showDiagTemplateDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 处方模板管理 -->
  <el-dialog v-model="showRxTemplateDialog" destroy-on-close title="处方模板管理" width="700px"
             @open="loadRxTemplates">
    <div class="space-y-3">
      <div class="text-xs text-slate-500">按病种保存常用处方组合，开方时一键套用。</div>
      <div class="flex items-center gap-2">
        <el-input v-model="newTemplateName" class="flex-1" placeholder="模板名称，如：上呼吸道感染"/>
        <el-button size="small" type="primary" @click="handleAddRxTemplate">保存当前处方为模板</el-button>
      </div>
      <div v-if="myRxTemplates.length === 0" class="py-8 text-center text-sm text-slate-400">暂无处方模板</div>
      <div v-else class="space-y-2">
        <div v-for="(tpl, idx) in myRxTemplates" :key="tpl.id || idx"
             class="rounded border border-slate-200 p-3 hover:bg-slate-50">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-slate-900">{{ tpl.templateName }}</span>
            <div class="flex gap-2">
              <el-button link size="small" type="primary" @click="handleApplyRxTemplate(tpl)">套用</el-button>
              <el-button link size="small" type="danger" @click="handleDeleteRxTemplate(idx)">删除</el-button>
            </div>
          </div>
          <div class="mt-1 text-xs text-slate-500">{{ tpl.drugCount }}种药品，合计 ¥{{ tpl.totalAmount }}</div>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showRxTemplateDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 药品/耗材套餐 -->
  <el-dialog v-model="showPackageDialog" destroy-on-close title="药品/耗材套餐" width="700px"
             @open="loadDrugPackages">
    <div class="space-y-3">
      <div class="text-xs text-slate-500">将药品+检查+检验打包成治疗方案套餐，一键开立全套医嘱。</div>
      <el-button size="small" type="primary" @click="showAddPackageDialog = true">
        <el-icon class="mr-0.5">
          <Plus/>
        </el-icon>
        将当前处方/检查/检验保存为套餐
      </el-button>
      <div v-if="myPackages.length === 0" class="py-8 text-center text-sm text-slate-400">暂无套餐</div>
      <div v-else class="space-y-2">
        <div v-for="(pkg, idx) in myPackages" :key="pkg.id || idx"
             class="rounded border border-slate-200 p-3 hover:bg-slate-50">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-slate-900">{{ pkg.packageName }}</span>
            <div class="flex gap-2">
              <el-button link size="small" type="primary" @click="handleApplyPackage(pkg)">套用</el-button>
              <el-button link size="small" type="danger" @click="handleDeletePackage(idx)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showPackageDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 新增套餐弹窗 -->
  <el-dialog v-model="showAddPackageDialog" destroy-on-close title="保存为套餐" width="400px">
    <div class="space-y-3">
      <el-input v-model="newPackageName" placeholder="套餐名称，如：术前检查套餐"/>
      <div class="text-xs text-slate-500">
        将当前处方（{{ prescriptionForm.details.length }}种药品）保存为套餐。
      </div>
    </div>
    <template #footer>
      <el-button type="primary" @click="handleSaveNewPackage">保存</el-button>
      <el-button @click="showAddPackageDialog = false">取消</el-button>
    </template>
  </el-dialog>

  <!-- 检查申请模板弹窗 -->
  <el-dialog v-model="showInspectionTemplateDialog" destroy-on-close title="检查申请模板" width="700px"
             @open="loadInspectionTemplates">
    <div class="space-y-3">
      <div class="text-xs text-slate-500">将常用检查项目保存为模板，一键套用。</div>
      <el-button size="small" type="primary" @click="showAddInspectionTemplateDialog = true">
        <el-icon class="mr-0.5">
          <Plus/>
        </el-icon>
        新增模板
      </el-button>
      <div class="max-h-[300px] space-y-2 overflow-y-auto">
        <div v-for="(tpl, idx) in myInspectionTemplates" :key="tpl.id"
             class="flex items-center justify-between rounded-lg border border-slate-100 p-3 transition-colors hover:bg-slate-50">
          <div class="flex-1">
            <div class="text-sm font-medium text-slate-800">{{ tpl.templateName }}</div>
            <div class="text-xs text-slate-400">{{ tpl.inspectionItemName }} · {{ tpl.bodyPart || '未指定部位' }}</div>
          </div>
          <div class="flex items-center gap-2">
            <el-button link size="small" type="primary" @click="handleApplyInspectionTemplate(tpl)">套用</el-button>
            <el-button link size="small" type="danger" @click="handleDeleteInspectionTemplate(idx)">删除</el-button>
          </div>
        </div>
        <div v-if="myInspectionTemplates.length === 0" class="py-8 text-center text-xs text-slate-400">
          暂无模板，可在开立检查申请时保存为模板
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showInspectionTemplateDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 新增检查模板弹窗 -->
  <el-dialog v-model="showAddInspectionTemplateDialog" destroy-on-close title="保存检查申请模板" width="500px">
    <div class="space-y-3">
      <el-input v-model="inspectionTemplateName" placeholder="模板名称，如：术前胸部检查"/>
      <el-select v-model="inspectionTemplateForm.inspectionItemId" :loading="inspectionTemplateItemLoading"
                 :remote-method="handleInspectionTemplateItemSearch" class="w-full"
                 filterable placeholder="搜索检查项目"
                 remote reserve-keyword
                 @change="handleInspectionTemplateItemSelect">
        <el-option v-for="item in inspectionTemplateItemResults" :key="item.id"
                   :label="`${item.itemCode} - ${item.itemName}`" :value="item.id">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-slate-900">{{ item.itemName }}</span>
            <span class="text-xs text-slate-400">¥{{ item.price }}</span>
          </div>
          <div class="text-xs text-slate-400">{{ item.itemCode }} · {{ item.bodyPart }}</div>
        </el-option>
      </el-select>
      <el-input v-model="inspectionTemplateForm.bodyPart" placeholder="检查部位（可选）"/>
      <el-input v-model="inspectionTemplateForm.inspectionPurpose" placeholder="检查目的（可选）"/>
    </div>
    <template #footer>
      <el-button type="primary" @click="handleSaveInspectionTemplate">保存</el-button>
      <el-button @click="showAddInspectionTemplateDialog = false">取消</el-button>
    </template>
  </el-dialog>

  <!-- 检验申请模板弹窗 -->
  <el-dialog v-model="showLaboratoryTemplateDialog" destroy-on-close title="检验申请模板" width="700px"
             @open="loadLaboratoryTemplates">
    <div class="space-y-3">
      <div class="text-xs text-slate-500">将常用检验项目保存为模板，一键套用。</div>
      <el-button size="small" type="primary" @click="showAddLaboratoryTemplateDialog = true">
        <el-icon class="mr-0.5">
          <Plus/>
        </el-icon>
        新增模板
      </el-button>
      <div class="max-h-[300px] space-y-2 overflow-y-auto">
        <div v-for="(tpl, idx) in myLaboratoryTemplates" :key="tpl.id"
             class="flex items-center justify-between rounded-lg border border-slate-100 p-3 transition-colors hover:bg-slate-50">
          <div class="flex-1">
            <div class="text-sm font-medium text-slate-800">{{ tpl.templateName }}</div>
            <div class="text-xs text-slate-400">{{ tpl.laboratoryItemName }} · {{
                tpl.sampleType || '未指定标本'
              }}
            </div>
          </div>
          <div class="flex items-center gap-2">
            <el-button link size="small" type="primary" @click="handleApplyLaboratoryTemplate(tpl)">套用</el-button>
            <el-button link size="small" type="danger" @click="handleDeleteLaboratoryTemplate(idx)">删除</el-button>
          </div>
        </div>
        <div v-if="myLaboratoryTemplates.length === 0" class="py-8 text-center text-xs text-slate-400">
          暂无模板，可在开立检验申请时保存为模板
        </div>
      </div>
    </div>
    <template #footer>
      <el-button @click="showLaboratoryTemplateDialog = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 新增检验模板弹窗 -->
  <el-dialog v-model="showAddLaboratoryTemplateDialog" destroy-on-close title="保存检验申请模板" width="500px">
    <div class="space-y-3">
      <el-input v-model="laboratoryTemplateName" placeholder="模板名称，如：术前血常规"/>
      <el-select v-model="laboratoryTemplateForm.laboratoryItemId" :loading="laboratoryTemplateItemLoading"
                 :remote-method="handleLaboratoryTemplateItemSearch" class="w-full"
                 filterable placeholder="搜索检验项目"
                 remote reserve-keyword
                 @change="handleLaboratoryTemplateItemSelect">
        <el-option v-for="item in laboratoryTemplateItemResults" :key="item.id"
                   :label="`${item.itemCode} - ${item.itemName}`" :value="item.id">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-slate-900">{{ item.itemName }}</span>
            <span class="text-xs text-slate-400">¥{{ item.price }}</span>
          </div>
          <div class="text-xs text-slate-400">{{ item.itemCode }} · {{ item.sampleType }}</div>
        </el-option>
      </el-select>
      <el-input v-model="laboratoryTemplateForm.sampleType" placeholder="标本类型（可选）"/>
      <el-input v-model="laboratoryTemplateForm.inspectionPurpose" placeholder="检验目的（可选）"/>
    </div>
    <template #footer>
      <el-button type="primary" @click="handleSaveLaboratoryTemplate">保存</el-button>
      <el-button @click="showAddLaboratoryTemplateDialog = false">取消</el-button>
    </template>
  </el-dialog>

  <!-- 病历文本智能录入（P1-3 结构化抽取） -->
  <el-dialog v-model="extractDialogVisible" :close-on-click-modal="false" destroy-on-close title="智能录入「病史」"
             width="900px">
    <div class="space-y-3">
      <el-input v-model="extractRawText" :rows="7"
                placeholder="例：主诉：反复咳嗽3天&#10;现病史：3天前受凉后出现咳嗽，夜间为甚，咳白色粘痰，无发热&#10;既往史：高血压5年，规律服药&#10;婚育史：已婚，生育1子&#10;T36.8℃ P82次/分 R18次/分 BP130/85mmHg"
                type="textarea"/>

      <div class="flex items-center justify-between">
        <span class="text-xs text-slate-400">
          {{ extractRawText.length }} 字（超过 4000 字会被截断，且会在结果里标注）
        </span>
        <div class="flex items-center gap-2">
          <el-button v-if="extractRawText" size="small" @click="extractRawText = ''">清空</el-button>
          <el-button :loading="extractLoading" size="small" type="primary" @click="runExtract">
            解析
          </el-button>
        </div>
      </div>

      <template v-if="extractResult">
        <div v-if="extractResult.degraded"
             class="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          <b>本次未经过大模型</b>：{{ extractResult.degradeReason }}
        </div>
        <div v-if="extractResult.truncated"
             class="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
          文本超出长度上限，<b>后半段没有参与解析</b>。
        </div>
        <div v-if="extractResult.rejectedCount > 0"
             class="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-xs text-red-700">
          <b>{{ extractResult.rejectedCount }} 条内容被丢弃</b>（原文里找不到依据，或字段不能写入病历）：
          <ul class="mt-1 list-disc pl-4">
            <li v-for="(note, idx) in extractResult.rejectedNotes" :key="idx">{{ note }}</li>
          </ul>
        </div>

        <div class="flex items-center justify-between">
          <span class="text-xs text-slate-500">
            共识别出 {{ extractResult.fieldCount }} 项（耗时 {{ extractResult.latencyMs }} ms）
          </span>
          <el-button v-if="extractResult.fields?.length" link size="small" type="primary"
                     @click="applyAllExtractFields">全部填入
          </el-button>
        </div>

        <el-table :data="extractResult.fields || []" border max-height="320" size="small">
          <el-table-column label="字段" width="100">
            <template #default="{ row }">
              <span class="text-xs font-medium text-slate-700">{{ row.fieldLabel }}</span>
            </template>
          </el-table-column>
          <el-table-column label="抽取内容" min-width="240">
            <template #default="{ row }">
              <div class="whitespace-pre-wrap text-sm text-slate-900">{{ row.value }}</div>
            </template>
          </el-table-column>
          <el-table-column label="来源" width="96">
            <template #default="{ row }">
              <el-tag :type="row.source === 'HARD_RULE' ? 'success' : 'info'" effect="plain" size="small">
                {{ row.source === 'HARD_RULE' ? '原文切分' : 'AI 搬运' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="原文依据" min-width="200">
            <template #default="{ row }">
              <div class="whitespace-pre-wrap text-xs text-slate-400">{{ row.evidence || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="90">
            <template #default="{ row }">
              <el-button v-if="extractAppliedFields.includes(row.field)" disabled link size="small">
                已填入
              </el-button>
              <el-button v-else link size="small" type="primary" @click="applyExtractField(row)">
                填入
              </el-button>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-center text-xs text-slate-400">
              没有识别出可搬运的内容。可以试试按「主诉：」「现病史：」这样的格式补上字段标签。
            </div>
          </template>
        </el-table>
      </template>
    </div>

    <template #footer>
      <el-button @click="extractDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 病历草拟（P1-3，只草拟现病史） -->
  <el-dialog v-model="draftDialogVisible" :close-on-click-modal="false" destroy-on-close title="AI 草拟现病史"
             width="700px">
    <div v-if="draftLoading" class="py-10 text-center text-sm text-slate-500">
      正在根据主诉与查体整理草稿……
    </div>
    <div v-else-if="draftResult" class="space-y-3">
      <div class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-600">
        草稿只依据你已经填写的主诉、查体与体征，<b>不会补写你没提到的内容</b>（尤其是「无发热」这类阴性描述）。
        系统不生成诊断，也不生成处理意见。
      </div>

      <div v-if="draftResult.degraded"
           class="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-700">
        <b>本次未经过大模型</b>：{{ draftResult.degradeReason }}
      </div>

      <div v-if="draftResult.presentIllness" class="rounded-lg border border-slate-200 p-3">
        <div class="mb-1 text-xs font-medium text-slate-500">草稿正文</div>
        <div class="whitespace-pre-wrap text-sm leading-6 text-slate-900">{{ draftResult.presentIllness }}</div>
      </div>
      <div v-else class="rounded-lg border border-slate-200 p-3 text-sm text-slate-500">
        这次没有生成草稿。
      </div>

      <div v-if="draftResult.missingPoints?.length">
        <div class="mb-1 text-xs font-medium text-slate-500">还缺这些信息（补充后可重新草拟）</div>
        <div class="flex flex-wrap gap-2">
          <el-tag v-for="(point, idx) in draftResult.missingPoints" :key="idx" effect="plain" size="small"
                  type="warning">
            {{ point }}
          </el-tag>
        </div>
      </div>

      <div v-if="draftResult.summary" class="text-xs text-slate-400">{{ draftResult.summary }}</div>
    </div>

    <template #footer>
      <el-button :disabled="!draftResult?.presentIllness" type="primary" @click="applyDraft">
        填入现病史
      </el-button>
      <el-button @click="draftDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 语音口述（G-14）：录音 → ASR 转写 → 可编辑 → 喂 emr_draft 整理 -->
  <el-dialog v-model="voiceDialogVisible" destroy-on-close title="语音口述现病史" width="560px"
             @closed="closeVoiceDialog">
    <div class="space-y-3">
      <div class="rounded-lg border border-slate-200 bg-slate-50 px-3 py-2 text-xs text-slate-600">
        录下口述内容（单次最长 {{ VOICE_MAX_SECONDS }} 秒，到时自动停止），转写文本可修改后再填入。
        转写只是语音转文字，不会补写没说到的内容；最终现病史由医生逐字确认。
      </div>
      <div class="flex items-center gap-3">
        <el-button v-if="!voiceRecording" :disabled="voiceTranscribing" type="danger" @click="startVoiceRecord">
          开始录音
        </el-button>
        <el-button v-else type="warning" @click="stopVoiceRecord">
          停止录音（{{ voiceSecondsText }}）
        </el-button>
        <span v-if="voiceRecording" class="text-sm text-red-600">录音中…最长 {{ VOICE_MAX_SECONDS }} 秒</span>
        <span v-else-if="voiceTranscribing" class="text-sm text-slate-500">正在转写，请稍候…</span>
      </div>
      <el-input v-model="voiceText" :disabled="voiceTranscribing" :rows="6"
                placeholder="录音停止后转写文本显示在此，也可手动修改"
                type="textarea"/>
    </div>
    <template #footer>
      <el-button :disabled="!voiceText.trim() || voiceTranscribing" plain @click="applyVoiceToField">
        填入现病史
      </el-button>
      <el-button :disabled="!voiceText.trim() || voiceTranscribing" type="primary" @click="draftFromVoice">
        AI 整理为现病史
      </el-button>
      <el-button @click="voiceDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 结诊开方医师手写签名（电子证书签名由后端自动完成，本板仅视觉落款） -->
  <RxSignDialog
      v-model="rxSignDialogVisible"
      :doctor-name="currentPatient?.doctorName || ''"
      :prescription-count="pendingSubmitData ? pendingSubmitData.prescriptions.length : 0"
      @cancel="onRxSignCancel"
      @confirm="onRxSigned"
  />

  <!-- ================= 开住院证（门诊 → 住院的入口） ================= -->
  <DoctorWorkstationAdmissionAdd
      v-model="orderDialogVisible"
      :diagnosis-code="recordForm.diagnosisCode"
      :diagnosis-name="recordForm.diagnosisName"
      :patient="currentPatient"
      :patient-detail="patientDetail"/>

  <!-- ================= 我开的住院证（本人开证记录 + 自助作废） ================= -->
  <DoctorWorkstationAdmissionList v-model="myAdmissionListVisible"/>

  <!-- 模板维护（五类模板统一入口：左侧分类，右侧加载列表；「套用」把模板内容落到当前病历/处方/申请） -->
  <el-dialog v-model="templateMaintainVisible" destroy-on-close title="模板维护" width="760px">
    <div class="flex gap-4" style="height: 460px;">
      <div class="w-36 shrink-0 space-y-1 border-r border-slate-200 pr-3">
        <div v-for="cat in templateMaintainTabs" :key="cat.key"
             :class="activeTemplateTab === cat.key
               ? 'bg-[#1269B5] text-white'
               : 'text-slate-600 hover:bg-slate-100'"
             class="cursor-pointer rounded px-3 py-2 text-sm font-medium transition-colors"
             @click="switchTemplateTab(cat.key)">
          {{ cat.label }}
        </div>
      </div>
      <div class="min-h-0 flex-1 overflow-y-auto custom-scrollbar">
        <!-- 常用诊断 -->
        <div v-if="activeTemplateTab === 'diag'" class="space-y-1.5">
          <div v-if="myDiagTemplates.length === 0" class="py-10 text-center text-sm text-slate-400">暂无常用诊断模板
          </div>
          <div v-for="(item, idx) in myDiagTemplates" :key="item.id || idx"
               class="flex items-center justify-between rounded-lg border border-slate-100 px-3 py-2">
            <div>
              <div class="text-sm font-medium text-slate-800">{{ item.icdName }}</div>
              <div class="text-xs text-slate-400">{{ item.icdCode }}
                <template v-if="item.icdCategory"> · {{ item.icdCategory }}</template>
              </div>
            </div>
            <div class="flex items-center">
              <el-button link size="small" type="primary" @click="handleQuickSelectDiag(item)">套用</el-button>
              <el-button link size="small" type="danger" @click="handleDeleteDiagTemplate(idx)">删除</el-button>
            </div>
          </div>
        </div>
        <!-- 处方模板 -->
        <div v-else-if="activeTemplateTab === 'rx'" class="space-y-1.5">
          <div v-if="myRxTemplates.length === 0" class="py-10 text-center text-sm text-slate-400">暂无处方模板</div>
          <div v-for="(tpl, idx) in myRxTemplates" :key="tpl.id"
               class="flex items-center justify-between rounded-lg border border-slate-100 px-3 py-2">
            <div class="flex items-center gap-2">
              <span class="text-sm font-medium text-slate-800">{{ tpl.templateName }}</span>
              <el-tag v-if="tpl.prescriptionType" size="small" type="info">
                {{ prescriptionTypeLabel(tpl.prescriptionType) }}
              </el-tag>
            </div>
            <div class="flex items-center">
              <el-button link size="small" type="primary" @click="handleApplyRxTemplate(tpl)">套用</el-button>
              <el-button link size="small" type="danger" @click="handleDeleteRxTemplate(idx)">删除</el-button>
            </div>
          </div>
        </div>
        <!-- 药品套餐 -->
        <div v-else-if="activeTemplateTab === 'package'" class="space-y-1.5">
          <div v-if="myPackages.length === 0" class="py-10 text-center text-sm text-slate-400">暂无药品套餐</div>
          <div v-for="(pkg, idx) in myPackages" :key="pkg.id"
               class="flex items-center justify-between rounded-lg border border-slate-100 px-3 py-2">
            <span class="text-sm font-medium text-slate-800">{{ pkg.packageName }}</span>
            <div class="flex items-center">
              <el-button link size="small" type="primary" @click="handleApplyPackage(pkg)">套用</el-button>
              <el-button link size="small" type="danger" @click="handleDeletePackage(idx)">删除</el-button>
            </div>
          </div>
        </div>
        <!-- 检查模板 -->
        <div v-else-if="activeTemplateTab === 'inspection'" class="space-y-1.5">
          <div v-if="myInspectionTemplates.length === 0" class="py-10 text-center text-sm text-slate-400">
            暂无检查申请模板
          </div>
          <div v-for="(tpl, idx) in myInspectionTemplates" :key="tpl.id"
               class="flex items-center justify-between rounded-lg border border-slate-100 px-3 py-2">
            <span class="text-sm font-medium text-slate-800">{{ tpl.templateName }}</span>
            <div class="flex items-center">
              <el-button link size="small" type="primary" @click="handleAppendInspectionTemplate(tpl)">套用</el-button>
              <el-button link size="small" type="danger" @click="handleDeleteInspectionTemplate(idx)">删除</el-button>
            </div>
          </div>
        </div>
        <!-- 检验模板 -->
        <div v-else class="space-y-1.5">
          <div v-if="myLaboratoryTemplates.length === 0" class="py-10 text-center text-sm text-slate-400">
            暂无检验申请模板
          </div>
          <div v-for="(tpl, idx) in myLaboratoryTemplates" :key="tpl.id"
               class="flex items-center justify-between rounded-lg border border-slate-100 px-3 py-2">
            <span class="text-sm font-medium text-slate-800">{{ tpl.templateName }}</span>
            <div class="flex items-center">
              <el-button link size="small" type="primary" @click="handleAppendLaboratoryTemplate(tpl)">套用</el-button>
              <el-button link size="small" type="danger" @click="handleDeleteLaboratoryTemplate(idx)">删除</el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import {computed, onMounted, onUnmounted, reactive, ref, watch} from 'vue';
import {useRoute} from 'vue-router';
import {patientAvatarTone, patientGenderText} from '@/lib/patientGender';
import {
  ArrowLeft,
  ArrowRight,
  CircleCheck,
  Clock,
  Coin,
  Delete,
  Document,
  InfoFilled,
  Loading,
  MagicStick,
  Plus,
  Printer,
  Reading,
  RefreshLeft,
  RefreshRight,
  Search,
  Tickets,
  VideoPause,
  VideoPlay,
  Warning
} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import PatientBriefBar from '@/components/his/PatientBriefBar.vue';
import {REVISIT_SOURCE} from '@/lib/revisitPolicy';
import PatientDetailDialog from '@/components/his/PatientDetailDialog.vue';
import {useCurrentPatientStore} from '@/stores/currentPatient';
import PageActionBar from '@/components/his/PageActionBar.vue';
import OrderPanel from '@/components/his/OrderPanel.vue';
import DoctorWorkstationAdmissionAdd from './DoctorWorkstationAdmissionAdd.vue';
import DoctorWorkstationAdmissionList from './DoctorWorkstationAdmissionList.vue';
import RxSignDialog from './RxSignDialog.vue';
import {
  callNextQueue,
  callPatient,
  createRevisitRegistration,
  estimateInsurance,
  getCurrentDoctorStatus,
  getQueueStats,
  getTodayQueueList,
  getTriageByQueueId,
  overdueQueue,
  recallPatient,
  rejoinQueue,
  setDoctorStatus
} from '@/api/appoint';
import {
  getDictDataMapList,
  getDrugSelectList,
  getEmployeeCurrent,
  getInspectionSelectList,
  getLaboratorySelectList,
  getPatientTagListAll,
  predictIcd10,
  searchIcd10,
  searchInspectionItem,
  searchLaboratoryItem
} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {getByRegistId, getEmrRecordList, printVisitDocs} from '@/api/emr';
import {draftEmrText, extractEmrText, predictIcd10 as predictIcd10Ai, transcribeVoice} from '@/api/ai';
import {localDateStr, shortQueueNo} from '@/lib/utils';
import {QUEUE_STATUS, queueVisitBadgeOf} from '@/lib/statusColor';
import {handleEmrSubmitVeto} from '@/lib/emrSubmit';
import {
  deleteDiagTemplate,
  deleteDrugPackage,
  deleteInspectionApply,
  deleteInspectionTemplate,
  deleteLaboratoryApply,
  deleteLaboratoryTemplate,
  deleteRxTemplate,
  getDiagTemplates,
  getDrugPackageDetail,
  getDrugPackages,
  getInspectionApplyList,
  getInspectionTemplates,
  getLaboratoryApplyList,
  getLaboratoryTemplates,
  getPrescriptionList,
  getPrevisitByRegist,
  getRxTemplateDetail,
  getRxTemplates,
  rationalCheckDraft,
  revokeMedicalRecord,
  saveDiagTemplates,
  saveDrugPackage,
  saveInspectionTemplate,
  saveLaboratoryTemplate,
  saveMedicalRecord,
  saveRxTemplate,
  submitMedicalRecord
} from '@/api/doctor';
import {getStockList} from '@/api/pharmacy';
import {
  addPatientTag,
  getPatientDetail,
  getPatientHealthProfile,
  getPatientTags,
  removePatientTag
} from '@/api/patient';
import {getInspectionDetail, getLaboratoryDetail, getLaboratoryRecordList} from '@/api/medicaltech';
import {getChronicActiveByPatient} from '@/api/chronic';

const GENDER_TONE_CLASS = {
  male: 'bg-blue-50 text-blue-600',
  female: 'bg-pink-50 text-pink-600',
  unknown: 'bg-slate-100 text-slate-500'
};
const queueStatusMap = QUEUE_STATUS;
const loading = ref(false);
const patientDataPending = ref(0);
const patientDataLoading = computed(() => patientDataPending.value > 0);
const trackPatientLoad = (p) => {
  patientDataPending.value++;
  // 各 loader 内部都自己吞异常，这里只负责收尾计数
  return p.finally(() => {
    patientDataPending.value = Math.max(0, patientDataPending.value - 1);
  });
};
const selectedDeptId = ref(null);
const queueList = ref([]);
const currentPatientStore = useCurrentPatientStore();
const route = useRoute();
const queueLoaded = ref(false);
let handledSwitchAt = 0;
let pendingSwitch = null;
let enteredWithTarget = false;
let urlIntent = false;
const QUEUE_STATUS_ORDER = {3: 0, 2: 1, 4: 2, 5: 3, 6: 4};
/** 队列行比较器：先状态优先级，同状态按到达时间，再按序号（保证排序稳定） */
const compareQueueRow = (a, b) => {
  const oa = QUEUE_STATUS_ORDER[a.queueStatus] ?? 5;
  const ob = QUEUE_STATUS_ORDER[b.queueStatus] ?? 5;
  if (oa !== ob)
    return oa - ob;
  if (a.arriveTime && b.arriveTime) {
    const ta = new Date(a.arriveTime).getTime();
    const tb = new Date(b.arriveTime).getTime();
    if (ta !== tb)
      return ta - tb;
  }
  return (a.sequenceNo || 0) - (b.sequenceNo || 0);
};
const pickQueueRow = (rows) => {
  if (!rows || !rows.length)
    return null;
  return [...rows].sort(compareQueueRow)[0];
};
const applyPendingSwitch = () => {
  const p = pendingSwitch;
  if (!p)
    return;
  pendingSwitch = null;
  const notify = currentPatientStore.consumeSwitchNotice() || urlIntent;
  urlIntent = false;
  // 同一患者今天多条队列时，挑「该接的那一条」而不是数组第一条（见 pickQueueRow）
  const target = pickQueueRow(queueList.value.filter((q) => String(q.patientId) === String(p.id)));
  if (target) {
    selectPatient(target);
    if (notify) {
      ElMessage.success(`已切换接诊患者：${target.patientName || p.patientName}`);
    }
  } else {
    if (notify) {
      // 用户主动搜的人不在队列：要说清楚，并把这次搜索交回详情框（不让它落空）
      ElMessage.warning(`${p.patientName || '该患者'} 不在您的今日候诊队列中，已改为展示患者档案`);
      currentPatientStore.requestDetail(p.id);
    }
    if (currentPatient.value) {
      currentPatientStore.syncPatient(toStorePatient(currentPatient.value));
    } else {
      currentPatientStore.syncClear();
    }
  }
  enteredWithTarget = false;
};
const toStorePatient = (row) => ({
  id: String(row.patientId),
  patientName: row.patientName,
  patientNo: row.patientNo,
  gender: row.gender,
  age: row.age,
});
const consumeSwitch = () => {
  const p = currentPatientStore.patient;
  const ts = currentPatientStore.switchedAt;
  if (!p || !ts || ts === handledSwitchAt)
    return;
  handledSwitchAt = ts;
  pendingSwitch = p;
  if (queueLoaded.value)
    applyPendingSwitch();
};
watch(() => currentPatientStore.switchedAt, consumeSwitch);
// ========== 慢病长处方资格（患者切换即查；服务端才是最终权威，此处只为开关 disabled 预检） ==========
const chronicQualified = ref(false);
const chronicRecords = ref([]);
const chronicDiseaseText = computed(() => chronicRecords.value.map((r) => r.diseaseName).filter(Boolean).join('、'));
const loadChronicQualification = async (patientId) => {
  chronicQualified.value = false;
  chronicRecords.value = [];
  if (!patientId) return;
  try {
    const res = await getChronicActiveByPatient(patientId);
    chronicRecords.value = res.data || [];
    chronicQualified.value = chronicRecords.value.length > 0;
  } catch (e) {
    // 资格查询失败不当有资格（开关禁用）；真漏判由服务端结诊时拦，不假装能开
    chronicQualified.value = false;
  }
};
const currentPatient = ref(null);
const activeTab = ref('record');
const queueSearch = ref('');
const queueFilter = ref('all');
const userInfo = ref({});
const stats = ref({waiting: 0, called: 0, inProgress: 0});
// 今日待办
const todoList = ref([]);
const todoStats = ref({pendingReview: 0, pendingReport: 0, pendingFollowUp: 0, pendingConsult: 0});
const todoSearch = ref('');
const filteredTodoList = computed(() => {
  if (!todoSearch.value)
    return todoList.value;
  const kw = todoSearch.value.toLowerCase();
  return todoList.value.filter((item) => item.title?.toLowerCase().includes(kw) || item.content?.toLowerCase().includes(kw));
});
const currentCalledPatient = computed(() => {
  return pickQueueRow(queueList.value.filter((q) => q.queueStatus === 3));
});
const waitingCount = computed(() => {
  return queueList.value.filter((q) => q.queueStatus === 2).length;
});
const nextWaiting = computed(() => {
  const waiting = queueList.value.filter((q) => q.queueStatus === 2);
  if (!waiting.length)
    return null;
  return [...waiting].sort((a, b) => {
    const la = a.triageLevel ?? 4;
    const lb = b.triageLevel ?? 4;
    if (la !== lb)
      return la - lb;
    return (a.sequenceNo || 0) - (b.sequenceNo || 0);
  })[0];
});
const formatArriveTime = (time) => {
  if (!time)
    return '';
  const d = new Date(time);
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
};
const calcWaitMinutes = (row) => {
  if (row.waitDuration != null)
    return row.waitDuration;
  if (!row.arriveTime)
    return 0;
  const arrive = new Date(row.arriveTime);
  const now = row.queueStatus === 3 && row.startTime ? new Date(row.startTime) : new Date();
  return Math.max(0, Math.floor((now.getTime() - arrive.getTime()) / 60000));
};
const waitDurationClass = (minutes) => {
  if (minutes >= 30)
    return 'text-red-500 font-medium';
  if (minutes >= 15)
    return 'text-amber-500';
  return 'text-slate-400';
};
// 医保信息
const insuranceInfo = ref(null);
const loadInsuranceInfo = async () => {
  if (!currentPatient.value) {
    insuranceInfo.value = null;
    return;
  }
  // 根据所有处方实际金额计算
  const drugTotal = prescriptionList.value.reduce((sum, p) => {
    return sum + (p.details || []).reduce((s, d) => s + (d.amount || 0), 0);
  }, 0);
  // 计算检查申请费用（模式A：本地编辑即计入预估 —— 未落库行(r.id 为空)与「已提交(1)」计入，
  // 已缴费(2)的不重复计入，已取消(6)的不算）
  const inspectionTotal = inspectionRecords.value
      .filter((r) => r.inspectionItemId && (r.applyStatus === 1 || !r.id))
      .reduce((sum, r) => sum + (r.price || 0), 0);
  // 计算检验申请费用（口径同检查）
  const laboratoryTotal = laboratoryRecords.value
      .filter((r) => r.laboratoryItemId && (r.applyStatus === 1 || !r.id))
      .reduce((sum, r) => sum + (r.price || 0), 0);
  try {
    const res = await estimateInsurance({
      settlementType: currentPatient.value.settlementType || 1,
      medicalInsuranceType: currentPatient.value.medicalInsuranceType || '',
      drugTotal: drugTotal,
      inspectionTotal: inspectionTotal,
      laboratoryTotal: laboratoryTotal,
    });
    insuranceInfo.value = res.data || null;
  } catch (error) {
    console.error('加载医保信息失败:', error);
    insuranceInfo.value = null;
  }
};
// 过滤后的队列
const filteredQueue = computed(() => {
  let list = queueList.value;
  if (queueSearch.value) {
    const kw = queueSearch.value.toLowerCase();
    list = list.filter((q) => q.patientName?.toLowerCase().includes(kw) ||
        q.registNo?.toLowerCase().includes(kw) ||
        q.queueNo?.toLowerCase().includes(kw));
  }
  // 队列状态口径以 QueueStatusEnum 为准：2 候诊中 / 3 就诊中（不是挂号的 1 已挂号）
  if (queueFilter.value === 'waiting')
    list = list.filter((q) => q.queueStatus === 2);
  else if (queueFilter.value === 'consulting')
    list = list.filter((q) => q.queueStatus === 3);
  // 门急诊分开：急诊号走急诊工作站，门诊工作台不再提供急诊筛选（急诊标记的旧申请单仍显示，见行内急诊 tag）
  // 排序与「该接哪一条」（pickQueueRow）共用 compareQueueRow，口径只有一处：
  // 就诊中 > 候诊中 > 已完成 > 已过号 > 已退号。
  // 两处各写一套的后果是「列表第一行排的是复诊，顶栏搜索却选中了初诊那条」。
  return [...list].sort(compareQueueRow);
});
// ========== 病历表单 ==========
const recordForm = reactive({
  id: null,
  recordNo: '',
  patientId: null,
  patientNo: '',
  patientName: '',
  gender: 0,
  age: 0,
  registId: null,
  registNo: '',
  visitDate: '',
  deptId: null,
  deptName: '',
  doctorId: null,
  doctorName: '',
  chiefComplaint: '',
  presentIllness: '',
  pastHistory: '',
  personalHistory: '',
  familyHistory: '',
  marriageHistory: '',
  allergyHistory: '',
  temperature: '',
  pulse: '',
  respiration: '',
  systolicPressure: '',
  diastolicPressure: '',
  generalCondition: '',
  skinMucosa: '',
  headNeck: '',
  chestLung: '',
  heart: '',
  abdomen: '',
  spineLimbs: '',
  nervousSystem: '',
  specialistExam: '',
  auxiliaryExam: '',
  diagnosis: '',
  diagnosisCode: '',
  diagnosisName: '',
  treatmentPlan: '',
  guidePdfPath: '',
  rxPdfPath: '',
  recordStatus: 1,
  reviewStatus: 0,
});
// 体格检查分节字段清单（复制上次查体只搬查体所见，生命体征列在排除项里，见 handleCopyLastExam）
const EXAM_FIELDS = ['temperature', 'pulse', 'respiration', 'systolicPressure', 'diastolicPressure',
  'generalCondition', 'skinMucosa', 'headNeck', 'chestLung', 'heart', 'abdomen', 'spineLimbs', 'nervousSystem',
  'specialistExam'];
// 病历左侧段落定位（tab 样式）：主诉病史 / 体格检查 / 诊断治疗。
// 点击滚动到对应段并置选中；病历区滚动时按当前可见段自动高亮（onRecordScroll）。
const recordNavTabs = [
  {key: 'chief', label: '主诉病史', title: '主诉与病史'},
  {key: 'exam', label: '体格检查', title: '体格检查'},
  {key: 'diag', label: '诊断治疗', title: '诊断与治疗'},
];
const activeRecordSection = ref('chief');
// 指示条位置：tab 高 56px（h-14），依次 0 / 56 / 112
const activeTabIndex = computed(() => {
  const i = recordNavTabs.findIndex((t) => t.key === activeRecordSection.value);
  return i < 0 ? 0 : i;
});
const chiefSection = ref(null);
const examSection = ref(null);
const diagSection = ref(null);
const recordScrollEl = ref(null);
const scrollToRecordSection = (key) => {
  activeRecordSection.value = key;
  const el = key === 'chief' ? chiefSection.value : key === 'exam' ? examSection.value : diagSection.value;
  if (el) el.scrollIntoView({behavior: 'smooth', block: 'start'});
};
// 滚动联动：当前高亮的 tab 跟随可见段。判据 = 最后一个「段顶 <= 容器顶部 + 120px」的段
//（120px 近似半个首屏，段滚过容器的视窗上沿一小段就切换，避免贴边才切换的顿挫感）。
// 兜底：滚到最底部时强制置最后一个 tab —— 内容短/未撑满时诊断段顶永远到不了 120px 阈值，
// 不兜底的话用户滚到底却高亮还停在体格检查。
const onRecordScroll = () => {
  const container = recordScrollEl.value;
  if (!container) return;
  if (container.scrollTop + container.clientHeight >= container.scrollHeight - 4) {
    if (activeRecordSection.value !== 'diag') activeRecordSection.value = 'diag';
    return;
  }
  const containerTop = container.getBoundingClientRect().top;
  const tops = [chiefSection.value, examSection.value, diagSection.value]
      .map((s) => (s ? s.getBoundingClientRect().top : Infinity));
  const keys = ['chief', 'exam', 'diag'];
  let current = keys[keys.length - 1];
  for (let i = tops.length - 1; i >= 0; i--) {
    if (tops[i] <= containerTop + 120) {
      current = keys[i];
      break;
    }
  }
  if (activeRecordSection.value !== current) {
    activeRecordSection.value = current;
  }
};
// 复制上次查体：真实 HIS 复诊高频动作。只搬 EXAM_FIELDS（查体所见），
// 生命体征是当次实测数据、质控禁止拷贝，所以不在复制范围内。
// 一键正常：查体所见文本字段统一填「正常」，同时把生命体征填一套成人安静状态标准均值。
// 数值项不能写「正常」，但留空等于没查——真实 HIS 的一键正常就是给这套预设值让医生核对，
// 点完再按实测改。覆盖语义与文本字段一致（无条件覆盖，含分诊带入值；医生改后分诊角标自动消）。
const VITAL_SIGN_NORMAL = {
  temperature: '36.5',
  pulse: '72',
  respiration: '18',
  systolicPressure: '120',
  diastolicPressure: '80',
};
const VITAL_SIGN_KEYS = Object.keys(VITAL_SIGN_NORMAL);
const handleExamAllNormal = () => {
  const textFields = EXAM_FIELDS.filter((k) => !VITAL_SIGN_KEYS.includes(k));
  textFields.forEach((k) => {
    recordForm[k] = '正常';
  });
  VITAL_SIGN_KEYS.forEach((k) => {
    recordForm[k] = VITAL_SIGN_NORMAL[k];
  });
  ElMessage.success(`体格检查已一键设为正常（查体所见 ${textFields.length} 项 + 生命体征 ${VITAL_SIGN_KEYS.length} 项），请核对`);
};
const copyExamLoading = ref(false);
const handleCopyLastExam = async () => {
  const patientId = currentPatient.value?.patientId;
  if (!patientId)
    return;
  copyExamLoading.value = true;
  try {
    const res = await getEmrRecordList({patientId});
    // 后端按 createTime 倒序；排除本次病历（同一次挂号产生的记录）
    const last = (res.data || []).find((r) => String(r.registId) !== String(recordForm.registId ?? '')
        && String(r.id) !== String(recordForm.id ?? ''));
    if (!last) {
      ElMessage.info('该患者没有可复制的既往查体');
      return;
    }
    const copied = EXAM_FIELDS.filter((k) => !['temperature', 'pulse', 'respiration', 'systolicPressure', 'diastolicPressure'].includes(k) && !!last[k]);
    if (copied.length === 0) {
      ElMessage.info('上次病历未填写查体所见');
      return;
    }
    copied.forEach((k) => {
      recordForm[k] = last[k];
    });
    ElMessage.success(`已复制 ${last.visitDate || '上次'} 的查体所见（${copied.length} 项），请核对修改`);
  } finally {
    copyExamLoading.value = false;
  }
};
// ===== 病史带入（既往史/个人史/婚育史/家族史/过敏史）=====
// 口径：复诊把历史病史预填进表单，医生核对后改。国标《病历书写基本规范》要求复诊病历呈现
// 「病史」；威海市立医院等标杆做法是预问诊「一诉五史」一键导入，同此思路。
// 优先级：本次挂号已有病历 > 上次门诊病历原文 > 患者健康档案投影（明细拼的文本）。
// 三条纪律：① 只填空字段，医生已写的一个字都不覆盖；② 带入值带来源角标，医生一改内容
//（≠带入原值）角标即消失——角标必须始终说真话；③ 不回写患者主档，档案明细的权威入口是
// 健康档案维护页，避免 copy-forward 把未核对的内容沉淀成档案。
const HISTORY_KEYS = ['pastHistory', 'personalHistory', 'marriageHistory', 'familyHistory', 'allergyHistory'];
// key → 'last'（上次病历）| 'profile'（患者档案）；historyPrefillValue 记带入原值供比对
const historySources = reactive({});
const historyPrefillValue = {};
const historySourceLabel = (key) => (historySources[key] === 'last' ? '上次病历' : '患者档案');
const clearPrefilled = (key) => {
  recordForm[key] = '';
  delete historySources[key];
  delete historyPrefillValue[key];
};
// 医生改动带入值 → 角标消失（只比较「是否还是带入时的原值」，医生自己填的从无角标）
watch(HISTORY_KEYS.map((k) => () => recordForm[k]), () => {
  HISTORY_KEYS.forEach((k) => {
    if (historySources[k] && recordForm[k] !== historyPrefillValue[k]) {
      delete historySources[k];
      delete historyPrefillValue[k];
    }
  });
});
const prefillHistories = async (row) => {
  const patientId = row?.patientId;
  if (!patientId) return;
  // 上次病历 + 患者档案并发拉；单个失败不影响接诊（带入是增强，不是关键路径）
  const [lastRes, profileRes] = await Promise.all([
    getEmrRecordList({patientId}).catch(() => null),
    getPatientHealthProfile(patientId).catch(() => null),
  ]);
  // 排除本次就诊（同 registId / 同病历 id），与「复制上次查体」同口径
  const last = (lastRes?.data || []).find((r) => String(r.registId) !== String(recordForm.registId ?? '')
      && String(r.id) !== String(recordForm.id ?? ''));
  const profile = profileRes?.data || null;
  // 婚育史/个人史档案侧暂无明细组，只从上次病历带入；既往/家族/过敏史档案有投影可兜底
  const candidates = {
    pastHistory: [last?.pastHistory, profile?.medicalHistoryText],
    personalHistory: [last?.personalHistory, null],
    marriageHistory: [last?.marriageHistory, null],
    familyHistory: [last?.familyHistory, profile?.familyHistoryText],
    allergyHistory: [last?.allergyHistory, profile?.allergyHistoryText],
  };
  HISTORY_KEYS.forEach((key) => {
    if (recordForm[key]) return; // 本次已写/已带入，不动
    const [lastVal, profileVal] = candidates[key];
    const val = lastVal || profileVal;
    if (val) {
      recordForm[key] = val;
      historyPrefillValue[key] = val;
      historySources[key] = lastVal ? 'last' : 'profile';
    }
  });
};
// ===== 分诊带入（生命体征 + 主诉）=====
// 口径：分诊台（预检分诊）登记的**当次**生命体征与主诉，接诊时自动带入病历的
// 体格检查-生命体征区与主诉（与「复制上次查体」不同：分诊值是本次就诊实测，质控允许）。
// 纪律与 prefillHistories 一致：① 只填空字段，病历已保存/医生已写的一律不覆盖；
// ② 带入值带「分诊」角标，医生一改内容角标即消失；③ 不回写分诊记录（分诊记录只增不改）。
const TRIAGE_PREFILL_KEYS = [
  {formKey: 'temperature', triageKey: 'temperature', label: '体温'},
  {formKey: 'pulse', triageKey: 'pulse', label: '脉搏'},
  {formKey: 'respiration', triageKey: 'respiration', label: '呼吸'},
  {formKey: 'systolicPressure', triageKey: 'systolicBp', label: '收缩压'},
  {formKey: 'diastolicPressure', triageKey: 'diastolicBp', label: '舒张压'},
];
// formKey → 分诊带入原值（角标比对用，与 historyPrefillValue 同模式）
const triagePrefillValue = {};
const triageSources = reactive({});
const triagePrefilled = computed(() => Object.keys(triageSources).length > 0);
// 体征：医生改动带入值 → 角标消失（只比较「是否还是带入时的原值」）
watch(TRIAGE_PREFILL_KEYS.map((m) => () => recordForm[m.formKey]), () => {
  TRIAGE_PREFILL_KEYS.forEach((m) => {
    if (triageSources[m.formKey] && recordForm[m.formKey] !== triagePrefillValue[m.formKey]) {
      delete triageSources[m.formKey];
      delete triagePrefillValue[m.formKey];
    }
  });
});
// 主诉角标：独立标记（与体征分开，医生改了主诉即消失）
const triageChiefComplaintValue = reactive({value: ''});
watch(() => recordForm.chiefComplaint, (v) => {
  if (triageChiefComplaintValue.value && v !== triageChiefComplaintValue.value) {
    triageChiefComplaintValue.value = '';
  }
});
const loadTriagePrefill = async (queueId) => {
  // 换患者先清角标（本次没有带入则不显示；带入后由上面的 watch 清除）
  TRIAGE_PREFILL_KEYS.forEach((m) => {
    delete triageSources[m.formKey];
    delete triagePrefillValue[m.formKey];
  });
  triageChiefComplaintValue.value = '';
  if (!queueId) return;
  try {
    // 分诊卡回显（当前生效值 + 历史）：只取 latest（本次就诊最新一次分诊登记）
    const res = await getTriageByQueueId(queueId);
    const latest = res?.data?.latest;
    if (!latest) return;
    TRIAGE_PREFILL_KEYS.forEach((m) => {
      const v = latest[m.triageKey];
      // 只填空字段：病历已有内容（含刚加载的已保存病历）一律不覆盖
      if (v !== null && v !== undefined && v !== '' && !recordForm[m.formKey]) {
        const val = String(v);
        recordForm[m.formKey] = val;
        triagePrefillValue[m.formKey] = val;
        triageSources[m.formKey] = true;
      }
    });
    if (latest.chiefComplaint && !recordForm.chiefComplaint) {
      recordForm.chiefComplaint = latest.chiefComplaint;
      triageChiefComplaintValue.value = latest.chiefComplaint;
    }
  } catch (error) {
    // 分诊数据读取失败不阻断接诊，仅失去自动带入（医生可手填）
  }
};
const inspectionRecords = ref([]);
const laboratoryRecords = ref([]);
// ========== 处方表单 ==========
const prescriptionForm = reactive({
  patientId: null,
  patientNo: '',
  patientName: '',
  gender: 0,
  age: 0,
  registId: null,
  registNo: '',
  deptId: null,
  deptName: '',
  doctorId: null,
  doctorName: '',
  prescriptionType: 1,
  diagnosis: '',
  usageInstruction: '',
  // 中药饮片方专属，挂在处方头（一张方一个剂数，不是每味药一个）
  doseCount: 7,
  decoctFlag: null,
  // 慢病长处方（处方级）：患者有已认定慢病档案才允许开；天数 1~90
  isLongPrescription: false,
  longPrescriptionDays: 28,
  details: [],
});
const newDrug = reactive({
  drugId: null,
  drugCode: '',
  drugName: '',
  genericName: '',
  specification: '',
  dosageForm: '',
  unit: '盒',
  quantity: 1,
  price: 0,
  usageDosage: '',
  frequency: '一日三次',
  route: '口服',
  duration: 7,
  singleDosage: '',
});
// ========== 检查申请表单 ==========
const inspectionForm = reactive({
  patientId: null,
  patientNo: '',
  patientName: '',
  gender: 0,
  age: 0,
  registId: null,
  registNo: '',
  deptId: null,
  deptName: '',
  doctorId: null,
  doctorName: '',
  inspectionItemId: null,
  inspectionItemName: '',
  bodyPart: '',
  inspectionPurpose: '',
  preparation: '',
  clinicalDiagnosis: '',
  isEmergency: 0,
});
// ========== 检验申请表单 ==========
const laboratoryForm = reactive({
  patientId: null,
  patientNo: '',
  patientName: '',
  gender: 0,
  age: 0,
  registId: null,
  registNo: '',
  deptId: null,
  deptName: '',
  doctorId: null,
  doctorName: '',
  laboratoryItemId: null,
  laboratoryItemName: '',
  specimenType: '血液',
  laboratoryPurpose: '',
  clinicalDiagnosis: '',
  isFasting: 0,
  isEmergency: 0,
});
// ========== 处方列表管理 ==========
const prescriptionList = ref([]);
const currentPrescriptionIdx = ref(0);
const currentPrescriptionType = ref(1);
const currentPrescription = computed(() => {
  return prescriptionList.value.find(p => p.prescriptionType === currentPrescriptionType.value) || null;
});
const prescriptionTypeLabel = (type) => {
  const map = {1: '西药', 2: '中成药', 3: '中药饮片'};
  return map[type] || '未知';
};
// ========== 中药饮片：剂数在处方头、克数按「每剂克数 × 剂数」算 ==========
// 煎法与代煎/自煎都走字典（sql/139），字典里加一味「焦三仙」这类特殊脚注不需要改代码。
const tcmMethodDict = ref([]);
const tcmDecoctFlagDict = ref([]);
// 标准 HIS 饮片脚注集合（特殊煎法：先煎/后下/包煎/烊化/冲服/另煎/兑服），
// 存明细 remark（后端 biz_prescription_detail.remark 已有），空 = 无特殊脚注
const tcmFootnoteOptions = ['先煎', '后下', '包煎', '烊化', '冲服', '另煎', '兑服'];
const loadTcmDicts = async () => {
  try {
    const res = await getDictDataMapList(`${DICT_TYPE.TCM_DECOCT_METHOD},${DICT_TYPE.TCM_DECOCT_FLAG}`);
    tcmMethodDict.value = res?.data?.[DICT_TYPE.TCM_DECOCT_METHOD] || [];
    tcmDecoctFlagDict.value = res?.data?.[DICT_TYPE.TCM_DECOCT_FLAG] || [];
  } catch (e) {
    console.error('加载中药煎法字典失败', e);
  }
};
/** 每剂克数：单剂剂量列只填数字（后端同样按数字解析，口径一致） */
const tcmPerDoseGrams = (item) => {
  const n = Number(String(item?.singleDosage ?? '').trim());
  return Number.isFinite(n) && n > 0 ? n : 0;
};
/** 实发总克数 = 每剂克数 × 剂数，也就是提交给后端的 quantity */
const tcmGramsOf = (item) => {
  const g = tcmPerDoseGrams(item) * (Number(prescriptionForm.doseCount) || 0);
  return Math.round(g * 100) / 100;
};
/** 饮片零售价是「元/档案单位(kg)」，按克开方要先除以换算率换成元/克 */
const tcmPerGramPrice = (drug) => {
  const gpu = Number(drug?.gramPerUnit) || 0;
  return gpu > 0 ? Number(drug.retailPrice || 0) / gpu : Number(drug?.retailPrice || 0);
};
/** 预估金额 = 行上单价（元/克）× 实发克数；真实金额以后端按 4 位单价重算为准 */
const tcmAmountOf = (item) => {
  return Math.round((Number(item?.price) || 0) * tcmGramsOf(item) * 100) / 100;
};
const applyTcmGrams = (item) => {
  item.unit = 'g';
  item.quantity = tcmGramsOf(item);
  item.amount = tcmAmountOf(item);
};
/** 饮片表尾合计：总克数 = Σ实发克数（每剂克数×剂数），金额 = Σ行金额（标准 HIS 表尾） */
const totalTcmGrams = computed(() => {
  return prescriptionForm.details.reduce((s, d) => s + (Number(d.quantity) || 0), 0);
});
const totalTcmAmount = computed(() => {
  return prescriptionForm.details.reduce((s, d) => s + (Number(d.amount) || 0), 0);
});
/** 把「当前类型那张方」灌进编辑态：明细 + 剂数 + 煎服方式。
 * 煎服方式：历史方有值保持原值；**新方/无值默认自煎（2）** —— 不然医生开完药没注意到
 * 代煎/自煎没选，结诊就被「请选择煎服方式」拦住（截图实锤：选项在、默认空）。 */
const bindPrescriptionForm = () => {
  const p = currentPrescription.value;
  prescriptionForm.details = p?.details || [];
  prescriptionForm.doseCount = p?.doseCount > 0 ? p.doseCount : 7;
  prescriptionForm.decoctFlag = p?.decoctFlag ?? 2;
  prescriptionForm.isLongPrescription = p?.isLongPrescription === 1;
  prescriptionForm.longPrescriptionDays = p?.longPrescriptionDays > 0 ? p.longPrescriptionDays : 28;
};
// 剂数一改，整张方的克数与金额跟着重算（只是本地预估，真实金额后端按 4 位单价算）
watch(() => prescriptionForm.doseCount, () => {
  if (currentPrescriptionType.value !== 3)
    return;
  prescriptionForm.details.forEach((item) => {
    if (item.drugId)
      applyTcmGrams(item);
  });
  loadInsuranceInfo();
});

// ========== 开方实时审方（前置审方）：处方行每次变化防抖 600ms 调后端比对相互作用/剂量上限，
// 命中实时摊在「用药安全审查」卡片上；只读辅助提示，接口失败静默不阻塞开方 ==========
const rxDraftHits = ref([]);
const rxDraftChecking = ref(false);
let rxDraftTimer = null;
const runRxDraftCheck = async () => {
  if (currentPatient.value?.queueStatus !== 3) {
    rxDraftHits.value = [];
    return;
  }
  const items = prescriptionForm.details
      .filter(d => d.drugId)
      .map(d => ({
        drugId: d.drugId,
        drugName: d.drugName || '',
        genericName: d.genericName || '',
        specification: d.specification || '',
        singleDosage: String(d.singleDosage ?? ''),
        frequency: d.frequency || ''
      }));
  if (items.length === 0) {
    rxDraftHits.value = [];
    return;
  }
  rxDraftChecking.value = true;
  try {
    rxDraftHits.value = await rationalCheckDraft(items);
  } catch (e) {
    rxDraftHits.value = [];
  } finally {
    rxDraftChecking.value = false;
  }
};
watch(() => prescriptionForm.details, () => {
  if (rxDraftTimer) clearTimeout(rxDraftTimer);
  rxDraftTimer = setTimeout(runRxDraftCheck, 600);
}, {deep: true});

// 医嘱费用合计（结诊前医生核对用）：处方=已保存非当前类型方 + 编辑态当前类型方，检查/检验按行单价
const rxAmountOfType = (type) => {
  // 当前编辑中的类型取编辑态现值（行金额实时重算），其余类型取已保存方
  const editing = currentPrescriptionType.value === type ? prescriptionForm.details : null;
  if (editing) {
    return editing.reduce((s, d) => s + (Number(d.amount) || 0), 0);
  }
  const p = prescriptionList.value.find(p => p.prescriptionType === type);
  return p ? (p.details || []).reduce((s, d) => s + (Number(d.amount) || 0), 0) : 0;
};
const insTotalAmount = computed(() => inspectionRecords.value.reduce((s, r) => s + (Number(r.price) || 0), 0));
const labTotalAmount = computed(() => laboratoryRecords.value.reduce((s, r) => s + (Number(r.price) || 0), 0));
const orderTotalAmount = computed(() => {
  const rxSum = [1, 2, 3].reduce((s, t) => s + rxAmountOfType(t), 0);
  return Math.round((rxSum + insTotalAmount.value + labTotalAmount.value) * 100) / 100;
});
// 费用明细弹窗行（只展示金额>0 的类型处方；检查/检验按申请单逐项列）
const rxFeeRows = computed(() => [
  {label: '西药处方', amount: rxAmountOfType(1)},
  {label: '中成药处方', amount: rxAmountOfType(2)},
  {label: '中药饮片处方', amount: rxAmountOfType(3)},
].filter(r => r.amount > 0));
const recordSaved = ref(false);
const icd10Results = ref([]);
const icd10Loading = ref(false);
// 按类型分组的药品数据
const drugResultsByType = {
  1: ref([]), // 西药
  2: ref([]), // 中成药
  3: ref([]), // 中药饮片
};
const drugLoading = ref(false);
// 当前处方类型的药品列表
const drugResults = computed(() => {
  return drugResultsByType[currentPrescriptionType.value]?.value || [];
});
const inspectionItemResults = ref([]);
const inspectionItemLoading = ref(false);
const laboratoryItemResults = ref([]);
const laboratoryItemLoading = ref(false);
let refreshTimer = null;
// ========== 结诊状态 ==========
const isVisitCompleted = computed(() => currentPatient.value?.queueStatus === 4);
const showGuideSheetDialog = ref(false);
// ========== 患者标签管理 ==========
const patientTags = ref([]);
const allTags = ref([]);
const showTagDialog = ref(false);
const tagSearchKeyword = ref('');
// ========== 个人模板管理 ==========
const showDiagTemplateDialog = ref(false);
const showRxTemplateDialog = ref(false);
const showPackageDialog = ref(false);
const showInspectionTemplateDialog = ref(false);
const showAddInspectionTemplateDialog = ref(false);
const showLaboratoryTemplateDialog = ref(false);
const showAddLaboratoryTemplateDialog = ref(false);
// 模板维护（统一大弹窗）：左侧五类模板，点击左侧切换并刷新右侧列表
const templateMaintainVisible = ref(false);
const activeTemplateTab = ref('diag');
const templateMaintainTabs = [
  {key: 'diag', label: '常用诊断'},
  {key: 'rx', label: '处方模板'},
  {key: 'package', label: '药品套餐'},
  {key: 'inspection', label: '检查模板'},
  {key: 'laboratory', label: '检验模板'},
];
const openTemplateMaintain = () => {
  activeTemplateTab.value = 'diag';
  templateMaintainVisible.value = true;
  switchTemplateTab('diag');
};
const switchTemplateTab = (key) => {
  activeTemplateTab.value = key;
  // 点击左侧分类即加载右侧数据（列表源与各维护弹窗共用）
  if (key === 'diag') loadDiagTemplates();
  else if (key === 'rx') loadRxTemplates();
  else if (key === 'package') loadDrugPackages();
  else if (key === 'inspection') loadInspectionTemplates();
  else if (key === 'laboratory') loadLaboratoryTemplates();
};
const myDiagTemplates = ref([]);
const myRxTemplates = ref([]);
// 常用药品快捷 chips：按当前处方类型 tab 过滤（模板带 prescriptionType）。
// 兜底：旧数据 NULL（改动前保存、后端归一未跑时）只在西药 tab 显示，避免西药模板混进饮片/中成药 tab
const filteredRxTemplates = computed(() => {
  const type = currentPrescriptionType.value;
  return myRxTemplates.value.filter(t => !t.prescriptionType ? type === 1 : t.prescriptionType === type);
});
const myPackages = ref([]);
const myInspectionTemplates = ref([]);
const myLaboratoryTemplates = ref([]);
const newTemplateName = ref('');
const diagSearchKeyword = ref('');
const diagSearchResults = ref([]);
const diagSearchLoading = ref(false);
const showAddPackageDialog = ref(false);
const newPackageName = ref('');
// 检查申请模板相关
const inspectionTemplateName = ref('');
const inspectionTemplateForm = ref({
  inspectionItemId: null,
  inspectionItemName: '',
  bodyPart: '',
  inspectionPurpose: '',
  isEmergency: 0
});
const inspectionTemplateItemResults = ref([]);
const inspectionTemplateItemLoading = ref(false);
// 检验申请模板相关
const laboratoryTemplateName = ref('');
const laboratoryTemplateForm = ref({
  laboratoryItemId: null,
  laboratoryItemName: '',
  sampleType: '',
  inspectionPurpose: '',
  isEmergency: 0
});
const laboratoryTemplateItemResults = ref([]);
const laboratoryTemplateItemLoading = ref(false);
const loadDiagTemplates = async () => {
  try {
    const res = await getDiagTemplates();
    myDiagTemplates.value = res.data || [];
  } catch (e) {
    console.error('加载常用诊断失败', e);
  }
};
const handleDeleteDiagTemplate = async (idx) => {
  const item = myDiagTemplates.value[idx];
  try {
    await deleteDiagTemplate(item.id);
    myDiagTemplates.value.splice(idx, 1);
    ElMessage.success('常用诊断已删除');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleDiagSearchInDialog = async (query) => {
  if (!query) {
    diagSearchResults.value = [];
    return;
  }
  diagSearchLoading.value = true;
  try {
    const res = await searchIcd10(query);
    diagSearchResults.value = (res.data || []).filter((item) => !myDiagTemplates.value.some((t) => t.icdCode === item.icdCode));
  } catch (e) {
    console.error('搜索失败', e);
  } finally {
    diagSearchLoading.value = false;
  }
};
const handleAddDiagFromDialog = async (item) => {
  const newList = [...myDiagTemplates.value, {icdCode: item.icdCode, icdName: item.icdName}];
  try {
    await saveDiagTemplates(newList);
    myDiagTemplates.value = newList;
    diagSearchKeyword.value = '';
    diagSearchResults.value = [];
    ElMessage.success('已添加');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleQuickSelectDiag = (tpl) => {
  recordForm.diagnosis = tpl.icdName;
  recordForm.diagnosisCode = tpl.icdCode;
  recordForm.diagnosisName = tpl.icdName;
};
const handleAddDiagToTemplate = async () => {
  if (!recordForm.diagnosisCode || !recordForm.diagnosisName) {
    ElMessage.warning('请先选择诊断');
    return;
  }
  const exists = myDiagTemplates.value.some((t) => t.icdCode === recordForm.diagnosisCode);
  if (exists) {
    ElMessage.info('该诊断已在常用列表中');
    return;
  }
  try {
    const newList = [...myDiagTemplates.value, {icdCode: recordForm.diagnosisCode, icdName: recordForm.diagnosisName}];
    await saveDiagTemplates(newList);
    myDiagTemplates.value = newList;
    ElMessage.success('已添加到常用诊断');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleSaveDiagTemplates = async () => {
  try {
    await saveDiagTemplates(myDiagTemplates.value);
    ElMessage.success('保存成功');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const loadRxTemplates = async () => {
  try {
    const res = await getRxTemplates();
    myRxTemplates.value = res.data || [];
  } catch (e) {
    console.error('加载处方模板失败', e);
  }
};
const handleAddRxTemplate = async () => {
  if (!newTemplateName.value) {
    ElMessage.warning('请输入模板名称');
    return;
  }
  if (prescriptionForm.details.length === 0) {
    ElMessage.warning('当前处方为空，请先添加药品');
    return;
  }
  try {
    await saveRxTemplate({
      templateName: newTemplateName.value,
      // 模板记下当前 tab 的处方类型：常用药品 chips 按类型过滤（西药/中成药/饮片各自独立）
      prescriptionType: currentPrescriptionType.value,
      details: [...prescriptionForm.details],
    });
    newTemplateName.value = '';
    ElMessage.success('模板保存成功');
    loadRxTemplates();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleApplyRxTemplate = async (tpl) => {
  try {
    const res = await getRxTemplateDetail(tpl.id);
    prescriptionForm.details = (res.data?.details || []).map((d) => ({...d}));
    loadInsuranceInfo();
    showRxTemplateDialog.value = false;
    ElMessage.success(`已套用模板「${tpl.templateName}」`);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleApplyRxTemplateToRecord = async (record, tpl) => {
  try {
    const res = await getRxTemplateDetail(tpl.id);
    const details = res.data?.details || [];
    if (details.length > 0) {
      const d = details[0];
      record.drugId = d.drugId;
      record.drugCode = d.drugCode;
      record.drugName = d.drugName;
      record.specification = d.specification || '';
      record.unit = d.unit || '盒';
      record.singleDosage = d.singleDosage || '';
      record.frequency = d.frequency || '';
      record.route = d.route || '';
      record.quantity = d.quantity || 1;
      record.duration = d.duration || 7;
      record.price = d.price || 0;
      record.amount = (d.quantity || 1) * (d.price || 0);
      // 饮片模板里的 quantity 未必对得上本张方的剂数，克数一律按「每剂克数 × 剂数」重算
      if (currentPrescriptionType.value === 3)
        applyTcmGrams(record);
    }
    ElMessage.success(`已套用模板「${tpl.templateName}」`);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
/** 表格化后「常用药品」chips 在表格上方、没有行对象可套 → 追加模板首味药为新行 */
const handleAppendRxTemplate = async (tpl) => {
  try {
    const res = await getRxTemplateDetail(tpl.id);
    const details = res.data?.details || [];
    if (details.length === 0) {
      ElMessage.warning(`模板「${tpl.templateName}」没有药品`);
      return;
    }
    const type = currentPrescriptionType.value;
    if (!hasPrescriptionType(type)) {
      const newPrescription = {
        prescriptionType: type, diagnosis: '', usageInstruction: '', details: [],
      };
      prescriptionList.value.push(newPrescription);
      prescriptionForm.details = newPrescription.details;
    }
    const d = details[0];
    const row = {
      drugId: d.drugId, drugCode: d.drugCode, drugName: d.drugName,
      specification: d.specification || '', unit: d.unit || (type === 3 ? 'g' : '盒'),
      singleDosage: d.singleDosage || '', frequency: d.frequency || '', route: d.route || '',
      quantity: d.quantity || 1, duration: d.duration || 7, price: d.price || 0,
      amount: (d.quantity || 1) * (d.price || 0), remark: d.remark || '',
    };
    if (type === 3) {
      row.unit = 'g';
      row.quantity = 0;
      if (!row.frequency) row.frequency = '每日一剂';
      if (!row.route) row.route = tcmMethodDict.value[0]?.dictValue || '水煎服';
      applyTcmGrams(row);
    }
    prescriptionForm.details.push(row);
    ElMessage.success(`已添加「${d.drugName || '药品'}」`);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleDeleteRxTemplate = async (idx) => {
  const item = myRxTemplates.value[idx];
  try {
    await deleteRxTemplate(item.id);
    myRxTemplates.value.splice(idx, 1);
    ElMessage.success('处方模板已删除');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const loadDrugPackages = async () => {
  try {
    const res = await getDrugPackages();
    myPackages.value = res.data || [];
  } catch (e) {
    console.error('加载药品套餐失败', e);
  }
};
const handleApplyPackage = async (pkg) => {
  try {
    const res = await getDrugPackageDetail(pkg.id);
    const details = res.data?.details || [];
    details.forEach((d) => {
      if (d.itemType === 1) {
        prescriptionForm.details.push({...d, drugId: d.itemId, drugName: d.itemName});
      } else if (d.itemType === 2) {
        inspectionForm.items.push({itemId: d.itemId, itemName: d.itemName, price: d.price});
      } else if (d.itemType === 3) {
        laboratoryForm.items.push({itemId: d.itemId, itemName: d.itemName, price: d.price});
      }
    });
    showPackageDialog.value = false;
    ElMessage.success(`已套用套餐「${pkg.packageName}」`);
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleDeletePackage = async (idx) => {
  const item = myPackages.value[idx];
  try {
    await deleteDrugPackage(item.id);
    myPackages.value.splice(idx, 1);
    ElMessage.success('套餐已删除');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleSaveNewPackage = async () => {
  if (!newPackageName.value) {
    ElMessage.warning('请输入套餐名称');
    return;
  }
  const details = [];
  // 药品
  prescriptionForm.details.forEach((d) => {
    details.push({
      itemType: 1, itemId: d.drugId, itemCode: d.drugCode, itemName: d.drugName,
      specification: d.specification, unit: d.unit, quantity: d.quantity,
      price: d.price, usageDosage: d.usageDosage, frequency: d.frequency,
      route: d.route, duration: d.duration
    });
  });
  // 检查
  inspectionForm.items?.forEach((d) => {
    details.push({
      itemType: 2, itemId: d.itemId, itemCode: d.itemCode || '', itemName: d.itemName,
      specification: '', unit: '', quantity: 1, price: d.price || 0
    });
  });
  // 检验
  laboratoryForm.items?.forEach((d) => {
    details.push({
      itemType: 3, itemId: d.itemId, itemCode: d.itemCode || '', itemName: d.itemName,
      specification: '', unit: '', quantity: 1, price: d.price || 0
    });
  });
  if (details.length === 0) {
    ElMessage.warning('当前没有可保存的项目，请先添加处方/检查/检验');
    return;
  }
  try {
    await saveDrugPackage({packageName: newPackageName.value, details});
    newPackageName.value = '';
    showAddPackageDialog.value = false;
    ElMessage.success('套餐保存成功');
    loadDrugPackages();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
// ========== 检查申请模板 ==========
const loadInspectionTemplates = async () => {
  try {
    const res = await getInspectionTemplates();
    myInspectionTemplates.value = res.data || [];
  } catch (e) {
    console.error('加载检查申请模板失败', e);
  }
};
const handleDeleteInspectionTemplate = async (idx) => {
  const item = myInspectionTemplates.value[idx];
  try {
    await deleteInspectionTemplate(item.id);
    myInspectionTemplates.value.splice(idx, 1);
    ElMessage.success('检查模板已删除');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleInspectionTemplateItemSearch = async (query) => {
  if (!query) {
    inspectionTemplateItemResults.value = [];
    return;
  }
  inspectionTemplateItemLoading.value = true;
  try {
    const res = await searchInspectionItem(query);
    inspectionTemplateItemResults.value = res.data || [];
  } catch (e) {
    console.error('搜索检查项目失败', e);
  } finally {
    inspectionTemplateItemLoading.value = false;
  }
};
const handleInspectionTemplateItemSelect = (itemId) => {
  const item = inspectionTemplateItemResults.value.find((i) => i.id === itemId);
  if (item) {
    inspectionTemplateForm.value.inspectionItemId = item.id;
    inspectionTemplateForm.value.inspectionItemName = item.itemName;
  }
};
const handleSaveInspectionTemplate = async () => {
  if (!inspectionTemplateName.value) {
    ElMessage.warning('请输入模板名称');
    return;
  }
  if (!inspectionTemplateForm.value.inspectionItemId) {
    ElMessage.warning('请选择检查项目');
    return;
  }
  try {
    await saveInspectionTemplate({
      templateName: inspectionTemplateName.value,
      ...inspectionTemplateForm.value
    });
    inspectionTemplateName.value = '';
    inspectionTemplateForm.value = {
      inspectionItemId: null,
      inspectionItemName: '',
      bodyPart: '',
      inspectionPurpose: '',
      isEmergency: 0
    };
    ElMessage.success('模板保存成功');
    loadInspectionTemplates();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleApplyInspectionTemplate = (tpl) => {
  inspectionForm.inspectionItemId = tpl.inspectionItemId;
  inspectionForm.bodyPart = tpl.bodyPart || '';
  inspectionForm.inspectionPurpose = tpl.inspectionPurpose || '';
  inspectionForm.isEmergency = tpl.isEmergency || 0;
  showInspectionTemplateDialog.value = false;
  ElMessage.success(`已套用模板「${tpl.templateName}」`);
};
const handleApplyInspectionTemplateToRecord = (record, tpl) => {
  record.inspectionItemId = tpl.inspectionItemId;
  record.inspectionItemName = tpl.inspectionItemName || '';
  record.bodyPart = tpl.bodyPart || '';
  record.inspectionPurpose = tpl.inspectionPurpose || '';
  record.isEmergency = tpl.isEmergency || 0;
  record.price = tpl.price || 0;
  ElMessage.success(`已套用模板「${tpl.templateName}」`);
};
/** 表格化后「常用检查」chips 在表格上方、无行对象可套 → 追加为新行 */
const handleAppendInspectionTemplate = (tpl) => {
  inspectionRecords.value.push({
    inspectionItemId: tpl.inspectionItemId,
    inspectionItemName: tpl.inspectionItemName || '',
    bodyPart: tpl.bodyPart || '',
    inspectionPurpose: tpl.inspectionPurpose || '',
    isEmergency: tpl.isEmergency || 0,
    price: tpl.price || 0,
    preparation: '',
    _pendingIdx: Date.now(),
  });
  ElMessage.success(`已添加「${tpl.inspectionItemName || '检查项目'}」`);
};
// ========== 检验申请模板 ==========
const loadLaboratoryTemplates = async () => {
  try {
    const res = await getLaboratoryTemplates();
    myLaboratoryTemplates.value = res.data || [];
  } catch (e) {
    console.error('加载检验申请模板失败', e);
  }
};
const handleDeleteLaboratoryTemplate = async (idx) => {
  const item = myLaboratoryTemplates.value[idx];
  try {
    await deleteLaboratoryTemplate(item.id);
    myLaboratoryTemplates.value.splice(idx, 1);
    ElMessage.success('检验模板已删除');
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleLaboratoryTemplateItemSearch = async (query) => {
  if (!query) {
    laboratoryTemplateItemResults.value = [];
    return;
  }
  laboratoryTemplateItemLoading.value = true;
  try {
    const res = await searchLaboratoryItem(query);
    laboratoryTemplateItemResults.value = res.data || [];
  } catch (e) {
    console.error('搜索检验项目失败', e);
  } finally {
    laboratoryTemplateItemLoading.value = false;
  }
};
const handleLaboratoryTemplateItemSelect = (itemId) => {
  const item = laboratoryTemplateItemResults.value.find((i) => i.id === itemId);
  if (item) {
    laboratoryTemplateForm.value.laboratoryItemId = item.id;
    laboratoryTemplateForm.value.laboratoryItemName = item.itemName;
  }
};
const handleSaveLaboratoryTemplate = async () => {
  if (!laboratoryTemplateName.value) {
    ElMessage.warning('请输入模板名称');
    return;
  }
  if (!laboratoryTemplateForm.value.laboratoryItemId) {
    ElMessage.warning('请选择检验项目');
    return;
  }
  try {
    await saveLaboratoryTemplate({
      templateName: laboratoryTemplateName.value,
      ...laboratoryTemplateForm.value
    });
    laboratoryTemplateName.value = '';
    laboratoryTemplateForm.value = {
      laboratoryItemId: null,
      laboratoryItemName: '',
      sampleType: '',
      inspectionPurpose: '',
      isEmergency: 0
    };
    ElMessage.success('模板保存成功');
    loadLaboratoryTemplates();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleApplyLaboratoryTemplate = (tpl) => {
  laboratoryForm.laboratoryItemId = tpl.laboratoryItemId;
  laboratoryForm.sampleType = tpl.sampleType || '';
  laboratoryForm.inspectionPurpose = tpl.inspectionPurpose || '';
  laboratoryForm.isEmergency = tpl.isEmergency || 0;
  showLaboratoryTemplateDialog.value = false;
  ElMessage.success(`已套用模板「${tpl.templateName}」`);
};
const filteredAllTags = computed(() => {
  if (!tagSearchKeyword.value)
    return allTags.value;
  const kw = tagSearchKeyword.value.toLowerCase();
  return allTags.value.filter((tag) => tag.tagName?.toLowerCase().includes(kw));
});
const loadPatientTags = async () => {
  if (!currentPatient.value?.patientId)
    return;
  try {
    const res = await getPatientTags({patientId: currentPatient.value.patientId});
    patientTags.value = res.data || [];
  } catch (error) {
    console.error('加载患者标签失败:', error);
  }
};
const patientDetail = ref(null);
const loadPatientDetail = async () => {
  if (!currentPatient.value?.patientId)
    return;
  try {
    const res = await getPatientDetail(currentPatient.value.patientId);
    patientDetail.value = res.data || null;
  } catch (error) {
    console.error('加载患者详情失败:', error);
  }
};
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    ElMessage.success('已复制到剪贴板');
  } catch  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const loadAllTags = async () => {
  try {
    const res = await getPatientTagListAll({});
    allTags.value = res.data?.records || res.data || [];
  } catch (error) {
    console.error('加载标签列表失败:', error);
  }
};
const handleOpenTagDialog = async () => {
  await loadAllTags();
  await loadPatientTags();
  showTagDialog.value = true;
};
const handleAddTag = async (tagId) => {
  if (!currentPatient.value?.patientId)
    return;
  try {
    await addPatientTag({
      patientId: currentPatient.value.patientId,
      tagId: tagId,
      sourceType: 1
    });
    await loadPatientTags();
    ElMessage.success('标签添加成功');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleRemoveTag = async (tagId) => {
  if (!currentPatient.value?.patientId)
    return;
  try {
    await removePatientTag({
      patientId: currentPatient.value.patientId,
      tagId: tagId
    });
    await loadPatientTags();
    ElMessage.success('标签已移除');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const isTagAdded = (tagId) => {
  return patientTags.value.some((t) => t.tagId === tagId);
};
// ========== 数据加载 ==========
let isFirstLoad = true;
const loadData = async () => {
  loading.value = true;
  /** 本次队列加载是否失败：失败时不能拿空队列去判定「这人不在队列」（会误伤自己的患者） */
  let queueLoadFailed = false;
  try {
    const today = localDateStr();
    const [listRes, statsRes] = await Promise.all([
      getTodayQueueList({date: today}),
      getQueueStats(),
    ]);
    queueList.value = listRes.data || [];
    stats.value = statsRes.data || {waiting: 0, called: 0, inProgress: 0};
    // 初次加载时，自动选中当前就诊中的患者。
    // 两种情况下不自动选：
    //   ① 已经有选中患者（页面自己已经定了接诊对象）；
    //   ② 本次进入是「为某个特定患者而来」（顶部搜索跳转 / 刷新恢复 / URL 带 patientId）——
    //      医生是来找那个人的，先自动选中队列里的另一个人，等于把一次「找人」变成了
    //      「切换到别人」，而开单、开药、写病历挂的都是当前患者，看错人就是开错单。
    //      （准入判定见 lib/todayQueue.js，正常路径下这种人根本不会到这里。）
    // isFirstLoad 必须**无条件**置 false：原来这行写在 if 内部，只要首屏时已有选中患者
    // 它就永远不复位，之后 30 秒一次的定时刷新会突然把医生正在看的患者换掉。
    if (isFirstLoad) {
      if (!currentPatient.value && !enteredWithTarget) {
        // 用 pickQueueRow 而不是 find：同一位患者可能有多条 queueStatus=3 的队列记录
        // （数据里就存在），find 取的是后端序号最小的那条，不稳定也不一定是最早到的那次
        const consultingPatient = pickQueueRow(queueList.value.filter((q) => q.queueStatus === 3));
        if (consultingPatient) {
          selectPatient(consultingPatient);
        }
      }
      isFirstLoad = false;
    }
    // 如果有当前叫号患者且在新列表中找不到，清空选中
    if (currentPatient.value) {
      const stillExists = queueList.value.find((q) => q.id === currentPatient.value.id);
      if (!stillExists) {
        currentPatient.value = null;
        // 顶部提示条同步清掉：条子说「当前患者是A」而页面已经没选中任何人，
        // 在防开错人的场景里比不显示更危险
        currentPatientStore.syncClear();
      }
    }
  } catch (error) {
    console.error('加载数据失败:', error);
    queueLoadFailed = true;
  } finally {
    loading.value = false;
    // 队列第一次加载结束后，把挂起的待切换患者落地（在队列里→选中，不在→提示并转详情框）。
    // 放在 finally 是为了任何情况下都不会把患者永久挂起。
    if (!queueLoaded.value) {
      queueLoaded.value = true;
      if (queueLoadFailed) {
        // 队列没拉到就说「这人不在您今日队列」是假话——医生搜自己的患者也会被弹档。
        // 所以这里只清挂起状态、不判定、不提示，并把顶栏那条也清掉：
        // 顶栏显示「当前患者是 A」而页面谁都没选中，在防开错人的场景里比不显示更危险。
        pendingSwitch = null;
        enteredWithTarget = false;
        if (currentPatientStore.patient) {
          currentPatientStore.syncClear();
        }
        ElMessage.warning('候诊队列加载失败，未切换接诊患者，请刷新重试');
      } else {
        applyPendingSwitch();
      }
    }
  }
};
const loadUserInfo = () => {
  userInfo.value = {
    userId: localStorage.getItem('userId') || '',
    deptId: localStorage.getItem('deptId') || null,
    deptName: localStorage.getItem('deptName') || '',
    currentRole: localStorage.getItem('currentRole') || '',
  };
  if (userInfo.value.deptId) {
    selectedDeptId.value = Number(userInfo.value.deptId);
  }
};
// ========== 加载今日待办 ==========
const loadTodoList = async () => {
  // 模拟待办数据，实际应从后端获取
  todoList.value = [
    {id: 1, type: 'review', title: '待审核病历', content: '张三的病历待审核', time: '10:30', urgent: false},
    {id: 2, type: 'report', title: '待查看检验报告', content: '李四的血常规报告已出', time: '09:45', urgent: true},
    {id: 3, type: 'report', title: '待查看检查报告', content: '王五的CT报告已出', time: '09:20', urgent: false},
    {id: 4, type: 'followUp', title: '待回访患者', content: '赵六术后3天需电话回访', time: '14:00', urgent: false},
    {id: 5, type: 'consult', title: '会诊申请', content: '内科申请联合会诊', time: '11:00', urgent: true},
  ];
  todoStats.value = {
    pendingReview: 3,
    pendingReport: 5,
    pendingFollowUp: 2,
    pendingConsult: 1,
  };
};
// ========== ICD-10搜索 ==========
const handleIcd10Search = async (query) => {
  if (!query) {
    icd10Results.value = [];
    return;
  }
  icd10Loading.value = true;
  try {
    const res = await searchIcd10(query);
    icd10Results.value = res.data || [];
  } catch (error) {
    console.error('搜索ICD-10失败:', error);
  } finally {
    icd10Loading.value = false;
  }
};
const handleIcd10Select = (val) => {
  const item = icd10Results.value.find((i) => i.icdName === val);
  if (item) {
    recordForm.diagnosisCode = item.icdCode;
    recordForm.diagnosisName = item.icdName;
  }
};
// ========== ICD-10智能预测 ==========
const icdPredictions = ref([]);
const icdPredictionLoading = ref(false);
const showPrediction = ref(false);
const handlePredictIcd = async () => {
  if (!recordForm.chiefComplaint && !recordForm.presentIllness && !recordForm.specialistExam) {
    ElMessage.warning('请先填写主诉、现病史或专科检查');
    return;
  }
  icdPredictionLoading.value = true;
  showPrediction.value = true;
  try {
    const res = await predictIcd10({
      chiefComplaint: recordForm.chiefComplaint || '',
      presentIllness: recordForm.presentIllness || '',
      specialistExam: recordForm.specialistExam || '',
      diagnosis: recordForm.diagnosis || '',
    });
    icdPredictions.value = res.data || [];
  } catch (error) {
    console.error('ICD预测失败:', error);
    icdPredictions.value = [];
  } finally {
    icdPredictionLoading.value = false;
  }
};
const handleSelectPrediction = (item) => {
  // diagnosis 是诊断下拉（必填项）的 v-model，只写 code/name 不回填它，
  // 选中预测结果后输入框仍是空的——必填校验照样拦，医生白点一次
  recordForm.diagnosis = item.icdName;
  recordForm.diagnosisCode = item.icdCode;
  recordForm.diagnosisName = item.icdName;
  showPrediction.value = false;
  ElMessage.success(`已选择: ${item.icdCode} ${item.icdName}`);
};
// ========== 病历文本智能录入 / 草拟（P1-3）==========
// 铁律：两个接口都不写库。抽取结果是候选值、草拟结果是草稿，
// 都必须由医生点「填入」才进表单，保存病历时才由保存流程落库。
const extractDialogVisible = ref(false);
const extractRawText = ref('');
const extractLoading = ref(false);
const extractResult = ref(null);
const extractAppliedFields = ref([]);
const openExtractDialog = () => {
  extractRawText.value = '';
  extractResult.value = null;
  extractAppliedFields.value = [];
  extractDialogVisible.value = true;
};
const runExtract = async () => {
  if (!extractRawText.value.trim()) {
    ElMessage.warning('请先粘贴或输入要解析的文本');
    return;
  }
  extractLoading.value = true;
  extractResult.value = null;
  extractAppliedFields.value = [];
  try {
    const res = await extractEmrText({
      rawText: extractRawText.value,
      recordId: recordForm.id || undefined,
      gender: recordForm.gender || undefined,
      age: recordForm.age || undefined,
    });
    extractResult.value = res.data || null;
    if (!extractResult.value?.fields?.length) {
      ElMessage.warning('没有从这段文本中识别出可搬运的内容');
    }
  } catch (error) {
    console.error('病历文本抽取失败:', error);
    extractResult.value = null;
  } finally {
    extractLoading.value = false;
  }
};
// 只有病历表单里真实存在的字段才允许写入，后端白名单挡一道、前端再挡一道
const canApplyField = (key) => !!key && Object.prototype.hasOwnProperty.call(recordForm, key);
const applyExtractField = (row) => {
  if (!canApplyField(row?.field)) {
    ElMessage.warning(`「${row?.fieldLabel || row?.field}」无法写入病历表单`);
    return;
  }
  ;
  recordForm[row.field] = row.value;
  if (!extractAppliedFields.value.includes(row.field)) {
    extractAppliedFields.value.push(row.field);
  }
  ElMessage.success(`已填入「${row.fieldLabel}」`);
};
const applyAllExtractFields = () => {
  const rows = extractResult.value?.fields || [];
  let count = 0;
  rows.forEach((row) => {
    if (!canApplyField(row?.field))
      return;
    recordForm[row.field] = row.value;
    if (!extractAppliedFields.value.includes(row.field)) {
      extractAppliedFields.value.push(row.field);
    }
    count++;
  });
  if (count === 0) {
    ElMessage.warning('没有可填入的字段');
    return;
  }
  ElMessage.success(`已填入 ${count} 个字段，请逐项核对原文后再保存`);
};
const draftDialogVisible = ref(false);
const draftLoading = ref(false);
const draftResult = ref(null);
// G-10 diff 留痕：本会话 applyDraft 填入的 AI 草稿原文。保存时若终稿与其不同，
// 把草稿原文带给后端算 diff（AI 写了什么 vs 医生留了什么）；保存成功即清空，一次草稿会话只留一条。
const aiDraftAppliedText = ref('');
const runDraft = async () => {
  if (!recordForm.chiefComplaint) {
    ElMessage.warning('请先填写主诉，草拟现病史必须以主诉为依据');
    return;
  }
  draftLoading.value = true;
  draftResult.value = null;
  draftDialogVisible.value = true;
  try {
    const res = await draftEmrText({
      recordId: recordForm.id || undefined,
      gender: recordForm.gender || undefined,
      age: recordForm.age || undefined,
      chiefComplaint: recordForm.chiefComplaint || '',
      presentIllness: recordForm.presentIllness || '',
      pastHistory: recordForm.pastHistory || '',
      allergyHistory: recordForm.allergyHistory || '',
      temperature: recordForm.temperature || '',
      pulse: recordForm.pulse || '',
      respiration: recordForm.respiration || '',
      systolicPressure: recordForm.systolicPressure || '',
      diastolicPressure: recordForm.diastolicPressure || '',
      generalCondition: recordForm.generalCondition || '',
      skinMucosa: recordForm.skinMucosa || '',
      headNeck: recordForm.headNeck || '',
      chestLung: recordForm.chestLung || '',
      heart: recordForm.heart || '',
      abdomen: recordForm.abdomen || '',
      spineLimbs: recordForm.spineLimbs || '',
      nervousSystem: recordForm.nervousSystem || '',
      specialistExam: recordForm.specialistExam || '',
      auxiliaryExam: recordForm.auxiliaryExam || '',
    });
    draftResult.value = res.data || null;
  } catch (error) {
    console.error('病历草拟失败:', error);
    draftResult.value = null;
  } finally {
    draftLoading.value = false;
  }
};
const applyDraft = () => {
  const text = draftResult.value?.presentIllness;
  if (!text) {
    ElMessage.warning('本次没有生成草稿');
    return;
  }
  recordForm.presentIllness = text;
  aiDraftAppliedText.value = text;
  draftDialogVisible.value = false;
  ElMessage.success('草稿已填入现病史，请逐字核对后修改');
};
// ========== 语音口述（G-14） ==========
// MediaRecorder 录音 → ASR 转写 → 文本可编辑 → 喂既有 emr_draft 整理。
// 转写是确定性转换：失败如实报错、不造文本（语音没有规则兜底文本，造文本即造假病历）。
const VOICE_MAX_SECONDS = 60;
const voiceDialogVisible = ref(false);
const voiceRecording = ref(false);
const voiceSeconds = ref(0);
const voiceTranscribing = ref(false);
const voiceText = ref('');
let voiceRecorder = null;
let voiceStream = null;
let voiceChunks = [];
let voiceTimer = null;
let voiceMime = '';
const voiceSecondsText = computed(() => {
  const s = voiceSeconds.value;
  return `${Math.floor(s / 60)}:${String(s % 60).padStart(2, '0')}`;
});
const openVoiceDialog = () => {
  voiceText.value = '';
  voiceSeconds.value = 0;
  voiceTranscribing.value = false;
  voiceDialogVisible.value = true;
};
// 弹窗关闭/录音丢弃时统一回收：停计时器、断麦克风、弃录音块
const closeVoiceDialog = () => {
  if (voiceTimer) {
    clearInterval(voiceTimer);
    voiceTimer = null;
  }
  if (voiceRecorder && voiceRecorder.state !== 'inactive') {
    voiceRecorder.onstop = null;
    try {
      voiceRecorder.stop();
    } catch (e) { /* 媒体流已释放 */
    }
  }
  voiceRecorder = null;
  if (voiceStream) {
    voiceStream.getTracks().forEach((t) => t.stop());
    voiceStream = null;
  }
  voiceChunks = [];
  voiceRecording.value = false;
};
const startVoiceRecord = async () => {
  if (!navigator.mediaDevices?.getUserMedia || typeof MediaRecorder === 'undefined') {
    ElMessage.error('当前浏览器不支持录音，请使用 Edge/Chrome');
    return;
  }
  try {
    voiceStream = await navigator.mediaDevices.getUserMedia({audio: true});
  } catch (e) {

    return;
  }
  voiceMime = ['audio/webm;codecs=opus', 'audio/webm', 'audio/mp4']
      .find(t => MediaRecorder.isTypeSupported(t)) || '';
  voiceChunks = [];
  voiceRecorder = new MediaRecorder(voiceStream, voiceMime ? {mimeType: voiceMime} : undefined);
  voiceRecorder.ondataavailable = (e) => {
    if (e.data.size > 0)
      voiceChunks.push(e.data);
  };
  voiceRecorder.start();
  voiceRecording.value = true;
  voiceSeconds.value = 0;
  voiceTimer = setInterval(() => {
    voiceSeconds.value++;
    if (voiceSeconds.value >= VOICE_MAX_SECONDS)
      stopVoiceRecord();
  }, 1000);
};
const stopVoiceRecord = () => {
  if (!voiceRecorder || voiceRecorder.state === 'inactive')
    return;
  if (voiceTimer) {
    clearInterval(voiceTimer);
    voiceTimer = null;
  }
  // 不在这里复位 voiceRecording：stop() 到 onstop 之间若把按钮恢复成「开始录音」，
  // 转写还没开始就出现可点按钮（再点一次会造出两段录音），转写中状态也会被吞掉；
  // 复位交给 onstop 里的 closeVoiceDialog，与 voiceTranscribing 同步块内切换，无中间态
  const duration = voiceSeconds.value;
  const recorder = voiceRecorder;
  recorder.onstop = async () => {
    const blob = new Blob(voiceChunks, {type: voiceMime || 'audio/webm'});
    closeVoiceDialog();
    if (blob.size > 0)
      await doTranscribe(blob, duration);
  };
  recorder.stop();
};
const doTranscribe = async (blob, durationSeconds) => {
  voiceTranscribing.value = true;
  voiceText.value = '';
  try {
    const fd = new FormData();
    const ext = voiceMime.includes('mp4') ? 'mp4' : 'webm';
    fd.append('file', blob, `voice.${ext}`);
    if (durationSeconds > 0)
      fd.append('durationSeconds', String(durationSeconds));
    const res = await transcribeVoice(fd);
    voiceText.value = res.data?.text || '';
    if (!voiceText.value)
      ElMessage.warning('未能从音频中识别出语音内容');
  } catch (e) {
    // 失败必须可见：ASR 拒识别/上游异常时医生要看到原因（request.js 对 500 只 console 不 toast），
    // 「点了没反应」会让医生以为录音功能坏了
    const msg = e?.response?.data?.message || e?.message || '语音转写失败，请重试';

    console.error('语音转写失败:', e);
  } finally {
    voiceTranscribing.value = false;
  }
};
// 转写文本原文直填现病史（追加，不覆盖医生已写内容）
const fillVoiceText = () => {
  const text = voiceText.value.trim();
  if (!text) {
    ElMessage.warning('没有可填入的转写文本');
    return false;
  }
  recordForm.presentIllness = recordForm.presentIllness
      ? `${recordForm.presentIllness}\n${text}` : text;
  return true;
};
const applyVoiceToField = () => {
  if (fillVoiceText()) {
    voiceDialogVisible.value = false;
    ElMessage.success('转写文本已填入现病史，请逐字核对后修改');
  }
};
// 填入现病史后走既有草拟：口述文本作为「医生已写的部分」喂 emr_draft，
// 模型整理成现病史草稿 → 复用草稿弹窗医生确认（G-10 留痕自动接上）
const draftFromVoice = () => {
  if (!fillVoiceText())
    return;
  voiceDialogVisible.value = false;
  runDraft();
};
// ========== 预问诊报告卡（G-05） ==========
// 患者在小程序提交的问卷 + AI 凝摘要；没做过预问诊时 data=null，卡片不渲染。
// 只读展示 —— 医生参考后再自己写现病史，AI 不代填。
const previsitInfo = ref(null);
const previsitSourceText = (v) => (v === 1 ? '模型凝摘要' : '规则摘要');
const loadPrevisit = async (registId) => {
  previsitInfo.value = null;
  if (!registId)
    return;
  try {
    const res = await getPrevisitByRegist(registId);
    if (res.code === 200)
      previsitInfo.value = res.data || null;
  } catch (e) {
    console.error('预问诊记录加载失败', e);
  }
};
// ========== AI辅助诊疗 ==========
const aiDiagnosisResults = ref([]);
const aiDiagnosisLoading = ref(false);
// 模型降级标记：degraded=true 表示结果是码表规则匹配出来的，必须如实告知，不得当模型推荐展示
const aiDiagnosisDegraded = ref(false);
const aiDiagnosisDegradeReason = ref('');
const aiGuideDialogVisible = ref(false);
const aiGuideData = ref(null);
const handleAiDiagnosis = async () => {
  if (!recordForm.chiefComplaint && !recordForm.presentIllness && !recordForm.specialistExam) {
    return;
  }
  aiDiagnosisLoading.value = true;
  try {
    const res = await predictIcd10Ai({
      chiefComplaint: recordForm.chiefComplaint || '',
      presentIllness: recordForm.presentIllness || '',
      specialistExam: recordForm.specialistExam || '',
      diagnosis: recordForm.diagnosis || '',
    });
    // his-ms-ai 契约：Result<Icd10PredictResultVO>，候选在 predictions 里；
    // degraded=true 表示模型未参与、规则降级，界面必须如实显示（ai.js 约定）
    const payload = res.data || {};
    // 置信度只认后端返回值（模型或规则给出的分值）。后端没返回就不显示，
    // 严禁前端按数组下标编造百分比 —— 医生会当真，这是安全问题不是展示问题。
    aiDiagnosisResults.value = (payload.predictions || []).slice(0, 5).map((item) => ({
      ...item,
      confidence: typeof item.confidence === 'number' ? item.confidence : null
    }));
    aiDiagnosisDegraded.value = payload.degraded === true;
    aiDiagnosisDegradeReason.value = payload.degradeReason || '';
  } catch (error) {
    console.error('AI诊断推荐失败:', error);
    aiDiagnosisResults.value = [];
    aiDiagnosisDegraded.value = false;
    aiDiagnosisDegradeReason.value = '';
  } finally {
    aiDiagnosisLoading.value = false;
  }
};
const handleAdoptAiDiagnosis = (item) => {
  recordForm.diagnosis = item.icdName;
  recordForm.diagnosisCode = item.icdCode;
  recordForm.diagnosisName = item.icdName;
  ElMessage.success(`已采纳诊断: ${item.icdName}`);
};
const handleViewAiGuide = (item) => {
  aiGuideData.value = item;
  aiGuideDialogVisible.value = true;
};
// ========== CDSS用药安全审查 ==========
const cdssAlerts = ref([]);
const checkDrugSafety = () => {
  cdssAlerts.value = [];
};
// ========== 药品搜索 ==========
const handleDrugSearch = async (query) => {
  if (!query) {
    drugResults.value = [];
    return;
  }
  drugLoading.value = true;
  try {
    const res = await getStockList({drugName: query, pageNum: 1, pageSize: 20});
    drugResults.value = res.data?.records || [];
  } catch (error) {
    console.error('搜索药品失败:', error);
  } finally {
    drugLoading.value = false;
  }
};
const handleDrugSelect = (val) => {
  // selectList 出参主键字段是 id（无 drugId）
  const drug = drugResults.value.find((d) => String(d.id) === String(val));
  if (drug) {
    newDrug.drugName = drug.drugName;
    newDrug.specification = drug.specification || '';
    newDrug.drugId = drug.id;
    newDrug.drugCode = drug.drugCode;
    newDrug.unit = drug.unit || '盒';
    newDrug.price = drug.retailPrice || 0;
  }
};
// ========== 检查项目搜索 ==========
const handleInspectionItemSearch = async (query) => {
  if (!query) {
    inspectionItemResults.value = [];
    return;
  }
  inspectionItemLoading.value = true;
  try {
    const res = await searchInspectionItem(query);
    inspectionItemResults.value = res.data || [];
  } catch (error) {
    console.error('搜索检查项目失败:', error);
  } finally {
    inspectionItemLoading.value = false;
  }
};
/**
 * 检查项目选中/切换 → 只改本地行数据（模式A：编辑与落库解耦）。
 *
 * 申请单不再"开单即落库"：医生工作台内的选择/编辑都停留在本地数组，
 * 「保存病历 / 结诊」时随病历一并提交（后端 ensureInspectionApplies 落库，结诊统一签名锁定）。
 * 这样切换项目不再产生一次写库 + 一次签名，也不会在编辑中途生成待缴费废单。
 */
const handleInspectionItemSelectForRecord = (record, val) => {
  const item = inspectionItemResults.value.find((i) => i.id === val);
  if (!item)
    return;
  record.inspectionItemId = item.id;
  record.inspectionItemName = item.itemName;
  record.bodyPart = item.bodyPart || '';
  record.preparation = item.preparation || '';
  record.price = item.price || 0;
  loadInsuranceInfo();
};
const handleInspectionItemSelect = (val) => {
  const item = inspectionItemResults.value.find((i) => i.id === val);
  if (item) {
    inspectionForm.inspectionItemId = item.id;
    inspectionForm.inspectionItemName = item.itemName;
    inspectionForm.bodyPart = item.bodyPart || '';
    inspectionForm.preparation = item.preparation || '';
  }
};
// ========== 检验项目搜索 ==========
const handleLaboratoryItemSearch = async (query) => {
  if (!query) {
    laboratoryItemResults.value = [];
    return;
  }
  laboratoryItemLoading.value = true;
  try {
    const res = await searchLaboratoryItem(query);
    laboratoryItemResults.value = res.data || [];
  } catch (error) {
    console.error('搜索检验项目失败:', error);
  } finally {
    laboratoryItemLoading.value = false;
  }
};
const handleLaboratoryItemSelect = (val) => {
  const item = laboratoryItemResults.value.find((i) => i.id === val);
  if (item) {
    laboratoryForm.laboratoryItemId = item.id;
    laboratoryForm.laboratoryItemName = item.itemName;
    laboratoryForm.specimenType = item.specimenType || '血液';
  }
};
/** 检验项目选中/切换 → 只改本地行数据（模式A），语义同检查。 */
const handleLaboratoryItemSelectForRecord = (record, val) => {
  const item = laboratoryItemResults.value.find((i) => i.id === val);
  if (!item)
    return;
  record.laboratoryItemId = item.id;
  record.laboratoryItemName = item.itemName;
  record.specimenType = item.specimenType || '血液';
  record.price = item.price || 0;
  loadInsuranceInfo();
};
const handleApplyLaboratoryTemplateToRecord = (record, tpl) => {
  record.laboratoryItemId = tpl.laboratoryItemId;
  record.laboratoryItemName = tpl.laboratoryItemName || '';
  record.specimenType = tpl.specimenType || '';
  record.laboratoryPurpose = tpl.laboratoryPurpose || '';
  record.isEmergency = tpl.isEmergency || 0;
  record.price = tpl.price || 0;
  ElMessage.success(`已套用模板「${tpl.templateName}」`);
};
/** 表格化后「常用检验」chips 在表格上方、无行对象可套 → 追加为新行 */
const handleAppendLaboratoryTemplate = (tpl) => {
  laboratoryRecords.value.push({
    laboratoryItemId: tpl.laboratoryItemId,
    laboratoryItemName: tpl.laboratoryItemName || '',
    specimenType: tpl.specimenType || '',
    laboratoryPurpose: tpl.laboratoryPurpose || '',
    isEmergency: tpl.isEmergency || 0,
    price: tpl.price || 0,
    isFasting: 0,
    _pendingIdx: Date.now(),
  });
  ElMessage.success(`已添加「${tpl.laboratoryItemName || '检验项目'}」`);
};
// ========== 选中患者 ==========
const selectPatient = async (row) => {
  currentPatient.value = row;
  // 同步顶部「当前患者」条（映射口径见 toStorePatient：队列行的 id 是队列号，不能当患者 id 用）。
  // 用 syncPatient 而不是 setPatient，避免改 switchedAt 后与本函数的 watch 形成回环。
  // 注意：队列行不带过敏史，条子就不会亮「过敏」标（不假装无过敏），完整信息点条子看档案。
  if (row?.patientId) {
    currentPatientStore.syncPatient(toStorePatient(row));
  }
  activeTab.value = 'record';
  recordSaved.value = false;
  // 清空历史数据（批次F：收费/详情/标签也要清，否则新患者加载期间看到的是上一位患者的钱和信息）
  inspectionRecords.value = [];
  laboratoryRecords.value = [];
  prescriptionList.value = [];
  currentPrescriptionIdx.value = 0;
  patientDetail.value = null;
  patientTags.value = [];
  insuranceInfo.value = null;
  // G-05/G-10：换患者时清掉上一位的预问诊卡与 AI 草稿会话
  previsitInfo.value = null;
  aiDraftAppliedText.value = '';
  loadPrevisit(row.registId);
  loadChronicQualification(row.patientId);
  // 重置所有表单
  const baseInfo = {
    patientId: row.patientId, patientNo: row.patientNo, patientName: row.patientName,
    registId: row.registId, registNo: row.registNo || '',
    deptId: row.deptId, deptName: row.deptName, doctorId: row.doctorId, doctorName: row.doctorName,
    gender: row.gender, age: row.age,
    visitDate: localDateStr(),
  };
  Object.assign(recordForm, baseInfo, {
    id: null,
    recordNo: '',
    recordStatus: 1,
    reviewStatus: 0,
    chiefComplaint: '',
    presentIllness: '',
    pastHistory: '',
    personalHistory: '',
    familyHistory: '',
    marriageHistory: '',
    allergyHistory: '',
    temperature: '',
    pulse: '',
    respiration: '',
    systolicPressure: '',
    diastolicPressure: '',
    generalCondition: '',
    skinMucosa: '',
    headNeck: '',
    chestLung: '',
    heart: '',
    abdomen: '',
    spineLimbs: '',
    nervousSystem: '',
    specialistExam: '',
    auxiliaryExam: '',
    diagnosis: '',
    diagnosisCode: '',
    diagnosisName: '',
    treatmentPlan: ''
  });
  Object.assign(prescriptionForm, baseInfo, {prescriptionType: 1, diagnosis: '', usageInstruction: '', details: []});
  Object.assign(inspectionForm, baseInfo, {
    inspectionItemId: null,
    inspectionItemName: '',
    bodyPart: '',
    inspectionPurpose: '',
    clinicalDiagnosis: '',
    isEmergency: 0
  });
  Object.assign(laboratoryForm, baseInfo, {
    laboratoryItemId: null,
    laboratoryItemName: '',
    specimenType: '血液',
    laboratoryPurpose: '',
    clinicalDiagnosis: '',
    isFasting: 0,
    isEmergency: 0
  });
  const params = {patientId: row.patientId, registId: row.registId};
  // 尝试加载已有的病历
  const res = await getByRegistId(params);
  Object.assign(recordForm, res.data);
  // 切换患者：病历定位 tab 重置回「主诉与病史」，不带上一个患者的高亮状态
  activeRecordSection.value = 'chief';
  // 病史带入：本次病历未写的字段，按「上次门诊病历 > 患者健康档案」预填（异步，不阻塞接诊区其他加载）
  prefillHistories(row);
  // 分诊带入：分诊台预检登记的当次生命体征与主诉 → 体格检查生命体征区/主诉（只填空字段，见 loadTriagePrefill）
  loadTriagePrefill(row.id);
  // 四个区块并发加载，统一挂「加载中」标记（各 loader 自己吞异常）
  // （原「既往病历」loader 已随左栏既往页签移除 —— 历史就诊改由患者条上的入口
  //   打开患者详情弹窗，内嵌 CDR 全景时间轴，取数口径也换成患者级的 getPatientCdr）
  // 加载检查申请记录
  trackPatientLoad(loadInspectionRecords(params));
  // 加载检验申请记录
  trackPatientLoad(loadLaboratoryRecords(params));
  // 加载已有处方
  trackPatientLoad(loadPrescriptionRecords(params));
  // 加载患者标签
  loadPatientTags();
  // 加载患者详情
  loadPatientDetail();
  // 加载收费信息
  // 加载医保信息
  loadInsuranceInfo();
};
const loadInspectionRecords = async (params) => {
  try {
    // 获取申请列表用于显示状态
    const applyRes = await getInspectionApplyList(params);
    inspectionRecords.value = (applyRes.data || []).map((r) => ({
      ...r,
      // 字段归一：服务端 VO 用 specialRequirements，行模板 v-model 用 preparation
      preparation: r.specialRequirements || '',
    }));
  } catch (error) {
    inspectionRecords.value = [];
  }
};
const loadLaboratoryRecords = async (params) => {
  try {
    // 获取申请列表用于显示状态
    const applyRes = await getLaboratoryApplyList(params);
    laboratoryRecords.value = applyRes.data || [];
  } catch (error) {
    laboratoryRecords.value = [];
  }
};
const loadPrescriptionRecords = async (params) => {
  try {
    const res = await getPrescriptionList(params);
    prescriptionList.value = res.data || [];
    // 保持当前选中索引不越界
    if (currentPrescriptionIdx.value >= prescriptionList.value.length) {
      currentPrescriptionIdx.value = 0;
    }
    bindPrescriptionForm();
    loadInsuranceInfo();
    // L7 审方退回重开闭环：被退回的处方要在医生选中患者第一时间可见（原因+次数），
    // 医生改方后重新保存病历即完成重提（后端先删后增继承退回次数并落重提流水）
    const returnedList = (res.data || []).filter((p) => p.prescriptionStatus === 7);
    if (returnedList.length) {
      const first = returnedList[0];
      ElMessage.warning({
        message: `有 ${returnedList.length} 张处方被审方退回（${first.returnCount > 1 ? `第 ${first.returnCount} 次` : '首次'}）：${first.returnReason || '未填写原因'}。请修改处方后重新保存病历提交。`,
        duration: 8000,
        showClose: true,
      });
    }
  } catch (error) {
    prescriptionList.value = [];
    prescriptionForm.details = [];
  }
};
const switchPrescription = () => {
  bindPrescriptionForm();
  loadInsuranceInfo();
};
watch(currentPrescriptionType, (_, oldType) => {
  // 切 tab 不做必填校验（用户明确要求：开方过程中允许留空，结诊时统一校验）
  // 保存旧类型的处方数据（剂数/煎服方式同在处方头，切走前必须回写，否则切回来就丢了）
  const oldPrescription = prescriptionList.value.find(p => p.prescriptionType === oldType);
  if (oldPrescription) {
    oldPrescription.details = [...prescriptionForm.details];
    oldPrescription.doseCount = prescriptionForm.doseCount;
    oldPrescription.decoctFlag = prescriptionForm.decoctFlag;
    oldPrescription.isLongPrescription = prescriptionForm.isLongPrescription ? 1 : 0;
    oldPrescription.longPrescriptionDays = prescriptionForm.isLongPrescription ? prescriptionForm.longPrescriptionDays : null;
  }
  // 加载新类型的处方数据
  bindPrescriptionForm();
});
const hasPrescriptionType = (type) => {
  return prescriptionList.value.some((p) => p.prescriptionType === type);
};
const handleAddPrescription = (type) => {
  if (hasPrescriptionType(type)) {
    ElMessage.warning(`${prescriptionTypeLabel(type)}处方已存在`);
    return;
  }
  const newPrescription = {
    prescriptionType: type,
    diagnosis: '',
    usageInstruction: '',
    isLongPrescription: 0,
    longPrescriptionDays: null,
    details: [],
  };
  prescriptionList.value.push(newPrescription);
  currentPrescriptionIdx.value = prescriptionList.value.length - 1;
  prescriptionForm.details = newPrescription.details;
  ElMessage.success(`已创建${prescriptionTypeLabel(type)}处方`);
};
// ========== 叫下一位 ==========
const RECORD_TEXT_KEYS_FOR_AUTOSAVE = ['chiefComplaint', 'presentIllness', 'allergyHistory',
  'pastHistory', 'personalHistory', 'familyHistory', 'marriageHistory', 'temperature', 'pulse',
  'respiration', 'systolicPressure', 'diastolicPressure', 'generalCondition', 'skinMucosa', 'headNeck',
  'chestLung', 'heart', 'abdomen', 'spineLimbs', 'nervousSystem', 'specialistExam', 'auxiliaryExam',
  'diagnosis', 'diagnosisCode', 'diagnosisName', 'treatmentPlan'];
const handleCallNext = async () => {
  // 如果有当前就诊患者，先检查是否需要保存
  if (currentCalledPatient.value) {
    const hasRecordText = RECORD_TEXT_KEYS_FOR_AUTOSAVE.some((k) => !!recordForm[k]);
    const hasPendingRx = prescriptionList.value.some((rx) => (rx.details?.length ?? 0) > 0);
    const hasUnsavedData = hasRecordText || hasPendingRx
        || inspectionRecords.value.length > 0
        || laboratoryRecords.value.length > 0;
    // 有未保存的数据就自动保存（保存接口幂等：病历整体覆盖、申请单按 id 增/改，重复保存无害）
    if (hasUnsavedData) {
      // handleSaveRecord 返回 false = 保存失败（错误已由全局拦截器提示过）。
      // 保存失败必须阻断叫号：静默放行 = 未保存内容（含处方）在收口后直接丢失。
      // 给医生显式二选一：返回修改 / 确认放弃未保存内容直接叫号。
      const saved = await handleSaveRecord();
      if (!saved) {
        try {
          await ElMessageBox.confirm(
              `<div style="line-height:1.7">当前病历自动保存失败，未保存的修改将在结束本就诊后丢失。<br>` +
              '请先返回完善，或明确确认放弃。',
              '自动保存失败',
              {
                confirmButtonText: '放弃保存，直接叫号',
                cancelButtonText: '返回修改',
                type: 'warning',
                dangerouslyUseHTMLString: true
              }
          );
        } catch (cancel) {
          return; // 医生选择返回修改，不叫号
        }
      } else {
        ElMessage.success('已自动保存当前病历');
      }
    }
  }
  // 呼叫下一位
  try {
    await ElMessageBox.confirm('确认接诊下一位候诊患者？', '接诊确认', {
      confirmButtonText: '确认接诊',
      cancelButtonText: '取消',
      type: 'info'
    });
  } catch (error) {
    return;
  }
  // 呼叫下一位
  try {
    // 接诊对象一律以服务端回执为准，前端不猜。
    const res = await callNextQueue({});
    const called = res?.data;
    if (!called || !called.id) {
      ElMessage.warning('已发送叫号，但未取到接诊回执，请刷新队列确认');
      await loadData();
      return;
    }
    if (called.previousPatientName) {
      // 同一医生同一时刻只允许一条「就诊中」，叫下一位时服务端会收掉上一条。
      // 如实告知，不静默改状态（医生可能只是想让上一位「挂起」，那就该走暂离/挂起）。
      ElMessage.info(`已自动结束上一位就诊：${called.previousPatientName}`);
    }
    await loadData();
    await selectPatient(called);
    ElMessage.success(`已接诊：${called.patientName}（${shortQueueNo(called.queueNo)}）`);
  } catch (error) {
    const msg = error.message || '叫号失败';
    if (msg.includes('没有候诊患者')) {
      ElMessage.warning('当前科室没有候诊患者');
    } else {
      ElMessage.error(msg);
    }
  }
};
// ========== 重呼当前患者 ==========
const handleRecallPatient = async () => {
  if (!currentCalledPatient.value) {
    ElMessage.warning('当前没有就诊中的患者');
    return;
  }
  try {
    await recallPatient(currentCalledPatient.value.id);
    ElMessage.success(`已重呼 ${currentCalledPatient.value.patientName}`);
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
// ========== 呼叫指定患者（插队）/ 回诊 ==========
const handleCallSpecific = async (row) => {
  // 队列 4-已就诊 的行走的是「回诊」：上一位只是被叫号自动收口、病历并未结诊。
  const reconsult = row.queueStatus === 4;
  try {
    const msg = reconsult
        ? (currentCalledPatient.value
            ? `当前就诊中：${currentCalledPatient.value.patientName}，确认结束并回诊 ${row.patientName}？`
            : `确认回诊 ${row.patientName}？回诊后继续书写的将是本次就诊的复诊记录。`)
        : currentCalledPatient.value
            ? `当前就诊中：${currentCalledPatient.value.patientName}，确认切换并呼叫 ${row.patientName}？`
            : `确认呼叫 ${row.patientName}？`;
    await ElMessageBox.confirm(msg, reconsult ? '回诊确认' : '呼叫确认', {
      confirmButtonText: reconsult ? '确认回诊' : '确认呼叫',
      cancelButtonText: '取消',
      type: 'warning'
    });
    // 呼叫指定患者：接诊对象用服务端回执，前端不再「等 500ms 再猜」。
    const res = await callPatient(row.id);
    const called = res?.data || row;
    if (called?.previousPatientName) {
      ElMessage.info(`已自动结束上一位就诊：${called.previousPatientName}`);
    }
    await loadData();
    await selectPatient(called);
    ElMessage.success(`${called.reconsult || reconsult ? '已回诊' : '已呼叫'} ${called.patientName || row.patientName}`);
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
// ========== 过号 / 过号回队 ==========
const handleOverdue = async (row) => {
  try {
    await ElMessageBox.confirm(
        `确认将 ${row.patientName}（${shortQueueNo(row.queueNo)}）标记为过号？过号后可随时回队。`,
        '过号处理', {confirmButtonText: '确认过号', cancelButtonText: '取消', type: 'warning'});
  } catch {
    return;
  }
  try {
    await overdueQueue(row.id);
    ElMessage.success(`已标记过号：${row.patientName}`);
    await loadData();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleRejoin = async (row) => {
  try {
    await rejoinQueue(row.id, 2);
    ElMessage.success(`已回队：${row.patientName} 排到候诊队尾`);
    await loadData();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
// ========== 暂离诊室 / 恢复接诊 ==========
const doctorPaused = ref(false);
const loadDoctorStatus = async () => {
  try {
    const res = await getCurrentDoctorStatus();
    doctorPaused.value = (res.data?.status ?? 0) === 2;
  } catch (error) {
    console.error('读取接诊状态失败:', error);
  }
};
const handlePause = async () => {
  const next = doctorPaused.value ? 0 : 2;
  try {
    await setDoctorStatus(next);
    doctorPaused.value = next === 2;
    ElMessage.success(next === 2 ? '已暂离，叫号将跳过本诊室' : '已恢复接诊');
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
// ========== 结诊 ==========
const handleComplete = async () => {
  if (!currentPatient.value)
    return;
  // 前置状态校验：只有 3-就诊中 / 4-已就诊（呼叫下一位自动收口、未结诊的回诊）允许结诊；
  const qs = currentPatient.value.queueStatus;
  if (qs !== 3 && qs !== 4) {
    await loadData();
    ElMessage.warning('该患者当前不在就诊中，无法结诊，已为你刷新队列，请核对后重试');
    return;
  }
  // 检查病历必填项
  const requiredFields = [
    {field: recordForm.chiefComplaint, label: '主诉', value: 'chiefComplaint'},
    {field: recordForm.presentIllness, label: '现病史', value: 'presentIllness'},
    {field: recordForm.allergyHistory, label: '过敏史', value: 'allergyHistory'},
    {field: recordForm.pastHistory, label: '既往史', value: 'pastHistory'},
    {field: recordForm.personalHistory, label: '个人史', value: 'personalHistory'},
    {field: recordForm.familyHistory, label: '家族史', value: 'familyHistory'},
    {field: recordForm.temperature, label: '体温', value: 'temperature'},
    {field: recordForm.systolicPressure, label: '收缩压', value: 'systolicPressure'},
    {field: recordForm.diagnosis, label: '诊断', value: 'diagnosis'},
  ];
  const missingFields = requiredFields.filter(item => !item.field);
  // 如果有未填写的必填项，提示医生
  if (missingFields.length > 0) {
    const missingListHtml = missingFields.map(item => `<div class="flex items-center justify-between py-1">
        <span class="text-red-600">✗ ${item.label}</span>
      </div>`).join('');
    try {
      await ElMessageBox.confirm(`<div class="space-y-2">
          <div class="text-sm text-slate-600">以下必填项尚未填写：</div>
          <div class="rounded-lg bg-red-50 p-3 text-sm">${missingListHtml}</div>
          <div class="text-xs text-slate-400 mt-2">请先填写完整后再结诊，无相关内容请在字段旁点击「填写无」</div>
        </div>`, '病历未完成', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '我知道了',
        showCancelButton: false,
        type: 'warning',
      });
    } catch (action) {
      return;
    }
    return; // 不继续结诊，让医生先填写
  }
  // 校验医嘱必填项：处方行必填 + 检查/检验项目必填（表格列头标了 *，结诊统一校验）
  const rxErrors = validateAllPrescriptions();
  const inspErrors = inspectionRecords.value
      .map((item, idx) => ({item, idx}))
      .filter(({item}) => !item.inspectionItemId)
      .map(({idx}) => `检查申请第${idx + 1}项：请选择检查项目`);
  const labErrors = laboratoryRecords.value
      .map((item, idx) => ({item, idx}))
      .filter(({item}) => !item.laboratoryItemId)
      .map(({idx}) => `检验申请第${idx + 1}项：请选择检验项目`);
  const orderErrors = [...rxErrors, ...inspErrors, ...labErrors];
  if (orderErrors.length > 0) {
    const errorListHtml = orderErrors.map(e => `<div class="py-0.5">• ${e}</div>`).join('');
    try {
      await ElMessageBox.confirm(`<div class="space-y-2">
          <div class="text-sm text-slate-600">医嘱必填项尚未填写完整：</div>
          <div class="rounded-lg bg-red-50 p-3 text-sm max-h-40 overflow-y-auto">${errorListHtml}</div>
          <div class="text-xs text-slate-400 mt-2">请先填写完整后再结诊。</div>
        </div>`, '医嘱未完成', {
        dangerouslyUseHTMLString: true,
        confirmButtonText: '我知道了',
        showCancelButton: false,
        type: 'warning',
      });
    } catch (action) {
      return;
    }
    return;
  }
  // 检查各项状态（包括暂存的检查检验）
  const hasPrescriptions = prescriptionForm.details.length > 0;
  const hasInspections = inspectionRecords.value.length > 0;
  const hasLaboratories = laboratoryRecords.value.length > 0;
  const hasAnyCharge = hasPrescriptions || hasInspections || hasLaboratories;
  // 构建检查清单
  const checkItems = [
    {label: '已开具处方', checked: hasPrescriptions, critical: false},
    {label: '已开具检查', checked: hasInspections, critical: false},
    {label: '已开具检验', checked: hasLaboratories, critical: false},
  ];
  // 构建确认内容HTML
  const checkListHtml = checkItems.map(item => `<div class="flex items-center gap-2 py-1.5">
      <span class="${item.checked ? 'text-emerald-500' : 'text-amber-500'}">${item.checked ? '✓' : '○'}</span>
      <span class="${item.checked ? 'text-slate-700' : 'text-slate-500'}">${item.label}</span>
    </div>`).join('');
  let warningHtml = '';
  if (!hasAnyCharge) {
    warningHtml = `<div class="mt-3 rounded-lg bg-amber-50 border border-amber-200 p-3 text-sm text-amber-700">
      ⚠ 当前没有开具任何处方/检查/检验，患者无需缴费。
    </div>`;
  }
  try {
    await ElMessageBox.confirm(`<div class="space-y-2">
        <div class="text-sm text-slate-600">确认完成该患者的就诊？</div>
        <div class="rounded-lg bg-slate-50 p-3 text-sm">${checkListHtml}</div>
        ${warningHtml}
        <div class="text-xs text-slate-400 mt-2">系统将自动保存并提交病历，生成收费单。</div>
      </div>`, '结诊确认', {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '确认完成',
      cancelButtonText: '返回修改',
      type: 'info',
    });
  } catch (action) {
    return; // 用户取消
  }
  // 获取患者信息并构建提交数据
  const patient = currentPatient.value;
  const submitData = {
    // 患者信息
    patientId: patient.patientId,
    patientNo: patient.patientNo,
    patientName: patient.patientName,
    registId: patient.registId,
    queueId: patient.id,
    // 科室医生信息
    deptId: patient.deptId,
    deptName: patient.deptName,
    doctorId: patient.doctorId,
    doctorName: patient.doctorName,
    // 病历信息
    recordId: recordForm.id || null,
    chiefComplaint: recordForm.chiefComplaint,
    presentIllness: recordForm.presentIllness,
    allergyHistory: recordForm.allergyHistory,
    pastHistory: recordForm.pastHistory,
    personalHistory: recordForm.personalHistory,
    familyHistory: recordForm.familyHistory,
    marriageHistory: recordForm.marriageHistory,
    temperature: recordForm.temperature,
    pulse: recordForm.pulse,
    respiration: recordForm.respiration,
    systolicPressure: recordForm.systolicPressure,
    diastolicPressure: recordForm.diastolicPressure,
    generalCondition: recordForm.generalCondition,
    specialistExam: recordForm.specialistExam,
    auxiliaryExam: recordForm.auxiliaryExam,
    diagnosis: recordForm.diagnosis,
    diagnosisCode: recordForm.diagnosisCode,
    diagnosisName: recordForm.diagnosisName,
    treatmentPlan: recordForm.treatmentPlan,
    // 处方信息（所有处方）
    prescriptions: buildPrescriptionPayload(),
    // 检查/检验申请（模式A）：随结诊提交落库 + 统一电子签名（内容最终确认、签名即锁定）；
    // 编辑期只留在本地数组，不产生中间态废单。
    inspectionApplies: buildInspectionApplyPayload(),
    laboratoryApplies: buildLaboratoryApplyPayload(),
  };
  // 有处方先取医师签名笔迹（视觉落款）：档案已维护笔迹 → 默认直接使用不弹框；
  // 档案没有 → 才退回签名板弹框手写
  if (submitData.prescriptions.length > 0) {
    pendingSubmitData.value = submitData;
    rxSignature.value = '';
    let archiveSign = '';
    try {
      const res = await getEmployeeCurrent();
      if (res.code === 200 && res.data?.signImage) {
        archiveSign = res.data.signImage;
      }
    } catch (error) /* 拉不到档案笔迹时退回弹框手写 */ {
    }
    if (archiveSign) {
      await onRxSigned(archiveSign);
      return;
    }
    rxSignDialogVisible.value = true;
    return;
  }
  try {
    await doSubmitRecord(submitData);
  } catch (error) /* 提示与弹框全在 doSubmitRecord 里收口 */ {

  }
};

// 实际结诊提交（签名板确认后或无处方时调用）
// forceSubmit=true 表示医生在否决项弹框里点了「仍要结诊」：缺陷不会消失，结诊收尾照旧出质控单留痕
const doSubmitRecord = async (submitData, forceSubmit = false) => {
  submitting.value = true;
  try {
    await submitMedicalRecord({...submitData, forceSubmit}, {silent: true});
    ElMessage.success('结诊成功');
    // 异步预生成就诊文书（引导单/处方签 PDF）：不 await、不阻塞结诊流程，
    // 生成完成后自动弹出「结诊文书」供医生打印给患者（缴费二维码在引导单左上角）
    prepGuideDocs();
    // 清空暂存的检查检验数据
    inspectionRecords.value = [];
    laboratoryRecords.value = [];
    // 刷新队列
    loadData();
  } catch (error) {
    // 本请求带 silent，提示归这里管（否决项要弹框，不能让全局红条先弹一遍）
    const message = String(error?.message || '');
    const vetoAction = await handleEmrSubmitVeto(error);
    if (vetoAction === 'force') {
      await doSubmitRecord(submitData, true);
      return;
    }
    if (vetoAction === 'cancel') {
      return;
    }
    ElMessage.error(message || '结诊失败');
    // 乐观锁冲突（挂号/队列状态已被并发操作变更）：这里自动刷新队列并引导重试，不静默也不让医生对着过期状态干瞪眼。
    if (message.includes('状态已变更')) {
      await loadData();
      ElMessage.info('就诊状态已被其他操作变更，已为你刷新队列，请核对后重试');
    }
  } finally {
    submitting.value = false;
  }
};

// ========== 撤回结诊 ==========
// 真实 HIS 门诊撤回惯例：医生刚结诊发现开错，在当前患者身上直接撤回（不是单独菜单——那是病案修改申请）。
// 后端闸门：本人当日、已提交(2)、未缴费、未执行；已归档(3) 后端拒。撤回后病历回草稿、队列回就诊中。
const revoking = ref(false);
const handleRevokeRecord = async () => {
  if (!recordForm.id || !currentPatient.value) return;
  try {
    await ElMessageBox.confirm(
        '确认撤回本次结诊？病历将回到草稿态重新编辑，已开处方/检查/检验的待缴账单同步作废。\n（已缴费或已执行的病历不允许撤回，需到收费台/医技科办理）',
        '撤回结诊',
        {confirmButtonText: '确认撤回', cancelButtonText: '再想想', type: 'warning'});
  } catch {
    return;
  }
  revoking.value = true;
  try {
    await revokeMedicalRecord(recordForm.id, currentPatient.value.id, '医生撤回结诊');
    ElMessage.success('已撤回结诊，病历回到草稿态');
    await loadData();
  } catch (error) {
    // 后端 BusinessException 的可读原因已由全局拦截器提示（如「病历已归档」「已缴费」等）
  } finally {
    revoking.value = false;
  }
};

// 签名板确认：笔迹回填每张处方后提交（电子证书签名由后端自动完成）
const onRxSigned = async (dataUrl) => {
  rxSignature.value = dataUrl || '';
  rxSignDialogVisible.value = false;
  const data = pendingSubmitData.value;
  pendingSubmitData.value = null;
  if (!data) return;
  data.prescriptions = data.prescriptions.map((rx) => ({...rx, doctorSignature: rxSignature.value}));
  try {
    await doSubmitRecord(data);
  } catch (error) /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};

const onRxSignCancel = () => {
  rxSignDialogVisible.value = false;
  pendingSubmitData.value = null;
  rxSignature.value = '';
};
// ========== 建复诊（批次E/E6）==========
// 位置：检查/检验「结果」条目的操作里 —— 报告回来 → 一键建复诊 → 直接接诊。
// 复诊免门诊诊查费（后端 waived：收费单金额 0 且直接置「已收费」，否则签不了到），
// 并带上 **原病历ID** 做关联 —— 只建立引用关系，原病历一律不改。
const canCreateRevisit = (item) => !!item?.execRecordId && !!recordForm.id && !!currentPatient.value;
/**
 * 该条既往病历是否就是本次复诊号关联的原病历。
 * 雪花ID 前后端都按字符串走，统一 String() 比较，避免 number/string 不等。
 */
const isLinkedRevisitRecord = (record) => {
  const linked = currentPatient.value?.revisitRecordId;
  return !!linked && !!record?.id && String(record.id) === String(linked);
};
const handleCreateRevisit = async (item) => {
  if (!currentPatient.value) {
    ElMessage.warning('请先选择患者');
    return;
  }
  // 复诊号必须挂在一份真实病历上；没保存病历就没有可关联的原病历
  if (!recordForm.id) {
    ElMessage.warning('请先保存病历，再建复诊（复诊号要关联本次病历）');
    return;
  }
  const itemName = item?.inspectionItemName || item?.laboratoryItemName || '本次就诊';
  const originLabel = recordForm.recordNo || String(recordForm.id);
  try {
    await ElMessageBox.confirm(`患者 ${currentPatient.value.patientName} 的「${itemName}」结果已回，确定为本次就诊创建复诊号？\n` +
        `关联原病历：${originLabel}（原病历不改动），收不收费由「复诊收费策略」判定（铺底策略：当日回诊全免）。`, '建复诊', {
      confirmButtonText: '确定创建',
      cancelButtonText: '取消',
      type: 'info'
    });
    // 当日回诊（来源 1）：不占号源、不选排班，它是同一次挂号的延续
    await createRevisitRegistration({
      patientId: currentPatient.value.patientId,
      // 挂号来源（AppointUpsertDTO 已必填）：当日回诊既非自助也非预约，沿用原默认口径「1-窗口挂号」
      registSource: 1,
      scheduleId: null, // 复诊不需要号源
      settlementType: currentPatient.value.settlementType || 1,
      medicalInsuranceType: currentPatient.value.medicalInsuranceType || '',
      medicalInsuranceNo: currentPatient.value.medicalInsuranceNo || '',
      visitType: 2, // 标记为复诊
      revisitSource: REVISIT_SOURCE.SAME_DAY_RETURN,
      revisitRecordId: recordForm.id, // 关联原病历（后端校验归属）
    });
    ElMessage.success('复诊号已创建（免门诊诊查费），签到后即可接诊');
    loadData();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
// ========== 医嘱复诊预约（来源 2，sql/121） ==========
const showRevisitAppoint = ref(false);
const revisitAppointBusy = ref(false);
/**
 * 与上面「建复诊」的区别：建复诊是**当日回诊**（结果回来了，不占号源、按策略全免）；
 * 这里是医生替患者约**未来某一次**就诊（拆线、化疗下一程、复查后再看），
 * 它占号源、按「复诊收费策略」收钱，所以必须能在提交前看到金额。
 */
const openRevisitAppoint = () => {
  if (!recordForm.id) {
    ElMessage.warning('请先保存病历，再预约复诊（复诊号要关联本次病历）');
    return;
  }
  showRevisitAppoint.value = true;
};
const handleRevisitAppoint = async (payload) => {
  if (!currentPatient.value)
    return;
  revisitAppointBusy.value = true;
  try {
    const res = await createRevisitRegistration({
      patientId: currentPatient.value.patientId,
      // 医生替患者约未来时段 = 预约渠道，扣预约池（不许吃现场余号）
      registSource: 4,
      visitType: 2,
      revisitSource: REVISIT_SOURCE.DOCTOR_ORDERED,
      ...payload,
    });
    showRevisitAppoint.value = false;
    ElMessage.success(`复诊号已预约${res.data?.registNo ? `（${res.data.registNo}）` : ''}，请让患者按预约时段来院`);
    loadData();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  } finally {
    revisitAppointBusy.value = false;
  }
};
// ========== 临时保存 ==========
const handleSaveRecord = async () => {
  if (!currentPatient.value)
    return;
  try {
    // 构建保存数据
    const saveData = {
      // 患者信息
      patientId: currentPatient.value.patientId,
      patientNo: currentPatient.value.patientNo,
      patientName: currentPatient.value.patientName,
      registId: currentPatient.value.registId,
      queueId: currentPatient.value.id,
      // 科室医生信息
      deptId: currentPatient.value.deptId,
      deptName: currentPatient.value.deptName,
      doctorId: currentPatient.value.doctorId,
      doctorName: currentPatient.value.doctorName,
      // 病历信息
      recordId: recordForm.id || null,
      chiefComplaint: recordForm.chiefComplaint,
      presentIllness: recordForm.presentIllness,
      // G-10：仅当本会话用过 AI 草稿且终稿已与其不同才带草稿原文（后端据此算 diff 留痕）
      aiDraftPresentIllness: (aiDraftAppliedText.value && recordForm.presentIllness !== aiDraftAppliedText.value)
          ? aiDraftAppliedText.value : undefined,
      allergyHistory: recordForm.allergyHistory,
      pastHistory: recordForm.pastHistory,
      personalHistory: recordForm.personalHistory,
      familyHistory: recordForm.familyHistory,
      marriageHistory: recordForm.marriageHistory,
      temperature: recordForm.temperature,
      pulse: recordForm.pulse,
      respiration: recordForm.respiration,
      systolicPressure: recordForm.systolicPressure,
      diastolicPressure: recordForm.diastolicPressure,
      generalCondition: recordForm.generalCondition,
      skinMucosa: recordForm.skinMucosa,
      headNeck: recordForm.headNeck,
      chestLung: recordForm.chestLung,
      heart: recordForm.heart,
      abdomen: recordForm.abdomen,
      spineLimbs: recordForm.spineLimbs,
      nervousSystem: recordForm.nervousSystem,
      specialistExam: recordForm.specialistExam,
      auxiliaryExam: recordForm.auxiliaryExam,
      diagnosis: recordForm.diagnosis,
      diagnosisCode: recordForm.diagnosisCode,
      diagnosisName: recordForm.diagnosisName,
      treatmentPlan: recordForm.treatmentPlan,
      // 处方信息（所有处方）
      prescriptions: buildPrescriptionPayload(),
      // 检查/检验申请（模式A）：随病历保存落库 —— 医生工作台内的编辑只停留在本地数组，
      // 保存/结诊时才把申请单数组提交给后端（无 id 新增、有 id 更新，结诊统一签名锁定）。
      // 撤单走列表上的「删除」（未落库行本地删；已落库行走后端，带缴费/执行/收费引用三重保护）。
      inspectionApplies: buildInspectionApplyPayload(),
      laboratoryApplies: buildLaboratoryApplyPayload(),
    };
    // 调用保存接口
    const res = await saveMedicalRecord(saveData);
    if (res.data) {
      recordForm.id = res.data;
    }
    recordSaved.value = true;
    // 模式A：保存后申请单已落库，把服务端回的行 id / 状态 merge 回本地行 ——
    // 否则下次保存时行仍无 id，后端走「新行判重跳过」，对已保存行的修改会静默丢失。
    // 空白行（未选项目）不匹配服务端，原样保留；本地行结构（_pendingIdx 等）也保留。
    const applyParams = {
      patientId: currentPatient.value.patientId,
      registId: currentPatient.value.registId,
    };
    const [insRes, labRes] = await Promise.all([
      getInspectionApplyList(applyParams),
      getLaboratoryApplyList(applyParams),
    ]);
    const serverIns = insRes.data || [];
    const serverLab = labRes.data || [];
    for (const row of inspectionRecords.value) {
      if (!row.inspectionItemId) {
        continue;
      }
      const server = serverIns.find((s) => s.inspectionItemId === row.inspectionItemId);
      if (server) {
        const pendingIdx = row._pendingIdx;
        Object.assign(row, server);
        // 字段归一：服务端 VO 用 specialRequirements，行模板 v-model 用 preparation
        row.preparation = server.specialRequirements || row.preparation || '';
        if (pendingIdx) {
          row._pendingIdx = pendingIdx;
        }
      }
    }
    for (const row of laboratoryRecords.value) {
      if (!row.laboratoryItemId) {
        continue;
      }
      const server = serverLab.find((s) => s.laboratoryItemId === row.laboratoryItemId);
      if (server) {
        const pendingIdx = row._pendingIdx;
        Object.assign(row, server);
        if (pendingIdx) {
          row._pendingIdx = pendingIdx;
        }
      }
    }
    // 草稿会话结束：本次保存已产生 diff 留痕，下次保存不再重复带
    aiDraftAppliedText.value = '';
    ElMessage.success('病历保存成功');
    return true;
  } catch (error) {
    // 错误已由全局拦截器统一提示；返回 false 让「呼叫下一位」的自动保存感知失败并阻断叫号。
    // 之前这里静默吞掉异常，自动保存失败时医生看不到任何提示、叫号照常执行，未保存内容在收口后直接丢失。
    return false;
  }
};
const handleRemoveDrug = (index) => {
  prescriptionForm.details.splice(index, 1);
  // 删除药品后重新预估
  loadInsuranceInfo();
  checkDrugSafety();
};
const handleAddEmptyDrug = () => {
  const type = currentPrescriptionType.value;
  // 如果该类型没有处方，先创建一个
  if (!hasPrescriptionType(type)) {
    const newPrescription = {
      prescriptionType: type,
      diagnosis: '',
      usageInstruction: '',
      details: [],
    };
    prescriptionList.value.push(newPrescription);
    prescriptionForm.details = newPrescription.details;
  }
  const base = {
    drugId: null, drugCode: '', drugName: '', genericName: '',
    specification: '', dosageForm: '', unit: '盒', quantity: 1,
    price: 0, usageDosage: '', amount: 0,
  };
  if (type === 3) {
    // 中药饮片：总量/金额不手填，选药 + 填每剂克数后按剂数算出来
    prescriptionForm.details.push({
      ...base, unit: 'g', quantity: 0, singleDosage: '', frequency: '每日一剂',
      route: tcmMethodDict.value[0]?.dictValue || '水煎服', duration: 7,
    });
  } else if (type === 2) {
    // 中成药
    prescriptionForm.details.push({
      ...base, singleDosage: '', frequency: '一日三次', route: '口服', duration: 7,
    });
  } else {
    // 西药
    prescriptionForm.details.push({
      ...base, singleDosage: '', frequency: '一日三次', route: '口服', duration: 7,
    });
  }
};
// ========== 处方必填校验 ==========
const validatePrescription = (type) => {
  const errors = [];
  const isCurrent = type === currentPrescriptionType.value;
  const prescription = prescriptionList.value.find(p => p.prescriptionType === type) || {};
  const details = prescription.details || [];
  if (details.length === 0)
    return errors;
  // 饮片方的剂数是「一张方一个」的处方头字段，后端强校验 1~30，且必须选代煎/自煎
  if (type === 3) {
    const doseCount = isCurrent ? prescriptionForm.doseCount : prescription.doseCount;
    const decoctFlag = isCurrent ? prescriptionForm.decoctFlag : prescription.decoctFlag;
    if (!doseCount || doseCount < 1 || doseCount > 30)
      errors.push('中药饮片处方：请填写剂数（1~30 剂）');
    if (decoctFlag !== 1 && decoctFlag !== 2)
      errors.push('中药饮片处方：请选择煎服方式（代煎 / 自煎）');
  }
  details.forEach((item, idx) => {
    const prefix = `${prescriptionTypeLabel(type)}处方第${idx + 1}项`;
    if (!item.drugId)
      errors.push(`${prefix}：请选择药品`);
    // 校验文案按类型精确到字段名：饮片行内是「每剂克数」，西药/中成药是「单次用量」，不笼统说「用量」
    if (!item.singleDosage)
      errors.push(`${prefix}：${type === 3 ? '请填写每剂克数' : '请填写单次用量'}`);
    if (!item.frequency)
      errors.push(`${prefix}：请填写频次/用法`);
    if (!item.route)
      errors.push(`${prefix}：请填写给药途径/煎法`);
    if (type === 3 && item.drugId && tcmPerDoseGrams(item) <= 0) {
      errors.push(`${prefix}：每剂克数必须是数字（如 15，不要写「15g」「适量」）`);
    }
    if (!item.quantity || item.quantity <= 0)
      errors.push(`${prefix}：请填写正确的总量/剂数`);
    if (type !== 3 && (!item.duration || item.duration <= 0))
      errors.push(`${prefix}：请填写天数`);
  });
  return errors;
};
const validateAllPrescriptions = () => {
  return [1, 2, 3].flatMap(type => validatePrescription(type));
};
/**
 * 病历提交用的处方载荷。剂数/煎服方式存在处方头，而当前 tab 的编辑态在 prescriptionForm 上，
 * 所以「正在编的那张」取表单值、其余取各自对象上的值（两处提交站点共用，避免口径漂移）。
 */
// 结诊医师手写笔迹（PNG dataURL）；电子证书签名由后端自动完成，本值仅视觉落款
const rxSignature = ref('');
const rxSignDialogVisible = ref(false);
const pendingSubmitData = ref(null);

const buildPrescriptionPayload = () => {
  return prescriptionList.value
      .filter((p) => p.details && p.details.length > 0)
      .map((p) => {
        const isCurrent = p.prescriptionType === currentPrescriptionType.value;
        return {
          prescriptionType: p.prescriptionType || 1,
          doseCount: isCurrent ? prescriptionForm.doseCount : p.doseCount,
          decoctFlag: isCurrent ? prescriptionForm.decoctFlag : p.decoctFlag,
          // 长处方（处方级）：当前 tab 取编辑态、其余取卡上值；关闭时天数置空由后端落 NO
          isLongPrescription: isCurrent ? prescriptionForm.isLongPrescription : p.isLongPrescription === 1,
          longPrescriptionDays: (isCurrent ? prescriptionForm.isLongPrescription : p.isLongPrescription === 1)
              ? (isCurrent ? prescriptionForm.longPrescriptionDays : p.longPrescriptionDays) : null,
          // 手写笔迹本次结诊一张，所有处方共用；验签仍以电子证书签名为准
          doctorSignature: rxSignature.value || null,
          details: p.details.map((d) => ({
            drugId: d.drugId,
            drugName: d.drugName,
            specification: d.specification,
            unit: d.unit,
            singleDosage: d.singleDosage,
            frequency: d.frequency,
            route: d.route,
            quantity: d.quantity,
            duration: d.duration,
            remark: d.remark,
          })),
        };
      });
};

/** 模式A：检查申请随病历提交的 payload（只提交已选项目行；无 id 新增、有 id 更新）。 */
const buildInspectionApplyPayload = () => {
  return inspectionRecords.value
      .filter((r) => r.inspectionItemId)
      .map((r) => ({
        id: r.id || undefined,
        itemId: r.inspectionItemId,
        bodyPart: r.bodyPart,
        purpose: r.inspectionPurpose,
        clinicalDiagnosis: r.clinicalDiagnosis || recordForm.diagnosis || '',
        isEmergency: r.isEmergency ?? 0,
        specialRequirements: r.preparation,
      }));
};

/** 模式A：检验申请随病历提交的 payload（语义同检查）。 */
const buildLaboratoryApplyPayload = () => {
  return laboratoryRecords.value
      .filter((r) => r.laboratoryItemId)
      .map((r) => ({
        id: r.id || undefined,
        itemId: r.laboratoryItemId,
        specimenType: r.specimenType || '',
        purpose: r.laboratoryPurpose,
        clinicalDiagnosis: r.clinicalDiagnosis || recordForm.diagnosis || '',
        isFasting: r.isFasting ?? 0,
        isEmergency: r.isEmergency ?? 0,
      }));
};

/**
 * 模式A：申请单行可编辑条件。
 * 未落库（本地编辑态）可编辑；已落库仅「已提交(1) 且 未签名」可编辑 ——
 * 已缴费(2)/已取消(6)/已签名行以库为准、禁编辑（后端保存病历对它们也跳过更新）。
 */
const applyRowEditable = (item) => {
  if (!item || !item.id)
    return true;
  return item.applyStatus === 1 && item.signStatus !== 1;
};
const handleDrugSelectForRecord = (record, val) => {
  // selectList 出参主键字段是 id（无 drugId），选中值 val 即 drug.id（Long 序列化为字符串）
  const drug = drugResults.value.find((d) => String(d.id) === String(val));
  if (drug) {
    record.drugId = drug.id;
    record.drugCode = drug.drugCode;
    record.drugName = drug.drugName;
    record.specification = drug.specification || '';
    record.unit = drug.unit || '盒';
    record.price = drug.retailPrice || 0;
    // 饮片按克开方：行上存的单价一律是「元/克」，界面预估和后端落库同一个口径
    // （元/kg 留在行上迟早被谁乘一次克数，金额直接放大一千倍）
    if (currentPrescriptionType.value === 3) {
      record.price = tcmPerGramPrice(drug);
      applyTcmGrams(record);
    } else {
      record.amount = record.quantity * (drug.retailPrice || 0);
    }
  }
};
// ========== 检查/检验申请（含知情同意） ==========
const pendingInspectionIdx = ref(0);
const handleAddEmptyInspection = () => {
  inspectionRecords.value.push({
    _pendingIdx: ++pendingInspectionIdx.value,
    inspectionItemId: null,
    inspectionItemName: '',
    // 批次E/F：不再写 applyStatus=0 —— E4 收口后申请单只有 1已提交/2已缴费/6已取消，
    // 未落库的空行没有申请单状态可言（有没有落库看 id）
    bodyPart: '',
    inspectionPurpose: '',
    preparation: '',
    clinicalDiagnosis: '',
    isEmergency: 0,
    price: 0,
  });
};
const pendingLaboratoryIdx = ref(0);
const handleAddEmptyLaboratory = () => {
  laboratoryRecords.value.push({
    _pendingIdx: ++pendingLaboratoryIdx.value,
    laboratoryItemId: null,
    laboratoryItemName: '',
    specimenType: '',
    laboratoryPurpose: '',
    clinicalDiagnosis: '',
    isFasting: 0,
    isEmergency: 0,
    price: 0,
  });
};
// 批次E/F：删掉了 handleSaveInspection / handleSaveLaboratory。
// 批次E/E1 的「选项目即落库」（handleSaveInspectionRow / persistInspectionRow）也已随模式A改造删除：
// 现在医生工作台内的申请单编辑只留在本地数组，落库统一发生在「保存病历 / 结诊」时
// （随病历提交，后端 ensureXxx 落库 + 结诊统一签名），不再有第二套开单链路。
// ========== 删除申请单（批次E/E2：走后端，带保护） ==========
/**
 * 删除检查申请。
 *
 * 模式A下分两种：未落库行（本地编辑、id 为空）直接删本地数组；
 * 已落库行（之前保存病历/结诊产生的真实单据）走后端删除接口，
 * 能不能删由后端说了算（已缴费 / 已生成检查记录 / 已被收费单引用 一律拒绝并给原因），
 * 前端不自己判状态 —— 只删界面等于"界面上没了、库里还在"，刷新一下又回来。
 */
const handleDeleteInspectionRecord = async (idx) => {
  const row = inspectionRecords.value[idx];
  if (!row)
    return;
  if (!row.id) {
    // 未落库的本地编辑行：保存病历前删除只是放弃这一行，不产生任何库数据
    inspectionRecords.value.splice(idx, 1);
    loadInsuranceInfo();
    return;
  }
  if (row.canDelete === false) {
    ElMessage.warning(row.deleteBlockReason || '当前状态不允许删除');
    return;
  }
  try {
    await ElMessageBox.confirm(`确定删除检查申请「${row.inspectionItemName}」？`, '删除检查申请', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    });
  } catch {
    return;
  }
  try {
    await deleteInspectionApply(row.id);
    inspectionRecords.value.splice(idx, 1);
    ElMessage.success('检查申请已删除');
    loadInsuranceInfo();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
const handleDeleteLaboratoryRecord = async (idx) => {
  const row = laboratoryRecords.value[idx];
  if (!row)
    return;
  if (!row.id) {
    laboratoryRecords.value.splice(idx, 1);
    loadInsuranceInfo();
    return;
  }
  if (row.canDelete === false) {
    ElMessage.warning(row.deleteBlockReason || '当前状态不允许删除');
    return;
  }
  try {
    await ElMessageBox.confirm(`确定删除检验申请「${row.laboratoryItemName}」？`, '删除检验申请', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    });
  } catch {
    return;
  }
  try {
    await deleteLaboratoryApply(row.id);
    laboratoryRecords.value.splice(idx, 1);
    ElMessage.success('检验申请已删除');
    loadInsuranceInfo();
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
// ========== 查看报告 ==========
/**
 * 影像随报告一起可见（sql/137 简化 PACS）。
 *
 * 弹框内容是 HTML 字符串，塞不进带缩放/调窗状态的阅片器组件，所以这里只铺缩略图；
 * 放大与窗宽窗位在检查/检验工作站的阅片器里做。地址必须走 /api/ 前缀
 * （后端 context-path=/api，静态资源映射在 /uploads/**），与病历引导单同一口径。
 */
const htmlEsc = (v) => String(v ?? '').replace(/[&<>"]/g, (c) => ({
  '&': '&amp;',
  '<': '&lt;',
  '>': '&gt;',
  '"': '&quot;'
}[c]));
const examImagesHtml = (images, label) => {
  const list = Array.isArray(images) ? images.filter((i) => i?.fileUrl) : [];
  if (!list.length) {
    return `<div class="border-t pt-3 text-xs text-slate-400">本次${label}未挂影像帧</div>`;
  }
  const cells = list.map((i) => `<div>
      <img src="/api/${htmlEsc(i.fileUrl)}" alt="${htmlEsc(i.fileName || '影像')}"
           class="max-h-40 w-full rounded border border-slate-200 bg-black object-contain"/>
      <p class="mt-1 text-[11px] text-slate-500">#${htmlEsc(i.seq)} ${htmlEsc(i.modalityText || '未标模态')}${i.source === 2 ? '（模拟）' : ''}</p>
    </div>`).join('');
  return `<div class="border-t pt-3">
      <p class="font-bold text-slate-700 mb-2">影像（共 ${list.length} 帧，放大请在工作站阅片器打开）</p>
      <div class="grid grid-cols-4 gap-2 rounded bg-slate-900 p-2">${cells}</div>
    </div>`;
};
/**
 * 申请单/执行状态 → el-tag 颜色。
 *
 * 颜色只是提示，**文案一律用后端给的 `execStatusText`**：
 * 检查与检验的 record_status 是两套不同码表（检查 2=已签到，检验 2=已采样），
 * 前端按码值自己翻译必然翻错 —— 医生站曾经就把"已挂号"显示成过"未知"。
 */
const applyTagType = (item) => {
  if (item?.critical)
    return 'danger';
  const text = item?.execStatusText || '';
  if (text === '待缴费')
    return 'warning';
  if (text === '已取消')
    return 'info';
  if (text === '已出结果' || text === '已审核' || text === '已发布')
    return 'success';
  if (['检查中', '检测中', '已到检', '已采样', '已接收', '已缴费待执行'].includes(text))
    return 'primary';
  return 'info';
};
/**
 * 查看检查报告。
 *
 * 原实现直接读申请单上的 `resultDescription / resultConclusion` —— 这两个字段
 * 从来就不在申请单 VO 里，所以它永远只显示"检查项目/部位/目的"，外加一张写死的
 * `huichuan.png` 假影像。现在按执行记录 ID 真去取报告。
 */
const handleViewInspectionReport = async (item) => {
  if (!item?.execRecordId) {
    ElMessage.warning('该检查还没有对应的检查记录，暂无报告可看');
    return;
  }
  let data = null;
  try {
    const res = await getInspectionDetail(item.execRecordId);
    data = res.data || {};
  } catch (error) {

    return;
  }
  const record = data.record || {};
  const report = data.report || {};
  const escapeHtml = (v) => String(v ?? '-').replace(/[&<>"]/g, (c) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;'
  }[c]));
  const block = (title, body, cls = 'bg-slate-50') => body
      ? `<div class="border-t pt-3"><p class="font-bold text-slate-700 mb-2">${title}</p>
         <p class="text-sm ${cls} p-2 rounded whitespace-pre-wrap">${escapeHtml(body)}</p></div>`
      : '';
  ElMessageBox.alert(`<div class="space-y-3 w-full">
      <div class="grid grid-cols-2 gap-2 text-sm">
        <p><strong>检查项目：</strong>${escapeHtml(record.inspectionItemName || item.inspectionItemName)}</p>
        <p><strong>检查部位：</strong>${escapeHtml(record.bodyPart || item.bodyPart)}</p>
        <p><strong>检查科室：</strong>${escapeHtml(record.inspectionDeptName)}</p>
        <p><strong>报告医师：</strong>${escapeHtml(report.auditBy || record.executeBy)}</p>
        <p><strong>记录状态：</strong>${escapeHtml(item.execStatusText)}</p>
        <p><strong>报告编号：</strong>${escapeHtml(report.reportNo)}</p>
      </div>
      ${block('检查所见', report.reportContent || record.resultDescription)}
      ${block('影像诊断/印象', report.conclusion || record.resultConclusion, 'bg-emerald-50')}
      ${block('建议', report.suggestions, 'bg-blue-50')}
      ${examImagesHtml(data.images, '检查')}
      ${!report.reportNo && !record.resultConclusion
      ? '<div class="border-t pt-3 text-xs text-slate-400">该检查尚无报告内容（当前状态：'
      + escapeHtml(item.execStatusText) + '）</div>' : ''}
    </div>`, '检查报告详情', {
    dangerouslyUseHTMLString: true, confirmButtonText: '关闭',
    customStyle: {'max-width': '70%', 'width': '70%'}
  });
};
const handleViewLaboratoryReport = async (item) => {
  // 加载检验结果明细
  // 优先用后端给的 execRecordId（批次E/E5：申请单 VO 已直接带执行记录 ID），
  // 拿不到才退回"按 applyId 在记录列表里翻"的老办法（多一次全量查询，且依赖 applyId 能对上）。
  let recordId = item?.execRecordId;
  try {
    if (!recordId) {
      const recordRes = await getLaboratoryRecordList({
        patientId: currentPatient.value?.patientId
      });
      const records = recordRes.data?.records || [];
      const found = records.find((r) => r.applyId == item.id);
      if (!found) {
        ElMessage.warning('未找到对应的检验记录');
        return;
      }
      recordId = found.id;
    }
    const detailRes = await getLaboratoryDetail(recordId);
    const detailRecord = detailRes.data?.record || {};
    const results = detailRes.data?.results || [];
    // 统计异常项目：只统计已判定为异常的，未判定项不能算进正常也不能算进异常
    const abnormalCount = results.filter((r) => r.abnormalFlag && r.abnormalFlag !== 0).length;
    const unjudgedCount = results.filter((r) => String(r.judgeNote || '').startsWith('未判定：')).length;
    // 构建结果明细表格
    let resultsHtml = '';
    if (results.length > 0) {
      resultsHtml = `
        <div class="border-t pt-3">
          <div class="flex items-center justify-between mb-2">
            <p class="font-bold text-slate-700">检验结果明细（共 ${results.length} 项，异常 ${abnormalCount} 项${unjudgedCount > 0 ? `，未判定 ${unjudgedCount} 项` : ''}）</p>
          </div>
          <table class="w-full text-sm border-collapse">
            <thead>
              <tr class="bg-slate-100">
                <th class="border border-slate-300 px-3 py-2 text-left">项目名称</th>
                <th class="border border-slate-300 px-3 py-2 text-left">结果</th>
                <th class="border border-slate-300 px-3 py-2 text-left">单位</th>
                <th class="border border-slate-300 px-3 py-2 text-left">参考范围</th>
                <th class="border border-slate-300 px-3 py-2 text-left">状态</th>
              </tr>
            </thead>
            <tbody>
              ${results.map((r) => {
        // 一律用后端算好的 abnormalFlagText。
        // 不要用 abnormalFlag 写三目判断：未判定时它也是 0，与「正常」同值，
        // 非 1/2/3 就显示「正常」会把「不知道」当成「正常」给医生看。
        const flagText = r.abnormalFlagText || '—';
        const isAbnormal = flagText === '偏高' || flagText === '偏低' || flagText === '异常';
        const isUnjudged = flagText === '未判定';
        const tagClass = isAbnormal
            ? 'bg-red-100 text-red-600'
            : isUnjudged
                ? 'bg-amber-100 text-amber-700'
                : 'bg-green-100 text-green-600';
        return `
                <tr class="${isAbnormal ? 'bg-red-50' : isUnjudged ? 'bg-amber-50' : ''}">
                  <td class="border border-slate-300 px-3 py-2">${r.laboratoryItemName}</td>
                  <td class="border border-slate-300 px-3 py-2 ${isAbnormal ? 'text-red-600 font-bold' : ''}">${r.resultValue || '-'}</td>
                  <td class="border border-slate-300 px-3 py-2">${r.resultUnit || '-'}</td>
                  <td class="border border-slate-300 px-3 py-2">${r.referenceRange || '-'}</td>
                  <td class="border border-slate-300 px-3 py-2">
                    <span class="inline-block px-2 py-0.5 rounded text-xs font-medium ${tagClass}">${flagText}</span>
                    ${isUnjudged && r.judgeNote ? `<div class="mt-0.5 text-[11px] text-slate-500">${r.judgeNote}</div>` : ''}
                  </td>
                </tr>
              `;
      }).join('')}
            </tbody>
          </table>
        </div>
      `;
    }
    // 检验结论/诊断
    const diagnosisHtml = detailRecord.diagnosis ? `
      <div class="border-t pt-3">
        <p class="font-bold text-slate-700 mb-2">检验结论：</p>
        <div class="rounded-lg bg-blue-50 p-3 text-sm text-slate-700">${detailRecord.diagnosis}</div>
      </div>
    ` : '';
    // 建议
    const suggestionsHtml = detailRecord.suggestions ? `
      <div class="border-t pt-3">
        <p class="font-bold text-slate-700 mb-2">建议：</p>
        <div class="rounded-lg bg-amber-50 p-3 text-sm text-slate-700">${detailRecord.suggestions}</div>
      </div>
    ` : '';
    ElMessageBox.alert(`<div class="space-y-3">
        <div class="grid grid-cols-2 gap-2 text-sm">
          <p><strong>检验项目：</strong>${detailRecord.laboratoryItemName || item.laboratoryItemName}</p>
          <p><strong>标本类型：</strong>${detailRecord.specimenType || item.specimenType || '-'}</p>
          <p><strong>检验目的：</strong>${detailRecord.laboratoryPurpose || item.laboratoryPurpose || '-'}</p>
          <p><strong>临床诊断：</strong>${detailRecord.clinicalDiagnosis || item.clinicalDiagnosis || '-'}</p>
        </div>
        ${resultsHtml}
        ${diagnosisHtml}
        ${suggestionsHtml}
        ${examImagesHtml(detailRes.data?.images, '检验')}
      </div>`, '检验报告详情', {
      dangerouslyUseHTMLString: true, confirmButtonText: '关闭', customStyle: {
        'max-width': '40%'
      }
    });
  } catch (error)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */ {

  }
};
// ========== 结诊文书（引导单 / 处方签 PDF） ==========
const guidePdfUrl = ref('');
const rxPdfUrl = ref('');
const guideDocTab = ref('guide');
const loadingGuide = ref(false);
const loadGuideContent = async () => {
  // 直接使用后端地址访问 PDF（uploads/guide/...、uploads/rx/...）
  guidePdfUrl.value = recordForm.guidePdfPath ? `/api/${recordForm.guidePdfPath}` : '';
  rxPdfUrl.value = recordForm.rxPdfPath ? `/api/${recordForm.rxPdfPath}` : '';
};
const printGuidePdf = () => {
  const url = guideDocTab.value === 'rx' ? rxPdfUrl.value : guidePdfUrl.value;
  if (!url)
    return;
  const printWindow = window.open(url, '_blank');
  if (printWindow) {
    printWindow.onload = () => {
      printWindow.print();
    };
  }
};
watch(() => showGuideSheetDialog.value, (val) => {
  if (val)
    loadGuideContent();
});
// ========== 结诊成功 → 异步预生成就诊文书并自动弹出 ==========
// 结诊接口返回后在前端 fire-and-forget 调 printVisitDocs 预生成 PDF（不阻塞结诊流程，
// 生成失败静默，医生仍可手动点「打印就诊文书」兜底）；生成完自动弹「结诊文书」弹窗。
// 防串台：异步返回时若已切换到下一位患者（recordForm.id 变化），丢弃本次结果。
const prepGuideDocs = async () => {
  const targetRecordId = recordForm.id;
  if (!targetRecordId)
    return;
  try {
    const res = await printVisitDocs(targetRecordId);
    if (recordForm.id !== targetRecordId)
      return; // 医生已切到下一位患者，不污染当前表单
    const data = res.data || {};
    if (data.guidePdfPath)
      recordForm.guidePdfPath = data.guidePdfPath;
    // 处方签 PDF 可能有两份（普通 + 饮片），弹窗单 iframe 优先展示饮片方（更特殊），
    // 全部文书仍可走底部「打印就诊文书」拿到
    if (data.rxHerbalPdfPath || data.rxNormalPdfPath)
      recordForm.rxPdfPath = data.rxHerbalPdfPath || data.rxNormalPdfPath;
    if (recordForm.id !== targetRecordId)
      return;
    if (recordForm.guidePdfPath || recordForm.rxPdfPath) {
      guideDocTab.value = 'guide';
      showGuideSheetDialog.value = true;
    }
  } catch (error) {
    console.error('预生成就诊文书失败（医生仍可手动打印）', error);
  }
};
// ========== 打印就诊文书（引导单 + 处方签，点按钮才现场生成 PDF） ==========
const printingDocs = ref(false);
const submitting = ref(false);
const handlePrintVisitDocs = async () => {
  if (!recordForm.id) {
    ElMessage.warning('请先保存病历');
    return;
  }
  printingDocs.value = true;
  const w = window.open('', '_blank');
  try {
    const res = await printVisitDocs(recordForm.id);
    const data = res.data || {};
    const items = [
      {path: data.guidePdfPath, label: '就诊引导单（含缴费二维码）'},
      {path: data.rxNormalPdfPath, label: '普通处方签（西药+中成药）'},
      {path: data.rxHerbalPdfPath, label: '中药饮片处方签'},
    ].filter(x => x.path);
    if (!items.length) {
      if (w) w.close();
      ElMessage.info('本次就诊没有需要打印的文书');
      return;
    }
    if (w) {
      w.document.write(`<html><head><title>就诊文书</title></head><body style="font-family:sans-serif;padding:20px;">
        <h2>就诊文书</h2>
        ${items.map(x => `<p style="margin:12px 0;"><a href="/api/${x.path}" target="_blank" style="font-size:16px;">${x.label}</a></p>`).join('')}
      </body></html>`);
      w.document.close();
    }
  } catch (e) {
    if (w) w.close();
    ElMessage.error('就诊文书生成失败：' + (e?.message || e));
  } finally {
    printingDocs.value = false;
  }
};

// ========== 打印处方（旧浏览器兜底，后端 PDF 优先） ==========
const printPrescriptions = () => {
  // 结诊后已有后端处方签 PDF（含医师手写笔迹 + 左上角聚合支付码）：直接打开，可打印/下载
  if (rxPdfUrl.value) {
    const pdfWindow = window.open(rxPdfUrl.value, '_blank');
    if (pdfWindow) {
      pdfWindow.onload = () => pdfWindow.print();
    }
    return;
  }
  const patient = currentPatient.value;
  if (!patient)
    return;
  let html = `
    <html><head><title>处方笺</title>
    <style>
      body { font-family: SimSun, serif; padding: 40px; font-size: 14px; }
      .header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 10px; margin-bottom: 20px; }
      .header h1 { font-size: 24px; margin: 0; }
      .info { display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 13px; }
      .rx-title { font-size: 16px; font-weight: bold; border-bottom: 1px solid #000; padding-bottom: 5px; margin: 20px 0 10px; }
      table { width: 100%; border-collapse: collapse; margin-bottom: 15px; }
      th, td { border: 1px solid #000; padding: 6px 10px; text-align: left; font-size: 13px; }
      th { background: #f5f5f5; }
      .footer { margin-top: 30px; display: flex; justify-content: space-between; font-size: 13px; align-items: flex-end; }
      .sign-img img { height: 56px; max-width: 180px; object-fit: contain; }
      @media print { body { padding: 20px; } }
    </style></head><body>
    <div class="header"><h1>处 方 笺</h1></div>
    <div class="info">
      <span>患者：${patient.patientName}</span>
      <span>性别：${patientGenderText(patient.gender)}</span>
      <span>年龄：${patient.age}岁</span>
      <span>科室：${patient.deptName}</span>
      <span>医生：${patient.doctorName}</span>
    </div>
  `;
  const hasAny = prescriptionList.value.some(p => p.details && p.details.length > 0);
  // 落款取第一张已签处方：医师名/签名时刻来自真实签名锚点，不再硬编码「已签名」
  const signedRx = prescriptionList.value.find(p => p.doctorSignId && p.details && p.details.length > 0);
  if (!hasAny) {
    html += '<div style="text-align:center;color:#999;padding:40px;">无处方</div>';
  } else {
    // 普通处方（西药+中成药）一页，中药饮片另起一页（《处方管理办法》要求饮片单独开具）
    const normalRxs = prescriptionList.value.filter(p => p.details?.length && p.prescriptionType !== 3);
    const herbalRxs = prescriptionList.value.filter(p => p.details?.length && p.prescriptionType === 3);
    normalRxs.forEach(p => {
      html += `<div class="rx-title">${prescriptionTypeLabel(p.prescriptionType)}处方</div>`;
      html += `<table><thead><tr><th>药品名称</th><th>规格</th><th>数量</th><th>用法</th><th>频次</th><th>天数</th></tr></thead><tbody>`;
      p.details.forEach((d) => {
        html += `<tr>
          <td>${d.drugName || '-'}</td>
          <td>${d.specification || '-'}</td>
          <td>${d.quantity || '-'} ${d.unit || ''}</td>
          <td>${d.route || '-'}</td>
          <td>${d.frequency || '-'}</td>
          <td>${d.duration || '-'}</td>
        </tr>`;
      });
      html += '</tbody></table>';
    });
    if (herbalRxs.length) {
      html += `<div style="page-break-before:always;"></div>
        <div class="header"><h1>中药处方笺</h1></div>
        <div class="info">
          <span>患者：${patient.patientName}</span>
          <span>性别：${patientGenderText(patient.gender)}</span>
          <span>年龄：${patient.age}岁</span>
          <span>科室：${patient.deptName}</span>
        </div>`;
      herbalRxs.forEach(p => {
        html += `<div class="rx-title">中药饮片处方</div>`;
        html += `<table><thead><tr><th>药品名称</th><th>规格</th><th>数量</th><th>用法</th><th>频次</th><th>天数</th></tr></thead><tbody>`;
        p.details.forEach((d) => {
          html += `<tr>
            <td>${d.drugName || '-'}</td>
            <td>${d.specification || '-'}</td>
            <td>${d.quantity || '-'} ${d.unit || ''}</td>
            <td>${d.route || '-'}</td>
            <td>${d.frequency || '-'}</td>
            <td>${d.duration || '-'}</td>
          </tr>`;
        });
        html += '</tbody></table>';
      });
    }
  }
  const signLine = signedRx
      ? `医师签名：${signedRx.doctorName || patient.doctorName || ''}（电子签名${signedRx.doctorSignedTime ? ' · ' + String(signedRx.doctorSignedTime).slice(0, 16).replace('T', ' ') : ''}，可验签）`
      : '医师签名：（尚未电子签名，结诊提交后生效）';
  const signDate = signedRx && signedRx.doctorSignedTime
      ? String(signedRx.doctorSignedTime).slice(0, 10)
      : new Date().toLocaleDateString('zh-CN');
  const handwritten = (signedRx && signedRx.doctorSignature) || rxSignature.value || '';
  const signImage = handwritten
      ? `<div class="sign-img"><img src="${handwritten}" alt="医师手写签名"/></div>`
      : '';
  html += `
    <div class="footer">
      <div>
        ${signImage}
        <div>${signLine}</div>
      </div>
      <span>日期：${signDate}</span>
    </div>
    </body></html>`;
  const printWindow = window.open('', '_blank');
  if (printWindow) {
    printWindow.document.write(html);
    printWindow.document.close();
    printWindow.onload = () => printWindow.print();
  }
};
// 加载全部药品列表
const loadAllDrugs = async () => {
  drugLoading.value = true;
  try {
    // 一次拉全部类型（不传 drugType = 全量字典），按类型分组：
    // 原来 3 次全量请求（西药/中成药/饮片各一次），接口慢时下拉要好几次才出得来
    const res = await getDrugSelectList({});
    const all = res.data || [];
    drugResultsByType[1].value = all.filter(d => d.drugType === 1);
    drugResultsByType[2].value = all.filter(d => d.drugType === 2);
    drugResultsByType[3].value = all.filter(d => d.drugType === 3);
  } catch (error) {
    console.error('加载药品列表失败:', error);
  } finally {
    drugLoading.value = false;
  }
};
// 加载全部检查项目
const loadAllInspectionItems = async () => {
  inspectionItemLoading.value = true;
  try {
    const res = await getInspectionSelectList();
    inspectionItemResults.value = res.data || [];
  } catch (error) {
    console.error('加载检查项目失败:', error);
  } finally {
    inspectionItemLoading.value = false;
  }
};
// 加载全部检验项目
const loadAllLaboratoryItems = async () => {
  laboratoryItemLoading.value = true;
  try {
    const res = await getLaboratorySelectList();
    laboratoryItemResults.value = res.data || [];
  } catch (error) {
    console.error('加载检验项目失败:', error);
  } finally {
    laboratoryItemLoading.value = false;
  }
};
// ------------------------------------------------------------------
// 开住院证（门诊 → 住院的入口）
//
// 表单、拟诊预填与提交都在 DoctorWorkstationAdmissionAdd.vue 里，这里只留打开入口。
// ------------------------------------------------------------------
const orderDialogVisible = ref(false);
// 我开的住院证（列表 + 自助作废在 DoctorWorkstationAdmissionList.vue 里）
const myAdmissionListVisible = ref(false);
const openOrderDialog = () => {
  if (!currentPatient.value)
    return ElMessage.warning('请先呼叫患者');
  orderDialogVisible.value = true;
};
onMounted(() => {
  // 统一「带着目标患者进入」的入口。此前分成两条路（store 有患者→consumeSwitch()，
  // URL 有 patientId→直接挂起），但两者语义完全相同——这次进来是为了接某个人——
  // 分路的代价是以后每加一个入口都要重新推一遍时序。
  //   ① 顶部搜索跳转：store 在组件挂载前已 setPatient（watch 的注册晚于这次写入，抓不到）
  //   ② 刷新 / 直达 URL：store 从 sessionStorage 恢复，或只剩 URL 上的 patientId
  const restored = currentPatientStore.patient;
  const targetId = restored?.id || route.query.patientId;
  if (targetId) {
    pendingSwitch = {id: String(targetId), patientName: restored?.patientName || ''};
    // 标记「为某个特定患者而来」：首屏不得自动选中队列里正在就诊的别人
    enteredWithTarget = true;
    // 记下这次切换已被本函数消费，避免 watch 之后把同一次再消费一遍
    handledSwitchAt = currentPatientStore.switchedAt;
    // 会话里没有当前患者、只有 URL 上的 id → 手输 / 外链进入，算一次显式意图（该有反馈）；
    // 若 store 里有患者，那是切菜单/刷新重建的恢复，不重复提示（见 applyPendingSwitch）
    if (!restored && route.query.patientId) {
      urlIntent = true;
    }
  }
  loadUserInfo();
  loadTcmDicts();
  loadDoctorStatus();
  loadData();
  loadTodoList();
  loadDiagTemplates();
  loadRxTemplates();
  loadDrugPackages();
  loadInspectionTemplates();
  loadLaboratoryTemplates();
  loadAllDrugs();
  loadAllInspectionItems();
  loadAllLaboratoryItems();
  refreshTimer = setInterval(loadData, 30000);
});
onUnmounted(() => {
  if (refreshTimer)
    clearInterval(refreshTimer);
});
// ========== 批次B 三栏同屏：右栏医嘱面板折叠 / 费用抽屉 ==========
const recordCollapsed = ref(false);
const showChargeDrawer = ref(false);
const COMPACT_BREAKPOINT = 1640;
const compactViewport = ref(false);
const syncCompactViewport = () => {
  compactViewport.value = window.innerWidth <= COMPACT_BREAKPOINT;
  // 从窄屏（可能正折着）拖宽到宽屏：病历列本就该常驻，顺手复位。
  if (!compactViewport.value)
    recordCollapsed.value = false;
};
syncCompactViewport();
window.addEventListener('resize', syncCompactViewport);
onUnmounted(() => window.removeEventListener('resize', syncCompactViewport));
// 真正的折叠态 = 窄屏 + 用户点了收起；宽屏下恒为展开
const recordColCollapsed = computed(() => compactViewport.value && recordCollapsed.value);
// 病历已提交（终态）：整个病历/诊断/处方/申请单只读，要改走修改申请流程
const isRecordSubmitted = computed(() => Number(recordForm.recordStatus) === 2);
// 历史就诊：患者级跨就诊次，用弹窗打开（不打断接诊），内嵌 CDR 全景时间轴
const showPatientDetail = ref(false);
// 患者条上的到达时间（队列里有 arriveTime 才有值）
const arriveText = computed(() => {
  const t = currentPatient.value?.arriveTime;
  return t ? formatArriveTime(t) + ' 到达' : '';
});
</script>

<style scoped>
/* 诊室叫号面板：白底卡片（与页面其余卡片同底），号码用主色做视觉主体 */
.call-board {
  background: #ffffff;
  border: 1px solid #e2e8f0;
}

.call-no {
  font-size: 38px;
  font-weight: 800;
  letter-spacing: 0.5px;
  font-variant-numeric: tabular-nums;
}

/* 诊室灯：接诊中绿 / 暂离琥珀 / 空闲灰，带辉光模拟实体指示灯 */
.call-light {
  width: 9px;
  height: 9px;
  border-radius: 9999px;
  display: inline-block;
  flex-shrink: 0;
}

.call-light.is-busy {
  background: #34d399;
  box-shadow: 0 0 6px rgba(52, 211, 153, 0.9);
}

.call-light.is-away {
  background: #fbbf24;
  box-shadow: 0 0 6px rgba(251, 191, 36, 0.9);
}

.call-light.is-idle {
  background: #94a3b8;
}

/* 病历表单整体放大：医生盯一天病历，14px 输入太小（2026-09-25 用户口径「字可以大一点」） */
.record-form :deep(.el-textarea__inner),
.record-form :deep(.el-input__inner) {
  font-size: 15px;
  line-height: 1.7;
}

.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}

.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 2px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
