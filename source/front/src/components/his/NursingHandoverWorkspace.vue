<script setup lang="ts">
/**
 * AI 护理交接班工作区（G-13）
 *
 * 分工铁律：事实（在院/出入院/体征越阈/高风险评估）全部由后端代码聚合，
 * 模型只拟 SBAR 摘要草稿 —— 摘要可编辑、护士终审，产物不写库，
 * 护士确认后自行贴进交班记录。「窗即班次」：时间窗由后端按班次算，前端只传日期。
 *
 * source=2（规则模板）或 degraded=true 时警示条必显 —— 护士有权知道这段文字是谁写的。
 */
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { DocumentCopy, MagicStick } from '@element-plus/icons-vue'
import { getInpatientWardList } from '@/api/inpatient'
import { composeNursingHandover } from '@/api/ai'

const wardId = ref<string>('')
const wardOptions = ref<any[]>([])
const shift = ref<number>(1)
const shiftDate = ref<string>('')

const composeLoading = ref(false)
const result = ref<any>(null)
const summaryText = ref('')

const SHIFT_OPTIONS = [
  { value: 1, label: '白班（08:00-16:00）' },
  { value: 2, label: '小夜（16:00-24:00）' },
  { value: 3, label: '大夜（00:00-08:00）' },
]

const shiftLabel = () => SHIFT_OPTIONS.find((s) => s.value === shift.value)?.label || ''

// 默认班次按当前时刻落位，符合「正在交的班」直觉
const defaultShift = () => {
  const h = new Date().getHours()
  if (h >= 8 && h < 16) return 1
  if (h >= 16) return 2
  return 3
}

const loadWards = async () => {
  try {
    const res: any = await getInpatientWardList()
    if (res.code !== 200) {
      ElMessage.error(res.message || '病区列表加载失败')
      return
    }
    wardOptions.value = res.data || []
    if (wardOptions.value.length && !wardId.value) {
      wardId.value = String(wardOptions.value[0].wardId)
    }
  } catch (e: any) {
    ElMessage.error(e?.message || '病区列表加载失败')
  }
}

const compose = async () => {
  if (!wardId.value) {
    ElMessage.warning('请先选择病区')
    return
  }
  composeLoading.value = true
  try {
    const res: any = await composeNursingHandover({
      wardId: wardId.value,
      shift: shift.value,
      shiftDate: shiftDate.value,
    })
    if (res.code !== 200) {
      ElMessage.error(res.message || '摘要生成失败')
      return
    }
    result.value = res.data
    summaryText.value = res.data?.summary || ''
  } catch (e: any) {
    ElMessage.error(e?.message || '摘要生成失败')
  } finally {
    composeLoading.value = false
  }
}

const copySummary = async () => {
  if (!summaryText.value) return
  try {
    await navigator.clipboard.writeText(summaryText.value)
    ElMessage.success('摘要已复制，请粘贴到交班记录并核对')
  } catch {
    ElMessage.error('复制失败，请手动选择文本复制')
  }
}

onMounted(async () => {
  const now = new Date()
  shiftDate.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  shift.value = defaultShift()
  await loadWards()
})
</script>

<template>
  <div class="space-y-4" data-testid="handover-workspace">
    <!-- 工具条 -->
    <div class="flex flex-wrap items-center gap-3 rounded-xl border border-slate-200 bg-white p-3">
      <el-select v-model="wardId" class="!w-56" placeholder="病区" data-testid="handover-ward">
        <el-option v-for="w in wardOptions" :key="w.wardId" :label="w.wardName" :value="String(w.wardId)" />
      </el-select>
      <el-radio-group v-model="shift" data-testid="handover-shift">
        <el-radio-button v-for="s in SHIFT_OPTIONS" :key="s.value" :value="s.value">{{ s.label }}</el-radio-button>
      </el-radio-group>
      <el-date-picker
        v-model="shiftDate"
        type="date"
        value-format="YYYY-MM-DD"
        :clearable="false"
        class="!w-40"
        placeholder="班次日期"
        data-testid="handover-date"
      />
      <el-button
        type="primary"
        :loading="composeLoading"
        :disabled="!wardId"
        data-testid="handover-compose"
        @click="compose"
      >
        <el-icon class="mr-1"><MagicStick /></el-icon>AI 拟摘要
      </el-button>
    </div>

    <template v-if="result">
      <!-- 来源与降级警示：source=2 或 degraded 必显 -->
      <el-alert
        v-if="result.degraded || result.source === 2"
        type="warning"
        :closable="false"
        show-icon
        data-testid="handover-degraded"
        :title="`本摘要未经过大模型（${result.degradeReason || '规则模板拼接'}），来自确定性事实拼接，请人工补写观察与交接重点。`"
      />

      <!-- 事实区：全部是后端聚合的代码事实 -->
      <div class="grid gap-3 md:grid-cols-3" data-testid="handover-facts">
        <div class="rounded-xl border border-slate-200 bg-white p-3">
          <div class="flex items-baseline justify-between">
            <h4 class="text-sm font-semibold text-slate-700">出入院</h4>
            <span class="text-xs text-slate-500">在院 {{ result.inHospitalCount }} · 出院 {{ result.dischargeCount }}</span>
          </div>
          <ul v-if="(result.newAdmissions || []).length" class="mt-2 list-disc pl-5 text-sm text-slate-700">
            <li v-for="(n, i) in result.newAdmissions" :key="i">{{ n }}</li>
          </ul>
          <div v-else class="mt-2 text-sm text-slate-400">本班次无新入院</div>
        </div>
        <div class="rounded-xl border border-slate-200 bg-white p-3">
          <h4 class="text-sm font-semibold text-slate-700">体征越阈事件</h4>
          <ul v-if="(result.abnormalEvents || []).length" class="mt-2 list-disc pl-5 text-sm text-slate-700">
            <li v-for="(n, i) in result.abnormalEvents" :key="i">{{ n }}</li>
          </ul>
          <div v-else class="mt-2 text-sm text-slate-400">本班次无越阈事件</div>
        </div>
        <div class="rounded-xl border border-slate-200 bg-white p-3">
          <h4 class="text-sm font-semibold text-slate-700">高风险评估</h4>
          <ul v-if="(result.riskAssessments || []).length" class="mt-2 list-disc pl-5 text-sm text-slate-700">
            <li v-for="(n, i) in result.riskAssessments" :key="i">{{ n }}</li>
          </ul>
          <div v-else class="mt-2 text-sm text-slate-400">本班次无高风险评估</div>
        </div>
      </div>

      <!-- 摘要草稿：护士编辑终审 -->
      <div class="rounded-xl border border-slate-200 bg-white p-3">
        <div class="flex flex-wrap items-center justify-between gap-2">
          <h4 class="text-sm font-semibold text-slate-700">
            SBAR 交接班摘要（{{ result.wardName }} · {{ result.shiftText }} · {{ result.windowText }}）
            <span
              class="ml-2 rounded px-1.5 py-0.5 text-xs"
              :class="result.source === 1 ? 'bg-[#0E9488]/10 text-[#0B6B63]' : 'bg-slate-100 text-slate-600'"
              data-testid="handover-source"
            >{{ result.source === 1 ? 'AI 草稿' : '规则模板' }}</span>
          </h4>
          <el-button size="small" :disabled="!summaryText" data-testid="handover-copy" @click="copySummary">
            <el-icon class="mr-1"><DocumentCopy /></el-icon>复制
          </el-button>
        </div>
        <el-input
          v-model="summaryText"
          type="textarea"
          :rows="10"
          maxlength="500"
          show-word-limit
          class="mt-2"
          placeholder="点击「AI 拟摘要」生成草稿，护士核对事实后编辑定稿"
          data-testid="handover-summary"
        />
        <div class="mt-1 text-xs text-slate-400">草稿不写库：核对上方事实后编辑定稿，复制粘贴到交班记录使用。</div>
      </div>
    </template>

    <!-- 首屏空态 -->
    <div v-else class="rounded-xl border border-dashed border-slate-300 bg-white py-16 text-center text-sm text-slate-500" data-testid="handover-empty">
      选择病区与班次，点击「AI 拟摘要」生成交接班草稿（{{ shiftLabel() }}）
    </div>
  </div>
</template>
