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
        width="min(1000px, 92vw)"
        top="5vh"
    >
      <div class="cred-body">
        <el-input
            v-model="credKeyword"
            class="cred-filter"
            clearable
            placeholder="按角色 / 科室 / 姓名 / 姓名首拼（如 xs=许霜）/ 账号 / 能做什么筛选（模糊匹配）"
            prefix-icon="Search"
        />
        <p class="cred-hint">
          共 {{ credAccountCount }} 个启用员工账号，按「角色 → 科室」归组；停用账号与小程序患者自助账号不在内。列表在表格内滚动，账号行可复制或一键登录
        </p>
        <el-table
            class="cred-table"
            :data="sortedCredRows"
            border
            max-height="60vh"
            size="small"
            :span-method="credSpanMethod"
        >
          <el-table-column label="角色" min-width="150" prop="role"/>
          <el-table-column label="科室" min-width="110" prop="dept"/>
          <el-table-column label="账号（姓名）" min-width="250">
            <template #default="{row}">
              <div v-for="item in row.accounts" :key="item" class="cred-account">
                <span class="cred-account-text">{{ formatAccount(item) }}</span>
                <button class="copy-btn" title="复制账号" type="button" @click="handleCopyAccount(item)">
                  <CopyDocument class="copy-icon"/>
                </button>
                <button class="quick-btn" title="用该账号直接登录（密码 123456）" type="button" @click="handleQuickLogin(item)">
                  一键登录
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
import {computed, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {ElMessage} from 'element-plus'
import {CircleCheck, CopyDocument, Lock, User} from '@element-plus/icons-vue'
import {login} from '@/api/system'
import {credAccounts} from './credAccounts'
import {encryptPassword} from '@/lib/password'
import {copyText} from '@/lib/clipboard'
import {useCurrentPatientStore} from '@/stores/currentPatient'
import {initialsOf} from '@/lib/pinyin'
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
const SCHEDULE_NURSING_AREA_AUDITOR_ABILITY = '护理排班审核发布（本科室排班须他人编制，不能自审自排）；排班总览'
const SCHEDULE_NURSING_FILING_ABILITY = '护理排班整周备案与退回（全院，按护理片区备案留痕）；不参与排班与审核'
const NURSING_DISPATCH_ABILITY = '护理人力调配（全院）：改护士岗位归属（可填生效/失效日期，不改编制）+ 护理排班（病区矩阵）/ 员工排班（门诊科室与全院单元）+ 排班总览'

// 其余角色的「能做什么」：按角色菜单实际授权归并成一段，供批量账号行直接引用
const ROLE_ABILITY = {
  '系统管理员': '全部业务页面 + 系统管理（用户/角色/菜单/字典/参数/日志审计）',
  '医生': DOCTOR_ABILITY,
  '护士': NURSE_ABILITY,
  '护士长': HEAD_NURSE_ABILITY,
  '收费员': '窗口收费、退费申请、发票管理、财务日结、费用记账、结算账单与支付流水查询、住院结算（自费与医保同一入口）',
  '药剂师': PHARM_ABILITY,
  '医技人员': MEDTECH_ABILITY,
  '设备与供应室管理员': 'CSSD 器械包与灭菌管理、设备台账与调配、医废管理、气体监测、资产定位（限本科室）',
  '前台导诊': '挂号预约（全院科室）、收费结算窗口、患者管理与患者标签',
  '体检中心前台': '体检登记与签到、套餐确认、现场建档、报告跟进（限本中心）；不进门诊挂号号源、不收费开票、不做临床总检',
  '急诊医生': ER_ABILITY,
  '门诊分诊护士': '分诊工作站（队列/呼叫/过号/复诊优先/分诊卡体征与分级/患者签到）+ 门诊日志、叫号改派、患者检索（限本科室）；不进急诊工作站',
  '急诊分诊护士': '急诊护士工作站：急诊登记（急诊科室单元 + I~IV 分级 MEWS 辅助 + 在岗值班医生）、批量登记、复评（只升不降）、腕带打印（限本科室）；不进门诊分诊台，接诊/转归归急诊医生',
  '检验技师': '检验工作站、标本管理、检验项目字典、输血管理、危急值处置（限本科室）',
  '检查技师': '检查工作站、检查预约中心、检查设备与号源、检查项目字典、标本管理、危急值处置（限本科室）',
  '临床药师': '处方审核/点评、I 类切口预防用药点评、处方公示、抗菌药物分级目录与使用监测、合理用药知识库、麻精药品专册、取药队列（限本科室）',
  '病案编码员': '病历查阅、病案管理、患者主索引、健康档案与 360、上报科室代码对照（维护，不做临床书写）',
  '统计上报员': '病案统计上报、上报科室代码对照（只读）',
  '病案质控员': '病历质控、专项质控、病历查阅、门诊日志、病案管理（限本科室）',
  '医保结算员': '医保结算清单、慢特病医保备案、目录对照、政策配置、合规审核台账、扣款飞检、支付渠道对账、资金账户、欠费管控、医保住院结算与 2304/2305 报盘（限本科室）',
  '审计统计员': 'BI 驾驶舱、运营统计报表、病案统计上报、医保付费（DRG/DIP 分组模拟）、AI 运营问数、支付渠道对账、资金账户',
  '院领导': '报表与驾驶舱、护理质控、排班总览、营养指标、患者 360（管理视角，只读为主）',
  '放射诊断医师': '放射报告书写与审核（检查工作站）、危急值处置、检查项目字典（限本科室）',
  '营养师': '营养风险筛查、膳食医嘱执行、订餐配送、营养会诊、营养指标监测（全院）',
  '公卫医师': '院感监测、公卫上报、慢病建档与长处方认定、门诊日志、我的未报、随访回访（限本科室）',
  '行政人员': '工作台、排班总览、值班点位与值班类型配置、工单受理、满意度评价、总值班日志（无临床业务页面）',
  '客服专员': '患者常见问题维护、工单受理、满意度评价、线上问诊、体检管理',
  '门诊办管理员': '门诊排班审核、排班总览、双向转诊、满意度评价、诊室管理、体检管理与体检套餐',
  '医务科管理员': '员工/急诊排班审核、人力配置标准、全院总值班、病历/专项/单病种质控、危急值监督、医疗安全与纠纷、技术授权与抗菌药物处方权、科室管理、签名证书/验签/吊销、日间手术准入、死亡证明审核签发',
  '科主任': '本科室急诊/员工排班（提交后由医务科审核发布）、住院医生站、病历质控、专项质控、危急值、会诊、死亡证明',
  '科室排班员': SCHEDULE_PLANNER_ABILITY,
  '护理部主任': '护理排班整周备案与退回（全院）、排班总览、全院总值班、护理质控与医疗安全、设备共享调配',
  '科护士长': SCHEDULE_NURSING_AREA_AUDITOR_ABILITY,
  '麻醉医师': '麻醉访视与术前评估、麻醉记录（生命体征时间轴）、PACU 复苏、术后随访、三方核查签名（登录后切「麻醉医师」岗）',
  '手术室护士': '器械清点（术前 → 关体前 → 关体后三轮）、三方核查签名、手术间与排台查看（登录后切「手术室护士」岗）',
  '信息科管理员': '签名证书台账与办理（生成密钥对 / CA 在线申请 / 证书回填）、用户/角色/菜单/字典/参数/日志审计',
  '护理调配员': NURSING_DISPATCH_ABILITY,
  '医务调配员': '医师岗位归属调整（可填生效/失效日期，不改编制）+ 跨科室排门诊号源/急诊值班 + 排班总览，只排不自批',
  '院办值班管理员': '全院总值班、值班点位与值班类型配置、总值班日志（交班本）',
  '未配置岗位': '尚未配置岗位（登录后只有工作台与统一待办）',
}

// 账号文案是「账号 ( 姓名 )」，登录只需要账号本身 → 复制与一键登录都取括号前的部分
const accountOf = (item) => item.split('(')[0].trim()

// 全部账号合并为一张表：每行 = 同一科室 + 同一角色；排班流程账号按「排班员 → 审核人」流程顺序置顶
const credRows = [
  // 排班流程（推荐演示链路：排班员 → 审核人 → 全院）
  {
    accounts: ['admin ( 超级管理员  )'],
    role: '系统管理员',
    dept: '长沙麓康医院',
    ability: '全部业务页面 + 系统管理（用户/角色/菜单/字典/参数/日志审计）'
  },
  {
    accounts: ['xushuang ( 许霜  )'],
    role: '前台导诊',
    dept: '门诊部',
    ability: '挂号预约（全院科室）、收费结算窗口、结算账单查询、患者管理、患者标签（分诊工作站与就诊总览已收回）'
  },
  {
    accounts: ['wangyongbang ( 王永邦 )'],
    role: '科室排班员',
    dept: '心血管内科',
    ability: SCHEDULE_PLANNER_ABILITY
  },
  {
    accounts: ['liudonglin ( 刘冬琳 )'],
    role: '门诊办管理员',
    dept: '长沙麓康医院',
    ability: SCHEDULE_AUDITOR_ABILITY + '；心血管内科排班审核人'
  },
  {
    accounts: ['linzhenqing ( 林珍晴  )'],
    role: '门诊分诊护士',
    dept: '心血管内科',
    ability: '分诊工作站：队列/呼叫/重呼/过号/复诊优先/分诊卡体征与分级/患者签到，默认诊区=心内科（限本科室）；急诊工作站已收回'
  },
  // 动态岗演示对象：仅在心内科病区在册，无排班权限；改岗位归属/有效期验证名册与排班边界
  {
    accounts: ['shijingling ( 时静玲 )'],
    role: '护士',
    dept: '神经内科',
    ability: NURSE_ABILITY + '；动态岗演示：转科前后在册名册与排班边界（本身不能排班）；顶栏可切岗位：护士（心血管内科）'
  },
  {
    accounts: ['caoyaxia ( 曹亚霞  )', 'qiulifeng ( 邱立峰  )'],
    role: '护士长',
    dept: '心血管内科',
    ability: HEAD_NURSE_ABILITY
  },
  // 动态岗对照组：神经内科是心内科的接收科室，两边名册互斥（转科生效日当天换边）
  {
    accounts: ['xusue ( 徐素娥 )'],
    role: '护士长',
    dept: '神经内科',
    ability: '病区护理排班编制/改格/删除/整周提交（神经内科病区，限本科室）；无审核权（审核在内科系统科护士长）'
  },
  // 护理三级链第二档：科护士长，审核发布片区内病区的整周排班
  {
    accounts: ['yangmeimei ( 杨美梅 )'],
    role: '科护士长',
    dept: '内科系统',
    ability: SCHEDULE_NURSING_AREA_AUDITOR_ABILITY
  },
  {
    accounts: ['chenmengyu ( 陈梦玉 )'],
    role: '护理部主任',
    dept: '护理部',
    ability: SCHEDULE_NURSING_FILING_ABILITY
  },
  // 护理部人力调配岗：跨科室改岗位 + 排班，演示「今天护士 A 去某科做分诊、护士 B 去输液室」
  {
    accounts: ['shenxiuying ( 沈秀莹  )'],
    role: '护理调配员',
    dept: '护理部',
    ability: NURSING_DISPATCH_ABILITY
  },
  {
    accounts: ['tanxiangli ( 谭向礼 )'],
    role: '医务科管理员',
    dept: '医务科',
    ability: '全院总值班登记/换班/审核发布、急诊排班审核、人力配置标准与岗位人力标准维护、排班总览；'
        + '顶栏可切岗位：医务调配员（医务科）= 改医师岗位归属（可填生效/失效日期，不改编制）+ 跨科室排门诊号源/急诊值班 + 排班总览，只排不自批'
  },

  // 三个治疗科室（推荐账号）
  {
    accounts: ['mojixin ( 莫吉信 )', 'shizhiqing ( 施志庆  )', 'wangyong ( 王勇  )', 'zengleimin ( 曾蕾敏 )', 'zhengchenglei ( 郑晟磊 )'],
    role: '医生',
    dept: '心血管内科',
    ability: DOCTOR_ABILITY
  },
  {
    accounts: ['lijinxin ( 李金信  )'],
    role: '信息科管理员',
    dept: '信息科',
    ability: '签名证书台账与办理（生成密钥对 / CA 在线申请 / 证书回填）、用户/角色/菜单/字典/参数/日志审计；顶栏可切岗位：设备与供应室管理员（设备科）'
  },
  // 签名证书固定账号（信息科管理员）：台账查询 + 办理（CA在线申请/回填）；吊销处置归医务科（分权）
  {
    accounts: ['jianglinhao ( 蒋林浩 )'],
    role: '信息科管理员',
    dept: '信息科',
    ability: '签名证书：台账查询、办理（生成密钥对 / CA 在线申请 / 状态刷新 / 证书回填）、CA 通道状态自检；吊销不在本岗（医务科处置，分权）'
  },
  // 手术麻醉链：主岗位是「医生」（落麻醉科），登录后切「麻醉医师」岗做麻醉动作
  {
    accounts: ['yuanfengzhi ( 袁凤芝 )', 'liuguifeng ( 刘桂凤 )', 'jiangxiajing ( 江夏静 )', 'chengui ( 陈贵 )'],
    role: '医生',
    dept: '麻醉科',
    ability: DOCTOR_ABILITY + '；顶栏可切岗位：麻醉医师（麻醉科）= 麻醉访视与术前评估、麻醉记录（生命体征时间轴）、PACU 复苏、术后随访、三方核查签名'
  },
  // 手术室没有独立科室节点，「手术室护士」岗位落在本人现科室；该角色 data_scope=1（全院），手术/清点数据不受落点科室收口
  {
    accounts: ['mengyuni ( 孟雨妮  )'],
    role: '护士长',
    dept: '呼吸内科',
    ability: HEAD_NURSE_ABILITY + '；顶栏可切岗位：手术室护士（呼吸内科）= 器械清点（术前 → 关体前 → 关体后三轮）、三方核查签名、手术间与排台查看'
  },
  {
    accounts: ['wenchengyan ( 温晟岩 )'],
    role: '护士',
    dept: '麻醉科',
    ability: NURSE_ABILITY + '；顶栏可切岗位：手术室护士（麻醉科）= 器械清点（术前 → 关体前 → 关体后三轮）、三方核查签名、手术间与排台查看'
  },
  {
    accounts: ['fengxuejing ( 冯雪晶  )', 'yuanhaibang ( 袁海邦  )'],
    role: '收费员',
    dept: '财务科',
    ability: '窗口收费、退费申请、发票管理、财务日结、费用记账、账单与支付流水查询（全院，可跨科室结账单）、住院结算（收预交金、办出院结算——自费与医保同一入口）'
  },
  {
    accounts: ['hannaying ( 韩娜颖  )'],
    role: '收费员',
    dept: '财务科',
    ability: '窗口收费、退费申请、发票管理、财务日结、费用记账、账单与支付流水查询、住院结算（自费与医保同一入口）；顶栏可切岗位：医保结算员（医保科）= 医保结算清单、慢特病备案、目录对照、政策配置、合规审核、扣款飞检、渠道对账、资金账户、欠费管控、医保住院结算与 2304/2305 报盘'
  },
  {accounts: ['dengrongdong ( 邓荣东  )'], role: '药剂师', dept: '门诊中药房', ability: PHARM_ABILITY},
  {accounts: ['dongxiaxia ( 董夏霞  )'], role: '药剂师', dept: '住院药房', ability: PHARM_ABILITY},
  {accounts: ['jiangleijie ( 江蕾洁  )'], role: '药剂师', dept: '静脉配液中心', ability: PHARM_ABILITY},
  {accounts: ['jiangzheng ( 蒋政  )'], role: '药剂师', dept: '门诊西药房', ability: PHARM_ABILITY},
  {accounts: ['caipingling ( 蔡萍玲  )'], role: '设备与供应室管理员', dept: '消毒供应中心', ability: 'CSSD 器械包与灭菌管理、设备台账与调配、医废管理、气体监测、资产定位（限本科室）'},
  {accounts: ['kangjie ( 康洁  )'], role: '检验技师', dept: '输血科', ability: '检验工作站、标本管理、检验项目字典、输血管理、危急值处置（限本科室）'},
  {
    accounts: ['cenzhenlu ( 岑珍璐  )'],
    role: '检验技师',
    dept: '医学检验科',
    ability: '检验工作站、标本管理、检验项目字典、输血管理、危急值处置（限本科室）'
  },
  {accounts: ['shiaiyan ( 石爱燕  )'], role: '急诊医生', dept: '急诊内科', ability: ER_ABILITY},
  {accounts: ['zhouhaiyong ( 周海永 )'], role: '急诊医生', dept: '急诊内科', ability: ER_ABILITY + '；顶栏可切岗位：科主任（急诊内科）= 本科室急诊排班（排班/修改/删除），提交后由医务科审核发布'},
  {accounts: ['chengmeimin ( 程梅敏  )'], role: '急诊医生', dept: '急诊外科', ability: ER_ABILITY},
  {accounts: ['dongxuejie ( 董雪洁  )'], role: '急诊医生', dept: '急诊内科', ability: ER_ABILITY},
  {accounts: ['heshaode ( 何少德  )'], role: '急诊医生', dept: '急诊儿科', ability: ER_ABILITY},
  {accounts: ['jiangqianjing ( 江倩静  )'], role: '急诊医生', dept: '肠道门诊', ability: ER_ABILITY},
  {accounts: ['mohainan ( 莫海楠  )'], role: '急诊医生', dept: '发热门诊', ability: ER_ABILITY},
  // 急诊挂号链路（2026-10-27 新增夹具）：汤晓岚急诊登记 → 急诊医生工作站接诊
  {
    accounts: ['tangshi ( 汤诗  )'],
    role: '急诊分诊护士',
    dept: '急诊内科',
    ability: '急诊护士工作站：急诊登记（选急诊科室单元 + I~IV 分级 MEWS 辅助 + 在岗值班医生）、批量登记、复评（只升不降）、腕带打印；不进门诊分诊台，接诊/转归归急诊医生'
  },
  // 急诊排班链路（2026-10-26 下放科主任）：科主任切岗排班 → 医务科(tanyiming)审核发布
  {
    accounts: ['linyanling ( 林艳玲 )'],
    role: '急诊医生',
    dept: '急诊外科',
    ability: ER_ABILITY + '；顶栏可切岗位：科主任（急诊外科）= 本科室急诊排班（排班/修改/删除），提交后由医务科审核发布'
  },
  {
    accounts: ['wujiehui ( 吴洁慧 )'],
    role: '急诊医生',
    dept: '急诊儿科',
    ability: ER_ABILITY + '；顶栏可切岗位：科主任（急诊儿科）= 本科室急诊排班（排班/修改/删除），提交后由医务科审核发布'
  },
  {
    accounts: ['longchunyun ( 龙春云  )'],
    role: '护士长',
    dept: '护理部',
    ability: HEAD_NURSE_ABILITY + '；落点科室为护理部，四类排班页按护理部口径展示（非某个病区）'
  },
  {
    accounts: ['qiqihang ( 齐启航  )'],
    role: '检查技师',
    dept: '放射科',
    ability: '检查工作站、检查预约中心、检查设备与号源、检查项目字典、标本管理、危急值处置（限本科室）'
  },
  {
    accounts: ['kangqinwen ( 康琴雯  )'],
    role: '病案编码员',
    dept: '病案室',
    ability: '病历查阅、病案管理、患者主索引、健康档案与 360、上报科室代码对照（维护）；顶栏可切岗位：统计上报员（病案室）= 病案统计上报（生成报文/报出/作废）+ 上报科室代码对照（只读）'
  },
  {
    accounts: ['hanyandan ( 韩艳丹  )'],
    role: '营养师',
    dept: '营养科',
    ability: '营养风险筛查、膳食医嘱执行、订餐配送、营养会诊、营养指标监测（全院）'
  },
  {
    accounts: ['gaofangjuan ( 高芳娟  )'],
    role: '行政人员',
    dept: '总务科',
    ability: '只有工作台与统一待办，无任何业务页面（用于岗位/权限实验）'
  },
  {
    accounts: ['longjianlin ( 龙建林  )'],
    role: '行政人员',
    dept: '保卫科',
    ability: '只有工作台与统一待办，无任何业务页面（用于岗位/权限实验）'
  },
  {
    accounts: ['liyuying ( 李玉莹  )'],
    role: '客服专员',
    dept: '客户服务中心',
    ability: '患者常见问题维护、工单受理'
  },
]

// 账号统一显示成「登录名 ( 姓名 )」：手工清单里有几个多敲了空格（`xushuang ( 许霜  )`），
// 库里导出的又统一是单个空格，两段并在一张表里能看出参差，这里一次性抹平
const formatAccount = (item) => {
  const matched = String(item).match(/^([^(（]+)[(（](.*)[)）]$/)
  if (!matched) {
    return String(item).trim()
  }
  return `${matched[1].trim()} ( ${matched[2].replace(/[\s　]+/g, '')} )`
}

// 库里的全部员工账号（credAccounts 由 dev 库导出）→ 按「角色 + 科室」归成行，
// 已在上面手工清单里出现的账号跳过（手工行带演示说明，不再重复列一遍）
const generatedCredRows = (() => {
  const listed = new Set()
  credRows.forEach((row) => row.accounts.forEach((item) => listed.add(accountOf(item))))

  const groups = new Map()
  credAccounts.forEach(([user, name, role, dept, otherPosts]) => {
    if (listed.has(user)) {
      return
    }
    const key = `${role}|${dept}`
    let group = groups.get(key)
    if (!group) {
      group = {role, dept, accounts: [], switchPosts: []}
      groups.set(key, group)
    }
    group.accounts.push(`${user} ( ${name} )`)
    if (otherPosts) {
      otherPosts.split('、').forEach((post) => {
        if (!group.switchPosts.includes(post)) {
          group.switchPosts.push(post)
        }
      })
    }
  })

  return [...groups.values()].map((group) => {
    // 已有同「角色 + 科室」的手工行：账号并进去，保留手工那段演示说明
    const manual = credRows.find((row) => row.role === group.role && row.dept === group.dept)
    if (manual) {
      manual.accounts.push(...group.accounts)
      return null
    }
    const ability = (ROLE_ABILITY[group.role] || `${group.role}：按角色菜单授权（限本科室）`)
        + (group.switchPosts.length
            ? `；顶栏可切岗位：${group.switchPosts.map((post) => post.replace('｜', '（') + '）').join('、')}`
            : '')
    // bulk：库里批量导出的账号行——每行自带角色/科室/说明，不做纵向合并（几百行合起来会滚出一片空白格）
    return {accounts: group.accounts, role: group.role, dept: group.dept, ability, bulk: true}
  }).filter(Boolean)
})()

// 手工清单（演示链路说明）+ 库里其余账号，合成弹框的全量表
const allCredRows = [...credRows, ...generatedCredRows]
// 账号数去重后计数：同一人挂多个岗位会在手工行里出现两次（演示「顶栏切岗位」），不能重复计
const credAccountCount = new Set(allCredRows.flatMap((row) => row.accounts.map(accountOf))).size

// 凭据关键词筛选（纯前端）：角色 / 科室 / 账号列（含姓名与登录名）/ 姓名首拼（xs=许霜）/ 能做什么 模糊匹配，不查后端
const credKeyword = ref('')

// 账号文案「账号 ( 姓名 )」→ 姓名；首拼与全拼都按姓名算，账号与姓名口径不一致时（手工演示行）也筛得到
const nameOf = (item) => {
  const matched = String(item).match(/^([^(（]+)[(（](.*)[)）]$/)
  return matched ? matched[2].replace(/[\s　]+/g, '') : String(item).trim()
}

// 每行检索文本只拼一次：490 行 × 每次按键都过一遍，不重复算拼音
const searchTextCache = new WeakMap()
const searchOf = (row) => {
  let cached = searchTextCache.get(row)
  if (!cached) {
    cached = {
      text: [row.role, row.dept, row.ability, ...row.accounts].join(' ').toLowerCase(),
      // 姓名首拼单独存：按首字母前缀命中（xs=许霜），不像正文那样 any-includes——
      // 否则 xy 会顺带命中「…x…y…」的一切首拼串，一个演示弹框筛出二十多行
      initials: row.accounts.map((item) => initialsOf(nameOf(item))),
    }
    searchTextCache.set(row, cached)
  }
  return cached
}

const filteredCredRows = computed(() => {
  const kw = credKeyword.value.trim().toLowerCase()
  if (!kw) {
    return allCredRows
  }
  // 空格分词、逐词命中（「心内科 护士长」这类组合也能筛）：每个词都出现在同一行即算命中
  const tokens = kw.split(/\s+/).filter(Boolean)
  return allCredRows.filter((row) => {
    const hay = searchOf(row)
    return tokens.every((token) => hay.text.includes(token)
        || hay.initials.some((item) => item.startsWith(token)))
  })
})

// 排序的唯一目标是「同一角色必须挨在一起」：手工演示行所在角色按原文顺序置顶，
// 其余角色按中文序；同一角色内手工科室在前、其余科室中文序；同科室手工行在前。
// 之前手工段与批量段分开排（手工全部置顶、批量另排），同一个角色只要有手工行就会在下方再出现一块
// （前台导诊、门诊办、护士都撞上过），角色列也就合并不了。
const manualRoleRank = new Map()
const manualDeptRank = new Map()
credRows.forEach((row, i) => {
  if (!manualRoleRank.has(row.role)) {
    manualRoleRank.set(row.role, manualRoleRank.size)
  }
  if (!manualDeptRank.has(`${row.role}|${row.dept}`)) {
    manualDeptRank.set(`${row.role}|${row.dept}`, i)
  }
})
const roleSortKey = (row) => (manualRoleRank.has(row.role) ? [manualRoleRank.get(row.role), ''] : [manualRoleRank.size, row.role])
const deptSortRank = (row) => (manualDeptRank.has(`${row.role}|${row.dept}`) ? manualDeptRank.get(`${row.role}|${row.dept}`) : Infinity)

const sortedCredRows = computed(() => {
  const [roleRank, roleName] = [0, 1]
  return filteredCredRows.value.slice().sort((a, b) => {
    const ka = roleSortKey(a)
    const kb = roleSortKey(b)
    return (ka[roleRank] - kb[roleRank])
        || String(ka[roleName]).localeCompare(String(kb[roleName]), 'zh-Hans-CN')
        || (deptSortRank(a) - deptSortRank(b))
        || a.dept.localeCompare(b.dept, 'zh-Hans-CN')
        || (a.bulk ? 1 : 0) - (b.bulk ? 1 : 0)
  })
})

// 跨行合并：一个角色一格（同角色下的多个科室都收在这一格下面），科室一格（可能挂多个账号）；
// 「能做什么」不合并——每个科室行重复一遍，滚到角色块中间也一直能看出这一岗能干什么
const credSpans = computed(() => {
  const list = sortedCredRows.value
  const sameRole = (i, j) => list[i].role === list[j].role
  const sameRoleDept = (i, j) => sameRole(i, j) && list[i].dept === list[j].dept
  // 同组首行给出组内行数，后续行给 0（交给上一行的 rowspan 覆盖）
  const spanAt = (i, same) => {
    if (i > 0 && same(i, i - 1)) {
      return 0
    }
    let n = 1
    while (i + n < list.length && same(i + n, i)) n++
    return n
  }
  // 列序：0 角色 / 1 科室 / 2 账号（一格里列多行，不跨行）/ 3 能做什么（每行都写）
  return [list.map((row, i) => spanAt(i, sameRole)), list.map((row, i) => spanAt(i, sameRoleDept)), null, null]
})

const credSpanMethod = ({columnIndex, rowIndex}) => {
  const spans = credSpans.value[columnIndex]
  if (!spans || spans[rowIndex] === undefined) {
    return {rowspan: 1, colspan: 1}
  }
  return {rowspan: spans[rowIndex], colspan: 1}
}

const handleCopyAccount = async (item) => {
  const account = accountOf(item)
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

// 一键登录：演示账号密码统一 123456（见弹框标题），验证码本页自动填充
// 关掉弹框后复用正常登录链路，不给登录逻辑留第二份分支
const TEST_PASSWORD = '123456'
const handleQuickLogin = (item) => {
  loginForm.username = accountOf(item)
  loginForm.password = TEST_PASSWORD
  loginForm.captcha = captchaCode
  credVisible.value = false
  handleLogin()
}

onMounted(() => {
  generateCaptcha()
})

const handleLogin = async () => {
  if (loading.value) return
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
  padding-right: 4px;
}

/* 滚动条收进表格体内（el-table max-height + 固定表头），外层不再整体滚动 */
.cred-filter {
  margin-bottom: 10px;
}

.cred-hint {
  margin: 0 0 8px;
  font-size: 12px;
  line-height: 1.6;
  color: #7D93A8;
}

/* 账号多到需要滚：竖向滚动条常显。
   EP 的 el-table 表体原生滚动条被 `scrollbar-width: none` 关掉，自带的细条又是 inline `display:none`
   （只在悬停/滚动那一瞬间闪现，Windows 上等于没有），490 个账号滚起来看不到滚动条，
   用户会以为列表到底了或页面卡住——这里把自带细条常显出来并上主题色 */
.cred-table :deep(.el-scrollbar__bar.is-vertical) {
  display: block !important;
  width: 8px;
  right: 2px;
}

.cred-table :deep(.el-scrollbar__bar.is-vertical .el-scrollbar__thumb) {
  border-radius: 4px;
  background: #BBD5EC;
  opacity: 1;
}

/* 滚动条是浮在表格右缘的：最后一列文字让出 14px，别被压住 */
.cred-table :deep(.el-table__body td:last-child .cell) {
  padding-right: 14px;
}

/* 合并格里的角色/科室/说明靠上对齐：角色块很高（医生跨几十个科室），垂直居中会让名字掉到块中间，
   滚到块里才突然出现，看着像空白列（也没法用 sticky 钉在顶部） */
.cred-table :deep(.el-table__body td:nth-child(1)),
.cred-table :deep(.el-table__body td:nth-child(2)),
.cred-table :deep(.el-table__body td:nth-child(4)) {
  vertical-align: top;
}

/* 一个角色块可能有几十个科室那么高：角色名在块内滚动时停在表体顶部，滚到中间不会只剩空白 */
.cred-table :deep(.el-table__body td:nth-child(1) .cell) {
  position: sticky;
  top: 8px;
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

/* 一键登录：账号行内的轻量文字按钮，与复制图标同一行 */
.quick-btn {
  flex-shrink: 0;
  padding: 1px 8px;
  border: 1px solid #BBD5EC;
  border-radius: 4px;
  background: #F2F8FD;
  color: #1269B5;
  font-size: 12px;
  line-height: 18px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.quick-btn:hover {
  border-color: #1269B5;
  background: #1269B5;
  color: #fff;
}

.login-footer {
  margin-top: 24px;
  font-size: 12px;
  color: #9AAFC2;
  letter-spacing: 1px;
}
</style>

