package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.his.common.base.PageParam;
import com.his.common.validation.InEnum;
import com.his.patient.enums.InpatientLeaveTypeEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 住院请假单入参（外层空壳 + 内部静态类，同 CriticalNoticeDTO 组织方式）。
 */
public class InpatientLeaveDTO {

    /**
     * 分页查询
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class QueryPage extends PageParam {
        /** 关键字 */
        private String keyword;
        /** 请假类别（1-临时外出当日往返 2-离院过夜 9-其他） */
        private Integer leaveType;
        /** 状态（1-待审批 2-已批准 3-已离院 4-已返回 5-已拒绝 6-已取消） */
        private Integer leaveStatus;
        /** 仅看超期未归（status=3 且 expected_return_time < now，查询时算） */
        private Boolean overdueOnly;
        /** 申请时点所在科室ID */
        private Long deptId;
        /** 开始日期 */
        private LocalDate startDate;
        /** 结束日期 */
        private LocalDate endDate;
    }

    /**
     * 填写/修改申请单（一般项目由服务端按住院重查快照；审批医师不由前端指定）
     */
    @Data
    public static class Upsert {
        /** 主键（雪花ID） */
        private Long id;
        /** 住院记录ID */
        @NotNull(message = "必须挂在一次住院记录上")
        private Long admissionId;
        /** 请假类别（1-临时外出当日往返 2-离院过夜 9-其他） */
        @NotNull(message = "请假类别不能为空")
        @InEnum(value = InpatientLeaveTypeEnum.class, message = "请假类别取值不合法（见字典 his_leave_type）")
        private Integer leaveType;
        /** 请假事由（必填） */
        @NotBlank(message = "请假事由不能为空")
        private String reason;
        /** 去向 */
        @NotBlank(message = "去向不能为空（写清去哪，责任界定的关键）")
        private String destination;
        /** 随行/联系人姓名（必填） */
        @NotBlank(message = "随行/联系人不能为空")
        @Size(min = 1, max = 50, message = "随行/联系人长度须在 1~50 字")
        private String companionName;
        /** 随行人与患者关系 */
        private Integer companionRelation;
        /**
         * 随行人电话：<b>刻意不加 @NotNull / @Size(min=1)</b> —— 列表 VO 不出联系方式、
         * 详情只出脱敏值，编辑草稿时前端拿不到明文回填，留空是合法的「沿用原值」语义。
         * 必填判定下沉到 Service（新建留空报错、编辑留空沿用），长度由服务端截断，
         * 入参层的 400 会抢在业务逻辑之前，等于把「编辑后直接保存」判成请求失败。
         */
        @Size(max = 20, message = "随行人电话不能超过 20 字")
        private String companionPhone;
        /** 预计离院时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull(message = "预计离院时间不能为空")
        private LocalDateTime expectedLeaveTime;
        /** 预计返回时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull(message = "预计返回时间不能为空")
        private LocalDateTime expectedReturnTime;
        /** 备注 */
        private String remark;
    }

    /**
     * 审批（allow=true 批准并电子签名锁定；allow=false 拒绝必填理由）
     */
    @Data
    public static class Approve {
        /** 主键（雪花ID） */
        @NotNull(message = "缺少请假单ID")
        private Long id;
        @NotNull(message = "缺少审批结论（allow=true 批准 / false 拒绝）")
        private Boolean allow;
        /** 医师意见是自由文本：批准时「必须有意见」由 Service 校验，长度由服务端截断到列宽 */
        private String doctorAdvice;
        /** 拒绝理由 */
        private String rejectReason;
        /** 签名留证 IP，由 Controller 从请求侧写入，不接受前端自报 */
        private String clientIp;
    }

    /**
     * 登记离院 = 患方签署「离院风险告知与责任承诺书」三要素 + 实际离院时间
     */
    @Data
    public static class Confirm {
        /** 主键（雪花ID） */
        @NotNull(message = "缺少请假单ID")
        private Long id;
        /** 患方确认人姓名 */
        @NotBlank(message = "患方确认人姓名不能为空")
        @Size(min = 1, max = 50, message = "确认人姓名长度须在 1~50 字")
        private String confirmName;
        /** 确认人与患者关系 */
        @NotNull(message = "确认人与患者的关系不能为空（责任界定必填）")
        private Integer confirmRelation;
        /** 确认人联系电话 */
        @NotBlank(message = "确认人联系电话不能为空")
        @Size(min = 1, max = 20, message = "确认人电话长度须在 1~20 字")
        private String confirmPhone;
        /** 手写签名 dataURL（data:image/png;base64,...），服务端校验前缀与长度 */
        @NotBlank(message = "患方手写签名不能为空（承诺书必须亲笔签署）")
        private String confirmSignature;
        /** 实际离院时间 */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime actualLeaveTime;
    }

    /**
     * 返回销假
     */
    @Data
    public static class Back {
        /** 主键（雪花ID） */
        @NotNull(message = "缺少请假单ID")
        private Long id;
        /** 返回情况备注 */
        private String returnNote;
    }

    /**
     * 取消（仅待审批/已批准；必填原因）
     */
    @Data
    public static class Cancel {
        /** 主键（雪花ID） */
        @NotNull(message = "缺少请假单ID")
        private Long id;
        /** 取消原因 */
        @NotBlank(message = "取消原因不能为空（写清为什么取消）")
        private String cancelReason;
    }

    /**
     * 超期处置记录（仅已离院且超期的单可记）
     */
    @Data
    public static class Contact {
        /** 主键（雪花ID） */
        @NotNull(message = "缺少请假单ID")
        private Long id;
        @NotNull(message = "联系结果不能为空")
        private Integer contactResult;
        private String contactNote;
        /** 上报对象（1-主管医师 2-病区护士长 3-医务科） */
        @NotNull(message = "上报对象不能为空（联系不上必须升级上报）")
        private Integer reportTo;
    }

    /**
     * 打印承诺书计数
     */
    @Data
    public static class Print {
        /** 主键（雪花ID） */
        @NotNull(message = "缺少请假单ID")
        private Long id;
    }
}
