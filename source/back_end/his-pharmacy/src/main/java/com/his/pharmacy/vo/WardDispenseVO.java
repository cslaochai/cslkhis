package com.his.pharmacy.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 住院摆药单 VO（主单 + 明细）。
 */
@Data
public class WardDispenseVO implements Serializable {

    /** 主键ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /** 摆药单号 WD+yyyyMMdd+4位 */
    private String dispenseNo;

    /** 摆药日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dispenseDate;

    /** 入院ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long admissionId;

    /** 患者ID */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long patientId;

    /** 患者编号（快照） */
    private String patientNo;

    /** 患者姓名（快照） */
    private String patientName;

    /** 病区ID（快照） */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wardId;

    /** 病区名称（快照） */
    private String wardName;

    /**
     * 主单状态：1-待配药 2-配药中 3-已配药 4-已核对 5-已退药
     */
    private Integer status;

    /** 生成人（药房） */
    private String generateBy;

    /** 生成时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generateTime;

    /** 备注 */
    private String remark;

    /**
     * 明细（getDetailById 时填充；列表页为 null）
     */
    private List<WardDispenseItemVO> items;
}
