package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.his.system.vo.SysPatientTagVO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 患者完整信息出参（含过敏史、既往疾病史、手术外伤史、家族史）
 */
@Data
public class PatientDetailVO {
    /**
     * 患者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /**
     * 患者号（病历号/就诊卡号）
     */
    private String patientNo;
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
     * 年龄（岁）
     */
    private Integer age;
    /**
     * 身份证号
     */
    private String idCard;
    /**
     * 联系电话
     */
    private String phone;
    /**
     * 紧急联系人姓名
     */
    private String contactName;
    /**
     * 紧急联系人电话
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
     * 婚姻状况：1-未婚 2-已婚 3-离异 4-丧偶
     */
    private Integer maritalStatus;
    /**
     * 血型（如：A、B、O、AB）
     */
    private String bloodType;
    /**
     * 过敏史描述
     */
    private String allergyHistory;
    /**
     * 既往病史描述
     */
    private String medicalHistory;
    /**
     * 患者类型：1-自费 2-城镇职工医保 3-城乡居民医保 4-公费 5-其他
     */
    private Integer patientType;
    /**
     * 医保卡号
     */
    private String medicalInsuranceNo;
    /**
     * 医保类型（如：职工医保、居民医保）
     */
    private String medicalInsuranceType;
    /**
     * 卡片类型（1-就诊卡 2-身份证 3-医保卡）
     */
    private Integer cardType;
    /**
     * 证件号码
     */
    private String cardNo;
    /**
     * 以下 xxxMasked 一律由**服务端**加工（{@code SensitiveMaskUtils}），对应明文本接口恒为 null。
     *
     * <p>本 VO 只服务「患者档案弹框」这一条**纯展示**链路，所以脱敏放在出参里做，
     * 前端拿到什么就显示什么（不再自己写遮码函数——那等于明文照样在响应体里，抓包和日志照漏）。
     * **不要**把本接口当编辑回显用：需要明文回填表单的走 {@code /patient/getById}。
     */
    private String idCardMasked;
    private String phoneMasked;
    private String contactPhoneMasked;
    private String cardNoMasked;
    private String medicalInsuranceNoMasked;
    /**
     * 账户余额，单位：元
     */
    private BigDecimal balance;
    /**
     * 累计消费金额，单位：元
     */
    private BigDecimal totalExpense;
    /**
     * 就诊次数
     */
    private Integer visitCount;
    /**
     * 最近就诊时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastVisitTime;
    /**
     * 最近就诊科室ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lastVisitDept;
    /**
     * 最近就诊医生ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lastVisitDoctor;
    /**
     * 患者照片URL
     */
    private String photo;
    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;
    /**
     * 过敏史列表
     */
    private List<PatientAllergyVO> allergies;
    /**
     * 既往疾病史列表
     */
    private List<PatientPastDiseaseVO> pastDiseases;
    /**
     * 手术外伤史列表
     */
    private List<PatientSurgeryHistoryVO> surgeryHistories;
    /**
     * 家族史列表
     */
    private List<PatientFamilyHistoryVO> familyHistories;
    /**
     * 既往用药史列表
     *
     * <p>补这一组的直接原因：健康档案六组里只有它此前**没有实体、没有 Mapper、没有 Controller**，
     * 只被 CDR 的裸 SQL 读过 —— 也就是说「看得见、改不了」。六组补齐后才谈得上业务闭环。
     */
    private List<PatientMedicationHistoryVO> medications;
    /**
     * 联系人列表（主要联系人在前）
     */
    private List<PatientContactVO> contacts;
    /**
     * 患者标签列表（VIP / 高血压 / 建档提醒这类运营标记）。
     *
     * <p>标签不属于临床内容（不随岗位裁剪）：收费窗口靠它认人（VIP、欠费）、
     * 护士靠它识别注意事项，与 {@code PatientVO.tags} 同一口径。
     *
     * <p>放在这里而不是让前端再发一次 {@code /patient/tag/getByPatientId}：
     * 患者详情卡片一共就两个数据源（主档 + CDR），标签属于主档聚合，
     * 多一个并行请求就多一处「先渲染后跳变」的闪烁。
     */
    private List<SysPatientTagVO> tags;
}
