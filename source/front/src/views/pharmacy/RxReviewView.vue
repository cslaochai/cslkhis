<template>
  <div class="rx-review-page">
    <!-- sql/191：515「处方点评」与 516「处方公示」合并（公示是点评的输出环节，515 名下本就有 pharmacy:rxReview:publicity 按钮码），516 置 is_visible=0 退出侧栏但保留 pharmacy:rxPublicity:list 权限码。 -->
    <el-tabs v-model="mergedTab" class="merged-tabs">
      <el-tab-pane label="处方点评" name="review">
        <!-- 评审口径统计卡：点评率 / 不合理处方率 / 超常处方 -->
        <el-card shadow="never" class="stat-card">
          <div class="stat-head">
            <span class="stat-title">处方点评统计</span>
            <div data-testid="stats-month">
              <el-date-picker
                v-model="statsMonth"
                type="month"
                placeholder="选择月份"
                value-format="YYYY-MM"
                :clearable="false"
                style="width: 140px"
                @change="loadStats" />
            </div>
          </div>
          <div class="stat-items">
            <div class="stat-item">
              <div class="stat-value">{{ stats.totalPrescriptions ?? '-' }}</div>
              <div class="stat-label">期内处方总数</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.reviewedCount ?? '-' }}</div>
              <div class="stat-label">已点评处方</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.reviewRate ?? '-' }}%</div>
              <div class="stat-label">处方点评率</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.unreasonableCount ?? '-' }}</div>
              <div class="stat-label">不合理处方（{{ stats.unreasonableRate ?? '-' }}%）</div>
            </div>
            <div class="stat-item">
              <div class="stat-value stat-value-danger">{{ stats.abnormalCount ?? '-' }}</div>
              <div class="stat-label">超常处方</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.publicityCount ?? '-' }}</div>
              <div class="stat-label">已公示</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ stats.pendingCount ?? '-' }}</div>
              <div class="stat-label">待点评</div>
            </div>
          </div>
        </el-card>

        <el-tabs v-model="activeTab" class="main-tabs">
          <!-- ============ tab1 点评批次 ============ -->
          <el-tab-pane label="点评批次" name="batch">
            <div class="toolbar">
              <el-input
                v-model="batchQuery.keyword"
                placeholder="批次号 / 批次名称"
                clearable
                style="width: 200px"
                @keyup.enter="loadBatches"
                @clear="loadBatches" />
              <el-select v-model="batchQuery.status" placeholder="状态" clearable style="width: 120px" @change="loadBatches">
                <el-option v-for="(text, code) in BATCH_STATUS" :key="code" :label="text" :value="Number(code)" />
              </el-select>
              <el-button type="primary" @click="loadBatches">查询</el-button>
              <div class="toolbar-right">
                <el-button v-perm="'pharmacy:rxReview:add'" type="primary" data-testid="btn-new-batch" @click="openBatchDialog()">
                  新建点评批次
                </el-button>
              </div>
            </div>
            <el-table :data="batchRows" border stripe v-loading="batchLoading">
              <el-table-column prop="batchNo" label="批次号" width="180" />
              <el-table-column prop="batchName" label="批次名称" min-width="200" show-overflow-tooltip />
              <el-table-column label="类型" width="150">
                <template #default="{ row }">
                  {{ REVIEW_TYPE[row.reviewType] || '未知(' + row.reviewType + ')' }}
                  <span v-if="row.reviewType === 2">（{{ row.specialty }}）</span>
                </template>
              </el-table-column>
              <el-table-column label="处方日期范围" width="200">
                <template #default="{ row }">{{ row.dateStart }} ~ {{ row.dateEnd }}</template>
              </el-table-column>
              <el-table-column prop="sampleCount" label="抽样数" width="80" align="center" />
              <el-table-column prop="reviewedCount" label="已点评" width="80" align="center" />
              <el-table-column label="状态" width="90" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.status === 2 ? 'info' : 'primary'">{{ BATCH_STATUS[row.status] || '未知(' + row.status + ')' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="reviewerName" label="点评人" width="100" />
              <el-table-column prop="createTime" label="创建时间" width="170" />
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button link type="primary" @click="enterBatch(row)">进入点评</el-button>
                  <el-button
                    v-if="row.status === 1"
                    v-perm="'pharmacy:rxReview:add'"
                    link
                    type="warning"
                    @click="doCompleteBatch(row)">完成批次</el-button>
                  <el-button v-perm="'pharmacy:rxReview:delete'" link type="danger" @click="doDeleteBatch(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="pager"
              layout="total, sizes, prev, pager, next"
              :total="batchTotal"
              :page-sizes="PAGE_SIZES"
              v-model:current-page="batchQuery.pageNum"
              v-model:page-size="batchQuery.pageSize"
              @current-change="loadBatches"
              @size-change="batchQuery.pageNum = 1; loadBatches()" />
          </el-tab-pane>

          <!-- ============ tab2 点评明细 ============ -->
          <el-tab-pane label="点评明细" name="item">
            <div class="toolbar">
              <el-select
                v-model="itemQuery.batchId"
                placeholder="选择点评批次"
                style="width: 260px"
                @change="loadItems">
                <el-option
                  v-for="b in batchOptions"
                  :key="b.id"
                  :label="b.batchNo + ' ' + b.batchName"
                  :value="b.id" />
              </el-select>
              <el-input
                v-model="itemQuery.prescriptionNo"
                placeholder="处方号"
                clearable
                style="width: 160px"
                @keyup.enter="loadItems"
                @clear="loadItems" />
              <el-input
                v-model="itemQuery.doctorName"
                placeholder="医生姓名"
                clearable
                style="width: 130px"
                @keyup.enter="loadItems"
                @clear="loadItems" />
              <el-select v-model="itemQuery.reviewStatus" placeholder="点评状态" clearable style="width: 120px" @change="loadItems">
                <el-option v-for="(text, code) in REVIEW_STATUS" :key="code" :label="text" :value="Number(code)" />
              </el-select>
              <el-select v-model="itemQuery.reviewResult" placeholder="结论" clearable style="width: 150px" @change="loadItems">
                <el-option v-for="(text, code) in REVIEW_RESULT" :key="code" :label="text" :value="Number(code)" />
              </el-select>
              <el-button type="primary" @click="loadItems">查询</el-button>
              <div class="toolbar-right">
                <el-button
                  v-perm="'pharmacy:rxReview:add'"
                  data-testid="btn-add-by-no"
                  @click="addPromptVisible = true">按处方号补录</el-button>
                <el-button
                  v-perm="'pharmacy:rxReview:publicity'"
                  type="danger"
                  :disabled="!selectedItems.length"
                  data-testid="btn-publicity"
                  @click="doPublicitySelected">公示所选（{{ selectedItems.length }}）</el-button>
                <el-button v-perm="'pharmacy:rxReview:export'" @click="doExport">导出台账</el-button>
              </div>
            </div>
            <el-table :data="itemRows" border stripe v-loading="itemLoading" @selection-change="onItemSelectionChange">
              <el-table-column type="selection" width="42" :selectable="itemSelectable" />
              <el-table-column prop="prescriptionNo" label="处方号" width="170" />
              <el-table-column prop="patientName" label="患者" width="90" />
              <el-table-column prop="doctorName" label="医生" width="100" />
              <el-table-column prop="deptName" label="科室" width="130" show-overflow-tooltip />
              <el-table-column prop="visitDate" label="就诊日期" width="110" />
              <el-table-column prop="totalAmount" label="金额" width="90" align="right" />
              <el-table-column label="点评结论" width="130">
                <template #default="{ row }">
                  <el-tag v-if="row.reviewResult" :type="RESULT_TAG_TYPE[row.reviewResult]">
                    {{ resultText(row.reviewResult) }}
                  </el-tag>
                  <span v-else class="muted">待点评</span>
                </template>
              </el-table-column>
              <el-table-column label="问题项" min-width="200" show-overflow-tooltip>
                <template #default="{ row }">{{ problemTypesText(row.problemTypes) }}</template>
              </el-table-column>
              <el-table-column label="公示" width="90" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.publicityStatus === 1" type="danger">已公示</el-tag>
                  <span v-else class="muted">—</span>
                </template>
              </el-table-column>
              <el-table-column prop="reviewerName" label="点评人" width="100" />
              <el-table-column label="操作" width="130" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-perm="'pharmacy:rxReview:add'"
                    link
                    type="primary"
                    :disabled="row.publicityStatus === 1"
                    @click="openReviewDialog(row)">点评</el-button>
                  <el-button
                    v-if="canPublicity(row)"
                    v-perm="'pharmacy:rxReview:publicity'"
                    link
                    type="danger"
                    @click="doPublicity(row)">公示</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="pager"
              layout="total, sizes, prev, pager, next"
              :total="itemTotal"
              :page-sizes="PAGE_SIZES"
              v-model:current-page="itemQuery.pageNum"
              v-model:page-size="itemQuery.pageSize"
              @current-change="loadItems"
              @size-change="itemQuery.pageNum = 1; loadItems()" />
          </el-tab-pane>

          <!-- ============ tab3 医师约谈 ============ -->
          <el-tab-pane label="医师约谈" name="talk">
            <div class="toolbar">
              <el-input
                v-model="talkQuery.keyword"
                placeholder="医师姓名 / 约谈编号"
                clearable
                style="width: 200px"
                @keyup.enter="loadTalks"
                @clear="loadTalks" />
              <el-select v-model="talkQuery.talkType" placeholder="约谈类型" clearable style="width: 140px" @change="loadTalks">
                <el-option v-for="(text, code) in TALK_TYPE" :key="code" :label="text" :value="Number(code)" />
              </el-select>
              <el-select v-model="talkQuery.rectifyStatus" placeholder="整改状态" clearable style="width: 120px" @change="loadTalks">
                <el-option v-for="(text, code) in RECTIFY_STATUS" :key="code" :label="text" :value="Number(code)" />
              </el-select>
              <el-button type="primary" @click="loadTalks">查询</el-button>
              <div class="toolbar-right">
                <el-button v-perm="'pharmacy:rxReview:talk'" type="primary" data-testid="btn-new-talk" @click="openTalkDialog()">
                  登记约谈
                </el-button>
              </div>
            </div>
            <el-table :data="talkRows" border stripe v-loading="talkLoading">
              <el-table-column prop="talkNo" label="约谈编号" width="170" />
              <el-table-column prop="doctorName" label="被约谈医师" width="110" />
              <el-table-column prop="deptName" label="科室" width="130" show-overflow-tooltip />
              <el-table-column label="约谈类型" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.talkType >= 3 ? 'danger' : 'warning'">{{ TALK_TYPE[row.talkType] || '未知(' + row.talkType + ')' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="talkTime" label="约谈时间" width="170" />
              <el-table-column prop="talkerName" label="约谈人" width="100" />
              <el-table-column prop="talkerOrg" label="约谈部门" width="110" />
              <el-table-column prop="relatedCount" label="关联不合理处方" width="130" align="center" />
              <el-table-column label="整改" width="100" align="center">
                <template #default="{ row }">
                  <el-tag :type="row.rectifyStatus === 2 ? 'success' : 'info'">{{ RECTIFY_STATUS[row.rectifyStatus] || '未知(' + row.rectifyStatus + ')' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="医师确认" width="150">
                <template #default="{ row }">
                  <template v-if="row.doctorConfirm === 1">
                    <el-tag type="success">{{ row.doctorConfirmBy }}</el-tag>
                  </template>
                  <span v-else class="muted">未确认</span>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button
                    v-if="row.doctorConfirm !== 1"
                    v-perm="'pharmacy:rxReview:talk'"
                    link
                    type="primary"
                    @click="openTalkDialog(row)">编辑</el-button>
                  <el-button
                    v-if="row.doctorConfirm !== 1"
                    v-perm="'pharmacy:rxReview:talk'"
                    link
                    type="success"
                    @click="doConfirmTalk(row)">医师签字确认</el-button>
                  <el-button
                    v-if="row.doctorConfirm !== 1"
                    v-perm="'pharmacy:rxReview:delete'"
                    link
                    type="danger"
                    @click="doDeleteTalk(row)">删除</el-button>
                  <el-button link type="info" @click="showTalkDetail(row)">详情</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="pager"
              layout="total, sizes, prev, pager, next"
              :total="talkTotal"
              :page-sizes="PAGE_SIZES"
              v-model:current-page="talkQuery.pageNum"
              v-model:page-size="talkQuery.pageSize"
              @current-change="loadTalks"
              @size-change="talkQuery.pageNum = 1; loadTalks()" />
          </el-tab-pane>
        </el-tabs>

        <!-- ============ 新建/编辑批次 ============ -->
        <el-dialog v-model="batchDialogVisible" :title="batchForm.id ? '编辑批次' : '新建点评批次'" width="520px">
          <el-form :model="batchForm" label-width="110px">
            <el-form-item label="批次名称" required>
              <el-input v-model="batchForm.batchName" placeholder="如：2026年9月门诊处方专项点评" data-testid="batch-name" />
            </el-form-item>
            <el-form-item label="点评类型" required>
              <el-radio-group v-model="batchForm.reviewType" :disabled="!!batchForm.id">
                <el-radio :value="1">常规点评</el-radio>
                <el-radio :value="2">专项点评</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="batchForm.reviewType === 2" label="专项主题" required>
              <el-input v-model="batchForm.specialty" placeholder="如：抗菌药物 / 中药注射剂 / 辅助用药" />
            </el-form-item>
            <el-form-item label="处方日期范围" required>
              <div data-testid="batch-date-range" style="width: 100%">
                <el-date-picker
                  v-model="batchDateRange"
                  type="daterange"
                  value-format="YYYY-MM-DD"
                  start-placeholder="起"
                  end-placeholder="止"
                  :disabled="!!batchForm.id"
                  style="width: 100%" />
              </div>
            </el-form-item>
            <el-form-item v-if="!batchForm.id" label="抽样张数" required>
              <el-input-number v-model="batchForm.sampleCount" :min="1" :max="2000" data-testid="batch-sample-count" />
              <div class="form-tip">规范要求：每月抽查门急诊处方不少于 100 张（或总处方量 1‰）</div>
            </el-form-item>
            <el-form-item label="备注">
              <el-input v-model="batchForm.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="batchDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveBatch">保存</el-button>
          </template>
        </el-dialog>

        <!-- ============ 提交点评 ============ -->
        <el-dialog v-model="reviewDialogVisible" title="处方点评" width="680px">
          <template v-if="reviewRow">
            <el-descriptions :column="2" border size="small" class="review-desc">
              <el-descriptions-item label="处方号">{{ reviewRow.prescriptionNo }}</el-descriptions-item>
              <el-descriptions-item label="患者">{{ reviewRow.patientName }}</el-descriptions-item>
              <el-descriptions-item label="医生 / 科室">{{ reviewRow.doctorName }} / {{ reviewRow.deptName }}</el-descriptions-item>
              <el-descriptions-item label="就诊日期">{{ reviewRow.visitDate }}</el-descriptions-item>
              <el-descriptions-item label="诊断" :span="2">{{ reviewRow.diagnosis || '—' }}</el-descriptions-item>
            </el-descriptions>
            <div class="drug-list">
              <div class="drug-list-title">处方药品（{{ (reviewRow.drugDetails || []).length }} 种）</div>
              <div v-for="(line, i) in reviewRow.drugDetails || []" :key="i" class="drug-line">{{ line }}</div>
            </div>
            <el-form label-width="90px" class="review-form">
              <el-form-item label="点评结论" required>
                <el-radio-group v-model="reviewForm.reviewResult">
                  <el-radio :value="1">合理处方</el-radio>
                  <el-radio :value="2">不规范处方</el-radio>
                  <el-radio :value="3">用药不适宜</el-radio>
                  <el-radio :value="4">超常处方</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="reviewForm.reviewResult && reviewForm.reviewResult !== 1" label="问题项" required>
                <el-checkbox-group v-model="reviewForm.problemCodes">
                  <el-checkbox v-for="p in (PROBLEM_GROUPS[reviewForm.reviewResult] || [])" :key="p.code" :value="p.code">
                    {{ p.label }}
                  </el-checkbox>
                </el-checkbox-group>
              </el-form-item>
              <el-form-item
                v-if="reviewForm.reviewResult && reviewForm.reviewResult !== 1"
                label="点评意见"
                required>
                <el-input
                  v-model="reviewForm.reviewOpinion"
                  type="textarea"
                  :rows="3"
                  maxlength="500"
                  show-word-limit
                  placeholder="说明具体问题与依据" />
              </el-form-item>
            </el-form>
          </template>
          <template #footer>
            <el-button @click="reviewDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveReview">提交点评</el-button>
          </template>
        </el-dialog>

        <!-- ============ 按处方号补录 ============ -->
        <el-dialog v-model="addPromptVisible" title="按处方号补录" width="420px">
          <el-form label-width="90px">
            <el-form-item label="处方号" required>
              <el-input v-model="addByNo" placeholder="输入处方号" data-testid="add-by-no-input" @keyup.enter="doAddByNo" />
            </el-form-item>
          </el-form>
          <div class="form-tip">处方须为已审核/已发药状态，且未被任何批次收录（一处方只点评一次）</div>
          <template #footer>
            <el-button @click="addPromptVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="doAddByNo">补录</el-button>
          </template>
        </el-dialog>

        <!-- ============ 登记约谈 ============ -->
        <el-dialog v-model="talkDialogVisible" :title="talkForm.id ? '编辑约谈记录' : '登记医师约谈'" width="640px">
          <el-form :model="talkForm" label-width="110px">
            <el-form-item label="被约谈医师" required>
              <el-input v-model="talkForm.doctorName" placeholder="医师姓名" data-testid="talk-doctor" @blur="loadRelatedItems" />
            </el-form-item>
            <el-form-item label="约谈类型" required>
              <el-select v-model="talkForm.talkType" style="width: 100%">
                <el-option v-for="(text, code) in TALK_TYPE" :key="code" :label="text" :value="Number(code)" />
              </el-select>
            </el-form-item>
            <el-form-item label="约谈时间" required>
              <div data-testid="talk-time" style="width: 100%">
                <el-date-picker
                  v-model="talkForm.talkTime"
                  type="datetime"
                  value-format="YYYY-MM-DD HH:mm:ss"
                  placeholder="约谈时间"
                  style="width: 100%" />
              </div>
            </el-form-item>
            <el-form-item label="约谈人 / 部门">
              <div class="inline-pair">
                <el-input v-model="talkForm.talkerName" placeholder="约谈人（缺省当前用户）" />
                <el-input v-model="talkForm.talkerOrg" placeholder="约谈部门（医务科/药学部）" />
              </div>
            </el-form-item>
            <el-form-item label="关联不合理处方">
              <el-select
                v-model="talkForm.relatedReviewIds"
                multiple
                filterable
                clearable
                placeholder="输入医师姓名后加载其不合理处方（可空=独立约谈）"
                style="width: 100%"
                :loading="relatedLoading">
                <el-option
                  v-for="it in relatedItems"
                  :key="it.id"
                  :label="`${it.prescriptionNo}（${resultText(it.reviewResult)}，${it.visitDate}）`"
                  :value="it.id" />
              </el-select>
              <div class="form-tip">约谈依据：仅可关联不合理处方，且必须同属被约谈医师</div>
            </el-form-item>
            <el-form-item label="问题摘要">
              <el-input v-model="talkForm.problemSummary" type="textarea" :rows="2" maxlength="500" placeholder="约谈事由" />
            </el-form-item>
            <el-form-item label="约谈内容">
              <el-input v-model="talkForm.talkContent" type="textarea" :rows="3" maxlength="1000" />
            </el-form-item>
            <el-form-item label="整改要求">
              <el-input v-model="talkForm.rectifyRequire" type="textarea" :rows="2" maxlength="500" />
            </el-form-item>
            <el-form-item label="整改状态">
              <el-radio-group v-model="talkForm.rectifyStatus">
                <el-radio :value="1">待整改</el-radio>
                <el-radio :value="2">已整改</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item v-if="talkForm.rectifyStatus === 2" label="整改说明">
              <el-input v-model="talkForm.rectifyRemark" type="textarea" :rows="2" maxlength="500" />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="talkDialogVisible = false">取消</el-button>
            <el-button type="primary" :loading="saving" @click="saveTalk">保存</el-button>
          </template>
        </el-dialog>

        <!-- ============ 约谈详情（只读） ============ -->
        <el-dialog v-model="talkDetailVisible" title="约谈记录详情" width="640px">
          <template v-if="talkDetailRow">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="约谈编号">{{ talkDetailRow.talkNo }}</el-descriptions-item>
              <el-descriptions-item label="约谈类型">{{ TALK_TYPE[talkDetailRow.talkType] }}</el-descriptions-item>
              <el-descriptions-item label="被约谈医师">{{ talkDetailRow.doctorName }}</el-descriptions-item>
              <el-descriptions-item label="科室">{{ talkDetailRow.deptName || '—' }}</el-descriptions-item>
              <el-descriptions-item label="约谈时间">{{ talkDetailRow.talkTime }}</el-descriptions-item>
              <el-descriptions-item label="约谈人">{{ talkDetailRow.talkerName }}（{{ talkDetailRow.talkerOrg || '—' }}）</el-descriptions-item>
              <el-descriptions-item label="问题摘要" :span="2">{{ talkDetailRow.problemSummary || '—' }}</el-descriptions-item>
              <el-descriptions-item label="约谈内容" :span="2">{{ talkDetailRow.talkContent || '—' }}</el-descriptions-item>
              <el-descriptions-item label="整改要求" :span="2">{{ talkDetailRow.rectifyRequire || '—' }}</el-descriptions-item>
              <el-descriptions-item label="整改状态" :span="2">
                {{ RECTIFY_STATUS[talkDetailRow.rectifyStatus] }}
                <span v-if="talkDetailRow.rectifyRemark">：{{ talkDetailRow.rectifyRemark }}</span>
              </el-descriptions-item>
              <el-descriptions-item label="关联处方" :span="2">
                <template v-if="(talkDetailRow.relatedItems || []).length">
                  <div v-for="it in talkDetailRow.relatedItems" :key="it.id" class="drug-line">
                    {{ it.prescriptionNo }}（{{ resultText(it.reviewResult) }}，{{ it.visitDate }}）
                  </div>
                </template>
                <span v-else>无（独立约谈）</span>
              </el-descriptions-item>
              <el-descriptions-item label="医师确认" :span="2">
                <template v-if="talkDetailRow.doctorConfirm === 1">
                  {{ talkDetailRow.doctorConfirmBy }} 于 {{ talkDetailRow.doctorConfirmTime }} 签字确认
                </template>
                <span v-else>未确认</span>
              </el-descriptions-item>
            </el-descriptions>
          </template>
        </el-dialog>
      </el-tab-pane>
      <el-tab-pane label="超常处方公示" name="publicity" lazy>
        <RxPublicityView />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import RxPublicityView from './RxPublicityView.vue'
import {
  BATCH_STATUS,
  PROBLEM_GROUPS,
  PUBLICITY_STATUS,
  RECTIFY_STATUS,
  RESULT_TAG_TYPE,
  REVIEW_RESULT,
  REVIEW_STATUS,
  REVIEW_TYPE,
  TALK_TYPE,
  problemTypesText,
  resultText,
  UNREASONABLE_RESULTS
} from '@/lib/rxReview'
import { DEFAULT_PAGE_SIZE, PAGE_SIZES } from '@/lib/pagination'
import {
  addRxItemByNo,
  completeRxBatch,
  confirmRxTalk,
  deleteRxBatchById,
  deleteRxTalkById,
  exportRxItemCsv,
  getRxReviewStats,
  listRxBatchPage,
  listRxItemPage,
  listRxTalkPage,
  publicityRxItems,
  upsertRxBatch,
  upsertRxItem,
  upsertRxTalk
} from '@/api/pharmacy'

const activeTab = ref('batch')
const saving = ref(false)

// ---------- 统计 ----------
const statsMonth = ref(new Date().toISOString().slice(0, 7))
const stats = ref({})
async function loadStats() {
  const res = await getRxReviewStats(statsMonth.value || undefined)
  stats.value = res?.data || {}
}

// ---------- 批次 ----------
const batchQuery = reactive({ keyword: '', status: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const batchRows = ref([])
const batchTotal = ref(0)
const batchLoading = ref(false)
const batchOptions = ref([])

async function loadBatches() {
  batchLoading.value = true
  try {
    const res = await listRxBatchPage({ ...batchQuery })
    batchRows.value = res?.data?.records || []
    batchTotal.value = Number(res?.data?.total || 0)
  } finally {
    batchLoading.value = false
  }
}

// 批次下拉候选（一次性抓全量，供明细 tab 选择；不是分页查询）
async function loadBatchOptions() {
  const res = await listRxBatchPage({ pageNum: 1, pageSize: 200 })
  batchOptions.value = res?.data?.records || []
}

function enterBatch(row) {
  itemQuery.batchId = row.id
  itemQuery.prescriptionNo = ''
  itemQuery.doctorName = ''
  itemQuery.reviewStatus = null
  itemQuery.reviewResult = null
  activeTab.value = 'item'
  loadItems()
}

const batchDialogVisible = ref(false)
const batchDateRange = ref([])
const batchForm = reactive({ id: null, batchName: '', reviewType: 1, specialty: '', sampleCount: 100, remark: '' })

function openBatchDialog(row) {
  if (row) {
    Object.assign(batchForm, { id: row.id, batchName: row.batchName, reviewType: row.reviewType, specialty: row.specialty || '', sampleCount: row.sampleCount, remark: row.remark || '' })
    batchDateRange.value = [row.dateStart, row.dateEnd]
  } else {
    Object.assign(batchForm, { id: null, batchName: '', reviewType: 1, specialty: '', sampleCount: 100, remark: '' })
    batchDateRange.value = []
  }
  batchDialogVisible.value = true
}

async function saveBatch() {
  if (!batchForm.batchName.trim()) {
    ElMessage.warning('请填写批次名称')
    return
  }
  const payload = {
    id: batchForm.id,
    batchName: batchForm.batchName,
    reviewType: batchForm.reviewType,
    specialty: batchForm.specialty,
    remark: batchForm.remark
  }
  if (!batchForm.id) {
    if (!batchDateRange.value || batchDateRange.value.length !== 2) {
      ElMessage.warning('请选择处方日期范围')
      return
    }
    payload.dateStart = batchDateRange.value[0]
    payload.dateEnd = batchDateRange.value[1]
    payload.sampleCount = batchForm.sampleCount
  }
  saving.value = true
  try {
    const res = await upsertRxBatch(payload)
    ElMessage.success(res?.data?.id && !batchForm.id
      ? `批次已建立，抽样 ${res.data.sampleCount} 张处方待点评`
      : '保存成功')
    batchDialogVisible.value = false
    await loadBatches()
    await loadBatchOptions()
  } finally {
    saving.value = false
  }
}

async function doCompleteBatch(row) {
  await ElMessageBox.confirm(`确认完成批次「${row.batchName}」？完成后不可再点评/补录。`, '完成批次')
  await completeRxBatch(row.id)
  ElMessage.success('批次已完成归档')
  await loadBatches()
  await loadBatchOptions()
}

async function doDeleteBatch(row) {
  await ElMessageBox.confirm(
    `确认删除批次「${row.batchName}」？未点评明细将一并物理删除；已有点评结论的批次不可删。`,
    '删除批次',
    { type: 'warning' }
  )
  await deleteRxBatchById(row.id)
  ElMessage.success('批次已删除')
  await loadBatches()
  await loadBatchOptions()
}

// ---------- 明细 ----------
const itemQuery = reactive({
  batchId: null,
  prescriptionNo: '',
  doctorName: '',
  reviewStatus: null,
  reviewResult: null,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE
})
const itemRows = ref([])
const itemTotal = ref(0)
const itemLoading = ref(false)
const selectedItems = ref([])

function itemSelectable(row) {
  return canPublicity(row)
}

function canPublicity(row) {
  return row.reviewStatus === 1
    && UNREASONABLE_RESULTS.includes(row.reviewResult)
    && row.publicityStatus !== 1
}

async function loadItems() {
  itemLoading.value = true
  try {
    const res = await listRxItemPage({ ...itemQuery })
    itemRows.value = res?.data?.records || []
    itemTotal.value = Number(res?.data?.total || 0)
  } finally {
    itemLoading.value = false
  }
}

function onItemSelectionChange(rows) {
  selectedItems.value = rows
}

async function doPublicity(row) {
  await ElMessageBox.confirm(
    `确认公示处方 ${row.prescriptionNo}（${resultText(row.reviewResult)}）？公示只增不可撤，公示后点评结论不可修改。`,
    '公示处方',
    { type: 'warning' }
  )
  await publicityRxItems([row.id])
  ElMessage.success('已公示')
  await loadItems()
  await loadStats()
}

async function doPublicitySelected() {
  const ids = selectedItems.value.map(r => r.id)
  await ElMessageBox.confirm(`确认公示所选 ${ids.length} 张不合理处方？公示只增不可撤。`, '批量公示', { type: 'warning' })
  const res = await publicityRxItems(ids)
  ElMessage.success(`已公示 ${res?.data ?? ids.length} 张`)
  await loadItems()
  await loadStats()
}

const addPromptVisible = ref(false)
const addByNo = ref('')

async function doAddByNo() {
  if (!itemQuery.batchId) {
    ElMessage.warning('请先选择点评批次')
    return
  }
  if (!addByNo.value.trim()) {
    ElMessage.warning('请输入处方号')
    return
  }
  saving.value = true
  try {
    await addRxItemByNo({ batchId: itemQuery.batchId, prescriptionNo: addByNo.value.trim() })
    ElMessage.success('补录成功')
    addPromptVisible.value = false
    addByNo.value = ''
    await loadItems()
    await loadStats()
  } finally {
    saving.value = false
  }
}

// ---------- 点评 ----------
const reviewDialogVisible = ref(false)
const reviewRow = ref(null)
const reviewForm = reactive({ id: null, reviewResult: null, problemCodes: [], reviewOpinion: '' })

function openReviewDialog(row) {
  reviewRow.value = row
  reviewForm.id = row.id
  reviewForm.reviewResult = row.reviewResult || null
  reviewForm.problemCodes = row.problemTypes ? row.problemTypes.split(',') : []
  reviewForm.reviewOpinion = row.reviewOpinion || ''
  reviewDialogVisible.value = true
}

async function saveReview() {
  if (!reviewForm.reviewResult) {
    ElMessage.warning('请选择点评结论')
    return
  }
  const unreasonable = reviewForm.reviewResult !== 1
  if (unreasonable && !reviewForm.problemCodes.length) {
    ElMessage.warning('不合理处方必须勾选问题项')
    return
  }
  if (unreasonable && !reviewForm.reviewOpinion.trim()) {
    ElMessage.warning('不合理处方必须填写点评意见')
    return
  }
  saving.value = true
  try {
    await upsertRxItem({
      id: reviewForm.id,
      reviewResult: reviewForm.reviewResult,
      problemTypes: unreasonable ? reviewForm.problemCodes : [],
      reviewOpinion: unreasonable ? reviewForm.reviewOpinion : ''
    })
    ElMessage.success('点评已提交')
    reviewDialogVisible.value = false
    await loadItems()
    await loadStats()
    await loadBatches()
  } finally {
    saving.value = false
  }
}

// ---------- 约谈 ----------
const talkQuery = reactive({ keyword: '', talkType: null, rectifyStatus: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE })
const talkRows = ref([])
const talkTotal = ref(0)
const talkLoading = ref(false)
const talkDialogVisible = ref(false)
const talkDetailVisible = ref(false)
const talkDetailRow = ref(null)
const relatedItems = ref([])
const relatedLoading = ref(false)
const talkForm = reactive({
  id: null,
  doctorName: '',
  talkType: 1,
  talkTime: '',
  talkerName: '',
  talkerOrg: '',
  relatedReviewIds: [],
  problemSummary: '',
  talkContent: '',
  rectifyRequire: '',
  rectifyStatus: 1,
  rectifyRemark: ''
})

async function loadTalks() {
  talkLoading.value = true
  try {
    const res = await listRxTalkPage({ ...talkQuery })
    talkRows.value = res?.data?.records || []
    talkTotal.value = Number(res?.data?.total || 0)
  } finally {
    talkLoading.value = false
  }
}

/** 加载该医师已点评的不合理处方（约谈依据候选） */
async function loadRelatedItems() {
  const name = (talkForm.doctorName || '').trim()
  if (!name) {
    relatedItems.value = []
    return
  }
  relatedLoading.value = true
  try {
    const res = await listRxItemPage({ doctorName: name, reviewStatus: 1, pageNum: 1, pageSize: 100 })
    relatedItems.value = (res?.data?.records || []).filter(r => UNREASONABLE_RESULTS.includes(r.reviewResult))
  } finally {
    relatedLoading.value = false
  }
}

function openTalkDialog(row) {
  if (row) {
    Object.assign(talkForm, {
      id: row.id,
      doctorName: row.doctorName,
      talkType: row.talkType,
      talkTime: row.talkTime,
      talkerName: row.talkerName || '',
      talkerOrg: row.talkerOrg || '',
      relatedReviewIds: row.relatedReviewIds ? row.relatedReviewIds.split(',').map(Number) : [],
      problemSummary: row.problemSummary || '',
      talkContent: row.talkContent || '',
      rectifyRequire: row.rectifyRequire || '',
      rectifyStatus: row.rectifyStatus,
      rectifyRemark: row.rectifyRemark || ''
    })
  } else {
    Object.assign(talkForm, {
      id: null,
      doctorName: '',
      talkType: 1,
      talkTime: '',
      talkerName: '',
      talkerOrg: '',
      relatedReviewIds: [],
      problemSummary: '',
      talkContent: '',
      rectifyRequire: '',
      rectifyStatus: 1,
      rectifyRemark: ''
    })
  }
  relatedItems.value = []
  talkDialogVisible.value = true
  if (talkForm.doctorName) {
    loadRelatedItems()
  }
}

async function saveTalk() {
  if (!talkForm.doctorName.trim()) {
    ElMessage.warning('请填写被约谈医师姓名')
    return
  }
  if (!talkForm.talkTime) {
    ElMessage.warning('请选择约谈时间')
    return
  }
  saving.value = true
  try {
    await upsertRxTalk({ ...talkForm, relatedReviewIds: talkForm.relatedReviewIds })
    ElMessage.success(talkForm.id ? '约谈记录已更新' : '约谈记录已登记')
    talkDialogVisible.value = false
    await loadTalks()
  } finally {
    saving.value = false
  }
}

async function doConfirmTalk(row) {
  const { value } = await ElMessageBox.prompt(
    `医师「${row.doctorName}」签字确认约谈记录 ${row.talkNo}。请输入签字医师姓名：`,
    '医师签字确认',
    { inputPattern: /\S+/, inputErrorMessage: '请输入签字医师姓名' }
  )
  await confirmRxTalk(row.id, value.trim())
  ElMessage.success('已记录医师签字确认')
  await loadTalks()
}

function showTalkDetail(row) {
  talkDetailRow.value = row
  talkDetailVisible.value = true
}

async function doDeleteTalk(row) {
  await ElMessageBox.confirm(`确认删除约谈记录 ${row.talkNo}？医师已签字确认的记录不可删除。`, '删除约谈', { type: 'warning' })
  await deleteRxTalkById(row.id)
  ElMessage.success('约谈记录已删除')
  await loadTalks()
}

// ---------- 导出 ----------
async function doExport() {
  const res = await exportRxItemCsv({ ...itemQuery })
  const blob = new Blob([res?.data || ''], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `处方点评台账_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出完成')
}

// 合并页签的当前页。被并页面自带 onMounted 请求，故其页签用 lazy —— 进页不预拉两套数据。
const mergedTab = ref('review')

onMounted(async () => {
  await Promise.all([loadStats(), loadBatches(), loadBatchOptions(), loadItems(), loadTalks()])
})
</script>

<style scoped>
.rx-review-page {
  padding: 16px;
}

.stat-card {
  margin-bottom: 12px;
}

.stat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.stat-title {
  font-weight: 600;
  font-size: 15px;
}

.stat-items {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.stat-item {
  flex: 1;
  min-width: 130px;
  background: var(--el-fill-color-light);
  border-radius: 8px;
  padding: 12px 16px;
  text-align: center;
}

.stat-value {
  font-size: 22px;
  font-weight: 700;
}

.stat-value-danger {
  color: var(--el-color-danger);
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.main-tabs {
  background: var(--el-bg-color);
  border-radius: 8px;
  padding: 4px 16px 12px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin: 8px 0 12px;
}

.toolbar-right {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.pager {
  margin-top: 12px;
  justify-content: flex-end;
}

.muted {
  color: var(--el-text-color-placeholder);
}

.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.review-desc {
  margin-bottom: 12px;
}

.drug-list {
  background: var(--el-fill-color-lighter);
  border-radius: 6px;
  padding: 10px 14px;
  margin-bottom: 12px;
  max-height: 200px;
  overflow: auto;
}

.drug-list-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.drug-line {
  font-size: 13px;
  line-height: 1.8;
}

.review-form {
  margin-top: 4px;
}

.inline-pair {
  display: flex;
  gap: 8px;
  width: 100%;
}

/* 被并页面自带页级留白，嵌进页签后统一由宿主提供，避免双层 padding */
.merged-tabs :deep(.el-tab-pane > .rx-publicity-page) {
  padding: 0;
}

</style>
