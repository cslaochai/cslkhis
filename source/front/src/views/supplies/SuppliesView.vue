<template>
  <div class="space-y-6">
    <!-- 页头操作 -->
    <div class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-base font-semibold text-slate-700">物资耗材管理</h2>
          <p class="mt-0.5 text-xs text-slate-400">耗材域独立于药品域：批次库存 + 出入库流水 +
            科室领用（领用即扣库存，先过期先出）；耗材基础档案在「耗材字典」（菜单 2921，sql/177）维护</p>
        </div>
        <div class="flex gap-2">
          <el-button v-perm="'asset:supplies:add'" :icon="Plus" type="primary" @click="openAddDialog">建批入库
          </el-button>
          <el-button v-perm="'asset:supplies:add'" :icon="TakeawayBox" type="success" @click="openConsumeDialog">
            科室领用
          </el-button>
        </div>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="rounded-lg border border-slate-200 bg-white p-4 shadow-sm"
             @tab-change="handleTabChange">
      <!-- 库存 -->
      <el-tab-pane label="批次库存" name="stock">
        <div class="mb-4 flex items-center gap-3">
          <el-input v-model="stockKeyword" :prefix-icon="Search" class="!w-60" clearable placeholder="耗材名称/编码"
                    @keyup.enter="loadStock"/>
          <el-select v-model="stockStatusFilter" class="!w-32" clearable placeholder="库存状态" @change="loadStock">
            <el-option :value="1" label="正常"/>
            <el-option :value="2" label="预警"/>
            <el-option :value="3" label="缺货"/>
            <el-option :value="4" label="过期"/>
          </el-select>
          <el-button :icon="Search" type="primary" @click="loadStock">查询</el-button>
          <el-button :icon="Refresh" @click="stockKeyword = ''; stockStatusFilter = null; loadStock()">重置</el-button>
        </div>
        <el-table v-loading="loading" :data="stockList" style="width: 100%">
          <el-table-column class-name="font-mono text-xs" label="编码" prop="consumableCode" width="110"/>
          <el-table-column label="耗材名称" min-width="150" prop="consumableName" show-overflow-tooltip/>
          <el-table-column align="center" label="类别" width="100">
            <template #default="{ row }">{{ categoryLabel(row.category) }}</template>
          </el-table-column>
          <el-table-column label="规格" min-width="130" prop="specification" show-overflow-tooltip/>
          <el-table-column class-name="font-mono text-xs" label="批号" prop="batchNo" width="110"/>
          <el-table-column label="有效期至" prop="expiryDate" width="105"/>
          <el-table-column align="right" label="库存" width="90">
            <template #default="{ row }">
              <span :class="row.quantity <= 50 ? 'text-amber-600' : 'text-slate-700'"
                    class="font-medium">{{ row.quantity }}</span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="单位" prop="unit" width="60"/>
          <el-table-column align="center" label="状态" width="80">
            <template #default="{ row }">
              <span
                  :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', stockStatusInfo(row.stockStatus).color]">
                {{ stockStatusInfo(row.stockStatus).label }}
              </span>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="150">
            <template #default="{ row }">
              <el-button v-perm="'asset:supplies:edit'" link type="primary" @click="handleInbound(row)">补货</el-button>
              <el-button v-perm="'asset:supplies:edit'" link type="warning" @click="handleOutbound(row)">出库
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="stockList.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">暂无库存记录
        </div>
        <div class="mt-4 flex justify-end">
          <el-pagination v-model:current-page="stockPagination.pageNum" v-model:page-size="stockPagination.pageSize"
                         :page-sizes="PAGE_SIZES" :total="stockPagination.total"
                         layout="total, sizes, prev, pager, next"
                         @size-change="loadStock" @current-change="loadStock"/>
        </div>
      </el-tab-pane>

      <!-- 领用台账 -->
      <el-tab-pane label="科室领用台账" name="consume">
        <div class="mb-4 flex items-center gap-3">
          <el-input v-model="consumeKeyword" :prefix-icon="Search" class="!w-60" clearable
                    placeholder="耗材名称/领用单号" @keyup.enter="loadConsume"/>
          <el-button :icon="Search" type="primary" @click="loadConsume">查询</el-button>
          <el-button :icon="Refresh" @click="consumeKeyword = ''; loadConsume()">重置</el-button>
        </div>
        <el-table v-loading="loading" :data="consumeList" style="width: 100%">
          <el-table-column class-name="font-mono text-xs" label="领用单号" prop="consumeNo" width="180"/>
          <el-table-column label="耗材名称" min-width="140" prop="consumableName" show-overflow-tooltip/>
          <el-table-column label="规格" min-width="120" prop="specification" show-overflow-tooltip/>
          <el-table-column align="right" label="数量" width="90">
            <template #default="{ row }">{{ row.quantity }}{{ row.unit || '' }}</template>
          </el-table-column>
          <el-table-column label="领用科室" prop="deptName" width="120"/>
          <el-table-column label="用途" min-width="130" prop="purpose" show-overflow-tooltip/>
          <el-table-column label="经办人" prop="operatorName" width="90"/>
          <el-table-column label="领用时间" prop="consumeTime" width="165"/>
          <el-table-column align="center" label="库存变化" width="120">
            <template #default="{ row }">
              <span class="font-mono text-xs text-slate-500">{{ row.stockBefore }} → {{ row.stockAfter }}</span>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="consumeList.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">暂无领用记录
        </div>
        <div class="mt-4 flex justify-end">
          <el-pagination v-model:current-page="consumePagination.pageNum" v-model:page-size="consumePagination.pageSize"
                         :page-sizes="PAGE_SIZES" :total="consumePagination.total"
                         layout="total, sizes, prev, pager, next"
                         @size-change="loadConsume" @current-change="loadConsume"/>
        </div>
      </el-tab-pane>

      <!-- 流水 -->
      <el-tab-pane label="出入库流水" name="log">
        <div class="mb-4 flex items-center gap-3">
          <el-input v-model="logKeyword" :prefix-icon="Search" class="!w-60" clearable placeholder="耗材名称"
                    @keyup.enter="loadLog"/>
          <el-select v-model="logTypeFilter" class="!w-36" clearable placeholder="变动类型" @change="loadLog">
            <el-option v-for="(label, key) in changeTypeMap" :key="key" :label="label" :value="Number(key)"/>
          </el-select>
          <el-button :icon="Search" type="primary" @click="loadLog">查询</el-button>
          <el-button :icon="Refresh" @click="logKeyword = ''; logTypeFilter = null; loadLog()">重置</el-button>
        </div>
        <el-table v-loading="loading" :data="logList" style="width: 100%">
          <el-table-column label="耗材名称" min-width="140" prop="consumableName" show-overflow-tooltip/>
          <el-table-column class-name="font-mono text-xs" label="批号" prop="batchNo" width="110"/>
          <el-table-column align="center" label="变动类型" width="100">
            <template #default="{ row }">{{ changeTypeMap[row.changeType] || `未知(${row.changeType})` }}</template>
          </el-table-column>
          <el-table-column align="right" label="变动数量" width="95">
            <template #default="{ row }">
              <span :class="row.changeQuantity >= 0 ? 'text-emerald-600' : 'text-red-600'"
                    class="font-mono font-medium">
                {{ row.changeQuantity >= 0 ? '+' : '' }}{{ row.changeQuantity }}
              </span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="批次数量变化" width="130">
            <template #default="{ row }">
              <span class="font-mono text-xs text-slate-500">{{ row.quantityBefore }} → {{ row.quantityAfter }}</span>
            </template>
          </el-table-column>
          <el-table-column class-name="font-mono text-xs" label="来源单据" prop="sourceNo" width="170"/>
          <el-table-column label="操作人" prop="operatorName" width="90"/>
          <el-table-column label="时间" prop="createTime" width="165"/>
        </el-table>
        <div v-if="logList.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">暂无流水记录</div>
        <div class="mt-4 flex justify-end">
          <el-pagination v-model:current-page="logPagination.pageNum" v-model:page-size="logPagination.pageSize"
                         :page-sizes="PAGE_SIZES" :total="logPagination.total" layout="total, sizes, prev, pager, next"
                         @size-change="loadLog" @current-change="loadLog"/>
        </div>
      </el-tab-pane>

      <!-- 高值扫码登记 -->
      <el-tab-pane label="高值扫码登记" name="trace">
        <div class="grid gap-6 lg:grid-cols-2">
          <div class="space-y-4">
            <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
              <p class="mb-2 text-sm font-semibold text-slate-600">① 扫码解析 UDI</p>
              <div class="flex gap-2">
                <el-input v-model="traceUdiInput"
                          clearable placeholder="扫描/粘贴 GS1 UDI，如 (01)06941234567890(21)SN001(17)271231(10)LOT1"
                          @keyup.enter="handleUdiScan">
                  <template #prefix>
                    <el-icon>
                      <TakeawayBox/>
                    </el-icon>
                  </template>
                </el-input>
                <el-button :loading="traceScanning" type="primary" @click="handleUdiScan">扫码解析</el-button>
              </div>
              <template v-if="traceScan">
                <el-alert v-if="!traceScan.parsed" :closable="false" :title="traceScan.tip || '无法解析 UDI-DI'" class="mt-3"
                          type="warning"/>
                <el-alert v-else-if="!traceScan.matched" :closable="false" :title="traceScan.tip || 'UDI-DI 未命中字典'" class="mt-3"
                          type="warning"/>
                <div v-else class="mt-3 space-y-1 text-sm">
                  <div class="flex justify-between"><span class="text-slate-400">UDI-DI</span><span
                      class="font-mono text-xs">{{ traceScan.udiDi }}</span></div>
                  <div class="flex justify-between"><span class="text-slate-400">序列号 / 批号 / 效期</span><span
                      class="font-mono text-xs">{{ traceScan.udiSerial || '-' }} / {{
                      traceScan.udiBatch || '-'
                    }} / {{ traceScan.udiExpiryDate || '-' }}</span></div>
                  <div class="flex justify-between"><span class="text-slate-400">命中耗材</span><span
                      class="font-medium">{{ traceScan.consumableName }}（{{ traceScan.specification || '-' }}）</span>
                  </div>
                  <div class="flex justify-between"><span class="text-slate-400">注册证号</span><span
                      class="font-mono text-xs">{{ traceScan.regCertNo || '-' }}</span></div>
                  <div class="flex justify-between"><span class="text-slate-400">零售单价</span><span
                      class="font-semibold text-red-600">¥{{ traceScan.retailPrice }}</span></div>
                </div>
              </template>
            </div>
            <div class="rounded-lg border border-slate-200 bg-slate-50 p-4">
              <p class="mb-2 text-sm font-semibold text-slate-600">登记结果</p>
              <template v-if="traceResult">
                <div class="space-y-1 text-sm">
                  <div class="flex justify-between"><span class="text-slate-400">溯源单号</span><span
                      class="font-mono text-xs">{{ traceResult.traceNo }}</span></div>
                  <div class="flex justify-between"><span class="text-slate-400">计费状态</span>
                    <span
                        :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', (chargeStatusMap[traceResult.chargeStatus] || chargeStatusMap[0]).color]">{{
                        (chargeStatusMap[traceResult.chargeStatus] || {label: '未知'}).label
                      }}</span>
                  </div>
                  <div v-if="traceResult.chargeNo" class="flex justify-between"><span
                      class="text-slate-400">收费单号</span><span class="font-mono text-xs">{{
                      traceResult.chargeNo
                    }}</span></div>
                  <div v-if="traceResult.chargeFailReason" class="mt-1 rounded bg-amber-50 p-2 text-xs text-amber-700">
                    {{ traceResult.chargeFailReason }}
                  </div>
                </div>
                <el-button class="mt-3" size="small" @click="resetTraceForm">再登记一件</el-button>
              </template>
              <p v-else class="text-xs text-slate-400">完成扫码与患者选择后点「确认登记」，一件耗材一条台账。</p>
            </div>
          </div>
          <div class="rounded-lg border border-slate-200 p-4">
            <p class="mb-2 text-sm font-semibold text-slate-600">② 使用信息</p>
            <el-form label-width="90px">
              <el-form-item label="出库批次" required>
                <el-select v-model="traceForm.stockId" :disabled="!traceScan?.batches?.length" :fit-input-width="false"
                           placeholder="选择扫码结果中的有货批次" style="width: 100%">
                  <el-option v-for="b in traceScan?.batches || []" :key="b.stockId" :label="`批 ${b.batchNo}｜效期 ${b.expiryDate || '-'}｜剩 ${b.quantity}${traceScan?.unit || ''}｜${b.location || '-'}`"
                             :value="String(b.stockId)"/>
                </el-select>
              </el-form-item>
              <el-form-item label="患者" required>
                <PatientSelect v-model="traceForm.patientId" @select="onTracePatientSelect"/>
              </el-form-item>
              <el-form-item label="关联就诊">
                <el-radio-group v-model="traceForm.visitMode" @change="loadVisitAnchors">
                  <el-radio-button :value="0">不关联</el-radio-button>
                  <el-radio-button :value="1">门诊</el-radio-button>
                  <el-radio-button :value="2">住院</el-radio-button>
                </el-radio-group>
                <p class="mt-1 w-full text-xs text-slate-400">不关联则暂不计费，可后补记或到收费窗口手工计费。</p>
              </el-form-item>
              <el-form-item v-if="traceForm.visitMode === 1" label="挂号记录" required>
                <el-select v-model="traceForm.registId" :fit-input-width="false" filterable
                           placeholder="选择该患者的挂号（计费锚点）" style="width: 100%" @change="onRegistChange">
                  <el-option v-for="r in registOptions" :key="String(r.id)" :label="`${r.registNo || r.id}｜${r.visitDate || ''}｜${r.deptName || ''}`"
                             :value="String(r.id)"/>
                </el-select>
              </el-form-item>
              <el-form-item v-if="traceForm.visitMode === 2" label="在院记录" required>
                <el-select v-model="traceForm.admissionId" :fit-input-width="false" filterable
                           placeholder="选择该患者的住院记录（计费锚点）" style="width: 100%">
                  <el-option v-for="a in admissionOptions" :key="String(a.admissionId ?? a.id)"
                             :label="`${a.admissionNo || ''}｜${a.deptName || ''}｜床位 ${a.bedNo || '-'}`"
                             :value="String(a.admissionId ?? a.id)"/>
                </el-select>
              </el-form-item>
              <el-form-item label="使用科室">
                <el-select v-model="traceForm.deptId" :fit-input-width="false" clearable filterable
                           placeholder="手术/使用科室（计费明细科室）" style="width: 100%">
                  <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
                </el-select>
              </el-form-item>
              <el-form-item label="用途">
                <el-input v-model="traceForm.purpose" :rows="2" placeholder="如 PCI 术中植入前降支" type="textarea"/>
              </el-form-item>
              <el-button v-perm="'asset:supplies:add'" class="w-full" type="primary" @click="handleTraceUseSubmit">
                确认登记（扣库存1件+计费）
              </el-button>
            </el-form>
          </div>
        </div>
      </el-tab-pane>

      <!-- 溯源台账 -->
      <el-tab-pane label="溯源台账" name="traceLedger">
        <div class="mb-4 flex items-center gap-3">
          <el-input v-model="traceKeyword" :prefix-icon="Search" class="!w-64"
                    clearable placeholder="溯源单号/UDI/序列号/患者/耗材名" @keyup.enter="loadTrace"/>
          <el-select v-model="traceChargeStatusFilter" class="!w-32" clearable placeholder="计费状态"
                     @change="loadTrace">
            <el-option :value="0" label="未计费"/>
            <el-option :value="1" label="已计费"/>
            <el-option :value="2" label="计费失败"/>
          </el-select>
          <el-select v-model="traceStatusFilter" class="!w-32" clearable placeholder="记录状态" @change="loadTrace">
            <el-option :value="1" label="使用中"/>
            <el-option :value="2" label="已作废"/>
          </el-select>
          <el-button :icon="Search" type="primary" @click="loadTrace">查询</el-button>
          <el-button :icon="Refresh"
                     @click="traceKeyword = ''; traceChargeStatusFilter = null; traceStatusFilter = null; loadTrace()">
            重置
          </el-button>
        </div>
        <el-table v-loading="loading" :data="traceList" style="width: 100%">
          <el-table-column class-name="font-mono text-xs" label="溯源单号" prop="traceNo" width="165"/>
          <el-table-column label="耗材" min-width="140" prop="consumableName" show-overflow-tooltip/>
          <el-table-column label="UDI（序列号/批号）" min-width="150" show-overflow-tooltip>
            <template #default="{ row }"><span class="font-mono text-xs">{{
                row.udiSerial || '-'
              }} / {{ row.udiBatch || '-' }}</span></template>
          </el-table-column>
          <el-table-column label="患者" prop="patientName" width="90"/>
          <el-table-column align="center" label="就诊" width="70">
            <template #default="{ row }">{{
                row.visitType === 1 ? '门诊' : row.visitType === 2 ? '住院' : '-'
              }}
            </template>
          </el-table-column>
          <el-table-column label="使用科室" prop="deptName" show-overflow-tooltip width="110"/>
          <el-table-column label="使用时间" prop="usageTime" width="160"/>
          <el-table-column label="计费" width="150">
            <template #default="{ row }">
              <span
                  :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', (chargeStatusMap[row.chargeStatus] || chargeStatusMap[0]).color]">
                {{ (chargeStatusMap[row.chargeStatus] || {label: `未知(${row.chargeStatus})`}).label }}
              </span>
              <span v-if="row.chargeNo" class="ml-1 font-mono text-xs text-slate-500">{{ row.chargeNo }}</span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="状态" width="80">
            <template #default="{ row }">
              <span
                  :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', row.status === 1 ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500']">
                {{ row.status === 1 ? '使用中' : row.status === 2 ? '已作废' : `未知(${row.status})` }}
              </span>
            </template>
          </el-table-column>
          <el-table-column fixed="right" label="操作" width="150">
            <template #default="{ row }">
              <el-button link type="primary" @click="openTraceDetail(row)">详情</el-button>
              <el-button v-if="row.status === 1 && row.chargeStatus !== 1" v-perm="'asset:supplies:edit'" link
                         type="warning" @click="handleTraceVoid(row)">作废
              </el-button>
              <el-button v-if="row.status === 1 && row.chargeStatus !== 1" v-perm="'asset:supplies:edit'" link
                         type="success" @click="handleTraceRecharge(row)">补记
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <div v-if="traceList.length === 0 && !loading" class="py-12 text-center text-sm text-slate-400">
          暂无高值耗材使用记录
        </div>
        <div class="mt-4 flex justify-end">
          <el-pagination v-model:current-page="tracePagination.pageNum" v-model:page-size="tracePagination.pageSize"
                         :page-sizes="PAGE_SIZES" :total="tracePagination.total"
                         layout="total, sizes, prev, pager, next"
                         @size-change="loadTrace" @current-change="loadTrace"/>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 建批入库弹窗 -->
    <el-dialog v-model="addDialogVisible" destroy-on-close title="建批入库" width="560px">
      <el-form label-width="90px">
        <el-form-item label="选择耗材" required>
          <el-select v-model="addForm.consumableId" :fit-input-width="false" filterable placeholder="从耗材字典选择"
                     style="width: 100%">
            <el-option v-for="c in consumableOptions" :key="c.id" :label="`${c.consumableName}（${c.specification || '-'}）`"
                       :value="c.id"/>
          </el-select>
          <p v-if="selectedConsumable()" class="mt-1 text-xs text-slate-400">
            单位：{{ selectedConsumable()?.unit || '-' }}　字典零售价：¥{{ selectedConsumable()?.retailPrice ?? 0 }}
          </p>
        </el-form-item>
        <el-form-item label="批号" required>
          <el-input v-model="addForm.batchNo" placeholder="如 GZ20260401"/>
        </el-form-item>
        <el-form-item label="生产日期">
          <el-date-picker v-model="addForm.productionDate" placeholder="选择生产日期" style="width: 100%"
                          type="date" value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="有效期至" required>
          <el-date-picker v-model="addForm.expiryDate" placeholder="选择有效期" style="width: 100%" type="date"
                          value-format="YYYY-MM-DD"/>
        </el-form-item>
        <el-form-item label="入库数量" required>
          <el-input-number v-model="addForm.quantity" :min="0.01" :precision="2" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="成本价" required>
          <el-input-number v-model="addForm.costPrice" :min="0" :precision="2" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="存放位置">
          <el-input v-model="addForm.location" placeholder="如 耗材库房A-01"/>
        </el-form-item>
        <el-form-item label="供应商">
          <el-input v-model="addForm.supplier"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button v-perm="'asset:supplies:add'" type="primary" @click="handleAddSubmit">确认入库</el-button>
      </template>
    </el-dialog>

    <!-- 科室领用弹窗 -->
    <el-dialog v-model="consumeDialogVisible" destroy-on-close title="科室领用" width="520px">
      <el-form label-width="90px">
        <el-form-item label="选择耗材" required>
          <el-select v-model="consumeForm.consumableId" :fit-input-width="false" filterable placeholder="从耗材字典选择"
                     style="width: 100%">
            <el-option v-for="c in consumableOptions" :key="c.id" :label="`${c.consumableName}（${c.specification || '-'}）`"
                       :value="c.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="领用数量" required>
          <el-input-number v-model="consumeForm.quantity" :min="0.01" :precision="2" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="领用科室" required>
          <el-select v-model="consumeForm.deptId" :fit-input-width="false" filterable placeholder="选择科室"
                     style="width: 100%">
            <el-option v-for="d in deptOptions" :key="d.id" :label="d.deptName" :value="d.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="用途">
          <el-input v-model="consumeForm.purpose" :rows="2" placeholder="如 病房日常换药使用" type="textarea"/>
        </el-form-item>
        <p class="pl-2 text-xs text-slate-400">领用将按先过期先出（FEFO）扣减批次库存，并落出入库流水。</p>
      </el-form>
      <template #footer>
        <el-button @click="consumeDialogVisible = false">取消</el-button>
        <el-button v-perm="'asset:supplies:add'" type="primary" @click="handleConsumeSubmit">确认领用</el-button>
      </template>
    </el-dialog>

    <!-- 溯源详情弹窗（字典→入库批次→使用患者→计费 全链） -->
    <el-dialog v-model="traceDetailVisible" destroy-on-close title="溯源全链" width="640px">
      <div v-if="traceDetail" class="space-y-4 text-sm">
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="mb-2 text-xs font-semibold text-slate-400">一、码与耗材字典</p>
          <div class="grid grid-cols-2 gap-x-6 gap-y-1">
            <div class="flex justify-between"><span class="text-slate-400">溯源单号</span><span
                class="font-mono text-xs">{{ traceDetail.traceNo }}</span></div>
            <div class="flex justify-between"><span
                class="text-slate-400">状态</span><span>{{ traceDetail.status === 1 ? '使用中' : '已作废' }}</span>
            </div>
            <div class="col-span-2 flex justify-between"><span class="text-slate-400">UDI 原文</span><span
                class="max-w-[60%] break-all font-mono text-xs">{{ traceDetail.udiCode }}</span></div>
            <div class="flex justify-between"><span class="text-slate-400">DI / 序列号</span><span
                class="font-mono text-xs">{{ traceDetail.udiDi }} / {{ traceDetail.udiSerial || '-' }}</span></div>
            <div class="flex justify-between"><span class="text-slate-400">批号 / 效期</span><span
                class="font-mono text-xs">{{ traceDetail.udiBatch || '-' }} / {{
                traceDetail.udiExpiryDate || '-'
              }}</span></div>
            <div class="flex justify-between"><span class="text-slate-400">耗材</span><span
                class="font-medium">{{ traceDetail.consumableName }}</span></div>
            <div class="flex justify-between"><span
                class="text-slate-400">厂家</span><span>{{ traceDetail.manufacturer || '-' }}</span></div>
            <div class="col-span-2 flex justify-between"><span class="text-slate-400">注册证号</span><span
                class="font-mono text-xs">{{ traceDetail.regCertNo || '-' }}</span></div>
          </div>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="mb-2 text-xs font-semibold text-slate-400">二、入库批次</p>
          <div class="grid grid-cols-2 gap-x-6 gap-y-1">
            <div class="flex justify-between"><span class="text-slate-400">批号</span><span
                class="font-mono text-xs">{{ traceDetail.batchNo }}</span></div>
            <div class="flex justify-between"><span
                class="text-slate-400">供应商</span><span>{{ traceDetail.supplier || '-' }}</span></div>
            <div class="flex justify-between"><span
                class="text-slate-400">存放位置</span><span>{{ traceDetail.location || '-' }}</span></div>
            <div class="flex justify-between"><span class="text-slate-400">入库数量/成本</span><span>{{
                traceDetail.batchQuantity ?? '-'
              }} / ¥{{ traceDetail.costPrice ?? '-' }}</span></div>
          </div>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="mb-2 text-xs font-semibold text-slate-400">三、使用患者</p>
          <div class="grid grid-cols-2 gap-x-6 gap-y-1">
            <div class="flex justify-between"><span class="text-slate-400">患者</span><span
                class="font-medium">{{ traceDetail.patientName }}（{{ traceDetail.patientNo }}）</span></div>
            <div class="flex justify-between"><span class="text-slate-400">就诊</span><span>{{
                traceDetail.visitType === 1 ? '门诊' : traceDetail.visitType === 2 ? '住院' : '未关联'
              }}</span></div>
            <div class="flex justify-between"><span
                class="text-slate-400">使用科室</span><span>{{ traceDetail.deptName || '-' }}</span></div>
            <div class="flex justify-between"><span class="text-slate-400">使用时间/登记人</span><span>{{
                traceDetail.usageTime
              }} / {{ traceDetail.operatorName }}</span></div>
          </div>
          <p v-if="traceDetail.remark" class="mt-1 text-xs text-slate-500">用途：{{ traceDetail.remark }}</p>
          <p v-if="traceDetail.status === 2" class="mt-1 text-xs text-red-600">已于 {{ traceDetail.voidTime }}
            作废：{{ traceDetail.voidReason }}</p>
        </div>
        <div class="rounded-lg border border-slate-200 p-3">
          <p class="mb-2 text-xs font-semibold text-slate-400">四、计费</p>
          <div class="grid grid-cols-2 gap-x-6 gap-y-1">
            <div class="flex justify-between"><span class="text-slate-400">计费状态</span>
              <span
                  :class="['inline-block rounded px-2 py-0.5 text-xs font-medium', (chargeStatusMap[traceDetail.chargeStatus] || chargeStatusMap[0]).color]">{{
                  (chargeStatusMap[traceDetail.chargeStatus] || {label: '未知'}).label
                }}</span>
            </div>
            <div class="flex justify-between"><span class="text-slate-400">金额</span><span
                class="font-semibold text-red-600">¥{{ traceDetail.chargeAmount ?? traceDetail.retailPrice }}</span>
            </div>
            <div class="flex justify-between"><span class="text-slate-400">收费单号</span><span
                class="font-mono text-xs">{{ traceDetail.chargeNo || '-' }}</span></div>
            <div class="flex justify-between"><span class="text-slate-400">收费明细ID</span><span
                class="font-mono text-xs">{{ traceDetail.chargeDetailId || '-' }}</span></div>
          </div>
          <p v-if="traceDetail.chargeFailReason" class="mt-1 text-xs text-amber-600">{{
              traceDetail.chargeFailReason
            }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="traceDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {onMounted, ref} from 'vue';
import {Plus, Refresh, Search, TakeawayBox} from '@element-plus/icons-vue';
import {ElMessage, ElMessageBox} from 'element-plus';
import {
  consume,
  createStock,
  getConsumableSelectList,
  getConsumeList,
  getStockList,
  getStockLogList,
  getTraceDetailById,
  getTraceList,
  inboundStock,
  outboundStock,
  traceRecharge,
  traceUse,
  traceVoid,
  udiScan,
} from '@/api/supplies';
import {getRegistrationList} from '@/api/appoint';
import {getInpatientListPage} from '@/api/inpatient';
import PatientSelect from '@/components/his/PatientSelect.vue';
import {getDepartmentSelectList} from '@/api/system';
import {DEFAULT_PAGE_SIZE, PAGE_SIZES} from '@/lib/pagination';

const activeTab = ref('stock');
const loading = ref(false);
// 类别口径：1-卫生材料 2-注射穿刺 3-医用敷料 4-防护用品 5-其他 6-植入介入（未知(n) 不回落）
const categoryMap = {
  1: '卫生材料', 2: '注射穿刺', 3: '医用敷料', 4: '防护用品', 5: '其他', 6: '植入介入',
};
const stockStatusMap = {
  1: {label: '正常', color: 'bg-emerald-100 text-emerald-700'},
  2: {label: '预警', color: 'bg-amber-100 text-amber-700'},
  3: {label: '缺货', color: 'bg-red-100 text-red-600'},
  4: {label: '过期', color: 'bg-slate-100 text-slate-500'},
};
const changeTypeMap = {
  1: '入库', 2: '领用出库', 3: '退回入库', 4: '其他出库', 5: '盘盈', 6: '盘亏', 7: '使用出库',
};
const categoryLabel = (c) => (c == null ? '未知' : categoryMap[c] || `未知(${c})`);
const stockStatusInfo = (s) => (s != null && stockStatusMap[s]) || {
  label: `未知(${s})`,
  color: 'bg-slate-100 text-slate-500'
};
// ========== 库存 ==========
const stockList = ref([]);
const stockKeyword = ref('');
const stockStatusFilter = ref(null);
const stockPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const loadStock = async () => {
  loading.value = true;
  try {
    const params = {pageNum: stockPagination.value.pageNum, pageSize: stockPagination.value.pageSize};
    if (stockKeyword.value.trim())
      params.keyword = stockKeyword.value.trim();
    if (stockStatusFilter.value != null)
      params.stockStatus = stockStatusFilter.value;
    const res = await getStockList(params);
    stockList.value = res.data?.records || [];
    stockPagination.value.total = res.data?.total || 0;
  } catch (e) {
    console.error('加载耗材库存失败:', e);
    ElMessage.error(e?.message || '加载耗材库存失败');
  } finally {
    loading.value = false;
  }
};
// 建批入库弹窗
const addDialogVisible = ref(false);
const consumableOptions = ref([]);
const addForm = ref({
  consumableId: null,
  batchNo: '',
  productionDate: '',
  expiryDate: '',
  quantity: null,
  costPrice: null,
  location: '',
  supplier: ''
});
const openAddDialog = async () => {
  addForm.value = {
    consumableId: null,
    batchNo: '',
    productionDate: '',
    expiryDate: '',
    quantity: null,
    costPrice: null,
    location: '',
    supplier: ''
  };
  try {
    const res = await getConsumableSelectList();
    consumableOptions.value = res.data || [];
  } catch (e) {
    console.error('加载耗材字典失败:', e);
  }
  addDialogVisible.value = true;
};
const selectedConsumable = () => consumableOptions.value.find((c) => c.id === addForm.value.consumableId);
const handleAddSubmit = async () => {
  const f = addForm.value;
  if (!f.consumableId)
    return ElMessage.warning('请从耗材字典选择耗材');
  if (!f.batchNo.trim())
    return ElMessage.warning('批号不能为空');
  if (!f.expiryDate)
    return ElMessage.warning('有效期不能为空');
  if (!f.quantity || f.quantity <= 0)
    return ElMessage.warning('库存数量必须大于0');
  if (f.costPrice == null || f.costPrice < 0)
    return ElMessage.warning('成本价不能为负数');
  try {
    await createStock({
      consumableId: f.consumableId, batchNo: f.batchNo.trim(),
      productionDate: f.productionDate || null, expiryDate: f.expiryDate,
      quantity: f.quantity, costPrice: f.costPrice, location: f.location, supplier: f.supplier,
    });
    ElMessage.success('建批入库成功');
    addDialogVisible.value = false;
    loadStock();
  } catch (e) {
    ElMessage.error(e?.message || '建批入库失败');
  }
};
// 补货入库
const handleInbound = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`为「${row.consumableName}」批号 ${row.batchNo} 补货入库`, '补货入库', {
      confirmButtonText: '确认入库', cancelButtonText: '取消',
      inputPattern: /^\d+(\.\d{1,2})?$/, inputErrorMessage: '请输入正数数量',
      inputPlaceholder: '入库数量',
    });
    await inboundStock(row.id, Number(value));
    ElMessage.success('补货入库成功');
    loadStock();
  } catch (e) {
    if (e !== 'cancel')
      ElMessage.error(e?.message || '入库失败');
  }
};
// 其他出库
const handleOutbound = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`从「${row.consumableName}」批号 ${row.batchNo} 出库`, '其他出库', {
      confirmButtonText: '确认出库', cancelButtonText: '取消',
      inputPattern: /^\d+(\.\d{1,2})?$/, inputErrorMessage: '请输入正数数量',
      inputPlaceholder: '出库数量（不能超过现有库存）',
    });
    await outboundStock(row.id, Number(value));
    ElMessage.success('出库成功');
    loadStock();
  } catch (e) {
    if (e !== 'cancel')
      ElMessage.error(e?.message || '出库失败');
  }
};
// ========== 科室领用 ==========
const consumeDialogVisible = ref(false);
const deptOptions = ref([]);
const consumeForm = ref({consumableId: null, quantity: null, deptId: null, purpose: ''});
const openConsumeDialog = async () => {
  consumeForm.value = {consumableId: null, quantity: null, deptId: null, purpose: ''};
  try {
    // 科室下拉改用统一入口：原先走 /supplies/deptSelectList，返回的是下划线 dept_name，
    // 与全仓 20 多处 deptName 不一致（本页靠模板里写 d.dept_name 硬顶）。
    // 统一后不传 scope → 默认按当前人过滤，字段名也是 deptName。
    const [cRes, dRes] = await Promise.all([getConsumableSelectList(), getDepartmentSelectList()]);
    consumableOptions.value = cRes.data || [];
    deptOptions.value = dRes.data || [];
  } catch (e) {
    console.error('加载下拉数据失败:', e);
  }
  consumeDialogVisible.value = true;
};
const handleConsumeSubmit = async () => {
  const f = consumeForm.value;
  if (!f.consumableId)
    return ElMessage.warning('请选择耗材');
  if (!f.quantity || f.quantity <= 0)
    return ElMessage.warning('领用数量必须大于0');
  if (!f.deptId)
    return ElMessage.warning('请选择领用科室');
  try {
    await consume({consumableId: f.consumableId, quantity: f.quantity, deptId: f.deptId, purpose: f.purpose.trim()});
    ElMessage.success('领用成功，库存已按先过期先出扣减');
    consumeDialogVisible.value = false;
    loadStock();
    if (activeTab.value === 'consume')
      loadConsume();
    if (activeTab.value === 'log')
      loadLog();
  } catch (e) {
    ElMessage.error(e?.message || '领用失败');
  }
};
const consumeList = ref([]);
const consumeKeyword = ref('');
const consumePagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const loadConsume = async () => {
  loading.value = true;
  try {
    const params = {pageNum: consumePagination.value.pageNum, pageSize: consumePagination.value.pageSize};
    if (consumeKeyword.value.trim())
      params.keyword = consumeKeyword.value.trim();
    const res = await getConsumeList(params);
    consumeList.value = res.data?.records || [];
    consumePagination.value.total = res.data?.total || 0;
  } catch (e) {
    console.error('加载领用台账失败:', e);
  } finally {
    loading.value = false;
  }
};
// ========== 流水 ==========
const logList = ref([]);
const logKeyword = ref('');
const logTypeFilter = ref(null);
const logPagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const loadLog = async () => {
  loading.value = true;
  try {
    const params = {pageNum: logPagination.value.pageNum, pageSize: logPagination.value.pageSize};
    if (logKeyword.value.trim())
      params.keyword = logKeyword.value.trim();
    if (logTypeFilter.value != null)
      params.changeType = logTypeFilter.value;
    const res = await getStockLogList(params);
    logList.value = res.data?.records || [];
    logPagination.value.total = res.data?.total || 0;
  } catch (e) {
    console.error('加载流水失败:', e);
  } finally {
    loading.value = false;
  }
};
// ========== L11 高值耗材 UDI 扫码登记 ==========
const traceScan = ref(null); // udiScan 返回
const traceUdiInput = ref('');
const traceScanning = ref(false);
// 雪花 ID 一律保持字符串，不做 Number() 转换（会静默丢精度）
const traceForm = ref({
  stockId: null, patientId: null, patientNo: '', patientName: '',
  visitMode: 0, registId: null, registNo: '', admissionId: null,
  deptId: null, purpose: ''
});
const registOptions = ref([]);
const admissionOptions = ref([]);
const traceResult = ref(null);
const handleUdiScan = async () => {
  if (!traceUdiInput.value.trim())
    return ElMessage.warning('请先扫描或输入 UDI 码');
  traceScanning.value = true;
  traceResult.value = null;
  try {
    const res = await udiScan(traceUdiInput.value.trim());
    traceScan.value = res.data;
    traceForm.value.stockId = null;
    if (res.data?.parsed && !res.data?.matched)
      ElMessage.warning(res.data.tip || 'UDI-DI 未命中耗材字典');
    else if (!res.data?.parsed)
      ElMessage.warning(res.data?.tip || '无法解析 UDI');
  } catch (e) {
    ElMessage.error(e?.message || '扫码解析失败');
    traceScan.value = null;
  } finally {
    traceScanning.value = false;
  }
};
const loadDeptOptions = async () => {
  if (deptOptions.value.length)
    return;
  const res = await getDepartmentSelectList();
  deptOptions.value = res.data || [];
};
// 就诊锚点跟着患者走：换患者就重拉该患者的挂号/在院记录
const loadVisitAnchors = async () => {
  traceForm.value.registId = null;
  traceForm.value.registNo = '';
  traceForm.value.admissionId = null;
  registOptions.value = [];
  admissionOptions.value = [];
  if (!traceForm.value.patientId)
    return;
  try {
    const tasks = [];
    tasks.push(getRegistrationList({patientId: traceForm.value.patientId, pageNum: 1, pageSize: DEFAULT_PAGE_SIZE}));
    if (traceForm.value.patientName)
      tasks.push(getInpatientListPage({
        patientName: traceForm.value.patientName,
        admitStatus: 1,
        pageNum: 1,
        pageSize: DEFAULT_PAGE_SIZE
      }));
    const [rRes, aRes] = await Promise.all(tasks);
    registOptions.value = rRes.data?.records || [];
    admissionOptions.value = aRes?.data?.records || aRes?.data || [];
  } catch (e) {
    console.error('加载就诊锚点失败:', e);
  }
};
const onTracePatientSelect = (p) => {
  traceForm.value.patientId = String(p.id);
  traceForm.value.patientNo = p.patientNo || '';
  traceForm.value.patientName = p.patientName || '';
  loadVisitAnchors();
};
const onRegistChange = (id) => {
  const r = registOptions.value.find((x) => String(x.id) === String(id));
  traceForm.value.registNo = r?.registNo || '';
};
const handleTraceUseSubmit = async () => {
  const s = traceScan.value;
  const f = traceForm.value;
  if (!s?.matched)
    return ElMessage.warning('请先扫码并命中高值耗材');
  if (!s.batches?.length)
    return ElMessage.warning('该耗材暂无有货批次，请先建批入库');
  if (!f.stockId)
    return ElMessage.warning('请选择出库批次');
  if (!f.patientId)
    return ElMessage.warning('请选择患者');
  if (f.visitMode === 1 && !f.registId)
    return ElMessage.warning('门诊使用请选择挂号记录（计费锚点）');
  if (f.visitMode === 2 && !f.admissionId)
    return ElMessage.warning('住院使用请选择在院记录（计费锚点）');
  try {
    const res = await traceUse({
      udiCode: s.udiCode, consumableId: s.consumableId, stockId: f.stockId,
      patientId: f.patientId, patientNo: f.patientNo, patientName: f.patientName,
      visitType: f.visitMode === 0 ? null : f.visitMode,
      registId: f.visitMode === 1 ? f.registId : null,
      registNo: f.visitMode === 1 ? f.registNo : null,
      admissionId: f.visitMode === 2 ? f.admissionId : null,
      deptId: f.deptId, purpose: f.purpose.trim(),
    });
    traceResult.value = res.data;
    const cs = res.data?.chargeStatus;
    if (cs === 1)
      ElMessage.success(`登记成功，已计费 ${res.data?.chargeNo}`);
    else if (cs === 2)
      ElMessage.warning(`登记成功，但计费失败：${res.data?.chargeFailReason}`);
    else
      ElMessage.warning('登记成功，未关联就诊暂不计费，请到收费窗口手工计费');
    traceUdiInput.value = '';
    traceScan.value = null;
    traceResult.value = res.data;
    loadStock();
  } catch (e) {
    ElMessage.error(e?.message || '登记失败');
  }
};
const resetTraceForm = () => {
  traceUdiInput.value = '';
  traceScan.value = null;
  traceResult.value = null;
  traceForm.value = {
    stockId: null,
    patientId: null,
    patientNo: '',
    patientName: '',
    visitMode: 0,
    registId: null,
    registNo: '',
    admissionId: null,
    deptId: null,
    purpose: ''
  };
};
// ========== L11 溯源台账 ==========
const chargeStatusMap = {
  0: {label: '未计费', color: 'bg-slate-100 text-slate-600'},
  1: {label: '已计费', color: 'bg-emerald-100 text-emerald-700'},
  2: {label: '计费失败', color: 'bg-red-100 text-red-600'},
};
const traceList = ref([]);
const traceKeyword = ref('');
const traceChargeStatusFilter = ref(null);
const traceStatusFilter = ref(null);
const tracePagination = ref({pageNum: 1, pageSize: DEFAULT_PAGE_SIZE, total: 0});
const traceDetailVisible = ref(false);
const traceDetail = ref(null);
const loadTrace = async () => {
  loading.value = true;
  try {
    const params = {pageNum: tracePagination.value.pageNum, pageSize: tracePagination.value.pageSize};
    if (traceKeyword.value.trim())
      params.keyword = traceKeyword.value.trim();
    if (traceChargeStatusFilter.value != null)
      params.chargeStatus = traceChargeStatusFilter.value;
    if (traceStatusFilter.value != null)
      params.status = traceStatusFilter.value;
    const res = await getTraceList(params);
    traceList.value = res.data?.records || [];
    tracePagination.value.total = res.data?.total || 0;
  } catch (e) {
    console.error('加载溯源台账失败:', e);
  } finally {
    loading.value = false;
  }
};
const openTraceDetail = async (row) => {
  try {
    const res = await getTraceDetailById(row.id);
    traceDetail.value = res.data;
    traceDetailVisible.value = true;
  } catch (e) {
    ElMessage.error(e?.message || '加载详情失败');
  }
};
const handleTraceVoid = async (row) => {
  try {
    const {value} = await ElMessageBox.prompt(`作废溯源记录 ${row.traceNo}（耗材将退回批次 ${row.batchNo}）`, '作废登记', {
      confirmButtonText: '确认作废', cancelButtonText: '取消',
      inputValidator: (v) => (v && v.trim().length >= 2) || '作废原因至少 2 个字',
      inputPlaceholder: '作废原因（如：发错型号退回库房）',
    });
    await traceVoid({traceId: row.id, reason: value.trim()});
    ElMessage.success('已作废，耗材退回批次');
    loadTrace();
    loadStock();
  } catch (e) {
    if (e !== 'cancel')
      ElMessage.error(e?.message || '作废失败');
  }
};
const handleTraceRecharge = async (row) => {
  try {
    const res = await traceRecharge(row.id);
    if (res.data?.chargeStatus === 1)
      ElMessage.success(`补记成功，已计费 ${res.data?.chargeNo}`);
    else
      ElMessage.warning(res.data?.chargeFailReason || '补记后仍未计费');
    loadTrace();
  } catch (e) {
    ElMessage.error(e?.message || '补记失败');
  }
};
const handleTabChange = (tab) => {
  if (tab === 'stock')
    loadStock();
  else if (tab === 'consume')
    loadConsume();
  else if (tab === 'log')
    loadLog();
  else if (tab === 'traceLedger')
    loadTrace();
  else if (tab === 'trace')
    loadDeptOptions();
};
onMounted(() => {
  loadStock();
});
</script>
