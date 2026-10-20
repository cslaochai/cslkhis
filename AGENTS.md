# AI Agent 全局指令与项目规范 (AGENTS.md)

> **核心原则（Token 节省与输出约束）**
> 1. **绝对精简**：禁止寒暄、解释、总结。直接输出代码或结构化数据。
> 2. **禁止重复**：只输出**新增/修改**部分，未改动处用 `// ... 现有代码 ...` 占位。
> 3. **结构化优先**：多参数传递用 JSON，不用冗长自然语言。
> 4. **按需阅读**：不一次性输出整个文件，除非明确要求。

---

## 1. 前后端交互与 RESTful 规范

- **方法**：只允许 `POST`（创建/复杂查询）、`GET`（取数，禁止复杂参数）、`DELETE`。
- **URL 命名**（驼峰）：新增/修改 `xxxUpsert`；单条 `getById`；明细（含扩展信息）`getDetailById`；删除 `deleteById`；分页
  `listPage`；下拉 `selectList`；懒加载下拉 `selectListPage`。
- **入参**：一律实体接收。新增/修改 `xxxUpsertDTO`，分页 `xxxPageQueryDTO`（继承 PageParam，见 §7），查询 `xxxQueryDTO`。必填校验用
  `@NotBlank`/`@NotNull` 等注解。
- **出参**：详情 `xxxDetailVO`；列表 `xxxListVO`（或直接 `List<xxxVO>`）；下拉 `xxxSelectListVO`（与接口末段同名口径，禁止
  `XxxSelectVO`）；基本信息 `xxxVO`。
- **枚举/字典**：能做成枚举的做成枚举（性别等）；能存 redis 的（标签等）存 redis 直接用。
- **Long 出参序列化**：Controller 返回给前端的类里主键 id 必须防 JS 丢精度：
  ```java
  @JsonSerialize(using = ToStringSerializer.class)
  private Long id;
  ```
- **分层依赖**：业务处理全在 service；Controller 只依赖 service，禁止直接依赖 mapper。A 依赖 B 只能调 B 的 service，禁止调 B
  的 mapper、禁止复用 B 的 entity/DTO 当契约。
- **表/列注释只写「是什么」**：`COMMENT` = 名词头 +（必要时）码值枚举，如 `状态（1-启用 0-停用）`
  。禁止举例、跨表引用、口径说明、踩坑警告——这些放代码注释或本文件。全库结构基线在 `docs/sql/`（23 个领域文件，由
  `workspace/_gen_ddl_by_domain.mjs` 从 dev 库导出）。
- **状态码**：200 成功 / 201 创建 / 400 校验失败 / 401·403 权限 / 500 服务端异常。
- **响应结构**：统一 `{ "code": 200, "message": "success", "data": {} }`。
- **主键入参一律 `Long`，禁止手写 `parseId(String)` 转换层**：
    - `ToStringSerializer` 只管**出参**；**入参**侧 Jackson 自己能把 `"1857..."` 反序列化成 Long。
      手写转换只有两个下场：① 正则 `\d{1,20}` + `parseLong` 失败返 `null` 不报错 → 下游 `selectById(null)`
      报「数据不存在」，掩盖真因；② 正则放行 20 位、parseLong 只吃 19 位，夹缝抛 `NumberFormatException` 兜成 500。
    - 正确形态：DTO `@NotNull private Long id` + Controller `@RequestParam Long id`，非法值自动 400。
    - 出参不动（保持 String id 或 ToStringSerializer）；同族：主键 `List<String>` + `IN (${ids})` 拼接改 `List<Long>` +
      `<foreach>`。
    - 仅解析**内部裸 SQL 行数据**（Map 里 CAST AS CHAR 的列）可留工具方法，名 `parseRowId`。
    - 判据：`grep -rn "parseId\|parseRowId\|\\d{1,20}" --include=*.java source/back_end` 只允许命中内部行数据那一处。

## 2. 前端界面规范（source/front）

- **禁止使用/引用 `src/components/his/ModulePage.vue`**（演示硬编码壳）。存量引用页面改造时直接换真实接口驱动实现，不许在其上加功能。
- **页面必须真实数据驱动**：`src/api/xxx.js` → 页面渲染 → 操作回写后端。禁止 mock / 写死数组 / 前端自造统计数字。
- **`el-dialog` 默认允许点遮罩关闭**，禁止 `:close-on-click-modal="false"`（详情类弹框点外必须能关）。仅表单类弹框在用户明确要求防误关时豁免，且必须注释写明原因。
- **主页面只留「查询卡 + 表格卡 + 打开弹框」**，新增/修改/详情拆到模块同级独立组件（`views/patients/` 除外）：
    - 命名 = 主视图去 `View` + 角色：`DepartmentsEdit.vue` / `DepartmentsDetail.vue` / `DepartmentsAdd.vue`，与主视图同目录。禁止
      `Dialog` 后缀、禁止 `views/<module>/components/`；同类弹框多时加业务限定（`RxReviewBatchEdit.vue`）。
    - `xxxEdit.vue` 同时承载新增与修改（`row` 空走新增）；字段/布局确实不同才另开 `xxxAdd.vue`。
    - 逻辑跟着弹框走：表单 state、rules、提交、提示全在子组件；父页面只 `v-model` 控显隐 + `@saved="loadData"`。
    - 组件契约统一 `modelValue` prop + `update:modelValue` emit（参照 `PatientDetailDialog.vue`，全仓禁 `defineModel`）。
    - 详情组件自己按 id 取数（`props: {modelValue, id}`）；父页面最多传当前行防闪白。
    - 列表列也用的字典/树由页面加载一次以 prop 下传，禁止两个子组件各拉一遍；仅表单用的下拉随子组件自加载。
    - 不拆例外：`ElMessageBox.confirm` 式无表单确认、<40 行纯动作弹框（作废/扫码/补录）留页面。
    - **搬动就是搬动**：Tailwind 类、`@click.stop`、`v-perm` 码、注释逐字跟着走；禁止改接口入参/字段名/顺手补字段。
    - 机械判据：`node workspace/_check_view_split_gate.mjs` 必须 `GATE PASS`（豁免写
      `workspace/_view_split_exemptions.json`）。浏览器验收 `node workspace/_verify_view_split_ui.mjs <模块目录>`
      ：弹框必须出现（判可见用 `getBoundingClientRect().height > 0`，**不能用 display!==none**——关闭的弹框仍留
      DOM）、编辑/详情字段有真实值、控制台 0 报错。
- **表格行点击开详情时，操作列按钮必须 `@click.stop`**；行详情弹框只读（`el-form :disabled` + 无确定键），编辑走独立按钮。
- **复用优先级**：① `src/components/his/` 骨架与业务组件 ② 同一表单第 2 次出现抽共用组件（带 `mode` 区分角色）③ 页面私有实现。
- **选患者一律 `PatientSelect.vue`**（`v-model` + `@select`），禁止手写 el-select 搜索。
- **筛选条件 >3~4 个用 `.query-grid-wrap` + `.query-grid`**（口径见 `style.css`，参照 `TodayVisitsView.vue`）：每条件固定
  240px（70 标签 + 170 控件）按剩余宽度换行，日期区间挂 `.is-daterange`（312px）。**禁止 `1fr` 等分拉伸**（两字下拉撑到 250px+
  用户判「太宽」），也禁止写死 `!w-xx`（截断日期区间）。查询/重置放栅格外 `.query-grid-ops`，靠右对齐首行。
- **分页只有两个旋钮（`src/lib/pagination.js`）**：`PAGE_SIZES = [10,20,50,100]`、`DEFAULT_PAGE_SIZE = 10`。页面禁止
  `:page-sizes="[10,20,50]"` 字面量与 `pageSize: 20` 默认值字面量。例外：一次性抓全量（下拉候选 200、统计 100、只要 total 的
  1）不是分页查询，保持原值；小程序另有口径。
- **弹层（popper）样式写全局 `src/style.css`**：popper teleport 到 body，组件 scoped `:deep(.xxx)` 带 `[data-v-x]` 前缀在
  body 下**静默失效**。下拉加宽配 `:fit-input-width="false"`。
- **`el-option`/`el-radio` 的 `value` 禁绑 `null`/`undefined`**（EP 必填 prop + `el-radio` 的 nil 会被当「没传 value」退回废弃
  `label`）。「不限/全部/自动判定」用非 nil 哨兵（`'ALL'` / `-1`），只在发请求前一行映射回契约值，回显把 null 归一成哨兵。HTTP
  出参形状必须逐字不变（抓包核对）。
- **行数不可控的可编辑表格必须分页**（参照 `EmployeePostTable.vue`），**不加搜索/过滤框**。行内改/删用行对象全局下标
  `__index`，禁 `$index`（分页后是页内序号，会改错行）。「一人只该有一条」的开关用 `el-checkbox`（radio 无法取消勾选），保存前用
  `lib/employeePost.js` 的 `checkPosts` 拦「一条都没勾」。只读列表同样截断渲染。
- **深色底 `el-icon` 禁 Tailwind 透明度色**：EP `.el-icon{color:var(--color)}` 与 `text-white/70` 同特异性后加载会盖掉。容器兜
  `color:#fff` + `:deep(svg){fill:#fff}` 或用纯色。
- **API 层函数签名是前后端稳定契约**：后端改造只改 `src/api/*.js` 内部实现，导出名与入参形状不变。
- `request.js` 强耦合 `{code,message,data}` → **SSE/流式接口用原生 `fetch`**，不复用 axios 实例。
- **401 与 403 两条路**：401 = 清 token 回登录；403 = 只弹提示，**绝不清 token**。
- 主题只动 `src/style.css` + `src/components/his/` 三骨架，60 个业务页面不动；主色 `#1269B5`、辅色 `#0E9488`，基调稳重克制。
- 前端验证用 playwright-core + Edge 真浏览器，**必须断言真实接口数据**，禁止靠 ModulePage 假数据通过验收；控制台零报错。

## 3. 数据与 SQL 铁律

- **`LocalDateTime`/`LocalDate` 入参必须宽进**：DTO 加 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")`（LocalDate 用
  `yyyy-MM-dd`）。声明后 Jackson 只认空格分隔，前端传 ISO `T` 会 400 且零堆栈。两侧对齐：`el-date-picker` 的 `value-format` 传
  `yyyy-MM-dd HH:mm:ss`，**不传** `toISOString()`。
- **日期字符串边界**：`datetime_col <= 'yyyy-MM-dd'` 会滤掉当天全部时点。按日过滤必须补 `endDate + ' 23:59:59'`（或
  `< DATE_ADD(endDate, INTERVAL 1 DAY)`）。
- **mysql2 出参**：DATETIME 是 JS `Date` 对象，`String(v).slice(0,16)` 得到 `"Wed Sep..."`。要文本用 SQL `DATE_FORMAT`
  别名或脚本 `d2str()`。BIGINT 一律字符串化（Java `ToStringSerializer`；Node `supportBigNumbers + bigNumberStrings` 双开）。
- **裸 SQL（尤其跨模块 `@Select`）先 `information_schema.COLUMNS` 对列名再写**：select 不存在的列编译不报错，运行时
  `Unknown column` 兜成 500，与业务失败零区别（G15 已踩）。
- **写库的「原因/异常」文本先截到列宽**：原始异常拼进 `VARCHAR(500)` 会 `Data too long`，「记账失败」升级 500。`NOT NULL` 无默认列传
  null 整条 insert 失败，写 gateway/biller 先确认必填列有值。
  **这类字段禁止在 DTO 加 `@Size(max = 列宽)`**：入参 400 抢在服务端截断前，把「粘贴长说明」变成请求失败。必填靠 `@NotBlank`
  ，长度靠截断。
- **新写接口的入参格式先探后端 pattern 再写验证脚本**：断言 `code !== 200` 前确认失败原因是业务校验而非格式解析（看
  message 是否「请求体格式不正确」），否则假 PASS。
- **唯一键不含 `del_flag` 的表，整体替换/删除必须物理删**（L12 踩坑）：`BaseEntity.del_flag` 带 `@TableLogic`，`remove()`
  一律软删，留下的行占着 `UNIQUE KEY`，「先清再插」必 `Duplicate entry`。给 Mapper 加显式 `@Delete("DELETE FROM ...")` 的
  `purgeXxx` 并注释写明撞哪个键。纯配置/关联表没有留档价值，不为「能回滚」舍不得物理删。建表二选一：唯一键带 `del_flag`，或 SQL
  注释写死「本表删除走物理删」。
- **`INSERT ... VALUES (...) AS new ON DUPLICATE KEY UPDATE` 里旧值列必须带表名限定**：裸列名命中新旧两行报 `ambiguous`
  ，且只在命中唯一键（走更新分支）时才报——新增月份好好的，已入账月份就炸。写 `IF(表名.col = 2, 表名.col, new.col)`。

## 4. 分层职责与包结构（后端）

### 4.1 包路径：`com.his.<模块>.<分层>[/impl].Xxx.java`，层级到此为止

- 分层目录**必须**直接挂模块根下（`com.his.pharmacy.controller` ✅）；分层下除 `service/impl` 外禁任何目录；模块与分层间禁业务子域（
  `emr.appoint.pathway.dto` ❌）。
- 允许的分层目录（名字逐字用）：`controller` `entity` `dto` `vo` `mapper` `service`(实现进 `service/impl`) `support`
  `config` `constant` `util`。每层只有一个目录，层外不留技术角色包（黑名单见 §5）。
- **`enums` 不是模块分层目录**：全仓枚举唯一落点 `his-common/.../common/enums`（见 §6）。
- **service 一律「接口 + Impl」**：`XxxService` 在 `service/`，`XxxServiceImpl` 在 `service/impl/`。没有第三种形态。
- 「模块太大该再切一刀」= 拆 Maven 模块，不是建三层包。判据按实体维度与数据流（同一实体维度被切断就不拆）。
- **改包名必须同步的连带项**（漏一处 = 运行期 `ClassNotFound`/`Invalid bound statement`）：① 全部 import 与代码体内全限定名；②
  `mapper/*.xml` 的 namespace/resultType/parameterType；③ `@MapperScan`/`typeAliasesPackage`/`logging.level.com.his.*`
  字符串配置；④ **mapper `@Select` 注解文本与 XML 里的 OGNL 静态引用**（`${@com.his.xxx.enums.YyyEnum@Z.getCode()}`
  这种是字符串，编译期零信号——2026-10-17 扫描实锤 EcgMapper/RadioReportMapper 残留旧包名）。
- **挪包验收只认 `mvn -o -DskipTests clean package` + 真启动**：增量编译不清 target 旧 class，新旧同名 Controller 直接
  `ConflictingBeanDefinitionException` 炸在启动。`mapper-locations` 必须 `classpath*:`（多模块 jar 各有一份 mapper/，单
  `classpath:` 只扫最前的）。
- **改包绝不顺带改 URL**：`@RequestMapping` 是前后端契约，包结构是内部事务。

### 4.2 五类各守其位

| 类型           | 唯一职责                                   | 禁止                         |
|--------------|----------------------------------------|----------------------------|
| `entity`     | 映射表（`@TableName`/`@TableField`）、字段与列对齐 | 承载查询条件、挂展示字段、内嵌业务方法        |
| `dto`        | 入参 + 校验注解                              | 持有 entity、被当作出参回传          |
| `vo`         | 出参                                     | 被前端传回来当入参                  |
| `service`    | 业务逻辑、数据范围收口、entity↔DTO/VO 转换、事务        | 被 Controller 绕过直接组合 Mapper |
| `controller` | 接 DTO → 调 service → 包统一响应              | 任何逻辑                       |

- **entity 不出 Controller**：签名只能是 `@RequestBody xxxDTO` → `Result<xxxVO/listVO/IPage<xxxVO>>`。entity 直接透出 =
  把列名当 API 契约；entity 当入参 = 绕过全部校验。
- **Controller 出参禁 `Map<String,Object>` / `Result<Object>`**（Map 的 key 是隐式契约：拼错不报错只渲染空白、Swagger 退化
  `{}`）。「SQL 结果没有对应实体」就建 `XxxStatVO`/`XxxCountVO`/`XxxTrendVO`/`XxxSnapshotVO`，Mapper 直接返回 VO，中间 Map
  转换全删（连带只服务它的 `toLong(Map,String)` 等工具）。真动态结构用 `List<ColumnVO> + List<RowVO>` 有类型形状。判据：
  `grep -l "Result<Map\|Result<Object" **/controller/*.java` 为空。
  保留豁免（当前 15 行，逐条有据）：① `WorkbenchMetricProvider.summary()` + `WorkbenchDataVO.data`（前端按 key 索引 +
  卡片注册表，真动态）；② `OperationQaCapabilityImpl.queryForList`（SELECT 列由模型现场决定）；③ `PromptTemplate.render` 占位符（
  `toMap()` 用 `BeanUtil.beanToMap` 反射字段名）；④ `JwtUtils` claims（jjwt API 形状）。配套：SQL 列别名与 VO 字段名逐字一致；DATETIME
  给 `LocalDateTime` 不再 DATE_FORMAT（小程序出参 `yyyy-MM-dd HH:mm:ss` 文本字段例外）。
- **Controller 禁任何处理逻辑**：不写 if/else 业务判断、不聚合、不 new entity、不注入 Mapper。方法体只允许「取 DTO → 调一个
  service 方法 → 返回 Result」。判据：`grep -l "^import com\.his\..*\.mapper\." **/controller/*.java` 为空。
- **跨模块只走 service**，禁止调 B 的 mapper、禁止复用 B 的 entity/DTO。
- **合并/共用 VO 只并「类型」，绝不并「查询宽度」**：窄接口只填本页渲染的列（合法形态 = 一个类多个 mapper 方法 + 私有 `toXxxVO`
  ，参照 `StaffScheduleServiceImpl`）。禁止为共用类动另一侧查询逻辑。**判据：下拉/列表出参有 `xxxText` 而页面不显示 = 白跑查询
  **（实锤：`CssdTemplateServiceImpl.selectList` 每行跑一次 dictCache 只为一个不渲染的字段）。共用类间搬字段禁
  `BeanUtils.copyProperties`（源 VO 加列时窄接口无声变宽），用显式 setter。验收 = 实测响应 key 集（
  `workspace/_verify_vo_narrow.mjs`）。窄接口外泄 null key 时三个选项让用户定，不许自拍。

### 4.3 入参校验唯一归属地 = DTO 注解

- service 禁写 `if (dto.getXxx() == null) throw new BusinessException("xxx不能为空")`——与 `@NotNull` 是同一件事写两遍，必然漂移，且
  **有注解没 `@Valid` 时注解一句都不生效**，service 那份成了遮羞布。做法：注解声明 + Controller
  `@RequestBody @Valid xxxDTO`（GlobalExceptionHandler 兜 400 + message）。
- **三类必须留 service**（注释标明类别）：① 条件必填（upsert 按 id 分支）——收口用 DTO `@AssertTrue`；② 非 web 入口（跨模块
  SPI、内部指令，Bean Validation 不跑）；③ 业务规则（状态机、码值合法性、关联存在性、临床取值范围）。
- **DTO 对象本身禁判空兜底**：`@RequestBody` 默认 required=true，缺 body 已是 400；`required = false` 是唯一活口，等于「空
  body 静默变默认分页查询」，把漏传 bug 洗成「能用」，全仓 18 处已清零。默认值由 `PageParam` getter 夹取提供。仅第②类留判空，注释写明。判据：
  ```bash
  grep -rnE "\w+\s+\w+\s*=\s*\w+\s*(==|!=)\s*null\s*\?" --include=*.java source/back_end        # 三元兜底
  grep -rn -A2 "if (\w+ == null) {" --include=*.java source/back_end | grep "new [A-Za-z]*DTO"  # 块内重建
  grep -rn "RequestBody(required" --include=*.java source/back_end                              # 活口
  ```
- **GET/`@RequestParam` 不做必填装饰**：默认已必填，不补 `if (x == null) throw`；分页收口在 `PageParam` 的 `@Min/@Max`。
- **三条绑定路各有各的异常**（选错 = 校验静默失效）：① `@RequestBody @Valid` → `MethodArgumentNotValidException`（已接）；②
  GET 对象参数 `@Valid XxxPageQueryDTO` → `BindException`（已接）——`@Valid` 挂参数上才级联字段；③ 标量参数上的约束注解（
  `@RequestParam @NotBlank`）**不要写**：`GlobalExceptionHandler` 没接 `HandlerMethodValidationException`/
  `ConstraintViolationException`，兜成 500。全仓 `@Validated` 已清零，只用 `@Valid`。判据：
  `grep -rn "@Validated" --include=*Controller.java source/back_end` 为空。
- **DTO 主键字段是 `String` 的当 Bug 查**：
  `grep -rnE "private String [a-zA-Z]*[iI]d;" --include=*DTO.java source/back_end` 应为空。豁免：外部厂商报文（
  `OpenAiChatResponseDTO.id`）、`openid`、前端自取 `sessionId`、审计 `targetId`。
- **类级 `@PreAuthorize` 禁用**（见 §8），`@PreAuthorize` 一律标方法。

## 5. 模块依赖：跨模块只走 his-core-spi 端口，模块内强制依赖

- **跨模块访问唯一形态**：契约（接口 + 跨域 VO/DTO）放 `his-core-spi`（`com.his.spi.{service,vo,dto}`），实现类放**表的宿主模块**
  `com.his.<模块>.spi`。`spi` 因此从下面黑名单解禁，且只用于「端口」这一个用途；其余角色包照样禁用。
- **角色包黑名单**（模块根与分层目录都不许出现）：
  `gateway` `capability` `provider` `rule` `evidence` `client` `llm` `prompt` `sign` `qc` `audit`。一律删除，内容按 §4.1 归位。
- **禁跨模块 import `entity`/`mapper`**：跨域出参一律 `XxxBriefVO`/`XxxListVO`，实体→VO 的转换只在宿主模块的 spi
  实现里发生。判据：按模块 `grep -rhoE "import com\.his\.[a-z]+\.(entity|mapper)\." <模块>/src/main/java`
  ，命中只允许是本模块自己。
- **端口实现必须是叶子**（2026-10-20 踩出来的）：`com.his.<模块>.spi` 的适配器只许依赖本模块 mapper
  与无跨模块依赖的 support，**不许注入本模块的编排 Service**。`@RequiredArgsConstructor` 是构造器注入，适配器一牵编排服务，调用方就跟着拖入整张服务图——实测把「无依赖的
  mapper 边」换成「编排 facade 边」，当场造出 7 个 Bean 环，`sqlSessionFactory` 起不来、应用直接启动失败。
- **一个模块按聚合切多个端口**：取数端口（如 `RegistFacadeService`：mapper 级读 + 诊室级联改写，实现只注入
  mapper）与编排端口（`AppointFacadeService`：建单/退号/退款）、`QueueFacadeService`（签到/我的排队）分开放；*
  *纯读取方一律依赖取数端口**。
- **模块内跨类调用写死依赖**：A 需要 B → `@Resource private BService bService;`；禁「接口定义在 A、实现在 B」的 SPI
  反转（跨模块的端口不算反转：契约与实现都按上两条归位），禁 `List<XxxProvider>` / `ObjectProvider` 策略收集器注入，禁运行期
  `ApplicationContext.getBean()`。理由：间接层抹掉「谁调谁」，Spring 注入失败报启动期 `NoSuchBeanDefinition`，与业务错误隔一整个进程。
    - ⚠ 现存唯一豁免（§4.2 Map 豁免①同一处）：`WorkbenchMetricProvider` + 7 个实现方（前端卡片注册表是真动态结构）。除它以外新代码一律直接依赖。
- **循环依赖唯一解法是改依赖方向**：纯读取被编排端口牵住 → 拆叶子取数端口；两边都要写同一状态 → 写入归一到表的宿主；或抽第三个
  `XxxService` 放真正拥有事实的模块。禁 `@Lazy`、禁事件替代调用。
  判据（启动前静态扫）：`python workspace/_scan_bean_cycles.py` → `TOTAL cycles: 0`。
- **外部渠道/医保/支付收口**：统一具体 `XxxChannelService`（调用方直接依赖具体类），联调期模拟在该 service
  内部按配置分支。不要「接口 + Mock 实现」两层。

## 6. 码值与枚举（一列一枚举，唯一落点）

### 6.1 铁律

- **码值 = 某一列的取值**（状态/类型/标志/级别/来源/结果），全部枚举化。禁 `private static final int RS_DRAFT = 1` 常量壳、禁
  `entity.setXxx(2)` / `Objects.equals(1, e.getXxx())` / `.eq(::getXxx, 0)` 裸数字（`@Select`/XML SQL 文本除外）。
- **一列一枚举**：同一含义全仓只许一个枚举；同名字段不同含义是两个枚举，各带业务主语（`BedWaitStatusEnum`/
  `BedAllocateStatusEnum`）。**禁**按表名造 `BizXxxStatusEnum`、**禁**无主语 `StatusEnum`/`TypeEnum`。
- **唯一落点 `his-common/src/main/java/com/his/common/enums`**：模块里禁开 `com.his.<模块>.enums` 包。理由是「新建前 grep
  一个目录就是全量清单」，防同含义重复建。⚠ his-common 的 pom 不依赖 his-clinic，枚举只能引用 common 内部；要带判定逻辑就把逻辑一起上提。判据：
  ```bash
  find source/back_end -type d -name enums -not -path "*/target/*" -not -path "*/his-common/*"   # 必须为空
  find source/back_end -name "*Enum.java" -not -path "*/target/*" -not -path "*/his-common/*" | wc -l   # 必须 0
  ```
- **枚举还是字典**：① 变化小、后端拿码值做判断的封闭集合 → 枚举；② 操作员后台维护的 → 字典
  `DictCacheService.getDicDataLabel(DictTypeConst.XXX, code)`。字典权威在 Java 枚举，改码值必须同步 `sql/xxx` 字典段与列注释。
- **撞同义枚举留「拿码值做业务判断」的当主**（改它 = 改数据口径）。合并前逐字核对码值与文案。公共兜底档（`UNKNOWN(0)`）不是业务码值，
  `isValid`/`getLabel` 都要排除。
- **码值形状相同含义不同不许合并**（`EmergencyTriageLevelEnum` 急诊 I~IV vs `TriageLevelEnum` 门诊四档）。
- **常量壳 + 私有文案 switch 是同一反模式的两半，一次收掉**：新建枚举 + 业务推导（红黄绿区/优先级/时限）留 support 调枚举。
- **文案差异不产生新枚举**：复用枚举，label 为唯一文案来源；确有措辞差异放调用侧局部常量，仍调枚举兜底。
- **枚举化只换表达不换语义**：`getCode()` 与被替换值逐字相等；Java 码值与库注释不一致以 Java 现有值为准（改码值 =
  独立的、单独拍板的一步）。
- **0/1 三兄弟按列注释含义选**：`是否xxx` → `YesOrNoEnum`（YES=1）；启用停用 → `EnableStatusEnum`；删除标志 → `DelFlagEnum`。
- **技术阈值不是码值**，继续 `static final int`：列宽、小数位、条数上限、时间窗、分数、加密参数。判据：*
  *是「某列能取的值」就枚举，是「多大/多少条」就留常量。**
- **零引用枚举必须删**：文件名去后缀全仓 grep 不到（排除自身）即删，不留「备着将来用」。⚠ 重名子串坑：删/改必须 `\b<完整类名>\b`
  全字匹配（`LabAbnormalFlagEnum` vs `UltrasoundAbnormalFlagEnum`）。判据：`node workspace/_check_enum_zero_ref.mjs` →
  `zero-reference = 0`；删完 `mvn -o -DskipTests clean install` 兜底。

### 6.2 码值→文案映射必须进枚举

- 任何翻译文案的 `switch`/`Map`/`getOrDefault + 未知(code)`（无论叫 `XxxLabels`/`XxxTexts`/`XxxItems` 还是内联 switch、实体
  `getXxxText`）都是反模式，必须下沉枚举或字典。**严禁集中式文案壳类**（2026-10-05 已全删 12
  个，禁止再建——哪怕自称「唯一映射处」）。唯一例外：含临床判定/计算口径的 `support` 类可保留业务计算，但**不得承载码值→文案映射
  **，且不得写 `未知(code)` 兜底。
- **禁 `static final Map<Integer,String>` 承载码值文案**——与壳类是同一错误的两种皮（编译期不校验、拿不到
  fromCode/isValid）。保留 4 个注册表：`TransfusionCheckItems`/`OperationCheckItems`/`SafetyCheckItems`/
  `FollowupAdverseItems`（核查项集合+勾稽逻辑，非纯映射）。判据：
  `grep -rn "static final Map<Integer, String>" --include=*.java source/back_end` 为 0。

### 6.3 枚举唯一模板与三方法口径

```java
@Getter
public enum XxxEnum {
    A(1, "甲"), B(2, "乙");
    private final int code;
    private final String label;
    XxxEnum(int code, String label) { this.code = code; this.label = label; }

    public static XxxEnum fromCode(Integer code) {
        if (null == code) { return null; }
        for (XxxEnum item : values()) { if (item.code == code) { return item; } }
        return null;
    }
    /** 展示：合法→label；null/脏值→StringUtils.EMPTY（或 javadoc 声明的缺省文案），绝不返回 null */
    public static String getLabel(Integer code) {
        XxxEnum item = fromCode(code);
        return item == null ? StringUtils.EMPTY : item.label;
    }
    /** 调用方自带缺省文案。⚠ 形参名全仓一律 defaultValue（判据按此名 grep） */
    public static String getLabel(Integer code, String defaultValue) {
        XxxEnum item = fromCode(code);
        return item == null ? defaultValue : item.label;
    }
    /** 异常/审计/合规：null/脏值→未知(n)，保留原始码值便于排查。绝不喂前端展示 */
    public static String getLabelOrUnknown(Integer code) {
        XxxEnum item = fromCode(code);
        return item == null ? (code == null ? "未知" : "未知(" + code + ")") : item.label;
    }
    // 仅在真有调用点（注册表校验、service 拿码值分支）时才写 isValid
}
```

- 展示口径标准名是 `getLabel`（2026-10-17 由 `getText` 全量更名，407 个枚举全在 his-common/enums；`getText`/`labelOf`/
  `textOf`/`descOf`/`whitelistText` 已作废）。字符串码值枚举比较写 `code.equals(item.code)`（`Integer` 字段用 `==`
  是比引用，>127 恒不等）。
- `getLabel` 的 null/脏值出 `StringUtils.EMPTY`（lang3），**禁 netty `StringUtil.EMPTY_STRING`**（传递依赖，pom
  一改就编译失败）。个别枚举显式声明缺省文案（`SysGenderEnum` null→「未知」、麻醉/输血 null→「—」、`EmergencyTriageLevelEnum`
  null→「未定级」）必须在 javadoc 写明。
- **getLabelOrUnknown 禁空壳**：方法体必须真查枚举（`fromCode(code)` + 返回实例文案字段），判据认「最终返回枚举实例的
  label/getLabel()/desc」，别只匹配一种写法。2026-10-06 曾扫出 49 个空壳（合法码值全输出「未知(n)」），已修复。
- **报错文案不枚举合法取值清单**：禁 `throw new BusinessException("只允许 " + XxxEnum.whitelistText())`
  ，一律「xx取值不合法」（清单跟着枚举漂移，操作员看下拉才有正确答案）。
- 33 个无展示 API 的枚举（`DelFlagEnum` 等）属正常，不补方法（补了就是零引用死代码）。
- 机械判据：`node workspace/_check_enum_label_api_gate.mjs` 必须 `GATE PASS`（查旧文案方法定义/调用、空壳、「未知(码值)」归属四条；
  **不查**字段名、查找入口别名、getLabel 重载缺失）。补查三条：
  ```bash
  grep -rn "private final String text" --include=*Enum.java source/back_end | grep -v /target/   # 必须 0
  grep -rnE "getByCode|\bofCode\(|static \w+ parse\(" --include=*Enum.java source/back_end | grep -v /target/   # 必须 0
  grep -rl "public static String getLabel(" source/back_end/his-common/src/main/java/com/his/common/enums | xargs grep -L "String defaultValue"
  # 第三条唯一命中 OperationSafetyCheckItemEnum 是复合键枚举（阶段×码值），刻意豁免
  ```
- 裸码值判据：`grep -rnE "set[A-Z]\w*\(\s*[0-9]+\s*\)|Objects\.equals\(\s*[0-9]+," --include=*.java source/back_end` 为
  0。

### 6.4 权威枚举表（同一含义全仓只许这一个）

| 含义          | 权威枚举与码值                                     | 已删除副本                                                |
|-------------|---------------------------------------------|------------------------------------------------------|
| 启用/停用       | `EnableStatusEnum` 0-禁用 1-启用                | `OperationRoomStatusEnum`、`SurveyTemplateStatusEnum` |
| 正常/异常（结果判定） | `TreatmentRecordStatusEnum` 0-异常 1-正常       | `MaintainResultEnum`                                 |
| 通过/不通过      | `CheckResultEnum` 0-不通过 1-通过                | `DaySurgeryEvalResultEnum`                           |
| 合格/不合格      | `QualifiedResultEnum` 1-合格 2-不合格            | `CssdCheckResultEnum`、`MeteringResultEnum`           |
| 错误级别        | `ClinicalRuleErrorLevelEnum` 1-提示 2-警告 3-严重 | `QualitySeverity`                                    |
| 是否删除        | `DelFlagEnum` 0-正常 1-已删除                    | 禁止再造                                                 |
| 男/女         | `SysGenderEnum` 1-男 2-女 9-未知                | 禁止再造                                                 |
| 是/否         | `YesOrNoEnum` 1-是 0-否                       | 禁止再造                                                 |

- `0-异常 1-正常` 与 `0-正常 1-异常` 是两个方向不许并：`sys_oper_log.status`、审计「0-成功 1-失败」沿用各自列注释与字典孪生，不指
  `TreatmentRecordStatusEnum`。并反了 = 历史行整体洗反。
- **先查后建**：新增映射前先 grep `his-common/enums` 全量清单，有则复用；警惕同名不同口径（欠费管控费用大类 vs
  `PaymentItemTypeEnum` 码值错位）。
- **改名/合并同步三处**：Java 枚举、宿主列 COMMENT（`docs/01-初始化DDL`）、字典行（`docs/02-铺底基础数据/sys_dict_data.sql` +
  `sql/xxx`）。动前先数库里落在废弃档位的行。
- **签名文案例外**：进被签规范化文本（CanonicalText）的措辞不得随枚举 label 漂移；差异在调用侧注释说明。

## 7. 分页 DTO 与参数治理

- **命名固定 `xxxPageQueryDTO`（语序 `PageQuery`），禁止 `xxxQueryPageDTO`/任何 `QueryPage` 拼写**：含内部类
  `XxxDTO.QueryPage` → `XxxDTO.PageQuery`、变量 `queryPageDTO` → `pageQueryDTO`。判据：
  `grep -rn "QueryPage" --include=*.java source/back_end`（排除 target）为 0。
- **分页查询 DTO 一律 `extends com.his.common.base.PageParam`**，禁自己声明 `pageNum/pageSize`（两套默认值 +
  `@EqualsAndHashCode` 漏 callSuper）。正确写法：
  ```java
  @Data
  @EqualsAndHashCode(callSuper = true)
  public class XxxPageQueryDTO extends PageParam implements Serializable { ... }
  ```
- **一个 DTO 一个文件**，文件名 = 类名，带 `DTO` 后缀（VO 同理）。禁 `public static class` 内部类（import 带外部类前缀、Swagger
  看不全、无法跨模块复用）。DTO 不留死代码：定义了无引用的直接删。
- **`@Data`/`@EqualsAndHashCode` 落在有父类的类型必须 `callSuper = true`**：`callSuper=false` 把告警闭嘴同时让父类字段不参与
  equals/hashCode。判据：`node workspace/_scan_eq_call_super.mjs` → `total = 0`。
- **`PageParam` 已加越界夹取（getter 层）**：`getPageNum()` <1 归 1；`getPageSize()` <1 归默认、非导出模式夹到
  `MAX_PAGE_SIZE=200`。用 getter 夹取而不是 `@Min/@Max` 打 400（导出要一次拉 5000 行）。*
  *导出绕过通道 `forExport(int maxRows)`**（`exportMode` transient+@JsonIgnore），新写导出必须走它，别拿外部入参 pageSize
  当上限（现有：`RxReviewServiceImpl.itemExportCsv`、`SysLogServiceImpl.exportCsv`）。⚠ `pageNum/pageSize` 是原始 `int`
  ，直接读字段拿不到夹取，必须走 getter。
- ⚠ `Integer`→`int` 迁移会让兜底代码编译失败且**增量编译掩盖**（实测第 6 轮才炸）：分页 DTO 改造一律以 `clean install` 为准。
- **分页只走 MyBatis-Plus，禁手搓 `selectCount` + `last("LIMIT offset,size")`**：
    - 单体查询（单 `BaseMapper` 宿主）：`Page<Entity> paged = page(new Page<>(pageNum, pageSize), wrapper);`
      （impl 必须挂 `ServiceImpl<Mapper, Entity>`），出参取 `paged.getTotal()/getPages()/getRecords()`。
    - 自定义 SQL：Mapper 方法首参用 `com.baomidou.mybatisplus.core.metadata.IPage<T>`，SQL 里**不写 `LIMIT`**、不传
      `offset/size`，`PaginationInnerInterceptor` 自动套 count + limit。首参是 IPage 才会被拦截，返回 `IPage<T>` 或
      `List<T>` 均可。
    - 手写 `countXxx` 与分页查询同源时直接删；`.last("LIMIT 1")` 只用于 `selectOne` 取单行，不在此列。
    - 判据：`grep -rnE 'last\("LIMIT " *\+ *\(.*\* ?(size|pageSize)' --include=*.java source/back_end` 为 0
      （拼接偏移量的分页；`LIMIT 1`、固定上限不受影响）。
- **`XxxServiceImpl` 必须 `extends ServiceImpl<XxxMapper, Xxx>`，接口 `XxxService` 不 `extends IService<T>`**：
  impl 才有 `page/saveBatch/lambdaQuery` 等基类能力；接口保持业务方法，避免 entity 随 `IService` 公开方法漏进 Controller
  契约（§4.2）。多 mapper 的聚合门面（`XxxFacadeServiceImpl`）无单一宿主，可不挂。
- **码值范围校验走 Bean Validation，禁 service 手写 containsKey 抛异常**（两处副本必漂移 + 绕过 Controller 时失效 + 每个
  service 重复一遍）：
    - 码值区间连续 → `@Min(1) @Max(3)`；
    - 不连续或来自枚举/字典 → `@AllowedValues(values = {"1","2","9"}, message = "...")`（`his-common/validation`，258
      文件在用；`separator` 默认 `,` 可拆串逐元素校验，null/空串放行，必填另加 `@NotNull`）。
    - ⚠ `values` 字面量是枚举码值的第二份副本，无机械判据查一致性，**改枚举必顺手改注解**。
    - **为什么不能一律 @Min/@Max**（实测）：`DeathPlaceEnum`/`DischargeWayEnum` 是 1-5,9（6/7/8 不存在）；`SysGenderEnum`
      1,2,9；`TriageLevelEnum` 等——不连续集合只能逐个列进 `@AllowedValues`。口径：**先看码值连不连续**。
    - service 只留跨字段业务规则与三类豁免（§4.3）；单字段码值合法性一律注解。
    - `isValid` 现状：253 个定义仅 7 处调用——新写枚举只在真有调用点时补 isValid，不为凑模板空写。
- **字典类型编码唯一来源 `com.his.common.constant.DictTypeConst`**（约 200 处引用）：调用点禁写字典类型字面量，一律
  `DictTypeConst.XXX`。常量名 = 编码去 `his_`/`sys_` 前缀大写（`his_prepay_type` → `PREPAY_TYPE`），机械可逆。私有
  `static final String DICT_XXX = "his_xxx"` 禁止。只收代码真用到的（78 个），库里另外 406 个操作员自取类型**不许抄进来**
  （副本冒充全量 = 第二个真相）。`getDicDataLabel` 取不到返回**空串**（与枚举 getLabel 同口径）；写错键名零报错、整列空白，这是它最危险的地方。
  **`biz_<模块>_<XxxEnum>` 形状的键名 = 历次「枚举治理」批量替换留下的假键**（35
  个已逐键清理），遇到陌生键名按序判：查库有没有 → 有则核对码值文案逐字一致 → 无则按 §6 选枚举/字典 →
  新建枚举码值取宿主列注释原文。改完回头删死依赖（不再出现 `dictCacheService.` 调用就删字段与 import）。判据：
  ```bash
  node workspace/_check_dict_type_gate.mjs          # 必须 GATE PASS
  grep -rn "getDicDataLabel(\"" --include=*.java source/back_end      # 必须 0
  grep -rn "getDictDataByType(\"" --include=*.java source/back_end    # 必须 0
  grep -rn "static final String DICT_" --include=*.java source/back_end  # 只允许 DICT_CACHE_PREFIX
  grep -rnE '"(biz|sys)_[a-z]+_[A-Za-z]+Enum"' --include=*.java source/back_end   # 必须 0
  ```

## 8. 鉴权与敏感数据

### 8.1 权限（G5b 已踩）

- **`@PreAuthorize` 一律标方法，禁挂类上**：类级注解**静默覆盖**所有没写自己注解的方法（`/system/menu/userMenus`
  被类级罩住 → 非admin全 403，现象「切角色被踢回登录页」）。只要求登录的公共接口写 `@PreAuthorize("isAuthenticated()")`
  （也在方法上）。
- **401/403 分流**：401 清 token 回登录；403 只弹提示绝不清 token（一个越权请求把人踢出登录是事故）。
- **按钮级权限用 `v-perm`**（数据源 `lib/perm.js` ← `/auth/info` 的 permissions = `sys_menu.menu_type=3` 按钮码）：
  `v-perm="'opd:appointments:add'"` 或数组任一命中；tab/区块级显隐用 `v-if="hasPerm(...)"`（指令移除节点对 el-tabs 不友好）。
    - **码必须逐字取自 `sys_menu`**：库里没有的码 = 任何角色拿不到 = 按钮永久消失。接线前跑对齐核对（前端引用的码 100% 在
      `menu_type=3` 里）。
    - **新增/修改后端是同一 `xxxUpsert`、同一 `:add` 权限**：前端「编辑」按钮挂 `:add` 是正确的，不要凭空造 `:edit` 码。
    - 集合没到/拉取失败时 `v-perm` **放行不收敛**，删掉的节点留注释占位并订阅集合变化（切角色组件实例不重建时真删的按钮回不来）。
- **按钮码铺底见 `sql/100`**
  ：凡被授某页面的角色自动获该页全部按钮码，接线当下行为中性。开关在「角色管理→菜单权限」，前端隐藏 + 后端 403 同时生效。
- **铺底 SQL 的 `sys_menu` id 段必须与同期文件错开，守卫三道键判重，跑完必须回查**：dev 库多会话共用，
  `INSERT ... WHERE NOT EXISTS` 撞别人占的 id 是**静默跳过 0 行**，后果「按钮码任何角色拿不到 = 写操作永久 403」。守卫写
  `NOT EXISTS (id OR menu_key OR permission)`、id 基数用本文件专属段，跑完
  `SELECT id, permission FROM sys_menu WHERE id IN (...)` 回查，再清 `his:perm:role:*` 缓存（TTL 10 分钟）。
- **通用参照数据不配权限码**（科室/角色/字典/药品/ICD 等 `selectList`）：跨岗位复用，挂了 hasAuthority 就「下拉空掉、页面像崩了」。口径统一
  `@PreAuthorize("isAuthenticated()")`。权限码只留给本页面独有业务数据（如 `/pharmacy/wardDispense/candidates`）。
- **字典类数据随 `/auth/info` 一次带回**：判据——某接口唯一用途是「把我自己的编码翻译成名字」，就该并进 `/auth/info`（返回
  `roleNames`），不留在页面挂载时请求。

### 8.2 敏感字段脱敏（手机号/身份证/邮箱/医保卡号）

- **脱敏只能在后端做，前端不许有任何遮码实现**：前端 mask = 没遮（明文仍在响应体）。展示接口出参就打码，页面只渲染
  `xxxMasked`。`src/lib/patientField.js` 的 mask 函数已删除，别抄回来（哪怕 `|| maskIdCard(p.idCard)` 兜底也算，且会渲染期报
  not defined）。
- **口径统一 `his-common/support/SensitiveMaskUtils`**（maskPhone/maskIdCard/maskEmail/maskMiddle）：保留前 N 后
  M、星数=被遮位数、总长不变。禁止各页/各 Controller 再抄（抄了就漂移成两副面孔）。
- **按「调用用途」不按 URL 决定遮不遮**：**编辑回显**（表单数据源，如 `/patient/getById`）必须明文——表单是回填后整对象
  upsert，回显星号会把真号洗成星号（不可逆损坏）。纯展示详情（`getDetailById`）照样脱敏。「本人看自己」另开只读接口（userId 取自
  token、不收参数）。
- **脱敏要成对**：明文置 null + 另给 `xxxMasked`；改了哪个字段的出参就回头确认所有读它的页面换成 xxxMasked、编辑入口是否走
  getById 补全量。漏一处的现象是**整列空白**（比显示明文更难发现）。

## 9. 收费四层铁律（sql/125 起，碰钱全按这条链）

| 层       | 表                                                                                  | 只回答               | 唯一写入方                                         |
|---------|------------------------------------------------------------------------------------|-------------------|-----------------------------------------------|
| L1 记账   | `biz_fee_record`                                                                   | 谁该付多少、来自哪张临床单据    | `FeeRecordService`                            |
| L2 结算   | `biz_settlement_bill` + `_item`                                                    | 合计、优惠/医保 split/应缴 | `SettlementBillService`                       |
| L3 支付   | `biz_payment_txn`、`biz_fund_account` + `_txn`                                      | 真金白银进出            | `PaymentService` / `FundAccountService`       |
| L4 票据对账 | `biz_invoice`、`biz_cashier_settlement`、`biz_day_settlement`、`biz_pay_channel_bill` | 账实相符              | `InvoiceService` / `FinanceSettlementService` |

- **L1 记账行不可修改，错账只能红冲**：金额列写入后禁 UPDATE；冲正 = 负数行（`orig_fee_id` 指原行，原行不动）。只有整行冲完才把原行和名下负行一起置
  4-已红冲（不置会让负行单独留在待结算、净额算成负数），原行 `orig_fee_id` 指向最后一笔负行。禁「红冲原行 +
  新记剩余行」拆行（剩余行不是临床发生的费用，且会破坏幂等判重）。
- **L2 优惠与医保 split 分列**：`discount_amount` 只放院内优惠；统筹 `pool_amount`、个账 `account_amount`、自付
  `self_amount` 各一列。统筹塞优惠列 → 读取侧只能猜、现金清点多出差额。
- **统筹不是支付方式**：`PaymentMethodEnum` 没有「统筹」（后付给医保局，走账单 pool_amount + 报盘）；`4-医保个账` 才是真实收款流水。
- **L3 一笔钱一行、收退同表带符号**：`direction` 1-收 2-退，`amount` 收正退负，日结 SUM 即净额。付清由
  `SUM(成功收款) >= payable_amount` 现算，禁「点按钮翻状态」。流水永不删、金额永不 UPDATE，冲正翻 `txn_status=2` 另起反向流水。
- **L4 对账事实只能来自 L3**：班结/日结/渠道勾对聚合源是 `biz_payment_txn`；渠道对账勾 `local_txn_no ↔ channel_txn_no`
  （旧口径勾 charge_no 方向就是错的）。
- **临床状态不得依赖支付状态**：签到/发药/执行读账单 `bill_status`（或「有未结账单」），绝不读收费单状态列——否则免收、0
  元单、后付费全要在支付层造假。
- **`encounter_type` 不是 `visit_type`**：收费域 encounter（1-门诊 2-住院）；`visit_type` 已是初诊/复诊；`biz_visit` 是另一张表。
- **字典权威在 Java 枚举**（无启动期自检）：`FeeStatusEnum`/`BillStatusEnum`/`PayDirectionEnum`/`PayTxnStatusEnum`/
  `TxnSourceEnum`/`AccountOwnerTypeEnum`/`AccountTxnTypeEnum`/`FeeSourceTypeEnum`/`BillTypeEnum`/`EncounterTypeEnum` 在
  his-common/enums，改码值同步 `sql/125` 字典段。
- 旧表 `biz_charge_info`/`biz_charge_detail`/`biz_refund_info`/`biz_refund_detail`/`biz_prepay` 已退役（sql/126）；*
  *新代码禁写旧表**、禁顺手加列。`biz_refund_apply` 保留（红冲前置审批单，非资金事实）。

## 10. 药品库存两层库位与三级逆向链（sql/154 起，碰药全按这条链）

| 级 | 场景         | 单据                                   | 流水类型（`DrugStockChangeTypeEnum`） |
|---|------------|--------------------------------------|---------------------------------|
| ① | 患者退药       | `biz_drug_dispensing` 置 3-已退药        | 3 退药回库（正，落药房）                   |
| ② | 药房退药库/药库下拨 | `biz_drug_transfer` + `_item`        | 7 调拨出（负）+ 8 调拨入（正）              |
| ③ | 供应商退货      | `biz_drug_supplier_return` + `_item` | 9 退货出库（负）                       |

- **`biz_drug_stock.stock_room` 是库位唯一事实**（1-药库 2-药房）：发药/锁库/退药回库/FEFO 只在药房侧；`biz_drug_stock_log`
  不存 stock_room，看库位 JOIN 批次表（冗余到流水会漂移）。
- **药进药房只有调拨单一条路**；供应商退货只能退药库批次（药库批次挂 supplier_id、药房批次不挂）。
- **调拨一张单出两行流水合计 0**：按单据捞（`selectBySource`）不按批次捞。状态机 1待发出→2待接收→3已完成/4已作废；退货
  1待退货→2已退货/3已作废。
- **成本随货走、同批号按到货额加权**（`addStockToRoom`：`cost = totalAmount / quantity`）。
- **`stock_room` 与单据表族删除走物理删**（唯一键不含 del_flag，见各 Mapper `purgeXxx`）；明细删除同样禁软删。
- **退费对账闸（药品↔收费）**：`PaymentServiceImpl.refund`/`closeBill`/`RefundApplyServiceImpl` 发起前必须过
  `assertDrugReturnedForRefund` → `assertNoDrugPendingReturn`（只认 `dispensing_status=2` 即拒退）。**退费只退钱、绝不替药师办退药
  **：`revertPrescriptionDetail` 只把 1-待发药取消为 4-已取消，已发药/已退药行不动。不为「跑通」放宽闸。

## 11. 岗位（角色×科室）= 身份唯一口径（sql/107 起）

- **身份 = 一行岗位**（`sys_employee_post`：employee_id + role_id + dept_id，唯一键 `uk_emp_role_dept`）。顶栏只有「切换岗位」一个入口；
  `/auth/switchRole`、`/auth/switchDept` 已删，不要复活。
- **`sys_employee_role` 镜像表已删（sql/118）**：鉴权直读 `sys_employee_post`，不复活镜像。`sys_employee.dept_id/dept_name`
  是主岗位快照，唯一写入方 `EmployeePostService.replacePosts`。
- **岗位状态是派生值**：`postStatus`（1-在职 2-已失效）由 expireDate 现算。不落列、不建定时任务——「已失效」唯一事实来源是
  `expire_date`。
- **硬不变量：每人恰一条 `is_primary=1`**（主岗位是**人**的定位 = 人事主科室）。写入归一在 `replacePosts`
  （多勾只认第一条，没勾把第一条提上去）。非主岗位登录落点由 `resolvePrimaryPost` 现算：该角色下与主岗位同科室优先，没有退该角色第一条。存量回填先认现有主科室（
  `(dept_id = sys_employee.dept_id) DESC`）定序，否则全员主科室漂到 dept_id 最小科室。
- **`dept_id NOT NULL`**：「全院岗位」用 NULL 表达已否决——与 `sys_role.data_scope=1` 重复、JWT 表达不出、受限角色挂空科室会让
  `DeptScopeGuard.allowedDeptIds()` 兜底返回 null = 不收口 = 看全院（越权）。
- **`posts` 的 null 与 `[]` 是两件事**：null = 保留原样；[] = 显式清空。医生名册只传 `{id, status}` 时把 null 当空列表 =
  物理删光所有岗位（本表无 del_flag）。
- **切换一律 fail-closed**：`/auth/switchPost` 必须 roleCode + deptId 且岗位行存在，否则拒发 token 并写审计。数据范围随岗位收口（
  `deptIdsOfEmployee` 按**当前角色**取），前端不传科室参数、不给「全部科室」开关。
- **配置入口只有两处**（系统管理→用户管理/员工档案），共用 `EmployeePostTable.vue` + `lib/employeePost.js`；别处要配岗位接同一套。

## 12. 操作人取值（2026-10-06 全仓收口）

- **`UserUtils` 只有一个方法 `getCurrentUser()`**。不新增 `getCurrentEmployeeId()`/`getCurrentEmployeeName()`/
  `requireOperatorName()` 封装——同语义两个出口必漂移（收口前 6 份私有副本各写各的兜底）。要操作人直接链式取：
  ```java
  entity.setCreateBy(UserUtils.getCurrentUser().getRealName());   // 姓名
  row.setOperatorId(UserUtils.getCurrentUser().getEmployeeId());  // 员工ID
  ```
- **姓名口径只有 `CurrentUser.realName`**（登录时无条件填充，缺员工档当场拒登录）。禁回落 `employeeName`/`username`——
  `username` 是账号拼音，混进操作人列会让按人名检索直接漏。
- **禁塞默认值**：`"system"`/`"未知操作人"`/`"系统"`/`String.valueOf(empId)`/`try-catch-return-null` 全禁。取不到就是**报错
  **（NPE 兜 500，或自己抛 BusinessException），不继续执行。塞假值 = 「谁干的」被藏进库，零报错。
- **三处定时任务豁免**（唯一允许落系统值）：

  | 位置 | 值 | 触发 |
    |---|---|---|
  | `DayEndSettleServiceImpl.currentOperator()` | `system:dayEndSettle` | cron 00:10 + 懒触发 |
  | `ExamAppointmentServiceImpl` | `system:examNoShow` | `ExamNoShowTrigger` 每 10 分钟 |
  | `FollowupTaskServiceImpl` | `system:autoFollowup` | `DischargeFollowupTrigger` 每 10 分钟 |

  后两处做法是方法加 operator 参数由调用方显式传，**不是**方法内判空回落。⚠ 改 cron 链路前用调用图查清会不会取当前人，别凭方法名猜。
- **旁路审计特例**（不抛异常，也不塞假名字）：`OperLogInterceptor`/`FieldChangeRecorderImpl`/`PriceServiceImpl`/
  `SysLogServiceImpl`/`AiAuditServiceImpl` 取不到登录态留空/null（能看出「这条没操作人」）。⚠ 取操作人的行在 try 块内会被吞成
  warn——挪到 try 外或明确接受丢失。
- 判据：
  `grep -rnE '"system"|"未知操作人"|"系统操作"|getCurrentEmployeeName\(\)|getCurrentEmployeeId\(\)' --include=*.java source/back_end`
  只允许命中上表三处常量、`ChannelEnum.SYSTEM`、`AiMessageDTO.ROLE_SYSTEM`、`OperLogInterceptor.resolveTitle` 的
  `return "系统"`（操作模块标题，非操作人）。

## 13. 工具三件套与通用收口（TextUtil / NumUtil / TimeUtil）

- **三个收口点**（`his-common/util`，禁第四个同名工具、禁 service/support 私有副本——同名不同实现是本体危害：`nz` 有的返 int
  有的返 long、`cut` 有的返 null 有的返 ""）：

  | 类 | 只管 | 方法 |
    |---|---|---|
  | `TextUtil` | 空白清洗与截断、判空布尔 | `hasText`（反向 `!hasText`，**不提供 isBlank 第二语义**）/ trim / trimToNull / trimToEmpty / nullToEmpty / blankToDefault / cut(v,max) / cut(v,max,blank) / cutToNull / ellipsis / requireTrimmed |
  | `NumUtil` | null 兜底、金额舍入、数量文本 | `orZero(Integer/Long/BigDecimal)` / `orDefault`（**只有 (Integer,Integer) 一个重载**，分页原始 int 就地拆箱）/ `scale(v,位数)` / `plain(v)`（null→null 是刻意的：签名要区分「没有值」和「0」；展示用 `plain(orZero(v))`） |
  | `TimeUtil` | 归一到秒与日边界、时长 | `toSeconds` / `nowSeconds` / `dayStart` / `dayEnd` / `minutesBetween` / `elapsedMinutes` / `elapsedHours` |

- 禁私有副本名单（出现即违规）：
  `trimToNull|tr|trim|safe|defaultStr|nullToDash|nvl|nz|nzAmount|nzInt|cut|clip|truncate|plain|scale|requireText|now|atStart|atEnd|dayStart|dayEnd|minutesBetween|hoursBetween|isText|isBlank|notBlank|firstNonBlank`。
- **判空布尔只有 `hasText`**，行内 `x != null && !x.isBlank()` 禁（一律 `!TextUtil.hasText(x)`）。外部库谓词（Spring
  `StringUtils.hasText`/hutool `StrUtil.isBlank`）算散落第二套口径，一律走 TextUtil——同一实现 ≠ 同一口径，两套拼写并存时改判空规则没有可
  grep 的落点。唯一豁免 `ClinicalTextMatcher.isBlank`（除空白还剥「—」「/」占位符，临床语义）；`x == null || x.isEmpty()`（不收全空白）与
  hasText 语义不同，逐处确认才动。
- **禁 StringTrimmerEditor / DTO Jackson trim 注解**：① 只作用于表单绑定，`@RequestBody` JSON 不查 PropertyEditor；②
  `emptyAsNull=true` 把「传空串清空」变成 null，MP `updateById` 跳过 null 列 = 静默改掉更新语义；③ HV `@Normalized`
  只在校验器里归一，字段值不变；④ 绑定层覆盖不了 83 处 service 内部 `new XxxDTO()`、13 处 `@RequestParam`、5 处 `readValue`
  ——凭空多出「看进门方式决定洗不洗」的第二套口径。
- **不自造时间截断**：全库 1068 个 datetime 列精度全为 0，MySQL 自动四舍五入，Java 再截是重复劳动且掩盖「实体没配
  fill」的真问题。正确顺序：① 查实体 `@TableField(fill = INSERT/INSERT_UPDATE)`（MP `MetaObjectHandler` 全库统一入口）→ 加注解后
  service 不用 setUpdateTime。② 唯一允许截秒的场景：落库回读比较/签名/哈希/防重放/幂等键——收口 `TimeUtil`（曾有 26 个私有副本、4
  种名字）。③ 单纯记时间直接 `LocalDateTime.now()`。
- **时间格式化只认 `DateFormats` 常量**，禁任何地方 `DateTimeFormatter.ofPattern(` / 私有 formatter 常量（收口前 128 处/90
  文件/32 种名字）。9 个常量按四组：`DATE`/`DATETIME`/`DATETIME_MINUTE`/`TIME_MINUTE`（可读）、`COMPACT_DATE`/
  `COMPACT_DATETIME`/`COMPACT_DATETIME_MS`（单号/键/目录名专用，**禁用于展示**）、`ISO_DATETIME`（仅医保/TSA 报文带 T）、
  `STRICT_COMPACT_DATE`（`uuuuMMdd`+STRICT，**仅**身份证出生日期解析——STRICT 才能把 `0230110` 当场判非法；别处解析走
  `LocalDate.parse`）。`DateFormats` 管渲染形状、`TimeUtil` 管归一秒，不造第三个门面。判据：
  ```bash
  grep -rl "DateTimeFormatter.ofPattern(" --include=*.java source/back_end    # 只允许 DateFormats.java
  grep -rl "static final DateTimeFormatter" --include=*.java source/back_end  # 只允许 DateFormats.java
  grep -rn "truncatedTo" --include=*.java source/back_end                     # 只允许 TimeUtil.java
  grep -rnE "StringUtils\.(hasText|isBlank|isNotBlank)|StrUtil\.(is)?Blank" --include=*.java source/back_end
  grep -rnE "\.atStartOfDay\(\)|\.atTime\(23, *59, *59\)|\.atTime\(LocalTime\.MAX\)" --include=*.java source/back_end   # 只允许 TimeUtil.java
  ```
  ⚠ `.atTime(LocalTime.MAX)` 当 `.le(右边界)` 在 DATETIME(0) 列上会被 MySQL 四舍五入进次日，「查某一天」多捞一行且零报错（G12/G14/审计三踩）。
  `atTime(8,0)` 这类业务时刻不是日边界，不并。
- **纯转发的私有文案壳直接删**：`statusText(s){ return XxxEnum.getLabel(s); }` 内联；自带缺省写 `getLabel(s, "—")`
  不加第三层壳。例外：喂 CanonicalText 签名的 `statusText` 是哈希输入，禁动。
- **含业务语义的不算副本**（PDF escape、数字转文本临床判据、按列名拼提示）：判据——**方法体抄到另一模块还成立吗？成立进三件套，不成立留着。
  **
- **依赖字段名 = 被注入类型首字母小写全称**：`@Resource`/`@Autowired`/`@RequiredArgsConstructor final` 字段一律
  `dictCacheService`/`sysUserService`。禁功能别名（`dictText`/`store`）、禁省前缀短名（`filmMapper`）。同类型禁注入两次（出现即合并，不写
  `xxx2` 避重名）。`@RequiredArgsConstructor` 类里依赖字段**必须 final**——非 final 无注解 = 容器不注入，启动通过、第一次调用才
  NPE（实测 22 个类全是 `DictCacheService`，字典翻译一调就 500）。判据：`node workspace/_scan_uninjected_fields.mjs` →
  `total = 0`；`python workspace/_scan_dep_field_names.py` → `total mismatches: 0`。
- **无参构造一律 Lombok**：工具/常量类 `@NoArgsConstructor(access = AccessLevel.PRIVATE)`；POJO/DTO/VO 普通
  `@NoArgsConstructor`；嵌套类注解落内层。**类上已有 `@NoArgsConstructor` 再手写空构造 = 重复构造，编译直接报错**
  （签名只看类名+参数表，访问修饰符不区分）。Spring Bean 类禁手写空构造；枚举不在此列。判据：
  `grep -rnE "\b(private|protected|public)\s+[A-Z][\w$]*\s*\(\s*\)\s*\{\s*\}" --include=*.java source/back_end`。
- **尚未收口的一族（下一步，不是豁免）**：私有「日期文本→LocalDate」解析 9 处（parseDate/parseDateTime 四种子口径互不等价），并成
  TimeUtil 方法 = 替所有人选容错度，**必须先定口径再动，不许批量替换**。

## 14. 注释规范：契约注释必写，AI 味注释禁写

### 14.1 三层契约注释（controller / service 接口 / entity / dto / vo 必写）

- **类头 JavaDoc = 「这是什么」**：一句话点明类在业务里的角色，主语必须是真实身份。
    - Controller：`/** 床位分配控制器：等床队列登记、跨科调配与退回的接口入口。 */`
    - Service 接口：`/** 床位中心服务：等床队列与全院床位状态的业务收口。 */`
    - entity：`/** 患者档案：一名患者一行，就诊身份的事实来源。 */`
    - DTO：`/** 床位分配入参：把等床队列中的患者落到指定床位。 */`
    - VO：`/** 等床队列列表行：护士站床位表格的出参。 */`
- **方法 JavaDoc = 「干什么」+「用在哪个业务场景」，两句缺一不可**（Controller 与 Service 接口方法逐个写）：
    - 场景写**页面 / 流程 / 触发时机**，例如 `用于住院护士站「床位中心」查询卡`、`收银台收款确认后写流水时调用`。
    - **禁写代码调用方**（`调用方：XxxController#method`、URL、行号）——调用链一变整行过期，是腐烂注释。
    - **拿不准就降级**：`src/api/*.js` 有封装函数但 `views/**` 没有页面 import（半接线）→ 写业务流程节点，不写页面名。
    - 参数/返回值有非显然口径（可空语义、单位、时区、码值来源、幂等键）才补 `@param`/`@return`；显然的不写。
- **entity / DTO / VO 每个字段一句「是什么」**：码值字段带码值含义（`状态（1-在职 2-已失效）`，去 `his-common/enums` 核宿主枚举，
  查不到就只写语义、不许猜码值）、金额字段带单位、时间字段区分口径（计划/实际/登记）。挂 `BaseEntity` 的 7 个公共列不重复写（见 §16）。
- **`xxxText` / `xxxLabel` 这类码值→文案镜像字段**只写「××文案（由枚举 label 渲染）」，**不复述码值清单**——清单归枚举，抄进字段注释就是第二份真相。
- **注释只答「是什么 / 干什么 / 用在哪」，不答「怎么实现」**：步骤解说（`// 1.校验 2.落库`）依旧禁。

### 14.2 一份注释只写一次

- **接口写、实现不写**：`XxxServiceImpl` 实现 `XxxService` 时，方法注释全留接口，Impl 的 `@Override` 方法**不挂
  JavaDoc**（两份必然漂移）。Impl 里只留「为什么这么写」的 WHY 行内注释。
- 覆写父类/接口的 `@Override` 一律不重抄注释。
- Controller 方法注释与 Service 方法注释**不算重复**：一个讲页面入口，一个讲能力本身，各写各的场景。

### 14.3 仍然禁止的 AI 味

- **注释禁表名/库结构线索**（`biz_xxx`/`sys_xxx`、列名、SQL 片段、information_schema 口径）。`@TableName`/`@Select`
  代码本身里的表名不算。结构归属 DDL 与 `docs/sql/`。判据：`grep -E "(//|\*+ ).*\b(biz|sys)_[a-z_]{3,}" --include=*.java` 为
  0。
- **复述型壳**（「xxx 服务实现类」「构造器注入」「get/set 方法」）、**生成体头**（@author/@since 模板、`// =====`
  分隔线）、**占位**（TODO/FIXME/「后续可扩展」）。**删注释只删注释，不顺带改代码**。
- **零信息量注释**：`/** 获取列表 */ List<X> listPage()`——重写为 §14.1 的「干什么 + 场景」两句，不许留壳。

### 14.4 该留的 WHY 注释

删掉后下一个读者会做出「错误但看起来合理」的改动才留——隐藏约束、幂等/并发口径、为什么物理删、workaround。WHY 与 §14.1
的场景注释可以同一段共存，WHY 排在场景之后。

### 14.5 机械判据

```bash
node workspace/_check_comment_coverage.mjs            # 五层缺口汇总，必须 GATE PASS
node workspace/_check_comment_coverage.mjs CTRL --detail   # 只看某一层（CTRL/SVC/ENT/DTO/VO）
node workspace/_build_field_glossary.mjs              # 重建字段名词典（存量注释里抽，供全仓措辞一致）
grep -E "(//|\*+ ).*\b(biz|sys)_[a-z_]{3,}" --include=*.java -rn source/back_end   # 注释里的表名，必须 0
```

- 门禁**不查 Impl**（§14.2：注释在接口上），只查 controller / service 接口 / entity / dto / vo 的类头、方法头、字段头。
- `forbidden` 计数含表名线索、TODO、@author/@since、`调用方：X#method` 四种腐烂写法。

## 15. 数据库默认值铁律（§25 全量落地）

- **铁律两半**：① 非 `del_flag` 的 `NOT NULL` 列一律禁 `DEFAULT`；② `del_flag` 必须写 `tinyint NOT NULL DEFAULT '0'`
  （可空+默认同样违规——这半条曾被判据盲区放过，97 张表反复漏）。
- **为什么**：`DEFAULT` 只在列被省略时触发；MP 默认策略对 null 字段直接省列 → `DEFAULT` 静默生效，漏传被填成合法业务值（
  `org_type DEFAULT '1'` → 按该列过滤漏收/错收），比报错更糟。去掉默认后漏传报 `1364`（fail loud），DTO `@NotNull` +
  `@AllowedValues` 前移 400 拦住。
- **`create_time`/`update_time` 禁 DB 默认**：由 `BaseEntity` fill 注解 + `MetaObjectHandler` 填（305 个 create_time 只有
  162 个带默认，不能指望 DB 兜底）。
- **`DEFAULT NULL` 一律删**：`NOT NULL DEFAULT NULL` 自相矛盾；可空列上是冗余噪音。
- 判据：`node workspace/_gate_del_flag_and_notnull_default.mjs` 必须 `GATE PASS`；
  `grep -rn "DEFAULT NULL" docs/01-初始化DDL docs/03-增量SQL变更` 为 0。⚠ 别用 `grep "NOT NULL DEFAULT"` 当判据（抓不到缺
  NOT NULL 那半）。已执行：DDL 侧 + 活库 1649 条 DROP DEFAULT + 97 条 del_flag 补 NOT NULL，回查全 0；防回归跑
  `python workspace/_gen_drop_default_sql.py` 回查 = 0。
- **fail-loud 后续**：insert 路径必须真给 NOT NULL 无默认列赋值。判据只查 `IS_NULLABLE='NO' AND COLUMN_DEFAULT IS NULL`
  （4288 列；可空列有隐式默认 NULL，不会报 1364，拉进来只会虚报）。清单核对用 `node workspace/_probe_notnull_unfilled.mjs`
  。排班域初值统一在 `BizClinicSourceServiceImpl.initNewScheduleRow`（addSchedule 与模板生成共用），判据
  `node workspace/_gate_schedule_domain_defaults.mjs` → `TOTAL unfilled = 0`。

## 16. 业务实体挂 BaseEntity

- **`com.his.common.base.BaseEntity` = 7 个公共列唯一声明处**（id + createBy/createTime + updateBy/updateTime +
  createById/updateById，全带 fill）。新建实体 = `extends BaseEntity` + `@EqualsAndHashCode(callSuper = true)`，不再自己写那
  7 个字段（同名并存会让 MP 表信息出错）。
- **唯一例外：主键列不叫 `id`**（`sys_patient_tag.tag_id`、`biz_referral.referral_id`、`sys_message.message_id`、
  `sys_supplier.supplier_id`、`biz_visit`/`biz_admission`/`sys_bed` 等 15 个）——禁挂基类（基类 id 列在库里不存在），改为实体自带审计字段，
  **每个字段必须带 fill 注解**（`SysPatientTag` 实锤：字段声明了但没 fill，INSERT 依旧漏列 500——判据查注解不查字段名）。
- **危害 = 整条 INSERT 报 1364**：`create_by_id`/`update_by_id` 在 300+ 张表 NOT NULL 无默认，实体没字段 → 列省略 →
  500「系统内部错误」与业务失败零区别（2026-10-12 实锤 88 个实体）。
- 两道门禁 + 一道真打接口：
  ```bash
  node workspace/_gate_entity_audit_columns.mjs   # GATE PASS
  node workspace/_gate_entity_base_match.mjs      # 反向漏改
  node workspace/_verify_entity_audit_columns.mjs # 真 insert/update 回库核 create_by_id
  ```

## 17. 批量操作一律「一次请求」（2026-10-12 收口）

- **一次动作要落多条数据（批量排班/多本证书/批量授权）→ 前端只发一个请求**，展开（人×日）、查重、逐条校验、落库全在一个
  `@Transactional(rollbackFor = Exception.class)` 方法。前端 for 循环调单条 mutation 接口一律算 Bug：① 第 N 条失败前 N-1
  条已各自提交（半成品回滚不掉）；② N 次往返 N 份独立事务，中间别人读到不一致（批量排班撞号源闸就是这么漏的）；③ 进度条掩盖少落。
- **接口形态**：`XxxBatchUpsertDTO` = 批头字段 + `@Valid @NotEmpty @Size(max = 50) List<XxxUpsertDTO> items`（嵌套必须
  @Valid 才级联）。Service `batchUpsert` **自调用**单条 `upsert(item)`（同 bean 同事务），批头归属覆盖条目（
  `item.setEmployeeId(batchDTO.getEmployeeId())`）。报错带条目归属：`"第 " + (i+1) + " 本证书（编号 xxx）：" + e.getMessage()`
  。出参 `XxxBatchResultVO`（created/skipped/message），幂等复刻用 created/skipped 表达跳过，不改覆盖写。
- URL 照 §1：`/schedule/batchUpsert`、`/schedule/replicate`、`/system/employee/qualification/qualificationBatchUpsert`。
- **单条用户动作不算批量**（点一行删除/切一个开关 = 一次一条，是对的）。判据是「一次动作是否要落多条」。
- 机械判据：`node workspace/_scan_frontend_batch_loops.mjs` → `total hits = 0`（抓循环/并发 +
  写接口；抓不到「拆成两次不同接口顺序发」，新批量页面人工确认：hook XHR 记 URL，保存一次写请求恰好 1 条、控制台 0 报错、回库核对条数）。

## 18. 请求幂等与防重提交（2026-10-15 收款幂等键落地）

**三个概念正交**：防重提交管「人别多点」（前端 loading，可绕过）；请求幂等管「请求别多生效」（服务端 requestId +
永久标记）；分布式锁管「代码别多跑」。防重挡不住重放、幂等挡不住并发、锁管不了重放——三者配齐。

- **碰钱的写操作一律接幂等键，用「幂等键模式」不是「锁模式」**：`SET key NX EX ttl`（
  `IdempotentGuardService.tryAcquire/release`，his-system 提供）。**成功不释放**（key 留到 TTL，同 requestId 重放直接拒）；*
  *仅业务失败时释放**。锁模式（成功也释放）防不住双击重放。
- **幂等键放业务校验之前**：tryAcquire 失败直接拒。Redis 不可用降级放行记 warn（防重组件故障绝不阻断收款）。
- **校验与开关归属**：① 字段必填 = DTO 注解；② 业务判断 = DTO 业务方法（`BillPayDTO.idempotentRequested()`）；③ 长度治理 =
  DTO getter 统一截断（`TextUtil.cut(…,64)`），不加 `@Size`、service 不重复截。
- **内部 SPI 调用不校验字段、幂等按需关闭**：患者端/扫码回调/住院账户不传 requestId → 幂等关闭（防重在上层状态机/乐观锁）。*
  *必须关闭**——多个内部调用方先后入账同一账单会拼出同一个 `"billId:null"` key 互相误拦。
- **并发互斥用 DB 行锁 `SELECT ... FOR UPDATE`**（事务内），不引 Redisson。
- 判据：service 里 `TextUtil.cut(.*requestId` 应为 0。

## 19. 凭据分流：登录口令可入库，环境口令一律不入库

| 类别     | 例子                                      | 落点                                          | 入库 |
|--------|-----------------------------------------|---------------------------------------------|----|
| 系统登录账号 | `sys_user` 480 个账号（口令统一 123456）         | `docs/测试账号与凭据.md`                           | ✅  |
| 环境凭据   | MySQL/Redis/Gitea/JWT secret/支付私钥/工具链口令 | `workspace/环境凭据.md` + `application-dev.yml` | ❌  |

- **`application.yml` 与 `config/domain/*.yml` 只放占位**：`${HIS_DB_PASSWORD:}` 等。真值落 `application-dev.yml`
  （.gitignore 按路径精确排除）。⚠ 优先级实测：dev 文件**能**覆盖 `spring.config.import` 进来的领域片段，凭据落 dev 文件即可；
  **不存在 `application-local.yml`**（没 import，写了不加载）。
- `.gitignore` 白名单模式，`workspace/` 天然不入库；`docs/` 放行，新增带凭据文档必须显式加排除。⚠ 排除规则别写 `*凭据*.md`
  （误伤可入库的测试账号文档），按文件名精确匹配。
- **提交前必扫**：
  ```bash
  git grep --cached -n -I -E "Feng123456!|his-system-jwt-secret|laochai:" -- .
  ```
  无输出才干净。⚠ `--cached` 必须排在模式串之前（放后面被当 revision，`fatal: unable to resolve revision`，闸没跑）。
- **改了凭据配置必须重启实测**：起 8080 → 探 `/api/auth/info`（期望 401）→ `POST /auth/login` +
  `POST /system/dict/refreshCache`（证明 DB 口令与 Redis 口令都真读到了）。

## 20. 本机验证环境与重启约定

- 后端 8080 = `run_8080.cmd` 托管实例（classpath 取自 IDEA 调试进程的 `<各模块 target/classes>`，日志 `run8080.log`）；前端
  3000 = vite dev。验证可直接停旧进程重启，不必另开端口；前端文件热更新。
- 重启后端**复用运行中进程的 classpath**（`Get-CimInstance Win32_Process` 拿 `-classpath`），不依赖 `his-backend.jar`
  （常被其他工具锁住导致 repackage rename 失败）。
- **8099 是其他工具（Qoder）的验证实例，勿停勿动**；本项目只认 8080 + 3000。
- dev 库：`192.168.88.132:3306/hn_biz_his`，`xz_feng / Feng123456!`（pymysql 只读/执行均可）。

---

## 附录 A：全仓代码质量扫描结果（2026-10-17，全量源码）

> 扫描方式：5 个并行审查 agent 按 §1–§19 机械判据 grep 全仓（后端 3495 个 Java、前端 740 个 Vue/JS、小程序 31 个 JS），主
> agent 逐条复核确认。以下为**确认存在**的问题，按严重度排列。

### A1 高危（真 bug / 运行期必炸）

| # | 问题                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | 位置                                                      | 违反                  |
|---|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------|---------------------|
| 1 | ~~**mapper SQL 残留挪包前的旧枚举全限定名**~~ **已修复（2026-10-17）**：4 处 `${@com.his.medicaltech.enums.ReportTypeEnum@...}` 全部改为 `#{reportType}` 参数绑定，码值由 service 传 `ReportTypeEnum.INSPECTION.getCode()`（编译期可见）；放射台零引用的 `selectWorkbenchByRecordId` 死方法已删。验证 = `mvn clean install` + 8081 临时实例真启动，心电/放射工作台 listPage 双双 200 且报告关联正确。**教训固化：mapper 注解 SQL 里禁任何 `${}` 插值承载码值，一律参数绑定**                                                                                                                                                                                                                                                                                                                                                | ~~EcgMapper:50,111、RadioReportMapper:39,103~~           | §4.1                |
| 2 | ~~**`AiPromptTemplateController` 整文件违反分层**~~ **已修复（2026-10-17）**：Controller 瘦身到「收 DTO → 调 service → 包 Result」，方法级 `@PreAuthorize` + `@Valid`；新增 `AiPromptTemplatePageQueryDTO`（继承 PageParam）/`AiPromptTemplateUpsertDTO`（@NotBlank）/`AiPromptTemplateVO`（id ToStringSerializer、时间 @JsonFormat）；业务逻辑（版本号计算、停用→启用的多步状态迁移、`PromptTemplate.evictCache`）全数下沉 `AiPromptTemplateServiceImpl`，写路径加 `@Transactional(rollbackFor)`；判空改抛 `BusinessException("版本不存在")`（400 语义）；顺带修 `PromptTemplate.load` L105 裸码值 → `YesOrNoEnum.YES.getCode()`，并删前端 `ai.js` 两个死函数（getByName/availableNames 后端本无实现）。验证 = `mvn clean install` + 8081 临时实例四接口实测：listPage total=16、history 12 版生效版置顶、upsert 出 v2.2、activate 切回 v2.1 且库状态复原（测试行已删） | ~~`ai/controller/AiPromptTemplateController.java` 全文件~~ | §4.2 五类各守其位、§1 出参规范 |

### A2 中危（规范违反，有真实回归风险）

| # | 问题                                                                                                                                                                                                                                    | 位置                                                                    | 违反     |
|---|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------|--------|
| 3 | `QueueController` 类级 `@PreAuthorize("hasAnyAuthority(...4个list码...)")`：无方法注解的方法（如 `stats`）被静默罩住；未来新增方法不带注解 = 零感知继承类级权限                                                                                                                | `appoint/controller/QueueController.java:25`                          | §8.1   |
| 4 | ~~`AiPromptTemplateController` 类级 `@PreAuthorize("isAuthenticated()")`~~ **已随 A1#2 修复（2026-10-17）**：重写后类级注解删除，仅保留方法级 `hasAuthority('ai:operationQa:config')`                                                                          | ~~同上 L18~~                                                            | §8.1   |
| 5 | **小程序两份硬编码 baseUrl 互不一致**：`app.js` globalData 为 `http://192.168.42.1:8080`，`utils/request.js` 为 `http://localhost:8080/api`——两个真相，换环境必漏改一处；且 `app.js` 直接读 `'ai_sql_token'` 字面量，未复用 `utils/auth.js` 的 TOKEN_KEY 封装                     | `source/miniapp/app.js:4,11`、`source/miniapp/utils/request.js:3`      | 配置唯一真相 |
| 6 | `GlobalBindingConfig` 注册 `StringTrimmerEditor(true)`：emptyAsNull 会把「传空串清空字段」洗成 null，MP updateById 跳过 null 列 = 静默改掉更新语义；且只覆盖表单/GET 绑定、覆盖不了 @RequestBody——正是 §13 论证并否决的方案，配置却还挂着（`GlobalExceptionHandler` 见 `his-web/exception`，两处口径相反） | `his-web/src/main/java/com/his/config/GlobalBindingConfig.java:13`    | §13    |
| 7 | 诊疗指南详情弹框 `:close-on-click-modal="false"`（详情类禁止防误关，无豁免注释；同文件另有一处）                                                                                                                                                                      | `views/doctor-workstation/DoctorWorkstationView.vue:1703`（另 L1807 附近） | §2     |

### A3 低危 / 待收敛

| #  | 问题                                                                                                                                                                                                                                                                                  | 位置                                                 | 说明                                                                                            |
|----|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------|-----------------------------------------------------------------------------------------------|
| 8  | 修改密码弹框 `:close-on-click-modal="false"`                                                                                                                                                                                                                                              | `components/his/Header.vue:278`                    | 表单类弹框可豁免，但 §2 要求注释写明原因——补注释或去掉                                                                |
| 9  | `${column}` 动态列名                                                                                                                                                                                                                                                                    | `patient/mapper/SysOrderDictDataMapper.java:28-29` | 有 `OrderDictTypes→OrderDictTypeEnum.orderColumn()` 白名单守卫 + javadoc，属可接受模式；新增列必须走枚举，防 `${}` 扩散 |
| 10 | **规范内部冲突**：§5 禁 `List<XxxProvider>` 收集注入，§4.2 豁免 `WorkbenchMetricProvider`（7 实现方），`WorkbenchServiceImpl:54` 注释明示用 List 注入容忍零实现                                                                                                                                                      | `system/service/impl/WorkbenchServiceImpl.java:54` | 已按本版 §5 折中：现存豁免仅此一处，新代码一律直接依赖——后续若卡片注册表改静态，应一并收口                                              |
| 11 | **医技检查记录孤儿科室引用（扫描后真启动暴露）**：dev 库 28 条 `biz_inspection_record.inspection_dept_id` 指向 102/105/106，而 `sys_department` 主键是 1958xxx 系列 → 科室 IN 收口后工作台恒空。**已修（2026-10-17）**：直接重映射 102→19580036 放射科、105→19580034 医技科室、106→19580004 消化内科，复测双接口 records/total 正确。教训：造测试数据必须引用主数据真实主键，别自造短 ID | dev 库 biz_inspection_record                        | 种子数据口径                                                                                        |

### A4 验证为合规的高频项（本次未发现问题）

枚举唯一落点（407 个全在 his-common/enums）、ModulePage 零引用、`patientField.js` 无前端遮码复活、`PAGE_SIZES`/
`DEFAULT_PAGE_SIZE` 全站引用、`defineModel` 零使用、`@Validated` 全仓清零、`Result<Map` controller 层清零、controller 注入
mapper 已清零（A1#2 修复后）、`patientField.js` 时间截断与金额文案口径正确。

> 后续修复顺序建议：~~A1#1~~（已修）→ ~~A1#2~~（已修，连带 A2#4）→ A2 各项（剩 #3/#5/#6/#7）→ A3。每修一项跑对应机械判据。
