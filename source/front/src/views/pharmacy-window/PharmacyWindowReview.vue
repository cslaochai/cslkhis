<template>
  <el-dialog
      v-model="visible"
      :title="pendingAction?.type === 'whole' ? '整单发药复核' : '发药复核'"
      data-testid="pharm-review-dialog"
      destroy-on-close
      width="720px"
  >
    <div v-loading="loading" class="min-h-[120px] space-y-4 text-sm">
      <template v-if="pc">
        <!-- 待发信息 -->
        <div class="rounded-lg bg-slate-50 p-3">
          <div>患者：<span class="font-medium">{{ pendingAction?.row?.patientName }}</span></div>
          <div>处方号：<span class="font-mono">{{ pc.prescriptionNo || pendingAction?.row?.prescriptionNo }}</span>
          </div>
          <div v-if="pendingAction?.type === 'one'">
            本次发出：{{ pendingAction?.row?.drugName }} × {{
              pendingAction?.row?.quantity
            }}{{ pendingAction?.row?.unit || '' }}
          </div>
          <div v-else>整单发出该处方全部待发药品（任一药品库存不足则整单不执行）</div>
        </div>

        <!-- 违规结论：BLOCK 红、WARN 黄。文案来自后端，不在这里重写措辞 -->
        <div v-if="blocks.length" class="rounded-lg border border-rose-200 bg-rose-50 p-3"
             data-testid="pharm-block-list">
          <div class="mb-1 font-medium text-rose-700">禁止发药（{{ blocks.length }} 条）</div>
          <ul class="space-y-1">
            <li v-for="(v, i) in blocks" :key="'b' + i" class="flex gap-2 text-rose-700">
              <el-tag effect="dark" size="small" type="danger">禁止</el-tag>
              <span>{{ v.message }}</span>
            </li>
          </ul>
        </div>
        <div v-if="warns.length" class="rounded-lg border border-amber-200 bg-amber-50 p-3"
             data-testid="pharm-warn-list">
          <div class="mb-1 font-medium text-amber-700">提示（{{ warns.length }} 条）</div>
          <ul class="space-y-1">
            <li v-for="(v, i) in warns" :key="'w' + i" class="flex gap-2 text-amber-700">
              <el-tag size="small" type="warning">提示</el-tag>
              <span>{{ v.message }}</span>
            </li>
          </ul>
        </div>
        <div
            v-if="!pc.violations || pc.violations.length === 0"
            class="rounded-lg border border-emerald-200 bg-emerald-50 p-3 text-emerald-700"
            data-testid="pharm-pass"
        >麻精限量校验通过
        </div>

        <!-- 管制明细清单：合规的也列出来，药师才知道这单里的管制品种 -->
        <div v-if="pc.controlledDrugs?.length" class="rounded-lg border border-slate-200 p-3"
             data-testid="pharm-controlled-list">
          <div class="mb-2 font-medium text-slate-700">本处方管制品种（{{ pc.controlledDrugs.length }}）</div>
          <el-table :data="pc.controlledDrugs" border size="small">
            <el-table-column label="药品" min-width="150" prop="drugName" show-overflow-tooltip/>
            <el-table-column label="剂型" prop="dosageForm" width="100"/>
            <el-table-column label="分类" width="120">
              <template #default="{ row }">
                <el-tag :type="rowFlagTag(row.specialFlag)" effect="dark" size="small">{{
                    rowFlagText(row.specialFlag)
                  }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column align="center" label="限量/实际" width="110">
              <template #default="{ row }">
                <span
                    :class="row.actualDays != null && row.limitDays != null && row.actualDays > row.limitDays ? 'font-medium text-rose-600' : ''">
                  {{ row.limitDays ?? '—' }} / {{ row.actualDays ?? '核不出' }} 日
                </span>
              </template>
            </el-table-column>
            <el-table-column label="发药手续" min-width="170">
              <template #default="{ row }">
                <el-tag v-if="row.requiresDualCheck" class="mr-1" size="small" type="danger">双人复核</el-tag>
                <el-tag v-if="row.requiresAmpouleTracking" size="small" type="warning">空安瓿回收</el-tag>
                <span v-if="!row.requiresDualCheck && !row.requiresAmpouleTracking"
                      class="text-xs text-slate-400">常规</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-form label-width="120px">
          <!-- 双人复核：麻醉药品 / 第一类精神药品 -->
          <el-form-item v-if="pc.requiresDualCheck" label="复核药师" required>
            <el-select
                v-model="reviewForm.checkerId"
                :fit-input-width="false"
                class="!w-72"
                clearable
                data-testid="pharm-checker-select"
                filterable
                placeholder="请选择在场复核药师"
            >
              <el-option v-for="e in checkerOptions" :key="e.id" :label="e.empName" :value="e.id"/>
            </el-select>
            <div class="mt-1 text-xs text-rose-600" data-testid="pharm-dual-hint">
              本处方含麻醉药品 / 第一类精神药品 / 毒性药品，调配须双人复核
              （《医疗机构麻醉药品、第一类精神药品管理规定》第 17 条、《医疗用毒性药品管理办法》第 9 条）。
              复核人须为在职、具有药师以上技术职称者，且不得与发药人同一人；
              名单已下推后端按药剂师岗位过滤并排除当前登录人，后端会再按员工 ID 校验一次。
            </div>
          </el-form-item>

          <!-- 二类精神药品超 7 日：须医师注明理由 -->
          <el-form-item v-if="pc.overLimitReasonRequired" label="超量理由" required>
            <el-input
                v-model="reviewForm.overLimitReason"
                :rows="2"
                data-testid="pharm-reason-input"
                placeholder="第二类精神药品超 7 日常用量，须由医师注明理由（《处方管理办法》第 24 条）"
                type="textarea"
            />
          </el-form-item>

          <el-form-item v-if="pc.requiresAmpouleTracking" label="空安瓿">
            <span class="text-xs text-slate-500">
              本处方含麻醉/第一类精神药品注射剂 —— 发药后会自动进麻精专册并置为「待回收」，
              请在专册页登记空安瓿回收与剩余液销毁。
            </span>
          </el-form-item>
        </el-form>
      </template>
    </div>
    <template #footer>
      <span v-if="reviewMissingHint" class="mr-auto align-middle text-xs text-rose-600"
            data-testid="pharm-review-hint">{{ reviewMissingHint }}</span>
      <el-button
          v-perm="'pharmacy:dispensing:edit'"
          :disabled="!canSubmitReview || loading"
          :loading="submitting"
          data-testid="pharm-review-submit"
          type="primary"
          @click="submitDispense"
      >{{ pendingAction?.type === 'whole' ? '确认整单发药' : '确认发药' }}
      </el-button>
      <el-button @click="visible = false">取消</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
/**
 * 发药复核弹框（单行 / 整单共用；麻精限量 + 双人复核 + 超量理由）
 * 从 views/pharmacy-window/PharmacyWindowView.vue 拆出。
 *
 * G10 口径（评审一票否决项）：
 * 1. **发药前预检 + 双人复核**：麻醉药品 / 第一类精神药品必须指定复核药师，
 *    且不得与发药人同一人（《医疗机构麻醉药品、第一类精神药品管理规定》第17条）。
 *    预检（`/narcotic/precheck`）会把「能不能发、为什么不能、每个管制品种限几日」一次说清，
 *    避免"点一次、被拒一次、再猜哪里错"。
 * 2. **超量理由**：第二类精神药品超 7 日须由医师注明理由后才放行（《处方管理办法》第24条），
 *    理由随发药写进专册留痕。
 *
 * ⚠ 这两条在前端都只是**提前告知**，真正的闸门在 his-emr 的 NarcoticControlService：
 * 前端算错只是提示不准，后端漏判才是合规事故。所以这里不"筛掉"任何超限行，
 * 一律把后端的结论原样摆出来。
 */
import {computed, reactive, ref, watch} from 'vue';
import {ElMessage} from 'element-plus';
import {dispense, dispenseByPrescription} from '@/api/dispensing';
import {precheckNarcotic} from '@/api/narcotic';
import {getEmployeeList} from '@/api/system';

const props = defineProps({
  modelValue: {type: Boolean, default: false},
  /** 待执行的发药动作：整单会带 row.prescriptionId，单行会用 row.id */
  pendingAction: {type: Object, default: null},
  /** 管制分类文案/配色（列表列同源，由页面按字典加载后传下） */
  rowFlagText: {type: Function, required: true},
  rowFlagTag: {type: Function, required: true},
});
const emit = defineEmits(['update:modelValue', 'saved']);

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
});

const loading = ref(false);
const submitting = ref(false);
const reviewForm = reactive({
  checkerId: null,
  overLimitReason: '',
  precheck: null,
});
/** 复核药师候选。当前登录人的姓名在 localStorage（登录/切换时写入），
 *  用它做前端软过滤；**硬判定在后端**（按员工ID 比，见 resolveAndAssertChecker）。 */
const checkerOptions = ref([]);
const currentUserName = ref(localStorage.getItem('realName') || '');

async function loadCheckerOptions() {
  if (checkerOptions.value.length)
    return;
  try {
    // empType=4（药剂师）**下推后端**，不在前端对全量员工切片 ——
    // 法条要求复核人"具有药师以上技术职称"，护士/收费员不能当麻精复核人。
    // 前端这层只是省得药师选错人；后端 resolveAndAssertChecker 仍按员工ID 再判一次。
    const res = await getEmployeeList({empType: 4});
    const list = res.data?.records || res.data || [];
    // 同一人不能给自己复核：登录时把 realName 写进了 localStorage，用它做前端软过滤
    // （/auth/info 不带 employeeId，前端拿不到自己的员工ID，所以只能按姓名滤）
    checkerOptions.value = list.filter((e) => e.status === 1 && e.empName !== currentUserName.value);
  } catch (e) {
    console.error('加载复核药师候选失败', e);
  }
}

/** 弹窗内结论 */
const pc = computed(() => reviewForm.precheck || null);
const blocks = computed(() => (pc.value?.violations || []).filter((v) => v.level === 'BLOCK'));
const warns = computed(() => (pc.value?.violations || []).filter((v) => v.level !== 'BLOCK'));
/** 提交条件：无阻断、需复核则必须选人、只差超量理由则必须填理由 */
const canSubmitReview = computed(() => {
  const p = pc.value;
  if (!p)
    return false;
  if (!p.canDispense && !p.overLimitReasonRequired)
    return false;
  if (p.requiresDualCheck && !reviewForm.checkerId)
    return false;
  if (p.overLimitReasonRequired && !reviewForm.overLimitReason.trim())
    return false;
  return true;
});
const reviewMissingHint = computed(() => {
  const p = pc.value;
  if (!p)
    return '';
  if (!p.canDispense && !p.overLimitReasonRequired)
    return '存在禁止发药的问题，请先修正处方';
  if (p.requiresDualCheck && !reviewForm.checkerId)
    return '本处方含麻醉药品 / 第一类精神药品，必须选择复核药师';
  if (p.overLimitReasonRequired && !reviewForm.overLimitReason.trim())
    return '第二类精神药品超 7 日，须填写医师注明理由';
  return '';
});

/**
 * 打开即预检：
 *   预检 → 摆结论 → （麻精）选复核药师 /（二类精神超限）填超量理由 → 提交。
 *
 * 单行与整单共用一套弹窗，因为两者的判定完全一致（都按处方整体判限量），
 * 差别只在最后调哪个接口。分成两套弹窗只会让口径跑偏。
 */
async function runPrecheck() {
  const row = props.pendingAction?.row;
  reviewForm.precheck = null;
  reviewForm.checkerId = null;
  reviewForm.overLimitReason = '';
  loading.value = true;
  try {
    const res = await precheckNarcotic(row.prescriptionId);
    reviewForm.precheck = res.data;
  } catch (e) {
    // 预检接口本身失败（网络/权限）不能当成"可以发"——直接关掉弹窗，让药师重试
    ElMessage.error(e?.message || '麻精预检失败，请稍后重试');
    visible.value = false;
  } finally {
    loading.value = false;
  }
}

const submitDispense = async () => {
  const p = pc.value;
  const action = props.pendingAction;
  if (!p || !action)
    return;
  const reason = reviewForm.overLimitReason.trim();
  submitting.value = true;
  try {
    // 填了理由就带理由重检一次：由后端确认"加了理由确实可发"。
    // 不在前端推断而直接发 —— 前端推断错的话，专册里会留下一笔没有依据的超量发药。
    if (reason) {
      const again = await precheckNarcotic(p.prescriptionId, reason);
      const pc2 = again.data;
      if (!pc2?.canDispense) {
        reviewForm.precheck = pc2;
        ElMessage.error('填写理由后仍存在不可放行的限量问题，请先修正处方');
        return;
      }
    }
    const checkerId = reviewForm.checkerId || undefined;
    if (action.type === 'whole') {
      await dispenseByPrescription(action.row.prescriptionId, checkerId, reason || undefined);
      ElMessage.success('整单发药成功');
    } else {
      await dispense({id: action.row.id, checkerId, overLimitReason: reason || undefined});
      ElMessage.success('发药成功，库存已扣减');
    }
    visible.value = false;
    emit('saved');
  } catch (e) {
    // 后端闸门的原始结论直接摆出来（含"哪个药、限几日、超几日"），不替换成笼统措辞
    ElMessage.error(e?.message || '发药失败');
  } finally {
    submitting.value = false;
  }
};

watch(() => props.modelValue, (v) => {
  if (v) {
    loadCheckerOptions();
    runPrecheck();
  }
});
</script>
