<template>
  <div class="wf-designer">
    <div class="wf-toolbar">
      <el-button size="small" @click="backToList">
        <el-icon style="margin-right: 4px"><ArrowLeft/></el-icon>
        返回流程列表
      </el-button>
      <el-divider direction="vertical"/>
      <el-input v-model="form.flowName" :disabled="loading" placeholder="流程名称" size="small" style="width: 160px"/>
      <el-input v-model="form.flowCode" :disabled="!!form.id || loading" placeholder="流程编码(唯一)"
                size="small" style="width: 190px; margin-left: 8px"/>
      <el-input v-model="form.flowDesc" :disabled="loading" placeholder="说明（意图识别会看这句）"
                size="small" style="width: 260px; margin-left: 8px"/>
      <el-tag v-if="isPublished" size="small" style="margin-left: 12px" type="success">已发布</el-tag>
      <el-tag v-else-if="form.id" size="small" style="margin-left: 12px" type="warning">草稿</el-tag>
      <el-tag v-else size="small" style="margin-left: 12px" type="info">新建</el-tag>

      <div style="margin-left: auto; display: flex; gap: 8px">
        <el-button v-if="nodes.length > 1" size="small" @click="autoLayout">
          <el-icon style="margin-right: 4px"><MagicStick/></el-icon>
          一键美化
        </el-button>
        <el-button v-if="!isPublished" :loading="saving" size="small" @click="saveDraft">保存草稿</el-button>
        <el-button :loading="publishing" size="small" type="success" @click="saveAndPublish">
          {{ isPublished ? '保存并重新发布' : '保存并发布' }}
        </el-button>
      </div>
    </div>

    <div class="wf-body">
      <!-- 左：节点面板 -->
      <div class="wf-panel wf-panel-left">
        <div class="panel-title">拖节点到画布</div>
        <div
            v-for="item in palette"
            :key="item.key"
            class="palette-item"
            draggable="true"
            @dragstart="onDragStart($event, item)"
        >
          <span class="palette-icon" :style="{ color: item.color }">{{ item.icon }}</span>
          <span class="palette-label">{{ item.label }}</span>
          <span class="palette-hint">{{ item.hint }}</span>
        </div>

        <el-divider style="margin: 14px 0"/>
        <div class="panel-title">流转线条件写法</div>
        <div class="tips">
          只支持 <code>变量路径 运算符 字面量</code>：<br/>
          <code>ROUTE.flowCode == 'ANSWER_REPORT'</code><br/>
          <code>qcScore &gt; 60</code><br/>
          兜底分支的条件写 <code>DEFAULT</code>。
        </div>
      </div>

      <!-- 中：画布 -->
      <div class="wf-canvas">
        <div class="wf-drop-zone" @dragover.prevent @drop.stop.prevent="onDrop">
          <VueFlow
              v-model:edges="edges"
              v-model:nodes="nodes"
              :connect-on-click="false"
              :default-edge-options="{ type: 'smoothstep', label: '' }"
              :delete-key-code="null"
              :max-zoom="2"
              :min-zoom="0.2"
              :node-types="nodeTypes"
              fit-view-on-init
              @connect="onConnect"
              @edge-click="onEdgeClick"
              @error="onFlowError"
              @node-click="onNodeClick"
              @pane-click="onPaneClick"
          >
            <Background :gap="16" pattern-color="#e2e8f0"/>
            <Controls position="bottom-left"/>
            <MiniMap position="bottom-right"/>
          </VueFlow>
        </div>
        <div v-if="!nodes.length" class="canvas-empty">从左侧拖入节点开始设计流程</div>
      </div>

      <!-- 右：属性面板 -->
      <div class="wf-panel wf-panel-right">
        <template v-if="selectedNode">
          <div class="panel-title">节点属性</div>
          <div class="form-row">
            <label>节点编码</label>
            <code class="code-block">{{ selectedNode.id }}</code>
          </div>
          <div class="form-row">
            <label>节点名称</label>
            <el-input v-model="selectedNode.data.label" size="small"/>
          </div>

          <template v-if="selectedNode.data.nodeType === 1">
            <div class="form-row">
              <label>审批人表达式</label>
              <el-input v-model="selectedNode.data.permissionFlag" placeholder="role:DOCTOR@@user:1001" size="small"/>
              <div class="form-hint">role: 角色码，user: 员工ID，@@ 表示多个候选人</div>
            </div>
            <div class="form-row">
              <label>签核方式</label>
              <el-select v-model="selectedNode.data.nodeRatio" size="small" style="width: 100%">
                <el-option label="或签（任一通过）" value="0"/>
                <el-option label="会签（全部通过）" value="100"/>
                <el-option label="票签 60%" value="60"/>
                <el-option label="票签 80%" value="80"/>
              </el-select>
            </div>
          </template>

          <template v-if="selectedNode.data.nodeType === 3 || selectedNode.data.nodeType === 4">
            <div class="form-row">
              <label>AI 动作</label>
              <el-select v-model="selectedNode.data.aiAction" clearable placeholder="不配则走纯分支" size="small"
                         style="width: 100%">
                <el-option label="能力调用（调 AI 出结果）" value="CAPABILITY"/>
                <el-option label="意图识别（输出意图码）" value="ROUTE"/>
              </el-select>
            </div>
            <div v-if="selectedNode.data.aiAction" class="form-row">
              <label>提示词模板编码</label>
              <el-input v-model="selectedNode.data.aiTemplateName" placeholder="flow-answer-general" size="small"/>
            </div>
            <div v-if="selectedNode.data.aiAction === 'CAPABILITY'" class="form-row">
              <label>能力键（可不填）</label>
              <el-input v-model="selectedNode.data.aiCapabilityKey" placeholder="flow_ai_node" size="small"/>
            </div>
            <div v-if="selectedNode.data.aiAction" class="form-row">
              <label>节点输入 JSON</label>
              <el-input v-model="selectedNode.data.aiInputJson" :rows="3" placeholder="{}" size="small" type="textarea"/>
            </div>
          </template>

          <el-button size="small" text type="danger" @click="deleteSelectedNode">删除此节点</el-button>
        </template>
        <template v-else>
          <div class="panel-title">操作提示</div>
          <ul class="tips">
            <li>左侧节点拖到画布即可创建</li>
            <li>拖动节点调整位置，位置会随保存写入 coordinate</li>
            <li>从节点右侧圆点拖到下一节点左侧圆点，建立流转</li>
            <li>点击节点在右侧改名称/审批人/签核方式/AI 配置</li>
            <li>点击连线改流转类型与条件表达式</li>
            <li>草稿必须发布后才会被启动或意图识别命中</li>
          </ul>
        </template>
      </div>
    </div>

    <!-- 连线属性 -->
    <el-dialog v-model="edgeDialogVisible" title="流转属性" width="460px">
      <div class="form-row">
        <label>起点 → 目标</label>
        <code class="code-block">{{ editingEdge.source }} → {{ editingEdge.target }}</code>
      </div>
      <div class="form-row">
        <label>流转类型</label>
        <el-radio-group v-model="editingEdge.skipType" size="small">
          <el-radio-button value="PASS">正常流转</el-radio-button>
          <el-radio-button value="COND">条件流转</el-radio-button>
          <el-radio-button value="REJECT">驳回</el-radio-button>
        </el-radio-group>
      </div>
      <div v-if="editingEdge.skipType === 'COND'" class="form-row">
        <label>条件表达式</label>
        <el-input v-model="editingEdge.conditionExpr" placeholder="ROUTE.flowCode == 'ANSWER_REPORT'；兜底写 DEFAULT"
                  size="small"/>
      </div>
      <template #footer>
        <el-button size="small" text type="danger" @click="deleteEditingEdge">删除连线</el-button>
        <el-button size="small" @click="edgeDialogVisible = false">取消</el-button>
        <el-button size="small" type="primary" @click="applyEdgeEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="js" setup>
import {markRaw, nextTick, onMounted, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {VueFlow, useVueFlow} from '@vue-flow/core'
import {Background} from '@vue-flow/background'
import {Controls} from '@vue-flow/controls'
import {MiniMap} from '@vue-flow/minimap'
import {ArrowLeft, MagicStick} from '@element-plus/icons-vue'
import {ElMessage} from 'element-plus'
import WorkflowNode from '@/views/workflow/WorkflowNode.vue'
import {flowDefinitionDetail, flowDefinitionListPage, flowDefinitionPublish, flowDefinitionUpsert} from '@/api/workflow'

import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import '@vue-flow/controls/dist/style.css'
import '@vue-flow/minimap/dist/style.css'

const nodeTypes = {
  start: markRaw(WorkflowNode),
  approval: markRaw(WorkflowNode),
  auto: markRaw(WorkflowNode),
  gateway: markRaw(WorkflowNode),
  end: markRaw(WorkflowNode),
}

const {screenToFlowCoordinate, fitView} = useVueFlow()

const palette = [
  {key: 'start', label: '开始', icon: '●', color: '#0E9488', nodeType: 0, hint: '唯一入口', flowType: 'start'},
  {key: 'approval', label: '审批节点', icon: '✎', color: '#1269B5', nodeType: 1, hint: '人工闸', flowType: 'approval'},
  {key: 'auto', label: 'AI 自动节点', icon: '✦', color: '#0E9488', nodeType: 3, hint: '调能力/出结果', flowType: 'auto'},
  {key: 'gateway', label: '意图识别/网关', icon: '◆', color: '#B45309', nodeType: 4, hint: '识别→分支', flowType: 'gateway'},
  {key: 'end', label: '结束', icon: '○', color: '#64748b', nodeType: 2, hint: '终态', flowType: 'end'},
]

const FLOW_TYPE_OF = {0: 'start', 1: 'approval', 2: 'end', 3: 'auto', 4: 'gateway'}

const route = useRoute()
const router = useRouter()

const form = ref({id: null, flowCode: '', flowName: '', flowDesc: ''})
const isPublished = ref(false)
const nodes = ref([])
const edges = ref([])
const selectedNode = ref(null)
const loading = ref(false)
const saving = ref(false)
const publishing = ref(false)

const edgeDialogVisible = ref(false)
const editingEdge = ref({})

let draggedItem = null

function onFlowError(err) {
  console.error('[VueFlow]', err)
}

function onDragStart(e, item) {
  draggedItem = item
  e.dataTransfer.effectAllowed = 'move'
  e.dataTransfer.setData('text/plain', item.key)
}

function nextNodeCode(nodeType) {
  if (nodeType === 0) return 'START'
  if (nodeType === 2) return 'END'
  const used = new Set(nodes.value.map(n => n.id))
  let i = 1
  let code = `N${i}`
  while (used.has(code)) {
    i += 1
    code = `N${i}`
  }
  return code
}

function onDrop(e) {
  if (!draggedItem) return
  const position = screenToFlowCoordinate({x: e.clientX, y: e.clientY})
  const nodeType = draggedItem.nodeType
  const code = nextNodeCode(nodeType)
  nodes.value = [...nodes.value, {
    id: code,
    type: FLOW_TYPE_OF[nodeType],
    position,
    data: {
      label: draggedItem.label,
      nodeType,
      permissionFlag: '',
      nodeRatio: '0',
      aiAction: nodeType === 4 ? 'ROUTE' : '',
      aiTemplateName: nodeType === 4 ? 'flow-intent-route' : '',
      aiCapabilityKey: '',
      aiInputJson: '',
    },
  }]
  draggedItem = null
}

function onNodeClick({node}) {
  selectedNode.value = node
}

function onPaneClick() {
  selectedNode.value = null
}

function onConnect({source, target}) {
  if (source === target) {
    ElMessage.warning('不能连到自身')
    return
  }
  if (edges.value.some(e => e.source === source && e.target === target)) {
    ElMessage.warning('这条流转已存在')
    return
  }
  edges.value.push({
    id: `e-${source}-${target}-${Date.now()}`,
    source,
    target,
    label: '正常流转',
    skipType: 'PASS',
    conditionExpr: '',
  })
}

function onEdgeClick({edge}) {
  editingEdge.value = {...edge}
  edgeDialogVisible.value = true
}

function applyEdgeEdit() {
  const target = edges.value.find(e => e.id === editingEdge.value.id)
  if (!target) {
    edgeDialogVisible.value = false
    return
  }
  target.skipType = editingEdge.value.skipType
  target.conditionExpr = editingEdge.value.skipType === 'COND' ? (editingEdge.value.conditionExpr || '') : ''
  target.label = edgeLabel(target)
  edgeDialogVisible.value = false
}

function edgeLabel(edge) {
  if (edge.skipType === 'COND') return edge.conditionExpr === 'DEFAULT' ? '兜底' : (edge.conditionExpr || '条件')
  if (edge.skipType === 'REJECT') return '驳回'
  return '正常流转'
}

function deleteEditingEdge() {
  edges.value = edges.value.filter(e => e.id !== editingEdge.value.id)
  edgeDialogVisible.value = false
}

function deleteSelectedNode() {
  const id = selectedNode.value?.id
  if (!id) return
  nodes.value = nodes.value.filter(n => n.id !== id)
  edges.value = edges.value.filter(e => e.source !== id && e.target !== id)
  selectedNode.value = null
}

// ---------- 载入 ----------
function parseCoordinate(text) {
  if (!text) return null
  const [x, y] = String(text).split('|')[0].split(',')
  const nx = Number(x)
  const ny = Number(y)
  if (Number.isNaN(nx) || Number.isNaN(ny)) return null
  return {x: nx, y: ny}
}

async function loadDefinition(id) {
  loading.value = true
  try {
    const res = await flowDefinitionDetail(id)
    if (res.code !== 200 || !res.data) {
      ElMessage.error('流程定义不存在')
      backToList()
      return
    }
    const detail = res.data
    form.value = {
      id: String(detail.id),
      flowCode: detail.flowCode,
      flowName: detail.flowName,
      flowDesc: detail.flowDesc || '',
    }
    isPublished.value = detail.status === 1
    const autoPos = {}
    const nodeList = detail.nodes || []
    const restored = nodeList.map((n, index) => {
      autoPos[n.nodeCode] = {x: 80 + index * 220, y: 120}
      return {
        id: n.nodeCode,
        type: FLOW_TYPE_OF[n.nodeType] || 'approval',
        position: parseCoordinate(n.coordinate) || autoPos[n.nodeCode],
        data: {
          label: n.nodeName,
          nodeType: n.nodeType,
          permissionFlag: n.permissionFlag || '',
          nodeRatio: n.nodeRatio || '0',
          aiAction: n.aiAction || '',
          aiTemplateName: n.aiTemplateName || '',
          aiCapabilityKey: n.aiCapabilityKey || '',
          aiInputJson: n.aiInputJson || '',
        },
      }
    })
    nodes.value = restored
    // 先让节点进入画布注册，再挂流转线：先挂边会报 "Edge source or target is missing"
    await nextTick()
    edges.value = (detail.skips || []).map(s => {
      const edge = {
        id: `e-${s.nowNodeCode}-${s.nextNodeCode}-${s.skipType || 'PASS'}`,
        source: s.nowNodeCode,
        target: s.nextNodeCode,
        skipType: s.skipType || 'PASS',
        conditionExpr: s.conditionExpr || '',
      }
      edge.label = edgeLabel(edge)
      return edge
    })
  } finally {
    loading.value = false
  }
}

function resetBlank() {
  form.value = {id: null, flowCode: '', flowName: '', flowDesc: ''}
  isPublished.value = false
  selectedNode.value = null
  nodes.value = [
    {
      id: 'START', type: 'start', position: {x: 120, y: 200},
      data: {label: '开始', nodeType: 0, permissionFlag: '', nodeRatio: '0', aiAction: '', aiTemplateName: '', aiCapabilityKey: '', aiInputJson: ''},
    },
    {
      id: 'END', type: 'end', position: {x: 620, y: 200},
      data: {label: '结束', nodeType: 2, permissionFlag: '', nodeRatio: '0', aiAction: '', aiTemplateName: '', aiCapabilityKey: '', aiInputJson: ''},
    },
  ]
  // 同上：节点先注册，再挂边
  return nextTick().then(() => {
    edges.value = [{id: 'e-START-END-PASS', source: 'START', target: 'END', skipType: 'PASS', label: '正常流转', conditionExpr: ''}]
  })
}

async function setupFromRoute() {
  const id = route.params.id
  if (id && id !== 'undefined') {
    await loadDefinition(id)
  } else {
    await resetBlank()
  }
}

onMounted(setupFromRoute)

function backToList() {
  router.push('/workflow/definition')
}

// ---------- 一键美化 ----------
function autoLayout() {
  if (nodes.value.length < 2) return
  const outgoing = new Map()
  edges.value.forEach(e => {
    if (!outgoing.has(e.source)) outgoing.set(e.source, [])
    outgoing.get(e.source).push(e.target)
  })
  const incoming = new Set(edges.value.map(e => e.target))
  const depth = new Map()
  let frontier = nodes.value
      .filter(n => n.data.nodeType === 0 || !incoming.has(n.id))
      .map(n => {
        depth.set(n.id, 0)
        return n.id
      })
  if (!frontier.length) {
    frontier = [nodes.value[0].id]
    depth.set(nodes.value[0].id, 0)
  }
  while (frontier.length) {
    const id = frontier.shift()
    const d = depth.get(id) || 0
    ;(outgoing.get(id) || []).forEach(t => {
      if (depth.has(t)) return
      depth.set(t, d + 1)
      frontier.push(t)
    })
  }
  const columns = new Map()
  nodes.value.forEach(n => {
    const d = depth.has(n.id) ? depth.get(n.id) : 0
    if (!columns.has(d)) columns.set(d, [])
    columns.get(d).push(n)
  })
  let x = 60
  ;[...columns.keys()].sort((a, b) => a - b).forEach(d => {
    const column = columns.get(d).sort((a, b) => a.position.y - b.position.y)
    let y = 60
    column.forEach(n => {
      n.position = {x, y}
      y += (n.dimensions?.height || 88) + 48
    })
    x += Math.max(...column.map(n => n.dimensions?.width || 180)) + 76
  })
  nextTick(() => fitView({padding: 0.2}))
}

// ---------- 保存 ----------
function validate() {
  if (!form.value.flowCode || !form.value.flowName) {
    ElMessage.warning('流程编码与名称都要填')
    return false
  }
  if (!nodes.value.length) {
    ElMessage.warning('至少需要一个节点')
    return false
  }
  if (!nodes.value.some(n => n.data.nodeType === 0)) {
    ElMessage.warning('缺少开始节点')
    return false
  }
  if (!nodes.value.some(n => n.data.nodeType === 2)) {
    ElMessage.warning('缺少结束节点')
    return false
  }
  return true
}

function toPayload() {
  return {
    id: form.value.id ? String(form.value.id) : null,
    flowCode: form.value.flowCode,
    flowName: form.value.flowName,
    flowDesc: form.value.flowDesc,
    nodes: nodes.value.map((n, index) => ({
      nodeCode: n.id,
      nodeName: n.data.label,
      nodeType: n.data.nodeType,
      permissionFlag: n.data.permissionFlag || null,
      nodeRatio: n.data.nodeRatio || '0',
      aiAction: n.data.aiAction || null,
      aiCapabilityKey: n.data.aiCapabilityKey || null,
      aiTemplateName: n.data.aiTemplateName || null,
      aiInputJson: n.data.aiInputJson || null,
      coordinate: `${Math.round(n.position.x)},${Math.round(n.position.y)}`,
      sortNo: index + 1,
    })),
    skips: edges.value.map((e, index) => ({
      nowNodeCode: e.source,
      nextNodeCode: e.target,
      skipType: e.skipType || 'PASS',
      conditionExpr: e.skipType === 'COND' ? (e.conditionExpr || 'DEFAULT') : null,
      sortNo: index + 1,
    })),
  }
}

async function persist() {
  const wasNew = !form.value.id
  const res = await flowDefinitionUpsert(toPayload())
  if (res.code !== 200) return false
  if (wasNew) {
    // upsert 出参是裸 Long，19 位雪花 id 过 JSON 会丢精度（1888942329123456789 → ...789000），
    // 不能拿来当主键二次提交。改从 listPage 回查字符串 id（VO 侧已 ToStringSerializer）
    const listed = await flowDefinitionListPage({keyword: form.value.flowCode, pageNum: 1, pageSize: 10})
    const row = (listed.data?.records || []).find(r => r.flowCode === form.value.flowCode)
    if (!row?.id) throw new Error('保存成功但回查不到流程 ID')
    form.value.id = String(row.id)
    router.replace({name: 'WorkflowDesigner', params: {id: form.value.id}})
  }
  return true
}

async function saveDraft() {
  if (!validate()) return
  saving.value = true
  try {
    if (await persist()) ElMessage.success('草稿已保存，发布后才生效')
  } catch (e) {
    console.error('保存流程草稿失败', e)
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

async function saveAndPublish() {
  if (!validate()) return
  publishing.value = true
  try {
    if (!await persist()) return
    const res = await flowDefinitionPublish(String(form.value.id))
    if (res.code === 200) {
      isPublished.value = true
      ElMessage.success('已发布，新启动的实例与意图识别都会用这版')
    }
  } catch (e) {
    console.error('保存并发布失败', e)
    ElMessage.error(e?.message || '发布失败')
  } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.wf-designer {
  height: calc(100vh - 64px);
  display: flex;
  flex-direction: column;
  background: #f8fafc;
}

.wf-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.wf-body {
  display: flex;
  flex: 1;
  min-height: 0;
}

.wf-panel {
  width: 250px;
  padding: 12px;
  background: #fff;
  border-right: 1px solid #e2e8f0;
  overflow-y: auto;
  flex-shrink: 0;
}

.wf-panel-right {
  border-right: none;
  border-left: 1px solid #e2e8f0;
}

.panel-title {
  margin-bottom: 10px;
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
}

.palette-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  margin-bottom: 6px;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  cursor: grab;
  font-size: 13px;
  user-select: none;
  transition: all .15s;
}

.palette-item:hover {
  border-color: #1269B5;
  background: #f1f5f9;
}

.palette-icon {
  width: 16px;
  text-align: center;
}

.palette-label {
  flex-shrink: 0;
}

.palette-hint {
  margin-left: auto;
  font-size: 11px;
  color: #94a3b8;
}

.wf-canvas {
  position: relative;
  flex: 1;
  min-width: 0;
}

.wf-drop-zone {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.wf-drop-zone :deep(.vue-flow) {
  width: 100%;
  height: 100%;
}

.canvas-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 14px;
  pointer-events: none;
}

.form-row {
  margin-bottom: 12px;
}

.form-row label {
  display: block;
  margin-bottom: 4px;
  font-size: 12px;
  color: #64748b;
}

.form-hint {
  margin-top: 2px;
  font-size: 11px;
  color: #94a3b8;
}

.code-block {
  display: inline-block;
  padding: 2px 6px;
  background: #f1f5f9;
  border-radius: 4px;
  font-size: 12px;
  color: #0ea5e9;
}

.tips {
  padding-left: 18px;
  color: #64748b;
  font-size: 12px;
  line-height: 1.9;
}

.tips code {
  padding: 1px 4px;
  background: #f1f5f9;
  border-radius: 3px;
}
</style>
