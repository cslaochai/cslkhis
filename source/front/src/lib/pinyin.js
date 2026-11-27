import {PINYIN_DICT} from './pinyinData'

// 姓名首拼（许妍 → xy）：登录页测试账号按人查时的「输入首拼也能筛到」用
// 字典只收登录弹框里出现过的汉字（见 pinyinData.js 头注释），非汉字原样转小写，查不到的字直接跳过——
// 跳过而不是留原字，是因为留原字会让首拼串中间夹一个汉字，之后任何 ASCII 输入都匹配不上
export const initialsOf = (text) => Array.from(String(text ?? '')).reduce((acc, ch) => {
  const py = PINYIN_DICT[ch]
  if (py) {
    return acc + py[0]
  }
  return /[a-z0-9]/.test(ch.toLowerCase()) ? acc + ch.toLowerCase() : acc
}, '')
