package com.his.patient.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ICU 监护记录入参（一条 = 一个时刻的床边记录）
 *
 * <p>GCS 总分与液体平衡由服务端回算，前端传了也不认；
 * 出入量、体温等留空表示该时刻未测，不补 0。
 */
@Data
public class IcuMonitorUpsertDTO {

    /** 为空=新增；非空=修改同一时刻的这条记录 */
    private Long id;

    /** 入科记录ID */
    @NotNull(message = "入科记录不能为空")
    private Long stayId;

    /** 记录时刻 */
    @NotNull(message = "记录时刻不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime recordTime;

    /** 体温 ℃ */
    private BigDecimal temperature;

    /** 脉搏次/分 */
    private Integer pulse;

    /** 呼吸次/分 */
    private Integer respiratory;

    /** 收缩压 mmHg */
    private Integer sbp;

    /** 舒张压 mmHg */
    private Integer dbp;

    /** 血氧饱和度（%） */
    private Integer spo2;

    /** GCS 睁眼 1~4 */
    private Integer gcsEye;

    /** GCS 语言 1~5 */
    private Integer gcsVerbal;

    /** GCS 运动 1~6 */
    private Integer gcsMotor;

    /** 瞳孔 */
    private String pupil;

    /** 中心静脉压 cmH2O */
    private BigDecimal cvp;

    /** 呼吸支持（1-鼻导管 2-无创 3-有创 4-脱机） */
    private Integer ventMode;

    /** 吸氧浓度（%） */
    private Integer fio2;

    /** 呼气末正压（cmH2O） */
    private BigDecimal peep;

    /** 入量 ml */
    private BigDecimal intakeMl;

    /** 出量 ml */
    private BigDecimal outputMl;

    /** 尿量 ml */
    private Integer urineMl;

    /** 人工气道/气管插管 0-无 1-有（0-无 1-有） */
    private Integer hasAirway;

    /** 中心静脉导管 0-无 1-有（0-无 1-有） */
    private Integer hasCvc;

    /** 动脉置管 0-无 1-有（0-无 1-有） */
    private Integer hasArterial;

    /** 导尿管 0-无 1-有（0-无 1-有） */
    private Integer hasCatheter;

    /** 引流管 0-无 1-有（0-无 1-有） */
    private Integer hasDrain;

    /** 病情观察 */
    private String conditionDesc;

    /** 处置/干预 */
    private String handling;

    /** 备注 */
    private String remark;
}
