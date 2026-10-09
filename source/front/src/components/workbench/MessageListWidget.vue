<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { readMessage } from '@/api/system'
import {
  messageActionOwner,
  messageActionPermissions,
  messageLabel,
  messagePayloadChips,
  messageTagClass,
} from '@/lib/messageCatalog'
import { hasAnyPerm } from '@/lib/perm'
import MessageProcessDialog from '@/components/his/MessageProcessDialog.vue'

const props = defineProps<{
  code: string
  mode: 'todo' | 'notice'
  data: Record<string, any> | null
  error?: string | null
}>()

const emit = defineEmits<{ (e: 'refresh'): void }>()

const router = useRouter()
const processVisible = ref(false)
const processTarget = ref<any>(null)

const items = computed<any[]>(() => props.data?.items || [])
const total = computed(() => Number(props.data?.total ?? 0))
const urgentTotal = computed(() => Number(props.data?.urgentTotal ?? 0))
const ready = computed(() => !!props.data && !props.error)

const isTodo = computed(() => props.mode === 'todo')

/** 待办的处置入口按当前角色权限锁；无权时只提示归属岗位，不给点了报错的按钮 */
const canProcess = (item: any) => hasAnyPerm(messageActionPermissions(item.bizType))

const openProcess = (item: any) => {
  processTarget.value = item
  processVisible.value = true
}

const markRead = async (item: any) => {
  try {
    await readMessage(item.messageId)
    emit('refresh')
  } catch (e: any) {
    ElMessage.error(e?.message || '标记已读失败')
  }
}

const onProcessed = () => {
  processVisible.value = false
  emit('refresh')
}
</script>

<template>
  <div>
    <div class="mb-3 flex items-center justify-between gap-3">
      <p class="text-[15px] text-slate-600">
        <template v-if="ready">
          共 <span class="text-[18px] font-bold text-slate-900">{{ total }}</span> 条
          <span v-if="isTodo && urgentTotal > 0" class="ml-2 text-red-600">危急值 {{ urgentTotal }} 条</span>
          <span v-else-if="!isTodo && total === 0" class="ml-2 text-slate-400">全部已读</span>
        </template>
        <span v-else class="text-slate-400">取数失败</span>
      </p>
      <a class="shrink-0 text-[15px] text-[#1269B5] hover:underline" @click="router.push('/messages')">
        查看全部
      </a>
    </div>

    <div v-if="!ready" class="py-8 text-center text-[15px] text-slate-400">—</div>
    <div v-else-if="items.length === 0" class="py-8 text-center text-[15px] text-slate-400">
      {{ isTodo ? '暂无待办' : '暂无未读通知' }}
    </div>

    <ul v-else class="divide-y divide-slate-100">
      <li v-for="item in items" :key="item.messageId" class="flex items-center gap-3 py-2.5">
        <span :class="['shrink-0 rounded px-2 py-0.5 text-[13px] font-medium', messageTagClass(item.bizType)]">
          {{ messageLabel(item.bizType) }}
        </span>
        <div class="min-w-0 flex-1">
          <p class="truncate text-[15px] font-medium text-slate-800" :title="item.title">
            {{ item.title || messageLabel(item.bizType) }}
          </p>
          <p class="mt-0.5 truncate text-[13px] text-slate-500">
            <span>{{ item.sendTime || '—' }}</span>
            <span v-for="chip in messagePayloadChips(item.payload)" :key="chip.label" class="ml-3">
              {{ chip.label }}：{{ chip.text }}
            </span>
          </p>
        </div>
        <button
            v-if="isTodo && canProcess(item)"
            class="shrink-0 rounded-md border border-[#1269B5] px-3 py-1 text-[14px] text-[#1269B5] transition-colors hover:bg-[#1269B5] hover:text-white"
            @click="openProcess(item)"
        >处理</button>
        <span v-else-if="isTodo" class="shrink-0 text-[13px] text-slate-400">
          由{{ messageActionOwner(item.bizType) }}处理
        </span>
        <button
            v-else
            class="shrink-0 rounded-md border border-slate-300 px-3 py-1 text-[14px] text-slate-600 transition-colors hover:border-slate-400 hover:text-slate-800"
            @click="markRead(item)"
        >已读</button>
      </li>
    </ul>

    <MessageProcessDialog v-model="processVisible" :message="processTarget" @processed="onProcessed" />
  </div>
</template>
