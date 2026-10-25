<template>
  <div class="login-container">
    <!-- 顶部横幅：公司 logo + 医院名（与应用主框架 Header 同风格） -->
    <header class="login-banner">
      <div class="login-banner-logo-wrap">
        <img alt="长沙市麓康医院" class="login-banner-logo" src="../../assets/sidebar_logo.png"/>
      </div>
      <span class="login-banner-name">长沙市麓康医院</span>
    </header>

    <div class="login-card">
      <!-- 院徽 + 院名 -->
      <div class="login-header">
        <img alt="长沙市麓康医院" class="hospital-logo" src="../../assets/main_logo.png"/>
        <h1 class="hospital-name">长沙市麓康医院</h1>
        <p class="hospital-sub">CHANGSHA LUKANG HOSPITAL</p>
      </div>

      <div class="login-divider"></div>

      <!-- 登录表单 -->
      <el-form :model="loginForm" @keyup.enter="handleLogin">
        <el-form-item>
          <el-input
              v-model="loginForm.username"
              :prefix-icon="User"
              class="login-input"
              placeholder="请输入用户名/工号"
              size="large"
          />
        </el-form-item>
        <el-form-item>
          <el-input
              v-model="loginForm.password"
              :prefix-icon="Lock"
              class="login-input"
              placeholder="请输入密码"
              show-password
              size="large"
              type="password"
          />
        </el-form-item>
        <el-form-item>
          <div class="captcha-row">
            <el-input
                v-model="loginForm.captcha"
                :prefix-icon="CircleCheck"
                class="captcha-input"
                maxlength="4"
                placeholder="请输入验证码"
                size="large"
            />
            <canvas
                ref="captchaCanvas"
                class="captcha-image"
                height="40"
                title="点击刷新验证码"
                width="100"
                @click="refreshCaptcha"
            ></canvas>
          </div>
        </el-form-item>
        <!-- 登录失败常驻提示：toast 几秒就消失不够醒目，改为表单内联红条，直到下次尝试才清除 -->
        <el-form-item v-if="loginError">
          <el-alert
              :closable="false"
              :title="loginError"
              class="login-error-alert"
              show-icon
              type="error"
          />
        </el-form-item>
        <el-form-item>
          <el-button
              :loading="loading"
              class="login-button"
              size="large"
              type="primary"
              @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
        <!-- 测试账号凭据：前端写死，仅用于演示，不查后端 -->
        <el-form-item class="cred-item">
          <el-button class="cred-button" plain size="large" @click="credVisible = true">
            测试账号
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <p class="login-footer">长沙市麓康医院信息管理平台 · 仅限授权人员使用 · V1.0.0</p>

    <el-dialog
        v-model="credVisible"
        title="测试账号凭据（密码统一为 123456）"
        width="920px"
        top="6vh"
    >
      <div class="cred-body">
        <el-table :data="credRows" border size="small">
          <el-table-column label="角色" min-width="150" prop="role"/>
          <el-table-column label="科室" min-width="110" prop="dept"/>
          <el-table-column label="账号（姓名）" min-width="220">
            <template #default="{row}">
              <div v-for="item in row.accounts" :key="item" class="cred-account">
                <span class="cred-account-text">{{ item }}</span>
                <button class="copy-btn" title="复制账号" type="button" @click="handleCopyAccount(item)">
                  <CopyDocument class="copy-icon"/>
                </button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="能做什么" min-width="300" prop="ability"/>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="js" setup>
import {onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {CircleCheck, CopyDocument, Lock, User} from '@element-plus/icons-vue'
import {login} from '@/api/system'
import {encryptPassword} from '@/lib/password'
import {copyText} from '@/lib/clipboard'
import {useCurrentPatientStore} from '@/stores/currentPatient'
import {clearSessionCaches} from '@/lib/session-cache'

const router = useRouter()
const currentPatientStore = useCurrentPatientStore()

const loading = ref(false)
const loginError = ref('')

const loginForm = reactive({
  username: '',
  password: '',
  captcha: '',
})

// ===== 测试账号凭据（前端写死，仅用于演示，不查后端）=====
// 按「科室 + 角色」合并：同一科室同一角色的多个账号并到一行，账号列展示 账号(姓名)
const credVisible = ref(false)

// 三类治疗科室共用的能力描述（文档中的「同上」按岗位角色还原）
const DOCTOR_ABILITY = '门诊看诊开方、检查检验申请、住院医嘱与病历、手术排期/会诊/转科、床位与病危重通知、死亡证明填写、患者360（限本科室）'
const NURSE_ABILITY = '护士站医嘱执行与校对、入出院、转科、床位、ICU监护、出院带药、VTE防控、器械清点（限本科室，不能排班）'
const HEAD_NURSE_ABILITY = '护士全部操作 + 护理排班、门诊排班、员工排班、排班总览、护理质控、不良事件上报、死亡证明审核签发（限本科室）'

const PHARM_ABILITY = '药房发药、住院摆药、静配、药房盘点、中药代煎、麻精专册、库存/采购/入库/调拨/供应商退货、追溯码、抗菌药物处方权（限本科室）'
const MEDTECH_ABILITY = '检查/检验工作站、标本与报告、输血与血库、病理/内镜/超声/放射/心电、血液净化、检验质控与室间质评、危急值（限本科室）'
const ER_ABILITY = '急诊中心（分诊-抢救-留观）、医生工作站开方、门诊治疗、慢病建档、我的未报、危急值、患者360（限本科室）'

const SCHEDULE_PLANNER_ABILITY = '门诊排班 维护与提交（限本科室）；无审核权限（审核分离，演示「排班员排班 → 门诊办审核」链路）'
const SCHEDULE_AUDITOR_ABILITY = '排班审批中心：员工排班 + 门诊排班双页签，可审核发布 / 驳回（留痕）；门诊排班全局可见（演示审核人）'
const SCHEDULE_NURSING_AUDITOR_ABILITY = '护理排班审核发布（全院，按科室 nursing_auditor_id 收口）；护理排班维护；排班总览（演示护理二级审核「护士长排 → 护理部审」）'

// 全部账号合并为一张表：每行 = 同一科室 + 同一角色；排班流程账号按「排班员 → 审核人」流程顺序置顶
const credRows = [
  // 排班流程（推荐演示链路：排班员 → 审核人 → 全院）
  {
    accounts: ['wanghaitao ( 王海涛 )'],
    role: '科室排班员',
    dept: '心血管内科',
    ability: SCHEDULE_PLANNER_ABILITY
  },
  {
    accounts: ['liumin ( 刘敏 )'],
    role: '门诊办',
    dept: '医院信息系统',
    ability: SCHEDULE_AUDITOR_ABILITY + '；心血管内科排班审核人'
  },
  {
    accounts: ['chenhuilan ( 陈慧兰 )'],
    role: '护理部排班审核',
    dept: '护理部',
    ability: SCHEDULE_NURSING_AUDITOR_ABILITY
  },
  {
    accounts: ['tanyiming ( 谭一鸣 )'],
    role: '医务科',
    dept: '医务科',
    ability: '员工排班（全院）、人力配置标准、排班总览；无临床业务页面'
  },
  // 三个治疗科室（推荐账号）
  {
    accounts: ['moyuanhang ( 莫远航 )', 'shijiaming ( 施佳明  )', 'wangsheng ( 王升  )', 'zengjie ( 曾洁 )'],
    role: '医生',
    dept: '心血管内科',
    ability: DOCTOR_ABILITY
  },
  {
    accounts: ['guhanmei ( 顾寒梅  )', 'kangyinuo ( 康一诺  )', 'pengsiqi ( 彭思琪  )'],
    role: '护士',
    dept: '心血管内科',
    ability: NURSE_ABILITY
  },
  {
    accounts: ['caoyumei ( 曹玉梅  )', 'qiuhaotian ( 邱昊天  )'],
    role: '护士长',
    dept: '心血管内科',
    ability: HEAD_NURSE_ABILITY
  },
  {
    accounts: ['linxinyi ( 林心怡  )'],
    role: '分诊护士',
    dept: '心血管内科',
    ability: '分诊工作站：队列/呼叫/重呼/过号/复诊优先/分诊卡体征与分级/患者签到，默认诊区=心内科（限本科室）'
  },
  {
    accounts: ['chenzeyu ( 陈泽宇  )', 'fanlicheng ( 范立诚  )', 'kangyihang ( 康亦航  )', 'nitian ( 倪天  )'],
    role: '医生',
    dept: '神经内科',
    ability: DOCTOR_ABILITY
  },
  {
    accounts: ['dengxinyi ( 邓心怡  )', 'jinbailu ( 金白露  )'],
    role: '护士',
    dept: '神经内科',
    ability: NURSE_ABILITY
  },
  {accounts: ['xuheyu ( 徐和玉  )'], role: '护士长', dept: '神经内科', ability: HEAD_NURSE_ABILITY},
  {
    accounts: ['jinxin ( 金欣  )', 'limin ( 李敏  )', 'qinan ( 齐楠  )', 'shijiajie ( 石嘉杰  )'],
    role: '医生',
    dept: '肿瘤内科',
    ability: DOCTOR_ABILITY
  },
  {accounts: ['sumin ( 苏敏  )', 'zhuchunyan ( 朱春燕  )'], role: '护士', dept: '肿瘤内科', ability: NURSE_ABILITY},
  {accounts: ['wenmingzhu ( 温明珠  )'], role: '护士长', dept: '肿瘤内科', ability: HEAD_NURSE_ABILITY},
  // 其他科室
  {
    accounts: ['admin ( 超级管理员  )'],
    role: '系统管理员',
    dept: '医院信息系统',
    ability: '全部业务页面 + 系统管理（用户/角色/菜单/字典/参数/日志审计）'
  },
  {
    accounts: ['lirui ( 李瑞  )'],
    role: '系统管理员 / 医技人员',
    dept: '信息科 / 设备科',
    ability: '切「系统管理员」= 同 admin；切「医技人员」= 只做检查检验与报告'
  },
  {
    accounts: ['fengruchu ( 冯如初  )', 'yuanchenxi ( 袁晨曦  )'],
    role: '收费员收费员',
    dept: '财务科',
    ability: '窗口收费、退费申请、发票管理、财务日结、费用记账、账单与支付流水查询（全院，可跨科室结账单）、住院结算（收预交金、办出院结算——自费与医保同一入口）'
  },
  {
    accounts: ['hanlei ( 韩蕾  )'],
    role: '收费员收费员 / 医保结算员',
    dept: '医保科 / 财务科',
    ability: '切「收费员」= 收费全部 + 住院结算；切「医保结算员」加做医保结算清单、慢特病备案、目录对照、政策配置、合规审核、扣款飞检、渠道对账、资金账户、欠费管控、医保住院结算与2304/2305报盘'
  },
  {accounts: ['denghoupu ( 邓厚朴  )'], role: '药剂师', dept: '门诊中药房', ability: PHARM_ABILITY},
  {accounts: ['dongxinyi ( 董心怡  )'], role: '药剂师', dept: '住院药房', ability: PHARM_ABILITY},
  {accounts: ['jiangqian ( 江倩  )'], role: '药剂师', dept: '静脉配液中心', ability: PHARM_ABILITY},
  {accounts: ['jiangzhiyuan ( 蒋致远  )'], role: '药剂师', dept: '门诊西药房', ability: PHARM_ABILITY},
  {accounts: ['caihanmei ( 蔡寒梅  )'], role: '医技人员', dept: '消毒供应中心', ability: MEDTECH_ABILITY},
  {accounts: ['kanghui ( 康慧  )'], role: '医技人员', dept: '输血科', ability: MEDTECH_ABILITY},
  {
    accounts: ['cenjing ( 岑静  )'],
    role: '检验技师',
    dept: '医学检验科',
    ability: '检验工作站、标本管理、检验项目字典、输血管理、危急值处置（限本科室）'
  },
  {
    accounts: ['xuyan ( 许妍  )'],
    role: '前台导诊',
    dept: '院办',
    ability: '挂号预约（全院科室）、收费结算窗口、结算账单查询、患者管理、患者标签（分诊工作站与就诊总览已收回）'
  },
  {accounts: ['chengruotong ( 程若彤  )'], role: '急诊医生', dept: '急诊外科', ability: ER_ABILITY},
  {accounts: ['donghanmei ( 董寒梅  )'], role: '急诊医生', dept: '急诊内科', ability: ER_ABILITY},
  {accounts: ['helicheng ( 何立诚  )'], role: '急诊医生', dept: '急诊儿科', ability: ER_ABILITY},
  {accounts: ['jiangbailu ( 江白露  )'], role: '急诊医生', dept: '肠道门诊', ability: ER_ABILITY},
  {accounts: ['mojingwen ( 莫静文  )'], role: '急诊医生', dept: '发热门诊', ability: ER_ABILITY},
  {
    accounts: ['longyuebai ( 龙月白  )'],
    role: '护士长',
    dept: '护理部',
    ability: HEAD_NURSE_ABILITY + '；落点科室为护理部，四类排班页按护理部口径展示（非某个病区）'
  },
  {
    accounts: ['qiwei ( 齐伟  )'],
    role: '检查技师',
    dept: '放射科',
    ability: '检查工作站、检查预约中心、检查设备与号源、检查项目字典、标本管理、危急值处置（限本科室）'
  },
  {
    accounts: ['kangyao ( 康瑶  )'],
    role: '病案编码员',
    dept: '病案室',
    ability: '门诊病历查阅、患者主索引合并、健康档案与360（不做临床书写）'
  },
  {
    accounts: ['hanyuwei ( 韩雨薇  )'],
    role: '营养师',
    dept: '营养科',
    ability: '营养风险筛查、膳食医嘱执行、订餐配送、营养会诊、营养指标监测（全院）'
  },
  {
    accounts: ['gaoheyu ( 高和玉  )'],
    role: '行政人员',
    dept: '总务科',
    ability: '只有工作台与消息待办，无任何业务页面（用于岗位/权限实验）'
  },
  {
    accounts: ['longjiaming ( 龙佳明  )'],
    role: '行政人员',
    dept: '保卫科',
    ability: '只有工作台与消息待办，无任何业务页面（用于岗位/权限实验）'
  },
  {
    accounts: ['kefulina ( 李娜  )'],
    role: '客服专员',
    dept: '客户服务中心',
    ability: '患者常见问题维护、工单受理'
  },
]

// 列表文案是「账号 ( 姓名 )」，登录只需要账号本身 → 复制时取括号前的部分
const handleCopyAccount = async (item) => {
  const account = item.split('(')[0].trim()
  const ok = await copyText(account)
  if (ok) {
    ElMessage.success(`已复制账号：${account}`)
  } else {
    ElMessage.error('复制失败，请手动选择文本')
  }
}

const AUTO_FILL_CAPTCHA = true
const captchaCanvas = ref(null)
let captchaCode = ''

const generateCaptcha = () => {
  // 去掉易混淆字符 I O 0 1
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'
  captchaCode = Array.from({length: 4}, () => chars.charAt(Math.floor(Math.random() * chars.length))).join('')
  if (AUTO_FILL_CAPTCHA) {
    loginForm.captcha = captchaCode
  }
  drawCaptcha()
}

const drawCaptcha = () => {
  const canvas = captchaCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const width = canvas.width
  const height = canvas.height

  // 背景
  ctx.fillStyle = '#EAF2F9'
  ctx.fillRect(0, 0, width, height)

  // 干扰线
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = `rgba(18, 105, 181, ${0.15 + Math.random() * 0.25})`
    ctx.lineWidth = 1
    ctx.beginPath()
    ctx.moveTo(Math.random() * width, Math.random() * height)
    ctx.lineTo(Math.random() * width, Math.random() * height)
    ctx.stroke()
  }

  // 文字：随机颜色、轻微旋转错位
  const colors = ['#1269B5', '#0E9488', '#0B4F8A', '#5B8DB8']
  for (let i = 0; i < captchaCode.length; i++) {
    ctx.save()
    ctx.font = `bold ${20 + Math.floor(Math.random() * 5)}px Arial`
    ctx.fillStyle = colors[Math.floor(Math.random() * colors.length)]
    ctx.textBaseline = 'middle'
    ctx.translate(12 + i * 20, height / 2 + (Math.random() - 0.5) * 8)
    ctx.rotate((Math.random() - 0.5) * 0.5)
    ctx.fillText(captchaCode[i], 0, 0)
    ctx.restore()
  }

  // 噪点
  ctx.fillStyle = 'rgba(11, 79, 138, 0.35)'
  for (let i = 0; i < 30; i++) {
    ctx.fillRect(Math.random() * width, Math.random() * height, 1.5, 1.5)
  }
}

const refreshCaptcha = () => {
  generateCaptcha()
}

onMounted(() => {
  generateCaptcha()
})

const handleLogin = async () => {
  if (!loginForm.username) {
    ElMessage.warning('请输入用户名')
    return
  }
  if (!loginForm.password) {
    ElMessage.warning('请输入密码')
    return
  }
  if (!loginForm.captcha) {
    ElMessage.warning('请输入验证码')
    return
  }
  if (loginForm.captcha.trim().toUpperCase() !== captchaCode) {
    ElMessage.error('验证码错误')
    refreshCaptcha()
    return
  }

  loading.value = true
  loginError.value = ''
  try {
    const password = await encryptPassword(loginForm.password)
    const res = await login({
      username: loginForm.username,
      password: password,
    })

    if (res.code === 200) {
      // 保存token和用户信息
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('userId', res.data.userId)
      localStorage.setItem('username', res.data.username)
      localStorage.setItem('realName', res.data.realName)
      localStorage.setItem('currentRole', res.data.currentRole)
      // 清掉上一个会话残留的「当前患者」（sessionStorage），避免串患者
      currentPatientStore.clearSession()
      clearSessionCaches()

      ElMessage.success('登录成功')
      router.push('/')
    } else {
      loginError.value = res.message || '登录失败'
      refreshCaptcha()
    }
  } catch (error) {
    loginError.value = error.message || '登录失败，请检查用户名和密码'
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #EAF2F9 0%, #DDEAF4 100%);
  /* 顶部留出 64px 横幅的高度，避免登录卡片被横幅压住 */
  padding: 88px 24px 24px;
}

/* 顶部横幅：占满整行宽，高度与系统 Header 一致（64px，Ant Design 企业系统顶部导航标准高度）。
   底色与系统顶栏取同一个令牌——它是顶栏的镜像，各写一份必然漂移（曾各自留过一版渐变）。 */
.login-banner {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 24px;
  background: var(--his-header-bg);
  box-shadow: var(--his-chrome-shadow);
}

.login-banner-logo-wrap {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
}

.login-banner-logo {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.login-banner-name {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 2px;
  white-space: nowrap;
}

.login-card {
  width: 440px;
  background: #fff;
  border: 1px solid #E3ECF5;
  border-radius: 14px;
  box-shadow: 0 6px 24px rgba(11, 79, 138, 0.08);
  padding: 40px 40px 28px;
}

.login-header {
  text-align: center;
}

.hospital-logo {
  width: 88px;
  height: 88px;
  object-contain: contain;
  margin: 0 auto 12px;
  display: block;
}

.hospital-name {
  font-size: 24px;
  font-weight: 700;
  color: #0B4F8A;
  letter-spacing: 4px;
  margin: 0;
}

.hospital-sub {
  font-size: 12px;
  color: #8CA6BC;
  letter-spacing: 2px;
  margin: 6px 0 0;
}

.login-divider {
  height: 1px;
  background: #E9F0F7;
  margin: 24px 0 20px;
}

.login-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
  align-items: center;
}

.login-error-alert {
  width: 100%;
  font-weight: 600;
}

.captcha-input {
  flex: 1;
}

.captcha-image {
  width: 100px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #E3ECF5;
  cursor: pointer;
  flex-shrink: 0;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 6px;
  border-radius: 8px;
}

.cred-item {
  display: flex;
  justify-content: center;
  margin-top: -8px;
}

.cred-button {
  border-radius: 8px;
}

.cred-body {
  max-height: 68vh;
  overflow-y: auto;
  padding-right: 4px;
}

.cred-account {
  display: flex;
  align-items: center;
  gap: 6px;
  line-height: 1.8;
  color: #33475B;
}

.cred-account-text {
  white-space: nowrap;
}

.copy-btn {
  display: inline-flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  padding: 0;
  border: none;
  border-radius: 4px;
  background: transparent;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.15s ease;
}

.copy-btn:hover {
  color: #1269b5;
  background: #e8f1fa;
}

.copy-icon {
  width: 14px;
  height: 14px;
}

.login-footer {
  margin-top: 24px;
  font-size: 12px;
  color: #9AAFC2;
  letter-spacing: 1px;
}
</style>
