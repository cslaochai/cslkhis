import { wxApi } from './api'

/**
 * 微信平台三个口子的小程序端封装（小程序一期，后端均为打印桩形态）。
 *
 * 模板 ID 现为占位：真实对接时替换为公众平台「订阅消息」里的模板 ID，
 * 后端 yml（wechat.miniapp.templates）同步配置同名 scene。
 */
const SUBSCRIBE_TEMPLATES = {
  // 场景 → 模板 ID（占位）
  report_ready: 'MOCK_TMPL_REPORT_READY',
  queue_called: 'MOCK_TMPL_QUEUE_CALLED',
  pay_receipt: 'MOCK_TMPL_PAY_RECEIPT'
}

/**
 * 请求订阅消息授权（一次性订阅）。
 * 用户拒绝/模板占位时不报错 —— 授权只是「让用户更快知道」，失败不影响业务。
 */
export function askSubscribe(scene) {
  const tmplId = SUBSCRIBE_TEMPLATES[scene]
  if (!tmplId || !wx.requestSubscribeMessage) {
    console.log(`[订阅消息口子] 场景 ${scene} 未配置模板或环境不支持，跳过授权`)
    return Promise.resolve(false)
  }
  return new Promise((resolve) => {
    wx.requestSubscribeMessage({
      tmplIds: [tmplId],
      success(res) {
        const accepted = res[tmplId] === 'accept'
        console.log(`[订阅消息口子] 场景 ${scene} 授权结果: ${res[tmplId]}`)
        resolve(accepted)
      },
      fail(err) {
        console.log('[订阅消息口子] 授权请求失败', err && err.errMsg)
        resolve(false)
      }
    })
  })
}

/**
 * 登录态下绑定 openid（订阅消息发送依赖此绑定）；失败不外抛（绑定只是增强）。
 * 注：微信一键登录暂未上线（未真对接平台），登录仍走账密/注册；本绑定只服务订阅消息。
 */
export function bindOpenidSilently() {
  wx.login({
    success({ code }) {
      wxApi.bindOpenid({ code }).catch(() => { /* 绑定失败静默：可能该微信已绑其他账号 */ })
    },
    fail: () => { /* 忽略 */ }
  })
}
