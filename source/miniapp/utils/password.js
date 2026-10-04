/**
 * 口令加密（SM2 国密）—— 小程序专用版本。
 *
 * 明文口令不能出现在网络链路上。PC 端写在 source/front/src/lib/password.js，
 * 这里之所以要单独写一份，不是重复劳动，而是两端有两处**必须不同**：
 *
 * 1. 拿不到 npm 包：source/miniapp 是原生小程序工程（nodeModules: false），
 *    sm-crypto 只能把 dist 产物原样拷到 utils/sm2-bundle.js。
 * 2. 随机源不同（这才是坑）：sm-crypto 取随机数的顺序写死为
 *        window.crypto.getRandomValues  ->  require('crypto')  ->  globalThis.crypto.getRandomValues
 *    小程序里既没有 window，也没有 globalThis.crypto，更没有 require('crypto')。
 *    它的 SecureRandom 是在 **模块被 require 的那一刻** 就去取 32 字节随机数做种子的，
 *    取不到就直接 throw —— 所以 polyfill 必须在 require 之前装好，require 之后再补已经晚了。
 *
 *    这里用 wx.getRandomValues 取**真随机**填池，绝不退化到 Math.random：
 *    SM2 加密每次要随机选 k，k 可预测就等于私钥能被推算出来 —— 那样加密就只是个摆设了。
 *
 *    种子是一次性的：sm-crypto 拿到 32 字节后用自带 CSPRNG 派生后续随机数，
 *    和它在浏览器里的行为完全一致，安全性同标准做法。
 */

import { request } from './request'

let randPool = []
let cachedPubKey = null
let sm2Instance = null

/** 把 wx 返回的各种形态随机字节统一成 number[] */
function toRandomBytes(rv) {
  if (!rv) return []
  if (rv instanceof ArrayBuffer) return Array.from(new Uint8Array(rv))
  if (ArrayBuffer.isView(rv)) return Array.from(new Uint8Array(rv.buffer || rv))
  if (Array.isArray(rv)) return rv.map((b) => Number(b) & 0xff)
  return []
}

/** 是否运行在微信开发者工具模拟器里（仅用于联调兜底，不影响真机） */
function isDevtools() {
  try {
    const info = wx.getSystemInfoSync ? wx.getSystemInfoSync() : {}
    return info.platform === 'devtools'
  } catch (e) {
    return false
  }
}

/**
 * 必须在 require('./sm2-bundle') 之前调用。
 * polyfill 用同步方式从 randPool 取字节喂给 sm-crypto；randPool 由 refillRandomPool 异步填满。
 */
function installCryptoPolyfill() {
  if (typeof globalThis.crypto !== 'undefined' && typeof globalThis.crypto.getRandomValues === 'function') {
    return
  }
  globalThis.crypto = {
    getRandomValues(arr) {
      const u8 = arr instanceof Uint8Array ? arr : new Uint8Array(arr.buffer || arr)
      if (randPool.length < u8.length) {
        // 说明前面的 refillRandomPool 没把有效字节填进来，根因在 wx.getRandomValues 返回值
        throw new Error('[password] 安全随机源未就绪：refillRandomPool 未成功填充随机池，请检查 wx.getRandomValues 返回值')
      }
      for (let i = 0; i < u8.length; i++) {
        u8[i] = randPool.shift()
      }
      return u8
    },
  }
}

/** 用微信的真随机源补充随机池 */
export function refillRandomPool(bytes = 64) {
  const need = bytes - randPool.length
  if (need <= 0) return Promise.resolve()
  if (typeof wx === 'undefined' || typeof wx.getRandomValues !== 'function') {
    return Promise.reject(new Error('[password] 当前小程序基础库不支持 wx.getRandomValues（需 >= 2.15.0）'))
  }
  return new Promise((resolve, reject) => {
    wx.getRandomValues({
      length: need,
      success: (res) => {
        const arr = toRandomBytes(res && res.randomValues)
        if (arr.length !== need) {
          // 开发者工具模拟器偶发返回空 ArrayBuffer，这里兜底（仅联调），真机走真随机
          if (isDevtools()) {
            console.warn('[password] 开发者工具模拟器 wx.getRandomValues 未返回有效字节，已用本地随机兜底（仅联调，真机不受影响）')
            const fake = []
            for (let i = 0; i < need; i++) fake.push(Math.floor(Math.random() * 256))
            randPool = randPool.concat(fake)
            resolve()
            return
          }
          console.error('[password] wx.getRandomValues 返回字节数异常', { need, got: arr.length, raw: res && res.randomValues })
          reject(new Error(`[password] wx.getRandomValues 未返回有效随机字节（需要 ${need}，得到 ${arr.length}）。请改用真机预览/调试`))
          return
        }
        randPool = randPool.concat(arr)
        resolve()
      },
      fail: (err) => reject(new Error('[password] wx.getRandomValues 调用失败：' + ((err && err.errMsg) || err))),
    })
  })
}

/** 惰性 require：第一次用时才补 polyfill，保证顺序正确 */
function getSm2() {
  if (!sm2Instance) {
    installCryptoPolyfill()
    sm2Instance = require('./sm2-bundle.js')
  }
  return sm2Instance
}

async function fetchPublicKey(force = false) {
  if (cachedPubKey && !force) return cachedPubKey
  const res = await request({ url: '/auth/publicKey', method: 'GET' })
  if (!res || res.code !== 200 || !res.data || !res.data.publicKey) {
    throw new Error('获取登录加密公钥失败')
  }
  cachedPubKey = { keyId: res.data.keyId, publicKey: res.data.publicKey }
  return cachedPubKey
}

/**
 * 口令加密 -> 十六进制密文（C1C3C2，不带 04 前缀，与后端 BouncyCastle 的 SM2Engine.Mode.C1C3C2 对齐）
 * @param {string} plain 明文口令
 * @param {boolean} forceRefresh 传 true 时先丢掉缓存的公钥重拉一次（后端换了密钥时用）
 * @returns {Promise<string>} 密文
 */
export async function encryptPassword(plain, forceRefresh = false) {
  await refillRandomPool(256)
  const { publicKey } = await fetchPublicKey(forceRefresh)
  return getSm2().doEncrypt(plain, publicKey, 1)
}

export function resetPublicKeyCache() {
  cachedPubKey = null
}
