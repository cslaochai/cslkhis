<template>
  <div class="space-y-6">
    <!-- 概览 -->
    <div v-if="overview" v-loading="overviewLoading" class="grid grid-cols-2 gap-4 lg:grid-cols-5"
         data-testid="p5-sign-overview">
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">有效签名</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900" data-testid="p5-sign-stat-valid">{{
            overview.validSign
          }}</p>
        <p class="mt-1 text-xs text-slate-400">今日新增 {{ overview.todaySign }} 条 · 最近
          {{ text(overview.lastSignTime) }}</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">已作废</p>
        <p class="mt-1 text-2xl font-semibold text-amber-600" data-testid="p5-sign-stat-invalid">{{
            overview.invalidSign
          }}</p>
        <p class="mt-1 text-xs text-slate-400">历史行保留，仅追加作废信息</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">未校验</p>
        <p class="mt-1 text-2xl font-semibold text-slate-500" data-testid="p5-sign-stat-unchecked">
          {{ overview.verifyUnchecked }}</p>
        <p class="mt-1 text-xs text-slate-400">「没验过」不等于「验过了没问题」</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">验签失败</p>
        <p class="mt-1 text-2xl font-semibold text-red-600" data-testid="p5-sign-stat-failed">{{
            overview.verifyFailed
          }}</p>
        <p class="mt-1 text-xs text-slate-400">{{
            num(overview.verifyFailed) > 0 ? '必须逐条查明原因' : '当前无失败记录'
          }}</p>
      </div>
      <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
        <p class="text-xs text-slate-500">签名证书</p>
        <p class="mt-1 text-2xl font-semibold text-slate-900" data-testid="p5-sign-stat-cert">{{
            overview.certActive
          }}</p>
        <p class="mt-1 text-xs text-slate-400">
          共 {{ overview.certTotal }} 张 · 已吊销 {{ overview.certRevoked }} · 自动签发 {{ overview.certAutoIssued }}
          张（信任级别低于人工）
        </p>
      </div>
    </div>

    <!-- 覆盖情况 -->
    <div v-if="overview" class="rounded-lg border border-slate-200 bg-white shadow-sm">
      <div class="flex flex-wrap items-center justify-between gap-2 border-b border-slate-100 px-4 py-3">
        <div>
          <h2 class="text-sm font-semibold text-slate-900">各业务对象签名覆盖情况</h2>
          <p class="mt-0.5 text-xs text-slate-500">
            已接入 {{ overview.bizTypeCount }} 类业务对象：{{ text(overview.bizTypeTexts) }}
          </p>
          <!-- 覆盖率只有病历文书两类。住院医嘱（双签）不在这里 —— 必须说明白，
               否则这张两行的表会被读成"全院签名情况就这么两行"。 -->
          <p class="mt-0.5 text-xs text-slate-400">
            覆盖率只统计<b>病历文书</b>（一条文书写对应一次签名）；<b>住院医嘱为双签</b>（医生开立 + 护士校对两条签名），
            分子分母不是一回事，改由上方「有效签名」与签名记录列表按签名条数统计，不在此表折算成比率。
          </p>
        </div>
        <div class="text-xs text-slate-500">
          未签名 <b class="text-amber-600" data-testid="p5-sign-pending-total">{{ pendingTotal }}</b> 份 · 签名已失效
          <b class="text-slate-600" data-testid="p5-sign-invalidated-total">{{ invalidatedTotal }}</b> 份
        </div>
      </div>
      <div class="p-4">
        <el-table :data="coverages" border data-testid="p5-sign-coverage-table" size="small">
          <el-table-column label="业务对象" width="140">
            <template #default="{ row }">
              <span class="text-xs font-medium">{{ row.bizTypeText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="对象总数" width="100">
            <template #default="{ row }">
              <span class="text-xs">{{ num(row.total) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="已签名" width="90">
            <template #default="{ row }">
              <span :data-testid="`p5-sign-cov-signed-${row.bizType}`"
                    class="text-xs font-medium text-emerald-600">{{ num(row.signed) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="未签名（存量）" width="140">
            <template #default="{ row }">
              <span :data-testid="`p5-sign-cov-pending-${row.bizType}`"
                    class="text-xs text-amber-600">{{ num(row.pendingSign) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="签名已失效" width="110">
            <template #default="{ row }">
              <span :data-testid="`p5-sign-cov-invalidated-${row.bizType}`"
                    class="text-xs text-slate-500">{{ num(row.invalidated) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="覆盖率" width="100">
            <template #default="{ row }">
              <!-- 分母为 0 时后端给的是「—（该类型还没有可统计的对象）」（不是 0%，也不是 100%）：没有对象可比，就不该给一个比率 -->
              <span
                  :class="isRatePlaceholder(row.signedRateText) ? 'text-slate-400' : 'text-slate-800'"
                  :data-testid="`p5-sign-cov-rate-${row.bizType}`"
                  class="text-xs font-medium"
              >{{ row.signedRateText }}</span>
            </template>
          </el-table-column>
          <el-table-column label="说明" min-width="300">
            <template #default="{ row }">
              <span class="text-xs text-slate-500">
                <template v-if="num(row.pendingSign) > 0">
                  未签名的 {{ num(row.pendingSign) }} 份是签名能力上线前的存量文书，按规范不可补签。
                </template>
                <template v-else-if="num(row.total) === 0">该类型暂无数据，覆盖率不适用。</template>
                <template v-else>全部已签名{{
                    num(row.invalidated) > 0 ? '，其中 ' + num(row.invalidated) + ' 份签名已失效需处理' : ''
                  }}。</template>
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 信任与来源说明（后端给什么就显示什么，前端不自造文案） -->
    <div v-if="overview"
         class="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-xs leading-6 text-amber-900"
         data-testid="p5-sign-trust-note">
      <div class="flex items-start gap-2">
        <el-icon class="mt-0.5">
          <WarningFilled/>
        </el-icon>
        <div>
          <p class="font-medium">时间戳来源：{{ text(overview.timeSourceNote) }}</p>
          <p data-testid="g6-tsa-status">
            时间戳服务：{{
              tsaStatus ? (tsaStatus.available
                  ? `在线 · ${text(tsaStatus.tsaName)} · 配置 ${text(tsaStatus.configTimeSource)} / 生效「${text(tsaStatus.effectiveTimeSourceText)}」 · 令牌台账 ${num(tsaStatus.tokenCount)} 条`
                  : '不在线（签名时间生效「本机时钟」）') : '—'
            }}
          </p>
          <p v-if="tsaStatus?.trustNote" data-testid="g6-tsa-trust">{{ tsaStatus.trustNote }}</p>
          <div class="mt-2 flex flex-wrap items-center gap-2" data-testid="g6b-tsa-ops">
            <span class="font-medium">时间来源切换：</span>
            <el-radio-group v-model="tsaSourceDraft" :disabled="tsaOpsLoading" data-testid="g6b-time-source"
                            @change="onTimeSourceChange">
              <el-radio-button :value="1">本机时钟</el-radio-button>
              <el-radio-button :value="3">可信时间戳</el-radio-button>
            </el-radio-group>
            <el-button
                v-perm="'sign:center:edit'"
                :loading="tsaOpsLoading"
                :plain="!!tsaStatus?.available"
                :type="tsaStatus?.available ? 'danger' : 'primary'"
                data-testid="g6b-tsa-toggle"
                size="small"
                @click="onToggleTsa"
            >{{ tsaStatus?.available ? '停用 TSA' : '启用 TSA' }}
            </el-button>
            <span class="text-slate-500">停用=不再签发新令牌，历史令牌仍可验证</span>
          </div>
          <p>证书信任级别：{{ text(overview.certTrustNote) }}</p>
          <p v-for="(n, i) in overview.notes || []" :key="i" class="mt-0.5">{{ n }}</p>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <!-- ---------------- 签名记录 ---------------- -->
      <el-tab-pane label="签名记录" name="sign">
        <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="flex flex-wrap items-center gap-2 border-b border-slate-100 px-4 py-3">
            <el-select v-model="signQuery.bizType" clearable data-testid="p5-sign-f-biztype" placeholder="对象类型"
                       style="width: 130px" @change="searchSign">
              <el-option v-for="o in options.bizTypes || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
            </el-select>
            <el-select v-model="signQuery.signScene" clearable data-testid="p5-sign-f-scene" placeholder="签名场景"
                       style="width: 120px" @change="searchSign">
              <el-option v-for="o in options.scenes || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
            </el-select>
            <el-select v-model="signQuery.signStatus" clearable data-testid="p5-sign-f-status" placeholder="签名状态"
                       style="width: 120px" @change="searchSign">
              <el-option v-for="o in options.signStatuses || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
            </el-select>
            <el-select v-model="signQuery.verifyStatus" clearable data-testid="p5-sign-f-verify" placeholder="验签结果"
                       style="width: 130px" @change="searchSign">
              <el-option v-for="o in options.verifyStatuses || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
            </el-select>
            <el-select v-model="signQuery.timeSource" clearable data-testid="p5-sign-f-timesource"
                       placeholder="时间来源"
                       style="width: 130px" @change="searchSign">
              <el-option v-for="o in options.timeSources || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
            </el-select>
            <el-date-picker
                v-model="timeRange"
                data-testid="p5-sign-f-timerange"
                end-placeholder="签名止"
                start-placeholder="签名起"
                style="width: 330px"
                type="datetimerange"
                value-format="YYYY-MM-DD HH:mm:ss"
                @change="searchSign"
            />
            <el-input v-model="signQuery.keyword" clearable data-testid="p5-sign-f-keyword"
                      placeholder="签名人 / 患者 / 对象单号"
                      style="width: 220px" @keyup.enter="searchSign">
              <template #prefix>
                <el-icon>
                  <Search/>
                </el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="searchSign">查询</el-button>
            <el-button @click="clearSignFilter">清除</el-button>
          </div>

          <div v-loading="signLoading" class="p-4">
            <el-table :data="signList" border data-testid="p5-sign-table" size="small" @row-click="openDetail">
              <el-table-column label="签名编号" width="170">
                <template #default="{ row }">
                  <span class="text-xs font-medium text-slate-700">{{ row.signNo }}</span>
                </template>
              </el-table-column>
              <el-table-column label="对象" width="190">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.bizTypeText }} · {{ row.bizNo || '—' }}</div>
                  <div class="text-xs text-slate-400">{{ row.patientName || '—' }}<span
                      v-if="row.deptName"> · {{ row.deptName }}</span></div>
                </template>
              </el-table-column>
              <el-table-column label="场景" width="80">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.signSceneText }}</div>
                  <div class="text-xs text-slate-400">第 {{ row.chainNo }} 次</div>
                </template>
              </el-table-column>
              <el-table-column label="签名人" width="130">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.signerName || '—' }}</div>
                  <div class="text-xs text-slate-400">{{ row.signerDeptName || '—' }}</div>
                </template>
              </el-table-column>
              <el-table-column label="摘要" width="150">
                <template #default="{ row }">
                  <span :data-testid="`p5-sign-row-digest-${row.signNo}`"
                        class="font-mono text-xs text-slate-600">{{ row.contentDigestShort || '—' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="签名时间" width="170">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.signedTime || '—' }}</div>
                  <div class="mt-0.5">
                    <el-tag :data-testid="`p5-sign-row-ts-${row.signNo}`" :type="timeSourceTag(row.timeSource)"
                            size="small">{{ row.timeSourceText || '未知' }}
                    </el-tag>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="90">
                <template #default="{ row }">
                  <el-tag :data-testid="`p5-sign-row-status-${row.signNo}`"
                          :type="row.signStatus === 1 ? 'success' : 'info'"
                          size="small">
                    {{ row.signStatusText }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="验签" width="110">
                <template #default="{ row }">
                  <el-tag :data-testid="`p5-sign-row-verify-${row.signNo}`" :type="verifyTag(row.verifyStatus)"
                          size="small">
                    {{ row.verifyStatusText }}
                  </el-tag>
                  <div v-if="row.verifyTime" class="mt-0.5 text-xs text-slate-400">{{ row.verifyTime }}</div>
                </template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="80">
                <template #default="{ row }">
                  <el-button :data-testid="`p5-sign-open-${row.signNo}`" link size="small" type="primary"
                             @click.stop="openDetail(row)">详情
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-6 text-sm text-slate-400">
                  暂无签名记录。病历提交 / 归档、医嘱开立 / 校对时会自动产生签名；本页也支持管理员对未签名对象发起补签。
                </div>
              </template>
            </el-table>

            <div class="mt-3 flex items-center justify-between">
              <p class="text-xs text-slate-500" data-testid="p5-sign-total">共 {{ signTotal }} 条签名记录（含已作废）</p>
              <el-pagination
                  v-model:current-page="signQuery.pageNum"
                  v-model:page-size="signQuery.pageSize"
                  :page-sizes="PAGE_SIZES"
                  :total="signTotal"
                  layout="sizes, prev, pager, next"
                  @current-change="loadSignList"
                  @size-change="() => { signQuery.pageNum = 1; loadSignList() }"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ---------------- 签名证书 ---------------- -->
      <el-tab-pane label="签名证书" name="cert">
        <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="flex flex-wrap items-center gap-2 border-b border-slate-100 px-4 py-3">
            <el-select v-model="certQuery.certStatus" clearable data-testid="p5-sign-f-certstatus"
                       placeholder="证书状态"
                       style="width: 130px" @change="() => { certQuery.pageNum = 1; loadCertList() }">
              <el-option v-for="o in options.certStatuses || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
            </el-select>
            <el-select v-model="certQuery.issuedMode" clearable data-testid="p5-sign-f-issuedmode"
                       placeholder="签发方式"
                       style="width: 130px" @change="() => { certQuery.pageNum = 1; loadCertList() }">
              <el-option v-for="o in options.issuedModes || []" :key="o.id" :label="o.text" :value="Number(o.id)"/>
            </el-select>
            <el-input v-model="certQuery.keyword" clearable data-testid="p5-sign-f-certkeyword"
                      placeholder="证书号 / 员工姓名 / 指纹"
                      style="width: 230px"
                      @keyup.enter="() => { certQuery.pageNum = 1; loadCertList() }">
              <template #prefix>
                <el-icon>
                  <Search/>
                </el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="() => { certQuery.pageNum = 1; loadCertList() }">查询</el-button>
            <el-button data-testid="p5-sign-issue-open" plain type="primary" @click="openIssue">人工签发证书</el-button>
          </div>

          <div v-loading="certLoading" class="p-4">
            <el-table :data="certList" border data-testid="p5-sign-cert-table" size="small">
              <el-table-column label="证书编号" width="180">
                <template #default="{ row }">
                  <span class="text-xs font-medium text-slate-700">{{ row.certNo }}</span>
                </template>
              </el-table-column>
              <el-table-column label="持有人" width="140">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.empName || '—' }}</div>
                  <div class="text-xs text-slate-400">{{ row.deptName || '—' }}</div>
                </template>
              </el-table-column>
              <el-table-column label="算法" width="200">
                <template #default="{ row }">
                  <span class="text-xs text-slate-600">{{ row.keyAlgo }} / {{ row.digestAlgo }} / {{
                      row.signAlgo
                    }}</span>
                </template>
              </el-table-column>
              <el-table-column label="公钥指纹" width="200">
                <template #default="{ row }">
                  <span :data-testid="`p5-sign-cert-fp-${row.certNo}`"
                        class="font-mono text-xs text-slate-600">{{ row.keyFingerprintGroups }}</span>
                </template>
              </el-table-column>
              <el-table-column label="签发方式" width="100">
                <template #default="{ row }">
                  <el-tag :data-testid="`p5-sign-cert-mode-${row.certNo}`"
                          :type="row.issuedMode === 1 ? 'success' : 'warning'"
                          size="small">
                    {{ row.issuedModeText }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="状态" width="120">
                <template #default="{ row }">
                  <el-tag :type="row.certStatus === 1 ? 'success' : 'info'" size="small">{{
                      row.certStatusText
                    }}
                  </el-tag>
                  <!-- 状态"有效"但有效期已过：这是两个独立事实，必须分开提示 -->
                  <div v-if="row.expired" :data-testid="`p5-sign-cert-expired-${row.certNo}`"
                       class="mt-0.5 text-xs text-red-600">已过期
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="有效期" width="190">
                <template #default="{ row }">
                  <div class="text-xs">{{ row.validFrom || '—' }}</div>
                  <div class="text-xs text-slate-400">至 {{ row.validTo || '—' }}</div>
                </template>
              </el-table-column>
              <el-table-column label="使用情况" width="120">
                <template #default="{ row }">
                  <div class="text-xs">签名 {{ num(row.signCount) }} 次</div>
                  <div class="text-xs text-slate-400">{{ row.lastUsedTime || '未使用' }}</div>
                </template>
              </el-table-column>
              <el-table-column fixed="right" label="操作" width="130">
                <template #default="{ row }">
                  <el-button link size="small" type="primary" @click="openCertDetail(row)">详情</el-button>
                  <el-button
                      v-if="row.canRevoke"
                      :data-testid="`p5-sign-cert-revoke-${row.certNo}`"
                      link
                      size="small"
                      type="danger"
                      @click="openRevoke(row)"
                  >吊销
                  </el-button>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-6 text-sm text-slate-400">暂无签名证书</div>
              </template>
            </el-table>

            <div class="mt-3 flex items-center justify-between">
              <p class="text-xs text-slate-500" data-testid="p5-sign-cert-total">共 {{ certTotal }} 张证书</p>
              <el-pagination
                  v-model:current-page="certQuery.pageNum"
                  v-model:page-size="certQuery.pageSize"
                  :page-sizes="PAGE_SIZES"
                  :total="certTotal"
                  layout="sizes, prev, pager, next"
                  @current-change="loadCertList"
                  @size-change="() => { certQuery.pageNum = 1; loadCertList() }"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>

      <!-- ---------------- 时间戳台账（TSA） ---------------- -->
      <el-tab-pane label="时间戳台账" name="tsa">
        <div class="rounded-lg border border-slate-200 bg-white shadow-sm">
          <div class="flex flex-wrap items-center gap-2 border-b border-slate-100 px-4 py-3">
            <div class="mr-auto">
              <h2 class="text-sm font-semibold text-slate-900">时间戳令牌台账（只增不改）</h2>
              <p class="mt-0.5 text-xs text-slate-500">
                TSA 每盖一次章落一行：序列号 + 内容摘要 + 授时时刻 + 令牌值（TSA 私钥签名）。
                签名行上冗余一份令牌，两处比对可发现任何一方被删改。
              </p>
              <p v-if="tsaStatus" :class="tsaStatus.available ? 'text-emerald-700' : 'text-amber-700'"
                 class="mt-0.5 text-xs" data-testid="g6-tsa-token-status">
                {{
                  tsaStatus.available
                      ? `服务在线：${text(tsaStatus.tsaName)} · 公钥指纹 ${text(tsaStatus.keyFingerprintGroups)}`
                      : '服务不在线：签名时间生效「本机时钟」，不会有新令牌产生'
                }}
              </p>
            </div>
            <el-input v-model="tsaTokenQuery.serial" clearable data-testid="g6-tsa-f-serial"
                      placeholder="按序列号精确查"
                      style="width: 220px" @clear="searchTsaToken" @keyup.enter="searchTsaToken">
              <template #prefix>
                <el-icon>
                  <Search/>
                </el-icon>
              </template>
            </el-input>
            <el-button data-testid="g6-tsa-search" type="primary" @click="searchTsaToken">查询</el-button>
          </div>

          <div v-loading="tsaTokenLoading" class="p-4">
            <el-table :data="tsaTokenList" border data-testid="g6-tsa-token-table" size="small">
              <el-table-column label="序列号" width="190">
                <template #default="{ row }">
                  <span :data-testid="`g6-tsa-token-row-${row.serial}`"
                        class="text-xs font-medium text-slate-700">{{ row.serial }}</span>
                </template>
              </el-table-column>
              <el-table-column label="内容摘要" width="240">
                <template #default="{ row }">
                  <span class="break-all font-mono text-xs text-slate-600">{{ row.digestHex }}</span>
                </template>
              </el-table-column>
              <el-table-column label="授时时刻" width="170">
                <template #default="{ row }">
                  <span class="text-xs">{{ row.tsaTime || '—' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="算法" width="150">
                <template #default="{ row }">
                  <span class="text-xs text-slate-600">{{ row.algo }}</span>
                </template>
              </el-table-column>
              <el-table-column label="落账时间" width="170">
                <template #default="{ row }">
                  <span class="text-xs text-slate-400">{{ row.createTime || '—' }}</span>
                </template>
              </el-table-column>
              <el-table-column label="复验" min-width="150">
                <template #default="{ row }">
                  <el-button
                      :data-testid="`g6b-verify-${row.serial}`"
                      :loading="verifyingTokenId === String(row.id)"
                      link
                      size="small"
                      type="primary"
                      @click="onVerifyTsaToken(row)"
                  >复验
                  </el-button>
                  <span
                      v-if="tsaVerifyResult[row.id]"
                      :class="tsaVerifyResult[row.id].valid ? 'text-emerald-700' : 'text-rose-700'"
                      :data-testid="`g6b-verify-result-${row.serial}`"
                      :title="tsaVerifyResult[row.id].failReason || ''"
                      class="ml-1 text-xs font-medium"
                  >{{ tsaVerifyResult[row.id].valid ? '✓ 通过' : '✗ 不通过' }}</span>
                </template>
              </el-table-column>
              <template #empty>
                <div class="py-6 text-sm text-slate-400">
                  暂无时间戳令牌。将 sys_config 的 sign.time_source 配为 3 且 TSA 在线后，新签名会自动盖章并落台账。
                </div>
              </template>
            </el-table>

            <div class="mt-3 flex items-center justify-between">
              <p class="text-xs text-slate-500" data-testid="g6-tsa-token-total">共 {{ tsaTokenTotal }} 枚令牌</p>
              <el-pagination
                  v-model:current-page="tsaTokenQuery.pageNum"
                  v-model:page-size="tsaTokenQuery.pageSize"
                  :page-sizes="PAGE_SIZES"
                  :total="tsaTokenTotal"
                  layout="sizes, prev, pager, next"
                  @current-change="loadTsaTokenList"
                  @size-change="() => { tsaTokenQuery.pageNum = 1; loadTsaTokenList() }"
              />
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ---------------- 签名详情抽屉（子组件 SignatureCenterDetail） ---------------- -->
    <SignatureCenterDetail v-model="drawerVisible" :id="detailId" @saved="onDetailSaved"/>

    <!-- ---------------- 证书详情（子组件 SignatureCenterCertDetail） ---------------- -->
    <SignatureCenterCertDetail v-model="certDetailVisible" :id="certDetailId"/>

    <!-- ---------------- 签发证书（子组件 SignatureCenterCertIssue） ---------------- -->
    <SignatureCenterCertIssue v-model="issueVisible" @saved="onIssueSaved"/>

    <!-- ---------------- 吊销证书 ---------------- -->
    <el-dialog v-model="revokeVisible" title="吊销签名证书" width="480px">
      <p class="text-sm">证书：<b>{{ revokeForm.certNo }}</b></p>
      <p class="mt-2 text-xs leading-5 text-slate-500">
        吊销只改状态，不删行 —— 该证书历史签过的签名仍然可以用它留下的公钥验签。吊销后该员工将无法再产生新签名。
      </p>
      <el-input v-model="revokeForm.reason" class="mt-3" data-testid="p5-sign-revoke-reason"
                placeholder="吊销理由（必填）"/>
      <template #footer>
        <el-button @click="revokeVisible = false">取消</el-button>
        <el-button :loading="revoking" data-testid="p5-sign-revoke-submit" type="danger" @click="submitRevoke">
          确认吊销
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 电子签名与时间戳 · 签名中心（P5.5）
 *
 * 这个页面回答的问题不是"有多少签名"，而是"**这份病历的签名还能不能证明它没被改过**"。
 *
 * 五条必须写在页面上的口径（否则数字与结论会被误读）：
 *   1. **签名是证据，不是状态**。签名一旦落库只增不改不删；作废只是追加作废信息，
 *      历史行、历史签名值、历史被签内容快照全部保留。所以列表默认带出已作废签名，
 *      "已作废 3 条"不是异常，而是"这 3 次签字动作真实发生过"。
 *   2. **验签是两个独立断言**：签名值校验（证据本身有没有被换）与内容比对（签名之后内容有没有被改）。
 *      合成一个"通过/不通过"会丢掉最关键的信息 —— 两者性质与处理方式完全不同。
 *   3. **时间戳来源必须自报**。本期时间取的是**本机时钟**，不具备可信时间效力；
 *      页面上原样展示来源，不得表述为"可信时间戳"。
 *   4. **证书是院内托管的**，不是 CA 签发的。自动签发的证书信任级别低于人工签发。
 *   5. **存量病历的签名补不回来**。库里已归档病历都是签名能力上线之前产生的，
 *      它们没有签名；覆盖率表把"未签名（存量）"单独列出来，显示为 0% 而不是藏起来。
 *      补签等于伪造，本系统不允许对历史归档文书补签。
 */
import {computed, onMounted, reactive, ref} from 'vue';
import {Search, WarningFilled} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  getSignatureList,
  getSignatureQueryOptions,
  getSignatureSummary,
  getSignCertList,
  getTsaStatus,
  getTsaTokenList,
  revokeSignCert,
  updateTsaStatus,
  updateTsaTimeSource,
  verifyTsaToken,
} from '@/api/signature';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';
import {num, text, timeSourceTag} from '@/lib/sign';
import SignatureCenterDetail from './SignatureCenterDetail.vue';
import SignatureCenterCertDetail from './SignatureCenterCertDetail.vue';
import SignatureCenterCertIssue from './SignatureCenterCertIssue.vue';
// ---------------- 概览 ----------------
const overviewLoading = ref(false);
const overview = ref(null);
// ---------------- 下拉选项 ----------------
const options = ref({});
// ---------------- 签名记录 ----------------
const signLoading = ref(false);
const signList = ref([]);
const signTotal = ref(0);
const signQuery = reactive({
  bizType: undefined,
  signScene: undefined,
  signStatus: undefined,
  verifyStatus: undefined,
  timeSource: undefined,
  keyword: '',
  beginTime: undefined,
  endTime: undefined,
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
const timeRange = ref(null);
// ---------------- 详情抽屉（子组件 SignatureCenterDetail） ----------------
const drawerVisible = ref(false);
const detailId = ref(null);
// ---------------- 证书 ----------------
const certLoading = ref(false);
const certList = ref([]);
const certTotal = ref(0);
const certQuery = reactive({
  certStatus: undefined,
  issuedMode: undefined,
  keyword: '',
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
});
// 证书详情（子组件 SignatureCenterCertDetail）
const certDetailVisible = ref(false);
const certDetailId = ref(null);
// 签发证书（子组件 SignatureCenterCertIssue）
const issueVisible = ref(false);
// 吊销证书
const revokeVisible = ref(false);
const revoking = ref(false);
const revokeForm = reactive({certId: '', certNo: '', reason: ''});
// ---------------- 时间戳（TSA） ----------------
const tsaStatus = ref(null);
const tsaLoading = ref(false);
const tsaTokenLoading = ref(false);
const tsaTokenList = ref([]);
const tsaTokenTotal = ref(0);
const tsaTokenQuery = reactive({serial: '', pageNum: 1, pageSize: DEFAULT_PAGE_SIZE});
const loadTsaStatus = async () => {
  tsaLoading.value = true;
  try {
    const res = await getTsaStatus();
    tsaStatus.value = res.data;
    syncSourceDraft();
  } catch (e) {
    ElMessage.error(e?.message || '加载TSA状态失败');
  } finally {
    tsaLoading.value = false;
  }
};
const loadTsaTokenList = async () => {
  tsaTokenLoading.value = true;
  try {
    const params = {pageNum: tsaTokenQuery.pageNum, pageSize: tsaTokenQuery.pageSize};
    if (tsaTokenQuery.serial)
      params.serial = tsaTokenQuery.serial.trim();
    const res = await getTsaTokenList(params);
    tsaTokenList.value = res.data?.records || [];
    tsaTokenTotal.value = Number(res.data?.total ?? 0);
  } catch (e) {
    ElMessage.error(e?.message || '加载时间戳台账失败');
  } finally {
    tsaTokenLoading.value = false;
  }
};
const searchTsaToken = () => {
  tsaTokenQuery.pageNum = 1;
  loadTsaTokenList();
};
// ---------------- TSA 运维（G6b）：启停 / 时间来源 / 令牌复验 ----------------
const tsaOpsLoading = ref(false);
// 单选框的草稿值：接口成功后以服务端 configTimeSource 为准回填，取消/失败回滚
const tsaSourceDraft = ref(1);
const syncSourceDraft = () => {
  tsaSourceDraft.value = tsaStatus.value?.configTimeSource === 3 ? 3 : 1;
};
const onTimeSourceChange = async (val) => {
  const target = val === 3 ? '可信时间戳（TSA 盖章）' : '本机时钟';
  const warn = val === 3 && !tsaStatus.value?.available
      ? '注意：TSA 服务当前不在线，配置会保存，但实际生效的仍是「本机时钟」（宁可承认不可信，也不谎报可信）。'
      : '';
  try {
    await ElMessageBox.confirm(`确认把签名时间来源切换为「${target}」？${warn}`, '切换时间来源', {
      type: 'warning', confirmButtonText: '确认切换', cancelButtonText: '取消',
    });
  } catch {
    syncSourceDraft();
    return;
  }
  tsaOpsLoading.value = true;
  try {
    const res = await updateTsaTimeSource(val);
    tsaStatus.value = res.data;
    syncSourceDraft();
    ElMessage.success('时间来源已切换');
  } catch (e) {
    ElMessage.error(e?.message || '切换失败');
    syncSourceDraft();
  } finally {
    tsaOpsLoading.value = false;
  }
};
const onToggleTsa = async () => {
  const stop = !!tsaStatus.value?.available;
  const msg = stop
      ? '停用后 TSA 不再签发新令牌，新签名时间将降级为「本机时钟」；历史令牌凭已登记公钥仍可验证。确认停用？'
      : '启用后，时间来源配为「可信时间戳」的新签名将重新由 TSA 盖章。确认启用？';
  try {
    await ElMessageBox.confirm(msg, stop ? '停用 TSA 服务' : '启用 TSA 服务', {
      type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消',
    });
  } catch {
    return;
  }
  tsaOpsLoading.value = true;
  try {
    const res = await updateTsaStatus(stop ? 0 : 1);
    tsaStatus.value = res.data;
    ElMessage.success(stop ? 'TSA 已停用' : 'TSA 已启用');
  } catch (e) {
    ElMessage.error(e?.message || '操作失败');
  } finally {
    tsaOpsLoading.value = false;
  }
};
const verifyingTokenId = ref('');
const tsaVerifyResult = reactive({});
const onVerifyTsaToken = async (row) => {
  verifyingTokenId.value = String(row.id);
  try {
    const res = await verifyTsaToken(row.id);
    tsaVerifyResult[row.id] = res.data;
    if (res.data?.valid) {
      ElMessage.success(`令牌 ${row.serial} 复验通过`);
    } else {
      ElMessage.warning(res.data?.failReason || '令牌复验不通过');
    }
  } catch (e) {
    ElMessage.error(e?.message || '复验失败');
  } finally {
    verifyingTokenId.value = '';
  }
};
// 台账 tab 首次点开时拉数据（懒加载；刷新走 tab 内查询按钮）
const onTabChange = (tab) => {
  if (tab === 'tsa' || tab?.props?.name === 'tsa') {
    if (!tsaTokenList.value.length)
      loadTsaTokenList();
  }
};
const activeTab = ref('sign');
// ---------------- 加载 ----------------
const loadOverview = async () => {
  overviewLoading.value = true;
  try {
    const res = await getSignatureSummary();
    overview.value = res.data;
  } catch (e) {
    ElMessage.error(e?.message || '加载签名概览失败');
  } finally {
    overviewLoading.value = false;
  }
};
const loadOptions = async () => {
  try {
    const res = await getSignatureQueryOptions();
    options.value = res.data || {};
  } catch (e) {
    ElMessage.error(e?.message || '加载下拉选项失败');
  }
};
const loadSignList = async () => {
  signLoading.value = true;
  try {
    const params = {pageNum: signQuery.pageNum, pageSize: signQuery.pageSize};
    if (signQuery.bizType !== undefined && signQuery.bizType !== null)
      params.bizType = signQuery.bizType;
    if (signQuery.signScene !== undefined && signQuery.signScene !== null)
      params.signScene = signQuery.signScene;
    if (signQuery.signStatus !== undefined && signQuery.signStatus !== null)
      params.signStatus = signQuery.signStatus;
    if (signQuery.verifyStatus !== undefined && signQuery.verifyStatus !== null)
      params.verifyStatus = signQuery.verifyStatus;
    if (signQuery.timeSource !== undefined && signQuery.timeSource !== null)
      params.timeSource = signQuery.timeSource;
    if (signQuery.keyword)
      params.keyword = signQuery.keyword.trim();
    if (timeRange.value && timeRange.value[0])
      params.beginTime = timeRange.value[0];
    if (timeRange.value && timeRange.value[1])
      params.endTime = timeRange.value[1];
    const res = await getSignatureList(params);
    signList.value = res.data?.records || [];
    signTotal.value = Number(res.data?.total ?? 0);
  } catch (e) {
    ElMessage.error(e?.message || '加载签名记录失败');
  } finally {
    signLoading.value = false;
  }
};
const loadCertList = async () => {
  certLoading.value = true;
  try {
    const params = {pageNum: certQuery.pageNum, pageSize: certQuery.pageSize};
    if (certQuery.certStatus !== undefined && certQuery.certStatus !== null)
      params.certStatus = certQuery.certStatus;
    if (certQuery.issuedMode !== undefined && certQuery.issuedMode !== null)
      params.issuedMode = certQuery.issuedMode;
    if (certQuery.keyword)
      params.keyword = certQuery.keyword.trim();
    const res = await getSignCertList(params);
    certList.value = res.data?.records || [];
    certTotal.value = Number(res.data?.total ?? 0);
  } catch (e) {
    ElMessage.error(e?.message || '加载证书列表失败');
  } finally {
    certLoading.value = false;
  }
};
const refreshAll = async () => {
  await Promise.all([loadOverview(), loadSignList(), loadCertList(), loadTsaStatus()]);
};
const searchSign = () => {
  signQuery.pageNum = 1;
  loadSignList();
};
const clearSignFilter = () => {
  signQuery.bizType = undefined;
  signQuery.signScene = undefined;
  signQuery.signStatus = undefined;
  signQuery.verifyStatus = undefined;
  signQuery.timeSource = undefined;
  signQuery.keyword = '';
  timeRange.value = null;
  signQuery.pageNum = 1;
  loadSignList();
};
// ---------------- 详情 / 验签 / 作废（子组件 SignatureCenterDetail） ----------------
const openDetail = (row) => {
  detailId.value = row.id;
  drawerVisible.value = true;
};
const onDetailSaved = async () => {
  await Promise.all([loadSignList(), loadOverview()]);
};
// ---------------- 证书操作 ----------------
const openCertDetail = (row) => {
  certDetailId.value = row.id;
  certDetailVisible.value = true;
};
const openIssue = () => {
  issueVisible.value = true;
};
const onIssueSaved = async () => {
  await Promise.all([loadCertList(), loadOverview()]);
};
const openRevoke = (row) => {
  revokeForm.certId = row.id;
  revokeForm.certNo = row.certNo;
  revokeForm.reason = '';
  revokeVisible.value = true;
};
const submitRevoke = async () => {
  if (!revokeForm.reason.trim()) {
    ElMessage.warning('吊销必须写理由');
    return;
  }
  revoking.value = true;
  try {
    await revokeSignCert(revokeForm.certId, revokeForm.reason.trim());
    ElMessage.success('已吊销该证书（历史签名仍可用其公钥验签）');
    revokeVisible.value = false;
    await Promise.all([loadCertList(), loadOverview()]);
  } catch (e) {
    ElMessage.error(e?.message || '吊销失败');
  } finally {
    revoking.value = false;
  }
};
// ---------------- 展示辅助 ----------------
const coverages = computed(() => overview.value?.coverages || []);
const pendingTotal = computed(() => coverages.value.reduce((sum, c) => sum + num(c.pendingSign), 0));
const invalidatedTotal = computed(() => coverages.value.reduce((sum, c) => sum + num(c.invalidated), 0));
// 覆盖率占位：分母为 0 时后端给的是「—（该类型还没有可统计的对象）」，不是 "0.0%" —— 别把它当比率渲染
const isRatePlaceholder = (t) => !t || String(t).startsWith('—');
// 验签状态：0-未校验（灰）1-通过（绿）2-失败（红）。0 必须显式给一档，
// 否则它会落到最后的分支上被渲染成"失败"，而"从没验过"和"验过发现被改"完全是两件事。
const verifyTag = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'info');
onMounted(async () => {
  await loadOptions();
  await refreshAll();
});
</script>
