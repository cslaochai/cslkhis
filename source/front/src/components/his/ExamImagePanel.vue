<script setup lang="ts">
import {computed, onMounted, ref, watch} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {Delete, ZoomIn} from '@element-plus/icons-vue'
import {examImageDeleteById, examImageListByApplyId, examImageMockImport, examImageSrc, examImageUpload} from '@/api/examImage'
import {getDictDataMapList} from '@/api/system'
import {hasPerm} from '@/lib/perm'

/**
 * 简化 PACS 影像区块（sql/137）
 *
 * 挂在「检查申请单」这个锚点上（bizType 1-检查 2-检验），写入入口目前只有检查工作站的录入弹框：
 * 超声/内镜工作站是自己的记录表（没有 apply_id），要先接就得给它们补申请单链，不在本期范围。
 * 医生站与小程序的报告详情吃同一个 `images` 出参，但它们是 HTML/wxml 静态渲染，不复用本组件。
 * 窗宽窗位用 CSS filter 模拟 —— 学习阶段拿的是 jpg/png 不是真 DICOM，没有 HU 值可映射，
 * 但阅片动作（变亮变暗、增减对比）的观感必须能演示与验证。
 */
const props = withDefaults(defineProps<{
    bizType: number
    applyId?: string | number | null
    readonly?: boolean
    title?: string
    thumbHeight?: string
}>(), {
    applyId: null,
    readonly: false,
    title: '影像资料',
    thumbHeight: '104px',
})

const images = ref<any[]>([])
const loading = ref(false)
const uploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)
const modalityOptions = ref<any[]>([])

const canAdd = computed(() => !props.readonly && hasPerm('medtech:inspectionWorkstation:imageAdd'))
const canDelete = computed(() => !props.readonly && hasPerm('medtech:inspectionWorkstation:imageDelete'))

async function loadImages() {
    if (!props.applyId) {
        images.value = []
        return
    }
    loading.value = true
    try {
        const res: any = await examImageListByApplyId(props.bizType, props.applyId)
        images.value = res.data || []
    } catch (e: any) {
        ElMessage.error(e?.message || '影像列表加载失败')
    } finally {
        loading.value = false
    }
}

watch(() => [props.applyId, props.bizType], loadImages)
onMounted(() => {
    loadImages()
    // 模态复用设备档位字典（sql/137 第五条：不另造 his_exam_modality，同一台机器不能有两个名字）
    // 注意 selectGroup 的 dictTypes 是逗号拼接的字符串，传数组会被 Jackson 判成请求体格式错误
    getDictDataMapList('his_exam_device_type').then((res: any) => {
        modalityOptions.value = res.data?.his_exam_device_type || []
    }).catch(() => {
        modalityOptions.value = []
    })
})

defineExpose({reload: loadImages})

// ========== 上传 ==========

async function onPickFile(ev: Event) {
    const input = ev.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file) return
    if (!/\.(png|jpe?g)$/i.test(file.name)) {
        ElMessage.warning('影像只支持 jpg / png 格式')
        return
    }
    uploading.value = true
    try {
        await examImageUpload({file, bizType: props.bizType, applyId: props.applyId, modality: modality.value || undefined})
        ElMessage.success('影像已上传')
        await loadImages()
    } catch (e: any) {
        ElMessage.error(e?.message || '上传失败')
    } finally {
        uploading.value = false
    }
}

// ========== 模拟 DICOM 导入 ==========

const modality = ref<number | ''>('')
const mockVisible = ref(false)
const mockFrameCount = ref(6)

async function submitMockImport() {
    uploading.value = true
    try {
        const res: any = await examImageMockImport({
            bizType: props.bizType, applyId: props.applyId,
            modality: modality.value || undefined, frameCount: mockFrameCount.value,
        })
        ElMessage.success(`已导入 ${(res.data || []).length} 帧模拟影像（非真实检查结果）`)
        mockVisible.value = false
        await loadImages()
    } catch (e: any) {
        ElMessage.error(e?.message || '导入失败')
    } finally {
        uploading.value = false
    }
}

// ========== 删除（留痕在后端审计） ==========

async function onDelete(img: any) {
    let reason = ''
    try {
        const r: any = await ElMessageBox.prompt(
            `将删除第 ${img.seq} 帧「${img.fileName}」及其磁盘文件，删除动作会写入审计日志。请填写删除原因：`,
            '删除影像帧',
            {confirmButtonText: '确认删除', cancelButtonText: '取消', inputPlaceholder: '如：重复帧 / 传错病人'}
        )
        reason = r.value || ''
    } catch {
        return
    }
    try {
        await examImageDeleteById(img.id, reason)
        ElMessage.success('已删除（操作已留痕）')
        await loadImages()
    } catch (e: any) {
        ElMessage.error(e?.message || '删除失败')
    }
}

// ========== 阅片器 ==========

const viewerVisible = ref(false)
const currentIdx = ref(0)
const zoom = ref(1)
const rotate = ref(0)
const windowWidth = ref(1)
const windowLevel = ref(0)
const pan = ref({x: 0, y: 0})

const current = computed(() => images.value[currentIdx.value] || null)
const imageStyle = computed(() => ({
    transform: `translate(${pan.value.x}px, ${pan.value.y}px) scale(${zoom.value}) rotate(${rotate.value}deg)`,
    filter: `grayscale(1) contrast(${windowWidth.value}) brightness(${1 + windowLevel.value * 0.6})`,
}))

function openViewer(idx: number) {
    currentIdx.value = idx
    zoom.value = 1
    rotate.value = 0
    windowWidth.value = 1
    windowLevel.value = 0
    pan.value = {x: 0, y: 0}
    viewerVisible.value = true
}

function step(delta: number) {
    if (!images.value.length) return
    openViewer((currentIdx.value + delta + images.value.length) % images.value.length)
}

function onWheel(ev: WheelEvent) {
    ev.preventDefault()
    zoom.value = Math.min(8, Math.max(0.3, zoom.value * (ev.deltaY < 0 ? 1.15 : 0.87)))
}

let dragFrom: { x: number; y: number; px: number; py: number } | null = null

function onDragStart(ev: MouseEvent) {
    dragFrom = {x: ev.clientX, y: ev.clientY, px: pan.value.x, py: pan.value.y}
}

function onDragMove(ev: MouseEvent) {
    if (!dragFrom) return
    pan.value = {x: dragFrom.px + ev.clientX - dragFrom.x, y: dragFrom.py + ev.clientY - dragFrom.y}
}

function onDragEnd() {
    dragFrom = null
}
</script>

<template>
  <div class="exam-image-panel" data-testid="exam-panel">
    <div class="mb-2 flex flex-wrap items-center gap-3">
      <p class="text-sm font-medium text-slate-700">{{ title }}
        <span class="ml-1 text-xs text-slate-400">共 {{ images.length }} 帧</span>
      </p>
      <div v-if="canAdd" class="ml-auto flex flex-wrap items-center gap-2">
        <el-select v-model="modality" size="small" placeholder="模态" clearable :fit-input-width="false"
                   style="width: 120px" data-testid="exam-modality">
          <el-option v-for="d in modalityOptions" :key="d.dictValue" :label="d.dictLabel"
                     :value="Number(d.dictValue)"/>
        </el-select>
        <el-button size="small" :loading="uploading" data-testid="exam-upload"
                   @click="fileInput?.click()">上传影像
        </el-button>
        <el-button size="small" type="primary" plain data-testid="exam-mock" @click="mockVisible = true">
          模拟 DICOM 导入
        </el-button>
        <input ref="fileInput" type="file" accept="image/png,image/jpeg" class="hidden"
               data-testid="exam-file" @change="onPickFile"/>
      </div>
    </div>

    <div v-loading="loading" class="min-h-[70px]">
      <p v-if="!images.length" class="rounded bg-slate-50 py-5 text-center text-xs text-slate-400"
         data-testid="exam-empty">
        {{ canAdd ? '该申请单还没有影像：可上传 jpg/png，或用「模拟 DICOM 导入」生成测试帧' : '该申请单还没有影像' }}
      </p>
      <div v-else class="grid grid-cols-4 gap-3" data-testid="exam-grid">
        <div v-for="(img, idx) in images" :key="img.id" data-testid="exam-cell"
             class="group relative cursor-zoom-in overflow-hidden rounded border border-slate-200 bg-black"
             @click="openViewer(idx)">
          <img :src="examImageSrc(img.fileUrl)" :alt="img.fileName"
               :style="{height: thumbHeight}" class="w-full object-contain" loading="lazy"/>
          <span class="absolute left-1 top-1 rounded bg-black/70 px-1.5 py-0.5 text-[11px] text-white"
                data-testid="exam-seq">#{{ img.seq }}</span>
          <span v-if="img.source === 2"
                class="absolute right-1 top-1 rounded bg-amber-500/90 px-1.5 py-0.5 text-[11px] text-white"
                data-testid="exam-mock-tag">模拟</span>
          <span class="absolute inset-x-0 bottom-0 truncate bg-black/60 px-1.5 py-1 text-[11px] text-slate-100">
            {{ img.modalityText || '未标模态' }} · {{ img.createBy || '-' }}
          </span>
          <el-button v-if="canDelete" size="small" type="danger" :icon="Delete" circle
                     class="invisible absolute right-1 top-6 group-hover:visible" data-testid="exam-delete"
                     @click.stop="onDelete(img)"/>
        </div>
      </div>
    </div>

    <!--
      阅片器：故意不用 fullscreen。全屏弹框整屏都是对话框本体，「点弹窗外关闭」（AGENTS §2）在这种形态下
      根本点不到 —— 实测点左上角只命中 stage，弹框关不掉。留 4% 边距当遮罩，点边上一条就关。
    -->
    <el-dialog v-model="viewerVisible" title="阅片器" width="96%" top="4vh" destroy-on-close
               data-testid="exam-viewer-dialog">
      <div v-if="current" class="flex h-full flex-col gap-3 lg:flex-row">
        <div class="exam-stage flex-1 overflow-hidden rounded bg-black" data-testid="exam-stage"
             @wheel="onWheel" @mousedown="onDragStart" @mousemove="onDragMove"
             @mouseup="onDragEnd" @mouseleave="onDragEnd">
          <img :src="examImageSrc(current.fileUrl)" :style="imageStyle" draggable="false"
               class="mx-auto block max-h-[62vh] origin-center select-none" data-testid="exam-frame" alt="影像"/>
        </div>
        <div class="w-full shrink-0 space-y-3 lg:w-80">
          <div class="rounded border border-slate-200 p-3">
            <p class="mb-2 text-sm font-bold text-slate-700">帧信息</p>
            <p class="text-xs text-slate-600">患者：{{ current.patientName || '-' }}</p>
            <p class="text-xs text-slate-600">项目：{{ current.itemName || '-' }}</p>
            <p class="text-xs text-slate-600">部位/标本：{{ current.bodyPart || '-' }}</p>
            <p class="text-xs text-slate-600">模态：{{ current.modalityText || '未标' }}</p>
            <p class="text-xs text-slate-600" data-testid="exam-cur-seq">序号：第 {{ current.seq }} 帧（本单共
              {{ images.length }} 帧）</p>
            <p class="text-xs text-slate-600">来源：{{ current.sourceText }}</p>
            <p class="text-xs text-slate-600">上传：{{ current.createBy || '-' }} {{ current.createTime || '' }}</p>
            <p v-if="current.source === 2" class="mt-1 text-xs text-amber-600">
              本帧由系统生成，属演示夹具，不代表真实检查结果</p>
          </div>
          <div class="rounded border border-slate-200 p-3">
            <p class="mb-2 text-sm font-bold text-slate-700">显示调节</p>
            <div class="mb-3 flex flex-wrap gap-2">
              <el-button size="small" :icon="ZoomIn" data-testid="exam-zoom-in"
                         @click="zoom = Math.min(8, zoom * 1.25)"/>
              <el-button size="small" data-testid="exam-zoom-out" @click="zoom = Math.max(0.3, zoom / 1.25)">
                缩小
              </el-button>
              <el-button size="small" data-testid="exam-rotate" @click="rotate = (rotate + 90) % 360">旋转 90°
              </el-button>
            </div>
            <p class="text-xs text-slate-500" data-testid="exam-zoom-text">放大倍率 {{ zoom.toFixed(2) }}× ·
              旋转 {{ rotate }}°</p>
            <p class="mb-1 mt-2 text-xs text-slate-500">窗宽 WW {{ windowWidth.toFixed(2) }}</p>
            <el-slider v-model="windowWidth" :min="0.4" :max="3" :step="0.05" size="small" data-testid="exam-ww"/>
            <p class="mb-1 mt-2 text-xs text-slate-500">窗位 WL {{ windowLevel.toFixed(2) }}</p>
            <el-slider v-model="windowLevel" :min="-1" :max="1" :step="0.05" size="small" data-testid="exam-wl"/>
            <p class="mt-2 text-[11px] leading-4 text-slate-400">
              窗宽窗位以 CSS contrast/brightness 模拟观感；接真 DICOM 后换成像素级 windowCenter/Width 重绘。</p>
          </div>
          <div class="flex gap-2">
            <el-button data-testid="exam-prev" @click="step(-1)">上一帧</el-button>
            <el-button data-testid="exam-next" @click="step(1)">下一帧</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="mockVisible" title="模拟 DICOM 导入" width="420px" data-testid="exam-mock-dialog">
      <p class="mb-3 text-xs text-slate-500">
        学习阶段不接真设备：由后端按申请单生成互不相同的灰阶测试帧，用来把「上传→列表→阅片→删除」这条链跑通。
      </p>
      <el-form label-width="80px">
        <el-form-item label="帧数">
          <el-input-number v-model="mockFrameCount" :min="1" :max="24" data-testid="exam-mock-count"/>
        </el-form-item>
        <el-form-item label="模态">
          <el-select v-model="modality" placeholder="可空" clearable :fit-input-width="false" style="width: 100%">
            <el-option v-for="d in modalityOptions" :key="d.dictValue" :label="d.dictLabel"
                       :value="Number(d.dictValue)"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="mockVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" data-testid="exam-mock-ok" @click="submitMockImport">
          导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.exam-stage {
  min-height: 58vh;
  cursor: grab;
}
</style>
