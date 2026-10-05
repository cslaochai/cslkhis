package com.his.patient.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者基本信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_patient")
public class BizPatient extends BaseEntity {

    /**
     * 患者号
     */
    private String patientNo;

    /**
     * 主索引：已并入的主档患者ID（P5.1 EMPI）。
     *
     * <p><b>NULL = 自己就是主档</b>（绝大多数患者）；非空 = 本档案已并入该主档，
     * 查询时按主档归并（{@code id = ? OR master_id = ?}）。
     * <p>刻意不回填成自身 ID：NULL 与"指向自己"是两种语义，回填自身会让
     * "我是不是影子"的判断退化成"masterId == id"，很容易写错。
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long masterId;

    /**
     * 主索引状态：0-正常 1-已并入主档（本档案失效，只在追溯时可见）
     */
    private Integer mergeStatus;

    /**
     * 并入主档的时间
     */
    private LocalDateTime mergeTime;

    /**
     * 患者姓名
     */
    private String patientName;
    /**
     * 性别（1-男 2-女 9-未知）
     */
    private Integer gender;
    /**
     * 出生日期
     */
    private LocalDate birthDate;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 身份证号
     */
    private String idCard;
    /**
     * 手机号码
     */
    private String phone;
    /**
     * 联系人姓名
     */
    private String contactName;
    /**
     * 联系人电话
     */
    private String contactPhone;
    /**
     * 联系人关系（父母、配偶、子女等）
     */
    private String contactRelation;
    /**
     * 家庭住址
     */
    private String address;
    /**
     * 民族
     */
    private String nation;
    /**
     * 职业
     */
    private String occupation;
    /**
     * 婚姻状况（0-未婚 1-已婚 2-离异 3-丧偶）
     */
    private Integer maritalStatus;
    /**
     * 血型（A/B/O/AB）
     */
    private String bloodType;
    /**
     * 过敏史
     */
    private String allergyHistory;
    /**
     * 既往病史
     */
    private String medicalHistory;
    /**
     * 患者类型（参保性质）：1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-其他
     * （与列注释、字典数据(his_patient_type) 三方一致）
     */
    private Integer patientType;
    /**
     * 医保卡号
     */
    private String medicalInsuranceNo;
    /**
     * 医保类型
     */
    private String medicalInsuranceType;
    /**
     * 卡片类型（1-就诊卡 2-身份证 3-医保卡）
     */
    private Integer cardType;
    /**
     * 卡片号码
     */
    private String cardNo;
    /**
     * 账户余额
     */
    private BigDecimal balance;
    /**
     * 累计消费金额
     */
    private BigDecimal totalExpense;
    /**
     * 就诊次数
     */
    private Integer visitCount;

    /**
     * 最近就诊时间（结诊时由就诊域回写；此前无写入方废置，本次启用）
     */
    private LocalDateTime lastVisitTime;

    /**
     * 最近就诊科室ID（结诊时回写）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lastVisitDept;

    /**
     * 最近接诊医生ID（结诊时回写）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lastVisitDoctor;

    /**
     * 最近就诊科室名（挂号单快照，冗余存储不 join）
     */
    private String lastVisitDeptName;

    /**
     * 最近接诊医生名（挂号单快照）
     */
    private String lastVisitDoctorName;

    /**
     * 首次就诊时间（结诊时由就诊域回写，取最早结诊时刻）
     */
    private LocalDateTime firstVisitTime;

    /**
     * 首次就诊科室ID（挂号单快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long firstVisitDeptId;

    /**
     * 首次就诊科室名（挂号单快照）
     */
    private String firstVisitDeptName;

    /**
     * 首次接诊医生ID（挂号单快照）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long firstVisitDoctorId;

    /**
     * 首次接诊医生名（挂号单快照）
     */
    private String firstVisitDoctorName;

    /**
     * 患者照片
     */
    private String photo;
    /**
     * 状态（0-停用 1-启用）
     */
    private Integer status;
}
