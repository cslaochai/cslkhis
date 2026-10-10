<template>
  <div>
    <el-card class="stat-card mb-3" shadow="never">
      <div class="stat-row">
        <div class="stat-item"><span class="stat-label">登记总数</span><span class="stat-value">{{ stats.total }}</span>
        </div>
        <div class="stat-item"><span class="stat-label">术后观察中</span><span
            class="stat-value warn">{{ stats.observingCount }}</span></div>
        <div class="stat-item"><span class="stat-label">滞留超期</span><span
            class="stat-value danger">{{ stats.overdueCount }}</span></div>
        <div class="stat-item"><span class="stat-label">已出院</span><span
            class="stat-value ok">{{ stats.dischargedCount }}</span></div>
        <div class="stat-item"><span class="stat-label">已转住院</span><span
            class="stat-value">{{ stats.transferredCount }}</span></div>
        <div class="stat-item"><span class="stat-label">应随访未随访</span><span
            class="stat-value danger">{{ stats.followOverdueCount }}</span></div>
        <div class="stat-item"><span class="stat-label">非计划再入院</span><span
            class="stat-value">{{ stats.readmitCount }}</span></div>
        <div class="stat-item"><span class="stat-label">按时离院率(%)</span><span
            class="stat-value">{{ stats.onTimeLeaveRate }}</span></div>
      </div>
    </el-card>

    <!-- 登记台账（「准入目录」页签已拆为独立菜单 2928「日间手术准入目录」，sql/183） -->
    <el-card ref="queryCardRef" class="query-card mb-3" shadow="never">
      <div class="flex items-start justify-between gap-4">
        <el-form :model="query" inline @submit.prevent>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" clearable placeholder="单号 / 患者 / 术式" style="width: 200px"
                      @keyup.enter="loadList"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="状态" style="width: 130px">
              <el-option v-for="d in statusDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </el-form-item>
          <el-form-item label="准入术式">
            <el-select v-model="query.itemId" clearable filterable placeholder="准入术式" style="width: 200px">
              <el-option v-for="d in itemOptions" :key="d.id" :label="`${d.itemCode} ${d.itemName}`" :value="d.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="手术科室">
            <el-select v-model="query.deptId" clearable filterable placeholder="手术科室" style="width: 170px">
              <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
            </el-select>
          </el-form-item>
          <el-form-item label="计划日期">
            <el-date-picker v-model="query.dateRange" end-placeholder="计划截止" start-placeholder="计划起始"
                            style="width: 240px" type="daterange" value-format="YYYY-MM-DD"/>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="query.openOnly">仅在院</el-checkbox>
            <el-checkbox v-model="query.overdueOnly">仅超期</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button :icon="Search" type="primary" @click="loadList">查询</el-button>
            <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>
        <div class="flex shrink-0 items-start gap-3">
          <el-button v-perm="'ipd:daySurgery:add'" :icon="Plus" type="primary" @click="openApplyCreate">预约登记
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table v-loading="loading" :data="rows" :max-height="tableMaxHeight" stripe @row-click="openDetail">
        <el-table-column label="登记单号" prop="applyNo" width="150"/>
        <el-table-column label="患者" prop="patientName" width="100"/>
        <el-table-column label="术式" min-width="180" prop="itemName" show-overflow-tooltip/>
        <el-table-column align="right" label="最长滞留(h)" width="100">
          <template #default="{ row }">{{ row.maxStayHours ?? '—' }}</template>
        </el-table-column>
        <el-table-column label="计划手术日" prop="planSurgeryDate" width="120"/>
        <el-table-column label="术前评估" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.evalResult === 1" size="small" type="success">通过</el-tag>
            <el-tag v-else-if="row.evalResult === 2" size="small" type="danger">不通过</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column align="center" label="滞留超期" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.overdue" size="small" type="danger">超期</el-tag>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="随访" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.followOverdue" size="small" type="danger">已过时限未访</el-tag>
            <span v-else-if="row.followCount">{{ row.followCount }} 次</span>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="手术间" prop="operatingRoom" width="100"/>
        <el-table-column label="主刀" prop="surgeon" width="100"/>
        <el-table-column fixed="right" label="操作" width="380">
          <template #default="{ row }">
            <el-button v-if="row.canEvaluate" v-perm="'ipd:daySurgery:edit'" link size="small" type="primary"
                       @click.stop="openEval(row)">术前评估
            </el-button>
            <el-button v-if="row.canArrange" v-perm="'ipd:daySurgery:edit'" link size="small" type="primary"
                       @click.stop="openArrange(row)">安排
            </el-button>
            <el-button v-if="row.canFinish" v-perm="'ipd:daySurgery:edit'" link size="small" type="primary"
                       @click.stop="doFinish(row)">完成手术
            </el-button>
            <el-button v-if="row.canDischarge" v-perm="'ipd:daySurgery:edit'" link size="small" type="success"
                       @click.stop="openDischarge(row)">离院
            </el-button>
            <el-button v-if="row.canTransfer" v-perm="'ipd:daySurgery:edit'" link size="small" type="warning"
                       @click.stop="openTransfer(row)">转住院
            </el-button>
            <el-button v-if="row.canFollow" v-perm="'ipd:daySurgery:edit'" link size="small" type="primary"
                       @click.stop="openFollow(row)">随访
            </el-button>
            <el-button v-if="row.canCancel" v-perm="'ipd:daySurgery:edit'" link size="small" type="info"
                       @click.stop="openCancel(row)">取消
            </el-button>
            <el-button v-if="row.canEdit" v-perm="'ipd:daySurgery:add'" link size="small" type="primary"
                       @click.stop="openApplyEdit(row)">编辑
            </el-button>
            <el-button v-if="row.canDelete" v-perm="'ipd:daySurgery:add'" link size="small" type="danger"
                       @click.stop="remove(row)">删除
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无日间手术登记"/>
        </template>
      </el-table>

      <div ref="footerRef" class="list-footer flex items-center justify-end">
        <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                       :page-sizes="PAGE_SIZES" :total="total" layout="total, sizes, prev, pager, next, jumper"
                       @size-change="loadList" @current-change="loadList"/>
      </div>
    </el-card>

    <!-- 预约登记 -->
    <el-dialog v-model="applyVisible" :title="applyForm.id ? '修改预约登记' : '日间手术预约登记'" width="640px">
      <el-form label-width="120px" size="small">
        <el-form-item label="准入术式" required>
          <el-select v-model="applyForm.itemId" filterable placeholder="仅启用中的术式" style="width: 100%">
            <el-option v-for="d in itemOptions" :key="d.id" :label="`${d.itemCode} ${d.itemName}`" :value="d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="患者" required>
          <PatientSelect :model-value="applyForm.patientId" placeholder="搜索患者（姓名/编号）" style="width: 100%"
                         @select="onPatientSelect"/>
        </el-form-item>
        <el-form-item label="手术科室">
          <el-select v-model="applyForm.deptId" clearable filterable placeholder="请选择" style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName || d.name" :value="d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="计划手术日期" required>
          <el-date-picker v-model="applyForm.planSurgeryDate" placeholder="请选择" style="width: 100%"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.remark"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button :loading="applySaving" type="primary" @click="saveApply">保存</el-button>
      </template>
    </el-dialog>

    <!-- 术前评估 -->
    <el-dialog v-model="evalVisible" title="术前评估" width="600px">
      <el-form label-width="110px" size="small">
        <el-form-item label="评估结论" required>
          <el-radio-group v-model="evalForm.evalResult">
            <el-radio v-for="d in evalDict" :key="d.dictValue" :value="Number(d.dictValue)">{{ d.dictLabel }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="评估意见" required>
          <el-input v-model="evalForm.evalRemark" :rows="4" placeholder="禁忌筛查结论 / 适应证与麻醉评估（不通过不得安排手术）"
                    type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evalVisible = false">取消</el-button>
        <el-button :loading="evalLoading" type="primary" @click="submitEval">提交评估</el-button>
      </template>
    </el-dialog>

    <!-- 安排 -->
    <el-dialog v-model="arrangeVisible" title="安排手术" width="600px">
      <el-form label-width="110px" size="small">
        <el-form-item label="手术时间" required>
          <el-date-picker v-model="arrangeForm.surgeryTime" placeholder="请选择" style="width: 100%"
                          type="datetime" value-format="YYYY-MM-DD HH:mm"/>
        </el-form-item>
        <el-form-item label="手术间" required>
          <el-input v-model="arrangeForm.operatingRoom" placeholder="如：日间手术室 1 间"/>
        </el-form-item>
        <el-form-item label="台次">
          <el-input-number v-model="arrangeForm.seqNo" :max="99" :min="1"/>
        </el-form-item>
        <el-form-item label="麻醉方式" required>
          <el-select v-model="arrangeForm.anesthesiaType" placeholder="请选择" style="width: 100%">
            <el-option v-for="d in anesDict" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="主刀医生" required>
          <el-input v-model="arrangeForm.surgeon"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="arrangeVisible = false">取消</el-button>
        <el-button :icon="Scissor" :loading="arrangeLoading" type="primary" @click="submitArrange">安排</el-button>
      </template>
    </el-dialog>

    <!-- 离院 -->
    <el-dialog v-model="disVisible" title="离院登记" width="600px">
      <el-form label-width="110px" size="small">
        <el-form-item label="离院方式" required>
          <el-radio-group v-model="disForm.leaveType">
            <el-radio :value="1">按时离院</el-radio>
            <el-radio :value="3">非计划再入院</el-radio>
          </el-radio-group>
          <div class="hint-line">转普通住院请走「转住院」动作（必须回填住院号）</div>
        </el-form-item>
        <el-form-item label="离院时间">
          <el-date-picker v-model="disForm.dischargeTime" placeholder="不填取当前时间" style="width: 100%"
                          type="datetime" value-format="YYYY-MM-DD HH:mm"/>
        </el-form-item>
        <el-form-item label="出院评估">
          <el-input v-model="disForm.dischargeRemark" :rows="3" placeholder="出院评估结论 / 医嘱交代" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="disVisible = false">取消</el-button>
        <el-button :loading="disLoading" type="primary" @click="submitDischarge">登记离院</el-button>
      </template>
    </el-dialog>

    <!-- 转住院 -->
    <el-dialog v-model="trVisible" title="转普通住院" width="600px">
      <el-form label-width="120px" size="small">
        <el-form-item label="住院记录ID" required>
          <el-input v-model="trForm.transferAdmissionId" placeholder="biz_admission.admission_id（必填）"/>
        </el-form-item>
        <el-form-item label="转住院原因" required>
          <el-input v-model="trForm.transferRemark" :rows="3" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="trVisible = false">取消</el-button>
        <el-button :loading="trLoading" type="primary" @click="submitTransfer">确认转住院</el-button>
      </template>
    </el-dialog>

    <!-- 随访 -->
    <el-dialog v-model="foVisible" title="登记随访（出院后 24h 内必访）" width="600px">
      <el-form label-width="110px" size="small">
        <el-form-item label="随访方式" required>
          <el-radio-group v-model="foForm.followType">
            <el-radio v-for="d in followTypeDict" :key="d.dictValue" :value="Number(d.dictValue)">{{
                d.dictLabel
              }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="随访结果" required>
          <el-select v-model="foForm.result" style="width: 100%">
            <el-option v-for="d in followResultDict" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="随访内容">
          <el-input v-model="foForm.content" :rows="3" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="foVisible = false">取消</el-button>
        <el-button :loading="foLoading" type="primary" @click="submitFollow">登记</el-button>
      </template>
    </el-dialog>

    <!-- 取消 -->
    <el-dialog v-model="cancelVisible" title="取消登记单" width="560px">
      <el-form label-width="100px" size="small">
        <el-form-item label="取消原因" required>
          <el-input v-model="cancelReason" :rows="4" placeholder="原因必填，将留痕" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelVisible = false">取消</el-button>
        <el-button :loading="cancelLoading" type="primary" @click="submitCancel">确认取消</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="日间手术登记详情" width="820px">
      <div v-if="detail">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="登记单号">{{ detail.applyNo }}</el-descriptions-item>
          <el-descriptions-item label="术式">{{ detail.itemName }}（{{ detail.itemCode }}）</el-descriptions-item>
          <el-descriptions-item label="最长滞留(h)">{{ detail.maxStayHours }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName }}（{{
              detail.patientNo || '—'
            }}）
          </el-descriptions-item>
          <el-descriptions-item label="手术科室">{{ detail.deptName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="计划手术日">{{ detail.planSurgeryDate }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTag(detail.status)" size="small">{{ statusText(detail.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="术前评估">
            <el-tag v-if="detail.evalResult === 1" size="small" type="success">通过</el-tag>
            <el-tag v-else-if="detail.evalResult === 2" size="small" type="danger">不通过</el-tag>
            <span v-else>—</span>
          </el-descriptions-item>
          <el-descriptions-item label="麻醉方式">{{ anesText(detail.anesthesiaType) }}</el-descriptions-item>
          <el-descriptions-item :span="3" label="评估意见">{{ detail.evalRemark || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手术时间">{{ detail.surgeryTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手术间">{{ detail.operatingRoom || '—' }}</el-descriptions-item>
          <el-descriptions-item label="主刀">{{ detail.surgeon || '—' }}</el-descriptions-item>
          <el-descriptions-item label="手术结束">{{ detail.surgeryEndTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="离院方式">{{
              detail.leaveType ? leaveText(detail.leaveType) : '—'
            }}
          </el-descriptions-item>
          <el-descriptions-item label="离院时间">{{ detail.dischargeTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="随访时限">{{ detail.followDue || '—' }}</el-descriptions-item>
          <el-descriptions-item label="随访次数">{{ detail.followCount ?? 0 }}</el-descriptions-item>
          <el-descriptions-item label="超期">
            <el-tag v-if="detail.overdue" size="small" type="danger">滞留超期</el-tag>
            <span v-else>否</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.transferAdmissionId" :span="2" label="转住院ID">
            {{ detail.transferAdmissionId }}
          </el-descriptions-item>
          <el-descriptions-item v-if="detail.cancelReason" :span="3" label="取消原因">{{
              detail.cancelReason
            }}
          </el-descriptions-item>
          <el-descriptions-item :span="3" label="出院评估">{{ detail.dischargeRemark || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div class="flow-title">随访台账</div>
        <el-timeline v-if="detail.follows && detail.follows.length">
          <el-timeline-item v-for="f in detail.follows" :key="f.id" :timestamp="f.followTime" placement="top">
            <div class="flow-item">
              <strong>{{ followTypeText(f.followType) }}</strong>
              <el-tag :type="Number(f.result) === 1 ? 'success' : 'warning'" size="small">{{
                  followResultText(f.result)
                }}
              </el-tag>
              <span class="flow-op">{{ f.operator }}</span>
            </div>
            <div class="flow-content">{{ f.content || '—' }}</div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else :image-size="60" description="暂无随访记录"/>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 日间手术（菜单 316）
 *
 * 闭环：预约登记（待评估）→ 术前评估（评估通过）→ 手术安排（已安排）
 *      → 完成手术（术后观察）→ 出院 / 转住院（终态）→ 24h 随访
 *
 * 准入目录已拆为独立菜单 2928「日间手术准入目录」（sql/183）：
 * 本页预约下拉走 getDaySurgeryItemList（仅启用术式）——目录停用即不能再新预约，数据流不断。
 *
 * 五条口径：
 * 1. **按钮可用性一律读后端 can* 字段**，不按 status 码值 switch。
 * 2. **准入是闸门**：预约只能选启用中的目录术式（下拉只给启用的，后端也校验）。
 * 3. **评估未通过不得安排、未安排不得登记完成** —— 评审必查的两道硬闸门，收在服务端。
 * 4. **超期 / 随访时限是后端算的**（overdue / followDue / followOverdue），
 *    前端不重算时间差 —— 两套算法必然对不上。
 * 5. 转住院必须填住院号：那是医保与病案口径的分界点，没有它无法证明"住院从哪天算起"。
 * 6. 所有 ID 都是字符串（雪花ID），不要 Number() 转换。
 */
import {onMounted, reactive, ref} from 'vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {Plus, Refresh, Scissor, Search} from '@element-plus/icons-vue';
import {
  arrangeDaySurgery,
  cancelDaySurgery,
  daySurgeryApplyUpsert,
  deleteDaySurgery,
  dischargeDaySurgery,
  evaluateDaySurgery,
  finishDaySurgery,
  followDaySurgery,
  getDaySurgeryDetail,
  getDaySurgeryItemList,
  getDaySurgeryStat,
  listDaySurgeryPage,
  transferDaySurgeryToIpd,
} from '@/api/daySurgery';
import {getDepartmentSelectList, getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {useTableMaxHeight} from '@/lib/useTableMaxHeight';
import PatientSelect from '@/components/his/PatientSelect.vue';

const stats = reactive({
  total: 0, observingCount: 0, dischargedCount: 0, canceledCount: 0, transferredCount: 0,
  overdueCount: 0, followOverdueCount: 0, readmitCount: 0, onTimeLeaveRate: '0',
});
const statusDict = ref([]);
const anesDict = ref([]);
const leaveDict = ref([]);
const followResultDict = ref([]);
const followTypeDict = ref([]);
const evalDict = ref([]);
const deptOptions = ref([]);
const itemOptions = ref([]);
const statusText = (v) => dictLabelText(statusDict.value, v);
const anesText = (v) => dictLabelText(anesDict.value, v);
const leaveText = (v) => dictLabelText(leaveDict.value, v);
const followResultText = (v) => dictLabelText(followResultDict.value, v);
const followTypeText = (v) => dictLabelText(followTypeDict.value, v);
const evalText = (v) => dictLabelText(evalDict.value, v);
const statusTag = (v) => {
  switch (Number(v)) {
    case 1:
      return 'warning';
    case 2:
      return 'primary';
    case 3:
      return 'primary';
    case 4:
      return 'danger';
    case 5:
      return 'success';
    case 6:
      return 'info';
    case 7:
      return 'warning';
    default:
      return 'info';
  }
};
// ---------------- 登记单 ----------------
const loading = ref(false);
const rows = ref([]);
const total = ref(0);
const query = reactive({
  keyword: '', status: null, itemId: null, deptId: null,
  openOnly: false, overdueOnly: false, dateRange: null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
// 两卡式列表页：表格只设最大高度，超高内部滚动（口径参照 views/system/user/UserView.vue）
const {queryCardRef, footerRef, tableMaxHeight} = useTableMaxHeight();
const loadList = async () => {
  loading.value = true;
  try {
    const res = await listDaySurgeryPage({
      keyword: query.keyword.trim() || undefined,
      status: query.status ?? undefined,
      itemId: query.itemId || undefined,
      deptId: query.deptId || undefined,
      openOnly: query.openOnly || undefined,
      overdueOnly: query.overdueOnly || undefined,
      dateFrom: query.dateRange?.[0],
      dateTo: query.dateRange?.[1],
      pageNum: query.pageNum, pageSize: query.pageSize,
    });
    if (res.code === 200) {
      rows.value = res.data?.records || [];
      total.value = Number(res.data?.total || 0);
    } else {
      ElMessage.error(res.message || '查询失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    loading.value = false;
  }
};
const resetQuery = () => {
  query.keyword = '';
  query.status = null;
  query.itemId = null;
  query.deptId = null;
  query.openOnly = false;
  query.overdueOnly = false;
  query.dateRange = null;
  query.pageNum = 1;
  loadList();
};
// 预约
const applyVisible = ref(false);
const applySaving = ref(false);
const applyForm = reactive({
  id: null,
  itemId: null,
  patientId: null,
  patientName: '',
  deptId: null,
  planSurgeryDate: '',
  remark: '',
});
const openApplyCreate = () => {
  Object.assign(applyForm, {
    id: null, itemId: null, patientId: null, patientName: '', deptId: null,
    planSurgeryDate: '', remark: '',
  });
  applyVisible.value = true;
};
const openApplyEdit = async (row) => {
  const res = await getDaySurgeryDetail(row.id);
  const d = res?.data || row;
  Object.assign(applyForm, {
    id: d.id, itemId: d.itemId ?? null, patientId: d.patientId ?? null, patientName: d.patientName || '',
    deptId: d.deptId ?? null, planSurgeryDate: (d.planSurgeryDate || '').slice(0, 10), remark: d.remark || '',
  });
  applyVisible.value = true;
};
const onPatientSelect = (p) => {
  applyForm.patientId = p?.id ?? null;
  applyForm.patientName = p?.name || p?.patientName || '';
};
const saveApply = async () => {
  if (!applyForm.itemId) {
    ElMessage.warning('请选择准入术式');
    return;
  }
  if (!applyForm.patientId) {
    ElMessage.warning('请选择患者');
    return;
  }
  if (!applyForm.planSurgeryDate) {
    ElMessage.warning('请选择计划手术日期');
    return;
  }
  applySaving.value = true;
  try {
    const res = await daySurgeryApplyUpsert({
      id: applyForm.id || undefined,
      itemId: applyForm.itemId,
      patientId: applyForm.patientId,
      deptId: applyForm.deptId || undefined,
      planSurgeryDate: applyForm.planSurgeryDate,
      remark: applyForm.remark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success(applyForm.id ? '修改成功' : `预约登记成功，单号 ${res.data?.applyNo || ''}`);
      applyVisible.value = false;
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '保存失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    applySaving.value = false;
  }
};
// 术前评估
const evalVisible = ref(false);
const evalRow = ref(null);
const evalForm = reactive({evalResult: 1, evalRemark: ''});
const evalLoading = ref(false);
const openEval = (row) => {
  evalRow.value = row;
  evalForm.evalResult = 1;
  evalForm.evalRemark = row.evalRemark || '';
  evalVisible.value = true;
};
const submitEval = async () => {
  if (!evalForm.evalRemark.trim()) {
    ElMessage.warning('请填写评估意见（禁忌筛查结论要留痕）');
    return;
  }
  evalLoading.value = true;
  try {
    const res = await evaluateDaySurgery({
      id: evalRow.value.id, evalResult: evalForm.evalResult, evalRemark: evalForm.evalRemark.trim(),
    });
    if (res.code === 200) {
      ElMessage.success(evalForm.evalResult === 1 ? '评估通过，可安排手术' : '评估不通过，不得安排日间手术');
      evalVisible.value = false;
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '评估失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('评估失败');
  } finally {
    evalLoading.value = false;
  }
};
// 安排
const arrangeVisible = ref(false);
const arrangeRow = ref(null);
const arrangeForm = reactive({
  surgeryTime: '', operatingRoom: '', seqNo: null,
  anesthesiaType: null, surgeon: '',
});
const arrangeLoading = ref(false);
const openArrange = (row) => {
  arrangeRow.value = row;
  arrangeForm.surgeryTime = '';
  arrangeForm.operatingRoom = '';
  arrangeForm.seqNo = null;
  arrangeForm.anesthesiaType = null;
  arrangeForm.surgeon = row.doctorName || '';
  arrangeVisible.value = true;
};
const submitArrange = async () => {
  if (!arrangeForm.surgeryTime) {
    ElMessage.warning('请选择手术时间');
    return;
  }
  if (!arrangeForm.operatingRoom.trim()) {
    ElMessage.warning('请填写手术间');
    return;
  }
  if (!arrangeForm.anesthesiaType) {
    ElMessage.warning('请选择麻醉方式');
    return;
  }
  if (!arrangeForm.surgeon.trim()) {
    ElMessage.warning('请填写主刀医生');
    return;
  }
  arrangeLoading.value = true;
  try {
    const res = await arrangeDaySurgery({
      id: arrangeRow.value.id,
      surgeryTime: arrangeForm.surgeryTime.length === 16 ? `${arrangeForm.surgeryTime}:00` : arrangeForm.surgeryTime,
      operatingRoom: arrangeForm.operatingRoom.trim(),
      seqNo: arrangeForm.seqNo ?? undefined,
      anesthesiaType: arrangeForm.anesthesiaType,
      surgeon: arrangeForm.surgeon.trim(),
    });
    if (res.code === 200) {
      ElMessage.success('已安排');
      arrangeVisible.value = false;
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '安排失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('安排失败');
  } finally {
    arrangeLoading.value = false;
  }
};
// 完成手术
const doFinish = async (row) => {
  try {
    const res = await finishDaySurgery({id: row.id});
    if (res.code === 200) {
      ElMessage.success('手术已完成，进入术后观察');
      loadList();
      loadStats();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
// 出院
const disVisible = ref(false);
const disRow = ref(null);
const disForm = reactive({leaveType: 1, dischargeTime: '', dischargeRemark: ''});
const disLoading = ref(false);
const openDischarge = (row) => {
  disRow.value = row;
  disForm.leaveType = 1;
  disForm.dischargeTime = '';
  disForm.dischargeRemark = '';
  disVisible.value = true;
};
const submitDischarge = async () => {
  disLoading.value = true;
  try {
    const res = await dischargeDaySurgery({
      id: disRow.value.id,
      leaveType: disForm.leaveType,
      dischargeTime: disForm.dischargeTime
          ? (disForm.dischargeTime.length === 16 ? `${disForm.dischargeTime}:00` : disForm.dischargeTime)
          : undefined,
      dischargeRemark: disForm.dischargeRemark.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('离院已登记，请在 24 小时内完成随访');
      disVisible.value = false;
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '登记失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('登记失败');
  } finally {
    disLoading.value = false;
  }
};
// 转住院
const trVisible = ref(false);
const trRow = ref(null);
const trForm = reactive({transferAdmissionId: '', transferRemark: ''});
const trLoading = ref(false);
const openTransfer = (row) => {
  trRow.value = row;
  trForm.transferAdmissionId = '';
  trForm.transferRemark = '';
  trVisible.value = true;
};
const submitTransfer = async () => {
  if (!trForm.transferAdmissionId) {
    ElMessage.warning('请填写转住院的住院号ID');
    return;
  }
  if (!trForm.transferRemark.trim()) {
    ElMessage.warning('请填写转住院原因');
    return;
  }
  trLoading.value = true;
  try {
    const res = await transferDaySurgeryToIpd({
      id: trRow.value.id,
      transferAdmissionId: trForm.transferAdmissionId,
      transferRemark: trForm.transferRemark.trim(),
    });
    if (res.code === 200) {
      ElMessage.success('已转住院');
      trVisible.value = false;
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '转住院失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('转住院失败');
  } finally {
    trLoading.value = false;
  }
};
// 随访
const foVisible = ref(false);
const foRow = ref(null);
const foForm = reactive({followType: 1, result: 1, content: ''});
const foLoading = ref(false);
const openFollow = (row) => {
  foRow.value = row;
  foForm.followType = 1;
  foForm.result = 1;
  foForm.content = '';
  foVisible.value = true;
};
const submitFollow = async () => {
  foLoading.value = true;
  try {
    const res = await followDaySurgery({
      id: foRow.value.id, followType: foForm.followType, result: foForm.result,
      content: foForm.content.trim() || undefined,
    });
    if (res.code === 200) {
      ElMessage.success('随访已登记');
      foVisible.value = false;
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '随访登记失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('随访登记失败');
  } finally {
    foLoading.value = false;
  }
};
// 取消
const cancelVisible = ref(false);
const cancelRow = ref(null);
const cancelReason = ref('');
const cancelLoading = ref(false);
const openCancel = (row) => {
  cancelRow.value = row;
  cancelReason.value = '';
  cancelVisible.value = true;
};
const submitCancel = async () => {
  if (!cancelReason.value.trim()) {
    ElMessage.warning('请填写取消原因');
    return;
  }
  cancelLoading.value = true;
  try {
    const res = await cancelDaySurgery({id: cancelRow.value.id, content: cancelReason.value.trim()});
    if (res.code === 200) {
      ElMessage.success('已取消');
      cancelVisible.value = false;
      loadList();
      loadStats();
    } else {
      ElMessage.error(res.message || '取消失败');
    }
  } catch (e) {
    console.error(e);
    ElMessage.error('取消失败');
  } finally {
    cancelLoading.value = false;
  }
};
const remove = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除登记单 ${row.applyNo}？`, '删除确认', {type: 'warning'});
  } catch {
    return;
  }
  try {
    const res = await deleteDaySurgery(row.id);
    if (res.code === 200) {
      ElMessage.success('删除成功');
      loadList();
      loadStats();
    } else
      ElMessage.error(res.message || '删除失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
// 详情
const detailVisible = ref(false);
const detail = ref(null);
const openDetail = async (row) => {
  const res = await getDaySurgeryDetail(row.id);
  if (res.code === 200) {
    detail.value = res.data;
    detailVisible.value = true;
  } else
    ElMessage.error(res.message || '加载详情失败');
};
// ---------------- 公共 ----------------
const loadStats = async () => {
  try {
    const res = await getDaySurgeryStat();
    if (res.code === 200 && res.data)
      Object.assign(stats, res.data);
  } catch (e) {
    console.error('加载统计失败', e);
  }
};
const loadItemOptions = async () => {
  try {
    const res = await getDaySurgeryItemList();
    if (res.code === 200)
      itemOptions.value = res.data || [];
  } catch (e) {
    console.error('加载术式下拉失败', e);
  }
};
const loadDicts = async () => {
  try {
    // 6 张字典超过单次 5 个 type 上限 → 分两批
    const a = await getDictDataMapList([
      DICT_TYPE.DAY_SURGERY_STATUS, DICT_TYPE.DAY_SURGERY_ANESTHESIA,
      DICT_TYPE.DAY_SURGERY_LEAVE_TYPE, DICT_TYPE.DAY_SURGERY_EVAL_RESULT,
      DICT_TYPE.DAY_SURGERY_FOLLOW_RESULT,
    ].join(','));
    const b = await getDictDataMapList(DICT_TYPE.DAY_SURGERY_FOLLOW_TYPE);
    const da = a?.data || {};
    const db = b?.data || {};
    statusDict.value = da[DICT_TYPE.DAY_SURGERY_STATUS] || [];
    anesDict.value = da[DICT_TYPE.DAY_SURGERY_ANESTHESIA] || [];
    leaveDict.value = da[DICT_TYPE.DAY_SURGERY_LEAVE_TYPE] || [];
    evalDict.value = da[DICT_TYPE.DAY_SURGERY_EVAL_RESULT] || [];
    followResultDict.value = da[DICT_TYPE.DAY_SURGERY_FOLLOW_RESULT] || [];
    followTypeDict.value = db[DICT_TYPE.DAY_SURGERY_FOLLOW_TYPE] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const loadDepts = async () => {
  try {
    const res = await getDepartmentSelectList({});
    if (res.code === 200)
      deptOptions.value = res.data || [];
  } catch (e) {
    console.error('加载科室失败', e);
  }
};
onMounted(async () => {
  await Promise.all([loadDicts(), loadDepts(), loadItemOptions()]);
  await loadList();
  await loadStats();
});
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  padding: 14px 16px;
}

.stat-row {
  display: flex;
  gap: 28px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.stat-value.warn {
  color: #e6a23c;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-value.ok {
  color: #67c23a;
}

.flow-title {
  margin: 16px 0 10px;
  font-weight: 600;
  color: #303133;
}

.flow-item {
  display: flex;
  gap: 10px;
  align-items: baseline;
}

.flow-op {
  font-size: 12px;
  color: #409eff;
}

.flow-content {
  margin-top: 4px;
  color: #606266;
  white-space: pre-wrap;
}

.hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.hint-line {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>
