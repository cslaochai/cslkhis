<script setup lang="js">
import {ref, computed, onMounted, onUnmounted, reactive, watch} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage, ElMessageBox, ElNotification} from 'element-plus'
import {
  Bell,
  User,
  ArrowDown,
  UserFilled,
  Lock,
  SwitchButton,
  Calendar,
  Setting,
  Switch,
  HomeFilled,
  ArrowRight,
  Expand,
  Fold,
  Close
} from '@element-plus/icons-vue'
// 菜单图标按 sys_menu.icon 的组件名动态解析，必须拿整个命名空间（与 Sidebar 同一口径）
import * as ElIcons from '@element-plus/icons-vue'
import {
  getUserInfo,
  changePassword,
  getPostList,
  switchPost,
  getUnreadCount,
  getMessageList,
  readMessage,
  getSelfProfile
} from '@/api/system'
import {openMessageStream} from '@/api/messageStream'
import {loadDictDataMap} from '@/lib/dict-cache'
import {loadMenuTree, findMenuByPath} from '@/lib/menu-cache'
import {loadWorkbenchConfig} from '@/lib/workbench-config'
import {
  sidebarCollapsed,
  toggleSidebar
} from '@/lib/sidebarState'
import {setPermList} from '@/lib/perm'
import {clearSessionCaches} from '@/lib/session-cache'
import {loadPermissions, hasAnyPermission} from '@/lib/permission'
// 站内信类型/紧急度/权限口径单点在 messageCatalog（消息工作台）
import {
  messageActionOwner,
  messageActionPermissions,
  messageHandleStatusMeta,
  messageLabel,
  messagePayloadChips,
  messageTagClass,
  messageSeverityRank,
} from '@/lib/messageCatalog'
// 消息处理弹窗：与消息中心页共用同一份实现（报告详情 + 危急值闭环），不得在此重写
import MessageProcessDialog from '@/components/his/MessageProcessDialog.vue'
import {resolveWorkspacePath, resolveLandingPath} from '@/lib/role-workspace'
import {isInMyTodayQueue} from '@/lib/todayQueue'
import {patientGenderSymbol} from '@/lib/patientGender'
import PatientSelect from './PatientSelect.vue'
// 患者详情弹框走通用组件（Header / 患者管理页 / 医生站共用同一份实现与字段口径）
import PatientDetailDialog from './PatientDetailDialog.vue'
import {useRoute} from 'vue-router'
import {useCurrentPatientStore} from '@/stores/currentPatient'

const router = useRouter()
const route = useRoute()
// 全局「当前患者」：患者工作区页面（meta.patientWorkspace）选中患者 = 切换工作对象
const currentPatientStore = useCurrentPatientStore()

// ========== 面包屑：从 sys_menu 菜单树反查 ==========
// 菜单的唯一口径是后端 sys_menu 表（见 source/back_end/sql/52-菜单表重建.sql），
// 这里不再维护第二份「路由 → 面包屑」映射，避免菜单改了、映射忘了改导致面包屑消失。
// 规则：首页 › 一级目录 › 二级菜单；页面没挂菜单时退化为「首页 › 路由 meta.title」。

const menuTree = ref([])

const loadBreadcrumbMenus = async () => {
  try {
    // 与侧边栏共用同一份缓存，不会额外多发请求
    menuTree.value = await loadMenuTree()
  } catch (e) {
    console.error('面包屑菜单加载失败：', e)
  }
}

// 菜单图标名（sys_menu.icon）→ 组件。与 Sidebar 同一口径：认不出来就退成 Document，
// 不留空位（面包屑少一个图标看起来像渲染坏了）。
// 注意：只在模板里现调现取，不要把组件塞进 computed/ref，否则 Vue 会警告组件被响应式代理。
const MENU_ICON_MAP = ElIcons

const resolveMenuIcon = (name) => MENU_ICON_MAP[name] || MENU_ICON_MAP.Document

// 计算面包屑
const breadcrumbs = computed(() => {
  const route = router.currentRoute.value
  const home = {label: '首页', path: '/', clickable: true}

  // 首页只显示「首页」（按路径判，不认路由 name —— 换首页组件时不该悄悄丢掉这条规则）
  if (route.path === '/') {
    return [home]
  }

  const hit = findMenuByPath(menuTree.value, route.path)
  if (hit) {
    return [
      home,
      // 一级目录只是分类，本身没有页面，不给跳转链接
      {label: hit.directory.menuName, icon: hit.directory.icon, path: '', clickable: false},
      {label: hit.menu.menuName, icon: hit.menu.icon, path: hit.menu.path, clickable: false},
    ]
  }

  // 未挂菜单的页面（如已下线的假壳页）：用路由 meta.title 兜住，不留空白
  const title = route.meta?.title
  return title ? [home, {label: title, path: route.path, clickable: false}] : [home]
})

const userInfo = ref({
  userId: '',
  username: '',
  realName: '',
  currentRole: '',
  deptId: null,
  deptName: '',
})

// 角色列表（编码来自 /auth/info，名称按 sys_role 实时解析）
const userRoleCodes = ref([])
const userRoles = computed(() => userRoleCodes.value.map(code => ({code, name: roleNameOf(code)})))

const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 通知相关
const unreadCount = ref(0)
const messageList = ref([])
const messageLoading = ref(false)
const messageDrawerVisible = ref(false)
// 待处理数：抽屉头部的待办徽标（handle_status=0，sql/70 口径；通知型为 NULL 不计入）
const pendingCount = computed(() => messageList.value.filter(m => m.handleStatus === 0).length)
// 处理弹窗（共用 MessageProcessDialog）：危急值可在抽屉里直接完成确认接收/处置闭环
const processVisible = ref(false)
const processTarget = ref(null)

// 实时时钟
const currentTime = ref('')
const currentWeekday = ref('')
const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
let clockTimer = null
let messagePollTimer = null
// SSE 关闭函数（openMessageStream 返回）；null = 未连接或已降级轮询
let closeMessageStream = null

// 患者搜索（PatientSelect 负责选人，选中后按「当前页面 + 当前角色」分流）
const showPatientDetail = ref(false)
const selectedPatient = ref(null)
const selectedPatientId = ref('')
const patientSelectRef = ref(null)

/**
 * 切换身份（角色 / 科室）后必须丢弃的两样东西：
 *
 * ① **当前患者**——接诊对象归属「人 + 岗位 + 科室」三要素，上一位患者既不在新岗位的
 *    业务范围里（收费员不该接着看医生的接诊对象），条子留着就是张冠李戴；
 * ② **下拉里已搜出的结果**——结果的置顶与「今日就诊 / 全院档案」分组是后端按当前角色
 *    算的，留着旧结果 = 还是上一个岗位的口径，看起来就像「切了没变」。
 *
 * clearSession 连 sessionStorage 一起清（该 store 有会话级持久化），否则刷新页面后
 * store.restore() 又会把这位患者恢复出来。
 * 调用方（切角色 / 切科室 / 登出）随后都会重建身份上下文，见 {@link landOnRoleWorkspace}。
 */
const resetPatientIdentityState = () => {
  currentPatientStore.clearSession()
  selectedPatient.value = null
  selectedPatientId.value = ''
  showPatientDetail.value = false
  patientSelectRef.value?.reset()
}

/**
 * 切换角色 / 科室后的落点 —— 「旧页面必须下岗」的唯一执行处
 *
 * 为什么不能用 `window.location.reload()`（原实现）：
 *   reload 换掉的是**进程**，不是**路由** —— 它回到**同一个 URL**。
 *   于是新角色没有权限的那一页被原样恢复：药剂师登进「角色管理」照样看到角色列表、
 *   照样刷数据（服务端当前无 `@PreAuthorize`，请求 200，页面看起来完全"正常"）。
 *   同时重载期间侧栏先渲染「菜单加载中…」，用户看到的是空菜单 ——
 *   所以现象被描述成「菜单没换，得手工刷新一下」。
 *
 * 这里改成：**清缓存 → 重新拉菜单 → 跳到新角色该待的页面**。
 *   · 清缓存：`clearSessionCaches` 一处清齐菜单树/工作台配置/角色权限码/按钮权限集
 *     （进程内缓存不清的话，Sidebar/面包屑/permission 下次仍拿旧角色那份）。
 *   · 跳转：落地页由 `resolveLandingPath` 从**新角色的菜单**推导（有工作台去工作台，
 *     没有回首页）。与路由守卫用的是**同一套推导**，不会出现「切换跳到 A、守卫认为该在 B」
 *     的来回弹。
 *   · 用 `router.replace` 而非 push：切角色不是"用户的浏览历史"，回退键应该回到上一步操作
 *     而不是"上一个角色看过的页面"。
 */
const landOnRoleWorkspace = async () => {
  clearSessionCaches()
  // 菜单必须**强制重取**：缓存刚被清掉，但 Header 自己的菜单树 ref 还存着旧的那份
  let tree = []
  try {
    tree = await loadMenuTree(true)
    menuTree.value = tree
  } catch (e) {
    console.error('切换角色后重新加载菜单失败：', e)
    // 拉不到菜单也必须离开当前页 —— 停在旧岗位的页面上比回首页更危险
    menuTree.value = []
  }
  // 落点策略（landing_scope）也要在新 token 下重取，且**必须在决策落点之前 await 完**：
  // 先跳 '/' 再改跳工作站会让人看见一次闪跳。拉不到就按 0（= 旧行为）走。
  let landingScope = 0
  try {
    landingScope = (await loadWorkbenchConfig()).landingScope
  } catch (e) {
    console.error('切换角色后重新加载工作台配置失败：', e)
  }
  const landing = resolveLandingPath(router, tree, userInfo.value.currentRole, landingScope)
  // 落地页与当前路径相同（如医生切医生）则不动，避免无谓的重导航
  if (route.path !== landing) {
    await router.replace(landing)
  }
}

/**
 * 顶部搜索选中患者后去哪 —— 真实 HIS 里这一步是「定当前患者」，不是「查一下」：
 *   ① 当前页就是患者工作区 → 就地切换，能不能接由工作区自己校验；
 *   ② 不是工作区、但当前角色**有**工作台、**且这人今天确实排给了我** → 带患者跳过去接着干活
 *      （医生在报表页搜到自己的患者，期望的是直接去接诊，而不是看个只读弹框）；
 *   ③ 其他情况（没有工作台 / 不是我的今日患者）→ 弹详情框查档。
 * 工作台从当前角色可见菜单里推导（lib/role-workspace），角色增减不用改这里。
 *
 * ② 里那个「今天确实排给了我」是必须的，不是严谨性洁癖：医生站首次加载会自动选中
 * 队列里**正在就诊**的患者，而开单全挂「当前患者」。不校验就带过去 = 医生搜 A、
 * 页面选中了 C，看错人就是开错单（见 lib/todayQueue.js）。
 */
const handlePatientSelect = async (patient) => {
  if (!patient?.id) return

  // ① 就地切换
  if (route.meta.patientWorkspace) {
    currentPatientStore.setPatient(patient)
    return
  }

  // ② 找当前角色的工作台。菜单拉不到就当没有工作台，直接查档
  //（不因为菜单故障挡住选人，也不假装能接诊）
  let workspace = ''
  try {
    const menuTree = await loadMenuTree()
    workspace = resolveWorkspacePath(router, menuTree, userInfo.value.currentRole)
  } catch (error) {
    workspace = ''
  }

  // ②-2 有工作台，还要确认这人在我今日队列里才带过去
  let queueCheckFailed = false
  if (workspace && workspace !== route.path) {
    let admitted = false
    try {
      admitted = await isInMyTodayQueue(patient.id)
    } catch (error) {
      // 队列查不到时**不跳**：跳过去医生站会误选队列里就诊中的别人。
      // 宁可这次只让医生看档案（并明确告诉他没确认成功），也不能赌。
      queueCheckFailed = true
    }
    if (admitted) {
      currentPatientStore.setPatient(patient)
      // 带 patientId 是为了刷新后还能回到这个患者（store 也有会话级持久化，双保险）
      router.push({path: workspace, query: {patientId: String(patient.id)}})
      return
    }
  }

  // ③ 查档
  selectedPatient.value = patient
  selectedPatientId.value = String(patient.id)
  showPatientDetail.value = true
  if (queueCheckFailed) {
    ElMessage.warning('未能确认该患者是否在您今日的候诊队列（队列查询失败），已改为展示患者档案')
  }
}

// 工作台判定「这个人不在我今天的业务里」时，通过 store 把患者交回详情框（见 currentPatient.requestDetail）
watch(() => currentPatientStore.detailRequest, (req) => {
  if (!req?.patientId) return
  const picked = currentPatientStore.patient
  selectedPatient.value = picked && String(picked.id) === req.patientId ? picked : null
  selectedPatientId.value = req.patientId
  showPatientDetail.value = true
})

/* ---------- 当前患者常驻条 ---------- */
const currentPatient = computed(() => currentPatientStore.patient)

// 性别 + 年龄（防错三要素里的两个，姓名在旁边）；患者号放 title 悬停可见，
// 完整身份信息点条子看档案 —— 顶栏空间有限，塞患者号会把搜索框挤没
const currentPatientMeta = computed(() => {
  const p = currentPatient.value
  if (!p) return ''
  const parts = []
  // 只在码值 1/2 时显示符号：patientGenderSymbol 对「未知/脏码值(0)/字段没带」都返回 '?'，
  // 而医生站队列行本身不带 gender —— 条子上挂个孤零零的「?」看着像页面坏了。
  // 这里宁可不显示（未知 ≠ 显示成一个问号），要查证性别就点开档案。
  const g = patientGenderSymbol(p.gender)
  if (g === '♂' || g === '♀') parts.push(g)
  const age = p.age ?? p.patientAge
  if (age !== null && age !== undefined && age !== '') parts.push(`${age}岁`)
  return parts.join(' ')
})

// 过敏史只有「确实有」才亮红标：没有该字段（如医生站队列行）时不显示，不假装「无过敏」
const currentPatientAllergy = computed(() => {
  const a = currentPatient.value?.allergyHistory
  return !!a && String(a).trim() !== '' && String(a).trim() !== '无'
})

const openCurrentPatientDetail = () => {
  const p = currentPatient.value
  if (!p?.id) return
  selectedPatient.value = p
  selectedPatientId.value = String(p.id)
  showPatientDetail.value = true
}

const clearCurrentPatient = () => {
  if (!currentPatient.value) return
  currentPatientStore.clear()
  ElMessage.success('已清除当前患者')
}

const updateClock = () => {
  const now = new Date()
  const y = now.getFullYear()
  const mon = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const h = String(now.getHours()).padStart(2, '0')
  const m = String(now.getMinutes()).padStart(2, '0')
  const s = String(now.getSeconds()).padStart(2, '0')
  currentTime.value = `${y}-${mon}-${d} ${h}:${m}:${s}`
  currentWeekday.value = dayNames[now.getDay()]
}

const userDeptName = computed(() => userInfo.value.deptName || '')

// 角色编码到名称：由 /auth/info 一次性带出（只要求登录），不再请求全院角色字典 ——
// 那是管理岗接口，非管理岗进首页就会被拦成 403。
// roleNameMap 只在拿不到时兜底；字典里没有的码值渲染成「未知(n)」，不静显示成某个看起来合法的角色名。
const roleDict = ref([])
const roleNameMap = {
  '10012': '系统管理员',
  '10013': '医生',
  '10014': '护士',
  '10016': '药剂师',
  '10011': '其他',
}

const roleNameOf = (code) => {
  if (!code) return ''
  const hit = roleDict.value.find(r => String(r.roleCode) === String(code))
  if (hit) return hit.roleName
  return roleNameMap[code] || `未知(${code})`
}

onMounted(() => {
  loadUserInfo()
  loadUnreadCount()
  // 消息「处理」入口按当前角色权限锁（hasAnyPermission），首屏就位避免按钮翻转闪烁
  loadPermissions()
  loadBreadcrumbMenus()
  updateClock()
  clockTimer = setInterval(updateClock, 1000)
  connectMessageStream()
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
  if (messagePollTimer) clearInterval(messagePollTimer)
  if (closeMessageStream) closeMessageStream()
})

/**
 * 消息实时通道：SSE 推送优先（新消息秒达），断线自动退回 5 秒轮询。
 * SSE 用原生 fetch 流（EventSource 带不了 Authorization 头，request.js 壳也不兼容），
 * 见 api/messageStream.js。降级是静默的：用户无感，只是回到「最多延迟 5 秒」。
 */
const connectMessageStream = () => {
  closeMessageStream = openMessageStream({
    onMessage: (data) => {
      if (data.type !== 'new-message') return
      unreadCount.value = typeof data.unread === 'number' && data.unread >= 0 ? data.unread : unreadCount.value + 1
      ElNotification({
        title: messageLabel(data.bizType) || '新消息',
        message: data.title || '您有一条新消息',
        type: data.severity === 'urgent' ? 'error' : data.severity === 'warning' ? 'warning' : 'info',
        duration: data.severity === 'urgent' ? 0 : 5000,
      })
      // 抽屉开着时同步刷新列表，条目即时出现
      if (messageDrawerVisible.value) loadMessageList()
    },
    onFallback: (reason) => {
      console.warn('消息 SSE 断开，退回轮询：', reason)
      closeMessageStream = null
      if (messagePollTimer) return // 已在轮询，不重复降级
      messagePollTimer = setInterval(loadUnreadCount, 5000)
    },
  })
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    if (res.code === 200) {
      unreadCount.value = res.data || 0
    }
  } catch (error) {
    console.error('加载未读消息数量失败', error)
  }
}

const loadMessageList = async () => {
  messageLoading.value = true
  try {
    const res = await getMessageList({pageNum: 1, pageSize: 20})
    // urgent（危急值）置顶展示：工作台排序在目录口径上做，不动服务端时间序
    const records = res.data.records || []
    records.sort((a, b) =>
        messageSeverityRank(a.bizType) - messageSeverityRank(b.bizType))
    messageList.value = records
  } catch (error) {
    console.error('加载消息列表失败', error)
  } finally {
    messageLoading.value = false
  }
}

const handleOpenMessages = () => {
  messageDrawerVisible.value = true
  loadMessageList()
}

/**
 * 点消息条目 = 打开处理弹窗（顺带标已读）。
 * 此前点击只标已读，危急值等待办型消息「看过」就等于「办完」了——已读 ≠ 处理完毕。
 * 「处理」按当前角色权限锁（口径与消息中心页一致，见 lib/messageCatalog）；
 * 危急值在弹窗内完成确认接收 → 处置措施闭环，回调后刷新未读数与列表。
 */
const canProcessMessage = (msg) => hasAnyPermission(messageActionPermissions(msg?.bizType))

const handleOpenMessage = async (message) => {
  if (!canProcessMessage(message)) {
    ElMessage.warning(`该通知属于${messageActionOwner(message.bizType)}岗位，当前角色无法处理，请切换岗位后再处理`)
    return
  }
  if (message.readStatus === 0) {
    try {
      await readMessage(message.messageId)
      message.readStatus = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
  processTarget.value = message
  processVisible.value = true
}

const handleMessageProcessed = () => {
  loadUnreadCount()
  loadMessageList()
}

const loadUserInfo = async () => {
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      userInfo.value = {
        userId: res.data.userId,
        username: res.data.username,
        realName: res.data.realName,
        currentRole: res.data.currentRole || '',
        deptId: res.data.deptId,
        deptName: res.data.deptName || '',
      }
      // 获取用户角色列表（带角色名称）
      userRoleCodes.value = res.data.roles || []
      // 我自己的角色码→名称，顶栏标签与切换角色弹窗都用它（不用再请求全院角色字典）
      roleDict.value = res.data.roleNames || []
      // 当前角色权限码集合 → v-perm / hasPerm 按钮级显隐的数据源
      setPermList(res.data.permissions || [])
    } else {
      loadFromStorage()
    }
  } catch {
    loadFromStorage()
  }
}

const loadFromStorage = () => {
  userInfo.value.userId = localStorage.getItem('userId') || ''
  userInfo.value.username = localStorage.getItem('username') || ''
  userInfo.value.realName = localStorage.getItem('realName') || '管理员'
  userInfo.value.currentRole = localStorage.getItem('currentRole') || ''
  userInfo.value.deptName = localStorage.getItem('deptName') || ''
  const rolesStr = localStorage.getItem('roles')
  if (rolesStr) {
    userRoleCodes.value = JSON.parse(rolesStr)
  }
}

const displayName = computed(() => userInfo.value.realName || userInfo.value.username || '管理员')

const currentRoleLabel = computed(() => {
  if (!userInfo.value.currentRole) return '未选择角色'
  return roleNameOf(userInfo.value.currentRole)
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      handleProfile()
      break
    case 'password':
      handleChangePassword()
      break
    case 'switchPost':
      handleShowSwitchPost()
      break
    case 'logout':
      handleLogout()
      break
  }
}

/* ---------- 切换岗位（角色 × 科室，成对切换）----------
   原先是「切换角色」「切换科室」两个独立弹窗、两个独立接口，各自只查自己的表，
   于是骨科医生先切药剂师、再切药房就能拼出一个从没分配过的身份（菜单按药剂师画、
   数据按药房取）。现在只列 sys_employee_post 里真实存在的岗位，选一条整体切换。 */
const showSwitchPostDialog = ref(false)
const userPosts = ref([])
const postsLoading = ref(false)
const postKeyword = ref('')

// 科室在前、角色在后：先认「在哪个科室」再认「以什么身份」，跟管理端岗位表的列序一致
const postLabel = (post) => `${post.deptName} · ${post.roleName}`

// deptId 有 string/number 混型（雪花ID 走字符串），一律字符串比较
const isCurrentPost = (post) =>
    post.roleCode === userInfo.value.currentRole && String(post.deptId) === String(userInfo.value.deptId)

// 当前岗位置顶 + 关键字过滤：多科室多角色的账号（实测有 94 个科室的）光靠滚动翻不完
const visiblePosts = computed(() => {
  const kw = postKeyword.value.trim().toLowerCase()
  const list = kw
      ? userPosts.value.filter(p => postLabel(p).toLowerCase().includes(kw))
      : userPosts.value
  return [...list.filter(isCurrentPost), ...list.filter(p => !isCurrentPost(p))]
})

// 只渲染前 50 条：万能演示账号实测 1692 条岗位（94 科室 × 18 角色），
// 全量 v-for 会把主线程冻住几十秒 —— 一个「看一眼就切」的弹窗不该有这种代价，
// 翻不到就提示输入科室名过滤（搜索走 visiblePosts，不限量）。
const POST_RENDER_LIMIT = 50
const renderPosts = computed(() => visiblePosts.value.slice(0, POST_RENDER_LIMIT))

const handleShowSwitchPost = async () => {
  postKeyword.value = ''
  showSwitchPostDialog.value = true
  postsLoading.value = true
  try {
    const res = await getPostList()
    if (res.code === 200) {
      userPosts.value = res.data || []
    }
  } catch (error) {
    console.error('加载岗位列表失败', error)
  } finally {
    postsLoading.value = false
  }
}

const handleSwitchPost = async (post) => {
  if (isCurrentPost(post)) {
    ElMessage.info('当前已在该岗位')
    return
  }
  postsLoading.value = true
  try {
    const res = await switchPost({roleCode: post.roleCode, deptId: post.deptId})
    if (res.code === 200) {
      const data = res.data || {}
      userInfo.value.currentRole = data.currentRole || post.roleCode
      userInfo.value.deptId = data.deptId ?? post.deptId
      userInfo.value.deptName = data.deptName || post.deptName
      localStorage.setItem('currentRole', userInfo.value.currentRole)
      localStorage.setItem('deptId', String(userInfo.value.deptId))
      localStorage.setItem('deptName', userInfo.value.deptName)
      // 岗位是会话级选择，后端把角色与科室一起写进了新 token —— 必须立刻换掉旧 token，
      // 否则下一个请求仍按库里的默认身份算（页面显示新岗位、接口按旧岗位，两边各说各话），
      // 刷新后连顶栏都会变回原岗位
      if (data.token) {
        localStorage.setItem('token', data.token)
      }
      ElMessage.success(`已切换到${postLabel(post)}`)
      showSwitchPostDialog.value = false
      // 换岗位 = 换一份「我的患者」+ 换一套搜索口径，见 resetPatientIdentityState
      resetPatientIdentityState()
      // 岗位变了，页面上按科室取的那些数据（在院患者、候诊队列、排班）全部作废：
      // 清全套会话缓存（含按钮权限集）+ 回到本岗位工作台，而不是 reload 原地重来 ——
      // 否则会停在「新岗位的页面上还显示旧科室的患者」这种更隐蔽的错配
      await landOnRoleWorkspace()
    } else {
      ElMessage.error(res.message || '岗位切换失败')
    }
  } catch (error) {
    // 后端 fail-closed：越权组合（这个角色在这个科室没配过岗位）直接拒绝，原因原样弹出
    ElMessage.error(error.message || '岗位切换失败')
  } finally {
    postsLoading.value = false
  }
}

// ========== 用户信息（个人档案）==========
// 头部只显示身份概要，完整档案在点开时按需拉取：一次 selfProfile（已含岗位）+ 一次字典
const profileDialogVisible = ref(false)
const profileLoading = ref(false)
const profileData = ref(null)
const profileLoadFailed = ref(false)
const profileDicts = ref({empType: [], title: [], position: [], gender: [], userType: []})

// 码值 -> 文案：字典里没有就渲染「未知(n)」，不回落成某个看起来合法的值
const dictLabel = (list, value) => {
  if (value === null || value === undefined || value === '') return '-'
  const hit = (list || []).find(item => String(item.dictValue) === String(value))
  return hit ? hit.dictLabel : `未知(${value})`
}

const avatarText = computed(() => (displayName.value || '?').slice(0, 1))

// 岗位（角色 × 科室）：selfProfile 直接带回，不再单独请求科室列表 ——
// 分开取就会出现「角色 3 个、科室 5 个，但真正分配过的岗位只有 2 个」这种对不上的展示
const profilePosts = computed(() => profileData.value?.posts || [])

// 同上：这一栏只有 104px 高的滚动区，却要为 1692 条岗位建 1692 个节点；截断后补一条总数
const renderProfilePosts = computed(() => profilePosts.value.slice(0, POST_RENDER_LIMIT))

const profileGroups = computed(() => {
  const d = profileData.value
  if (!d) return []
  const dicts = profileDicts.value
  return [
    {
      title: '账号信息',
      items: [
        ['登录账号', d.userName],
        ['工号', d.empNo],
        ['用户类型', dictLabel(dicts.userType, d.userType)],
        ['账号状态', d.status === 1 ? '启用' : d.status === 0 ? '禁用' : `未知(${d.status})`],
        ['账号创建', d.createTime],
        ['密码更新', d.passwordUpdateTime],
      ]
    },
    {
      title: '登录信息',
      items: [
        ['登录次数', d.loginCount === null || d.loginCount === undefined ? '-' : `${d.loginCount} 次`],
        ['最后登录', d.lastLoginTime],
        ['登录 IP', d.lastLoginIp],
      ]
    },
    {
      title: '岗位信息',
      items: [
        ['真实姓名', d.realName],
        ['员工类型', dictLabel(dicts.empType, d.empType)],
        ['职称', dictLabel(dicts.title, d.title)],
        ['职位', dictLabel(dicts.position, d.position)],
        ['学历', dictLabel(dicts.education, d.education)],
        ['入职日期', d.hireDate],
        ['专业特长', d.specialty],
      ]
    },
    {
      title: '联系方式',
      items: [
        ['手机号码', d.phone],
        ['电子邮箱', d.email],
        ['性别', dictLabel(dicts.gender, d.gender)],
        ['出生日期', d.birthDate],
        ['身份证号', d.idCard],
        ['主科室', d.deptName],
      ]
    },
  ]
})

const loadProfile = async () => {
  profileLoading.value = true
  profileLoadFailed.value = false
  profileData.value = null
  try {
    const [detailRes, dictMap] = await Promise.all([
      getSelfProfile(),
      loadDictDataMap('emp_type,sys_hospital_title,hospital_position,sys_gender,his_user_type,his_education'),
    ])
    if (detailRes.code === 200 && detailRes.data) {
      profileData.value = detailRes.data
    } else {
      profileLoadFailed.value = true
    }
    if (dictMap) {
      profileDicts.value = {
        empType: dictMap.emp_type || [],
        title: dictMap.sys_hospital_title || [],
        position: dictMap.hospital_position || [],
        gender: dictMap.sys_gender || [],
        userType: dictMap.his_user_type || [],
        education: dictMap.his_education || [],
      }
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
    profileLoadFailed.value = true
  } finally {
    profileLoading.value = false
  }
}

const handleProfile = () => {
  if (!userInfo.value.userId) {
    ElMessage.warning('未获取到当前登录用户标识，请重新登录后查看')
    return
  }
  profileDialogVisible.value = true
  loadProfile()
}

const handleChangePassword = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

const validatePassword = () => {
  if (!passwordForm.oldPassword) {
    ElMessage.warning('请输入旧密码')
    return false
  }
  if (!passwordForm.newPassword) {
    ElMessage.warning('请输入新密码')
    return false
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('新密码长度不能少于6位')
    return false
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return false
  }
  return true
}

const submitChangePassword = async () => {
  if (!validatePassword()) return

  passwordLoading.value = true
  try {
    const res = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '密码修改失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    passwordLoading.value = false
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('realName')
    localStorage.removeItem('currentRole')
    localStorage.removeItem('roles')

    // 顺手丢掉会话级「当前患者」，否则退出后换个账号登录会看到上一个人的名字挂在顶部
    resetPatientIdentityState()

    ElMessage.success('已退出登录')
    await router.push('/login')
    // 菜单/权限/工作台配置的模块级缓存必须清，否则下一个账号侧边栏画的还是这个角色的菜单。
    // 但只能放在跳转之后：提前清会推进代次，驱动仍挂载的 Sidebar/工作台在 token 已删后重拉 → 401 刷屏
    clearSessionCaches()
  } catch {
    // 用户取消
  }
}
</script>

<template>
  <header
      class="sticky top-0 z-30 flex h-16 items-center justify-between px-6"
      style="background: linear-gradient(90deg, #0A4376 0%, #0F5FA0 42%, #1179B8 72%, #0E9488 100%); box-shadow: 0 2px 12px rgba(9, 60, 110, 0.28);"
  >
    <!-- 左侧：Logo + 企业名 + 菜单伸缩 + 面包屑 -->
    <div class="flex items-center gap-4">
      <!--
        品牌位 = 「回首页」入口（真实 HIS 惯例：点医院标识回主菜单，不再另配一个图标按钮）。
        Logo 常驻（收起态它是唯一的医院标识，不能一起藏掉），只把医院名收掉。
        宽度与侧栏同口径（展开 256 / 收起 64），右边框就是那条分割线 ——
        于是它始终压在侧栏的右边界上，两种状态都不错位。
        收起态额外把 header 的 24px 左内边距抵消掉（marginLeft:-24），
        让盒子从 x=0 起算并居中放 Logo —— 这样 Logo 正好落在 64px 图标条的中轴上，
        与侧栏收起后的菜单图标、底部箭头同一条竖线。
      -->
      <router-link
          to="/"
          title="返回首页"
          class="flex shrink-0 items-center gap-3 border-r border-white/30 no-underline transition-all duration-300"
          :style="sidebarCollapsed
            ? {width: '64px', marginLeft: '-24px', justifyContent: 'center'}
            : {width: '232px'}"
      >
        <!-- Logo -->
        <div class="h-8 w-8 shrink-0 overflow-hidden rounded-full bg-white">
          <img src="@/assets/sidebar_logo.png" alt="长沙市麓康医院" class="h-full w-full object-contain"/>
        </div>
        <div v-if="!sidebarCollapsed" class="flex flex-col w-40">
          <span class="text-[18px] font-semibold text-white tracking-wide">长沙市麓康医院</span>
        </div>
      </router-link>
      <!--
        菜单伸缩按钮：与 Sidebar 底部箭头共用一个开关（lib/sidebarState）。
        展开时侧栏撑到 256px 把主内容整体往右推，收起时退成 64px 图标条。
      -->
      <button
          class="shrink-0 rounded-lg p-1.5 text-white/70 transition-colors hover:bg-white/10 hover:text-white"
          :title="sidebarCollapsed ? '展开菜单' : '收起菜单'"
          @click="toggleSidebar"
      >
        <Expand v-if="sidebarCollapsed" class="h-5 w-5"/>
        <Fold v-else class="h-5 w-5"/>
      </button>
      <!-- 面包屑导航（空间紧张时最先收缩：当前患者条和搜索是开单前的确认入口，优先保住） -->
      <nav class="breadcrumb-nav flex min-w-0 items-center gap-1.5 overflow-hidden text-sm">
        <template v-for="(item, index) in breadcrumbs" :key="index">
          <el-icon v-if="index === 0" class="h-4 w-4 shrink-0 text-white/70">
            <HomeFilled/>
          </el-icon>
          <!-- 目录 / 菜单条目带自己的图标（sys_menu.icon），与侧边栏同一份图标名 -->
          <el-icon v-if="index > 0 && item.icon" class="h-3.5 w-3.5 shrink-0 text-white/70">
            <component :is="resolveMenuIcon(item.icon)"/>
          </el-icon>
          <router-link
              v-if="index < breadcrumbs.length - 1 && item.clickable"
              :to="item.path"
              class="shrink-0 whitespace-nowrap text-white/70 hover:text-white transition-colors"
          >
            {{ item.label }}
          </router-link>
          <span v-else-if="index < breadcrumbs.length - 1"
                class="shrink-0 whitespace-nowrap text-white/70">{{ item.label }}</span>
          <span v-else class="truncate whitespace-nowrap font-medium text-white">{{ item.label }}</span>
          <el-icon v-if="index < breadcrumbs.length - 1" class="h-3 w-3 shrink-0 text-white/50">
            <ArrowRight/>
          </el-icon>
        </template>
      </nav>
    </div>

    <!-- 中间：当前患者（常驻）+ 全局患者搜索 -->
    <div class="flex min-w-0 flex-1 items-center justify-center gap-3">
      <!--
        当前患者常驻条：真实 HIS 的顶部永远挂着「现在给谁看病」，因为开医嘱、开药、开检查
        之前必须确认对象，这是防开错人的最后一道提示；此前选中患者的信息只在弹框里出现一次，
        跳去别的页面（患者管理、危急值…）就再也看不到自己选的是谁。
        点主体 = 看档案，点 × = 清除当前患者。
      -->
      <div
          v-if="currentPatient"
          class="cp-chip"
          :title="'当前患者：' + currentPatient.patientName + (currentPatient.patientNo ? '（' + currentPatient.patientNo + '）' : '')"
          @click="openCurrentPatientDetail"
      >
        <span class="cp-label">当前患者</span>
        <span class="cp-name">{{ currentPatient.patientName }}</span>
        <span class="cp-meta">{{ currentPatientMeta }}</span>
        <span
            v-if="currentPatientAllergy"
            class="cp-allergy"
            :title="'过敏史：' + currentPatient.allergyHistory"
        >过敏</span>
        <button class="cp-close" title="清除当前患者" @click.stop="clearCurrentPatient">
          <Close class="h-3 w-3"/>
        </button>
      </div>

      <div class="w-full min-w-[260px] max-w-[400px]">
        <PatientSelect
            ref="patientSelectRef"
            width="100%"
            :page-size="10"
            :model-value="currentPatientStore.patientId"
            search-icon
            @select="handlePatientSelect"
            @clear="clearCurrentPatient"
        />
      </div>
    </div>

    <!-- 右侧：消息 + 姓名 + 角色 + 科室 + 时钟 + 设置 -->
    <div class="flex items-center gap-4">
      <!-- 消息 -->
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="cursor-pointer">
        <button class="rounded-lg p-2 text-white/70 transition-colors hover:bg-white/10 hover:text-white"
                @click="handleOpenMessages">
          <Bell class="h-5 w-5"/>
        </button>
      </el-badge>

      <div class="h-6 w-px bg-white/30"/>

      <!-- 用户姓名 -->
      <div class="flex items-center gap-1.5 text-sm font-medium text-white">
        <User class="h-4 w-4 text-white/70"/>
        <span>{{ displayName }}</span>
      </div>

      <!-- 用户角色 -->
      <div class="rounded bg-white/20 px-2.5 py-1 text-sm font-medium text-white">
        {{ currentRoleLabel }}
      </div>

      <!-- 所属科室 -->
      <div v-if="userDeptName" class="rounded bg-white/20 px-2.5 py-1 text-sm font-medium text-white">
        {{ userDeptName }}
      </div>

      <div class="h-6 w-px bg-white/30"/>

      <!-- 实时时钟 -->
      <div class="flex items-center gap-1.5 text-sm text-white">
        <Calendar class="h-4 w-4"/>
        <span class="font-mono font-medium">{{ currentTime }}</span>
        <span>{{ currentWeekday }}</span>
      </div>

      <div class="h-6 w-px bg-white/30"/>

      <!-- 设置 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <button class="rounded-lg p-2 text-white transition-colors hover:bg-white/20">
          <Setting class="h-5 w-5"/>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item :icon="UserFilled" command="profile">用户信息</el-dropdown-item>
            <el-dropdown-item :icon="Switch" command="switchPost">切换岗位</el-dropdown-item>
            <el-dropdown-item :icon="Lock" command="password">修改密码</el-dropdown-item>
            <el-dropdown-item divided :icon="SwitchButton" command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>

  <!-- 用户信息弹窗（个人档案） -->
  <el-dialog v-model="profileDialogVisible" title="用户信息" width="920px" top="5vh" destroy-on-close>
    <div v-loading="profileLoading" class="min-h-[280px]">
      <template v-if="profileData">
        <!-- 身份概要 -->
        <div class="mb-3 flex items-center gap-4 rounded-lg border border-slate-200 bg-slate-50 p-3">
          <div class="h-16 w-16 shrink-0 overflow-hidden rounded-full bg-blue-500 ring-2 ring-blue-100">
            <img v-if="profileData.avatar" :src="profileData.avatar" alt="头像" class="h-full w-full object-cover"/>
            <span v-else
                  class="flex h-full w-full items-center justify-center text-2xl font-bold text-white">
              {{ avatarText }}
            </span>
          </div>
          <div class="flex-1">
            <div class="flex flex-wrap items-center gap-2">
              <h3 class="text-lg font-semibold text-slate-900">{{ displayName }}</h3>
              <span class="rounded bg-blue-100 px-2 py-0.5 text-xs font-medium text-blue-600">{{
                  currentRoleLabel
                }}</span>
              <span v-if="userDeptName"
                    class="rounded bg-teal-50 px-2 py-0.5 text-xs font-medium text-teal-700">{{ userDeptName }}</span>
              <span class="rounded bg-slate-100 px-2 py-0.5 text-xs text-slate-500">
                {{ dictLabel(profileDicts.userType, profileData.userType) }}
              </span>
            </div>
            <div class="mt-1.5 flex flex-wrap items-center gap-4 text-sm text-slate-500">
              <span>账号：{{ profileData.userName || '-' }}</span>
              <span>工号：{{ profileData.empNo || '-' }}</span>
            </div>
          </div>
        </div>

        <!-- 分组明细：两列排布，把弹框压到一屏内（外层不出滚动条） -->
        <div class="grid grid-cols-2 gap-x-4 gap-y-3">
          <section v-for="group in profileGroups" :key="group.title">
            <h4 class="mb-1.5 flex items-center gap-1.5 text-sm font-semibold text-slate-700">
              <span class="h-3.5 w-1 rounded bg-blue-500"></span>{{ group.title }}
            </h4>
            <div class="grid grid-cols-2 gap-x-3 gap-y-1.5 rounded-lg border border-slate-200 p-2.5 text-sm">
              <div v-for="item in group.items" :key="item[0]" class="flex min-w-0 items-start gap-1">
                <span class="shrink-0 text-slate-400">{{ item[0] }}：</span>
                <span class="break-all font-medium text-slate-700">{{ item[1] || '-' }}</span>
              </div>
            </div>
          </section>
        </div>

        <div class="mt-3 space-y-3">
          <!-- 角色与岗位 -->
          <section>
            <h4 class="mb-2 flex items-center gap-1.5 text-sm font-semibold text-slate-700">
              <span class="h-3.5 w-1 rounded bg-blue-500"></span>角色与岗位
            </h4>
            <div class="rounded-lg border border-slate-200 px-3 text-sm">
              <div class="flex items-start gap-2 border-b border-slate-300 py-2.5">
                <span class="shrink-0 text-slate-400">角色：</span>
                <div class="flex max-h-[104px] flex-wrap gap-1.5 overflow-y-auto pr-1">
                  <span v-for="role in userRoles" :key="role.code"
                        :class="role.code === userInfo.currentRole
                          ? 'bg-blue-500 text-white'
                          : 'bg-slate-100 text-slate-600'"
                        class="rounded px-2 py-0.5 text-xs font-medium">
                    {{ role.name }}（{{ role.code }}）
                  </span>
                  <span v-if="userRoles.length === 0" class="text-slate-400">未分配角色</span>
                </div>
              </div>
              <!-- 岗位 = 角色 × 科室 的成对授权，顶栏「切换岗位」列出的就是这些 -->
              <div class="flex items-start gap-2 py-2.5">
                <span class="shrink-0 text-slate-400">岗位：</span>
                <div class="flex max-h-[104px] flex-wrap gap-1.5 overflow-y-auto pr-1">
                  <span v-for="post in renderProfilePosts" :key="post.roleCode + '-' + post.deptId"
                        :class="isCurrentPost(post)
                          ? 'bg-teal-500 text-white'
                          : 'bg-slate-100 text-slate-600'"
                        class="rounded px-2 py-0.5 text-xs font-medium">
                    {{ postLabel(post) }}<template v-if="post.isPrimary === 1">（主）</template>
                  </span>
                  <span v-if="profilePosts.length > renderProfilePosts.length"
                        class="rounded bg-slate-100 px-2 py-0.5 text-xs text-slate-400">
                    还有 {{ profilePosts.length - renderProfilePosts.length }} 条…
                  </span>
                  <span v-if="profilePosts.length === 0" class="text-slate-400">未分配岗位</span>
                </div>
              </div>
            </div>
          </section>
        </div>
      </template>

      <div v-else-if="!profileLoading" class="py-16 text-center text-sm text-slate-400">
        {{ profileLoadFailed ? '用户信息加载失败，请稍后重试' : '暂无用户信息' }}
      </div>
    </div>
    <template #footer>
      <el-button v-if="profileLoadFailed && !profileLoading" @click="loadProfile">重新加载</el-button>
      <el-button type="primary" @click="profileDialogVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <!-- 修改密码弹窗 -->
  <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
      :close-on-click-modal="false"
  >
    <el-form :model="passwordForm" label-width="80px">
      <el-form-item label="旧密码">
        <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
        />
      </el-form-item>
      <el-form-item label="新密码">
        <el-input
            v-model="passwordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
        />
      </el-form-item>
      <el-form-item label="确认密码">
        <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="passwordLoading" @click="submitChangePassword">
        确定修改
      </el-button>
    </template>
  </el-dialog>
  <el-dialog v-model="showSwitchPostDialog" title="切换岗位" width="460px" destroy-on-close>
    <el-input
        v-model="postKeyword"
        placeholder="搜索岗位（科室 / 角色）"
        clearable
        class="mb-3"
    />
    <div v-loading="postsLoading" class="max-h-[320px] space-y-2 overflow-y-auto pr-1">
      <div
          v-for="post in renderPosts"
          :key="post.roleCode + '-' + post.deptId"
          class="flex cursor-pointer items-center justify-between rounded-lg border p-3 transition-colors"
          :class="isCurrentPost(post) ? 'border-blue-300 bg-blue-50' : 'border-slate-200 hover:bg-slate-50'"
          @click="handleSwitchPost(post)"
      >
        <span class="text-sm font-medium text-slate-700">{{ postLabel(post) }}</span>
        <span class="flex items-center gap-1.5">
          <span v-if="post.isPrimary === 1"
                class="rounded bg-blue-100 px-1.5 py-0.5 text-[10px] text-blue-600">主岗位</span>
          <span v-if="isCurrentPost(post)" class="text-xs text-blue-500">当前 ✓</span>
        </span>
      </div>
      <div v-if="visiblePosts.length === 0 && !postsLoading" class="py-8 text-center text-sm text-slate-400">
        {{ userPosts.length === 0 ? '暂无分配岗位，请联系管理员在员工档案中配置' : '没有匹配的岗位' }}
      </div>
      <div v-if="visiblePosts.length > POST_RENDER_LIMIT" class="pt-1 text-center text-xs text-slate-400">
        共 {{ visiblePosts.length }} 条岗位，只列出前 {{ POST_RENDER_LIMIT }} 条，输入科室名可缩小范围
      </div>
    </div>
  </el-dialog>

  <!-- 患者详情弹框：统一走通用组件（主档详情 + CDR 全景时间轴），与患者管理页共用同一实现 -->
  <PatientDetailDialog
      v-model="showPatientDetail"
      :patient-id="selectedPatientId"
      :patient="selectedPatient"
  />

  <!-- 消息抽屉：多场景工作台入口（类型目录/紧急度/处理全部走 lib/messageCatalog 口径） -->
  <el-drawer
      v-model="messageDrawerVisible"
      title="消息通知"
      direction="rtl"
      size="400px"
  >
    <template #header>
      <div class="flex items-center justify-between">
        <span>消息通知</span>
        <span class="flex items-center gap-2 text-xs">
          <span v-if="pendingCount > 0" class="rounded bg-amber-100 px-1.5 py-0.5 text-amber-700">{{ pendingCount }} 条待处理</span>
          <span v-if="unreadCount > 0" class="text-red-500">{{ unreadCount }} 条未读</span>
        </span>
      </div>
    </template>
    <div v-loading="messageLoading">
      <div v-if="messageList.length === 0" class="py-10 text-center text-slate-400">
        暂无消息
      </div>
      <div v-else class="space-y-3">
        <div
            v-for="msg in messageList"
            :key="msg.messageId"
            class="cursor-pointer rounded-lg border p-3 transition-colors hover:bg-slate-50"
            :class="msg.readStatus === 0
              ? (messageSeverityRank(msg.bizType) === 0 ? 'border-red-300 bg-red-50' : 'bg-blue-50')
              : ''"
            @click="handleOpenMessage(msg)"
        >
          <div class="mb-1 flex items-center gap-2">
            <!-- 类型 chip：目录驱动，未知码值渲染「未知(n)」 -->
            <span :class="['rounded px-1.5 py-0.5 text-[11px] font-medium', messageTagClass(msg.bizType)]">
              {{ messageLabel(msg.bizType) }}
            </span>
            <span class="min-w-0 flex-1 truncate font-medium text-slate-800">{{ msg.title || '系统通知' }}</span>
            <!-- 待办型处置回执：看过≠办完（handle_status 口径见 sql/70） -->
            <span v-if="messageHandleStatusMeta(msg.handleStatus)"
                  :class="['rounded px-1.5 py-0.5 text-[11px] font-medium', messageHandleStatusMeta(msg.handleStatus).tagClass]">
              {{ messageHandleStatusMeta(msg.handleStatus).label }}
            </span>
            <el-tag v-if="msg.readStatus === 0" type="danger" size="small">未读</el-tag>
          </div>
          <p class="text-sm text-slate-600">{{ msg.content }}</p>
          <div v-if="messagePayloadChips(msg.payload).length" class="mt-1 flex flex-wrap gap-1">
            <span v-for="chip in messagePayloadChips(msg.payload)" :key="chip.label"
                  class="rounded bg-white/80 px-1.5 py-0.5 text-[11px] text-slate-600">
              {{ chip.label }}：{{ chip.text }}
            </span>
          </div>
          <p class="mt-1 text-xs text-slate-400">{{ msg.sendTime }}</p>
        </div>
      </div>
    </div>
  </el-drawer>

  <!-- 消息处理弹窗：与消息中心页共用（报告详情 / 危急值确认接收→处置闭环） -->
  <MessageProcessDialog
      v-model="processVisible"
      :message="processTarget"
      @processed="handleMessageProcessed"
  />
</template>

<style scoped>
/* ---------- 面包屑图标 ----------
   el-icon 的 svg 用 fill="currentColor"，一旦 text-white/70 这类的透明度写法没生效，
   就会退回继承链上的默认色（nav 上没设 color → 黑），图标直接压在渐变底上。
   这里把色值钉死在 svg 自身，并给 nav 兜一个基础色，不再依赖继承。 */
.breadcrumb-nav {
  color: #fff;
}

.breadcrumb-nav :deep(svg) {
  fill: #ffffff;
}

:deep(.el-select .el-input__wrapper) {
  border: 1.5px solid #d1d5db;
  border-radius: 8px;
  background-color: #f9fafb;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
  height: 36px;
}

:deep(.el-select .el-input__wrapper:hover) {
  border-color: #93c5fd;
}

:deep(.el-select .el-input__wrapper.is-focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.15);
}

:deep(.el-select .el-input__inner::placeholder) {
  color: #6b7280;
}

/* ---------- 当前患者常驻条（真实 HIS 顶部的「现在给谁看病」） ----------
   这一条是开医嘱/开药/开检查前的最后一道防错提示，必须一眼能看见：
   加深底色 + 亮描边 + 投影让它从渐变 header 上「浮」出来，姓名加粗放大，
   「当前患者」四个字做成白底蓝字小标签（纯文字太淡，扫过去会漏掉）。 */
.cp-chip {
  display: flex;
  align-items: center;
  gap: 7px;
  flex-shrink: 0;
  max-width: 260px;
  min-width: 0;
  padding: 6px 8px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.26);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 1px 6px rgba(6, 45, 84, 0.3);
  color: #fff;
  cursor: pointer;
  transition: background 0.15s ease, box-shadow 0.15s ease;
}

.cp-chip:hover {
  background: rgba(255, 255, 255, 0.4);
  box-shadow: 0 2px 10px rgba(6, 45, 84, 0.38);
}

.cp-label {
  flex-shrink: 0;
  padding: 2px 6px;
  border-radius: 4px;
  background: #fff;
  color: #0A4376;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.3px;
}

.cp-name {
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
  text-shadow: 0 1px 2px rgba(6, 45, 84, 0.35);
}

.cp-meta {
  min-width: 0;
  font-size: 12px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.95);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cp-allergy {
  flex-shrink: 0;
  padding: 1px 6px;
  border-radius: 4px;
  background: #FDE8E8;
  color: #C81E1E;
  font-size: 11px;
  font-weight: 600;
}

.cp-close {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.85);
  background: transparent;
  cursor: pointer;
}

.cp-close:hover {
  background: rgba(255, 255, 255, 0.32);
  color: #fff;
}
</style>
