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
              @row-click="openDetail"
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
              @row-click="openDetail"
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
              @row-click="openFieldBatch"
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
                <el-button link type="primary" @click.stop="openFieldBatch(row)">本批次</el-button>
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
      </el-tabs>
    </el-card>

    <!-- 详情：只读（审计记录不给改），编辑没有入口 -->
    <el-dialog
        v-model="detailVisible"
        :title="detailType === 'audit' ? '审计日志详情' : '操作日志详情'"
        width="720px"
    >
      <el-descriptions :column="2" border size="small">
        <template v-if="detailType === 'audit'">
          <el-descriptions-item label="时间">{{ detail.createTime }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ detail.userName }}</el-descriptions-item>
          <el-descriptions-item label="模块">{{ detail.module }}</el-descriptions-item>
          <el-descriptions-item label="操作">{{ detail.operation }}</el-descriptions-item>
          <el-descriptions-item label="对象类型">{{ detail.targetType }}</el-descriptions-item>
          <el-descriptions-item label="对象ID">{{ detail.targetId }}</el-descriptions-item>
          <el-descriptions-item label="IP">{{ detail.ip }}</el-descriptions-item>
          <el-descriptions-item label="结果">{{ detail.statusText }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="内容">{{ detail.content }}</el-descriptions-item>
          <el-descriptions-item v-if="detail.errorMsg" :span="2" label="失败原因">
            {{ detail.errorMsg }}
          </el-descriptions-item>
        </template>
        <template v-else>
          <el-descriptions-item label="时间">{{ detail.operTime }}</el-descriptions-item>
          <el-descriptions-item label="操作人">{{ detail.operName }}</el-descriptions-item>
          <el-descriptions-item label="模块">{{ detail.title }}</el-descriptions-item>
          <el-descriptions-item label="业务类型">{{ detail.businessTypeText }}</el-descriptions-item>
          <el-descriptions-item label="方法">{{ detail.method }}</el-descriptions-item>
          <el-descriptions-item label="请求方式">{{ detail.requestMethod }}</el-descriptions-item>
          <el-descriptions-item label="科室">{{ detail.deptName }}</el-descriptions-item>
          <el-descriptions-item label="IP">{{ detail.operIp }}（{{ detail.operLocation }}）</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusText }}</el-descriptions-item>
          <el-descriptions-item label="耗时">{{ detail.costTime }} ms</el-descriptions-item>
          <el-descriptions-item :span="2" label="URL">{{ detail.operUrl }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="请求参数">
            <pre class="log-pre">{{ detail.operParam || '-' }}</pre>
          </el-descriptions-item>
          <el-descriptions-item :span="2" label="失败原因">{{ detail.errorMsg || '-' }}</el-descriptions-item>
        </template>
      </el-descriptions>
    </el-dialog>

    <!-- 字段变更：同批次明细（一次保存改了哪些字段） -->
    <el-dialog v-model="batchVisible" data-testid="field-batch-dialog" title="本次修改明细" width="720px">
      <el-descriptions :column="2" border class="batch-meta" size="small">
        <el-descriptions-item label="时间">{{ batchMeta.changeTime }}</el-descriptions-item>
        <el-descriptions-item label="操作人">{{ batchMeta.operatorName }}</el-descriptions-item>
        <el-descriptions-item label="对象">{{ batchMeta.bizTypeText }} ·
          {{ batchMeta.bizName || batchMeta.bizNo || batchMeta.bizId }}
        </el-descriptions-item>
        <el-descriptions-item label="批次号">{{ batchMeta.batchNo }}</el-descriptions-item>
      </el-descriptions>
      <el-table :data="batchRows" border class="batch-table" data-testid="field-batch-table" size="small" stripe>
        <el-table-column label="字段" prop="fieldLabel" width="140"/>
        <el-table-column label="变更前" min-width="200">
          <template #default="{ row }">{{ row.oldValue || '（空）' }}</template>
        </el-table-column>
        <el-table-column label="变更后" min-width="200">
          <template #default="{ row }">{{ row.newValue || '（空）' }}</template>
        </el-table-column>
        <template #empty>
          <span data-testid="field-batch-empty">本批次没有明细</span>
        </template>
      </el-table>
    </el-dialog>
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
  getAuditLogDetail,
  getFieldChangeBatch,
  getLogStat,
  getOperLogDetail,
  listAuditLogPage,
  listFieldChangePage,
  listLoginLogPage,
  listOperLogPage,
} from '@/api/log';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
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
const operRows = ref([]);
const loginRows = ref([]);
const auditRows = ref([]);
const fieldRows = ref([]);
const operTotal = ref(0);
const loginTotal = ref(0);
const auditTotal = ref(0);
const fieldTotal = ref(0);
const currentQuery = () => {
  if (activeTab.value === 'oper')
    return operQuery;
  if (activeTab.value === 'login')
    return loginQuery;
  if (activeTab.value === 'field')
    return fieldQuery;
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
    if (activeTab.value === 'oper')
      await loadOper();
    else if (activeTab.value === 'login')
      await loadLogin();
    else if (activeTab.value === 'field')
      await loadField();
    else
      await loadAudit();
  } catch (e) {
    ElMessage.error(e?.message || '加载日志失败');
  } finally {
    loading.value = false;
  }
};
const onTabChange = () => {
  load();
};
const resetQuery = () => {
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
const detailVisible = ref(false);
const detailType = ref('oper');
const detail = ref({});
const openDetail = async (row) => {
  detailType.value = activeTab.value === 'audit' ? 'audit' : 'oper';
  try {
    const res = detailType.value === 'audit' ? await getAuditLogDetail(row.id) : await getOperLogDetail(row.id);
    detail.value = res?.data || {};
    detailVisible.value = true;
  } catch (e) {
    ElMessage.error(e?.message || '加载日志详情失败');
  }
};
// ============ 字段变更：同批次明细 ============
// 一次保存改 5 个字段会落 5 行，列表里散着看不出是"同一个人同一分钟改的"。
// 点行按 batchNo 拉回整批，一次性看完这一刀动了什么 —— 这才是审计要问的。
const batchVisible = ref(false);
const batchRows = ref([]);
const batchMeta = ref({});
const openFieldBatch = async (row) => {
  try {
    const res = await getFieldChangeBatch(row.batchNo);
    batchRows.value = res?.data || [];
    batchMeta.value = row;
    batchVisible.value = true;
  } catch (e) {
    ElMessage.error(e?.message || '加载变更明细失败');
  }
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
  } catch (e) {
    ElMessage.error(e?.message || '导出失败');
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

.log-pre {
  margin: 0;
  max-height: 200px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
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

.batch-meta {
  margin-bottom: 12px;
}
</style>
