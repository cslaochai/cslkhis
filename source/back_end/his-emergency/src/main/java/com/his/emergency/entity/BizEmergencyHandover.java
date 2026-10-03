package com.his.emergency.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 急诊交班单（sql/153）。
 *
 * <p><b>本表是"责任移交凭证"，不是实时统计</b>（同收费员班结单的口径）：
 * 五个计数字段是提交那一刻逐条点完之后的定格值，事后有人离院、有新登记都不会改这张单。
 * 要看实时数去查 {@code /emergency/list}，拿台账当实时口径必然对不上。
 *
 * <p><b>为什么要这张表</b>：sql/145 让"没人管"变得可见（待派单池 + 超时催办），
 * 但可见不等于交接。急诊三班 24 小时连续运转，系统里原先没有任何"班次边界"，
 * 于是池子里那几条无主行会在三个班之间原样传下去，而同一条急诊的催办信
 * 发给"当班医生"——"当班"每天换三个人，谁都被催过、谁都不觉得是自己的。
 * 交班动作把这件事变成事实：<b>交出人必须把本科室该他负责的与无人负责的行逐条点名</b>
 * （漏一条提交不了），接班人由此在系统里被写进这些行的责任位。
 *
 * <p>{@code periodBegin} = 交出人在本科室上一次交班的 {@code periodEnd}（首次取当日 00:00:00），
 * 用滚动区间而不是固定班次时刻：固定时刻要求系统掌握护士/医生班表到分钟，
 * 而排班信息只排医生，滚动区间语义等价（"从我上次交班到现在"）且无外部依赖。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_emergency_handover")
public class BizEmergencyHandover extends BaseEntity {

    /**
     * 交班单号（EJ + yyyyMMdd + 4 位序号，服务端生成，唯一键）
     */
    private String handoverNo;

    /**
     * 交班科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 交班科室名称（快照）
     */
    private String deptName;

    /**
     * 交出人员工ID（登录态写入，不信前端）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromEmpId;

    /**
     * 交出人姓名（快照）
     */
    private String fromEmpName;

    /**
     * 接班人员工ID（整单默认接续责任人；明细可逐条改指他人）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long takeEmpId;

    /**
     * 接班人姓名（服务端按 ID 补齐的快照）
     */
    private String takeEmpName;

    /**
     * 班次名（交出人当日该科室在岗排班的班次名；查不到为空，不编造）
     */
    private String shiftName;

    /**
     * 本班区间起（不含）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodBegin;

    /**
     * 本班区间止（含，= 本次提交时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime periodEnd;

    /**
     * 本次移交未闭环人数（定格）
     */
    private Integer pendingCount;

    /**
     * 其中交班前无人指派的条数（定格，= 本次清零掉的池子行数）
     */
    private Integer poolCount;

    /**
     * 其中候诊已超时的条数（定格）
     */
    private Integer overdueCount;

    /**
     * 其中留观中的条数（定格）
     */
    private Integer observationCount;

    /**
     * 其中留观已超时限的条数（定格）
     */
    private Integer obsOverLimitCount;

    /**
     * 整单交代备注
     */
    private String remark;
}
