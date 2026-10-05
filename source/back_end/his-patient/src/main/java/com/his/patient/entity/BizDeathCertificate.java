package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 居民死亡医学证明（推断）书（死亡医学证明书，sql/157）。
 *
 * <p>口径：
 * <ol>
 *   <li>一次住院同时只允许一张<b>有效</b>证明（草稿/已审核/已开具都算占用）；改错走
 *       「作废原证 + 另起新证」，新证 {@code origCertId} 指向原证，原证内容永不修改
 *       （法定文书留痕，同收费四层红冲口径）。</li>
 *   <li>一般项目全是<b>快照</b>：证明是对外法定凭证（户籍注销、殡葬火化），必须能脱离患者档案独立回看。</li>
 *   <li>{@code idCard} / {@code relativePhone} 明文入库，展示位一律由 VO 出参脱敏，
 *       只有编辑回显（getDetailById）保持明文（AGENTS.md 第 5 条）。</li>
 *   <li>{@code reportPayload} 是死因监测上报报文（当前不对接外部平台，落库即留痕）；
 *       已上报的证明连同报文一起冻结。</li>
 * </ol>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_death_certificate")
public class BizDeathCertificate extends BaseEntity implements Serializable {

    /**
     * 证明编号（DC + yyyyMMdd + 4 位，院内流水）
     */
    private String certNo;

    /**
     * 住院记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 死亡出院记录ID（签发时回填，证明与出院事实互相锚定）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dischargeId;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 死者姓名（快照）
     */
    private String patientName;

    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;

    /**
     * 民族（快照）
     */
    private String nation;

    /**
     * 出生日期（快照）
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    /**
     * 死亡年龄
     */
    private Integer age;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 职业（快照）
     */
    private String occupation;

    /**
     * 婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）
     */
    private Integer maritalStatus;

    /**
     * 死亡时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deathTime;

    /**
     * 死亡地点（1-医院 2-来院途中 3-家中 4-民政管理机构 5-其他机构 9-未指明）
     */
    private Integer deathPlace;

    /**
     * 死亡科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deathDeptId;

    /**
     * 死亡科室名称（快照）
     */
    private String deathDeptName;

    /**
     * 死亡病区名称（快照）
     */
    private String deathWardName;

    /**
     * 死亡床位号（快照）
     */
    private String deathBedNo;

    /**
     * 死亡诊断
     */
    private String clinicalDiagnosis;

    /**
     * 根本死因ICD-10编码
     */
    private String underlyingIcdCode;

    /**
     * 根本死因名称（快照）
     */
    private String underlyingIcdName;

    /**
     * 既往病史
     */
    private String pastHistory;

    /**
     * 是否尸检（0-否 1-是）
     */
    private Integer autopsyFlag;

    /**
     * 尸检结论/病理诊断
     */
    private String autopsyResult;

    /**
     * 死者近亲属姓名
     */
    private String relativeName;

    /**
     * 与死者关系
     */
    private String relativeRelation;

    /**
     * 近亲属联系电话
     */
    private String relativePhone;

    /**
     * 填表医师ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long physicianId;

    /**
     * 填表医师姓名
     */
    private String physicianName;

    /**
     * 填表时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fillTime;

    /**
     * 审核人ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewerId;

    /**
     * 审核人姓名
     */
    private String reviewerName;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reviewTime;

    /**
     * 审核意见
     */
    private String reviewOpinion;

    /**
     * 状态（1-草稿 2-已审核 3-已开具 4-已作废）
     */
    private Integer certStatus;

    /**
     * 签发（出具/盖章）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issueTime;

    /**
     * 最后打印人
     */
    private String printerName;

    /**
     * 打印次数
     */
    private Integer printCount;

    /**
     * 最后打印时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastPrintTime;

    /**
     * 作废原因
     */
    private String voidReason;

    /**
     * 作废经办人
     */
    private String voidBy;

    /**
     * 作废时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime voidTime;

    /**
     * 重开来源证明ID（本证是作废谁之后重开的）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long origCertId;

    /**
     * 死因监测上报状态（1-未上报 2-已上报 3-上报失败）
     */
    private Integer reportStatus;

    /**
     * 上报时限
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportDeadline;

    /**
     * 上报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reportTime;

    /**
     * 上报回执编号/区域死因监测编号
     */
    private String reportNo;

    /**
     * 上报失败原因
     */
    private String reportError;

    /**
     * 上报报文（JSON 文本，TEXT 列）：走 updateById 写入；读取一律用 VO 的显式列，不在 WHERE 里引用它（TEXT 参与比较会炸）
     */
    private String reportPayload;

    /**
     * 最近一次超时催报时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime notifyTime;
}
