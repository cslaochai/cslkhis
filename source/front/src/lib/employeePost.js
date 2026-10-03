/**
 * 岗位（角色 × 科室）表单行的共用口径：回显归一、提交清洗、前端预校验。
 *
 * 用户管理与员工档案两处表单都要配岗位，口径必须一致，否则一边能存一边不能存。
 */

/** 接口返回的 posts → 表单行。deptId 统一字符串化（后端 Long 出参是 string，科室下拉的 id 是 number） */
export function postsFromApi(posts) {
  return (posts || []).map((p) => ({
    roleCode: p.roleCode || '',
    deptId: p.deptId === null || p.deptId === undefined ? '' : String(p.deptId),
    isPrimary: p.isPrimary === 1 ? 1 : 0,
    effectiveDate: p.effectiveDate || '',
    expireDate: p.expireDate || '',
    // 后端派生（1-在职 2-已失效），只用于展示「已失效」标记；提交时 postsToPayload 会丢掉它
    postStatus: p.postStatus === 2 ? 2 : 1,
  }))
}

/** 表单行 → 提交给后端的 payload；角色或科室没选的行（点了「添加岗位」没填）直接丢掉 */
export function postsToPayload(rows) {
  return (rows || [])
    .filter((r) => r.roleCode && r.deptId)
    .map((r) => ({
      roleCode: r.roleCode,
      deptId: r.deptId,
      isPrimary: r.isPrimary === 1 ? 1 : 0,
      // '' → null：生效/失效日期两侧 NULL = 不限（保存即生效 / 长期有效），空串会被后端解析成 400
      effectiveDate: r.effectiveDate || null,
      expireDate: r.expireDate || null,
    }))
}

/**
 * 提交前校验，返回第一条错误文案（空串 = 通过）。
 * @param names 可选的 {roleNameOf, deptNameOf}，只为了把报错写得像人话
 */
export function checkPosts(rows, names = {}) {
  const errors = []
  const picked = []
  ;(rows || []).forEach((row, index) => {
    const no = index + 1
    if (!row.roleCode && !row.deptId) return
    if (!row.roleCode) {
      errors.push(`第 ${no} 行岗位没有选择角色`)
      return
    }
    if (!row.deptId) {
      errors.push(`第 ${no} 行岗位没有选择科室`)
      return
    }
    if (row.effectiveDate && row.expireDate && row.expireDate < row.effectiveDate) {
      errors.push(`第 ${no} 行岗位失效日期早于生效日期`)
    }
    picked.push(row)
  })
  if (picked.length === 0 && !errors.length) {
    errors.push('至少配置一条岗位：这个人在哪个科室、以什么角色执业')
  }
  const seen = new Set()
  picked.forEach((row) => {
    const key = `${row.roleCode}#${row.deptId}`
    if (seen.has(key)) {
      const roleName = names.roleNameOf ? names.roleNameOf(row.roleCode) : row.roleCode
      const deptName = names.deptNameOf ? names.deptNameOf(row.deptId) : row.deptId
      errors.push(`岗位重复：${deptName} · ${roleName}`)
    }
    seen.add(key)
  })
  // 主岗位可以在表格里再点一次取消，所以「一条都没勾」是常见状态而不是 bug，必须在这里拦住：
  // 后端 replacePosts 遇到全 0 会兜底把第一条提上去，那是 API 直连时的最后一道闸，不该让它替用户做决定。
  if (!errors.length && !picked.some((row) => row.isPrimary === 1)) {
    errors.push('请选择一条主岗位：它是这个人的主科室（登录默认落点、号源与名册都读它）')
  }
  return errors[0] || ''
}
