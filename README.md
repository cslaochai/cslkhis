# cslk-HIS · 三甲医院信息化系统（Hospital Information System）

一套面向三甲医院业务闭环的全栈 HIS 系统：模块化单体后端 + Vue3 管理端 + 微信患者端小程序，覆盖挂号预约、门诊医生/护士站、电子病历、药房药库、收费结算、检验检查、急诊、院感上报、耗材设备、统计上报与 AI 辅助（ICD-10 编码推荐）等域。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 21 · Spring Boot 3.2.5 · MyBatis-Plus 3.5.6 · MySQL 8 · Redis · JWT (jjwt) · Knife4j (OpenAPI) · Hutool · OpenPDF · ZXing |
| 管理端 | Vue 3.5 · Vite 8 · Element Plus · Tailwind CSS 4 · Pinia · Vue Router · Axios |
| 患者端 | 微信小程序（原生） |
| 数据库 | 分模块增量 SQL 脚本（`source/back_end/sql/`，1~100+ 编号） |

## 目录结构

```
cslk_his/
├── source/
│   ├── back_end/          # Maven 多模块后端（模块化单体）
│   │   ├── his-common/    #   底座：公共实体、签名/时间戳(TSA)、工具
│   │   ├── his-security/  #   认证鉴权：JWT、按钮级权限
│   │   ├── his-system/    #   系统管理：用户/角色/菜单/字典
│   │   ├── his-patient/   #   患者主数据、就诊人
│   │   ├── his-pharmacy/  #   药房药库、摆药发药
│   │   ├── his-appoint/   #   排班、班次、预约挂号
│   │   ├── his-supplies/  #   物资耗材（含高值耗材 UDI 扫码溯源）、CSSD 器械包
│   │   ├── his-equipment/ #   设备后勤
│   │   ├── his-medicaltech/ # 检验检查（LIS/PACS 报盘）
│   │   ├── his-emergency/ #   急诊
│   │   ├── his-emr/       #   电子病历、门诊医嘱、院感上报
│   │   ├── his-charge/    #   收费结算、医保
│   │   ├── his-report/    #   统计上报
│   │   ├── his-ai/        #   AI 能力接入（ICD-10 召回推荐等）
│   │   ├── his-web/       #   启动层（唯一 Spring Boot 入口，端口 8080）
│   │   └── sql/           #   数据库初始化与增量脚本
│   ├── front/             # Vue3 管理端（npm run dev → :3000）
│   └── miniapp/           # 微信患者端小程序
├── docs/                  # 设计与施工文档（功能缺口大纲、闭环计划、各模块施工方案）
└── AGENTS.md              # 开发规范（前后端契约、命名、鉴权铁律等）
```

## 快速开始

### 前置要求

- JDK 21+、Maven 3.9+
- MySQL 8.0+、Redis
- Node.js 20+、pnpm

### 1. 初始化数据库

按编号顺序执行 `source/back_end/sql/` 下的脚本（`1-HIS数据库初始化.sql` 起步，增量脚本按编号递增执行）。

### 2. 启动后端

修改 `source/back_end/his-web/src/main/resources/application.yml` 中的 MySQL / Redis 连接信息，然后：

```bash
cd source/back_end
mvn clean package -DskipTests
java -jar his-web/target/his-web-1.0.0.jar
# 或 IDE 直接运行 com.his.HisApplication，服务监听 :8080
```

接口文档（Knife4j）：`http://localhost:8080/doc.html`

### 3. 启动管理端

```bash
cd source/front
pnpm install
pnpm dev        # 默认 http://localhost:3000
```

### 4. 患者端小程序

用微信开发者工具导入 `source/miniapp/`，修改 `app.js` 中的 `globalData.baseUrl`（默认 `http://localhost:8080`）指向你的后端服务。

## 架构要点

- **模块化单体**：业务域各自成 Maven 模块，只有 `his-web` 是启动层；跨模块只依赖对方 Service，禁止直连 Mapper。
- **接口契约**：RESTful + 统一响应体 `{code, message, data}`；入参 `xxxDTO`（带校验注解）、出参 `listVO/detailVO/SelectListVO`；主键 ID 序列化为字符串避免前端精度丢失。
- **鉴权**：JWT 登录态 + `sys_menu` 驱动的菜单/按钮级权限（前端 `v-perm` 指令 + 后端方法级 `@PreAuthorize`）。
- **电子签名**：内置 TSA 时间戳网关（`his-common/sign`），支撑病历/处方签名合规。
- 详细开发规范见 [AGENTS.md](AGENTS.md)，业务规划与施工方案见 [docs/](docs/)。

## 文档索引

- [三甲HIS功能缺口大纲](docs/三甲HIS功能缺口大纲.md)
- [三甲HIS业务闭环打通计划](docs/三甲HIS业务闭环打通计划.md)
- [患者端小程序功能规划与实施方案](docs/患者端小程序功能规划与实施方案.md)
- [AI能力接入方案](docs/AI能力接入方案.md)
- [测试账号与凭据](docs/测试账号与凭据.md)

## License

私有项目，保留所有权利。
