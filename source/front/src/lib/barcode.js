/**
 * Code128B 条码渲染（纯 JS，无外部依赖）。
 *
 * 为什么自己实现：标签上的条码必须能被扫码枪读出来，图形只是表达，
 * 真正的信息是那串 ASCII 码值。渲染成 SVG 后按毫米给宽高，打印不会糊。
 *
 * 用法：barcodeSvg('S202611270012', 47, 9) → 返回 <svg> 字符串，塞进 v-html。
 */

/* 107 个符号的条空宽度，交替 条/空；除终止符（7 位）外每个符号合计必须 11 模块 */
const C128 = [
  '212222', '222122', '222221', '121223', '121322', '131222', '122213', '122312', '132212', '221213',
  '221312', '231212', '112232', '122132', '122231', '113222', '123122', '123221', '223211', '221132',
  '221231', '213212', '223112', '312131', '311222', '321122', '321221', '312212', '322112', '322211',
  '212123', '212321', '232121', '111323', '131123', '131321', '112313', '132113', '132311', '211313',
  '231113', '231311', '112133', '112331', '132131', '113123', '113321', '133121', '313121', '211331',
  '231131', '213113', '213311', '213131', '311123', '311321', '331121', '312113', '312311', '332111',
  '314111', '221411', '431111', '111224', '111422', '121124', '121421', '141122', '141221', '112214',
  '112412', '122114', '122411', '142112', '142211', '241211', '221114', '413111', '241112', '134111',
  '111242', '121142', '121241', '114212', '124112', '124211', '411212', '421112', '421211', '212141',
  '214121', '412121', '111143', '111341', '131141', '114113', '114311', '411113', '411311', '113141',
  '114131', '311141', '411131', '211412', '211214', '211232', '2331112',
]

const START_B = 104
const STOP = 106

const sumWidth = (pattern) => pattern.split('').reduce((sum, ch) => sum + Number(ch), 0)

/* 表错一位整段码就废，加载时自检：除终止符外必须都是 11 模块 */
const INVALID = C128.slice(0, 106).filter((p) => sumWidth(p) !== 11)
if (INVALID.length || sumWidth(C128[STOP]) !== 13) {
  console.error('Code128 宽度表异常，条码不可用', INVALID)
}

function toCodes(text) {
  const codes = [START_B]
  let checksum = START_B
  for (let i = 0; i < text.length; i += 1) {
    const value = text.charCodeAt(i) - 32
    if (value < 0 || value > 94) throw new Error(`Code128B 只支持可见 ASCII：${text}`)
    codes.push(value)
    checksum += value * (i + 1)
  }
  codes.push(checksum % 103, STOP)
  return codes
}

/**
 * 生成 Code128B 条码 SVG。
 * @param {string} text 码值（只能是可见 ASCII）
 * @param {number} widthMm 条码总宽（毫米）
 * @param {number} heightMm 条码总高（毫米）
 */
export function barcodeSvg(text, widthMm, heightMm) {
  const modules = []
  toCodes(text).forEach((code) => {
    C128[code].split('').forEach((width, index) => {
      modules.push({ bar: index % 2 === 0, width: Number(width) })
    })
  })

  // 两侧各留 10 模块静区（Code128 的可读性要求，缺了扫码枪容易读偏）
  const quiet = 10
  const total = modules.reduce((sum, item) => sum + item.width, 0) + quiet * 2
  let x = quiet
  let rects = ''
  modules.forEach((item) => {
    if (item.bar) rects += `<rect x="${x}" y="0" width="${item.width}" height="100"/>`
    x += item.width
  })

  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${total} 100" preserveAspectRatio="none" `
      + `style="width:${widthMm}mm;height:${heightMm}mm;display:block"><g fill="#000">${rects}</g></svg>`
}
