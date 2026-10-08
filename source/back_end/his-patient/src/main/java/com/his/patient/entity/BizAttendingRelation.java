package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 住院管床关系（主管医生）—— sql/202。
 *
 * <p><b>它是「带时效的归属关系」，不是按天的排班事实</b>，这是本表最容易搞错的一点：
 * 「张三主管 3-12 床」从入院生效、到出院或转交失效，中间不会因为张三某天休息就消失一天。
 * 跟 {@code biz_staff_schedule}（一人一天一条事实）是完全两种东西，别混。
 *
 * <p><b>为什么非要单独建表</b>：住院医生此前只有门诊排班（{@code biz_schedule}），
 * 「这个在院患者归谁管」在库里查不出来 —— 只有 {@code biz_admission.admit_doctor_id} 一个裸字段，
 * 既没有生效/失效时间，也无法表达转交、协作、主诊组长。出了事不知道该找谁，只能靠科室自己记 Excel。
 *
 * <p><b>姓名/科室名是快照</b>：管床关系一旦落到病案里就是历史凭证，
 * 医生后来改了名、科室后来改了名，都不该把这条历史记录改掉。
 *
 * <p><b>删除一律物理删</b>：唯一键 {@code uk_adm_rel_emp} 不含删除标志，
 * 软删的行会继续占着「同一次住院 + 同类型 + 同一医生」这个键位，重建必撞重复键。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_attending_relation")
public class BizAttendingRelation extends BaseEntity {

    /**
     * 住院登记ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

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
     * 主管医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long employeeId;

    /**
     * 主管医生姓名
     */
    private String employeeName;

    /**
     * 科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;

    /**
     * 科室名称
     */
    private String deptName;

    /**
     * 病区ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /**
     * 床位ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long bedId;

    /**
     * 关系类型（1-主管 2-主诊组长 3-协作）
     */
    private Integer relationType;

    /**
     * 状态（1-有效 0-已结束）
     */
    private Integer status;

    /**
     * 生效时间（一般＝入院时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTime;

    /**
     * 失效时间（出院或转交时填）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;
}
