# 病历签名对接 csssl 院内 CA：端到端联调记录与本地签名体系盘点

> 项目：cslk（院内 HIS）+ csssl（院内 CA 根认证中心）
> 联调时间：2026-10-27
> 结论：**HIS 医生病历签名已真实走通 csssl 签发的证书**（外部 CA 模式，RA 公钥签发），验签通过。

---

## 一、端到端链路验证结果

| 环节 | 结果 | 关键凭证 |
|---|---|---|
| HIS 触发签名 | 通过 | 接口 `POST /api/emr/signature/sign`（补签，admin），返回 code=200 |
| 签名对象 | 门诊病历 | bizNo=MR202609141211260002（bizType=2，患者：老刘） |
| HIS 检查证书 | 无有效证书 → 触发外部签发 | admin（emp_id=1）遗留本地证书已先行吊销 |
| HIS 本地生成密钥对 | 通过 | RSA2048，私钥由主口令 PBKDF2+AES-GCM 加密后落库（key_algo=RSA2048） |
| HIS → csssl 登录 | 通过 | SM2 C1C3C2 口令加密，服务账号 admin |
| HIS → csssl 公钥签发 | 通过 | `POST /ssl/admin/certs/public-key`，certType=CLIENT，DN=`CN=超级管理员,O=长沙市麓康医院` |
| csssl 签发回执 | 通过 | serial=5c5fe91a76c05bf977a4a2a76f2f087，issuer=CSSSL SubCA - Server Signing，有效期 365 天 |
| HIS 下载证书链 | 通过 | 叶子 + SUB_CA，PEM 2904 字符 |
| HIS 落库 | 通过 | `sys_sign_cert`：cert_pem/ca_serial/ca_issuer 三列齐备，issued_mode=2（外部CA） |
| 病历签名 | 通过 | signNo=SIG20261027000001，sign_status=有效 |
| 验签 | **通过** | signatureValid=true，contentMatched=true，结论“签名值有效，且内容自签名后未发生变更” |
| csssl 侧证书台账 | 可见 | /ssl/admin/certs 第 3 条：subjectCn=超级管理员，CLIENT，VALID，createdBy=admin |

**链路图**

```
医生签名入口(补签/病历签) ──> SignCertFacadeService.ensureActiveCert
        │ 无有效证书
        ▼
issueViaExternalCa: 本地产RSA2048密钥对 → 私钥主口令加密落库
        │
        ▼
CssslCaClient: SM2口令加密登录csssl → POST /admin/certs/public-key(公钥+DN)
        │
        ▼
csssl: SUB_CA签发CLIENT证书(CDP/AIA已内嵌) → 审计CERT_PUBKEY_ISSUE
        │ 证书链(叶子+SUB_CA)回传
        ▼
HIS: cert_pem/ca_serial/ca_issuer落库 → SHA256withRSA签名 → 验签(本地公钥)通过
```

---

## 二、联调中发现并修复的问题

| # | 问题 | 修复 |
|---|---|---|
| 1 | `BusinessException` 无 `(String, Throwable)` 构造，CssslCaClient 双参调用编译失败 | 改为单参拼接消息（异常链信息并入 message） |
| 2 | `HttpRequest.newBuilder(request)` 不合法（无该重载） | send 方法统一改为 `newBuilder(URI)` + token/body 分支构造 |
| 3 | `ExternalCaChannelService.IssuedCert` record 字段 caName 与实现不一致 | 统一命名为 caIssuer（三字段 certPem/caSerial/caIssuer） |
| 4 | csssl `X500Name.getInstance(String)` 不存在 → “unknown object in getInstance” 400 | 改为 `new X500Name(subjectDn)`（RFC2253 构造） |
| 5 | 运行库 `hn_biz_his.sys_sign_cert` 缺 cert_pem/ca_serial/ca_issuer 三列 | 已 ALTER TABLE 补齐（DDL 三处源文件同步更新） |
| 6 | admin 遗留本地证书私钥解密失败（Tag mismatch，旧加密配置） | 已吊销遗留证书，重新走 csssl 签发（这类旧证书属可清理项，见下） |

---

## 三、本地签名体系盘点（可删除 / 可替换 / 保留）

> 原则：签名是医疗文书证据链的一部分，**历史签名行与历史证书不能物理删除**（验签与追溯依赖它们）。
> 可清理的是“实现路径、遗留测试数据、冗余配置”，一律用吊销/停用代替硬删，删数据只删确认无引用的测试行。

### A. 可直接清理（联调已完成，无引用风险）

| 项 | 位置 | 说明 |
|---|---|---|
| 遗留本地证书数据 | `hn_biz_his.sys_sign_cert` | 旧 self-issue 证书（issued_mode=1）多为测试遗留；有效的一律先吊销（cert_status=2），确认无在途签名引用后可物理删行，保留审计在 revoke 字段 |
| 旧配置残留 | `application-dev.yml` 签名段 | 已新增 ca-mode=external 段；无残留需清 |
| 前端旧“本地自签”文案 | `front/src/views/.../SignatureCenterView.vue` 等 | “证书信任级别/本地自签”说明文案在 external 固化后可改为“CSSSL 院内CA签发” |

### B. 代码层：可替换/可下线（不删，留作切换与回退）

| 实现 | 现状 | 建议 |
|---|---|---|
| `SignCertServiceImpl.doIssue` 院内自签分支（issued_mode=1） | external 已可用 | 保留为回退选项；若医院定稿只用外部 CA，可把 `his.sign.ca-mode` 默认值改为 external 并冻结 internal 分支（不建议物理删除，防审计追查） |
| `KeyPairFactory`（RSA2048） | external 模式继续使用 | 保留；后续 SM2 升级时扩展（WS/T 847 要求） |
| `SignCryptoUtil`（SHA256withRSA） | 联调验证可用 | 保留；SM2 升级时加 SM3withSM2 |
| `KeyProtectorUtil`（主口令+PBKDF2+AES-GCM） | 外部模式继续保护本地私钥 | 保留 |
| `ExternalCaChannelService` 原打印桩实现 | 已重写为真实调用 csssl | 旧桩代码已被替换，无需额外清理 |

### C. 明确保留（证据链/审计必需）

- `biz_emr_signature` 全部历史签名行（含已作废）——验签与司法举证依赖
- `sys_sign_cert` 历史证书行（吊销后可保留或归档），证书指纹/公钥是验签依据
- csssl 侧 `ssl_ca_cert` 台账 + `ssl_audit_log` 审计日志（CERT_PUBKEY_ISSUE 已留痕）

### D. 联调测试数据（本次产生，确认无引用后可清）

| 数据 | 位置 |
|---|---|
| 补签记录 SIG20261027000001 | `hn_biz_his.biz_emr_signature` |
| 新证书 CERT202610270001（emp_id=1） | `hn_biz_his.sys_sign_cert` |
| csssl 侧 id=3 证书（CN=超级管理员） | `ssl_ca.ssl_ca_cert` |
| 吊销的 4 条遗留证书 | `hn_biz_his.sys_sign_cert`（cert_status=2） |

---

## 四、配置说明（对接 csssl 所需）

```yaml
# application-dev.yml / application-pro.yml 的 his.sign 段
his:
  sign:
    master-secret: <主口令>            # 本地私钥加密用，两环境各配
    ca-mode: external                  # internal-院内自签 / external-外部CA(csssl)
    ca-base-url: http://localhost:8092/ssl   # pro 用环境变量 HIS_SIGN_CA_BASE_URL
    ca-username: admin                 # csssl 服务账号（HIS_SIGN_CA_USERNAME）
    ca-password: "123456"              # csssl 服务账号口令（HIS_SIGN_CA_PASSWORD）
    ca-timeout-ms: 5000                # 调用 csssl 超时（HIS_SIGN_CA_TIMEOUT_MS）
```

> 设计约束：external 模式下 csssl 未返回证书即中断签发，**绝不静默回退院内自签**（防止“看起来签了，实际链不成立”）。

---

## 五、遗留与后续建议（按优先级）

1. **SM2 医生签名证书**（合规项，WS/T 847—2024 强制 SM2/SM3、私钥不可导出）：csssl 已支持 SM2 公钥签发；HIS 侧需扩展 `KeyPairFactory`/`SignCryptoUtil` 支持 SM2/SM3withSM2 后再切换，当前联调用 RSA2048 先行打通。
2. **双证书 + KMC 密钥托管**（GM/T 0034）：加密证书托管恢复，医疗场景可先只做签名证书，架构已预留。
3. **时间戳 TSA**：医疗文书要求“权威时间源+签名同时打戳”，当前 time_source=1（本机时钟），下一步接院内授时/第三方 TSA。
4. **生产部署**：csssl 根私钥已 SM4 加密落盘 + 根可封存（P0）；对接生产环境前按密评要求复核“根离线、私钥不出密码机、三权分立”。
5. **医保链路**：cslk→csmig→医保，接口层 SM2 签名+SM4 加密双向验签，与院内 PKI 是两条线；csssl 可承载院内证书体系，医保凭证仍由国家医保平台签发（XJ-D01-2019），不混用。
