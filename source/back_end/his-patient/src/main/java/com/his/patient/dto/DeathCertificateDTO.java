package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import com.his.common.validation.InEnum;
import com.his.patient.enums.DeathPlaceEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 死亡证明（死亡医学证明书）入参集合。
 *
 * <p>说明类字段（诊断、审核意见、作废原因、纠纷情况）一律不在入参层挂 {@code @Size}：
 * 服务端按列宽截断，让「用户粘贴了一长段说明」变成请求 400 是错的（AGENTS.md 第 3 条）。
 * 一般项目（姓名/性别/民族/出生日期/身份证/职业/婚姻）不接收前端值，一律服务端按住院重查快照。
 */
public class DeathCertificateDTO {

    /** 分页查询（证明台账 + 上报台账共用） */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class QueryPage extends PageParam {
        /** 证明编号 / 死者姓名 / 根本死因关键字 */
        private String keyword;

        /** 状态（1-草稿 2-已审核 3-已开具 4-已作废） */
        private Integer certStatus;

        /** 死因监测上报状态（1-未上报 2-已上报 3-上报失败） */
        private Integer reportStatus;

        /** 死亡地点（1-医院 2-来院途中 3-家中 4-民政管理机构 5-其他机构 9-未指明） */
        private Integer deathPlace;

        /** 死亡科室ID */
        private Long deathDeptId;

        /** 死亡时间起（含当天 00:00:00） */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate startDate;

        /** 死亡时间止（服务端补 23:59:59 全天边界） */
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate endDate;

        /** 1-只看已逾上报时限且未上报成功的证明 */
        private Integer overdue;
    }

    /** 死因链行（Ⅰ部分按 seqNo 顺序即 a→b→c→d，链尾为根本死因；Ⅱ部分其他疾病） */
    @Data
    public static class CauseRow {
        private Integer part;

        @NotNull(message = "死因链行序不能为空")
        private Integer seqNo;

        private String icdCode;

        @NotBlank(message = "死因链每行必须填写疾病或情况名称")
        private String icdName;

        /** 发病至死亡间隔（文本，如「30分钟」「10年」） */
        private String intervalText;
    }

    /** 填写/修改证明（草稿与已审核可改；已开具禁改，只能作废重开） */
    @Data
    public static class Upsert {
        /** 主键（雪花ID） */
        private Long id;

        /** 住院记录ID */
        @NotNull(message = "请选择住院记录")
        private Long admissionId;

        /** 死亡时间 */
        @NotNull(message = "死亡时间不能为空")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime deathTime;

        /** 死亡地点（1-医院 2-来院途中 3-家中 4-民政管理机构 5-其他机构 9-未指明） */
        @NotNull(message = "死亡地点不能为空")
        @InEnum(value = DeathPlaceEnum.class, message = "死亡地点取值不合法（见字典 his_death_place）")
        private Integer deathPlace;

        /** 医院内死亡必填（服务端按住院快照兜底为出院时所在科室） */
        private Long deathDeptId;

        /** 死亡诊断 */
        @NotBlank(message = "死亡诊断不能为空")
        private String clinicalDiagnosis;

        /** 根本死因ICD-10编码 */
        private String underlyingIcdCode;

        /** 根本死因名称（快照） */
        private String underlyingIcdName;

        /** 既往病史 */
        private String pastHistory;

        /** 是否尸检（0-否 1-是） */
        private Integer autopsyFlag;

        /** 尸检结论/病理诊断 */
        private String autopsyResult;

        /** 死者近亲属姓名 */
        private String relativeName;

        /** 与死者关系 */
        private String relativeRelation;

        /** 近亲属联系电话 */
        private String relativePhone;

        /** 填表医师（法定签名位，应为诊治医师）；不传按当前登录人 */
        private Long physicianId;

        /** 填表医师姓名 */
        private String physicianName;

        /** 填表时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime fillTime;

        /** 死因链：整体替换（先物理删旧行再插新行），一次提交全量 */
        @Valid
        private List<CauseRow> causes;

        /** 备注 */
        private String remark;
    }

    /** 审核（1→2） */
    @Data
    public static class Audit {
        /** 主键（雪花ID） */
        @NotNull(message = "证明ID不能为空")
        private Long id;

        private String opinion;
    }

    /** 签发（2→3）：审核通过 + 该住院已办「死亡」离院 + 死因链完整，三者齐了才是对外凭证 */
    @Data
    public static class Issue {
        /** 主键（雪花ID） */
        @NotNull(message = "证明ID不能为空")
        private Long id;
    }

    /** 作废（1/2/3→4）：必填原因，之后才能重开新证 */
    @Data
    public static class VoidCert {
        /** 主键（雪花ID） */
        @NotNull(message = "证明ID不能为空")
        private Long id;

        /** 原因 */
        @NotBlank(message = "作废原因必填（写清错在哪，重开才能对上）")
        private String reason;
    }

    /** 打印回执（四联打印一次计数一次，法定文书打印留痕） */
    @Data
    public static class Print {
        /** 主键（雪花ID） */
        @NotNull(message = "证明ID不能为空")
        private Long id;
    }
}
