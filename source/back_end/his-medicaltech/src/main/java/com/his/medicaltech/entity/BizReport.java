package com.his.medicaltech.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableLogic;


/**
 * 报告单
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_report")
public class BizReport extends BaseEntity {
    /** 逻辑删除标志（0 未删除 1 已删除） */
    @TableLogic
    private Integer delFlag;

    /** 备注 */
    private String remark;


    /**
     * 报告编号（唯一）
     */
    private String reportNo;

    /**
     * 报告类型（1-检查报告 2-检验报告）
     */
    private Integer reportType;

    /**
     * 检查/检验记录ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long recordId;

    /**
     * 检查/检验记录号
     */
    private String recordNo;

    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 患者姓名
     */
    private String patientName;

    /**
     * 性别（1-男 2-女）
     */
    private Integer gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 就诊日期
     */
    private LocalDate visitDate;

    /**
     * 项目名称
     */
    private String itemName;

    /**
     * 检查/检验科室
     */
    private String examDeptName;

    /**
     * 申请科室
     */
    private String applyDeptName;

    /**
     * 申请医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applyDoctorId;

    /**
     * 申请医生
     */
    private String applyDoctorName;

    /**
     * 临床诊断
     */
    private String clinicalDiagnosis;

    /**
     * 报告内容
     */
    private String reportContent;

    /**
     * 报告结论
     */
    private String conclusion;

    /**
     * 建议
     */
    private String suggestions;

    // 放射报告专用列（sql/138）

    /**
     * 检查方法（放射报告专用，如"胸部CT平扫+三维重建"；超声/内镜的报告不填）
     */
    private String examMethod;

    /**
     * 阴阳性（字典 his_positive_flag：0-未判定 1-阴性 2-阳性 3-未见异常）
     *
     * <p>放射科阳性率是质控硬指标，必须落在报告行的列上，不能事后从文本里分词猜。
     */
    private Integer positiveFlag;

    /**
     * 是否危急（0-否 1-是）：诊断医师写报告时判定
     */
    private Integer isCritical;

    /**
     * 报告书写人（与 auditBy 分开：写报告和审报告的不是同一个人，这是分岗的证据列）
     *
     * <p>由服务端从登录态写入，不接收入参 —— 接了就能把报告挂到别人名下。
     */
    private String writeBy;

    /**
     * 报告书写人员工ID（与 writeBy 并存：姓名给人看，ID 给程序判「不能自己审自己」）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long writeById;

    /**
     * 报告书写/提交时间
     */
    private LocalDateTime writeTime;

    /**
     * 使用的报告模板ID（放射报告模板的ID）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long templateId;

    /**
     * 退回原因（审核不通过时必填）
     */
    private String rejectReason;

    /**
     * 报告版本号（每次退回重写 +1）
     */
    private Integer reportVersion;

    /**
     * 已登记胶片张数（检查胶片用量汇总的冗余展示列；统计口径以检查胶片用量为准）
     */
    private Integer filmCount;

    /**
     * 报告状态（0-草稿 1-待审核 2-初审通过 3-已审核 4-已发布 5-已作废）
     */
    private Integer reportStatus;

    /**
     * 审核人（初审）
     */
    private String auditBy;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 复审人
     */
    private String audit2By;

    /**
     * 复审时间
     */
    private LocalDateTime audit2Time;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 发布人
     */
    private String publishBy;

    /**
     * 是否加急（0-否 1-是）
     */
    private Integer isUrgent;

    /**
     * 报告文件路径
     */
    private String reportFilePath;
}
