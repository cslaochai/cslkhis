package com.his.patient.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 病区床位图（一张图看全科室床位与在院患者）
 *
 * <p><b>一行床 = 一张卡</b>：dev 库存在「一张床挂着多条在院记录」的脏数据（历史验证夹具），
 * 占用者由 Mapper 里的相关子查询定死为「优先床位上记的那个患者，其次最近入院的一条」，
 * 所以这里不能用 LEFT JOIN 入院记录，否则一张床会渲染出六张卡。
 *
 * <p>{@code beds} 含空床（空闲/维修/锁定），前端据此画满格；统计口径见 {@link Summary}。
 */
@Data
public class BedMapVO {

    /** 科室ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /** 科室名称 */
    private String deptName;

    /** 当前收窄到的病区，null = 全院区 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 病区名称（快照） */
    private String wardName;

    /** 小结 */
    private Summary summary = new Summary();

    /** 可切换的科室（已按登录态的授权范围收口，前端不再拉全院科室表） */
    private List<DeptOption> deptOptions;

    /** 当前科室下真正有床位的病区 */
    private List<WardOption> wardOptions;

    private List<BedCard> beds;

    /** 顶部统计条：全部按本次返回的床位现算，与卡片严格自洽 */
    @Data
    public static class Summary {
        private int totalBeds;
        private int occupied;
        private int free;
        private int repair;
        private int locked;
        /** 床位使用率（%），= 占用 / (总数 - 维修 - 锁定)，不可用床不参与分母 */
        private BigDecimal usageRate;
        /** 今日新入 */
        private int newToday;
        /** 有过敏史 */
        private int allergyCount;
        /** 术后 0~30 天（含） */
        private int postOpCount;
        /** 病案首页标记危重 */
        private int criticalCount;
        /** 特级护理 */
        private int levelSpecial;
        /** 一级护理 */
        private int levelOne;
        /** 二级护理 */
        private int levelTwo;
        /** 三级护理 */
        private int levelThree;
        /** 占床但无护理级别记录（不等于"不需要护理"，如实标未评估） */
        private int levelUnknown;
    }

    @Data
    public static class DeptOption {
        /** 科室ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        /** 科室名称 */
        private String deptName;

        private int occupied;

        /** 已被床位中心锁定预留的床（bed_status=3）：不空闲，但也没人躺着 */
        private int reserved;

        /** 总条数 */
        private int total;
    }

    @Data
    public static class WardOption {
        /** 病区ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;

        /** 病区名称（快照） */
        private String wardName;

        /** 病区归属科室，服务端用它校验 wardId 是否落在已收口的科室内 */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        private int occupied;

        private int reserved;

        /** 总条数 */
        private int total;
    }

    /** 一张床位卡 */
    @Data
    public static class BedCard {
        /** 床位ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long bedId;

        /** 床位号 */
        private String bedNo;

        /** 0-普通床 ICU-VIP 等，取床位的床位类型（字典 his_bed_type 口径：normal/ICU/VIP） */
        private String bedType;

        /** 0-维修 1-空闲 2-占用 3-锁定 */
        private Integer bedStatus;

        private String bedStatusText;

        /** 病区ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long wardId;

        /** 病区名称（快照） */
        private String wardName;

        /** 科室ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long deptId;

        /** 科室名称 */
        private String deptName;

        /** 入院ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long admissionId;

        /** 患者ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long patientId;

        /** 患者姓名 */
        private String patientName;

        /** 1-男 2-女 9-未知，文案走前端 lib/patientGender 唯一口径 */
        private Integer gender;

        /** 年龄 */
        private Integer age;

        /** 医生ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long doctorId;

        /** 医生姓名 */
        private String doctorName;

        /** 入院时刻（已格式化，避免前端再处理时区） */
        private String admitTime;

        /** 入院至今的日历天数 */
        private Integer admitDays;

        /** 今日新入 */
        private Boolean newToday;

        /** 护理级别：1-特级 2-一级 3-二级 4-三级，null=无护理记录（未评估） */
        private Integer nursingLevel;

        private String nursingLevelText;

        /** 主要手术名（无手术为 null） */
        private String mainOperationName;

        /** 术后天数 = 今天 - 主要手术日期，null=无主手术 */
        private Integer postOpDays;

        /** 病案首页 is_critical=1 */
        private Boolean critical;

        /** 过敏史原文，空串=无记录 */
        private String allergyHistory;

        /** 在治医嘱条数（已校对/执行中） */
        private Integer activeOrderCount;

        /** 备注 */
        private String remark;

        // 预留去向（床位服务中心）
        //
        // bed_status=3 在字典里叫「锁定」，但光看这个状态没人知道床留给谁了 ——
        // 护士站看到的是一张"写着锁定、点开什么都没有"的床，床位中心也没法在图上直接放人。
        // 这里把在途预留（床位调配台账.alloc_status=1）带出来，图才具备调配意义。
        // 没有在途预留的锁定床（如手工锁床）这些字段全为 null，按"锁定（无预留记录）"渲染。

        /** 等床记录ID */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long reservedWaitId;

        /** 等待号（DC+日期+序号） */
        private String reservedWaitNo;

        /** 被预留给的患者 */
        @JsonSerialize(using = ToStringSerializer.class)
        private Long reservedPatientId;

        private String reservedPatientName;

        /** 优先级 1普通 2急 3危重，文案走 BedCenterLabels 唯一口径 */
        private Integer reservedPriority;

        private String reservedPriorityText;

        /** 调配单号（TP+日期+序号） */
        private String allocateNo;

        /** 调配类型 1本科室预留 2跨科调配 3急诊占床 */
        private Integer allocType;

        private String allocTypeText;

        /** 是否为跨科调配（use_dept_id ≠ own_dept_id） */
        private Boolean crossDept;

        /** 预留时刻（已格式化） */
        private String reserveTime;

        // 图上的动作可用性：后端算，前端不自判状态机

        /** 空闲床 → 可预留给等床患者 */
        private Boolean canReserve;

        /** 预留床 → 可释放（退回队列） */
        private Boolean canRelease;

        /** 预留床 → 可直接办入院 */
        private Boolean canAdmit;
    }
}
