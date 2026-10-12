/**
 * 渠道对账枚举与金额展示 —— 页面与拆出的弹框组件共享的单点定义（原单点定义在 PayChannelView 页面文件，
 * 拆分后由页面与子组件共同引用，页面禁写本地副本）。
 * 无字典码值，命中不了渲染 未知(n) 不回落。
 *
 * 渠道只列走商户平台的在线渠道（对齐后端 PaymentMethodEnum.channelBacked）：
 * 4 是医保个人账户，刷参保人卡扣的钱不由商户平台出账单，混进来会让"渠道短款"和"医保欠付"看起来是同一件事。
 */
export const CHANNELS = [
  { value: 2, label: '微信' },
  { value: 3, label: '支付宝' },
  { value: 6, label: '银行卡' },
]
export const MATCH_STATUS = [
  { value: 0, label: '待勾对' },
  { value: 1, label: '已勾对' },
  { value: 2, label: '长款' },
  { value: 3, label: '短款' },
]

export const money = (v) => Number(v ?? 0).toFixed(2)
