package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 住院转科轨迹（P4.2：发起 → 转入科室接收 → 停原医嘱 + 换科室换床 + 回写病历）。
 *
 * <p><b>为什么不把转科信息塞进入院记录</b>：一次住院可以转多次（A→B→C），
 * 轨迹是 1:N，admission 上的几个字段只能记住"最后一次"。而且科室改名、床位重排之后，
 * 历史转科单必须仍然是当时的样子 —— 所以科室名/病区名/床号一律是<b>快照</b>。
 *
 * <p>字段与住院转科轨迹 <b>一一对应</b>（多一个库里没有的列 → 全表 select 直接 500）。
 * 本表主键是 {@code id}，故继承 {@link BaseEntity}；注意 BaseEntity 固定映射备注 /
 * 删除标记等列，建表脚本里必须有它们。
 *
 * <p>时间字段落库前必须 truncate 到秒（库表是 DATETIME(0)，MySQL 会四舍五入 →
 * "写进去的 ≠ 读回来的"）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_transfer")
public class BizInpatientTransfer extends BaseEntity implements Serializable {

    /**
     * 转科单号（ZK + yyyyMMdd + 4位序号）
     */
    private String transferNo;

    /**
     * 入院ID（入院记录的入院ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /**
     * 入院号
     */
    private String admissionNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 转出科室ID（本条轨迹发起时患者所在科室）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromDeptId;

    /**
     * 转出科室名称
     */
    private String fromDeptName;

    /**
     * 转出病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromWardId;

    /**
     * 转出病区名称
     */
    private String fromWardName;

    /**
     * 转出床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fromBedId;

    /**
     * 转出床位号
     */
    private String fromBedNo;

    /**
     * 转入科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toDeptId;

    /**
     * 转入科室名称
     */
    private String toDeptName;

    /**
     * 转入病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toWardId;

    /**
     * 转入病区名称
     */
    private String toWardName;

    /**
     * 转入床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long toBedId;

    /**
     * 转入床位号
     */
    private String toBedNo;

    /**
     * 转科类型：1-普通转科 2-急诊转科 3-转入ICU 4-ICU转出
     */
    private Integer transferType;

    /**
     * 转科原因（转科是一个医疗决定，必须写清）
     */
    private String transferReason;

    /**
     * 发起转科时该次住院的已住院天数
     */
    private Integer hospitalDays;

    /**
     * 接收时随之停止的长期医嘱条数
     */
    private Integer stopOrdersCount;

    /**
     * 医嘱处置说明（停不掉的医嘱在这里写明，绝不静默）
     */
    private String orderRemark;

    /**
     * 转出方发起医生ID（员工ID，不是用户的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 转出方发起医生姓名
     */
    private String applyDoctorName;

    /**
     * 转入方接收医生ID（员工ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long receiveDoctorId;

    /**
     * 转入方接收医生姓名
     */
    private String receiveDoctorName;

    /**
     * 回写的住院病历ID（住院病历文书的ID，四核对"病历"一侧的锚点）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 发起时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;

    /**
     * 接收时间（转科真正生效的时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

    /**
     * 转科状态：0-待接收 1-已完成 2-已取消
     */
    private Integer transferStatus;

    /**
     * 取消原因（仅待接收可取消）
     */
    private String cancelReason;
}
