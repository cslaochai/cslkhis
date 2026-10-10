# AI Agent 全局指令与项目规范 (AGENTS.md)

> **️ 核心原则（Token 节省与输出约束）**
> 1. **绝对精简**：禁止输出任何寒暄、解释、总结或“好的”、“没问题”等废话。直接输出代码或结构化数据。
> 2. **禁止重复**：不要重复用户已提供的代码或上下文。只输出**新增**或**修改**的部分。
> 3. **结构化优先**：当需要传递多个参数时，优先使用 JSON 格式，禁止使用冗长的自然语言描述。
> 4. **按需阅读**：不要一次性输出整个文件，除非明确要求。使用占位符（如 `// ... 现有代码 ...`）省略未修改的部分。

## 1. 前后端交互与 RESTful 规范

- **架构风格**：只允许POST、GET、DELETE 方法。
- **HTTP 动词**：
    - `GET`：仅用于获取资源，禁止在 GET 请求体中传递复杂参数。
    - `POST`：用于创建资源或执行复杂查询。
    - `DELETE`：用于删除资源。
- **URL 命名**：
    - 1、总体使用驼峰命名
    - 2、如果是新增或者修改，采用 xxxUpsert的形式，例如：userUpsert
    - 3、如果是获取单条数据，则使用getById，如果是获取明细，除了获取当前表的还要获取其他扩展信息则用 getDetailById
    - 4、如果是删除，则直接使用deleteById的形式
    - 5、如果是分页查询，则直接使用listPage的形式
    - 6、如果是提供给下拉框使用的，则直接使用selectList的形式,如果是懒加载下拉选择则使用selectListPage

- **前后端输入输出规范**：
    - 前端传入数据到后端，统统采用实体类进行接收 命名规则为xxxDTO，
    - 如果是新增和修改则使用xxxUpsertDTO
    - 如果是分页查询则使用xxxQueryPageDTO
    - 如果是分页查询则使用xxxQueryDTO

    - 对于输出，如果是返回详细信息则为detailVO
    - 如果是返回列表数据则为listVO
    - 如果是返回下拉选择数据则为XXXXSelectListVO（与接口末段 `selectList` 同名口径，禁止再写 `XxxSelectVO`）
    - 如果仅仅只是返回数据的基本信息不包含其他扩展信息，则后缀为xxxVO
    - 前端传入到后端的数据必须要加上必填或者非必填校验，例如@NotBlank @NotNull等注解

- **枚举规范**：
    - 对于很多可以做成枚举的东西直接做成枚举，例如 性别 1-男，2-女，3-未知，对于其他能存redis的东西，存redis，例如标签信息可以直接存储在redis，其他地方直接使用即可

- **代码规范**：
  -对于java代码中需要返回给前端的，就是在controller类里面要返回给前端的类，如果里面包含的是某个主键id，则需要使用序列化为字符串形式，避免精度丢失，例如
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;
  -对于业务代码的处理请写在service中，并且在controller层不能直接依赖mapper的东西，必须是依赖service层，如果是A依赖B，则A不能直接调用B的Mapper，只能依赖B的service，B的service依赖B的mapper

- **表/列注释只写"是什么"**：`COMMENT` = 名词头 +（必要时）码值枚举，例如 `状态（1-启用 0-停用）`、`手术间编码`。
  禁止写举例（`OR01、OR02…`）、跨表引用（`biz_xxx.id`）、口径说明、踩坑警告（`删除走物理删`）、幂等策略与实现细节 ——
  这些放代码注释或本文件。建表 SQL 与库内注释同一口径；全库结构基线见 `docs/sql/`（按领域 23 个文件，由
  `workspace/_gen_ddl_by_domain.mjs` 从 dev 库导出）。

- **状态码规范**：
    - `200 OK`：请求成功。
    - `201 Created`：资源创建成功。
    - `400 Bad Request`：参数校验失败。
    - `401 Unauthorized` / `403 Forbidden`：权限问题。
    - `500 Internal Server Error`：服务端异常。
- **响应结构**：统一使用以下 JSON 格式：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {}
  }
  ```

- **主键入参一律 `Long`，禁止手写 `parseId(String)` 转换层**（2026-10-07 全仓清掉 11 处）：
  `ToStringSerializer` 是**出参**防 JS 丢精度，**入参**侧前端必然传字符串，而 Jackson 自己就能把 `"1857..."` 反序列化成
  `Long` 字段 ——
  多写这一层只有两个下场，都比不写更糟：
  ① 正则 `\d{1,20}` + `Long.parseLong`：转化不了返回 `null` **不报错** → 下游 `selectById(null)` 报「数据不存在」，
  把「你传了个坏 ID」说成「数据没了」，真因被掩盖；
  ② **正则放行 20 位、`parseLong` 只吃 19 位**，两边不一致的那一格抛 `NumberFormatException` → 走 `GlobalExceptionHandler`
  的 `Exception` 兜底 = **HTTP 500「系统内部错误」**，调用方传错一个字符，后端说自己崩了。
  正则 + 手写解析 = 双重真相，天然出裂缝。
    - ✅ 正确形态：DTO `@NotNull private Long id`（`@NotBlank` 只对 String 有意义）+ Controller `@RequestParam Long id`。
      非法值自动 400（`MethodArgumentTypeMismatchException` / `HttpMessageNotReadableException` 都已接住）。
    - **出参一个都不动**（保持 `String id` 或 `Long + @JsonSerialize(ToStringSerializer)`）→ 前端零改动。
    - 同族：主键用 `List<String>` + `IN (${ids})` 拼接的一律改 `List<Long>` + `<foreach>` 逐个 `#{}`，
      「非数字白名单 for 循环」整个不需要（入参已是 Long，非数字进不来；拼 IN 反而是给自己开注入面）。
    - 仅当解析的是**内部裸 SQL 行数据**（`Map` 里 CAST AS CHAR 的列）才留工具方法，名字写明 `parseRowId`。
    - 机械判据：`grep -rn "parseId\|parseRowId\|\\\\d{1,20}" --include=*.java source/back_end` 只允许命中
      内部行数据那一处，其余为 0。

## 2. 前端界面规范

- **禁止使用 `src/components/his/ModulePage.vue`**。它是演示用的硬编码壳（`rows` / `stats` 写死在页面里，不接后端），*
  *不允许新增引用、不允许在其上继续加功能**；存量引用页面（`views/**`，约 20
  个）属待删除项，改造时直接换成真实接口驱动的页面，不要"顺手补两个字段"。
- **页面必须是真实数据驱动的**：`src/api/xxx.js` 调后端 → 页面渲染 → 操作回写后端。禁止任何形式的 mock / 写死数组 /
  前端自造统计数字。
- **`el-dialog` 默认允许点遮罩关闭**：禁止写 `:close-on-click-modal="false"`
  （详情/查看类弹框点弹窗外必须能自动关闭，关不掉很反直觉）。只有表单填写类弹框在用户明确要求防误关时才可豁免，且需在代码注释里写明原因。
- **表格行点击开详情时，操作列按钮必须 `@click.stop`**：否则点「编辑/删除」会同时触发行点击弹框（`el-table` 的 row-click
  冒泡自单元格内按钮）。行详情弹框一律只读（`el-form :disabled` + 页脚无确定键），编辑走独立按钮入口。
- 复用优先级：① `src/components/his/` 下已有骨架与业务组件（`Header` / `Sidebar` / `DashboardLayout` / `PatientSelect` /
  `InpatientOrderWorkspace` 等）
  ② 同一表单/工作区出现第 2 次就抽成共用组件（带 `mode` 属性区分角色，如 `InpatientOrderWorkspace.vue` 的
  `mode="doctor|nurse"`） ③ 最后才是页面内私有实现。
- **选患者一律用 `PatientSelect.vue`**（`v-model` + `@select`），禁止再手写 `el-select` + 搜索逻辑。
- **筛选条件多于 3~4 个时用全局类 `.query-grid-wrap` + `.query-grid`（口径见 `style.css`
  ，参照 `views/today-visits/TodayVisitsView.vue`）**：
  标准 HIS 查询条的做法是**每个条件固定宽 240px（70 标签 + 170 控件）、按剩余宽度堆放换行**，
  日期区间要放两个日期所以单独挂 `.is-daterange`（312px）。
  **禁止用 `1fr` 等分栅格拉伸控件**：窗口一宽，「号别」这种两字下拉会撑到 250px 以上（用户 2026-10-08 判为「太宽」）；
  反过来把控件写死成同一个 `!w-xx` 又会让日期区间显示被截断。列数交给宽度，**行首对齐交给固定格宽**。
  查询/重置放在栅格外侧的 `.query-grid-ops`（`flex-shrink:0`），靠右并与首行对齐，不要挂在最后一个条件后面。
- **分页查询只有两个旋钮，都在 `src/lib/pagination.js`**：`PAGE_SIZES = [10, 20, 50, 100]` 与 `DEFAULT_PAGE_SIZE = 10`。
  页面里禁止再写 `:page-sizes="[10, 20, 50]"` 这类字面量数组，也禁止再写 `pageSize: 20` 这类字面量默认值 ——
  一律 `:page-sizes="PAGE_SIZES"` + `pageSize: DEFAULT_PAGE_SIZE`，这样「某页 10 条太少」时只改一处就全站生效。
  **例外**：一次性抓全量/探总数用的 `pageSize`（下拉候选 `pageSize: 200`、统计 `100`、只要 total 的 `1`）不是分页查询，
  保持原值，因为它们不受用户翻页控制；小程序端另有自己的口径。
- **弹层（popper）样式必须写全局 `src/style.css`**：`el-select`/`el-dropdown`/`el-date-picker` 的 popper teleport 到
  body，组件 `<style scoped>` 里的 `:deep(.xxx)` 编译后带 `[data-v-x]` 前缀，body 下没有祖先命中 → 规则被**静默丢弃**
  （现象是"样式没写对"）。下拉加宽还要配 `:fit-input-width="false"`。
- **`el-option` / `el-radio` / `el-radio-button` 的 `value` 禁止绑 `null`/`undefined`，「不限/全部/自动判定」一律用非 nil
  哨兵 + 边界映射**：
  EP 侧 `value: { type: [String, Number, Boolean, Object], required: true }`，绑 `null` 报
  `Invalid prop: type check failed for prop "value"`，绑 `undefined` 报 missing required prop，而 `el-radio` 更坏 ——
  它的 `isPropAbsent` 就是 `isNil`，nil 会被当成「没传 value」而退回**已废弃的 `label` 兜底**并告警。
  做法：选项写哨兵（`value="ALL"` / `const FLAG_AUTO_JUDGE = -1`），**只在发请求前那一行**映射回契约值
  （`admitStatus: query.admitStatus === 'ALL' ? null : query.admitStatus`），回显时把接口的 `null` 归一成哨兵
  （`r.abnormalFlag ?? FLAG_AUTO_JUDGE`）。HTTP 出参形状必须逐字不变 —— 用抓包（hook `XHR.send`）核对，别只看界面。
- **行数不可控的可编辑表格必须分页 + 过滤**：一行配一个 `el-select`（科室下拉还要摊平上百个
  option）时，整表渲染开销随行数线性炸掉 ——
  岗位配置表在演示账号上实测 **1692 行**（94 科室 × 18 角色），点「编辑」后主线程冻几十秒，现象与「后端挂了」完全一样
  （而 `/system/user/getById` 只花 30ms；排查时先用 node 直调接口测耗时再归因，别急着怀疑后端）。
  做法见 `components/his/EmployeePostTable.vue`：**分页**（`PAGE_SIZES`/`DEFAULT_PAGE_SIZE`）＋ 渲染上限；
  **不要加搜索/过滤框**（用户 2026-09-25 明确要求撤掉，配置表就靠翻页看）。
  行内改/删/勾选**必须用行对象上带的全局下标 `__index`**，不能用 `$index` —— 分页后 `$index` 是页内序号，会改错行。
  「主岗位」这类一人只该有一条的开关用 `el-checkbox` 而不是 radio：radio 无法取消勾选，用户要点掉就得先选别的，
  实测很反直觉；改成可自由勾选 + **保存前用 `lib/employeePost.js` 的 `checkPosts` 拦住「一条都没勾」**
  （后端 `replacePosts` 全 0 时兜底提第一条只是 API 直连的最后一道闸，不替代前端提示）。
  只读列表同样要截断渲染（顶栏「切换岗位」只渲染前 50 条并显示总数）。
- **深色底上的 `el-icon` 不能用 Tailwind 透明度色**：Element Plus 的 `.el-icon{--color:inherit; color:var(--color)}` 与
  `text-white/70` **同特异性但后加载**，会把工具类盖掉 → 图标退回继承的黑色。要么给容器兜 `color:#fff` +
  `:deep(svg){fill:#fff}`（见 `Header.vue` 的 `.breadcrumb-nav`），要么用不带 `/` 的纯色。
- **API 层函数签名是前后端的稳定契约**：后端接口改造时只改 `src/api/*.js` 的内部实现，保持导出函数名与入参形状不变，视图层零改动。
- `request.js` 强耦合 `{code,message,data}` → **SSE / 流式接口必须用原生 `fetch`**，不能复用该 axios 实例。
- 主题只动 `src/style.css` + `src/components/his/` 三个骨架，**60 个业务页面不动**；主色 `#1269B5`、辅色青绿 `#0E9488`
  ，风格基调"稳重克制"。
- 前端验证用 playwright-core + Edge 真实浏览器，且**必须断言真实接口回来的数据**；禁止靠断言 ModulePage
  的假数据通过验收。控制台零报错才算通过。

## 3. 后端数据格式化铁律（G12/G14 连踩两次，勿再犯）

- **`LocalDateTime` / `LocalDate` 入参必须宽进**：DTO 字段加
  `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")`（LocalDate 用 `yyyy-MM-dd`）。
  一旦声明了该 pattern，Jackson 就**只认空格分隔**，前端若传 ISO `T` 分隔（`2026-09-23T17:32:16`）
  会直接 400「请求体格式不正确，无法解析」且零堆栈线索。两侧必须对齐：
  后端 pattern 是空格格式 → 前端 `el-date-picker` 的 `value-format` 与验证脚本一律传
  `yyyy-MM-dd HH:mm:ss`，**不传** `toISOString()` / `YYYY-MM-DDTHH:mm:ss`。
- **日期字符串边界**：SQL 里 `datetime_col <= 'yyyy-MM-dd'` 会把当天全部时点滤掉
  （datetime 恒大于当日 00:00:00 字符串）。按日期过滤必须补全天边界
  `endDate + ' 23:59:59'`（或 `< DATE_ADD(endDate, INTERVAL 1 DAY)`）。
- **mysql2 出参**：DATETIME/DATE 列在 Node 侧是 JS `Date` 对象（`supportBigNumbers` 只影响 BIGINT）——
  对它 `String(v).slice(0,16)` 会得到 `"Wed Sep 23..."`。要日期文本用
  SQL `DATE_FORMAT(col, '%Y-%m-%d %H:%i:%s')` 别名输出，或脚本侧 `d2str()` 格式化。
  BIGINT 一律字符串化比较（Java 侧 `ToStringSerializer`，Node 侧 `supportBigNumbers: true,
  bigNumberStrings: true` 双开）。
- **裸 SQL（尤其跨模块 `@Select`）先 `information_schema.COLUMNS` 对列名再写**：
  select 了表里不存在的列（如 `sys_treatment_item` 并没有 `unit` / `spec`），编译不报错，
  运行时 `Unknown column` 被全局异常处理兜成 500，现象与业务失败完全一样（G15 已踩）。
- **写库的"原因/异常"文本一律先截到列宽**：把原始数据库异常拼进 `VARCHAR(500)` 的原因列，
  超长时报 `Data too long`，结果是**"记账失败"升级成 500，用户连失败原因都看不到**。
  另：`NOT NULL` 且无默认的列（如 `biz_charge_detail.source_no`）传 null 会让整条 insert 失败，
  写 gateway / biller 时先确认每个必填列都有值或兜底。
  **这类字段不要在 DTO 上再加 `@Size(max = 列宽)`**：入参层的 400 会抢在服务端截断之前，
  等于把"用户粘贴了一长段说明"变成请求失败（L13 变异原因列宽 200、`@Size(255)` 拦下 300 字，
  服务端那段 `cut(reason, 200)` 永远跑不到）。必填靠 `@NotBlank`，长度靠截断。
- **新写接口的入参格式先探后端 pattern 再写脚本/前端**：验证脚本假 PASS 常见于
  「请求体解析失败被当成『接口正确拒绝』」——断言 `code !== 200` 前先确认失败原因是业务校验
  而不是格式解析（看 message 是否为「请求体格式不正确」）。
- **唯一键不含 `del_flag` 的表，整体替换/删除必须物理删**（L12 踩坑，配置页必现 500）：
  `BaseEntity` 的 `del_flag` 带 `@TableLogic`，所以 `remove(wrapper)` / `removeById()` **一律是软删**，
  留下的行仍占着 `UNIQUE KEY`。于是「先清旧行再插新行」的整表替换写法（角色配置、字典覆盖、
  模板明细、成员绑定）第二步必然 `Duplicate entry`；按 code 删了再新增同一 code 同理。
  做法：给 Mapper 加显式 `@Delete("DELETE FROM ...")` 的 `purgeXxx` 方法并在注释里写明撞的是哪个键；
  **纯配置/关联表没有留档价值，不要为了"能回滚"舍不得物理删**。建表时就二选一：要么唯一键带上 `del_flag`，
  要么在 SQL 注释里写死"本表删除走物理删"。
- **`INSERT ... VALUES (...) AS new ON DUPLICATE KEY UPDATE` 里的旧值列必须带表名限定**：给新行起了
  `AS new` 别名之后，裸列名会同时命中新旧两行 → MySQL 报 `Column 'x' in field list is ambiguous`，
  被兜成 500「系统内部错误」。而且**只在命中唯一键（走更新分支）时才报**：纯新增的月份跑得好好的，
  一到已入账的月份就炸，现象跟「业务闸门 SQL 写错」毫无相似之处。写 `IF(表名.col = 2, 表名.col, new.col)`。

## 4. 鉴权与按钮级权限（G5b 已踩，勿再犯）

- **`@PreAuthorize` 不要挂在 Controller 类上，一律标到方法**：类级注解会**静默覆盖**所有
  没写自己注解的方法。`/system/menu/userMenus` 是**所有角色**画侧边栏的入口，被类级
  `system:menu:list` 罩住后医生/药剂师/前台导诊等非管理岗一律 403 —— 现象是「点切换角色就被踢回登录页」。
  新增 Controller 方法必须显式标注；只要求登录的公共接口写 `@PreAuthorize("isAuthenticated()")`。
- **401 与 403 在 `src/api/request.js` 里是两条路**：401 = 身份失效 → 清 token 回登录页；
  403 = 身份有效但当前角色碰不得 → **只弹提示，绝不清 token**（一个越权请求把人整个踢出登录是事故）。
- **按钮级权限用全局指令 `v-perm`**（`main.js` 注册，数据源 `lib/perm.js` ← `/auth/info` 的
  `permissions`，即 `sys_menu.menu_type=3` 按钮码按当前角色算出的集合）：
    - `v-perm="'opd:appointments:add'"` 或 `v-perm="['a:add','a:edit']"`（任一命中）；
      tab/区块级显隐用 `v-if="hasPerm('x:y:add')"`（指令移除节点对 `el-tabs` 子组件不友好）。
    - **码必须逐字取自 `sys_menu`**：`sys_menu` 里没有的码 = 任何角色都拿不到 = 按钮永久消失。
      接线前先跑一次对齐核对（前端引用的码 100% 能在 `menu_type=3` 里找到才算完）。
    - **新增/修改在后端是同一个 `xxxUpsert` 接口、同一个 `:add` 权限**（如 `system:role:add` 既管新增
      又管修改），所以前端「编辑」按钮挂 `:add` 是**正确的**，不是凑合 —— 前端口径永远跟后端接口的
      实际要求走，不要凭空造后端没有的 `:edit` 码。
    - 集合还没到 / 拉取失败时 `v-perm` **放行不收敛**（宁可不隐藏），删掉的节点留注释占位符并订阅
      集合变化 —— 切角色时落地页与当前页相同则组件实例不重建，真删掉的按钮再也回不来。
- **按钮码铺底见 `sql/100`**：规则是「凡被授某页面的角色，自动获该页面全部按钮码」，
  因此接线的当下对所有岗位**行为中性**（不会出现谁突然少了按钮）。真正的开关在
  「系统管理 → 角色管理 → 菜单权限」里取消勾选某个按钮，届时前端隐藏 + 后端 403 同时生效。
- **铺底 SQL 的 `sys_menu` id 段必须与同期文件错开，守卫要三道键判重，跑完必须回查**：
  dev 库是多个会话共用的，`INSERT ... WHERE NOT EXISTS (id OR menu_key)` 撞到别的文件已占用的 id 时是
  **静默跳过（0 行）**，SQL 全程无报错，而后果是「按钮码任何角色都拿不到 = 写操作永久 403」，
  排查时很容易误判成 Redis 缓存或后端 bug。做法：守卫写成 `NOT EXISTS (id OR menu_key OR permission)`、
  授权行的 id 基数用本文件专属段（不要在 8900…020 这类公共低位段里挤），跑完立刻
  `SELECT id, permission FROM sys_menu WHERE id IN (...)` 确认真的落进去，再清 `his:perm:role:*` 缓存
  （TTL 10 分钟，不清就是旧集合）。
- **通用参照数据不配权限码**（科室、角色、字典、药品/项目/ICD 等 `selectList` 接口）：
  这类下拉被跨岗位页面复用（前台挂号要选医生所在科室、库房要选药品、病历要搜 ICD），
  一旦挂上 `hasAuthority('system:xxx:list')`，非管理岗一进页面就 403，
  现象是「下拉直接空掉、页面像崩了」，而这些又不是敏感数据。口径统一写
  `@PreAuthorize("isAuthenticated()")`。**权限码只留给「本页面独有的业务数据」**
  （如 `/pharmacy/wardDispense/candidates` 只服务摆药页，保留 `pharmacy:wardDispense:list`）。
- **字典类数据随登录接口一次带回，禁止首屏再请求全院字典**：顶栏的角色标签原先在
  `Header.vue` 的 `onMounted` 里拉 `/system/role/selectList`（全院角色表），既多一次请求
  又踩上一条权限。现改为 `/auth/info` 直接返回 `roleNames`（`[{roleCode, roleName}]`），
  组件里 `roleDict.value = res.data.roleNames`。判据：**只要某接口的唯一用途是「把我自己的
  编码翻译成名字」，它就该并进 `/auth/info`，而不是留在页面挂载时请求**。

## 5. 敏感字段脱敏（手机号 / 身份证 / 邮箱 / 医保卡号）

- **脱敏只能在后端做，前端不许有任何遮码实现**：只在前端 mask 等于没做 —— 明文仍在响应体里，抓包、日志采集、
  接口复用到第二个页面（忘记调 mask 的那个）都会漏。展示型接口出参时就打码，页面只渲染后端给的 `xxxMasked`。
  `src/lib/patientField.js` 里的 `maskMiddle/maskPhone/maskIdCard/maskCardNo/maskInsuranceNo` **已删除**，
  别再抄回来（哪怕只写一个 `|| maskIdCard(p.idCard)` 兜底也算违规，而且它会因为没 import 在渲染期报
  `Property "maskIdCard" ... not defined`）。
- **口径统一走 `his-common/support/SensitiveMaskUtils`**（`maskPhone` / `maskIdCard` / `maskEmail` / `maskMiddle`）：
  保留前 N 后 M、星数 = 被遮位数、总长度不变。禁止各页面、各 Controller 再抄一份（抄了就会漂移：
  同一个人在列表里遮成 `1101**0011`、在档案里遮成 `1101**********0011`，看着像两个号）。
- **⚠ 按「调用用途」而不是按 URL 决定遮不遮**：只有**编辑回显**（表单数据源，如 `/patient/getById`）必须保持明文 ——
  前端表单是 `Object.assign(form, res.data)` 后整对象 `xxxUpsert` 回写，
  一旦回显的是 `188****5878`，下一次保存就把库里的真号洗成了星号（不可逆数据损坏）。
  纯展示的详情（如 `/patient/getDetailById`，只喂患者档案弹框）**照样要脱敏**。
  「本人看自己」的场景另开只读接口（如 `/system/user/selfProfile`，userId 取自 token、不接收参数）。
- **脱敏要成对，且改完必须回头确认它的编辑入口**：明文字段置 `null` + 另给 `xxxMasked` 字段
  （`phoneMasked` / `idCardMasked`），前端渲染 `row.xxxMasked`；
  改了哪个字段的出参，就顺手确认所有读它的页面是否已换成 `xxxMasked`、以及它的编辑入口是否走 `getById` 补全量。
  漏一处的现象是**整列变成空白**（不是显示明文），比没脱敏更难发现。

## 6. 岗位（角色 × 科室）= 身份的唯一口径（sql/107 起，勿再单边切）

- **身份 = 一行岗位**（`sys_employee_post`：`employee_id` + `role_id` + `dept_id`，唯一键 `uk_emp_role_dept`）。
  「在骨科是医生」不代表「在康复科也是医生」，组合由管理端**分配岗位时**定死，界面只负责选。
  顶栏只有「切换岗位」一个入口；`/auth/switchRole`、`/auth/switchDept` 已删除，不要复活。
- **`sys_employee_role` 镜像表已删除（sql/118）**：鉴权（角色集合、菜单/按钮权限 join）直读 `sys_employee_post`，
  不要再复活镜像 —— 旁路直插岗位表曾会让「菜单按镜像画、数据范围按岗位取」错配且不报错。
  `sys_employee.dept_id/dept_name` 仍是「主岗位所在科室」的快照，唯一写入方是 `EmployeePostService.replacePosts`。
- **岗位状态是派生值，不落列、不建定时任务**：`EmployeePostVO.postStatus`（1-在职 2-已失效）由 expireDate 与当天现算。
  加列只会多出「日期已过、状态还没刷」的漂移窗口；「已失效」的唯一事实来源永远是 `expire_date`。
- **硬不变量：每个人恰好一条 `is_primary=1`**（主岗位是**人**的定位 = 人事主科室，不是每个角色一条）。
  写入归一化在 `replacePosts`（多勾只认提交里的第一条，一条都没勾就把第一条提上去），存量在 `sql/107` 第 6 步回填。
  曾经按「每角色一条」存，结果管理员在配置表里排出一排「主岗位」，连管理员都要先问"这是什么" —— 人事没这个概念。
  非主岗位角色的**登录落点**由 `resolvePrimaryPost` 现算：该角色下「与主岗位同科室」的那条优先，没有才退该角色第一条
  （一人双岗多半在同一科室执业，登录后不该被扔到陌生科室）。
  存量回填的定序必须**先认员工现有主科室**（`(dept_id = sys_employee.dept_id) DESC`），否则全员主科室会漂到
  dept_id 最小的那个科室上。
- **`dept_id NOT NULL`**：「全院岗位」用 `dept_id=NULL` 表达已否决 —— 与 `sys_role.data_scope=1` 能力重复，
  且 `JwtAuthenticationFilter`「token 无 deptId → 退回库里主科室」根本表达不出 NULL；
  更要命的是受限角色挂上空科室会让 `DeptScopeGuard.allowedDeptIds()` 兜底返回 null = 不收口 = 看全院（越权）。
- **`posts` 字段的 `null` 与 `[]` 是两件事**：`null`=本次没提交岗位 → 原样保留；`[]`=显式清空。
  医生名册切启用/禁用只传 `{id, status}`，把 `null` 当空列表会把这个人所有岗位和角色物理删掉（本表无 `del_flag`，删即真删）。
- **切换一律 fail-closed**：`/auth/switchPost` 必须同时给 `roleCode` + `deptId`，且这一行岗位确实存在，否则拒发 token 并写审计。
  数据范围随岗位收口（`deptIdsOfEmployee` 按**当前角色**取，不是所有角色的并集），前端不传科室参数、也不给「全部科室」开关。
- **配置入口只有两处**，共用 `components/his/EmployeePostTable.vue` + `lib/employeePost.js`：
  系统管理→用户管理、系统管理→员工档案。别处再要配岗位就接同一套组件，不要各自写表。

## 7. 收费四层铁律（sql/125 起，任何碰钱的需求都按这条链落位）

真实 HIS 的收费域是**四层一条链**，每层只回答一个问题、只有一张事实表。旧实现把四层压进
`biz_charge_info` 一行（应收 `total_amount` + 实付 `actual_amount` + 单值 `payment_method` +
跨三层的 `charge_status` + 票据 `invoice_no` + 资金镜像 `refund_amount`），后果全都显性发生过：
日结只能按状态列反推、医保统筹借用"优惠金额"列、现金+医保+余额组合支付表达不出来、
签到要读支付状态所以免收必须造 0 元单。

| 层        | 表                                                                                  | 只回答                        | 唯一写入方                                         |
|----------|------------------------------------------------------------------------------------|----------------------------|-----------------------------------------------|
| L1 记账    | `biz_fee_record`                                                                   | 谁该付多少钱、这笔从哪张临床单据来          | `FeeRecordService`                            |
| L2 结算    | `biz_settlement_bill` + `_item`                                                    | 这批应收合计多少、优惠/医保 split/应缴各多少 | `SettlementBillService`                       |
| L3 支付    | `biz_payment_txn`、`biz_fund_account` + `_txn`                                      | 真金白银进出：几笔、走哪个渠道、流水号多少      | `PaymentService` / `FundAccountService`       |
| L4 票据与对账 | `biz_invoice`、`biz_cashier_settlement`、`biz_day_settlement`、`biz_pay_channel_bill` | 凭证与核对：账实是否相符               | `InvoiceService` / `FinanceSettlementService` |

铁律（违反即回到老模型那几个已知缺陷）：

- **L1 记账行不可修改，错账只能红冲**：金额列一经写入不再 `UPDATE`；冲正=写一条负数行
  （负行用 `orig_fee_id` 指向原行，原行金额与状态都不动）。部分冲减可以有多笔负行，
  应收净额永远 `SUM(amount)` 现算；**只有整行冲完时**才把原行和它名下所有负行一起置
  4-已红冲（不置 4 会让负行单独留在待结算里，净额直接算成负数），并把原行 `orig_fee_id`
  指向最后一笔负行。禁止"红冲原行 + 新记剩余数量行"那种拆行：拆出来的剩余行既不是临床上
  发生的费用，也会让幂等判重把后续执行当成重复记账。
  就地 `update refund_quantity` 等于销毁"这笔费用历史上是多少"，追溯与医保核查无从下手。
- **L2 优惠与医保 split 分列，不许互相借用**：`discount_amount` 只放院内优惠/抹零；
  统筹 `pool_amount`、个账 `account_amount`、自付 `self_amount` 各一列。
  把统筹塞进优惠列（旧 `ChargeController.java:467`）会让真优惠无处安放，读取侧只能
  `insurancePay = discountAmount` 猜，且收费员现金清点必然多出一块说不来的差额。
- **统筹不是支付方式**：`PaymentMethodEnum` 里没有"统筹"，它是后付给医保局的钱，
  走账单 `pool_amount` + 报盘台账；`4-医保个账` 才是刷参保人卡扣的额度，是一笔真实收款流水。
- **L3 一笔钱一行、收退同表带符号**：`direction` 1-收 2-退，`amount` 收正退负，
  日结 `SUM(amount)` 即净额。账单是否付清由 `SUM(成功收款流水) >= payable_amount` 现算，
  不允许"点一下按钮翻状态"。一账单多笔、多渠道组合支付是这个层的常态而非特例。
  流水永不删除、金额永不 UPDATE，冲正翻 `txn_status=2` 并另起反向流水。
- **L4 的对账事实只能来自 L3**：班结/日结/渠道勾对的聚合源是 `biz_payment_txn`，
  不是任何状态列。渠道对账勾 `biz_pay_channel_bill.local_txn_no ↔ biz_payment_txn.channel_txn_no`
  （旧口径勾的是 `charge_no`，方向就是错的，且表达不出"渠道退了一笔"）。
- **临床状态不得依赖支付状态**：签到、发药、执行判断"费用已结清"时，
  读的是账单 `bill_status`（或"该就诊下有未结账单"），绝不能读收费单状态列——
  否则免收、0 元单、后付费这些路径全要在支付层造假数据去满足临床门禁。
- **`encounter_type` 不是 `visit_type`**：收费域用 encounter（1-门诊 2-住院）表达费用归属；
  `visit_type` 在本项目已是初诊/复诊，`biz_visit` 是另一张就诊次表。三个词混用必然查询错表。
- **字典权威在 Java 枚举**（无启动期自检）：`FeeStatusEnum`/`BillStatusEnum`/`PayDirectionEnum`/
  `PayTxnStatusEnum`/`TxnSourceEnum`/`AccountOwnerTypeEnum`/`AccountTxnTypeEnum`/`FeeSourceTypeEnum`/
  `BillTypeEnum`/`EncounterTypeEnum` 在 `his-common/enums`，改码值必须同步改 `sql/125` 的字典段。
- 旧表 `biz_charge_info`/`biz_charge_detail`/`biz_refund_info`/`biz_refund_detail`/`biz_prepay`
  在四层链路验收通过后退役（迁移与 DROP 在 `sql/126`）；**新代码禁止再往旧表写**，
  也禁止"顺手在旧表加一列"。`biz_refund_apply` 保留（它是红冲的前置审批单，不是资金事实）。

## 8. 药品库存两层库位与三级逆向链（sql/154 起，任何碰药的库存需求都按这条链落位）

药品原先只有一本 `biz_drug_stock` 流水账：发药有 `biz_drug_dispensing`、出库有 `biz_drug_outbound`，
但**全链路没有任何反向单据** —— 退费四层已支持红冲，药却冲不掉，账实必然不符。现按三级链补齐：

| 级 | 场景            | 单据                                   | 流水类型（`DrugStockChangeTypeEnum`） |
|---|---------------|--------------------------------------|---------------------------------|
| ① | 患者退药          | `biz_drug_dispensing` 置 3-已退药（不另建表）  | 3 退药回库（正，落药房）                   |
| ② | 药房退回药库 / 药库下拨 | `biz_drug_transfer` + `_item`        | 7 调拨出（负）+ 8 调拨入（正）              |
| ③ | 供应商退货         | `biz_drug_supplier_return` + `_item` | 9 退货出库（负）                       |

- **`biz_drug_stock.stock_room` 是库位的唯一事实来源**（1-药库 2-药房，`StockRoomEnum`，字典 `his_stock_room`）。
  发药/锁库/退药回库/FEFO **一律只在药房侧**；`biz_drug_stock_log` 表本身**不存 `stock_room`**，
  要看库位就 JOIN 批次表（批次是唯一事实，冗余到流水会漂移）。
- **想让药进药房只有一条路：调拨单。** 库存新增接口不再接受"直接建药房批次"的口径（在途可见），
  采购入库落药库层。供应商退货**只能退药库批次**（药房的货必须先调拨回药库）——
  因为铺底事实是药库批次全挂 `supplier_id`、药房批次全不挂。
- **调拨一张单必出两行流水、合计 0**：所以按单据捞（`selectBySource`）而不是按批次捞，
  才能一眼看出"搬出去了还没搬进来"。状态机 1待发出→2待接收→3已完成 / 4已作废，退货 1待退货→2已退货 / 3已作废。
- **成本随货走、同批号按到货额加权**（`addStockToRoom`：`cost = totalAmount / quantity`，与采购入库同口径）。
  调拨不是新进货，但同一批号两侧成本已被各自入库拉出差异，沿用接收方旧成本会把真实成本差抹掉。
- **`stock_room` 与单据表族删除走物理删**（唯一键不含 `del_flag`，见各 Mapper 的 `purgeXxx`）；
  单据明细的删除同样不能软删，否则"删了重建同一批次"必然 `Duplicate entry`。
- **退费对账闸（药品侧 ↔ 收费侧）**：`PaymentServiceImpl.refund` / `closeBill` / `RefundApplyServiceImpl`
  发起前必须过 `SourceAdvanceServiceImpl.assertDrugReturnedForRefund` →
  `SourcePaidAdvanceServiceImpl.assertNoDrugPendingReturn`，只认 `dispensing_status = 2`（已发药未退药）即拒退。
  **退费只退钱、绝不替药师办退药**：`revertPrescriptionDetail` 只把 1-待发药 取消为 4-已取消，
  已发药(2)/已退药(3) 的行原样不动。不要为了"让退费跑通"去放宽这个闸或让退费自动回库 ——
  那等于把「钱退了、药还在患者手上」这个事实抹掉。

## 9. 模块包结构：模块 → 分层（二级强制，路径唯一）

- **路径恒为 `com.his.<模块>.<分层>[/impl].XxxYyy.java`，层级到此为止**：
  分层目录**必须**直接挂在模块根下（`com.his.pharmacy.controller` 是正确形态），
  分层目录下面除 `service/impl` 外**禁止任何目录**，模块与分层之间也**禁止**插入业务子域目录
  （`com.his.emr.appoint.pathway.dto` 这类三层包是违规）。
- **参照实现 = 任一模块**（全仓 16 个业务模块已按此形态收口），新代码照它落位。
- **允许的分层目录（名字逐字用）**：
  `controller` `entity` `dto` `vo` `mapper` `service`（实现类进 `service/impl`）`enums` `support` `config` `constant`
  `util`。
  **一个模块每层只有一个目录**，分层目录之外不留任何技术角色包（见第 11 节黑名单）。
- **service 一律「接口 + Impl」**：`XxxService` 接口在 `service/`，`XxxServiceImpl` 在 `service/impl/`，
  没有第三种形态 —— 既不允许只有具体类的 `XxxService`，也不允许找不到同名接口的 `XxxImpl`
  （跨模块 SPI 反转产生的 `XxxGatewayImpl/XxxProviderImpl` 是待拆除项，见第 11 节）。
- **为什么取消业务子域那一级**：业务归属是**模块**该表达的事，包路径里再嵌一层等于把同一个信息编码两遍，
  于是出现「`emr.appoint.pathway` 还是 `emr.pathway.appoint`」这类没有答案的排序问题；
  两级让「路径 = 模块 + 分层」成为唯一事实，而分层目录名固定在最后一级，是为了
  `@MapperScan("com.his.**.mapper")`、IDE 包折叠和跨模块 import 三条都仍然只靠路径就能判定 ——
  所以**挪包不用改扫描配置**，只要那一级目录名还叫 `mapper`。
- **觉得"这个模块太大、该再切一刀"= 该拆 Maven 模块**，不是该在建三层包。
  拆与不拆按实体维度与数据流判（同一实体维度/宿主数据流被切断就不拆），判据见仓库 git log 的模块拆分评审结论。
- **改包名必须同步的连带项**（漏一处就是运行期 `ClassNotFound`/`Invalid bound statement`，比业务 bug 更难查）：
  ① 全部 `import`（含 `import com.his.x.y.*;` 通配）与代码体内的全限定名；
  ② `src/main/resources/mapper/*.xml` 的 `namespace` 与 `resultType`/`parameterType`/`javaType`；
  ③ `@MapperScan` / `typeAliasesPackage` / `logging.level.com.his.*` 这类字符串配置。
- **挪包后的验收不认 `mvn compile`，只认 `clean package` + 真启动**：增量编译不清掉 `target/classes` 里的旧路径 `.class`，
  它们会被一起打进 fat jar，Spring 组件扫描按「类名首字母小写」生成 bean name，新旧两份同名 Controller 直接
  `ConflictingBeanDefinitionException` 炸在启动那一刻（编译期零信号）。所以必须
  `mvn -o -DskipTests clean package` 重新出包，起一个新端口跑 `Started HisApplication` 再打几个接口。
  同理 `mapper-locations` 必须是 `classpath*:`：`mapper/` 目录在多个模块 jar 里各有一份，
  单 root 的 `classpath:` 只扫 classpath 最靠前的那个模块，其余模块的 XML 语句全部
  `Invalid bound statement (not found)`。
- **改包绝不顺带改 URL**：`@RequestMapping` 是前后端契约（`src/api/*.js` 依赖），包结构是后端内部事务，
  改名、挪层、挪模块都不允许动接口路径。

## 10. 分层职责硬性约束（Entity / DTO / VO / Service / Controller）

- **五类各守其位，不许串门**：
  | 类型 | 唯一职责 | 禁止 |
  |---|---|---|
  | `entity` | 映射表（`@TableName`/`@TableField`）、字段与列对齐 | 承载查询条件、挂展示字段、内嵌业务方法 |
  | `dto` | 入参（`xxxUpsertDTO`/`xxxQueryPageDTO`/`xxxQueryDTO`）+ 校验注解 | 持有 entity、被当作出参回传 |
  | `vo` | 出参（`xxxVO`/`xxxDetailVO`/`xxxSelectListVO`） | 被前端传回来当入参 |
  | `service` | 业务逻辑、数据范围收口、entity↔DTO/VO 转换、事务 | 被 Controller 绕过直接组合 Mapper |
  | `controller` | 接 DTO → 调 service → 包统一响应 | 任何逻辑（见下条） |
- **entity 不出 Controller**：Controller 方法签名只能是 `@RequestBody xxxDTO` → `Result<xxxVO/xxxDetailVO/List<xxxVO>>`。
  把 entity 直接透出，等于把列名当 API 契约，改一列就要前端陪葬；把 entity 当入参则绕过全部参数校验。
- **Controller 出参只能是 VO，禁止 `Map<String, Object>` / `Result<Object>`**：
  唯一形态是 `Result<xxxVO>` / `Result<List<xxxVO>>` / `Result<IPage<xxxVO>>`。
  Map 的 key 是**隐式契约**：前端拼错 key 不报错、只渲染空白；改字段名时 IDE 不会带着前端一起改；
  Swagger 出参退化成 `{}`，前端联调只能靠抓包猜字段。`Map<String, Long>` 这类"看着有类型其实没类型"的同样禁。
  ⚠️ **「Map 在 Mapper 层、VO 在 service 层转换」这条路已经全部走完并封死**（2026-10-07 全仓清零）：
  「SQL 结果天然没有对应实体」不是理由 —— 那就建 `XxxStatVO` / `XxxCountVO` / `XxxTrendVO` /
  `XxxSnapshotVO`，一行多列也有对应结构。Mapper 层现在**直接返回 VO**，中间那层 Map 转换全部删除，
  连带把只服务它的 `toLong(Map,String)` 之类的工具方法一起删掉。统计/字典类接口本来就要有名字，禁止拿 Map 当 VO。
  真需要动态结构（交叉表列头由数据决定）时，用 `List<ColumnVO> + List<RowVO>` 这种**有类型的形状**表达，不要退回 Map。
  判据与豁免清单见本节末尾「`Map<String, Object>` 一律不许当数据契约用」那一条。
  机械判据：`grep -l "Result<Map\|Result<Object\|PageResult<Map" **/controller/*.java` 必须为空。
- **Controller 里禁止出现任何处理逻辑**：不写业务 if/else、不做状态兜底、不算统计、不拼多表结果、
  **不注入 Mapper**。方法体只允许「取 DTO → 调一个 service 方法 → 返回 Result」。
  机械判据：`grep -l "^import com\.his\..*\.mapper\." **/controller/*.java` 必须为空；
  出现 `for`/`stream` 聚合、`new XxxEntity()`、`if (dto.getXxx() == null)` 这类判断，就是逻辑漏在了 Controller，搬到
  service。
- **跨模块只走 service**（重申第 1 节）：A 依赖 B 只能 `@Resource BService`，禁止 A 调 B 的 Mapper，
  也禁止 A 复用 B 的 entity/DTO 当自己的接口契约（各模块自带一套 DTO/VO）。
- **入参校验的唯一归属地是 DTO 注解，service 里不写「不能为空」**（上条表格里 `dto` 那一行的展开）：
  `if (dto.getXxx() == null) throw new BusinessException("xxx不能为空")` 与 `@NotNull` 是同一件事写两遍，
  两套口径必然漂移（改了注解忘改 if，或反过来，且**有注解没 @Valid 时注解一句都不生效、零报错**，
  service 那份就成了掩盖缺失 `@Valid` 的遮羞布）。做法：注解上声明 + 中文 message 保留原文案，
  Controller 参数写 `@RequestBody @Valid xxxDTO`（`GlobalExceptionHandler` 已把校验失败兜成 `400 + message`，
  前端拿到的提示与原来一致），service 删掉那份重复判断。
  **三类必须留在 service**（不是漏改，是注解做不到，留在原地要在注释里写明属于哪一类）：
  ① **条件必填**——同一接口内按请求内容分支：upsert 的 `id==null` 走新增（admissionId 必填）/
  `id!=null` 走修改（不传 admissionId），「有不良反应时才必填描述」。`@NotNull` 是一刀切，
  加了就把合法的 update 挡成 400；要收口就在 DTO 上加 `@AssertTrue` 方法，不要写回 service。
  ② **非 web 入口的入参**——被其他 service 直接调用的方法（跨模块 SPI、`SignCommandDTO` 这类内部指令），
  Bean Validation 只在 HTTP 参数绑定时跑，内部调用根本不过这一层。
  ③ **业务规则**——状态机（「只有草稿可提交」）、码值合法性、关联实体存在性（「入院记录不存在」）、
  临床取值范围与单位提示。这些与"字段填没填"无关，DTO 注解无处安放。
  机械判据：`grep -A1 "if (dto.get.*== null\|if (!TextUtil.hasText(dto.get" service/impl/*.java`
  里抛「不能为空/必填」的行，除上述三类（带注释标注类别）之外为 0。
- **入参 DTO 对象本身一律不许判空兜底**（2026-10-10 全仓清理，`@RequestBody(required = false)` 18 处清零）：
  `TicketPageQueryDTO q = pageQueryDTO == null ? new TicketPageQueryDTO() : pageQueryDTO;`
  这类写法（含 `if (q == null) q = new Q();`、`Q q = query != null ? query : new Q();` 三种拼写）
  **在 web 链路上是死代码**：`@RequestBody` 默认 `required=true`，缺请求体由 Spring 抛
  `HttpMessageNotReadableException`（已兜成 400），GET/表单的 `@Valid XxxQueryPageDTO` 走
  `@ModelAttribute` 必然被实例化 —— 参数永远不可能为 null。而 `@RequestBody(required = false)`
  是它唯一的活口，等于「让空请求体静默变成一次默认分页查询」，把调用方漏传 body 的 bug 洗成
  「接口看起来能用」。口径：**`required = false` 一律删掉**（本仓 18 处已清零），空请求体 = 400；
  默认值由 `PageParam` 的 getter 夹取提供，不需要在 service 里 `new` 一个空 DTO 去兜。
  危害不是冗余，是**同一接口两种语义**：漏 body 的调用方拿到第 1 页数据而不是错误，
  于是「前端没传筛选条件」在联调时看不出来，而 service 里那份判空还会诱导后来人以为
  这个方法有非 web 调用方，往 true 方向继续加兜底。
  **只有第②类（非 web 入口）才留**，且必须带注释写明：本仓仅剩 `FeeBookDTO`（跨模块记账 SPI）、
  `SignCommandDTO`/`QcExecuteDTO`（内部指令）、`StockBatchMoveDTO`（调拨/退货 service 现造）、
  `TechAuthGateDTO`（技术授权闸门内部入参）、`PatientSearchScopeDTO`（重载显式传 null 的契约）。
  合并式写法 `if (dto == null || dto.getBillId() == null) throw` 只删前半截，字段级校验原样保留。
  机械判据（三条都必须为空）：
  ```bash
  grep -rnE "\w+\s+\w+\s*=\s*\w+\s*(==|!=)\s*null\s*\?" --include=*.java source/back_end        # 三元兜底
  grep -rn -A2 "if (\w+ == null) {" --include=*.java source/back_end | grep "new [A-Za-z]*DTO"  # 块内重建
  grep -rn "RequestBody(required" --include=*.java source/back_end                              # 活口本身
  ```
- **查询接口的 GET/`@RequestParam` 不做"必填装饰"**：`@RequestParam` 默认 `required=true` 已经是必填
  （缺参由 Spring 抛 `MissingServletRequestParameterException`），不要再补 `if (x == null) throw`；
  要收口分页参数就放在 `PageDTO` 基类的 `@Min/@Max` 上。
- **校验只在「参数绑定」那一刻发生，三条绑定路各有各的异常**（选错机制等于校验静默失效）：
  ① POST JSON `@RequestBody @Valid xxxDTO` → `MethodArgumentNotValidException` → 400 + 中文 message（已接）。
  ② GET/表单绑定的对象参数（不写注解即 `@ModelAttribute` 形态，如 `listPage(@Valid XxxQueryPageDTO)`）→
  `BindException` → 400（已接）。**注意 `@Valid` 挂在对象参数上才会级联校验它的字段**，只给字段写注解、
  参数不加 `@Valid`，注解一句都不跑。
  ③ 直接标在标量参数上的约束注解（`@RequestParam @NotBlank String x`）**不要这么写**：类上没 `@Validated` 时
  Spring 抛 `HandlerMethodValidationException`，有类级 `@Validated` 时走 AOP 抛 `ConstraintViolationException`
  ——`GlobalExceptionHandler` 这两个都没接，兜底成 500「系统内部错误」，用户看不到是哪条约束没过。
  标量参数必填只用 `@RequestParam`（缺参异常已接），复杂条件一律收口成 DTO。
  全仓 `@Validated` 已清零（参数级统一成 `@Valid`；唯一的类级 `@Validated` 也删了——它在这个 Controller 上
  只等价于「给标量约束埋 500」，`@RequestBody @Valid` 和 `@Valid 对象参数` 都不依赖它）。
  两者在 `@RequestBody` 上都生效（`ValidationAnnotationUtils` 认 `Validated` 与 simpleName 以 `Valid` 开头的注解），
  但只用 `@Valid` 这一种写法，避免类级注解带来的隐式 AOP 校验。
  机械判据：`grep -rn "@Validated" --include=*Controller.java source/back_end` 为空。
- **DTO 主键字段是 `String` 的，一律当 Bug 查**：这不是「防精度」的必要写法，而是第 1 节那条
  「主键入参一律 `Long`」被写歪了（出参的精度要求漏到入参上）。
  `grep -rn "private String id;\|private String faqId;" --include=*DTO.java source/back_end`
  应为空；命中就按第 1 节的形态改 `Long + @NotNull`，并删掉 service 里配套的
  `parseId` 与 `if (id == null) throw`。
  **豁免**：对接外部厂商 API 的响应体（字段由对方定义，如 `OpenAiChatResponseDTO.id` 是 OpenAI 的
  `chatcmpl-xxx` 字符串）不是我们的入参，不在判据内 —— 别照着判据把它改成 Long，那会破坏对接。
- **`Map<String, Object>` 一律不许当数据契约用（2026-10-07 全仓清零，362 处 → 15 行豁免）**：
  Map 的 key 是**隐式契约** —— 拼错不报错、只渲染空白；改字段名时 IDE 不会带着调用方一起改；
  Swagger 出参退化成 `{}`。所以：
  | 场景 | 唯一正确形态 |
  |---|---|
  | Mapper 裸 SQL 返回单行快照 | 行的列就是某表 → 直接用该表 entity；只取部分列 → 建 `XxxSnapshotVO` |
  | Mapper 裸 SQL 返回 `List<Map>`（group by / 趋势 / TOP N） | 建 `XxxCountVO` / `XxxStatVO` / `XxxTrendVO`，**字段名写全
  **，禁止 `k`/`c`/`d`/`n` 缩写 |
  | `new LinkedHashMap<>(){{ put(..) }}` 双花括号拼 JSON | 建 `XxxPayloadVO`；**JSON 键名一字不改**（前端契约） |
  | 外部报文（疾控报卡、医保 2304/2305、微信模板消息） | 建 VO；Java 关键字做字段名用 `@Alias("class")`（Hutool）/
  `@JsonProperty`（Jackson） |
  | service 里 `row.get("x")` 强转取值 | 随 Mapper 一起改成 `row.getX()`，**并删掉只服务 Map 的转换工具**（
  `toLong(Map,String)`/`asLong(Object)`/`nz(Map)`/`decimal(Object)`）—— 改完必零引用，属第 18 节零引用删除范围 |
  | 局部 `Map<K,V>` 做分组聚合（`Map<Integer,Long> typeCount`）、JWT claims | **保留** —— 是真字典不是数据契约 |
  | 跨模块 SPI 边界且对端按 key 动态索引（见下方豁免） | 实现方**内部出参全改有类型 VO**，只在 `return` 一行做 VO→Map
  适配 |

  **两条配套铁律**：
  ① **SQL 列别名必须与 VO 字段名逐字一致**（MyBatis 按列名映射）。`AS k` 改成 `AS followupType` 后
  VO 字段也得改名。别名别用 `count`/`key`/`value` 做 `ORDER BY` 目标（MySQL 内置含义），用 `cnt` 更安全。
  ② **改 VO 后 DATETIME 给 `LocalDateTime` 字段，不再 `DATE_FORMAT` 成字符串** —— 那是为绕开裸 Map
  取日期对象强转才那么干，换成有类型的类之后 MyBatis 自己映射。唯一例外：小程序端出参是
  `yyyy-MM-dd HH:mm:ss` 文本的字段保持 String（改 LT 会变 ISO `T` 分隔 = 破坏出参契约）。

  **豁免（当前仅存 15 行，逐条有据，不是漏改）**：
  ① `WorkbenchMetricProvider.summary()` + `WorkbenchDataVO.data` —— 前端 `MetricWidget.vue` 用
  `props.data?.[item.key]` **按 key 索引**，且 key 集合由前端 `workbench-widgets.js` 的 `METRIC_SPECS`
  加可增删的 `sys_workbench_widget` 卡片注册表决定 = 真动态结构，改它要同时动前端 + 7 个实现方；
  ② `OperationQaCapabilityImpl` 的 `queryForList` —— SELECT 列由**模型按问题现场决定**，编译期不存在任何 VO 可建；
  ③ `PromptTemplate.render(String, PromptVariables)` —— 模板占位符天然动态，各能力改用
  `XxxPromptVariablesVO implements PromptVariables`，`toMap()` 由 `BeanUtil.beanToMap(this)` **从字段名反射**
  （人手写键名 = 原地搬 Map，收益归零）；
  ④ `JwtUtils` 解析 claims —— jjwt `claims(Map)` 的 API 形状，标准 claims 大量可选，收成类更脆。

  机械判据（排除 import 与注释行后应只剩上述豁免）：
  `grep -rn "Map<String, *Object>" --include=*.java source/back_end | grep -v "import \|:\s*\*\|:\s*//"`
  Controller 层与 DTO 层必须**各为 0**：
  `grep -rln "Map<String, *Object>" --include=*.java source/back_end/*/src/main/java/*/controller/`。

- **合并 / 共用 VO 只并「类型」，绝不并「查询宽度」**（2026-10-08 用户口径）：「x 接口和 y 接口返回同一个 VO」
  ≠「y 要把这个 VO 的字段全查出来」。**哪些字段该查，唯一依据是前端那一页实际渲染的列**，不是出参类的字段表。
    - **合法形态 = 一个类、多个 mapper 方法**：每个接口用自己的私有 `toXxxVO(row, …)` 只填本页读的列。
      参照 `StaffScheduleServiceImpl`：`onDutyAt` 走 `toOnDutyVO`（14 列，分诊台只显示 10 列），
      `listPage` 仍走全量 `toVO`，**两边的查询与处理逻辑互不改动**。
    - **禁止**为"共用一个类"去动另一侧的查询逻辑，也**禁止**往窄接口塞它不展示的字段 —— 窄列表被摊宽后
      每次刷新都多载一倍正文，而**现象是零报错**，只有响应体和序列化开销掉下去。
    - **合并后必自查逐行开销**：窄链路最容易经共用类把「逐行字典翻译 / 额外 JOIN」带进来。实锤：
      `CssdTemplateServiceImpl.selectList` 原先 `BeanUtils.copyProperties(toVo(t, null), vo)`，
      为一个前端不渲染的 `sterilizeMethodText` 每行跑一次 `dictCacheService.getDicDataLabel` → 改三个直接 setter。
      **判据：下拉/列表接口的出参里有 `xxxText` 而页面不显示 = 一定在白跑查询。**
    - **共用类之间搬字段禁用 `BeanUtils.copyProperties`**：属性拷贝把源类的宽度自动带过来，源 VO 将来加一列，
      窄接口就无声变宽。显式 setter 才会让「谁填了哪些列」留在代码里。
    - **验收 = 实测响应 key 集**（脚本按接口打 `Object.keys(data)` 并分区「有值 / 为 null」，见
      `workspace/_verify_vo_narrow.mjs`），`clean install` 通过不代表宽度没变。
    - 共用类让窄接口外泄一批 `null` key 时（本仓无全局 NON_NULL 约定，Jackson 照出 null 字段名），
      **三个选项摆出来让用户定，不许自己拍**：(a) 接受（key 名多，值仍是本页列）
      (b) 该 VO 加 `@JsonInclude(NON_NULL)`（会同时改掉其他接口的 key 契约）(c) 保留独立小 VO（等于放弃合并）。

## 11. 禁止 spi / gateway 等间接依赖：一律强制依赖，循环依赖加一层中间 Service

- **角色包黑名单（模块根与分层目录都不许出现）**：`spi` `gateway` `capability` `provider` `rule` `evidence`
  `client` `llm` `prompt` `sign` `qc` `audit`。这些目录一律删除，内容按第 9 节归到所属模块的分层目录里。
- **依赖只写死**：A 需要 B 的能力 → 直接 `@Resource private BService bService;`。
  禁止「接口定义在 A、实现放在 B」的 SPI 反转，禁止 `List<XxxProvider>` / `ObjectProvider` / 策略收集器注入，
  禁止运行期用 `ApplicationContext.getBean()` 找实现。理由：间接层把「谁调谁」从代码里抹掉了 ——
  读 B 的接口实现不知道 A 会不会调它，改一个实现要在全体模块里反查谁声明了这个 SPI，
  而 Spring 注入失败时报的是启动期 `NoSuchBeanDefinition`，与业务错误隔了一整个进程。
- **循环依赖的唯一解法是把公共能力上提**：A→B 且 B→A 时，抽第三个 `XxxService`（放在真正拥有这份事实的模块，
  即谁写这张表的单据谁拥有），A 与 B 各自单向依赖它；
  或者把「双向都要碰的那段状态写入」归一到其中一方，另一方改读它的 service。
  **禁止用绕过手段破环**：不许 `@Lazy`、不许发事件替代调用、不许 `ApplicationContext` 取 bean、不许为此复活 SPI 接口。
- **外部渠道/医保/支付对接收口**：不再有 `InsuranceGateway`/`PayChannelGateway` 接口 + `MockXxxGateway` 实现的两层写法，
  统一为具体 `XxxChannelService`（放所属模块的 `service`，实现进 `service/impl`），调用方直接依赖具体类；
  联调期需要模拟就在该 service 内部按配置分支，**不要**再用「接口 + Mock 实现」表达。

## 12. 代码注释：不写表名，删掉 AI 味

- **注释里禁止出现表名 / 库结构线索**：`biz_xxx`、`sys_xxx` 这类表名、列名、SQL 片段、`information_schema` 口径
  一律不许写进 `*.java` 的注释（`@TableName`、`@Select` 等**代码本身**里的表名不算，那是映射不是说明）。
  结构的归属地是 DDL 与库内 `COMMENT`（见第 1 节）和 `docs/sql/`，不是 Java 注释 ——
  写在 Java 注释里既会随改包/改名漂移成假事实，又把库结构泄漏给每个读代码的人。
  机械判据：`grep -E "(//|\*+ ).*\b(biz|sys)_[a-z_]{3,}" --include=*.java` 结果为 0。
- **AI 味注释全部删除**：
  复述型（「xxx 服务实现类」「构造器注入」「这里返回 VO」「方法说明」）、
  步骤解说（`// 1. 参数校验 2. 落库 3. 返回`）、
  生成体头（`@author`/`@since`/`@description` 模板、分隔线 `// ===== 查询 ===== =====`）、
  占位（`TODO`/`FIXME`/「后续可扩展」）。
  **删注释只删注释，不许顺带改代码**（否则 git diff 里逻辑变更混在清理里，评审无法分辨）。
- **该留下的注释**：读代码看不出来的 WHY —— 隐藏约束、幂等/并发口径、为什么物理删、为什么故意不软删、
  绕过某个上游缺陷的 workaround。判据：删掉这条注释后，下一个读者是否会做出**错误但看起来合理**的改动；
  不会，就该删。

## 13. 业务码值一律枚举化（按含义唯一，禁止私有 int 常量与裸数字）

- **码值 = 某一列的取值**（状态 / 类型 / 标志 / 类别 / 级别 / 来源 / 结果判定），全部用枚举表达：
  禁止 `private static final int RS_DRAFT = 1;` 这类「常量壳」（一个含义一组 int），
  禁止 `entity.setXxx(2)`、`Objects.equals(1, e.getXxx())`、`.eq(BizXxx::getXxx, 0)` 这类裸数字。
  `@Select` / mapper XML 里的 SQL 文本数字除外（那是 SQL，不是 Java 码值）。
- **按业务含义切枚举，不按表切**：同一个含义全仓**只能有一个枚举**，多个模块要用就放
  `his-common/enums`；同名字段在不同表里表达不同含义时是两个不同枚举，各自带业务主语
  （`BedWaitStatusEnum` / `BedAllocateStatusEnum`，不是一个 `StatusEnum` 通吃）。
  **禁止**按表名造 `BizXxxStatusEnum`，**禁止**无主语的 `StatusEnum`/`TypeEnum`/`ResultEnum`。
- **撞上两个同义枚举时，留「拿码值做业务判断」的那个当主**，不是留名字更贴切的：
  只做展示的副本通常只有一两个调用点，而做状态机的副本的码值**已经写进库里**（改它等于改数据口径）。
  合并前先逐字核对码值与文案（`getText` 口径按上一条：脏值出空串，展示侧原来靠「未知状态」占位的档位在合并后变空串，
  属预期的口径纠正）。公共兜底档（如 `UNKNOWN(0)`）**不是业务码值**：`isValid`/`getText` 都要把它排除，
  否则 `isValid(0)` 为真 = 允许往一个库里从不存在的档位写。
  实例（2026-10-07）：`EmergencyStatusEnum`（his-common，20 处调用点，appoint 拿它流转状态）
  vs `CdrEmergencyStatusEnum`（his-medicaltech，1 处展示调用点）→ 删后者、CDR 两处改指前者，
  码值 1-6 与 `biz_emergency` 列注释和字典 `his_emergency_status` 逐字一致，故合并零数据风险。
- **同一列的码值散在「support 类的 `static final int` 常量组 + 一个私有 `levelText` switch」时，那是同一个反模式的两半，
  必须一次收掉**：常量壳（§13 末尾判据：是不是「某一列能取的值」）和码值→文案 switch 都建在**列的外面**，
  于是「新增一个级别」要改常量、改 switch、改区域推导、改时限表四处，而改枚举只需一处。
  做法：新建枚举带 `getCode/isValid/getText/labelOrUnknown` + 把**业务推导**（红黄绿区、优先级、超时时限）
  留在 support 类里调枚举；support 类只删常量与文案方法，计算逻辑不动（属 §13「含临床判定/计算口径」例外）。
  **注意别把两个不同的域合并成一个枚举**：码值形状相同（都是 1-4）但含义由不同列表达时是两个枚举
  （`EmergencyTriageLevelEnum` 急诊 I~IV 级濒危/危重/急症/非急症 vs `TriageLevelEnum` 门诊 危重/急症/亚急/非急），
  合成一个就会让「门诊 1-危重」去渲染急诊的「I级濒危」。
  实例（2026-10-07）：`EmergencyTriageRules.LEVEL_*`×4 + `levelText` + `CdrEmergencyTriageEnum`（4 处展示调用点）
  → 新建 `EmergencyTriageLevelEnum`（label 逐字取列注释「1-I级濒危 2-II级危重 3-III级急症 4-IV级非急症」）、删枚举副本，
  原「I级 濒危」（带空格）与「未知级别」两套写法归一，脏值改出空串；null→「未定级」是刻意声明的缺省档
  （这一列的真实语义是「还没分诊」，与 §13 里 `SysGenderEnum` null→「未知」同一例外），必须在 javadoc 写明。
- **码值 → 文案的映射一律进枚举或字典，禁止独立的「码值→文案」反模式**：任何把码值翻译成文案的
  `switch` / `Map` / `getOrDefault` + `未知(code)` 兜底，**无论它叫什么名字**
  （`XxxLabels` / `XxxText` / `XxxTexts` / `XxxItems` / `XxxRules`，还是 service impl 里的一段内联 `switch`、
  实体上的一个 `getXxxText` 方法），都属于反模式 —— 映射逻辑必须下沉到**枚举**或**字典**，
  调用侧只调 `枚举.getText(...)`（展示）或 `DictCacheService.getDicDataLabel(dictType, code)`（字典项）。
  **严禁存在任何「集中式文案壳类」**（`QcTexts` / `XxxLabels` / `XxxTexts` 这类一层包装）：壳类的每个方法
  只是把调用转发给枚举，多一层没有信息量，只会让「码值→文案唯一出口」变成两个地方
  （2026-10-05 已全量删除 12 个壳类，禁止再建——哪怕它自称「唯一映射处」）。
  唯一例外：含**临床判定 / 计算口径**（如 Aldrete 评分、相容性判定、记账折算、时长格式化、
  手术安全核查项注册表）的 `support` 类可保留业务计算逻辑，但**不得承载码值→文案映射**
  （文案方法一律删掉、调用点直接走枚举 getText），且里面不得再写 `未知(code)` 兜底。
- **枚举还是字典（落点选取口径）**：按"是否稳定、后端是否拿码值做逻辑判断"决定落点 ——
  ① **变化小、后端要用码值做判断**（状态机流转、权限/分支、计算口径）的封闭集合 → **枚举**
  （全仓通用放 `his-common/enums`，否则放所属模块 `enums`）；
  ② **变化大、由操作员在后台字典维护**（机构自定的类型 / 项目 / 选项）的 → **字典**，走
  `DictCacheService.getDicDataLabel(dictType, code)`，不进 Java 枚举；
  ③ 既有的"集中式大字典"壳类（`QcTexts` / `CdrStatusTexts` / `XxxLabels` 等）**已于 2026-10-05 全部删除**：
  每个方法下沉到对应枚举（或字典）后删类，调用点直接调枚举方法。**禁止再建任何壳类。**
- **双方法口径**：每个枚举提供两个静态翻译方法，语义严格区分（**展示口径的标准方法名是 `getText`**，
  2026-10-05 由 `labelOf` 全局更名而来，别再写 `labelOf`）：
    - `getText(Integer|String)`——**展示用**：合法码值→`label`；`null` 或不在枚举内（脏数据）一律返回
      空串 `""`（个别枚举可显式声明缺省文案，如 `SysGenderEnum.getText` 性别 null→「未知」，必须在 javadoc 写明），
      不回落到某个合法文案、也不暴露「未知(n)」。**绝不返回 null**（返回 null 会把 NPE 风险甩给调用方，
      而返回 `""` 是界面最安全的「无此文案」）。
    - `labelOrUnknown(Integer)`——**异常 / 审计 / 合规用**：`null` 或不在枚举内返回「未知(n)」
      （`null` 本身渲染成「未知」），**保留原始码值**以便排查脏数据。业务异常消息、审计日志、
      合规报表里需要让人看到「到底是哪个脏值」时才用，绝不用它喂前端展示。
    - 机械判据：`grep -rn "未知(" --include=*.java` 命中的，必须只是 `labelOrUnknown` / 少数显式声明
      缺省文案的 `getText`（如 SysGenderEnum）的方法体、或显式 `Objects.toString(xxxEnum.getText(...), "未知(n)")`
      这类手写等价物，以及 `DictCacheService` 内部翻译与纯注释；纯展示路径、service 内联 `switch`、
      任何 `XxxLabels`/`XxxTexts` 壳类里出现「未知(code)」即违规。
    - 迁移进度（2026-10-05 全量收口完成）：12 个文案壳类全部删除 ——
      emr `QcTexts`、report `CdrStatusTexts`、patient `BedCenterLabels`/`InpatientLabels`/
      `InpatientTransferLabels`/`InpatientOrderLabels`/`InpatientRecordLabels`/`ConsultationLabels`、
      operation `AnesthesiaLabels`（改名 `AnesthesiaCalcs`，只剩纯计算）、`OperationApplyLabels`、
      charge `InpatientAccountLabels`、medicaltech `TransfusionLabels`（改名 `TransfusionRules`）、
      system `CodeText`；全仓 220 个枚举的 `labelOf` 已更名为 `getText` 并补 `isValid`；
      service 层内联码值 switch（SysLog 四处、PayChannel、床位匹配级别、医技执行状态）已下沉枚举。
      验收判据：`grep -rnE "class \w+(Labels|Texts)" --include=*.java source/back_end` 结果为 0。
- **文案差异不产生新枚举**：码值相同、中文叫法不同时**复用枚举**（文案以枚举 `label` 为唯一来源），
  不同模块若确有不可调和的措辞差异，差异放在调用侧局部常量 / 方法，且仍调枚举 `getText` 做兜底；
  不许为一句话的措辞复制出一个枚举，也不许为改文案去动公共枚举的 `label`。
- **枚举的唯一模板**（`@Getter` + `code`/`label` + `fromCode` + `getText` + `labelOrUnknown`）：
  新建与改造到的枚举一律照此写；`getText` 必返回 `""`（或显式声明的缺省文案）、不得返回 null，
  异常路径统一走 `labelOrUnknown`。
- **0/1 三兄弟按列注释的含义选，不按字段名前缀选**：`是否 xxx` → `YesOrNoEnum`（YES=1 是 / NO=0 否），
  启用停用 → `EnableStatusEnum`，删除标志 → `DelFlagEnum`。
- **技术阈值不是码值**，继续用 `static final int`：列宽（`W_*`、`*_MAX_LENGTH`）、小数位（`*_SCALE`）、
  条数/长度上限（`MAX_*`、`*_LIMIT`）、时间窗（`*_DAYS`/`*_HOURS`）、分数与置信度、加密参数。
  判据一句话：**这个数是不是「某一列能取的值」——是就枚举，是「多大 / 多少条」就留常量。**
- **枚举化只换表达方式，不换语义**：`XxxEnum.Y.getCode()` 必须与被替换的原值逐字相等。
  Java 码值与库注释不一致时以 Java 现有值为准（改码值=改数据口径，属于独立的、要单独拍板的一步），
  并把冲突单独列出来修，不许在枚举化顺手「修正」。
- **字典权威在 Java 枚举**（重申 §7）：新增/调整码值必须同步 `sql/xxx` 的字典段，两侧同码同义。
    - **labelOrUnknown 必须真的查枚举**（2026-10-06 实锤过一批系统 bug）：批量补方法时最容易写出
      ```java
      public static String labelOrUnknown(Integer code) {
          return code == null ? "未知" : "未知(" + code + ")";   // ← 任何合法码值都返回「未知」
      }
      ```
      这种**空壳实现**：`getText` 是对的，`labelOrUnknown` 却对任何码值都输出「未知(n)」，
      编译不报错、单测也测不出来（只要没有"合法码值应输出 label"的断言）。
      **判据**：方法体里必须能看到它查了枚举（`XxxEnum item = fromCode(code)` + `item.label`）。
      修法：`return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;`
      2026-10-06 全仓扫出 **49 个这类空壳**（report 41 个 `Cdr*` + charge 3 + common 2
      （`PaymentMethodEnum` / `SettlementModeEnum`）+ emr 1（`QcDimensionEnum`）+ `PaymentItemTypeEnum`
        + `StatReportTypeEnum`（"未知类型"变体）），已全部修复。
          **注意判据别写太死**：合法实现可能用 `item.label` / `item.getLabel()` / `item.desc` /
          `item.text`（Lombok getter，变量名也可能是 `status`/`source`/`e` 而不是 `item`）——
          判据要认「**方法体最终返回了枚举实例的文案字段**」，别只匹配一种写法，否则会误报成 bug。
- 机械判据：
  `grep -rn "static final int" --include=*.java source/back_end` 里只剩技术阈值常量（逐条核对，业务码值为 0）；
  `grep -rnE "set[A-Z]\w*\(\s*[0-9]+\s*\)|Objects\.equals\(\s*[0-9]+," --include=*.java source/back_end` 结果为 0。

## 14. 码值映射禁止用 static final Map 承载（枚举的Map 写法是同一条反模式）

- **禁止** `private static final Map<Integer, String> XXX_TYPE = Map.of(1, "保养", 2, "维修", 3, "巡检");`
  这类把码值+文案写进 `Map` 常量的写法。它和 `XxxLabels` 壳类是同一个错误的两种皮：
  映射逻辑写在了枚举外面，编译期不校验、IDE 跳不过去、也没有 `isValid` 可供校验注解调用。
  **所有「某一列的码值 → 文案」必须是一个枚举**，调用侧写 `XxxEnum.getText(code)`。
- 一句话判据：**看到 `Map<Integer, String>` / `Map.of(1,"…")` 里装的是码值和中文，就是违规**，
  立刻改成枚举。（真·非码值用途的 Map 常量不受此限：查表缓存、ID→对象的合并结果、`Map.of()` 空集合等。）
- 迁移进度（2026-10-06 全量收口完成）：全仓 11 处 `static final Map<Integer,String>` 已全部处置——
  equipment 6（维保类型/维保结果/计量类型/计量结果/设备状态/设备类别 → 6 个新枚举）、
  emr 3（随访任务类型、问卷维度、问卷渠道：其中 2 个**已有枚举直接复用**，只新建 `SurveyDimensionEnum`）、
  ai 1（费用解释项目类型 → **复用 his-common的 `PaymentItemTypeEnum`**，码值 1-8 与
  `biz_settlement_bill_item.item_type` 落库分布逐字一致）、operation 1（麻醉收费项）。
  **保留 4 处**：`TransfusionCheckItems` / `OperationCheckItems` / `SafetyCheckItems` /
  `FollowupAdverseItems` 是 `support` 里的**注册表**（核查项集合 + 勾稽逻辑），
  按 §13 例外条款保留（不是单纯码值映射），但 `text()` 脏值兜底已改成 `""` + 补 `labelOrUnknown`。
  验收判据：`grep -rn "static final Map<Integer, String>" --include=*.java source/back_end` 只剩上述注册表。

## 15. 分页 DTO 必须继承 PageParam，且必须是独立顶层类

- **分页查询 DTO 一律 `extends com.his.common.base.PageParam`**，不再各自声明
  `private Integer pageNum = 1; private Integer pageSize = 10;`。
  `PageParam` 已有 `int pageNum = 1 / int pageSize = 10`，重复声明会造成两套默认值，
  而且 `@Data` 子类不给 `@EqualsAndHashCode(callSuper = true)` 会让 Lombok 编译告警、
  父类字段被equals/hashCode 漏掉。正确写法：
  ```java
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class XxxQueryPageDTO extends PageParam implements Serializable { ... }
  ```
- **禁止把 DTO 写成 `XxxDTO` 聚合类里的 `public static class` 内部类**。
  一个内部类文件会让 import 变成 `EquipmentDTO.QueryPage` 这种带外部类限定的前缀，
  Swagger/日志/前端联调看不全类名，按类名 grep 也搜不到；且**内部类无法被其他模块复用**。
  规矩：**一个 DTO 一个文件**，文件名 = 类名，必须带 `DTO` 后缀（VO 同理带 `VO` 后缀）。
- **DTO 不留死代码**：定义了却没有任何 Controller/Service 引用的内部类直接删，别"提出来"——
  `EquipmentDTO.MaintainDelete`（带 `reason` 字段）就是活例子：接口用 `@RequestParam Long id`、
  前端 `maintainDelete(id)` 也不传 reason，这段校验和字段从来没生效过，2026-10-05 已随聚合类一起删除。
- 迁移进度（2026-10-05~06）：`EquipmentDTO` 5 个内部类拆为5 个独立文件
  （`EquipmentQueryPageDTO` / `MaintainQueryPageDTO` / `MaintainCreateDTO` /
  `MeteringQueryPageDTO` / `MeteringCreateDTO`，聚合类 `EquipmentDTO` 整个删除）；
  全仓约 **75 个分页 DTO** 已全部改为 `extends PageParam`（`grep -rln "private Integer pageNum" `
  已无非 PageParam 命中）。另把 miniapp 两个 controller 里写在类内部的 `MessagePageDTO` /
  `MarkReadDTO` 提成独立 DTO 文件（同时违反 §10 分层与 §15）。
  验收判据：`grep -rln "private Integer pageNum\|private int pageNum" --include=*.java source/back_end`
  的每个文件（除 `his-common/base/PageParam.java` 本身）都要能在同文件里grep 到 `extends PageParam`。
- **`@Data`/`@EqualsAndHashCode` 落在有父类的类型上必须带 `callSuper = true`（2026-10-08 全仓清零 7 处）**：
  否则 javac 出告警「Generating equals/hashCode implementation but without a call to superclass」，
  而 `callSuper = false` 是最坏的写法 —— 它把告警闭嘴了，同时让 `pageNum/pageSize`（或父类那批公共列）
  **不参与 equals/hashCode**，等于声明「两个不同页的相同查询条件是同一个对象」。
  判据是跑 `node workspace/_scan_eq_call_super.mjs` 必须输出 `total = 0`
  （它按「声明上的 @Data/@Value/@EqualsAndHashCode + `extends 非 Object` + 无 callSuper=true」配对，
  嵌套 static class 也算 —— `XxxVO.Detail extends XxxVO.Row` 这类同文件继承最容易漏）。
- **主键入参写成 `String` 的 DTO 是静默 bug，编译器不报（同一条被 2026-10-08 又抓到 6 处）**：
  `selectById(Serializable)` 照收字符串，MySQL 再把 BIGINT 列隐式转换比较 —— 传 `"abc"` 不报 400，
  而是落进「数据不存在」（实测旧链路 `POST /ai/patient/feeExplain {billId:"abc"}` → **500「账单不存在：abc」**，
  真因「你传了个坏 ID」被完全掩盖）。改成 `Long` 后同一请求 = **400「请求体格式不正确，无法解析」**，
  空串 = 400「billId不能为空」，而 19 位雪花 ID 以字符串传入仍精确命中（实测 `8900000000002200001` → 200）。
  判据：`grep -rnE "private String [a-zA-Z]*[iI]d;" --include=*DTO.java source/back_end`
  只允许命中第 1 节列出的对接外部报文/非我方主键那几处（`OpenAiChatResponseDTO.id`、`openid`、
  前端自取的 `sessionId`、跨表审计标识 `targetId`）。
- **✅ `PageParam` 已加越界夹取（2026-10-06 老王拍板落地）**，全部 75+ 分页 DTO 一处生效：
  ```java
  public int getPageNum()  { return pageNum < 1 ? 1 : pageNum; }
  public int getPageSize() { if (pageSize < 1) return DEFAULT_PAGE_SIZE;
                             return exportMode ? pageSize : Math.min(pageSize, MAX_PAGE_SIZE); }
  ```
    - `MAX_PAGE_SIZE = 200`（与前端 `page-sizes` 最大档一致）、`DEFAULT_PAGE_SIZE = 10`。
    - **用getter 夹取而不是 `@Min/@Max` 报 400**：前端本来就合法地传 200，导出还要一次拉 5000 行，
      硬校验会把导出和超档请求一起打回。越界静默夹到上限，语义是「你要多少最多给你这么多」。
    - **导出绕过通道**：`forExport(int maxRows)` 置 `exportMode`（`transient` + `@JsonIgnore`，
      请求体传不进来、响应不外泄），固定第 1 页并放开上限。
      改用它的两处：`RxReviewServiceImpl.itemExportCsv`、`SysLogServiceImpl.exportCsv`（原 `setPageSize(EXPORT_MAX)`）。
      **新写导出必须走 `forExport`，别拿外部入参的 pageSize 当上限。**
    - 原本在 service 里重复写的 `Math.max(1, …)` / `Math.min(…, 200)` 属重复造轮子，已删
      （`LabPlainItemAdminServiceImpl.adminPage`）。
    - ⚠️ **`pageSize/pageNum` 是原始 `int`，直接读字段拿不到夹取**——必须走 getter。
      同理 8 个原本默认 20 的 DTO 迁移后默认变10，属预期统一。
- ⚠️ **`Integer` → `int` 会让兜底代码编译失败（已踩，现象具有欺骗性）**：
  `PageParam.pageNum` 是原始 `int`，迁移前 DTO 声明的是 `Integer`，所以
  `dto.getPageNum() == null ? 1 : dto.getPageNum()`、`Integer::equals`、三元里混 `Integer`/`int`
  这类写法继承后会报「二元运算符 '==' 的操作数类型错误」或 NPE 风险。
  **更坑的是：增量编译会掩盖它**（2026-10-06 实测前 5 轮 `mvn -o -DskipTests install` 全 SUCCESS，
  第 6 轮才炸出 4 处）——**分页 DTO 改造一律以 `clean install` 为准**。

## 16. 码值范围校验走 Bean Validation，禁止 service 里手写 containsKey 抛异常

- **「这个码值合不合法」是入参约束，必须用 Bean Validation 声明在 DTO 字段上**，不要在 service 里
  `if (!XXX_MAP.containsKey(dto.getXxx())) throw new BusinessException("取值不合法（1-… 2-…）");`。
  手写版的三个问题：① 校验文案和枚举 `label` 是两处副本，枚举加一个码值这里就漏改；
  ② 绕过 Controller 直接调 service（内部复用、定时任务）时校验失效；
  ③ 每个 Service 重复一遍，`GlobalExceptionHandler` 的 `MethodArgumentNotValidException`
  分支已经有了，统一走它错误码和文案才一致。
- **用法**：
    - 码值**区间连续**（如 1/2/3/4）→ 用 `@Min(1) @Max(3)`（jakarta.validation 自带，本仓已有 206 处在用）。
    - 码值**不连续或来自枚举/字典** → 用 `@InEnum(XxxEnum.class)`（`his-common/validation/InEnum`），
      反射调枚举的 `isValid(code)`。**这正是 §13 强制每个枚举提供 `isValid` 的原因** ——
      枚举模板的 `isValid` 不再只是自测用，它是校验注解的落点。
    - **为什么不能一律用 @Min/@Max`（实测数据，别凭感觉）**：全库 377 个带 int code 的枚举里，
    **9 个码值不连续**，`@Min(min) @Max(max)` 会把这些码值全放行：
    `DeathPlaceEnum`/`DischargeWayEnum` 是 `1,2,3,4,5,9`（9 是"未指明/其他"的保留码，
    6/7/8 根本不存在，@Min(1)@Max(9) 会放过 6/7/8）；`SysGenderEnum`/`InpatientLeaveTypeEnum` 是 `1,2,9`；
    `TcmDecoctStatusEnum` 是 `1,2,3,9`；`EndoscopyTypeEnum` 是 `1,2,7`；`OpdLogStatusEnum` 是 `0,1,8`；
    `GuardianRelationEnum` 是 `1..16,99`；`QcSeverityEnum` 有重复的 0。
    这类集合**只能靠枚举 `isValid` 卡**。所以判断口径是：
      **先看枚举码值连不连续，连续用 @Min/@Max，不连续用 @InEnum。**
    - `@InEnum` 的 `null` 一律放行（是否必填交给 `@NotNull`），只管"填了之后合不合法"。
    - 字符串码值枚举（血型、输血反应类型）用 `@InEnum(value = XxxEnum.class, type = InEnum.Type.TEXT)`。
- **Service 层只保留跨字段业务规则**（"下次维保日期不能早于本次维保日期"、"有效期至不能早于计量日期"），
  这类规则确实没法用注解表达，留在 service 是对的；**单字段码值合法性一律上注解**。
- 迁移进度（2026-10-06 全量收口完成）：`@InEnum` + `InEnumValidator` 建于 `his-common/validation`；
  共迁走 **40+ 处** service 手写 `containsKey` / `Set.of().contains()` 码值校验，落到 DTO 字段注解
  （equipment 4、emr 2、patient 18、medicaltech 6、pharmacy 7、其余零散），
  码值连续的用 `@Min/@Max`、不连续的用 `@InEnum`；顺带给 **19 个已有枚举补 `isValid`**（`@InEnum` 的落点，
  patient 模块枚举本来就齐、缺的只是 `isValid`）。
  **保留在 service 的三类是对的，不要再往注解上搬**：① 跨字段业务规则（"下次维保日期不能早于本次维保日期"）；
  ② 后台字典维护的集合（`his_notice_consciousness` 等 7 处，属"字典"不进 Java 枚举）；
  ③ service 内部派生值校验（不是 HTTP 入参，挂 DTO 注解无效，删了就真失去兜底）。
  验收判据：`grep -rn "取值不合法" --include=*.java source/back_end` 只允许出现在 DTO 注解参数与
  `@Schema(description=...)` 里，不允许出现在 service impl 的方法体里。

## 17. 不自造时间截断工具方法：DATETIME(0) 的精度由库保证（签名/哈希场景才截秒）

- **禁止**在 service 里写这类私有工具方法：
  ```java
  private static LocalDateTime nowSeconds() { return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS); }
  private static LocalDateTime toSeconds(LocalDateTime t) { return t == null ? null : t.truncatedTo(ChronoUnit.SECONDS); }
  ```
  这类方法的存在几乎总是**掩盖了一个更该修的根因**，而不是真的需要。
  本库实测：全库 1068 个 `datetime` 列**精度全部为 0**（`DATETIME_PRECISION = 0`），
  MySQL 存进去自动四舍五入到秒，**Java 侧再截一次是重复劳动**，且掩盖了实体没配自动填充的真问题。
- **正确顺序**（遇到"更新时间要写但没写上"时按这个查）：
    1. 先查实体有没有 `@TableField(fill = FieldFill.INSERT)` / `INSERT_UPDATE`。
       本库 296 个 `update_time` 列里只有 60 个带 `on update CURRENT_TIMESTAMP`，
       305 个 `create_time` 里只有 162 个带 `DEFAULT CURRENT_TIMESTAMP` —— **不能指望 DB 兜底**。
       MP 的 `MetaObjectHandler`（`his-web/config/MyBatisPlusConfig`）是全库统一入口，
       实体加注解即生效，**这才是正解**。
    2. 加了注解后，service 里就不用再`setUpdateTime(...)`，让`strictUpdateFill` 自动填。
    3. 实锤案例：`sys_equipment` 90 行里**89 行 `update_time` 是 NULL**，
       因为 `SysEquipment` 实体没配 `fill`、DB 列又没 `on update` —— 当初只能靠手写
       `setUpdateTime(nowSeconds())` 补洞，方法本身是**症状**，补 `fill` 才是**病因**。
       2026-10-05 已给 `SysEquipment` / `BizEquipmentMaintain` / `BizEquipmentMetering`
       三个实体补齐 `createTime`/`updateTime`/`delFlag` 的 `fill`，`nowSeconds()` 随之删除。
- **唯一允许截秒的场景**：时间值参与**落库后回读比较 / 签名 / 哈希 / 防重放 / 幂等键**计算，
  这类必须保证"同一秒内重复计算结果一致"。**收口到 `com.his.common.util.TimeUtil`**
  （`TimeUtil.toSeconds(t)` 归一入参、`TimeUtil.nowSeconds()` 取当前秒），
  **禁止每个 service 各写一份私有副本**——2026-10-06 收口前全仓有 **26 个副本**
  （`nowSeconds` / `nowSec` / `toSeconds` / `seconds` 四种名字混用，283 处调用），
  同一语义复制 26 份还各叫各的，这不叫收口。已全部改为调`TimeUtil`。
- **单纯"记个时间"不要截秒**：`createTime` / `updateTime` / 操作日志时间直接 `LocalDateTime.now()`，
  库会自动四舍五入（1068 个列全是 `DATETIME_PRECISION = 0`），且该配实体
  `@TableField(fill = ...)` 让 MP 统一填（`sys_equipment` 89/90 行 `update_time` 是 NULL 就是反例）。
- 机械判据：
  -
  `grep -rn "private \(static \)\?LocalDateTime \(toSeconds\|seconds\|nowSeconds\|nowSec\)" --include=*.java source/back_end`
  结果必须为 **0**（唯一实现在 `his-common/util/TimeUtil.java`）。
    - `grep -rn "static final Map<Integer, String>" --include=*.java source/back_end` 只剩 §14 允许的注册表类。

## 18. 零引用枚举必须删掉，不留"备着将来用"

- **判据很简单**：一个 `*Enum.java` 文件名去掉后缀，在全后端 `.java`/`.xml` 里grep 不到任何
  `\bXxxEnum\b` 命中（排除自身文件），就是零引用，直接删。
  2026-10-06 扫出 **23 个零引用枚举已全删**（`ChargeStatusEnum` / `ChargeTypeEnum` /
  `SettlementTypeEnum` / `AbnormalFlagEnum` / `CatalogTypeEnum` / `FollowupStatusEnum` /
  `RefundFlowSourceEnum` / `SysDicEnum`（空壳类，连枚举都不是）/ `DecoctMethodEnum` /
  `DisputeLevelEnum` / `DoctorTalkTypeEnum` / `RxFlowStatusEnum` / `BedWaitGenderLimitEnum` /
  `BedWaitPriorityEnum` / `CheckupAbnormalFlagEnum` / `DischargeStatusEnum` / `InpatientDiagTypeEnum` /
  `NutritionScreenSourceEnum` / `OrderDictSourceEnum` / `PatientMergeLogStatusEnum` /
  `PatientTagSourceEnum` / `CdrNursingLevelEnum` / `CdrOrderClassEnum`），删完全仓枚举 394→ 371，零引用 0。
- **为什么删**：码值文案的唯一价值是被读侧调用。没人调用的枚举是纯噪音——它会让人误以为
  「这个码值已经有口径了」，真要用时从枚举里抄的label 也没人验证过。**枚举不是文档，是代码。**
- **⚠️ 注意重名坑**：`his-common` 的 `AbnormalFlagEnum` 与 `his-medicaltech` 的
  `UltrasoundAbnormalFlagEnum` 码值重叠，grep 引用时必须用 `\b<完整类名>\b` 全字匹配，
  别用前缀/子串匹配（`AbnormalFlag` 会同时命中两个，导致删错）。
- 机械判据：全仓枚举文件逐个做上述零引用统计，**结果必须为空**。
  删完必须 `mvn -o -DskipTests clean install` 兜底（增量编译对删除类不可靠）。

## 8. 凭据分流：登录口令可入库，环境口令一律不入库

口径一句话：**「谁能拿这个口令登录系统」可入库；「能连上这台机器/这个中间件」不可入库。**

| 类别     | 例子                                                                        | 落点                                            | 入库 |
|--------|---------------------------------------------------------------------------|-----------------------------------------------|----|
| 系统登录账号 | `sys_user` 的 480 个账号（`admin` / `shennan` / `13899000001` …，口令统一 `123456`） | `docs/测试账号与凭据.md`                             | ✅  |
| 环境凭据   | MySQL `xz_feng`、Redis `123456`、Gitea `laochai`、JWT secret、构建工具链口令         | `workspace/环境凭据.md` + `application-local.yml` | ❌  |

- **`application.yml` 只放占位**：`password: ${HIS_DB_PASSWORD:}` / `${HIS_REDIS_PASSWORD:}` / `${HIS_JWT_SECRET:}`。
  真值落 `application-local.yml`（`spring.config.import: optional:classpath:application-local.yml` 加载，
  `.gitignore` 已排除该文件名，仓库内不落盘）。
  **新增任何数据源/中间件凭据都照抄这个口径**，不要在 `application.yml` 里直接写字面口令。
- `.gitignore` 是白名单模式（`/*` + 放行 `source/`·`docs/`），`workspace/` 天然不入库；
  但 `docs/` 是放行的，**新增带凭据的文档必须显式加排除规则**。
  ⚠ 排除规则别写 `*凭据*.md` —— 会误伤可入库的 `docs/测试账号与凭据.md`；
  环境凭据只按文件名精确匹配（`环境凭据.md` / `*环境凭据*`）。
- **提交前必扫**（口令曾明文躺在已入库的 `application.yml` 里，2026-10-03 才清掉）：
  ```bash
  git grep --cached -n -I -E "Feng123456!|his-system-jwt-secret|laochai:" -- .
  ```
  无输出才算干净。`docs/er/vendor/mermaid.min.js` 里的 `"0123456789"` 是误报，人工看上下文。
  ⚠ `--cached` 必须排在模式串**之前**：写成 `… -E "pattern" --cached -- .` 时 git 把 `--cached`
  当 revision 解析，直接 `fatal: unable to resolve revision: --cached`，等于这道闸没跑（原命令就是错的）。
- **改了 `application.yml` 的凭据配置必须重启实测**：`mvn -o -DskipTests install` → 停旧 JVM →
  `java -jar his-web/target/his-backend.jar` → 探 `/api/auth/info`（期望 401）→ 跑登录脚本。
  端点 401 只说明进程活着，**还要跑一次 `POST /auth/login` + `POST /system/dict/refreshCache`**
  才证明 DB 口令（登录）与 Redis 口令（缓存刷新）都真的读到了。

## 19. 无参构造一律用 Lombok 生成，禁止手写（2026-10-06 全量收口）

- **本工程 Lombok 重度使用**（`@Data` 1988 处、`@NoArgsConstructor` 已用 17 处），手写 `public Xxx() {}` /
  `private Xxx() {}` 是非惯用法，统一由注解生成。全仓 **59 处**手写空构造已于 2026-10-06 全部替换（4 个 `public` + 55 个
  `private`，覆盖 13 个模块）。
- **访问级规则（逐字节还原手写语义）**：
    - 工具 / 常量 / support 类（`final class` + 全静态，防止实例化）：`private Xxx() {}` →
      `@NoArgsConstructor(access = AccessLevel.PRIVATE)`（需 `import lombok.AccessLevel;`）。
    - 需要反序列化无参构造的 POJO / DTO / VO / Result（带 `@Data`）：`public Xxx() {}` → 普通 `@NoArgsConstructor`（默认
      public，保留 `@Data`）。
    - 嵌套类同理按所在类的访问级选注解——注解必须落在内层类上、缩进对齐内层类（如 `ArrearsControlGate.OrderCheck`）。
- **禁止重复声明（已踩出真编译 bug）**：类上**已经**有 `@NoArgsConstructor` 时，再手写一个无参构造 = **重复构造**——Java
  构造签名只看「类名 + 参数列表」，访问修饰符不参与区分，于是 `public` 注解构造与 `private` 手写构造签名相同 → 编译直接报错。修法：
  **删掉手写那个**，按访问级调注解。2026-10-06 的 `PemCodecUtil` 正是「`@NoArgsConstructor`(public) + 手写
  `private PemCodec()`」双声明导致的编译阻塞，已修。
- **`@Data` 与 `@NoArgsConstructor` 不冲突**：`@Data` 只在类有 `final` / `@NonNull` 字段时才生成无参构造；本仓核心类（
  `Result` / `PageResult` 等）无 `final` / `@NonNull` 字段，现在能编过就是证据，补 `@NoArgsConstructor` 不会与之撞。
- **枚举构造不在此列**：枚举（隐式）无参构造不能由 `@NoArgsConstructor` 替代，枚举一律不要手写构造（本项目枚举均无参、无手写构造）。
- **Spring Bean 不碰**：带 `@Component` / `@Service` / `@Configuration` / `@Repository`
  的类由容器实例化，本就不该有手写无参构造（若见到，是误写，应删而非加注解）。
- 机械判据：`grep -rnE "\b(private|protected|public)\s+[A-Z][\w$]*\s*\(\s*\)\s*\{\s*\}" --include=*.java source/back_end`
  命中的，必须已是枚举（自然豁免，因其构造不带上述修饰符）或已配套 `@NoArgsConstructor`（手写体应删除）；新增代码一律不手写无参构造。改造脚本见
  `workspace/_scan_empty_ctors.py` + `workspace/_apply_noargs.py`。

## 20. 操作人取值：直接 `getCurrentUser().getRealName()`，严禁任何默认值（2026-10-06 全仓收口）

- **`UserUtils` 只有一个方法 `getCurrentUser()`**。不要新增 `getCurrentEmployeeId()` / `getCurrentEmployeeName()` /
  `requireOperatorName()` 这类封装 —— 同一个语义出现两个出口就一定会漂移成两套口径（本次收口前正是如此：
  6 份私有 `currentOperator()` 副本各写各的兜底）。要操作人直接链式取字段：
  ```java
  entity.setCreateBy(UserUtils.getCurrentUser().getRealName());   // 姓名
  row.setOperatorId(UserUtils.getCurrentUser().getEmployeeId());  // 员工ID
  ```
- **姓名口径只有 `CurrentUser.realName`**（即 `sys_user.real_name`，登录时 `UserDetailsServiceImpl` 无条件填充，
  缺员工档当场拒登录）。**禁止**回落 `employeeName` / `username` / `getUsername()`：
  `username` 是账号拼音（`user_name = real_name` 去符号小写全拼），拿它当人名会让库里操作人列出现两种格式，事后按人名检索直接漏。
- **禁止塞默认值**：`"system"` / `"未知操作人"` / `"系统"` / `"系统操作"` / `String.valueOf(empId)` /
  `try-catch-return-null` 全部禁止。
  取不到就是**报错**（NPE 由全局异常处理兜成 500，或自己抛 `BusinessException`），不是继续执行。
  塞假值的代价是「谁干的」被藏进库里，出事时查不出来，而且现象是零报错。
- **三处定时任务豁免（唯一允许落系统值的地方，2026-10-06 逐个 cron 用调用图排查确定）**：
  | 位置 | 值 | 触发 |
  |---|---|---|
  | `DayEndSettleServiceImpl.currentOperator()` | `system:dayEndSettle` | cron 每天 00:10 + 进页面懒触发（共用
  `doSettle`） |
  | `ExamAppointmentServiceImpl` | `system:examNoShow` | `ExamNoShowTrigger` 每 10 分钟（与人工改约/取消共用
  `releaseOld`） |
  | `FollowupTaskServiceImpl` | `system:autoFollowup` | `DischargeFollowupTrigger` 每 10 分钟（
  `autoCreateFromDischarge`） |
  后两处的做法是**给方法加 operator 参数 / 重载，由调用方显式传**（`createFromDischarge(dto, operator)`），
  **不是**在方法内部判空回落 —— 内部回落出来的系统值在调用链上根本看不出来。
  ⚠ 改任何 cron 链路前，先用「花括号配平 + 方法调用图递归」查清它到底会不会取当前人，别凭方法名猜
  （`escalateOverdue`/`notifyOverdue`/`autoNoShow` 这类名字看着像，其实现身不取，调用链才取）。
- **旁路审计特例（不抛异常，但也不塞假名字）**：操作日志与字段变更记录构造失败绝不能打断业务，
  所以 `OperLogInterceptor` / `FieldChangeRecorderImpl` / `PriceServiceImpl` / `SysLogServiceImpl`
  取不到登录态时**留空/null**（库里能看出「这条没操作人」），而不是填假值；同时也要删掉它们的 `employeeName`/`username` 回落。
  `AiAuditServiceImpl` 同理：调用方没给 operator 就 warn + 不落库。
  ⚠ 若取操作人的那行**在 try 块内**，异常会被 catch 吞成 warn，报错等于没有 —— 要么把取值挪到 try 外面，要么明确接受这条记录丢失。
- **机械判据**（新增/改动代码自查）：
  ```bash
  grep -rnE '"system"|"未知操作人"|"系统操作"|getCurrentEmployeeName\(\)|getCurrentEmployeeId\(\)' --include=*.java source/back_end
  ```
  只允许命中上表三处定时任务常量、`ChannelEnum.SYSTEM`、`AiMessageDTO.ROLE_SYSTEM`，
  以及 `OperLogInterceptor.resolveTitle` 的 `return "系统"`（那是**操作模块标题**，不是操作人）。

## 21. 时间格式化只认 `DateFormats` 的常量，禁止任何地方 new formatter（2026-10-07 全仓收口）

- **禁止**在业务代码里出现这两种写法（`import java.time.format.DateTimeFormatter` 也一并禁止）：
  ```java
  private static final DateTimeFormatter NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");  // 私有常量副本
  "RX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))              // 行内新建
  ```
  一律改为引用 `com.his.common.util.DateFormats` 的常量：`DateFormats.COMPACT_DATE` / `DateFormats.DATETIME` / …
- **为什么连 hutool 的 `DateUtil` 也不用**（老王原本提「要么抽 DateUtil，要么用 hutool 的 Formatter」，已否决）：
    1. hutool 的 `DatePattern` 是 **`String` 常量**不是 `DateTimeFormatter`，写法是
       `DateUtil.format(x, DatePattern.PURE_DATETIME_PATTERN)` —— pattern 字面量照样散在每个调用点，
       「同一格式两处各写一遍、改一处漏一处」原样保留，只是把 `ofPattern` 换了层皮。
    2. **覆盖不全**：本库要用的 `HH:mm`（hutool 只有 `HH:mm:ss`）和身份证的 `uuuuMMdd + ResolverStyle.STRICT` 都没有。
    3. `DateUtil` 门面类型是 `java.util.Date`，本库全链路 `java.time`，引入它等于把旧时间类型引回业务代码。
    4. hutool 只在 `his-common` / `his-emr` / `his-system` 三个 `pom.xml` 里声明了依赖，其余 9 个模块靠传递，不能当全库口径。
- **9 个常量按用途分四组，不要新增第 10 个同名 pattern**（同一个 pattern 不许出现第二次；确属新形状才往里加）：
  | 分组 | 常量 | 形态 | 用途 |
  |---|---|---|---|
  | 可读格式 | `DATE` | `2026-10-07` | 日期展示、日分组键 |
  | 可读格式 | `DATETIME` | `2026-10-07 14:30:05` | 导出 CSV / 日志 / 对外文本（最常用） |
  | 可读格式 | `DATETIME_MINUTE` | `2026-10-07 14:30` | 精确到分钟的展示 |
  | 可读格式 | `TIME_MINUTE` | `14:30` | 一天内时刻，不带日期 |
  | 紧凑格式 | `COMPACT_DATE` | `20261007` | 单号日期段 / Redis 日序列 key 后缀 |
  | 紧凑格式 | `COMPACT_DATETIME` | `20261007143005` | 单号时间段 |
  | 紧凑格式 | `COMPACT_DATETIME_MS` | `20261007143005123` | 支付渠道流水戳等需同秒再区分的场景 |
  | 协议格式 | `ISO_DATETIME` | `2026-10-07T14:30:05` | **仅**医保/TSA 通道报文（对方协议要带 `T`） |
  | 解析专用 | `STRICT_COMPACT_DATE` | `uuuuMMdd` + STRICT | **仅**解析身份证出生日期 |
- **`COMPACT_*` 系列禁止用于任何展示字段**（单号/键/目录名专用，给人看就用可读格式组）。
- **`STRICT_COMPACT_DATE` 的两个参数都不能改**：
  `yyyy` 是 year-of-era，STRICT 下缺 era 会直接抛 `DateTimeParseException`，必须是 `uuuu`；
  STRICT 才能把身份证里的 `0230110` 当场判非法 —— SMART 会悄悄规整成 2 月 28 日然后放过。
  这是全库**唯一**该用它的地方；别处解析日期一律走 ISO（`LocalDate.parse(text)`）。
- **这些常量只管「渲染形状」，不管「入参解析」**：DTO 的 `@JsonFormat` 与 `LocalDate.parse` 的 pattern 由 §3 单独规定
  （空格分隔 vs ISO `T`），两者必须对齐，**不要因为有 `ISO_DATETIME` 就去改 DTO 上的 pattern**。
- **`TimeUtil` 与 `DateFormats` 分工，别混**：`TimeUtil` 管**归一到秒**（落库回读比较/签名场景，见 §17），
  `DateFormats` 管**渲染成什么形状**。要「此刻 + 秒级」写 `TimeUtil.nowSeconds()`，
  要「此刻 → 字符串」写 `DateFormats.DATETIME.format(TimeUtil.nowSeconds())`，**不要**再加第三个门面类。
- 机械判据（用 `-l` 只看文件名，别用数字 —— 命中数会随常量增减变化，写死数字会过期）：
  ```bash
  grep -rl "DateTimeFormatter.ofPattern(" --include=*.java source/back_end
  grep -rl "static final DateTimeFormatter" --include=*.java source/back_end
  ```
  两条命令都**只能输出 `his-common/util/DateFormats.java` 这一个文件**；出现第二个文件就是有人又自己造了一份。
  2026-10-07 收口前是 **128 处/ 90 文件 / 10 模块**，其中 95 处私有常量字段用了 **32 个不同名字**
  （`NO_DATE`×25 / `DAY_FMT`×7 / `TS`×6 / `TIME`×6 / `DAY`×6… 指的就 4 种格式），
  33 处是行内 `format(ofPattern(...))` —— 每次调用新建一个 formatter，且全落在「生成业务单号」热路径上。
  改后 `ofPattern` 归零，常量被 197 个引用点复用。脚本：`workspace/_refactor_date_formats.py`（pattern→常量映射表在文件头）。
  连带把 `DateTimeFormatter.BASIC_ISO_DATE`（== `yyyyMMdd`）的私有字段 + 行内用法也一并收进 `COMPACT_DATE`。

## 22. 依赖字段名 = 被注入类型的首字母小写全称（2026-10-07 全仓收口 712 处 / 296 文件）

- `@Resource` / `@Autowired` 注入的字段，以及 `@RequiredArgsConstructor` 的 `private final` 依赖字段，
  一律命名为**类型名的首字母小写全称**：`DictCacheService dictCacheService`、
  `BizExamFilmMapper bizExamFilmMapper`、`SysUserService sysUserService`。
- **禁止功能别名**（`dictText` / `subDictText` / `store` / `tsaChannel` / `perfMapper2` 这类），
  **禁止省前缀的短名**（`filmMapper` / `userService`）。理由：一个 Service 里常常注入十几个依赖，
  别名让「这个字段到底是哪个类」必须跳类型才能知道；按类名 grep 找不到依赖点，评审与改造（例如某接口收口、
  某 Mapper 换实现）时点不齐；`@Resource` 的解析顺序是「先按字段名找 bean」，名字与类型对齐后
  这条路径才是可预期的，而不是永远靠按类型兜底。
- **同一类型不许注入两次**：出现即合并成一个字段（本次实锤 `EmrServiceImpl` 注入
  `BizMedicalRecordArchiveMapper` 两份 —— `archiveMapper` 与 `medicalRecordArchiveMapper`，已删成一份）。
  不要为了避开重名而去写 `xxx2` / `subXxx`：那是把「重复注入」这个 bug 固化成命名。
- **只管依赖，不管数据字段**：entity / DTO / VO 的业务字段名跟**列名**对齐（那是第 1 节的事），
  局部变量、`static final` 技术阈值常量不在本条范围内。
- **`@RequiredArgsConstructor` 的类里，依赖字段必须带 `final`**：Lombok 只把 `final`（与 `@NonNull`）字段
  放进构造器，非 `final` 字段既不在构造器里、又没有 `@Resource`/`@Autowired` 时**容器不会注入它** ——
  编译通过、启动通过、**只有第一次调用到那个方法才 NPE**（2026-10-07 实测 22 个类全是同一个字段
  `DictCacheService dictCacheService`，即字典翻译一被调到就 500「系统内部错误」，
  而 `/xxx/listPage` 大多数行没有字典列，所以长期没被发现）。
  要么 `private final Xxx xxx;`（走构造器，缺 bean 直接启动失败 = 早爆），要么显式 `@Resource`，不许裸写。
  机械判据：`node workspace/_scan_uninjected_fields.mjs` 必须输出 `total = 0`
  （它找的就是「组件类里 `private 类型 名;` 非 final、无注入注解、构造器里也没赋值」）。
- 机械判据：`python workspace/_scan_dep_field_names.py` 必须输出 `total mismatches: 0`
  （脚本按 `@Resource/@Autowired/@Qualifier` + `@RequiredArgsConstructor` 的 final 字段收集，
  比对 `name == decap(type)`；`@ConfigurationProperties`/`@TableName` 类自身的字段自动跳过，
  因为它们的字段名是配置键 / 列名）。改造脚本 `workspace/_apply_dep_field_rename.py`（含 `--apply`），
  改完验收口径 = `mvn -o -DskipTests clean install` 通过 + 真启动 + 跨模块接口实测（本次 10 模块 26 个接口 200）。

## 23. 洗字符串/兜数值/归一时间的小工具只许有三个家（2026-10-07 全仓收口，现 185 文件 / 1835 处调用走三件套）

- **三个收口点**（`his-common/util`，除此之外不许出现第四个同名工具）：
  | 类 | 只管这件事 | 方法 |
  |---|---|---|
  | `TextUtil` | 空白清洗与截断、判空布尔 | `hasText(CharSequence)`（反向写 `!hasText`） / `trim` / `trimToNull` /
  `trimToEmpty` / `nullToEmpty` / `blankToDefault` / `cut(v,max)` / `cut(v,max,blank)` / `cutToNull` / `ellipsis` /
  `requireTrimmed` |
  | `NumUtil` | null 兜底、金额舍入、数量文本 | `orZero(Integer/Long/BigDecimal)` / `orDefault` / `scale(v,位数)` /
  `plain(v)` |
  | `TimeUtil` | 归一到秒与日边界、时长 | `toSeconds` / `nowSeconds` / `dayStart` / `dayEnd` / `minutesBetween` /
  `elapsedMinutes` / `elapsedHours` |
- **禁止在 service / support / controller / 接口里写私有副本**，方法名叫什么都算：
  `trimToNull` `tr` `trim` `safe` `defaultStr` `nullToDash` `nvl` `nz` `nzAmount` `nzInt` `cut` `clip` `truncate`
  `plain` `scale` `requireText` `now` `atStart` `atEnd` `dayStart` `dayEnd` `minutesBetween` `hoursBetween`。
  危害不是重复代码，是**同名不同实现**：`nz(Integer)` 有的文件返回 `int` 有的返回 `long`；
  `cut` 有的返回 `null` 有的返回 `""`；`nvl(x,"-")` 有的判 `hasText` 有的只判 `null` ——
  同一个入参洗完落库成什么，取决于这段代码恰好写在哪个文件里。要新方法就先往上面三个类加，
  **加的时候在 javadoc 写清它和相邻方法差在哪**（`trim` vs `trimToNull` vs `trimToEmpty` 是三种落库结果）。
- **不要用 Spring 的 `StringTrimmerEditor` 代替**（用户 2026-10-07 问过，结论是不行）：
  ① 它只作用于**表单/查询参数绑定**（`@ModelAttribute`/`@RequestParam`），本仓 DTO 绝大多数走
  `@RequestBody` JSON，Jackson 不查 `PropertyEditor`，注册了也不生效；
  ② `emptyAsNull=true` 会把「传空串清空该字段」变成传 `null`，而 MyBatis-Plus `updateById`
  **跳过 null 列** —— 等于静默改掉更新语义，用户点了保存但库里没变，零报错。
- **也不要在 DTO 字段上加 Jackson 注解做绑定层 trim**（用户 2026-10-07 提过两条，均已实测否决）：
  ① Jackson **没有**内置的 trim 反序列化器 —— 实测 `jackson-databind 2.15.4`（Boot 3.2.5）里字符串相关
  只有 `deser.std.StringDeserializer`（不 trim）/ `FromStringDeserializer` / `util.StdConverter`，
  教程里的 `TrimStringDeserializer` 是要自己写的那一个；
  ② HV 8.0.1 的 `@Normalized` 只在**送进校验器那一刻**归一，字段值不变，service 拿到的还是 `" 张三 "`，
  比不写更骗人；
  ③ 绑定层只覆盖 `@RequestBody` 一条路 —— 实测另有 **83 处 service 内部 `new XxxDTO()`、
  13 处 `@RequestParam String`、5 处手写 `readValue`** 完全不经过 Jackson，
  洗完之后 service 侧那 300 余处 `TextUtil` 调用一处也删不掉，等于凭空多出「看进门方式决定洗不洗」的第二套口径。
- **判空布尔只有 `hasText` 一个语义**，反向一律写 `!hasText(x)`，**不提供 `isBlank` 第二个方法**
  （2026-10-07 收口：删掉私有副本 `isText`×3、`isBlank`×3、`notBlank`×1、`firstNonBlank`×2（归 `blankToDefault`），
  行内 `x != null && !x.isBlank()` 21 处改判；正向与取反成对写时，改口径必须找齐两份，漏一份就是脏数据入口）。
- **外部库的同义谓词也算「散落的第二套口径」，一律走 `TextUtil.hasText`**（2026-10-07 全仓迁移 1122 处 / 231 文件：
  Spring `StringUtils.hasText` 1109、`StringUtils::hasText` 方法引用 15、`StringUtils.isBlank/isNotBlank` 各 1（后者是
  commons-lang3，与前者的 import 来源不同 —— 迁移前先 `grep "import .*StringUtils"` 认清是哪个包）、
  hutool `StrUtil.isBlank` 2）。
  原先记「存量 1066 处 `StringUtils.hasText` 不迁（同一实现）」是错的：**同一实现 ≠ 同一口径**，
  两套拼写在同一文件里并存时，「改判空规则」这件事就没有可 grep 的落点，且新代码会照抄邻近的那一种。
  迁移后 `StringUtils` 只剩三处**非谓词**用法（`TextUtil` 内部的委托、`getFilenameExtension`、`StringUtils.EMPTY`），
  `TextUtil.java` 本身是全库唯一还引用 Spring `StringUtils` 的业务代码。
  ⚠ `hasText` 的形参必须是 `CharSequence` 而不是 `String`：Spring 的版本收 `CharSequence`，
  收窄会让 `instanceof CharSequence cs` 分支和 `Stream<CharSequence>` 的 filter 编译失败（实踩 his-patient 一处）。
  **唯一豁免：`ClinicalTextMatcher.isBlank`** —— 它除空白还剥「—」「/」这类占位符号，是病历质控的临床语义，
  不是判空副本，不许并进 `TextUtil`（`QcRuleEnum` 的注释依赖这个区别）；它内部的纯判空仍走 `TextUtil.hasText`。
  **另一种不算副本的形状**：`x == null || x.isEmpty()`（不收全空白）与 `hasText` 语义不同，
  改成 `hasText` 会让「粘贴了一段空格的备注」从"有内容"变成"无内容"，属行为变更，逐处确认后才动
  （`EvidenceKeywordMatcher`、`TextSplitter` 等命中即按此处理，不许顺手批量替换）。
- **日边界只有 `TimeUtil.dayStart` / `dayEnd` 两种拼写**（2026-10-07 收口 65 处 / 29 文件）：
  `d.atStartOfDay()`、`d.atTime(23, 59, 59)`、以及 `d == null ? null : d.atXxx()` 这类自带 null 守卫的三元，
  全部换成 `TimeUtil.dayStart(d)` / `TimeUtil.dayEnd(d)`（这两个方法本来就 null 安全，守卫是重复劳动）。
  收口时抓到一处真 bug：`AiAuditQueryServiceImpl` 用 `atTime(LocalTime.MAX)` 当 `.le(create_time)` 的右边界 ——
  `23:59:59.999999999` 在本库全为 `DATETIME(0)` 的列上会被 MySQL **四舍五入进次日**，于是「查某一天」多捞一行，
  且零报错（同一坑 §3 已写过一次，这次是它第三次回来，所以判据要 grep 表达式而不是只查私有方法名）。
  **不许合并的例外**：`atTime(8, 0)` / `atTime(16, 0)` / `atTime(start.toLocalTime())` 是业务时刻，不是日边界。
- **`NumUtil.orDefault` 只允许一个重载** `(Integer, Integer)`：本仓分页参数是原始 `int`
  （`PageParam.getPageNum()`），再配一个 `(Integer, int)` 会在装箱阶段同时可用且互不更特 →
  javac「对 orDefault 的引用不明确」。要原始 `int` 就地拆箱。
- **`plain` 的 null 语义是刻意的**：签名/哈希用的数量文本必须能区分「没有这个值」和「值为 0」，
  所以 `NumUtil.plain(null)` 返回 `null`；展示与报错文案要 `"0"` 的写 `plain(orZero(v))`。
  为什么必须 `stripTrailingZeros().toPlainString()`：`1E+2` 的 `toString()` 带 `E`，
  同一数量从除法来和从字面量来会得到两个不同字符串，而它已经是历史签名的一部分。
- **纯转发的私有文案壳直接删，调用点写枚举**：`private static String statusText(Integer s){ return XxxEnum.getText(s); }`
  这种一层包装（含 `status == null ? "未知" : XxxEnum.labelOrUnknown(s)` —— `labelOrUnknown` 自己就管 null→「未知」）
  属第 13 节「集中式文案壳」的同族，内联成 `XxxEnum.getText(...)` / `labelOrUnknown(...)`。
  **例外**：喂给规范化签名（`CanonicalText`）的 `statusText` 是哈希输入，改动=历史签名全部校验不上，不许动。
- **含业务语义的不算副本，留在原地**：文件格式转义（PDF 的 `safe`/`escape`）、
  数字转文本的临床判据、按列名/动作拼提示语的私有方法 —— 它们不是「洗一个通用类型」。
  判据一句话：**把方法体抄到另一个模块里还成立吗？成立就进三件套，不成立就留着。**
- 机械判据：
  ```bash
  grep -rnE "private (static )?(String|BigDecimal|Integer|Long|LocalDateTime|boolean) " \
    --include=*.java source/back_end | grep -E "\b(trimToNull|trimToEmpty|defaultStr|nullToDash|nvl|nz|cut|clip|truncate|plain|scale|requireText|now|atStart|atEnd|dayStart|dayEnd|minutesBetween|hoursBetween|isText|isBlank|notBlank|firstNonBlank)\("
  grep -rn "truncatedTo" --include=*.java source/back_end
  grep -rnE "!= *null *&& *![a-zA-Z().]+\.isBlank\(\)" --include=*.java source/back_end
  grep -rnE "StringUtils\.(hasText|isBlank|isNotBlank)|StringUtils::hasText|StrUtil\.(is)?Blank" --include=*.java source/back_end
  grep -rnE "\.atStartOfDay\(\)|\.atTime\(23, *59, *59\)|\.atTime\(LocalTime\.MAX\)" --include=*.java source/back_end
  ```
  第一条必须只剩「含业务语义」那一类（逐条核对），第二条**只允许输出 `his-common/util/TimeUtil.java` 一个文件**，
  第三条（行内自己写判空）必须为 **0** —— 一律 `TextUtil.hasText(x)` / `!TextUtil.hasText(x)`，
  第四条只允许 `TextUtil.java`（委托实现）+ `getFilenameExtension` + `StringUtils.EMPTY` 这三处非谓词用法，
  第五条只允许 `TimeUtil.java` 的方法体。
  改造脚本：`workspace/_apply_helper_collapse.mjs`（先 dry 再 `--apply`，映射表在 `mapDef`）、
  `workspace/_apply_truncation_sweep.mjs`、`workspace/_inline_statustext_shells.mjs`、
  `workspace/_collapse_blank_predicates.mjs`（判空谓词收口，`EXCLUDE` 里必须留着 `ClinicalTextMatcher` 的方法定义与
  `TextUtil`）、
  `workspace/_collapse_day_bounds.mjs`（日边界表达式收口）。
  ⚠ 引擎**必须跳过 `*/util/*` 目录**：否则 `TimeUtil` 自己会进流水线，删掉自己的方法并 import 自己
  （2026-10-07 真踩过：`toSeconds` 改成调 `toSeconds` 的无限递归）。
  ⚠ 替换 `Foo.bar(` 这类带包名的调用时**先去限定名**，否则 `org.springframework.util.StringUtils.hasText`
  会被改成 `org.springframework.util.TextUtil.hasText`（不存在的类，编译期才报出来）。
- **尚未收口的一族（下一步，不是豁免）**：私有「日期文本 → `LocalDate`」解析方法 9 处
  （`parseDate`/`parseDateTime`/`parseDateOrNull`，散在 ai/appoint/charge/emr×2/medicaltech/operation×2），
  **四种子口径互不等价**：`LocalDate.parse(text)`（不 trim）、`parse(text.trim(), DateFormats.DATE)`、
  `text.substring(0,10)`（容忍带时间的文本）、`LocalDateTime.parse(s.replace(' ','T'))`（容忍空格分隔）。
  并成一个 `TimeUtil` 方法等于替所有人选一种容错度 + 把「格式错」从 `DateTimeParseException`(500)
  或 `BusinessException`(400 中文) 里改成另一种，是行为变更，必须先定口径再动，**不许照上面的脚本套路批量替换**。

## 24. 字典类型编码只有一个来源：`com.his.common.constant.DictType`（2026-10-07 全仓收口 202 处引用 / 79 个键）

- **调用点禁止写字典类型字面量**，一律 `dictCacheService.getDicDataLabel(DictType.XXX, code)` /
  `getDictDataByType(DictType.XXX)`。私有 `private static final String DICT_XXX = "his_xxx";`（全仓曾有 37 个）也算散落，删掉。
- **常量名 = 编码去掉 `his_`/`sys_` 前缀后大写**（`his_prepay_type` → `PREPAY_TYPE`），机械可逆，
  所以「按名字就能 grep 到字典」，不需要第二张对照表。`DictType.java` **由脚本生成、不许手写**：
  `node workspace/_gen_dict_type_class.mjs` 扫代码里的 `DictType.X` 引用反查库内字典类型，
  常量名冲突或库里不存在该类型时**直接抛错**（手写就会漂成假事实）。
- **只收代码真用到的**（现在 79 个）：库里另有 406 个类型是后台字典页给操作员自取的，**不许抄进来** ——
  抄了就是用副本冒充全量，改库时没人同步这里，反而制造第二个真相。
- **写错键名不会报错，这是它最危险的地方**：`getDicDataLabel` 取不到行返回 **空串**（不抛异常、不返回 null），
  现象是那一片 `xxxText` 字段整列空白，与「这个码值本来就没文案」一模一样；编译、启动、接口全绿。
  所以口径是：**取不到一律空串**（与枚举 `getText` 同口径，见 §13），要保留脏码值排查是枚举 `labelOrUnknown` 的事。
- **`biz_<模块>_<某Enum>` 这种形状的键名本身就是 bug 证据**（表名前缀 + `Enum` 后缀，字典编码从不长这样）。
  本次实锤来源：一次「枚举治理」提交把 `XxxEnum.getText(code)` 批量替换成
  `getDicDataLabel("biz_<模块>_<XxxEnum>", code)`，而**这些字典类型从未被创建**，码值→文案全变空白。
  处置结论（35 个残留假键，逐键拿库内列注释与字典行核对后定）：**8 个**指回真实字典类型、
  **20 个**走新建枚举、**7 个**复用既有枚举（3 个 0/1 标志 → `YesOrNoEnum`，`cdrRegistStatus` →
  `AppointStatusEnum`，`cdrEmergencyStatus` → `EmergencyStatusEnum`，`opdLogStatus` → 从 git 恢复被误删的
  `OpdLogStatusEnum`）。
- **遇到陌生键名按这个顺序判，不许猜**：① `SELECT DISTINCT dict_type FROM sys_dict_data` 查有没有这个类型；
  ② 有 → 用 `DictType` 常量，并核对字典行的码值/文案与宿主列注释**逐字一致**（不一致就是两套口径）；
  ③ 没有 → 按 §13 选落点：后端拿码值做分支/集合封闭 → **枚举**（先找现有的复用，同含义全仓只许有一个），
  纯展示但形状固定 → 枚举；确属操作员维护的才补字典并同步 `sql/xxx` 字典段；
  ④ 新建枚举的码值**一律取宿主列注释原文**，不凭业务直觉补码值。
- **改完回头清死依赖**：类里不再出现 `dictCacheService.` 调用，就删掉它的字段与 import（本次删了 16 处）——
  留着就是 §22 说的「注入了但没人用」，而 `@RequiredArgsConstructor` 类里漏 `final` 的依赖会在第一次调用时 NPE。
- 机械判据：
  ```bash
  node workspace/_check_dict_type_gate.mjs          # 必须 PASS
  grep -rn "getDicDataLabel(\"" --include=*.java source/back_end      # 必须 0
  grep -rn "getDictDataByType(\"" --include=*.java source/back_end    # 必须 0（DictCacheServiceImpl 内的 sys:dict: 前缀除外）
  grep -rn "static final String DICT_" --include=*.java source/back_end  # 只允许 DICT_CACHE_PREFIX（Redis key 前缀，不是字典类型）
  grep -rnE '"(biz|sys)_[a-z]+_[A-Za-z]+Enum"' --include=*.java source/back_end  # 必须 0
  ```
  改造脚本：`workspace/_apply_dict_type_const.mjs`（字面量/私有常量 → `DictType`）、
  `workspace/_apply_enum_retarget.mjs` + `workspace/_enum_retarget.json`（假键 → 枚举/真实字典，含新建枚举模板）、
  `workspace/_sweep_dead_dict_dep.mjs`（删死依赖）。
