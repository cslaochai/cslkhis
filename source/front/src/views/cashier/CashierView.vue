<template>
  <div class="space-y-4">
    <el-card shadow="never">
      <div class="flex flex-wrap items-center gap-3">
        <el-radio-group v-model="encounterType" @change="switchEncounterType">
          <el-radio-button :value="1">门诊</el-radio-button>
          <el-radio-button :value="2">住院</el-radio-button>
        </el-radio-group>
        <el-input
            v-model="keyword"
            clearable
            placeholder="患者姓名 / 患者号 / 就诊单号"
            style="width: 260px"
            @keyup.enter="handleSearch"
        />
        <el-button :icon="Search" data-testid="cash-search" type="primary" @click="handleSearch">查询</el-button>
        <el-button :icon="Refresh" @click="handleReset">重置</el-button>
      </div>
    </el-card>

    <el-card shadow="never">
      <el-table
          v-loading="boardLoading"
          :data="boardRows"
          data-testid="cash-board-table"
          stripe
          style="width: 100%"
      >
        <el-table-column label="患者" prop="patientName" width="110"/>
        <el-table-column label="患者号" prop="patientNo" show-overflow-tooltip width="150"/>
        <el-table-column label="就诊单号" min-width="180" prop="encounterNo" show-overflow-tooltip/>
        <el-table-column align="right" label="待出账应收" width="170">
          <template #default="{ row }">
            <span v-if="Number(row.pendingFeeCount) > 0" class="font-medium text-amber-600">
              ¥{{ money(row.pendingFeeAmount) }}
              <b class="text-xs font-normal text-slate-400">/ {{ row.pendingFeeCount }} 行</b>
            </span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column align="right" label="未收账单差额" width="170">
          <template #default="{ row }">
            <span v-if="Number(row.unpaidBillCount) > 0" class="font-medium text-red-600">
              ¥{{ money(row.unpaidBillAmount) }}
              <b class="text-xs font-normal text-slate-400">/ {{ row.unpaidBillCount }} 张</b>
            </span>
            <span v-else class="text-slate-300">—</span>
          </template>
        </el-table-column>
        <el-table-column label="最近发生" prop="lastTime" width="180"/>
        <el-table-column fixed="right" label="操作" width="120">
          <template #default="{ row }">
            <el-button :icon="Coin" data-testid="cash-open" link type="primary" @click="openWorkbench(row)">
              结算收款
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <span class="text-sm text-slate-400">当前没有待收费的就诊</span>
        </template>
      </el-table>

      <div class="mt-4 flex justify-end">
        <el-pagination
            v-model:current-page="pagination.pageNum"
            v-model:page-size="pagination.pageSize"
            :page-sizes="PAGE_SIZES"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSearch"
            @current-change="loadBoard"
        />
      </div>
    </el-card>

    <el-dialog v-model="workbench" data-testid="cash-workbench" title="就诊结算工作台" width="1180px">
      <div v-if="current" v-loading="workLoading" class="space-y-4">
        <div class="rounded-lg bg-slate-50 p-3">
          <div class="grid grid-cols-2 gap-2 text-sm sm:grid-cols-4">
            <div><span class="text-slate-500">患者：</span><b>{{ current.patientName }}</b></div>
            <div><span class="text-slate-500">患者号：</span>{{ current.patientNo || '—' }}</div>
            <div><span class="text-slate-500">就诊单号：</span>{{ current.encounterNo || '—' }}</div>
            <div>
              <span class="text-slate-500">就诊类型：</span>
              {{ Number(current.encounterType) === 2 ? '住院' : '门诊' }}
            </div>
          </div>
        </div>

        <!-- 待结算记账行 -->
        <div>
          <div class="mb-2 flex items-center justify-between">
            <span class="text-sm text-slate-500">
              全部 ¥{{ money(feeTotal) }} · 已勾选 <b class="text-slate-900">{{ selectedFees.length }}</b> 行
              ¥{{ money(selectedTotal) }}
            </span>
          </div>
          <el-table
              :data="fees"
              data-testid="cash-fee-table"
              max-height="240"
              size="small"
              @selection-change="onFeeSelectionChange"
          >
            <el-table-column type="selection" width="42"/>
            <el-table-column label="记账号" prop="feeNo" width="150"/>
            <el-table-column label="项目名称" min-width="180" prop="itemName" show-overflow-tooltip/>
            <el-table-column label="类别" width="90">
              <template #default="{ row }">
                <el-tag size="small">{{ itemTypeText(row.itemType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="来源" width="100">
              <template #default="{ row }">
                <span class="text-xs text-slate-500">{{ sourceTypeText(row.sourceType) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="来源单号" prop="sourceNo" show-overflow-tooltip width="160"/>
            <el-table-column align="right" label="数量" width="70">
              <template #default="{ row }">{{ row.quantity }}</template>
            </el-table-column>
            <el-table-column align="right" label="单价" width="80">
              <template #default="{ row }">{{ money(row.price) }}</template>
            </el-table-column>
            <el-table-column align="right" label="金额" width="100">
              <template #default="{ row }">
                <span :class="Number(row.amount) < 0 ? 'text-emerald-600' : 'font-medium'">{{
                    money(row.amount)
                  }}</span>
              </template>
            </el-table-column>
            <el-table-column label="记账时间" prop="bookTime" width="160"/>
            <template #empty><span class="text-sm text-slate-400">该就诊没有待结算的记账行</span></template>
          </el-table>
        </div>

        <!-- 结算设置 + 试算 -->
        <div class="rounded-lg border border-slate-200 p-3">
          <div class="flex flex-wrap items-center gap-4">
            <div class="flex items-center gap-2">
              <span class="text-sm text-slate-500">结算方式</span>
              <el-select v-model="settleForm.settlementMode" class="!w-28" data-testid="cash-settle-mode"
                         @change="invalidatePreview">
                <el-option v-for="o in selectOptions(DICT_TYPE.SETTLEMENT_MODE)" :key="o.value" :label="o.label"
                           :value="o.value"/>
              </el-select>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-slate-500">账单类型</span>
              <el-select v-model="settleForm.billType" class="!w-40" clearable placeholder="自动"
                         @change="invalidatePreview">
                <el-option v-for="o in selectOptions(DICT_TYPE.BILL_TYPE)" :key="o.value" :label="o.label"
                           :value="o.value"/>
              </el-select>
            </div>
            <div class="flex items-center gap-2">
              <span class="text-sm text-slate-500">院内优惠</span>
              <el-input-number
                  v-model="settleForm.discountAmount"
                  :min="0"
                  :precision="2"
                  :step="10"
                  class="!w-36"
                  controls-position="right"
                  data-testid="cash-discount"
                  @change="invalidatePreview"
              />
            </div>
            <el-button :icon="Document" :loading="previewLoading" data-testid="cash-preview" @click="runPreview">
              出账试算
            </el-button>
            <el-button
                :disabled="!preview"
                :icon="Coin"
                :loading="settling"
                data-testid="cash-settle"
                type="primary"
                @click="runSettle"
            >
              确认出账
            </el-button>
          </div>

          <div v-if="preview" class="mt-3 space-y-2" data-testid="cash-preview-result">
            <div class="grid grid-cols-3 gap-2 text-sm sm:grid-cols-8">
              <div><p class="text-xs text-slate-400">记账行数</p><b>{{ preview.feeCount }}</b></div>
              <div><p class="text-xs text-slate-400">应收合计</p><b>¥{{ money(preview.totalAmount) }}</b></div>
              <div><p class="text-xs text-slate-400">优惠</p><b>¥{{ money(preview.discountAmount) }}</b></div>
              <div><p class="text-xs text-slate-400">统筹</p><b class="text-emerald-700">¥{{
                  money(preview.poolAmount)
                }}</b></div>
              <div><p class="text-xs text-slate-400">个账</p><b
                  class="text-emerald-700">¥{{ money(preview.accountAmount) }}</b></div>
              <div><p class="text-xs text-slate-400">自付</p><b>¥{{ money(preview.selfAmount) }}</b></div>
              <div><p class="text-xs text-slate-400">患者应缴</p><b
                  class="text-red-600">¥{{ money(preview.payableAmount) }}</b></div>
              <div>
                <p class="text-xs text-slate-400">{{ preview.settlementModeText }}</p>
                <b class="text-sm">{{ preview.insuranceType || '—' }}
                  <span v-if="preview.coverageRatio != null" class="text-xs font-normal text-slate-500">
                    报销 {{ money(preview.coverageRatio) }}%
                  </span>
                </b>
              </div>
            </div>
            <el-table :data="preview.items || []" max-height="200" size="small">
              <el-table-column label="项目" min-width="180" prop="itemName" show-overflow-tooltip/>
              <el-table-column label="类别" width="90">
                <template #default="{ row }">{{ itemTypeText(row.itemType) }}</template>
              </el-table-column>
              <el-table-column align="center" label="目录" width="70">
                <template #default="{ row }">
                  {{ ['自费', '甲类', '乙类', '丙类'][Number(row.catalogType)] || '—' }}
                </template>
              </el-table-column>
              <el-table-column align="right" label="金额" width="100">
                <template #default="{ row }">{{ money(row.amount) }}</template>
              </el-table-column>
              <el-table-column align="right" label="优惠" width="90">
                <template #default="{ row }">{{ money(row.discountAmount) }}</template>
              </el-table-column>
              <el-table-column align="right" label="统筹" width="90">
                <template #default="{ row }">{{ money(row.poolAmount) }}</template>
              </el-table-column>
              <el-table-column align="right" label="个账" width="90">
                <template #default="{ row }">{{ money(row.accountAmount) }}</template>
              </el-table-column>
              <el-table-column align="right" label="自付" width="90">
                <template #default="{ row }">
                  <span class="font-medium">{{ money(row.selfAmount) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>

        <!-- 本次就诊已出账单 -->
        <div>
          <div class="mb-2 flex items-center justify-between">
            <h4 class="text-sm font-bold text-slate-700">本次就诊的账单（L2）</h4>
            <span class="text-sm text-slate-500">未收齐合计 <b class="text-red-600">¥{{
                money(unpaidBillAmount)
              }}</b></span>
          </div>
          <el-table :data="bills" data-testid="cash-bill-table" max-height="220" size="small">
            <el-table-column label="账单号" prop="billNo" width="160"/>
            <el-table-column label="类型" width="120">
              <template #default="{ row }">{{ billTypeText(row.billType) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="billStatusTag(row.billStatus)" size="small">{{ billStatusText(row.billStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column align="right" label="应收" width="100">
              <template #default="{ row }">{{ money(row.totalAmount) }}</template>
            </el-table-column>
            <el-table-column align="right" label="优惠" width="90">
              <template #default="{ row }">{{ money(row.discountAmount) }}</template>
            </el-table-column>
            <el-table-column align="right" label="统筹" width="90">
              <template #default="{ row }">{{ money(row.poolAmount) }}</template>
            </el-table-column>
            <el-table-column align="right" label="应缴" width="100">
              <template #default="{ row }"><b>{{ money(row.payableAmount) }}</b></template>
            </el-table-column>
            <el-table-column align="right" label="已收" width="100">
              <template #default="{ row }">{{ money(row.paidAmount) }}</template>
            </el-table-column>
            <el-table-column align="right" label="尚需缴" width="100">
              <template #default="{ row }">
                <span :class="Number(row.unpaidAmount) > 0 ? 'font-medium text-red-600' : 'text-slate-400'">
                  {{ money(row.unpaidAmount) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="出账人" prop="billByName" width="100"/>
            <el-table-column label="出账时间" prop="billTime" width="160"/>
            <el-table-column fixed="right" label="操作" width="230">
              <template #default="{ row }">
                <el-button v-if="Number(row.unpaidAmount) > 0" data-testid="cash-btn-pay" link type="primary"
                           @click="openPay(row)">收款
                </el-button>
                <el-button v-if="Number(row.billStatus) === 3" link type="success" @click="openRefund(row)">退费
                </el-button>
                <el-button v-if="Number(row.billStatus) === 1" link type="warning" @click="runVoid(row)">作废
                </el-button>
                <el-button link @click="openDetail(row)">详情</el-button>
              </template>
            </el-table-column>
            <template #empty><span class="text-sm text-slate-400">该就诊还没有账单</span></template>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="workbench = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- ============ 收款 ============ -->
    <el-dialog v-model="payDialog" data-testid="cash-pay-dialog" title="账单收款（一笔钱一行流水）" width="860px">
      <div v-if="payBillRow" v-loading="payLoading" class="space-y-3">
        <div class="rounded-lg bg-slate-50 p-3">
          <div class="grid grid-cols-2 gap-2 text-sm sm:grid-cols-6">
            <div><span class="text-slate-500">账单号</span><br/>{{ payBillRow.billNo }}</div>
            <div><span class="text-slate-500">患者</span><br/>{{ payBillRow.patientName }}</div>
            <div><span class="text-slate-500">应缴</span><br/><b>¥{{ money(payBillRow.payableAmount) }}</b></div>
            <div><span class="text-slate-500">已收</span><br/>¥{{ money(payBillRow.paidAmount) }}</div>
            <div><span class="text-slate-500">已退</span><br/>¥{{ money(payBillRow.refundAmount) }}</div>
            <div><span class="text-slate-500">尚需缴</span><br/><b class="text-red-600">¥{{ money(payRemaining) }}</b>
            </div>
          </div>
        </div>

        <div class="flex items-center justify-between">
          <h4 class="text-sm font-bold text-slate-700">收款明细</h4>
          <div class="flex items-center gap-3">
            <span class="text-sm text-slate-500">本次合计 <b class="text-slate-900">¥{{ money(payTotal) }}</b></span>
            <el-button link type="primary" @click="addPayItem">加一笔</el-button>
          </div>
        </div>
        <el-table :data="payItems" data-testid="cash-pay-items" size="small">
          <el-table-column label="支付方式" width="180">
            <template #default="{ row }">
              <el-select v-model="row.payMethod" class="!w-full">
                <el-option v-for="o in selectOptions(DICT_TYPE.PAY_METHOD)" :key="o.value" :label="o.label"
                           :value="o.value"/>
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="金额" width="200">
            <template #default="{ row }">
              <el-input-number
                  v-model="row.amount"
                  :min="0"
                  :precision="2"
                  :step="10"
                  class="!w-full"
                  controls-position="right"
                  data-testid="cash-pay-amount"
              />
            </template>
          </el-table-column>
          <el-table-column label="备注" min-width="180">
            <template #default="{ row }">
              <el-input v-model="row.remark" placeholder="可留空"/>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button :icon="Remove" link type="danger" @click="removePayItem($index)">删</el-button>
            </template>
          </el-table-column>
        </el-table>
        <p class="text-xs text-slate-400">
          一笔钱一行、收退同表带符号：现金 30 + 余额 50 + 医保个账 20 就是三笔流水，
          合并成一笔会让退费无从知道该退回哪个渠道。「院内余额」按本就诊对应账户扣减。
        </p>

        <div v-if="payTxns.length" class="space-y-1">
          <h4 class="text-sm font-bold text-slate-700">本账单已有流水</h4>
          <el-table :data="payTxns" max-height="180" size="small">
            <el-table-column label="流水号" prop="txnNo" width="170"/>
            <el-table-column align="center" label="方向" width="70">
              <template #default="{ row }">{{ Number(row.direction) === 1 ? '收款' : '退款' }}</template>
            </el-table-column>
            <el-table-column label="方式" width="100">
              <template #default="{ row }">{{ payMethodText(row.payMethod) }}</template>
            </el-table-column>
            <el-table-column align="right" label="金额" width="110">
              <template #default="{ row }">
                <span :class="Number(row.amount) < 0 ? 'text-emerald-600' : 'font-medium'">{{
                    money(row.amount)
                  }}</span>
              </template>
            </el-table-column>
            <el-table-column label="来源" width="120">
              <template #default="{ row }">{{ txnSourceText(row.sourceType) }}</template>
            </el-table-column>
            <el-table-column label="渠道流水号" prop="channelTxnNo" show-overflow-tooltip width="180"/>
            <el-table-column label="收银" prop="cashierName" width="90"/>
            <el-table-column label="时间" prop="txnTime" width="160"/>
          </el-table>
        </div>
      </div>

      <template #footer>
        <el-button @click="payDialog = false">关闭</el-button>
        <el-button v-if="isPaid" :icon="Tickets" data-testid="cash-issue" type="success" @click="runIssue">
          按账单出票
        </el-button>
        <el-button
            :disabled="payRemaining <= 0"
            :icon="Coin"
            :loading="paying"
            data-testid="cash-pay-submit"
            type="primary"
            @click="submitPay"
        >
          确认收款 ¥{{ money(payTotal) }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 退费 ============ -->
    <el-dialog v-model="refundDialog" data-testid="cash-refund-dialog" title="按账单退费（红冲记账行 + 原路退回）"
               width="860px">
      <div v-if="refundBillRow" v-loading="refundLoading" class="space-y-3">
        <div class="rounded-lg bg-slate-50 p-3 text-sm">
          账单 {{ refundBillRow.billNo }} · {{ refundBillRow.patientName }} ·
          应缴 ¥{{ money(refundBillRow.payableAmount) }} · 已收 ¥{{ money(refundBillRow.paidAmount) }}
        </div>
        <p class="text-xs text-slate-500">
          退多少不由本页填金额：永远是「被红冲记账行的净额」。勾选全部行 = 整单退（医保清单会发 2305 冲正），
          只勾部分行时医保清单退回待重新结算。
        </p>
        <el-table
            :data="refundItems"
            data-testid="cash-refund-table"
            max-height="280"
            size="small"
            @selection-change="(rows: any[]) => (refundSelected = rows)"
        >
          <el-table-column type="selection" width="42"/>
          <el-table-column label="项目" min-width="180" prop="itemName" show-overflow-tooltip/>
          <el-table-column label="类别" width="90">
            <template #default="{ row }">{{ itemTypeText(row.itemType) }}</template>
          </el-table-column>
          <el-table-column align="right" label="数量" width="80">
            <template #default="{ row }">{{ row.quantity }}</template>
          </el-table-column>
          <el-table-column align="right" label="金额" width="100">
            <template #default="{ row }">{{ money(row.amount) }}</template>
          </el-table-column>
          <el-table-column align="right" label="统筹" width="100">
            <template #default="{ row }">{{ money(row.poolAmount) }}</template>
          </el-table-column>
          <el-table-column align="right" label="患者实付" width="110">
            <template #default="{ row }">{{ money(row.selfAmount) }}</template>
          </el-table-column>
        </el-table>
        <div class="flex items-center justify-between">
          <span class="text-sm text-slate-500">本次退费合计 <b class="text-red-600">¥{{ money(refundTotal) }}</b></span>
        </div>
        <el-input
            v-model="refundReason"
            :rows="2"
            data-testid="cash-refund-reason"
            maxlength="200"
            placeholder="退费原因（必填，会写入红冲行与退费流水）"
            show-word-limit
            type="textarea"
        />
      </div>

      <template #footer>
        <el-button @click="refundDialog = false">取消</el-button>
        <el-button :loading="refunding" data-testid="cash-refund-submit" type="danger" @click="submitRefund">
          确认退费
        </el-button>
      </template>
    </el-dialog>

    <!-- ============ 账单详情 ============ -->
    <el-dialog v-model="detailDialog" title="账单详情" width="960px">
      <div v-if="detail" v-loading="detailLoading" class="space-y-3">
        <el-descriptions :column="4" border size="small">
          <el-descriptions-item label="账单号">{{ detail.billNo }}</el-descriptions-item>
          <el-descriptions-item label="患者">{{ detail.patientName }} {{ detail.patientNo }}</el-descriptions-item>
          <el-descriptions-item label="就诊单号">{{ detail.encounterNo || '—' }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="billStatusTag(detail.billStatus)" size="small">{{
                billStatusText(detail.billStatus)
              }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="账单类型">{{ billTypeText(detail.billType) }}</el-descriptions-item>
          <el-descriptions-item label="结算方式">
            {{ Number(detail.settlementMode) === 2 ? `医保 ${detail.insuranceType || ''}` : '自费' }}
          </el-descriptions-item>
          <el-descriptions-item label="应收 / 优惠">{{ money(detail.totalAmount) }} / {{
              money(detail.discountAmount)
            }}
          </el-descriptions-item>
          <el-descriptions-item label="统筹 / 个账">{{ money(detail.poolAmount) }} / {{
              money(detail.accountAmount)
            }}
          </el-descriptions-item>
          <el-descriptions-item label="应缴">{{ money(detail.payableAmount) }}</el-descriptions-item>
          <el-descriptions-item label="已收 / 已退">{{ money(detail.paidAmount) }} / {{
              money(detail.refundAmount)
            }}
          </el-descriptions-item>
          <el-descriptions-item label="尚需缴">{{ money(detail.unpaidAmount) }}</el-descriptions-item>
          <el-descriptions-item label="收讫时间">{{ detail.payTime || '—' }}</el-descriptions-item>
          <el-descriptions-item label="出账人">{{ detail.billByName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="出账时间">{{ detail.billTime }}</el-descriptions-item>
          <el-descriptions-item :span="2" label="结清说明">{{ detail.closeReason || '—' }}</el-descriptions-item>
        </el-descriptions>

        <div>
          <h4 class="mb-1 text-sm font-bold text-slate-700">账单行快照（出账时冻结，不随记账行变化）</h4>
          <el-table :data="detail.items || []" max-height="240" size="small">
            <el-table-column label="项目" min-width="180" prop="itemName" show-overflow-tooltip/>
            <el-table-column label="科室" prop="deptName" show-overflow-tooltip width="120"/>
            <el-table-column align="right" label="数量×单价" width="130">
              <template #default="{ row }">{{ row.quantity }} × {{ money(row.price) }}</template>
            </el-table-column>
            <el-table-column align="right" label="金额" width="100">
              <template #default="{ row }">{{ money(row.amount) }}</template>
            </el-table-column>
            <el-table-column align="right" label="统筹" width="90">
              <template #default="{ row }">{{ money(row.poolAmount) }}</template>
            </el-table-column>
            <el-table-column align="right" label="自付" width="90">
              <template #default="{ row }">{{ money(row.selfAmount) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div>
          <h4 class="mb-1 text-sm font-bold text-slate-700">全部收/退流水（资金事实的唯一来源）</h4>
          <el-table :data="detail.txns || []" max-height="220" size="small">
            <el-table-column label="流水号" prop="txnNo" width="170"/>
            <el-table-column align="center" label="方向" width="70">
              <template #default="{ row }">{{ Number(row.direction) === 1 ? '收款' : '退款' }}</template>
            </el-table-column>
            <el-table-column label="方式" width="100">
              <template #default="{ row }">{{ payMethodText(row.payMethod) }}</template>
            </el-table-column>
            <el-table-column align="right" label="金额" width="110">
              <template #default="{ row }">
                <span :class="Number(row.amount) < 0 ? 'text-emerald-600' : 'font-medium'">{{
                    money(row.amount)
                  }}</span>
              </template>
            </el-table-column>
            <el-table-column label="来源" width="120">
              <template #default="{ row }">{{ txnSourceText(row.sourceType) }}</template>
            </el-table-column>
            <el-table-column label="渠道流水号" prop="channelTxnNo" show-overflow-tooltip width="170"/>
            <el-table-column label="收银" prop="cashierName" width="90"/>
            <el-table-column label="时间" prop="txnTime" width="160"/>
            <el-table-column label="原因" min-width="140" prop="reason" show-overflow-tooltip/>
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
/**
 * 收费结算窗口（四层：L1 记账 → L2 结算 → L3 支付 → L4 票据）
 *
 * 页面只走一条链，每一步都只碰自己那一层：
 *   待收费就诊榜 →（选记账行）出账单 → （多笔多渠道）收款 → 出票
 *
 * 三条前端必须守住的口径：
 * 1. **应收金额不由本页传入**：settle 只提交「哪些记账行 + 优惠多少 + 结算方式」，
 *    合计、统筹、应缴一律后端现算；试算（settlePreview）与出账共用同一份算法，
 *    所以「试算说该收多少」和「账单开出多少」必然是同一个数。
 * 2. **payableAmount 才是患者该掏的钱**：已减优惠、统筹、个账。
 *    selfAmount 是「应收 − 统筹」，仍带着优惠与个账两段，两者并排显示必然不等。
 *    统筹不是支付方式，不进现金清点，所以收款明细里没有「统筹」这一档。
 * 3. **是否付清由流水比出来**：本页不翻状态，只在收款后重新拉账单；
 *    unpaidAmount 由后端算，前端不再减一遍。
 */
import {computed, onMounted, ref} from 'vue';
import {Coin, Document, Refresh, Remove, Search, Tickets} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  getBillDetailById,
  getBillListPage,
  getPendingEncounterPage,
  listPendingFees,
  payBill,
  refundBill,
  settleBill,
  settlePreview,
  voidBill,
} from '@/api/settlementBill';
import {issueInvoice} from '@/api/invoice';
import {DICT_TYPE, loadDictDataMap} from '@/lib/dict-cache';
import {dictLabelText, formatMoney} from '@/lib/utils';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';

/** 金额累加先转成分，避免浮点误差把「合计」和后端算出来的差出 0.01 */
const sumMoney = (rows, key = 'amount') => rows.reduce((acc, r) => acc + Math.round(Number(r?.[key] || 0) * 100), 0) / 100;
const money = (v) => formatMoney(v);
// ==================== 字典 ====================
const dicts = ref({});
const dictOptions = (type) => dicts.value[type] || [];

/** 字典项给 el-select 用：dictValue 是字符串，页面里的码值一律按数字比较 */
function selectOptions(type) {
  return dictOptions(type).map((o) => ({value: Number(o.dictValue), label: o.dictLabel}));
}

const billStatusText = (v) => dictLabelText(dictOptions(DICT_TYPE.BILL_STATUS), v);
const billTypeText = (v) => dictLabelText(dictOptions(DICT_TYPE.BILL_TYPE), v);
const payMethodText = (v) => dictLabelText(dictOptions(DICT_TYPE.PAY_METHOD), v);
const txnSourceText = (v) => dictLabelText(dictOptions(DICT_TYPE.TXN_SOURCE), v);
const itemTypeText = (v) => dictLabelText(dictOptions(DICT_TYPE.CHARGE_ITEM_TYPE), v);
const sourceTypeText = (v) => dictLabelText(dictOptions(DICT_TYPE.FEE_SOURCE_TYPE), v);
// ==================== 待收费就诊榜 ====================
const boardLoading = ref(false);
const encounterType = ref(1);
const keyword = ref('');
const boardRows = ref([]);
const pagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});

async function loadBoard() {
  boardLoading.value = true;
  try {
    const res = await getPendingEncounterPage({
      encounterType: encounterType.value,
      keyword: keyword.value || undefined,
      pageNum: pagination.value.pageNum,
      pageSize: pagination.value.pageSize,
    });
    boardRows.value = res.data?.records || [];
    pagination.value.total = Number(res.data?.total || 0);
  } catch (error) {
    ElMessage.error(error.message || '加载待收费就诊失败');
  } finally {
    boardLoading.value = false;
  }
}

function handleSearch() {
  pagination.value.pageNum = 1;
  loadBoard();
}

function handleReset() {
  keyword.value = '';
  handleSearch();
}

function switchEncounterType() {
  pagination.value.pageNum = 1;
  loadBoard();
}

onMounted(async () => {
  dicts.value = await loadDictDataMap([
    DICT_TYPE.BILL_STATUS,
    DICT_TYPE.BILL_TYPE,
    DICT_TYPE.PAY_METHOD,
    DICT_TYPE.CHARGE_ITEM_TYPE,
    DICT_TYPE.FEE_SOURCE_TYPE,
    DICT_TYPE.TXN_SOURCE,
    DICT_TYPE.SETTLEMENT_MODE,
    DICT_TYPE.PAY_DIRECTION,
    DICT_TYPE.PAY_TXN_STATUS,
  ].join(','));
  await loadBoard();
});
const workbench = ref(false);
const workLoading = ref(false);
const current = ref(null);
const fees = ref([]);
const feeTotal = ref(0);
const unpaidBillAmount = ref(0);
const selectedFees = ref([]);
const bills = ref([]);
const settleForm = ref({settlementMode: 1, billType: null, discountAmount: 0});
const preview = ref(null);
const previewLoading = ref(false);
const settling = ref(false);
const selectedFeeIds = computed(() => selectedFees.value.map((f) => f.id));
const selectedTotal = computed(() => sumMoney(selectedFees.value));

async function openWorkbench(row) {
  current.value = row;
  workbench.value = true;
  settleForm.value = {settlementMode: 1, billType: null, discountAmount: 0};
  preview.value = null;
  selectedFees.value = [];
  await loadWorkbench();
}

async function loadWorkbench() {
  if (!current.value)
    return;
  workLoading.value = true;
  try {
    const [feeRes, billRes] = await Promise.all([
      listPendingFees(current.value.encounterType, current.value.encounterId),
      getBillListPage({
        encounterType: current.value.encounterType,
        encounterId: current.value.encounterId,
        pageNum: 1,
        pageSize: 50,
      }),
    ]);
    fees.value = feeRes.data?.fees || [];
    feeTotal.value = Number(feeRes.data?.totalAmount || 0);
    unpaidBillAmount.value = Number(feeRes.data?.unpaidBillAmount || 0);
    // 已作废的账单留在台账页看，收费台只关心还要处理的：待支付 / 部分支付 / 已支付（可退费出票）
    bills.value = (billRes.data?.records || []).filter((b) => Number(b.billStatus) !== 4);
  } catch (error) {
    ElMessage.error(error.message || '加载就诊费用失败');
  } finally {
    workLoading.value = false;
  }
}

/** 选中行、结算方式、优惠任一变化都要重算：试算的意义就是「现在出账会写成多少」 */
function invalidatePreview() {
  preview.value = null;
}

function onFeeSelectionChange(rows) {
  selectedFees.value = rows;
  invalidatePreview();
}

function buildSettleDTO() {
  return {
    encounterType: current.value.encounterType,
    encounterId: current.value.encounterId,
    feeIds: selectedFeeIds.value,
    billType: settleForm.value.billType || undefined,
    settlementMode: settleForm.value.settlementMode,
    discountAmount: Number(settleForm.value.discountAmount || 0),
  };
}

async function runPreview() {
  if (!selectedFees.value.length) {
    ElMessage.warning('请先勾选要结算的记账行');
    return;
  }
  previewLoading.value = true;
  try {
    const res = await settlePreview(buildSettleDTO());
    preview.value = res.data || null;
  } catch (error) {
    preview.value = null;
    ElMessage.error(error.message || '出账试算失败');
  } finally {
    previewLoading.value = false;
  }
}

async function runSettle() {
  if (!preview.value) {
    ElMessage.warning('请先试算，确认金额后再出账');
    return;
  }
  settling.value = true;
  try {
    const res = await settleBill(buildSettleDTO());
    const bill = res.data;
    ElMessage.success(`已出账单 ${bill?.billNo}，应缴 ¥${money(bill?.payableAmount)}`);
    selectedFees.value = [];
    preview.value = null;
    await loadWorkbench();
    await loadBoard();
    if (Number(bill?.payableAmount || 0) > 0) {
      await openPay(bill);
    }
  } catch (error) {
    ElMessage.error(error.message || '出账失败');
  } finally {
    settling.value = false;
  }
}

// ==================== 收款（L3：一笔钱一行流水） ====================
const payDialog = ref(false);
const payLoading = ref(false);
const paying = ref(false);
const payBillRow = ref(null);
const payTxns = ref([]);
const payItems = ref([]);
const payRemaining = computed(() => Number(payBillRow.value?.unpaidAmount || 0));
const payTotal = computed(() => sumMoney(payItems.value));

function emptyPayItem() {
  return {payMethod: 1, amount: undefined, remark: ''};
}

/** 一账单多笔、多渠道是这一层的常态：默认给一笔「现金 = 尚需缴」，收银员再按需拆分 */
function seedPayItems() {
  const remaining = payRemaining.value;
  payItems.value = [remaining > 0 ? {...emptyPayItem(), amount: remaining} : emptyPayItem()];
}

function addPayItem() {
  payItems.value.push(emptyPayItem());
}

function removePayItem(index) {
  if (payItems.value.length <= 1) {
    ElMessage.warning('至少保留一笔收款');
    return;
  }
  payItems.value.splice(index, 1);
}

async function openPay(bill) {
  payBillRow.value = bill;
  payItems.value = [];
  payTxns.value = [];
  payDialog.value = true;
  await loadPayBill(bill.id, true);
}

async function loadPayBill(billId, resetItems = false) {
  payLoading.value = true;
  try {
    const res = await getBillDetailById(billId);
    payBillRow.value = res.data || payBillRow.value;
    payTxns.value = res.data?.txns || [];
    if (resetItems || !payItems.value.length) {
      seedPayItems();
    }
  } catch (error) {
    ElMessage.error(error.message || '加载账单失败');
  } finally {
    payLoading.value = false;
  }
}

async function submitPay() {
  const items = payItems.value.filter((i) => Number(i.amount) > 0);
  if (!items.length) {
    ElMessage.warning('请至少填写一笔大于 0 的收款金额');
    return;
  }
  if (payTotal.value > payRemaining.value) {
    ElMessage.warning(`收款合计 ¥${money(payTotal.value)} 超过尚需缴纳 ¥${money(payRemaining.value)}`);
    return;
  }
  paying.value = true;
  try {
    // 支付方式 5-院内余额扣的是「本就诊对应账户」：门诊=患者账户、住院=住院账户，
    // 由本页按就诊定位，不让收银员手填主体ID（填错就是扣了别人的钱）
    const payload = {
      billId: payBillRow.value.id,
      items: items.map((i) => ({
        payMethod: i.payMethod,
        amount: Number(i.amount),
        ownerId: Number(i.payMethod) === 5
            ? (Number(current.value?.encounterType || payBillRow.value.encounterType) === 2
                ? Number(current.value?.encounterId || payBillRow.value.encounterId)
                : Number(payBillRow.value.patientId))
            : undefined,
        remark: i.remark || undefined,
      })),
    };
    const res = await payBill(payload);
    ElMessage.success(`已收 ${res.data?.length || 0} 笔流水`);
    await loadPayBill(payBillRow.value.id, true);
    await loadWorkbench();
    await loadBoard();
    if (Number(payBillRow.value.billStatus) === 3) {
      ElMessage.success('账单已结清，可出票');
    }
  } catch (error) {
    ElMessage.error(error.message || '收款失败');
  } finally {
    paying.value = false;
  }
}

const isPaid = computed(() => Number(payBillRow.value?.billStatus) === 3);

async function runIssue() {
  try {
    const res = await issueInvoice({billId: payBillRow.value.id, invoiceType: 1});
    ElMessage.success(`已出票 ${res.data?.invoiceNo || ''}`);
  } catch (error) {
    ElMessage.error(error.message || '出票失败');
  }
}

// ==================== 退费（红冲记账行 + 原路退回） ====================
const refundDialog = ref(false);
const refundLoading = ref(false);
const refunding = ref(false);
const refundBillRow = ref(null);
const refundItems = ref([]);
const refundSelected = ref([]);
const refundReason = ref('');
const refundTotal = computed(() => sumMoney(refundSelected.value));

async function openRefund(bill) {
  refundBillRow.value = bill;
  refundReason.value = '';
  refundSelected.value = [];
  refundDialog.value = true;
  refundLoading.value = true;
  try {
    const res = await getBillDetailById(bill.id);
    refundItems.value = res.data?.items || [];
    // 默认整单退（不点名记账行 = 后端按全部行处理），但这里显式全选，页面上能看到退的是哪些行
    refundSelected.value = [...refundItems.value];
  } catch (error) {
    ElMessage.error(error.message || '加载账单明细失败');
  } finally {
    refundLoading.value = false;
  }
}

async function submitRefund() {
  if (!refundReason.value.trim()) {
    ElMessage.warning('请填写退费原因');
    return;
  }
  if (!refundSelected.value.length) {
    ElMessage.warning('请勾选要退费的记账行');
    return;
  }
  const whole = refundSelected.value.length >= refundItems.value.length;
  try {
    await ElMessageBox.confirm(`确认退费 ¥${money(refundTotal.value)}（${whole ? '整单退，医保侧会发 2305 冲正' : '部分退，医保清单将退回待重新结算'}），款项按原收款渠道逐笔退回？`, '退费确认', {type: 'warning'});
  } catch {
    return;
  }
  refunding.value = true;
  try {
    const res = await refundBill({
      billId: refundBillRow.value.id,
      feeIds: whole ? undefined : refundSelected.value.map((i) => i.feeRecordId),
      reason: refundReason.value.trim(),
    });
    ElMessage.success(`已退 ${res.data?.length || 0} 笔，原路退回`);
    refundDialog.value = false;
    await loadWorkbench();
    await loadBoard();
  } catch (error) {
    ElMessage.error(error.message || '退费失败');
  } finally {
    refunding.value = false;
  }
}

// ==================== 取消结算 / 账单详情 ====================
async function runVoid(bill) {
  let reason = '';
  try {
    const r = await ElMessageBox.prompt('作废会把记账行解锁回「待结算」，不动资金（已有收款的账单会被拒绝）', `作废账单 ${bill.billNo}`, {
      inputPlaceholder: '请填写作废原因',
      inputValidator: (v) => !!v?.trim() || '必须填写原因'
    });
    reason = String(r.value || '').trim();
  } catch {
    return;
  }
  try {
    await voidBill({billId: bill.id, reason});
    ElMessage.success('账单已作废，记账行已解锁');
    await loadWorkbench();
    await loadBoard();
  } catch (error) {
    ElMessage.error(error.message || '作废失败');
  }
}

const detailDialog = ref(false);
const detailLoading = ref(false);
const detail = ref(null);

async function openDetail(bill) {
  detailDialog.value = true;
  detailLoading.value = true;
  try {
    const res = await getBillDetailById(bill.id);
    detail.value = res.data || null;
  } catch (error) {
    ElMessage.error(error.message || '加载账单详情失败');
  } finally {
    detailLoading.value = false;
  }
}

const billStatusTag = (status) => Number(status) === 3 ? 'success' : Number(status) === 2 ? 'warning' : Number(status) === 4 || Number(status) === 5 ? 'info' : '';
</script>
