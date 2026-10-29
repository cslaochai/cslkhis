<template>
  <div class="flex h-[var(--his-page-h)] gap-3">
    <!-- ========== 左栏：急诊叫号板 + 候诊队列（卡片化，与门诊医生站同款骨架 + 共享令牌） ========== -->
    <div class="flex w-[var(--his-queue-col-w)] shrink-0 flex-col gap-3">
      <!-- 急诊叫号板：大号码 / 分区色 / MEWS 角标 / 下一位 -->
      <div class="shrink-0 rounded-lg border border-slate-200 bg-white p-3.5 shadow-sm" data-testid="emg-doc-callboard">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-1.5">
            <span :class="current ? 'is-busy' : 'is-idle'" class="call-light"></span>
            <span class="text-xs font-bold tracking-wide text-slate-700">{{ current ? '接诊中' : '空闲' }}</span>
          </div>
          <span class="rounded bg-slate-100 px-1.5 py-0.5 text-xs font-medium text-slate-600">危重 {{
              criticalCount
            }} 人</span>
        </div>

        <!-- 正在接诊：大号急诊号尾段 + 姓名（窄列 truncate，长名不挤竖排） -->
        <div v-if="current" class="mt-2">
          <div class="flex items-end gap-2">
            <div class="call-no leading-none" :class="zoneTone(current)">{{ shortNo(current.emergencyNo) }}</div>
            <div class="min-w-0 flex-1 pb-1">
              <div class="truncate text-lg font-bold leading-tight text-slate-900">{{ current.patientName }}</div>
              <div class="mt-0.5 flex items-center gap-1 text-xs text-slate-500">
                <span>{{ genderText(current.gender) }} {{ current.age ?? '—' }}岁</span>
                <span v-if="current.mewsScore != null && current.mewsScore >= 5"
                      class="rounded bg-red-500 px-1 text-[10px] font-bold text-white">MEWS {{
                    current.mewsScore
                  }}</span>
              </div>
            </div>
          </div>
          <div class="mt-1.5 text-xs text-slate-500">{{ levelOf(current).label }} · 停留 {{ stayText }}</div>
          <div class="mt-0.5 truncate font-mono text-xs text-slate-400" :title="current.emergencyNo">
            {{ current.emergencyNo }}
          </div>
        </div>
        <div v-else class="mt-2 flex h-[74px] items-center">
          <span class="text-sm leading-6 text-slate-400">尚未接诊，点「接诊下一位」或从队列选择患者</span>
        </div>

        <!-- 下一位预览：我名下候诊（status=1）队首，后端已按级别/时间排序 -->
        <div class="mt-2 flex items-center gap-2 rounded-md bg-slate-50 px-2 py-1.5 text-xs">
          <span class="shrink-0 font-medium text-slate-500">下一位</span>
          <template v-if="nextWaiting">
            <span class="min-w-0 truncate font-bold text-slate-900">{{ nextWaiting.patientName }}</span>
            <span class="shrink-0 rounded px-1 text-[10px] font-bold text-white"
                  :class="zoneBgTone(nextWaiting)">{{ levelOf(nextWaiting).zone }}</span>
            <span v-if="nextWaiting.mewsScore != null"
                  class="ml-auto shrink-0 text-slate-400">MEWS {{ nextWaiting.mewsScore }}</span>
          </template>
          <span v-else class="text-slate-400">候诊队列已空</span>
        </div>

        <div class="mt-2.5 flex gap-2">
          <el-button v-perm="'opd:emergencyDoctor:edit'" :disabled="!nextWaiting" :icon="VideoPlay"
                     class="!flex-1" type="primary" data-testid="emg-doc-btn-accept-next" @click="acceptNext">接诊下一位
          </el-button>
          <!-- 重呼：当前患者已接诊（队列行就诊中）但人没到，再播一遍叫号 -->
          <el-button v-if="current && current.emergencyStatus === 2" v-perm="'opd:emergencyDoctor:edit'"
                     :icon="Bell" data-testid="emg-doc-btn-recall" @click="handleRecall(current)">重呼
          </el-button>
        </div>
      </div>

      <!-- 候诊队列卡 -->
      <div class="flex min-h-0 flex-1 flex-col rounded-lg border border-slate-200 bg-white shadow-sm"
           data-testid="emg-doc-queue">
        <div class="flex shrink-0 items-center gap-1 border-b border-slate-200 px-2 py-2">
          <el-radio-group v-model="queueTab" size="small" @change="reloadQueue">
            <el-radio-button value="mine">我的患者</el-radio-button>
            <el-radio-button value="pool">待派单池</el-radio-button>
          </el-radio-group>
          <span class="ml-auto">
            <el-button :icon="Refresh" link size="small" data-testid="emg-doc-refresh"
                       @click="reloadQueue">刷新</el-button>
          </span>
        </div>
        <div class="flex shrink-0 flex-wrap items-center gap-x-2 px-3 py-1.5 text-xs text-slate-500">
          <span data-testid="emg-doc-count-mine">我的患者 <b class="text-slate-800">{{ mineCount }}</b> 人</span>
          <span data-testid="emg-doc-count-critical">危重（I/II级）<b class="text-red-600">{{ criticalCount }}</b></span>
          <span data-testid="emg-doc-count-pool">待派单池 <b class="text-slate-800">{{ poolCount }}</b> 人</span>
        </div>
        <!-- 分区 / 状态筛选（纯前端，零后端改动） -->
        <div class="shrink-0 border-t border-slate-100 px-3 py-1.5">
          <div class="flex items-center gap-1">
            <span class="text-[11px] text-slate-400">区域</span>
            <el-radio-group v-model="zoneFilter" size="small">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button value="red">红区</el-radio-button>
              <el-radio-button value="yellow">黄区</el-radio-button>
              <el-radio-button value="green">绿区</el-radio-button>
            </el-radio-group>
          </div>
          <div class="mt-1 flex items-center gap-1">
            <span class="text-[11px] text-slate-400">状态</span>
            <el-radio-group v-model="statusFilter" size="small">
              <el-radio-button value="all">全部</el-radio-button>
              <el-radio-button :value="1">候诊</el-radio-button>
              <el-radio-button :value="2">诊治中</el-radio-button>
              <el-radio-button :value="3">留观</el-radio-button>
            </el-radio-group>
          </div>
          <!-- 检索（真实产品标配）：姓名 / 患者号 / 急诊号（含「急+尾5位」短号数字），纯前端过滤 -->
          <div class="mt-1.5 flex items-center gap-1">
            <span class="text-[11px] text-slate-400">检索</span>
            <el-input v-model="searchKeyword" size="small" clearable :prefix-icon="Search"
                      placeholder="姓名 / 患者号 / 急诊号" data-testid="emg-doc-search" class="flex-1"/>
          </div>
        </div>
        <div class="min-h-0 flex-1 overflow-y-auto">
          <div v-if="displayQueue.length === 0" class="p-8 text-center text-sm text-slate-400">
            {{ queueTab === 'mine' ? '暂无派给我的患者' : '待派单池是空的' }}
          </div>
          <div
              v-for="row in displayQueue"
              :key="row.id"
              class="cursor-pointer border-b border-slate-100 px-3 py-2.5 hover:bg-blue-50/60"
              :class="current && String(current.id) === String(row.id) ? 'bg-blue-50 border-l-4 border-l-blue-500' : 'border-l-4 border-l-transparent'"
              :data-testid="'emg-doc-card-' + row.id"
              @click="selectPatient(row)"
          >
            <!-- 首行只放 姓名 + 性别年龄 + 分级：姓名 min-w-0 flex-1 truncate，
                 三字名不竖排、新疆长名（买买提·艾力·…）悬停 title 看全名 -->
            <div class="flex items-center gap-1.5">
              <span class="min-w-0 flex-1 truncate text-sm font-bold text-slate-800"
                    :title="row.patientName">{{ row.patientName }}</span>
              <span class="shrink-0 text-xs text-slate-400">{{ genderText(row.gender) }} / {{ row.age ?? '—' }}岁</span>
              <el-tag size="small" :class="levelOf(row).color" class="!border shrink-0">{{
                  levelOf(row).label
                }}
              </el-tag>
            </div>
            <div class="mt-1 flex items-center gap-1.5">
              <el-tag v-if="row.mewsScore != null && row.mewsScore >= 5" size="small" type="danger" effect="dark">
                MEWS {{ row.mewsScore }}
              </el-tag>
              <el-tag v-else-if="row.mewsScore != null" size="small" type="info">MEWS {{ row.mewsScore }}</el-tag>
              <el-tag v-if="row.greenChannel && row.greenChannel !== '无'" size="small" type="success" effect="dark">
                {{ row.greenChannel }}
              </el-tag>
              <!-- 欠费标（真实产品患者行标配）：名下有挂账/欠费账单（bill_status=6）才亮；
                   先救治后付费口径下诊治期间的待缴账单不打扰 -->
              <el-tag v-if="row.hasArrears" size="small" type="danger" effect="dark"
                      data-testid="emg-doc-arrears-tag">欠费
              </el-tag>
              <!-- 有新报告（真实产品「新医嘱提醒」对应物）：检查/检验申请单已出报告即亮，30s 轮询自动刷新 -->
              <el-tag v-if="(row.reportReadyCount ?? 0) > 0" size="small" type="primary" effect="dark"
                      class="animate-pulse" data-testid="emg-doc-newreport-tag">
                报告 {{ row.reportReadyCount }}
              </el-tag>
              <!-- 过号标记只落在队列行（is_overdue），急诊临床态不变：候诊中过号的患者临床上仍在候诊 -->
              <el-tag v-if="row.isOverdue" size="small" type="warning" effect="plain" data-testid="emg-doc-overdue-tag">
                已过号
              </el-tag>
              <el-tag v-if="row.emergencyStatus != null" size="small" :type="statusTagType(row.emergencyStatus)">
                {{ statusText(row.emergencyStatus) }}
              </el-tag>
              <span class="flex-1"></span>
              <span v-if="row.overdueLevel > 0" class="shrink-0 text-xs font-bold text-red-600">超时 {{
                  row.waitMinutes
                }}分</span>
            </div>
            <div class="mt-1 flex items-center gap-2 text-xs text-slate-400">
              <span class="font-mono" :title="row.emergencyNo">{{ shortNo(row.emergencyNo) }}</span>
              <span v-if="row.emergencyStatus === 1 && row.waitMinutes != null">候诊 {{ row.waitMinutes }} 分</span>
              <span v-else-if="row.emergencyStatus === 2">诊治中</span>
              <span v-else-if="row.emergencyStatus === 3">留观中</span>
              <span v-if="row.assignTypeText" class="text-slate-500">{{ row.assignTypeText }}</span>
            </div>
            <div class="mt-0.5 truncate text-xs text-slate-500" :title="row.chiefComplaint">
              {{ row.chiefComplaint || '（未填主诉）' }}
            </div>
            <!-- 行内叫号操作（@click.stop 防止触发整行 selectPatient）：
                 候诊→呼叫/过号；已过号→回队；诊治中→重呼。接口与门诊医生站同一组 -->
            <div v-if="rowActions(row).length" class="mt-1.5 flex items-center justify-end gap-2" @click.stop>
              <el-button v-for="a in rowActions(row)" :key="a.key" v-perm="'opd:emergencyDoctor:edit'"
                         link size="small" :type="a.type"
                         :data-testid="'emg-doc-row-' + a.key" @click="a.run(row)">{{ a.label }}
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ========== 主区：患者条 + 急诊要素 + 操作条 + 中间卡（病历/处方/医嘱）+ 右辅助列 ========== -->
    <div class="flex min-w-0 flex-1 flex-col gap-3">
      <div v-if="!current"
           class="flex flex-1 flex-col items-center justify-center rounded-lg border border-slate-200 bg-white text-slate-400 shadow-sm">
        <el-icon :size="48">
          <FirstAidKit/>
        </el-icon>
        <p class="mt-3 text-sm">请从左侧队列选择患者开始接诊</p>
        <p class="mt-1 text-xs">「我的患者」是派给你 / 你接诊的；「待派单池」里的患者可以抢单接诊</p>
        <el-button class="mt-4" :icon="Refresh" size="small" @click="reloadQueue">刷新队列</el-button>
      </div>

      <template v-else>
        <!-- 患者条 + 急诊要素 -->
        <PatientBriefBar
            :patient="current"
            :detail="patientDetail"
            :record="recordForm"
            :arrive-text="'候诊 ' + (current.waitMinutes ?? 0) + ' 分'"
            :visit-badge="{label: '急诊', tagType: 'warning'}"
            @open-tags="handleOpenTagDialog"
            @open-detail="showPatientDetail = true"
            @open-charge="showInsuranceDialog = true"
        />
        <div
            class="flex shrink-0 flex-wrap items-center gap-1.5 rounded-lg border border-slate-200 bg-white px-4 py-2 text-xs shadow-sm">
          <el-tag size="small" :class="levelOf(current).color" class="!border">{{ levelOf(current).label }} ·
            {{ levelOf(current).zone }}
          </el-tag>
          <el-tag v-if="current.greenChannel && current.greenChannel !== '无'" size="small" type="success">
            {{ current.greenChannel }}
          </el-tag>
          <el-tag v-if="current.hasArrears" size="small" type="danger" effect="dark">欠费</el-tag>
          <el-tag v-if="(current.reportReadyCount ?? 0) > 0" size="small" type="primary" effect="dark"
                  class="animate-pulse">报告 {{ current.reportReadyCount }}
          </el-tag>
          <el-tag size="small" type="info" :title="current.emergencyNo">急诊号 {{
              shortNo(current.emergencyNo)
            }}
          </el-tag>
          <el-tag size="small" :type="statusTagType(current.emergencyStatus)">{{
              statusText(current.emergencyStatus)
            }}
          </el-tag>
          <span v-for="v in vitalChips" :key="v.label" class="rounded bg-slate-100 px-1.5 py-0.5 text-slate-600">
                {{ v.label }} {{ v.value }}
              </span>
          <span class="text-slate-400">意识：{{ current.consciousnessText || '—' }}</span>
          <span class="rounded bg-blue-50 px-1.5 py-0.5 text-blue-600">停留 {{ stayText }}</span>
        </div>

        <!-- 中间卡（病历/处方/医嘱）+ 右辅助列（评估与流程常驻） -->
        <div class="flex min-h-0 flex-1 gap-3">
          <!-- 中间栏：病历卡 + 底部动作条（动作条只占本栏宽度，不压第三栏「评估与流程」） -->
          <div class="flex min-w-0 flex-1 flex-col gap-3">
            <div
                class="flex min-w-0 flex-1 flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
              <el-alert
                  v-if="current.emergencyStatus !== 2"
                  :title="current.emergencyStatus === 1 ? '该患者还未接诊——点上方「接诊」后开始书写'
                      : current.emergencyStatus === 3 ? '该患者留观中——病历与处方只读，可分诊复评、转住院或交班'
                      : '该患者已转归结束，内容只读'"
                  type="info" show-icon :closable="false" class="!rounded-none border-0 border-b"
              />
              <el-tabs v-model="activeTab" class="emg-doc-tabs">
                <!-- ===== 病历 ===== -->
                <el-tab-pane label="病历" name="record">
                  <!-- 两列各 ≈516px，对齐门诊医生站 520px 病历列口径 -->
                  <div class="grid max-w-[1044px] grid-cols-2 gap-3">
                    <el-form label-position="top" :disabled="current.emergencyStatus !== 2">
                      <!-- 快捷录入（2026-10-28）：体征一键正常显式覆盖；阴性史/查体只补空字段不覆盖已写内容 -->
                      <div class="mb-2 flex flex-wrap items-center gap-1.5" data-testid="emg-doc-quickbar">
                        <span class="text-xs font-bold text-slate-500">快捷录入</span>
                        <el-button size="small" type="primary" plain
                                   :disabled="current.emergencyStatus !== 2"
                                   data-testid="emg-doc-btn-normal-vitals"
                                   @click="fillNormalVitals">体征一键正常
                        </el-button>
                        <el-button size="small" plain :disabled="current.emergencyStatus !== 2"
                                   data-testid="emg-doc-btn-negative-history"
                                   @click="fillNegativeHistory">阴性病史
                        </el-button>
                        <el-button size="small" plain :disabled="current.emergencyStatus !== 2"
                                   data-testid="emg-doc-btn-normal-exam"
                                   @click="fillNormalExam">查体无特殊
                        </el-button>
                      </div>
                      <el-form-item label="主诉" required>
                        <el-input v-model="recordForm.chiefComplaint" type="textarea" :rows="3"
                                  data-testid="emg-doc-chief"/>
                      </el-form-item>
                      <el-form-item label="现病史" required>
                        <el-input v-model="recordForm.presentIllness" type="textarea" :rows="3"
                                  data-testid="emg-doc-present"/>
                      </el-form-item>
                      <el-form-item label="过敏史" required>
                        <el-input v-model="recordForm.allergyHistory" type="textarea" :rows="3"
                                  data-testid="emg-doc-allergy"/>
                      </el-form-item>
                      <el-form-item label="既往史" required>
                        <el-input v-model="recordForm.pastHistory" type="textarea" :rows="3"/>
                      </el-form-item>
                      <el-form-item label="个人史" required>
                        <el-input v-model="recordForm.personalHistory" type="textarea" :rows="3"/>
                      </el-form-item>
                      <el-form-item label="家族史" required>
                        <el-input v-model="recordForm.familyHistory" type="textarea" :rows="3"/>
                      </el-form-item>
                    </el-form>
                    <el-form label-position="top" :disabled="current.emergencyStatus !== 2">
                      <div class="mb-1 text-xs font-bold text-slate-500">生命体征</div>
                      <div class="grid grid-cols-3 gap-2">
                        <el-form-item label="体温℃" required>
                          <el-input v-model="recordForm.temperature" data-testid="emg-doc-temperature"/>
                        </el-form-item>
                        <el-form-item label="脉搏" required>
                          <el-input v-model="recordForm.pulse" data-testid="emg-doc-pulse"/>
                        </el-form-item>
                        <el-form-item label="呼吸" required>
                          <el-input v-model="recordForm.respiration" data-testid="emg-doc-respiration"/>
                        </el-form-item>
                        <el-form-item label="收缩压" required>
                          <el-input v-model="recordForm.systolicPressure" data-testid="emg-doc-sbp"/>
                        </el-form-item>
                        <el-form-item label="舒张压" required>
                          <el-input v-model="recordForm.diastolicPressure" data-testid="emg-doc-dbp"/>
                        </el-form-item>
                      </div>
                      <el-form-item label="体格检查" class="mt-2">
                        <el-input v-model="recordForm.generalCondition" type="textarea" :rows="3"
                                  placeholder="查体所见；专科评分可从「评估与流程」一键写入"
                                  data-testid="emg-doc-physicalexam"/>
                      </el-form-item>
                      <div class="mb-1 mt-2 text-xs font-bold text-slate-500">诊断（ICD-10）</div>
                      <el-form-item label="ICD-10 检索">
                        <el-select v-model="icdSelected" filterable remote reserve-keyword clearable
                                   :remote-method="searchIcd10Remote" :loading="icdLoading" size="small"
                                   placeholder="输入诊断名称 / 编码搜索" @change="onIcdSelected"
                                   data-testid="emg-doc-diag-icd" style="width:100%">
                          <el-option v-for="o in icdOptions" :key="o.icdCode" :value="o.icdCode"
                                     :label="(o.icdCode || '') + ' ' + (o.icdName || '')"/>
                        </el-select>
                      </el-form-item>
                      <div v-if="diagTplList.length" class="mb-2 flex flex-wrap items-center gap-1"
                           data-testid="emg-doc-diag-tpl">
                        <span class="text-[11px] text-slate-400">常用诊断：</span>
                        <el-tag v-for="t in diagTplList" :key="t.id" size="small" class="cursor-pointer"
                                type="primary" effect="plain" @click="applyDiagTpl(t)">
                          {{ (t.icdCode ? t.icdCode + ' ' : '') + (t.icdName || '') }}
                        </el-tag>
                      </div>
                      <el-form-item label="诊断名称" required>
                        <el-input v-model="recordForm.diagnosisName" placeholder="ICD-10 诊断名称（可点上方检索选码）"
                                  data-testid="emg-doc-diag-name"/>
                      </el-form-item>
                      <el-form-item label="诊断编码">
                        <el-input v-model="recordForm.diagnosisCode" placeholder="ICD-10 编码（选码自动回填）"
                                  data-testid="emg-doc-diag-code"/>
                      </el-form-item>
                      <el-form-item label="处置计划" required>
                        <el-input v-model="recordForm.treatmentPlan" type="textarea" :rows="3"/>
                      </el-form-item>
                    </el-form>
                  </div>
                  <!-- 处方摘要条：病历页签常驻底部（sticky），已开处方时显示、点击直跳处方页签——
                       对齐主流「少切换」口径（2026-10-28）；无处方不显示，不打扰纯病历书写 -->
                  <button v-if="current.emergencyStatus === 2 && rxDetailCount"
                          class="sticky bottom-0 z-10 mt-2 flex w-full items-center justify-center gap-1 rounded-md border border-sky-200 bg-sky-50/95 py-2 text-sm font-medium text-sky-700 shadow-sm backdrop-blur transition-colors hover:bg-sky-100"
                          data-testid="emg-doc-rx-summary"
                          type="button"
                          @click="activeTab = 'rx'">
                    处方已有 {{ rxDetailCount }} 条明细，去开方 →
                  </button>
                </el-tab-pane>

                <!-- ===== 处方 ===== -->
                <el-tab-pane :label="'处方' + (rxDetailCount ? '（' + rxDetailCount + '）' : '')" name="rx">
                  <div v-if="current.emergencyStatus !== 2" class="py-6 text-center text-sm text-slate-400">
                    接诊后可开处方
                  </div>
                  <template v-else>
                    <div class="mb-3 flex items-center gap-2">
                      <el-radio-group v-model="currentRxType" size="small" @change="switchRxTab">
                        <el-radio-button :value="1">西药</el-radio-button>
                        <el-radio-button :value="2">中成药</el-radio-button>
                        <el-radio-button :value="3">中药饮片</el-radio-button>
                      </el-radio-group>
                      <span class="flex-1"></span>
                      <template v-if="currentRxType === 3">
                        <span class="text-xs text-slate-500">剂数</span>
                        <el-input-number v-model="rxDraft.doseCount" :min="1" :max="30" size="small" class="!w-24"/>
                        <span class="text-xs text-slate-500">煎服</span>
                        <el-select v-model="rxDraft.decoctFlag" size="small" class="!w-28" placeholder="选择">
                          <el-option :value="1" label="代煎"/>
                          <el-option :value="2" label="自煎"/>
                        </el-select>
                      </template>
                      <!-- 慢病长处方（处方级）：仅西药/中成药；患者须有已认定慢病档案（服务端强校验，此处只做 disabled 预检） -->
                      <template v-if="currentRxType !== 3">
                        <span class="text-xs text-slate-500" data-testid="emg-doc-longrx-label">慢病长处方</span>
                        <el-switch v-model="rxDraft.isLongPrescription" :disabled="!chronicQualified"
                                   data-testid="emg-doc-longrx-switch" size="small"/>
                        <template v-if="rxDraft.isLongPrescription">
                          <span class="text-xs text-slate-500">天数</span>
                          <el-input-number v-model="rxDraft.longPrescriptionDays" :min="1" :max="90" size="small"
                                           class="!w-24" data-testid="emg-doc-longrx-days"/>
                          <span class="text-[10px] text-slate-400">1~90 天</span>
                        </template>
                        <el-tooltip v-else-if="!chronicQualified" effect="dark"
                                    content="该患者无已认定的慢病档案，不能开慢病长处方">
                        <span class="text-[10px] text-amber-600"
                              data-testid="emg-doc-longrx-nochronic">无慢病档案</span>
                        </el-tooltip>
                        <el-tooltip v-else effect="dark" :content="'已认定：' + chronicDiseaseText">
                          <span class="text-[10px] text-slate-400">{{ chronicDiseaseText }}</span>
                        </el-tooltip>
                      </template>
                      <el-button size="small" type="warning" plain v-perm="'opd:emergencyDoctor:edit'"
                                 data-testid="emg-doc-rx-pkg" @click="openPkgDialog">医嘱套
                      </el-button>
                      <el-button :icon="Plus" size="small" data-testid="emg-doc-rx-add-row" @click="addRxRow">加一行
                      </el-button>
                    </div>

                    <el-table :data="rxDraft.details" size="small" empty-text="暂无明细，点「加一行」开始开方"
                              data-testid="emg-doc-rx-table">
                      <el-table-column label="药品" min-width="180">
                        <template #default="{row}">
                          <el-select
                              v-model="row.drugId" filterable remote reserve-keyword
                              :remote-method="searchDrug" :loading="drugLoading" size="small"
                              placeholder="输入药品名搜索" @change="onDrugPicked(row)"
                          >
                            <el-option v-for="d in drugResults" :key="d.id" :value="d.drugId"
                                       :label="d.drugName + (d.specification ? '（' + d.specification + '）' : '')"/>
                          </el-select>
                        </template>
                      </el-table-column>
                      <el-table-column label="规格" min-width="110">
                        <template #default="{row}"><span class="text-xs text-slate-500">{{
                            row.specification || '—'
                          }}</span></template>
                      </el-table-column>
                      <el-table-column :label="currentRxType === 3 ? '每剂克数' : '单次用量'" width="120">
                        <template #default="{row}">
                          <el-input v-model="row.singleDosage" size="small"/>
                        </template>
                      </el-table-column>
                      <el-table-column label="频次" width="120">
                        <template #default="{row}">
                          <el-select v-model="row.frequency" size="small">
                            <el-option v-if="currentRxType === 3" value="每日一剂" label="每日一剂"/>
                            <el-option v-if="currentRxType === 3" value="每日两剂" label="每日两剂"/>
                            <el-option v-if="currentRxType !== 3" value="一日一次" label="一日一次"/>
                            <el-option v-if="currentRxType !== 3" value="一日两次" label="一日两次"/>
                            <el-option v-if="currentRxType !== 3" value="一日三次" label="一日三次"/>
                          </el-select>
                        </template>
                      </el-table-column>
                      <el-table-column label="途径/煎法" width="120">
                        <template #default="{row}">
                          <el-select v-model="row.route" size="small">
                            <el-option v-if="currentRxType === 3" value="先煎" label="先煎"/>
                            <el-option v-if="currentRxType === 3" value="后下" label="后下"/>
                            <el-option v-if="currentRxType !== 3" value="口服" label="口服"/>
                            <el-option v-if="currentRxType !== 3" value="静脉注射" label="静脉注射"/>
                            <el-option v-if="currentRxType !== 3" value="肌肉注射" label="肌肉注射"/>
                            <el-option v-if="currentRxType !== 3" value="外用" label="外用"/>
                          </el-select>
                        </template>
                      </el-table-column>
                      <el-table-column label="总量" width="100">
                        <template #default="{row}">
                          <el-input v-model="row.quantity" size="small"/>
                        </template>
                      </el-table-column>
                      <el-table-column v-if="currentRxType !== 3" label="天数" width="100">
                        <template #default="{row}">
                          <el-input v-model="row.duration" size="small"/>
                        </template>
                      </el-table-column>
                      <el-table-column label="备注" min-width="110">
                        <template #default="{row}">
                          <el-input v-model="row.remark" size="small"/>
                        </template>
                      </el-table-column>
                      <el-table-column label="" width="56">
                        <template #default="{ $index }">
                          <el-button link type="danger" size="small" @click="rxDraft.details.splice($index, 1)">删
                          </el-button>
                        </template>
                      </el-table-column>
                    </el-table>

                    <div v-if="rxErrors.length"
                         class="mt-2 rounded border border-red-200 bg-red-50 p-2 text-xs text-red-700"
                         data-testid="emg-doc-rx-errors">
                      <div v-for="(e, i) in rxErrors" :key="i">· {{ e }}</div>
                    </div>
                  </template>
                </el-tab-pane>

                <!-- ===== 医嘱（检查/检验/用血/手术/会诊）===== -->
                <el-tab-pane label="医嘱" name="order">
                  <EmergencyOrderPanel ref="orderPanelRef" :patient="current" :record-id="recordForm.id"
                                       :editable="current.emergencyStatus === 2"/>
                </el-tab-pane>
              </el-tabs>
            </div>
            <PageActionBar v-if="current" align="start" class="flex-wrap !h-auto">
              <el-button
                  v-if="current.emergencyStatus === 1"
                  v-perm="'opd:emergencyDoctor:edit'"
                  type="primary" :icon="VideoPlay" data-testid="emg-doc-btn-accept"
                  @click="acceptPatient"
              >接诊
              </el-button>
              <el-button
                  v-if="current.emergencyStatus >= 2 && current.emergencyStatus <= 3"
                  v-perm="'opd:emergencyDoctor:edit'"
                  type="warning" :icon="Switch" data-testid="emg-doc-btn-transfer"
                  @click="transferVisible = true"
              >转归
              </el-button>
              <!-- 转归结束（4转住院/5离院/6死亡）后内容只读，流程操作按钮整组不展示 -->
              <template v-if="current.emergencyStatus >= 1 && current.emergencyStatus <= 3">
                <el-button :icon="RefreshRight" v-perm="'opd:emergencyDoctor:edit'"
                           data-testid="emg-doc-btn-reeval" @click="openReeval">分诊复评
                </el-button>
                <el-button :icon="House" v-perm="'opd:emergencyDoctor:edit'"
                           data-testid="emg-doc-btn-obs" @click="openObs">转入留观
                </el-button>
                <el-button :icon="Promotion" type="success" v-perm="'opd:emergencyDoctor:edit'"
                           data-testid="emg-doc-btn-admit" @click="openAdmit">转住院
                </el-button>
                <el-button :icon="Switch" type="warning" v-perm="'opd:emergencyDoctor:edit'"
                           data-testid="emg-doc-btn-handover" @click="openHandover">交班清零
                </el-button>
                <el-button :icon="Postcard" v-perm="'opd:emergencyDoctor:edit'"
                           data-testid="emg-doc-btn-wristband" @click="openWristband">打印腕带
                </el-button>
                <el-button type="danger" plain v-perm="'opd:emergencyDoctor:edit'"
                           data-testid="emg-doc-btn-greenchannel" @click="openGreenChannel">
                  绿色通道{{ current.greenChannel && current.greenChannel !== '无' ? '：' + current.greenChannel : '' }}
                </el-button>
              </template>
              <div class="ml-auto flex items-center gap-3">
                <span class="text-xs text-slate-400">
                  {{
                    current.emergencyStatus === 1 ? '接诊后才能书写病历和开处方'
                        : current.emergencyStatus === 3 ? '留观中：病历处方只读'
                            : current.emergencyStatus >= 4 ? '该患者已转归结束，内容只读'
                                : current.doctorName ? '接诊医生：' + current.doctorName : ''
                  }}
                </span>
                <el-button
                    v-if="current.emergencyStatus === 2"
                    type="primary" :loading="saving" data-testid="emg-doc-btn-save"
                    @click="saveAll">保存
                </el-button>
              </div>
            </PageActionBar>
          </div>

          <!-- 右辅助列：评估与流程常驻（时间轴 / MEWS 趋势 / 专科评分 / 流程按钮） -->
          <div
              class="flex w-[420px] shrink-0 flex-col overflow-hidden rounded-lg border border-slate-200 bg-white shadow-sm">
            <div class="flex shrink-0 items-center justify-between border-b border-slate-200 bg-slate-50 px-3 py-2">
              <span class="text-sm font-bold text-slate-700">评估与流程</span>
              <span class="truncate text-xs text-slate-400">时间轴 / MEWS / 评分</span>
            </div>
            <div class="min-h-0 flex-1 space-y-3 overflow-y-auto p-3">
              <EmergencyTimeline :row="current"/>
              <EmergencyMewsTrend :points="mewsPoints"/>
              <!-- 体征趋势（P1）：分诊登记 + 复评流水回看，随复评自动追加 -->
              <div class="rounded border border-slate-100 bg-slate-50/60 p-2" data-testid="emg-doc-vital-trend">
                <div class="mb-1 flex items-center justify-between">
                  <span class="text-xs font-bold text-slate-500">体征趋势</span>
                  <span class="text-[10px] text-slate-400">复评流水 + 在档体征</span>
                </div>
                <table v-if="vitalTrendRows.length" class="w-full text-[11px] leading-5">
                  <thead>
                  <tr class="text-slate-400">
                    <th class="text-left font-normal">时间</th>
                    <th class="text-right font-normal">体温℃</th>
                    <th class="text-right font-normal">脉搏</th>
                    <th class="text-right font-normal">呼吸</th>
                    <th class="text-right font-normal">血压</th>
                    <th class="text-right font-normal">SpO₂%</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="(p, i) in vitalTrendRows" :key="i" class="border-t border-slate-100">
                    <td class="text-slate-500">{{ p.t }}</td>
                    <td class="text-right">{{ p.temperature ?? '—' }}</td>
                    <td class="text-right">{{ p.pulse ?? '—' }}</td>
                    <td class="text-right">{{ p.respiratory ?? '—' }}</td>
                    <td class="text-right">{{ p.bp ?? '—' }}</td>
                    <td class="text-right">{{ p.spo2 ?? '—' }}</td>
                  </tr>
                  </tbody>
                </table>
                <div v-else class="py-1 text-[11px] text-slate-400">暂无体征记录</div>
              </div>
              <EmergencyScorePanel :patient="current" :vitals="vitalsForScore"
                                   :editable="current.emergencyStatus === 2"
                                   :can-write="current.emergencyStatus === 2"
                                   @write="onScoreWrite"/>
            </div>
          </div>
        </div>
      </template>

    </div>

    <!-- ========== 转归面板（复用护士站同款组件）========== -->
    <EmergencyTransferEdit v-model="transferVisible" :row="current" @saved="onTransferSaved"/>

    <!-- ========== 处方签名板（复用门诊医生工作站同款组件）========== -->
    <RxSignDialog
        v-model="rxSignVisible"
        :doctor-name="current?.doctorName || ''"
        :prescription-count="pendingRxCount"
        @confirm="onRxSigned"
        @cancel="rxSignVisible = false"
    />

    <!-- ========== 软签二次确认（PIN 由签名人本人输入，服务端校验通过才解密私钥）========== -->
    <SignPinDialog ref="pinDialogRef" v-model="pinDialogVisible"/>

    <!-- ========== 医嘱套（复用门诊「药品/耗材套餐」数据；套用=药品进当前处方页签，检查/检验直接提交急诊申请单）========== -->
    <el-dialog v-model="pkgVisible" title="医嘱套" width="640px" destroy-on-close
               data-testid="emg-doc-pkg-dialog">
      <el-alert type="info" show-icon :closable="false" class="!mb-2"
                title="套用后：药品进入当前处方页签（需补齐用量/天数），检查/检验按急诊申请单直接提交，可在「医嘱」页签查看。"/>
      <el-table :data="pkgList" size="small" max-height="360"
                empty-text="暂无医嘱套；可在门诊医生工作站「药品/耗材套餐」中维护"
                data-testid="emg-doc-pkg-table">
        <el-table-column label="套餐名称" prop="packageName" min-width="200" show-overflow-tooltip/>
        <el-table-column label="明细数" width="90" align="center">
          <template #default="{row}">{{ row.detailCount ?? row.details?.length ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center">
          <template #default="{row}">
            <el-button link type="primary" size="small" :loading="pkgApplying"
                       data-testid="emg-doc-pkg-apply" @click="applyPkg(row)">套用
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <template v-if="current">
      <EmergencyReevalEdit v-model="reevalVisible" :row="current" :triage-level-map="triageLevelMap"
                           @saved="onActionSaved"/>
      <EmergencyHandoverEdit v-model="hoVisible" @saved="onActionSaved"/>
      <EmergencyAdmitEdit v-model="admitVisible" :row="current" :wards="wards" @saved="onActionSaved"/>
      <EmergencyObsEdit v-model="obsVisible" :row="current" :wards="wards" @saved="onActionSaved"/>
      <EmergencyWristband v-model="wristbandVisible" :row="current" :triage-level-map="triageLevelMap"/>

      <!-- 绿色通道一键设置 -->
      <el-dialog v-model="gcVisible" title="绿色通道" width="420px" data-testid="emg-gc-dialog">
        <el-alert type="info" show-icon :closable="false" class="!mb-3"
                  title="开通后优先检查/优先处置；口径与分诊登记一致，可随时变更"/>
        <el-radio-group v-model="gcForm.greenChannel" data-testid="emg-gc-radio">
          <el-radio v-for="o in GC_OPTIONS" :key="o" :value="o">{{ o }}</el-radio>
        </el-radio-group>
        <template #footer>
          <el-button size="small" @click="gcVisible = false">取消</el-button>
          <el-button size="small" type="primary" :loading="gcSaving"
                     data-testid="emg-gc-save" @click="saveGreenChannel">保存
          </el-button>
        </template>
      </el-dialog>

      <!-- 患者详情（主档 / 健康档案 / 历次就诊，复用全局组件） -->
      <PatientDetailDialog v-model="showPatientDetail" :patient="current" :patient-id="current?.patientId"/>

      <!-- 医保与费别（急诊医生站只读查看，结算走收费台/床旁结算） -->
      <el-dialog v-model="showInsuranceDialog" title="医保与费别" width="380px">
        <div class="space-y-3">
          <div class="rounded-lg bg-blue-50 p-3">
            <div class="text-xs text-slate-500">费别 / 医保类型</div>
            <div class="mt-0.5 text-base font-bold text-slate-900">
              {{ patientDetail?.medicalInsuranceType || '自费' }}
            </div>
          </div>
          <div class="grid grid-cols-2 gap-2 text-sm">
            <div class="rounded border border-slate-200 p-2">
              <div class="text-xs text-slate-400">患者</div>
              <div class="font-medium text-slate-800">{{ current.patientName }}</div>
            </div>
            <div class="rounded border border-slate-200 p-2">
              <div class="text-xs text-slate-400">就诊号</div>
              <div class="truncate font-mono text-slate-600" :title="current.patientNo">{{ current.patientNo }}</div>
            </div>
          </div>
          <div class="text-xs text-slate-400">费用结算与医保报销请到收费台办理（或床旁结算）。</div>
        </div>
        <template #footer>
          <el-button size="small" @click="showInsuranceDialog = false">关闭</el-button>
        </template>
      </el-dialog>

      <!-- 患者标签管理（口径同门诊医生站） -->
      <el-dialog v-model="showTagDialog" :close-on-click-modal="false" title="患者标签管理" width="500px">
        <div class="space-y-4">
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
          <div>
            <h4 class="mb-2 text-sm font-medium text-slate-700">添加标签</h4>
            <el-input v-model="tagSearchKeyword" :prefix-icon="Search" class="mb-3" clearable
                      placeholder="搜索标签..."/>
            <div class="max-h-60 overflow-y-auto">
              <div class="flex flex-wrap gap-2">
                <span v-for="tag in filteredAllTags" :key="tag.tagId"
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
    </template>
  </div>
</template>

<script setup>
import {computed, onBeforeUnmount, onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {useRouter} from 'vue-router';
import {
  Bell,
  Delete,
  FirstAidKit,
  House,
  Plus,
  Postcard,
  Promotion,
  Refresh,
  RefreshRight,
  Search,
  Switch,
  VideoPlay
} from '@element-plus/icons-vue';
import PatientBriefBar from '@/components/his/PatientBriefBar.vue';
import PatientDetailDialog from '@/components/his/PatientDetailDialog.vue';
import PageActionBar from '@/components/his/PageActionBar.vue';
import SignPinDialog from '@/components/his/SignPinDialog.vue';
import RxSignDialog from '@/views/doctor-workstation/RxSignDialog.vue';
import EmergencyTransferEdit from '@/views/emergency/EmergencyTransferEdit.vue';
import EmergencyTimeline from '@/views/emergency/EmergencyTimeline.vue';
import EmergencyMewsTrend from '@/views/emergency/EmergencyMewsTrend.vue';
import EmergencyReevalEdit from '@/views/emergency/EmergencyReevalEdit.vue';
import EmergencyHandoverEdit from '@/views/emergency/EmergencyHandoverEdit.vue';
import EmergencyAdmitEdit from '@/views/emergency/EmergencyAdmitEdit.vue';
import EmergencyObsEdit from '@/views/emergency/EmergencyObsEdit.vue';
import EmergencyWristband from '@/views/emergency/EmergencyWristband.vue';
import EmergencyOrderPanel from '@/views/emergency/EmergencyOrderPanel.vue';
import EmergencyScorePanel from '@/views/emergency/EmergencyScorePanel.vue';
import {getPatientTagListAll, searchIcd10} from '@/api/system';
import {
  getEmergencyList,
  getEmergencyReevalList,
  getEmergencyWardSelectList,
  updateEmergencyGreenChannel,
  updateEmergencyStatus
} from '@/api/emergency';
// 叫号套件（与门诊医生站同一组接口）：呼叫=纯播报（急诊行后端保状态）、重呼、过号、过号回队
import {callPatient, overdueQueue, recallPatient, rejoinQueue} from '@/api/appoint';
// 医嘱套（复用门诊「药品/耗材套餐」数据与接口）+ 检查/检验急诊开单
import {
  getDiagTemplates,
  getDrugPackageDetail,
  getDrugPackages,
  getPrescriptionList,
  saveInspectionApply,
  saveLaboratoryApply,
  saveMedicalRecord,
  submitMedicalRecord
} from '@/api/doctor';
// 传染病诊断触发：法定传染病目录（公共参照数据，只要求登录）
import {getDiseaseSelectList} from '@/api/infectiousReport';
import {handleEmrSubmitVeto} from '@/lib/emrSubmit';
import {getByRegistId} from '@/api/emr';
import {addPatientTag, getPatientFullDetail, getPatientTags, removePatientTag} from '@/api/patient';
import {getStockList} from '@/api/pharmacy';
import {getChronicActiveByPatient} from '@/api/chronic';
import {PATIENT_GENDER_OPTIONS} from '@/lib/patientGender';
import {useCurrentPatientStore} from '@/stores/currentPatient';

const router = useRouter();

// ---------- 常量（口径与急诊护士工作站 EmergencyView 同源，不引变量避免跨页耦合）----------
const triageLevelMap = {
  1: {label: 'I级 濒危', color: 'bg-red-100 text-red-700 border-red-300', zone: '红区'},
  2: {label: 'II级 危重', color: 'bg-orange-100 text-orange-700 border-orange-300', zone: '红区'},
  3: {label: 'III级 急症', color: 'bg-amber-100 text-amber-700 border-amber-300', zone: '黄区'},
  4: {label: 'IV级 非急症', color: 'bg-emerald-100 text-emerald-700 border-emerald-300', zone: '绿区'},
};
const statusMap = {
  1: {label: '候诊', type: 'primary'},
  2: {label: '诊治中', type: 'warning'},
  3: {label: '留观', type: 'info'},
  4: {label: '转住院', type: 'success'},
  5: {label: '离院', type: 'info'},
  6: {label: '死亡', type: 'danger'},
};
const RX_TYPE_LABEL = {1: '西药', 2: '中成药', 3: '中药饮片'};

// ---------- 队列 ----------
const queueTab = ref('mine'); // mine=我的患者(派给我/我接诊的,未结束) pool=待派单池
const queueList = ref([]);
const mineRowsRef = ref([]);
const poolRowsRef = ref([]);
const mineCount = ref(0);
const poolCount = ref(0);
const criticalCount = ref(0);
const current = ref(null);
let pollTimer = null;
const currentPatientStore = useCurrentPatientStore();

const levelOf = (row) => triageLevelMap[row?.triageLevel] || triageLevelMap[4];
const statusText = (s) => (statusMap[s] || {label: '—'}).label;
const statusTagType = (s) => (statusMap[s] || {type: 'info'}).type;
const genderText = (g) => (PATIENT_GENDER_OPTIONS.find((o) => o.value === g) || {}).label || '未知';

// ---------- 急诊叫号板（与门诊叫号板同款交互：大号码 + 状态灯 + 下一位） ----------
// 叫号短号口径与后端护士站一致（EmergencyServiceImpl：叫号屏只放「急+当日尾5位」短号，
// 全量 JZ 单号留在登记表/队列卡上，不丢）
const shortNo = (no) => {
  const s = String(no || '');
  return s ? '急' + s.slice(-5) : '—';
};
const zoneTone = (row) => (row?.triageLevel <= 2 ? 'text-red-600' : row?.triageLevel === 3 ? 'text-amber-600' : 'text-emerald-600');
const zoneBgTone = (row) => (row?.triageLevel <= 2 ? 'bg-red-500' : row?.triageLevel === 3 ? 'bg-amber-500' : 'bg-emerald-500');
// 下一位 = 我名下的候诊（status=1）且**未被过号**的队首（后端已按级别/时间排序）。
// 过号（只落在队列行 is_overdue 上）的人不该被「接诊下一位」自动选中——
// 叫了没到就是这一位先跳过，患者回来了走行内「回队」再排回来
const nextWaiting = computed(() => (mineRowsRef.value || []).find((r) => (r.emergencyStatus ?? 1) === 1 && !r.isOverdue) || null);
const acceptNext = async () => {
  const next = nextWaiting.value;
  if (!next) return;
  await selectPatient(next);
  await acceptPatient();
};

const reloadQueue = async () => {
  try {
    const [mine, pool] = await Promise.all([
      getEmergencyList({mineOnly: true, pageNum: 1, pageSize: 100}),
      getEmergencyList({unassignedOnly: true, pageNum: 1, pageSize: 100}),
    ]);
    // 「我的患者」= 派给我 / 我接诊的、且未结束（<4）的急诊记录。
    // 关键修复：原先硬传 emergencyStatus:1，导致「已接诊未结诊(status=2)」的患者
    // 既不在「我的候诊」(要 status=1) 也不在「待派单池」(要 doctor_id 为空)，界面彻底消失。
    // 这里改按 doctor_id=我 取全部活动态（1 候诊 / 2 诊治中 / 3 留观），终态(>=4)不进队列。
    const mineRows = (mine.data?.records || []).filter((r) => (r.emergencyStatus ?? 1) < 4);
    const poolRows = pool.data?.records || [];
    mineRowsRef.value = mineRows;
    poolRowsRef.value = poolRows;
    mineCount.value = mineRows.length;
    poolCount.value = poolRows.length;
    criticalCount.value = mineRows.filter((r) => r.triageLevel <= 2).length;
    queueList.value = queueTab.value === 'mine' ? mineRows : poolRows;
  } catch (e) { /* 错误已由全局拦截器提示 */
  }
};

// ---------- 队列筛选（区域 / 状态 / 检索，纯前端，零后端改动）----------
const zoneFilter = ref('all');
const statusFilter = ref('all');
const searchKeyword = ref('');
const zoneOf = (row) =>
    row.triageLevel === 1 || row.triageLevel === 2 ? 'red'
        : row.triageLevel === 3 ? 'yellow' : 'green';
const displayQueue = computed(() => {
  // 检索口径：姓名 / 患者号 / 急诊号（用户直接输「急+尾5位」里的数字也能命中全量单号）
  const kw = (searchKeyword.value || '').trim().toLowerCase();
  return (queueList.value || []).filter((r) => {
    if (zoneFilter.value !== 'all' && zoneOf(r) !== zoneFilter.value) return false;
    if (statusFilter.value !== 'all' && (r.emergencyStatus ?? 0) !== Number(statusFilter.value)) return false;
    if (kw) {
      const hit = (r.patientName || '').toLowerCase().includes(kw)
          || String(r.patientNo || '').toLowerCase().includes(kw)
          || String(r.emergencyNo || '').toLowerCase().includes(kw);
      if (!hit) return false;
    }
    return true;
  });
});

// ---------- 当前患者 ----------
const patientDetail = ref(null);
const reevalList = ref([]);
const activeTab = ref('record');
const recordForm = reactive({
  id: null, chiefComplaint: '', presentIllness: '', allergyHistory: '',
  pastHistory: '', personalHistory: '', familyHistory: '',
  temperature: '', pulse: '', respiration: '', systolicPressure: '', diastolicPressure: '',
  generalCondition: '',
  diagnosis: '', diagnosisCode: '', diagnosisName: '', treatmentPlan: '',
});

// ---------- 快捷录入（2026-10-28）----------
// 体征一键正常：医生显式点按才覆盖（轻症快速成文），填入后必须人工核对再保存
const fillNormalVitals = () => {
  recordForm.temperature = '36.5';
  recordForm.pulse = '80';
  recordForm.respiration = '18';
  recordForm.systolicPressure = '120';
  recordForm.diastolicPressure = '80';
  ElMessage.success('已按正常值填入五项体征，请核对后保存');
};
// 阴性史/查体：只补空字段，已写内容一律不覆盖
const fillEmptyOnly = (pairs) => {
  let n = 0;
  for (const [k, v] of pairs) {
    if (!String(recordForm[k] || '').trim()) {
      recordForm[k] = v;
      n++;
    }
  }
  ElMessage.success(n ? `已填充 ${n} 项（已有内容不覆盖）` : '各项均已有内容，未覆盖');
};
const fillNegativeHistory = () => fillEmptyOnly([
  ['allergyHistory', '否认药物、食物过敏史。'],
  ['pastHistory', '既往体健，无高血压、糖尿病等慢性病史。'],
  ['personalHistory', '无特殊。'],
  ['familyHistory', '无特殊。'],
]);
const fillNormalExam = () => fillEmptyOnly([
  ['generalCondition', '神志清楚，精神可，心肺腹查体未见明显异常。'],
]);

// ---------- ICD-10 结构化诊断（复用 system.js searchIcd10，后端零改动）----------
const icdOptions = ref([]);
const icdLoading = ref(false);
const icdSelected = ref('');
const searchIcd10Remote = async (query) => {
  if (!query) {
    icdOptions.value = [];
    return;
  }
  icdLoading.value = true;
  try {
    const res = await searchIcd10(query);
    icdOptions.value = res.data || [];
  } catch (e) { /* 全局提示 */
  } finally {
    icdLoading.value = false;
  }
};
const onIcdSelected = (code) => {
  const opt = icdOptions.value.find((o) => o.icdCode === code);
  if (opt) {
    recordForm.diagnosisCode = opt.icdCode;
    recordForm.diagnosisName = opt.icdName;
  }
};

// ---------- 常用诊断 chips（复用医生个人模板 diagList） ----------
const diagTplList = ref([]);
const loadDiagTpls = async () => {
  try {
    const res = await getDiagTemplates();
    diagTplList.value = res.data || [];
  } catch (e) {
    diagTplList.value = [];
  }
};
const applyDiagTpl = (t) => {
  recordForm.diagnosisCode = t.icdCode || '';
  recordForm.diagnosisName = t.icdName || '';
  icdSelected.value = t.icdCode || '';
  ElMessage.success('已套用常用诊断：' + (t.icdName || ''));
};

const vitalChips = computed(() => {
  let v = {};
  try {
    v = JSON.parse(current.value?.vitalSigns || '{}') || {};
  } catch (e) {
    v = {};
  }
  // 键名 = 护士站 buildVitalSigns 的口径：temperature/pulse/respiratory/systolic/diastolic/spo2
  const chips = [];
  if (v.temperature != null && v.temperature !== '') chips.push({label: 'T', value: v.temperature + '℃'});
  if (v.pulse != null && v.pulse !== '') chips.push({label: 'P', value: v.pulse});
  if (v.respiratory != null && v.respiratory !== '') chips.push({label: 'R', value: v.respiratory});
  if (v.systolic != null && v.diastolic != null) chips.push({
    label: 'BP',
    value: v.systolic + '/' + v.diastolic
  });
  if (v.spo2 != null && v.spo2 !== '') chips.push({label: 'SpO2', value: v.spo2 + '%'});
  return chips;
});

const selectPatient = async (row) => {
  current.value = row;
  activeTab.value = 'record';
  resetForms();
  // 同步顶部「当前患者」条（口径同门诊医生工作站：患者条亮起后可全局带人）
  currentPatientStore.syncPatient({
    id: String(row.patientId), patientName: row.patientName, patientNo: row.patientNo,
    gender: row.gender, age: row.age,
  });
  try {
    const [detail, recordRes, rxRes, chronicRes] = await Promise.all([
      getPatientFullDetail(row.patientId),
      row.registId ? getByRegistId({patientId: row.patientId, registId: row.registId}) : Promise.resolve({data: null}),
      row.registId ? getPrescriptionList({
        patientId: row.patientId,
        registId: row.registId
      }) : Promise.resolve({data: []}),
      getChronicActiveByPatient(row.patientId),
    ]);
    chronicRecords.value = chronicRes.data || [];
    chronicQualified.value = chronicRecords.value.length > 0;
    patientDetail.value = detail.data || null;
    if (recordRes.data) Object.assign(recordForm, recordRes.data);
    // 护士站分诊录入直接带入，医生不重录：主诉 + 体征空槽位（已有病历/已填的字段不覆盖）。
    // 坑：getByRegistId 对「还没写过病历」的急诊患者也返回字段全 null 的空对象，
    // Object.assign 会把表单里的 '' 洗成 null —— 所以空槽位判断必须用 blank() 而不是 === ''。
    // 键名转换：护士站存 respiratory/systolic/diastolic（buildVitalSigns 口径），
    // 病历表单字段是 respiration/systolicPressure/diastolicPressure。
    const blank = (x) => x == null || x === '';
    let tv = {};
    try {
      tv = JSON.parse(row.vitalSigns || '{}') || {};
    } catch (e) {
      tv = {};
    }
    if (blank(recordForm.chiefComplaint) && row.chiefComplaint) recordForm.chiefComplaint = row.chiefComplaint;
    if (blank(recordForm.temperature) && tv.temperature != null) recordForm.temperature = String(tv.temperature);
    if (blank(recordForm.pulse) && tv.pulse != null) recordForm.pulse = String(tv.pulse);
    if (blank(recordForm.respiration) && tv.respiratory != null) recordForm.respiration = String(tv.respiratory);
    if (blank(recordForm.systolicPressure) && tv.systolic != null) recordForm.systolicPressure = String(tv.systolic);
    if (blank(recordForm.diastolicPressure) && tv.diastolic != null) recordForm.diastolicPressure = String(tv.diastolic);
    icdSelected.value = recordForm.diagnosisCode || '';
    // rxLoaded 是固定引用的数组：只改内容不改引用（rxDetailCount computed 盯着它）
    rxLoaded.length = 0;
    rxLoaded.push(...(rxRes.data || []));
    bindRxDraft();
    // 复评流水（MEWS 趋势点来源）：独立请求，失败不影响主流程
    getEmergencyReevalList(row.id)
        .then((r) => {
          reevalList.value = r.data || [];
        })
        .catch(() => {
          reevalList.value = [];
        });
  } catch (e) { /* 错误已由全局拦截器提示 */
  }
};

const resetForms = () => {
  Object.assign(recordForm, {
    id: null, chiefComplaint: '', presentIllness: '', allergyHistory: '',
    pastHistory: '', personalHistory: '', familyHistory: '',
    temperature: '', pulse: '', respiration: '', systolicPressure: '', diastolicPressure: '',
    generalCondition: '',
    diagnosis: '', diagnosisCode: '', diagnosisName: '', treatmentPlan: '',
  });
  rxLoaded.length = 0;
  bindRxDraft();
};

// ---------- 接诊 ----------
const acceptPatient = async () => {
  if (!current.value) return;
  try {
    await updateEmergencyStatus(current.value.id, 2);
    ElMessage.success(`已接诊：${current.value.patientName}`);
    // 本地即时翻态，让按钮/门控马上正确，再拉队列（该患者会从候诊列表消失）
    current.value = {...current.value, emergencyStatus: 2, doctorName: current.value.doctorName || '我'};
    await reloadQueue();
  } catch (e) { /* 错误已由全局拦截器提示 */
  }
};

// ---------- 叫号套件（呼叫/重呼/过号/回队，对齐门诊医生站同组接口） ----------
// 急诊口径：呼叫=纯播报（后端对急诊行保「候诊中」不翻态，接诊仍走「接诊」按钮）；
// 过号/回队只动队列行（is_overdue），不改急诊记录的临床态——过号的人临床上还在候诊
const ensureQueueId = (row) => {
  if (!row?.queueId) {
    ElMessage.warning('该患者没有关联的叫号队列（老数据），无法执行叫号操作');
    return null;
  }
  return row.queueId;
};
const handleCallRow = async (row) => {
  const qid = ensureQueueId(row);
  if (!qid) return;
  try {
    await callPatient(qid);
    ElMessage.success(`已呼叫 ${row.patientName}（${shortNo(row.emergencyNo)}），请患者到诊室`);
    reloadQueue();
  } catch (e) { /* 全局提示 */
  }
};
const handleRecall = async (row) => {
  const qid = ensureQueueId(row);
  if (!qid) return;
  try {
    await recallPatient(qid);
    ElMessage.success(`已重呼 ${row.patientName}`);
  } catch (e) { /* 全局提示 */
  }
};
const handleOverdueRow = async (row) => {
  const qid = ensureQueueId(row);
  if (!qid) return;
  try {
    await ElMessageBox.confirm(
        `确认将 ${row.patientName}（${shortNo(row.emergencyNo)}）标记为过号？过号后患者返回可随时「回队」。`,
        '过号处理', {confirmButtonText: '确认过号', cancelButtonText: '取消', type: 'warning'});
  } catch (e) {
    return;
  }
  try {
    await overdueQueue(qid);
    ElMessage.success(`已过号：${row.patientName}`);
    reloadQueue();
  } catch (e) { /* 全局提示 */
  }
};
const handleRejoinRow = async (row) => {
  const qid = ensureQueueId(row);
  if (!qid) return;
  try {
    await rejoinQueue(qid, 2);
    ElMessage.success(`已回队：${row.patientName} 排到候诊队尾`);
    reloadQueue();
  } catch (e) { /* 全局提示 */
  }
};
// 行内按钮组：候诊→[呼叫/过号]；已过号→[回队]；诊治中→[重呼]；其余（留观/终态）无叫号操作
const rowActions = (row) => {
  if (row.isOverdue || row.queueStatus === 6) {
    return [{key: 'rejoin', label: '回队', type: 'success', run: handleRejoinRow}];
  }
  if ((row.emergencyStatus ?? 1) === 1) {
    return [
      {key: 'call', label: '呼叫', type: 'primary', run: handleCallRow},
      {key: 'overdue', label: '过号', type: 'warning', run: handleOverdueRow},
    ];
  }
  if (row.emergencyStatus === 2) {
    return [{key: 'recall', label: '重呼', type: 'primary', run: handleRecall}];
  }
  return [];
};

// ---------- 转归 ----------
const transferVisible = ref(false);
const onTransferSaved = () => {
  transferVisible.value = false;
  current.value = null;
  reloadQueue();
};

// ---------- 复评 / 交接班 / 入院 / 留观 / 腕带（复用急诊总览页同款组件）----------
const reevalVisible = ref(false);
const openReeval = () => {
  reevalVisible.value = true;
};
const hoVisible = ref(false);
const openHandover = () => {
  hoVisible.value = true;
};
const admitVisible = ref(false);
const openAdmit = async () => {
  if (wards.value.length === 0) await loadWards();
  admitVisible.value = true;
};
const obsVisible = ref(false);
const openObs = async () => {
  if (wards.value.length === 0) await loadWards();
  obsVisible.value = true;
};
const wristbandVisible = ref(false);
const openWristband = () => {
  wristbandVisible.value = true;
};
const onActionSaved = () => {
  reevalVisible.value = false;
  hoVisible.value = false;
  admitVisible.value = false;
  obsVisible.value = false;
  current.value = null;
  reloadQueue();
};

// MEWS 趋势点：复评历史 + 当前分（单点=无复评，仅有当前值）
const mewsPoints = computed(() => {
  const list = reevalList.value || [];
  const pts = list.map((r) => ({t: (r.createTime || '').slice(5, 16), score: r.mewsScore}));
  if (current.value?.mewsScore != null) pts.push({t: '当前', score: current.value.mewsScore});
  return pts;
});

// 体征趋势（P1）：复评流水回看 + 在档体征。口径：复评会把主记录 vitalSigns 回写成最新复测值
// （EmergencyServiceImpl#reevaluate），所以「登记原值」复评后不再单独成点，末条流水与在档值相同则去重。
const vitalTrendRows = computed(() => {
  const parse = (s) => {
    try {
      return JSON.parse(s || '{}') || {};
    } catch (e) {
      return {};
    }
  };
  const vitalKey = (v) => [v.temperature, v.pulse, v.respiratory, v.systolic, v.diastolic, v.spo2].join('|');
  const rows = (reevalList.value || []).map((r) => ({
    key: r.id, time: r.createTime || '', t: (r.createTime || '').slice(5, 16), ...parse(r.vitalSigns),
  }));
  const cur = parse(current.value?.vitalSigns);
  const last = rows[rows.length - 1];
  if (Object.keys(cur).length && (!last || vitalKey(cur) !== vitalKey(last))) {
    rows.push({key: 'cur', time: current.value?.lastReevalTime || current.value?.createTime || '', t: '当前', ...cur});
  }
  rows.sort((a, b) => String(a.time).localeCompare(String(b.time)));
  return rows.slice(-8).map((p) => ({
    t: p.t,
    temperature: p.temperature ?? null,
    pulse: p.pulse ?? null,
    respiratory: p.respiratory ?? null,
    bp: (p.systolic != null || p.diastolic != null) ? `${p.systolic ?? '?'}/${p.diastolic ?? '?'}` : null,
    spo2: p.spo2 ?? null,
  }));
});

// ---------- 专科评分（GCS/RTS/REMS）体征预填与写回 ----------
const GC_OPTIONS = ['无', '胸痛中心', '卒中中心', '创伤中心'];
const num = (x) => (x === '' || x == null || isNaN(Number(x)) ? null : Number(x));
const vitalsForScore = computed(() => {
  let v = {};
  try {
    v = JSON.parse(current.value?.vitalSigns || '{}') || {};
  } catch (e) {
    v = {};
  }
  return {
    sbp: num(recordForm.systolicPressure) ?? num(v.systolic),
    dbp: num(recordForm.diastolicPressure) ?? num(v.diastolic),
    rr: num(recordForm.respiration) ?? num(v.respiratory),
    hr: num(recordForm.pulse) ?? num(v.pulse),
    spo2: num(v.spo2),
  };
});
const onScoreWrite = (text) => {
  const tag = '【专科评分】';
  const cur = recordForm.generalCondition || '';
  if (cur.includes(text)) {
    ElMessage.info('该评分已在体格检查中');
    return;
  }
  recordForm.generalCondition = cur ? cur + '\n' + tag + text : tag + text;
  ElMessage.success('已写入体格检查，保存病历后生效');
};

// ---------- 绿色通道（医生站一键设置；口径与分诊登记同源） ----------
const gcVisible = ref(false);
const gcSaving = ref(false);
const gcForm = reactive({id: null, greenChannel: '无'});
const openGreenChannel = () => {
  gcForm.id = current.value?.id;
  gcForm.greenChannel = current.value?.greenChannel || '无';
  gcVisible.value = true;
};
const saveGreenChannel = async () => {
  gcSaving.value = true;
  try {
    await updateEmergencyGreenChannel({id: gcForm.id, greenChannel: gcForm.greenChannel});
    ElMessage.success('绿色通道已更新');
    current.value = {...current.value, greenChannel: gcForm.greenChannel};
    gcVisible.value = false;
  } catch (e) { /* 全局提示 */
  } finally {
    gcSaving.value = false;
  }
};

// ---------- 患者详情 / 医保 / 标签（患者条三个按钮，口径同门诊医生站） ----------
const showPatientDetail = ref(false);
const showInsuranceDialog = ref(false);
const showTagDialog = ref(false);
const patientTags = ref([]);
const allTags = ref([]);
const tagSearchKeyword = ref('');
const filteredAllTags = computed(() => {
  if (!tagSearchKeyword.value) return allTags.value;
  const kw = tagSearchKeyword.value.toLowerCase();
  return allTags.value.filter((tag) => tag.tagName?.toLowerCase().includes(kw));
});
const isTagAdded = (tagId) => patientTags.value.some((t) => t.tagId === tagId);
const loadAllTags = async () => {
  try {
    const res = await getPatientTagListAll({});
    allTags.value = res.data?.records || res.data || [];
  } catch (e) { /* 全局提示 */
  }
};
const loadPatientTags = async () => {
  if (!current.value?.patientId) return;
  try {
    const res = await getPatientTags({patientId: current.value.patientId});
    patientTags.value = res.data || [];
  } catch (e) { /* 全局提示 */
  }
};
const handleOpenTagDialog = async () => {
  await Promise.all([loadAllTags(), loadPatientTags()]);
  showTagDialog.value = true;
};
const handleAddTag = async (tagId) => {
  if (!current.value?.patientId) return;
  try {
    await addPatientTag({patientId: current.value.patientId, tagId: tagId, sourceType: 1});
    await loadPatientTags();
    ElMessage.success('标签添加成功');
  } catch (e) { /* 全局提示 */
  }
};
const handleRemoveTag = async (tagId) => {
  if (!current.value?.patientId) return;
  try {
    await removePatientTag({patientId: current.value.patientId, tagId: tagId});
    await loadPatientTags();
    ElMessage.success('标签已移除');
  } catch (e) { /* 全局提示 */
  }
};

// 停留时间：从入急诊（admissionTime 优先，否则登记 createTime）算到现在
const stayText = computed(() => {
  const r = current.value;
  if (!r) return '—';
  const t = r.admissionTime || r.createTime;
  if (!t) return '—';
  const start = new Date(String(t).replace(/-/g, '/').replace('T', ' '));
  if (isNaN(start.getTime())) return '—';
  const mins = Math.floor((Date.now() - start.getTime()) / 60000);
  if (mins < 0) return '—';
  if (mins < 60) return mins + ' 分';
  return `${Math.floor(mins / 60)} 小时 ${mins % 60} 分`;
});

// 病区名册（转住院 / 留观弹框共用）
const wards = ref([]);
const loadWards = async () => {
  try {
    const res = await getEmergencyWardSelectList();
    wards.value = res.data || [];
  } catch (e) {
    wards.value = [];
  }
};

// ---------- 慢病长处方资格（患者切换即查；服务端才是最终权威，此处只为开关 disabled 预检） ----------
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
    // 资格查询失败不当有资格（开关禁用）；真漏判由服务端保存时拦，不假装能开
    chronicQualified.value = false;
  }
};

// ---------- 处方 ----------
const currentRxType = ref(1);
const rxLoaded = reactive([]); // 已从服务端加载的处方列表（三个类型各一张）；🔴 必须 reactive——普通数组 length=0/push 不触发 rxDetailCount computed，保存后 tab 计数与摘要条不更新
const rxDraft = reactive({
  doseCount: 7, decoctFlag: null, details: [],
  // 慢病长处方（处方级）：患者有已认定慢病档案才允许开；天数 1~90，服务端强校验
  isLongPrescription: false, longPrescriptionDays: 28
});
const drugResults = ref([]);
const drugLoading = ref(false);
const rxErrors = ref([]);

const rxDetailCount = computed(() => rxLoaded.reduce((s, p) => s + (p.details?.length || 0), 0));

const bindRxDraft = () => {
  const found = rxLoaded.find((p) => p.prescriptionType === currentRxType.value);
  rxDraft.details = found?.details ? JSON.parse(JSON.stringify(found.details)) : [];
  rxDraft.doseCount = found?.doseCount || 7;
  rxDraft.decoctFlag = found?.decoctFlag ?? null;
  rxDraft.isLongPrescription = found?.isLongPrescription === 1;
  rxDraft.longPrescriptionDays = found?.longPrescriptionDays > 0 ? found.longPrescriptionDays : 28;
};

const switchRxTab = () => {
  // 切走前把当前编辑态回写到 rxLoaded，避免丢
  flushRxDraft();
  bindRxDraft();
};

const flushRxDraft = () => {
  let found = rxLoaded.find((p) => p.prescriptionType === currentRxType.value);
  if (!found) {
    if (!rxDraft.details.length) return;
    found = {
      prescriptionType: currentRxType.value, diagnosis: '', usageInstruction: '', details: [],
      isLongPrescription: 0, longPrescriptionDays: null
    };
    rxLoaded.push(found);
  }
  found.details = rxDraft.details;
  found.doseCount = rxDraft.doseCount;
  found.decoctFlag = rxDraft.decoctFlag;
  found.isLongPrescription = rxDraft.isLongPrescription ? 1 : 0;
  found.longPrescriptionDays = rxDraft.isLongPrescription ? rxDraft.longPrescriptionDays : null;
};

const addRxRow = () => {
  rxDraft.details.push({
    drugId: null,
    drugName: '',
    specification: '',
    unit: '',
    singleDosage: '',
    frequency: currentRxType.value === 3 ? '每日一剂' : '一日三次',
    route: currentRxType.value === 3 ? '' : '口服',
    quantity: '',
    duration: '',
    remark: ''
  });
};

const searchDrug = async (query) => {
  if (!query) {
    drugResults.value = [];
    return;
  }
  drugLoading.value = true;
  try {
    const res = await getStockList({drugName: query, pageNum: 1, pageSize: 20});
    drugResults.value = res.data?.records || [];
  } catch (e) { /* 全局提示 */
  } finally {
    drugLoading.value = false;
  }
};

const onDrugPicked = (row) => {
  const drug = drugResults.value.find((d) => d.drugId === row.drugId);
  if (drug) {
    row.drugName = drug.drugName;
    row.specification = drug.specification || '';
    row.unit = drug.unit || '';
  }
};

// ---------- 医嘱套（P1）：复用门诊「药品/耗材套餐」，套用=药品进当前处方页签，检查/检验直接提交急诊申请单 ----------
const orderPanelRef = ref(null);
const pkgVisible = ref(false);
const pkgList = ref([]);
const pkgApplying = ref(false);
// 处方明细下拉只放行这几个枚举值，套餐里的历史写法（如 bid/tid）归一到默认值
const RX_FREQ_OK = ['每日一剂', '每日两剂', '一日一次', '一日两次', '一日三次'];
const RX_ROUTE_OK = ['先煎', '后下', '口服', '静脉注射', '肌肉注射', '外用'];
const openPkgDialog = async () => {
  pkgVisible.value = true;
  try {
    const res = await getDrugPackages();
    pkgList.value = res.data || [];
  } catch (e) {
    pkgList.value = [];
  }
};
const applyPkg = async (pkg) => {
  if (pkgApplying.value) return;
  pkgApplying.value = true;
  try {
    const res = await getDrugPackageDetail(pkg.id);
    const details = res.data?.details || [];
    // 检查/检验开单即签：一个套餐可能含多张申请单，弹一次 PIN 复用于本批次全部签名
    const hasSignedItem = details.some((d) => (d.itemType === 2 || d.itemType === 3)
        && current.value?.registId && current.value?.patientId);
    let pin = null;
    if (hasSignedItem) {
      pin = await pinDialogRef.value.askPin();
      if (!pin) {
        ElMessage.info('已取消签名，套餐未套用');
        return;
      }
    }
    let rxCount = 0, insCount = 0, labCount = 0;
    for (const d of details) {
      if (d.itemType === 1) {
        rxDraft.details.push({
          drugId: d.itemId, drugName: d.itemName,
          specification: d.specification || '', unit: d.unit || '',
          singleDosage: d.usageDosage || '',
          frequency: RX_FREQ_OK.includes(d.frequency) ? d.frequency : (currentRxType.value === 3 ? '每日一剂' : '一日三次'),
          route: RX_ROUTE_OK.includes(d.route) ? d.route : (currentRxType.value === 3 ? '' : '口服'),
          quantity: d.quantity != null ? String(d.quantity) : '',
          duration: d.duration != null ? String(d.duration) : '',
          remark: '',
        });
        // 套餐药品不在远程搜索候选里：补一条候选，否则 el-select 显示裸 drugId
        if (!drugResults.value.some((o) => o.drugId === d.itemId)) {
          drugResults.value = [...drugResults.value,
            {drugId: d.itemId, drugName: d.itemName, specification: d.specification || '', unit: d.unit || ''}];
        }
        rxCount++;
      } else if ((d.itemType === 2 || d.itemType === 3) && current.value?.registId && current.value?.patientId) {
        const base = {
          registId: current.value.registId,
          patientId: current.value.patientId,
          patientName: current.value.patientName,
          patientNo: current.value.patientNo,
          clinicalDiagnosis: recordForm.diagnosisName || '',
          isEmergency: 1,
          pin,
        };
        if (recordForm.id) base.recordId = recordForm.id;
        if (d.itemType === 2) {
          await saveInspectionApply({...base, inspectionItemId: d.itemId, inspectionItemName: d.itemName});
          insCount++;
        } else {
          await saveLaboratoryApply({...base, laboratoryItemId: d.itemId, laboratoryItemName: d.itemName});
          labCount++;
        }
      }
    }
    pkgVisible.value = false;
    const parts = [];
    if (rxCount) parts.push(`药品 ${rxCount} 项进处方（请补齐用量/天数）`);
    if (insCount) parts.push(`检查 ${insCount} 单已提交`);
    if (labCount) parts.push(`检验 ${labCount} 单已提交`);
    if (!parts.length) {
      ElMessage.info('该套餐没有可套用的明细');
      return;
    }
    ElMessage.success(`已套用「${pkg.packageName}」：` + parts.join('，'));
    if ((insCount || labCount) && orderPanelRef.value?.reloadAll) orderPanelRef.value.reloadAll();
  } catch (e) { /* 全局提示 */
  } finally {
    pkgApplying.value = false;
  }
};

// ---------- 传染病诊断触发（P1）：保存病历后核对法定传染病目录，命中提示上报（同一患者同一诊断只提示一次） ----------
const infectiousDiseases = ref(null);
const infectiousPrompted = new Set();
const checkInfectiousReport = async () => {
  const name = (recordForm.diagnosisName || '').trim();
  const pid = current.value?.id;
  if (!name || !pid || infectiousPrompted.has(pid + '|' + name)) return;
  try {
    if (!infectiousDiseases.value) {
      const res = await getDiseaseSelectList('');
      infectiousDiseases.value = res.data || [];
    }
    const hit = (infectiousDiseases.value || []).filter((dz) => {
      const dn = dz.diseaseName || dz.name || '';
      return dn && (name.includes(dn) || dn.includes(name));
    });
    if (!hit.length) return;
    infectiousPrompted.add(pid + '|' + name);
    const names = hit.map((dz) => dz.diseaseName || dz.name).join('、');
    try {
      await ElMessageBox.confirm(
          `本次诊断「${name}」可能属于法定传染病：${names}。请及时填写传染病报告卡（24 小时内上报）。`,
          '传染病上报提醒',
          {confirmButtonText: '前往上报', cancelButtonText: '稍后处理', type: 'warning'}
      );
      router.push('/infectious-report');
    } catch (e) { /* 稍后处理 */
    }
  } catch (e) { /* 目录加载失败不打扰开单流程 */
  }
};

const validateRx = () => {
  const errors = [];
  flushRxDraft();
  for (const p of rxLoaded) {
    if (!p.details?.length) continue;
    const label = RX_TYPE_LABEL[p.prescriptionType] || '西药';
    if (p.prescriptionType === 3) {
      if (!p.doseCount || p.doseCount < 1 || p.doseCount > 30) errors.push(`${label}处方：请填写剂数（1~30 剂）`);
      if (p.decoctFlag !== 1 && p.decoctFlag !== 2) errors.push(`${label}处方：请选择煎服方式（代煎/自煎）`);
    }
    p.details.forEach((item, idx) => {
      const prefix = `${label}处方第${idx + 1}项`;
      if (!item.drugId) errors.push(`${prefix}：请选择药品`);
      if (!item.singleDosage) errors.push(`${prefix}：请填写${p.prescriptionType === 3 ? '每剂克数' : '单次用量'}`);
      if (!item.frequency) errors.push(`${prefix}：请填写频次/用法`);
      if (!item.route) errors.push(`${prefix}：请填写给药途径/煎法`);
      if (!item.quantity || Number(item.quantity) <= 0) errors.push(`${prefix}：请填写正确的总量/剂数`);
      if (p.prescriptionType !== 3 && (!item.duration || Number(item.duration) <= 0)) errors.push(`${prefix}：请填写天数`);
    });
  }
  return errors;
};

const buildRxPayload = () => {
  flushRxDraft();
  return rxLoaded
      .filter((p) => p.details && p.details.length > 0)
      .map((p) => ({
        prescriptionType: p.prescriptionType || 1,
        doseCount: p.doseCount,
        decoctFlag: p.decoctFlag,
        // 长处方（处方级）：关时天数置空由后端落 NO；饮片不开长处方（天数撞剂数上限）
        isLongPrescription: p.isLongPrescription === 1,
        longPrescriptionDays: p.isLongPrescription === 1 ? p.longPrescriptionDays : null,
        details: p.details.map((d) => ({
          drugId: d.drugId, drugName: d.drugName, specification: d.specification, unit: d.unit,
          singleDosage: d.singleDosage, frequency: d.frequency, route: d.route,
          quantity: d.quantity, duration: d.duration, remark: d.remark,
        })),
      }));
};

const saving = ref(false);
const rxSignVisible = ref(false);
const pendingRxCount = ref(0);
let pendingSubmitData = null;
// 软签二次确认：PIN 由签名人本人输入，服务端校验通过才解密私钥
const pinDialogRef = ref(null);
const pinDialogVisible = ref(false);

const saveAll = async () => {
  if (!current.value) return;
  const requiredFields = [
    {field: recordForm.chiefComplaint, label: '主诉'},
    {field: recordForm.presentIllness, label: '现病史'},
    {field: recordForm.allergyHistory, label: '过敏史'},
    {field: recordForm.pastHistory, label: '既往史'},
    {field: recordForm.personalHistory, label: '个人史'},
    {field: recordForm.familyHistory, label: '家族史'},
    {field: recordForm.temperature, label: '体温'},
    {field: recordForm.pulse, label: '脉搏'},
    {field: recordForm.respiration, label: '呼吸'},
    {field: recordForm.systolicPressure, label: '收缩压'},
    {field: recordForm.diastolicPressure, label: '舒张压'},
    {field: recordForm.diagnosisName || recordForm.diagnosis, label: '诊断'},
    {field: recordForm.treatmentPlan, label: '处置计划'},
  ];
  const missingFields = requiredFields.filter((item) => !item.field);
  if (missingFields.length > 0) {
    const missingListHtml = missingFields
        .map((item) => `<div class="py-1 text-red-600">✗ ${item.label}</div>`)
        .join('');
    try {
      await ElMessageBox.confirm(
          `<div class="space-y-2">
           <div class="text-sm text-slate-600">以下必填项尚未填写，请补全后再保存：</div>
           <div class="rounded-lg bg-red-50 p-3 text-sm">${missingListHtml}</div>
         </div>`,
          '病历必填项未完成',
          {dangerouslyUseHTMLString: true, confirmButtonText: '我知道了', showCancelButton: false, type: 'warning'},
      );
    } catch (e) { /* 关闭/取消均放弃保存 */
    }
    return;
  }
  rxErrors.value = validateRx();
  if (rxErrors.value.length) {
    ElMessage.warning('处方存在必填项缺失，请修正后再保存');
    return;
  }
  const data = {
    patientId: current.value.patientId,
    patientNo: current.value.patientNo,
    patientName: current.value.patientName,
    registId: current.value.registId,
    // queueId 病历/处方 DTO 强校验（VO 由 fillRegistIds 按 regist_id 关联补出），漏传结诊必崩
    queueId: current.value.queueId,
    deptId: current.value.deptId,
    deptName: current.value.deptName,
    doctorId: current.value.doctorId,
    doctorName: current.value.doctorName,
    recordId: recordForm.id || null,
    chiefComplaint: recordForm.chiefComplaint,
    presentIllness: recordForm.presentIllness,
    allergyHistory: recordForm.allergyHistory,
    pastHistory: recordForm.pastHistory,
    personalHistory: recordForm.personalHistory,
    familyHistory: recordForm.familyHistory,
    temperature: recordForm.temperature,
    pulse: recordForm.pulse,
    respiration: recordForm.respiration,
    systolicPressure: recordForm.systolicPressure,
    diastolicPressure: recordForm.diastolicPressure,
    // 结诊强校验 diagnosis（DTO 主字段）：本页诊断区只有名称/编码两个输入框，
    // 与门诊医生工作站的 ICD select 口径对齐——名称即诊断主值，别让 diagnosis 永远空串
    diagnosis: recordForm.diagnosis || recordForm.diagnosisName,
    diagnosisCode: recordForm.diagnosisCode,
    diagnosisName: recordForm.diagnosisName,
    generalCondition: recordForm.generalCondition,
    treatmentPlan: recordForm.treatmentPlan,
    prescriptions: buildRxPayload(),
  };
  saving.value = true;
  try {
    if (data.prescriptions.length > 0) {
      // 有处方：先签名板（笔迹随处方落库，电子证书签名后端自动完成），再走结诊提交
      pendingSubmitData = data;
      pendingRxCount.value = data.prescriptions.length;
      rxSignVisible.value = true;
    } else {
      await saveMedicalRecord(data);
      ElMessage.success('病历已保存');
      checkInfectiousReport();
    }
  } catch (e) { /* 全局提示 */
  } finally {
    saving.value = false;
  }
};

const onRxSigned = async (dataUrl) => {
  rxSignVisible.value = false;
  const data = pendingSubmitData;
  pendingSubmitData = null;
  if (!data) return;
  data.prescriptions = data.prescriptions.map((rx) => ({...rx, doctorSignature: dataUrl || ''}));
  saving.value = true;
  // 结诊带 silent：否决项拦截要弹框让医生选，不能让全局红条先弹一遍；其余失败提示归本函数
  try {
    // 软签二次确认：结诊这一次请求里后端要签病历+处方，弹一次 PIN 即可
    const pin = await pinDialogRef.value.askPin();
    if (!pin) {
      ElMessage.info('已取消签名，本次结诊未提交');
      return;
    }
    let res;
    try {
      res = await submitMedicalRecord({...data, pin}, {silent: true});
    } catch (e) {
      const action = await handleEmrSubmitVeto(e);
      if (action === 'cancel') return;
      if (action !== 'force') throw e;
      // 驳回后重试的是同一次结诊，复用同一个 PIN
      res = await submitMedicalRecord({...data, forceSubmit: true, pin}, {silent: true});
    }
    if (res.data) recordForm.id = res.data;
    ElMessage.success('已保存并提交（处方已签名）');
    checkInfectiousReport();
    if (current.value?.registId) {
      const rxRes = await getPrescriptionList({patientId: current.value.patientId, registId: current.value.registId});
      rxLoaded.length = 0;
      rxLoaded.push(...(rxRes.data || []));
      bindRxDraft();
    }
  } catch (e) {
    ElMessage.error(String(e?.message || '结诊失败'));
  } finally {
    saving.value = false;
  }
};

// ---------- 轮询 ----------
onMounted(() => {
  reloadQueue();
  loadWards();
  loadDiagTpls();
  pollTimer = setInterval(reloadQueue, 30000);
});
onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer);
});
</script>

<style scoped>
/* 表格内紧凑编辑 */
:deep(.el-table .el-select),
:deep(.el-table .el-input) {
  width: 100%;
}

/* 急诊叫号板：与门诊医生站同款灯/号码样式（门诊侧为 scoped 私有，这里按口径复刻） */
.call-no {
  font-size: 38px;
  font-weight: 800;
  letter-spacing: 0.5px;
  font-variant-numeric: tabular-nums;
}

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

.call-light.is-idle {
  background: #94a3b8;
}

/* tabs 撑满中间卡：header 定高、内容区滚动 */
.emg-doc-tabs {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.emg-doc-tabs :deep(.el-tabs__header) {
  flex-shrink: 0;
  margin-bottom: 0;
  padding: 0 12px;
}

/* 2026-10-28 与门诊左侧导航视觉统一：选中加粗（门诊 recordNavTabs font-bold）、
   指示条 2.5px 圆角（模拟门诊 inkbar 存在感）、底部分隔线 slate-200 同色 */
.emg-doc-tabs :deep(.el-tabs__item) {
  font-weight: 500;
}

.emg-doc-tabs :deep(.el-tabs__item.is-active) {
  font-weight: 700;
}

.emg-doc-tabs :deep(.el-tabs__active-bar) {
  height: 2.5px;
  border-radius: 2px;
}

.emg-doc-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #e2e8f0;
}

.emg-doc-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow: auto;
  padding: 12px;
}
</style>
