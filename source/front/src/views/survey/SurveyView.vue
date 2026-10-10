<template>
  <div class="survey-page">
    <!-- 看板 -->
    <el-card v-loading="statLoading" class="stat-card" shadow="never">
      <div class="stat-row">
        <div class="stat-item">
          <span class="stat-label">发放总数</span>
          <span class="stat-value">{{ stat?.dispatchTotal ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已回收</span>
          <span class="stat-value ok">{{ stat?.recycledCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">回收率</span>
          <span class="stat-value">{{ stat?.recycleRate ?? 0 }}%</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">待推送</span>
          <span class="stat-value">{{ stat?.pendingPushCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已推送待回收</span>
          <span class="stat-value warn">{{ stat?.waitingCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已过期</span>
          <span class="stat-value danger">{{ stat?.overdueCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">已拒答</span>
          <span class="stat-value">{{ stat?.refusedCount ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">有效答卷</span>
          <span class="stat-value">{{ stat?.answerTotal ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">平均百分制</span>
          <span class="stat-value">{{ stat?.avgScore100 ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">满意率</span>
          <span class="stat-value ok">{{ stat?.satisfiedRate ?? 0 }}%</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">NPS 净推荐值</span>
          <span class="stat-value">{{ stat?.nps ?? 0 }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">低分数 / 已转投诉</span>
          <span class="stat-value danger">{{ stat?.lowScoreCount ?? 0 }} / {{ stat?.disputedCount ?? 0 }}</span>
        </div>
      </div>
      <div class="stat-foot text-xs text-gray-400">
        口径：百分制 =（量表均分 − 1）/ 4 × 100；NPS =（推荐者 9-10 − 贬损者 0-6）/ 有效推荐作答 × 100；
        作废答卷不进任何统计；过期按截止时间与当前时间现算。
      </div>
    </el-card>

    <el-card shadow="never">
      <el-tabs v-model="activeTab">
        <!-- ============ 看板明细 ============ -->
        <el-tab-pane label="满意度看板" name="board">
          <div class="filter-bar">
            <el-select v-model="statQuery.templateId" :fit-input-width="false" clearable filterable placeholder="问卷"
                       style="width: 240px">
              <el-option v-for="t in tpRows" :key="t.id" :label="t.templateName" :value="t.id"/>
            </el-select>
            <el-select v-model="statQuery.scene" :fit-input-width="false" clearable placeholder="场景"
                       style="width: 140px">
              <el-option v-for="d in dicts.scene" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-date-picker v-model="statQuery.dateRange" end-placeholder="发放截止" start-placeholder="发放起始"
                            style="width: 240px" type="daterange" value-format="YYYY-MM-DD"/>
            <el-button :icon="Search" type="primary" @click="loadStat">查询</el-button>
            <el-button :icon="Refresh"
                       @click="statQuery.templateId = null; statQuery.scene = null; statQuery.dateRange = null; loadStat()">
              重置
            </el-button>
          </div>

          <div class="two-col">
            <div>
              <div class="block-title">维度短板（按均分升序，最差在最上）</div>
              <el-table v-loading="statLoading" :data="stat?.byDimension || []" border size="small" stripe>
                <el-table-column label="评价维度" prop="name" width="120"/>
                <el-table-column label="均分（5 级）" min-width="180">
                  <template #default="{ row }">
                    <div class="bar-wrap">
                      <div :class="{ bad: Number(row.avgScore) <= 2 }" :style="{ width: barWidth(row.avgScore * 20) }"
                           class="bar"/>
                      <span class="bar-text">{{ row.avgScore }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column align="right" label="作答数" prop="count" width="90"/>
              </el-table>
              <div class="text-xs text-gray-400 mt-2">
                维度均分 ≤ 2 会触发低分转投诉，所以这张表就是「下一步整改往哪使劲」的答案。
              </div>
            </div>
            <div>
              <div class="block-title">科室短板（百分制由低到高，TOP10）</div>
              <el-table v-loading="statLoading" :data="stat?.byDeptBottom || []" border size="small" stripe>
                <el-table-column label="科室" min-width="140" prop="name"/>
                <el-table-column align="right" label="百分制" prop="avgScore" width="90">
                  <template #default="{ row }">
                    <el-tag :type="scoreTag(row.avgScore)" size="small">{{ row.avgScore }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column align="right" label="答卷数" prop="count" width="90"/>
              </el-table>
              <div class="block-title mt-4">回收渠道分布</div>
              <el-table v-loading="statLoading" :data="stat?.byChannel || []" border size="small" stripe>
                <el-table-column label="渠道" min-width="140" prop="name"/>
                <el-table-column align="right" label="发放数" prop="count" width="90"/>
                <el-table-column align="right" label="回收率" prop="rate" width="90">
                  <template #default="{ row }">{{ row.rate }}%</template>
                </el-table-column>
              </el-table>
              <div class="text-xs text-gray-400 mt-2">
                短信/微信没有网关，发放了也收不回来 —— 回收率挂在 0% 是事实，不是 bug，别用代填把它抹平。
              </div>
            </div>
          </div>

          <div class="block-title mt-4">按日回收</div>
          <el-table v-loading="statLoading" :data="stat?.byDay || []" border max-height="260" size="small" stripe>
            <el-table-column label="日期" prop="name" width="140"/>
            <el-table-column align="right" label="发放数" prop="count" width="110"/>
            <el-table-column align="right" label="均分" prop="avgScore" width="110"/>
            <el-table-column align="right" label="回收率" prop="rate" width="110">
              <template #default="{ row }">{{ row.rate }}%</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ============ 发放与回收 ============ -->
        <el-tab-pane label="发放与回收" name="dispatch">
          <div class="filter-bar">
            <el-input v-model="dpQuery.keyword" clearable placeholder="单号 / 患者姓名 / 患者编号" style="width: 220px"
                      @keyup.enter="dpQuery.pageNum = 1; loadDispatch()"/>
            <el-select v-model="dpQuery.dispatchStatus" :fit-input-width="false" clearable placeholder="回收状态"
                       style="width: 150px">
              <el-option v-for="d in dicts.dispatchStatus" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="dpQuery.channel" :fit-input-width="false" clearable placeholder="渠道"
                       style="width: 130px">
              <el-option v-for="d in dicts.channel" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-date-picker v-model="dpQuery.dateRange" end-placeholder="发放截止" start-placeholder="发放起始"
                            style="width: 240px" type="daterange" value-format="YYYY-MM-DD"/>
            <el-checkbox v-model="dpQuery.overdueOnly" @change="dpQuery.pageNum = 1; loadDispatch()">仅已过期未回收
            </el-checkbox>
            <el-button :icon="Search" type="primary" @click="dpQuery.pageNum = 1; loadDispatch()">查询</el-button>
            <el-button :icon="Refresh" @click="resetDispatch">重置</el-button>
          </div>
          <el-table v-loading="dpLoading" :data="dpRows" border data-testid="survey-dispatch-table" size="small"
                    stripe @row-click="openDispatchDetail">
            <el-table-column label="发放单号" prop="dispatchNo" width="170"/>
            <el-table-column label="问卷" prop="templateName" show-overflow-tooltip width="180"/>
            <el-table-column label="患者" prop="patientName" width="110">
              <template #default="{ row }">{{ row.patientName }}<span
                  class="text-gray-400 text-xs ml-1">{{ row.patientNo }}</span></template>
            </el-table-column>
            <el-table-column label="联系电话" prop="phoneMasked" width="120">
              <template #default="{ row }">{{ row.phoneMasked || '—' }}</template>
            </el-table-column>
            <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="130">
              <template #default="{ row }">{{ row.deptName || '—' }}</template>
            </el-table-column>
            <el-table-column label="来源" width="100">
              <template #default="{ row }">{{ sourceText(row.sourceType) }}</template>
            </el-table-column>
            <el-table-column label="渠道" width="100">
              <template #default="{ row }">{{ channelText(row.channel) }}</template>
            </el-table-column>
            <el-table-column label="回收状态" width="120">
              <template #default="{ row }">
                <el-tag :type="dispatchTag(row.dispatchStatus)" size="small">{{
                    dispatchStatusText(row.dispatchStatus)
                  }}
                </el-tag>
                <el-tag v-if="row.overdue" class="ml-1" size="small" type="danger">逾期</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="截止时间" width="150">
              <template #default="{ row }">{{ fmtTime(row.expireTime) }}</template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="220">
              <template #default="{ row }">
                <el-button v-if="row.canFill" v-perm="'qc:survey:edit'" link size="small" type="primary"
                           @click.stop="openFill(row)">录入答卷
                </el-button>
                <el-button v-if="row.canPush" v-perm="'qc:survey:add'" link size="small" type="warning"
                           @click.stop="mark(row, 1)">标记已推送
                </el-button>
                <el-button v-if="row.canRefuse" v-perm="'qc:survey:add'" link size="small" type="info"
                           @click.stop="mark(row, 2)">标记拒答
                </el-button>
                <span v-if="!row.canFill && !row.canPush && !row.canRefuse" class="text-gray-400 text-xs">已闭环</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination v-model:current-page="dpQuery.pageNum" v-model:page-size="dpQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="dpTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @size-change="dpQuery.pageNum = 1; loadDispatch()" @current-change="loadDispatch"/>
          </div>
        </el-tab-pane>

        <!-- ============ 答卷台账 ============ -->
        <el-tab-pane label="答卷台账" name="answer">
          <div class="filter-bar">
            <el-input v-model="anQuery.keyword" clearable placeholder="答卷号 / 患者" style="width: 200px"
                      @keyup.enter="anQuery.pageNum = 1; loadAnswer()"/>
            <el-select v-model="anQuery.templateId" :fit-input-width="false" clearable filterable placeholder="问卷"
                       style="width: 220px">
              <el-option v-for="t in tpRows" :key="t.id" :label="t.templateName" :value="t.id"/>
            </el-select>
            <el-select v-model="anQuery.answerStatus" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 120px">
              <el-option v-for="d in dicts.answerStatus" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-date-picker v-model="anQuery.dateRange" end-placeholder="填报截止" start-placeholder="填报起始"
                            style="width: 240px" type="daterange" value-format="YYYY-MM-DD"/>
            <el-checkbox v-model="anQuery.lowScoreOnly" @change="anQuery.pageNum = 1; loadAnswer()">仅低分</el-checkbox>
            <el-button :icon="Search" type="primary" @click="anQuery.pageNum = 1; loadAnswer()">查询</el-button>
            <el-button :icon="Refresh" @click="resetAnswer">重置</el-button>
          </div>
          <el-table v-loading="anLoading" :data="anRows" border data-testid="survey-answer-table" size="small"
                    stripe @row-click="openAnswer">
            <el-table-column label="答卷号" prop="answerNo" width="170"/>
            <el-table-column label="问卷" prop="templateName" show-overflow-tooltip width="170"/>
            <el-table-column label="患者" prop="patientName" width="110"/>
            <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="130">
              <template #default="{ row }">{{ row.deptName || '—' }}</template>
            </el-table-column>
            <el-table-column align="right" label="均分" width="80">
              <template #default="{ row }">{{ row.avgScore ?? '—' }}</template>
            </el-table-column>
            <el-table-column align="right" label="百分制" width="90">
              <template #default="{ row }">
                <el-tag :type="scoreTag(row.score100)" size="small">{{ row.score100 ?? '—' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column align="right" label="NPS" width="80">
              <template #default="{ row }">{{ row.nps ?? '—' }}</template>
            </el-table-column>
            <el-table-column label="填报方式" width="110">
              <template #default="{ row }">{{ fillSourceText(row.fillSource) }}</template>
            </el-table-column>
            <el-table-column label="填报时间" width="150">
              <template #default="{ row }">{{ fmtTime(row.fillTime) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="150">
              <template #default="{ row }">
                <el-tag :type="Number(row.answerStatus) === 1 ? 'success' : 'info'" size="small">
                  {{ answerStatusText(row.answerStatus) }}
                </el-tag>
                <el-tag v-if="row.disputeCaseId" class="ml-1" size="small" type="danger">已转投诉</el-tag>
              </template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="120">
              <template #default="{ row }">
                <el-button v-if="row.canVoid" v-perm="'qc:survey:edit'" link size="small" type="danger"
                           @click.stop="openVoid(row)">作废
                </el-button>
                <el-button v-if="!row.canVoid" link size="small" type="primary" @click.stop="openAnswer(row)">查看
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination v-model:current-page="anQuery.pageNum" v-model:page-size="anQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="anTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @size-change="anQuery.pageNum = 1; loadAnswer()" @current-change="loadAnswer"/>
          </div>
        </el-tab-pane>

        <!-- ============ 问卷模板 ============ -->
        <el-tab-pane label="问卷模板" name="template">
          <div class="filter-bar">
            <el-input v-model="tpQuery.keyword" clearable placeholder="名称 / 编号" style="width: 200px"
                      @keyup.enter="tpQuery.pageNum = 1; loadTemplate()"/>
            <el-select v-model="tpQuery.scene" :fit-input-width="false" clearable placeholder="场景"
                       style="width: 140px">
              <el-option v-for="d in dicts.scene" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
            <el-select v-model="tpQuery.status" :fit-input-width="false" clearable placeholder="状态"
                       style="width: 120px">
              <el-option v-for="d in dicts.tplStatus" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
            <el-button :icon="Search" type="primary" @click="tpQuery.pageNum = 1; loadTemplate()">查询</el-button>
            <el-button :icon="Refresh" @click="resetTemplate">重置</el-button>
            <el-button v-perm="'qc:survey:add'" :icon="Plus" type="primary" @click="openTemplateCreate">新建问卷
            </el-button>
          </div>
          <el-table v-loading="tpLoading" :data="tpRows" border data-testid="survey-template-table" size="small" stripe>
            <el-table-column label="问卷编号" prop="templateNo" width="150"/>
            <el-table-column label="问卷名称" min-width="200" prop="templateName" show-overflow-tooltip/>
            <el-table-column label="场景" width="110">
              <template #default="{ row }">{{ sceneText(row.scene) }}</template>
            </el-table-column>
            <el-table-column align="right" label="题目数" prop="itemCount" width="90">
              <template #default="{ row }">{{ row.itemCount ?? (row.items || []).length }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="Number(row.status) === 1 ? 'success' : 'info'" size="small">{{
                    tplStatusText(row.status)
                  }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" width="150">
              <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column fixed="right" label="操作" width="200">
              <template #default="{ row }">
                <el-button v-perm="'qc:survey:add'" link size="small" type="primary" @click="openTemplateEdit(row)">
                  编辑
                </el-button>
                <el-button v-perm="'qc:survey:add'" :type="Number(row.status) === 1 ? 'info' : 'success'" link
                           size="small"
                           @click="toggleTemplateStatus(row)">{{ Number(row.status) === 1 ? '停用' : '启用' }}
                </el-button>
                <el-button v-perm="'qc:survey:add'" link size="small" type="danger" @click="removeTemplate(row)">删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="pager">
            <el-pagination v-model:current-page="tpQuery.pageNum" v-model:page-size="tpQuery.pageSize"
                           :page-sizes="PAGE_SIZES" :total="tpTotal"
                           layout="total, sizes, prev, pager, next, jumper"
                           @size-change="tpQuery.pageNum = 1; loadTemplate()" @current-change="loadTemplate"/>
          </div>
          <div class="text-xs text-gray-400 mt-2">
            同一场景只会启用一张卷（自动发放取「启用中」的那张），所以「停用旧卷 + 启用新卷」要连着做。
            已发放过的问卷不能删除，只能停用 —— 历史答卷要能回答「当时问的是哪张卷」。
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 发放单只读详情 -->
    <el-dialog v-model="dpDetailVisible" title="发放单详情" width="600px">
      <el-form v-if="dpDetail" disabled label-width="100px">
        <el-form-item label="发放单号">
          <el-input :model-value="dpDetail.dispatchNo"/>
        </el-form-item>
        <el-form-item label="问卷">
          <el-input :model-value="dpDetail.templateName"/>
        </el-form-item>
        <el-form-item label="场景">
          <el-input :model-value="sceneText(dpDetail.scene)"/>
        </el-form-item>
        <el-form-item label="患者">
          <el-input :model-value="`${dpDetail.patientName || ''} ${dpDetail.patientNo || ''}`"/>
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input :model-value="dpDetail.phoneMasked || '—'"/>
        </el-form-item>
        <el-form-item label="科室">
          <el-input :model-value="dpDetail.deptName || '—'"/>
        </el-form-item>
        <el-form-item label="发放来源">
          <el-input :model-value="sourceText(dpDetail.sourceType)"/>
        </el-form-item>
        <el-form-item label="回收渠道">
          <el-input :model-value="channelText(dpDetail.channel)"/>
        </el-form-item>
        <el-form-item label="回收状态">
          <el-input :model-value="dispatchStatusText(dpDetail.dispatchStatus) + (dpDetail.overdue ? '（已逾期）' : '')"/>
        </el-form-item>
        <el-form-item label="推送时间">
          <el-input :model-value="fmtTime(dpDetail.pushTime)"/>
        </el-form-item>
        <el-form-item label="截止时间">
          <el-input :model-value="fmtTime(dpDetail.expireTime)"/>
        </el-form-item>
        <el-form-item label="答卷">
          <el-input :model-value="dpDetail.answerId ? String(dpDetail.answerId) : '未回收'"/>
        </el-form-item>
        <el-form-item v-if="dpDetail.remark" label="备注">
          <el-input :model-value="dpDetail.remark" :rows="2" type="textarea"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dpDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 答卷录入（电话代填） -->
    <el-dialog v-model="fillVisible" title="满意度答卷录入" top="6vh" width="780px">
      <div v-loading="fillLoading">
        <div v-if="fillDispatch" class="fill-head">
          <div class="fill-patient">
            {{ fillDispatch.patientName }}
            <span class="text-gray-400 text-xs">{{ fillDispatch.patientNo }}</span>
            <el-tag class="ml-2" size="small" type="info">{{ fillDispatch.deptName || '未归科' }}</el-tag>
          </div>
          <div class="fill-phone">联系电话：{{ fillDispatch.phone || fillDispatch.phoneMasked }}</div>
          <div class="text-xs text-gray-400">
            问卷：{{ fillDispatch.templateName }} ｜ 截止 {{ fmtTime(fillDispatch.expireTime) }}
          </div>
        </div>

        <el-form v-if="fillPaper.length" class="fill-items" label-width="46px">
          <div v-for="it in fillPaper" :key="it.id" class="fill-item">
            <div class="fill-title">
              <span class="seq">{{ it.seqNo }}.</span>
              <span>{{ it.title }}</span>
              <el-tag v-if="Number(it.required) === 1" class="ml-1" size="small" type="danger">必答</el-tag>
              <span class="dim">{{ dimensionText(it.dimension) }} · {{ questionTypeText(it.questionType) }}</span>
            </div>
            <!-- 1-量表：5 级李克特，文案来自 lib/surveyScale（不是字典，见其注释） -->
            <el-radio-group v-if="Number(it.questionType) === 1" v-model="fillAnswers[it.id].score">
              <el-radio v-for="s in [1,2,3,4,5]" :key="s" :value="s">{{ s }} {{ LIKERT_LABELS[s] }}</el-radio>
            </el-radio-group>
            <!-- 4-NPS：0-10 单列，不参与百分制加权 -->
            <div v-else-if="Number(it.questionType) === 4" class="nps-row">
              <el-select v-model="fillAnswers[it.id].score" :fit-input-width="false" placeholder="推荐度"
                         style="width: 120px">
                <el-option v-for="n in npsOptions" :key="n" :label="String(n)" :value="n"/>
              </el-select>
              <span class="text-xs text-gray-400 ml-2">
                {{
                  fillAnswers[it.id].score === null || fillAnswers[it.id].score === '' ? '0=完全不推荐，10=强烈推荐' : npsBand(fillAnswers[it.id].score)
                }}
              </span>
            </div>
            <!-- 5-开放文本 -->
            <el-input v-else-if="Number(it.questionType) === 5" v-model="fillAnswers[it.id].textValue"
                      :rows="2" placeholder="患者原话（不要替患者总结）" type="textarea"/>
            <!-- 2/3-单选多选：表结构里没有选项集，按患者所选文本记录 -->
            <el-input v-else v-model="fillAnswers[it.id].optionLabel"
                      placeholder="选项内容（本卷未定义选项集，按患者所选文本记录）"/>
          </div>

          <el-form-item class="mt-3" label="方式">
            <el-select v-model="fillForm.fillSource" :fit-input-width="false" style="width: 160px">
              <el-option :value="2" label="随访员代填"/>
              <el-option :value="1" label="患者自填"/>
              <el-option :value="3" label="现场扫码"/>
            </el-select>
            <el-checkbox v-model="fillForm.anonymousFlag" :false-value="0" :true-value="1" class="ml-3">匿名
            </el-checkbox>
          </el-form-item>
          <el-form-item label="留言">
            <el-input v-model="fillForm.commentText" :rows="2" placeholder="患者补充意见（原文）" type="textarea"/>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="fillForm.remark" :rows="2" placeholder="录入备注（接通情况等）" type="textarea"/>
          </el-form-item>
        </el-form>
        <el-empty v-else-if="!fillLoading" description="该问卷没有题目，请先在「问卷模板」里配置"/>
      </div>
      <template #footer>
        <span class="foot-hint text-xs text-gray-400">
          {{ unansweredRequired.length ? `还有 ${unansweredRequired.length} 道必答题未作答` : '必答题已全部作答' }}
        </span>
        <el-button @click="fillVisible = false">取消</el-button>
        <el-button :disabled="!fillPaper.length" :loading="fillBusy" type="primary" @click="submitFill">提交回收
        </el-button>
      </template>
    </el-dialog>

    <!-- 答卷只读详情（含逐题快照） -->
    <el-dialog v-model="anDetailVisible" title="答卷详情" top="8vh" width="720px">
      <div v-loading="anDetailLoading">
        <el-form v-if="anDetail" disabled label-width="100px">
          <el-form-item label="答卷号">
            <el-input :model-value="anDetail.answerNo"/>
          </el-form-item>
          <el-form-item label="问卷">
            <el-input :model-value="anDetail.templateName"/>
          </el-form-item>
          <el-form-item label="患者">
            <el-input :model-value="`${anDetail.patientName || ''} ${anDetail.patientNo || ''}`"/>
          </el-form-item>
          <el-form-item label="科室">
            <el-input :model-value="anDetail.deptName || '—'"/>
          </el-form-item>
          <el-form-item label="得分">
            <el-input
                :model-value="`均分 ${anDetail.avgScore ?? '—'} ｜ 百分制 ${anDetail.score100 ?? '—'} ｜ NPS ${anDetail.nps ?? '—'}`"/>
          </el-form-item>
          <el-form-item label="填报">
            <el-input
                :model-value="`${fillSourceText(anDetail.fillSource)} ${anDetail.fillEmployeeName || ''} ${fmtTime(anDetail.fillTime)}`"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-input
                :model-value="answerStatusText(anDetail.answerStatus) + (anDetail.disputeCaseId ? '（已自动转投诉）' : '')"/>
          </el-form-item>
          <el-form-item v-if="anDetail.commentText" label="患者留言">
            <el-input :model-value="anDetail.commentText" :rows="3" type="textarea"/>
          </el-form-item>
        </el-form>
        <div class="block-title">逐题答案（题目快照，改模板不影响这张历史卷）</div>
        <el-table :data="anDetail?.items || []" border max-height="280" size="small" stripe>
          <el-table-column label="题号" prop="seqNo" width="60"/>
          <el-table-column label="维度" prop="dimension" width="110">
            <template #default="{ row }">{{ dimensionText(row.dimension) }}</template>
          </el-table-column>
          <el-table-column label="题干" min-width="200" prop="title" show-overflow-tooltip/>
          <el-table-column label="作答" width="150">
            <template #default="{ row }">
              <span v-if="row.score !== null && row.score !== undefined">{{
                  row.score
                }} 分{{ Number(row.questionType) === 1 ? `（${likertLabel(row.score)}）` : '' }}</span>
              <span v-else-if="row.optionLabel">{{ row.optionLabel }}</span>
              <span v-else-if="row.textValue" class="text-xs">{{ row.textValue }}</span>
              <span v-else class="text-gray-400">未作答</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="anDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 作废答卷 -->
    <el-dialog v-model="voidVisible" title="作废答卷" width="460px">
      <el-input v-model="voidReason" :rows="3" placeholder="作废原因（必填：误录/患者中途挂断/重复提交等）"
                type="textarea"/>
      <div class="text-xs text-gray-400 mt-2">
        作废的答卷不再进统计，但发放单会退回「未回收」—— 作废不等于「没问过」，回收率的分母不能跟着缩。
        已转出投诉的答卷不允许重填，作废前请先在投诉台账结案。
      </div>
      <template #footer>
        <el-button @click="voidVisible = false">取消</el-button>
        <el-button :loading="voidBusy" type="danger" @click="submitVoid">确认作废</el-button>
      </template>
    </el-dialog>

    <!-- 问卷模板编辑 -->
    <el-dialog v-model="tpEditVisible" :title="tpForm.id ? '编辑问卷' : '新建问卷'" top="5vh" width="920px">
      <el-form label-width="90px">
        <el-form-item label="问卷名称" required>
          <el-input v-model="tpForm.templateName" placeholder="如：住院出院随访满意度调查表" style="width: 360px"/>
        </el-form-item>
        <el-form-item label="适用场景" required>
          <el-select v-model="tpForm.scene" :fit-input-width="false" style="width: 200px">
            <el-option v-for="d in dicts.scene" :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
          </el-select>
          <span class="hint">同一场景只会启用一张卷</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="tpForm.status" :fit-input-width="false" style="width: 140px">
            <el-option v-for="d in dicts.tplStatus" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="tpForm.description" :rows="2" placeholder="适用范围与填卷说明" type="textarea"/>
        </el-form-item>
      </el-form>

      <div class="block-title">题目清单（整卷覆盖：保存即以此为准）</div>
      <el-table :data="tpItems" border max-height="360" size="small">
        <el-table-column label="题号" prop="seqNo" width="60"/>
        <el-table-column label="维度" width="140">
          <template #default="{ row }">
            <el-select v-model="row.dimension" :fit-input-width="false" style="width: 120px">
              <el-option v-for="d in dicts.dimension" :key="d.dictValue" :label="d.dictLabel"
                         :value="Number(d.dictValue)"/>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="题型" width="120">
          <template #default="{ row }">
            <el-select v-model="row.questionType" :fit-input-width="false" style="width: 100px"
                       @change="onQuestionTypeChange(row)">
              <el-option
                  v-for="d in dicts.questionType.filter(x => EDITABLE_QUESTION_TYPES.includes(Number(x.dictValue)))"
                  :key="d.dictValue" :label="d.dictLabel" :value="Number(d.dictValue)"/>
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="题干" min-width="240">
          <template #default="{ row }">
            <el-input v-model="row.title" placeholder="题干"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="必答" width="70">
          <template #default="{ row }">
            <el-checkbox v-model="row.required" :false-value="0" :true-value="1"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="权重" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.weight" :disabled="Number(row.questionType) === 5" :max="10" :min="0" :precision="2" :step="0.1"
                             controls-position="right" size="small" style="width: 86px"/>
          </template>
        </el-table-column>
        <el-table-column align="center" label="操作" width="70">
          <template #default="{ $index }">
            <el-button :icon="Delete" link size="small" type="danger" @click="removeTemplateItem($index)"/>
          </template>
        </el-table-column>
      </el-table>
      <el-button :icon="Plus" class="mt-2" @click="addTemplateItem">添加题目</el-button>
      <div class="text-xs text-gray-400 mt-2">
        百分制只算量表题（按权重加权，均分 →（x−1）/4×100）；NPS 单列算净推荐值；开放文本权重固定 0，不参与打分。
      </div>
      <template #footer>
        <el-button @click="tpEditVisible = false">取消</el-button>
        <el-button :loading="tpEditBusy" type="primary" @click="submitTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 满意度评价（sql/164，菜单 616）
 *
 * 链路：问卷模板（问什么）→ 发放台账（该收谁、走哪个渠道、收回来没有）
 *      → 答卷 + 逐题答案（收回来的事实）→ 看板（服务端聚合）。
 *
 * 五条口径，改页面前先读完：
 * 1. **回收率的分母是发放行，不是答卷行**。没收回来的必须是一行可见的数据
 *    （待推送 / 待回收 / 已过期 / 已拒答），而不是"库里没有这条" —— 否则回收率永远好看。
 * 2. **看板数字全部来自 `/survey/stat`**，前端不数当前页、不自造任何统计。
 * 3. **按钮可用性一律读后端 can* 字段**（canFill / canPush / canRefuse / canVoid），
 *    不按 dispatch_status 码值 switch。
 * 4. 手机号列表只有 `phoneMasked`；明文只从 `dispatch/getById` 出（要 `qc:survey:edit`），
 *    且只用于代填弹框顶部给随访员照着拨号，**不写回任何表单再提交**。
 * 5. 低分（百分制 < 60 或任一维度均分 ≤ 2）由后端在同一事务里自动登记投诉单，
 *    前端不判阈值、也不许"顺手"提示可以转投诉 —— 判两遍就会有两套口径。
 *
 * 日期入参一律 `YYYY-MM-DD`（后端按日边界补 23:59:59），不传 ISO T 分隔。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {ElMessage} from 'element-plus';
import {Delete, Plus, Refresh, Search} from '@element-plus/icons-vue';
import {
  deleteTemplate,
  getAnswerDetail,
  getDispatchDetail,
  getSurveyStat,
  getTemplateDetail,
  listAnswerPage,
  listDispatchPage,
  listTemplatePage,
  markDispatch,
  submitAnswer,
  templateUpsert,
  voidAnswer,
} from '@/api/survey';
import {getDictDataMapList} from '@/api/system';
import {DICT_TYPE} from '@/lib/dict-cache';
import {dictLabelText} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {LIKERT_LABELS, likertLabel, NPS_MAX, NPS_MIN, npsBand} from '@/lib/surveyScale';

const activeTab = ref('board');
const fmtTime = (v) => (v ? String(v).slice(0, 16).replace('T', ' ') : '—');
// ---------------- 字典（一次最多 5 个 type，超了整批返回空且不报错 → 分两批） ----------------
const dicts = reactive({
  scene: [], dimension: [], questionType: [], source: [], channel: [],
  dispatchStatus: [], answerStatus: [], fillSource: [], tplStatus: [],
});
const sceneText = (v) => dictLabelText(dicts.scene, v);
const dimensionText = (v) => dictLabelText(dicts.dimension, v);
const questionTypeText = (v) => dictLabelText(dicts.questionType, v);
const sourceText = (v) => dictLabelText(dicts.source, v);
const channelText = (v) => dictLabelText(dicts.channel, v);
const dispatchStatusText = (v) => dictLabelText(dicts.dispatchStatus, v);
const answerStatusText = (v) => dictLabelText(dicts.answerStatus, v);
const fillSourceText = (v) => dictLabelText(dicts.fillSource, v);
const tplStatusText = (v) => dictLabelText(dicts.tplStatus, v);
const loadDicts = async () => {
  try {
    const a = await getDictDataMapList([
      DICT_TYPE.SURVEY_SCENE, DICT_TYPE.SURVEY_DIMENSION, DICT_TYPE.SURVEY_QUESTION_TYPE,
      DICT_TYPE.SURVEY_SOURCE, DICT_TYPE.SURVEY_CHANNEL,
    ].join(','));
    const b = await getDictDataMapList([
      DICT_TYPE.SURVEY_DISPATCH_STATUS, DICT_TYPE.SURVEY_ANSWER_STATUS,
      DICT_TYPE.SURVEY_FILL_SOURCE, DICT_TYPE.SURVEY_TPL_STATUS,
    ].join(','));
    dicts.scene = a?.data?.[DICT_TYPE.SURVEY_SCENE] || [];
    dicts.dimension = a?.data?.[DICT_TYPE.SURVEY_DIMENSION] || [];
    dicts.questionType = a?.data?.[DICT_TYPE.SURVEY_QUESTION_TYPE] || [];
    dicts.source = a?.data?.[DICT_TYPE.SURVEY_SOURCE] || [];
    dicts.channel = a?.data?.[DICT_TYPE.SURVEY_CHANNEL] || [];
    dicts.dispatchStatus = b?.data?.[DICT_TYPE.SURVEY_DISPATCH_STATUS] || [];
    dicts.answerStatus = b?.data?.[DICT_TYPE.SURVEY_ANSWER_STATUS] || [];
    dicts.fillSource = b?.data?.[DICT_TYPE.SURVEY_FILL_SOURCE] || [];
    dicts.tplStatus = b?.data?.[DICT_TYPE.SURVEY_TPL_STATUS] || [];
  } catch (e) {
    console.error('加载字典失败', e);
  }
};
const dispatchTag = (v) => ({1: 'info', 2: 'warning', 3: 'success', 4: 'danger', 5: 'info'}[Number(v)] || 'info');
const scoreTag = (v) => {
  const n = Number(v);
  if (Number.isNaN(n))
    return 'info';
  if (n >= 90)
    return 'success';
  if (n >= 60)
    return 'warning';
  return 'danger';
};
// ---------------- 看板 ----------------
const stat = ref(null);
const statLoading = ref(false);
const statQuery = reactive({
  templateId: null,
  scene: null,
  dateRange: null,
});
const loadStat = async () => {
  statLoading.value = true;
  try {
    const res = await getSurveyStat({
      templateId: statQuery.templateId ?? undefined,
      scene: statQuery.scene ?? undefined,
      dateFrom: statQuery.dateRange?.[0] ?? undefined,
      dateTo: statQuery.dateRange?.[1] ?? undefined,
    });
    if (res.code === 200)
      stat.value = res.data;
    else
      ElMessage.error(res.message || '看板加载失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('看板加载失败');
  } finally {
    statLoading.value = false;
  }
};
/** 维度短板条：宽度按该维度得分占满量程比例，只是视觉，数字仍以文本为准 */
const barWidth = (v) => {
  const n = Number(v);
  if (Number.isNaN(n) || n <= 0)
    return '2%';
  return `${Math.min(100, Math.max(2, n))}%`;
};
// ---------------- 发放与回收 ----------------
const dpLoading = ref(false);
const dpRows = ref([]);
const dpTotal = ref(0);
const dpQuery = reactive({
  keyword: '', channel: null, dispatchStatus: null,
  overdueOnly: false, dateRange: null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const loadDispatch = async () => {
  dpLoading.value = true;
  try {
    const res = await listDispatchPage({
      keyword: dpQuery.keyword.trim() || undefined,
      channel: dpQuery.channel ?? undefined,
      dispatchStatus: dpQuery.dispatchStatus ?? undefined,
      overdueOnly: dpQuery.overdueOnly || undefined,
      dateFrom: dpQuery.dateRange?.[0] ?? undefined,
      dateTo: dpQuery.dateRange?.[1] ?? undefined,
      pageNum: dpQuery.pageNum, pageSize: dpQuery.pageSize,
    });
    if (res.code === 200) {
      dpRows.value = res.data?.records || [];
      dpTotal.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    dpLoading.value = false;
  }
};
const resetDispatch = () => {
  Object.assign(dpQuery, {
    keyword: '',
    channel: null,
    dispatchStatus: null,
    overdueOnly: false,
    dateRange: null,
    pageNum: 1
  });
  loadDispatch();
};
const mark = async (row, action) => {
  try {
    const res = await markDispatch({
      id: row.id,
      action,
      remark: action === 2 ? '电话未接通/患者拒答' : '随访员已电话联系'
    });
    if (res.code === 200) {
      ElMessage.success(action === 1 ? '已标记推送' : '已标记拒答');
      loadDispatch();
      loadStat();
    } else
      ElMessage.error(res.message || '操作失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
// 只读详情（行点击）：列表行已含全部展示字段，不再回后端取明文手机号
const dpDetailVisible = ref(false);
const dpDetail = ref(null);
const openDispatchDetail = (row) => {
  dpDetail.value = row;
  dpDetailVisible.value = true;
};
// ---------------- 答卷录入 ----------------
const fillVisible = ref(false);
const fillLoading = ref(false);
const fillBusy = ref(false);
const fillDispatch = ref(null);
const fillPaper = ref([]);
const fillForm = reactive({fillSource: 2, anonymousFlag: 0, commentText: '', remark: ''});
/** itemId -> { score, optionLabel, textValue } */
const fillAnswers = reactive({});
const npsOptions = computed(() => {
  const out = [];
  for (let i = NPS_MIN; i <= NPS_MAX; i++)
    out.push(i);
  return out;
});
/**
 * 打开录入：并发取「发放单（含明文电话，供照着拨号）」与「当前问卷题目」。
 * 题目取的是模板**当前**版本 —— 已发放未回收的卷子按新题作答，
 * 历史答卷有逐题快照保护，不会被改题篡改。
 */
const openFill = async (row) => {
  fillVisible.value = true;
  fillLoading.value = true;
  fillDispatch.value = null;
  fillPaper.value = [];
  Object.keys(fillAnswers).forEach(k => delete fillAnswers[k]);
  Object.assign(fillForm, {fillSource: 2, anonymousFlag: 0, commentText: '', remark: ''});
  try {
    const [d, t] = await Promise.all([getDispatchDetail(row.id), getTemplateDetail(row.templateId)]);
    if (d.code !== 200) {
      ElMessage.error(d.message || '加载发放单失败');
      return;
    }
    if (t.code !== 200) {
      ElMessage.error(t.message || '加载问卷失败');
      return;
    }
    fillDispatch.value = d.data;
    fillPaper.value = (t.data?.items || []).slice().sort((x, y) => Number(x.seqNo) - Number(y.seqNo));
    fillPaper.value.forEach(it => {
      fillAnswers[it.id] = {score: null, optionLabel: '', textValue: ''};
    });
  } catch (e) {
    console.error(e);
    ElMessage.error('加载失败');
  } finally {
    fillLoading.value = false;
  }
};
const unansweredRequired = computed(() => fillPaper.value.filter(it => {
  if (Number(it.required) !== 1)
    return false;
  const a = fillAnswers[it.id] || {};
  if (Number(it.questionType) === 5)
    return !String(a.textValue || '').trim();
  if (Number(it.questionType) === 2 || Number(it.questionType) === 3)
    return !String(a.optionLabel || '').trim();
  return a.score === null || a.score === undefined || a.score === '';
}));
const submitFill = async () => {
  if (unansweredRequired.value.length) {
    ElMessage.warning(`还有 ${unansweredRequired.value.length} 道必答题未作答（第 ${unansweredRequired.value.map(i => i.seqNo).join('、')} 题）`);
    return;
  }
  const items = fillPaper.value.map(it => {
    const a = fillAnswers[it.id] || {};
    return {
      itemId: it.id,
      score: a.score === null || a.score === '' || a.score === undefined ? undefined : Number(a.score),
      optionLabel: a.optionLabel ? String(a.optionLabel).trim() : undefined,
      textValue: a.textValue ? String(a.textValue).trim() : undefined,
    };
  }).filter(i => i.score !== undefined || i.optionLabel || i.textValue);
  fillBusy.value = true;
  try {
    const res = await submitAnswer({
      dispatchId: fillDispatch.value.id,
      fillSource: fillForm.fillSource,
      anonymousFlag: fillForm.anonymousFlag,
      commentText: fillForm.commentText || undefined,
      remark: fillForm.remark || undefined,
      items,
    });
    if (res.code === 200) {
      // 是否转投诉由后端判，这里只把它已经做的事说清楚，不重复判阈值
      ElMessage.success(res.data?.disputeCaseId
          ? `已回收：总分 ${res.data?.score100} 分，低分已自动登记投诉单，请医务/质控岗跟进受理`
          : `已回收：总分 ${res.data?.score100} 分（均分 ${res.data?.avgScore}）`);
      fillVisible.value = false;
      loadDispatch();
      loadAnswer();
      loadStat();
    } else
      ElMessage.error(res.message || '提交失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('提交失败');
  } finally {
    fillBusy.value = false;
  }
};
// ---------------- 答卷台账 ----------------
const anLoading = ref(false);
const anRows = ref([]);
const anTotal = ref(0);
const anQuery = reactive({
  keyword: '', templateId: null, answerStatus: null,
  lowScoreOnly: false, dateRange: null,
  pageNum: 1, pageSize: DEFAULT_PAGE_SIZE,
});
const loadAnswer = async () => {
  anLoading.value = true;
  try {
    const res = await listAnswerPage({
      keyword: anQuery.keyword.trim() || undefined,
      templateId: anQuery.templateId ?? undefined,
      answerStatus: anQuery.answerStatus ?? undefined,
      lowScoreOnly: anQuery.lowScoreOnly || undefined,
      dateFrom: anQuery.dateRange?.[0] ?? undefined,
      dateTo: anQuery.dateRange?.[1] ?? undefined,
      pageNum: anQuery.pageNum, pageSize: anQuery.pageSize,
    });
    if (res.code === 200) {
      anRows.value = res.data?.records || [];
      anTotal.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    anLoading.value = false;
  }
};
const resetAnswer = () => {
  Object.assign(anQuery, {
    keyword: '',
    templateId: null,
    answerStatus: null,
    lowScoreOnly: false,
    dateRange: null,
    pageNum: 1
  });
  loadAnswer();
};
const anDetailVisible = ref(false);
const anDetailLoading = ref(false);
const anDetail = ref(null);
const openAnswer = async (row) => {
  anDetailLoading.value = true;
  anDetailVisible.value = true;
  anDetail.value = null;
  try {
    const res = await getAnswerDetail(row.id);
    if (res.code === 200)
      anDetail.value = res.data;
    else
      ElMessage.error(res.message || '加载答卷失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('加载答卷失败');
  } finally {
    anDetailLoading.value = false;
  }
};
const voidVisible = ref(false);
const voidBusy = ref(false);
const voidId = ref('');
const voidReason = ref('');
const openVoid = (row) => {
  voidId.value = row.id;
  voidReason.value = '';
  voidVisible.value = true;
};
const submitVoid = async () => {
  if (!voidReason.value.trim()) {
    ElMessage.warning('作废原因必填');
    return;
  }
  voidBusy.value = true;
  try {
    const res = await voidAnswer({id: voidId.value, reason: voidReason.value.trim()});
    if (res.code === 200) {
      ElMessage.success('已作废：这张答卷不再进统计，但发放单退回未回收（作废不等于没问过）');
      voidVisible.value = false;
      loadAnswer();
      loadDispatch();
      loadStat();
    } else
      ElMessage.error(res.message || '作废失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('作废失败');
  } finally {
    voidBusy.value = false;
  }
};
// ---------------- 问卷模板 ----------------
const tpLoading = ref(false);
const tpRows = ref([]);
const tpTotal = ref(0);
const tpQuery = reactive({keyword: '', scene: null, status: null, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadTemplate = async () => {
  tpLoading.value = true;
  try {
    const res = await listTemplatePage({
      keyword: tpQuery.keyword.trim() || undefined,
      scene: tpQuery.scene ?? undefined,
      status: tpQuery.status ?? undefined,
      pageNum: tpQuery.pageNum, pageSize: tpQuery.pageSize,
    });
    if (res.code === 200) {
      tpRows.value = res.data?.records || [];
      tpTotal.value = Number(res.data?.total || 0);
    } else
      ElMessage.error(res.message || '查询失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('查询失败');
  } finally {
    tpLoading.value = false;
  }
};
const resetTemplate = () => {
  Object.assign(tpQuery, {keyword: '', scene: null, status: null, pageNum: 1});
  loadTemplate();
};
const tpEditVisible = ref(false);
const tpEditLoading = ref(false);
const tpEditBusy = ref(false);
const tpForm = reactive({
  id: null, templateName: '', scene: 1, status: 1, description: '', remark: '',
});
const tpItems = ref([]);
/**
 * 题型只放能算分的三种：量表 / NPS / 开放文本。
 * 单选与多选要能落地，得先给题目加「选项集」这一列（当前表结构没有），
 * 与其让录入员对着无选项的单选题自由发挥，不如不在编辑器里开这个口子。
 */
const EDITABLE_QUESTION_TYPES = [1, 4, 5];
const openTemplateCreate = () => {
  Object.assign(tpForm, {id: null, templateName: '', scene: 1, status: 1, description: '', remark: ''});
  tpItems.value = [];
  addTemplateItem();
  tpEditVisible.value = true;
};
const openTemplateEdit = async (row) => {
  tpEditLoading.value = true;
  try {
    const res = await getTemplateDetail(row.id);
    if (res.code !== 200) {
      ElMessage.error(res.message || '加载问卷失败');
      return;
    }
    const t = res.data;
    Object.assign(tpForm, {
      id: t.id, templateName: t.templateName, scene: t.scene, status: t.status,
      description: t.description || '', remark: t.remark || '',
    });
    tpItems.value = (t.items || []).map((i) => ({
      seqNo: i.seqNo,
      dimension: i.dimension,
      questionType: i.questionType,
      title: i.title,
      required: Number(i.required),
      weight: Number(i.weight ?? 1),
      maxScore: Number(i.maxScore ?? (i.questionType === 4 ? 10 : 5)),
    }));
    tpEditVisible.value = true;
  } catch (e) {
    console.error(e);
    ElMessage.error('加载问卷失败');
  } finally {
    tpEditLoading.value = false;
  }
};
const addTemplateItem = () => {
  tpItems.value.push({
    seqNo: tpItems.value.length + 1, dimension: 7, questionType: 1, title: '',
    required: 1, weight: 1, maxScore: 5,
  });
};
const removeTemplateItem = (index) => {
  tpItems.value.splice(index, 1);
  tpItems.value.forEach((it, i) => {
    it.seqNo = i + 1;
  });
};
const onQuestionTypeChange = (it) => {
  // 满分跟着题型走：量表 5、NPS 10、开放文本不计分（权重 0）
  if (Number(it.questionType) === 4) {
    it.maxScore = 10;
    it.weight = 1;
  } else if (Number(it.questionType) === 5) {
    it.maxScore = 5;
    it.weight = 0;
    it.required = 0;
  } else {
    it.maxScore = 5;
    it.weight = Number(it.weight) || 1;
  }
};
const submitTemplate = async () => {
  if (!tpForm.templateName.trim()) {
    ElMessage.warning('问卷名称必填');
    return;
  }
  if (!tpItems.value.length) {
    ElMessage.warning('至少一道题');
    return;
  }
  if (tpItems.value.some(i => !String(i.title).trim())) {
    ElMessage.warning('存在题干为空的题目');
    return;
  }
  tpEditBusy.value = true;
  try {
    const res = await templateUpsert({
      id: tpForm.id ?? undefined,
      templateName: tpForm.templateName.trim(),
      scene: tpForm.scene,
      status: tpForm.status,
      description: tpForm.description || undefined,
      remark: tpForm.remark || undefined,
      items: tpItems.value.map(i => ({
        seqNo: i.seqNo, dimension: i.dimension, questionType: i.questionType,
        title: String(i.title).trim(), required: i.required,
        weight: i.questionType === 5 ? 0 : Number(i.weight) || 0, maxScore: i.maxScore,
      })),
    });
    if (res.code === 200) {
      ElMessage.success('问卷已保存（整卷覆盖：题目以本次提交为准）');
      tpEditVisible.value = false;
      loadTemplate();
      loadStat();
    } else
      ElMessage.error(res.message || '保存失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('保存失败');
  } finally {
    tpEditBusy.value = false;
  }
};
const removeTemplate = async (row) => {
  try {
    const res = await deleteTemplate(row.id);
    if (res.code === 200) {
      ElMessage.success('已删除');
      loadTemplate();
    } else
      ElMessage.error(res.message || '删除失败');
  } catch (e) {
    console.error(e);
    ElMessage.error('删除失败');
  }
};
/**
 * 启停开关：`templateUpsert` 是整卷覆盖语义（`items` 带 `@NotEmpty`），
 * 没有"只改状态"的接口，所以先读回题目再原样提交 —— 少带一题就等于把卷子改短了。
 */
const toggleTemplateStatus = async (row) => {
  const next = Number(row.status) === 1 ? 2 : 1;
  try {
    const d = await getTemplateDetail(row.id);
    if (d.code !== 200) {
      ElMessage.error(d.message || '加载问卷失败');
      return;
    }
    const items = (d.data?.items || []).map((i) => ({
      seqNo: i.seqNo, dimension: i.dimension, questionType: i.questionType, title: i.title,
      required: i.required, weight: i.weight, maxScore: i.maxScore,
    }));
    if (!items.length) {
      ElMessage.error('该问卷没有题目，请先补全再启停');
      return;
    }
    const res = await templateUpsert({
      id: row.id, templateName: row.templateName, scene: row.scene, status: next, items,
    });
    if (res.code !== 200) {
      ElMessage.error(res.message || '操作失败');
      return;
    }
    ElMessage.success(next === 1 ? '已启用' : '已停用（停用后不再自动发放，历史答卷不受影响）');
    loadTemplate();
  } catch (e) {
    console.error(e);
    ElMessage.error('操作失败');
  }
};
const reloadAll = () => {
  loadStat();
  loadDispatch();
  loadAnswer();
  loadTemplate();
};
onMounted(async () => {
  await loadDicts();
  reloadAll();
});
</script>

<style scoped>
.survey-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-card :deep(.el-card__body) {
  padding: 14px 16px;
}

.stat-row {
  display: flex;
  gap: 26px;
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

.stat-value.ok {
  color: #67c23a;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-foot {
  margin-top: 10px;
}

.filter-bar {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
  align-items: center;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.two-col {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.block-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.hint {
  margin-left: 8px;
  font-size: 12px;
  color: #909399;
}

.bar-wrap {
  position: relative;
  height: 16px;
  background: #f1f5f9;
  border-radius: 3px;
}

.bar {
  height: 100%;
  background: #1269b5;
  border-radius: 3px;
}

.bar.bad {
  background: #f56c6c;
}

.bar-text {
  position: absolute;
  right: 6px;
  top: 0;
  font-size: 12px;
  line-height: 16px;
  color: #303133;
}

.fill-head {
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 12px;
  background: #f8fafc;
}

.fill-patient {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.fill-phone {
  font-size: 13px;
  color: #1269b5;
  margin-top: 2px;
}

.fill-items {
  margin-top: 4px;
}

.fill-item {
  padding: 8px 0;
  border-bottom: 1px dashed #eef2f7;
}

.fill-title {
  margin-bottom: 6px;
  color: #303133;
}

.fill-title .seq {
  font-weight: 600;
  margin-right: 4px;
}

.fill-title .dim {
  font-size: 12px;
  color: #909399;
  margin-left: 8px;
}

.nps-row {
  display: flex;
  align-items: center;
}

.foot-hint {
  float: left;
  line-height: 32px;
}
</style>
