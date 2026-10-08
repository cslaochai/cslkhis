package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.his.common.base.BaseEntity;
import com.his.patient.enums.InpatientLeaveTypeEnum;
import com.his.patient.enums.LeaveStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 住院患者请假/离院登记单（住院请假登记，sql/162）。
 *
 * <p>一张单一次请假：医师审批（批准即电子签名业务类型=10）→ 患方签署「离院风险告知与责任承诺书」
 * （手写签名 + 法定关系 + 联系电话，签署即登记实际离院时间）→ 返回销假。
 * 超期未归是查询时算的展示态（expectedReturnTime 已过且未销假），不落状态列；
 * 超期处置（联系结果/上报对象）是护士的动作，落本表字段留痕 —— 「回去出事责任界定」的院内凭证全在这张单上。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_inpatient_leave")
public class BizInpatientLeave extends BaseEntity {

    /**
     * 单号（LV+yyyyMMdd+4位）
     */
    private String leaveNo;

    /**
     * 住院记录ID
     */
    private Long admissionId;
    /**
     * 患者ID
     */
    private Long patientId;
    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 患者编号
     */
    private String patientNo;
    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 申请时点所在科室ID
     */
    private Long deptId;
    /**
     * 科室名称
     */
    private String deptName;
    /**
     * 病区名称
     */
    private String wardName;
    /**
     * 床位号
     */
    private String bedNo;
    /**
     * 住院号
     */
    private String admissionNo;

    /**
     * 请假类别（1-临时外出当日往返 2-离院过夜 9-其他）
     */
    private Integer leaveType;
    /**
     * 请假事由（必填）
     */
    private String reason;
    /**
     * 去向
     */
    private String destination;
    /**
     * 随行/联系人姓名（必填）
     */
    private String companionName;
    /**
     * 随行人与患者关系
     */
    private Integer companionRelation;
    /**
     * 随行人联系电话（必填）
     */
    private String companionPhone;
    /**
     * 预计离院时间
     */
    private LocalDateTime expectedLeaveTime;
    /**
     * 预计返回时间
     */
    private LocalDateTime expectedReturnTime;
    /**
     * 申请时间
     */
    private LocalDateTime applyTime;
    /**
     * 申请人
     */
    private String applyBy;

    /**
     * 医师意见
     */
    private String doctorAdvice;
    /**
     * 审批时间
     */
    private LocalDateTime approveTime;
    /**
     * 审批医师ID
     */
    private Long doctorId;
    /**
     * 审批医师姓名
     */
    private String doctorName;
    /**
     * 拒绝理由
     */
    private String rejectReason;

    /**
     * 患方确认人姓名
     */
    private String confirmName;
    /**
     * 确认人与患者关系
     */
    private Integer confirmRelation;
    /**
     * 确认人联系电话
     */
    private String confirmPhone;
    /**
     * 患方手写签名（base64 PNG dataURL，承诺书打印原样贴图）
     */
    private String confirmSignature;
    /**
     * 患方签署时间
     */
    private LocalDateTime confirmTime;

    /**
     * 实际离院时间
     */
    private LocalDateTime actualLeaveTime;
    /**
     * 实际返回时间
     */
    private LocalDateTime actualReturnTime;
    /**
     * 返回情况备注
     */
    private String returnNote;
    /**
     * 销假经办人
     */
    private String returnBy;

    /**
     * 状态（1-待审批 2-已批准 3-已离院 4-已返回 5-已拒绝 6-已取消）
     */
    private Integer leaveStatus;

    /**
     * 超期联系结果（1-联系上并约定返回 2-联系不上 3-家属）
     */
    private Integer overdueContactResult;
    /**
     * 超期处置备注
     */
    private String overdueContactNote;
    /**
     * 超期处置时间
     */
    private LocalDateTime overdueContactTime;
    /**
     * 超期处置人
     */
    private String overdueContactBy;
    /**
     * 上报对象（1-主管医师 2-病区护士长 3-医务科）
     */
    private Integer reportTo;

    /**
     * 最后打印人
     */
    private String printerName;
    /**
     * 承诺书打印次数
     */
    private Integer printCount;
    /**
     * 最后打印时间
     */
    private LocalDateTime lastPrintTime;

    /**
     * 取消原因
     */
    private String cancelReason;
    /**
     * 取消经办人
     */
    private String cancelBy;
    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 电子签名状态（0-未签名 1-已签名 2-签名已作废，ObjectSignStatus 同码）
     */
    private Integer signStatus;
    /**
     * 当前有效签名ID
     */
    private Long signId;
    /**
     * 签名时刻
     */
    private LocalDateTime signedTime;

    public static String statusText(Integer status) {
        if (status == null) {
            return "—";
        }
        return LeaveStatusEnum.getText(status);
    }

    public static String typeText(Integer type) {
        if (type == null) {
            return "—";
        }
        return InpatientLeaveTypeEnum.getText(type);
    }
}
