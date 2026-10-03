/**
 * 剪贴板复制 —— 全前端唯一实现
 *
 * 为什么必须收口：改造前 `PatientsView.vue`（handleCopyName）和 `DoctorWorkstationView.vue`
 * （copyToClipboard）各写了一份，失败路径还不一样 —— 一份 catch 后提示「复制失败」，
 * 另一份 catch 里什么都不做（点了没反应，用户以为按钮坏了）。
 *
 * 为什么必须带降级：`navigator.clipboard` 只在**安全上下文**存在（https / localhost / 127.0.0.1）。
 * 本项目多人用 `http://192.168.x.x:3000` 访问开发机（非安全上下文）→ `navigator.clipboard`
 * 是 undefined 或直接 reject。只写 clipboard 的实现在局域网访问下**恒失败**，而开发机上
 * （localhost）测不出来 —— 这正是上面的老实现留下的坑。
 */

/**
 * 降级复制：临时 textarea + execCommand。
 * 不能用 display:none / visibility:hidden（不可选中，select() 无效），只能移出视口。
 * @param {string} text
 * @returns {boolean}
 */
function legacyCopy(text) {
  try {
    const ta = document.createElement('textarea')
    ta.value = text
    ta.setAttribute('readonly', '')
    ta.style.position = 'fixed'
    ta.style.top = '-1000px'
    ta.style.left = '-1000px'
    ta.style.opacity = '0'
    document.body.appendChild(ta)
    ta.select()
    ta.setSelectionRange(0, text.length)
    const ok = document.execCommand('copy')
    document.body.removeChild(ta)
    return ok
  } catch (e) {
    console.error('降级复制失败', e)
    return false
  }
}

/**
 * 复制文本到剪贴板
 *
 * @param {string|number|null|undefined} raw 待复制内容（雪花 ID 请传字符串）
 * @returns {Promise<boolean>} 是否复制成功 —— 调用方必须据此提示，不要不声不响
 */
export function copyText(raw) {
  const text = raw === null || raw === undefined ? '' : String(raw).trim()
  if (!text) return Promise.resolve(false)

  if (navigator.clipboard?.writeText) {
    // 有些环境下 clipboard 存在但仍会 reject（页面失焦、权限被拒）→ 一律回落到 execCommand
    return navigator.clipboard
      .writeText(text)
      .then(() => true)
      .catch(() => legacyCopy(text))
  }
  return Promise.resolve(legacyCopy(text))
}
