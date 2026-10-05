package com.his.patient.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 患者信息出参
 */
@Data
public class PatientVO {
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
     *
     * <p><b>列表接口（listPage）不回传明文</b>：该字段在列表里恒为 null，展示用
     * {@link #idCardMasked}；明文只出现在 {@code getById / getDetailById}
     * （患者编辑弹窗需要真实值回写，脱敏值会被当成真号存库）。
     */
    private String idCard;
    /**
     * 身份证号脱敏值（列表展示用）：保留前 4 后 4，中间按位打码
     */
    private String idCardMasked;
    /**
     * 联系电话
     */
    /**
     * 联系电话。列表接口不回明文（见 phoneMasked），编辑弹窗走 getById 取全量
     */
    private String phone;
    /**
     * 联系电话脱敏值（列表展示用）：11 位手机号保留前 3 后 4（138****5678）
     */
    private String phoneMasked;
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
     *
     * <p><b>列表接口（listPage）不回传明文</b>：该字段在列表里恒为 null，
     * 展示用 {@link #medicalInsuranceNoMasked}；明文只出现在
     * {@code getById / getDetailById / patientUpsert}（编辑弹窗与详情需要真实值）。
     */
    private String medicalInsuranceNo;
    /**
     * 医保卡号（脱敏展示值，仅 listPage 回填）：保留前 4 后 4（短号全遮）
     */
    private String medicalInsuranceNoMasked;
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
     * 患者照片URL
     */
    private String photo;
    /**
     * 建档时间（患者基本信息的创建时间）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 最近就诊时间（结诊时由就诊域回写）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastVisitTime;
    /**
     * 最近就诊科室ID（结诊时回写）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lastVisitDept;
    /**
     * 最近就诊科室名（挂号单快照）
     */
    private String lastVisitDeptName;
    /**
     * 最近接诊医生ID（结诊时回写）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long lastVisitDoctor;
    /**
     * 最近接诊医生名（挂号单快照）
     */
    private String lastVisitDoctorName;
    /**
     * 首次就诊时间（结诊时由就诊域回写，取最早结诊时刻）
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
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
     * 状态：0-停用 1-启用
     */
    private Integer status;
    /**
     * 患者标签（含标签名称与颜色，供前端直接渲染彩色标签）
     *
     * <p>列表接口批量查询后回填，单条查询接口同样返回；无标签时为空数组而非 null。
     */
    private List<com.his.system.vo.SysPatientTagVO> tags;
    /**
     * 预约（挂号）次数：挂号信息（原挂号单）中该患者的未删除挂号记录数
     */
    private Integer appointCount;

    // 今日就诊（全局患者搜索排序用）
    // 由就诊域通过 PatientTodayVisitProvider 回填。为空 = 患者今天没有门诊就诊，
    // 前端据此把它归到「全院档案」而不是伪造一个「无就诊」文案（未判定 ≠ 正常）。

    /**
     * 今日门诊就诊状态：2-候诊中 3-就诊中 4-已就诊；null = 今日无门诊就诊
     */
    private Integer todayVisitStatus;
    /**
     * 今日就诊状态文案（由就诊域渲染，前端不自造码值映射）
     */
    private String todayVisitText;
    /**
     * 今日就诊科室名
     */
    private String todayVisitDept;
    /**
     * 今日接诊医生名
     */
    private String todayVisitDoctor;
    /**
     * 今日门诊序号，如「3 号」
     */
    private String todayVisitQueueNo;
    /**
     * 是否属于当前登录用户本人/本科室的业务 —— 决定是否置顶到列表最前
     */
    private Boolean todayVisitMine;
}
