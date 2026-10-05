#  AI Agent 全局指令与项目规范 (AGENTS.md)

> **️ 核心原则（Token 节省与输出约束）**
> 1. **绝对精简**：禁止输出任何寒暄、解释、总结或“好的”、“没问题”等废话。直接输出代码或结构化数据。
> 2. **禁止重复**：不要重复用户已提供的代码或上下文。只输出**新增**或**修改**的部分。
> 3. **结构化优先**：当需要传递多个参数时，优先使用 JSON 格式，禁止使用冗长的自然语言描述。
> 4. **按需阅读**：不要一次性输出整个文件，除非明确要求。使用占位符（如 `// ... 现有代码 ...`）省略未修改的部分。

## 1.  前后端交互与 RESTful 规范
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
  这些放代码注释或本文件。建表 SQL 与库内注释同一口径；全库结构基线见 `docs/sql/`（按领域 23 个文件，由 `workspace/_gen_ddl_by_domain.mjs` 从 dev 库导出）。

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

## 2. 前端界面规范

- **禁止使用 `src/components/his/ModulePage.vue`**。它是演示用的硬编码壳（`rows` / `stats` 写死在页面里，不接后端），**不允许新增引用、不允许在其上继续加功能**；存量引用页面（`views/**`，约 20 个）属待删除项，改造时直接换成真实接口驱动的页面，不要"顺手补两个字段"。
- **页面必须是真实数据驱动的**：`src/api/xxx.js` 调后端 → 页面渲染 → 操作回写后端。禁止任何形式的 mock / 写死数组 / 前端自造统计数字。
- **`el-dialog` 默认允许点遮罩关闭**：禁止写 `:close-on-click-modal="false"`（详情/查看类弹框点弹窗外必须能自动关闭，关不掉很反直觉）。只有表单填写类弹框在用户明确要求防误关时才可豁免，且需在代码注释里写明原因。
- **表格行点击开详情时，操作列按钮必须 `@click.stop`**：否则点「编辑/删除」会同时触发行点击弹框（`el-table` 的 row-click 冒泡自单元格内按钮）。行详情弹框一律只读（`el-form :disabled` + 页脚无确定键），编辑走独立按钮入口。
- 复用优先级：① `src/components/his/` 下已有骨架与业务组件（`Header` / `Sidebar` / `DashboardLayout` / `PatientSelect` / `InpatientOrderWorkspace` 等）
  ② 同一表单/工作区出现第 2 次就抽成共用组件（带 `mode` 属性区分角色，如 `InpatientOrderWorkspace.vue` 的 `mode="doctor|nurse"`） ③ 最后才是页面内私有实现。
- **选患者一律用 `PatientSelect.vue`**（`v-model` + `@select`），禁止再手写 `el-select` + 搜索逻辑。
- **分页查询只有两个旋钮，都在 `src/lib/pagination.js`**：`PAGE_SIZES = [10, 20, 50, 100]` 与 `DEFAULT_PAGE_SIZE = 10`。
  页面里禁止再写 `:page-sizes="[10, 20, 50]"` 这类字面量数组，也禁止再写 `pageSize: 20` 这类字面量默认值 ——
  一律 `:page-sizes="PAGE_SIZES"` + `pageSize: DEFAULT_PAGE_SIZE`，这样「某页 10 条太少」时只改一处就全站生效。
  **例外**：一次性抓全量/探总数用的 `pageSize`（下拉候选 `pageSize: 200`、统计 `100`、只要 total 的 `1`）不是分页查询，
  保持原值，因为它们不受用户翻页控制；小程序端另有自己的口径。
- **弹层（popper）样式必须写全局 `src/style.css`**：`el-select`/`el-dropdown`/`el-date-picker` 的 popper teleport 到 body，组件 `<style scoped>` 里的 `:deep(.xxx)` 编译后带 `[data-v-x]` 前缀，body 下没有祖先命中 → 规则被**静默丢弃**（现象是"样式没写对"）。下拉加宽还要配 `:fit-input-width="false"`。
- **行数不可控的可编辑表格必须分页 + 过滤**：一行配一个 `el-select`（科室下拉还要摊平上百个 option）时，整表渲染开销随行数线性炸掉 ——
  岗位配置表在演示账号上实测 **1692 行**（94 科室 × 18 角色），点「编辑」后主线程冻几十秒，现象与「后端挂了」完全一样
  （而 `/system/user/getById` 只花 30ms；排查时先用 node 直调接口测耗时再归因，别急着怀疑后端）。
  做法见 `components/his/EmployeePostTable.vue`：**分页**（`PAGE_SIZES`/`DEFAULT_PAGE_SIZE`）＋ 渲染上限；
  **不要加搜索/过滤框**（用户 2026-09-25 明确要求撤掉，配置表就靠翻页看）。
  行内改/删/勾选**必须用行对象上带的全局下标 `__index`**，不能用 `$index` —— 分页后 `$index` 是页内序号，会改错行。
  「主岗位」这类一人只该有一条的开关用 `el-checkbox` 而不是 radio：radio 无法取消勾选，用户要点掉就得先选别的，
  实测很反直觉；改成可自由勾选 + **保存前用 `lib/employeePost.js` 的 `checkPosts` 拦住「一条都没勾」**
  （后端 `replacePosts` 全 0 时兜底提第一条只是 API 直连的最后一道闸，不替代前端提示）。
  只读列表同样要截断渲染（顶栏「切换岗位」只渲染前 50 条并显示总数）。
- **深色底上的 `el-icon` 不能用 Tailwind 透明度色**：Element Plus 的 `.el-icon{--color:inherit; color:var(--color)}` 与 `text-white/70` **同特异性但后加载**，会把工具类盖掉 → 图标退回继承的黑色。要么给容器兜 `color:#fff` + `:deep(svg){fill:#fff}`（见 `Header.vue` 的 `.breadcrumb-nav`），要么用不带 `/` 的纯色。
- **API 层函数签名是前后端的稳定契约**：后端接口改造时只改 `src/api/*.js` 的内部实现，保持导出函数名与入参形状不变，视图层零改动。
- `request.js` 强耦合 `{code,message,data}` → **SSE / 流式接口必须用原生 `fetch`**，不能复用该 axios 实例。
- 主题只动 `src/style.css` + `src/components/his/` 三个骨架，**60 个业务页面不动**；主色 `#1269B5`、辅色青绿 `#0E9488`，风格基调"稳重克制"。
- 前端验证用 playwright-core + Edge 真实浏览器，且**必须断言真实接口回来的数据**；禁止靠断言 ModulePage 的假数据通过验收。控制台零报错才算通过。
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

## 4.  鉴权与按钮级权限（G5b 已踩，勿再犯）

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

## 5.  敏感字段脱敏（手机号 / 身份证 / 邮箱 / 医保卡号）

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

| 层 | 表 | 只回答 | 唯一写入方 |
|---|---|---|---|
| L1 记账 | `biz_fee_record` | 谁该付多少钱、这笔从哪张临床单据来 | `FeeRecordService` |
| L2 结算 | `biz_settlement_bill` + `_item` | 这批应收合计多少、优惠/医保 split/应缴各多少 | `SettlementBillService` |
| L3 支付 | `biz_payment_txn`、`biz_fund_account` + `_txn` | 真金白银进出：几笔、走哪个渠道、流水号多少 | `PaymentService` / `FundAccountService` |
| L4 票据与对账 | `biz_invoice`、`biz_cashier_settlement`、`biz_day_settlement`、`biz_pay_channel_bill` | 凭证与核对：账实是否相符 | `InvoiceService` / `FinanceSettlementService` |

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

| 级 | 场景 | 单据 | 流水类型（`DrugStockChangeTypeEnum`） |
|---|---|---|---|
| ① | 患者退药 | `biz_drug_dispensing` 置 3-已退药（不另建表） | 3 退药回库（正，落药房） |
| ② | 药房退回药库 / 药库下拨 | `biz_drug_transfer` + `_item` | 7 调拨出（负）+ 8 调拨入（正） |
| ③ | 供应商退货 | `biz_drug_supplier_return` + `_item` | 9 退货出库（负） |

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

## 9. 模块包结构：模块  → 分层（二级强制，路径唯一）

- **路径恒为 `com.his.<模块>.<分层>[/impl].XxxYyy.java`，层级到此为止**：
  分层目录**必须**直接挂在模块根下（`com.his.pharmacy.controller` 是正确形态），
  分层目录下面除 `service/impl` 外**禁止任何目录**，模块与分层之间也**禁止**插入业务子域目录
  （`com.his.emr.appoint.pathway.dto` 这类三层包是违规）。
- **参照实现 = 任一模块**（全仓 16 个业务模块已按此形态收口），新代码照它落位。
- **允许的分层目录（名字逐字用）**：
  `controller` `entity` `dto` `vo` `mapper` `service`（实现类进 `service/impl`）`enums` `support` `config` `constant` `util`。
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
  **Mapper 聚合查询返回 `Map<String, Object>` 是允许的**（那是 SQL 结果天然的形状 —— 一行多列没有对应实体），
  但**必须在 service 里转成 VO 再出**，不许一路透传到 Controller：转换写进 Controller 就违反了上条"Controller 禁止任何处理逻辑"。
  统计/字典类接口一样要有名字：`XxxStatVO`、`XxxDictVO`、`XxxCountVO`，禁止拿 Map 当 VO。
  真需要动态结构（交叉表列头由数据决定）时，用 `List<ColumnVO> + List<RowVO>` 这种**有类型的形状**表达，不要退回 Map。
  机械判据：`grep -l "Result<Map\|Result<Object\|PageResult<Map" **/controller/*.java` 必须为空。
- **Controller 里禁止出现任何处理逻辑**：不写业务 if/else、不做状态兜底、不算统计、不拼多表结果、
  **不注入 Mapper**。方法体只允许「取 DTO → 调一个 service 方法 → 返回 Result」。
  机械判据：`grep -l "^import com\.his\..*\.mapper\." **/controller/*.java` 必须为空；
  出现 `for`/`stream` 聚合、`new XxxEntity()`、`if (dto.getXxx() == null)` 这类判断，就是逻辑漏在了 Controller，搬到 service。
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
  机械判据：`grep -A1 "if (dto.get.*== null\|if (!StringUtils.hasText(dto.get" service/impl/*.java`
  里抛「不能为空/必填」的行，除上述三类（带注释标注类别）之外为 0。
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
- **码值 → 文案的映射一律进枚举或字典，禁止独立的「码值→文案」反模式**：任何把码值翻译成文案的
  `switch` / `Map` / `getOrDefault` + `未知(code)` 兜底，**无论它叫什么名字**
  （`XxxLabels` / `XxxText` / `XxxTexts` / `XxxItems` / `XxxRules`，还是 service impl 里的一段内联 `switch`、
  实体上的一个 `getXxxText` 方法），都属于反模式 —— 映射逻辑必须下沉到**枚举**或**字典**，
  调用侧只调 `枚举.labelOf(...)`（展示）或 `DictCacheService.getDicDataLabel(dictType, code)`（字典项）。
  唯一例外：含**临床判定 / 计算口径**（如 Aldrete 评分、相容性判定、记账折算、时长格式化、
  手术安全核查项注册表）的 `support` 类可保留为「内部注册表」，但里面不得再写 `未知(code)` 兜底——
  未知码值一律返回空串。
- **枚举还是字典（落点选取口径）**：按"是否稳定、后端是否拿码值做逻辑判断"决定落点 ——
  ① **变化小、后端要用码值做判断**（状态机流转、权限/分支、计算口径）的封闭集合 → **枚举**
     （全仓通用放 `his-common/enums`，否则放所属模块 `enums`）；
  ② **变化大、由操作员在后台字典维护**（机构自定的类型 / 项目 / 选项）的 → **字典**，走
     `DictCacheService.getDicDataLabel(dictType, code)`，不进 Java 枚举；
  ③ 既有的"集中式大字典"类（`QcTexts` / `CdrStatusTexts` / `SafetyCheckItems` 等）本身就是某域的码值字典，
     按上述口径逐方法下沉到枚举或字典，类可保留为"字典层"，但每个方法只做"调枚举/字典"这一件事。
- **双方法口径**：每个枚举提供两个静态翻译方法，语义严格区分：
  - `labelOf(Integer)`——**展示用**：`null` 或不在枚举内（脏数据）一律返回空串 `""`，
    不回落到某个合法文案、也不暴露「未知(n)」。**绝不返回 null**（返回 null 会把 NPE 风险甩给调用方，
    而返回 `""` 是界面最安全的「无此文案」）。
  - `labelOrUnknown(Integer)`——**异常 / 审计 / 合规用**：`null` 或不在枚举内返回「未知(n)」
    （`null` 本身渲染成「未知」），**保留原始码值**以便排查脏数据。业务异常消息、审计日志、
    合规报表里需要让人看到「到底是哪个脏值」时才用，绝不用它喂前端展示。
  - 机械判据：`grep -rn "未知(" --include=*.java` 命中的，必须只是 `labelOrUnknown` 的方法体、
    或显式 `Objects.toString(xxxEnum.labelOf(...), "未知(n)")` 这类手写等价物，以及 `DictCacheService`
    内部翻译；纯展示路径、独立 `XxxText(s)` 壳类、service 内联 `switch` 里出现「未知(code)」即违规。
  - 迁移进度：patient 模块 5 个纯文案壳类 + 26 个枚举已下沉；VTE 域 `VteRules` 7 个方法全委托枚举、
    4 个 VTE 枚举升级双方法、新增 `VteDiagnosisBasisEnum`/`VteOutcomeEnum`/`VteRiskLevelEnum`；
    emr 的 `QcTexts` 14 个方法全委托枚举（新增 `QcStatusEnum`/`QcResultEnum`/`QcGradeEnum`）。
    其余模块（system / supplies / pharmacy / medicaltech / operation / report / miniapp 及 common 其余枚举）
    仍用旧 `未知(code)` 兜底，属待迁移项——新代码一律按双方法写，存量按此口径逐步收口。
- **文案差异不产生新枚举**：码值相同、中文叫法不同时**复用枚举**（文案以枚举 `label` 为唯一来源），
  不同模块若确有不可调和的措辞差异，差异放在调用侧局部常量 / 方法，且仍调枚举 `labelOf` 做兜底；
  不许为一句话的措辞复制出一个枚举，也不许为改文案去动公共枚举的 `label`。
- **枚举的唯一模板**（`@Getter` + `code`/`label` + `fromCode` + `labelOf` + `labelOrUnknown`）：
  新建与改造到的枚举一律照此写；`labelOf` 必返回 `""`、不得返回 null，异常路径统一走 `labelOrUnknown`。
- **0/1 三兄弟按列注释的含义选，不按字段名前缀选**：`是否 xxx` → `YesOrNoEnum`（YES=1 是 / NO=0 否），
  启用停用 → `EnableStatusEnum`，删除标志 → `DelFlagEnum`。
- **技术阈值不是码值**，继续用 `static final int`：列宽（`W_*`、`*_MAX_LENGTH`）、小数位（`*_SCALE`）、
  条数/长度上限（`MAX_*`、`*_LIMIT`）、时间窗（`*_DAYS`/`*_HOURS`）、分数与置信度、加密参数。
  判据一句话：**这个数是不是「某一列能取的值」——是就枚举，是「多大 / 多少条」就留常量。**
- **枚举化只换表达方式，不换语义**：`XxxEnum.Y.getCode()` 必须与被替换的原值逐字相等。
  Java 码值与库注释不一致时以 Java 现有值为准（改码值=改数据口径，属于独立的、要单独拍板的一步），
  并把冲突单独列出来修，不许在枚举化顺手「修正」。
- **字典权威在 Java 枚举**（重申 §7）：新增/调整码值必须同步 `sql/xxx` 的字典段，两侧同码同义。
- 机械判据：
  `grep -rn "static final int" --include=*.java source/back_end` 里只剩技术阈值常量（逐条核对，业务码值为 0）；
  `grep -rnE "set[A-Z]\w*\(\s*[0-9]+\s*\)|Objects\.equals\(\s*[0-9]+," --include=*.java source/back_end` 结果为 0。

## 8. 凭据分流：登录口令可入库，环境口令一律不入库

口径一句话：**「谁能拿这个口令登录系统」可入库；「能连上这台机器/这个中间件」不可入库。**

| 类别 | 例子 | 落点 | 入库 |
|---|---|---|---|
| 系统登录账号 | `sys_user` 的 480 个账号（`admin` / `shennan` / `13899000001` …，口令统一 `123456`） | `docs/测试账号与凭据.md` | ✅ |
| 环境凭据 | MySQL `xz_feng`、Redis `123456`、Gitea `laochai`、JWT secret、构建工具链口令 | `workspace/环境凭据.md` + `application-local.yml` | ❌ |

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
  git grep -n -I -E "Feng123456!|his-system-jwt-secret|laochai:" --cached -- .
  ```
  无输出才算干净。`docs/er/vendor/mermaid.min.js` 里的 `"0123456789"` 是误报，人工看上下文。
- **改了 `application.yml` 的凭据配置必须重启实测**：`mvn -o -DskipTests install` → 停旧 JVM →
  `java -jar his-web/target/his-backend.jar` → 探 `/api/auth/info`（期望 401）→ 跑登录脚本。
  端点 401 只说明进程活着，**还要跑一次 `POST /auth/login` + `POST /system/dict/refreshCache`**
  才证明 DB 口令（登录）与 Redis 口令（缓存刷新）都真的读到了。
