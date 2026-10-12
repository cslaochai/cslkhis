<template>
  <!-- ============ 登记约谈 ============ -->
  <el-dialog v-model="visible" :title="talkForm.id ? '编辑约谈记录' : '登记医师约谈'" width="640px">
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
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="saveTalk">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { TALK_TYPE, UNREASONABLE_RESULTS, resultText } from '@/lib/rxReview'
import { listRxItemPage, upsertRxTalk } from '@/api/pharmacy'

// row 为空走登记、有值走修改（后端是同一个 talkUpsert）
const props = defineProps({
  modelValue: { type: Boolean, default: false },
  row: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const saving = ref(false)
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

watch(visible, (open) => {
  if (!open) return
  const row = props.row
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
  if (talkForm.doctorName) {
    loadRelatedItems()
  }
})

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
    visible.value = false
    emit('saved')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.4;
}

.inline-pair {
  display: flex;
  gap: 8px;
  width: 100%;
}
</style>
