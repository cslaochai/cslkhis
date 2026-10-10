<template>
  <div class="space-y-6">
    <!-- 页头 -->
    <div class="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
      <div class="flex items-center gap-3">
        <el-select
            v-model="admissionId"
            class="!w-80"
            data-testid="p2-admission-select"
            filterable
            placeholder="选择在院患者"
            @change="handleAdmissionChange"
        >
          <el-option v-for="a in admissions" :key="a.admissionId" :label="patientLabel(a)"
                     :value="String(a.admissionId)"/>
        </el-select>
        <el-button :icon="Refresh" @click="reloadAll">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div
          v-for="s in statCards"
          :key="s.label"
          class="flex items-center gap-3 rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div :class="s.bg" class="flex h-11 w-11 shrink-0 items-center justify-center rounded-lg">
          <el-icon :class="s.color" class="h-5 w-5">
            <component :is="s.icon"/>
          </el-icon>
        </div>
        <div class="min-w-0">
          <p class="truncate text-xs text-slate-500">{{ s.label }}</p>
          <p class="text-lg font-bold text-slate-900">{{ s.value }}</p>
          <p class="truncate text-[11px] text-slate-400">{{ s.hint }}</p>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <!-- ============== 病历列表 ============== -->
      <el-tab-pane label="病历列表" name="records">
        <div class="mb-3 flex flex-wrap items-center gap-3">
          <el-input
              v-model="query.keyword"
              :prefix-icon="Search"
              class="!w-64"
              clearable
              placeholder="搜索文书号 / 标题 / 主诉 / 诊断"
              @keyup.enter="loadRecords"
          />
          <el-select v-model="query.recordType" class="!w-36" clearable placeholder="文书类型">
            <el-option v-for="t in typeOptions" :key="t.code" :label="t.label" :value="t.code"/>
          </el-select>
          <el-select v-model="query.recordStatus" class="!w-32" clearable placeholder="文书状态">
            <el-option v-for="t in statusOptions" :key="t.code" :label="t.label" :value="t.code"/>
          </el-select>
          <el-button type="primary" @click="loadRecords">查询</el-button>
          <el-button v-perm="'ipd:record:add'" :icon="Plus" class="!ml-auto" data-testid="p2-open-record" type="primary"
                     @click="openCreate">
            新建病历文书
          </el-button>
        </div>

        <el-table v-loading="loading" :data="rows" border data-testid="p2-record-table" style="width: 100%">
          <el-table-column label="文书号" prop="recordNo" width="150"/>
          <el-table-column label="类型" width="110">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ row.recordTypeText }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="标题 / 主诉" min-width="220">
            <template #default="{ row }">
              <div class="text-sm font-medium text-slate-800">{{ row.recordTitle || row.recordTypeText || '—' }}</div>
              <div class="truncate text-xs text-slate-400">{{ row.chiefComplaint || '—' }}</div>
            </template>
          </el-table-column>
          <el-table-column label="诊断" min-width="160">
            <template #default="{ row }">
              <span class="text-xs text-slate-600">{{ row.diagnosisName || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="结构化率" width="150">
            <template #default="{ row }">
              <div :style="{ color: rateColor(row.structuredRate) }" class="text-sm font-medium">
                {{ row.structuredRateText }}
              </div>
              <div class="text-xs text-slate-400">{{ row.structuredFilled ?? 0 }}/{{ row.structuredTotal ?? 0 }} 要素
              </div>
            </template>
          </el-table-column>
          <el-table-column label="缺项" min-width="180">
            <template #default="{ row }">
              <span v-if="row.missingLabels && row.missingLabels.length" class="text-xs text-rose-500">
                {{ row.missingLabels.slice(0, 4).join('、') }}{{ row.missingLabels.length > 4 ? ' …' : '' }}
              </span>
              <span v-else class="text-xs text-emerald-600">无缺失</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.recordStatus)" size="small">{{ row.recordStatusText }}</el-tag>
              <div v-if="row.archiveByName" class="mt-0.5 text-xs text-slate-400">归档 {{ row.archiveByName }}</div>
            </template>
          </el-table-column>
          <el-table-column label="签名" width="130">
            <template #default="{ row }">
              <!-- 三态分开显示：0-未签名 / 1-已签名 / 2-签名已失效。
                   「2-签名已失效」绝不能回落成「未签名」—— "有人作废过签名"和"从来没签过"
                   是两件完全不同的事实（P5.5 口径）。码值未知时如实显示未知(n)，不猜。 -->
              <el-tag v-if="row.signStatus === 1" :data-testid="`p5-sign-tag-${row.recordNo}`" size="small"
                      type="success">已签名
              </el-tag>
              <el-tag v-else-if="row.signStatus === 2" :data-testid="`p5-sign-tag-${row.recordNo}`" size="small"
                      type="warning">签名已失效
              </el-tag>
              <el-tag v-else-if="row.signStatus === 0" :data-testid="`p5-sign-tag-${row.recordNo}`" effect="plain" size="small"
                      type="info">未签名
              </el-tag>
              <span v-else class="text-xs text-slate-400">未知({{ row.signStatus }})</span>
              <div v-if="row.signedTime" class="mt-0.5 text-xs text-slate-400">{{ fmtTime(row.signedTime) }}</div>
              <div v-else-if="row.signStatus === 0 && row.recordStatus === 3" class="mt-0.5 text-xs text-slate-400">
                存量文书，不补签
              </div>
            </template>
          </el-table-column>
          <el-table-column label="书写 / 时间" width="150">
            <template #default="{ row }">
              <div class="text-xs text-slate-600">{{ row.doctorName || '—' }}</div>
              <div class="text-xs text-slate-400">{{ fmtTime(row.recordTime) }}</div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="240">
            <template #default="{ row }">
              <el-button v-if="row.canEdit" v-perm="'ipd:record:edit'" link size="small" type="primary"
                         @click="openEdit(row)">编辑
              </el-button>
              <el-button link size="small" type="info" @click="openDetail(row)">要素明细</el-button>
              <el-button v-if="row.canSubmit" v-perm="'ipd:record:edit'" data-testid="p2-submit-btn" link size="small"
                         type="warning" @click="doSubmit(row)">提交
              </el-button>
              <el-button v-if="row.canArchive" v-perm="'ipd:record:edit'" data-testid="p2-archive-btn" link size="small"
                         type="success" @click="doArchive(row)">归档
              </el-button>
              <span v-if="!row.canEdit && !row.canSubmit && !row.canArchive"
                    class="text-xs text-slate-400">已封存</span>
            </template>
          </el-table-column>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">该患者暂无病历文书，点右上「新建病历文书」</div>
          </template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="pagination.pageNum"
              v-model:page-size="pagination.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="total"
              layout="total, sizes, prev, pager, next"
              @size-change="loadRecords"
              @current-change="loadRecords"
          />
        </div>
      </el-tab-pane>

      <!-- ============== 修改留痕 ============== -->
      <el-tab-pane label="修改留痕" name="logs">
        <el-table v-loading="logLoading" :data="logs" border data-testid="p2-log-table" style="width: 100%">
          <el-table-column label="时间" width="150">
            <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="文书号" prop="recordNo" width="150"/>
          <el-table-column label="操作" prop="operation" width="100">
            <template #default="{ row }">
              <el-tag effect="plain" size="small">{{ row.operation }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="字段" prop="fieldLabel" width="140"/>
          <el-table-column label="变更前" min-width="200">
            <template #default="{ row }">
              <span class="text-xs text-slate-500">{{ row.oldValue || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="变更后" min-width="200">
            <template #default="{ row }">
              <span class="text-xs text-slate-800">{{ row.newValue || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作人" prop="userName" width="120"/>
          <template #empty>
            <div class="py-6 text-sm text-slate-400">还没有修改留痕（逐字段 diff，值真变才写）</div>
          </template>
        </el-table>
        <div class="mt-3 flex justify-end">
          <el-pagination
              v-model:current-page="logPagination.pageNum"
              v-model:page-size="logPagination.pageSize"
              :page-sizes="PAGE_SIZES"
              :total="logTotal"
              layout="total, sizes, prev, pager, next"
              @size-change="loadLogs"
              @current-change="loadLogs"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ============== 新建 / 编辑 ============== -->
    <el-dialog
        v-model="dialog"
        :title="editingId ? '编辑病历文书（传什么覆盖什么，变更逐字段留痕）' : '新建病历文书（结构化要素）'"
        top="4vh"
        width="72%"
    >
      <el-scrollbar max-height="62vh">
        <el-form class="pr-2" label-width="110px">
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="文书类型" required>
                <el-select
                    v-model="form.recordType"
                    :disabled="!!editingId"
                    class="!w-full"
                    data-testid="p2-form-type"
                >
                  <el-option v-for="t in recordTypeOptions" :key="t.code" :label="t.label" :value="t.code"/>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="文书标题">
                <el-input v-model="form.recordTitle" placeholder="留空则取类型文案"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="记录时间">
                <el-date-picker
                    v-model="form.recordTime"
                    class="!w-full"
                    placeholder="留空取当前时间"
                    type="datetime"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- AI 抽取：产出是候选值，必须医生点「填入」 -->
          <el-card class="mb-4" shadow="never">
            <template #header>
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium">AI 结构化抽取（不写库，产出只是候选值）</span>
                <el-button :loading="aiLoading" data-testid="p2-ai-extract" size="small" type="primary"
                           @click="extractByAi">
                  抽取
                </el-button>
              </div>
            </template>
            <el-input
                v-model="aiRaw"
                :rows="3"
                data-testid="p2-ai-text"
                placeholder="粘贴外院病历 / 口述转写文本，如：主诉：咳嗽发热3天。现病史：…… 既往史：……"
                type="textarea"
            />
            <div v-if="aiNote" class="mt-2 text-xs text-emerald-600">{{ aiNote }}</div>
            <div v-if="aiCandidates.length" class="mt-3 space-y-2">
              <div
                  v-for="c in aiCandidates"
                  :key="c.field"
                  class="flex items-start gap-3 rounded border border-slate-200 bg-slate-50 p-2"
              >
                <span class="w-20 shrink-0 text-xs text-slate-500">{{ c.fieldLabel }}</span>
                <span class="flex-1 text-xs text-slate-700">{{ c.value }}</span>
                <el-tag effect="plain" size="small">{{ c.source === 'HARD_RULE' ? '原文切分' : '模型搬运' }}</el-tag>
                <el-button link size="small" type="primary" @click="applyCandidate(c)">填入</el-button>
              </div>
            </div>
          </el-card>

          <!-- 病史 -->
          <div class="mb-1 text-sm font-semibold text-slate-700">病史要素</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="主诉">
                <el-input v-model="form.chiefComplaint" data-testid="p2-chief-complaint" placeholder="症状 + 持续时间"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="过敏史">
                <el-input v-model="form.allergyHistory" placeholder="无过敏史请写「否认」"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="现病史">
            <el-input v-model="form.presentIllness" :rows="2" type="textarea"/>
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="8">
              <el-form-item label="既往史">
                <el-input v-model="form.pastHistory" :rows="2" type="textarea"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="个人史">
                <el-input v-model="form.personalHistory" :rows="2" type="textarea"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item label="家族史">
                <el-input v-model="form.familyHistory" :rows="2" type="textarea"/>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 生命体征（数值列） -->
          <div class="mb-1 text-sm font-semibold text-slate-700">生命体征（数值列，0 不是空值）</div>
          <el-row :gutter="16">
            <el-col :span="4">
              <el-form-item label="体温℃">
                <el-input-number v-model="form.temperature" :max="43" :min="34" :precision="1" :step="0.1"
                                 class="!w-full" controls-position="right"/>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="脉搏">
                <el-input-number v-model="form.pulse" :max="250" :min="20" class="!w-full" controls-position="right"/>
              </el-form-item>
            </el-col>
            <el-col :span="4">
              <el-form-item label="呼吸">
                <el-input-number v-model="form.respiration" :max="80" :min="5" class="!w-full"
                                 controls-position="right"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="收缩压">
                <el-input-number v-model="form.systolicPressure" :max="300" :min="40" class="!w-full"
                                 controls-position="right"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="舒张压">
                <el-input-number v-model="form.diastolicPressure" :max="200" :min="20" class="!w-full"
                                 controls-position="right"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="6">
              <el-form-item label="身高cm">
                <el-input-number v-model="form.height" :max="250" :min="30" :precision="1" class="!w-full"
                                 controls-position="right"/>
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="体重kg">
                <el-input-number v-model="form.weight" :max="300" :min="1" :precision="1" class="!w-full"
                                 controls-position="right"/>
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 体格检查 -->
          <div class="mb-1 text-sm font-semibold text-slate-700">体格检查（按系统拆列，缺哪一项算得出来）</div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="一般情况">
                <el-input v-model="form.generalCondition"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="皮肤黏膜">
                <el-input v-model="form.skinMucosa"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="头颈部">
                <el-input v-model="form.headNeck"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="胸部及肺">
                <el-input v-model="form.chestLung"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="心脏">
                <el-input v-model="form.heart"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="腹部">
                <el-input v-model="form.abdomen" data-testid="p2-abdomen"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="脊柱四肢">
                <el-input v-model="form.spineLimbs"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="神经系统">
                <el-input v-model="form.nervousSystem"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="专科检查">
            <el-input v-model="form.specialistExam" :rows="2" type="textarea"/>
          </el-form-item>

          <!-- 结论 -->
          <div class="mb-1 text-sm font-semibold text-slate-700">诊疗过程与结论</div>
          <el-form-item label="辅助检查">
            <el-input v-model="form.auxiliaryExam" :rows="2" type="textarea"/>
          </el-form-item>
          <el-row :gutter="16">
            <el-col :span="14">
              <el-form-item label="诊断编码（ICD-10）">
                <!-- 只能从码表里选：编码是病案首页/DRG/医保结算的上游依据，
                     放开自由输入等于把「码写错」变成无人可查的静默错误。
                     但**不**禁止显示历史值：存量数据里 J18.900 等码已不在码表，
                     el-select 在无匹配项时会原样显示该值，医生据此决定改不改。 -->
                <el-select
                    v-model="icdCodes"
                    :loading="icdLoading"
                    :remote-method="searchIcdOptions"
                    class="!w-full"
                    clearable
                    data-testid="p2-diagnosis"
                    filterable
                    multiple
                    placeholder="输入疾病名称或 ICD 编码检索，可多选"
                    remote
                    reserve-keyword
                    @change="syncDiagnosisText"
                >
                  <el-option
                      v-for="o in icdOptions"
                      :key="o.icdCode"
                      :label="`${o.icdCode} ${o.icdName}`"
                      :value="o.icdCode"
                  >
                    <span class="font-mono text-xs text-slate-500">{{ o.icdCode }}</span>
                    <span class="ml-2 text-slate-800">{{ o.icdName }}</span>
                    <span v-if="o.icdCategory" class="ml-2 text-xs text-slate-400">{{ o.icdCategory }}</span>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="10">
              <el-form-item label="诊断名称">
                <el-input
                    v-model="form.diagnosisName"
                    data-testid="p2-diagnosis-name"
                    placeholder="选中编码后自动带出，可再补充"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="诊疗计划">
            <el-input v-model="form.treatmentPlan" :rows="2" type="textarea"/>
          </el-form-item>
          <el-form-item label="病程正文">
            <el-input v-model="form.courseNote" :rows="2" placeholder="病程类文书填这里" type="textarea"/>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" data-testid="p2-remark"/>
          </el-form-item>
        </el-form>
      </el-scrollbar>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button v-perm="['ipd:record:add','ipd:record:edit']" :loading="saving" data-testid="p2-submit-record"
                   type="primary" @click="submitRecord">
          {{ editingId ? '保存修改' : '创建文书' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============== 要素明细 ============== -->
    <el-dialog v-model="detailDialog" title="结构化要素明细" width="60%">
      <div class="mb-3 text-sm text-slate-600">
        {{ detail.patientName || '—' }} · {{ detail.recordTypeText || '—' }} · {{ detail.recordNo || '—' }}
        <span :style="{ color: rateColor(detail.structuredRate) }" class="ml-3 font-semibold">
          结构化率 {{ detail.structuredRateText || '—' }}（{{
            detail.structuredFilled ?? 0
          }}/{{ detail.structuredTotal ?? 0 }}）
        </span>
        <el-tag
            :data-testid="`p5-sign-detail-tag-${detail.recordNo}`"
            :type="detail.signStatus === 1 ? 'success' : detail.signStatus === 2 ? 'warning' : 'info'"
            class="ml-3"
            size="small"
        >{{ detail.signStatusText || ('未知(' + detail.signStatus + ')') }}
        </el-tag>
      </div>
      <!-- 锁提示由后端给：按钮能不能按、为什么不能按，前端不自判状态（同一口径只维护一处） -->
      <div
          v-if="detail.signLockHint"
          class="mb-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-xs leading-5 text-amber-900"
          data-testid="p5-sign-lock-hint"
      >{{ detail.signLockHint }}
      </div>
      <el-table :data="detailElements" border data-testid="p2-element-table" max-height="50vh" style="width: 100%">
        <el-table-column label="分组" prop="groupLabel" width="140"/>
        <el-table-column label="要素" prop="label" width="140"/>
        <el-table-column align="center" label="是否已填" width="110">
          <template #default="{ row }">
            <el-tag :type="row.filled ? 'success' : 'danger'" size="small">{{ row.filled ? '已填' : '缺失' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="当前值" min-width="200">
          <template #default="{ row }">
            <span class="text-xs text-slate-700">{{ num(row.value) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {DataLine, Document, Plus, Refresh, Search, Warning} from '@element-plus/icons-vue';
import {getInpatientListPage} from '@/api/inpatient';
import {
  archiveInpatientRecord,
  getInpatientRecordDetail,
  getInpatientRecordListPage,
  getInpatientRecordLogs,
  getInpatientRecordStatusOptions,
  getInpatientRecordTypeOptions,
  getRecordQualityStat,
  saveInpatientRecord,
  submitInpatientRecord,
} from '@/api/inpatientRecord';
import {extractEmrText} from '@/api/ai';
import {searchIcd10} from '@/api/system';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
// ---------------- 基础数据 ----------------
const admissions = ref([]);
const admissionId = ref('');
const typeOptions = ref([]);
const statusOptions = ref([]);
const fmtTime = (v) => (v ? String(v).replace('T', ' ').slice(0, 16) : '—');
/** 数值 0 必须显示成 0，不能当空 */
const num = (v) => (v === null || v === undefined || v === '' ? '—' : String(v));
const patientLabel = (a) => `${a.bedNo || '—'} ${a.patientName || '—'}（${a.wardName || a.deptName || '—'}）`;
const loadAdmissions = async () => {
  try {
    const res = await getInpatientListPage({admitStatus: 1, pageNum: 1, pageSize: 200});
    admissions.value = (res.data?.records || []);
    if (!admissionId.value && admissions.value.length > 0) {
      admissionId.value = String(admissions.value[0].admissionId);
    }
  } catch (error) {
    console.error('加载在院患者失败:', error);
  }
};
// ---------------- 列表 ----------------
const activeTab = ref('records');
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const query = ref({recordType: null, recordStatus: null, keyword: ''});
const stat = ref({});
const loadRecords = async () => {
  loading.value = true;
  try {
    const res = await getInpatientRecordListPage({
      admissionId: admissionId.value || undefined,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
      recordType: query.value.recordType ?? undefined,
      recordStatus: query.value.recordStatus ?? undefined,
      keyword: query.value.keyword || undefined,
    });
    rows.value = (res.data?.records || []);
    total.value = Number(res.data?.total ?? 0);
  } catch (error) {
    ElMessage.error(error.message || '加载病历列表失败');
  } finally {
    loading.value = false;
  }
};
const loadStat = async () => {
  try {
    const res = await getRecordQualityStat(admissionId.value);
    stat.value = (res.data || {});
  } catch (error) {
    stat.value = {};
  }
};
// ---------------- 修改留痕 ----------------
const logLoading = ref(false);
const logs = ref([]);
const logTotal = ref(0);
const logPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadLogs = async () => {
  logLoading.value = true;
  try {
    const res = await getInpatientRecordLogs({
      admissionId: admissionId.value || undefined,
      pageNum: logPagination.value.pageNum,
      pageSize: logPagination.value.pageSize,
    });
    logs.value = (res.data?.records || []);
    logTotal.value = Number(res.data?.total ?? 0);
  } catch (error) {
    ElMessage.error(error.message || '加载修改留痕失败');
  } finally {
    logLoading.value = false;
  }
};
const statCards = computed(() => [
  {
    label: '结构化率',
    value: stat.value.structuredRateText || '—',
    hint: `${stat.value.elementFilled ?? 0}/${stat.value.elementTotal ?? 0} 个要素`,
    color: 'text-emerald-600',
    bg: 'bg-emerald-50',
    icon: DataLine
  },
  {
    label: '文书份数',
    value: stat.value.recordCount ?? 0,
    hint: '含草稿与已归档',
    color: 'text-blue-600',
    bg: 'bg-blue-50',
    icon: Document
  },
  {
    label: '列表条数',
    value: total.value,
    hint: '当前筛选条件',
    color: 'text-slate-600',
    bg: 'bg-slate-100',
    icon: Search
  },
  {
    label: '修改留痕',
    value: logTotal.value,
    hint: '逐字段 diff，值真变才写',
    color: 'text-amber-600',
    bg: 'bg-amber-50',
    icon: Warning
  },
]);
// ---------------- 新建 / 编辑 ----------------
const dialog = ref(false);
const saving = ref(false);
const editingId = ref('');
const recordTypeOptions = ref([]);
const emptyForm = () => ({
  recordType: 1, recordTitle: '', recordTime: '',
  chiefComplaint: '', presentIllness: '', pastHistory: '', personalHistory: '', familyHistory: '', allergyHistory: '',
  temperature: undefined, pulse: undefined, respiration: undefined,
  systolicPressure: undefined, diastolicPressure: undefined, height: undefined, weight: undefined,
  generalCondition: '', skinMucosa: '', headNeck: '', chestLung: '', heart: '', abdomen: '',
  spineLimbs: '', nervousSystem: '', specialistExam: '',
  auxiliaryExam: '', diagnosisName: '', diagnosisCode: '', treatmentPlan: '', courseNote: '', remark: '',
});
const form = ref(emptyForm());
const openCreate = () => {
  if (!admissionId.value) {
    ElMessage.warning('请先选择在院患者');
    return;
  }
  editingId.value = '';
  form.value = emptyForm();
  icdCodes.value = [];
  icdOptions.value = [];
  icdNameOf.clear();
  aiCandidates.value = [];
  aiRaw.value = '';
  aiNote.value = '';
  dialog.value = true;
};
const openEdit = async (row) => {
  try {
    const res = await getInpatientRecordDetail(row.id);
    const d = res.data || {};
    editingId.value = row.id;
    form.value = {
      recordType: d.recordType ?? 1,
      recordTitle: d.recordTitle ?? '',
      recordTime: (d.recordTime || '').replace(' ', 'T'),
      chiefComplaint: d.chiefComplaint ?? '',
      presentIllness: d.presentIllness ?? '',
      pastHistory: d.pastHistory ?? '',
      personalHistory: d.personalHistory ?? '',
      familyHistory: d.familyHistory ?? '',
      allergyHistory: d.allergyHistory ?? '',
      temperature: d.temperature ?? undefined,
      pulse: d.pulse ?? undefined,
      respiration: d.respiration ?? undefined,
      systolicPressure: d.systolicPressure ?? undefined,
      diastolicPressure: d.diastolicPressure ?? undefined,
      height: d.height ?? undefined,
      weight: d.weight ?? undefined,
      generalCondition: d.generalCondition ?? '',
      skinMucosa: d.skinMucosa ?? '',
      headNeck: d.headNeck ?? '',
      chestLung: d.chestLung ?? '',
      heart: d.heart ?? '',
      abdomen: d.abdomen ?? '',
      spineLimbs: d.spineLimbs ?? '',
      nervousSystem: d.nervousSystem ?? '',
      specialistExam: d.specialistExam ?? '',
      auxiliaryExam: d.auxiliaryExam ?? '',
      diagnosisName: d.diagnosisName ?? '',
      diagnosisCode: d.diagnosisCode ?? '',
      treatmentPlan: d.treatmentPlan ?? '',
      courseNote: d.courseNote ?? '',
      remark: d.remark ?? '',
    };
    seedDiagnosis(d.diagnosisCode, d.diagnosisName);
    aiCandidates.value = [];
    aiRaw.value = '';
    aiNote.value = '';
    dialog.value = true;
  } catch (error) {
    ElMessage.error(error.message || '加载病历详情失败');
  }
};
// ---------------- ICD-10 诊断选码（G3） ----------------
/**
 * 诊断为什么要用选码器而不是两个手输框：
 * 手输时医生凭记忆敲编码，敲错/敲简写都不会有人发现，而 `diagnosis_code` 是
 * 病案首页、DRG 分组、医保结算的上游依据 —— 错一个码下游全错，且**零报错**。
 *
 * 两个必须说清的现状（实测 2026-09-23）：
 * 1. 码表已从演示期 35 条扩到 **40477 条**，检索必须按相关性排序，
 *    否则输入「肺炎」首条会返回「A01.005+J17.0* 伤寒并发肺炎」（后端已修）。
 * 2. 存量数据里有**码表里查不到的编码**（`J18.900` / `J45.900` / `O14.900` /
 *    `K29.500` / `J20.900`）。所以选码器**不能**把不在选项里的值当空值丢掉 ——
 *    打开老病历时必须原样把编码显示出来，医生改不改由他决定。
 */
const icdOptions = ref([]);
const icdLoading = ref(false);
/** 本次选中的编码（`diagnosis_code` 是分号分隔的多值列，这里用数组承载） */
const icdCodes = ref([]);
/** 编码 → 名称缓存：含历史数据回填，避免改码后把解析不出的诊断名写空 */
const icdNameOf = new Map();
/** 请求序号：远程搜索是异步的，慢响应回来晚了不能覆盖新结果 */
let icdSeq = 0;
const searchIcdOptions = async (query) => {
  const kw = (query || '').trim();
  if (!kw) {
    icdOptions.value = [];
    return;
  }
  const seq = ++icdSeq;
  icdLoading.value = true;
  try {
    const res = await searchIcd10(kw);
    if (seq !== icdSeq)
      return;
    const seen = new Set();
    icdOptions.value = (res.data || [])
        .filter(o => o?.icdCode && !seen.has(o.icdCode) && seen.add(o.icdCode))
        .map(o => ({icdCode: o.icdCode, icdName: o.icdName, icdCategory: o.icdCategory}));
    icdOptions.value.forEach(o => icdNameOf.set(o.icdCode, o.icdName));
  } catch (error) {
    if (seq !== icdSeq)
      return;
    icdOptions.value = [];
    console.error('检索 ICD 编码失败:', error);
  } finally {
    if (seq === icdSeq)
      icdLoading.value = false;
  }
};
/** 打开表单时把「分号分隔的双列」还原成编码数组，并用历史名称回填缓存 */
const seedDiagnosis = (codeStr, nameStr) => {
  const codes = String(codeStr || '').split(';').map(s => s.trim()).filter(Boolean);
  const names = String(nameStr || '').split(';').map(s => s.trim());
  icdCodes.value = codes;
  if (codes.length > 0 && codes.length === names.length) {
    codes.forEach((code, i) => {
      if (names[i])
        icdNameOf.set(code, names[i]);
    });
  }
};
/**
 * 选码后回写两个文本列。
 * 名称只在**每个编码都能解析出名称**时才整体重写：只要有解析不出的（老码），
 * 就保留原文本不动 —— 宁可名称没跟着更新，也不能把诊断名写空。
 */
const syncDiagnosisText = () => {
  const codes = icdCodes.value.filter(Boolean);
  form.value.diagnosisCode = codes.join(';');
  const names = codes.map(code => icdNameOf.get(code));
  if (names.length > 0 && names.every(n => n)) {
    form.value.diagnosisName = names.join(';');
  }
};
// ---------------- AI 抽取（候选值，不自动填表） ----------------
const aiRaw = ref('');
const aiLoading = ref(false);
const aiNote = ref('');
const aiCandidates = ref([]);
/** 只认这份白名单：模型给出的其它字段一律不进表单（防"顺手多填"） */
const AI_FIELD_MAP = {
  chiefComplaint: 'chiefComplaint',
  presentIllness: 'presentIllness',
  pastHistory: 'pastHistory',
  personalHistory: 'personalHistory',
  familyHistory: 'familyHistory',
  allergyHistory: 'allergyHistory',
  generalCondition: 'generalCondition',
  skinMucosa: 'skinMucosa',
  headNeck: 'headNeck',
  chestLung: 'chestLung',
  heart: 'heart',
  abdomen: 'abdomen',
  spineLimbs: 'spineLimbs',
  nervousSystem: 'nervousSystem',
  specialistExam: 'specialistExam',
  auxiliaryExam: 'auxiliaryExam',
  treatmentPlan: 'treatmentPlan',
};
const extractByAi = async () => {
  if (!aiRaw.value.trim()) {
    ElMessage.warning('请先粘贴待抽取的病历文本');
    return;
  }
  aiLoading.value = true;
  aiNote.value = '';
  try {
    const res = await extractEmrText({rawText: aiRaw.value});
    const data = res.data || {};
    aiCandidates.value = (data.fields || []).filter((f) => AI_FIELD_MAP[f.field]);
    const rejected = Number(data.rejectedCount ?? 0);
    aiNote.value = rejected > 0
        ? `抽取完成，${rejected} 条被判为「原文中找不到依据 / 字段不可写」已丢弃`
        : '抽取完成，请逐条核对后点「填入」';
  } catch (error) {
    ElMessage.error(error.message || '病历文本抽取失败');
  } finally {
    aiLoading.value = false;
  }
};
const applyCandidate = (c) => {
  const key = AI_FIELD_MAP[c.field];
  if (!key)
    return;
  form.value[key] = c.value;
  ElMessage.success(`已填入「${c.field}」（仍需医生核对）`);
};
// ---------------- 保存 / 提交 / 归档 ----------------
const submitRecord = async () => {
  if (!admissionId.value) {
    ElMessage.warning('请先选择在院患者');
    return;
  }
  saving.value = true;
  try {
    const payload = {...form.value, admissionId: admissionId.value};
    if (editingId.value) {
      payload.id = editingId.value;
    }
    // 「传什么覆盖什么」：空字符串保持空串（置空是有意的动作，服务层会留痕）
    await saveInpatientRecord(payload);
    ElMessage.success(editingId.value ? '病历已修改（变更已逐字段留痕）' : '病历文书已创建');
    dialog.value = false;
    await reloadAll();
  } catch (error) {
    ElMessage.error(error.message || '保存病历失败');
  } finally {
    saving.value = false;
  }
};
const doSubmit = async (row) => {
  try {
    const res = await submitInpatientRecord({ids: [row.id]});
    ElMessage.success(`已提交 ${res.data} 份病历`);
    await reloadAll();
  } catch (error) {
    ElMessage.error(error.message || '提交失败');
  }
};
const doArchive = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`归档 ${row.recordNo}（${row.recordTypeText || ''}）。归档是单向门，归档后禁止修改：`, '归档病历文书', {
      inputPlaceholder: '如：已送病案室，2026-09 批次',
      confirmButtonText: '确认归档',
      cancelButtonText: '取消'
    });
    if (!value) {
      ElMessage.warning('归档说明必填');
      return;
    }
    const res = await archiveInpatientRecord({ids: [row.id], remark: value});
    ElMessage.success(`已归档 ${res.data} 份病历`);
    await reloadAll();
  } catch (error) {
    if (error === 'cancel' || error === 'close')
      return;
    ElMessage.error(error.message || '归档失败');
  }
};
// ---------------- 详情（要素明细） ----------------
const detailDialog = ref(false);
const detail = ref({});
const detailElements = ref([]);
const openDetail = async (row) => {
  try {
    const res = await getInpatientRecordDetail(row.id);
    detail.value = res.data || {};
    detailElements.value = (res.data?.elements || []);
    detailDialog.value = true;
  } catch (error) {
    ElMessage.error(error.message || '加载病历详情失败');
  }
};
const rateColor = (rate) => {
  if (rate === undefined || rate === null)
    return '#94a3b8';
  if (rate >= 80)
    return '#16a34a';
  if (rate >= 60)
    return '#f59e0b';
  return '#ef4444';
};
const statusTagType = (s) => {
  if (s === 1)
    return 'info';
  if (s === 2)
    return 'warning';
  if (s === 3)
    return 'success';
  return 'info';
};
// ---------------- 刷新 ----------------
const reloadAll = async () => {
  await Promise.all([loadRecords(), loadLogs(), loadStat()]);
};
const handleAdmissionChange = async () => {
  pagination.value.pageNum = 1;
  logPagination.value.pageNum = 1;
  await reloadAll();
};
onMounted(async () => {
  try {
    const [t, s] = await Promise.all([getInpatientRecordTypeOptions(), getInpatientRecordStatusOptions()]);
    typeOptions.value = (t.data || []);
    statusOptions.value = (s.data || []);
    recordTypeOptions.value = typeOptions.value;
  } catch (error) {
    console.error('加载下拉失败:', error);
  }
  await loadAdmissions();
  await reloadAll();
});
</script>
