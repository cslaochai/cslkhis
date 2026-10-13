<template>
  <div class="log-view">
    <el-card class="stat-card" shadow="never">
      <div class="stat-grid" data-testid="log-stat">
        <div v-for="c in statCards" :key="c.key" class="stat-item">
          <div class="stat-label">{{ c.label }}</div>
          <div :data-testid="`stat-${c.key}`" class="stat-value">{{ c.value }}</div>
          <div class="stat-sub">{{ c.sub }}</div>
        </div>
      </div>
      <el-alert
          v-if="riskyAccounts.length"
          :closable="false"
          class="risk-alert"
          data-testid="log-risk-alert"
          show-icon
          type="warning"
      >
        <template #title>
          近 24 小时登录失败 ≥ 5 次的账号（口令爆破嫌疑）：
          <span v-for="a in riskyAccounts" :key="a.userName" class="risk-item">
            {{ a.userName }}（{{ a.failCount }} 次，最后 {{ a.lastFailTime }}）
          </span>
        </template>
      </el-alert>
    </el-card>

    <el-card shadow="never">
      <el-tabs v-model="activeTab" data-testid="log-tabs" @tab-change="onTabChange">
        <!-- 操作日志 -->
        <el-tab-pane label="操作日志" name="oper">
          <div class="filter-bar">
            <el-input v-model="operQuery.keyword" clearable placeholder="URL / 方法名 / IP" style="width: 200px"/>
            <el-input v-model="operQuery.operator" clearable placeholder="操作人" style="width: 140px"/>
            <el-select v-model="operQuery.businessType" clearable placeholder="业务类型" style="width: 130px">
              <el-option v-for="o in OPER_BUSINESS_TYPE" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-select v-model="operQuery.status" clearable placeholder="状态" style="width: 110px">
              <el-option v-for="o in OPER_STATUS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <div data-testid="log-date-range">
              <el-date-picker
                  v-model="dateRange"
                  end-placeholder="结束日期"
                  start-placeholder="开始日期"
                  style="width: 240px"
                  type="daterange"
                  value-format="YYYY-MM-DD"
              />
            </div>
            <el-button type="primary" @click="operQuery.pageNum = 1; load()">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
            <el-button v-perm="'system:log:export'" :loading="exporting" data-testid="btn-export" @click="doExport">
              导出 CSV
            </el-button>
          </div>

          <el-table
              v-loading="loading"
              :data="operRows"
              border
              data-testid="oper-log-table"
              size="small"
              stripe
          >
            <el-table-column label="时间" prop="operTime" width="160"/>
            <el-table-column label="模块" prop="title" show-overflow-tooltip width="120"/>
            <el-table-column label="业务类型" width="90">
              <template #default="{ row }">{{ row.businessTypeText }}</template>
            </el-table-column>
            <el-table-column label="方法" prop="method" show-overflow-tooltip width="150"/>
            <el-table-column label="方式" prop="requestMethod" width="70"/>
            <el-table-column label="操作人" prop="operName" width="100"/>
            <el-table-column label="科室" prop="deptName" width="110"/>
            <el-table-column label="URL" min-width="180" prop="operUrl" show-overflow-tooltip/>
            <el-table-column label="IP" prop="operIp" width="130"/>
            <el-table-column label="地点" prop="operLocation" width="80"/>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="operStatusTagType(row.status)" size="small">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="耗时(ms)" prop="costTime" width="90"/>
            <el-table-column label="失败原因" min-width="160" prop="errorMsg" show-overflow-tooltip/>
            <el-table-column fixed="right" label="操作" width="80">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <span data-testid="oper-empty">暂无操作日志</span>
            </template>
          </el-table>

          <div class="pager">
            <el-pagination
                v-model:current-page="operQuery.pageNum"
                v-model:page-size="operQuery.pageSize"
                :page-sizes="PAGE_SIZES"
                :total="operTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="load"
                @size-change="load"
            />
          </div>
        </el-tab-pane>

        <!-- 登录日志 -->
        <el-tab-pane label="登录日志" name="login">
          <div class="filter-bar">
            <el-input v-model="loginQuery.operator" clearable placeholder="用户名 / 姓名" style="width: 180px"/>
            <el-input v-model="loginQuery.keyword" clearable placeholder="IP / 提示消息" style="width: 180px"/>
            <el-select v-model="loginQuery.status" clearable placeholder="状态" style="width: 110px">
              <el-option v-for="o in LOGIN_STATUS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <div data-testid="log-date-range">
              <el-date-picker
                  v-model="dateRange"
                  end-placeholder="结束日期"
                  start-placeholder="开始日期"
                  style="width: 240px"
                  type="daterange"
                  value-format="YYYY-MM-DD"
              />
            </div>
            <el-button type="primary" @click="loginQuery.pageNum = 1; load()">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
            <el-button v-perm="'system:log:export'" :loading="exporting" data-testid="btn-export" @click="doExport">
              导出 CSV
            </el-button>
          </div>

          <el-table v-loading="loading" :data="loginRows" border data-testid="login-log-table" size="small" stripe>
            <el-table-column label="登录时间" prop="loginTime" width="160"/>
            <el-table-column label="用户名" prop="userName" width="130"/>
            <el-table-column label="姓名" prop="realName" width="110"/>
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="loginStatusTagType(row.loginStatus)" size="small">{{ row.loginStatusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="提示消息" min-width="180" prop="msg" show-overflow-tooltip/>
            <el-table-column label="IP" prop="loginIp" width="130"/>
            <el-table-column label="地点" prop="loginLocation" width="80"/>
            <el-table-column label="浏览器" prop="browser" width="100"/>
            <el-table-column label="操作系统" prop="os" width="110"/>
            <el-table-column label="User-Agent" min-width="200" prop="userAgent" show-overflow-tooltip/>
            <template #empty>
              <span data-testid="login-empty">暂无登录日志</span>
            </template>
          </el-table>

          <div class="pager">
            <el-pagination
                v-model:current-page="loginQuery.pageNum"
                v-model:page-size="loginQuery.pageSize"
                :page-sizes="PAGE_SIZES"
                :total="loginTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="load"
                @size-change="load"
            />
          </div>
        </el-tab-pane>

        <!-- 审计日志 -->
        <el-tab-pane label="审计日志" name="audit">
          <div class="filter-bar">
            <el-input v-model="auditQuery.module" clearable placeholder="模块" style="width: 130px"/>
            <el-input v-model="auditQuery.operation" clearable placeholder="操作类型" style="width: 140px"/>
            <el-input v-model="auditQuery.operator" clearable placeholder="操作人" style="width: 130px"/>
            <el-input v-model="auditQuery.keyword" clearable placeholder="内容 / 对象ID / IP" style="width: 200px"/>
            <el-select v-model="auditQuery.status" clearable placeholder="结果" style="width: 110px">
              <el-option v-for="o in AUDIT_STATUS" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <div data-testid="log-date-range">
              <el-date-picker
                  v-model="dateRange"
                  end-placeholder="结束日期"
                  start-placeholder="开始日期"
                  style="width: 240px"
                  type="daterange"
                  value-format="YYYY-MM-DD"
              />
            </div>
            <el-button type="primary" @click="auditQuery.pageNum = 1; load()">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
            <el-button v-perm="'system:log:export'" :loading="exporting" data-testid="btn-export" @click="doExport">
              导出 CSV
            </el-button>
          </div>

          <el-table
              v-loading="loading"
              :data="auditRows"
              border
              data-testid="audit-log-table"
              size="small"
              stripe
          >
            <el-table-column label="时间" prop="createTime" width="160"/>
            <el-table-column label="模块" prop="module" width="120"/>
            <el-table-column label="操作" prop="operation" show-overflow-tooltip width="140"/>
            <el-table-column label="操作人" prop="userName" width="110"/>
            <el-table-column label="对象类型" prop="targetType" width="130"/>
            <el-table-column label="对象ID" prop="targetId" show-overflow-tooltip width="150"/>
            <el-table-column label="内容" min-width="240" prop="content" show-overflow-tooltip/>
            <el-table-column label="IP" prop="ip" width="130"/>
            <el-table-column label="结果" width="80">
              <template #default="{ row }">
                <el-tag :type="auditStatusTagType(row.status)" size="small">{{ row.statusText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="失败原因" min-width="140" prop="errorMsg" show-overflow-tooltip/>
            <el-table-column fixed="right" label="操作" width="80">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <span data-testid="audit-empty">暂无审计日志</span>
            </template>
          </el-table>

          <div class="pager">
            <el-pagination
                v-model:current-page="auditQuery.pageNum"
                v-model:page-size="auditQuery.pageSize"
                :page-sizes="PAGE_SIZES"
                :total="auditTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="load"
                @size-change="load"
            />
          </div>
        </el-tab-pane>

        <!-- 字段级修改日志（sql/159 第四本账） -->
        <el-tab-pane label="字段变更" name="field">
          <div class="filter-bar">
            <el-select
                v-model="fieldQuery.targetType"
                clearable
                data-testid="field-biz-type"
                placeholder="对象类型"
                style="width: 140px"
            >
              <el-option v-for="o in FIELD_CHANGE_BIZ_TYPE" :key="o.value" :label="o.label" :value="o.value"/>
            </el-select>
            <el-input v-model="fieldQuery.fieldName" clearable placeholder="字段名（如 过敏史）" style="width: 160px"/>
            <el-input v-model="fieldQuery.targetId" clearable placeholder="对象ID" style="width: 150px"/>
            <el-input v-model="fieldQuery.operator" clearable placeholder="操作人" style="width: 120px"/>
            <el-input v-model="fieldQuery.keyword" clearable placeholder="对象名 / 编号 / 字段 / 值"
                      style="width: 200px"/>
            <div data-testid="log-date-range">
              <el-date-picker
                  v-model="dateRange"
                  end-placeholder="结束日期"
                  start-placeholder="开始日期"
                  style="width: 240px"
                  type="daterange"
                  value-format="YYYY-MM-DD"
              />
            </div>
            <el-button type="primary" @click="fieldQuery.pageNum = 1; load()">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
            <el-button v-perm="'system:log:export'" :loading="exporting" data-testid="btn-export" @click="doExport">
              导出 CSV
            </el-button>
          </div>

          <el-table
              v-loading="loading"
              :data="fieldRows"
              border
              data-testid="field-log-table"
              size="small"
              stripe
          >
            <el-table-column label="时间" prop="changeTime" width="160"/>
            <el-table-column label="对象类型" prop="bizTypeText" width="100"/>
            <el-table-column label="对象编号" prop="bizNo" show-overflow-tooltip width="120"/>
            <el-table-column label="对象名称" prop="bizName" show-overflow-tooltip width="120"/>
            <el-table-column label="字段" prop="fieldLabel" width="110"/>
            <el-table-column label="变更前 → 变更后" min-width="260">
              <template #default="{ row }">
                <template v-if="row.changeType === 'ACTION'">
                  <span class="diff-action">{{ row.fieldLabel }}</span>
                </template>
                <template v-else>
                  <span class="diff-old">{{ row.oldValue || '（空）' }}</span>
                  <span class="diff-arrow">→</span>
                  <span class="diff-new">{{ row.newValue || '（空）' }}</span>
                </template>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="changeTypeTagType(row.changeType)" size="small">{{ row.changeTypeText }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作人" prop="operatorName" width="100"/>
            <el-table-column label="科室" prop="deptName" width="110"/>
            <el-table-column fixed="right" label="操作" width="90">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openFieldBatch(row)">详情</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <span data-testid="field-empty">暂无字段变更日志</span>
            </template>
          </el-table>

          <div class="pager">
            <el-pagination
                v-model:current-page="fieldQuery.pageNum"
                v-model:page-size="fieldQuery.pageSize"
                :page-sizes="PAGE_SIZES"
                :total="fieldTotal"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="load"
                @size-change="load"
            />
          </div>
        </el-tab-pane>

        <!-- AI 调用审计 -->
        <el-tab-pane label="AI审计" name="ai">
          <div class="filter-bar">
            <el-select v-model="aiQuery.capabilityKey" clearable placeholder="能力" style="width: 160px">
              <el-option v-for="c in AI_CAPABILITIES" :key="c.value" :label="c.label" :value="c.value"/>
            </el-select>
            <el-select v-model="aiQuery.status" clearable placeholder="状态" style="width: 110px">
              <el-option v-for="s in AI_STATUS" :key="s.value" :label="s.label" :value="s.value"/>
            </el-select>
            <el-input v-model="aiQuery.operator" clearable placeholder="操作人账号" style="width: 140px"/>
            <div data-testid="log-date-range">
              <el-date-picker v-model="dateRange" end-placeholder="结束日期" start-placeholder="开始日期"
                              style="width: 240px" type="daterange" value-format="YYYY-MM-DD"/>
            </div>
            <el-button type="primary" @click="aiQuery.pageNum = 1; load()">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </div>

          <el-table v-loading="loading" :data="aiRows" border size="small" stripe>
            <el-table-column label="时间" width="160">
              <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column label="能力" min-width="130">
              <template #default="{ row }">{{ aiCapabilityLabel(row.capabilityKey) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{ row }">
                <el-tag :type="aiStatusTag(row.status)" size="small">{{ aiStatusLabel(row) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="模型" prop="model" min-width="130" show-overflow-tooltip/>
            <el-table-column label="耗时" prop="latencyMs" width="80" align="right">
              <template #default="{ row }">{{ row.latencyMs != null ? row.latencyMs + 'ms' : '—' }}</template>
            </el-table-column>
            <el-table-column label="Token(入/出)" width="110" align="right">
              <template #default="{ row }">{{ row.promptTokens ?? '—' }}/{{ row.completionTokens ?? '—' }}</template>
            </el-table-column>
            <el-table-column label="操作人" prop="operator" min-width="100" show-overflow-tooltip/>
            <el-table-column label="业务" min-width="130" show-overflow-tooltip>
              <template #default="{ row }">
                <template v-if="row.bizType">{{ row.bizType }}<template v-if="row.bizId"> #{{ row.bizId }}</template></template>
                <span v-else>—</span>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="70" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openAiDetail(row)">详情</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager">
            <el-pagination v-model:current-page="aiQuery.pageNum" v-model:page-size="aiQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="aiTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @current-change="load" @size-change="load"/>
          </div>
        </el-tab-pane>

        <!-- AI 病历草拟记录（G-10）：医生工作站「AI 草拟」现病史 → 医生修改 → 保存病历 的 diff 留痕 -->
        <el-tab-pane label="AI病历草拟" name="draftDiff">
          <div class="filter-bar">
            <el-input v-model="diffQuery.patientName" clearable placeholder="患者姓名" style="width: 150px"/>
            <el-input v-model="diffQuery.doctorName" clearable placeholder="医生姓名" style="width: 150px"/>
            <el-select v-model="diffQuery.changed" clearable placeholder="修改状态" style="width: 130px">
              <el-option :value="1" label="有修改"/>
              <el-option :value="0" label="未修改"/>
            </el-select>
            <el-button type="primary" @click="diffQuery.pageNum = 1; load()">查询</el-button>
            <el-button @click="resetQuery">重置</el-button>
          </div>

          <el-table v-loading="loading" :data="diffRows" border size="small" stripe>
            <el-table-column label="发生时间" prop="createTime" width="160"/>
            <el-table-column label="患者" prop="patientName" min-width="100" show-overflow-tooltip/>
            <el-table-column label="科室" prop="deptName" min-width="120" show-overflow-tooltip/>
            <el-table-column label="医生" prop="doctorName" min-width="100" show-overflow-tooltip/>
            <el-table-column label="状态" width="90" align="center">
              <template #default="{ row }">
                <el-tag :type="row.changed ? 'warning' : 'info'" size="small">{{ row.changed ? '有修改' : '未修改' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="AI 草稿" prop="draftText" min-width="220" show-overflow-tooltip/>
            <el-table-column fixed="right" label="操作" width="80" align="center">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openDiffDetail(row)">详情</el-button>
              </template>
            </el-table-column>
            <template #empty>
              <span data-testid="draft-diff-empty">暂无 AI 病历草拟记录</span>
            </template>
          </el-table>

          <div class="pager">
            <el-pagination v-model:current-page="diffQuery.pageNum" v-model:page-size="diffQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="diffTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @current-change="load" @size-change="load"/>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 日志详情（只读）与本批次明细：弹框内部逻辑在子组件，页面只控显隐与传行 -->
    <LogDetail v-model="detailVisible" :id="detailId" :row="detailRow" :type="detailType"/>
    <LogChangeDetail v-model="batchVisible" :batch-no="batchNo" :row="batchRow"/>
    <AiAuditDetail v-model="aiDetailVisible" :row="aiDetailRow"/>
    <LogDraftDiffDetail v-model="diffVisible" :row="diffRow"/>
  </div>
</template>

<script setup>
/**
 * 日志审计（sql/158 + sql/159，菜单 1108，权限 system:log:list / system:log:export）
 *
 * 四本账放在一个页面里是有意的：等保三级查的是"审计覆盖到每个用户、记录重要安全事件"，
 * 分开四个页面的话，"登录失败 5 次之后紧接着来了 3 次越权操作"这类串联证据永远拼不起来。
 *
 *  ① 操作日志 sys_oper_log —— 谁调了哪个写接口（读接口后端死表挡掉，不记）
 *  ② 登录日志 sys_login_log —— 成功失败都记，失败那一半才是等保要看的
 *  ③ 审计日志 sys_audit_log —— 业务模块显式写的"谁对哪个对象做了什么"
 *  ④ 字段级修改日志 sys_field_change_log —— 哪个字段从什么值改成了什么值
 *     （前三本都答不出"改之前是什么"，这本就是补那一刀的）
 *
 * 时间范围是四个页签共用的：查一件事时通常要在同一时间窗里横着看几本账。
 * 页面只读 + 导出，后端没有任何删除接口 —— 审计记录不允许应用侧删改。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {
  exportLogCsv,
  getLogStat,
  listAuditLogPage,
  listFieldChangePage,
  listLoginLogPage,
  listOperLogPage,
} from '@/api/log';
import { getAiAuditLogPage, listDraftDiffPage } from '@/api/ai';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import LogChangeDetail from './LogChangeDetail.vue';
import LogDetail from './LogDetail.vue';
import LogDraftDiffDetail from './LogDraftDiffDetail.vue';
import AiAuditDetail from './AiAuditDetail.vue';
import {
  AUDIT_STATUS,
  auditStatusTagType,
  changeTypeTagType,
  FIELD_CHANGE_BIZ_TYPE,
  LOG_TYPE,
  LOGIN_STATUS,
  loginStatusTagType,
  OPER_BUSINESS_TYPE,
  OPER_STATUS,
  operStatusTagType,
} from '@/lib/log';

const activeTab = ref('oper');
const loading = ref(false);
const stat = ref({});
// 四本账共用一个时间窗（查一件事要横着看几本账），格式 yyyy-MM-dd
const dateRange = ref(null);
const makeQuery = (extra = {}) => reactive({
  keyword: '',
  operator: '',
  status: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  ...extra,
});
const operQuery = makeQuery({businessType: null});
const loginQuery = makeQuery();
const auditQuery = makeQuery({module: '', operation: ''});
// 字段变更：targetType 复用为对象类型、targetId 为对象ID，与后端 DTO 口径一致
const fieldQuery = makeQuery({
  targetType: null,
  targetId: '',
  fieldName: '',
});
// AI 审计
const aiQuery = makeQuery({ capabilityKey: '', status: null });
const aiRows = ref([]);
const aiTotal = ref(0);
const AI_CAPABILITIES = [
  {value: 'icd10', label: 'ICD-10 编码'},
  {value: 'drug_audit', label: '处方审核'},
  {value: 'emr_qc', label: '病历质控'},
  {value: 'lab_interpret', label: '检验解读'},
  {value: 'emergency_triage', label: '急诊分诊'},
  {value: 'emr_extract', label: '病历抽取'},
  {value: 'emr_draft', label: '病历草拟'},
  {value: 'patient_report_explain', label: '患者报告解读'},
  {value: 'patient_triage_normalize', label: '导诊口语归一'},
  {value: 'knowledge_qa', label: '知识库问答'},
  {value: 'operation_qa', label: '运营问数'},
  {value: 'previsit_summary', label: '预问诊摘要'},
  {value: 'followup_compose', label: '随访话术'},
  {value: 'insurance_evidence', label: '医保证据判定'},
  {value: 'health_check', label: '连通性自检'},
];
const AI_STATUS = [
  {value: 1, label: '成功'},
  {value: 2, label: '失败'},
  {value: 3, label: '超时'},
  {value: 4, label: '降级'},
  {value: 5, label: '熔断'},
];
const aiCapabilityLabel = (key) => AI_CAPABILITIES.find(c => c.value === key)?.label || key;
const aiStatusTag = (s) => ({1:'success',2:'danger',3:'warning',4:'warning',5:'danger'})[s] || 'info';
const aiStatusLabel = (row) => row.statusText || AI_STATUS.find(s => s.value === row.status)?.label || row.status;
const formatTime = (t) => t ? String(t).replace('T', ' ').slice(0, 19) : '—';
const loadAi = async () => {
  const res = await getAiAuditLogPage({
    ...aiQuery,
    ...range(),
    capabilityKey: aiQuery.capabilityKey || undefined,
    startDate: dateRange.value?.[0] || undefined,
    endDate: dateRange.value?.[1] || undefined,
  });
  aiRows.value = res?.data?.records || [];
  aiTotal.value = Number(res?.data?.total || 0);
};
// AI 审计详情弹框
const aiDetailVisible = ref(false);
const aiDetailRow = ref(null);
const openAiDetail = (row) => {
  aiDetailRow.value = row;
  aiDetailVisible.value = true;
};
// AI 病历草拟记录（G-10）：后端 /emr/draftDiff/listPage 不支持时间范围，本 tab 不挂共用 dateRange
const diffQuery = makeQuery({ patientName: '', doctorName: '', changed: undefined });
const diffRows = ref([]);
const diffTotal = ref(0);
const loadDraftDiff = async () => {
  const res = await listDraftDiffPage({
    pageNum: diffQuery.pageNum,
    pageSize: diffQuery.pageSize,
    patientName: diffQuery.patientName || undefined,
    doctorName: diffQuery.doctorName || undefined,
    changed: diffQuery.changed,
  });
  diffRows.value = res?.data?.records || [];
  diffTotal.value = Number(res?.data?.total || 0);
};
const diffVisible = ref(false);
const diffRow = ref(null);
const openDiffDetail = (row) => {
  diffRow.value = row;
  diffVisible.value = true;
};
const operRows = ref([]);
const loginRows = ref([]);
const auditRows = ref([]);
const fieldRows = ref([]);
const operTotal = ref(0);
const loginTotal = ref(0);
const auditTotal = ref(0);
const fieldTotal = ref(0);
const currentQuery = () => {
  if (activeTab.value === 'oper') return operQuery;
  if (activeTab.value === 'login') return loginQuery;
  if (activeTab.value === 'field') return fieldQuery;
  if (activeTab.value === 'ai') return aiQuery;
  if (activeTab.value === 'draftDiff') return diffQuery;
  return auditQuery;
};
const range = () => ({
  beginTime: dateRange.value?.[0] || null,
  endTime: dateRange.value?.[1] || null,
});
// ============ 统计 ============
const statCards = computed(() => [
  {key: 'operToday', label: '今日操作', value: stat.value.operToday ?? 0, sub: `近7天 ${stat.value.oper7d ?? 0}`},
  {
    key: 'operFailToday',
    label: '今日操作失败',
    value: stat.value.operFailToday ?? 0,
    sub: `总量 ${stat.value.operTotal ?? 0}`
  },
  {key: 'loginToday', label: '今日登录', value: stat.value.loginToday ?? 0, sub: `近7天 ${stat.value.login7d ?? 0}`},
  {
    key: 'loginFailToday',
    label: '今日登录失败',
    value: stat.value.loginFailToday ?? 0,
    sub: `总量 ${stat.value.loginTotal ?? 0}`
  },
  {key: 'auditToday', label: '今日审计', value: stat.value.auditToday ?? 0, sub: `近7天 ${stat.value.audit7d ?? 0}`},
  {
    key: 'fieldChangeToday',
    label: '今日字段变更',
    value: stat.value.fieldChangeToday ?? 0,
    sub: `总量 ${stat.value.fieldChangeTotal ?? 0}`
  },
]);
const riskyAccounts = computed(() => stat.value.riskyAccounts || []);
const loadStat = async () => {
  try {
    const res = await getLogStat();
    stat.value = res?.data || {};
  } catch (e) {
    console.error('加载日志统计失败', e);
  }
};
// ============ 列表 ============
const loadOper = async () => {
  const res = await listOperLogPage({...operQuery, ...range()});
  operRows.value = res?.data?.records || [];
  operTotal.value = Number(res?.data?.total || 0);
};
const loadLogin = async () => {
  const res = await listLoginLogPage({...loginQuery, ...range()});
  loginRows.value = res?.data?.records || [];
  loginTotal.value = Number(res?.data?.total || 0);
};
const loadAudit = async () => {
  const res = await listAuditLogPage({...auditQuery, ...range()});
  auditRows.value = res?.data?.records || [];
  auditTotal.value = Number(res?.data?.total || 0);
};
const loadField = async () => {
  const res = await listFieldChangePage({...fieldQuery, ...range()});
  fieldRows.value = res?.data?.records || [];
  fieldTotal.value = Number(res?.data?.total || 0);
};
const load = async () => {
  loading.value = true;
  try {
    if (activeTab.value === 'oper') await loadOper();
    else if (activeTab.value === 'login') await loadLogin();
    else if (activeTab.value === 'field') await loadField();
    else if (activeTab.value === 'ai') await loadAi();
    else if (activeTab.value === 'draftDiff') await loadDraftDiff();
    else await loadAudit();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    loading.value = false;
  }
};
const onTabChange = () => {
  load();
};
const resetQuery = () => {
  if (activeTab.value === 'draftDiff') {
    Object.assign(diffQuery, {patientName: '', doctorName: '', changed: undefined, pageNum: 1});
    load();
    return;
  }
  const q = currentQuery();
  Object.assign(q, {keyword: '', operator: '', status: null, pageNum: 1});
  if (activeTab.value === 'oper')
    q.businessType = null;
  if (activeTab.value === 'audit') {
    q.module = '';
    q.operation = '';
  }
  if (activeTab.value === 'field') {
    q.targetType = null;
    q.targetId = '';
    q.fieldName = '';
  }
  load();
};
onMounted(async () => {
  await Promise.all([loadStat(), load()]);
});
// ============ 详情 ============
// 取数与渲染搬进 LogDetail.vue，页面只留显隐 + 当前行（打开瞬间不闪白）
const detailVisible = ref(false);
const detailType = ref('oper');
const detailId = ref(null);
const detailRow = ref(null);
const openDetail = (row) => {
  detailType.value = activeTab.value === 'audit' ? 'audit' : 'oper';
  detailId.value = row.id;
  detailRow.value = row;
  detailVisible.value = true;
};
// ============ 字段变更：同批次明细 ============
// 一次保存改 5 个字段会落 5 行，列表里散着看不出是"同一个人同一分钟改的"。
// 点行按 batchNo 拉回整批，一次性看完这一刀动了什么 —— 这才是审计要问的。
// 拉取与渲染搬进 LogChangeDetail.vue，页面只传批次号与当前行。
const batchVisible = ref(false);
const batchNo = ref(null);
const batchRow = ref(null);
const openFieldBatch = (row) => {
  batchNo.value = row.batchNo;
  batchRow.value = row;
  batchVisible.value = true;
};
// ============ 导出 ============
const exporting = ref(false);
const doExport = async () => {
  exporting.value = true;
  try {
    const logType = activeTab.value === 'oper'
        ? LOG_TYPE.OPER
        : activeTab.value === 'login'
            ? LOG_TYPE.LOGIN
            : activeTab.value === 'field'
                ? LOG_TYPE.FIELD_CHANGE
                : LOG_TYPE.AUDIT;
    const res = await exportLogCsv({...currentQuery(), ...range(), logType});
    const nameMap = {oper: '操作日志', login: '登录日志', audit: '审计日志', field: '字段变更日志'};
    const blob = new Blob([res?.data || ''], {type: 'text/csv;charset=utf-8'});
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${nameMap[activeTab.value]}_${new Date().toISOString().slice(0, 10)}.csv`;
    a.click();
    URL.revokeObjectURL(url);
    ElMessage.success('导出完成');
    await loadStat();
  } catch (e)  /* 错误已由全局拦截器统一提示，无需在此弹窗 */{
    
  } finally {
    exporting.value = false;
  }
};
</script>

<style scoped>
.log-view {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.stat-item {
  padding: 10px 12px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 6px;
  background: var(--el-fill-color-lighter);
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.stat-value {
  font-size: 22px;
  font-weight: 600;
  line-height: 1.4;
}

.stat-sub {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.risk-alert {
  margin-top: 12px;
}

.risk-item {
  margin-right: 12px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

/* 变更前 → 变更后：旧值灰掉划掉，新值主色加重，一眼看出"改成了啥" */
.diff-old {
  color: var(--el-text-color-secondary);
  text-decoration: line-through;
}

.diff-arrow {
  margin: 0 8px;
  color: var(--el-text-color-placeholder);
}

.diff-new {
  color: var(--el-color-primary);
  font-weight: 600;
}

.diff-action {
  color: var(--el-text-color-secondary);
}
</style>
